package ObmSaslauthd::SocketServer::socketServer;

$VERSION = '1.0';

$debug = 1;

use POSIX ":sys_wait_h";
use IO::Socket::UNIX;
use Carp;

use ObmSaslauthd::Tools::commonMethods;
@ISA = ('ObmSaslauthd::Tools::commonMethods');

use 5.006_001;
require Exporter;
use strict;


sub new {
    my $class = shift;
    my( $parent, $userDesc ) = @_;

    BEGIN { $ENV{PATH} = '/bin' }

    my $self = bless { }, $class;

    $self->{'NAME'} = '/tmp/catsock';
    unlink($self->{'NAME'});

    $self->{'maxPrefork'} = 5;
    $self->{'maxRequest'} = 100;
    
    $self->{'server'} = IO::Socket::UNIX->new(
        Local => $self->{'NAME'},
        Type => SOCK_STREAM,
        Listen => 5
    ) or die $@;

    close( STDIN );
    close( STDOUT );

    $SIG{CHLD} = sub{ $self->REAPER };

    return $self;
}


sub REAPER {
    my $self = shift;
    my $child;

    while (($self->{'waitedpid'} = waitpid(-1,WNOHANG)) > 0) {
        $self->_log( 'reaped '.$self->{'waitedpid'}.($? ? ' with exit '.$? : ''), 4 );
        $self->spawn( sub { $self->loop() } );
    }

    $SIG{CHLD} = sub{ $self->REAPER };
}


sub spawn {
    my $self = shift;
    my $coderef = shift;
    
    unless (@_ == 0 && $coderef && ref($coderef) eq 'CODE') {
        confess "usage: spawn CODEREF";
    }

    my $pid;
    if (!defined($pid = fork)) {
        $self->_log( 'cannot fork: '.$!, 0 );
        return;
    } elsif ($pid) {
        $self->_log( 'starting new children at PID '.$pid, 2 );
        return; # I'm the parent
    }
    # else I'm the child -- go spawn
    
    exit &$coderef();
}


sub run {
    my $self = ref($_[0]) ? shift() : shift->new;

    $self->_log( 'spawn '.$self->{'maxPrefork'}.' childrens', 2 );
    for( my $i=0; $i<$self->{'maxPrefork'}; $i++ ) {
        $self->spawn( sub { $self->loop() } );
    }

    while( 1 ) {
        sleep 100;
    }

    print 'ended'."\n";
}


sub loop {
    my $self = shift;
    my $requestNumber = 0;

    while( ($requestNumber < $self->{'maxRequest'} ) && ( my $client = $self->{'server'}->accept() ) ) {
        $requestNumber++;

        my $fileno= fileno $client;
        close(STDIN);
        close(STDOUT);
        if( defined $fileno ) {
            open( STDIN,  "<&$fileno" ) or die "Couldn't open STDIN to the client socket: $!";
            open( STDOUT,  ">&$fileno" ) or die "Couldn't open STDOUT to the client socket: $!";
        }else {
            *STDIN= \*{ $client };
            *STDOUT= \*{ $client };
        }
        STDIN->autoflush(1);
        STDOUT->autoflush(1);
        select(STDOUT);

        my $in = <STDIN>;
        $self->_log( $in, 0 );
    
        print "Hello there, it's now ". scalar localtime(). "\n";
        print "PID ".$$."\n";
        $client->close();
        $self->_log( 'lalo', 0 );

        close(STDIN);
        close(STDOUT);
    }

    return 0;
}


sub _prepare_stdInOut {
    my $self = shift;
    my( $client ) = @_;
}
