################################################################################
#
# Copyright (C) 2011-2012 Linagora
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option) any
# later version, provided you comply with the Additional Terms applicable for OBM
# software by Linagora pursuant to Section 7 of the GNU Affero General Public
# License, subsections (b), (c), and (e), pursuant to which you must notably (i)
# retain the displaying by the interactive user interfaces of the "OBM, Free
# Communication by Linagora" Logo with the "You are using the Open Source and
# free version of OBM developed and supported by Linagora. Contribute to OBM R&D
# by subscribing to an Enterprise offer !" infobox, (ii) retain all hypertext
# links between OBM and obm.org, between Linagora and linagora.com, as well as
# between the expression "Enterprise offer" and pro.obm.org, and (iii) refrain
# from infringing Linagora intellectual property rights over its trademarks and
# commercial brands. Other Additional Terms apply, see
# <http://www.linagora.com/licenses/> for more details.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License and
# its applicable Additional Terms for OBM along with this program. If not, see
# <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
# version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
# applicable to the OBM software.
#
################################################################################

import os
import subprocess

import obm.healthcheck as hc

class JettyJVMVersionCheck(hc.Check):

    ID = 'JettyJVMVersionCheck'
    NAME = 'Jetty JVM Version Check'
    DESCRIPTION = 'Checks Jetty JVM version at the expected path'

    _JVM_BINARY = '/usr/lib/jvm/java/bin/java'
    _EXPECTED_VERSION = '1.6.0_35'
    _EXPECTED_TYPE = 'HotSpot(TM)'

    def execute(self):
        result = hc.CheckResult(hc.CheckStatus.OK)
        try:
            output = subprocess.Popen([self._JVM_BINARY, '-version'],
                    stderr=subprocess.PIPE).communicate()[1]
            if output.find(self._EXPECTED_VERSION) == -1:
                result.status = hc.CheckStatus.WARNING
                result.messages.append('JVM version is not ' + self._EXPECTED_VERSION)
            if output.find(self._EXPECTED_TYPE) == -1:
                result.status = hc.CheckStatus.ERROR
                result.messages.append('JVM type is not ' + self._EXPECTED_TYPE)
        except OSError:
            result.status = hc.CheckStatus.ERROR
            result.messages.append('The JVM binary could not be started')
        return result
