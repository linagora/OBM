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


package OBM::Postfix::smtpInServer;

$VERSION = '1.0';

use OBM::Log::log;
@ISA = ('OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub new {
    my $class = shift;
    my( $serverDesc ) = @_;

    my $self = bless { }, $class;

    if( $self->_init( $serverDesc ) ) {
        $self->_log( 'problème lors de l\'initialisation du serveur de type SMTP-in', 0 );
        return undef;
    }

    $self->{'enable'} = 1;

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub _init {
    my $self = shift;
    my( $serverDesc ) = @_;

    if( !defined($serverDesc) || (ref($serverDesc) ne 'HASH') ) {
        $self->_log( 'description du serveur de type SMTP-in invalide', 2 );
        return 1;
    }

    if( !defined($serverDesc->{'host_ip'}) && !defined($serverDesc->{'host_fqdn'}) ) {
        $self->_log( 'pas d\'adresse IP ou de nom d\'hôte associé, traitement impossible', 0 );
        return 1;
    }elsif( defined($serverDesc->{'host_ip'}) ) {
        $self->_log( 'contact de l\'hote sur son adresse IP '.$serverDesc->{'host_ip'}, 4 );
        $self->_log( 'si l\'IP est définie en BD, elle est utilisée en priorité', 4 );

        $self->{'network_name'} = $serverDesc->{'host_ip'};
    }elsif( defined($serverDesc->{'host_fqdn'}) ) {
        $self->_log( 'contact de l\'hote sur son nom \''.$serverDesc->{'host_fqdn'}.'\'', 3 );

        $self->{'network_name'} = $serverDesc->{'host_fqdn'};
    }

    $self->{'host_ip'} = $serverDesc->{'host_ip'};
    $self->{'host_fqdn'} = $serverDesc->{'host_fqdn'};

    if( !defined($serverDesc->{'host_name'}) ) {
        $self->_log( 'non d\'hôte non défini', 0 );
        return 1;
    }
    $self->{'host_name'} = $serverDesc->{'host_name'};

    $self->{'host_id'} = $serverDesc->{'host_id'};

    return 0;
}


sub getDescription {
    my $self = shift;

    my $description = 'Hôte SMTP-in \''.$self->{'host_name'}.'\''.eval{
            my $desc;
            if( defined($self->{'host_id'}) ) {
                $desc .= ' (ID '.$self->{'host_id'}.')';
            }
    
            return $desc;
        }
        .', '.$self->{'network_name'};

    return $description;
}


sub enable {
    my $self = shift;

    $self->{'enable'} = 1;

    return 0;
}


sub disable {
    my $self = shift;

    $self->{'enable'} = 0;

    return 0;
}


sub update {
    my $self = shift;
    my $errorCode = 0;

    if( !$self->{'enable'} ) {
        $self->_log( 'serveur '.$self->getDescription().' désactivé, impossible d\'effectuer la mise à jour', 0 );
        return 1;
    }

    require OBM::ObmSatellite::client;
    my $obmSatelliteClient = OBM::ObmSatellite::client->instance();
    if( !defined($obmSatelliteClient) ) {
        $self->_log( 'Echec lors de l\'initialisation du client obmSatellite', 3 );
        return 1;
    }

    return $obmSatelliteClient->post( $self->{'network_name'}, '/postfixsmtpinmaps/host/'.$self->{'host_name'} );
}
