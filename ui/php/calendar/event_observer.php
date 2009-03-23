<?php

class EventObserver
{
  private $mailer;
  
  public function __construct()
  {
    $this->mailer = new CalendarMailer();
    $this->userId = $GLOBALS['obm']['uid'];
  }
  
  public function afterInsert($event)
  {
    if (is_array($event['sel_user_id'])) {
      $attendees = array_diff($event['sel_user_id'], array($this->userId));
      if (!empty($attendees)) {
        $this->mailer->sendEventInvitation($event, $attendees);
      }
    }
    if (is_array($event['sel_resource_id'])) {
      foreach ($event['sel_resource_id'] as $resourceId) {
        $resourceOwners = array_keys(OBM_Acl::getEntityWriters('resource', $resourceId));
        if (in_array($this->userId, $resourceOwners) || empty($resourceOwners)) continue;
        $this->mailer->sendResourceReservation($event, $resourceOwners);
      }
    }
  }
  
  public function afterUpdate($event)
  {
    $oldEvent = run_query_calendar_detail($event['calendar_id']);
    $oldEvent = $oldEvent->Record;
    $oldEvent['date_begin'] = new Of_Date($oldEvent['event_date'], 'GMT');
    $oldEvent['date_end'] = clone $oldEvent['date_begin'];
    $oldEvent['date_end']->addSecond($oldEvent['event_duration']);
    
    /*$oldtitle = $oldEvent->f('event_title');
    $old_start = new Of_Date($oldEvent->f('event_date'), 'GMT');
    $old_end = clone $old_start;
    $old_end->addSecond($oldEvent->f('event_duration'));
    $old_location = $oldEvent->f('event_location');
    $old_all_day = $oldEvent->f('event_allday');
    $old_duration = $oldEvent->f('event_duration');
    $old_repeatkind = $oldEvent->f('event_repeatkind');
    $old_repeatdays = $oldEvent->f('event_repeatdays');
    $old_repeatend = new Of_Date($oldEvent->f('event_endrepeat'),'GMT');
    $old_repeatfrequence = $oldEvent->f('event_repeatfrequence');
    $title = $event['title'];
    $start =  $event['date_begin'];
    $end = $event['date_end'];
    $location = $event['location'];
    ($event['all_day'] != 1)? $all_day = FALSE : $all_day = TRUE;
    $duration = $event['event_duration'];
    $repeatkind = $event['repeat_kind'];
    $repeatdays = $event['repeat_days'];
    $repeatend = $event['repeat_end'];
    $repeatfrequence = $event['repeatfrequency'];
    $owner = $oldEvent->f('owner_lastname').' '.$oldEvent->f('owner_firstname');

    $sendUpdateMail = false;
    $sendUpdateMail = (($location != $old_location) || $sendUpdateMail);
    $sendUpdateMail = (($all_day != $old_all_day) || $sendUpdateMail);
    $sendUpdateMail = (($start->compare($old_start) != 0) || $sendUpdateMail);
    $sendUpdateMail = (($duration != $old_duration) || $sendUpdateMail);
    $sendUpdateMail = (($repeatkind != $old_repeatkind) || $sendUpdateMail);
    if($repeatkind != 'none') {
      $sendUpdateMail = (($repeatdays != $old_repeatdays) || $sendUpdateMail);
      $sendUpdateMail = (($repeatend->compare($old_repeatend) != 0) || $sendUpdateMail);
      $sendUpdateMail = (($repeatfrequence != $old_repeatfrequence) || $sendUpdateMail);
    }*/
    
    $sendUpdateMail = $this->hasEventChanged($event, $oldEvent); 

    $date_exception = array();
    $exceptions = array(); 
    $p_exceptions = array();
    $new_exceptions = array();
    $old_exceptions = array();
    $exceptions_q = run_query_get_one_event_exception($event['calendar_id']);
    $entities = get_calendar_event_entity($event['calendar_id']); 
    
    if (is_array($entities['user']['ids'])) {
      $attendees_q = $entities['user']['ids'];
    } else {
      $attendees_q = array();
    }
  
    if (is_array($entities['resource']['ids'])) {
      $resources_q = $entities['resource']['ids'];
    } else {
      $resources_q = array();
    }
    
    $attendees = array_intersect($event['sel_user_id'], $attendees_q);
    $attendees = array_diff($attendees, array($obm['uid']));
    $old_attendees = array_diff($attendees_q,$event['sel_user_id'], array($obm['uid']));
    $new_attendees = array_diff($event['sel_user_id'],$attendees_q, array($obm['uid']));
    if (is_array($event['sel_resource_id'])) {
      $resources = array_intersect($event['sel_resource_id'], $resources_q);
      $old_resources = array_diff($resources_q,$event['sel_resource_id']);
      $new_resources = array_diff($event['sel_resource_id'],$resources_q);
    } else {
      $resources = array();
      $old_resources = $resources_q;
      $new_resources = array();
    }
  
    $mail_datas['reset_resource_state'] = $sendUpdateMail && (count($resources) > 0);
    $sendUpdateMail = $sendUpdateMail && (count($attendees) > 0);
    if (count($old_attendees) > 0) {
      $mail_datas['remove_attendee'] = true;
    }
    if (count($new_attendees) > 0) {
      $mail_datas['add_attendee'] = true;
    }
    if (count($old_resources) > 0) {
      $mail_datas['remove_resource'] = true;
    }
    if (count($new_resources) > 0) {
      $mail_datas['add_resource'] = true;
    }
    
    if (!$sendUpdateMail) {
      $date_exception = $event['date_exception'];
      if (!is_array($date_exception)) {
        $date_exception = array();
      }
      while ($exceptions_q->next_record()) {
        $exceptions[] = new Of_Date($exceptions_q->f('eventexception_date'), 'GMT');
      }
      $old_exceptions = array_udiff($exceptions,$date_exception, array('Of_Date', 'cmpDate'));
      $new_exceptions = array_udiff($date_exception,$exceptions, array('Of_Date', 'cmpDate'));
      if (count($old_exceptions) > 0) {
        $mail_datas['remove_exception'] = true;
      }
      if (count($new_exceptions) > 0) {
        $mail_datas['add_exception'] = true;
      }
    }
    
    if ($sendUpdateMail) {
      $this->mailer->sendEventUpdate($event, $oldEvent, $attendees);
    }
  }
  
  private function hasEventChanged($event, $oldEvent)
  {
    return $event['location'] != $oldEvent['event_location']
      || $event['all_day']  != $oldEvent['event_allday']
      || $event['date_begin']->compare($oldEvent['date_begin']) != 0
      || $event['event_duration'] != $oldEvent['event_duration']
      || $event['repeat_kind']    != $oldEvent['event_repeatkind']
      || $event['repeat_kind']    != $oldEvent['event_repeatkind']
      || ($event['repeat_kind'] != 'none' && $event['repeat_days'] != $oldEvent['event_repeatdays'])
      || ($event['repeat_kind'] != 'none' && $event['repeat_end']->compare($oldEvent['event_endrepeat'], 'GMT') != 0)
      || ($event['repeat_kind'] != 'none' && $event['repeatfrequency'] != $oldEvent['event_repeatfrequence']);
  }
}