package OBM::Entities::obmPostfixConf;

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
    my( $incremental ) = @_;

    my %obmPostfixConfAttr = (
        type => undef,
        typeDesc => undef,
        incremental => undef,
        links => undef,
        toDelete => undef,
        archive => undef,
        sieve => undef,
        domainId => undef,
        postfixConf => undef
    );


    if( !defined($incremental) ) {
        croak( "Usage: PACKAGE->new(INCR)" );

    }

    # Pas de mode incrémental pour ce type
    $obmPostfixConfAttr{"incremental"} = 0;
    $obmPostfixConfAttr{"links"} = 1;

    $obmPostfixConfAttr{"type"} = $POSTFIXCONF;
    $obmPostfixConfAttr{"typeDesc"} = $attributeDef->{$obmPostfixConfAttr{"type"}};
    $obmPostfixConfAttr{"toDelete"} = 0;

    bless( \%obmPostfixConfAttr, $self );
}


sub getEntity {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;


    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( "obmPostfixConf: connecteur a la base de donnee invalide", "W" );
        return 0;
    }

    if( !defined($domainDesc->{"domain_id"}) || ($domainDesc->{"domain_id"} !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( "obmPostfixConf: description de domaine OBM incorrecte", "W" );
        return 0;

    }else {
        # On positionne l'identifiant du domaine de l'entité
        $self->{"domainId"} = $domainDesc->{"domain_id"};
    }


    &OBM::toolBox::write_log( "obmPostfixConf: gestion de la configuration de postfix, domaine '".$domainDesc->{"domain_label"}."'", "W" );

    $self->{"postfixConf"}->{"postfixconf_name"} = $domainDesc->{"domain_label"};
    $self->{"postfixConf"}->{"postfixconf_domain"} = $domainDesc->{"domain_label"};

    $self->{"postfixConf"}->{"postfixconf_mail_domains"} = [];
    push( @{$self->{"postfixConf"}->{"postfixconf_mail_domains"}}, $domainDesc->{"domain_name"} );
    for( my $i=0; $i<=$#{$domainDesc->{"domain_alias"}}; $i++ ) {
        push( @{$self->{"postfixConf"}->{"postfixconf_mail_domains"}}, $domainDesc->{"domain_alias"}->[$i] );
        
    }


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


sub getEntityLinks {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;

    return 1;
}


sub getLdapDnPrefix {
    my $self = shift;
    my $dnPrefix = undef;

    if( defined($self->{"typeDesc"}->{"dn_prefix"}) && defined($self->{"postfixConf"}->{$self->{"typeDesc"}->{"dn_value"}}) ) {
        $dnPrefix = $self->{"typeDesc"}->{"dn_prefix"}."=".$self->{"postfixConf"}->{$self->{"typeDesc"}->{"dn_value"}};
    }

    return $dnPrefix;
}


sub createLdapEntry {
    my $self = shift;
    my ( $ldapEntry ) = @_;
    my $entry = $self->{"postfixConf"};


    if( !defined($entry->{"postfixconf_name"}) ) {
        return 0;
    }

    # On construit la nouvelle entree
    #
    # Les parametres nécessaires
    $ldapEntry->add(
        objectClass => $self->{"typeDesc"}->{"objectclass"},
        cn => to_utf8({ -string => $entry->{"postfixconf_name"}, -charset => $defaultCharSet })
    );

    # Les domaines de messagerie
    if( $entry->{"postfixconf_mail_domains"} ) {
        $ldapEntry->add( myDestination => $entry->{"postfixconf_mail_domains"} );
    }

    # Le domaine
    if( $entry->{"postfixconf_domain"} ) {
        $ldapEntry->add( obmDomain => to_utf8({ -string => $entry->{"postfixconf_domain"}, -charset => $defaultCharSet }) );
    }

    return 1;
}


sub updateLdapEntry {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $entry = $self->{"postfixConf"};
    my $update = 0;

    # Les domaines de messagerie
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"postfixconf_mail_domains"}, $ldapEntry, "myDestination" ) ) {
        $update = 1;
    }

    # Le domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"postfixconf_domain"}, $ldapEntry, "obmDomain") ) {
        $update = 1;
    }

    return $update;
}


sub getMailboxName {
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
