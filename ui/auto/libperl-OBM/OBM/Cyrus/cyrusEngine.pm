package OBM::Cyrus::cyrusEngine;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


use OBM::Tools::commonMethods qw(
        _log
        dump
        );
use OBM::Ldap::utils qw(
        _modifyAttr
        );
use Cyrus::IMAP::Admin;


sub new {
    my $class = shift;

    my $self = bless { }, $class;

    require OBM::Parameters::common;
    if( !$OBM::Parameters::common::obmModules->{'mail'} ) {
        $self->_log( 'module OBM-MAIL désactivé, moteur non démarré', 3 );
        return '0 but true';
    }

    require OBM::Cyrus::cyrusServers;
    if( !($self->{'cyrusServers'} = OBM::Cyrus::cyrusServers->instance()) ) {
        $self->_log( 'initialisation du gestionnaire de serveur Cyrus impossible', 3 );
        return undef;
    }


    # Definition des droits
    $self->{'rightDefinition'} = {
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

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );

    $self->{'rightDefinition'} = undef;
    $self->{'cyrusServers'} = undef;
}


sub _doWork {
    my $self = shift;
    my $returnCode = 1;

    my $entity = $self->{'currentEntity'};
    if( !defined($entity) ) {
        return 1;
    }


    # La bal existe ?
    my %srvBalDesc;
    my $isExist = $self->isMailboxExist( \%srvBalDesc, $entity->getMailboxPrefix(), $entity->getMailboxName('new') );
    if( !defined($isExist) ) {
        $self->_log( 'probleme lors de l\'obtention des informations de la boite sur le serveur Cyrus', 2 );
        return 1;

    }elsif( $isExist && $entity->getDelete() ) {
        # On la supprime
        $returnCode = $self->_deleteBox();
        if( $returnCode ) {
            $self->_log( 'echec lors de la suppression de la boite', 2 );
            return 1;
        }

    }elsif( $isExist && !$entity->getDelete() ) {
        # On met à jour
        $returnCode = $self->_updateMailbox();
        if( $returnCode ) {
            $self->_log( 'echec lors de la MAJ de la boite', 2 );
            return 1;
        }

    }elsif( !$isExist && !$entity->getDelete() ) {
        # On la crée
        $returnCode = $self->_createMailbox();
        if( $returnCode ) {
            $self->_log( 'echec lors de la creation/renommage de la boite', 2 );
            return 1;
        }

    }

    return $returnCode;
}


sub update {
    my $self = shift;
    my( $entity ) = @_;


    if( !defined($entity) ) {
        $self->_log( 'entité non définie', 3 );
        return 1;
    }elsif( !ref($entity) ) {
        $self->_log( 'entité incorrecte', 3 );
        return 1;
    }
    $self->{'currentEntity'} = $entity;

    # If entity don't have Cyrus dependancy, we do nothing and it's not an error
    if( !$entity->isMailAvailable() ) {
        $self->_log( 'entité '.$entity->getDescription().' n\'a aucune représentation Cyrus', 3 );
        return 0;
    }


    # Get user BAL server object
    my $mailserverId = $entity->getMailServerId();
    if( !defined($mailserverId) && $entity->isMailActive() && !$entity->getArchive() ) {
        $self->_log( 'serveur de courrier IMAP non defini et droit mail actif - erreur', 2 );
        return 1;
    }elsif( !defined($mailserverId) && (!$entity->isMailActive() || $entity->getArchive()) ) {
        $self->_log( 'serveur de courrier IMAP non defini et droit mail inactif - succés', 2 );
        return 0;
    }elsif( !defined($mailserverId) ) {
        $self->_log( 'serveur de courrier IMAP non defini - erreur', 2 );
        return 1;
    }

    # Get Cyrus server connection
    $self->{'currentCyrusSrv'} = $self->{'cyrusServers'}->getEntityCyrusServer( $entity );
    if( !defined($self->{'currentCyrusSrv'}) ) {
        $self->_log( 'serveur de courrier IMAP d\'identifiant \''.$entity->getMailServerId().'\' inconnu - Operation annulee !', 2 );
        return 1;
    }

    # Do stuff...
    my $returnCode = $self->_doWork();
    if( $returnCode ) {
        $self->_log( 'probleme de traitement de '.$entity->getDescription().' - Operation annulee !', 2 );
    }

    return $returnCode;
}


sub updateAcl {
    my $self = shift;
    my( $entity ) = @_;

    if( !defined($entity) ) {
        $self->_log( 'entité non définie', 3 );
        return 1;
    }elsif( !ref($entity) ) {
        $self->_log( 'entité incorrecte', 3 );
        return 1;
    }
    $self->{'currentEntity'} = $entity;

    if( !$entity->isMailAvailable() ) {
        $self->_log( 'entité '.$entity->getDescription().' n\'a aucune représentation Cyrus', 3 );
        return 0;
    }


    # Récupération de la description du serveur de la boîte à traiter
    my $mailserverId = $entity->getMailServerId();
    if( !defined($mailserverId) ) {
        $self->_log( 'serveur de courrier IMAP non defini - Operation annulee', 2 );
        return 0;
    }

    # Get Cyrus server connection
    $self->{'currentCyrusSrv'} = $self->{'cyrusServers'}->getEntityCyrusServer( $entity );
    if( !defined($self->{'currentCyrusSrv'}) ) {
        $self->_log( 'problème avec le serveur de courrier IMAP d\'identifiant \''.$entity->getMailServerId().'\' - Operation annulee !', 2 );
        return 1;
    }


    return $self->_imapSetMailboxAcls();
}


sub isMailboxExist {
    my $self = shift;
    my( $srvBalDesc, $mailboxPrefix, $mailboxName ) = @_;

    my $entity = $self->{'currentEntity'};
    if( !defined($entity) ) {
        return 1;
    }

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getCyrusConn($entity->getDomainId());
    if( !defined($cyrusSrv) ) {
        return undef;
    }

    if( !defined($mailboxName) ) {
        return undef;
    }

    my @mailBox = $cyrusSrv->listmailbox( $mailboxName, $mailboxPrefix );
    if( $cyrusSrv->error ) {
        $self->_log( 'erreur Cyrus lors de la recherche de la BAL : '.$cyrusSrv->error(), 2 );
        return undef;
    }

    if( $#mailBox < 0 ) {
        return 0;
    }elsif( $#mailBox > 0 ) {
        return undef;
    }


    # Si la boîte existe on charge ses caractéristiques
    if( defined($srvBalDesc) ) {
        $srvBalDesc->{'box_name'} = $mailBox[0][0];
        $srvBalDesc->{'box_login'} = $mailBox[0][0];
        $srvBalDesc->{'box_login'} =~ s/^$mailboxPrefix//;
        $srvBalDesc->{'box_quota'} = $self->getMailboxQuota( $mailboxPrefix, $mailboxName );
    }


    return 1;
}


sub getMailboxQuota {
    my $self = shift;
    my( $mailboxPrefix, $mailboxName ) = @_;
    my $mailBoxQuota = 0;

    my $entity = $self->{'currentEntity'};
    if( !defined($entity) ) {
        return 1;
    }

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getCyrusConn($entity->getDomainId());
    if( !defined($cyrusSrv) ) {
        return undef;
    }

    if( !defined($mailboxName) || !defined($mailboxPrefix) ) {
        return undef;
    }


    my @quotaDesc = $cyrusSrv->listquotaroot( $mailboxPrefix.$mailboxName );
    if( $cyrusSrv->error ) {
        $self->_log( 'erreur Cyrus a l\'obtention du quota maximum : '.$cyrusSrv->error(), 3 );
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
        $self->_log( 'erreur Cyrus a l\'obtention du quota utilise : '.$cyrusSrvConn->error(), 2 );
        return undef;
    }

    if( defined( $quotaDesc[2][1] ) ) {
        $mailBoxQuotaUse = $quotaDesc[2][0];
    }

    return $mailBoxQuotaUse;
}


sub _imapSetMailboxQuota {
    my $self = shift;

    my $entity = $self->{'currentEntity'};
    if( !defined($entity) ) {
        return 1;
    }

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getCyrusConn($entity->getDomainId());
    if( !defined($cyrusSrv) ) {
        return 1;
    }

    $self->_log( 'mise à jour du quota de '.$entity->getDescription(), 3 );

    my $boxName = $entity->getMailboxName( 'new' );
    my $boxPrefix = $entity->getMailboxPrefix();
    my $boxQuota = $entity->getMailboxQuota();

    if( !$boxQuota ) {
        $cyrusSrv->setquota( $boxPrefix.$boxName );
    }else {
        $cyrusSrv->setquota( $boxPrefix.$boxName, 'STORAGE', $boxQuota );
    }

    if( $cyrusSrv->error ) {
        $self->_log( 'erreur Cyrus au positionnement du quota utilisé : '.$cyrusSrv->error(), 3 );
        return 1;
    }

    return 0;
}


sub _imapGetMailboxAcls {
    my $self = shift;

    my $entity = $self->{'currentEntity'};
    if( !defined($entity) ) {
        return 1;
    }

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getCyrusConn($entity->getDomainId());
    if( !defined($cyrusSrv) || !defined($entity) ) {
        return 1;
    }


    my $boxName = $entity->getMailboxName( 'new' );
    my $boxPrefix = $entity->getMailboxPrefix();
    my $definedRight = $self->{'rightDefinition'};

    my %boxAclList = $cyrusSrv->listacl( $boxPrefix.$boxName );
    my $boxRight;
    if( $cyrusSrv->error ) {
        $self->_log( 'erreur Cyrus a l\'obtention des ACLs : '.$cyrusSrv->error(), 3 );
        return undef;

    }else {
        while( my( $user, $right ) = each( %boxAclList ) ) {
            # le droit POST est gere de façon transparente
            $right =~ s/$definedRight->{'post'}//g;

            $right = $self->_checkAclRight( $right );

            if( $user ne 'anyone' ) {
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

    my $entity = $self->{'currentEntity'};
    if( !defined($entity) ) {
        return 1;
    }

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getCyrusConn($entity->getDomainId());
    if( !defined($cyrusSrv) || !defined($entity) ) {
        return 1;
    }


    # Si l'objet n'est pas chargé avec les liens, on ne met pas à jour les ACLs
    if( !$entity->getUpdateLinks() ) {
        return 0;
    }

    $self->_log( 'mise à jour des ACLs de '.$entity->getDescription(), 3 );

    my $boxName = $entity->getMailboxName( 'new' );
    my $boxPrefix = $entity->getMailboxPrefix();

    # Obtention des ACL depuis la BD (new) et depuis le serveur (old)
    my $newAclList = $entity->getMailboxAcl();
    my $oldAclList = $self->_imapGetMailboxAcls();

    # Obtention de la liste des sous-répertoires de la boite
    my $boxPattern = $boxPrefix.$boxName;
    my @boxStruct = $cyrusSrv->listmailbox( $boxPattern, '' );
    if( $cyrusSrv->error ) {
        $self->_log( 'erreur Cyrus a l\'obtention des ACLs de la BAL : '.$cyrusSrv->error(), 3 );
        return 1;
    }

    $boxPattern =~ s/(@.*)$/\/*$1/;
    push( @boxStruct, $cyrusSrv->listmailbox( $boxPattern, '' ) );
    if( $cyrusSrv->error ) {
        $self->_log( 'erreur Cyrus a l\'obtention des ACLs de la BAL : '.$cyrusSrv->error(), 3 );
        return 1;
    }

    # Gestion des ACL
    my $errors = 0;
    for( my $i=0; $i<=$#boxStruct; $i++ ) {
        while( my( $right, $oldUserList ) = each( %$oldAclList ) ) {
            my $newUserList = $newAclList->{$right};

            while( my( $userName, $value ) = each( %$oldUserList ) ) {
                if( !defined($newUserList) || !exists( $newUserList->{$userName} ) ) {
                    if( !$self->_imapSetMailboxAcl( $boxStruct[$i][0], $userName, 'none' ) ) {
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
                    if( !$self->_imapSetMailboxAcl( $boxStruct[$i][0], $userName, $right ) ) {
                        $errors++;
                    }
                }

                if( $userName eq 'anyone' ) {
                    $anyoneRight = 1;
                }
            }
        }

        if( !$anyoneRight ) {
            if( !$self->_imapSetMailboxAcl( $boxStruct[$i][0], 'anyone', 'post' ) ) {
                $errors++
            }
        }
    }

    return 0;
}


sub _deleteBox {
    my $self = shift;

    my $entity = $self->{'currentEntity'};
    if( !defined($entity) ) {
        return 1;
    }

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getCyrusConn($entity->getDomainId());
    if( !defined($cyrusSrv) || !defined($entity) ) {
        return 1;
    }


    my $boxName = $entity->getMailboxName( 'current' );
    my $boxPrefix = $entity->getMailboxPrefix();
    my @boxStruct = $cyrusSrv->listmailbox( $boxPrefix.$boxName, '' );

    my $boxSubfolders = undef;
    if( ref($entity) eq 'OBM::Entities::obmMailshare' ) {
        if( $boxName =~ /^(.*)(@.*)$/ ) {
            $boxSubfolders = $1.'/*'.$2;
        }
    }

    if( defined($boxSubfolders) ) {
        push( @boxStruct, $cyrusSrv->listmailbox( $boxSubfolders, '' ) );
    }

    $self->_log( 'suppression de la boite de '.$entity->getDescription(), 2 );
    for( my $i=0; $i<=$#boxStruct; $i++ ) {
        require OBM::Parameters::common;
        if( $self->_imapSetMailboxAcl( $boxStruct[$i][0], $OBM::Parameters::common::cyrusAdminLogin, 'admin' ) ) {
            $self->_log( 'erreur au positionnement des ACLs nécessaires à la suppression de '.$entity->getDescription(), 0 );
            return 1;
        }
    
        $self->_log( 'Suppression de \''.$boxStruct[$i][0].'\'', 4 );
        $cyrusSrv->delete($boxStruct[$i][0]);
    
        if( $cyrusSrv->error() ) {
            $self->_log( 'erreur Cyrus a la suppression de '.$entity->getDescription().' : '.$cyrusSrv->error(), 0 );
            return 1;
        }
    }

    return 0;
}


sub _createMailbox {
    my $self = shift;
    my $returnCode = 0;

    my $entity = $self->{'currentEntity'};
    if( !defined($entity) ) {
        return 1;
    }

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getCyrusConn($entity->getDomainId());
    if( !defined($cyrusSrv) || !defined($entity) ) {
        return 1;
    }


    my $currentBoxName = $entity->getMailboxName( 'current' );
    my $newBoxName = $entity->getMailboxName( 'new' );
    my $boxPrefix = $entity->getMailboxPrefix();
    my $boxPartition = $entity->getMailboxPartition();


    # Si la BAL 'current' est définie, existe et est différente de la BAL 'new', on renomme
    # Si la BAL 'current' est définie et n'existe pas, on crée la 'new'
    # Si la BAL 'current' n'est pas définie, on crée la 'new'
    my $action;
    if( defined($currentBoxName) && ($currentBoxName ne $newBoxName) && ($self->isMailboxExist( undef, $boxPrefix, $currentBoxName) ) ) {
        $action = 'rename';

        # On renomme la BAL
        if( !defined( $boxPartition ) ) {
            $self->_log( 'renommage de la boite \''.$currentBoxName.'\' vers \''.$newBoxName.'\' sur la partition Cyrus par defaut, du serveur '.$self->{'currentCyrusSrv'}->getDescription(), 2 );
        }else {
            $self->_log( 'renommage de la boite \''.$currentBoxName.'\' vers \''.$newBoxName.'\' sur la partition Cyrus \''.$boxPartition.'\', du serveur '.$self->{'currentCyrusSrv'}->getDescription(), 2 );
        }

        $cyrusSrv->rename( $boxPrefix.$currentBoxName, $boxPrefix.$newBoxName, $boxPartition );
        $returnCode = 0;
    }else {
        $action = 'create';

        if( !$entity->isMailActive() ) {
            $self->_log( 'l\'entité '.$entity->getDescription().' n\'a pas le droit mail, BAL non créée', 2 );
            return 0;
        }

        if( $entity->getArchive() ) {
            $self->_log( 'l\'entité '.$entity->getDescription().' est archivée, BAL non créée', 2 );
            return 0;
        }

        # Création de la boîte
        if( !defined( $boxPartition ) ) {
            $self->_log( 'creation de la boite \''.$newBoxName.'\' sur la partition Cyrus par defaut, du serveur '.$self->{'currentCyrusSrv'}->getDescription(), 2 );
        }else {
            $self->_log( 'creation de la boite \''.$newBoxName.'\' sur la partition Cyrus \''.$boxPartition.'\', du serveur '.$self->{'currentCyrusSrv'}->getDescription(), 2 );
        }

        $cyrusSrv->create( $boxPrefix.$newBoxName, $boxPartition );
        $returnCode = 0;
    }

    if( $cyrusSrv->error() ) {
        if( ($cyrusSrv->error() =~ /invalid/i) && ($cyrusSrv->error() =~ /partition/i) ) {
            if( !$self->{'currentCyrusSrv'}->updateCyrusPartitions($entity->getDomainId()) ) {
                return $self->_createMailbox();
            }
        }

        if( $action eq 'rename' ) {
            $self->_log( 'erreur Cyrus au renommage de la BAL : '.$cyrusSrv->error(), 2 );
        }elsif( $action eq 'create' ) {
            $self->_log( 'erreur Cyrus a la creation de la BAL : '.$cyrusSrv->error(), 2 );
        }else {
            $self->_log( 'erreur Cyrus : '.$cyrusSrv->error(), 2 );
        }
            
        return 1;
    }

    # On crée les répertoires par défaut si nécessaire
    if( $self->_createMailboxDefaultFolders() ) {
        return 1;
    }

    # On met à jour la boîte
    if( $self->_updateMailbox() ) {
        return 1;
    }

    return $returnCode;
}


sub _createMailboxDefaultFolders {
    my $self = shift;

    my $entity = $self->{'currentEntity'};
    if( !defined($entity) ) {
        return 1;
    }

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getCyrusConn($entity->getDomainId());
    if( !defined($cyrusSrv) || !defined($entity) ) {
        return 1;
    }


    my $boxName = $entity->getMailboxName( 'current' );
    my $boxPrefix = $entity->getMailboxPrefix();
    my $boxPartition = $entity->getMailboxPartition();
    my $boxDefaultFolders = $entity->getMailboxDefaultFolders();

    if( !defined($boxDefaultFolders) ) {
        return 0;
    }

    my $folderError = 0;
    foreach my $folder ( @{$boxDefaultFolders} ) {
        $cyrusSrv->create( $boxPrefix.$folder, $boxPartition );

        if( $cyrusSrv->error() ) {
            $self->_log( 'erreur lors de la creation du repertoire \''.$folder.'\'', 2 );
            $folderError++;
        }else {
            $self->_log( 'creation du repertoire \''.$folder.'\' reussie', 2 );
        }
    }

    if( $folderError ) {
        return 1;
    }else {
        return 0;
    }
}


sub _updateMailbox {
    my $self = shift;

    my $entity = $self->{'currentEntity'};
    if( !defined($entity) ) {
        return 1;
    }


    $self->_log( 'MAJ de la boite de '.$entity->getDescription(), 2 );

    # Uniquement si l'entité doit être mise à jour
    if( $entity->getUpdateEntity() ) {
        # Positionnement du quota
        if( $self->_imapSetMailboxQuota() ) {
            return 1;
        }
    }

    # Uniquement si les liens de l'entité sont à mettre à jour
    if( $entity->getUpdateLinks() ) {
        # Positionnement des ACL
        if( $self->_imapSetMailboxAcls() ) {
            return 1;
        }
    }
    
    return 0;
}


sub _imapSetMailboxAcl {
    my $self = shift;
    my( $boxName, $boxRightUser, $boxRight ) = @_;

    my $entity = $self->{'currentEntity'};
    if( !defined($entity) ) {
        return 1;
    }

    my $cyrusSrvConn = $self->{'currentCyrusSrv'}->getCyrusConn($entity->getDomainId());
    if( !defined($cyrusSrvConn) ) {
        return 1;
    }


    # Préparation du droit à assigner
    my $definedRight = $self->{'rightDefinition'};
    if( !defined($definedRight->{$boxRight}) ) {
        return 1;
    }

    my $imapRight = $definedRight->{$boxRight};
    if( ($boxRightUser eq 'anyone') && ($boxRight ne 'none') ) {
        $imapRight .= $definedRight->{'post'};
    }

    $cyrusSrvConn->setaclmailbox( $boxName, $boxRightUser => $imapRight );
    if( $cyrusSrvConn->error() ) {
        $self->_log( 'erreur Cyrus au positionnement des ACLs de la BAL : '.$cyrusSrvConn->error(), 3 );
        return 1;
    }

    return 0;
}
