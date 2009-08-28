package ObmSaslauthd::Server::configure;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub configure_hook {
    my $self = shift;

    $self->{'server'}->{'name'} = 'obmSaslauthd';
    $self->{'server'}->{'conf_file'} = '/etc/obm-saslauthd/'.$self->{'server'}->{'name'}.'.cf';
    
    $self->{'server'}->{'setsid'} = 1;
}


sub post_configure_hook {
    my $self = shift;

    if( $self->_loadAuthenticationModules() ) {
        $self->log( 2, 'Authentication modules initialization fail' );
        $self->server_close();
    }
}


sub _loadAuthenticationModules {
    my $self = shift;

    my $authenticationModules = $self->loadOption( 'auth_mods' );
    if( !$authenticationModules->[0] ) {
        $self->log( 0, 'No authentication modules defined, see \'auth_mods\' configuration option' );
        return 1;
    }

    $self->{'authenticationModules'} = [];

    $authenticationModules->[0] =~ s/ //g;
    my @authenticationModules = split( ',', $authenticationModules->[0] );

    for( my $i=0; $i<=$#authenticationModules; $i++ ) {
        my $moduleInternalName = $authenticationModules[$i];
        $moduleInternalName =~ s/-/_/g;
        my $modulePath = 'ObmSaslauthd/AuthMods/'.$moduleInternalName.'.pm';
        my $moduleClass = 'ObmSaslauthd::AuthMods::'.$moduleInternalName;

        eval {
            require $modulePath;
        } or ($self->log( 0, 'Unknow authentication module \''.$authenticationModules[$i].'\'' ) && next);

        my $authMod = $moduleClass->new( $self );
        if( !defined($authMod) ) {
            $self->log( 0, 'loading authentication module \''.$authenticationModules[$i].'\' failed' );
            next;
        }

        if( $authMod->init( $self ) ) {
            $self->log( 0, 'authentication module \''.$authenticationModules[$i].'\' initialization failed' );
            next;
        }

        $self->log( 0, 'loading authentication module \''.$authenticationModules[$i].'\' success' );
        push( @{$self->{'authenticationModules'}}, $authMod );
    }

    if( $#{$self->{'authenticationModules'}} < 0 ) {
        $self->log( 0, 'No authentication module loaded !' );
        return 1;
    }

    return 0;
}


sub loadOption {
    my $self = shift;
    my( $option ) = @_;

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
