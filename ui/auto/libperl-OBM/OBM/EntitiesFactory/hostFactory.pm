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


package OBM::EntitiesFactory::hostFactory;

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
    $self->{'entitiesDescList'} = undef;


    return $self;
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

    while( defined($self->{'entitiesDescList'}) && (my $userHostDesc = $self->{'entitiesDescList'}->fetchrow_hashref()) ) {
        require OBM::Entities::obmHost;
        if( !($self->{'currentEntity'} = OBM::Entities::obmHost->new( $self->{'parentDomain'}, $userHostDesc )) ) {
            next;
        }else {
            if( $self->_getSambaServiceState() ) {
                $self->_log( 'probleme au chargement du status Samba de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                next;
            }
            if( !$self->_loadCurrentEntityCategories() ) {
                $self->_log( 'problème au chargement des informations de catégories de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                next;
            }
            if( !$self->_loadCurrentEntityFields() ) {
                $self->_log( 'problème au chargement des informations des champs spécifiques de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                next;
            }
            if( !$self->_loadCurrentEntityServiceProperty() ) {
                $self->_log( 'problème au chargement des informations des propriétés de service de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                next;
            }


            SWITCH: {
                if( $self->{'updateType'} eq 'UPDATE_ALL' ) {
                    if( $self->_loadHostLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                        next;
                    }

                    $self->_log( 'mise à jour de l\'entité, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateEntity();
                    $self->{'currentEntity'}->setUpdateLinks();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'UPDATE_ENTITY' ) {
                    $self->_log( 'mise à jour de l\'entité, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateEntity();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'UPDATE_LINKS' ) {
                    if( $self->_loadHostLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                        next;
                    }

                    $self->_log( 'mise à jour des liens, '.$self->{'currentEntity'}->getDescription(), 3 );

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


sub _loadEntities {
    my $self = shift;

    $self->_log( 'chargement des hôtes du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 4 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return 1;
    }

    my $hostTablePrefix = '';
    if( $self->{'updateType'} !~ /^(UPDATE_ALL|UPDATE_ENTITY)$/ ) {
        $hostTablePrefix = 'P_';
    }

    my $query = 'SELECT '.$hostTablePrefix.'Host.*,
                        current.host_name as host_name_current
                 FROM '.$hostTablePrefix.'Host
                 LEFT JOIN P_Host current ON current.host_id='.$hostTablePrefix.'Host.host_id
                 WHERE '.$hostTablePrefix.'Host.host_domain_id='.$self->{'domainId'};

    if( $self->{'ids'} ) {
        $query .= ' AND '.$hostTablePrefix.'Host.host_id IN ('.join( ', ', @{$self->{'ids'}} ).')';
    }

    $query .= ' ORDER BY '.$hostTablePrefix.'Host.host_name';

    if( !defined($dbHandler->execQuery( $query, \$self->{'entitiesDescList'} )) ) {
        $self->_log( 'chargement des hôtes depuis la BD impossible', 1 );
        return 1;
    }

    return 0;
}


sub _loadCurrentEntityCategories {
    my $self = shift;

    $self->_log( 'chargement des informations de catégories de l\'entité '.$self->{'currentEntity'}->getDescription(), 4 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return 0;
    }

    my $tablePrefix = '';
    if( $self->{'updateType'} !~ /^(UPDATE_ALL|UPDATE_ENTITY)$/ ) {
        $tablePrefix = 'P_';
    }

    my $query = 'SELECT Category.category_code,
                        Category.category_category
                 FROM '.$tablePrefix.'CategoryLink
                 INNER JOIN Category
                    ON '.$tablePrefix.'CategoryLink.categorylink_category_id = Category.category_id
                 INNER JOIN '.$tablePrefix.'HostEntity
                    ON '.$tablePrefix.'HostEntity.hostentity_entity_id = '.$tablePrefix.'CategoryLink.categorylink_entity_id
                 WHERE '.$tablePrefix.'HostEntity.hostentity_host_id = '.$self->{'currentEntity'}->getId();

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 0;
    }

    my %entityCategories;
    while( my($categoryValue, $categoryName) = $queryResult->fetchrow_array() ) {
        $entityCategories{$categoryName}->{$categoryValue} = 1;
    }

    while( my($key, $value) = each(%entityCategories) ) {
        my @keys = keys(%{$value});
        $entityCategories{$key} = \@keys;
    }

    return $self->{'currentEntity'}->setExtraDescription(\%entityCategories);
}


sub _loadCurrentEntityFields {
    my $self = shift;

    $self->_log( 'chargement des informations des champs spécifiques de l\'entité '.$self->{'currentEntity'}->getDescription(), 4 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return 0;
    }

    my $tablePrefix = '';
    if( $self->{'updateType'} !~ /^(UPDATE_ALL|UPDATE_ENTITY)$/ ) {
        $tablePrefix = 'P_';
    }

    my $query = 'SELECT '.$tablePrefix.'field.field,
                        '.$tablePrefix.'field.value
                 FROM '.$tablePrefix.'field
                 INNER JOIN '.$tablePrefix.'HostEntity
                    ON '.$tablePrefix.'HostEntity.hostentity_entity_id = '.$tablePrefix.'field.entity_id
                 WHERE '.$tablePrefix.'HostEntity.hostentity_host_id = '.$self->{'currentEntity'}->getId();

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 0;
    }

    my %entityFields;
    while( my($fieldName, $fieldValue) = $queryResult->fetchrow_array() ) {
        if($fieldValue) {
            $entityFields{$fieldName}->{$fieldValue} = 1;
        }
    }

    while( my($key, $value) = each(%entityFields) ) {
        my @keys = keys(%{$value});
        $entityFields{$key} = \@keys;
    }

    return $self->{'currentEntity'}->setExtraDescription(\%entityFields);
}


sub _loadCurrentEntityServiceProperty {
    my $self = shift;

    $self->_log( 'chargement des propriétés de services de l\'entité '.$self->{'currentEntity'}->getDescription(), 4 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return 0;
    }

    my $tablePrefix = '';
    if( $self->{'updateType'} !~ /^(UPDATE_ALL|UPDATE_ENTITY)$/ ) {
        $tablePrefix = 'P_';
    }

    my $query = 'SELECT serviceproperty_service,
                        serviceproperty_property,
                        serviceproperty_value
                 FROM '.$tablePrefix.'ServiceProperty
                 INNER JOIN '.$tablePrefix.'HostEntity ON hostentity_entity_id=serviceproperty_entity_id
                 WHERE hostentity_host_id='.$self->{'currentEntity'}->getId();

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 0;
    }

    my %entityServiceProperties;
    while( my($servicePropertyPrefix, $servicePropertyName, $servicePropertyValue) = $queryResult->fetchrow_array() ) {
        $entityServiceProperties{$servicePropertyPrefix.'_'.$servicePropertyName}->{$servicePropertyValue} = 1;
    }

    while( my($key, $values) = each(%entityServiceProperties) ) {
        my @keys = keys(%{$values});
        $entityServiceProperties{$key} = \@keys;
    }

    return $self->{'currentEntity'}->setExtraDescription(\%entityServiceProperties);
}


sub _loadHostLinks {
    my $self = shift;

    $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription(), 3 );

    return $self->_getSambaServiceState();
}


sub _getSambaServiceState {
    my $self = shift;

    $self->_log( 'obtention du status Samba de '.$self->{'currentEntity'}->getDescription(), 3 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return 1;
    }

    my $entityId = $self->{'currentEntity'}->getId();

    my $hostEntityTable = 'HostEntity';
    my $serviceTable = 'Service';
    if( $self->{'updateType'} =~ /^(SYSTEM_ALL|SYSTEM_ENTITY|SYSTEM_LINKS)$/ ) {
        $hostEntityTable = 'P_'.$hostEntityTable;
        $serviceTable = 'P_'.$serviceTable;
    }

    my $query = 'SELECT service_id AS host_samba
                 FROM '.$serviceTable.'
                 INNER JOIN '.$hostEntityTable.' ON hostentity_host_id='.$entityId.'
                 WHERE service_entity_id=hostentity_entity_id AND service_service=\'samba\'
                 LIMIT 1';

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription().' depuis la BD impossible', 1 );
        return 1;
    }

    my $links = $queryResult->fetchrow_hashref();
    $queryResult->finish();

    $self->{'currentEntity'}->setLinks( $links );

    return 0;
}
