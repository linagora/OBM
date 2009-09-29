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
 * OBM_Error
 *
 * Singleton here is not a good idea, but the simple thing to do 
 * no to change all the codes using the $GLOBALS['err'] variable. 
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class OBM_Error {

  const ERROR            = 'error';

  const WARNING          = 'warning';
  
  private static $singleton;

  private $errors;


  public static function getInstance() {
    if( self::$singleton === NULL ) {
      self::$singleton = new self();
    }
    return self::$singleton;
  }

  /**
   * Add an message to the error stack 
   * 
   * @param mixed $field : field  id or name in error
   * @param mixed $message : field  id or name in error
   * @param mixed $level : field  id or name in error
   * @access public
   * @return void
   */
  public function add($field, $message, $level) {
    $this->errors[$level][$field] = $message;
  } 

  /**
   * Add an warning to the error stack
   * 
   * @param mixed $field 
   * @param mixed $message 
   * @access public
   * @return void
   */
  public function addError($field, $message) {
    $this->add($field, $message, self::ERROR);
  } 

  /**
   * Add an warning to the error stack 
   * 
   * @param mixed $field : field  id or name in error
   * @param mixed $message : field  id or name in error
   * @access public
   * @return void
   */
  public function addWarning($field, $message) {
    $this->add($field, $message, self::WARNING);
  } 

  /**
   * toJson 
   * 
   * @access public
   * @return void
   */
  public function toJson() {
    return json_encode($this->errors);
  }

  /**
   * return true if any error is stored 
   * 
   * @access public
   * @return void
   */
  public function inError() {
    return (count($this->errors['error']) > 0);
  }
}

