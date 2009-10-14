package OBM::Update::updateBdQuotaUse;

$VERSION = '1.0';

use OBM::Entities::systemEntityIdGetter;
@ISA = ('OBM::Entities::systemEntityIdGetter');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


use OBM::Tools::commonMethods qw(_log dump);


sub new {
    my $class = shift;
    my( $parameters ) = @_;

    my $self = bless { }, $class;

    if( !defined($parameters) ) {
        $self->_log( 'paramètres d\'initialisation non définis', 0 );
        return undef;
    }

    if( defined($parameters->{'domain-id'}) ) {
        push( @{$self->{'domainId'}}, $parameters->{'domain-id'} );
    }

    if( $self->_getEntities() ) {
        return undef;
    }

    return $self;
} 


sub _getEntities {
    my $self = shift;

    if( ref($self->{'domainId'}) ne 'ARRAY' ) {
        $self->{'domainId'} = $self->getDomainId( 0, undef );
    }

    my $errorCode = 0;
    my $userIdByDomain = $self->getUserIdByDomainId( $self->{'domainId'} );
    while( my( $domainId, $userIdList ) = each(%{$userIdByDomain}) ) {
        # Getting user entity
        require OBM::EntitiesFactory::factoryProgramming;
        my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
        if( !defined($programmingObj) ) {
            $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
            $errorCode = 1;
            next;
        }
        if( $programmingObj->setEntitiesType( 'USER' ) || $programmingObj->setUpdateType( 'SYSTEM_ENTITY' ) || $programmingObj->setEntitiesIds( $userIdList ) ) {
            $self->_log( 'problème lors de l\'initialisation du programmateur de factory', 4 );
            $errorCode = 1;
            next;
        }

        require OBM::entitiesFactory;
        my $entitiesFactory = OBM::entitiesFactory->new( 'PROGRAMMABLEWITHOUTDOMAIN', $domainId );
        if( !defined($entitiesFactory) ) {
            $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
            $errorCode = 1;
            next;
        }
        if( $entitiesFactory->loadEntities($programmingObj) ) {
            $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
            $errorCode = 1;
            next;
        }

        push( @{$self->{'entitiesFactory'}}, $entitiesFactory );
    }

    return $errorCode;
}


sub update {
    my $self = shift;

    if( !defined($self->{'entitiesFactory'}) || ref($self->{'entitiesFactory'}) ne 'ARRAY' ) {
        $self->_log( 'problème avec la factory d\'entités', 0 );
        return 1;
    }

    require OBM::Cyrus::cyrusUpdateQuotaUsedEngine;
    $self->_log( 'initialisation du moteur Cyrus de mise à jour du quota utilisé', 2 );
    my $cyrusUpdateQuotaUsedEngine;
    if( !($cyrusUpdateQuotaUsedEngine = OBM::Cyrus::cyrusUpdateQuotaUsedEngine->new()) ) {
        $self->_log( 'problème à l\'initialisation du moteur Cyrus de mise à jour du quota utilisé', 0 );
        return 1;
    }

    require OBM::DbUpdater::sqlQuotaUsedUpdater;
    $self->_log( 'initialisation du moteur de mise à jour BD du quota utilisé', 2 );
    my $sqlQuotaUsedUpdater;
    if( !($sqlQuotaUsedUpdater = OBM::DbUpdater::sqlQuotaUsedUpdater->new()) ) {
        $self->_log( 'problème à l\'initialisation du moteur de mise à jour BD du quota utilisé', 0 );
        return 1;
    }

    my $errorCode = 0;
    while( my $entityFactory = pop(@{$self->{'entitiesFactory'}}) ) {
        while( my $entity = $entityFactory->next() ) {
            $self->_log( 'obtention du quota utilisé de '.$entity->getDescription(), 1 );

            if( $cyrusUpdateQuotaUsedEngine->update( $entity ) ) {
                $self->_log( 'problème lors de l\'obtention du quota Cyrus utilisé de '.$entity->getDescription(), 0 );
                $errorCode = 1;
                next;
            }

            $self->_log( 'mise à jour en BD du quota utilisé de '.$entity->getDescription(), 1 );

            if( $sqlQuotaUsedUpdater->update( $entity ) ) {
                $self->_log( 'problème lors de la mise à jour en BD du quota utilisé de '.$entity->getDescription(), 0 );
                $errorCode = 1;
                next;
            }
        }
    }

    return $errorCode;
}
