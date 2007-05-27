#!/usr/bin/perl -w
#####################################################################
# OBM               - File : incrementalUpdate.pl                   #
#                   - Desc : Script permettant de mettre à jour le  #
#                   système de façon incrémentale                   #
#-------------------------------------------------------------------#
# $Id: ldapChangePasswd.pl 1719 2007-05-22 14:14:13Z anthony $
#-------------------------------------------------------------------#

use strict;
require OBM::toolBox;
require OBM::loadDbIncremental;
require OBM::Ldap::ldapEngine;
require OBM::Ldap::posixUser;
use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
use Getopt::Long;

$ENV{PATH}=$automateOBM;
delete @ENV{qw(IFS CDPATH ENV BASH_ENV)};


# Fonction de verification des parametres du script
sub getParameter {
    my( $parameters ) = @_;

    # Analyse de la ligne de commande
    &GetOptions( $parameters, "user=s", "domain=s", "delegation=s", "help" );

    if( exists($parameters->{"user"}) ) {
        if( exists($parameters->{"domain"}) || exists($parameters->{"delegation"}) ) {
            &OBM::toolBox::write_log( "Trop de parametres de mise a jour incrementale precise", "W" );
            $parameters->{"help"} = "";
        }

    }elsif( exists($parameters->{"domain"}) ) {
        if( exists($parameters->{"user"}) || exists($parameters->{"delegation"}) ) {
            &OBM::toolBox::write_log( "Trop de parametres de mise a jour incrementale precise", "W" );
            $parameters->{"help"} = "";
        }

    }elsif( exists($parameters->{"delegation"}) ) {
        if( exists($parameters->{"domain"}) || exists($parameters->{"user"}) ) {
            &OBM::toolBox::write_log( "Trop de parametres de mise a jour incrementale precise", "W" );
            $parameters->{"help"} = "";
        }
    }else {
        &OBM::toolBox::write_log( "Au moins un parametre de mise a jour incrementale doit etre precise", "W" );
        $parameters->{"help"} = "";
    }

    if( exists( $parameters->{"help"} ) ) {
        &OBM::toolBox::write_log( "Affichage de l'aide", "WC" );

        print "Veuillez indiquer le critere de mise a jour :\n";
        print "\tuser <val> : utilisateur d'identifiant <val> ;\n";
        print "\tdomain <val> : domaine d'identifiant <val> ;\n";
        print "\tdelegation <motcle> : delegation de mot cle <motcle>.\n";
        print "Un seul de ces parametres doit etre indique.\n";

        exit 0;
    }
}


# On prepare le log
&OBM::toolBox::write_log( "incrementalUpdate.pl: ", "O" );

# Traitement des parametres
&OBM::toolBox::write_log( "Analyse des parametres du script", "W" );
my %parameters;
getParameter( \%parameters );

#
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


my $loadDbIncremental = OBM::loadDbIncremental->new( $dbHandler, \%parameters );

#
# Recuperation des domaines a traiter
&OBM::toolBox::write_log( "Recuperation de la liste des domaines a traiter", "W" );
local $main::domainList = undef;
if( defined($parameters{"domain"}) ) {
    $main::domainList = &OBM::toolBox::getDomains( $dbHandler, $parameters{"domain"} );
}else {
    $main::domainList = &OBM::toolBox::getDomains( $dbHandler, undef );
}


#
# Otention des serveurs LDAP par domaines
&OBM::ldap::getServerByDomain( $dbHandler, $main::domainList );

my $ldapEngine = OBM::Ldap::ldapEngine->new( $main::domainList );
$ldapEngine->init;
$ldapEngine->destroy;

#
# On referme la connexion a la base
if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
    &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "W" );
}

#
# On ferme le log
&OBM::toolBox::write_log( "", "C" );

exit 0
