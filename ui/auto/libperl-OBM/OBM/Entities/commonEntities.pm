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
                    makeEntityEmail
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


sub makeEntityEmail {
    require OBM::Parameters::common;
    my $self = shift;
    my( $mailAddress, $mainDomain, $domainAlias ) = @_;
    my $totalEmails = 0;

    my @email = split( /\r\n/, $mailAddress );
    
    for( my $i=0; $i<=$#email; $i++ ) {
        SWITCH: {
            if( $email[$i] =~ /$OBM::Parameters::common::regexp_email/ ) {
                push( @{$self->{"properties"}->{"email"}}, $email[$i] );
                $totalEmails++;
                last SWITCH;
            }

            if( $email[$i] =~ /$OBM::Parameters::common::regexp_email_left/ ) {
                push( @{$self->{"properties"}->{"email"}}, $email[$i]."@".$mainDomain );
                $totalEmails++;

                for( my $j=0; $j<=$#{$domainAlias}; $j++ ) {
                    push( @{$self->{"properties"}->{"emailAlias"}}, $email[$i]."@".$domainAlias->[$j] );
                    $totalEmails++;
                }

                last SWITCH;
            }
        }
    }

    return $totalEmails;
}
