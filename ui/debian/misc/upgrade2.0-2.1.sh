#!/bin/sh
#Shell script to upgrade OBM 2.0 to 2.1

loginBdObm ()
{
	grep "dbuser" /etc/obm/debian-db-obm.conf | cut -d"=" -f2
}

passwdBdObm ()
{
	grep "dbpass" /etc/obm/debian-db-obm.conf | cut -d"=" -f2	
}

nameBdObm ()
{
	grep "dbname" /etc/obm/debian-db-obm.conf | cut -d"=" -f2	
}

LOGIN=loginBdObm
PASSWD=passwdBdObm
BD=nameBdObm

#/usr/bin/mysql -u $LOGIN -p$PASSWD $BD < /usr/share/obm/scripts/2.1/update-2.0-2.1.mysql.sql
#/usr/bin/php -d 

touch /tmp/upgra20to21


exit 0

