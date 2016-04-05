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


# Declaration des contantes
BINOBM="/usr/bin"
OBMCONF="/etc/obm/obm-rpm.conf"
FIC_PERM_PG="/var/lib/pgsql/data/pg_hba.conf"
$BINOBM/obm-config

if [ -x $BINOBM/obm-core ] && [ -s $BINOBM/obm-core ] ;then
	$BINOBM/obm-core
fi

# Source du fichier de config rpm pour savoir
#	quel type de BD installer
. ${OBMCONF}

${BINOBM}/obm-pgsql

if [ -x $BINOBM/obm-sysusers ]; then
	${BINOBM}/obm-sysusers
fi

if [ -x $BINOBM/obm-cert ]; then
	${BINOBM}/obm-cert
fi

if [ -x $BINOBM/obm-ui ]; then
	${BINOBM}/obm-ui
fi

if [ -x $BINOBM/obm-ldap ] && [ -s $BINOBM/obm-ldap ] ;then
	$BINOBM/obm-ldap
fi

if [ -x $BINOBM/obm-funambol ] && [ -s $BINOBM/obm-funambol ] ;then
	$BINOBM/obm-funambol
fi

if [ -x $BINOBM/obm-funambol-storage ] && [ -s $BINOBM/obm-funambol-storage ] ;then

#	echo "Vérification des droits sur la base de données FUNAMBOL"
#	LOCAL=`grep "local ${FU_DBNAME} ${FU_DBUSER} trust" /var/lib/pgsql/data/pg_hba.conf`
#	HOST=`grep "host ${FU_DBNAME} ${FU_DBUSER} ${FU_HOST}/32 trust" /var/lib/pgsql/data/pg_hba.conf`
#	if [ "x$LOCAL" == "x" -o "x$HOST" == "x" ];then
#		sed -i -e "s/^\(local.*\)$/local ${FU_DBNAME} ${FU_DBUSER} trust\n\1/" /var/lib/pgsql/data/pg_hba.conf
#		sed -i -e "s/^\(host.*\)$/host ${FU_DBNAME} ${FU_DBUSER} ${FU_HOST}\/32 trust\n\1/" /var/lib/pgsql/data/pg_hba.conf
	#	echo "Redémarrage de PostgreSQL pour prise en compte des nouveaux droits"
	#	service postgresql restart
	#	while [ ! -S /tmp/.s.PGSQL.5432 ];do
	#		echo "Attente de PostgreSQL"
	#		sleep 2
	#	done
	#fi

	echo "Execution du script $BINOBM/obm-funambol-storage"
	$BINOBM/obm-funambol-storage
fi

if [ -x $BINOBM/obm-sync ] && [ -s $BINOBM/obm-sync ] ;then
	echo "Execution du script $BINOBM/obm-sync"
	$BINOBM/obm-sync
fi

if [ -x $BINOBM/obm-autoconf ] && [ -s $BINOBM/obm-autoconf ] ;then
	echo "Execution du script $BINOBM/obm-autoconf"
	$BINOBM/obm-autoconf
fi

if [ -x $BINOBM/obm-cyrus ] && [ -s $BINOBM/obm-cyrus  ] ;then
	$BINOBM/obm-cyrus
fi

if [ -x $BINOBM/obm-postfix ] && [ -s $BINOBM/obm-postfix  ] ;then
	$BINOBM/obm-postfix
fi

if [ -x $BINOBM/obm-satellite ] && [ -s $BINOBM/obm-satellite  ] ;then
	$BINOBM/obm-satellite
fi

if [ -x $BINOBM/minig-conf ] && [ -s $BINOBM/minig-conf  ] ;then
	$BINOBM/minig-conf
fi

if [ -x $BINOBM/minig-storage ] && [ -s $BINOBM/minig-storage  ] ;then
	$BINOBM/minig-storage
fi

if [ -x $BINOBM/minig-backend ] && [ -s $BINOBM/minig-backend  ] ;then
	$BINOBM/minig-backend
fi

if [ -x $BINOBM/minig ] && [ -s $BINOBM/minig  ] ;then
	$BINOBM/minig
fi

if [ -x $BINOBM/obm-solr ] && [ -s $BINOBM/obm-solr  ] ;then
	$BINOBM/obm-solr
fi
