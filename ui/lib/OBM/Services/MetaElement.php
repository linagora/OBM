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
 * Abstract class used to describe meta-element of services
 **/
abstract class OBM_Services_MetaElement {
  protected $name;
  protected $label;
  protected $properties;

  /**
   * standard constructor
   * @param  string $name    the element name
   * @param  string $label   the displayable name (optionnal)
   * @access public
   **/
  public function __construct($name, $label = null) {
    $this->name = $name;
    $this->label = is_null($label) ? ucfirst($name) : $label;
    $this->properties = new ArrayObject();
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
    //else
    return $this->get_property($key);
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
   * property getter
   * @param  string $property
   * @access public
   * @return mixed
   **/
  public function get_property($property) {
    return $this->properties[$property];
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
    } elseif ($value instanceof OBM_Services_MetaProperty) {
      $this->set_property($key, $value);
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

  /**
   * property setter
   * @param  string                    $name          the property name
   * @param  OBM_Services_MetaProperty $property      the property object
   * @access public
   **/
  public function set_property($name, OBM_Services_MetaProperty $property) {
    $this->properties[$name] = $property;
  }

}

