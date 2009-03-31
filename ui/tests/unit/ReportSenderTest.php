<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
*/

require_once 'PHPUnit/Extensions/OutputTestCase.php';
require_once 'report/sender.php';

class ReportSenderTest extends PHPUnit_Extensions_OutputTestCase {

  public function testReportSingleSenderEcho() {
    $s1 = new SenderEchoUn;
    $this->expectOutputString("1:toto\n");
    $s1->send("toto");
  }

  public function testReportMultipleSenderEcho() {
    $s1 = new SenderEchoUn;
    $s1->setNext(new SenderEchoDeux);
    $this->expectOutputString("1:tutu\n2:tutu\n");
    $s1->send("tutu");
  }

}

class SenderEchoUn extends Sender {
  protected function sendMessage($report) {
    echo "1:$report\n";
  }
}
class SenderEchoDeux extends Sender {
  protected function sendMessage($report) {
    echo "2:$report\n";
  }
}
