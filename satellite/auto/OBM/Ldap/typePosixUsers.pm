package OBM::Ldap::typePosixUsers;

require Exporter;

use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
require OBM::Ldap::utils;
require OBM::passwd;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
use strict;


sub initStruct {
    return 1;
}


sub getDbValues {
    my( $parentDn, $domainId ) = @_;


    if( !defined($main::domainList->[$domainId]->{"domain_id"}) ) {
        &OBM::toolBox::write_log( "Identifiant de domaine non définie", "W" );
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
    my $query = "SELECT userobm_id, userobm_login, userobm_password_type, userobm_password, userobm_uid, userobm_gid, userobm_lastname, userobm_firstname, userobm_address1, userobm_address2, userobm_address3, userobm_zipcode, userobm_town, userobm_title, userobm_service, userobm_description, userobm_mail_perms, userobm_mail_ext_perms, userobm_email, mailserver_host_id, userobm_web_perms, userobm_phone, userobm_phone2, userobm_fax, userobm_fax2, userobm_mobile FROM P_UserObm JOIN P_MailServer ON userobm_mail_server_id=mailserver_id WHERE userobm_archive=0 AND userobm_domain_id=".$main::domainList->[$domainId]->{"domain_id"};

    # On execute la requete
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete des utilisateurs : ".$dbHandler->err, "W" );
        return undef;
    }

    # On range les resultats dans la structure de donnees des resultats
    my $i = 0;
    my @users = ();
    while( my( $user_id, $user_login, $user_passwd_type, $user_passwd, $user_uid, $user_gid, $user_lastname, $user_firstname, $user_address1, $user_address2, $user_address3, $user_zipcode, $user_town, $user_title, $user_service, $user_description, $user_mail_perms, $user_mail_ext_perms, $user_email, $user_mail_server_id, $user_web_perms, $user_phone, $user_phone2, $user_fax, $user_fax2, $user_mobile ) = $queryResult->fetchrow_array ) {

        &OBM::toolBox::write_log( "Gestion de l'utilisateur '".$user_login."'", "W" );
        
        #
        # On cree la structure correspondante a l'utilisateur
        # Cette structure est composee des valeurs recuperees dans la base +
        # la valeur de la racine de l'annuaire
        $users[$i] = {
            "user_id"=>$user_id,
            "user_login"=>$user_login,
            "user_uid"=>$user_uid,
            "user_gid"=>$user_gid,
            "user_lastname"=>$user_lastname,
            "user_firstname"=>$user_firstname,
            "user_homedir"=>"$baseHomeDir/$user_login",
            "user_mailperms"=>$user_mail_perms,
            "user_webperms"=>$user_web_perms,
            "user_passwd_type"=>$user_passwd_type,
            "user_passwd"=>$user_passwd,
            "user_title"=>$user_title,
            "user_service"=>$user_service,
            "user_description"=>$user_description,
            "user_zipcode"=>$user_zipcode,
            "user_town"=>$user_town,
            "user_mobile"=>$user_mobile,
            "user_domain" => $main::domainList->[$domainId]->{"domain_label"}
            };

        # gestion de l'adresse
        if( defined($user_address1) && ($user_address1 ne "") ) {
            $users[$i]->{"user_address"} = $user_address1;
        }
        
        if( defined($user_address2) && ($user_address2 ne "") ) {
            $users[$i]->{"user_address"} .= "\r\n".$user_address2;
        }
        
        if( defined($user_address3) && ($user_address3 ne "") ) {
            $users[$i]->{"user_address"} .= "\r\n".$user_address3;
        }
        

        # Gestion du téléphone
        if( defined($user_phone) && ($user_phone ne "") ) {
            push( @{$users[$i]->{"user_phone"}}, $user_phone );
        }

        if( defined($user_phone2) && ($user_phone2 ne "") ) {
            push( @{$users[$i]->{"user_phone"}}, $user_phone2 );
        }

        # Gestion du fax
        if( defined($user_fax) && ($user_fax ne "") ) {
            push( @{$users[$i]->{"user_fax"}}, $user_fax );
        }

        if( defined($user_fax2) && ($user_fax2 ne "") ) {
            push( @{$users[$i]->{"user_fax"}}, $user_fax2 );
        }

        # Gestion du droit de messagerie
        if( $user_mail_perms ) {
            my $localServerIp = &OBM::toolBox::getHostIpById( $dbHandler, $user_mail_server_id );

            if( !defined($localServerIp) ) {
                &OBM::toolBox::write_log( "Droit mail de l'utilisateur '".$user_login."' annule - Serveur inconnu !", "W" );
                $users[$i]->{"user_mailperms"} = 0;

            }else {

                # Limite la messagerie aux domaines locaux
                if( !$user_mail_ext_perms ) {
                    $users[$i]->{"user_mailLocalOnly"} = "local_only";
                }

                # Gestions des e-mails des utilisateurs.
                my @email = split( /\r\n/, $user_email );
                for( my $j=0; $j<=$#email; $j++ ) {
                    push( @{$users[$i]->{"user_email"}}, $email[$j]."@".$main::domainList->[$domainId]->{"domain_name"} );

                    for( my $k=0; $k<=$#{$main::domainList->[$domainId]->{"domain_alias"}}; $k++ ) {
                        push( @{$users[$i]->{"user_email_alias"}}, $email[$j]."@".$main::domainList->[$domainId]->{"domain_alias"}->[$k] );
                    }
                }

                # Gestion des BAL destination
                $users[$i]->{"user_mailbox"} = $users[$i]->{"user_login"}."@".$main::domainList->[$domainId]->{"domain_name"};

                # On ajoute le serveur de mail associé
                $users[$i]->{"user_mailLocalServer"} = "lmtp:".$localServerIp.":24";
            }
        }

        # On ajoute les informations de la structure
        $users[$i]->{"node_type"} = $POSIXUSERS;
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

    #
    # Gestion du mot de passe
    if( !defined( $entry->{"user_passwd_type"} ) || ($entry->{"user_passwd_type"} eq "") ) {
        return 0;
    }

    my $userPasswd = &OBM::passwd::convertPasswd( $entry->{"user_passwd_type"}, $entry->{"user_passwd"} );
    if( !defined( $userPasswd ) ) {
        return 0;
    }


    #
    # On construit la nouvelle entree
    #
    # Les parametres nececessaires
    if( $entry->{"user_login"} && $entry->{"user_lastname"} && defined($entry->{"user_uid"}) && ($entry->{"user_uid"} ne "") && defined($entry->{"user_gid"})  && $entry->{"user_homedir"} ) {

        # Creation de la valeur du champs CN
        my $longName;
        if( $entry->{"user_firstname"} ) {
            $longName = $entry->{"user_firstname"}." ".$entry->{"user_lastname"};
        }else {
            $longName = $entry->{"user_lastname"};
        }
                
        $ldapEntry->add(
            objectClass => $attributeDef->{$type}->{"objectclass"},
            uid => $entry->{"user_login"},
            cn => to_utf8({ -string => $longName, -charset => $defaultCharSet }),
            sn => to_utf8({ -string => $entry->{"user_lastname"}, -charset => $defaultCharSet }),
            displayName => to_utf8({ -string => $longName, -charset => $defaultCharSet }),
            uidNumber => $entry->{"user_uid"},
            gidNumber => $entry->{"user_gid"},
            homeDirectory => $entry->{"user_homedir"},
            loginShell => "/bin/bash"
        );

    }else {
        return 0;
    }

    # Le prenom
    if( $entry->{"user_firstname"} ) {
        $ldapEntry->add( givenName => to_utf8({ -string => $entry->{"user_firstname"}, -charset => $defaultCharSet }) );
    }

    # Le mot de passe
    if( $userPasswd ) {
        $ldapEntry->add( userPassword => $userPasswd );
    }

    # Le telephone
    if( $entry->{"user_phone"} ) {
        $ldapEntry->add( telephoneNumber => $entry->{"user_phone"} );
    }

    # Le fax
    if( $entry->{"user_fax"} ) {
        $ldapEntry->add( facsimileTelephoneNumber => $entry->{"user_fax"} );
    }

    # Le mobile
    if( $entry->{"user_mobile"} ) {
        $ldapEntry->add( mobile => $entry->{"user_mobile"} );
    }

    # Le titre
    if( $entry->{"user_title"} ) {
        $ldapEntry->add( title => to_utf8({ -string => $entry->{"user_title"}, -charset => $defaultCharSet }) );
    }

    # Le service
    if( $entry->{"user_service"} ) {
        $ldapEntry->add( ou => to_utf8({ -string => $entry->{"user_service"}, -charset => $defaultCharSet }) );
    }

    # La description
    if( $entry->{"user_description"} ) {
        $ldapEntry->add( description => to_utf8({ -string => $entry->{"user_description"}, -charset => $defaultCharSet }) );
    }

    # L'acces web
    if( $entry->{"user_webperms"} || ( defined( $entry->{"user_webperms"} ) && ($entry->{"user_webperms"} == 0) ) ) {
        $ldapEntry->add( webAccess => $entry->{"user_webperms"} );
    }

    # La boite a lettres de l'utilisateur
    if( $entry->{"user_mailbox"} ) {
        $ldapEntry->add( mailBox => $entry->{"user_mailbox"} );
    }

    # Le serveur de BAL local
    if( $entry->{"user_mailLocalServer"} ) {
        $ldapEntry->add( mailBoxServer => $entry->{"user_mailLocalServer"} );
    }

    # L'acces mail
    if( $entry->{"user_mailperms"} ) {
        $ldapEntry->add( mailAccess => "PERMIT" );
    }else {
        $ldapEntry->add( mailAccess => "REJECT" );
    }

    # La limite aux domaines locaux
    if( $entry->{"user_mailLocalOnly"} ) {
        $ldapEntry->add( mailLocalOnly => $entry->{"user_mailLocalOnly"} );
    }

    # Les adresses mails
    if( $entry->{"user_email"} && ($#{$entry->{"user_email"}} != -1) ) {
        $ldapEntry->add( mail => $entry->{"user_email"} );
    }

    # Les adresses mail secondaires
    if( $entry->{"user_email_alias"} && ($#{$entry->{"user_email_alias"}} != -1) ) {
        $ldapEntry->add( mailAlias => $entry->{"user_email_alias"} );
    }

    # L'adresse postale
    if( $entry->{"user_address"} ) {
        # Thunderbird, IceDove... : ne comprennent que cet attribut
        $ldapEntry->add( street => to_utf8({ -string => $entry->{"user_address"}, -charset => $defaultCharSet }) );
        # Outlook : ne comprend que cet attribut
        # Outlook Express : préfère celui-là à 'street'
        $ldapEntry->add( postalAddress => to_utf8({ -string => $entry->{"user_address"}, -charset => $defaultCharSet }) );
    }

    # Le code postal
    if( $entry->{"user_zipcode"} ) {
        $ldapEntry->add( postalCode => to_utf8({ -string => $entry->{"user_zipcode"}, -charset => $defaultCharSet }) );
    }

    # La ville
    if( $entry->{"user_town"} ) {
        $ldapEntry->add( l => to_utf8({ -string => $entry->{"user_town"}, -charset => $defaultCharSet }) );
    }

    # Le domaine
    if( $entry->{"user_domain"} ) {
        $ldapEntry->add( obmDomain => to_utf8({ -string => $entry->{"user_domain"}, -charset => $defaultCharSet }) );
    }

    return 1;
}


sub updateLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};
    my $update = 0;

    # Le champs nom, prenom de l'utilisateur
    my $longName;
    if( $entry->{"user_firstname"} ) {
        $longName = $entry->{"user_firstname"}." ".$entry->{"user_lastname"};
    }else {
        $longName = $entry->{"user_lastname"};
    }

    if( &OBM::Ldap::utils::modifyAttr( $longName, $ldapEntry, "cn" ) ) {
        # On synchronise le nom affichable
        &OBM::Ldap::utils::modifyAttr( $longName, $ldapEntry, "displayName" );

        $update = 1;
    }

    # Le nom de famille
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_lastname"}, $ldapEntry, "sn" ) ) {
        $update = 1;
    }

    # Le prenom
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_firstname"}, $ldapEntry, "givenName" ) ) {
        $update = 1;
    }

    # Le titre
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_title"}, $ldapEntry, "title" ) ) {
        $update = 1;
    }

    # Le service
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_service"}, $ldapEntry, "ou" ) ) {
        $update = 1;
    }

    # La description
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_description"}, $ldapEntry, "description" ) ) {
        $update = 1;
    }

    # L'adresse
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_address"}, $ldapEntry, "street" ) ) {
        &OBM::Ldap::utils::modifyAttr( $entry->{"user_address"}, $ldapEntry, "postalAddress" );
        $update = 1;
    }

    # Le code postal
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_zipcode"}, $ldapEntry, "postalCode" ) ) {
        $update = 1;
    }

    # La ville
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_town"}, $ldapEntry, "l" ) ) {
        $update = 1;
    }

    # Le repertoire personnel
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_homedir"}, $ldapEntry, "homeDirectory" ) ) {
        $update = 1;
    }
            
    # L'UID
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_uid"}, $ldapEntry, "uidNumber" ) ) {
        $update = 1;
    }

    # Le GID
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_gid"}, $ldapEntry, "gidNumber" ) ) {
        $update = 1;
    }

    # Le telephone
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"user_phone"}, $ldapEntry, "telephoneNumber" ) ) {
        $update = 1;
    }

    # Le fax
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"user_fax"}, $ldapEntry, "facsimileTelephoneNumber" ) ) {
        $update = 1;
    }

    # Le mobile
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_mobile"}, $ldapEntry, "mobile" ) ) {
        $update = 1;
    }

    # L'acces au web
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_webperms"}, $ldapEntry, "webAccess" ) ) {
        $update = 1;
    }

    # Le domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_domain"}, $ldapEntry, "obmDomain") ) {
        $update = 1;
    }

    # L'acces au mail
    if( $entry->{"user_mailperms"} && ( &OBM::Ldap::utils::modifyAttr( "PERMIT", $ldapEntry, "mailAccess" ) ) ) {
        $update = 1;

    }elsif( !$entry->{"user_mailperms"} && ( &OBM::Ldap::utils::modifyAttr( "REJECT", $ldapEntry, "mailAccess" ) ) ) {
        $update = 1;

    }

    # La boite a lettres de l'utilisateur
    if( !$entry->{"user_mailperms"} ) {
        if( &OBM::Ldap::utils::modifyAttr( undef, $ldapEntry, "mailBox" ) ) {
            $update = 1;
        }

    }elsif( &OBM::Ldap::utils::modifyAttr( $entry->{"user_mailbox"}, $ldapEntry, "mailBox" ) ) {
        $update = 1;
    }

    # Le serveur de BAL local
    if( !$entry->{"user_mailperms"} ) {
        if( &OBM::Ldap::utils::modifyAttr( undef, $ldapEntry, "mailBoxServer" ) ) {
            $update = 1;
        }

    }elsif( &OBM::Ldap::utils::modifyAttr( $entry->{"user_mailLocalServer"}, $ldapEntry, "mailBoxServer" ) ) {
        $update = 1;
    }

    # La limitation au domaine local
    if( !$entry->{"user_mailperms"} ) {
        if( &OBM::Ldap::utils::modifyAttr( undef, $ldapEntry, "mailLocalOnly" ) ) {
            $update = 1;
        }

    }elsif( &OBM::Ldap::utils::modifyAttr( $entry->{"user_mailLocalOnly"}, $ldapEntry, "mailLocalOnly" ) ) {
        $update = 1;
    }

    # Le cas des adresses mails
    if( !$entry->{"user_mailperms"} ) {
        # Adresse principales
        if( &OBM::Ldap::utils::modifyAttrList( undef, $ldapEntry, "mail" ) ) {
            $update = 1;
        }

        # Adresses secondaires
        if( &OBM::Ldap::utils::modifyAttrList( undef, $ldapEntry, "mailAlias" ) ) {
            $update = 1;
        }


    }else {
        # Adresse principales
        if( &OBM::Ldap::utils::modifyAttrList( $entry->{"user_email"}, $ldapEntry, "mail" ) ) {
            $update = 1;
        }

        # Adresses secondaires
        if( &OBM::Ldap::utils::modifyAttrList( $entry->{"user_email_alias"}, $ldapEntry, "mailAlias" ) ) {
            $update = 1;
        }
    }

    return $update;
}


sub updatePasswd {
    my( $ldapEntry, $passwdType, $newPasswd ) = @_;
    my $update = 0;

    my $userPasswd = &OBM::passwd::convertPasswd( $passwdType, $newPasswd );
    if( !defined( $userPasswd ) ) {
        return 0;
    }

    if( &OBM::Ldap::utils::modifyAttr( $userPasswd, $ldapEntry, "userPassword" ) ) {
        $update = 1;
    }

    return $update;
}
