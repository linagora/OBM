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
FIC_MAIN="/etc/postfix/main.cf"
FIC_RPM_CONF="${REP_ETC_OBM}/obm-rpm.conf"
REP_DOC="/usr/share/doc"
DOC_DIR=`rpm -qa |grep obm-postfix | cut -d"-" -f1-3`

echo -e "================= OBM postfix configuration ==================\n"

# Test de l'existance du fichier de conf obm-rpm.conf
if [ -s $FIC_RPM_CONF ]; then
        source $FIC_RPM_CONF
else
        echo "$0 (Err):Le fichier $FIC_RPM_CONF n'exite pas ou est vide"
        echo "La configuration de Postfix ne peut pas être exacte"
fi

if [ -e $FIC_MAIN ]; then
  echo "file $FIC_MAIN already exist"
        echo -e "do you want replace this file? (y)es,(n)o ? [n] \c"
	read replace_postfix_conf
	if [ "x$replace_postfix_conf" == "xy" ]; then
		mv $FIC_MAIN ${FIC_MAIN}.old
		cp $REP_DOC/$DOC_DIR/postfix_main.cf_-_SMTPin.2.3.x.sample /etc/postfix/main.cf
		echo -e "o Please enter your relay host if you have it\n"
		read RELAYHOST

		# Modification du fichier de configuration
		MYHOSTNAME=`hostname -f` > /dev/null 2>&1
		if [ $? -ne 0 ]; then
			MYHOSTNAME=`hostname` > /dev/null 2>&1
		fi
		sed -i -e "s#^myhostname.*#myhostname = ${MYHOSTNAME}#" $FIC_MAIN
		if [ "x${RELAYHOST}" == "x" ]; then
			sed -i -e "s/^relayhost.*/#relayhost/" $FIC_MAIN
			else
			sed -i -e "s/^relayhost.*/relayhost = ${RELAYHOST}/" $FIC_MAIN
		fi
		echo -e "o Please enter your 'mynetwork' (default empty)"
		read mynetwork_postfix_conf
		if [ -z ${mynetwork_postfix_conf} ] ; then
		  /usr/sbin/postconf -e mynetworks=''
		else
		  /usr/sbin/postconf -e mynetworks="${mynetwork_postfix_conf}"
		fi
	fi
        echo "create initital postfix maps"
        touch /etc/postfix/virtual_mailbox && chown root:root /etc/postfix/virtual_mailbox && chmod 664 /etc/postfix/virtual_mailbox
        touch /etc/postfix/virtual_alias && chown root:root /etc/postfix/virtual_alias && chmod 664 /etc/postfix/virtual_alias
        touch /etc/postfix/transport && chown root:root /etc/postfix/transport && chmod 664 /etc/postfix/transport
        touch /etc/postfix/virtual_domains && chown root:root /etc/postfix/virtual_domains && chmod 664 /etc/postfix/virtual_domains

else
	echo "Une erreur est survenu lors de l'installation de postfix"
	echo "car le fichier $FIC_MAIN n'existe pas"
fi

echo
echo -e "================= End of OBM postfix configuration ==================\n"
