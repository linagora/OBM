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
 * iCalendar vFreeBusy class
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 * @license GPL 2.0
 */
class Vpdi_Icalendar_Vfreebusy extends Vpdi_Icalendar_Component {
  
  protected $profile = 'VFREEBUSY';
  
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
  
  public function getFreebusys() {
    $fbs = array();
    foreach ($this->getPropertiesByName('FREEBUSY') as $f) {
      $period = Vpdi_Icalendar_Freebusy::decode($f);
      if (is_array($period)) {
        $fbs = array_merge($fbs, $period);
      } else {
        $fbs[] = $period;
      }
    }
    return $fbs;
  }
}