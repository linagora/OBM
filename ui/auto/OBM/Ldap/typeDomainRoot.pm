package OBM::Ldap::typeDomainRoot;

require Exporter;

use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);


#
# Necessaire pour le bon fonctionnement du package
$debug=1;


sub initStruct {
    my( $ldapStruct, $parentDn ) = @_;

    if( exists($ldapStruct->{"ldap_server"}) ) {
        $ldapStruct->{"ldap_server"}->{"login"} = $ldapAdminLogin;
    }

    return 1;
}


sub getDbValues {
    my( $parentDn, $domainId ) = @_;
    my @domainRoot = ();

    return \@DomainRoot;
}


sub createLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};
    my $name = $entry->{"name"};

    $ldapEntry->add(
        objectClass => $attributeDef->{$type}->{"objectclass"},
        dc => to_utf8( { -string => $name, -charset => $defaultCharSet } ),
        o => to_utf8( { -string => $name, -charset => $defaultCharSet } )
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
