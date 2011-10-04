package OBM::Entities::systemEntityIdGetter;

$VERSION = '1.0';

use OBM::Entities::entityIdGetter;
@ISA = ('OBM::Entities::entityIdGetter');

$debug = 1;

use 5.006_001;
use strict;


sub _userObmTable {
    my $self = shift;

    return 'P_UserObm';
}


sub _mailShareTable {
    my $self = shift;

    return 'P_MailShare';
}
