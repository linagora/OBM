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

#####################################################################
# OBM               - File : updateSieve.pl                         #
#                   - Desc : Script permettant de gérer le filtre   #
#                   sieve d'un utilisateur                          #
#                   - Paramètres :                                  #
#                       uid : login de l'utilisateur à traiter      #
#                       deomain : domain ID de l'utilisateur        #
#####################################################################
# Retour :                                                          #
#    - 0 : tout c'est bien passé                                    #
#    - 1 : erreur à l'analyse des paramètres du script              #
#    - 2 : erruer à l'ouverture de la BD                            #
#####################################################################

use strict;
#require OBM::toolBox;
require OBM::Tools::obmDbHandler;
require OBM::Update::updateSieve;
use OBM::Parameters::common;
use OBM::Parameters::regexp;
use Getopt::Long;

$ENV{PATH}=$automateOBM;
delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};


# Fonction de vérification des paramètres du script
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
        if( $$parameters{"domain"} !~ /$regexp_domain_id/ ) {
            &OBM::toolBox::write_log( "Parametre --domain invalide", "W" );
            return 1;
        }
    }

    return 0;
}


# On prepare le log
my ($scriptname) = ($0=~'.*/([^/]+)');
&OBM::toolBox::write_log( $scriptname.': ', 'O', 0 );

# Traitement des parametres
&OBM::toolBox::write_log( 'Analyse des parametres du script', 'W', 3 );
my %parameters;
if( getParameter( \%parameters ) ) {
    &OBM::toolBox::write_log( '', 'C' );
    exit 1;
}

# On se connecte a la base
my $dbHandler = OBM::Tools::obmDbHandler->instance();
if( !defined($dbHandler) ) {
    &OBM::toolBox::write_log( 'Probleme lors de l\'ouverture de la base de donnees', 'WC', 0 );
    exit 2;
}


my $errorCode = 0;
my $updateSieve = OBM::Update::updateSieve->new( \%parameters );
if( defined($updateSieve) ) {
    $errorCode = $updateSieve->update();
}


# On referme la connexion à la base
$dbHandler->destroy();


# On ferme le log
&OBM::toolBox::write_log( "Execution du script terminee", "WC", 0 );

exit !$errorCode;

# Perldoc

=head1 NAME

updateSieve.pl - OBM administration tool to update user Sieve script

=head1 SYNOPSIS

  # Update Sieve script for user
  $ updateSieve.pl --login <LOGIN> --domain <DOMAIN_ID>

=head1 DESCRIPTION

This script is used bt OBM-UI to real-time Sieve operations like vacation or
redirection.

Allow users to modify their Sieve script without admin validation.

=head1 COMMANDS

=over 4

=item C<login> : only left part of login (before '@' for multi-domains)

=item C<domain> : domain BD ID

=back

This script is used for :

=over 4

=item enable/disable vacation

=item enable/disable redirection

=back

This script generate log via syslog.
