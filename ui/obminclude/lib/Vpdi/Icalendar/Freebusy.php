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
 * Represents a freebusy period
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 * @license GPL 2.0
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
  
  public function __construct(DateTime $start, DateTime $end) {
    $this->start = $start;
    $this->end = $end;
    $this->duration = $this->end->format('U') - $this->start->format('U');
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

  public function isAllDay() {
    return ($this->duration >= 84600 || $this->end->format('d') != $this->start->format('d'));
  }

  public function isPrivate() {
    return false;
  }

  public function match($pattern) {
    return $pattern == strtolower(__("Busy"));
  }
}
