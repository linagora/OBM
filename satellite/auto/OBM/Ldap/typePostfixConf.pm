package OBM::Ldap::typePostfixConf;

require Exporter;

use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
require OBM::Ldap::utils;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
use strict;


sub initStruct {
    return 1;
}


sub getDbValues {
    my( $parentDn, $domainId ) = @_;
    my $currentDomain = $main::domainList->[$domainId];


    if( !defined($currentDomain->{"domain_id"}) ) {
        &OBM::toolBox::write_log( "Identifiant de domaine OBM non définie", "W" );
        return undef;
    }


    if( !defined($currentDomain->{"domain_label"}) ) {
        return undef;
    }

    &OBM::toolBox::write_log( "Gestion de la configuration de Postfix pour le domaine '".$currentDomain->{"domain_label"}."'", "W" );
        
    #
    # On cree la structure correspondante a la configuration de Postfix par
    # domaine
    my @postfixConfs;
    $postfixConfs[0]->{"postfixconf_name"} = $currentDomain->{"domain_label"};
    $postfixConfs[0]->{"postfixconf_domain"} = $currentDomain->{"domain_label"};

    $postfixConfs[0]->{"postfixconf_mail_domains"} = [];
    push( @{$postfixConfs[0]->{"postfixconf_mail_domains"}}, $currentDomain->{"domain_name"} );
    for( my $i=0; $i<=$#{$currentDomain->{"domain_alias"}}; $i++ ) {
        push( @{$postfixConfs[0]->{"postfixconf_mail_domains"}}, $currentDomain->{"domain_alias"}->[$i] );
    }

    # On ajoute les informations de la structure
    $postfixConfs[0]->{"node_type"} = $MAILSERVER;
    $postfixConfs[0]->{"name"} = $postfixConfs[0]->{$attributeDef->{$postfixConfs[0]->{"node_type"}}->{"dn_value"}};
    $postfixConfs[0]->{"domain_id"} = $domainId;
    $postfixConfs[0]->{"dn"} = &OBM::ldap::makeDn( $postfixConfs[0], $parentDn );


    return \@postfixConfs;
}


sub createLdapEntry {
    my ( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};

    if( !defined( $entry->{"postfixconf_name"} ) ) {
        return 0;
    }

    #
    # On construit la nouvelle entree
    #
    $ldapEntry->add(
        objectClass => $attributeDef->{$type}->{"objectclass"},
        cn => to_utf8({ -string => $entry->{"postfixconf_name"}, -charset => $defaultCharSet }),
    );

    # Les domaines de messagerie
    if( $entry->{"postfixconf_mail_domains"} ) {
        $ldapEntry->add( myDestination => $entry->{"postfixconf_mail_domains"} );
    }

    # Le domaine
    if( $entry->{"postfixconf_domain"} ) {
        $ldapEntry->add( obmDomain => to_utf8({ -string => $entry->{"postfixconf_domain"}, -charset => $defaultCharSet }) );
    }

    return 1;
}


sub updateLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};
    my $update = 0;

    # Les domaines de messagerie
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"postfixconf_mail_domains"}, $ldapEntry, "myDestination" ) ) {
        $update = 1;
    }

    # Le domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"postfixconf_domain"}, $ldapEntry, "obmDomain") ) {
        $update = 1;
    }

    return $update;
}

