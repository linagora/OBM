#!/bin/sh

set -e 

CURDIR=$1
PKG_TYPE=$2
maven_home=${HOME}
BUILD_DEB_DIR="${CURDIR}/debian"
BUILD_RPM_DIR="${CURDIR}/rpm"
OBM_SYNC_BUILD_DEB_DIR="${BUILD_DEB_DIR}/opush"
OBM_SYNC_BUILD_RPM_DIR="${BUILD_RPM_DIR}/opush"
if [ "x${PKG_TYPE}" == "xdeb" ]; then
	INSTALL_DIR="${OBM_SYNC_BUILD_DEB_DIR}/var/lib/jetty/webapps/"
elif [ "x${PKG_TYPE}" == "xrpm" ]; then
	INSTALL_DIR="${OBM_SYNC_BUILD_RPM_DIR}/var/lib/jetty/webapps/"
fi
MVN_BIN="/usr/bin/mvn -Dmaven.test.skip -Duser.home=${maven_home}"
POM_FILE="pom.xml"
CHANGELOG_FILE="$BUILD_DEB_DIR/changelog"
PROJECT_NAME="opush/push"

# clean project
${MVN_BIN} clean
if [ $? -ne 0 ]; then
  echo "FATAL: mvn clean"
  exit 1 
fi

# build project
${MVN_BIN} install
if [ $? -ne 0 ]; then
  echo "FATAL: mvn package"
  exit 1
fi

# copie du web-inf
WEBAPP="${PROJECT_NAME}/target/Microsoft-Server-ActiveSync"
cp -r ${WEBAPP} ${INSTALL_DIR}

# Copie des fichiers de template de configuration
#_usrshare="${OBM_SYNC_BUILD_DEB_DIR}/usr/share/o-push"
#mkdir -p ${_usrshare}
#cp -r ${CURDIR}/opush/config-sample ${_usrshare}/debian-conf

exit 0 
