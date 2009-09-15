package ObmSatellite::Server::processRequest;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use Digest::SHA qw(sha256_hex);
use HTTP::Status;

use constant PASSWORD_LIFE_TIME => 120;


sub process_request {
    my $self = shift;
    my $processedRequest = 0;

    while( $processedRequest < $self->{'server'}->{'max_requests'} ) {
        my $httpRequest;

        # Accept a connection from HTTP::Daemon
        my $connection = $self->{'server'}->{'socket'}->accept() or next;

        $self->log( 0, 'Connect from: '.$connection->peerhost() );
        
        # Get the request
        while( ($processedRequest < $self->{'server'}->{'max_requests'}) && ($httpRequest = $connection->get_request()) ) {
            if( ref($httpRequest) ne 'HTTP::Request' ) {
                $self->log( 0, 'Invalid request' );
                $connection->send_error(RC_BAD_REQUEST);
                next;
            }

            if( $self->_checkHttpBasicAuth( $httpRequest->authorization_basic()) ) {
                $processedRequest++;
                $self->processHttpRequest( $httpRequest, $connection );
            }else {
                my $res = HTTP::Response->new( 401, 'Auth Required', undef, $self->{'server'}->{'name'}.' need valid authentication' );
                $res->www_authenticate('Basic realm="'.$self->{'server'}->{'name'}.' realm"' );
                $connection->send_response($res);
            }

        }

        $self->log( 2, 'Disconnect from: '.$connection->peerhost());

        $connection->close();
    }

    if( $processedRequest == $self->{'server'}->{'max_requests'} ) {
        $self->log( 3, 'Maximum process requests reached ('.$processedRequest.'). Process renewed' );
    }else {
        $self->log( 3, $processedRequest.' processed requests ( maximum allowed '.$self->{'server'}->{'max_requests'}.'). Process renewed' );
    }


    $self->log( 0, 'fin' );
}


sub _checkHttpBasicAuth {
    my $self = shift;
    my($user, $pass) = @_;

    if( !$user || !$pass ) {
        $self->log( 0, 'HTTP request not authenticated. HTTP authentication needed' );
        return 0;
    }

    if( $user ne $self->{'httpAuthentication'}->{'validUser'} ) {
        $self->log( 0, 'HTTP authentication : invalid login \''.$user.'\'' );
        return 0;
    }

    # Check password against cache
    if( $self->{'httpAuthentication'}->{'validPasswdTime'} ) {
        my $currentTime = time();
        $currentTime = $currentTime - $self->{'httpAuthentication'}->{'validPasswdTime'};

        if( ($currentTime < PASSWORD_LIFE_TIME) && defined($self->{'httpAuthentication'}->{'validPasswd'}) && ($self->{'httpAuthentication'}->{'validPasswd'} eq sha256_hex($pass)) ) {
            $self->log( 3, 'Basic HTTP authentication : ckeck against cache password success' );
            return 1;
        }else {
            $self->log( 3, 'Basic HTTP authentication : cached password expire. Check against LDAP server' );
            # Reset cached password
            delete( $self->{'httpAuthentication'}->{'validPasswdTime'} );
            delete( $self->{'httpAuthentication'}->{'validPasswd'} );
        }
    }

    eval {
        require ObmSatellite::Services::LDAP;
    } or ($self->log( 0, 'Unable to load LDAP service' ) && return 1);
    my $ldapServer = ObmSatellite::Services::LDAP->instance();

    if( !defined($ldapServer) ) {
        $self->log( 0, 'Unable to load LDAP service' );
        return 0;
    }

    # Check password against LDAP server
    if( !$ldapServer->checkAuthentication( $self->{'httpAuthentication'}->{'validUserDn'}, $pass ) ) {
        $self->log( 0, 'Basic HTTP authentication : LDAP check HTTP basic password fail !' );
        return 0;
    }

    $self->log( 3, 'Basic HTTP authentication : LDAP check HTTP basic password success !' );
    $self->{'httpAuthentication'}->{'validPasswd'} = sha256_hex($pass);
    $self->{'httpAuthentication'}->{'validPasswdTime'} = time();

    return 1;
}
