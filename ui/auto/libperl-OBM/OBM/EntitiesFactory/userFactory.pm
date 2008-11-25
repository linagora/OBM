package OBM::EntitiesFactory::userFactory;

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
        _checkUpdateType
        _getEntityRight
        _computeRight
        );
use OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $source, $updateType, $parentDomain ) = @_;

    my $self = bless { }, $class;

    $self->{'source'} = $source;
    if( !$self->_checkSource() ) {
        return undef;
    }

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

    $self->{'running'} = undef;
    $self->{'currentEntity'} = undef;
    $self->{'userDescList'} = undef;


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
    $self->{'userDescList'} = undef;

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

    if( $self->_loadUsers() ) {
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

    while( defined($self->{'userDescList'}) && (my $userDesc = $self->{'userDescList'}->fetchrow_hashref()) ) {
        require OBM::Entities::obmUser;
        if( !(my $current = OBM::Entities::obmUser->new( $self->{'parentDomain'}, $userDesc )) ) {
            next;
        }else {
            $self->{'currentEntity'} = $current;

            SWITCH: {
                if( $self->{'updateType'} eq 'ALL' ) {
                    if( $self->_loadUserLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 2 );
                        next;
                    }

                    $self->_log( 'mise à jour de l\'entité et des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateEntity();
                    $self->{'currentEntity'}->setUpdateLinks();

                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'ENTITY' ) {
                    $self->_log( 'mise à jour de l\'entité, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateEntity();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'LINKS' ) {
                    if( $self->_loadUserLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 2 );
                        next;
                    }

                    $self->_log( 'mise à jour des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateLinks();
                    last SWITCH;
                }

                $self->_log( 'type de mise à jour inconnu \''.$self->{'currentEntity'}.'\'', 0 );
                return undef;
            }

            return $self->{'currentEntity'};
        }
    }

    return undef;
}


sub _loadUsers {
    my $self = shift;

    $self->_log( 'chargement des utilisateur du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 2 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return 1;
    }

    my $userTable = 'UserObm';
    if( $self->{'source'} =~ /^SYSTEM$/ ) {
        $userTable = 'P_'.$userTable;
    }

    my $query = 'SELECT '.$userTable.'.*,
                        current.userobm_login as userobm_login_current
                 FROM '.$userTable.'
                 LEFT JOIN P_'.$userTable.' current ON current.userobm_id='.$userTable.'.userobm_id
                 WHERE '.$userTable.'.userobm_domain_id='.$self->{'domainId'};

    if( !defined($dbHandler->execQuery( $query, \$self->{'userDescList'} )) ) {
        $self->_log( 'chargement des utilisateurs depuis la BD impossible', 3 );
        return 1;
    }

    return 0;
}


sub _loadUserLinks {
    my $self = shift;
    my %rightDef;

    $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription(), 2 );;

    my $entityId = $self->{'currentEntity'}->getId();
    my $entityType = 'mailbox';

    my $userObmTable = 'UserObm';
    my $entityRightTable = 'EntityRight';
    my $ofUserGroupTable = 'of_usergroup';
    if( $self->{'source'} =~ /^SYSTEM$/ ) {
        $userObmTable = 'P_'.$userObmTable;
        $entityRightTable = 'P_'.$entityRightTable;
        $ofUserGroupTable = 'P_'.$ofUserGroupTable;
    }

    $rightDef{'read'}->{'compute'} = 1;
    $rightDef{'read'}->{'sqlQuery'} = 'SELECT
                  userobm_id,
                  userobm_login
                FROM '.$userObmTable.'
                LEFT JOIN '.$ofUserGroupTable.' ON userobm_id = of_usergroup_user_id
                LEFT JOIN '.$entityRightTable.' ON (userobm_id = entityright_consumer_id AND entityright_consumer = \'user\')
                WHERE entityright_entity_id IN ('.$entityId.') AND entityright_entity = \''.$entityType.'\' AND entityright_write=0 AND entityright_read=1 AND userobm_archive=0 AND userobm_mail_perms=1
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$userObmTable.'
                LEFT JOIN '.$ofUserGroupTable.' ON userobm_id = of_usergroup_user_id
                LEFT JOIN '.$entityRightTable.' ON (of_usergroup_group_id = entityright_consumer_id AND entityright_consumer = \'group\')
                WHERE entityright_entity_id IN ('.$entityId.') AND entityright_entity = \''.$entityType.'\' AND entityright_write=0 AND entityright_read=1 AND userobm_archive=0 AND userobm_mail_perms=1
                ORDER BY userobm_login';

    $rightDef{'writeonly'}->{'compute'} = 1;
    $rightDef{'writeonly'}->{'sqlQuery'} = 'SELECT
                  userobm_id,
                  userobm_login
                FROM '.$userObmTable.'
                LEFT JOIN '.$ofUserGroupTable.' ON userobm_id = of_usergroup_user_id
                LEFT JOIN '.$entityRightTable.' ON (userobm_id = entityright_consumer_id AND entityright_consumer = \'user\')
                WHERE entityright_entity_id IN ('.$entityId.') AND entityright_entity = \''.$entityType.'\' AND entityright_write=1 AND entityright_read=0 AND userobm_archive=0 AND userobm_mail_perms=1
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$userObmTable.'
                LEFT JOIN '.$ofUserGroupTable.' ON userobm_id = of_usergroup_user_id
                LEFT JOIN '.$entityRightTable.' ON (of_usergroup_group_id = entityright_consumer_id AND entityright_consumer = \'group\')
                WHERE entityright_entity_id IN ('.$entityId.') AND entityright_entity = \''.$entityType.'\' AND entityright_write=1 AND entityright_read=0 AND userobm_archive=0 AND userobm_mail_perms=1
                ORDER BY userobm_login';

    $rightDef{'write'}->{'compute'} = 1;
    $rightDef{'write'}->{'sqlQuery'} = 'SELECT
                  userobm_id,
                  userobm_login
                FROM '.$userObmTable.'
                LEFT JOIN '.$ofUserGroupTable.' ON userobm_id = of_usergroup_user_id
                LEFT JOIN '.$entityRightTable.' ON (userobm_id = entityright_consumer_id AND entityright_consumer = \'user\')
                WHERE (entityright_entity_id IN ('.$entityId.') AND entityright_entity = \''.$entityType.'\' AND entityright_write=1 AND entityright_read=1 AND userobm_archive=0 AND userobm_mail_perms=1) OR userobm_id='.$entityId.'
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$userObmTable.'
                LEFT JOIN '.$ofUserGroupTable.' ON userobm_id = of_usergroup_user_id
                LEFT JOIN '.$entityRightTable.' ON (of_usergroup_group_id = entityright_consumer_id AND entityright_consumer = \'group\')
                WHERE entityright_entity_id IN ('.$entityId.') AND entityright_entity = \''.$entityType.'\' AND entityright_write=1 AND entityright_read=1 AND userobm_archive=0 AND userobm_mail_perms=1
                ORDER BY userobm_login';

    $rightDef{'admin'}->{'compute'} = 1;
    $rightDef{'admin'}->{'sqlQuery'} = 'SELECT
                  userobm_id,
                  userobm_login
                FROM '.$userObmTable.'
                LEFT JOIN '.$ofUserGroupTable.' ON userobm_id = of_usergroup_user_id
                LEFT JOIN '.$entityRightTable.' ON (userobm_id = entityright_consumer_id AND entityright_consumer = \'user\')
                WHERE (entityright_entity_id IN ('.$entityId.') AND entityright_entity = \''.$entityType.'\' AND entityright_admin=1 AND userobm_archive=0 AND userobm_mail_perms=1) OR userobm_id='.$entityId.'
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$userObmTable.'
                LEFT JOIN '.$ofUserGroupTable.' ON userobm_id = of_usergroup_user_id
                LEFT JOIN '.$entityRightTable.' ON (of_usergroup_group_id = entityright_consumer_id AND entityright_consumer = \'group\')
                WHERE entityright_entity_id IN ('.$entityId.') AND entityright_entity = \''.$entityType.'\' AND entityright_admin=1 AND userobm_archive=0 AND userobm_mail_perms=1
                ORDER BY userobm_login';

    $rightDef{'public'}->{'compute'} = 0;
    $rightDef{'public'}->{'sqlQuery'} = 'SELECT entityright_read, entityright_write
            FROM '.$entityRightTable.'
            WHERE entityright_entity_id='.$entityId.'
                AND entityright_entity=\''.$entityType.'\'
                AND entityright_consumer_id=0';

    $self->{'currentEntity'}->setLinks( $self->_getEntityRight( \%rightDef ) );

    return 0;
}
