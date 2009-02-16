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


package update;

use strict;
use OBM::Tools::commonMethods qw(_log dump);

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

use Getopt::Long;
my %parameters;
my $return = GetOptions( \%parameters, 'user=s', 'domain-id=s', 'delegation=s', 'global', 'incremental', 'help' );

if( !$return ) {
    %parameters = undef;
}

exit update->run(\%parameters);

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
    SWITCH: {
        if( $parameters->{'incremental'} ) {
            require OBM::Update::updateIncremental;
            $update = OBM::Update::updateIncremental->new( $parameters );
            last SWITCH;
        }

        if( $parameters->{'global'} ) {
            require OBM::Update::updateGlobal;
            $update = OBM::Update::updateGlobal->new( $parameters );
            last SWITCH;
        }
    }

    if( !defined($update) ) {
        $self->_log( 'Probleme a l\'initialisation de l\'objet de mise a jour', 0 );
    }else {
        if( my $code = $update->update() ) {
            $self->_log( 'La mise à jour ne s\'est pas correctement déroulée', 0 );
            return $code;
        }else {
            require OBM::updateStateUpdater;
            my $updateState;
            if( !($updateState = OBM::updateStateUpdater->new( $parameters->{'domain-id'} )) || $updateState->update() ) {
                $self->_log( 'échec de la mise à jour du flag de mise à jour en attente', 0 );
                return 1;
            }

            $self->_log( 'Mise à jour terminée avec succés', 0 );
        }
    }

    return 0;
}


# Fonction de verification des parametres du script
sub getParameter {
    my $self = shift;
    my( $parameters ) = @_;

    if( !exists($parameters->{'domain-id'}) ) {
        $self->_log( 'Paramétre \'--domain-id\' manquant', 0 );
        $parameters->{'help'} = 1;

    }else {
        $self->_log( 'Mise a jour du domaine d\'identifiant \''.$parameters->{'domain-id'}.'\'', 0 );
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

    my $mode = 0;
    if( $parameters->{'incremental'} ) {
        $parameters->{'incremental'} = 1;
        $mode++;
    }

    if( $parameters->{'global'} ) {
        $parameters->{'global'} = 1;
        $mode++;
    }

    SWITCH: {
        if( $mode == 0 ) {
            $self->_log( 'un paramètre de mode d\'exécution doit être indiqué [global|incremental]', 0 );
            $parameters->{'help'} = 1;
            last SWITCH;
        }

        if( $mode > 1 ) {
            $self->_log( 'un et un seul mode d\'exécution doit être indiqué [global|incremental]', 0 );
            $parameters->{'help'} = 1;
            last SWITCH;
        }
    }


    if( exists( $parameters->{'help'} ) ) {
        $self->_log( 'Affichage de l\'aide', 3 );

        print STDERR "Veuillez indiquer le critere de mise a jour :\n";
        print STDERR "\tSyntaxe: script --domain-id id [--user id | --delegation word] [--global | --incremental]\n";
        print STDERR "\tuser <id> : utilisateur d'identifiant <id> ;\n";
        print STDERR "\tdomain-id <id> : domaine d'identifiant <id> ;\n";
        print STDERR "\tdelegation <word> : delegation de mot cle <word> ;\n";
        print STDERR "\tglobal : fait une mise a jour globale du domaine ;\n";
        print STDERR "\tincremental : fait une mise a jour incrementale du domaine.\n";

        exit 0;
    }
}


# Perldoc

=head1 NAME

update.pl - OBM administration tool, alter ego of Cyrus::IMAP::Shell

=head1 SYNOPSIS

  # Domain global update
  $ update.pl --domain-id <DOMAIN_ID> --global

  # Domain incremental update
  $ update.pl --domain-id <DOMAIN_ID> --incremental

  # Domain incremental update - only updates done by an admin
  $ update.pl --domain-id <DOMAIN_ID> --user <USER_ID> --incremental

  # Domain incremental update - only updates done for a delegation
  $ update.pl --domain-id <DOMAIN_ID> --delegation <DELEGATION> --incremental

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

=item C<domain-id> : B<needed>

=over 4

=item domain BD ID

=back

=item C<global> : global update

=item C<incremental> : incremental update

=item C<user> : apply updates done by only this user

=item C<delegation> : apply update done by only this delegation

=back

Parameters 'user' and 'delegation' are exclusive.

This script generate log via syslog.
