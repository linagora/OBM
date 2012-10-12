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


obmconf=/etc/obm/obm-rpm.conf
REP_BIN_PGSQL="/usr/bin"
REP_SCRIPTS_OBM="/usr/share/obm/scripts/creation"
SCRIPT_INSTALL="$REP_SCRIPTS_OBM/install_obmdb.sh"
SCRIPT_RC_INSTALL="$REP_SCRIPTS_OBM/install_roundcubedb_2.4.sh"
SCRIPT_UPDATE="$REP_SCRIPTS_OBM/update-$FROM_VER-$TO_VER.sh"
LIB_ADMIN_PG="/usr/bin/pgadmin.lib"
RC_DBNAME="roundcubemail"

echo "=============== OBM DataBase initialisation ================"
echo


if [ -s ${obmconf} ]; then
	source ${obmconf}
else
	echo "Erreur le fichier ${obmconf} n'existe pas"
	exit 1
fi

source ${LIB_ADMIN_PG}
export PGPASSWORD="${OBM_PASSWD}"

# Check if PostgreSQL is init
check_pg_init
if [ $? -eq 1 ]; then
	echo "Erreur dans l'int de postgres"
	exit 1
fi

# Check perm and fix it
restart_pgsql=0
check_pg_perms ${OBM_DBNAME} ${OBM_DBUSER} md5
if [ $? -eq 1 ]; then
	fix_pg_perms ${OBM_DBNAME} ${OBM_DBUSER} md5
	restart_pgsql=1
fi
check_pg_perms ${OBM_DBNAME} ${OBM_DBUSER} 0.0.0.0/0 md5
if [ $? -eq 1 ]; then
	fix_pg_perms ${OBM_DBNAME} ${OBM_DBUSER} 0.0.0.0/0 md5
	restart_pgsql=1
fi
check_pg_perms ${OBM_DBNAME} ${OBM_DBUSER} ::1/128 md5
if [ $? -eq 1 ]; then
    fix_pg_perms ${OBM_DBNAME} ${OBM_DBUSER} ::1/128 md5
    restart_pgsql=1
fi
  

# Check listen addrese and fix it
if [ "${OBM_HOST}" != "127.0.0.1" ]; then
	#check_listen_adress ${OBM_HOST}
	check_listen_adress 0.0.0.0
	if [ $? -eq 1 ]; then
		#fix_listen_adress ${OBM_HOST}
		fix_listen_adress 0.0.0.0
		restart_pgsql=1
	fi
fi
if [ "${restart_pgsql}" -eq 1 ]; then
	/etc/init.d/postgresql restart
	 while [ ! -S /tmp/.s.PGSQL.5432 ];do
                echo "Attente de PostgreSQL"
                sleep 2
        done
fi

# Execution du script d'installation de la BD
# Choix d'exection du script d'install ou d'upgrade
echo "Vérification de la base de données ${OBM_DBNAME}"
CHECKBD=`su - postgres -c "$REP_BIN_PGSQL/psql -c \"\l\"" |grep ${OBM_DBNAME} |awk '{print $1}'`

if [ "x$CHECKBD" == "x${OBM_DBNAME}" ]; then
	echo "The ${OBM_DBNAME} already exsit so"
	echo "What do you want? update(not yet) / reinstall / nothing"
	read wanted
	case "$wanted" in
		reinstall)
			echo "Reinstall obm DB"
			pushd $REP_SCRIPTS_OBM 1>/dev/null
			$SCRIPT_INSTALL
			popd 1>/dev/null
		;;
		#update)
		#	echo "Wath is your version of OBM DB(2.1)"
		#	read FROM_VER
		#	if [ "x${$FROM_VER}" == "x" ];then
		#		FROM_VER="2.1"
		#	fi
		#	echo "Wath version do you want of OBM DB (2.2)"
		#	read TO_VER 
		#	if [ "x${$TO_VER}" == "x" ];then
		#		FROM_VER="2.2"
		#	fi
		#	pushd $REP_SCRIPTS_OBM 1>/dev/null
		#	$SCRIPT_UPDATE
		#	popd 1>/dev/null
		#;;
		*)
			echo "nothing"	
		;;
	esac
else
	echo "Installing OBM DB"
	pushd $REP_SCRIPTS_OBM 1>/dev/null
	$SCRIPT_INSTALL
	popd 1>/dev/null
	
fi

echo "Vérification de la base de données ${RC_DBNAME}"
CHECKBD=`su - postgres -c "$REP_BIN_PGSQL/psql -c \"\l\"" |grep ${RC_DBNAME} |awk '{print $1}'`

if [ "x$CHECKBD" == "x${RC_DBNAME}" ]; then
	echo "${RC_DBNAME} database already exist, skipping."
else
	echo "Installing ${RC_DBNAME} database."
	pushd $REP_SCRIPTS_OBM 1>/dev/null
	$SCRIPT_RC_INSTALL
	popd 1>/dev/null
fi

unset PGPASSWORD

echo
echo "=========== end of OBM DataBase initialisation ============"

