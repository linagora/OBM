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
 * iCalendar vFreeBusy class
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
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