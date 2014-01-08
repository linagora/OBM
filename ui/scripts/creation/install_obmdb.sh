#!/bin/bash
###############################################################################
# OBM - File : install_obmdb.sh                                               #
#     - Desc : OBM Database installation script                               #
###############################################################################

installation_type=$1

source `dirname $0`/../common/sh/obm.sh


get_val dbtype
DBTYPE=`echo $VALUE | tr A-Z a-z`
get_val user
U=$VALUE
get_val password
P=$VALUE
get_val db
DB=$VALUE
get_val lang
OBM_LANG=$VALUE

echo "*** Parameters used"
echo "database type  = $DBTYPE"
echo "database = $DB"
echo "database user = $U"
echo "database password = $P"
echo "install lang = $OBM_LANG"



locate_php_interp

echo "*** Document repository creation"
./install_document.sh

if [ -z $installation_type ] || [ $installation_type != 'filldata' ]; then
	./install_obmdb_${DBTYPE}.sh ${DB} ${U} ${P} ${OBM_LANG} "full"
else
	./install_obmdb_${DBTYPE}.sh ${DB} ${U} ${P} ${OBM_LANG} "filldata"
fi
