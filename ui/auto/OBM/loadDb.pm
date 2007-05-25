package OBM::loadDb;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
require overload;
use Carp;


require OBM::dbUtils;
require OBM::toolBox;


# Definition des attributs de l'objet
my %loadDbAttr = (
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
            $loadDbAttr{"user"} = $parameters->{"user"};

            # On recupere l'id du domaine de l'utilisateur
            my $queryResult;
            my $query = "SELECT userobm_domain_id FROM P_UserObm WHERE userobm_id=".$loadDbAttr{"user"};
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
                    $loadDbAttr{"domain"} = $results->[0]->[0];
                }
            }

            $loadDbAttr{"filter"} = "user";
            last SWITCH;
        }

        if( defined($parameters->{"domain"}) ) {
            $loadDbAttr{"domain"} = $parameters->{"domain"};
            $loadDbAttr{"filter"} = "domain";
            last SWITCH;
        }

        if( defined($parameters->{"delegation"}) ) {
            $loadDbAttr{"delegation"} = $parameters->{"delegation"};
            $loadDbAttr{"filter"} = "delegation";
            last SWITCH;
        }
    }

    print $loadDbAttr{"user"}."\n";
    print $loadDbAttr{"domain"}."\n";
    print $loadDbAttr{"delegation"}."\n";
    print $loadDbAttr{"filter"}."\n";

    my $self = \%loadDbAttr ;

    bless( $self, $obj );
    return $self;
}


