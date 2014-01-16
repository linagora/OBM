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


package OBM::DbUpdater::domainUpdater;

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

    if( ref($entity) ne 'OBM::Entities::obmDomain' ) {
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

    if( $self->delete($entity) ) {
        $self->_log( 'problème à la mise à jour BD du domaine '.$entity->getDescription(), 1 );
        return 1;
    }


    my $query = 'INSERT INTO P_Domain
                (   domain_id,
                    domain_timecreate,
                    domain_usercreate,
                    domain_userupdate,
                    domain_label,
                    domain_description,
                    domain_name,
                    domain_alias,
                    domain_global,
                    domain_uuid
                ) SELECT    domain_id,
                            domain_timecreate,
                            domain_usercreate,
                            domain_userupdate,
                            domain_label,
                            domain_description,
                            domain_name,
                            domain_alias,
                            domain_global,
                            domain_uuid
                  FROM Domain
                  WHERE domain_id='.$entity->getId();
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 1 );
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
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 1 );
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
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 1 );
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
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 1 );
        return 1;
    }

    return 0;
}


sub delete {
    my $self = shift;
    my( $entity ) = @_;

    if( ref($entity) ne 'OBM::Entities::obmDomain' ) {
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


    my $query = 'DELETE FROM P_ServiceProperty WHERE serviceproperty_entity_id IN (SELECT domainentity_entity_id FROM P_DomainEntity WHERE domainentity_domain_id='.$entity->getId().')';
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 1 );
        return 1;
    }

    $query = 'DELETE FROM P_Service WHERE service_entity_id IN (SELECT domainentity_entity_id FROM P_DomainEntity WHERE domainentity_domain_id='.$entity->getId().')';
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 1 );
        return 1;
    }

    $query = 'DELETE FROM P_DomainEntity WHERE domainentity_domain_id='.$entity->getId();
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 1 );
        return 1;
    }

    $query = 'DELETE FROM P_Domain WHERE domain_id='.$entity->getId();
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 1 );
        return 1;
    }

    return 0;
}
