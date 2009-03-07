package OBM::EntitiesFactory::mailshareFactory;

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
        _checkUpdateType
        _getEntityRight
        _computeRight
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
    $self->{'mailshareDescList'} = undef;


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
    $self->{'mailshareDescList'} = undef;

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

    if( $self->_loadMailshare() ) {
        $self->_log( 'problème lors de l\'obtention de la description des mailshare du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 3 );
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

    while( defined($self->{'mailshareDescList'}) && (my $mailshareDesc = $self->{'mailshareDescList'}->fetchrow_hashref()) ) {
        require OBM::Entities::obmMailshare;
        if( !(my $current = OBM::Entities::obmMailshare->new( $self->{'parentDomain'}, $mailshareDesc )) ) {
            next;
        }else {
            $self->{'currentEntity'} = $current;

            SWITCH: {
                if( $self->{'updateType'} eq 'UPDATE_ALL' ) {
                    if( $self->_loadMailshareLinks() ) {
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
                    if( $self->_loadMailshareLinks() ) {
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


sub _loadMailshare {
    my $self = shift;

    $self->_log( 'chargement des mailshare du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 2 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return 1;
    }

    my $mailshareTablePrefix = '';
    if( $self->{'updateType'} !~ /^(UPDATE_ALL|UPDATE_ENTITY)$/ ) {
        $mailshareTablePrefix = 'P_';
    }

    my $query = 'SELECT '.$mailshareTablePrefix.'MailShare.*,
                        current.mailshare_name as mailshare_name_current
                 FROM '.$mailshareTablePrefix.'MailShare
                 LEFT JOIN P_MailShare current ON current.mailshare_id='.$mailshareTablePrefix.'MailShare.mailshare_id
                 WHERE '.$mailshareTablePrefix.'MailShare.mailshare_domain_id='.$self->{'domainId'};

    if( $self->{'ids'} ) {
        $query .= ' AND '.$mailshareTablePrefix.'MailShare.mailshare_id IN ('.join( ', ', @{$self->{'ids'}}).')';
    }

    if( !defined($dbHandler->execQuery( $query, \$self->{'mailshareDescList'} )) ) {
        $self->_log( 'chargement des mailshare depuis la BD impossible', 3 );
        return 1;
    }

    return 0;
}


sub _loadMailshareLinks {
    my $self = shift;
    my %rightDef;

    $self->_log( 'chargement des liens de '.$self->{'currentEntity'}->getDescription(), 2 );

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
                WHERE mailshareentity_mailshare_id = '.$entityId.' AND entityright_write=0 AND entityright_read=1 AND userobm_archive=0 AND userobm_mail_perms=1
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$groupEntityTable.' ON groupentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailshareEntity.' ON mailshareentity_entity_id = entityright_entity_id
                INNER JOIN '.$ofUserGroupTable.' ON of_usergroup_group_id = groupentity_group_id
                INNER JOIN '.$userObmTable.' ON userobm_id = of_usergroup_user_id
                WHERE mailshareentity_mailshare_id = '.$entityId.' AND entityright_write=0 AND entityright_read=1 AND userobm_archive=0 AND userobm_mail_perms=1
                ORDER BY userobm_login';

    $rightDef{'writeonly'}->{'compute'} = 1;
    $rightDef{'writeonly'}->{'sqlQuery'} = 'SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$userEntityTable.' ON userentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailshareEntity.' ON mailshareentity_entity_id = entityright_entity_id
                INNER JOIN '.$userObmTable.' ON userobm_id = userentity_user_id
                WHERE mailshareentity_mailshare_id = '.$entityId.' AND entityright_write=1 AND entityright_read=0 AND userobm_archive=0 AND userobm_mail_perms=1
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$groupEntityTable.' ON groupentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailshareEntity.' ON mailshareentity_entity_id = entityright_entity_id
                INNER JOIN '.$ofUserGroupTable.' ON of_usergroup_group_id = groupentity_group_id
                INNER JOIN '.$userObmTable.' ON userobm_id = of_usergroup_user_id
                WHERE mailshareentity_mailshare_id = '.$entityId.' AND entityright_write=1 AND entityright_read=0 AND userobm_archive=0 AND userobm_mail_perms=1
                ORDER BY userobm_login';

    $rightDef{'write'}->{'compute'} = 1;
    $rightDef{'write'}->{'sqlQuery'} = 'SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$userEntityTable.' ON userentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailshareEntity.' ON mailshareentity_entity_id = entityright_entity_id
                INNER JOIN '.$userObmTable.' ON userobm_id = userentity_user_id
                WHERE mailshareentity_mailshare_id = '.$entityId.' AND entityright_write=1 AND entityright_read=1 AND userobm_archive=0 AND userobm_mail_perms=1
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$groupEntityTable.' ON groupentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailshareEntity.' ON mailshareentity_entity_id = entityright_entity_id
                INNER JOIN '.$ofUserGroupTable.' ON of_usergroup_group_id = groupentity_group_id
                INNER JOIN '.$userObmTable.' ON userobm_id = of_usergroup_user_id
                WHERE mailshareentity_mailshare_id = '.$entityId.' AND entityright_write=1 AND entityright_read=1 AND userobm_archive=0 AND userobm_mail_perms=1
                ORDER BY userobm_login';

    $rightDef{'admin'}->{'compute'} = 1;
    $rightDef{'admin'}->{'sqlQuery'} = 'SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$userEntityTable.' ON userentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailshareEntity.' ON mailshareentity_entity_id = entityright_entity_id
                INNER JOIN '.$userObmTable.' ON userobm_id = userentity_user_id
                WHERE mailshareentity_mailshare_id = '.$entityId.' AND entityright_admin=1 AND userobm_archive=0 AND userobm_mail_perms=1
                UNION
                SELECT
                  userobm_id,
                  userobm_login
                FROM '.$entityRightTable.'
                INNER JOIN '.$groupEntityTable.' ON groupentity_entity_id = entityright_consumer_id
                INNER JOIN '.$mailshareEntity.' ON mailshareentity_entity_id = entityright_entity_id
                INNER JOIN '.$ofUserGroupTable.' ON of_usergroup_group_id = groupentity_group_id
                INNER JOIN '.$userObmTable.' ON userobm_id = of_usergroup_user_id
                WHERE mailshareentity_mailshare_id = '.$entityId.' AND entityright_admin=1 AND userobm_archive=0 AND userobm_mail_perms=1
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
