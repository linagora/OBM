#!/usr/bin/perl -w -T

use strict;


use Getopt::Long;
my %parameters;
my $return = GetOptions( \%parameters, 'os-server=s', 'hostname=s' );


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

use File::Basename;
my $modulePath = dirname($0);

if( $modulePath !~ /^([\/\.-_a-zA-Z0-9]+)$/ ) {
    print STDERR "unable to find needed perl modules !\n";
    exit 10;
}
$modulePath = $1;

my $clientModulePath = $modulePath.'/lib/client.pm';
require $clientModulePath;

my $client = lib::client->instance();
if( !defined($client) ) {
    print STDERR "[Error] Unable to initialize 'OBM::ObmSatellite::client'\n";
    exit 1;
}

my $errorCode = 0;

my $root = '/postfixsmtpinmaps/host/'.$parameters{'hostname'};
my @maps = ( 'alias', 'mailbox', 'transport', 'domain' );

for( my $i=0; $i<=$#maps; $i++ ) {
    print "Generating ".$maps[$i]." postfix maps on '".$parameters{'os-server'}."': ";
    my $path = $root.'/'.$maps[$i];
    if( !$client->post( $parameters{'os-server'}, $path ) ) {
        print '[OK]'."\n";
    }else {
        print '[KO]'."\n";
        $errorCode++;
    }
}

print "Generating all postfix maps on '".$parameters{'os-server'}."': ";
my $path = $root;
if( !$client->post( $parameters{'os-server'}, $path ) ) {
    print '[OK]'."\n";
}else {
    print '[KO]'."\n";
    $errorCode++;
}


print "All tests done succefully !\n" if !$errorCode;
print STDERR $errorCode." fail !\n" if $errorCode;

exit $errorCode;
