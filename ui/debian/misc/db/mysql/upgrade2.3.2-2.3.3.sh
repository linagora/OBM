#!/bin/bash

set -e

#returns a value from obm_conf.ini and store it into $VALUE
function get_val() {
    VALUE=`grep ^$1\ *= /etc/obm/obm_conf.ini | cut -d= -f2 | tr -d '^ ' | tr -d '" '`
  }

get_val user
U=$VALUE
get_val password
P=$VALUE
get_val db
DB=$VALUE


OBM_PATH_UPDATE="/usr/share/obm-storage/update-install/scripts/2.3/updates"

mysql -u $U -p$P  --default-character-set='UTF8' ${DB} < ${OBM_PATH_UPDATE}/update-2.3.0-2.3.1.mysql.sql || true
mysql -u $U -p$P  --default-character-set='UTF8' ${DB} < ${OBM_PATH_UPDATE}/update-2.3.1-2.3.2.mysql.sql || true
mysql -u $U -p$P  --default-character-set='UTF8' ${DB} < ${OBM_PATH_UPDATE}/update-2.3.2-2.3.3.mysql.sql || true

exit 0
