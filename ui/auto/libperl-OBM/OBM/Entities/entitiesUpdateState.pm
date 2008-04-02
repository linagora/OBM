package OBM::Entities::entitiesUpdateState;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub new {
    my $self = shift;

    my %entitiesUpdateState = (
        updated => undef,
        updatedLinks => undef,
        deleted => undef
    );

    $entitiesUpdateState{"updated"} = 0;
    $entitiesUpdateState{"updatedLinks"} = 0;
    $entitiesUpdateState{"deleted"} = 0;

    bless( \%entitiesUpdateState, $self );
}


sub setUpdate {
    my $self = shift;

    $self->{"updated"} = 1;

    return 1;
}


sub getUpdate {
    my $self = shift;

    return $self->{"updated"};
}


sub setUpdateLinks {
    my $self = shift;

    $self->{"updatedLinks"} = 1;

    return 1;
}


sub getUpdateLinks {
    my $self = shift;

    return $self->{"updatedLinks"};
}


sub setDelete {
    my $self = shift;

    $self->{"deleted"} = 1;

    return 1;
}


sub getDelete {
    my $self = shift;

    return $self->{"deleted"};
}
