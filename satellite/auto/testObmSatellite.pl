#!/usr/bin/perl -w -T

#+-------------------------------------------------------------------------+
#|   Copyright (c) 1997-2009 OBM.org project members team                  |
#|                                                                         |
#|  This program is free software; you can redistribute it and/or          |
#|  modify it under the terms of the GNU General Public License            |
#|  as published by the Free Software Foundation; version 2                |
#|  of the License.                                                        |
#|                                                                         |
#|  This program is distributed in the hope that it will be useful,        |
#|  but WITHOUT ANY WARRANTY; without even the implied warranty of         |
#|  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          |
#|  GNU General Public License for more details.                           | 
#+-------------------------------------------------------------------------+
#|  http://www.obm.org                                                     |
#+-------------------------------------------------------------------------+


require 5.003;
require OBM::Tools::obmDbHandler;
use Net::Telnet;
use Getopt::Long;
use strict;
use OBM::Tools::commonMethods qw(_log dump);

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};


# fonction de verification des parametres du script
sub getParameter {
    my( $parameters ) = @_;

    # Analyse de la ligne de commande
    &GetOptions( $parameters, "smtpInConf", "smtpOutConf", "cyrusPartitionsAdd", "cyrusPartitionsDel", "help" );

    my $goodParams = 0;
    my $helpParam = 0;
    while( my( $paramName, $paramValue ) = each(%{$parameters}) ) {
        SWITCH: {
            if( $paramName eq "smtpInConf" ) {
                _log( "Mise a jour des tables Postfix des serveurs SMTP-in", "W" );
                $goodParams++;
                last SWITCH;
            }

            if( $paramName eq "smtpOutConf" ) {
                _log( "Mise a jour des tables Postfix des serveurs SMTP-out", "W" );
                $goodParams++;
                last SWITCH;
            }

            if( $paramName eq "cyrusPartitionsAdd" ) {
                _log( "Mise a jour (ajout) des partitions Cyrus", "W" );
                $goodParams++;
                last SWITCH;
            }

            if( $paramName eq "cyrusPartitionsDel" ) {
                _log( "Mise a jour (suppression) des partitions Cyrus", "W" );
                $goodParams++;
                last SWITCH;
            }

            if( $paramName eq "help" ) {
                _log( "Affichage de l'aide", "W" );
                $helpParam = 1;
                last SWITCH;
            }
        }
    }

    # Affichage de l'aide
    if( !$goodParams || $helpParam ) {
        print "Vous devez indiquer au moins un des paramètres suivants :\n";
        print "\tsmtpInConf: permet de régénérer les tables Postfix des serveurs SMTP-in\n";
        print "\tsmtpOutConf: permet de régénérer les tables Postfix des serveurs SMTP-out\n";
        print "\tcyrusPartitionsAdd: permet d'ajouter les partitions Cyrus manquantes - Provoque un redémarrage du/des services Cyrus !\n";
        print "\tcyrusPartitionsDel: permet de supprimer les partitions Cyrus non déclarées - Provoque un redémarrage du/des services Cyrus !\n\n";
        return 0;
    }

    return 1;
}


sub updateServer {
    my( $srv, $cmd ) = @_;

    if( !defined($cmd) || ($cmd eq "") ) {
        return 1;
    }

    _log( "Connexion au serveur : '".$srv."'", "W" );
    my $srvCon = new Net::Telnet(
        Host => $srv,
        Port => 30000,
        Timeout => 60,
        errmode => "return"
    );
    
    if( !defined($srvCon) || !$srvCon->open() ) {
        _log( "Echec : lors de la connexion au serveur : ".$srv, "W" );
        return 1;
    }
    while( (!$srvCon->eof()) && (my $line = $srvCon->getline(Timeout => 1)) ) {
        chomp($line);
        _log( "Reponse : '".$line."'", "W" );
    }


    _log( "Envoi de la commande : '".$cmd."'", "W" );
    $srvCon->print( $cmd );
    if( (!$srvCon->eof()) && (my $line = $srvCon->getline()) ) {
        chomp($line);
        _log( "Reponse : '".$line."'", "W" );
    }

    _log( "Deconnexion du serveur : '".$srv."'", "W" );
    $srvCon->print( "quit" );
    while( !$srvCon->eof() && (my $line = $srvCon->getline(Timeout => 1)) ) {
        chomp($line);
        _log( "Reponse : '".$line."'", "W" );
    }

    return 0;
}


# On prepare le log
my ($scriptname) = ($0=~'.*/([^/]+)');

# Vérification des paramètres du script
_log( "Analyse des parametres du script", "W", 3 );
my %parameters;
if( !getParameter( \%parameters ) ) {
    _log( "Affichage de l'aide ou mauvais parametres...", "WC", 0 );
    exit 1;
}

# On se connecte a la base
my $dbHandler = OBM::Tools::obmDbHandler->instance();
if( !defined($dbHandler) ) {
    _log( 'Probleme lors de l\'ouverture de la base de donnees', 'WC', 0 );
    exit 1;
}

# Obtention de la liste des serveurs SMTP
my $query = "SELECT i.host_name, i.host_ip, j.mailserver_imap, j.mailserver_smtp_in, j.mailserver_smtp_out FROM Host i, MailServer j WHERE i.host_id=j.mailserver_host_id";

# On execute la requete
my $queryResult;
if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
    exit 1;
}

while( my( $serverName, $serverIp, $imapSrv, $smtpInSrv, $smtpOutSrv ) = $queryResult->fetchrow_array() ) {
    if( !defined($serverName) || !defined($serverIp) ) {
        next;
    }

    while( my( $paramName, $paramValue ) = each(%parameters) ) {
        my $cmd = undef;

        SWITCH: {
            if( $smtpInSrv && ($paramName eq "smtpInConf") ) {
                _log( "Mise a jour des tables Postfix des serveurs SMTP-in", "W" );
                $cmd = "smtpInConf: ".$serverName;
                last SWITCH;
            }

            if( $smtpOutSrv && ($paramName eq "smtpOutConf") ) {
                _log( "Mise a jour des tables Postfix des serveurs SMTP-out", "W" );
                $cmd = "smtpOutConf: ".$serverName;
                last SWITCH;
            }

            if( $imapSrv && ($paramName eq "cyrusPartitionsAdd") ) {
                _log( "Mise a jour des partitions Cyrus - Ajout", "W" );
                $cmd = "cyrusPartitions: add:".$serverName;
                last SWITCH;
            }

            if( $imapSrv && ($paramName eq "cyrusPartitionsDel") ) {
                _log( "Mise a jour des partitions Cyrus - Suppression", "W" );
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
$dbHandler->destroy();

# Fin de MAJ des MTA
_log( "Fin de mise a jour d'ObmSatellite", "W" );

exit 0;
