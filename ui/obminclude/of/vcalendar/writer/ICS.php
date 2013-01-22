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



include_once('obminclude/of/Vcalendar.php');
include_once('obminclude/of/of_dynamicmethod.php');

class Vcalendar_Writer_ICS {
  
  private static $methodCache = array();

  var $parsed_event;

  var $buffer;

  var $attendees;

  var $noreply;

  function Vcalendar_Writer_ICS() {
    $this->buffer = '';
    $this->attendees = array('user' => array(), 'resource' => array(), 'contact' => array(), 'group' => array());
    $this->noreply = get_entity_email('noreply'); 
  }

  /**
   * @param Vcalendar $document
   */
  function writeDocument(&$document) {
    $vcalendar = &$document->vcalendar;
    $this->buffer .= 'BEGIN:'.$this->parseName($vcalendar->name)."\r\n";
    $properties = get_object_vars($vcalendar);
    $name = $properties['name'];
    unset($properties['name']);
    unset($properties['document']);
    unset($properties['children']);
    unset($properties['private']);
    foreach ($properties as $name => $value) {
      $this->writeProperty($name, $value);
    }
    $vevents = &$document->getVevents();
    for($i=0; $i < count($vevents); $i++ ) {
      $this->parsed_event = $vevents[$i];
      $this->writeVevent($vevents[$i]);
      $this->parsed_event = null;
    }
    $valarms = &$document->getValarms();
    for($i=0; $i < count($valarms); $i++) {
      $this->writeValarm($valarms[$i]);
    }
    $this->buffer .= 'END:'.$this->parseName($vcalendar->name)."\r\n";    
  }

  function writeVevent(&$vevent) {
    $this->buffer .= 'BEGIN:'.$this->parseName($vevent->name)."\r\n";
    $properties = get_object_vars($vevent);
    $name = $properties['name'];
    unset($properties['name']);
    unset($properties['document']);
    unset($properties['children']);
    unset($properties['private']);
    unset($properties['dtend']);
    foreach ($properties as $name => $value) {
      $this->writeProperty($name, $value);
    }

    if ( ! array_key_exists("dtstamp",$properties) ) {
        $this->writeProperty('dtstamp', new Of_Date());
    }
    $this->buffer .= 'END:'.$this->parseName($vevent->name)."\r\n";
  }

  function writeProperty($name,$value) {
    $method = Vcalendar_Writer_ICS::$methodCache[$name];
    
    if (isset($method)) {
      if ($method->exists) {
        $methodName = $method->methodName;
        
        $this->$methodName($name,$value);
      } else {
        $this->buffer .= $this->parseProperty($this->parseName($name). ":".$this->parseText($value)) . "\r\n";
      }
    } else {
      $methodName = 'write'.str_replace(' ','',ucwords(str_replace('-',' ',$name)));
      
      if(method_exists($this, $methodName)) {
        Vcalendar_Writer_ICS::$methodCache[$name] = new DynamicMethod($methodName, true);
        $this->$methodName($name,$value);
      } else {
        Vcalendar_Writer_ICS::$methodCache[$name] = new DynamicMethod($methodName, false);
        $this->buffer .= $this->parseProperty($this->parseName($name). ":".$this->parseText($value)) . "\r\n";
      }
    }
  }

  function writeRecurrenceId($name, $value)
  {
	$result = "RECURRENCE-ID:" . $this->parseDate($value) . "\r\n";
	$this->buffer .= $result;
  }

  function parseName($name) {
    return strtoupper($name);
  }

  function parseText($text) {
    $text = addcslashes($text,"\;,\r\n");
    return $text;
  }

  function parseProperty($property) {
    return trim(mb_chunk_split($property,74,"\r\n "));
  }

  function writeDtstart($name, $value) {
    $this->writeBoundDate($name, $value);
  }

  function writeDtend($name, $value) {
    $this->writeBoundDate($name, $value);
  }

  function writeCreated($name, $value) {
    $this->buffer .= $this->parseProperty($this->parseName($name). ":".$this->parseDate($value));
    $this->buffer .= "\r\n";      
  }

  function writeLastModified($name, $value) {
    $this->buffer .= $this->parseProperty($this->parseName($name). ":".$this->parseDate($value));
    $this->buffer .= "\r\n";      
  }

  function writeDtstamp($name, $value) {
    $this->buffer .= $this->parseProperty($this->parseName($name). ":".$this->parseDate($value));
    $this->buffer .= "\r\n";      
  }

  function writeOrganizer($name, $value) {
    // OBMFULL-2980
    // This will share the cache with the attendees, which is an added benefit
    // the primary benefit being we don't call get_user_info for every VEVENT !
    if(!$this->attendees['user'][$value]) {
      $this->attendees['user'][$value] = get_user_info($value);
    }
    $userInfo = $this->attendees['user'][$value];
    $params[] = $this->parseName('x-obm-id').'='.$value;
    $params[] = 'CN='.$this->parseText($userInfo['firstname'].' '.$userInfo['lastname']);
    if(!$userInfo['email']) $userInfo['email'] = $this->noreply ; 
    $value =  'MAILTO:'.$this->parseText($userInfo['email']);
    $property = $this->parseProperty($this->parseName($name).';'.implode(';',$params).':'.$value);
    $this->buffer .= $property."\r\n";      
  }
  
  function writeAttach($name, $value) {
    foreach ((array) $value as $uri) {
      $this->buffer .= $this->parseProperty('ATTACH:'.$uri)."\r\n";
    }
  }

  function writeAttendee($name, $value) {
    if (key_exists('state', $value)) {
      $value = array($value); 
    }
    foreach ($value as $attendee) {
      $params = array();
      $value = '';
      if($attendee['state']) {
        $partstat = $attendee['state'];
      } else {
        $partstat = 'NEEDS-ACTION';
      }

      switch($attendee['entity']) {
      case 'user' :
        if(!$this->attendees['user'][$attendee['id']]) {
          $this->attendees['user'][$attendee['id']] = get_user_info($attendee['id']);
        }
        $userInfo = $this->attendees['user'][$attendee['id']];
        if(!$userInfo['email']) $userInfo['email'] = $this->noreply ; 
        $value =  'MAILTO:'.$this->parseText($userInfo['email']);
        $params[] = 'CUTYPE=INDIVIDUAL';
        $params[] = 'CN='.$this->parseText($userInfo['firstname'].' '.$userInfo['lastname']);
        $params[] = 'PARTSTAT='.$partstat;
        $params[] = $this->parseName('x-obm-id').'='.$attendee['id'];
        break;
      case 'resource' :   
        if(!$this->attendees['resource'][$attendee['id']]) {
          $this->attendees['resource'][$attendee['id']] = get_entity_info($attendee['id'],'resource');
        }
        $resourceInfo = $this->attendees['resource'][$attendee['id']];
        $value =  'MAILTO:'.$this->parseText($this->noreply );
        $params[] = 'CUTYPE=RESOURCE';
        $params[] = 'CN='.$this->parseText($resourceInfo['label']);          
        $params[] = 'PARTSTAT='.$partstat;
        $params[] = $this->parseName('x-obm-id').'='.$attendee['id'];
        break;
      case 'group' :
        if(!$this->attendees['group'][$attendee['id']]) {
          $this->attendees['group'][$attendee['id']] = get_group_info($attendee ['id']);
        }
        $groupInfo = $this->attendees['contact'][$attendee['id']];        
        if(!$groupInfo['email']) $groupInfo['email'] = $this->noreply ; 
        $value = 'MAILTO:'.$this->parseText($groupInfo['email']);
        $params[] = 'CUTYPE=GROUP';
        $params[] = 'CN='.$this->parseText($groupInfo['name']);
        $params[] = 'PARTSTAT='.$partstat;
        $params[] = $this->parseName('x-obm-id').'='.$attendee['id'];
        break;
      case 'task' :
        $mail =  'MAILTO:'.$this->parseText($this->noreply );
        $params[] = 'CUTYPE=X-TASK';          
        $params[] = 'PARTSTAT=ACCEPTED';
        $params[] = $this->parseName('x-obm-id').'='.$attendee['id'];
        break;
      case 'contact' :
        if(!$this->attendees['contact'][$attendee['id']]) {
          $this->attendees['contact'][$attendee['id']] = get_contact_from_ids(array($attendee['id']));
        }
        $contactInfo = $this->attendees['contact'][$attendee['id']];          
        $params[] = 'CUTYPE=INDIVIDUAL';
        if(!$contactInfo['entity'][ $attendee['id'] ]['email_address'])$contactInfo['entity'][ $attendee['id'] ]['email_address'] = $this->noreply ;
        $value = 'MAILTO:'.$this->parseText($contactInfo['entity'][ $attendee['id'] ]['email_address']);
        break;

      default:
        // Skip $params[]'less entities
        continue 2; // This 'continue' is catched by switch(). With '2' we reach our foreach().
      } 

      $property = $this->parseProperty($this->parseName($name).';'.implode(';',$params).':'.$value);
      $this->buffer .= $property."\r\n";
    }
  }

  function writeRrule($name, $value) {
    $params[] = 'INTERVAL='.strtoupper($value['interval']);
    $params[] = 'FREQ='.strtoupper($value['kind']);
    if ($value['until'])
      $params[] = 'UNTIL='.$this->parseDate($value['until']);
    if(!is_null($value['byday'])) {
      $params[] = 'BYDAY='.strtoupper(implode(',',$value['byday']));
    }
    $property = $this->parseProperty($this->parseName($name).':'.implode(';',$params));
    $this->buffer .= $property."\r\n";
  }

  function writeCategories($name, $value) {
    $this->buffer .= $this->parseProperty($this->parseName($name).':'.implode(',',$value))."\r\n";
  }

  function writeDuration($name, $seconds) {
    // With all-day events we refers to the bound dates rather than the duration
    if($this->parsed_event->isAllDay()) {
      $nb_days = (int) ($seconds / 86400);
      if($nb_days > 1) {
        $dtend = clone $this->parsed_event->get('dtstart');
        $dtend->addSecond($seconds);
        $this->writeDtend('dtend', $dtend);
      }
    } else {
      $this->buffer .= $this->parseProperty($this->parseName($name).':'.$this->secondsToDuration($seconds))."\r\n";
    }
  }

  function writeExdate($name, $value) {
    if ( !is_array($value) && $value ) {
      $value = array($value);
    }
    if(is_array($value)) {
      foreach($value as $exdate) {
        $this->buffer .= $this->parseProperty($this->parseName($name). $this->parseTZIDedDate($exdate))."\r\n";
      }
    }
  }
  
  /**
   * @param Of_Date $date
   * @param string $format Date format. Can be ICS_DATETIME or ICS_DATE.
   */
  function parseDate($date, $format=Of_Date::ICS_DATETIME) {
    if($format == Of_Date::ICS_DATETIME) {
      $date->setTimezone(new DateTimeZone('GMT'));
    }
    $return = $date->get($format);
    if($format == Of_Date::ICS_DATETIME) {
      $return .= 'Z';
    }
    $date->setDefaultTimezone();
    return $return;
  }
  
  /**
   * Parse a date with the given format, preferably with ICS_DATETIME if possible.
   *
   * @param Of_Date $date
   * @param string $format Date format. Can be Of_Date::ICS_DATETIME or Of_Date::ICS_DATE.
   */
  function parseTZIDedDate($date, $format=Of_Date::ICS_DATETIME) {
    if ($date->getOriginalTimeZone() && $format == Of_Date::ICS_DATETIME) {
      $date->setTimezone(new DateTimeZone($date->getOriginalTimeZone()));
      $res = ';TZID='. $date->getOriginalTimeZone().':'. $date->get($format);
      $date->setDefaultTimezone();
    } else {
      $res = ':'. $this->parseDate($date, $format);
    }
    
    return $res;
  }

  /**
   * Convert a duration in seconds to the ICS DURATION format (witch can include weeks, days, etc.).
   * See RFC 2445.
   * @param int $seconds
   */
  function secondsToDuration($seconds) {
    $divs  = array( 604800, 86400, 3600,  60,   1 );
    $names = array(    'W',   'D',  'H', 'M', 'S' );
    $duration = 'P';
    $add_T = 0;

    for($i = 0 ; $i < count($divs) ; $i++) {
      $n = (int) ($seconds / $divs[$i]);
      if($n > 0) {
        if($divs[$i] < 86400 && $add_T == 0) {
          $duration .= 'T';
          $add_T = 1;
        }
        $duration .= $n.$names[$i];
        $seconds -= $divs[$i] * $n;
      }
    }

    return $duration;
  }

  /**
   * @param string $name The ICS field name.
   * @param Of_Date $value
   */
  function writeBoundDate($name, $value) {
    if($this->parsed_event->isAllDay()) {
      $this->buffer .= $this->parseProperty($this->parseName($name).';'.$this->parseName('value').'=DATE'.$this->parseTZIDedDate($value, Of_Date::ICS_DATE))."\r\n";
    } else {
      $this->buffer .= $this->parseProperty($this->parseName($name).$this->parseTZIDedDate($value, Of_Date::ICS_DATETIME))."\r\n";
    }
  }

  function writeXObmAlert($name, $values) {
    foreach($values as $value ) {
      $property = $this->parseProperty($this->parseName($name).';'.$this->parseName('x-obm-id').'='.$value['user'].':'.$value['duration']);
      $this->buffer .= $property."\r\n";       
    }
  }

  function writeVAlarm($name, $values) {
    $this->buffer .= "BEGIN:VALARM\r\n";
    foreach($values as $name=>$value ) {
      $property  = $this->parseProperty(
                    $this->parseName($name).':'.$value."\r\n"
                  )."\r\n";
      $this->buffer .= $property;
    }
    $this->buffer .= "END:VALARM\r\n";
  }
}
?>
