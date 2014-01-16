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


package OBM::EntitiesFactory::factoryProgramming;

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

    my $self = bless { }, $class;

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );
}


sub _initFactoryProgramming {
    my $self = shift;

    $self->_log( 'initialisation du programmateur de factory', 4 );
    $self->{'entityType'} = undef;
    $self->{'updateType'} = undef;
    $self->{'entitiesId'} = undef;

    return 0;
}


sub setEntitiesType {
    my $self = shift;
    my( $type ) = @_;

    $self->_initFactoryProgramming();

    SWITCH: {
        if( $type eq 'USER' ) {
            $self->_log( 'initialisation d\'un programmateur d\'entité de type utilisateur', 4 );
            $self->{'entityType'} = $type;
            last SWITCH;
        }

        if( $type eq 'MAILSHARE' ) {
            $self->_log( 'initialisation d\'un programmateur d\'entité de type partage messagerie', 4 );
            $self->{'entityType'} = $type;
            last SWITCH;
        }

        if( $type eq 'CONTACT' ) {
            $self->_log( 'initialisation d\'un programmateur d\'entité de type contacts', 4 );
            $self->{'entityType'} = $type;
            last SWITCH;
        }

        if( $type eq 'GROUP' ) {
            $self->_log( 'initialisation d\'un programmateur d\'entité de type groupe', 4 );
            $self->{'entityType'} = $type;
            last SWITCH;
        }

        if( $type eq 'HOST' ) {
            $self->_log( 'initialisation d\'un programmateur d\'entité de type hôte', 4 );
            $self->{'entityType'} = $type;
            last SWITCH;
        }

        if( $type eq 'CONTACT_SERVICE' ) {
            $self->_log( 'initialisation d\'un programmateur d\'entité de type configuration du service contacts', 3 );
            $self->{'entityType'} = $type;
            last SWITCH;
        }

        $self->_log( 'type d\'entité inconnu \''.$type.'\'', 3 );
        return 1;
    }

    return 0;
}


sub getEntitiesType {
    my $self = shift;

    return $self->{'entityType'};
}


sub setUpdateType {
    my $self = shift;
    my( $updateType ) = @_;

    $self->{'updateType'} = $updateType;

    if( !$self->_checkUpdateType() ) {
        $self->{'updateType'} = undef;
        return 1;
    }

    return 0;
}


sub getUpdateType {
    my $self = shift;

    return $self->{'updateType'};
}


sub setEntitiesIds {
    my $self = shift;
    my( $entitiesId ) = @_;

    if( ref($entitiesId) ne 'ARRAY' ) {
        $self->_log( 'listes d\'identifiant incorrecte', 1 );
        return 1;
    }

    $self->{'entitiesId'} = $entitiesId;

    return 0;
}


sub getEntitiesIds {
    my $self = shift;

    return $self->{'entitiesId'};
}
