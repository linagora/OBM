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
REP_ETC_OBM_SATELITE="/etc/obm-satellite"
REP_RUN="/var/run"
obmconf="${REP_ETC_OBM}/obm-rpm.conf"
NEWFILE="${REP_ETC_OBM}/obm_conf.ini"
CMD_INIT_OBM_SATELITE="/etc/init.d/obmSatellite"

# check if the file exist

if [ -s "${REP_RUN}/obm/obmSatellite.pid" ]; then
	$CMD_INIT_OBM_SATELITE restart
else
	$CMD_INIT_OBM_SATELITE start
fi

echo
echo -e "======================= End Config obm-satellite =======================\n"
