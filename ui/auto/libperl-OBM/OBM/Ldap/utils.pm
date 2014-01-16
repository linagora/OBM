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


package OBM::Ldap::utils;

$VERSION = '1.0';

$debug = 1;


use 5.006_001;
require Exporter;
use strict;
use utf8;


sub _modifyAttr {
    my $self = shift;
    my( $newValue, $ldapEntry, $attr ) = @_;
    my $update = 0;

    if( ref($newValue) eq 'ARRAY' ) {
        return $self->_modifyAttrList( $newValue, $ldapEntry, $attr );
    }


    # L'attribut n'est plus renseigné
    if( !defined( $newValue ) || ($newValue eq '') ) {
        if( defined($ldapEntry->get_value($attr)) ) {
            $self->_log( 'suppression de l\'attribut LDAP \''.$attr.'\'', 4 );
            $ldapEntry->delete( $attr => [] );

            # Il y a eu modification
            $update = 1;
        }
        
    }elsif( !defined($ldapEntry->get_value($attr)) ) {
        # L'attribut n'existe pas
        if( defined( $newValue ) && ($newValue ne '') ) {
            $self->_log( 'mise à jour de l\'attribut LDAP \''.$attr.'\', avec la valeur \''.$newValue.'\'', 4 );
            $ldapEntry->add( $attr => $newValue );

            # Il y a eu modification
            $update = 1;
        }
            
    }else {
        # La valeur de l'attribut doit être mise à jour
        my $ldapVal = $ldapEntry->get_value($attr);

        # Cree de nouvelles valeurs pour comparaison, pour eviter des problemes de double encodages dans certains cas
        my $ldapValCompare = $ldapVal;
        utf8::upgrade($ldapValCompare);
        my $newValueCompare = $newValue;
        utf8::encode($newValueCompare);
        
        if( $ldapValCompare ne $newValueCompare ){
            $self->_log( 'mise à jour de l\'attribut LDAP \''.$attr.'\', avec la valeur \''.$newValue.'\'', 4 );
            $ldapEntry->replace( $attr => $newValue );
                    
            # Il y a eu modification
            $update = 1;
        }
    }

    return $update;
}


sub _modifyAttrList {
    my $self = shift;
    my( $newValues, $ldapEntry, $attr ) = @_;
    my $update = 0;

    if( !defined($newValues) ) {
        if( $ldapEntry->get_value( $attr, asref => 1) ) {
            $ldapEntry->delete( $attr => [ ] );
            $update = 1;
        }

        return $update;

    }elsif( ref($newValues) ne 'ARRAY' ) {
        return $update;
    }

    if( $#{$newValues} < 0 ) {
        if( $ldapEntry->get_value( $attr, asref => 1) ) {
            $ldapEntry->delete( $attr => [ ] );
            $update = 1;
        }

        return $update,
    }


    my $ldapValues = $ldapEntry->get_value( $attr, asref => 1);
    if( $newValues && !defined( $ldapValues ) ) {
        # cet attribut n'est pas defini dans LDAP mais est defini dans la
        # description en BD
        if( $#$newValues != -1 ) {
            $self->_log( 'mise à jour de l\'attribut LDAP \''.$attr.'\'', 4 );
            $ldapEntry->add( $attr => $newValues );

            # Il y a eu modification
            $update = 1;
        }
    }elsif( ($#{$newValues} >= 0) && defined( $ldapValues ) ) {
        # Cet attribut est defini dans LDAP et dans la description de l'utilisateur
        if( $#$ldapValues != $#$newValues ) {
            $ldapEntry->delete( $attr => [ ] );

            if( $#$newValues != -1 ) {
                $self->_log( 'mise à jour de l\'attribut LDAP \''.$attr.'\'', 4 );
                $ldapEntry->add( $attr => $newValues );
                        
                # Il y a eu modification
                $update = 1;
            }
        }else {
            my @ldapValuesSort = sort( @{$ldapValues} );
            my @userDescSort = sort( @{$newValues} );

            my $i = 0;
            while($i<=$#ldapValuesSort) {
                utf8::upgrade($ldapValuesSort[$i]);
                utf8::encode($userDescSort[$i]);
                if($ldapValuesSort[$i] ne $userDescSort[$i]) {
                    last;
                }

                $i++;
            }

            # Si on sort de la boucle parce que 1 valeur de l'attribut dans ldap n'a pas
            # d'equivalent dans la description de l'utilisateur, c'est
            # qu'il y a eu modification...
            if( $i<=$#ldapValuesSort ) {
                $self->_log( 'mise à jour de l\'attribut LDAP \''.$attr.'\'', 4 );
                $ldapEntry->delete( $attr => [ ] );
                $ldapEntry->add( $attr => $newValues );

                # Il y a eu modification
                $update = 1;
            }
        }
    }elsif( defined( $ldapValues ) ) {
        # Attribut definies dans LDAP mais pas dans la description de l'utilisateur
        $self->_log( 'suppression de l\'attribut LDAP \''.$attr.'\'', 4 );
        $ldapEntry->delete( $attr => [ ] );

        # Il y a eu modification
        $update = 1;
    }

    return $update;
}


sub _diffObjectclassAttrs {
    my $self = shift;
    my( $deleteObjectclass, $origObjectclass, $objectclassDesc ) = @_;
    my %deleteAttrs;
    my %origAttrs;
    my %diffAttrs;

    if( ref($deleteObjectclass) ne 'ARRAY' ) {
        return undef;
    }

    if( ref($origObjectclass) ne 'ARRAY' ) {
        return undef;
    }

    if( ref($objectclassDesc) ne 'HASH' ) {
        return undef;
    }


    for( my $i=0; $i<=$#{$deleteObjectclass}; $i++ ) {
        my $currentObjectclassDesc = $objectclassDesc->{$deleteObjectclass->[$i]};
        for( my $j=0; $j<=$#{$currentObjectclassDesc}; $j++ ) {
            $deleteAttrs{$currentObjectclassDesc->[$j]->{name}} = 1;
        }
    }


    for( my $i=0; $i<=$#{$origObjectclass}; $i++ ) {
        my $currentObjectclassDesc = $objectclassDesc->{$origObjectclass->[$i]};
        for( my $j=0; $j<=$#{$currentObjectclassDesc}; $j++ ) {
            $origAttrs{$currentObjectclassDesc->[$j]->{name}} = 1;
        }
    }


    # On détermine les attributs à supprimer en repérant ceux qui appartiennent
    # à une classe à supprimer, sans appartenir à une classe restante
    while( my($key, $value) = each(%deleteAttrs) ) {
        if( !exists($origAttrs{$key}) ) {
            $diffAttrs{$key} = 1;
        }
    }


    my @diffAttrs = keys(%diffAttrs);
    return \@diffAttrs;
}
