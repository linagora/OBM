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
 * Represents an ics event
 * 
 * @package Vpdi
 * @version $Id:$
 * @author David Phan <david.phan@aliasource.fr> 
 * @license GPL 2.0
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

  public function getUid() {
    return "ext-".$this->evt->getUid();
  }

  public function getSummary() {
    return $this->evt->getSummary();
  }

  public function getDescription() {
    return $this->evt->getDescription();
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

  public function isAllDay() {
    return ($this->duration >= 84600);
  }

  public function isPrivate() {
    return $this->evt->isPrivate();
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

}

?>
