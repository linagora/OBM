package OBM::Entities::systemEntityIdGetter;

$VERSION = '1.0';

use OBM::Entities::entityIdGetter;
@ISA = ('OBM::Entities::entityIdGetter');

$debug = 1;

use 5.006_001;
use strict;


my $domain = 'Domain';
my $userObmTable = 'P_UserObm';
my $mailShareTable = 'P_MailShare';
