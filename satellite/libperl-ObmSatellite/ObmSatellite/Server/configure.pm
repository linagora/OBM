package ObmSatellite::Server::configure;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use IO::Socket qw(AF_INET);

eval {
    require Config::IniFiles;
} or die 'Config::IniFiles perl module needed !'."\n";

use constant HTTP_PORT => 30000;
use constant MAX_PROCESS => 2;
use constant MAX_REQUEST_PER_PROCESS => 10;
use constant CONF_DIR => '/etc/obm-satellite';
use constant LOG_DIR => '/var/log/obm-satellite';
use constant SOCKET_TIMEOUT => 30;
use constant SSL_PROTOCOL_VERSION => 'SSLv2/3';
use constant SSL_OBM_CERT => '/etc/obm/certs/obm_cert.pem';
use constant SSL_CA_CERT => '/var/lib/obm-ca/cacert.pem';
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

    $self->{'server'}->{'max_spare_servers'} = MAX_PROCESS;     # max process
    $self->{'server'}->{'max_requests'} = MAX_REQUEST_PER_PROCESS;    # max queries per process

    $self->_configureSSL();

    # PID file
    $self->{'server'}->{'pid_file'} = PID_DIR.'/'.$self->{'server'}->{'name'}.'.pid';

    # Log
    $self->{'logFile'} = LOG_DIR.'/'.$self->{'server'}->{'name'}.'.log';
    $self->{'logLevel'} = $ObmSatellite::Log::log::INFO;

    $self->_loadConfFile();

    # Initialize log
    $self->_configureLog();

    # Check SSL parameters
    $self->_checkSSL();

    # HTTP authentication
    $self->{'httpAuthentication'}->{'validUser'} = HTTP_AUTHENTICATION_VALID_USER;
    $self->{'httpAuthentication'}->{'ldapFilter'} = HTTP_AUTHENTICATION_LDAP_FILTER;
}


sub _configurePreBind {
    my $self = shift;

    if( $self->_startServices( [ 'LDAP' ] ) ) {
        $self->_log( 'Fail to load LDAP service, needed to HTTP basic authentication', 0 );
        exit 1;
    }

    # Load modules
    if( $self->_loadModules() ) {
        $self->_log( 'Modules initialization fail', 0 );
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
        $self->{'logLevel'} = $iniValue if defined($iniValue);
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
    $self->{'server'}->{'socketConf'}->{'Domain'} = AF_INET;
    $self->{'server'}->{'socketConf'}->{'ReuseAddr'} = 'TRUE';
    $self->{'server'}->{'socketConf'}->{'SSL_key_file'} = SSL_OBM_CERT;
    $self->{'server'}->{'socketConf'}->{'SSL_cert_file'} = SSL_OBM_CERT;
    $self->{'server'}->{'socketConf'}->{'SSL_ca_file'} = SSL_CA_CERT;
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

        $self->_log( $msg, 1 ) if defined($msg);
        $self->_log( 'Unknow error during HTTPs negociation', 1 ) if !defined($msg);
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
            $self->_log( 'Service '.$service.' already loaded', 4 );
            next;
        }

        my $serviceInternalName = $service;
        $serviceInternalName =~ s/-/_/g;
        my $servicePath = 'ObmSatellite/Services/'.$serviceInternalName.'.pm';
        my $serviceClass = 'ObmSatellite::Services::'.$serviceInternalName;

        eval {
            require $servicePath;
        } or ($self->_log( 'Unknow or invalid service \''.$serviceInternalName.'\'', 1 ) && return 1);

        $self->{'services'}->{$service} = $serviceClass->instance( $self->{'server'}->{'conf_file'} );

        if( !defined($self->{'services'}->{$service}) ) {
            delete($self->{'services'}->{$service});
            $self->_log( 'unable to initialize needed '.$service.' service', 1 );
            return 1;
        }else {
            $self->_log( 'needed '.$service.' service initialized', 3 );
        }
    }

    return 0;
}
