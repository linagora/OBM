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


package OBM::EntitiesFactory::programmableFactories;

$VERSION = '1.0';

use OBM::EntitiesFactory::factory;
use OBM::Log::log;
@ISA = ('OBM::EntitiesFactory::factory', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub new {
    my $class = shift;
    my( $entitiesFactory ) = @_;

    my $self = bless { }, $class;

    if( !defined($entitiesFactory) || (ref($entitiesFactory) !~ /^OBM::entitiesFactory$/) ) {
        $self->_log( 'la factory doit être de type \'OBM::entitiesFactory\'', 1 );
        return undef;
    }

    $self->{'entitiesFactory'} = $entitiesFactory;

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );
}


sub addEntities {
    my $self = shift;
    my( $programmingObj ) = @_;

    if( ref($programmingObj) ne 'OBM::EntitiesFactory::factoryProgramming' ) {
        $self->_log( 'liste d\'entités à charger incorrecte', 1 );
        return 1;
    }

    $self->{'programmingObj'} = $programmingObj;

    $self->{'updateType'} = $programmingObj->getUpdateType();
    if( !$self->_checkUpdateType() ) {
        return 1;
    }

    SWITCH: {
        if( !defined($programmingObj->getEntitiesType()) ) {
            $self->_log( 'type d\'entité à programmer incorrect', 1 );
            return 1;
        }

        if( $programmingObj->getEntitiesType() eq 'USER' ) {
            $self->_initUserFactory();
            last SWITCH;
        }

        if( $programmingObj->getEntitiesType() eq 'MAILSHARE' ) {
            $self->_initMailshareFactory();
            last SWITCH;
        }

        if( $programmingObj->getEntitiesType() eq 'CONTACT' ) {
            $self->_initContactFactory();
            last SWITCH;
        }

        if( $programmingObj->getEntitiesType() eq 'GROUP' ) {
            $self->_initGroupFactory();
            last SWITCH;
        }

        if( $programmingObj->getEntitiesType() eq 'HOST' ) {
            $self->_initHostFactory();
            last SWITCH;
        }

        if( $programmingObj->getEntitiesType() eq 'CONTACT_SERVICE' ) {
            $self->_initContactServiceFactory();
            last SWITCH;
        }

        $self->_log( 'type d\'entité non supporté', 0 );
    }

    return 0;
}


sub _initHostFactory {
    my $self = shift;
    my $entitiesIds = $self->{'programmingObj'}->getEntitiesIds();
    my $entitiesFactory = $self->{'entitiesFactory'};

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::hostFactory;
        if( !($entityFactory = OBM::EntitiesFactory::hostFactory->new( $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des hôtes', 1 );
            return 1;
        }

        if( $self->{'programmingObj'}->getUpdateLinkedEntities() ) {
            $entityFactory->setUpdateLinkedEntities();
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    return 0;
}


sub _initGroupFactory {
    my $self = shift;
    my $entitiesIds = $self->{'programmingObj'}->getEntitiesIds();
    my $entitiesFactory = $self->{'entitiesFactory'};

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::groupFactory;
        if( !($entityFactory = OBM::EntitiesFactory::groupFactory->new( $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des groupes', 1 );
            return 1;
        }

        if( $self->{'programmingObj'}->getUpdateLinkedEntities() ) {
            $entityFactory->setUpdateLinkedEntities();
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    return 0;
}


sub _initMailshareFactory {
    my $self = shift;
    my $entitiesIds = $self->{'programmingObj'}->getEntitiesIds();
    my $entitiesFactory = $self->{'entitiesFactory'};

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::mailshareFactory;
        if( !($entityFactory = OBM::EntitiesFactory::mailshareFactory->new( $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des partages de messagerie', 1 );
            return 1;
        }

        if( $self->{'programmingObj'}->getUpdateLinkedEntities() ) {
            $entityFactory->setUpdateLinkedEntities();
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    return 0;
}


sub _initUserFactory {
    my $self = shift;
    my $entitiesIds = $self->{'programmingObj'}->getEntitiesIds();
    my $entitiesFactory = $self->{'entitiesFactory'};

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::userFactory;
        if( !($entityFactory = OBM::EntitiesFactory::userFactory->new( $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des utilisateurs', 1 );
            return 1;
        }

        if( $self->{'programmingObj'}->getUpdateLinkedEntities() ) {
            $entityFactory->setUpdateLinkedEntities();
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    return 0;
}


sub _initContactFactory {
    my $self = shift;
    my $entitiesIds = $self->{'programmingObj'}->getEntitiesIds();
    my $entitiesFactory = $self->{'entitiesFactory'};

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::contactFactory;
        if( !($entityFactory = OBM::EntitiesFactory::contactFactory->new( $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des contacts', 1 );
            return 1;
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }
}


sub _initContactServiceFactory {
    my $self = shift;
    my $entityFactory;
    my $entitiesFactory = $self->{'entitiesFactory'};

    require OBM::EntitiesFactory::contactServiceFactory;
    if( !($entityFactory = OBM::EntitiesFactory::contactServiceFactory->new( $self->{'updateType'}, $entitiesFactory->{'domain'} )) ) {
        $self->_log( 'problème au chargement de la factory du service contact', 1 );
        return 1;
    }

    $entitiesFactory->enqueueFactory( $entityFactory );
}
