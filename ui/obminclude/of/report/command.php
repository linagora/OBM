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

require_once('obminclude/of/report/reportFactory.php');
require_once('obminclude/of/report/sender.php');
/**
 * Abstract class used to describe program actions. Must be inherited for
 * each report to generate.
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2009 Aliasource - Groupe LINAGORA
 * @author Vincent Alquier <vincent.alquier@aliasource.fr> 
 * @license GPL 2.0
 */
abstract class Command {
  private $sender;

  /**
   * Called by the client. launch setup(), then execute() and then tearDown()
   *
   * @access public
   * @return void
   */
  public function doIt() {
    setup();
    execute();
    tearDown();
  }

  /**
   * Define default values (like sender to use).
   *
   * @access private
   * @return void
   */
  private function setup() {
  }

  /**
   * Main function of the programm.
   *
   * @access private
   * @return void
   */
  abstract protected function execute();

  /**
   * send the report (call this->sender->send()).
   *
   * @access private
   * @return void
   */
  private function tearDown() {
    if (isset($this->sender))
      $this->sender->send();
  }

}
