#########################################################################
# OBM           - File : OBM::ObmSatellite::utils.pm (Perl Module)   #
#               - Desc : Librairie Perl pour OBM                        #
#               Les fonctions communes necessaires au service           #
#               obmSatellite                                            #
#########################################################################
# Cree le 2007-03-12                                                    #
#########################################################################
package OBM::ObmSatellite::utils;

require Exporter;
use strict;


sub connectLdapSrv {
    my( $ldapSrv ) = @_;
    require Net::LDAP;

    if( !defined($ldapSrv->{server}) ) {
        return 0;
    }

    $ldapSrv->{"conn"} = Net::LDAP->new(
        $ldapSrv->{"server"},
        port => "389",
        debug => "0",
        timeout => "60",
        version => "3"
    ) or return 0;


    if( !defined($ldapSrv->{"conn"}) ) {
        return 0;
    }

    my $errorCode;
    if( defined($ldapSrv->{"login"}) && defined($ldapSrv->{"password"}) ) {
        $errorCode = $ldapSrv->{"conn"}->bind(
            $ldapSrv->{"login"},
            password => $ldapSrv->{"password"}
        );
    }else {
        $errorCode = $ldapSrv->{"conn"}->bind();
    }

    if( $errorCode->code ) {
        return 0;
    }

    return 1;
}


sub disconnectLdapSrv {
    my( $ldapSrv ) = @_;

    if( defined($ldapSrv->{"conn"}) ) {
        $ldapSrv->{"conn"}->unbind();
    }

    return 1;
}


sub ldapSearch {
    my( $ldapSrv, $ldapEntries, $ldapFilter, $ldapAttributes ) = @_;
    use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
    use Net::LDAP::Util qw(escape_filter_value);
    require Net::LDAP;

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
        filter => to_utf8( { -string => $ldapFilter, -charset => "ISO-8859-1"} ),
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
