package OBM::Entities::obmDomainRoot;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Entities::commonEntities qw(getType setDelete getDelete getArchive getLdapObjectclass isLinks getEntityId _log);
use OBM::Parameters::common;
require OBM::Parameters::ldapConf;
require OBM::Ldap::utils;


sub new {
    my $self = shift;
    my( $links, $deleted ) = @_;

    my %obmDomainRootAttr = (
        type => undef,
        links => undef,
        toDelete => undef,
        domainId => undef,
        domainDesc => undef,
        objectclass => undef,
        dnPrefix => undef,
        dnValue => undef
    );


    if( !defined($links) || !defined($deleted) ) {
        $self->_log( 'Usage: PACKAGE->new(LINKS, DELETED)', 1 );
        return undef;
    }

    $obmDomainRootAttr{'links'} = $links;
    $obmDomainRootAttr{'toDelete'} = $deleted;

    $obmDomainRootAttr{'type'} = $OBM::Parameters::ldapConf::DOMAINROOT;

    # Définition de la représentation LDAP de ce type
    $obmDomainRootAttr{objectclass} = $OBM::Parameters::ldapConf::attributeDef->{$obmDomainRootAttr{'type'}}->{objectclass};
    $obmDomainRootAttr{dnPrefix} = $OBM::Parameters::ldapConf::attributeDef->{$obmDomainRootAttr{'type'}}->{dn_prefix};;
    $obmDomainRootAttr{dnValue} = $OBM::Parameters::ldapConf::attributeDef->{$obmDomainRootAttr{'type'}}->{dn_value};

    bless( \%obmDomainRootAttr, $self );
}


sub getEntity {
    my $self = shift;
    my( $domainDesc ) = @_;

    if( !defined($domainDesc->{'domain_id'}) || ($domainDesc->{'domain_id'} !~ /^\d+$/) ) {
        $self->_log( '[Entities::obmDomainRoot]: description de domaine OBM incorrecte', 3 );
        return 0;
    }else {
        # On positionne l'identifiant du domaine de l'entité
        $self->{'domainId'} = $domainDesc->{'domain_id'};
    }

    $self->{'domainDesc'} = $domainDesc;

    return 1;
}


sub getLdapDnPrefix {
    my $self = shift;
    my $dnPrefix = undef;

    if( defined($self->{'dnPrefix'}) && defined($self->{'domainDesc'}->{$self->{'dnValue'}}) ) {
        $dnPrefix = $self->{'dnPrefix'}.'='.$self->{'domainDesc'}->{'domain_dn'};
    }

    return $dnPrefix;
}


sub createLdapEntry {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $entry = $self->{'domainDesc'};


    # On construit la nouvelle entree
    if( $entry->{'domain_label'} ) {
        $ldapEntry->add(
            objectClass => $self->{'objectclass'},
            dc => $entry->{'domain_dn'},
            o => $entry->{'domain_label'}
        );

    }else {
        return 0;
    }

    # La description
    if( $entry->{'domain_desc'} ) {
        $ldapEntry->add( description => $entry->{'domain_desc'} );
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
    my $entry = $self->{'domainDesc'};

    require OBM::Entities::entitiesUpdateState;
    my $update = OBM::Entities::entitiesUpdateState->new();


    if( !defined($ldapEntry) ) {
        return undef;
    }


    if( &OBM::Ldap::utils::modifyAttr( $entry->{'domain_label'}, $ldapEntry, 'o' ) ) {
        $update->setUpdate();
    }

    if( &OBM::Ldap::utils::modifyAttr( $entry->{'domain_desc'}, $ldapEntry, 'description' ) ) {
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


sub dump {
    my $self = shift;
    my @desc;

    push( @desc, $self );
    
    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}
