#!/bin/sh
###############################################################################
# OBM - File : install_obmdb_0.9.pgsql.sh                                     #
#     - Desc : PostgreSQL Database 0.9 installation script                    #
# 2004-12-30 ALIACOM                                                          #
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


# Database creation
echo "Postgres Database creation"
psql -U $U $DB < create_obmdb_0.9.pgsql.sql

# Dictionnary data insertion
echo "Dictionnary data insertion"
cat postgres-pre.sql data-$DATA_LANG/obmdb_ref_0.9.sql | psql -U $U $DB

# Company Naf Code data insertion
echo "Company Naf Code data insertion"
cat postgres-pre.sql data-$DATA_LANG/obmdb_nafcode_0.9.sql | psql -U $U $DB

# Test data insertion
echo "Test data insertion"
cat postgres-pre.sql obmdb_test_values_0.9.sql | psql -U $U $DB

# Default preferences data insertion
echo "Default preferences data insertion"
cat postgres-pre.sql obmdb_default_values_0.9.sql | psql -U $U $DB 

# Default preferences propagation on created users
echo "Default preferences propagation on created users"
$PHP ../../php/admin_pref/admin_pref_index.php -a user_pref_update

# Update calculated values
echo "Update calculated values"
$PHP ../../php/admin_data/admin_data_index.php -a data_update

# Update phonetics and approximative searches
echo "Update phonetics ans approximative searches"
$PHP ../../php/admin_data/admin_data_index.php -a sound_aka_update
