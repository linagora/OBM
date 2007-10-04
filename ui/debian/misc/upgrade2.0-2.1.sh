#!/bin/sh
#Shell script to upgrade OBM 2.0 to 2.1

OBM_USER=`grep "dbuser" /etc/obm/debian-db-obm.conf | cut -d"=" -f2`
OBM_PASSWD=`grep "dbpass" /etc/obm/debian-db-obm.conf | cut -d"=" -f2`	
OBM_DBNAME=`grep "dbname" /etc/obm/debian-db-obm.conf | cut -d"=" -f2`	
OBM_HOST=`grep "dbserver" /etc/obm/debian-db-obm.conf | cut -d"=" -f2`

cd /usr/share/obm/www/scripts/2.1/
/usr/bin/mysql -u ${OBM_USER} -p${OBM_PASSWD} ${OBM_DBNAME} < /usr/share/obm/www/scripts/2.1/update-2.0-2.1.mysql.sql || true
/usr/bin/php -d include_path="/usr/share/obm/www"  /usr/share/obm/www/scripts/2.1/update-2.0-2.1.php || true

#touch /tmp/upgra20to21


exit 0

