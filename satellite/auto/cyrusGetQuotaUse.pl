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


package cyrusGetQuotaUse;

use strict;
use OBM::Parameters::regexp;
use OBM::Tools::commonMethods qw(_log dump);

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

use Getopt::Long;
my %parameters;
my $return = GetOptions( \%parameters, 'domain-id=s', 'help' );

if( !$return ) {
    cyrusGetQuotaUse->_displayHelp();
    exit 1;
}

exit cyrusGetQuotaUse->run(\%parameters);

$| = 1;


# Do work...
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

    require OBM::Update::updateBdQuotaUse;
    my $updateBdQuotaUse = OBM::Update::updateBdQuotaUse->new( \%parameters );
    my $errorCode = 0;

    if( defined($updateBdQuotaUse) ) {
        $errorCode = $updateBdQuotaUse->update();
    }else {
        $self->_log( 'problème à l\'initialisation du cyrus quota used updater', 0 );
        $errorCode = 1;
    }

    if( $errorCode ) {
        $self->_log( 'échec de la mise à jour du quota Cyrus utilisé', 0 );
    }else {
        $self->_log( 'mise à jour du quota Cyrus utilisé avec succés', 0 );
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


    # Check 'domain-id' parameter
    if( !$$parameters{'domain-id'} ) {
            $self->_log( 'paramètre --domain-id non indiqué, traitement de tous les domaines', 0 );
    }else {
        if( $$parameters{'domain-id'} !~ /$regexp_id/ ) {
            $self->_log( 'paramètre --domain-id incorrect', 0 );
            return 1;
        }
    }

    return 0;
}


# Display Help
sub _displayHelp {
    my $self = shift;

    $self->_log( 'Affichage de l\'aide', 3 );

    print STDERR 'Script permettant de mettre à jour en BD le quota Cyrus utilisé'."\n";
    print STDERR 'Syntaxe :'."\n";
    print STDERR "\t".'cyrusGetQuotaUse.pl [--domain DOMAIN_ID]'."\n";

    return 0;
}


# Perldoc

=head1 NAME

cyrusGetQuotaUse.pl - OBM administration to udapte BD quota use

=head1 SYNOPSIS

    # Update quota use for all entites, with quota set, of any domains
    $ cyrusGetQuotaUse.pl

    # Update quota use for all entites, with quota set, of domain DOMAIN_ID
    $ cyrusGetQuotaUse.pl --domain DOMAIN_ID

=head1 COMMANDS

TO DO -- UPDATE -- TO DO -- UPDATE

=over 4

=item C<global> : global public contacts update

=item C<incremental> : incremental public contacts update

=item C<help> : display help

=back

This script must be run by system cron.

This script will do nothing if 'obm-contact' option, from 'obm_conf.ini', is false.

This script generate log via syslog.
