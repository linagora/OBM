<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

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
 * iCalendar vEvent class
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 */
class Vpdi_Icalendar_Vevent extends Vpdi_Icalendar_Component {
  
  protected $profile = 'VEVENT';
  private $frequency = array("secondly","minutely","hourly","daily","weekly","monthly","yearly");
  private $repeat = array("bysecond","byminute","byhour","byday","bymonthday","byyearday","byweekno","bymonth","bysetpos","wkst");
  private $weekDays = array('su' => 'sunday', 'mo' => 'monday', 'tu' => 'tuesday', 'we' => 'wednesday', 'th' => 'thursday' , 'fr' => 'friday', 'sa' => 'saturday');

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
      return "ext-".sha1(uniqid());
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
  
  public function isConfidential() {
    return ($this->getValue('CLASS') == "CONFIDENTIAL");
  }

  public function setAsTransparent() {
    $this->setProperty('TRANSP', 'TRANSPARENT');
  }

  public function getDuration() {
    $value = $this->getProperty('duration');
    if(preg_match('/\s*P((\d*)D)?(T((\d*)H)?((\d*)M)?((\d*)S)?)?((\d*)W)?$/',$value,$match)) {
      $duration = 86400 * $match[2] + 3600 * $match[5] + 60 * $match[7] + $match[9] +604800 * $match[11] ;
    } 
    return $duration;
  }

  public function getRecurrenceId() {
	$recurrenceIdDate = $this->getDateTime('RECURRENCE-ID');
	if ( $recurrenceIdDate === null) {
		return '';
	}
	  return $recurrenceIdDate;
  }
  
   public function getExceptionRecurrenceId() {
	$recurrenceId = $this->getProperty('RECURRENCE-ID');
	if ( $recurrenceId === null) {
		return '';
	}
	return Vpdi::decodeText($recurrenceId->value());
  }

  public function getExceptionRecurrenceIdProperty() {
	$recurrenceIdProperty= $this->getPropertiesByName('RECURRENCE-ID');
	if ( $recurrenceIdProperty  === null ) {
		  return '';
	}
	return $recurrenceIdProperty;
  }
  
  public function getCanceledExceptionExtEvents() {
	$cancelException = $this->getPropertiesByName('EXDATE');
	if ( $cancelException === null ) {
		  return '';
	}
	return $cancelException;
  }
  
  public function getMovedExceptionExtEvents() {
	$movedException = $this->getPropertiesByName('DTSTART') ;
	$recurrenceId =  $this->getExceptionRecurrenceId();
	if ( $recurrenceId  === null ) {
	  return '';
	}
	return $movedException;
  }
  
  public function extractDateValue(){
	$value_dtstart = $this[0]->value();
	$str_date = strtotime( $value_dtstart);
	$date = new Of_Date($str_date, 'GMT');
	$date_value = $date->getOutputDateTime();
	return  $date_value;
  }
  
  public function getRrule() {
    if (($rrule = $this->getProperty('RRULE')) === null) {
      return null;
    }
    $data = Vpdi::decodeTextList($rrule->value(), ';');
    foreach($data as $d) {
      list($name, $value) = explode('=', $d);
      $name = strtolower($name);
      $property[$name]=$value;
    }
    $freq = strtolower($property['freq']);
    if (!in_array($freq, $this->frequency)) return null;

    $return = array();

    switch ($freq)  {
      case 'weekly':
        $days = '0000000';
        if(!is_null($property['byday'])) {
          $byday = Vpdi::decodeTextList($property['byday']);
          foreach($byday as $day) {
            $index = date('w', strtotime($this->weekDays[strtolower($day)]));
            $days[$index] = '1';
          }
        }
        $return['repeat_days'] = $days;
        break;
      case 'monthly' :
        if(!is_null($property['byday'])) {
          $freq = 'monthlybyday';
        } else {
          $freq = 'monthlybydate';
        }
        break;
    }

    $return['freq'] = $freq;
    if(is_numeric($property['interval'])) {
      $return['interval'] = $property['interval'];
    } else {
      $return['interval'] = 1;
    }

    if(!is_null($property['count'])) {
      $return['count'] = $property['count'];
    } elseif(!is_null($property['until'])) {
      $return['until'] = Vpdi::decodeDate($property['until']);
    }

    // Exceptions
    $return['exdates'] = null;
    if (($exdates = $this->getPropertiesByName('EXDATE')) != null) {
      foreach($exdates as $exdate) {
        $return['exdates'][] = new Of_Date($exdate->value());
      } 
    }
    return $return;
  }
}
