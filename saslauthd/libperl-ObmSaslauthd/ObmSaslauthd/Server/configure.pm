package ObmSaslauthd::Server::configure;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use ObmSaslauthd::Ldap::ldapCheckPasswd;


sub configure_hook {
    my $self = shift;

    $self->{'server'}->{'name'} = 'obmSaslauthd';
    $self->{'server'}->{'conf_file'} = '/etc/obm/'.$self->{'server'}->{'name'}.'.cf';
    
    $self->{'server'}->{'setsid'} = 1;

    if( $self->_configureLdap() ) {
        $self->log( 0, 'ldap configuration error' );
        die 'ldap configuration error';
    }
}


sub _configureLdap {
    my $self = shift;

    my $daemonOptions = {
        ldap_server => [],
        ldap_server_tls => [],
        ldap_base => [],
        ldap_login => [],
        ldap_password => [],
        ldap_filter => []
        };
    $self->configure( $daemonOptions );

    $daemonOptions->{'ldap_filter'} = $self->_loadOption( 'ldap_filter' );


    $self->{'ldapCheckPasswd'} = ObmSaslauthd::Ldap::ldapCheckPasswd->new( $self, {
        ldap_server => shift( @{$daemonOptions->{'ldap_server'}} ),
        ldap_server_tls => shift( @{$daemonOptions->{'ldap_server_tls'}} ),
        ldap_base => shift( @{$daemonOptions->{'ldap_base'}} ),
        ldap_login => shift( @{$daemonOptions->{'ldap_login'}} ),
        ldap_password => shift( @{$daemonOptions->{'ldap_password'}} ),
        ldap_filter => shift( @{$daemonOptions->{'ldap_filter'}} )
    } );

    if( !defined($self->{'ldapCheckPasswd'}) ) {
        return 1;
    }

    return 0;
}


sub _loadOption {
    my $self = shift;
    my( $option ) = @_;
    my $args = $self->{'server'}->{'conf_file_args'};

    if( not open(_CONF, "<".$self->{'server'}->{'conf_file'}) ) {
        die 'Couldn\'t open conf \''.$self->{'server'}->{'conf_file'}.'\' [$!]';
    }

    my $value;
    while( my $line = <_CONF> ) {
        if( $line =~ /^$option([=\ ](.+))?$/ ) {
            $value = $2;
            last;
        }
    }

    close(_CONF);

    return [ $value ];
}
