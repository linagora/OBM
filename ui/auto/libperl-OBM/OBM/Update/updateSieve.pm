package OBM::Update::updateSieve;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


require OBM::toolBox;
require OBM::imapd;
require OBM::Update::utils;
require OBM::Cyrus::sieveEngine;
require OBM::Entities::obmUser;


sub new {
    my $self = shift;
    my( $parameters ) = @_;

    # Définition des attributs de l'objet
    my %updateSieveAttr = (
        userLogin => undef,
        userId => undef,
        userObject => undef,
        domainId => undef,
        domainList => undef,
        engine => undef
    );

    if( !defined($parameters) ) {
        croak( "[Update::updateSieve]: Usage: PACKAGE->new(DBHANDLER, PARAMLIST)" );
    }

    $updateSieveAttr{userLogin} = $parameters->{login};
    $updateSieveAttr{domainId} = $parameters->{domain};


    # Obtention du userId BD
    $updateSieveAttr{userId} = &OBM::Update::utils::getUserIdFromUserLoginDomain( $updateSieveAttr{userLogin}, $updateSieveAttr{domainId} );
    if( !defined($updateSieveAttr{userId}) ) {
        &OBM::toolBox::write_log( "[Update::updateSieve]: utilisateur '".$updateSieveAttr{userLogin}."' inconnu", "W" );
        return undef;
    }


    # Obtention des informations sur les domaines nécessaires
    $updateSieveAttr{domainList} = &OBM::Update::utils::getDomains( $updateSieveAttr{domainId} );

    # Paramétrage des serveurs IMAP par domaine
    &OBM::Update::utils::getCyrusServers( $updateSieveAttr{"domainList"} );
    if( !&OBM::imapd::getAdminImapPasswd( $updateSieveAttr{"domainList"} ) ) {
        return undef;
    }


    # Initialisation du moteur Sieve
    $updateSieveAttr{engine}->{sieveEngine} = OBM::Cyrus::sieveEngine->new( $updateSieveAttr{"domainList"} );
    if( !defined($updateSieveAttr{engine}->{sieveEngine}) ) {
        &OBM::toolBox::write_log( "[Update::updateSieve]: probleme a l'initialisation du moteur Sieve.", "W" );
        return undef;
    }

    if( !$updateSieveAttr{engine}->{sieveEngine}->init() ) {
        &OBM::toolBox::write_log( "[Update::updateSieve]: probleme a l'initialisation du moteur Sieve.", "W" );
        return undef;
    }


    # Création de l'objet de l'utilisateur
    $updateSieveAttr{userObject} = OBM::Entities::obmUser->new( 0, 0, $updateSieveAttr{userId} );
    if( !defined($updateSieveAttr{userObject}) ) {
        &OBM::toolBox::write_log( "[Update::updateSieve]: erreur a la mise a jour de l'utilisateur.", "W" );
        return undef;
    }

    if( !$updateSieveAttr{userObject}->getEntity( &OBM::Update::utils::findDomainbyId( $updateSieveAttr{domainList}, $updateSieveAttr{domainId} ) ) ) {
        &OBM::toolBox::write_log( "[Update::updateSieve]: erreur a la mise a jour de l'utilisateur.", "W" );
        return undef;
    }


    bless( \%updateSieveAttr, $self );
} 


sub update {
    my $self = shift;
    my $sieveEngine = $self->{engine}->{sieveEngine};
    my $userObject = $self->{userObject};

    if( !defined($sieveEngine) || !defined($userObject) ) {
        return 0;
    }

    return $sieveEngine->update( $userObject );
}
