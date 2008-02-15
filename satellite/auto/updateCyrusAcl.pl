#!/usr/bin/perl -w -T
#####################################################################
# OBM               - File : updateCyrusAcl.pl                      #
#                   - Desc : script permettant une mise à jour en   #
#                            temps réel des ACLs sur une BAL/share  #
#####################################################################

use strict;
require OBM::toolBox;
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
            &OBM::toolBox::write_log( "Parametre --login invalide", "W" );
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
        if( $$parameters{"domain"} !~ /^[0-9]+$/ ) {
            &OBM::toolBox::write_log( "Parametre --domain invalide", "W" );
            print STDERR "Paramètre '--domain' invalide.\nCe paramètre indique l'ID BD du domaine de l'entité à mettre à jour.\n";
            return 1;
        }
    }

    return 0;
}


# On prepare le log
&OBM::toolBox::write_log( "UpdateBalAcl: ", "O" );

# Traitement des paramètrs
&OBM::toolBox::write_log( "Analyse des parametres du script", "W" );
my %parameters;
if( getParameter( \%parameters ) ) {
    &OBM::toolBox::write_log( "", "C" );
    exit 1;
}


# On se connecte àla base
my $dbHandler;
&OBM::toolBox::write_log( "Connexion a la base de donnees OBM", "W" );
if( !&OBM::dbUtils::dbState( "connect", \$dbHandler ) ) {
    &OBM::toolBox::write_log( "Probleme lors de l'ouverture de la base de donnee : ".$dbHandler->err, "WC" );
    exit 1;
}


my $updateCyrusAcl = OBM::Update::updateCyrusAcl->new( $dbHandler, \%parameters );
my $errorCode = 0;
if( defined($updateCyrusAcl) ) {
     $errorCode = $updateCyrusAcl->update();
     $updateCyrusAcl->destroy();
}



# On referme la connexion àla base
&OBM::toolBox::write_log( "Deconnexion de la base de donnees OBM", "W" );
if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
    &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "W" );
}


# On ferme le log
&OBM::toolBox::write_log( "Execution du script terminee", "WC" );

exit !$errorCode;

