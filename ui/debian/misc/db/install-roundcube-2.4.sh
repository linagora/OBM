#!/bin/bash

set -e

ROUNDCUBE_PATH_INSTALL="/usr/share/obm-storage/update-install/scripts/creation"

cd ${ROUNDCUBE_PATH_INSTALL}
bash install_roundcubedb_2.4.sh


exit 0
