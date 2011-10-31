#! /bin/sh
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
sed -i -e "s|^external-url = obm|external-url = $EXTERNALURL|" ${NEWFILE}
sed -i -e "s|^external-protocol = http|external-protocol = $EXTERNALPROTOCOL|" ${NEWFILE}
sed -i -e "s|^ldapServer = ldap://localhost|ldapServer = ldap://$OBM_LDAPSERVER|" ${NEWFILE}
sed -i -e "s|^obm-ldap =.*|obm-ldap = $LDAP_MODULE|" ${NEWFILE}
sed -i -e "s|^obm-mail =.*|obm-mail = $MAIL_MODULE|" ${NEWFILE}
sed -i -e "s|^obm-samba =.*|obm-samba = $SAMBA_MODULE|" ${NEWFILE}
sed -i -e "s|^obm-web =.*|obm-web = $WEB_MODULE|" ${NEWFILE}

echo -e "======================= End of file =======================\n"
