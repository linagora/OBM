package OBM::Postfix::smtpInServer;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(_log dump);


sub new {
    my $class = shift;
    my( $serverDesc ) = @_;

    my $self = bless { }, $class;

    if( $self->_init( $serverDesc ) ) {
        $self->_log( 'problème lors de l\'initialisation du serveur de type SMTP-in', 0 );
        return undef;
    }

    $self->{'enable'} = 1;

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub _init {
    my $self = shift;
    my( $serverDesc ) = @_;

    if( !defined($serverDesc) || (ref($serverDesc) ne 'HASH') ) {
        $self->_log( 'description du serveur de type SMTP-in invalide', 2 );
        return 1;
    }

    if( !defined($serverDesc->{'host_ip'}) && !defined($serverDesc->{'host_fqdn'}) ) {
        $self->_log( 'pas d\'adresse IP ou de nom d\'hôte associé, traitement impossible', 0 );
        return 1;
    }elsif( defined($serverDesc->{'host_ip'}) ) {
        $self->_log( 'contact de l\'hote sur son adresse IP '.$serverDesc->{'host_ip'}, 3 );
        $self->_log( 'si l\'IP est définie en BD, elle est utilisée en priorité', 4 );

        $self->{'network_name'} = $serverDesc->{'host_ip'};
    }elsif( defined($serverDesc->{'host_fqdn'}) ) {
        $self->_log( 'contact de l\'hote sur son nom \''.$serverDesc->{'host_fqdn'}.'\'', 3 );

        $self->{'network_name'} = $serverDesc->{'host_fqdn'};
    }

    $self->{'host_ip'} = $serverDesc->{'host_ip'};
    $self->{'host_fqdn'} = $serverDesc->{'host_fqdn'};

    if( !defined($serverDesc->{'host_name'}) ) {
        $self->_log( 'non d\'hôte non défini', 0 );
        return 1;
    }
    $self->{'host_name'} = $serverDesc->{'host_name'};

    $self->{'host_id'} = $serverDesc->{'host_id'};

    return 0;
}


sub getDescription {
    my $self = shift;

    my $description = 'Hôte SMTP-in \''.$self->{'host_name'}.'\''.eval{
            my $desc;
            if( defined($self->{'host_id'}) ) {
                $desc .= ' (ID '.$self->{'host_id'}.')';
            }
    
            return $desc;
        }
        .', '.$self->{'network_name'};

    return $description;
}


sub enable {
    my $self = shift;

    $self->{'enable'} = 1;

    return 0;
}


sub disable {
    my $self = shift;

    $self->{'enable'} = 0;

    return 0;
}


sub update {
    my $self = shift;
    my $errorCode = 0;

    if( !$self->{'enable'} ) {
        $self->_log( 'serveur '.$self->getDescription().' désactivé, impossible d\'effectuer la mise à jour', 0 );
        return 1;
    }

    require OBM::ObmSatellite::client;
    my $obmSatelliteClient = OBM::ObmSatellite::client->instance();
    if( !defined($obmSatelliteClient) ) {
        $self->_log( 'Echec lors de l\'initialisation du client obmSatellite', 3 );
        return 1;
    }

    return $obmSatelliteClient->post( $self->{'network_name'}, '/postfixsmtpinmaps/host/'.$self->{'host_name'} );
}
