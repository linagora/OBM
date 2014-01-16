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


# Default parameters
PGPORT=5432
PGDATA=/var/lib/pgsql/data
PGLOG=/var/lib/pgsql/pgstartup.log
# Can be overrided in this file in EL
[ -f /etc/sysconfig/pgsql/postgresql ] && ./etc/sysconfig/pgsql/postgresql
FIC_PERM_PG="${PGDATA}/pg_hba.conf"
FIC_CONF_PG="${PGDATA}/postgresql.conf"


function check_pg_xa_init {
	# Only modified on a new local database
	PG_MAX_CONNECTION=`grep '^max_connections\b' $FIC_CONF_PG | awk '{print $3}'`
	sed -i -e "s|#max_prepared_transactions = 0|max_prepared_transactions = ${PG_MAX_CONNECTION}|"  $FIC_CONF_PG
}

function check_pg_init {
	if [ ! -s $FIC_PERM_PG ]; then
		echo "Initilise postgres"
		if [ -e /usr/bin/postgresql-setup ] ; then
			/usr/bin/postgresql-setup initdb
		else
			/etc/init.d/postgresql initdb
		fi
		if [ $? -eq 0 ]; then
			check_pg_xa_init
			return 0
		else
			return 1
		fi
	else
		echo "PostgreSQL already initialized"
		return 0
	fi
}

function check_pg_status {
	service postgresql status 1>/dev/null 2>&1
        if [ $? -ne 0 ]; then
                service postgresql start
                while [ ! -S /tmp/.s.PGSQL.${PGPORT} ];do
                        echo "Waiting for PostgreSQL"
                        sleep 2
                done
		return 0
	else
		return 1
        fi

}

function check_args {
	if [ $# -lt "3" -o $# -gt "4" ]; then
		echo "Parameter error for the check_pg_perms functions"
		echo "ou fix_pg_perms"
		exit 1
	fi
	if [ $# -eq 4 ]; then
		DATABASE=$1
		USER=$2
		CIDR_ADDRESS=$3
		METHOD=$4
	fi
	if [ $# -eq 3 ]; then
		DATABASE=$1
		USER=$2
		METHOD=$3
	fi
}

function check_pg_perms {
	check_args $@
	echo "Checking permission on PostgreSQL"
	if [ "x${CIDR_ADDRESS}" == "x" ]; then
		# Nous somme en mode local
		LOCAL=`grep "local ${DATABASE} ${USER} ${METHOD}" $FIC_PERM_PG`
		if [ "x${LOCAL}" == "x" ]; then
			return 1
		else
			return 0
		fi
	else
		# Nous somme en mode cidr
		HOST=`grep "host ${DATABASE} ${USER} ${CIDR_ADDRESS} ${METHOD}" $FIC_PERM_PG`
		if [ "x${HOST}" == "x" ]; then
			return 1
		else
			return 0
		fi
	fi

}

function fix_pg_perms {
	check_args $@ 
	echo "Fix permission on PostgreSQL"
	if  [ "x${CIDR_ADDRESS}" == "x" ]; then
                # Nous somme en mode local
		 sed -i -e "1,/^local.*$/s/^\(local.*\)$/local ${DATABASE} ${USER} ${METHOD}\n\1/" $FIC_PERM_PG
        else
                # Nous somme en mode cidr
		sed -i -e "1,/^host.*$/s|^\(host.*\)$|host ${DATABASE} ${USER} ${CIDR_ADDRESS} ${METHOD} \n\1|" $FIC_PERM_PG
	fi
}	

function check_listen_adress {
	LISTEN_HOST=$1
	if [ $# -ne 1 ]; then
		echo "Parameter error for the check_listen_adress function"
		return 1
	fi
	LISTEN=`grep  "^listen_addresses = \'${LISTEN_HOST}\'" $FIC_CONF_PG`
	if [ "x${LISTEN}" == "x" ]; then
		return 1
	else
		return 0
	fi
}

function fix_listen_adress {
	LISTEN_HOST=$1
        if [ $# -ne 1 ]; then
                echo "Parameter error for the check_listen_adress function"
                return 1
        fi
	sed -i -e "/^#listen_addresses.*$/ a\listen_addresses = \'${LISTEN_HOST}\'" $FIC_CONF_PG

}

