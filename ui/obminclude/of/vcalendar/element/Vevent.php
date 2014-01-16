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



include_once('obminclude/of/vcalendar/Element.php');

/**
 * Vcalendar_Element_Vevent
 *
 * @package
 * @version $Id:$
 * @copyright Copyright (c) 1997-2007 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr>
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
    if(isset($this->dtend)) {
      $this->set('duration', $this->dtend->diffTimestamp($this->dtstart));
    } elseif(!isset($this->duration)) {
      $this->duration = 86400;
    }
  }

  function setDtend($value) {
    $this->dtend = $value;
    if(isset($this->dtstart)) {
      $this->duration =  $this->dtend->diffTimestamp($this->dtstart);
    }
  }

  function setDuration($value) {
    $this->duration = $value;
    if(isset($this->dtstart)) {
      $dtend = clone $this->dtstart;
      $this->dtend = $dtend->addSecond($this->duration);
    }
  }
    
  function setXObmAlert($value) {
    $name = 'x-obm-alert'; 
    if(!is_array($this->$name)) $this->$name = array();
    array_push($this->$name,$value);
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
