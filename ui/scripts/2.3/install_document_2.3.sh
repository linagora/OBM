#!/bin/bash

#-------------------------------------------------------------------------+
#  Copyright (c) 1997-2009 OBM.org project members team                   |
#                                                                         |
# This program is free software; you can redistribute it and/or           |
# modify it under the terms of the GNU General Public License             |
# as published by the Free Software Foundation; version 2                 |
# of the License.                                                         |
#                                                                         |
# This program is distributed in the hope that it will be useful,         |
# but WITHOUT ANY WARRANTY; without even the implied warranty of          |
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
# GNU General Public License for more details.                            |
#-------------------------------------------------------------------------+
# http://www.obm.org                                                      |
#-------------------------------------------------------------------------+

#Desc: OBM install : create default Document repository

documentRoot=`cat ../../conf/obm_conf.ini |grep "^documentRoot"| cut -d'=' -f2 |sed -e 's/ //g' | sed -e 's/"//g'`

#Check document path is set
if [ -z ${documentRoot} ]; then
  echo "The document repository root is not set in obm_conf.ini !"
  exit 1
fi

#If document root does not exist, try to create it
if [ ! -d ${documentRoot} ]; then
  echo "The document repository root does not exist. Trying to create it"
  mkdir -p ${documentRoot}
fi

#Populate root repository with storage dirs
for ((i=0; $i<10; i++)); do
  mkdir -p ${documentRoot}/${i}
done

exit 0
