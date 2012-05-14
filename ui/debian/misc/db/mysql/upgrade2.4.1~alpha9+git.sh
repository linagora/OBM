#!/bin/sh

set -e

OBM_PATH_UPDATE="/usr/share/obm-storage/update-install/scripts/2.4"

cd ${OBM_PATH_UPDATE}
sh update-2.4.0-2.4.1~alpha9+git.mysql.sh
