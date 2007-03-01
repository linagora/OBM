package OBM::Ldap::typeMailShareDir;

require Exporter;

use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
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

    if( defined($main::domainList->[$domainId]->{"domain_id"}) ) {
        &OBM::toolBox::write_log( "Identifiant de domaine non définie", "WC" );
        return undef;
    }

    # La requete a executer - obtention des informations sur les repertoires
    # partages de la messagerie
    my $query = "SELECT mailsharedir_name, mailsharedir_description, mailsharedir_email FROM P_MailShareDir WHERE mailsharedir_domain_id=".$main::domainList->[$domainId]->{"domain_id"};

    # On execute la requête
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
        return undef;
    }

    # On range les resultats dans la structure de données
    my $i = 0;
    my @mailShare = ();
    while( my( $mailsharedir_name, $mailsharedir_description, $mailsharedir_email ) = $queryResult->fetchrow_array ) {
        $mailShare[$i]->{"mailsharedir_name"} = $mailsharedir_name;
        $mailShare[$i]->{"mailsharedir_mailbox"} = "+".$mailsharedir_name."@".$main::domainList->[$domainId]->{"domain_name"};
        $mailShare[$i]->{"mailsharedir_description"} = $mailsharedir_description;

        if( $mailsharedir_email ) {
            $mailShare[$i]->{"mailsharedir_mailperms"} = 1;
            push( @{$mailShare[$i]->{"mailsharedir_mail"}}, $mailsharedir_email."@".$main::domainList->[$domainId]->{"domain_name"} );

            for( my $j=0; $j<=$#{$main::domainList->[$domainId]->{"domain_alias"}}; $j++ ) {
                push( @{$mailShare[$i]->{"mailsharedir_mail"}}, $mailsharedir_email."@".$main::domainList->[$domainId]->{"domain_alias"}->[$j] );
            }

        }else {
            $mailShare[$i]->{"mailsharedir_mailperms"} = 0;

        }

        # On ajoute les informations de la structure
        $mailShare[$i]->{"node_type"} = $MAILSHAREDIR;
        $mailShare[$i]->{"name"} = $mailShare[$i]->{$attributeDef->{$mailShare[$i]->{"node_type"}}->{"dn_value"}};
        $mailShare[$i]->{"domain_id"} = $domainId;
        $mailShare[$i]->{"dn"} = &OBM::ldap::makeDn( $mailShare[$i], $parentDn );

        $i++;
    }


    # On referme la connexion a la base
    if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
        &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "W" );
    }

    return \@mailShare;
}


sub createLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};

    # Les parametres necessaires
    if( $entry->{"mailsharedir_name"} ) {
        $ldapEntry->add(
            objectClass => $attributeDef->{$type}->{"objectclass"},
            cn => $entry->{"mailsharedir_name"},
            mailBox => $entry->{"mailsharedir_mailbox"}
        );

    }else {
        return 0;
    }

    if( $entry->{"mailsharedir_description"} ) {
        $ldapEntry->add( description => to_utf8({ -string => $entry->{"mailsharedir_description"}, -charset => $defaultCharSet }) );
    }

    # Les adresses mails
    if( $entry->{"mailsharedir_mail"} ) {
        $ldapEntry->add( mail => $entry->{"mailsharedir_mail"} );
    }

    # L'acces mail
    if( $entry->{"mailsharedir_mailperms"} ) {
        $ldapEntry->add( mailAccess => "PERMIT" );
    }else {
        $ldapEntry->add( mailAccess => "REJECT" );
    }

    return 1;
}


sub updateLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};
    my $update = 0;

    # Le nom de la BAL
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"mailsharedir_mailbox"}, $ldapEntry, "mailbox" ) ) {
        $update = 1;
    }

    # La description
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"mailsharedir_description"}, $ldapEntry, "description" ) ) {
        $update = 1;
    }

    # Le cas des alias mails
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"mailsharedir_mail"}, $ldapEntry, "mail" ) ) {
        $update = 1;
    }

    # L'acces au mail
    if( $entry->{"mailsharedir_mailperms"} && (&OBM::Ldap::utils::modifyAttr( "PERMIT", $ldapEntry, "mailAccess" )) ) {
        $update = 1;

    }elsif( !$entry->{"mailsharedir_mailperms"} && (&OBM::Ldap::utils::modifyAttr( "PERMIT", $ldapEntry, "mailAccess" )) ) {
        $update = 1;

    }

    return $update;
}
