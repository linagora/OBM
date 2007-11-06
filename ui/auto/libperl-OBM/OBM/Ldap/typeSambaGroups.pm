package OBM::Ldap::typeSambaGroups;

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

    # La requete a executer - obtention des informations sur les utilisateurs de
    # l'organisation.
    my $query = "SELECT group_id, group_gid, group_name FROM P_UGroup WHERE group_samba=1 AND group_domain_id=".$main::domainList->[$domainId]->{"domain_id"};

    # On execute la requete
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete des utilisateurs : ".$dbHandler->err, "W" );
        return undef;
    }

    # On range les resultats dans la structure de donnees des resultats
    my $i = 0;
    my @groups = ();
    while( my( $group_id, $group_gid, $group_name ) = $queryResult->fetchrow_array ) {

        &OBM::toolBox::write_log( "Gestion de l'utilisateur '".$group_name."'", "W" );


        # On cree la structure correspondante au groupe
        # Cette structure est composee des valeurs recuperees dans la base +
        # la valeur de la racine de l'annuaire
        $groups[$i] = {
            "group_gid" => $group_gid,
            "group_name" => $group_name,
            "group_smb_sid" => $main::domainList->[$domainId]->{"domain_samba_sid"}."-".$group_gid,
            "group_smb_users" => getGroupUsersSid( $dbHandler, $group_id, $main::domainList->[$domainId]->{"domain_samba_sid"} ),
            "group_smb_group_type" => "2"
        };

        # On ajoute les informations de la structure
        $groups[$i]->{"node_type"} = $SAMBAGROUPS;
        $groups[$i]->{"name"} = $groups[$i]->{$attributeDef->{$groups[$i]->{"node_type"}}->{"dn_value"}};
        $groups[$i]->{"domain_id"} = $domainId;
        $groups[$i]->{"dn"} = &OBM::ldap::makeDn( $groups[$i], $parentDn );

        $i++;
    }
 
    #
    # On referme la connexion a la base
    if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
        &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "W" );
    }

    return \@groups;
}


sub createLdapEntry {
    my ( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};


    # Les parametres nécessaires
    if( $entry->{"group_name"} && $entry->{"group_sid"} ) {

        $ldapEntry->add(
            objectClass => $attributeDef->{$type}->{"objectclass"},
            cn => to_utf8({ -string => $entry->{"group_name"}, -charset => $defaultCharSet }),
            sambaSID => $entry->{"group_smb_sid"}
        );

    }else {
        return 0;
    }

    # Le type windows du groupe
    if( $entry->{"group_smb_group_type"} ) {
        $ldapEntry->add( sambaGroupType => $entry->{"group_smb_group_type"} );
    }

    # La liste des utilisateurs du groupe
    if( $entry->{"group_smb_users"} ) {
        $ldapEntry->add( sambaSIDList => $entry->{"group_smb_users"} );
    }

    return 1;
}


sub updateLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};
    my $update = 0;

    # Objet LDAP non structurel - mise a jour de l'attributs 'objectClass'
    if( !$attributeDef->{$entry}->{"structural"} ) {
        my $errorCode = &OBM::Ldap::utils::addAttrList( $attributeDef->{$type}->{"objectclass"}, $ldapEntry, "objectClass" );

        if( !$errorCode ) {
            $update = 1;
        }elsif( $errorCode != 1 ) {
            return $update;
        }
    }

    # Le SID du groupe
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"group_smb_sid"}, $ldapEntry, "sambaSID" ) ) {
        $update = 1;
    }

    # Le type windows du groupe
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"group_smb_group_type"}, $ldapEntry, "sambaGroupType" ) ) {
        $update = 1;
    }

    # La liste des utilisateurs du groupe
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"group_smb_users"}, $ldapEntry, "sambaSIDList" ) ) {
        $update = 1;
    }

    return $update;
}


sub getGroupUsersSid {
    my( $dbHandler, $groupId, $sambaSid ) = @_;
    my @sidList;


    # Recuperation de la liste d'utilisateur de ce groupe id : $groupId.
    my $query = "SELECT i.userobm_uid FROM P_UserObm i, P_UserObmGroup j WHERE j.userobmgroup_group_id=".$groupId." AND j.userobmgroup_userobm_id=i.userobm_id AND i.userobm_samba_perms=1";

    # On execute la requete
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        write_log( "Probleme lors de l'execution de la requete : ".$queryResult->err, "W" );
        return undef;
    }

    # On stocke le resultat dans le tableau des resultats
    while( my( $userUid ) = $queryResult->fetchrow_array ) {
        push( @sidList, $sambaSid."-".$userUid );
    }

    # Recuperation de la liste des groupes du groupe id : $groupId.
    $query = "SELECT groupgroup_child_id FROM P_GroupGroup WHERE groupgroup_parent_id=".$groupId;

    # On execute la requete
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        write_log( "Probleme lors de l'execution de la requete : ".$queryResult->err, "W" );
        return undef;
    }

    # On traite les resultats
    while( my( $groupGroupId ) = $queryResult->fetchrow_array ) {
        my $userGroupTmp = getGroupUsersSid( $dbHandler, $groupGroupId, $sambaSid );

        for( my $i=0; $i<=$#$userGroupTmp; $i++ ) {
            my $j =0;
            while( ($j<=$#sidList) && ($$userGroupTmp[$i] ne $sidList[$j]) ) {
                $j++;
            }

            if( $j>$#sidList ) {
                push( @sidList, $sambaSid."-".$$userGroupTmp[$i] );
            }
        }
    }

    return \@sidList;
}
