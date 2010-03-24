package ObmSatellite::Modules::BackupEntity::mailshare;

$VERSION = '1.0';

$debug = 1;
use 5.006_001;

use ObmSatellite::Modules::BackupEntity::entities;
@ISA = qw(ObmSatellite::Modules::BackupEntity::entities);
use strict;

use File::Find;


sub getLdapFilter {
    my $self = shift;

    return '(&(cn='.$self->getLogin().')(obmDomain='.$self->getRealm().'))';
}


sub getCyrusMailboxRoots {
    my $self = shift;

    my $mailboxRoot = $self->getCyrusPartitionPath().'/domain';
    $mailboxRoot .= eval {
            my $realm = $self->getRealm();
            $realm =~ /^(\w)/;
            my $firstLetter = lc($1);
            if( $firstLetter !~ /^[a-z]$/i ) {
                $firstLetter = 'q';
            }
            my $partitionTree = '/'.$firstLetter.'/'.$realm;
        };

    my $backupLink = $self->getTmpMailboxPath();
    my @mailboxTree;
    find( {
            wanted => sub {
                my $path = $_;
                my $login = $self->getLogin();
                if( $path =~ /^($mailboxRoot\/(\w)\/$login)$/ ) {
                    push( @mailboxTree, {
                        cyrus => $1,
                        backup => $backupLink.'/'.$2.'/'.$login
                        } );
                }
            },
            no_chdir  => 1
        }, $mailboxRoot );

    return \@mailboxTree;
}


sub getMailboxFolderRestore {
    my $self = shift;

    return $self->getLogin().'/'..'@'.$self->getRealm();
}
