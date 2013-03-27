#!/bin/bash
#sylvain.garcia@obm.org
#build_hook for obm-storage - manage upgrade scripts
set -e

CURDIR=$1
BUILD_DEB_DIR="${CURDIR}/debian"
BUILD_DEB_DIR_STORAGE="${BUILD_DEB_DIR}/obm-storage"
INSTALL_DIR_SQL="${BUILD_DEB_DIR_STORAGE}/usr/share/dbconfig-common/data/obm-storage/install"
INSTALL_DIR_SCRIPT="${BUILD_DEB_DIR_STORAGE}/usr/share/dbconfig-common/scripts/obm-storage/install"
INSTALL_DIR_ADMINSQL="${BUILD_DEB_DIR_STORAGE}/usr/share/dbconfig-common/data/obm-storage/install-dbadmin"
UPGRADE_SCRIPTS_DIR_MYSQL="${BUILD_DEB_DIR_STORAGE}/usr/share/dbconfig-common/scripts/obm-storage/upgrade/mysql"
UPGRADE_SCRIPTS_DIR_PGSQL="${BUILD_DEB_DIR_STORAGE}/usr/share/dbconfig-common/scripts/obm-storage/upgrade/pgsql"
UPGRADE_SQL_DIR_MYSQL="${BUILD_DEB_DIR_STORAGE}/usr/share/dbconfig-common/data/obm-storage/upgrade/mysql"
UPGRADE_SQL_DIR_PGSQL="${BUILD_DEB_DIR_STORAGE}/usr/share/dbconfig-common/data/obm-storage/upgrade/pgsql"

OBM_UPDATE_SCRIPTS_24=${CURDIR}/scripts/2.4/updates

##Mysql OBM Installation
cp ${CURDIR}/debian/misc/db/mysql/mysql.sh ${INSTALL_DIR_SCRIPT}/mysql
BUILD_DEB_DIR_STORAGE_SCRIPTS="${BUILD_DEB_DIR_STORAGE}/usr/share/obm-storage/update-install"
mkdir -p ${BUILD_DEB_DIR_STORAGE_SCRIPTS}/scripts
cp -r ${CURDIR}/scripts/* ${BUILD_DEB_DIR_STORAGE_SCRIPTS}/scripts/
ln -sf /etc/obm ${BUILD_DEB_DIR_STORAGE_SCRIPTS}/conf

#Pgsql OBM Installation
cp ${CURDIR}/debian/misc/db/pgsql/pgsql.sh ${INSTALL_DIR_SCRIPT}/pgsql


#MYSQL UPGRADE
#Upgrade Mysql OBM 2.1.5
cp ${CURDIR}/debian/misc/db/mysql/upgrade2.1.5-1.sql ${UPGRADE_SQL_DIR_MYSQL}/2.1.5-1
#Upgrade Mysql OBM 2.1.11
cp ${CURDIR}/debian/misc/db/mysql/upgrade2.1.11-0.sql ${UPGRADE_SQL_DIR_MYSQL}/2.1.11-0
#Upgrade Mysql OBM 2.1.15
cp ${CURDIR}/debian/misc/db/mysql/upgrade2.1.15-0.sql ${UPGRADE_SQL_DIR_MYSQL}/2.1.15-0
#Upgrade Mysql 2.1.X to 2.2.0
#Scritp are now installed on /usr/share/dbconfig-common/obm-storage with installed script
cp ${CURDIR}/debian/misc/db/mysql/upgrade2.1-2.2.sh ${UPGRADE_SCRIPTS_DIR_MYSQL}/2.2.0-0
#upgrade Mysql OBM 2.2.1
cp ${CURDIR}/debian/misc/db/mysql/upgrade2.2.1-4.sql ${UPGRADE_SQL_DIR_MYSQL}/2.2.1-4
#upgrade Mysql OBM 2.2.2
#cp ${OBM_UPDATE_SCRIPTS}/update-2.2.1-2.2.2.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.2.2-1
cp ${CURDIR}/debian/misc/db/mysql/upgrade2.2.1-2.2.2.sh ${UPGRADE_SCRIPTS_DIR_MYSQL}/2.2.2-1
OBM_UPDATE_SCRIPTS=${CURDIR}/scripts/2.2/update22/scripts/2.2/updates
#upgrade Mysql OBM 2.2.4
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.3-2.2.4.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.2.4-1
#upgrade Mysql OBM 2.2.5
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.4-2.2.5.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.2.5-1
#upgrade Mysql OBM 2.2.6
cp ${CURDIR}/debian/misc/db/mysql/upgrade2.2.5-2.2.6.sh ${UPGRADE_SCRIPTS_DIR_MYSQL}/2.2.6-3
#upgrade Mysql OBM 2.2.9
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.8-2.2.9.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.2.9-1
#upgrade Mysql OBM 2.2.10
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.9-2.2.10.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.2.10-1
#upgrade Mysql OBM 2.2.11
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.10-2.2.11.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.2.11-1
#upgrade Mysql OBM 2.2.12
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.11-2.2.12.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.2.12-1
#upgrade Mysql OBM 2.2.13
cp ${CURDIR}/debian/misc/db/mysql/upgrade2.2.12-2.2.13.sh ${UPGRADE_SCRIPTS_DIR_MYSQL}/2.2.13-1
#upgrade Mysql OBM 2.2.14
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.13-2.2.14.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.2.14-1
#upgrade Mysql OBM 2.2.15
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.14-2.2.15.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.2.15-1
#upgrade Mysql OBM 2.2.16
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.15-2.2.16.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.2.16-1
#upgrade Mysql OBM 2.3.0
cp ${CURDIR}/debian/misc/db/mysql/upgrade2.2-2.3.sh ${UPGRADE_SCRIPTS_DIR_MYSQL}/2.3.0-1
OBM_UPDATE_SCRIPTS_23=${CURDIR}/scripts/2.3/updates
#upgrade Mysql OBM 2.3.1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.0-2.3.1.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.1-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.1-2.3.2.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.2-1
cp ${CURDIR}/debian/misc/db/mysql/upgrade2.3.2-2.3.3.sh ${UPGRADE_SCRIPTS_DIR_MYSQL}/2.3.3-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.3-2.3.4.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.4-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.4-2.3.5.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.5-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.5-2.3.6.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.6-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.6-2.3.7.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.7-2
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.7-2.3.8.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.8-1
cp ${CURDIR}/debian/misc/db/mysql/upgrade2.3.7-2.3.8.sh ${UPGRADE_SCRIPTS_DIR_MYSQL}/2.3.8-2
cp ${CURDIR}/debian/misc/db/mysql/upgrade2.3.8-2.3.9.sh ${UPGRADE_SCRIPTS_DIR_MYSQL}/2.3.9-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.9-2.3.10.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.10-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.10-2.3.11.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.11-2
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.11-2.3.12.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.12-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.12-2.3.13.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.13-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.13-2.3.14.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.14-2
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.14-2.3.15.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.15-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.15-2.3.16.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.16-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.16-2.3.17.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.17-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.17-2.3.18.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.18-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.18-2.3.19.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.19-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.19-2.3.20.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.3.20-1
#upgrade Mysql OBM 2.4.0
cp ${CURDIR}/debian/misc/db/update-2.3-2.4.sh ${UPGRADE_SCRIPTS_DIR_MYSQL}/2.4.0-1
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.1~alpha6.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.4.1~alpha6
cp ${CURDIR}/debian/misc/db/mysql/upgrade2.4.1~alpha9+git.sh ${UPGRADE_SCRIPTS_DIR_MYSQL}/2.4.1~alpha9+git
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.1~alpha10+git.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.4.1~alpha10+git
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.1~beta1.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.4.1~beta1
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.1~beta2.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.4.1~beta2
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.1~beta3.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.4.1~beta3
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.2.0~0.alpha2.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.4.2.0~0.alpha2
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.2.0~0.beta3.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.4.2.0~0.beta3
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.2.0~0.beta4.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.4.2.0~0.beta4
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.2.2~0.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.4.2.2~0
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.2.5~1.mysql.sql ${UPGRADE_SQL_DIR_MYSQL}/2.4.2.5~1

#PGSQL UPGRADE
#upgrade Pgsql OBM 2.2.1
cp ${CURDIR}/debian/misc/db/pgsql/upgrade2.2.1-4.sql ${UPGRADE_SQL_DIR_PGSQL}/2.2.1-4
#upgrade Pgsql OBM 2.2.2
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.1-2.2.2.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.2.2-1
#upgrade Pgsql OBM 2.2.3
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.2-2.2.3.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.2.3-1
#upgrade Pgsql OBM 2.2.4
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.3-2.2.4.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.2.4-1
#upgrade Pgsql OBM 2.2.5
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.4-2.2.5.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.2.5-2
#upgrade Pgsql OBM 2.2.6
cp ${CURDIR}/debian/misc/db/pgsql/upgrade2.2.5-2.2.6.sh ${UPGRADE_SCRIPTS_DIR_PGSQL}/2.2.6-1
#upgrade Pgsql OBM 2.2.9
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.8-2.2.9.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.2.9-1
#upgrade Pgsql OBM 2.2.10
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.9-2.2.10.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.2.10-1
#upgrade Pgsql OBM 2.2.11
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.10-2.2.11.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.2.11-1
#upgrade Pgsql OBM 2.2.12
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.11-2.2.12.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.2.12-1
#upgrade Pgsql OBM 2.2.13
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.12-2.2.13.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.2.13-1
#upgrade Pgsql OBM 2.2.14
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.13-2.2.14.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.2.14-1
#upgrade Pgsql OBM 2.2.15
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.14-2.2.15.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.2.15-1
#upgrade Pgsql OBM 2.2.16
cp ${OBM_UPDATE_SCRIPTS}/update-2.2.15-2.2.16.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.2.16-1
#upgrade Pgsql OBM 2.3.0-1
cp ${CURDIR}/debian/misc/db/mysql/upgrade2.2-2.3.sh ${UPGRADE_SCRIPTS_DIR_PGSQL}/2.3.0-1
#upgrade Pgsql OBM 2.3.1-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.0-2.3.1.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.1-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.1-2.3.2.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.2-1
cp ${CURDIR}/debian/misc/db/pgsql/upgrade2.3.2-2.3.3.sh ${UPGRADE_SCRIPTS_DIR_PGSQL}/2.3.3-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.3-2.3.4.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.4-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.4-2.3.5.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.5-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.5-2.3.6.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.6-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.6-2.3.7.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.7-2
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.7-2.3.8.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.8-1
cp ${CURDIR}/debian/misc/db/pgsql/upgrade2.3.7-2.3.8.sh ${UPGRADE_SCRIPTS_DIR_PGSQL}/2.3.8-2
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.8-2.3.9.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.9-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.9-2.3.10.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.10-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.10-2.3.11.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.11-2
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.11-2.3.12.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.12-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.12-2.3.13.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.13-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.13-2.3.14.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.14-2
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.14-2.3.15.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.15-1
cp ${CURDIR}/debian/misc/db/pgsql/upgrade2.3.15-2.3.16.sh ${UPGRADE_SCRIPTS_DIR_PGSQL}/2.3.16-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.15-2.3.16.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.16-0
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.16-2.3.17.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.17-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.17-2.3.18.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.18-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.18-2.3.19.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.19-1
cp ${OBM_UPDATE_SCRIPTS_23}/update-2.3.19-2.3.20.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.3.20-1

#upgrade Pgsql OBM 2.4.0
cp ${CURDIR}/debian/misc/db/update-2.3-2.4.sh ${UPGRADE_SCRIPTS_DIR_PGSQL}/2.4.0-1
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.1~alpha6.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.4.1~alpha6
cp ${CURDIR}/debian/misc/db/pgsql/upgrade2.4.1~alpha9+git.sh ${UPGRADE_SCRIPTS_DIR_PGSQL}/2.4.1~alpha9+git
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.1~alpha10+git.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.4.1~alpha10+git
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.1~beta1.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.4.1~beta1
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.1~beta2.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.4.1~beta2
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.1~beta3.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.4.1~beta3
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.2.0~0.alpha2.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.4.2.0~0.alpha2
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.2.0~0.beta3.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.4.2.0~0.beta3
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.2.0~0.beta4.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.4.2.0~0.beta4
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.2.2~0.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.4.2.2~0
cp ${OBM_UPDATE_SCRIPTS_24}/update-2.4.2.5~1.pgsql.sql ${UPGRADE_SQL_DIR_PGSQL}/2.4.2.5~1

exit 0
