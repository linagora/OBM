<?php

/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
*
* This program is free software: you can redistribute it and/or modify it under
* the terms of the GNU Affero General Public License as published by the Free
* Software Foundation, either version 3 of the License, or (at your option) any
* later version, provided you comply with the Additional Terms applicable for OBM
* software by Linagora pursuant to Section 7 of the GNU Affero General Public
* License, subsections (b), (c), and (e), pursuant to which you must notably (i)
* retain the displaying by the interactive user interfaces of the “OBM, Free
* Communication by Linagora” Logo with the “You are using the Open Source and
* free version of OBM developed and supported by Linagora. Contribute to OBM R&D
* by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
* links between OBM and obm.org, between Linagora and linagora.com, as well as
* between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
* from infringing Linagora intellectual property rights over its trademarks and
* commercial brands. Other Additional Terms apply, see
* <http://www.linagora.com/licenses/> for more details.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License and
* its applicable Additional Terms for OBM along with this program. If not, see
* <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
* version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
* applicable to the OBM software.
* ***** END LICENSE BLOCK ***** */

require_once dirname(__FILE__) . '/../../Check.php';
require_once dirname(__FILE__) . '/../../CheckResult.php';
require_once dirname(__FILE__) . '/../../CheckStatus.php';

class VersionStatus implements Check {

  public function execute() {
    if($this->hasPHPCorrectDateTimeBehaviour()) {
      return new CheckResult(CheckStatus::OK);
    }

    return new CheckResult(CheckStatus::ERROR,
        "Your version of PHP (" . phpVersion() . ") is not valid. "
          . "It contains a critical date/time bug ( see https://bugs.php.net/bug.php?id=52290 ). "
          . "You must upgrade it to the latest available version of PHP 5.3 or 5.4.");
  }

  public function hasPHPCorrectDateTimeBehaviour() {

    $dt = new DateTime('2006-01-01', new DateTimeZone('UTC'));

    $test1 = $dt->format('o-\WW-N | Y-m-d | H:i:s');

    $dt->setISODate(2005, 52, 1);
    $test2 = $dt->format('o-\WW-N | Y-m-d | H:i:s');

    $dt->setDate(2007, 10, 10);
    $test3 = $dt->format('o-\WW-N | Y-m-d | H:i:s');

    $dt->setTime(20, 30, 40);
    $test4 = $dt->format('o-\WW-N | Y-m-d | H:i:s');

    return   $test1 == '2005-W52-7 | 2006-01-01 | 00:00:00'
          && $test2 == '2005-W52-1 | 2005-12-26 | 00:00:00'
          && $test3 == '2007-W41-3 | 2007-10-10 | 00:00:00'
          && $test4 == '2007-W41-3 | 2007-10-10 | 20:30:40';
  }
}
