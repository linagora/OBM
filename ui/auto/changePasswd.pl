#!/usr/bin/perl -w -T
#################################################################################
# Copyright (C) 2011-2014 Linagora
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option) any
# later version, provided you comply with the Additional Terms applicable for OBM
# software by Linagora pursuant to Section 7 of the GNU Affero General Public
# License, subsections (b), (c), and (e), pursuant to which you must notably (i)
# retain the displaying by the interactive user interfaces of the “OBM, Free
# Communication by Linagora” Logo with the “You are using the Open Source and
# free version of OBM developed and supported by Linagora. Contribute to OBM R&D
# by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
# links between OBM and obm.org, between Linagora and linagora.com, as well as
# between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
# from infringing Linagora intellectual property rights over its trademarks and
# commercial brands. Other Additional Terms apply, see
# <http://www.linagora.com/licenses/> for more details.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License and
# its applicable Additional Terms for OBM along with this program. If not, see
# <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
# version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
# applicable to the OBM software.
#################################################################################




package changePasswd;

use OBM::Log::log;
@ISA = ('OBM::Log::log');

use strict;
use OBM::Parameters::regexp;

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

use Getopt::Long;
my %parameters;
my $return = GetOptions( \%parameters, 'login=s', 'domain-id=s', 'passwd=s', 'old-passwd=s', 'unix', 'samba', 'sql', 'interactiv', 'no-old', 'help' );

if( !$return ) {
    %parameters = undef;
}

my $changePasswd = changePasswd->new();
exit $changePasswd->run(\%parameters);

$| = 1;


sub new {
    my $class = shift;
    my $self = bless { }, $class;

    $self->_configureLog();

    return $self;
}


sub run {
    my $self = shift;
    my( $parameters ) = @_;

    if( !defined($parameters) ) {
        $parameters->{'help'} = 1;
    }

    $self->_log( 'Analyse des paramètres du script', 3 ); 
    if( $self->_getParameter( $parameters ) ) {
        $self->_log( 'erreur à l\'analyse des parametres du scripts', 0 );
        return 1;
    }

    require OBM::Update::updatePassword;
    my $updatePasswd = OBM::Update::updatePassword->new( $parameters );
    my $errorCode = 0;
    if( defined($updatePasswd) ) {
        $errorCode = $updatePasswd->update();
    }else {
        $self->_log( 'problème à l\'initialisation du password updater', 0 );
        $errorCode = 1;
    }

    if( $errorCode ) {
        $self->_log( 'échec de mise à jour du mot de passe', 0 );

        if( $parameters->{interactiv} ) {
            print STDERR 'Password update failed'."\n";
        }
    }else {
        $self->_log( 'mot de passe mis à jour avec succés', -1 );

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


    # Check 'domain-id' parameter
    if( !$$parameters{'domain-id'} ) {
        $self->_log( 'Parametre --domain-id manquant', 0 );
        return 1;

    }else {
        if( $$parameters{'domain-id'} !~ /$regexp_id/ ) {
            $self->_log( 'Parametre --domain-id incorrect', 0 );
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
    print STDERR "\t".'changePasswd.pl --login LOGIN --domain-id DOMAIN_ID --interactiv [--no-old] [--unix|--samba|--sql]'."\n";
    print STDERR "\t".'changePasswd.pl --login LOGIN --domain-id DOMAIN_ID --passwd NEW_PASSWD [--no-old|--old-passwd OLD_PASSWD] [--unix|--samba|--sql]'."\n";
    print STDERR "Par défaut, seul le mot de passe Unix est mis à jour\n";

    return 0;
}
