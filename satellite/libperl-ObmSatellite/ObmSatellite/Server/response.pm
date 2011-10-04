package ObmSatellite::Server::response;

$VERSION = '1.0';

$debug = 1;

use ObmSatellite::Log::log;
@ISA = qw(ObmSatellite::Log::log HTTP::Status);

use 5.006_001;
require Exporter;
use strict;

use HTTP::Status;

use constant ROOTNAME => 'obmSatellite';
# Response type
use constant XML => 'text/xml';

sub new {
    my $class = shift;
    my( $module ) = @_;

    my $self = bless { }, $class;

    $self->setHttpHeader( 'Content-Type', XML );
    $self->setStatus( RC_INTERNAL_SERVER_ERROR );
    if( !defined($module) ) {
        $self->setModule( 'obmSatellite' );
    }elsif( ref($module) ) {
        $self->setModule( ref($module) );
    }else {
        $self->setModule( $module );
    }

    return $self;
}


sub setHttpHeader {
    my $self = shift;
    my( $httpHeader, $value ) = @_;

    if( !defined($httpHeader) || $httpHeader !~ /^[a-z_-]+$/i ) {
        $self->_log( 'Invalid HTTP header name !', 2 );
        return 0;
    }

    if( ref($value) && ref($value) ne 'ARRAY' ) {
        $self->_log( 'Invalid value for HTTP header \''.$httpHeader.'\'', 2 );
        return 0;
    }

    SWITCH: {
        if( !ref( $value ) ) {
            push( @{$self->{'httpHeaders'}->{$httpHeader}}, $value );
            last SWITCH;
        }

        if( ref($value) eq 'ARRAY' ) {
            push( @{$self->{'httpHeaders'}->{$httpHeader}}, @{$value} );
            last SWITCH;
        }

        $self->_log( 'Invalid value for HTTP header \''.$httpHeader.'\'', 2 );
        return 0;
    }

    return 1;
}


sub unsetHttpHeader {
    my $self = shift;
    my( $httpHeader ) = @_;

    if( !defined($httpHeader) || $httpHeader !~ /^[a-z_-]+$/i ) {
        $self->_log( 'Invalid HTTP header name !', 2 );
        return 0;
    }

    delete( $self->{'httpHeaders'}->{$httpHeader} );
    return 1;
}


sub getHttpHeader {
    my $self = shift;
    my( $httpHeader ) = @_;

    return $self->{'httpHeaders'}->{$httpHeader};
}


sub asString {
    my $self = shift;

    my $string = "statusCode => ".$self->{'statusCode'}."\n";
    
    $string .= "HTTP headers => {\n";
    while( my( $key, $value ) = each( %{$self->{'httpHeaders'}} ) ) {
        $string .= $key." => ".join(',', @{$value})."\n";
    }
    $string .= "}\n";
    $string .= "rootName => \'".ROOTNAME."\'\n";
    $string .= "content {\n";
    $string .= $self->_contentToXML();
    $string .= "}\n";

    $self->_log( $string, 5 );

    return $string;
}


sub setStatus {
    my $self = shift;
    my( $httpStatus ) = @_;

    if( !defined($httpStatus) || $httpStatus !~ /^[0-9]+$/ ) {
        $self->_log( 'Invalid HTTP status code', 1 );
        return 0;
    }

    $self->{'statusCode'} = $httpStatus;
    $self->setStatusMessage( status_message( $self->{'statusCode'} ) );

    if( !$self->{'content'}->{'status'} ) {
        $self->{'content'}->{'status'} = 'Unknow HTTP status code '.$self->{'statusCode'};
    }
    return 1;
}


sub setStatusMessage {
    my $self = shift;
    my( $status ) = @_;

    if( !defined($status) ) {
        $self->_log( 'Invalid response content', 1 );
        return 0;
    }

    $self->{'content'}->{'status'} = $status;
    return 1;
}


sub setExtraContent {
    my $self = shift;
    my( $extraContent ) = @_;

    if( ref($extraContent) ne 'HASH' ) {
        $self->_log( 'Invalid extra content. Must be an array reference', 1 );
        return 1;
    }

    while( my( $key, $value) = each( %{$extraContent} ) ) {
        if( $key =~ /^(status|module)$/i ) {
            next;
        }

        if( ref($value) && ref($value) !~ /^(HASH|ARRAY)$/ ) {
            next;
        }

        $self->{'content'}->{$key} = $value;
    }
}


sub getContentValue {
    my $self = shift;
    my($content) = @_;

    if( ref($content) || !$content ) {
        return undef;
    }

    return $self->{'content'}->{$content};
}


sub setModule {
    my $self = shift;
    my( $module ) = @_;

    if( !defined($module) || ref($module) || $module !~ /^[a-z0-9-]+$/i ) {
        $self->_log( 'Invalid module name', 1 );
        return 0;
    }

    $self->{'content'}->{'module'} = $module;
    return 1;
}


sub getHttpResponse {
    my $self = shift;

    use HTTP::Response;
    my $response = HTTP::Response->new( $self->{'statusCode'} );
    if( !defined($response) ) {
        $self->_log( 'Can\'t generate response !', 0 );
        return undef;
    }

    while( my( $httpHeader, $value ) = each(%{$self->{'httpHeaders'}}) ) {
        $response->header( $httpHeader => $value );
    }

    $response->content( $self->_contentToXML() );

    if( is_error($self->{'statusCode'}) ) {
        $self->_log( 'Sending response : '.$self->{'statusCode'}.' - '.$response->content(), 1 );
    }else {
        $self->_log( 'Sending response : '.$self->{'statusCode'}.' - '.$response->content(), 4 );
    }
    
    return $response;
}


sub _contentToXML {
    my $self = shift;
    
    use XML::Simple;
    return XMLout( $self->{'content'},
            rootName => ROOTNAME,
            XMLDecl => "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>" )
}


sub isError {
    my $self = shift;

    if( !defined($self->{'statusCode'}) ) {
        return undef;
    }

    return is_error($self->{'statusCode'});
}
