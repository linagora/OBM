#!/bin/bash
# Copyright (C) 2011-2012 Linagora
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


echo -e "================= OBM LDAP configuration ==================\n"

slapd_doc=`rpm -qa |grep obm-ldap | cut -d"-" -f1-3`
OBMCONF="/etc/obm/obm-rpm.conf"
REP_ETC_LDAP="/etc/openldap"
REP_DOC="/usr/share/doc"
REP_LIB_LDAP="/var/lib/ldap"
CMD_INIT_LDAP="/etc/init.d/ldap"
PID_FILE_LDAP="/var/run/openldap/slapd.pid"

if [ -e ${OBMCONF} ]; then
	source ${OBMCONF}
fi

export PGPASSWORD="${OBM_PASSWD}";

if [ -f ${PID_FILE_LDAP} ];then
	${CMD_INIT_LDAP} stop
fi

if [ -e ${REP_ETC_LDAP}/slapd.conf ]; then
	echo -e "The slapd.conf fil alradey exist\n"
	echo -e "Do you want replace it? (y)es, (n)o [n] \c "
	read replace_slapd_conf
	if [ "x$replace_slapd_conf" == "xy" ];then
		if [ ! -f "${REP_ETC_LDAP}/slapd.conf.orig" ]; then
			cp "${REP_ETC_LDAP}/slapd.conf" "${REP_ETC_LDAP}/slapd.conf.orig"
		fi
		cp "$REP_DOC/${slapd_doc}/ldap_slapd.conf.sample" "${REP_ETC_LDAP}/slapd.conf"
		sed -i -e "s#_PATH_SCHEMA_#/etc/openldap/schema#" "${REP_ETC_LDAP}/slapd.conf"
		sed -i -e "s#_PATH_PIDFILE_#/var/run/openldap#" "${REP_ETC_LDAP}/slapd.conf"
		sed -i -e "s#_PATH_ARGSFILE_#/var/run/openldap#" "${REP_ETC_LDAP}/slapd.conf"
		sed -i -e "s|_COMMENT_|#|" "${REP_ETC_LDAP}/slapd.conf"

		# Configuration ldap pour samba
		if [ ${SAMBA_MODULE} == 'true' ]; then
			# On comment les indexes sans samba
			sed -i -e "s/^\(index.*cn,sn,uid.*pres,sub,eq\)$/#\1/" \
						${REP_ETC_LDAP}/slapd.conf
			sed -i -e "s/^\(index.*objectClass.*eq\)$/#\1/" \
						${REP_ETC_LDAP}/slapd.conf
			# On active les index avec Samba
			sed -i -e "s/^#\(index.*cn,sn,uid,displayName.*pres,sub,eq\)$/\1/" \
						${REP_ETC_LDAP}/slapd.conf
			sed -i -e "s/^#\(index.*sambaSID.*eq\)$/\1/" \
						${REP_ETC_LDAP}/slapd.conf
			sed -i -e "s/^#\(index.*sambaPrimaryGroupSID.*eq\)$/\1/" \
						${REP_ETC_LDAP}/slapd.conf
			sed -i -e "s/^#\(index.*sambaDomainName.*eq\)$/\1/" \
						${REP_ETC_LDAP}/slapd.conf
			sed -i -e "s/^#\(index.*objectClass.*pres,eq\)$/\1/" \
						${REP_ETC_LDAP}/slapd.conf
			# On commente le bloc des ACLs sans SAMBA
			sed -i -e '/BEGIN without SAMBA ACLs/ , /END without SAMBA ACLs/ s/^/#/' ${REP_ETC_LDAP}/slapd.conf
			# On decommente le bloc d'ACL avec SAMBA
			sed -i -e '/BEGIN with SAMBA ACLs/ , /END with SAMBA ACLs/ s/^#\(.*\)$/\1/' ${REP_ETC_LDAP}/slapd.conf
			# On supprime le dc=<domain> dans les acls samba
			sed -i -e "s/\,dc=<domain>//" ${REP_ETC_LDAP}/slapd.conf

		fi

		# Activation pour synrepl
		echo -e "o Do you want active syncrepl for this LDAP? (y)es, (n)o: [n] \c "
		read active_syncrepl
		if [ "x${active_syncrepl}" == "xy" ]; then
			# Activation du module suncprov
			#sed -i -e "s/^#\(moduleload.*syncprov\)$/\1/" \
			#			${REP_ETC_LDAP}/slapd.conf
			sed -i -e "s/^#\(index.*entryCSN,entryUUID.*eq\)$/\1/" \
						${REP_ETC_LDAP}/slapd.conf
			sed -i -e "s/^#\(overlay.*syncprov\)$/\1/" \
						${REP_ETC_LDAP}/slapd.conf
			sed -i -e "s/^#\(syncprov-checkpoint.*100.*10\)$/\1/" \
						${REP_ETC_LDAP}/slapd.conf
			sed -i -e "s/^#\(syncprov-sessionlog.*100\)$/\1/" \
						${REP_ETC_LDAP}/slapd.conf
			echo -e "o Enter the dn use to syncrepl process (for ACLs) [uid=syncrepl,ou=sysusers,dc=local] \c "
			read sync_user
			if [ "x$sync_user" = "x" ] ;then
				sync_user="uid=syncrepl,ou=sysusers,dc=local"
			fi
			sed -i -e "/^access to filter=(hiddenUser=TRUE)/ a\    by dn=\"${sync_user}\" read" ${REP_ETC_LDAP}/slapd.conf
			sed -i -e "/^access to attrs=userPassword,shadowLastChange/ a\    by dn=\"${sync_user}\" read" ${REP_ETC_LDAP}/slapd.conf
			sed -i -e "/^access to attrs=sambaLMPassword,sambaNTPassword,sambaPwdLastSet,sambaPwdMustChange,sambaPwdCanChange,sambaPasswordHistory/ a\    by dn=\"${sync_user}\" read" ${REP_ETC_LDAP}/slapd.conf
			
		fi
		# Mise à jour du mdp rootdn
		if [ ${OBM_DBTYPE} == "MYSQL" ]; then
			MDP_BD=`mysql -u obm -pobm obm -NB -e \
				"SELECT usersystem_password 
				FROM UserSystem 
				WHERE usersystem_login = 'ldapadmin'"`

		elif [ ${OBM_DBTYPE} == "PGSQL" ]; then
			MDP_BD=`psql -t -U ${OBM_DBUSER} ${OBM_DBNAME} -c \
				"SELECT usersystem_password 
				FROM usersystem 
				WHERE usersystem_login = 'ldapadmin'"`
		fi 
		MDP_LDAP=`slappasswd -h {SSHA} -s ${MDP_BD}`
		sed -i -e "s~^rootpw.*$~rootpw ${MDP_LDAP}~" ${REP_ETC_LDAP}/slapd.conf
		# Activation SSL/TLS 
		echo -e "o Do you want active SSL/TLS in LDAP ? (y)es,(n)o [n] \c "
		read active_ssl
		if [ "x$active_ssl" = "xy" ]; then
			sed -i -e '/## BEGIN TLS/ , /## End TLS/ s/^#\(.*\)$/\1/' ${REP_ETC_LDAP}/slapd.conf	
			echo -e "o Do you want force use of ssl/tls ? (y)es,n()o [n] \c "
			read force_tls
			if [ "x$force_tls" = "xy" ] ;then
				sed -i -e "/# BEGIN TLS/ a\security tls=1"  ${REP_ETC_LDAP}/slapd.conf
				grep TLS_REQCERT ${REP_ETC_LDAP}/ldap.conf 1>/dev/null 2>&1
				if [ $? -eq 1 ] ;then
					echo "TLS_REQCERT  allow" >> ${REP_ETC_LDAP}/ldap.conf
				fi
			fi
		fi
	fi
fi

cp -r ${REP_LIB_LDAP} ${REP_LIB_LDAP}.backup 
rm -rf ${REP_LIB_LDAP}/*

# Configuration syslog pour ldap
SYSLOG=`grep -c local4.* /etc/syslog.conf`
if [ ${SYSLOG} -ne 1 ]; then
	echo "# config ldap log (modified by obm-ldap)" >> /etc/syslog.conf
	echo "local4.*	/var/log/ldap.log" >> /etc/syslog.conf
	/etc/init.d/syslog restart
fi

${CMD_INIT_LDAP} start

unset PGPASSWORD

echo 
echo -e "================= End of OBM LDAP configuration ==================\n"

exit 0
