package OBM::Update::updateCyrusAcl;

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


    $self->{'entityType'} = $parameters->{'type'};
    $self->{'entityName'} = $parameters->{'name'};
    $self->{'domainId'} = $parameters->{'domain-id'};

    if( $self->_getEntity() ) {
        return undef;
    }

    return $self;
}


sub _getEntity {
    my $self = shift;

    if( $self->{'entityType'} =~ /^mailbox$/  ) {
        return $self->_getUserEntity();
    }elsif( $self->{'entityType'} =~ /^mailshare$/ ) {
        return $self->_getMaishareEntity();
    }else {
       $self->_log( 'type d\'entité inconnu', 3 );
       return 1;
    }

    return 0;
}


sub _getUserEntity {
    my $self = shift;

    # Getting BD user ID
    my $userId = $self->_getUserIdFromUserLoginDomain( $self->{'entityName'}, $self->{'domainId'});
    if( !defined($userId) ) {
        $self->_log( 'utilisateur \''.$self->{'entityName'}.'\', domaine d\'ID '.$self->{'domainId'}.' inconnu', 1 );
        return 1;
    }

    # Getting user entity
    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }
    if( $programmingObj->setEntitiesType( 'USER' ) || $programmingObj->setUpdateType( 'UPDATE_LINKS' ) || $programmingObj->setEntitiesIds( [$userId] ) ) {
        $self->_log( 'problème lors de l\'initialisation du programmateur de factory', 4 );
        return 1;
    }

    require OBM::entitiesFactory;
    my $entitiesFactory = OBM::entitiesFactory->new( 'PROGRAMMABLE', 1 );
    if( !defined($entitiesFactory) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }
    if( $entitiesFactory->loadEntities($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }

    while( my $userEntity = $entitiesFactory->next() ) {
        $self->{'entity'} = $userEntity;
    }

    if( !defined($self->{'entity'}) ) {
        $self->_log( 'problème lors de la récupération de la description de l\'utilisateur', 0 );
        return 1;
    }

    return 0;
}


sub _getMaishareEntity {
    my $self = shift;

    # Getting BD mailshare ID
    my $mailshareId = $self->_getMailShareIdFromUserLoginDomain( $self->{'entityName'}, $self->{'domainId'});
    if( !defined($mailshareId) ) {
        $self->_log( 'patage messagerie \''.$self->{'entityName'}.'\', domaine d\'ID '.$self->{'domainId'}.' inconnu', 1 );
        return 1;
    }

    # Getting mailshare entity
    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }
    if( $programmingObj->setEntitiesType( 'MAILSHARE' ) || $programmingObj->setUpdateType( 'UPDATE_LINKS' ) || $programmingObj->setEntitiesIds( [$mailshareId] ) ) {
        $self->_log( 'problème lors de l\'initialisation du programmateur de factory', 4 );
        return 1;
    }

    require OBM::entitiesFactory;
    my $entitiesFactory = OBM::entitiesFactory->new( 'PROGRAMMABLE', 1 );
    if( !defined($entitiesFactory) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }
    if( $entitiesFactory->loadEntities($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }

    while( my $mailshareEntity = $entitiesFactory->next() ) {
        $self->{'entity'} = $mailshareEntity;
    }

    if( !defined($self->{'entity'}) ) {
        $self->_log( 'problème lors de la récupération de la description du partage de messagerie', 0 );
        return 1;
    }

    return 0;
}


sub update {
    my $self = shift;

    require OBM::Cyrus::cyrusUpdateAclEngine;
    $self->_log( 'initialisation du moteur ide mise à jour des ACLs et mise à jour des ACLs', 2 );
    if( !(my $cyrusUpdateAclEngine = OBM::Cyrus::cyrusUpdateAclEngine->new()) || ($cyrusUpdateAclEngine->update( $self->{'entity'} )) ) {
        $self->_log( 'problème à la mise à jour des ACLs de '.$self->{'entity'}->getDescription(), 0 );
        return 1;
    }

    require OBM::dbUpdater;
    $self->_log( 'initialisation du BD updater', 2 );
    if( !($self->{'dbUpdater'} = OBM::dbUpdater->new()) ) {
        $self->_log( 'echec de l\'initialisation du BD updater', 0 );
        return 1;
    }

    $self->{'entity'}->setUpdated();

    if( $self->{'dbUpdater'}->update($self->{'entity'}) ) {
        $self->_log( 'problème à la mise à jour BD de l\'entité '.$$self->{'entity'}->getDescription(), 1 );
        return 1;
    }

 
    return 0;
}
