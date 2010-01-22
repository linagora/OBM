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

use OBM::ObmSatellite::client;
my $client = OBM::ObmSatellite::client->instance();
if( !defined($client) ) {
    print STDERR "[Error] Unable to initialize 'OBM::ObmSatellite::client'\n";
    exit 1;
}

print "Generating all postfix maps on '".$parameters{'os-server'}."': ";
my $path = '/postfixsmtpinmaps/host/'.$parameters{'hostname'};
if( !$client->post( $parameters{'os-server'}, $path ) ) {
    print '[OK]'."\n";
}else {
    print '[KO]'."\n";
}


#package ostestPostfixSmtpInMaps;
#
#$VERSION = '1.0';
#$debug = 1;
#
#use OBM::ObmSatellite::client;
#@ISA = qw( OBM::ObmSatellite::client );
#
#use 5.006_001;
#require Exporter;
#use strict;
