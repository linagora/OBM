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

cd ${OBM_PATH_UPDATE}
php update-2.3.7-2.3.8.post.php


exit 0
