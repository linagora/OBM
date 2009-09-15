#!/usr/bin/perl -t -w

package obmSatellite;

use ObmSatellite::Server;
use strict;


delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

my $obmSatellite = ObmSatellite::Server->new();
exit $obmSatellite->run();

$|=1;
