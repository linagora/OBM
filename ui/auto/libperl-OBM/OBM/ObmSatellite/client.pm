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


package OBM::ObmSatellite::client;

$VERSION = '1.0';

use Class::Singleton;
use OBM::Log::log;
@ISA = ('Class::Singleton', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

require LWP::UserAgent;
require HTTP::Request;


use constant OBM_SATELLITE_LOGIN => 'obmsatelliterequest';
use constant OBM_SATELLITE_PORT => 30000;
use constant OBM_SATELLITE_CONNECTION_TIMEOUT => 30;


sub _new_instance {
    my $class = shift;

    my $self = bless { }, $class;

    $self->{'obmSatelliteLogin'} = OBM_SATELLITE_LOGIN;
    $self->{'obmSatellitePort'} = OBM_SATELLITE_PORT;
    if( $self->_getLoginPasswd() ) {
        $self->_log( 'erreur à l\'obtention du mot de passe de l\'utilisateur système '.$self->{'obmSatelliteLogin'}, 1 );
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );
}


sub _getLoginPasswd {
    my $self = shift;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return 1;
    }

    my $query = 'SELECT usersystem_password
                 FROM UserSystem
                 WHERE usersystem_login=\''.$self->{'obmSatelliteLogin'}.'\'
                 LIMIT 1';

    my $stHandler;
    if( !defined($dbHandler->execQuery( $query, \$stHandler )) ) {
        $self->_log( 'obtention du mot de passe de l\'utilisateur système '.$self->{'obmSatelliteLogin'}.' impossible', 1 );
        return 1;
    }

    if( my $result = $stHandler->fetchrow_hashref() ) {
        $self->{'obmSatellitePassword'} = $result->{'usersystem_password'};
    }
    $stHandler->finish();

    if( !defined($self->{'obmSatellitePassword'}) ) {
        $self->_log( 'obtention du mot de passe de l\'utilisateur système '.$self->{'obmSatelliteLogin'}.' impossible', 1 );
        return 1;
    }

    $self->_log( 'mot de passe de l\'utilisateur système '.$self->{'obmSatelliteLogin'}.' : '.$self->{'obmSatellitePassword'}, 4 );
    return 0;
}


sub get {
    my $self = shift;
    my( $host, $path ) = @_;

    my $url = $self->_checkUrl( $host, $path );

    if( !defined($url) ) {
        $self->_log( 'URL incorrecte', 1 );
        return 1;
    }

    my $request = HTTP::Request->new( GET => $url );
    if( !$request ) {
        $self->_log( 'erreur à l\'initialisation de la requête', 1 );
        return 1;
    }

    # Add authentication headers
    $request->authorization_basic( $self->{'obmSatelliteLogin'}, $self->{'obmSatellitePassword'} );

    my $ua = LWP::UserAgent->new();
    if( !$ua ) {
        $self->_log( 'erreur à l\'initialisation du navigateur LWP::UserAgent', 0 );
        return 1;
    }

    my $response;
    eval {
        local $SIG{ALRM} = sub {
            $self->_log('échec de connexion à l\'hote '.$host, 0);
            die 'connection timeout'."\n";
        };

        alarm OBM_SATELLITE_CONNECTION_TIMEOUT;
        $response = $ua->request($request);
        alarm 0;
    };

    return $self->_displayResponse( $url, $response );
}


sub post {
    my $self = shift;
    my( $host, $path, $content ) = @_;

    my $url = $self->_checkUrl( $host, $path );

    if( !defined($url) ) {
        $self->_log( 'URL incorrecte', 1 );
        return 1;
    }

    my $request = HTTP::Request->new( POST => $url );
    if( !$request ) {
        $self->_log( 'erreur à l\'initialisation de la requête', 1 );
        return 1;
    }

    # Adding content to request
    $request->content( $content ) if defined($content);

    # Add authentication headers
    $request->authorization_basic( $self->{'obmSatelliteLogin'}, $self->{'obmSatellitePassword'} );

    my $ua = LWP::UserAgent->new();
    if( !$ua ) {
        $self->_log( 'erreur à l\'initialisation du navigateur LWP::UserAgent', 0 );
        return 1;
    }

    my $response;
    eval {
        local $SIG{ALRM} = sub {
            $self->_log('échec de connexion à l\'hote '.$host, 0);
            die 'connection timeout'."\n";
        };

        alarm OBM_SATELLITE_CONNECTION_TIMEOUT;
        $response = $ua->request($request);
        alarm 0;
    };

    return $self->_displayResponse( $url, $response );
}


sub put {
    my $self = shift;
    my( $host, $path, $content ) = @_;

    my $url = $self->_checkUrl( $host, $path );

    if( !defined($url) ) {
        $self->_log( 'URL incorrecte', 1 );
        return 1;
    }

    my $request = HTTP::Request->new( PUT => $url );
    if( !$request ) {
        $self->_log( 'erreur à l\'initialisation de la requête', 1 );
        return 1;
    }

    # Adding content to request
    $request->content( $content ) if defined($content);

    # Add authentication headers
    $request->authorization_basic( $self->{'obmSatelliteLogin'}, $self->{'obmSatellitePassword'} );

    my $ua = LWP::UserAgent->new();
    if( !$ua ) {
        $self->_log( 'erreur à l\'initialisation du navigateur LWP::UserAgent', 0 );
        return 1;
    }

    my $response;
    eval {
        local $SIG{ALRM} = sub {
            $self->_log('échec de connexion à l\'hote '.$host, 0);
            $response = undef;
            die 'connection timeout'."\n";
        };

        alarm OBM_SATELLITE_CONNECTION_TIMEOUT;
        $response = $ua->request($request);
        alarm 0;
    };

    return $self->_displayResponse( $url, $response );
}


sub _displayResponse {
    my $self = shift;
    my( $url, $response ) = @_;


    if( !$response->is_success() ) {
        $self->_log( 'Erreur lors de la requête \''.$url.'\' : '.$response->status_line(), 1 );
        $self->_log( $response->headers_as_string(), 4 ) if $response->headers_as_string();
        $self->_log( $response->content(), 1 ) if $response->content();
        return 1;
    }

    $self->_log( 'requête \''.$url.'\' : '.$response->status_line(), 3 );
    $self->_log( $response->headers_as_string(), 4 ) if $response->headers_as_string();
    $self->_log( $response->content(), 4 ) if $response->content();

    return 0;
}


sub _checkUrl {
    my $self = shift;
    my( $host, $path ) = @_;

    if( !$host ) {
        $self->_log( 'hote inconnu', 1 );
        return undef;
    }

    if( !$path ) {
        $path = '/';
    }elsif( $path !~ /^\// ) {
        $path = '/'.$path;
    }

    return 'https://'.$host.':'.$self->{'obmSatellitePort'}.$path;
}
