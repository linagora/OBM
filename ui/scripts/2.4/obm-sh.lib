#!/bin/bash

# Returns a value from obm_conf.ini and store it into $VALUE
function get_val() {
    VALUE=`grep ^$1\ *= ../../conf/obm_conf.ini | cut -d= -f2 | tr -d '^ ' | tr -d '" '`
}

# Locate php-cli interprete and sets $PHP
function locate_php_interp() {
    PHP=`which php5 2> /dev/null`
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
    export PHP
}
