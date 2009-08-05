package OBM::Ldap::ldapEngine;

$VERSION = '1.0';

use OBM::Ldap::utils;
use OBM::Tools::commonMethods;
@ISA = ('OBM::Ldap::utils', 'OBM::Tools::commonMethods');

$debug = 1;

use 5.006_001;
use strict;


sub new {
    my $class = shift;
    my $self = undef;

    if( !ref($class) ) {
        $self = bless { }, $class;
    }else {
        $self = $class;
    }

    require OBM::Parameters::common;
    if( !$OBM::Parameters::common::obmModules->{'ldap'} ) {
        $self->_log( 'module OBM-LDAP désactivé, moteur non démarré', 3 );
        return '0 but true';
    }

    require OBM::Ldap::ldapServers;
    if( !($self->{'ldapservers'} = OBM::Ldap::ldapServers->instance()) ) {
        $self->_log( 'initialisation du gestionnaire de serveur LDAP impossible', 3 );
        return undef;
    }

    $self->{'currentEntity'} = undef;
    $self->{'objectclassDesc'} = undef;

    $self->_log( 'démarrage du moteur LDAP', 4 );

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );

    $self->{'ldapservers'} = undef;
    $self->{'currentEntity'} = undef;
    $self->{'objectclassDesc'} = undef;
}


sub update {
    my $self = shift;
    my( $entity ) = @_;


    if( !defined($entity) ) {
        $self->_log( 'entité non définie', 3 );
        return 1;
    }elsif( !ref($entity) ) {
        $self->_log( 'entité incorrecte', 3 );
        return 1;
    }
    $self->{'currentEntity'} = $entity;


    # Obtention des DNs de l'entité mise à jour
    my $updateEntityDNs = $entity->getDnPrefix();

    # Obtention des DNs de l'entité avant mise à jour
    my $currentEntityDNs = $entity->getCurrentDnPrefix();

    if( $#{$updateEntityDNs} != $#{$currentEntityDNs} ) {
        $self->_log( 'Problème avec les DN de l\'entité : '.$entity->getDescription(), 1 );
        return 3;
    }

    for( my $i=0; $i<=$#{$updateEntityDNs}; $i++ ) {
        my $updateLdapEntity = undef;
        if( $updateEntityDNs->[$i] ne $currentEntityDNs->[$i] ) {
            # Si le nouveau DN est différent de l'actuel, on cherche l'entité
            # ayant le nouveau DN
            $updateLdapEntity = $self->_searchLdapEntityByDN( $updateEntityDNs->[$i] );
            if( defined($updateLdapEntity) && !ref($updateLdapEntity) ) {
                return 4;
            }
        }

        # On cherche l'entité de DN courant
        my $currentLdapEntity = $self->_searchLdapEntityByDN( $currentEntityDNs->[$i] );
        if( defined($currentLdapEntity) && !ref($currentLdapEntity) ) {
            return 4;
        }

        my $toDelete = 0;
        if( $entity->getArchive() || $entity->getDelete() ) {
            # Si l'entité est archivée ou à détruire, on la supprime de
            # l'annuaire LDAP
            $toDelete = 1;
        }

        SWITCH: {
            my $errorCode = 6;

            if( defined($currentLdapEntity) && defined($updateLdapEntity) && ($updateEntityDNs->[$i] ne $currentEntityDNs->[$i]) ) {
                # Cas particulier, le DN actuel et le nouveau existent bien que
                # différents...
                # On ignore le DN actuel (qui peut correspondre en fait à un
                # nouvel utilisateur dont le DN est le DN actuel de l'entité a
                # renommer)
                $self->_log( 'l\'ancien DN \''.$currentEntityDNs->[$i].'\' et le nouveau \''.$updateEntityDNs->[$i].'\' existent', 2 );
                $self->_log( 'l\'ancien DN ne correspond plus à cette entité, on l\'ignore', 2 );
                $currentLdapEntity = undef;
            }

            if( defined($currentLdapEntity) && $toDelete ) {
                # Suppression du DN actuel
                $self->_log( 'supression de '.$self->{'currentEntity'}->getDescription().', DN '.$currentEntityDNs->[$i], 2 );

                if( $self->_deleteEntity( $currentLdapEntity ) ) {
                    $self->( 'echec de suppression de '.$self->{'currentEntity'}->getDescription().', DN '.$currentEntityDNs->[$i], 3 );
                    return $errorCode;
                }

                last SWITCH;
            }

            if( !defined($currentLdapEntity) && !defined($updateLdapEntity) && $toDelete ) {
                # DN à supprimer et non existant dans l'annuaire
                $self->_log( 'DN \''.$currentEntityDNs->[$i].'\' non présent dans l\'annuaire, suppression déjà effectuée', 2 );

                last SWITCH;
            }

            if( !defined($currentLdapEntity) && !defined($updateLdapEntity) && !$toDelete ) {
                # Création de l'entité de nouveau DN
                if( $self->_createEntity( $updateEntityDNs->[$i] ) ) {
                    $self->_log( 'echec de création du DN \''.$updateEntityDNs->[$i].'\'', 3 );
                    return $errorCode;
                }

                last SWITCH;
            }

            if( defined($currentLdapEntity) && !$toDelete ) {
                # Mise à jour de l'entité de DN actuel
                if( $self->_updateEntity($currentLdapEntity) ) {
                    $self->_log( 'echec de mise à jour de '.$self->{'currentEntity'}->getDescription().', DN '.$currentEntityDNs->[$i], 3 );
                    return $errorCode;
                }

                if( $self->_updateEntityDn($currentEntityDNs->[$i], $updateEntityDNs->[$i], $currentLdapEntity) ) {
                    $self->_log( 'echec de mise à jour du DN de '.$self->{'currentEntity'}->getDescription().', DN '.$currentEntityDNs->[$i], 3 );
                    return $errorCode;
                }

                last SWITCH;
            }

            if( defined($updateLdapEntity) && !$toDelete ) {
                # Mise à jour de l'entité de nouveau DN
                if( $self->_updateEntity($currentLdapEntity) ) {
                    $self->_log( 'echec de mise à jour de '.$self->{'currentEntity'}->getDescription().', DN '.$updateEntityDNs->[$i], 3 );
                    return $errorCode;
                }

                last SWITCH;
            }

            $self->_log( 'problème de traitement de l\'entité '.$entity->getDescrption(), 1 );
            return 5;
        }
    }


    return 0;
}


sub _searchLdapEntityByDN {
    my $self = shift;
    my( $entityDn ) = @_;

    if( !$entityDn ) {
        $self->_log( 'DN a chercher non défini ou incorrect', 3 );
        return undef;
    }

    # Get LDAP server conn for this entity
    my $ldapServerConn;
    if( !($ldapServerConn = $self->{'ldapservers'}->getLdapServerConn($self->{'currentEntity'}->getLdapServerId())) ) {
        $self->_log( 'problème avec le serveur LDAP de l\'entité : '.$self->{'currentEntity'}->getDescription(), 2 );
        return 2;
    }

    
    $self->_log( 'Recherche du DN \''.$entityDn.'\'', 3 );

    my $result = $ldapServerConn->search(
        base => $entityDn,
        scope => 'base',
        filter => '(objectclass=*)'
    );

    if( $result->code == 32 ) {
        # L'erreur 'No such object' n'est, dans ce cas, pas considérée comme
        # une erreur
        return undef;
    }elsif( $result->is_error() ) {
        $self->_log( 'problème lors de la recherche LDAP \''.$result->code.'\', '.$result->error, 3 );
        return 0;
    }

    return $result->entry(0);
}


sub _ldapUpdateEntity {
    my $self = shift;
    my( $entry ) = @_;

    if( ref($entry) ne 'Net::LDAP::Entry' ) {
        return 1;
    }

    # Get LDAP server conn for this entity
    my $ldapServerConn;
    if( !($ldapServerConn = $self->{'ldapservers'}->getLdapServerConn($self->{'currentEntity'}->getLdapServerId())) ) {
        $self->_log( 'problème avec le serveur LDAP de l\'entité : '.$self->{'currentEntity'}->getDescription(), 2 );
        return 2;
    }


    my $result = $entry->update( $ldapServerConn );

    if( $result->is_error() ) {
        $self->_log( 'erreur LDAP à la mise à jour du DN '.$entry->dn().', '.$result->code().' - '.$result->error(), 0 );

        if( $result->code() == 32 ) {
            $self->_log( 'l\'objet père du DN '.$entry->dn().' n\'existe pas', 1 );
        }

        return $result->code();
    }

    return 0;
}


sub _createEntity {
    my $self = shift;
    my( $entityDn ) = @_;

    if( !$entityDn ) {
        $self->_log( 'DN de l\'entité a créer incorrect', 3 );
        return 1;
    }

    if( !ref($self->{'currentEntity'}) ) {
        $self->_log( 'entité à mettre à jour incorrecte', 3 );
        return 1;
    }


    $self->_log( 'création du DN \''.$entityDn.'\'', 2 );

    require Net::LDAP::Entry;
    my $entry = Net::LDAP::Entry->new();
    if( $self->{'currentEntity'}->createLdapEntry($entityDn, $entry) ) {
        $self->_log( 'problème à la création de l\'entrée LDAP de l\'entité '.$self->{'currentEntity'}->getDescription(), 2 );
        return 1;
    }

    $entry->dn( $entityDn );

    return $self->_ldapUpdateEntity( $entry );
}


sub _updateEntity {
    my $self = shift;
    my( $entry ) = @_;

    if( ref($entry) ne 'Net::LDAP::Entry' ) {
        $self->_log( 'entrée LDAP incorecte', 3 );
        return 1;
    }

    if( !ref($self->{'currentEntity'}) ) {
        $self->_log( 'entité à mettre à jour incorrecte', 3 );
        return 1;
    }

    # Obtention de la description des classes d'objets LDAP
    if( $self->_getObjectclassDesc( $entry ) ) {
        $self->_log( 'problème à l\'obtention de la description des classes LDAP', 3 );
        return 1;
    }

    if( $self->{'currentEntity'}->updateLdapEntry( $entry, $self->{'objectclassDesc'} ) ) {
        $self->_log( 'mise à jour de '.$self->{'currentEntity'}->getDescription().', DN '.$entry->dn(), 2 );
        return $self->_ldapUpdateEntity( $entry );
    }

    # Pas de mises à jour en attente
    return 0;
}


sub _updateEntityDn {
    my $self = shift;
    my( $currentDn, $newDn, $entry ) = @_;

    if( ref($entry) ne 'Net::LDAP::Entry' ) {
        $self->_log( 'entrée LDAP incorecte', 3 );
        return 1;
    }

    if( ref($currentDn) || ref($newDn) || !$currentDn || !$newDn ) {
        $self->_log( 'DN de '.$self->{'currentEntity'}->getDescription().' incorrects', 3 );
        return 1;
    }

    if( $currentDn eq $newDn ) {
        return 0;
    }

    # Le DN de l'entité doit être mis à jour
    my @newRdn = split( ',', $newDn );
    my( $attr, $value ) = split( '=', $newRdn[0] );
    
    if( !$self->_modifyAttr( $value, $entry, $attr ) ) {
        $self->_log( 'problème à la mise à jour du DN de '.$self->{'currentEntity'}->getDescription(), 3 );
        return 1;
    }
    
    $entry->add( newrdn => $newRdn[0] );
    $entry->replace( deleteoldrdn => $currentDn );
    $entry->changetype( 'moddn' );
    if( $self->_ldapUpdateEntity( $entry ) ) {
        $self->_log( 'problème à la mise à jour du DN de '.$self->{'currentEntity'}->getDescription().', DN '.$entry->dn(), 3 );
        return 1;
    }

    $self->_log( 'mise à jour du DN de '.$self->{'currentEntity'}->getDescription().', nouveau DN '.$newDn, 2 );
    return 0;
}


sub _deleteEntity {
    my $self = shift;
    my ( $entry ) = @_;

    if( ref($entry) ne 'Net::LDAP::Entry' ) {
        $self->_log( 'entrée LDAP incorecte', 3 );
        return 1;
    }

    # Get LDAP server conn for this entity
    my $ldapServerConn;
    if( !($ldapServerConn = $self->{'ldapservers'}->getLdapServerConn($self->{'currentEntity'}->getLdapServerId())) ) {
        $self->_log( 'problème avec le serveur LDAP de l\'entité : '.$self->{'currentEntity'}->getDescription(), 2 );
        return 1;
    }

    my $dn = $entry->dn();
    my $result = $ldapServerConn->delete( $dn );

    if( $result->is_error() ) {
        $self->_log( 'erreur LDAP à la suppression de '.$self->{'currentEntity'}->getDescription().', DN '.$dn.' : '.$result->code().' - '.$result->error(), 0 );
    }else {
        $self->_log( 'suppression de l\'entité de '.$self->{'currentEntity'}->getDescription().', DN '.$dn, 2 );
    }

    return $result->code();
}


# Obtention de la description des classes d'objets de l'entité LDAP à traiter
# si non référencées dans $self->{objectclassDesc}
sub _getObjectclassDesc {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $objectclassDesc = $self->{'objectclassDesc'};

    # Get LDAP server conn for this entity
    my $ldapServerConn;
    if( !($ldapServerConn = $self->{'ldapservers'}->getLdapServerConn($self->{'currentEntity'}->getLdapServerId())) ) {
        $self->_log( 'problème avec le serveur LDAP de l\'entité : '.$self->{'currentEntity'}->getDescription(), 2 );
        return 1;
    }


    my $objectObjectclass = $ldapEntry->get_value( 'objectClass', asref => 1);


    for( my $i=0; $i<=$#$objectObjectclass; $i++ ) {
        if( !exists($objectclassDesc->{$objectObjectclass->[$i]}) ) {
            # On construit une table de hachage :
            #   - clé : nom du schéma LDAP ;
            #   - valeur : référence à un tableau contenant une référence à un
            #   hachage contenant la description d'un attribut
            #   (equality, name, oid, desc, aliases, type, single-value,
            #   syntax)
    
            my $ldapSchema = $ldapServerConn->schema;
            @{$objectclassDesc->{lc($objectObjectclass->[$i])}} = $ldapSchema->must($objectObjectclass->[$i]);
            push( @{$objectclassDesc->{lc($objectObjectclass->[$i])}}, $ldapSchema->may($objectObjectclass->[$i]) );

            $self->{'objectclassDesc'} = $objectclassDesc;
        }
    }

    return 0;
}


