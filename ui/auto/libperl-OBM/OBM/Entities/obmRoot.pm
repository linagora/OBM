package OBM::Entities::obmRoot;

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

    my %ldapEngineAttr = (
        type => undef,
        links => undef,
        toDelete => undef,
        domainId => undef,
        rootDesc => undef,
        objectclass => undef,
        dnPrefix => undef,
        dnValue => undef
    );


    if( !defined($links) || !defined($deleted) ) {
        croak( "Usage: PACKAGE->new(LINKS, DELETED)" );

    }


    $ldapEngineAttr{"links"} = $links;
    $ldapEngineAttr{"toDelete"} = $deleted;

    $ldapEngineAttr{"type"} = $OBM::Parameters::ldapConf::ROOT;

    # Définition de la représentation LDAP de ce type
    $ldapEngineAttr{objectclass} = $OBM::Parameters::ldapConf::attributeDef->{$ldapEngineAttr{"type"}}->{objectclass};
    $ldapEngineAttr{dnPrefix} = $OBM::Parameters::ldapConf::attributeDef->{$ldapEngineAttr{"type"}}->{dn_prefix};
    $ldapEngineAttr{dnValue} = $OBM::Parameters::ldapConf::attributeDef->{$ldapEngineAttr{"type"}}->{dn_value};

    bless( \%ldapEngineAttr, $self );
}


sub getEntity {
    my $self = shift;
    my( $name, $description, $domainDesc ) = @_;

    if( !defined($domainDesc->{"domain_id"}) || ($domainDesc->{"domain_id"} !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( "[Entities::obmRoot]: description de domaine OBM incorrecte", "W" );
        return 0;
    }else {
        # On positionne l'identifiant du domaine de l'entité
        $self->{"domainId"} = $domainDesc->{"domain_id"};
    }

    if( defined($name) ) {
        $self->{"nodeDesc"}->{"name"} = $name;
    }else {
        &OBM::toolBox::write_log( "[Entities::obmRoot]: nom de noeud invalide", "W" );
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
            objectClass => $self->{objectclass},
            o => to_utf8({ -string => $entry->{"name"}, -charset => $defaultCharSet }),
            dc => to_utf8({ -string => $entry->{"name"}, -charset => $defaultCharSet })
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


sub updateLdapEntry {
    my $self = shift;
    my( $ldapEntry, $objectclassDesc ) = @_;
    my $entry = $self->{"nodeDesc"};
    my $update = 0;

    if( &OBM::Ldap::utils::modifyAttr( $entry->{"description"}, $ldapEntry, "description" ) ) {
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
