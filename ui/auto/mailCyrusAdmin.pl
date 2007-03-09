#!/usr/bin/perl -w
###############################################################################
# OBM       - File : mailCyrusAdmin.pl                                        #
#           - Desc : script d'amdinistration du serveur Cyrus IMAP et SIEVE   #
###############################################################################
# Creer le 2002-07-30                                                         #
# $Id$                 #
###############################################################################
# Code retour :                                                               #
#       - 0 : tout est ok                                                     #
#       - 1 : probleme avec la connexion a la base de donnees                 #
###############################################################################


use strict;
require OBM::imapd;
require OBM::toolBox;
require OBM::dbUtils;
use OBM::Parameters::common;

$ENV{PATH}=$automateOBM;
delete @ENV{qw(IFS CDPATH ENV BASH_ENV)};


sub exitScript {
    my( $state, $dbHandler ) = @_;

    if( $state !~ /^[0-9]+$/ ) {
        $state = 1;
    }

    if( defined($dbHandler) ) {
        # On referme la connexion a la base
        &OBM::toolBox::write_log( "Deconnexion de la base de donnees OBM", "W" );
        if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
            &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "WC" );
        }
    }

    # On ferme la connection avec Syslog
    &OBM::toolBox::write_log( "", "C" );

    # On termine proprement
    exit $state;
}

###############################################################################
#                             Debut du main                                   #
###############################################################################
#
# On ouvre un connection avec Syslog
&OBM::toolBox::write_log( "mailCyrusAdmin: ", "O" );


# On se connecte a la base
my $dbHandler;
&OBM::toolBox::write_log( "Connexion a la base de donnees OBM", "W" );
if( !&OBM::dbUtils::dbState( "connect", \$dbHandler ) ) {
    &OBM::toolBox::write_log( "Probleme lors de l'ouverture de la base de donnee : ".$dbHandler->err, "WC" );
    exit 1;
}

# Recuperation des domaines a traiter
&OBM::toolBox::write_log( "Recuperation de la liste des domaines a traiter", "W" );
my $domainList = &OBM::toolBox::getDomains( $dbHandler );

# Récupération des serveurs de courrier par domaine
my $listDomainSrv = &OBM::imapd::getServerByDomain( $dbHandler, $domainList );

# Recuperation du mot de passe de l'administrateur IMAP 
&OBM::toolBox::write_log( "Recuperation des informations de l'administrateur.", "W" );
if( !&OBM::imapd::getAdminImapPasswd( $dbHandler, $listDomainSrv ) ) {
    exitScript( "1", $dbHandler );
}

# Chargement des informations depuis la BD OBM
&OBM::imapd::loadBdValues( $dbHandler, $listDomainSrv );

# Traitement domaine/domaine - serveur/serveur
&OBM::imapd::updateServers( $listDomainSrv );

exitScript( "0", $dbHandler );
