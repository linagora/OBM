#!/bin/sh
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
FIC_RPM_CONF="${REP_ETC_OBM}/obm-rpm.conf"

echo -e "================= OBM system user configuration ==================\n"

# Test de l'existance du fichier de conf obm-rpm.conf
if [ -s $FIC_RPM_CONF ]; then
        source $FIC_RPM_CONF
else
        echo "$0 (Err):Le fichier $FIC_RPM_CONF n'exite pas ou est vide"
        echo "La configuration des utilisateurs systèmes est annulé"
	exit 1
fi


# Demande du mot de passe pour le compte ldapadmin
echo -e "o Enter root ldap password [mdp3PaAl]\n"
stty -echo
read rootpw
stty echo
if [ "x${rootpw}" == "x" ];then
	rootpw='mdp3PaAL'
fi

# Demande du mot de passe pour le compte cyrus
echo -e "o Enter admin cyrus password [cyrus]\n"
stty -echo
read cyruspw
stty echo
if [ "x${cyruspw}" == "x" ];then
	cyruspw='cyrus'
fi

if [ "${SAMBA_MODULE}" == "true" ]; then
	echo -e "o Enter password for samba user [m#Pa!NtA]\n"
	stty -echo
	read sambapw
	stty echo
	if [ "x${sambapw}" == "x" ]; then
		sambapw='m#Pa!NtA'
	fi
fi

echo -e "o Do you want add syncrepl user? (y)es,(n)o [n]\n"
read add_synrepl
if [ "x${add_synrepl}" == "xy" ]; then
	echo "o Enter password for syncrepl user [MoSync#]"
	stty -echo
	read synreplpw
	stty echo
	if [ "x${synreplpw}" == "x" ]; then
		synreplpw="MoSync#"
	fi
fi


# Détection du tpye de base de données

# definition des requetes PGSQL
PG_SYNCREPL="INSERT into usersystem (usersystem_login,usersystem_password,usersystem_uid,usersystem_gid,usersystem_homedir,usersystem_lastname,usersystem_firstname,usersystem_shell) VALUES ('syncrepl','${synreplpw}','102','65534','/','LDAP Syncrepl user','LDAP Syncrepl user','/sbin/nologin');"
PG_LDAPADMIN_PW="UPDATE usersystem SET usersystem_password = '${rootpw}' WHERE usersystem_login = 'ldapadmin';"
PG_SAMBA_PW="UPDATE usersystem SET usersystem_password = '${sambapw}' WHERE usersystem_login = 'samba';"
PG_CYRUS_PW="UPDATE usersystem SET usersystem_password = '${cyruspw}' WHERE usersystem_login = 'cyrus';"

MY_SYNCREPL="INSERT into UserSystem (usersystem_login,usersystem_password,usersystem_uid,usersystem_gid,usersystem_homedir,usersystem_lastname,usersystem_firstname,usersystem_shell) VALUES ('syncrepl','${synreplpw}','102','65534','/','LDAP Syncrepl user','LDAP Syncrepl user','/sbin/nologin');"
MY_LDAPADMIN_PW="UPDATE UserSystem SET usersystem_password = '${rootpw}' WHERE usersystem_login = 'ldapadmin';"
MY_SAMBA_PW="UPDATE UserSystem SET usersystem_password = '${sambapw}' WHERE usersystem_login = 'samba';"
MY_CYRUS_PW="UPDATE UserSystem SET usersystem_password = '${cyruspw}' WHERE usersystem_login = 'cyrus';"

echo "Insertion des utilisateurs en BD"
if [ "${OBM_DBTYPE}" == "PGSQL" ]; then
export PGPASSWORD="${OBM_PASSWD}";
# On recharge les index pour les sequences postgres
#psql -U ${OBM_DBUSER} ${OBM_DBNAME} -c "VACUUM;"
# ci-dessus ne fonctionne pas donc:
export PGPASSWORD=${OBM_PASSWD}
psql -U ${OBM_DBUSER} -h ${OBM_HOST} ${OBM_DBNAME} -c "SELECT nextval('usersystem_usersystem_id_seq');" 1>/dev/null 2>&1
	if [ "x${add_synrepl}" == "xy" ]; then 
		psql -U ${OBM_DBUSER} -h ${OBM_HOST} ${OBM_DBNAME} -c "${PG_SYNCREPL}" \
			1>/dev/null 2>&1
	fi
	if [ "${sambapw}" != 'm#Pa!NtA' ]; then
		psql -U ${OBM_DBUSER} -h ${OBM_HOST} ${OBM_DBNAME} -c "${PG_SAMBA_PW}" 1>/dev/null 2>&1
	fi	
	if [ "${rootpw}" != "mdp3PaAL" ]; then
		psql -U ${OBM_DBUSER} -h ${OBM_HOST} ${OBM_DBNAME} \
			-c "${PG_LDAPADMIN_PW}" 1>/dev/null 2>&1
	fi
	if [ "${cyruspw}" != "cyrus" ]; then
		psql -U ${OBM_DBUSER} -h ${OBM_HOST} ${OBM_DBNAME} \
			-c "${PG_CYRUS_PW}" 1>/dev/null 2>&1
	fi

	unset PGPASSWORD

elif [ "${OBM_DBTYPE}" == "MYSQL" ];then
	if [ "x${add_synrepl}" == "xy" ]; then
		mysql -u ${OBM_DBUSER} -h ${OBM_HOST} \
			-p${OBM_PASSWD} ${OBM_DBNAME} -e \
			"${MY_SYNCREPL}" 1>/dev/null 2>&1
	fi
	if [ "${sambapw}" != 'm#Pa!NtA' ]; then
		mysql -u ${OBM_DBUSER} -h ${OBM_HOST} \
			-p${OBM_PASSWD} ${OBM_DBNAME} -e \
			"${MY_SAMBA_PW}" 1>/dev/null 2>&1
		
	fi	
	if [ "${rootpw}" != "mdp3PaAL" ]; then
		mysql -u ${OBM_DBUSER} -h ${OBM_HOST} \
			-p${OBM_PASSWD} ${OBM_DBNAME} -e \
			"${MY_LDAPADMIN_PW}" 1>/dev/null 2>&1
	fi
	if [ "${cyruspw}" != "cyrus" ]; then
		mysql -u ${OBM_DBUSER} -h ${OBM_HOST} \
			-p${OBM_PASSWD} ${OBM_DBNAME} -e \
			"${MY_CYRUS_PW}" 1>/dev/null 2>&1
	fi
	
fi

echo
echo -e "================= End of OBM system user configuration ==================\n"
