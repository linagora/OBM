package ObmSatellite::Server;

$VERSION = '1.0';

$debug = 1;

use ObmSatellite::Server::configure;
use ObmSatellite::Server::module;
use ObmSatellite::Server::processRequest;
@ISA = qw(ObmSatellite::Server::configure ObmSatellite::Server::module ObmSatellite::Server::processRequest);

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

    $self->log( 0, 'Parent daemon running (PID: '.$$.')' );
    $self->_writePidFile();
    
    # Now we're a daemonized parent process!
    
    # Detach from the shell entirely by setting our own
    # session and making our own process group
    # as well as closing any standard open filehandles.
    if( $self->{'server'}->{'setsid'} ) {
        POSIX::setsid();
        close (STDOUT); 
        close (STDIN);
        close (STDERR);
        open( STDERR, '/dev/null' );
    }
    
    # Set up signals we want to catch. Let's log
    # warnings, fatal errors, and catch hangups
    # and dying children
    
    $SIG{__WARN__} = sub {
        $self->log( 4, 'NOTE! '.join(' ', @_));
    };
    
    $SIG{__DIE__} = sub {
        $self->log( 0, 'FATAL! '.join(' ', @_));
        exit(1);
    };
    
    $SIG{HUP} = $SIG{TERM} = sub {
        my $sigset = POSIX::SigSet->new(SIGCHLD);
        sigprocmask(SIG_BLOCK, $sigset) or die 'Can\'t block SIGCHLD for stop daemon';

        # Any sort of death trigger results in death of all
        my $sig = shift;
        $SIG{$sig} = 'IGNORE';

        $self->log( 3, 'Send SIGTERM to : '.join( ' ', keys(%{$self->{'childrens'}}) ) );
        kill 'TERM' => keys(%{$self->{'childrens'}});

        $self->log( 0, 'Daemon stopped by '.$sig );

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
            $self->log( 2, 'Child '.$stiff.' terminated abnormaly -- status '.$returnVal );
        }else {
            $self->log( 2, 'Child '.$stiff.' terminated -- status '.$returnVal );
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

        $self->log( 2, 'new child forked (PID: '.$pid.'), we now have '.$self->{'forkedChildren'}.' children' );
        return;
    }
    
    $self->_childSig();

    $self->log( 0, 'child '.$$.' ready to process requests' );

    $self->process_request();
    exit 0;
}


sub _childSig {
    my $self = shift;

    $SIG{HUP} = $SIG{TERM} = sub {
        # Any sort of death trigger results in death of all
        my $sig = shift;
        $SIG{$sig} = 'IGNORE';
        $self->log( 0, 'Daemon stopped by '.$sig );
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


sub log {
    my $self = shift;

    return $self->{'logger'}->log( @_ );
}


sub _bind {
    my $self = shift;

    eval {
        require HTTP::Daemon::SSL;
    } or die 'Unable to load perl module HTTP::Daemon::SSL';

    my $oldSigDie = $SIG{__DIE__};

    $SIG{__DIE__} = sub {
        $self->log( 0, 'FATAL! Unable to bind SSL port '.$self->{'server'}->{'socketConf'}->{'LocalPort'});
        exit;
    };

    $self->log( 3, 'Trying to bind to SSL port '.$self->{'server'}->{'socketConf'}->{'LocalPort'}.' using certificate '.$self->{'server'}->{'socketConf'}->{'SSL_cert_file'}.' (key '.$self->{'server'}->{'socketConf'}->{'SSL_key_file'}.')' );
    $self->log( 3, 'CA certificate '.$self->{'server'}->{'socketConf'}->{'SSL_ca_file'} ) if $self->{'server'}->{'socketConf'}->{'SSL_ca_file'};

    $self->{'server'}->{'socket'} = HTTP::Daemon::SSL->new( %{$self->{'server'}->{'socketConf'}} ) || die 'Unable to create HTTP::Daemon'."\n" ;

    $SIG{__DIE__} = $oldSigDie;
}


sub _writePidFile {
    my $self = shift;

    if( $self->check_pid_file() ) {
        $self->log( 3, 'Writting PID file \''.$self->{'server'}->{'pid_file'}.'\'' );
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
        $self->log( 0, 'Couldn\'t open existant PID file '.$pid_file.' ['.$!.']' );
        print 'Couldn\'t open existant PID file '.$pid_file.' ['.$!.']'."\n";
        exit 2;
    }
    my $_current_pid = <_PID>;
    close _PID;
    my $current_pid;
    if( $_current_pid =~ /^(\d{1,10})/ ) {
        $current_pid = $1;
    }else {
        $self->log( 0, 'Couldn\'t find pid existing PID file ('.$pid_file.')' );
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
            $self->log( 0, 'PID file (PID: '.$current_pid.') created by this same process. Doing nothing' );
            return 1;
        }else{
            $self->log( 0, 'PID file (PID: '.$current_pid.') already exists for running process...  aborting' );
            print 'PID file (PID: '.$current_pid.') already exists for running process...  aborting'."\n";
            exit 2;
        }
    
    ### remove the pid_file
    }else{
        $self->log( 0, 'PID file already exists ('.$pid_file.'). Overwriting!' );
        if( !unlink $pid_file ) {
            $self->log( 0, 'Couldn\'t remove PID file \''.$pid_file.'\' ['.$!.']' );
            print 'Couldn\'t remove PID file \''.$pid_file.'\' ['.$!.']'."\n";
            exit 1;
        }

        return 1;
    }
}
