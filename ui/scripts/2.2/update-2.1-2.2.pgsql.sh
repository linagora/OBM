#!/bin/su postgres
# 
# Converts OBM DB to UTF-8, and runs 2.1 to 2.2 schema upgrade
#

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
echo "utf-8 encoded dump stored in ${HOME}/migration.sql"

dropdb ${DB}
dropuser ${U}
createuser --createdb --no-superuser --no-createrole --login ${U}

psql template1 <<EOF
alter user ${U} with password '${P}'
\q
EOF

createdb -O ${U} --encoding=UTF-8 ${DB}

psql -U postgres ${DB} -f ${HOME}/migration.sql >/tmp/update_obm.log 2>&1

echo "utf-8 dump still available in ${HOME}/migration.sql"

echo "Running 2.1 -> 2.2 schema upgrade script..."
psql -U ${U} ${DB} -f ./update-2.1-2.2.pgsql.sql >>/tmp/update_obm.log 2>&1
success=$?
test ${success} -eq 0 || {
    echo "Error running 2.1 to 2.2 upgrade script."
    exit 1
}
echo "[DONE]"

psql ${DB} <<EOF
CREATE LANGUAGE plpgsql;
\q
EOF

psql -U ${U} ${DB} -f ./obmdb_triggers_2.2.pgsql.sql >>/tmp/update_obm.log 2>&1
success=$?
test ${success} -eq 0 || {
    echo "Error adding 2.2 triggers."
    exit 1
}
echo "[DONE]"


exit 0
