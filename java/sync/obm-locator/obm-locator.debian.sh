#!/bin/sh -e
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
# Description:       OBM locator is a webserver offering a http endpoint 
#                    to locate ip address of others remote services
### END INIT INFO


PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin
NAME=obm-locator
DESC="OBM locator servlet engine"
LOCATOR_HOME=/usr/share/$NAME
LOCATOR_PORT=8084
LOGDIR="/var/log/obm-locator"
LOGFILE="$LOGDIR/obm-locator.log"
START_JAR="$LOCATOR_HOME/obm-locator.jar"

if [ `id -u` -ne 0 ]; then
        echo "You need root privileges to run this script"
        exit 1
fi

# Make sure obm-locator is started with system locale
if [ -r /etc/default/locale ]; then
        . /etc/default/locale
        export LANG
fi

. /lib/lsb/init-functions

if [ -r /etc/default/rcS ]; then
        . /etc/default/rcS
fi

# Run obm-locator as this user ID (default: locator)
# Set this to an empty string to prevent obm-locator from starting automatically
LOCATOR_USER=locator

# Extra options to pass to the JVM
# Set java.awt.headless=true if JAVA_OPTIONS is not set so the
# Xalan XSL transformer can work without X11 display on JDK 1.4+
# It also sets the maximum heap size to 256M to deal with most cases.
JAVA_OPTIONS="-Xmx100m -Djava.awt.headless=true"
export JAVA_OPTIONS

# The first existing directory is used for JAVA_HOME
# Should contain a list of space separated directories.
JDK_DIRS="
          /usr/lib/jvm/default-java \
          /usr/lib/jvm/java-6-sun \
          /usr/lib/jvm/java-6-openjdk \
         "

# Timeout in seconds for the shutdown of all webapps
LOCATOR_SHUTDOWN=10

# Look for the right JVM to use
for jdir in $JDK_DIRS; do
        if [ -d "$jdir" -a -z "${JAVA_HOME}" ]; then
                JAVA_HOME="$jdir"
        fi
done
export JAVA_HOME
export JAVA="$JAVA_HOME/bin/java"
LOCATOR_COMMAND="$JAVA -- -jar $START_JAR"

# Define other required variables
PIDFILE="/var/run/$NAME.pid"
HOSTNAME=$(uname -n)

##################################################
# Check for JAVA_HOME
##################################################
if [ -z "$JAVA_HOME" ]; then
        log_failure_msg "Could not start $DESC because no Java Development Kit"
        log_failure_msg "(JDK) was found. Please download and install JDK 1.6 or higher"
        exit 0
fi

##################################################
# LOCATOR FUNCTIONS
##################################################
locatorIsStopped () {
        start-stop-daemon --quiet --test --start --pidfile "$PIDFILE" \
                          --user "$LOCATOR_USER" --startas $JAVA > /dev/null
}

startLocator () {
        touch $LOGFILE
        chown -R $LOCATOR_USER:adm "$LOGDIR"

        start-stop-daemon --start --pidfile "$PIDFILE" --make-pidfile \
                          --chuid "$LOCATOR_USER" --startas $LOCATOR_COMMAND >> $LOGFILE 2>&1 &
}

stopLocator () {
        start-stop-daemon --quiet --stop --signal 9 --pidfile "$PIDFILE" \
                          --user "$LOCATOR_USER" --startas $JAVA >> $LOGFILE 2>&1
        rm -f "$PIDFILE"

}

##################################################
# Do the action
##################################################
case "$1" in
  start)
        log_daemon_msg "Starting $DESC." "$NAME"
        if locatorIsStopped ; then

                if [ -f $PIDFILE ] ; then
                        log_warning_msg "$PIDFILE exists, but obm-locator was not running. Ignoring $PIDFILE"
                fi

                if startLocator ; then
	                log_daemon_msg "$DESC started, reachable on http://$HOSTNAME:$LOCATOR_PORT/." "$NAME"
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
        log_daemon_msg "Stopping $DESC (was reachable on http://$HOSTNAME:$LOCATOR_PORT/)." "$NAME"

        if locatorIsStopped ; then
                if [ -x "$PIDFILE" ]; then
                        log_warning_msg "(not running but $PIDFILE exists)."
                else
                        log_warning_msg "(not running)."
                fi
        else
                stopLocator
                log_daemon_msg "$DESC stopped." "$NAME"
                log_end_msg 0
        fi
        ;;

  status)
        if locatorIsStopped ; then
                if [ -f "$PIDFILE" ]; then
                    log_success_msg "$DESC is not running, but pid file exists."
                        exit 1
                else
                    log_success_msg "$DESC is not running."
                        exit 3
                fi
        else
                log_success_msg "$DESC is running with pid `cat $PIDFILE`, and is reachable on http://$HOSTNAME:$LOCATOR_PORT/"
        fi
        ;;

  restart)
        if ! locatorIsStopped ; then
                $0 stop $*
                sleep 1
        fi
        $0 start $*
        ;;

  check)
        log_success_msg "Checking arguments for obm-locator: "
        log_success_msg ""
        log_success_msg "PIDFILE        =  $PIDFILE"
        log_success_msg "JAVA_OPTIONS   =  $JAVA_OPTIONS"
        log_success_msg "JAVA           =  $JAVA"
        log_success_msg "LOCATOR_USER     =  $LOCATOR_USER"
        log_success_msg "ARGUMENTS      =  $ARGUMENTS"

        if [ -f $PIDFILE ]
        then
                log_success_msg "$DESC is running with pid `cat $PIDFILE`, and is reachable on http://$HOSTNAME:$LOCATOR_PORT/"
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
