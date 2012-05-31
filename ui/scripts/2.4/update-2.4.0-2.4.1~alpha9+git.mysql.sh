
#!/bin/bash
# 
# Runs 2.4.0 to 2.4.1~alpha9+git schema upgrade
#

export LC_ALL=en_US.UTF-8

source `dirname $0`/obm-sh.lib

START_VERSION='2.4.0'
END_VERSION='2.4.1~alpha9+git'

# DB parameters
get_val user
U=$VALUE
get_val password
P=$VALUE
get_val db
DB=$VALUE

locate_php_interp

migration_file=/var/lib/mysql/migration.sql.gz
mysqldump -u $U -p$P  --default-character-set='UTF8' ${DB} | gzip > ${migration_file} 
success=$?

test ${success} -eq 0 || {
    echo "Error dumping DB to ${migration_file}, abort."
    exit 1
}
echo "Dump stored in ${migration_file}"

echo "Running $START_VERSION -> $END_VERSION schema upgrade script..."

$PHP `dirname $0`/updates/update-2.4.0-2.4.1~alpha9+git.mysql.php

success=$?
test ${success} -eq 0 || {
    echo "Error running $START_VERSION to $END_VERSION schema upgrade script."
    exit 1
}
echo "[DONE]"

exit 0
