package OBM::Cyrus::cyrusEngine;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
use strict;

use OBM::Parameters::common;
use Cyrus::IMAP::Admin;


sub new {
    my $self = shift;
    my( $domainList ) = @_;

    # Definition des attributs de l'objet
    my %cyrusEngineAttr = (
        domainList => undef,
        rightDefinition => undef
    );


    if( !defined($domainList) ) {
        croak( "Usage: PACKAGE->new(DOMAINLIST)" );
    }else {
        $cyrusEngineAttr{"domainList"} = $domainList;
    }

    # Definition des droits
    $cyrusEngineAttr{"rightDefinition"} = {
        none => 'none',
        read => 'lrs',
        readAdmin => 'lrsc',
        writeonly => 'li',
        writeonlyAdmin => 'lic',
        write => 'lrswid',
        writeAdmin => 'lrswidc',
        admin => 'lc',
        post => 'p'
    };

    bless( \%cyrusEngineAttr, $self );
}


sub init {
    my $self = shift;

    if( !$OBM::Parameters::common::obmModules->{"mail"} ) {
        return 0;
    }

    &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: initialisation du moteur", "W" );

    # Etablissement des connexions
    $self->_cyrusSrvsConn( 1 );

    return 1;
}


sub destroy {
    my $self = shift;

    &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: arret du moteur", "W" );

    return $self->_cyrusSrvsConn( 0 );
}


sub dump {
    my $self = shift;
    my @desc;

    push( @desc, $self );

    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}


sub _cyrusSrvsConn {
    my $self = shift;
    my( $connect ) = @_;
    my $domainsDesc = $self->{"domainList"};

    for( my $i=0; $i<=$#$domainsDesc; $i++ ) {
        my $currentDomainDesc = $domainsDesc->[$i];

        if( $currentDomainDesc->{"meta_domain"} ) {
            next;
        }

        if( !defined($currentDomainDesc->{"imap_servers"}) ) {
            next;
        }

        my $domainSrvList = $currentDomainDesc->{"imap_servers"};
        if( $#$domainSrvList < 0 ) {
            next;
        }

        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: gestion des serveurs IMAP du domaine '".$currentDomainDesc->{"domain_name"}."'", "W" );
        for( my $j=0; $j<=$#$domainSrvList; $j++ ) {
            if( $connect ) {
                $self->_connectCyrusSrv( $domainSrvList->[$j] );
            }else {
                $self->_disconnectCyrusSrv( $domainSrvList->[$j] );
            }
        }
    }

    return 1;
}


sub _connectCyrusSrv {
    my $self = shift;
    my( $srvDesc ) = @_;

    if( !defined($srvDesc->{"imap_server_login"}) ) {
        return 0;
    }elsif( !defined($srvDesc->{"imap_server_passwd"}) ) {
        return 0;
    }elsif( !defined($srvDesc->{"imap_server_ip"}) ) {
        return 0;
    }

    &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: connexion au serveur IMAP '".$srvDesc->{"imap_server_name"}."' en tant que '".$srvDesc->{"imap_server_login"}."'", "W" );

    $srvDesc->{"imap_server_conn"} = Cyrus::IMAP::Admin->new($srvDesc->{"imap_server_ip"});

    if( !defined( $srvDesc->{"imap_server_conn"} ) ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: probleme lors de la connexion au serveur IMAP", "W" );
        $self->_disconnectCyrusSrv( $srvDesc );
        return 0;

    }else {
        if( $srvDesc->{"imap_server_conn"}->authenticate( -user=>$srvDesc->{"imap_server_login"}, -password=>$srvDesc->{"imap_server_passwd"}, -mechanism=>"login" ) ) {
            &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: connexion au serveur IMAP etablie", "W" );

        }else {
            &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: echec de connexion au serveur IMAP", "W" );
            $self->_disconnectCyrusSrv( $srvDesc );
            return 0;

        }
    }

    return 1;
}


sub _disconnectCyrusSrv {
    my $self = shift;
    my( $srvDesc ) = @_;

    if( defined($srvDesc->{"imap_server_conn"}) ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: deconnexion du serveur '".$srvDesc->{"imap_server_name"}."'", "W" );
        undef $srvDesc->{"imap_server_conn"};
    }

    return 1;
}


sub _findDomainbyId {
    my $self = shift;
    my( $domainId ) = @_;
    my $domainDesc = undef;

    if( !defined($domainId) || ($domainId !~ /^\d+$/) ) {
        return undef;
    }

    for( my $i=0; $i<=$#{$self->{"domainList"}}; $i++ ) {
        if( $self->{"domainList"}->[$i]->{"domain_id"} == $domainId ) {
            $domainDesc = $self->{"domainList"}->[$i];
            last;
        }
    }

    return $domainDesc;
}


sub _findCyrusSrvbyId {
    my $self = shift;
    my( $domainId, $cyrusSrvId ) = @_;

    if( !defined($domainId) || ($domainId !~ /^\d+$/) ) {
        return undef;

    }elsif(!defined($cyrusSrvId) || ($cyrusSrvId !~ /^\d+$/) ) {
        return undef;

    }

    my $domainDesc = $self->_findDomainbyId( $domainId );
    if( !defined($domainDesc) ) {
        return undef;
    }

    if( !defined($domainDesc->{"imap_servers"}) || ($#{$domainDesc->{"imap_servers"}} < 0) ) {
        return undef;
    }

    my $cyrusSrvList = $domainDesc->{"imap_servers"};
    my $cyrusSrv = undef;
    for( my $i=0; $i<=$#$cyrusSrvList; $i++ ) {
        if( $cyrusSrvList->[$i]->{"imap_server_id"} == $cyrusSrvId ) {
            $cyrusSrv = $cyrusSrvList->[$i];
            last;
        }
    }

    return $cyrusSrv;
}


sub _doWork {
    my $self = shift;
    my( $cyrusSrv, $object ) = @_;
    my $returnCode = 1;

    if( !defined($cyrusSrv) || !defined($object) ) {
        return 0;
    }


    # La bal existe ?
    my %srvBalDesc;
    my $isExist = $self->isMailboxExist( $cyrusSrv, \%srvBalDesc, $object->getMailboxPrefix(), $object->getMailboxName( "new" ) );
    if( !defined($isExist) ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: probleme lors de l'obtention des informations de la boite sur le serveur Cyrus", "W" );
        return 0;

    }elsif( $isExist && $object->getDelete() ) {
        # On la supprime
        $returnCode = $self->_deleteBox( $cyrusSrv, $object );
        if( !$returnCode ) {
            &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: echec lors de la suppression de la boite", "W" );
            return 0;
        }

    }elsif( $isExist && !$object->getDelete() ) {
        # On met à jour
        $returnCode = $self->_updateMailbox( $cyrusSrv, $object );
        if( !$returnCode ) {
            &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: echec lors de la MAJ de la boite", "W" );
            return 0;
        }

    }elsif( !$isExist && !$object->getDelete() ) {
        # On la crée
        $returnCode = $self->_createMailbox( $cyrusSrv, $object );
        if( !$returnCode ) {
            &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: echec lors de la creation/renommage de la boite", "W" );
            return 0;
        }

    }

    return $returnCode;
}


sub update {
    my $self = shift;
    my( $object ) = @_;

    if( !defined($object) ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: mise a jour d'un objet non definit - Operation annulee !", "W" );
        return 0;
    }elsif( !defined($object->{"type"}) ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: mise a jour d'un objet de type non définit - Operation annulee !", "W" );
        return 0;
    }elsif( !defined($object->{"domainId"}) ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: mise a jour d'un objet de domaine non definit - Operation annulee !", "W" );
        return 0;
    }

    # Récupération du nom de la boîte à traiter
    my $mailBoxName = $object->getMailboxName( "new" );
    if( !defined($mailBoxName) ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: pas de boite a lettres definies pour l'objet : ".$object->getEntityDescription(), "W" );
        return 1;
    }

    # Récupération de la description du serveur de la boîte à traiter
    my $mailserverId = $object->getMailServerId();
    if( !defined($mailserverId) ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: serveur de courrier IMAP non definit - Operation annulee", "W" );
        return 0;
    }

    my $cyrusSrv = $self->_findCyrusSrvbyId( $object->{"domainId"}, $mailserverId );
    if( !defined($cyrusSrv) ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: serveur de courrier IMAP d'identifiant '".$mailserverId."' inconnu - Operation annulee !", "W" );
        return 0;
    }

    # Est-on connecté à ce serveur
    if( !defined($cyrusSrv->{"imap_server_conn"}) ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: probleme de connexion avec le serveur de courrier IMAP d'identifiant '".$mailBoxName."' - Operation annulee !", "W" );
        return 0;
    }

    my $returnCode = $self->_doWork( $cyrusSrv, $object );
    if( !$returnCode ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: probleme de traitement de l'objet de type '".$object->{"type"}."' - Operation annulee !", "W" );
        return 0;
    }

    return $returnCode;
}


sub updateAcl {
    my $self = shift;
    my( $object ) = @_;

    if( !defined($object) ) {
        return 0;
    }

    # Récupération du nom de la boîte à traiter
    my $mailBoxName = $object->getMailboxName( "new" );
    if( !defined($mailBoxName) ) {
        return 0;
    }

    # Récupération de la description du serveur de la boîte à traiter
    my $cyrusSrv = $self->_findCyrusSrvbyId( $object->{"domainId"}, $object->getMailServerId() );
    if( !defined($cyrusSrv) ) {
        return 0;
    }

    # Est-on connecté à ce serveur
    if( !defined($cyrusSrv->{"imap_server_conn"}) ) {
        return 0;
    }

    return $self->_imapSetMailboxAcls( $cyrusSrv, $object );
}


sub isMailboxExist {
    my $self = shift;
    my( $cyrusSrv, $srvBalDesc, $mailboxPrefix, $mailboxName ) = @_;

    if( !defined($cyrusSrv->{"imap_server_conn"}) ) {
        return undef;
    }
    my $cyrusSrvConn = $cyrusSrv->{"imap_server_conn"};

    if( !defined($mailboxName) ) {
        return undef;
    }

    my @mailBox = $cyrusSrvConn->listmailbox( $mailboxName, $mailboxPrefix );
    if( $cyrusSrvConn->error ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: erreur Cyrus lors de la recherche de la BAL : ".$cyrusSrvConn->error(), "W" );
        return undef;
    }

    if( $#mailBox < 0 ) {
        return 0;
    }elsif( $#mailBox > 0 ) {
        return undef;
    }


    # Si la boîte existe on charge ses caractéristiques
    if( defined($srvBalDesc) ) {
        $srvBalDesc->{"box_name"} = $mailBox[0][0];
        $srvBalDesc->{"box_login"} = $mailBox[0][0];
        $srvBalDesc->{"box_login"} =~ s/^$mailboxPrefix//;
        $srvBalDesc->{"box_quota"} = $self->getMailboxQuota( $cyrusSrv, $mailboxPrefix, $mailboxName );
    }


    return 1;
}


sub getMailboxQuota {
    my $self = shift;
    my( $cyrusSrv, $mailboxPrefix, $mailboxName ) = @_;
    my $mailBoxQuota = 0;

    if( !defined($cyrusSrv->{"imap_server_conn"}) ) {
        return 0;
    }
    my $cyrusSrvConn = $cyrusSrv->{"imap_server_conn"};

    if( !defined($mailboxName) || !defined($mailboxPrefix) ) {
        return 0;
    }


    my @quotaDesc = $cyrusSrvConn->listquotaroot( $mailboxPrefix.$mailboxName );
    if( $cyrusSrvConn->error ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: erreur Cyrus a l'obtention du quota maximum : ".$cyrusSrvConn->error(), "W" );
        return undef;
    }

    if( defined( $quotaDesc[2][1] ) ) {
        $mailBoxQuota = $quotaDesc[2][1];
    }

    return $mailBoxQuota;
}


sub getMailboxQuotaUse {
    my $self = shift;
    my( $object ) = @_;

    if( !defined($object) ) {
        return undef;
    }

    # Récupération du nom de la boîte à traiter
    my $mailBoxName = $object->getMailboxName( "new" );
    if( !defined($mailBoxName) ) {
        return undef;
    }

    # Récupération de la description du serveur de la boîte à traiter
    my $cyrusSrv = $self->_findCyrusSrvbyId( $object->{"domainId"}, $object->getMailServerId() );
    if( !defined($cyrusSrv) ) {
        return undef;
    }

    # Est-on connecté à ce serveur
    if( !defined($cyrusSrv->{"imap_server_conn"}) ) {
        return undef;
    }

    # Obtention du quota utilisé
    my $cyrusSrvConn = $cyrusSrv->{"imap_server_conn"};
    my $mailBoxQuotaUse = 0;
    my $boxPrefix = $object->getMailboxPrefix();


    my @quotaDesc = $cyrusSrvConn->listquotaroot( $boxPrefix.$mailBoxName );
    if( $cyrusSrvConn->error ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: erreur Cyrus a l'obtention du quota utilise : ".$cyrusSrvConn->error(), "W" );
        return undef;
    }

    if( defined( $quotaDesc[2][1] ) ) {
        $mailBoxQuotaUse = $quotaDesc[2][0];
    }

    return $mailBoxQuotaUse;
}


sub _imapSetMailboxQuota {
    my $self = shift;
    my( $cyrusSrv, $object ) = @_;

    if( !defined($cyrusSrv->{"imap_server_conn"}) ) {
        return 0;
    }
    my $cyrusSrvConn = $cyrusSrv->{"imap_server_conn"};

    if( !defined($object) ) {
        return 0;
    }

    my $boxName = $object->getMailboxName( "new" );
    my $boxPrefix = $object->getMailboxPrefix();
    my $boxQuota = $object->getMailboxQuota();

    if( !$boxQuota ) {
        $cyrusSrvConn->setquota( $boxPrefix.$boxName );
    }else {
        $cyrusSrvConn->setquota( $boxPrefix.$boxName, "STORAGE", $boxQuota );
    }

    if( $cyrusSrvConn->error ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: erreur Cyrus au positionnement du quota utilise : ".$cyrusSrvConn->error(), "W" );
        return 0;
    }

    return 1;
}


sub _imapGetMailboxAcls {
    my $self = shift;
    my( $cyrusSrv, $object ) = @_;

    if( !defined($cyrusSrv->{"imap_server_conn"}) ) {
        return 0;
    }
    my $cyrusSrvConn = $cyrusSrv->{"imap_server_conn"};

    if( !defined($object) ) {
        return 0;
    }

    my $boxName = $object->getMailboxName( "new" );
    my $boxPrefix = $object->getMailboxPrefix();
    my $definedRight = $self->{"rightDefinition"};

    my %boxAclList = $cyrusSrvConn->listacl( $boxPrefix.$boxName );
    my $boxRight;
    if( $cyrusSrvConn->error ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: erreur Cyrus a l'obtention des ACLs : ".$cyrusSrvConn->error(), "W" );
        return undef;

    }else {
        while( my( $user, $right ) = each( %boxAclList ) ) {
            # le droit POST est gere de façon transparente
            $right =~ s/$definedRight->{"post"}//g;

            $right = $self->_checkAclRight( $right );

            if( $user ne "anyone" ) {
                $boxRight->{$right}->{$user} = 1;
            }
        }
    }

    return $boxRight;
}


sub _checkAclRight {
    my $self = shift;
    my( $right ) = @_;
    my $definedRight = $self->{"rightDefinition"};

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


sub _imapSetMailboxAcls {
    my $self = shift;
    my( $cyrusSrv, $object ) = @_;

    if( !defined($cyrusSrv->{"imap_server_conn"}) ) {
        return 0;
    }
    my $cyrusSrvConn = $cyrusSrv->{"imap_server_conn"};

    if( !defined($object) ) {
        return 0;
    }

    # Si l'objet n'est pas chargé avec les liens, on ne met pas à jour les ACLs
    if( !$object->isLinks() ) {
        return 1;
    }

    my $boxName = $object->getMailboxName( "new" );
    my $boxPrefix = $object->getMailboxPrefix();

    # Obtention des ACL depuis la BD (new) et depuis le serveur (old)
    my $newAclList = $object->getMailboxAcl();
    my $oldAclList = $self->_imapGetMailboxAcls( $cyrusSrv, $object );

    # Obtention de la liste des sous-répertoires de la boite
    my $boxPattern = $boxPrefix.$boxName;
    my @boxStruct = $cyrusSrvConn->listmailbox( $boxPattern, '' );
    if( $cyrusSrvConn->error ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: erreur Cyrus a l'obtention des ACLs de la BAL : ".$cyrusSrvConn->error(), "W" );
        return 1;
    }

    $boxPattern =~ s/(@.*)$/\/*$1/;
    push( @boxStruct, $cyrusSrvConn->listmailbox( $boxPattern, '' ) );
    if( $cyrusSrvConn->error ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: erreur Cyrus a l'obtention des ACLs de la BAL : ".$cyrusSrvConn->error(), "W" );
        return 1;
    }

    # Gestion des ACL
    my $errors = 0;
    for( my $i=0; $i<=$#boxStruct; $i++ ) {
        while( my( $right, $oldUserList ) = each( %$oldAclList ) ) {
            my $newUserList = $newAclList->{$right};

            while( my( $userName, $value ) = each( %$oldUserList ) ) {
                if( !defined($newUserList) || !exists( $newUserList->{$userName} ) ) {
                    if( !$self->_imapSetMailboxAcl( $cyrusSrv, $boxStruct[$i][0], $userName, "none" ) ) {
                        $errors++;
                    }
                }
            }
        }

        my $anyoneRight = 0;
        while( my( $right, $newUserList ) = each( %$newAclList ) ) {
            my $oldUserList = $oldAclList->{$right};

            while( my( $userName, $value ) = each( %$newUserList ) ) {
                if( !defined($oldUserList) || !exists($oldUserList->{$userName} ) ) {
                    if( !$self->_imapSetMailboxAcl( $cyrusSrv, $boxStruct[$i][0], $userName, $right ) ) {
                        $errors++;
                    }
                }

                if( $userName eq "anyone" ) {
                    $anyoneRight = 1;
                }
            }
        }

        if( !$anyoneRight ) {
            if( !$self->_imapSetMailboxAcl( $cyrusSrv, $boxStruct[$i][0], "anyone", "post" ) ) {
                $errors++
            }
        }
    }

    return 1;
}


sub _deleteBox {
    my $self = shift;
    my( $cyrusSrv, $object ) = @_;

    if( !defined($cyrusSrv->{"imap_server_conn"}) ) {
        return 0;
    }
    my $cyrusSrvConn = $cyrusSrv->{"imap_server_conn"};

    if( !defined($object) ) {
        return 0;
    }

    my $boxName = $object->getMailboxName( "current" );
    my $boxPrefix = $object->getMailboxPrefix();

    if( !$self->_imapSetMailboxAcl( $cyrusSrv, $boxPrefix.$boxName, $cyrusSrv->{"imap_server_login"}, "admin" ) ) {
        return 0;
    }

    &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: suppression de la boite '".$boxName."', du serveur '".$cyrusSrv->{"imap_server_ip"}."'", "W" );
    $cyrusSrvConn->delete($boxPrefix.$boxName);

    if( $cyrusSrvConn->error() ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: erreur Cyrus a la suppression de la BAL : ".$cyrusSrvConn->error(), "W" );
        return 0;
    }

    return 1;
}


sub _createMailbox {
    my $self = shift;
    my( $cyrusSrv, $object ) = @_;
    my $returnCode = 1;

    if( !defined($cyrusSrv->{"imap_server_conn"}) ) {
        return 0;
    }
    my $cyrusSrvConn = $cyrusSrv->{"imap_server_conn"};

    if( !defined($object) ) {
        return 0;
    }

    my $currentBoxName = $object->getMailboxName( "current" );
    my $newBoxName = $object->getMailboxName( "new" );
    my $boxPrefix = $object->getMailboxPrefix();
    my $boxPartition = $object->getMailboxPartition();


    # Si la BAL 'current' est définie, existe et est différente de la BAL 'new', on renomme
    # Si la BAL 'current' est définie et n'existe pas, on crée la 'new'
    # Si la BAL 'current' n'est pas définie, on crée la 'new'
    my $action;
    if( defined($currentBoxName) && ($currentBoxName ne $newBoxName) && ($self->isMailboxExist( $cyrusSrv, undef, $boxPrefix, $currentBoxName) ) ) {
        $action = "rename";

        # On renomme la BAL
        if( !defined( $boxPartition ) ) {
            &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: renommage de la boite '".$currentBoxName."' vers '".$newBoxName."' sur la partition Cyrus par defaut, du serveur '".$cyrusSrv->{"imap_server_ip"}."'", "W" );
        }else {
            &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: renommage de la boite '".$currentBoxName."' vers '".$newBoxName."' sur la partition Cyrus '".$boxPartition."', du serveur '".$cyrusSrv->{"imap_server_ip"}."'", "W" );
        }

        $cyrusSrvConn->rename( $boxPrefix.$currentBoxName, $boxPrefix.$newBoxName, $boxPartition );
        $returnCode = 2;
    }else {
        $action = "create";

        # Création de la boîte
        if( !defined( $boxPartition ) ) {
            &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: creation de la boite '".$newBoxName."' sur la partition Cyrus par defaut, du serveur '".$cyrusSrv->{"imap_server_ip"}."'", "W" );
        }else {
            &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: creation de la boite '".$newBoxName."' sur la partition Cyrus '".$boxPartition."', du serveur '".$cyrusSrv->{"imap_server_ip"}."'", "W" );
        }

        $cyrusSrvConn->create( $boxPrefix.$newBoxName, $boxPartition );
        $returnCode = 1;
    }

    if( $cyrusSrvConn->error() ) {
        if( $action eq "rename" ) {
            &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: erreur Cyrus au renommage de la BAL : ".$cyrusSrvConn->error(), "W" );
        }elsif( $action eq "create" ) {
            &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: erreur Cyrus a la creation de la BAL : ".$cyrusSrvConn->error(), "W" );
        }else {
            &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: erreur Cyrus : ".$cyrusSrvConn->error(), "W" );
        }
            
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: verifiez que la version de Cyrus (2.2 min) et que l'option 'allowusermoves' est active !", "W" );
        return 0;
    }

    # On crée les répertoires par défaut si nécessaire
    if( !$self->_createMailboxDefaultFolders( $cyrusSrv, $object ) ) {
        return 0;
    }

    # On met à jour la boîte
    if( !$self->_updateMailbox( $cyrusSrv, $object ) ) {
        return 0;
    }


    return $returnCode;
}

sub _createMailboxDefaultFolders {
    my $self = shift;
    my( $cyrusSrv, $object ) = @_;

    if( !defined($cyrusSrv->{"imap_server_conn"}) ) {
        return 0;
    }
    my $cyrusSrvConn = $cyrusSrv->{"imap_server_conn"};
    
    if( !defined($object) ) {
        return 0;
    }

    my $boxName = $object->getMailboxName( "current" );
    my $boxPrefix = $object->getMailboxPrefix();
    my $boxPartition = $object->getMailboxPartition();
    my $boxDefaultFolders = $object->getMailboxDefaultFolders();

    if( !defined($boxDefaultFolders) ) {
        return 1;
    }

    my $folderError = 0;
    foreach my $folder ( @{$boxDefaultFolders} ) {
        $cyrusSrvConn->create( $boxPrefix.$folder, $boxPartition );

        if( $cyrusSrvConn->error() ) {
            &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: Erreur lors de la creation du repertoire '".$folder."'", "W" );
            $folderError++;
        }else {
            &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: Creation du repertoire '".$folder."' reussie", "W" );
        }
    }

    if( $folderError ) {
        return 0;
    }else {
        return 1;
    }
}


sub _updateMailbox {
    my $self = shift;
    my( $cyrusSrv, $object ) = @_;

    if( !defined($cyrusSrv->{"imap_server_conn"}) ) {
        return 0;
    }
    my $cyrusSrvConn = $cyrusSrv->{"imap_server_conn"};

    if( !defined($object) ) {
        return 0;
    }

    &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: MAJ de la boite '".$object->getMailboxName( "new" )."', du serveur '".$cyrusSrv->{"imap_server_ip"}."'", "W" );
    my $boxName = $object->getMailboxName( "new" );
    my $boxPrefix = $object->getMailboxPrefix();

    # Positionnement du quota
    if( !$self->_imapSetMailboxQuota( $cyrusSrv, $object ) ) {
        return 0;
    }

    # Uniquement si les liens de l'entité sont à mettre à jour
    if( $object->isLinks() ) {
        # Positionnement des ACL
        if( !$self->_imapSetMailboxAcls( $cyrusSrv, $object ) ) {
            return 0;
        }
    }
    
    return 1;
}


sub _imapSetMailboxAcl {
    my $self = shift;
    my( $cyrusSrv, $boxName, $boxRightUser, $boxRight ) = @_;

    if( !defined($cyrusSrv->{"imap_server_conn"}) ) {
        return 0;
    }
    my $cyrusSrvConn = $cyrusSrv->{"imap_server_conn"};


    # Préparation du droit à assigner
    my $definedRight = $self->{"rightDefinition"};
    if( !defined($definedRight->{$boxRight}) ) {
        return 0;
    }

    my $imapRight = $definedRight->{$boxRight};
    if( ($boxRightUser eq "anyone") && ($boxRight ne "none") ) {
        $imapRight .= $definedRight->{"post"};
    }

    $cyrusSrvConn->setaclmailbox( $boxName, $boxRightUser => $imapRight );
    if( $cyrusSrvConn->error() ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusEngine]: erreur Cyrus au positionnement des ACLs de la BAL : ".$cyrusSrvConn->error(), "W" );
        return 0;
    }

    return 1;
}
