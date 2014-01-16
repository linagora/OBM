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


include_once("CronJob.class.php");
define('DAY_DURATION',86400);
global $day_duration;
$day_duration = DAY_DURATION;
/**
 * EventAlertCronJob 
 * 
 * @uses CronJob
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 */
class EventAlertCronJob extends CronJob{

  var $jobDelta = 120;

  /**
   * mustExecute 
   * 
   * @param mixed $date 
   * @access public
   * @return void
   */
  function mustExecute($date) {
    $date = new Of_Date($date);
    $min = date("i");
    $modulo = $this->jobDelta / 60;
    return ($min%$modulo === 0);
  }

  /**
   * getJobsFiles 
   * 
   * @access public
   * @return void
   */
  function getJobsFiles() {
    return array('php/calendar/calendar_query.inc');
  }

  /**
   * execute 
   * 
   * @param mixed $date 
   * @access public
   * @return void
   */
  function execute($date) {
    global $obm;

    include('obminclude/lang/fr/calendar.inc');
    $lang_file = 'conf/lang/fr/calendar.inc';
    if (file_exists("{$includePath}/{$lang_file}")) {
      include("$lang_file");
    }    
    $date = new Of_Date($date);
    $end = clone $date;
    $end = $end->addSecond($this->jobDelta);    
    $this->getAlerts($date, $end);

    $of = &OccurrenceFactory::getInstance();
    $occurrences = $of->getOccurrences();

    foreach($occurrences as $occurrence) {
      $event = $occurrence->event;

      $delta = $this->getAlertDelta($event->id, $occurrence->id);
      
      if($occurrence->date->compare($date)>=0 && $occurrence->date->compare($end)<0) {
        $current = clone $occurrence->date;
        $this->logger->debug("Alert for event ".$event->id." will be sent");
        $consult_link = "$GLOBALS[cgp_host]/calendar/calendar_index.php?action=detailconsult&calendar_id=".$event->id;
        if(isset($events[$event->id])) {
          $recipients = $events[$event->id]['recipients'];
        } else {
          $recipients = array();
        }        
        array_push($recipients, $occurrence->id);
        $events[$event->id] = array (
          "subject" => sprintf($l_alert_mail_subject,($event->title)),
          "message" => sprintf($l_alert_mail_body,
                          ($event->title), 
                          $current->addSecond($delta)->getOutputDateTime(), 
                          $current->addSecond($event->duration)->getOutputDateTime(), 
                          ($delta/60),
                          $event->location,
                          $consult_link,
                          Of_Date::today()->getOutputDate(), Of_Date::today()->get(Of_Date::TIME_SHORT)
                          ,$GLOBALS['cgp_host']
                       ),
          "recipients" => array_unique($recipients),
          "owner" => $event->owner
        );
      } 
    }

    if(is_array($events)) {
      foreach($events as $event_id => $event) {
        $obm["uid"] = $event["owner"];
        send_mail($event["subject"], $event["message"], $event["recipients"], array(), false);
        $this->logger->info("Alert sent to ".implode(",",$event["recipients"])." about $event[subject] by $obm[uid]");
      }
    }    
    $current = clone $date;
    $this->deleteDeprecatedAlerts($current->addSecond($this->jobDelta));

    return true;
  }

  /**
   * deleteDeprecatedAlerts 
   * 
   * @param mixed $date 
   * @access public
   * @return void
   */
  function deleteDeprecatedAlerts($datetime) {
    $obm_q = new DB_OBM;
    $obm2_q = new DB_OBM;
    $db_type = $obm_q->type;
    $this->logger->debug("Deleting alerts older than $datetime");
    $date = clone $datetime;
    $date = $date->setHour(0)->setMinute(0)->setSecond(0);
    $query = "
      SELECT eventalert_user_id, eventalert_event_id FROM EventAlert
      LEFT JOIN Event ON event_id = eventalert_event_id 
      WHERE 
      event_id IS NULL 
      OR (#SUBSECONDS(event_date , eventalert_duration) <= '$datetime' AND event_repeatkind = 'none')
      OR (event_endrepeat < '$date' AND event_repeatkind != 'none')";
    $obm_q = new DB_OBM;
    $this->logger->core($query);
    $obm_q->xquery($query);
    while($obm_q->next_record()) {
      if ( $obm_q->f('eventalert_event_id') ) {
        $eventalert_selector = " = ".$obm_q->f('eventalert_event_id');
      } else {
        $eventalert_selector = " IS NULL";
      }
      $query = "DELETE FROM EventAlert WHERE eventalert_event_id ".$eventalert_selector." 
                AND eventalert_user_id = ".$obm_q->f('eventalert_user_id');
      $this->logger->core($query);
      $obm2_q->query($query);
    }
    $this->logger->info($obm_q->nf()." alerts deleted");
  }

  /**
   * getAlerts 
   * 
   * @param mixed $start
   * @param mixed $end
   * @access public
   * @return void
   */
  function getAlerts($start, $end) {
    $this->logger->debug("Getting alerts between $start and $end");
    $of = &OccurrenceFactory::getInstance();
    $of->setBegin($start);
    $of->setEnd($end);
    $this->getSimpleAlerts($start, $end);
    $this->getReccurentAlerts($start, $end);
  }

  /**
   * getSimpleAlerts 
   * 
   * @param mixed $start
   * @param mixed $end
   * @access public
   * @return void
   */
  function getSimpleAlerts($start, $end) {
    $of = &OccurrenceFactory::getInstance();
    $this->logger->debug("Getting alerts on non-reccurent events between $start and $end");
    $nr_q = run_query_calendar_no_repeat_alerts($start,$end);
    $this->logger->debug($nr_q->nf()." potentials alerts founded on non-reccurent event");
    while ($nr_q->next_record()) {
      $id = $nr_q->f("event_id");
      $title = $nr_q->f("event_title");
      $privacy = $nr_q->f("event_privacy");
      $description = $nr_q->f("event_description"); 
      $entity_label = $nr_q->f("userobm_lastname") ." ".$nr_q->f("userobm_firstname");
      $location = $nr_q->f("event_location"); 
      $category1 = $nr_q->f("eventcategory1_label");
      $priority = $nr_q->f("event_priority");
      $date = new Of_Date($nr_q->f("event_date"),'GMT');
      $duration = $nr_q->f("event_duration");
      $state = $nr_q->f("eventlink_state");
      $all_day = $nr_q->f("event_allday");
      $entity = $nr_q->f("eventlink_entity");
      $entity_id = $nr_q->f("eventlink_entity_id");
      $owner = $nr_q->f("event_owner");
      $opacity = $nr_q->f('event_opacity');
      $tag = $nr_q->f('eventtag_id');
      if (isset($of->events[$id])) {
        $event = &$of->events[$id];
      } else {
        $event = new Event($id,$duration,$title,$location,$category1,$privacy,$description,$properties,$all_day,'none',$owner, '',$color,$opacity,$tag);
      }
      $this->logger->debug("$entity $entity_id ($entity_label) added on event ".$event->id);
      $event->addAttendee($entity,$entity_id,$entity_label,$state);
      $of->addOccurrence($event, $date, $entity, $entity_id);
    }    
  }

  /**
   * getReccurentAlerts 
   * 
   * @param mixed $start 
   * @param mixed $end 
   * @access public
   * @return void
   */
  function getReccurentAlerts($start, $end) {
    $of = &OccurrenceFactory::getInstance();
    $this->logger->debug("Getting alerts on reccurent events between $start and $end");
    $r_q = run_query_calendar_repeat_alerts($start,$end);
    $this->logger->debug($r_q->nf().' potentials alerts founded on reccurent events');
    while ($r_q->next_record()) {
      $id = $r_q->f('event_id');
      $title = $r_q->f('event_title');
      $privacy = $r_q->f('event_privacy');
      $description = $r_q->f('event_description');
      $properties = $r_q->f('event_properties');
      $location = $r_q->f('event_location'); 
      $category1 = $r_q->f('eventcategory1_label');
      $date = new Of_Date($r_q->f('event_date'), 'GMT');
      $duration = $r_q->f('event_duration');
      $repeatkind = $r_q->f('event_repeatkind');
      $endrepeat = new Of_Date($r_q->f('event_endrepeat'),'GMT');
      $entity = $r_q->f('eventlink_entity');
      $all_day = $r_q->f('event_allday');
      $color = $r_q->f('event_color');
      $repeatfrequence = $r_q->f('event_repeatfrequence');
      $repeatdays = $r_q->f('event_repeatdays');
      $entity_id = $r_q->f('eventlink_entity_id');    
      $entity_state = $r_q->f('eventlink_state');
      $owner = $r_q->f('event_owner');
      $entity_label = $r_q->f('userobm_lastname') .' '.$r_q->f('userobm_firstname');
      $state = $r_q->f('eventlink_state');
      $timezone = $r_q->f('event_timezone');
      $opacity = $r_q->f('event_opacity');
      $tag = $r_q->f('eventtag_id');
      if ($endrepeat->error() == Of_Date::WARN_EMPTY_DATE) {
        $endrepeat = $end;
      }
      if (isset($of->events[$id])) {
        $event = $of->events[$id];
      } else {
        $event = new Event($id,$duration,$title,$location,$category1,$privacy,$description,$properties,$all_day,$repeatkind,$owner,'',$color,$opacity,$tag);
        $event->setTimezone($timezone);
      }
      $this->logger->debug("$entity $entity_id ($entity_label) added on event ".$event->id);
      $tz = new DateTimeZone($timezone);
      $date->setTimezone($tz);
      $event->addAttendee($entity,$entity_id,$entity_label,$state);      
      $event_start = clone $start;
      $event_start->setTimezone($tz); 
      $event_start->subSecond($duration)->setHour($date)->setMinute($date)->setSecond($date);
      $event_end = ($end->compare($endrepeat) > 0)? clone $endrepeat: clone $end; 
      $event_end->setTimezone($tz);
      $event_end->setHour($date)->setMinute($date)->setSecond($date)->addSecond($duration);
      calendar_add_anonymous_occurrences($repeatkind, $date, $event_start, $event_end, $repeatfrequence, $event, $entity_id, $entity, $entity_state, $repeatdays);
    }
    $this->logger->debug("Removing exceptions");
    if (count($of->events) > 0) {
      $exception_q = run_query_get_events_exception(array_keys($of->events),$start,NULL);
      $this->logger->debug($exception_q->nf()." exceptions founded");
      while($exception_q->next_record()) {
        $of->removeOccurrences($exception_q->f('eventexception_parent_id'), new Of_Date($exception_q->f('eventexception_date'), 'GMT'));
      }
    }
  }

  /**
   * getAlertDelta 
   * 
   * @param mixed $id 
   * @access public
   * @return void
   */
  function getAlertDelta($id, $user_id) {
    $query = "SELECT eventalert_duration FROM EventAlert WHERE eventalert_event_id = '$id' AND eventalert_user_id = $user_id";
    $obm_q = new DB_OBM;
    $this->logger->core($query);
    $obm_q->query($query);
    $obm_q->next_record();
    $this->logger->debug("Reminder delta for event ".$id." is ".$obm_q->f('eventalert_duration')." seconds");
    return $obm_q->f('eventalert_duration');
  }

}

///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a week of users or/and groups
// Parameters:
//   - $start           : timestamp of start date
//   - $end             : timestamp of end date
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_no_repeat_alerts($start,$end) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $query = "SELECT
      event_id,
      event_title,
      event_privacy,
      event_description,
      event_location,
      event_repeatfrequence,
      event_owner,
      userentity_user_id as eventlink_entity_id,
      'user' as eventlink_entity,
      eventlink_state,
      #SUBSECONDS(event_date,eventalert_duration) as event_date,
      eventalert_duration,
      event_duration,
      event_allday,
      userobm_lastname,
      event_timezone,
      userobm_firstname
    FROM EventAlert
      INNER JOIN Event ON event_id = eventalert_event_id  
      INNER JOIN UserEntity ON userentity_user_id = eventalert_user_id
      INNER JOIN EventLink ON event_id = eventlink_event_id AND eventlink_entity_id = userentity_entity_id 
      INNER JOIN UserObm ON userobm_id = userentity_user_id 
    WHERE eventlink_state = 'ACCEPTED'
      AND event_repeatkind = 'none'
      AND #SUBSECONDS(event_date,eventalert_duration) >= '$start'
      AND #SUBSECONDS(event_date,eventalert_duration) <=  '$end'
      AND  eventalert_duration >= 0
      ORDER BY event_date
";

  Logger::log($query,L_CORE,"eventalertcronjob");
  $obm_q->xquery($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a week of users or/and groups
// Parameters:
//   - $calendar          : calendar params
//   - $calendar_entity : array of entities elements to display
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_repeat_alerts($start, $end) {
  global $cdg_sql;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $query = "SELECT
      event_id,
      event_title,
      event_privacy,
      event_description, 
      event_location, 
      #SUBSECONDS(event_date, eventalert_duration) as event_date,
      eventalert_duration,
      event_duration,
      event_repeatkind,
      event_endrepeat,
      event_repeatfrequence,
      event_owner,
      'user' as eventlink_entity,
      userentity_user_id as eventlink_entity_id,
      eventlink_state,
      event_repeatdays,
      event_allday,
      event_timezone,
      userobm_lastname,
      userobm_firstname
    FROM EventAlert
      INNER JOIN Event ON event_id = eventalert_event_id
      INNER JOIN UserEntity ON userentity_user_id = eventalert_user_id
      INNER JOIN EventLink ON event_id = eventlink_event_id AND eventlink_entity_id = userentity_entity_id 
      INNER JOIN UserObm ON userobm_id = userentity_user_id 
    WHERE event_repeatkind != 'none'
      AND eventlink_state = 'ACCEPTED'
      AND #SUBSECONDS(event_date  , eventalert_duration) <= '$end' 
      AND eventalert_duration >= 0
    ORDER BY event_date"; 

  Logger::log($query,L_CORE,'eventalertcronjob');
  $obm_q->xquery($query);
  return $obm_q;
}
?>
