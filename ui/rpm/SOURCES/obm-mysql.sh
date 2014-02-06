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

obmconf="/etc/obm/obm-rpm.conf"
REP_DOC="/usr/share/doc"
REP_SCRIPTS_OBM="/usr/share/obm/scripts/creation"
SCRIPT_INSTALL="$REP_SCRIPTS_OBM/install_obmdb.sh"
SCRIPT_RC_INSTALL="$REP_SCRIPTS_OBM/install_roundcubedb_2.4.sh"
SCRIPT_UPDATE="$REP_SCRIPTS_OBM/update-$FROM_VER-$TO_VER.sh"
LIB_ADMIN_MY="/usr/bin/myadmin.lib"
RC_DBNAME="roundcubemail"

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

# Check if roundcube database already exist
CHECKDB=`${MYSQL_CMD} -e "SHOW DATABASES"| grep ^${RC_DBNAME}$`
if [ "$CHECKDB" = "${RC_DBNAME}" ] ; then
       echo "${RC_DBNAME} database already exist, skipping."
else
       echo "Installing ${RC_DBNAME} database."
       fix_my_perms ${RC_DBNAME} ${OBM_DBUSER} ${OBM_PASSWD}
       pushd $REP_SCRIPTS_OBM 1>/dev/null
       $SCRIPT_RC_INSTALL
       popd 1>/dev/null
fi

echo
echo -e "================= End of OBM MySQL configuration ==================\n"
