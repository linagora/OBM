package ObmSatellite::Modules::abstract;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

eval {
    require Config::IniFiles;
} or die 'Config::IniFiles perl module needed !'."\n";
use HTTP::Status;

use constant ENABLED_MODS => '/etc/obm-satellite/mods-enabled';
use constant AVAILABLE_MODS => '/etc/obm-satellite/mods-available';


sub new {
    my $class = shift;
    my $self = bless { }, $class;

    if( !$self->_init() ) {
        return undef;
    }
    
    return $self;
}


sub _init {
    my $self = shift;

    ref($self) =~ /::([^:]+)$/;
    $self->{'name'} = $1;
    $self->{'uri'} = [];
    $self->{'neededServices'} = [];

    if( !$self->_initHook() ) {
        return 0;
    }

    return 1;
}


sub _initHook {
    my $self = shift;

    return 1;
}


sub log {
    my $self = shift;

    require ObmSatellite::Log::log;
    my $logger = ObmSatellite::Log::log->instance();

    return $logger->log( @_ );
}


sub register {
    my $self = shift;

    return $self->{'uri'};
}


# Available services :
# LDAP, SQL
sub neededServices {
    my $self = shift;

    return $self->{'neededServices'};
}


sub getModuleName {
    my $self = shift;

    return $self->{'name'};
}


# Must return undef on error
# Must return an array of [ HTTP::Status, content ] on sucess
sub processHttpRequest {
    my $self = shift;
    my( $requestMethod, $requestUri, $requestBody ) = @_;

    SWITCH: {
        if( uc($requestMethod) eq 'POST' ) {
            return $self->_postMethod( $requestUri, $requestBody );
            last SWITCH;
        }

        if( uc($requestMethod) eq 'GET' ) {
            return $self->_getMethod( $requestUri, $requestBody );
            last SWITCH;
        }

        if( uc($requestMethod) eq 'PUT' ) {
            return $self->_putMethod( $requestUri, $requestBody );
            last SWITCH;
        }
    }

    return $self->_returnStatus( RC_METHOD_NOT_ALLOWED, 'Method '.$requestMethod.' not allowed' );
}


sub _postMethod {
    my $self = shift;
    my( $requestUri, $requestBody ) = @_;

    $self->log( 0, '\'_postMethod\' method not implemented on module '.$self->getModuleName() );
    $self->log( 4, 'Request method : POST' );
    $self->log( 4, 'Request URI : '.$requestUri );
    $self->log( 4, 'Request Body : '.$requestBody );

    return $self->_returnStatus( RC_METHOD_NOT_ALLOWED, 'method \'POST\' not allowed on module '.$self->getModuleName() );;
}


sub _getMethod {
    my $self = shift;
    my( $requestUri, $requestBody ) = @_;

    $self->log( 0, '\'_getMethod\' method not implemented on module '.$self->getModuleName() );
    $self->log( 4, 'Request method : GET' );
    $self->log( 4, 'Request URI : '.$requestUri );
    $self->log( 4, 'Request Body : '.$requestBody );

    return $self->_returnStatus( RC_METHOD_NOT_ALLOWED, 'method \'GET\' not allowed on module '.$self->getModuleName() );
}


sub _putMethod {
    my $self = shift;
    my( $requestUri, $requestBody ) = @_;

    $self->log( 0, '\'_putMethod\' method not implemented on module '.$self->getModuleName() );
    $self->log( 4, 'Request method : PUT' );
    $self->log( 4, 'Request URI : '.$requestUri );
    $self->log( 4, 'Request Body : '.$requestBody );

    return $self->_returnStatus( RC_METHOD_NOT_ALLOWED, 'method \'GET\' not allowed on module '.$self->getModuleName() );
}


sub _splitUrlPath {
    my $self = shift;
    my( $urlPath ) = @_;

    return split( '/', $urlPath );
}


sub _returnStatus {
    my $self = shift;
    my( $httpCode, $status ) = @_;

    my $return = [ $httpCode ];

    if( defined($status) ) {
        $return->[1]->{'status'} = [ $status ];
    }

    return $return;
}


sub _returnContent {
    my $self = shift;
    my( $httpCode, $content ) = @_;

    my $return = [
        $httpCode,
        { 'type' => 'PLAIN' }
    ];

    if( defined($content) ) {
        $return->[1]->{'content'} = $content;
    }

    return $return;
}


sub _loadConfFile {
    my $self = shift;
    my( $options ) = @_;

    if( ref($options) ne 'ARRAY' ) {
        $self->log( 3, 'Options to load must be an array' );
        return undef;
    }

    if( !$self->isEnabled() ) {
        $self->log( 0, 'Module '.$self->getModuleName().' disabled' );
        return undef;
    }

    my $confFile = ENABLED_MODS.'/'.$self->getModuleName();
    if( !( -f $confFile && -r $confFile ) ) {
        $self->log( 0, 'Unable to read configuration file '.$confFile );
        return undef;
    }

    my @confileStat = stat( $confFile );
    if( $confileStat[7] == 0 ) {
        $self->log( 0, 'Empty configuration file '.$confFile );
        return undef;
    }

    my %params;
    if( my $cfgFile = Config::IniFiles->new( -file => $confFile ) ) {
        for( my $i=0; $i<=$#{$options}; $i++ ) {
            my $iniValue = $cfgFile->val( $self->getModuleName(), $options->[$i] );
            $self->log( 4, 'INI file for module '.$self->getModuleName().': '.$options->[$i].'->'.$iniValue ) if $iniValue;
            $params{$options->[$i]} = $iniValue if defined($iniValue);
        }
    }else {
        $self->log( 0, 'Failed to load configuration file '.$confFile );
        return undef;
    }

    return \%params;
}


sub isEnabled {
    my $self = shift;

    my $confFile = ENABLED_MODS.'/'.$self->getModuleName();
    if( -f $confFile ) {
        return 1;
    }

    return 0;
}


sub _getLdapConn {
    my $self = shift;

    eval {
        require ObmSatellite::Services::LDAP;
    } or ($self->log( 0, 'Unable to load LDAP service' ) && return undef);
    my $ldapServer = ObmSatellite::Services::LDAP->instance();

    if( !defined($ldapServer) ) {
        $self->log( 0, 'Unable to load LDAP service' );
        return undef;
    }

    return $ldapServer->getConn();
}


sub _getLdapRootFromLdapService {
    my $self = shift;

    eval {
        require ObmSatellite::Services::LDAP;
    } or ($self->log( 0, 'Unable to load LDAP service' ) && return undef);
    my $ldapServer = ObmSatellite::Services::LDAP->instance();

    if( !defined($ldapServer) ) {
        $self->log( 0, 'Unable to load LDAP service' );
        return undef;
    }

    return $ldapServer->getLdapRoot();
}


sub _getLdapRoot {
    my $self = shift;

    $self->{'ldapRoot'} = $self->_getLdapRootFromLdapService() if !$self->{'ldapRoot'};
    $self->log( 0, 'No ldapRoot defined. Check that your LDAP server configuration define a default LDAP root...' ) if !$self->{'ldapRoot'};

    return $self->{'ldapRoot'};
}


sub _getHostDomains {
    my $self = shift;
    my( $role, $hostname ) = @_;

    if( !defined($role) ) {
        $self->log( 0, 'Undefined host role' );
        return undef;
    }

    if( !defined($hostname) ) {
        $self->log( 0, 'Undefined host name' );
        return undef;
    }


    my $ldapSearchFilter = '(&(objectclass=obmMailServer)('.$role.'='.$hostname.'))';
    my $ldapEntries = $self->_getLdapValues( $ldapSearchFilter, [ 'obmDomain' ] );

    return undef if !defined($ldapEntries);

    my @domainList;
    foreach my $entry (@{$ldapEntries}) {
        my $entryDomainList = $entry->get_value( 'obmDomain', asref => 1 );
        for( my $i=0; $i<=$#{$entryDomainList}; $i++ ) {
            $self->log( 4, 'Host '.$hostname.' linked to domain '.$entryDomainList->[$i] );
            push( @domainList, $entryDomainList->[$i] );
        }
    }

    if( $#domainList < 0 ) {
        return undef;
    }

    return \@domainList;
}


sub _getLdapValues {
    my $self = shift;
    my( $ldapFilter, $ldapAttributes ) = @_;

    my $ldapConn = $self->_getLdapConn() or return undef;

    if( ref($ldapAttributes) ne 'ARRAY' ) {
        $self->log( 4, 'LDAP attributes must be an ARRAY ref' );
        return undef;
    }

    my $ldapRoot = $self->_getLdapRoot();
    $self->log( 4, 'Search LDAP root \''.$ldapRoot.'\', filter '.$ldapFilter ) if $ldapRoot;
    $self->log( 4, 'Search default LDAP server root, filter '.$ldapFilter ) if !$ldapRoot;

    my $ldapResult;
    if( $ldapRoot ) {
        $ldapResult = $ldapConn->search(
                            base => $ldapRoot,
                            scope => 'sub',
                            filter => $ldapFilter,
                            attrs => $ldapAttributes
                        );
    }else {
        $ldapResult = $ldapConn->search(
                            scope => 'sub',
                            filter => $ldapFilter,
                            attrs => $ldapAttributes
                        );
    }

    if( $ldapResult->is_error() ) {
        $self->log( 0, 'LDAP search fail on error : '.$ldapResult->error() );
        return undef;
    }

    my @results = $ldapResult->entries();
    return \@results;
}
