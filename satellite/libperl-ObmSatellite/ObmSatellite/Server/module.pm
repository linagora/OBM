package ObmSatellite::Server::module;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use File::Basename;
use ObmSatellite::Server::response;
use HTTP::Status;
use HTTP::Response;


sub _loadModules {
    my $self = shift;

    my @modules;
    my @modulesEnabled = </etc/obm-satellite/mods-enabled/*>;
    for( my $i=0; $i<=$#modulesEnabled; $i++ ) {
        if( ! -l $modulesEnabled[$i] ) {
            $self->_log( 'Ignoring module \''.$modulesEnabled[$i].'\'. Must be symlink to ../mods-available files', 2 );
            next;
        }

        my $enMod = basename( $modulesEnabled[$i] );
        if( $enMod !~ /^(\w+)$/ ) {
            next;
        }

        push( @modules, $1 );
    }

    for( my $i=0; $i<=$#modules; $i++ ) {
        my $moduleInternalName = $modules[$i];
        $moduleInternalName =~ s/-/_/g;
        my $modulePath = 'ObmSatellite/Modules/'.$moduleInternalName.'.pm';
        my $moduleClass = 'ObmSatellite::Modules::'.$moduleInternalName;

        # Disable SIGDIE handler to load only valid modules without fatal error
        local $SIG{__DIE__} = sub {
            $self->_log( 'Unknow or invalid module \''.$modules[$i].'\' '.join( ' ', @_ ), 1 );
        };

        eval {
            require $modulePath;
        };
        if ($@) {
            $self->_log("Couldn't load module ".$modules[$i].": $@", 1);
            next;
        }

        my $module = $moduleClass->new();
        if( !defined($module) ) {
            $self->_log( 'loading module \''.$modules[$i].'\' failed', 1 );
            next;
        }

        my $neededServices = $module->neededServices();
        if( $self->_startServices( $module->neededServices() ) ) {
            $self->_log( 'starting needed service failed. Loading module \''.$modules[$i].'\' failed', 1 );
            next;
        }

        my $urls = $module->register();
        for( my $i=0; $i<=$#{$urls}; $i++ ) {
            $self->_log( 'Register module '.$module->getModuleName().' for URL '.$urls->[$i], 4 );
            push( @{$self->{'modules'}->{$urls->[$i]}}, $module );
        }

        $self->_log( 'loading module \''.$modules[$i].'\' success', 3 );
    }

    my @loadedModules = keys(%{$self->{'modules'}});
    if( $#loadedModules < 0 ) {
        $self->_log( 'No module loaded !', 0 );
        return 1;
    }

    return 0;
}


sub processHttpRequest {
    my $self = shift;
    my( $request, $httpClient ) = @_;
    my $modules = $self->{'modules'};

    if( ref($modules) ne 'HASH' ) {
        $self->_log( 'No module loaded', 0 );
        return 0;
    }

    my @urls = keys( %{$modules} );
    my $requestUri = $request->uri->path();
    my $i = 0;
    my $uriModules = undef;
    while( ($i<=$#urls) && !defined($uriModules) ) {
        if( $requestUri =~ /^$urls[$i](\/){0,1}/ ) {
            $uriModules = $urls[$i];
        }

        $i++;
    }

    if( defined($uriModules) ) {
        my $response = $self->processUriModule( $uriModules, $request, $httpClient );

        if( !defined( $response ) ) {
            $response = ObmSatellite::Server::response->new();
            $response->setStatus( RC_INTERNAL_SERVER_ERROR );
            $response->setStatusMessage( RC_INTERNAL_SERVER_ERROR.' Internal Server Error' );
        }

        $httpClient->send_response( $response->getHttpResponse() );

    }else {
        my $response = ObmSatellite::Server::response->new();
        $response->setStatus( RC_NOT_FOUND );
        $response->setStatusMessage( RC_NOT_FOUND.' URL does not exist' );
        $httpClient->send_response( $response->getHttpResponse() );
    }


    return 0;
}


sub processUriModule {
    my $self = shift;
    my( $uriModule, $request, $httpClient ) = @_;

    my $modules = $self->{'modules'}->{$uriModule};
    my $response = undef;
    my $i = 0;
    while( ($i <= $#{$modules}) && (ref($response) ne 'ARRAY') ) {
        $self->_log( 'Sending request \''.$request->uri->path_query().'\' to module \''.$modules->[$i]->getModuleName().'\'', 3 );
        $response = $modules->[$i]->processHttpRequest( $request->method(), $request->uri->path_query(), $request->content() );

        $i++;
    }

    if( ref($response) ne 'ObmSatellite::Server::response' ) {
        return undef;
    }

    return $response;
}
