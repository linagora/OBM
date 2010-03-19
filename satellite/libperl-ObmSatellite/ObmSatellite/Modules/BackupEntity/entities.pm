package ObmSatellite::Modules::BackupEntity::entities;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;

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


sub setBackupRoot {
    my $self = shift;
    my( $backupRoot ) = @_;

    $self->{'backupRoot'} = $backupRoot;
}


sub getBackupRoot {
    my $self = shift;
    my( $backupRoot ) = @_;

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


sub getBackupName {
    my $self = shift;

    if( !defined($self->{'backupName'}) ) {
        $self->_getGenericBackupName();
    }

    return $self->{'backupName'};
}


sub _getGenericBackupName {
    my $self = shift;

    my @dateTime = localtime(time);
    my $backupName = $self->getBackupNamePrefix().eval {
        return 1900+$dateTime[5];
    }.eval {
        $dateTime[4]+=1;
        if($dateTime[4]<10) {
            return "0".$dateTime[4];
        }else{
            return $dateTime[4];
        }
    }.eval {
        if ($dateTime[3]<10) {
            return "0".$dateTime[3];
        }else {
            return $dateTime[3];
        }
    }.'.tar.gz';

    $self->setBackupName( $backupName );
}


sub getBackupNamePrefix {
    my $self = shift;

    return $self->getEntityType().'_-_'.$self->getLogin().'_-_';
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

    return undef;
}


sub getIcs {
    my $self = shift;

    return undef;
}


sub getVcard {
    my $self = shift;

    return undef;
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
