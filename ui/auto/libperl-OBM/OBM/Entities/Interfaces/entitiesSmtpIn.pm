package OBM::Entities::Interfaces::entitiesSmtpIn;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub smtpInUpdateMap {
    my $self = shift;

    $self->_log( 'l\'entité '.$self->getDescription().' n\'a pas d\'impact sur le contenu des maps postfix', 3 );
    return 0;
}
