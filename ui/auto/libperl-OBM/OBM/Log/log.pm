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


package OBM::Log::log;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;
use Log::Log4perl;

use constant TRACE => 5;
use constant DEBUG => 4;
use constant INFO => 3;
use constant WARN => 2;
use constant ERROR => 1;
use constant FATAL => 0;


sub _configureLog {
    my $self = shift;

    require OBM::Parameters::common;
    
    my $logger = 'LOGFILE';
    if( !defined($OBM::Parameters::common::logFile) ) {
        $logger = 'SCREEN';
    }

    my $logLevel = 'INFO';
    SWITCH: {
        if( !defined( $OBM::Parameters::common::logLevel ) ) {
            last SWITCH;
        }

        if( $OBM::Parameters::common::logLevel == TRACE ) {
            if( $Log::Log4perl::VERSION < 1.13 ) {
                $logLevel = 'DEBUG';
            }else {
                $logLevel = 'TRACE';
            }
            last SWITCH;
        }

        if( $OBM::Parameters::common::logLevel == DEBUG ) {
            $logLevel = 'DEBUG';
            last SWITCH;
        }

        if( $OBM::Parameters::common::logLevel == WARN ) {
            $logLevel = 'WARN';
            last SWITCH;
        }

        if( $OBM::Parameters::common::logLevel == ERROR ) {
            $logLevel = 'ERROR';
            last SWITCH;
        }

        if( $OBM::Parameters::common::logLevel == FATAL ) {
            $logLevel = 'FATAL';
            last SWITCH;
        }
    }

    my %logConf = (
        'log4perl.rootLogger' => $logLevel.', '.$logger,
        'log4perl.appender.LOGFILE' => 'Log::Log4perl::Appender::File',
        'log4perl.appender.LOGFILE.filename' => $OBM::Parameters::common::logFile,
        'log4perl.appender.LOGFILE.mode' => 'append',
        'log4perl.appender.LOGFILE.layout' => 'PatternLayout',
        'log4perl.appender.LOGFILE.layout.ConversionPattern' => '%d:%r [%P]: %C:%L %p - %m%n',
        'log4perl.appender.LOGFILE.umask' => 0002,
        'log4perl.appender.SCREEN' => 'Log::Log4perl::Appender::Screen',
        'log4perl.appender.SCREEN.stderr' => 0,
        'log4perl.appender.SCREEN.layout' => 'PatternLayout',
        'log4perl.appender.SCREEN.layout.ConversionPattern' => '%d:%r [%P]: %C:%L %p - %m%n',
    );

    Log::Log4perl::init_once( \%logConf );
    $Log::Log4perl::caller_depth = 1;

    return 0;
}


sub _log {
    my $self = shift;
    my( $logMessage, $logLevel ) = @_;

    if( !Log::Log4perl->initialized() ) {
        return 1;
    }

    my $log = Log::Log4perl->get_logger(__PACKAGE__);

    SWITCH: {
        if( $logLevel == TRACE || $logLevel eq 'TRACE' ) {
            if( $Log::Log4perl::VERSION < 1.13 ) {
                $log->debug( $logMessage );
            }else {
                $log->trace( $logMessage );
            }

            last SWITCH;
        }

        if( $logLevel == DEBUG || $logLevel eq 'DEBUG' ) {
            $log->debug( $logMessage );
            last SWITCH;
        }

        if( $logLevel == INFO || $logLevel eq 'INFO' ) {
            $log->info( $logMessage );
            last SWITCH;
        }

        if( $logLevel == WARN || $logLevel eq 'WARN' ) {
            $log->warn( $logMessage );
            last SWITCH;
        }

        if( $logLevel == ERROR || $logLevel eq 'ERROR' ) {
            $log->error( $logMessage );
            last SWITCH;
        }

        if( $logLevel == FATAL || $logLevel eq 'FATAL' ) {
            $log->fatal( $logMessage );
            last SWITCH;
        }
    }

    return 0;
}
