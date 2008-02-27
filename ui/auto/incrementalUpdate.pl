#!/usr/bin/perl -w -T
#####################################################################
# OBM               - File : incrementalUpdate.pl                   #
#                   - Desc : Script permettant de mettre à jour le  #
#                   système de façon incrémentale                   #
#-------------------------------------------------------------------#

use strict;
require OBM::Update::update;
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

    }else {
        &OBM::toolBox::write_log( "Mise a jour du domaine d'identifiant '".$parameters->{"domain"}."'", "W" );
    }
    
    if( exists($parameters->{"user"}) ) {
        if( exists($parameters->{"delegation"}) ) {
            &OBM::toolBox::write_log( "Trop de parametres de mise a jour precise", "W" );
            $parameters->{"help"} = "";
        }else{
            &OBM::toolBox::write_log( "Uniquement les mises a jour de l'utilisateur d'identifiant '".$parameters->{"user"}."'", "W" );
        }

    }elsif( exists($parameters->{"delegation"}) ) {
        if( exists($parameters->{"user"}) ) {
            &OBM::toolBox::write_log( "Trop de parametres de mise a jour precise", "W" );
            $parameters->{"help"} = "";
        }else {
            &OBM::toolBox::write_log( "Uniquement les mises a jour de la delegation '".$parameters->{"delegation"}."'", "W" );
        }

    }

    if( exists($parameters->{"incremental"}) && exists($parameters->{"global"}) ) {
        &OBM::toolBox::write_log( "parametres '--incremental' et '--global' incompatibles", "W" );
        $parameters->{"help"} = "";

    }elsif( exists($parameters->{"incremental"}) ) {
        &OBM::toolBox::write_log( "Mise a jour incrementale", "W" );

    }elsif( exists($parameters->{"global"}) ) {
        &OBM::toolBox::write_log( "Mise a jour globale", "W" );
    }


    if( exists( $parameters->{"help"} ) ) {
        &OBM::toolBox::write_log( "Affichage de l'aide", "WC" );

        print STDERR "Veuillez indiquer le critere de mise a jour :\n";
        print STDERR "\tSyntaxe: script --domain id [--user id | --delegation word] [--global | --incremental]\n";
        print STDERR "\tuser <id> : utilisateur d'identifiant <id> ;\n";
        print STDERR "\tdomain <id> : domaine d'identifiant <id> ;\n";
        print STDERR "\tdelegation <word> : delegation de mot cle <word> ;\n";
        print STDERR "\tglobal : fait une mise a jour globale du domaine ;\n";
        print STDERR "\tincremental : fait une mise a jour incrementale du domaine.\n";

        exit 0;
    }
}


# On prepare le log
&OBM::toolBox::write_log( "incrementalUpdate.pl: ", "O" );

# Traitement des paramètres
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


my $update = OBM::Update::update->new( $dbHandler, \%parameters );
$update->update();
$update->destroy();


# On referme la connexion a la base
if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
    &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "W" );
}

# On ferme le log
&OBM::toolBox::write_log( "Fin du traitement", "W" );
&OBM::toolBox::write_log( "", "C" );

exit 0
