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
import pwd

from check import Check
from check_result import CheckResult
from check_status import CheckStatus

class FileOwnerCheck(Check):

    def _check_path_owner(self, path, owner, result):
        if os.path.exists(path):
            actual_owner = pwd.getpwuid(os.stat(path).st_uid).pw_name
            if actual_owner != owner:
                result.status = CheckStatus.ERROR
                result.messages.append('%s should be owned by %s, but is owned by %s' % (path, owner, actual_owner))
        else:
            result.status = CheckStatus.ERROR
            result.messages.append(path + ' does not exist on the file system')
        return result

    def _check_tree_owner(self, basepath_list, owner, result):
        for path in basepath_list:
            self._check_path_owner(path, owner, result)
            if os.path.isdir(path):
                self._check_tree_owner([os.path.join(path, entry)
                                            for entry in os.listdir(path)],
                                       owner,
                                       result)
        return result
