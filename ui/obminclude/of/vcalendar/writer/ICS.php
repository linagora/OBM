<?php

include_once('obminclude/of/Vcalendar.php');

class Vcalendar_Writer_ICS {

  var $parsed_event;

  var $buffer;

  function Vcalendar_Writer_ICS() {
    $this->buffer = '';
  }

  /**
   * @param Vcalendar $document
   */
  function writeDocument(&$document) {
    $vcalendar = &$document->vcalendar;
    $this->buffer .= 'BEGIN:'.$this->parseName($vcalendar->name)."\n";
    $properties = get_object_vars($vcalendar);
    $name = $properties['name'];
    unset($properties['name']);
    unset($properties['document']);
    unset($properties['children']);
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
    $this->buffer .= 'END:'.$this->parseName($vcalendar->name)."\n";    
  }

  function writeVevent(&$vevent) {
    $this->buffer .= 'BEGIN:'.$this->parseName($vevent->name)."\n";
    $properties = get_object_vars($vevent);
    $name = $properties['name'];
    unset($properties['name']);
    unset($properties['document']);
    unset($properties['children']);
    unset($properties['dtend']);
    foreach ($properties as $name => $value) {
      $this->writeProperty($name, $value);
    }
    //$this->writeProperty('dtstamp', $this->parseDate(gmdate('Y-m-d H:i:s')));
    
    // FIXME ??? maybe a good substitute
    $this->writeProperty('dtstamp', $this->parseDate(new Of_Date()));
    $this->buffer .= 'END:'.$this->parseName($vevent->name)."\n";
  }

  function writeProperty($name,$value) {
    $methodName = 'write'.str_replace(' ','',ucwords(str_replace('-',' ',$name)));
    if(method_exists($this, $methodName)) {
      $this->$methodName($name,$value);
    } else {
      $this->buffer .= $this->parseProperty($this->parseName($name). ":".$this->parseText($value));
      $this->buffer .= "\n";      
    }    
  }

  function parseName($name) {
    return strtoupper($name);
  }

  function parseText($text) {
    $text = addcslashes($text,"\;,\n");
    return $text;
  }

  function parseProperty($property) {
    return trim(chunk_split($property,74,"\n "));
  }

  function writeDtstart($name, $value) {
    $this->writeBoundDate($name, $value);
  }

  function writeDtend($name, $value) {
    $this->writeBoundDate($name, $value);
  }

  function writeOrganizer($name, $value) {
    $userInfo = get_user_info($value);
    $params[] = $this->parseName('x-obm-id').'='.$value;
    $params[] = 'CN='.$this->parseText($userInfo['firstname'].' '.$userInfo['lastname']);
    $value =  'MAILTO:'.$userInfo['email'];
    $property = $this->parseProperty($this->parseName($name).';'.implode(';',$params).':'.$value);
    $this->buffer .= $property."\n";      
  }

  function writeAttendee($name,$value) {
    if(key_exists('state',$value)) {
      $value = array($value); 
    }
    foreach($value as $attendee) {
      $params = array();
      $value = '';
      if($attendee['state']) {
        $partstat = $attendee['state'];
      } else {
        $partstat = 'NEEDS-ACTION';
      }
      switch($attendee['entity']) {
      case 'user' :
        $userInfo = get_user_info($attendee['id']);
        $value =  'MAILTO:'.$userInfo['email'];
        $params[] = 'CUTYPE=INDIVIDUAL';
        $params[] = 'CN='.$this->parseText($userInfo['firstname'].' '.$userInfo['lastname']);
        $params[] = 'PARTSTAT='.$partstat;
        $params[] = $this->parseName('x-obm-id').'='.$attendee['id'];
        break;
      case 'resource' :          
        $resourceInfo = get_entity_info($attendee['id'],'resource');
        $value =  'MAILTO:';
        $params[] = 'CUTYPE=RESOURCE';
        $params[] = 'CN='.$this->parseText($resourceInfo['label']);          
        $params[] = 'PARTSTAT='.$partstat;
        $params[] = $this->parseName('x-obm-id').'='.$attendee['id'];
        break;
      case 'group' :
        $groupInfo = get_group_info($attendee ['id']);
        $value = 'MAILTO:'.$this->parseText($groupInfo['email']);
        $params[] = 'CUTYPE=GROUP';
        $params[] = 'CN='.$this->parseText($groupInfo['name']);
        $params[] = 'PARTSTAT='.$partstat;
        $params[] = $this->parseName('x-obm-id').'='.$attendee['id'];
        break;
      case 'task' :
        $mail =  'MAILTO:';
        $params[] = 'CUTYPE=X-TASK';          
        $params[] = 'PARTSTAT=ACCEPTED';
        $params[] = $this->parseName('x-obm-id').'='.$attendee['id'];
        break;
      case 'contact' :
        $contactInfo = get_contact_from_ids(array($attendee['id']));
        $params[] = 'CUTYPE=INDIVIDUAL';
        $value = 'MAILTO:'.$contactInfo['entity'][ $attendee['id'] ]['email_address'];
        break;

      default:
        // Skip $params[]'less entities
        continue 2; // This 'continue' is catched by switch(). With '2' we reach our foreach().
      } 

      $property = $this->parseProperty($this->parseName($name).';'.implode(';',$params).':'.$value);
      $this->buffer .= $property."\n";
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
    $this->buffer .= $property."\n";
  }

  function writeCategories($name, $value) {
    $this->buffer .= $this->parseProperty($this->parseName($name).':'.implode(',',$value))."\n";
  }

  function writeDuration($name, $seconds) {
    // With all-day events we refers to the bound dates rather than the duration
    if($this->parsed_event->isAllDay()) {
      $nb_days = (int) ($seconds / 86400);
      if($nb_days > 1) {
        $dtend = clone $this->parsed_event->get('dtstart');
        $dtend->addSecond($seconds)->subDay(1);
        $this->writeDtend('dtend', $dtend);
      }
    } else {
      $this->buffer .= $this->parseProperty($this->parseName($name).':'.$this->secondsToDuration($seconds))."\n";
    }
  }

  function writeExdate($name, $value) {
    if(is_array($value)) {
      foreach($value as $exdate) {
        $this->buffer .= $this->parseProperty($this->parseName($name). $this->parseTZIDedDate($exdate))."\n";
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
      $this->buffer .= $this->parseProperty($this->parseName($name).$this->parseTZIDedDate($value, Of_Date::ICS_DATE))."\n";
    } else {
      $this->buffer .= $this->parseProperty($this->parseName($name).$this->parseTZIDedDate($value, Of_Date::ICS_DATETIME))."\n";
    }
  }
}
?>
