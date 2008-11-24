package OBM::Ldap::ldapServer;

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

    $self->{'serverid'} = $serverId;
    $self->{'ldapServerConn'} = undef;

    if( $self->_getServerDesc() ) {
        $self->_log( 'problème lors de l\'initialisation du serveur LDAP', 1 );
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );

    if( ref( $self->{'ldapServerConn'} ) eq 'Net::LDAP' ) {
        $self->{'ldapServerConn'}->unbind();
    }else {
        $self->{'ldapServerConn'} = undef;
    }
}


sub _getServerDesc {
    my $self = shift;

    if( !defined($self->{'serverid'}) ) {
        $self->_log( 'identifiant de serveur non défini', 3 );
        return 1;
    }

    if( ref($self->{'serverid'}) || ($self->{'serverid'} !~ /$OBM::Parameters::regexp::regexp_server_id/) ) {
        $self->_log( 'identifiant de serveur incorrect', 3 );
        return 1;
    }

    require OBM::Parameters::common;
    $self->{'ldap_server'} = $OBM::Parameters::common::ldapServer;
    $self->{'ldap_admin_login'} = $OBM::Parameters::common::ldapAdminLogin;
    if( !($self->{'ldap_admin_dn'} = $self->_getAdminDn()) ) {
        $self->_log( 'obtention du DN de l\'administrateur LDAP impossible', 1 );
        return 1;
    }


    $self->{'ldap_description'} = $OBM::Parameters::common::ldapDescription;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    $self->_log( 'obtention du mot de passe de l\'utilisateur LDAP', 1 );
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
        $self->_log( 'pas de mot de passe pour l\'utilisateur LDAP defini en BD', 1 );
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
        $self->_log( 'obtention de plusieurs utilisateurs système \''.$self->{'ldap_admin_login'}.'\'', 4 );
        return undef;
    }elsif( $#{$systemUser} < 0 ) {
        $self->_log( 'utilisateurs système \''.$self->{'ldap_admin_login'}.'\' non trouvé', 4 );
        return undef;
    }

    $systemUser = $systemUser->[0];
    return $systemUser->getDnPrefix();
}


sub getId {
    my $self = shift;

    return $self->{'serverid'};
}


sub getDescription {
    my $self = shift;
    
    my $description = 'serveur LDAP d\'ID \''.$self->{'serverid'}.'\'';

    if( $self->{'ldap_description'} ) {
        $description .= ', \''.$self->{'ldap_description'}.'\'';
    }

    if( $self->{'ldap_server'} ) {
        $description .= ', \''.$self->{'ldap_server'}.'\'';
    }

    return $description;
}


sub getLdapConn {
    my $self = shift;

    $self->_connect();

    return $self->{'ldapServerConn'};
}


sub _connect {
    my $self = shift;

    if( ref( $self->{'ldapServerConn'} ) eq 'Net::LDAP' ) {
        $self->_log( 'connexion déjà établie à '.$self->getDescription(), 3 );
        return 0;
    }

    $self->_log( 'connexion au '.$self->getDescription(), 2 );

    require Net::LDAP;
    $self->{'ldapServerConn'} = Net::LDAP->new(
        $self->{'ldap_server'},
        debug => '0',
        timeout => '60',
        version => '3'
    );

    if( !$self->{'ldapServerConn'} ) {
        $self->{'ldapServerConn'} = undef;
        $self->_log( 'echec de connexion au '.$self->getDescription(), 2 );
        return 1;
    }

    $self->_log( 'authentification en tant que \''.$self->{'ldap_admin_login'}.'\' au '.$self->getDescription(), 2 );

    my $error;
    for( my $i=0; $i<=$#{$self->{'ldap_admin_dn'}}; $i++ ) {
        $self->_log( 'connexion avec le DN : '.$self->{'ldap_admin_dn'}->[$i], 4 );
        $error = $self->{'ldapServerConn'}->bind(
           $self->{'ldap_admin_dn'}->[$i],
           password => $self->{'ldap_admin_password'}
        );

        if( !$error->code ) {
            $self->_log( 'connexion à l\'annuaire LDAP établie', 2 );
            last;
        }elsif( $error->code ) {
            $self->_log( 'echec de l\'authentification : '.$error->error, 3 );
        }
    }

    if( $error->code ) {
        $self->_log( 'echec de l\'authentification : '.$error->error, 2 );
        $self->{'ldapServerConn'} = undef;
    }

    return 0;
}
