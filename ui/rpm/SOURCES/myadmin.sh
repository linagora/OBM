#!/bin/bash
#+-------------------------------------------------------------------------+
#|   Copyright (c) 1997-2009 OBM.org project members team                  |
#|                                                                         |
#|  This program is free software; you can redistribute it and/or          |
#|  modify it under the terms of the GNU General Public License            |
#|  as published by the Free Software Foundation; version 2                |
#|  of the License.                                                        |
#|                                                                         |
#|  This program is distributed in the hope that it will be useful,        |
#|  but WITHOUT ANY WARRANTY; without even the implied warranty of         |
#|  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          |
#|  GNU General Public License for more details.                           | 
#+-------------------------------------------------------------------------+
#|  http://www.obm.org                                                     |
#+-------------------------------------------------------------------------+


function fix_my_perms {
	if [ $# -eq 3 ]; then
		DATABASE=$1
		USER=$2
		PASSWD=$3
	else
		echo "Erreur de paramatre pour la fonction fix_my_perms"
		exit 1
	fi

	HOSTNAME=`hostname -f` > /dev/null 2>&1
	if [ $? -eq 1 ]; then
		HOSTNAME=`hostname`
	fi
	echo "Fix permission to Mysql "
	$MYSQL_CMD -e "GRANT ALL on ${DATABASE}.* TO ${USER}@'localhost' IDENTIFIED BY '${PASSWD}'"
	$MYSQL_CMD -e "GRANT ALL on ${DATABASE}.* TO ${USER}@'127.0.0.1' IDENTIFIED BY '${PASSWD}'"
	$MYSQL_CMD -e "GRANT ALL on ${DATABASE}.* TO ${USER}@'${HOSTNAME}' IDENTIFIED BY '${PASSWD}'"
	
}	

function fix_root_perms {
	if [ $# -eq 2 ]; then
		OLD_PASSWORD=$1
		PASSWORD=$2
		mysqladmin password -p${OLD_PASSWORD} ${PASSWORD}
	elif [ $# -eq 1]; then
		PASSWORD=$1
		mysqladmin password ${PASSWORD}
	else
	
		echo "Erreur de paramatre pour la fonction check_root_perms"
		exit 1
		
	fi
}

