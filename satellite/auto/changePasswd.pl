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
#require OBM::toolBox;
require OBM::Tools::obmDbHandler;
require OBM::Update::updatePassword;
use OBM::Parameters::common;
use OBM::Parameters::regexp;
use Getopt::Long;
use Unicode::MapUTF8 qw(from_utf8);

$ENV{PATH}=$automateOBM;
delete @ENV{qw(IFS CDPATH ENV BASH_ENV)};


# Fonction de vérification des paramètres du script
sub getParameter {
    my( $parameters ) = @_;

    # Analyse de la ligne de commande
    GetOptions( $parameters, "login=s", "domain=s", "type=s", "passwd=s", "old-passwd=s", "unix", "samba", "sql", "interactiv", "no-old", "help" );


    if( $$parameters{help} ) {
        return 2;
    }


    # Vérification du paramètre 'login'
    if( !$$parameters{login} ) {
        &OBM::toolBox::write_log( "Parametre --login manquant", "WC", 0 );
        return 1;

    }else {
        if( !($$parameters{login} =~ /$regexp_login/) ) {
            &OBM::toolBox::write_log( "Parametre --login invalide", "W", 0 );
            return 1;
        }
    }


    # Vérification du paramètre 'domain'
    if( !$$parameters{domain} ) {
        &OBM::toolBox::write_log( "Parametre --domain manquant", "WC", 0 );
        return 1;

    }else {
        if( $$parameters{domain} !~ /$regexp_domain_id/ ) {
            &OBM::toolBox::write_log( "Parametre --domain incorrect", "WC", 0 );
            return 1;
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
            return 1;

        }else {
            &OBM::toolBox::write_log( "Type du mot de passe : ".$$parameters{type}, "W", 3 );
            last SWITCH;
        }
    }


    # Si auncun mot de passe n'est specifié
    if( !$$parameters{interactiv} && !$$parameters{passwd} ) {
        &OBM::toolBox::write_log( "Mot de passe non indiqué sur la ligne de commande, utilisation du  mode interactif", "WC", 0 );
        $$parameters{interactiv} = 1;
    }


    if( !$$parameters{"no-old"} ) {
        # Vérification de l'ancien mot de passe
        SWITCH: {
            if( !$$parameters{interactiv} && !$$parameters{"old-passwd"} ) {
                &OBM::toolBox::write_log( "Mode non interactif - l'ancien mot de passe doit etre specifie sur la ligne de commande", "WC", 0 );

                return 1;
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

            return 1;
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
                return 1;

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
        if( $$parameters{interactiv} ) {
            print STDERR 'Le nouveau mot de passe ne respecte pas la syntaxe requise - \''.$regexp_passwd."'\n";
        }
        &OBM::toolBox::write_log( 'Le nouveau mot de passe ne respecte pas la syntaxe requise - \''.$regexp_passwd.'\'', "WC", 2 );

        exit 2;
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
                return 1;
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

    return 0;
}


# On prépare le log
my ($scriptname) = ($0=~'.*/([^/]+)');
&OBM::toolBox::write_log( $scriptname.': ', "O", 0 );

# Traitement des paramètres
&OBM::toolBox::write_log( "Analyse des parametres du script", "W", 3 );
my %parameters;
my $getParamRet = getParameter( \%parameters );
if( ($getParamRet == 2) || ($parameters{'interactiv'} && $getParamRet) ) {
    &OBM::toolBox::write_log( 'Affichage de l\'aide', 'WC', 2 );
    print STDERR 'Script permettant de mettre à jour les mots de passes SQL et/ou LDAP'."\n";
    print STDERR 'Pour plus d\'informations : perldoc path_to/changePasswd.pl'."\n";
    print STDERR 'Syntaxe :'."\n";
    print STDERR "\t".'changePasswd.pl --login LOGIN --domain DOMAIN_ID --interactiv [--no-old] [--unix|--samba|--sql]'."\n";
    print STDERR "\t".'changePasswd.pl --login LOGIN --domain DOMAIN_ID --type PASSWD_TYPE --passwd NEW_PASSWD [--no-old|--old-passwd OLD_PASSWD] [--unix|--samba|--sql]'."\n";
    print STDERR 'By default, only unix passwd is update'."\n";
    exit 0;

}elsif( $getParamRet ) {
    &OBM::toolBox::write_log( "", "C" );
    exit 1;
}

# On se connecte a la base
my $dbHandler = OBM::Tools::obmDbHandler->instance();
if( !defined($dbHandler) ) {
    &OBM::toolBox::write_log( 'Probleme lors de l\'ouverture de la base de donnees', 'WC', 0 );
    exit 2;
}


my $updatePasswd = OBM::Update::updatePassword->new( \%parameters );
my $errorCode = 0;
if( defined($updatePasswd) ) {
    $errorCode = $updatePasswd->update();
}


# On referme la connexion à la base
$dbHandler->destroy();


# On ferme le log
&OBM::toolBox::write_log( "Execution du script terminee", "WC", 0 );

exit !$errorCode;

# Perldoc
=head1 NAME

changePasswd.pl - OBM administration tool to manipulate user password

=head1 SYNOPSIS

  # Prompt for old and new password, update only LDAP 'userPassword' attribute
  $ changePasswd.pl --login <LOGIN> --domain <DOMAIN_ID> --interactiv

  # Prompt for new password, don't check old, update only LDAP 'userPassword'
  # attribute
  $ changePasswd.pl --login <LOGIN> --domain <DOMAIN_ID> --interactiv --no-old

  # Prompt for old and new password, update :
  #     - LDAP 'userPassword' attribute
  #     - LDAP 'sambaNTPassword' and 'sambaLMPassword' attributes
  #     - SQL 'userobm_password' and 'userobm_password_type' column for
  #     'UserObm' and 'P_UserObm' tables
  $ changePasswd.pl --login <LOGIN> --domain <DOMAIN_ID> --interactiv --unix --samba --sql

  # Pass new and old password on command line, update only LDAP 'userPassword'
  # attribute
  $ changePasswd.pl --login <LOGIN> --domain <DOMAIN_ID> --type <PWD_TYPE> --passwd <NEW_PASSWD> --old-passwd <OLD_PASSWD>

  # Pass only new password on command line, update only LDAP 'userPassword'
  # attribute
  $ changePasswd.pl --login <LOGIN> --domain <DOMAIN_ID> --type <PWD_TYPE> --passwd <NEW_PASSWD> --no-old

  # Typical Samba usage for 'smb.conf' 'passwd program' option
  $ changePasswd.pl --login %u --domain <DOMAIN_ID> --interactiv --unix --sql --no-old
  # 'smb.conf' 'passwd chat' sample :
  passwd chat = *New*password:* %n*Re-type*new*password:* %n*

=head1 DESCRIPTION

This script allow modify OBM password in all of his forms (LDAP, SQL).

=head1 COMMANDS

=over 4

=item C<login> : B<needed>

=over 4

=item only left part of login (before '@' for multi-domains)

=item modify password for this user

=back

=item C<domain> : B<needed>

=over 4

=item domain BD ID

=back

=item C<sql> : change user BD password

=item C<unix> : change user 'userPassword' LDAP attribute

=item C<samba> : change 'sambaNTPassword' and 'sambaLMPassword' LDAP attributes

=item C<no-old> : change password without verifying old

=item C<interactiv> : prompt for passwords

=item C<type> : password type for non interactive change

=over 4

=item B<PLAIN> : plain password

=back

=item C<passwd> : new password

=item C<old-passwd> : old password

=back

If none of 'passwd' and 'interactiv' options are specified, then script run in
interactive mode.

Options 'type', 'passwd' and 'old-passwd' are exclusive with 'interactiv'.

Mode non interactive have priority on 'interactiv' option.

Interactive mode password type is 'PLAIN'. On non interactive mode, password type must be
indicate.

Options 'sql', 'unix' and 'samba' can be used at the same time.

This script generate log via syslog.
