#!/usr/bin/perl -w
###############################################################################
# Aliamin - File : update.pl                                                #
#           - Desc : Script permettant d'executer les scripts de mises a jour #
#                    dans un ordre determine                                  #
# 2002-07-26 Anthony Prades                                                   #
###############################################################################
# $Id$   #
###############################################################################
# Parametres :                                                                #
#   - all      : reconfiguration du systeme complet                           #
#   - ldap     : gestion de l'annuaire LDAP                                   #
#   - cyrus    : gestion du serveur Cyrus IMAP                                #
###############################################################################
# Retour :                                                                    #
#    - 0 : tout c'est bien passe                                              #
#    - 1 : problemes avec les parametres du script                            #
###############################################################################

use strict;
require OBM::toolBox;
use OBM::Parameters::common;
use Getopt::Long;


#
# On prepare le log
&OBM::toolBox::write_log( "Update: ", "O" );

#
# Traitement des parametres
&OBM::toolBox::write_log( "Script de prise en compte des modifications de configuration", "W" );
my %parameters;

#
# Analyse de la ligne de commande
&GetOptions( \%parameters, "ldap", "cyrus", "all" );


# Si l'option 'all' est presente, on force l'execution de tout
if( $parameters{"all"} ) {
    &OBM::toolBox::write_log( "Mise a jour de tous les services", "W" );
    $parameters{"ldap"} = "1";
    $parameters{"cyrus"} = "1";
}


# L'annuaire LDAP a ete modifie
if( $parameters{"ldap"} ) {
    &OBM::toolBox::write_log( "Mise a jour de l'annuaire LDAP.", "W" );
    if( &OBM::toolBox::execCmd( "$automateLdapUpdate", 0 ) ) {
        &OBM::toolBox::write_log( "Probleme lors de la creation du nouvel annuaire LDAP", "W" );
    }else
    {
        &OBM::toolBox::write_log( "Creation du nouvel annuaire LDAP terminee", "W" );
    }
}

# Configuration du serveur Cyrus IMAP
if( $parameters{"cyrus"} ) {
    &OBM::toolBox::write_log( "Reconfiguration du serveur Cyrus IMAP", "W" );
    if( &OBM::toolBox::execCmd( "$automateCyrusAdmin", 0 ) ) {
        &OBM::toolBox::write_log( "Probleme lors de la reconfiguration du serveur Cyrus IMAP", "W" );
    }else
    {
        &OBM::toolBox::write_log( "Reconfiguration du serveur Cyrus IMAP terminee", "W" );
    }
}


#
# On ferme le log
&OBM::toolBox::write_log( "", "C" );

#
# Tout c'est bien passe
exit 0

__END__

=head1 NOM

update.pl - Permet de gerer les scripts de l'automate. C'est ce script qui est
appelé par l'interface après avoir calculé les paramètres à y passer.

=head1 DEPENDANCES

    use Aliamin::ToolBox;
    use Aliamin::Parameters::Common;
    use Getopt::Long;

=head1 DESCRIPTION

Ce script permet de gérer les scripts de l'automate.

L'interface calcule les paramètres à passer en fonction des modifications
faites.

=head1 PARAMETRES

Le script admet les paramètres :

=over 4

=item --ldap : mise à jour de l'arbre LDAP ;

=item --cyrus : mise à jour du serveur de messagerie ;

=item --all : exécution de tous les scripts.

=back

=cut
