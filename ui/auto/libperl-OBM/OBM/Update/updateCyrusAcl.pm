package OBM::Update::updateCyrusAcl;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

#require OBM::toolBox;
#require OBM::imapd;
require OBM::Update::utils;
require OBM::Cyrus::cyrusEngine;


sub new {
    my $self = shift;
    my( $parameters ) = @_;

    my %updateAclAttr = (
        name => undef,
        type => undef,
        id => undef,
        object => undef,
        domainId => undef,
        domainList => undef,
        engine => undef
    );

    if( !defined($parameters) ) {
        croak( "[Update::updateCyrusAcl]: Usage: PACKAGE->new(DBHANDLER, PARAMLIST)" );
    }

    $updateAclAttr{domainId} = $parameters->{domain};
    $updateAclAttr{name} = $parameters->{name};
    $updateAclAttr{type} = $parameters->{type};

    if( $updateAclAttr{type} =~ /^mailbox$/ ) {
        $updateAclAttr{id} = &OBM::Update::utils::getUserIdFromUserLoginDomain( $updateAclAttr{name}, $updateAclAttr{domainId} );
    }elsif( $updateAclAttr{type} =~ /^mailshare$/ ) {
        $updateAclAttr{id} = &OBM::Update::utils::getMailshareIdFromMailshareNameDomain( $updateAclAttr{name}, $updateAclAttr{domainId} );
    }

    if( !defined($updateAclAttr{id}) ) {
        &OBM::toolBox::write_log( "[Update::updateCyrusAcl]: entite '".$updateAclAttr{name}."' de type '".$updateAclAttr{type}."' inconnu", "W" );
        return undef;
    }

    # Obtention des informations sur les domaines nécessaires
    $updateAclAttr{domainList} = &OBM::Update::utils::getDomains( $updateAclAttr{domainId} );

    # Paramétrage des serveurs IMAP par domaine
    &OBM::Update::utils::getCyrusServers( $updateAclAttr{"domainList"} );
    if( !&OBM::imapd::getAdminImapPasswd( $updateAclAttr{"domainList"} ) ) {
        return undef;
    }

    # Initialisation du moteur Cyrus
    $updateAclAttr{"engine"}->{"cyrusEngine"} = OBM::Cyrus::cyrusEngine->new( $updateAclAttr{"domainList"} );
    if( !$updateAclAttr{"engine"}->{"cyrusEngine"}->init() ) {
        return undef;
    }

    # Création de l'objet de l'utilisateur avec ses liens
    if( $updateAclAttr{type} =~ /^mailbox$/ ) {
        require OBM::Entities::obmUser;
        $updateAclAttr{object} = OBM::Entities::obmUser->new( 1, 0, $updateAclAttr{id} );
    }elsif( $updateAclAttr{type} =~ /^mailshare$/ ) {
        require OBM::Entities::obmMailshare;
        $updateAclAttr{object} = OBM::Entities::obmMailshare->new( 1, 0, $updateAclAttr{id} );
    }

    if( !defined($updateAclAttr{object}) ) {
        &OBM::toolBox::write_log( "[Update::updateCyrusAcl]: erreur a l'initialisation de l'utilisateur.", "W" );
        return undef;
    }

    if( !$updateAclAttr{object}->getEntity( &OBM::Update::utils::findDomainbyId( $updateAclAttr{domainList}, $updateAclAttr{domainId} ) ) ) {
        &OBM::toolBox::write_log( "[Update::updateCyrusAcl]: erreur a l'initialisation de l'utilisateur.", "W" );
        return undef;
    }


    bless( \%updateAclAttr, $self );
}


sub update {
    my $self = shift;
    my $object = $self->{object};
    my $cyrusEngine = $self->{"engine"}->{"cyrusEngine"};

    &OBM::toolBox::write_log( "[Update::updateCyrusAcl]: mise a jour des ACL de l'entite ".$object->getEntityDescription(), "W" );
    if( !$cyrusEngine->updateAcl($object) ) {
        &OBM::toolBox::write_log( "[Update::updateCyrusAcl]: erreur lors de la mise a jour des ACLs", "W" );
        return 0;
    }

    &OBM::toolBox::write_log( "[Update::updateCyrusAcl]: mise a jour en BD des ACLs de l'entite ".$object->getEntityDescription(), "W" );
    if( !$object->updateDbEntityLinks() ) {
        &OBM::toolBox::write_log( "[Update::updateCyrusAcl]: erreur lors de la mise a jour de la BD", "W" );
        return 0;
    }

    return 1;
}


sub DESTROY {
    my $self = shift;
    my $cyrusEngine = $self->{"engine"}->{"cyrusEngine"};

    $cyrusEngine->DESTROY();
}
