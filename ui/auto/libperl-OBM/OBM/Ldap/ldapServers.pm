package OBM::Ldap::ldapServers;

$VERSION = "1.0";

use Class::Singleton;
use OBM::Log::log;
@ISA = ('Class::Singleton', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

require OBM::Parameters::regexp;


sub _new_instance {
    my $class = shift;

    my $self = bless { }, $class;

    $self->{'servers'} = undef;

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );
    $self->{'servers'} = undef;
}


sub getLdapServer {
    my $self = shift;
    my( $serverId ) = @_;

    if( !defined($serverId) ) {
        $self->_log( 'identifiant de serveur non défini', 1 );
        return undef;
    }

    if( ref($serverId) || ($serverId !~ /$OBM::Parameters::regexp::regexp_server_id/) ) {
        $self->_log( 'identifiant de serveur incorrect', 1 );
        return undef;
    }

    if( exists($self->{'servers'}->{$serverId}) ) {
        $self->_log( 'serveur d\'identifiant \''.$serverId.'\' déjà chargé', 4 );
        if( !defined($self->{'servers'}->{$serverId}) ) {
            $self->_log( 'serveur d\'identifiant \''.$serverId.'\' non défini', 1 );
            return undef;
        }
    }elsif( !$self->_loadServer( $serverId ) ) {
        return undef;
    }

    return $self->{'servers'}->{$serverId};
}


sub getLdapServerConn {
    my $self = shift;
    my( $serverId ) = @_;

    if( !$self->getLdapServer($serverId ) ) {
        return undef;
    }

    $self->_log( 'obtention de la connexion au '.$self->{'servers'}->{$serverId}->getDescription(), 5 );
    return $self->{'servers'}->{$serverId}->getConn();
}


sub _loadServer {
    my $self = shift;
    my( $serverId ) = @_;

    $self->_log( 'chargement du serveur LDAP d\'identifiant \''.$serverId.'\'', 3 );

    require OBM::Ldap::ldapServer;
    $self->{'servers'}->{$serverId} = OBM::Ldap::ldapServer->new( $serverId );

    if( !defined($self->{'servers'}->{$serverId}) ) {
        $self->_log( 'serveur d\'identifiant \''.$serverId.'\' non trouvé', 1 );
    }

    return $self->{'servers'}->{$serverId};
}
