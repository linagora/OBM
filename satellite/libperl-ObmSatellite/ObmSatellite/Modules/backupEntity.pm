package ObmSatellite::Modules::backupEntity;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;

use ObmSatellite::Modules::abstract;
@ISA = qw(ObmSatellite::Modules::abstract);
use strict;
use POSIX;

eval {
    require ObmSatellite::Modules::BackupEntity::user;
    require ObmSatellite::Modules::BackupEntity::mailshare;
    require MIME::Lite;
    require MIME::Base64;
    require Net::FTP;
};

use File::Path;
use File::Copy;
use File::Find;
use File::Basename;

use Encode qw/encode decode/;

use HTTP::Status;
use ObmSatellite::Misc::constant;
use ObmSatellite::Misc::regex;

use constant OBM_CONF_INI_FILE => '/etc/obm/obm_conf.ini';
use constant RECONSTRUCT_PATH => '/usr/lib/cyrus-imapd:/usr/lib/cyrus/bin';
use constant RECONSTRUCT_CMD => 'reconstruct';
use constant QUOTA_PATH => '/usr/lib/cyrus-imapd:/usr/lib/cyrus/bin';
use constant QUOTA_CMD => 'quota';
use constant MAIL_REPORT_RECIPIENT => 'x-obm-backup';
use constant BACKUP_FTP_TIMEOUT => 10;


sub _setUri {
    my $self = shift;

    return [ '/backupentity', '/restoreentity', '/availablebackup', '/retrievebackup', '/endOfBackups' ];
}


sub _initHook {
    my $self = shift;
    
    if( ! -f OBM_CONF_INI_FILE || ! -r OBM_CONF_INI_FILE ) {
        print 'WARNING: Unable to open OBM configuration file '. OBM_CONF_INI_FILE ."\n";
        return;
    }

    if( my $cfgFile = Config::IniFiles->new( -file => OBM_CONF_INI_FILE ) ) {
    	$self->{'backupRoot'} = $cfgFile->val( 'global', 'backupRoot' );
    	# In case the ini file contains quotes around the directory name
    	$self->{'backupRoot'} =~ s/"//g;
    	
    	$self->_log('Backup root: ' . $self->{'backupRoot'}, 4);
    }

    $self->{'neededServices'} = [ 'LDAP', 'CYRUS' ];

    $self->{'imapdConfFile'} = IMAPD_CONF_FILE;
    $self->{'tmpDir'} = TMP_DIR;
    $self->{'tarCmd'} = OBM_TAR_COMMAND;


    # Load some options from module configuration file
    my @params = ( 'imapdConfFile', 'tmpDir', 'tarCmd' );
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

    if( $requestUri !~ /^\/backupentity\/(user|mailshare)\/([^\/]+)$/ ) {
        return $self->_response( RC_BAD_REQUEST, {
            content => [ 'Invalid URI '.$requestUri ],
            help => [ $self->getModuleName().' URI must be : /backupentity/<entity>/<login>@<realm>' ]
            } );
    }

    my $entityType = $1;
    my $entity = undef;
    SWITCH: {
        if( $entityType eq 'user' ) {
            $entity = ObmSatellite::Modules::BackupEntity::user->new( $2 );
            last SWITCH;
        }

        if( $entityType eq 'mailshare' ) {
            $entity = ObmSatellite::Modules::BackupEntity::mailshare->new( $2 );
            last SWITCH;
        }

        return $self->_response( RC_NOT_FOUND, {
            content => [ 'Unknow entity \''.$entityType.'\'' ]
            } );
    }

    if( !defined($entity) ) {
        return $self->_response( RC_BAD_REQUEST, {
            content => [ 'Invalid login '.$requestUri ],
            help => [ $self->getModuleName().' URI must be : /backupentity/'.$entityType.'/<login>@<realm>' ]
            } );
    }

    if( my $result = $self->_isEntityExist( $entity ) ) {
        return $result;
    }

    my $xmlContent;
    if( !($xmlContent = $self->_xmlContent($requestBody, ForceArray => ['addressBook', 'email']))
        || !($entity->setContent($xmlContent))
    ) {
        return $self->_response( RC_BAD_REQUEST, {
            content => [ 'Invalid request content' ],
            help => [ $self->getModuleName().' request content must use XML form' ]
            } );
    }


    if( my $return = $self->_setBackupRoot( $entity ) ) {
        return $return;
    }

    my $response = $self->_createArchive( $entity );
    

    $self->_removeTmpArchive( $entity );
    $self->_removeBackupBackupFile( $entity );
    $self->_purgeOldBackupFiles( $entity );
    my $language = $self->_getDefaultObmLang( $entity->getRealm() );

    if( !$response ) {
        $self->_log( 'Backup '.$entity->getLogin().'@'.$entity->getRealm().' successfully', 3 );
        $response = $self->_response( RC_OK, {
            content => [ 'Backup '.$entity->getLogin().'@'.$entity->getRealm().' successfully' ]
            } );
        if ( $language eq 'fr' ) {
         $response = $self->_response( RC_OK, {
             content => [ 'Sauvegarde '.$entity->getLogin().'@'.$entity->getRealm().' reussie' ]
             } );
        }

        $self->_pushFtpBackup($entity, $xmlContent->{'options'}, $response);
    }else {
        $self->_log( 'Fail to backup '.$entity->getLogin().'@'.$entity->getRealm(), 1 );
    }

    $self->_sendMailBackupReport($entity, $xmlContent->{'options'}, $response);

    return $response;
}


sub _getMethod {
    my $self = shift;
    my( $requestUri, $requestBody ) = @_;

    if( $requestUri !~ /^\/availablebackup\/(user|mailshare)\/([^\/]+)$/ ) {
        return $self->_response( RC_BAD_REQUEST, {
            content => [ 'Invalid URI '.$requestUri ],
            help => [ $self->getModuleName().' URI must be : /restoreentity/<entity>/<login>@<realm>' ]
            } );
    }

    my $entity = $1;
    SWITCH: {
        if( $entity eq 'user' ) {
            $entity = ObmSatellite::Modules::BackupEntity::user->new( $2 );
            last SWITCH;
        }

        if( $entity eq 'mailshare' ) {
            $entity = ObmSatellite::Modules::BackupEntity::mailshare->new( $2 );
            last SWITCH;
        }

        return $self->_response( RC_NOT_FOUND, {
            content => [ 'Unknow entity \''.$entity.'\'' ]
            } );
    }

    if( !defined($entity) ) {
        return $self->_response( RC_BAD_REQUEST, {
            content => [ 'Invalid login '.$requestUri ],
            help => [ $self->getModuleName().' URI must be : /backupentity/'.$entity.'/<login>@<realm>' ]
            } );
    }

    if( my $result = $self->_isEntityExist( $entity ) ) {
        return $result;
    }

    if( my $return = $self->_setBackupRoot( $entity ) ) {
        return $return;
    }

    my $availableBackupFile = $self->_getAvailableBackupFile( $entity );

    if( ref($availableBackupFile) ne 'ARRAY' ) {
        return $availableBackupFile;
    }

    $self->_log( 'Getting available backup file for '.$entity->getLogin().'@'.$entity->getRealm().' successfully', 3 );
    return $self->_response( RC_OK, {
        backupFile => $availableBackupFile
    } );
}


sub _postMethod {
    my $self = shift;
    my( $requestUri, $requestBody ) = @_;

    if($requestUri =~ /^\/restoreentity/) {
        return $self->_postMethodRestoreEntity($requestUri, $requestBody);
    }

    if($requestUri =~ /^\/retrievebackup/) {
        return $self->_postMethodRetrieveBackup($requestUri, $requestBody);
    }

    if($requestUri =~ /^\/endOfBackups/) {
        return $self->endOfBackups();
    }

    return $self->_response( RC_BAD_REQUEST, {
        content => [ 'Invalid URI '.$requestUri ]
        } );
}

sub endOfBackups {
    my $self = shift;
    my $directory = $self->{'backupRoot'};
    if (!(-d "$directory")) {
        return $self->_response( RC_BAD_REQUEST, {
            content => [' The Directory : ' . $self->{'backupRoot'} . ' doesn\'t exist ! ' ]
            } );
    }
    my $successFile = 'successDailyBackup.txt';
    open( DESCR, "> $directory/$successFile" ) ||
        return $self->_response( RC_BAD_REQUEST, {
            content => [' Couldn\'t create file : ' . $successFile ]
            } );
    
    my $response = $self->_response(RC_OK);
    my $localtime=strftime("dailyBackupDate_%d-%b-%Y", localtime);
    print DESCR $localtime . "\n";
    close DESCR;

    return $response;
}

sub _postMethodRestoreEntity {
    my $self = shift;
    my($requestUri, $requestBody) = @_;

    if( $requestUri !~ /^\/restoreentity\/(user|mailshare)\/([^\/]+)(\/(mailbox|contact|calendar)){0,1}$/ ) {
        return $self->_response( RC_BAD_REQUEST, {
            content => [ 'Invalid URI '.$requestUri ],
            help => [ $self->getModuleName().' URI must be : /restoreentity/<entity>/<login>@<realm>[/[mailbox|contact|calendar]]' ]
            } );
    }

    my $entity = $1;
    my $data = $4;

    SWITCH: {
        if( $entity eq 'user' ) {
            $entity = ObmSatellite::Modules::BackupEntity::user->new( $2 );
            last SWITCH;
        }

        if( $entity eq 'mailshare' ) {
            $entity = ObmSatellite::Modules::BackupEntity::mailshare->new( $2 );
            last SWITCH;
        }

        return $self->_response( RC_NOT_FOUND, {
            content => [ 'Unknow entity \''.$entity.'\'' ]
            } );
    }

    if( !defined($entity) ) {
        return $self->_response( RC_BAD_REQUEST, {
            content => [ 'Invalid login '.$requestUri ],
            help => [ $self->getModuleName().' URI must be : /restoreentity/'.$entity.'/<login>@<realm>[/[mailbox|contact|calendar]]' ]
            } );
    }

    if( my $result = $self->_isEntityExist( $entity ) ) {
        return $result;
    }

    my $xmlContent = $self->_xmlContent($requestBody, ForceArray => ['email']);
    if($xmlContent) {
        if(!ref($xmlContent->{'backupFile'})) {
            $entity->setBackupName( $xmlContent->{'backupFile'} );
        }else {
            return $self->_response( RC_BAD_REQUEST, {
                content => [ 'Invalid request content' ],
                help => [ $self->getModuleName().' restore request content must have \'backupFile\' element' ]
                } );
        }
    }else {
        return $self->_response( RC_BAD_REQUEST, {
            content => [ 'Invalid request content' ],
            help => [ $self->getModuleName().' restore request content must use XML form' ]
            } );
    }

    if( my $return = $self->_setBackupRoot( $entity ) ) {
        return $return;
    }

    my $response = $self->_restoreFromArchive( $entity, $data );

    $self->_removeTmpArchive( $entity );
    $self->_sendMailRestoreReport($entity, $xmlContent->{'options'}, $response);

    return $response;
}


sub _postMethodRetrieveBackup {
    my $self = shift;
    my($requestUri, $requestBody) = @_;

    if( $requestUri !~ /^\/retrievebackup\/(user|mailshare)\/([^\/]+)$/ ) {
        return $self->_response( RC_BAD_REQUEST, {
            content => [ 'Invalid URI '.$requestUri ],
            help => [ $self->getModuleName().' URI must be : /retrievebackup/<entity>/<login>@<realm>[/[mailbox|contact|calendar]]' ]
            } );
    }

    my $entity = $1;

    SWITCH: {
        if( $entity eq 'user' ) {
            $entity = ObmSatellite::Modules::BackupEntity::user->new( $2 );
            last SWITCH;
        }

        if( $entity eq 'mailshare' ) {
            $entity = ObmSatellite::Modules::BackupEntity::mailshare->new( $2 );
            last SWITCH;
        }

        return $self->_response( RC_NOT_FOUND, {
            content => [ 'Unknow entity \''.$entity.'\'' ]
            } );
    }

    if( !defined($entity) ) {
        return $self->_response( RC_BAD_REQUEST, {
            content => [ 'Invalid login '.$requestUri ],
            help => [ $self->getModuleName().' URI must be : /retrievebackup/'.$entity.'/<login>@<realm>' ]
            } );
    }

    if( my $result = $self->_isEntityExist( $entity ) ) {
        return $result;
    }

    my $xmlContent = $self->_xmlContent($requestBody, ForceArray => ['email']);

    if( my $return = $self->_setBackupRoot( $entity ) ) {
        return $return;
    }

    my $response = $self->_response(RC_OK);

    $self->_getFtpBackup($entity, $response);

    my $pushFtp = $response->getContentValue('pushFtp');
    if($pushFtp->{'success'} eq 'false') {
        $response->setStatus(RC_INTERNAL_SERVER_ERROR);
        $response->setExtraContent({
            content => ['Fail to download '.$entity->getLogin().'@'.$entity->getRealm().' backup archive from backup FTP serveur']
        });
    }else {
        $response->setExtraContent({
            content => ['Download '.$entity->getLogin().'@'.$entity->getRealm().' backup archive from backup FTP serveur success']
        });
    }

    $self->_sendRetrieveReport($entity, $xmlContent->{'options'}, $response);
    return $response;
}


sub _isEntityExist {
    my $self = shift;
    my( $entity ) = @_;

    my $ldapFilter = $entity->getLdapFilter();

    my $ldapEntity = $self->_getLdapValues(
        $ldapFilter,
        [] );

    if(!defined($ldapEntity)) {
        return $self->_response( RC_BAD_REQUEST, {
            content => [ 'Fail to contact LDAP server. Contact system administrators' ]
            } );
    }

    if( $#{$ldapEntity} != 0 ) {
        return $self->_response( RC_BAD_REQUEST, {
            content => [ 'Entity '.$entity->getLogin().'@'.$entity->getRealm().' doesn\'t exist in LDAP' ]
            } );
    }

    return undef;
}


sub _isEntityMailboxDefined {
    my $self = shift;
    my( $entity ) = @_;

    my $cyrusConn = $self->_getCyrusConn();
    if( !defined( $cyrusConn ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Can\'t connect to Cyrus service' ]
            } );
    }

    my @mailBox = $cyrusConn->listmailbox( $entity->getLogin().'@'.$entity->getRealm(), $entity->getMailboxPrefix() );
    if( $cyrusConn->error() ) {
        $self->_log( 'Cyrus error: '.$cyrusConn->error(), 0 );
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Cyrus error: '.$cyrusConn->error() ]
            } );
    }

    # Mailbox exist 
    if( $#mailBox != 0 ) {
        return $self->_response( RC_OK, {
            content => [ 'Mailbox '.$entity->getMailboxPrefix().$entity->getLogin().'@'.$entity->getRealm().' doesn\'t exist !' ]
            } );
    }

    return undef;
}


sub _setBackupRoot {
    my $self = shift;
    my( $entity ) = @_;

    $entity->setBackupRoot( $self->{'backupRoot'} );

    if( ! -d $entity->getBackupPath() ) {
        if( $self->_mkdir( $entity->getBackupPath() ) ) {
            return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                content => [ 'Can\'t create \''.$entity->getBackupPath().'\'' ]
                } );
        }
    }

    $self->_log( 'Backup path : '.$entity->getBackupPath(), 4 );
    $self->_log( 'Backup name : '.$entity->getBackupName(), 4 );

    return undef;
}


sub _backupBackupFile {
    my $self = shift;
    my( $entity ) = @_;
    my $language = $self->_getDefaultObmLang( $entity->getRealm() );

    my $backupFullPath = $entity->getBackupPath().'/'.$entity->getBackupName();
    my $backupFullPathBackup = $backupFullPath.'.backup';

    if( -e $backupFullPathBackup ) {
        $self->_log( 'Remove backuped backup file : '.$backupFullPathBackup, 5 );
        if( !unlink( $backupFullPathBackup ) ) {
            if ( $language eq 'fr' ) {
                return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                 content => [ 'Impossible de supprimer le fichier de sauvegarde: '.$backupFullPathBackup ]
                    } );
            }
            else {
                return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                 content => [ 'Can\'t remove backuped backup file : '.$backupFullPathBackup ]
                    } );
            }
        }
    }
    
    if( -e $backupFullPath ) {
        $self->_log( 'Rename '.$backupFullPath.' to '.$backupFullPathBackup, 5 );
        if( !move( $backupFullPath, $backupFullPathBackup ) ) {
            if ( $language eq 'fr' ) {
                return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                    content => [ 'Impossible de renomer '.$backupFullPath.' en '.$backupFullPathBackup ]
                    } );
            }
            else {
                return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                    content => [ 'Can\'t rename '.$backupFullPath.' to '.$backupFullPathBackup ]
                    } );

            }
        }
    }

    return undef;
}


sub _removeBackupBackupFile {
    my $self = shift;
    my( $entity ) = @_;

    my $backupFullPath = $entity->getBackupPath().'/'.$entity->getBackupName();
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
    my( $entity ) = @_;

    my $backupFullPath = $entity->getBackupPath().'/'.$entity->getBackupName();
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


sub _purgeOldBackupFiles {
    my $self = shift;
    my( $entity ) = @_;

    my $backupName = $entity->getBackupNamePrefix();

    opendir( DIR, $entity->getBackupPath() );
    my @userBackup = grep( /^$backupName/, readdir(DIR) );
    close(DIR);

    for( my $i=0; $i<=$#userBackup; $i++ ) {
        $userBackup[$i] =~ /^(.+)$/;
        $userBackup[$i] = $1;

        if( $userBackup[$i] ne $entity->getBackupName() ) {
            $self->_log( 'Remove old backup file '.$userBackup[$i], 5 );
            unlink( $entity->getBackupPath().'/'.$userBackup[$i] );
        }
    }
}


sub _writeArchive {
    my $self = shift;
    my( $entity ) = @_;
    my $language = $self->_getDefaultObmLang( $entity->getRealm() );

    my $backupFullPath = $entity->getBackupFileName();
    my $backupFullPathBackup = $backupFullPath.'.backup';

    if( my $result = $self->_backupBackupFile( $entity ) ) {
        return $result;
    }

    my $cmd = $self->{'tarCmd'}.' --ignore-failed-read -C '.$entity->getTmpBackupPath().' -czhf '.$backupFullPath.' . > /dev/null 2>&1';
    $self->_log( 'Executing '.$cmd, 4 );

    if( my $returnCode = system( $cmd ) ) {
        $returnCode = ($returnCode >> 8) & 0xffff;
        my $content = {
            content => [ 'Can\'t write backup archive '.$backupFullPath.' - Error: '.$returnCode ]
            };

        my $result = $self->_restoreBackupBackupFile( $entity );
        SWITCH: {
            if( $result == 1 ) {
                if ( $language eq 'fr' ) {
                    push( @{$content->{'content'}}, 'Aucun fichier de sauvegarde trouvé' );
                }
                else {
                    push( @{$content->{'content'}}, 'No backuped backup file found' );
                }
                last SWITCH;
            }

            if( $result == 2 ) {
                if ( $language eq 'fr' ) {
                    push( @{$content->{'content'}}, 'Impossible de déplacer le fichier de sauvegarde de '.$backupFullPathBackup.' vers '.$backupFullPath );
                }
                else {
                    push( @{$content->{'content'}}, 'Can\'t move backuped backup file from'.$backupFullPathBackup.' to '.$backupFullPath );
                }
                last SWITCH;
            }

            if ( $language eq 'fr' ) {
                push( @{$content->{'content'}}, 'Fichier de sauvegarde '.$backupFullPathBackup.' restauré vers '.$backupFullPath );
            }
            else {
                push( @{$content->{'content'}}, 'Backuped backup file '.$backupFullPathBackup.' restored to '.$backupFullPath );
            }
            last SWITCH;
        }

        return $self->_response( RC_INTERNAL_SERVER_ERROR, $content );
    }

    return undef;
}


sub _createArchive {
    my $self = shift;
    my( $entity ) = @_;

    $self->_log( 'Beginning backup for '.$entity->getLogin().'@'.$entity->getRealm(), 3 );

    if( my $result = $self->_prepareTmpArchive( $entity ) ) {
        return $result;
    }

    if( $self->_addIcs( $entity ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Can\'t add ICS file to archive' ]
            } );
    }

    if( $self->_addVcard( $entity ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Can\'t add VCARD file to archive' ]
            } );
    }

    if( $self->_addMailbox( $entity ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Can\'t add mailbox file to archive' ]
            } );
    }

    return $self->_writeArchive( $entity );
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
    my( $entity ) = @_;

    if( $self->_rmdir( $entity->getTmpBackupPath() ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Fail to remove '.$entity->getTmpBackupPath() ]
            } );
    }

    return undef;
}


sub _prepareTmpArchive {
    my $self = shift;
    my( $entity ) = @_;

    $entity->setTmpBackupPath( $self->{'tmpDir'}.'/'.$entity->getEntityType().'_-_'.$entity->getRealm().'_-_'.$entity->getLogin() );
    if( -e $entity->getTmpBackupPath() ) {
        $self->_log( $entity->getTmpBackupPath().' already exist !', 1 );
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                    content => [ $entity->getTmpBackupPath().' already exist !' ]
                    } );
    }

    $self->_log( 'Temporary backup path: '.$entity->getTmpBackupPath(), 4 );

    if( $entity->getTmpIcsPath() && $self->_mkdir( $entity->getTmpIcsPath() ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Fail to create '.$entity->getTmpIcsPath() ]
            } );
    }

    if( $entity->getTmpVcardPath() && $self->_mkdir( $entity->getTmpVcardPath() ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Fail to create '.$entity->getTmpVcardPath() ]
            } );
    }

    if( $entity->getTmpMailboxPath() && $self->_mkdir( $entity->getTmpMailboxPath() ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Fail to create '.$entity->getTmpMailboxPath() ]
            } );
    }

    return undef;
}


sub _addIcs {
    my $self = shift;
    my( $entity ) = @_;

    if( !$entity->getIcs() ) {
        return undef;
    }

    if( !open( FIC,
            '>:encoding(UTF-8)',
            $entity->getTmpIcsFile() ) ) {
        $self->_log( $!, 1 );
        return 1;
    }

    $self->_log( 'Writing ICS file '.$entity->getTmpIcsFile(), 5 );
    print FIC $entity->getIcs();
    close(FIC);

    return 0;
}


sub _addVcard {
    my $self = shift;
    my( $entity ) = @_;

    my $addressBooks;
    if( !($addressBooks = $entity->getVcards()) ) {
        return undef;
    }

    while( my($addressBookName, $addressBookContent) = each(%{$addressBooks}) ) {
        if( !open(FIC,
                '>:encoding(UTF-8)',
                $entity->getTmpVcardFile($addressBookName)) ) {
            $self->_log( $!, 1 );
            return 1;
        }
    
        $self->_log( 'Writing VCARD file '.$entity->getTmpVcardFile($addressBookName), 5 );
        print FIC $addressBookContent->{'content'};
        close(FIC);
    }

    return 0;
}


sub _addMailbox {
    my $self = shift;
    my( $entity ) = @_;

    if( my $result = $self->_isEntityMailboxDefined($entity) ) {
        if( $result->isError() ) {
            return $result;
        }

        $self->_log( 'Mailbox \''.$entity->getLogin().'@'.$entity->getRealm().'\' isn\'t backuped', 4 );
        $self->_log( $result->asString(), 5 );
        return undef;
    }

    if( my $result = $self->_getCyrusPartitionPath( $entity ) ) {
        return $result;
    }

    my $mailboxRoot = $entity->getCyrusMailboxRoots();
    for( my $i=0; $i<=$#{$mailboxRoot}; $i++ ) {
        my $linkRoot = $mailboxRoot->[$i]->{'backup'};
        $linkRoot =~ /^(.+)\/[^\/]+$/;
        $linkRoot = $1;

        if( $self->_mkdir( $linkRoot ) ) {
            return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                content => [ 'Can\'t create \''.$linkRoot.'\'' ]
                } );
        }

        if( $self->_mkSymlink( $mailboxRoot->[$i]->{'cyrus'}, $mailboxRoot->[$i]->{'backup'} ) ) {
            return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                content => [ 'Can\'t create mailbox backup link '.$mailboxRoot->[$i]->{'backup'}.' to '.$mailboxRoot->[$i]->{'cyrus'} ]
                } );
        }
    }

    return undef;
}


sub _getCyrusPartitionPath {
    my $self = shift;
    my( $entity ) = @_;

    my $imapPartitionName = $entity->getRealm();
    $imapPartitionName =~ s/[\.-]/_/g;
    $imapPartitionName = 'partition-'.$imapPartitionName;
    $self->_log( 'Cyrus partition name : \''.$imapPartitionName.'\'', 5 );

    if( !open( FIC, $self->{'imapdConfFile'} ) ) {
        $self->_log( 'Can\'t open '.$self->{'imapdConfFile'}, 0 );
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Can\'t open '.$self->{'imapdConfFile'} ]
            } );
    }

    my $cyrusPartitionPath;
    while( !$cyrusPartitionPath && (my $line = <FIC>) ) {
        if( $line =~ /^$imapPartitionName:(\s)*([^\s]+)$/ ) {
            $cyrusPartitionPath = $2;
        }
    }

    close(FIC);

    if( !$cyrusPartitionPath ) {
        $self->_log( 'Can\'t find partition path for cyrus parition', 1 );
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Can\'t find partition path for cyrus parition' ]
            } );
    }

    $entity->setCyrusPartitionPath( $cyrusPartitionPath );

    $self->_log( 'Cyrus partition path : \''.$entity->getCyrusPartitionPath().'\'', 5 );

    return undef;
}


sub _getAvailableBackupFile {
    my $self = shift;
    my( $entity ) = @_;

    my $backupNamePrefix = $entity->getBackupNamePrefix();
    my $realm = $entity->getRealm();
    my @availableBackup = ();
    
    # We now search the backupRoot recursively to be compliant with
    # the new daily/weekly/monthly backups. This still finds the manual backups though...
    if (-e $entity->getBackupRoot()) {
    	$self->_log('Traversing directory ' . $entity->getBackupRoot() . ' to find available backups for '.$entity->getLogin().'@'.$entity->getRealm(), 4 );
    	
    	find( {wanted => sub {
    		my $fname = "$File::Find::name";
		    
		    if ($fname =~ /$realm\/$backupNamePrefix/) {
		    	push @availableBackup, basename($_);
		    }
    	}, no_chdir => 1}, $entity->getBackupRoot());
    } else {
    	return $self->_response(RC_INTERNAL_SERVER_ERROR, {
        content => [ 'Can\'t open backup root ' . $entity->getBackupRoot() ]
        });
    }

    return \@availableBackup;
}


sub _restoreFromArchive {
    my $self = shift;
    my( $entity, $restoreData ) = @_;

    if( !defined($restoreData) || ($restoreData !~ /^(mailbox|contact|calendar)$/) ) {
        $restoreData = 'all';
    }

    if( my $result = $self->_prepareTmpArchive( $entity ) ) {
        return $result;
    }

    if( my $result = $self->_getFilesFromArchive( $entity, $restoreData ) ) {
        return $result;
    }

    my $response = $self->_response(RC_OK, $entity->getEntityContent());
    $response->setExtraContent({
        content => ['Restoring '.$entity->getEntityType().' '.$entity->getLogin().'@'.$entity->getRealm().' '.$restoreData.' data successfully']
    });

    return $response;
}


sub _getFilesFromArchive {
    my $self = shift;
    my( $entity, $restoreData ) = @_;
    my $entityArchive = $entity->getBackupFileName();
    
    # Before failing if the backlup doesn't exist, test if this is
    # an automated backup by trying to locate it in the directory tree
    if( !(-f $entityArchive) || !(-r $entityArchive) ) {
        my $automaticBackup;
        my $backupName = $entity->getBackupName();
        
        find( {wanted => sub {
    		my $fname = basename($_);
		    
		    if ($fname eq $backupName) {
		    	$automaticBackup = $File::Find::name;
		    	$File::Find::prune = 1;
		    }
    	}, no_chdir => 1}, $entity->getBackupRoot());
        
        if ($automaticBackup) {
        	$entity->setBackupFileName($automaticBackup);
        } else {
	        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
	            content => [ 'Entity archive \''.$entityArchive.'\' doesn\'t exist or isn\'t readable' ]
	            } );
        }
    }

    $self->_log( 'Beginning \''.$restoreData.'\' restore for '.$entity->getLogin().'@'.$entity->getRealm() . ' using backup: ' . $entity->getBackupFileName(), 3 );

    if( my $result = $self->_getIcsVcardFromArchive( $entity, $restoreData ) ) {
        return $result;
    }

    if( my $result = $self->_restoreMailbox( $entity, $restoreData ) ) {
        return $result;
    }

    $self->_log( 'Restore \''.$restoreData.'\' for '.$entity->getLogin().'@'.$entity->getRealm().' successfully', 3 );
    return undef;
}


sub _getIcsVcardFromArchive {
    my $self = shift;
    my( $entity, $restoreData ) = @_;

    my @extractedFiles;
    if( $restoreData =~ /^(all|calendar)$/ ) {
        if( defined($entity->getArchiveIcsPath()) ) {
            push( @extractedFiles, $entity->getArchiveIcsPath() );
        }
    }

    if( $restoreData =~ /^(all|contact)$/ ) {
        if( defined($entity->getArchiveVcardPath()) ) {
            push( @extractedFiles, $entity->getArchiveVcardPath() );
        }
    }

    if( $#extractedFiles < 0 ) {
        return undef;
    }

    my $cmd = $self->{'tarCmd'}.' -C '.$entity->getTmpBackupPath().' -xzf '.$entity->getBackupFileName().' '.join( ' ', @extractedFiles).' > /dev/null 2>&1';
    $self->_log( 'Executing '.$cmd, 4 );

    if( my $returnCode = system( $cmd ) ) {
        $returnCode = ($returnCode >> 8) & 0xffff;
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                    content => [ 'Can\'t extract files from backup archive '.$entity->getTmpBackupPath().' - Error: '.$returnCode ]
                    } );
    }

    if( $restoreData =~ /^(all|calendar)$/ ) {
        if( (-e $entity->getTmpIcsFile()) && !(-f $entity->getTmpIcsFile()) ) {
            return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                        content => [ 'Can\'t load extracted ICS file from '.$entity->getTmpIcsFile() ]
                        } );
        }

        open( FIC, $entity->getTmpIcsFile() );
        my @ics = <FIC>;
        close(FIC);

        $entity->setIcs( join( '', @ics ) );
    }

    if( $restoreData =~ /^(all|contact)$/ ) {
        if(!(-d $entity->getTmpVcardPath())) {
            return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                        content => [ 'Can\'t open VCARD directory '.$entity->getTmpVcardPath() ]
                        } );
        }

        my $dh;
        opendir($dh, $entity->getTmpVcardPath());
        my @files = grep { /\.vcf$/ && -f $entity->getTmpVcardPath().'/'.$_ } readdir($dh);
        closedir($dh);

        for(my $i=0; $i<=$#files; $i++) {
            my $addressBookName = $files[$i];
            $addressBookName =~ s/\.vcf$//;

            open( FIC, $entity->getTmpVcardFile($addressBookName));
            my @vcard = <FIC>;
            close(FIC);

            $entity->setVcard($addressBookName, join('', @vcard));
        }
    }

    return undef;
}


sub _restoreMailbox {
    my $self = shift;
    my( $entity, $restoreData ) = @_;

    if( $restoreData !~ /^(all|mailbox)$/ ) {
        return undef;
    }

    if( my $result = $self->_getCyrusPartitionPath( $entity ) ) {
        return $result;
    }

    if( my $result = $self->_createRestoreMailboxFolder( $entity ) ) {
        return $result;
    }

    return undef;
}


sub _createRestoreMailboxFolder {
    my $self = shift;
    my( $entity) = @_;

    if( my $result = $self->_isEntityMailboxDefined($entity) ) {
        if( $result->isError() ) {
            return $result;
        }

        $self->_log( 'Mailbox \''.$entity->getLogin().'@'.$entity->getRealm().'\' isn\'t restored', 4 );
        $self->_log( $result->asString(), 5 );
        return undef;
    }

    my $cyrusConn = $self->_getCyrusConn();
    if( !defined( $cyrusConn ) ) {
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Can\'t connect to Cyrus service' ]
            } );
    }

    my @mailBox = $cyrusConn->listmailbox( $entity->getMailboxRestoreFolder(), $entity->getMailboxPrefix() );
    if( $cyrusConn->error() ) {
        $self->_log( 'Cyrus error: '.$cyrusConn->error(), 0 );
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
            content => [ 'Cyrus error: '.$cyrusConn->error() ]
            } );
    }
    my $contentUid = (stat($entity->getMailboxRestorePath()))[4];
    my $contentGid = (stat($entity->getMailboxRestorePath()))[5];

    if (@mailBox) {
        if ($self->_backupCurrentMailbox($entity)) {
            $self->_log("Failed to move current mailbox: $!", 0);
            return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                    content => [ "$@" ]
                } );
        }
    }
    else {
        # Maybe the mailbox was deleted in Cyrus?
        $self->_log( 'Create mailbox: '.$entity->getMailboxPrefix().$entity->getMailboxRestoreFolder(), 4 );

        $cyrusConn->create( $entity->getMailboxPrefix().$entity->getMailboxRestoreFolder() );
        if( $cyrusConn->error() ) {
            $self->_log( 'Fail to create mailbox \''.$entity->getMailboxPrefix().$entity->getMailboxRestoreFolder().'\': '.$cyrusConn->error() );
            return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                content => [ 'Fail to create mailbox \''.$entity->getMailboxPrefix().$entity->getMailboxRestoreFolder().'\': '.$cyrusConn->error() ]
                } );
        }
    }

    # Restore mailbox content
    mkdir $entity->getMailboxRestorePath();
    my $cmd = $self->{'tarCmd'}.' --strip '.$entity->getRestoreMailboxArchiveStrip().' -C '.$entity->getMailboxRestorePath().' -xzf '.$entity->getBackupFileName().' '.$entity->getArchiveMailboxPath().' > /dev/null 2>&1';
    $self->_log( 'Executing '.$cmd, 4 );

    if( my $returnCode = system( $cmd ) ) {
        $returnCode = ($returnCode >> 8) & 0xffff;
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                    content => [ 'Can\'t extract files from backup archive '.$entity->getTmpBackupPath().' - Error: '.$returnCode ]
                    } );
    }

    my $mailboxRestorePath = $entity->getMailboxRestorePath();

    find( {
            wanted => sub {
                my $path = $_;
                # Untaint $path
                if ($path =~ /(.+)/) {
                    chown( $contentUid, $contentGid, $1 );
                    my $filePermissions = -f $1 ? 0600 : 0700;
                    chmod($filePermissions, $1);
                }
            },
            no_chdir  => 1
        }, $entity->getMailboxRestorePath() );
    my $reconstructMailbox = $entity->getMailboxPrefix().$entity->getLogin();
    $reconstructMailbox =~ s/^(.+)@/$1\*@/;
    $cmd = 'su -l -c \'PATH="'.RECONSTRUCT_PATH.'" '.RECONSTRUCT_CMD.' -r -f -x '.$reconstructMailbox.'\' cyrus -s /bin/sh';
    $self->_log( 'Executing '.$cmd, 4 );

    if( my $returnCode = system($cmd) ) {
        $returnCode = ($returnCode >> 8) & 0xffff;
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                    content => [ 'Can\'t reconstruct restore mailbox \''.$reconstructMailbox.'\' - Error: '.$returnCode ]
                    } );
    }

    $cmd = 'su -l -c \'PATH="'.QUOTA_PATH.'" '.QUOTA_CMD.' -d '.$entity->getRealm().' '.$reconstructMailbox.'\' cyrus -s /bin/sh';
    $self->_log( 'Executing '.$cmd, 4 );

    if( my $returnCode = system($cmd) ) {
        $returnCode = ($returnCode >> 8) & 0xffff;
        return $self->_response( RC_INTERNAL_SERVER_ERROR, {
                    content => [ 'Can\'t refresh quota for '.$entity->getLogin().'@'.$entity->getRealm().' - Error: '.$returnCode ]
                    } );
    }

 
    return undef;
}

sub _backupCurrentMailbox {
    my ($self, $entity) = @_;

    my $currentMailboxDir = $entity->getMailboxRestorePath();
    my $backupCurrentMailboxDir = $entity->getTmpBackupCurrentMailboxPath();
    $self->_log("Backing up $currentMailboxDir to $backupCurrentMailboxDir", 2);
    my $moveSuccessful;
    # OBMFULL-4521 : We cannot use the Perl move function here
    # because move from Perl 5.8.8 does not work well when moving directory
    # between different partitions or devices.
    my $cmd = 'mv "'.$currentMailboxDir.'" "'.$backupCurrentMailboxDir.'"';
    if (-e $currentMailboxDir) {
        $self->_log( 'Executing '.$cmd, 4 );
        $moveSuccessful = system($cmd);
    }
    else {
        $moveSuccessful = 0;
    }
    return $moveSuccessful;

}


sub _sendMailBackupReport {
    my $self = shift;
    my( $entity, $options, $response ) = @_;

    my $language = $self->_getDefaultObmLang( $entity->getRealm() );

    my $mail = {};

    my $title = 'Backup report';
    my $entityType = $entity->getEntityType();
    my $success = eval {
        if($response->isError()) {
            return 'FAIL';
        }

        return 'SUCCESS';
    };

    SWITCH: {
        if($language eq 'fr') {
            $title = 'Rapport de sauvegarde';
            $entityType = eval {
                my $type = $entity->getEntityType();

                if($type eq 'user') {
                    return 'utilisateur';
                }elsif($type eq 'mailshare') {
                    return 'partage de messagerie';
                }

                return 'type inconnu';
            };
            $success = eval {
                if($response->isError()) {
                    return 'Échec';
                }

                return 'Succés';
            };

            last SWITCH;
        }
    }

    $mail->{'subject'} = $title.' - '.$entityType.' '
        .$entity->getLogin().'@'.$entity->getRealm().': '.$success;
    $mail->{'content'} = [];
    if( my $content = $response->getContentValue('content') ) {
        push( @{$mail->{'content'}}, @{$content} );

        $content = $response->getContentValue('pushFtp');
        if(ref($content) eq 'HASH') {
            if($content->{'success'} eq 'true') {
                if ( $language eq 'fr') {
                    push(@{$mail->{'content'}}, 'Transfert FTP réussi - '.$content->{'content'});
                }
                else {
                    push(@{$mail->{'content'}}, 'FTP upload: success - '.$content->{'content'});
                }
            }
            else {
                if ( $language eq 'fr') {
                    push(@{$mail->{'content'}}, 'Transfert FTP échoué - '.$content->{'content'});
                }
                else {
                    push(@{$mail->{'content'}}, 'FTP upload: fail - '.$content->{'content'});
                }
            }
        }
    }

    return $self->_sendMailReport($mail, $entity, $options);
}


sub _sendMailRestoreReport {
    my $self = shift;
    my( $entity, $options, $response ) = @_;

    my $language = $self->_getDefaultObmLang( $entity->getRealm() );

    my $mail = {};

    my $title = 'Restore report';
    my $entityType = $entity->getEntityType();
    my $success = eval {
        if($response->isError()) {
            return 'FAIL';
        }

        return 'SUCCESS';
    };

    SWITCH: {
        if($language eq 'fr') {
            $title = 'Rapport de restauration';
            $entityType = eval {
                my $type = $entity->getEntityType();

                if($type eq 'user') {
                    return 'utilisateur';
                }elsif($type eq 'mailshare') {
                    return 'partage de messagerie';
                }

                return 'type inconnu';
            };
            $success = eval {
                if($response->isError()) {
                    return 'Échec';
                }

                return 'Succés';
            };

            last SWITCH;
        }
    }

    $mail->{'subject'} = $title.' - '.$entityType.' '
        .$entity->getLogin().'@'.$entity->getRealm().': '.$success;
    $mail->{'content'} = [];
    if( my $content = $response->getContentValue('content') ) {
        push( @{$mail->{'content'}}, @{$content} );
    }

    return $self->_sendMailReport($mail, $entity, $options);
}


sub _sendRetrieveReport {
    my $self = shift;
    my( $entity, $options, $response ) = @_;

    my $language = $self->_getDefaultObmLang($entity->getRealm());

    my $mail = {};

    my $title = 'Retrieve backup archives from FTP backup server report';
    my $entityType = $entity->getEntityType();
    my $success = eval {
        if($response->isError()) {
            return 'FAIL';
        }

        return 'SUCCESS';
    };

    SWITCH: {
        if($language eq 'fr') {
            $title = 'Rapport de récupération des archives depuis le serveur FTP';
            $entityType = eval {
                my $type = $entity->getEntityType();

                if($type eq 'user') {
                    return 'utilisateur';
                }elsif($type eq 'mailshare') {
                    return 'partage de messagerie';
                }

                return 'type inconnu';
            };
            $success = eval {
                if($response->isError()) {
                    return 'Échec';
                }

                return 'Succés';
            };

            last SWITCH;
        }
    }

    $mail->{'subject'} = $title.' - '.$entityType.' '
        .$entity->getLogin().'@'.$entity->getRealm().': '.$success;
    $mail->{'content'} = [];
    if( my $content = $response->getContentValue('content') ) {
        push( @{$mail->{'content'}}, @{$content} );

        $content = $response->getContentValue('pushFtp');
        if(ref($content) eq 'HASH') {
            if($content->{'success'} eq 'true') {
                if ( $language eq 'fr') {
                    push(@{$mail->{'content'}}, 'Téléchargement FTP réussi - '."\n".$content->{'content'});
                }
                else {
                    push(@{$mail->{'content'}}, 'FTP download: success - '."\n".$content->{'content'});
                }
            }
            else {
                if ( $language eq 'fr') {
                    push(@{$mail->{'content'}}, 'Téléchargement FTP échoué - '.$content->{'content'});
                }
                else {
                    push(@{$mail->{'content'}}, 'FTP download: fail - '.$content->{'content'});
                }
            }
        }
    }

    return $self->_sendMailReport($mail, $entity, $options);
}


sub _sendMailReport {
    my $self = shift;
    my( $mailContent, $entity, $options ) = @_;

    if( !defined($mailContent) || (ref($mailContent) ne 'HASH') ) {
        $self->_log( 'Empty or invalid mail report - no mail report send', 2 );
        return 1;
    }

    my $mailReportRecipient = MAIL_REPORT_RECIPIENT.'@'.$entity->getRealm();
    my $mailReportCcRecipient = [];

    if( defined($options) && (ref($options) eq 'HASH') ) {
        if( ref($options->{'report'}) eq 'HASH' ) {
            if( defined($options->{'report'}->{'sendMail'}) && (lc($options->{'report'}->{'sendMail'}) eq 'false') ) {
                $self->_log( 'No mail report', 4 );
                return 1;
            }
    
            $self->_log( 'Sending report to: '.$mailReportRecipient, 5 );
            if( defined($options->{'report'}->{'email'}) && (ref($options->{'report'}->{'email'}) eq 'ARRAY') ) {
                for( my $i=0; $i<=$#{$options->{'report'}->{'email'}}; $i++ ) {
                    if( $options->{'report'}->{'email'}->[$i] =~ /^($REGEX_EMAIL)$/ ) {
                        $self->_log( 'Sending report copy to: '.$1, 5 );
                        push( @{$mailReportCcRecipient}, $1 );
                    }
                }
            }
        }
    }

    my $msg = MIME::Lite->new(
        From    => $mailReportRecipient,
        To      => $mailReportRecipient,
        Subject => '=?UTF-8?B?'.&MIME::Base64::encode_base64($mailContent->{'subject'}, '').'?=',
        Type    => 'multipart/related'
        );
    $msg->attr( 'Content-Type.charset' => 'UTF-8' );

    if($#{$mailReportCcRecipient} >= 0) {
        $msg->add( 'cc', $mailReportCcRecipient )
    }

    $msg->attach(
        Type    => 'text/plain; charset=UTF-8',
        Data    => join( "\n", @{$mailContent->{'content'}} )
        );
    
    if( $MIME::Lite::VERSION >= 3.025 ) {
        eval {
            local $SIG{__DIE__} = sub {};
        
            $self->_log( $msg->as_string(), 5 );
            $msg->send( 'smtp', 'localhost' );
        };
    
        if( !$msg->last_send_successful() ) {
            $self->_log( 'Sending mail report fail', 1 );
            return 1;
        }
    }else {
        my $mailSend = eval {
            local $SIG{__DIE__} = sub {};

            $self->_log( $msg->as_string(), 5 );
            return $msg->send( 'smtp', 'localhost' );
        };

        if(!$mailSend) {
            $self->_log( 'Sending mail report fail', 1 );
            return 1;
        }
    }
    
    $self->_log( 'Sending mail report success', 3 );
    return 0;
}


sub _pushFtpBackup {
    my $self = shift;
    my ($entity, $options, $response) = @_;

    my $language = $self->_getDefaultObmLang( $entity->getRealm() );

    if(lc($options->{'ftp'}->{'push'}) eq 'false') {
        $self->_log('No push FTP asked', 3);
        $response->setExtraContent({
            pushFtp => {
                content => 'No push FTP asked',
                success => 'true'
            }
        });
        return 0;
    }

    # Getting FTP backup server name for entity's OBM domain
    my $ftpHostName = $self->_getFtpBackupHost($entity, $response);
    if(!defined($ftpHostName)) {
        return 1;
    }

    # Getting FTP backup host informations
    my $ftpConn = $self->_getFtpBackupConn($entity, $response, $ftpHostName);
    if(!defined($ftpConn)) {
        return 1;
    }

    $self->_log('Uploading backup file \''.$entity->getBackupFileName().'\' to backup FTP server \''.$ftpHostName.'\'', 3);
    my $error = $ftpConn->put($entity->getBackupFileName(), $entity->getBackupName());
    if(!$error) {
        my $errorMsg = '' ;
        if ( $language eq 'fr' ) {
            $errorMsg = 'Transfert du fichier de sauvegarde \''.$entity->getBackupFileName().'\' vers le serveur FTP \''.$ftpHostName.'\' echoué: '.$ftpConn->message();
        }
        else {  
            $errorMsg = 'Uploading backup file \''.$entity->getBackupFileName().'\' to backup FTP server \''.$ftpHostName.'\' fail: '.$ftpConn->message();
        }
        $self->_log($errorMsg, 1);
        $response->setExtraContent({
            pushFtp => {
                content => $errorMsg,
                success => 'false'
            }
        });
        $ftpConn->quit();
        return 1;
    }

    $ftpConn->quit();

    if ( $language eq 'fr' ) {
        $response->setExtraContent({
            pushFtp => {
                content => 'Transfert du fichier de sauvegarde \''.$entity->getBackupFileName().'\' vers le serveur FTP \''.$ftpHostName.'\' réussi',
                success => 'true'
            }
        });
    }
    else {
        $response->setExtraContent({
            pushFtp => {
                content => 'Uploading backup file \''.$entity->getBackupFileName().'\' to backup FTP server \''.$ftpHostName.'\' success',
                success => 'true'
            }
        });
    }

    return 0;
}


sub _getFtpBackupHost {
    my $self = shift;
    my($entity, $response) = @_;
    # get delegation value of entity in ldap
    my $ldapEntityEntity = $self->_getLdapValues($entity->getLdapFilter(),['delegation']) ;
    my $entityDelegation = $ldapEntityEntity->[0]->get_value('delegation');

    my $ldapEntity = undef ;
    if ( defined($entityDelegation))  {
        $self->_log("Search for FTP server in $entityDelegation",3 ) ;
        $ldapEntity = $self->_getLdapValues(
            '(&(objectClass=obmHost)(obmDomain='.$entity->getRealm().')
                (delegation='.$entityDelegation.'))',['cn'] ) ;
    # Si on a rien trouve on prend les hotes ftp puis on compare leur delegation a
    #  celle de l'entity. On prend la plus proche
        if ( $#{$ldapEntity} < 0 ) {
              $self->_log('They are not FTP backup with entity\'s delegation.Search for parent delegation',3);
          my $ldapEntities = $self->_getLdapValues(
            '(&(objectClass=obmHost)(ftpLogin=*)(obmDomain='.$entity->getRealm().'))'
                 ,['cn','delegation'] );

            my $best_deleg = _searchParentDelegation($ldapEntities,$entityDelegation) ;
            if ( defined($best_deleg) ) {
                $self->_log("The best delegation candidate is $best_deleg",1 ) ;
                $ldapEntity = $self->_getLdapValues(
                    '(&(objectClass=obmHost)(obmDomain='.$entity->getRealm().')
                   (delegation='.$best_deleg.'))',['cn'] ) ;
                $self->_log("The FTP server is".$ldapEntity->[0]->get_value("cn")."for $entityDelegation",1 ) ;
            }
        }
    }
    # if entityDelegation aren't defined search ftp of obm domain 
    # or if entityDelegation are defined but no ftp found
    if ( !defined($entityDelegation) || $#{$ldapEntity} < 0 ) {
        $ldapEntity = $self->_getLdapValues(
            '(&(objectClass=obmBackup)(obmDomain='.$entity->getRealm().'))',
            ['ftpHost'] );
    }

    if(!defined($ldapEntity)) {
        $self->_log('Fail to contact LDAP server. Contact system administrators', 1);
        $response->setExtraContent({
            pushFtp => {
                content => 'Fail to contact LDAP server. Contact system administrators',
                success => 'false'
            }
        });
        return undef;
    }

    if($#{$ldapEntity} < 0) {
        $self->_log('No backup FTP server linked to OBM domain \''.$entity->getRealm().'\'', 3);
        $response->setExtraContent({
            pushFtp => {
                content => 'No backup FTP server linked to OBM domain \''.$entity->getRealm().'\'',
                success => 'false'
            }
        });
        return undef;
    }elsif($#{$ldapEntity} > 0) {
        $self->_log('More than one FTP server linked to OBM domaine \''.$entity->getRealm().'\'', 1);
        $response->setExtraContent({
            pushFtp => {
                content => 'More than one FTP server linked to OBM domaine \''.$entity->getRealm().'\'',
                success => 'false'
            }
        });
        return undef;
    }

    my $ftpHostName = undef ;
    if ( !defined($entityDelegation) ) {
        $ftpHostName = $ldapEntity->[0]->get_value('ftpHost');
    }
    else {
        $ftpHostName = $ldapEntity->[0]->get_value('cn');
    }

    if(!$ftpHostName) {
        $self->_log('No backup FTP server linked to OBM domaine \''.$entity->getRealm().'\'', 3);
        $response->setExtraContent({
            pushFtp => {
                content => 'No backup FTP server linked to OBM domaine \''.$entity->getRealm().'\'',
                success => 'false'
            }
        });
        return undef;
    }

    return $ftpHostName;
}


sub _getFtpBackupConn {
    my $self = shift;
    my($entity, $response, $obmBackupFtpHostname) = @_;
    
    my $ldapEntity = $self->_getLdapValues(
        '(&(objectClass=obmHost)(cn='.$obmBackupFtpHostname.')(|(obmDomain='.$entity->getRealm().')(obmDomain='.OBM_GLOBAL_DOMAIN_NAME.')))',
        ['ipHostNumber', 'ftpLogin', 'ftpPassword', 'ftpRoot']
        );

    if(!defined($ldapEntity)) {
        $self->_log('Fail to contact LDAP server. Contact system administrators', 1);
        $response->setExtraContent({
            pushFtp => {
                content => 'Fail to contact LDAP server. Contact system administrators',
                success => 'false'
            }
        });
        return undef;
    }

    if($#{$ldapEntity} < 0) {
        $self->_log('FTP backup server \''.$obmBackupFtpHostname.'\' not found in LDAP', 1);
        $response->setExtraContent({
            pushFtp => {
                content => 'FTP backup server \''.$obmBackupFtpHostname.'\' not found in LDAP',
                success => 'false'
            }
        });
        return undef;
    }elsif($#{$ldapEntity} > 0) {
        $self->_log('More than one host named \''.$obmBackupFtpHostname.'\' found in LDAP', 1);
        $response->setExtraContent({
            pushFtp => {
                content => 'More than one host named \''.$obmBackupFtpHostname.'\' found in LDAP',
                success => 'false'
            }
        });
        return undef;
    }

    my $ftpHostIp = $ldapEntity->[0]->get_value('ipHostNumber');
    if(!defined($ftpHostIp) || ref($ftpHostIp) || ($ftpHostIp !~ /$REGEX_IP/)) {
        $self->_log('Invalid IP address \''.$ftpHostIp.'\' for FTP backup server named \''.$obmBackupFtpHostname.'\'', 1);
        $response->setExtraContent({
            pushFtp => {
                content => 'Invalid IP address for FTP backup server named \''.$obmBackupFtpHostname.'\'',
                success => 'false'
            }
        });
        return undef;
    }

    my $ftpHostLogin = $ldapEntity->[0]->get_value('ftpLogin');
    if(!defined($ftpHostLogin) || ref($ftpHostLogin) || ($ftpHostLogin !~ /$REGEX_LOGIN/)) {
        $self->_log('Invalid login \''.$ftpHostLogin.'\' for FTP backup server named \''.$obmBackupFtpHostname.'\'', 1);
        $response->setExtraContent({
            pushFtp => {
                content => 'Invalid login for FTP backup server named \''.$obmBackupFtpHostname.'\'',
                success => 'false'
            }
        });
        return undef;
    }

    my $ftpHostPassword = $ldapEntity->[0]->get_value('ftpPassword');
    if(ref($ftpHostPassword)) {
        $ftpHostPassword = undef;
    }

    my $ftpHostRoot = $ldapEntity->[0]->get_value('ftpRoot');

    return $self->_getFtpConn($response, $obmBackupFtpHostname, $ftpHostIp, $ftpHostLogin, $ftpHostPassword, $ftpHostRoot);
}


sub _getFtpConn {
    my $self = shift;
    my($response, $obmBackupFtpHostname, $ftpHostIp, $ftpHostLogin, $ftpHostPassword, $ftpHostRoot) = @_;

    my $ftpConn = Net::FTP->new( $ftpHostIp, Timeout => BACKUP_FTP_TIMEOUT );
    if( !defined($ftpConn) ) {
        $self->_log('Fail to contact backup FTP server \''.$ftpHostIp.'\'', 1);
        $response->setExtraContent({
            pushFtp => {
                content => 'Fail to contact backup FTP server \''.$obmBackupFtpHostname.'\'',
                success => 'false'
            }
        });
        return undef;
    }

    my $error = $ftpConn->login($ftpHostLogin, $ftpHostPassword);
    if(!$error) {
        my $errorMsg = 'Backup FTP server \''.$obmBackupFtpHostname.'\' fail: '.$ftpConn->message();
        $self->_log($errorMsg, 1);
        $response->setExtraContent({
            pushFtp => {
                content => $errorMsg,
                success => 'false'
            }
        });
        return undef;
    }

    $error = $ftpConn->binary();
    if(!$error) {
        my $errorMsg = 'Backup FTP server \''.$obmBackupFtpHostname.'\' setup fail: '.$ftpConn->message();
        $self->_log($errorMsg, 1);
        $response->setExtraContent({
            pushFtp => {
                content => $errorMsg,
                success => 'false'
            }
        });
        return undef;
    }

    if($ftpHostRoot) {
        $error = $ftpConn->cwd($ftpHostRoot);
        if(!$error) {
            my $errorMsg = 'Backup FTP server \''.$obmBackupFtpHostname.'\' setup fail: '.$ftpConn->message();
            $self->_log($errorMsg, 1);
            $response->setExtraContent({
                pushFtp => {
                    content => $errorMsg,
                    success => 'false'
                }
            });
            return undef;
        }
    }

    return $ftpConn;
}


sub _getFtpBackup {
    my $self = shift;
    my($entity, $response) = @_;
    my $language = $self->_getDefaultObmLang( $entity->getRealm() );

    # Getting FTP backup server name for entity's OBM domain
    my $ftpHostName = $self->_getFtpBackupHost($entity, $response);
    if(!defined($ftpHostName)) {
        return 1;
    }

    # Getting FTP backup host informations
    my $ftpConn = $self->_getFtpBackupConn($entity, $response, $ftpHostName);
    if(!defined($ftpConn)) {
        return 1;
    }

    $self->_log('Downloading '.$entity->getLogin().'@'.$entity->getRealm().' backup file from backup FTP server \''.$ftpHostName.'\'', 3);
    my $fileList = $ftpConn->ls( $entity->getBackupNamePrefix().'*' );
    if( !$fileList || (ref($fileList) ne 'ARRAY') ) {
    my $errorMsg;
        if ( $language eq 'fr' ) {
            $errorMsg = 'Téléchargement du fichier de sauvegarde de '.$entity->getLogin().'@'.$entity->getRealm().' depuis le serveur FTP \''.$ftpHostName.'\' echoué: '.$ftpConn->message();
        }
        else {
            $errorMsg = 'Downloading '.$entity->getLogin().'@'.$entity->getRealm().' backup file from backup FTP server \''.$ftpHostName.'\' fail: '.$ftpConn->message();
        }
        $self->_log($errorMsg, 1);
        $response->setExtraContent({
            pushFtp => {
                content => $errorMsg,
                success => 'false'
            }
        });
        $ftpConn->quit();
        return 1;
    }

    my $nbFailed = 0;
    my @downloadMsgs;
    for( my $i=0; $i<=$#{$fileList}; $i++ ) {
        if(-f $entity->getBackupPath().'/'.$fileList->[$i]) {
            my $errorMsg;
            if ( $language eq 'fr' ) {
              $errorMsg = 'La sauvegarde \''.$fileList->[$i].'\' existe déjà, téléchargement non effectué.';
            } else {
              $errorMsg = 'Backup \''.$fileList->[$i].'\' already exist, skip download';
            }
            push(@downloadMsgs, $errorMsg);
            next;
        }

        # Retrieve backup archive
        my $error = $ftpConn->get( $fileList->[$i], $entity->getBackupPath().'/'.$fileList->[$i] );

        my $errorMsg;
        if(!$error) {
            if ( $language eq 'fr' ) {
              $errorMsg = 'Le téléchargement \''.$fileList->[$i].'\' depuis le serveur de sauvegarde FTP \''.$ftpHostName.'\' a échoué : '.$ftpConn->message();
            } else {
              $errorMsg = 'Download \''.$fileList->[$i].'\' from backup FTP server \''.$ftpHostName.'\' fail: '.$ftpConn->message();
            }
            $self->_log($errorMsg, 1);

            $nbFailed++;
        } else {
            if ( $language eq 'fr' ) {
              $errorMsg = 'Le téléchargement \''.$fileList->[$i].'\' depuis le serveur de sauvegarde FTP \''.$ftpHostName.'\' a réussi.';
            } else {
              $errorMsg = 'Download \''.$fileList->[$i].'\' from backup FTP server \''.$ftpHostName.'\' success';
            }
          $self->_log($errorMsg, 4);
        }

        push(@downloadMsgs, $errorMsg);
    }

    $ftpConn->quit();
    my $success = 'true';
    if($nbFailed == $#downloadMsgs+1) {
        $success = 'false';
    }

    $response->setExtraContent({
        pushFtp => {
            content => join("\n", @downloadMsgs),
            success => $success
        }
    });

    if($success eq 'false') {
        return 1;
    }

    return 0;
}

sub _searchParentDelegation {
   my $ldapEntities = shift ;
   my $entityDelegation = shift ;
   my $best_deleg ;
   my @deleg ;
   foreach my $ldapEntity (@{$ldapEntities}) {
       push(@deleg,$ldapEntity->get_value('delegation')) ;
   }
   my @strEntityDelegation = split("",$entityDelegation) ;
   my $last = 0 ;
   foreach my $char ( @strEntityDelegation ) {
        chop($entityDelegation) ;
        foreach my $ftpDelegation ( @deleg ) {
                if ( $entityDelegation eq $ftpDelegation) {
                        $best_deleg = $ftpDelegation ;
                        $last = 1 ;
                        last ;
                }
        }
        # On finit la boucle uniquement si nous avons trouvé une correspondance
        if ( $last == 1 ) {
                last ;
        }
   }
   if ( $best_deleg ne "" ) {
        return $best_deleg ;
   }
   else {
        return undef;
   }
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
