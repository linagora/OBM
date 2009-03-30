package OBM::DbUpdater::hostUpdater;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(_log dump);


sub new {
    my $class = shift;

    my $self = bless { }, $class;

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub update {
    my $self = shift;
    my( $entity ) = @_;

    if( ref($entity) ne 'OBM::Entities::obmHost' ) {
        $self->_log( 'entité incorrecte, traitement impossible', 3 );
        return 1;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler;
    my $sth;
    if( !($dbHandler = OBM::Tools::obmDbHandler->instance()) ) {
        $self->_log( 'connexion à la base de données impossible', 3 );
        return 1;
    }

    if( $self->_delete($entity) ) {
        $self->_log( 'problème à la mise à jour BD de l\hôte '.$entity->getDescription(), 2 );
        return 1;
    }


    if( !$entity->getDelete() && $entity->getUpdateEntity() ) {
        my $query = 'INSERT INTO P_Host
                    (   host_id,
                        host_domain_id,
                        host_timecreate,
                        host_userupdate,
                        host_usercreate,
                        host_uid,
                        host_gid,
                        host_archive,
                        host_name,
                        host_fqdn,
                        host_ip,
                        host_delegation,
                        host_description
                    ) SELECT    host_id,
                                host_domain_id,
                                host_timecreate,
                                host_userupdate,
                                host_usercreate,
                                host_uid,
                                host_gid,
                                host_archive,
                                host_name,
                                host_fqdn,
                                host_ip,
                                host_delegation,
                                host_description
                      FROM Host
                      WHERE host_id='.$entity->getId();
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour '.$entity->getDescription(), 2 );
            return 1;
        }

        $query = 'INSERT INTO P_HostEntity
                 (  hostentity_entity_id,
                    hostentity_host_id
                 ) SELECT   hostentity_entity_id,
                            hostentity_host_id
                   FROM HostEntity
                   WHERE hostentity_host_id='.$entity->getId();
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour '.$entity->getDescription(), 2 );
            return 1;
        }
    }

    if( !$entity->getDelete() && $entity->getUpdateLinks() ) {
        my $query = 'INSERT INTO P_Service
                 (  service_id,
                    service_service,
                    service_entity_id
                 ) SELECT   service_id,
                            service_service,
                            service_entity_id
                   FROM Service
                   WHERE service_entity_id IN
                    ( SELECT hostentity_entity_id
                      FROM HostEntity
                      WHERE hostentity_host_id='.$entity->getId().'
                    )';
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour '.$entity->getDescription(), 2 );
            return 1;
        }
    }


    return 0;
}


sub delete {
    my $self = shift;
    my( $entity ) = @_;

    if( !$entity->getDelete() ) {
        $self->_log( 'l\'entité '.$entity->getDescription().' n\'est pas à supprimer. Suppression annulée', 3 );
        return 0;
    }

    return $self->_delete( $entity );
}


sub _delete {
    my $self = shift;
    my( $entity ) = @_;

    if( ref($entity) ne 'OBM::Entities::obmHost' ) {
        $self->_log( 'entité incorrecte, traitement impossible', 3 );
        return 1;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler;
    my $sth;
    if( !($dbHandler = OBM::Tools::obmDbHandler->instance()) ) {
        $self->_log( 'connexion à la base de données impossible', 3 );
        return 1;
    }


    if( $entity->getDelete() || $entity->getUpdateLinks() ) {
        my $query = 'DELETE FROM P_Service
                        WHERE service_entity_id IN (
                                    SELECT  hostentity_entity_id
                                    FROM P_HostEntity
                                    WHERE hostentity_host_id='.$entity->getId().')';
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD '.$entity->getDescription(), 2 );
            return 1;
        }
    }

    if( $entity->getDelete() || $entity->getUpdateEntity() ) {
        my $query = 'DELETE FROM P_HostEntity WHERE hostentity_host_id='.$entity->getId();
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD '.$entity->getDescription(), 2 );
            return 1;
        }

        $query = 'DELETE FROM P_Host WHERE host_id='.$entity->getId();
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD '.$entity->getDescription(), 2 );
            return 1;
        }
    }


    return 0;
}
