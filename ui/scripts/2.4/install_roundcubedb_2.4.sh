#!/bin/bash
###############################################################################
# OBM - File : install_roundcubedb_2.4.sh                                           #
#     - Desc : Roundcube Database 2.4 installation script                           #
###############################################################################

source `dirname $0`/obm-sh.lib

get_val dbtype
DBTYPE=`echo $VALUE | tr A-Z a-z`
get_val user
U=$VALUE
get_val password
P=$VALUE

./install_roundcubedb_${DBTYPE}_2.4.sh roundcubemail ${U} ${P}
