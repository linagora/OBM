package OBM::Ldap::ldapEngine;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
use strict;

use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
require Net::LDAP;
require Net::LDAP::Entry;
require OBM::toolBox;
require OBM::utils;
require OBM::Entities::obmRoot;
require OBM::Entities::obmDomainRoot;
require OBM::Entities::obmNode;


sub new {
    my $self = shift;
    my( $domainList ) = @_;

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


    if( !defined($domainList) ) {
        croak( "Usage: PACKAGE->new(DOMAINLIST)" );
    }else {
        $ldapEngineAttr{"domainList"} = $domainList;
    }

    $ldapEngineAttr{"ldapStruct"} = &OBM::utils::cloneStruct($OBM::Parameters::ldapConf::ldapStruct),
    $ldapEngineAttr{"typeDesc"} = $OBM::Parameters::ldapConf::attributeDef;

    bless( \%ldapEngineAttr, $self );
}


sub init {
    my $self = shift;

    &OBM::toolBox::write_log( "ldapEngine: initialisation du moteur", "W" );

    # Creation de l'arbre
    $self->_initTree( $self->{"ldapStruct"}, undef, undef );

    # Initialisation des paramètres de connexions LDAP
    $self->{"ldapConn"}->{"ldapServer"} = $self->{"domainList"}->[0]->{"ldap_admin_server"};
    $self->{"ldapConn"}->{"ldapAdmin"} = $self->{"domainList"}->[0]->{"ldap_admin_login"};
    $self->{"ldapConn"}->{"ldapPasswd"} = $self->{"domainList"}->[0]->{"ldap_admin_passwd"};

    # Etabli la connexion à l'annuaire
    if( !$self->_connectLdapSrv() ) {
        return 0;
    }

    # On vérifie l'arborescence
    &OBM::toolBox::write_log( "ldapEngine: verification de l'arborescence de l'annuaire LDAP", "W" );
    if( !$self->checkLdapTree( $self->{"ldapStruct"} ) ) {
        $self->destroy();
        return 0;
    }

    return 1;
}


sub destroy {
    my $self = shift;

    &OBM::toolBox::write_log( "ldapEngine: arret du moteur", "W" );

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

        if( lc($what) eq "all" ) {
            push( @desc, $self );
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

    my $parentDn = $self->_findTypeParentDn( $ldapStruct, $SYSTEMUSERS, 0 );
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

    if( !defined($domainId) ) {
        # Si le domaine du noeud n'est pas transmit
        $domainId = 0;
    }elsif( $ldapStruct->{"domain_id"} ) {
        # Si le noeud est déjà asigné à un domaine
        $domainId = $ldapStruct->{"domain_id"};
    }


    # On initialise le noeud courant
    $ldapStruct->{"dn"} = $self->_makeDn( $ldapStruct, $parentDn );
    $ldapStruct->{"domain_id"} = $domainId;

    &OBM::toolBox::write_log( "ldapEngine: gestion du noeud de type '".$ldapStruct->{"node_type"}."' et de dn : ".$ldapStruct->{"dn"}, "W" );


    # Création de l'objet adéquat
    SWITCH: {
        # Obtention de la description du domaine courrant
        my $domainDesc = $self->_findDomainbyId($domainId);

        if( $ldapStruct->{"node_type"} eq $ROOT ) {
            my $object = OBM::Entities::obmRoot->new(1, 0);
            $object->getEntity( $ldapStruct->{"name"}, $ldapStruct->{"description"}, $domainDesc );
            $ldapStruct->{"object"} = $object;

            last SWITCH;
        }

        if( $ldapStruct->{"node_type"} eq $DOMAINROOT ) {
            my $object = OBM::Entities::obmDomainRoot->new(1, 0);
            $object->getEntity( $domainDesc );
            $ldapStruct->{"object"} = $object;

            last SWITCH;
        }

        if( $ldapStruct->{"node_type"} eq $NODE ) {
            my $object = OBM::Entities::obmNode->new(1, 0);
            $object->getEntity( $ldapStruct->{"name"}, $ldapStruct->{"description"}, $domainDesc );
            $ldapStruct->{"object"} = $object;

            last SWITCH;
        }
    }


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
            $currentDomainBranch->{"domain_id"} = $self->{"domainList"}->[$j]->{"domain_id"};

            push( @{$ldapStruct->{"branch"}}, $currentDomainBranch )
        }
    }


    # On parcours les sous branches
    for( my $i=0; $i<=$#{$ldapStruct->{"branch"}}; $i++ ) {
        $self->_initTree( $ldapStruct->{"branch"}->[$i], $ldapStruct->{"dn"}, $ldapStruct->{"domain_id"} );
    }

    return 1;
}


sub checkLdapTree {
    my $self = shift;
    my( $ldapStruct ) = @_;
    my $type = $ldapStruct->{"node_type"};

    if( !$self->{"typeDesc"}->{$type}->{"is_branch"} ) {
        return 0;
    }

    if( !defined($ldapStruct->{"object"}) ) {
        return 0;
    }

    if( !defined($ldapStruct->{"dn"}) ) {
        return 0;
    }

    $self->_doWork( $ldapStruct->{"dn"}, $ldapStruct->{"object"} );

    # On parcours les sous branches
    for( my $i=0; $i<=$#{$ldapStruct->{"branch"}}; $i++ ) {
        if( !$self->checkLdapTree( $ldapStruct->{"branch"}->[$i] ) ) {
            return 0;
        }
    }

    return 1;
}


sub updateLdapEntity {
    my $self = shift;
    my( $ldapEntry ) = @_;

    if( !defined($self->{"ldapConn"}->{"conn"}) ) {
        return 0;
    }
    my $ldapConn = $self->{"ldapConn"}->{"conn"};

    if( !defined($ldapEntry) ) {
        return 0;
    }

    my $result = $ldapEntry->update( $ldapConn );

    if( $result->is_error() ) {
        &OBM::toolBox::write_log( "ldapEngine: erreur LDAP : ".$result->code." - ".$result->error, "W" );
        return 0;
    }

    return 1;
}


sub deleteLdapEntity {
    my $self = shift;
    my( $ldapEntry ) = @_;

    if( !defined($self->{"ldapConn"}->{"conn"}) ) {
        return 0;
    }
    my $ldapConn = $self->{"ldapConn"}->{"conn"};


    if( !defined($ldapEntry) ) {
        return 0;
    }

    my $result = $ldapConn->delete( $ldapEntry->dn );

    if( $result->is_error() ) {
        &OBM::toolBox::write_log( "ldapEngine: erreur : ".$result->code." - ".$result->error, "W" );
    }

    return 1;
}


sub findDn {
    my $self = shift;
    my( $dn ) = @_;

    my $scope="base";
    my $ldapFilter = "(objectclass=*)";
    my $ldapConn = $self->{"ldapConn"}->{"conn"};

    if( !defined($ldapConn) || !defined($dn) ) {
        return undef;
    }

    my $result = $ldapConn->search(
                    base => to_utf8( { -string => $dn, -charset => $defaultCharSet } ),   
                    scope => $scope,
                    filter => $ldapFilter
                    );

    if( !defined($result) ) {
        return undef;

    }elsif( ($result->code != 32) && $result->is_error() ) {
        # L'erreur 'No such object' n'est, dans ce cas, pas considérée comme un
        # erreur.
        &OBM::toolBox::write_log( "ldapEngine: erreur: probleme lors d'une requête LDAP : ".$result->code." - ".$result->error, "W" );
        return undef;

    }elsif( $result->count != 1 ) {
        return undef;

    }

    return $result->entry(0);
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
            my $parentDn = $self->_findTypeParentDn( $ldapStruct->{"branch"}->[$i], $type, $domainId );
            if( defined($parentDn) ) {
                return $parentDn;
            }
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


sub _findDomainbyId {
    my $self = shift;
    my( $domainId ) = @_;
    my $domainDesc = undef;

    if( !defined($domainId) || ($domainId !~ /^\d+$/) ) {
        return undef;
    }

    for( my $i=0; $i<=$#{$self->{"domainList"}}; $i++ ) {
        if( $self->{"domainList"}->[$i]->{"domain_id"} == $domainId ) {
            $domainDesc = $self->{"domainList"}->[$i];
            last;
        }
    }

    return $domainDesc;
}


sub _doWork {
    my $self = shift;
    my( $dn, $object ) = @_;


    my $ldapEntry = $self->findDn($dn);
    if( defined($ldapEntry) && ($object->getDelete() || $object->getArchive()) ) {
        # Suppression
        &OBM::toolBox::write_log( "ldapEngine: suppression du noeud : ".$dn, "W" );

        if( !$self->deleteLdapEntity($ldapEntry) ) {
            return 0;
        }

    }elsif( defined($ldapEntry) && !($object->getDelete() || $object->getArchive()) ) {
        # Mise à jour
        if( $object->updateLdapEntry($ldapEntry) ) {
            &OBM::toolBox::write_log( "ldapEngine: mise a jour du noeud : ".$dn, "W" );

            if( !$self->updateLdapEntity($ldapEntry) ) {
                return 0;
            }
        }

    }elsif( !defined($ldapEntry) && !($object->getDelete() || $object->getArchive()) ) {
        # Création
        my $ldapEntry = Net::LDAP::Entry->new;
        if( $object->createLdapEntry($ldapEntry) ) {
            &OBM::toolBox::write_log( "ldapEngine: creation du noeud : ".$dn, "W" );

            # On positionne le DN
            $ldapEntry->dn( to_utf8( { -string => $dn, -charset => $defaultCharSet } ) );

            if( !$self->updateLdapEntity($ldapEntry) ) {
                return 0;
            }
        }
    }


    return 1;
}


sub update {
    my $self = shift;
    my( $object ) = @_;

    if( !defined($object) ) {
        return 0;
    }elsif( !defined($object->{"type"}) ) {
        return 0;
    }elsif( !defined($object->{"domainId"}) ) {
        return 0;
    }

    my $domainDesc = $self->_findDomainbyId($object->{"domainId"});

    my $parentDn = $self->_findTypeParentDn( $self->{"ldapStruct"}, $object->{"type"}, $object->{"domainId"} );
    if( !defined($parentDn) ) {
        # Le fait que l'entité n'a pas de DN parent signifie simplement qu'elle n'a pas
        # de représentation LDAP, ce n'est donc pas une erreur fatale.
        return 1;
    }

    my $ldapPrefix = $object->getLdapDnPrefix();
    if( !defined($ldapPrefix) ) {
        # Si par contre elle a un DN parent mais pas de DN propre, c'est une
        # erreur fatale.
        return 0;
    }
    my $objectDn = $ldapPrefix.",".$parentDn;

    return $self->_doWork( $objectDn, $object );
}
