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

require_once('obminclude/of/report/formater.php');
/**
 *  Permit to make a textual report based on obm database data.
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class Report {
  private $_list;

  /**
   * Constructor 
   * 
   * @access public
   * @return void
   */
  public function __construct() {
    $this->_list = array();
  }

  /**
   * add a record to the inner list 
   * 
   * @param mixed $record 
   * @access public
   * @return void
   */
  public function addRecord($record) {
    $this->_list[] = $record;
  }

  /**
   * Format inner list in the correct format
   * 
   * @param mixed $formater 
   * @access public
   * @return void
   */
  public function format($formater) {
    $output = $formater->getHeader();
    foreach($this->_list as $record) {
      $output .= $formater->format($record);
    }
    $output .= $formater->getFooter();
    return $output;
  }
}


