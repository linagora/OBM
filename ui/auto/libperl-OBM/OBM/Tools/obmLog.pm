package OBM::Tools::obmLog;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use base qw( Class::Singleton );
require Sys::Syslog;
require OBM::Parameters::common;


sub _new_instance {
    my $class = shift;
    my( $logId ) = @_;

    my $self = bless { }, $class;

    $self->{'logOpen'} = 0;
    $self->{'logLevel'} = $OBM::Parameters::common::logLevel;
    $self->{'facility'} = $OBM::Parameters::common::facility_log;
    $self->{'logDftPriority'} = 'notice';

    if( !$logId ) {
        use File::Basename;
        $self->{'logId'} = basename $0;
    }else {
        $self->{'logId'} = $logId;
    }

    return $self;
}


sub _openLog {
    my $self = shift;
    
    Sys::Syslog::openlog( $self->{'logId'}, "pid", $self->{'facility'} );
    $self->{'logOpen'} = 1;

    return 0;
}


sub writeLog {
    my $self = shift;
    my( $text, $level, $priority ) = @_;


    if( !defined($text) ) {
        return 0;
    }

    if( !defined($level) || ($level !~ /^[0-9]+$/) ) {
        $level = 0;
    }

    if( !defined($priority) ) {
        $priority = $self->{'logDftPriority'};
    }

    if( $level <= $self->{'logLevel'} ) {
        if( !$self->{'logOpen'} ) {
            $self->_openLog();
        }

        Sys::Syslog::syslog( $priority, $text );
    }


    return 0;
}


sub destroy {
    my $self = shift;

    if( $self->{'logOpen'} ) {
        Sys::Syslog::closelog();
        $self->{'logOpen'} = 0;
    }

    return 0;
}
