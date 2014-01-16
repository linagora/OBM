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


package OBM::Ldap::ldapDeleteEngine;

$VERSION = '1.0';

use OBM::Ldap::ldapEngine;
@ISA = ('OBM::Ldap::ldapEngine');

$debug = 1;

use 5.006_001;
use strict;

use OBM::Parameters::regexp;


sub update {
    my $self = shift;
    my( $entity ) = @_;


    if( ref($entity) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'entité de type \'OBM::Entities::obmDomain\' non prisent en charge', 3 );
        return 0;
    }
    $self->{'currentEntity'} = $entity;


    $self->_log( 'suppression des entrées LDAP du domaine '.$entity->getDescription(), 2 );
    my $domainEntities = $self->_searchLdapDomainEntitiesDn();

    if( (ref($domainEntities) ne 'ARRAY') && ($domainEntities) ) {
        $self->_log( 'échec à l\'obtention des DN du domaine '.$entity->getDescription(), 0 );
        return 1;
    }

    if( $self->_deleteDomainDn($domainEntities) ) {
        return 1;
    }


    return 0;
}


sub _searchLdapDomainEntitiesDn {
    my $self = shift;

    # Get LDAP server conn for this entity
    my $ldapServerConn;
    if( !($ldapServerConn = $self->{'ldapservers'}->getLdapServerConn($self->{'currentEntity'}->getLdapServerId())) ) {
        $self->_log( 'problème avec le serveur LDAP de l\'entité : '.$self->{'currentEntity'}->getDescription(), 2 );
        return 2;
    }

    my $domainDn = $self->{'currentEntity'}->getCurrentDnPrefix();
    $domainDn = $domainDn->[0];
    $self->_log( 'Recherche des DN du domaine '.$domainDn, 3 );

    my $result = $ldapServerConn->search(
        base => $domainDn,
        scope => 'sub',
        filter => '(objectclass=*)',
        attrs => [ 'dn' ]
    );

    if( $result->code == 32 ) {
        # L'erreur 'No such object' n'est, dans ce cas, pas considérée comme
        # une erreur
        return undef;
    }elsif( $result->is_error() ) {
        $self->_log( 'problème lors de la recherche LDAP \''.$result->code.'\', '.$result->error, 3 );
        return 1;
    }

    my @entries = $result->entries();
    return \@entries;
}


sub _deleteDomainDn {
    my $self = shift;
    my( $entries ) = @_;

    # Get LDAP server conn for this entity
    my $ldapServerConn;
    if( !($ldapServerConn = $self->{'ldapservers'}->getLdapServerConn($self->{'currentEntity'}->getLdapServerId())) ) {
        $self->_log( 'problème avec le serveur LDAP de l\'entité : '.$self->{'currentEntity'}->getDescription(), 2 );
        return 2;
    }

    my $deleteError = 0;
    my $deleteSuccess = 0;
    my $totalEntries = $#$entries;
    while( my $entry = shift(@{$entries}) ) {
        my $resultDelete = $ldapServerConn->delete( $entry->dn() );

        if( $resultDelete->code == 66 ) {
            push( @{$entries}, $entry );
        }elsif( !$resultDelete->is_error() ) {
            $self->_log( 'supression de l\'entité de DN '.$entry->dn(), 2 );
            $deleteSuccess++;
        }else {
            $self->_log( 'erreur à la suppression de l\'entité de DN '.$entry->dn(), 0 );
            $deleteError++;
        }
    }

    if( $deleteSuccess == ($totalEntries+1) ) {
        $self->_log( $deleteSuccess.'/'.($totalEntries+1).' entités supprimées avec succés, '.$deleteError.' erreurs', 3 );
    }else {
        $self->_log( $deleteSuccess.'/'.($totalEntries+1).' entités supprimées avec succés, '.$deleteError.' erreurs', 1 );
    }

    return $deleteError;
}
