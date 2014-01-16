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
