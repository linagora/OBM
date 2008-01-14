package OBM::Entities::obmNode;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::common;
require OBM::Parameters::ldapConf;
require OBM::Ldap::utils;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);


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
        &OBM::toolBox::write_log( "[Entities::obmNode]: description de domaine OBM incorrecte", "W" );
        return 0;
    }else {
        # On positionne l'identifiant du domaine de l'entité
        $self->{"domainId"} = $domainDesc->{"domain_id"};
    }

    if( defined($name) ) {
        $self->{"nodeDesc"}->{"name"} = $name;
    }else {
        &OBM::toolBox::write_log( "[Entities::obmNode]: nom de noeud invalide", "W" );
        return 0;
    }

    $self->{"nodeDesc"}->{"description"} = $description;

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
    my $self =shift;

    return 0;
}


sub isLinks {
    my $self = shift;

    return $self->{links};
}


sub getLdapObjectclass {
    my $self = shift;

    return $self->{objectclass};
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
    my $entry = $self->{"nodeDesc"};

    # On construit la nouvelle entree
    if( $entry->{"name"} ) {
        $ldapEntry->add(
            objectClass => $self->{"objectclass"},
            ou => to_utf8({ -string => $entry->{"name"}, -charset => $defaultCharSet })
        );
                
    }else {
        return 0;
    }

    # La description
    if( $entry->{"description"} ) {
        $ldapEntry->add( description => to_utf8({ -string => $entry->{"description"}, -charset => $defaultCharSet }) );
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
    my $update = 0;


    if( !defined($ldapEntry) ) {
        return 0;
    }


    if( &OBM::Ldap::utils::modifyAttr( $entry->{"description"}, $ldapEntry, "description" ) ) {
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
