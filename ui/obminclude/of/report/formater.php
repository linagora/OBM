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
 * Define how an element will be outputed 
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
interface IFormater {

  /**
   * Format the current object 
   * 
   * @param mixed $object 
   * @access public
   * @return void
   */
  public function format($record);
  /**
   * get output header 
   * 
   * @access public
   * @return void
   */
  public function getHeader();
  /**
   * get output footer
   * 
   * @access public
   * @return void
   */
  public function getFooter();

}

/**
 * Define a generic format, this formater will display inline
 * all the fields of the object which have been added
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class GenericFormater implements IFormater{
  private $_fields;
  
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
    $line = '';
    foreach($this->_fields as $field) {
       $line .= self::escapeField($object->$field).";";
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
      if(isset(${"l_".$field}))
        $head .= self::escapeField(${"l_".$field}).";";
      else
        $head .= self::escapeField($field).";";
    }
    $head .= "\n";
    return $head;
  }

  /**
   * @see IFormater::getFooter
   */
  public function getFooter() {
    return '';
  }

  /**
   * Escape a field
   * 
   * @param mixed $field 
   * @static
   * @access private
   * @return void
   */
  protected static function escapeField($field) {
    return '"'.addcslashes($field,";\\\"").'"';
  }
}
