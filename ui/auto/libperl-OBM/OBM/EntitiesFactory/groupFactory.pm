package OBM::EntitiesFactory::groupFactory;

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
    $self->{'entitiesDescList'} = undef;

    # Update linked entities on
    $self->{'updateLinkedEntityOn'} = 'UPDATE_ALL|UPDATE_ENTITY|UPDATE_LINKS';

    # Definition de la Description du groupe Host 515
    $self->{'sambaHostGroup'} = $self->_getVirtualGroups();

    return $self;
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
        $self->_log( 'obtention des groupes enfants', 2 );
        $groupDesc->{'child_group_ids'} = $self->_getChildGroupIds( [$groupDesc->{'group_id'}] );

        $self->_log( 'obtention des contacts externes des groupes enfants', 2 );
        if( my $childExternalContacts = $self->_getChildContacts( $groupDesc->{'child_group_ids'} ) ) {
            $groupDesc->{'group_contacts'} .= "\r\n" if $groupDesc->{'group_contacts'};
            $groupDesc->{'group_contacts'} .= $childExternalContacts;

            $groupDesc->{'group_contacts_current'} .= "\r\n" if $groupDesc->{'group_contacts_current'};
            $groupDesc->{'group_contacts_current'} .= $childExternalContacts;
        }

        require OBM::Entities::obmGroup;
        if( !(my $current = OBM::Entities::obmGroup->new( $self->{'parentDomain'}, $groupDesc )) ) {
            next;
        }else {
            $self->{'currentEntity'} = $current;

            SWITCH: {
                if( $self->{'updateType'} eq 'UPDATE_ALL' ) {
                    if( $self->_loadGroupLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 2 );
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
                    if( $self->_loadGroupLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 2 );
                        next;
                    }

                    $self->_log( 'mise à jour des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateLinks();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'SYSTEM_ALL' ) {
                    if( $self->_loadGroupLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 2 );
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
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 2 );
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

    $self->_log( 'chargement des groupes du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 2 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return 1;
    }

    my $groupTablePrefix = '';
    if( $self->{'updateType'} !~ /^(UPDATE_ALL|UPDATE_ENTITY)$/ ) {
        $groupTablePrefix = 'P_';
    }

    my $query = 'SELECT '.$groupTablePrefix.'UGroup.*,
                        current.group_name as group_name_current,
                        current.group_contacts as group_contacts_current
                 FROM '.$groupTablePrefix.'UGroup
                 LEFT JOIN P_UGroup current ON current.group_id='.$groupTablePrefix.'UGroup.group_id
                 WHERE '.$groupTablePrefix.'UGroup.group_domain_id='.$self->{'domainId'}.'
                 AND NOT group_privacy';

    if( $self->{'ids'} ) {
        $query .= ' AND '.$groupTablePrefix.'UGroup.group_id IN ('.join( ', ', @{$self->{'ids'}}).')';
    }

    $query .= ' ORDER BY '.$groupTablePrefix.'UGroup.group_name';


    if( !defined($dbHandler->execQuery( $query, \$self->{'entitiesDescList'} )) ) {
        $self->_log( 'chargement des groupes depuis la BD impossible', 3 );
        return 1;
    }

    return 0;
}


sub _loadGroupLinks {
    my $self = shift;

    $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription(), 2 );;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return 1;
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
        $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription().' depuis la BD impossible', 3 );
        return 1;
    }

    $self->{'currentEntity'}->setLinks( $groupLinks->fetchall_arrayref({}) );


    # Needed to know all removed members
    # Needed to update members on group 512
    $query = 'SELECT DISTINCT(of_usergroup_user_id)
              FROM P_of_usergroup
              WHERE of_usergroup_group_id='.$entityId.'
              AND of_usergroup_user_id NOT IN
                (SELECT DISTINCT(of_usergroup_user_id)
                 FROM '.$groupLinksTable.'
                 WHERE of_usergroup_group_id='.$entityId.')';

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'obtention des IDs des membres supprimés impossible', 0 );
        return undef;
    }

    my @removedMemberIds;
    while( my $removeMemberId = $queryResult->fetchrow_hashref() ) {
        push( @removedMemberIds, $removeMemberId->{'of_usergroup_user_id'} );
    }

    $self->{'currentEntity'}->setRemovedMembers( \@removedMemberIds );

    return 0;
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
		'group_contacts' => undef,
        'group_contacts_current' => undef,
        'group_domain_id' => '2' };
	 
}


sub _getChildGroupIds {
    my $self = shift;
    my( $groupIds ) = @_;

    if( ref($groupIds) ne 'ARRAY' ) {
        $self->_log( 'liste d\'identifiant pères incorrecte', 3 );
        return undef;
    }

    if( $#$groupIds < 0 ) {
        return undef;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return undef;
    }

    my $query = 'SELECT DISTINCT(groupgroup_child_id)
                 FROM GroupGroup
                 WHERE groupgroup_parent_id IN ('.join( ',', @{$groupIds} ).')';

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'obtention des IDs des groupes fils impossible', 0 );
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


sub _getChildContacts {
    my $self = shift;
    my( $groupChildIds ) = @_;

    if( ref($groupChildIds) ne 'ARRAY' ) {
        $self->_log( 'liste d\'identifiant pères incorrecte', 3 );
        return undef;
    }

    if( $#$groupChildIds < 0 ) {
        return undef;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return undef;
    }

    my $query = 'SELECT group_contacts
                 FROM P_UGroup
                 WHERE group_id IN ('.join( ',', @{$groupChildIds} ).')
                 ORDER BY group_id';

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'obtention des contacts externes des groupes fils impossible', 0 );
        return undef;
    }

    my $childExternalContacts;
    while( my $groupChild = $queryResult->fetchrow_hashref() ) {
        if( $groupChild->{'group_contacts'} ) {
            $childExternalContacts .= "\r\n" if $childExternalContacts;
            $childExternalContacts .= $groupChild->{'group_contacts'};
        }
    }

    return $childExternalContacts;
}


sub _loadLinkedEntitiesFactories {
    my $self = shift;
    my @factories;

    $self->_log( 'programmation de la mise à jour des entités liées à '.$self->{'currentEntity'}->getDescription(), 2 );

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
        $self->_log( 'connexion à la base de données impossible', 4 );
        return undef;
    }

    my $groupIds = $self->_getParentGroupIds( [$entityId] );

    # Getting group factory programming
    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return undef;
    }
    if( $programmingObj->setEntitiesType( 'GROUP' ) || $programmingObj->setUpdateType( 'SYSTEM_ENTITY' ) || $programmingObj->setEntitiesIds( $groupIds ) ) {
        $self->_log( 'problème lors de l\'initialisation du programmateur de factory', 4 );
        return undef;
    }

    return $programmingObj;
}


sub _getParentGroupIds {
    my $self = shift;
    my( $groupIds ) = @_;

    if( ref($groupIds) ne 'ARRAY' ) {
        $self->_log( 'liste d\'identifiant enfants incorrecte', 3 );
        return undef;
    }

    if( $#$groupIds < 0 ) {
        return undef;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return undef;
    }

    my $query = 'SELECT DISTINCT(groupgroup_parent_id)
                 FROM GroupGroup
                 WHERE groupgroup_child_id IN ('.join( ',', @{$groupIds} ).')';

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'obtention des IDs des groupes pères impossible', 0 );
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
        $self->_log( 'connexion à la base de données impossible', 4 );
        return undef;
    }

    my $query = 'SELECT DISTINCT(of_usergroup_user_id)
                 FROM P_of_usergroup
                 WHERE of_usergroup_group_id='.$entityId;
    
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'obtention des IDs des membres impossible', 0 );
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
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return undef;
    }
    if( $programmingObj->setEntitiesType( 'USER' ) || $programmingObj->setUpdateType( 'SYSTEM_ENTITY' ) || $programmingObj->setEntitiesIds( \@memberIds ) ) {
        $self->_log( 'problème lors de l\'initialisation du programmateur de factory', 4 );
        return undef;
    }

    return $programmingObj;
}
