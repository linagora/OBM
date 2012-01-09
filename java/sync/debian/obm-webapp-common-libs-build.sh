#!/bin/sh

set -e 
set -x
CURDIR=$1
WEBAPP_CONTAINER=$2

maven_home=${HOME}
MVN_BIN="/usr/bin/mvn -Duser.home=${maven_home}"

BUILD_DEB_DIR="${CURDIR}/debian"
OBM_COMMON_LIBS_DIR="${BUILD_DEB_DIR}/obm-${WEBAPP_CONTAINER=}-common-libs"
INSTALL_DIR="${OBM_COMMON_LIBS_DIR}/usr/share/${WEBAPP_CONTAINER}/lib/"
CHANGELOG_FILE="${BUILD_DEB_DIR}/changelog"
PROJECT_NAME="webapp-common-dependencies"

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

# copie libs
cp  ${PROJECT_NAME}/target/${WEBAPP_CONTAINER}/*.jar ${INSTALL_DIR}
exit 0
