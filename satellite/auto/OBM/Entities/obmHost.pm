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
    my $self =shift;
    my( $links, $deleted, $hostId ) = @_;

    my %obmHostAttr = (
        type => undef,
        typeDesc => undef,
        links => undef,
        toDelete => undef,
        hostId => undef,
        domainId => undef,
        hostDesc => undef,
        hostBdDesc => undef
    );


    if( !defined($links) || !defined($deleted) || !defined($hostId) ) {
        croak( "Usage: PACKAGE->new(LINKS, DELETED, HOSTID)" );

    }elsif( $hostId !~ /^\d+$/ ) {
        &OBM::toolBox::write_log( "obmHost: identifiant d'hote incorrect", "W" );
        return undef;
    }else {
        $obmHostAttr{"hostId"} = $hostId;
    }


    $obmHostAttr{"links"} = $links;
    $obmHostAttr{"toDelete"} = $deleted;

    $obmHostAttr{"type"} = $SAMBAHOSTS;
    $obmHostAttr{"typeDesc"} = $attributeDef->{$obmHostAttr{"type"}};

    bless( \%obmHostAttr, $self );
}


sub getEntity {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;
    my $hostId = $self->{"hostId"};


    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( "obmHost: connecteur a la base de donnee invalide", "W" );
        return 0;
    }

    if( !defined($domainDesc->{"domain_id"}) || ($domainDesc->{"domain_id"} !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( "obmHost: description de domaine OBM incorrecte", "W" );
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
        &OBM::toolBox::write_log( "obmUser: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    my( $numRows ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( $numRows == 0 ) {
        &OBM::toolBox::write_log( "obmHost: pas d'hote d'identifiant : ".$hostId, "W" );
        return 0;
    }elsif( $numRows > 1 ) {
        &OBM::toolBox::write_log( "obmHost: plusieurs hotes d'identifiant : ".$hostId." ???", "W" );
        return 0;
    }

    # La requete a executer - obtention des informations sur l'hote
    $query = "SELECT * FROM ".$hostTable." WHERE host_id=".$hostId;

    # On execute la requete
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmUser: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    # On range les resultats dans la structure de donnees des resultats
    my $dbHostDesc = $queryResult->fetchrow_hashref();
    $queryResult->finish();

    # On stocke la description BD utile pour la MAJ des tables
    $self->{"hostBdDesc"} = $dbHostDesc;


    # Si nous ne sommes pas en mode incrémental, on charge aussi les liens de
    # cette entité
    if( $self->isLinks() ) {
        $self->getEntityLinks( $dbHandler, $domainDesc );
    }
}


sub updateDbEntity {
    my $self = shift;
    my( $dbHandler ) = @_;

    if( !defined($dbHandler) ) {
        return 0;
    }

    my $dbHostDesc = $self->{"hostBdDesc"};
    if( !defined($dbHostDesc) ) {
        return 0;
    }

    &OBM::toolBox::write_log( "obmHost: MAJ de l'hote '".$dbHostDesc->{"host_name"}."', domaine ".." dans les tables de production", "W" );

    # MAJ de l'entité dans la table de production
    my $query = "DELETE FROM P_Host WHERE host_id=".$self->{"hostId"};
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmHost: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    # Obtention des noms de colonnes de la table
    $query = "SELECT * FROM P_Host WHERE 0=1";
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmHost: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }
    my $columnList = $queryResult->{NAME};

    $query = "INSERT INTO P_Host SET ";
    my $first = 1;
    for( my $i=0; $i<=$#$columnList; $i++ ) {
        if( !$first ) {
            $query .= ", ";
        }else {
            $first = 0;
        }

        $query .= $columnList->[$i]."=".$dbHandler->quote($dbHostDesc->{$columnList->[$i]});
    }

    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmMailshare: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    return 1;
}


sub getEntityLinks {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;

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

    return 0;
}


sub isLinks {
    my $self = shift;

    return $self->{"links"};
}
