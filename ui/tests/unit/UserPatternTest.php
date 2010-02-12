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
require_once dirname(__FILE__).'/../../app/default/models/UserPattern.php';

class UserPatternTest extends PHPUnit_Framework_TestCase {

  public function setup() {
    $GLOBALS['obm']['uid'] = 1;
    $GLOBALS['obm']['domain_id'] = 2;
    $GLOBALS['obm']['domain_global'] = false;
  }

  public function testApplyTo() {
    $pattern = new UserPattern('test');

    $params = array();
    $pattern->applyTo($params);
    $this->assertEquals(array(),$params);

    $params = array('login' => 'toto');
    $pattern->applyTo($params);
    $this->assertEquals(array('login' => 'toto'),$params);

    $pattern->set_attributes(array(
      'email' => '%login%@test.fr',
      'login' => '%firstname%.%lastname%',
      'desc'  => 'Utilisateur %login% (%email%)'
    ));

    $params = array(
      'lastname' => 'Backdoor',
      'firstname' => 'Benny'
    );
    $pattern->applyTo($params);
    $this->assertEquals('Backdoor',$params['lastname']);
    $this->assertEquals('Benny',$params['firstname']);
    $this->assertEquals('Benny.Backdoor',$params['login']);
    $this->assertEquals('Benny.Backdoor@test.fr',$params['email']);
    $this->assertEquals('Utilisateur Benny.Backdoor (Benny.Backdoor@test.fr)',$params['desc']);

    $params = array(
      'lastname' => 'Backdoor',
      'firstname' => 'Benny',
      'login' => 'BennyB'
    );
    $pattern->applyTo($params);
    $this->assertEquals('BennyB',$params['login']);
    $this->assertEquals('BennyB@test.fr',$params['email']);

  }

}

