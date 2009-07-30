package ObmSaslauthd::Tools::commonMethods;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub dump {
    my $self = shift;
    my @desc;

    push( @desc, $self );

    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}
