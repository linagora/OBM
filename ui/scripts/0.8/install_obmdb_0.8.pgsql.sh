#!/bin/sh
###############################################################################
# OBM - File : install_obmdb_0.8.pgsql.sh                                     #
#     - Desc : PostgreSQL Database 0.8 installation script                    #
# 2003-12-30 ALIACOM                                                          #
###############################################################################
# $Id$
###############################################################################

# Postgres User and Password var definition
U=obm
P="obm"
DB="obm"

# We search for the PHP interpreter (different name on Debian, RedHat)
PHP=`which php4 2> /dev/null`
if [ $? != 0 ]; then
  PHP=`which php 2> /dev/null`
  if [ $? != 0 ]; then
    echo "Can't find php interpreter"
    exit
  fi
fi
echo $PHP : PHP interpreter found


# Database creation
echo "Postgres Database creation"
psql -U $U $DB < create_obmdb_0.8.pgsql.sql

# Dictionnary data insertion
echo "Dictionnary data insertion"
psql -U $U $DB < obmdb_ref_0.8_fr.sql

# Test data insertion
echo "Test data insertion"
psql -U $U $DB < obmdb_test_values_0.8.sql

# Default preferences data insertion
echo "Default preferences data insertion"
psql -U $U $DB < obmdb_default_values_0.8.sql

# Default preferences propagation on created users
echo "Default preferences propagation on created users"
$PHP ../../php/admin_pref/admin_pref_index.php -a user_pref_update

# Update calculated values
echo "Update calculated values"
$PHP ../../php/admin_data/admin_data_index.php -a data_update

# Update phonetics ans approximative searches
echo "Update phonetics ans approximative searches"
$PHP ../../php/admin_data/admin_data_index.php -a sound_aka_update
