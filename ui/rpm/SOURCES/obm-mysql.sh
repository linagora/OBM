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


obmconf="/etc/obm/obm-rpm.conf"
REP_DOC="/usr/share/doc"
REP_SCRIPTS_OBM="/usr/share/obm/scripts/2.4"
SCRIPT_INSTALL="$REP_SCRIPTS_OBM/install_obmdb_2.4.sh"
SCRIPT_UPDATE="$REP_SCRIPTS_OBM/update-$FROM_VER-$TO_VER.sh"
LIB_ADMIN_MY="/usr/bin/myadmin.lib"

echo "================= OBM MySQL configuration =================="
echo

if [ -s ${obmconf} ]; then
        source ${obmconf}
else
        echo "Erreur le fichier ${obmconf} n'existe pas"
        exit 1
fi

source ${LIB_ADMIN_MY}


# Check if MySQL server is running
#check mysql-server status
/sbin/service mysqld status %2>/dev/null	
if [ "$?" -gt "0" ] ; then
	/sbin/service mysqld start %2>/dev/null
fi

echo -e "Do you have a root password for mysql: y(es),n(o) [n]\c "
read have_root_pw
if [ "x${have_root_pw}" == "xy" ]; then
	echo -e "Do you want modify root password: y(es),n(o) [n]\c "
	read change_root_pw
	if [ "x${change_root_pw}" == "xy" ]; then
		echo -e "Enter MySQL old root Password : \c "
		echo -e "\r"
		stty -echo
		read old_root_passwd
		stty echo
		echo -e "Enter MySQL new root Password : \c "
		echo -e "\r"
		stty -echo
		read new_root_passwd
		stty echo
		
		if [ "x${have_root_pw}" == "xy" ]; then
			fix_root_perms $old_root_passwd $new_root_passwd
		else
			fix_root_perms $new_root_passwd
			
		fi
		MYSQL_CMD="/usr/bin/mysql -u root -p${new_root_passwd}"
	else
		
		echo -e "Enter MySQL root Password : \c "
		echo -e "\r"
		stty -echo
		read root_passwd
		stty echo
		
		MYSQL_CMD="/usr/bin/mysql -u root -p${root_passwd}"
	fi
else
	MYSQL_CMD="/usr/bin/mysql"
fi

# Check if database already exist
CHECKDB=`${MYSQL_CMD} -e "SHOW DATABASES"| grep ^${OBM_DBNAME}$`
if [ "$CHECKDB" = "${OBM_DBNAME}" ] ; then
	echo "DataBase ${OBM_DBNAME} already exist"
	echo "What do you want? update(not yet) / reinstall / nothing"
        read wanted
        case "$wanted" in
                reinstall)
			# Add user to DataBase
			fix_my_perms ${OBM_DBNAME} ${OBM_DBUSER} ${OBM_PASSWD}
                        echo "Reinstall obm DB"
                        pushd $REP_SCRIPTS_OBM 1>/dev/null
                        $SCRIPT_INSTALL
                        popd 1>/dev/null
                ;;
                *)
                        echo "nothing"  
                ;;
        esac
else
        echo "Installing OBM DB"
	# Add user to DataBase
	fix_my_perms ${OBM_DBNAME} ${OBM_DBUSER} ${OBM_PASSWD}
        pushd $REP_SCRIPTS_OBM 1>/dev/null
        $SCRIPT_INSTALL
        popd 1>/dev/null

fi

echo
echo -e "================= End of OBM MySQL configuration ==================\n"
