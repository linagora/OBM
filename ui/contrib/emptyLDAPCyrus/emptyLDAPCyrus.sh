#!/bin/bash
# This script clean LDAP and Cyrus configuration
# Work on centralized installation (all services on same computer)

if [ -e /etc/init.d/cyrus2.2 ]; then
    CYRUSSTART='cyrus2.2'
else
    CYRUSSTART='cyrus2.3'
fi

which cvt_cyrusdb
if [ $? ne 0 ]; then
    echo 'commande cvt_cyrusdb introuvable'
    exit 1
fi

echo "Purge de l'annuaire LDAP: "
invoke-rc.d slapd stop
cd /var/lib/ldap
rm -f *
invoke-rc.d slapd start
echo done


echo 'Purge de la configuration cyrus: '
echo 'vide le fichier mailboxes.db: '
invoke-rc.d ${CYRUSSTART} stop
rm -f /tmp/mailboxes
touch /tmp/mailboxes
chmod 666 /tmp/mailboxes

cp -f /var/lib/cyrus/mailboxes.db /var/lib/cyrus/mailboxes.db.orig
rm /var/lib/cyrus/mailboxes.db

su -c "/usr/sbin/cvt_cyrusdb /tmp/mailboxes flat /var/lib/cyrus/mailboxes.db skiplist" - cyrus 
chmod 600 /var/lib/cyrus/mailboxes.db
chown cyrus:mail /var/lib/cyrus/mailboxes.db
echo done

partitions=`grep 'partition-' /etc/imapd.conf |grep -v 'partition-default:' |cut -d ' ' -f 2`
for part in ${partitions}; do
    echo -n 'suppression de '${part}': '
    rm -rf ${part}
    echo done
done

echo -n 'nettoie le fichier /etc/imapd.conf: '
partitionName=`grep 'partition-' /etc/imapd.conf |grep -v 'partition-default:' |cut -d ' ' -f 1`

cp -f /etc/imapd.conf /etc/imapd.conf.orig
for part in ${partitionName}; do
    grep -v "${part}" /etc/imapd.conf > /etc/imapd.conf.modif
    mv /etc/imapd.conf.modif /etc/imapd.conf
done
echo done

invoke-rc.d ${CYRUSSTART} start
echo done
