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


package OBM::Update::updateEntity;

$VERSION = '1.0';

use OBM::Update::update;
use OBM::Entities::entityIdGetter;
use OBM::Log::log;
@ISA = ('OBM::Update::update', 'OBM::Entities::entityIdGetter', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $parameters ) = @_;

    my $self = bless { }, $class;


    if( !defined($parameters) ) {
        $self->_log( 'Usage: PACKAGE->new(PARAMLIST)', 4 );
        return undef;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connecteur a la base de donnee invalide', 3 );
        return undef;
    }

    # Updater initialization
    $self->{'entity'} = $parameters->{'entity'};

    # Domain identifier
    if( defined($parameters->{'domain-id'}) ) {
        $self->{'domainId'} = $parameters->{'domain-id'};
    }else {
        $self->_log( 'Le parametre domain-id doit etre precise', 0 );
        return undef;
    }

    $self->_ckeckEntity( $parameters->{'updateEntityList'} );
    if( !defined($self->{'entity'}) ) {
        $self->_log( 'Aucune entités indiquée ou valides. Au moins une entité à mettre à jour doit être indiquée', 0 );
        return undef;
    }


    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub _ckeckEntity {
    my $self = shift;
    my( $entityList ) = @_;

    if( ref($entityList) ne 'ARRAY' ) {
        return 1;
    }elsif( $#{$entityList} < 0 ) {
        return 1;
    }

    for( my $i=0; $i<=$#{$entityList}; $i++ ) {
        if( $entityList->[$i] !~ /^(user|mailshare|group|host):(.+)$/ ) {
            $self->_log( 'Entrée invalide : '.$entityList->[$i], 2 );
            next;
        }

        SWITCH: {
            if( ($1 eq 'user') ) {
                my $id =  $self->_getUserIdFromUserLoginDomain( $2, $self->{'domainId'} );
                push( @{$self->{'entities'}->{'USER'}}, $id ) if $id;
                last SWITCH;
            }

            if( ($1 eq 'mailshare') ) {
                my $id = $self->_getMailshareIdFromMailshareNameDomain( $2, $self->{'domainId'} );
                push( @{$self->{'entities'}->{'MAILSHARE'}}, $id ) if $id;
                last SWITCH;
            }

            if( ($1 eq 'group') ) {
                my $id = $self->_getGroupIdFromGroupNameDomain( $2, $self->{'domainId'} );
                push( @{$self->{'entities'}->{'GROUP'}}, $id ) if $id;
                last SWITCH;
            }

            if( ($1 eq 'host') ) {
                my $id = $self->_getHostIdFromHostNameDomain( $2, $self->{'domainId'} );
                push( @{$self->{'entities'}->{'HOST'}}, $id ) if $id;
                last SWITCH;
            }

        }
    }
}


sub _updateInitFactory {
    my $self = shift;

    require OBM::entitiesFactory;
    $self->_log( 'initialisation de l\'entity factory', 4 );
    if( !($self->{'entitiesFactory'} = OBM::entitiesFactory->new( 'PROGRAMMABLEWITHOUTDOMAIN', $self->{'domainId'} )) ) {
        $self->_log( 'echec de l\'initialisation de l\'entity factory', 0 );
        return 1;
    }

    while( my($entityType, $entitiesIds) = each(%{$self->{'entities'}}) ) {
        require OBM::EntitiesFactory::factoryProgramming;
        my $factoryProgramming = OBM::EntitiesFactory::factoryProgramming->new();

        if( $factoryProgramming->setEntitiesType( $entityType ) ||
            $factoryProgramming->setUpdateType( 'UPDATE_ALL' ) ||
            $factoryProgramming->setEntitiesIds( $entitiesIds ) ||
            $factoryProgramming->setUpdateLinkedEntities() ||
            $self->{'entitiesFactory'}->loadEntities( $factoryProgramming ) ) {
            $self->_log( 'Impossible de programmer la factory pour les entités de type \''.$entityType.'\'', 0 );
            next;
        }
    }

    return 0;
}
