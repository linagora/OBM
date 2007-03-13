package OBM::Ldap::typePosixGroups;

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
    my $query = "SELECT group_id, group_gid, group_name, group_desc, group_email, group_contacts FROM P_UGroup WHERE group_privacy=0 AND group_domain_id=".$main::domainList->[$domainId]->{"domain_id"};

    # On execute la requete
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete des groupes : ".$dbHandler->err, "W" );
        return undef;
    }

    # On range les resultats dans la structure de donnees des resultats
    my $i = 0;
    my @groups = ();
    while( my( $group_id, $group_gid, $group_name, $group_desc, $group_email, $group_contacts ) = $queryResult->fetchrow_array ) {

        &OBM::toolBox::write_log( "Gestion du groupe : '".$group_name."'", "W" );
        
        # On range les resultats dans la structure de donnees des resultats
        $groups[$i]->{"group_gid"} = $group_gid;
        $groups[$i]->{"group_name"} = $group_name;
        $groups[$i]->{"group_desc"} = $group_desc;
        $groups[$i]->{"group_users"} = getGroupUsers( $group_id, $dbHandler );
        $groups[$i]->{"group_domain"} = $main::domainList->[$domainId]->{"domain_label"};

        if( $group_email ) {
            $groups[$i]->{"group_mailperms"} = 1;

            # L'adresse du groupe
            $group_email = lc($group_email);
            push( @{$groups[$i]->{"group_email"}}, $group_email."@".$main::domainList->[$domainId]->{"domain_name"} );

            for( my $j=0; $j<=$#{$main::domainList->[$domainId]->{"domain_alias"}}; $j++ ) {
                push( @{$groups[$i]->{"group_email_alias"}}, $group_email."@".$main::domainList->[$domainId]->{"domain_alias"}->[$j] );
            }

            # Gestion des utilisateurs de la liste
            $groups[$i]->{"group_contacts"} = getGroupUsersMailEnable( $group_id, $dbHandler );
            for( my $j=0; $j<=$#{$groups[$i]->{"group_contacts"}}; $j++ ) {
                $groups[$i]->{"group_contacts"}->[$j] .= "@".$main::domainList->[$domainId]->{"domain_name"};
            }

        }else {
            $groups[$i]->{"group_mailperms"} = 0;
        }

        # Gestion des contacts externes du groupe
        if( $group_contacts ) {
            my @email = split( /\r\n/, $group_contacts );
            my $j = 0;
            for( $j=0; $j<=$#email; $j++ ) {
                if( $email[$j] ) {
                    push( @{$groups[$i]->{"group_contacts"}}, $email[$j] );
                }
            }
        }

        # On ajoute les informations de la structure
        $groups[$i]->{"node_type"} = $POSIXGROUPS;
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
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};

    # Les parametres ncecessaires
    if( $entry->{"group_name"} && $entry->{"group_gid"} ) {
        $ldapEntry->add(
            objectClass => $attributeDef->{$type}->{"objectclass"},
            cn => to_utf8({ -string => $entry->{"group_name"}, -charset => $defaultCharSet }),
            gidNumber => $entry->{"group_gid"}
        );

    }else {
        return 0;
    }

    if( $#{$entry->{"group_users"}} != -1 ) {
        $ldapEntry->add( memberUid => $entry->{"group_users"} );
    }

    if( $entry->{"group_desc"} ) {
        $ldapEntry->add( description => to_utf8({ -string => $entry->{"group_desc"}, -charset => $defaultCharSet }) );       
    }

    # L'acces mail
    if( ($entry->{"group_email"}) && ($entry->{"group_mailperms"}) ) {
        $ldapEntry->add( mailAccess => "PERMIT" );
    }else {
        $ldapEntry->add( mailAccess => "REJECT" );
    }

    # Les adresses mails
    if( $#{$entry->{"group_email"}} != -1 ) {
        $ldapEntry->add( mail => $entry->{"group_email"} );
    }

    # Les adresses mails secondaires
    if( $#{$entry->{"group_email_alias"}} != -1 ) {
        $ldapEntry->add( mailAlias => $entry->{"group_email_alias"} );
    }
            
    # Les contacts externes
    if( $#{$entry->{"group_contacts"}} != -1 ) {
        $ldapEntry->add( mailBox => $entry->{"group_contacts"} );
    }

    # Le domaine
    if( $entry->{"group_domain"} ) {
        $ldapEntry->add( obmDomain => to_utf8({ -string => $entry->{"group_domain"}, -charset => $defaultCharSet }) );
    }

    return 1;
}


sub updateLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};
    my $update = 0;

    # verification du GID
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"group_gid"}, $ldapEntry, "gidNumber" ) ) {
        $update = 1;
    }

    # La description
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"group_desc"}, $ldapEntry, "description" ) ) {
        $update = 1;
    }

    # Les membres du groupe
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"group_users"}, $ldapEntry, "memberUid" ) ) {
        $update = 1;
    }

    # L'acces au mail
    if( $entry->{"group_mailperms"} && (&OBM::Ldap::utils::modifyAttr( "PERMIT", $ldapEntry, "mailAccess" )) ) {
        $update = 1;

    }elsif( !$entry->{"group_mailperms"} && (&OBM::Ldap::utils::modifyAttr( "REJECT", $ldapEntry, "mailAccess" )) ) {
        $update = 1;

    }

    # Le cas des alias mails
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"group_email"}, $ldapEntry, "mail" ) ) {
        $update = 1;
    }

    # Le cas des alias mails secondaires
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"group_email_alias"}, $ldapEntry, "mailAlias" ) ) {
        $update = 1;
    }

    # Le cas des contacts
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"group_contacts"}, $ldapEntry, "mailBox" ) ) {
        $update = 1;
    }

    # Le domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"group_domain"}, $ldapEntry, "obmDomain") ) {
        $update = 1;
    }

    return $update;
}


###############################################################################

sub getGroupUsersMailEnable {
    my( $groupId, $dbHandler ) = @_;

    return getGroupUsers( $groupId, $dbHandler, "AND i.userobm_mail_perms=1" );
}


sub getGroupUsers {
    my( $groupId, $dbHandler, $sqlRequest ) = @_;

    my @tabResult;
    my $queryResult;

    # Recuperation de la liste d'utilisateur de ce groupe id : $groupId.
    my $query = "SELECT i.userobm_login FROM UserObm i, UserObmGroup j WHERE j.userobmgroup_group_id=".$groupId." AND j.userobmgroup_userobm_id=i.userobm_id";

    if( defined( $sqlRequest ) && ($sqlRequest ne "") ) {
        $query .= " ".$sqlRequest;
    }
    
    # On execute la requete
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        write_log( "Probleme lors de l'execution de la requete : ".$queryResult->err, "W" );
        return undef;
    }

    # On stocke le resultat dans le tableau des resultats
    while( my( $userLogin ) = $queryResult->fetchrow_array ) {
        push( @tabResult, $userLogin );
    }

    # Recuperation de la liste des utilisateurs du groupe id : $groupId.
    $query = "SELECT groupgroup_child_id FROM GroupGroup WHERE groupgroup_parent_id=".$groupId;

    # On execute la requete
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        write_log( "Probleme lors de l'execution de la requete : ".$queryResult->err, "W" );
        return undef;
    }

    # On traite les resultats
    while( my( $groupGroupId ) = $queryResult->fetchrow_array ) {
        my $userGroupTmp = getGroupUsers( $groupGroupId, $dbHandler, $sqlRequest );

        for( my $i=0; $i<=$#$userGroupTmp; $i++ ) {
            my $j =0;
            while( ($j<=$#tabResult) && ($$userGroupTmp[$i] ne $tabResult[$j]) ) {
                $j++;
            }

            if( $j>$#tabResult ) {
                push( @tabResult, $$userGroupTmp[$i] );
            }
        }
    }
    
    return \@tabResult;
}
