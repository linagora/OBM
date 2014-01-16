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


package OBM::Cyrus::cyrusRemoteEngine;

$VERSION = '1.0';

use Class::Singleton;
use OBM::Log::log;
@ISA = ('Class::Singleton', 'OBM::Log::log' );

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub _new_instance {
    my $class = shift;

    my $self = bless { }, $class;

    require OBM::Parameters::common;
    if( !$OBM::Parameters::common::obmModules->{'mail'} ) {
        $self->_log( 'module OBM-MAIL désactivé, gestionnaire Cyrus distant non démarré', 3 );
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub addCyrusPartition {
    my $self = shift;
    my( $cyrusSrv ) = @_;

    if( !$OBM::Parameters::common::cyrusDomainPartition ) {
        # Cyrus partition support disable
        return 0;
    }

    if( !defined($cyrusSrv) ) {
        $self->_log( 'serveur Cyrus non défini, ajout de partition impossible', 3 );
        return 1;
    }elsif( ref($cyrusSrv) ne 'OBM::Cyrus::cyrusServer' ) {
        $self->_log( 'description du serveur Cyrus incorrecte, ajout de partition impossible', 3 );
        return 1;
    }

    my $cyrusSrvIp = $cyrusSrv->getCyrusServerIp();
    if( !$cyrusSrvIp ) {
        $self->_log( 'adresse IP du serveur Cyrus incorrecte, ajout de partition impossible', 3 );
        return 1;
    }

    my $cyrusSrvName = $cyrusSrv->getCyrusServerName();
    if( !$cyrusSrvName ) {
        $self->_log( 'nom du serveur Cyrus incorrect, ajout de partition impossible', 3 );
        return 1;
    }

    $self->_log( 'connexion à obmSatellite de '.$cyrusSrv->getDescription(), 2 );

    require OBM::ObmSatellite::client;
    my $obmSatelliteClient = OBM::ObmSatellite::client->instance();
    if( !defined($obmSatelliteClient) ) {
        $self->_log( 'Echec lors de l\'initialisation du client obmSatellite', 3  );
        return 1;
    }

    return $obmSatelliteClient->post( $cyrusSrvIp, '/cyruspartition/host/add/'.$cyrusSrvName );
}


sub delCyrusPartition {
    my $self = shift;
    my( $cyrusSrv ) = @_;

    if( !$OBM::Parameters::common::cyrusDomainPartition ) {
        # Cyrus partition support disable
        return 0;
    }

    if( !defined($cyrusSrv) ) {
        $self->_log( 'serveur Cyrus non défini, suppression de partition impossible', 3 );
        return 1;
    }elsif( ref($cyrusSrv) ne 'OBM::Cyrus::cyrusServer' ) {
        $self->_log( 'description du serveur Cyrus incorrecte, suppression de partition impossible', 3 );
        return 1;
    }

    my $cyrusSrvIp = $cyrusSrv->getCyrusServerIp();
    if( !$cyrusSrvIp ) {
        $self->_log( 'adresse IP du serveur Cyrus incorrecte, suppression de partition impossible', 3 );
        return 1;
    }

    my $cyrusSrvName = $cyrusSrv->getCyrusServerName();
    if( !$cyrusSrvName ) {
        $self->_log( 'nom du serveur Cyrus incorrect, suppression de partition impossible', 3 );
        return 1;
    }

    $self->_log( 'connexion à obmSatellite de '.$cyrusSrv->getDescription(), 2 );

    require OBM::ObmSatellite::client;
    my $obmSatelliteClient = OBM::ObmSatellite::client->instance();
    if( !defined($obmSatelliteClient) ) {
        $self->_log( 'Echec lors de l\'initialisation du client obmSatellite', 3  );
        return 1;
    }


    return $obmSatelliteClient->post( $cyrusSrvIp, '/cyruspartition/host/del/'.$cyrusSrvName );
}
