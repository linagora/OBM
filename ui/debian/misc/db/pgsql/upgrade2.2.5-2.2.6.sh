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

export PGPASSWORD=$P

OBM_PATH_UPDATE="/usr/share/obm-storage/update-install/scripts/2.2/update22/scripts/2.2/updates"

psql -U ${U} -h localhost ${DB} -f ${OBM_PATH_UPDATE}/update-2.2.5-2.2.6.pgsql.sql || true

exit 0
