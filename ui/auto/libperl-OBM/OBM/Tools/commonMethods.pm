package OBM::Tools::commonMethods;


$debug = 1;


use 5.006_001;
use strict;
use vars qw( @EXPORT_OK $VERSION );
use base qw(Exporter);


$VERSION = '1.0';
@EXPORT_OK = qw(    dump
                    _log
               );


sub dump {
    my $self = shift;
    my @desc;

    push( @desc, $self );

    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}


sub _log {
    my $self = shift;
    my( $text, $level ) = @_;
    require OBM::Tools::obmLog;

    $self =~ /^OBM::(.+)=.+/;
    my $prefix = $1;

    if( !defined($prefix) ) {
        $prefix = $self;
    }

    my $logObject = OBM::Tools::obmLog->instance();
    return $logObject->writeLog( '['.$prefix.']: '.$text, $level, undef );
}
