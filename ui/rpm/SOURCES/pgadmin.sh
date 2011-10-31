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


FIC_PERM_PG="/var/lib/pgsql/data/pg_hba.conf"
FIC_CONF_PG="/var/lib/pgsql/data/postgresql.conf"

function check_pg_init {
	if [ ! -s $FIC_PERM_PG ]; then
		echo "Initilise postgres"
                /etc/init.d/postgresql initdb
		if [ $? -eq 0 ]; then
			return 0
		else
			return 1
		fi
	else
		echo "postgres already initialized"
		return 0
	fi
}

function check_pg_status {
	/etc/init.d/postgresql status 1>/dev/null 2>&1
        if [ $? -ne 0 ]; then
                /etc/init.d/postgresql start
                while [ ! -S /tmp/.s.PGSQL.5432 ];do
                        echo "Attente de PostgreSQL"
                        sleep 2
                done
		return 0
	else
		return 1
        fi

}

function check_args {
	if [ $# -lt "3" -o $# -gt "4" ]; then
		echo "Erreur de paramatre pour les fonction check_pg_perms"
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
	echo "Checking permission on Postgres"
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
	echo "Fix permission on Postgres"
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
		echo "Erreur de parametre dans la function check_listen_adress"
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
                echo "Erreur de parametre dans la function check_listen_adress"
                return 1
        fi
	sed -i -e "/^#listen_addresses.*$/ a\listen_addresses = \'${LISTEN_HOST}\'" $FIC_CONF_PG

}

