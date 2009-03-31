package OBM::Tools::obmServersList;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(_log dump);
require OBM::Parameters::regexp;


sub new {
    return undef;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub _checkServerIds {
    my $self = shift;
    my( $serverId, $domainId ) = @_;

    if( !defined($serverId) ) {
        $self->_log( 'identifiant de serveur non défini', 3 );
        return 1;
    }elsif( ref($serverId) || ($serverId !~ /$OBM::Parameters::regexp::regexp_server_id/) ) {
        $self->_log( 'identifiant de serveur incorrect', 3 );
        return 1;
    }

    if( !defined($domainId) ) {
        $self->_log( 'identifiant de domaine non défini', 3 );
        return 1;
    }elsif( ref($domainId) || ($domainId !~ /$OBM::Parameters::regexp::regexp_id/) ) {
        $self->_log( 'identifiant de domaine incorrect', 3 );
        return 1;
    }

    if( !defined($self->{'servers'}->{$serverId}) ) {
        if( !$self->_loadServer( $serverId ) ) {
            return 1;
        }else {
            push( @{$self->{'serversList'}}, $self->{'servers'}->{$serverId} );
        }
    }else {
        $self->_log( 'serveur d\'identifiant \''.$serverId.'\' déjà chargé', 3 );
    }

    return 0;
}


sub _loadServer {
    my $self = shift;
}


sub nextServer {
    my $self = shift;

    if( !defined($self->{'serversListIndex'}) ) {
        $self->{'serversListIndex'} = 0;
    }else {
        $self->{'serversListIndex'}++;
    }

    if( (ref($self->{'serversList'}) eq 'ARRAY') && ($self->{'serversListIndex'} <= $#{$self->{'serversList'}}) ) {
        return $self->{'serversList'}->[$self->{'serversListIndex'}];
    }

    $self->resetServerListIndex();
    return undef;
}


sub resetServerListIndex {
    my $self = shift;

    $self->{'serversListIndex'} = undef;

    return 0;
}
