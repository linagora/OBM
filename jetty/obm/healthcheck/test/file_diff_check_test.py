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
import os

from obm.healthcheck import FileDiffCheck
from obm.healthcheck import CheckResult
from obm.healthcheck import CheckStatus

class FileDiffCheckTest(unittest.TestCase):

    def test_diff_files_identical(self):
        path = os.path.dirname(os.path.abspath(__file__)) + '/resources/files/'
        differ = FileDiffCheck()
        result = differ._diff_files(path + 'left', path + 'right')
        assert result.status == CheckStatus.OK

    def test_diff_files_wrongpath_both(self):
        path = os.path.dirname(os.path.abspath(__file__)) + '/resources/files/'
        differ = FileDiffCheck()
        result = differ._diff_files('wrongpath1', 'wrongpath2')
        assert result.status == CheckStatus.ERROR
        assert result.messages == ['Could not open one of the diffed files:',
                "  [Errno 2] No such file or directory: 'wrongpath1'"]


    def test_diff_files_wrongpath_right(self):
        path = os.path.dirname(os.path.abspath(__file__)) + '/resources/files/'
        differ = FileDiffCheck()
        result = differ._diff_files(path + 'left', 'wrongpath2')
        assert result.status == CheckStatus.ERROR
        assert result.messages == ['Could not open one of the diffed files:',
                "  [Errno 2] No such file or directory: 'wrongpath2'"]

    def test_diff_files_different(self):
        path = os.path.dirname(os.path.abspath(__file__)) + '/resources/files/'
        expected = ['[%swrong_left] is different from the reference file [%sright]' % (path, path),
                'See unified diff below:',
                '  --- %swrong_left' %path,
                '  +++ %sright' % path,
                '  @@ -1,1 +1,1 @@',
                '  -tata',
                '  +toto']
        differ = FileDiffCheck()
        result = differ._diff_files(path + 'wrong_left', path + 'right')
        assert result.status == CheckStatus.ERROR
        assert result.messages == expected

    def test_diff_data(self):
        left = ['toto', 'titi', 'tata']
        right = ['toto', 'tata', 'titi']
        expected = ['[left] is different from the reference file [right]',
                'See unified diff below:',
                '  --- left',
                '  +++ right',
                '  @@ -1,3 +1,3 @@',
                '  toto',
                '  +tata',
                '  titi',
                '  -tata']
        differ = FileDiffCheck()
        result = CheckResult(CheckStatus.OK)
        result = differ._diff_data(left, right, 'left', 'right', result)
        assert result.status == CheckStatus.ERROR
        assert result.messages == expected
