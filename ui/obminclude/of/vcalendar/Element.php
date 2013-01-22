<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

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

include_once('obminclude/of/of_dynamicmethod.php');

/**
 * Vcalendar Element
 *
 * @package
 * @version $Id:$
 * @copyright Copyright (c) 1997-2007 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr>
 */
class Vcalendar_Element {
  
  private static $methodCache = array();

  var $document = NULL;

  var $name;

  var $children = array();

  public $private = true;

  /**
   * Vcalendar_Element
   *
   * @param mixed $document
   * @param mixed $name
   * @access public
   * @return void
   */
  function Vcalendar_Element(&$document, $name) {
    $this->document = &$document;
    $this->name = $name;
    $this->private;
  }


  /**
   * setProperties
   *
   * @access public
   * @return void
   */
  function setProperties($properties) {
    if(is_array($properties)) {
      foreach($properties as $property ) {
        if(!is_null($property['name']) && !is_null($property['value'])) {
          $this->set($property['name'],$property['value']);
        }
      }
    }
  }

  /**
   * set
   *
   * @param mixed $name
   * @param mixed $values
   * @param mixed $options
   * @access public
   * @return void
   */
  function set($name, $value) {
    $method = Vcalendar_Element::$methodCache[$name];
    
    if (isset($method)) {
      if ($method->exists) {
        $methodName = $method->methodName;
        
        $this->$methodName($value);
      } else {
        $this->setProperty($name, $value);
      }
    } else {
      $methodName = 'set'.str_replace(' ','',ucwords(str_replace('-',' ',$name)));
      
      if(method_exists($this, $methodName)) {
        Vcalendar_Element::$methodCache[$name] = new DynamicMethod($methodName, true);
        $this->$methodName($value);
      } else {
        Vcalendar_Element::$methodCache[$name] = new DynamicMethod($methodName, false);
        $this->setProperty($name, $value);
      }
    }
  }
  /**
   * set
   *
   * @param mixed $name
   * @param mixed $values
   * @param mixed $options
   * @access public
   * @return void
   */
  function reset($name) {
    unset($this->$name);
  }

  function setProperty($name,$value) {
    if(isset($this->$name)) {
      if(!is_array($this->$name) || !is_int(key($this->$name))) {
        $this->$name =  array($this->$name);
      }
      array_push($this->$name,$value);
    } else {
      $this->$name = $value;
    }
  }

  /**
   * getProperty
   *
   * @param mixed $name
   * @param mixed $values
   * @param mixed $options
   * @access public
   * @return void
   */
  function get($name) {
    $methodName = 'get'.str_replace(' ','',ucwords(str_replace('-',' ',$name)));
    if(method_exists($this, $methodName)) {
      $this->$methodName($value, $options);
    } else {
      return $this->$name;
    }
  }

  /**
   * getDocument
   *
   * @access public
   * @return void
   */
  function & getDocument() {
    return $this->document;
  }


  /**
   * appendChild
   *
   * @param mixed $child
   * @access public
   * @return void
   */
  function appendChild(&$child) {
    $this->children[] = &$child;
  }

  /**
   * getElementByName
   *
   * @param mixed $name
   * @param mixed $recursive
   * @access public
   * @return void
   */
  function & getElementByName($name, $recursive=false) {
    $elements = array();
    foreach($this->children as $child) {
      if($child->name == $name) {
        array_push($elements,$child);
      }
      if($recursive) {
        $return = $child->getElementbyName($name, $recursive);
        $elements = array_merge($elements,$return);
      }
    }
    return $elements;
  }

  function & getElementByProperty($name, $value, $recursive=false) {
    $elements = array();
    foreach($this->children as $child) {
      if(!is_null($child->get($name))) {
        $propertyValue = $child->get($name);
        if ((!is_array($propertyValue) || !is_int(key($propertyValue))) && $propertyValue == $value) {
          array_push($elements,$child);
        } else if(is_array($propertyValue) && in_array($value,$propertyValue)) {
          array_push($elements,$child);
        }
      }
    }
    if($recursive) {
      $return = $child->getElementByProperty($name, $value, $recursive);
      $elements = array_merge($elements,$return);
    }
    return $elements;
  }
}
?>
