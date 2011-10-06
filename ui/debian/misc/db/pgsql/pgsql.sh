#/bin/bash
#installation script
set -x
set -e

cd /usr/share/obm-storage/update-install/scripts/2.4
bash ./install_obmdb_2.4.sh filldata

exit 0

