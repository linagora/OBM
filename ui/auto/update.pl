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
my $return = GetOptions( \%parameters, 'user=s', 'domain-id=s', 'domain-global', 'domain-name=s', 'delegation=s', 'global', 'incremental', 'help' );

if( !$return ) {
    undef %parameters;
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

            $self->_log( 'Mise à jour terminée avec succés', -1 );
        }
    }

    return 0;
}


# Fonction de verification des parametres du script
sub getParameter {
    my $self = shift;
    my( $parameters ) = @_;

    local $SIG{__DIE__} = sub {
         $self->_log( 'Affichage de l\'aide', 3 );

        print STDERR "Veuillez indiquer les parametres de mise a jour :\n";
        print STDERR "\tSyntaxe: update.pl [--domain-id id | --domain-name domainName | --domain-global] [--user id | --delegation word] [--global | --incremental]\n";
        print STDERR "\tdomain-id <id> : domaine d'identifiant <id> ;\n";
        print STDERR "\tdomain-name <domainName> : domaine de domaine de messagerie <domainName> ;\n";
        print STDERR "\tdomain-global : domaine global ;\n";
        print STDERR "\tuser <id> : utilisateur d'identifiant <id> ;\n";
        print STDERR "\tdelegation <word> : delegation de mot cle <word> ;\n";
        print STDERR "\tglobal : fait une mise a jour globale du domaine ;\n";
        print STDERR "\tincremental : fait une mise a jour incrementale du domaine.\n";
        print STDERR "Un des paramètres '--domain-id', '--domain-name' ou '--domain-global' doit être indiqué. '--domain-id' est prioritaire.\n";
        print STDERR "Un des paramètres '--global' ou '--incremental' doit être indiqué.\n";

        exit 0;
    };


    if( exists( $parameters->{'help'} ) ) {
        die;
    }

    SWITCH: {
        if( $parameters->{'domain-id'} ) {
            last SWITCH;
        }

        if( exists($parameters->{'domain-global'}) ) {
            $self->_log( 'Obtention de l\'ID du domaine global', 3 );
            $parameters->{'domain-id'} = $self->_getGlobalDomainId();
            last SWITCH;
        }

        if( $parameters->{'domain-name'} ) {
            $self->_log( 'Obtention de l\'ID du domaine \''.$parameters->{'domain-name'}.'\'', 3 );
            $parameters->{'domain-id'} = $self->_getNameDomainId( $parameters->{'domain-name'} );
            last SWITCH;
        }

        die;
    }

    if( defined($parameters->{'domain-id'}) && ($parameters->{'domain-id'} =~ /^[0-9]+$/) ) {
        $self->_log( 'Mise a jour du domaine d\'identifiant \''.$parameters->{'domain-id'}.'\'', -1 );
        delete($parameters->{'domain-global'});
        delete($parameters->{'domain-name'});
    }else {
        $self->_log( 'Paramétre \'--domain-id\' manquant ou incorrect', 0 );
        die;
    }

    if( exists($parameters->{'user'}) ) {
        if( exists($parameters->{'delegation'}) ) {
            $self->_log( 'Trop de parametres de mise a jour precise', 0 );
            die;
        }else{
            $self->_log( 'Uniquement les mises a jour de l\'utilisateur d\'identifiant \''.$parameters->{'user'}.'\'', 0 );
        }

    }elsif( exists($parameters->{'delegation'}) ) {
        if( exists($parameters->{'user'}) ) {
            $self->_log( 'Trop de parametres de mise a jour precise', 0 );
            die;
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
            die;
        }

        if( $mode > 1 ) {
            $self->_log( 'un et un seul mode d\'exécution doit être indiqué [global|incremental]', 0 );
            die;
        }
    }
}


sub _getGlobalDomainId {
    my $self = shift;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return undef;
    }

    my $query = 'SELECT Domain.domain_id
                    FROM Domain
                    WHERE domain_global
                    LIMIT 1';

    
    my $sth;
    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $self->_log( 'Chargement de l\'ID du domaine global d\'OBM impossible', 3 );
        return 1;
    }

    my $rowResult = $sth->fetchrow_hashref();
    $sth->finish();

    return $rowResult->{'domain_id'};
}


sub _getNameDomainId {
    my $self = shift;
    my( $domainName ) = @_;

    if( !defined($domainName) ) {
        return undef;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return undef;
    }

    my $query = 'SELECT Domain.domain_id
                    FROM Domain
                    WHERE domain_name=\''.$domainName.'\'
                    LIMIT 1';

    my $sth;
    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $self->_log( 'Impossible de charger l\'ID du domaine OBM ayant pour domaine de messagerie principal \''.$domainName;'\'', 3 );
        return 1;
    }

    my $rowResult = $sth->fetchrow_hashref();
    $sth->finish();

    return $rowResult->{'domain_id'};
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
