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

    #Definition de la Description du groupe Host 515
    $self->{'sambaHostGroup'} = $self->_getVirtualHost();

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

    if ( (defined($self->{'sambaHostGroup'}) ) && ( $self->{'parentDomain'}->isSambaDomain() ) ) { 
        require OBM::Entities::obmGroup;
        my $current = OBM::Entities::obmGroup->new( $self->{'parentDomain'}, $self->{'sambaHostGroup'} );
        $current->setUpdateEntity();
        $current->setUpdateLinks();
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
                        current.group_name as group_name_current
                 FROM '.$groupTablePrefix.'UGroup
                 LEFT JOIN P_UGroup current ON current.group_id='.$groupTablePrefix.'UGroup.group_id
                 WHERE '.$groupTablePrefix.'UGroup.group_domain_id='.$self->{'domainId'};

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

    return 0;
}

sub _getVirtualHost {
	
	return {
		'group_contacts' => undef,
		'group_ext_id' => undef,
		'group_samba' => '1',
        'group_desc' => 'Host group',
        'group_system' => '0',
        'group_delegation' => '',
        'group_userupdate' => undef,
        'group_email' => '',
        'group_mailing' => '0',
        'group_name' => 'hosts',
        'group_timecreate' => '',
        'group_timeupdate' => '',
        'group_manager_id' => undef,
        'group_archive' => '0',
        'group_privacy' => '0',
        'group_usercreate' => '1',
        'group_id' => '0',
        'group_local' => '1',
        'group_gid' => '515',
        'group_name_current' => 'hosts',
        'group_domain_id' => '2' };
	 
}
