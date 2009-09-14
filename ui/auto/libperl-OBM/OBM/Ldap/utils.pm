package OBM::Ldap::utils;

$VERSION = '1.0';

$debug = 1;


use 5.006_001;
require Exporter;
use strict;


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
        if( $ldapEntry->get_value($attr) ne $newValue ){
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
            while( ($i<=$#ldapValuesSort) && ($ldapValuesSort[$i] eq $userDescSort[$i]) ) {
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
