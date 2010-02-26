package ObmSatellite::Server;

$VERSION = '1.0';

$debug = 1;

use ObmSatellite::Server::configure;
use ObmSatellite::Server::module;
use ObmSatellite::Server::processRequest;
use ObmSatellite::Log::log;
@ISA = qw(ObmSatellite::Server::configure ObmSatellite::Server::module ObmSatellite::Server::processRequest ObmSatellite::Log::log);

use 5.006_001;
require Exporter;
use strict;

use FileHandle;
use POSIX ":sys_wait_h";
use POSIX;


sub new {
    my $class = shift;

    my $self = bless { }, $class;

    $self->{'forkedChildren'} = 0;
    $self->{'childrens'} = {};

    return $self;
}


sub run {
    my $self = shift;

    $self->_configure();

    $self->_daemonize();

    $self->_configurePreBind();

    $self->_bind();

    $self->_spawnChildren();

    $self->_stayAlive();

    return 0;
}


sub _daemonize {
    my $self = shift;

    my $pid = fork;
    defined ($pid) or die "Cannot start daemon: $!";
    
    #print "PID: $pid\n";
    #print "Parent daemon running.\n" if $pid;
    
    # If we're the shell-called process,
    # let the user know the daemon is now running.
    # If we're the shell-called process, exit back.
    exit if $pid;

    $self->_log( 'Parent daemon running (PID: '.$$.')', 3 );
    $self->_writePidFile();
    
    # Now we're a daemonized parent process!
    
    # Detach from the shell entirely by setting our own
    # session and making our own process group
    # as well as closing any standard open filehandles.
    if( $self->{'server'}->{'setsid'} ) {
        POSIX::setsid();
        close (STDOUT); 
        open( STDOUT, '>/dev/null' );
        close (STDIN);
        close (STDERR);
        open( STDERR, '/dev/null' );
    }
    
    # Set up signals we want to catch. Let's log
    # warnings, fatal errors, and catch hangups
    # and dying children
    
    $SIG{__WARN__} = sub {
        $self->_log( 'NOTE! '.join(' ', @_), 4 );
    };
    
    $SIG{__DIE__} = sub {
        $self->_log( 'FATAL! '.join(' ', @_), 0 );
        exit(1);
    };
    
    $SIG{HUP} = $SIG{TERM} = sub {
        my $sigset = POSIX::SigSet->new(SIGCHLD);
        sigprocmask(SIG_BLOCK, $sigset) or die 'Can\'t block SIGCHLD for stop daemon';

        # Any sort of death trigger results in death of all
        my $sig = shift;
        $SIG{$sig} = 'IGNORE';

        $self->_log( 'Send SIGTERM to : '.join( ' ', keys(%{$self->{'childrens'}}) ), 4 );
        kill 'TERM' => keys(%{$self->{'childrens'}});

        $self->_log( 'Daemon stopped by '.$sig, 2 );

        unlink($self->{'server'}->{'pid_file'});
        exit;
    };
    
    # We'll handle our child reaper in a separate sub
    # REAPER sub that's called whenever a child process dies
    $SIG{CHLD} = sub{$self->REAPER()};
}


# This sub may look a little intimidating. Basically, it uses a POSIX function,
# waitpid to collect up the PIDs and exit codes of any dying/dead children (this
# automatically removes them from the process table and stops them from being
# defunct / zombies). It then reduces the $children count by one (so that we know
# to create a new child in its place later) and removes the PID from the parent's
# list of children PIDs.
sub REAPER {
    my $self = shift;
    my $stiff;

    while (($stiff = waitpid(-1, WNOHANG)) > 0) {
        my $returnVal = $? >> 8;

        if( $returnVal ) {
            $self->_log( 'Child '.$stiff.' terminated abnormaly -- status '.$returnVal, 1 );
        }else {
            $self->_log( 'Child '.$stiff.' terminated -- status '.$returnVal, 3 );
        }
        $self->{'forkedChildren'}--;
        delete $self->{'childrens'}->{$stiff};

        $self->_spawnChildren();
    }
}


sub _spawnChildren {
    my $self = shift;
    
    for( my $i=$self->{'forkedChildren'}; $i<$self->{'server'}->{'max_spare_servers'}; $i++ ) {
        $self->_newChildren();
    }
}


sub _newChildren {
    my $self = shift;

    # Daemonize away from the parent process.
    my $pid;
    my $sigset = POSIX::SigSet->new(SIGINT);
    my $old_sigset = POSIX::SigSet->new;
    sigprocmask(SIG_BLOCK, $sigset, $old_sigset) or die "Can't block SIGINT for fork: $!";

    die "Cannot fork child: $!\n" unless defined ($pid = fork);

    sigprocmask(SIG_UNBLOCK, $old_sigset) or die "Can't unblock SIGINT after fork: $!";

    if ($pid) {
        $self->{'childrens'}->{$pid} = 1;
        $self->{'forkedChildren'}++;

        $self->_log( 'new child forked (PID: '.$pid.'), we now have '.$self->{'forkedChildren'}.' child', 3 );
        return;
    }
    
    $self->_childSig();

    $self->_log( 'child '.$$.' ready to process requests', 3 );

    $self->process_request();
    exit 0;
}


sub _childSig {
    my $self = shift;

    $SIG{HUP} = $SIG{TERM} = sub {
        # Any sort of death trigger results in death of all
        my $sig = shift;
        $SIG{$sig} = 'IGNORE';
        $self->_log( 'Daemon stopped by '.$sig, 3 );
        exit;
    };
    
    # We'll handle our child reaper in a separate sub
    # REAPER sub that's called whenever a child process dies
    $SIG{CHLD} = undef;

}


sub _stayAlive {
    my $self = shift;

    while( 1 ) {
        sleep 1000;
    }

    return 0;
}


sub _bind {
    my $self = shift;

    my $oldSigDie = $SIG{__DIE__};


    $SIG{__DIE__} = sub {
        $self->_log( 'FATAL! Unable to bind SSL port '.$self->{'server'}->{'socketConf'}->{'LocalPort'}, 0 );
        exit 10;
    };
    $SIG{__DIE__} = undef;

    $self->_log( 'Trying to bind to SSL port '.$self->{'server'}->{'socketConf'}->{'LocalPort'}.' using certificate '.$self->{'server'}->{'socketConf'}->{'SSL_cert_file'}.' (key '.$self->{'server'}->{'socketConf'}->{'SSL_key_file'}.')', 3 );

    $self->_log( 'CA certificate '.$self->{'server'}->{'socketConf'}->{'SSL_ca_file'}, 3 ) if $self->{'server'}->{'socketConf'}->{'SSL_ca_file'};

    eval {
        require HTTP::Daemon::SSL;
        $self->_log( 'use HTTP::Daemon::SSL module', 5 );

        $self->{'server'}->{'socket'} = HTTP::Daemon::SSL->new( %{$self->{'server'}->{'socketConf'}} ) || die 'Unable to create HTTP::Daemon'."\n" ;
    } or eval {
        require ObmSatellite::Server::Daemon::SSL;
        $self->_log( 'Use ObmSatellite::Server::Daemon::SSL module', 5 );

        $self->{'server'}->{'socket'} = ObmSatellite::Server::Daemon::SSL->new( %{$self->{'server'}->{'socketConf'}} ) || die 'Unable to create HTTP::Daemon'."\n" ;
    } or eval {
        $self->_log( 'Unable to load perl module HTTP::Daemon::SSL or ObmSatellite::Server::Daemon::SSL', 0 );
        exit 10;
    };


    $SIG{__DIE__} = $oldSigDie;
}


sub _writePidFile {
    my $self = shift;

    if( $self->check_pid_file() ) {
        $self->_log( 'Writting PID file \''.$self->{'server'}->{'pid_file'}.'\'', 4 );
        open( PID_FILE, '>'.$self->{'server'}->{'pid_file'} ) or ( print 'Can\'t write PID file\''.$self->{'server'}->{'pid_file'}.'\'' && exit 1 );
        print PID_FILE $$;
        close( PID_FILE );
    }else {
        exit 2;
    }
}


### check for existance of pid_file
### if the file exists, check for a running process
sub check_pid_file {
    my $self = shift;
    my $pid_file = $self->{'server'}->{'pid_file'};

    ### no pid_file = return success
    return 1 unless -e $pid_file;

    ### get the currently listed pid
    if( ! open(_PID, $pid_file) ){
        $self->_log( 'Couldn\'t open existant PID file '.$pid_file.' ['.$!.']', 3 );
        print 'Couldn\'t open existant PID file '.$pid_file.' ['.$!.']'."\n";
        exit 2;
    }
    my $_current_pid = <_PID>;
    close _PID;
    my $current_pid;
    if( $_current_pid =~ /^(\d{1,10})/ ) {
        $current_pid = $1;
    }else {
        $self->_log( 'Couldn\'t find pid existing PID file ('.$pid_file.')', 2 );
        print 'Couldn\'t find pid existing PID file ('.$pid_file.')'."\n";
        exit 2;
    }

    my $exists = undef;

    ### try a proc file system
    if( -d '/proc' ) {
        $exists = -e "/proc/$current_pid";

    }elsif( `ps h o pid p $$` =~ /^\s*$$\s*$/ ){ # can I play ps on myself ?
        $exists = `ps h o pid p $current_pid`;

    }
    
    ### running process exists, ouch
    if( $exists ) {
        if( $current_pid == $$ ){
            $self->_log( 'PID file (PID: '.$current_pid.') created by this same process. Doing nothing', 2 );
            return 1;
        }else{
            $self->_log( 'PID file (PID: '.$current_pid.') already exists for running process...  aborting', 0 );
            print 'PID file (PID: '.$current_pid.') already exists for running process...  aborting'."\n";
            exit 2;
        }
    
    ### remove the pid_file
    }else{
        $self->_log( 'PID file already exists ('.$pid_file.'). Overwriting!', 2 );
        if( !unlink $pid_file ) {
            $self->_log( 'Couldn\'t remove PID file \''.$pid_file.'\' ['.$!.']', 0 );
            print 'Couldn\'t remove PID file \''.$pid_file.'\' ['.$!.']'."\n";
            exit 1;
        }

        return 1;
    }
}
