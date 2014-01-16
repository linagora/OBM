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



package update;

use OBM::Log::log;
@ISA = ('OBM::Log::log');

use strict;

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

use Getopt::Long;
my %parameters;
my $return = GetOptions( \%parameters, 'user=s', 'domain=s', 'domain-id=s', 'domain-global', 'domain-name=s', 'delegation=s', 'global', 'incremental', 'entity', 'delete', 'help' );

if( !$return ) {
    %parameters = undef;
}

my $update = update->new();
exit $update->run(\%parameters);

$|=1;


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

        if( $parameters->{'entity'} ) {
            require OBM::Update::updateEntity;
            $update = OBM::Update::updateEntity->new( $parameters );
            last SWITCH;
        }

        if( $parameters->{'delete'} ) {
            require OBM::Update::deleteDomain;
            $update = OBM::Update::deleteDomain->new( $parameters );
            last SWITCH;
        }
    }

    if( !defined($update) ) {
        $self->_log( 'Probleme a l\'initialisation de l\'objet de mise a jour', 0 );
    }else {
        if( my $code = $update->update() ) {
            $self->_log( 'La mise à jour ne s\'est pas correctement déroulée', 0 );
            return $code;
        }elsif( $parameters->{'delete'} ) {
            $self->_log( 'Suppression de domaine terminée avec succés', -1 );
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
        print STDERR "Syntaxe:\n";
        print STDERR "\tupdate.pl [--domain-id id | --domain-name domainName | --domain-global] [--user id | --delegation word] [--global | --incremental]\n";
        print STDERR "\tupdate.pl [--domain-id id | --domain-name domainName | --domain-global] --entity\n";
        print STDERR "\tupdate.pl [--domain-id id | --domain-name domainName | --domain-global] --delete\n";
        print STDERR "\t\tdomain-id <id> : domaine d'identifiant <id> ;\n";
        print STDERR "\t\tdomain-name <domainName> : domaine de domaine de messagerie <domainName> ;\n";
        print STDERR "\t\tdomain-global : domaine global ;\n";
        print STDERR "\t\tuser <id> : utilisateur d'identifiant <id> ;\n";
        print STDERR "\t\tdelegation <word> : delegation de mot cle <word> ;\n";
        print STDERR "\t\tglobal : fait une mise a jour globale du domaine ;\n";
        print STDERR "\t\tincremental : fait une mise a jour incrementale du domaine.\n";
        print STDERR "\t\tentity : fait une mise a jour par entité. Les entités à mettre à jour sont indiquées sur l'entrée standard sous la forme 'type:nom', un par ligne\n";
        print STDERR "\t\t\ttype : [user|mailshare|group|host]\n";
        print STDERR "\t\t\tname : nom/identifiant de l'entité\n";
        print STDERR "\t\tdelete : suppression d'un domaine.\n";
        print STDERR "Un des paramètres '--domain-id', '--domain-name' ou '--domain-global' doit être indiqué. '--domain-id' est prioritaire.\n";
        print STDERR "Un et un seul des paramètres '--global', '--incremental', '--entity' ou '--delete' peuvent être indiqués à la fois.\n";

        exit 0;
    };


    if( exists( $parameters->{'help'} ) ) {
        die;
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

    if( $parameters->{'entity'} ) {
        $parameters->{'entity'} = 1;
        $mode++;
    }

    if( $parameters->{'delete'} ) {
        $parameters->{'delete'} = 1;
        $mode++;
    }

    SWITCH: {
        if( $mode == 0 ) {
            $self->_log( 'un paramètre de mode d\'exécution doit être indiqué [global|incremental|entity]', 0 );
            die;
        }

        if( $mode > 1 ) {
            $self->_log( 'un et un seul mode d\'exécution doit être indiqué [global|incremental]', 0 );
            die;
        }
    }


    if( defined($parameters->{'delegation'}) && $parameters->{'delegation'} eq '' ) {
        delete($parameters->{'delegation'});
    }

    if( defined($parameters->{'user'}) && $parameters->{'user'} !~ /^\d+$/ ) {
        delete($parameters->{'user'});
    }

    SWITCH: {
        if( $parameters->{'delete'} ) {
            delete($parameters->{'user'});
            delete($parameters->{'delegation'});
            last SWITCH;
        }


        if( $parameters->{'incremental'} || $parameters->{'global'} ) {
            if( exists($parameters->{'user'}) ) {
                if( exists($parameters->{'delegation'}) ) {
                    $self->_log( 'Trop de parametres de mise a jour precise', 0 );
                    die;
                }else{
                    $self->_log( 'Uniquement les mises a jour de l\'utilisateur d\'identifiant \''.$parameters->{'user'}.'\'', 2 );
                }
        
            }elsif( exists($parameters->{'delegation'}) ) {
                if( exists($parameters->{'user'}) ) {
                    $self->_log( 'Trop de parametres de mise a jour precise', 0 );
                    die;
                }else {
                    $self->_log( 'Uniquement les mises a jour de la delegation \''.$parameters->{'delegation'}.'\'', 2 );
                }
            }

            last SWITCH;
        }


        if( $parameters->{'entity'} ) {
            $self->_log( 'Lit les entités à mettre à jour depuis l\'entrée standard', 3 );
            @{$parameters->{'updateEntityList'}} = <STDIN>;
            last SWITCH;
        }
    }


    SWITCH: {
        if( $parameters->{'domain-id'} ) {
            $self->_log( 'Obtention du nom du domaine d\'ID \''.$parameters->{'domain-id'}.'\'', 5 );
            $parameters->{'domain-name'} = $self->_getIdDomainName( $parameters->{'domain-id'}, $parameters->{'delete'} );
            last SWITCH;
        }

        # --domain is deprecated from OBM 2.3 and may be remove on OBM 2.4
        if( $parameters->{'domain'} ) {
            $parameters->{'domain-id'} = $parameters->{'domain'};
            $self->_log( 'Obtention du nom du domaine d\'ID \''.$parameters->{'domain-id'}.'\'', 5 );
            $parameters->{'domain-name'} = $self->_getIdDomainName( $parameters->{'domain-id'}, $parameters->{'delete'} );
            last SWITCH;
        }

        if( exists($parameters->{'domain-global'}) ) {
            $self->_log( 'Obtention de l\'ID du domaine global', 5 );
            ( $parameters->{'domain-id'}, $parameters->{'domain-name'} ) = $self->_getGlobalDomainId( $parameters->{'delete'} );
            last SWITCH;
        }

        if( $parameters->{'domain-name'} ) {
            $self->_log( 'Obtention de l\'ID du domaine \''.$parameters->{'domain-name'}.'\'', 5 );
            $parameters->{'domain-id'} = $self->_getNameDomainId( $parameters->{'domain-name'}, $parameters->{'delete'} );
            last SWITCH;
        }
    }

    if( defined($parameters->{'domain-id'}) && ($parameters->{'domain-id'} =~ /^[0-9]+$/) ) {
        if( defined($parameters->{'domain-name'}) ) {
            $self->_log( 'Mise a jour du domaine \''.$parameters->{'domain-name'}.'\' (ID: '.$parameters->{'domain-id'}.')', 3);
        }else {
            $self->_log( 'Mise a jour du domaine d\'ID '.$parameters->{'domain-id'}, 3 );
        }
    }else {
        $self->_log( 'Paramétre \'--domain-id\' ou \'--domain-name\' ou \'--domain-global\' manquant ou incorrect', 0 );
        print STDERR 'Paramétre \'--domain-id\' ou \'--domain-name\' ou \'--domain-global\' manquant ou incorrect, vérifiez les fichiers journaux'."\n";
        die;
    }
}


sub _getGlobalDomainId {
    my $self = shift;
    my( $productionTable ) = @_;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return undef;
    }

    my $table = 'Domain';
    if( $productionTable ) {
        $table = 'P_'.$table;
    }

    my $query = 'SELECT '.$table.'.domain_id,
                    '.$table.'.domain_name
                    FROM '.$table.'
                    WHERE domain_global
                    LIMIT 1';

    
    my $sth;
    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $self->_log( 'Chargement de l\'ID du domaine global d\'OBM impossible', 3 );
        return undef;
    }

    my $rowResult = $sth->fetchrow_hashref();
    $sth->finish();

    return ($rowResult->{'domain_id'}, $rowResult->{'domain_name'});
}


sub _getNameDomainId {
    my $self = shift;
    my( $domainName, $productionTable ) = @_;

    if( !defined($domainName) ) {
        return undef;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return undef;
    }

    my $table = 'Domain';
    if( $productionTable ) {
        $table = 'P_'.$table;
    }

    my $query = 'SELECT '.$table.'.domain_id
                    FROM '.$table.'
                    WHERE domain_name=\''.$domainName.'\'
                    LIMIT 1';

    my $sth;
    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $self->_log( 'Impossible de charger l\'ID du domaine OBM ayant pour domaine de messagerie principal \''.$domainName.'\'', 3 );
        return 1;
    }

    my $rowResult = $sth->fetchrow_hashref();
    $sth->finish();

	if (! defined($rowResult->{'domain_id'}) ) {
        print "Impossible de charger l\'ID du domaine OBM ayant pour domaine de messagerie principal $domainName";
		return undef ;
	}

    return $rowResult->{'domain_id'};
}


sub _getIdDomainName {
    my $self = shift;
    my( $domainId, $productionTable ) = @_;

    if( !defined($domainId) ) {
        return undef;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return undef;
    }

    my $table = 'Domain';
    if( $productionTable ) {
        $table = 'P_'.$table;
    }

    my $query = 'SELECT '.$table.'.domain_name
                    FROM '.$table.'
                    WHERE domain_id=\''.$domainId.'\'
                    LIMIT 1';

    my $sth;
    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $self->_log( 'Impossible de charger le nom du domaine OBM ayant pour domaine ID \''.$domainId.'\'', 3 );
        return 1;
    }

    my $rowResult = $sth->fetchrow_hashref();
    $sth->finish();

    return $rowResult->{'domain_name'};
}


# Perldoc

=head1 NAME

update.pl - OBM administration tool, alter ego of Cyrus::IMAP::Shell

=head1 SYNOPSIS

  # Generic command
  $ update.pl <ACTION_PARAM> <CONFIG_PARAM>

  # Domain global update
  $ update.pl --domain-id <DOMAIN_ID> --global

  # Domain incremental update
  $ update.pl --domain-id <DOMAIN_ID> --incremental

  # Domain incremental update - only updates done by an admin
  $ update.pl --domain-id <DOMAIN_ID> --user <USER_ID> --incremental

  # Domain incremental update - only updates done for a delegation
  $ update.pl --domain-id <DOMAIN_ID> --delegation <DELEGATION> --incremental

  # Global update on global domain
  $ update.pl --domain-global --global

  # Global update domain by domain name
  $ update.pl --domain-name <DOMAIN_NAME> --global

  # Update entity
  $ update.pl --domain-id <DOMAIN_ID> --entity <<EOF
  user:login
  mailshare:mailshare_name
  host:host_name
  group:group_name
  EOF

  # Delete entire domain
  $ update.pl --domain-id <DOMAIN_ID> --delete

  # Display help
  $ update.pl --help

=head1 DESCRIPTION

This script is used by OBM-UI when an admin apply updates.

C<ACTION_PARAM> are :

=over 4

=item global : global update

=item incremental : incremental update

=item entity : entity update

=item delete : domain delete

=back

C<CONFIG_PARAM> are :

=over 4

=item domain-id (or domain-name, or domain-global) : run on this OBM domain

=item user : apply updates done by only this user - used on icremental mode only

=item delegation : apply updates done by only this delegation - used on
icremental mode only

=back

Global update apply all datas for a domain in the system regardless of BD
updates.

Incremental update apply only updates mark by the scope. It's possible to apply
updates for only a particular user or for a delegation.

Only one C<ACTION_PARAM> must be specify at same time.

Only one C<CONFIG_PARAM> must be specify at same time.

Parameters 'user' and 'delegation' are exclusive.

This script log via syslog.
