#########################################################################
# OBM           - File : OBM::utils.pm (Perl Module)                    #
#               - Desc : Librairie Perl pour OBM                        #
#               Les fonctions communes n'ayant pas de dependances       #
#########################################################################
# Cree le 2002-07-22                                                    #
#########################################################################
# $Id$   #
#########################################################################
package OBM::utils;

use Storable qw(dclone);
require Exporter;

@ISA = qw(Exporter);
@EXPORT = ();
@EXPORT_OK = qw();

#
# Nécessaire pour le bon fonctionnement du package
$debug=1;


# Permet d'exécuter une commande système et de retourner le code retour de
# celle-ci
sub execCmd {
    local( $cmd, $verbose ) = @_;

    my $pid;

    if( $pid = fork ) {
        waitpid($pid, 0);
    }else {
        if( $verbose ) {
            exec( $cmd ) or return -1;
        }else {
            exec($cmd." > /dev/null 2>&1") or return -1;
        }
    }

    # on retourne la valeur retournee par le programme execute
    my $retour = $? >> 8;
    return $retour;
}


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
