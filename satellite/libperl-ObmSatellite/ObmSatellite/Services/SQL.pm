package ObmSatellite::Services::SQL;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use base qw( Class::Singleton );
require DBI;
require Config::IniFiles;
require ObmSatellite::Log::log;

use constant OBM_CONF => '/etc/obm/obm_conf.ini';


sub _new_instance {
    my $class = shift;

    my $self = bless { }, $class;

    my $cfgFile = Config::IniFiles->new( -file => OBM_CONF );

    # Definition des bases de donnees
    my $userDb = $cfgFile->val( 'global', 'user' );
    my $userPasswd = $cfgFile->val( 'global', 'password' );
    if( defined( $userPasswd ) && $userPasswd =~ /^"(.*)"$/ ) {
        $userPasswd = $1;
    }else {
        $userPasswd = undef;
    }
    
    # La base de travail
    #
    # La base des mises à jours
    my $dbName = $cfgFile->val( 'global', 'db' );
    my $dbHost = $cfgFile->val( 'global', 'host' );
    my $dbType = lc( $cfgFile->val( 'global', 'dbtype' ));
    
    $self->{'dbHandler'} = undef;
    $self->{'dbHost'} = $dbHost;
    $self->{'dbName'} = $dbName;
    $self->{'dbType'} = $dbType;
    $self->{'dbDriver'} = undef;
    $self->{'dbUser'} = $userDb;
    $self->{'dbPassword'} = $userPasswd;

    return $self;
}


sub log {
    my $self = shift;

    my $log = ObmSatellite::Log::log->instance();
    return $log->log( @_ );
}


sub _getDriver {
    my $self = shift;

    SWITCH:{
        if( $self->{'dbType'} eq 'mysql' ) {
            my $moduleInstalled = eval { require DBD::mysql; };
            if( !defined($moduleInstalled) ) {
                $self->log( 0, 'module DBD::mysql not installed, DB connection fail' );
                return 1;
            }

            $self->{'dbDriver'} = 'mysql';
            last SWITCH;
        }

        if( $self->{'dbType'} eq 'pgsql' ) {
            my $moduleInstalled = eval { require DBD::Pg; };
            if( !defined($moduleInstalled) ) {
                $self->log( 0, 'module DBD::Pg not installed, DB connection fail' );
                return 1;
            }

            $self->{'dbDriver'} = 'Pg';
            last SWITCH;
        }

        $self->log( 1, 'unknow DBD driver for database '.$self->{'dbType'} );
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
        $self->log( 3, 'SQL request undefined !' );

        $$sth = undef;
        return undef;
    }

    if( $self->_dbConnect() ) {
        $self->log( 0, 'DB connection fail' );

        $$sth = undef;
        return undef;
    }

    $self->log( 3, 'request : \''.$query.'\'' );

    my $dbHandler = $self->{'dbHandler'};
    $$sth = $dbHandler->prepare( $query );
    my $rv = $$sth->execute();

    if( !defined($rv) ) {
        $self->log( 0, 'failed on SQL request \''.$query.'\'' );
        if( defined($$sth) ) {
            $self->log( 0, $$sth->err().' - '.$$sth->errstr() );
        }
        return undef;
    }

    if( defined($$sth->{NUM_OF_FIELDS}) ) {
        # SELECT SQL request
        $self->log( 4, '\'SELECT\' request, unknow amount SQL records are selected' );
        return 0;
    }

    if( $rv == -1 ) {
        $self->log( 4, 'amount SQL records process undefined' );
        return 0;
    }

    $self->log( 4, $rv.' SQL records were processed' );

    return $rv;
}


sub quote {
    my $self = shift;
    my( $string ) = @_;

    if( $self->_dbConnect() ) {
        $self->log( 3, 'DB connection fail' );

        return undef;
    }

    my $dbHandler = $self->{'dbHandler'};
    my $quotedString = $dbHandler->quote($string);

    $self->log( 4, 'string process by DBD : '.$quotedString );

    return $quotedString
}


sub _dbConnect {
    my $self = shift;
    my $dbHandler = $self->{'dbHandler'};

    if( defined($dbHandler) && $dbHandler->ping() ) {
        $self->log( 4, 'DB already connected' );
        return 0;
    }

    if( !defined($self->{'dbDriver'}) && $self->_getDriver() ) {
        $self->log( 3, 'DBD driver fail, DB connection fail' );
        return 0;
    }

    $self->log( 1, 'connexion a la DB \''.$self->{'dbName'}.'\', en tant que \''.$self->{'dbUser'}.'\'' );
    $self->log( 1, 'connect DB \''.$self->{'dbName'}.'\', as \''.$self->{'dbUser'}.'\'' );
    $dbHandler = DBI->connect( 'dbi:'.$self->{'dbDriver'}.':database='.$self->{'dbName'}.';host='.$self->{'dbHost'}, $self->{'dbUser'}, $self->{'dbPassword'} );

    if( !$dbHandler ) {
        $self->log( 0, 'Failed to connect DB \''.$self->{'dbName'}.'\', as \''.$self->{'dbUser'}.'\'' );
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
            $self->log( 2, 'auto-commit disable, rollback any pending request' );
            $dbHandler->rollback();
        }

        $self->log( 1, 'disconnect DB' );
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

