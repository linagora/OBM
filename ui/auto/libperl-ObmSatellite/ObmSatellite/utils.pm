#########################################################################
# OBM           - File : ObmSatellite::utils.pm (Perl Module)           #
#               - Desc : Librairie Perl pour OBM                        #
#               Les fonctions communes necessaires au service           #
#               obmSatellite                                            #
#########################################################################
# Cree le 2007-03-12                                                    #
#########################################################################
package ObmSatellite::utils;

$VERSION = '1.0';

$debug = 1;

use strict;
require Net::LDAP;


sub connectLdapSrv {
    my( $ldapSrv ) = @_;

    if( ref($ldapSrv->{'conn'}) eq 'Net::LDAP' ) {
        return 1;
    }

    if( !defined($ldapSrv->{server}) ) {
        return 0;
    }

    $ldapSrv->{'conn'} = Net::LDAP->new(
        $ldapSrv->{'server'},
        debug => '0',
        timeout => '60',
        version => '3'
    ) or return 0;


    if( !defined($ldapSrv->{'conn'}) ) {
        return 0;
    }

    if( $ldapSrv->{'ldap_server_tls'} =~ /^(may|encrypt)$/ ) {
        my $errorCode = $ldapSrv->{'conn'}->start_tls( verify => 'none' );

        if( $errorCode->code() && ($ldapSrv->{'ldap_server_tls'} eq 'encrypt') ) {
            # TLS fatal error. 'ldap_server_tls' is 'encrypt', TLS must succeed
            return 0;
        }

        if( $errorCode->code() && ($ldapSrv->{'ldap_server_tls'} eq 'may') ) {
            # TLS error. 'ldap_server_tls' is 'may', TLS may succed or not
            $ldapSrv->{'ldap_server_tls'} = 'none';

            $ldapSrv->{'conn'} = undef;
            return connectLdapSrv( $ldapSrv );
        }
    }


    my $errorCode;
    if( defined($ldapSrv->{'login'}) && defined($ldapSrv->{'password'}) ) {
        $errorCode = $ldapSrv->{'conn'}->bind(
            $ldapSrv->{'login'},
            password => $ldapSrv->{'password'}
        );
    }else {
        $errorCode = $ldapSrv->{'conn'}->bind();
    }

    if( $errorCode->code ) {
        return 0;
    }

    return 1;
}


sub disconnectLdapSrv {
    my( $ldapSrv ) = @_;

    if( defined($ldapSrv->{'conn'}) ) {
        $ldapSrv->{'conn'}->unbind();
        $ldapSrv->{'conn'} = undef;
    }

    return 1;
}


sub ldapSearch {
    my( $ldapSrv, $ldapEntries, $ldapFilter, $ldapAttributes ) = @_;

    if( !defined($ldapSrv->{conn}) ) {
        return 1;
    }
    my $ldapConn = $ldapSrv->{conn};

    if( !defined($ldapFilter) ) {
        return 1;
    }

    if( !defined($ldapAttributes) || ( ref($ldapAttributes) ne "ARRAY" ) ) {
        return 1;
    }

    my $ldapResult = $ldapConn->search(
        base => $ldapSrv->{"base"},
        filter => $ldapFilter,
        scope => "sub",
        attrs => $ldapAttributes
    );

    if( !defined($ldapResult) || $ldapResult->is_error() ) {
        return 1;
    }else {
        @{$ldapEntries} = $ldapResult->entries();
    }

    return 0;
}


sub writeMap {
    my( $file, $separator, $mapEntries ) = @_;

    if( !defined($file) ) {
        return 1;
    }

    open( FIC, ">".$file ) or return 1;

    while( my( $key, $value ) = each(%{$mapEntries}) ) {
        print FIC $key.$separator.$value."\n";
    }

    close( FIC );
    return 0;
}
