#! /bin/sh
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
obmconf="${REP_ETC_OBM}/obm-rpm.conf"
NEWFILE="${REP_ETC_OBM}/obm_conf.ini"

# check if the file exist
if [ -f $obmconf ] ; then
	mv -f $obmconf $obmconf.old
fi
echo -e "================= OBM main configuration ==================\n"
echo -e "o Please enter external url (IP): \c"
read value
echo "EXTERNALURL=$value" >> $obmconf
# Always in https now
#echo -e "o Please enter external protocol (http/https): \c"
#read value
echo "EXTERNALPROTOCOL=https" >> $obmconf
echo -e "o Plese enter LDAP server name : \c"
read value
echo "OBM_LDAPSERVER=$value" >> $obmconf

# Correctly set ldap url observer in configuration file
opushconf="/etc/opush/ldap_conf.ini"
if [ -f $opushconf ] ; then
	cp -f $opushconf $opushconf.old
	perl -i -pe"s@search.ldap.url=.*\$@search.ldap.url=ldap://$value@" $opushconf
fi


# Modules stuff
for modules in LDAP MAIL SAMBA WEB ; do
	echo -e "o Enable module $modules, (y)es (n)o : \c"
	read value
		if [ "x$value" = "xy" -o "x$value" = "yes" ] ; then
			echo "${modules}_MODULE=true" >> $obmconf
		else
			if [ "x$value" = "xn" -o "x$value" = "no" ] ; then
				echo "${modules}_MODULE=false" >> $obmconf
			fi
		fi
done
              
echo -e "\n"
echo -e "=============== OBM DataBase configuration ================\n"
echo -e "o Please enter the DataBase hostname: \c"
read value
echo "OBM_HOST=$value" >> $obmconf

echo -e "o Please enter the DataBase type (MYSQL/PGSQL): \c"
read value
if [ "x$value" == "x" ];then
	echo "OBM_DBTYPE=MYSQL" >> $obmconf
fi
if [ "$value" != "MYSQL" -o "$value" != "PGSQL" ];then
	value=`echo $value | tr '[:lower:]' '[:upper:]'`
	echo "OBM_DBTYPE=$value" >> $obmconf
else
	echo "OBM_DBTYPE=$value" >> $obmconf
fi

echo -e "o Enter the DataBase name: \c"
read value
echo "OBM_DBNAME=$value" >> $obmconf

echo -e "o Enter the DataBase user: \c"
read value
echo "OBM_DBUSER=$value" >> $obmconf
echo -e "o Enter the DataBase user password: \c"
stty -echo
read value
stty echo
   OBM_PASSWD=$value
echo -e "\n"
echo "OBM_PASSWD=$value" >> $obmconf

. $obmconf
# DB
sed -i -e "s|^host = .*|host = ${OBM_HOST}|" ${NEWFILE}
sed -i -e "s|^db = .*|db = ${OBM_DBNAME}|"  ${NEWFILE}
sed -i -e "s|^dbtype = .*|dbtype = ${OBM_DBTYPE}|"  ${NEWFILE}
sed -i -e "s|^user = .*|user = ${OBM_DBUSER}|"  ${NEWFILE}
sed -i -e "s|^password = .*|password = \"${OBM_PASSWD}\"|" ${NEWFILE}

# main
sed -i -e "s|^external-url =.*|external-url = $EXTERNALURL|" ${NEWFILE}
sed -i -e "s|^external-protocol =.*|external-protocol = $EXTERNALPROTOCOL|" ${NEWFILE}
sed -i -e "s|^ldapServer =.*|ldapServer = ldap://$OBM_LDAPSERVER|" ${NEWFILE}
sed -i -e "s|^obm-ldap =.*|obm-ldap = $LDAP_MODULE|" ${NEWFILE}
sed -i -e "s|^obm-mail =.*|obm-mail = $MAIL_MODULE|" ${NEWFILE}
sed -i -e "s|^obm-samba =.*|obm-samba = $SAMBA_MODULE|" ${NEWFILE}
sed -i -e "s|^obm-web =.*|obm-web = $WEB_MODULE|" ${NEWFILE}

echo -e "======================= End of file =======================\n"
