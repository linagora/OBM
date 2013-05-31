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
import unittest
import nose

from obm.healthcheck import CheckModulesLoader


class CheckModulesLoaderTest(unittest.TestCase):

    def test_is_sourcefile(self):
        check_loader = CheckModulesLoader(os.path.dirname(os.path.abspath(__file__)) + '/resources/packages/001-ExampleOne')
        assert check_loader._is_sourcefile('sourcefile.py') == True
        assert check_loader._is_sourcefile('001-MyCheck.py') == True
        assert check_loader._is_sourcefile('compiledfile.pyc') == False
        assert check_loader._is_sourcefile('textifle.txt') == False
        assert check_loader._is_sourcefile('unknown') == False

    def test_checkname(self):
        check_loader = CheckModulesLoader(os.path.dirname(os.path.abspath(__file__)) + '/resources/packages/001-ExampleOne')
        name = check_loader._checkname('001-MyCheck.py')
        assert name == 'MyCheck'

    def test_load_module(self):
        path = os.path.dirname(os.path.abspath(__file__)) + '/resources/packages/001-ExampleOne'
        file = '001-TestCheckOne.py'
        filepath = os.path.join(path, file)
        binfilepath = filepath + 'c'
        check_loader = CheckModulesLoader(path)
        module = check_loader._load_module(file, path)
        assert module != None
        assert module.__name__ == 'TestCheckOne'
        assert module.__file__ == filepath or module.__file__ == binfilepath

    def test_check_modules_generator(self):
        path = os.path.dirname(os.path.abspath(__file__)) + '/resources/packages/001-ExampleOne'
        check_loader = CheckModulesLoader(path)
        checks = list(check_loader._check_modules_generator(path))
        assert len(checks) == 2
        assert checks[0].ID == 'TestCheckOne'
        assert checks[1].ID == 'TestCheckTwo'

    def test_get_modules(self):
        path = os.path.dirname(os.path.abspath(__file__)) + '/resources/packages/001-ExampleOne'
        check_loader = CheckModulesLoader(path)
        checks = check_loader.get_modules()
        assert len(checks) == 2
        assert checks[0].ID == 'TestCheckOne'
        assert checks[1].ID == 'TestCheckTwo'
