#!/usr/bin/perl -w -T

package updateSieve;

use strict;
use OBM::Parameters::regexp;
use OBM::Tools::commonMethods qw(_log dump);

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

use Getopt::Long;
my %parameters;
my $return = GetOptions( \%parameters, 'login=s', 'domain-id=i', 'help' );

if( !$return ) {
    updateSieve->_displayHelp();
    exit 1;
}

exit updateSieve->run(\%parameters);

$| = 1;


sub run {
    my $self = shift;
    my( $parameters ) = @_;

    if( !defined($parameters) ) {
        $parameters->{'help'} = 1;
    }

    $self->_log( 'Analyse des paramètres du script', 3 );
    if( $self->_getParameter( $parameters ) ) {
        $self->_log( 'Erreur à l\'analyse des parametres du scripts', 0 );
        return 1;
    }

    require OBM::Update::updateSieve;
    my $updateSieve = OBM::Update::updateSieve->new( $parameters );
    my $errorCode = 0;
    if( defined($updateSieve) ) {
        $errorCode = $updateSieve->update();
    }else {
        $self->_log( 'problème à l\'initialisation du Sieve updater', 0 );
        $errorCode = 1;
    }

    if( $errorCode ) {
        $self->_log( 'échec de mise à jour du filtre Sieve', 0 );
    }else {
        $self->_log( 'mise à jour du filtre Sieve avec succés', 0 );
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

    # Check user name
    if( !exists($$parameters{'login'}) ) {
        $self->_log( 'Parametre --login manquant', 0 );
        return 1;
    }else {
        if( $$parameters{'login'} !~ /$regexp_login/ ) {
            $self->_log( 'Parametre --login invalide', 0 );
            return 1;
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

    print STDERR 'Script permettant de mettre à jour le filtre Sieve d\'une BAL'."\n";
    print STDERR 'Syntaxe :'."\n";
    print STDERR "\t".'updateSieve.pl --login LOGIN --domain-id DOMAIN_ID'."\n";

    return 0;
}


# Perldoc

=head1 NAME

updateSieve.pl - OBM administration tool to update user Sieve script

=head1 SYNOPSIS

  # Update Sieve script for user
  $ updateSieve.pl --login <LOGIN> --domain <DOMAIN_ID>

=head1 DESCRIPTION

This script is used bt OBM-UI to real-time Sieve operations like vacation or
redirection.

Allow users to modify their Sieve script without admin validation.

=head1 COMMANDS

=over 4

=item C<login> : only left part of login (before '@' for multi-domains)

=item C<domain> : domain BD ID

=back

This script is used for :

=over 4

=item enable/disable vacation

=item enable/disable redirection

=back

This script generate log via syslog.
