#!/bin/bash -x         

set -e
shopt -s extglob

BACKUPROOT="/var/lib/obm/backup"
J1="$BACKUPROOT/Daily"
S1="$BACKUPROOT/Weekly1"
S2="$BACKUPROOT/Weekly2"
M1="$BACKUPROOT/Monthly1"
M2="$BACKUPROOT/Monthly2"

FLAGFILE="$BACKUPROOT/successDailyBackup.txt"
LOGFILE="/var/log/obm-cron.log"

ADMINMAIL="admin@centos.obm.com"
SUBJECT="The daily backup has failed."
MAILMESSAGE="The daily backup has encountered an error and the flag file has not been moved. Please check the log /var/log/obm-cron.log to find out what has caused the fail."

init() {
    /bin/mkdir -pv $J1
    moveSatelliteBackup "$BACKUPROOT/*" $J1

    find $BACKUPROOT/* -type d -not \
        \( -path $J1 -prune \) \
        -exec rm -rvf {} +
    rm -rvf $FLAGFILE

    for i in $S1 $S2 $M1 $M2; 
    do
        /bin/mkdir -pv $i
        moveSatelliteBackup "$J1/*" $i
    done
}

moveSatelliteBackup() {
    echo "Moving old backup to $2"
    $(which rsync) -avvz \
                   --progress \
                   --exclude Daily \
                   --log-file=$LOGFILE \
                   $1 $2
}

moveDailyBackup() {
    echo "Processing a daily backup"
    $(which rsync) -avvz \
                   --delete-before \
                   --progress \
                   --exclude Daily \
                   --exclude Monthly1 \
                   --exclude Weekly1 \
                   --exclude Monthly2 \
                   --exclude Weekly2 \
                   --log-file=$LOGFILE \
                   $BACKUPROOT/* $J1
}

moveWeeklyBackup() {
    echo "Processing a weekly backup"
    moveDir $S1 $S2
    moveDir $J1 $S1
}

moveMonthlyBackup() {
    echo "Processing a montly backup"
    moveDir $M1 $M2
    moveDir $J1 $M1
}


forceRemoveFlagFile() {
    echo "Deleting moved daily backups and flag file"
    find $BACKUPROOT/* -type d -not \
        \( -path $J1 -prune -o \
           -path $S1 -prune -o \
           -path $S2 -prune -o \
           -path $M1 -prune -o \
           -path $M2 -prune \) \
        -exec rm -rvf {} +
    /bin/rm -rvf $FLAGFILE
}

moveDir() {
    $(which rsync) -avvz --delete-before --log-file=$LOGFILE $1/* $2
}

checkAndSendMail() {
    if [ ! -d $BACKUPROOT ] || [ -f $FLAGFILE ]
    then
        echo $MAILMESSAGE | $(which mail) -s "$SUBJECT" "$ADMINMAIL"
    fi
}

case "$1" in
    -d) moveDailyBackup
        forceRemoveFlagFile
        ;;
    -w) moveDailyBackup
        moveWeeklyBackup
        forceRemoveFlagFile
        ;;
    -m) moveDailyBackup
        moveMonthlyBackup
        forceRemoveFlagFile
        ;;
    -mw)moveDailyBackup
        moveWeeklyBackup
        moveMonthlyBackup
        forceRemoveFlagFile
        ;;
    -check)
        checkAndSendMail
        ;;
    -init) init
        ;;
esac  
