#!/bin/bash
# 
# Runs 2.2 to 2.3 schema upgrade
#

export LC_ALL=en_US.UTF-8

source `dirname $0`/obm-sh.lib

# DB parameters
get_val user
U=$VALUE
get_val password
P=$VALUE
get_val db
DB=$VALUE

export PGPASSWORD=$P

#test redhat instalation"
if [ -d "/var/lib/pgsql" ]; then
  postrges_dir="/var/lib/pgsql"
else
  postrges_dir="/var/lib/postgresql"
fi


su - postgres -c "pg_dump --file=${postgres_dir}migration.sql --format=p \
--encoding=UTF-8 \
${DB} >/dev/null"
success=$?

test ${success} -eq 0 || {
    echo "Error dumping DB to ${postgres_dir}/migration.sql, abort."
    exit 1
}
echo "Dump stored in ${postgres_dir}/migration.sql"

echo "Running 2.2 -> 2.3 schema upgrade script..."
psql -U ${U} -h localhost ${DB} -f ./update-2.2-2.3.pgsql.sql >>/tmp/update_obm.log 2>&1
success=$?
test ${success} -eq 0 || {
    echo "Error running 2.2 to 2.3 upgrade script."
    exit 1
}
echo "[DONE]"

echo "Reloading default 2.3 preferences..."
psql -U ${U} -h localhost ${DB} -f ./obmdb_prefs_values_2.3.sql >>/tmp/update_obm.log 2>&1
success=$?
test ${success} -eq 0 || {
    echo "Error reloading default 2.3 preferences."
    exit 1
}
echo "[DONE]"


exit 0
