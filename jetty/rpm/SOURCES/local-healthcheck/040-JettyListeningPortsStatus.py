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


import subprocess

import obm.healthcheck as hc

class JettyListeningPortsStatus(hc.Check):

    ID = 'JettyListeningPortsStatus'
    NAME = 'Jetty Listening Ports Status'
    DESCRIPTION = 'Checks if Jetty is listening on the expected ports'

    _JETTY_PID_FILE = '/var/run/jetty6/jetty.pid'
    _JETTY_EXPECTED_PORTS = frozenset(['8082', '8084'])
    _NS_IDX_PID = 6
    _NS_IDX_LOCAL = 3
    _NS_DATA_STARTLINE = 2


    def execute(self):
        result = hc.CheckResult(hc.CheckStatus.OK)

        grab_pid = lambda line: line[self._NS_IDX_PID].strip().split('/')[0]
        grab_port = lambda line: line[self._NS_IDX_LOCAL].split(':')[1]
        try:
            jetty_pid = [line for line in open(self._JETTY_PID_FILE, 'r')][0].strip('\n')
            try:
                stdout, stderr = subprocess.Popen(['netstat', '-ltpn'],
                        stdout=subprocess.PIPE,
                        stderr=subprocess.PIPE).communicate()
                ns_conn_lines = [line.strip().split()
                        for line in stdout.split('\n')
                                if line.strip()][self._NS_DATA_STARTLINE:]
                jetty_ports = frozenset(grab_port(line)
                        for line in ns_conn_lines
                                if grab_pid(line) == jetty_pid)
                if jetty_ports != self._JETTY_EXPECTED_PORTS:
                    result.status = hc.CheckStatus.ERROR
                    result.messages.append('Netstat result different than expected.')
                    result.messages.append('\tJetty PID: %s' % jetty_pid)
                    result.messages.append('\tExpected ports:')
                    for port in self._JETTY_EXPECTED_PORTS :
                        result.messages.append('\t\t%s' % port)
                    result.messages.append('\tActual ports:')
                    for port in jetty_ports :
                        result.messages.append('\t\t%s' % port)
                    if not jetty_ports : result.messages.append('\t\tNONE')
            except OSError:
                result.status = hc.CheckStatus.ERROR
                result.messages.append('The netstat command failed')
        except IOError:
            result.status = hc.CheckStatus.ERROR
            result.messages.append('Could not open Jetty pid file')
        return result
