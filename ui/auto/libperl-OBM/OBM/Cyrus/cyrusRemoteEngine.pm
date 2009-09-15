package OBM::Cyrus::cyrusRemoteEngine;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use base qw( Class::Singleton );
use OBM::Tools::commonMethods qw(
        _log
        dump
        );
require Sys::Syslog;
use Net::Telnet;


sub _new_instance {
    my $class = shift;

    my $self = bless { }, $class;

    require OBM::Parameters::common;
    if( !$OBM::Parameters::common::obmModules->{'mail'} ) {
        $self->_log( 'module OBM-MAIL désactivé, gestionnaire Cyrus distant non démarré', 3 );
        return undef;
    }

    # Setting obmSatellite TCP port
    $self->{'obmSatellitePort'} = '30000';

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub addCyrusPartition {
    my $self = shift;
    my( $cyrusSrv ) = @_;

    if( !$OBM::Parameters::common::cyrusDomainPartition ) {
        # Cyrus partition support disable
        return 0;
    }

    if( !defined($cyrusSrv) ) {
        $self->_log( 'serveur Cyrus non défini, ajout de partition impossible', 3 );
        return 1;
    }elsif( ref($cyrusSrv) ne 'OBM::Cyrus::cyrusServer' ) {
        $self->_log( 'description du serveur Cyrus incorrecte, ajout de partition impossible', 3 );
        return 1;
    }

    my $cyrusSrvIp = $cyrusSrv->getCyrusServerIp();
    if( !$cyrusSrvIp ) {
        $self->_log( 'adresse IP du serveur Cyrus incorrecte, ajout de partition impossible', 3 );
        return 1;
    }

    my $cyrusSrvName = $cyrusSrv->getCyrusServerName();
    if( !$cyrusSrvName ) {
        $self->_log( 'nom du serveur Cyrus incorrect, ajout de partition impossible', 3 );
        return 1;
    }

    $self->_log( 'connexion à obmSatellite de '.$cyrusSrv->getDescription(), 2 );

    require OBM::ObmSatellite::client;
    my $obmSatelliteClient = OBM::ObmSatellite::client->instance();
    if( !defined($obmSatelliteClient) ) {
        $self->_log( 'Echec lors de l\'initialisation du client obmSatellite', 3  );
        return 1;
    }

    return $obmSatelliteClient->post( $cyrusSrvIp, '/cyruspartition/host/add/'.$cyrusSrvName );
}


sub delCyrusPartition {
    my $self = shift;
    my( $cyrusSrv ) = @_;

    if( !$OBM::Parameters::common::cyrusDomainPartition ) {
        # Cyrus partition support disable
        return 0;
    }

    if( !defined($cyrusSrv) ) {
        $self->_log( 'serveur Cyrus non défini, suppression de partition impossible', 3 );
        return 1;
    }elsif( ref($cyrusSrv) ne 'OBM::Cyrus::cyrusServer' ) {
        $self->_log( 'description du serveur Cyrus incorrecte, suppression de partition impossible', 3 );
        return 1;
    }

    my $cyrusSrvIp = $cyrusSrv->getCyrusServerIp();
    if( !$cyrusSrvIp ) {
        $self->_log( 'adresse IP du serveur Cyrus incorrecte, suppression de partition impossible', 3 );
        return 1;
    }

    $self->_log( 'connexion à obmSatellite de '.$cyrusSrv->getDescription(), 2 );
    my $srvCon = new Net::Telnet(
        Host => $cyrusSrvIp,
        Port => $self->{'obmSatellitePort'},
        Timeout => 60,
        errmode => 'return'
    );

    if( !defined($srvCon) ) {
        $self->_log( 'problème à l\'initialisation de la connexion à obmSatellite de '.$cyrusSrv->getDescription(), 0 );
        return 1;
    }

    if( !$srvCon->open() ) {
        $self->_log( 'echec de connexion à obmSatellite de '.$cyrusSrv->getDescription().' : '.$srvCon->errmsg(), 0 );
        return 1;
    }

    while( (!$srvCon->eof()) && (my $line = $srvCon->getline(Timeout => 5)) ) {
        chomp($line);
        $self->_log( 'réponse : \''.$line.'\'', 4 );
    }

    my $cmd = 'cyrusPartitions: add:'.$cyrusSrv->getCyrusServerName();
    my $errorCode = 0;
    $self->_log( 'envoi de la commande : '.$cmd, 2 );
    $srvCon->print( $cmd );
    if( (!$srvCon->eof()) && (my $line = $srvCon->getline(Timeout => 60)) ) {
        chomp($line);
        $self->_log( 'réponse : \''.$line.'\'', 4 );
        if( $line !~ /^ok$/i ) {
            $self->_log( 'problème lors de l\'exécution de la commande '.$cmd, 0 );
            $errorCode = 1;
        }
    }

    $self->_log( 'déconnexion d\'obmSatellite de '.$cyrusSrv->getDescription(), 2 );
    $srvCon->print( 'quit' );
    while( !$srvCon->eof() && (my $line = $srvCon->getline(Timeout => 5)) ) {
        chomp($line);
        $self->_log( 'réponse : \''.$line.'\'', 4 );
    }


    return $errorCode;
}
