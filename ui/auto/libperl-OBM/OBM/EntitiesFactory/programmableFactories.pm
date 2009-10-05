package OBM::EntitiesFactory::programmableFactories;

$VERSION = '1.0';

use OBM::EntitiesFactory::factory;
@ISA = ('OBM::EntitiesFactory::factory');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(_log dump);


sub new {
    my $class = shift;
    my( $entitiesFactory ) = @_;

    my $self = bless { }, $class;

    if( !defined($entitiesFactory) || (ref($entitiesFactory) !~ /^OBM::entitiesFactory$/) ) {
        $self->_log( 'la factory doit être de type \'OBM::entitiesFactory\'', 3 );
        return undef;
    }

    $self->{'entitiesFactory'} = $entitiesFactory;

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub addEntities {
    my $self = shift;
    my( $programmingObj ) = @_;

    if( ref($programmingObj) ne 'OBM::EntitiesFactory::factoryProgramming' ) {
        $self->_log( 'liste d\'entités à charger incorrecte', 3 );
        return 1;
    }

    $self->{'programmingObj'} = $programmingObj;

    $self->{'updateType'} = $programmingObj->getUpdateType();
    if( !$self->_checkUpdateType() ) {
        return 1;
    }

    SWITCH: {
        if( !defined($programmingObj->getEntitiesType()) ) {
            $self->_log( 'type d\'entité à programmer incorrect', 4 );
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
            $self->_log( 'problème au chargement de la factory des hôtes', 3 );
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
            $self->_log( 'problème au chargement de la factory des groupes', 3 );
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
            $self->_log( 'problème au chargement de la factory des partages de messagerie', 3 );
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
            $self->_log( 'problème au chargement de la factory des utilisateurs', 3 );
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
            $self->_log( 'problème au chargement de la factory des contacts', 3 );
            return 1;
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }
}
