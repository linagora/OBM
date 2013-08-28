#!/bin/bash
# 
# Runs 2.5 to 3.0 schema upgrade
#

# to ensure things defaulting to system locale don't fail

echo "MySQL update shell script"

export LC_ALL=en_US.UTF-8

source `dirname $0`/obm-sh.lib

# DB parameters
get_val user
U=$VALUE
get_val password
P=$VALUE
get_val db
DB=$VALUE

mysqldump -u $U -p$P  --default-character-set='UTF8' ${DB} | gzip > ${HOME}/migration-backup.sql.gz 
echo "database backup stored in ${HOME}/migration-backup.sql.gz"

echo "Running 2.5 -> 3.0 schema upgrade script..."
mysql -u $U -p$P  --default-character-set='UTF8' ${DB} <  ./update-2.5-3.0.mysql.sql >/dev/null
status=$?
test $status -eq 0 || {
    echo "error on data migration, aborting."
    exit 1
}
echo "[DONE]"

exit 0

