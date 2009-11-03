package ObmSatellite::Server::processRequest;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use HTTP::Status;

use constant PASSWORD_LIFE_TIME => 120;


sub process_request {
    my $self = shift;
    my $processedRequest = 0;

    while( $processedRequest < $self->{'server'}->{'max_requests'} ) {
        my $httpRequest;

        # Accept a connection from HTTP::Daemon
        my $connection = $self->{'server'}->{'socket'}->accept() or next;

        $self->log( -1, 'Connect from: '.$connection->peerhost() );
        
        # Get the request
        while( ($processedRequest < $self->{'server'}->{'max_requests'}) && ($httpRequest = $connection->get_request()) ) {
            if( ref($httpRequest) ne 'HTTP::Request' ) {
                $self->log( 0, 'Invalid request' );
                $connection->send_error(RC_BAD_REQUEST);
                next;
            }

            if( $self->_checkHttpBasicAuth( $httpRequest->authorization_basic()) ) {
                $self->processHttpRequest( $httpRequest, $connection );
            }else {
                my $res = HTTP::Response->new( 401, 'Auth Required', undef, $self->{'server'}->{'name'}.' need valid authentication' );
                $res->www_authenticate('Basic realm="'.$self->{'server'}->{'name'}.' realm"' );
                $connection->send_response($res);
            }

            $processedRequest++;
        }

        $self->log( -1, 'Disconnect from: '.$connection->peerhost());

        $connection->close();
    }

    if( $processedRequest == $self->{'server'}->{'max_requests'} ) {
        $self->log( 3, 'Maximum process requests reached ('.$processedRequest.'). Process renewed' );
    }else {
        $self->log( 3, $processedRequest.' processed requests ( maximum allowed '.$self->{'server'}->{'max_requests'}.'). Process renewed' );
    }
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

    if( !defined($self->{'httpAuthentication'}->{'validUserDn'}) ) {
        if( $self->_getHttpAuthValidUserDN() ) {
            $self->log( 0, 'Unable to find DN of valid user \''.$self->{'httpAuthentication'}->{'validUser'}.'\'' );
            return 0;
        }
    }

    # Check password against cache
    if( $self->{'httpAuthentication'}->{'validPasswdTime'} ) {
        my $currentTime = time();
        $currentTime = $currentTime - $self->{'httpAuthentication'}->{'validPasswdTime'};

        # Disable SIGDIE handler to load only valid modules without fatal error
        my $oldSigDie = $SIG{__DIE__};
        $SIG{__DIE__} = undef;

        my $currentPasswdSsha;
        eval {
            require Digest::SHA;
        } or ($self->log( 0, 'Digest::SHA module needed' ) && exit 10); 

        # Re-enable SIGDIE handler
        $SIG{__DIE__} = $oldSigDie;

        $currentPasswdSsha = Digest::SHA::sha256_hex($pass);


        if( ($currentTime < PASSWORD_LIFE_TIME) && defined($self->{'httpAuthentication'}->{'validPasswd'}) && ($self->{'httpAuthentication'}->{'validPasswd'} eq $currentPasswdSsha) ) {
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

    # Disable SIGDIE handler to load only valid modules without fatal error
    my $oldSigDie = $SIG{__DIE__};
    $SIG{__DIE__} = undef;

    $self->log( 3, 'Basic HTTP authentication : LDAP check HTTP basic password success !' );
    eval {
        require Digest::SHA;
    } or ($self->log( 0, 'Digest::SHA module needed' ) && exit 10); 

    # Re-enable SIGDIE handler
    $SIG{__DIE__} = $oldSigDie;

    $self->{'httpAuthentication'}->{'validPasswd'} = Digest::SHA::sha256_hex($pass);
    $self->{'httpAuthentication'}->{'validPasswdTime'} = time();

    return 1;
}


sub _getHttpAuthValidUserDN {
    my $self = shift;
    my $validUser = $self->{'httpAuthentication'}->{'validUser'};

    eval {
        require ObmSatellite::Services::LDAP;
    } or ($self->log( 0, 'Unable to load LDAP service' ) && return 1);
    my $ldapServer = ObmSatellite::Services::LDAP->instance();

    if( !defined($ldapServer) ) {
        $self->log( 0, 'Unable to load LDAP service' );
        return 1;
    }

    my $ldapConn;
    if( !($ldapConn = $ldapServer->getConn()) ) {
        return 1;
    }

    my $ldapRoot = $ldapServer->getLdapRoot();

    my $ldapFilter = $self->{'httpAuthentication'}->{'ldapFilter'};
    $ldapFilter =~ s/%u/$self->{'httpAuthentication'}->{'validUser'}/g;

    $self->log( 4, 'Search LDAP root \''.$ldapRoot.'\', filter '.$ldapFilter ) if $ldapRoot;
    $self->log( 4, 'Search default LDAP server root, filter '.$ldapFilter ) if !$ldapRoot;

    my $ldapResult;
    if( $ldapRoot ) {
        $ldapResult = $ldapConn->search(
                            base => $ldapRoot,
                            scope => 'sub',
                            filter => $ldapFilter,
                            attrs => [ 'uid' ]
                        );
    }else {
        $ldapResult = $ldapConn->search(
                            scope => 'sub',
                            filter => $ldapFilter,
                            attrs => [ 'uid' ]
                        );
    }

    if( $ldapResult->is_error() ) {
        $self->log( 0, 'LDAP search fail on error : '.$ldapResult->error() );
        return 1;
    }

    my @results = $ldapResult->entries();
    if( $#results < 0 ) {
        $self->log( 0, 'User \''.$validUser.'\' not found in LDAP' );
        return 1;
    }elsif( $#results > 0 ) {
        $self->log( 0, 'More than one user \''.$validUser.'\' LDAP entries found' );
        return 1;
    }

    $self->{'httpAuthentication'}->{'validUserDn'} = $results[0]->dn();
    $self->log( 3, 'DN of valid user HTTP basic authentication : '.$self->{'httpAuthentication'}->{'validUserDn'} );

    return 0;
}
