package OBM::Tools::obmDbHandler;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use base qw( Class::Singleton );
require DBI;
require OBM::Parameters::common;
use OBM::Tools::commonMethods qw(_log dump);


sub _new_instance {
    my $class = shift;

    my $self = bless { }, $class;

    $self->{'dbHandler'} = undef;
    $self->{'dbHost'} = $OBM::Parameters::common::dbHost;
    $self->{'dbName'} = $OBM::Parameters::common::dbName;
    $self->{'dbType'} = $OBM::Parameters::common::dbType;
    $self->{'dbDriver'} = undef;
    $self->{'dbUser'} = $OBM::Parameters::common::userDb;
    $self->{'dbPassword'} = $OBM::Parameters::common::userPasswd;

    return $self;
}


sub _getDriver {
    my $self = shift;

    SWITCH:{
        if( $self->{'dbType'} eq 'mysql' ) {
            my $moduleInstalled = eval { require DBD::mysql; };
            if( !defined($moduleInstalled) ) {
                $self->_log( 'module DBD::mysql non installé, connexion à la BD impossible', 0 );
                return 1;
            }

            $self->{'dbDriver'} = 'mysql';
            last SWITCH;
        }

        if( $self->{'dbType'} eq 'pgsql' ) {
            my $moduleInstalled = eval { require DBD::Pg; };
            if( !defined($moduleInstalled) ) {
                $self->_log( 'module DBD::Pg non installé, connexion à la BD impossible', 0 );
                return 1;
            }

            $self->{'dbDriver'} = 'Pg';
            last SWITCH;
        }

        $self->_log( 'driver pour les SGBD de type \''.$self->{'dbType'}.'\' inconnu', 1 );
        return 1;
    }

    return 0;
}


sub _driverConnectHook {
    my $self = shift;
    my $dbHandler = $self->{'dbHandler'};

    SWITCH: {
        if( $self->{'dbType'} eq 'mysql' ) {
            # Perldoc said that $dbHandler->{'mysql_enable_utf8'} is needed,
            # but in fact, it seems that it has no effect, or can break accute
            # letter
            $dbHandler->do('SET NAMES utf8');
            last SWITCH;
        }
    }

    return 0;
}


# Return :
#   undef on error, error message can be retrieve via $sth if defined
#   0 (false) : number of affected line unknow
#   0E0 (0 but true) or > : number of affected line
sub execQuery {
    my $self = shift;
    my( $query, $sth ) = @_;


    if( !defined($query) ) {
        $self->_log( 'requete SQL non definie !', 3 );

        $$sth = undef;
        return undef;
    }

    if( $self->_dbConnect() ) {
        $self->_log( 'erreur lors de la connexion a la BD', 0 );

        $$sth = undef;
        return undef;
    }

    $self->_log( 'requete a executer : \''.$query.'\'', 3 );

    my $dbHandler = $self->{'dbHandler'};
    $$sth = $dbHandler->prepare( $query );
    my $rv = $$sth->execute();

    if( !defined($rv) ) {
        $self->_log( 'requete executée : \''.$query.'\'', 0 );
        $self->_log( 'erreur lors de l\'execution de la requete', 3 );
        if( defined($$sth) ) {
            $self->_log( $$sth->err().' - '.$$sth->errstr(), 0 );
        }
        return undef;
    }

    if( defined($$sth->{NUM_OF_FIELDS}) ) {
        # SELECT SQL request
        $self->_log( 'requete de type \'SELECT\', nombre de tuples affectes par la requete non defini', 4 );
        return 0;
    }

    if( $rv == -1 ) {
        $self->_log( 'nombre de tuples affectes par la requete non defini', 4 );
        return 0;
    }

    $self->_log( $rv.' tuple(s) affecte(s) par la requete', 4 );

    return $rv;
}


sub quote {
    my $self = shift;
    my( $string ) = @_;

    if( $self->_dbConnect() ) {
        $self->_log( 'erreur lors de la connexion a la BD', 3 );

        return undef;
    }

    my $dbHandler = $self->{'dbHandler'};
    my $quotedString = $dbHandler->quote($string);

    $self->_log( 'chaine apres mise en forme pour le SGBD : '.$quotedString, 4 );

    return $quotedString
}


sub _dbConnect {
    my $self = shift;
    my $dbHandler = $self->{'dbHandler'};

    if( defined($dbHandler) && $dbHandler->ping() ) {
        $self->_log( 'connexion a la BD deja etablie', 4 );
        return 0;
    }

    if( !defined($self->{'dbDriver'}) && $self->_getDriver() ) {
        $self->_log( 'problème de driver SGBD, connexion BD impossible', 3 );
        return 0;
    }

    $self->_log( 'connexion a la BD \''.$self->{'dbName'}.'\', en tant que \''.$self->{'dbUser'}.'\'', 1 );
    $dbHandler = DBI->connect( 'dbi:'.$self->{'dbDriver'}.':database='.$self->{'dbName'}.';host='.$self->{'dbHost'}, $self->{'dbUser'}, $self->{'dbPassword'} );

    if( !$dbHandler ) {
        $self->_log( 'problème de connexion a la BD \''.$self->{'dbName'}.'\', en tant que \''.$self->{'dbUser'}.'\'', 0 );
        return 1;
    }

    $self->{'dbHandler'} = $dbHandler;

    $self->_driverConnectHook();

    return 0;
}


sub _dbDisconnect {
    my $self = shift;
    my $dbHandler = $self->{'dbHandler'};

    if( defined($dbHandler) && $dbHandler->ping() ) {
        if( !$dbHandler->{'AutoCommit'} ) {
            $self->_log( 'pas d\'auto-commit, rollback des eventuelles transactions en attentes', 2 );
            $dbHandler->rollback();
        }

        $self->_log( 'deconnexion de la BD', 1 );
        $dbHandler->disconnect();
    }

    undef $self->{'dbHandler'};

    return 0;
}


sub DESTROY {
    my $self = shift;

    $self->_dbDisconnect();

    return 0;
}


sub castAsInteger {
    my $self = shift;
    my( $value ) = @_;

    if( defined($value) ) {
        SWITCH:{
            if( $self->{'dbType'} eq 'mysql' ) {
                $value = 'CAST('.$value.' AS UNSIGNED)';
                last SWITCH;
            }
    
            if( $self->{'dbType'} eq 'pgsql' ) {
                $value = 'CAST('.$value.' AS INTEGER)';
                last SWITCH;
            }
        }
    }

    return $value;
}

# Perldoc

=head1 NAME

=head1 SYNOPSIS

=head1 DESCRIPTION

=head1 CONSTRUCTOR

=head1 METHODS

=head2 PUBLICS

=head2 PRIVATE

