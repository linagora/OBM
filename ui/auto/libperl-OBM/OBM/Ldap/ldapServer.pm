#################################################################################
# Copyright (C) 2011-2014 Linagora
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option) any
# later version, provided you comply with the Additional Terms applicable for OBM
# software by Linagora pursuant to Section 7 of the GNU Affero General Public
# License, subsections (b), (c), and (e), pursuant to which you must notably (i)
# retain the displaying by the interactive user interfaces of the “OBM, Free
# Communication by Linagora” Logo with the “You are using the Open Source and
# free version of OBM developed and supported by Linagora. Contribute to OBM R&D
# by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
# links between OBM and obm.org, between Linagora and linagora.com, as well as
# between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
# from infringing Linagora intellectual property rights over its trademarks and
# commercial brands. Other Additional Terms apply, see
# <http://www.linagora.com/licenses/> for more details.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License and
# its applicable Additional Terms for OBM along with this program. If not, see
# <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
# version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
# applicable to the OBM software.
#################################################################################


package OBM::Ldap::ldapServer;

$VERSION = '1.0';

use OBM::Tools::obmServer;
use OBM::Log::log;
@ISA = ('OBM::Tools::obmServer', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


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

    $self->_log( 'suppression de l\'objet', 5 );

    if( ref( $self->{'ldapServerConn'} ) eq 'Net::LDAP' ) {
        # Trying to unbind silently...
        eval{ $self->{'ldapServerConn'}->disconnect(); };
    }else {
        undef $self->{'ldapServerConn'};
    }
}


sub _getServerDesc {
    my $self = shift;

    if( !defined($self->{'serverId'}) ) {
        $self->_log( 'identifiant de serveur non défini', 1 );
        return 1;
    }

    if( ref($self->{'serverId'}) || ($self->{'serverId'} !~ /$OBM::Parameters::regexp::regexp_server_id/) ) {
        $self->_log( 'identifiant de serveur incorrect', 1 );
        return 1;
    }

    require OBM::Parameters::common;
    $self->{'ldapUri'} = $OBM::Parameters::common::ldapServer;
    $self->{'ldapTls'} = $OBM::Parameters::common::ldapTls;
    $self->{'ldapConnectionPooling'} = $OBM::Parameters::common::ldapConnectionPooling;

    $self->{'ldap_admin_login'} = $OBM::Parameters::common::ldapAdminLogin;
    if( !($self->{'ldap_admin_dn'} = $self->_getAdminDn()) ) {
        $self->_log( 'obtention du DN de l\'administrateur LDAP impossible', 1 );
        return 1;
    }


    $self->{'ldapDescription'} = $OBM::Parameters::common::ldapDescription;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    $self->_log( 'obtention du mot de passe de l\'utilisateur LDAP', 4 );
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

    $self->_log( 'chargement : '.$self->getDescription(), 4 );
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

    $self->_log( 'obtention du domaine global', 4 );
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
        $self->_log( 'chargement du domain global depuis la BD impossible', 1 );
        return undef;
    }

    my $globalDomain;
    if( !($globalDomain = $sth->fetchrow_hashref()) ) {
        $self->_log( 'domaine global non défini', 1 );
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
        $self->_log( 'chargement de l\'utilisateur système \''.$self->{'ldap_admin_login'}.'\'', 3 );
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
        $self->_log( 'obtention de plusieurs utilisateurs système \''.$self->{'ldap_admin_login'}.'\'', 3 );
        return undef;
    }elsif( $#{$systemUser} < 0 ) {
        $self->_log( 'utilisateurs système \''.$self->{'ldap_admin_login'}.'\' non trouvé', 1 );
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
        $self->_log( $self->getDescription().' est désactivé', 2 );
        return 1;
    }

    if( $self->{'ldapConnectionPooling'} == 1 && ref( $self->{'ldapServerConn'} ) eq 'Net::LDAP' ) {
        $self->_log( 'connexion déjà établie à '.$self->getDescription(), 4 );
        return 0;
    }

    $self->_log( 'connexion au '.$self->getDescription(), 3 );

    my @tempo = ( 1, 3, 5, 10, 20 );
    require Net::LDAP;
    while( !($self->{'ldapServerConn'} = Net::LDAP->new( $self->{'ldapUri'}, debug => '0', timeout => '60', version => '3' )) ) {
        $self->_log( 'échec de connexion au '.$self->getDescription(), 1 );

        my $tempo = shift(@tempo);
        if( !defined($tempo) ) {
            last;
        }

        $self->_log( 'prochaine tentative dans '.$tempo.'s', 3 );
        sleep $tempo;
    }

    if( !$self->{'ldapServerConn'} ) {
        $self->_log( $self->getDescription().' désactivé car injoignable ', 2 );
        $self->_setDeadStatus();
        return 1;
    }

    use Net::LDAP qw(LDAP_EXTENSION_START_TLS);
    my $ldapDse = $self->{'ldapServerConn'}->root_dse();
    if( !defined($ldapDse) ) {
        $self->_log( 'impossible d\'interroger le root DSE de '.$self->getDescription().'. Vérifiez vos ACLs', 1 );
        $self->_log( 'impossible de vérifier que '.$self->getDescription().' a été compilé avec le support du TLS/SSL', 1 );

    }elsif( !$ldapDse->supported_extension(LDAP_EXTENSION_START_TLS) ) {
        $self->_log( 'le serveur LDAP n\'a pas le support du TLS/SSL activé !', 2 );
        $self->{'ldapTls'} = 'none';
    }

    use Net::LDAP qw(LDAP_CONFIDENTIALITY_REQUIRED);
    if( $self->{'ldapTls'} =~ /^(may|encrypt)$/ ) {
        my $error = $self->{'ldapServerConn'}->start_tls( verify => 'none' );

        if( $error->code() && ($self->{'ldapTls'} eq 'encrypt') ) {
            $self->_log( 'erreur fatale au start TLS : '.$error->error, 0 );
            $self->_log( 'l\'automate (\'ldapTls\'='.$self->{'ldapTls'}.') nécessite une connexion TLS', 3 );
            $self->_log( $self->getDescription().' désactivé', 2 );
            $self->_setDeadStatus();
            return 1;
        }

        if( $error->code() && ($self->{'ldapTls'} eq 'may') ) {
            $self->_log( 'erreur au start TLS : '.$error->error, 1 );
            $self->_log( 'TLS facultatif (\'ldapTls='.$self->{'ldapTls'}.'\'), re-connexion sans TLS', 3 );
            $self->{'ldapTls'} = 'none';

            $self->{'ldapServerConn'} = undef;
            return $self->_connect();
        }

        if( !$error->code() ) {
            $self->_log( 'session TLS établie', 3 );
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

    $self->_log('authentification en tant que \''.$ldapLogin.'\' au '.$self->getDescription(), 4);

    my $error;
    for( my $i=0; $i<=$#{$ldapDnLogin}; $i++ ) {
        $self->_log( 'connexion avec le DN : '.$ldapDnLogin->[$i], 3 );
        $error = $self->{'ldapServerConn'}->bind(
           $ldapDnLogin->[$i],
           password => $ldapPassword
        );

        if( !$error->code ) {
            $self->_log( 'connexion à l\'annuaire LDAP établie', 4 );
            last;
        }elsif( $error->code == LDAP_CONFIDENTIALITY_REQUIRED ) {
            $self->_log( 'erreur fatale : start TLS nécessaire pour le serveur LDAP '.$self->getDescription(), 0 );
            $self->_log( 'l\'automate n\'a pas droit de faire du TLS (\'ldapTls\'='.$self->{'ldapTls'}.')', 3 );
            $self->_log( $self->getDescription().' désactivé', 2 );
            $self->_setDeadStatus();
            return 1;
        }elsif( $error->code ) {
            $self->_log( 'echec de l\'authentification : '.$error->error, 1 );
        }
    }

    if(( $error->code ) && ( $error->code ne LDAP_CONFIDENTIALITY_REQUIRED )) {
        $self->_log( 'echec de l\'authentification : '.$error->error, 1 );
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
