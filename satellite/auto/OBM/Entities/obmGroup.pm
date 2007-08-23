package OBM::Entities::obmGroup;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
require OBM::Ldap::utils;
require OBM::toolBox;
require OBM::dbUtils;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);


sub new {
    my $self = shift;
    my( $links, $deleted, $groupId ) = @_;

    my %obmGroupAttr = (
        type => undef,
        typeDesc => undef,
        links => undef,
        toDelete => undef,
        archive => undef,
        sieve => undef,
        groupId => undef,
        domainId => undef,
        groupDesc => undef,
        groupDbDesc => undef
    );


    if( !defined($links) || !defined($deleted) || !defined($groupId) ) {
        croak( "Usage: PACKAGE->new(LINKS, DELETED, GROUPID)" );

    }elsif( $groupId !~ /^\d+$/ ) {
        &OBM::toolBox::write_log( "obmGroup: identifiant de groupe incorrect", "W" );
        return undef;

    }else {
        $obmGroupAttr{"groupId"} = $groupId;
    }

    $obmGroupAttr{"links"} = $links;
    $obmGroupAttr{"toDelete"} = $deleted;

    $obmGroupAttr{"type"} = $POSIXGROUPS;
    $obmGroupAttr{"typeDesc"} = $attributeDef->{$obmGroupAttr{"type"}};
    $obmGroupAttr{"archive"} = 0;
    $obmGroupAttr{"sieve"} = 0;

    bless( \%obmGroupAttr, $self );
}


sub getEntity {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;
    my $groupId = $self->{"groupId"};


    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( "obmGroup: connecteur a la base de donnee invalide", "W" );
        return 0;
    }

    if( !defined($domainDesc->{"domain_id"}) || ($domainDesc->{"domain_id"} !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( "obmGroup: description de domaine OBM incorrecte", "W" );
        return 0;

    }else {
        # On positionne l'identifiant du domaine de l'entité
        $self->{"domainId"} = $domainDesc->{"domain_id"};
    }


    my $uGroupTable = "UGroup";
    if( $self->getDelete() ) {
        $uGroupTable = "P_".$uGroupTable;
    }

    my $query = "SELECT COUNT(*) FROM ".$uGroupTable." WHERE group_privacy=0 AND group_id=".$groupId;

    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmGroup: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    my( $numRows ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( $numRows == 0 ) {
        &OBM::toolBox::write_log( "obmGroup: groupe prive ou inexistant d'identifiant : ".$groupId, "W" );
        return 1;

    }elsif( $numRows > 1 ) {
        &OBM::toolBox::write_log( "obmGroup: plusieurs groupes d'identifiant : ".$groupId." ???", "W" );
        return 0;

    }


    # La requete a executer - obtention des informations sur le groupe
    $query = "SELECT * FROM ".$uGroupTable." WHERE group_privacy=0 AND group_id=".$groupId;

    # On execute la requete
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmGroup: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    # On range les resultats dans la structure de donnees des resultats
    my $dbGroupDesc = $queryResult->fetchrow_hashref();
    $queryResult->finish();

    # On stocke la description BD utile pour la MAJ des tables
    $self->{"groupDbDesc"} = $dbGroupDesc;

    if( $self->getDelete() ) {
        &OBM::toolBox::write_log( "obmGroup: suppression du groupe : '".$dbGroupDesc->{"group_name"}."', domaine '".$domainDesc->{"domain_label"}."'", "W" );

    }else {
        &OBM::toolBox::write_log( "obmGroup: gestion du groupe : '".$dbGroupDesc->{"group_name"}."', domaine '".$domainDesc->{"domain_label"}."'", "W" );
    
    }

    # On range les resultats dans la structure de donnees des resultats
    $self->{"groupDesc"}->{"group_gid"} = $dbGroupDesc->{"group_gid"};
    $self->{"groupDesc"}->{"group_name"} = $dbGroupDesc->{"group_name"};
    $self->{"groupDesc"}->{"group_desc"} = $dbGroupDesc->{"group_desc"};
    $self->{"groupDesc"}->{"group_domain"} = $domainDesc->{"domain_label"};

    if( $dbGroupDesc->{"group_email"} ) {
        $self->{"groupDesc"}->{"group_mailperms"} = 1;

        # L'adresse du groupe
        my $group_email = lc($dbGroupDesc->{"group_email"});
        push( @{$self->{"groupDesc"}->{"group_email"}}, $group_email."@".$domainDesc->{"domain_name"} );

        for( my $j=0; $j<=$#{$domainDesc->{"domain_alias"}}; $j++ ) {
            push( @{$self->{"groupDesc"}->{"group_email_alias"}}, $group_email."@".$domainDesc->{"domain_alias"}->[$j] );
        }

    }else {
        $self->{"groupDesc"}->{"group_mailperms"} = 0;
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

    my $dbGroupDesc = $self->{"groupDbDesc"};
    if( !defined($dbGroupDesc) ) {
        return 0;
    }

    &OBM::toolBox::write_log( "obmGroup: MAJ du groupe '".$dbGroupDesc->{"group_name"}."' dans les tables de production", "W" );

    # MAJ de l'entité dans la table de production
    my $query = "DELETE FROM P_UGroup WHERE group_id=".$self->{"groupId"};
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmGroup: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    # Obtention des noms de colonnes de la table
    $query = "SELECT * FROM P_UGroup WHERE 0=1";
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmGroup: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }
    my $columnList = $queryResult->{NAME};

    $query = "INSERT INTO P_UGroup SET ";
    my $first = 1;
    for( my $i=0; $i<=$#$columnList; $i++ ) {
        if( !$first ) {
            $query .= ", ";
        }else {
            $first = 0;
        }

        $query .= $columnList->[$i]."=".$dbHandler->quote($dbGroupDesc->{$columnList->[$i]});
    }

    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmGroup: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    # Les liens
    if( $self->isLinks() ) {
        # On supprime les liens actuels de la table de production des liens
        # utilisateurs/groupes
        $query = "DELETE FROM P_UserObmGroup WHERE userobmgroup_group_id=".$self->{"groupId"};
        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "obmUser: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
            return 0;
        }


        # On copie les nouveaux droits
        $query = "SELECT * FROM UserObmGroup WHERE userobmgroup_group_id=".$self->{"groupId"};

        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "obmGroup: probleme lors de l'execution d' une requete SQL : ".$dbHandler->err, "W" );
            return 0;
        }

        while( my $rowHash = $queryResult->fetchrow_hashref() ) {
            $query = "INSERT INTO P_UserObmGroup SET ";

            my $first = 1;
            while( my( $column, $value ) = each(%{$rowHash}) ) {
                if( !$first ) {
                    $query .= ", ";
                }else {
                    $first = 0;
                }

                $query .= $column."=".$dbHandler->quote($value);
            }

            my $queryResult2;
            if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult2 ) ) {
                &OBM::toolBox::write_log( "obmGroup: probleme lors de l'executio n d'une requete SQL : ".$dbHandler->err, "W" );
                return 0;
             }
        }

        # On supprime les liens actuels de la table de production des liens
        # utilisateurs/groupes
        $query = "DELETE FROM P_GroupGroup WHERE groupgroup_parent_id=".$self->{"groupId"};
        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "obmUser: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
            return 0;
        }


        # On copie les nouveaux droits
        $query = "SELECT * FROM GroupGroup WHERE groupgroup_parent_id=".$self->{"groupId"};

        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "obmGroup: probleme lors de l'execution d' une requete SQL : ".$dbHandler->err, "W" );
            return 0;
        }

        while( my $rowHash = $queryResult->fetchrow_hashref() ) {
            $query = "INSERT INTO P_GroupGroup SET ";

            my $first = 1;
            while( my( $column, $value ) = each(%{$rowHash}) ) {
                if( !$first ) {
                    $query .= ", ";
                }else {
                    $first = 0;
                }

                $query .= $column."=".$dbHandler->quote($value);
            }

            my $queryResult2;
            if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult2 ) ) {
                &OBM::toolBox::write_log( "obmGroup: probleme lors de l'executio n d'une requete SQL : ".$dbHandler->err, "W" );
                return 0;
             }
        }
    }

    return 1;
}



sub getEntityLinks {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;

    # Récupération des membres du groupe
    $self->{"groupDesc"}->{"group_users"} = $self->_getGroupUsers( $dbHandler, $domainDesc, undef );

    if( $self->{"groupDesc"}->{"group_mailperms"} ) {
        # Gestion des utilisateurs de la liste ayant droit au mail
        $self->{"groupDesc"}->{"group_contacts"} = $self->_getGroupUsersMailEnable( $dbHandler, $domainDesc );
        for( my $j=0; $j<=$#{$self->{"groupDesc"}->{"group_contacts"}}; $j++ ) {
            $self->{"groupDesc"}->{"group_contacts"}->[$j] .= "@".$domainDesc->{"domain_name"};
        }
    }

    # On précise que les liens de l'entité sont aussi à mettre à jour.
    $self->{"links"} = 1;

    return 1;
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

    return $self->{"archive"};
}


sub isLinks {
    my $self = shift;

    return $self->{"links"}
}


sub _getGroupUsersMailEnable {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;

    return $self->_getGroupUsers( $dbHandler, $domainDesc, "AND i.userobm_mail_perms=1" );
}


sub _getGroupUsers {
    my $self = shift;
    my( $dbHandler, $domainDesc, $sqlRequest ) = @_;
    my $groupId = $self->{"groupId"};


    if( !defined($dbHandler) ) {
        return undef;
    }

    if( !defined($domainDesc) ) {
        return undef;
    }


    my $userObmTable = "UserObm";
    my $userObmGroupTable = "UserObmGroup";
    my $groupGroupTable = "GroupGroup";
    if( $self->getDelete() ) {
        $userObmTable = "P_".$userObmTable;
        $userObmGroupTable = "P_".$userObmGroupTable;
        $groupGroupTable = "P_".$groupGroupTable;
    }

    # Recuperation de la liste d'utilisateur de ce groupe id : $groupId.
    my $query = "SELECT i.userobm_login FROM ".$userObmTable." i, ".$userObmGroupTable." j WHERE j.userobmgroup_group_id=".$groupId." AND j.userobmgroup_userobm_id=i.userobm_id";

    if( defined( $sqlRequest ) && ($sqlRequest ne "") ) {
        $query .= " ".$sqlRequest;
    }
    
    # On execute la requete
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmGroup: probleme SQL lors de l'obtention des utilisateurs du groupe : ".$queryResult->err, "W" );
        return undef;
    }

    # On stocke le resultat dans le tableau des resultats
    my @tabResult;
    while( my( $userLogin ) = $queryResult->fetchrow_array ) {
        push( @tabResult, $userLogin );
    }

    # Recuperation de la liste des groupes du groupe id : $groupId.
    $query = "SELECT groupgroup_child_id FROM ".$groupGroupTable." WHERE groupgroup_parent_id=".$groupId;

    # On execute la requete
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmGroup: probleme SQL lors de l'obtention des utilisateurs du groupe : ".$queryResult->err, "W" );
        return undef;
    }

    # On traite les resultats
    while( my( $groupGroupId ) = $queryResult->fetchrow_array ) {
        my $userGroupTmp = $self->_getGroupUsers( $groupGroupId, $dbHandler, $sqlRequest );

        for( my $i=0; $i<=$#$userGroupTmp; $i++ ) {
            my $j =0;
            while( ($j<=$#tabResult) && ($$userGroupTmp[$i] ne $tabResult[$j]) ) {
                $j++;
            }

            if( $j>$#tabResult ) {
                push( @tabResult, $$userGroupTmp[$i] );
            }
        }
    }
    
    return \@tabResult;
}


sub getLdapDnPrefix {
    my $self = shift;
    my $dnPrefix = undef;

    if( defined($self->{"typeDesc"}->{"dn_prefix"}) && defined($self->{"groupDesc"}->{$self->{"typeDesc"}->{"dn_value"}}) ) {
        $dnPrefix = $self->{"typeDesc"}->{"dn_prefix"}."=".$self->{"groupDesc"}->{$self->{"typeDesc"}->{"dn_value"}};
    }

    return $dnPrefix;
}


sub createLdapEntry {
    my $self = shift;
    my ( $ldapEntry ) = @_;
    my $entry = $self->{"groupDesc"};

    # Les parametres nécessaires
    if( $entry->{"group_name"} && $entry->{"group_gid"} ) {
        $ldapEntry->add(
            objectClass => $self->{"typeDesc"}->{"objectclass"},
            cn => to_utf8({ -string => $entry->{"group_name"}, -charset => $defaultCharSet }),
            gidNumber => $entry->{"group_gid"}
        );

    }else {
        return 0;
    }

    # Les membres
    if( $self->isLinks() && $#{$entry->{"group_users"}} != -1 ) {
        $ldapEntry->add( memberUid => $entry->{"group_users"} );
    }

    # Les contacts
    if( $self->isLinks() && $#{$entry->{"group_contacts"}} != -1 ) {
        $ldapEntry->add( mailBox => $entry->{"group_contacts"} );
    }

    # La description
    if( $entry->{"group_desc"} ) {
        $ldapEntry->add( description => to_utf8({ -string => $entry->{"group_desc"}, -charset => $defaultCharSet }) );       
    }

    # L'acces mail
    if( ($entry->{"group_email"}) && ($entry->{"group_mailperms"}) ) {
        $ldapEntry->add( mailAccess => "PERMIT" );
    }else {
        $ldapEntry->add( mailAccess => "REJECT" );
    }

    # Les adresses mails
    if( $#{$entry->{"group_email"}} != -1 ) {
        $ldapEntry->add( mail => $entry->{"group_email"} );
    }

    # Les adresses mails secondaires
    if( $#{$entry->{"group_email_alias"}} != -1 ) {
        $ldapEntry->add( mailAlias => $entry->{"group_email_alias"} );
    }
            
    # Le domaine
    if( $entry->{"group_domain"} ) {
        $ldapEntry->add( obmDomain => to_utf8({ -string => $entry->{"group_domain"}, -charset => $defaultCharSet }) );
    }

    return 1;
}


sub updateLdapEntry {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $entry = $self->{"groupDesc"};
    my $update = 0;

    # verification du GID
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"group_gid"}, $ldapEntry, "gidNumber" ) ) {
        $update = 1;
    }

    # La description
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"group_desc"}, $ldapEntry, "description" ) ) {
        $update = 1;
    }

    # L'acces au mail
    if( $entry->{"group_mailperms"} && (&OBM::Ldap::utils::modifyAttr( "PERMIT", $ldapEntry, "mailAccess" )) ) {
        $update = 1;

    }elsif( !$entry->{"group_mailperms"} && (&OBM::Ldap::utils::modifyAttr( "REJECT", $ldapEntry, "mailAccess" )) ) {
        $update = 1;

    }

    # Le cas des alias mails
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"group_email"}, $ldapEntry, "mail" ) ) {
        $update = 1;
    }

    # Le cas des alias mails secondaires
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"group_email_alias"}, $ldapEntry, "mailAlias" ) ) {
        $update = 1;
    }

    # Le domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"group_domain"}, $ldapEntry, "obmDomain") ) {
        $update = 1;
    }

    # Les membres du groupe
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"group_users"}, $ldapEntry, "memberUid" ) ) {
        $update = 1;
    }

    # Le cas des contacts
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"group_contacts"}, $ldapEntry, "mailBox" ) ) {
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

    return $self->{"sieve"};
}
