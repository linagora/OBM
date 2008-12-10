package OBM::entitiesFactory;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(_log dump);
use OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $mode, $domainId ) = @_;

    my $self = bless { }, $class;

    if( !defined($domainId) || ref($domainId) ) {
        $self->_log( 'un et un seul identifiant de domaine doit être indiqué', 3 );
        return undef;
    }

    $self->{'domainId'} = $domainId;
    $self->{'domain'} = undef;
    $self->{'entitiesQueue'} = undef;
    $self->{'factoriesQueue'} = undef;

    $self->{'mode'} = $mode;
    if( $self->_initMode() ) {
        $self->_log( 'problème à l\'initialisation de la factory', 4 );
        return undef;
    }

    $self->{'running'} = undef;

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
    $self->{'domain'} = undef;
    $self->{'entitiesQueue'} = undef;
    $self->{'factoriesQueue'} = undef;
}


sub _initMode {
    my $self = shift;

    if( !defined($self->{'mode'}) ) {
        $self->_log( 'mode d\'exécution non définit', 3 );
        return 1;
    }

    if( ref($self->{'mode'}) ) {
        $self->_log( 'mode d\'exécution incorrect', 3 );
        return 1;
    }

    if( $self->{'mode'} =~ /^GLOBAL$/ ) {
        $self->_log( 'mode d\'exécution global', 3 );
        return $self->_initGlobal();
    }

    if( $self->{'mode'} =~ /^INCREMENTAL$/ ) {
        $self->_log( 'mode d\'exécution incrémental', 3 );
        return $self->_initIncremental();
    }

    $self->_log( 'mode d\'exécution \''.$self->{'mode'}.'\' incorrect', 3 );

    return 1;
}


sub enqueueFactory {
    my $self = shift;
    my( $factory ) = @_;

    if( !defined($factory) || ref($factory) !~ /^OBM::EntitiesFactory::/ ) {
        $self->_log( 'factory incorrecte, ajout dans la file d\'attente impossible', 1 );
        return 1;
    }

    push( @{$self->{'factoriesQueue'}}, $factory );

    return 0;
}


sub enqueueEntity {
    my $self = shift;
    my( $entity ) = @_;

    if( !defined($entity) || ref($entity) !~ /^OBM::Entities::/ ) {
        $self->_log( 'entity incorrecte, ajout dans la file d\'attente impossible', 1 );
        return 1;
    }

    push( @{$self->{'entitiesQueue'}}, $entity );

    return 0;
}


sub _initGlobal {
    my $self = shift;

    require OBM::EntitiesFactory::globalFactories;
    my $globalFactories = OBM::EntitiesFactory::globalFactories->new( $self );
    if( !defined($globalFactories) ) {
        $self->_log( 'problème à l\'initialisation des factories nécessaire pour le mode global', 0 );
        return 1;
    }

    return 0;
}


sub _initIncremental {
    my $self = shift;

    require OBM::EntitiesFactory::domainFactory;
    my $entityFactory = OBM::EntitiesFactory::domainFactory->new( 'SYSTEM', $self->{'domainId'});

    if( $self->_loadDomains( $entityFactory ) ) {
        return 1;
    }

    return 0;
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

    $self->{'running'} = 1;
    return $self->{'running'};
}


sub next {
    my $self = shift;

    $self->_log( 'obtention de l\'entité suivante', 2 );

    if( !$self->isRunning() ) {
        if( !$self->_start() ) {
            return undef;
        }
    }

    if( my $currentEntity = shift( @{$self->{'entitiesQueue'}} ) ) {
        return $currentEntity;
    }

    while( my $currentFactory = shift( @{$self->{'factoriesQueue'}} ) ) {
        if( my $currentEntity = $currentFactory->next() ) {
            unshift( @{$self->{'factoriesQueue'}}, $currentFactory );
            return $currentEntity;
        }
    }

    return undef;
}


sub _loadDomains {
    my $self = shift;
    my( $domainFactory ) = @_;

    if( !defined($domainFactory) || ref($domainFactory) ne 'OBM::EntitiesFactory::domainFactory' ) {
        $self->_log( 'problème au chargement de la factory de domaine', 3 );
        return 1;
    }

    while( my $currentDomain = $domainFactory->next() ) {
        SWITCH: {
            if( $currentDomain->getId() == $self->{'domainId'} ) {
                $self->{'domain'} = $currentDomain;
            }

            $self->enqueueEntity( $currentDomain );
        }
    }

    if( !defined($self->{'domain'}) || (ref($self->{'domain'}) ne 'OBM::Entities::obmDomain') ) {
        $self->_log( 'domain d\'identifiant '.$self->{'domainId'}.' non trouvé', 4 );
        return 1;
    }

    return 0;
}
