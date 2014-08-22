#!/bin/sh
#
### BEGIN INIT INFO
# Provides:          obm-locator
# Required-Start:    $remote_fs $syslog $network
# Required-Stop:     $remote_fs $syslog $network
# Should-Start:      $named
# Should-Stop:       $named
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: OBM component that help to locate other OBM services
# chkconfig: - 70 50
# Description:       OBM locator is a webserver offering a http endpoint 
#                    to locate ip address of others remote services
### END INIT INFO

# Source function library.
. /etc/init.d/functions

LOCATOR_JAR="/usr/share/obm-locator/obm-locator.jar"
LOCATOR_LOG_FILE="/var/log/obm-locator/obm-locator.log"
LOCATOR_RUNNABLE="/usr/share/obm-locator/obm-locator-start.sh"
JAVA_OPTS="\"-Xmx100m -Djava.awt.headless=true\""

if [ -z "$JAVA_HOME" ]; then
    JAVA_HOME="/usr/lib/jvm/jre-1.7.0-openjdk."`arch`
fi

if [ -z "$SHUTDOWN_WAIT" ]; then
    SHUTDOWN_WAIT=10
fi

if [ -z "$LOCATOR_PID" ]; then
    LOCATOR_PID=/var/run/obm-locator/obm-locator.pid
fi

# if LOCATOR_USER is not set, use locator
if [ -z "$LOCATOR_USER" ]; then
    LOCATOR_USER="locator"
fi



prog=obm-locator
RETVAL=0

start() {

        if [ -f $LOCATOR_PID ] ; then
                read kpid < $LOCATOR_PID
                if checkpid $kpid 2>&1; then
                        echo "process already running"
                        return 0
                else
                        echo "pid file found but no process running for pid $kpid, continuing"
                fi
        fi

        echo -n $"Starting $prog: "

        if [ -r /etc/rc.d/init.d/functions ]; then
                daemon --user $LOCATOR_USER LOCATOR_JAR=$LOCATOR_JAR LOCATOR_PID=$LOCATOR_PID LOCATOR_LOG_FILE=$LOCATOR_LOG_FILE JAVA_HOME=$JAVA_HOME JAVA_OPTS=$JAVA_OPTS $LOCATOR_RUNNABLE
        else
                echo "Cannot found functions library at /etc/rc.d/init.d/functions"
        fi

        echo
        return 0
}


stop() {

        echo -n $"Stopping $prog: "
        count=0;

        if [ -f $LOCATOR_PID ]; then

            read kpid < $LOCATOR_PID
            let kwait=$SHUTDOWN_WAIT

#           Try issuing SIGTERM

            kill -15 $kpid
            until [ `ps --pid $kpid 2> /dev/null | grep -c $kpid 2> /dev/null` -eq '0' ] || [ $count -gt $kwait ]
            do
#               echo "waiting for processes to exit";
                sleep 1
                let count=$count+1;
            done


            if [ $count -gt $kwait ]; then
#               echo "killing processes which didn't stop after $SHUTDOWN_WAIT seconds"
                kill -9 $kpid
            fi
	    rm -f $LOCATOR_PID
        fi
        success
        echo
}


case "$1" in
  start)
        start
        ;;
  stop)
        stop
        ;;
  status)
        status -p $LOCATOR_PID obm-locator
        exit $?
        ;;
  restart)
        stop
        sleep 2
        start
        ;;
  *)
        echo "Usage: $0 {start|stop|status|restart}"
        exit 1
esac


exit $RETVAL

#
#
# end
