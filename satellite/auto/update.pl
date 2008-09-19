#!/usr/bin/perl -w -T
#####################################################################
# OBM               - File : update.pl                              #
#                   - Desc : Script permettant de mettre à jour le  #
#                   système de façon incrémentale                   #
#####################################################################

use strict;
require OBM::toolBox;
require OBM::Tools::obmDbHandler;
use Getopt::Long;

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};


# Fonction de verification des parametres du script
sub getParameter {
    my( $parameters ) = @_;

    # Analyse de la ligne de commande
    my $return = GetOptions( $parameters, "user=s", "domain=s", "delegation=s", "global", "incremental", "help" );

    if( !$return ) {
        $parameters->{"help"} = 1;
    }


    if( !exists($parameters->{"domain"}) ) {
        &OBM::toolBox::write_log( "Parametre '--domain' manquant", "W" );
        $parameters->{"help"} = 1;

    }else {
        &OBM::toolBox::write_log( "Mise a jour du domaine d'identifiant '".$parameters->{"domain"}."'", "W" );
    }
    
    if( exists($parameters->{"user"}) ) {
        if( exists($parameters->{"delegation"}) ) {
            &OBM::toolBox::write_log( "Trop de parametres de mise a jour precise", "W" );
            $parameters->{"help"} = 1;
        }else{
            &OBM::toolBox::write_log( "Uniquement les mises a jour de l'utilisateur d'identifiant '".$parameters->{"user"}."'", "W" );
        }

    }elsif( exists($parameters->{"delegation"}) ) {
        if( exists($parameters->{"user"}) ) {
            &OBM::toolBox::write_log( "Trop de parametres de mise a jour precise", "W" );
            $parameters->{"help"} = 1;
        }else {
            &OBM::toolBox::write_log( "Uniquement les mises a jour de la delegation '".$parameters->{"delegation"}."'", "W" );
        }

    }

    if( exists($parameters->{"incremental"}) && exists($parameters->{"global"}) ) {
        &OBM::toolBox::write_log( "parametres '--incremental' et '--global' incompatibles", "W" );
        $parameters->{"help"} = 1;

    }elsif( exists($parameters->{"incremental"}) ) {
        $parameters->{"incremental"} = 1;
        &OBM::toolBox::write_log( "Mise a jour incrementale", "W" );

    }elsif( exists($parameters->{"global"}) || !(exists($parameters->{"incremental"}) || exists($parameters->{"global"})) ) {
        $parameters->{"global"} = 1;
        &OBM::toolBox::write_log( "Mise a jour globale", "W" );

    }


    if( exists( $parameters->{"help"} ) ) {
        &OBM::toolBox::write_log( "Affichage de l'aide", "WC" );

        print STDERR "Veuillez indiquer le critere de mise a jour :\n";
        print STDERR "\tSyntaxe: script --domain id [--user id | --delegation word] [--global | --incremental]\n";
        print STDERR "\tuser <id> : utilisateur d'identifiant <id> ;\n";
        print STDERR "\tdomain <id> : domaine d'identifiant <id> ;\n";
        print STDERR "\tdelegation <word> : delegation de mot cle <word> ;\n";
        print STDERR "\tglobal : fait une mise a jour globale du domaine - action par defaut ;\n";
        print STDERR "\tincremental : fait une mise a jour incrementale du domaine.\n";

        exit 0;
    }
}


# On prepare le log
my ($scriptname) = ($0=~'.*/([^/]+)');
&OBM::toolBox::write_log( $scriptname.': ', 'O', 0 );

# Traitement des paramètres
&OBM::toolBox::write_log( 'Analyse des parametres du script', 'W', 3 );
my %parameters;
getParameter( \%parameters );

# On se connecte a la base
my $dbHandler = OBM::Tools::obmDbHandler->instance();
if( !defined($dbHandler) ) {
    &OBM::toolBox::write_log( 'Probleme lors de l\'ouverture de la base de donnees', 'WC', 0 );
    exit 1;
}


my $update;
if( $parameters{"global"} ) {
    require OBM::Update::updateGlobal;
    $update = OBM::Update::updateGlobal->new( \%parameters );
}else {
    require OBM::Update::updateIncremental;
    $update = OBM::Update::updateIncremental->new( \%parameters );
}

if( !defined($update) ) {
    &OBM::toolBox::write_log( "Probleme a l'initialisation de l'objet de mise a jour", "W" );
}else {
    $update->update();
    $update->destroy();
}


# On referme la connexion a la base
$dbHandler->destroy();

# On ferme le log
&OBM::toolBox::write_log( "Fin du traitement", "W" );
&OBM::toolBox::write_log( "", "C" );

exit 0;

# Perldoc

=head1 NAME

update.pl - OBM administration tool , alter ego of Cyrus::IMAP::Shell

=head1 SYNOPSIS

  # Domain global update
  $ update.pl --domain <DOMAIN_ID> --global

  # Domain incremental update
  $ update.pl --domain <DOMAIN_ID> --incremental

  # Domain incremental update - only updates done by an admin
  $ update.pl --domain <DOMAIN_ID> --user <USER_ID> --incremental

  # Domain incremental update - only updates done for a delegation
  $ update.pl --domain <DOMAIN_ID> --delegation <DELEGATION> --incremental

  # Display help
  $ update.pl --help

=head1 DESCRIPTION

This script is used by OBM-UI when an admin apply updates.

Global update apply all datas for a domain in the system regardless of BD
updates.

Incremental update apply only updates mark by the scope. It's possible to apply
updates for only a particular user or for a delegation.

=head1 COMMANDS

=over 4

=item C<help> : display help

=item C<domain> : B<needed>

=over 4

=item domain BD ID

=back

=item C<global> : global update

=item C<incremental> : incremental update

=item C<user> : apply updates done by only this user

=item C<delegation> : apply update done by only this delegation

=back

Global update by default.

Parameters 'user' and 'delegation' are exclusive.

This script generate log via syslog.
