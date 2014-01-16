#################################################################################
# Copyright (C) 2011-2014 Linagora
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option) any
# later version, provided you comply with the Additional Terms applicable for OBM
# software by Linagora pursuant to Section 7 of the GNU Affero General Public
# License, subsections (b), (c), and (e), pursuant to which you must notably (i)
# retain the displaying by the interactive user interfaces of the “OBM, Free
# Communication by Linagora” Logo with the “You are using the Open Source and
# free version of OBM developed and supported by Linagora. Contribute to OBM R&D
# by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
# links between OBM and obm.org, between Linagora and linagora.com, as well as
# between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
# from infringing Linagora intellectual property rights over its trademarks and
# commercial brands. Other Additional Terms apply, see
# <http://www.linagora.com/licenses/> for more details.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License and
# its applicable Additional Terms for OBM along with this program. If not, see
# <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
# version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
# applicable to the OBM software.
#################################################################################


package OBM::Tools::obmDbHandler;

$VERSION = "1.0";

use Class::Singleton;
use OBM::Log::log;
@ISA = ('Class::Singleton', 'OBM::Log::log');

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

        $self->_log( 'driver pour le SGBD de type \''.$self->{'dbType'}.'\' inconnu', 0 );
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
        $self->_log( join( ' ', @_ ), 1 );
    };

    if( !defined($query) ) {
        $self->_log( 'requete SQL non definie !', 4 );

        $$sth = undef;
        return undef;
    }

    if( $self->_dbConnect() ) {
        $self->_log( 'erreur lors de la connexion a la BD', 0 );

        $$sth = undef;
        return undef;
    }

    $self->_log( 'requete a executer : \''.$query.'\'', 4 );

    my $dbHandler = $self->{'dbHandler'};
    $$sth = $dbHandler->prepare( $query );
    my $rv = $$sth->execute();

    if( !defined($rv) ) {
        $self->_log( 'erreur lors de l\'execution de la requete \''.$query.'\'', 0 );
        if( defined($$sth) ) {
            $self->_log( $$sth->err().' - '.$$sth->errstr(), 0 );
        }
        return undef;
    }

    if( defined($$sth->{NUM_OF_FIELDS}) ) {
        # SELECT SQL request
        $self->_log( 'requete de type \'SELECT\', nombre de tuples affectes par la requete non defini', 5 );
        return 0;
    }

    if( $rv == -1 ) {
        $self->_log( 'nombre de tuples affectes par la requete non defini', 5 );
        return 0;
    }

    $self->_log( $rv.' tuple(s) affecte(s) par la requete', 5 );

    return $rv;
}


sub quote {
    my $self = shift;
    my( $string ) = @_;

    if( $self->_dbConnect() ) {
        $self->_log( 'erreur lors de la connexion a la BD', 0 );

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
        $self->_log( 'connexion a la BD deja etablie', 5 );
        return 0;
    }

    $self->_log( 'connexion a la BD \''.$self->{'dbName'}.'\', en tant que \''.$self->{'dbUser'}.'\'', 3 );
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
            $self->_log( 'pas d\'auto-commit, rollback des eventuelles transactions en attentes', 4 );
            $dbHandler->rollback();
        }

        $self->_log( 'deconnexion de la BD', 3 );
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

