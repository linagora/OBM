#!/bin/bash
# 
# Converts OBM DB to UTF-8, and runs 2.1 to 2.2 schema upgrade
#

# to ensure things defaulting to system locale don't fail
export LC_ALL=en_US.UTF-8

source `dirname $0`/obm-sh.lib

# DB parameters
get_val user
U=$VALUE
get_val password
P=$VALUE
get_val db
DB=$VALUE

mysqldump -u $U -p$P  --default-character-set='UTF8' ${DB} > ${HOME}/migration.sql 
success=$?

# Transform MyISAM to InnoDB
sed "s/ ENGINE=MyISAM/ ENGINE=InnoDB/g " ${HOME}/migration.sql > ${HOME}/migration.sql.utf8
mv ${HOME}/migration.sql.utf8 ${HOME}/migration.sql

# Transform Charset latin1 to UTF8
sed "s/ CHARSET=latin1/ CHARSET=utf8 COLLATE=utf8_general_ci/g " ${HOME}/migration.sql > ${HOME}/migration.sql.utf8
mv ${HOME}/migration.sql.utf8 ${HOME}/migration.sql


test ${success} -eq 0 || {
    echo "Error dumping DB to ${HOME}/migration.sql, abort."
    exit 1
}

echo "utf-8 encoded dump stored in ${HOME}/migration.sql"

mysqladmin -u $U -p$P -f drop ${DB}

mysqladmin -u $U -p$P --default-character-set='UTF8' create ${DB}


mysql -u $U -p$P  --default-character-set='UTF8' ${DB} < ${HOME}/migration.sql 

echo "utf-8 dump still available in ${HOME}/migration.sql"

echo "Running 2.1 -> 2.2 schema upgrade script..."
mysql -u $U -p$P  --default-character-set='UTF8' ${DB} <  ./update-2.1-2.2.mysql.sql >/dev/null
success=$?

test ${success} -eq 0 || {
    echo "Error running 2.1 to 2.2 upgrade script."
    exit 1
}
echo "[DONE]"

exit 0
