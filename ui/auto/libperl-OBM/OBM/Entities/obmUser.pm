package OBM::Entities::obmUser;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Entities::commonEntities qw(getType setDelete getDelete getArchive isLinks getEntityId makeEntityEmail getMailboxDefaultFolders);
use OBM::Parameters::common;
require OBM::Parameters::ldapConf;
require OBM::Ldap::utils;
require OBM::passwd;
require OBM::toolBox;
require OBM::dbUtils;
require OBM::Samba::utils;
use URI::Escape;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);


sub new {
    my $self = shift;
    my( $links, $deleted, $userId ) = @_;


    my %obmUserAttr = (
        type => undef,
        entityRightType => undef,
        links => undef,
        toDelete => undef,
        archive => undef,
        sieve => undef,
        objectId => undef,
        domainId => undef,
        userDbDesc => undef,        # Pure description BD
        properties => undef,        # Propriétés calculées
        userLinks => undef,         # Les relations avec d'autres entités
        objectclass => undef,
        dnPrefix => undef,
        newDnValue => undef,
        currentDnValue => undef
    );


    if( !defined($links) || !defined($deleted) || !defined($userId) ) {
        croak( "Usage: PACKAGE->new(LINKS, DELETED, USERID)" );

    }elsif( $userId !~ /^\d+$/ ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: identifiant d'utilisateur incorrect", "W" );
        return undef;

    }else {
        $obmUserAttr{"objectId"} = $userId;
    }


    $obmUserAttr{"links"} = $links;
    $obmUserAttr{"toDelete"} = $deleted;

    $obmUserAttr{"type"} = $OBM::Parameters::ldapConf::POSIXUSERS;
    $obmUserAttr{"entityRightType"} = "MailBox";
    $obmUserAttr{"archive"} = 0;
    $obmUserAttr{"sieve"} = 1;

    # Définition de la représentation LDAP de ce type
    $obmUserAttr{objectclass} = $OBM::Parameters::ldapConf::attributeDef->{$obmUserAttr{"type"}}->{objectclass};
    $obmUserAttr{dnPrefix} = $OBM::Parameters::ldapConf::attributeDef->{$obmUserAttr{"type"}}->{dn_prefix};
    $obmUserAttr{newDnValue} = $OBM::Parameters::ldapConf::attributeDef->{$obmUserAttr{"type"}}->{dn_value};
    $obmUserAttr{currentDnValue} = "current_".$OBM::Parameters::ldapConf::attributeDef->{$obmUserAttr{"type"}}->{dn_value};

    bless( \%obmUserAttr, $self );
}


sub getEntity {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;

    my $userId = $self->{"objectId"};
    if( !defined($userId) ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: aucun identifiant d'utilisateur definit", "W" );
        return 0;
    }


    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: connecteur a la base de donnee invalide", "W" );
        return 0;
    }

    if( !defined($domainDesc->{"domain_id"}) || ($domainDesc->{"domain_id"} !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: description de domaine OBM incorrecte", "W" );
        return 0;

    }else {
        # On positionne l'identifiant du domaine de l'entité
        $self->{"domainId"} = $domainDesc->{"domain_id"};
    }


    my $userObmTable = "UserObm";
    my $mailServerTable = "MailServer";
    if( $self->getDelete() ) {
        $userObmTable = "P_".$userObmTable;
        $mailServerTable = "P_".$mailServerTable;
    }


    my $query = "SELECT COUNT(*) FROM ".$userObmTable." LEFT JOIN ".$mailServerTable." ON userobm_mail_server_id=mailserver_id WHERE userobm_id=".$userId;

    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    my( $numRows ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( $numRows == 0 ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: pas d'utilisateur d'identifiant : ".$userId, "W" );
        return 0;
    }elsif( $numRows > 1 ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: plusieurs utilisateurs d'identifiant : ".$userId." ???", "W" );
        return 0;
    }


    # Obtention de la description BD de l'utilisateur
    $query = "SELECT * FROM ".$userObmTable." WHERE userobm_id=".$userId;

    # On exécute la requête
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    # On range les résultats dans la structure de données des résultats
    my $dbUserDesc = $queryResult->fetchrow_hashref();
    $queryResult->finish();

    # On stocke la description BD utile pour la MAJ des tables
    $self->{"userDbDesc"} = $dbUserDesc;


    # La requête à exécuter - obtention des informations sur l'utilisateur
    if( $self->getDelete() ) {
        $query = "SELECT i.mailserver_host_id
                    FROM ".$userObmTable." j
                    LEFT JOIN ".$mailServerTable." i ON j.userobm_mail_server_id=i.mailserver_id
                    WHERE j.userobm_id=".$userId;
    }else {
        $query = "SELECT i.mailserver_host_id, k.userobm_login as current_userobm_login
                    FROM ".$userObmTable." j
                    LEFT JOIN ".$mailServerTable." i ON j.userobm_mail_server_id=i.mailserver_id
                    LEFT JOIN P_".$userObmTable." k ON k.userobm_id=j.userobm_id
                    WHERE j.userobm_id=".$userId;
    }

    # On exécute la requête
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    my $dbUserMoreDesc = $queryResult->fetchrow_hashref();
    $queryResult->finish();

    # Positionnement du flag archive
    $self->{archive} = $dbUserDesc->{userobm_archive};

    # Action effectuée
    if( $self->getDelete() ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: chargement de l'utilisateur supprime  : ".$self->getEntityDescription(), "W" );

    }elsif( $dbUserDesc->{userobm_archive} ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: chargement de l'utilisateur archive : ".$self->getEntityDescription(), "W" );

    }else {
        &OBM::toolBox::write_log( "[Entities::obmUser]: chargement de l'utilisateur : ".$self->getEntityDescription(), "W" );

    }


    # On range les résultats dans la structure de données des résultats
    $self->{"properties"}->{userobm_domain} = $domainDesc->{domain_label};

    if( $self->getDelete() ) {
        $self->{"properties"}->{current_userobm_login} = $dbUserDesc->{userobm_login};
    }elsif( defined($dbUserMoreDesc->{current_userobm_login}) ) {
        $self->{"properties"}->{current_userobm_login} = $dbUserMoreDesc->{current_userobm_login};
    }

    $self->{"properties"}->{userobm_crypt_passwd} = &OBM::passwd::convertPasswd( $dbUserDesc->{userobm_password_type}, $dbUserDesc->{userobm_password} );

    $self->{"properties"}->{userobm_uid} = $dbUserDesc->{userobm_uid};

    # Le nom complet de l'utilisateur
    $self->{"properties"}->{userobm_full_name} = $dbUserDesc->{userobm_firstname};
    if( $self->{"properties"}->{userobm_full_name} && $dbUserDesc->{userobm_lastname} ) {
        $self->{"properties"}->{userobm_full_name} .= " ".$dbUserDesc->{userobm_lastname};
    }elsif( $dbUserDesc->{userobm_lastname} ) {
        $self->{"properties"}->{userobm_full_name} = $dbUserDesc->{userobm_lastname}
    }

    # Gestion du shell
    $self->{"properties"}->{userobm_shell} = "/bin/bash";

    # Gestion du répertoire personnel
    $self->{"properties"}->{userobm_homedir} = $baseHomeDir."/".$dbUserDesc->{userobm_login};

    # Gestion de la visibilité
    if( $dbUserDesc->{userobm_hidden} ) {
        $self->{"properties"}->{userobm_hidden} = "TRUE";
    }else {
        $self->{"properties"}->{userobm_hidden} = "FALSE";
    }


    # gestion de l'adresse
    if( defined($dbUserDesc->{userobm_address1}) && ($dbUserDesc->{userobm_address1} ne "") ) {
        $self->{"properties"}->{userobm_address} = $dbUserDesc->{userobm_address1};
    }
        
    if( defined($dbUserDesc->{userobm_address2}) && ($dbUserDesc->{userobm_address2} ne "") ) {
        $self->{"properties"}->{userobm_address} .= "\r\n".$dbUserDesc->{userobm_address2};
    }
        
    if( defined($dbUserDesc->{userobm_address3}) && ($dbUserDesc->{userobm_address3} ne "") ) {
        $self->{"properties"}->{userobm_address} .= "\r\n".$dbUserDesc->{userobm_address3};
    }


    # Gestion du téléphone
    if( defined($dbUserDesc->{userobm_phone}) && ($dbUserDesc->{userobm_phone} ne "") ) {
        push( @{$self->{"properties"}->{userobm_phone}}, $dbUserDesc->{userobm_phone} );
    }

    if( defined($dbUserDesc->{userobm_phone2}) && ($dbUserDesc->{userobm_phone2} ne "") ) {
        push( @{$self->{"properties"}->{userobm_phone}}, $dbUserDesc->{userobm_phone2} );
    }

    # Gestion du fax
    if( defined($dbUserDesc->{userobm_fax}) && ($dbUserDesc->{userobm_fax} ne "") ) {
        push( @{$self->{"properties"}->{userobm_fax}}, $dbUserDesc->{userobm_fax} );
    }

    if( defined($dbUserDesc->{userobm_fax2}) && ($dbUserDesc->{userobm_fax2} ne "") ) {
        push( @{$self->{"properties"}->{userobm_fax}}, $dbUserDesc->{userobm_fax2} );
    }

    # Gestion du droit de messagerie de l'utilisateur
    $self->{"properties"}->{userobm_mail_perms} = $dbUserDesc->{userobm_mail_perms};

    my $localServerIp;
    SWITCH: {
        if( !$self->{"properties"}->{userobm_mail_perms} ) {
            last SWITCH;
        }

        if( $dbUserDesc->{userobm_domain_id} == 0 ) {
            &OBM::toolBox::write_log( "[Entities::obmUser]: droit mail de l'utilisateur : ".$self->getEntityDescription()." - annule, pas de droit mail dans le domaine 'metadomain'", "W" );
            $self->{"properties"}->{userobm_mail_perms} = 0;
            last SWITCH;
        }

        $localServerIp = $self->getHostIpById( $dbHandler, $dbUserMoreDesc->{mailserver_host_id} );
        if( !defined($localServerIp) ) {
            &OBM::toolBox::write_log( "[Entities::obmUser]: droit mail de l'utilisateur '".$dbUserDesc->{userobm_login}."', domaine '".$domainDesc->{domain_label}."' - annule, serveur inconnu !", "W" );
            $self->{"properties"}->{userobm_mail_perms} = 0;
            last SWITCH;
        }

        # Gestion des adresses de l'utilisateur
        my $return = $self->makeEntityEmail( $dbUserDesc->{userobm_email}, $domainDesc->{domain_name}, $domainDesc->{domain_alias} );
        if( $return == 0 ) {
            &OBM::toolBox::write_log( "[Entities::obmUser]: droit mail de l'utilisateur : ".$self->getEntityDescription()." - annule, pas d'adresses mails valides", "W" );
            $self->{"properties"}->{userobm_mail_perms} = 0;
            last SWITCH;
        }

        $self->{"properties"}->{userobm_mail_perms} = 1;
    }

    # Gestion du droit de messagerie
    if( $self->{"properties"}->{userobm_mail_perms} ) {
        # Limite la messagerie aux domaines locaux
        if( !$dbUserDesc->{userobm_mail_ext_perms} ) {
            $self->{"properties"}->{userobm_mailLocalOnly} = "local_only";
        }

        # Gestion de la BAL destination
        #   valeur dans LDAP
        $self->{"properties"}->{userobm_mailbox_ldap_name} = $dbUserDesc->{userobm_login}."@".$domainDesc->{domain_name};

        # Nom de la BAL Cyrus
        # Son nouveau nom - qui peut si on ne le change pas et que la
        # BAL existe déjà, ou qu'on supprime l'entrée, être le même que
        # l'ancien
        $self->{"properties"}->{new_userobm_mailbox_cyrus_name} = $dbUserDesc->{userobm_login};
        if( !$singleNameSpace ) {
            $self->{"properties"}->{new_userobm_mailbox_cyrus_name} .= "@".$domainDesc->{domain_name};
        }

        if( !$self->getDelete() ) {
            if( defined($dbUserMoreDesc->{current_userobm_login}) ) {
                # Si l'utilisateur existe déjà dans la tables P_UserObm, on
                # récupère le nom actuel de sa BAL
                $self->{"properties"}->{current_userobm_mailbox_cyrus_name} = $dbUserMoreDesc->{current_userobm_login};

                if( !$singleNameSpace ) {
                    $self->{"properties"}->{current_userobm_mailbox_cyrus_name} .= "@".$domainDesc->{domain_name};
                }

            }

        }else {
            # En cas de suppression de l'utilisateur, la BAL 'new' est la
            # même que la BAL 'current'
            $self->{"properties"}->{current_userobm_mailbox_cyrus_name} = $dbUserDesc->{userobm_login};

            if( !$singleNameSpace ) {
                $self->{"properties"}->{current_userobm_mailbox_cyrus_name} .= "@".$domainDesc->{domain_name};
            }

        }

        # Partition Cyrus associée à cette BAL
        if( $OBM::Parameters::common::cyrusDomainPartition ) {
            $self->{"properties"}->{userobm_mailbox_partition} = $domainDesc->{domain_dn};
            $self->{"properties"}->{userobm_mailbox_partition} =~ s/\./_/g;
            $self->{"properties"}->{userobm_mailbox_partition} =~ s/-/_/g;
        }

        # Gestion du serveur de mail
        $self->{"properties"}->{userobm_mailbox_server} = $dbUserMoreDesc->{mailserver_host_id};

        # Gestion du quota
        $self->{"properties"}->{userobm_mailbox_quota} = $dbUserDesc->{userobm_mail_quota}*1024;

        # Gestion des sous-répertoires de la BAL a créer
        if( defined($userMailboxDefaultFolders) ) {
            foreach my $folderTree ( split( ',', $userMailboxDefaultFolders ) ) {
                if( $folderTree !~ /(^[",]$)|(^$)/ ) {
                    my $folderName = $dbUserDesc->{userobm_login};
                    foreach my $folder ( split( '/', $folderTree ) ) {
                        $folder =~ s/^\s+//;

                        $folderName .= '/'.$folder;
                        if( !$singleNameSpace ) {
                            push( @{$self->{"properties"}->{mailbox_folders}}, $folderName.'@'.$domainDesc->{domain_name} );
                        }else {
                            push( @{$self->{"properties"}->{mailbox_folders}}, $folderName );
                        }
                    }
                }
            }
        }

        # Gestion de la livraison du courrier
        $self->{"properties"}->{userobm_mailLocalServer} = "lmtp:".$localServerIp.":24";

        # Gestion du message d'absence
        $self->{"properties"}->{userobm_vacation_message} = uri_unescape($dbUserDesc->{userobm_vacation_message});
    }


    if( $ldapAllMainMailAddress && !$self->{"properties"}->{userobm_mail_perms} ) {
        # Si la personne n'a pas le droit mail, mais a une/des adresses mails
        # valides, on la positionne dans l'annuaire. On ne positionne que les
        # attributs 'mail', pas les 'mailAlias'.
        my $return = $self->makeEntityEmail( $dbUserDesc->{userobm_email}, $domainDesc->{domain_name}, $domainDesc->{domain_alias} );
        if( $return && exists( $self->{"properties"}->{"emailAlias"} ) ) {
            delete( $self->{"properties"}->{"emailAlias"} );
        }
    }

    # Gestion du droit Samba de l'utilisateur
    if( $OBM::Parameters::common::obmModules->{samba} && $dbUserDesc->{userobm_samba_perms} ) {
        # Le mot de passe
        my $errorCode = &OBM::passwd::getNTLMPasswd( $dbUserDesc->{userobm_password}, \$self->{"properties"}->{userobm_samba_lm_password}, \$self->{"properties"}->{userobm_samba_nt_password} );
        if( $errorCode ) {
            &OBM::toolBox::write_log( "[Entities::obmUser]: probleme lors de la generation du mot de passe windows de l'utilisateur : ".$self->getEntityDescription(), "W" );
            return 0;
        }

        $self->{"properties"}->{userobm_samba_perms} = 1;
        if( lc($dbUserDesc->{userobm_perms}) eq "admin" ) {
            $self->{"properties"}->{userobm_uid} = 0;
        }
 
        $self->{"properties"}->{userobm_samba_sid} = &OBM::Samba::utils::getUserSID( $domainDesc->{domain_samba_sid}, $dbUserDesc->{userobm_uid} );
        $self->{"properties"}->{userobm_samba_group_sid} = &OBM::Samba::utils::getGroupSID( $domainDesc->{domain_samba_sid}, $dbUserDesc->{userobm_gid} );
        $self->{"properties"}->{userobm_samba_flags} = "[U]";

        # Le script de session
        if( $dbUserDesc->{userobm_samba_logon_script} ) {
            $self->{"properties"}->{userobm_samba_logon_script} = $dbUserDesc->{userobm_samba_logon_script};
        }

        # La lettre du lecteur du répertoire personnel
        if( $dbUserDesc->{userobm_samba_home_drive} && $dbUserDesc->{userobm_samba_home} ) {
            $self->{"properties"}->{userobm_samba_home_drive} = $dbUserDesc->{userobm_samba_home_drive}.":";
            $self->{"properties"}->{userobm_samba_home} = $dbUserDesc->{userobm_samba_home};
        }

        # Le répertoire du profil W2k (version antérieures voir smb.conf)
        if( $domainDesc->{domain_samba_user_profile} ) {
            $self->{"properties"}->{userobm_samba_user_profile} = $domainDesc->{domain_samba_user_profile};
            $self->{"properties"}->{userobm_samba_user_profile} =~ s/\%u/$dbUserDesc->{userobm_login}/g;

        }
    }else {
        $self->{"properties"}->{userobm_samba_perms} = 0;
    }


    # Si nous ne sommes pas en mode incrémental, on charge aussi les liens de
    # cette entité
    if( $self->isLinks() ) {
        $self->getEntityLinks( $dbHandler, $domainDesc );
    }


    return 1;
}


sub updateDbEntity {
    my $self = shift;
    my( $dbHandler ) = @_;

    if( !defined($dbHandler) ) {
        return 0;
    }

    my $dbUserDesc = $self->{"userDbDesc"};
    if( !defined($dbUserDesc) ) {
        return 0;
    }

    &OBM::toolBox::write_log( "[Entities::obmUser]: MAJ de l'utilisateur ".$self->getEntityDescription()." dans les tables de production", "W" );

    # MAJ de l'entité dans la table de production
    my $query = "REPLACE INTO P_UserObm SET ";
    my $first = 1;
    while( my( $columnName, $columnValue ) = each(%{$dbUserDesc}) ) {
        if( !$first ) {
            $query .= ", ";
        }else {
            $first = 0;
        }

        $query .= $columnName."=".$dbHandler->quote($columnValue);
    }

    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }


    return 1;
}


sub updateDbEntityLinks {
    my $self = shift;
    my( $dbHandler ) = @_;

    if( !defined($dbHandler) ) {
        return 0;
    }

    &OBM::toolBox::write_log( "[Entities::obmUser]: MAJ des liens de l'utilisateur ".$self->getEntityDescription()." dans les tables de production", "W" );

    # On supprime les liens actuels de la table de production
    my $query = "DELETE FROM P_EntityRight WHERE entityright_consumer='user' AND entityright_entity='mailbox' AND entityright_entity_id=".$self->{"objectId"};

    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }


    # On copie les nouveaux droits
    $query = "INSERT INTO P_EntityRight SELECT * FROM EntityRight WHERE entityright_consumer='user' AND entityright_entity='mailbox' AND entityright_entity_id=".$self->{"objectId"};

    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }


    return 1;
}


sub updateDbEntityPassword {
    my $self = shift;
    my( $dbHandler, $passwordDesc ) = @_;
    my $queryResult;

    if( !defined($dbHandler) || (ref($passwordDesc) ne "HASH") ) {
        return 0;
    }

    if( !($passwordDesc->{newPasswordType}) || !($passwordDesc->{newPassword}) ) {
        return 0;
    }

    if( !$passwordDesc->{sql} ) {
        return 0;
    }


    &OBM::toolBox::write_log( "[Entities::obmUser]: mise a jour du mot de passe SQL de l'utilisateur : ".$self->getEntityDescription(), "W" );
    

    my $query = "UPDATE UserObm SET userobm_password_type=".$dbHandler->quote($passwordDesc->{newPasswordType}).", userobm_password=".$dbHandler->quote($passwordDesc->{newPassword})." WHERE userobm_id=".$self->{objectId};
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: probleme lors de l'execution de la requete.", "W" );
        if( defined($queryResult) ) {
            &OBM::toolBox::write_log( $queryResult->err, "W" );
        }

        return 0;
    }


    $query = "UPDATE P_UserObm SET userobm_password_type=".$dbHandler->quote($passwordDesc->{newPasswordType}).", userobm_password=".$dbHandler->quote($passwordDesc->{newPassword})." WHERE userobm_id=".$self->{objectId};
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: probleme lors de l'execution de la requete.", "W" );
        if( defined($queryResult) ) {
            &OBM::toolBox::write_log( $queryResult->err, "W" );
        }

        return 0;
    }
    

    return 1;
}


sub getEntityLinks {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;

    $self->_getEntityMailboxAcl( $dbHandler, $domainDesc );

    # On précise que les liens de l'entité sont aussi à mettre à jour.
    $self->{"links"} = 1;

    return 1;
}


sub getEntityDescription {
    my $self = shift;
    my $dbEntry = $self->{userDbDesc};
    my $entryProp = $self->{"properties"};
    my $description = "";


    if( defined($dbEntry->{userobm_login}) ) {
        $description .= "identifiant '".$dbEntry->{userobm_login}."'";
    }

    if( defined($entryProp->{userobm_domain}) ) {
        $description .= ", domaine '".$entryProp->{userobm_domain}."'";
    }

    if( ($description ne "") && defined($self->{type}) ) {
        $description .= ", type '".$self->{type}."'";
    }

    if( $description ne "" ) {
        return $description;
    }

    if( defined($self->{objectId}) ) {
        $description .= "ID BD '".$self->{objectId}."'";
    }

    if( defined($self->{type}) ) {
        $description .= ",type '".$self->{type}."'";
    }

    return $description;
}


sub _getEntityMailboxAcl {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;
    my $dbEntry = $self->{userDbDesc};
    my $entryProp = $self->{"properties"};
    my $userId = $self->{"objectId"};

    if( $entryProp->{"userobm_mail_perms"} ) {
        my $entityType = $self->{"entityRightType"};
        my %rightDef;

        my $userObmTable = "UserObm";
        my $entityRightTable = "EntityRight";
        if( $self->getDelete() ) {
            $userObmTable = "P_".$userObmTable;
            $entityRightTable = "P_".$entityRightTable;
        }


        $rightDef{"read"}->{"compute"} = 1;
        $rightDef{"read"}->{"sqlQuery"} = "SELECT i.userobm_id, i.userobm_login FROM ".$userObmTable." i, ".$entityRightTable." j WHERE i.userobm_id=j.entityright_consumer_id AND j.entityright_write=0 AND j.entityright_read=1 AND j.entityright_entity_id=".$userId." AND j.entityright_entity='".$entityType."'";

        $rightDef{"writeonly"}->{"compute"} = 1;
        $rightDef{"writeonly"}->{"sqlQuery"} = "SELECT i.userobm_id, i.userobm_login FROM ".$userObmTable." i, ".$entityRightTable." j WHERE i.userobm_id=j.entityright_consumer_id AND j.entityright_write=1 AND j.entityright_read=0 AND j.entityright_entity_id=".$userId." AND j.entityright_entity='".$entityType."'";

        $rightDef{"write"}->{"compute"} = 1;
        $rightDef{"write"}->{"sqlQuery"} = "SELECT userobm_id, userobm_login FROM ".$userObmTable." LEFT JOIN ".$entityRightTable." ON entityright_write=1 AND entityright_read=1 AND entityright_consumer_id=userobm_id AND entityright_entity='".$entityType."' WHERE entityright_entity_id=".$userId." OR userobm_id=".$userId;

        $rightDef{"public"}->{"compute"} = 0;
        $rightDef{"public"}->{"sqlQuery"} = "SELECT entityright_read, entityright_write FROM ".$entityRightTable." WHERE entityright_entity_id=".$userId." AND entityright_entity='".$entityType."' AND entityright_consumer_id=0";

        # On recupere la definition des ACL
        $self->{"userLinks"}->{"userobm_mailbox_acl"} = &OBM::toolBox::getEntityRight( $dbHandler, $domainDesc, \%rightDef, $userId );
    }

    return 1;
}


sub getLdapDnPrefix {
    my $self = shift;
    my( $which ) = @_;
    my $dnPrefix = undef;
    my $dbEntry = $self->{"userDbDesc"};
    my $entryProp = $self->{"properties"};

    if( (lc($which) =~ /^new$/) && !$self->getDelete() ) {
        if( defined($self->{"dnPrefix"}) && defined($dbEntry->{$self->{newDnValue}}) ) {
            $dnPrefix = $self->{"dnPrefix"}."=".$dbEntry->{$self->{newDnValue}};
        }
    }elsif( lc($which) =~ /^current$/ ) {
        if( defined($self->{"dnPrefix"}) && defined($entryProp->{$self->{currentDnValue}}) ) {
            $dnPrefix = $self->{"dnPrefix"}."=".$entryProp->{$self->{currentDnValue}};
        }
    }

    return $dnPrefix;
}


sub getLdapObjectclass {
    my $self = shift;
    my($objectclass, $deletedObjectclass) = @_;
    my $entryProp = $self->{"properties"};
    my %realObjectClass;

    if( !defined($objectclass) || (ref($objectclass) ne "ARRAY") ) {
        $objectclass = $self->{objectclass};
    }

    for( my $i=0; $i<=$#$objectclass; $i++ ) {
        if( (lc($objectclass->[$i]) eq "sambasamaccount") && !$entryProp->{userobm_samba_perms} ) {
            push( @{$deletedObjectclass}, $objectclass->[$i] );
            next;
        }

        $realObjectClass{$objectclass->[$i]} = 1;
    }

    # Si le droit Samba est actif, on s'assure de la présence des classes
    # nécessaires - nécessaires pour les MAJ
    if( $entryProp->{userobm_samba_perms} ) {
        $realObjectClass{sambaSamAccount} = 1;
    }

    my @realObjectClass = keys(%realObjectClass);
    return \@realObjectClass;
}


sub createLdapEntry {
    my $self = shift;
    my ( $ldapEntry ) = @_;
    my $dbEntry = $self->{"userDbDesc"};
    my $entryProp = $self->{"properties"};
    my $entryLinks = $self->{"userLinks"};


    # Les paramètres nécessaires
    if( $dbEntry->{userobm_login} && defined($entryProp->{userobm_uid}) && defined($dbEntry->{userobm_gid}) && $entryProp->{userobm_shell} ) {

        $ldapEntry->add(
            objectClass => $self->getLdapObjectclass(),
            uid => to_utf8({ -string => $dbEntry->{userobm_login}, -charset => $defaultCharSet }),
            uidNumber => $entryProp->{"userobm_uid"},
            gidNumber => $dbEntry->{"userobm_gid"},
            loginShell => $entryProp->{userobm_shell}
        );
    }else {
        return 0;
    }

    # Le nom complet
    if( $entryProp->{userobm_full_name} ) {
        $ldapEntry->add( cn => to_utf8({ -string => $entryProp->{userobm_full_name}, -charset => $defaultCharSet }) );
        $ldapEntry->add( displayName => to_utf8({ -string => $entryProp->{userobm_full_name}, -charset => $defaultCharSet }) );
    }

    # Le nom
    if( $dbEntry->{userobm_lastname} ) {
        $ldapEntry->add( sn => to_utf8({ -string => $dbEntry->{userobm_lastname}, -charset => $defaultCharSet }) );
    }

    # Le prénom
    if( $dbEntry->{userobm_firstname} ) {
        $ldapEntry->add( givenName => to_utf8({ -string => $dbEntry->{userobm_firstname}, -charset => $defaultCharSet }) );
    }

    # Le répertoire personnel
    if( $entryProp->{userobm_homedir} ) {
        $ldapEntry->add( homeDirectory => to_utf8({ -string => $entryProp->{userobm_homedir}, -charset => $defaultCharSet }) );
    }

    # Le mot de passe
    if( $entryProp->{userobm_crypt_passwd} ) {
        $ldapEntry->add( userPassword => $entryProp->{userobm_crypt_passwd} );
    }

    # Le téléphone
    if( $entryProp->{userobm_phone} ) {
        $ldapEntry->add( telephoneNumber => $entryProp->{userobm_phone} );
    }

    # Le fax
    if( $entryProp->{userobm_fax} ) {
        $ldapEntry->add( facsimileTelephoneNumber => $entryProp->{userobm_fax} );
    }

    # Le mobile
    if( $dbEntry->{userobm_mobile} ) {
        $ldapEntry->add( mobile => $dbEntry->{userobm_mobile} );
    }

    # Le titre
    if( $dbEntry->{userobm_title} ) {
        $ldapEntry->add( title => to_utf8({ -string => $dbEntry->{userobm_title}, -charset => $defaultCharSet }) );
    }

    # Le service
    if( $dbEntry->{userobm_service} ) {
        $ldapEntry->add( ou => to_utf8({ -string => $dbEntry->{userobm_service}, -charset => $defaultCharSet }) );
    }

    # La description
    if( $dbEntry->{userobm_description} ) {
        $ldapEntry->add( description => to_utf8({ -string => $dbEntry->{userobm_description}, -charset => $defaultCharSet }) );
    }

    # L'accés web
    if( $dbEntry->{userobm_web_perms} ) {
        $ldapEntry->add( webAccess => $dbEntry->{userobm_web_perms} );
    }

    # La boîte à lettres de l'utilisateur
    if( $entryProp->{userobm_mailbox_ldap_name} ) {
        $ldapEntry->add( mailBox => $entryProp->{userobm_mailbox_ldap_name} );
    }

    # Le serveur de BAL local
    if( $entryProp->{userobm_mailLocalServer} ) {
        $ldapEntry->add( mailBoxServer => $entryProp->{userobm_mailLocalServer} );
    }

    # L'acces mail
    if( $entryProp->{userobm_mail_perms} ) {
        $ldapEntry->add( mailAccess => "PERMIT" );
    }else {
        $ldapEntry->add( mailAccess => "REJECT" );
    }

    # La limite aux domaines locaux
    if( $entryProp->{userobm_mailLocalOnly} ) {
        $ldapEntry->add( mailLocalOnly => $entryProp->{userobm_mailLocalOnly} );
    }
    
    # Les adresses mails
    if( $entryProp->{"email"} ) {
        $ldapEntry->add( mail => $entryProp->{"email"} );
    }
    
    # Les adresses mail secondaires
    if( $entryProp->{"emailAlias"} ) {
        $ldapEntry->add( mailAlias => $entryProp->{"emailAlias"} );
    }
    
    # L'adresse postale
    if( $entryProp->{userobm_address} ) {
        # Thunderbird, IceDove... : ne comprennent que cet attribut
        $ldapEntry->add( street => to_utf8({ -string => $entryProp->{userobm_address}, -charset => $defaultCharSet }) );
        # Outlook : ne comprend que cet attribut
        # Outlook Express : préfère celui-là à 'street'
        $ldapEntry->add( postalAddress => to_utf8({ -string => $entryProp->{userobm_address}, -charset => $defaultCharSet }) );
    }
    
    # Le code postal
    if( $dbEntry->{userobm_zipcode} ) {
        $ldapEntry->add( postalCode => to_utf8({ -string => $dbEntry->{userobm_zipcode}, -charset => $defaultCharSet }) );
    }
    
    # La ville
    if( $dbEntry->{userobm_town} ) {
        $ldapEntry->add( l => to_utf8({ -string => $dbEntry->{userobm_town}, -charset => $defaultCharSet }) );
    }

    # La visibilité
    if( $entryProp->{userobm_hidden} ) {
        $ldapEntry->add( hiddenUser => $entryProp->{userobm_hidden} );
    }

    # Le domaine
    if( $entryProp->{userobm_domain} ) {
        $ldapEntry->add( obmDomain => to_utf8({ -string => $entryProp->{userobm_domain}, -charset => $defaultCharSet }) );
    }

    # Le SID de l'utilisateur
    if( $entryProp->{userobm_samba_sid} ) {
        $ldapEntry->add( sambaSID => to_utf8({ -string => $entryProp->{userobm_samba_sid}, -charset => $defaultCharSet }) );
    }

    # Le SID du groupe de l'utilisateur
    if( $entryProp->{userobm_samba_group_sid} ) {
        $ldapEntry->add( sambaPrimaryGroupSID => to_utf8({ -string => $entryProp->{userobm_samba_group_sid}, -charset => $defaultCharSet }) );
    }

    # Les flags de l'utilisateur
    if( $entryProp->{userobm_samba_flags} ) {
        $ldapEntry->add( sambaAcctFlags => to_utf8({ -string => $entryProp->{userobm_samba_flags}, -charset => $defaultCharSet }) );
    }

    # Le mot de passe
    if( $entryProp->{userobm_samba_lm_password} ) {
        $ldapEntry->add( sambaLMPassword => to_utf8({ -string => $entryProp->{userobm_samba_lm_password}, -charset => $defaultCharSet }) );
        $ldapEntry->add( sambaNTPassword => to_utf8({ -string => $entryProp->{userobm_samba_nt_password}, -charset => $defaultCharSet }) );
    }

    # Le script de session
    if( $entryProp->{userobm_samba_logon_script} ) {
        $ldapEntry->add( sambaLogonScript => to_utf8({ -string => $entryProp->{userobm_samba_logon_script}, -charset => $defaultCharSet }) );
    }

    # Le répertoire personnel et la lettre du lecteur associé
    if( $entryProp->{userobm_samba_home_drive} && $entryProp->{userobm_samba_home} ) {
        $ldapEntry->add( sambaHomeDrive => to_utf8({ -string => $entryProp->{userobm_samba_home_drive}, -charset => $defaultCharSet }) );
        $ldapEntry->add( sambaHomePath => to_utf8({ -string => $entryProp->{userobm_samba_home}, -charset => $defaultCharSet }) );
    }

    # Le répertoire du profil W2k
    if( $entryProp->{userobm_samba_user_profile} ) {
        $ldapEntry->add( sambaProfilePath => to_utf8({ -string => $entryProp->{userobm_samba_user_profile}, -charset => $defaultCharSet }) );
    }


    return 1;
}


sub updateLdapEntryDn {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $dbEntry = $self->{userDbDesc};
    my $update = 0;

    if( !$OBM::Parameters::common::renameUserMailbox ) {
        return 0;
    }

    if( !defined($ldapEntry) ) {
        return 0;
    }

    # L'UID
    if( &OBM::Ldap::utils::modifyAttr( $dbEntry->{"userobm_login"}, $ldapEntry, "uid" ) ) {
        # Si cet attribut est modifié, son DN doit aussi être mis à jour
        $ldapEntry->add( newrdn => to_utf8( { -string => $self->getLdapDnPrefix( "new" ), -charset => $defaultCharSet } ) );
        $update = 1;
    }

    return $update;
 }


sub updateLdapEntry {
    my $self = shift;
    my( $ldapEntry, $objectclassDesc ) = @_;
    my $dbEntry = $self->{userDbDesc};
    my $entryProp = $self->{"properties"};
    my $entryLinks = $self->{userLinks};

    require OBM::Entities::entitiesUpdateState;
    my $update = OBM::Entities::entitiesUpdateState->new();


    if( !defined($ldapEntry) ) {
        return undef;
    }

    
   # Vérification des objectclass
    my @deletedObjectclass;
    my $currentObjectclass = $self->getLdapObjectclass( $ldapEntry->get_value("objectClass", asref => 1), \@deletedObjectclass);
    if( &OBM::Ldap::utils::modifyAttrList( $currentObjectclass, $ldapEntry, "objectClass" ) ) {
        $update->setUpdate();
    }

    if( $#deletedObjectclass >= 0 ) {
        # Pour les schémas LDAP supprimés, on détermine les attributs à
        # supprimer.
        # Uniquement ceux qui ne sont pas utilisés par d'autres objets.
        my $deleteAttrs = &OBM::Ldap::utils::diffObjectclassAttrs(\@deletedObjectclass, $currentObjectclass, $objectclassDesc);

        for( my $i=0; $i<=$#$deleteAttrs; $i++ ) {
            SWITCH: {
                if( $deleteAttrs->[$i] =~ /^sambaSID$/ ) {
                    $update->setUpdateLinks();
                    last SWITCH;
                }
            }

            if( &OBM::Ldap::utils::modifyAttrList( undef, $ldapEntry, $deleteAttrs->[$i] ) ) {
                $update->setUpdate();
            }
        }
    }

    # L'UID number
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{"userobm_uid"}, $ldapEntry, "uidNumber" ) ) {
        $update->setUpdate();
    }
    
    # Le GID number
    if( &OBM::Ldap::utils::modifyAttr( $dbEntry->{"userobm_gid"}, $ldapEntry, "gidNumber" ) ) {
        $update->setUpdate();
    }

    # Le shell utilisateur
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_shell}, $ldapEntry, "loginShell" ) ) {
        $update->setUpdate();
    }
    
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_full_name}, $ldapEntry, "cn" ) ) {
    # On synchronise le nom affichable
        &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_full_name}, $ldapEntry, "displayName" );
    
        $update->setUpdate();
    }
    
    # Le nom de famille
    if( &OBM::Ldap::utils::modifyAttr( $dbEntry->{userobm_lastname}, $ldapEntry, "sn" ) ) {
        $update->setUpdate();
    }
    
    # Le prénom
    if( &OBM::Ldap::utils::modifyAttr( $dbEntry->{userobm_firstname}, $ldapEntry, "givenName" ) ) {
        $update->setUpdate();
    }

    # Le répertoire personnel
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_homedir}, $ldapEntry, "homeDirectory" ) ) {
        $update->setUpdate();
    }

    # Le téléphone
    if( &OBM::Ldap::utils::modifyAttrList( $entryProp->{userobm_phone}, $ldapEntry, "telephoneNumber" ) ) {
        $update->setUpdate();
    }
    
    # Le fax
    if( &OBM::Ldap::utils::modifyAttrList( $entryProp->{userobm_fax}, $ldapEntry, "facsimileTelephoneNumber" ) ) {
        $update->setUpdate();
    }
    
    # Le mobile
    if( &OBM::Ldap::utils::modifyAttr( $dbEntry->{userobm_mobile}, $ldapEntry, "mobile" ) ) {
        $update->setUpdate();
    }
 
    # Le titre
    if( &OBM::Ldap::utils::modifyAttr( $dbEntry->{userobm_title}, $ldapEntry, "title" ) ) {
        $update->setUpdate();
    }
    
    # Le service
    if( &OBM::Ldap::utils::modifyAttr( $dbEntry->{userobm_service}, $ldapEntry, "ou" ) ) {
        $update->setUpdate();
    }
    
    # La description
    if( &OBM::Ldap::utils::modifyAttr( $dbEntry->{userobm_description}, $ldapEntry, "description" ) ) {
        $update->setUpdate();
    }
    
    # L'accés au web
    if( &OBM::Ldap::utils::modifyAttr( $dbEntry->{userobm_web_perms}, $ldapEntry, "webAccess" ) ) {
        $update->setUpdate();
    }
    
    # La boîte à lettres de l'utilisateur
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_mailbox_ldap_name}, $ldapEntry, "mailBox" ) ) {
        $update->setUpdate();
    }
    
    # Le serveur de BAL local
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_mailLocalServer}, $ldapEntry, "mailBoxServer" ) ) {
        $update->setUpdate();
    }

    # L'accés au mail
    if( $entryProp->{userobm_mail_perms} && ( &OBM::Ldap::utils::modifyAttr( "PERMIT", $ldapEntry, "mailAccess" ) ) ) {
        $update->setUpdate();
    
    }elsif( !$entryProp->{userobm_mail_perms} && ( &OBM::Ldap::utils::modifyAttr( "REJECT", $ldapEntry, "mailAccess" ) ) ) {
        $update->setUpdate();
    }
    
    # La limitation au domaine local
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_mailLocalOnly}, $ldapEntry, "mailLocalOnly" ) ) {
        $update->setUpdate();
    }

    # Les adresses mails
    if( &OBM::Ldap::utils::modifyAttrList( $entryProp->{"email"}, $ldapEntry, "mail" ) ) {
        $update->setUpdate();
    }

    # Les adresses mail secondaires
    if( &OBM::Ldap::utils::modifyAttrList( $entryProp->{"emailAlias"}, $ldapEntry, "mailAlias" ) ) {
        $update->setUpdate();
    }
   
    # L'adresse postale
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_address}, $ldapEntry, "street" ) ) {
        &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_address}, $ldapEntry, "postalAddress" );
        $update->setUpdate();
    }
    
    # Le code postal
    if( &OBM::Ldap::utils::modifyAttr( $dbEntry->{userobm_zipcode}, $ldapEntry, "postalCode" ) ) {
        $update->setUpdate();
    }
    
    # La ville
    if( &OBM::Ldap::utils::modifyAttr( $dbEntry->{userobm_town}, $ldapEntry, "l" ) ) {
        $update->setUpdate();
    }

    # La visibilité
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_hidden}, $ldapEntry, "hiddenUser" ) ) {
        $update->setUpdate();
    }
    
    # Le domaine
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_domain}, $ldapEntry, "obmDomain") ) {
        $update->setUpdate();
    }


    if( defined($entryProp->{userobm_samba_sid}) ) {
        my @currentLdapUserSambaSid = $ldapEntry->get_value( "sambaSID", asref => 1 );
        if( $#currentLdapUserSambaSid < 0 ) {
            # Si le SID de l'utilisateur n'est pas actuellement dans LDAP mais est dans
            # la description de l'utilisateur, c'est qu'on vient de ré-activer le droit
            # samba de l'utilisateur. Il faut donc placer les mots de passes.
            if( &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_samba_lm_password}, $ldapEntry, "sambaLMPassword" ) ) {
                &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_samba_nt_password}, $ldapEntry, "sambaNTPassword" );
                $update->setUpdate();
            }

            # On replace aussi les flags de l'utilisateur
            if( &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_samba_flags}, $ldapEntry, "sambaAcctFlags" ) ) {
                $update->setUpdate();
            }
        }
    }

    # Le SID de l'utilisateur
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_samba_sid}, $ldapEntry, "sambaSID") ) {
        $update->setUpdate();
        $update->setUpdateLinks();
    }

    # Le SID du groupe de l'utilisateur
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_samba_group_sid}, $ldapEntry, "sambaPrimaryGroupSID") ) {
        $update->setUpdate();
        $update->setUpdateLinks();
    }

    # Les flags de l'utilisateur
    # Non mis à jour car peuvent évoluer via les outils d'administration windows

    # Le script de session
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_samba_logon_script}, $ldapEntry, "sambaLogonScript") ) {
        $update->setUpdate();
    }

    # Le répertoire personnel
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_samba_home}, $ldapEntry, "sambaHomePath") ) {
        $update->setUpdate();
    }

    # La lettre du lecteur associée au répertoire personnel
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_samba_home_drive}, $ldapEntry, "sambaHomeDrive") ) {
        $update->setUpdate();
    }

    # Le répertoire du profil W2k
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{userobm_samba_user_profile}, $ldapEntry, "sambaProfilePath") ) {
        $update->setUpdate();
    }


    if( $self->isLinks() ) {
        if( $self->updateLdapEntryLinks( $ldapEntry ) ) {
            $update->setUpdate();
        }
    }


    return $update;
}


sub updateLdapEntryLinks {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $update = 0;
    my $entryLinks = $self->{userLinks};

    if( !defined($ldapEntry) ) {
        return $update;
    }
    
    return $update;
}


sub updateLdapEntryPassword {
    my $self = shift;
    my( $ldapEntry, $passwordDesc ) = @_;
    my $update = 0;

    if( !defined($ldapEntry) || (ref($passwordDesc) ne "HASH") ) {
        return 0;
    }

    if( !($passwordDesc->{newPasswordType}) || !($passwordDesc->{newPassword}) ) {
        return 0;
    }

    
    if( $passwordDesc->{unix} ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: mise a jour du mot de passe unix de l'utilisateur : ".$self->getEntityDescription(), "W" );
        if( &OBM::Ldap::utils::modifyAttr( &OBM::passwd::convertPasswd( $passwordDesc->{newPasswordType}, $passwordDesc->{newPassword} ), $ldapEntry, "userPassword" ) ) {
            $update = 1;
        }
    }

    
    if( $passwordDesc->{samba} ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: mise a jour du mot de passe samba de l'utilisateur : ".$self->getEntityDescription(), "W" );

        my $userSambaLMPassword;
        my $userSambaNTPassword;

        my $errorCode = &OBM::passwd::getNTLMPasswd( $passwordDesc->{newPassword}, \$userSambaLMPassword, \$userSambaNTPassword );
        if( $errorCode ) {
            &OBM::toolBox::write_log( "[Entities::obmUser]: probleme lors de la generation du mot de passe windows de l'utilisateur : ".$self->getEntityDescription(), "W" );
            return 0;
        }

        if( &OBM::Ldap::utils::modifyAttr( $userSambaLMPassword, $ldapEntry, "sambaLMPassword" ) ) {
            &OBM::Ldap::utils::modifyAttr( $userSambaNTPassword, $ldapEntry, "sambaNTPassword" );

            $update = 1;
        }
    }


    return $update;
}


sub getMailServerId {
    my $self = shift;
    my $mailServerId = undef;
    my $dbEntry = $self->{userDbDesc};
    my $entryProp = $self->{"properties"};

    if( $entryProp->{userobm_mail_perms} ) {
        $mailServerId = $entryProp->{userobm_mailbox_server};
    }

    return $mailServerId;
}


sub getMailboxPrefix {
    my $self = shift;
    
    return "user/";
}


sub getMailboxSieve {
    my $self = shift;

    return $self->{"sieve"};
}


sub getMailboxName {
    my $self = shift;
    my( $which ) = @_;
    my $mailBoxName = undef;
    my $dbEntry = $self->{userDbDesc};
    my $entryProp = $self->{"properties"};


    if( $entryProp->{userobm_mail_perms} ) {
        if( lc($which) =~ /^new$/ ) {
            $mailBoxName = $entryProp->{new_userobm_mailbox_cyrus_name};
        }elsif( lc($which) =~ /^current$/ ) {
            $mailBoxName = $entryProp->{current_userobm_mailbox_cyrus_name};
        }
    }

    return $mailBoxName;
}


sub getMailboxPartition {
    my $self = shift;
    my $mailboxPartition = undef;
    my $dbEntry = $self->{userDbDesc};
    my $entryProp = $self->{"properties"};

    if( $entryProp->{userobm_mail_perms} ) {
        $mailboxPartition = $entryProp->{userobm_mailbox_partition};
    }

    return $mailboxPartition;
}


sub getMailboxQuota {
    my $self = shift;
    my $mailBoxQuota = undef;
    my $dbEntry = $self->{userDbDesc};
    my $entryProp = $self->{"properties"};

    if( $entryProp->{userobm_mail_perms} ) {
        $mailBoxQuota = $entryProp->{userobm_mailbox_quota};
    }

    return $mailBoxQuota;
}


sub getMailboxAcl {
    my $self = shift;
    my $mailBoxAcl = undef;
    my $dbEntry = $self->{userDbDesc};
    my $entryProp = $self->{"properties"};
    my $entryLinks = $self->{userLinks};

    if( $entryProp->{userobm_mail_perms} ) {
        $mailBoxAcl = $entryLinks->{userobm_mailbox_acl};
    }

    return $mailBoxAcl;
}


sub getSieveVacation {
    my $self = shift;
    my $dbEntry = $self->{userDbDesc};
    my $entryProp = $self->{"properties"};

    if( !$dbEntry->{userobm_vacation_enable} ) {
        return undef;
    }

    if( !$entryProp->{"email"} ) {
        return undef;
    }
    my $boxEmails = $entryProp->{"email"};
    my $boxEmailsAlias = $entryProp->{"emailAlias"};

    if( !$entryProp->{userobm_vacation_message} ) {
        return undef;
    }
    my $boxVacationMessage = $entryProp->{userobm_vacation_message};

    my $vacationMsg = "vacation :addresses [ ";
    my $firstAddress = 1;
    for( my $i=0; $i<=$#{$boxEmails}; $i++ ) {
        if( !$firstAddress ) {
            $vacationMsg .= ", ";
        }else {
            $firstAddress = 0;
        }

        $vacationMsg .= "\"".$boxEmails->[$i]."\"";
    }

    for( my $i=0; $i<=$#{$boxEmailsAlias}; $i++ ) {
        if( !$firstAddress ) {
            $vacationMsg .= ", ";
        }else {
            $firstAddress = 0;
        }

        $vacationMsg .= "\"".$boxEmailsAlias->[$i]."\"";
    }

    $vacationMsg .= " ] \"".to_utf8( { -string => $boxVacationMessage, -charset => $defaultCharSet } )."\";\n";


    return $vacationMsg;
}


sub getSieveNomade {
    my $self = shift;
    my $dbEntry = $self->{userDbDesc};
    my $entryProp = $self->{"properties"};

    if( !$dbEntry->{userobm_nomade_perms} ) {
        return undef;
    }

    if( !$dbEntry->{userobm_nomade_enable} ) {
        return undef;
    }

    if( !$dbEntry->{userobm_email_nomade} ) {
        return undef;
    }
    my $nomadeEmail = $dbEntry->{userobm_email_nomade};

    my $nomadeMsg = "redirect \"".$nomadeEmail."\";\n";

    if( !$dbEntry->{userobm_nomade_local_copy} ) {
        $nomadeMsg .= "discard;\n";
        $nomadeMsg .= "stop;\n";
    }else {
        $nomadeMsg .= "keep;\n";
    }

    return $nomadeMsg;
}


sub dump {
    my $self = shift;
    my @desc;

    push( @desc, $self );
    
    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}


sub getHostIpById {
    my $self = shift;
    my( $dbHandler, $hostId ) = @_;

    if( !defined($hostId) ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: identifiant de l'hote non défini !", "W" );
        return undef;
    }elsif( $hostId !~ /^[0-9]+$/ ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: identifiant de l'hote '".$hostId."' incorrect !", "W" );
        return undef;
    }elsif( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: connection à la base de donnee incorrect !", "W" );
        return undef;
    }

    my $hostTable = "Host";
    if( $self->getDelete() ) {
        $hostTable = "P_".$hostTable;
    }

    my $query = "SELECT host_ip FROM ".$hostTable." WHERE host_id='".$hostId."'";


    # On execute la requete
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: probleme lors de l'execution de la requete.", "W" );
        if( defined($queryResult) ) {
            &OBM::toolBox::write_log( $queryResult->err, "W" );
        }

        return undef;
    }

    if( !(my( $hostIp ) = $queryResult->fetchrow_array) ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: identifiant de l'hote '".$hostId."' inconnu !", "W" );

        $queryResult->finish;
        return undef;
    }else{
        $queryResult->finish;
        return $hostIp;
    }

    return undef;

}

