package ObmSatellite::Modules::BackupEntity::entities;

$VERSION = '1.0';

$debug = 1;
use 5.006_001;

use ObmSatellite::Log::log;
@ISA = qw(ObmSatellite::Log::log);
use strict;

use ObmSatellite::Misc::regex;


sub new {
    my $class = shift;
    my( $fullLogin ) = @_;

    my $self = bless { }, $class;

    if( !$self->_setEntityLogin($fullLogin) ) {
        return undef;
    }

    return $self;
}


sub _setEntityLogin {
    my $self = shift;
    my( $fullLogin ) = @_;

    ($self->{'login'}, $self->{'realm'}) = split( '@', $fullLogin );
    if( $self->{'login'} !~ /$REGEX_LOGIN/ || $self->{'realm'} !~ /$REGEX_DOMAIN/ ) {
        return 0;
    }

    return 1;
}


sub getLogin {
    my $self = shift;

    return $self->{'login'};
}


sub getRealm {
    my $self = shift;

    return $self->{'realm'};
}


sub setContent {
    my $self = shift;
    my( $content ) = @_;

    if( ref($content) ne 'HASH' ) {
        return 0;
    }

    return $self->_setEntityContent( $content );
}


sub _setEntityContent {
    my $self = shift;
    my( $content ) = @_;

    return 1;
}


sub getEntityContent {
    my $self = shift;

    return undef;
}


sub setBackupRoot {
    my $self = shift;
    my( $backupRoot ) = @_;

    $self->{'backupRoot'} = $backupRoot;
}


sub getBackupRoot {
    my $self = shift;
    my( $backupRoot ) = @_;

    if( !defined($self->{'backupRoot'}) ) {
        $self->_log( 'Undefined backupRoot', 2 );
    }

    return $self->{'backupRoot'};
}


sub getBackupPath {
    my $self = shift;

    return $self->getBackupRoot().'/'.$self->getRealm();
}


sub setBackupName {
    my $self = shift;
    my( $backupName ) = @_;

    $self->{'backupName'} = $backupName;
}

sub setBackupFileName {
    my $self = shift;
    my( $backupFileName ) = @_;

    $self->{'backupFileName'} = $backupFileName;
}


sub getBackupName {
    my $self = shift;

    if( !defined($self->{'backupName'}) ) {
        $self->_getGenericBackupName();
    }

    return $self->{'backupName'};
}


sub _getGenericBackupName {
    my $self = shift;

    my $backupName = $self->getBackupNamePrefix().$self->_getStringDate().'.tar.gz';

    $self->setBackupName( $backupName );
}


sub _getStringDate {
    my $self = shift;

    my @dateTime = localtime(time);

    return eval {
        return 1900+$dateTime[5];
    }.eval {
        $dateTime[4]+=1;
        if($dateTime[4]<10) {
            return "0".$dateTime[4];
        }else {
            return $dateTime[4];
        }
    }.eval {
        if ($dateTime[3]<10) {
            return "0".$dateTime[3];
        }else {
            return $dateTime[3];
        }
    }.'-'.eval {
        if ($dateTime[2]<10) {
            return '0'.$dateTime[2];
        }else {
            return $dateTime[2];
        }
    }.eval {
        if ($dateTime[1]<10) {
            return '0'.$dateTime[1];
        }else {
            return $dateTime[1];
        }
    }.eval {
        if ($dateTime[0]<10) {
            return '0'.$dateTime[0];
        }else {
            return $dateTime[0];
        }
    }
}


sub getBackupNamePrefix {
    my $self = shift;

    return
    $self->getEntityType().'_-_'.$self->getLogin().'_-_'.$self->getRealm().'_-_';
}


sub getLdapFilter {
    my $self = shift;

    return undef;
}


sub setTmpBackupPath {
    my $self = shift;
    my( $tmpBackupPath ) = @_;

    $self->{'tmpBackupPath'} = $tmpBackupPath;
}


sub getTmpBackupPath {
    my $self = shift;

    return $self->{'tmpBackupPath'};
}


sub getTmpBackupArchiveRoot {
    my $self = shift;

    return $self->getTmpBackupPath().'/'.$self->getLogin().'@'.$self->getRealm();
}


sub getTmpIcsPath {
    my $self = shift;

    return undef;
}


sub getTmpVcardPath {
    my $self = shift;

    return undef;
}

sub getTmpBackupCurrentMailboxPath {
    my $self = shift;

    return $self->getTmpBackupArchiveRoot().'/mailbox_before_restore';
}

sub getTmpMailboxPath {
    my $self = shift;

    return $self->getTmpBackupArchiveRoot().'/mailbox';
}


sub getTmpIcsFile {
    my $self = shift;

    return undef;
}


sub getTmpVcardFile {
    my $self = shift;
    my($addressBookName) = @_;

    return undef;
}


sub getIcsFileName {
    my $self = shift;

    return undef;
}


sub getVcardFileName {
    my $self = shift;
    my($addressBookName) = @_;

    return undef;
}


sub getIcs {
    my $self = shift;

    return undef;
}


sub getVcards {
    my $self = shift;

    return undef;
}


sub getVcard {
    my $self = shift;
    my($name) = @_;

    return undef;
}


sub setIcs {
    my $self = shift;
    my( $ics ) = @_;
}


sub setVcards {
    my $self = shift;
    my( $addressBooks ) = @_;
}


sub setVcard {
    my $self = shift;
    my( $addressBookName, $vcard ) = @_;
}


sub getBackupFileName {
    my $self = shift;

	if( defined($self->{'backupFileName'})) {
        return $self->{'backupFileName'};
    }

    if( !$self->{'backupName'} ) {
        return undef;
    }

    return $self->getBackupPath().'/'.$self->{'backupName'};
}


sub setCyrusPartitionPath {
    my $self = shift;
    my( $cyrusPartitionPath ) = @_;

    $self->{'cyrusPartitionPath'} = $cyrusPartitionPath;
}


sub getCyrusPartitionPath {
    my $self = shift;

    return $self->{'cyrusPartitionPath'};
}


sub getCyrusMailboxRoots {
    my $self = shift;

    return [];
}


sub getEntityType {
    my $self = shift;

    my $entityType = ref($self);
    $entityType = eval {
        my @parts = split( '::', $entityType );
        return $parts[$#parts];
    };

    return $entityType;
}


sub getArchiveRoot {
    my $self = shift;

    return './'.$self->getLogin().'@'.$self->getRealm();
}


sub getArchiveIcsPath {
    my $self = shift;

    return undef;
}


sub getArchiveVcardPath {
    my $self = shift;

    return undef;
}


sub getArchiveMailboxPath {
    my $self = shift;

    return $self->getArchiveRoot().'/mailbox';
}


# Get Cyrus mailbox prefix
sub getMailboxPrefix {
    my $self = shift;

    return '';
}


sub _cleanLogin {
    my ($self) = @_;
    my $login = $self->getLogin() ;
    $login =~ /^(\w)/;
    $login =~ s/\./^/g;
    return $login;
}

# Get Cyrus mailbox restauration name
sub getMailboxRestoreFolder {
    my $self = shift;
    my( $new ) = @_;

    return $self->_cleanLogin().'@'.$self->getRealm();
}


# Get Cyrus mailbox path corresponding to Cyrus mailbox restauration name
sub getMailboxRestorePath {
    my $self = shift;

    return undef;
}


# How many directory needed to strip on backup archive to get mailbox root
sub getRestoreMailboxArchiveStrip {
    my $self = shift;

    return undef;
}
