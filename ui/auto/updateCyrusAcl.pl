#!/usr/bin/perl -w -T
#####################################################################
# OBM               - File : updateCyrusAcl.pl                      #
#                   - Desc : script permettant une mise à jour en   #
#                            temps réel des ACLs sur une BAL/share  #
#####################################################################

use strict;
require OBM::toolBox;
require OBM::Tools::obmDbHandler;
require OBM::Update::updateCyrusAcl;
use OBM::Parameters::common;
use Getopt::Long;


delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};


# Fonction de vérification des paramètres du script
sub getParameter {
    my( $parameters ) = @_;

    # Analyse de la ligne de commande
    &GetOptions( $parameters, "type=s", "name=s", "domain=s" );

    # Vérification du type d'entité à mettre à jour
    if( !exists($$parameters{"type"}) ) {
        &OBM::toolBox::write_log( "Parametre --type manquant", "W" );
        print STDERR "Paramètre '--type' manquant.\nCe paramètre vaut :\n";
        print STDERR "\t'mailbox' pour mettre à jour les ACL d'une boîte à lettres utilisateur\n";
        print STDERR "\t'mailshare' pour mettre à jour les ACL d'un répertoire partagé\n";
        return 1;
    }else {
        if( $$parameters{"type"} !~ /^(mailbox|mailshare)$/ ) {
            &OBM::toolBox::write_log( "Parametre --type invalide", "W" );
            print STDERR "Paramètre '--type' invalide.\nCe paramètre vaut :\n";
            print STDERR "\t'mailbox' pour mettre à jour les ACL d'une boîte à lettres utilisateur\n";
            print STDERR "\t'mailshare' pour mettre à jour les ACL d'un répertoire partagé\n";
            return 1;
        }
    }

    # Vérification de l'identifiant utilisateur
    if( !exists($$parameters{"name"}) ) {
        &OBM::toolBox::write_log( "Parametre --name manquant", "W" );
        print STDERR "Paramètre '--name' manquant.\nCe paramètre est :\n";
        print STDERR "\tle login d'un utilisateur si le paramètre '--type' vaut 'mailbox'\n";
        print STDERR "\tle nom d'un répertoire partagé si le paramètre '--type' vaut 'mailshare'\n";
        return 1;
    }else {
        if( $$parameters{"name"} !~ /$regexp_login/ ) {
            &OBM::toolBox::write_log( "Parametre --name invalide", "W" );
            print STDERR "Paramètre '--name' invalide.\nCe paramètre est :\n";
            print STDERR "\tle login d'un utilisateur si le paramètre '--type' vaut 'mailbox'\n";
            print STDERR "\tle nom d'un répertoire partagé si le paramètre '--type' vaut 'mailshare'\n";
            return 1;
        }
    }

    # Verification du domaine
    if( !exists($$parameters{"domain"}) ) {
        &OBM::toolBox::write_log( "Parametre --domain manquant", "W" );
        print STDERR "Paramètre '--domain' manquant.\nCe paramètre indique l'ID BD du domaine de l'entité à mettre à jour.\n";
        return 1;
    }else {
        if( $$parameters{"domain"} !~ /$regexp_domain/ ) {
            &OBM::toolBox::write_log( "Parametre --domain invalide", "W" );
            print STDERR "Paramètre '--domain' invalide.\nCe paramètre indique l'ID BD du domaine de l'entité à mettre à jour.\n";
            return 1;
        }
    }

    return 0;
}


# On prepare le log
my ($scriptname) = ($0=~'.*/([^/]+)');
&OBM::toolBox::write_log( $scriptname.': ', 'O', 0 );

# Traitement des paramètrs
&OBM::toolBox::write_log( 'Analyse des parametres du script', 'W', 3 );
my %parameters;
if( getParameter( \%parameters ) ) {
    &OBM::toolBox::write_log( "", "C" );
    exit 1;
}


# On se connecte àla base
my $dbHandler = OBM::Tools::obmDbHandler->instance();
if( !defined($dbHandler) ) {
    &OBM::toolBox::write_log( 'Probleme lors de l\'ouverture de la base de donnees', 'WC', 0 );
    exit 1;
}


my $updateCyrusAcl = OBM::Update::updateCyrusAcl->new( \%parameters );
my $errorCode = 0;
if( defined($updateCyrusAcl) ) {
     $errorCode = $updateCyrusAcl->update();
     $updateCyrusAcl->destroy();
}



# On referme la connexion àla base
$dbHandler->destroy();


# On ferme le log
&OBM::toolBox::write_log( "Execution du script terminee", "WC" );

exit !$errorCode;

# Perldoc

=head1 NAME

updateCyrusAcl.pl - OBM administration tool to update Cyrus ACL

=head1 SYNOPSIS

  # Update user mailbox ACLs
  $ updateCyrusAcl.pl --type mailbox --name <LOGIN> --domain <DOMAIN_ID>

  # Update mailshare ACLs
  $ updateCyrusAcl.pl --type mailshare --name <MAILSHARE_NAME> --domain <DOMAIN_ID>

=head1 DESCRIPTION

This script is used by OBM-UI to real-time update Cyrus ACLs.

Allow users to modify their mailbox ACLs without admin validation.

Allow mailshare admins to modify mailshare ACLs without admin validation.

=head1 COMMANDS

=over 4

=item C<type> :

=over 4

=item B<mailbox> : update mailbox ACL

=item B<mailshare> : update mailshare ACL

=back

=item C<name> :

=over 4

=item B<LOGIN>, only left part of login (before '@' for multi-domains), if type
is 'mailbox'

=item B<MAILSHARE_NAME>, only left part of mailshare name (before '@' for
multi-domains), if type is 'mailshare'

=back

=item C<domain> : domain BD ID

=back

This script generate log via syslog.
