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

//FIXME re-think read functions

class Vcalendar_Reader_OBM {

  var $document;

  var $entities;

  var $vevents = array();
  
  var $weekDays = array( 'sunday' => 'su', 'monday' => 'mo', 'tuesday' => 'tu', 'wednesday' => 'we', 'thursday' => 'th' , 'friday' => 'fr', 'saturday' => 'sa');

  var $eventSets = array();

  var $recurrenceId;
  
  // 'status' enables filtering of event upon user participation
  // recurrenceId is yet another fugly hack to let us serialize a negative exception, it contains the date of the exception
  function Vcalendar_Reader_OBM($entities, $ids = array(), $startTime = NULL, $endTime = NULL, $status = NULL, $recurrenceId = NULL) {
    $this->db = new DB_OBM;
    $this->entities = $entities;
    if (count($ids) > 1 && $occurrenceDate != null) {
        throw new Exception("Can't have more than one id with the occurrenceDate parameter");
    }
    $this->recurrenceId = $recurrenceId;
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
      array_unshift($this->eventSets, $repeatEvent->Record);
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
  function & getDocument($method = 'PUBLISH', $include_attachments = false, $privatize_events=true) {
    $this->document = new Vcalendar();
    $this->setHeaders($method);
    foreach($this->eventSets as $set) {
	  if (!($set))
		  continue;
      $id = $set['event_id'];      
      if(is_null($this->vevents[$id])) {
        $this->vevents[$id] = &$this->addVevent($set, $method);
      }
    }
     $attendees = run_query_get_events_attendee(array_keys($this->vevents));
    if ( strcasecmp($method,"REPLY") == 0 && $this->entities && is_array($this->entities) && count($this->entities) && array_key_exists("user",$this->entities) && is_array($this->entities["user"]) ) {
      $userId = reset(array_keys($this->entities["user"]));
      while($attendees->next_record()) {

       if ( $attendees->f('eventlink_entity_id') == $userId ) {
         $this->addAttendee($this->vevents[$attendees->f('event_id')] , $attendees->Record);
         $this->addComment($attendees);
         break;
       }
      }
    } else {
     while($attendees->next_record()) {
       $this->addAttendee($this->vevents[$attendees->f('event_id')] , $attendees->Record);
     }
    }
    /* this only adds exceptions that have been deleted. Those that have been changed have their own ics*/
    if(count($this->vevents) > 0 && $this->recurrenceId == null) {
      $exceptions = run_query_get_events_exception(array_keys($this->vevents),NULL,NULL,TRUE);
      while($exceptions->next_record()) {
        $date = new Of_Date($exceptions->f('eventexception_date'), "GMT");
        $timezone = $exceptions->f('event_timezone');
        if ($timezone) $date->setOriginalTimeZone($timezone);
        $this->addExdate($this->vevents[$exceptions->f('eventexception_parent_id')] , $date);
      }    
    }

    if ($privatize_events) {
        foreach($this->vevents as $id => $vevent) {
          VCalendar_Utils::privatizeEvent($this->vevents[$id]);
        }
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

  function & addVevent(&$data, $method) {
    $vevent = &$this->document->createElement('vevent');
    $vevent->private = ($vevent->private && $data['event_privacy']);
    $vevent->private = ($vevent->private && ($GLOBALS['obm']['uid'] != $data['event_owner']));
    // here we cast bc PHP ceil when ms is > 500 and lightning floor watever the miliseconds value
    // so we remove miliseconds
    $created = $this->parseDate((integer)$data['timecreate']);
    $created->setOriginalTimeZone($data['event_timezone']);
    $vevent->set('created', $created);
    // here we cast bc PHP ceil when ms is > 500 and lightning floor watever the miliseconds value
    // so we remove miliseconds
    // OBMFULL-3595
    $timeupdate = (integer) $data['timeupdate'];
    if ($timeupdate == 0) {
      $last_modified = $created;
    } else {
      $last_modified = $this->parseDate($timeupdate);
    }
    $last_modified->setOriginalTimeZone($data['event_timezone']);

    $dtstamp = (strcasecmp($method, "reply") == 0) ? $this->parseDate(time()) : $last_modified;

    $vevent->set('last-modified', $last_modified);
    $vevent->set('dtstamp', $dtstamp);

    if ($this->recurrenceId == null) {
      $dtstart = $this->parseDate($data['event_date']);
    }
    else {
      $dtstart = $this->recurrenceId;
    }
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
    $vevent->set('x-obm-domain-uuid', $data['owner_domain_uuid']);
    $vevent->set('location', $data['event_location']);
    $vevent->set('categories', array($data['eventcategory1_label']));
    $vevent->set('x-obm-color', $data['event_color']);

    if ($data['event_is_exception']) {
        $recurrence_id = $this->parseDate($data['event_eventexception_date']);
        $recurrence_id->setOriginalTimeZone($data['event_timezone']);
        $vevent->set('recurrence-id', $recurrence_id);
    }
    elseif ($this->recurrenceId != null) {
        $vevent->set('recurrence-id', $this->recurrenceId);
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
    if(!is_null($data['event_repeatkind']) && $data['event_repeatkind'] != 'none' && $this->recurrenceId == null) {
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
  function addComment($attendees) {
   if ($attendees->f('eventlink_comment')){
      foreach($this->vevents as $vevent){
        $vevent->set('comment', $attendees->f('eventlink_comment'));
      }
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
    return array('entity' => $entity, 'id' => $id, 'state' => $state);
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
