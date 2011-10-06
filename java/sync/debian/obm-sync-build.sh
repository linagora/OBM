#!/bin/sh

set -e 
set -x
CURDIR=$1
#maven_home=$2
maven_home=${HOME}
BUILD_DEB_DIR="${CURDIR}/debian"
OBM_SYNC_BUILD_DEB_DIR="${BUILD_DEB_DIR}/obm-sync"
INSTALL_DIR="${OBM_SYNC_BUILD_DEB_DIR}/var/lib/jetty/webapps/"
MVN_BIN="/usr/bin/mvn -Duser.home=${maven_home}"
POM_FILE="pom.xml"
CHANGELOG_FILE="$BUILD_DEB_DIR/changelog"
PROJECT_NAME="services"

# clean project
${MVN_BIN} clean
if [ $? -ne 0 ]; then
  echo "Erreur clean"
  exit 1 
fi

# build project
${MVN_BIN} install
if [ $? -ne 0 ]; then
  echo "Erreur install"
  exit 1
fi

# copie du web-inf et du meta-inf
WEB_INF=`find ${PROJECT_NAME}/target -name WEB-INF `
META_INF=`find ${PROJECT_NAME}/target -name META-INF `
mkdir ${INSTALL_DIR}obm-sync
cp -r ${WEB_INF} ${INSTALL_DIR}obm-sync/
cp -r ${META_INF} ${INSTALL_DIR}obm-sync/

exit 0 
