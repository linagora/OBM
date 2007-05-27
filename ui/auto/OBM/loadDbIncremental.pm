package OBM::loadDbIncremental;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
require overload;
use Carp;


require OBM::dbUtils;
require OBM::toolBox;


# Definition des attributs de l'objet
my %loadDbIncrementalAttr = (
    filter => undef,
    user => undef,
    domain => undef,
    delegation => undef
);


sub new {
    my( $obj, $dbHandler, $parameters ) = @_;
    $obj = ref($obj) || $obj;

    if( !defined($dbHandler) || !defined($parameters) ) {
        croak( "Usage: PACKAGE->new(PARAMLIST)" );
    }elsif( !exists($parameters->{"user"}) && !exists($parameters->{"domain"}) && !exists($parameters->{"delegation" }) ) {
        croak( "Usage: PARAMLIST: table de hachage avec les cle 'user', 'domain' et 'delegation'" );
    }

    SWITCH: {
        if( defined($parameters->{"user"}) ) {
            $loadDbIncrementalAttr{"user"} = $parameters->{"user"};

            # On recupere l'id du domaine de l'utilisateur
            my $queryResult;
            my $query = "SELECT userobm_domain_id FROM P_UserObm WHERE userobm_id=".$loadDbIncrementalAttr{"user"};
            if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
                &OBM::toolBox::write_log( "lodaDb: probleme lors de l'execution de la requete", "W" );
                if( defined($queryResult) ) {
                    &OBM::toolBox::write_log( $queryResult->err, "W" );
                }
            }else {
                my $results = $queryResult->fetchall_arrayref();
                if( $#$results != 0 ) {
                    &OBM::toolBox::write_log( "lodaDb: utilisateur inexistant", "W" );
                    return undef;
                }else {
                    $loadDbIncrementalAttr{"domain"} = $results->[0]->[0];
                }
            }

            $loadDbIncrementalAttr{"filter"} = "user";
            last SWITCH;
        }

        if( defined($parameters->{"domain"}) ) {
            $loadDbIncrementalAttr{"domain"} = $parameters->{"domain"};
            $loadDbIncrementalAttr{"filter"} = "domain";
            last SWITCH;
        }

        if( defined($parameters->{"delegation"}) ) {
            $loadDbIncrementalAttr{"delegation"} = $parameters->{"delegation"};
            $loadDbIncrementalAttr{"filter"} = "delegation";
            last SWITCH;
        }
    }

    my $self = \%loadDbIncrementalAttr ;

    bless( $self, $obj );
    return $self;
}


