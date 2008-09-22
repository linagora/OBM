#!/bin/bash

pushd extension
rm -f ../obm-maja_*.xpi
zip -r ../obm-maja_`cat ../VERSION`.xpi * -x "*~" "*svn*"
popd
