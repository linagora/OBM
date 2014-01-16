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



class Vpdi_InvalidPropertyException extends Exception {}

/**
 * Base class for RFC2425 entities
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 */
class Vpdi_Entity implements ArrayAccess {
  
  protected $properties;
  
  protected $profile;
  
  /**
   * Constructor
   * 
   * @param array $properties an array of Vpdi_Property objects
   * @access public
   * @return void
   */
  public function __construct($properties = array()) {
    foreach ($properties as $p) {
      $this->addProperty($p);
    }
  }
  
  public function __get($property) {
    $get_method = 'get'.ucfirst($property);
    if (method_exists($this, $get_method)) {
      return $this->$get_method();
    }
    return $this->getValue($property);
  }
  
  public function __set($property, $value) {
    $set_method = 'set'.ucfirst($property);
    $add_method = 'add'.ucfirst($property);
    if (method_exists($this, $set_method)) {
      $this->$set_method($value);
    } elseif (method_exists($this, $add_method)) {
      $this->$add_method($value);
    } else {
      $this->addProperty(new Vpdi_Property($property, $value));
    }
  }
  
  public function __toString() {
    return "BEGIN:{$this->profile}\n"
           .Vpdi::encodeProperties($this->properties)
           ."\nEND:{$this->profile}";
  }
  
  public function offsetExists($offset) {
    return (isset($this->properties[$offset]));
  }

  public function offsetGet($offset) {
    return @$this->properties[$offset];
  }

  public function offsetSet($offset, $property) {
    if (empty($offset)) {
      $this->properties[] = $property;
    } else {
      $this->properties[$offset] = $property;
    }
  }

  public function offsetUnset($offset) {
    $this->properties[$offset] = null;
  }
  
  /**
   * Adds a property to the entity
   * 
   * @param Vpdi_Property|Vpdi_Entity $property
   * @access public
   * @return void
   */
  public function addProperty($property) {
    if (!$property instanceof Vpdi_Property && !$property instanceof Vpdi_Entity) {
      throw new Vpdi_InvalidPropertyException($property);
    }
    $this->properties[] = $property;
  }
  
  /**
   * Returns all entity properties
   * 
   * @access public
   * @return array
   */
  public function getProperties() {
    return $this->properties;
  }
  
  /**
   * Sets the profile of the entity
   * 
   * @param string $profile
   * @access public
   * @return void
   */
  public function setProfile($profile) {
    $this->profile = strtoupper($profile);
  }
  
  /**
   * Returns the profile of the entity
   * 
   * @access public
   * @return void
   */
  public function profile() {
    return $this->profile;
  }
  
  /**
   * Returns an array of all the properties named $name
   * 
   * @param string $name name of the properties
   * @access public
   * @return mixed
   */
  public function getPropertiesByName($name) {
    $properties = array();
    foreach ($this->properties as $property) {
      if ($property instanceof Vpdi_Property && $property->nameEquals($name)) {
        $properties[] = $property;
      }
    }
    return $properties;
  }
  
  /**
   * Returns the first non empty property named $name
   * 
   * @param string $name name of the property
   * @access public
   * @return the matching property, or null if no properties match
   */
  public function getProperty($name) {
    foreach ($this->getPropertiesByName($name) as $property) {
      if ($property->rawValue() != '') {
        return $property;
      }
    }
    return null;
  }
  
  /**
   * Returns the raw value (i.e. encoded) of the first non empty property named $name
   * 
   * @param string $name name of the property
   * @access public
   * @return mixed
   */
  public function getRawValue($name) {
    $property = $this->getProperty($name);
    if ($property === null) {
      return null;
    }
    return $property->rawValue();
  }
  
  /**
   * Returns the value of the first non empty property named $name
   * 
   * @param string $name name of the property
   * @access public
   * @return mixed
   */
  public function getValue($name) {
    $property = $this->getProperty($name);
    if ($property === null) {
      return null;
    }
    return $this->decodeProperty($property);
  }
  
  /**
   * Returns the value for a property
   * 
   * @param Vpdi_Property $property
   * @access private
   * @return mixed
   */
  protected function decodeProperty($property) {
    return Vpdi::decodeText($property->value());
  }
}
