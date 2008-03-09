<?php

include_once('obminclude/of/vcalendar/Element.php');

/**
 * Vcalendar_Element_Vevent
 *
 * @package
 * @version $Id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr>
 * @license GPL 2.0
 */
class Vcalendar_Element_Vevent extends Vcalendar_Element {


  /**
   * Vcalendar_Element_Vevent
   *
   * @param mixed $document
   * @access public
   * @return void
   */
  function Vcalendar_Element_Vevent(&$document) {
    $this->document = &$document;
    $this->name = vevent;
  }


  function setDtstart($value) {
    $this->dtstart = $value;
    if(isset($this->dtend) && !isset($this->duration)) {
      $this->set('duration', strtotime($this->dtend) - strtotime($this->dtstart));
    }
  }

  function setDtend($value) {
    $this->dtend = $value;
    if(isset($this->dtstart) && !isset($this->duration)) {
      $this->set('duration', strtotime($this->dtend) - strtotime($this->dtstart));
    }
  }

  function setDuration($value) {
    $this->duration = $value;
    if(isset($this->duration) && isset($this->dtend)) {
      $this->set('dtend', gmdate('Y-m-d H:i:s',strtotime($this->dtstart) + $this->duration));
    }
  }
    
  function isAllDay() {
    if(date('His', $this->date) == '000000' && date('His', $this->date + $this->duration) == '000000') {
      return true;
    }
    if($this->duration == '0') {
      return true;
    }
    if($this->xObmAllDay == 1) {
      return true;
    }
    return false;
  }

}

?>
