#!/bin/su postgres
# 
# Runs 2.2 to 2.3 schema upgrade
#

echo "Postgres update shell script"

test ${USER} = "postgres" || {
    echo "$0 must run as postgres user"
    exit 1
}
# to ensure things defaulting to system locale don't fail
export LC_ALL=en_US.UTF-8

source `dirname $0`/obm-sh.lib

# DB parameters
get_val user
U=$VALUE
get_val password
P=$VALUE
get_val db
DB=$VALUE

pg_dump --file=${HOME}/migration.sql --format=p \
--encoding=UTF-8 \
${DB} >/dev/null
success=$?

test ${success} -eq 0 || {
    echo "Error dumping DB to ${HOME}/migration.sql, abort."
    exit 1
}
echo "Dump stored in ${HOME}/migration.sql"

echo "Running 2.2 -> 2.3 schema upgrade script..."
psql -U ${U} ${DB} -f ./update-2.2-2.3.pgsql.sql >>/tmp/update_obm.log 2>&1
success=$?
test ${success} -eq 0 || {
    echo "Error running 2.2 to 2.3 upgrade script."
    exit 1
}
echo "[DONE]"

echo "Reloading default 2.3 preferences..."
psql -U ${U} ${DB} -f ./obmdb_prefs_values_2.3.sql >>/tmp/update_obm.log 2>&1
success=$?
test ${success} -eq 0 || {
    echo "Error reloading default 2.3 preferences."
    exit 1
}
echo "[DONE]"


exit 0
