package OBM::Tools::obmServer;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(_log dump);


sub new {
    return undef;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub _getServerDesc {
    return 1;
}


sub getId {
    my $self = shift;

    return $self->{'serverId'};
}


sub getDescription {
    my $self = shift;
    
    my $description = 'serveur '.$self->{'serverType'}.' d\'ID \''.$self->{'serverId'}.'\'';

    if( $self->{'serverDesc'}->{'host_description'} ) {
        $description .= ', \''.$self->{'serverDesc'}->{'host_description'}.'\'';
    }

    if( $self->{'serverDesc'}->{'host_ip'} ) {
        $description .= ', \''.$self->{'serverDesc'}->{'host_ip'}.'\'';
    }

    return $description;
}


sub getConn {
    return undef;
}


sub _connect {
    return 1;
}


sub _ping {
    return 1;
}


sub _setDeadStatus {
    my $self = shift;

    $self->{'deadStatus'} = 1;

    return 0;
}


sub _unsetDeadStatus {
    my $self = shift;

    $self->{'deadStatus'} = 0;

    return 0;
}


sub getDeadStatus {
    my $self = shift;

    return $self->{'deadStatus'};
}
