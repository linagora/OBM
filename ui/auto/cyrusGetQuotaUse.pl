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




package cyrusGetQuotaUse;

use OBM::Log::log;
@ISA = ('OBM::Log::log');

use strict;
use OBM::Parameters::regexp;

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

use Getopt::Long;
my %parameters;
my $return = GetOptions( \%parameters, 'domain-id=s', 'help' );

if( !$return ) {
    %parameters = undef;
}

my $cyrusGetQuotaUse = cyrusGetQuotaUse->new();
exit $cyrusGetQuotaUse->run(\%parameters);

$| = 1;


sub new {
    my $class = shift;
    my $self = bless { }, $class;

    $self->_configureLog();

    return $self;
}


sub DESTROY {
    my $self = shift;
}


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

        if( $errorCode ) {
            $self->_log( 'échec de la mise à jour du quota Cyrus utilisé', 0 );
        }else {
            $self->_log( 'mise à jour du quota Cyrus utilisé avec succés', -1 );
        }
    
        return $errorCode;
    }

    return 1;
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
        $self->_log( 'paramètre --domain-id non indiqué, traitement de tous les domaines', 2);
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

This script generates log via syslog.
