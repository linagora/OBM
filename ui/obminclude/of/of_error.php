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



/**
 * OBM_Error
 *
 * Singleton here is not a good idea, but the simple thing to do 
 * no to change all the codes using the $GLOBALS['err'] variable. 
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
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

