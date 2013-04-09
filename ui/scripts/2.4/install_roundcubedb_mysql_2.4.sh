#!/bin/bash


db=$1
user=$2
pw=$3

echo "Creating $db database"
mysql -u $user -p$pw -e "DROP DATABASE IF EXISTS $db"
mysql -u $user -p$pw -e "CREATE DATABASE $db CHARACTER SET utf8 COLLATE utf8_general_ci"

echo "Filling ${db} database"
mysql -u $user -p$pw $db < ../../php/webmail/SQL/mysql.initial.sql
mysql -u $user -p$pw $db < ../../php/webmail/SQL/mysql.update.sql