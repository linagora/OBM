#!/usr/bin/perl -w -T -C7
#####################################################################
# OBM               - File : changePasswd.pl                        #
#                   - Desc : Script permettant de modifier le mot   #
#                   de passe de l'utilisateur dont le login et le   #
#                   domaine sont passes en paramètre                #
#####################################################################

package changePasswd;

use strict;
use OBM::Parameters::regexp;
use OBM::Tools::commonMethods qw(_log dump);

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

use Getopt::Long;
my %parameters;
my $return = GetOptions( \%parameters, 'login=s', 'domain=s', 'passwd=s', 'old-passwd=s', 'unix', 'samba', 'sql', 'interactiv', 'no-old', 'help' );

if( !$return ) {
    %parameters = undef;
}

exit changePasswd->run(\%parameters);

$| = 1;


sub run {
    my $self = shift;
    my( $parameters ) = @_;

    if( !defined($parameters) ) {
        $parameters->{'help'} = 1;
    }

    # Traitement des paramètres
    $self->_log( 'Analyse des parametres du script', 3 ); 
    if( $self->_getParameter( $parameters ) ) {
        $self->_log( 'Erreur à l\'analyse des parametres du scripts', 0 );
        return 1;
    }

    require OBM::Update::updatePassword;
    my $updatePasswd = OBM::Update::updatePassword->new( \%parameters );
    my $errorCode = 0;
    if( defined($updatePasswd) ) {
        $errorCode = $updatePasswd->update();
    }else {
        $self->_log( 'problème à l\'initialisation du password updater', 0);
        return 1;
    }

    if( $errorCode ) {
        $self->_log( 'échec de mise à jour du mot de passe', 0 );

        if( $parameters->{interactiv} ) {
            print STDERR 'Password update failed'."\n";
        }
    }else {
        $self->_log( 'mot de passe mis à jour avec succès', 0 );

        if( $parameters->{interactiv} ) {
            print STDERR 'Password update success'."\n";
        }
    }

    return $errorCode;
}


# Check script parameters
sub _getParameter {
    my $self = shift;
    my( $parameters ) = @_;

    if( $$parameters{'help'} ) {
        $self->_displayHelp();
        return 1;
    }


    # Check 'login' parameter
    if( !$$parameters{'login'} ) {
        $self->_log( 'Parametre --login manquant', 0 );
        return 1;

    }else {
        if( !($$parameters{'login'} =~ /$regexp_login/) ) {
            $self->_log( 'Parametre --login invalide', 0 );
            return 1;
        }
    }


    # Check 'domain' parameter
    if( !$$parameters{'domain'} ) {
        $self->_log( 'Parametre --domain manquant', 0 );
        return 1;

    }else {
        if( $$parameters{'domain'} !~ /$regexp_id/ ) {
            $self->_log( 'Parametre --domain incorrect', 0 );
            return 1;
        }
    }


    # If no password on command line
    if( !$$parameters{'interactiv'} && !$$parameters{'passwd'} ) {
        $self->_log( 'Mot de passe non indiqué sur la ligne de commande, utilisation du  mode interactif', 0 );
        $$parameters{'interactiv'} = 1;
    }


    if( !$$parameters{'no-old'} ) {
        # Check old password
        SWITCH: {
            if( !$$parameters{'interactiv'} && !$$parameters{'old-passwd'} ) {
                $self->_log( 'Mode non interactif - l\'ancien mot de passe doit être spécifié sur la ligne de commande', 0 );

                return 1;
            }

            if( $$parameters{'interactiv'} ) {
                $self->_log( 'Mode interactif - saisie de l\'ancien mot de passe depuis l\'entrée standard.', 1 );
            
                print 'Old password: ';
                system('/bin/stty', '-echo'); # Disable echo
                $$parameters{'old-passwd'} = <STDIN>;
                system('/bin/stty', 'echo'); # Enable echo

                # Just for better displaying
                print "\n";

                $self->_log( 'Mode interactif - ancien mot de passe lu depuis l\'entrée standard', 2 );

                chop( $$parameters{'old-passwd'} );
            }
        }

        # On log level 4, old password will be log
        $self->_log( 'Ancien mot de passe : '.$$parameters{'old-passwd'}, 4 );
    }


    # Checking new password
    SWITCH: {
        # If no new password is given
        if( !$$parameters{'interactiv'} && !$$parameters{'passwd'} ) {
            $self->_log( 'Mode non interactif - vous devez indiquer le mot de passe sur la ligne de commande', 0 );

            return 1;
        }

        # If interactive mode and password on command line, password on command
        # line is used
        if( !$$parameters{'interactiv'} && ( $$parameters{'passwd'} ) ) {
            $self->_log( 'Mode non interactif - le mot de passe est passé en paramètre', 2 ); 

            last SWITCH;
        }

        # On interactive mode, read password from STDIN
        if( $$parameters{'interactiv'} ) {
            $self->_log( 'Mode interactif - saisie du nouveau mot de passe depuis l\'entrée standard', 2 );
            print 'New password: ';
            system('/bin/stty', '-echo'); # Disable echo
            my $newPasswd = <STDIN>;
            system('/bin/stty', 'echo'); # Enable echo
            chop( $newPasswd );
            $self->_log( 'Mode interactif - nouveau mot de passe lu depuis l\'entree standard', 2 );
            $self->_log( 'Mode interactif - 1° saisie du nouveau mot de passe \''.$newPasswd.'\'', 4 );
            
            $self->_log( 'Mode interactif - deuxième saisie du nouveau mot de passe depuis l\'entrée standard', 2 );
            print "\n".'Re-type new password: ';
            system('/bin/stty', '-echo'); # Disable echo
            my $newPasswd2 = <STDIN>;
            system('/bin/stty', 'echo'); # Enable echo
            chop( $newPasswd2 );
            $self->_log( 'Mode interactif - nouveau mot de passe re-lu depuis l\'entrée standard', 2 );
            $self->_log( 'Mode interactif - 2° saisie du nouveau mot de passe \''.$newPasswd2.'\'', 4 );

            # Just for better displaying
            print "\n";

            if( $newPasswd ne $newPasswd2 ) {
                $self->_log( 'Mode interactif - les nouveaux mots de passes ne correspondent pas', 0 );
                return 1;

            }else {
                $$parameters{'passwd'} = $newPasswd;

            }
        }
    }

    # Check password syntax
    if( $$parameters{'passwd'} !~ /$regexp_passwd/ ) {
        if( $$parameters{'interactiv'} ) {
            print STDERR 'Le nouveau mot de passe ne respecte pas la syntaxe requise - \''.$regexp_passwd."'\n";
        }
        $self->_log( 'Le nouveau mot de passe ne respecte pas la syntaxe requise - \''.$regexp_passwd.'\'', 0 );

        return 1;
    }

    # On log level 4, log password
    $self->_log( 'Nouveau mot passe : '.$$parameters{'passwd'}, 4 );


    # Which password is to be updated - by default, only UNIX password
    SWITCH: {
        if( !$$parameters{'unix'} && !$$parameters{'samba'} && !$$parameters{'sql'} ) {
            $$parameters{'unix'} = 1;
        }
   

        # Update 'userPassword' LDAP attribut - UNIX password
        if( $$parameters{'unix'} ) {
           $self->_log( 'Modification du mot de passe UNIX', 2 );
        }else {
           $self->_log( 'Pas de modification du mot de passe UNIX', 2 );
        }


        # Update NT and LM password LDAP attribut
        if( $$parameters{'samba'} ) {
            $self->_log( 'Modification du mot de passe Samba', 2 );
        }else {
            $self->_log( 'pas de modification du mot de passe Samba', 2 );
        }


        # Update SQL password
        if( $$parameters{'sql'} ) {
            $self->_log( 'modification du mot de passe Sql', 2 );
        }else {
            $self->_log( 'pas de modification du mot de passe Sql', 2 );
        }
    }

    return 0;
}


sub _displayHelp {
    my $self = shift;

    $self->_log( 'Affichage de l\'aide', 3 );

    print STDERR 'Script permettant de mettre à jour les mots de passes SQL et/ou LDAP'."\n";
    print STDERR 'Pour plus d\'informations : perldoc path_to/changePasswd.pl'."\n";
    print STDERR 'Syntaxe :'."\n";
    print STDERR "\t".'changePasswd.pl --login LOGIN --domain DOMAIN_ID --interactiv [--no-old] [--unix|--samba|--sql]'."\n";
    print STDERR "\t".'changePasswd.pl --login LOGIN --domain DOMAIN_ID --passwd NEW_PASSWD [--no-old|--old-passwd OLD_PASSWD] [--unix|--samba|--sql]'."\n";
    print STDERR "Par défaut, seul le mot de passe Unix est mis à jour\n";

    return 0;
}


## On prépare le log
#my ($scriptname) = ($0=~'.*/([^/]+)');
#$self->_log( $scriptname.': ', "O", 0 );
#
## Traitement des paramètres
#$self->_log( "Analyse des parametres du script", "W", 3 );
#my %parameters;
#my $getParamRet = getParameter( \%parameters );
#if( ($getParamRet == 2) || ($parameters{'interactiv'} && $getParamRet) ) {
#    $self->_log( 'Affichage de l\'aide', 'WC', 2 );
#    exit 0;
#
#}elsif( $getParamRet ) {
#    $self->_log( "", "C" );
#    exit 1;
#}
#
## On se connecte a la base
#my $dbHandler = OBM::Tools::obmDbHandler->instance();
#if( !defined($dbHandler) ) {
#    $self->_log( 'Probleme lors de l\'ouverture de la base de donnees', 'WC', 0 );
#    exit 2;
#}
#
#
#my $updatePasswd = OBM::Update::updatePassword->new( \%parameters );
#my $errorCode = 0;
#if( defined($updatePasswd) ) {
#    $errorCode = $updatePasswd->update();
#}
#
#
## On referme la connexion à la base
#$dbHandler->destroy();
#
#
## On ferme le log
#$self->_log( "Execution du script terminee", "WC", 0 );
#
#exit !$errorCode;

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
  #     - SQL 'userobm_password' column for
  #     'UserObm' and 'P_UserObm' tables
  $ changePasswd.pl --login <LOGIN> --domain <DOMAIN_ID> --interactiv --unix --samba --sql

  # Pass new and old password on command line, update only LDAP 'userPassword'
  # attribute
  $ changePasswd.pl --login <LOGIN> --domain <DOMAIN_ID> --passwd <NEW_PASSWD> --old-passwd <OLD_PASSWD>

  # Pass only new password on command line, update only LDAP 'userPassword'
  # attribute
  $ changePasswd.pl --login <LOGIN> --domain <DOMAIN_ID> --passwd <NEW_PASSWD> --no-old

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

=over 4

=item B<PLAIN> : plain password

=back

=item C<passwd> : new password

=item C<old-passwd> : old password

=back

If none of 'passwd' and 'interactiv' options are specified, then script run in
interactive mode.

Options 'passwd' and 'old-passwd' are exclusive with 'interactiv'.

Mode non interactive have priority on 'interactiv' option.

Options 'sql', 'unix' and 'samba' can be used at the same time.

This script generate log via syslog.
