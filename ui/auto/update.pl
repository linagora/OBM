#!/usr/bin/perl -w -T
#####################################################################
# OBM               - File : update.pl                              #
#                   - Desc : Script permettant de mettre à jour le  #
#                   système de façon incrémentale                   #
#####################################################################

package update;

use strict;
use OBM::Tools::commonMethods qw(_log dump);

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

use Getopt::Long;
my %parameters;
my $return = GetOptions( \%parameters, 'user=s', 'domain=s', 'delegation=s', 'global', 'incremental', 'help' );

if( !$return ) {
    %parameters = undef;
}

update->run(\%parameters);
exit;

$|=1;


sub run {
    my $self = shift;
    my( $parameters ) = @_;

    if( !defined($parameters) ) {
        $parameters->{'help'} = 1;
    }

    # Traitement des paramètres
    $self->_log( 'Analyse des parametres du script', 3 );
    $self->getParameter( $parameters );
    
    
    my $update;
    if( $parameters->{'global'} ) {
        require OBM::Update::updateGlobal;
        $update = OBM::Update::updateGlobal->new( $parameters );
    }else {
        require OBM::Update::updateGlobal;
        $update = OBM::Update::updateGlobal->new( $parameters );
    }
    
    if( !defined($update) ) {
        $self->_log( 'Probleme a l\'initialisation de l\'objet de mise a jour', 0 );
    }else {
        $update->update();
    }

    return 0;
}


# Fonction de verification des parametres du script
sub getParameter {
    my $self = shift;
    my( $parameters ) = @_;

    if( !exists($parameters->{'domain'}) ) {
        $self->_log( 'Parametre \'--domain\' manquant', 0 );
        $parameters->{'help'} = 1;

    }else {
        $self->_log( 'Mise a jour du domaine d\'identifiant \''.$parameters->{'domain'}.'\'', 0 );
    }
    
    if( exists($parameters->{'user'}) ) {
        if( exists($parameters->{'delegation'}) ) {
            $self->_log( 'Trop de parametres de mise a jour precise', 0 );
            $parameters->{'help'} = 1;
        }else{
            $self->_log( 'Uniquement les mises a jour de l\'utilisateur d\'identifiant \''.$parameters->{'user'}.'\'', 0 );
        }

    }elsif( exists($parameters->{'delegation'}) ) {
        if( exists($parameters->{'user'}) ) {
            $self->_log( 'Trop de parametres de mise a jour precise', 0 );
            $parameters->{'help'} = 1;
        }else {
            $self->_log( 'Uniquement les mises a jour de la delegation \''.$parameters->{'delegation'}.'\'', 0 );
        }

    }

    if( exists($parameters->{'incremental'}) && exists($parameters->{'global'}) ) {
        $self->_log( 'parametres \'--incremental\' et \'--global\' incompatibles', 0 );
        $parameters->{'help'} = 1;

    }elsif( exists($parameters->{'incremental'}) ) {
        $parameters->{'incremental'} = 1;
        $self->_log( 'Mise a jour incrementale', 0 );

    }elsif( exists($parameters->{'global'}) || !(exists($parameters->{'incremental'}) || exists($parameters->{'global'})) ) {
        $parameters->{'global'} = 1;
        $self->_log( 'Mise a jour globale', 0 );

    }


    if( exists( $parameters->{'help'} ) ) {
        $self->_log( 'Affichage de l\'aide', 3 );

        print STDERR "Veuillez indiquer le critere de mise a jour :\n";
        print STDERR "\tSyntaxe: script --domain id [--user id | --delegation word] [--global | --incremental]\n";
        print STDERR "\tuser <id> : utilisateur d'identifiant <id> ;\n";
        print STDERR "\tdomain <id> : domaine d'identifiant <id> ;\n";
        print STDERR "\tdelegation <word> : delegation de mot cle <word> ;\n";
        print STDERR "\tglobal : fait une mise a jour globale du domaine - action par defaut ;\n";
        print STDERR "\tincremental : fait une mise a jour incrementale du domaine.\n";

        exit 0;
    }
}


# Perldoc

=head1 NAME

update.pl - OBM administration tool , alter ego of Cyrus::IMAP::Shell

=head1 SYNOPSIS

  # Domain global update
  $ update.pl --domain <DOMAIN_ID> --global

  # Domain incremental update
  $ update.pl --domain <DOMAIN_ID> --incremental

  # Domain incremental update - only updates done by an admin
  $ update.pl --domain <DOMAIN_ID> --user <USER_ID> --incremental

  # Domain incremental update - only updates done for a delegation
  $ update.pl --domain <DOMAIN_ID> --delegation <DELEGATION> --incremental

  # Display help
  $ update.pl --help

=head1 DESCRIPTION

This script is used by OBM-UI when an admin apply updates.

Global update apply all datas for a domain in the system regardless of BD
updates.

Incremental update apply only updates mark by the scope. It's possible to apply
updates for only a particular user or for a delegation.

=head1 COMMANDS

=over 4

=item C<help> : display help

=item C<domain> : B<needed>

=over 4

=item domain BD ID

=back

=item C<global> : global update

=item C<incremental> : incremental update

=item C<user> : apply updates done by only this user

=item C<delegation> : apply update done by only this delegation

=back

Global update by default.

Parameters 'user' and 'delegation' are exclusive.

This script generate log via syslog.
