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
 * iCalendar vEvent class
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 * @license GPL 2.0
 */
class Vpdi_Icalendar_Vevent extends Vpdi_Icalendar_Component {
  
  protected $profile = 'VEVENT';
  
  public function getDtstart() {
    return $this->getDateTime('dtstart');
  }
  
  public function setDtstart(DateTime $start) {
    $this->setDateTime('dtstart', $start);
  }
  
  public function getDtend() {
    return $this->getDateTime('dtend');
  }
  
  public function setDtend(DateTime $end) {
    $this->setDateTime('dtend', $end);
  }
  
  public function getDtstamp() {
    return $this->getDateTime('dtstamp');
  }
  
  public function setDtstamp(DateTime $stamp) {
    $this->setDateTime('dtstamp', $stamp);
  }
  
  public function getCreated() {
    return $this->getDateTime('created');
  }
  
  public function setCreated(DateTime $date) {
    $this->setDateTime('created', $date);
  }
  
  public function getLastModified() {
    return $this->getDateTime('last-modified');
  }
  
  public function setLastModified(DateTime $date) {
    $this->setDateTime('last-modified', $date);
  }
  
  public function getOrganizer() {
    if (($org = $this->getProperty('ORGANIZER')) === null) {
      return null;
    }
    return Vpdi_Icalendar_Organizer::decode($org);
  }
  
  public function setOrganizer(Vpdi_Icalendar_Organizer $organizer) {
    $this->addProperty($organizer->encode());
  }
  
  public function getAttendees() {
    $attendees = array();
    foreach ($this->getPropertiesByName('ATTENDEE') as $a) {
      $attendees[] = Vpdi_Icalendar_Attendee::decode($a);
    }
    return $attendees;
  }
  
  public function addAttendee(Vpdi_Icalendar_Attendee $attendee) {
    $this->addProperty($attendee->encode);
  }
  
  public function getCategories() {
    if (($cats = $this->getProperty('CATEGORIES')) === null) {
      return array();
    }
    return Vpdi::decodeTextList($cats->value());
  }
  
  public function setCategories(array $cats) {
    $this->addProperty(new Vpdi_Property('CATEGORIES', Vpdi::encodeTextList($cats)));
  }
  
  public function getGeo() {
    if (($geo = $this->getProperty('GEO')) === null) {
      return null;
    }
    list($lat, $lon) = Vpdi::decodeTextList($geo->value(), ';');
    return array((float) $lat, (float) $lon);
  }
  
  public function setGeo(array $geo) {
    $this->addProperty(new Vpdi_Property('GEO', Vpdi::encodeTextList($geo, ';')));
  }
  
  public function getPriority() {
    if (($prio = $this->getProperty('PRIORITY')) === null) {
      return null;
    }
    return (int) Vpdi::decodeText($prio->value());
  }
  
  public function setPriority($prio) {
    $this->addProperty(new Vpdi_Property('PRIORITY', Vpdi::encodeText($prio)));
  }
  
  public function getSequence() {
    if (($seq = $this->getProperty('SEQUENCE')) === null) {
      return 0;
    }
    return (int) Vpdi::decodeText($seq->value());
  }

  public function getSummary() {
    if (($sum = $this->getProperty('SUMMARY')) === null) {
      return 0;
    }
    return Vpdi::decodeText($sum->value());
  }

  public function getUid() {
    if (($uid = $this->getProperty('UID')) === null) {
      return 0;
    }
    return Vpdi::decodeText($uid->value());
  }

  public function getDescription() {
    if (($desc = $this->getProperty('DESCRIPTION')) === null) {
      return "";
    }
    return Vpdi::decodeText($desc->value());
  }

  public function getLocation() {
    if (($loc = $this->getProperty('LOCATION')) === null) {
      return "";
    }
    return Vpdi::decodeText($loc->value());
  }

  public function setSequence($seq) {
    $this->addProperty(new Vpdi_Property('SEQUENCE', Vpdi::encodeText($seq)));
  }
  
  public function isTransparent() {
    return $this->getValue('TRANSP') == 'TRANSPARENT';
  }

  public function isPrivate() {
    return ($this->getValue('CLASS') == "PRIVATE");
  }
  
  public function setAsTransparent() {
    $this->setProperty('TRANSP', 'TRANSPARENT');
  }

  public function getDuration() {
    $value = $this->getProperty('duration');
    if(preg_match('/\s*P((\d*)D)?(T((\d)*H)?((\d*)M)?((\d*)S)?)?((\d*)W)?$/',$value,$match)) {
      $duration = 86400 * $match[2] + 3600 * $match[5] + 60 * $match[7] + $match[9] +604800 * $match[11] ;
    } 
    return $duration;
  }
}
