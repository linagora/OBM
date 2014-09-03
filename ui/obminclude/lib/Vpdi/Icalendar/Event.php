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



/**
 * Represents an ics event
 * 
 * @package Vpdi
 * @version $Id:$
 * @author David Phan <david.phan@aliasource.fr> 
 */

class Vpdi_Icalendar_Event {

  public $evt;
  public $start;
  public $end;
  public $duration;

  public function __construct($vevent) {
    $this->evt = $vevent;
    $this->start = $vevent->getDtstart();
    $this->end = $vevent->getDtend();
    $this->duration = $vevent->getDuration();
    if(isset($this->end)) {
      $this->duration = $this->end->diffTimestamp($this->start);
    } elseif(!isset($this->duration)) {
      $this->duration = 86400;
    }
    $this->end = new Of_Date($this->start->format('U') + $this->duration);
  }

  public function getRecurrenceId() {
    return $this->evt->getRecurrenceId();
  }

  public function getUid() {
    return "ext-".$this->evt->getUid();
  }

  public function getSummary() {
    return $this->evt->getSummary();
  }
  
  public function getAttendees() {
    return $this->evt->getAttendees();
  }
  
  public function getPriority() {
	  return $this->evt->getPriority();
  }

  public function getDescription() {
    return $this->evt->getDescription();
  }
  
  public function getDtstart() {
	return $this->evt->getDtstart();
  }

  public function getDuration() {
    return $this->duration;
  }

  public function getLocation() {
    return $this->evt->getLocation();
  }

  public function getTransparency() {
    if ($this->evt->isTransparent()) {
      return "TRANSPARENT";
    }
    return "OPAQUE";
  }

  public function getPartStat() {
    return "ACCEPTED"; // FIXME
  }

  public function getCategory() {
    return $this->evt->getCategories();
  }
  
  public function getOrganizer() {
    return $this->evt->getOrganizer();
  }

  public function isAllDay() {
    return ($this->duration >= 84600);
  }

  public function isPrivate() {
    return $this->evt->isPrivate();
  }

  public function isConfidential() {
    return $this->evt->isConfidential();
  }

  public function getPrivacy() {
    switch ($this->evt->getValue('CLASS')) {
      case 'PUBLIC':
        return 0;
      case 'PRIVATE':
        return 1;
      case 'CONFIDENTIAL':
        return 2;
    }
  }

  public function match($pattern, $type) {
    if ($type == "basic") {
      $p = explode(" ", $pattern);
      return (
        array_intersect(explode(" ", strtolower($this->getSummary())), $p) || 
        array_intersect(explode(" ", strtolower($this->getLocation())), $p) || 
        array_intersect(explode(" ", strtolower($this->getDescription())), $p)
        );
    } elseif ($type == "advanced") {
      $r = true; 
      $summary = strtolower($pattern['summary']);
      if (!empty($summary)) {
        $r = $r && array_intersect(explode(" ", strtolower($this->getSummary())), explode(" ", $summary));
      }

      $location = strtolower($pattern['location']);
      if (!empty($location)) {
        $r = $r && array_intersect(explode(" ", strtolower($this->getLocation())), explode(" ", $location));
      }

      $desc = strtolower($pattern['desc']);
      if (!empty($desc)) {
        $r = $r && array_intersect(explode(" ", strtolower($this->getDescription())), explode(" ", $desc));
      }

      return $r;
    }
  }

  public function getRrule() {
    return $this->evt->getRrule();
  }

  /**
   * Returns the original RRULE as a dictionary of strings
   *
   * @return string[] The RRULE, as a dictionary of key => values
   */
  public function getRealRrule() {
    return $this->evt->getRealRrule();
  }

}

?>
