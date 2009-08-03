package ObmSaslauthd::AuthMods::abstract;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub new {
    my $class = shift;
    my( $daemon ) = @_;

    my $self = bless { }, $class;
    $self->{'daemon'} = $daemon;
    if( !ref($self->{'daemon'}) ) {
        return undef;
    }

    return $self;
}


sub init {
}


sub checkAuth {
}
