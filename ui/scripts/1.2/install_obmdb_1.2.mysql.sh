#!/bin/sh
###############################################################################
# OBM - File : install_obmdb_1.2.sh                                           #
#     - Desc : MySQL Database 1.2 installation script                         #
# 2005-06-08 ALIACOM                                                          #
###############################################################################
# $Id$
###############################################################################

# Mysql User, Password and Data lang var definition
U="obm"
P="obm"
DB="obm"
DATA_LANG="en"

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

# Create the Artichow library link
mv ../../obminclude/Artichow-1.06-php4+5/ ../obminclude/Artichow

echo "*** Document repository creation"
$PHP install_document_1.2.php || (echo $?; exit $?)


echo "*** Database creation"

echo "  Delete old database if exists"
mysql -u $U -p$P -e "DROP DATABASE IF EXISTS $DB"

echo "  Create new $DB database"
mysql -u $U -p$P -e "CREATE DATABASE $DB"

echo "  Create new $DB database model"
mysql -u $U -p$P $DB < create_obmdb_1.2.mysql.sql


echo "*** Database filling"

# Dictionnary data insertion
echo "  Dictionnary data insertion"
mysql -u $U -p$P $DB < data-$DATA_LANG/obmdb_ref_1.2.sql

# Company Naf Code data insertion
echo "  Company Naf Code data insertion"
mysql -u $U -p$P $DB < data-$DATA_LANG/obmdb_nafcode_1.2.sql

# Test data insertion
echo "  Test data insertion"
mysql -u $U -p$P $DB < obmdb_test_values_1.2.sql

# Default preferences data insertion
echo "  Default preferences data insertion"
mysql -u $U -p$P $DB < obmdb_default_values_1.2.sql


echo "*** Data checking and validation"

# Set the current dir to php/admin_data (to resolve includes then)
cd ../../php/admin_data

# Update calculated values
echo "  Update calculated values"
$PHP admin_data_index.php -a data_update

# Update phonetics and approximative searches
echo "  Update phonetics and approximative searches"
$PHP admin_data_index.php -a sound_aka_update
