#!/bin/bash

pushd extension
sed 's/\(<em:version>\).\+\(<\/em:version>\)/\1'$(cat ../VERSION)'\2/' install.rdf > install.rdf.sed
mv install.rdf.sed install.rdf
rm -f ../obm-maja_*.xpi
zip -r ../obm-maja_`cat ../VERSION`.xpi * -x "*~" "*svn*"
popd
