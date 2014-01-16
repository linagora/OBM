#################################################################################
# Copyright (C) 2011-2014 Linagora
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option) any
# later version, provided you comply with the Additional Terms applicable for OBM
# software by Linagora pursuant to Section 7 of the GNU Affero General Public
# License, subsections (b), (c), and (e), pursuant to which you must notably (i)
# retain the displaying by the interactive user interfaces of the “OBM, Free
# Communication by Linagora” Logo with the “You are using the Open Source and
# free version of OBM developed and supported by Linagora. Contribute to OBM R&D
# by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
# links between OBM and obm.org, between Linagora and linagora.com, as well as
# between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
# from infringing Linagora intellectual property rights over its trademarks and
# commercial brands. Other Additional Terms apply, see
# <http://www.linagora.com/licenses/> for more details.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License and
# its applicable Additional Terms for OBM along with this program. If not, see
# <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
# version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
# applicable to the OBM software.
#################################################################################


package OBM::Password::unixPasswdUpdater;

$VERSION = '1.0';

use OBM::Ldap::ldapEngine;
use OBM::Password::passwd;
use OBM::Ldap::utils;
@ISA = ('OBM::Ldap::ldapEngine', 'OBM::Password::passwd', 'OBM::Ldap::utils');

$debug = 1;

use 5.006_001;
use strict;


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
        $self->_log( 'utilisateur archivé, pas de mise à jour du mot de passe Unix nécessaire', 0 );
        return 0;
    }


    if( !defined($passwd) ) {
        $self->_log( 'pas de nouveau mot de passe', 4 );
        return 0;
    }


    # Get updated entity DN's
    my $currentEntityDNs = $entity->getCurrentDnPrefix();
    for( my $i=0; $i<=$#{$currentEntityDNs}; $i++ ) {
        my $updateLdapEntity = $self->_searchLdapEntityByDN( $currentEntityDNs->[$i] );

        if( defined($updateLdapEntity) && !ref($updateLdapEntity) ) {
            return 1;
        }

        my $update = $entity->setLdapUnixPasswd( $updateLdapEntity, $passwd );

        if( $update ) {
            if( $self->_ldapUpdateEntity($updateLdapEntity) ) {
                $self->_log( 'échec de mise à jour de l\'entrée LDAP', 3 );
                return 1;
            }

        }elsif( !defined($update) ) {
            $self->_log( 'échec de mise à jour du mot de passe Unix', 3 );
            return 1;
        }elsif( !$update ) {
            $self->_log( 'pas de mise à jour du mot de passe Unix nécessaire', 3 );
            return 0;
        }
    }

    $self->_log( 'mot de passe Unix mis à jour', 2 );

    return 0;
}
