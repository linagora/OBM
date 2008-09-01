package OBM::Entities::obmNode;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Entities::commonEntities qw(getType setDelete getDelete getArchive getLdapObjectclass isLinks getEntityId);
use OBM::Tools::commonMethods qw(_log dump);
use OBM::Parameters::common;
require OBM::Parameters::ldapConf;
require OBM::Ldap::utils;


sub new {
    my $self = shift;
    my( $links, $deleted ) = @_;

    my %obmNodeAttr = (
        type => undef,
        links => undef,
        toDelete => undef,
        domainId => undef,
        nodeDesc => undef,
        objectclass => undef,
        dnPrefix => undef,
        dnValue => undef
    );


    if( !defined($links) || !defined($deleted) ) {
        croak( "Usage: PACKAGE->new(LINKS, DELETED)" );
    
    }
    

    $obmNodeAttr{"links"} = $links;
    $obmNodeAttr{"toDelete"} = $deleted;

    $obmNodeAttr{"type"} = $OBM::Parameters::ldapConf::NODE;

    # Définition de la représentation LDAP de ce type
    $obmNodeAttr{objectclass} = $OBM::Parameters::ldapConf::attributeDef->{$obmNodeAttr{"type"}}->{objectclass};
    $obmNodeAttr{dnPrefix} = $OBM::Parameters::ldapConf::attributeDef->{$obmNodeAttr{"type"}}->{dn_prefix};
    $obmNodeAttr{dnValue} = $OBM::Parameters::ldapConf::attributeDef->{$obmNodeAttr{"type"}}->{dn_value};

    bless( \%obmNodeAttr, $self );
}


sub getEntity {
    my $self = shift;
    my( $name, $description, $domainDesc ) = @_;

    if( !defined($domainDesc->{"domain_id"}) || ($domainDesc->{"domain_id"} !~ /^\d+$/) ) {
        $self->_log( 'description de domaine OBM incorrecte', 3 );
        return 0;
    }else {
        # On positionne l'identifiant du domaine de l'entité
        $self->{"domainId"} = $domainDesc->{"domain_id"};
    }

    if( defined($name) ) {
        $self->{"nodeDesc"}->{"name"} = $name;
    }else {
        $self->_log( 'nom de noeud invalide', 3 );
        return 0;
    }

    $self->{"nodeDesc"}->{"description"} = $description;

    return 1;
}


sub getLdapDnPrefix {
    my $self = shift;
    my $dnPrefix = undef;

    if( defined($self->{"dnPrefix"}) && defined($self->{"nodeDesc"}->{$self->{"dnValue"}}) ) {
        $dnPrefix = $self->{"dnPrefix"}."=".$self->{"nodeDesc"}->{$self->{"dnValue"}};
    }

    return $dnPrefix;
}


sub createLdapEntry {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $entry = $self->{'nodeDesc'};

    # On construit la nouvelle entree
    if( $entry->{'name'} ) {
        $ldapEntry->add(
            objectClass => $self->{'objectclass'},
            ou => $entry->{'name'}
        );
                
    }else {
        return 0;
    }

    # La description
    if( $entry->{'description'} ) {
        $ldapEntry->add( description => $entry->{'description'} );
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
    my $entry = $self->{"nodeDesc"};

    require OBM::Entities::entitiesUpdateState;
    my $update = OBM::Entities::entitiesUpdateState->new();


    if( !defined($ldapEntry) ) {
        return undef;
    }


    if( &OBM::Ldap::utils::modifyAttr( $entry->{"description"}, $ldapEntry, "description" ) ) {
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
