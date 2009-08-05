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

require_once dirname(__FILE__).'/TestsHelper.php';
require_once dirname(__FILE__).'/../../obminclude/lang/fr/report.inc';

require_once 'AclTest.php';
require_once 'VpdiTest.php';
require_once 'EventTest.php';
require_once 'EventMailObserverTest.php';
//require_once 'ReportTest.php';
//require_once 'UserReportCommandTest.php';
//require_once 'ReportSenderTest.php';
 
class AllTests {
  public static function suite() {
    $suite = new PHPUnit_Framework_TestSuite('OBM');
    //$suite->addTestSuite('UserReportCommandTest');
    //$suite->addTestSuite('ReportTest');
    //$suite->addTestSuite('ReportSenderTest');
    $suite->addTestSuite('AclTest');
    $suite->addTestSuite('VpdiTest');
    $suite->addTestSuite('EventTest');
    $suite->addTestSuite('EventMailObserverTest');
    return $suite;
  }
}
