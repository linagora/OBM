package OBM::Entities::commonEntities;


$debug = 1;


use 5.006_001;
use strict;
use vars qw( @EXPORT_OK $VERSION );
use base qw(Exporter);


$VERSION = '1.0';
@EXPORT_OK = qw(    setDelete
                    getDelete
                    getArchive
                    setArchive
                    getParent
                    setBdUpdate
                    unsetBdUpdate
                    getBdUpdate
                    setUpdated
                    unsetUpdated
                    getUpdated
                    getDesc
                    _makeEntityEmail
                    setUpdateEntity
                    getUpdateEntity
                    setUpdateLinks
                    getUpdateLinks
                    isMailAvailable
                    isSieveAvailable
               );



sub setDelete {
    my $self = shift;

    $self->{'toDelete'} = 1;

    return 0;
}


sub getDelete {
    my $self = shift;

    return $self->{'toDelete'};
}


sub setArchive {
    my $self = shift;

    $self->{'archive'} = 1;

    return 0;
}


sub getArchive {
    my $self = shift;

    return $self->{'archive'};
}


sub getParent {
    my $self = shift;

    return $self->{'parent'};
}


# Set entity to be updated in BD if it's system update is ok
sub setBdUpdate {
    my $self = shift;

    $self->{'allowBdUpdate'} = 1;

    return 0;
}


# Set entity to be updated in BD whatever it's system update is ok or not
sub unsetBdUpdate {
    my $self = shift;

    $self->{'allowBdUpdate'} = 0;

    return 0;
}


# Is entity can be updated in BD ?
sub getBdUpdate {
    my $self = shift;

    return $self->{'allowBdUpdate'};
}


# Set that entity system update is ok
sub setUpdated {
    my $self = shift;

    $self->{'updated'} = 1;

    return 0;
}


# Set that entity system update isn't ok
sub unsetUpdated {
    my $self = shift;

    $self->{'updated'} = 0;

    return 0;
}


# Is entity system update ok or not ?
sub getUpdated {
    my $self = shift;

    return $self->{'updated'};
}


sub getDesc {
    my $self = shift;
    my( $desc ) = @_;

    if( $desc && !ref($desc) ) {
        return $self->{'entityDesc'}->{$desc};
    }

    return undef;
}


sub _makeEntityEmail {
    require OBM::Parameters::regexp;
    my $self = shift;
    my( $mailAddress, $mainDomain, $domainAlias ) = @_;
    my $totalEmails = 0;
    my %emails;
    my %emailsAlias;

    if( !$mailAddress ) {
        $self->_log( 'pas d\'adresses mails définis', 3 );
        return $totalEmails;
    }

    if( !$mainDomain ) {
        $self->_log( 'pas de domaine principal défini', 3 );
        return $totalEmails;
    }

    if( ref($domainAlias) ne 'ARRAY' ) {
        $self->_log( 'pas d\'alias de domaine définis', 3 );
        $domainAlias = undef;
    }

    my @email = split( /\r\n/, $mailAddress );
    
    for( my $i=0; $i<=$#email; $i++ ) {
        $email[$i] = lc($email[$i]);

        SWITCH: {
            if( $email[$i] =~ /$OBM::Parameters::regexp::regexp_email/ ) {
                $emails{$email[$i]} = 1;
                $totalEmails++;
                last SWITCH;
            }

            if( $email[$i] =~ /$OBM::Parameters::regexp::regexp_email_left/ ) {
                $emails{$email[$i].'@'.$mainDomain} = 1;
                $totalEmails++;

                for( my $j=0; $j<=$#{$domainAlias}; $j++ ) {
                    $emailsAlias{$email[$i].'@'.$domainAlias->[$j]} = 1;
                    $totalEmails++;
                }

                last SWITCH;
            }
        }
    }

    my @emails = keys(%emails);
    if( $#emails >= 0 ) {
        $self->{'email'} = \@emails;
    }

    my @emailsAlias = keys(%emailsAlias);
    if( $#emailsAlias >= 0 ) {
        $self->{'emailAlias'} = \@emailsAlias;
    }

    return $totalEmails;
}


# Update entity informations
sub setUpdateEntity {
    my $self = shift;

    $self->{'update'}->{'entity'} = 1;
}


# Get update entity informations state
sub getUpdateEntity {
    my $self = shift;

    return $self->{'update'}->{'entity'};
}


# Update entity links
sub setUpdateLinks {
    my $self = shift;

    $self->{'update'}->{'links'} = 1;
}


# Get update entity links state
sub getUpdateLinks {
    my $self = shift;

    return $self->{'update'}->{'links'};
}


# Get if the entity can have mail permission
sub isMailAvailable {
    my $self = shift;

    return 0;
}


# Get if the entity can have mail permission
sub isSieveAvailable {
    my $self = shift;

    return 0;
}


# Set mailbox quota used
sub setCyrusQuotaUsed {
    my $self = shift;
    my( $quotaUsed ) = @_;

    if( $quotaUsed !~ /^\d+$/ ) {
        $self->_log( 'quota utilisé incorrect : '.$quotaUsed, 0 );
        return 1;
    }

    $self->{'mail_quota_used'} = $quotaUsed;

    return 0;
}


# Get mailbox quota used
sub getCyrusQuotaUsed {
    my $self = shift;
    my $quotaUsed = 0;

    if( defined($self->{'mail_quota_used'}) ) {
        $quotaUsed = $self->{'mail_quota_used'};
    }

    return $quotaUsed;
}
