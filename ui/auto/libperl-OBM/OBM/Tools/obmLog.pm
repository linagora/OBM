package OBM::Tools::obmLog;

$VERSION = '1.0';

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

        $level = $self->_convertLevel( $level );

        $text =~ s/\s+/ /g;
        $text =~ s/\t+/ /g;
        Sys::Syslog::syslog( $priority, $level.$text );
    }


    return 0;
}


sub DESTROY {
    my $self = shift;

    if( $self->{'logOpen'} ) {
        Sys::Syslog::closelog();
        $self->{'logOpen'} = 0;
    }

    return 0;
}


sub _convertLevel {
    my $self = shift;
    my( $level ) = @_;

    SWITCH: {
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


# Perldoc

=head1 NAME

OBM::Tools::obmLog - Logger object

=head1 SYNOPSIS

    use OBM::Tools::obmLog

    # Initialise a new logger object or get the already initialized object
    my $logger = OBM::Tools::obmLog->instance();

    # Write a new message with a level priority set to 2
    $logger->writeLog( 'My message', 2 );

=head1 DESCRIPTION

This object is used to write message into log files througth a unique way.

It use the 'OBM::Parameters::common::facility_log' as syslog facility and
'OBM::Parameters::common::logLevel' as default log level.

By default, message use the 'notice' syslog priority

The default log level can be set via ithe 'logLevel' directive of
'obm_conf.ini'.

Only message with a lesser or equal level will be logged via syslog.

Available level are :

=over 4

=item 0 : only critical informations (script name, critical errors...) ;

=item 1 : basics informations ;

=item 2 : advanced informations - default level ;

=item 3 : low debug informations ;

=item 4 : high debug informations.

=back

=head1 CONSTRUCTOR

instance ()

    Initialise a new logger object or get the already initialized object.

    Use current script name as 'logId', which is the first word on the syslog
    messages.

instance ( myWord )

    Initialise a new logger object or get the already initialized object.

    Use 'myWord' as 'logId', which is the first word on the syslog messages.

=head1 METHODS

=head2 PUBLICS

writeLog ( $text, $level, $priority )

    write 'text' as 'level' and 'priority'.

    If no 'priority' set, 'notice' is used.

    if no 'level' set, '0' is used.

DESTROY ()

    close syslog connection

=head2 PRIVATE

_openLog ()

    try to establish a syslog connection

_convertLevel ()

    convert integer level into word
