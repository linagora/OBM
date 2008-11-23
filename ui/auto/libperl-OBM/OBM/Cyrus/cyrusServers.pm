package OBM::Cyrus::cyrusServers;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use base qw( Class::Singleton );
use OBM::Tools::commonMethods qw(_log dump);
require OBM::Parameters::regexp;


sub _new_instance {
    my $class = shift;

    my $self = bless { }, $class;

    $self->{'servers'} = undef;

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub _checkCyrusServerIds {
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
        $self->_log( 'identifiant de domain non défini', 3 );
        return 1;
    }elsif( ref($domainId) || ($domainId !~ /$OBM::Parameters::regexp::regexp_id/) ) {
        $self->_log( 'identifiant de domain incorrect', 3 );
        return 1;
    }

    if( exists($self->{'servers'}->{$serverId}) ) {
        if( !defined($self->{'servers'}->{$serverId}) ) {
            $self->_log( 'problème au chargement du serveur d\'identifiant \''.$serverId.'\'', 3 );
            return 1;
        }else {
            $self->_log( 'serveur d\'identifiant \''.$serverId.'\' déjà chargé', 3 );
        }
    }elsif( !$self->_loadServer( $serverId, $domainId ) ) {
        return 1;
    }

    return 0;
}


sub _loadServer {
    my $self = shift;
    my( $serverId, $domainId ) = @_;

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

    if( $self->_checkCyrusServerIds( $entity->getMailServerId(), $entity->getDomainId() ) ) {
        return undef;
    }

    $self->_log( 'obtention du serveur '.$self->{'servers'}->{$entity->getMailServerId()}->getDescription(), 3 );

    return $self->{'servers'}->{$entity->getMailServerId()};
}


sub getCyrusServerConn {
    my $self = shift;
    my( $serverId, $domainId ) = @_;

    if( $self->_checkCyrusServerIds( $serverId, $domainId ) ) {
        return undef;
    }

    $self->_log( 'obtention de la connexion au '.$self->{'servers'}->{$serverId}->getDescription(), 3 );
    return $self->{'servers'}->{$serverId}->getCyrusConn( $domainId );
}


sub getCyrusServerIp {
    my $self = shift;
    my( $serverId, $domainId ) = @_;

    if( $self->_checkCyrusServerIds( $serverId, $domainId ) ) {
        return undef;
    }

    $self->_log( 'obtention du nom d\'hôte du '.$self->{'servers'}->{$serverId}->getDescription(), 3 );
    return $self->{'servers'}->{$serverId}->getCyrusServerIp();
}
