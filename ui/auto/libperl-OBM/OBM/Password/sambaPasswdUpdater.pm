package OBM::Password::sambaPasswdUpdater;

$VERSION = '1.0';

use OBM::Ldap::ldapEngine;
use OBM::Password::passwd;
use OBM::Ldap::utils;
@ISA = ('OBM::Ldap::ldapEngine', 'OBM::Password::passwd', 'OBM::Ldap::utils');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(
        _log
        dump
        );


sub update {
    my $self = shift;
    my( $entity, $passwd ) = @_;


    if( !defined($entity) ) {
        $self->_log( 'entité non définie', 3 );
        return 1;
    }elsif( !ref($entity) ) {
        $self->_log( 'entité incorrecte', 3 );
        return 1;
    }elsif( ref($entity) ne 'OBM::Entities::obmUser' ) {
        $self->_log( 'type d\'entité \''.ref($entity).' non supporté', 0 );
        return 1;
    }
    $self->{'currentEntity'} = $entity;

    if( $entity->getDesc('userobm_archive') ) {
        $self->_log( 'utilisateur archivé, pas de mise à jour du mot de passe Samba nécessaire', 0 );
        return 0;
    }

    if( !$entity->getDesc('userobm_samba_perms') ) {
        $self->_log( 'utilisateur non Samba, pas de mise à jour du mot de passe Samba nécessaire', 0 );
        return 0;
    }


    if( !defined($passwd) ) {
        $self->_log( 'pas de nouveau mot de passe', 4 );
        return 0;
    }

    my $lmPasswd;
    my $ntPasswd;
    if( $self->_getNTLMPasswd( $passwd, \$lmPasswd, \$ntPasswd ) ) {
        $self->_log( 'echec de conversion du mot de passe Samba', 3 );
        return 1;
    }


    # Obtention des DNs de l'entité mise à jour
    my $currentEntityDNs = $entity->getCurrentDnPrefix();

    for( my $i=0; $i<=$#{$currentEntityDNs}; $i++ ) {
        my $updateLdapEntity = $self->_searchLdapEntityByDN( $currentEntityDNs->[$i] );

        if( defined($updateLdapEntity) && !ref($updateLdapEntity) ) {
            return 1;
        }

        if( $self->_modifyAttr( $ntPasswd , $updateLdapEntity, 'sambaNTPassword' ) && $self->_modifyAttr( $lmPasswd, $updateLdapEntity, 'sambaLMPassword' ) ) {
            if( $self->_ldapUpdateEntity($updateLdapEntity) ) {
                $self->_log( 'échec de mise à jour de l\'entrée LDAP', 3 );
                return 1;
            }
        }else {
            $self->_log( 'échec de mise à jour du mot de passe Unix', 3 );
            return 1;
        }
    }

    $self->_log( 'mot de passe Samba mis à jour', 2 );

    return 0;
}
