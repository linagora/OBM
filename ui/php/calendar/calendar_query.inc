<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : calendar_query.inc                                           //
//     - Desc : Calendar query File                                          //
// 2001-06-27 : Mehdi Rande                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Return events details
// Parameters:
//   - $calendar_id
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_detail($calendar_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type; 
  $timeupdate = sql_date_format($db_type, "calendarevent_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "calendarevent_timecreate", "timecreate");
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date","calendarevent_date");
  $calendarevent_endrepeat = sql_date_format($db_type,"calendarevent_endrepeat", "calendarevent_endrepeat");
  $multidomain = sql_multidomain("calendarevent");
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
    WHERE calendarevent_id = '$calendar_id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_calendar_detail()");
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the name and first name of users
// Parameters: 
//   - $users : users id array, the event is assigned to 
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_get_user_name($users="") {
  global $cdg_sql;
  $multidomain = sql_multidomain("userobm");

  $in = "'".implode ("','", $users)."'";

  $obm_q = new DB_OBM;
  $query = "SELECT userobm_lastname, userobm_firstname, userobm_id
            FROM UserObm
	    WHERE userobm_id IN ($in) $multidomain";

  $query.= " ORDER BY userobm_id"; 
  display_debug_msg($query, $cdg_sql, "run_query_calendar_get_user_name()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the label of resources
// Parameters:
//   - $resources : resource id array, the event is assigned to
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_get_resource_label($resources) {
  global $cdg_sql;
  
  if (count($resources) > 0) {
    $in = "'".implode ("','", $resources)."'";
    $where = " resource_id IN ($in)";
    $and = true;
  }
  $multidomain = sql_multidomain("resource",$and);
  $and = false; 
  $obm_q = new DB_OBM;
  $query = "SELECT resource_name, resource_id
            FROM Resource 
            WHERE $where $multidomain";

  $query.= " ORDER BY resource_id";
  display_debug_msg($query, $cdg_sql, "run_query_calendar_get_resource_label()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the property of resources
// Parameters:
//   - $r_id : resource id 
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_get_resource_property($r_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("resource");
  $obm_q = new DB_OBM;
  $query = "SELECT resourcetype_property 
    FROM ResourceType 
    LEFT JOIN Resource ON resource_rtype_id=resourcetype_id
    WHERE resource_id='$r_id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_calendar_get_resource_propertyl()");
  $obm_q->query($query);
  $obm_q->next_record();
  $return = $obm_q->f("resourcetype_property");

  return $return;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the label of entities 
// Parameters:
//   - $entities : entity id array, the event is assigned to
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_get_entity_label($entities) {

  if ((is_array($entities["user"]))
      && (count($entities["user"]) > 0)) {
    $ret["user"] = run_query_calendar_get_user_name($entities["user"]);
  }
  if ((is_array($entities["resource"]))
      && (count($entities["resource"]) > 0)) {
    $ret["resource"] = run_query_calendar_get_resource_label($entities["resource"]);
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
function run_query_calendar_get_allusers($users_array,$groups_array) {

  $r_array = array();
  if (is_array($groups_array)) {
    foreach($groups_array as $group_id) {
      $r_array = array_merge($r_array, get_all_users_id_from_group($group_id));
    }
  }
  if(is_array($users_array)) {
    $r_array = array_merge($users_array,$r_array);
  }
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
    
    $multidomain = sql_multidomain("rgroup");
    
    $obm_q = new DB_OBM;
    $db_type = $obm_q->type;
    $query = "SELECT 
           resource_id,
           resource_name,
           rgroup_name, 
           resourcegroup_rgroup_id as resource_group
           FROM ResourceGroup LEFT JOIN Resource ON resourcegroup_resource_id=resource_id
           LEFT JOIN RGroup ON rgroup_id = resourcegroup_rgroup_id
        WHERE resourcegroup_rgroup_id IN (".implode(",",$group).") $multidomain";

    display_debug_msg($query, $cdg_sql, "run_query_resource_resourcegroup()");
    $obm_q->query($query);

    return $obm_q;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Insert a user decision for an user
// Parameters:
//   - $calendar : Calendar params
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_insert_decision($calendar) {
  $entity_id = $calendar["entity_id"];
  $entity = $calendar["entity_kind"];
  $event_id = $calendar["calendar_id"];
  $state = $calendar["decision_event"];

  run_query_calendar_update_occurence_state($event_id,$entity,$entity_id,$state);
}


///////////////////////////////////////////////////////////////////////////////
// Update a user decision for its participation to an event
// Parameters: 
//   - $event_id : event Id
//   - $user_id  : User id
//   - $state    : New state to set (A, W or R)
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_update_occurence_state($event_id, $entity, $entity_id,$state){
  global $cdg_sql;
  global $obm, $err, $l_of_right_err_auth;
  if($obm["uid"] != $entity_id || $entity != "user") {
    if( $entity == "user") {
      $writable = of_right_entity_for_consumer("calendar", "user", $obm["uid"], "write", "", "userobm");
    } elseif($entity == "resource") {
      $writable = of_right_entity_for_consumer("Resource", "user", $obm["uid"], "write");
    }
    
    if (!in_array($entity_id, $writable["ids"])) {
      $err["msg"] = $l_of_right_err_auth;  
      return false;
    } 
  }

  $query = "UPDATE EventEntity 
            SET evententity_state = '$state'
            WHERE evententity_entity_id = $entity_id
              AND evententity_entity = '$entity'
              AND evententity_event_id = '$event_id'
              ";

  display_debug_msg($query, $cdg_sql, "run_query_calendar_update_occurence_state()");  
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;    
}


///////////////////////////////////////////////////////////////////////////////
// Select All waiting Events
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_waiting_events() {
  global $obm, $cdg_sql;

  $uid = $obm["uid"];
  
  $multidomain = sql_multidomain("calendarevent");
  $writable_cal = of_right_entity_for_consumer("calendar", "user", $uid, "write", "", "userobm");
  $writable_res = of_right_entity_for_consumer("Resource", "user", $uid, "write");
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $calendarevent_endrepeat = sql_date_format($db_type,"calendarevent_endrepeat","calendarevent_endrepeat"); 
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date","calendarevent_date");    
  $query = "SELECT 
      u.userobm_lastname as usercreate_lastname,
      u.userobm_firstname as usercreate_firstname,
      entity_label, 
      entity_id,
      entity_entity,
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
      LEFT JOIN (
        SELECT CONCAT(userobm_lastname ,' ', userobm_firstname ) AS entity_label, userobm_id AS entity_id, 'user' AS entity_entity FROM UserObm 
        UNION SELECT resource_name AS entity_label, resource_id AS entity_id, 'resource' AS entity_entity FROM Resource
      ) AS Entity  ON evententity_entity_id = entity_id AND evententity_entity = entity_entity
      LEFT JOIN UserObm u ON calendarevent_owner = u.userobm_id
    WHERE 
      evententity_state = 'W'
      AND ( 1=0
      $multidomain";

  if (is_array($writable_cal)) {
    $query.= "OR ( entity_id IN ('".implode("','",$writable_cal["ids"])."') AND entity_entity = 'user')";    
  } 
  if (is_array($writable_res)) {
    $query.= "OR ( entity_id IN ('".implode("','",$writable_res["ids"])."') AND entity_entity = 'resource')";
  }
  $query .= " OR (entity_id = '$uid' AND entity_entity = 'user')) ORDER BY entity_entity, entity_label"; 
  display_debug_msg($query, $cdg_sql, "run_query_calendar_waiting_events()");
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Delete all participation to an event
// Parameters:
//   - $calendar[] : hash values for the event
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_delete_event_entity($calendar) {
  global $cdg_sql;

  $e_id = $calendar["calendar_id"];
  $query = "DELETE FROM EventEntity WHERE evententity_event_id = '$e_id'";

  display_debug_msg($query, $cdg_sql, "run_query_calendar_delete_event_entity()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
} 


///////////////////////////////////////////////////////////////////////////////
// Insert one event
// Parameters: 
//   - $calendar        : Calendar params
//   - $sel_entity_id : List entitis id ["user"] ["resource"]
//   - $event_id      : var to get the id to be returned
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_add_event($calendar, $sel_entity_id, &$event_id) {
  global $obm, $l_from, $l_to, $l_date;
  global $l_add_event_mail_head,$l_add_event_mail_subject;
  global $ccalendar_resource_admin, $l_resource_allocated;

  $writable_cal = of_right_entity_for_consumer("calendar", "user", $obm["uid"], "write", "", "userobm");
  $mail  = $calendar["mail"];
  $groups = $sel_entity_id["group"];
  $mail_title = stripslashes($calendar["title"]);
  $datebegin = $calendar["date_begin"];
  $dateend = $calendar["date_end"];
  if ($calendar["task_new_id"]) $task_id = $calendar["task_new_id"]; 
  else $task_id = $calendar["task_id"];
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
  $event_id = run_query_calendar_insert_event_data($calendar);   

  // Insert Event users
  foreach ($users_array as $user_id) {
    if ($user_id == $obm["uid"] || in_array($user_id, $writable_cal["ids"])) {
      run_query_calendar_insert_entity_event("user", $user_id, $event_id, 'A');
    } else {
      run_query_calendar_insert_entity_event("user", $user_id, $event_id, 'W');
    }
  }

  // Insert The event task
  if ($task_id > 0) {
    run_query_calendar_insert_entity_event("task", $task_id, $event_id, 'A');
  }

  // Insert Event resources
  $resources_writable = array(); // Writable Resources  
  $resources_address = array(); // Resource Admin notification
  // Get the final resource list and insert resource links
  $resources_array = $sel_entity_id["resource"];
  if ( (is_array($resources_array)) && (count($resources_array) > 0) ) {
    $resources_array = array_unique($resources_array);
    $writable_res = of_right_entity_for_consumer("Resource", "user", $obm["uid"], "write");
    foreach ($resources_array as $res_id) {
      if (in_array($res_id, $writable_res["ids"])) {
	run_query_calendar_insert_entity_event("resource", $res_id, $event_id, 'A');
      } else {
	run_query_calendar_insert_entity_event("resource", $res_id, $event_id, 'W');
      }
      array_push($resources_writable, $res_id);
    }
  }    
  if ((count($resources_writable) > 0) && ($ccalendar_resource_admin != "")) {
    $resources_address = array($ccalendar_resource_admin);
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
    $recipient_array = array_diff($users_array, array($obm["uid"]));
  } else {
    $recipient_array = array();
  }
  
  if (count($recipient_array) > 0 || count($r_address) > 0) {
    $tmp_filename = secure_tmpname(".ics","ics_");
    create_calendar_ics_meeting_file($calendar, $event_id, $recipient_array, $resources_writable, $tmp_filename);
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
//   - $calendar : hashed calendar params
// Returns: Event id inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_insert_event_data($calendar) {
  global $cdg_sql, $obm;

  $now = date("Y-m-d H:i:s");
  $title = $calendar["title"];
  $category1_id = $calendar["category1"];
  $priority = $calendar["priority"];
  $description = $calendar["description"]."|<extended_desc>";
  $location = $calendar["location"];
  ($calendar["all_day"] != 1)?$all_day= '0':$all_day = '1';
  $datebegin = of_isodate_convert($calendar["date_begin"]);
  $dateend = of_isodate_convert($calendar["date_end"]);
  $event_duration = $calendar["event_duration"];
  if ($calendar["privacy"]!=1) $privacy = 0; else $privacy = 1; 
  $repeat_kind = $calendar["repeat_kind"];
  $repeat_days = $calendar["repeat_days"]; 
  $repeat_end = of_isodate_convert($calendar["repeat_end"]);
  $repeatfrequency = $calendar["repeatfrequency"];  
  $date_exception = $calendar["date_exception"];
  $item = $calendar["list-item"];
  $item_id = $calendar["list-item-id"];
  $res_name = $calendar["res-name"];
  $res_id = $calendar["res-id"];
  $property = $calendar["property"];
  $property_name = $calendar["property-name"];
  if (is_array($res_name)) {
    foreach($res_name as $key=>$res) {
      $description .= "<resource>
         <res_name>$res</res_name>
         <res_id>$res_id[$key]</res_id>";

      if (is_array($item) && isset($item[$key])) {
        $description .= "<item>$item[$key]</item>
          <item_id>$item_id[$key]</item_id>";
      }

      if (is_array($property_name) && isset($property_name[$key])) {
        $description .= "<property_name>$property_name[$key]</property_name>
                         <property_value>$property[$key]</property_value>";                       
      }
      $description .= "</resource>";
    }
  }
  $description .= "</extended_desc>";

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
    calendarevent_endrepeat,
    calendarevent_domain_id)
  VALUES (
    '$now',
    '$now',
    '$obm[uid]',
    '$obm[uid]',
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
    '$repeat_end',
    '$obm[domain_id]')";

  display_debug_msg($query, $cdg_sql, "run_query_calendar_insert_event_data()");
  $obm_q->query($query);
  
  $query = "SELECT MAX(calendarevent_id) as max_id FROM CalendarEvent";
  display_debug_msg($query, $cdg_sql, "run_query_calendar_insert_event_data()");
  $obm_q->query($query);
  $obm_q->next_record(); 
  $max_id = $obm_q->f("max_id");

  // Exception
  if($repeat_kind != "none" && is_array($date_exception)) {
    foreach(array_unique($date_exception) as $value) {
      if($value != "") {
        $value = of_isodate_convert($value);
        $calendar["old_date_begin"] = $value." ".$calendar["time_begin"].":".$calendar["min_begin"].":00";
        run_query_calendar_event_exception_insert($calendar); 
      }
    }
  }

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
function run_query_calendar_insert_entity_event($entity, $entity_id,$event_id, $state) {
  global $cdg_sql, $obm;

  $uid = $obm["uid"];
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
    '$uid',
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
//   - $calendar : calendar hash values
///////////////////////////////////////////////////////////////////////////////
function create_calendar_ics_meeting_file($calendar, $event_id, $attendees, $resources, $filename) {
  global $obm, $obm_version,$ccalendar_weekstart;
  global $l_file_error, $l_write_error, $l_read_error;
  global $l_resource_allocated;

  $fd_dest = fopen ($filename,"w");
  if ($fd_dest == false) {
    die ($l_file_error.$dest."dead");
  }

  $iso_date = $calendar["date"];
  $ts_date = strtotime($iso_date);  
 
  $start_time = strtotime("-1 year", $ts_date);
  $end_time = strtotime("+1 year", $ts_date);

  $calendar_user = array ($obm["uid"] => "dummy");  
  $obm_event = run_query_calendar_detail($event_id);

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
      $start_week_day = strtotime($ccalendar_weekstart);
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
//   - $calendar       : calendar parameters
//   - sel_entity_id : selected entities id
///////////////////////////////////////////////////////////////////////////////
function check_calendar_conflict($calendar, $sel_entity_id) {

  $ts_begin = isodatetime_to_timestamp($calendar["date_begin"]);
  $ts_end = isodatetime_to_timestamp($calendar["date_end"]);
  $id = $calendar["calendar_id"];
  $obm_q = run_query_calendar_conflict_events($ts_begin, $ts_end, $sel_entity_id, $id);
  if ($obm_q->nf() == 0) {
    return false;
  }
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Check for conflict : Only with the first occurence of each event
///////////////////////////////////////////////////////////////////////////////
function check_calendar_decision_conflict($calendar) {
  
  $user["user"] = $calendar["user_id"];
  $event_id = $calendar["calendar_id"];
  $e_q = run_query_calendar_detail($event_id);
  $ts_begin = $e_q->f("calendarevent_date");
  $ts_end = $e_q->f("calendarevent_date") + $e_q->f("calendarevent_duration");
  $c_q = run_query_calendar_conflict_events($ts_begin,$ts_end,$user,$event_id);
  if ($c_q->nf() == 0) {
    return false;
  }
  return $c_q;
}

///////////////////////////////////////////////////////////////////////////////
// Event Update Execution
// Parameters: 
//   - $calendar      : Calendar params
//   - $sel_entity_id : List entitis id ["user"] ["resource"]
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_event_update($calendar, $sel_entity_id, &$event_id) {
  global $c_use_connectors, $obm, $l_date;
  global $l_update_event_mail_head,$l_update_event_mail_subject,$l_update_event_mail_body,$l_to,$l_from;
  global $ccalendar_resource_admin, $l_resource_allocated;

  $datebegin = $calendar["date_begin"];
  $dateend = $calendar["date_end"];
  $event_id = $calendar["calendar_id"];
  $groups = $calendar["group"];
  $mail = $calendar["mail"];
  $mail_title = stripslashes($calendar["title"]);
  $writable_cal = of_right_entity_for_consumer("calendar", "user", $obm["uid"], "write", "", "userobm");

  if($calendar["task_new_id"]) $task_id = $calendar["task_new_id"]; 
  else $task_id = $calendar["task_id"];

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
    $writable_res = of_right_entity_for_consumer("Resource", "user", $obm["uid"], "write");
  }    

  $event_data = run_query_calendar_detail($event_id);
  $old_begin = of_isodate_format($event_data->f("calendarevent_date"),"", 1);
  $old_end = of_isodate_format($event_data->f("calendarevent_date") + $event_data->f("calendarevent_duration"),"",1);

  // Handle Connectors data
  if ($c_use_connectors) {
    $obm_q = new DB_OBM;
    $now = date("Y-m-d H:i:s");
    $old_users = get_calendar_event_users_info($event_id);
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
	display_debug_msg($query, $cdg_sql, "run_query_calendar_event_update(1)");
	$retour = $obm_q->query($query);
      }
    }

    // If users have been inserted they must not appear in DeletedCalendarEvent
    if (count($new_users) > 0) {
      foreach($new_users as $u_id) {
	$query = "DELETE FROM DeletedCalendarEvent
      WHERE deletedcalendarevent_event_id = '$event_id'
        AND deletedcalendarevent_user_id = '$u_id'";
	display_debug_msg($query, $cdg_sql, "run_query_calendar_event_update(2)");
	$retour = $obm_q->query($query);
      }
    }
  }

  // XXXX bizarre les old_end et old_begin semblent etre affectes par l'update
  // ci-dessous sous mysql 4.0.23-10 (portable fourmi)
  run_query_calendar_update_event_data($calendar);  
  run_query_calendar_delete_event_entity($calendar);

  // User insertion
  foreach($users_array as $user_id) {
    if ($user_id == $obm["uid"] || in_array($user_id, $writable_cal["ids"])) {
      run_query_calendar_insert_entity_event("user", $user_id, $event_id, 'A');	
    } else {
      run_query_calendar_insert_entity_event("user", $user_id, $event_id, 'W');	
    }
  }

  // Resource insertion
  $resources_writable=array(); // Writable Resources
  $resources_address =array(); // Resource Admin notification
  if (is_array($resources_array)) {
    foreach ($resources_array as $res_id) {
      if (in_array($res_id, $writable_res["ids"])) {
	run_query_calendar_insert_entity_event("resource", $res_id, $event_id, 'A');
      } else {
	run_query_calendar_insert_entity_event("resource", $res_id, $event_id, 'W');
      }
      array_push($resources_writable, $res_id);
    }
  }
  if ((count($resources_writable) > 0) && ($ccalendar_resource_admin != "")) {
    $resources_address = array($ccalendar_resource_admin);
    $message_resource = "\r\n\r\n$l_resource_allocated:";
    $resource_q = run_query_get_resources_info($resources_writable);
    while ($resource_q->next_record()) {
      $message_resource .=  "\n  ".$resource_q->f("resource_name");
    }
  }

  if($task_id > 0) {
    run_query_calendar_insert_entity_event("task", $task_id, $event_id, 'A');
  }

  $subject = "$l_update_event_mail_subject" . $mail_title;
  $message = "$l_update_event_mail_head $mail_title
$l_date : $l_from $datebegin $l_to $dateend";
  if (($old_begin != $datebegin || $old_end != $dateend) ) {
    $message .= "
$l_update_event_mail_body $l_from $old_begin $l_to $old_end";
  }
  $message .= $message_resource; 

  $recipient_array = array_diff($users_array, array($obm["uid"]));
  send_mail($subject, $message, $recipient_array, $resources_address, $mail);
}


///////////////////////////////////////////////////////////////////////////////
// Event Data Update Execution
// Parameters: 
//   - $calendar : Calendar params
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_update_event_data($calendar) {
  global $cdg_sql, $obm;

  $now = date("Y-m-d H:i:s");
  $title = $calendar["title"];
  $category1_id = $calendar["category1"];
  $priority = $calendar["priority"];
  $description = $calendar["description"]."|<extended_desc>";
  $location = $calendar["location"];
  $datebegin = of_isodate_convert($calendar["date_begin"]);
  $event_duration = $calendar["event_duration"];
  ($calendar["all_day"] != 1)?$all_day= '0':$all_day = '1';
  $event_id = $calendar["calendar_id"]; 
  if ($calendar["privacy"] != 1) $privacy = 0; else $privacy = 1; 
  $repeat_kind = $calendar["repeat_kind"];
  $repeat_days = $calendar["repeat_days"];
  $repeat_end = of_isodate_convert($calendar["repeat_end"]);
  $frequency = $calendar["repeatfrequency"];  
  $date_exception = $calendar["date_exception"];
  $item = $calendar["list-item"];
  $item_id = $calendar["list-item-id"];
  $res_name = $calendar["res-name"];
  $res_id = $calendar["res-id"];
  $property = $calendar["property"];
  $property_name = $calendar["property-name"];
  if (is_array($res_name)) {
    foreach($res_name as $key=>$res) {
      $description .= "<resource>
         <res_name>$res</res_name>
         <res_id>$res_id[$key]</res_id>";

      if (is_array($item) && isset($item[$key])) {
        $description .= "<item>$item[$key]</item>
          <item_id>$item_id[$key]</item_id>";
      }

      if (is_array($property_name) && isset($property_name[$key])) {
        $description .= "<property_name>$property_name[$key]</property_name>
                         <property_value>$property[$key]</property_value>";                       
      }
      $description .= "</resource>";
    }
  }
  $description .= "</extended_desc>";

  $obm_q = new DB_OBM;
  $query = "UPDATE CalendarEvent SET
    calendarevent_timeupdate = '$now',
    calendarevent_userupdate = '$obm[uid]', 
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

  display_debug_msg($query, $cdg_sql, "run_query_calendar_update_event_data()");
  $obm_q->query($query);

  $query = "DELETE FROM CalendarException WHERE calendarexception_event_id = '$event_id'";
  display_debug_msg($query, $cdg_sql, "run_query_calendar_update_event_data(2)");
  $obm_q->query($query);

  if($repeat_kind != "none" && is_array($date_exception)) {
    foreach(array_unique($date_exception) as $key => $value) {
      if($value != "") {
        $value = of_isodate_convert($value);
        $calendar["old_date_begin"] = $value." ".$calendar["time_begin"].":".$calendar["min_begin"].":00";
        run_query_calendar_event_exception_insert($calendar); 
      }
    }
  }
}


/**
 * Event Data Update Execution for ajax client 
 * 
 * @param mixed $calendar 
 * @access public
 * @return void
 */
function run_query_calendar_quick_event_update($calendar) {
  global $cdg_sql, $obm;

  $multidomain = sql_multidomain("calendarevent");
  $now = date("Y-m-d H:i:s");
  $title = $calendar["title"];
  $datebegin = $calendar["date_begin"];
  $event_duration = $calendar["duration"];
  $event_id = $calendar["calendar_id"]; 
  $obm_q = new DB_OBM;
  $query = "UPDATE CalendarEvent SET
    calendarevent_timeupdate = '$now',
    calendarevent_userupdate = '$obm[uid]', 
    calendarevent_title = '$title', 
    calendarevent_date = '$datebegin',     
    calendarevent_duration = '$event_duration'
  WHERE
  calendarevent_id = '$event_id' $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_calendar_update_event_data()");
  $obm_q->query($query);  
}


/**
 * Event Data  Execution for ajax client 
 * 
 * @param mixed $calendar 
 * @access public
 * @return void
 */
function run_query_calendar_quick_event_insert($calendar, $sel_entity_id) {
  global $cdg_sql, $obm;

  $writable_cal = of_right_entity_for_consumer("calendar", "user", $obm["uid"], "write", "", "userobm");
  $now = date("Y-m-d H:i:s");
  $title = $calendar["title"];
  $datebegin = $calendar["date_begin"];
  $event_duration = $calendar["duration"];
  $all_day = $calendar["all_day"];

  $obm_q = new DB_OBM;
  $query = "INSERT INTO CalendarEvent
    (calendarevent_timecreate,
     calendarevent_usercreate,
     calendarevent_owner,
     calendarevent_title,
     calendarevent_date,
     calendarevent_duration,
     calendarevent_allday,
     calendarevent_description,
     calendarevent_privacy,
     calendarevent_repeatkind,
     calendarevent_repeatfrequence,
     calendarevent_endrepeat,
     calendarevent_repeatdays,
     calendarevent_location, 
     calendarevent_category1_id,
     calendarevent_priority,
     calendarevent_domain_id
   )
  VALUES (
    '$now',
    '$obm[uid]', 
    '$obm[uid]', 
    '$title', 
    '$datebegin',     
    '$event_duration',
    '$all_day',
    '',
    '0',
    'none',
    '1',
    'NOW()',
    '',
    '',
    NULL,
    '2',
    '$obm[domain_id]'
  )";
  display_debug_msg($query, $cdg_sql, "run_query_calendar_quick_insert_event_data()");
  $obm_q->query($query);
  $query = "SELECT MAX(calendarevent_id) as max_id FROM CalendarEvent";
  display_debug_msg($query, $cdg_sql, "run_query_calendar_insert_event_data()");
  $obm_q->query($query);
  $obm_q->next_record(); 
  $event_id = $obm_q->f("max_id"); 

  run_query_calendar_insert_entity_event("user", $obm["uid"], $event_id, 'A');

  return $event_id;
}


/**
 * Create an exception for an event 
 * 
 * @param mixed $event_q 
 * @param mixed $calendar 
 * @access public
 * @return void
 */
function run_query_calendar_event_exception_insert( $calendar, $event_q = "") {
  global $cdg_sql, $obm;

  $now = date("Y-m-d H:i:s");
  $title = $calendar["title"];
  $id = $calendar["calendar_id"];
  $olddatebegin = $calendar["old_date_begin"];
  $event_duration = $calendar["duration"];
  
  $obm_q = new DB_OBM;
  $query = "INSERT INTO CalendarException (
      calendarexception_usercreate,
      calendarexception_timecreate ,   
      calendarexception_userupdate,
      calendarexception_timeupdate,
      calendarexception_event_id,
      calendarexception_date)
      VALUES (
        '$obm[uid]',
        '$now',
        '$obm[uid]',
        '$now',
        '$id',
        '$olddatebegin'
      )";

  display_debug_msg($query, $cdg_sql, "run_query_calendar_event_exception_insert(1)");
  $obm_q->query($query);
  $query = "Update CalendarEvent SET
    calendarevent_userupdate = '$obm[uid]',
    calendarevent_timeupdate = NOW()
    WHERE
    calendarevent_id = '$id'";

  display_debug_msg($query, $cdg_sql, "run_query_calendar_event_exception_insert(2)");
  $obm_q->query($query);

  if(is_object($event_q)) {
    if ($event_q->f("calendarevent_privacy")!=1) $privacy = 0; else $privacy = 1; 
    $repeat_kind = $event_q->f("calendarevent_repeatkind");
    $repeat_days = $event_q->f("calendarevent_repeatdays");
    $repeat_end = $event_q->f("calendarevent_endrepeat");
    $repeatfrequency = $event_q->f("calendarevent_repeatfrequence");  
    $category1_id = $event_q->f("calendarevent_category1_id");
    $priority = $event_q->f("calendarevent_priority");
    $description = $event_q->f("calendarevent_description");
    $location = $event_q->f("calendarevent_location");
    ($calendar["all_day"] != 1)?$all_day= '0':$all_day = '1';
    $datebegin = $calendar["date_begin"];

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
      calendarevent_endrepeat,
      calendarevent_domain_id
    )
    VALUES (
      '$now',
      '$now',
      '$obm[uid]',
      '$obm[uid]',
      '$title',
      '$datebegin',
      '$description',
      '$location',
      '$category1_id',
      '$priority',
      '$privacy',
      '$event_duration',		
      'none',
      '$repeatfrequency',
      '$repeat_days',
      '$all_day',
      '$repeat_end',
      '$obm[domain_id]'
    )";

    display_debug_msg($query, $cdg_sql, "run_query_calendar_insert_event_data(3)");
    $obm_q->query($query);
    
    $query = "SELECT MAX(calendarevent_id) as max_id FROM CalendarEvent";
    display_debug_msg($query, $cdg_sql, "run_query_calendar_insert_event_data()");
    $obm_q->query($query);
    $obm_q->next_record(); 
    $new_id = $obm_q->f("max_id");

    $entities = get_calendar_event_entity($id);
    foreach ($entities as $entity => $data) {
      foreach ($data["entity"] as $entity_id => $entity_data) {  
        run_query_calendar_insert_entity_event($entity, $entity_id, $new_id, $entity_data['state']);
      }
    }
  }

  return $new_id;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the event can be deleted
// Parameters:
//   - $id : event id
// Returns:
//   true if the event can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_calendar_can_delete($id) {
  global $ok_msg;

  $delete_ok = true;

  return $delete_ok;
}


///////////////////////////////////////////////////////////////////////////////
// Delete all events of a evenements, and the event
// Parameters: 
//   - $calendar : calendar infos hash
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_delete($calendar) {
  global $cdg_sql, $c_use_connectors, $l_to, $l_from;
  global $l_delete_event_mail_head,$l_delete_event_mail_subject,$l_delete_event_mail_body;
  global $ccalendar_resource_admin, $l_resource_allocated;

  $obm_q = new DB_OBM;
  $event_id = $calendar["calendar_id"];
  $mail = $calendar["mail"]; 
  
  $users_array = get_calendar_event_users_info($event_id);

  $multidomain = sql_multidomain("calendarevent");
  $query = "SELECT calendarevent_title
    FROM CalendarEvent
    WHERE calendarevent_id = '$event_id' $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_calendar_delete(1)");
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
  if (count($resources) > 0 && ($ccalendar_resource_admin != "")) {
    $resources_address = array($ccalendar_resource_admin);
    $message_resource = "\r\n\r\n$l_resource_allocated:";
    $resource_q = run_query_get_resources_info($resources);
    while ($resource_q->next_record()) {
      $message_resource .=  "\n  ".$resource_q->f("resource_name");
    }
  }

  $query = "DELETE FROM CalendarException WHERE calendarexception_event_id = '$event_id'";
  display_debug_msg($query, $cdg_sql, "run_query_calendar_delete(2)");
  $obm_q->query($query);
  $query = "DELETE FROM EventEntity WHERE evententity_event_id = '$event_id'";
  display_debug_msg($query, $cdg_sql, "run_query_calendar_delete(3)");
  $obm_q->query($query);
  $query = "DELETE FROM CalendarEvent WHERE calendarevent_id = '$event_id'";
  display_debug_msg($query, $cdg_sql, "run_query_calendar_delete(4)");
  $obm_q->query($query);

  // If connectors in use
  if ($c_use_connectors) {
    $now = date("Y-m-d H:i:s");
    
    // We purge the individual user entries
    $query = "DELETE FROM DeletedCalendarEvent
      WHERE deletedcalendarevent_event_id = '$event_id'";
    display_debug_msg($query, $cdg_sql, "run_query_calendar_delete()");
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
    display_debug_msg($query, $cdg_sql, "run_query_calendar_delete()");
    $retour = $obm_q->query($query);
  }

  $message = $l_delete_event_mail_head.$title.$message_resource;   
  $subject = "$l_delete_event_mail_subject" . $title;
  $recipient_array = array_diff($users_array, array($obm["uid"]));
  send_mail($subject, $message, $recipient_array, $resources_address, $mail);
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable calendar entities for the current user:
//   - user
//   - users group
//   - resource
//   - resources group
///////////////////////////////////////////////////////////////////////////////
function get_calendar_entity_readable() {

  $ent = array (
    "user" => get_calendar_user_readable(),
    "group" => get_calendar_group_readable(),
    "resource" => get_calendar_resource_readable()
  );

  return $ent;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable calendar users for the current user
///////////////////////////////////////////////////////////////////////////////
function get_calendar_user_readable() {
  global $cdg_sql, $obm, $calendar, $c_all;

  $uid = $obm["uid"];
  $gusers = "";
  if ( (isset($calendar["entity"]["group_view"]))
       && ($calendar["entity"]["group_view"] != $c_all) ) {
    $gusers = get_all_users_id_from_group($calendar["entity"]["group_view"]);
  }
  $entity = of_right_entity_for_consumer("calendar", "user", $uid, "read", $gusers, "userobm");

  // Add the user own calendar
  $entity["ids"][] = $uid;
  $obm_q = run_query_calendar_get_user_name(array($uid));
  $obm_q->next_record();
  $id = $obm_q->f("userobm_id");
  $label = $obm_q->f("userobm_lastname") ." ". $obm_q->f("userobm_firstname");
  $entity["entity"][] = array("id" => $id, "label" => $label);

  return $entity;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable calendar group for the current user
///////////////////////////////////////////////////////////////////////////////
function get_calendar_group_readable() {
  global $cdg_sql, $obm, $ccalendar_public_groups;

  $uid = $obm["uid"];
  $where = sql_obm_entity_privacy("group");
  if (! $ccalendar_public_groups) {
    $where .= " AND group_privacy=1";
  }

  $multidomain = sql_multidomain("group");
  $query = "
    SELECT group_id, group_privacy, group_name
    FROM UGroup
    WHERE $where $multidomain
    ORDER BY group_privacy DESC, group_name";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "get_calendar_group_readable()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable calendar group for the current user
///////////////////////////////////////////////////////////////////////////////
function get_calendar_resource_readable() {
  global $cdg_sql, $obm, $ccalendar_public_groups;

  $uid = $obm["uid"];
  $entity = of_right_entity_for_consumer("Resource", "user", $uid, "read");

  return $entity;
}


///////////////////////////////////////////////////////////////////////////////
// Get resource infos from the resources id array given
// Parameters:
//   - $resources : array of resources id
// Returns:
// array [ids], [entity] (label=>)
///////////////////////////////////////////////////////////////////////////////
function get_calendar_resource_from_ids($resources) {
  global $cdg_sql;

  if (is_array($resources)) {
    $where = "(0";
    foreach($resources as $resource) {
      $where .= ", $resource";
    }
    $multidomain = sql_multidomain("resource");
    $where .= ")";
    $query = "SELECT *
    FROM Resource 
    WHERE resource_id IN $where $multidomain
    ORDER by resource_name";
    display_debug_msg($query, $cdg_sql, "get_calendar_resource_from_ids()");
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
function get_calendar_group_from_ids($groups) {
  global $cdg_sql;

  $multidomain = sql_multidomain("group");
  if (is_array($groups)) {
    $where = "(0";
    foreach($groups as $group) {
      $where .= ", $group";
    }
    $where .= ")";
    $query = "SELECT *
    FROM UGroup
    WHERE 
      group_id IN $where $multidomain
    ORDER by group_name";
    display_debug_msg($query, $cdg_sql, "get_calendar_group_from_ids()");
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
function store_calendar_entities($entities_q) {
  global $ico_calendar_user0,$ico_calendar_user1,$ico_calendar_user2;
  global $ico_calendar_user3,$ico_calendar_user4,$ico_calendar_user5;
  global $display, $obm;

  $i = 0;
  $entity_tab = array();
  $user_q = $entities_q["user"];
  if (is_object($user_q)) {
    while ($user_q->next_record()) {
      $id = $user_q->f("userobm_id");
      $name = $user_q->f("userobm_firstname")." ".$user_q->f("userobm_lastname");
      if ($obm["uid"] == $id) {
      $entity_tab["user"][$id] = array(
        "name"  => $name,
        "class" => "eventOwner",
        "type"  => "user",
        "id"    => $user_q->f("userobm_id"));
      } else {
        $entity_tab["user"][$id] = array(
          "name"  => $name,
          "class" => "eventOwner$i",
          "type"  => "user",
          "id"    => $user_q->f("userobm_id"));
        $i++;
        $i = $i%6;
      }
    }
  }
  $resource_q = $entities_q["resource"];
  if (is_object($resource_q)) {
    while($resource_q->next_record()) {
      $id = $resource_q->f("resource_id");
      $entity_tab["resource"][$id] = array(
        "name"  => $resource_q->f("resource_name"),
        "class" => "eventOwner$i",
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
        "class" => "calendarEventBg$i",
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
function get_calendar_event_info($id) {
  global $cdg_sql;

  if ($id == "") {
    return false;
  }

  $multidomain = sql_multidomain("calendarevent");
  $query = "SELECT * FROM CalendarEvent WHERE calendarevent_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "get_calendar_event_info()");
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
function get_calendar_event_users_info($id) {
  global $cdg_sql;

  $users_array = array();

  if ($id == "") {
    return $users_array;
  }

  $query = "SELECT evententity_entity_id
    FROM EventEntity
    WHERE evententity_event_id = '$id' AND evententity_entity = 'user'";

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
//   - $calendar_id
///////////////////////////////////////////////////////////////////////////////
function get_calendar_event_entity($calendar_id) {
  global $cdg_sql;

  // XXXX Optimisation possible : 1 seule requete pour toutes les entites
  $multidomain = sql_multidomain("userobm");

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
    evententity_event_id = '$calendar_id' AND 
    evententity_entity = 'user'
    $multidomain";

  display_debug_msg($query, $cdg_sql, "get_calendar_event_entity(1)");
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
  $multidomain = sql_multidomain("resource");

  $query = "SELECT DISTINCT
    resource_id,
    resource_name,
    evententity_entity,
    evententity_entity_id,
    evententity_state as state
  FROM EventEntity
    LEFT JOIN Resource ON evententity_entity_id = resource_id
  WHERE 
    evententity_event_id = '$calendar_id'
    AND evententity_entity = 'resource'
    $multidomain";

  display_debug_msg($query, $cdg_sql, "get_calendar_event_entity(2)");
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
  $multidomain = sql_multidomain("project");

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
    evententity_event_id = '$calendar_id'
    AND evententity_entity = 'task'
    $multidomain";

  display_debug_msg($query, $cdg_sql, "get_calendar_event_entity(2)");
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
// Calendar Quick Form Data checking and formatting
// Parameters:
//   - $calendar[] : values checked
//     keys used  : num, name, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_calendar_data_quick_form($calendar) {
  global $err, $php_regexp_isodate, $php_regexp_isodatetime;
  global $l_fill_title, $l_fill_dateend,$l_fill_datebegin,$l_err_begin_end;

  $title = $calendar["title"];
  $datebegin = $calendar["date_begin"];
  $duration = $calendar["duration"];
  $all_day = $calendar["all_day"];
  if (trim($title) == "") {
    $err["msg"] = $l_fill_title;
    return false;
  }
  
  // Begin Date check
  if (trim($datebegin) == "") {
    $err["msg"] = $l_fill_datebegin;
    return false;
  } else if (preg_match($php_regexp_isodatetime, $datebegin) == 0) {
    $err["msg"] = "$l_datebegin : $l_invalid_date"; 
    return false;
  }
  
  // End Date check
  if((!is_numeric($duration) ||  $duration < 1) && !$all_day) {
    $err["msg"] = $l_err_begin_end;
    return false;
  }
  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Calendar Form Data checking and formatting
// Parameters:
//   - $calendar[] : values checked
//     keys used  : num, name, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_calendar_data_form($calendar) {
  global $err, $php_regexp_isodate, $php_regexp_isodatetime;
  global $l_fill_title, $l_fill_dateend,$l_fill_datebegin,$l_err_weekly_repeat;
  global $l_invalid_date, $l_datebegin, $l_dateend, $l_date_repeatend;
  global $l_err_begin_end, $l_err_end_repeat,$l_err_end_repeat2,$l_err_repeatfrequency;
  global $l_err_end_repeat3, $l_err_days_repeat, $l_err_days_repeat_not_weekly;
  
  $title = $calendar["title"];
  $datebegin = $calendar["date_begin"];
  $dateend = $calendar["date_end"];
  $repeat_end = $calendar["repeat_end"];
  $kind = $calendar["repeat_kind"];
  $repeat_days = $calendar["repeat_days"];
  $repeatfrequency = $calendar["repeatfrequency"];
  ($calendar["all_day"] != 1)?$all_day= '0':$all_day = '1';

  if (trim($title) == "") {
    $err["msg"] = $l_fill_title;
    return false;
  }
  
  // Begin Date check
  if (trim($datebegin) == "") {
    $err["msg"] = $l_fill_datebegin;
    return false;
  } else if (preg_match($php_regexp_isodatetime, $datebegin) == 0) {
    $err["msg"] = "$l_datebegin : $l_invalid_date"; 
    return false;
  }
  
  // End Date check
  if (trim($dateend) == "") {
    $err["msg"] = $l_fill_dateend;
    return false;
  } else if (preg_match($php_regexp_isodatetime, $dateend) == 0) {
    $err["msg"] = "$l_dateend : $l_invalid_date"; 
    return false;
  }
  
  // Repeatition check
  if ($kind != "none") {
    // End repeat Date check
    if (($repeat_end != "") && (preg_match($php_regexp_isodate, $repeat_end) == 0)) {
      $err["msg"] = "$l_date_repeatend : $l_invalid_date"; 
      return false;
    }
  }
  
  if (!is_numeric($repeatfrequency) ||  $repeatfrequency < 1) {
    $err["msg"] = $l_err_repeatfrequency;
    return false;
  }

  if ($dateend < $datebegin || ( $all_day !=  1 && $dateend == $datebegin)) {
    $err["msg"] = $l_err_begin_end;
    return false;
  } 
  
  if (trim($repeat_end) != "" && $dateend>$repeat_end && $kind != "none") {
    $err["msg"] =  $l_err_end_repeat;
    return false;
  }
  
  // If repeat kind is weekly, repeat days must be set
  if ($kind == "weekly" && $repeat_days == "0000000") {
    $err["msg"] = $l_err_days_repeat;
    return false;
  }

  // If repeat days are set, repeat kind must be weekly
  if ($kind != "weekly" && $repeat_days != "0000000") {
    $err["msg"] = $l_err_days_repeat_not_weekly;
    return false;
  }

  if ($kind == "weekly" && strtotime("+ 1 week",strtotime($dateend)) > strtotime($repeat_end)) {
    $err["msg"] = $l_err_weekly_repeat;
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
function calendar_localizeDate($format, $timestamp) {
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
function get_calendar_date_day_of_week($timestamp, $day) {
  global $ccalendar_weekstart;

  if (!isset($ccalendar_weekstart)) $ccalendar_weekstart = 'Sunday';
  $num = date('w', strtotime($ccalendar_weekstart));
  $start_day_time = strtotime((date('w',$timestamp)==$num ? "$ccalendar_weekstart" : "last $ccalendar_weekstart"), $timestamp);
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
//   - $nb_max          : nb max entities to display
//   - $entity_readable : if given, entities eligibles
///////////////////////////////////////////////////////////////////////////////
function slice_calendar_entities($sel_entity_id, $nb_max, $entity_readable="") {
  global $obm;

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
  if (count($res_user_id) > $nb_max) {
    $res_user_id = array_slice ($res_user_id, 0, $nb_max);
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
  $max_res = $nb_max - $nb_user;
  $nb_res = count($res_resource_id);
  if ($nb_res > $max_res) {
    $res_resource_id = array_slice ($res_resource_id, 0, $max_res);
  }
  $nb_res = count($res_resource_id);
  if (($nb_user == 0) && ($nb_res == 0)) {
    $res_user_id = array($obm["uid"]);
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
function get_calendar_week_num($date) {
  global $ccalendar_weekstart;

  $num = date('w', strtotime($ccalendar_weekstart));
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
//   - $calendar       : calendar params
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_conflict_events($start,$end,$sel_entity_id,$event_id) {
  global $cdg_sql;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date");
  $calendarevent_date_l = sql_date_format($db_type,"calendarevent_date","calendarevent_date");

  // get all users (from groups too)
  $users = run_query_calendar_get_allusers($sel_entity_id["user"], $sel_entity_id["group"]);

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
  $multidomain = sql_multidomain("calendarevent");

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
      $multidomain
    ORDER BY calendarevent_date";

  display_debug_msg($query, $cdg_sql, "run_query_calendar_conflict_events(()");
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
function run_query_calendar_no_repeat_events($start,$end, $calendar_entity,$category_filter) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date");
  $calendarevent_date_l = sql_date_format($db_type,"calendarevent_date","calendarevent_date");
  

  $multidomain = sql_multidomain("calendarevent");
  if($category_filter != "") {
    $filter = " AND calendarcategory1_id = '$category_filter' ";
  }  
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
      calendarevent_owner,
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
      $filter
      AND calendarevent_repeatkind = 'none'
      AND $calendarevent_date >= $start - calendarevent_duration
      AND $calendarevent_date <= $end
      AND $where_grp
      $multidomain";
  }
  
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
      calendarcategory1_label,
      $calendarevent_date_l,
      calendarevent_duration,
      calendarevent_allday
    FROM CalendarEvent
      LEFT JOIN CalendarCategory1
        ON calendarevent_category1_id = calendarcategory1_id
      LEFT JOIN EventEntity ON calendarevent_id = evententity_event_id
    WHERE evententity_state = 'A'
      $filter
      AND calendarevent_repeatkind = 'none'
      AND $calendarevent_date >= $start - calendarevent_duration
      AND $calendarevent_date <= $end
      $where_ent
      $multidomain
      $union
      ORDER BY calendarevent_date
";

  display_debug_msg($query, $cdg_sql, "run_query_calendar_no_repeat_events()");
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a week of users or/and groups
// Parameters:
//   - $calendar          : calendar params
//   - $calendar_entity : array of entities elements to display
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_repeat_events($start, $end, $calendar_entity, $category_filter) {
  global $cdg_sql;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $calendarevent_endrepeat = sql_date_format($db_type,"calendarevent_endrepeat");
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date");
  $calendarevent_date_l = sql_date_format($db_type,"calendarevent_date","calendarevent_date");
  $calendarevent_endrepeat_l = sql_date_format($db_type,"calendarevent_endrepeat","calendarevent_endrepeat");
  $multidomain = sql_multidomain("calendarevent");
  if($category_filter != "") {
    $filter = " AND calendarcategory1_id = '$category_filter' ";
  }
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
      calendarevent_owner,
      'resourcegroup' as evententity_entity,
      evententity_entity_id,
      calendarevent_repeatdays,
      calendarevent_allday
    FROM CalendarEvent
      LEFT JOIN CalendarCategory1
        ON calendarevent_category1_id = calendarcategory1_id
      LEFT JOIN EventEntity ON calendarevent_id = evententity_event_id
   WHERE calendarevent_repeatkind != 'none'
      $filter
      AND $calendarevent_date <= $end 
      AND ($calendarevent_endrepeat >= $start - calendarevent_duration
      OR $calendarevent_endrepeat = '0')
      AND $where_grp
      $multidomain
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
      calendarevent_owner,
      evententity_entity,
      evententity_entity_id,
      calendarevent_repeatdays,
      calendarevent_allday
    FROM CalendarEvent
      LEFT JOIN CalendarCategory1
        ON calendarevent_category1_id = calendarcategory1_id
      LEFT JOIN EventEntity ON calendarevent_id = evententity_event_id
    WHERE calendarevent_repeatkind != 'none'
      $filter
      AND evententity_state = 'A'
      AND $calendarevent_date <= $end 
      AND ($calendarevent_endrepeat >= $start - calendarevent_duration
      OR $calendarevent_endrepeat = '0')
      $where_ent
      $multidomain
      $union

    ORDER BY calendarevent_date"; 

  display_debug_msg($query, $cdg_sql, "run_query_calendar_repeat_events()");
  $obm_q->query($query);
  return $obm_q;
}


/**
 * Get all attendees of an array of events
 *  
 * @param array $events 
 * @access public
 * @return void
 */
function run_query_get_events_attendee($events) {
  global $cdg_sql;

  $event_list = implode(",",$events) ;
  if ($event_list == "") {
    $event_list = "0";
  }
  $obm_q = new DB_OBM;

  $multidomain = sql_multidomain("calendarevent");
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
   WHERE calendarevent_id IN ($event_list) $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_get_events_attendee()");
  $obm_q->query($query);
  return $obm_q;
}


/**
 * Get all exceptions for an array of events
 * 
 * @param array $events 
 * @access public
 * @return void
 */
function run_query_get_events_exception($events,$start_time,$end_time) {
  global $cdg_sql;
  $event_list = implode(",",$events) ;
  $obm_q = new DB_OBM;
  $calendarexception_date = sql_date_format($obm_q->type,"calendarexception_date");
  $calendarexception_date_l = sql_date_format($obm_q->type,"calendarexception_date","calendarexception_date");  

  $query = "SELECT
    calendarexception_event_id,
    $calendarexception_date_l
   FROM CalendarException
   WHERE calendarexception_event_id IN ($event_list) 
   AND $calendarexception_date > $start_time 
   AND $calendarexception_date < $end_time";
  display_debug_msg($query, $cdg_sql, "run_query_get_events_exception()");
  $obm_q->query($query);
  return $obm_q;
}


/**
 * Get all exceptions for an event
 * 
 * @param  $event_id
 * @access public
 * @return void
 */

function run_query_get_one_event_exception($calendar_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $calendarexception_date = sql_date_format($obm_q->type,"calendarexception_date");
  $calendarexception_date_l = sql_date_format($obm_q->type,"calendarexception_date","calendarexception_date");  

  $query = "SELECT
    calendarexception_event_id,
    $calendarexception_date_l
   FROM CalendarException
   WHERE calendarexception_event_id='$calendar_id'"; 

  display_debug_msg($query, $cdg_sql, "run_query_get_one_event_exception()");
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
function calendar_events_model($start_time, $end_time, $calendar_entity,$category_filter = "", $unique=false) {
  global $day_duration;

  $day_duration = 86400;
  $nr_q = run_query_calendar_no_repeat_events($start_time,$end_time,$calendar_entity,$category_filter);
  $of = &OccurrenceFactory::getInstance();
  $of->setBegin($start_time);
  $of->setEnd($end_time);
  while ($nr_q->next_record()) {
    $id = $nr_q->f("calendarevent_id");
    $title = $nr_q->f("calendarevent_title");
    $privacy = $nr_q->f("calendarevent_privacy");
    $description = $nr_q->f("calendarevent_description"); 
    $location = $nr_q->f("calendarevent_location"); 
    $category1 = $nr_q->f("calendarcategory1_label");
    $priority = $nr_q->f("calendarevent_priority");
    $date = $nr_q->f("calendarevent_date");
    $duration = $nr_q->f("calendarevent_duration");
    $all_day = $nr_q->f("calendarevent_allday");
    $entity = $nr_q->f("evententity_entity");
    $entity_id = $nr_q->f("evententity_entity_id");
    $owner = $nr_q->f("calendarevent_owner");
    if (isset($of->events[$id])) {
      $event = &$of->events[$id];
      if (!$unique) {
        $of->addOccurrence($event, $date, $entity, $entity_id);
      }
    } else {
      $event = &new Event($id,$duration,$title,$location,$category1,$privacy,$description, $all_day,'none',$owner);
      $of->addOccurrence($event, $date, $entity, $entity_id);
    }
  }
  
  $r_q = run_query_calendar_repeat_events($start_time,$end_time,$calendar_entity,$category_filter);
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
    $entity_id = $r_q->f("evententity_entity_id");    
    $all_day = $r_q->f("calendarevent_allday");       
    $owner = $r_q->f("calendarevent_owner");
    if (!$endrepeat) {
      $endrepeat = $end_time;
    }
    $addOcc = true;
    if (isset($of->events[$id])) {
      $event = &$of->events[$id];
      if ($unique) { 
        $addOcc = false;
      }
    } else {
      $event = &new Event($id,$duration,$title,$location,$category1,$privacy,$description, $all_day,$repeatkind,$owner);
    }
    $event_start =  $start_time ;
    $delta = date("H",$date) * 3600 + date("i",$date) * 60 + date("s",$date) + $duration;
    $delta = floor($delta/$day_duration);
    $event_start -= $delta * $day_duration;
    $end_date = ($endrepeat < $end_time) ? $endrepeat : $end_time;
    $end_date += $day_duration;
if ($addOcc) {
    switch ($repeatkind) {
      case "daily" :
        calendar_daily_repeatition($date,$event_start,$end_date,$repeatfrequence,&$event,$entity_id,$entity);	
        break; 
      case "weekly" :
	calendar_weekly_repeatition($date,$event_start,$end_date,$repeatdays,$repeatfrequence,&$event,$entity_id,$entity); 
	break;
      case "monthlybyday" :
	$stored = calendar_monthlybyday_repeatition($date,$event_start,$end_date,$repeatfrequence,&$event,$entity_id,$entity); 
        break;
      case "monthlybydate" :
	$stored = calendar_monthlybydate_repeatition($date,$event_start,$end_date,$repeatfrequence,&$event,$entity_id,$entity);
	break;
      case "yearly" :
	$stored = calendar_yearly_repeatition($date,$event_start,$end_date,$repeatfrequence,&$event,$entity_id,$entity);
	break;	
    }
}
  }
  if (count($of->events) > 0) {
    $exception_q = run_query_get_events_exception(array_keys($of->events),$start_time,$end_time);
    while($exception_q->next_record()) {
      $of->removeOccurrences($exception_q->f('calendarexception_event_id'), $exception_q->f('calendarexception_date'));
    }

    $attendee_q = run_query_get_events_attendee(array_keys($of->events));
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
      $event = &$of->events[$id];
      $event->add_attendee($entity,$entity_id,$entity_label);
    }
  }
  return $of->getOccurrences(); 
}


///////////////////////////////////////////////////////////////////////////////
// Perform the daily repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function calendar_daily_repeatition($date,$start_time,$end_date,$repeatfrequence,&$event,$entity_id,$entity) {
  global $day_duration;

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
    $of = &OccurrenceFactory::getInstance();
    $of->addOccurrence($event, $temp_date, $entity, $entity_id);
    $temp_date = strtotime("+$repeatfrequence days",$temp_date);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Perform the weekly repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function calendar_weekly_repeatition($date,$start_time,$end_date,$repeatdays,$repeatfrequence,&$event,$entity_id,$entity) {
  global $day_duration, $ccalendar_weekstart;

  $temp_date = get_calendar_date_day_of_week($date, $ccalendar_weekstart);
  $temp_date = strtotime("+".of_date_get_hour($date)." hours +".date("i",$date)." minutes",$temp_date);
  if ($temp_date < $start_time) {
    $temp_date = ceil(($start_time - $temp_date) / $day_duration);
    $hour_diff = of_date_get_hour($date) - of_date_get_hour($start_time);
    $min_diff = date("i",$date) - date("i",$start_time);
    $temp_date = ($temp_date % ($repeatfrequence * 7));
    if ($temp_date == "") $temp_date = 0;
    $temp_date = strtotime("-$temp_date days +$hour_diff hours +$min_diff minutes", $start_time);
  }
  $num = (date('w', $temp_date) - date('w', strtotime($ccalendar_weekstart)) +7)%7;
  for ($i=0; $i<strlen($repeatdays);$i++) {;
    $k = ($i+$num)%7;
    $repeat_days[$i]=substr($repeatdays, $k, 1);
  }
  while ($temp_date <= $end_date) {
    foreach ($repeat_days as $delta_day => $repeatition) {	  
      if ($repeatition == 1) {
        $current_date = strtotime("+$delta_day days",$temp_date);
        if ($current_date >= $start_time && $current_date <= $end_date 
            && $current_date >= $date) {
          $of = &OccurrenceFactory::getInstance();
          $of->addOccurrence($event, $current_date, $entity, $entity_id);
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
function calendar_monthlybyday_repeatition($date,$start_time,$end_date,$repeatfrequence,&$event,$entity_id,$entity) {
  global $day_duration,$ccalendar_weekstart;

  $temp_date = $date;
  $start_week = date('w', strtotime($ccalendar_weekstart));
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
      $of = &OccurrenceFactory::getInstance();
      $of->addOccurrence($event, $current_date, $entity, $entity_id);
    }
    $ts_cur = strtotime("+$repeatfrequence months", $ts_cur);
  }	
}


///////////////////////////////////////////////////////////////////////////////
// Perform the monthly by date repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function calendar_monthlybydate_repeatition($date,$start_time,$end_date,$repeatfrequence,&$event,$entity_id,$entity) {  
  global $day_duration;

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
    $of = &OccurrenceFactory::getInstance();
    $of->addOccurrence($event, $ts_date, $entity, $entity_id);
    $ts_date = strtotime("+$repeatfrequence months",$ts_date);
  }  
}


///////////////////////////////////////////////////////////////////////////////
// Perform the yearly repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function calendar_yearly_repeatition($date,$start_time,$end_date,$repeatfrequence,&$event,$entity_id,$entity) {  
  global $day_duration;

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
    $of = &OccurrenceFactory::getInstance();
    $of->addOccurrence($event, $ts_date, $entity, $entity_id);
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
  var $location;
  var $category1;
  var $privacy;
  var $description;
  var $attendee;
  var $repeat;
  var $all_day;
  var $owner;

  function Event($id,$duration,$title,$location,$category1,$privacy,$description,$all_day,$repeat,$owner) {;
    $this->id = $id;
    $this->duration = $duration;
    $this->title = $title;
    $this->location = $location;
    $this->category1 = $category1;
    $this->privacy = $privacy;
    $this->description = $description;
    $this->repeat = $repeat;
    $this->all_day = $all_day;
    $this->attendee = array();
    $this->owner = $owner;
  }

  function isMeeting() {
    return (count($this->attendee["user"]) > 1);
  }

  function isPeriodic() {
    return ($this->repeat != 'none');
  }

  function add_attendee($entity,$entity_id,$entity_label) {
    $this->attendee[$entity][$entity_id] = $entity_label;
  }

}

Class Occurrence {
  var $event;
  var $date;
  var $entity;
  var $id;

  function Occurrence(&$event, $date, $entity, $id) {
    $this->event = &$event;
    $this->date = $date;
    $this->entity = $entity;
    $this->id = $id;
  }

}

Class OccurrenceFactory {

  var $occurrences;
  var $events;
  var $begin;
  var $end;

  function OccurrenceFactory() {
    $this->occurrences = array();
    $this->events = array();
  }
  
  function setBegin($begin) {
    $this->begin = $begin;
  }

  function setEnd($end) {
    $this->end = $end;
  }

  function addOccurrence(&$event, $date, $entity, $id) {
    if ($date < $this->end && ($date + $event->duration) > $this->begin) {
      $this->occurrences[$event->id][] = &new Occurrence($event, $date, $entity, $id);
      $this->events[$event->id] = &$event;
    }
  }

  function removeOccurrences($id, $date) {
    foreach($this->occurrences[$id] as $key => $occurrence) {
      if(date('Ymd',$date) ==  date('Ymd',$occurrence->date)) {
        unset($this->occurrences[$id][$key]); 
      }
    }
  }

  function getOccurrences() {
    $return = array();
    foreach($this->occurrences as $evtOccurrences) {
      $return = array_merge($return,$evtOccurrences);
    }
    return $return;
  }

  function &getInstance() {
    static $singleton = null;
    if (is_null($singleton)) {
      $singleton = new OccurrenceFactory();
    }
    return $singleton;
  }

  function eventHaveOccurences($id) {
    return isset($this->events[$id]);
  }

  function periodHaveOccurrences($time, $duration, $entityFilter=null) {
    foreach($this->occurrences as $evtOccurrences) {
      foreach($evtOccurrences as $occurence) {
        if(!$entityFilter || ( 
           is_array($entityFilter[$occurence->entity]) && (
             count($entityFilter[$occurence->entity]) == 0 || 
             in_array($occurence->id,$entityFilter[$occurence->entity])))) {
          $eventDuration = $occurence->event->duration;
          $eventDate = $occurence->date;
          if($time <= ($eventDate + $eventDuration) && ($time + $duration) > $eventDate) {
            return true;
          }
        }
      }
    }
    return false;
  }

  function getPeriodOccurrences($time, $duration, $entityFilter=null) {
    foreach($this->occurrences as $evtOccurrences) {
      foreach($evtOccurrences as $occurence) {
        if(!$entityFilter || ( 
           is_array($entityFilter[$occurence->entity]) && (
             count($entityFilter[$occurence->entity]) == 0 || 
             in_array($occurence->id,$entityFilter[$occurence->entity])))) {
          $eventDuration = $occurence->event->duration;
          $eventDate = $occurence->date;
          if($time <= ($eventDate + $eventDuration) && ($time + $duration) > $eventDate) {
            $occurences[] = $occurence;
          }
        }
      }
    }
    return $occurences;
  }
}


?>
