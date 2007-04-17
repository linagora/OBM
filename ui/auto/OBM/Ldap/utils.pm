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
    my( $newValue, $ldapEntry, $attr ) = @_;
    my $update = 0;

    my $ldapValues = $ldapEntry->get_value( $attr, asref => 1);
    if( $newValue && !defined( $ldapValues ) ) {
        # cet attribut n'est pas defini dans LDAP mais est defini dans la
        # description en BD
        if( $#$newValue != -1 ) {
            $ldapEntry->add( $attr => $newValue );

            # Il y a eu modification
            $update = 1;
        }
    }elsif( $newValue && defined( $ldapValues ) ) {

        # Cet attribut est defini dans LDAP et dans la description de l'utilisateur
        if( $#$ldapValues != $#$newValue ) {
            $ldapEntry->delete( $attr => [ ] );

            if( $#$newValue != -1 ) {
                $ldapEntry->add( $attr => $newValue );
                        
                # Il y a eu modification
                $update = 1;
            }
        }else {
            my @ldapValuesSort = sort( @{$ldapValues} );
            my @userDescSort = sort( @{$newValue} );

            my $i = 0;
            while( ($i<=$#ldapValuesSort) && ($ldapValuesSort[$i] eq $userDescSort[$i]) ) {
                $i++;
            }

            # Si on sort de la boucle parce que 1 valeur de l'attribut dans ldap n'a pas
            # d'equivalent dans la description de l'utilisateur, c'est
            # qu'il y a eu modification...
            if( $i<=$#ldapValuesSort ) {
                $ldapEntry->delete( $attr => [ ] );
                $ldapEntry->add( $attr => $newValue );

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
