<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : agenda_query.inc                                             //
//     - Desc : Agenda query File                                            //
// 2001-06-27 : Mehdi Rande                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Return events details
// Parameters:
//   - $agenda_id
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_detail($agenda_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type; 
  $timeupdate = sql_date_format($db_type, "calendarevent_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "calendarevent_timecreate", "timecreate");
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date","calendarevent_date");
  $calendarevent_endrepeat = sql_date_format($db_type,"calendarevent_endrepeat", "calendarevent_endrepeat");

  $query = "SELECT DISTINCT
      calendarevent_id,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate,
      $timeupdate,
      $timecreate,
      calendarevent_owner, 
      calendarevent_title, 
      calendarevent_description,
      calendarevent_repeatfrequence,
      calendarevent_location, 
      calendarevent_category1_id,
      calendarcategory1_label,
      calendarevent_privacy,
      calendarevent_priority,
      calendarevent_repeatkind,
      calendarevent_duration,
      calendarevent_repeatdays,
      calendarevent_allday,
      $calendarevent_endrepeat,
      $calendarevent_date
    FROM  
      CalendarEvent
      LEFT JOIN CalendarCategory1 ON calendarevent_category1_id = calendarcategory1_id
      LEFT JOIN UserObm AS c ON calendarevent_usercreate = c.userobm_id
      LEFT JOIN UserObm AS u ON calendarevent_userupdate = u.userobm_id
    WHERE calendarevent_id = '$agenda_id'";

  display_debug_msg($query, $cdg_sql, "run_query_agenda_detail()");
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the name and first name of users
// Parameters: 
//   - $users : users id array, the event is assigned to 
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_get_user_name($users="") {
  global $cdg_sql;

  $in = "'".implode ("','", $users)."'";

  $obm_q = new DB_OBM;
  $query = "SELECT userobm_lastname, userobm_firstname, userobm_id
            FROM UserObm
	    WHERE userobm_id IN ($in)";

  $query.= " ORDER BY userobm_id"; 
  display_debug_msg($query, $cdg_sql, "run_query_agenda_get_user_name()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the label of resources
// Parameters:
//   - $resources : resource id array, the event is assigned to
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_get_resource_label($resources) {
  global $cdg_sql;

  if (count($resources) > 0) {
    $in = "'".implode ("','", $resources)."'";
    $where = "WHERE resource_id IN ($in)";
  }
  $obm_q = new DB_OBM;
  $query = "SELECT resource_name, resource_id
            FROM Resource 
            $where";

  $query.= " ORDER BY resource_id";
  display_debug_msg($query, $cdg_sql, "run_query_agenda_get_resource_label()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the label of entities 
// Parameters:
//   - $entities : entity id array, the event is assigned to
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_get_entity_label($entities) {

  if ((is_array($entities["user"]))
      && (count($entities["user"]) > 0)) {
    $ret["user"] = run_query_agenda_get_user_name($entities["user"]);
  }
  if ((is_array($entities["resource"]))
      && (count($entities["resource"]) > 0)) {
    $ret["resource"] = run_query_agenda_get_resource_label($entities["resource"]);
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Perform the concatenation of users and groups array in a users array with
// users from the users array an users from group from the group array 
// Parameters:
//   - $users_array : List of the users
//   - $groups_array : List of the groups 
// Returns:
//   array of user id
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_get_allusers($users_array,$groups_array) {

  $r_array = array();
  if (is_array($groups_array)) {
    foreach($groups_array as $group_id) {
      $r_array = array_merge($r_array, get_all_users_id_from_group($group_id));
    }
  }
  $r_array = array_merge($users_array,$r_array);
  $r_array = array_unique($r_array);

  return $r_array;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get the resources of the given group                    //
// Parameters:
//   - $group[] : Group parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_resourcegroup($group) {
  global $cdg_sql, $ctype_resource;
  if (is_array($group) && count($group) > 0) {
    
    $obm_q = new DB_OBM;
    $db_type = $obm_q->type;
    $query = "SELECT 
           resource_id,
           resource_name,
           rgroup_name, 
           resourcegroup_rgroup_id as resource_group
           FROM ResourceGroup LEFT JOIN Resource ON resourcegroup_resource_id=resource_id
           LEFT JOIN RGroup ON rgroup_id = resourcegroup_rgroup_id
        WHERE resourcegroup_rgroup_id IN (".implode(",",$group).")";

    display_debug_msg($query, $cdg_sql, "run_query_resource_resourcegroup()");
    $obm_q->query($query);

    return $obm_q;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Insert a user decision for an user
// Parameters:
//   - $agenda : Agenda params
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_insert_decision($agenda) {

  $user = $agenda["user_id"];
  $event_id = $agenda["agenda_id"];
  $state = $agenda["decision_event"];
  run_query_agenda_update_occurence_state($event_id,$user,$state);
}


///////////////////////////////////////////////////////////////////////////////
// Update a user decision for its participation to an event
// Parameters: 
//   - $event_id : event Id
//   - $user_id  : User id
//   - $state    : New state to set (A, W or R)
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_update_occurence_state($event_id, $user_id, $state){
  global $cdg_sql;

  $query = "UPDATE EventEntity 
            SET evententity_state = '$state'
            WHERE evententity_entity_id = $user_id
              AND evententity_event_id = '$event_id'
              AND evententity_entity = 'user'";

  display_debug_msg($query, $cdg_sql, "run_query_agenda_update_occurence_state()");  
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;    
}


///////////////////////////////////////////////////////////////////////////////
// Select All waiting Events
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_waiting_events() {
  global $auth, $cdg_sql;

  $uid = $auth->auth["uid"];
  
  $writable_cal = of_right_entity_for_consumer("calendar", "user", $uid, "write");
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $calendarevent_endrepeat = sql_date_format($db_type,"calendarevent_endrepeat","calendarevent_endrepeat"); 
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date","calendarevent_date");    
  $query = "SELECT attendee.userobm_id,
      u.userobm_lastname as usercreate_lastname,
      u.userobm_firstname as usercreate_firstname,
      attendee.userobm_lastname,
      attendee.userobm_firstname,
      calendarevent_id,		  
      calendarevent_title, 
      calendarevent_location, 
      calendarcategory1_label,
      calendarevent_privacy,
      calendarevent_priority,
      calendarevent_repeatkind,
      calendarevent_repeatdays,
      calendarevent_duration,
      $calendarevent_endrepeat,
      $calendarevent_date		   
    FROM CalendarEvent
      LEFT JOIN CalendarCategory1 ON calendarevent_category1_id = calendarcategory1_id
      LEFT JOIN EventEntity ON calendarevent_id  = evententity_event_id
      LEFT JOIN UserObm attendee ON evententity_entity_id = attendee.userobm_id
      LEFT JOIN UserObm u ON calendarevent_usercreate = u.userobm_id
    WHERE 
      evententity_state = 'W'
      AND evententity_entity = 'user'";

  if (is_array($writable_cal)) {
    $query .= " AND evententity_entity_id IN (";
    $num = count($writable_cal["ids"]);
    foreach($writable_cal["ids"] as $user_id) {
      $query .= "'$user_id',";
    }
    $query.= "'$uid')";
  } else {
    $query .= " AND evententity_entity_id = '$uid'";
  }
  $query .= " ORDER BY userobm_id"; 
  display_debug_msg($query, $cdg_sql, "run_query_agenda_waiting_events()");
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Delete all participation to an event
// Parameters:
//   - $agenda[] : hash values for the event
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_delete_event_entity($agenda) {
  global $cdg_sql;

  $e_id = $agenda["agenda_id"];
  $query = "DELETE FROM EventEntity WHERE evententity_event_id = '$e_id'";

  display_debug_msg($query, $cdg_sql, "run_query_agenda_delete_event_entity()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
} 


///////////////////////////////////////////////////////////////////////////////
// Insert one event
// Parameters: 
//   - $agenda        : Agenda params
//   - $sel_entity_id : List entitis id ["user"] ["resource"]
//   - $event_id      : var to get the id to be returned
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_add_event($agenda, $sel_entity_id, &$event_id) {
  global $uid, $l_from, $l_to, $l_date;
  global $l_add_event_mail_head,$l_add_event_mail_subject;
  global $cagenda_resource_admin, $l_resource_allocated;

  $writable_cal = of_right_entity_for_consumer("calendar", "user", $uid, "write");
  $mail  = $agenda["mail"];
  $groups = $sel_entity_id["group"];
  $mail_title = stripslashes($agenda["title"]);
  $datebegin = $agenda["date_begin"];
  $dateend = $agenda["date_end"];
  if ($agenda["task_new_id"]) $task_id = $agenda["task_new_id"]; 
  else $task_id = $agenda["task_id"];
  // Get the final user list (merge group)
  $users_array = $sel_entity_id["user"];
  if (is_array($groups)) {
    $user_group_array = array();
    foreach($groups as $group_id) {
      $user_group_array = array_merge($user_group_array, get_all_users_id_from_group($group_id));
    }
    $users_array = array_merge($users_array, $user_group_array);
    $users_array = array_unique($users_array);
  }
  if (! is_array($users_array)) {
    $users_array = array();
  }

  // Insert the Event
  $event_id = run_query_agenda_insert_event_data($agenda);   

  // Insert Event users
  foreach ($users_array as $user_id) {
    if ($user_id == $uid || in_array($user_id, $writable_cal["ids"])) {
      run_query_agenda_insert_entity_event("user", $user_id, $event_id, 'A');
    } else {
      run_query_agenda_insert_entity_event("user", $user_id, $event_id, 'W');
    }
  }

  // Insert The event task
  if ($task_id > 0) {
    run_query_agenda_insert_entity_event("task", $task_id, $event_id, 'A');
  }

  // Insert Event resources
  $resources_writable = array(); // Writable Resources  
  $resources_address = array(); // Resource Admin notification
  // Get the final resource list and insert resource links
  $resources_array = $sel_entity_id["resource"];
  if ( (is_array($resources_array)) && (count($resources_array) > 0) ) {
    $resources_array = array_unique($resources_array);
    $writable_res = of_right_entity_for_consumer("resource", "user", $uid, "write");
    foreach ($resources_array as $res_id) {
      if (in_array($res_id, $writable_res["ids"])) {
	run_query_agenda_insert_entity_event("resource", $res_id, $event_id, 'A');
        array_push($resources_writable, $res_id);
      }
    }
  }    
  if ((count($resources_writable) > 0) && ($cagenda_resource_admin != "")) {
    $resources_address = array($cagenda_resource_admin);
    $message_resource = "\r\n\r\n$l_resource_allocated:";
    $resource_q = run_query_get_resources_info($resources_writable);
    while ($resource_q->next_record()) {
      $message_resource .=  "\n  ".$resource_q->f("resource_name");
    }
  }

  // Send mail to users
  $subject = "$l_add_event_mail_subject" . $mail_title;
  $message = "$l_add_event_mail_head $mail_title $l_date : $l_from $datebegin $l_to $dateend $message_resource";
  if (is_array($users_array)) {
    $recipient_array = array_diff($users_array, array($uid));
  } else {
    $recipient_array = array();
  }
  
  if (count($recipient_array) > 0 || count($r_address) > 0) {
    $tmp_filename = secure_tmpname(".ics","ics_");
    create_agenda_ics_meeting_file($agenda, $event_id, $recipient_array, $resources_writable, $tmp_filename);
    $h_file = array(
		    array (
			   "filename" => "$tmp_filename",
			   "name" => "meeting.ics",
			   "type" => "ics"
			   )
		    );
    send_mail($subject, $message, $recipient_array, $resources_address, $mail, $h_file);
    unlink($tmp_filename);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Insert an event data
// Parameters:
//   - $agenda : hashed agenda params
// Returns: Event id inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_insert_event_data($agenda) {
  global $cdg_sql, $uid;

  $now = date("Y-m-d H:i:s");
  $title = $agenda["title"];
  $category1_id = $agenda["category1"];
  $priority = $agenda["priority"];
  $description = $agenda["description"];
  $location = $agenda["location"];
  ($agenda["all_day"] != 1)?$all_day= '0':$all_day = '1';
  $datebegin = $agenda["date_begin"];
  $dateend = $agenda["date_end"];
  $event_duration = $agenda["event_duration"];
  if ($agenda["privacy"]!=1) $privacy = 0; else $privacy = 1; 
  $repeat_kind = $agenda["repeat_kind"];
  $repeat_days = $agenda["repeat_days"]; 
  $repeat_end = $agenda["repeat_end"];
  $repeatfrequency = $agenda["repeatfrequency"];  
  
  $obm_q = new DB_OBM;
  $query = "INSERT INTO CalendarEvent (
    calendarevent_timeupdate,                         
    calendarevent_timecreate,                         
    calendarevent_usercreate,
    calendarevent_owner,
    calendarevent_title, 
    calendarevent_date,     
    calendarevent_description,
    calendarevent_location, 
    calendarevent_category1_id,
    calendarevent_priority,
    calendarevent_privacy, 
    calendarevent_duration,
    calendarevent_repeatkind,
    calendarevent_repeatfrequence,
    calendarevent_repeatdays,
    calendarevent_allday,
    calendarevent_endrepeat)
  VALUES (
    '$now',
    '$now',
    '$uid',
    '$uid',
    '$title',
    '$datebegin',
    '$description',
    '$location',
    '$category1_id',
    '$priority',
    '$privacy',
    '$event_duration',		
    '$repeat_kind',
    '$repeatfrequency',
    '$repeat_days',
    '$all_day',
    '$repeat_end')";

  display_debug_msg($query, $cdg_sql, "run_query_agenda_insert_event_data()");
  $obm_q->query($query);
  
  $query = "SELECT MAX(calendarevent_id) as max_id FROM CalendarEvent";
  display_debug_msg($query, $cdg_sql, "run_query_agenda_insert_event_data()");
  $obm_q->query($query);
  $obm_q->next_record(); 
  $max_id = $obm_q->f("max_id");


  return $max_id;
}


///////////////////////////////////////////////////////////////////////////////
// Insert an occurence of a event
// Parameters: 
//   - $entity : entity type "user", "resource"
//   - $entity_id :
//   - $event_id  :
//   - $state     :
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_insert_entity_event($entity, $entity_id,$event_id, $state) {
  global $cdg_sql,$auth;
  $uid = $auth->auth["uid"];
  $query = "INSERT INTO EventEntity (
    evententity_timecreate,
    evententity_usercreate,
    evententity_event_id, 
    evententity_entity_id,
    evententity_entity,
    evententity_required,
    evententity_state) 
  VALUES (
    NOW(),
    $uid,
    '$event_id',
    '$entity_id',
    '$entity',
    '0',
    '$state')";

  display_debug_msg($query, $cdg_sql, "run_query_entity_user_event()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q; 
}


///////////////////////////////////////////////////////////////////////////////
// Perform the export meeting to the vCalendar format
// Parameters:
//   - $agenda : agenda hash values
///////////////////////////////////////////////////////////////////////////////
function create_agenda_ics_meeting_file($agenda, $event_id, $attendees, $resources, $filename) {
  global $obm_version,$cagenda_weekstart,$auth;
  global $l_file_error, $l_write_error, $l_read_error;
  global $l_resource_allocated;

  $fd_dest = fopen ($filename,"w");
  if ($fd_dest == false) {
    die ($l_file_error.$dest."dead");
  }

  $iso_date = $agenda["date"];
  $ts_date = strtotime($iso_date);  
 
  $start_time = strtotime("-1 year", $ts_date);
  $end_time = strtotime("+1 year", $ts_date);

  $calendar_user = array ($auth->auth["uid"] => "dummy");  
  $obm_event = run_query_agenda_detail($event_id);

  $msg =  "
BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//OBM/OBM V$obm_version Calendar//EN
METHOD:REQUEST";

  if ($obm_event->f("calendarevent_repeatkind") == 'none') {
    $id = $obm_event->f("calendarevent_id");
    $title = "SUMMARY:" . $obm_event->f("calendarevent_title");
    $title = wordwrap($title,74,"\n ",1);
    $location = $obm_event->f("calendarevent_location");
    $priority = $obm_event->f("calendarevent_priority");
    $category1 = strtoupper($obm_event->f("calendarcategory1_label")); 
    $date_ts = $obm_event->f("calendarevent_timeupdate");
    $date_ts = gmdate("Ymd\THis\Z",$date_ts);
    if ($obm_event->f("calendarevent_description") != "") {
      $description = preg_replace("/\\r?\\n/","\\n",$obm_event->f("calendarevent_description"));
      $description = "DESCRIPTION:" . $description;
    } else {
      $description = "";
    }
    if (count($resources) > 0) {
      if ($description == "") $description = "DESCRIPTION:";
      $description .= "$l_resource_allocated:";
      $resource_q = run_query_get_resources_info($resources);
      while ($resource_q->next_record()) {
        $description .=  "\\n  ".$resource_q->f("resource_name");
      }
    }
    $description = "\n" . wordwrap($description,74,"\n ",1);
    $date_b = $obm_event->f("calendarevent_date");
    $date_b = gmdate("Ymd\THis\Z",$date_b);
    $date_e = $obm_event->f("calendarevent_date") + $obm_event->f("calendarevent_duration") ;
    $date_e = gmdate("Ymd\THis\Z",$date_e);
    switch($priority) {
      case 1 : $priority = 3;
	break;
      case 3 : $priority = 1;
	break;
    }
    $private = $obm_event->f("calendarevent_privacy");
    switch($private) {
      case 0 : $private = "PUBLIC";
	break;
      case 1 : $private = "PRIVATE";
	break;
    }        
    $organizer_q = run_query_get_recipients(array($obm_event->f("calendarevent_owner")), 1);
    $organizer_q->next_record();
    $organizer_cn = $organizer_q->f("userobm_lastname")." ".$organizer_q->f("userobm_firstname");
    $organizer_email=$organizer_q->f("userobm_email");
    $attendee_q = run_query_get_recipients($attendees, 1);
    $msg_attendee = "";
    if (is_array($attendee_q)) {
      while ($attendee_q->next_record()) {
        $msg_attendee .= "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=\"'";
        $msg_attendee .= $attendee_q->f("userobm_lastname")." ".$attendee_q->f("userobm_firstname");
        $msg_attendee .= "'\":MAILTO:";
        $msg_attendee .= $attendee_q->f("userobm_email");
      }
    }
    $msg .= "
BEGIN:VEVENT
UID:$id    
$title
CLASS:$private
ORGANIZER;CN=\"$organizer_cn\":MAILTO:$organizer_email
$msg_attendee
LOCATION:$location
CATEGORIES:$category1
STATUS:CONFIRMED$description
DTSTART:$date_b
DTEND:$date_e
DTSTAMP:$date_ts
END:VEVENT";

  } else {  
    $id = $obm_event->f("calendarevent_id");
    $title = $obm_event->f("calendarevent_title");
    $location = $obm_event->f("calendarevent_location");
    $priority = $obm_event->f("calendarevent_priority"); 
    $category1 = strtoupper($obm_event->f("calendarcategory1_label")); 
    if ($obm_event->f("calendarevent_description") != "") {
      $description = "\nDESCRIPTION:".$obm_event->f("calendarevent_description");
    } else {
      $description = "";
    }
    if (count($resources) > 0) {
      if ($description == "") $description = "DESCRIPTION:";
      $description .= "$l_resource_allocated:";
      $resource_q = run_query_get_resources_info($resources);
      while ($resource_q->next_record()) {
        $description .=  "\\n  ".$resource_q->f("resource_name");
      }
    }
    $date_b = $obm_event->f("calendarevent_date");
    $date_b = date("Ymd",$date_b)."T".date("His",$date_b);
    $start_date = $date_b;
    $date_e = $obm_event->f("calendarevent_date") + $obm_event->f("calendarevent_duration") ;
    $date_e = date("Ymd",$date_e)."T".date("His",$date_e);
    $kind = $obm_event->f("calendarevent_repeatkind");
    $end = $obm_event->f("calendarevent_endrepeat");
    $end = date("Ymd",$end)."T".date("His",$end);
    $repeat_days = $obm_event->f("calendarevent_repeatdays");
    $repeat_frequence = $obm_event->f("calendarevent_repeatfrequence");
    switch($priority) {
      case 1 : $priority = 3;
  	  break;
      case 3 : $priority = 1;
	  break;
    }
    $private = $obm_event->f("calendarevent_privacy");
    switch($private) {
      case 0 : $private = "PUBLIC";
	  break;
      case 1 : $private = "PRIVATE";
	  break;
    }   
    if ($kind == "daily") {
      $repeat = "FREQ=DAILY;UNTIL=$end;INTERVAL=$repeat_frequence";
    } elseif($kind == "weekly") {
      $l_day_repeat = array("SU","MO","TU","WE","TH","FR","SA");
      $start_week_day = strtotime($cagenda_weekstart);
      for ($i=0; $i<7; $i++) {
  	$day_num = date("w", $start_week_day);
      	$day = $l_day_repeat[$day_num];
	if (strcmp(substr($repeat_days,$i,1),"1")==0) {
	  if ($i!=0) {
	    $dis_repeat_days .= ",";
	  }
	  $dis_repeat_days .= "$day";
  	}
      	$start_week_day = strtotime("+1 day", $start_week_day); 
      } 
      $repeat = "FREQ=WEEKLY;UNTIL=$end;INTERVAL=1;BYDAY=$dis_repeat_days";
    } elseif ($kind == "monthlybydate") {
      $day = date("d",$start_date);
      $repeat = "FREQ=MONTHLY;UNTIL=$end;INTERVAL=$repeat_frequence;BYDAY=$day"; 
    } elseif ($kind == "monthlybyday") {
      $start_week_day = date("w",$start_date);
      $daypos = ceil(substr($start_date,6,2)/7);
      $day_num = date("w",strtotime("+ $start_week_day days",$start_date));
      $day = $l_day_repeat[$day_num];
      $repeat = "FREQ=MONTHLY;UNTIL=$end;INTERVAL=$repeat_frequence;BYDAY=$daypos$day";
    } elseif($kind == "yearly") {
      $monthpos = date("m",$start_date);
      $repeat = "FREQ=YEARLY;UNTIL=$end;INTERVAL=$repeat_frequence;BYMONTH=$monthpos";
    }
    $msg .= "
BEGIN:VEVENT
UID:$id    
SUMMARY:$title
CLASS:$private
LOCATION:$location
CATEGORIES:$category1
STATUS:CONFIRMED$description
DTSTART:$date_b
DTEND:$date_e
RRULE:$repeat
END:VEVENT";
  }

  $msg .= "
END:VCALENDAR";


  // we don't forget the last line :
  fputs($fd_dest, $msg);
  fclose($fd_dest);
}


///////////////////////////////////////////////////////////////////////////////
// Check for conflict : Only with the first occurence of each event,
// Parameters:
//   - $agenda       : agenda parameters
//   - sel_entity_id : selected entities id
///////////////////////////////////////////////////////////////////////////////
function check_agenda_conflict($agenda, $sel_entity_id) {

  $ts_begin = isodatetime_to_timestamp($agenda["date_begin"]);
  $ts_end = isodatetime_to_timestamp($agenda["date_end"]);
  $id = $agenda["agenda_id"];
  $obm_q = run_query_agenda_conflict_events($ts_begin, $ts_end, $sel_entity_id, $id);
  if ($obm_q->nf() == 0) {
    return false;
  }
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Check for conflict : Only with the first occurence of each event
///////////////////////////////////////////////////////////////////////////////
function check_agenda_decision_conflict($agenda) {
  
  $user["user"] = $agenda["user_id"];
  $event_id = $agenda["agenda_id"];
  $e_q = run_query_agenda_detail($event_id);
  $ts_begin = $e_q->f("calendarevent_date");
  $ts_end = $e_q->f("calendarevent_date") + $e_q->f("calendarevent_duration");
  $c_q = run_query_agenda_conflict_events($ts_begin,$ts_end,$user,$event_id);
  if ($c_q->nf() == 0) {
    return false;
  }
  return $c_q;
}

///////////////////////////////////////////////////////////////////////////////
// Event Update Execution
// Parameters: 
//   - $agenda      : Agenda params
//   - $sel_entity_id : List entitis id ["user"] ["resource"]
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_event_update($agenda, $sel_entity_id, &$event_id) {
  global $c_use_connectors, $uid, $l_date;
  global $l_update_event_mail_head,$l_update_event_mail_subject,$l_update_event_mail_body,$l_to,$l_from;
  global $cagenda_resource_admin, $l_resource_allocated;

  $datebegin = $agenda["date_begin"];
  $dateend = $agenda["date_end"];
  $event_id = $agenda["agenda_id"];
  $groups = $agenda["group"];
  $mail = $agenda["mail"];
  $mail_title = stripslashes($agenda["title"]);
  $writable_cal = of_right_entity_for_consumer("calendar", "user", $uid, "write");

  if($agenda["task_new_id"]) $task_id = $agenda["task_new_id"]; 
  else $task_id = $agenda["task_id"];

  // Get the final user list
  $users_array = $sel_entity_id["user"];
  if (is_array($groups)) {
    foreach($groups as $group_id) {
      $user_group_array = array_merge($user_group_array, get_all_users_id_from_group($group_id));
    }
    $users_array = array_merge($users_array,$user_group_array);
    $users_array = array_unique($users_array);
  }
  if (! is_array($users_array)) {
    $users_array = array();
  }

  // Get the final resource list
  $resources_array = $sel_entity_id["resource"];
  if ( (is_array($resources_array)) && (count($resources_array) > 0) ) {
    $resources_array = array_unique($resources_array);
    $writable_res = of_right_entity_for_consumer("resource", "user", $uid, "write");
  }    

  $event_data = run_query_agenda_detail($event_id);
  $old_begin = of_isodate_format($event_data->f("calendarevent_date"),"", 1);
  $old_end = of_isodate_format($event_data->f("calendarevent_date") + $event_data->f("calendarevent_duration"),"",1);

  // Handle Connectors data
  if ($c_use_connectors) {
    $obm_q = new DB_OBM;
    $now = date("Y-m-d H:i:s");
    $old_users = get_agenda_event_users_info($event_id);
    if (is_array($old_users)) {
      $removed_users = array_diff($old_users, $users_array);
      $new_users = array_diff($users_array, $old_users);
    } else {
      $removed_users = array();
      $new_users = $users_array;
    }

    // If users have been removed, we note it in DeletedCalendarEvent
    if (count($removed_users) > 0) {
      foreach($removed_users as $u_id) {
	$query = "INSERT INTO DeletedCalendarEvent (
        deletedcalendarevent_event_id,
        deletedcalendarevent_user_id,
        deletedcalendarevent_timestamp)
      VALUES (
        '$event_id',
        '$u_id',
        '$now')";
	display_debug_msg($query, $cdg_sql, "run_query_agenda_event_update(1)");
	$retour = $obm_q->query($query);
      }
    }

    // If users have been inserted they must not appear in DeletedCalendarEvent
    if (count($new_users) > 0) {
      foreach($new_users as $u_id) {
	$query = "DELETE FROM DeletedCalendarEvent
      WHERE deletedcalendarevent_event_id = '$event_id'
        AND deletedcalendarevent_user_id = '$u_id'";
	display_debug_msg($query, $cdg_sql, "run_query_agenda_event_update(2)");
	$retour = $obm_q->query($query);
      }
    }
  }

  // XXXX bizarre les old_end et old_begin semblent etre affectes par l'update
  // ci-dessous sous mysql 4.0.23-10 (portable fourmi)
  run_query_agenda_update_event_data($agenda);  
  run_query_agenda_delete_event_entity($agenda);

  // User insertion
  foreach($users_array as $user_id) {
    if ($user_id == $uid || in_array($user_id, $writable_cal["ids"])) {
      run_query_agenda_insert_entity_event("user", $user_id, $event_id, 'A');	
    } else {
      run_query_agenda_insert_entity_event("user", $user_id, $event_id, 'W');	
    }
  }

  // Resource insertion
  $resources_writable=array(); // Writable Resources
  $resources_address =array(); // Resource Admin notification
  if (is_array($resources_array)) {
    foreach ($resources_array as $res_id) {
      if (in_array($res_id, $writable_res["ids"])) {
	run_query_agenda_insert_entity_event("resource", $res_id, $event_id, 'A');
	array_push($resources_writable, $res_id);
      }
    }
  }
  if ((count($resources_writable) > 0) && ($cagenda_resource_admin != "")) {
    $resources_address = array($cagenda_resource_admin);
    $message_resource = "\r\n\r\n$l_resource_allocated:";
    $resource_q = run_query_get_resources_info($resources_writable);
    while ($resource_q->next_record()) {
      $message_resource .=  "\n  ".$resource_q->f("resource_name");
    }
  }

  if($task_id > 0) {
    run_query_agenda_insert_entity_event("task", $task_id, $event_id, 'A');
  }

  $subject = "$l_update_event_mail_subject" . $mail_title;
  $message = "$l_update_event_mail_head $mail_title
$l_date : $l_from $datebegin $l_to $dateend";
  if (($old_begin != $datebegin || $old_end != $dateend) ) {
    $message .= "
$l_update_event_mail_body $l_from $old_begin $l_to $old_end";
  }
  $message .= $message_resource; 

  $recipient_array = array_diff($users_array, array($uid));
  send_mail($subject, $message, $recipient_array, $resources_address, $mail);
}


///////////////////////////////////////////////////////////////////////////////
// Event Data Update Execution
// Parameters: 
//   - $agenda : Agenda params
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_update_event_data($agenda) {
  global $cdg_sql, $uid;

  $now = date("Y-m-d H:i:s");
  $title = $agenda["title"];
  $category1_id = $agenda["category1"];
  $priority = $agenda["priority"];
  $description = $agenda["description"];
  $location = $agenda["location"];
  $datebegin = $agenda["date_begin"];
  $event_duration = $agenda["event_duration"];
  ($agenda["all_day"] != 1)?$all_day= '0':$all_day = '1';
  $event_id = $agenda["agenda_id"]; 
  if ($agenda["privacy"] != 1) $privacy = 0; else $privacy = 1; 
  $repeat_kind = $agenda["repeat_kind"];
  $repeat_days = $agenda["repeat_days"];
  $repeat_end = $agenda["repeat_end"];
  $frequency = $agenda["repeatfrequency"];  
   
  $obm_q = new DB_OBM;
  $query = "UPDATE CalendarEvent SET
    calendarevent_timeupdate = '$now',
    calendarevent_userupdate = '$uid', 
    calendarevent_title = '$title', 
    calendarevent_date = '$datebegin',     
    calendarevent_description = '$description',
    calendarevent_location = '$location',
    calendarevent_category1_id = '$category1_id',
    calendarevent_priority = '$priority',
    calendarevent_privacy = '$privacy', 
    calendarevent_duration = '$event_duration',
    calendarevent_allday = '$all_day', 
    calendarevent_repeatkind = '$repeat_kind',
    calendarevent_repeatfrequence = '$frequency',
    calendarevent_repeatdays = '$repeat_days',
    calendarevent_endrepeat = '$repeat_end'
  WHERE
    calendarevent_id = '$event_id'";

  display_debug_msg($query, $cdg_sql, "run_query_agenda_update_event_data()");
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Check if the event can be deleted
// Parameters:
//   - $id : event id
// Returns:
//   true if the event can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_agenda_can_delete($id) {
  global $err_msg, $ok_msg;

  $delete_ok = true;

  return $delete_ok;
}


///////////////////////////////////////////////////////////////////////////////
// Delete all events of a evenements, and the event
// Parameters: 
//   - $agenda : agenda infos hash
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_delete($agenda) {
  global $cdg_sql, $c_use_connectors, $l_to, $l_from;
  global $l_delete_event_mail_head,$l_delete_event_mail_subject,$l_delete_event_mail_body;
  global $cagenda_resource_admin, $l_resource_allocated;

  $obm_q = new DB_OBM;
  $event_id = $agenda["agenda_id"];
  $mail = $agenda["mail"]; 
  
  $users_array = get_agenda_event_users_info($event_id);

  $query = "SELECT calendarevent_title
    FROM CalendarEvent
    WHERE calendarevent_id = '$event_id'";
  display_debug_msg($query, $cdg_sql, "run_query_agenda_delete(1)");
  $obm_q->query($query);
  $obm_q->next_record();
  $title = $obm_q->f("calendarevent_title");

  // Resources display
  $query = "SELECT evententity_entity_id FROM EventEntity WHERE evententity_event_id = '$event_id' AND evententity_entity = 'resource'";
  $obm_q->query($query);
  $resources = array();
  while ($obm_q->next_record()) {
    array_push($resources, $obm_q->f("evententity_entity_id"));
  }
  $resources_address = array();
  if (count($resources) > 0 && ($cagenda_resource_admin != "")) {
    $resources_address = array($cagenda_resource_admin);
    $message_resource = "\r\n\r\n$l_resource_allocated:";
    $resource_q = run_query_get_resources_info($resources);
    while ($resource_q->next_record()) {
      $message_resource .=  "\n  ".$resource_q->f("resource_name");
    }
  }

  $query = "DELETE FROM CalendarException WHERE calendarexception_event_id = '$event_id'";
  display_debug_msg($query, $cdg_sql, "run_query_agenda_delete(2)");
  $obm_q->query($query);
  $query = "DELETE FROM EventEntity WHERE evententity_event_id = '$event_id'";
  display_debug_msg($query, $cdg_sql, "run_query_agenda_delete(3)");
  $obm_q->query($query);
  $query = "DELETE FROM CalendarEvent WHERE calendarevent_id = '$event_id'";
  display_debug_msg($query, $cdg_sql, "run_query_agenda_delete(4)");
  $obm_q->query($query);

  // If connectors in use
  if ($c_use_connectors) {
    $now = date("Y-m-d H:i:s");
    
    // We purge the individual user entries
    $query = "DELETE FROM DeletedCalendarEvent
      WHERE deletedcalendarevent_event_id = '$event_id'";
    display_debug_msg($query, $cdg_sql, "run_query_agenda_delete()");
    $retour = $obm_q->query($query);

    // We note the event deletion
    $query = "INSERT INTO DeletedCalendarEvent (
        deletedcalendarevent_event_id,
        deletedcalendarevent_user_id,
        deletedcalendarevent_timestamp)
      VALUES (
        '$event_id',
        null,
        '$now')";
    display_debug_msg($query, $cdg_sql, "run_query_agenda_delete()");
    $retour = $obm_q->query($query);
  }

  $message = $l_delete_event_mail_head.$title.$message_resource;   
  $subject = "$l_delete_event_mail_subject" . $title;
  $recipient_array = array_diff($users_array, array($uid));
  send_mail($subject, $message, $recipient_array, $resources_address, $mail);
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable agenda entities for the current user:
//   - user
//   - users group
//   - resource
//   - resources group
///////////////////////////////////////////////////////////////////////////////
function get_agenda_entity_readable() {

  $ent = array (
    "user" => get_agenda_calendar_readable(),
    "group" => get_agenda_group_readable(),
    "resource" => get_agenda_resource_readable()
  );

  return $ent;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable agenda users for the current user
///////////////////////////////////////////////////////////////////////////////
function get_agenda_calendar_readable() {
  global $cdg_sql, $auth, $agenda, $c_all;

  $uid = $auth->auth["uid"];
  $gusers = "";
  if ( (isset($agenda["entity"]["group_view"]))
       && ($agenda["entity"]["group_view"] != $c_all) ) {
    $gusers = get_all_users_id_from_group($agenda["entity"]["group_view"]);
  }
  $entity = of_right_entity_for_consumer("calendar", "user", $uid, "read", $gusers);

  // Add the user own calendar
  $entity["ids"][] = $uid;
  $obm_q = run_query_agenda_get_user_name(array($uid));
  $obm_q->next_record();
  $id = $obm_q->f("userobm_id");
  $label = $obm_q->f("userobm_lastname") ." ". $obm_q->f("userobm_firstname");
  $entity["entity"][] = array("id" => $id, "label" => $label);

  return $entity;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable calendar group for the current user
///////////////////////////////////////////////////////////////////////////////
function get_agenda_group_readable() {
  global $cdg_sql, $auth, $cagenda_public_groups;

  $uid = $auth->auth["uid"];
  $where = sql_obm_entity_privacy("group");
  if (! $cagenda_public_groups) {
    $where .= " and group_privacy=1";
  }

  $query = "
    SELECT group_id, group_privacy, group_name
    FROM UGroup
    WHERE $where
    ORDER BY group_privacy DESC, group_name";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "get_agenda_group_readable()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable calendar group for the current user
///////////////////////////////////////////////////////////////////////////////
function get_agenda_resource_readable() {
  global $cdg_sql, $auth, $cagenda_public_groups;

  $uid = $auth->auth["uid"];
  $entity = of_right_entity_for_consumer("resource", "user", $uid, "read");

  return $entity;
}


///////////////////////////////////////////////////////////////////////////////
// Get resource infos from the resources id array given
// Parameters:
//   - $resources : array of resources id
// Returns:
// array [ids], [entity] (label=>)
///////////////////////////////////////////////////////////////////////////////
function get_agenda_resource_from_ids($resources) {
  global $cdg_sql;

  if (is_array($resources)) {
    $where = "(0";
    foreach($resources as $resource) {
      $where .= ", $resource";
    }
    $where .= ")";
    $query = "SELECT *
    FROM Resource 
    WHERE resource_id IN $where
    ORDER by resource_name";
    display_debug_msg($query, $cdg_sql, "get_agenda_resource_from_ids()");
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    while ($obm_q->next_record()) {
      $id = $obm_q->f("resource_id");
      $name = $obm_q->f("resource_name");
      $res["ids"][] = $id;
      $res["entity"][$id] = array("label" => $name);
    }
  } else {
    $res = "";
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Get groups infos from the group id array given
// Parameters:
//   - groups : array of groups id
// Returns:
// array [ids], [entity] (label=>)
///////////////////////////////////////////////////////////////////////////////
function get_agenda_group_from_ids($groups) {
  global $cdg_sql;

  if (is_array($groups)) {
    $where = "(0";
    foreach($groups as $group) {
      $where .= ", $group";
    }
    $where .= ")";
    $query = "SELECT *
    FROM UGroup
    WHERE 
      group_id IN $where
    ORDER by group_name";
    display_debug_msg($query, $cdg_sql, "get_agenda_group_from_ids()");
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    while ($obm_q->next_record()) {
      $id = $obm_q->f("group_id");
      $name = $obm_q->f("group_name");
      $res["ids"][] = $id;
      $res["entity"][$id] = array("label" => $name);
    }
  } else {
    $res = "";
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Return tables of hashed entities.
// Parameters :
//  - $entities_array : hashed array of userobm and resource DBO
///////////////////////////////////////////////////////////////////////////////
function store_agenda_entities($entities_q) {
  global $ico_calendar_user0,$ico_calendar_user1,$ico_calendar_user2;
  global $ico_calendar_user3,$ico_calendar_user4,$ico_calendar_user5;
  global $display;

  $i = 0;
  $entity_tab = array();
  $user_q = $entities_q["user"];
  if (is_object($user_q)) {
    while ($user_q->next_record()) {
      $id = $user_q->f("userobm_id");
      $name = $user_q->f("userobm_firstname")." ".$user_q->f("userobm_lastname");
      $entity_tab["user"][$id] = array(
        "name"  => $name,
        "class" => "agendaEventBg$i",
        "image" => ${"ico_calendar_user".$i},
        "type"  => "user",
        "id"    => $user_q->f("userobm_id"));
      $i++;
      $i = $i%6;
    }
  }
  $resource_q = $entities_q["resource"];
  if (is_object($resource_q)) {
    while($resource_q->next_record()) {
      $id = $resource_q->f("resource_id");
      $entity_tab["resource"][$id] = array(
        "name"  => $resource_q->f("resource_name"),
        "class" => "agendaEventBg$i",
        "image" => ${"ico_calendar_user".$i},
        "type"  => "resource",
        "id"    => $id
      );
      $i++;
      $i = $i%6;
    }
  }
  $resourcegroup_q = $entities_q["resourcegroup"];
  if (is_object($resourcegroup_q)) {
    while($resourcegroup_q->next_record()) {
      $id = $resourcegroup_q->f("resource_id");
      $entity_tab["resourcegroup"][$id] = array(
        "name"  => $resourcegroup_q->f("resource_name"),
        "class" => "agendaEventBg$i",
        "image" => ${"ico_calendar_user".$i},
        "type"  => "resourcegroup",
        "id"    => $id,
        "group" => $resourcegroup_q->f("resource_group"),
        "groupLabel" => $resourcegroup_q->f("rgroup_name"),
      );
      $i++;
      $i = $i%6;
    }
  }  

  return $entity_tab;
}


///////////////////////////////////////////////////////////////////////////////
// Return the event infos
// Parameters:
//   - $id : event id
///////////////////////////////////////////////////////////////////////////////
function get_agenda_event_info($id) {
  global $cdg_sql;

  if ($id == "") {
    return false;
  }

  $query = "SELECT * FROM CalendarEvent WHERE calendarevent_id='$id'";

  display_debug_msg($query, $cdg_sql, "get_agenda_event_info()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $e["usercreate"] = $obm_q->f("calendarevent_usercreate");
  $e["owner"] = $obm_q->f("calendarevent_owner");

  return $e;
}


///////////////////////////////////////////////////////////////////////////////
// Return the event users id
// Parameters:
//   - $id : event id
///////////////////////////////////////////////////////////////////////////////
function get_agenda_event_users_info($id) {
  global $cdg_sql;

  $users_array = array();

  if ($id == "") {
    return $users_array;
  }

  $query = "SELECT evententity_entity_id
    FROM EventEntity
    WHERE evententity_event_id = '$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  while ($obm_q->next_record()) {
    $users_array[] = $obm_q->f("evententity_entity_id");
  }

  return $users_array;
}


///////////////////////////////////////////////////////////////////////////////
// Return event entities
// Parameters: 
//   - $agenda_id
///////////////////////////////////////////////////////////////////////////////
function get_agenda_event_entity($agenda_id) {
  global $cdg_sql;

  // XXXX Optimisation possible : 1 seule requete pour toutes les entites

  $query = "SELECT DISTINCT
    userobm_id,
    userobm_lastname,
    userobm_firstname,
    evententity_entity,
    evententity_entity_id,
    evententity_state as state
  FROM EventEntity
    LEFT JOIN UserObm ON evententity_entity_id = userobm_id
  WHERE 
    evententity_event_id = '$agenda_id' AND 
    evententity_entity = 'user'";

  display_debug_msg($query, $cdg_sql, "get_agenda_event_entity(1)");
  $u_q = new DB_OBM;
  $u_q->query($query);
  while ($u_q->next_record()) {
    $entity = $u_q->f("evententity_entity");
    $entity_id = $u_q->f("evententity_entity_id");
    $state = $u_q->f("state");
    $label = $u_q->f("userobm_lastname"). " " .$u_q->f("userobm_firstname");
    $entities[$entity]["ids"][] = $entity_id;
    $entities[$entity]["entity"][$entity_id] = array("label"=> $label,
						     "state" => $state);
  }

  $query = "SELECT DISTINCT
    resource_id,
    resource_name,
    evententity_entity,
    evententity_entity_id,
    evententity_state as state
  FROM EventEntity
    LEFT JOIN Resource ON evententity_entity_id = resource_id
  WHERE 
    evententity_event_id = '$agenda_id'
    AND evententity_entity = 'resource'";

  display_debug_msg($query, $cdg_sql, "get_agenda_event_entity(2)");
  $r_q = new DB_OBM;
  $r_q->query($query);
  while ($r_q->next_record()) {
    $entity = $r_q->f("evententity_entity");
    $entity_id = $r_q->f("evententity_entity_id");
    $state = $r_q->f("state");
    $label = $r_q->f("resource_name");
    $entities[$entity]["ids"][] = $entity_id;
    $entities[$entity]["entity"][$entity_id] = array("label"=> $label,
						     "state" => $state);
  }

  $query = "SELECT DISTINCT
    projecttask_id,
    project_name,
    projecttask_label,
    evententity_entity,
    evententity_entity_id,
    evententity_state as state
  FROM EventEntity
    LEFT JOIN ProjectTask ON evententity_entity_id = projecttask_id
    LEFT JOIN Project ON projecttask_project_id = project_id
  WHERE 
    evententity_event_id = '$agenda_id'
    AND evententity_entity = 'task'";

  display_debug_msg($query, $cdg_sql, "get_agenda_event_entity(2)");
  $r_q = new DB_OBM;
  $r_q->query($query);
  while ($r_q->next_record()) {
    $entity = $r_q->f("evententity_entity");
    $entity_id = $r_q->f("evententity_entity_id");
    $state = $r_q->f("state");
    $label = $r_q->f("project_name")."--".$r_q->f("projecttask_label");
    $entities[$entity]["ids"][] = $entity_id;
    $entities[$entity]["entity"][$entity_id] = array("label"=> $label,
						     "state" => $state);
  }
  return $entities;
}


///////////////////////////////////////////////////////////////////////////////
// Agenda Form Data checking and formatting
// Parameters:
//   - $agenda[] : values checked
//     keys used  : num, name, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_agenda_data_form($agenda) {
  global $err_msg, $php_regexp_isodate, $php_regexp_isodatetime;
  global $l_fill_title, $l_fill_dateend,$l_fill_datebegin,$l_err_weekly_repeat;
  global $l_invalid_date, $l_datebegin, $l_dateend, $l_date_repeatend;
  global $l_err_begin_end, $l_err_end_repeat,$l_err_end_repeat2,$l_err_repeatfrequency;
  global $l_err_end_repeat3, $l_err_days_repeat, $l_err_days_repeat_not_weekly;
  
  $title = $agenda["title"];
  $datebegin = $agenda["date_begin"];
  $dateend = $agenda["date_end"];
  $repeat_end = $agenda["repeat_end"];
  $kind = $agenda["repeat_kind"];
  $repeat_days = $agenda["repeat_days"];
  $repeatfrequency = $agenda["repeatfrequency"];

  if (trim($title) == "") {
    $err_msg = $l_fill_title;
    return false;
  }
  
  // Begin Date check
  if (trim($datebegin) == "") {
    $err_msg = $l_fill_datebegin;
    return false;
  } else if (preg_match($php_regexp_isodatetime, $datebegin) == 0) {
    $err_msg = "$l_datebegin : $l_invalid_date"; 
    return false;
  }
  
  // End Date check
  if (trim($dateend) == "") {
    $err_msg = $l_fill_dateend;
    return false;
  } else if (preg_match($php_regexp_isodatetime, $dateend) == 0) {
    $err_msg = "$l_dateend : $l_invalid_date"; 
    return false;
  }
  
  // Repeatition check
  if ($kind != "none") {
    // End repeat Date check
    if (($repeat_end != "") && (preg_match($php_regexp_isodate, $repeat_end) == 0)) {
      $err_msg = "$l_date_repeatend : $l_invalid_date"; 
      return false;
    }
  }
  
  if(!is_numeric($repeatfrequency) ||  $repeatfrequency < 1) {
    $err_msg = $l_err_repeatfrequency;
    return false;
  }

  if ($dateend<$datebegin) {
    $err_msg = $l_err_begin_end;
    return false;
  } 
  
  if (trim($repeat_end) != "" && $dateend>$repeat_end && $kind != "none") {
    $err_msg =  $l_err_end_repeat;
    return false;
  }
  
  // If repeat kind is weekly, repeat days must be set
  if ($kind == "weekly" && $repeat_days == "0000000") {
    $err_msg = $l_err_days_repeat;
    return false;
  }

  // If repeat days are set, repeat kind must be weekly
  if ($kind != "weekly" && $repeat_days != "0000000") {
    $err_msg = $l_err_days_repeat_not_weekly;
    return false;
  }

  if ($kind == "weekly" && strtotime("+ 1 week",strtotime($dateend)) > strtotime($repeat_end)) {
    $err_msg = $l_err_weekly_repeat;
    return false;
  }

  return true; 
}


///////////////////////////////////////////////////////////////////////////////
// localizeDate() - similar to strftime but uses a preset arrays of localized
// months and week days and only supports %A, %a, %B, %b, %e, and %Y
// more can be added as needed but trying to keep it small while we can
// Parameters:
//   - $format : format of the wished result
//   - $timestamp : time to format
///////////////////////////////////////////////////////////////////////////////
function agenda_localizeDate($format, $timestamp) {
 global $l_daysofweek, $l_daysofweekshort;
 global $l_monthsofyear, $l_monthsofyearshort;

 $day = '%A %e %B';
 $week = '%e %B';
 $week_list = '%a %e';
 $week_jump = '%e %b';
 $month = '%B %Y';
 $month_list = '%A %e %B';

 $year = date("Y", $timestamp);
 $months = date("n", $timestamp)-1;
 $days = date("j", $timestamp);
 $dayofweek = date("w", $timestamp);
	
 $date = str_replace('%Y', $year, ${$format});
 $date = str_replace('%e', $days, $date);
 $date = str_replace('%B', $l_monthsofyear[$months], $date);
 $date = str_replace('%b', $l_monthsofyearshort[$months], $date);
 $date = str_replace('%A', $l_daysofweek[$dayofweek], $date);
 $date = str_replace('%a', $l_daysofweekshort[$dayofweek], $date);
	
 return $date;	
}


///////////////////////////////////////////////////////////////////////////////
// Get the date of a day, from its day of week and a week date
// Parameters:
//   - $timestamp : timestamp date for the week 
//   - $day       : day of week ("sun", "mon",...) at least 3 letters
// Returns:
//   - $ts : timestamp of the day
///////////////////////////////////////////////////////////////////////////////
function get_agenda_date_day_of_week($timestamp, $day) {
  global $cagenda_weekstart;

  if (!isset($cagenda_weekstart)) $cagenda_weekstart = 'Sunday';
  $num = date('w', strtotime($cagenda_weekstart));
  $start_day_time = strtotime((date('w',$timestamp)==$num ? "$cagenda_weekstart" : "last $cagenda_weekstart"), $timestamp);
  $ret_ts = strtotime($day,$start_day_time);
  // we correct the value to eliminate DST pbs
  $ret_ts = strtotime('+12 hours', $ret_ts);
  $ret_ts = strtotime(of_isodate_format($ret_ts));

  return $ret_ts;
}


///////////////////////////////////////////////////////////////////////////////
// Slice the entities to display. Limit the entities selected to the entities
// really readable by the user, and limit to a max (=6) for display
// Parameters:
//   - $sel_entity_id   : ["user"] ["resource"] ids
//   - $entity_readable : if given, entities eligibles
///////////////////////////////////////////////////////////////////////////////
function slice_agenda_entities($sel_entity_id, $entity_readable="") {
  global $auth, $max_display;

  $sel_user_id = $sel_entity_id["user"];
  $sel_group_id = $sel_entity_id["group"];
  $sel_resource_id = $sel_entity_id["resource"];

  // users : slice only if users are selected 
  if (is_array($sel_user_id)) {
    $res_user_id = array();
    if ($action != "perform_meeting") {
      foreach ($sel_user_id as $u_id) {
	if (is_array($entity_readable)) {
	    if (in_array($u_id, $entity_readable["user"]["ids"])) {
	      $res_user_id[] = $u_id;
	    }
	} else {
	  $res_user_id[] = $u_id;
	}
      }
    }
  }
  if (count($res_user_id) > $max_display) {
    $res_user_id = array_slice ($res_user_id, 0, $max_display);
  }
  $nb_user = count($res_user_id);

  // resources : slice only if resources are selected 
  if (is_array($sel_resource_id)) {
    $res_resource_id = array();
    if ($action != "perform_meeting") {
      foreach ($sel_resource_id as $r_id) {
	if (is_array($entity_readable)) {
	    if (in_array($r_id, $entity_readable["resource"]["ids"])) {
	      $res_resource_id[] = $r_id;
	    }
	} else {
	  $res_resource_id[] = $r_id;
	}
      }
    }
  }
  $max_res = $max_display - $nb_user;
  $nb_res = count($res_resource_id);
  if ($nb_res > $max_res) {
    $res_resource_id = array_slice ($res_resource_id, 0, $max_res);
  }
  $nb_res = count($res_resource_id);
  if (($nb_user == 0) && ($nb_res == 0)) {
    $res_user_id = array($auth->auth["uid"]);
  }

  return array(
    "user" => $res_user_id,
    "group" => $sel_group_id,
    "resource" => $res_resource_id
  );
}


///////////////////////////////////////////////////////////////////////////////
// return the number of the week of the argument.
//------------------------------------------------------------------------
// Argument:
// ---------
//     - $date
///////////////////////////////////////////////////////////////////////////////
function get_agenda_week_num($date) {
  global $cagenda_weekstart;

  $num = date('w', strtotime($cagenda_weekstart));
  $delta_thursday = date("w",strtotime("-$num days",strtotime("thursday")))-date("w",strtotime("-$num days",$data));
  $week_num = date("W",strtotime("$delta_thursday days",$date));
  return $week_num;
}


///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a week of users or/and groups
// Parameters:
//   - $start        : event start datetime (timestamp)
//   - $end          : event end datetime (timestamp)
//   - $sel_entity_id : selected entities id
//   - $agenda       : agenda params
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_conflict_events($start,$end,$sel_entity_id,$event_id) {
  global $cdg_sql, $uid;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date");
  $calendarevent_date_l = sql_date_format($db_type,"calendarevent_date","calendarevent_date");

  // get all users (from groups too)
  $users = run_query_agenda_get_allusers($sel_entity_id["user"], $sel_entity_id["group"]);

  if ((is_array($users)) && (count($users) > 0)) {
    $has_user = true;
  }
  if (is_array($sel_entity_id["resource"])
      && (count($sel_entity_id["resource"]) > 0) ) {
    $has_resource = true;
  }

  if ($has_user) {
    $where_user .= "( evententity_entity_id IN (";
    foreach($users as $id) {
      $where_user .= "$coma'$id'";
      $coma = ",";
    }
    $where_user.= ")";
    $where_user .= " AND evententity_entity = 'user' )"; 
    $select_user = "userobm_lastname,
      userobm_firstname,";
    $join_user = "LEFT JOIN UserObm ON evententity_entity_id = userobm_id";
  }   	     	    

  if ($has_resource) {
    $coma = "";
    $where_res .= "( evententity_entity_id IN (";
    foreach($sel_entity_id["resource"] as $id) {
      $where_res .= "$coma'$id'";
      $coma = ",";
    }
    $where_res.= ")";
    $where_res .= " AND evententity_entity = 'resource' )"; 
    $select_resource = "resource_name,";
    $join_resource = "LEFT JOIN Resource ON evententity_entity_id = resource_id";
  }   	     	    

  if ($has_user && $has_resource) {
    $where_ent = "AND ( $where_user OR $where_res)";
  } elseif ($has_user || $has_resource) {
    $where_ent = "AND $where_user $where_res";
  }

  if ($event_id != "") {
    $where_event .= " AND calendarevent_id != '$event_id' ";
  }

  $query = "SELECT
      calendarevent_id,
      calendarevent_title,
      calendarevent_privacy,
      calendarevent_description, 
      calendarevent_location,
     calendarevent_repeatfrequence, 
      evententity_entity,
      evententity_entity_id,
      $select_user
      $select_resource
      calendarcategory1_label,
      $calendarevent_date_l,
      calendarevent_duration,
      calendarevent_allday
    FROM CalendarEvent
      LEFT JOIN CalendarCategory1 ON calendarcategory1_id = calendarevent_category1_id
      LEFT JOIN EventEntity ON calendarevent_id = evententity_event_id
      $join_user
      $join_resource
    WHERE evententity_state = 'A'
      AND $calendarevent_date > $start - calendarevent_duration
      AND $calendarevent_date < $end 
      $where_ent
      $where_event
    ORDER BY calendarevent_date";

  display_debug_msg($query, $cdg_sql, "run_query_agenda_conflict_events(()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a week of users or/and groups
// Parameters:
//   - $start           : timestamp of start date
//   - $end             : timestamp of end date
//   - $calendar_entity : array of entities elements to display
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_no_repeat_events($start,$end, $calendar_entity) {
  global $cdg_sql, $uid;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date");
  $calendarevent_date_l = sql_date_format($db_type,"calendarevent_date","calendarevent_date");

  if (is_array($calendar_entity["user"])
      && (count($calendar_entity["user"]) > 0) ) {
    $has_user = true;
  }
  if (is_array($calendar_entity["resource"])
      && (count($calendar_entity["resource"]) > 0) ) {
    $has_resource = true;
  }
  if (is_array($calendar_entity["resourcegroup"])
      && (count($calendar_entity["resourcegroup"]) > 0) ) {
    $has_resourcegroup = true;
  }
  
  if ($has_user) {
    $where_user .= "( evententity_entity_id IN (";
    foreach($calendar_entity["user"] as $id => $prop) {
      $where_user .= "$coma'$id'";
      $coma = ",";
    }
    $where_user.= ")";
    $where_user .= " AND evententity_entity = 'user' )"; 
  }   	     	    

  if ($has_resource) {
    $coma = "";
    $where_res .= "( evententity_entity_id IN (";
    foreach($calendar_entity["resource"] as $id => $prop) {
      $where_res .= "$coma'$id'";
      $coma = ",";
    }
    $where_res.= ")";
    $where_res .= " AND evententity_entity = 'resource' )"; 
  }   	     	    

  if ($has_user && $has_resource) {
    $where_ent = "AND ( $where_user OR $where_res)";
  } elseif ($has_user || $has_resource) {
    $where_ent = "AND $where_user $where_res";
  }
  
  if ($has_resourcegroup) {
    $coma = "";
    $where_grp .= "( evententity_entity_id IN (";
    foreach($calendar_entity["resourcegroup"] as $id => $prop) {
      $where_grp .= "$coma'$id'";
      $coma = ",";
    }
    $where_grp.= ")";
    $where_grp .= " AND evententity_entity = 'resource' )"; 
    
    $union = "
      UNION
    SELECT
      calendarevent_id,
      calendarevent_title,
      calendarevent_privacy,
      calendarevent_description,
      calendarevent_location,
      calendarevent_repeatfrequence,
      evententity_entity_id,
      'resourcegroup' as evententity_entity,
      calendarcategory1_label,
      $calendarevent_date_l,
      calendarevent_duration,
      calendarevent_allday
    FROM CalendarEvent
      LEFT JOIN CalendarCategory1
        ON calendarevent_category1_id = calendarcategory1_id
      LEFT JOIN EventEntity ON calendarevent_id = evententity_event_id
    WHERE evententity_state = 'A'
      AND calendarevent_repeatkind = 'none'
      AND $calendarevent_date >= $start - calendarevent_duration
      AND $calendarevent_date <= $end
      AND $where_grp
    ";
  }
  
  $query = "SELECT
      calendarevent_id,
      calendarevent_title,
      calendarevent_privacy,
      calendarevent_description,
      calendarevent_repeatfrequence,
      calendarevent_location,
      evententity_entity_id,
      evententity_entity,
      calendarcategory1_label,
      $calendarevent_date_l,
      calendarevent_duration,
      calendarevent_allday
    FROM CalendarEvent
      LEFT JOIN CalendarCategory1
        ON calendarevent_category1_id = calendarcategory1_id
      LEFT JOIN EventEntity ON calendarevent_id = evententity_event_id
    WHERE evententity_state = 'A'
      AND calendarevent_repeatkind = 'none'
      AND $calendarevent_date >= $start - calendarevent_duration
      AND $calendarevent_date <= $end
      $where_ent
      $union
      ORDER BY calendarevent_date
      "; 

  display_debug_msg($query, $cdg_sql, "run_query_agenda_no_repeat_events()");
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a week of users or/and groups
// Parameters:
//   - $agenda          : agenda params
//   - $calendar_entity : array of entities elements to display
///////////////////////////////////////////////////////////////////////////////
function run_query_agenda_repeat_events($start, $end, $calendar_entity) {
  global $cdg_sql, $uid;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $calendarevent_endrepeat = sql_date_format($db_type,"calendarevent_endrepeat");
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date");
  $calendarevent_date_l = sql_date_format($db_type,"calendarevent_date","calendarevent_date");
  $calendarevent_endrepeat_l = sql_date_format($db_type,"calendarevent_endrepeat","calendarevent_endrepeat");

  if (is_array($calendar_entity["user"])
      && (count($calendar_entity["user"]) > 0) ) {
    $has_user = true;
  }
  if (is_array($calendar_entity["resource"])
      && (count($calendar_entity["resource"]) > 0) ) {
    $has_resource = true;
  }

  if ($has_user) {
    $where_user .= "( evententity_entity_id IN (";
    foreach($calendar_entity["user"] as $id => $prop) {
      $where_user .= "$coma'$id'";
      $coma = ",";
    }
    $where_user.= ")";
    $where_user .= " AND evententity_entity = 'user' )"; 
  }   	     	    

  if ($has_resource) {
    $coma = "";
    $where_res .= "( evententity_entity_id IN (";
    foreach($calendar_entity["resource"] as $id => $prop) {
      $where_res .= "$coma'$id'";
      $coma = ",";
    }
    $where_res.= ")";
    $where_res .= " AND evententity_entity = 'resource' )"; 
  }   

  if (is_array($calendar_entity["resourcegroup"])
      && (count($calendar_entity["resourcegroup"]) > 0) ) {
    $has_resourcegroup = true;
  }

  if ($has_user && $has_resource) {
    $where_ent = "AND ( $where_user OR $where_res)";
  } elseif ($has_user || $has_resource) {
    $where_ent = "AND $where_user $where_res";
  }
  
  if ($has_resourcegroup) {
    $coma = "";
    $where_grp .= "( evententity_entity_id IN (";
    foreach($calendar_entity["resourcegroup"] as $id => $prop) {
      $where_grp .= "$coma'$id'";
      $coma = ",";
    }
    $where_grp.= ")";
    $where_grp .= " AND evententity_entity = 'resource' )"; 
    
    $union = "
      UNION
      SELECT
      calendarevent_id,
      calendarevent_title,
      calendarevent_privacy,
      calendarevent_description, 
      calendarevent_location, 
      calendarcategory1_label,
      $calendarevent_date_l,
      calendarevent_duration,
      calendarevent_repeatkind,
      $calendarevent_endrepeat_l,
      calendarevent_repeatfrequence,
      'resourcegroup' as evententity_entity,
      evententity_entity_id,
      calendarevent_repeatdays,
      calendarevent_allday
    FROM CalendarEvent
      LEFT JOIN CalendarCategory1
        ON calendarevent_category1_id = calendarcategory1_id
      LEFT JOIN EventEntity ON calendarevent_id = evententity_event_id
   WHERE calendarevent_repeatkind != 'none'
      AND $calendarevent_date <= $end 
      AND ($calendarevent_endrepeat >= $start - calendarevent_duration
      OR $calendarevent_endrepeat = '0')
      AND $where_grp
    ";
  }

  $query = "SELECT
      calendarevent_id,
      calendarevent_title,
      calendarevent_privacy,
      calendarevent_description, 
      calendarevent_location, 
      calendarcategory1_label,
      $calendarevent_date_l,
      calendarevent_duration,
      calendarevent_repeatkind,
      $calendarevent_endrepeat_l,
      calendarevent_repeatfrequence,
      evententity_entity,
      evententity_entity_id,
      calendarevent_repeatdays,
      calendarevent_allday
    FROM CalendarEvent
      LEFT JOIN CalendarCategory1
        ON calendarevent_category1_id = calendarcategory1_id
      LEFT JOIN EventEntity ON calendarevent_id = evententity_event_id
                               AND evententity_state = 'A'
    WHERE calendarevent_repeatkind != 'none'
      AND $calendarevent_date <= $end 
      AND ($calendarevent_endrepeat >= $start - calendarevent_duration
      OR $calendarevent_endrepeat = '0')
      $where_ent
      $union

    ORDER BY calendarevent_date"; 

  display_debug_msg($query, $cdg_sql, "run_query_agenda_repeat_events()");
  $obm_q->query($query);
  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Return 
// Parameters: 
//   - $events : ids of the
///////////////////////////////////////////////////////////////////////////////
function run_query_get_events_attendee($events) {
  global $cdg_sql, $uid;
  $event_list = implode(",",$events) ;
  $obm_q = new DB_OBM;

  $query = "SELECT
   calendarevent_id,
   evententity_entity_id,
   evententity_entity,
   userobm_firstname,
   userobm_lastname,
   resource_name,
   projecttask_label,
   project_name
   FROM CalendarEvent
   JOIN EventEntity ON evententity_event_id = calendarevent_id
   LEFT JOIN UserObm ON evententity_entity = 'user' AND evententity_entity_id = userobm_id
   LEFT JOIN Resource ON evententity_entity = 'resource' AND evententity_entity_id = resource_id
   LEFT JOIN ProjectTask ON evententity_entity = 'task' AND  evententity_entity_id = projecttask_id
   LEFT JOIN Project ON project_id = projecttask_project_id
   WHERE calendarevent_id IN ($event_list)";

  display_debug_msg($query, $cdg_sql, "run_query_get_events_attendee()");
  $obm_q->query($query);
  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Return a table of Events between two dates
// Parameters: 
//   - $start_time      : timestamp of start date
//   - $end_time        : timestamp of end date
//   - $calendar_entity :
///////////////////////////////////////////////////////////////////////////////
function agenda_events_model($start_time, $end_time, $calendar_entity) {
  global $day_duration;
  $events_array = array();
  $day_duration = 86400;
  $nr_q = run_query_agenda_no_repeat_events($start_time,$end_time,$calendar_entity);
  while ($nr_q->next_record()) {
    $id = $nr_q->f("calendarevent_id");
    $title = $nr_q->f("calendarevent_title");
    $privacy = $nr_q->f("calendarevent_privacy");
    $description = $nr_q->f("calendarevent_description"); 
    $location = $nr_q->f("calendarevent_location"); 
    $category1 = $nr_q->f("calendarcategory1_label");
    $date = $nr_q->f("calendarevent_date");
    $duration = $nr_q->f("calendarevent_duration");
    $user_id = $nr_q->f("evententity_entity_id");
    $all_day = $nr_q->f("calendarevent_allday");
    $entity = $nr_q->f("evententity_entity");
    if(isset($events_array[$id])) {
      $event = &$events_array[$id];
    } else {
      $event = &new Event($id,$duration,$title,$location,$category1,$privacy,$description);
      $events_array[$id] = &$event;
    }  
    store_agenda_event($date, $event, $day_events, $user_id, $all_day, $end_time, $entity);
  }
  
  $r_q = run_query_agenda_repeat_events($start_time,$end_time,$calendar_entity);
  while ($r_q->next_record()) {
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
    $entity = $r_q->f("evententity_entity");
    $all_day = $r_q->f("calendarevent_allday");     
    $repeatfrequence = $r_q->f("calendarevent_repeatfrequence");
    $repeatdays = $r_q->f("calendarevent_repeatdays");
    $user_id = $r_q->f("evententity_entity_id");    
    $all_day = $r_q->f("calendarevent_allday");       
    if (!$endrepeat) {
      $endrepeat = $end_time;
    }
    if(isset($events_array[$id])) {
      $event = &$events_array[$id];
    } else {
      $event = &new Event($id,$duration,$title,$location,$category1,$privacy,$description);
    }
    $event_start =  $start_time ;
    $delta = date("H",$date) * 3600 + date("i",$date) * 60 + date("s",$date) + $duration;
    $delta = floor($delta/$day_duration);
    $event_start -= $delta * $day_duration;
    $end_date = ($endrepeat < $end_time) ? $endrepeat : $end_time;
    $end_date += $day_duration;
    switch ($repeatkind) {
      case "daily" :
        $stored = agenda_daily_repeatition($date,$event_start,$end_date,$end_time,$repeatfrequence,&$day_events,&$event,$user_id,$all_day,$entity);	
        break; 
      case "weekly" :
	$stored = agenda_weekly_repeatition($date,$event_start,$end_date,$end_time,$repeatdays,$repeatfrequence,&$day_events,&$event,$user_id,$all_day,$entity); 
	break;
      case "monthlybyday" :
	$stored = agenda_agenda_monthlybyday_repeatition($date,$event_start,$end_date,$end_time,$repeatfrequence,&$day_events,&$event,$user_id,$all_day,$entity); 
        break;
      case "monthlybydate" :
	$stored = agenda_monthlybydate_repeatition($date,$event_start,$end_date,$end_time,$repeatfrequence,&$day_events,&$event,$user_id,$all_day,$entity);
	break;
      case "yearly" :
	$stored = agenda_yearly_repeatition($date,$event_start,$end_date,$end_time,$repeatfrequence,&$day_events,&$event,$user_id,$all_day,$entity);
	break;	
    }
    if($stored && !isset($events_array[$id])) {
      $events_array[$id] = &$event;
      
    }
  }
  if(count($events_array) > 0) {
    $attendee_q = run_query_get_events_attendee(array_keys($events_array));
    while($attendee_q->next_record()) {
      $id = $attendee_q->f("calendarevent_id");
      $entity = $attendee_q->f("evententity_entity");
      $entity_id = $attendee_q->f("evententity_entity_id");
      switch($entity) {
        case "user":
          $entity_label = $attendee_q->f("userobm_lastname")." ".$attendee_q->f("userobm_firstname");
          break;
        case "resource":
          $entity_label = $attendee_q->f("resource_name");
          break;
        case "task":
          $entity_label = $attendee_q->f("project_name")."-".$attendee_q->f("projecttask_label");
          break;
      }
      $event = &$events_array[$id];
      $event->add_attendee($entity,$entity_id,$entity_label);
    }
  }
  return $day_events; 
}


///////////////////////////////////////////////////////////////////////////////
// Perform the daily repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function store_agenda_event($date, &$event, &$day_events,$user_id,$all_day,$end_time,$entity) {
  $temp_date = $date;
  $event_end = $date + $event->duration;
  do {
    $iso_date = of_isodate_format($temp_date);
    $day = &$day_events[$iso_date];
    if (!is_object($day) || !$day->is_same_day($temp_date)) {
      $day = &new Day($temp_date);
      $day_events[$iso_date] = &$day;
    }
    $day->store_agenda_event(&$event,$date,$user_id,$all_day,$entity);
    $temp_date = strtotime("+1 day",$temp_date);
  } while ($event_end > $temp_date && $temp_date < $end_time);
}


///////////////////////////////////////////////////////////////////////////////
// Perform the daily repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function agenda_daily_repeatition($date,$start_time,$end_date,$end_time,$repeatfrequence,&$day_events,&$event,$user_id,$all_day,$entity) {
  global $day_duration;

  $return = false;
  $temp_date = $date;
  if ($temp_date < $start_time) {
    $temp_date = ceil(($start_time - $date) / $day_duration);
    $hour_diff = of_date_get_hour($date) - of_date_get_hour($start_time);
    $min_diff = of_date_get_min($date) - of_date_get_min($start_time);
    if ($temp_date > 0) {
      $temp_date = ($temp_date % $repeatfrequence);
    }	    
    $temp_date = strtotime("+$temp_date days +$hour_diff hours +$min_diff minutes", $start_time);
  }
  while ($temp_date <= $end_date ) {
    $iso_date = of_isodate_format($temp_date);
    $day = &$day_events[$iso_date];
    if(!is_object($day)) {
      $day = &new Day($temp_date);
      $day_events[$iso_date] = &$day;
    }
    $return = true;
    store_agenda_event($temp_date, $event, $day_events, $user_id, $all_day,$end_time,$entity);
    $temp_date = strtotime("+$repeatfrequence days",$temp_date);
  }
  return $return;
}


///////////////////////////////////////////////////////////////////////////////
// Perform the weekly repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function agenda_weekly_repeatition($date,$start_time,$end_date,$end_time,$repeatdays,$repeatfrequence,&$day_events,&$event,$user_id,$all_day,$entity) {
  global $day_duration,$cagenda_weekstart;

  $return = false;
  $temp_date = get_agenda_date_day_of_week($date, $cagenda_weekstart);
  $temp_date = strtotime("+".of_date_get_hour($date)." hours +".date("i",$date)." minutes",$temp_date);
  if ($temp_date < $start_time) {
    $temp_date = ceil(($start_time - $temp_date) / $day_duration);
    $hour_diff = of_date_get_hour($date) - of_date_get_hour($start_time);
    $min_diff = date("i",$date) - date("i",$start_time);
    $temp_date = ($temp_date % ($repeatfrequence * 7));
    if ($temp_date == "") $temp_date = 0;
    $temp_date = strtotime("-$temp_date days +$hour_diff hours +$min_diff minutes", $start_time);
  }
  $num = (date('w', $temp_date) - date('w', strtotime($cagenda_weekstart)) +7)%7;
  for ($i=0; $i<strlen($repeatdays);$i++) {;
    $k = ($i+$num)%7;
    $repeat_days[$i]=substr($repeatdays, $k, 1);
  }
  while ($temp_date <= $end_date) {
    foreach ($repeat_days as $delta_day => $repeatition) {	  
      if ($repeatition == 1) {
	$current_date = strtotime("+$delta_day days",$temp_date);
        $iso_date = of_isodate_format($current_date);
	if ($current_date >= $start_time && $current_date <= $end_date) {
	  $day = &$day_events[$iso_date];
	  if (!is_object($day)) {
	    $day = &new Day($current_date);
	    $day_events[$iso_date] = &$day;
          }
          store_agenda_event($current_date, $event, $day_events,$user_id,$all_day,$end_time,$entity);
          $return = true;
	}
      }
    } 
    $temp_date = strtotime("+$repeatfrequence weeks",$temp_date);
  }	
  return $return;
}


///////////////////////////////////////////////////////////////////////////////
// Perform the monthly by day repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function agenda_agenda_monthlybyday_repeatition($date,$start_time,$end_date,$end_time,$repeatfrequence,&$day_events,&$event,$user_id,$all_day,$entity) {
  global $day_duration,$cagenda_weekstart;

  $return = false;
  $temp_date = $date;
  $start_week = date('w', strtotime($cagenda_weekstart));
  $num = (date('w', $date) - $start_week +7)%7;
  $monthlybyday = ceil(date('d',$date) / 7);
  $event_end = $date + $event->duration;
  if ($event_end < $start_time) {
    $month_diff = (of_date_get_year($start_time) - of_date_get_year($temp_date)) * 12;
    $month_diff += of_date_get_month($start_time) - of_date_get_month($temp_date);
    $hour_diff = of_date_get_hour($date) - of_date_get_hour($start_time);
    $min_diff = of_date_get_min($date) - of_date_get_min($start_time);
    $month_diff = $month_diff % ($repeatfrequence);
    if ($month_diff > 0) {
      $month_diff = $repeatfrequence - $month_diff;
    }
    $temp_date = strtotime("+$month_diff months +$hour_diff hours +$min_diff minutes", $start_time);
  }
  $hour_diff = of_date_get_hour($date);
  $min_diff = of_date_get_min($date);
  $ts_cur = strtotime(date("Y-m-01",$temp_date));
  while ($ts_cur <= $end_date) {
    $start_num = (date('w', $ts_cur) - $start_week +7)%7;
    $day_diff = ($num - $start_num +7)%7;
    $day_diff += 7*($monthlybyday-1);
    $current_date = strtotime("+ $day_diff days +$hour_diff hours +$min_diff minutes",$ts_cur);
    $event_end = $current_date + $event->duration;
    if (of_date_get_month($ts_cur) == of_date_get_month($current_date)
       && $event_end >= $start_time
       && $current_date <= $end_date) {
      $iso_date = of_isodate_format($current_date);
      $day = &$day_events[$iso_date];
      if (!is_object($day)) {
	$day = &new Day($current_date);
	$day_events[$iso_date] = &$day;
      }
      $return = true;
      store_agenda_event($current_date, $event, $day_events,$user_id,$all_day,$end_time,$entity);
    }
    $ts_cur = strtotime("+$repeatfrequence months", $ts_cur);
  }	
  return $return;
}


///////////////////////////////////////////////////////////////////////////////
// Perform the monthly by date repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function agenda_monthlybydate_repeatition($date,$start_time,$end_date,$end_time,$repeatfrequence,&$day_events,&$event,$user_id,$all_day,$entity) {  
  global $day_duration;

  $return = false;
  $temp_date = $date;
  if ($temp_date < $start_time) {
    $temp_date = strtotime(date("Y-m-01",$start_time));
    $month_diff = (of_date_get_year($start_time) - of_date_get_year($date)) * 12;
    $month_diff += of_date_get_month($start_time) - of_date_get_month($date);
    $hour_diff = of_date_get_hour($date) - of_date_get_hour($start_time);
    $min_diff = of_date_get_min($date) - of_date_get_min($start_time);
    $month_diff = $month_diff % ($repeatfrequence);
    if ($month_diff > 0) {
      $month_diff = $repeatfrequence - $month_diff;
    }

    $temp_date = strtotime("+$month_diff months +$hour_diff hours +$min_diff minutes", $temp_date);
  }	
  $hour_diff = of_date_get_hour($date);
  $min_diff = of_date_get_min($date);
  $ts_date = strtotime(date("Y-m-".date("d",$date),$temp_date));
  $ts_date = strtotime("+$hour_diff hours +$min_diff minutes",$ts_date);

  while ($ts_date <= $end_date) {
    $iso_date = of_isodate_format($ts_date);
    $day = &$day_events[$iso_date];
    if (!is_object($day)) {
      $day = &new Day($ts_date);
      $day_events[$iso_date] = &$day;
    }
    $return = true;
    store_agenda_event($ts_date, $event, $day_events,$user_id,$all_day,$end_time,$entity);
    $ts_date = strtotime("+$repeatfrequence months",$ts_date);
  }  
  return $return;
}


///////////////////////////////////////////////////////////////////////////////
// Perform the yearly repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function agenda_yearly_repeatition($date,$start_time,$end_date,$end_time,$repeatfrequence,&$day_events,&$event,$user_id,$all_day,$entity) {  
  global $day_duration;

  $return = false;
  $temp_date = $date;    
  if ($temp_date < $start_time) {
    $temp_date = strtotime(date("Y-m-01",$start_time));
    $month_diff = (of_date_get_year($start_time) - of_date_get_year($date)) * 12;
    $month_diff += of_date_get_month($start_time) - of_date_get_month($date);
    $hour_diff = of_date_get_hour($date) - of_date_get_hour($start_time);
    $min_diff = of_date_get_min($date) - of_date_get_min($start_time);
    $month_diff = $month_diff % ($repeatfrequence*12);
    if ($month_diff > 0) {
      $month_diff = $repeatfrequence*12 - $month_diff;
    }
    $temp_date = strtotime("+$month_diff months +$hour_diff hours +$min_diff minutes", $temp_date);
  }
  $hour_diff = of_date_get_hour($date);
  $min_diff = of_date_get_min($date);
  $ts_date = strtotime(date("Y-m-".date("d",$date),$temp_date));
  $ts_date = strtotime("+$hour_diff hours +$min_diff minutes",$ts_date);
  while ($ts_date <= $end_date) {
    $iso_date = of_isodate_format($ts_date);
    $day = &$day_events[$iso_date];
    if (!is_object($day)) {
      $day = &new Day($ts_date);
      $day_events[$iso_date] = &$day;
    }
    $return = true;
    store_agenda_event($ts_date, $event, $day_events,$user_id,$all_day,$end_time,$entity);
    $ts_date = strtotime("+$repeatfrequence years",$ts_date);
  }
  return $return;
}


/////////////////////////////////////////////////////////////////////////////
// Class Event :
// Describe an event.
//////////////////////////////////////////////////////////////////////////////
class Event {
  
  var $id;
  var $duration;
  var $title;
  var $location;
  var $category1;
  var $privacy;
  var $description;
  var $attendee;

  function Event($id,$duration,$title,$location,$category1,$privacy,$description) {
    $this->id = $id;
    $this->duration = $duration;
    $this->title = $title;
    $this->location = $location;
    $this->category1 = $category1;
    $this->privacy = $privacy;
    $this->description = $description;
    $this->attendee = array();
  }
  
  function add_attendee($entity,$entity_id,$entity_label) {
    $this->attendee[$entity][$entity_id] = $entity_label;
  }
  /**
   * match_period 
   *  
   * Check if an event is in a given lapse of time.
   * return :
   * -2 : if the event is before the period
   * -1 : if the event is after the period
   * 0 : in all unchecked case (might not happen)
   * 1 : if the event begin after the beginning of the period but before the end
   * 2 : if the event begin before the period and end after the beginning of the period
   * 
   * @param timestamp $date The timestamp of the event beginning
   * @param timestamp $start The beginning of the period
   * @param timestamp $end The end of the period
   * @access public
   * @return int
   */
  function match_period($date,$start,$end) {
    
    $ev_start = $date;
    $ev_end = $this->duration + $ev_start;
    
    if ($ev_end <= $start) {
      return -2;
    }

    if ($ev_start >= $end) {
      return -1;
    }

    if ($ev_end > $start && $ev_start < $start) {
      return 2;
    }
    
    if ($ev_start < $end && $ev_end > $start) {
      return 1;
    }

    return 0;
  }

}
  
///////////////////////////////////////////////////////////////////////////////
// Class Day :
// Manage a list of event in a day
///////////////////////////////////////////////////////////////////////////////
class Day {

  var $day;
  var $events = array();
  var $day_events;

  function Day($day) {
    $this->day = $day;
  }

  /**
   * is_same_day
   *
   * @param timestamp $date 
   * @access public
   * @return boolean true if it's the same day, false otherwise
   */
  function is_same_day($date) {
    if (date("Ymd",$this->day) == date("Ymd",$date)) {
      return true;
    }
    return false;
  }
  
  /**
   * store_agenda_event
   * Add an event in the Day structure 
   * 
   * @param Event $event Event object
   * @param timestamp $begin_date Beginning of the event
   * @param id $uid entity id
   * @param string $entity entity kind
   * @param int $day_event = 1 if the $event var is an all day event
   * @access public
   * @return void
   */
  function store_agenda_event(&$event,$begin_date,$uid,$day_event,$entity="user") {
    if ($day_event == 1) {
      $this->add_day_event(&$event,$uid,$entity);
    } else {
      $this->add_event(&$event,$begin_date,$uid,$entity);
    }
  }

  /**
   * add_event
   * Add a non all day event to the Day structure 
   * 
   * @param Event $event event to add
   * @param timesamp $begin_date  Beginning of the event
   * @param int $uid User id
   * @access public
   * @return void
   */
  function add_event(&$event,$begin_date,$uid,$entity) {
    $date = date("YmdHis",$begin_date);
    if (!isset($this->events[$date.".".$event->id])) {
      $this->events[$date.".".$event->id] = array("event"=>&$event,"date"=>$begin_date,$entity=>array($uid));
      ksort($this->events);
    } else {
      $this->events[$date.".".$event->id][$entity][] = $uid;
    }
  }
  
  /**
   * add_day_event 
   * Add an all day event
   * 
   * @param Event $event event to add
   * @param int $uid user id
   * @access public
   * @return void
   */
  function add_day_event(&$event,$uid,$entity) {
    $this->day_events[$entity][$uid][] = &$event;
  }

  /**
   * get_events
   *
   * Return an Event object array with all the event
   * off the given user.
   * 
   * @param int $uid entity id
   * @access public
   * @return array Event
   */
  function get_events($uid,$entity="user") {
    foreach($this->events as $event) {
      if (!is_array($event[$entity]) || !in_array($uid,$event[$entity])) {
	continue;
      } else {
	$return[] = $event;
      }
    }
    return $return;
  }
 
  /**
   * have_events_between
   * Test if there is one or more event between the two parameters 
   * (including all day events)
   * If an event begin at $end or end at $start it will not be
   * considerated as between $start and $end
   * 
   * @param timestamp $start 
   * @param timestamp $end 
   * @access public
   * @return boolean
   */
  function have_events_between($start,$end,$entity="user") {
    if (count($this->day_events[$entity]) > 0) {
      return true;
    }
    foreach($this->events as $event) {
      if(is_array($event[$entity])) {
        $case = $event["event"]->match_period($event["date"],$start,$end);
      
        if ($case > 0) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * entity_have_events_between
   * Test if there is one or more event between the two parameters 
   * (including all day events) for a given user
   * If an event begin at $end or end at $start it will not be
   * considerated as between $start and $end.
   *  
   * @param timestamp $start 
   * @param timestamp $end 
   * @param int $uid entity id
   * @access public
   * @return boolean
   */
  function entity_have_events_between($start,$end,$uid,$entity="user") {
    if (count($this->day_events[$entity][$uid]) > 0) {
      return true;
    }
    foreach($this->events as $event) {
      if (!is_array($event[$entity]) || !in_array($uid,$event[$entity])) {;
	continue;
      }
      
      $case = $event["event"]->match_period($event["date"],$start,$end);
      
      if ($case > 0) {
	return true;
      }
    }
    return false;
  }
  
  /**
   * get_events_before
   * Return all the events before $end for a given user.
   * The result will be an array with the duration of
   * all events which match, and an array of all events which match.
   * $start is the referer to calculate the duration of the events,
   * 
   * @param timestamp $end 
   * @param int $uid  entity id
   * @param timestamp $start 
   * @access public
   * @return array
   */
  function get_events_before($start,$end, $uid,$entity="user") {
    if (count($this->events) > 0) {
      foreach($this->events as $event) {
      
        $case = $event["event"]->match_period($event["date"],$start,$end);
        
	if ($case == -1) {
	  break;
	}
	if (!is_array($event[$entity]) || !in_array($uid,$event[$entity])) {
	  continue;
        }
        
	$ev_end = $event["event"]->duration + $event["date"];
        $return[] = $event;
        if ($ev_end > $end) {
          $end = $ev_end;
        }
      }
    }
    if ($return != null) {
      $return = array("duration"=>$end - $start,"events"=>$return);
    }
    return $return;
  }
    
  /**
   * get_events_between 
   * 
   * Return all the events between $end and $start for a given user.
   * The result will be an array with the duration of
   * all events which match, and an array of all events which match.
   * $start is the referer to calculate the duration of the events,
   * 
   * @param timestamp $end 
   * @param int $uid  entity id
   * @param timestamp $start 
   * @access public
   * @return array
   */
  function get_events_between($start, $end, $uid,$entity="user") {
    if (count($this->events) > 0) {
      foreach($this->events as $event) {

        $case = $event["event"]->match_period($event["date"],$start,$end);
	
	if ($case == -2) {
	  continue;
	}
	
	if ($case == -1) {
	  break;
	}

	if (!is_array($event[$entity]) || !in_array($uid,$event[$entity])) {
	  continue;
        }
        
	if ($case == 2) {
	  return -1;
	}
              
	if ($case == 1) {
          $return[] = $event;
          $ev_end = $event["event"]->duration + $event["date"];
	  if ($ev_end > $end) {
	    $end = $ev_end;
	  }
	}
      }
    }
    
    if ($return != null) {
      $return = array("duration"=>$end - $start,"events"=>$return);
    }

    return $return;
  }
}


?>
