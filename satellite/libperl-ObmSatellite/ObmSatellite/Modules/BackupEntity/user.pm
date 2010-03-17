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

    $self->{'privateContact'} = $content->{'privateContact'};
    $self->{'calendar'} = $content->{'calendar'};

    return 1;
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

    return $self->getTmpIcsPath().'/calendarExport.ics';
}


sub getTmpVcardFile {
    my $self = shift;

    return $self->getTmpVcardPath().'/privateContacts.csv';
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


sub getCyrusMailboxRoots {
    my $self = shift;

    my $mailboxRoot = $self->getCyrusPartitionPath().'/domain';
    $mailboxRoot .= eval {
            my $realm = $self->getRealm();
            $realm =~ /^(\w)/;
            my $partitionTree = '/'.$1.'/'.$realm;

            my $login = $self->getLogin();
            $login =~ /^(\w)/;
            return $partitionTree.'/'.$1.'/user/'.$login;
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
