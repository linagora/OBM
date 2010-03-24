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

    if( (ref($content->{'calendar'}) eq 'ARRAY') && (defined($content->{'calendar'}->[0])) ) {
        $self->setIcs( $content->{'calendar'}->[0] );
    }

    if( (ref($content->{'privateContact'}) eq 'ARRAY') && (defined($content->{'privateContact'}->[0])) ) {
        $self->setVcard( $content->{'privateContact'}->[0] );
    }

    return 1;
}


sub getEntityContent {
    my $self = shift;

    return {
        calendar => [ $self->getIcs() ],
        privateContact => [ $self->getVcard() ]
    };
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

    return $self->getTmpVcardPath().'/'.$self->getVcardFileName();
}


sub getIcsFileName {
    my $self = shift;

    return 'calendarExport.ics';
}


sub getVcardFileName {
    my $self = shift;

    return 'privateContacts.csv';
}


sub getIcs {
    my $self = shift;

    if( !$self->{'calendar'} ) {
        return undef;
    }

    return $self->{'calendar'};
}


sub getVcard {
    my $self = shift;

    if( !$self->{'privateContact'} ) {
        return undef;
    }

    return $self->{'privateContact'};
}


sub setIcs {
    my $self = shift;
    my( $ics ) = @_;

    $self->{'calendar'} = $ics;
}


sub setVcard {
    my $self = shift;
    my( $vcard ) = @_;

    $self->{'privateContact'} = $vcard;
}


sub getCyrusMailboxRoots {
    my $self = shift;

    my $mailboxRoot = $self->getCyrusPartitionPath().'/domain';
    $mailboxRoot .= eval {
            my $realm = $self->getRealm();
            $realm =~ /^(\w)/;
            my $firstLetter = lc($1);
            if( $firstLetter =~ /^[a-z]$/i ) {
                $firstLetter = 'q';
            }
            my $partitionTree = '/'.$firstLetter.'/'.$realm;

            my $login = $self->getLogin();
            $login =~ /^(\w)/;
            $firstLetter = lc($1);
            if( $firstLetter =~ /^[a-z]$/i ) {
                $firstLetter = 'q';
            }
            return $partitionTree.'/'.$firstLetter.'/user/'.$login;
        };

    my $backupLink = $self->getTmpMailboxPath();
    $backupLink .= eval {
            my $login = $self->getLogin();
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
