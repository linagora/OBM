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
 * Represents a freebusy period
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 */
  
class Vpdi_Icalendar_Freebusy {
  
  const FREE = 'FREE';
  
  const BUSY = 'BUSY';
  
  const BUSY_UNAVAILABLE = 'BUSY-UNAVAILABLE';
  
  const BUSY_TENTATIVE = 'BUSY-TENTATIVE';
  
  public $start;
  
  public $end;
  
  public $duration;
  
  public $type;

  public static function decode(Vpdi_Property $FREEBUSY) {
    $fbs = array();
    $periods = Vpdi::decodeTextList($FREEBUSY->value());
    foreach ($periods as $p) {
      list($start, $end) = Vpdi::decodePeriod($p);
      $fbs[] = new Vpdi_Icalendar_Freebusy($start, $end);
    }
    if (count($fbs) == 1) return $fbs[0];
    return $fbs;
  }
  
  public function __construct(Of_Date $start, Of_Date $end) {
    $this->start = clone $start;
    $this->end = clone $end;
    $this->duration = $this->end->diffTimestamp($this->start);
    $this->type = self::BUSY;
  }
  
  public function __toString() {
    return $this->encode()->__toString();
  }
  
  public function encode() {
    return new Vpdi_Property('FREEBUSY', Vpdi::encodePeriod($this->start, $this->end), array('FBTYPE' => $this->type));
  }

  public function getUid() {
    return "ext-".sha1(uniqid());
  }

  public function getSummary() {
    return __("Busy");
  }

  public function getDescription() {
    return __("Busy");
  }

  public function getDuration() {
    return $this->duration;
  }

  public function getLocation() {
    return "";
  }

  public function getTransparency() {
    return "OPAQUE";
  }

  public function getPartStat() {
    return "ACCEPTED";
  }

  public function getCategory() {
    return "";
  }

  public function getRecurrenceId() {
    return null;
  }

  public function isAllDay() {
    return ($this->duration >= 84600);
  }

  public function isPrivate() {
    return false;
  }

  public function match($pattern, $type) {
    if ($type == 'basic') {
      return in_array(strtolower(__("Busy")), explode(" ", strtolower($pattern)));
    }
    if ($type == 'advanced') {
      $r = true;
      $summary = strtolower($pattern['summary']);
      if (!empty($summary)) {
        $r = $r && in_array(strtolower($this->getSummary()), explode(" ", $summary));
      }

      $location = strtolower($pattern['location']);
      if (!empty($location)) {
        $r = $r && in_array(strtolower($this->getLocation()), explode(" ", $location));
      }

      $desc = strtolower($pattern['desc']);
      if (!empty($desc)) {
        $r = $r && in_array(strtolower($this->getDescription()), explode(" ", $desc));
      }

      return $r;
    }
  }

  public function getRrule() {
    return false;
  }
}
