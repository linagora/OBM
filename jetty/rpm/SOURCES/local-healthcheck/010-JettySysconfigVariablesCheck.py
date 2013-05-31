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

import sets

import obm.healthcheck as hc

class JettySysconfigVariablesCheck(hc.Check):

    ID = 'JettySysconfigVariablesCheck'
    NAME = 'Jetty Sysconfig Variables Check'
    DESCRIPTION = 'Checks if /etc/sysconfig/jetty6 contains mandatory variables'

    _JETTY_SYSCONFIG = '/etc/sysconfig/jetty6'
    _VARIABLES = frozenset(['JAVA_HOME', 'JAVA_OPTS', 'JETTY_PORT'])

    def execute(self):
        result = hc.CheckResult(hc.CheckStatus.OK)
        try:
            lines = [line for line in open(self._JETTY_SYSCONFIG, 'r')]
            matches = frozenset(var for var in self._VARIABLES
                                    for line in lines
                                            if line.strip().startswith(var + '='))
            if matches != self._VARIABLES:
                result.status = hc.CheckStatus.ERROR
                result.messages.append('Some variables are missing in %s:' % self._JETTY_SYSCONFIG)
                for var in self._VARIABLES - matches:
                    result.messages.append('\t%s is not defined' % var)
        except (IOError), ex:
            result.status = hc.CheckStatus.ERROR
            result.messages.append('Could not read %s' % self._JETTY_SYSCONFIG)
        return result
