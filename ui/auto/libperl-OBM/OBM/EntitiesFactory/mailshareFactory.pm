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


package OBM::EntitiesFactory::mailshareFactory;

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

    while( defined($self->{'entitiesDescList'}) && (my $mailshareDesc = $self->{'entitiesDescList'}->fetchrow_hashref()) ) {
        require OBM::Entities::obmMailshare;
        if( !($self->{'currentEntity'} = OBM::Entities::obmMailshare->new( $self->{'parentDomain'}, $mailshareDesc )) ) {
            next;
        }else {
            if( !$self->_loadCurrentEntityCategories() ) {
                $self->_log( 'problème au chargement des informations de catégrories de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
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
                    if( $self->_loadMailshareLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                        next;
                    }

                    $self->_log( 'mise à jour de l\'entité et des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
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
                    if( $self->_loadMailshareLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                        next;
                    }

                    $self->_log( 'mise à jour des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateLinks();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'SYSTEM_ALL' ) {
                    if( $self->_loadMailshareLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                        next;
                    }

                    $self->_log( 'mise à jour de l\'entité et des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateEntity();
                    $self->{'currentEntity'}->setUpdateLinks();
                    $self->{'currentEntity'}->unsetBdUpdate();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'SYSTEM_ENTITY' ) {
                    $self->_log( 'mise à jour de l\'entité, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateEntity();
                    $self->{'currentEntity'}->unsetBdUpdate();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'SYSTEM_LINKS' ) {
                    if( $self->_loadMailshareLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                        next;
                    }

                    $self->_log( 'mise à jour des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateLinks();
                    $self->{'currentEntity'}->unsetBdUpdate();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'DELETE' ) {
                    $self->_log( 'suppression de l\'entité, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setDelete();
                    last SWITCH;
                }

                $self->_log( 'type de mise à jour inconnu \''.$self->{'updateType'}.'\'', 1 );
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

    $self->_log( 'chargement des mailshare du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 4 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return 1;
    }

    my $mailshareTablePrefix = '';
    my $hostTablePrefix = '';
    if( $self->{'updateType'} !~ /^(UPDATE_ALL|UPDATE_ENTITY)$/ ) {
        $mailshareTablePrefix = 'P_';
        $hostTablePrefix = 'P_';
    }

    my $query = 'SELECT '.$mailshareTablePrefix.'MailShare.*,
                        '.$hostTablePrefix.'Host.host_ip as mailshare_mail_server_ip,
                        '.$hostTablePrefix.'Host.host_fqdn as mailshare_mail_server_fqdn,
                        current.mailshare_name as mailshare_name_current
                 FROM '.$mailshareTablePrefix.'MailShare
                 LEFT JOIN P_MailShare current ON current.mailshare_id='.$mailshareTablePrefix.'MailShare.mailshare_id
                 LEFT JOIN '.$hostTablePrefix.'Host ON '.$hostTablePrefix.'Host.host_id = '.$mailshareTablePrefix.'MailShare.mailshare_mail_server_id
                 WHERE '.$mailshareTablePrefix.'MailShare.mailshare_domain_id='.$self->{'domainId'};

    if( $self->{'ids'} ) {
        $query .= ' AND '.$mailshareTablePrefix.'MailShare.mailshare_id IN ('.join( ', ', @{$self->{'ids'}}).')';
    }

    $query .= ' ORDER BY '.$mailshareTablePrefix.'MailShare.mailshare_name';

    if( !defined($dbHandler->execQuery( $query, \$self->{'entitiesDescList'} )) ) {
        $self->_log( 'chargement des mailshare depuis la BD impossible', 1 );
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
                 INNER JOIN '.$tablePrefix.'MailshareEntity
                    ON '.$tablePrefix.'MailshareEntity.mailshareentity_entity_id = '.$tablePrefix.'CategoryLink.categorylink_entity_id
                 WHERE '.$tablePrefix.'MailshareEntity.mailshareentity_mailshare_id = '.$self->{'currentEntity'}->getId();

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
                 INNER JOIN '.$tablePrefix.'MailshareEntity
                    ON '.$tablePrefix.'MailshareEntity.mailshareentity_entity_id = '.$tablePrefix.'field.entity_id
                 WHERE '.$tablePrefix.'MailshareEntity.mailshareentity_mailshare_id = '.$self->{'currentEntity'}->getId();

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
                 INNER JOIN '.$tablePrefix.'MailshareEntity ON mailshareentity_entity_id=serviceproperty_entity_id
                 WHERE mailshareentity_mailshare_id='.$self->{'currentEntity'}->getId();

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


sub _loadMailshareLinks {
    my $self = shift;
    my %rightDef;

    $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription(), 3 );

    my $entityId = $self->{'currentEntity'}->getId();

    my $userObmTable = 'UserObm';
    my $userEntityTable = 'UserEntity';
    my $mailshareEntity = 'MailshareEntity';
    my $groupEntityTable = 'GroupEntity';
    my $entityRightTable = 'EntityRight';
    my $ofUserGroupTable = 'of_usergroup';
    if( $self->{'updateType'} =~ /^(SYSTEM_ALL|SYSTEM_ENTITY|SYSTEM_LINKS)$/ ) {
        $userObmTable = 'P_'.$userObmTable;
        $userEntityTable = 'P_'.$userEntityTable;
        $mailshareEntity = 'P_'.$mailshareEntity;
        $groupEntityTable = 'P_'.$groupEntityTable;
        $entityRightTable = 'P_'.$entityRightTable;
        $ofUserGroupTable = 'P_'.$ofUserGroupTable;
    }
    
    $rightDef{'read'}->{'compute'} = 1;
    $rightDef{'read'}->{'sqlQuery'} = 'SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$userEntityTable.' ON userentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailshareEntity.' ON mailshareentity_entity_id = entityright_entity_id
                INNER JOIN '.$userObmTable.' ON userobm_id = userentity_user_id
                WHERE mailshareentity_mailshare_id = '.$entityId.' AND entityright_write=0 AND entityright_read=1 AND userobm_status=\'VALID\' AND userobm_archive=0 AND userobm_mail_perms=1
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$groupEntityTable.' ON groupentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailshareEntity.' ON mailshareentity_entity_id = entityright_entity_id
                INNER JOIN '.$ofUserGroupTable.' ON of_usergroup_group_id = groupentity_group_id
                INNER JOIN '.$userObmTable.' ON userobm_id = of_usergroup_user_id
                WHERE mailshareentity_mailshare_id = '.$entityId.' AND entityright_write=0 AND entityright_read=1 AND userobm_status=\'VALID\' AND userobm_archive=0 AND userobm_mail_perms=1
                ORDER BY userobm_login';

    $rightDef{'writeonly'}->{'compute'} = 1;
    $rightDef{'writeonly'}->{'sqlQuery'} = 'SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$userEntityTable.' ON userentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailshareEntity.' ON mailshareentity_entity_id = entityright_entity_id
                INNER JOIN '.$userObmTable.' ON userobm_id = userentity_user_id
                WHERE mailshareentity_mailshare_id = '.$entityId.' AND entityright_write=1 AND entityright_read=0 AND userobm_status=\'VALID\' AND userobm_archive=0 AND userobm_mail_perms=1
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$groupEntityTable.' ON groupentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailshareEntity.' ON mailshareentity_entity_id = entityright_entity_id
                INNER JOIN '.$ofUserGroupTable.' ON of_usergroup_group_id = groupentity_group_id
                INNER JOIN '.$userObmTable.' ON userobm_id = of_usergroup_user_id
                WHERE mailshareentity_mailshare_id = '.$entityId.' AND entityright_write=1 AND entityright_read=0 AND userobm_status=\'VALID\' AND userobm_archive=0 AND userobm_mail_perms=1
                ORDER BY userobm_login';

    $rightDef{'write'}->{'compute'} = 1;
    $rightDef{'write'}->{'sqlQuery'} = 'SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$userEntityTable.' ON userentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailshareEntity.' ON mailshareentity_entity_id = entityright_entity_id
                INNER JOIN '.$userObmTable.' ON userobm_id = userentity_user_id
                WHERE mailshareentity_mailshare_id = '.$entityId.' AND entityright_write=1 AND entityright_read=1 AND userobm_status=\'VALID\' AND userobm_archive=0 AND userobm_mail_perms=1
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$groupEntityTable.' ON groupentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailshareEntity.' ON mailshareentity_entity_id = entityright_entity_id
                INNER JOIN '.$ofUserGroupTable.' ON of_usergroup_group_id = groupentity_group_id
                INNER JOIN '.$userObmTable.' ON userobm_id = of_usergroup_user_id
                WHERE mailshareentity_mailshare_id = '.$entityId.' AND entityright_write=1 AND entityright_read=1 AND userobm_status=\'VALID\' AND userobm_archive=0 AND userobm_mail_perms=1
                ORDER BY userobm_login';

    $rightDef{'admin'}->{'compute'} = 1;
    $rightDef{'admin'}->{'sqlQuery'} = 'SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$userEntityTable.' ON userentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailshareEntity.' ON mailshareentity_entity_id = entityright_entity_id
                INNER JOIN '.$userObmTable.' ON userobm_id = userentity_user_id
                WHERE mailshareentity_mailshare_id = '.$entityId.' AND entityright_admin=1 AND userobm_status=\'VALID\' AND userobm_archive=0 AND userobm_mail_perms=1
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$groupEntityTable.' ON groupentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailshareEntity.' ON mailshareentity_entity_id = entityright_entity_id
                INNER JOIN '.$ofUserGroupTable.' ON of_usergroup_group_id = groupentity_group_id
                INNER JOIN '.$userObmTable.' ON userobm_id = of_usergroup_user_id
                WHERE mailshareentity_mailshare_id = '.$entityId.' AND entityright_admin=1 AND userobm_status=\'VALID\' AND userobm_archive=0 AND userobm_mail_perms=1
                ORDER BY userobm_login';

    $rightDef{'public'}->{'compute'} = 0;
    $rightDef{'public'}->{'sqlQuery'} = 'SELECT
                  entityright_read,
                  entityright_write
                FROM '.$entityRightTable.'
                INNER JOIN '.$mailshareEntity.' ON mailshareentity_entity_id = entityright_entity_id
                WHERE mailshareentity_mailshare_id = '.$entityId.' AND entityright_consumer_id IS NULL';


    $self->{'currentEntity'}->setLinks( $self->_getEntityRight( \%rightDef ) );

    return 0;
}
