package lib::client;

$VERSION = '1.0';

use Class::Singleton;
@ISA = ('Class::Singleton');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

require LWP::UserAgent;
require HTTP::Request;


use constant OBM_SATELLITE_LOGIN => 'obmsatelliterequest';
use constant OBM_SATELLITE_PORT => 30000;


sub _new_instance {
    my $class = shift;

    my $self = bless { }, $class;

    $self->{'obmSatelliteLogin'} = OBM_SATELLITE_LOGIN;
    $self->{'obmSatellitePort'} = OBM_SATELLITE_PORT;
    if( $self->_getLoginPasswd() ) {
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

}


sub _getLoginPasswd {
    my $self = shift;

    require 'lib/obmDbHandler.pm';
    my $dbHandler = lib::obmDbHandler->instance();

    if( !$dbHandler ) {
        return 1;
    }

    my $query = 'SELECT usersystem_password
                 FROM UserSystem
                 WHERE usersystem_login=\''.$self->{'obmSatelliteLogin'}.'\'
                 LIMIT 1';

    my $stHandler;
    if( !defined($dbHandler->execQuery( $query, \$stHandler )) ) {
        return 1;
    }

    if( my $result = $stHandler->fetchrow_hashref() ) {
        $self->{'obmSatellitePassword'} = $result->{'usersystem_password'};
    }
    $stHandler->finish();

    if( !defined($self->{'obmSatellitePassword'}) ) {
        return 1;
    }

    return 0;
}


sub get {
    my $self = shift;
    my( $host, $path ) = @_;

    my $url = $self->_checkUrl( $host, $path );

    if( !defined($url) ) {
        return 1;
    }

    my $request = HTTP::Request->new( GET => $url );
    if( !$request ) {
        return 1;
    }

    # Add authentication headers
    $request->authorization_basic( $self->{'obmSatelliteLogin'}, $self->{'obmSatellitePassword'} );

    my $ua = LWP::UserAgent->new();
    if( !$ua ) {
        return 1;
    }

    my $response = $ua->request($request);
    return $self->_displayResponse( $url, $response );
}


sub post {
    my $self = shift;
    my( $host, $path, $content ) = @_;

    my $url = $self->_checkUrl( $host, $path );

    if( !defined($url) ) {
        return 1;
    }

    my $request = HTTP::Request->new( POST => $url );
    if( !$request ) {
        return 1;
    }

    # Adding content to request
    $request->content( $content ) if defined($content);

    # Add authentication headers
    $request->authorization_basic( $self->{'obmSatelliteLogin'}, $self->{'obmSatellitePassword'} );

    my $ua = LWP::UserAgent->new();
    if( !$ua ) {
        return 1;
    }

    my $response = $ua->request($request);
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

    my $response = $ua->request($request);
    return $self->_displayResponse( $url, $response );
}


sub _displayResponse {
    my $self = shift;
    my( $url, $response ) = @_;


    if( !$response->is_success() ) {
        return 1;
    }


    return 0;
}


sub _checkUrl {
    my $self = shift;
    my( $host, $path ) = @_;

    if( !$host ) {
        return undef;
    }

    if( !$path ) {
        $path = '/';
    }elsif( $path !~ /^\// ) {
        $path = '/'.$path;
    }

    return 'https://'.$host.':'.$self->{'obmSatellitePort'}.$path;
}
