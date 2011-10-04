#!/bin/bash

source /etc/qemu-kvm.conf

test -f ${HOME}/${vm_storage_dir}/$1/vm.conf || {
    echo "usage: $0 <vm_name> where vm_name is a valid virtual machine name"
    exit 1
}

ant dist

scp_vm.sh $1 dist/WEB-INF/web.xml dist/WEB-INF/lib/obm-autoconf.jar dist/WEB-INF/lib/ldap.jar dist/WEB-INF/lib/utilities.jar

connect_vm.sh $1 <<EOF
mv web.xml	/usr/share/obm-autoconf/WEB-INF
mv obm-autoconf.jar	/usr/share/obm-autoconf/WEB-INF/lib
mv ldap.jar	/usr/share/obm-autoconf/WEB-INF/lib
mv utilities.jar	/usr/share/obm-autoconf/WEB-INF/lib
/etc/init.d/obm-tomcat restart
logout
EOF

