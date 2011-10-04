package ObmSaslauthd::Ldap::ldapServer;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


# Daemon : ref to daemon object (needed to log)
# ldapDesc : hash ref. Keys: ldap_server, ldap_server_tls, ldap_login, ldap_password
sub new {
    my $class = shift;
    my( $daemon, $ldapDesc ) = @_;

    my $self = bless { }, $class;

    $self->{'daemon'} = $daemon;


    my $regexp_ip = '^((ldap|ldaps):\/\/){0,1}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)[/]{0,1}$';
    my $regexp_hostname = '^((ldap|ldaps):\/\/){0,1}[a-z0-9-]+(\.[a-z0-9-]+)*\.[a-z]{2,6}[/]{0,1}$';

    # Load LDAP configuration options
    if( defined($ldapDesc->{'ldap_server'}) && (($ldapDesc->{'ldap_server'} =~ $regexp_ip || ($ldapDesc->{'ldap_server'} =~ /$regexp_hostname/)) ) ) {
        my %ldapDesc;

        $self->{'ldap_server'} = $ldapDesc->{'ldap_server'};
        if( $self->{'ldap_server'} !~ /^(ldap|ldaps):/ ) {
            $self->{'ldap_server'} = 'ldap://'.$self->{'ldap_server'}->{'server'};
        }

        $self->{'ldap_server_tls'} = $ldapDesc->{'ldap_server_tls'};
        if( $self->{'ldap_server'} =~ /^ldaps:/ ) {
            $self->{'ldap_server_tls'} = 'none';
        }
        if( !defined($self->{'ldap_server_tls'}) || ($self->{'ldap_server_tls'} !~ /^(none|may|encrypt)$/) ) {
            $self->{'ldap_server_tls'} = 'may';
        }

        $self->{'ldap_login'} = $ldapDesc->{'ldap_login'};
        $self->{'ldap_password'} = $ldapDesc->{'ldap_password'};

    }else {
        $self->{'daemon'}->log( 0, 'ldap_server not defined or incorrect in configuration file' );
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

    if( ref( $self->{'ldapServerConn'} ) eq 'Net::LDAP' ) {
        # Trying to unbind silently...
        eval{ $self->{'ldapServerConn'}->disconnect(); };
    }else {
        undef $self->{'ldapServerConn'};
    }
}


sub getConn {
    my $self = shift;

    if( ref( $self->{'ldapServerConn'} ) ne 'Net::LDAP' ) {
        if( $self->{'ldapServerConn'} = $self->_connect() ) {
            $self->_searchAuthenticate();
        }
    }elsif( !$self->_ping($self->{'ldapServerConn'}) ) {
        $self->{'daemon'}->log( 4, 'LDAP server ping failed. Try to reconnect...' );
        $self->{'ldapServerConn'}->disconnect();
        $self->{'ldapServerConn'} = undef;
        return $self->getConn();
    }else {
        $self->{'daemon'}->log( 4, 'LDAP server connection already established' );
    }

    return $self->{'ldapServerConn'};
}


sub _connect {
    my $self = shift;
    my $ldapServerConn;

    if( $self->getDeadStatus() ) {
        $self->{'daemon'}->log( 0, 'LDAP server is disable' );
        return undef;
    }

    $self->{'daemon'}->log( 3, 'connect LDAP server...' );

    my @tempo = ( 1, 3, 5, 10, 20, 30 );
    require Net::LDAP;
    while( !($ldapServerConn = Net::LDAP->new( $self->{'ldap_server'}, debug => '0', timeout => '60', version => '3' )) ) {
        my $tempo = shift(@tempo);
        if( !defined($tempo) ) {
            last;
        }

        $self->{'daemon'}->log( 0, 'LDAP connection failed. Retry in '.$tempo.'s' );
        sleep $tempo;
    }

    if( !$ldapServerConn ) {
        $self->{'daemon'}->log( 0, 'Can\'t connect LDAP server. Disabling LDAP server' );
        $self->_setDeadStatus();
        return undef;
    }


    use Net::LDAP qw(LDAP_EXTENSION_START_TLS);
    my $ldapDse = $ldapServerConn->root_dse();
    if( !defined($ldapDse) ) {
        $self->{'daemon'}->log( 0, 'Can\'t get LDAP root DSE. Can\'t check for TLS/SSL server support. Check server ACLs' );
    }elsif( !$ldapDse->supported_extension(LDAP_EXTENSION_START_TLS) ) {
        $self->{'daemon'}->log( 0, 'no TLS/SSL LDAP server support' );
        $self->{'ldap_server_tls'} = 'none';
    }

    use Net::LDAP qw(LDAP_CONFIDENTIALITY_REQUIRED);
    if( $self->{'ldap_server_tls'} =~ /^(may|encrypt)$/ ) {
        my $error = $ldapServerConn->start_tls( verify => 'none' );

        if( $error->code() && ($self->{'ldap_server_tls'} eq 'encrypt') ) {
            $self->{'daemon'}->log( 0, 'fatal error on start_tls : '.$error->error );
            $self->{'daemon'}->log( 0, 'TLS connection needed by configuration.  Disabling LDAP server' );
            $self->_setDeadStatus();
            return undef;
        }

        if( $error->code() && ($self->{'ldap_server_tls'} eq 'may') ) {
            $self->{'daemon'}->log( 0, 'fatal error on start_tls : '.$error->error );
            $self->{'daemon'}->log( 0, 'TLS not needed, trying to reconnect without TLS' );
            $self->{'ldap_server_tls'} = 'none';

            $ldapServerConn = undef;
            return $self->_connect();
        }

        if( !$error->code() ) {
            $self->{'daemon'}->log( 3, 'TLS connection established' );
        }
    }

    return $ldapServerConn;
}


# Must be connect to LDAP server before calling
sub _searchAuthenticate {
    my $self = shift;

    # LDAP authentication
    my $error; 
    if( $self->{'ldap_login'} ) {
        $self->{'daemon'}->log( 2, 'Authenticating to LDAP server as user DN '.$self->{'ldap_login'} );
        $error = $self->{'ldapServerConn'}->bind(
            $self->{'ldap_login'},
            password => $self->{'ldap_password'}
        );
    }else {
        $self->{'daemon'}->log( 2, 'Authenticating anonymously as user DN' );
        $error = $self->{'ldapServerConn'}->bind();
    }

    if( !$error->code ) {
        $self->{'daemon'}->log( 2, 'LDAP connection success' );

    }elsif( $error->code == LDAP_CONFIDENTIALITY_REQUIRED ) {
        $self->{'daemon'}->log( 0, 'start_tls needed by LDAP server. Check your configuration' );
        $self->{'daemon'}->log( 0, 'disabling LDAP server' );
        $self->_setDeadStatus();
        $self->{'ldapServerConn'} = undef;
        return 1;

    }elsif( $error->code ) {
        $self->{'daemon'}->log( 0, 'fail to authenticate against LDAP server : '.$error->error );
        $self->{'ldapServerConn'} = undef;
        return 1;
    }

    return 0;
}


sub checkAuthentication {
    my $self = shift;
    my( $request ) = @_;

    my $ldapConn = $self->getConn();

    # LDAP authentication
    my $error = $ldapConn->bind(
        $request->getDn(),
        password => $request->getPasswd()
    );

    my $returnCode = 0;
    if( !$error->code ) {
        $self->{'daemon'}->log( 2, 'LDAP authentication success for user '.$request->getDn() );
        $returnCode = 1;
    
    }elsif( $error->code == LDAP_CONFIDENTIALITY_REQUIRED ) {
        $self->{'daemon'}->log( 0, 'start_tls needed by LDAP server. Check your configuration' );
        $self->{'daemon'}->log( 0, 'disabling LDAP server' );
        $self->_setDeadStatus();
        $self->{'ldapServerConn'} = undef;

    }else {
        $self->{'daemon'}->log( 0, 'LDAP authentication fail for user '.$request->getDn() );
    }

    $self->_searchAuthenticate();
    return $returnCode;
}



sub getDeadStatus {
    my $self = shift;

    return $self->{'deadStatus'};
}


sub _setDeadStatus {
    my $self = shift;

    $self->{'deadStatus'} = 1;

    return 0;
}


sub _unsetDeadStatus {
    my $self = shift;

    $self->{'ldapServerConn'} = undef;
    $self->{'deadStatus'} = 0;

    return 0;
}


sub _ping {
    my $self = shift;
    my( $ldapServerConn ) = @_;

    my $result = $ldapServerConn->search(
                    scope => 'base',
                    filter => '(objectclass=*)',
                    sizelimit => 1
                    );

    if( $result->is_error() ) {
        return 0;
    }

    return 1;
}
