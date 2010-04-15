#!/usr/bin/perl -w -T

use strict;


use File::Basename;
use XML::Simple;
my $modulePath = dirname($0);

if( $modulePath !~ /^([\/\.-_a-zA-Z0-9]+)$/ ) {
    print STDERR "unable to find needed perl modules !\n";
    exit 10;
}
$modulePath = $1;
require $modulePath.'/lib/common.pm';

my %parameters;
if( &lib::common::getIniParms( 'backupEntity', \%parameters ) ) {
    print STDERR "Fail to load parameters from INI file !\n";
    exit 1;
}

sub help {
    print STDERR "You need to complete 'backupEntity' section in 'unitTests.ini' file !\n";
    exit 1;
}

sub genInvalidContent {
    return 'invalid content, not XML !'
}

sub genContent {
    my $xml = '<obmSatellite name="unitTest">
<calendar>
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
END:VCALENDAR
</calendar>
<privateContact>
    <addressBook name="book1">
Display name;Display name;Company;Address;Mobile phone;Work phone;E-Mail
"Bronski ";"";"    ";"";"";"bronski@gmail.fr"
"Contact 00";"";"16, rue des prés 31000  Toulouse ";"";"";"pouet@gmail.fr"
"Rabbit Roger";"MaSociete";" 31520  MaVille ";"";"";"info@mydomain.fr"
"Tutu ";"";"    ";"";"";"tutu@yahoo.net"
    </addressBook>
    <addressBook name="space book">
Display name;Display name;Company;Address;Mobile phone;Work phone;E-Mail
"Bronski ";"";"    ";"";"";"bronski@gmail.fr"
"Contact 00";"";"16, rue des prés 31000  Toulouse ";"";"";"pouet@gmail.fr"
"Tutu ";"";"    ";"";"";"tutu@yahoo.net"
    </addressBook>
    <addressBook name="spacebook">
Display name;Display name;Company;Address;Mobile phone;Work phone;E-Mail
"Bronski ";"";"    ";"";"";"bronski@gmail.fr"
"Contact 00";"";"16, rue des prés 31000  Toulouse ";"";"";"pouet@gmail.fr"
"Tutu ";"";"    ";"";"";"tutu@yahoo.net"
    </addressBook>
</privateContact>
</obmSatellite>';

    return $xml;
}

my %entityType;
SWITCH: {
    if( !$parameters{'os-server'} ) {
        help();
    }

    if( !$parameters{'user-login'} && !$parameters{'mailshare-name'} ) {
        help();
    }

    if( $parameters{'user-login'} ) {
        $entityType{'user'} = $parameters{'user-login'};
    }

    if( $parameters{'mailshare-name'} ) {
        $entityType{'mailshare'} = $parameters{'mailshare-name'};
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

while( my($entityType, $entityLogin) = each(%entityType) ) {
    my $root = '/backupentity/'.$entityType.'/'.$entityLogin;
    my $path;
    
    if( $parameters{'test-backup-entity'} ) {
        print 'Backup entity \''.$entityType.'\', login \''.$entityLogin.'\' on '.$parameters{'os-server'}.'\': ';
        my $path = $root;
        if( !$client->put( $parameters{'os-server'}, $path, genContent() ) ) {
            print '[OK]'."\n";
        }else {
            print '[KO]'."\n";
            $errorCode++;

            my $response = $client->getResponse();
            my $xmlContent = XMLin( $response->content() );
            print $xmlContent->{'content'}."\n";
        }
    }
    
    if( $parameters{'test-backup-entity-invalid-request-content'} ) {
        print 'Backup entity \''.$entityType.'\', login \''.$entityLogin.'\' on '.$parameters{'os-server'}.'\' with invalid content: ';
        $path = $root;
        if( $client->put( $parameters{'os-server'}, $path, genInvalidContent() ) ) {
            print '[OK]'."\n";
        }else {
            print '[KO]'."\n";
            $errorCode++;

            my $response = $client->getResponse();
            my $xmlContent = XMLin( $response->content() );
            print $xmlContent->{'content'}."\n";
        }
    }
    
    
    if( $parameters{'test-backup-entity-no-request-content'} ) {
        print 'Backup entity \''.$entityType.'\', login \''.$entityLogin.'\' on '.$parameters{'os-server'}.'\' without content: ';
        $path = $root;
        if( !$client->put( $parameters{'os-server'}, $path, undef ) ) {
            print '[OK]'."\n";
        }else {
            print '[KO]'."\n";
            $errorCode++;

            my $response = $client->getResponse();
            my $xmlContent = XMLin( $response->content() );
            print $xmlContent->{'content'}."\n";
        }
    }
    
    
    if( $parameters{'test-list-available-backup'} ) {
        print 'Get available entity backup for \''.$entityType.'\', login \''.$entityLogin.'\' on '.$parameters{'os-server'}.'\': ';
        $path = '/availablebackup/'.$entityType.'/'.$entityLogin;
        if( !$client->get( $parameters{'os-server'}, $path, undef ) ) {
            print '[OK]'."\n";
        }else {
            print '[KO]'."\n";
            $errorCode++;

            my $response = $client->getResponse();
            my $xmlContent = XMLin( $response->content() );
            print $xmlContent->{'content'}."\n";
        }
    }

    if( $parameters{'test-restore-invalid-file'} ) {
        print 'Restore invalid \'user_-_test01_-_20091122.tar.gz\' backup file for entity \''.$entityType.'\', login \''.$entityLogin.'\' on '.$parameters{'os-server'}.'\': ';

        $path = '/restoreentity/'.$entityType.'/'.$entityLogin;
        if( $client->post( $parameters{'os-server'},
                            $path, 
                            '<obmSatellite module="backupEntity">
                            <backupFile>user_-_test01_-_20091122.tar.gz</backupFile>
                            </obmSatellite>' ) ) {
            print '[OK]'."\n";

            my $response = $client->getResponse();
            my $xmlContent = XMLin( $response->content() );
            print $xmlContent->{'content'}."\n";

        }else {
            print '[KO]'."\n";
            $errorCode++;

            my $response = $client->getResponse();
            my $xmlContent = XMLin( $response->content() );
            print $xmlContent->{'content'}."\n";
        }
    }


    if( $parameters{'test-restore-ics'} ) {
        $path = '/availablebackup/'.$entityType.'/'.$entityLogin;

        if( !$client->get( $parameters{'os-server'}, $path, undef ) ) {
            my $response = $client->getResponse();
            my $xmlContent = XMLin( $response->content() );

            if( !$xmlContent->{'backupFile'} ) {
                print '[KO]'."\n";
                $errorCode++;

                my $response = $client->getResponse();
                my $xmlContent = XMLin( $response->content() );
                print $xmlContent->{'content'}."\n";
            }else {
                print 'Restore calendar from \''.$xmlContent->{'backupFile'}.'\' backup for entity \''.$entityType.'\', login \''.$entityLogin.'\' on '.$parameters{'os-server'}.'\': ';
                $path = '/restoreentity/'.$entityType.'/'.$entityLogin.'/calendar';
                if( !$client->post( $parameters{'os-server'},
                                    $path, 
                                    '<obmSatellite module="backupEntity">
                                    <backupFile>'.$xmlContent->{'backupFile'}.'</backupFile>
                                    </obmSatellite>' ) ) {
                    print '[OK]'."\n";
                }else {
                    print '[KO]'."\n";
                    $errorCode++;
                }
            }
        }else {
            print '[KO]'."\n";
            $errorCode++;
        }
    }


    if( $parameters{'test-restore-vcard'} ) {
        $path = '/availablebackup/'.$entityType.'/'.$entityLogin;

        if( !$client->get( $parameters{'os-server'}, $path, undef ) ) {
            my $response = $client->getResponse();
            my $xmlContent = XMLin( $response->content() );

            if( !$xmlContent->{'backupFile'} ) {
                print '[KO]'."\n";
                $errorCode++;

                my $response = $client->getResponse();
                my $xmlContent = XMLin( $response->content() );
                print $xmlContent->{'content'}."\n";
            }else {
                print 'Restore contact from \''.$xmlContent->{'backupFile'}.'\' backup for entity \''.$entityType.'\', login \''.$entityLogin.'\' on '.$parameters{'os-server'}.'\': ';
                $path = '/restoreentity/'.$entityType.'/'.$entityLogin.'/contact';
                if( !$client->post( $parameters{'os-server'},
                                    $path, 
                                    '<obmSatellite module="backupEntity">
                                    <backupFile>'.$xmlContent->{'backupFile'}.'</backupFile>
                                    </obmSatellite>' ) ) {
                    print '[OK]'."\n";
                }else {
                    print '[KO]'."\n";
                    $errorCode++;
                }
            }
        }else {
            print '[KO]'."\n";
            $errorCode++;
        }
    }


    if( $parameters{'test-restore-mailbox'} ) {
        $path = '/availablebackup/'.$entityType.'/'.$entityLogin;

        if( !$client->get( $parameters{'os-server'}, $path, undef ) ) {
            my $response = $client->getResponse();
            my $xmlContent = XMLin( $response->content() );

            if( !$xmlContent->{'backupFile'} ) {
                print '[KO]'."\n";
                $errorCode++;

                my $response = $client->getResponse();
                my $xmlContent = XMLin( $response->content() );
                print $xmlContent->{'content'}."\n";
            }else {
                print 'Restore mailbox from \''.$xmlContent->{'backupFile'}.'\' backup for entity \''.$entityType.'\', login \''.$entityLogin.'\' on '.$parameters{'os-server'}.'\': ';
                $path = '/restoreentity/'.$entityType.'/'.$entityLogin.'/mailbox';
                if( !$client->post( $parameters{'os-server'},
                                    $path, 
                                    '<obmSatellite module="backupEntity">
                                    <backupFile>'.$xmlContent->{'backupFile'}.'</backupFile>
                                    </obmSatellite>' ) ) {
                    print '[OK]'."\n";
                }else {
                    print '[KO]'."\n";
                    $errorCode++;
                }
            }
        }else {
            print '[KO]'."\n";
            $errorCode++;
        }
    }
}


print "All tests done successfully !\n" if !$errorCode;
print STDERR $errorCode." fail !\n" if $errorCode;

exit $errorCode;
