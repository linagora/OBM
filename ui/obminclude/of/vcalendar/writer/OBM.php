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



include_once('obminclude/of/Vcalendar.php');

class Vcalendar_Writer_OBM {

  var $db;

  var $lazyRead;

  var $lazyWrite;

  var $ids;

  var $frequency = array('daily','weekly','monthly','monthlybydate', 'monthlybyday', 'yearly');

  var $repeat = array('byday','bymonthday','byyearday','byweekno','bymonth','bysetpos','wkst');

  var $rights;

  var $event_error = 0; //count wrong events (without start date for example). These events are not saved.

  function Vcalendar_Writer_OBM($force=false) {
    $this->db = new DB_OBM;
    $this->lazyRead = true;
    if(!$force) {
      $this->rights = array_keys(OBM_Acl::getAllowedEntities($GLOBALS['obm']['uid'], 'calendar', 'write'));
    } else {
      $this->rights = true;
    }
  }

  function writeDocument(&$document) {
    $vevents = &$document->getVevents();
    for($i=0; $i < count($vevents); $i++ ) {
      $this->writeVevent($vevents[$i]);
    }
  }

  function writeVevent(&$vevent) {
    $eventData = $this->getOBMEvent($vevent);

    if(is_null($eventData)) {
      $id = $this->insertEvent($vevent);
    } else {
      $id = $eventData->f('event_id');
      if($this->haveAccess($eventData->f('event_owner'))) {
        $this->updateEvent($id, $vevent);
      } else {
        // FIXME WHAT TO DO??
        $this->updateAttendees($id,$vevent);
        $this->updateAlerts($vevent->get('x-obm-alert'), $id, $GLOBALS['obm']['uid']);
      }
    }
    $valarms = &$vevent->getElementByName('valarm');
    for($i=0; $i < count($valarms); $i++) {
      $this->insertValarm($valarms[$i], $vevent, $id);
    }
  }

  function insertValarm(&$valarm, &$vevent, $id) {
    if(is_numeric($id)) {
      $attendees = $valarm->get('attendee');
      if(isset($attendees['id'])) {
        $attendees = array($attendees);
      }
      $ts = $vevent->get('dtstart')->diffTimestamp($valarm->get('trigger'));
      foreach($attendees as $attendee) {
        if($attendee['entity'] == 'user' && $this->haveAccess($attendee['id'])) {
          $this->updateAlerts(array(array('user' => $attendee['id'], 'duration' => $ts)), $id);
        }
      }
    }
  }

  function & getEventByData(&$vevent) {
    $eventData = NULL;
    $start = $vevent->get('dtstart');
    $summary = $vevent->get('summary');
    if (isset($start) && isset($summary)) {
      if($this->lazyRead) {
        if(($organizer = $vevent->get('organizer'))) {
          $owner = "OR event_owner ".sql_parse_id($organizer, true)."";
        }
        $query = "SELECT event_id as id
          FROM Event WHERE event_title =  '".addslashes($vevent->get('summary'))."'
          AND event_date = '".$vevent->get('dtstart')."' AND
          (event_owner ".sql_parse_id($GLOBALS['obm']['uid'], true)." $owner)";
        $this->db->query($query);
        if($this->db->nf() > 0) {
          $this->db->next_record();
          $eventData = $this->getEventById($this->db->f('id'));
        }
      } else {
        //TODO Hard working query.
      }
    }
    return $eventData;
  }

  function getEventById($id) {
    $eventData = run_query_calendar_detail($id);
    if($eventData->nf() == 0) {
      return null;
    }
    return $eventData;    
  }

  function getEventByExtId($id, $recurrence_id=null) {
    $query = null;
    if ($recurrence_id == null) {
      $query = "SELECT event_id as id, event_owner FROM Event WHERE event_ext_id = '$id'";
    }
    else {
      $query = "SELECT event_id as id, event_owner
        FROM Event
        JOIN EventException ee ON ee.eventexception_child_id = event_id
        WHERE event_ext_id = '$id' AND ee.eventexception_date = '$recurrence_id'";
    }
    $this->db->query($query);
    if($this->db->nf() == 0) {
      return null;
    }
    if($this->db->nf() > 1){
      // Returns event owned by user if exists
      while($this->db->next_record()){
        if(($this->db->f('event_owner') == $GLOBALS['obm']['uid'])){
          $eventData = $this->getEventById($this->db->f('id'));
          return $eventData;
        }

      }
      
      // Returns event where user is an attendee if exists
      $this->db->seek(0);
      while($this->db->next_record()){
        $eventAttendees = $this->getEventAttendees($this->db->f('id'));
        if(is_array($eventAttendees) && in_array($GLOBALS['obm']['uid'], array_keys($eventAttendees))){
          $eventData = $this->getEventById($this->db->f('id'));
          return $eventData;
        }
      }

      // Returns event where user has rights
      $this->db->seek(0);
      while($this->db->next_record()){
        if($this->haveAccess($this->db->f('event_owner'))){
          $eventData = $this->getEventById($this->db->f('id'));
          return $eventData;
        }
      }
      
      $this->db->seek(0);
    }

    $this->db->next_record();
    $eventData = $this->getEventById($this->db->f('id'));
    return $eventData;    
  }

  /**
   * @param Vcalendar_Element $vevent
   */
  function parseEventData(&$vevent) {
    $entities = array();
    $states = array();
    $attendees = $vevent->get('attendee');
    if(!is_null($attendees)) {
      if(is_array($attendees) && !array_key_exists('entity',$attendees)) {
        foreach($attendees as $attendee) {
          $this->createUnknownContact($attendee);
          $entities[$attendee['entity']][] = $attendee['id'];
          $states[$attendee['entity']][$attendee['id']] = $attendee['state'];
        }
      } elseif(!is_null($attendees)) {
        $this->createUnknownContact($attendees);
        $entities[$attendees['entity']][] = $attendees['id'];
        $states[$attendees['entity']][$attendees['id']] = $attendees['state'];
      }
    }
    $entities['user'][] = $GLOBALS['obm']['uid'];
    $entities['user'] = array_unique($entities['user']);
    $event['ext_id'] = $vevent->get('uid');
    $organizer = $this->parseOrganizer($vevent->get('organizer'));
    $event['owner'] = $organizer;
    $event['organizer'] = $organizer;
    $event['title'] = addslashes($vevent->get('summary'));
    $dtstart = $vevent->get('dtstart');
    $event['date_begin'] = $dtstart;

    // if no startdate, we consider that it's not an event and we don't insert it
    if (!is_null($dtstart)){
      if ($dtstart->getOriginalTimeZone())
        $event['timezone'] = $dtstart->getOriginalTimeZone();

      $event['date_end'] = $vevent->get('dtend');
      $event['event_duration'] = $vevent->get('duration');
      $event['duration'] = $vevent->get('duration');
      $event['opacity'] = $vevent->get('transp') ? $vevent->get('transp') : "OPAQUE";

      $event['date_exception'] = array();
      $exdates = $vevent->get('exdate');

      $dates_exception = array();

      if (is_array($exdates))
        foreach($exdates as $exdate) {
          if (is_array($exdate)) {
            foreach($exdate as $exdate2) {
              $dates_exception[] = $exdate2;
            }
          } else {
            $dates_exception[] = $exdate;
          }
        }

      $event['date_exception'] = $dates_exception;

      // BEGIN one exception for each day

      foreach ($dates_exception as $k => $v) {
        $dates_exception[$k] = $v->get(Of_Date::DATE_ISO);
      }

      $dates_exception = array_unique($dates_exception);

      foreach ($dates_exception as $k => $v) {
        $date = new Of_Date($v);
        $dates_exception[$k] = $date->setHour($dtstart->getHour())->setMinute($dtstart->getMinute())->setSecond($dtstart->getSecond());
      }

      $event['date_exception'] = $dates_exception;

      // END one exception for each day

      $event['description'] = addslashes($vevent->get('description'));
      $event['location'] = addslashes($vevent->get('location'));
      $event['category1'] = $this->parseCategories($vevent->get('categories'));
      $event['priority'] = $this->parsePriority($vevent->get('priority'));
      $event['privacy'] = $this->parsePrivacy($vevent->get('class')) ;
      $event = array_merge($event, $this->parseRrule($vevent->get('rrule'), $vevent));
      $event['all_day'] = $vevent->isAllDay();
      $event['color'] = $vevent->get('x-obm-color');
      $event['properties'] = $vevent->get('x-obm-properties');
      $event['recurrence_id'] = $vevent->get('recurrence-id');
      return array('event' => $event, 'entities' => $entities, 'states' => $states);
    }
    else {
      return null;
    }
  }

  function createUnknownContact(&$attendee) {
    if(is_array($attendee['id']) && $attendee['entity'] == 'contact') {
      list( $id ) = run_query_insert_others_attendees(
          array( 'others_attendees' => array($attendee['id']['email']) ));
      $attendee['id'] = $id;
    }
  }

  function insertEvent(&$vevent) {
    $data = $this->parseEventData($vevent);
    $in_error = false;
    if (!is_null($data)){
      if(!$this->haveAccess($data['event']['owner'])) {
        $data['event']['owner'] = $GLOBALS['obm']['uid'];
      }
      if ($vevent->get('recurrence-id') == null) {
        $id = run_query_calendar_add_event($data['event'], $data['entities']);
      }
      else {
        $parent_event_id = run_query_get_recurring_event_id_from_ext_id($data['event']['ext_id'], $GLOBALS['obm']['uid']);
        if ($parent_event_id != null) {
          $parent_event_data = run_query_calendar_detail($parent_event_id);
          $dao_params = array(
            'calendar_id'       => $parent_event_id,
            'title'             => $data['event']['title'],
            'old_date_begin'    => new Of_Date($data['event']['recurrence_id'], 'GMT'),
            'date_begin'        => $data['event']['date_begin'],
            'duration'          => $data['event']['event_duration'],
            'all_day'           => $data['event']['all_day'],
            'send_mail'         => false,
          );
          $id = run_query_calendar_event_exception_insert($dao_params, $parent_event_data);
        }
        else {
          $in_error = true;
          $this->event_error ++;
        }
      }
      if (!$in_error) {
        $this->updateStates($data['states'], $id);
        $this->updateAlerts($vevent->get('x-obm-alert'), $id);
      }
    } else {
      $in_error = true;
    }
    if ($in_error) {
      $this->event_error ++;
    }
    return $id;
  }

  function updateStates($states, $id) {
    foreach($states as $entity => $stateInfo) {
      foreach($stateInfo as $entityId => $state) {
        if(!is_null($state)) {
          run_query_calendar_update_occurrence_state($id,$entity,$entityId,$state, true, ($this->rights === true));
        }
      }
    }
  }

  function updateAlerts($alerts, $id, $filter = null) {
    if(is_array($alerts))
      foreach($alerts as $alert) {
        if(!$filter || $filter != $alert['user'])
          run_query_calendar_event_alert_insert($id, $alert['user'], $alert['duration']);
      }
  }

  function updateAttendees($id, &$vevent) {
    $data = $this->parseEventData($vevent);
    $this->updateStates($data['states'], $id);
  }

  function updateEvent($id, &$vevent) {

    $data = $this->parseEventData($vevent);
    if(!$this->lazyWrite) {
      //TODO : Hard working update.
    }
    if(!$this->haveAccess($data['event']['owner'])) {
      $data['event']['owner'] = $GLOBALS['obm']['uid'];
    }    
    $data['event']['calendar_id'] = $id;
    $data['event']['organizer'] = $GLOBALS['obm']['uid'];
    run_query_calendar_event_update($data['event'], $data['entities'], $id, true);
    $this->updateStates($data['states'], $id);
    $alert = $vevent->get('x-obm-alert');
    $this->updateAlerts($alert, $id);
  }

  function & getOBMEvent(&$vevent) {
    $eventData = NULL;
    if(($id = $this->getOBMId($vevent->get('uid')))) {
      $eventData = $this->getEventByExtId($id, $vevent->get('recurrence-id'));
    }
    if(is_null($eventData)) {
      $eventData = $this->getEventByData($vevent);
    }
    return $eventData;
  }

  function getOBMId($id) {
    return $id;
  }

  function addAttendee($id, &$vevent) {
  }

  function parseCategories($categories) {
    if(is_null($categories)) {
      return NULL;
    }
    $name = addslashes(array_shift($categories));
    $query = "SELECT eventcategory1_id as id FROM EventCategory1 WHERE
      eventcategory1_label = '$name' AND 
      eventcategory1_domain_id ".sql_parse_id($GLOBALS['obm']['domain_id'], true);
    $this->db->query($query);
    if($this->db->next_record()) {
      return $this->db->f('id');
    }
    return NULL;
  }

  function parsePrivacy($value) {
    if(strtolower($value) == 'private') {
      return 1;
    }
    return 0;
  }

  function parseOrganizer($organizer) {
    if($this->haveAccess($organizer)) {
      return $organizer;
    } else {
      return $GLOBALS['obm']['uid'];
    }

  }

  function parsePriority($value) {
    if($value > 5 ) {
      return 1;
    } elseif ($value < 5) {
      return 3;
    }
    return 2;
  }

  function & parseRrule($rrule, &$vevent) {
    $event = array();
    if(!isset($rrule['kind']) || is_null($rrule)||!in_array($rrule['kind'],$this->frequency) ) {
      //FIXME Error Handler.
      $event['repeat_kind'] = 'none';
      $event['repeatfrequency'] =1;
      return $event;
    }
    $event['repeatfrequency'] = $rrule['interval'];
    $countFactor = $rrule['interval'];
    switch ($rrule['kind'])  {
      case 'yearly' :
        $countUnit = 'year';
        break;    
      case 'daily' :
        if(is_null($rrule['byday'])) {
          $countUnit = 'day';
          break;
        }
        $rrule['kind'] = 'weekly';
      case 'weekly' :
        $countUnit = 'day';
        $days = '0000000';
        if(!is_null($rrule['byday'])) {
          $countFactor = $countFactor / count($rrule['byday']) * 7;
          foreach($rrule['byday'] as $day) {
            $index = date('w', strtotime($day)); // - date('w', strtotime($GLOBALS['ccalendar_weekstart']));
            $days[$index] = '1';
          }
        }
        $event['repeat_days'] = $days;
        break;
      case 'monthly' :
        $countUnit = 'month';
        if(!is_null($rrule['byday'])) {
          $rrule['kind'] = 'monthlybyday';
        } else {
          $rrule['kind'] = 'monthlybydate';
        }
        break;
      case 'monthlybyday' :
        $countUnit = 'month';
        break;        
      case 'monthlybydate' :
        $countUnit = 'month';
        break;        
    }
    $event['repeat_kind'] = $rrule['kind'];
    if(!is_null($rrule['until'])) {
      $event['repeat_end'] = $rrule['until'];
    }elseif(!is_null($rrule['count'])) {
      $countFactor = ceil($countFactor * ($rrule['count'] - 1));
      $repeatEnd = clone $vevent->get('dtstart');
      $repeatEnd->custom("+$countFactor $countUnit");
      $event['repeat_end'] = $repeatEnd;      
    }else {
      $event['repeat_end'] = NULL;
    }
    return $event;
  }

  function haveAccess($organizer) {
    if($this->rights === true || in_array($organizer,$this->rights)) {
      return true;
    }
    return false;
  }

  function getEventAttendees($eventId){
    $db = new DB_OBM;
    $query =  "SELECT     UserObm.*
               FROM       Event
               INNER JOIN EventLink ON Event.event_id = EventLink.eventlink_event_id
               INNER JOIN UserEntity ON EventLink.eventlink_entity_id = UserEntity.userentity_entity_id
               INNER JOIN UserObm ON UserEntity.userentity_user_id = UserObm.userobm_id
               WHERE      event_id = '".$eventId."'";
    $db->query($query);
    if($db->nf() == 0)
      return null;
    
    $attendees = array();
    while($db->next_record()){
      $attendees[$db->f("userobm_id")] = $this->db->Record;
    }
    return $attendees;
  }

}

?>
