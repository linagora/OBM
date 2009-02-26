package OBM::Cyrus::cyrusServer;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(_log dump);


sub new {
    my $class = shift;
    my( $serverId ) = @_;

    my $self = bless { }, $class;

    $self->{'serverId'} = $serverId;
    $self->{'cyrusServerConn'} = undef;
    $self->{'deadStatus'} = 0;

    if( $self->_getServerDesc() ) {
        $self->_log( 'problème lors de l\'initialisation du serveur Cyrus', 1 );
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );

    eval{ $self->{'cyrusServerConn'} = undef; };
}


sub _getServerDesc {
    my $self = shift;

    if( !defined($self->{'serverId'}) ) {
        $self->_log( 'identifiant de serveur non défini', 3 );
        return 1;
    }

    if( ref($self->{'serverId'}) || ($self->{'serverId'} !~ /$OBM::Parameters::regexp::regexp_server_id/) ) {
        $self->_log( 'identifiant de serveur incorrect', 3 );
        return 1;
    }


    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    $self->_log( 'obtention du mot de passe de l\'utilisateur IMAP', 1 );
    my $query = 'SELECT     host.*,
                            usersystem.usersystem_login AS cyrus_login,
                            usersystem.usersystem_password AS cyrus_password,
                            domainentity_domain_id AS mailserver_for_domain_id
                    FROM Host host
                    INNER JOIN ServiceProperty ON '.$dbHandler->castAsInteger('serviceproperty_value').'=host_id AND serviceproperty_service=\'mail\' AND serviceproperty_property=\'imap\'
                    INNER JOIN DomainEntity ON domainentity_entity_id=serviceproperty_entity_id
                    LEFT JOIN UserSystem usersystem ON usersystem.usersystem_login=\''.$OBM::Parameters::common::cyrusAdminLogin.'\'
                    WHERE host_id='.$self->{'serverId'};

    my $sth;
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'obtention du serveur IMAP impossible', 1 );
        return 1;
    }

    if( !($self->{'serverDesc'} = $sth->fetchrow_hashref()) ) {
        $self->_log( 'le serveur d\'ID \''.$self->{'serverId'}.'\' n\'existe pas, ou n\'est pas un serveur IMAP', 0 );
        return 1;
    }else {
        push( @{$self->{'domainsId'}}, $self->{'serverDesc'}->{'mailserver_for_domain_id'} );
    }


    # Some checks
    if( !defined($self->{'serverDesc'}->{'cyrus_login'}) ) {
        $self->_log( 'administrateur du serveur non défini', 0 );
        return 1;
    }

    if( !defined($self->{'serverDesc'}->{'cyrus_password'}) ) {
        $self->_log( 'mot de passe de l\'administrateur du serveur non défini', 0 );
        return 1;
    }

    if( !defined($self->{'serverDesc'}->{'host_name'}) ) {
        $self->_log( 'nom d\'hôte du serveur non défini', 0 );
        return 1;
    }

    if( !defined($self->{'serverDesc'}->{'host_ip'}) ) {
        $self->_log( 'ip d\'hôte du serveur non défini', 0 );
        return 1;
    }


    while( my $srvDesc = $sth->fetchrow_hashref() ) {
        push( @{$self->{'domainsId'}}, $srvDesc->{'mailserver_for_domain_id'} );
    }

    $self->_log( 'chargement : '.$self->getDescription(), 1 );
    $self->_log( 'administrateur du serveur IMAP \''.$self->{'serverDesc'}->{'cyrus_login'}.'\', \''.$self->{'serverDesc'}->{'cyrus_password'}.'\'', 4 );

    return 0;
}


sub getId {
    my $self = shift;

    return $self->{'serverId'};
}


sub getDescription {
    my $self = shift;
    
    my $description = 'serveur Cyrus d\'ID \''.$self->{'serverId'}.'\'';

    if( $self->{'serverDesc'}->{'host_description'} ) {
        $description .= ', \''.$self->{'serverDesc'}->{'host_description'}.'\'';
    }

    if( $self->{'serverDesc'}->{'host_ip'} ) {
        $description .= ', \''.$self->{'serverDesc'}->{'host_ip'}.'\'';
    }

    return $description;
}


sub getCyrusConn {
    my $self = shift;
    my( $domainId ) = @_;

    if( !$self->_checkDomainId($domainId) ) {
        $self->_connect();
    }else {
        return undef;
    }

    $self->_log( 'Obtention de la connexion IMAP à '.$self->getDescription(), 3 );
    return $self->{'cyrusServerConn'};
}


sub _connect {
    my $self = shift;

    if( $self->getDeadStatus() ) {
        $self->_log( $self->getDescription().' est désactivé', 0 );
        return 1;
    }

    if( $self->_ping() ) {
        $self->_log( 'connexion déjà établie au '.$self->getDescription(), 4 );
        return 0;
    }

    $self->_log( 'connexion au '.$self->getDescription(), 2 );

    my @tempo = ( 1, 3, 5, 10, 20, 30 );
    while( !($self->{'cyrusServerConn'} = OBM::Cyrus::cyrusAdmin->new( $self->{'serverDesc'}->{'host_ip'} )) ) {
        $self->_log( 'échec de connexion au '.$self->getDescription(), 0 );

        my $tempo = shift(@tempo);
        if( !defined($tempo) ) {
            last;
        }

        $self->_log( 'prochaine tentative dans '.$tempo.'s', 3 );
        sleep $tempo;
    }

    if( !$self->{'cyrusServerConn'} ) {
        $self->{'cyrusServerConn'} = undef;
        $self->_log( $self->getDescription().' désactivé car injoignable ', 0 );
        $self->_setDeadStatus();
        return 1;
    }

    $self->_log( 'authentification en tant que \''.$self->{'serverDesc'}->{'cyrus_login'}.'\' au '.$self->getDescription(), 2 );

    if( !$self->{'cyrusServerConn'}->authenticate( -user=>$self->{'serverDesc'}->{'cyrus_login'}, -password=>$self->{'serverDesc'}->{'cyrus_password'}, -mechanism=>'login') ) {
        $self->_log( 'échec d\'authentification au '.$self->getDescription(), 0 );
        return 1;
    }

    $self->_log( 'connexion au '.$self->getDescription().' établie', 2 );

    return 0;
}


sub _ping {
    my $self = shift;

    if( ref( $self->{'cyrusServerConn'} ) ne 'OBM::Cyrus::cyrusAdmin' ) {
        return 0;
    }

    if( !defined($self->{'cyrusServerConn'}->listmailbox('')) ) {
        $self->_log( 'la connexion à '.$self->getDescription().' a expirée', 2 );
        return 0;
    }

    return 1;
}


sub _checkDomainId {
    my $self = shift;
    my( $domainId ) = @_;

    if( !defined($domainId) ) {
        $self->_log( 'ID du domaine non défini', 3 );
        return 1;
    }elsif( $domainId !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$domainId.'\' incorrect', 4 );
        return 1;
    }elsif( $self->_isDisable($domainId) ) {
        return 1;
    }

    my $notFound = 1;
    for( my $i=0; $i<=$#{$self->{'domainsId'}}; $i++ ) {
        if( $self->{'domainsId'}->[$i] == $domainId ) {
            $notFound = 0;
            last;
        }
    }

    if( $notFound ) {
        $self->_log( $self->getDescription().' n\'est pas un serveur du domaine \''.$domainId.'\'', 0 );
    }

    return $notFound;
}


sub getCyrusServerName {
    my $self = shift;

    return $self->{'serverDesc'}->{'host_name'};
}


sub getCyrusServerIp {
    my $self = shift;

    return $self->{'serverDesc'}->{'host_ip'};
}


sub updateCyrusPartitions {
    my $self = shift;
    my( $domainId ) = @_;

    if( $self->getDeadStatus() ) {
        $self->_log( $self->getDescription().' est désactivé', 0 );
        return 1;
    }

    if( $self->_checkDomainId($domainId) ) {
        return 1;
    }

    require OBM::Cyrus::cyrusRemoteEngine;
    my $partitionUpdater = OBM::Cyrus::cyrusRemoteEngine->instance();

    if( !defined($partitionUpdater) ) {
        return 0;
    }

    if( $partitionUpdater->addCyrusPartition( $self ) ) {
        $self->_log( 'Problème à la mise à jour des partitions de '.$self->getDescription(), 0 );
        $self->_setDisable( $domainId );
        return 1;
    }

    return 0;
}


sub _setDisable {
    my $self = shift;
    my( $domainId ) = @_;

    if( !defined($domainId) ) {
        $self->_log( 'ID du domaine non défini', 3 );
        return 0;
    }elsif( $domainId !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$domainId.'\' incorrect', 4 );
        return 0;
    }

    push( @{$self->{'disabledDomains'}}, $domainId );

    return 0;
}


sub _isDisable {
    my $self = shift;
    my( $domainId ) = @_;

    if( !defined($domainId) ) {
        $self->_log( 'ID du domaine non défini', 3 );
        return 0;
    }elsif( $domainId !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$domainId.'\' incorrect', 4 );
        return 0;
    }

    if( !defined($self->{'disabledDomains'}) ) {
        return 0;
    }

    for( my $i=0; $i<=$#{$self->{'disabledDomains'}}; $i++ ) {
        if( $self->{'disabledDomains'}->[$i] == $domainId ) {
            $self->_log( $self->getDescription().' est désactivé pour le domaine d\'ID \''.$domainId.'\'', 0 );
            return 1;
        }
    }

    return 0;
}


sub getSieveServerConn {
    my $self = shift;
    my( $domainId, $login ) = @_;

    if( $self->_checkDomainId($domainId) ) {
        return undef;
    }

    if( !defined($login) ) {
        $self->_log( 'pas d\'identifiant de connexion indiqué', 3 );
        return undef;
    }

    # Replace '@' char by '%' for authentication domain separator
    $login =~ s/@/%/;

    $self->_log( 'connexion au '.$self->getDescription().' en tant que \''.$self->{'serverDesc'}->{'cyrus_login'}.'\' pour le compte de \''.$login.'\'', 2 );

    use Cyrus::SIEVE::managesieve;
    my $sieveSrvConn = sieve_get_handle( $self->{'serverDesc'}->{'host_ip'}, sub{return $login}, sub{return $self->{'serverDesc'}->{'cyrus_login'}}, sub{return $self->{'serverDesc'}->{'cyrus_password'}}, sub{return undef} );

    if( !defined($sieveSrvConn) ) {
        $self->_log( 'probleme lors de l\'établissement de la connexion Sieve à '.$self->getDescription(), 0 );
        return undef;
    }

    $self->_log( 'Obtention de la connexion Sieve à '.$self->getDescription(), 2 );
    return $sieveSrvConn;
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



# This is done to prevent a non fatal uninitialized value on 'Cyrus::IMAP::Admin' global
# destruction
package OBM::Cyrus::cyrusAdmin;

use strict;

use Cyrus::IMAP::Admin;
our @ISA = qw(Cyrus::IMAP::Admin);

use OBM::Tools::commonMethods qw(_log dump);


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}
