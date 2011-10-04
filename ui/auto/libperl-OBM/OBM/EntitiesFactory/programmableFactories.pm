package OBM::EntitiesFactory::programmableFactories;

$VERSION = '1.0';

use OBM::EntitiesFactory::factory;
use OBM::Log::log;
@ISA = ('OBM::EntitiesFactory::factory', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub new {
    my $class = shift;
    my( $entitiesFactory ) = @_;

    my $self = bless { }, $class;

    if( !defined($entitiesFactory) || (ref($entitiesFactory) !~ /^OBM::entitiesFactory$/) ) {
        $self->_log( 'la factory doit être de type \'OBM::entitiesFactory\'', 1 );
        return undef;
    }

    $self->{'entitiesFactory'} = $entitiesFactory;

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );
}


sub addEntities {
    my $self = shift;
    my( $programmingObj ) = @_;

    if( ref($programmingObj) ne 'OBM::EntitiesFactory::factoryProgramming' ) {
        $self->_log( 'liste d\'entités à charger incorrecte', 1 );
        return 1;
    }

    $self->{'programmingObj'} = $programmingObj;

    $self->{'updateType'} = $programmingObj->getUpdateType();
    if( !$self->_checkUpdateType() ) {
        return 1;
    }

    SWITCH: {
        if( !defined($programmingObj->getEntitiesType()) ) {
            $self->_log( 'type d\'entité à programmer incorrect', 1 );
            return 1;
        }

        if( $programmingObj->getEntitiesType() eq 'USER' ) {
            $self->_initUserFactory();
            last SWITCH;
        }

        if( $programmingObj->getEntitiesType() eq 'MAILSHARE' ) {
            $self->_initMailshareFactory();
            last SWITCH;
        }

        if( $programmingObj->getEntitiesType() eq 'CONTACT' ) {
            $self->_initContactFactory();
            last SWITCH;
        }

        if( $programmingObj->getEntitiesType() eq 'GROUP' ) {
            $self->_initGroupFactory();
            last SWITCH;
        }

        if( $programmingObj->getEntitiesType() eq 'HOST' ) {
            $self->_initHostFactory();
            last SWITCH;
        }

        if( $programmingObj->getEntitiesType() eq 'CONTACT_SERVICE' ) {
            $self->_initContactServiceFactory();
            last SWITCH;
        }

        $self->_log( 'type d\'entité non supporté', 0 );
    }

    return 0;
}


sub _initHostFactory {
    my $self = shift;
    my $entitiesIds = $self->{'programmingObj'}->getEntitiesIds();
    my $entitiesFactory = $self->{'entitiesFactory'};

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::hostFactory;
        if( !($entityFactory = OBM::EntitiesFactory::hostFactory->new( $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des hôtes', 1 );
            return 1;
        }

        if( $self->{'programmingObj'}->getUpdateLinkedEntities() ) {
            $entityFactory->setUpdateLinkedEntities();
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    return 0;
}


sub _initGroupFactory {
    my $self = shift;
    my $entitiesIds = $self->{'programmingObj'}->getEntitiesIds();
    my $entitiesFactory = $self->{'entitiesFactory'};

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::groupFactory;
        if( !($entityFactory = OBM::EntitiesFactory::groupFactory->new( $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des groupes', 1 );
            return 1;
        }

        if( $self->{'programmingObj'}->getUpdateLinkedEntities() ) {
            $entityFactory->setUpdateLinkedEntities();
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    return 0;
}


sub _initMailshareFactory {
    my $self = shift;
    my $entitiesIds = $self->{'programmingObj'}->getEntitiesIds();
    my $entitiesFactory = $self->{'entitiesFactory'};

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::mailshareFactory;
        if( !($entityFactory = OBM::EntitiesFactory::mailshareFactory->new( $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des partages de messagerie', 1 );
            return 1;
        }

        if( $self->{'programmingObj'}->getUpdateLinkedEntities() ) {
            $entityFactory->setUpdateLinkedEntities();
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    return 0;
}


sub _initUserFactory {
    my $self = shift;
    my $entitiesIds = $self->{'programmingObj'}->getEntitiesIds();
    my $entitiesFactory = $self->{'entitiesFactory'};

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::userFactory;
        if( !($entityFactory = OBM::EntitiesFactory::userFactory->new( $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des utilisateurs', 1 );
            return 1;
        }

        if( $self->{'programmingObj'}->getUpdateLinkedEntities() ) {
            $entityFactory->setUpdateLinkedEntities();
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    return 0;
}


sub _initContactFactory {
    my $self = shift;
    my $entitiesIds = $self->{'programmingObj'}->getEntitiesIds();
    my $entitiesFactory = $self->{'entitiesFactory'};

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::contactFactory;
        if( !($entityFactory = OBM::EntitiesFactory::contactFactory->new( $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des contacts', 1 );
            return 1;
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }
}


sub _initContactServiceFactory {
    my $self = shift;
    my $entityFactory;
    my $entitiesFactory = $self->{'entitiesFactory'};

    require OBM::EntitiesFactory::contactServiceFactory;
    if( !($entityFactory = OBM::EntitiesFactory::contactServiceFactory->new( $self->{'updateType'}, $entitiesFactory->{'domain'} )) ) {
        $self->_log( 'problème au chargement de la factory du service contact', 1 );
        return 1;
    }

    $entitiesFactory->enqueueFactory( $entityFactory );
}
