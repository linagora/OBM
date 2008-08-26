package OBM::Entities::obmGroup;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Entities::commonEntities qw(getType setDelete getDelete getArchive isLinks getEntityId makeEntityEmail);
use OBM::Parameters::common;
require OBM::Parameters::ldapConf;
require OBM::Ldap::utils;
require OBM::toolBox;
require OBM::dbUtils;
require OBM::Samba::utils;


sub new {
    my $self = shift;
    my( $links, $deleted, $groupId ) = @_;

    my %obmGroupAttr = (
        type => undef,
        links => undef,
        toDelete => undef,
        archive => undef,
        sieve => undef,
        objectId => undef,
        domainId => undef,
        groupDbDesc => undef,   # Pure description BD
        properties => undef,    # Propriétés calculées
        groupLinks => undef,    # Les relations avec d'autres entités
        objectclass => undef,
        dnPrefix => undef,
        dnValue => undef
    );


    if( !defined($links) || !defined($deleted) || !defined($groupId) ) {
        croak( "Usage: PACKAGE->new(LINKS, DELETED, GROUPID)" );

    }elsif( $groupId !~ /^\d+$/ ) {
        &OBM::toolBox::write_log( "[Entities::obmGroup]: identifiant de groupe incorrect", "W" );
        return undef;

    }else {
        $obmGroupAttr{objectId} = $groupId;
    }

    $obmGroupAttr{links} = $links;
    $obmGroupAttr{toDelete} = $deleted;

    $obmGroupAttr{type} = $OBM::Parameters::ldapConf::POSIXGROUPS;
    $obmGroupAttr{archive} = 0;
    $obmGroupAttr{sieve} = 0;

    # Définition de la représentation LDAP de ce type
    $obmGroupAttr{objectclass} = $OBM::Parameters::ldapConf::attributeDef->{$obmGroupAttr{"type"}}->{objectclass};
    $obmGroupAttr{dnPrefix} = $OBM::Parameters::ldapConf::attributeDef->{$obmGroupAttr{"type"}}->{dn_prefix};
    $obmGroupAttr{dnValue} = $OBM::Parameters::ldapConf::attributeDef->{$obmGroupAttr{"type"}}->{dn_value};

    bless( \%obmGroupAttr, $self );
}


sub getEntity {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;

    my $groupId = $self->{objectId};
    if( !defined($groupId) ) {
        &OBM::toolBox::write_log( '[Entities::obmGroup]: aucun identifiant de groupe defini', 'W', 3 );
        return 0;
    }


    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( '[Entities::obmGroup]: connecteur a la base de donnee invalide', 'W', 3 );
        return 0;
    }

    if( !defined($domainDesc->{domain_id}) || ($domainDesc->{domain_id} !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( '[Entities::obmGroup]: description de domaine OBM incorrecte', 'W', 3 );
        return 0;

    }else {
        $self->{domainId} = $domainDesc->{domain_id};
    }


    my $uGroupTable = "UGroup";
    if( $self->getDelete() ) {
        $uGroupTable = "P_".$uGroupTable;
    }

    my $query = 'SELECT COUNT(*) FROM '.$uGroupTable.' WHERE group_id='.$groupId;

    my $queryResult;
    if( !defined(&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult )) ) {
        &OBM::toolBox::write_log( '[Entities::obmGroup]: probleme lors de l\'execution d\'une requete SQL : '.$dbHandler->err.' - '.$dbHandler->errstr, 'W', 2 );
        return 0;
    }

    my( $numRows ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( $numRows == 0 ) {
        &OBM::toolBox::write_log( '[Entities::obmGroup]: groupe inexistant d\'identifiant : '.$groupId, 'W', 3 );
        return 0;

    }elsif( $numRows > 1 ) {
        &OBM::toolBox::write_log( '[Entities::obmGroup]: plusieurs groupes d\'identifiant : '.$groupId.' ???', 'W', 3 );
        return 0;
    }


    # La requete a executer - obtention des informations sur le groupe
    $query = "SELECT * FROM ".$uGroupTable." WHERE group_id=".$groupId;

    # On execute la requete
    if( !defined(&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult )) ) {
        &OBM::toolBox::write_log( '[Entities::obmGroup]: probleme lors de l\'execution d\'une requete SQL : '.$dbHandler->err.' - '.$dbHandler->errstr, 'W', 2 );
        return 0;
    }

    # On range les resultats dans la structure de donnees des resultats
    my $dbGroupDesc = $queryResult->fetchrow_hashref();
    $queryResult->finish();

    # On stocke la description BD utile pour la MAJ des tables
    $self->{groupDbDesc} = $dbGroupDesc;

    if( $self->getDelete() ) {
        &OBM::toolBox::write_log( '[Entities::obmGroup]: suppression du groupe : '.$self->getEntityDescription(), 'W', 1 );
    }else {
        &OBM::toolBox::write_log( '[Entities::obmGroup]: gestion du groupe : '.$self->getEntityDescription(), 'W', 1 );
    }

    # On range les resultats calculés dans la structure de données dédiée
    $self->{'properties'}->{'group_domain'} = $domainDesc->{'domain_label'};


    SWITCH: {
        # Les adresses du groupe
        my $return = $self->makeEntityEmail( $dbGroupDesc->{'group_email'}, $domainDesc->{'domain_name'}, $domainDesc->{'domain_alias'} );
        if( $return == 0 ) {
            $self->{'properties'}->{'group_mailperms'} = 0;
            last SWITCH;
        }

        $self->{'properties'}->{'group_mailperms'} = 1;
    }


    # Les données Samba
    if( $OBM::Parameters::common::obmModules->{'samba'} && $dbGroupDesc->{'group_samba'} ) {
        $self->{'properties'}->{'group_samba'} = 1;
        $self->{'properties'}->{'group_samba_sid'} =
        &OBM::Samba::utils::getGroupSID( $domainDesc->{'domain_samba_sid'}, $dbGroupDesc->{'group_gid'} );
        if( !defined($self->{'properties'}->{'group_samba_sid'}) ) {
            &OBM::toolBox::write_log( '[Entities::obmGroup]: annulation du droit Samba du groupe : '.$self->getEntityDescription().' - SID non definit', 'W', 2 );
            $self->{'properties'}->{'group_samba'} = 0;
        }else {
            $self->{'properties'}->{'group_samba_type'} = 2;
            $self->{'properties'}->{'group_samba_name'} = $dbGroupDesc->{'group_name'};
        }
    }else {
        $self->{'properties'}->{'group_samba'} = 0;
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

    my $dbEntry = $self->{groupDbDesc};
    if( !defined($dbEntry) ) {
        return 0;
    }

    &OBM::toolBox::write_log( '[Entities::obmGroup]: MAJ du groupe '.$self->getEntityDescription().' dans les tables de production', 'W', 1 );


    # Champs de la BD qui ne sont pas mis à jour car champs références
    my $exceptFields = '^(group_id)$';

    # MAJ de l'entité dans la table de production
    my @updateFields;
    my @whereFields;
    while( my( $columnName, $columnValue ) = each(%{$dbEntry}) ) {
        if( $columnName =~ /$exceptFields/ ) {
            push( @whereFields, $columnName."=".$dbHandler->quote($columnValue) );
        }else {
            push( @updateFields, $columnName."=".$dbHandler->quote($columnValue) );
        }
    }

    my $query = 'UPDATE P_UGroup SET '.join( ', ', @updateFields ).' WHERE '.join( ' AND ', @whereFields );


    my $queryResult;
    my $result = &OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult );

    if( !defined($result) ) {
        &OBM::toolBox::write_log( '[Entities::obmGroup]: probleme a la mise a jour du groupe : '.$dbHandler->err.' - '.$dbHandler->errstr, 'W', 2 );
        return 0;

    }elsif( $result == 0 ) {
        my @fields;
        my @fieldsValues;
        while( my( $columnName, $columnValue ) = each(%{$dbEntry}) ) {
            push( @fields, $columnName );
            push( @fieldsValues, $dbHandler->quote($columnValue) );
        }

        $query = 'INSERT INTO P_UGroup ('.join( ', ', @fields ).') VALUES ('.join( ', ', @fieldsValues ).')'; 
        
        $result = &OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult );
        if( !defined($result) ) {
            &OBM::toolBox::write_log( '[Entities::obmGroup]: probleme a la mise a jour du groupe : '.$dbHandler->err.' - '.$dbHandler->errstr, 'W', 2 );
            return 0;
        }elsif( $result != 1 ) {
            &OBM::toolBox::write_log( '[Entities::obmGroup]: probleme a la mise a jour du groupe : groupe insere '.$result.' fois dans les tables de production !', 'W', 2 );
            return 0;
        }
    }

    &OBM::toolBox::write_log( '[Entities::obmGroup]: MAJ des tables de production reussie', 'W', 2 );

    return 1;
}


sub updateDbEntityLinks {
    my $self = shift;
    my( $dbHandler ) = @_;
    my $queryResult;

    if( !defined($dbHandler) ) {
        return 0;
    }

    &OBM::toolBox::write_log( '[Entities::obmGroup]: MAJ des liens du groupe '.$self->getEntityDescription().' dans les tables de production', 'W', 1 );

    # On supprime les liens actuels de la table de production des liens
    # utilisateurs/groupes
    my $query = 'DELETE FROM P_of_usergroup WHERE of_usergroup_group_id='.$self->{objectId};

    if( !defined(&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult )) ) {
        &OBM::toolBox::write_log( '[Entities::obmGroup]: probleme lors de l\'execution d\'une requete SQL : '.$dbHandler->err.' - '.$dbHandler->errstr, 'W', 2 );
        return 0;
    }


    # On copie les nouveaux droits
    $query = 'INSERT INTO P_of_usergroup SELECT * FROM of_usergroup WHERE of_usergroup_group_id='.$self->{objectId};

    if( !defined(&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult )) ) {
        &OBM::toolBox::write_log( '[Entities::obmGroup]: probleme lors de l\'execution d\'une requete SQL : '.$dbHandler->err.' - '.$dbHandler->errstr, 'W', 2 );
        return 0;
    }

    &OBM::toolBox::write_log( '[Entities::obmGroup]: MAJ des tables de production reussie', 'W', 2 );

    return 1;
}


sub getEntityLinks {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;

    # Récupération des membres du groupe
    $self->{groupLinks}->{group_users} = $self->_getGroupUsers( $dbHandler, $domainDesc, undef, undef );

    # Gestion des utilisateurs de la liste ayant droit au mail
    if( $self->{"properties"}->{group_mailperms} ) {
        $self->{groupLinks}->{group_contacts} = $self->_getGroupUsersMailEnable( $dbHandler, $domainDesc );
    }

    # Gestion des utilisateurs de la liste ayant le droit Samba
    if( $self->{"properties"}->{group_samba} ) {
        $self->{groupLinks}->{group_samba_users} = $self->_getGroupUsersSid( $dbHandler, $domainDesc );
    }

    # On précise que les liens de l'entité sont aussi à mettre à jour.
    $self->{links} = 1;

    return 1;
}


sub getEntityDescription {
    my $self = shift;
    my $dbEntry = $self->{groupDbDesc};
    my $entryProp = $self->{"properties"};
    my $description = "";


    if( defined($dbEntry->{group_name}) ) {
        $description .= "identifiant '".$dbEntry->{group_name}."'";
    }

    if( defined($entryProp->{group_domain}) ) {
        $description .= ", domaine '".$entryProp->{group_domain}."'";
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


sub _getGroupUsersMailEnable {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;

    my $loginList = $self->_getGroupUsers( $dbHandler, $domainDesc, undef, "AND i.userobm_mail_perms=1" );

    for( my $i=0; $i<=$#$loginList; $i++ ) {
        $loginList->[$i] .= "@".$domainDesc->{domain_name};
    }

    return $loginList;
}


sub _getGroupUsersSid {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;

    my $uidList = $self->_getGroupUsers( $dbHandler, $domainDesc, "userobm_uid", "AND i.userobm_samba_perms=1" );

    for( my $i=0; $i<=$#$uidList; $i++ ) {
        $uidList->[$i] = $domainDesc->{domain_samba_sid}."-".$uidList->[$i];
    }

    return $uidList;
}


sub _getGroupUsers {
    my $self = shift;
    my( $dbHandler, $domainDesc, $sqlResultColumn, $sqlRequest ) = @_;
    my $groupId = $self->{objectId};


    if( !defined($dbHandler) ) {
        return undef;
    }

    if( !defined($domainDesc) ) {
        return undef;
    }

    if( !defined($sqlResultColumn) ) {
        $sqlResultColumn = "userobm_login";
    }

    &OBM::toolBox::write_log( '[Entities::obmGroup]: obtention des utilisateurs du groupe : '.$self->getEntityDescription(), 'W', 1 );

    my $userObmTable = "UserObm";
    my $userObmGroupTable = "of_usergroup";
    if( $self->getDelete() ) {
        $userObmTable = "P_".$userObmTable;
        $userObmGroupTable = "P_".$userObmGroupTable;
    }

    # Récupération de la liste d'utilisateur de ce groupe id : $groupId.
    my $query = 'SELECT i.'.$sqlResultColumn.' FROM '.$userObmTable.' i, '.$userObmGroupTable.' j WHERE j.of_usergroup_group_id='.$groupId.' AND j.of_usergroup_user_id=i.userobm_id';

    if( defined( $sqlRequest ) && ($sqlRequest ne "") ) {
        $query .= " ".$sqlRequest;
    }

    # On exécute la requête
    my $queryResult;
    if( !defined(&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult )) ) {
        &OBM::toolBox::write_log( '[Entities::obmGroup]: probleme SQL lors de l\'obtention des utilisateurs du groupe : '.$queryResult->err.' - '.$dbHandler->errstr, 'W', 2 );
        return undef;
    }

    # On stocke le resultat dans le tableau des resultats
    my @tabResult;
    while( my( $userLogin ) = $queryResult->fetchrow_array ) {
        push( @tabResult, $userLogin );
    }

    &OBM::toolBox::write_log( '[Entities::obmGroup]: '.eval{$#tabResult+1}.' utilisateurs font parti du groupe', 'W', 3 );

    return \@tabResult;
}


sub getLdapDnPrefix {
    my $self = shift;
    my $dnPrefix = undef;

    if( defined($self->{dnPrefix}) && defined($self->{groupDbDesc}->{$self->{dnValue}}) ) {
        $dnPrefix = $self->{dnPrefix}."=".$self->{groupDbDesc}->{$self->{dnValue}};
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
        if( (lc($objectclass->[$i]) eq "sambagroupmapping") && !$entryProp->{group_samba} ) {
            push( @{$deletedObjectclass}, $objectclass->[$i] );
            next;
        }

        $realObjectClass{$objectclass->[$i]} = 1;
    }

    # Si le droit Samba est actif, on s'assure de la présence des classes
    # nécessaires - nécessaires pour les MAJ
    if( $entryProp->{group_samba} ) {
        $realObjectClass{sambaGroupMapping} = 1;
    }

    my @realObjectClass = keys(%realObjectClass);
    return \@realObjectClass;
}


sub createLdapEntry {
    my $self = shift;
    my ( $ldapEntry ) = @_;
    my $dbEntry = $self->{'groupDbDesc'};
    my $entryProp = $self->{'properties'};
    my $entryLinks = $self->{'groupLinks'};

    # Les paramètres nécessaires
    if( $dbEntry->{'group_name'} && $dbEntry->{'group_gid'} ) {
        $ldapEntry->add(
            objectClass => $self->getLdapObjectclass(),
            cn => $dbEntry->{'group_name'},
            gidNumber => $dbEntry->{'group_gid'}
        );

    }else {
        return 0;
    }

    # Les membres
    if( $self->isLinks() && $#{$entryLinks->{'group_users'}} != -1 ) {
        $ldapEntry->add( memberUid => $entryLinks->{'group_users'} );
    }

    # Les contacts
    if( $self->isLinks() && $#{$entryLinks->{'group_contacts'}} != -1 ) {
        $ldapEntry->add( mailBox => $entryLinks->{'group_contacts'} );
    }

    # La description
    if( $dbEntry->{'group_desc'} ) {
        $ldapEntry->add( description => $dbEntry->{'group_desc'} );       
    }

    # L'acces mail
    if( $entryProp->{'group_mailperms'} ) {
        $ldapEntry->add( mailAccess => 'PERMIT' );
    }else {
        $ldapEntry->add( mailAccess => 'REJECT' );
    }

    # Les adresses mails
    if( $entryProp->{'email'} ) {
        $ldapEntry->add( mail => $entryProp->{'email'} );
    }

    # Les adresses mails secondaires
    if( $entryProp->{'emailAlias'} ) {
        $ldapEntry->add( mailAlias => $entryProp->{'emailAlias'} );
    }
            
    # Le domaine
    if( $entryProp->{'group_domain'} ) {
        $ldapEntry->add( obmDomain => $entryProp->{'group_domain'} );
    }

    # Le SID du groupe
    if( $entryProp->{'group_samba_sid'} ) {
        $ldapEntry->add( sambaSID => $entryProp->{'group_samba_sid'} );
    }

    # Le type du groupe
    if( $entryProp->{'group_samba_type'} ) {
        $ldapEntry->add( sambaGroupType => $entryProp->{'group_samba_type'} );
    }

    # Le nom du groupe
    if( $entryProp->{'group_samba_name'} ) {
        $ldapEntry->add( displayName => $entryProp->{'group_samba_name'} );
    }

    # La liste des utilisateurs Samba
    if( $self->isLinks() && $#{$entryLinks->{'group_samba_users'}} != -1 ) {
        $ldapEntry->add( sambaSIDList => $entryLinks->{'group_samba_users'} );
    }

    return 1;
}


sub updateLdapEntryDn {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $update = 0;


    if( !defined($ldapEntry) ) {
        return 0;
    }


    return $update;
}


sub updateLdapEntry {
    my $self = shift;
    my( $ldapEntry, $objectclassDesc ) = @_;
    my $dbEntry = $self->{groupDbDesc};
    my $entryProp = $self->{"properties"};
    my $entryLinks = $self->{groupLinks};

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
            if( &OBM::Ldap::utils::modifyAttrList( undef, $ldapEntry, $deleteAttrs->[$i] ) ) {
                $update->setUpdate();
            }
        }
    }


    # verification du GID
    if( &OBM::Ldap::utils::modifyAttr( $dbEntry->{group_gid}, $ldapEntry, "gidNumber" ) ) {
        $update->setUpdate();
    }

    # La description
    if( &OBM::Ldap::utils::modifyAttr( $dbEntry->{group_desc}, $ldapEntry, "description" ) ) {
        $update->setUpdate();
    }

    # L'acces au mail
    if( $entryProp->{group_mailperms} && (&OBM::Ldap::utils::modifyAttr( "PERMIT", $ldapEntry, "mailAccess" )) ) {
        $update->setUpdate();

    }elsif( !$entryProp->{group_mailperms} && (&OBM::Ldap::utils::modifyAttr( "REJECT", $ldapEntry, "mailAccess" )) ) {
        $update->setUpdate();

    }

    # Le cas des alias mails
    if( &OBM::Ldap::utils::modifyAttrList( $entryProp->{"email"}, $ldapEntry, "mail" ) ) {
        $update->setUpdate();
    }

    # Le cas des alias mails secondaires
    if( &OBM::Ldap::utils::modifyAttrList( $entryProp->{"emailAlias"}, $ldapEntry, "mailAlias" ) ) {
        $update->setUpdate();
    }

    # Le domaine
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{group_domain}, $ldapEntry, "obmDomain") ) {
        $update->setUpdate();
    }

    # Le SID Samba
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{group_samba_sid}, $ldapEntry, "sambaSID" ) ) {
        $update->setUpdate();
    }

    # Le type Samba du groupe
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{group_samba_type}, $ldapEntry, "sambaGroupType" ) ) {
        $update->setUpdate();
    }

    # Le nom Samba affichable du groupe
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{group_samba_name}, $ldapEntry, "displayName" ) ) {
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
    my $entryLinks = $self->{groupLinks};


    if( !defined($ldapEntry) ) {
        return 0;
    }


    # Les membres du groupe
    if( &OBM::Ldap::utils::modifyAttrList( $entryLinks->{group_users}, $ldapEntry, "memberUid" ) ) {
        $update = 1;
    }

    # Le cas des contacts
    if( &OBM::Ldap::utils::modifyAttrList( $entryLinks->{group_contacts}, $ldapEntry, "mailBox" ) ) {
        $update = 1;
    }

    # La liste des utilisateurs Samba
    if( &OBM::Ldap::utils::modifyAttrList( $entryLinks->{group_samba_users}, $ldapEntry, "sambaSIDList" ) ) {
        $update = 1;
    }

    return $update;
}


sub dump {
    my $self = shift;
    my @desc;

    push( @desc, $self );
    
    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}


sub getMailboxName {
    my $self = shift;

    return undef;
}


sub getMailboxPartition {
    my $self = shift;

    return undef;
}


sub getMailboxSieve {
    my $self = shift;

    return $self->{sieve};
}
