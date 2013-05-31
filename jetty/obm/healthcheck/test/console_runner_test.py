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

import unittest
import nose
import sys
import minimock
import os

from StringIO import StringIO

from obm.healthcheck import ConsoleRunner
from obm.healthcheck import CheckResult
from obm.healthcheck import CheckStatus
from obm.healthcheck import Check

class ConsoleRunnerTest(unittest.TestCase):

    def test_escape_text(self):
        runner = ConsoleRunner(os.path.dirname(os.path.abspath(__file__)) + '/resources/packages/')
        assert runner._escape_text('test', runner._BOLD) == '\033[1m' + 'test' + '\033[0m'
        assert runner._escape_text('test', runner._BOLD_YELLOW) == '\033[93m' + 'test' + '\033[0m'

    def test_load_checks(self):
        runner = ConsoleRunner(os.path.dirname(os.path.abspath(__file__)) + '/resources/packages/')
        assert runner._packages != None
        stderr = sys.stderr
        try:
            sys.stderr = StringIO()
            runner._load_checks('wrongpath')
        except SystemExit, ex:
            output = sys.stderr.getvalue().strip()
            expected = ("Error loading checks, terminating process...\n"
                        "Cause: [Errno 2] No such file or directory: 'wrongpath'")
            assert output == expected
        finally:
            sys.stderr = stderr

    def test_run_checks(self):
        tracer = minimock.TraceTracker()
        expected = ("Called Check.execute()\n"
                    "Called Check.execute()")
        runner = ConsoleRunner(os.path.dirname(os.path.abspath(__file__)) + '/resources/packages')
        for package in runner._packages:
            for i in range(len(package.checks)):
                package.checks[i] = minimock.Mock('Check', tracker = tracer)
                package.checks[i].mock_returns = minimock.Mock('InstanceOfCheck', show_attrs = True)
                package.checks[i].execute.mock_returns = CheckResult(CheckStatus.OK)
                package.checks[i].ID = 'Dummy'
                package.checks[i].DESCRIPTION = 'Dummy'
                package.checks[i].NAME = 'Dummy'
        runner.run_checks()
        minimock.assert_same_trace(tracer, expected)
