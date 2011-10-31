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

HORT_NAME=`/bin/hostname`
FULL_NAME=`/bin/hostname -f`
OBM_EXTERNALURL=`cat /etc/obm/obm_conf.ini | grep ^external-url | cut -d" " -f 3`

function gencertif ()
{
	/usr/share/obm-ca/createcert.sh ${OBM_EXTERNALURL}
	rm -f /etc/obm/certs/obm_cert.pem
	ln -s /etc/obm/certs/${OBM_EXTERNALURL}_signed.pem /etc/obm/certs/obm_cert.pem

}

echo "Generate Certificate for this host (${OBM_EXTERNALURL})"
gencertif

echo "============ End of OBM cert configuration  ============"
echo
