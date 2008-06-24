#!/bin/bash

test $# -eq 4 || {
    echo "usage: $0 db user password lang"
    exit 1
}

db=$1
user=$2
pw=$3
obm_lang=$4

echo "Creating role '${user}' (pw: ${pw}) & db '${db}' (lang: ${obm_lang})..."
sleep 5

dropdb ${db}

dropuser ${user}
createuser --createdb --no-superuser --no-createrole --login ${user}

psql template1 <<EOF
alter user ${user} with password '${pw}'
\q
EOF

createdb -O ${user} --encoding=ISO-8859-1 ${db}

psql -U ${user} ${db} -f \
create_obmdb_2.2.pgsql.sql > /tmp/data_insert.log 2>&1
psql -U ${user} ${db} -f \
obmdb_default_values_2.2.sql >> /tmp/data_insert.log 2>&1
psql -U ${user} ${db} -f \
data-${obm_lang}/obmdb_ref_2.2.sql >> /tmp/data_insert.log 2>&1
psql -U ${user} ${db} -f \
data-${obm_lang}/obmdb_nafcode_2.2.sql >> /tmp/data_insert.log 2>&1
psql -U ${user} ${db} -f \
obmdb_prefs_values_2.2.sql >> /tmp/data_insert.log 2>&1

psql -U ${user} ${db} <<EOF
UPDATE UserObmPref set userobmpref_value='${obm_lang}' where userobmpref_option='set_lang'
\q
EOF

echo "DONE."
