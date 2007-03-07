#########################################################################
# OBM           - File : OBM::sieve.pm (Perl Module)                    #
#               - Desc : Librairie Perl pour aliamin                    #
#########################################################################
# Cree le 2005-07-21                                                    #
#########################################################################
# $Id$                 #
#########################################################################
package OBM::imapd;

use Cyrus::IMAP::Admin;
use Cyrus::SIEVE::managesieve;
use Unicode::MapUTF8 qw(to_utf8 utf8_supported_charset);
require OBM::toolBox;
require OBM::dbUtils;
use OBM::Parameters::common;
use OBM::Parameters::cyrusConf;
require Exporter;

@ISA = qw(Exporter);
@EXPORT_const = qw();
@EXPORT_function = qw();
@EXPORT = (@EXPORT_function, @EXPORT_const);
@EXPORT_OK = qw();


# Necessaire pour le bon fonctionnement du package
$debug=1;


sub imapdConnectSrv {
    my( $srvDesc ) = @_;

    if( !exists($srvDesc->{"imap_server_login"}) || ($srvDesc->{"imap_server_login"} eq "") ) {
        return 0;
    }elsif( !exists($srvDesc->{"imap_server_passwd"}) || ($srvDesc->{"imap_server_passwd"} eq "") ) {
        return 0;
    }elsif( !exists($srvDesc->{"imap_server_ip"}) || ($srvDesc->{"imap_server_ip"} eq "") ) {
        return 0;
    }


    &OBM::toolBox::write_log( "Connexion au serveur IMAP '".$srvDesc->{"imap_server_name"}."' en tant que '".$srvDesc->{"imap_server_login"}."'", "W" );
    $srvDesc->{"imap_server_conn"} = Cyrus::IMAP::Admin->new($srvDesc->{"imap_server_ip"});
        
    if( !defined( $srvDesc->{"imap_server_conn"} ) ) {
        &OBM::toolBox::write_log( "Probleme lors de la connexion au serveur IMAP", "W" );
        imapDisconnectSrv( $srvDesc );
        return 0;

    }else {
        if( $srvDesc->{"imap_server_conn"}->authenticate( -user=>$srvDesc->{"imap_server_login"}, -password=>$srvDesc->{"imap_server_passwd"}, -mechanism=>"login" ) ) {
            &OBM::toolBox::write_log( "Connexion au serveur IMAP etablie", "W" );
            
        }else {
            &OBM::toolBox::write_log( "Echec de connexion au serveur IMAP", "W" );
            imapDisconnectSrv( $srvDesc );

            return 0;
        }
    }

    return 1;
}


sub imapDisconnectSrv {
    my( $srvDesc ) = @_;

    if( exists($srvDesc->{"imap_server_conn"}) && defined($srvDesc->{"imap_server_conn"}) ) {
        &OBM::toolBox::write_log( "Deconnexion du serveur '".$srvDesc->{"imap_server_name"}."'", "W" );
        undef $srvDesc->{"conn"};
    }

    return 0;
}


sub getAdminImapPasswd {
    my( $dbHandler, $srvList ) = @_;
    my $cyrusAdmin = &OBM::toolBox::cloneStruct(OBM::Parameters::cyrusConf::cyrusAdmin);

    # Le statement handler (pointeur sur le resultat)
    my $queryResult;

    # La requete a executer - obtention des informations sur l'administrateur de
    # la messagerie.
    my $query = "SELECT usersystem_password FROM UserSystem WHERE usersystem_login='".$cyrusAdmin->{"login"}."'";

    # On execute la requete
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
        return 0;
    }

    if( !(($cyrusAdmin->{"passwd"}) = $queryResult->fetchrow_array) ) {
        &OBM::toolBox::write_log( "Echec: mot de passe de l'administrateur IMAP inconnu", "W" );
        return 0;
    }

    # Si on a recupere un resultat, c'est bon...
    $queryResult->finish;

    # On positionne le login et mot de passe au niveau de la description des
    # serveurs
    for( my $i=0; $i<=$#$srvList; $i++ ) {
        for( my $j=0; $j<=$#{$srvList->[$i]->{"imap_servers"}}; $j++ ) {
            $srvList->[$i]->{"imap_servers"}->[$j]->{"imap_server_login"} = $cyrusAdmin->{"login"};
            $srvList->[$i]->{"imap_servers"}->[$j]->{"imap_server_passwd"} = $cyrusAdmin->{"passwd"};
        }
    }

    return 1;
}


sub getServerByDomain {
    my( $dbHandler, $domainList ) = @_;
    my $srvListByDomain = &OBM::toolBox::cloneStruct(OBM::Parameters::cyrusConf::listDomainSrv);

    for( my $i=0; $i<=$#$domainList; $i++ ) {
        if( $domainList->[$i]->{"meta_domain"} ) {
            next;
        }

        my $currentDomainSrvList = &OBM::toolBox::cloneStruct(OBM::Parameters::cyrusConf::domainSrv);
        $currentDomainSrvList->{"domain"} = $domainList->[$i];

        &OBM::toolBox::write_log( "Recuperation des serveurs de courrier pour le domaine '".$domainList->[$i]->{"domain_name"}."'", "W" );
        my $srvQuery = "SELECT i.host_id, i.host_name, i.host_ip FROM P_Host i, P_MailServer j WHERE (i.host_domain_id=0 OR i.host_domain_id=".$domainList->[$i]->{"domain_id"}.") AND i.host_id=j.mailserver_host_id";

        # On execute la requete
        my $queryResult;
        if( !&OBM::dbUtils::execQuery( $srvQuery, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
            next;
        }

        my @srvList = ();
        while( my( $hostId, $hostName, $hostIp) = $queryResult->fetchrow_array ) {
            my $srv = &OBM::toolBox::cloneStruct(OBM::Parameters::cyrusConf::srvDesc);
            $srv->{"imap_server_id"} = $hostId;
            $srv->{"imap_server_name"} = $hostName;
            $srv->{"imap_server_ip"} = $hostIp;

            push( @srvList, $srv );
        }
        $currentDomainSrvList->{"imap_servers"} = \@srvList;

        push( @{$srvListByDomain}, $currentDomainSrvList );
    }

    return $srvListByDomain;
}


sub loadBdValues {
    my( $dbHandler, $listDomainSrv ) = @_;
for( my $i=0; $i<=$#$listDomainSrv; $i++ ) {
        my $currentDomainSrv = $listDomainSrv->[$i];
        my $domainDesc = $currentDomainSrv->{"domain"};
        my $domainServerList = $currentDomainSrv->{"imap_servers"};

        # On parcourt les domaines
        &OBM::toolBox::write_log( "Traitement du domaine '".$domainDesc->{"domain_name"}."'", "W" );

        for( my $j=0; $j<=$#{$domainServerList}; $j++ ) {
            my $srvDesc = $domainServerList->[$j];

            # On parcourt les serveurs
            &OBM::toolBox::write_log( "Traitement du serveur ".$srvDesc->{"imap_server_name"}, "W" );

            foreach my $boxType ( keys(%$OBM::Parameters::cyrusConf::boxTypeDef) ) {
                my $currentBoxTypeDef = $OBM::Parameters::cyrusConf::boxTypeDef->{$boxType};

                # Récupération des informations attachées à ce domaine et ce serveur
                &OBM::toolBox::write_log( "Chargement des informations de type '".$boxType."' depuis la base de donnees OBM", "W" );

                if( defined($currentBoxTypeDef->{"get_bd_values"}) ) {
                    $srvDesc->{"BD_".$boxType} = &{$currentBoxTypeDef->{"get_bd_values"}}( $dbHandler, $domainDesc, $srvDesc->{"imap_server_id"} );
                }
            }
        }

        &OBM::toolBox::write_log( "-----------------", "W" );
    }
}


sub updateServers {
    my( $listDomainSrv ) = @_;
    my $errors = 0;

    for( my $i=0; $i<=$#$listDomainSrv; $i++ ) {
        my $domainDesc = $listDomainSrv->[$i]->{"domain"};

        if( !exists($listDomainSrv->[$i]->{"imap_servers"}) || !defined($listDomainSrv->[$i]->{"imap_servers"}) ) {
            next;
        }

        my $domainServerList = $listDomainSrv->[$i]->{"imap_servers"};

        for( my $j=0; $j<=$#{$domainServerList}; $j++ ) {
            my $srvDesc = $domainServerList->[$j];
            &OBM::toolBox::write_log( "Gestion du serveur '".$srvDesc->{"imap_server_name"}."' pour le domaine '".$domainDesc->{"domain_name"}."'", "W" );

            if( !imapdConnectSrv( $srvDesc ) ) {
                $errors++;
                next;
            }

            if( imapGetDomainBoxesList( $domainDesc, $srvDesc ) ) {
                imapDisconnectSrv( $srvDesc );
                $errors++;
                next;
            }

            &OBM::toolBox::write_log( "-----------------", "W" );

            if( updateServer( $srvDesc ) ) {
                $errors++;
            };

            imapDisconnectSrv( $srvDesc );
        }

        &OBM::toolBox::write_log( "-----------------", "W" );
    }

    return $errors;
}


sub updateServer {
    my ( $srvDesc ) = @_;
    my $imapdHdl = $srvDesc->{"imap_server_conn"};
    my $errors = 0;

    foreach my $boxType ( keys(%$OBM::Parameters::cyrusConf::boxTypeDef) ) {
        &OBM::toolBox::write_log( "Traitement des comptes de type '".$boxType."'", "W" );

        my $srvListImapBox = $srvDesc->{"SRV_".$boxType};
        my $bdListImapBox = $srvDesc->{"BD_".$boxType};
        while( my( $boxLogin, $srvImapBox ) = each(%{$srvListImapBox}) ) {
            if( !exists($bdListImapBox->{$boxLogin}) ) {
                if( deleteBox( $srvDesc, $srvImapBox ) ) {
                    $errors++;
                }
            }
        }

        while( my( $boxLogin, $bdImapBox ) = each(%{$bdListImapBox}) ) {
            # Si la bal n'existe pas...
            if( !exists($srvListImapBox->{$boxLogin}) ) {
                # ... on la crée
                if( createBox( $srvDesc, $bdImapBox ) ) {
                    $errors++;
                    next;
                }
            }

            # ... sinon on la met simplement à jour
            if( updateBox( $srvDesc, $srvListImapBox->{$boxLogin}, $bdImapBox ) ) {
                $errors++
            }
        }
    }

    return $errors;
}


sub imapGetDomainBoxesList {
    my( $domainDesc, $srvDesc ) = @_;
    my $imapSrvConn = $srvDesc->{"imap_server_conn"};
    my $domainName = $domainDesc->{"domain_name"};

    foreach my $boxType ( keys(%$OBM::Parameters::cyrusConf::boxTypeDef) ) {
        &OBM::toolBox::write_log( "Obtention des comptes de type '".$boxType."'", "W" );

        my $boxPrefix = $OBM::Parameters::cyrusConf::boxTypeDef->{$boxType}->{"prefix"}.$OBM::Parameters::cyrusConf::boxTypeDef->{$boxType}->{"separator"};

        # on recupere la liste des Box de ce type
        my @balList = $imapSrvConn->listmailbox( '%@'.$domainName, $boxPrefix );
        if( $imapSrvConn->error ) {
            return 1;
        }

        my $listImapBox = &OBM::toolBox::cloneStruct( OBM::Parameters::cyrusConf::listImapBox );
        for( my $i=0; $i<=$#balList; $i++ ) {
            if( $balList[$i][1] =~ /nonexistent/i ) {
                next;
            }

            my $imapUser = &OBM::toolBox::cloneStruct( OBM::Parameters::cyrusConf::imapBox );  
            $imapUser->{"box_name"} = $balList[$i][0];
            $imapUser->{"box_login"} = $balList[$i][0];
            $imapUser->{"box_login"} =~ s/^$boxPrefix//;
            $imapUser->{"box_quota"} = imapGetBoxQuota( $imapSrvConn, $imapUser->{"box_name"} );
            $imapUser->{"box_acl"} = undef;

            if( !exists($listImapBox->{$imapUser->{"box_login"}}) ) {
                $listImapBox->{$imapUser->{"box_login"}} = $imapUser;
            }
#            push( @{$listImapBox}, $imapUser );
        }

        $srvDesc->{"SRV_".$boxType} = $listImapBox;
    }

    return 0;
}


sub createBox {
    my( $srvDesc, $imapBox ) = @_;

    if( !defined($srvDesc->{"imap_server_conn"}) ) {
        return 1;
    }
    my $imapSrvConn = $srvDesc->{"imap_server_conn"};

    if( !defined($imapBox->{"box_login"}) ) {
        return 1;
    }
    my $boxLogin = $imapBox->{"box_login"};

    if( !defined($imapBox->{"box_name"}) ) {
        return 1;
    }
    my $boxName = $imapBox->{"box_name"};


    &OBM::toolBox::write_log( "Creation de la boite '".$boxLogin."'", "W" );
    $imapSrvConn->create( $boxName );
    if( $imapSrvConn->error ) {
        &OBM::toolBox::write_log( "Echec : lors de la creation de la boite '".$boxLogin."'", "W" );
        return 1;
    }

    updateSieve( $srvDesc, $imapBox );

    return 0;
}


sub updateBox {
    my( $srvDesc, $oldImapBoxDesc, $newImapBoxDesc ) = @_;
    my $errors = 0;

    if( !defined($srvDesc->{"imap_server_conn"}) ) {
        return 1;
    }
    my $imapSrvConn = $srvDesc->{"imap_server_conn"};

    if( !defined($oldImapBoxDesc->{"box_login"}) ) {
        return 1;
    }
    my $boxLogin = $oldImapBoxDesc->{"box_login"};

    if( !defined($oldImapBoxDesc->{"box_name"}) ) {
        return 1;
    }
    my $boxName = $oldImapBoxDesc->{"box_name"};


    &OBM::toolBox::write_log( "Mise a jour de la boite '".$boxLogin."'", "W" );

    # Mise a jour du quota
    if( exists($oldImapBoxDesc->{"box_quota"}) && exists($newImapBoxDesc->{"box_quota"}) && ($oldImapBoxDesc->{"box_quota"}!=$newImapBoxDesc->{"box_quota"}) ) {
        &OBM::toolBox::write_log( "Mise a jour du quota de la boite '".$boxLogin."'", "W" );
        if( imapSetBoxQuota( $imapSrvConn, $boxName, $newImapBoxDesc->{"box_quota"} ) ) {
            $errors++;
        }
    }
}


sub deleteBox {
    my( $srvDesc, $imapBox ) = @_;

    if( !defined($srvDesc->{"imap_server_conn"}) ) {
        return 1;
    }
    my $imapSrvConn = $srvDesc->{"imap_server_conn"};

    if( !defined($imapBox->{"box_login"}) ) {
        return 1;
    }
    my $boxLogin = $imapBox->{"box_login"};

    if( !defined($imapBox->{"box_name"}) ) {
        return 1;
    }
    my $boxName = $imapBox->{"box_name"};


    &OBM::toolBox::write_log( "Suppression de la boite '".$boxLogin."'", "W" );
    if( setBoxAcl( $imapSrvConn, $boxName, $srvDesc->{"imap_server_login"}, "admin" ) ) {
        return 1;
    }

    $imapSrvConn->delete( $boxName );
    if( $imapSrvConn->error ) {
        &OBM::toolBox::write_log( "Echec : lors de la suppression de la boite '".$boxLogin."'", "W" );
        return 1;
    }

    return 0;
}


sub setBoxAcl {
    my( $imapSrvConn, $boxLogin, $boxRightUser, $boxRight ) = @_;
    my $rights = OBM::Parameters::cyrusConf::definedRight;

    if( !defined($rights->{$boxRight}) ) {
        return 1;
    }

    $imapSrvConn->setaclmailbox( $boxLogin, $boxRightUser => $rights->{$boxRight} );

    if( $imapSrvConn->error ) {
        return 1;
    }

    return 0;
}


sub imapGetBoxQuota {
    my( $imapSrvConn, $boxName ) = @_;

    if( !defined($imapSrvConn) || !defined($boxName) ) {
        return 0;
    }

    my @quotaDesc = $imapSrvConn->listquotaroot( $boxName );
    if( $imapSrvConn->error ) {
        return 0;
    }

    if( !defined( $quotaDesc[2][1] ) ) {
        $quotaDesc[2][1] = 0;
    }

    return $quotaDesc[2][1];
}


sub imapSetBoxQuota {
    my( $imapSrvConn, $boxName, $quota ) = @_;

    if( !$quota ) {
        $imapSrvConn->setquota( $boxName );
    }else {
        $imapSrvConn->setquota( $boxName, "STORAGE", $quota*1000 );
    }

    if( $imapSrvConn->error ) {
        return 1;
    }

    return 0;
}


sub updateSieve {
    my( $srvDesc, $imapBox ) = @_;
    my @sieveScript;

    if( !defined($imapBox->{"box_login"}) ) {
        return 1;
    }
    my $boxLogin = $imapBox->{"box_login"};


    # On met les règles pour le vacation dans le script Sieve
    mkSieveVacationScript( $imapBox, \@sieveScript );

    if( connectSrvSieve( $srvDesc, $boxLogin ) ) {
        return 1;
    }

    my $sieveScriptName = $boxLogin.".sieve";
    $sieveScriptName =~ s/@/-/g;
    my $localSieveScriptName = $tmpAliamin.$sieveScriptName;

    &OBM::toolBox::write_log( "Mise a jour du script Sieve pour l'utilisateur : '".$boxLogin."'", "W" );

    # On desactive l'ancien script
    sieve_activate( $srvDesc->{"imap_sieve_server_conn"}, "" );
    # On supprime l'ancien script
    sieve_delete( $srvDesc->{"imap_sieve_server_conn"}, $sieveScriptName );

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
        &OBM::toolBox::execCmd( "/bin/rm -f ".$localSieveScriptName );
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

    &OBM::toolBox::write_log( "Creation du message d'abscence de la boite '".$boxLogin."'", "W" );

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
