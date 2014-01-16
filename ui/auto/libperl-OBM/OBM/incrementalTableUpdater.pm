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


package OBM::incrementalTableUpdater;

$VERSION = '1.0';

use OBM::Log::log;
@ISA = ('OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $domainId, $userId, $delegation ) = @_;

    my $self = bless { }, $class;

    if( !defined($domainId) || ref($domainId) || ($domainId !~ /$regexp_id/) ) {
        $self->_log( 'un et un seul identifiant de domaine doit être indiqué', 3 );
        return undef;
    }

    $self->{'domainId'} = $domainId;
    $self->{'userId'} = $userId;
    $self->{'delegation'} = $delegation;

    $self->{'deletedId'} = ();

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub update {
    my $self = shift;
    my( $entity ) = @_;

    # If entity is deleted correctly, delete ID from 'Deleted' table
    if( $entity->getUpdated() && $entity->getDelete() ) {
        push( @{$self->{'deletedId'}}, $entity->getId() );
    }

    return 0;
}


sub updateBd {
    my $self = shift;
    my $error = 0;

    if( $self->_purgeUpdated() ) {
        $self->_log( 'problème lors du nettoyage de la table Updated', 1 );
        $error = 1;
    }

    if( $self->_purgeUpdatedlinks() ) {
        $self->_log( 'problème lors du nettoyage de la table Updatedlinks', 1 );
        $error = 1;
    }

    if( $self->_deleteDeleted() ) {
        $self->_log( 'problème lors du nettoyage de la table Deleted', 1 );
        $error = 1;
    }

    return $error;
}


sub purgeBd {
    my $self = shift;
    my $error = 0;

    if( $self->_purgeUpdated() ) {
        $self->_log( 'problème lors du nettoyage de la table Updated', 1 );
        $error = 1;
    }

    if( $self->_purgeUpdatedlinks() ) {
        $self->_log( 'problème lors du nettoyage de la table Updatedlinks', 1 );
        $error = 1;
    }

    if( $self->_purgeDeleted() ) {
        $self->_log( 'problème lors du nettoyage de la table Deleted', 1 );
        $error = 1;
    }

    return $error;
}


sub _purgeUpdated {
    my $self = shift;

    require OBM::Tools::obmDbHandler;
    my $dbHandler;
    if( !($dbHandler = OBM::Tools::obmDbHandler->instance()) ) {
        $self->_log( 'connexion à la base de données impossible', 3 );
        return 1;
    }

    my $query = 'DELETE FROM Updated
                    WHERE updated_domain_id='.$self->{'domainId'};

    if( defined($self->{'userId'}) ) {
        $query .= ' AND updated_user_id='.$self->{'userId'};
    }

    if( defined($self->{'delegation'}) ) {
        $query .= ' AND updated_delegation=\''.$self->{'delegation'}.'\'';
    }

    $self->_log( 'nettoyage de la table Updated', 3 );
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'problème lors du nettoyage de la table Updated', 0 );
        return 1;
    }

    return 0;
}


sub _purgeUpdatedlinks {
    my $self = shift;

    require OBM::Tools::obmDbHandler;
    my $dbHandler;
    if( !($dbHandler = OBM::Tools::obmDbHandler->instance()) ) {
        $self->_log( 'connexion à la base de données impossible', 3 );
        return 1;
    }

    my $query = 'DELETE FROM Updatedlinks
                    WHERE updatedlinks_domain_id='.$self->{'domainId'};

    if( defined($self->{'userId'}) ) {
        $query .= ' AND updatedlinks_user_id='.$self->{'userId'};
    }

    if( defined($self->{'delegation'}) ) {
        $query .= ' AND updatedlinks_delegation=\''.$self->{'delegation'}.'\'';
    }

    $self->_log( 'nettoyage de la table Updatedlinks', 3 );
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'problème lors du nettoyage de la table Updatedlinks', 0 );
        return 1;
    }

    return 0;
}


sub _purgeDeleted {
    my $self = shift;

    require OBM::Tools::obmDbHandler;
    my $dbHandler;
    if( !($dbHandler = OBM::Tools::obmDbHandler->instance()) ) {
        $self->_log( 'connexion à la base de données impossible', 3 );
        return 1;
    }

    my $query = 'DELETE FROM Deleted
                    WHERE deleted_domain_id='.$self->{'domainId'};

    $self->_log( 'nettoyage de la table Deleted', 3 );
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'problème lors du nettoyage de la table Deleted', 0 );
        return 1;
    }

    return 0;
}


sub _deleteDeleted {
    my $self = shift;

    if( $#{$self->{'deletedId'}} < 0 ) {
        $self->_log( 'pas d\'entité supprimée', 4 );
        return 0;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler;
    if( !($dbHandler = OBM::Tools::obmDbHandler->instance()) ) {
        $self->_log( 'connexion à la base de données impossible', 3 );
        return 1;
    }

    my $query = 'DELETE FROM Deleted
                    WHERE deleted_domain_id='.$self->{'domainId'}.' AND
                    deleted_entity_id IN ('.join( ', ', @{$self->{'deletedId'}} ).')';

    if( defined($self->{'userId'}) ) {
        $query .= ' AND deleted_user_id='.$self->{'userId'};
    }

    if( defined($self->{'deleted_delegation'}) ) {
        $query .= ' AND deleted_delegation=\''.$self->{'delegation'}.'\'';
    }

    $self->_log( 'nettoyage de la table Deleted', 3 );
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'problème lors du nettoyage de la table Deleted', 0 );
        return 1;
    }

    # Empty deleted ID table
    $self->{'deletedId'} = ();

    return 0;
}
