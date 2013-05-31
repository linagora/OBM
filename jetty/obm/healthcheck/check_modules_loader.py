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
import imp
import inspect

from check import Check

class CheckModulesLoader(object):

    def __init__(self, path='.'):
        self._checks = list(self._check_modules_generator(path))

    def _is_sourcefile(self, name):
        return os.path.splitext(name)[1].lower() == '.py'

    def _checkname(self, filename):
        tmp_name = os.path.splitext(filename)[0]
        hyphen_idx = tmp_name.rfind('-')
        return tmp_name[hyphen_idx + 1:]

    def _load_module(self, file, path):
        return imp.load_source(self._checkname(file), os.path.join(path, file))

    def _check_modules_generator(self, checks_path):
        for entry in sorted(os.listdir(checks_path)):
            if self._is_sourcefile(entry):
                module = self._load_module(entry, checks_path)
                clazz_members = inspect.getmembers(module, inspect.isclass)
                for clazz in clazz_members:
                    if issubclass(clazz[1], Check):
                        check = clazz[1]()
                        yield check

    def get_modules(self):
        return self._checks
