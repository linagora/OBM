package ObmSatellite::Modules::postfixAccess;

=head1 NAME

ObmSatellite::Modules::postfixAccess - enables/disables the mailbox of a user.

=cut

$VERSION = '1.0';

use 5.006_001;
use strict;
use warnings;

use ObmSatellite::Modules::abstract;
use ObmSatellite::Modules::Postfix::constants qw/POSTMAP_CMD TRANSPORT_MAP/;
use ObmSatellite::Modules::Postfix::ldap;
use File::Temp;
use HTTP::Status;

use base qw(ObmSatellite::Modules::abstract);

sub _setUri {
    my $self = shift;

    return [ '/postfixAccess' ];
}

sub _initHook {
    my $self = shift;
    eval {
        require ObmSatellite::Services::LDAP;
    } or ($self->_log( 'Unable to load LDAP service', 0 ) && return undef);

    my $ldapService = ObmSatellite::Services::LDAP->instance();
    $self->{ldap} = ObmSatellite::Modules::Postfix::ldap->new($ldapService);
    return 1;
}

sub _postMethod {
    my ($self, $requestUri, $requestBody ) = @_;

    return $self->_updatePostfixAccess(uri => $requestUri, enableAccess => 1);
}

sub _deleteMethod {
    my ($self, $requestUri, $requestBody ) = @_;

    return $self->_updatePostfixAccess(uri => $requestUri, enableAccess => 0);
}

sub _updatePostfixAccess {
    my ($self, %params) = @_;
    my $requestUri = $params{uri};
    my $enableAccess = $params{enableAccess};

    my ($userLogin, $domain) = $self->_parseUri($requestUri);
    if (!defined $userLogin) {
        my $return = $self->_response( RC_BAD_REQUEST, { content => [ 'Invalid URI '.$requestUri ] } );
        return $return;
    }
    my $success = $self->_updateTransportMap($userLogin, $domain, $enableAccess);
    if ($success) {
        $self->_log("Failed to update the transport map: $success", 1);
        my $return = $self->_response( RC_INTERNAL_SERVER_ERROR, { content => [ 'Failed to update the transport map' ] } );
        return $return;
    }
    return $self->_response( RC_OK, { content => [ 'Postfix transport map updated successfully' ] } );
}

sub _updateTransportMap {
    my ($self, $userLogin, $domain, $enableAccess) = @_;

    my $fileWasUpdated = $self->_updateTransportFile($userLogin, $domain, $enableAccess);
    if (!$fileWasUpdated) {
        $self->_log("Unable to update ".TRANSPORT_MAP, 1);
        return 1;
    }
    my @args = (TRANSPORT_MAP);
    $self->_log("Updating ".TRANSPORT_MAP." with the command ".POSTMAP_CMD."
        ".join(' ', @args), 5);
    my $returnCode = 0xfff & system(POSTMAP_CMD, @args);
    return $returnCode;
}

sub _updateTransportFile {
    my ($self, $userLogin, $domain, $enableAccess) = @_;

    my $userEmail = $userLogin.'@'.$domain;
    my $postfixLine;
    if ($enableAccess) {
        my $mailBoxServer = $self->_getUserServerData($userLogin, $domain);
        if (!(defined $mailBoxServer)) {
            $self->_log("Can't locate LDAP record for ".$userLogin, 1);
            return 0;
        }
        $postfixLine = join("\t", ($userEmail, $mailBoxServer))."\n";
    }
    else {
        $postfixLine = join("\t", ($userEmail,
                "error:450 mailbox temporarily unavailable"))."\n";
    }

    my ($in_fh, $out_fh);

    if (!(open $in_fh, TRANSPORT_MAP)) {
        $self->_log("Can't open ".TRANSPORT_MAP.": $!", 1);
        return 0;
    }
    $out_fh = File::Temp->new(
        TEMPLATE    => 'etc_postfix_transport_XXXX',
        TMPDIR      => 1,
        UNLINK      => 0);
    my $wasUpdated = 0;
    while (my $lineRead = <$in_fh>) {
        my $lineToWrite;
        if ($lineRead =~ /^$userEmail\s/) {
            $lineToWrite = $postfixLine;
        }
        else {
            $lineToWrite = $lineRead;
        }
        print $out_fh $lineToWrite;
    }
    close $out_fh;
    close $in_fh;
    if (! rename ($out_fh->filename(), TRANSPORT_MAP) ) {
        $self->_log("Can't rename ".$out_fh->filename()." to ".TRANSPORT_MAP, 1);
        return 0;
    }
    return 1;
}

sub _parseUri {
    my ($self, $requestUri) = @_;

    my ($login, $domain);
    if ($requestUri =~ m!^/postfixAccess/([^@]+)@(.+)$!) {
        ($login, $domain) = ($1, $2);
    }
    return ($login, $domain);
}

sub _getUserServerData {
    my ($self, $userLogin, $domain) = @_;

    my $mailboxServer = $self->{ldap}->query($userLogin, $domain);

    return $mailboxServer;
}

1;
