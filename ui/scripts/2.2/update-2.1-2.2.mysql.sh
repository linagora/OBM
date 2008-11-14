#!/bin/bash
# 
# Converts OBM DB to UTF-8, and runs 2.1 to 2.2 schema upgrade
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

echo "database backup stored in ${HOME}/migration.sql"
mysqldump -u $U -p$P  --default-character-set='UTF8' ${DB} > ${HOME}/migration-backup.sql 
echo "Running 2.1 -> 2.2 schema upgrade script..."
mysql -u $U -p$P  --default-character-set='UTF8' ${DB} <  ./update-2.1-2.2.mysql.sql >/dev/null
echo "Updated data dump stored in ${HOME}/migration.sql"
mysqldump -u $U -p$P -tcn  --default-character-set='UTF8' ${DB} > ${HOME}/migration.sql 
success=$?

test ${success} -eq 0 || {
    echo "Error dumping DB to ${HOME}/migration.sql, abort."
    exit 1
}

echo "Drop/Create database"
mysqladmin -u $U -p$P -f drop ${DB}

mysqladmin -u $U -p$P --default-character-set='UTF8' create ${DB}

echo "Running 2.2 creation schema script..."
mysql -u $U -p$P  --default-character-set='UTF8' ${DB} < ./create_obmdb_2.2.mysql.sql 
echo "Inserting migrated data from migration.sql"
mysql -u $U -p$P  --default-character-set='UTF8' ${DB} < ${HOME}/migration.sql 
success=$?

test ${success} -eq 0 || {
    echo "Error running 2.1 to 2.2 upgrade script."
    exit 1
}
echo "[DONE]"

echo "Reloading default 2.2 preferences..."
mysql -u $U -p$P  --default-character-set='UTF8' ${DB} <  ./obmdb_prefs_values_2.2.sql >/dev/null
success=$?
test ${success} -eq 0 || {
    echo "Error reloading default 2.2 preferences."
    exit 1
}
echo "[DONE]"

exit 0
