#!/bin/bash
###############################################################################
# OBM - File : install_obmdb_2.2.sh                                           #
#     - Desc : OBM Database 2.2 installation script                           #
# 2005-06-08 AliaSource                                                       #
###############################################################################

source `dirname $0`/obm-sh.lib

# Lecture des parametres de connexion a la BD
get_val dbtype
dbtype=`echo $VALUE | tr A-Z a-z`

echo "dbtype: ${dbtype}"

locate_php_interp

`dirname $0`/update-2.1-2.2.${dbtype}.sh

$PHP `dirname $0`/update-2.1-2.2.php
