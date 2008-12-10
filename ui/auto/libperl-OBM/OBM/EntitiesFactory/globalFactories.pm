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

    if( $self->_initDomainFactory() ) {
        $self->_log( 'problème à l\'initialisation de la factory de domaine', 0 );
        return undef;
    }

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


sub _initDomainFactory {
    my $self = shift;
    my $entitiesFactory = $self->{'entitiesFactory'};

    require OBM::EntitiesFactory::domainFactory;
    my $entityFactory = OBM::EntitiesFactory::domainFactory->new( 'WORK', $entitiesFactory->{'domainId'} );

    if( $entitiesFactory->_loadDomains( $entityFactory ) ) {
        return 1;
    }

    return 0;
}


sub _initDeleteFactory {
    my $self = shift;
    my $entitiesFactory = $self->{'entitiesFactory'};

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
        if( !($entityFactory = OBM::EntitiesFactory::mailServerFactory->new( 'WORK', 'ALL', $entitiesFactory->{'domain'} )) ) {
            $self->_log( 'problème au chargement de la factory de configuration des serveurs de courriers', 3 );
            return 1;
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    require OBM::EntitiesFactory::hostFactory;
    if( !($entityFactory = OBM::EntitiesFactory::hostFactory->new( 'WORK', 'ALL', $entitiesFactory->{'domain'} )) ) {
        $self->_log( 'problème au chargement de la factory des hôtes', 3 );
        return 1;
    }

    $entitiesFactory->enqueueFactory( $entityFactory );

    require OBM::EntitiesFactory::groupFactory;
    if( !($entityFactory = OBM::EntitiesFactory::groupFactory->new( 'WORK', 'ALL', $entitiesFactory->{'domain'} )) ) {
        $self->_log( 'problème au chargement de la factory des groupes', 3 );
        return 1;
    }

    $entitiesFactory->enqueueFactory( $entityFactory );

    require OBM::EntitiesFactory::mailshareFactory;
    if( !($entityFactory = OBM::EntitiesFactory::mailshareFactory->new( 'WORK', 'ALL', $entitiesFactory->{'domain'} )) ) {
        $self->_log( 'problème au chargement de la factory des mailshare', 3 );
        return 1;
    }

    $entitiesFactory->enqueueFactory( $entityFactory );

    require OBM::EntitiesFactory::userFactory;
    if( !($entityFactory = OBM::EntitiesFactory::userFactory->new( 'WORK', 'ALL', $entitiesFactory->{'domain'} )) ) {
        $self->_log( 'problème au chargement de la factory des utilisateurs', 3 );
        return 1;
    }

    $entitiesFactory->enqueueFactory( $entityFactory );

    return 0;
}
