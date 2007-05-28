#########################################################################
# OBM:          - File : OBM::dbUtils.pm (Perl Module)                  #
#               - Desc : Librairie Perl pour OBM                        #
#               Les fonctions de gestion des acces a la BD              #
#########################################################################
# Cree le 2002-07-19                                                    #
#########################################################################
# $Id$   #
#########################################################################
package OBM::dbUtils;

use OBM::Parameters::common;
require DBI;
require Exporter;

@ISA = qw(Exporter);
@EXPORT_const = qw();
@EXPORT_function = qw(dbState execQuery);
@EXPORT = (@EXPORT_const, @EXPORT_function);
@EXPORT_OK = qw();

#
# Necessaire pour le bon fonctionnement du package
$debug=1;


sub dbState {
	local($action, *dbh) = @_;

	if( $action eq "connect" )
	{
		# On etablie la connection a la base
		$dbh = DBI->connect($db, $userDb, $userPasswd);

		# On teste si la connexion a reussie
		if( $dbh )
		{
			return 1;
		}else
		{
			return 0;
		}

	}elsif( $action eq "disconnect" )
	{
		# On se deconnecte de la base
		$dbh->disconnect;

		undef $dbh;
		return 1;
	}else
	{
		# Probleme avec le parametre $action
		return 0;
	}
}


sub execQuery {
	local($query, $dbh, *sth) = @_;

	# On verifie que la requete n'est pas nulle
	if( !defined($query) || ($query eq "") ) {
		return 0;

	}else {
		# On prepare la requete, puis on l'execute et analyse la valeur
		# retour.
		$sth = $dbh->prepare( $query );
		my $rv = $sth->execute();

		if( $rv ) {
			return 1;
		}else {
			return 0;
		}
	}
}


sub getTableName {
    my( $tableName, $incremental ) = @_;

    if( !$incremental ) {
        $tableName = "P_".$tableName;
    }

    return $tableName;
}
