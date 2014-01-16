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


package OBM::EntitiesFactory::userFactory;

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

    if( my $linkedEntities = $self->_getLinkedEntities() ) {
        return $linkedEntities;
    }

    while( defined($self->{'entitiesDescList'}) && (my $userDesc = $self->{'entitiesDescList'}->fetchrow_hashref()) ) {
        require OBM::Entities::obmUser;
        if( !($self->{'currentEntity'} = OBM::Entities::obmUser->new( $self->{'parentDomain'}, $userDesc )) ) {
            next;

        }else {
            if( !$self->_loadCurrentEntityCategories() ) {
                $self->_log( 'problème au chargement des informations de catégories de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                next;
            }
            if( !$self->_loadCurrentEntityFields() ) {
                $self->_log( 'problème au chargement des informations des champs spécifiques de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                next;
            }
            if( !$self->_loadCurrentEntityServiceProperty() ) {
                $self->_log( 'problème au chargement des informations de catégories de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                next;
            }
            if( !$self->_loadCurrentEntityGroups() ) {
                $self->_log( 'problème au chargement des informations des groupes de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                next;
            }

            SWITCH: {
                if( $self->{'updateType'} eq 'UPDATE_ALL' ) {
                    if( $self->_loadUserLinks() ) {
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
                    if( $self->_loadUserLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                        next;
                    }

                    $self->_log( 'mise à jour des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateLinks();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'SYSTEM_ALL' ) {
                    if( $self->_loadUserLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                        next;
                    }

                    $self->_log( 'mise à jour de l\'entité et des liens, '.$self->{'currentEntity'}->getDescription(), 3 );

                    $self->{'currentEntity'}->setUpdateEntity();
                    $self->{'currentEntity'}->setUpdateLinks();
                    $self->{'currentEntity'}->unsetBdUpdate();
                }
                
                if( $self->{'updateType'} eq 'SYSTEM_ENTITY' ) {
                    $self->_log( 'chargement de l\'entité, '.$self->{'currentEntity'}->getDescription(), 3 );

                    $self->{'currentEntity'}->setUpdateEntity();
                    $self->{'currentEntity'}->unsetBdUpdate();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'SYSTEM_LINKS' ) {
                    if( $self->_loadUserLinks() ) {
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

    $self->_log( 'chargement des utilisateur du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 4 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return 1;
    }

    my $userTablePrefix = '';
    my $hostTablePrefix = '';
    if( $self->{'updateType'} !~ /^(UPDATE_ALL|UPDATE_ENTITY)$/ ) {
        $userTablePrefix = 'P_';
        $hostTablePrefix = 'P_';
    }

    my $query = 'SELECT '.$userTablePrefix.'UserObm.*,
                        current.userobm_login as userobm_login_current,
                        current.userobm_archive as user_obm_archive_current,
                        current.userobm_samba_perms as userobm_samba_perms_current,
                        current.userobm_mail_perms as userobm_mail_perms_current,
                        '.$hostTablePrefix.'Host.host_ip as userobm_mail_server_ip,
                        '.$hostTablePrefix.'Host.host_fqdn as userobm_mail_server_fqdn,
                        group_gid
                 FROM '.$userTablePrefix.'UserObm
                 LEFT JOIN P_UserObm current ON current.userobm_id='.$userTablePrefix.'UserObm.userobm_id
                 LEFT JOIN (SELECT group_gid, of_usergroup_user_id
                            FROM UGroup
                            INNER JOIN of_usergroup ON of_usergroup_group_id = group_id WHERE group_gid = 512) AS grp
                                ON grp.of_usergroup_user_id = '.$userTablePrefix.'UserObm.userobm_id
                 LEFT JOIN '.$hostTablePrefix.'Host ON '.$hostTablePrefix.'Host.host_id = '.$userTablePrefix.'UserObm.userobm_mail_server_id
                 WHERE '.$userTablePrefix.'UserObm.userobm_domain_id='.$self->{'domainId'}.'
                 AND '.$userTablePrefix.'UserObm.userobm_status=\'VALID\'';

    if( $self->{'ids'} ) {
        $query .= ' AND '.$userTablePrefix.'UserObm.userobm_id IN ('.join( ', ', @{$self->{'ids'}}).')';
    }

    $query .= ' ORDER BY '.$userTablePrefix.'UserObm.userobm_login';

    if( !defined($dbHandler->execQuery( $query, \$self->{'entitiesDescList'} )) ) {
        $self->_log( 'chargement des utilisateurs depuis la BD impossible', 1 );
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
                 INNER JOIN '.$tablePrefix.'UserEntity
                    ON '.$tablePrefix.'UserEntity.userentity_entity_id = '.$tablePrefix.'CategoryLink.categorylink_entity_id
                 WHERE '.$tablePrefix.'UserEntity.userentity_user_id = '.$self->{'currentEntity'}->getId();

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
                 INNER JOIN '.$tablePrefix.'UserEntity
                    ON '.$tablePrefix.'UserEntity.userentity_entity_id = '.$tablePrefix.'field.entity_id
                 WHERE '.$tablePrefix.'UserEntity.userentity_user_id = '.$self->{'currentEntity'}->getId();

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
                 INNER JOIN '.$tablePrefix.'UserEntity ON userentity_entity_id=serviceproperty_entity_id
                 WHERE userentity_user_id='.$self->{'currentEntity'}->getId();

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 0;
    }

    my %entityServiceProperties;
    while( my($servicePropertyPrefix, $servicePropertyName, $servicePropertyValue) = $queryResult->fetchrow_array() ) {
        $entityServiceProperties{$servicePropertyPrefix.'_'.$servicePropertyName}->{$servicePropertyValue} = 1;
    }

    $query = 'SELECT serviceproperty_service,
                        serviceproperty_property,
                        serviceproperty_value
                 FROM '.$tablePrefix.'ServiceProperty
                 INNER JOIN '.$tablePrefix.'MailboxEntity ON mailboxentity_entity_id=serviceproperty_entity_id
                 WHERE mailboxentity_mailbox_id='.$self->{'currentEntity'}->getId();

    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 0;
    }

    while( my($servicePropertyPrefix, $servicePropertyName, $servicePropertyValue) = $queryResult->fetchrow_array() ) {
        $entityServiceProperties{$servicePropertyPrefix.'_'.$servicePropertyName}->{$servicePropertyValue} = 1;
    }

    while( my($key, $values) = each(%entityServiceProperties) ) {
        my @keys = keys(%{$values});
        $entityServiceProperties{$key} = \@keys;
    }

    return $self->{'currentEntity'}->setExtraDescription(\%entityServiceProperties);
}


sub _loadUserLinks {
    my $self = shift;
    my %rightDef;

    $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription(), 3 );

    my $entityId = $self->{'currentEntity'}->getId();

    my $userObmTable = 'UserObm';
    if( $self->{'updateType'} !~ /^(UPDATE_ALL|UPDATE_ENTITY)$/ ) {
        $userObmTable = 'P_'.$userObmTable;
    }

    my $userEntityTable = 'UserEntity';
    my $mailboxEntity = 'MailboxEntity';
    my $groupEntityTable = 'GroupEntity';
    my $entityRightTable = 'EntityRight';
    my $ofUserGroupTable = 'of_usergroup';
    if( $self->{'updateType'} =~ /^(UPDATE_ENTITY|SYSTEM_ALL|SYSTEM_ENTITY|SYSTEM_LINKS)$/ ) {
        $userEntityTable = 'P_'.$userEntityTable;
        $mailboxEntity = 'P_'.$mailboxEntity;
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
                INNER JOIN '.$mailboxEntity.' ON mailboxentity_entity_id = entityright_entity_id
                INNER JOIN '.$userObmTable.' ON userobm_id = userentity_user_id
                WHERE mailboxentity_mailbox_id = '.$entityId.' AND entityright_write=0 AND entityright_read=1 AND userobm_status=\'VALID\' AND userobm_archive=0 AND userobm_mail_perms=1
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$groupEntityTable.' ON groupentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailboxEntity.' ON mailboxentity_entity_id = entityright_entity_id
                INNER JOIN '.$ofUserGroupTable.' ON of_usergroup_group_id = groupentity_group_id
                INNER JOIN '.$userObmTable.' ON userobm_id = of_usergroup_user_id
                WHERE mailboxentity_mailbox_id = '.$entityId.' AND entityright_write=0 AND entityright_read=1 AND userobm_status=\'VALID\' AND userobm_archive=0 AND userobm_mail_perms=1
                ORDER BY userobm_login';

    $rightDef{'writeonly'}->{'compute'} = 1;
    $rightDef{'writeonly'}->{'sqlQuery'} = 'SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$userEntityTable.' ON userentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailboxEntity.' ON mailboxentity_entity_id = entityright_entity_id
                INNER JOIN '.$userObmTable.' ON userobm_id = userentity_user_id
                WHERE mailboxentity_mailbox_id = '.$entityId.' AND entityright_write=1 AND entityright_read=0 AND userobm_status=\'VALID\' AND userobm_archive=0 AND userobm_mail_perms=1
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$groupEntityTable.' ON groupentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailboxEntity.' ON mailboxentity_entity_id = entityright_entity_id
                INNER JOIN '.$ofUserGroupTable.' ON of_usergroup_group_id = groupentity_group_id
                INNER JOIN '.$userObmTable.' ON userobm_id = of_usergroup_user_id
                WHERE mailboxentity_mailbox_id = '.$entityId.' AND entityright_write=1 AND entityright_read=0 AND userobm_status=\'VALID\' AND userobm_archive=0 AND userobm_mail_perms=1
                ORDER BY userobm_login';

    $rightDef{'write'}->{'compute'} = 1;
    $rightDef{'write'}->{'sqlQuery'} = 'SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$userEntityTable.' ON userentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailboxEntity.' ON mailboxentity_entity_id = entityright_entity_id
                INNER JOIN '.$userObmTable.' ON userobm_id = userentity_user_id
                WHERE mailboxentity_mailbox_id = '.$entityId.' AND entityright_write=1 AND entityright_read=1 AND userobm_status=\'VALID\' AND userobm_archive=0 AND userobm_mail_perms=1
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$userObmTable.'
                WHERE userobm_id = '.$entityId.'
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$groupEntityTable.' ON groupentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailboxEntity.' ON mailboxentity_entity_id = entityright_entity_id
                INNER JOIN '.$ofUserGroupTable.' ON of_usergroup_group_id = groupentity_group_id
                INNER JOIN '.$userObmTable.' ON userobm_id = of_usergroup_user_id
                WHERE mailboxentity_mailbox_id = '.$entityId.' AND entityright_write=1 AND entityright_read=1 AND userobm_status=\'VALID\' AND userobm_archive=0 AND userobm_mail_perms=1
                ORDER BY userobm_login';

    $rightDef{'admin'}->{'compute'} = 1;
    $rightDef{'admin'}->{'sqlQuery'} = 'SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$userEntityTable.' ON userentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailboxEntity.' ON mailboxentity_entity_id = entityright_entity_id
                INNER JOIN '.$userObmTable.' ON userobm_id = userentity_user_id
                WHERE mailboxentity_mailbox_id = '.$entityId.' AND entityright_admin=1 AND userobm_status=\'VALID\' AND userobm_archive=0 AND userobm_mail_perms=1
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$userObmTable.'
                WHERE userobm_id = '.$entityId.'
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$groupEntityTable.' ON groupentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailboxEntity.' ON mailboxentity_entity_id = entityright_entity_id
                INNER JOIN '.$ofUserGroupTable.' ON of_usergroup_group_id = groupentity_group_id
                INNER JOIN '.$userObmTable.' ON userobm_id = of_usergroup_user_id
                WHERE mailboxentity_mailbox_id = '.$entityId.' AND entityright_admin=1 AND userobm_status=\'VALID\' AND userobm_archive=0 AND userobm_mail_perms=1
                ORDER BY userobm_login';

    $rightDef{'public'}->{'compute'} = 0;
    $rightDef{'public'}->{'sqlQuery'} = 'SELECT
                  entityright_read,
                  entityright_write
                FROM '.$entityRightTable.'
                INNER JOIN '.$mailboxEntity.' ON mailboxentity_entity_id = entityright_entity_id
                WHERE mailboxentity_mailbox_id = '.$entityId.' AND entityright_consumer_id IS NULL';

    $self->{'currentEntity'}->setLinks( $self->_getEntityRight( \%rightDef ) );

    return 0;
}


sub _loadLinkedEntitiesFactories {
    my $self = shift;
    my @factories;

    $self->_log( 'programmation de la mise à jour des entités liées à '.$self->{'currentEntity'}->getDescription(), 3 );

    if( my $factoryProgramming = $self->_loadLinkedUsers() ) {
        $self->_enqueueLinkedEntitiesFactory( $factoryProgramming );
    }

    if( my $factoryProgramming = $self->_loadLinkedGroups() ) {
        $self->_enqueueLinkedEntitiesFactory( $factoryProgramming );
    }

    if( my $factoryProgramming = $self->_loadLinkedMailshares() ) {
        $self->_enqueueLinkedEntitiesFactory( $factoryProgramming );
    }

    return 0;
}


sub _loadLinkedGroups {
    my $self = shift;
    my $entityId = $self->{'currentEntity'}->getId();

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return undef;
    }

    my $query = 'SELECT of_usergroup_group_id
                 FROM P_of_usergroup
                 WHERE of_usergroup_user_id='.$entityId;

    my $linkedGroupsIds;
    if( !defined($dbHandler->execQuery( $query, \$linkedGroupsIds )) ) {
        $self->_log( 'chargement des groupes liés depuis la BD impossible', 1 );
        return undef;
    }

    my %groupIds;
    while (my $groupId = $linkedGroupsIds->fetchrow_hashref()) {
        $groupIds{$groupId->{'of_usergroup_group_id'}} = undef;
    }

    my @groupIds = keys(%groupIds);
    if( $#groupIds < 0 ) {
        $self->_log( 'pas de groupes liés à '.$self->{'currentEntity'}->getDescription(), 3 );
        return undef;
    }

    # Getting group factory programming
    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 1 );
        return undef;
    }
    if( $programmingObj->setEntitiesType( 'GROUP' ) || $programmingObj->setUpdateType( 'SYSTEM_LINKS' ) || $programmingObj->setEntitiesIds( \@groupIds ) ) {
        $self->_log( 'problème lors de l\'initialisation du programmateur de factory', 1 );
        return undef;
    }

    return $programmingObj;
}


sub _loadLinkedUsers {
    my $self = shift;
    my $entityId = $self->{'currentEntity'}->getId();

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return undef;
    }

    my $query = 'SELECT mailboxentity_mailbox_id AS id
                 FROM P_MailboxEntity
                 INNER JOIN P_EntityRight ON mailboxentity_entity_id = entityright_entity_id
                 INNER JOIN P_UserEntity ON userentity_entity_id = entityright_consumer_id
                 WHERE userentity_user_id = \''.$entityId.'\'
                  AND mailboxentity_mailbox_id <> \''.$entityId.'\'
                  AND (entityright_read = \'1\' OR entityright_write = \'1\')
                 UNION
                 SELECT mailboxentity_mailbox_id AS id
                 FROM P_MailboxEntity
                 INNER JOIN P_EntityRight ON mailboxentity_entity_id = entityright_entity_id
                 INNER JOIN P_GroupEntity ON groupentity_entity_id = entityright_consumer_id
                 WHERE groupentity_group_id IN (SELECT of_usergroup_group_id
                                                FROM P_of_usergroup
                                                WHERE of_usergroup_user_id=\''.$entityId.'\')
                  AND mailboxentity_mailbox_id <> \''.$entityId.'\'
                  AND (entityright_read = \'1\' OR entityright_write = \'1\')';

    my $linkedUsersIds;
    if( !defined($dbHandler->execQuery( $query, \$linkedUsersIds )) ) {
        $self->_log( 'chargement des utilisateurs liés depuis la BD impossible', 3 );
        return undef;
    }

    my %userIds;
    while (my $userId = $linkedUsersIds->fetchrow_hashref()) {
        $userIds{$userId->{'id'}} = undef;
    }

    my @userIds = keys(%userIds);
    if( $#userIds < 0 ) {
        $self->_log( 'pas d\'utilisateurs liés à '.$self->{'currentEntity'}->getDescription(), 3 );
        return undef;
    }

    # Getting user factory programming
    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 1 );
        return undef;
    }
    if( $programmingObj->setEntitiesType( 'USER' ) || $programmingObj->setUpdateType( 'SYSTEM_LINKS' ) || $programmingObj->setEntitiesIds( \@userIds ) ) {
        $self->_log( 'problème lors de l\'initialisation du programmateur de factory', 1 );
        return undef;
    }

    return $programmingObj;
}


sub _loadLinkedMailshares {
    my $self = shift;
    my $entityId = $self->{'currentEntity'}->getId();

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return undef;
    }

    my $query = 'SELECT mailshareentity_mailshare_id AS id
                 FROM P_MailshareEntity
                 INNER JOIN P_EntityRight ON mailshareentity_entity_id = entityright_entity_id
                 INNER JOIN P_UserEntity ON userentity_entity_id = entityright_consumer_id
                 WHERE userentity_user_id = \''.$entityId.'\'
                  AND mailshareentity_mailshare_id <> \''.$entityId.'\'
                  AND (entityright_read = \'1\' OR entityright_write = \'1\')
                 UNION
                 SELECT mailshareentity_mailshare_id AS id
                 FROM P_MailshareEntity
                 INNER JOIN P_EntityRight ON mailshareentity_entity_id = entityright_entity_id
                 INNER JOIN P_GroupEntity ON groupentity_entity_id = entityright_consumer_id
                 WHERE groupentity_group_id IN (SELECT of_usergroup_group_id
                                                FROM P_of_usergroup
                                                WHERE of_usergroup_user_id=\''.$entityId.'\')
                  AND mailshareentity_mailshare_id <> \''.$entityId.'\'
                  AND (entityright_read = \'1\' OR entityright_write = \'1\')';

    my $linkedMailsharesIds;
    if( !defined($dbHandler->execQuery( $query, \$linkedMailsharesIds )) ) {
        $self->_log( 'chargement des mailshare liés depuis la BD impossible', 1 );
        return undef;
    }

    my %mailshareIds;
    while (my $mailshareId = $linkedMailsharesIds->fetchrow_hashref()) {
        $mailshareIds{$mailshareId->{'id'}} = undef;
    }

    my @mailshareIds = keys(%mailshareIds);
    if( $#mailshareIds < 0 ) {
        $self->_log( 'pas de mailshare liés à '.$self->{'currentEntity'}->getDescription(), 3 );
        return undef;
    }

    # Getting mailshare factory programming
    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 1 );
        return undef;
    }
    if( $programmingObj->setEntitiesType( 'MAILSHARE' ) || $programmingObj->setUpdateType( 'SYSTEM_LINKS' ) || $programmingObj->setEntitiesIds( \@mailshareIds ) ) {
        $self->_log( 'problème lors de l\'initialisation du programmateur de factory', 1 );
        return undef;
    }

    return $programmingObj;
}


sub _loadCurrentEntityGroups {
    my $self = shift;

    $self->_log( 'chargement des informations des groupes de l\'entité '.$self->{'currentEntity'}->getDescription(), 4 );

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

    my $query = 'SELECT group_name
                 FROM '.$tablePrefix.'UGroup
                 INNER JOIN '.$tablePrefix.'of_usergroup
                    ON of_usergroup_group_id = group_id
                 WHERE of_usergroup_user_id = '.$self->{'currentEntity'}->getId().' AND group_privacy = 0';

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 0;
    }

    my %entityGroups;
    while( my($groupName) = $queryResult->fetchrow_array() ) {
        $entityGroups{$groupName} = 1;
    }

    my %entityGroupList;
    my @groupList = keys(%entityGroups);
    $entityGroupList{'userobm_group_list'} = \@groupList;

    return $self->{'currentEntity'}->setExtraDescription(\%entityGroupList);
}
