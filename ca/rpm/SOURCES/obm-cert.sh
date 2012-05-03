#!/bin/bash
# Copyright (C) 2008 Linagora Group.
#
# Authors:  Ronan Lanore <ronan.lanore@aliasource.fr>
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>


OBMCONF="/etc/obm/obm-rpm.conf"
FIC_HTTPD_CONF="/etc/httpd/conf.d/obm.conf"

echo "=============== OBM cert configuration  ================"
echo

if [ -e ${OBMCONF} ]; then
	source ${OBMCONF}
fi

if [ -e /etc/sysconfig/selinux ] ; then
	source /etc/sysconfig/selinux
fi

HORT_NAME=`/bin/hostname`
FULL_NAME=`/bin/hostname -f`
OBM_EXTERNALURL=`cat /etc/obm/obm_conf.ini | grep ^external-url | cut -d" " -f 3`

function gencertif ()
{
	/usr/share/obm-ca/createcert.sh ${OBM_EXTERNALURL}
	rm -f /etc/obm/certs/obm_cert.pem
	ln -s /etc/obm/certs/${OBM_EXTERNALURL}_signed.pem /etc/obm/certs/obm_cert.pem

}

function checkcertselinux ()
{
	if [ x$SELINUX = x"enforcing" -o x$SELINUX = x"permissive" ] ; then
		echo "$0 (Warn): SELinux support is experimental !"
		if [ -e /usr/sbin/semanage -a -e /sbin/restorecon ] ; then
			HASOBMSECONTEXT=`semanage fcontext -l |grep -e /var/lib/obm-ca/cacert.pem | wc -l`
			if [ x$HASOBMSECONTEXT = x0 ] ; then
				echo "Modifying context for /etc/obm/certs/*.pem and /var/lib/obm-ca/cacert.pem"
			        semanage fcontext -a -t cert_t '/etc/obm/certs(/.*)?'
			        semanage fcontext -a -t cert_t '/var/lib/obm-ca/cacert.pem'
			fi
			[ -f /var/lib/obmca/cacert.pem ] && /sbin/restorecon /var/lib/obmca/cacert.pem
			[ -L /etc/obm/certs/obm_cert.pem ] && /sbin/restorecon /etc/obm/certs/*.pem
			echo "DONE"
		else
			echo "$0 (Err): SELinux enabled but /usr/sbin/semanage or /sbin/restorecon are not available"
			echo "Please disable SELinux or run as root:"
			echo "# yum install policycoreutils-python"
			exit 1
		fi
	fi
}

if [ ! -e /etc/obm/certs/${OBM_EXTERNALURL}_signed.pem ] ; then
	echo "Generate Certificate for this host (${OBM_EXTERNALURL})"
	gencertif
	checkcertselinux
else
	echo "A Certificate already exists for this host (${OBM_EXTERNALURL})"
        echo
fi

echo "============ End of OBM cert configuration  ============"
echo
