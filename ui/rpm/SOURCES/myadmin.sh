#!/bin/bash
# Copyright (C) 2011-2014 Linagora
# 
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option) any
# later version, provided you comply with the Additional Terms applicable for OBM
# software by Linagora pursuant to Section 7 of the GNU Affero General Public
# License, subsections (b), (c), and (e), pursuant to which you must notably (i)
# retain the displaying by the interactive user interfaces of the “OBM, Free
# Communication by Linagora” Logo with the “You are using the Open Source and
# free version of OBM developed and supported by Linagora. Contribute to OBM R&D
# by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
# links between OBM and obm.org, between Linagora and linagora.com, as well as
# between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
# from infringing Linagora intellectual property rights over its trademarks and
# commercial brands. Other Additional Terms apply, see
# <http://www.linagora.com/licenses/> for more details.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License and
# its applicable Additional Terms for OBM along with this program. If not, see
# <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
# version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
# applicable to the OBM software.


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

