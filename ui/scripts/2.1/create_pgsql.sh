#!/bin/bash

# Get obm_conf.ini parameters
getVal () {
   VALUE=`grep ^$1\ *= ../../conf/obm_conf.ini | cut -d= -f2 | tr -d '^ ' | tr -d '" '`
}

# Lecture des parametres de connexion a la BD
getVal user
U=$VALUE

getVal password
P=$VALUE

getVal db
DB=$VALUE

su postgres -c "./create_pgsql_as_postgres.sh ${DB} ${U} ${P}"


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
echo "PHP interpreter found: ${PHP}"


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
