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
require OBM::toolBox;
require DBI;
require Exporter;

@ISA = qw(Exporter);
@EXPORT_const = qw();
@EXPORT_function = qw(dbState execQuery);
@EXPORT = (@EXPORT_const, @EXPORT_function);
@EXPORT_OK = qw();

# Necessaire pour le bon fonctionnement du package
$debug=1;


sub dbState {
	local($action, *dbh) = @_;

	if( $action eq "connect" ) {
		# On établie la connection à la base
		$dbh = DBI->connect($db, $userDb, $userPasswd);

		# On teste si la connexion a reussie
		if( $dbh ) {
            SWITCH: {
                if( $dbType eq 'mysql' ) {
                    $dbh->{'mysql_enable_utf8'} = 1;
                    $dbh->do('SET NAMES utf8');
                }
            }

			return 1;
		}else {
			return 0;
		}

	}elsif( $action eq "disconnect" ) {
		# On se déconnecte de la base
		$dbh->disconnect;

		undef $dbh;
		return 1;
	}else {
		# Problème avec le paramètre $action
		return 0;
	}
}


# Retour :
#   undef : si erreur
#   nombre de lignes affectées par la requête :
#       - si 'SELECT', O peut simplement signifier qu'il est impossible de
#         connaître le nombre de lignes à l'avance. Dans ce cas le 0 pourra être
#         interprété comme un 'true' ;
#       - si non 'SELECT', si il n'est pas possiblde de connaître le nombre de
#         lignes affectées, la valeur retour est '-1'.
sub execQuery {
	local($query, $dbh, *sth) = @_;

	# On vérifie que la rêquete n'est pas nulle
	if( !defined($query) || ($query eq "") ) {
		return undef;

	}else {
		# On prépare la rêquete, puis on l'exécute et analyse la valeur
		# retour.
        &OBM::toolBox::write_log( '[dbUtils]: requete a executer : \''.$query.'\'', 'W', 3 );
		$sth = $dbh->prepare( $query );
		my $rv = $sth->execute();

		if( !defined($rv) ) {
            return undef;
		}

        if( $query =~ /^SELECT/i ) {
            if( $sth->{NUM_OF_FIELDS} > 0 ) {
                return $sth->{NUM_OF_FIELDS};
            }else {
                return $rv;
            }
        }else {
            return $rv;
        }
	}

    return undef;
}
