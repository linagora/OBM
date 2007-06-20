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
    my( $incremental, $groupId ) = @_;

    my %obmGroupAttr = (
        type => undef,
        typeDesc => undef,
        incremental => undef,
        links => undef,
        toDelete => undef,
        archive => undef,
        sieve => undef,
        groupId => undef,
        domainId => undef,
        groupDesc => undef
    );


    if( !defined($groupId) ) {
        croak( "Usage: PACKAGE->new(INCR, GROUPID)" );

    }elsif( $groupId !~ /^\d+$/ ) {
        &OBM::toolBox::write_log( "obmGroup: identifiant d'utilisateur incorrect", "W" );
        return undef;

    }else {
        $obmGroupAttr{"groupId"} = $groupId;
    }

    if( $incremental ) {
        $obmGroupAttr{"incremental"} = 1;
        $obmGroupAttr{"links"} = 0;
    }else {
        $obmGroupAttr{"incremental"} = 0;
        $obmGroupAttr{"links"} = 1;
    }

    $obmGroupAttr{"type"} = $POSIXGROUPS;
    $obmGroupAttr{"typeDesc"} = $attributeDef->{$obmGroupAttr{"type"}};
    $obmGroupAttr{"toDelete"} = 0;
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


    my $query = "SELECT COUNT(*) FROM ".&OBM::dbUtils::getTableName("UGroup", $self->{"incremental"})." WHERE group_privacy=0 AND group_id=".$groupId;

    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmGroup: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    my( $numRows ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( $numRows == 0 ) {
        &OBM::toolBox::write_log( "obmGroup: pas de groupe d'identifiant : ".$groupId, "W" );
        return 0;

    }elsif( $numRows > 1 ) {
        &OBM::toolBox::write_log( "obmGroup: plusieurs groupes d'identifiant : ".$groupId." ???", "W" );
        return 0;

    }


    # La requete a executer - obtention des informations sur l'utilisateur
    $query = "SELECT group_id, group_gid, group_name, group_desc, group_email, group_contacts FROM ".&OBM::dbUtils::getTableName("UGroup", $self->{"incremental"})." WHERE group_privacy=0 AND group_id=".$groupId;

    # On execute la requete
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmGroup: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    # On range les resultats dans la structure de donnees des resultats
    my( $group_id, $group_gid, $group_name, $group_desc, $group_email, $group_contacts ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    &OBM::toolBox::write_log( "obmGroup: gestion du groupe : '".$group_name."', domaine '".$domainDesc->{"domain_label"}."'", "W" );

    # On range les resultats dans la structure de donnees des resultats
    $self->{"groupDesc"}->{"group_gid"} = $group_gid;
    $self->{"groupDesc"}->{"group_name"} = $group_name;
    $self->{"groupDesc"}->{"group_desc"} = $group_desc;
    $self->{"groupDesc"}->{"group_domain"} = $domainDesc->{"domain_label"};

    if( $group_email ) {
        $self->{"groupDesc"}->{"group_mailperms"} = 1;

        # L'adresse du groupe
        $group_email = lc($group_email);
        push( @{$self->{"groupDesc"}->{"group_email"}}, $group_email."@".$domainDesc->{"domain_name"} );

        for( my $j=0; $j<=$#{$domainDesc->{"domain_alias"}}; $j++ ) {
            push( @{$self->{"groupDesc"}->{"group_email_alias"}}, $group_email."@".$domainDesc->{"domain_alias"}->[$j] );
        }

    }else {
        $self->{"groupDesc"}->{"group_mailperms"} = 0;
    }


    # Si nous ne sommes pas en mode incrémental, on charge aussi les liens de
    # cette entité
    if( $self->{"links"} ) {
        $self->getEntityLinks( $dbHandler, $domainDesc );
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


sub isIncremental {
    my $self = shift;

    return $self->{"incremental"};
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


    # Recuperation de la liste d'utilisateur de ce groupe id : $groupId.
    my $query = "SELECT i.userobm_login FROM ".&OBM::dbUtils::getTableName("UserObm", $self->{"incremental"})." i, ".&OBM::dbUtils::getTableName("UserObmGroup", $self->{"incremental"})." j WHERE j.userobmgroup_group_id=".$groupId." AND j.userobmgroup_userobm_id=i.userobm_id";

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
    $query = "SELECT groupgroup_child_id FROM ".&OBM::dbUtils::getTableName("GroupGroup", $self->{"incremental"})." WHERE groupgroup_parent_id=".$groupId;

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


sub getMailboxSieve {
    my $self = shift;

    return $self->{"sieve"};
}
