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


sub _connect {
    my $self = shift;

    if( (ref($self->{'serverConn'}) eq 'Net::Telnet') && (!$self->{'serverConn'}->eof()) ) {
        $self->_log( 'connexion déjà établie à l\'ObmSatellite de '.$self->getDescription(), 3 );
        return 0;
    }

    $self->_log( 'connexion à '.$self->getDescription(), 3 );

    require Net::Telnet;
    $self->{'serverConn'} = Net::Telnet->new(
        Host => $self->{'network_name'},
        Port => 30000,
        Timeout => 60,
        errmode => 'return'
    );

    if( !defined($self->{'serverConn'}) || !$self->{'serverConn'}->open() ) {
        $self->_log( 'échec de connexion à l\'ObmSatellite de '.$self->getDescription(), 0 );
        return 1;
    }

    while( (!$self->{'serverConn'}->eof()) && (my $line = $self->{'serverConn'}->getline(Timeout => 1)) ) {
        chomp($line);
        $self->_log( 'réponse: '.$line, 3 );
    }

    if( $self->{'serverConn'}->eof() ) {
        $self->_log( 'ObmSatellite de '.$self->getDescription().' a terminé la connexion. Vérifiez ses autorisations d\'accès', 0 );
        return 1;
    }

    return 0;
}


sub _disConnect {
    my $self = shift;

    if( (ref($self->{'serverConn'}) ne 'Net::Telnet') || ($self->{'serverConn'}->eof()) ) {
        $self->_log( 'connexion non établie à l\'ObmSatellite de '.$self->getDescription(), 3 );
        return 0;
    }

    $self->_log( 'envoi de la commande: quit', 1 );
    $self->{'serverConn'}->print( 'quit' );
    while( !$self->{'serverConn'}->eof() && (my $line = $self->{'serverConn'}->getline(Timeout => 1)) ) {
        chomp($line);
        $self->_log( 'réponse: '.$line, 3 );
    }

    $self->_log( 'déconnexion de '.$self->getDescription(), 3 );
    $self->{'serverConn'}->close();    

    return 0;
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

    if( $self->_connect() ) {
        $self->_log( 'impossible d\'établir la connexion à '.$self->getDescription(), 3 );
        return 1;
    }

    my $cmd = 'smtpInConf: '.$self->{'host_name'};
    $self->_log( 'envoi de la commande: '.$cmd, 1 );

    $self->{'serverConn'}->print( $cmd );
    if( (!$self->{'serverConn'}->eof()) && (my $line = $self->{'serverConn'}->getline()) ) {
        chomp($line);
        $self->_log( 'réponse: '.$line, 1 );

        if( $line !~ /OK$/ ) {
            $errorCode = 1;
        }
    }

    $self->_disConnect();

    return $errorCode;
}
