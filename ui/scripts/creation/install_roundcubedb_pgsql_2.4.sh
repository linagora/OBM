#!/bin/bash

db=$1
user=$2
pw=$3

if [ -f /usr/share/obm/www/php/webmail/SQL/postgres.initial.sql ]
then
  init_path=/usr/share/obm/www/php/webmail/SQL/postgres.initial.sql 
else
  init_path=/usr/share/obm/php/webmail/SQL/postgres.initial.sql 
fi

echo "Creating ${db} database"
ignored=$(su - postgres -c "dropdb ${db}" >/dev/null 2>&1)
su - postgres -c "createdb  -O ${user} --encoding=UTF-8 ${db}"

echo "Filling ${db} database"
PGPASSWORD="$pw" psql -U ${user} -h localhost ${db} -f "$init_path"
