package ObmSatellite::Modules::BackupEntity::user;

$VERSION = '1.0';

$debug = 1;
use 5.006_001;

use ObmSatellite::Modules::BackupEntity::entities;
@ISA = qw(ObmSatellite::Modules::BackupEntity::entities);
use strict;


sub _setEntityContent {
    my $self = shift;
    my( $content ) = @_;

    if( !ref($content->{'calendar'}) && (defined($content->{'calendar'})) ) {
        $self->setIcs( $content->{'calendar'} );
    }

    if( ref($content->{'privateContact'}) eq 'HASH' ) {
        $self->setVcards( $content->{'privateContact'}->{'addressBook'} );
    }

    return 1;
}


sub getEntityContent {
    my $self = shift;

    my $content = {
        calendar => [ $self->getIcs() ],
        privateContact => {},
        mailbox => [ $self->{'folderRestore'} ]
    };

    my $addressBooks = $self->getVcards();
    while(my($addressBookName, $addressBookContent) = each(%{$addressBooks})) {
        push( @{$content->{'privateContact'}->{'addressBook'}},
            {
                $addressBookName => {
                    content => $addressBookContent
                }
            });
    }

    return $content;
}


sub getLdapFilter {
    my $self = shift;

    return '(&(uid='.$self->getLogin().')(obmDomain='.$self->getRealm().'))';
}


sub getTmpIcsPath {
    my $self = shift;

    return $self->getTmpBackupArchiveRoot().'/ics';
}


sub getTmpVcardPath {
    my $self = shift;

    return $self->getTmpBackupArchiveRoot().'/vcard';
}


sub getTmpIcsFile {
    my $self = shift;

    return $self->getTmpIcsPath().'/'.$self->getIcsFileName();
}


sub getTmpVcardFile {
    my $self = shift;
    my($addressBookName) = @_;

    return $self->getTmpVcardPath().'/'.$self->getVcardFileName($addressBookName);
}


sub getIcsFileName {
    my $self = shift;

    return 'calendarExport.ics';
}


sub getVcardFileName {
    my $self = shift;
    my($addressBookName) = @_;

    return $addressBookName.'.vcf';
}


sub getIcs {
    my $self = shift;

    if( !$self->{'calendar'} ) {
        return undef;
    }

    return $self->{'calendar'};
}


sub getVcards {
    my $self = shift;

    if(ref($self->{'privateContact'}) ne 'HASH') {
        return undef;
    }

    return $self->{'privateContact'};
}


sub getVcard {
    my $self = shift;
    my($name) = @_;

    if(!$self->{'privateContact'}->{$name}) {
        return undef;
    }

    return $self->{'privateContact'}->{$name};
}


sub setIcs {
    my $self = shift;
    my( $ics ) = @_;

    $self->{'calendar'} = $ics;
}


sub setVcards {
    my $self = shift;
    my( $addressBooks ) = @_;

    if(ref($addressBooks) eq 'HASH') {
        $self->{'privateContact'} = $addressBooks;
    }
}


sub setVcard {
    my $self = shift;
    my( $addressBookName, $vcard ) = @_;

    $self->{'privateContact'}->{$addressBookName} = $vcard;
}


sub _getMailboxRoot {
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

            my $login = $self->getLogin();
            $login =~ /^(\w)/;
            $firstLetter = lc($1);
            if( $firstLetter !~ /^[a-z]$/i ) {
                $firstLetter = 'q';
            }
            $login =~ s/\./^/g;

            return $partitionTree.'/'.$firstLetter.'/user/'.$login;
        };

    return $mailboxRoot;
}


sub getCyrusMailboxRoots {
    my $self = shift;

    my $mailboxRoot = $self->_getMailboxRoot();

    my $backupLink = $self->getTmpMailboxPath();
    $backupLink .= eval {
            my $login = $self->getLogin();
            $login =~ s/\./^/g;
            $login =~ /^(\w)/;
            return '/'.$1.'/user/'.$login;
        };

    return [ {
        cyrus => $mailboxRoot,
        backup => $backupLink
        } ];
}


sub getArchiveIcsPath {
    my $self = shift;

    return $self->getArchiveRoot().'/ics';
}


sub getArchiveVcardPath {
    my $self = shift;

    return $self->getArchiveRoot().'/vcard';
}


sub getMailboxPrefix {
    my $self = shift;

    return 'user/';
}


sub getMailboxRestorePath {
    my $self = shift;

    my $mailboxRestorePath = $self->_getMailboxRoot();


    return $self->_getMailboxRoot().'/';
}


sub getRestoreMailboxArchiveStrip {
    my $self = shift;

    return 6;
}
