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
      $this->set('duration', $this->dtend->diffTimestamp($this->dtstart));
    }
  }

  function setDtend($value) {
    $this->dtend = $value;
    if(isset($this->dtstart) && !isset($this->duration)) {
      $this->set('duration', $this->dtend->diffTimestamp($this->dtstart));
    }
  }

  function setDuration($value) {
    $this->duration = $value;
    if(isset($this->dtstart) && !isset($this->dtend)) {
      $dtend = clone $this->dtstart;
      $this->set('dtend', $dtend->addSecond($this->duration));
    }
  }
    
  function isAllDay() {
    if($this->get('x-obm-all-day') == 1) {
      return true;
    }
    
    // if(date('His', strtotime($this->dtstart)) == '000000' && date('His', strtotime($this->dtstart + $this->duration)) == '000000') {
    $start = $this->dtstart;
    $end = clone $start;
    $end = $end->addSecond($this->duration);
    
    if ($start->getHour() == 0 && $start->getMinute() == 0 && $start->getSecond() == 0 &&
        $end->getHour() == 0 && $end->getMinute() == 0 && $end->getSecond() == 0
      ) { 
      return true;
    }
    if($this->duration == '0') {
      return true;
    }
    return false;
  }

}

?>
