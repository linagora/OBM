#!/bin/bash

source /etc/qemu-kvm.conf

test -f ${HOME}/${vm_storage_dir}/$1/vm.conf || {
    echo "usage: $0 <vm_name> where vm_name is a valid KVM virtual machine name"
    exit 1
}

funambol_path=/usr/lib/funambol-6.5.1/funambol

ant pack && \

scp_vm.sh $1 ../output/obm-0.1.1/lib/obm-0.1.1.jar 
connect_vm.sh $1 <<EOF
cp obm-0.1.1.jar ${funambol_path}/WEB-INF/lib
/etc/init.d/obm-tomcat restart
exit
EOF

