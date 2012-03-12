#!/bin/bash
###############################################################################
# OBM - File : install_obmdb_2.4.sh                                           #
#     - Desc : OBM Database 2.4 installation script                           #
###############################################################################

installation_type=$1

source `dirname $0`/obm-sh.lib


get_val dbtype
DB_TYPE=`echo $VALUE | tr A-Z a-z`
get_val user
DB_USER=$VALUE
get_val password
DB_PASSWD=$VALUE
get_val db
DB_NAME=$VALUE
get_val lang
OBM_LANG=$VALUE
get_val host
DB_HOST=$VALUE

echo "*** Parameters used"
echo "database type  = $DB_TYPE"
echo "database = $DB_NAME"
echo "database user = $DB_USER"
echo "database password = $DB_PASSWD"
echo "install lang = $OBM_LANG"
echo "database host = $DB_HOST"



locate_php_interp

echo "*** Document repository creation"
./install_document_2.4.sh

if [ -z $installation_type ] || [ $installation_type != 'filldata' ]; then
  ./install_obmdb_${DB_TYPE}_2.4.sh ${DB_NAME} ${DB_USER} ${DB_PASSWD} ${OBM_LANG} "full" ${DB_HOST}
else
  ./install_obmdb_${DB_TYPE}_2.4.sh ${DB_NAME} ${DB_USER} ${DB_PASSWD} ${OBM_LANG} "filldata" ${DB_HOST}
fi
