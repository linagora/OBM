package OBM::Ldap::ldapServer;

$VERSION = '1.0';

use OBM::Tools::obmServer;
@ISA = ('OBM::Tools::obmServer');


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
    $self->{'ldapServerConn'} = undef;
    $self->{'deadStatus'} = 0;
    $self->{'serverType'} = 'LDAP';

    if( $self->_getServerDesc() ) {
        $self->_log( 'problème lors de l\'initialisation du serveur '.$self->{'serverType'}, 1 );
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );

    if( ref( $self->{'ldapServerConn'} ) eq 'Net::LDAP' ) {
        # Trying to unbind silently...
        eval{ $self->{'ldapServerConn'}->unbind(); };
    }else {
        undef $self->{'ldapServerConn'};
    }
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

    require OBM::Parameters::common;
    $self->{'ldapUri'} = $OBM::Parameters::common::ldapServer;
    $self->{'ldapTls'} = $OBM::Parameters::common::ldapTls;

    $self->{'ldap_admin_login'} = $OBM::Parameters::common::ldapAdminLogin;
    if( !($self->{'ldap_admin_dn'} = $self->_getAdminDn()) ) {
        $self->_log( 'obtention du DN de l\'administrateur LDAP impossible', 1 );
        return 1;
    }


    $self->{'ldapDescription'} = $OBM::Parameters::common::ldapDescription;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    $self->_log( 'obtention du mot de passe de l\'utilisateur LDAP', 3 );
    my $query = 'SELECT usersystem.usersystem_password as ldap_admin_password
                    FROM UserSystem usersystem
                    WHERE usersystem.usersystem_login=\''.$self->{'ldap_admin_login'}.'\'
                    LIMIT 1';

    my $sth;
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'obtention du mot de passe de l\'administrateur LDAP impossible', 1 );
        return 1;
    }

    if( !(( $self->{'ldap_admin_password'} ) = $sth->fetchrow_array()) ) {
        $self->_log( 'pas de mot de passe pour l\'utilisateur LDAP defini en BD', 0 );
        $self->{'ldap_admin_password'} = '';
    }
    $sth->finish();

    $self->_log( 'chargement : '.$self->getDescription(), 1 );
    $self->_log( 'mot de passe de l\'utilisateur LDAP \''.$self->{'ldap_admin_password'}.'\'', 4 );

    return 0;
}


sub _getAdminDn {
    my $self = shift;

    if( !$self->{'ldap_admin_login'} ) {
        return undef;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    $self->_log( 'obtention du domaine global', 3 );
    my $query = 'SELECT domain_id,
                        domain_global,
                        domain_label,
                        domain_description,
                        domain_name,
                        domain_alias
                 FROM Domain
                 WHERE domain_global=true
                 LIMIT 1';

    my $sth;
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'chargement du domain global depuis la BD impossible', 4 );
        return undef;
    }

    my $globalDomain;
    if( !($globalDomain = $sth->fetchrow_hashref()) ) {
        $self->_log( 'domaine global non défini', 4 );
        return undef;
    }

    require OBM::Entities::obmDomain;
    $globalDomain = OBM::Entities::obmDomain->new( $globalDomain );
    $sth->finish();


    $self->_log( 'obtention de l\'utilisateur système \''.$self->{'ldap_admin_login'}.'\'', 4 );
    $query = 'SELECT *
              FROM UserSystem
              WHERE usersystem_login=\''.$self->{'ldap_admin_login'}.'\'';

    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'chargement de l\'utilisateur système \''.$self->{'ldap_admin_login'}.'\'', 4 );
        return undef;
    }

    my $systemUser;
    while( my $userSystemDesc = $sth->fetchrow_hashref() ) {
        require OBM::Entities::obmSystemUser;
        if( my $adminLdap = OBM::Entities::obmSystemUser->new($globalDomain, $userSystemDesc) ) {
            push( @{$systemUser}, $adminLdap );
        }
    }

    if( $#{$systemUser} > 0 ) {
        $self->_log( 'obtention de plusieurs utilisateurs système \''.$self->{'ldap_admin_login'}.'\'', 1 );
        return undef;
    }elsif( $#{$systemUser} < 0 ) {
        $self->_log( 'utilisateurs système \''.$self->{'ldap_admin_login'}.'\' non trouvé', 0 );
        return undef;
    }

    $systemUser = $systemUser->[0];
    return $systemUser->getDnPrefix();
}


sub getConn {
    my $self = shift;

    $self->_connect();

    return $self->{'ldapServerConn'};
}


sub _connect {
    my $self = shift;

    if( $self->getDeadStatus() ) {
        $self->_log( $self->getDescription().' est désactivé', 0 );
        return 1;
    }

    if( ref( $self->{'ldapServerConn'} ) eq 'Net::LDAP' ) {
        $self->_log( 'connexion déjà établie à '.$self->getDescription(), 3 );
        return 0;
    }

    $self->_log( 'connexion au '.$self->getDescription(), 2 );

    my @tempo = ( 1, 3, 5, 10, 20, 30 );
    require Net::LDAP;
    while( !($self->{'ldapServerConn'} = Net::LDAP->new( $self->{'ldapUri'}, debug => '0', timeout => '60', version => '3' )) ) {
        $self->_log( 'échec de connexion au '.$self->getDescription(), 0 );

        my $tempo = shift(@tempo);
        if( !defined($tempo) ) {
            last;
        }

        $self->_log( 'prochaine tentative dans '.$tempo.'s', 3 );
        sleep $tempo;
    }

    if( !$self->{'ldapServerConn'} ) {
        $self->{'ldapServerConn'} = undef;
        $self->_log( $self->getDescription().' désactivé car injoignable ', 0 );
        $self->_setDeadStatus();
        return 1;
    }

    use Net::LDAP qw(LDAP_CONFIDENTIALITY_REQUIRED) ;
    if( $self->{'ldapTls'} =~ /^(may|encrypt)$/ ) {
        my $error = $self->{'ldapServerConn'}->start_tls( verify => 'none' );

        if( $error->code && ($self->{'ldapTls'} eq 'encrypt') ) {
            $self->_log( 'erreur fatale au start TLS : '.$error->error, 0 );
            $self->_log( 'l\'automate (\'ldapTls\'='.$self->{'ldapTls'}.') nécessite une connexion TLS', 0 );
            $self->_log( $self->getDescription().' désactivé', 0 );
            $self->_setDeadStatus();
            return 1;
        }

        if( $error->code() && ($self->{'ldapTls'} eq 'may') ) {
            $self->_log( 'erreur au start TLS : '.$error->error, 0 );
            $self->_log( 'TLS facultatif (\'ldapTls\'='.$self->{'ldapTls'}.'), erreur non fatale', 0 );
            $self->{'ldapTls'} = 'none';
        }
    }

    my $ldapLogin = $self->{'ldap_admin_login'};
    my $ldapDnLogin = $self->{'ldap_admin_dn'};
    my $ldapPassword = $self->{'ldap_admin_password'};
    if( defined($self->{'ldap_user_dnlogin'}) && defined($self->{'ldap_user_password'}) ) {
        $ldapLogin = $self->{'ldap_user_dnlogin'};
        $ldapDnLogin = [ $self->{'ldap_user_dnlogin'} ];
        $ldapPassword = $self->{'ldap_user_password'};
    }

    $self->_log( 'authentification en tant que \''.$ldapLogin.'\' au '.$self->getDescription(), 2 );

    my $error;
    for( my $i=0; $i<=$#{$ldapDnLogin}; $i++ ) {
        $self->_log( 'connexion avec le DN : '.$ldapDnLogin->[$i], 2 );
        $error = $self->{'ldapServerConn'}->bind(
           $ldapDnLogin->[$i],
           password => $ldapPassword
        );

        if( !$error->code ) {
            $self->_log( 'connexion à l\'annuaire LDAP établie', 2 );
            last;
	    }elsif( $error->code == LDAP_CONFIDENTIALITY_REQUIRED ) {
	        $self->_log( 'erreur fatale : start TLS nécessaire pour le serveur LDAP '.$self->getDescription(), 0 );
            $self->_log( 'l\'automate n\'a pas droit de faire du TLS (\'ldapTls\'='.$self->{'ldapTls'}.')', 0 );
            $self->_log( $self->getDescription().' désactivé', 0 );
            $self->_setDeadStatus();
            return 1;
        }elsif( $error->code ) {
            $self->_log( 'echec de l\'authentification : '.$error->error, 3 );
        }
    }

    if(( $error->code ) && ( $error->code ne LDAP_CONFIDENTIALITY_REQUIRED )) {
        $self->_log( 'echec de l\'authentification : '.$error->error, 0 );
        $self->{'ldapServerConn'} = undef;
    }

    return 0;
}


sub setDnLogin {
    my $self = shift;
    my( $login ) = @_;

    $self->{'ldap_user_dnlogin'} = $login;

    return 0;
}


sub setPasswd {
    my $self = shift;
    my( $password ) = @_;

    $self->{'ldap_user_password'} = $password;

    return 0;
}


# Reset LDAP connection and user/password
# On next connect, LDAP will be bind as Admin if no call to setDnLogin and
# setPasswd methods
sub resetConn {
    my $self = shift;

    if( ref( $self->{'ldapServerConn'} ) eq 'Net::LDAP' ) {
        # Trying to unbind silently...
        eval{ $self->{'ldapServerConn'}->unbind(); };
        $self->{'ldapServerConn'} = undef;
    }

    delete($self->{'ldap_user_dnlogin'});
    delete($self->{'ldap_user_password'});
    $self->{'deadStatus'} = 0;

    return 0;
}


sub _setDeadStatus {
    my $self = shift;

    $self->{'ldapServerConn'} = undef;
    return $self->SUPER::_setDeadStatus();
}
