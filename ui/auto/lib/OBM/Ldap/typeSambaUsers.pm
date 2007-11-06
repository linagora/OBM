package OBM::Ldap::typeSambaUsers;

require Exporter;

use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
require OBM::passwd;
require OBM::toolBox;
require OBM::dbUtils;
require OBM::Ldap::utils;
require OBM::Ldap::sambaUtils;
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
    my $query = "SELECT userobm_login, userobm_password_type, userobm_password, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid, userobm_samba_home, userobm_samba_home_drive, userobm_samba_logon_script, samba_value FROM P_UserObm LEFT JOIN P_Samba ON samba_domain_id=userobm_domain_id AND samba_name=\"samba_profile\" WHERE userobm_samba_perms='1' AND userobm_domain_id=".$main::domainList->[$domainId]->{"domain_id"};

    # On execute la requete
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete des utilisateurs : ".$dbHandler->err, "W" );
        return undef;
    }

    # On range les resultats dans la structure de donnees des resultats
    my $i = 0;
    my @users = ();
    while( my( $user_login, $user_password_type, $user_password, $user_lastname, $user_firstname, $user_uid, $user_gid, $user_smb_home, $user_smb_home_drive, $user_smb_logon_script, $user_smb_profile ) = $queryResult->fetchrow_array ) {

        &OBM::toolBox::write_log( "Gestion de l'utilisateur '".$user_login."'", "W" );


        # Gestion du mot de passe
        if( !defined($user_password_type) || (uc($user_password_type) ne "PLAIN") ) {
            &OBM::toolBox::write_log( "Erreur: le mot de passe doit etre de type 'PLAIN'", "W" );
            next;
        }
        my $errorCode = &OBM::passwd::getNTLMPasswd( $user_password, \$users[$i]->{"host_lm_passwd"}, \$users[$i]->{"host_nt_passwd"} );
        if( $errorCode ) {
            &OBM::toolBox::write_log( "Erreur: lors de la generation du mot de passe de l'utilisateur : '".$user_login."'", "W" );
            next;
        }

        # Gestion du SID de l'utilisateur
        my $userSid = &OBM::Ldap::sambaUtils::getUserSID( $main::domainList->[$domainId]->{"domain_samba_sid"}, $user_uid );
        if( !defined($userSid) ) {
            &OBM::toolBox::write_log( "Erreur: lors de la generation du SID de l'utilisateur : '".$user_login."'", "W" );
            next;
        }

        # Gestion du SID du groupe principal de l'utilisateur
        my $userGroupSid = &OBM::Ldap::sambaUtils::getGroupSID( $main::domainList->[$domainId]->{"domain_samba_sid"}, $user_gid );
        if( !defined($userGroupSid) ) {
            &OBM::toolBox::write_log( "Erreur: lors de la generation du SID du groupe principal de l'utilisateur : '".$user_login."'", "W" );
            next;
        }

        # On cree la structure correspondante à l'utilisateur
        $users[$i]->{"user_login"} = $user_login;
        $users[$i]->{"user_sid"} = $userSid;
        $users[$i]->{"user_group_sid"} = $userGroupSid;
        $users[$i]->{"user_smb_logon_script"} = $user_smb_logon_script;
        $users[$i]->{"user_smb_flags"} = "[U]";

        if( defined( $user_firstname ) ) {
            $users[$i]->{"user_display_name"} = $user_firstname;
        }
        if( defined($user_lastname) ) {
            $users[$i]->{"user_display_name"} .= " ".$user_lastname;
        }

        # La lettre pour connecter le lecteur personnel
        if( defined( $user_smb_home_drive ) && ( $user_smb_home_drive ne "" ) ) {
            $users[$i]->{"user_smb_home_drive"} = $user_smb_home_drive.":";
            $users[$i]->{"user_smb_home"} = $user_smb_home;
        }


        # Le répertoire du profil
        if( $user_smb_profile ) {
            $user_smb_profile =~ s/\%u/$user_login/g;
            $user_smb_profile .= "\\w2k";
            $users[$i]->{"user_smb_profile"} = $user_smb_profile;
        }else {
            $users[$i]->{"user_smb_profile"} = undef;
        }


        # On ajoute les informations de la structure
        $users[$i]->{"node_type"} = $SAMBAUSERS;
        $users[$i]->{"name"} = $users[$i]->{$attributeDef->{$users[$i]->{"node_type"}}->{"dn_value"}};
        $users[$i]->{"domain_id"} = $domainId;
        $users[$i]->{"dn"} = &OBM::ldap::makeDn( $users[$i], $parentDn );

        $i++;
    }
 
    #
    # On referme la connexion a la base
    if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
        &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "W" );
    }

    return \@users;
}


sub createLdapEntry {
    my ( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};


    # Les parametres nécessaires
    if( $entry->{"user_login"} && $entry->{"user_sid"} ) {

        $ldapEntry->add(
            objectClass => $attributeDef->{$type}->{"objectclass"},
            uid => to_utf8({ -string => $entry->{"user_login"}, -charset => $defaultCharSet }),
            sambaSID => $entry->{"user_sid"},
            sambaAcctFlags => $entry->{"user_smb_flags"}
        );

    }else {
        return 0;
    }

    # Le SID du groupe
    if( $entry->{"user_group_sid"} ) {
        $ldapEntry->add( sambaPrimaryGroupSID => $entry->{"user_group_sid"} );
    }

    # Les mots de passes
    if( $entry->{"host_lm_passwd"} && $entry->{"host_nt_passwd"} ) {
        $ldapEntry->add(
            sambaLMPassword => $entry->{"host_lm_passwd"},
            sambaNTPassword => $entry->{"host_nt_passwd"}
        );
    }

    # Le display name
    if( $entry->{"user_display_name"} ) {
        $ldapEntry->add( displayName => $entry->{"user_display_name"} );
    }

    # Le samba home path et drive
    if( $entry->{"user_smb_home"} && $entry->{"user_smb_home_drive"} ) {
        $ldapEntry->add(
            sambaHomePath => to_utf8({ -string => $entry->{"user_smb_home"}, -charset => $defaultCharSet }),
            sambaHomeDrive => to_utf8({ -string => $entry->{"user_smb_home_drive"}, -charset => $defaultCharSet })
        );
    }

    # Le logon script
    if( $entry->{"user_smb_logon_script"} ) {
        $ldapEntry->add( sambaLogonScript => to_utf8({ -string => $entry->{"user_smb_logon_script"}, -charset => $defaultCharSet }) );
    }

    # Le path du profil
    if( $entry->{"user_smb_profile"} ) {
        $ldapEntry->add( sambaProfilePath => to_utf8({ -string => $entry->{"user_smb_profile"}, -charset => $defaultCharSet }) );
    }


    return 1;
}


sub updateLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};
    my $update = 0;
    my $create = 0;

    # Objet LDAP non structurel - mise a jour de l'attributs 'objectClass'
    if( !$attributeDef->{$entry}->{"structural"} ) {
        my $errorCode = &OBM::Ldap::utils::addAttrList( $attributeDef->{$type}->{"objectclass"}, $ldapEntry, "objectClass" );

        if( !$errorCode ) {
            $update = 1;

            # Si on modifie les objectClass, on considère qu'on est en mode
            # création de l'entité
            $create = 1;
        }elsif( $errorCode != 1 ) {
            return $update;
        }
    }

    if( $create ) {
        if( &OBM::Ldap::utils::modifyAttr( $entry->{"host_lm_passwd"}, $ldapEntry, "sambaLMPassword" ) ) {
            $update = 1;
        }

        if( &OBM::Ldap::utils::modifyAttr( $entry->{"host_nt_passwd"}, $ldapEntry, "sambaNTPassword" ) ) {
            $update = 1;
        }
    }

    # Les flags Windows
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_smb_flags"}, $ldapEntry, "sambaAcctFlags" ) ) {
        $update = 1;
    }

    # Le SID de l'utilisateur
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_sid"}, $ldapEntry, "sambaSID" ) ) {
        $update = 1;
    }

    # Le SID du groupe primaire
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_group_sid"}, $ldapEntry, "sambaPrimaryGroupSID" ) ) {
        $update = 1;
    }

    # Le displayName
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_display_name"}, $ldapEntry, "displayName" ) ) {
        $update = 1;
    }

    # Le repertoire Home samba
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_smb_home"}, $ldapEntry, "sambaHomePath" ) ) {
        $update = 1;
    }

    # La lettre du lecteur de montage du repertoire Home samba
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_smb_home_drive"}, $ldapEntry, "sambaHomeDrive" ) ) {
        $update = 1;
    }

    # Le script de logon
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_smb_logon_script"}, $ldapEntry, "sambaLogonScript" ) ) {
        $update = 1;
    }

    # Le repertoire du profil
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_smb_profile"}, $ldapEntry, "sambaProfilePath" ) ) {
        $update = 1;
    }


    return $update;
}


sub updatePasswd {
    my( $ldapEntry, $passwdType, $newPasswd ) = @_;
    my $update = 0;
    my $lmPasswd;
    my $ntPasswd;

    # Gestion du mot de passe
    if( !defined($passwdType) || (uc($passwdType) ne "PLAIN") ) {
        &OBM::toolBox::write_log( "Erreur: le mot de passe doit etre de type 'PLAIN'", "W" );
        next;
    }
    my $errorCode = &OBM::passwd::getNTLMPasswd( $newPasswd, \$lmPasswd, \$ntPasswd );
    if( $errorCode ) {
        return $update;
    }

    if( &OBM::Ldap::utils::modifyAttr( $lmPasswd, $ldapEntry, "sambaLMPassword" ) ) {
        $update = 1;
    }

    if( &OBM::Ldap::utils::modifyAttr( $ntPasswd, $ldapEntry, "sambaNTPassword" ) ) {
        $update = 1;
    }

    return $update;
}
