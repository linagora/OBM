<?php
include_once("CronJob.class.php");
define('DAY_DURATION',86400);
global $day_duration;
$day_duration = DAY_DURATION;
/**
 * CalendarAlertCronJob 
 * 
 * @uses CronJob
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class CalendarAlertCronJob extends CronJob{

  var $jobDelta = 120;

  /**
   * mustExecute 
   * 
   * @param mixed $date 
   * @access public
   * @return void
   */
  function mustExecute($date) {
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

    include_once('obminclude/lang/fr/calendar.inc');
    $delta = $this->jobDelta - 1;
    $this->getAlerts($date, $date + $delta);

    $of = &OccurrenceFactory::getInstance();
    $occurrences = $of->getOccurrences();

    foreach($occurrences as $occurrence) {
      $event = $occurrence->event;
      $delta = $this->getAlertDelta($event->id);
      
      if($occurrence->date + $this->jobDelta> $date && $occurrence->date <= $date  + $delta) {
        $this->logger->debug("Alert for event ".$event->id." will be sent");
        $consult_link = "$GLOBALS[cgp_host]/calendar/calendar_index.php?action=detailconsult&calendar_id=".$event->id;
        $events[$event->id] = array (
          "subject" => sprintf($l_alert_mail_subject,addslashes($event->title)),
          "message" => sprintf($l_alert_mail_body,
                          addslashes($event->title), 
                          date('d/m/Y H:i',$occurrence->date + $delta), 
                          date('d/m/Y H:i',$occurrence->date + $delta + $event->duration), 
                          ($delta/60),
                          $event->location,
                          $consult_link,
                          of_date_format(), date("H:i")
                          ,$GLOBALS['cgp_host']
                       ),
          "recipents" => array_unique(array_keys($event->attendee["user"])),
          "owner" => $event->owner
        );
      } 
    }

    if(is_array($events)) {
      foreach($events as $event_id => $event) {
        $obm["uid"] = $event["owner"];
        send_mail($event["subject"], $event["message"], $event["recipents"], array(), false);
        $this->logger->info("Alert sent to ".implode(",",$event["recipents"])." about $event[subject] by $obm[uid]");
      }
    }    

    $this->deleteDeprecatedAlerts($date + $delta);

    return true;
  }

  /**
   * deleteDeprecatedAlerts 
   * 
   * @param mixed $date 
   * @access public
   * @return void
   */
  function deleteDeprecatedAlerts($date) {
    $obm_q = new DB_OBM;
    $obm2_q = new DB_OBM;
    $db_type = $obm_q->type;
    $calendarevent_endrepeat = sql_date_format($db_type,"calendarevent_endrepeat");
    $calendarevent_date = sql_date_format($db_type,"calendarevent_date");

    $this->logger->debug("Deleting alerts older than ".date("Y-m-d H:i:s",$date));
    $query = "
      SELECT calendaralert_user_id, calendaralert_event_id FROM CalendarAlert
      LEFT JOIN CalendarEvent ON calendarevent_id = calendaralert_event_id 
      WHERE 
      calendarevent_id IS NULL 
      OR ($calendarevent_date - calendaralert_duration < $date AND calendarevent_repeatkind = 'none')
      OR ($calendarevent_endrepeat - calendaralert_duration < $date AND calendarevent_repeatkind != 'none')";
    $obm_q = new DB_OBM;
    $this->logger->core($query);
    $obm_q->query($query);
    while($obm_q->next_record()) {
      $query = "DELETE FROM CalendarAlert WHERE calendaralert_event_id = ".$obm_q->f('calendaralert_event_id')."
                AND calendaralert_user_id = ".$obm_q->f('calendaralert_event_id');
      $this->logger->core($query);
      $obm2_q->query($query);
    }
    $this->logger->info($obm_q->nf()." alerts deleted");
  }

  /**
   * getAlerts 
   * 
   * @param mixed $start_time 
   * @param mixed $end_time 
   * @access public
   * @return void
   */
  function getAlerts($start_time, $end_time) {
    $this->logger->debug("Getting alerts between ".date("Y-m-d H:i:s",$start_time)." and ".date("Y-m-d H:i:s",$end_time));
    $of = &OccurrenceFactory::getInstance();
    $of->setBegin($start_time);
    $of->setEnd($end_time);
    $this->getSimpleAlerts($start_time, $end_time);
    $this->getReccurentAlerts($start_time, $end_time);
  }

  /**
   * getSimpleAlerts 
   * 
   * @param mixed $start_time 
   * @param mixed $end_time 
   * @access public
   * @return void
   */
  function getSimpleAlerts($start_time, $end_time) {
    $of = &OccurrenceFactory::getInstance();
    $this->logger->debug("Getting alerts on non-reccurent events between ".date("Y-m-d H:i:s",$start_time)." and ".date("Y-m-d H:i:s",$end_time));
    $nr_q = run_query_calendar_no_repeat_alerts($start_time,$end_time);
    $this->logger->debug($nr_q->nf()." potentials alerts founded on non-reccurent event");
    while ($nr_q->next_record()) {
      $id = $nr_q->f("calendarevent_id");
      $title = $nr_q->f("calendarevent_title");
      $privacy = $nr_q->f("calendarevent_privacy");
      $description = $nr_q->f("calendarevent_description"); 
      $entity_label = $nr_q->f("userobm_lastname") ." ".$nr_q->f("userobm_firstname");
      $location = $nr_q->f("calendarevent_location"); 
      $category1 = $nr_q->f("calendarcategory1_label");
      $priority = $nr_q->f("calendarevent_priority");
      $date = $nr_q->f("calendarevent_date");
      $duration = $nr_q->f("calendarevent_duration");
      $state = $nr_q->f("evententity_state");
      $all_day = $nr_q->f("calendarevent_allday");
      $entity = $nr_q->f("evententity_entity");
      $entity_id = $nr_q->f("evententity_entity_id");
      $owner = $nr_q->f("calendarevent_owner");
      if (isset($of->events[$id])) {
        $event = &$of->events[$id];
      } else {
        $event = &new Event($id,$duration,$title,$location,$category1,$privacy,$description,$properties,$all_day,'none',$owner,$color);
      }
      $this->logger->debug("$entity $entity_id ($entity_label) added on event ".$event->id);
      $event->addAttendee($entity,$entity_id,$entity_label,$state);
      $of->addOccurrence($event, $date, $entity, $entity_id);
    }    
  }

  /**
   * getReccurentAlerts 
   * 
   * @param mixed $start_time 
   * @param mixed $end_time 
   * @access public
   * @return void
   */
  function getReccurentAlerts($start_time, $end_time) {
    $of = &OccurrenceFactory::getInstance();
    $this->logger->debug("Getting alerts on reccurent events between ".date("Y-m-d H:i:s",$start_time)." and ".date("Y-m-d H:i:s",$end_time));
    $r_q = run_query_calendar_repeat_alerts($start_time,$end_time);
    $this->logger->debug($r_q->nf()." potentials alerts founded on reccurent events");
    while ($r_q->next_record()) {
      $d = $r_q->f("calendarevent_date");
      $id = $r_q->f("calendarevent_id");
      $title = $r_q->f("calendarevent_title");
      $privacy = $r_q->f("calendarevent_privacy");
      $description = $r_q->f("calendarevent_description"); 
      $location = $r_q->f("calendarevent_location"); 
      $category1 = $r_q->f("calendarcategory1_label");
      $date = $r_q->f("calendarevent_date");
      $duration = $r_q->f("calendarevent_duration");
      $repeatkind = $r_q->f("calendarevent_repeatkind");
      $endrepeat = $r_q->f("calendarevent_endrepeat");
      $all_day = $r_q->f("calendarevent_allday");     
      $repeatfrequence = $r_q->f("calendarevent_repeatfrequence");
      $repeatdays = $r_q->f("calendarevent_repeatdays");
      $entity = $r_q->f("evententity_entity");
      $entity_id = $r_q->f("evententity_entity_id");    
      $entity_label = $r_q->f("userobm_lastname") ." ".$r_q->f("userobm_firstname");
      $state = $r_q->f("evententity_state");
      $all_day = $r_q->f("calendarevent_allday");       
      $owner = $r_q->f("calendarevent_owner");
      if (!$endrepeat) {
        $endrepeat = $end_time;
      }
      if (isset($of->events[$id])) {
        $event = &$of->events[$id];
      } else {
        $event = &new Event($id,$duration,$title,$location,$category1,$privacy,$description,$properties,$all_day,$repeatkind,$owner,$color);
      }
      $this->logger->debug("$entity $entity_id ($entity_label) added on event ".$event->id);
      $event->addAttendee($entity,$entity_id,$entity_label,$state);      
      $event_start =  $start_time ;
      $delta = date("H",$date) * 3600 + date("i",$date) * 60 + date("s",$date) + $duration;
      $delta = floor($delta/DAY_DURATION);
      $event_start -= $delta * DAY_DURATION;
      $end_date = ($endrepeat < $end_time) ? $endrepeat : $end_time;
      $end_date += DAY_DURATION;
      switch ($repeatkind) {
        case "daily" :
          calendar_daily_repeatition($date,$event_start,$end_date,$repeatfrequence,$event,$entity_id,$entity);	
          break; 
        case "weekly" :
          calendar_weekly_repeatition($date,$event_start,$end_date,$repeatdays,$repeatfrequence,$event,$entity_id,$entity); 
          break;
        case "monthlybyday" :
          $stored = calendar_monthlybyday_repeatition($date,$event_start,$end_date,$repeatfrequence,$event,$entity_id,$entity); 
          break;
        case "monthlybydate" :
          $stored = calendar_monthlybydate_repeatition($date,$event_start,$end_date,$repeatfrequence,$event,$entity_id,$entity);
          break;
        case "yearly" :
          $stored = calendar_yearly_repeatition($date,$event_start,$end_date,$repeatfrequence,$event,$entity_id,$entity);
          break;	
      }
    }

    $this->logger->debug("Removing exceptions");
    if (count($of->events) > 0) {
      $exception_q = run_query_get_events_exception(array_keys($of->events),$start_time,$end_time);
      $this->logger->debug($exception_q->nf()." exceptions founded");
      while($exception_q->next_record()) {
        $of->removeOccurrences($exception_q->f('calendarexception_event_id'), $exception_q->f('calendarexception_date'));
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
  function getAlertDelta($id) {
    $query = "SELECT calendaralert_duration from CalendarAlert WHERE calendaralert_event_id = '$id'";
    $obm_q = new DB_OBM;
    $this->logger->core($query);
    $obm_q->query($query);
    $obm_q->next_record();
    $this->logger->debug("Reminder delta for event ".$id." is ".$obm_q->f('calendaralert_duration')." seconds");
    return $obm_q->f('calendaralert_duration');
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
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date");
  $calendarevent_date_l = sql_date_format($db_type,"calendarevent_date");


  $query = "SELECT
      calendarevent_id,
      calendarevent_title,
      calendarevent_privacy,
      calendarevent_description,
      calendarevent_location,
      calendarevent_repeatfrequence,
      calendarevent_owner,
      evententity_entity_id,
      evententity_entity,
      evententity_state,
      $calendarevent_date_l - calendaralert_duration as calendarevent_date,
      calendaralert_duration,
      calendarevent_duration,
      calendarevent_allday,
      userobm_lastname,
      userobm_firstname
    FROM CalendarAlert
      JOIN CalendarEvent ON calendarevent_id = calendaralert_event_id  
      JOIN EventEntity ON calendarevent_id = evententity_event_id AND calendaralert_user_id = evententity_entity_id AND evententity_entity = 'user'
      JOIN UserObm ON userobm_id = evententity_entity_id
    WHERE evententity_state = 'A'
      AND calendarevent_repeatkind = 'none'
      AND ($calendarevent_date - calendaralert_duration) >= $start
      AND ($calendarevent_date - calendaralert_duration) <=  $end
      AND  calendaralert_duration > 0
      ORDER BY calendarevent_date
";

  Logger::log($query,L_CORE,"calendaralertcronjob");
  $obm_q->query($query);
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
  $calendarevent_endrepeat = sql_date_format($db_type,"calendarevent_endrepeat");
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date");
  $calendarevent_date_l = sql_date_format($db_type,"calendarevent_date");
  $calendarevent_endrepeat_l = sql_date_format($db_type,"calendarevent_endrepeat","calendarevent_endrepeat");

  $query = "SELECT
      calendarevent_id,
      calendarevent_title,
      calendarevent_privacy,
      calendarevent_description, 
      calendarevent_location, 
      $calendarevent_date_l - calendaralert_duration as calendarevent_date,
      calendaralert_duration,
      calendarevent_duration,
      calendarevent_repeatkind,
      $calendarevent_endrepeat_l,
      calendarevent_repeatfrequence,
      calendarevent_owner,
      evententity_entity,
      evententity_entity_id,
      evententity_state,
      calendarevent_repeatdays,
      calendarevent_allday,
      userobm_lastname,
      userobm_firstname
    FROM CalendarAlert
      JOIN CalendarEvent ON calendarevent_id = calendaralert_event_id
      JOIN EventEntity ON calendarevent_id = evententity_event_id AND calendaralert_user_id = evententity_entity_id AND evententity_entity = 'user'
      JOIN UserObm ON userobm_id = evententity_entity_id
    WHERE calendarevent_repeatkind != 'none'
      AND evententity_state = 'A'
      AND ($calendarevent_date  - calendaralert_duration) <= $end 
      AND (($calendarevent_endrepeat  - calendaralert_duration) >= $start
      OR $calendarevent_endrepeat = '0')
      AND  calendaralert_duration > 0
    ORDER BY calendarevent_date"; 

  Logger::log($query,L_CORE,"calendaralertcronjob");
  $obm_q->query($query);
  return $obm_q;
}
?>
