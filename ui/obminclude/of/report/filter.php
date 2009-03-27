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
 * Interface for fitering elements. 
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
interface IFilter {

  /**
   * Test if the element match the IFilter filter 
   * 
   * @param mixed $record 
   * @access public
   * @return boolean
   */
  public function filter($record);
}


/**
 * Define a generic filter, this filter will be build with
 * a field name, an operator and a value, and will filter record
 * using if($rescod->$filed $operator $value) return true;
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class GenericFilter {
  private $_field;
  private $_operator;
  private $_value;

  /**
   * Constructor 
   * 
   * @param mixed $field Field to test
   * @param mixed $operator Operation : =, <, >, <=,>=, !=
   * @param mixed $value value to test with, the accepted type are boolean, string, integer, float
   * @access public
   * @return void
   */
  public function __construct($field, $operator, $value) {
    $this->_field = $field;

    $this->_operator = $operator;
    $this->_value = $value;
  }

  public function filter($record) {
    return true;
  }
}
