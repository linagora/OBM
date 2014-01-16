<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



require_once 'PHPUnit/Extensions/OutputTestCase.php';
require_once 'report/sender.php';
require_once 'report/sender/stdoutSender.php';
require_once 'report/sender/mailSender.php';

class ReportSenderTest extends PHPUnit_Extensions_OutputTestCase {

  public function testReportSingleSenderEcho() {
    $s1 = new SenderEchoUn;
    $this->expectOutputString("1:toto\n");
    $s1->send("toto","titi");
  }

  public function testReportMultipleSenderEcho() {
    $s1 = new SenderEchoUn;
    $s1->setNext(new SenderEchoDeux);
    $this->expectOutputString("1:tutu\n2:tutu\n");
    $s1->send("tutu","titi");
  }

  public function testReportStdoutSender() {
    $s1 = new StdoutSender;
    $this->expectOutputString("tutu");
    $s1->send("tutu","titi");
  }

  public function testReportMultipleStdoutSender() {
    $s1 = new StdoutSender;
    $s2 = new StdoutSender;
    $s2->setNext(new StdoutSender);
    $s1->setNext($s2);
    $this->expectOutputString("tutututututu");
    $s1->send("tutu","titi");
  }

}

class SenderEchoUn extends Sender {
  const context = 'test';

  protected function sendMessage($report,$name) {
    echo "1:$report\n";
  }
}

class SenderEchoDeux extends Sender {
  const context = 'test';

  protected function sendMessage($report,$name) {
    echo "2:$report\n";
  }
}
