<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2010 OBM.org project members team                   |
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

/**
 * Class used to describe meta-property of services
 **/
class OBM_Services_MetaProperty {
  protected $name;
  protected $label;
  protected $type;

  /**
   * standard constructor
   * @param  string $name    the property name
   * @param  string $label   the displayable name (optionnal)
   * @access public
   **/
  public function __construct($name, $label = null) {
    $this->name = $name;
    if (is_null($label)) {
      $this->label = ucfirst($name);
    }
  }

  /**
   * Standard getter
   * @param  string $key
   * @access public
   * @return mixed
   **/
  public function __get($key) {
    $getter = "get_{$key}";
    if (method_exists($this,$getter)) {
      return $this->$getter();
    }
  }

  /**
   * name getter
   * @access public
   * @return int
   **/
  public function get_name() {
    return $this->name;
  }

  /**
   * label getter
   * @access public
   * @return int
   **/
  public function get_label() {
    return $this->label;
  }

  /**
   * Standard setter
   * @param  string $key
   * @param  mixed  $value
   * @access public
   **/
  public function __set($key, $value) {
    $setter = "set_{$key}";
    if (method_exists($this,$setter)) {
      $this->$setter($value);
    }
  }

  /**
   * name setter
   * @param  string $name
   * @access public
   **/
  public function set_name($name) {
    $this->name = $name;
  }

  /**
   * label setter
   * @param  string $label
   * @access public
   **/
  public function set_label($label) {
    $this->label = $label;
  }

}

