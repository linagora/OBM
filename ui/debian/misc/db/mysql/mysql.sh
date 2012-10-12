#/bin/bash
#installation script
set -e

cd /usr/share/obm-storage/update-install/scripts/creation
bash ./install_obmdb.sh filldata
bash ./install_roundcubedb_2.4.sh

exit 0
