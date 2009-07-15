#!/bin/bash
###############################################################################
# OBM - File : update-2.2-2.3.sh                                              #
#     - Desc : OBM Database 2.2 to 2.3 update script                          #
###############################################################################

source `dirname $0`/obm-sh.lib

# Lecture des parametres de connexion a la BD
get_val dbtype
dbtype=`echo $VALUE | tr A-Z a-z`

echo "dbtype: ${dbtype}"

locate_php_interp

`dirname $0`/update-2.2-2.3.${dbtype}.sh || {
  echo "Error running update-2.2-2.3.sh abort."
  exit 1
}

$PHP `dirname $0`/update-2.2-2.3.php

exit 0
