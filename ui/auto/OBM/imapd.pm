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
use Unicode::MapUTF8 qw(to_utf8 utf8_supported_charset);
require OBM::toolBox;
require OBM::dbUtils;
require OBM::utils;
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
    my $cyrusAdmin = &OBM::utils::cloneStruct(OBM::Parameters::cyrusConf::cyrusAdmin);

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
    my $srvListByDomain = &OBM::utils::cloneStruct(OBM::Parameters::cyrusConf::listDomainSrv);

    for( my $i=0; $i<=$#$domainList; $i++ ) {
        if( $domainList->[$i]->{"meta_domain"} ) {
            next;
        }

        my $currentDomainSrvList = &OBM::utils::cloneStruct(OBM::Parameters::cyrusConf::domainSrv);
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
            my $srv = &OBM::utils::cloneStruct(OBM::Parameters::cyrusConf::srvDesc);
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
                if( createBox( $srvDesc, $boxType, $bdImapBox ) ) {
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
        my @boxList = $imapSrvConn->listmailbox( '%@'.$domainName, $boxPrefix );
        if( $imapSrvConn->error ) {
            return 1;
        }

        my $listImapBox = &OBM::utils::cloneStruct( OBM::Parameters::cyrusConf::listImapBox );
        for( my $i=0; $i<=$#boxList; $i++ ) {
            if( $boxList[$i][1] =~ /nonexistent/i ) {
                next;
            }

            my $imapUser = &OBM::utils::cloneStruct( OBM::Parameters::cyrusConf::imapBox );  
            $imapUser->{"box_name"} = $boxList[$i][0];
            $imapUser->{"box_login"} = $boxList[$i][0];
            $imapUser->{"box_login"} =~ s/^$boxPrefix//;
            $imapUser->{"box_quota"} = imapGetBoxQuota( $imapSrvConn, $imapUser->{"box_name"} );
            $imapUser->{"box_acl"} = imapGetBoxAcl( $imapSrvConn, $imapUser->{"box_name"} );

            if( !exists($listImapBox->{$imapUser->{"box_login"}}) ) {
                $listImapBox->{$imapUser->{"box_login"}} = $imapUser;
            }
        }

        $srvDesc->{"SRV_".$boxType} = $listImapBox;
    }

    return 0;
}


sub createBox {
    my( $srvDesc, $boxType, $imapBox ) = @_;
    my $boxTypeDef = $OBM::Parameters::cyrusConf::boxTypeDef;

    if( !defined($srvDesc->{"imap_server_conn"}) ) {
        return 1;
    }
    my $imapSrvConn = $srvDesc->{"imap_server_conn"};

    if( !defined($boxType) ) {
        return 1;
    }

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


    if( exists($boxTypeDef->{$boxType}->{"create_box"}) && defined($boxTypeDef->{$boxType}->{"create_box"}) ) {
        &{$boxTypeDef->{$boxType}->{"create_box"}}( $srvDesc, $imapBox );
    }

    return 0;
}


sub updateBox {
    my( $srvDesc, $oldImapBoxDesc, $newImapBoxDesc ) = @_;
    my $errors = 0;

    if( !defined($srvDesc->{"imap_server_conn"}) ) {
        return 1;
    }
    my $imapSrvConn = $srvDesc->{"imap_server_conn"};

    if( !defined($newImapBoxDesc->{"box_login"}) ) {
        return 1;
    }
    my $boxLogin = $newImapBoxDesc->{"box_login"};

    if( !defined($newImapBoxDesc->{"box_name"}) ) {
        return 1;
    }
    my $boxName = $newImapBoxDesc->{"box_name"};


    &OBM::toolBox::write_log( "Mise a jour de la boite '".$boxLogin."'", "W" );

    # Mise a jour du quota
    if( exists($oldImapBoxDesc->{"box_quota"}) && exists($newImapBoxDesc->{"box_quota"}) && ($oldImapBoxDesc->{"box_quota"}!=$newImapBoxDesc->{"box_quota"}) ) {
        &OBM::toolBox::write_log( "Mise a jour du quota de la boite '".$boxLogin."'", "W" );
        if( imapSetBoxQuota( $imapSrvConn, $boxName, $newImapBoxDesc->{"box_quota"} ) ) {
            $errors++;
        }
    }

    # Mise a jour des ACLs
    if( &OBM::toolBox::aclUpdated( $oldImapBoxDesc->{"box_acl"}, $newImapBoxDesc->{"box_acl"} ) ) {
        &OBM::toolBox::write_log( "Mise a jour des ACL de la boite '".$boxLogin."'", "W" );

        if( setBoxAcl( $imapSrvConn, $boxName, $oldImapBoxDesc->{"box_acl"}, $newImapBoxDesc->{"box_acl"} ) ) {
            $errors++;
        }
    
    }

    return $errors;
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
    if( imapSetBoxAcl( $imapSrvConn, $boxName, $srvDesc->{"imap_server_login"}, "admin" ) ) {
        return 1;
    }

    $imapSrvConn->delete( $boxName );
    if( $imapSrvConn->error ) {
        &OBM::toolBox::write_log( "Echec : lors de la suppression de la boite '".$boxLogin."'", "W" );
        return 1;
    }

    return 0;
}


sub imapSetBoxAcl {
    my( $imapSrvConn, $boxName, $boxRightUser, $boxRight ) = @_;
    my $rights = OBM::Parameters::cyrusConf::definedRight;

    if( !defined($rights->{$boxRight}) ) {
        return 1;
    }

    my $imapRight = $rights->{$boxRight};

    if( ($boxRightUser eq "anyone") && ($boxRight ne "none") ) {
        $imapRight .= $rights->{"post"};
    }

    $imapSrvConn->setaclmailbox( $boxName, $boxRightUser => $imapRight );

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
        $imapSrvConn->setquota( $boxName, "STORAGE", $quota );
    }

    if( $imapSrvConn->error ) {
        return 1;
    }

    return 0;
}


sub imapGetBoxAcl {
    my ( $imapSrvConn, $boxName ) = @_;
    my $boxRight = &OBM::utils::cloneStruct(OBM::Parameters::cyrusConf::boxRight);
    my $definedRight = OBM::Parameters::cyrusConf::definedRight;

    my %boxAclList = $imapSrvConn->listacl( $boxName );
    if( $imapSrvConn->error ) {
        return undef;

    }else {
        while( my( $user, $right ) = each( %boxAclList ) ) {
            # le droit POST est gere de facon transparente
            $right =~ s/$definedRight->{"post"}//g;

            $right = checkAclRight( $definedRight, $right );

            if( $right ne $definedRight->{"none"} ) {
                $boxRight->{$right}->{$user} = 1;
            }
        }
    }

    return $boxRight;
}


sub checkAclRight {
    my( $definedRight, $right ) = @_;
    my $returnedRight = $definedRight->{"none"};

    if( exists( $definedRight->{$right} ) ) {
        return $definedRight->{$right};
    }

    my @obmRight = keys(%{$definedRight});
    for( my $i=0; $i<=$#obmRight; $i++ ) {
        if( $right =~ /^$definedRight->{$obmRight[$i]}$/ ) {
            return $obmRight[$i];
        }
    }
    
    return $returnedRight;
}


sub setBoxAcl {
    my( $imapSrvConn, $boxName, $oldAclList, $newAclList ) = @_;


    # Recuperation des sous repertoires de la boite
    my $boxPattern = $boxName;
    $boxPattern =~ s/(@.*)$/*$1/;
    my @boxStruct = $imapSrvConn->listmailbox( $boxPattern, '' );
    if( $imapSrvConn->error ) {
        return 1;
    }

    my $errors = 0;
    for( my $i=0; $i<=$#boxStruct; $i++ ) {
        while( my( $right, $oldUserList ) = each( %$oldAclList ) ) {
            my $newUserList = $newAclList->{$right};

            while( my( $userName, $value ) = each( %$oldUserList ) ) {
                if( !defined($newUserList) || !exists( $newUserList->{$userName} ) ) {
                    if( imapSetBoxAcl( $imapSrvConn, $boxStruct[$i][0], $userName, "none" ) ) {
                        $errors++;
                    }
                }
            }
        }

        my $anyoneRight = 0;
        while( my( $right, $newUserList ) = each( %$newAclList ) ) {
            my $oldUserList = $oldAclList->{$right};

            while( my( $userName, $value ) = each( %$newUserList ) ) {

                if( !defined($oldUserList) || !exists($oldUserList->{$userName}) ) {
                    if( imapSetBoxAcl( $imapSrvConn, $boxStruct[$i][0], $userName, $right ) ) {
                        $errors++;
                    }

                    if( $userName eq "anyone" ) {
                        $anyoneRight = 1;
                    }
                }
            }
        }

        if( !$anyoneRight ) {
            if( imapSetBoxAcl( $imapSrvConn, $boxStruct[$i][0], "anyone", "post" ) ) {
                $errors++;
            }
        }
    }

    return 0;
}
