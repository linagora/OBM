#!/bin/bash
###############################################################################
# OBM - File : update-2.1-2.2.sh                                              #
#     - Desc : OBM Database 2.2 installation script for obm-storage           #
# 2005-06-08 AliaSource sylvain.garcia@obm.org                                #
###############################################################################



set -e

OBM_PATH_UPDATE="update22/scripts/2.2/"

pushd ${OBM_PATH_UPDATE}
./update-2.1-2.2.sh
popd

exit 0

