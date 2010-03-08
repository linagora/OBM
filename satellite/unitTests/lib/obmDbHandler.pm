package lib::obmDbHandler;

$VERSION = "1.0";

use Class::Singleton;
@ISA = ('Class::Singleton');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

require DBI;
require OBM::Parameters::common;


sub _new_instance {
    my $class = shift;

    my $self = bless { }, $class;

    $self->{'dbHandler'} = undef;
    $self->{'dbHost'} = $OBM::Parameters::common::dbHost;
    $self->{'dbName'} = $OBM::Parameters::common::dbName;
    $self->{'dbType'} = $OBM::Parameters::common::dbType;
    $self->{'dbDriver'} = undef;
    if( $self->_getDriver() ) {
        return undef;
    }
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
                return 1;
            }

            $self->{'dbDriver'} = 'mysql';
            last SWITCH;
        }

        if( $self->{'dbType'} eq 'pgsql' ) {
            my $moduleInstalled = eval { require DBD::Pg; };
            if( !defined($moduleInstalled) ) {
                return 1;
            }

            $self->{'dbDriver'} = 'Pg';
            last SWITCH;
        }

        return 1;
    }

    return 0;
}


sub _driverConnectHook {
    my $self = shift;
    my $dbHandler = $self->{'dbHandler'};

    SWITCH: {
        if( $self->{'dbType'} eq 'mysql' ) {
            $dbHandler->{'mysql_enable_utf8'} = 1;
            $dbHandler->do('SET NAMES utf8');
            last SWITCH;
        }

        if( $self->{'dbType'} eq 'pgsql' ) {
            $dbHandler->{'pg_enable_utf8'} = 1;
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

    local $SIG{__DIE__} = sub {
    };

    if( !defined($query) ) {
        $$sth = undef;
        return undef;
    }

    if( $self->_dbConnect() ) {
        $$sth = undef;
        return undef;
    }

    my $dbHandler = $self->{'dbHandler'};
    $$sth = $dbHandler->prepare( $query );
    my $rv = $$sth->execute();

    if( !defined($rv) ) {
        return undef;
    }

    if( defined($$sth->{NUM_OF_FIELDS}) ) {
        # SELECT SQL request
        return 0;
    }

    if( $rv == -1 ) {
        return 0;
    }

    return $rv;
}


sub quote {
    my $self = shift;
    my( $string ) = @_;

    if( $self->_dbConnect() ) {
        return undef;
    }

    my $dbHandler = $self->{'dbHandler'};
    my $quotedString = $dbHandler->quote($string);

    return $quotedString
}


sub _dbConnect {
    my $self = shift;
    my $dbHandler = $self->{'dbHandler'};

    if( defined($dbHandler) && $dbHandler->ping() ) {
        return 0;
    }

    $dbHandler = DBI->connect( 'dbi:'.$self->{'dbDriver'}.':database='.$self->{'dbName'}.';host='.$self->{'dbHost'}, $self->{'dbUser'}, $self->{'dbPassword'} );

    if( !$dbHandler ) {
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
            $dbHandler->rollback();
        }

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

