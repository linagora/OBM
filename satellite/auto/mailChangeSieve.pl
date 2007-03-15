#!/usr/bin/perl -w -T
#####################################################################
# Aliamin           - File : mailChangeSieve.pl                     #
#                   - Desc : Script permettant de gerer le filtre   #
#                   sieve d'un utilisateur                          #
#                   - Parametres :                                  #
#                       uid : login de l'utilisateur a traiter      #
#####################################################################
# Cree le 2005-07-21                                                #
# $Id$   #
#####################################################################
# Retour :                                                          #
#    - 0 : tout c'est bien passe                                    #
#    - 1 : probleme de connexion a la base                          #
#    - 2 : probleme d'ouverture du fichier des alias actuels        #
#    - 3 : probleme de parametres                                   #
#####################################################################

use strict;
require OBM::imapd;
require OBM::toolBox;
use OBM::Parameters::common;
use Getopt::Long;

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};


# Fonction de verification des parametres du script
sub getParameter {
    my( $parameters ) = @_;

    # Analyse de la ligne de commande
    &GetOptions( $parameters, "login=s", "domain=s" );

    # Verification de l'identifiant utilisateur
    if( !exists($$parameters{"login"}) ) {
        &OBM::toolBox::write_log( "Parametre --login manquant", "W" );
        return 1;

    }else {
        if( $$parameters{"login"} !~ /$regexp_login/ ) {
            &OBM::toolBox::write_log( "Parametre --login invalide", "W" );
            return 1;
        }
    }

    # Verification du domaine
    if( !exists($$parameters{"domain"}) ) {
        &OBM::toolBox::write_log( "Parametre --domain manquant", "W" );
        return 1;
    }else {
        if( $$parameters{"domain"} !~ /^[0-9]+$/ ) {
            &OBM::toolBox::write_log( "Parametre --domain invalide", "W" );
            return 1;
        }
    }

    return 0;
}


sub exitScript {
    my( $state, $dbHandler ) = @_;

    if( $state !~ /^[0-9]+$/ ) {
        $state = 1;
    }

    if( defined($dbHandler) ) {
        # On referme la connexion a la base
        &OBM::toolBox::write_log( "Deconnexion de la base de donnees OBM", "W" );
        if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
            &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "W" );
        }
    }

    # On ferme la connection avec Syslog
    &OBM::toolBox::write_log( "", "C" );

    # On termine proprement
    exit $state;
}


# On prepare le log
&OBM::toolBox::write_log( "MailChangeSieve: ", "O" );

# Traitement des parametres
&OBM::toolBox::write_log( "Analyse des parametres du script", "W" );
my %parameters;
if( getParameter( \%parameters ) ) {
    &OBM::toolBox::write_log( "", "C" );
    exit 1;
}

# On se connecte a la base
my $dbHandler;
&OBM::toolBox::write_log( "Connexion a la base de donnees OBM", "W" );
if( !&OBM::dbUtils::dbState( "connect", \$dbHandler ) ) {
    &OBM::toolBox::write_log( "Probleme lors de l'ouverture de la base de donnee : ".$dbHandler->err, "WC" );
    exit 1;
}

# Recuperation des domaines a traiter
&OBM::toolBox::write_log( "Recuperation de la liste des domaines", "W" );
my $domainList = &OBM::toolBox::getDomains( $dbHandler, $parameters{"domain"} );

# Récupération des serveurs de courrier par domaine
&OBM::imapd::getServerByDomain( $dbHandler, $domainList );

# Recuperation du mot de passe de l'administrateur IMAP 
&OBM::toolBox::write_log( "Recuperation des informations de l'administrateur.", "W" );
if( !&OBM::imapd::getAdminImapPasswd( $dbHandler, $domainList ) ) {
    exitScript( "1", $dbHandler );
}


my $i=0;
while( ( $i<=$#{$domainList} ) && ( $domainList->[$i]->{"domain_id"}!=$parameters{"domain"} ) ) {
    $i++;
}

if( $i > $#{$domainList} ) {
    &OBM::toolBox::write_log( "Echec: domaine inconnu", "W" );
    exitScript( "2", $dbHandler );
}

&OBM::toolBox::write_log( "Mise a jour du message d'absence de l'utilisateur '".$parameters{"login"}."', du domaine '".$domainList->[$i]->{"domain_label"}."'", "W" );

if( &OBM::imapd::updateSieve( $dbHandler, $domainList->[$i], $parameters{"login"} ) ) {
    &OBM::toolBox::write_log( "Echec: probleme lors de la mise a jour du message d'absence", "W" );
    exitScript( "3", $dbHandler );
}

    &OBM::toolBox::write_log( "Mise a jour du message d'absence reussie", "W" );


# Tout s'est bien passe
exitScript( "0", $dbHandler );
