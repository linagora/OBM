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
import sys

from check import Check
from check_result import CheckResult
from check_packages_loader import CheckPackagesLoader
from check_status import CheckStatus

class ConsoleRunner(object):
    _BOLD = '\033[1m'
    _BOLD_YELLOW = '\033[93m'
    _END = '\033[0m'
    _STATUS_STRING = ('[\033[1;32m OK \033[0m]',  '[\033[1;31m KO \033[0m]', '[\033[1;33mWARN\033[0m]')
    # used to adjust padding with escape characters length (4 escapes - 1 right-padding space)
    _PAD_ADJUST = 15

    def __init__(self, checks_path):
        self._load_checks(checks_path)
        self._status_string = ('[\033[92m OK \033[0m]',  '[\033[91m KO \033[0m]', '[\033[93mWARN\033[0m]')
        self._width = int(os.popen('stty size', 'r').read().split()[1])

    def _load_checks(self, checks_path):
        try:
            self._packages = CheckPackagesLoader(checks_path).get_packages()
        except OSError, ex:
            print >> sys.stderr, 'Error loading checks, terminating process...'
            print >> sys.stderr, 'Cause: %s' % str((ex))
            sys.exit(1)

    def _escape_text(self, string, escape):
        return escape + string + self._END

    def run_checks(self):
        for package in self._packages:
            print self._escape_text('Testing %s:' % package.name, self._BOLD)
            for check in package.checks:
                running = self._escape_text('Running %s:' % check.NAME, self._BOLD)
                description = '  %s %s...' % (running, check.DESCRIPTION)
                pad = self._width - len(description) + self._PAD_ADJUST
                print description,
                check_result = check.execute()
                status_string = self._STATUS_STRING[check_result.status]
                status = status_string.rjust(pad)
                print status
                for message in check_result.messages:
                    print '    %s' % self._escape_text(message, self._BOLD_YELLOW)
