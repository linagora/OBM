#!/bin/sh
###############################################################################
# OBM - File : install_obmdb_1.0.pgsql.sh                                     #
#     - Desc : PostgreSQL Database 1.0 installation script                    #
# 2005-06-08 ALIACOM                                                          #
###############################################################################
# $Id$
###############################################################################

# Postgres User, Password and Data lang var definition
U=obm
P="obm"
DB="obm"
DATA_LANG="fr"

# We search for PHP interpreter (different name on Debian, RedHat, Mandrake)
PHP=`which php4 2> /dev/null`
if [ $? != 0 ]; then
  PHP=`which php 2> /dev/null`
  if [ $? != 0 ]; then
    PHP=`which php-cgi 2> /dev/null`
    if [ $? != 0 ]; then
      echo "Can't find php interpreter"
      exit
    fi
  fi
fi
echo $PHP : PHP interpreter found


echo "*** Document repository creation"
$PHP install_document.php || exit $?


echo "*** Database creation"

echo "  Delete old database if exists"
psql -U $U $DB -c "DROP DATABASE $DB"

## XXXXXX obm postgres user creation ?

echo "  Create new $DB database"
psql -U $U $DB -c "CREATE DATABASE $DB with owner = $U"

echo "  Create new $DB database model"
psql -U $U $DB < create_obmdb_1.0.pgsql.sql


echo "*** Database filling"

# Dictionnary data insertion
echo "  Dictionnary data insertion"
cat postgres-pre.sql data-$DATA_LANG/obmdb_ref_1.0.sql | psql -U $U $DB

# Company Naf Code data insertion
echo "  Company Naf Code data insertion"
cat postgres-pre.sql data-$DATA_LANG/obmdb_nafcode_1.0.sql | psql -U $U $DB

# Test data insertion
echo "  Test data insertion"
cat postgres-pre.sql obmdb_test_values_1.0.sql | psql -U $U $DB

# Default preferences data insertion
echo "Default preferences data insertion"
cat postgres-pre.sql obmdb_default_values_1.0.sql | psql -U $U $DB 


echo "*** Data checking and validation"

# Set the current dir to php/admin_data (to resolve includes then)
cd ../../php/admin_data

# Update calculated values
echo "  Update calculated values"
$PHP admin_data_index.php -a data_update

# Update phonetics ans approximative searches
echo "  Update phonetics and approximative searches"
$PHP admin_data_index.php -a sound_aka_update
