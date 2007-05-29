package OBM::Ldap::ldapEngine;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
require overload;
use Carp;
use strict;

use Net::LDAP;
use Net::LDAP::Entry;
use OBM::Parameters::common;
require OBM::Parameters::ldapConf;
require OBM::toolBox;
require OBM::utils;
require OBM::ldap;


# Definition des attributs de l'objet
my %ldapEngineAttr = (
    ldapStruct => undef,
    domainList => undef,
    typeDesc => undef,
    ldapConn => {
        ldapServer => undef,
        ldapAdmin => undef,
        ldapAdminDn => undef,
        ldapPasswd => undef,
        conn => undef
    }
);


sub new {
    my( $obj, $domainList ) = @_;
    $obj = ref($obj) || $obj;

    if( !defined($domainList) ) {
        croak( "Usage: PACKAGE->new(DOMAINLIST)" );
    }else {
        $ldapEngineAttr{"domainList"} = $domainList;
    }

    $ldapEngineAttr{"ldapStruct"} = &OBM::utils::cloneStruct($OBM::Parameters::ldapConf::ldapStruct),
    $ldapEngineAttr{"typeDesc"} = $OBM::Parameters::ldapConf::attributeDef;

    my $self = \%ldapEngineAttr;
    bless( $self, $obj );

    return $self;
}


sub init {
    my $self = shift;

    # Creation de l'arbre
    $self->_initTree( $self->{"ldapStruct"}, undef, undef );

    # Initialisation des paramètres de connexions LDAP
    $self->{"ldapConn"}->{"ldapServer"} = $self->{"domainList"}->[0]->{"ldap_admin_server"};
    $self->{"ldapConn"}->{"ldapAdmin"} = $self->{"domainList"}->[0]->{"ldap_admin_login"};
    $self->{"ldapConn"}->{"ldapPasswd"} = $self->{"domainList"}->[0]->{"ldap_admin_passwd"};

    # Etabli la connexion à l'annuaire
    return $self->_connectLdapSrv();
}


sub destroy {
    my $self = shift;

    return $self->_disconnectLdapSrv();
}


sub dump {
    my $self = shift;
    my( $what ) = @_;
    my @desc;

    if( !defined($what) ) {
        return 0;
    }

    SWITCH: {
        if( lc($what) eq "ldapstruct" ) {
            push( @desc, $self->{"ldapStruct"} );
            last SWITCH;
        }
    }

    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}


sub _connectLdapSrv {
    my $self = shift;
    my $ldapConn = $self->{"ldapConn"};
    my $ldapStruct = $self->{"ldapStruct"};

    if( !defined($ldapConn->{"ldapServer"}) || !defined($ldapConn->{"ldapAdmin"}) || !defined($ldapConn->{"ldapPasswd"}) ) {
        &OBM::toolBox::write_log( "ldapEngine: pas d'information de connexion a l'annuaire LDAP", "W" );
        return 0;
    }


    &OBM::toolBox::write_log( "ldapEngine: connexion a l'annuaire LDAP '".$ldapConn->{"ldapServer"}."'", "W" );

    $ldapConn->{"conn"} = Net::LDAP->new(
        $ldapConn->{"ldapServer"},
        port => "389",
        debug => "0",
        timeout => "60",
        version => "3"
    ) or &OBM::toolBox::write_log( "ldapEngine: echec de connexion a LDAP : $@", "W" ) && return 0;

    if( !defined($ldapConn->{"conn"}) ) {
        return 0;
    }

    my $errorCode;
    my $ldapAdmin = {
        node_type => $OBM::Parameters::ldapConf::SYSTEMUSERS,
        name => $ldapConn->{"ldapAdmin"}
    };

    my $parentDn = $self->_findTypeParentDn( $ldapStruct, $OBM::Parameters::ldapConf::SYSTEMUSERS, 0 );
    if( defined( $parentDn ) ) {
        $ldapConn->{"ldapAdminDn"} = $self->_makeDn( $ldapAdmin, $parentDn );
        &OBM::toolBox::write_log( "ldapEngine: connexion a l'annuaire en tant que '".$ldapConn->{"ldapAdminDn"}."'", "W" );

        $errorCode = $ldapConn->{"conn"}->bind(
            $ldapConn->{"ldapAdminDn"},
            password => $ldapConn->{"ldapPasswd"}
        );

    }else {
        &OBM::toolBox::write_log( "ldapEngine: DN de l'administrateur LDAP inconnu", "W" );
        return 0;
    }

    if( $errorCode->code ) {
        &OBM::toolBox::write_log( "ldapEngine: echec de connexion : ".$errorCode->error, "W" );
        return 0;
    }else {
        &OBM::toolBox::write_log( "ldapEngine: connexion a l'annuaire LDAP etablie", "W" );
    }

    return 1;
}


sub _disconnectLdapSrv {
    my $self = shift;
    my $ldapConn = $self->{"ldapConn"};

    if( defined($ldapConn->{"conn"}) ) {
        &OBM::toolBox::write_log( "ldapEngine: deconnexion de l'annuaire LDAP '".$ldapConn->{"ldapServer"}."'", "W" );
        $ldapConn->{"conn"}->unbind();
    }

    return 1;
}

sub _initTree {
    my $self = shift;
    my( $ldapStruct, $parentDn, $domainId ) = @_;

    # Si le domaine du noeud n'est pas positionné
    if( !exists($ldapStruct->{"domain_id"}) && !defined($domainId) ) {
        # Si il n'y a pas de domaine précisé et que le domaine du noeud n'est
        # pas positionné, on attache les informations au meta-domaine
        $domainId = 0;
    }


    # On initialise le noeud courant
    $ldapStruct->{"dn"} = $self->_makeDn( $ldapStruct, $parentDn );
    if( !exists($ldapStruct->{"domain_id"}) && defined($domainId) ) {
        $ldapStruct->{"domain_id"} = $domainId;
    }

    &OBM::toolBox::write_log( "ldapEngine: gestion du noeud de type '".$ldapStruct->{"node_type"}."' et de dn : ".$ldapStruct->{"dn"}, "W" );


    # On cré les branches correspondants aux templates pour chacun des domaines
    # sauf pour le domaine '0'
    for( my $i=0; $i<=$#{$ldapStruct->{"template"}}; $i++ ) {
        for( my $j=0; $j<=$#{$self->{"domainList"}}; $j++ ) {
            if( $self->{"domainList"}->[$j]->{"meta_domain"} ) {
                # On ne cré pas de structure pour les meta-domaines
                next;
            }

            &OBM::toolBox::write_log( "ldapEngine: creation de la structure pour le domaine '".$self->{"domainList"}->[$j]->{"domain_dn"}."'", "W" );
            my $currentDomainBranch = &OBM::utils::cloneStruct( $ldapStruct->{"template"}->[$i] );

            # On positionne le nom en fonction du domaine, afin de
            # pouvoir créer le DN
            $currentDomainBranch->{"name"} = $self->{"domainList"}->[$j]->{"domain_dn"};
            $currentDomainBranch->{"domain_id"} = $j;

            push( @{$ldapStruct->{"branch"}}, $currentDomainBranch )
        }
    }


    # On parcours les sous branches
    for( my $i=0; $i<=$#{$ldapStruct->{"branch"}}; $i++ ) {
        my $currentDomainId = undef;
        if( defined( $ldapStruct->{"domain_id"} ) ) {
            $currentDomainId = $ldapStruct->{"domain_id"};
        }

        $self->_initTree( $ldapStruct->{"branch"}->[$i], $ldapStruct->{"dn"}, $currentDomainId );
    }

    return 1;
}


sub _findTypeParentDn {
    my $self = shift;
    my ( $ldapStruct, $type, $domainId ) = @_;

    if( $self->{"typeDesc"}->{$type}->{"is_branch"} ) {
        return 0;
    }

    if( defined($ldapStruct->{"data_type"}) && defined($ldapStruct->{"domain_id"}) &&  ($ldapStruct->{"domain_id"} == $domainId) ) {
        for( my $i=0; $i<=$#{$ldapStruct->{"data_type"}}; $i++ ) {
            if( $ldapStruct->{"data_type"}->[$i] eq $type ) {
                return $ldapStruct->{"dn"};
            }
        }
    }

    if( exists($ldapStruct->{"branch"}) && defined($ldapStruct->{"branch"}) ) {
        for( my $i=0; $i<=$#{$ldapStruct->{"branch"}}; $i++ ) {
            return $self->_findTypeParentDn( $ldapStruct->{"branch"}->[$i], $type, $domainId );
        }
    }

    return undef;
}


sub _makeDn {
    my $self = shift;
    my( $entry, $parentDn ) = @_;

    my $entryDn = $self->{"typeDesc"}->{$entry->{"node_type"}}->{"dn_prefix"}."=".$entry->{"name"};

    if( defined($parentDn) ) {
        $entryDn .= ",".$parentDn;
    }

    return $entryDn;
}


sub checkTree {
    my $self = shift;

}
