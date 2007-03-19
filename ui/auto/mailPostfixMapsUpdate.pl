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

    

    &OBM::toolBox::write_log( "Envoie de la commande : '".$cmd."'", "W" );
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

# Recuperation des domaines a traiter
&OBM::toolBox::write_log( "Recuperation de la liste des domaines", "W" );
my $domainList = &OBM::toolBox::getDomains( $dbHandler, undef );

# Récupération des serveurs de courrier par domaine
&OBM::imapd::getServerByDomain( $dbHandler, $domainList );

# Deconnexion de la BD
&OBM::toolBox::write_log( "Deconnexion de la base de donnees OBM", "W" );
if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
    &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "W" );
}

# Obtention de la liste des domaines sous forme de chaine de caractère
my $domainString;
for( my $i=0; $i<=$#{$domainList}; $i++ ) {
    if( $domainList->[$i]->{"meta_domain"} ) {
        next;
    }
    
    if( defined($domainString) ) {
        $domainString .= ":";
    }

    $domainString .= $domainList->[$i]->{"domain_label"};
}

# MAJ des MTA
&OBM::toolBox::write_log( "Mise a jour des MTA", "W" );

for( my $i=0; $i<=$#{$domainList}; $i++ ) {
    my $currentDomain = $domainList->[$i];
    my %serverOk;

    if( !exists($currentDomain->{imap_servers}) ) {
        next;
    }

    for( my $j=0; $j<=$#{$currentDomain->{imap_servers}}; $j++ ) {
        my $currentServer = $currentDomain->{imap_servers}->[$j];
        &OBM::toolBox::write_log( "Traitement du serveur '".$currentServer->{imap_server_name}."'", "W" );
        if( !exists($serverOk{$currentServer->{imap_server_name}}) && !updateServer( $currentServer->{imap_server_ip}, "domains: ".$domainString ) ) {
            $serverOk{$currentServer->{imap_server_name}} = 1;
        }
    }
}

# Fin de MAJ des MTA
&OBM::toolBox::write_log( "Fin de mise a jour des MTA", "W" );

# On ferme la connection avec Syslog
&OBM::toolBox::write_log( "", "C" );

exit 0;
