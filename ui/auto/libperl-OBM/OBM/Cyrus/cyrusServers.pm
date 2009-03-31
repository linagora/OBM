package OBM::Cyrus::cyrusServers;

$VERSION = '1.0';

use OBM::Tools::obmServersList;
@ISA = ('OBM::Tools::obmServersList', 'Class::Singleton');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub _new_instance {
    my $class = shift;

    my $self = bless { }, $class;

    $self->{'servers'} = undef;

    return $self;
}


sub _loadServer {
    my $self = shift;
    my( $serverId ) = @_;

    $self->_log( 'chargement du serveur IMAP d\'identifiant \''.$serverId.'\'', 2 );

    require OBM::Cyrus::cyrusServer;
    $self->{'servers'}->{$serverId} = OBM::Cyrus::cyrusServer->new( $serverId );

    if( !defined($self->{'servers'}->{$serverId}) ) {
        $self->_log( 'serveur d\'identifiant \''.$serverId.'\' non trouvé', 3 );
    }

    return $self->{'servers'}->{$serverId};
}


sub getEntityCyrusServer {
    my $self = shift;
    my( $entity ) = @_;

    if( !ref($entity) ) {
        $self->_log( 'entité incorrecte', 3 );
        return undef;
    }

    if( $self->_checkServerIds( $entity->getMailServerId(), $entity->getDomainId() ) ) {
        return undef;
    }

    $self->_log( 'obtention du serveur '.$self->{'servers'}->{$entity->getMailServerId()}->getDescription(), 3 );

    return $self->{'servers'}->{$entity->getMailServerId()};
}


sub getCyrusServerById {
    my $self = shift;
    my( $serverId, $domainId ) = @_;

    if( $self->_checkServerIds( $serverId, $domainId ) ) {
        return undef;
    }

    $self->_log( 'obtention de la description de '.$self->{'servers'}->{$serverId}->getDescription(), 3 );
    return $self->{'servers'}->{$serverId};
}


sub getCyrusServerIp {
    my $self = shift;
    my( $serverId, $domainId ) = @_;

    if( $self->_checkServerIds( $serverId, $domainId ) ) {
        return undef;
    }

    $self->_log( 'obtention du nom d\'hôte du '.$self->{'servers'}->{$serverId}->getDescription(), 3 );
    return $self->{'servers'}->{$serverId}->getCyrusServerIp();
}
