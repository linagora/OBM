#!/bin/bash
###############################################################################
# OBM - File : install_obmdb_2.3.sh                                           #
#     - Desc : OBM Database 2.3 installation script                           #
###############################################################################

source `dirname $0`/obm-sh.lib

# Lecture des parametres de connexion a la BD
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
$PHP install_document_2.3.php || (echo $?; exit $?)

./install_obmdb_${DBTYPE}_2.3.sh ${DB} ${U} ${P} ${OBM_LANG}

echo "*** Data checking and validation"

# Set the current dir to php/admin_data (to resolve includes then)
pushd ../../php/admin_data

# Update internal group values
echo "  Update internal group values"
$PHP admin_data_index.php -a data_update -m group

# Update calculated values
echo "  Update calculated values"
$PHP admin_data_index.php -a data_update

# Update phonetics and approximative searches
echo "  Update phonetics and approximative searches"
$PHP admin_data_index.php -a sound_aka_update

popd
