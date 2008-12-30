package OBM::incrementalTableUpdater;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(_log dump);
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

    # Only delete really deleted entities references
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
        $query .= ' AND updated_delegation='.$self->{'delegation'};
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
        $query .= ' AND updatedlinks_delegation='.$self->{'delegation'};
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
                    WHERE deleted_domain_id='.$self->{'domainId'}. 'AND
                    deleted_entity_id IN ('.join( ', ', @{$self->{'deletedId'}} ).')';

    if( defined($self->{'userId'}) ) {
        $query .= ' AND deleted_user_id='.$self->{'userId'};
    }

    if( defined($self->{'deleted_delegation'}) ) {
        $query .= ' AND deleted_delegation='.$self->{'delegation'};
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
