#!/bin/bash
###############################################################################
# OBM - File : update-2.1-2.2.sh                                              #
#     - Desc : OBM Database 2.2 installation script for obm-storage           #
# 2005-06-08 AliaSource sylvain.garcia@obm.org                                #
###############################################################################



set -e

OBM_PATH_UPDATE="/usr/share/obm-storage/update-install/scripts/2.2"

cd ${OBM_PATH_UPDATE}
/bin/bash upgrade2.1-2.2.sh

exit 0


