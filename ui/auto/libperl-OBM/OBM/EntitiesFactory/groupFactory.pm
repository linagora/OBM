package OBM::EntitiesFactory::groupFactory;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(
        _log
        dump
        );
use OBM::EntitiesFactory::commonFactory qw(
        _checkSource
        _getSourceByUpdateType
        _checkUpdateType
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

    $self->{'source'} = $self->_getSourceByUpdateType();
    if( !$self->_checkSource() ) {
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
    $self->{'groupDescList'} = undef;


    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );

    $self->_reset();
}


sub _reset {
    my $self = shift;

    $self->_log( 'factory reset', 3 );

    $self->{'running'} = undef;
    $self->{'currentEntity'} = undef;
    $self->{'groupDescList'} = undef;

    return 1;
}


sub isRunning {
    my $self = shift;

    if( $self->{'running'} ) {
        $self->_log( 'la factory est en cours d\'exécution', 4 );
        return 1;
    }

    $self->_log( 'la factory n\'est pas en cours d\'exécution', 4 );

    return 0;
}


sub _start {
    my $self = shift;

    $self->_log( 'debut de traitement', 2 );

    if( $self->_loadGroups() ) {
        $self->_log( 'problème lors de l\'obtention de la description des groupes du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 3 );
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

    while( defined($self->{'groupDescList'}) && (my $groupDesc = $self->{'groupDescList'}->fetchrow_hashref()) ) {
        require OBM::Entities::obmGroup;
        if( !(my $current = OBM::Entities::obmGroup->new( $self->{'parentDomain'}, $groupDesc )) ) {
            next;
        }else {
            $self->{'currentEntity'} = $current;

            SWITCH: {
                if( $self->{'source'} =~ /^SYSTEM$/ ) {
                    $self->{'currentEntity'}->unsetBdUpdate();
                    last SWITCH;
                }

                $self->{'currentEntity'}->setBdUpdate();
            }

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


sub _loadGroups {
    my $self = shift;

    $self->_log( 'chargement des groupes du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 2 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return 1;
    }

    my $groupTable = 'UGroup';
    if( $self->{'source'} =~ /^SYSTEM$/ ) {
        $groupTable = 'P_'.$groupTable;
    }

    my $query = 'SELECT '.$groupTable.'.*,
                        current.group_name as group_name_current
                 FROM '.$groupTable.'
                 LEFT JOIN P_UGroup current ON current.group_id='.$groupTable.'.group_id
                 WHERE '.$groupTable.'.group_domain_id='.$self->{'domainId'};

    if( $self->{'ids'} ) {
        $query .= ' AND '.$groupTable.'.group_id IN ('.join( ', ', @{$self->{'ids'}}).')';
    }

    if( !defined($dbHandler->execQuery( $query, \$self->{'groupDescList'} )) ) {
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
    if( $self->{'source'} =~ /^SYSTEM$/ ) {
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
                 AND '.$userTable.'.userobm_archive=0';

    my $groupLinks;
    if( !defined($dbHandler->execQuery( $query, \$groupLinks )) ) {
        $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription().' depuis la BD impossible', 3 );
        return 1;
    }

    $self->{'currentEntity'}->setLinks( $groupLinks->fetchall_arrayref({}) );

    return 0;
}
