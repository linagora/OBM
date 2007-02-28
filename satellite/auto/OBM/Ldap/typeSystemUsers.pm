package OBM::Ldap::typeSystemUsers;

require Exporter;

use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
require OBM::passwd;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);


#
# Necessaire pour le bon fonctionnement du package
$debug=1;


sub initStruct {
    return 1;
}


sub getDbValues {
    my( $parentDn, $domainId ) = @_;


    #
    # On se connecte a la base
    my $dbHandler;
    if( !&OBM::dbUtils::dbState( "connect", \$dbHandler ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'ouverture de la base de donnee : ".$dbHandler->err, "WC" );
        return undef;
    }

    #
    # La requete a executer - obtention des informations sur les utilisateurs systeme
    my $query = "SELECT usersystem_login, usersystem_password, usersystem_uid, usersystem_gid, usersystem_homedir, usersystem_shell, usersystem_firstname, usersystem_lastname FROM UserSystem";
    #
    # On execute la requete
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete des utilisateurs systemes : ".$dbHandler->err, "W" );
        return undef;
    }

    #
    # On range les resultats dans la structure de donnees des resultats
    my $i = 0;
    my @users = ();
    while( my( $user_login, $user_password, $user_uid, $user_gid, $user_homedir, $user_shell, $user_firstname, $user_lastname ) = $queryResult->fetchrow_array ) {
        #
        # On cree la structure correspondante a l'utilisateur
        # Cette structure est composee des valuers recuperees dans la base +
        # la valeur de la racine de l'annuaire + la valeur de
        # l''organizationalUnit' sous laquelle vont etre placee les
        # utilisateurs ('users' dans notre cas).
        $users[$i] = {
                    "user_login"=>$user_login,
                    "user_password"=>$user_password,
                    "user_uid"=>$user_uid,
                    "user_gid"=>$user_gid,
                    "user_lastname"=>$user_lastname,
                    "user_firstname"=>$user_firstname,
                    "user_homedir"=>$user_homedir,
                    "user_shell"=>$user_shell,
                    "rootLdap"=>$results{"rootLdap"}
        };

        #
        # On ajoute les informations de la structure
        $users[$i]->{"node_type"} = $SYSTEMUSERS;
        $users[$i]->{"name"} = $users[$i]->{$attributeDef->{$users[$i]->{"node_type"}}->{"dn_value"}};
        $users[$i]->{"domain_id"} = $domainId;
        $users[$i]->{"dn"} = &OBM::ldap::makeDN( $users[$i], $parentDn );

        $i++;
    }


    # On referme la connexion a la base
    if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
        &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "W" );
    }

    return \@users;
}


sub createLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};


    # On construit la nouvelle entree
    # Les parametres ncecessaires
    if( $entry->{"user_login"} && $entry->{"user_firstname"} && $entry->{"user_lastname"} && $entry->{"user_uid"} && $entry->{"user_gid"} && $entry->{"user_homedir"} && $entry->{"user_password"} ) {

        my $longName = $entry->{"user_firstname"}." ".$entry->{"user_lastname"};
        my $sshaPasswd = "{SSHA}".&OBM::passwd::toSsha( $entry->{"user_password"} );

        $ldapEntry->add(
            objectClass => $attributeDef->{$type}->{"objectclass"},
            uid => $entry->{"user_login"},
            cn => to_utf8({ -string => $longName, -charset => $defaultCharSet }),
            sn => to_utf8({ -string => $longName, -charset => $defaultCharSet }),
            uidNumber => $entry->{"user_uid"},
            gidNumber => $entry->{"user_gid"},
            homeDirectory => $entry->{"user_homedir"},
            loginShell => "/bin/bash",
            userpassword => $sshaPasswd
        );

    }else {
        return 0;
    }

    return 1;
}


sub updateLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};
    my $update = 0;


    my $longName = $entry->{"user_firstname"}." ".$entry->{"user_lastname"};
    # Le champs nom, prenom de l'utilisateur
    if( &OBM::Ldap::utils::modifyAttr( $longName, $ldapEntry, "cn" ) ) {
        # On synchronise le surname
        &OBM::Ldap::utils::modifyAttr( $longName, $ldapEntry, "sn" );

        $update = 1;
    }

    return $update;
}
