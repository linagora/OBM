package OBM::Cyrus::typeBal;

require Exporter;

use URI::Escape;
use Cyrus::SIEVE::managesieve;
use OBM::Parameters::common;
use OBM::Parameters::cyrusConf;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
use strict;


sub getBdValues {
    my( $dbHandler, $domain, $srvId ) = @_;
    my $domainId = $domain->{"domain_id"};
    my $balPrefix = $OBM::Parameters::cyrusConf::boxTypeDef->{"BAL"}->{"prefix"};
    my $balSeparator = $OBM::Parameters::cyrusConf::boxTypeDef->{"BAL"}->{"separator"};

    # La requete a executer - obtention des informations sur les utilisateurs
    # mails de l'organisation.
    my $query = "SELECT userobm_id, userobm_login, userobm_mail_quota, userobm_vacation_enable, userobm_vacation_message, userobm_email FROM UserObm WHERE userobm_mail_perms=1 AND userobm_domain_id=".$domainId." AND userobm_mail_server_id=".$srvId ;

    # On execute la requete
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
        return undef;
    }

    # On tri les resultats dans le tableau
    my $users = &OBM::utils::cloneStruct(OBM::Parameters::cyrusConf::listImapBox);
    while( my( $userId, $userLogin, $userQuota, $userVenable, $userVmessage, $userEmail ) = $queryResult->fetchrow_array ) {
        my $userDesc = &OBM::utils::cloneStruct(OBM::Parameters::cyrusConf::imapBox);

        $userDesc->{"box_login"} = lc($userLogin)."@".lc($domain->{"domain_name"});
        $userDesc->{"box_name"} = $balPrefix.$balSeparator.$userDesc->{"box_login"};

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
    my $entityType = "mailbox";
    my %rightDef;

    $rightDef{"read"}->{"compute"} = 1;
    $rightDef{"read"}->{"sqlQuery"} = "SELECT i.userobm_login FROM UserObm i, EntityRight j WHERE i.userobm_id=j.entityright_consumer_id AND j.entityright_write=0 AND j.entityright_read=1 AND j.entityright_entity_id=".$userId." AND j.entityright_entity='".$entityType."'";
        
    $rightDef{"writeonly"}->{"compute"} = 1;
    $rightDef{"writeonly"}->{"sqlQuery"} = "SELECT i.userobm_login FROM UserObm i, EntityRight j WHERE i.userobm_id=j.entityright_consumer_id AND j.entityright_write=1 AND j.entityright_read=0 AND j.entityright_entity_id=".$userId." AND j.entityright_entity='".$entityType."'";

    $rightDef{"write"}->{"compute"} = 1;
    $rightDef{"write"}->{"sqlQuery"} = "SELECT userobm_login FROM UserObm LEFT JOIN EntityRight ON entityright_write=1 AND entityright_read=1 AND entityright_consumer_id=userobm_id WHERE (entityright_entity='".$entityType."' AND entityright_entity_id=".$userId.") OR userobm_id=".$userId;

    $rightDef{"public"}->{"compute"} = 0;
    $rightDef{"public"}->{"sqlQuery"} = "SELECT entityright_read, entityright_write FROM EntityRight WHERE entityright_entity_id=".$userId." AND entityright_entity='".$entityType."' AND entityright_consumer_id=0";

    return \%rightDef;
}


sub createBox {
    my( $srvDesc, $imapBox ) = @_;
    my $errors = 0;

    if( defined($imapBox->{"box_vacation_enable"}) && $imapBox->{"box_vacation_enable"} ) {
        if( updateSieve( $srvDesc, $imapBox ) ) {
            $errors++;
        }
    }

    return $errors;
}

sub updateSieve {
    my( $srvDesc, $imapBox ) = @_;
    my @sieveScript;


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

    # On desactive l'ancien script
    sieve_activate( $srvDesc->{"imap_sieve_server_conn"}, "" );
    # On supprime l'ancien script
    sieve_delete( $srvDesc->{"imap_sieve_server_conn"}, $sieveScriptName );

    if( $imapBox->{"box_vacation_enable"} ) {
        # On met les règles pour le vacation dans le script Sieve
        mkSieveVacationScript( $imapBox, \@sieveScript );

        if( $#sieveScript >= 0 ) {
            # On cree le script Sieve en local
            open( FIC, ">".$localSieveScriptName ) or return 1;
            print FIC @sieveScript;
            close( FIC );

            # On installe le nouveau script
            if( sieve_put_file_withdest( $srvDesc->{"imap_sieve_server_conn"}, $localSieveScriptName, $sieveScriptName ) ) {
                my $errstr = sieve_get_error( $srvDesc->{"imap_sieve_server_conn"} );
                $errstr = "Echec : Sieve - erreur inconnue." if(!defined($errstr));

                &OBM::toolBox::write_log( "Probleme lors du telechargement du script Sieve : ".$errstr , "W" );

                return 1;
            }

            &OBM::toolBox::write_log( "Activation du script Sieve pour l'utilisateur : ".$boxLogin, "W" );

            # On active le nouveau script
            if( sieve_activate( $srvDesc->{"imap_sieve_server_conn"}, $sieveScriptName ) ) {
                my $errstr = sieve_get_error( $srvDesc->{"imap_sieve_server_conn"} );
                $errstr = "Echec : Sieve - erreur inconnue." if(!defined($errstr));

                &OBM::toolBox::write_log( "Probleme lors de l'activation du script Sieve : ".$errstr, "W" );

                return 1;
            }

            # On supprime le script local
            &OBM::utils::execCmd( "/bin/rm -f ".$localSieveScriptName );
        }
    }

    disconnectSrvSieve( $srvDesc );

    return 0;
}


sub mkSieveVacationScript {
    my( $imapBox, $sieveScript ) = @_;

    if( !exists($imapBox->{"box_vacation_enable"}) || !$imapBox->{"box_vacation_enable"} ) {
        return 1;
    }

    if( !defined($imapBox->{"box_login"}) ) {
        return 1;
    }
    my $boxLogin = $imapBox->{"box_login"};

    &OBM::toolBox::write_log( "Creation du message d'absence de la boite '".$boxLogin."'", "W" );

    if( !defined( $imapBox->{"box_vacation_message"} ) ) {
        return 1;
    }
    my $boxVacationMessage = $imapBox->{"box_vacation_message"};

    if( !defined( $imapBox->{"box_email"} ) ) {
        return 1;
    }
    my $boxEmails = $imapBox->{"box_email"};


    push( @{$sieveScript}, "require \"vacation\";\n" );
    push( @{$sieveScript}, "\n" );

    push( @{$sieveScript}, "vacation :addresses [ " );
    for( my $i=0; $i<=$#{$boxEmails}; $i++ ) {
        if( $i != 0 ) {
            $sieveScript->[$#{$sieveScript}] .= ", ";
        }
        $sieveScript->[$#{$sieveScript}] .= "\"".$boxEmails->[$i]."\"";
    }
    $sieveScript->[$#{$sieveScript}] .= " ] \"".to_utf8( { -string => $boxVacationMessage, -charset => $defaultCharSet } )."\";\n";

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
