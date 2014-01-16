#################################################################################
# Copyright (C) 2011-2014 Linagora
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option) any
# later version, provided you comply with the Additional Terms applicable for OBM
# software by Linagora pursuant to Section 7 of the GNU Affero General Public
# License, subsections (b), (c), and (e), pursuant to which you must notably (i)
# retain the displaying by the interactive user interfaces of the “OBM, Free
# Communication by Linagora” Logo with the “You are using the Open Source and
# free version of OBM developed and supported by Linagora. Contribute to OBM R&D
# by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
# links between OBM and obm.org, between Linagora and linagora.com, as well as
# between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
# from infringing Linagora intellectual property rights over its trademarks and
# commercial brands. Other Additional Terms apply, see
# <http://www.linagora.com/licenses/> for more details.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License and
# its applicable Additional Terms for OBM along with this program. If not, see
# <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
# version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
# applicable to the OBM software.
#################################################################################


package OBM::EntitiesFactory::contactFactory;

$VERSION = '1.0';

use OBM::EntitiesFactory::factory;
use OBM::Log::log;
@ISA = ('OBM::EntitiesFactory::factory', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $updateType, $parentDomain, $ids ) = @_;

    my $self = bless { }, $class;

    $self->{'updateType'} = $updateType;
    if( !$self->_checkUpdateType() ) {
        return undef;
    }

    if( !defined($parentDomain) ) {
        $self->_log( 'description du domaine père indéfini', 1 );
        return undef;
    }

    if( ref($parentDomain) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'description du domaine père incorrecte', 1 );
        return undef;
    }
    $self->{'parentDomain'} = $parentDomain;

    $self->{'domainId'} = $parentDomain->getId();
    if( ref($self->{'domainId'}) || ($self->{'domainId'} !~ /$regexp_id/) ) {
        $self->_log( 'identifiant de domaine \''.$self->{'domainId'}.'\' incorrect', 1 );
        return undef;
    }

    if( defined($ids) && (ref($ids) ne 'ARRAY') ) {
        $self->_log( 'liste d\'ID à traiter incorrecte', 1 );
        return undef;
    }

    if( $#{$ids} >= 0 ) {
        $self->{'ids'} = $ids;
    }

    $self->{'running'} = undef;
    $self->{'currentEntity'} = undef;
    $self->{'contactDescList'} = undef;


    return $self;
}


sub _start {
    my $self = shift;

    $self->_log( 'debut de traitement', 4 );

    if( $self->_loadContacts() ) {
        $self->_log( 'problème lors de l\'obtention de la description des utilisateurs du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 1 );
        return 0;
    }

    $self->{'running'} = 1;
    return $self->{'running'};
}


sub next {
    my $self = shift;

    $self->_log( 'obtention de l\'entité suivante', 4 );

    if( !$self->isRunning() ) {
        if( !$self->_start() ) {
            $self->_reset();
            return undef;
        }
    }

    while( defined($self->{'entitiesDescList'}) && (my $userDesc = $self->{'entitiesDescList'}->fetchrow_hashref()) ) {
        require OBM::Entities::obmContact;
        if( !(my $current = OBM::Entities::obmContact->new( $self->{'parentDomain'}, $userDesc )) ) {
            next;
        }else {
            $self->{'currentEntity'} = $current;

            SWITCH: {
                if( $self->{'updateType'} =~ /^(UPDATE_ALL|UPDATE_ENTITY|UPDATE_LINKS)$/ ) {
                    if( $self->_loadContactLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                        next;
                    }

                    $self->_log( 'mise à jour de l\'entité et des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateEntity();
                    $self->{'currentEntity'}->setUpdateLinks();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'DELETE' ) {
                    $self->_log( 'suppression de l\'entité, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setDelete();
                    last SWITCH;
                }

                $self->_log( 'type de mise à jour inconnu \''.$self->{'updateType'}.'\'', 0 );
                return undef;
            }

            return $self->{'currentEntity'};
        }
    }

    $self->{'currentEntity'} = undef;

    return undef;
}


sub _loadContacts {
    my $self = shift;

    $self->_log( 'chargement des contacts du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 4 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return 1;
    }

    my $query = 'SELECT Contact.*,
                        ContactEntity.contactentity_entity_id,
                        Company.*
                 FROM Contact
                 INNER JOIN ContactEntity ON ContactEntity.contactentity_contact_id=Contact.contact_id
                 LEFT JOIN Company ON Company.company_id=Contact.contact_company_id
                 WHERE Contact.contact_domain_id='.$self->{'domainId'};

    if( $self->{'ids'} ) {
        $query .= ' AND Contact.contact_id IN ('.join( ', ', @{$self->{'ids'}}).')';
    }

#    $query .= ' ORDER BY Contact.contact_lastname';

    if( !defined($dbHandler->execQuery( $query, \$self->{'entitiesDescList'} )) ) {
        $self->_log( 'chargement des contacts depuis la BD impossible', 1 );
        return 1;
    }

    return 0;
}


sub _loadContactLinks {
    my $self = shift;
    my $links;

    my $entityId = $self->{'currentEntity'}->getId();

    $links->{'phone'} = $self->_loadContactPhones();
    $links->{'address'} = $self->_loadContactAddresses();
    $links->{'email'} = $self->_loadContactEmail();
    $links->{'website'} = $self->_loadContactWebsite();

    $self->{'currentEntity'}->setLinks( $links );

    return 0;
}


sub _loadContactPhones {
    my $self = shift;

    my $entityId = $self->{'currentEntity'}->getId();

    my $query = 'SELECT Phone.*
                 FROM Phone
                 INNER JOIN ContactEntity ON ContactEntity.contactentity_contact_id='.$entityId.'
                 WHERE Phone.phone_entity_id=ContactEntity.contactentity_entity_id';

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return undef;
    }

    my $phonesList;
    if( !defined($dbHandler->execQuery( $query, \$phonesList ) ) ) {
        $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription().' depuis la BD impossible', 1 );
        return undef;
    }

    return $phonesList->fetchall_arrayref({});
}


sub _loadContactAddresses {
    my $self = shift;

    my $entityId = $self->{'currentEntity'}->getId();

    my $query = 'SELECT Address.*
                 FROM Address
                 INNER JOIN ContactEntity ON ContactEntity.contactentity_contact_id='.$entityId.'
                 WHERE Address.address_entity_id=ContactEntity.contactentity_entity_id';

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return undef;
    }

    my $addressesList;
    if( !defined($dbHandler->execQuery( $query, \$addressesList ) ) ) {
        $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription().' depuis la BD impossible', 1 );
        return undef;
    }

    return $addressesList->fetchall_arrayref({});
}


sub _loadContactEmail {
    my $self = shift;

    my $entityId = $self->{'currentEntity'}->getId();

    my $query = 'SELECT Email.*
                 FROM Email
                 INNER JOIN ContactEntity ON ContactEntity.contactentity_contact_id='.$entityId.'
                 WHERE Email.email_entity_id=ContactEntity.contactentity_entity_id';

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return undef;
    }

    my $addressesList;
    if( !defined($dbHandler->execQuery( $query, \$addressesList ) ) ) {
        $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription().' depuis la BD impossible', 1 );
        return undef;
    }

    return $addressesList->fetchall_arrayref({});
}


sub _loadContactWebsite {
    my $self = shift;

    my $entityId = $self->{'currentEntity'}->getId();

    my $query = 'SELECT Website.*
                 FROM Website
                 INNER JOIN ContactEntity ON ContactEntity.contactentity_contact_id='.$entityId.'
                 WHERE Website.website_entity_id=ContactEntity.contactentity_entity_id';

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return undef;
    }

    my $websitesList;
    if( !defined($dbHandler->execQuery( $query, \$websitesList ) ) ) {
        $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription().' depuis la BD impossible', 1 );
        return undef;
    }

    return $websitesList->fetchall_arrayref({});
}
