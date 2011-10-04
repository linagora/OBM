package ObmSatellite::Modules::BackupEntity::mailshare;

$VERSION = '1.0';

$debug = 1;
use 5.006_001;

use ObmSatellite::Modules::BackupEntity::entities;
@ISA = qw(ObmSatellite::Modules::BackupEntity::entities);
use strict;

use File::Find;


sub getEntityContent {
    my $self = shift;

    return {
        mailbox => [ $self->{'folderRestore'} ]
    };
}


sub getLdapFilter {
    my $self = shift;

    return '(&(cn='.$self->getLogin().')(obmDomain='.$self->getRealm().'))';
}


sub _getMailboxesRoot {
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

    return $mailboxRoot;
}


sub getCyrusMailboxRoots {
    my $self = shift;

    my $mailboxRoot = $self->_getMailboxesRoot();

    my $backupLink = $self->getTmpMailboxPath();
    my @mailboxTree;
    find( {
            wanted => sub {
                my $path = $_;
                my $login = $self->getLogin();
                $login =~ s/\./^/g;
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


sub getMailboxRestorePath {
    my $self = shift;

    return $self->_getMailboxesRoot().'/q/'.$self->getLogin().'/'.$self->getRestoreFolder();

    return undef;
}


sub getRestoreMailboxArchiveStrip {
    my $self = shift;

    return 5;
}
