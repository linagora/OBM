#!/bin/sh

set -e 
set -x
CURDIR=$1
#maven_home=$2
maven_home=${HOME}
BUILD_DEB_DIR="${CURDIR}/debian"
OBM_SYNC_BUILD_DEB_DIR="${BUILD_DEB_DIR}/obm-sync"
INSTALL_DIR="${OBM_SYNC_BUILD_DEB_DIR}/usr/share/obm-sync"
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

# copie du web-inf
WEB_INF=`find ${PROJECT_NAME}/target -name WEB-INF `
cp -r ${WEB_INF} ${INSTALL_DIR}
rm -f ${INSTALL_DIR}/WEB-INF/lib/postgresql-9.0-801.jdbc4.jar
rm -f ${INSTALL_DIR}/WEB-INF/lib/slf4j-api-*.jar
rm -f ${INSTALL_DIR}/WEB-INF/lib/logback*.jar
rm -f ${INSTALL_DIR}/WEB-INF/lib/jta-1.1.jar
exit 0
