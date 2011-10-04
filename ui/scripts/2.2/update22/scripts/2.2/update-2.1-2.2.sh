#!/bin/bash
###############################################################################
# OBM - File : update-2.1-2.2.sh                                              #
#     - Desc : OBM Database 2.1 to 2.2 update script                          #
# 2008-06-08 AliaSource                                                       #
###############################################################################

source `dirname $0`/obm-sh.lib

# Lecture des parametres de connexion a la BD
get_val dbtype
dbtype=`echo $VALUE | tr A-Z a-z`

echo "dbtype: ${dbtype}"

locate_php_interp

`dirname $0`/update-2.1-2.2.${dbtype}.sh || {
  #echo "TEST Error running update-2.1-2.2.${dbtype}.sh abort."
  echo "Error running update-2.1-2.2.sh abort."
  exit 1
}

$PHP `dirname $0`/update-2.1-2.2.php

exit 0
