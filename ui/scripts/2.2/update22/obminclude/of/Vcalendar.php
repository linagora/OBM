<?php
include_once('obminclude/of/vcalendar/Element.php');
include_once('obminclude/of/vcalendar/element/Vevent.php');
include_once('obminclude/of/vcalendar/Utils.php');
//TODO : VAlerts
//TODO : Timezone support
//TODO : ICS Writer
//TODO : VCAL Reader
//TODO : Resources items

/**
 * OBM Vcalendar Object  
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class Vcalendar {

  var $vcalendar;
  
  var $error;

  /**
   * Constructor
   * Create a vcalendar Element. //FIXME necessaire?
   * 
   * @access public
   * @return void
   */
  function Vcalendar () {
    $this->vcalendar = new Vcalendar_Element($this,'vcalendar');
  }

  /**
   * Create a new ICS element 
   * 
   * @param string $name kind of element
   * @access public
   * @return Vcalendar_Element
   */
  function & createElement($name) {
    if(class_exists('Vcalendar_Element_'.ucfirst($name))) {
      $class = 'Vcalendar_Element_'.ucfirst($name);
      return new $class($this);
    } else {
      return new Vcalendar_Element($this, strtolower($name));
    }
  }

  /**
   * Get all vevent in the document 
   * 
   * @access public
   * @return array of Vcalendar_Element
   */
  function & getVevents() {
    return $this->vcalendar->getElementByName('vevent');
  }
  
  function & getElementByUid($id) {
    return $this->vcalendar->getElementByProperty('uid',$id);
  }
  /**
   * Get all valarms in the document 
   * 
   * @access public
   * @return array of Vcalendar_Element
   */
  function & getValarms() {
    return $this->vcalendar->getElementByName('valarm');
  }
}
?>
