#!/bin/bash

test -f ${HOME}/kvm/$1/kvm.conf || {
    echo "usage: $0 <vm_name> where vm_name is a valid KVM virtual machine name"
    exit 1
}

funambol_path=/usr/lib/funambol-6.5.1/funambol

ant pack && \

scp.sh $1 ../output/obm-0.1.1/lib/obm-0.1.1.jar 
connect_to_kvm.sh $1 <<EOF
cp obm-0.1.1.jar ${funambol_path}/WEB-INF/lib
/etc/init.d/obm-tomcat restart
exit
EOF

