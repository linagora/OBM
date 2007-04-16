package OBM::Ldap::typeSambaHosts;

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

    if( !defined($main::domainList->[$domainId]->{"domain_id"}) ) {
        &OBM::toolBox::write_log( "Identifiant de domaine OBM non définie", "W" );
        return undef;
    }

    # On se connecte a la base
    my $dbHandler;
    if( !&OBM::dbUtils::dbState( "connect", \$dbHandler ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'ouverture de la base de donnee : ".$dbHandler->err, "W" );
        return undef;
    }

    my $query = "SELECT host_id, host_uid, host_gid, host_name, host_description FROM Host WHERE host_samba='1' AND host_domain_id=".$main::domainList->[$domainId]->{"domain_id"};

    # On execute la requete
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete du domaine windows : ".$dbHandler->err, "W" );
        return undef;
    }

    # On range les resultats dans la structure de donnees des resultats
    my $i = 0;
    my @sambaHosts = ();
    while( my( $host_id, $host_uid, $host_gid, $host_name, $host_description ) = $queryResult->fetchrow_array ) {
        &OBM::toolBox::write_log( "Gestion de l'hote '".$host_name."'", "W" );

        $sambaHosts[$i]->{"host_uid"} = $host_uid;
        $sambaHosts[$i]->{"host_gid"} = $host_gid;
        $sambaHosts[$i]->{"host_login"} = $host_name."\$";
        $sambaHosts[$i]->{"host_name"} = $host_name;
        $sambaHosts[$i]->{"host_desc"} = $host_description;

        $sambaHosts[$i]->{"host_sid"} = getUserSID();
        $sambaHosts[$i]->{"host_group_sid"} = getGroupSID();
        $sambaHosts[$i]->{"host_nt_passwd"} = getNTPasswd();
        $sambaHosts[$i]->{"host_lm_passwd"} = getLMPasswd();

        # On ajoute les informations de la structure
        $sambaHosts[$i]->{"node_type"} = $SAMBADOMAIN;
        $sambaHosts[$i]->{"name"} = $sambaHosts[$i]->{$attributeDef->{$sambaHosts[$i]->{"node_type"}}->{"dn_value"}};
        $sambaHosts[$i]->{"domain_id"} = $domainId;
        $sambaHosts[$i]->{"dn"} = &OBM::ldap::makeDn( $domainSamba[0], $parentDn );
    }

    # On referme la connexion a la base
    if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
        &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "W" );
    }

    return \@domainSamba;
}


sub createLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};

    # Les parametres nececessaires
    if( $entry->{"samba_domain"} && $entry->{"samba_sid"} ) {
        $ldapEntry->add(
            objectClass => $attributeDef->{$type}->{"objectclass"},
            sambaDomainName => to_utf8({ -string => $entry->{"samba_domain"}, -charset => $defaultCharSet }),
            sambaSID => $entry->{"samba_sid"}
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
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"samba_sid"}, $ldapEntry, "sambaSID" ) ) {
        $update = 1;
    };

    return $update;
}


