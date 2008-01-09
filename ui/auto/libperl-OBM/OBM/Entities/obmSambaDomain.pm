package OBM::Entities::obmSambaDomain;

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
use URI::Escape;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);


sub new {
    my $self = shift;
    my( $links, $deleted ) = @_;

    my %obmSambaDomainConfAttr = (
        type => undef,
        links => undef,
        toDelete => undef,
        archive => undef,
        sieve => undef,
        domainId => undef,
        sambaConf => undef,
        objectclass => undef,
        dnPrefix => undef,
        dnValue => undef
    );


    if( !defined($links) || !defined($deleted) ) {
        croak( "Usage: PACKAGE->new(LINKS)" );

    }

    $obmSambaDomainConfAttr{"links"} = $links;
    $obmSambaDomainConfAttr{"toDelete"} = $deleted;

    $obmSambaDomainConfAttr{"type"} = $OBM::Parameters::ldapConf::SAMBADOMAIN;

    # Définition de la représentation LDAP de ce type
    $obmSambaDomainConfAttr{objectclass} = $OBM::Parameters::ldapConf::attributeDef->{$obmSambaDomainConfAttr{"type"}}->{objectclass};
    $obmSambaDomainConfAttr{dnPrefix} = $OBM::Parameters::ldapConf::attributeDef->{$obmSambaDomainConfAttr{"type"}}->{dn_prefix};
    $obmSambaDomainConfAttr{dnValue} = $OBM::Parameters::ldapConf::attributeDef->{$obmSambaDomainConfAttr{"type"}}->{dn_value};

    bless( \%obmSambaDomainConfAttr, $self );
}


sub getEntity {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;


    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( "[Entities::obmSambaDomain]: connecteur a la base de donnee invalide", "W" );
        return 0;
    }

    if( !defined($domainDesc->{"domain_id"}) || ($domainDesc->{"domain_id"} !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( "[Entities::obmSambaDomain]: description de domaine OBM incorrecte", "W" );
        return 0;

    }else {
        # On positionne l'identifiant du domaine de l'entité
        $self->{"domainId"} = $domainDesc->{"domain_id"};
    }


    &OBM::toolBox::write_log( "[Entities::obmSambaDomain]: gestion de la configuration du domaine Samba, domaine '".$domainDesc->{"domain_label"}."'", "W" );


    $self->{"sambaConf"}->{"sambaConf_domain"} = $domainDesc->{"domain_label"};
    $self->{"sambaConf"}->{"sambaConf_domain_name"} = $domainDesc->{"domain_samba_name"};
    $self->{"sambaConf"}->{"sambaConf_domain_sid"} = $domainDesc->{"domain_samba_sid"};


    return 1;
}


sub updateDbEntity {
    my $self = shift;
    # Pas de tables de production pour le type obmSambaDomain. Ces informations
    # font parties des informations de domaines

    return 1;
}


sub getEntityLinks {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;

    return 1;
}


sub getEntityDescription {
    my $self = shift;
    my $entry = $self->{"sambaConf"};
    my $description = "";


    if( defined($entry->{postfixconf_domain}) ) {
        $description .= "domaine '".$entry->{sambaConf_domain}."'";
    }

    if( ($description ne "") && defined($self->{type}) ) {
        $description .= ", type '".$self->{type}."'";
    }

    if( $description ne "" ) {
        return $description;
    }

    if( defined($self->{domainId}) ) {
        $description .= "ID BD '".$self->{domainId}."'";
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

    return $self->{"archive"};
}


sub isLinks {
    my $self = shift;

    return $self->{"links"};
}


sub getLdapObjectclass {
    my $self = shift;

    return $self->{objectclass};
}


sub getLdapDnPrefix {
    my $self = shift;
    my $dnPrefix = undef;

    if( defined($self->{"dnPrefix"}) && defined($self->{"sambaConf"}->{$self->{"dnValue"}}) ) {
        $dnPrefix = $self->{"dnPrefix"}."=".$self->{"sambaConf"}->{$self->{"dnValue"}};
    }

    return $dnPrefix;
}


sub createLdapEntry {
    my $self = shift;
    my ( $ldapEntry ) = @_;
    my $entry = $self->{"sambaConf"};


    if( !defined($entry->{"sambaConf_domain_name"}) || !defined($entry->{"sambaConf_domain_sid"}) ) {
        return 0;
    }

    # On construit la nouvelle entrée
    #
    # Les paramètres nécessaires
    $ldapEntry->add(
        objectClass => $self->{"objectclass"},
        sambaSID => $entry->{"sambaConf_domain_sid"},
        sambaDomainName => to_utf8({ -string => $entry->{"sambaConf_domain_name"}, -charset => $defaultCharSet })
    );


    return 1;
}


sub updateLdapEntry {
    my $self = shift;
    my( $ldapEntry, $objectclassDesc ) = @_;
    my $entry = $self->{"sambaConf"};
    my $update = 0;


    # Le SID du domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"sambaConf_domain_sid"}, $ldapEntry, "sambaSID") ) {
        $update = 1;
    }

    # Le nom du domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"sambaConf_domain_name"}, $ldapEntry, "sambaDomainName" ) ) {
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

    return $self->{"sieve"};
}


sub dump {
    my $self = shift;
    my @desc;

    push( @desc, $self );
    
    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}
