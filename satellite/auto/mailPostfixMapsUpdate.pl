#!/usr/bin/perl -w -T

require 5.003;
use strict;
require OBM::imapd;
require OBM::toolBox;
use OBM::Parameters::common;
use Net::Telnet;

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};


sub updateServer {
    my( $srv, $cmd ) = @_;

    if( !defined($cmd) || ($cmd eq "") ) {
        return 1;
    }

    &OBM::toolBox::write_log( "Connexion au serveur : '".$srv."'", "W" );
    my $srvCon = new Net::Telnet(
        Host => $srv,
        Port => 30000,
        Timeout => 60,
        errmode => "return"
    );
    
    if( !defined($srvCon) || !$srvCon->open() ) {
        &OBM::toolBox::write_log( "Echec : lors de la connexion au serveur : ".$srv, "W" );
        return 1;
    }
    while( (!$srvCon->eof()) && (my $line = $srvCon->getline(Timeout => 1)) ) {
        chomp($line);
        &OBM::toolBox::write_log( "Reponse : '".$line."'", "W" );
    }

    

    &OBM::toolBox::write_log( "Envoi de la commande : '".$cmd."'", "W" );
    $srvCon->print( $cmd );
    if( (!$srvCon->eof()) && (my $line = $srvCon->getline()) ) {
        chomp($line);
        &OBM::toolBox::write_log( "Reponse : '".$line."'", "W" );
    }

    &OBM::toolBox::write_log( "Deconnexion du serveur : '".$srv."'", "W" );
    $srvCon->print( "quit" );
    while( !$srvCon->eof() && (my $line = $srvCon->getline(Timeout => 1)) ) {
        chomp($line);
        &OBM::toolBox::write_log( "Reponse : '".$line."'", "W" );
    }

    return 0;
}


# On prepare le log
&OBM::toolBox::write_log( "mailPostfixMapsUpdate: ", "O" );


# On se connecte a la base
my $dbHandler;
&OBM::toolBox::write_log( "Connexion a la base de donnees OBM", "W" );
if( !&OBM::dbUtils::dbState( "connect", \$dbHandler ) ) {
    &OBM::toolBox::write_log( "Probleme lors de l'ouverture de la base de donnee : ".$dbHandler->err, "WC" );
    exit 1;
}

# Obtention de la liste des serveurs SMTP
my $query = "SELECT i.host_name, i.host_ip FROM Host i, MailServer j WHERE i.host_id=j.mailserver_host_id";

# On execute la requete
my $queryResult;
if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
    &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
    return 0;
}

while( my( $serverName, $serverIp ) = $queryResult->fetchrow_array() ) {
    if( !defined($serverName) || !defined($serverIp) ) {
        next;
    }

    updateServer( $serverIp, "postfixMaps: ".$serverName );
}

# Deconnexion de la BD
&OBM::toolBox::write_log( "Deconnexion de la base de donnees OBM", "W" );
if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
    &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "W" );
}


# Fin de MAJ des MTA
&OBM::toolBox::write_log( "Fin de mise a jour des MTA", "W" );

# On ferme la connection avec Syslog
&OBM::toolBox::write_log( "", "C" );

exit 0;
