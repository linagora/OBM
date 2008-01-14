package OBM::Entities::obmHost;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::common;
require OBM::Parameters::ldapConf;
require OBM::Ldap::utils;
require OBM::toolBox;
require OBM::dbUtils;
require OBM::passwd;
use URI::Escape;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);


sub new {
    my $self = shift;
    my( $links, $deleted, $hostId ) = @_;

    my %obmHostAttr = (
        type => undef,
        links => undef,
        toDelete => undef,
        sieve => undef,
        hostId => undef,
        domainId => undef,
        hostDbDesc => undef,        # Pure description BD
        hostDesc => undef,          # Propriétés calculées
        hostLinks => undef,         # Les relations avec d'autres entités
        objectclass => undef,
        dnPrefix => undef,
        dnValue => undef
    );


    if( !defined($links) || !defined($deleted) || !defined($hostId) ) {
        croak( "Usage: PACKAGE->new(LINKS, DELETED, HOSTID)" );

    }elsif( $hostId !~ /^\d+$/ ) {
        &OBM::toolBox::write_log( "[Entities::obmHost]: identifiant d'hote incorrect", "W" );
        return undef;
    }else {
        $obmHostAttr{"hostId"} = $hostId;
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
    my( $dbHandler, $domainDesc ) = @_;

    my $hostId = $self->{"hostId"};
    if( !defined($hostId) ) {
        &OBM::toolBox::write_log( "[Entities::obmHost]: aucun identifiant d'hote definit", "W" );
        return 0;
    }


    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( "[Entities::obmHost]: connecteur a la base de donnee invalide", "W" );
        return 0;
    }

    if( !defined($domainDesc->{"domain_id"}) || ($domainDesc->{"domain_id"} !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( "[Entities::obmHost]: description de domaine OBM incorrecte", "W" );
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
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Entities::obmHost]: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    my( $numRows ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( $numRows == 0 ) {
        &OBM::toolBox::write_log( "[Entities::obmHost]: pas d'hote d'identifiant : ".$hostId, "W" );
        return 0;
    }elsif( $numRows > 1 ) {
        &OBM::toolBox::write_log( "[Entities::obmHost]: plusieurs hotes d'identifiant : ".$hostId." ???", "W" );
        return 0;
    }

    # La requete a executer - obtention des informations sur l'hote
    $query = "SELECT * FROM ".$hostTable." WHERE host_id=".$hostId;

    # On execute la requete
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Entities::obmHost]: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    # On range les resultats dans la structure de donnees des resultats
    my $dbEntry = $queryResult->fetchrow_hashref();
    $queryResult->finish();

    # On stocke la description BD utile pour la MAJ des tables
    $self->{"hostDbDesc"} = $dbEntry;

    if( $self->getDelete() ) {
        &OBM::toolBox::write_log( "[Entities::obmHost]: gestion de l'hote supprime : ".$self->getEntityDescription(), "W" );
    }else {
        &OBM::toolBox::write_log( "[Entities::obmHost]: gestion de l'hote : ".$self->getEntityDescription(), "W" );
    }

    # On range les résultats calculés dans la structure de données dédiée
    $self->{hostDesc}->{host_domain} = $domainDesc->{domain_label};


    # Les données Samba
    if( $OBM::Parameters::common::obmModules->{samba} && $dbEntry->{host_samba} ) {
        $self->{hostDesc}->{host_samba} = 1;
        $self->{hostDesc}->{host_login} = $dbEntry->{host_name}."\$";
        $self->{hostDesc}->{host_samba_sid} = &OBM::Samba::utils::getUserSID( $domainDesc->{domain_samba_sid}, $dbEntry->{host_uid} );
        $self->{hostDesc}->{host_samba_group_sid} = &OBM::Samba::utils::getGroupSID( $domainDesc->{domain_samba_sid}, $dbEntry->{host_gid} );
        $self->{hostDesc}->{host_samba_flags} = "[W]";

        if( &OBM::passwd::getNTLMPasswd( $dbEntry->{host_name}, \$self->{hostDesc}->{"host_lm_passwd"}, \$self->{hostDesc}->{"host_nt_passwd"} ) ) {
            &OBM::toolBox::write_log( "[Entities::obmHost]: probleme lors de la generation du mot de passe de l'hote : ".$self->getEntityDescription(), "W" );
            return 0;
        }

    }else {
        $self->{hostDesc}->{host_samba} = 0;
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

    my $dbEntry = $self->{"hostDbDesc"};
    if( !defined($dbEntry) ) {
        return 0;
    }

    &OBM::toolBox::write_log( "[Entities::obmHost]: MAJ de l'hote '".$dbEntry->{"host_name"}."' dans les tables de production", "W" );

    # MAJ de l'entité dans la table de production
    my $query = "REPLACE INTO P_Host SET ";
    my $first = 1;
    while( my( $columnName, $columnValue ) = each(%{$dbEntry}) ) {
        if( !$first ) {
            $query .= ", ";
        }else {
            $first = 0;
        }

        $query .= $columnName."=".$dbHandler->quote($columnValue);
    }

    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Entities::obmHost]: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    return 1;
}


sub getEntityLinks {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;

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

    if( defined($self->{hostId}) ) {
        $description .= "ID BD '".$self->{hostId}."'";
    }

    if( defined($self->{type}) ) {
        $description .= ",type '".$self->{type}."'";
    }

    return $description;
}


sub setDelete {
    my $self = shift;

    $self->{"toDelete"} = 1;

    return 1;
}


sub getDelete {
    my $self = shift;

    return $self->{"toDelete"};
}


sub getArchive {
    my $self = shift;

    return 0;
}


sub isLinks {
    my $self = shift;

    return $self->{"links"};
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
    my $dbEntry = $self->{hostDbDesc};
    my $entryProp = $self->{hostDesc};
    my $entryLinks = $self->{hostLinks};

    # Les paramètres nécessaires
    if( $dbEntry->{host_name} ) {
        $ldapEntry->add(
            objectClass => $self->getLdapObjectclass(),
            cn => $dbEntry->{host_name}
        );

    }else {
        return 0;
    }

    # La description
    if( $dbEntry->{host_description} ) {
        $ldapEntry->add( description => to_utf8({ -string => $dbEntry->{host_description}, -charset => $defaultCharSet }) );
    }

    # L'adresse IP
    if( $dbEntry->{host_ip} ) {
        $ldapEntry->add( ipHostNumber => $dbEntry->{host_ip} );
    }

    # Le domaine OBM
    if( $entryProp->{host_domain} ) {
        $ldapEntry->add( obmDomain => to_utf8({ -string => $entryProp->{host_domain}, -charset => $defaultCharSet }) );
    }

    # Le nom windows
    if( $entryProp->{host_login} ) {
        $ldapEntry->add( uid => to_utf8({ -string => $entryProp->{host_login}, -charset => $defaultCharSet }) );
    }

    # Le SID de l'hôte
    if( $entryProp->{host_samba_sid} ) {
        $ldapEntry->add( sambaSID => to_utf8({ -string => $entryProp->{host_samba_sid}, -charset => $defaultCharSet }) );
    }

    # Le groupe de l'hôte
    if( $entryProp->{host_samba_group_sid} ) {
        $ldapEntry->add( sambaPrimaryGroupSID => to_utf8({ -string => $entryProp->{host_samba_group_sid}, -charset => $defaultCharSet }) );
    }

    # Les flags de l'hôte Samba
    if( $entryProp->{host_samba_flags} ) {
        $ldapEntry->add( sambaAcctFlags => to_utf8({ -string => $entryProp->{host_samba_flags}, -charset => $defaultCharSet }) );
    }

    # Les mots de passes windows
    if( $entryProp->{host_lm_passwd} ) {
        $ldapEntry->add( sambaLMPassword => $entryProp->{host_lm_passwd} );
    }
    if( $entryProp->{host_nt_passwd} ) {
        $ldapEntry->add( sambaNTPassword => $entryProp->{host_nt_passwd} );
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
    my $update = 0;
    my $dbEntry = $self->{hostDbDesc};
    my $entryProp = $self->{hostDesc};
    my $entryLinks = $self->{hostLinks};


    if( !defined($ldapEntry) ) {
        return 0;
    }


    # Vérification des objectclass
    my @deletedObjectclass;
    my $currentObjectclass = $self->getLdapObjectclass( $ldapEntry->get_value("objectClass", asref => 1), \@deletedObjectclass);
    if( &OBM::Ldap::utils::modifyAttrList( $currentObjectclass, $ldapEntry, "objectClass" ) ) {
        $update = 1;
    }

    if( $#deletedObjectclass >= 0 ) {
        # Pour les schémas LDAP supprimés, on détermine les attributs à
        # supprimer.
        # Uniquement ceux qui ne sont pas utilisés par d'autres objets.
        my $deleteAttrs = &OBM::Ldap::utils::diffObjectclassAttrs(\@deletedObjectclass, $currentObjectclass, $objectclassDesc);

        for( my $i=0; $i<=$#$deleteAttrs; $i++ ) {
            if( &OBM::Ldap::utils::modifyAttrList( undef, $ldapEntry, $deleteAttrs->[$i] ) ) {
                $update = 1;
            }
        }
    }


    # La description
    if( &OBM::Ldap::utils::modifyAttr( $dbEntry->{"host_description"}, $ldapEntry, "description" ) ) {
        $update = 1;
    }

    # L'adresse IP
    if( &OBM::Ldap::utils::modifyAttr( $dbEntry->{"host_ip"}, $ldapEntry, "ipHostNumber" ) ) {
        $update = 1;
    }

    # Le domaine OBM
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{"host_domain"}, $ldapEntry, "obmDomain" ) ) {
        $update = 1;
    }

    # Le nom windows
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{host_login}, $ldapEntry, "uid" ) ) {
        $update = 1;
    }

    if( defined($entryProp->{host_samba_sid}) ) {
        my @currentLdapHostSambaSid = $ldapEntry->get_value( "sambaSID", asref => 1 );
        if( $#currentLdapHostSambaSid < 0 ) {
            # Si le SID de l'hôte n'est pas actuellement dans LDAP mais est dans
            # la description de l'hôte, c'est qu'on vient de ré-activer le droit
            # samba de l'hôte. Il faut donc placer les mots de passes.
            if( &OBM::Ldap::utils::modifyAttr( $entryProp->{host_lm_passwd}, $ldapEntry, "sambaLMPassword" ) ) {
                &OBM::Ldap::utils::modifyAttr( $entryProp->{host_nt_passwd}, $ldapEntry, "sambaNTPassword" );
                $update = 1;
            }
        }
    }

    # Le SID de l'hôte
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{host_samba_sid}, $ldapEntry, "sambaSID" ) ) {
        $update = 1;
    }

    # Le groupe de l'hôte
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{host_samba_group_sid}, $ldapEntry, "sambaPrimaryGroupSID" ) ) {
        $update = 1;
    }

    # Les flags de l'hôte Samba
    if( &OBM::Ldap::utils::modifyAttr( $entryProp->{host_samba_flags}, $ldapEntry, "sambaAcctFlags" ) ) {
        $update = 1;
    }


    if( $self->isLinks() ) {
        $update = $update || $self->updateLdapEntryLinks( $ldapEntry );
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
