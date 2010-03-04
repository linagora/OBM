package ObmSatellite::Log::log;

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
    
    my $logger = 'LOGFILE';
    if( !defined($self->{'logFile'}) ) {
        $logger = 'SCREEN';
    }

    my $logLevel = 'INFO';
    SWITCH: {
        if( !defined( $self->{'logLevel'} ) ) {
            last SWITCH;
        }

        if( $self->{'logLevel'} == TRACE ) {
            if( $Log::Log4perl::VERSION < 1.13 ) {
                $logLevel = 'DEBUG';
            }else {
                $logLevel = 'TRACE';
            }
            last SWITCH;
        }

        if( $self->{'logLevel'} == DEBUG ) {
            $logLevel = 'DEBUG';
            last SWITCH;
        }

        if( $self->{'logLevel'} == WARN ) {
            $logLevel = 'WARN';
            last SWITCH;
        }

        if( $self->{'logLevel'} == ERROR ) {
            $logLevel = 'ERROR';
            last SWITCH;
        }

        if( $self->{'logLevel'} == FATAL ) {
            $logLevel = 'FATAL';
            last SWITCH;
        }
    }

    my %logConf = (
        'log4perl.rootLogger' => $logLevel.', '.$logger,
        'log4perl.appender.LOGFILE' => 'Log::Log4perl::Appender::File',
        'log4perl.appender.LOGFILE.filename' => $self->{'logFile'},
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
