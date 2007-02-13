#!/bin/sh
###############################################################################
# OBM - File : install_obmdb_2.0.sh                                           #
#     - Desc : MySQL Database 2.0 installation script                         #
# 2005-06-08 ALIACOM                                                          #
###############################################################################
# $Id$
###############################################################################

# Lit le fichier de configuration global obm_conf.ini
# Code repris du fichier www/scripts/2.4/install_db_2.4.sh d'Aliamin
function getVal () {
   echo Recherche $1
   VALUE=`grep ^$1\ *= ../../conf/obm_conf.ini | cut -d= -f2 | tr -d '^ ' | tr -d '" '`
   echo $VALUE
}

# Lecture des parametres de connexion a la BD
getVal user
U=$VALUE

getVal password
P=$VALUE

getVal db
DB=$VALUE

# Mysql User, Password and Data lang var definition
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

# Create the Artichow library link
mv ../../obminclude/Artichow-1.06-php4+5/ ../../obminclude/Artichow

echo "*** Document repository creation"
$PHP install_document_2.0.php || (echo $?; exit $?)


echo "*** Database creation"

echo "  Delete old database if exists"
mysql -u $U -p$P -e "DROP DATABASE IF EXISTS $DB"

echo "  Create new $DB database"
mysql -u $U -p$P -e "CREATE DATABASE $DB"

echo "  Create new $DB database model"
mysql -u $U -p$P $DB < create_obmdb_2.0.mysql.sql


echo "*** Database filling"

# Dictionnary data insertion
echo "  Dictionnary data insertion"
mysql -u $U -p$P $DB < data-$DATA_LANG/obmdb_ref_2.0.sql

# Company Naf Code data insertion
echo "  Company Naf Code data insertion"
mysql -u $U -p$P $DB < data-$DATA_LANG/obmdb_nafcode_2.0.sql

# Test data insertion
echo "  Test data insertion"
mysql -u $U -p$P $DB < obmdb_test_values_2.0.sql

# Default preferences data insertion
echo "  Default preferences data insertion"
mysql -u $U -p$P $DB < obmdb_default_values_2.0.sql


echo "*** Data checking and validation"

# Set the current dir to php/admin_data (to resolve includes then)
cd ../../php/admin_data

# Update calculated values
echo "  Update calculated values"
$PHP admin_data_index.php -a data_update

# Update phonetics and approximative searches
echo "  Update phonetics and approximative searches"
$PHP admin_data_index.php -a sound_aka_update
