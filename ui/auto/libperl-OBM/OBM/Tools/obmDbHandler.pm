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
            $self->{'dbDriver'} = 'mysql';
            last SWITCH;
        }

        if( $self->{'dbType'} eq 'pgsql' ) {
            $self->{'dbDriver'} = 'Pg';
            last SWITCH;
        }

        $self->_log( 'driver pour les SGBD de type \''.$self->{'dbType'}.'\' inconnu', 1 );
        return 1;
    }

    return 0;
}


sub _driverHook {
    my $self = shift;
    my $dbHandler = $self->{'dbHandler'};

    SWITCH: {
        if( $self->{'dbType'} eq 'mysql' ) {
            $dbHandler->{'mysql_enable_utf8'} = 1;
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
        $self->_log( 'erreur lors de la connexion a la BD', 3 );

        $$sth = undef;
        return undef;
    }

    $self->_log( 'requete a executer : \''.$query.'\'', 3 );

    my $dbHandler = $self->{'dbHandler'};
    $$sth = $dbHandler->prepare( $query );
    my $rv = $$sth->execute();

    if( !defined($rv) ) {
        $self->_log( 'erreur lors de l\'execution de la requete', 3 );
        if( defined($$sth) ) {
            $self->_log( $$sth->err().' - '.$$sth->errstr(), 3 );
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
        $self->_log( 'driver inconnu pour les SGBD de type \''.$self->{'dbType'}.'\', connexion impossible', 0 );
        return 0;
    }

    $self->_log( 'connexion a la BD \''.$self->{'dbName'}.'\', en tant que \''.$self->{'dbUser'}.'\'', 1 );
    $dbHandler = DBI->connect( 'dbi:'.$self->{'dbDriver'}.':database='.$self->{'dbName'}.';host='.$self->{'dbHost'}, $self->{'dbUser'}, $self->{'dbPassword'} );

    if( !$dbHandler ) {
        return 1;
    }

    $self->{'dbHandler'} = $dbHandler;

    $self->_driverHook();

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


sub destroy {
    my $self = shift;

    $self->_dbDisconnect();

    return 0;
}

# Perldoc

=head1 NAME

=head1 SYNOPSIS

=head1 DESCRIPTION

=head1 CONSTRUCTOR

=head1 METHODS

=head2 PUBLICS

=head2 PRIVATE

