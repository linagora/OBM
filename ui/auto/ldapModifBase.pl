#!/usr/bin/perl -w -T
###############################################################################
# OBM       - File : ldapModifBase.pl                                         #
#           - Desc : Modification de la base LDAP en fonction des infos       #
#           de la base SQL                                                    #
###############################################################################
# Creer le 2002-07-30                                                         #
# $Id$
###############################################################################
# Code retour :                                                               #
#   0 : tout c'est bien passe                                                 #
###############################################################################

use strict;
require OBM::dbUtils;
require OBM::toolBox;
require OBM::ldap;
use OBM::Parameters::common;
use OBM::Parameters::ldapConf;

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};


#
# Debut du main
####################################

#
# On ouvre un connection avec Syslog
&OBM::toolBox::write_log( "ldapModifBase: ", "O" );

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

#
# Recuperation des domaines a traiter
&OBM::toolBox::write_log( "Recuperation de la liste des domaines a traiter", "W" );
local $main::domainList = undef;
$main::domainList = &OBM::toolBox::getDomains( $dbHandler, undef );

&OBM::ldap::getServerByDomain( $dbHandler, $main::domainList );

#
# On referme la connexion a la base
if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
    &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "W" );
}

#
# Construit la structure de l'arbre LDAP en mémoire
&OBM::toolBox::write_log( "--------", "W" );
&OBM::toolBox::write_log( "Construction de la structure de l'arbre LDAP depuis la BD", "W" );
&OBM::ldap::initTree( $ldapStruct, undef, undef, 1 );

#
# Mise a jour des annuaires en fonction des informations de la structure
&OBM::toolBox::write_log( "--------", "W" );
&OBM::toolBox::write_log( "Mise a jour des annuaires LDAP", "W" );
&OBM::ldap::updateLdap( $ldapStruct, undef );

#
# On ferme la connection avec Syslog
&OBM::toolBox::write_log( "--------", "W" );
&OBM::toolBox::write_log( "", "C" );

#
# On termine proprement
exit 0;
