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


package OBM::EntitiesFactory::groupFactory;

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

    # Update linked entities on
    $self->{'updateLinkedEntityOn'} = 'UPDATE_ALL|UPDATE_ENTITY|UPDATE_LINKS';

    # Definition de la Description du groupe Host 515
    $self->{'sambaHostGroup'} = $self->_getVirtualGroups();

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

    if ( (defined($self->{'sambaHostGroup'}) ) && ( $self->{'parentDomain'}->isSambaDomain() ) ) { 
        require OBM::Entities::obmGroup;
        my $current = OBM::Entities::obmGroup->new( $self->{'parentDomain'}, $self->{'sambaHostGroup'} );
        $current->unsetBdUpdate();
        $self->{'sambaHostGroup'} = undef;

        return $current;
    }

    while( defined($self->{'entitiesDescList'}) && (my $groupDesc = $self->{'entitiesDescList'}->fetchrow_hashref()) ) {
        require OBM::Entities::obmGroup;

        if( !(my $current = OBM::Entities::obmGroup->new( $self->{'parentDomain'}, $groupDesc )) ) {
            next;
        }else {
            $self->{'currentEntity'} = $current;

            SWITCH: {
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

                if( $self->{'updateType'} eq 'UPDATE_ALL' ) {
                    if( $self->_loadGroupLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                        next;
                    }

                    $self->_log( 'mise à jour de l\'entité et des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateEntity();
                    $self->{'currentEntity'}->setUpdateLinks();

                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'UPDATE_ENTITY' ) {
                    # In some case, even if scope indicate that only
                    # UPDATE_ENTITY is needed, entity links must be updated
                    if($self->{'currentEntity'}->getForceLoadEntityLinks()) {
                        if($self->_loadGroupLinks()) {
                            $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 2 );
                            next;
                        }else {
                            $self->{'currentEntity'}->setUpdateLinks();
                        }
                    }

                    $self->_log( 'mise à jour de l\'entité, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateEntity();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'UPDATE_LINKS' ) {
                    if( $self->_loadGroupLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 1 );
                        next;
                    }

                    $self->_log( 'mise à jour des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateLinks();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'SYSTEM_ALL' ) {
                    if( $self->_loadGroupLinks() ) {
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
                    $self->_log( 'chargement de l\'entité, '.$self->{'currentEntity'}->getDescription(), 3 );

                    $self->{'currentEntity'}->setUpdateEntity();
                    $self->{'currentEntity'}->unsetBdUpdate();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'SYSTEM_LINKS' ) {
                    if( $self->_loadGroupLinks() ) {
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

    $self->_log( 'chargement des groupes du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 4 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return 1;
    }

    my $groupTablePrefix = '';
    if( $self->{'updateType'} !~ /^(UPDATE_ALL|UPDATE_ENTITY)$/ ) {
        $groupTablePrefix = 'P_';
    }

    my $query = 'SELECT '.$groupTablePrefix.'UGroup.*,
                        current.group_name as group_name_current,
                        current.group_email as group_email_current,
                        '.$groupTablePrefix.'GroupEntity.groupentity_entity_id
                 FROM '.$groupTablePrefix.'UGroup
                 LEFT JOIN P_UGroup current ON current.group_id='.$groupTablePrefix.'UGroup.group_id
                 INNER JOIN '.$groupTablePrefix.'GroupEntity ON '.$groupTablePrefix.'GroupEntity.groupentity_group_id='.$groupTablePrefix.'UGroup.group_id
                 WHERE '.$groupTablePrefix.'UGroup.group_domain_id='.$self->{'domainId'}.'
                 AND '.$groupTablePrefix.'UGroup.group_privacy=0';

    if( $self->{'ids'} ) {
        $query .= ' AND '.$groupTablePrefix.'UGroup.group_id IN ('.join( ', ', @{$self->{'ids'}}).')';
    }

    $query .= ' ORDER BY '.$groupTablePrefix.'UGroup.group_name';


    if( !defined($dbHandler->execQuery( $query, \$self->{'entitiesDescList'} )) ) {
        $self->_log( 'chargement des groupes depuis la BD impossible', 1 );
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
                 INNER JOIN '.$tablePrefix.'GroupEntity
                    ON '.$tablePrefix.'GroupEntity.groupentity_entity_id = '.$tablePrefix.'CategoryLink.categorylink_entity_id
                 WHERE '.$tablePrefix.'GroupEntity.groupentity_group_id = '.$self->{'currentEntity'}->getId();

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
                 INNER JOIN '.$tablePrefix.'GroupEntity
                    ON '.$tablePrefix.'GroupEntity.groupentity_entity_id = '.$tablePrefix.'field.entity_id
                 WHERE '.$tablePrefix.'GroupEntity.groupentity_group_id = '.$self->{'currentEntity'}->getId();

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
                 INNER JOIN '.$tablePrefix.'GroupEntity ON groupentity_entity_id=serviceproperty_entity_id
                 WHERE groupentity_group_id='.$self->{'currentEntity'}->getId();

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


sub _loadGroupLinks {
    my $self = shift;

    $self->{'currentEntity'}->setLinks( {
        members => $self->_loadGroupMembers(),
        contacts => $self->_loadGroupContacts()
        } );


    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return undef;
    }

    my $groupLinksTable = 'of_usergroup';
    if( $self->{'updateType'} =~ /^(SYSTEM_ALL|SYSTEM_ENTITY|SYSTEM_LINKS)$/ ) {
        $groupLinksTable = 'P_'.$groupLinksTable;
    }

    my $entityId = $self->{'currentEntity'}->getId();

    # Needed to know all removed members
    # Needed to update members on group 512
    my $query = 'SELECT DISTINCT(of_usergroup_user_id)
              FROM P_of_usergroup
              WHERE of_usergroup_group_id='.$entityId.'
              AND of_usergroup_user_id NOT IN
                (SELECT DISTINCT(of_usergroup_user_id)
                 FROM '.$groupLinksTable.'
                 WHERE of_usergroup_group_id='.$entityId.')';

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'obtention des IDs des membres supprimés impossible', 1 );
        return undef;
    }

    my @removedMemberIds;
    while( my $removeMemberId = $queryResult->fetchrow_hashref() ) {
        push( @removedMemberIds, $removeMemberId->{'of_usergroup_user_id'} );
    }

    $self->{'currentEntity'}->setRemovedMembers( \@removedMemberIds );

    return 0;
}


sub _loadGroupMembers {
    my $self = shift;

    $self->_log( 'chargement des membres de '.$self->{'currentEntity'}->getDescription(), 3 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return undef;
    }

    my $groupLinksTable = 'of_usergroup';
    my $userTable = 'UserObm';
    if( $self->{'updateType'} =~ /^(SYSTEM_ALL|SYSTEM_ENTITY|SYSTEM_LINKS)$/ ) {
        $groupLinksTable = 'P_'.$groupLinksTable;
        $userTable = 'P_'.$userTable;
    }

    my $entityId = $self->{'currentEntity'}->getId();

    my $query = 'SELECT '.$userTable.'.userobm_login,
                        '.$userTable.'.userobm_uid,
                        '.$userTable.'.userobm_samba_perms,
                        '.$userTable.'.userobm_mail_perms
                 FROM '.$userTable.',
                      '.$groupLinksTable.'
                 WHERE '.$groupLinksTable.'.of_usergroup_group_id='.$entityId.'
                 AND '.$groupLinksTable.'.of_usergroup_user_id='.$userTable.'.userobm_id
                 AND '.$userTable.'.userobm_archive=0
                 AND userobm_status=\'VALID\'';

    my $groupLinks;
    if( !defined($dbHandler->execQuery( $query, \$groupLinks )) ) {
        $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription().' depuis la BD impossible', 1 );
        return undef;
    }

    my $result = $groupLinks->fetchall_arrayref({});

    if( $#$result < 0 ) {
        return undef;
    }

    return $result;
}


sub _loadGroupContacts {
    my $self = shift;

    $self->_log( 'chargement des contacts de '.$self->{'currentEntity'}->getDescription(), 3 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return undef;
    }

    my $contactTable = 'Contact';
    my $contactGroupTable = '_contactgroup';
    my $contactEntityTable = 'ContactEntity';
    my $emailTable = 'Email';
    if( $self->{'updateType'} =~ /^(SYSTEM_ALL|SYSTEM_ENTITY|SYSTEM_LINKS)$/ ) {
        $contactGroupTable = 'P_'.$contactGroupTable;
    }

    my $entityId = $self->{'currentEntity'}->getId();

    my $query = 'SELECT email.email_address AS contact_email_address
                 FROM '.$contactTable.' contact
                 INNER JOIN '.$contactGroupTable.' contactgroup
                    ON contactgroup.contact_id=contact.contact_id
                INNER JOIN '.$contactEntityTable.' contactentity
                    ON contactentity.contactentity_contact_id=contact.contact_id
                INNER JOIN '.$emailTable.' email ON email.email_entity_id=contactentity.contactentity_entity_id
                WHERE contactgroup.group_id='.$entityId.'
                AND email.email_label=\'INTERNET;X-OBM-Ref1\'';

    my $groupLinks;
    if( !defined($dbHandler->execQuery( $query, \$groupLinks )) ) {
        $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription().' depuis la BD impossible', 1 );
        return undef;
    }

    my $result = $groupLinks->fetchall_arrayref({});

    if( $#$result < 0 ) {
        return undef;
    }

    return $result;
}


sub _getVirtualGroups {
	
	return {
		'group_ext_id' => undef,
		'group_samba' => '1',
        'group_desc' => 'Host group',
        'group_system' => '0',
        'group_delegation' => '',
        'group_userupdate' => undef,
        'group_email' => '',
        'group_mailing' => '0',
        'group_name' => 'hosts',
        'group_name_current' => 'hosts',
        'group_timecreate' => '',
        'group_timeupdate' => '',
        'group_manager_id' => undef,
        'group_archive' => '0',
        'group_privacy' => '0',
        'group_usercreate' => '1',
        'group_id' => '0',
        'group_local' => '1',
        'group_gid' => '515',
        'group_domain_id' => '2' };
	 
}


sub _getChildGroupIds {
    my $self = shift;
    my( $groupIds ) = @_;

    if( ref($groupIds) ne 'ARRAY' ) {
        $self->_log( 'liste d\'identifiant pères incorrecte', 1 );
        return undef;
    }

    if( $#$groupIds < 0 ) {
        return undef;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return undef;
    }

    my $query = 'SELECT DISTINCT(groupgroup_child_id)
                 FROM GroupGroup
                 WHERE groupgroup_parent_id IN ('.join( ',', @{$groupIds} ).')';

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'obtention des IDs des groupes fils impossible', 1 );
        return undef;
    }

    my %childIds;
    while( my $groupChild = $queryResult->fetchrow_hashref() ) {
        $childIds{$groupChild->{'groupgroup_child_id'}} = undef;
    }

    my @childIds = keys(%childIds);
    my $childIds = $self->_getChildGroupIds( \@childIds );
    for( my $i=0; $i<=$#$childIds; $i++ ) {
        $childIds{$childIds->[$i]} = undef;
    }

    @childIds = keys(%childIds);
    return \@childIds;
}


sub _loadLinkedEntitiesFactories {
    my $self = shift;
    my @factories;

    $self->_log( 'programmation de la mise à jour des entités liées à '.$self->{'currentEntity'}->getDescription(), 3 );

    if( my $factoryProgramming = $self->_loadParentGroups() ) {
        $self->_enqueueLinkedEntitiesFactory( $factoryProgramming );
    }
    if( ($self->{'updateType'} =~ /^(UPDATE_ALL|UPDATE_LINKS)$/) && (my $factoryProgramming = $self->_loadMembers()) ) {
        $self->_enqueueLinkedEntitiesFactory( $factoryProgramming );
    }

    return 0;
}


sub _loadParentGroups {
    my $self = shift;
    my $entityId = $self->{'currentEntity'}->getId();

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return undef;
    }

    my $groupIds = $self->_getParentGroupIds( [$entityId] );

    # Getting group factory programming
    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 1 );
        return undef;
    }
    if( $programmingObj->setEntitiesType( 'GROUP' ) || $programmingObj->setUpdateType( 'SYSTEM_ENTITY' ) || $programmingObj->setEntitiesIds( $groupIds ) ) {
        $self->_log( 'problème lors de l\'initialisation du programmateur de factory', 1 );
        return undef;
    }

    return $programmingObj;
}


sub _getParentGroupIds {
    my $self = shift;
    my( $groupIds ) = @_;

    if( ref($groupIds) ne 'ARRAY' ) {
        $self->_LOG( 'liste d\'identifiant enfants incorrecte', 1 );
        return undef;
    }

    if( $#$groupIds < 0 ) {
        return undef;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return undef;
    }

    my $query = 'SELECT DISTINCT(groupgroup_parent_id)
                 FROM GroupGroup
                 WHERE groupgroup_child_id IN ('.join( ',', @{$groupIds} ).')';

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'obtention des IDs des groupes pères impossible', 1 );
        return undef;
    }

    my %parentIds;
    while( my $groupParent = $queryResult->fetchrow_hashref() ) {
        $parentIds{$groupParent->{'groupgroup_parent_id'}} = undef;
    }

    my @parentIds = keys(%parentIds);
    my $parentIds = $self->_getParentGroupIds( \@parentIds );
    for( my $i=0; $i<=$#$parentIds; $i++ ) {
        $parentIds{$parentIds->[$i]} = undef;
    }

    @parentIds = keys(%parentIds);
    return \@parentIds;
}


sub _loadMembers {
    my $self = shift;

    my $entityId = $self->{'currentEntity'}->getId();

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return undef;
    }

    my $query = 'SELECT DISTINCT(of_usergroup_user_id)
                 FROM P_of_usergroup
                 WHERE of_usergroup_group_id='.$entityId;
    
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'obtention des IDs des membres impossible', 1 );
        return undef;
    }

    my @memberIds;
    while( my $memberId = $queryResult->fetchrow_hashref() ) {
        push( @memberIds, $memberId->{'of_usergroup_user_id'} );
    }

    my $removedMembersIds = $self->{'currentEntity'}->getRemovedMembersId();
    for( my $i=0; $i<=$#$removedMembersIds; $i++ ) {
        push( @memberIds, $removedMembersIds->[$i] );
    }

    # Getting user factory programming
    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 1 );
        return undef;
    }
    if( $programmingObj->setEntitiesType( 'USER' ) || $programmingObj->setUpdateType( 'SYSTEM_ENTITY' ) || $programmingObj->setEntitiesIds( \@memberIds ) ) {
        $self->_log( 'problème lors de l\'initialisation du programmateur de factory', 1 );
        return undef;
    }

    return $programmingObj;
}
