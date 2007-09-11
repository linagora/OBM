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


    # Obtention des domaines pour lesquels cet hôte est serveur SMTP entrant
    $query = "select distinct(i.domain_label) FROM Domain i, DomainMailServer j, MailServer k WHERE k.mailserver_host_id=".$self->{"hostDesc"}->{"host_id"}." AND k.mailserver_id=j.domainmailserver_mailserver_id AND j.domainmailserver_role='smtp-in' AND j.domainmailserver_domain_id=i.domain_id";
#    $query = "SELECT i.domain_label from Domain i, MailServer j WHERE i.domain_mail_server_id=j.mailserver_id AND j.mailserver_host_id=".$hostId;

    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Entities::obmHost]: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    while( my( $obmDomain ) = $queryResult->fetchrow_array() ) {
        push( @{$self->{"hostDesc"}->{"smtp-in_domain"}}, $obmDomain );
    }

    # Obtention des domaines pour lesquels cet hôte est serveur de courrier
    # IMAP
    $query = "select distinct(i.domain_label) FROM Domain i, DomainMailServer j, MailServer k WHERE k.mailserver_host_id=".$self->{"hostDesc"}->{"host_id"}." AND k.mailserver_id=j.domainmailserver_mailserver_id AND j.domainmailserver_role='imap' AND j.domainmailserver_domain_id=i.domain_id";

    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Entities::obmHost]: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    while( my( $obmDomain ) = $queryResult->fetchrow_array() ) {
        push( @{$self->{"hostDesc"}->{"cyrus_domain"}}, $obmDomain );
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

    my $dbHostDesc = $self->{"hostDbDesc"};
    if( !defined($dbHostDesc) ) {
        return 0;
    }

    &OBM::toolBox::write_log( "[Entities::obmHost]: MAJ de l'hote '".$dbHostDesc->{"host_name"}."' dans les tables de production", "W" );

    # MAJ de l'entité dans la table de production
    my $query = "DELETE FROM P_Host WHERE host_id=".$self->{"hostId"};
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Entities::obmHost]: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    # Obtention des noms de colonnes de la table
    $query = "SELECT * FROM P_Host WHERE 0=1";
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Entities::obmHost]: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
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

    # Les domaines pour lesquels cet hôte est serveur SMTP entrant
    if( defined($entry->{"smtp-in_domain"}) && (ref($entry->{"smtp-in_domain"}) eq "ARRAY" ) ) {
        for( my $i=0; $i<=$#{$entry->{"smtp-in_domain"}}; $i++ ) {
            $entry->{"smtp-in_domain"}->[$i] = to_utf8({ -string => $entry->{"smtp-in_domain"}->[$i], -charset => $defaultCharSet });
        }

        $ldapEntry->add( smtpInDomain => $entry->{"smtp-in_domain"} );
    }

    # Les domaines pour lesquels cet hôte est serveur Cyrus
    if( defined($entry->{"cyrus_domain"}) && (ref($entry->{"cyrus_domain"}) eq "ARRAY" ) ) {
        for( my $i=0; $i<=$#{$entry->{"cyrus_domain"}}; $i++ ) {
            $entry->{"cyrus_domain"}->[$i] = to_utf8({ -string => $entry->{"cyrus_domain"}->[$i], -charset => $defaultCharSet });
        }

        $ldapEntry->add( cyrusDomain => $entry->{"cyrus_domain"} );
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

    # Les domaines pour lesquels cet hôte est serveur SMTP
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"smtp-in_domain"}, $ldapEntry, "smtpInDomain" ) ) {
        $update = 1;
    }

    # Les domaines pour lesquels cet hôte est serveur Cyrus
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"cyrus_domain"}, $ldapEntry, "cyrusDomain" ) ) {
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
