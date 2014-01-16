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


package OBM::Cyrus::cyrusServer;

$VERSION = '1.0';

use OBM::Tools::obmServer;
use OBM::Log::log;
@ISA = ('OBM::Tools::obmServer', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

require OBM::Parameters::regexp;
use OBM::Parameters::common;

use constant IMAP_TCP_CONN_TIMEOUT => 20;


sub new {
    my $class = shift;
    my( $serverId ) = @_;

    my $self = bless { }, $class;

    $self->{'serverId'} = $serverId;
    $self->{'ServerConn'} = undef;
    $self->{'deadStatus'} = 0;
    $self->{'serverType'} = 'Cyrus IMAP';

    if( $self->_getServerDesc() ) {
        $self->_log( 'problème lors de l\'initialisation du serveur '.$self->{'serverType'}, 0 );
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );

    eval{ $self->{'ServerConn'} = undef; };
}


sub _getServerDesc {
    my $self = shift;

    if( !defined($self->{'serverId'}) ) {
        $self->_log( 'identifiant de serveur non défini', 0 );
        return 1;
    }

    if( ref($self->{'serverId'})
        || ($self->{'serverId'} !~ /$OBM::Parameters::regexp::regexp_server_id/)
    ) {
        $self->_log( 'identifiant de serveur incorrect', 0 );
        return 1;
    }


    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    $self->_log( 'obtention du mot de passe de l\'utilisateur IMAP', 4 );
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
        $self->_log( 'obtention du serveur IMAP impossible', 0 );
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

    if( !defined($self->{'serverDesc'}->{'host_name'})
        || ($self->{'serverDesc'}->{'host_name'} !~ /$OBM::Parameters::regexp::regexp_hostname/)
    ) {
        $self->_log( 'nom d\'hôte du serveur non défini ou incorrect', 0 );
        return 1;
    }

    if( !defined($self->{'serverDesc'}->{'host_ip'})
        || ($self->{'serverDesc'}->{'host_ip'} !~ /$OBM::Parameters::regexp::regexp_ip/)
    ) {
        $self->_log( 'ip d\'hôte du serveur non défini ou incorrecte', 0 );
        return 1;
    }


    while( my $srvDesc = $sth->fetchrow_hashref() ) {
        push( @{$self->{'domainsId'}}, $srvDesc->{'mailserver_for_domain_id'} );
    }

    $self->_log( 'chargement : '.$self->getDescription(), 4 );
    $self->_log( 'administrateur du serveur IMAP \''.$self->{'serverDesc'}->{'cyrus_login'}.'\', \''.$self->{'serverDesc'}->{'cyrus_password'}.'\'', 4 );

    return 0;
}


sub getConn {
    my $self = shift;
    my( $domainId ) = @_;

    if( !$self->_checkDomainId($domainId) ) {
        $self->_connect();
    }else {
        return undef;
    }

    $self->_log( 'Obtention de la connexion IMAP à '.$self->getDescription(), 4 );
    return $self->{'ServerConn'};
}


sub _connect {
    my $self = shift;

    if( $self->getDeadStatus() ) {
        $self->_log( $self->getDescription().' est désactivé', 2 );
        return 1;
    }

    if( $self->_ping() ) {
        $self->_log( 'connexion déjà établie au '.$self->getDescription(), 5 );
        return 0;
    }

    $self->_log( 'connexion au '.$self->getDescription(), 3 );

    eval {
        local $SIG{ALRM} = sub {
            $self->_log( 'échec de connexion au '.$self->getDescription().' - le serveur n\'a pas répondu', 2 );
            delete($self->{'ServerConn'});
            die 'alarm'."\n";
        };

        alarm IMAP_TCP_CONN_TIMEOUT;
        $self->{'ServerConn'} = OBM::Cyrus::cyrusAdmin->new( $self->{'serverDesc'}->{'host_ip'} );
        alarm 0;
    };

    my @tempo = ( 1, 3, 5, 10, 20 );
    while( !$self->{'ServerConn'} ) {
        $self->_log( 'échec de connexion au '.$self->getDescription(), 2 ) if (defined($@) && ($@ ne 'alarm'."\n"));

        my $tempo = shift(@tempo);
        if( !defined($tempo) ) {
            last;
        }

        $self->_log( 'prochaine tentative dans '.$tempo.'s', 3 );
        sleep $tempo;

        eval {
            local $SIG{ALRM} = sub {
                $self->_log( 'échec de connexion au '.$self->getDescription().' - le serveur n\'a pas répondu', 2 );
                delete($self->{'ServerConn'});
                die 'alarm'."\n";
            };
    
            alarm IMAP_TCP_CONN_TIMEOUT;
            $self->{'ServerConn'} = OBM::Cyrus::cyrusAdmin->new( $self->{'serverDesc'}->{'host_ip'} );
            alarm 0;
        };
    }

    if( !$self->{'ServerConn'} ) {
        $self->{'ServerConn'} = undef;
        $self->_log( $self->getDescription().' désactivé car injoignable ', 0 );
        $self->_setDeadStatus();
        return 1;
    }

    $self->_log( 'authentification en tant que \''.$self->{'serverDesc'}->{'cyrus_login'}.'\' au '.$self->getDescription(), 3 );

    my %auth_opts = $self->_build_auth_opts();
    if( !$self->{'ServerConn'}->authenticate(%auth_opts) ) {
        $self->_log( 'échec d\'authentification au '.$self->getDescription(), 0 );
        return 1;
    }

    $self->_log( 'connexion au '.$self->getDescription().' établie', 4 );

    return 0;
}

sub _build_auth_opts {
    my ($self) = @_;

    my %auth_opts;
    my %basic_auth_opts = (
        -user =>        $self->{'serverDesc'}->{'cyrus_login'},
        -password =>    $self->{'serverDesc'}->{'cyrus_password'},
        -mechanism =>   'login');

    if (defined $OBM::Parameters::common::cyrusKeyAndCert) {
        my %tls_auth_opts = (
            -tlskey =>  $OBM::Parameters::common::cyrusKeyAndCert);
        if (defined $OBM::Parameters::common::cyrusCa) {
            $tls_auth_opts{'-cafile'} = $OBM::Parameters::common::cyrusCa;
        }
        if (defined $OBM::Parameters::common::cyrusCaPath) {
            $tls_auth_opts{'-capath'} = $OBM::Parameters::common::cyrusCaPath;
        }
        $self->_log("STARTTLS will be used with the certificate ".
            $OBM::Parameters::common::cyrusKeyAndCert, 3);
        %auth_opts = (%basic_auth_opts, %tls_auth_opts);
    }
    else {
        %auth_opts = %basic_auth_opts;
    }
    return %auth_opts;
}

sub _ping {
    my $self = shift;

    if( ref( $self->{'ServerConn'} ) ne 'OBM::Cyrus::cyrusAdmin' ) {
        return 0;
    }

    if( !defined($self->{'ServerConn'}->listmailbox('')) ) {
        $self->_log( 'la connexion à '.$self->getDescription().' a expirée', 2 );
        return 0;
    }

    return 1;
}


sub _checkDomainId {
    my $self = shift;
    my( $domainId ) = @_;

    if( !defined($domainId) ) {
        $self->_log( 'ID du domaine non défini', 1 );
        return 1;
    }elsif( $domainId !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$domainId.'\' incorrect', 1 );
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
        $self->_log( $self->getDescription().' est désactivé', 1 );
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
        $self->_log( 'ID du domaine non défini', 1 );
        return 0;
    }elsif( $domainId !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$domainId.'\' incorrect', 1 );
        return 0;
    }

    push( @{$self->{'disabledDomains'}}, $domainId );

    return 0;
}


sub _isDisable {
    my $self = shift;
    my( $domainId ) = @_;

    if( !defined($domainId) ) {
        $self->_log( 'ID du domaine non défini', 1 );
        return 0;
    }elsif( $domainId !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$domainId.'\' incorrect', 1 );
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
        $self->_log( 'pas d\'identifiant de connexion indiqué', 1 );
        return undef;
    }

    # Replace '@' char by '%' for authentication domain separator
    $login =~ s/@/%/;

    $self->_log( 'connexion au '.$self->getDescription().' en tant que \''.$self->{'serverDesc'}->{'cyrus_login'}.'\' pour le compte de \''.$login.'\'', 3 );

    use Cyrus::SIEVE::managesieve;
    my $sieveSrvConn = sieve_get_handle( $self->{'serverDesc'}->{'host_ip'}, sub{return $login}, sub{return $self->{'serverDesc'}->{'cyrus_login'}}, sub{return $self->{'serverDesc'}->{'cyrus_password'}}, sub{return undef} );

    if( !defined($sieveSrvConn) ) {
        $self->_log( 'probleme lors de l\'établissement de la connexion Sieve à '.$self->getDescription(), 0 );
        return undef;
    }

    $self->_log( 'Obtention de la connexion Sieve à '.$self->getDescription(), 3 );
    return $sieveSrvConn;
}



# This is done to prevent a non fatal uninitialized value on 'Cyrus::IMAP::Admin' global
# destruction
package OBM::Cyrus::cyrusAdmin;

use strict;

use Cyrus::IMAP::Admin;
use OBM::Log::log;
our @ISA = ('Cyrus::IMAP::Admin', 'OBM::Log::log');


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );
}


# Catch STDERR to drop messages print on this by Cyrus::IMAP::Admin
sub authenticate {
    my $self = shift;

    open (OLDERR, ">&STDERR");
    close(STDERR);
    my $returnCode = eval{ close(STDERR); return $self->SUPER::authenticate(@_); };
    open(STDERR, ">&OLDERR");
    close(OLDERR);

    return $returnCode;
}
