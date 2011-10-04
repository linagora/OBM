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
?>
<?php
require_once dirname(__FILE__).'/TestsHelper.php';
require_once 'of_search.php';

class SearchTest extends PHPUnit_Framework_TestCase {

  public function testParser() {
      $strings[] = "toto";
      $assert[] = array('*' => array('toto'));
      $strings[] = "toto titi";
      $assert[] = array('*' => array('toto', 'titi'));
      $strings[] = "name:toto";
      $assert[] = array('name' => array('toto'));
      $strings[] = "name:toto:";
      $assert[] = array('name' => array('toto:'));
      $strings[] = "toto:";
      $assert[] = array('*' => array('toto:'));
      $strings[] = "name:toto lname:titi";
      $assert[] = array('name' => array('toto'), 'lname' => array('titi'));
      $strings[] = '"toto titi"';
      $assert[] = array('*' =>  array('toto titi'));
      $strings[] = "name:\"toto titi\"";
      $assert[] = array('name' => array('toto titi'));
      $strings[] = "name:\"toto titi\" lname:\"titi toto\"";
      $assert[] = array('name' => array('toto titi'), 'lname' => array('titi toto'));
      $strings[] = "(toto titi)";
      $assert[] = array('*' => array('toto', 'titi'));
      $strings[] = "name:(toto titi)";
      $assert[] = array('name' =>  array('toto', 'titi'));
      $strings[] = "name:(toto titi) lname:(titi toto)";
      $assert[] = array('name' => array('toto', 'titi'), 'lname' =>  array('titi', 'toto'));
      $strings[] = "toto titi lname:tutu";
      $assert[] = array('*' => array('toto', 'titi'), 'lname' => array('tutu'));
      $strings[] = "name:toto titi lname:tutu";
      $assert[] = array('name' => array('toto'), '*' => array('titi'), 'lname' => array('tutu'));
      $strings[] = "txtx name:toto titi lname:(tutu tata) tyty name:\"test de\"";
      $assert[] = array('*' => array('txtx', 'titi', 'tyty'), 'name' => array('toto','test de'), 'lname' => array('tutu', 'tata'));
      foreach($strings as $index => $string) {
        $result = OBM_Search::parse($string);
        $this->assertEquals($assert[$index], $result);
      }
  }

  public function testBuildQuery() {
      $strings[] = "toto";
      $assert[] = "(1 = 1 AND (firstname #LIKE 'toto%' OR lastname #LIKE 'toto%'))";
      $strings[] = "toto titi";
      $assert[] = "(1 = 1 AND (firstname #LIKE 'toto%' OR lastname #LIKE 'toto%') AND (firstname #LIKE 'titi%' OR lastname #LIKE 'titi%'))";
      $strings[] = "name:toto";
      $assert[] = "(1 = 1 AND (firstname #LIKE 'toto%'))";
      $strings[] = "name:toto:";
      $assert[] = "(1 = 1 AND (firstname #LIKE 'toto:%'))";
      $strings[] = "toto:";
      $assert[] = "(1 = 1 AND (firstname #LIKE 'toto:%' OR lastname #LIKE 'toto:%'))";
      $strings[] = "name:toto lname:titi";
      $assert[] = "(1 = 1 AND (firstname #LIKE 'toto%') AND (lastname #LIKE 'titi%'))";
      $strings[] = '"toto titi"';
      $assert[] = "(1 = 1 AND (firstname #LIKE 'toto titi%' OR lastname #LIKE 'toto titi%'))";
      $strings[] = "name:\"toto titi\"";
      $assert[] = "(1 = 1 AND (firstname #LIKE 'toto titi%'))";
      $strings[] = "name:\"toto titi\" lname:\"titi toto\"";
      $assert[] = "(1 = 1 AND (firstname #LIKE 'toto titi%') AND (lastname #LIKE 'titi toto%'))";
      $strings[] = "(toto titi)";
      $assert[] = "(1 = 1 AND (firstname #LIKE 'toto%' OR lastname #LIKE 'toto%') AND (firstname #LIKE 'titi%' OR lastname #LIKE 'titi%'))";
      $strings[] = "name:(toto titi)";
      $assert[] = "(1 = 1 AND (firstname #LIKE 'toto%') AND (firstname #LIKE 'titi%'))";
      $strings[] = "name:(toto titi) lname:(titi toto)";
      $assert[] = "(1 = 1 AND (firstname #LIKE 'toto%') AND (firstname #LIKE 'titi%') AND (lastname #LIKE 'titi%') AND (lastname #LIKE 'toto%'))";
      $strings[] = "toto titi lname:tutu";
      $assert[] = "(1 = 1 AND (firstname #LIKE 'toto%' OR lastname #LIKE 'toto%') AND (firstname #LIKE 'titi%' OR lastname #LIKE 'titi%') AND (lastname #LIKE 'tutu%'))";
      $strings[] = "name:toto titi lname:tutu";
      $assert[] = "(1 = 1 AND (firstname #LIKE 'toto%') AND (firstname #LIKE 'titi%' OR lastname #LIKE 'titi%') AND (lastname #LIKE 'tutu%'))";
      $strings[] = "txtx name:toto titi lname:(tutu tata) tyty name:\"test de\"";
      $assert[] = "(1 = 1 AND (firstname #LIKE 'txtx%' OR lastname #LIKE 'txtx%') AND (firstname #LIKE 'titi%' OR lastname #LIKE 'titi%') AND (firstname #LIKE 'tyty%' OR lastname #LIKE 'tyty%') AND (firstname #LIKE 'toto%') AND (firstname #LIKE 'test de%') AND (lastname #LIKE 'tutu%') AND (lastname #LIKE 'tata%'))";
      foreach($strings as $index => $string) {
        $result = OBM_Search::buildSearchQuery('SearchableMockup',$string);
        $this->assertEquals($assert[$index], $result);
      }
  }  
}

class SearchableMockup implements OBM_ISearchable {

  public static function fieldsMap() {
    $fields['*'] = array('firstname' => 'text', 'lastname' => 'text');
    $fields['name'] = array('firstname' => 'text');
    $fields['lname'] = array('lastname' => 'text');
    return $fields;
  }


}
