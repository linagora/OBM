package OBM::DbUpdater::domainUpdater;

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

    if( ref($entity) ne 'OBM::Entities::obmDomain' ) {
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

    if( $self->delete($entity) ) {
        $self->_log( 'problème à la mise à jour BD du domaine '.$entity->getDescription(), 2 );
        return 1;
    }


    my $query = 'INSERT INTO P_Domain
                (   domain_id,
                    domain_label,
                    domain_description,
                    domain_name,
                    domain_alias,
                    domain_global
                ) SELECT    domain_id,
                            domain_label,
                            domain_description,
                            domain_name,
                            domain_alias,
                            domain_global
                  FROM Domain
                  WHERE domain_id='.$entity->getId();
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 2 );
        return 1;
    }


    $query = 'INSERT INTO P_DomainEntity
                (   domainentity_entity_id,
                    domainentity_domain_id
                ) SELECT    domainentity_entity_id,
                            domainentity_domain_id
                  FROM DomainEntity
                  WHERE domainentity_domain_id='.$entity->getId();
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 2 );
        return 1;
    }


    $query = 'INSERT INTO P_Service
                (   service_id,
                    service_service,
                    service_entity_id
                ) SELECT    service_id,
                            service_service,
                            service_entity_id
                  FROM Service
                  WHERE service_entity_id IN
                    ( SELECT domainentity_entity_id
                      FROM DomainEntity
                      WHERE domainentity_domain_id='.$entity->getId().'
                    )';
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 2 );
        return 1;
    }


    $query = 'INSERT INTO P_ServiceProperty
                (   serviceproperty_id,
                    serviceproperty_service,
                    serviceproperty_property,
                    serviceproperty_entity_id,
                    serviceproperty_value
                ) SELECT    serviceproperty_id,
                            serviceproperty_service,
                            serviceproperty_property,
                            serviceproperty_entity_id,
                            serviceproperty_value
                  FROM ServiceProperty
                  WHERE serviceproperty_entity_id IN
                    ( SELECT domainentity_entity_id
                      FROM DomainEntity
                      WHERE domainentity_domain_id='.$entity->getId().'
                    )';
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 2 );
        return 1;
    }

    return 0;
}


sub delete {
    my $self = shift;
    my( $entity ) = @_;

    if( ref($entity) ne 'OBM::Entities::obmDomain' ) {
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


    my $query = 'DELETE FROM P_ServiceProperty WHERE serviceproperty_entity_id IN (SELECT domainentity_entity_id FROM P_DomainEntity WHERE domainentity_domain_id='.$entity->getId().')';
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 2 );
        return 1;
    }

    $query = 'DELETE FROM P_Service WHERE service_entity_id IN (SELECT domainentity_entity_id FROM P_DomainEntity WHERE domainentity_domain_id='.$entity->getId().')'
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 2 );
        return 1;
    }

    $query = 'DELETE FROM P_DomainEntity WHERE domainentity_domain_id='.$entity->getId();
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 2 );
        return 1;
    }

    $query = 'DELETE FROM P_Domain WHERE domain_id='.$entity->getId();
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 2 );
        return 1;
    }

    return 0;
}
