#!/bin/bash

test $# -eq 3 || {
    echo "usage: $0 db user password"
    exit 1
}

db=$1
user=$2
pw=$3

echo "Creating role '${user}' (pw: ${pw}) & db '${db}' as user postgres..."

dropdb ${db}

dropuser ${user}
createuser --createdb --no-superuser --no-createrole --login ${user}

psql template1 <<EOF
alter user ${user} with password '${pw}'
\q
EOF

createdb -O ${user} --encoding=ISO-8859-1 ${db}

psql -U ${user} ${db} -f create_obmdb_2.1.pgsql.sql
psql -U ${user} ${db} -f obmdb_default_values_2.1.sql
psql -U ${user} ${db} -f data-en/obmdb_ref_2.1.sql
psql -U ${user} ${db} -f data-en/obmdb_nafcode_2.1.sql
psql -U ${user} ${db} -f obmdb_prefs_values_2.1.sql

psql -U ${user} ${db} <<EOF
UPDATE UserObmPref set userobmpref_value='fr' where userobmpref_option='set_lang'
\q
EOF

echo "DONE."
