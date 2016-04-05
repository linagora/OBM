#!/bin/sh
#
### BEGIN INIT INFO
# Provides:          obm-provisioning
# Required-Start:    $remote_fs $syslog $network
# Required-Stop:     $remote_fs $syslog $network
# Should-Start:      $named
# Should-Stop:       $named
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: OBM component to provision OBM resources
# chkconfig: - 70 50
# Description:       OBM Provisioning is a webserver offering a http API 
#                    to manage OBM resources such as domains, users and groups
### END INIT INFO

# Source function library.
. /etc/init.d/functions

PROVISIONING_JAR="/usr/share/obm-provisioning/provisioning-server.jar"
PROVISIONING_LOG_FILE="/var/log/obm-provisioning/out.log"
PROVISIONING_RUNNABLE="/usr/share/obm-provisioning/provisioning-start.sh"
JAVA_OPTS="\"-Xmx200m -Djava.awt.headless=true\""

if [ -z "$JAVA_HOME" ]; then
    JAVA_HOME="/usr/lib/jvm/jre-1.7.0"
fi

if [ -z "$SHUTDOWN_WAIT" ]; then
    SHUTDOWN_WAIT=10
fi

if [ -z "$PROVISIONING_PID" ]; then
    PROVISIONING_PID=/var/run/obm-provisioning/obm-provisioning.pid
fi

# if PROVISIONING_USER is not set, use provisioning
if [ -z "$PROVISIONING_USER" ]; then
    PROVISIONING_USER="provisioning"
fi



prog=obm-provisioning
RETVAL=0

start() {

        if [ -f $PROVISIONING_PID ] ; then
                read kpid < $PROVISIONING_PID
                if checkpid $kpid 2>&1; then
                        echo "process already running"
                        return 0
                else
                        echo "pid file found but no process running for pid $kpid, continuing"
                fi
        fi

        echo -n $"Starting $prog: "

        if [ -r /etc/rc.d/init.d/functions ]; then
                daemon --user $PROVISIONING_USER PROVISIONING_JAR=$PROVISIONING_JAR PROVISIONING_PID=$PROVISIONING_PID PROVISIONING_LOG_FILE=$PROVISIONING_LOG_FILE JAVA_HOME=$JAVA_HOME JAVA_OPTS=$JAVA_OPTS $PROVISIONING_RUNNABLE
        else
                echo "Cannot found functions library at /etc/rc.d/init.d/functions"
        fi

        echo
        return 0
}


stop() {

        echo -n $"Stopping $prog: "
        count=0;

        if [ -f $PROVISIONING_PID ]; then

            read kpid < $PROVISIONING_PID
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
	    rm -f $PROVISIONING_PID
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
        status -p $PROVISIONING_PID obm-provisioning
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
