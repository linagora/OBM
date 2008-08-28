#########################################################################
# OBM           - File : OBM::sieve.pm (Perl Module)                    #
#               - Desc : Librairie Perl pour aliamin                    #
#########################################################################
# Cree le 2005-07-21                                                    #
#########################################################################
# $Id$                 #
#########################################################################
package OBM::imapd;

require OBM::toolBox;
require OBM::utils;
require OBM::Tools::obmDbHandler;
use OBM::Parameters::cyrusConf;
require Exporter;

@ISA = qw(Exporter);
@EXPORT_const = qw();
@EXPORT_function = qw();
@EXPORT = (@EXPORT_function, @EXPORT_const);
@EXPORT_OK = qw();


# Necessaire pour le bon fonctionnement du package
$debug=1;


sub getAdminImapPasswd {
    my( $domainList ) = @_;
    my $cyrusAdmin = &OBM::utils::cloneStruct(OBM::Parameters::cyrusConf::cyrusAdmin);

    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( 'Connecteur a la base de donnee invalide', 'W', 3 );
        return 0;
    }

    # Le statement handler (pointeur sur le resultat)
    my $queryResult;

    # La requete a executer - obtention des informations sur l'administrateur de
    # la messagerie.
    my $query = "SELECT usersystem_password FROM UserSystem WHERE usersystem_login='".$cyrusAdmin->{"login"}."'";

    # On execute la requete
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 0;
    }

    if( !(($cyrusAdmin->{"passwd"}) = $queryResult->fetchrow_array) ) {
        &OBM::toolBox::write_log( "Echec: mot de passe de l'administrateur IMAP inconnu", "W" );
        return 0;
    }

    # Si on a recupere un resultat, c'est bon...
    $queryResult->finish;

    # On positionne le login et mot de passe au niveau de la description des
    # serveurs
    for( my $i=0; $i<=$#$domainList; $i++ ) {
        for( my $j=0; $j<=$#{$domainList->[$i]->{"imap_servers"}}; $j++ ) {
            $domainList->[$i]->{"imap_servers"}->[$j]->{"imap_server_login"} = $cyrusAdmin->{"login"};
            $domainList->[$i]->{"imap_servers"}->[$j]->{"imap_server_passwd"} = $cyrusAdmin->{"passwd"};
        }
    }

    return 1;
}
