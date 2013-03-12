#!/bin/bash
#build_hook!!!!
set -e

CURDIR=$1
#copy file for obm-core
BUILD_DEB_DIR="${CURDIR}/debian"
BUILD_DEB_DIR_CORE="${BUILD_DEB_DIR}/obm-core"

mkdir -p ${BUILD_DEB_DIR_CORE}/usr/share/obm/debian
mkdir -p ${BUILD_DEB_DIR_CORE}/usr/share/obm/sample
mkdir -p ${BUILD_DEB_DIR_CORE}/usr/share/obm/www
mkdir -p ${BUILD_DEB_DIR_CORE}/etc/obm/modules
mkdir -p ${BUILD_DEB_DIR_CORE}/etc/obm/hooks/user
mkdir -p ${BUILD_DEB_DIR_CORE}/etc/obm/themes/images
mkdir -p ${BUILD_DEB_DIR_CORE}/var/lib/obm/backup
mkdir -p ${BUILD_DEB_DIR_CORE}/var/lib/obm/documents/0
mkdir -p ${BUILD_DEB_DIR_CORE}/var/lib/obm/documents/1
mkdir -p ${BUILD_DEB_DIR_CORE}/var/lib/obm/documents/2
mkdir -p ${BUILD_DEB_DIR_CORE}/var/lib/obm/documents/3
mkdir -p ${BUILD_DEB_DIR_CORE}/var/lib/obm/documents/4
mkdir -p ${BUILD_DEB_DIR_CORE}/var/lib/obm/documents/5
mkdir -p ${BUILD_DEB_DIR_CORE}/var/lib/obm/documents/6
mkdir -p ${BUILD_DEB_DIR_CORE}/var/lib/obm/documents/7
mkdir -p ${BUILD_DEB_DIR_CORE}/var/lib/obm/documents/8
mkdir -p ${BUILD_DEB_DIR_CORE}/var/lib/obm/documents/9
mkdir -p ${BUILD_DEB_DIR_CORE}/usr/share/obm-ui

cp ${CURDIR}/conf/obm_conf.inc.sample ${BUILD_DEB_DIR_CORE}/usr/share/obm/sample
cp -r ${CURDIR}/contrib ${BUILD_DEB_DIR_CORE}/usr/share/obm/www
cp -r ${CURDIR}/obminclude ${BUILD_DEB_DIR_CORE}/usr/share/obm/www
cp -r ${CURDIR}/php ${BUILD_DEB_DIR_CORE}/usr/share/obm/www
cp -r ${CURDIR}/scripts ${BUILD_DEB_DIR_CORE}/usr/share/obm/www
cp -r ${CURDIR}/cron ${BUILD_DEB_DIR_CORE}/usr/share/obm/www
cp -r ${CURDIR}/resources ${BUILD_DEB_DIR_CORE}/usr/share/obm/www
cp -r ${CURDIR}/locale ${BUILD_DEB_DIR_CORE}/usr/share/obm/www
cp -r ${CURDIR}/views ${BUILD_DEB_DIR_CORE}/usr/share/obm/www
cp -r ${CURDIR}/app ${BUILD_DEB_DIR_CORE}/usr/share/obm/www
cp -r ${CURDIR}/lib ${BUILD_DEB_DIR_CORE}/usr/share/obm/www
cp ${CURDIR}/tz/timezone-generator.php ${BUILD_DEB_DIR_CORE}/usr/share/obm-ui

exit 0

