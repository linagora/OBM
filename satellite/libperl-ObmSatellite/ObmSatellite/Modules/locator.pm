package ObmSatellite::Modules::locator;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;

use ObmSatellite::Modules::abstract;
@ISA = qw(ObmSatellite::Modules::abstract);
use strict;

use HTTP::Status;
require ObmSatellite::Services::SQL;


sub _initHook {
    my $self = shift;

    $self->{'uri'} = [ '/locator' ];
    $self->{'neededServices'} = [ 'SQL' ];

    return 1;
}


sub _getMethod {
    my $self = shift;
    my( $requestUri, $requestBody ) = @_;
    my %datas;

    $datas{'requestUri'} = $requestUri;

    if( $requestUri !~ /^\/locator\/([^\/]+)(.*)$/ ) {
        my $return = $self->_response( RC_BAD_REQUEST, { content => [ 'Invalid URI '.$requestUri ] } );
        $return->[1]->{'help'} = [ $self->getModuleName().' URI must be : /locator/<entity>' ];
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

    my $regexp = '^\/locator\/'.$datas->{'entity'}.'\/([^\/]+)(.*)$';
    if( $datas->{'requestUri'} !~ /$regexp/ ) {
        my $return = $self->_response( RC_BAD_REQUEST, { content => [ 'Invalid URI '.$datas->{'requestUri'} ] } );
        $return->[1]->{'help'} = [ 'Locator URI must be : /locator/'.$datas->{'entity'}.'/<service>' ];
        return $return;
    }

    $datas->{'service'} = $1;

    SWITCH: {
        if( $datas->{'service'} eq 'imap' ) {
            return $self->_imapService( $datas );
        }

        if( $datas->{'service'} eq 'sync' ) {
            return $self->_obmSyncService( $datas );
        }
    }
    

    return $self->_response( RC_NOT_FOUND, { content => [ 'Unknow service \''.$datas->{'service'}.'\'' ] } );
}


sub _imapService {
    my $self = shift;
    my( $datas ) = @_;

    my $regexp = '^\/locator\/'.$datas->{'entity'}.'\/'.$datas->{'service'}.'\/([^\/]+)(.*)$';
    if( $datas->{'requestUri'} !~ /$regexp/ ) {
        my $return = $self->_response( RC_BAD_REQUEST, { content => [ 'Invalid URI '.$datas->{'requestUri'} ] } );
        $return->[1]->{'help'} = [ 'Locator URI must be : /locator/'.$datas->{'entity'}.'/'.$datas->{'service'}.'/<entityType>' ];
        return $return;
    }

    $datas->{'entityType'} = $1;

    SWITCH: {
        if( $datas->{'entityType'} eq 'user' ) {
            return $self->_userImapService( $datas );
        }

        if( $datas->{'entityType'} eq 'mailshare' ) {
            return $self->_mailshareImapService( $datas );
        }
    }

    return $self->_response( RC_NOT_FOUND, { content => [ 'Unknow entity type
    \''.$datas->{'entityType'}.'\'' ] } );
}


sub _userImapService {
    my $self = shift;
    my( $datas ) = @_;

    my $regexp = '^\/locator\/'.$datas->{'entity'}.'\/'.$datas->{'service'}.'\/'.$datas->{'entityType'}.'\/([^\/]+)$';
    if( $datas->{'requestUri'} !~ /$regexp/ ) {
        my $return = $self->_response( RC_BAD_REQUEST, { content => [ 'Invalid URI '.$datas->{'requestUri'} ] } );
        $return->[1]->{'help'} = [ 'Locator URI must be : /locator/'.$datas->{'entity'}.'/'.$datas->{'service'}.'/'.$datas->{'entityType'}.'/<entityId>' ];
        return $return;
    }

    $datas->{'id'} = $1;

    if( $datas->{'id'} !~ /^([^@]+)@([^@]+)$/ ) {
        my $return = $self->_response( RC_BAD_REQUEST, { content => [ 'Invalid login '.$datas->{'id'} ] } );
        $return->[1]->{'help'} = [ 'Login must be of the form : login@domain' ];
        return $return;
    }

    $datas->{'login'} = $1;
    $datas->{'domain'} = $2;

    my $query = 'SELECT P_Host.host_ip
                    FROM P_UserObm 
                    INNER JOIN P_Domain ON P_Domain.domain_id = P_UserObm.userobm_domain_id 
                    INNER JOIN P_Host on P_Host.host_id = P_UserObm.userobm_mail_server_id 
                    WHERE P_Domain.domain_name = \''.$datas->{'domain'}.'\'
                        AND P_UserObm.userobm_login = \''.$datas->{'login'}.'\'
                    LIMIT 1';

    my $dbHandler = ObmSatellite::Services::SQL->instance();

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'Failed on get imap service for user '.$datas->{'id'}.' SQL query', 1 );
        return $self->_response( RC_INTERNAL_SERVER_ERROR, { content => [ 'Failed on get imap service for user '.$datas->{'id'}.' SQL query' ] } );
    }

    my $result = $queryResult->fetchall_arrayref({});
    if( $#{$result} == 0 ) {
        return $self->_response( RC_OK, $result->[0]->{'host_ip'} );
    }

    return $self->_response( RC_NOT_FOUND );
}


sub _mailshareImapService {
    my $self = shift;
    my( $datas ) = @_;

    my $regexp = '^\/locator\/'.$datas->{'entity'}.'\/'.$datas->{'service'}.'\/'.$datas->{'entityType'}.'\/([^\/]+)$';
    if( $datas->{'requestUri'} !~ /$regexp/ ) {
        my $return = $self->_response( RC_BAD_REQUEST, { content => [ 'Invalid URI '.$datas->{'requestUri'} ] } );
        $return->[1]->{'help'} = [ 'Locator URI must be : /locator/'.$datas->{'entity'}.'/'.$datas->{'service'}.'/'.$datas->{'entityType'}.'/<entityId>' ];
        return $return;
    }

    $datas->{'id'} = $1;

    if( $datas->{'id'} !~ /^([^@]+)@([^@]+)$/ ) {
        my $return = $self->_response( RC_BAD_REQUEST, { content => [ 'Invalid mailshare name '.$datas->{'id'} ] } );
        $return->[1]->{'help'} = [ 'Mailshare name must be of the form : login@domain' ];
        return $return;
    }

    $datas->{'login'} = $1;
    $datas->{'domain'} = $2;

    my $query = 'SELECT P_Host.host_ip
                    FROM P_MailShare 
                    INNER JOIN P_Domain ON P_Domain.domain_id = P_MailShare.mailshare_domain_id 
                    INNER JOIN P_Host on P_Host.host_id = P_MailShare.mailshare_mail_server_id
                    WHERE P_Domain.domain_name = \''.$datas->{'domain'}.'\'
                        AND P_MailShare.mailshare_name = \''.$datas->{'login'}.'\'
                    LIMIT 1';

    my $dbHandler = ObmSatellite::Services::SQL->instance();

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'Failed on get imap service for user '.$datas->{'id'}.' SQL query', 1 );
        return $self->_response( RC_INTERNAL_SERVER_ERROR, { content => [ 'Failed on get imap service for user '.$datas->{'id'}.' SQL query' ] } );
    }

    my $result = $queryResult->fetchall_arrayref({});
    if( $#{$result} == 0 ) {
        return $self->_response( RC_OK, $result->[0]->{'host_ip'} );
    }

    return $self->_response( RC_NOT_FOUND );
}


sub _obmSyncService {
    my $self = shift;
    my( $datas ) = @_;

    my $regexp = '^\/locator\/'.$datas->{'entity'}.'\/'.$datas->{'service'}.'\/([^\/]+)\/([^\/]+)$';
    if( $datas->{'requestUri'} !~ /$regexp/ ) {
        my $return = $self->_response( RC_BAD_REQUEST, { content => [ 'Invalid URI '.$datas->{'requestUri'} ] } );
        $return->[1]->{'help'} = [ 'Locator URI must be : /locator/'.$datas->{'entity'}.'/'.$datas->{'service'}.'/<serviceProperty>/<entityId>' ];
        return $return;
    }

    $datas->{'serviceProperty'} = $1;
    $datas->{'id'} = $2;

    if( $datas->{'id'} !~ /^([^@]+)@([^@]+)$/ ) {
        my $return = $self->_response( RC_BAD_REQUEST, { content => [ 'Invalid ID '.$datas->{'id'} ] } );
        $return->[1]->{'help'} = [ 'ID must be of the form : login@domain' ];
        return $return;
    }

    $datas->{'login'} = $1;
    $datas->{'domain'} = $2;


    my $query = 'SELECT P_Host.host_ip
                    FROM P_ServiceProperty
                    INNER JOIN P_DomainEntity ON P_DomainEntity.domainentity_entity_id = P_ServiceProperty.serviceproperty_entity_id
                    INNER JOIN P_Host ON P_Host.host_id = P_ServiceProperty.serviceproperty_value
                    INNER JOIN P_Domain ON P_DomainEntity.domainentity_domain_id = P_Domain.domain_id
                    WHERE P_ServiceProperty.serviceproperty_service = \''.$datas->{'service'}.'\'
                        AND P_ServiceProperty.serviceproperty_property = \''.$datas->{'serviceProperty'}.'\'
                        AND P_Domain.domain_name=\''.$datas->{'domain'}.'\'
                    LIMIT 1';

    my $dbHandler = ObmSatellite::Services::SQL->instance();

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'Failed on get sync service for user '.$datas->{'id'}.' SQL query', 1 );
        return $self->_response( RC_INTERNAL_SERVER_ERROR, { content => [ 'Failed on get sync service for user '.$datas->{'id'}.' SQL query' ] } );
    }

    my $result = $queryResult->fetchall_arrayref({});
    if( $#{$result} == 0 ) {
        return $self->_response( RC_OK, $result->[0]->{'host_ip'} );
    }

    return $self->_response( RC_NOT_FOUND );
}


# Perldoc

=head1 NAME

locator obmSatellite module

=head1 SYNOPSIS

This module permit to obtain OBM services informations from database distant
server. It need to contact OBM SQL database, getting needed informations from
/etc/obm/obm_conf.ini.

This module is PLAIN/HTTP REST compliant.

=head1 COMMAND

=head2 IMAP server

=over 4

=item B<GET /locator/host/imap/user>/login@domain : getting Imap server IP for
user I<login@domain>.

=over 4

=item Return PLAIN server IP.

=back

=item B<GET /locator/host/imap/mailshare>/mailsharename@domain : getting Imap
server IP for mailshare I<mailsharename@domain>.

=over 4

=item Return PLAIN server IP.

=back

=back

=head2 Sync service

=over 4

=item B<GET /locator/host/sync>/<serviceProperty>/login@domain : getting
I<serviceProperty> Sync service for user I<login@domain>.

=over 4

=item Return PLAIN I<serviceProperty> Sync service value.

=back

=back

B<serviceProperty> can be any I<serviceproperty_property> value from database
table I<ServiceProperty> associated with B<sync> I<serviceproperty_service>
