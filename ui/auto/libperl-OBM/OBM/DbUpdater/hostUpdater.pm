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


package OBM::DbUpdater::hostUpdater;

$VERSION = '1.0';

use OBM::Log::log;
@ISA = ('OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub new {
    my $class = shift;

    my $self = bless { }, $class;

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );
}


sub update {
    my $self = shift;
    my( $entity ) = @_;

    if( ref($entity) ne 'OBM::Entities::obmHost' ) {
        $self->_log( 'entité incorrecte, traitement impossible', 0 );
        return 1;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler;
    my $sth;
    if( !($dbHandler = OBM::Tools::obmDbHandler->instance()) ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return 1;
    }

    if( $self->_delete($entity) ) {
        $self->_log( 'problème à la mise à jour BD de l\hôte '.$entity->getDescription(), 1 );
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
            $self->_log( 'problème à la mise à jour '.$entity->getDescription(), 1 );
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
            $self->_log( 'problème à la mise à jour '.$entity->getDescription(), 1 );
            return 1;
        }

        $query = 'INSERT INTO P_CategoryLink
                    (   categorylink_category_id,
                        categorylink_entity_id,
                        categorylink_category
                    ) SELECT    categorylink_category_id,
                                categorylink_entity_id,
                                categorylink_category
                      FROM CategoryLink
                      WHERE categorylink_entity_id=(SELECT hostentity_entity_id
                                                    FROM HostEntity
                                                    WHERE hostentity_host_id = '.$entity->getId().')';
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour '.$entity->getDescription(), 1 );
            return 1;
        }

        $query = 'INSERT INTO P_field
                    (   id,
                        entity_id,
                        field,
                        value
                    ) SELECT    id,
                                entity_id,
                                field,
                                value
                      FROM field
                      WHERE entity_id=(SELECT hostentity_entity_id
                                                    FROM HostEntity
                                                    WHERE hostentity_host_id = '.$entity->getId().')';
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour '.$entity->getDescription(), 1 );
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
                      WHERE serviceproperty_entity_id=(SELECT hostentity_entity_id
                                                    FROM HostEntity
                                                    WHERE hostentity_host_id = '.$entity->getId().')';
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour '.$entity->getDescription(), 1 );
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
            $self->_log( 'problème à la mise à jour '.$entity->getDescription(), 1 );
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
        $self->_log( 'entité incorrecte, traitement impossible', 0 );
        return 1;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler;
    my $sth;
    if( !($dbHandler = OBM::Tools::obmDbHandler->instance()) ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return 1;
    }


    if( $entity->getDelete() || $entity->getUpdateLinks() ) {
        my $query = 'DELETE FROM P_Service
                        WHERE service_entity_id IN (
                                    SELECT  hostentity_entity_id
                                    FROM P_HostEntity
                                    WHERE hostentity_host_id='.$entity->getId().')';
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD '.$entity->getDescription(), 1 );
            return 1;
        }
    }

    if( $entity->getDelete() || $entity->getUpdateEntity() ) {
        my $query = 'DELETE FROM P_CategoryLink
                    WHERE categorylink_entity_id=(SELECT hostentity_entity_id
                                                    FROM HostEntity
                                                    WHERE hostentity_host_id = '.$entity->getId().')';
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD '.$entity->getDescription(), 1 );
            return 1;
        }

        $query = 'DELETE FROM P_field
                    WHERE entity_id=(SELECT hostentity_entity_id
                                                    FROM HostEntity
                                                    WHERE hostentity_host_id = '.$entity->getId().')';
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD '.$entity->getDescription(), 1 );
            return 1;
        }

        $query = 'DELETE FROM P_ServiceProperty
                    WHERE serviceproperty_entity_id=(SELECT hostentity_entity_id
                                                    FROM HostEntity
                                                    WHERE hostentity_host_id = '.$entity->getId().')';
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD '.$entity->getDescription(), 1 );
            return 1;
        }

        $query = 'DELETE FROM P_HostEntity WHERE hostentity_host_id='.$entity->getId();
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD '.$entity->getDescription(), 1 );
            return 1;
        }

        $query = 'DELETE FROM P_Host WHERE host_id='.$entity->getId();
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD '.$entity->getDescription(), 1 );
            return 1;
        }
    }


    return 0;
}
