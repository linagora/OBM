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


# Script de configuration de cryus.
# Il s'agit du remplacement des fichiers de conf par defaut par ceux du repertoire
# /usr/share/doc/obm-cyrus???
echo -e "================= OBM Cyrus configuration ==================\n"
REP_ETC_OBM="/etc/obm"
FIC_CYRUS="/etc/cyrus.conf"
FIC_IMAP="/etc/imapd.conf"
FIC_RPM_CONF="$REP_ETC_OBM/obm-rpm.conf"
FIC_PID_CYRUS="/var/run/cyrus-master.pid"
FIC_SASL_CONF="/etc/saslauthd.conf"
FIC_SASL_SYSCONFIG="/etc/sysconfig/saslauthd"
FIC_SASL_PID="/var/run/saslauthd/saslauthd.pid"
REP_DOC="/usr/share/doc"
DOC_DIR=`rpm -qa |grep obm-cyrus | cut -d"-" -f1-3`

# Test de l'existance du fichier de conf obm-rpm.conf
if [ -s $FIC_RPM_CONF ]; then
	source $FIC_RPM_CONF
else
	echo "$0 (Err):Le fichier FIC_RPM_CONF n'exite pas ou est vide"
	echo "La configuration de saslauthd.conf ne peut pas être exacte"
fi

# Trop de modif pour remplacer ce fichier par celui de debian
# On active quand même le lmtp sans authentification
# On desactive la gestion de imaps et pop3s
sed -i -e "s/^#.*\(.*lmtp\)\(.*\)cmd=\"lmtpd\"\(.*\)/\1\2cmd=\"lmtpd -a\"\3/" \
	-e "s/\(imaps.*cmd=\"imapd -s\".*\)/#\1/" \
	-e "s/\(pop3s.*cmd=\"pop3d -s\".*\)/#\1/" $FIC_CYRUS

#if [ -e $FIC_CYRUS ] ; then
#	mv $FIC_CYRUS ${FIC_CYRUS}.old
#	cp $REP_DOC/$DOC_DIR/cyrus_cyrus.conf.sample $FIC_CYRUS
#fi

if [ -e $FIC_IMAP ] ; then
	echo -e "o Do you want replace configuration ? (y)es,(n)o \c "
	read replace_conf
	if [ "x$replace_conf" == "xy" ]; then
		# Copie et remplacement de imapd.conf
        	mv $FIC_IMAP ${FIC_IMAP}.old
		cp $REP_DOC/$DOC_DIR/cyrus_imapd.conf.sample $FIC_IMAP
		# Copie et remplacement de saslauthd.conf
		if [ -e $FIC_SASL_CONF ]; then
			mv $FIC_SASL_CONF ${FIC_SASL_CONF}.old
		fi
		cp $REP_DOC/$DOC_DIR/cyrus_saslauthd.conf.sample $FIC_SASL_CONF
		# Copie et remplacement du fichier sysconfig d'SASL
		cp $FIC_SASL_SYSCONFIG ${FIC_SASL_SYSCONFIG}.old
		# Modification du fichier imap

		sed -i -e "s#configdirectory: /var/lib/cyrus#configdirectory: /var/lib/imap#" \
			-e "s#partition-default: /var/spool/cyrus/mail#partition-default: /var/spool/imap#" \
			-e "s|partition-news: /var/spool/cyrus/news|#partition-news: /var/spool/cyrus/news|" \
			-e "s|newsspool: /var/spool/news|#newsspool: /var/spool/news|" \
			-e "s#^sievedir:.*#sievedir: /var/lib/imap/sieve#" \
			$FIC_IMAP
		# Modification du fichier SASL
		sed -i -e "s/_LDAP_SERVER_/$OBM_LDAPSERVER/" $FIC_SASL_CONF
		# Modification du fichier sysconfig d'SASL
		sed -i -e "s/^MECH.*/MECH=ldap/" $FIC_SASL_SYSCONFIG
	fi
else
        echo "Une Erreur est survenu lors de l'installation de cyrus"
fi

if [ ! -s $FIC_PID_CYRUS ]; then
	/etc/init.d/cyrus-imapd start
else
	/etc/init.d/cyrus-imapd restart
fi

if [ ! -s $FIC_SASL_PID ]; then
	/etc/init.d/saslauthd start
else
	/etc/init.d/saslauthd restart
fi

echo
echo -e "================= End of OBM Cyrus configuration ==================\n"
