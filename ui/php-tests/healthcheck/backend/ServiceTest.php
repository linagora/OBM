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
  public function testRouteWithUnsupportedMethod() {
    $service = new Service();

    $service->route("/unknown");
  }

  /**
   * @expectedException InvalidArgumentException
   */
  public function testRouteWithEmptyMethod() {
    $service = new Service();

    $service->route("/");
  }

  public function testRouteWithGetAvailableServices() {
    $service = $this->getMock('Service', array('getAvailableChecks'));
    $service->expects($this->once())->method("getAvailableChecks")->with();

    $service->route("/getAvailableChecks");
  }

  public function testRouteWithExecuteCheck() {
    $service = $this->getMock('Service', array('executeCheck'));
    $service->expects($this->once())->method("executeCheck")->with($this->equalTo("test"));

    $service->route("/executeCheck/test");
  }

  /**
   * @expectedException InvalidArgumentException
   */
  public function testExecuteCheckWithNoId() {
    $service = new Service();
    $service->executeCheck(null);
  }

  public function testGetAvailableChecks() {
    $service = new Service();
    
    $this->assertNotNull(json_decode($service->getAvailableChecks()));
  }
  
  public function testExecuteCheck() {
    $service = new Service();
    $expectedResult = new CheckResult(CheckStatus::OK, array("TestMessage"));
    $loader = $this->getMock('CheckLoader', array("load"));
    $check = $this->getMock('Check', array("execute"));
    $loader->expects($this->once())->method("load")->with("identifier")->will($this->returnValue($check));
    $check->expects($this->once())->method("execute")->will($this->returnValue($expectedResult));

    $service->setCheckLoader($loader);
    $checkResult = $service->executeCheck("identifier");
    
    $this->assertNotNull(json_decode($checkResult));
  }
  
  /**
   * @expectedException InvalidArgumentException
   */
  public function testExecuteCheckWithNonExistentCheck() {
    $service = new Service();
    $loader = $this->getMock('CheckLoader', array("load"));
    $loader->expects($this->once())->method("load")->with("non-existent")->will($this->returnValue(null));
  
    $service->setCheckLoader($loader);
    $checkResult = $service->executeCheck("non-existent");
  }

}