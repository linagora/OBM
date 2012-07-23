#!/bin/bash -x         

set -e

BACKUPPATH="/var/lib/obm/backup"
OLDBACKUPROOT="/var/backups/obm"
J1="$BACKUPPATH/Daily"
S1="$BACKUPPATH/Weekly1"
S2="$BACKUPPATH/Weekly2"
M1="$BACKUPPATH/Monthly1"
M2="$BACKUPPATH/Monthly2"

init() {
    for i in $J1 $S1 $S2 $M1 $M2; 
    do
        /bin/mkdir -pv $i
        moveSatelliteBackup $i
    done
}

moveSatelliteBackup() {
    if [ "$(ls -A $OLDBACKUPROOT)" ]
    then
        [ "$(ls -A $OLDBACKUPROOT)" ] && /bin/cp -rv $OLDBACKUPROOT/* $1
    fi
}

removeDailyBackup() {
    echo "Processing a daily backup"
    forceRemove $J1
}

moveWeeklyBackup() {
    echo "Processing a weekly backup"
    forceRemove $S2
    moveDir $S1 $S2
    moveDailyTo $S1
}

moveMonthlyBackup() {
    echo "Processing a montly backup"
    forceRemove $M2
    moveDir $M1 $M2
    moveDailyTo $M1
}

forceRemove() {
    /bin/rm -rfv $1/*
}

moveDir() {
    if [ "$(ls -A $1)" ] 
    then
        /bin/mv -v $1/* $2
    fi
}

moveDailyTo() {
    if [ "$(ls -A $J1)" ] 
    then
        /bin/cp -rv $J1/* $1
    fi
}

case "$1" in
    -d) removeDailyBackup
        ;;
    -w) moveWeeklyBackup
        ;;
    -m) moveMonthlyBackup
        ;;
    -init) init
        ;;
esac
    
