<?php

/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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

require dirname(__FILE__) . '/../../../php/healthcheck/backend/Service.php';
require dirname(__FILE__) . '/../../../php/healthcheck/backend/CheckResult.php';
require dirname(__FILE__) . '/../../../php/healthcheck/backend/CheckStatus.php';

class ServiceTest extends PHPUnit_Framework_TestCase {

  /**
   * @expectedException InvalidArgumentException
   */
  public function testRouteWithUnsupportedModule() {
    $service = new Service();

    $service->route("/unknown");
  }
  
  public function testRouteWithOnlyModule() {
    $service = $this->getMock('Service', array('getModuleChecks'));
    $service->expects($this->once())->method("getModuleChecks")->with($this->equalTo("php"));
  
    $service->route("/php");
  }
  
  public function testRouteWithNoPathInfo() {
    $service = $this->getMock('Service', array('getAvailableModules'));
    $service->expects($this->once())->method("getAvailableModules")->with();
  
    $service->route("");
  }

  public function testRouteWithEmptyPathInfo() {
    $service = $this->getMock('Service', array('getAvailableModules'));
    $service->expects($this->once())->method("getAvailableModules")->with();

    $service->route("/");
  }

  public function testRouteWithExecuteCheck() {
    $service = $this->getMock('Service', array('executeCheck'));
    $service->expects($this->once())->method("executeCheck")->with($this->equalTo("database"), $this->equalTo("test"));

    $service->route("/database/test");
  }

  public function testGetAvailableModules() {
    $service = $this->getMock('Service', array('loadGlobalInc', 'listDomains', 'isModuleEnabled'));

    $service->expects($this->once())
            ->method('loadGlobalInc');

    $domain[0]['id'] = 0;
    $domain[0]['label'] = "domain.linagora.org";
    $domain[0]['name'] = "domain.linagora.org";
    $domain[0]['global'] = FALSE;
    $service->expects($this->once())
            ->method('listDomains')
            ->will($this->returnValue($domain));
    
    $service->expects($this->any())
            ->method('isModuleEnabled')
            ->will($this->returnValue(TRUE));
    
    $this->assertNotNull(json_decode($service->getAvailableModules()));
  }
  
  public function testExecuteCheck() {
    $service = new Service();
    $expectedResult = new CheckResult(CheckStatus::OK, array("TestMessage"));
    $loader = $this->getMock('CheckLoader', array("load"));
    $check = $this->getMock('Check', array("execute"));
    $loader->expects($this->once())->method("load")->with("database", "identifier")->will($this->returnValue($check));
    $check->expects($this->once())->method("execute")->will($this->returnValue($expectedResult));

    $service->setCheckLoader($loader);
    $checkResult = $service->executeCheck("database", "identifier");
    
    $this->assertNotNull(json_decode($checkResult));
  }
  
  /**
   * @expectedException InvalidArgumentException
   */
  public function testExecuteCheckWithNonExistentCheck() {
    $service = new Service();
    $loader = $this->getMock('CheckLoader', array("load"));
    $loader->expects($this->once())->method("load")->with("database", "non-existent")->will($this->returnValue(null));
  
    $service->setCheckLoader($loader);
    $checkResult = $service->executeCheck("database", "non-existent");
  }

}
