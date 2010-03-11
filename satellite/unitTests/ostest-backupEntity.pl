#!/usr/bin/perl -w -T

use strict;


use Getopt::Long;
my %parameters;
my $return = GetOptions( \%parameters, 'os-server=s', 'entity-type=s', 'entity-login=s' );

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
    print STDERR "\t$0 --os-server <obmSatelliteServer> --entity-type <OBM-entity-type> --entity-login <OBM-entity-login>\n";
    exit 1;
}

sub genInvalidContent {
    return 'invalid content, not XML !'
}

sub genContent {
    my $xml->{'calendar'}->[0] = 'BEGIN:VCALENDAR
PRODID:-//Aliasource Groupe LINAGORA//OBM Calendar 2.4.0-rc//FR
CALSCALE:GREGORIAN
X-OBM-TIME:1268220521
VERSION:2.0
METHOD:PUBLISH
BEGIN:VEVENT
CREATED:20090318T230300Z
LAST-MODIFIED:20090403T091039Z
DTSTART;TZID=Europe/Paris:20090310T113000
DURATION:PT1H
TRANSP:OPAQUE
SUMMARY:Test
DESCRIPTION:
CLASS:PUBLIC
PRIORITY:5
ORGANIZER;X-OBM-ID=6;CN=Test 01 User:MAILTO:test01@aliasource.fr
LOCATION:Là-bas
CATEGORIES:Appel tel.
X-OBM-COLOR:
UID:
ATTENDEE;CUTYPE=INDIVIDUAL;CN=James Chief;PARTSTAT=ACCEPTED;X-OBM-ID=4:MAI
 LTO:james.chief@aliasource.fr
ATTENDEE;CUTYPE=INDIVIDUAL;CN=Test 01 User;PARTSTAT=ACCEPTED;X-OBM-ID=6:MA
 ILTO:test01@aliasource.fr
DTSTAMP:20100310T112841Z
END:VEVENT
END:VCALENDAR';

    $xml->{'privateContact'}->[0] = 'Display name;Display name;Company;Address;Mobile phone;Work phone;E-Mail
"Bronski ";"";"    ";"";"";"bronski@gmail.fr"
"Contact 00";"";"16, rue des prés 31000  Toulouse ";"";"";"pouet@gmail.fr"
"Rabbit Roger";"MaSociete";" 31520  MaVille ";"";"";"info@mydomain.fr"
"Tutu ";"";"    ";"";"";"tutu@yahoo.net"';

    $xml->{'module'} = 'backupEntity';

    use XML::Simple;
    return XMLout( $xml, rootName => 'obmSatellite', XMLDecl => "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>" );
}

SWITCH: {
    if( !$parameters{'os-server'} ) {
        help();
    }

    if( !$parameters{'entity-type'} ) {
        help();
    }

    if( !$parameters{'entity-login'} ) {
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

my $root = '/backupentity/'.$parameters{'entity-type'}.'/'.$parameters{'entity-login'};

print 'Backup entity \''.$parameters{'entity-type'}.'\', login \''.$parameters{'entity-login'}.'\' on '.$parameters{'os-server'}.'\': ';
my $path = $root;
if( !$client->put( $parameters{'os-server'}, $path, genContent() ) ) {
    print '[OK]'."\n";
}else {
    print '[KO]'."\n";
    $errorCode++;
}

print 'Backup entity \''.$parameters{'entity-type'}.'\', login \''.$parameters{'entity-login'}.'\' on '.$parameters{'os-server'}.'\' with invalid content: ';
$path = $root;
if( $client->put( $parameters{'os-server'}, $path, genInvalidContent() ) ) {
    print '[OK]'."\n";
}else {
    print '[KO]'."\n";
    $errorCode++;
}


print "All tests done succefully !\n" if !$errorCode;
print STDERR $errorCode." fail !\n" if $errorCode;

exit $errorCode;
