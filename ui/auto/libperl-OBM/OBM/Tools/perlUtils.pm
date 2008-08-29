#########################################################################
# OBM           - File : perlUtils.pm (Perl Module)                     #
#               - Desc : Librairie Perl pour OBM                        #
#               Les fonctions communes n'ayant pas de dependances       #
#########################################################################
# Cree le 2002-07-22                                                    #
#########################################################################
# $Id$   #
#########################################################################
package OBM::Tools::perlUtils;

use Storable qw(dclone);
require Exporter;

@ISA = qw(Exporter);
@EXPORT = ();
@EXPORT_OK = qw();

#
# Nécessaire pour le bon fonctionnement du package
$debug=1;


# Permet de cloner une structure complexe
sub cloneStruct {
    my( $structRef ) = @_;

    return dclone($structRef);
}


# Permet de déterminer si une variable est tainté ou pas
sub is_tainted {
    my( $var ) = @_;
    require Scalar::Util;

    if( Scalar::Util::tainted( $var ) ) {
        return 1;
    }

    return 0;
}
