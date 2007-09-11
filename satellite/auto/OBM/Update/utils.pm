package OBM::Update::utils;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
require OBM::toolBox;
require OBM::dbUtils;


sub getDomains {
    my( $dbHandler, $obmDomainId ) = @_;
    my @domainList;

    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( "[Update::utils]: connection à la base de donnée incorrect !", "W" );
        return undef;
    }


    # Création du meta-domaine
    $domainList[0]->{"meta_domain"} = 1;
    $domainList[0]->{"domain_id"} = 0;
    $domainList[0]->{"domain_label"} = "metadomain";
    $domainList[0]->{"domain_name"} = "metadomain";
    $domainList[0]->{"domain_dn"} = "metadomain";
    $domainList[0]->{"domain_desc"} = "Informations de l'annuaire ne faisant partie d'aucun domaine";


    # Requete de recuperation des informations des domaines
    my $queryDomain = "SELECT domain_id, domain_label, domain_description, domain_name, domain_alias, samba_value FROM Domain LEFT JOIN Samba ON samba_name=\"samba_sid\" AND samba_domain_id=domain_id";
    if( defined($obmDomainId) && $obmDomainId =~ /^\d+$/ ) {
        $queryDomain .= " WHERE domain_id=".$obmDomainId;
    }

    # On execute la requete concernant les domaines
    my $queryDomainResult;
    if( !&OBM::dbUtils::execQuery( $queryDomain, $dbHandler, \$queryDomainResult ) ) {
        &OBM::toolBox::write_log( "[Update::utils]: probleme lors de l'execution de la requete.", "W" );
        if( defined($queryDomainResult) ) {
            &OBM::toolBox::write_log( "[Update::utils]: ".$queryDomainResult->err, "W" );
        }

        return undef;
    }

    while( my( $domainId, $domainLabel, $domainDesc, $domainName, $domainAlias, $domainSambaSid ) = $queryDomainResult->fetchrow_array ) {
        my $currentDomain;
        $currentDomain->{"meta_domain"} = 0;
        $currentDomain->{"domain_id"} = $domainId;
        $currentDomain->{"domain_label"} = $domainLabel;
        $currentDomain->{"domain_desc"} = $domainDesc;
        $currentDomain->{"domain_name"} = $domainName;
        $currentDomain->{"domain_dn"} = $domainName;

        $currentDomain->{"domain_alias"} = [];
        if( defined($domainAlias) ) {
            push( @{$currentDomain->{"domain_alias"}}, split( /\r\n/, $domainAlias ) );
        }

        $currentDomain->{"domain_samba_sid"} = $domainSambaSid;

        push( @domainList, $currentDomain );
    }

    return \@domainList;
}


sub getLdapServer {
    my( $dbHandler, $domainList ) = @_;

    if( !defined($ldapAdminLogin) ) {
        return 0;
    }

    for( my $i=0; $i<=$#$domainList; $i++ ) {
        &OBM::toolBox::write_log( "[Update::update]: recuperation du serveur LDAP pour le domaine '".$domainList->[$i]->{"domain_name"}."'", "W" );

        my $queryLdapAdmin = "SELECT usersystem_password FROM UserSystem WHERE usersystem_login='".$ldapAdminLogin."'";

        # On execute la requete concernant l'administrateur LDAP associé
        my $queryLdapAdminResult;
        if( !&OBM::toolBox::execQuery( $queryLdapAdmin, $dbHandler, \$queryLdapAdminResult ) ) {
            &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete.", "W" );
            if( defined($queryLdapAdminResult) ) {
                &OBM::toolBox::write_log( "[Update::update]: ".$queryLdapAdminResult->err, "W" );
            }
        }elsif( my( $ldapAdminPasswd ) = $queryLdapAdminResult->fetchrow_array ) {
            $domainList->[$i]->{"ldap_admin_server"} = $ldapServer;
            $domainList->[$i]->{"ldap_admin_login"} = $ldapAdminLogin;
            $domainList->[$i]->{"ldap_admin_passwd"} = $ldapAdminPasswd;

            $queryLdapAdminResult->finish;
        }
    }

    return 1;
}


sub getCyrusServers {
    my( $dbHandler, $domainList ) = @_;

    for( my $i=0; $i<=$#$domainList; $i++ ) {
        if( $domainList->[$i]->{"meta_domain"} ) {
            next;
        }

        &OBM::toolBox::write_log( "[Update::update]: recuperation des serveurs de courrier pour le domaine '".$domainList->[$i]->{"domain_name"}."'", "W" );
        my $srvQuery = "SELECT i.host_id, i.host_name, i.host_ip FROM Host i, DomainMailServer j, MailServer k WHERE j.domainmailserver_domain_id=1 AND domainmailserver_role='imap' AND j.domainmailserver_mailserver_id=k.mailserver_id AND k.mailserver_host_id=i.host_id";

        # On execute la requete
        my $queryResult;
        if( !&OBM::dbUtils::execQuery( $srvQuery, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
            next;
        }

        my @srvList = ();
        while( my( $hostId, $hostName, $hostIp) = $queryResult->fetchrow_array ) {
            my $srv;
            $srv->{"imap_server_id"} = $hostId;
            $srv->{"imap_server_name"} = $hostName;
            $srv->{"imap_server_ip"} = $hostIp;

            push( @{$domainList->[$i]->{"imap_servers"}}, $srv );
        }
    }

    return 0;
}


sub getSmtpInServers {
    my( $dbHandler, $domainList ) = @_;

    for( my $i=0; $i<=$#$domainList; $i++ ) {
        if( $domainList->[$i]->{"meta_domain"} ) {
            next;
        }

        &OBM::toolBox::write_log( "[Update::update]: recuperation des serveurs de courrier pour le domaine '".$domainList->[$i]->{"domain_name"}."'", "W" );
        my $srvQuery = "SELECT i.host_id, i.host_name, i.host_ip FROM Host i, DomainMailServer j, MailServer k WHERE j.domainmailserver_domain_id=1 AND domainmailserver_role='smtp-in' AND j.domainmailserver_mailserver_id=k.mailserver_id AND k.mailserver_host_id=i.host_id";

        # On execute la requete
        my $queryResult;
        if( !&OBM::dbUtils::execQuery( $srvQuery, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
            next;
        }

        my @srvList = ();
        while( my( $hostId, $hostName, $hostIp) = $queryResult->fetchrow_array ) {
            my $srv;
            $srv->{"smpt-in_server_id"} = $hostId;
            $srv->{"smtp-in_server_name"} = $hostName;
            $srv->{"smtp-in_server_ip"} = $hostIp;

            push( @{$domainList->[$i]->{"smtp-in_servers"}}, $srv );
        }
    }

    return 0;
}
