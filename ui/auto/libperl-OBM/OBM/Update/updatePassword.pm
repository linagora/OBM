package OBM::Update::updatePassword;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


require OBM::toolBox;
require OBM::dbUtils;
require OBM::Update::utils;
require OBM::Ldap::ldapEngine;
require OBM::Entities::obmUser;


sub new {
    my $self = shift;
    my( $dbHandler, $parameters ) = @_;

    # Définition des attributs de l'objet
    my %updatePasswdAttr = (
        dbHandler => undef,
        oldPassword => undef,
        newPasswordDesc => undef,
        userLogin => undef,
        userId => undef,
        userObject => undef,
        domainLabel => undef,
        domainId => undef,
        domainList => undef,
        engine => undef,
    );

    if( !defined($dbHandler) || !defined($parameters) ) {
        croak( "[Update::updatePassword]: Usage: PACKAGE->new(DBHANDLER, PARAMLIST)" );
    }

    $updatePasswdAttr{dbHandler} = $dbHandler;


    if( !$parameters->{"no-old"} ) {
        $updatePasswdAttr{oldPassword} = $parameters->{"old-passwd"};
    }

    $updatePasswdAttr{newPasswordDesc}->{newPassword} = $parameters->{passwd};
    $updatePasswdAttr{newPasswordDesc}->{newPasswordType} = $parameters->{type};
    $updatePasswdAttr{newPasswordDesc}->{unix} = $parameters->{unix};
    $updatePasswdAttr{newPasswordDesc}->{samba} = $parameters->{samba};
    $updatePasswdAttr{newPasswordDesc}->{sql} = $parameters->{sql};

    $updatePasswdAttr{userLogin} = $parameters->{login};
    $updatePasswdAttr{domainId} = $parameters->{domain};


    # Obtention du userId BD
    $updatePasswdAttr{userId} = $self->_getUserIdFromUserLoginDomain( $updatePasswdAttr{dbHandler}, $updatePasswdAttr{userLogin}, $updatePasswdAttr{domainId} );
    if( !defined($updatePasswdAttr{userId}) ) {
        &OBM::toolBox::write_log( "[Update::updatePassword]: utilisateur '".$updatePasswdAttr{userLogin}."' inconnu", "W" );
        return undef;
    }


    # Obtention des informations sur les domaines nécessaires
    $updatePasswdAttr{domainList} = &OBM::Update::utils::getDomains( $updatePasswdAttr{dbHandler}, $updatePasswdAttr{domainId} );

    # Obtention des serveurs LDAP par domaines
    &OBM::Update::utils::getLdapServer( $updatePasswdAttr{dbHandler}, $updatePasswdAttr{domainList} );

    # Initialisation du moteur LDAP
    $updatePasswdAttr{engine}->{ldapEngine} = OBM::Ldap::ldapEngine->new( $updatePasswdAttr{domainList} );
    if( !$updatePasswdAttr{"engine"}->{"ldapEngine"}->init( 0 ) ) {
        return undef;
    }

    # Création de l'objet de l'utilisateur
    $updatePasswdAttr{userObject} = OBM::Entities::obmUser->new( 0, 0, $updatePasswdAttr{userId} );
    if( !$updatePasswdAttr{userObject}->getEntity( $updatePasswdAttr{dbHandler}, &OBM::Update::utils::findDomainbyId( $updatePasswdAttr{domainList}, $updatePasswdAttr{domainId} ) ) ) {
        return undef;
    }

    bless( \%updatePasswdAttr, $self );
}


sub update {
    my $self = shift;
    my $ldapEngine = $self->{engine}->{ldapEngine};
    my $userObject = $self->{userObject};

    
    if( $self->{oldPassword} ) {
        if( !$ldapEngine->checkPasswd( $userObject, $self->{oldPassword} ) ) {
            return 0;
        }
    }


    if( !$ldapEngine->updatePassword( $userObject, $self->{newPasswordDesc} ) ) {
        return 0;
    }


    if( !$userObject->updateDbEntityPassword( $self->{dbHandler}, $self->{newPasswordDesc} ) ) {
        return 0;
    }

    return 1;
}


sub _getUserIdFromUserLoginDomain {
    my $self = shift;
    my( $dbHandler, $userLogin, $domainId ) = @_;

    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( "[Update::updatePassword]: connection à la base de donnees incorrecte !", "W" );
        return undef;
    }

    my $query = "SELECT userobm_id FROM UserObm WHERE userobm_login=".$dbHandler->quote($userLogin)." AND userobm_domain_id=".$domainId;
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::updatePassword]: probleme lors de l'execution de la requete.", "W" );
        if( defined($queryResult) ) {
            &OBM::toolBox::write_log( "[Update::updatePassword]: ".$queryResult->err, "W" );
        }

        return undef;
    }

    my( $userId ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    return $userId;
}


sub dump {
    my $self = shift;
    my @desc;

    push( @desc, $self );

    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}
