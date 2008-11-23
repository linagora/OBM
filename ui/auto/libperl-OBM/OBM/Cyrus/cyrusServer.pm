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

    if( $self->_getServerDesc() ) {
        $self->_log( 'problème lors de l\'initialisation du serveur LDAP', 1 );
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
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

    $self->_log( 'obtention du mot de passe de l\'utilisateur LDAP', 1 );
    my $query = 'SELECT     host.*,
                            usersystem.usersystem_login AS cyrus_login,
                            usersystem.usersystem_password AS cyrus_password,
                            domainmailserver.domainmailserver_domain_id AS mailserver_for_domain_id
                    FROM Host host, MailServer mailserver
                    LEFT JOIN DomainMailServer domainmailserver ON domainmailserver.domainmailserver_mailserver_id=mailserver.mailserver_id
                    LEFT JOIN UserSystem usersystem ON usersystem.usersystem_login=\''.$OBM::Parameters::common::cyrusAdminLogin.'\'
                    WHERE domainmailserver.domainmailserver_role=\'imap\' AND host.host_id=mailserver.mailserver_host_id AND mailserver.mailserver_id='.$self->{'serverId'};

    my $sth;
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'obtention du serveur IMAP impossible', 1 );
        return 1;
    }

    if( !($self->{'serverDesc'} = $sth->fetchrow_hashref()) ) {
        $self->_log( 'le serveur d\'ID \''.$self->{'serverId'}.'\' n\'existe pas, ou n\'est pas un serveur IMAP', 2 );
        return 1;
    }else {
        push( @{$self->{'domainsId'}}, $self->{'serverDesc'}->{'mailserver_for_domain_id'} );
    }


    # Some checks
    if( !defined($self->{'serverDesc'}->{'cyrus_login'}) ) {
        $self->_log( 'administrateur du serveur non défini', 3 );
        return 1;
    }

    if( !defined($self->{'serverDesc'}->{'cyrus_password'}) ) {
        $self->_log( 'mot de passe de l\'administrateur du serveur non défini', 3 );
        return 1;
    }

    if( !defined($self->{'serverDesc'}->{'host_ip'}) ) {
        $self->_log( 'nom d\'hôte du serveur non défini', 3 );
        return 1;
    }


    while( my $srvDesc = $sth->fetchrow_hashref() ) {
        push( @{$self->{'domainsId'}}, $self->{'serverDesc'}->{'mailserver_for_domain_id'} );
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
    
    my $description = 'serveur IMAP d\'ID \''.$self->{'serverId'}.'\'';

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

    return $self->{'cyrusServerConn'};
}


sub _connect {
    my $self = shift;

    if( ref( $self->{'cyrusServerConn'} ) eq 'Cyrus::IMAP::Admin' ) {
        $self->_log( 'connexion déjà établie au '.$self->getDescription(), 4 );
        return 0;
    }

    $self->_log( 'connexion au '.$self->getDescription(), 2 );

    require Cyrus::IMAP::Admin;
    $self->{'cyrusServerConn'} = Cyrus::IMAP::Admin->new( $self->{'serverDesc'}->{'host_ip'} );

    if( !$self->{'cyrusServerConn'} ) {
        $self->{'cyrusServerConn'} = undef;
        $self->_log( 'échec de connexion au '.$self->getDescription(), 2 );
        return 1;
    }

    $self->_log( 'authentification en tant que \''.$self->{'serverDesc'}->{'cyrus_login'}.'\' au '.$self->getDescription(), 2 );

    if( !$self->{'cyrusServerConn'}->authenticate( -user=>$self->{'serverDesc'}->{'cyrus_login'}, -password=>$self->{'serverDesc'}->{'cyrus_password'}, -mechanism=>'login') ) {
        $self->_log( 'échec d\'authentification au '.$self->getDescription(), 2 );
        return 1;
    }

    $self->_log( 'connexion au '.$self->getDescription().' établie', 2 );

    return 0;
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
    }

    my $notFound = 1;
    for( my $i=0; $i<=$#{$self->{'domainsId'}}; $i++ ) {
        if( $self->{'domainsId'}->[$i] == $domainId ) {
            $notFound = 0;
            last;
        }
    }

    if( $notFound ) {
        $self->_log( $self->getDescription().' n\'est pas un serveur du domaine \''.$domainId.'\'', 2 );
    }

    return $notFound;
}


sub getCyrusServerIp {
    my $self = shift;

    return $self->{'serverDesc'}->{'host_ip'};
}
