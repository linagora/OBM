#!/bin/bash
###############################################################################
# OBM - File : install_obmdb_2.2.sh                                           #
#     - Desc : OBM Database 2.2 installation script                           #
# 2005-06-08 AliaSource                                                       #
###############################################################################
# $Id$
###############################################################################

# Get obm_conf.ini parameters
getVal () {
   echo Recherche $1
   VALUE=`grep ^$1\ *= ../../conf/obm_conf.ini | cut -d= -f2 | tr -d '^ ' | tr -d '" '`
   echo $VALUE
}

# Lecture des parametres de connexion a la BD
getVal host
H=$VALUE

getVal dbtype
DBTYPE=$VALUE

getVal user
U=$VALUE

getVal password
P=$VALUE

getVal db
DB=$VALUE

getVal lang
OBM_LANG=$VALUE

echo "*** Parameters used"
echo "database type  = $DBTYPE"
echo "database = $DB"
echo "database user = $U"
echo "database password = $P"
echo "install lang = $OBM_LANG"


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

PHP="$PHP -d include_path=.:`dirname $0`/../.."
echo $PHP : PHP interpreter found
# Create the Artichow library link
# mv ../../obminclude/Artichow-1.1.0-php4+5/ ../../obminclude/Artichow

echo "*** Document repository creation"
$PHP install_document_2.2.php || (echo $?; exit $?)


echo "*** Database creation"

echo "  Delete old database if exists"
if [ $DBTYPE == "MYSQL" ]; then
  mysql -h $H -u $U -p$P -e "DROP DATABASE IF EXISTS $DB"
elif [ $DBTYPE == "PGSQL" ]; then
  echo "PGSQL : nothing"
fi

echo "  Create new $DB database"
if [ $DBTYPE == "MYSQL" ]; then
  mysql -h $H -u $U -p$P -e "CREATE DATABASE $DB"
elif [ $DBTYPE == "PGSQL" ]; then
  psql -U $U template1 -c "CREATE DATABASE $DB with owner = $U"
fi

echo "  Create new $DB database model"
if [ $DBTYPE == "MYSQL" ]; then
  mysql -h $H -u $U -p$P $DB < create_obmdb_2.2.mysql.sql
elif [ $DBTYPE == "PGSQL" ]; then
  psql -U $U $DB < create_obmdb_2.2.pgsql.sql
fi

echo "*** Database filling"

# Default data insertion
echo "  Default data insertion"
if [ $DBTYPE == "MYSQL" ]; then
  mysql -h $H -u $U -p$P $DB < obmdb_default_values_2.2.sql
elif [ $DBTYPE == "PGSQL" ]; then
  cat postgres-pre.sql obmdb_default_values_2.2.sql | psql -U $U $DB 
fi

# Dictionnary data insertion
echo "  Dictionnary data insertion"
if [ $DBTYPE == "MYSQL" ]; then
  mysql -h $H -u $U -p$P $DB < data-$OBM_LANG/obmdb_ref_2.2.sql
elif [ $DBTYPE == "PGSQL" ]; then
  cat postgres-pre.sql data-$OBM_LANG/obmdb_ref_2.2.sql | psql -U $U $DB
fi

# Company Naf Code data insertion
echo "  Company Naf Code data insertion"
if [ $DBTYPE == "MYSQL" ]; then
  mysql -h $H -u $U -p$P $DB < data-$OBM_LANG/obmdb_nafcode_2.2.sql
elif [ $DBTYPE == "PGSQL" ]; then
  cat postgres-pre.sql data-$OBM_LANG/obmdb_nafcode_2.2.sql | psql -U $U $DB
fi

# Preferences data insertion & Update default lang to .ini value
echo "  Default preferences data insertion"
if [ $DBTYPE == "MYSQL" ]; then
  mysql -h $H -u $U -p$P $DB < obmdb_prefs_values_2.2.sql
  echo "UPDATE UserObmPref set userobmpref_value='$OBM_LANG' where userobmpref_option='set_lang'" | mysql -h $H -u $U -p$P $DB 
elif [ $DBTYPE == "PGSQL" ]; then
  cat postgres-pre.sql obmdb_prefs_values_2.2.sql | psql -U $U $DB 
  echo "UPDATE UserObmPref set userobmpref_value='$OBM_LANG' where userobmpref_option='set_lang'" | psql -U $U $DB 
fi

# Test data insertion
echo "  Test data insertion"
if [ $DBTYPE == "MYSQL" ]; then
  mysql -h $H -u $U -p$P $DB < obmdb_test_values_2.2.sql
elif [ $DBTYPE == "PGSQL" ]; then
  cat postgres-pre.sql obmdb_test_values_2.2.sql | psql -U $U $DB 
fi


echo "*** Data checking and validation"

# Set the current dir to php/admin_data (to resolve includes then)
cd ../../php/admin_data

# Update internal group values
echo "  Update internal group values"
$PHP admin_data_index.php -a data_update -m group

# Update calculated values
echo "  Update calculated values"
$PHP admin_data_index.php -a data_update

# Update phonetics and approximative searches
echo "  Update phonetics and approximative searches"
$PHP admin_data_index.php -a sound_aka_update
