package ObmSatellite::Services::LDAP;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use base qw( Class::Singleton );
require Config::IniFiles;
require ObmSatellite::Log::log;

use constant OBM_CONF => '/etc/obm/obm_conf.ini';


sub _new_instance {
    my $class = shift;
    my( $confFile ) = @_;

    my $self = bless { }, $class;

    my $ldapDesc = $self->_loadObmConf();
    my $ldapDescConfFile = $self->_loadConfFile( $confFile );

    if( !defined($ldapDesc) && !defined($ldapDescConfFile) ) {
        $self->log( 0, 'No LDAP server configuration' );
        return undef;
    }elsif( defined($ldapDescConfFile) ) {
        while( my( $option, $value ) = each(%{$ldapDescConfFile}) ) {
            $ldapDesc->{$option} = $value;
        }
    }


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
        $self->{'ldap_root'} = $ldapDesc->{'ldap_root'};

    }else {
        $self->log( 0, 'ldap_server not defined or incorrect in configuration file' );
        return undef;
    }

    return $self;
}


sub _loadObmConf {
    my $self = shift;
    my %ldapDesc;

    if( !(-f OBM_CONF && -r OBM_CONF) ) {
        return undef;
    }

    my $cfgFile = Config::IniFiles->new( -file => OBM_CONF );
    return undef if !defined($cfgFile);

    my $iniValue = $cfgFile->val( 'automate', 'ldapServer' );
    $ldapDesc{'ldap_server'} = $iniValue if $iniValue;

    $iniValue = $cfgFile->val( 'automate', 'ldapTls' );
    $ldapDesc{'ldap_server_tls'} = $iniValue if $iniValue;

    $iniValue = $cfgFile->val( 'automate', 'ldapRoot' );
    if( $iniValue ) {
        my @root = split( /,/, $iniValue );
        $iniValue = '';
        while( my $part = pop(@root) ) {
            if( $iniValue ) {
                $iniValue = ','.$iniValue;
            }

            $iniValue = 'dc='.$part.$iniValue;
        }
    }
    $ldapDesc{'ldap_root'} = $iniValue if $iniValue;

    return \%ldapDesc;
}


sub _loadConfFile {
    my $self = shift;
    my( $confFile ) = @_;
    my %ldapDesc;

    if( !(-f $confFile && -r $confFile) ) {
        return undef;
    }

    my $cfgFile = Config::IniFiles->new( -file => $confFile );
    return undef if !defined($cfgFile);

    my $iniValue = $cfgFile->val( 'server', 'ldapServer' );
    $ldapDesc{'ldap_server'} = $iniValue if $iniValue;

    $iniValue = $cfgFile->val( 'server', 'ldapTls' );
    $ldapDesc{'ldap_server_tls'} = $iniValue if $iniValue;

    $iniValue = $cfgFile->val( 'server', 'ldapRoot' );
    $ldapDesc{'ldap_root'} = $iniValue if $iniValue;

    $iniValue = $cfgFile->val( 'server', 'ldapLogin' );
    $ldapDesc{'ldap_login'} = $iniValue if $iniValue;

    $iniValue = $cfgFile->val( 'server', 'ldapPassword' );
    $ldapDesc{'ldap_password'} = $iniValue if $iniValue;
}


sub log {
    my $self = shift;

    my $log = ObmSatellite::Log::log->instance();
    return $log->log( @_ );
}


sub DESTROY {
    my $self = shift;


    $self->log( 0, 'Deleting LDAP server' );

    if( ref( $self->{'ldapServerConn'} ) eq 'Net::LDAP' ) {
        $self->disconnect();
    }else {
        undef $self->{'ldapServerConn'};
    }
}


sub disconnect {
    my $self = shift;

    if( ref( $self->{'ldapServerConn'} ) ne 'Net::LDAP' ) {
        $self->log( 3, 'LDAP not connected' );

        $self->{'ldapServerConn'} = undef;
        return 0;
    }

    if( $self->_ping($self->{'ldapServerConn'}) ) {
        $self->log( 3, 'Disconnect from LDAP server' );
        # Trying to unbind silently...
        eval{ $self->{'ldapServerConn'}->disconnect() };

        $self->{'ldapServerConn'} = undef;
        return 0;
    }

    $self->log( 3, 'LDAP already disconnected' );

    $self->{'ldapServerConn'} = undef;
    return 0;
}


sub getConn {
    my $self = shift;

    if( ref( $self->{'ldapServerConn'} ) ne 'Net::LDAP' ) {
        if( $self->{'ldapServerConn'} = $self->_connect() ) {
            $self->_searchAuthenticate();
        }
    }elsif( !$self->_ping($self->{'ldapServerConn'}) ) {
        $self->log( 4, 'LDAP server ping failed. Try to reconnect...' );
        $self->{'ldapServerConn'} = undef;
        return $self->getConn();
    }else {
        $self->log( 4, 'LDAP server connection already established' );
    }

    return $self->{'ldapServerConn'};
}


sub _connect {
    my $self = shift;
    my $ldapServerConn;


    if( $self->getDeadStatus() ) {
        $self->log( 0, 'LDAP server is disable' );
        return undef;
    }

    $self->log( 3, 'connect LDAP server '.$self->{'ldap_server'}.'...' );

    my @tempo = ( 1, 3, 5, 10, 20, 30 );
    require Net::LDAP;
    while( !($ldapServerConn = Net::LDAP->new( $self->{'ldap_server'}, debug => '0', timeout => '60', version => '3' )) ) {
        my $tempo = shift(@tempo);
        if( !defined($tempo) ) {
            last;
        }

        $self->log( 0, 'LDAP connection failed. Retry in '.$tempo.'s' );
        sleep $tempo;
    }

    if( !$ldapServerConn ) {
        $self->log( 0, 'Can\'t connect LDAP server. Disabling LDAP server' );
        $self->_setDeadStatus();
        return undef;
    }


    use Net::LDAP qw(LDAP_EXTENSION_START_TLS);
    my $ldapDse = $ldapServerConn->root_dse();
    if( !defined($ldapDse) ) {
        $self->log( 0, 'Can\'t get LDAP root DSE. Can\'t check for TLS/SSL server support. Check server ACLs' );
    }elsif( !$ldapDse->supported_extension(LDAP_EXTENSION_START_TLS) ) {
        $self->log( 0, 'no TLS/SSL LDAP server support' );
        $self->{'ldap_server_tls'} = 'none';
    }

    use Net::LDAP qw(LDAP_CONFIDENTIALITY_REQUIRED);
    if( $self->{'ldap_server_tls'} =~ /^(may|encrypt)$/ ) {
        my $error = $ldapServerConn->start_tls( verify => 'none' );

        if( $error->code() && ($self->{'ldap_server_tls'} eq 'encrypt') ) {
            $self->log( 0, 'fatal error on start_tls : '.$error->error );
            $self->log( 0, 'TLS connection needed by configuration.  Disabling LDAP server' );
            $self->_setDeadStatus();
            return undef;
        }

        if( $error->code() && ($self->{'ldap_server_tls'} eq 'may') ) {
            $self->log( 0, 'fatal error on start_tls : '.$error->error );
            $self->log( 0, 'TLS not needed, trying to reconnect without TLS' );
            $self->{'ldap_server_tls'} = 'none';

            $ldapServerConn = undef;
            return $self->_connect();
        }

        if( !$error->code() ) {
            $self->log( 3, 'TLS connection established' );
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
        $self->log( 2, 'Authenticating to LDAP server as user DN '.$self->{'ldap_login'} );
        $error = $self->{'ldapServerConn'}->bind(
            $self->{'ldap_login'},
            password => $self->{'ldap_password'}
        );
    }else {
        $self->log( 2, 'Authenticating anonymously' );
        $error = $self->{'ldapServerConn'}->bind();
    }

    if( !$error->code ) {
        $self->log( 2, 'LDAP connection success' );

    }elsif( $error->code == LDAP_CONFIDENTIALITY_REQUIRED ) {
        $self->log( 0, 'start_tls needed by LDAP server. Check your configuration' );
        $self->log( 0, 'disabling LDAP server' );
        $self->_setDeadStatus();
        $self->{'ldapServerConn'} = undef;
        return 1;

    }elsif( $error->code ) {
        $self->log( 0, 'fail to authenticate against LDAP server : '.$error->error );
        $self->{'ldapServerConn'} = undef;
        return 1;
    }

    return 0;
}


sub checkAuthentication {
    my $self = shift;
    my( $dn, $passwd ) = @_;

    my $ldapConn = $self->getConn();

    # LDAP authentication
    my $error = $ldapConn->bind(
        $dn,
        password => $passwd
    );

    my $returnCode = 0;
    if( !$error->code ) {
        $self->log( 2, 'LDAP authentication success for user '.$dn );
        $returnCode = 1;
    
    }elsif( $error->code == LDAP_CONFIDENTIALITY_REQUIRED ) {
        $self->log( 0, 'start_tls needed by LDAP server. Check your configuration' );
        $self->log( 0, 'disabling LDAP server' );
        $self->_setDeadStatus();
        $self->{'ldapServerConn'} = undef;

    }else {
        $self->log( 0, 'LDAP authentication fail for user '.$dn );
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

    if( !defined($ldapServerConn) ) {
        return 0;
    }

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


sub getLdapRoot {
    my $self = shift;

    return $self->{'ldap_root'};
}
