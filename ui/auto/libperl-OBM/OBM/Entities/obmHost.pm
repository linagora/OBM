package OBM::Entities::obmHost;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Entities::commonEntities qw(
           getType
            setDelete
            getDelete
            getArchive
            isLinks
            getEntityId
            isMailActive
            getMailServerId
            updateLinkedEntity
            );
use OBM::Tools::commonMethods qw(_log dump);
use OBM::Parameters::common;
use OBM::Parameters::regexp;
require OBM::Parameters::ldapConf;
require OBM::Ldap::utils;
require OBM::Tools::obmDbHandler;
require OBM::passwd;
use URI::Escape;


sub new {
    my $self = shift;
    my( $links, $deleted, $hostId ) = @_;

    my %obmHostAttr = (
        type => undef,
        links => undef,
        toDelete => undef,
        sieve => undef,
        objectId => undef,
        domainId => undef,
        hostDbDesc => undef,        # Pure description BD
        hostDesc => undef,          # Propriétés calculées
        hostLinks => undef,         # Les relations avec d'autres entités
        objectclass => undef,
        dnPrefix => undef,
        dnValue => undef
    );


    if( !defined($links) || !defined($deleted) || !defined($hostId) ) {
        $self->_log( 'Usage: PACKAGE->new(LINKS, DELETED, HOSTID)', 1 );
        return undef;
    }elsif( $hostId !~ /^\d+$/ ) {
        $self->_log( 'identifiant d\'hote incorrect', 2 );
        return undef;
    }else {
        $obmHostAttr{"objectId"} = $hostId;
    }


    $obmHostAttr{"links"} = $links;
    $obmHostAttr{"toDelete"} = $deleted;
    $obmHostAttr{"sieve"} = 0;

    $obmHostAttr{"type"} = $OBM::Parameters::ldapConf::DOMAINHOSTS;

    # Définition de la représentation LDAP de ce type
    $obmHostAttr{objectclass} = $OBM::Parameters::ldapConf::attributeDef->{$obmHostAttr{"type"}}->{objectclass};
    $obmHostAttr{dnPrefix} = $OBM::Parameters::ldapConf::attributeDef->{$obmHostAttr{"type"}}->{dn_prefix};
    $obmHostAttr{dnValue} = $OBM::Parameters::ldapConf::attributeDef->{$obmHostAttr{"type"}}->{dn_value};

    bless( \%obmHostAttr, $self );
}


sub getEntity {
    my $self = shift;
    my( $domainDesc ) = @_;

    my $hostId = $self->{"objectId"};
    if( !defined($hostId) ) {
        $self->_log( 'aucun identifiant d\'hote definit', 3 );
        return 0;
    }


    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connecteur a la base de donnee invalide', 3 );
        return 0;
    }

    if( !defined($domainDesc->{"domain_id"}) || ($domainDesc->{"domain_id"} !~ /^\d+$/) ) {
        $self->_log( 'description de domaine OBM incorrecte', 3 );
        return 0;

    }else {
        # On positionne l'identifiant du domaine de l'entité
        $self->{"domainId"} = $domainDesc->{"domain_id"};
    }


    my $hostTable = "Host";
    if( $self->getDelete() ) {
        $hostTable = "P_".$hostTable;
    }


    my $query = "SELECT COUNT(*) FROM ".$hostTable." WHERE host_id=".$hostId;

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 0;
    }

    my( $numRows ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( $numRows == 0 ) {
        $self->_log( 'pas d\'hote d\'identifiant : '.$hostId, 3 );
        return 0;
    }elsif( $numRows > 1 ) {
        $self->_log( 'plusieurs hotes d\'identifiant : '.$hostId.' ???', 3 );
        return 0;
    }

    # La requête à exécuter - obtention des informations sur l'hôte
    $query = "SELECT * FROM ".$hostTable." WHERE host_id=".$hostId;

    # On exécute la requête
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 0;
    }

    # On range les resultats dans la structure de donnees des resultats
    my $dbEntry = $queryResult->fetchrow_hashref();
    $queryResult->finish();

    # On stocke la description BD utile pour la MAJ des tables
    $self->{"hostDbDesc"} = $dbEntry;

    if( $self->getDelete() ) {
        $self->_log( 'suppression de l\'hote : '.$self->getEntityDescription(), 1 );
    }else {
        $self->_log( 'gestion de l\'hote : '.$self->getEntityDescription(), 1 );
    }

    # On range les résultats calculés dans la structure de données dédiée
    $self->{hostDesc}->{host_domain} = $domainDesc->{domain_label};

    # Gestion de l'adresse IP de l'hôte
    if( !$dbEntry->{host_ip} || $dbEntry->{host_ip} !~ /$regexp_ip/ ) {
        $self->{hostDesc}->{host_ip} = "0.0.0.0";
    }else {
        $self->{hostDesc}->{host_ip} = $dbEntry->{host_ip};
    }

    # Les données Samba
    if( $OBM::Parameters::common::obmModules->{samba} && $dbEntry->{host_samba} ) {
        $self->{hostDesc}->{host_samba} = 1;
        $self->{hostDesc}->{host_login} = $dbEntry->{host_name}."\$";
        $self->{hostDesc}->{host_samba_sid} = &OBM::Samba::utils::getUserSID( $domainDesc->{domain_samba_sid}, $dbEntry->{host_uid} );
        $self->{hostDesc}->{host_samba_group_sid} = &OBM::Samba::utils::getGroupSID( $domainDesc->{domain_samba_sid}, $dbEntry->{host_gid} );
        $self->{hostDesc}->{host_samba_flags} = "[W]";

        if( &OBM::passwd::getNTLMPasswd( $dbEntry->{host_name}, \$self->{hostDesc}->{"host_lm_passwd"}, \$self->{hostDesc}->{"host_nt_passwd"} ) ) {
            $self->_log( 'probleme lors de la generation du mot de passe windows de l\'hote : '.$self->getEntityDescription(), 2 );
            return 0;
        }

    }else {
        $self->{hostDesc}->{host_samba} = 0;
    }


    # Si nous ne sommes pas en mode incrémental, on charge aussi les liens de
    # cette entité
    if( $self->isLinks() ) {
        $self->getEntityLinks( $domainDesc );
    }

    return 1;
}


sub updateDbEntity {
    my $self = shift;

    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        return 0;
    }

    my $dbEntry = $self->{'hostDbDesc'};
    if( !defined($dbEntry) ) {
        return 0;
    }

    $self->_log( 'MAJ de l\'hote '.$self->getEntityDescription().' dans les tables de production', 1 );

    # Champs de la BD qui ne sont pas mis à jour car champs références
    my $exceptFields = '^(host_id)$';

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

    my $query = 'UPDATE P_Host SET '.join( ', ', @updateFields ).' WHERE '.join( ' AND ', @whereFields );


    my $queryResult;
    my $result = $dbHandler->execQuery( $query, \$queryResult );
                                                                              
    if( !defined($result) ) {
        $self->_log( 'probleme a la mise a jour de l\'hote', 2 );
        return 0;

    }elsif( $result == 0 ) {
        my @fields;
        my @fieldsValues;
        while( my( $columnName, $columnValue ) = each(%{$dbEntry}) ) {
            push( @fields, $columnName );
            push( @fieldsValues, $dbHandler->quote($columnValue) );
        }

        $query = 'INSERT INTO P_Host ('.join( ', ', @fields ).') VALUES ('.join( ', ', @fieldsValues ).')';

        $result = $dbHandler->execQuery( $query, \$queryResult );
        if( !defined($result) ) {
            $self->_log( 'probleme a la mise a jour de l\'hote', 2 );
            return 0;
        }elsif( $result != 1 ) {
            $self->_log( 'probleme a la mise a jour de l\'hote : hote insere '.$result.' fois dans les tables de production !', 2 );
            return 0;
        }
    }

    $self->_log( 'MAJ des tables de production reussie', 2 );

    return 1;
}


sub updateDbEntityLinks {
    my $self = shift;

#    my $dbHandler = OBM::Tools::obmDbHandler->instance();
#    if( !defined($dbHandler) ) {
#        return 0;
#    }
#
#    $self->_log( "MAJ des liens de l'hote ".$self->getEntityDescription()." dans les tables de production", 1 );

    return 1;
}


sub getEntityLinks {
    my $self = shift;
    my( $domainDesc ) = @_;

    return 1;
}


sub getEntityDescription {
    my $self = shift;
    my $dbEntry = $self->{hostDbDesc};
    my $entryProp = $self->{hostDesc};
    my $description = "";


    if( defined($dbEntry->{host_name}) ) {
        $description .= "identifiant '".$dbEntry->{host_name}."'";
    }

    if( defined($entryProp->{host_domain}) ) {
        $description .= ", domaine '".$entryProp->{host_domain}."'";
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


sub getLdapDnPrefix {
    my $self = shift;
    my $dnPrefix = undef;

    if( defined($self->{dnPrefix}) && defined($self->{hostDbDesc}->{$self->{dnValue}}) ) {
        $dnPrefix = $self->{dnPrefix}."=".$self->{hostDbDesc}->{$self->{dnValue}};
    }

    return $dnPrefix;
}


sub getLdapObjectclass {
    my $self = shift;
    my($objectclass, $deletedObjectclass) = @_;
    my $entryProp = $self->{hostDesc};
    my %realObjectClass;

    if( !defined($objectclass) || (ref($objectclass) ne "ARRAY") ) {
        $objectclass = $self->{objectclass};
    }

    for( my $i=0; $i<=$#$objectclass; $i++ ) {
        if( (lc($objectclass->[$i]) eq "sambasamaccount") && !$entryProp->{host_samba} ) {
            push( @{$deletedObjectclass}, $objectclass->[$i] );
            next;
        }

        $realObjectClass{$objectclass->[$i]} = 1;
    }

    # Si le droit Samba est actif, on s'assure de la présence des classes
    # nécessaires - nécessaires pour les MAJ
    if( $entryProp->{host_samba} ) {
        $realObjectClass{sambaSamAccount} = 1;
    }

    my @realObjectClass = keys(%realObjectClass);
    return \@realObjectClass;
}


sub createLdapEntry {
    my $self = shift;
    my ( $ldapEntry ) = @_;
    my $dbEntry = $self->{'hostDbDesc'};
    my $entryProp = $self->{'hostDesc'};
    my $entryLinks = $self->{'hostLinks'};

    # Les paramètres nécessaires
    if( $dbEntry->{'host_name'} ) {
        $ldapEntry->add(
            objectClass => $self->getLdapObjectclass(),
            cn => $dbEntry->{'host_name'}
        );

    }else {
        return 0;
    }

    # La description
    if( $dbEntry->{'host_description'} ) {
        $ldapEntry->add( description => $dbEntry->{'host_description'} );
    }

    # L'adresse IP
    if( $entryProp->{'host_ip'} ) {
        $ldapEntry->add( ipHostNumber => $entryProp->{'host_ip'} );
    }

    # Le domaine OBM
    if( $entryProp->{'host_domain'} ) {
        $ldapEntry->add( obmDomain => $entryProp->{'host_domain'} );
    }

    # Le nom windows
    if( $entryProp->{'host_login'} ) {
        $ldapEntry->add( uid => $entryProp->{'host_login'} );
    }

    # Le SID de l'hôte
    if( $entryProp->{'host_samba_sid'} ) {
        $ldapEntry->add( sambaSID => $entryProp->{'host_samba_sid'} );
    }

    # Le groupe de l'hôte
    if( $entryProp->{'host_samba_group_sid'} ) {
        $ldapEntry->add( sambaPrimaryGroupSID => $entryProp->{'host_samba_group_sid'} );
    }

    # Les flags de l'hôte Samba
    if( $entryProp->{'host_samba_flags'} ) {
        $ldapEntry->add( sambaAcctFlags => $entryProp->{'host_samba_flags'} );
    }

    # Les mots de passes windows
    if( $entryProp->{'host_lm_passwd'} ) {
        $ldapEntry->add( sambaLMPassword => $entryProp->{'host_lm_passwd'} );
    }
    if( $entryProp->{'host_nt_passwd'} ) {
        $ldapEntry->add( sambaNTPassword => $entryProp->{'host_nt_passwd'} );
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
    my $dbEntry = $self->{hostDbDesc};
    my $entryProp = $self->{hostDesc};
    my $entryLinks = $self->{hostLinks};

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


    # La description
    if( &OBM::Ldap::utils::modifyAttr( $dbEntry->{"host_description"}, $ldapEntry, "description" ) ) {
        $update->setUpdate();
    }

    # L'adresse IP
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{"host_ip"}, $ldapEntry, "ipHostNumber" ) ) {
        $update->setUpdate();
    }

    # Le domaine OBM
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{"host_domain"}, $ldapEntry, "obmDomain" ) ) {
        $update->setUpdate();
    }

    # Le nom windows
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{host_login}, $ldapEntry, "uid" ) ) {
        $update->setUpdate();
    }

    if( defined($entryProp->{host_samba_sid}) ) {
        my @currentLdapHostSambaSid = $ldapEntry->get_value( "sambaSID", asref => 1 );
        if( $#currentLdapHostSambaSid < 0 ) {
            # Si le SID de l'hôte n'est pas actuellement dans LDAP mais est dans
            # la description de l'hôte, c'est qu'on vient de ré-activer le droit
            # samba de l'hôte. Il faut donc placer les mots de passes.
            if( &OBM::Ldap::utils::modifyAttr( $entryProp->{host_lm_passwd}, $ldapEntry, "sambaLMPassword" ) ) {
                &OBM::Ldap::utils::modifyAttr( $entryProp->{host_nt_passwd}, $ldapEntry, "sambaNTPassword" );
                $update->setUpdate();
            }
        }
    }

    # Le SID de l'hôte
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{host_samba_sid}, $ldapEntry, "sambaSID" ) ) {
        $update->setUpdate();
    }

    # Le groupe de l'hôte
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{host_samba_group_sid}, $ldapEntry, "sambaPrimaryGroupSID" ) ) {
        $update->setUpdate();
    }

    # Les flags de l'hôte Samba
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{host_samba_flags}, $ldapEntry, "sambaAcctFlags" ) ) {
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
    my $entryLinks = $self->{hostLinks};

    if( !defined($ldapEntry) ) {
        return 0;
    }

    return $update;
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

    return return $self->{"sieve"};
}
