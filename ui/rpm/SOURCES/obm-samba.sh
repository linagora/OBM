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


FIC_PDC=`rpm -qd obm-Samba | grep smb_pdc.conf.sample`
FIC_BDC=`rpm -qd obm-Samba | grep smb_bdc.conf.sample`
FIC_NSSWITCH="/etc/nsswitch.conf"
FIC_LDAP="/etc/ldap.conf"
FIC_SMB="/etc/samba/smb.conf"

echo -e "================= OBM Samba configuration ==================\n"
echo -e "o Please enter the name of your domain: \c "
read smb_domain_name
echo -e "o Please enter the SID of your domain: \c "
read smb_domain_sid
echo -e "o Please enter the suffix of your ldap: \c "
read ldap_suffix
echo -e "o Please enter LDAP server name (ldap://ldap1 ldap://ldap2) \c "
read ldap_srv
echo -e "o Please enter role of samba server (pdc/bdc) \c "
read smb_role


# write file nsswitch.conf
cp $FIC_NSSWITCH $FIC_NSSWITCH.orig
sed -i -e "s/^passwd:.*/passwd: files ldap/" $FIC_NSSWITCH
sed -i -e "s/^group:.*/group: files ldap/" $FIC_NSSWITCH
sed -i -e "s/^shadow:.*/shadow: files ldap/" $FIC_NSSWITCH

# write file ldap.conf
cp $FIC_LDAP $FIC_LDAP.orig
sed -i -e "s/^base.*$/base dc=$smb_domain_name,dc=$ldap_suffix/" $FIC_LDAP
sed -i -e "s/^#uri.*$/uri $ldap_srv/" $FIC_LDAP
sed -i -e "s/^#ldap_version.*$/ldap_version 3/" $FIC_LDAP
sed -i -e "s/^#nss_base_passwd.*$/nss_base_passwd         ou=users,dc=$smb_domain_name,dc=$ldap_suffix?one/" $FIC_LDAP
sed -i -e "s/^#nss_base_passwd.*$/nss_base_passwd         ou=hosts,dc=$smb_domain_name,dc=$ldap_suffix?one/" $FIC_LDAP
sed -i -e "s/^#nss_base_shadow.*$/nss_base_shadow         ou=users,dc=$smb_domain_name,dc=$ldap_suffix?one/" $FIC_LDAP
sed -i -e "s/^#nss_base_group.*$/nss_base_group          ou=groups,dc=$smb_domain_name,dc=$ldap_suffix?one/" $FIC_LDAP

# Write smb.conf for your config
if [ $smb_role == "pdc" ];then
	cp $FIC_PDC $FIC_SMB
elif [ $smb_role == "bdc" ];then
	cp $FIC_BDC $FIC_SMB
else
	echo "An error occur on role of samba server"
	exit 1
fi

