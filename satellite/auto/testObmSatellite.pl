#!/usr/bin/perl -w -T

require 5.003;
require OBM::toolBox;
use Net::Telnet;
use Getopt::Long;
use strict;

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};


# fonction de verification des parametres du script
sub getParameter {
    my( $parameters ) = @_;

    # Analyse de la ligne de commande
    &GetOptions( $parameters, "smtpInConf", "cyrusParitionsAdd", "cyrusParitionsDel", "help" );

    my $goodParams = 0;
    my $helpParam = 0;
    while( my( $paramName, $paramValue ) = each(%{$parameters}) ) {
        SWITCH: {
            if( $paramName eq "smtpInConf" ) {
                &OBM::toolBox::write_log( "Mise a jour des tables Postfix", "W" );
                $goodParams++;
                last SWITCH;
            }

            if( $paramName eq "cyrusParitionsAdd" ) {
                &OBM::toolBox::write_log( "Mise a jour (ajout) des partitions Cyrus", "W" );
                $goodParams++;
                last SWITCH;
            }

            if( $paramName eq "cyrusParitionsDel" ) {
                &OBM::toolBox::write_log( "Mise a jour (suppression) des partitions Cyrus", "W" );
                $goodParams++;
                last SWITCH;
            }

            if( $paramName eq "help" ) {
                &OBM::toolBox::write_log( "Affichage de l'aide", "W" );
                $helpParam = 1;
                last SWITCH;
            }
        }
    }

    # Affichage de l'aide
    if( !$goodParams || $helpParam ) {
        print "Vous devez indiquer au moins un des paramètres suivants :\n";
        print "\tsmtpInConf: permet de régénérer les tables Postfix\n";
        print "\tcyrusParitionsAdd: permet d'ajouter les partitions Cyrus manquantes - Provoque un redémarrage du/des services Cyrus !\n";
        print "\tcyrusParitionsDel: permet de supprimer les partitions Cyrus non déclarées - Provoque un redémarrage du/des services Cyrus !\n\n";
        return 0;
    }

    return 1;
}


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
&OBM::toolBox::write_log( "testObmSatellite: ", "O" );

# Vérification des paramètres du script
&OBM::toolBox::write_log( "Analyse des parametres du script", "W" );
my %parameters;
if( !getParameter( \%parameters ) ) {
    &OBM::toolBox::write_log( "Affichage de l'aide ou mauvais parmetres...", "WC" );
    exit 1;
}

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
    &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete : ".$dbHandler->err, "WC" );
    exit 1;
}

while( my( $serverName, $serverIp ) = $queryResult->fetchrow_array() ) {
    if( !defined($serverName) || !defined($serverIp) ) {
        next;
    }

    while( my( $paramName, $paramValue ) = each(%parameters) ) {
        my $cmd = undef;

        SWITCH: {
            if( $paramName eq "smtpInConf" ) {
                &OBM::toolBox::write_log( "Mise a jour des tables Postfix", "W" );
                $cmd = "smtpInConf: ".$serverName;
                last SWITCH;
            }

            if( $paramName eq "cyrusParitionsAdd" ) {
                &OBM::toolBox::write_log( "Mise a jour des partitions Cyrus - Ajout", "W" );
                $cmd = "cyrusPartitions: add:".$serverName;
                last SWITCH;
            }

            if( $paramName eq "cyrusParitionsDel" ) {
                &OBM::toolBox::write_log( "Mise a jour des partitions Cyrus - Suppression", "W" );
                $cmd = "cyrusPartitions: del:".$serverName;
                last SWITCH;
            }
        }

        if( defined($cmd) ) {
            updateServer( $serverIp, $cmd );
        }
    }
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
