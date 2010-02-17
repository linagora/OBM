package ObmSatellite::Modules::cyrusPartition;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;

use ObmSatellite::Modules::abstract;
@ISA = qw(ObmSatellite::Modules::abstract);
use strict;

use HTTP::Status;

use constant IMAPD_CONF_FILE => '/etc/imapd.conf';
use constant CYRUS_STARTUP_SCRIPT => '/etc/init.d/cyrus2.3';
use constant IMAPD_PARTITION_ROOT => '/var/spool/cyrus';


sub _initHook {
    my $self = shift;

    $self->{'uri'} = [ '/cyruspartition' ];
    $self->{'neededServices'} = [ 'LDAP' ];

    $self->{'imapdConfFile'} = IMAPD_CONF_FILE;
    $self->{'cyrusStartupScript'} = CYRUS_STARTUP_SCRIPT;
    $self->{'imapdPartitionRoot'} = IMAPD_PARTITION_ROOT;


    my @params = ( 'imapdConfFile', 'cyrusStartupScript', 'imapdPartitionRoot', 'ldapRoot' );
    my $confFileParams = $self->_loadConfFile( \@params );

    $self->_log( $self->getModuleName().' module configuration :', 4 );
    for( my $i=0; $i<=$#params; $i++ ) {
        $self->{$params[$i]} = $confFileParams->{$params[$i]} if defined($confFileParams->{$params[$i]});
        $self->_log( $params[$i].' : '.$self->{$params[$i]}, 4 ) if defined($self->{$params[$i]});
    }

    return 1;
}


sub _postMethod {
    my $self = shift;
    my( $requestUri, $requestBody ) = @_;
    my %datas;

    $datas{'requestUri'} = $requestUri;

    if( $requestUri !~ /^\/cyruspartition\/([^\/]+)(.*)$/ ) {
        my $return = $self->_response( RC_BAD_REQUEST, { content => [ 'Invalid URI '.$requestUri ] } );
        $return->[1]->{'help'} = [ $self->getModuleName().' URI must be : /cyruspartition/<entity>' ];
        return $return;
    }

    $datas{'entity'} = $1;

    SWITCH: {
        if( $datas{'entity'} eq 'host' ) {
            return $self->_hostEntity( \%datas );
        }
    }

    return $self->_response( RC_NOT_FOUND, { content => [ 'Unknow entity \''.$datas{'entity'}.'\'' ] } );
}


sub _hostEntity {
    my $self = shift;
    my( $datas ) = @_;

    my $regexp = '^\/cyruspartition\/'.$datas->{'entity'}.'\/([^\/]+)(.*)$';
    if( $datas->{'requestUri'} !~ /$regexp/ ) {
        my $return = $self->_response( RC_BAD_REQUEST, {
            content => [ 'Invalid URI '.$datas->{'requestUri'} ],
            help => [ $self->getModuleName().' URI must be : /cyruspartition/'.$datas->{'entity'}.'/<operation>'."\n".'[add|del]' ]
            } );
        return $return;
    }

    $datas->{'operation'} = $1;

    SWITCH: {
        if( $datas->{'operation'} eq 'add' ) {
            return $self->_addPartition( $datas );
        }

        if( $datas->{'operation'} eq 'del' ) {
            return $self->_delPartition( $datas );
        }
    }

    return $self->_reponse( RC_NOT_FOUND, { content => [ 'Unknow operation \''.$datas->{'operation'}.'\'' ] } );
}


sub _addPartition {
    my $self = shift;
    my( $datas ) = @_;

    my $regexp = '^\/cyruspartition\/'.$datas->{'entity'}.'\/'.$datas->{'operation'}.'\/([^\/]+)$';
    if( $datas->{'requestUri'} !~ /$regexp/ ) {
        my $return = $self->_response( RC_BAD_REQUEST, {
            content => [ 'Invalid URI '.$datas->{'requestUri'} ],
            help => [
                $self->getModuleName().' URI must be : /cyruspartition/'.$datas->{'entity'}.'/'.$datas->{'operation'}.'/<hostName>'
                ."\n".'<hostName> : OBM host name with imap role'
            ]
            } );
        return $return;
    }

    $datas->{'hostname'} = $1;

    my $domainList = $self->_getHostDomains( 'imapHost', $datas->{'hostname'} );
    if( !defined($domainList) ) {
        my $return = $self->_response( RC_INTERNAL_SERVER_ERROR, { content => [ 'Can\'t get domain linked to host '.$datas->{'hostname'}.' from LDAP server' ] } );
        return $return;
    }elsif( $#{$domainList} < 0 ) {
        my $return = $self->_response( RC_NOT_FOUND, { content => [ 'No domain linked to host '.$datas->{'hostname'}.' as IMAP service' ] } );
        return $return;
    }

    return $self->_updateImapdConf($datas->{'hostname'}, $domainList);
}


sub _delPartition {
    my $self = shift;
    my( $datas ) = @_;

    my $regexp = '^\/cyruspartition\/'.$datas->{'entity'}.'\/'.$datas->{'operation'}.'\/([^\/]+)$';
    if( $datas->{'requestUri'} !~ /$regexp/ ) {
        my $return = $self->_response( RC_BAD_REQUEST, {
            content => [ 'Invalid URI '.$datas->{'requestUri'} ],
            help => $self->getModuleName().' URI must be : /cyruspartition/'.$datas->{'entity'}.'/'.$datas->{'operation'}.'/<hostName>'
            ."\n".'<hostName> : OBM host name with imap role'
            } );
        return $return;
    }

    $datas->{'hostname'} = $1;

    my $domainList = $self->_getHostDomains( 'imapHost', $datas->{'hostname'} );
    if( !defined($domainList) ) {
        my $return = $self->_response( RC_INTERNAL_SERVER_ERROR, { content => [ 'Can\'t get domain linked to host '.$datas->{'hostname'}.' from LDAP server' ] } );
        return $return;
    }

    return $self->_updateImapdConf($datas->{'hostname'}, $domainList);
}


sub _updateImapdConf {
    my $self = shift;
    my( $hostname, $domainList ) = @_;

    if( !open( FIC, $self->{'imapdConfFile'} ) ) {
        $self->_log( 'Unable to open Cyrus Imapd configuration file', 1 );
        return $self->_response( RC_INTERNAL_SERVER_ERROR, { content => [ 'Unable to open Cyrus Imapd configuration file' ] } );
    }


    my @file;
    while( my $line = <FIC> ) {
        chomp($line);
        push( @file, $line );
    }
    close(FIC);

    # Read configuration file to :
    #  - get defined default partitions
    #  - create a conf file without partitions definitions
    my %currentPartitions;
    my $defaultPartitionName;
    my @template;
    for( my $i=0; $i<=$#file; $i++ ) {
        if( $file[$i] =~ /^partition-(.+)\s*:(.+)$/ ) {
            my $partitionName = $1;
            $currentPartitions{$partitionName} = $2;
            $currentPartitions{$partitionName} =~ s/^\s+//;

            $self->_log( 'Load partition \''.$partitionName.'\', directory \''.$currentPartitions{$partitionName}.'\' from configuration file', 4 );
        }elsif( $file[$i] =~ /^defaultpartition\s*:(.+)$/ ) {
            $defaultPartitionName = $1;
            $defaultPartitionName =~ s/^\s+//;

            if( $defaultPartitionName !~ /^[a-zA-Z0-9]+$/ ) {
                $self->_log( 'Invalid default partition name \''.$defaultPartitionName.'\'', 1 );
                $defaultPartitionName = undef;
            }else {
                $self->_log( 'Default partition name \''.$defaultPartitionName.'\'', 3 );
            }
        }else {
            push( @template, $file[$i] );
        }
    }

    # Keep only default partition
    while( my($partitionName, $path) = each(%currentPartitions) ) {
        if( $partitionName eq $defaultPartitionName ) {
            next;
        }

        delete($currentPartitions{$partitionName});
    }

    if( !defined($defaultPartitionName) ) {
        $self->_log( 'No default partition defined', 2 );
    }elsif( defined($defaultPartitionName) && !defined($currentPartitions{$defaultPartitionName}) ) {
        $self->_log( 'No default partition defined', 2 );
        $defaultPartitionName = undef;
    }

    # Set original file to undef
    @file = undef;

    for( my $i=0; $i<=$#{$domainList}; $i++ ) {
        my $domainPartitionName = $domainList->[$i];
        $domainPartitionName =~ s/\./_/g;
        $domainPartitionName =~ s/-/_/g;

        if( !exists($currentPartitions{$domainPartitionName}) ) {
            $currentPartitions{$domainPartitionName} = $self->{'imapdPartitionRoot'}.'/'.$domainPartitionName;
            $self->_log( 'Add Cyrus Imapd partition \''.$domainPartitionName.'\', directory \''.$currentPartitions{$domainPartitionName}.'\'', 3 );
        }
    }

    $self->_log( 'Re-write Cyrus Imapd configuration file', 3 );
    if( !open( FIC, '>'.$self->{'imapdConfFile'} ) ) {
        $self->_log( 'Unable to open Cyrus Imapd configuration file', 1 );
        return $self->_response( RC_INTERNAL_SERVER_ERROR, { content => [ 'Unable to open Cyrus Imapd configuration file' ] } );
    }

    while( my( $partitionName, $partitionPath ) = each(%currentPartitions) ) {
        print FIC 'partition-'.$partitionName.': '.$partitionPath."\n";
    }

    if( defined($defaultPartitionName) ) {
        print FIC 'defaultpartition: '.$defaultPartitionName."\n";
    }else {
        $self->_log( 'No default partition set. If you wish to set one, add \'defaultpartition\' to \''.$self->{'imapdConfFile'}.'\' configuration file and restart Cyrus services', 2 );
    }

    for( my $i=0; $i<=$#template; $i++ ) {
        print FIC $template[$i]."\n";
    }
    close(FIC);

    my $return = $self->_restartCyrusService();
    push( @{$return->[1]->{'status'}}, $self->{'imapdConfFile'}.' Cyrus configuration file update successfully on host '.$hostname );
    push( @{$return->[1]->{'domain'}}, @{$domainList} );
    return $return;
}


sub _restartCyrusService {
    my $self = shift;

    my $cmd = $self->{'cyrusStartupScript'}.' stop > /dev/null 2>&1';
    $self->_log( 'Stop Cyrus service '.$cmd, 3 );
    my $ret = 0xffff & system $cmd;

    if( $ret ) {
        $self->_log( 'Fail to stop Cyrus service', 1 );
        return $self->_response( RC_INTERNAL_SERVER_ERROR, { content => [ 'Fail to stop Cyrus service' ] } );
    }

    $cmd = $self->{'cyrusStartupScript'}.' start > /dev/null 2>&1';
    $self->_log( 'Start Cyrus service '.$cmd, 3 );
    $ret = 0xffff & system $cmd;

    if( $ret ) {
        $self->_log( 'Fail to start Cyrus service', 1 );
        return $self->_response( RC_INTERNAL_SERVER_ERROR, { content => [ 'Fail to start Cyrus service' ] } );
    }


    return $self->_response( RC_OK, { content => [ 'Cyrus service restart successfully' ] } );
}


# Perldoc

=head1 NAME

cyrusPartition obmSatellite module

=head1 SYNOPSIS

This module manage Cyrus Imapd partitions. It need to contact OBM LDAP database.

This module is XML/HTTP REST compliant.

=head1 COMMAND

=over 4

=item B<POST /cyruspartition/host/add>/<obmHostName> : add needed Cyrus Imapd
partitions on host I<obmHostName>. Restart Cyrus service

=over 4

=item POST data : none

=item Return success or fail status

=back

=back
