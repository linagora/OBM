#!/bin/bash
# this script must be call to reinstall all tomcat application
set -e


list_appli="/etc/obm-tomcat/applis"
tomcat_register_path="/usr/share/tomcat/conf/Catalina/localhost/"

rm -f ${tomcat_register_path}/*

if [ -d ${list_appli} ]; then
  for i in `ls ${list_appli}/*.xml`; do
    echo "publish $i into OBM tomcat server..."
    ln -s $i ${tomcat_register_path}/
  done
fi

/etc/init.d/obm-tomcat restart || true

exit 0
