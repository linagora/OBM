package ObmSatellite::Server::processRequest;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use HTTP::Status;

use constant PASSWORD_LIFE_TIME => 120;
use constant SOCKET_TIMEOUT => 30;


sub process_request {
    my $self = shift;
    my $processedRequest = 0;

    while( $processedRequest < $self->{'server'}->{'max_requests'} ) {
        my $httpRequest;

        # Setting socket timeout
        $self->{'server'}->{'socket'}->timeout(SOCKET_TIMEOUT);
        # Accept a connection from HTTP::Daemon
        my $connection = $self->{'server'}->{'socket'}->accept() or next;
        # Remove socket timeout before getting request
        $connection->timeout(0);

        $self->_log( 'Connect from: '.$connection->peerhost(), 3 );
        
        # Get the request
        while( ($processedRequest < $self->{'server'}->{'max_requests'}) && ($httpRequest = $connection->get_request()) ) {
            if(ref($httpRequest) ne 'HTTP::Request') {
                $self->_log( 'Invalid request', 0 );
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

        $self->_log( 'Disconnect from: '.$connection->peerhost(), 3 );

        $connection->close();
    }

    if( $processedRequest == $self->{'server'}->{'max_requests'} ) {
        $self->_log( 'Maximum process requests reached ('.$processedRequest.'). Process renewed', 4 );
    }else {
        $self->_log( $processedRequest.' processed requests ( maximum allowed '.$self->{'server'}->{'max_requests'}.'). Process renewed', 4 );
    }
}


sub _checkHttpBasicAuth {
    my $self = shift;
    my($user, $pass) = @_;

    if( !$user || !$pass ) {
        $self->_log( 'HTTP request not authenticated. HTTP authentication needed', 1 );
        return 0;
    }

    if( $user ne $self->{'httpAuthentication'}->{'validUser'} ) {
        $self->_log( 'HTTP authentication : invalid login \''.$user.'\'', 0 );
        return 0;
    }

    if( !defined($self->{'httpAuthentication'}->{'validUserDn'}) ) {
        if( $self->_getHttpAuthValidUserDN() ) {
            $self->_log( 'Unable to find DN of valid user \''.$self->{'httpAuthentication'}->{'validUser'}.'\'', 0 );
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
        } or ($self->_log( 'Digest::SHA module needed', 0 ) && exit 10); 

        # Re-enable SIGDIE handler
        $SIG{__DIE__} = $oldSigDie;

        $currentPasswdSsha = Digest::SHA::sha256_hex($pass);


        if( ($currentTime < PASSWORD_LIFE_TIME) && defined($self->{'httpAuthentication'}->{'validPasswd'}) && ($self->{'httpAuthentication'}->{'validPasswd'} eq $currentPasswdSsha) ) {
            $self->_log( 'Basic HTTP authentication : ckeck against cache password success', 4 );
            return 1;
        }else {
            $self->_log( 'Basic HTTP authentication : cached password expire.  Check against LDAP server', 4 );
            # Reset cached password
            delete( $self->{'httpAuthentication'}->{'validPasswdTime'} );
            delete( $self->{'httpAuthentication'}->{'validPasswd'} );
        }
    }

    eval {
        require ObmSatellite::Services::LDAP;
    } or ($self->_log( 'Unable to load LDAP service', 1 ) && return 1);
    my $ldapServer = ObmSatellite::Services::LDAP->instance();

    if( !defined($ldapServer) ) {
        $self->_log( 'Unable to load LDAP service', 1 );
        return 0;
    }

    # Check password against LDAP server
    if( !$ldapServer->checkAuthentication( $self->{'httpAuthentication'}->{'validUserDn'}, $pass ) ) {
        $self->_log( 'Basic HTTP authentication : LDAP check HTTP basic password fail !', 0 );
        return 0;
    }

    # Disable SIGDIE handler to load only valid modules without fatal error
    my $oldSigDie = $SIG{__DIE__};
    $SIG{__DIE__} = undef;

    $self->_log( 'Basic HTTP authentication : LDAP check HTTP basic password success !', 4 );
    eval {
        require Digest::SHA;
    } or ($self->_log( 'Digest::SHA module needed', 0 ) && exit 10); 

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
    } or ($self->_log( 'Unable to load LDAP service', 1 ) && return 1);
    my $ldapServer = ObmSatellite::Services::LDAP->instance();

    if( !defined($ldapServer) ) {
        $self->_log( 'Unable to load LDAP service', 1 );
        return 1;
    }

    my $ldapConn;
    if( !($ldapConn = $ldapServer->getConn()) ) {
        return 1;
    }

    my $ldapRoot = $ldapServer->getLdapRoot();

    my $ldapFilter = $self->{'httpAuthentication'}->{'ldapFilter'};
    $ldapFilter =~ s/%u/$self->{'httpAuthentication'}->{'validUser'}/g;

    $self->_log( 'Search LDAP root \''.$ldapRoot.'\', filter '.$ldapFilter, 4 ) if $ldapRoot;
    $self->_log( 'Search default LDAP server root, filter '.$ldapFilter, 4 ) if !$ldapRoot;

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
        $self->_log( 'LDAP search fail on error : '.$ldapResult->error(), 1 );
        return 1;
    }

    my @results = $ldapResult->entries();
    if( $#results < 0 ) {
        $self->_log( 'User \''.$validUser.'\' not found in LDAP', 1 );
        return 1;
    }elsif( $#results > 0 ) {
        $self->_log( 'More than one user \''.$validUser.'\' LDAP entries found', 1 );
        return 1;
    }

    $self->{'httpAuthentication'}->{'validUserDn'} = $results[0]->dn();
    $self->_log( 'DN of valid user HTTP basic authentication : '.$self->{'httpAuthentication'}->{'validUserDn'}, 4 );

    return 0;
}
