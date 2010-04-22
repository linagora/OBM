package ObmSatellite::Services::CYRUS;

$VERSION = '1.0';

$debug = 1;

use Class::Singleton;
use ObmSatellite::Log::log;
@ISA = qw(Class::Singleton ObmSatellite::Log::log);

use 5.006_001;
require Exporter;
use strict;

require Config::IniFiles;

use constant OBM_CONF => '/etc/obm/obm_conf.ini';
# LDAP server dead time (s)
use constant DEAD_STATUS_TIME => 180;
use constant IMAP_TCP_CONN_TIMEOUT => 20;
use constant CYRUS_SERVER => 'localhost';
use constant CYRUS_ADMIN => 'cyrus';


sub _new_instance {
    my $class = shift;
    my( $confFile ) = @_;

    my $self = bless { }, $class;

    my $cyrusDesc = $self->_loadDefaultConf();
    my $cyrusDescConfFile = $self->_loadConfFile( $confFile );

    if( !defined($cyrusDesc) && !defined($cyrusDescConfFile) ) {
        $self->_log( 'No Cyrus server configuration', 1 );
        return undef;
    }elsif( defined($cyrusDescConfFile) ) {
        while( my( $option, $value ) = each(%{$cyrusDescConfFile}) ) {
            $cyrusDesc->{$option} = $value;
        }
    }


    my $regexp_ip = '^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$';
    my $regexp_hostname = '^[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,6}){0,1}$';

    # Load CYRUS configuration options
    if( defined($cyrusDesc->{'cyrus_server'}) && (($cyrusDesc->{'cyrus_server'} =~ $regexp_ip || ($cyrusDesc->{'cyrus_server'} =~ /$regexp_hostname/)) ) ) {
        my %cyrusDesc;

        $self->{'cyrus_server'} = $cyrusDesc->{'cyrus_server'};
        $self->{'cyrus_admin'} = $cyrusDesc->{'cyrus_admin'};
        if( !$self->_getCyrusAdminPasswd( $confFile ) ) {
            $self->_log( 'Can\'t get Cyrus admin \''.$self->{'cyrus_admin'}.'\' password from LDAP', 0 );
            return undef;
        }

    }else {
        $self->_log( 'cyrus_server not defined or incorrect in configuration file', 0 );
        return undef;
    }

    return $self;
}


sub _loadDefaultConf {
    my $self = shift;
    my %cyrusDesc;

    $cyrusDesc{'cyrus_server'} = CYRUS_SERVER;
    $cyrusDesc{'cyrus_admin'} = CYRUS_ADMIN;

    return \%cyrusDesc;
}


sub _loadConfFile {
    my $self = shift;
    my( $confFile ) = @_;
    my %cyrusDesc;

    if( !(-f $confFile && -r $confFile) ) {
        return undef;
    }

    my $cfgFile = Config::IniFiles->new( -file => $confFile );
    return undef if !defined($cfgFile);

    my $iniValue = $cfgFile->val( 'server', 'cyrusServer' );
    $cyrusDesc{'cyrus_server'} = $iniValue if $iniValue;

    $iniValue = $cfgFile->val( 'server', 'cyrusAdmin' );
    $cyrusDesc{'cyrus_admin'} = $iniValue if $iniValue;

    return \%cyrusDesc;
}


sub _getCyrusAdminPasswd {
    my $self = shift;
    my( $confFile ) = @_;

    my $ldapFilter = '(&(uid='.$self->{'cyrus_admin'}.')(objectclass=obmSystemUser))';

    my $ldapEntity = $self->_getLdapValues(
        $ldapFilter,
        [ 'userPassword' ],
        $confFile );

    if( $#{$ldapEntity} != 0 ) {
        $self->_log( 'Can\'t get \''.$self->{'cyrus_admin'}.'\' admin password from LDAP', 0 );
        return 0;
    }
    $self->{'cyrus_admin_password'} = $ldapEntity->[0]->get_value('userPassword');

    $self->_log( 'Cyrus admin login \''.$self->{'cyrus_admin'}.'\', password \''.$self->{'cyrus_admin_password'}.'\'', 4 );

    return 1;
}


sub _getLdapValues {
    my $self = shift;
    my( $ldapFilter, $ldapAttributes, $confFile ) = @_;

    eval {
        require ObmSatellite::Services::LDAP;
    } or ($self->_log( 'Unable to load LDAP service', 0 ) && return []);
    my $ldapServer = ObmSatellite::Services::LDAP->instance($confFile);

    if( !defined($ldapServer) ) {
        $self->_log( 'Unable to load LDAP service', 1 );
        return [];
    }

    my $ldapConn = $ldapServer->getConn() or return [];

    if( ref($ldapAttributes) ne 'ARRAY' ) {
        $self->_log( 'LDAP attributes must be an ARRAY ref', 4 );
        return [];
    }

    $self->_log( 'Search default LDAP server root, filter '.$ldapFilter, 5 );

    my $ldapResult;
    $ldapResult = $ldapConn->search(
                        scope => 'sub',
                        filter => $ldapFilter,
                        attrs => $ldapAttributes
                    );

    if( $ldapResult->is_error() ) {
        $self->_log( 'LDAP search fail on error : '.$ldapResult->error(), 1 );
        return [];
    }

    my @results = $ldapResult->entries();

    # Explicit disconnect LDAP service before obmSatellite daemon fork
    $ldapServer->disconnect();

    return \@results;
}


sub DESTROY {
    my $self = shift;

    $self->disconnect();
}


sub disconnect {
    my $self = shift;

    $self->_log( 'Disconnect from '.$self->getDescription(), 4 );
    eval{ $self->{'ServerConn'} = undef; };
}


sub getDescription {
    my $self = shift;

    return 'Cyrus server \''.$self->{'cyrus_server'}.'\'';
}


sub getConn {
    my $self = shift;

    if( $self->getDeadStatus() ) {
        $self->_log( $self->getDescription().' is disable', 2 );
        return undef;
    }

    if( $self->_ping() ) {
        $self->_log( 'Already connected to '.$self->getDescription(), 5 );
        return $self->{'cyruscyrusServerConn'};
    }

    $self->_log( 'Authenticate to '.$self->getDescription().' as \''.$self->{'cyrus_admin'}.'\', password \''.$self->{'cyrus_admin_password'}.'\'', 4 );

    eval {
        local $SIG{ALRM} = sub {
            $self->_log( 'Fail to connect to '.$self->getDescription().' - No server response', 2 );
            delete($self->{'cyrusServerConn'});
            die 'alarm'."\n";
        };

        alarm IMAP_TCP_CONN_TIMEOUT;
        $self->{'cyrusServerConn'} = ObmSatellite::Services::cyrusAdmin->new( $self->{'cyrus_server'} );
        alarm 0;
    };

    my @tempo = ( 1, 3, 5, 10, 20 );
    while( !$self->{'cyrusServerConn'} ) {
        $self->_log( 'Fail to connect to '.$self->getDescription(), 2 ) if (defined($@) && ($@ ne 'alarm'."\n"));

        my $tempo = shift(@tempo);
        if( !defined($tempo) ) {
            last;
        }

        $self->_log( 'Try to connect to Cyrus '.$self->getDescription().' again in '.$tempo.'s', 3 );
        sleep $tempo;

        eval {
            local $SIG{ALRM} = sub {
                $self->_log( 'Fail to connect to '.$self->getDescription().' - No server response', 2 );
                delete($self->{'cyrusServerConn'});
                die 'alarm'."\n";
            };

            alarm IMAP_TCP_CONN_TIMEOUT;
            $self->{'cyrusServerConn'} = OBM::Cyrus::cyrusAdmin->new( $self->{'cyrus_server'} );
            alarm 0;
        };
    }

    if( !$self->{'cyrusServerConn'} ) {
        $self->{'cyrusServerConn'} = undef;
        $self->_log( 'Disable '.$self->getDescription().' - Can\'t connect', 0 );
        $self->_setDeadStatus();
        return undef;
    }

    $self->_log( 'Authenticate as \''.$self->{'cyrus_admin'}.'\' to '.$self->getDescription(), 3
);

    if( !$self->{'cyrusServerConn'}->authenticate(
                -user=>$self->{'cyrus_admin'},
                -password=>$self->{'cyrus_admin_password'}, -mechanism=>'login') ) {
        $self->_log( 'Fail to authenticate to '.$self->getDescription(), 0 );
        return undef;
    }

    $self->_log( 'Connection established to '.$self->getDescription(), 3 );

    return $self->{'cyrusServerConn'};
}


sub getDeadStatus {
    my $self = shift;

    return $self->{'deadStatus'} if !$self->{'deadStatus'};

    if( (time() - $self->{'deadStatus'}) < DEAD_STATUS_TIME ) {
        return 1;
    }

    return $self->_unsetDeadStatus();
}


sub _setDeadStatus {
    my $self = shift;

    $self->{'deadStatus'} = time();

    return 0;
}


sub _unsetDeadStatus {
    my $self = shift;

    $self->{'cyrusServerConn'} = undef;
    $self->{'deadStatus'} = 0;

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


# This is done to prevent a non fatal uninitialized value on
# 'Cyrus::IMAP::Admin' global
# destruction
package ObmSatellite::Services::cyrusAdmin;

use strict;

use Cyrus::IMAP::Admin;
use ObmSatellite::Log::log;
our @ISA = ('Cyrus::IMAP::Admin', 'ObmSatellite::Log::log');


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );
}


# Catch STDERR to drop messages print on this by Cyrus::IMAP::Admin
sub authenticate {
    my $self = shift;
    
    my $returnCode = eval{
            close(STDERR);
            return $self->SUPER::authenticate(@_);
        };
    
    return $returnCode;
}
