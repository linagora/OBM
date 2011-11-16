<?php

include_once('obminclude/of/Vcalendar.php');

//FIXME re-think read functions

class Vcalendar_Reader_OBM {

  var $document;

  var $entities;

  var $vevents = array();
  
  var $weekDays = array( 'sunday' => 'su', 'monday' => 'mo', 'tuesday' => 'tu', 'wednesday' => 'we', 'thursday' => 'th' , 'friday' => 'fr', 'saturday' => 'sa');

  var $eventSets = array();
  
  // 'status' enables filtering of event upon user participation
  function Vcalendar_Reader_OBM($entities, $ids = array(), $startTime = NULL, $endTime = NULL, $status = NULL) {
    $this->db = new DB_OBM;
    $this->entities = $entities;
    if(!empty($ids)) {
      $this->readSet($ids);
    } else {
      $ids = array();
    }
    if(!is_null($startTime) && !is_null($endTime)) {
      $ids = array_merge($ids,$this->readPeriod($startTime,$endTime, $status));
    }
    $this->alerts = $this->readEventsAlerts($ids);
    $this->valarms = $this->readEventsVAlarms($ids);
  }

  function readPeriod($startTime, $endTime, $status=NULL) {
    $noRepeatEvent = run_query_calendar_no_repeat_events($startTime,$endTime,$this->entities,$status);
    $repeatEvent = run_query_calendar_repeat_events($startTime,$endTime,$this->entities,$status,'',null,null,true);
    $ids = array();
    $indexed = array();
    while($noRepeatEvent->next_record()) {
      $ids[] = $noRepeatEvent->f('event_id');
      $indexed[$noRepeatEvent->f('event_id')] = $noRepeatEvent->Record;
      $this->eventSets[] =& $indexed[$noRepeatEvent->f('event_id')];
    }
    if ( count($ids) ) {
      $parents = array();
      $parentsRes = run_query_calendar_get_parent_event($ids);
      while ( $parentsRes->next_record() ) {
	if ( array_key_exists($parentsRes->f("eventexception_child_id"), $indexed) ) {
	  $indexed[$parentsRes->f("eventexception_child_id")]["event_eventexception_date"] = $parentsRes->f("eventexception_date");
	  $indexed[$parentsRes->f("eventexception_child_id")]["event_is_exception"] = true;
	}
      }
    }

    while($repeatEvent->next_record()) {
      $ids[] = $repeatEvent->f('event_id');
      $this->eventSets[] = $repeatEvent->Record;
    }
    return $ids;
  }
   
  function readSet($events) {
    foreach($events as $eventId) {
      $set = run_query_calendar_detail($eventId);
      $this->eventSets[] = $set->Record;
    }
  }

  function readEventsAlerts($events) {
    if(empty($events)) {
      return array();
    }
    $query = 'SELECT eventalert_duration, eventalert_user_id, eventalert_event_id FROM EventAlert WHERE eventalert_event_id IN ('.implode(',', $events).') and eventalert_duration > 0';
    $this->db->query($query);
    while($this->db->next_record()) {
      $alerts[$this->db->f('eventalert_event_id')][] = array('user' => $this->db->f('eventalert_user_id'), 'duration' => $this->db->f('eventalert_duration'));
    }
    return $alerts;
  }

  function readEventsVAlarms($events) {
    $valarms = array();

    if(!empty($events)){
      $query = 'SELECT      event_date,
                            event_title,
                            eventalert_duration,
                            eventalert_event_id
                FROM        EventAlert
                INNER JOIN  Event ON eventalert_event_id = event_id
                WHERE       eventalert_event_id IN ('.implode(',', $events).')';
      $this->db->query($query);
      while($this->db->next_record()) {
        $trigger = $this->db->f('eventalert_duration')  < 0 ? "-P" : "P";
        $trigger .= $this->db->f('eventalert_duration')."S";
        $valarm = array(
          "trigger"=>$trigger,
          "action"=>"display",
          "description"=>$this->db->f('event_title')
        );
        $valarms[$this->db->f('eventalert_event_id')][] = $valarm;
      }
    }
    return $valarms;
  }

  /**
   * @return Vcalendar
   */
  function & getDocument($method = 'PUBLISH', $include_attachments = false) {
    $this->document = new Vcalendar();
    $this->setHeaders($method);
    foreach($this->eventSets as $set) {
	  if (!($set))
		  continue;
      $id = $set['event_id'];      
      if(is_null($this->vevents[$id])) {
        $this->vevents[$id] = &$this->addVevent($set);
      }
    }
     $attendees = run_query_get_events_attendee(array_keys($this->vevents));
    if ( strcasecmp($method,"REPLY") == 0 && $this->entities && is_array($this->entities) && count($this->entities) && array_key_exists("user",$this->entities) && is_array($this->entities["user"]) ) {
      $userId = reset(array_keys($this->entities["user"]));
      while($attendees->next_record()) {

       if ( $attendees->f('eventlink_entity_id') == $userId ) {
         $this->addAttendee($this->vevents[$attendees->f('event_id')] , $attendees->Record);
         break;
       }
      }
    } else {
     while($attendees->next_record()) {
       $this->addAttendee($this->vevents[$attendees->f('event_id')] , $attendees->Record);
     }
    }
    /* this only adds exceptions that have been deleted. Those that have been changed have their own ics*/
    if(count($this->vevents) > 0) {
      $exceptions = run_query_get_events_exception(array_keys($this->vevents),NULL,NULL,TRUE);
      while($exceptions->next_record()) {
        $date = new Of_Date($exceptions->f('eventexception_date'));
        $timezone = $exceptions->f('event_timezone');
        if ($timezone) $date->setOriginalTimeZone($timezone);
        $this->addExdate($this->vevents[$exceptions->f('eventexception_parent_id')] , $date);
      }    
    }
    
    foreach($this->vevents as $id => $vevent) {
      VCalendar_Utils::privatizeEvent($this->vevents[$id]);
    }
    return $this->document;
  }

  function setHeaders($method) {
    $vcalendar = &$this->document->vcalendar;
    $vcalendar->set('prodid','-//Aliasource Groupe LINAGORA//OBM Calendar '.$GLOBALS['obm_version'].'//FR');
    $vcalendar->set('calscale', 'GREGORIAN');
    $vcalendar->set('x-obm-time',time());
    $vcalendar->set('version','2.0');
    $vcalendar->set('method', strtoupper($method));
  }

  function & addVevent(&$data) {
    $vevent = &$this->document->createElement('vevent');
    $vevent->private = ($vevent->private && $data['event_privacy']);
    $vevent->private = ($vevent->private && ($GLOBALS['obm']['uid'] != $data['event_owner']));
    $created = $this->parseDate($data['timecreate']);
    $created->setOriginalTimeZone($data['event_timezone']);
    $vevent->set('created', $created);

    $last_modified = $this->parseDate($data['timeupdate']);
    $last_modified->setOriginalTimeZone($data['event_timezone']);
    $vevent->set('last-modified', $last_modified);

    $dtstart = $this->parseDate($data['event_date']);
    $dtstart->setOriginalTimeZone($data['event_timezone']);
    $vevent->set('dtstart', $dtstart);
    $vevent->set('duration', $data['event_duration']);
    $vevent->set('transp', $data['event_opacity']);
    if($data['event_allday'] != 0) {
      $vevent->set('x-obm-all-day', 1);
    }
    $vevent->set('sequence', $data['event_sequence']);
    $vevent->set('summary', $data['event_title']);
    $vevent->set('description', $data['event_description']);
    $vevent->set('class', $this->parsePrivacy($data['event_privacy']));
    if($data['event_priority']) $vevent->set('priority', $this->parsePriority($data['event_priority']));
    $vevent->set('organizer', $data['event_owner']);
    $vevent->set('x-obm-domain', $data['owner_domain']);
    $vevent->set('location', $data['event_location']);
    $vevent->set('categories', array($data['eventcategory1_label']));
    $vevent->set('x-obm-color', $data['event_color']);

    if ($data['event_is_exception']) {
        $recurrence_id = $this->parseDate($data['event_eventexception_date']);
        $recurrence_id->setOriginalTimeZone($data['event_timezone']);
        $vevent->set('recurrence-id', $recurrence_id);
    }

    if(!empty($this->alerts[$data['event_id']])) {
      foreach($this->alerts[$data['event_id']] as $alert) {
        $vevent->set('x-obm-alert', $alert);
      }
    }
    if(!empty($this->valarms[$data['event_id']])) {
      foreach($this->valarms[$data['event_id']] as $valarm) {
        $vevent->set('valarm', $valarm);
      }
    }
    $vevent->set('uid', $data['event_ext_id']);
    if(!is_null($data['event_repeatkind']) && $data['event_repeatkind'] != 'none') {
      $vevent->set('rrule',$this->parseRrule($data));
    }
    $this->document->vcalendar->appendChild($vevent);
    return $vevent;
  }

  /**
   * @return Of_Date
   */
  function parseDate($timestamp) {
    return new Of_Date($timestamp, 'GMT');
  }
  
  function addAttendee(&$vevent, &$data) {
   $vevent->private = ($vevent->private && ($GLOBALS['obm']['uid'] != $data['eventlink_entity_id']));
   if (!$vevent->private) {
       $vevent->set('attendee', $this->parseAttendee($data['eventlink_entity_id'], $data['eventlink_entity'], $data['eventlink_state']));
   }
  }
  
  function addDocument(&$vevent, $document_id) {
    $vevent->set('attach', $GLOBALS['cgp_host'].'calendar/calendar_render.php?action=download_document&externalToken='.get_calendar_entity_share($document_id, 'document', 'private').'&document_id='.$document_id);
  }
  
  function addExdate(&$vevent, &$date) {
   $vevent->set('exdate', $date);
  }
    
  function parsePrivacy($privacy) {
    if($privacy == 1) {
      return 'PRIVATE';
    }
    return 'PUBLIC';
  }

  function parsePriority($priority) {
    if($priority == 1) {
      return 9;
    } elseif ($priority == 2) {
      return 5;
    } elseif ($priority == 3) {
      return 1;
    }
    return 5;
  }

  function parseAttendee($id, $entity, $state) {
    return array('entity' => $entity, 'id' => $id, 'state' => $state);;
  }
  
  function parseRrule($data) {
    $rrule = array();
     switch ($data['event_repeatkind']) {
       case 'daily' :
       case 'yearly' :
         $rrule['kind'] = $data['event_repeatkind'];
         break;
       case 'monthlybydate' :
         $rrule['kind'] = 'monthly';
         break;
       case 'monthlybyday' :
         $rrule['kind'] = 'monthly';
         $date = new Of_Date($data['event_date'], 'GMT');
         $day = $date->get(Of_Date::WEEKDAY_ICS);
         
         $num =  ceil($date->getDay()/7);
         $rrule['byday'] = array($num.$day);
         break;
       case 'weekly' :
         $rrule['kind'] = 'weekly';
         foreach($this->weekDays as $longDay => $shortDay) {
           $index = date('w', strtotime($longDay));
           if($data['event_repeatdays'][$index] == '1') {
             $days[] = $shortDay;
           }           
         }
         $rrule['byday'] = $days;
         break;
     }
     if ($data['event_endrepeat'])
       $rrule['until'] = $this->parseDate($data['event_endrepeat']);
     $rrule['interval'] = $data['event_repeatfrequence'];
     return $rrule;
  }
}
?>
