package OBM::EntitiesFactory::contactFactory;

$VERSION = '1.0';

use OBM::EntitiesFactory::factory;
@ISA = ('OBM::EntitiesFactory::factory');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(
        _log
        dump
        );
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
        $self->_log( 'description du domaine père indéfini', 3 );
        return undef;
    }

    if( ref($parentDomain) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'description du domaine père incorrecte', 3 );
        return undef;
    }
    $self->{'parentDomain'} = $parentDomain;

    $self->{'domainId'} = $parentDomain->getId();
    if( ref($self->{'domainId'}) || ($self->{'domainId'} !~ /$regexp_id/) ) {
        $self->_log( 'identifiant de domaine \''.$self->{'domainId'}.'\' incorrect', 3 );
        return undef;
    }

    if( defined($ids) && (ref($ids) ne 'ARRAY') ) {
        $self->_log( 'liste d\'ID à traiter incorrecte', 3 );
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

    $self->_log( 'debut de traitement', 2 );

    if( $self->_loadContacts() ) {
        $self->_log( 'problème lors de l\'obtention de la description des utilisateurs du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 3 );
        return 0;
    }

    $self->{'running'} = 1;
    return $self->{'running'};
}


sub next {
    my $self = shift;

    $self->_log( 'obtention de l\'entité suivante', 2 );

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
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 2 );
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

    $self->_log( 'chargement des contacts du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 2 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return 1;
    }

    my $query = 'SELECT Contact.*,
                        ContactEntity.contactentity_entity_id,
                        Company.*
                 FROM Contact
                 INNER JOIN ContactEntity ON ContactEntity.contactentity_contact_id=Contact.contact_id
                 LEFT JOIN Company ON Company.company_id=Contact.contact_company_id
                 WHERE Contact.contact_domain_id='.$self->{'domainId'}.'
                 AND Contact.contact_privacy = 0';

    if( $self->{'ids'} ) {
        $query .= ' AND Contact.contact_id IN ('.join( ', ', @{$self->{'ids'}}).')';
    }

    $query .= ' ORDER BY Contact.contact_lastname';

    if( !defined($dbHandler->execQuery( $query, \$self->{'entitiesDescList'} )) ) {
        $self->_log( 'chargement des contacts depuis la BD impossible', 3 );
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
        $self->_log( 'connexion à la base de données impossible', 4 );
        return undef;
    }

    my $phonesList;
    if( !defined($dbHandler->execQuery( $query, \$phonesList ) ) ) {
        $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription().' depuis la BD impossible', 3 );
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
        $self->_log( 'connexion à la base de données impossible', 4 );
        return undef;
    }

    my $addressesList;
    if( !defined($dbHandler->execQuery( $query, \$addressesList ) ) ) {
        $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription().' depuis la BD impossible', 3 );
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
        $self->_log( 'connexion à la base de données impossible', 4 );
        return undef;
    }

    my $addressesList;
    if( !defined($dbHandler->execQuery( $query, \$addressesList ) ) ) {
        $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription().' depuis la BD impossible', 3 );
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
        $self->_log( 'connexion à la base de données impossible', 4 );
        return undef;
    }

    my $websitesList;
    if( !defined($dbHandler->execQuery( $query, \$websitesList ) ) ) {
        $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription().' depuis la BD impossible', 3 );
        return undef;
    }

    return $websitesList->fetchall_arrayref({});
}
