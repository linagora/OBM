package OBM::Entities::obmHost;

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
use URI::Escape;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);


sub new {
    my $self = shift;
    my( $links, $deleted, $hostId ) = @_;

    my %obmHostAttr = (
        type => undef,
        typeDesc => undef,
        links => undef,
        toDelete => undef,
        sieve => undef,
        hostId => undef,
        domainId => undef,
        hostDesc => undef,
        hostDbDesc => undef
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

    $obmHostAttr{"type"} = $DOMAINHOSTS;
    $obmHostAttr{"typeDesc"} = $attributeDef->{$obmHostAttr{"type"}};

    bless( \%obmHostAttr, $self );
}


sub getEntity {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;
    my $hostId = $self->{"hostId"};


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
    my $dbHostDesc = $queryResult->fetchrow_hashref();
    $queryResult->finish();

    # On stocke la description BD utile pour la MAJ des tables
    $self->{"hostDbDesc"} = $dbHostDesc;

    # Action effectuee
    if( $self->getDelete() ) {
        &OBM::toolBox::write_log( "[Entities::obmHost]: gestion de l'hote supprime '".$dbHostDesc->{"host_name"}."', domaine '".$domainDesc->{"domain_label"}."'", "W" );
    }else {
        &OBM::toolBox::write_log( "[Entities::obmHost]: gestion de l'hote '".$dbHostDesc->{"host_name"}."', domaine '".$domainDesc->{"domain_label"}."'", "W" );
    }

    # On stocke les informations utiles dans la structure de donnees des
    # resultats
    $self->{"hostDesc"} = $dbHostDesc;
    $self->{"hostDesc"}->{"host_domain"} = $domainDesc->{"domain_label"};


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

    my $dbHostDesc = $self->{"hostDbDesc"};
    if( !defined($dbHostDesc) ) {
        return 0;
    }

    &OBM::toolBox::write_log( "[Entities::obmHost]: MAJ de l'hote '".$dbHostDesc->{"host_name"}."' dans les tables de production", "W" );

    # MAJ de l'entité dans la table de production
    my $query = "REPLACE INTO P_Host SET ";
    my $first = 1;
    while( my( $columnName, $columnValue ) = each(%{$dbHostDesc}) ) {
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
    my $entry = $self->{"hostDesc"};
    my $description = "";


    if( defined($entry->{host_name}) ) {
        $description .= "identifiant '".$entry->{host_name}."'";
    }

    if( defined($entry->{host_domain}) ) {
        $description .= ", domaine '".$entry->{host_domain}."'";
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

    if( defined($self->{"typeDesc"}->{"dn_prefix"}) && defined($self->{"hostDesc"}->{$self->{"typeDesc"}->{"dn_value"}}) ) {
        $dnPrefix = $self->{"typeDesc"}->{"dn_prefix"}."=".$self->{"hostDesc"}->{$self->{"typeDesc"}->{"dn_value"}};
    }

    return $dnPrefix;
}


sub createLdapEntry {
    my $self = shift;
    my ( $ldapEntry ) = @_;
    my $entry = $self->{"hostDesc"};

    # Les paramètres nécessaires
    if( $entry->{"host_name"} ) {
        $ldapEntry->add(
            objectClass => $self->{"typeDesc"}->{"objectclass"},
            cn => $entry->{"host_name"}
        );

    }else {
        return 0;
    }

    # La description
    if( $entry->{"host_description"} ) {
        $ldapEntry->add( description => to_utf8({ -string => $entry->{"host_description"}, -charset => $defaultCharSet }) );
    }

    # L'adresse IP
    if( $entry->{"host_ip"} ) {
        $ldapEntry->add( ipHostNumber => $entry->{"host_ip"} );
    }

    # Le domaine OBM
    if( $entry->{"host_domain"} ) {
        $ldapEntry->add( obmDomain => to_utf8({ -string => $entry->{"host_domain"}, -charset => $defaultCharSet }) );
    }

    return 1;
}


sub updateLdapEntry {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $entry = $self->{"hostDesc"};
    my $update = 0;

    # La description
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"host_description"}, $ldapEntry, "description" ) ) {
        $update = 1;
    }

    # L'adresse IP
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"host_ip"}, $ldapEntry, "ipHostNumber" ) ) {
        $update = 1;
    }

    # Le domaine OBM
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"host_domain"}, $ldapEntry, "obmDomain" ) ) {
        $update = 1;
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
