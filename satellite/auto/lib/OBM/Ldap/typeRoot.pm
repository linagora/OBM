package OBM::Ldap::typeRoot;

require Exporter;

use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
use strict;


sub initStruct {
    my( $ldapStruct, $parentDn ) = @_;
    my $domainId = $ldapStruct->{"domain_id"};

    if( exists($ldapStruct->{"ldap_server"}) ) {
        if( exists($main::domainList->[$domainId]->{"ldap_admin_server"}) && defined($main::domainList->[$domainId]->{"ldap_admin_server"}) ) {
            $ldapStruct->{"ldap_server"}->{"server"} = $main::domainList->[$domainId]->{"ldap_admin_server"};
        }

        if( exists($main::domainList->[$domainId]->{"ldap_admin_login"}) && defined($main::domainList->[$domainId]->{"ldap_admin_login"}) ) {
            $ldapStruct->{"ldap_server"}->{"login"} = $main::domainList->[$domainId]->{"ldap_admin_login"};
        }

        if( exists($main::domainList->[$domainId]->{"ldap_admin_passwd"}) && defined($main::domainList->[$domainId]->{"ldap_admin_passwd"}) ) {
            $ldapStruct->{"ldap_server"}->{"passwd"} = $main::domainList->[$domainId]->{"ldap_admin_passwd"};
        }
    }

    return 1;
}


sub getDbValues {
    my( $parentDn, $domainId ) = @_;
    my @root = ();

    return \@root;
}


sub createLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};
    my $name = lc($entry->{"name"});

    $ldapEntry->add(
        objectClass => $attributeDef->{$type}->{"objectclass"},
        dc => to_utf8( { -string => $name, -charset => $defaultCharSet } ),
        o => to_utf8( { -string => ucfirst($name), -charset => $defaultCharSet } )
    );

    return 1;
}


sub updateLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $name = lc($entry->{"name"});

    $ldapEntry->replace(
        o => to_utf8( { -string => ucfirst($name), -charset => $defaultCharSet } )
    );

    return 1;
}
