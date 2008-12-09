<?php

require_once dirname(__FILE__).'/TestsHelper.php';

require_once 'AclTest.php';
 
class AllTests {
  public static function suite() {
    $suite = new PHPUnit_Framework_TestSuite('OBM');
    $suite->addTestSuite('OBM_Acl_TestCase');
    return $suite;
  }
}
