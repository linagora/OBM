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
        &OBM::toolBox::write_log( "[Update::utils]: connection à la base de donnée incorrecte !", "W" );
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
    my $queryDomain = "SELECT   domain_id,
                                domain_label,
                                domain_description,
                                domain_name,
                                domain_alias,
                                name.samba_value as samba_domain_name,
                                sid.samba_value as samba_domain_sid,
                                pdc.samba_value as samba_domain_pdc,
                                profile.samba_value as samba_user_profile
                        FROM Domain
                        LEFT JOIN Samba as name ON name.samba_name=\"samba_domain\" AND name.samba_domain_id=domain_id
                        LEFT JOIN Samba as sid ON sid.samba_name=\"samba_sid\" AND sid.samba_domain_id=domain_id
                        LEFT JOIN Samba as pdc ON pdc.samba_name=\"samba_pdc\" AND pdc.samba_domain_id=domain_id
                        LEFT JOIN Samba as profile ON profile.samba_name=\"samba_profile\" AND profile.samba_domain_id=domain_id";

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

    while( my( $domainId, $domainLabel, $domainDesc, $domainName, $domainAlias, $domainSambaName, $domainSambaSid, $domainSambaPdc, $domainSambaUserProfile ) = $queryDomainResult->fetchrow_array ) {
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

        $currentDomain->{"domain_samba_name"} = $domainSambaName;
        $currentDomain->{"domain_samba_sid"} = $domainSambaSid;
        $currentDomain->{"domain_samba_pdc"} = $domainSambaPdc;
        $currentDomain->{"domain_samba_user_profile"} = $domainSambaUserProfile;

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
        &OBM::toolBox::write_log( "[Update::utils]: recuperation du serveur LDAP pour le domaine '".$domainList->[$i]->{"domain_name"}."'", "W" );

        my $queryLdapAdmin = "SELECT usersystem_password FROM UserSystem WHERE usersystem_login='".$ldapAdminLogin."'";

        # On execute la requete concernant l'administrateur LDAP associé
        my $queryLdapAdminResult;
        if( !&OBM::toolBox::execQuery( $queryLdapAdmin, $dbHandler, \$queryLdapAdminResult ) ) {
            &OBM::toolBox::write_log( "[Update::utils]: probleme lors de l'execution de la requete.", "W" );
            if( defined($queryLdapAdminResult) ) {
                &OBM::toolBox::write_log( "[Update::utils]: ".$queryLdapAdminResult->err, "W" );
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

        &OBM::toolBox::write_log( "[Update::utils]: recuperation des serveurs de courrier IMAP pour le domaine '".$domainList->[$i]->{"domain_name"}."'", "W" );
        my $srvQuery = "SELECT i.host_id, i.host_name, i.host_ip FROM Host i, DomainMailServer j, MailServer k WHERE j.domainmailserver_domain_id=".$domainList->[$i]->{"domain_id"}." AND j.domainmailserver_role='imap' AND j.domainmailserver_mailserver_id=k.mailserver_id AND k.mailserver_host_id=i.host_id";

        # On execute la requete
        my $queryResult;
        if( !&OBM::dbUtils::execQuery( $srvQuery, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "[Update::utils]: probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
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

        &OBM::toolBox::write_log( "[Update::utils]: recuperation des serveurs de courrier SMTP-in pour le domaine '".$domainList->[$i]->{"domain_name"}."'", "W" );
        my $srvQuery = "SELECT i.host_id, i.host_name, i.host_ip FROM Host i, DomainMailServer j, MailServer k WHERE j.domainmailserver_domain_id=".$domainList->[$i]->{"domain_id"}." AND j.domainmailserver_role='smtp_in' AND j.domainmailserver_mailserver_id=k.mailserver_id AND k.mailserver_host_id=i.host_id";

        # On execute la requete
        my $queryResult;
        if( !&OBM::dbUtils::execQuery( $srvQuery, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "[Update::utils]: probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
            next;
        }

        my @srvList = ();
        while( my( $hostId, $hostName, $hostIp) = $queryResult->fetchrow_array ) {
            my $srv;
            $srv->{"smptin_server_id"} = $hostId;
            $srv->{"smtpin_server_name"} = $hostName;
            $srv->{"smtpin_server_ip"} = $hostIp;

            push( @{$domainList->[$i]->{"smtpin_servers"}}, $srv );
        }
    }

    return 0;
}


sub getSmtpOutServers {
    my( $dbHandler, $domainList ) = @_;

    for( my $i=0; $i<=$#$domainList; $i++ ) {
        if( $domainList->[$i]->{"meta_domain"} ) {
            next;
        }

        &OBM::toolBox::write_log( "[Update::utils]: recuperation des serveurs de courrier SMTP-out pour le domaine '".$domainList->[$i]->{"domain_name"}."'", "W" );
        my $srvQuery = "SELECT i.host_id, i.host_name, i.host_ip FROM Host i, DomainMailServer j, MailServer k WHERE j.domainmailserver_domain_id=".$domainList->[$i]->{"domain_id"}." AND j.domainmailserver_role='smtp_out' AND j.domainmailserver_mailserver_id=k.mailserver_id AND k.mailserver_host_id=i.host_id";

        # On exécute la requête
        my $queryResult;
        if( !&OBM::dbUtils::execQuery( $srvQuery, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "[Update::utils]: probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
            next;
        }

        my @srvList = ();
        while( my( $hostId, $hostName, $hostIp) = $queryResult->fetchrow_array ) {
            my $srv;
            $srv->{"smptout_server_id"} = $hostId;
            $srv->{"smtpout_server_name"} = $hostName;
            $srv->{"smtpout_server_ip"} = $hostIp;

            push( @{$domainList->[$i]->{"smtpout_servers"}}, $srv );
        }
    }

    return 0;
}


sub findDomainbyId {
    my( $domainList, $domainId ) = @_;
    my $domainDesc = undef;

    if( !defined($domainId) || ($domainId !~ /^\d+$/) ) {
        return undef;
    }

    for( my $i=0; $i<=$#{$domainList}; $i++ ) {
        if( $domainList->[$i]->{"domain_id"} == $domainId ) {
            $domainDesc = $domainList->[$i];
            last;
        }
    }

    return $domainDesc;
}


sub getUserIdFromUserLoginDomain {
    my( $dbHandler, $userLogin, $domainId ) = @_;

    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( "[Update::utils]: connection à la base de donnees incorrecte !", "W" );
        return undef;
    }

    if( $userLogin !~ /$regexp_login/ ) {
        &OBM::toolBox::write_log( "[Update::utils] nom d'utilisateur non specifie ou incorrect", "W" );
        return undef;
    }

    if( $domainId !~ /^[0-9]+$/ ) {
        &OBM::toolBox::write_log( "[Update::utils] Id BD de domaine non specifie ou incorrect", "W" );
        return undef;
    }

    my $query = "SELECT userobm_id FROM UserObm WHERE userobm_login=".$dbHandler->quote($userLogin)." AND userobm_domain_id=".$domainId;
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::utils]: probleme lors de l'execution de la requete.", "W" );
        if( defined($queryResult) ) {
            &OBM::toolBox::write_log( "[Update::utils]: ".$queryResult->err, "W" );
        }

        return undef;
    }

    my( $userId ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    return $userId;
}


sub getMailshareIdFromMailshareNameDomain {
    my( $dbHandler, $mailshareName, $domainId ) = @_;

    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( "[Update::utils] connection à la base de donnees incorrecte :", "W" );
        return undef;
    }

    if( $mailshareName !~ /$regexp_login/ ) {
        &OBM::toolBox::write_log( "[Update::utils] nom de repertoire partage non specifie", "W" );
        return undef;
    }

    if( $domainId !~ /^[0-9]+$/ ) {
        &OBM::toolBox::write_log( "[Update::utils] Id BD de domaine non specifie ou incorrect", "W" );
        return undef;
    }

    my $query = "SELECT mailshare_id FROM MailShare WHERE mailshare_name=".$dbHandler->quote($mailshareName)." AND mailshare_domain_id=".$domainId;
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::utils]: probleme lors de l'execution de la requete.", "W" );
        if( defined($queryResult) ) {
            &OBM::toolBox::write_log( "[Update::utils]: ".$queryResult->err, "W" );
        }

        return undef;
    }

    my( $mailshareId ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    return $mailshareId;
}
