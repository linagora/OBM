<script language="php">
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
//   - $param_event
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($param_event) {
  global $cdg_sql;

//XXXXXXXX user resource

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
      calendarevent_location, 
      calendarevent_category_id,
      calendarcategory_label,
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
      LEFT JOIN EventEntity ON calendarevent_id = evententity_event_id
      LEFT JOIN CalendarCategory ON calendarevent_category_id = calendarcategory_id
      LEFT JOIN UserObm AS c ON calendarevent_usercreate = c.userobm_id
      LEFT JOIN UserObm AS u ON calendarevent_userupdate = u.userobm_id
    WHERE calendarevent_id = $param_event";

  display_debug_msg($query, $cdg_sql, "run_query_detail()");
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return events users
// Parameters: 
//   - $param_event
///////////////////////////////////////////////////////////////////////////////
function run_query_event_attendees($param_event) {
  global $cdg_sql;

  $query = "SELECT DISTINCT
    userobm_id,
    userobm_lastname,
    userobm_firstname,
    evententity_state as state
  FROM EventEntity
    LEFT JOIN UserObm ON evententity_entity_id = userobm_id
  WHERE 
    evententity_event_id = '$param_event' AND 
    evententity_entity = 'user'";

  display_debug_msg($query, $cdg_sql, "run_query_event_attendees()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the name and first name of users
// Parameters: 
//   - $users : users id array, the event is assigned to 
///////////////////////////////////////////////////////////////////////////////
function run_query_get_user_name($users="") {
  global $cdg_sql;

  $in = "'".implode ("','", $users)."'";

  $obm_q = new DB_OBM;
  $query = "SELECT userobm_lastname, userobm_firstname, userobm_id
            FROM UserObm
	    WHERE userobm_id IN ($in)";

  $query.= " ORDER BY userobm_id"; 
  display_debug_msg($query, $cdg_sql, "run_query_get_user_name()");
  $obm_q->query($query);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Return all the label of resources
// Parameters:
//   - $resources : resource id array, the event is assigned to
///////////////////////////////////////////////////////////////////////////////
function run_query_get_resource_label($resources) {
  global $cdg_sql;

  $in = "'".implode ("','", $resources)."'";

  $obm_q = new DB_OBM;
  $query = "SELECT resource_name, resource_id
            FROM Resource 
            WHERE resource_id IN ($in)";

  $query.= " ORDER BY resource_id";
  display_debug_msg($query, $cdg_sql, "run_query_get_resource_label()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the label of entities 
// Parameters:
//   - $entities : entity id array, the event is assigned to
///////////////////////////////////////////////////////////////////////////////
function run_query_get_entity_label($entities) {

  if (is_array($entities["user"])) {
    $ret["user"] = run_query_get_user_name($entities["user"]);
  }
  if (is_array($entities["resource"])) {
    $ret["resource"] = run_query_get_resource_label($entities["resource"]);
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
function run_query_get_allusers($users_array,$groups_array) {

  $r_array = array();
  if (is_array($groups_array)) {
    foreach($groups_array as $group_id) {
      $r_array = array_merge($r_array, get_all_users_from_group($group_id));
    }
  }
  $r_array = array_merge($users_array,$r_array);
  $r_array = array_unique($r_array);

  return $r_array;
}


///////////////////////////////////////////////////////////////////////////////
// Insert a user decision for an user
// Parameters:
//   - $agenda : Agenda params
///////////////////////////////////////////////////////////////////////////////
function run_query_insert_decision($agenda) {

  $user = $agenda["user_id"];
  $event_id = $agenda["id"];
  $state = $agenda["decision_event"];
  run_query_update_occurence_state($event_id,$user,$state);
}


///////////////////////////////////////////////////////////////////////////////
// Update a user decision for its participation to an event
// Parameters: 
//   - $event_id : event Id
//   - $user_id  : User id
//   - $state    : New state to set (A, W or R)
///////////////////////////////////////////////////////////////////////////////
function run_query_update_occurence_state($event_id, $user_id, $state){
  global $cdg_sql;

  $query = "UPDATE EventEntity 
            SET evententity_state = '$state'
            WHERE evententity_entity_id = $user_id
              AND evententity_event_id = '$event_id'
              AND evententity_entity = 'user'";

  display_debug_msg($query, $cdg_sql, "run_query_update_occurence_state()");  
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;    
}


///////////////////////////////////////////////////////////////////////////////
// Select All waiting Events
///////////////////////////////////////////////////////////////////////////////
function run_query_waiting_events() {
  global $auth, $cdg_sql;

  $uid = $auth->auth["uid"];
  
  $writable_cal = of_right_entity_for_consumer("calendar", "user", $uid, "write");
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type;
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
      calendarcategory_label,
      calendarevent_privacy,
      calendarevent_priority,
      calendarevent_repeatkind,
      calendarevent_repeatdays,
      calendarevent_duration,
      $calendarevent_endrepeat,
      $calendarevent_date		   
    FROM CalendarEvent
      LEFT JOIN CalendarCategory ON calendarevent_category_id = calendarcategory_id
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
  display_debug_msg($query, $cdg_sql, "run_query_waiting_events()");
  $obm_db->query($query);
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Delete all participation to an event
// Parameters:
//   - $agenda[] : hash values for the event
///////////////////////////////////////////////////////////////////////////////
function run_query_delete_user_event($agenda) {
  global $cdg_sql;

  $e_id = $agenda["id"];
  $query = "DELETE FROM EventEntity WHERE evententity_event_id = '$e_id'";

  display_debug_msg($query, $cdg_sql, "run_query_delete_user_event()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
} 


///////////////////////////////////////////////////////////////////////////////
// Insert one event
// Parameters: 
//   - $agenda        : Agenda params
//   - $sel_entity_id : List entitis id ["user"] ["resource"]
///////////////////////////////////////////////////////////////////////////////
function run_query_add_event($agenda, $sel_entity_id, &$event_id) {
  global $uid, $l_from, $l_to, $l_date;
  global $l_add_event_mail_head,$l_add_event_mail_subject;

  $writable_cal = of_right_entity_for_consumer("calendar", "user", $uid, "write");
  $mail  = $agenda["mail"];
  $groups = $sel_entity_id["group"];
  $mail_title = stripslashes($agenda["title"]);
  $datebegin = $agenda["datebegin"];
  $dateend = $agenda["dateend"];
  
  $users_array = $sel_entity_id["user"];
  if (is_array($groups)) {
    foreach($groups as $group_id) {
      $user_group_array = array_merge($user_group_array, get_all_users_from_group($group_id));
    }
    $users_array = array_merge($users_array, $user_group_array);
    $users_array = array_unique($users_array);
  }
  
  $event_id = run_query_insert_event_data($agenda);   
  foreach ($users_array as $user_id) {
    if ($user_id == $uid || in_array($user_id, $writable_cal["ids"])) {
      run_query_insert_user_event($user_id,$event_id,'A');	
    } else {
      run_query_insert_user_event($user_id,$event_id,'W');	
    }
  }

  $subject = "$l_add_event_mail_subject" . $mail_title;
  $message = "$l_add_event_mail_head $mail_title $l_date : $l_from $datebegin $l_to $dateend";
  $recipient_array = array_diff($users_array, array($uid));
  
  if (count($recipient_array) > 0) {
    $tmp_filename = secure_tmpname(".ics","ics_");
    create_ics_meeting_file($agenda, $event_id, $recipient_array, $tmp_filename);
    $h_file = array(
		    array (
			   "filename" => "$tmp_filename",
			   "name" => "meeting.ics",
			   "type" => "ics"
			   )
		    );
    send_mail($subject, $message, $recipient_array, "",$mail, $h_file);
    unlink($tmp_filename);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Insert an event data
// Parameters:
//   - $agenda : hashed agenda params
// Returns: Event id inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_insert_event_data($agenda) {
  global $cdg_sql, $uid;

  $now = date("Y-m-d H:i:s");
  $title = $agenda["title"];
  $category_id = $agenda["category"];
  $priority = $agenda["priority"];
  $description = $agenda["description"];
  $location = $agenda["location"];
  $all_day = $agenda["allday"]; 
  $datebegin = $agenda["datebegin"];
  $dateend = $agenda["dateend"];
  $event_duration = $agenda["event_duration"];
  if ($agenda["privacy"]!=1) $privacy = 0; else $privacy = 1; 
  $repeat_kind = $agenda["kind"];
  $repeat_days = $agenda["repeat_days"]; 
  $repeat_end = $agenda["repeat_end"]; 
  
  $obm_q = new DB_OBM;
  // XXXXXXXXXXXXXXxx revenir comme d'hab. insere, apres on recupere id
  $query = "SELECT MAX(calendarevent_id) as max_id FROM CalendarEvent";
  display_debug_msg($query, $cdg_sql, "run_query_insert_event_data()");
  $obm_q->query($query);
  $obm_q->next_record(); 
  $max_id = $obm_q->f("max_id")+1;

  $query = "INSERT INTO CalendarEvent (
    calendarevent_id,
    calendarevent_timeupdate,                         
    calendarevent_timecreate,                         
    calendarevent_usercreate,
    calendarevent_owner,
    calendarevent_title, 
    calendarevent_date,     
    calendarevent_description,
    calendarevent_location, 
    calendarevent_category_id,
    calendarevent_priority,
    calendarevent_privacy, 
    calendarevent_duration,
    calendarevent_repeatkind,
    calendarevent_repeatfrequence,
    calendarevent_repeatdays,
    calendarevent_allday,
    calendarevent_endrepeat)
  VALUES (
    '$max_id',
    '$now',
    '$now',
    '$uid',
    '$uid',
    '$title',
    '$datebegin',
    '$description',
    '$location',
    '$category_id',
    '$priority',
    '$privacy',
    '$event_duration',		
    '$repeat_kind',
    '1',
    '$repeat_days',
    '$all_day',
    '$repeat_end')";

  display_debug_msg($query, $cdg_sql, "run_query_insert_event_data()");
  $obm_q->query($query);

  return $max_id;
}


///////////////////////////////////////////////////////////////////////////////
// Insert an occurence of a event
// Parameters: 
//   - $user_id :
//   - $event_id :
//   - $state :
///////////////////////////////////////////////////////////////////////////////
function run_query_insert_user_event($user_id,$event_id,$state){
  global $cdg_sql;

  $query = "INSERT INTO EventEntity (
    evententity_event_id, 
    evententity_entity_id,
    evententity_entity,
    evententity_required,
    evententity_state) 
  VALUES (
    '$event_id',
    '$user_id',
    'user',
    '0',
    '$state')";

  display_debug_msg($query, $cdg_sql, "run_query_insert_user_event()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q; 
}


///////////////////////////////////////////////////////////////////////////////
// Perform the export meeting to the vCalendar format
// Parameters:
//   - $agenda : agenda hash values
///////////////////////////////////////////////////////////////////////////////
function create_ics_meeting_file($agenda, $event_id, $attendees, $filename) {
  global $obm_version,$cagenda_weekstart,$auth;
  global $l_file_error, $l_write_error, $l_read_error;

  $fd_dest = fopen ($filename,"w");
  if ($fd_dest == false) {
    die ($l_file_error.$dest."dead");
  }

  $iso_date = $agenda["date"];
  $ts_date = strtotime($iso_date);  
 
  $start_time = strtotime("-1 year", $ts_date);
  $end_time = strtotime("+1 year", $ts_date);

  $calendar_user = array ($auth->auth["uid"] => "dummy");  
  $obm_event = run_query_detail($event_id);

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
    $category = strtoupper($obm_event->f("calendarcategory_label")); 
    $date_ts = $obm_event->f("calendarevent_timeupdate");
    $date_ts = gmdate("Ymd\THis\Z",$date_ts);
    if ($obm_event->f("calendarevent_description") != "") {
      $description = preg_replace("/\\r?\\n/","\\n",$obm_event->f("calendarevent_description"));
      $description = "DESCRIPTION:" . $description;
      $description = "\n" . wordwrap($description,74,"\n ",1);
    } else {
      $description = "";
    }
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
    while ($attendee_q->next_record()) {
      $msg_attendee .= "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=\"'";
      $msg_attendee .= $attendee_q->f("userobm_lastname")." ".$attendee_q->f("userobm_firstname");
      $msg_attendee .= "'\":MAILTO:";
      $msg_attendee .= $attendee_q->f("userobm_email");
    }
    $msg .= "
BEGIN:VEVENT
UID:$id    
$title
CLASS:$private
ORGANIZER;CN=\"$organizer_cn\":MAILTO:$organizer_email
$msg_attendee
LOCATION:$location
CATEGORIES:$category
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
    $category = strtoupper($obm_event->f("calendarcategory_label")); 
    if ($obm_event->f("calendarevent_description") != "") {
      $description = "\nDESCRIPTION:".$obm_event->f("calendarevent_description");
    } else {
      $description = "";
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
CATEGORIES:$category
STATUS:CONFIRMED$description
DTSTART:$date_b
DTEND:$date_e
RRULE:$repeat
END:VEVENT";
  }

  $msg .= "
END:VCALENDAR";

  $msg = iconv("ISO-8859-1","UTF-8",$msg);

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
function check_for_conflict($agenda, $sel_entity_id) {

  $ts_begin = isodate_to_timestamp($agenda["datebegin"]);
  $ts_end = isodate_to_timestamp($agenda["dateend"]);
  $id = $agenda["id"];
  $obm_q = run_query_conflict_events($ts_begin, $ts_end, $sel_entity_id, $id);
  if ($obm_q->nf() == 0) {
    return false;
  }
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Check for conflict : Only with the first occurence of each event
///////////////////////////////////////////////////////////////////////////////
function check_for_decision_conflict($agenda) {
  
  $user["user"] = $agenda["user_id"];
  $event_id = $agenda["id"];
  $e_q = run_query_detail($event_id);
  $ts_begin = $e_q->f("calendarevent_date");
  $ts_end = $e_q->f("calendarevent_date") + $e_q->f("calendarevent_duration");
  $c_q = run_query_conflict_events($ts_begin,$ts_end,$user,$event_id);
  if ($c_q->nf() == 0) {
    return false;
  }
  return $c_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get all event categories
///////////////////////////////////////////////////////////////////////////////
function run_query_get_eventcategories() {
  global $cdg_sql;

  $query = "SELECT * FROM CalendarCategory ORDER BY calendarcategory_label";
  display_debug_msg($query, $cdg_sql, "run_query_get_eventcategories()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Event Update Execution
// Parameters: 
//   - $agenda      : Agenda params
//   - $users_array : List of the users 
///////////////////////////////////////////////////////////////////////////////
function run_query_event_update($agenda,$users_array,&$event_id) {
  global $c_use_connectors, $uid, $l_date;
  global $l_update_event_mail_head,$l_update_event_mail_subject,$l_update_event_mail_body,$l_to,$l_from;

  $datebegin = $agenda["datebegin"];
  $dateend = $agenda["dateend"];
  $event_id = $agenda["id"];
  $groups = $agenda["group"];
  $mail = $agenda["mail"];
  $mail_title = stripslashes($agenda["title"]);
  $writable_cal = of_right_entity_for_consumer("calendar", "user", $uid, "write");

  if (is_array($groups)) {
    foreach($groups as $group_id) {
      $user_group_array = array_merge($user_group_array, get_all_users_from_group($group_id));
    }
    $users_array = array_merge($users_array,$user_group_array);
    $users_array = array_unique($users_array);
  }
  
  $event_data = run_query_detail($event_id);
  $old_begin = isodate_format($event_data->f("calendarevent_date"),"", 1);
  $old_end = isodate_format($event_data->f("calendarevent_date") + $event_data->f("calendarevent_duration"),"",1);

  // Handle Connectors data
  if ($c_use_connectors) {
    $obm_q = new DB_OBM;
    $now = date("Y-m-d H:i:s");
    $old_users = get_event_users_info($event_id);
    $removed_users = array_diff($old_users, $users_array);
    $new_users = array_diff($users_array, $old_users);

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
	display_debug_msg($query, $cdg_sql, "run_query_event_update(1)");
	$retour = $obm_q->query($query);
      }
    }

    // If users have been inserted they must not appear in DeletedCalendarEvent
    if (count($new_users) > 0) {
      foreach($new_users as $u_id) {
	$query = "DELETE FROM DeletedCalendarEvent
      WHERE deletedcalendarevent_event_id = '$event_id'
        AND deletedcalendarevent_user_id = '$u_id'";
	display_debug_msg($query, $cdg_sql, "run_query_event_update(2)");
	$retour = $obm_q->query($query);
      }
    }
  }

  // XXXX bizarre les old_end et old_begin semblent etre affectes par l'update
  // ci-dessous sous mysql 4.0.23-10 (portable fourmi)
  run_query_update_event_data($agenda);  
  run_query_delete_user_event($agenda);
  foreach($users_array as $user_id) {
    if ($user_id == $uid || in_array($user_id,$writable_cal["ids"])) {
      run_query_insert_user_event($user_id,$event_id,'A');	
    } else {
      run_query_insert_user_event($user_id,$event_id,'W');	
    }
  }

  $subject = "$l_update_event_mail_subject" . $mail_title;
  $message = "$l_update_event_mail_head $mail_title
$l_date : $l_from $datebegin $l_to $dateend";
  if (($old_begin != $datebegin || $old_end != $dateend) ) {
    $message .= "
$l_update_event_mail_body $l_from $old_begin $l_to $old_end";
  }

  $recipient_array = array_diff($users_array, array($uid));
  send_mail($subject, $message, $recipient_array, "",$mail);
}


///////////////////////////////////////////////////////////////////////////////
// Event Data Update Execution
// Parameters: 
//   - $agenda : Agenda params
///////////////////////////////////////////////////////////////////////////////
function run_query_update_event_data($agenda) {
  global $cdg_sql, $uid;

  $now = date("Y-m-d H:i:s");
  $title = $agenda["title"];
  $category_id = $agenda["category"];
  $priority = $agenda["priority"];
  $description = $agenda["description"];
  $location = $agenda["location"];
  $datebegin = $agenda["datebegin"];
  $event_duration = $agenda["event_duration"];
  $all_day = $agenda["allday"];
  $event_id = $agenda["id"]; 
  if ($agenda["privacy"] != 1) $privacy = 0; else $privacy = 1; 
  $repeat_kind = $agenda["kind"];
  $repeat_days = $agenda["repeat_days"]; 
  $repeat_end = $agenda["repeat_end"]; 
   
  $obm_q = new DB_OBM;
  $query = "UPDATE CalendarEvent SET
    calendarevent_timeupdate = '$now',
    calendarevent_userupdate = '$uid', 
    calendarevent_title = '$title', 
    calendarevent_date = '$datebegin',     
    calendarevent_description = '$description',
    calendarevent_location = '$location',
    calendarevent_category_id = '$category_id',
    calendarevent_priority = '$priority',
    calendarevent_privacy = '$privacy', 
    calendarevent_duration = '$event_duration',
    calendarevent_repeatkind = '$repeat_kind',
    calendarevent_repeatdays = '$repeat_days',
    calendarevent_allday = '$all_day', 
    calendarevent_endrepeat = '$repeat_end'
  WHERE
    calendarevent_id = '$event_id'
  ";
  display_debug_msg($query, $cdg_sql, "run_query_update_event_data()");
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Delete all events of a evenements, and the event
// Parameters: 
//   - $agenda : agenda infos hash
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($agenda) {
  global $cdg_sql, $c_use_connectors, $l_to, $l_from;
  global $l_delete_event_mail_head,$l_delete_event_mail_subject,$l_delete_event_mail_body;

  $obm_q = new DB_OBM;
  $event_id = $agenda["id"];
  $mail = $agenda["mail"]; 
  
  $users_array = get_event_users_info($event_id);

  $query = "SELECT calendarevent_title
    FROM CalendarEvent
    WHERE calendarevent_id = '$event_id'";
  display_debug_msg($query, $cdg_sql, "run_query_delete(1)");
  $obm_q->query($query);
  $obm_q->next_record();
  $title = $obm_q->f("calendarevent_title");

  $query = "DELETE FROM CalendarException WHERE calendarexception_event_id = '$event_id'";
  display_debug_msg($query, $cdg_sql, "run_query_delete(2)");
  $obm_q->query($query);
  $query = "DELETE FROM CalendarEvent WHERE calendarevent_id = '$event_id'";
  display_debug_msg($query, $cdg_sql, "run_query_delete(3)");
  $obm_q->query($query);
  $query = "DELETE FROM EventEntity WHERE evententity_event_id = '$event_id'";
  display_debug_msg($query, $cdg_sql, "run_query_delete(4)");
  $obm_q->query($query);

  // If connectors in use
  if ($c_use_connectors) {
    $now = date("Y-m-d H:i:s");
    
    // We purge the individual user entries
    $query = "DELETE FROM DeletedCalendarEvent
      WHERE deletedcalendarevent_event_id = '$event_id'";
    display_debug_msg($query, $cdg_sql, "run_query_delete()");
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
    display_debug_msg($query, $cdg_sql, "run_query_delete()");
    $retour = $obm_q->query($query);
  }

  $message = $l_delete_event_mail_head.$title;   
  $subject = "$l_delete_event_mail_subject" . $title;
  $recipient_array = array_diff($users_array, array($uid));
  send_mail($subject, $message, $recipient_array, "", $mail);
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of default agenda resources for the current group
///////////////////////////////////////////////////////////////////////////////
function get_default_group_resource_ids($p_group) {
  global $cdg_sql, $auth, $agenda, $c_all;

  $uid = $auth->auth["uid"];
  $group = $p_group;
  if ($p_group != $c_all) {
  $query = "SELECT r.*,
      c.entityright_read
    FROM RGroup g, ResourceGroup rg, Resource as r
      LEFT OUTER JOIN EntityRight as c
        ON 
          c.entityright_customer_id = '$uid' AND 
          r.resource_id = c.entityright_entity_id AND
          c.entityright_entity = 'resource'
    WHERE
      g.rgroup_id = '$group' AND
      rg.resourcegroup_rgroup_id = g.rgroup_id AND
      rg.resourcegroup_resource_id = r.resource_id AND
      (c.entityright_read = 1 OR r.resource_id='$uid') 
    ORDER BY r.resource_name";
  } else {
    $query = "SELECT r.*,
        c.entityright_read
      FROM Resource as r
        LEFT OUTER JOIN EntityRight as c
          ON 
            c.entityright_customer_id = '$uid' AND 
            r.resource_id = c.entityright_entity_id AND
            c.entityright_entity = 'resource'
      WHERE (c.entityright_read = 1 OR r.resource_id='$uid')
      ORDER BY r.resource_name";
  }
  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "get_default_group_resource_ids()");
  $obm_db->query($query);

  $ret_array = array();
  while($obm_db->next_record()) {
     $ret_array[] = $obm_db->f("resource_id");
  }

  return $ret_array;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable agenda entities for the current user:
//   - user
//   - users group
//   - resource
//   - resources group
///////////////////////////////////////////////////////////////////////////////
function get_entity_readable() {

  $ent = array(
    "user" => get_calendar_readable(),
    "group" => get_group_readable(),
    "resource" => get_resource_readable(),
    "resourcegroup" => run_query_resource_group()
  );

  return $ent;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable agenda users for the current user
///////////////////////////////////////////////////////////////////////////////
function get_calendar_readable() {
  global $cdg_sql, $auth, $agenda, $c_all;

  $uid = $auth->auth["uid"];
  $gusers = "";
  if ( (isset($agenda["entity"]["group_view"]))
       && ($agenda["entity"]["group_view"] != $c_all) ) {
    $gusers = get_all_users_from_group($agenda["entity"]["group_view"]);
  }
  $entity = of_right_entity_for_consumer("calendar", "user", $uid, "read", $gusers);

  // Add the user own calendar
  $entity["ids"][] = $uid;
  $obm_q = run_query_get_user_name(array($uid));
  $obm_q->next_record();
  $id = $obm_q->f("userobm_id");
  $label = $obm_q->f("userobm_lastname") ." ". $obm_q->f("userobm_firstname");
  $entity["entity"][] = array("id" => $id, "label" => $label);

  return $entity;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable calendar group for the current user
///////////////////////////////////////////////////////////////////////////////
function get_group_readable() {
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
  display_debug_msg($query, $cdg_sql, "get_group_readable()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable calendar group for the current user
///////////////////////////////////////////////////////////////////////////////
function get_resource_readable() {
  global $cdg_sql, $auth, $cagenda_public_groups;

  $uid = $auth->auth["uid"];
  $entity = of_right_entity_for_consumer("resource", "user", $uid, "read");

  return $entity;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable agenda ressource for the current user
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_readable() {
  global $cdg_sql, $auth, $agenda, $c_all;

  $uid = $auth->auth["uid"];

  if ( (is_array($agenda["group_ids"]))
       && (count($agenda["group_ids"]) == 1)
       && ($agenda["group_ids"][0] != $c_all) ) {
    $group_id = $agenda["group_ids"][0];
    $query = "SELECT distinct r.*,
        c.calendarentityright_read,
        c.calendarentityright_customer_id
      FROM RGroup g, ResourceGroup rg, Resource as r
        LEFT OUTER JOIN CalendarEntityRight as c
          ON 
            (c.calendarentityright_customer_id = '$uid' OR c.calendarentityright_customer_id = 0) AND 
            r.resource_id = c.calendarentityright_entity_id AND 
            c.calendarentityright_entity = 'resource'
      WHERE
        $where
        g.rgroup_id = '$group_id' AND
        rg.resourcegroup_rgroup_id = g.rgroup_id AND
        rg.resourcegroup_resource_id = r.resource_id AND
        (c.calendarentityright_read = 1) 
      ORDER BY r.resource_name";
  } else {
    $entity = of_right_entity_for_consumer("resource", "user", $uid, "read");
    return $entity;
  }

  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "run_query_resource_readable()");
  $obm_db->query($query);
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of resource group for the current user
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_group() {
  global $cdg_sql, $auth;

  $uid = $auth->auth["uid"];
  $where = sql_obm_entity_privacy("rgroup");
  $query = "
    SELECT *
    FROM RGroup
    WHERE ";
  $query .= $where;
  $query .= " ORDER BY rgroup_name";

  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "run_query_resource_group()");
  $obm_db->query($query);
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Get groups infos from the gid array given
// Parameters:
//   - groups : array of groups id
///////////////////////////////////////////////////////////////////////////////
function run_query_group_in($groups) {
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
    display_debug_msg($query, $cdg_sql, "run_query_group_in()");
    $obm_q = new DB_OBM;
    $obm_q->query($query);
  } else {
    $obm_q = "";
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of writable agenda groups for the current user
///////////////////////////////////////////////////////////////////////////////
function run_query_group_writable() {
  global $cdg_sql,$auth;

  $uid = $auth->auth["uid"];

  $query = "SELECT * from UGroup";
  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "run_query_group_writable()");	    
  $obm_db->query($query);
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// category insertion query construction and execution
// Parameters:
//   - $label : category label
///////////////////////////////////////////////////////////////////////////////
function run_query_category_insert($agenda) {
  global $auth, $cdg_sql;

  $label = $agenda["category_label"];
  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];

  $query = "insert into CalendarCategory (
    calendarcategory_timecreate,
    calendarcategory_usercreate,
    calendarcategory_label)
  values (
    '$timecreate',
    '$usercreate',
    '$label')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// category update query execution
// Parameters:
//   - $label     : label to set
//   - $label_old : category label to update
///////////////////////////////////////////////////////////////////////////////
function run_query_category_update($agenda) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $label = $agenda["category_label"];
  $category_id = $agenda["category_id"];
  $query = "update CalendarCategory set
    calendarcategory_label='$label',
    calendarcategory_timeupdate='$timeupdate',
    calendarcategory_userupdate='$userupdate'
      where
    calendarcategory_id ='$category_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// category deletion query execution
// Parameters:
//   - category_id     : category id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_category_delete($agenda) {
  global $cdg_sql;

  $category_id = $agenda["category_id"];
  $query = "delete from CalendarCategory where calendarcategory_id = $category_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// category deletion query execution
// Parameters:
//   - $label     : category label to delete
///////////////////////////////////////////////////////////////////////////////
function get_category_label($agenda) {
  global $cdg_sql;

  $category_id = $agenda["category_id"];
  $query = "Select * from CalendarCategory Where calendarcategory_id = $category_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q->f("calendarcategory_label");
}

///////////////////////////////////////////////////////////////////////////////
// category - Company links query execution
// Parameters:
//   - $p_id : category id
///////////////////////////////////////////////////////////////////////////////
function run_query_category_links($p_id) {
  global $cdg_sql;

  $query = "select count(*) as numlink from CalendarEvent where calendarevent_category_id = $p_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return tables of hashed entities.
// Parameters :
//  - $entities_array : hashed array of userobm and resource DBO
///////////////////////////////////////////////////////////////////////////////
function store_entities($entities_q) {
  global $ico_calendar_user0,$ico_calendar_user1,$ico_calendar_user2;
  global $ico_calendar_user3,$ico_calendar_user4,$ico_calendar_user5;

  $i = 0;
  $entity_tab = array();
  $user_q = $entities_q["user"];
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
    }
  }

  return $entity_tab;
}


///////////////////////////////////////////////////////////////////////////////
// Return the event infos
// Parameters:
//   - $id : event id
///////////////////////////////////////////////////////////////////////////////
function get_event_info($id) {
  global $cdg_sql;

  if ($id == "") {
    return false;
  }

  $query = "SELECT * FROM CalendarEvent WHERE calendarevent_id='$id'";

  display_debug_msg($query, $cdg_sql, "get_event_info()");
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
function get_event_users_info($id) {
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
// Agenda Form Data checking and formatting
// Parameters:
//   - $agenda[] : values checked
//     keys used  : num, name, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_data_form($agenda) {
  global $err_msg, $php_regexp_isodate, $php_regexp_isodatetime;
  global $l_fill_title, $l_fill_dateend,$l_fill_datebegin,$l_err_weekly_repeat;
  global $l_invalid_date, $l_datebegin, $l_dateend, $l_date_repeatend;
  global $l_err_begin_end, $l_err_end_repeat,$l_err_end_repeat2;
  global $l_err_end_repeat3, $l_err_days_repeat, $l_err_days_repeat_not_weekly;
  
  $title = $agenda["title"];
  $datebegin = $agenda["datebegin"];
  $dateend = $agenda["dateend"];
  $repeat_end = $agenda["repeat_end"];
  $kind = $agenda["kind"];
  $repeat_days = $agenda["repeat_days"];

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
function localizeDate($format, $timestamp) {
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
function get_date_day_of_week($timestamp, $day) {
  global $cagenda_weekstart;

  if (!isset($cagenda_weekstart)) $cagenda_weekstart = 'Sunday';
  $num = date('w', strtotime($cagenda_weekstart));
  $start_day_time = strtotime((date('w',$timestamp)==$num ? "$cagenda_weekstart" : "last $cagenda_weekstart"), $timestamp);
  $ret_ts = strtotime($day,$start_day_time);
  // we correct the value to eliminate DST pbs
  $ret_ts = strtotime('+12 hours', $ret_ts);
  $ret_ts = strtotime(isodate_format($ret_ts));

  return $ret_ts;
}


///////////////////////////////////////////////////////////////////////////////
// Slice the entities to display. Limit the entities selected to the entities
// really readable by the user, and limit to a max (=6) for display
// Parameters:
//   - $sel_entity_id   : ["user"] ["resource"] ids
//   - $entity_readable : if given, entities eligibles
///////////////////////////////////////////////////////////////////////////////
function slice_entities($sel_entity_id, $entity_readable="") {
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
  if (count($res_user_id) == 0) {
    $res_user_id = array($auth->auth["uid"]);
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
  if (count($res_resource_id) > $max_res) {
    $res_resource_id = array_slice ($res_resource_id, 0, $max_res);
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
function get_week_num($date) {
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
function run_query_conflict_events($start,$end,$sel_entity_id,$event_id) {
  global $cdg_sql, $uid;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date");
  $calendarevent_date_l = sql_date_format($db_type,"calendarevent_date","calendarevent_date");

  // get all users (from groups too)
  $users = run_query_get_allusers($sel_entity_id["user"], $sel_entity_id["group"]);

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
    $where_ent = "AND ( $where_user || $where_res)";
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
      evententity_entity,
      evententity_entity_id,
      $select_user
      $select_resource
      calendarcategory_label,
      $calendarevent_date_l,
      calendarevent_duration,
      calendarevent_allday
    FROM CalendarEvent
      LEFT JOIN CalendarCategory ON calendarcategory_id = calendarevent_category_id
      LEFT JOIN EventEntity ON calendarevent_id = evententity_event_id
      $join_user
      $join_resource
    WHERE evententity_state = 'A'
      AND $calendarevent_date > $start - calendarevent_duration
      AND $calendarevent_date < $end 
      $where_ent
      $where_event
    ORDER BY calendarevent_date";

  display_debug_msg($query, $cdg_sql, "run_query_conflict_events(()");
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
function run_query_no_repeat_events($start,$end, $calendar_entity) {
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
    $where_ent = "AND ( $where_user || $where_res)";
  } elseif ($has_user || $has_resource) {
    $where_ent = "AND $where_user $where_res";
  }

  $query = "SELECT
      calendarevent_id,
      calendarevent_title,
      calendarevent_privacy,
      calendarevent_description,
      calendarevent_location,
      evententity_entity_id,
      calendarcategory_label,
      $calendarevent_date_l,
      calendarevent_duration,
      calendarevent_allday
    FROM CalendarEvent
      LEFT JOIN CalendarCategory
        ON calendarevent_category_id = calendarcategory_id
      LEFT JOIN EventEntity ON calendarevent_id = evententity_event_id
    WHERE evententity_state = 'A'
      AND calendarevent_repeatkind = 'none'
      AND $calendarevent_date >= $start - calendarevent_duration
      AND $calendarevent_date <= $end
      $where_ent
    ORDER BY calendarevent_date"; 

  display_debug_msg($query, $cdg_sql, "run_query_no_repeat_events()");
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a week of users or/and groups
// Parameters:
//   - $agenda          : agenda params
//   - $calendar_entity : array of entities elements to display
///////////////////////////////////////////////////////////////////////////////
function run_query_repeat_events($start, $end, $calendar_entity) {
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

  if ($has_user && $has_resource) {
    $where_ent = "AND ( $where_user || $where_res)";
  } elseif ($has_user || $has_resource) {
    $where_ent = "AND $where_user $where_res";
  }

  $query = "SELECT
      calendarevent_id,
      calendarevent_title,
      calendarevent_privacy,
      calendarevent_description, 
      calendarevent_location, 
      calendarcategory_label,
      $calendarevent_date_l,
      calendarevent_duration,
      calendarevent_repeatkind,
      $calendarevent_endrepeat_l,
      calendarevent_repeatfrequence,
      evententity_entity_id,
      calendarevent_repeatdays,
      calendarevent_allday
    FROM CalendarEvent, CalendarCategory, EventEntity
    WHERE calendarevent_category_id = calendarcategory_id
      AND calendarevent_id = evententity_event_id
      AND evententity_state = 'A'
      AND calendarevent_repeatkind != 'none'
      AND $calendarevent_date <= $end 
      AND ($calendarevent_endrepeat >= $start - calendarevent_duration
           OR $calendarevent_endrepeat = '0')
      $where_ent
    ORDER BY calendarevent_date"; 

  display_debug_msg($query, $cdg_sql, "run_query_repeat_events()");
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
function events_model($start_time, $end_time, $calendar_entity) {
  global $day_duration;

  $day_duration = 86400;
  $nr_q = run_query_no_repeat_events($start_time,$end_time,$calendar_entity);
  while ($nr_q->next_record()) {
    $id = $nr_q->f("calendarevent_id");
    $title = $nr_q->f("calendarevent_title");
    $privacy = $nr_q->f("calendarevent_privacy");
    $description = $nr_q->f("calendarevent_description"); 
    $location = $nr_q->f("calendarevent_location"); 
    $category = $nr_q->f("calendarcategory_label");
    $date = $nr_q->f("calendarevent_date");
    $duration = $nr_q->f("calendarevent_duration");
    $user_id = $nr_q->f("evententity_entity_id");
    $all_day = $nr_q->f("calendarevent_allday");    
    $event = &new Event($id,$duration,$title,$category,$privacy,$description);
    store_event($date, $event, $day_events, $user_id, $all_day, $end_time);
  }
  
  $r_q = run_query_repeat_events($start_time,$end_time,$calendar_entity);
  while ($r_q->next_record()) {
    $id = $r_q->f("calendarevent_id");
    $title = $r_q->f("calendarevent_title");
    $privacy = $r_q->f("calendarevent_privacy");
    $description = $r_q->f("calendarevent_description"); 
    $location = $r_q->f("calendarevent_location"); 
    $category = $r_q->f("calendarcategory_label");
    $date = $r_q->f("calendarevent_date");
    $duration = $r_q->f("calendarevent_duration");
    $repeatkind = $r_q->f("calendarevent_repeatkind");
    $endrepeat = $r_q->f("calendarevent_endrepeat");
    $all_day = $r_q->f("calendarevent_allday");     
    if (!$endrepeat) {
      $endrepeat = $end_time;
    }
    $repeatfrequence = $r_q->f("calendarevent_repeatfrequence");
    $repeatdays = $r_q->f("calendarevent_repeatdays");
    $user_id = $r_q->f("evententity_entity_id");    
    $all_day = $r_q->f("calendarevent_allday");       
    $event = &new Event($id,$duration,$title,$category,$privacy,$description);
    $temp_date = $date;
    $end_date = ($endrepeat < $end_time) ? $endrepeat : $end_time;
    $end_date += $day_duration;
    switch ($repeatkind) {
      case "daily" :
        daily_repeatition($date,$start_time,$end_date,$repeatfrequence,&$day_events,&$event,$user_id,$all_day);	
        break; 
      case "weekly" :
	weekly_repeatition($date,$start_time,$end_date,$repeatdays,$repeatfrequence,&$day_events,&$event,$user_id,$all_day); 
	break;
      case "monthlybyday" :
	monthlybyday_repeatition($date,$start_time,$end_date,$repeatfrequence,&$day_events,&$event,$user_id,$all_day); 
        break;
      case "monthlybydate" :
	monthlybydate_repeatition($date,$start_time,$end_date,$repeatfrequence,&$day_events,&$event,$user_id,$all_day) ;
	break;
      case "yearly" :
	yearly_repeatition($date,$start_time,$end_date,$repeatfrequence,&$day_events,&$event,$user_id,$all_day);
	break;	
    }
  }
  return $day_events; 
}


///////////////////////////////////////////////////////////////////////////////
// Perform the daily repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function store_event($date, &$event, &$day_events,$user_id,$all_day,$end_time) {
  $temp_date = $date;
  $event_end = $date + $event->duration;
  do {
    $iso_date = isodate_format($temp_date);
    $day = &$day_events[$iso_date];
    if (!is_object($day) || !$day->is_same_day($temp_date)) {
      $day = &new Day($temp_date);
      $day_events[$iso_date] = &$day;
    }
    $day->store_event(&$event,$date,$user_id,$all_day);
    $temp_date = strtotime("+1 day",$temp_date);
  } while ($event_end > $temp_date && $temp_date < $end_time);
}


///////////////////////////////////////////////////////////////////////////////
// Perform the daily repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function daily_repeatition($date,$start_time,$end_date,$repeatfrequence,&$day_events,&$event,$user_id,$all_day) {
  global $day_duration;

  $temp_date = $date;
  if ($temp_date < $start_time) {
    $temp_date = ceil(($start_time - $date) / $day_duration);
    $hour_diff = get_hour($date) - get_hour($start_time);
    $min_diff = get_min($date) - get_min($start_time);
    if ($temp_date > 0) {
      $temp_date = ($temp_date % $repeatfrequence);
    }	    
    $temp_date = strtotime("+$temp_date days +$hour_diff hours +$min_diff minutes", $start_time);
  }
  while ($temp_date <= $end_date) {
    $iso_date = isodate_format($temp_date);
    $day = &$day_events[$iso_date];
    if(!is_object($day)) {
      $day = &new Day($temp_date);
      $day_events[$iso_date] = &$day;
    }
    store_event($temp_date, $event, $day_events, $user_id, $all_day,$end_date);
    $temp_date = strtotime("+$repeatfrequence days",$temp_date);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Perform the weekly repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function weekly_repeatition($date,$start_time,$end_date,$repeatdays,$repeatfrequence,&$day_events,&$event,$user_id,$all_day) {
  global $day_duration,$cagenda_weekstart;

  $temp_date = get_date_day_of_week($date, $cagenda_weekstart);
  $temp_date = strtotime("+".get_hour($date)." hours +".date("i",$date)." minutes",$temp_date);
  if ($temp_date < $start_time) {
    $temp_date = ceil(($start_time - $temp_date) / $day_duration);
    $hour_diff = get_hour($date) - get_hour($start_time);
    $min_diff = date("i",$date) - date("i",$start_time);
    $temp_date = ($temp_date % ($repeatfrequence * 7));
    if ($temp_date == "") $temp_date = 0;
    $temp_date = strtotime("-$temp_date days +$hour_diff hours +$min_diff minutes", $start_time);
  }
  $num = (date('w', $temp_date) - date('w', strtotime($cagenda_weekstart)) +7)%7;
  for ($i=$num; $i<strlen($repeatdays);$i++) {
    $repeat_days[$i - $num]=substr($repeatdays, $i, 1);
  }	
  while ($temp_date <= $end_date) {
    foreach ($repeat_days as $delta_day => $repeatition) {	  
      if ($repeatition == 1) {
	$current_date = strtotime("+$delta_day days",$temp_date);
        $iso_date = isodate_format($current_date);
	if ($current_date >= $date) {
	  $day = &$day_events[$iso_date];
	  if (!is_object($day)) {
	    $day = &new Day($current_date);
	    $day_events[$iso_date] = &$day;
	  }
	  store_event($current_date, $event, $day_events,$user_id,$all_day,$end_date);
	}
      }
    } 
    $temp_date = strtotime("+$repeatfrequence weeks",$temp_date);
  }	
}


///////////////////////////////////////////////////////////////////////////////
// Perform the monthly by day repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function monthlybyday_repeatition($date,$start_time,$end_date,$repeatfrequence,&$day_events,&$event,$user_id,$all_day) {
  global $day_duration,$cagenda_weekstart;

  $temp_date = $date;
  $start_week = date('w', strtotime($cagenda_weekstart));
  $num = (date('w', $date) - $start_week +7)%7;
  $monthlybyday = ceil(date('d',$date) / 7);
  $event_end = $date + $event->duration;
  if ($event_end < $start_time) {
    $month_diff = (get_year($start_time) - get_year($temp_date)) * 12;
    $month_diff += get_month($start_time) - get_month($temp_date);
    $hour_diff = get_hour($date) - get_hour($start_time);
    $min_diff = get_min($date) - get_min($start_time);
    $month_diff = $month_diff % ($repeatfrequence);
    if ($month_diff > 0) {
      $month_diff = $repeatfrequence - $month_diff;
    }
    $temp_date = strtotime("+$month_diff months +$hour_diff hours +$min_diff minutes", $start_time);
  }
  $hour_diff = get_hour($date);
  $min_diff = get_min($date);
  $ts_cur = strtotime(date("Y-m-01",$temp_date));
  while ($ts_cur <= $end_date) {
    $start_num = (date('w', $ts_cur) - $start_week +7)%7;
    $day_diff = ($num - $start_num +7)%7;
    $day_diff += 7*($monthlybyday-1);
    $current_date = strtotime("+ $day_diff days +$hour_diff hours +$min_diff minutes",$ts_cur);
    $event_end = $current_date + $event->duration;
    if (get_month($ts_cur) == get_month($current_date)
       && $event_end >= $start_time
       && $current_date <= $end_date) {
      $iso_date = isodate_format($current_date);
      $day = &$day_events[$iso_date];
      if (!is_object($day)) {
	$day = &new Day($current_date);
	$day_events[$iso_date] = &$day;
      }
      store_event($current_date, $event, $day_events,$user_id,$all_day,$end_date);
    }
    $ts_cur = strtotime("+$repeatfrequence months", $ts_cur);
  }	
}


///////////////////////////////////////////////////////////////////////////////
// Perform the monthly by date repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function monthlybydate_repeatition($date,$start_time,$end_date,$repeatfrequence,&$day_events,&$event,$user_id,$all_day) {  
  global $day_duration;

  $temp_date = $date;
  if ($temp_date < $start_time) {
    $temp_date = strtotime(date("Y-m-01",$start_time));
    $month_diff = (get_year($start_time) - get_year($date)) * 12;
    $month_diff += get_month($start_time) - get_month($date);
    $hour_diff = get_hour($date) - get_hour($start_time);
    $min_diff = get_min($date) - get_min($start_time);
    $month_diff = $month_diff % ($repeatfrequence);
    if ($month_diff > 0) {
      $month_diff = $repeatfrequence - $month_diff;
    }

    $temp_date = strtotime("+$month_diff months +$hour_diff hours +$min_diff minutes", $temp_date);
  }	
  $hour_diff = get_hour($date);
  $min_diff = get_min($date);  	
  $ts_date = strtotime(date("Y-m-".date("d",$date),$temp_date));
  $ts_date = strtotime("+$hour_diff hours +$min_diff minutes",$ts_date);
  while ($ts_date <= $end_date) {
    $iso_date = isodate_format($ts_date);
    $day = &$day_events[$iso_date];
    if (!is_object($day)) {
      $day = &new Day($ts_date);
      $day_events[$iso_date] = &$day;
    }
    store_event($ts_date, $event, $day_events,$user_id,$all_day,$end_date);
    $ts_date = strtotime("+$repeatfrequence months",$ts_date);
  }  
}


///////////////////////////////////////////////////////////////////////////////
// Perform the yearly repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function yearly_repeatition($date,$start_time,$end_date,$repeatfrequence,&$day_events,&$event,$user_id,$all_day) {  
  global $day_duration;

  $temp_date = $date;    
  if ($temp_date < $start_time) {
    $temp_date = strtotime(date("Y-m-01",$start_time));
    $month_diff = (get_year($start_time) - get_year($date)) * 12;
    $month_diff += get_month($start_time) - get_month($date);
    $hour_diff = get_hour($date) - get_hour($start_time);
    $min_diff = get_min($date) - get_min($start_time);
    $month_diff = $month_diff % ($repeatfrequence*12);
    if ($month_diff > 0) {
      $month_diff = $repeatfrequence*12 - $month_diff;
    }
    $temp_date = strtotime("+$month_diff months +$hour_diff hours +$min_diff minutes", $temp_date);
  }
  $hour_diff = get_hour($date);
  $min_diff = get_min($date);
  $ts_date = strtotime(date("Y-m-".date("d",$date),$temp_date));
  $ts_date = strtotime("+$hour_diff hours +$min_diff minutes",$ts_date);
  while ($ts_date <= $end_date) {
    $iso_date = isodate_format($ts_date);
    $day = &$day_events[$iso_date];
    if (!is_object($day)) {
      $day = &new Day($ts_date);
      $day_events[$iso_date] = &$day;
    }
    store_event($ts_date, $event, $day_events,$user_id,$all_day,$end_date);
    $ts_date = strtotime("+$repeatfrequence years",$ts_date);
  }
}


/////////////////////////////////////////////////////////////////////////////
// Class Event :
// Describe an event.
//////////////////////////////////////////////////////////////////////////////
class Event {
  
  var $id;
  var $duration;
  var $title;
  var $category;
  var $privacy;
  var $description;

  function Event($id,$duration,$title,$category,$privacy,$description) {
    $this->id = $id;
    $this->duration = $duration;
    $this->title = $title;
    $this->category = $category;
    $this->privacy = $privacy;
    $this->description = $description;
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

  function is_same_day($date) {
    if (date("Ymd",$this->day) == date("Ymd",$date)) {
      return true;
    }
    return false;
  }
  
  function store_event(&$event,$begin_date,$uid,$day_event) {
    if ($day_event == 1) {
      $this->add_day_event(&$event,$uid);
    } else {
      $this->add_event(&$event,$begin_date,$uid);
    }
  }

  function add_event(&$event,$begin_date,$uid) {
    $date = date("YmdHis",$begin_date);
    if (!isset($this->events[$date.".".$event->id])) {
      $this->events[$date.".".$event->id] = array("event"=>&$event,"date"=>$begin_date,"users"=>array($uid));
      ksort($this->events);
    } else {
      $this->events[$date.".".$event->id]["users"][] = $uid;
    }
  }
  
  function add_day_event(&$event,$uid) {
    $this->day_events[$uid][] = &$event;
  }

  function get_events($uid) {
    foreach($this->events as $event) {
      if (!in_array($uid,$event["users"])) {
	continue;
      } else {
	$return[] = $event;
      }
    }
    return $return;
  }
 
  function have_events_between($start,$end) {
    if (count($this->day_events) > 0) {
      return true;
    }
    foreach($this->events as $event) {
      $ev_start = $event["date"];
      $ev_end = $event["event"]->duration + $ev_start;
      if ($ev_start < $end && $ev_end > $start) {
	return true;
      }
    }
    return false;
  }

  function user_have_events_between($start,$end,$uid) {
    if (count($this->day_events[$uid]) > 0) {
      return true;
    }
    foreach($this->events as $event) {
      $ev_start = $event["date"];
      $ev_end = $event["event"]->duration + $ev_start;
      if (!in_array($uid,$event["users"])) {
	continue;
      }
      if ($ev_start < $end && $ev_end > $start) {
	return true;
      }
    }
    return false;
  }
  
  function get_events_before($end, $uid,$start="") {
    if (count($this->events) > 0) {
      foreach($this->events as $event) {
      
	$ev_start = $event["date"];
	$ev_end = $event["event"]->duration + $ev_start;
          
	if ($ev_start > $end) {
	  break;
	}
	
	if (!in_array($uid,$event["users"])) {
	  continue;
	}
	if ($ev_start < $end) {
	  $return[] = $event;
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
    
  function get_events_between($start, $end, $uid) {
    if (count($this->events) > 0) {
      foreach($this->events as $event) {
      
	$ev_start = $event["date"];
	$ev_end = $event["event"]->duration + $ev_start;
	
	if ($ev_end < $start) {
	  continue;
	}
	
	if ($ev_start > $end) {
	  break;
	}
	
	if (!in_array($uid,$event["users"])) {
	  continue;
	}
      
	if ($ev_end > $start && $ev_start < $start) {
	  return -1;
	}
      
	if ($ev_start < $end && $ev_end > $start) {
	  $return[] = $event;
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


</script>
