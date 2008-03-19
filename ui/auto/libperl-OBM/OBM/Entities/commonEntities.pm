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

    return 0;
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
