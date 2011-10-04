package ObmSaslauthd::Server::bind;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub post_bind_hook {
    my $self = shift;

    $self->{'server'}->{'port'}->[0] =~ /^([\w\.\-\*\/]+)\|(\w+)$/;
    foreach my $sock ( @{ $self->{'server'}->{'sock'} } ) {
        chmod( 0777, $sock->NS_unix_path() );
    }
}
