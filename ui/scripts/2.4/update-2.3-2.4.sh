#!/bin/bash
# 2.3 to 2.4 update script

source `dirname $0`/obm-sh.lib

locate_php_interp
# Lecture des parametres de connexion a la BD
get_val dbtype
dbtype=`echo $VALUE | tr A-Z a-z`

echo "dbtype: ${dbtype}"

`dirname $0`/update-2.3-2.4.${dbtype}.sh || {
  echo "Error running update-2.3-2.4.sh abort."
  exit 1
}

echo "Post migration script"
$PHP `dirname $0`/update-2.3-2.4.php

exit 0
