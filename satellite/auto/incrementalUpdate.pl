#!/usr/bin/perl -w -T
#####################################################################
# OBM               - File : incrementalUpdate.pl                   #
#                   - Desc : Script permettant de mettre à jour le  #
#                   système de façon incrémentale                   #
#-------------------------------------------------------------------#
# $Id: ldapChangePasswd.pl 1719 2007-05-22 14:14:13Z anthony $
#-------------------------------------------------------------------#

use strict;
require OBM::loadDb;
use Getopt::Long;

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};


# Fonction de verification des parametres du script
sub getParameter {
    my( $parameters ) = @_;

    # Analyse de la ligne de commande
    &GetOptions( $parameters, "user=s", "domain=s", "delegation=s", "global", "incremental", "help" );


    if( !exists($parameters->{"domain"}) ) {
        &OBM::toolBox::write_log( "Parametre '--domain' manquant", "W" );
        $parameters->{"help"} = "";

    }
    
    if( exists($parameters->{"user"}) ) {
        if( exists($parameters->{"delegation"}) ) {
            &OBM::toolBox::write_log( "Trop de parametres de mise a jour precise", "W" );
            $parameters->{"help"} = "";
        }

    }elsif( exists($parameters->{"delegation"}) ) {
        if( exists($parameters->{"user"}) ) {
            &OBM::toolBox::write_log( "Trop de parametres de mise a jour precise", "W" );
            $parameters->{"help"} = "";
        }

    }


    if( exists( $parameters->{"help"} ) ) {
        &OBM::toolBox::write_log( "Affichage de l'aide", "WC" );

        print "Veuillez indiquer le critere de mise a jour :\n";
        print "\tSyntaxe: script --domain id [--user id | --delegation word] [--global]\n";
        print "\tuser <id> : utilisateur d'identifiant <id> ;\n";
        print "\tdomain <id> : domaine d'identifiant <id> ;\n";
        print "\tdelegation <word> : delegation de mot cle <word>.\n";
        print "\tglobal : fait une mise a jour globale du domaine.\n";

        exit 0;
    }
}


# On prepare le log
&OBM::toolBox::write_log( "incrementalUpdate.pl: ", "O" );

# Traitement des parametres
&OBM::toolBox::write_log( "Analyse des parametres du script", "W" );
my %parameters;
getParameter( \%parameters );

# On se connecte a la base
my $dbHandler;
if( !&OBM::dbUtils::dbState( "connect", \$dbHandler ) ) {
    if( defined($dbHandler) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'ouverture de la base de donnee : ".$dbHandler->err, "WC" );
    }else {
        &OBM::toolBox::write_log( "Probleme lors de l'ouverture de la base de donnee : erreur inconnue", "WC" );
    }

    exit 1;
}


my $loadDb = OBM::loadDb->new( $dbHandler, \%parameters );
$loadDb->update();
$loadDb->destroy();


#
# On referme la connexion a la base
if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
    &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "W" );
}

#
# On ferme le log
&OBM::toolBox::write_log( "", "C" );

exit 0
