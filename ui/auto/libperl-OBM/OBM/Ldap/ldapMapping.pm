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


package OBM::Ldap::ldapMapping;

$VERSION = '1.0';

use Class::Singleton;
use OBM::Log::log;
@ISA = ('Class::Singleton', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub _new_instance {
    my $class = shift;

    my $self = bless { }, $class;

    $self->{'xmlFile'} = '/etc/obm/automateLdapMapping.xml';

    if ( ! -f $self->{'xmlFile'} ) {
        $self->_log( 'Erreur fatale: le fichier XML de description de l\'annuaire LDAP \''.$self->{'xmlFile'}.'\' n\'est pas accessible', 0 );
        return undef;
    }

    if(!open(XML, "<:utf8", $self->{'xmlFile'})) {
        $self->_log('Erreur fatale: le fichier XML de description de l\'annuaire LDAP \''.$self->{'xmlFile'}.'\' ne peut pas être lu', 0);
        return undef;
    }
    my @xmlFile = <XML>;
    close(XML);

    require XML::Simple;
    my $xmlParser = XML::Simple->new( ForceArray => [ 'entity', 'objectclass', 'map' ] );
    $self->_log( 'chargement du fichier XML de description de l\'annuaire LDAP \''.$self->{'xmlFile'}.'\'', 3 );
    $self->{'xml'} = $xmlParser->XMLin(join('', @xmlFile));

    if( !defined($self->{'xml'}) ) {
        $self->_log( 'Erreur fatale: échec du chargement du fichier XML de description de l\'annuaire LDAP \''.$self->{'xmlFile'}.'\'', 0 );
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );
}


sub getObjectClass {
    my $self = shift;
    my( $entity, $currentObjectClass ) = @_;
    my $entityType = ref($entity);

    if( !defined($self->{'xml'}->{'entity'}->{$entityType}) ) {
        $self->_log( 'entité de type \''.$entityType.'\' inconnue', 1 );
        return undef;
    }

    my $objectClassDesc = $self->{'xml'}->{'entity'}->{$entityType}->{'objectclass'};
    if( !defined($objectClassDesc) || (ref($objectClassDesc) ne 'HASH') ) {
        $self->_log( 'pas d\'objectclass définis pour les entités de type \''.$entityType.'\'', 1 );
        return undef;
    }

    # add objectclass from LDAP entry
    if( defined($currentObjectClass) && (ref($currentObjectClass) eq 'ARRAY') ) {
        for( my $i=0; $i<=$#{$currentObjectClass}; $i++ ) {
            if( !exists($objectClassDesc->{$currentObjectClass->[$i]}) ) {
                $objectClassDesc->{$currentObjectClass->[$i]} = undef;
            }
        }
    }

    my @objectClass = keys( %{$objectClassDesc} );
    my @deletedObjectclass;
    my %realObjectClass;
    for( my $i=0; $i<=$#objectClass; $i++ ) {
        if( defined($objectClassDesc->{$objectClass[$i]}->{'condition'}) ) {
            my $condition = $objectClassDesc->{$objectClass[$i]}->{'condition'};
            if( defined($condition) && defined($entity->getDesc($condition)) && !$entity->getDesc($condition) ) {
                push( @deletedObjectclass, $objectClass[$i] );
                next;
            }
        }

        $realObjectClass{$objectClass[$i]} = '';
    }

    while( my( $oc, $desc ) = each(%{$objectClassDesc}) ) {
        if( defined($desc->{'condition'}) && $entity->getDesc($desc->{'condition'}) ) {
            $realObjectClass{$oc} = '';
        }
    }

    return {
        'objectClass' => eval{ my @realObjectClass = keys(%realObjectClass); return \@realObjectClass},
        'deletedObjectclass' => \@deletedObjectclass
        };
}


sub getRdn {
    my $self = shift;
    my( $entity ) = @_;

    my $entityType = $entity;
    if( ref($entity) ) {
        $entityType = ref($entity);
    }

    if( !defined($self->{'xml'}->{'entity'}->{$entityType}) ) {
        $self->_log( 'entité de type \''.$entityType.'\' inconnue', 1 );
        return undef;
    }

    my $ldapEntityDesc = $self->{'xml'}->{'entity'}->{$entityType};
    if( !defined($ldapEntityDesc->{'rdn'}) ) {
        $self->_log( 'pas de RDN défini pour l\'entité de type \''.$entityType.'\'', 1 );
        return undef;
    }

    my $attrsMapping = $ldapEntityDesc->{'map'};
    if( !defined($attrsMapping) || (ref($attrsMapping) ne 'ARRAY') ) {
        $self->_log( 'mapping des attributs LDAP non définis ou incorrect, vérifiez le fichier XML', 1 );
        return undef;
    }

    for( my $i=0; $i<=$#{$attrsMapping}; $i++ ) {
        my $currentMapping = $attrsMapping->[$i];
        if( lc($currentMapping->{'ldap'}->{'name'}) eq lc($ldapEntityDesc->{'rdn'}->{'ldap'}->{'name'}) ) {
            return $currentMapping;
        }
    }

    $self->_log( 'mapping de l\'attribut LDAP \''.$ldapEntityDesc->{'rdn'}->{'ldap'}->{'name'}.'\', constituant le RDN de l\'entité de type \''.$entityType.'\', non défini', 1 );
    return undef;
}


sub getCurrentRdn {
    my $self = shift;
    my( $entity ) = @_;
    my $entityType = ref($entity);

    if( !defined($self->{'xml'}->{'entity'}->{$entityType}) ) {
        $self->_log( 'entité de type \''.$entityType.'\' inconnue', 1 );
        return undef;
    }

    my $ldapEntityDesc = $self->{'xml'}->{'entity'}->{$entityType};
    if( !defined($ldapEntityDesc->{'rdn'}) ) {
        $self->_log( 'pas de RDN défini pour l\'entité de type \''.$entityType.'\'', 1 );
        return undef;
    }

    return $ldapEntityDesc->{'rdn'};
}


sub getAllAttrsMapping {
    my $self = shift;
    my( $entity, $exception, $withoutRdn ) = @_;
    my $entityType = ref($entity);

    if( !defined($self->{'xml'}->{'entity'}->{$entityType}) ) {
        $self->_log( 'entité de type \''.$entityType.'\' inconnue', 1 );
        return undef;
    }

    my $ldapEntityDesc = $self->{'xml'}->{'entity'}->{$entityType};

    if( ref($ldapEntityDesc->{'map'}) ne 'ARRAY' ) {
        return [];
    }

    if( ref($exception) ne 'ARRAY' ) {
        return $ldapEntityDesc->{'map'};
    }

    my @ldapMapping;
    for( my $i=0; $i<=$#{$ldapEntityDesc->{'map'}}; $i++ ) {
        my $map = $ldapEntityDesc->{'map'}->[$i];
        my $desc = lc($map->{'desc'}->{'name'});

        if($withoutRdn && ($map->{'ldap'}->{'name'} eq $ldapEntityDesc->{'rdn'}->{'ldap'}->{'name'})) {
            next;
        }
        
        my $j = 0;
        while( ($j<=$#{$exception}) && ($desc ne lc($exception->[$j])) ) {
            $j++;
        }

        if( $j>$#{$exception} ) {
            push( @ldapMapping, $map );
        }
    }

    return \@ldapMapping;
}


sub getAttrsMapping {
    my $self = shift;
    my( $entity, $attrList ) = @_;
    my $entityType = ref($entity);

    if( !defined($self->{'xml'}->{'entity'}->{$entityType}) ) {
        $self->_log( 'entité de type \''.$entityType.'\' inconnue', 1 );
        return undef;
    }

    my $ldapEntityDesc = $self->{'xml'}->{'entity'}->{$entityType};

    if( ref($ldapEntityDesc->{'map'}) ne 'ARRAY' ) {
        return [];
    }

    if( ref($attrList) ne 'ARRAY' ) {
        return [];
    }

    my @ldapMapping;
    for( my $i=0; $i<=$#{$attrList}; $i++ ) {
        my $attr = lc($attrList->[$i]);

        my $j = 0;
        while( $j<=$#{$ldapEntityDesc->{'map'}} ) {
            my $map = $ldapEntityDesc->{'map'}->[$j];

            if( $attr eq lc($map->{'desc'}->{'name'}) ) {
                push( @ldapMapping, $map );
                last;
            }

            $j++;
        }
    }

    return \@ldapMapping;
}
