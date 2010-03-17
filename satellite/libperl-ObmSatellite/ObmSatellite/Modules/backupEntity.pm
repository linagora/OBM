package ObmSatellite::Modules::backupEntity;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;

use ObmSatellite::Modules::abstract;
@ISA = qw(ObmSatellite::Modules::abstract);
use strict;

use File::Path;
use File::Copy;
use File::Find;

use HTTP::Status;
use ObmSatellite::Misc::constant;
use ObmSatellite::Misc::regex;

use constant OBM_BACKUP_ROOT => '/var/backup/obm';

sub _initHook {
    my $self = shift;

    $self->{'uri'} = [ '/backupentity' ];
    $self->{'neededServices'} = [ 'LDAP' ];

    $self->{'backupRoot'} = OBM_BACKUP_ROOT;
    $self->{'imapdConfFile'} = IMAPD_CONF_FILE;
    $self->{'tmpDir'} = TMP_DIR;
    $self->{'tarCmd'} = OBM_TAR_COMMAND;


    # Load some options from module configuration file
    my @params = ( 'backupRoot', 'imapdConfFile', 'tmpDir', 'tarCmd' );
    my $confFileParams = $self->_loadConfFile( \@params );

    $self->_log( $self->getModuleName().' module configuration :', 4 );
    for( my $i=0; $i<=$#params; $i++ ) {
        $self->{$params[$i]} = $confFileParams->{$params[$i]} if defined($confFileParams->{$params[$i]});
        $self->_log( $params[$i].' : '.$self->{$params[$i]}, 4 ) if defined($self->{$params[$i]});
    }

    return 1;
}


sub _putMethod {
    my $self = shift;
    my( $requestUri, $requestBody ) = @_;
    my %data;

    $data{'requestUri'} = $requestUri;
    if( $requestUri !~ /^\/backupentity\/([^\/]+)(.*)$/ ) {
        return $self->_response( RC_BAD_REQUEST, {
            content => [ 'Invalid URI '.$requestUri ],
            help => [ $self->getModuleName().' URI must be : /backupentity/<entity>' ]
            } );
    }
    $data{'entity'} = $1;

    if( !($data{'requestContent'} = $self->_xmlContent( $requestBody )) ) {
        return $self->_response( RC_BAD_REQUEST, {
            content => [ 'Invalid request content' ],
            help => [ $self->getModuleName().' request content must use XML form' ]
            } );
    }
    

    my $response;
    SWITCH: {
        if( $data{'entity'} eq 'user' ) {
            $response = $self->_userEntity( \%data );
            last SWITCH;
        }

        if( $data{'entity'} eq 'mailshare' ) {
            $response = $self->_mailshareEntity( \%data );
            last SWITCH;
        }

        return $self->_response( RC_NOT_FOUND, {
            content => [ 'Unknow entity \''.$data{'entity'}.'\'' ]
            } );
    }

    $self->_removeTmpArchive( \%data );
    $self->_removeBackupBackupFile( \%data );
    $self->_purgeOldBackupFile( \%data );

    if( !$response ) {
        $self->_log( 'Backup '.$data{'login'}.'@'.$data{'realm'}.' successfully', 3 );
        $response = $self->_response( RC_OK );
    }else {
        $self->_log( 'Fail to backup '.$data{'login'}.'@'.$data{'realm'}, 1 );
    }

    return $response;
}


sub _userEntity {
    my $self = shift;
    my( $data ) = @_;

    if( my $return = $self->_getEntityLogin( $data ) ) {
        return $return;
    }

    if( my $return = $self->_getBackupName( $data ) ) {
        return $return;
    }

    if( my $return = $self->_createUserArchive( $data ) ) {
        return $return;
    }

    return undef;
}


sub _mailshareEntity {
    my $self = shift;
    my( $data ) = @_;

    if( my $return = $self->_getEntityLogin( $data ) ) {
        return $return;
    }

    if( my $return = $self->_getBackupName( $data ) ) {
        return $return;
    }

    if( my $return = $self->_createMailshareArchive( $data ) ) {
        return $return;
    }

    return undef;
}


sub _getEntityLogin {
    my $self = shift;
    my( $data ) = @_;

    my $regexp = '^\/backupentity\/'.$data->{'entity'}.'\/([^\/]+)(.*)$';
    if( $data->{'requestUri'} !~ /$regexp/ ) {
        return $self->_response( RC_BAD_REQUEST, {
            content => [ 'Invalid URI '.$data->{'requestUri'} ],
            help => [ $self->getModuleName().' URI must be : /backupentity/'.$data->{'entity'}.'/<login>@<realm>' ]
            } );
    }

    my $fullLogin = $1;
    ($data->{'login'}, $data->{'realm'}) = split( '@', $fullLogin );
    if( $data->{'login'} !~ /$REGEX_LOGIN/ || $data->{'realm'} !~ /$REGEX_DOMAIN/ ) {
        return $self->_response( RC_BAD_REQUEST, {
            content => [ 'Invalid login '.$data->{'requestUri'} ],
            help => [ $self->getModuleName().' URI must be : /backupentity/'.$data->{'entity'}.'/<login>@<realm>' ]
            } );
    }

    return $self->_isEntityDefined( $data );
}


sub _isEntityDefined {
    my $self = shift;
    my( $data ) = @_;

    my $ldapFilter;
    if( $data->{'entity'} eq 'user' ) {
        $ldapFilter = '(&(uid='.$data->{'login'}.')(obmDomain='.$data->{'realm'}.'))';
    }elsif( $data->{'entity'} eq 'mailshare' ) {
        $ldapFilter = '(&(cn='.$data->{'login'}.')(obmDomain='.$data->{'realm'}.'))';
    }

    my $entity = $self->_getLdapValues(
        $ldapFilter,
        [] );

    if( $#{$entity} != 0 ) {
        return $self->_response( RC_BAD_REQUEST, {
            content => [ 'Entity '.$data->{'login'}.'@'.$data->{'realm'}.' doesn\'t exist in LDAP' ],
            } );
    }

    return undef;
}


sub _getBackupName {
    my $self = shift;
    my( $data ) = @_;

    $data->{'backupPath'} = $self->{'backupRoot'}.'/'.$data->{'realm'};

    if( ! -d $data->{'backupPath'} ) {
        if( $self->_mkdir( $data->{'backupPath'} ) ) {
            return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                content => [ 'Can\'t create \''.$data->{'backupPath'}.'\'' ]
                } );
        }
    }

    my @dateTime = localtime(time);
    $data->{'backupName'} = $data->{'login'}.'_-_'.eval {
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

    $self->_log( 'Backup path : '.$data->{'backupPath'}, 4 );
    $self->_log( 'Backup name : '.$data->{'backupName'}, 4 );

    return undef;
}


sub _backupBackupFile {
    my $self = shift;
    my( $data ) = @_;

    my $backupFullPath = $data->{'backupPath'}.'/'.$data->{'backupName'};
    my $backupFullPathBackup = $backupFullPath.'.backup';

    if( -e $backupFullPathBackup ) {
        $self->_log( 'Remove backuped backup file : '.$backupFullPathBackup, 5 );
        if( !unlink( $backupFullPathBackup ) ) {
            return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                content => [ 'Can\'t remove backuped backup file : '.$backupFullPathBackup ]
                } );
        }
    }
    
    if( -e $backupFullPath ) {
        $self->_log( 'Rename '.$backupFullPath.' to '.$backupFullPathBackup, 5 );
        if( !move( $backupFullPath, $backupFullPathBackup ) ) {
            return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                content => [ 'Can\'t rename '.$backupFullPath.' to '.$backupFullPathBackup ]
                } );
        }
    }

    return undef;
}


sub _removeBackupBackupFile {
    my $self = shift;
    my( $data ) = @_;

    my $backupFullPath = $data->{'backupPath'}.'/'.$data->{'backupName'};
    my $backupFullPathBackup = $backupFullPath.'.backup';

    if( -e $backupFullPathBackup ) {
        $self->_log( 'Remove backuped backup file '.$backupFullPathBackup, 5 );
        if( !unlink( $backupFullPathBackup ) ) {
            $self->_log( 'Fail to remove backuped backup file '.$backupFullPathBackup, 5 );
            return 1;
        }
    }else {
        $self->_log( $backupFullPathBackup.' not found', 5 );
        return 2;
    }

    return 0;
}


sub _restoreBackupBackupFile {
    my $self = shift;
    my( $data ) = @_;

    my $backupFullPath = $data->{'backupPath'}.'/'.$data->{'backupName'};
    my $backupFullPathBackup = $backupFullPath.'.backup';

    if( -e $backupFullPathBackup ) {
        $self->_log( 'Restore backuped backup file '.$backupFullPathBackup, 5 );
        if( !move( $backupFullPathBackup, $backupFullPath ) ) {
            $self->_log( 'Fail to restore backuped backup file '.$backupFullPathBackup, 5 );
            return 1;
        }
    }else {
        $self->_log( $backupFullPathBackup.' not found', 5 );
        return 2;
    }

    return 0;
}


sub _purgeOldBackupFile {
    my $self = shift;
    my( $data ) = @_;

    my $backupName = $data->{'login'}.'_-_';

    opendir( DIR, $data->{'backupPath'} );
    my @userBackup = grep( /^$backupName/, readdir(DIR) );
    close(DIR);

    for( my $i=0; $i<=$#userBackup; $i++ ) {
        $userBackup[$i] =~ /^(.+)$/;
        $userBackup[$i] = $1;

        if( $userBackup[$i] ne $data->{'backupName'} ) {
            $self->_log( 'Remove old backup file '.$userBackup[$i], 5 );
            unlink( $data->{'backupPath'}.'/'.$userBackup[$i] );
        }
    }
}


sub _writeArchive {
    my $self = shift;
    my( $data, $files ) = @_;

    my $backupFullPath = $data->{'backupPath'}.'/'.$data->{'backupName'};
    my $backupFullPathBackup = $backupFullPath.'.backup';

    if( my $result = $self->_backupBackupFile( $data ) ) {
        return $result;
    }

    my $cmd = $self->{'tarCmd'}.' --ignore-failed-read -C '.$data->{'tmpBackupPath'}.' -czhf '.$backupFullPath.' . > /dev/null 2>&1';

    $self->_log( 'Executing '.$cmd, 4 );
    if( my $returnCode = 0xffff & system( $cmd ) ) {
        my $content = {
            content => [ 'Can\'t write backup archive '.$backupFullPath ]
            };

        my $result = $self->_restoreBackupBackupFile( $data );
        SWITCH: {
            if( $result == 1 ) {
                push( @{$content->{'content'}}, 'No backuped backup file found' );
                last SWITCH;
            }

            if( $result == 2 ) {
                push( @{$content->{'content'}}, 'Can\'t move backuped backup file from'.$backupFullPathBackup.' to '.$backupFullPath );
                last SWITCH;
            }

            push( @{$content->{'content'}}, 'Backuped backup file '.$backupFullPathBackup.' restored to '.$backupFullPath );
            last SWITCH;
        }

        return $self->_response( RC_INTERNAL_SERVER_ERROR, $content );
    }

    return undef;
}


sub _createUserArchive {
    my $self = shift;
    my( $data ) = @_;

    $self->_log( 'Beginning backup for user '.$data->{'login'}.'@'.$data->{'realm'}, 3 );

    if( my $result = $self->_prepareTmpArchive( $data ) ) {
        return $result;
    }

    if( $self->_addIcs( $data ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Can\'t add ICS file to archive' ]
            } );
    }

    if( $self->_addVcard( $data ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Can\'t add VCARD file to archive' ]
            } );
    }

    if( $self->_addUserMailbox( $data ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Can\'t add mailbox file to archive' ]
            } );
    }

    my $result = $self->_writeArchive( $data );

    return $self->_writeArchive( $data );
}


sub _createMailshareArchive {
    my $self = shift;
    my( $data ) = @_;

    $self->_log( 'Beginning backup for mailshare '.$data->{'login'}.'@'.$data->{'realm'}, 3 );

    if( my $result = $self->_prepareTmpArchive( $data ) ) {
        return $result;
    }

    if( $self->_addUserMailshare( $data ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Can\'t add mailbox file to archive' ]
            } );
    }

    my $result = $self->_writeArchive( $data );

    return $self->_writeArchive( $data );
}


sub _mkdir {
    my $self = shift;
    my( $path ) = @_;
    my $errors = [];

    if( ! -d $path ) {
        $self->_log( 'Creating path \''.$path.'\'', 5 );

        eval {
            local $SIG{__WARN__} = undef;
            local $SIG{__DIE__} = undef;
            mkpath( $path,
                { error => \$errors } );
        };

        if( $#$errors >= 0 ) {
            return $errors;
        }
    }

    return undef;
}


sub _rmdir {
    my $self = shift;
    my( $path ) = @_;
    my $errors = [];

    if( -d $path ) {
        $self->_log( 'Removing path \''.$path.'\'', 5 );

        eval {
            local $SIG{__WARN__} = undef;
            local $SIG{__DIE__} = undef;
            rmtree( $path,
                { error => \$errors } );
        };

        if( $#$errors >= 0 ) {
            return $errors;
        }
    }

    return undef;
}


sub _mkSymlink {
    my $self = shift;
    my( $dstFile, $symlinkName )  = @_;

    if( !eval{ symlink("",""); 1 } ) {
        $self->_log( 'Symlinks must be possible file system !', 0 );
        return [ 'Symlinks must be possible on file system !' ];
    }

    if( (-e $symlinkName) && (!-l $symlinkName) ) {
        if( !unlink( $symlinkName ) ) {
            $self->_log( $symlinkName.' exist but isn\'t a symlink and can\'t be remove', 1 );
            return [ $symlinkName.' exist but isn\'t a symlink and can\'t be remove' ];
        }
    }

    if( -d $symlinkName ) {
        $self->_log( $symlinkName.' exist and is a diretory', 1 );
        return [ $symlinkName.' exist and is a diretory' ];
    }


    $self->_log( 'Creating link from '.$dstFile.' to '.$symlinkName, 5 );
    if( !symlink( $dstFile, $symlinkName ) ) {
        return [ 'Creating link from '.$dstFile.' to '.$symlinkName.' fail' ];
    }

    return undef;
}


sub _removeTmpArchive {
    my $self = shift;
    my( $data ) = @_;

    if( $self->_rmdir( $data->{'tmpBackupPath'} ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Fail to remove '.$data->{'tmpBackupPath'} ]
            } );
    }

    return undef;
}


sub _prepareTmpArchive {
    my $self = shift;
    my( $data ) = @_;

    $data->{'tmpBackupPath'} = $self->{'tmpDir'}.'/'.$self->getModuleName().'_-_'.$data->{'realm'}.'_-_'.$data->{'login'};
    $self->_log( 'Temporary backup path: '.$data->{'tmpBackupPath'}, 4 );

    if( ($data->{'entity'} eq 'user')
        && $self->_mkdir( $data->{'tmpBackupPath'}.'/'.$data->{'login'}.'@'.$data->{'realm'}.'/ics' ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Fail to create '.$data->{'tmpBackupPath'} ]
            } );
    }

    if( ($data->{'entity'} eq 'user')
        && $self->_mkdir( $data->{'tmpBackupPath'}.'/'.$data->{'login'}.'@'.$data->{'realm'}.'/vcard' ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Fail to create '.$data->{'tmpBackupPath'} ]
            } );
    }

    if( $self->_mkdir( $data->{'tmpBackupPath'}.'/'.$data->{'login'}.'@'.$data->{'realm'}.'/mailbox' ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Fail to create '.$data->{'tmpBackupPath'} ]
            } );
    }

    return undef;
}


sub _addIcs {
    my $self = shift;
    my( $data ) = @_;

    if( !open( FIC,
            '>:encoding(UTF-8)',
            $data->{'tmpBackupPath'}.'/'.$data->{'login'}.'@'.$data->{'realm'}.'/ics/calendarExport.ics' ) ) {
        $self->_log( $!, 1 );
        return 1;
    }

    print FIC $data->{'requestContent'}->{'calendar'};
    close(FIC);

    return 0;
}


sub _addVcard {
    my $self = shift;
    my( $data ) = @_;

    if( !open( FIC,
            '>:encoding(UTF-8)',
            $data->{'tmpBackupPath'}.'/'.$data->{'login'}.'@'.$data->{'realm'}.'/vcard/privateContacts.csv' ) ) {
        $self->_log( $!, 1 );
        return 1;
    }

    print FIC $data->{'requestContent'}->{'privateContact'};
    close(FIC);

    return 0;
}


sub _addUserMailbox {
    my $self = shift;
    my( $data ) = @_;

    if( my $result = $self->_getCyrusPartitionPath( $data ) ) {
        return $result;
    }

    my $mailboxRoot = $data->{'cyrusPartitionPath'}.'/domain';
    $mailboxRoot .= eval {
            $data->{'realm'} =~ /^(\w)/;
            my $partitionTree = '/'.$1.'/'.$data->{'realm'};

            $data->{'login'} =~ /^(\w)/;
            return $partitionTree.'/'.$1.'/user/'.$data->{'login'};
        };

    my $backupLink = $data->{'tmpBackupPath'}.'/'.$data->{'login'}.'@'.$data->{'realm'}.'/mailbox'.eval {
            $data->{'login'} =~ /^(\w)/;
            return '/'.$1;
        };

    if( $self->_mkdir( $backupLink ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Can\'t create \''.$backupLink.'\'' ]
            } );
    }

    $backupLink .= '/'.$data->{'login'};

    if( $self->_mkSymlink( $mailboxRoot, $backupLink ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Can\'t create mailbox backup link '.$backupLink.' to '.$mailboxRoot ]
            } );
    }

    return undef;
}


sub _addUserMailshare {
    my $self = shift;
    my( $data ) = @_;

    if( my $result = $self->_getCyrusPartitionPath( $data ) ) {
        return $result;
    }

    my $mailboxRoot = $data->{'cyrusPartitionPath'}.'/domain';
    $mailboxRoot .= eval {
            $data->{'realm'} =~ /^(\w)/;
            my $partitionTree = '/'.$1.'/'.$data->{'realm'};
        };

    my %mailboxTree;
    find( {
            wanted => sub {
                my $path = $_;
                if( $path =~ /^($mailboxRoot\/(\w)\/$data->{'login'})$/ ) {
                    $mailboxTree{$2} = $1;
                    $self->_log( $path, 5 );
                }
            },
            no_chdir  => 1
        }, $mailboxRoot );

    my $backupLink = $data->{'tmpBackupPath'}.'/'.$data->{'login'}.'@'.$data->{'realm'}.'/mailbox';
    while( ( my $letter, $mailboxRoot ) = each(%mailboxTree) ) {
        my $currentBackupLink = $backupLink.'/'.$letter;

        if( $self->_mkdir( $currentBackupLink ) ) {
            return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                content => [ 'Can\'t create \''.$currentBackupLink.'\'' ]
                } );
        }

        $currentBackupLink .= '/'.$data->{'login'};

        if( $self->_mkSymlink( $mailboxRoot, $currentBackupLink ) ) {
            return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                content => [ 'Can\'t create mailbox backup link '.$currentBackupLink.' to '.$mailboxRoot ]
                } );
        }
    }

    return undef;
}


sub _getCyrusPartitionPath {
    my $self = shift;
    my( $data ) = @_;

    my $imapPartitionName = $data->{'realm'};
    $imapPartitionName =~ s/[\.-]/_/g;
    $imapPartitionName = 'partition-'.$imapPartitionName;
    $self->_log( 'Cyrus partition name : \''.$imapPartitionName.'\'', 5 );

    if( !open( FIC, $self->{'imapdConfFile'} ) ) {
        $self->_log( 'Can\'t open '.$self->{'imapdConfFile'}, 0 );
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Can\'t open '.$self->{'imapdConfFile'} ]
            } );
    }

    delete($data->{'cyrusPartitionPath'});
    while( !$data->{'cyrusPartitionPath'} && (my $line = <FIC>) ) {
        if( $line =~ /^$imapPartitionName:(\s)*([^\s]+)$/ ) {
            $data->{'cyrusPartitionPath'} = $2;
        }
    }

    close(FIC);

    if( !$data->{'cyrusPartitionPath'} ) {
        $self->_log( 'Can\'t find partition path for cyrus parition', 1 );
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Can\'t find partition path for cyrus parition' ]
            } );
    }

    $self->_log( 'Cyrus partition path : \''.$data->{'cyrusPartitionPath'}.'\'', 5 );

    return undef;
}


# Perldoc

=head1 NAME

BackupEntity obmSatellite module

=head1 SYNOPSIS

This module backup entity data.

This module is XML/HTTP REST compliant.

=head1 COMMAND

=over 4

=item B<PUT /backupentity>/<entity>/<login>@<realm> : backup login entity data.

=over 4

=item PUT data : none

=item Return success or fail status

=back

=back
