package OBM::Entities::obmSambaDomain;

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
            getLdapObjectclass
            isLinks
            getEntityId
            isMailActive
            getMailServerId
            updateLinkedEntity
            );
use OBM::Tools::commonMethods qw(_log dump);
use OBM::Parameters::common;
require OBM::Parameters::ldapConf;
require OBM::Ldap::utils;
require OBM::Tools::obmDbHandler;
use URI::Escape;


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
    my( $domainDesc ) = @_;


    if( !defined($domainDesc->{"domain_id"}) || ($domainDesc->{"domain_id"} !~ /^\d+$/) ) {
        $self->_log( 'description de domaine OBM incorrecte', 3 );
        return 0;

    }else {
        # On positionne l'identifiant du domaine de l'entité
        $self->{"domainId"} = $domainDesc->{"domain_id"};
    }


    $self->_log( 'gestion de la configuration du domaine Samba, domaine \''.$domainDesc->{'domain_label'}.'\'', 1 );


    $self->{"sambaConf"}->{"sambaConf_domain"} = $domainDesc->{"domain_label"};
    $self->{"sambaConf"}->{"sambaConf_domain_name"} = $domainDesc->{"domain_samba_name"};
    $self->{"sambaConf"}->{"sambaConf_domain_sid"} = $domainDesc->{"domain_samba_sid"};


    return 1;
}


sub updateDbEntity {
    my $self = shift;
    # Pas de tables de production pour le type obmSambaDomain. Ces informations
    # font parties des informations de domaines

#    my $dbHandler = OBM::Tools::obmDbHandler->instance();
#    if( !defined($dbHandler) ) {
#        return 0;
#    }

    return 1;
}


sub updateDbEntityLinks {
    my $self = shift;
    # Pas de tables de production pour le type obmSambaDomain. Ces informations
    # font parties des informations de domaines

#    my $dbHandler = OBM::Tools::obmDbHandler->instance();
#    if( !defined($dbHandler) ) {
#        return 0;
#    }

    return 1;

}


sub getEntityLinks {
    my $self = shift;
    my( $domainDesc ) = @_;

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
    my $entry = $self->{'sambaConf'};


    if( !defined($entry->{'sambaConf_domain_name'}) || !defined($entry->{'sambaConf_domain_sid'}) ) {
        return 0;
    }

    # On construit la nouvelle entrée
    #
    # Les paramètres nécessaires
    $ldapEntry->add(
        objectClass => $self->{'objectclass'},
        sambaSID => $entry->{'sambaConf_domain_sid'},
        sambaDomainName => $entry->{'sambaConf_domain_name'}
    );


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
    my $entry = $self->{"sambaConf"};

    require OBM::Entities::entitiesUpdateState;
    my $update = OBM::Entities::entitiesUpdateState->new();


    if( !defined($ldapEntry) ) {
        return undef;
    }


    # Le SID du domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"sambaConf_domain_sid"}, $ldapEntry, "sambaSID") ) {
        $update->setUpdate();
    }

    # Le nom du domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"sambaConf_domain_name"}, $ldapEntry, "sambaDomainName" ) ) {
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

    return $self->{"sieve"};
}
