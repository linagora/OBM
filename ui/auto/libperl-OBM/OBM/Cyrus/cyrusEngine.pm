#################################################################################
# Copyright (C) 2011-2014 Linagora
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option) any
# later version, provided you comply with the Additional Terms applicable for OBM
# software by Linagora pursuant to Section 7 of the GNU Affero General Public
# License, subsections (b), (c), and (e), pursuant to which you must notably (i)
# retain the displaying by the interactive user interfaces of the “OBM, Free
# Communication by Linagora” Logo with the “You are using the Open Source and
# free version of OBM developed and supported by Linagora. Contribute to OBM R&D
# by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
# links between OBM and obm.org, between Linagora and linagora.com, as well as
# between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
# from infringing Linagora intellectual property rights over its trademarks and
# commercial brands. Other Additional Terms apply, see
# <http://www.linagora.com/licenses/> for more details.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License and
# its applicable Additional Terms for OBM along with this program. If not, see
# <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
# version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
# applicable to the OBM software.
#################################################################################


package OBM::Cyrus::cyrusEngine;

$VERSION = '1.0';

use OBM::Log::log;
@ISA = ('OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use Cyrus::IMAP::Admin;


sub new {
    my $class = shift;

    my $self = bless { }, $class;

    require OBM::Parameters::common;
    if( !$OBM::Parameters::common::obmModules->{'mail'} ) {
        $self->_log( 'module OBM-MAIL désactivé, moteur non démarré', 2 );
        return '0 but true';
    }

    require OBM::Cyrus::cyrusServers;
    if( !($self->{'cyrusServers'} = OBM::Cyrus::cyrusServers->instance()) ) {
        $self->_log( 'initialisation du gestionnaire de serveur Cyrus impossible', 0 );
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
        writeAdmin => 'lrswicd',
        admin => 'lc',
        post => 'p',
        nodelete => '-x'
    };

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );

    $self->{'rightDefinition'} = undef;
    $self->{'cyrusServers'} = undef;
}


sub _doWork {
    my $self = shift;

    my $entity = $self->{'currentEntity'};
    if( !defined($entity) ) {
        return 1;
    }


    # La bal existe ?
    my %srvBalDesc;
    my $isExist = $self->isMailboxExist( \%srvBalDesc, $entity->getMailboxPrefix(), $entity->getMailboxName('new') );
    if( !defined($isExist) ) {
        $self->_log( 'probleme lors de l\'obtention des informations de la boite sur le serveur Cyrus', 1 );
        return 1;

    }elsif( $isExist && $entity->getDelete() ) {
        # On la supprime
        if( $self->_deleteBox() ) {
            $self->_log( 'echec lors de la suppression de la boite', 1 );
            return 1;
        }

    }elsif( !$isExist && $entity->getDelete() ) {
        $self->_log( 'boite deja supprimee', 3 );

    }elsif( $isExist && !$entity->getDelete() ) {
        # On met à jour
        if( $self->_updateMailbox() ) {
            $self->_log( 'echec lors de la MAJ de la boite', 1 );
            return 1;
        }

    }elsif( !$isExist && !$entity->getDelete() ) {
        # On la crée
        if( $self->_createMailbox() ) {
            $self->_log( 'echec lors de la creation/renommage de la boite', 1 );
            return 1;
        }

    }

    return 0;
}


sub update {
    my $self = shift;
    my( $entity ) = @_;


    if( !defined($entity) ) {
        $self->_log( 'entité non définie', 1 );
        return 1;
    }elsif( !ref($entity) ) {
        $self->_log( 'entité incorrecte', 1 );
        return 1;
    }
    $self->{'currentEntity'} = $entity;

    # If entity don't have Cyrus dependancy, we do nothing and it's not an error
    if( !$entity->isMailAvailable() || !$entity->isMailActive() ) {
        $self->_log( 'entité '.$entity->getDescription().' n\'a aucune représentation Cyrus', 4 );
        return 0;
    }


    # Get user BAL server Id
    my $mailserverId = $entity->getMailServerId();
    if( !defined($mailserverId) ) {
        $self->_log( 'serveur de courrier IMAP non defini - erreur', 1 );
        return 1;
    }

    # Get Cyrus server connection
    $self->{'currentCyrusSrv'} = $self->{'cyrusServers'}->getEntityCyrusServer( $entity );
    if( !defined($self->{'currentCyrusSrv'}) ) {
        $self->_log( 'serveur de courrier IMAP d\'identifiant \''.$entity->getMailServerId().'\' inconnu - Operation annulee !', 1 );
        return 1;
    }

    # Do stuff...
    my $returnCode = $self->_doWork();
    if( $returnCode ) {
        $self->_log( 'probleme de traitement de '.$entity->getDescription().' - Operation annulee !', 1 );
    }

    return $returnCode;
}


sub updateAcl {
    my $self = shift;
    my( $entity ) = @_;

    if( !defined($entity) ) {
        $self->_log( 'entité non définie', 1 );
        return 1;
    }elsif( !ref($entity) ) {
        $self->_log( 'entité incorrecte', 1 );
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
        $self->_log( 'serveur de courrier IMAP non defini - Operation annulee', 1 );
        return 0;
    }

    # Get Cyrus server connection
    $self->{'currentCyrusSrv'} = $self->{'cyrusServers'}->getEntityCyrusServer( $entity );
    if( !defined($self->{'currentCyrusSrv'}) ) {
        $self->_log( 'problème avec le serveur de courrier IMAP d\'identifiant \''.$entity->getMailServerId().'\' - Operation annulee !', 1 );
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

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getConn($entity->getDomainId());
    if( !defined($cyrusSrv) ) {
        return undef;
    }

    if( !defined($mailboxName) ) {
        return undef;
    }

    my @mailBox = $cyrusSrv->listmailbox( $mailboxName, $mailboxPrefix );
    if( $cyrusSrv->error ) {
        $self->_log( 'erreur Cyrus lors de la recherche de la BAL : '.$cyrusSrv->error(), 1 );
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
        $srvBalDesc->{'box_quota_used'} = $self->getMailboxQuotaUse( $mailboxPrefix, $mailboxName );
    }


    return 1;
}


sub getMailboxQuota {
    my $self = shift;
    my( $mailboxPrefix, $mailboxName ) = @_;
    my $mailBoxQuota = 0;

    my $entity = $self->{'currentEntity'};
    if( !defined($entity) ) {
        return undef;
    }

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getConn($entity->getDomainId());
    if( !defined($cyrusSrv) ) {
        return undef;
    }

    if( !defined($mailboxName) || !defined($mailboxPrefix) ) {
        return undef;
    }


    my @quotaDesc = $cyrusSrv->listquotaroot( $mailboxPrefix.$mailboxName );
    if( $cyrusSrv->error ) {
        $self->_log( 'erreur Cyrus a l\'obtention du quota maximum : '.$cyrusSrv->error(), 1 );
        return undef;
    }

    if( defined( $quotaDesc[2][1] ) ) {
        $mailBoxQuota = $quotaDesc[2][1];
    }

    return $mailBoxQuota;
}


sub getMailboxQuotaUse {
    my $self = shift;
    my( $mailboxPrefix, $mailboxName ) = @_;
    my $mailBoxQuotaUse = 0;

    my $entity = $self->{'currentEntity'};
    if( !defined($entity) ) {
        return undef;
    }

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getConn($entity->getDomainId());
    if( !defined($cyrusSrv) ) {
        return undef;
    }

    if( !defined($mailboxName) || !defined($mailboxPrefix) ) {
        return undef;
    }

    my @quotaDesc = $cyrusSrv->listquotaroot( $mailboxPrefix.$mailboxName );
    if( $cyrusSrv->error ) {
        $self->_log( 'erreur Cyrus a l\'obtention du quota utilise : '.$cyrusSrv->error(), 1 );
        return undef;
    }

    if( defined( $quotaDesc[2][1] ) ) {
        $mailBoxQuotaUse = int($quotaDesc[2][0]/1024);
    }

    return $mailBoxQuotaUse;
}


sub _imapSetMailboxQuota {
    my $self = shift;

    my $entity = $self->{'currentEntity'};
    if( !defined($entity) ) {
        return 1;
    }

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getConn($entity->getDomainId());
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
        $self->_log( 'erreur Cyrus au positionnement du quota utilisé : '.$cyrusSrv->error(), 1 );
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

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getConn($entity->getDomainId());
    if( !defined($cyrusSrv) || !defined($entity) ) {
        return 1;
    }


    my $boxName = $entity->getMailboxName( 'new' );
    my $boxPrefix = $entity->getMailboxPrefix();
    my $definedRight = $self->{'rightDefinition'};

    my %boxAclList = $cyrusSrv->listacl( $boxPrefix.$boxName );
    my $boxRight;
    if( $cyrusSrv->error ) {
        $self->_log( 'erreur Cyrus a l\'obtention des ACLs : '.$cyrusSrv->error(), 1 );
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

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getConn($entity->getDomainId());
    if( !defined($cyrusSrv) ) {
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
    my @boxStructs = $cyrusSrv->listmailbox( $boxPattern, '' );
    if( $cyrusSrv->error ) {
        $self->_log( 'erreur Cyrus a l\'obtention des ACLs de la BAL : '.$cyrusSrv->error(), 1 );
        return 1;
    }

    $boxPattern =~ s/(@.*)$/\/*$1/;
    push( @boxStructs, $cyrusSrv->listmailbox( $boxPattern, '' ) );
    if( $cyrusSrv->error ) {
        $self->_log( 'erreur Cyrus a l\'obtention de la structure de la BAL : '.$cyrusSrv->error(), 1 );
        return 1;
    }
    # Gestion des ACL
    my $errors = 0;
    foreach my $boxStruct(@boxStructs) {
        my $mailboxPath = $boxStruct[0];
        while( my( $right, $oldUserList ) = each( %$oldAclList ) ) {
            my $newUserList = $newAclList->{$right};

            while( my( $userName, $value ) = each( %$oldUserList ) ) {
                if( !defined($newUserList) || !exists( $newUserList->{$userName} ) ) {
                    if( !$self->_imapSetMailboxAcl( $mailboxPath, $userName, 'none' ) ) {
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
                    if( !$self->_imapSetMailboxAcl( $mailboxPath, $userName, $right ) ) {
                        $errors++;
                    }

                    # The first mailbox is the root one, we need to remove the 'x' right to prevent
                    # accidental deletion of mailshares by the user. This should only be done through OBM.
                    if (defined($entity->{'entityDesc'}) && defined($entity->{'entityDesc'}->{'mailshare_id'}) && $i == 0) {
                        $self->_imapSetMailboxAcl($mailboxPath, $userName, 'nodelete');
                    }
                }

                if( $userName eq 'anyone' ) {
                    $anyoneRight = 1;
                }
            }
        }

        if( !$anyoneRight ) {
            if( !$self->_imapSetMailboxAcl( $mailboxPath, 'anyone', 'post' ) ) {
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

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getConn($entity->getDomainId());
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

    $self->_log( 'suppression de la boite de '.$entity->getDescription(), 3 );
    for( my $i=0; $i<=$#boxStruct; $i++ ) {
        require OBM::Parameters::common;
        if( $self->_imapSetMailboxAcl( $boxStruct[$i][0], $OBM::Parameters::common::cyrusAdminLogin, 'admin' ) ) {
            $self->_log( 'erreur au positionnement des ACLs nécessaires à la suppression de '.$entity->getDescription(), 1 );
            return 1;
        }
    
        $self->_log( 'Suppression de \''.$boxStruct[$i][0].'\'', 4 );
        $cyrusSrv->delete($boxStruct[$i][0]);
    
        if( $cyrusSrv->error() ) {
            $self->_log( 'erreur Cyrus a la suppression de '.$entity->getDescription().' : '.$cyrusSrv->error(), 1 );
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

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getConn($entity->getDomainId());
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
            $self->_log( 'renommage de la boite \''.$currentBoxName.'\' vers \''.$newBoxName.'\' sur la partition Cyrus par defaut, du serveur '.$self->{'currentCyrusSrv'}->getDescription(), 3 );
        }else {
            $self->_log( 'renommage de la boite \''.$currentBoxName.'\' vers \''.$newBoxName.'\' sur la partition Cyrus \''.$boxPartition.'\', du serveur '.$self->{'currentCyrusSrv'}->getDescription(), 3 );
        }

        $cyrusSrv->rename( $boxPrefix.$currentBoxName, $boxPrefix.$newBoxName, $boxPartition );
        $returnCode = 0;
    }else {
        $action = 'create';

        if( !$entity->isMailActive() ) {
            $self->_log( 'l\'entité '.$entity->getDescription().' n\'a pas le droit mail, BAL non créée', 3 );
            return 0;
        }

        if( $entity->getArchive() ) {
            $self->_log( 'l\'entité '.$entity->getDescription().' est archivée, BAL non créée', 3 );
            return 0;
        }

        # Création de la boîte
        if( !defined( $boxPartition ) ) {
            $self->_log( 'creation de la boite \''.$newBoxName.'\' sur la partition Cyrus par defaut, du serveur '.$self->{'currentCyrusSrv'}->getDescription(), 3 );
        }else {
            $self->_log( 'creation de la boite \''.$newBoxName.'\' sur la partition Cyrus \''.$boxPartition.'\', du serveur '.$self->{'currentCyrusSrv'}->getDescription(), 3 );
        }

        $cyrusSrv->create( $boxPrefix.$newBoxName, $boxPartition );
        $returnCode = 0;
    }

    if( $cyrusSrv->error() ) {
        if ($cyrusSrv->error() =~ /invalid/i) {
            $self->_log( $cyrusSrv->error().': tentative de création de la partition', 3 );
            if( !$self->{'currentCyrusSrv'}->updateCyrusPartitions($entity->getDomainId()) ) {
                return $self->_createMailbox();
            }
        }

        if( $action eq 'rename' ) {
            $self->_log( 'erreur Cyrus au renommage de la BAL : '.$cyrusSrv->error(), 1 );
        }elsif( $action eq 'create' ) {
            $self->_log( 'erreur Cyrus a la creation de la BAL : '.$cyrusSrv->error(), 1 );
        }else {
            $self->_log( 'erreur Cyrus : '.$cyrusSrv->error(), 1 );
        }
            
        return 1;
    }

    # On crée les répertoires par défaut si nécessaire
    if( ($action eq 'create') && $self->_createMailboxDefaultFolders() ) {
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

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getConn($entity->getDomainId());
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
            $self->_log( 'erreur lors de la creation du repertoire \''.$folder.'\'', 1 );
            $folderError++;
        }else {
            $self->_log( 'creation du repertoire \''.$folder.'\' reussie', 3 );
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


    $self->_log( 'MAJ de la boite de '.$entity->getDescription(), 3 );

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

    my $cyrusSrvConn = $self->{'currentCyrusSrv'}->getConn($entity->getDomainId());
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

    $self->_log( 'SETACLMAILBOX ' . $boxName . ' ' . $boxRightUser . ' ' . $imapRight, 4 );

    $cyrusSrvConn->setaclmailbox( $boxName, $boxRightUser => $imapRight );
    if( $cyrusSrvConn->error() ) {
        $self->_log( 'erreur Cyrus au positionnement des ACLs de la BAL \''.$boxName.'\' : '.$cyrusSrvConn->error(), 1 );
        return 1;
    }

    return 0;
}
