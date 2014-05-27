#!/bin/sh -e
#
### BEGIN INIT INFO
# Provides:          obm-imap-archive
# Required-Start:    $remote_fs $syslog $network
# Required-Stop:     $remote_fs $syslog $network
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: OBM component that perform email archiving
# Description:       obm-imap-archive is a http server exposing webservices 
#                    to perform email archiving
### END INIT INFO

PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin
NAME=obm-imap-archive
APP_HOME=/usr/share/$NAME
APP_USER=imap-archive
LOGDIR="/var/log/$NAME"
LOGFILE="$LOGDIR/out.log"
START_JAR="$APP_HOME/imap-archive.jar"
PIDFILE="/var/run/$NAME.pid"
HOSTNAME=$(uname -n)

if [ `id -u` -ne 0 ]; then
        echo "You need root privileges to run this script"
        exit 1
fi

# Make sure obm-imap-archive is started with system locale
if [ -r /etc/default/locale ]; then
        . /etc/default/locale
        export LANG
fi

. /lib/lsb/init-functions

if [ -r /etc/default/rcS ]; then
        . /etc/default/rcS
fi


if [ -z "$APP_PORT" ]; then
	APP_PORT=8085
fi
if [ -z "$JAVA_HOME" ]; then
	JAVA_HOME="/usr/lib/jvm/java-7-openjdk-"`dpkg --print-architecture`
fi

if [ ! -d "$TMP_DIR" ]; then
	TMP_DIR="/tmp"
fi
if [ -z "$SHUTDOWN_TIMEOUT" ]; then
	SHUTDOWN_TIMEOUT=10
fi

JAVA_OPTIONS="$JAVA_OPTIONS -Dfile.encoding=UTF-8 -XX:+UseG1GC -Djava.io.tmpdir=$TMP_DIR -DimapArchivePort=$APP_PORT"

JAVA="$JAVA_HOME/bin/java"
APP_COMMAND="$JAVA -- $JAVA_OPTIONS -jar $START_JAR"

##################################################
# FUNCTIONS
##################################################
is_stopped () {
        start-stop-daemon --quiet --test --start --pidfile "$PIDFILE" \
                          --user "$APP_USER" --startas $JAVA > /dev/null
}

do_start () {
        start-stop-daemon --start --pidfile "$PIDFILE" --make-pidfile \
                          --chuid "$APP_USER" --startas $APP_COMMAND > $LOGFILE 2>&1 &
}

do_stop () {
        do_stop_sig_term
        while ! is_stopped ; do
                sleep 1
                log_progress_msg "."
                SHUTDOWN_TIMEOUT=`expr $SHUTDOWN_TIMEOUT - 1` || true
                if [ $SHUTDOWN_TIMEOUT -ge 0 ]; then
                        do_stop_sig_term
                else
                        log_progress_msg " (killing) "
                        do_stop_sig_kill
                fi
        done

        rm -f "$PIDFILE"
}
do_stop_sig_term () {
        start-stop-daemon --quiet --stop --signal 15 --pidfile "$PIDFILE" --oknodo \
                          --user "$APP_USER" --startas $JAVA >> $LOGFILE 2>&1
}
do_stop_sig_kill () {
        start-stop-daemon --quiet --stop --signal 9 --pidfile "$PIDFILE" --oknodo \
                          --user "$APP_USER" --startas $JAVA >> $LOGFILE 2>&1
}

##################################################
# Do the action
##################################################
case "$1" in
  start)
        log_daemon_msg "Starting $NAME"
        if is_stopped ; then

                if [ -f $PIDFILE ] ; then
                        log_warning_msg "$PIDFILE exists, but $NAME was not running. Ignoring $PIDFILE"
                fi

                if do_start ; then
	                log_daemon_msg "$NAME started, reachable on http://$HOSTNAME:$APP_PORT/." "$NAME"
                        log_end_msg 0
                else
                        log_end_msg 1
                fi

        else
                log_warning_msg "(already running)."
                log_end_msg 0
                exit 1
        fi
        ;;

  stop)
        log_daemon_msg "Stopping $NAME (was reachable on http://$HOSTNAME:$APP_PORT/)." "$NAME"

        if is_stopped ; then
                if [ -x "$PIDFILE" ]; then
                        log_warning_msg "(not running but $PIDFILE exists)."
                else
                        log_warning_msg "(not running)."
                fi
        else
                do_stop
                log_daemon_msg "$NAME stopped."
                log_end_msg 0
        fi
        ;;

  status)
        if is_stopped ; then
                if [ -f "$PIDFILE" ]; then
                    log_success_msg "$NAME is not running, but pid file exists."
                        exit 1
                else
                    log_success_msg "$NAME is not running."
                        exit 3
                fi
        else
                log_success_msg "$NAME is running with pid `cat $PIDFILE`, and is reachable on http://$HOSTNAME:$APP_PORT/"
        fi
        ;;

  restart)
        if ! is_stopped ; then
                $0 stop $*
                sleep 1
        fi
        $0 start $*
        ;;

  check)
        log_success_msg "Checking arguments for $NAME: "
        log_success_msg ""
        log_success_msg "PIDFILE        =  $PIDFILE"
        log_success_msg "JAVA_OPTIONS   =  $JAVA_OPTIONS"
        log_success_msg "JAVA           =  $JAVA"
        log_success_msg "APP_USER       =  $APP_USER"
        log_success_msg "ARGUMENTS      =  $ARGUMENTS"

        if [ -f $PIDFILE ]
        then
                log_success_msg "$NAME is running with pid `cat $PIDFILE`, and is reachable on http://$HOSTNAME:$APP_PORT/"
                exit 0
        fi
        exit 1
        ;;

  *)
        log_success_msg "Usage: $0 {start|stop|restart|status|check}"
        exit 1
        ;;
esac

exit 0