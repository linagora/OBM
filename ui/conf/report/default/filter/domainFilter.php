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

class domainFilter implements IFilter {
  private $_value;
  private $_filter_cmd;

  /**
   * Constructor 
   * 
   * @param string $field Field to test
   * @param string $operator Operation : =, <, >, <=,>=, !=
   * @param mixed $value value to test with, the accepted type are boolean, string, integer, float
   * @access public
   * @return void
   */
  public function __construct() {
    $this->_value = $GLOBALS['params']['domain_id'];

    $operator = '==';

    $field = 'domain_id';

    $this->_filter_cmd = "return (\$record->". $field ." $operator \$this->_value);";
  }

  /**
   * @see IFilter::filter
   */
  public function filter($record) {
    if($this->_value != '') {
      return eval($this->_filter_cmd);
    } else {
      return true;
    }
  }

}
