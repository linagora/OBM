#!/usr/bin/perl -w -T
###############################################################################
# OBM :     - File : dbUpdate.pl
#           - Desc : permet de valider ou d'annuler les modifications en
#                    attente
###############################################################################

use strict;
require OBM::dbUtils;
require OBM::toolBox;
use Getopt::Long;

#
# Fonction de verification des parametres du script
sub getParameter {
    my( $parameters ) = @_;

    #
    # Analyse de la ligne de comman
    &GetOptions( $parameters, "valid", "cancel" );

    SWITCH: {
        if( exists($$parameters{"valid"}) ) {
            &OBM::toolBox::write_log( "Validation des modifications en attente.", "W" );
            last SWITCH;
        }

        if( exists($$parameters{"cancel"}) ) {
            &OBM::toolBox::write_log( "Annulation des modifications en attente.", "W" );
            last SWITCH;
        }

        &OBM::toolBox::write_log( "Les parametres possibles pour le script sont :", "W" );
        &OBM::toolBox::write_log( "    --valid : pour valider les modifications ;", "W" );
        &OBM::toolBox::write_log( "    --cancel : pour annuler les modifications ;", "WC" );
        exit 1;
    }
}

sub applyDbModif {
    my( $dbTable, $operation ) = @_;

    #
    # On se connecte a la base
    my $dbHandler;
    if( !&OBM::dbUtils::dbState( "connect", \$dbHandler ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'ouverture de la base de donnee : ".$dbHandler->err, "WC" );
        exit 1;
    }

    for( my $i=0; $i<=$#$dbTable; $i++ ) {
        my $orig = "";
        my $dst = "";

        if( $operation eq "valid" ) {
            $orig = $$dbTable[$i];
            $dst = "P_".$$dbTable[$i];

        }elsif( $operation eq "cancel" ) {
            $orig = "P_".$$dbTable[$i];
            $dst = $$dbTable[$i];

        }

        my $query = "DELETE FROM ".$dst;

        my $queryResult;
        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "Probleme de l'effacement de la table ".$dst." : ".$dbHandler->err, "W" );
            next;
        }

        $query = "INSERT INTO ".$dst." SELECT * FROM ".$orig;

        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "Probleme lors du traitement de la table ".$$dbTable[$i]." : ".$dbHandler->err, "W" );
        }
    }

    #
    # On referme la connexion a la base
    if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
        &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "WC" );
        exit 1;
    }
}

#
# On prepare le log
&OBM::toolBox::write_log( "dbUpdate.pl: ", "O" );
&OBM::toolBox::write_log( "Traitement des tables de la BD.", "W" );

#
# Traitement des parametres
&OBM::toolBox::write_log( "Analyse des parametres du script", "W" );
my %parameters;
getParameter( \%parameters );

SWITCH: {
    my @dbTable = ( 'EntityRight', 'GroupGroup', 'Host', 'MailServer', 'MailServerNetwork', 'MailShare', 'Samba', 'UGroup', 'UserObm', 'UserObmGroup' );

    if( exists($parameters{"valid"}) ) {
        applyDbModif( \@dbTable, "valid" );

        last SWITCH;
    }

    if( exists($parameters{"cancel"}) ) {
        applyDbModif( \@dbTable, "cancel" );

        last SWITCH;
    }
}

#
# On ferme le log
&OBM::toolBox::write_log( "Fin du traitement des tables de la BD.", "WC" );

#
# Tout c'est bien passe
exit 0

__END__

=head1 NOM

    dbUpdate.pl - permet de valider ou d'annuler les modifications en attente.

=head1 DESCRIPTION

    Ce script permet de valider ou d'annuler les modifications en attente en BD.

    Ce script a besoin des paramètres :
        - valid : permet de valider les modifications en attente ;
        - cancel : permet d'annuler les modifications en attente.
