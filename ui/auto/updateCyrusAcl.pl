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



package updateCyrusAcl;

use OBM::Log::log;
@ISA = ('OBM::Log::log');

use strict;
use OBM::Parameters::regexp;

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

use Getopt::Long;
my %parameters;
my $return = GetOptions( \%parameters, 'type=s', 'name=s', 'domain-id=s', 'help' );

if( !$return ) {
    %parameters = undef;
}

my $updateCyrusAcl = updateCyrusAcl->new();
exit $updateCyrusAcl->run(\%parameters);

$| = 1;

sub new {
    my ($class) = @_;
    bless {}, $class;
}

sub run {
    my $self = shift;
    my( $parameters ) = @_;

    $self->_configureLog();

    if( !defined($parameters) ) {
        $parameters->{'help'} = 1;
    }

    $self->_log( 'Analyse des paramètres du script', 3 );
    if( $self->_getParameter( $parameters ) ) {
        $self->_log( 'Erreur à l\'analyse des parametres du scripts', 0 );
        return 1;
    }

    require OBM::Update::updateCyrusAcl;
    my $updateCyrusAcl = OBM::Update::updateCyrusAcl->new( $parameters );
    my $errorCode = 0;
    if( defined($updateCyrusAcl) ) {
        $errorCode = $updateCyrusAcl->update();
    }else {
        $self->_log( 'problème à l\'initialisation de l\'ACL updater', 0 );
        $errorCode = 1;
    }

    if( $errorCode ) {
        $self->_log( 'échec de mise à jour des ACLs Cyrus', 0 );
    }else {
        $self->_log( 'mise à jour des ACLs Cyrus avec succés', -1 );
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

    # Check updated entity type
    if( !exists($$parameters{'type'}) ) {
        $self->_log( 'Parametre --type manquant', 0 );
        return 1;
    }else {
        if( $$parameters{'type'} !~ /^(mailbox|mailshare)$/ ) {
            $self->_log( 'Parametre --type invalide', 0 );
            return 1;
        }
    }

    # Check entity name
    if( !exists($$parameters{'name'}) ) {
        $self->_log( 'Parametre --name manquant', 0 );
        return 1;
    }else {
        $$parameters{'name'} = lc($$parameters{'name'});
        if( ($$parameters{'type'} eq 'mailbox') && ($$parameters{'name'} !~ /$regexp_login/) ) {
            $self->_log( 'Parametre --name invalide', 0 );
            return 1;
        }elsif( ($$parameters{'type'} eq 'mailshare') && ($$parameters{'name'} !~ /$regexp_mailsharename/) ) {
            $self->_log( 'Parametre --name invalide', 0 );
        }
    }

    # Check domain ID
    if( !exists($$parameters{'domain-id'}) ) {
        $self->_log( 'Parametre --domain-id manquant', 0 );
        return 1;
    }else {
        if( $$parameters{'domain-id'} !~ /$regexp_id/ ) {
            $self->_log( 'Parametre --domain-id invalide', 0 );
            return 1;
        }
    }

    return 0;
}


# Display help
sub _displayHelp {
    my $self = shift;

    $self->_log( 'Affichage de l\'aide', 3 );

    print STDERR 'Script permettant de mettre à jour les ACLs Cyrus'."\n";
    print STDERR 'Syntaxe :'."\n";
    print STDERR "\t".'updateCyrusAcl.pl --type [mailbox|mailshare] --name NAME --domain-id DOMAIN_ID'."\n";

    return 0;
}


# Perldoc

=head1 NAME

updateCyrusAcl.pl - OBM administration tool to update Cyrus ACL

=head1 SYNOPSIS

  # Update user mailbox ACLs
  $ updateCyrusAcl.pl --type mailbox --name <LOGIN> --domain <DOMAIN_ID>

  # Update mailshare ACLs
  $ updateCyrusAcl.pl --type mailshare --name <MAILSHARE_NAME> --domain <DOMAIN_ID>

=head1 DESCRIPTION

This script is used by OBM-UI to real-time update Cyrus ACLs.

Allow users to modify their mailbox ACLs without admin validation.

Allow mailshare admins to modify mailshare ACLs without admin validation.

=head1 COMMANDS

=over 4

=item C<type> :

=over 4

=item B<mailbox> : update mailbox ACL

=item B<mailshare> : update mailshare ACL

=back

=item C<name> :

=over 4

=item B<LOGIN>, only left part of login (before '@' for multi-domains), if type
is 'mailbox'

=item B<MAILSHARE_NAME>, only left part of mailshare name (before '@' for
multi-domains), if type is 'mailshare'

=back

=item C<domain> : domain BD ID

=back

This script generate log via syslog.
