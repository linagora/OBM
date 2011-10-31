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
OBMCONF="$REP_ETC_OBM/obm-rpm.conf"
OBM_INC="$REP_ETC_OBM/obm_conf.inc"

# check if the file exist
echo "=============== OBM-CORE main configuration ================"

. $OBMCONF
# DB
echo "Set doucment_root (OBM) setting"
sed -i -e 's#^\$cdocument_root.*#\$cdocument_root = "/var/lib/obm/documents";#' ${OBM_INC}
echo "Set default_pat"
sed -i -e "s#^\$default_path.*#\$default_path = '/';#" ${OBM_INC}

echo 
echo "=============== OBM-CORE DONE ==============="
