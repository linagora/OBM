package OBM::EntitiesFactory::globalFactories;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(_log dump);
use OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $entitiesFactory ) = @_;

    my $self = bless { }, $class;

    if( !defined($entitiesFactory) || (ref($entitiesFactory) !~ /^OBM::entitiesFactory$/) ) {
        $self->_log( 'la factory doit être de type \'OBM::entitiesFactory\'', 3 );
        return undef;
    }

    $self->{'entitiesFactory'} = $entitiesFactory;

    if( $self->_initDeleteFactory() ) {
        $self->_log( 'problème à l\'initialisation des factory des entités supprimées', 0 );
        return undef;
    }

    if( $self->_initUpdateFactory() ) {
        $self->_log( 'problème à l\'initialisation des factory', 0 );
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub _initDeleteFactory {
    my $self = shift;

    # Deleted hosts
    if( $self->_initDeleteHostFactory() ) {
        $self->_log( 'problème au chargement des hôtes à supprimer', 1 );
        return 1;
    }

    # Deleted groups
    if( $self->_initDeleteGroupFactory() ) {
        $self->_log( 'problème au chargement des groupes à supprimer', 1 );
        return 1;
    }

    # Deleted mailshares
    if( $self->_initDeleteMailshareFactory() ) {
        $self->_log( 'problème au chargement des partages de messagerie à supprimer', 1 );
        return 1;
    }

    return 0;
}


sub _initDeleteHostFactory {
    my $self = shift;
    my $entitiesFactory = $self->{'entitiesFactory'};

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return 1;
    }

    my $query = 'SELECT host_id
                    FROM P_Host
                    WHERE host_domain_id='.$entitiesFactory->{'domain'}->getId().' AND host_id NOT IN
                        (SELECT host_id
                            FROM Host
                            WHERE host_domain_id='.$entitiesFactory->{'domain'}->getId().')';

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'chargement des hôtes à supprimer depuis la BD impossible', 3 );
        return 1;
    }

    my $deletedHosts = ();
    while( my( $hostId ) = $queryResult->fetchrow_array() ) {
        $self->_log( 'programmation de la suppression de l\'hôte d\'ID '.$hostId, 4 );
        push( @{$deletedHosts}, $hostId );
    }

    if( $#{$deletedHosts} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::hostFactory;
        if( !($entityFactory = OBM::EntitiesFactory::hostFactory->new( 'SYSTEM', 'DELETE', $entitiesFactory->{'domain'}, $deletedHosts )) ) {
            $self->_log( 'problème au chargement de la factory des hôtes à supprimer', 3 );
            return 1;
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    return 0;
}


sub _initDeleteGroupFactory {
    my $self = shift;
    my $entitiesFactory = $self->{'entitiesFactory'};

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return 1;
    }

    my $query = 'SELECT group_id
                    FROM P_UGroup
                    WHERE group_domain_id='.$entitiesFactory->{'domain'}->getId().' AND group_id NOT IN
                        (SELECT group_id
                            FROM UGroup
                            WHERE group_domain_id='.$entitiesFactory->{'domain'}->getId().')';

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'chargement des groupes à supprimer depuis la BD impossible', 3 );
        return 1;
    }

    my $deletedGroups = ();
    while( my( $groupId ) = $queryResult->fetchrow_array() ) {
        $self->_log( 'programmation de la suppression du groupe d\'ID '.$groupId, 4 );
        push( @{$deletedGroups}, $groupId );
    }

    if( $#{$deletedGroups} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::groupFactory;
        if( !($entityFactory = OBM::EntitiesFactory::groupFactory->new( 'SYSTEM', 'DELETE', $entitiesFactory->{'domain'}, $deletedGroups )) ) {
            $self->_log( 'problème au chargement de la factory des groupes à supprimer', 3 );
            return 1;
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    return 0;
}


sub _initDeleteMailshareFactory {
    my $self = shift;
    my $entitiesFactory = $self->{'entitiesFactory'};

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return 1;
    }

    my $query = 'SELECT mailshare_id
                    FROM P_MailShare
                    WHERE mailshare_domain_id='.$entitiesFactory->{'domain'}->getId().' AND mailshare_id NOT IN
                        (SELECT mailshare_id
                            FROM MailShare
                            WHERE mailshare_domain_id='.$entitiesFactory->{'domain'}->getId().')';

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'chargement des partages de messagerie à supprimer depuis la BD impossible', 3 );
        return 1;
    }

    my $deletedMailshare = ();
    while( my( $mailshareId ) = $queryResult->fetchrow_array() ) {
        $self->_log( 'programmation de la suppression du partage de messagerie d\'ID '.$mailshareId, 4 );
        push( @{$deletedMailshare}, $mailshareId );
    }

    if( $#{$deletedMailshare} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::mailshareFactory;
        if( !($entityFactory = OBM::EntitiesFactory::mailshareFactory->new( 'SYSTEM', 'DELETE', $entitiesFactory->{'domain'}, $deletedMailshare )) ) {
            $self->_log( 'problème au chargement de la factory des partages de messagerie à supprimer', 3 );
            return 1;
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    return 0;
}


sub _initUpdateFactory {
    my $self = shift;
    my $entitiesFactory = $self->{'entitiesFactory'};
    my $entityFactory;

    if( $entitiesFactory->{'domain'}->isGlobal() ) {
        require OBM::EntitiesFactory::userSystemFactory;
        if( !($entityFactory = OBM::EntitiesFactory::userSystemFactory->new( $entitiesFactory->{'domain'} )) ) {
            $self->_log( 'problème au chargement de la factory d\'utilisateurs système', 3 );
            return 1;
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    if( !$entitiesFactory->{'domain'}->isGlobal() ) {
        require OBM::EntitiesFactory::mailServerFactory;
        if( !($entityFactory = OBM::EntitiesFactory::mailServerFactory->new( 'WORK', 'UPDATE_ALL', $entitiesFactory->{'domain'} )) ) {
            $self->_log( 'problème au chargement de la factory de configuration des serveurs de courriers', 3 );
            return 1;
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    require OBM::EntitiesFactory::hostFactory;
    if( !($entityFactory = OBM::EntitiesFactory::hostFactory->new( 'WORK', 'UPDATE_ALL', $entitiesFactory->{'domain'} )) ) {
        $self->_log( 'problème au chargement de la factory des hôtes', 3 );
        return 1;
    }

    $entitiesFactory->enqueueFactory( $entityFactory );

    require OBM::EntitiesFactory::groupFactory;
    if( !($entityFactory = OBM::EntitiesFactory::groupFactory->new( 'WORK', 'UPDATE_ALL', $entitiesFactory->{'domain'} )) ) {
        $self->_log( 'problème au chargement de la factory des groupes', 3 );
        return 1;
    }

    $entitiesFactory->enqueueFactory( $entityFactory );

    require OBM::EntitiesFactory::mailshareFactory;
    if( !($entityFactory = OBM::EntitiesFactory::mailshareFactory->new( 'WORK', 'UPDATE_ALL', $entitiesFactory->{'domain'} )) ) {
        $self->_log( 'problème au chargement de la factory des mailshare', 3 );
        return 1;
    }

    $entitiesFactory->enqueueFactory( $entityFactory );

    require OBM::EntitiesFactory::userFactory;
    if( !($entityFactory = OBM::EntitiesFactory::userFactory->new( 'WORK', 'UPDATE_ALL', $entitiesFactory->{'domain'} )) ) {
        $self->_log( 'problème au chargement de la factory des utilisateurs', 3 );
        return 1;
    }

    $entitiesFactory->enqueueFactory( $entityFactory );

    return 0;
}
