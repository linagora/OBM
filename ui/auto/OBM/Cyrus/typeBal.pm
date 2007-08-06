package OBM::Cyrus::typeBal;

require Exporter;

use URI::Escape;
use Cyrus::SIEVE::managesieve;
use OBM::Parameters::common;
use OBM::Parameters::cyrusConf;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
use strict;


sub getDbValues {
    my( $dbHandler, $domain, $obmSrvId, $obmUserLogin ) = @_;
    my $domainId = $domain->{"domain_id"};
    my $balPrefix = $OBM::Parameters::cyrusConf::boxTypeDef->{"BAL"}->{"prefix"};
    my $balSeparator = $OBM::Parameters::cyrusConf::boxTypeDef->{"BAL"}->{"separator"};

    # La requete a executer - obtention des informations sur les utilisateurs
    # mails de l'organisation.
    my $query = "SELECT i.userobm_id, i.userobm_login, i.userobm_mail_quota, j.mailserver_host_id, i.userobm_vacation_enable, i.userobm_vacation_message, i.userobm_email, i.userobm_nomade_perms, i.userobm_nomade_enable, i.userobm_nomade_local_copy, i.userobm_email_nomade FROM P_UserObm i, P_MailServer j WHERE i.userobm_mail_perms=1 AND i.userobm_domain_id=".$domainId." AND i.userobm_mail_server_id=j.mailserver_id";

    if( defined($obmSrvId) && ( $obmSrvId =~ /^\d+$/ ) ) {
        $query .= " AND j.mailserver_host_id=".$obmSrvId;
    }

    if( defined($obmUserLogin) && ( $obmUserLogin =~ /$regexp_login/ ) ) {
        $query .= " AND i.userobm_login='".$obmUserLogin."'";
    }


    # On execute la requete
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
        return undef;
    }

    # On tri les resultats dans le tableau
    my $users = &OBM::utils::cloneStruct(OBM::Parameters::cyrusConf::listImapBox);
    while( my( $userId, $userLogin, $userQuota, $userSrvId, $userVenable, $userVmessage, $userEmail, $userNomadePerms, $userNomadeEnable, $userNomadeLocalCopy, $userNomadeDst ) = $queryResult->fetchrow_array ) {
        my $userDesc = &OBM::utils::cloneStruct(OBM::Parameters::cyrusConf::imapBox);

        $userDesc->{"box_login"} = lc($userLogin);
        if( !$singleSpaceName ) {
            $userDesc->{"box_login"} .= "@".lc($domain->{"domain_name"});
        }
        $userDesc->{"box_name"} = $balPrefix.$balSeparator.$userDesc->{"box_login"};
        $userDesc->{"box_srv_id"} = $userSrvId;

        if( defined($userQuota) && ($userQuota ne "") ) {
            $userDesc->{"box_quota"} = $userQuota*1000;
        }

        $userDesc->{"box_vacation_enable"} = $userVenable;
        $userDesc->{"box_vacation_message"} = uri_unescape( $userVmessage );

        # Si le vacation est actif, on récupère toutes les adresses mails de
        # l'utilisateur
        if( $userVenable ) {
            $userDesc->{"box_email"} = &OBM::toolBox::makeEntityMailAddress( $userEmail, $domain );
        }

        $userDesc->{"box_nomade_perms"} = $userNomadePerms;
        $userDesc->{"box_nomade_enable"} = $userNomadeEnable;
        $userDesc->{"box_nomade_local_copy"} = $userNomadeLocalCopy;
        $userDesc->{"box_nomade_dst"} = $userNomadeDst;

        # On recupere la definition des ACL
        $userDesc->{"box_acl"} = &OBM::toolBox::getEntityRight( $dbHandler, $domain, initRight( $userId ), $userId );

        if( !exists($users->{$userDesc->{"box_login"}}) ) {
            $users->{$userDesc->{"box_login"}} = $userDesc;
        }
    }

    return $users;
}


sub initRight {
    my( $userId ) = @_;
    my $entityType = "MailBox";
    my %rightDef;

    $rightDef{"read"}->{"compute"} = 1;
    $rightDef{"read"}->{"sqlQuery"} = "SELECT i.userobm_id, i.userobm_login FROM P_UserObm i, P_EntityRight j WHERE i.userobm_id=j.entityright_consumer_id AND j.entityright_write=0 AND j.entityright_read=1 AND j.entityright_entity_id=".$userId." AND j.entityright_entity='".$entityType."'";
        
    $rightDef{"writeonly"}->{"compute"} = 1;
    $rightDef{"writeonly"}->{"sqlQuery"} = "SELECT i.userobm_id, i.userobm_login FROM P_UserObm i, P_EntityRight j WHERE i.userobm_id=j.entityright_consumer_id AND j.entityright_write=1 AND j.entityright_read=0 AND j.entityright_entity_id=".$userId." AND j.entityright_entity='".$entityType."'";

    $rightDef{"write"}->{"compute"} = 1;
    $rightDef{"write"}->{"sqlQuery"} = "SELECT userobm_id, userobm_login FROM P_UserObm LEFT JOIN P_EntityRight ON entityright_write=1 AND entityright_read=1 AND entityright_consumer_id=userobm_id AND entityright_entity='".$entityType."' WHERE entityright_entity_id=".$userId." OR userobm_id=".$userId;

    $rightDef{"public"}->{"compute"} = 0;
    $rightDef{"public"}->{"sqlQuery"} = "SELECT entityright_read, entityright_write FROM P_EntityRight WHERE entityright_entity_id=".$userId." AND entityright_entity='".$entityType."' AND entityright_consumer_id=0";

    return \%rightDef;
}


sub updateSieve {
    my( $srvDesc, $imapBox ) = @_;
    my @newSieveScript;
    my $sieveErrorCode;

    if( !defined($imapBox->{"box_vacation_enable"}) ) {
        $imapBox->{"box_vacation_enable"} = 0;
    }

    if( !defined($imapBox->{"box_login"}) ) {
        return 1;
    }
    my $boxLogin = $imapBox->{"box_login"};


    if( connectSrvSieve( $srvDesc, $boxLogin ) ) {
        return 1;
    }

    my $sieveScriptName = $boxLogin.".sieve";
    $sieveScriptName =~ s/@/-/g;
    my $localSieveScriptName = $tmpOBM.$sieveScriptName;

    &OBM::toolBox::write_log( "Mise a jour du script Sieve pour l'utilisateur : '".$boxLogin."'", "W" );
    my $currentScriptString = "";
    sieve_get( $srvDesc->{"imap_sieve_server_conn"}, $sieveScriptName, $currentScriptString );
    my @oldSieveScript;
    if( defined($currentScriptString) ) {
        @oldSieveScript = split( /\n/, $currentScriptString );
    }


    # On desactive l'ancien script
    $sieveErrorCode = sieve_activate( $srvDesc->{"imap_sieve_server_conn"}, "" );
    # On supprime l'ancien script
    $sieveErrorCode = sieve_delete( $srvDesc->{"imap_sieve_server_conn"}, $sieveScriptName );

    # On met les règles pour le vacation dans le script Sieve
    updateSieveScript( $imapBox, \@oldSieveScript, \@newSieveScript );

    if( $#newSieveScript >= 0 ) {
        # On cree le script Sieve en local
        open( FIC, ">".$localSieveScriptName ) or return 1;
        print FIC @newSieveScript;
        close( FIC );

        # On installe le nouveau script
        if( sieve_put_file_withdest( $srvDesc->{"imap_sieve_server_conn"}, $localSieveScriptName, $sieveScriptName ) ) {
            my $errstr = sieve_get_error( $srvDesc->{"imap_sieve_server_conn"} );
            $errstr = "Echec : Sieve - erreur inconnue." if(!defined($errstr));

            &OBM::toolBox::write_log( "Echec: lors du telechargement du script Sieve : ".$errstr , "W" );
            disconnectSrvSieve( $srvDesc );
            return 1;
        }

        &OBM::toolBox::write_log( "Activation du script Sieve pour l'utilisateur : ".$boxLogin, "W" );

        # On active le nouveau script
        if( sieve_activate( $srvDesc->{"imap_sieve_server_conn"}, $sieveScriptName ) ) {
            my $errstr = sieve_get_error( $srvDesc->{"imap_sieve_server_conn"} );
            $errstr = "Echec : Sieve - erreur inconnue." if(!defined($errstr));

            &OBM::toolBox::write_log( "Probleme lors de l'activation du script Sieve : ".$errstr, "W" );
            disconnectSrvSieve( $srvDesc );
            return 1;
        }

        # On supprime le script local
        &OBM::utils::execCmd( "/bin/rm -f ".$localSieveScriptName );
    }else {
        &OBM::toolBox::write_log( "Suppression du script Sieve pour l'utilisateur : ".$boxLogin, "W" );
    }

    disconnectSrvSieve( $srvDesc );

    return 0;
}


sub updateSieveScript {
    my( $imapBox, $oldSieveScript, $newSieveScript ) = @_;
    require OBM::Cyrus::utils;

    # Recuperation des en-tetes 'require' de l'ancien script
    my @headers;
    &OBM::Cyrus::utils::sieveGetHeaders( $oldSieveScript, \@headers );

    my @vacation;
    updateSieveVacation( $imapBox, \@headers, $oldSieveScript, \@vacation );

    my @nomade;
    updateSieveNomade( $imapBox, \@headers, $oldSieveScript, \@nomade );

    splice( @{$newSieveScript}, 0 );

    if( ( $#vacation < 0 ) && ( $#nomade < 0 ) && ($#{$oldSieveScript} < 0) ) {
        return 0;
    }

    push( @{$newSieveScript}, @headers );
    push( @{$newSieveScript}, @vacation );
    push( @{$newSieveScript}, @nomade );

    for( my $i=0; $i<=$#{$oldSieveScript}; $i++ ) {
        $oldSieveScript->[$i] .="\n";
    }
    push( @{$newSieveScript}, @{$oldSieveScript} );

    return 0;
}


sub updateSieveVacation {
    my( $imapBox, $headers, $oldSieveScript, $newSieveScript ) = @_;
    my $vacationMark = "# OBM2 - Vacation";


    if( exists($imapBox->{"box_vacation_enable"}) && $imapBox->{"box_vacation_enable"} && defined($imapBox->{"box_login"}) && defined($imapBox->{"box_vacation_message"}) && defined($imapBox->{"box_email"}) && ( $#{$imapBox->{"box_email"}}>=0 ) ) {
        # On verifie que l'en-tête necessaire soit bien placé
        my $i=0;
        while( ( $i<=$#{$headers} ) && ( $headers->[$i] !~ /[^#]*require \"vacation\";/) ) {
            $i++;
        }

        if( $i > $#{$headers} ) {
            unshift( @{$headers}, "require \"vacation\";\n" );
        }

        my $boxLogin = $imapBox->{"box_login"};
        &OBM::toolBox::write_log( "Creation du message d'absence de la boite '".$boxLogin."'", "W" );

        my $boxVacationMessage = $imapBox->{"box_vacation_message"};
        my $boxEmails = $imapBox->{"box_email"};

        push( @{$newSieveScript}, $vacationMark."\n" );
    
        push( @{$newSieveScript}, "vacation :addresses [ " );
        for( my $i=0; $i<=$#{$boxEmails}; $i++ ) {
            if( $i != 0 ) {
                $newSieveScript->[$#{$newSieveScript}] .= ", ";
            }
            $newSieveScript->[$#{$newSieveScript}] .= "\"".$boxEmails->[$i]."\"";
        }
        $newSieveScript->[$#{$newSieveScript}] .= " ] \"".to_utf8( { -string => $boxVacationMessage, -charset => $defaultCharSet } )."\";\n";
        push( @{$newSieveScript}, $vacationMark."\n" );
    }

    # On supprime le vacation de l'ancien script
    &OBM::Cyrus::utils::sieveDeleteMark( $oldSieveScript, $vacationMark );

    return 0;
}


sub updateSieveNomade {
    my( $imapBox, $headers, $oldSieveScript, $newSieveScript ) = @_;
    my $nomadeMark = "# OBM2 - Nomade";

    if( defined($imapBox->{"box_nomade_perms"}) && $imapBox->{"box_nomade_perms"} && defined($imapBox->{"box_nomade_enable"}) && $imapBox->{"box_nomade_enable"} && defined($imapBox->{"box_nomade_dst"}) ) {
        push( @{$newSieveScript}, $nomadeMark."\n" );
        push( @{$newSieveScript}, "redirect \"".$imapBox->{"box_nomade_dst"}."\";\n" );

        if( defined($imapBox->{"box_nomade_local_copy"}) && !$imapBox->{"box_nomade_local_copy"} ) {
            push( @{$newSieveScript}, "discard;\n" );
            push( @{$newSieveScript}, "stop;\n" );
        }else {
            push( @{$newSieveScript}, "keep;\n" );
        }

        push( @{$newSieveScript}, $nomadeMark."\n" );
    }

    # On supprime le nomade de l'ancien script
    &OBM::Cyrus::utils::sieveDeleteMark( $oldSieveScript, $nomadeMark );

    return 0;
}


sub connectSrvSieve {
    my( $srvDesc, $boxLogin ) = @_;

    if( !defined($srvDesc->{"imap_server_ip"}) ) {
        return 1;
    }
    my $imapServerIp = $srvDesc->{"imap_server_ip"};

    if( !defined($srvDesc->{"imap_server_login"}) ) {
        return 1;
    }
    my $imapServerLogin = $srvDesc->{"imap_server_login"};

    if( !defined($srvDesc->{"imap_server_passwd"}) ) {
        return 1;
    }
    my $imapServerPasswd = $srvDesc->{"imap_server_passwd"};


    &OBM::toolBox::write_log( "Connexion au serveur SIEVE '".$srvDesc->{"imap_server_name"}."' en tant que '".$srvDesc->{"imap_server_login"}."'", "W" );
    $srvDesc->{"imap_sieve_server_conn"} = sieve_get_handle( $imapServerIp, sub{return $boxLogin}, sub{return $imapServerLogin}, sub{return $imapServerPasswd}, sub{return undef} );

    if( !defined($srvDesc->{"imap_sieve_server_conn"}) ) {
        &OBM::toolBox::write_log( "Probleme lors de la connexion au serveur SIEVE", "W" );
        return 1;
    }else {
        &OBM::toolBox::write_log( "Connexion au serveur SIEVE etablie", "W" );
    }

    return 0;
}


sub disconnectSrvSieve {
    my( $srvDesc ) = @_;

    if( !defined($srvDesc->{"imap_sieve_server_conn"}) ) {
        return 1;
    }
    my $imapSieveServerConn = $srvDesc->{"imap_sieve_server_conn"};

    &OBM::toolBox::write_log( "Deconnexion du serveur SIEVE.", "W" );
    sieve_logout( $imapSieveServerConn );

    return 0;
}
