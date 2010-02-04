#!/bin/bash
# 
# Runs 2.2 to 2.3 schema upgrade
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
echo "Database : $DB"

mysqldump -u $U -p$P  --default-character-set='UTF8' ${DB} > ${HOME}/migration-backup.sql 
echo "database backup stored in ${HOME}/migration-backup.sql"

echo "Running 2.2 -> 2.3 schema upgrade script..."
mysql -u $U -p$P  --default-character-set='UTF8' ${DB} <  ./update-2.2-2.3.mysql.sql >/dev/null
status=$?
test $status -eq 0 || {
    echo "error on data migration, aborting."
    exit 1
}

echo "Reloading default 2.3 preferences..."
mysql -u $U -p$P  --default-character-set='UTF8' ${DB} <  ./obmdb_prefs_values_2.3.sql >/dev/null
success=$?
test ${success} -eq 0 || {
    echo "Error reloading default 2.3 preferences."
    exit 1
}
echo "[DONE]"

exit 0

