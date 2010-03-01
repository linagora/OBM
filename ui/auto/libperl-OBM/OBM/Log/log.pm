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
            $logLevel = 'TRACE';
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
            $log->trace( $logMessage );
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
