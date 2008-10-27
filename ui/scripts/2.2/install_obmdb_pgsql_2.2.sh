#!/bin/su postgres

test $# -eq 4 || {
    echo "usage: $0 db user password lang"
    exit 1
}

db=$1
user=$2
pw=$3
obm_lang=$4


echo "  Delete old database"
dropdb ${db}


dropuser ${user}

echo "Creating role '${user}' (pw: ${pw}) & db '${db}' (lang: ${obm_lang})..."
createuser --createdb --no-superuser --no-createrole --login ${user}

psql template1 <<EOF
ALTER USER ${user} WITH PASSWORD '${pw}'
\q
EOF

echo "  Create new $DB database"

createdb -O ${user} --encoding=UTF-8 ${db}

psql ${db} <<EOF
CREATE LANGUAGE plpgsql;
\q
EOF

psql -U ${user} ${db} -f \
create_obmdb_2.2.pgsql.sql > /tmp/data_insert.log 2>&1

psql -U ${user} ${db} -f \
obmdb_default_values_2.2.sql >> /tmp/data_insert.log 2>&1

psql -U ${user} ${db} -f \
obmdb_triggers_2.2.pgsql.sql >> /tmp/data_insert.log 2>&1

echo "  Dictionnary data insertion"
psql -U ${user} ${db} -f \
data-${obm_lang}/obmdb_ref_2.2.sql >> /tmp/data_insert.log 2>&1

echo "  Company Naf Code data insertion"
psql -U ${user} ${db} -f \
data-${obm_lang}/obmdb_nafcode_2.2.sql >> /tmp/data_insert.log 2>&1

echo "  Default preferences data insertion"
psql -U ${user} ${db} -f \
obmdb_prefs_values_2.2.sql >> /tmp/data_insert.log 2>&1

psql -U ${user} -q ${db} <<EOF
UPDATE UserObmPref SET userobmpref_value='${obm_lang}' WHERE userobmpref_option='set_lang'
\q
EOF

echo "DONE."
