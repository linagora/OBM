package OBM::Ldap::typeNode;

require Exporter;

use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
require OBM::Ldap::utils;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);


#
# Necessaire pour le bon fonctionnement du package
$debug=1;


sub initStruct {
    return 1;
}


sub getDbValues {
    my( $parentDn, $domainId ) = @_;
    my @node = ();

    return \@node;
}


sub createLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};

    #
    # On construit la nouvelle entree
    if( $entry->{"name"} ) {
        $ldapEntry->add(
            objectClass => $attributeDef->{$type}->{"objectclass"},
            ou => to_utf8({ -string => $entry->{"name"}, -charset => $defaultCharSet })
        );
                
    }else {
        return 0;
    }

    # La description
    if( $entry->{"description"} ) {
        $ldapEntry->add( description => to_utf8({ -string => $entry->{"description"}, -charset => $defaultCharSet }) );
    }

    return 1;
}


sub updateLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};
    my $update = 0;

    if( &OBM::Ldap::utils::modifyAttr( $entry->{"description"}, $ldapEntry, "description" ) ) {
        $update = 1;
    }

    return $update;
}
