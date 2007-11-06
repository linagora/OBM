package OBM::Ldap::typeMailShare;

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

    # La requete a executer - obtention des informations sur les repertoires
    # partages de la messagerie
    my $query = "SELECT mailshare_name, mailshare_description, mailshare_email, mailshare_mail_server_id FROM P_MailShare WHERE mailshare_domain_id=".$main::domainList->[$domainId]->{"domain_id"};

    # On execute la requête
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
        return undef;
    }

    # On range les resultats dans la structure de données
    my $i = 0;
    my @mailShare = ();
    while( my( $mailshare_name, $mailshare_description, $mailshare_email, $mailshare_mail_server_id ) = $queryResult->fetchrow_array ) {

        &OBM::toolBox::write_log( "Gestion du repertoire partage : '".$mailshare_name."'", "W" );

        # On range les resultats dans la structure de donnees des resultats
        $mailShare[$i]->{"mailshare_name"} = $mailshare_name;
        $mailShare[$i]->{"mailshare_mailbox"} = "+".$mailshare_name."@".$main::domainList->[$domainId]->{"domain_name"};
        $mailShare[$i]->{"mailshare_description"} = $mailshare_description;
        $mailShare[$i]->{"mailshare_domain"} = $main::domainList->[$domainId]->{"domain_label"};

        if( $mailshare_email ) {
            my $localServerIp = &OBM::toolBox::getHostIpById( $dbHandler, $mailshare_mail_server_id );

            if( !defined($localServerIp) ) {
                &OBM::toolBox::write_log( "Droit mail du repertoire partage : '".$mailshare_name."' annule - Serveur inconnu !", "W" );
                $mailShare[$i]->{"mailshare_mailperms"} = 0;

            }else {
                $mailShare[$i]->{"mailshare_mailperms"} = 1;
                push( @{$mailShare[$i]->{"mailshare_mail"}}, $mailshare_email."@".$main::domainList->[$domainId]->{"domain_name"} );

                for( my $j=0; $j<=$#{$main::domainList->[$domainId]->{"domain_alias"}}; $j++ ) {
                    push( @{$mailShare[$i]->{"mailshare_mail_alias"}}, $mailshare_email."@".$main::domainList->[$domainId]->{"domain_alias"}->[$j] );
                }

                # On ajoute le serveur de mail associé
                $mailShare[$i]->{"mailShare_mailLocalServer"} = "lmtp:".$localServerIp.":24";
            }

        }else {
            $mailShare[$i]->{"mailshare_mailperms"} = 0;

        }

        # On ajoute les informations de la structure
        $mailShare[$i]->{"node_type"} = $MAILSHARE;
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
    if( $entry->{"mailshare_name"} ) {
        $ldapEntry->add(
            objectClass => $attributeDef->{$type}->{"objectclass"},
            cn => $entry->{"mailshare_name"},
            mailBox => $entry->{"mailshare_mailbox"}
        );

    }else {
        return 0;
    }

    if( $entry->{"mailshare_description"} ) {
        $ldapEntry->add( description => to_utf8({ -string => $entry->{"mailshare_description"}, -charset => $defaultCharSet }) );
    }

    # Le serveur de BAL local
    if( $entry->{"mailShare_mailLocalServer"} ) {
        $ldapEntry->add( mailBoxServer => $entry->{"mailShare_mailLocalServer"} );
    }

    # Les adresses mails
    if( $entry->{"mailshare_mail"} ) {
        $ldapEntry->add( mail => $entry->{"mailshare_mail"} );
    }

    # Les adresses mails secondaires
    if( $entry->{"mailshare_mail_alias"} ) {
        $ldapEntry->add( mailAlias => $entry->{"mailshare_mail_alias"} );
    }

    # L'acces mail
    if( $entry->{"mailshare_mailperms"} ) {
        $ldapEntry->add( mailAccess => "PERMIT" );
    }else {
        $ldapEntry->add( mailAccess => "REJECT" );
    }

    # Le domaine
    if( $entry->{"mailshare_domain"} ) {
        $ldapEntry->add( obmDomain => to_utf8({ -string => $entry->{"mailshare_domain"}, -charset => $defaultCharSet }) );
    }

    return 1;
}


sub updateLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};
    my $update = 0;

    # Le nom de la BAL
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"mailshare_mailbox"}, $ldapEntry, "mailbox" ) ) {
        $update = 1;
    }

    # La description
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"mailshare_description"}, $ldapEntry, "description" ) ) {
        $update = 1;
    }

    # Le cas des alias mails
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"mailshare_mail"}, $ldapEntry, "mail" ) ) {
        $update = 1;
    }

    # Le cas des alias mails secondaires
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"mailshare_mail_alias"}, $ldapEntry, "mailAlias" ) ) {
        $update = 1;
    }

    # L'acces au mail
    if( $entry->{"mailshare_mailperms"} && (&OBM::Ldap::utils::modifyAttr( "PERMIT", $ldapEntry, "mailAccess" )) ) {
        $update = 1;

    }elsif( !$entry->{"mailshare_mailperms"} && (&OBM::Ldap::utils::modifyAttr( "PERMIT", $ldapEntry, "mailAccess" )) ) {
        $update = 1;

    }

    # Le serveur de BAL local
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"mailShare_mailLocalServer"}, $ldapEntry, "mailBoxServer" ) ) {
        $update = 1;
    }

    # Le domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"mailshare_domain"}, $ldapEntry, "obmDomain" ) ) {
        $update = 1;
    }

    return $update;
}
