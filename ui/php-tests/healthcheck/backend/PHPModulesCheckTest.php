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

require dirname(__FILE__) . '/../../../php/healthcheck/backend/checks/PHPModulesCheck.php';

class PHPVersionCheckTest extends PHPUnit_Framework_TestCase {
  private $modulesAgregator;
  private $phpNeededModules;
  

  protected function setUp() {
    $this->modulesAgregator = new PHPModulesCheck();
    $this->phpNeededModules = array(
        "gd"      => array("status" => CheckStatus::ERROR,
            "desc" => ""),
        "curl"    => array("status" => CheckStatus::ERROR,
            "desc" => "You will not be able to communicate with OBM-Satellite and OBM-Sync."),
        "apc"     => array("status" => CheckStatus::WARNING,
            "desc" => "You will not be able to generate PDF with Zend framework."),
        "imagick" => array("status" => CheckStatus::WARNING,
            "desc" => "You will not be able to generate PDF with OBM.")
    );
    
  }
  
  public function testValidCheckModules() {
    $loadedModules = array("cli", "pgsql", "gd", "db", "curl", "apc", "imagick");
    $actual = $this->modulesAgregator->checkModules($this->phpNeededModules, $loadedModules);
    $expectedCheckResult = new CheckResult(CheckStatus::OK);
    $this->assertEquals($expectedCheckResult, $actual);
  }
  
  public function testWarnCheckModules() {
    $loadedModules = array("cli", "pgsql", "mysql", "gd", "db", "curl", "apc");
    $actual = $this->modulesAgregator->checkModules($this->phpNeededModules, $loadedModules);
    $expectedCheckResult = new CheckResult(CheckStatus::WARNING,
        array("The PHP module imagick is not loaded or installed. You will not be able to generate PDF with OBM."));
    $this->assertEquals($expectedCheckResult, $actual);
  }
  
  public function testErrorCheckModules() {
    $loadedModules = array("cli", "gd", "db", "curl", "apc");
    $actual = $this->modulesAgregator->checkModules($this->phpNeededModules, $loadedModules);
    $expectedCheckResult = new CheckResult(CheckStatus::ERROR,
        array("The database PHP module is not loaded or installed. OBM will not work.",
              "The PHP module imagick is not loaded or installed. You will not be able to generate PDF with OBM."));
    $this->assertEquals($expectedCheckResult, $actual);
  }
}