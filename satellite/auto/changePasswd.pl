#!/usr/bin/perl -w
#####################################################################
# OBM               - File : changePasswd.pl                        #
#                   - Desc : Script permettant de modifier le mot   #
#                   de passe de l'utilisateur dont le login est     #
#                   passe en paramètre                              #
#####################################################################
# Retour :                                                          #
#    - 0 : tout c'est bien passé                                    #
#    - 1 : erreur à l'analyse des paramètres du script              #
#    - 2 : erruer à l'ouverture de la BD                            #
#####################################################################

use strict;
require OBM::toolBox;
require OBM::dbUtils;
require OBM::Update::updatePassword;
use OBM::Parameters::common;
use Getopt::Long;
use Unicode::MapUTF8 qw(from_utf8);

$ENV{PATH}=$automateOBM;
delete @ENV{qw(IFS CDPATH ENV BASH_ENV)};


# Fonction de vérification des paramètres du script
sub getParameter {
    my( $parameters ) = @_;

    # Analyse de la ligne de commande
    &GetOptions( $parameters, "login=s", "domain=s", "type=s", "passwd=s", "old-passwd=s", "unix", "samba", "sql", "interactiv", "no-old" );


    if( !$$parameters{login} ) {
        &OBM::toolBox::write_log( "Parametre --login manquant", "WC", 0 );
        exit 1;

    }else {
        if( !($$parameters{login} =~ /$regexp_login/) ) {
            &OBM::toolBox::write_log( "Parametre --login invalide", "W", 0 );
            exit 1;
        }
    }


    # Vérification du type du mot de passe
    $$parameters{type} = uc( $$parameters{type} );
    SWITCH: {
        if( $$parameters{interactiv} ) {
            $$parameters{type} = "PLAIN";
        }

        if( !$$parameters{type} ) {
            &OBM::toolBox::write_log( "Type du mot de passe non specifie.", "WC", 0 );
            exit 1;
        }else {
            &OBM::toolBox::write_log( "Type du mot de passe : ".$$parameters{type}, "W", 3 );
            last SWITCH;
        }
    }

    # Vérification des paramètres
    if( !$$parameters{domain} ) {
        &OBM::toolBox::write_log( "Parametre --domain manquant", "WC", 0 );
        exit 1;
    }else {
        if( $$parameters{domain} !~ /$regexp_domain/ ) {
            &OBM::toolBox::write_log( "Parametre --domain incorrect", "WC", 0 );
            exit 1;
        }
    }


    if( !$$parameters{"no-old"} ) {
        # Vérification de l'ancien mot de passe
        SWITCH: {
            if( !$$parameters{interactiv} && !$$parameters{"old-passwd"} ) {
                &OBM::toolBox::write_log( "Mode non interactif - l'ancien mot de passe doit etre specifie sur la ligne de commande", "WC", 0 );

                exit 1;
            }

            if( $$parameters{interactiv} ) {
                &OBM::toolBox::write_log( "Mode interactif - saisie de l'ancien mot de passe depuis l'entree standart.", "W", 1 );
            
                print "Old password: ";
                system('/bin/stty', '-echo'); # Disable echo
                $$parameters{"old-passwd"} = <STDIN>;
                system('/bin/stty', 'echo'); # Enable echo

                # Pour embellir l'affichage
                print "\n";

                &OBM::toolBox::write_log( "Mode interactif - ancien mot de passe lu depuis l'entree standart.", "W", 2 );

                chop( $$parameters{"old-passwd"} );

                # Conversion, si nécessaire, du mot de passe en UTF8 avant de l'utiliser
                if( $ENV{LANG} =~ /utf-8/i ) {
                    $$parameters{"old-passwd"} = from_utf8( { -string => $$parameters{"old-passwd"}, -charset => "ISO-8859-1" } );
                }
            }
        }

        # En niveau 4 de log on logge l'ancien mot de passe !
        &OBM::toolBox::write_log( "Ancien mot de passe : ".$$parameters{"old-passwd"}, "W", 4 );
    }


    # vérification du nouveau mot de passe
    SWITCH: {
        # Si auncun mot de passe n'est specifie
        if( !$$parameters{interactiv} && !$$parameters{passwd} ) {
            &OBM::toolBox::write_log( "Mode non interactif - vous devez indiquer le mot de passe sur la ligne de commande", "WC", 0 );

            exit 1;
        }

        # Si les 2 formes de mots de passés sont specifiées
        if( !$$parameters{interactiv} && ( $$parameters{passwd} ) ) {
            &OBM::toolBox::write_log( "Mode non interactif - le mot de passe est passe en parametre", "W", 2 ); 

            last SWITCH;
        }

        # en mode interactif, on lit le mot de passe depuis l'entrée standart
        if( $$parameters{interactiv} ) {
            &OBM::toolBox::write_log( "Mode interactif - saisie du nouveau mot de passe depuis l'entree standart", "W", 2 );
            print "New password: ";
            system('/bin/stty', '-echo'); # Disable echo
            my $newPasswd = <STDIN>;
            system('/bin/stty', 'echo'); # Enable echo
            chop( $newPasswd );
            &OBM::toolBox::write_log( "Mode interactif - nouveau mot de passe lu depuis l'entree standart", "W", 2 );
            &OBM::toolBox::write_log( "Mode interactif - 1° saisie du nouveau mot de passe '".$newPasswd."'", "W", 4 );
            
            &OBM::toolBox::write_log( "Mode interactif - deuxieme saisie du nouveau mot de passe depuis l'entree standart", "W", 2 );
            print "\nRe-type new password: ";
            system('/bin/stty', '-echo'); # Disable echo
            my $newPasswd2 = <STDIN>;
            system('/bin/stty', 'echo'); # Enable echo
            chop( $newPasswd2 );
            &OBM::toolBox::write_log( "Mode interactif - nouveau mot de passe re-lu depuis l'entree standart", "W", 2 );
            &OBM::toolBox::write_log( "Mode interactif - 2° saisie du nouveau mot de passe '".$newPasswd2."'", "W", 4 );

            # Pour embellir l'affichage
            print "\n";

            if( $newPasswd ne $newPasswd2 ) {
                &OBM::toolBox::write_log( "Mode interactif - les nouveaux mots de passes ne correspondent pas", "WC", 0 );
                exit 1;

            }else {
                $$parameters{passwd} = $newPasswd;

            }

            # Conversion, si nécessaire, du mot de passe en UTF8 avant de l'utiliser
            if( $ENV{LANG} =~ /utf-8/i ) {
                $$parameters{passwd} = from_utf8( { -string => $$parameters{passwd}, -charset => "ISO-8859-1" } );
            }
        }
    }

    # Vérification syntaxique du mot de passe
    if( $$parameters{passwd} !~ /$regexp_passwd/ ) {
        &OBM::toolBox::write_log( "Syntaxe du nouvau mot de passe incorrecte", "WC", 2 );
        exit 1;
    }

    # En niveau 4 de log on logge le mot de passe !
    &OBM::toolBox::write_log( "Nouveau mot passe : ".$$parameters{passwd}, "W", 4 );


    # Quel mot de passe est à traiter - par défaut mot de passe UNIX
    SWITCH: {
        if( !$$parameters{unix} && !$$parameters{samba} && !$$parameters{sql} ) {
            $$parameters{unix} = 1;
        }
   

        # Traite seulement l'attribut 'userPassword' correspondant au compte UNIX
        if( $$parameters{unix} ) {
           &OBM::toolBox::write_log( "Modification du mot de passe UNIX", "W", 2 );
        }else {
           &OBM::toolBox::write_log( "Pas de modification du mot de passe UNIX", "W", 2 );
        }


        # Traite seulement les attributs correspondants aux comptes Samba
        if( $$parameters{samba} ) {
            if( uc($$parameters{type}) eq "PLAIN" ) {
                &OBM::toolBox::write_log( "Modification du mot de passe Samba", "W", 2 );
            }else {
                &OBM::toolBox::write_log( "Erreur: pour pouvoir modifier les mots de passes Samba, le mot de passe doit etre disponible en PLAIN", "WC", 0 );
                exit 1;
            }
        }else {
           &OBM::toolBox::write_log( "Pas de modification du mot de passe Samba", "W", 2 );
        }


        # Traite seulement les mots de passes stockés en base SQL
        if( $$parameters{sql} ) {
            &OBM::toolBox::write_log( "Modification du mot de passe Sql", "W", 2 );
        }else {
            &OBM::toolBox::write_log( "Pas de modification du mot de passe Sql", "W", 2 );
        }
    }

}


# On prépare le log
&OBM::toolBox::write_log( "changePasswd.pl: ", "O", 0 );

# Traitement des paramètres
&OBM::toolBox::write_log( "Analyse des parametres du script", "W", 3 );
my %parameters;
if( getParameter( \%parameters ) ) {
    &OBM::toolBox::write_log( "", "C" );
    exit 1;
}

# On se connecte a la base
my $dbHandler;
&OBM::toolBox::write_log( "Connexion a la base de donnees OBM", "W" );
if( !&OBM::dbUtils::dbState( "connect", \$dbHandler ) ) {
    if( defined($dbHandler) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'ouverture de la base de donnee : ".$dbHandler->err, "WC" );
    }else {
        &OBM::toolBox::write_log( "Probleme lors de l'ouverture de la base de donnee : erreur inconnue", "WC" );
    }

    exit 2;
}


my $updatePasswd = OBM::Update::updatePassword->new( $dbHandler, \%parameters );
my $errorCode = 0;
if( defined($updatePasswd) ) {
    $errorCode = $updatePasswd->update();
}


# On referme la connexion à la base
if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
    &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "W" );
}


# On ferme le log
&OBM::toolBox::write_log( "Execution du script terminee", "WC", 0 );

exit !$errorCode;
