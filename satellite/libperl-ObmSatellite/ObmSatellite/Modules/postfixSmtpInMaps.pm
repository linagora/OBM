package ObmSatellite::Modules::postfixSmtpInMaps;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;

use ObmSatellite::Modules::abstract;
@ISA = qw(ObmSatellite::Modules::abstract);
use strict;

use HTTP::Status;

use constant POSTMAP_CMD => '/usr/sbin/postmap';
use constant MAILBOX_MAP => '/etc/postfix/virtual_mailbox';
use constant ALIAS_MAP => '/etc/postfix/virtual_alias';
use constant TRANSPORT_MAP => '/etc/postfix/transport';
use constant DOMAIN_MAP => '/etc/postfix/virtual_domains';


sub _initHook {
    my $self = shift;

    $self->{'uri'} = [ '/postfixsmtpinmaps' ];
    $self->{'neededServices'} = [ 'LDAP' ];

    $self->{'postmapCmd'} = POSTMAP_CMD;
    $self->{'mailboxMap'} = MAILBOX_MAP;
    $self->{'aliasMap'} = ALIAS_MAP;
    $self->{'transportMap'} = TRANSPORT_MAP;
    $self->{'domainMap'} = DOMAIN_MAP;

    $self->{'description'} = {
        'mailboxMap' => {
            ldap_attibute_left => [ 'mailbox' ],
            ldap_attibute_right => 'OK',
            ldap_filter => '(&(|(objectclass=obmuser)(objectclass=obmmailshare))(mailAccess=PERMIT)%d)'
        },
        'aliasMap' => {
            ldap_attibute_left => [ 'mail', 'mailAlias' ],
            ldap_attibute_right => [ 'mailbox', 'externalContactEmail' ],
            ldap_filter => '(&(mailAccess=PERMIT)%d)'
        },
        'transportMap' => {
            ldap_attibute_left => [ 'mailbox' ],
            ldap_attibute_right => [ 'mailBoxServer' ],
            ldap_filter => '(&(|(objectClass=obmUser)(objectClass=obmMailShare))(mailAccess=PERMIT)%d)'
        },
        'domainMap' => {
            ldap_attibute_left => [ 'myDestination' ],
            ldap_attibute_right => 'OK',
            ldap_filter => '(&(objectClass=obmMailServer)%d)'
        }
    };


    # Load some options from module configuration file
    my @params = ( 'postmapCmd', 'ldapRoot' );
    my $confFileParams = $self->_loadConfFile( \@params );

    $self->_log( $self->getModuleName().' module configuration :', 4 );
    for( my $i=0; $i<=$#params; $i++ ) {
        $self->{$params[$i]} = $confFileParams->{$params[$i]} if defined($confFileParams->{$params[$i]});
        $self->_log( $params[$i].' : '.$self->{$params[$i]}, 4 ) if defined($self->{$params[$i]});
    }

    return 1;
}


sub _postMethod {
    my $self = shift;
    my( $requestUri, $requestBody ) = @_;
    my %datas;

    $datas{'requestUri'} = $requestUri;

    if( $requestUri !~ /^\/postfixsmtpinmaps\/host\/([^\/]+)(\/([^\/]+)){0,1}$/ ) {
        my $return = $self->_response( RC_BAD_REQUEST, { content => [ 'Invalid URI '.$requestUri ] } );
        $return->[1]->{'help'} = [ $self->getModuleName().' URI must be : /postfixsmtpinmaps/host/<hostname>[/<map>]' ];
        return $return;
    }

    $datas{'hostname'} = $1;
    $datas{'map'} = $3;

    SWITCH: {
        if( !$datas{'map'} ) {
            $datas{'maps'} = [ 'mailboxMap', 'aliasMap', 'transportMap', 'domainMap' ];
            last SWITCH;
        }

        if( $datas{'map'} eq 'mailbox' ) {
            $datas{'maps'} = [ 'mailboxMap' ];
            last SWITCH;
        }

        if( $datas{'map'} eq 'alias' ) {
            $datas{'maps'} = [ 'aliasMap' ];
            last SWITCH;
        }

        if( $datas{'map'} eq 'transport' ) {
            $datas{'maps'} = [ 'transportMap' ];
            last SWITCH;
        }

        if( $datas{'map'} eq 'domain' ) {
            $datas{'maps'} = [ 'domainMap' ];
            last SWITCH;
        }

        return $self->_response( RC_NOT_FOUND, { content => [ 'Unknow Postfix map \''.$datas{'map'}.'\'' ] } );
    }

    return $self->_generateMaps( \%datas );
}


sub _generateMaps {
    my $self = shift;
    my( $datas ) = @_;

    my $domainList = $self->_getHostDomains( 'smtpInHost', $datas->{'hostname'} );
    if( !defined($domainList) ) {
        my $return = $self->_response( RC_INTERNAL_SERVER_ERROR, { content => [ 'Can\'t get domain linked to host '.$datas->{'hostname'}.' from LDAP server' ] } );
        return $return;
    }

    foreach my $map ( @{$datas->{'maps'}} ) {
        $self->_log( 'Generate flat map \''.$map.'\', file '.$self->{$map}, 3 );
        if( $self->_generateFlatMap( $map, $domainList ) ) {
            $self->_log( 'Fail to generate flat map \''.$map.'\', file '.$self->{$map}, 1 );
            return $self->_response( RC_INTERNAL_SERVER_ERROR, { content => [ 'Fail to generate flat map \''.$map.'\', file '.$self->{$map} ] } );
        }
    }

    foreach my $map ( @{$datas->{'maps'}} ) {
        $self->_log( 'Generate DB map \''.$map.'\', from file '.$self->{$map}, 3 );

        if( $self->_generateDbMap( $map ) ) {
            $self->_log( 'Fail to generate DB map \''.$map.'\', from file '.$self->{$map}, 1 );
            return $self->_response( RC_INTERNAL_SERVER_ERROR, { content => [ 'Fail to generate DB map \''.$map.'\', from file '.$self->{$map} ] } );
        }
    }

    return $self->_response( RC_OK, { content => [ 'Postfix SMTP-in maps generated successfully' ] } );
}


sub _generateFlatMap {
    my $self = shift;
    my( $map, $domainList ) = @_;

    my $ldapFilter = $self->{'description'}->{$map}->{'ldap_filter'};
    my $ldapAttrsLeft = $self->{'description'}->{$map}->{'ldap_attibute_left'};
    my $ldapAttrsRight = $self->{'description'}->{$map}->{'ldap_attibute_right'};
    my $ldapAttrs;
    push( @{$ldapAttrs}, @{$ldapAttrsLeft} ) if ref($ldapAttrsLeft) eq 'ARRAY';
    push( @{$ldapAttrs}, @{$ldapAttrsRight} ) if ref($ldapAttrsRight) eq 'ARRAY';

    my $domainFilter = '';
    foreach my $domain ( @{$domainList} ) {
        $domainFilter .= '(obmdomain='.$domain.')';
    }
    $domainFilter = '(|'.$domainFilter.')';

    $ldapFilter =~ s/%d/$domainFilter/g;

    my $ldapEntries = $self->_getLdapValues( $ldapFilter, $ldapAttrs );

    return 1 if !defined($ldapEntries);

    if( !open( POSTFIX_MAP, '>'.$self->{$map} ) ) {
        $self->_log( 'Unable to open file '.$self->{$map}.' to write postfix map \''.$map.'\'', 1 );
        return 1;
    }

    foreach my $entry ( @{$ldapEntries} ) {
        my $mapLeftColumn;
        for( my $i=0; $i<=$#{$ldapAttrsLeft}; $i++ ) {
            my $attrs = $entry->get_value( $ldapAttrsLeft->[$i], asref => 1 );
            push( @{$mapLeftColumn}, @{$attrs} ) if ref($attrs) eq 'ARRAY';
        }

        my $mapRightColumn;
        if( ref($ldapAttrsRight) eq 'ARRAY' ) {
            my $rightColumnVal = [];
            for( my $i=0; $i<=$#{$ldapAttrsRight}; $i++ ) {
                my $attrs = $entry->get_value( $ldapAttrsRight->[$i], asref => 1 );
                push( @{$rightColumnVal}, @{$attrs} ) if ref($attrs) eq 'ARRAY';
            }

            $mapRightColumn = join( ', ', @{$rightColumnVal} );
        }else {
            $mapRightColumn = $ldapAttrsRight;
        }

        for( my $i=0; $i<=$#{$mapLeftColumn}; $i++ ) {
            print POSTFIX_MAP $mapLeftColumn->[$i]."\t".$mapRightColumn."\n" if ($mapLeftColumn->[$i] && $mapRightColumn);
        }
    }

    close(POSTFIX_MAP);

    return 0;
}


sub _generateDbMap {
    my $self = shift;
    my( $map ) = @_;

    my $cmd = $self->{'postmapCmd'}.' hash:'.$self->{$map};
    $self->_log( 'Exec: '.$cmd, 4 );
    my $ret = 0xffff & system $cmd;

    if( $ret ) {
        $self->_log( 'Command \''.$cmd.'\' return status '.$ret, 1 );
        return 1;
    }

    return 0;
}


# Perldoc

=head1 NAME

Postfix SMTP-in maps generation obmSatellite module

=head1 SYNOPSIS

This module manage Postfix SMTP-in maps. It need to contact OBM LDAP database.

This module is XML/HTTP REST compliant.

=head1 COMMAND

=over 4

=item B<POST /postfixsmtpinmaps/host/<obmHostName>> : generate all Postfix
SMTP-in needed maps on host I<obmHostName>

=over 4

=item POST data : none

=item Return success or fail status

=back

=item B<POST /postfixsmtpinmaps/host/<obmHostName>>B</<map>> : generate Postfix
SMTP-in I<map> on host I<obmHostName>

=over 4

=item POST data : none

=item Return success or fail status

=item Map :

=over 4

=item I<mailboxMap> : existing mailbox name map

=item I<aliasMap> : alias map

=item I<transportMap> : mailbox transport map

=item I<domainMap> : existing mail domain map

=back

=back

=back
