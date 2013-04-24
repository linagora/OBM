package ObmSatellite::Modules::abstract;

$VERSION = '1.0';

$debug = 1;

use ObmSatellite::Log::log;
@ISA = qw(ObmSatellite::Log::log);

use 5.006_001;
use strict;

eval {
    require Config::IniFiles;
} or die 'Config::IniFiles perl module needed !'."\n";

use HTTP::Status;
use ObmSatellite::Misc::regex;

use constant ENABLED_MODS => '/etc/obm-satellite/mods-enabled';
use constant AVAILABLE_MODS => '/etc/obm-satellite/mods-available';
use constant DEFAULT_OBM_LANG => 'en';


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
    $self->{'neededServices'} = [];

    $self->{'uri'} = $self->_setUri();    
    if( ref($self->{'uri'}) ne 'ARRAY' ) {
        return 0;
    }

    if( !$self->_initHook() ) {
        return 0;
    }

    return 1;
}


sub _setUri {
    my $self = shift;

    $self->_log( 'Method \'setUri\' must be defined by module '.$self->getModuleName(), 0 );
    return undef;
}


sub _initHook {
    my $self = shift;

    return 1;
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

    $self->_log( 'Request method : '.$requestMethod, 4 );
    $self->_log( 'Request URI : '.$requestUri, 4 );
    $self->_log( 'Request content : '.$requestBody, 4 );

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

        if( uc($requestMethod) eq 'DELETE' ) {
            return $self->_deleteMethod( $requestUri, $requestBody );
            last SWITCH;
        }
    }

    return $self->_returnStatus( RC_METHOD_NOT_ALLOWED, 'Method '.$requestMethod.' not allowed' );
}

sub _methodNotAllowed {
    my ($self, $method, $requestUri, $requestBody) = @_;

    $self->_log( "No handler for the HTTP method $method implemented in module ".$self->getModuleName(), 0 );
    $self->_log( "Request method : $method", 5 );
    $self->_log( "Request URI : $requestUri", 5 );
    $self->_log( "Request Body : $requestBody", 5 );

    return $self->_returnStatus( RC_METHOD_NOT_ALLOWED, "method '$method' not allowed in module ".$self->getModuleName() );;
}

sub _postMethod {
    my ($self, $requestUri, $requestBody ) = @_;

    return $self->_methodNotAllowed('POST', $requestUri, $requestBody);
}


sub _getMethod {
    my ($self, $requestUri, $requestBody ) = @_;

    return $self->_methodNotAllowed('GET', $requestUri, $requestBody);
}


sub _putMethod {
    my ($self, $requestUri, $requestBody ) = @_;

    return $self->_methodNotAllowed('PUT', $requestUri, $requestBody);
}

sub _deleteMethod {
    my ($self, $requestUri, $requestBody ) = @_;

    return $self->_methodNotAllowed('DELETE', $requestUri, $requestBody);
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


sub _response {
    my $self = shift;
    my( $httpCode, $content ) = @_;

    use ObmSatellite::Server::response;
    my $response = ObmSatellite::Server::response->new( $self->getModuleName() );
    $response->setStatus( $httpCode );
    if( defined($content) ) {
        if( ref( $content ) ) {
            $response->setExtraContent( $content );
        }else {
            $response->setStatusMessage( $content );
        }
    }

    return $response;
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
        $self->_log( 'Options to load must be an array', 4 );
        return undef;
    }

    if( !$self->isEnabled() ) {
        $self->_log( 'Module '.$self->getModuleName().' disabled', 3 );
        return undef;
    }

    my $confFile = ENABLED_MODS.'/'.$self->getModuleName();
    if( !( -f $confFile && -r $confFile ) ) {
        $self->_log( 'Unable to read configuration file '.$confFile, 1 );
        return undef;
    }

    my @confileStat = stat( $confFile );
    if( $confileStat[7] == 0 ) {
        $self->_log( 'Empty configuration file '.$confFile, 3 );
        return undef;
    }

    my %params;
    if( my $cfgFile = Config::IniFiles->new( -file => $confFile ) ) {
        for( my $i=0; $i<=$#{$options}; $i++ ) {
            my $iniValue = $cfgFile->val( $self->getModuleName(), $options->[$i] );
            $self->_log( 'INI file for module '.$self->getModuleName().': '.$options->[$i].'->'.$iniValue, 4 ) if $iniValue;
            $params{$options->[$i]} = $iniValue if defined($iniValue);
        }
    }else {
        $self->_log( 'Failed to load configuration file '.$confFile, 1 );
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
    } or ($self->_log( 'Unable to load LDAP service', 0 ) && return undef);
    my $ldapServer = ObmSatellite::Services::LDAP->instance();

    if( !defined($ldapServer) ) {
        $self->_log( 'Unable to load LDAP service', 1 );
        return undef;
    }

    return $ldapServer->getConn();
}


sub _getLdapRootFromLdapService {
    my $self = shift;

    eval {
        require ObmSatellite::Services::LDAP;
    } or ($self->_log( 'Unable to load LDAP service', 0 ) && return undef);
    my $ldapServer = ObmSatellite::Services::LDAP->instance();

    if( !defined($ldapServer) ) {
        $self->_log( 'Unable to load LDAP service', 1 );
        return undef;
    }

    return $ldapServer->getLdapRoot();
}


sub _getLdapRoot {
    my $self = shift;

    $self->{'ldapRoot'} = $self->_getLdapRootFromLdapService() if !$self->{'ldapRoot'};
    $self->_log( 'No ldapRoot defined. Check that your LDAP server configuration define a default LDAP root...', 2 ) if !$self->{'ldapRoot'};

    return $self->{'ldapRoot'};
}


sub _getHostDomains {
    my $self = shift;
    my( $role, $hostname ) = @_;

    if( !defined($role) ) {
        $self->_log( 'Undefined host role', 4 );
        return undef;
    }

    if( !defined($hostname) ) {
        $self->_log( 'Undefined host name', 4 );
        return undef;
    }


    my $ldapSearchFilter = '(&(objectclass=obmMailServer)('.$role.'='.$hostname.'))';
    my $ldapEntries = $self->_getLdapValues( $ldapSearchFilter, [ 'obmDomain' ] );

    return undef if !defined($ldapEntries);

    my @domainList;
    foreach my $entry (@{$ldapEntries}) {
        my $entryDomainList = $entry->get_value( 'obmDomain', asref => 1 );
        for( my $i=0; $i<=$#{$entryDomainList}; $i++ ) {
            $self->_log( 'Host '.$hostname.' linked to domain '.$entryDomainList->[$i], 5 );
            push( @domainList, $entryDomainList->[$i] );
        }
    }

    return \@domainList;
}


sub _getLdapValues {
    my $self = shift;
    my( $ldapFilter, $ldapAttributes ) = @_;

    my $ldapConn = $self->_getLdapConn() or return undef;

    if( ref($ldapAttributes) ne 'ARRAY' ) {
        $self->_log( 'LDAP attributes must be an ARRAY ref', 4 );
        return undef;
    }

    my $ldapRoot = $self->_getLdapRoot();
    $self->_log( 'Search LDAP root \''.$ldapRoot.'\', filter '.$ldapFilter, 5 ) if $ldapRoot;
    $self->_log( 'Search default LDAP server root, filter '.$ldapFilter, 5 ) if !$ldapRoot;

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
        $self->_log( 'LDAP search fail on error : '.$ldapResult->error(), 1 );
        return undef;
    }

    my @results = $ldapResult->entries();
    return \@results;
}


sub _xmlContent {
    my $self = shift;
    my $requestBody = shift;
    my @xmlInOpts = @_;

    local $SIG{__DIE__} = sub {
        $self->_log( join( ' ', @_ ), 0 );
    };

    if( !$requestBody ) {
        return {};
    }

    use XML::Simple;
    my $xmlContent = undef;
    eval {
        $xmlContent = XMLin( $requestBody, @xmlInOpts );
    };

    if( !$xmlContent ) {
        return undef;
    }

    use Data::Dumper;
    $self->_log( Dumper($xmlContent), 5 );

    return $xmlContent;
}


sub _getCyrusConn {
    my $self = shift;

    eval {
        require ObmSatellite::Services::CYRUS;
    } or ($self->_log( 'Unable to load CYRUS service', 0 ) && return undef);
    my $cyrusServer = ObmSatellite::Services::CYRUS->instance();

    if( !defined($cyrusServer) ) {
        $self->_log( 'Unable to load CYRUS service', 1 );
        return undef;
    }

    return $cyrusServer->getConn();
}


sub _getDefaultObmLang {
    my $self = shift;
    my( $realm ) = @_;

    if( !defined($realm) || ($realm !~ /$REGEX_REALM/) ) {
        $self->_log( 'Using default language \''.DEFAULT_OBM_LANG.'\'', 4 );
        return DEFAULT_OBM_LANG;
    }

    my $ldapDefaultObmLang = $self->_getLdapValues(
            '(&(cn=obmSettings)(obmDomain='.$realm.'))',
            [ 'obmLang' ]
            );

    if( !defined($ldapDefaultObmLang) ) {
        $self->_log( 'Error on default language LDAP search, using default \''.DEFAULT_OBM_LANG.'\'', 2 );
        return DEFAULT_OBM_LANG;
    }

    if( $#{$ldapDefaultObmLang} != 0 ) {
        $self->_log( 'Using default language \''.DEFAULT_OBM_LANG.'\'', 4 );
        return DEFAULT_OBM_LANG;
    }

    $self->_log( 'Using language \''.$ldapDefaultObmLang->[0]->get_value('obmLang').'\'', 4 );
    return $ldapDefaultObmLang->[0]->get_value('obmLang');
}
