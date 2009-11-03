package ObmSatellite::Log::log;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use base qw( Class::Singleton );


sub _new_instance {
    my $class = shift;
    my( $logFile, $logLevel ) = @_;

    my $self = bless { }, $class;

    $self->{'logFile'} = $logFile;

    $self->{'logLevel'} = $logLevel;
    $self->{'logLevel'} = 2 if !defined($self->{'logLevel'});
    $self->{'logLevel'} = 2 if $self->{'logLevel'} !~ /^[01234]$/;

    $self->_openLog();

    return $self;
}


sub _openLog {
    my $self = shift;

    if( !$self->{'logFile'} ) {
        $self->{'disabled'};
    }else {
        open( _LOG_HDL, '>>'.$self->{'logFile'} ) or die 'Unable to open log file';
        _LOG_HDL->autoflush(1);
    }
}


sub DESTROY {
    my $self = shift;

    close( _LOG_HDL );
}


sub log {
    my $self = shift;
    my( $logLevel, $logMessage ) = @_;

    if( $self->{'disabled'} ) {
        return;
    }

    if( $logLevel <= $self->{'logLevel'} ) {
        my $level = $self->_convertLevel( $logLevel );

        print _LOG_HDL $self->log_time().' ['.$$.']: '.$level.$logMessage;

        if( $logMessage !~ /\n$/ ) {
            print _LOG_HDL "\n";
        }
    }
}


sub _convertLevel {
    my $self = shift;
    my( $level ) = @_;

    SWITCH: {
        if( $level == -1 ) {
            return '';
        }

        if( $level == 0 ) {
            return 'CRITICAL: ';
        }

        if( $level == 1 ) {
            return 'BASIC: ';
        }

        if( $level == 2 ) {
            return 'ADVANCED: ';
        }
    }

    return 'DEBUG: '
}


### default time format                                                                                                                                      
sub log_time {                                                                                                                                               
    my $self = shift;
    my ($sec,$min,$hour,$day,$mon,$year) = localtime;

    return sprintf("%04d/%02d/%02d-%02d:%02d:%02d", $year+1900, $mon+1, $day, $hour, $min, $sec);
}
