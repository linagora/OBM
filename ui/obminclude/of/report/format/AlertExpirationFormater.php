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

/**
 * AlertExpirationFormater 
 * 
 * @uses IFormater
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2009 LINAGORA GSO
 * @author Benoît Caudesaygues <benoit.caudesaygues@linagora.com> 
 * @license GPL 2.0
 */
class AlertExpirationFormater implements IFormater{
  private $_fields;
  private $delaijour = 10;
  
  /**
   * Constructor 
   * 
   * @access public
   * @return void
   */
  public function __construct() {
    $this->_fields = array();
  }

  /**
   * Add $field to the field to format 
   * 
   * @param mixed $field 
   * @access public
   * @return void
   */
  public function addField($field) {
    $this->_fields[] = $field;
  }

  /**
   *  
   * @see IFormater::format 
   **/
  public function format($object) {
    global $l_warn_exp;
    $line = '';
    foreach($this->_fields as $field) {
       $line .=$object->$field."\t";
    }
    $date_limit = date('Y-m-d',mktime(0, 0, 0, date("m")  , date("d")+$this->delaijour, date("Y")));
    if($object->account_dateexp != '' && ($object->account_dateexp < $date_limit)) {
      $line .= $l_warn_exp."\t";
    } else {
      $line .="\t";
    }
    $line .= "\n";
    return $line;
  }

  /**
   * @see IFormater::getHeader
   */
  public function getHeader() {
    require "obminclude/lang/".$_SESSION['set_lang']."/report.inc";
    $head = '';
    foreach($this->_fields as $field) {
      $head .= ${"l_".$field}."\t";
    }
    $head .= $l_warn."\t";
    $head .= "\n";
    return $head;
  }

  /**
   * @see IFormater::getFooter
   */
  public function getFooter() {
    return '';
  }

}
