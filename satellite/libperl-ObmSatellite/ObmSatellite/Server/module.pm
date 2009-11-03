package ObmSatellite::Server::module;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use File::Basename;
use HTTP::Status;
use HTTP::Response;
use XML::Simple;


sub _loadModules {
    my $self = shift;

    my @modules;
    my @modulesEnabled = </etc/obm-satellite/mods-enabled/*>;
    for( my $i=0; $i<=$#modulesEnabled; $i++ ) {
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
        local $SIG{__DIE__} = undef;

        eval {
            require $modulePath;
        } or ($self->log( 0, 'Unknow or invalid module \''.$modules[$i].'\'' ) && next);

        my $module = $moduleClass->new();
        if( !defined($module) ) {
            $self->log( 0, 'loading module \''.$modules[$i].'\' failed' );
            next;
        }

        my $neededServices = $module->neededServices();
        if( $self->_startServices( $module->neededServices() ) ) {
            $self->log( 0, 'starting needed service failed. Loading module \''.$modules[$i].'\' failed' );
            next;
        }

        my $urls = $module->register();
        for( my $i=0; $i<=$#{$urls}; $i++ ) {
            $self->log( 4, 'Register module '.$module->getModuleName().' for URL '.$urls->[$i] );
            push( @{$self->{'modules'}->{$urls->[$i]}}, $module );
        }

        $self->log( 0, 'loading module \''.$modules[$i].'\' success' );
    }

    my @loadedModules = keys(%{$self->{'modules'}});
    if( $#loadedModules < 0 ) {
        $self->log( 0, 'No module loaded !' );
        return 1;
    }

    return 0;
}


sub processHttpRequest {
    my $self = shift;
    my( $request, $httpClient ) = @_;
    my $modules = $self->{'modules'};

    if( ref($modules) ne 'HASH' ) {
        $self->log( 0, 'No module loaded' );
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
        my( $status, $responseContent ) = $self->processUriModule( $uriModules, $request, $httpClient );

        if( !defined( $status ) ) {
            $status = RC_INTERNAL_SERVER_ERROR;
            $responseContent = {
                'module' => 'obmSatellite',
                'status' => '500 Internal Server Error'
                };
        }

        if( ref($responseContent) ne 'HASH' ) {
            $responseContent = undef;
        }elsif( defined($responseContent->{'type'}) && $responseContent->{'type'} eq 'PLAIN' ) {
            $responseContent = $responseContent->{'content'};
        }else {
            $responseContent = XMLout( $responseContent, rootname => 'obmSatellite' );
        }

        if( defined($responseContent) ) {
            $self->log( 3, 'Sending response : '.$status.' - '.$responseContent );
        }else {
            $self->log( 3, 'Sending response : '.$status );
        }
        my $response = HTTP::Response->new( $status );
        $response->content( $responseContent ) if defined($responseContent);
        $httpClient->send_response( $response );

    }else {
        my $response = HTTP::Response->new( RC_NOT_FOUND );
        $response->content( XMLout( {
                'module' => 'obmSatellite',
                'status' => [ RC_NOT_FOUND.' URL does not exist' ]
                } ), rootname => 'obmSatellite' );

        $self->log( 3, 'Sending response : '.RC_NOT_FOUND.' - '.$response->content() );
        $httpClient->send_response( $response );
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
        $self->log( -1, 'Sending request \''.$request->uri->path().'\' to module \''.$modules->[$i]->getModuleName().'\'' );
        $response = $modules->[$i]->processHttpRequest( $request->method(), $request->uri->path(), $request->content() );

        if( ref($response) eq 'ARRAY' ) {
            $response->[1]->{'module'} = $modules->[$i]->getModuleName();
        }

        $i++;
    }

    if( ref($response) ne 'ARRAY' ) {
        return undef;
    }

    return @{$response};
}
