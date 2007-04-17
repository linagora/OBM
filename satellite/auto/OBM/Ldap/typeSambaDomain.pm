package OBM::Ldap::typeSambaDomain;

require Exporter;

use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
use strict;


sub initStruct {
    return 1;
}


sub getDbValues {
    my( $parentDn, $domainId ) = @_;
    my $i = 0;
    my @sambaDomain = ();

    if( !defined($main::domainList->[$domainId]->{"domain_id"}) ) {
        &OBM::toolBox::write_log( "Identifiant de domaine OBM non définie", "W" );
        return undef;
    }

    if( defined($main::domainList->[$domainId]->{"domain_label"}) ) {
        $sambaDomain[$i]->{"samba_domain_sid"} = $main::domainList->[$domainId]->{"domain_samba_sid"};
    }else {
        &OBM::toolBox::write_log( "SID du domaine windows non defini", "W" );
        return undef;
    }

    # On se connecte a la base
    my $dbHandler;
    if( !&OBM::dbUtils::dbState( "connect", \$dbHandler ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'ouverture de la base de donnee : ".$dbHandler->err, "W" );
        return undef;
    }

    my $query = "SELECT samba_name, samba_value FROM Samba WHERE samba_domain_id=".$main::domainList->[$domainId]->{"domain_id"};

    # On execute la requete
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete du domaine windows : ".$dbHandler->err, "W" );
        return undef;
    }

    # On range les resultats dans la structure de donnees des resultats
    my %dbSambaDomain;
    while( my( $samba_name, $samba_value ) = $queryResult->fetchrow_array ) {
        $dbSambaDomain{$samba_name} = $samba_value;
    }


    if( defined($dbSambaDomain{"samba_domain"}) ) {
        &OBM::toolBox::write_log( "Gestion du domaine windows : '".$dbSambaDomain{"samba_domain"}."'", "W" );
        $sambaDomain[$i]->{"samba_domain"} = $dbSambaDomain{"samba_domain"};
    }else {
        &OBM::toolBox::write_log( "Nom de domaine windows non defini.", "W" );
        return undef;
    }

    if( defined($dbSambaDomain{"samba_pdc"}) ) {
        $sambaDomain[$i]->{"samba_pdc"} = $dbSambaDomain{"samba_pdc"};
    }

    if( defined($dbSambaDomain{"samba_profile"}) ) {
        $sambaDomain[$i]->{"samba_profile"} = $dbSambaDomain{"samba_profile"};
    }

    if( defined($dbSambaDomain{"samba_home_def"}) ) {
        $sambaDomain[$i]->{"samba_home_def"} = $dbSambaDomain{"samba_home_def"};
    }

    if( defined($dbSambaDomain{"samba_home_drive_def"}) ) {
        $sambaDomain[$i]->{"samba_home_drive_def"} = $dbSambaDomain{"samba_home_drive_def"};
    }

    $sambaDomain[$i]->{"samba_obm_domain"} = $main::domainList->[$domainId]->{"domain_label"};


    # On ajoute les informations de la structure
    $sambaDomain[$i]->{"node_type"} = $SAMBADOMAIN;
    $sambaDomain[$i]->{"name"} = $sambaDomain[$i]->{$attributeDef->{$sambaDomain[$i]->{"node_type"}}->{"dn_value"}};
    $sambaDomain[$i]->{"domain_id"} = $domainId;
    $sambaDomain[$i]->{"dn"} = &OBM::ldap::makeDn( $sambaDomain[$i], $parentDn );

    # On referme la connexion a la base
    if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
        &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "W" );
    }

    return \@sambaDomain;
}


sub createLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};

    # Les parametres nececessaires
    if( $entry->{"samba_domain"} && $entry->{"samba_domain_sid"} ) {
        $ldapEntry->add(
            objectClass => $attributeDef->{$type}->{"objectclass"},
            sambaDomainName => to_utf8({ -string => $entry->{"samba_domain"}, -charset => $defaultCharSet }),
            sambaSID => $entry->{"samba_domain_sid"}
        );

    }else {
        return 0;
    }

    # Le domaine
    if( $entry->{"samba_obm_domain"} ) {
        $ldapEntry->add( obmDomain => to_utf8({ -string => $entry->{"samba_obm_domain"}, -charset => $defaultCharSet }) );
    }

    return 1;
}


sub updateLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};
    my $update = 0;

    # Vérification du nom de domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"samba_domain"}, $ldapEntry, "sambaDomainName" ) ) {
        $update = 1;
    };

    # Vérification du SID
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"samba_domain_sid"}, $ldapEntry, "sambaSID" ) ) {
        $update = 1;
    };

    return $update;
}


