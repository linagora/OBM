#!/bin/bash

db=$1
user=$2
pw=$3

echo "Creating ${db} database"
ignored=$(su - postgres -c "dropdb ${db}" >/dev/null 2>&1)
su - postgres -c "createdb  -O ${user} --encoding=UTF-8 ${db}"

echo "Filling ${db} database"
PGPASSWORD="$pw" psql -U ${user} -h localhost ${db} -f "/usr/share/obm/www/webmail/SQL/postgres.initial.sql"
