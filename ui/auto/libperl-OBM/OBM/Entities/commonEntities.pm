package OBM::Entities::commonEntities;


$debug = 1;


use 5.006_001;
use strict;
use vars qw( @EXPORT_OK $VERSION );
use base qw(Exporter);


$VERSION = "1.0";

@EXPORT_OK = qw(    getType
                    setDelete
                    getDelete
                    getArchive
                    getLdapObjectclass
                    isLinks
                    getEntityId
                    isMailActive
                    makeEntityEmail
                    getMailboxDefaultFolders
                    getHostIpById
               );



sub getType {
    my $self = shift;

    return $self->{type};
}


sub setDelete {
    my $self = shift;

    $self->{"toDelete"} = 1;

    return 1;
}


sub getDelete {
    my $self = shift;

    return $self->{"toDelete"};
}


sub getArchive {
    my $self = shift;

    return $self->{"archive"};
}


sub getLdapObjectclass {
    my $self = shift;

    return $self->{objectclass};
}


sub isLinks {
    my $self = shift;

    return $self->{links};
}


sub getEntityId {
    my $self = shift;

    return $self->{objectId};
}


sub isMailActive {
    my $self = shift;

    return 0;
}


sub makeEntityEmail {
    require OBM::Parameters::regexp;
    my $self = shift;
    my( $mailAddress, $mainDomain, $domainAlias ) = @_;
    my $totalEmails = 0;
    my %emails;
    my %emailsAlias;

    if( !defined($mailAddress) || ($mailAddress eq "") ) {
        return $totalEmails;
    }

    my @email = split( /\r\n/, $mailAddress );
    
    for( my $i=0; $i<=$#email; $i++ ) {
        SWITCH: {
            if( $email[$i] =~ /$OBM::Parameters::regexp::regexp_email/ ) {
                $emails{$email[$i]} = 1;
                $totalEmails++;
                last SWITCH;
            }

            if( $email[$i] =~ /$OBM::Parameters::regexp::regexp_email_left/ ) {
                $emails{$email[$i]."@".$mainDomain} = 1;
                $totalEmails++;

                for( my $j=0; $j<=$#{$domainAlias}; $j++ ) {
                    $emailsAlias{$email[$i]."@".$domainAlias->[$j]} = 1;
                    $totalEmails++;
                }

                last SWITCH;
            }
        }
    }

    my @emails = keys(%emails);
    if( $#emails >= 0 ) {
        $self->{"properties"}->{"email"} = \@emails;
    }

    my @emailsAlias = keys(%emailsAlias);
    if( $#emailsAlias >= 0 ) {
        $self->{"properties"}->{"emailAlias"} = \@emailsAlias;
    }

    return $totalEmails;
}


sub getMailboxDefaultFolders {
    my $self = shift;
    my $entryProp = $self->{"properties"};

    return $self->{"properties"}->{mailbox_folders};
}


sub getHostIpById {
    my $self = shift;
    my( $hostId ) = @_;

    if( !defined($hostId) ) {
        $self->_log( 'identifiant de l\'hote non défini !', 3 );
        return undef;
    }elsif( $hostId !~ /^[0-9]+$/ ) {
        $self->_log( 'identifiant de l\'hote \''.$hostId.'\' incorrect !', 3 );
        return undef;
    }
    
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connection a la base de donnee incorrect !', 3 );
        return undef;
    }

    my $hostTable = "Host";
    if( $self->getDelete() ) {
        $hostTable = "P_".$hostTable;
    }

    my $query = "SELECT host_ip FROM ".$hostTable." WHERE host_id='".$hostId."'";
    # On execute la requete
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return undef;
    }

    if( !(my( $hostIp ) = $queryResult->fetchrow_array) ) {
        $self->_log( 'identifiant de l\'hote \''.$hostId.'\' inconnu !', 3 );

        $queryResult->finish;
        return undef;
    }else{
        $queryResult->finish;
        return $hostIp;
    }

    return undef;

}


sub getMailServerId {
    my $self = shift;

    return undef;
}


sub updateLinkedEntity {
    my $self = shift;

    return 0;
}
