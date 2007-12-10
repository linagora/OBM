#!/usr/bin/perl -w
#####################################################################
# OBM               - File : ldapChangePasswd.pl                    #
#                   - Desc : Script permettant de modifier le mot   #
#                   de passe de l'utilisateur dont le login est     #
#                   passe en parametre                              #
#--------------------------------------------------------------------
# $Id$
#--------------------------------------------------------------------

use strict;
require OBM::toolBox;
require OBM::dbUtils;
require OBM::ldap;
use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
use Getopt::Long;
use Unicode::MapUTF8 qw(from_utf8);

$ENV{PATH}=$automateOBM;
delete @ENV{qw(IFS CDPATH ENV BASH_ENV)};


# Fonction de verification des parametres du script
sub getParameter {
    my( $parameters, $dbHandler ) = @_;

    # Analyse de la ligne de commande
    &GetOptions( $parameters, "login=s", "domain=s", "type=s", "passwd=s", "old-passwd=s", "unix", "samba", "sql", "interactiv", "no-old" );


    if( !$$parameters{"login"} ) {
        &OBM::toolBox::write_log( "Parametre --login manquant", "WC", 0 );
        exit 1;

    }else {
        if( !($$parameters{"login"} =~ /$regexp_login/) ) {
            &OBM::toolBox::write_log( "Parametre --login invalide", "W", 0 );
            exit 1;
        }
    }


    # Verification du type du mot de passe
    $$parameters{"type"} = uc( $$parameters{"type"} );
    SWITCH: {
        if( $$parameters{"interactiv"} ) {
            $$parameters{"type"} = "PLAIN";
        }

        if( !$$parameters{"type"} ) {
            &OBM::toolBox::write_log( "Type du mot de passe non specifie.", "WC", 0 );
            exit 1;
        }else {
            &OBM::toolBox::write_log( "Type du mot de passe : ".$$parameters{"type"}, "W", 3 );
            last SWITCH;
        }
    }

    # Verification des parametres
    if( !$$parameters{"domain"} ) {
        &OBM::toolBox::write_log( "Parametre --domain manquant", "WC", 0 );
        exit 1;
    }else {
        if( $$parameters{"domain"} !~ /^[0-9]+$/ ) {
            &OBM::toolBox::write_log( "Parametre --domain incorrect", "WC", 0 );
            exit 1;
        }

        my $i = 0;
        my $found = 0;
        while( ($i<=$#{$main::domainList}) && (!$found) ) {
            if( $main::domainList->[$i]->{"domain_id"} == $$parameters{"domain"} ) {
                $found = 1;
                last;
            }
            $i++;
        }
        
        if( !$found ) {
            &OBM::toolBox::write_log( "Domaine d'identifiant '".$$parameters{"domain"}."' inexistant", "WC", 0 );
            exit 1;
        }

        $$parameters{"domain_id"} = $i;
    }


    if( !$$parameters{"no-old"} ) {
        # Verification de l'ancien mot de passe
        SWITCH: {
            if( !$$parameters{"interactiv"} && !$$parameters{"old-passwd"} ) {
                &OBM::toolBox::write_log( "Mode non interactif - l'ancien mot de passe doit etre specifie sur la ligne de commande", "WC", 0 );

                exit 1;
            }

            if( $$parameters{"interactiv"} ) {
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


    # verification du nouveau mot de passe
    SWITCH: {
        # Si auncun mot de passe n'est specifie
        if( !$$parameters{"interactiv"} && !$$parameters{"passwd"} ) {
            &OBM::toolBox::write_log( "Mode non interactif - vous devez indiquer le mot de passe sur la ligne de commande", "WC", 0 );

            exit 1;
        }

        # Si les 2 formes de mots de passes sont specifies
        if( !$$parameters{"interactiv"} && ( $$parameters{"passwd"} ) ) {
            &OBM::toolBox::write_log( "Mode non interactif - le mot de passe est passe en parametre", "W", 2 ); 

            last SWITCH;
        }

        # en mode interactif, on lit le mot de passe depuis l'entree standart
        if( $$parameters{"interactiv"} ) {
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
                $$parameters{"passwd"} = $newPasswd;

            }

            # Conversion, si nécessaire, du mot de passe en UTF8 avant de l'utiliser
            if( $ENV{LANG} =~ /utf-8/i ) {
                $$parameters{"passwd"} = from_utf8( { -string => $$parameters{"passwd"}, -charset => "ISO-8859-1" } );
            }
        }
    }

    # Vérification syntaxique du mot de passe
    if( $$parameters{"passwd"} !~ /$regexp_passwd/ ) {
        &OBM::toolBox::write_log( "Syntaxe du nouvau mot de passe incorrecte", "WC", 2 );
        exit 1;
    }

    # En niveau 4 de log on logge le mot de passe !
    &OBM::toolBox::write_log( "Nouveau mot passe : ".$$parameters{"passwd"}, "W", 4 );


    # Quel mot de passe est a traiter - par defaut mot de passe UNIX
    SWITCH: {
        if( !$$parameters{"unix"} && !$$parameters{"samba"} && !$$parameters{"sql"} ) {
            $$parameters{"unix"} = 1;
        }
   

        # Traite seulement l'attribut 'userPassword' correspondant au compte UNIX
        if( $$parameters{"unix"} ) {
           &OBM::toolBox::write_log( "Modification du mot de passe UNIX", "W", 2 );
        }else {
           &OBM::toolBox::write_log( "Pas de modification du mot de passe UNIX", "W", 2 );
        }


        # Traite seulement les attributs correspondants aux comptes Samba
        if( $$parameters{"samba"} ) {
            if( uc($$parameters{"type"}) eq "PLAIN" ) {
                &OBM::toolBox::write_log( "Modification du mot de passe Samba", "W", 2 );
            }else {
                &OBM::toolBox::write_log( "Erreur: pour pouvoir modifier les mots de passes Samba, le mot de passe doit etre disponible en PLAIN", "WC", 0 );
                exit 1;
            }
        }else {
           &OBM::toolBox::write_log( "Pas de modification du mot de passe Samba", "W", 2 );
        }


        # Traite seulement les mots de passes stockés en base SQL
        if( $$parameters{"sql"} ) {
            &OBM::toolBox::write_log( "Modification du mot de passe Sql", "W", 2 );
        }else {
            &OBM::toolBox::write_log( "Pas de modification du mot de passe Sql", "W", 2 );
        }
    }

}


# On prepare le log
&OBM::toolBox::write_log( "ldapChangePasswd.pl: ", "O", 0 );
&OBM::toolBox::write_log( "Execution du script 'ldapChangePasswd.pl'", "W", 0 );

# On se connect a la base
&OBM::toolBox::write_log( "Connexion a la base de donnees OBM", "W", 3 );
my $dbHandler;
if( !&OBM::dbUtils::dbState( "connect", \$dbHandler ) ) {
    &OBM::toolBox::write_log( "Erreur: probleme lors de l'ouverture de la base de donnee de production", "WC", 0 );
    exit 2;
}

# Recuperation des domaines
local $main::domainList = undef;
$main::domainList = &OBM::toolBox::getDomains( $dbHandler, undef );

&OBM::ldap::getServerByDomain( $dbHandler, $main::domainList );

# Traitement des parametres
&OBM::toolBox::write_log( "Analyse des parametres du script", "W", 3 );
my %parameters;
getParameter( \%parameters, $dbHandler );

# On initialise la structure de l'arbre LDAP sans les valeurs de la BD
&OBM::ldap::initTree( $ldapStruct, undef, undef, 0 );

# On récupère les informations du serveur LDAP du domaine
my $ldapSrv;
&OBM::ldap::getLdapSrv( $ldapStruct, $parameters{"domain_id"}, \$ldapSrv );

if( !$parameters{"no-old"} ) {
    # On verifie que l'ancien mot de passe fournit corresponde bien au mot de
    # passe de l'entite LDAP (attribut 'userPasswd')
    my @userDn = ();
    if( &OBM::ldap::getEntityDn( $ldapStruct, $POSIXUSERS, $parameters{"login"}, $parameters{"domain_id"}, \@userDn ) ) {
        &OBM::toolBox::write_log( "Erreur: type '".$POSIXUSERS."' inconnu", "WC", 0 );
        exit 3;
    }

    for( my $i=0; $i<=$#userDn; $i++ ) {
        &OBM::toolBox::write_log( "Verification de l'ancien mot de passe de l'entite de type '".$POSIXUSERS."' et de dn : ".$userDn[$i], "W", 2 );
        if( !&OBM::ldap::checkOldEntityPasswd( $ldapSrv, $userDn[$i], $parameters{"old-passwd"} ) ) {
            &OBM::toolBox::write_log( "Erreur: lors de la verification de l'ancien mot de passe", "WC", 0 );
            exit 4;
        }
    }
}

if( $parameters{"unix"} ) {
    # On cherche dans l'annuaire le ou les DN de l'utilisateur
    my @userDn = ();
    &OBM::ldap::getEntityDn( $ldapStruct, $POSIXUSERS, $parameters{"login"}, $parameters{"domain_id"}, \@userDn );

    if( !&OBM::ldap::updateEntityPasswd( $ldapSrv, $POSIXUSERS, \@userDn, $parameters{"type"}, $parameters{"passwd"} ) ) {
        &OBM::toolBox::write_log( "Erreur: lors de la mise a jour du mot de passe unix", "W", 0 );
    }else {
        &OBM::toolBox::write_log( "Mise a jour du mot de passe unix de l'utilisateur ".$parameters{"login"}." correctement effectuee", "W", 0 );
    }
}

if( $parameters{"samba"} ) {
    # On cherche dans l'annuaire le ou les DN de l'utilisateur
    my @userDn = ();
    &OBM::ldap::getEntityDn( $ldapStruct, $SAMBAUSERS, $parameters{"login"}, $parameters{"domain_id"}, \@userDn );

    if( !&OBM::ldap::updateEntityPasswd( $ldapSrv, $SAMBAUSERS, \@userDn, $parameters{"type"}, $parameters{"passwd"} ) ) {
        &OBM::toolBox::write_log( "Erreur: lors de la mise a jour du mot de passe Samba", "W", 0 );
    }else {
        &OBM::toolBox::write_log( "Mise a jour du mot de passe Samba de l'utilisateur ".$parameters{"login"}." correctement effectuee", "W", 0 );
    }
}

if( $parameters{"sql"} ) {
    # On met a jour les 2 tables de la BD si l'utilisateur existe
    my $query = "SELECT count(*) FROM UserObm WHERE userobm_domain_id=".$parameters{"domain_id"}." AND userobm_login='".$parameters{"login"}."'";

    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "Erreur: probleme lors de l'execution de la requete : ".$dbHandler->err, "WC", 0 );
        exit 2;
    }
    my( $count ) = $queryResult->fetchrow_array;

    if( $count != 1 ) {
        &OBM::toolBox::write_log( "Erreur: l'utilisateur de login '".$parameters{"login"}."' appartenant au domain '".$parameters{"domain_id"}."' n'existe pas en BD", "W", 0 );
    }else {
        $query = "UPDATE UserObm SET userobm_nb_login_failed='0', userobm_password_type='".$parameters{"type"}."', userobm_password='".$parameters{"passwd"}."' WHERE userobm_domain_id=".$parameters{"domain_id"}." AND userobm_login='".$parameters{"login"}."'";
        # On execute la requete
        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "Erreur: probleme lors de l'execution de la requete de mise a jour du mot de passe SQL : ".$dbHandler->err, "WC", 0 );
            exit 2;
        }


        $query = "UPDATE P_UserObm SET userobm_nb_login_failed='0', userobm_password_type='".$parameters{"type"}."', userobm_password='".$parameters{"passwd"}."' WHERE userobm_domain_id=".$parameters{"domain_id"}." AND userobm_login='".$parameters{"login"}."'";
        # On execute la requete
        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "Erreur: probleme lors de l'execution de la requete de mise a jour du mot de passe SQL : ".$dbHandler->err, "WC", 0 );
            exit 2;
        }

        &OBM::toolBox::write_log( "Mise a jour du mot de passe SQL de l'utilisateur ".$parameters{"login"}." correctement effectuee.", "W", 2 );
    }
}

# On referme la connexion a la base
&OBM::toolBox::write_log( "Deconnexion de la base de donnees OBM", "W", 3 );
if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
    &OBM::toolBox::write_log( "Erreur: probleme lors de la fermeture de la base de donnees OBM...", "WC", 3 );
    exit 2;
}

# On ferme le log
&OBM::toolBox::write_log( "Execution du script 'ldapChangePasswd.pl' terminee", "WC", 0 );

exit 0
