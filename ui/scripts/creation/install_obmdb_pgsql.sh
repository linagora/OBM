#!/bin/bash

test $# -eq 5 || {
    echo "usage: $0 db user password lang installationtype"
    exit 1
}

db=$1
user=$2
pw=$3
obm_lang=$4
host=localhost
obm_installation_type=$5

export PGPASSWORD=$pw

if [ $obm_installation_type = "full" ]; then
  echo "  Delete old database"
  su - postgres -c "dropdb ${db}"
  
  
  su - postgres -c "dropuser ${user}"
  
  echo "Creating role '${user}' (pw: ${pw}) & db '${db}' (lang: ${obm_lang})..."
  su - postgres -c "createuser --createdb --no-superuser --no-createrole --login ${user}"
  
  su - postgres -c "psql template1 <<EOF
ALTER USER ${user} WITH PASSWORD '${pw}'
\q
EOF"
  
  echo "  Create new $DB database"
  
  su - postgres -c "createdb  -O ${user} --encoding=UTF-8 ${db}"

  su - postgres -c "psql ${db} <<EOF
CREATE LANGUAGE plpgsql;
ALTER DATABASE ${db} SET TIMEZONE='GMT';
\q
EOF"

fi

psql -U ${user} -h ${host} ${db} -f \
create_obmdb.pgsql.sql > /tmp/data_insert.log 2>&1
grep -i error /tmp/data_insert.log && {
    echo "error in pg script"
    exit 1
}

psql -U ${user} -h ${host} ${db} -f \
obmdb_default_values.sql >> /tmp/data_insert.log 2>&1
grep -i error /tmp/data_insert.log && {
    echo "error in pg script"
    exit 1
}

echo "  Dictionnary data insertion"
psql -U ${user} -h ${host} ${db} -f \
data-${obm_lang}/obmdb_ref.sql >> /tmp/data_insert.log 2>&1
grep -i error /tmp/data_insert.log && {
    echo "error in pg script"
    exit 1
}

echo "  Company Naf Code data insertion"
psql -U ${user} -h ${host} ${db} -f \
data-${obm_lang}/obmdb_nafcode.sql >> /tmp/data_insert.log 2>&1
grep -i error /tmp/data_insert.log && {
    echo "error in pg script"
    exit 1
}

echo "  Default preferences data insertion"
psql -U ${user} -h ${host} ${db} -f \
obmdb_prefs_values.sql >> /tmp/data_insert.log 2>&1
grep -i error /tmp/data_insert.log && {
    echo "error in pg script"
    exit 1
}

psql -U ${user} -h ${host} -q ${db} <<EOF
UPDATE UserObmPref SET userobmpref_value='${obm_lang}' WHERE userobmpref_option='set_lang'
\q
EOF

psql -U ${user} -h ${host} ${db} -f \
  "../2.5/updates/update-2.4.2.9~0.pgsql.sql" >> /tmp/data_insert.log 2>&1

psql -U ${user} -h ${host} ${db} -f \
  "../2.5/updates/update-2.5.7~1.pgsql.sql" >> /tmp/data_insert.log 2>&1

psql -U ${user} -h ${host} ${db} -f \
  "../2.5/updates/update-2.5.7~2.pgsql.sql" >> /tmp/data_insert.log 2>&1

psql -U ${user} -h ${host} ${db} -f \
  "../2.5/updates/update-2.5.8~1.pgsql.sql" >> /tmp/data_insert.log 2>&1

psql -U ${user} -h ${host} ${db} -f \
  "../2.6/updates/update-2.6.0~1.pgsql.sql" >> /tmp/data_insert.log 2>&1

psql -U ${user} -h ${host} ${db} -f \
  "../3.0/updates/update-3.0.0~1.pgsql.sql" >> /tmp/data_insert.log 2>&1

echo "DONE."
