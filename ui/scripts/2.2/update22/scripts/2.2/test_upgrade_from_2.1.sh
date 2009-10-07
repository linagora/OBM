#!/bin/bash

pushd ../2.1
./install_obmdb_2.1.sh
popd

./update-2.1-2.2.sh
