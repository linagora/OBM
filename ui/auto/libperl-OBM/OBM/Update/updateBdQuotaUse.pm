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


package OBM::Update::updateBdQuotaUse;

$VERSION = '1.0';

use OBM::Entities::systemEntityIdGetter;
use OBM::Log::log;
@ISA = ('OBM::Entities::systemEntityIdGetter', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub new {
    my $class = shift;
    my( $parameters ) = @_;

    my $self = bless { }, $class;

    if( !$OBM::Parameters::common::obmModules->{'mail'} ) {
        $self->_log( 'module OBM-MAIL désactivé, mise à jour annulée', 2 );
        return undef;
    }

    if( !defined($parameters) ) {
        $self->_log( 'paramètres d\'initialisation non définis', 0 );
        return undef;
    }

    if( defined($parameters->{'domain-id'}) ) {
        push( @{$self->{'domainId'}}, $parameters->{'domain-id'} );
    }

    if( $self->_getEntities() ) {
        return undef;
    }

    return $self;
} 


sub _getEntities {
    my $self = shift;

    if( ref($self->{'domainId'}) ne 'ARRAY' ) {
        $self->{'domainId'} = $self->getDomainId( 0, undef );
    }

    my $errorCode = 0;
    my $userIdByDomain = $self->getUserIdByDomainId( $self->{'domainId'} );
    while( my( $domainId, $userIdList ) = each(%{$userIdByDomain}) ) {
        # Getting user entity
        require OBM::EntitiesFactory::factoryProgramming;
        my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
        if( !defined($programmingObj) ) {
            $self->_log( 'probleme lors de la programmation de la factory d\'entités', 1 );
            $errorCode = 1;
            next;
        }
        if( $programmingObj->setEntitiesType( 'USER' ) || $programmingObj->setUpdateType( 'SYSTEM_ENTITY' ) || $programmingObj->setEntitiesIds( $userIdList ) ) {
            $self->_log( 'problème lors de l\'initialisation du programmateur de factory', 1 );
            $errorCode = 1;
            next;
        }

        require OBM::entitiesFactory;
        my $entitiesFactory = OBM::entitiesFactory->new( 'PROGRAMMABLEWITHOUTDOMAIN', $domainId );
        if( !defined($entitiesFactory) ) {
            $self->_log( 'probleme lors de la programmation de la factory d\'entités', 1 );
            $errorCode = 1;
            next;
        }
        if( $entitiesFactory->loadEntities($programmingObj) ) {
            $self->_log( 'probleme lors de la programmation de la factory d\'entités', 1 );
            $errorCode = 1;
            next;
        }

        push( @{$self->{'entitiesFactory'}}, $entitiesFactory );
    }

    return $errorCode;
}


sub update {
    my $self = shift;

    if( !defined($self->{'entitiesFactory'}) || ref($self->{'entitiesFactory'}) ne 'ARRAY' ) {
        $self->_log( 'problème avec la factory d\'entités', 0 );
        return 1;
    }

    require OBM::Cyrus::cyrusUpdateQuotaUsedEngine;
    $self->_log( 'initialisation du moteur Cyrus de mise à jour du quota utilisé', 3 );
    my $cyrusUpdateQuotaUsedEngine = OBM::Cyrus::cyrusUpdateQuotaUsedEngine->new();
    if( !ref($cyrusUpdateQuotaUsedEngine) ) {
        $self->_log( 'problème à l\'initialisation du moteur Cyrus de mise à jour du quota utilisé', 0 );
        return 1;
    }

    require OBM::DbUpdater::sqlQuotaUsedUpdater;
    $self->_log( 'initialisation du moteur de mise à jour BD du quota utilisé', 3 );
    my $sqlQuotaUsedUpdater;
    if( !($sqlQuotaUsedUpdater = OBM::DbUpdater::sqlQuotaUsedUpdater->new()) ) {
        $self->_log( 'problème à l\'initialisation du moteur de mise à jour BD du quota utilisé', 0 );
        return 1;
    }

    my $errorCode = 0;
    while( my $entityFactory = pop(@{$self->{'entitiesFactory'}}) ) {
        while( my $entity = $entityFactory->next() ) {
            $self->_log( 'obtention du quota utilisé de '.$entity->getDescription(), 3 );

            if( $cyrusUpdateQuotaUsedEngine->update( $entity ) ) {
                $self->_log( 'problème lors de l\'obtention du quota Cyrus utilisé de '.$entity->getDescription(), 0 );
                $errorCode = 1;
                next;
            }

            $self->_log( 'mise à jour en BD du quota utilisé de '.$entity->getDescription(), 3 );

            if( $sqlQuotaUsedUpdater->update( $entity ) ) {
                $self->_log( 'problème lors de la mise à jour en BD du quota utilisé de '.$entity->getDescription(), 0 );
                $errorCode = 1;
                next;
            }
        }
    }

    return $errorCode;
}
