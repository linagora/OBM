package ObmSaslauthd::Server::processRequest;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

require ObmSaslauthd::Server::request;

use constant SASL_SUCC_RESP  => pack("nA3", 2, "OK\000");
use constant SASL_FAIL_RESP  => pack("nA3", 2, "NO\000");

sub process_request {
    my $self = shift;

    $self->{'request'} = ObmSaslauthd::Server::request->new( $self );

    for( my $i=0; $i<=$#{$self->{'authenticationModules'}}; $i++ ) {
        if( $self->{'authenticationModules'}->[$i]->checkAuth( $self->{'request'} ) ) {
            print SASL_SUCC_RESP;
            return;
        }
    }

    print SASL_FAIL_RESP;
}
