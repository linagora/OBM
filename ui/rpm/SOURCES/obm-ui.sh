#!/bin/sh
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

REP_ETC_OBM="/etc/obm"
FIC_RPM_CONF="${REP_ETC_OBM}/obm-rpm.conf"
FIC_HTTPD_OBM="/etc/httpd/conf.d/obm.conf"
FIC_CONF_OBM="${REP_ETC_OBM}/obm_conf.ini"
FIC_CONF_OBM_INC="${REP_ETC_OBM}/obm_conf.inc"

echo "================= OBM UI  configuration =================="
echo 

# Test de l'existence du fichier de conf obm-rpm.conf
if [ -s $FIC_RPM_CONF ]; then
        source $FIC_RPM_CONF
else
        echo "$0 (Err): the file $FIC_RPM_CONF doesn't exist or is empty"
        exit 1
fi

echo -e "Choose the type of authentication: (database/ldap) [database] \c "
read authentication
if [ "x${authentication}" == "x" ]; then
	authentication="database"
elif [ "${authentication}" == "ldap" ]; then
	while [ "x${ldap_server}" == "x" ]; do
		echo -e "Enter the host name or IP of your ldap server: \c "
		read ldap_server
	done	
	echo -e "Enter the suffix ldap: [dc=local] \c "
	read ldap_suffix
	if [ "x${ldap_suffix}" == "x" ]; then
		ldap_suffix="dc=local"
	fi
	echo -e "Enter the filter ldap: [(&(uid=%u)(obmDomain=%d))] \c "
	read ldap_filter
	if [ "x${ldap_filter}" == "x" ]; then
		ldap_filter="(&(uid=%u)(obmDomain=%d))"
	fi
fi

# Modify obm_conf.ini for auth ldap
if [ ${authentication} == "ldap" ];then
	if [ ! -e "$FIC_CONF_OBM.old" ]; then
	cp $FIC_CONF_OBM $FIC_CONF_OBM.old
	fi
	sed -i -e "s|^;auth-ldap-server.*|auth-ldap-server = ldap://${ldap_server}|" $FIC_CONF_OBM
	sed -i -e "s/^;auth-ldap-basedn.*/auth-ldap-basedn = \"${ldap_suffix}\"/" $FIC_CONF_OBM
	sed -i -e "s/; auth-ldap-filter.*/auth-ldap-filter = \"${ldap_filter}\"/" $FIC_CONF_OBM
	sed -i -e "s|^//\$auth_kind='ldap';.*$|\$auth_kind='ldap'; |" $FIC_CONF_OBM_INC
	
fi

if [ -e $FIC_HTTPD_OBM ]; then
	echo "The $FIC_HTTPD_OBM file already exists"
	echo -e "Do you want to replace it? (y)es,(n)o ?\c"
	read replace_httpd_conf

	if [ "x$replace_httpd_conf" == "xy" ]; then
		cp $FIC_HTTPD_OBM ${FIC_HTTPD_OBM}.old

    sed -i -e "s%.*ServerName.*%ServerName ${EXTERNALURL}%" ${FIC_HTTPD_OBM}
    sed -i -e "s%.*DocumentRoot.*%DocumentRoot /usr/share/obm/php%" ${FIC_HTTPD_OBM} 
    sed -i -e "s%.*ErrorLog.*%ErrorLog /var/log/httpd/obm-error.log%" ${FIC_HTTPD_OBM} 
    sed -i -e "s%.*CustomLog.*%CustomLog /var/log/httpd/obm-access.log common%" ${FIC_HTTPD_OBM} 
    sed -i -e "s%.*Alias.*/images.*%Alias /images /usr/share/obm/resources%" ${FIC_HTTPD_OBM} 
    sed -i -e "s%.*include_path.*%php_value include_path  \".:/usr/share/obm\"%" ${FIC_HTTPD_OBM} 

    echo "Activation of the Provisioning proxy"
    echo -e "what is the IP adress of the obm-provisioning server ?\c"
    read provisioning_server
    sed -i -e "s%#provisioning#%%" ${FIC_HTTPD_OBM}
    sed -i -e "s%_PROVISIONING_SERVER_%${provisioning_server}%" ${FIC_HTTPD_OBM}

    echo "Activation of the Tomcat proxy"
    echo -e "what is the IP adress of the OBM-TOMCAT server (obm-sync, funambol) ?\c"
    read tomcat_server
    sed -i -e "s%#obm#%%" ${FIC_HTTPD_OBM}
    sed -i -e "s%_TOMCAT_SERVER_%${tomcat_server}%" ${FIC_HTTPD_OBM}

    echo "Activation of the opush proxy"
    echo -e "what is the IP adress of the OPUSH server ?\c"
    read opush_server
    sed -i -e "s%#opush#%%" ${FIC_HTTPD_OBM}
    sed -i -e "s%_OPUSH_SERVER_%${opush_server}%" ${FIC_HTTPD_OBM}

		service httpd restart
	fi
else
	echo "$0 (Err): the file $FIC_HTTPD_OBM doesn't exist."
	exit 1
fi

echo
echo -e "================= End of OBM UI configuration ==================\n"
