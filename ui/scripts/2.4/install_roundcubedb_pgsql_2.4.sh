#!/bin/bash

db=$1
user=$2
pw=$3

echo "Creating ${db} database"
su - postgres -c "dropdb ${db}"
su - postgres -c "createdb  -O ${user} --encoding=UTF-8 ${db}"

echo "Filling ${db} database"
psql -U ${user} -h localhost ${db} -f "../../php/webmail/SQL/postgres.initial.sql"
psql -U ${user} -h localhost ${db} -f "../../php/webmail/SQL/postgres.update.sql"