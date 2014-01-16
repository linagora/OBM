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


package OBM::Update::updateCyrusAcl;

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


    $self->{'entityType'} = $parameters->{'type'};
    $self->{'entityName'} = $parameters->{'name'};
    $self->{'domainId'} = $parameters->{'domain-id'};

    if( $self->_getEntity() ) {
        return undef;
    }

    return $self;
}


sub _getEntity {
    my $self = shift;

    if( $self->{'entityType'} =~ /^mailbox$/  ) {
        return $self->_getUserEntity();
    }elsif( $self->{'entityType'} =~ /^mailshare$/ ) {
        return $self->_getMaishareEntity();
    }else {
       $self->_log( 'type d\'entité inconnu', 3 );
       return 1;
    }

    return 0;
}


sub _getUserEntity {
    my $self = shift;

    # Getting BD user ID
    my $userId = $self->_getUserIdFromUserLoginDomain( $self->{'entityName'}, $self->{'domainId'});
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
    if( $programmingObj->setEntitiesType( 'USER' ) || $programmingObj->setUpdateType( 'UPDATE_LINKS' ) || $programmingObj->setEntitiesIds( [$userId] ) ) {
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


sub _getMaishareEntity {
    my $self = shift;

    # Getting BD mailshare ID
    my $mailshareId = $self->_getMailShareIdFromUserLoginDomain( $self->{'entityName'}, $self->{'domainId'});
    if( !defined($mailshareId) ) {
        $self->_log( 'patage messagerie \''.$self->{'entityName'}.'\', domaine d\'ID '.$self->{'domainId'}.' inconnu', 1 );
        return 1;
    }

    # Getting mailshare entity
    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }
    if( $programmingObj->setEntitiesType( 'MAILSHARE' ) || $programmingObj->setUpdateType( 'UPDATE_LINKS' ) || $programmingObj->setEntitiesIds( [$mailshareId] ) ) {
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

    require OBM::Cyrus::cyrusUpdateAclEngine;
    $self->_log( 'initialisation du moteur de mise à jour des ACLs et mise à jour des ACLs', 2 );
    my $cyrusUpdateAclEngine;
    if( !($cyrusUpdateAclEngine = OBM::Cyrus::cyrusUpdateAclEngine->new()) ) {
        $self->_log( 'problème à l\'initialisation du moteur de mise à jour des ACLs Cyrus', 0 );
        return 1;
    }

    require OBM::dbUpdater;
    $self->_log( 'initialisation du BD updater', 2 );
    if( !($self->{'dbUpdater'} = OBM::dbUpdater->new()) ) {
        $self->_log( 'echec de l\'initialisation du BD updater', 0 );
        return 1;
    }

    my $errorCode = 0;
    while( my $entity = $self->{'entityFactory'}->next() ) {
        if( !defined($entity) ) {
            $self->_log( 'problème lors de la récupération de la description de l\'entité', 0 );
            $errorCode = 1;
            next;
        }

        # Update Cyrus ACLs
        if( $cyrusUpdateAclEngine->update( $entity ) || $entity->setUpdated() ) {
            $self->_log( 'problème à la mise à jour des ACLs Cyrus de '.$entity->getDescription(), 0 );
            $errorCode = 1;
            next;
        }

        if( $self->{'dbUpdater'}->update($entity) ) {
            $self->_log( 'problème à la mise à jour BD de l\'entité '.$entity->getDescription(), 1 );
            $errorCode = 1;
        }
    }

 
    return $errorCode;
}
