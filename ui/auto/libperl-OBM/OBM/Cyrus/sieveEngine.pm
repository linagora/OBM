package OBM::Cyrus::sieveEngine;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
use strict;

use OBM::Parameters::common;
use Cyrus::SIEVE::managesieve;


sub new {
    my $self = shift;
    my( $domainList ) = @_;

    # Definition des attributs de l'objet
    my %sieveEngineAttr = (
        domainList => undef
    );


    if( !defined($domainList) ) {
        croak( "Usage: PACKAGE->new(DOMAINLIST)" );
    }else {
        $sieveEngineAttr{"domainList"} = $domainList;
    }

    bless( \%sieveEngineAttr, $self );
}


sub init {
    my $self = shift;

    if( !$OBM::Parameters::common::obmModules->{"mail"} ) {
        return 0;
    }
    
    &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: initialisation du moteur", "W" );

    return 1;
}


sub destroy {
    my $self = shift;

    &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: arret du moteur", "W" );

    return 1;
}


sub dump {
    my $self = shift;
    my @desc;

    push( @desc, $self );

    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}


sub _connectSrvSieve {
    my $self = shift;
    my( $srvDesc, $boxLogin ) = @_;
    # Il faut remplacer le '@' des login utilisateurs par des '%'
    $boxLogin =~ s/@/%/;

    if( !defined($srvDesc->{"imap_server_ip"}) ) {
        return 0;
    }
    my $imapServerIp = $srvDesc->{"imap_server_ip"};

    if( !defined($srvDesc->{"imap_server_login"}) ) {
        return 0;
    }
    my $imapServerLogin = $srvDesc->{"imap_server_login"};

    if( !defined($srvDesc->{"imap_server_passwd"}) ) {
        return 0;
    }
    my $imapServerPasswd = $srvDesc->{"imap_server_passwd"};


    &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: connexion au serveur SIEVE '".$srvDesc->{"imap_server_name"}."' en tant que '".$srvDesc->{"imap_server_login"}."' pour le compte de '".$boxLogin."'", "W" );
    $srvDesc->{"imap_sieve_server_conn"} = sieve_get_handle( $imapServerIp, sub{return $boxLogin}, sub{return $imapServerLogin}, sub{return $imapServerPasswd}, sub{return undef} );

    if( !defined($srvDesc->{"imap_sieve_server_conn"}) ) {
        &OBM::toolBox::write_log( "sieveEngine : probleme lors de la connexion au serveur SIEVE", "W" );
        return 0;
    }else {
        &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: connexion au serveur SIEVE etablie", "W" );
    }

    return 1;
}


sub _disconnectSrvSieve {
    my $self = shift;
    my( $srvDesc ) = @_;

    if( !defined($srvDesc->{"imap_sieve_server_conn"}) ) {
        return 1;
    }
    my $imapSieveServerConn = $srvDesc->{"imap_sieve_server_conn"};

    &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: deconnexion du serveur SIEVE.", "W" );
    sieve_logout( $imapSieveServerConn );

    return 1;
}


sub _findDomainbyId {
    my $self = shift;
    my( $domainId ) = @_;
    my $domainDesc = undef;

    if( !defined($domainId) || ($domainId !~ /^\d+$/) ) {
        return undef;
    }

    for( my $i=0; $i<=$#{$self->{"domainList"}}; $i++ ) {
        if( $self->{"domainList"}->[$i]->{"domain_id"} == $domainId ) {
            $domainDesc = $self->{"domainList"}->[$i];
            last;
        }
    }

    return $domainDesc;
}


sub _findCyrusSrvbyId {
    my $self = shift;
    my( $domainId, $cyrusSrvId ) = @_;

    if( !defined($domainId) || ($domainId !~ /^\d+$/) ) {
        return undef;

    }elsif(!defined($cyrusSrvId) || ($cyrusSrvId !~ /^\d+$/) ) {
        return undef;

    }

    my $domainDesc = $self->_findDomainbyId( $domainId );
    if( !defined($domainDesc) ) {
        return undef;
    }

    if( !defined($domainDesc->{"imap_servers"}) || ($#{$domainDesc->{"imap_servers"}} < 0) ) {
        return undef;
    }

    my $cyrusSrvList = $domainDesc->{"imap_servers"};
    my $cyrusSrv = undef;
    for( my $i=0; $i<=$#$cyrusSrvList; $i++ ) {
        if( $cyrusSrvList->[$i]->{"imap_server_id"} == $cyrusSrvId ) {
            $cyrusSrv = $cyrusSrvList->[$i];
            last;
        }
    }

    return $cyrusSrv;
}


sub _doWork {
    my $self = shift;
    my( $sieveSrv, $object ) = @_;

    if( !defined($sieveSrv->{"imap_sieve_server_conn"}) || !defined($object) ) {
        return 0;
    }

    # Récupération du nom de la boîte à traiter
    my $mailBoxName = $object->getMailboxName( "new" );
    if( !defined($mailBoxName) ) {
        return 0;
    }


    my $sieveScriptName = $mailBoxName.".sieve";
    $sieveScriptName =~ s/@/-/g;
    my $localSieveScriptName = $tmpOBM."/".$sieveScriptName;

    &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: mise a jour du script Sieve pour l'utilisateur : '".$mailBoxName."'", "W" );
    my $currentScriptString = "";
    sieve_get( $sieveSrv->{"imap_sieve_server_conn"}, $sieveScriptName, $currentScriptString );
    my @oldSieveScript;
    if( defined($currentScriptString) ) {
        @oldSieveScript = split( /\n/, $currentScriptString );
    }


    # On desactive l'ancien script
    my $sieveErrorCode;
    $sieveErrorCode = sieve_activate( $sieveSrv->{"imap_sieve_server_conn"}, "" );
    # On supprime l'ancien script
    $sieveErrorCode = sieve_delete( $sieveSrv->{"imap_sieve_server_conn"}, $sieveScriptName );

    # On génère le nouveau script Sieve
    my @newSieveScript;
    $self->_updateSieveScript( $object, \@oldSieveScript, \@newSieveScript );

    if( $#newSieveScript >= 0 ) {
        # On cree le script Sieve en local
        open( FIC, ">".$localSieveScriptName ) or return 1;
        print FIC @newSieveScript;
        close( FIC );

        # On installe le nouveau script
        if( sieve_put_file_withdest( $sieveSrv->{"imap_sieve_server_conn"}, $localSieveScriptName, $sieveScriptName ) ) {
            my $errstr = sieve_get_error( $sieveSrv->{"imap_sieve_server_conn"} );
            $errstr = "Sieve - erreur inconnue." if(!defined($errstr));
            &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: echec: lors du telechargement du script Sieve : ".$errstr , "W" );
            return 0;
        }

        &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: activation du script Sieve pour l'utilisateur '".$mailBoxName."'", "W" );

        # On active le nouveau script
        if( sieve_activate( $sieveSrv->{"imap_sieve_server_conn"}, $sieveScriptName ) ) {
            my $errstr = sieve_get_error( $sieveSrv->{"imap_sieve_server_conn"} );
            $errstr = "Sieve - erreur inconnue." if(!defined($errstr));
            &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: probleme lors de l'activation du script Sieve : ".$errstr, "W" );
            return 0;
        }

        # On supprime le script local
        unlink( $localSieveScriptName );
    }else {
        &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: suppression du script Sieve pour l'utilisateur '".$mailBoxName."'", "W" );
    }

    return 1;
}


sub _updateSieveScript {
    my $self = shift;
    my( $object, $oldSieveScript, $newSieveScript ) = @_;
    require OBM::Cyrus::utils;

    # Recuperation des en-tetes 'require' de l'ancien script
    my @headers;
    &OBM::Cyrus::utils::sieveGetHeaders( $oldSieveScript, \@headers );

    my @vacation;
    $self->_updateSieveVacation( $object, \@headers, $oldSieveScript, \@vacation );

    my @nomade;
    $self->_updateSieveNomade( $object, \@headers, $oldSieveScript, \@nomade );

    splice( @{$newSieveScript}, 0 );

    if( ( $#vacation < 0 ) && ( $#nomade < 0 ) && ($#{$oldSieveScript} < 0) ) {
        return 0;
    }

    push( @{$newSieveScript}, @headers );
    push( @{$newSieveScript}, @vacation );
    push( @{$newSieveScript}, @nomade );

    for( my $i=0; $i<=$#{$oldSieveScript}; $i++ ) {
        $oldSieveScript->[$i] .="\n";
    }
    push( @{$newSieveScript}, @{$oldSieveScript} );

    return 0;
}


sub _updateSieveVacation {
    my $self = shift;
    my( $object, $headers, $oldSieveScript, $newSieveScript ) = @_;
    my $vacationMark = "# OBM2 - Vacation";

    if( !defined($object) ) {
        return 0;
    }


    if( my $vacationMsg = $object->getSieveVacation() ) {
        # On verifie que l'en-tête necessaire soit bien placée
        my $i=0;
        while( ( $i<=$#{$headers} ) && ( $headers->[$i] !~ /[^#]*require \"vacation\";/) ) {
            $i++;
        }

        if( $i > $#{$headers} ) {
            unshift( @{$headers}, "require \"vacation\";\n" );
        }

        my $boxLogin = $object->getMailboxName( "new" );
        &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: gestion du message d'absence de la boite '".$boxLogin."'", "W" );

        push( @{$newSieveScript}, $vacationMark."\n" );
        push( @{$newSieveScript}, $vacationMsg );
        push( @{$newSieveScript}, $vacationMark."\n" );
    }

    # On supprime le vacation de l'ancien script
    &OBM::Cyrus::utils::sieveDeleteMark( $oldSieveScript, $vacationMark );

    return 0;
}


sub _updateSieveNomade {
    my $self = shift;
    my( $object, $headers, $oldSieveScript, $newSieveScript ) = @_;
    my $nomadeMark = "# OBM2 - Nomade";

    if( my $nomadeMsg = $object->getSieveNomade() ) {
        &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: gestion de la redirection de la boite '".$object->getMailboxName( "new" )."'", "W" );
        push( @{$newSieveScript}, $nomadeMark."\n" );
        push( @{$newSieveScript}, $nomadeMsg );
        push( @{$newSieveScript}, $nomadeMark."\n" );
    }

    # On supprime le nomade de l'ancien script
    &OBM::Cyrus::utils::sieveDeleteMark( $oldSieveScript, $nomadeMark );

    return 0;
}


sub update {
    my $self = shift;
    my( $object ) = @_;

    if( !defined($object) ) {
        &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: mise a jour d'un objet non definit - Operation annulee !", "W" );
        return 0;
    }elsif( !defined($object->{"type"}) ) {
        &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: mise a jour d'un objet de type non définit - Operation annulee !", "W" );
        return 0;
    }elsif( !defined($object->{"domainId"}) ) {
        &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: mise a jour d'un objet de domaine non definit - Operation annulee !", "W" );
        return 0;
    }

    # Si l'objet n'a pas le droit mail, on ne fait rien
    if( !$object->isMailActive() ) {
        &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: droit mail desactive pour l'objet : ".$object->getEntityDescription(), "W" );
        return 1;
    }

    # Si cette entité n'a pas d'interaction avec Sieve
    if( !$object->getMailboxSieve() ) {
        &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: pas de compte SIEVE definit pour l'objet : ".$object->getEntityDescription(), 'W' );
        return 1;
    }

    # Récupération du nom de la boîte à traiter
    my $mailBoxName = $object->getMailboxName( "new" );
    if( !defined($mailBoxName) ) {
        &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: pas de support SIEVE pour l'objet : ".$object->getEntityDescription(), 'W' );
        return 1;
    }

    # Si l'objet est marqué à effacer, on ne fait rien
    if( $object->getDelete() ) {
        &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: suppression de l'objet : ".$object->getEntityDescription(), "W" );
        return 1;
    }

    # Récupération de la description du serveur de la boîte à traiter
    my $mailServerId = $object->getMailServerId();
    if( !defined($mailServerId) ) {
        &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: serveur SIEVE non definit - Operation annulee !", "W" );
        return 1;
    }

    my $sieveSrv = $self->_findCyrusSrvbyId( $object->{"domainId"}, $mailServerId );
    if( !defined($sieveSrv) ) {
        &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: serveur SIEVE d'identifiant '".$mailServerId."' inconnu - Operation annulee !", "W" );
        return 0;
    }

    # Connexion au serveur
    if( !$self->_connectSrvSieve( $sieveSrv, $mailBoxName ) ) {
        &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: probleme de connexion au serveur SIEVE d'identifiant '".$mailServerId."' - Operation annulee !", "W" );
        return 0;
    }

    # Traitement
    if( !$self->_doWork( $sieveSrv, $object ) ) {
        &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: probleme de traitement de l'objet : ".$object->getEntityDescription()."' - Operation annulee !", "W" );

        # On se deconnecte de serveur SIEVE
        $self->_disconnectSrvSieve($sieveSrv);
        return 0;
    }

    # Deconnexion du serveur
    if( !$self->_disconnectSrvSieve($sieveSrv) ) {
        &OBM::toolBox::write_log( "[Cyrus::sieveEngine]: probleme de deconnexion du serveur SIEVE d'identifiant '".$mailServerId."' - Operation annulee !", "W" );
        return 0;
    }

    return 1;
}
