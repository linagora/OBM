package ObmSatellite::Server::configure;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

eval {
    require Config::IniFiles;
} or die 'Config::IniFiles perl module needed !'."\n";

use constant HTTP_PORT => 30000;
use constant MAX_PROCESS => 2;
use constant MAX_REQUEST_PER_PROCESS => 10;
use constant CONF_DIR => '/etc/obm-satellite';
use constant LOG_DIR => '/var/log/obm';
use constant LOG_LEVEL => 2;
use constant SOCKET_TIMEOUT => 30;
use constant SSL_PROTOCOL_VERSION => 'SSLv2/3';
use constant SSL_CERT_DIR => '/etc/obm/certs';
use constant PID_DIR => '/var/run/obm';
use constant HTTP_AUTHENTICATION_VALID_USER => 'obmsatelliterequest';
use constant HTTP_AUTHENTICATION_LDAP_FILTER => '(&(uid=%u)(objectclass=obmSystemUser))';
use constant OBM_CONF => '/etc/obm/obm_conf.ini';


sub _configure {
    my $self = shift;

    $self->{'server'}->{'name'} = 'obmSatellite';
    $self->{'server'}->{'conf_file'} = CONF_DIR.'/'.$self->{'server'}->{'name'}.'.ini';

    @{$self->{'server'}->{'mods_enabled'}} = <CONF_DIR/mods-enabled/*>;

    $self->{'server'}->{'setsid'} = 1;

#    $self->{'server'}->{'chdir'} = '/tmp';
#    $self->{'server'}->{'no_close_by_child'} = 1;   # Daemon connot be stopped by childs
    $self->{'server'}->{'max_spare_servers'} = MAX_PROCESS;     # max process
    $self->{'server'}->{'max_requests'} = MAX_REQUEST_PER_PROCESS;    # max queries per process

    $self->_configureSSL();

    # PID file
    $self->{'server'}->{'pid_file'} = PID_DIR.'/'.$self->{'server'}->{'name'}.'.pid';

    # Log
    $self->{'logFile'} = LOG_DIR.'/'.$self->{'server'}->{'name'}.'.log';
    $self->{'logLevel'} = LOG_LEVEL;

    $self->_loadConfFile();

    # Initialize log
    require ObmSatellite::Log::log;
    $self->{'logger'} = ObmSatellite::Log::log->instance( $self->{'logFile'}, $self->{'logLevel'} );

    # Check SSL parameters
    $self->_checkSSL();

    # HTTP authentication
    $self->{'httpAuthentication'}->{'validUser'} = HTTP_AUTHENTICATION_VALID_USER;
    $self->{'httpAuthentication'}->{'ldapFilter'} = HTTP_AUTHENTICATION_LDAP_FILTER;
}


sub _configurePreBind {
    my $self = shift;

    if( $self->_startServices( [ 'LDAP' ] ) ) {
        $self->log( 2, 'Fail to load LDAP service, needed to HTTP basic authentication' );
        exit 1;
    }

    if( $self->_getHttpAuthValidUserDN() ) {
        $self->log( 0, 'Unable to find DN of valid user \''.$self->{'httpAuthentication'}->{'validUser'}.'\'' );
        exit 1;
    }

    # Disconnect from LDAP server before forking...
    $self->{'services'}->{'LDAP'}->disconnect();

    # Load modules
    if( $self->_loadModules() ) {
        $self->log( 2, 'Modules initialization fail' );
        exit 1;
    }
}


sub _loadConfFile {
    my $self = shift;

    if( ! -f $self->{'server'}->{'conf_file'} || ! -r $self->{'server'}->{'conf_file'} ) {
        print 'WARNING: Unable to open configuration file '.$self->{'server'}->{'conf_file'}."\n";
        return;
    }

    if( my $cfgFile = Config::IniFiles->new( -file => $self->{'server'}->{'conf_file'} ) ) {
        # SSL key
        my $iniValue = $cfgFile->val( 'server', 'ssl-key-file' );
        $self->{'server'}->{'socketConf'}->{'SSL_key_file'} = $iniValue if $iniValue;

        # SSL certificate
        $iniValue = $cfgFile->val( 'server', 'ssl-cert-file' );
        $self->{'server'}->{'socketConf'}->{'SSL_cert_file'} = $iniValue if $iniValue;

        # SSL CA certificate
        $iniValue = $cfgFile->val( 'server', 'ssl-ca-file' );
        $self->{'server'}->{'socketConf'}->{'SSL_ca_file'} = $iniValue if $iniValue;

        # Maximum prefork child
        $iniValue = $cfgFile->val( 'server', 'max-spare-server' );
        $self->{'server'}->{'max_spare_servers'} = $iniValue if $iniValue;

        # Log level
        $iniValue = $cfgFile->val( 'server', 'log-level' );
        $self->{'logLevel'} = $iniValue if $iniValue;
    }
}


sub _configureSSL {
    my $self = shift;

    if( !(-f OBM_CONF && -r OBM_CONF) ) {
        die 'FATAL: can\'t read '.OBM_CONF.' file'."\n";
    }

    my $cfgFile = Config::IniFiles->new( -file => OBM_CONF );
    die 'FATAL: can\'t read '.OBM_CONF.' file'."\n" if !defined($cfgFile);

    my $externalUrl = $cfgFile->val( 'global', 'external-url' );

    $self->{'server'}->{'socketConf'}->{'LocalAddr'} = '';
    $self->{'server'}->{'socketConf'}->{'LocalPort'} = HTTP_PORT;
    $self->{'server'}->{'socketConf'}->{'Timeout'} = SOCKET_TIMEOUT;
    $self->{'server'}->{'socketConf'}->{'Listen'} = 1;
    $self->{'server'}->{'socketConf'}->{'ReuseAddr'} = 'TRUE';
    $self->{'server'}->{'socketConf'}->{'SSL_key_file'} = SSL_CERT_DIR.'/'.$externalUrl.'_signed.pem';
    $self->{'server'}->{'socketConf'}->{'SSL_cert_file'} = SSL_CERT_DIR.'/'.$externalUrl.'_signed.pem';
    $self->{'server'}->{'socketConf'}->{'SSL_ca_file'} = SSL_CERT_DIR.'/obm_cert.pem';
    $self->{'server'}->{'socketConf'}->{'SSL_version'} = SSL_PROTOCOL_VERSION;
}


sub _checkSSL {
    my $self = shift;

    if( ! -r $self->{'server'}->{'socketConf'}->{'SSL_key_file'} ) {
        print STDERR 'FATAL: Unable to load SSL key file '.$self->{'server'}->{'socketConf'}->{'SSL_key_file'}."\n";
        exit 1;
    }

    if( ! -r $self->{'server'}->{'socketConf'}->{'SSL_cert_file'} ) {
        print STDERR 'FATAL: Unable to load SSL key file '.$self->{'server'}->{'socketConf'}->{'SSL_cert_file'}."\n";
        exit 1;
    }

    if( ! -r $self->{'server'}->{'socketConf'}->{'SSL_ca_file'} ) {
        delete($self->{'server'}->{'socketConf'}->{'SSL_ca_file'});
    }

    $self->{'server'}->{'socketConf'}->{'SSL_error_trap'} = sub{
        my( $connection, $msg ) = @_;

        $self->log( 0, $msg ) if defined($msg);
        $self->log( 0, 'Unknow error during HTTPs negociation' ) if !defined($msg);
        use HTTP::Status;
        my $response = HTTP::Response->new();
        $response->content( 'HTTPs negociation fail. HTTPs required. Check your URL' ) if defined($msg);
        $connection->send_response( $response );
    };
}


sub _startServices {
    my $self = shift;
    my( $neededServices ) = @_;

    if( ref($neededServices) ne 'ARRAY' ) {
        return 1;
    }

    for( my $i=0; $i<=$#{$neededServices}; $i++ ) {
        my $service = $neededServices->[$i];
        if( defined($self->{'services'}->{$service}) ) {
            $self->log( 4, 'Service '.$service.' already loaded' );
            next;
        }

        my $serviceInternalName = $service;
        $serviceInternalName =~ s/-/_/g;
        my $servicePath = 'ObmSatellite/Services/'.$serviceInternalName.'.pm';
        my $serviceClass = 'ObmSatellite::Services::'.$serviceInternalName;

        eval {
            require $servicePath;
        } or ($self->log( 0, 'Unknow or invalid service \''.$serviceInternalName.'\'' ) && return 1);

        $self->{'services'}->{$service} = $serviceClass->instance( $self->{'server'}->{'conf_file'} );

        if( !defined($self->{'services'}->{$service}) ) {
            delete($self->{'services'}->{$service});
            $self->log( 0, 'unable to initialize needed '.$service.' service' );
            return 1;
        }else {
            $self->log( 0, 'needed '.$service.' service initialized' );
        }
    }

    return 0;
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
