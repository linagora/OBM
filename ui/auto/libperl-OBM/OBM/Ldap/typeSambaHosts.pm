package OBM::Ldap::typeSambaHosts;

require Exporter;

use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
require OBM::passwd;
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

    my $query = "SELECT host_id, host_uid, host_gid, host_name, host_description FROM P_Host WHERE host_samba='1' AND host_domain_id=".$main::domainList->[$domainId]->{"domain_id"};

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
        if( !defined($host_name) ) {
            next;
        }

        &OBM::toolBox::write_log( "Gestion de l'hote '".$host_name."'", "W" );

        # Gestion du mot de passe
        my $errorCode = &OBM::passwd::getNTLMPasswd( $host_name, \$sambaHosts[$i]->{"host_lm_passwd"}, \$sambaHosts[$i]->{"host_nt_passwd"} );
        if( $errorCode ) {
            &OBM::toolBox::write_log( "Erreur: lors de la generation du mot de passe de l'hote : '".$host_name."'", "W" );
            next;
        }

        $sambaHosts[$i]->{"host_uid"} = $host_uid;
        $sambaHosts[$i]->{"host_gid"} = $host_gid;
        $sambaHosts[$i]->{"host_login"} = $host_name."\$";
        $sambaHosts[$i]->{"host_name"} = $host_name;
        $sambaHosts[$i]->{"host_desc"} = $host_description;

        $sambaHosts[$i]->{"host_sid"} = $main::domainList->[$domainId]->{"domain_samba_sid"}."-".$host_uid;
        $sambaHosts[$i]->{"host_group_sid"} = $main::domainList->[$domainId]->{"domain_samba_sid"}."-".$host_gid;
        $sambaHosts[$i]->{"host_acct_flags"} = "[W]";

        $sambaHosts[$i]->{"host_obm_domain"} = $main::domainList->[$domainId]->{"domain_label"};

        # On ajoute les informations de la structure
        $sambaHosts[$i]->{"node_type"} = $SAMBAHOSTS;
        $sambaHosts[$i]->{"name"} = $sambaHosts[$i]->{$attributeDef->{$sambaHosts[$i]->{"node_type"}}->{"dn_value"}};
        $sambaHosts[$i]->{"domain_id"} = $domainId;
        $sambaHosts[$i]->{"dn"} = &OBM::ldap::makeDn( $sambaHosts[$i], $parentDn );

        $i++;
    }

    # On referme la connexion a la base
    if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
        &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "W" );
    }

    return \@sambaHosts;
}


sub createLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};

    # Les parametres nécéssaires
    if( $entry->{"host_uid"} && $entry->{"host_gid"} && $entry->{"host_name"} && $entry->{"host_sid"} && $entry->{"host_group_sid"} && $entry->{"host_lm_passwd"} && $entry->{"host_nt_passwd"} ) {

        $ldapEntry->add(
            objectClass => $attributeDef->{$type}->{"objectclass"},
            cn => to_utf8({ -string => $entry->{"host_login"}, -charset => $defaultCharSet }),
            sn => to_utf8({ -string => $entry->{"host_login"}, -charset => $defaultCharSet }),
            uid => to_utf8({ -string => $entry->{"host_login"}, -charset => $defaultCharSet }),
            sambaSID => $entry->{"host_sid"},
            sambaPrimaryGroupSID => $entry->{"host_group_sid"},
            sambaAcctFlags => $entry->{"host_acct_flags"},
            sambaLMPassword => $entry->{"host_lm_passwd"},
            sambaNTPassword => $entry->{"host_nt_passwd"}
        );

    }else {
        return 0;
    }

    # La description
    if( $entry->{"host_desc"} ) {
        $ldapEntry->add( description => to_utf8({ -string => $entry->{"host_desc"}, -charset => $defaultCharSet }) );
        $ldapEntry->add( displayName => to_utf8({ -string => $entry->{"host_desc"}, -charset => $defaultCharSet }) );
    }

    # Le domaine OBM
    if( $entry->{"host_obm_domain"} ) {
        $ldapEntry->add( obmDomain => to_utf8({ -string => $entry->{"host_obm_domain"}, -charset => $defaultCharSet }) );
    }

    return 1;
}


sub updateLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};
    my $update = 0;

    # Vérification du nom d'hôte
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"host_login"}, $ldapEntry, "cn" ) ) {
        $update = 1;
    };

    # Vérification du nom d'hôte
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"host_login"}, $ldapEntry, "sn" ) ) {
        $update = 1;
    };

    # Vérification du nom d'hôte
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"host_login"}, $ldapEntry, "uid" ) ) {
        $update = 1;
    };

    # Vérification du SID de l'hôte
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"host_sid"}, $ldapEntry, "sambaSID" ) ) {
        $update = 1;
    };

    # Vérification du SID du groupe des hôtes
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"host_group_sid"}, $ldapEntry, "sambaPrimaryGroupSID" ) ) {
        $update = 1;
    };

    # Vérification des flags Samba des hôtes
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"host_acct_flags"}, $ldapEntry, "sambaAcctFlags" ) ) {
        $update = 1;
    };

    # Vérification du domaine OBM
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"host_obm_domain"}, $ldapEntry, "obmDomain" ) ) {
        $update = 1;
    };

    return $update;
}


