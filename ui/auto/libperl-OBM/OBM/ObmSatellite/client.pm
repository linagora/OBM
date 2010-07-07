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
        $self->_log( $response->content(), 1 ) if $response->content();
        return 1;
    }

    $self->_log( 'requête \''.$url.'\' : '.$response->status_line(), 2 );
    $self->_log( $response->headers_as_string(), 4 ) if $response->headers_as_string();
    $self->_log( $response->content(), 2 ) if $response->content();

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
