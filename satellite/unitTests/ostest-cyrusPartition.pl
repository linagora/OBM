#!/usr/bin/perl -w -T

use strict;


use Getopt::Long;
my %parameters;
my $return = GetOptions( \%parameters, 'os-server=s', 'hostname=s' );

use File::Basename;
my $modulePath = dirname($0);

if( $modulePath !~ /^([\/\.-_a-zA-Z0-9]+)$/ ) {
    print STDERR "unable to find needed perl modules !\n";
    exit 10;
}
$modulePath = $1;
require $modulePath.'/lib/common.pm';

sub help {
    print STDERR "Usage: \n";
    print STDERR "\t$0 --os-server <obmSatelliteServer> --hostname <OBM-smtpInRole-HostName>\n";
    exit 1;
}

SWITCH: {
    if( !$parameters{'os-server'} ) {
        help();
    }

    if( !$parameters{'hostname'} ) {
        help();
    }
}

my $clientModulePath = 'lib/client.pm';
require $clientModulePath;

my $client = lib::client->instance();
if( !defined($client) ) {
    print STDERR "[Error] Unable to initialize 'OBM::ObmSatellite::client'\n";
    exit 1;
}

my $errorCode = 0;

my $root = '/cyruspartition/host/<operation>/'.$parameters{'hostname'};
my @operation = ( 'add', 'del' );

for( my $i=0; $i<=$#operation; $i++ ) {
    print $operation[$i]." cyrus partition on ".$parameters{'os-server'}."': ";
    my $path = $root;
    $path =~ s/<operation>/$operation[$i]/;
    if( !$client->post( $parameters{'os-server'}, $path ) ) {
        print '[OK]'."\n";
    }else {
        print '[KO]'."\n";
        $errorCode++;
    }
}

print "All tests done succefully !\n" if !$errorCode;
print STDERR $errorCode." fail !\n" if $errorCode;

exit $errorCode;
