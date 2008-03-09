<?php

include_once('obminclude/of/Vcalendar.php');

class Vcalendar_Writer_ICS {

  var $buffer;
  
  function Vcalendar_Writer_ICS() {
    $this->buffer = '';
  }

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
      $this->writeVevent($vevents[$i]);
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
    $this->writeProperty('dtstamp', $this->parseDate(gmdate('Y-m-d H:i:s')));
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
    return trim(chunk_split($property,75,"\n "));
  }
  
  function writeDtstart($name,$value) {
    $this->buffer .= $this->parseProperty($this->parseName($name).':'.$this->parseDate($value));
    $this->buffer .= "\n";
  }
  
  function writeOrganizer($name, $value) {
      $userInfo = get_user_info($value);
      $params[] = $this->parseName('x-obm-id').'='.$attendee['id'];
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
      switch($attendee['state']) {
        case 'A' :
          $partstat = 'ACCEPTED';
          break;
        case 'R' :
          $partstat = 'REFUSED';
          break;
        case 'W' :
          $partstat = 'NEEDS-ACTION';
          break;
      }
      switch($attendee['entity']) {
        case 'user' :
          $userInfo = get_user_info($attendee['id']);
          $value =  'MAILTO:'.$userInfo['email'];
          $params[] = 'CUTYPE=INDIVIDUAL';
          $params[] = 'CN='.$this->parseText($userInfo['firstname'].' '.$userInfo['lastname']);
          $params[] = 'PARTSTAT='.$partstat;
          break;
        case 'resource' :          
          $resourceInfo = get_entity_info($attendee['id'],'resource');
          $value =  'MAILTO:';
          $params[] = 'CUTYPE=RESOURCE';
          $params[] = 'CN='.$this->parseText($resourceInfo['label']);          
          $params[] = 'PARTSTAT='.$partstat;
          break;
        case 'group' :
          $groupInfo = get_group_info($attendee ['id']);
          $value = 'MAILTO:'.$this->parseText($groupInfo['email']);
          $params[] = 'CUTYPE=GROUP';
          $params[] = 'CN='.$this->parseText($groupInfo['name']);
          $params[] = 'PARTSTAT='.$partstat;
          break;
        case 'task' :
          $mail =  'MAILTO:';
          $params[] = 'CUTYPE=X-TASK';          
          $params[] = 'PARTSTAT=ACCEPTED';
          break;
          
      }
      $params[] = $this->parseName('x-obm-id').'='.$attendee['id'];
      $property = $this->parseProperty($this->parseName($name).';'.implode(';',$params).':'.$value);
      $this->buffer .= $property."\n";
    }
  }
  
  function writeRrule($name, $value) {
    $params[] = 'INTERVAL='.strtoupper($value['interval']);
    $params[] = 'FREQ='.strtoupper($value['kind']);
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
  
  function writeDuration($name, $value) {
    $this->buffer .= $this->parseProperty($this->parseName($name).':PT'.$value.'S')."\n";
  }
  
  function writeExdate($name, $value) {
    if(is_array($value)) {
      foreach($value as $exdate) {
        $property[] = $this->parseDate($exdate);
      } 
    } else {
      $property[] = $this->parseDate($value);
    }
    $this->buffer .= $this->parseProperty($this->parseName($name).':'.implode(',',$property))."\n";
  }

  function parseDate($date) {
    $timestamp = strtotime($date);
    return gmdate('Ymd\THis\Z',$timestamp);
  }
}
?>
