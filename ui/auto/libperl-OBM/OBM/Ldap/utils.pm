package OBM::Ldap::utils;

require Exporter;

use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
use strict;


sub modifyAttr {
    my( $newValue, $ldapEntry, $attr ) = @_;
    require OBM::Parameters::common;
    my $update = 0;

    # L'attribut n'est plus renseigné
    if( !defined( $newValue ) || ($newValue eq "") ) {
        if( defined($ldapEntry->get_value($attr)) ) {
            $ldapEntry->delete( $attr => [] );

            # Il y a eu modification
            $update = 1;
        }
        
    }elsif( !defined($ldapEntry->get_value($attr)) ) {
        # L'attribut n'existe pas
        if( defined( $newValue ) && ($newValue ne "") ) {
            $ldapEntry->add( $attr => to_utf8({ -string => $newValue, -charset => $OBM::Parameters::common::defaultCharSet }) );

            # Il y a eu modification
            $update = 1;
        }
            
    }else {
        # La valeur de l'attribut doit être mise à jour
        if( from_utf8( { -string => $ldapEntry->get_value($attr), -charset => $OBM::Parameters::common::defaultCharSet } ) ne $newValue ){
            $ldapEntry->replace( $attr => to_utf8({ -string => $newValue, -charset => $OBM::Parameters::common::defaultCharSet }) );
                    
            # Il y a eu modification
            $update = 1;
        }
    }

    return $update;
}


sub modifyAttrList {
    my( $newValues, $ldapEntry, $attr ) = @_;
    my $update = 0;

    if( !defined($newValues) ) {
        if( $ldapEntry->get_value( $attr, asref => 1) ) {
            $ldapEntry->delete( $attr => [ ] );
            $update = 1;
        }

        return $update;

    }elsif( defined($newValues) && (uc(ref($newValues)) ne "ARRAY" ) ) {
        return $update;

    }

    # On converti les nouvelles valeurs dans le bon encodage
    for( my $i=0; $i<=$#{$newValues}; $i++ ) {
        $newValues->[$i] = to_utf8({ -string => $newValues->[$i], -charset => $OBM::Parameters::common::defaultCharSet });
    }

    my $ldapValues = $ldapEntry->get_value( $attr, asref => 1);
    if( $newValues && !defined( $ldapValues ) ) {
        # cet attribut n'est pas defini dans LDAP mais est defini dans la
        # description en BD
        if( $#$newValues != -1 ) {
            $ldapEntry->add( $attr => $newValues );

            # Il y a eu modification
            $update = 1;
        }
    }elsif( ($#{$newValues} >= 0) && defined( $ldapValues ) ) {
        # Cet attribut est defini dans LDAP et dans la description de l'utilisateur
        if( $#$ldapValues != $#$newValues ) {
            $ldapEntry->delete( $attr => [ ] );

            if( $#$newValues != -1 ) {
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
                $ldapEntry->delete( $attr => [ ] );
                $ldapEntry->add( $attr => $newValues );

                # Il y a eu modification
                $update = 1;
            }
        }
    }elsif( defined( $ldapValues ) ) {
        # Attribut definies dans LDAP mais pas dans la description de l'utilisateur
        $ldapEntry->delete( $attr => [ ] );

        # Il y a eu modification
        $update = 1;
    }

    return $update;
}


sub addAttrList { 
    my( $newAttrList, $ldapEntry, $attr ) = @_;
    my $update = 2;

    if( !ref($newAttrList) || (uc(ref($newAttrList)) ne "ARRAY") ) {
        return $update;
    }

    $update = 1;
    my $ldapValues = $ldapEntry->get_value( $attr, asref => 1);
    for( my $i=0; $i<=$#{$newAttrList}; $i++ ) {
        my $j = 0;

        while( ($j<=$#{$ldapValues}) && (lc($newAttrList->[$i]) ne lc($ldapValues->[$j])) ) {
            $j++;
        }

        if( $j>$#{$ldapValues} ) {
            push( @{$ldapValues}, $newAttrList->[$i] );
            $update = 0;
        }
    }

    if( !$update ) {
        $ldapEntry->delete( $attr => [ ] );
        $ldapEntry->add( $attr => $ldapValues );
    }

    return $update;
}


sub diffObjectclassAttrs {
    my( $deleteObjectclass, $origObjectclass, $objectclassDesc ) = @_;
    my %deleteAttrs;
    my %origAttrs;
    my %diffAttrs;

    if( ref($deleteObjectclass) ne "ARRAY" ) {
        return undef;
    }

    if( ref($origObjectclass) ne "ARRAY" ) {
        return undef;
    }

    if( ref($objectclassDesc) ne "HASH" ) {
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
