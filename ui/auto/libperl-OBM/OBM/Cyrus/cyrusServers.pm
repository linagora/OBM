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


package OBM::Cyrus::cyrusServers;

$VERSION = '1.0';

use Class::Singleton;
use OBM::Tools::obmServersList;
use OBM::Log::log;
@ISA = ('Class::Singleton', 'OBM::Tools::obmServersList', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub _new_instance {
    my $class = shift;

    my $self = bless { }, $class;

    $self->{'servers'} = undef;

    return $self;
}


sub _loadServer {
    my $self = shift;
    my( $serverId ) = @_;

    $self->_log( 'chargement du serveur IMAP d\'identifiant \''.$serverId.'\'', 3 );

    require OBM::Cyrus::cyrusServer;
    $self->{'servers'}->{$serverId} = OBM::Cyrus::cyrusServer->new( $serverId );

    if( !defined($self->{'servers'}->{$serverId}) ) {
        $self->_log( 'serveur d\'identifiant \''.$serverId.'\' non trouvé', 3 );
    }

    return $self->{'servers'}->{$serverId};
}


sub getEntityCyrusServer {
    my $self = shift;
    my( $entity ) = @_;

    if( !ref($entity) ) {
        $self->_log( 'entité incorrecte', 3 );
        return undef;
    }

    if( $self->_checkServerIds( $entity->getMailServerId(), $entity->getDomainId() ) ) {
        return undef;
    }

    $self->_log( 'obtention du serveur '.$self->{'servers'}->{$entity->getMailServerId()}->getDescription(), 4 );

    return $self->{'servers'}->{$entity->getMailServerId()};
}


sub getCyrusServerById {
    my $self = shift;
    my( $serverId, $domainId ) = @_;

    if( $self->_checkServerIds( $serverId, $domainId ) ) {
        return undef;
    }

    $self->_log( 'obtention de la description de '.$self->{'servers'}->{$serverId}->getDescription(), 3 );
    return $self->{'servers'}->{$serverId};
}


sub getCyrusServerIp {
    my $self = shift;
    my( $serverId, $domainId ) = @_;

    if( $self->_checkServerIds( $serverId, $domainId ) ) {
        return undef;
    }

    $self->_log( 'obtention du nom d\'hôte du '.$self->{'servers'}->{$serverId}->getDescription(), 3 );
    return $self->{'servers'}->{$serverId}->getCyrusServerIp();
}
