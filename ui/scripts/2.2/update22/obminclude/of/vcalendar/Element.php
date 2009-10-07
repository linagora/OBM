<?php
/**
 * Vcalendar Element
 *
 * @package
 * @version $Id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr>
 * @license GPL 2.0
 */
class Vcalendar_Element {

  var $document = NULL;

  var $name;

  var $children = array();

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
    $methodName = 'set'.str_replace(' ','',ucwords(str_replace('-',' ',$name)));
    if(method_exists($this, $methodName)) {
      $this->$methodName($value);
    } else {
      $this->setProperty($name, $value);
    }
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
