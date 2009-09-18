#!/usr/bin/perl -w -T
#+-------------------------------------------------------------------------+
#|   Copyright (c) 1997-2009 OBM.org project members team                  |
#|                                                                         |
#|  This program is free software; you can redistribute it and/or          |
#|  modify it under the terms of the GNU General Public License            |
#|  as published by the Free Software Foundation; version 2                |
#|  of the License.                                                        |
#|                                                                         |
#|  This program is distributed in the hope that it will be useful,        |
#|  but WITHOUT ANY WARRANTY; without even the implied warranty of         |
#|  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          |
#|  GNU General Public License for more details.                           | 
#+-------------------------------------------------------------------------+
#|  http://www.obm.org                                                     |
#+-------------------------------------------------------------------------+

#####################################################################
# OBM               - File : ldapContacts.pl                        #
#                   - Desc : Script permettant de gérer le contenu  #
#                   de la branche des contacts publics              #
#####################################################################

package ldapContacts;

use strict;
use OBM::Parameters::regexp;
use OBM::Tools::commonMethods qw(_log dump);

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

use Getopt::Long;
my %parameters;
my $return = GetOptions( \%parameters, 'global', 'incremental', 'help' );

if( !$return ) {
    ldapContacts->_displayHelp();
    exit 1;
}

exit ldapContacts->run(\%parameters);

$| = 1;


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

    require OBM::Update::updateContacts;
    my $updateContacts = OBM::Update::updateContacts->new( $parameters );

    my $errorCode = 0;
    if( !defined($updateContacts) ) {
        $self->_log( 'problème à l\'initialisation du contact updater', 0 );
        $errorCode = 1;

    }elsif( ref($updateContacts) ) {
        $errorCode = $updateContacts->update();
    }

    if( $errorCode ) {
        $self->_log( 'échec de mise à jour des contacts', 0 );
    }else {
        $self->_log( 'mise à jour des contacts avec succés', -1 );
    }

    return $errorCode;
}


sub _getParameter {
    my $self = shift;
    my( $parameters ) = @_;

    if( $$parameters{'help'} ) {
        $self->_displayHelp();
        return 1;
    }


    SWITCH: {
        if( exists($parameters->{'incremental'}) && exists($parameters->{'global'}) ) {
            $self->_log( 'parametres \'--incremental\' et \'--global\' incompatibles', 0 );
            return 1;
        }
        
        if( exists($parameters->{'incremental'}) ) {
            $self->_log( 'Mise a jour incrementale', 0 );
            $parameters->{'incremental'} = 1;
            $parameters->{'global'} = 0;
            last SWITCH;
        }
        
        if( exists($parameters->{'global'}) ) {
            $self->_log( 'Mise a jour globale', 0 );
            $parameters->{'incremental'} = 0;
            $parameters->{'global'} = 1;
            last SWITCH;
        }

        $self->_log( 'Mise a jour globale', 0 );
        $parameters->{'incremental'} = 0;
        $parameters->{'global'} = 1;
    }

    return 0;
}


sub _displayHelp {
    my $self = shift;

    $self->_log( 'Affichage de l\'aide', 3 );

    print STDERR 'Script permettant de faire une synchronisation des contacts publics d\'OBM dans une branche de l\'annuaire LDAP (ou=contacts par défaut)'."\n\n";

    print STDERR 'Veuillez indiquer le critere de mise a jour :'."\n";
    print STDERR 'Syntaxe: $0 [--global | --incremental]'."\n";
    print STDERR "\t".'--global : Fait une mise a jour globale des contacts ;'."\n";
    print STDERR "\t".'--incremental : Fait une mise a jour incrementale du domaine (Option par defaut)'."\n";

    return 0;
}


#
## Perldoc
#
#=head1 NAME
#
#ldapContacts.pl - OBM administration to publish public contact in LDAP
#
#=head1 SYNOPSIS
#
#    # Publish all public contacts for all OBM domains
#    $ ldapContacts.pl --global
#
#    # Incrmental LDAP public contacts update for last script execution
#    $ ldapContacts.pl --incremental
#
#=head1 COMMANDS
#
#=over 4
#
#=item C<global> : global public contacts update
#
#=item C<incremental> : incremental public contacts update
#
#=item C<help> : display help
#
#=back
#
#This script must be run by system cron.
#
#This script will do nothing if 'obm-contact' option, from 'obm_conf.ini', is false.
#
#This script generate log via syslog.
