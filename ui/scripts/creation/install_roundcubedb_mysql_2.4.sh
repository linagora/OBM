#!/bin/bash


db=$1
user=$2
pw=$3

if [ -f /usr/share/obm/www/php/webmail/SQL/mysql.initial.sql ]
then
  init_path=/usr/share/obm/www/php/webmail/SQL/mysql.initial.sql 
else
  init_path=/usr/share/obm/php/webmail/SQL/mysql.initial.sql 
fi

echo "Creating $db database"
mysql -u $user -p$pw -e "DROP DATABASE IF EXISTS $db"
mysql -u $user -p$pw -e "CREATE DATABASE $db CHARACTER SET utf8 COLLATE utf8_general_ci"

echo "Filling ${db} database"
mysql -u $user -p$pw $db < $init_path
