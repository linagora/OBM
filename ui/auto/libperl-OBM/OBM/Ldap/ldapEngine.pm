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
            ldapUser => undef,
            ldapUserDn => undef,
            ldapPasswd => undef,
            conn => undef
        },
        objectclassDesc => undef
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
    my( $checkLdap ) = @_;

    if( !defined($checkLdap) ) {
        $checkLdap = 1;
    }

    if( !$OBM::Parameters::common::obmModules->{"ldap"} ) {
        return 0;
    }

    &OBM::toolBox::write_log( "[Ldap::ldapEngine]: initialisation du moteur", "W" );

    # Creation de l'arbre
    $self->_initTree( $self->{"ldapStruct"}, undef, undef );

    # Initialisation des paramètres de connexions LDAP
    $self->{"ldapConn"}->{"ldapServer"} = $self->{"domainList"}->[0]->{"ldap_admin_server"};
    $self->{"ldapConn"}->{"ldapUser"} = $self->{"domainList"}->[0]->{"ldap_admin_login"};

    my $ldapUserRdn = $self->_makeRdn( { node_type => $OBM::Parameters::ldapConf::SYSTEMUSERS, name => $self->{"ldapConn"}->{"ldapUser"} } );
    $self->{"ldapConn"}->{"ldapUserDn"} = $ldapUserRdn.",".$self->_findTypeParentDn( undef, $OBM::Parameters::ldapConf::SYSTEMUSERS, 0 );
    $self->{"ldapConn"}->{"ldapPasswd"} = $self->{"domainList"}->[0]->{"ldap_admin_passwd"};

    # Etabli la connexion à l'annuaire
    if( !$self->_connectLdapSrv() ) {
        return 0;
    }

    if( $checkLdap ) {
        # On vérifie l'arboréscence
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: verification de l'arborescence de l'annuaire LDAP", "W" );
        if( !$self->checkLdapTree( $self->{"ldapStruct"} ) ) {
            $self->destroy();
            return 0;
        }
    }

    return 1;
}


sub destroy {
    my $self = shift;

    &OBM::toolBox::write_log( "[Ldap::ldapEngine]: arret du moteur", "W" );

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
    my( $ldapConn ) = @_;
    my $ldapStruct = $self->{"ldapStruct"};

    if( !defined($ldapConn) ) {
        $ldapConn = $self->{"ldapConn"};
    }

    if( !defined($ldapConn->{"ldapServer"}) || !defined($ldapConn->{"ldapUserDn"}) || !defined($ldapConn->{"ldapPasswd"}) ) {
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: pas d'information de connexion a l'annuaire LDAP", "W" );
        return 0;
    }


    &OBM::toolBox::write_log( "[Ldap::ldapEngine]: connexion a l'annuaire LDAP '".$ldapConn->{"ldapServer"}."'", "W" );

    $ldapConn->{"conn"} = Net::LDAP->new(
        $ldapConn->{"ldapServer"},
        port => "389",
        debug => "0",
        timeout => "60",
        version => "3"
    ) or &OBM::toolBox::write_log( "[Ldap::ldapEngine]: echec de connexion a LDAP : $@", "W" ) && return 0;

    if( !defined($ldapConn->{"conn"}) ) {
        return 0;
    }

    &OBM::toolBox::write_log( "[Ldap::ldapEngine]: connexion a l'annuaire en tant que '".$ldapConn->{"ldapUserDn"}."'", "W" );

    my $errorCode = $ldapConn->{"conn"}->bind(
        $ldapConn->{"ldapUserDn"},
        password => $ldapConn->{"ldapPasswd"}
    );

    if( $errorCode->code ) {
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: echec de connexion : ".$errorCode->error, "W" );
        return 0;
    }else {
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: connexion a l'annuaire LDAP etablie", "W" );
    }

    return 1;
}


sub _disconnectLdapSrv {
    my $self = shift;
    my( $ldapConn ) = @_;

    if( !defined($ldapConn) ) {
        $ldapConn = $self->{"ldapConn"};
    }

    if( defined($ldapConn->{"conn"}) ) {
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: deconnexion de l'annuaire LDAP '".$ldapConn->{"ldapServer"}."'", "W" );
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
    $ldapStruct->{"rdn"} = $self->_makeRdn( $ldapStruct );
    $ldapStruct->{"dn"} = $ldapStruct->{"rdn"};
    if( defined($parentDn) ) {
        $ldapStruct->{"parentDn"} = $parentDn;
        $ldapStruct->{"dn"} .= ",".$parentDn;
    }
    $ldapStruct->{"domain_id"} = $domainId;

    &OBM::toolBox::write_log( "[Ldap::ldapEngine]: gestion du noeud de type '".$ldapStruct->{"node_type"}."' et de dn : ".$ldapStruct->{"dn"}, "W" );


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

            &OBM::toolBox::write_log( "[Ldap::ldapEngine]: creation de la structure pour le domaine '".$self->{"domainList"}->[$j]->{"domain_dn"}."'", "W" );
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

    $self->_doWork( $ldapStruct->{"parentDn"}, $ldapStruct->{"object"} );

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
        return 1;
    }
    my $ldapConn = $self->{"ldapConn"}->{"conn"};

    if( !defined($ldapEntry) ) {
        return 1;
    }

    my $result = $ldapEntry->update( $ldapConn );

    if( $result->is_error() ) {
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: erreur LDAP : ".$result->code()." - ".$result->error(), "W" );
        return $result->code();
    }

    return 0;
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
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: erreur : ".$result->code." - ".$result->error, "W" );
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
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: erreur: probleme lors d'une requête LDAP : ".$result->code." - ".$result->error, "W" );
        return undef;

    }elsif( $result->count != 1 ) {
        return undef;

    }

    return $result->entry(0);
}


sub _findTypeParentDn {
    my $self = shift;
    my ( $ldapStruct, $type, $domainId ) = @_;

    # Si aucune structure d'arbre LDAP n'est passée en paramètre, on part de la
    # racine
    if( !defined($ldapStruct) ) {
        $ldapStruct = $self->{"ldapStruct"};
    }

    # Si le type demandé est de type 'branche' on stoppe le traitement
    if( $self->{"typeDesc"}->{$type}->{"is_branch"} ) {
        return 0;
    }

    if( defined($ldapStruct->{"data_type"}) && defined($ldapStruct->{"domain_id"}) && ($ldapStruct->{"domain_id"} == $domainId) ) {
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


sub _makeRdn {
    my $self = shift;
    my( $entry ) = @_;

    return $self->{"typeDesc"}->{$entry->{"node_type"}}->{"dn_prefix"}."=".$entry->{"name"};
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
    my( $parentDn, $object ) = @_;
    my $returnCode = 1;
    my( $newLdapEntry, $currentLdapEntry );

    my $currentRdn = $object->getLdapDnPrefix( "current" );
    my $currentDn = $currentRdn;
    if( defined($currentDn) && defined($parentDn) ) {
        $currentDn .= ",".$parentDn;
    }
    $currentLdapEntry = $self->findDn($currentDn);

    my $newRdn = $object->getLdapDnPrefix( "new" );
    my $newDn = $newRdn;
    if( defined($newDn) && defined($parentDn) ) {
        $newDn .= ",".$parentDn;
    }
    $newLdapEntry = $self->findDn($newDn);


    SWITCH: {
        if( defined($currentLdapEntry) && defined($newLdapEntry) && ($currentDn ne $newDn) ) {
            # Cas particulier, le DN actuel et le nouveau existent bien que
            # différents...
            # On ignore le DN actuel (qui peut correspondre en fait à un nouvel
            # utilisateur dont le DN est le DN actuel de l'entité a renommer)
            &OBM::toolBox::write_log( "[Ldap::ldapEngine]: les DN '".$currentDn."' et '".$newDn."' existent. Ignore le DN '".$currentDn."'", "W" );

            $currentLdapEntry = undef;
        }

        if( defined($currentLdapEntry) && ($object->getDelete() || $object->getArchive()) ) {
            # Suppression du DN actuel
            &OBM::toolBox::write_log( "[Ldap::ldapEngine]: suppression du noeud '".$currentDn."'", "W" );
    
            if( !$self->deleteLdapEntity($currentLdapEntry) ) {
                return 0;
            }
        
            last SWITCH;
        }

        if( !defined($currentLdapEntry) && !defined($newLdapEntry) && ($object->getDelete() || $object->getArchive()) ) {
            # Noeud a supprimé non présent dans l'annuaire
            &OBM::toolBox::write_log( "[Ldap::ldapEngine]: noeud '".$currentDn."' non présent dans l'annuaire. Suppression deja effectuee", "W" );

            last SWITCH;
        }

        if( !defined($currentLdapEntry) && !defined($newLdapEntry) && !($object->getDelete() || $object->getArchive()) ) {
            # Création de l'entité de DN nouveau
            my $ldapEntry = Net::LDAP::Entry->new;
            if( $object->createLdapEntry($ldapEntry) ) {
                &OBM::toolBox::write_log( "[Ldap::ldapEngine]: creation du DN '".$newDn."'", "W" );

                $ldapEntry->dn( to_utf8( { -string => $newDn, -charset => $defaultCharSet } ) );

                if( $self->updateLdapEntity($ldapEntry) ) {
                    return 0;
                }
            }else {
                &OBM::toolBox::write_log( '[Ldap::ldapEngine]: probleme a la generation de la representation LDAP de l\'objet : '.$object->getEntityDescription(), 'W', 1 );
                return 0;
            }

            last SWITCH;
        }

        if( defined($currentLdapEntry) && !($object->getDelete() || $object->getArchive()) ) {
            # Mise à jour de l'entité de DN actuel

            # Obtention de la description des classes d'objets
            $self->_getObjectclassDesc( $currentLdapEntry );
    
            my $return = $object->updateLdapEntry($currentLdapEntry, $self->{objectclassDesc});
            if( !defined($return) ) {
                return 0;
            }

            if( $return->getUpdate() ) {
                &OBM::toolBox::write_log( "[Ldap::ldapEngine]: mise a jour du DN '".$currentDn."'", "W" );
    
                if( $self->updateLdapEntity($currentLdapEntry) ) {
                    return 0;
                }
            }

            if( $object->updateLdapEntryDn($currentLdapEntry) ) {
                # Mise à jour du DN de l'entité vers le nouveau DN
                &OBM::toolBox::write_log( "[Ldap::ldapEngine]: renommage du  DN '".$currentDn."', en '".$newDn."'", "W" );
    
                $currentLdapEntry->replace( deleteoldrdn => to_utf8( { -string => $currentDn, -charset => $defaultCharSet } ) );
                $currentLdapEntry->changetype( "moddn" );
                if( $self->updateLdapEntity($currentLdapEntry) ) {
                    return 0;
                }

                $return->setUpdateLinks();
            }

            if( $return->getUpdateLinks() ) {
                $returnCode = 2;
            }

            last SWITCH;
        }

        if( defined($newLdapEntry) && !($object->getDelete() || $object->getArchive()) ) {
            # Mise à jour de l'entité de DN nouveau

            # Obtention de la description des classes d'objets
            $self->_getObjectclassDesc( $newLdapEntry );

            my $return = $object->updateLdapEntry($newLdapEntry, $self->{objectclassDesc});
            if( !defined($return) ) {
                return 0;
            }

            if( $return->getUpdate() ) {
                &OBM::toolBox::write_log( "[Ldap::ldapEngine]: mise a jour du DN '".$newDn."'", "W" );
                
                if( $self->updateLdapEntity($newLdapEntry) ) {
                    return 0;
                }
            }

            if( $return->getUpdateLinks() ) {
                $returnCode = 2;
            }

            last SWITCH;
        }

        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: description BD de : ".$object->getEntityDescription()." incorrecte.", "W" );
        return 0;
    }

    return $returnCode;
}


sub update {
    my $self = shift;
    my( $object ) = @_;


    if( !defined($object) ) {
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: mise a jour d'un objet non definit - Operation annulee !", "W" );
        return 0;
    }elsif( !defined($object->{"type"}) ) {
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: mise a jour d'un objet de type non définit - Operation annulee !", "W" );
        return 0;
    }elsif( !defined($object->{"domainId"}) ) {
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: mise a jour d'un objet de domaine non definit - Operation annulee !", "W" );
        return 0;
    }

    my $domainDesc = $self->_findDomainbyId($object->{"domainId"});
    if( !defined($domainDesc) ) {
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: description du domaine '".$object->{"domainId"}."' non definit - Operation annulee !", "W" );
        return 0;
    }

    my $objectRdn;
    my $parentDn;
    my $return = $self->getObjectParentDN( $object, \$parentDn );
    if( $return == 1 ) {
        return 1;
    }elsif( $return ) {
        return 0;
    }

    my $returnCode = $self->_doWork( $parentDn, $object );
    if( !$returnCode ) {
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: probleme de traitement de l'objet : ".$object->getEntityDescription()." - Operation annulee !", "W" );
        return 0;
    }

    return $returnCode;
}


sub checkUserPasswd {
    my $self = shift;
    my( $object ) = @_;

    if( !defined($object) ) {
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: mise a jour d'un objet non definit - Operation annulee !", "W" );
        return 0;
    }elsif( !defined($object->{"type"}) ) {
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: mise a jour d'un objet de type non définit - Operation annulee !", "W" );
        return 0;
    }elsif( !defined($object->{"domainId"}) ) {
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: mise a jour d'un objet de domaine non definit - Operation annulee !", "W" );
        return 0;
    }

    my $domainDesc = $self->_findDomainbyId($object->{"domainId"});
    if( !defined($domainDesc) ) {
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: description du domaine '".$object->{"domainId"}."' non definit - Operation annulee !", "W" );
        return 0;
    }

    return 1;
}


sub _getObjectclassDesc {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $objectclassDesc = $self->{objectclassDesc};

    if( !defined($self->{ldapConn}->{conn}) ) {
        return 0;
    }
    my $ldapConn = $self->{ldapConn}->{conn};


    my $objectObjectclass = $ldapEntry->get_value( "objectClass", asref => 1);


    for( my $i=0; $i<=$#$objectObjectclass; $i++ ) {
        if( !exists($objectclassDesc->{$objectObjectclass->[$i]}) ) {
            # On construit une table de hachage :
            #   - clé : nom du schéma LDAP ;
            #   - valeur : référence à un tableau contenant une référence à un
            #   hachage contenant la description d'un attribut
            #   (equality, name, oid, desc, aliases, type, single-value,
            #   syntax)
    
            my $ldapSchema = $ldapConn->schema;
            @{$objectclassDesc->{$objectObjectclass->[$i]}} = $ldapSchema->must($objectObjectclass->[$i]);
            push( @{$objectclassDesc->{$objectObjectclass->[$i]}}, $ldapSchema->may($objectObjectclass->[$i]) );

            $self->{objectclassDesc} = $objectclassDesc;
        }
    }

    return 1;
}


sub getObjectParentDN {
    my $self = shift;
    my( $object, $parentDn ) = @_;

    if( !defined( $object ) ) {
        return undef;
    }

    $$parentDn = $self->_findTypeParentDn( undef, $object->{"type"}, $object->{"domainId"} );
    if( !defined($$parentDn) ) {
        # Le fait que l'entité n'ait pas de DN parent signifie simplement qu'elle n'a pas
        # de représentation LDAP, ce n'est donc pas une erreur fatale.
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: type d'objet '".$object->{"type"}."' non definit dans l'arborescence du domaine '".$object->{"domainId"}."'", "W" );
        return 1;
    }

    return 0;
}


sub checkPasswd {
    my $self = shift;
    my( $object, $passwd ) = @_;

    my $parentDn;
    if( $self->getObjectParentDN( $object, \$parentDn ) ) {
        return 0;
    }

    my $objectRdn = $object->getLdapDnPrefix( "current" );
    if( !defined($objectRdn) ) {
        return 0;
    }
    my $objectDn = $objectRdn.",".$parentDn;


    my %ldapConn = (
        ldapServer => $self->{ldapConn}->{ldapServer},
        ldapUserDn => $objectDn,
        ldapPasswd => $passwd
    );

    if( !$self->_connectLdapSrv( \%ldapConn ) ) {
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: ancien mot de passe incorrect", "W" );
        return 0;
    }else {
        &OBM::toolBox::write_log( "[Ldap::ldapEngine]: ancien mot de passe correct", "W" );
        $self->_disconnectLdapSrv( \%ldapConn );
    }

    return 1;
}


sub updatePassword {
    my $self = shift;
    my( $object, $passwordDesc ) = @_;

    my $parentDn;
    if( $self->getObjectParentDN( $object, \$parentDn ) ) {
        return 0;
    }

    my $objectRdn = $object->getLdapDnPrefix( "current" );
    if( !defined($objectRdn) ) {
        return 0;
    }
    my $objectDn = $objectRdn.",".$parentDn;

    my $ldapEntry = $self->findDn($objectDn);
    if( defined($ldapEntry) ) {
        my $update = $object->updateLdapEntryPassword( $ldapEntry, $passwordDesc );
        if( $update ) {
            if( $self->updateLdapEntity( $ldapEntry ) ) {
                &OBM::toolBox::write_log( "[Ldap::ldapEngine]: probleme de traitement de l'objet : ".$object->getEntityDescription()." - Operation annulee !", "W" );
                return 0;
            }
        }
    }else {
        return 0;
    }


    return 1;
}
