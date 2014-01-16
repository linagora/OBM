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


package OBM::Update::updateSieve;

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


    if( !defined($parameters) ) {
        $self->_log( 'paramètres d\'initialisation non définis', 0 );
        return undef;
    }


    $self->{'userLogin'} = $parameters->{'login'};
    $self->{'domainId'} = $parameters->{'domain-id'};

    if( $self->_getEntity() ) {
        return undef;
    }

    return $self;
}


sub _getEntity {
    my $self = shift;

    # Getting BD user ID
    my $userId = $self->_getUserIdFromUserLoginDomain( $self->{'userLogin'}, $self->{'domainId'});
    if( !defined($userId) ) {
        $self->_log( 'utilisateur \''.$self->{'entityName'}.'\', domaine d\'ID '.$self->{'domainId'}.' inconnu', 1 );
        return 1;
    }

    # Getting user entity
    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }
    if( $programmingObj->setEntitiesType( 'USER' ) || $programmingObj->setUpdateType( 'UPDATE_ENTITY' ) || $programmingObj->setEntitiesIds( [$userId] ) ) {
        $self->_log( 'problème lors de l\'initialisation du programmateur de factory', 4 );
        return 1;
    }

    require OBM::entitiesFactory;
    my $entitiesFactory = OBM::entitiesFactory->new( 'PROGRAMMABLEWITHOUTDOMAIN', $self->{'domainId'} );
    if( !defined($entitiesFactory) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }
    if( $entitiesFactory->loadEntities($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }

    $self->{'entityFactory'} = $entitiesFactory;

    return 0;
}


sub update {
    my $self = shift;

    if( !defined($self->{'entityFactory'}) ) {
        $self->_log( 'problème avec la factory d\'entités', 0 );
        return 1;
    }

    require OBM::Cyrus::sieveEngine;
    $self->_log( 'initialisation du moteur Sieve', 2 );
    my $sieveEngine;
    if( !($sieveEngine = OBM::Cyrus::sieveEngine->new()) ) {
        $self->_log( 'problème à l\'initialisation du moteur Sieve', 0 );
        return 1;
    }

    require OBM::DbUpdater::sqlSieveUpdater;
    $self->_log( 'initialisation du moteur de mise à jour BD', 2 );
    my $dbUpdater;
    if( !($dbUpdater = OBM::DbUpdater::sqlSieveUpdater->new()) ) {
        $self->_log( 'problème à l\'initialisation du moteur de mise à jour BD', 0 );
        return 1;
    }

    my $errorCode = 0;
    while( my $userEntity = $self->{'entityFactory'}->next() ) {
        if( !defined($userEntity) ) {
            $self->_log( 'problème lors de la récupération de la description de l\'entité', 0 );
            $errorCode = 1;
            next;
        }

        # Sieve update
        if( $sieveEngine->update( $userEntity ) ) {
            $self->_log( 'problème à la mise à jour du script Sieve de '.$userEntity->getDescription(), 0 );
            $errorCode = 1;
            next;
        }

        # SQL update
        if( $dbUpdater->update( $userEntity ) ) {
            $self->_log( 'problème à la mise à jour BD de '.$userEntity->getDescription(), 0 );
            $errorCode = 1;
            next;
        }
    }

    return $errorCode;
}
