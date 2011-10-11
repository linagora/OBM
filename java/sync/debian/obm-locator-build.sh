#!/bin/sh

set -e 
set -x
CURDIR=$1
maven_home=${HOME}
BUILD_DEB_DIR="${CURDIR}/debian"
OBM_SYNC_BUILD_DEB_DIR="${BUILD_DEB_DIR}/obm-locator"
INSTALL_DIR="${OBM_SYNC_BUILD_DEB_DIR}/var/lib/jetty/webapps/"
MVN_BIN="/usr/bin/mvn -Dmaven.test.skip -Duser.home=${maven_home}"
POM_FILE="pom.xml"
CHANGELOG_FILE="$BUILD_DEB_DIR/changelog"
PROJECT_NAME="obm-locator"

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
WEBAPP="${PROJECT_NAME}/target/obm-locator"

cp -r ${WEBAPP} ${INSTALL_DIR}

exit 0 
