<?php

include_once('obminclude/of/Vcalendar.php');

//FIXME re-think read functions

class Vcalendar_Reader_OBM {

  var $document;

  var $entities;

  var $vevents = array();
  
  var $weekDays = array( 'sunday' => 'su', 'monday' => 'mo', 'tuesday' => 'tu', 'wednesday' => 'we', 'thursday' => 'th' , 'friday' => 'fr', 'saturday' => 'sa');

  var $eventSets = array();
  
  function Vcalendar_Reader_OBM($entities,$ids = NULL, $startTime = NULL, $endTime = NULL) {
    $this->entities = $entities;
    if(!is_null($ids)) {
      $this->readSet($ids);
    }
    if(!is_null($startTime) && !is_null($endTime)) {
      $this->readPeriod($startTime,$endTime);
    }
  }

  function readPeriod($startTime, $endTime) {
    $noRepeatEvent = run_query_calendar_no_repeat_events($startTime,$endTime,$this->entities,NULL);
    $repeatEvent = run_query_calendar_repeat_events($startTime,$endTime,$this->entities,NULL);
    if($noRepeatEvent->next_record()) {
      $this->eventSets[] = &$noRepeatEvent;
    }
    if($repeatEvent->next_record()) {
      $this->eventSets[] = &$repeatEvent;
    }
  }

  function readSet($events) {
    foreach($events as $eventId) {
      $this->eventSets[] = &run_query_calendar_detail($eventId);
    }
  }

  function & getDocument() {
    $this->document = new Vcalendar();
    $this->setHeaders();
    foreach($this->eventSets as $set) {
      do {
        $id = $set->f('calendarevent_id');      
        if(is_null($this->vevents[$id])) {
          $this->vevents[$id] = &$this->addVevent($set->Record);
        }
      }while($set->next_record()) ;
    }

    $attendees = run_query_get_events_attendee(array_keys($this->vevents));
    while($attendees->next_record()) {
      $this->addAttendee($this->vevents[$attendees->f('calendarevent_id')] , $attendees->Record);
    }
    if(count($this->vevents) > 0) {
      $exceptions = run_query_get_events_exception(array_keys($this->vevents));
      while($exceptions->next_record()) {
        $this->addExdate($this->vevents[$exceptions->f('eventexception_event_id')] , $exceptions->f('eventexception_date'));
      }    
    }
    return $this->document;
  }

  function setHeaders() {
    $vcalendar = &$this->document->vcalendar;
    $vcalendar->set('prodid','-//Aliasource Groupe LINAGORA//OBM Calendar '.$GLOBALS['obm_version'].'//FR');
    $vcalendar->set('calscale', 'GREGORIAN');
    $vcalendar->set('x-obm-time',time());
    $vcalendar->set('version','2.0');
  }

  function & addVevent(&$data) {
    $vevent = &$this->document->createElement('vevent');
    $vevent->set('dtstart', $this->parseDate($data['calendarevent_date']));
    $vevent->set('duration', $data['calendarevent_duration']);
    if($data['calendarevent_allday'] != 0) {
      $vevent->set('x-obm-all-day', 1);
    }
    $vevent->set('summary', $data['calendarevent_title']);
    $vevent->set('description', $data['calendarevent_description']);
    $vevent->set('class', $this->parsePrivacy($data['calendarevent_privacy']));
    $vevent->set('priority', $this->parsePriority($data['calendarevent_priority']));
    $vevent->set('organizer', $data['calendarevent_owner']);
    $vevent->set('location', $data['calendarevent_location']);
    $vevent->set('categories', array($data['calendarcategory1_label']));
    $vevent->set('x-obm-color', $data['calendarevent_color']);
    $vevent->set('uid', $this->parseUid($data['calendarevent_id']));
    if(!is_null($data['calendarevent_repeatkind']) && $data['calendarevent_repeatkind'] != 'none') {
      $vevent->set('rrule',$this->parseRrule($data));
    }
    $this->document->vcalendar->appendChild($vevent);
    return $vevent;
  }

  function parseDate($timestamp) {
    return date('Y-m-d H:i:s', $timestamp);
  }
  
  function addAttendee(&$vevent, &$data) {
   $vevent->set('attendee',$this->parseAttendee($data['evententity_entity_id'], $data['evententity_entity'], $data['evententity_state']));
  }
  
  function addExdate(&$vevent, &$date) {
   $vevent->set('exdate',$this->parseDate($date));
  }
    
  function parsePrivacy($privacy) {
    if($privacy == 1) {
      return 'PRIVATE';
    }
    return 'PUBLIC';
  }

  function parseUid($id) {
    return 'obm@'.$id;
  }
  
  function parsePriority($priority) {
    if($priority == 1) {
      return 9;
    } elseif ($priority == 2) {
      return 5;
    } elseif ($priority == 3) {
      return 1;
    }
  }

  function parseAttendee($id, $entity, $state) {
    return array('entity' => $entity, 'id' => $id, 'state' => $state);;
  }
  
  function parseRrule($data) {
    $rrule = array();
     switch ($data['calendarevent_repeatkind']) {
       case 'daily' :
       case 'yearly' :
         $rrule['kind'] = $data['calendarevent_repeatkind'];
         break;
       case 'monthlybydate' :
         $rrule['kind'] = 'monthly';
         break;
       case 'monthlybyday' :
         $rrule['kind'] = 'monthly';
         $day = $this->weekDays[strtolower(date('l',$data['calendarevent_date']))];
         $num =  ceil(date('d',$data['calendarevent_date'])/7);
         $rrule['byday'] = array($num.$day);
         break;
       case 'weekly' :
         $rrule['kind'] = 'weekly';
         foreach($this->weekDays as $longDay => $shortDay) {
           $index = date('w', strtotime($longDay)) - date('w', strtotime($GLOBALS['ccalendar_weekstart']));
           if($data['calendarevent_repeatdays'][$index] == '1') {
             $days[] = $shortDay;
           }           
         }
         $rrule['byday'] = $days;
         break;
     }
     $rrule['until'] = $this->parseDate($data['calendarevent_endrepeat']);
     $rrule['interval'] = $data['calendarevent_repeatfrequence'];
     return $rrule;
  }
}
?>
