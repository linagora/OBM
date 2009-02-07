package OBM::Ldap::ldapServers;

$VERSION = "1.0";

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
    $self->{'servers'} = undef;
}


sub getLdapServer {
    my $self = shift;
    my( $serverId ) = @_;

    if( !defined($serverId) ) {
        $self->_log( 'identifiant de serveur non défini', 3 );
        return undef;
    }

    if( ref($serverId) || ($serverId !~ /$OBM::Parameters::regexp::regexp_server_id/) ) {
        $self->_log( 'identifiant de serveur incorrect', 3 );
        return undef;
    }

    if( exists($self->{'servers'}->{$serverId}) ) {
        $self->_log( 'serveur d\'identifiant \''.$serverId.'\' déjà chargé', 3 );
        if( !defined($self->{'servers'}->{$serverId}) ) {
            $self->_log( 'serveur d\'identifiant \''.$serverId.'\' non défini', 3 );
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

    $self->_log( 'obtention de la connexion au '.$self->{'servers'}->{$serverId}->getDescription(), 3 );
    return $self->{'servers'}->{$serverId}->getLdapConn();
}


sub _loadServer {
    my $self = shift;
    my( $serverId ) = @_;

    $self->_log( 'chargement du serveur LDAP d\'identifiant \''.$serverId.'\'', 2 );

    require OBM::Ldap::ldapServer;
    $self->{'servers'}->{$serverId} = OBM::Ldap::ldapServer->new( $serverId );

    if( !defined($self->{'servers'}->{$serverId}) ) {
        $self->_log( 'serveur d\'identifiant \''.$serverId.'\' non trouvé', 3 );
    }

    return $self->{'servers'}->{$serverId};
}
