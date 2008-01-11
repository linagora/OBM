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
  $timeupdate = sql_date_format($db_type, 'calendarevent_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'calendarevent_timecreate', 'timecreate');
  $calendarevent_date = sql_date_format($db_type,'calendarevent_date','calendarevent_date');
  $calendarevent_endrepeat = sql_date_format($db_type,'calendarevent_endrepeat', 'calendarevent_endrepeat');
  $multidomain = sql_multidomain('calendarevent');
  $query = "SELECT DISTINCT
      calendarevent_id,
      c.userobm_login AS usercreate,
      u.userobm_login AS userupdate,
      $timeupdate,
      $timecreate,
      calendarevent_owner, 
      o.userobm_lastname AS owner_lastname,
      o.userobm_firstname AS owner_firstname,
      calendarevent_title, 
      calendarevent_description,
      calendarevent_properties,
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
      calendarevent_color,
      $calendarevent_endrepeat,
      $calendarevent_date
    FROM  
      CalendarEvent
      LEFT JOIN CalendarCategory1 ON calendarevent_category1_id = calendarcategory1_id
      LEFT JOIN UserObm AS c ON calendarevent_usercreate = c.userobm_id
      LEFT JOIN UserObm AS u ON calendarevent_userupdate = u.userobm_id
      LEFT JOIN UserObm AS o ON calendarevent_owner = o.userobm_id
    WHERE calendarevent_id = '$calendar_id' $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_detail()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the name and first name of users
// Parameters: 
//   - $users : users id array, the event is assigned to 
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_get_user_name($users='') {
  global $cdg_sql;
  $multidomain = sql_multidomain('userobm');

  $in = "'".implode ("','", $users)."'";

  $obm_q = new DB_OBM;
  $query = "SELECT userobm_lastname, userobm_firstname, userobm_id
            FROM UserObm
	    WHERE userobm_id IN ($in) $multidomain";

  $query.= " ORDER BY userobm_id"; 
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_get_user_name()');
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
  $multidomain = sql_multidomain('resource',$and);
  $and = false; 
  $obm_q = new DB_OBM;
  $query = "SELECT resource_name, resource_id
            FROM Resource 
            WHERE $where $multidomain";

  $query.= " ORDER BY resource_id";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_get_resource_label()');
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

  $multidomain = sql_multidomain('resource');
  $obm_q = new DB_OBM;
  $query = "SELECT resourcetype_property 
    FROM ResourceType 
    LEFT JOIN Resource ON resource_rtype_id=resourcetype_id
    WHERE resource_id='$r_id' $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_get_resource_property()');
  $obm_q->query($query);
  $obm_q->next_record();
  $return = $obm_q->f('resourcetype_property');

  return $return;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the label of entities 
// Parameters:
//   - $entities : entity id array, the event is assigned to
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_get_entity_label($entities) {

  if ((is_array($entities['user']))
      && (count($entities['user']) > 0)) {
    $ret['user'] = run_query_calendar_get_user_name($entities['user']);
  }
  if ((is_array($entities['resource']))
      && (count($entities['resource']) > 0)) {
    $ret['resource'] = run_query_calendar_get_resource_label($entities['resource']);
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
      $r_array = array_merge($r_array, of_usergroup_get_group_users($group_id));
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
    
    $multidomain = sql_multidomain('rgroup');
    
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

    display_debug_msg($query, $cdg_sql, 'run_query_resource_resourcegroup()');
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
  $entity_id = $calendar['entity_id'];
  $entity = $calendar['entity_kind'];
  $event_id = $calendar['calendar_id'];
  $state = $calendar['decision_event'];

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
  if($entity_id == '') {
    $entity_id = $obm['uid'];
  }
  if($obm['uid'] != $entity_id || $entity != 'user') {
    if( $entity == 'user') {
      $writable = of_right_entity_for_user('calendar', $obm['uid'], 'write', '', 'userobm');
    } elseif($entity == 'resource') {
      $writable = of_right_entity_for_user('Resource', $obm['uid'], 'write');
    }
    
    if (!in_array($entity_id, $writable['ids'])) {
      $err['msg'] = $l_of_right_err_auth;  
      return false;
    } 
  }

  $query = "UPDATE EventEntity 
            SET evententity_state = '$state'
            WHERE evententity_entity_id = $entity_id
              AND evententity_entity = '$entity'
              AND evententity_event_id = '$event_id'
              ";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_occurence_state()');  
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  return $retour;    
}


///////////////////////////////////////////////////////////////////////////////
// Select All waiting Events
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_waiting_events() {
  global $obm, $cdg_sql;

  $uid = $obm['uid'];
  
  $multidomain = sql_multidomain('calendarevent');
  $writable_cal = of_right_entity_for_user('calendar', $uid, 'write', '', 'userobm');
  $writable_res = of_right_entity_for_user('Resource', $uid, 'write');
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $concat = array (
    array (
      'type' => 'field',
      'value' => 'userobm_lastname'
      ),
    array (
      'type' => 'string',
      'value' => ' '
      ),
    array (
      'type' => 'field',
      'value' => 'userobm_firstname'
      ) 
    );
  $entity_label = sql_string_concat($db_type,$concat);
  $calendarevent_endrepeat = sql_date_format($db_type,'calendarevent_endrepeat','calendarevent_endrepeat'); 
  $calendarevent_date = sql_date_format($db_type,'calendarevent_date','calendarevent_date');    
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
        SELECT $entity_label AS entity_label, userobm_id AS entity_id, 'user' AS entity_entity FROM UserObm 
        UNION SELECT resource_name AS entity_label, resource_id AS entity_id, 'resource' AS entity_entity FROM Resource
      ) AS Entity  ON evententity_entity_id = entity_id AND evententity_entity = entity_entity
      LEFT JOIN UserObm u ON calendarevent_owner = u.userobm_id
    WHERE 
      evententity_state = 'W'
      AND ( 1=0
      $multidomain
      ";

  if (is_array($writable_cal) && (count($writable_cal['ids'])>0)) {
    $query .= "OR ( entity_id IN ('".implode("','",$writable_cal['ids'])."') AND entity_entity = 'user')";    
  }
  if (is_array($writable_res) && (count($writable_res['ids'])>0)) {
    $query .= "OR ( entity_id IN ('".implode("','",$writable_res['ids'])."') AND entity_entity = 'resource')";
  }
  $query .= " OR (entity_id = '$uid' AND entity_entity = 'user')) ORDER BY entity_entity, entity_label, calendarevent_date"; 
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_waiting_events()');
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Delete all participation to an event
// Parameters:
//   - $calendar[] : hash values for the event
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_delete_event_entity($calendar, $users = null) {
  global $cdg_sql;

  $e_id = $calendar['calendar_id'];
  $query = "DELETE FROM EventEntity WHERE evententity_event_id = '$e_id'";
  if(is_array($users)) {
    if(count($users) != 0) {
      $query .= " AND (evententity_entity != 'user' OR evententity_entity_id IN (".implode(",",$users)."))";
    }
  } else {
     $query .= " AND evententity_entity != 'user'";
  }

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete_event_entity()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
} 


///////////////////////////////////////////////////////////////////////////////
// Insert one event
// Parameters: 
//   - $calendar        : Calendar params
//   - $sel_entity_id : List entitis id ['user'] ['resource']
//   - $event_id      : var to get the id to be returned
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_add_event($calendar, $sel_entity_id, &$event_id) {
  global $obm, $ccalendar_resource_admin, $ccalendar_send_ics;
  global $l_from, $l_to, $l_date, $l_resource_allocated;
  global $l_add_event_mail_head,$l_add_event_mail_subject;

  $writable_cal = of_right_entity_for_user('calendar', $obm['uid'], 'write', '', 'userobm');
  $mail  = $calendar['mail'];
  $groups = $sel_entity_id['group'];
  $mail_title = stripslashes($calendar['title']);
  $datebegin = $calendar['date_begin'];
  $dateend = $calendar['date_end'];
  if ($calendar['task_new_id']) $task_id = $calendar['task_new_id']; 
  else $task_id = $calendar['task_id'];
  // Get the final user list (merge group)
  $users_array = $sel_entity_id['user'];
  if (is_array($groups)) {
    $user_group_array = array();
    foreach($groups as $group_id) {
      $user_group_array = array_merge($user_group_array, of_usergroup_get_group_users($group_id));
    }
    $users_array = array_merge($users_array, $user_group_array);
    $users_array = array_unique($users_array);
  }
  if (! is_array($users_array)) {
    $users_array = array($obm["uid"]);
  }

  // Insert the Event
  $event_id = run_query_calendar_insert_event_data($calendar);   

  // Insert Event users
  foreach ($users_array as $user_id) {
    if ($user_id == $obm['uid'] || in_array($user_id, $writable_cal['ids'])) {
      run_query_calendar_insert_entity_event('user', $user_id, $event_id, 'A');
    } else {
      run_query_calendar_insert_entity_event('user', $user_id, $event_id, 'W');
    }
  }

  // Insert The event task
  if ($task_id > 0) {
    run_query_calendar_insert_entity_event('task', $task_id, $event_id, 'A');
  }

  // Insert Event resources
  $resources_writable = array(); // Writable Resources  
  $resources_address = array(); // Resource Admin notification
  // Get the final resource list and insert resource links
  $resources_array = $sel_entity_id['resource'];
  if ( (is_array($resources_array)) && (count($resources_array) > 0) ) {
    $resources_array = array_unique($resources_array);
    $writable_res = of_right_entity_for_user('Resource', $obm['uid'], 'write');
    foreach ($resources_array as $res_id) {
      if (in_array($res_id, $writable_res['ids'])) {
	run_query_calendar_insert_entity_event('resource', $res_id, $event_id, 'A');
      } else {
	run_query_calendar_insert_entity_event('resource', $res_id, $event_id, 'W');
      }
      array_push($resources_writable, $res_id);
    }
  }    

  // Insert My alert if set
  if (isset($calendar['alert'])) {
    run_query_calendar_event_alert_insert($event_id, $obm['uid'], $calendar['alert']);
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

  $now = date('Y-m-d H:i:s');
  if($calendar['owner']) {
    $owner = $calendar['owner'];
  } {
    $owner = $obm['uid'];
  }
  $title = $calendar['title'];
  $category1_id = $calendar['category1'];
  $priority = $calendar['priority'];
  $description = $calendar['description'];
  $location = $calendar['location'];
  ($calendar['all_day'] != 1)?$all_day= '0':$all_day = '1';
  $datebegin = of_isodate_convert($calendar['date_begin']);
  $dateend = of_isodate_convert($calendar['date_end']);
  $event_duration = $calendar['event_duration'];
  if ($calendar['privacy']!=1) $privacy = 0; else $privacy = 1; 
  $repeat_kind = $calendar['repeat_kind'];
  $repeat_days = $calendar['repeat_days']; 
  $repeat_end = of_isodate_convert($calendar['repeat_end']);
  $repeatfrequency = $calendar['repeatfrequency'];  
  $date_exception = $calendar['date_exception'];
  $item = $calendar['list-item'];
  $item_id = $calendar['list-item-id'];
  $res_name = $calendar['res-name'];
  $res_id = $calendar['res-id'];
  $property = $calendar['property'];
  $color = $calendar['color'];
  $property_name = $calendar['property-name'];
  if (is_array($res_name)) {
    foreach($res_name as $key=>$res) {
      $properties .= "<resource>
         <res_name>$res</res_name>
         <res_id>$res_id[$key]</res_id>";

      if (is_array($item) && isset($item[$key])) {
        $properties .= "<item>$item[$key]</item>
          <item_id>$item_id[$key]</item_id>";
      }

      if (is_array($property_name) && isset($property_name[$key])) {
        $properties .= "<property_name>$property_name[$key]</property_name>
                         <property_value>$property[$key]</property_value>";                       
      }
      $properties .= "</resource>";
    }
  }
  $properties = "<extended_desc>$properties</extended_desc>";

  $obm_q = new DB_OBM;
  
  $query = "INSERT INTO CalendarEvent (
    calendarevent_timeupdate,                         
    calendarevent_timecreate,                         
    calendarevent_usercreate,
    calendarevent_owner,
    calendarevent_title, 
    calendarevent_date,     
    calendarevent_description,
    calendarevent_properties,
    calendarevent_location, 
    calendarevent_category1_id,
    calendarevent_priority,
    calendarevent_privacy, 
    calendarevent_duration,
    calendarevent_repeatkind,
    calendarevent_repeatfrequence,
    calendarevent_repeatdays,
    calendarevent_allday,
    calendarevent_color,
    calendarevent_endrepeat,
    calendarevent_domain_id)
  VALUES (
    '$now',
    '$now',
    '$obm[uid]',
    '$owner',
    '$title',
    '$datebegin',
    '$description',
    '$properties',
    '$location',
    '$category1_id',
    '$priority',
    '$privacy',
    '$event_duration',		
    '$repeat_kind',
    '$repeatfrequency',
    '$repeat_days',
    '$all_day',
    '$color',
    '$repeat_end',
    '$obm[domain_id]')";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_event_data()');
  $obm_q->query($query);
  
  $query = "SELECT calendarevent_id
    FROM CalendarEvent
    WHERE calendarevent_timecreate = '$now'
      AND calendarevent_date='$datebegin' 
      AND calendarevent_title='$title'
      AND calendarevent_duration='$event_duration'
      AND calendarevent_domain_id='$obm[domain_id]'
      ORDER BY calendarevent_id";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_event_data()');
  $obm_q->query($query);
  while($obm_q->next_record())$id = $obm_q->f('calendarevent_id');
  $calendar['calendar_id'] = $id;

  // Exception
  if ($repeat_kind != 'none' && is_array($date_exception)) {
    foreach(array_unique($date_exception) as $value) {
      if($value != '') {
        $value = of_isodate_convert($value);
        $calendar['old_date_begin'] = $value.' '.$calendar['time_begin'].':'.$calendar['min_begin'].':00';
        run_query_calendar_event_exception_insert($calendar); 
      }
    }
  }

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Insert an occurence of a event
// Parameters: 
//   - $entity : entity type 'user', 'resource'
//   - $entity_id :
//   - $event_id  :
//   - $state     :
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_insert_entity_event($entity, $entity_id,$event_id, $state) {
  global $cdg_sql, $obm;

  $uid = $obm['uid'];
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

  display_debug_msg($query, $cdg_sql, 'run_query_entity_user_event()');
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

  $fd_dest = fopen ($filename,'w');
  if ($fd_dest == false) {
    die ($l_file_error.$dest.'dead');
  }

  $iso_date = $calendar['date'];
  $ts_date = strtotime($iso_date);  
 
  $start_time = strtotime('-1 year', $ts_date);
  $end_time = strtotime('+1 year', $ts_date);

  $calendar_user = array ($obm['uid'] => 'dummy');  
  $obm_event = run_query_calendar_detail($event_id);

  $msg =  "
BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//OBM/OBM V$obm_version Calendar//EN
METHOD:REQUEST";

  if ($obm_event->f('calendarevent_repeatkind') == 'none') {
    $id = $obm_event->f('calendarevent_id');
    $title = 'SUMMARY:' . $obm_event->f('calendarevent_title');
    $title = wordwrap($title,74,"\n ",1);
    $location = $obm_event->f('calendarevent_location');
    $priority = $obm_event->f('calendarevent_priority');
    $category1 = strtoupper($obm_event->f('calendarcategory1_label')); 
    $date_ts = $obm_event->f('calendarevent_timeupdate');
    $date_ts = gmdate("Ymd\THis\Z",$date_ts);
    if ($obm_event->f('calendarevent_description') != '') {
      $description = preg_replace("/\\r?\\n/","\\n",$obm_event->f('calendarevent_description'));
      $description = 'DESCRIPTION:' . $description;
    } else {
      $description = '';
    }
    if (count($resources) > 0) {
      if ($description == '') $description = 'DESCRIPTION:';
      $description .= "$l_resource_allocated:";
      $resource_q = run_query_get_resources_info($resources);
      while ($resource_q->next_record()) {
        $description .=  "\\n  ".$resource_q->f('resource_name');
      }
    }
    $description = "\n" . wordwrap($description,74,"\n ",1);
    $date_b = $obm_event->f('calendarevent_date');
    $date_b = gmdate("Ymd\THis\Z",$date_b);
    $date_e = $obm_event->f('calendarevent_date') + $obm_event->f('calendarevent_duration') ;
    $date_e = gmdate("Ymd\THis\Z",$date_e);
    switch($priority) {
      case 1 : $priority = 3;
	break;
      case 3 : $priority = 1;
	break;
    }
    $private = $obm_event->f('calendarevent_privacy');
    switch($private) {
      case 0 : $private = 'PUBLIC';
	break;
      case 1 : $private = 'PRIVATE';
	break;
    }        
    $organizer_q = run_query_get_recipients(array($obm_event->f('calendarevent_owner')), 1);
    $organizer_q->next_record();
    $organizer_cn = $organizer_q->f('userobm_lastname').' '.$organizer_q->f('userobm_firstname');
    $organizer_email=$organizer_q->f('userobm_email');
    $attendee_q = run_query_get_recipients($attendees, 1);
    $msg_attendee = '';
    if (is_array($attendee_q)) {
      while ($attendee_q->next_record()) {
        $msg_attendee .= "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=\"'";
        $msg_attendee .= $attendee_q->f('userobm_lastname').' '.$attendee_q->f('userobm_firstname');
        $msg_attendee .= "'\":MAILTO:";
        $msg_attendee .= $attendee_q->f('userobm_email');
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
    $id = $obm_event->f('calendarevent_id');
    $title = $obm_event->f('calendarevent_title');
    $location = $obm_event->f('calendarevent_location');
    $priority = $obm_event->f('calendarevent_priority'); 
    $category1 = strtoupper($obm_event->f('calendarcategory1_label')); 
    if ($obm_event->f('calendarevent_description') != '') {
      $description = "\nDESCRIPTION:".$obm_event->f('calendarevent_description');
    } else {
      $description = '';
    }
    if (count($resources) > 0) {
      if ($description == '') $description = 'DESCRIPTION:';
      $description .= "$l_resource_allocated:";
      $resource_q = run_query_get_resources_info($resources);
      while ($resource_q->next_record()) {
        $description .=  "\\n  ".$resource_q->f('resource_name');
      }
    }
    $date_b = $obm_event->f('calendarevent_date');
    $date_b = date('Ymd',$date_b).'T'.date('His',$date_b);
    $start_date = $date_b;
    $date_e = $obm_event->f('calendarevent_date') + $obm_event->f('calendarevent_duration') ;
    $date_e = date('Ymd',$date_e).'T'.date('His',$date_e);
    $kind = $obm_event->f('calendarevent_repeatkind');
    $end = $obm_event->f('calendarevent_endrepeat');
    $end = date('Ymd',$end).'T'.date('His',$end);
    $repeat_days = $obm_event->f('calendarevent_repeatdays');
    $repeat_frequence = $obm_event->f('calendarevent_repeatfrequence');
    switch($priority) {
      case 1 : $priority = 3;
  	  break;
      case 3 : $priority = 1;
	  break;
    }
    $private = $obm_event->f('calendarevent_privacy');
    switch($private) {
      case 0 : $private = 'PUBLIC';
	  break;
      case 1 : $private = 'PRIVATE';
	  break;
    }   
    if ($kind == 'daily') {
      $repeat = "FREQ=DAILY;UNTIL=$end;INTERVAL=$repeat_frequence";
    } elseif($kind == 'weekly') {
      $l_day_repeat = array('SU','MO','TU','WE','TH','FR','SA');
      $start_week_day = strtotime($ccalendar_weekstart);
      for ($i=0; $i<7; $i++) {
  	$day_num = date('w', $start_week_day);
      	$day = $l_day_repeat[$day_num];
	if (strcmp(substr($repeat_days,$i,1),'1')==0) {
	  if ($i!=0) {
	    $dis_repeat_days .= ',';
	  }
	  $dis_repeat_days .= "$day";
  	}
      	$start_week_day = strtotime('+1 day', $start_week_day); 
      } 
      $repeat = "FREQ=WEEKLY;UNTIL=$end;INTERVAL=1;BYDAY=$dis_repeat_days";
    } elseif ($kind == 'monthlybydate') {
      $day = date('d',$start_date);
      $repeat = "FREQ=MONTHLY;UNTIL=$end;INTERVAL=$repeat_frequence;BYDAY=$day"; 
    } elseif ($kind == 'monthlybyday') {
      $start_week_day = date('w',$start_date);
      $daypos = ceil(substr($start_date,6,2)/7);
      $day_num = date('w',strtotime("+ $start_week_day days",$start_date));
      $day = $l_day_repeat[$day_num];
      $repeat = "FREQ=MONTHLY;UNTIL=$end;INTERVAL=$repeat_frequence;BYDAY=$daypos$day";
    } elseif($kind == 'yearly') {
      $monthpos = date('m',$start_date);
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
// Analyse the imported ics file
///////////////////////////////////////////////////////////////////////////////
function run_query_icalendar_insert($params) {
  global $obm ;
  global $all_emails ;

  // --- get the unfolding ics
  $ics = file($params['ics_tmp']) ;
  $new_ics = array() ; // unfolded $ics
  $prevline = '' ;
  $i = -1 ;
  foreach ($ics as $line) {
    // use ISO-8859-15
    $line = mb_convert_encoding($line, 'ISO-8859-15', 'UTF-8, ISO-8859-15');

    // if begin with RFC2445_WSP, add to previous line
    if (preg_match("/^[".RFC2445_WSP."]/", $line)) { 
      $line = preg_replace("/^[".RFC2445_WSP."]/",'',$line) ;
      $prevline .= $line ;
      $line = '' ;
      continue ;
    }

    if ($prevline !== '') {
      $new_ics[] = $prevline ;
    }

    $prevline = preg_replace(array("/\n/","/\r/"),'',$line) ;
  } ;
  if ($prevline !== '') {
    $new_ics[] = $prevline ;
  }

  // --- get calendars
  $vcalendars = ics_get_component('VCALENDAR', $new_ics) ; // return an array of calendar components

  // get events and todos
  foreach ($vcalendars as $vcalendar) {
    $vevents = ics_get_component('VEVENT', $vcalendar) ;
    //$vtodos = ics_get_component('VTODO', $vcalendar) ;
  }

  // for future use, constructs a hash of all users emails
  $all_emails = get_all_email_list() ;

  // process events
  $nb_processed_event = 0 ;
  foreach($vevents as $vevent) {
    $properties = array() ;
    $properties = ics_get_vevent_properties($vevent) ;

    //if no property returned, event is not processed (ex : organizer is set but not equal to current userobm)
    if (count($properties) == 0) {
      continue ;
    }

    $sel_entity_id = array() ;

    // organizer (if none, then current userobm)
    if (isset($properties['organizer'])) { 
      $sel_entity_id['user'][] = $properties['organizer']['uid'] ;
    }
    else {
      $sel_entity_id['user'][] = $obm['uid'] ;
    }

    // attendee(s)
    if (isset($properties['attendees'])) {
      foreach($properties['attendees'] as $attendee) {
        $sel_entity_id['user'][] = $attendee['uid'] ;
      }
    }

    run_query_calendar_add_event($properties, $sel_entity_id, &$event_id) ;
    $nb_processed_event++ ;
  }

  return array($nb_processed_event) ;

  // process todos
  //foreach($vtodos as $vtodo) {
  //  echo "VTODO : " ;
  //  echo ics_get_property('SUMMARY', $vtodo) ;
  //  $icsdate = ics_get_property('DTSTART', $vtodo) ;
  //  $date = date("d/m/Y H:i:s", ics_date_to_timestamp($icsdate)) ;
  //  echo " (".$icsdate." -> ".$date.")" ;
  //  echo "<br>\n" ;
  //}
}

///////////////////////////////////////////////////////////////////////////////
// get_all_emails_list
// Constructs a list of all possible users emails in OBM
///////////////////////////////////////////////////////////////////////////////
function get_all_email_list() {
  global $cgp_use, $cdg_sql, $obm ;

  $all_emails = array() ;

  $obm_q = new DB_OBM();

  // si mail activé, construire la liste des noms de domaines smtp (+alias) pour le domaine (sens obm) en cours.
  if ($cgp_use['service']['mail']) {
    $domain_list = array() ;

    $query = "SELECT domain_name, domain_alias FROM Domain where domain_id = ".$obm['domain_id'] ;
    display_debug_msg($query, $cdg_sql, 'get_all_emails_list / get domains names and aliases');
    $result = $obm_q->query($query) ;
    if ($obm_q->num_rows() > 0) {
      while ($obm_q->next_record()) {
        $domain_list[] = $obm_q->f('domain_name') ;
	array_merge($domain_list, explode("\n", $obm_q->f('domain_alias'))) ;
      }
    }
  }

  // pour chaque utilisateur
  $query = "SELECT userobm_id, userobm_email FROM UserObm WHERE userobm_domain_id = ".$obm['domain_id'] ;
  display_debug_msg($query, $cdg_sql, 'get_all_emails_list / get user list');
  $result = $obm_q->query($query) ;

  while ($obm_q->next_record()) {
    $emails = $obm_q->f('userobm_email') ;
    $uid = $obm_q->f('userobm_id') ;

    // -- si mail actif, on mixe les parties gauche et les nom de domaines smtp
    if ($cgp_use['service']['mail']) {
      // ---- partie gauche
      $gauche_list = explode("\n", $emails) ;
      foreach($gauche_list as $email) {
        if ($email != '') {
	  $email = rtrim($email, "\r") ;
          // ---- partie droite
          foreach ($domain_list as $domain) {
            $all_emails["$email@$domain"] = $uid ;
          }
        }
      }
    }
    // -- si mail inactif, on récupère direct l'adresse
    else {
      if ($emails != '') {
        $all_emails[$emails] = $uid ;
      }
    }
  }

  return $all_emails ;
}

///////////////////////////////////////////////////////////////////////////////
// ics_get_component
// params : 
// 	$keyword	: keyword for searching in vcalendar (BEGIN... and END...)
//	$cal		: array of lines containing the (unfolded) ics file
///////////////////////////////////////////////////////////////////////////////
function ics_get_component($keyword, $cal) {
  $components = array() ;
  $flagGet = false ;

  foreach($cal as $line) {
    if (strpos($line, 'BEGIN:'.$keyword) === 0) {
      $component = array() ;
      $component[] = $line ;
      $flagGet = true ;
    }
    if ($flagGet) {
      $component[] = $line ;
    }
    if (strpos($line, 'END:'.$keyword) === 0) {
      $components[] = $component ;
      $flagGet = false ;
    }
  }

  return $components ;
}


///////////////////////////////////////////////////////////////////////////////
// ics_get_property
///////////////////////////////////////////////////////////////////////////////
function ics_get_property($keyword, $component) {
  $value = '' ;

  foreach ($component as $line) {
    if (strpos($line, $keyword) === 0) {
      $value = substr($line, strlen($keyword)+1) ;
      break ;
    }
  }

  return $value ;
}


///////////////////////////////////////////////////////////////////////////////
// ics_get_vevent_properties
///////////////////////////////////////////////////////////////////////////////
function ics_get_vevent_properties($vevent) {
  global $c_none, $ics_event_priority_to_obm, $obm ;
  //$duration = strtotime($date_end) - strtotime($date_begin); 

  $properties = array() ;
  $properties['attendees'] = array() ;
  foreach ($vevent as $line) {

    // direct import, no processing
    $no_process = array('SUMMARY' => 'title', 
    			'DURATION' => 'event_duration',
			'DESCRIPTION' => 'description',
			'LOCATION' => 'location') ;
    foreach ($no_process as $keyword => $field) {
      if(strpos($line, $keyword) === 0) {
        $properties[$field] = addslashes(substr($line, strlen($keyword) + 1)) ;
        continue ;
      }
    }

    // date_begin
    $date_begin = '' ;
    if (strpos($line, 'DTSTART') === 0) {
      $icsdate = substr($line, strlen('DTSTART') + 1) ;
      $date_begin = ics_date_to_timestamp($icsdate) ;
      //echo date('Y-m-d H:i:s', $date_begin)."<br>" ;
      $properties['date_begin'] = date('Y-m-d H:i:s', $date_begin) ;
      continue ;
    }

    // duration
    if (strpos($line, 'DTEND') === 0) {
      $icsdate = substr($line, strlen('DTEND') + 1) ;
      $date_end = ics_date_to_timestamp($icsdate) ;
      // date_begin ?
      if ($date_begin == '') {
        $date_begin = ics_date_to_timestamp(ics_get_property('DTSTART', $vevent)) ;
      }
      if ($date_begin != '') {
      	$duration = $date_end - $date_begin ;
	if ($duration > 0) {
	  $properties['event_duration'] = $duration ;
	}
      }
      continue ;
    }

    // priority
    if (strpos($line, 'PRIORITY') === 0) {
      $priority = substr($line, strlen('PRIORITY') + 1) ;
      $properties['priority'] = $ics_event_priority_to_obm[$priority] ;
      continue ;
    }

    // privacy
    if (strpos($line, 'CLASS') === 0) {
      $class = substr($line, strlen('CLASS') + 1) ;
      if ($class == 'PUBLIC') {
        $privacy = 0 ;
      }
      else {
        $privacy = 1 ;
      }
      $properties['privacy'] = $privacy ;
      continue ;
    }

    // organizer
    if (strpos($line, 'ORGANIZER') === 0) {
      $organizer = substr($line, strlen('ORGANIZER')) ;
      $userobm = ics_get_obmuser_from_icsuser($organizer) ;
      if ((is_array($userobm)) and (isset($userobm['uid'])) and ($userobm['uid'] == $obm['uid'])) {
      	$properties['organizer'] = $userobm ;
      }
      else {
        // current userobm_id is not organizer for this event, stop all process
	return array() ;
      }
      continue ;
    }

    // attendees
    if (strpos($line, 'ATTENDEE') === 0) {
      $attendee = substr($line, strlen('ATTENDEE')) ;
      $userobm = ics_get_obmuser_from_icsuser($attendee) ;
      // if attendee ROLE is CHAIR, forget it
      // if attendee obm_uid is 0, forget it
      if ((is_array($userobm)) and ($userobm['params']['ROLE'] != 'CHAIR') and isset($userobm['uid']) and ($userobm['uid'] != $c_none)) {
      	$properties['attendees'][] = $userobm ;
      }
    }

    // recurent events


  }
  
  if ($properties['priority'] == 0) {
    $properties['priority'] = 2 ;
  }
  if ($properties['repeat_kind'] == '') {
    $properties['repeat_kind'] = 'none' ;
  }

  return $properties ;
}


///////////////////////////////////////////////////////////////////////////////
// ics_get_vtodo_properties
///////////////////////////////////////////////////////////////////////////////
function ics_get_vtodo_properties($vtodo) {
}


///////////////////////////////////////////////////////////////////////////////
// ics_date_to_timestamp
///////////////////////////////////////////////////////////////////////////////
function ics_date_to_timestamp($icsdate) {
  if ($icsdate == '') {
    return 0 ;
  }
  preg_match('/.*(\d{4})(\d{2})(\d{2})(T(\d{2})(\d{2})(\d{2})){0,1}/', $icsdate, $result) ;
  list($all, $year, $month, $day, $time, $hour, $minute, $second) = $result ;
  return mktime($hour, $minute, $second, $month, $day, $year) ;
}


///////////////////////////////////////////////////////////////////////////////
// ics_get_obmuser_from_icsuser
///////////////////////////////////////////////////////////////////////////////
function ics_get_obmuser_from_icsuser($attendee_line) {
  global $c_none ;

  $parameters = array() ;
  $params = array() ;
  $cal_address = '' ;
  $attendee = array() ;

  // split parameters from value (cal address)
  list($all_parameters, $cal_address) = explode(':', $attendee_line, 2) ;
  if (strpos($parameters, ';') === 0) {
    $parameters = rtrim($parameters, ';') ; # FIXME utilité du test ?
  }
 
  if ($cal_address == '') {
    return $c_none ; // invalid field
  }

  // get parameters
  if ($all_parameters != '') {
    $parameters = split(';', $all_parameters) ;
    if (count($parameters) > 0) {
      for ($i = 0; $i < count($parameters) ; $i++) {
        list($key, $val) = split('=', $parameters[$i]) ;
        $attendee['params'][$key] = $val ;
      }
    }
  }

  // -- Lotus Note way
  ics_get_obmuser_from_lotus($attendee, $cal_address) ;

  // -- standard way
  if (!isset($attendee['uid']) or ($attendee['uid'] == $c_none)) {
    $attendee['uid'] = ics_get_obmuser_from_smtp($cal_address) ;
  }

  return $attendee ;
}

///////////////////////////////////////////////////////////////////////////////
// LOTUS SPECIFIC
// ics_get_obmuser_from_lotus
///////////////////////////////////////////////////////////////////////////////
function ics_get_obmuser_from_lotus(& $attendee, $cal_address) {
 // recup CN ?
  if (isset($attendee['params']) and (array_key_exists('CN', $attendee['params']))) {
    list($firstname, $lastname) = explode(' ', $attendee['params']['CN']) ;
    $uid = search_user_by_name($lastname, $firstname) ;
    $attendee['uid'] = $uid ;
  }
  else { // try to analyse cal_address 
    if (preg_match('#^(?:CN=|MAILTO:)([^/]*)/#i', trim($cal_address), $matches)) {
      // seems that some lotus versions have '_' separator and others versions have ' ' separator
      if (strpos($matches[1], '_') !== false) {
        list($firstname, $lastname) = explode('_', $matches[1]) ;
      }
      else {
        list($firstname, $lastname) = explode(' ', $matches[1]) ;
      }
      $uid = search_user_by_name($lastname, $firstname) ;
      $attendee['uid'] = $uid ;
    }
  }
}

///////////////////////////////////////////////////////////////////////////////
// search_user_by_name
// Parameters:
//   - $lastname
//   - $firstname
// Return
//   - userobm_id
///////////////////////////////////////////////////////////////////////////////
function search_user_by_name($lastname, $firstname) {
  global $c_none ;

  $sql = "SELECT userobm_id from UserObm ";
  $sql .= "WHERE ";
  $sql .= "UPPER(userobm_lastname) = '".strtoupper($lastname)."' AND ";
  $sql .= "UPPER(userobm_firstname) = '".strtoupper($firstname)."' AND ";
  $sql .= "userobm_archive = '0';";

  $obm_q = new DB_OBM() ;
  $result = $obm_q->query($sql) ;
  if ($obm_q->num_rows() == 1) {
    $obm_q->next_record() ;
    return $obm_q->f('userobm_id') ;
  }
  else { // no result OR more than one result
    return $c_none ;
  }
}

///////////////////////////////////////////////////////////////////////////////
// STANDARD : SMTP address
// ics_get_obmuser_from_smtp
///////////////////////////////////////////////////////////////////////////////
function ics_get_obmuser_from_smtp($cal_address) {
  global $c_none, $all_emails ;

  $uid = $c_none ;

  if (preg_match('#^MAILTO:([^@]*)@(.*)$#i', trim($cal_address), $matches)) {
    $user = $matches[1];
    $domain = $matches[2] ;

    if (array_key_exists("$user@$domain", $all_emails)) {
      $uid = $all_emails["$user@$domain"] ;
    }
  }

  return $uid ;
}

///////////////////////////////////////////////////////////////////////////////
// Check for conflict : Only with the first occurence of each event,
// Parameters:
//   - $calendar       : calendar parameters
//   - sel_entity_id : selected entities id
///////////////////////////////////////////////////////////////////////////////
function check_calendar_conflict($calendar, $sel_entity_id) {

  $ts_begin = isodatetime_to_timestamp($calendar['date_begin']);
  $ts_end = isodatetime_to_timestamp($calendar['date_end']);
  $id = $calendar['calendar_id'];
  if($calendar['all_day'] == 1) {
    $ts_begin = strtotime(date('Y-m-d 00:00:00',$ts_begin));
    $ts_end = strtotime(date('Y-m-d 23:59:59',$ts_end));
  }  
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
  
  $user['user'] = $calendar['user_id'];
  $event_id = $calendar['calendar_id'];
  $e_q = run_query_calendar_detail($event_id);
  $ts_begin = $e_q->f('calendarevent_date');
  $ts_end = $e_q->f('calendarevent_date') + $e_q->f('calendarevent_duration');
  if($e_q->f('all_day') == 1) {
    $ts_begin = strtotime(date('Y-m-d 00:00:00',$ts_begin));
    $ts_end = strtotime(date('Y-m-d 23:59:59',$ts_end));
  }
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
//   - $sel_entity_id : List entitis id ['user'] ['resource']
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_event_update($calendar, $sel_entity_id, &$event_id, $reset_state) {
  global $c_use_connectors, $obm, $l_date;
  global $l_update_event_mail_head,$l_update_event_mail_subject,$l_update_event_mail_body,$l_to,$l_from;
  global $ccalendar_resource_admin, $l_resource_allocated;

  $datebegin = $calendar['date_begin'];
  $dateend = $calendar['date_end'];
  $event_id = $calendar['calendar_id'];
  $groups = $calendar['group'];
  $mail = $calendar['mail'];
  $mail_title = stripslashes($calendar['title']);
  $writable_cal = of_right_entity_for_user('calendar', $obm['uid'], 'write', '', 'userobm');

  if($calendar['task_new_id']) $task_id = $calendar['task_new_id']; 
  else $task_id = $calendar['task_id'];

  // Get the final user list
  $users_array = $sel_entity_id['user'];
  if (is_array($groups)) {
    foreach($groups as $group_id) {
      $user_group_array = array_merge($user_group_array, of_usergroup_get_group_users($group_id));
    }
    $users_array = array_merge($users_array,$user_group_array);
    $users_array = array_unique($users_array);
  }
  if (! is_array($users_array)) {
    $users_array = array($obm["uid"]);
  }

  // Get the final resource list
  $resources_array = $sel_entity_id['resource'];
  if ( (is_array($resources_array)) && (count($resources_array) > 0) ) {
    $resources_array = array_unique($resources_array);
    $writable_res = of_right_entity_for_user('Resource', $obm['uid'], 'write');
  } else {
    $resources_array = array();
  }
  $event_data = run_query_calendar_detail($event_id);
  $old_begin = of_isodate_format($event_data->f('calendarevent_date'),'', 1);
  $old_end = of_isodate_format($event_data->f('calendarevent_date') + $event_data->f('calendarevent_duration'),'',1);

  // Handle Connectors data
  if ($c_use_connectors) {
    $obm_q = new DB_OBM;
    $now = date('Y-m-d H:i:s');
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
	display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_update(1)');
	$retour = $obm_q->query($query);
      }
    }

    // If users have been inserted they must not appear in DeletedCalendarEvent
    if (count($new_users) > 0) {
      foreach($new_users as $u_id) {
	$query = "DELETE FROM DeletedCalendarEvent
      WHERE deletedcalendarevent_event_id = '$event_id'
        AND deletedcalendarevent_user_id = '$u_id'";
	display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_update(2)');
	$retour = $obm_q->query($query);
      }
    }
  }

  // XXXX bizarre les old_end et old_begin semblent etre affectes par l'update
  // ci-dessous sous mysql 4.0.23-10 (portable fourmi)
  run_query_calendar_update_event_data($calendar); 


  if(!$reset_state) {
    $entities = get_calendar_event_entity($event_id); 
    if(count($users_array) > 0) {
      $qusers = "AND NOT (evententity_entity_id IN (".implode(',',$users_array).") AND evententity_entity = 'user')";
    }
    if(count($resources_array) > 0) {
      $rresources = "AND NOT (evententity_entity_id IN (".implode(',',$resources_array).") AND evententity_entity = 'resource')";
    }
    if(is_array($entities['user']['ids'])) {
      $users_array = array_diff($users_array,$entities['user']['ids']);
    }
    if(is_array($entities['resource']['ids'])) {
      $resources_array = array_diff($resources_array, $entities['resource']['ids']);
    }
  }
  
  $query = "DELETE FROM EventEntity WHERE 1=1 $qusers $rresources AND evententity_event_id = '$event_id'";
  $db = new DB_OBM;
  $db->query($query);

  // User insertion
  if(is_array($users_array)) {
    foreach($users_array as $user_id) {
      if ($user_id == $obm['uid'] || in_array($user_id, $writable_cal['ids'])) {
        run_query_calendar_insert_entity_event('user', $user_id, $event_id, 'A');	
      } else {
        run_query_calendar_insert_entity_event('user', $user_id, $event_id, 'W');	
      }
    }
  }

  if (is_array($resources_array)) {
    foreach ($resources_array as $res_id) {
      if (in_array($res_id, $writable_res['ids'])) {
	run_query_calendar_insert_entity_event('resource', $res_id, $event_id, 'A');
      } else {
	run_query_calendar_insert_entity_event('resource', $res_id, $event_id, 'W');
      }
    }
  }
  
  if ($task_id > 0) {
    run_query_calendar_insert_entity_event('task', $task_id, $event_id, 'A');
  }

  // Insert My alert if set
  if (isset($calendar['alert'])) {
    run_query_calendar_event_alert_insert($event_id, $obm['uid'], $calendar['alert']);
  }

}


///////////////////////////////////////////////////////////////////////////////
// Event Data Update Execution
// Parameters: 
//   - $calendar : Calendar params
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_update_event_data($calendar) {
  global $cdg_sql, $obm;

  $now = date('Y-m-d H:i:s');
  $title = $calendar['title'];
  $owner = $calendar['owner'];
  $category1_id = $calendar['category1'];
  $priority = $calendar['priority'];
  $description = $calendar['description'];
  $location = $calendar['location'];
  $datebegin = of_isodate_convert($calendar['date_begin']);
  $event_duration = $calendar['event_duration'];
  ($calendar['all_day'] != 1)?$all_day= '0':$all_day = '1';
  $event_id = $calendar['calendar_id']; 
  if ($calendar['privacy'] != 1) $privacy = 0; else $privacy = 1; 
  $repeat_kind = $calendar['repeat_kind'];
  $repeat_days = $calendar['repeat_days'];
  $repeat_end = of_isodate_convert($calendar['repeat_end']);
  $frequency = $calendar['repeatfrequency']; 
  $color = $calendar['color']; 
  $date_exception = $calendar['date_exception'];
  $item = $calendar['list-item'];
  $item_id = $calendar['list-item-id'];
  $res_name = $calendar['res-name'];
  $res_id = $calendar['res-id'];
  $property = $calendar['property'];
  $property_name = $calendar['property-name'];
  if (is_array($res_name)) {
    foreach($res_name as $key=>$res) {
      $properties .= "<resource>
         <res_name>$res</res_name>
         <res_id>$res_id[$key]</res_id>";

      if (is_array($item) && isset($item[$key])) {
        $properties .= "<item>$item[$key]</item>
          <item_id>$item_id[$key]</item_id>";
      }

      if (is_array($property_name) && isset($property_name[$key])) {
        $properties .= "<property_name>$property_name[$key]</property_name>
                         <property_value>$property[$key]</property_value>";                       
      }
      $properties .= "</resource>";
    }
  }
  $properties = "<extended_desc>$properties</extended_desc>";

  $obm_q = new DB_OBM;
  $query = "UPDATE CalendarEvent SET
    calendarevent_timeupdate = '$now',
    calendarevent_userupdate = '$obm[uid]', 
    calendarevent_owner = '$owner', 
    calendarevent_title = '$title', 
    calendarevent_date = '$datebegin',     
    calendarevent_description = '$description',
    calendarevent_properties = '$properties', 
    calendarevent_location = '$location',
    calendarevent_category1_id = '$category1_id',
    calendarevent_priority = '$priority',
    calendarevent_privacy = '$privacy', 
    calendarevent_duration = '$event_duration',
    calendarevent_allday = '$all_day', 
    calendarevent_color = '$color',
    calendarevent_repeatkind = '$repeat_kind',
    calendarevent_repeatfrequence = '$frequency',
    calendarevent_repeatdays = '$repeat_days',
    calendarevent_endrepeat = '$repeat_end'
  WHERE
    calendarevent_id = '$event_id'";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_event_data()');
  $obm_q->query($query);

  $query = "DELETE FROM CalendarException WHERE calendarexception_event_id = '$event_id'";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_event_data(2)');
  $obm_q->query($query);

  if($repeat_kind != 'none' && is_array($date_exception)) {
    foreach(array_unique($date_exception) as $key => $value) {
      if($value != '') {
        $value = of_isodate_convert($value);
        $calendar['old_date_begin'] = $value.' '.$calendar['time_begin'].':'.$calendar['min_begin'].':00';
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

  $multidomain = sql_multidomain('calendarevent');
  $now = date('Y-m-d H:i:s');
  $title = $calendar['title'];
  $datebegin = $calendar['date_begin'];
  $event_duration = $calendar['duration'];
  $event_id = $calendar['calendar_id']; 
  $obm_q = new DB_OBM;
  $query = "UPDATE CalendarEvent SET
    calendarevent_timeupdate = '$now',
    calendarevent_userupdate = '$obm[uid]', 
    calendarevent_title = '$title', 
    calendarevent_date = '$datebegin',     
    calendarevent_duration = '$event_duration'
  WHERE
  calendarevent_id = '$event_id' $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_event_data()');
  $obm_q->query($query);  
}


/**
 * Event Data  Execution for ajax client 
 * 
 * @param mixed $calendar 
 * @access public
 * @return void
 */
function run_query_calendar_quick_event_insert($calendar) {
  global $cdg_sql, $obm;

  $writable_cal = of_right_entity_for_user('calendar', $obm['uid'], 'write', '', 'userobm');
  $now = date('Y-m-d H:i:s');
  $title = $calendar['title'];
  $datebegin = $calendar['date_begin'];
  $event_duration = $calendar['duration'];
  $all_day = $calendar['all_day'];

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
     calendarevent_properties,
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
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_quick_insert_event_data()');
  $obm_q->query($query);
  $query = "SELECT MAX(calendarevent_id) as max_id FROM CalendarEvent";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_event_data()');
  $obm_q->query($query);
  $obm_q->next_record(); 
  $event_id = $obm_q->f('max_id'); 
  run_query_calendar_insert_entity_event('user', $obm['uid'], $event_id, 'A');

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
function run_query_calendar_event_exception_insert( $calendar, $event_q = '') {
  global $cdg_sql, $obm;

  $now = date('Y-m-d H:i:s');
  $title = $calendar['title'];
  $id = $calendar['calendar_id'];
  $olddatebegin = $calendar['old_date_begin'];
  $event_duration = $calendar['duration'];
  
  $obm_q = new DB_OBM;

  $query = "UPDATE CalendarEvent SET calendarevent_timeupdate = NOW() WHERE calendarevent_id = $id";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_exception_insert(0)');
  $obm_q->query($query);

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

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_exception_insert(1)');
  $obm_q->query($query);
  $query = "Update CalendarEvent SET
    calendarevent_userupdate = '$obm[uid]',
    calendarevent_timeupdate = NOW()
    WHERE
    calendarevent_id = '$id'";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_exception_insert(2)');
  $obm_q->query($query);

  if(is_object($event_q)) {
    if ($event_q->f('calendarevent_privacy')!=1) $privacy = 0; else $privacy = 1; 
    $repeat_kind = $event_q->f('calendarevent_repeatkind');
    $repeat_days = $event_q->f('calendarevent_repeatdays');
    $repeat_end = $event_q->f('calendarevent_endrepeat');
    $repeatfrequency = $event_q->f('calendarevent_repeatfrequence');  
    $category1_id = $event_q->f('calendarevent_category1_id');
    $priority = $event_q->f('calendarevent_priority');
    $color = $event_q->f('calendarevent_color');
    $description = $event_q->f('calendarevent_description');
    $properties = $event_q->f('calendarevent_properties');
    $location = $event_q->f('calendarevent_location');
    ($calendar['all_day'] != 1)?$all_day= '0':$all_day = '1';
    $datebegin = $calendar['date_begin'];

    $query = "INSERT INTO CalendarEvent (
      calendarevent_timeupdate,                         
      calendarevent_timecreate,                         
      calendarevent_usercreate,
      calendarevent_owner,
      calendarevent_title, 
      calendarevent_date,     
      calendarevent_description,
      calendarevent_properties,
      calendarevent_location, 
      calendarevent_category1_id,
      calendarevent_priority,
      calendarevent_privacy, 
      calendarevent_duration,
      calendarevent_repeatkind,
      calendarevent_repeatfrequence,
      calendarevent_repeatdays,
      calendarevent_allday,
      calendarevent_color,
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
      '$properties',
      '$location',
      '$category1_id',
      '$priority',
      '$privacy',
      '$event_duration',		
      'none',
      '$repeatfrequency',
      '$repeat_days',
      '$all_day',
      '$color',
      '$repeat_end',
      '$obm[domain_id]'
    )";

    display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_event_data(3)');
    $obm_q->query($query);
    
    $query = "SELECT MAX(calendarevent_id) as max_id FROM CalendarEvent";
    display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_event_data()');
    $obm_q->query($query);
    $obm_q->next_record(); 
    $new_id = $obm_q->f('max_id');

    $entities = get_calendar_event_entity($id);
    foreach ($entities as $entity => $data) {
      foreach ($data['entity'] as $entity_id => $entity_data) {  
        run_query_calendar_insert_entity_event($entity, $entity_id, $new_id, $entity_data['state']);
      }
    }
  }

  return $new_id;
}


/**
 * Update attendee_participation 
 * 
 * @param mixed $params 
 * @param mixed $eve_q 
 * @access public
 * @return void
 */
function run_query_quick_attendee_update($params, $eve_q) {
  global $obm, $cgp_host;
  global $l_update_event_mail_body, $l_update_event_mail_subject ;

  $event_id = $eve_q->f("calendarevent_id");
  $writable["user"] = of_right_entity_for_user("calendar", $obm["uid"], "write", "", "userobm");
  array_push($writable["user"]["ids"], $obm["uid"]); 
  $writable["resource"] = of_right_entity_for_user("Resource", $obm["uid"], "write");
  $entities = get_calendar_event_entity( $event_id);
  run_query_calendar_delete_event_entity($params,array());
  foreach ($entities as $entity => $data) {
    foreach ($data["entity"] as $entity_id => $entity_data) { 
      if(is_array( $writable[$entity]["ids"]))  {
        if (in_array($entity_id, $writable[$entity]["ids"])) {
          run_query_calendar_insert_entity_event($entity, $entity_id, $event_id, 'A');	
        } else {
          run_query_calendar_insert_entity_event($entity, $entity_id, $event_id, 'W');	
        }     
      }
    }
  }
}

/**
 * Create alerts for an event 
 * 
 * @param $event_id 
 * @param $user_id 
 * @param mixed $alerts 
 * @access public
 * @return void
 */
function run_query_calendar_event_alert_insert($event_id, $user_id, $alerts) {
  global $cdg_sql, $obm;

  $now = date('Y-m-d H:i:s');
  run_query_calendar_event_alert_delete($event_id, $user_id);

  if (! is_array($alerts) && $alerts > 0) {
    $alerts = array($alerts => $alerts);
  }

  if (is_array($alerts) && count($alerts) > 0) {
    $obm_q = new DB_OBM;
    foreach ($alerts as $dur => $label) {
      $query = "INSERT INTO CalendarAlert (
        calendaralert_timecreate,
        calendaralert_usercreate,
        calendaralert_event_id,
        calendaralert_user_id,
        calendaralert_duration)
      VALUES (
        '$now',
        '$obm[uid]',
        '$event_id',
        '$user_id',
        '$dur')";
      display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_alert_insert(0)');
      $obm_q->query($query);
    }
  }

  return true;
}


/**
 * Delete alerts for an (event_id, user_id) 
 * 
 * @param $event_id
 * @param $user_id [Optionnal]
 * @access public
 * @return void
 */
function run_query_calendar_event_alert_delete($event_id, $user_id='') {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;
  
  if ($user_id > 0) {
    $where_user = "AND calendaralert_user_id = '$user_id'";
  }

  $query = "DELETE FROM CalendarAlert
    WHERE calendaralert_event_id = '$event_id'
      $where_user";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_alert_delete()');
  $obm_q->query($query);

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the event can be deleted or modify
// Parameters:
//   - $id : event id
// Returns:
//   true if the event can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_calendar_access($id, $right = 'write') {
  global $l_event_cant_delete, $obm, $delete_ok, $err, $l_insufficient_permission, $l_err_reference;
  
  if(!$id) {
    $err['msg'] = $l_err_reference;
    return false;
  }
  $e = get_calendar_event_info($id);
  if($e['owner'] == $obm['uid']) {
    return true;
  }
  if($right == 'write') {
    $access = of_right_entity_for_user('calendar', $obm['uid'], 'write', array($e['owner']), 'userobm');
  } else {
    if(in_array($obm['uid'],$e['attendees']['user'])) {
      return true;
    }
    $access = of_right_entity_for_user('calendar', $obm['uid'], $right, $e['attendees']['user'], 'userobm');
    if(count($access['ids']) == 0  && is_array($e['attendees']['resource'])) {
      $access = of_right_entity_for_user('Resource', $obm['uid'], $right, $e['attendees']['resource']);
    }
  }
  if(count($access['ids']) == 0) { 
    $err['msg'] = $l_insufficient_permission;
    return false;
  }

  return true;
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
  $event_id = $calendar['calendar_id'];
  $mail = $calendar['mail']; 
  
  $users_array = get_calendar_event_users_info($event_id);

  $multidomain = sql_multidomain('calendarevent');
  $query = "SELECT calendarevent_title
    FROM CalendarEvent
    WHERE calendarevent_id = '$event_id' $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete(1)');
  $obm_q->query($query);
  $obm_q->next_record();
  $title = $obm_q->f('calendarevent_title');

  // Resources display
  $query = "SELECT evententity_entity_id FROM EventEntity WHERE evententity_event_id = '$event_id' AND evententity_entity = 'resource'";
  $obm_q->query($query);
  $resources = array();
  while ($obm_q->next_record()) {
    array_push($resources, $obm_q->f('evententity_entity_id'));
  }
  
  $query = "DELETE FROM CalendarException WHERE calendarexception_event_id = '$event_id'";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete(2)');
  $obm_q->query($query);
  $query = "DELETE FROM EventEntity WHERE evententity_event_id = '$event_id'";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete(3)');
  $obm_q->query($query);
  $query = "DELETE FROM CalendarEvent WHERE calendarevent_id = '$event_id'";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete(4)');
  $obm_q->query($query);

  // Alerts
  run_query_calendar_event_alert_delete($event_id);

  // If connectors in use
  if ($c_use_connectors) {
    $now = date('Y-m-d H:i:s');
    
    // We purge the individual user entries
    $query = "DELETE FROM DeletedCalendarEvent
      WHERE deletedcalendarevent_event_id = '$event_id'";
    display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete()');
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
    display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete()');
    $retour = $obm_q->query($query);
  }

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
    'user' => get_calendar_user_readable(),
    'group' => get_calendar_group_readable(),
    'resource' => get_calendar_resource_readable()
  );

  return $ent;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable calendar users for the current user
///////////////////////////////////////////////////////////////////////////////
function get_calendar_user_readable() {
  global $cdg_sql, $obm, $calendar, $c_all;

  $uid = $obm['uid'];
  $gusers = '';
  if ( (isset($calendar['entity']['group_view']))
       && ($calendar['entity']['group_view'] != $c_all) ) {
    $gusers = of_usergroup_get_group_users($calendar['entity']['group_view']);
  }
  $entity = of_right_entity_for_user('calendar', $uid, 'read', $gusers, 'userobm');

  // Add the user own calendar
  $entity['ids'][] = $uid;
  $obm_q = run_query_calendar_get_user_name(array($uid));
  $obm_q->next_record();
  $id = $obm_q->f('userobm_id');
  $label = $obm_q->f('userobm_lastname') .' '. $obm_q->f('userobm_firstname');
  $entity['entity'][] = array('id' => $id, 'label' => $label);

  return $entity;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable calendar group for the current user
///////////////////////////////////////////////////////////////////////////////
function get_calendar_group_readable() {
  global $cdg_sql, $obm, $ccalendar_public_groups;

  $uid = $obm['uid'];
  $where = sql_obm_entity_privacy('group');
  if (! $ccalendar_public_groups) {
    $where .= " AND group_privacy=1";
  }

  $multidomain = sql_multidomain('group');
  $query = "
    SELECT group_id, group_privacy, group_name
    FROM UGroup
    WHERE $where $multidomain
    ORDER BY group_privacy DESC, group_name";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, 'get_calendar_group_readable()');
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable calendar group for the current user
///////////////////////////////////////////////////////////////////////////////
function get_calendar_resource_readable() {
  global $cdg_sql, $obm, $ccalendar_public_groups;

  $uid = $obm['uid'];
  $entity = of_right_entity_for_user('Resource', $uid, 'read');

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
    $where = '(0';
    foreach($resources as $resource) {
      if(is_numeric($resource)) {
        $where .= ", $resource";
      }
    }
    $multidomain = sql_multidomain('resource');
    $where .= ')';
    $query = "SELECT *
    FROM Resource 
    WHERE resource_id IN $where $multidomain
    ORDER by resource_name";
    display_debug_msg($query, $cdg_sql, 'get_calendar_resource_from_ids()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    while ($obm_q->next_record()) {
      $id = $obm_q->f('resource_id');
      $name = $obm_q->f('resource_name');
      $res['ids'][] = $id;
      $res['entity'][$id] = array('label' => $name);
    }
  } else {
    $res = '';
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

  $multidomain = sql_multidomain('group');
  if (is_array($groups)) {
    $where = '(0';
    foreach($groups as $group) {
      $where .= ", $group";
    }
    $where .= ')';
    $query = "SELECT *
    FROM UGroup
    WHERE 
      group_id IN $where $multidomain
    ORDER by group_name";
    display_debug_msg($query, $cdg_sql, 'get_calendar_group_from_ids()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    while ($obm_q->next_record()) {
      $id = $obm_q->f('group_id');
      $name = $obm_q->f('group_name');
      $res['ids'][] = $id;
      $res['entity'][$id] = array('label' => $name);
    }
  } else {
    $res = '';
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
  $user_q = $entities_q['user'];
  if (is_object($user_q)) {
    while ($user_q->next_record()) {
      $id = $user_q->f('userobm_id');
      $name = $user_q->f('userobm_firstname').' '.$user_q->f('userobm_lastname');
      if ($obm['uid'] == $id) {
      $entity_tab['user'][$id] = array(
        'name'  => $name,
        'class' => 'eventOwner',
        'type'  => 'user',
        'id'    => $user_q->f('userobm_id'));
      } else {
        $entity_tab['user'][$id] = array(
          'name'  => $name,
          'class' => "eventOwner$i",
          'type'  => 'user',
          'id'    => $user_q->f('userobm_id'));
        $i++;
        $i = $i%6;
      }
    }
  }
  $resource_q = $entities_q['resource'];
  if (is_object($resource_q)) {
    while($resource_q->next_record()) {
      $id = $resource_q->f('resource_id');
      $entity_tab['resource'][$id] = array(
        'name'  => $resource_q->f('resource_name'),
        'class' => "eventOwner$i",
        'image' => ${"ico_calendar_user".$i},
        'type'  => 'resource',
        'id'    => $id
      );
      $i++;
      $i = $i%6;
    }
  }
  $resourcegroup_q = $entities_q['resourcegroup'];
  if (is_object($resourcegroup_q)) {
    while($resourcegroup_q->next_record()) {
      $id = $resourcegroup_q->f('resource_id');
      $entity_tab['resourcegroup'][$id] = array(
        'name'  => $resourcegroup_q->f('resource_name'),
        'class' => "calendarEventBg$i",
        'image' => ${"ico_calendar_user".$i},
        'type'  => 'resourcegroup',
        'id'    => $id,
        'group' => $resourcegroup_q->f('resource_group'),
        'groupLabel' => $resourcegroup_q->f('rgroup_name'),
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

  if ($id == '') {
    return false;
  }

  $multidomain = sql_multidomain('calendarevent');
  $query = "SELECT * FROM CalendarEvent 
            LEFT JOIN EventEntity ON evententity_event_id = calendarevent_id
            WHERE calendarevent_id='$id'
            $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_calendar_event_info()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  while($obm_q->next_record()) {
    $e['usercreate'] = $obm_q->f('calendarevent_usercreate');
    $e['owner'] = $obm_q->f('calendarevent_owner');
    if($obm_q->f('evententity_entity_id')) {
      $e['attendees'][$obm_q->f('evententity_entity')][] = $obm_q->f('evententity_entity_id');
    }
  }

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

  if ($id == '') {
    return $users_array;
  }

  $query = "SELECT evententity_entity_id
    FROM EventEntity
    WHERE evententity_event_id = '$id' AND evententity_entity = 'user'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  while ($obm_q->next_record()) {
    $users_array[] = $obm_q->f('evententity_entity_id');
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
  $multidomain = sql_multidomain('userobm');

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

  display_debug_msg($query, $cdg_sql, 'get_calendar_event_entity(1)');
  $u_q = new DB_OBM;
  $u_q->query($query);
  while ($u_q->next_record()) {
    $entity = $u_q->f('evententity_entity');
    $entity_id = $u_q->f('evententity_entity_id');
    $state = $u_q->f('state');
    $label = $u_q->f('userobm_lastname'). ' ' .$u_q->f('userobm_firstname');
    $entities[$entity]['ids'][] = $entity_id;
    $entities[$entity]['entity'][$entity_id] = array('label'=> $label,
						     'state' => $state);
  }
  $multidomain = sql_multidomain('resource');

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

  display_debug_msg($query, $cdg_sql, 'get_calendar_event_entity(2)');
  $r_q = new DB_OBM;
  $r_q->query($query);
  while ($r_q->next_record()) {
    $entity = $r_q->f('evententity_entity');
    $entity_id = $r_q->f('evententity_entity_id');
    $state = $r_q->f('state');
    $label = $r_q->f('resource_name');
    $entities[$entity]['ids'][] = $entity_id;
    $entities[$entity]['entity'][$entity_id] = array('label'=> $label,
						     'state' => $state);
  }
  $multidomain = sql_multidomain('project');

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

  display_debug_msg($query, $cdg_sql, 'get_calendar_event_entity(2)');
  $r_q = new DB_OBM;
  $r_q->query($query);
  while ($r_q->next_record()) {
    $entity = $r_q->f('evententity_entity');
    $entity_id = $r_q->f('evententity_entity_id');
    $state = $r_q->f('state');
    $label = $r_q->f('project_name').'--'.$r_q->f('projecttask_label');
    $entities[$entity]['ids'][] = $entity_id;
    $entities[$entity]['entity'][$entity_id] = array('label'=> $label,
						     'state' => $state);
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

  $title = $calendar['title'];
  $datebegin = $calendar['date_begin'];
  $duration = $calendar['duration'];
  $all_day = $calendar['all_day'];
  if (trim($title) == '') {
    $err['msg'] = $l_fill_title;
    return false;
  }
  
  // Begin Date check
  if (trim($datebegin) == '') {
    $err['msg'] = $l_fill_datebegin;
    return false;
  } else if (preg_match($php_regexp_isodatetime, $datebegin) == 0) {
    $err['msg'] = "$l_datebegin : $l_invalid_date"; 
    return false;
  }
  
  // End Date check
  if((!is_numeric($duration) ||  $duration < 1) && !$all_day) {
    $err['msg'] = $l_err_begin_end;
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
  
  $title = $calendar['title'];
  $datebegin = $calendar['date_begin'];
  $dateend = $calendar['date_end'];
  $repeat_end = $calendar['repeat_end'];
  $kind = $calendar['repeat_kind'];
  $repeat_days = $calendar['repeat_days'];
  $repeatfrequency = $calendar['repeatfrequency'];
  ($calendar['all_day'] != 1)?$all_day= '0':$all_day = '1';

  if (trim($title) == '') {
    $err['msg'] = $l_fill_title;
    return false;
  }
  
  // Begin Date check
  if (trim($datebegin) == '') {
    $err['msg'] = $l_fill_datebegin;
    return false;
  } else if (preg_match($php_regexp_isodatetime, $datebegin) == 0) {
    $err['msg'] = "$l_datebegin : $l_invalid_date"; 
    return false;
  }
  
  // End Date check
  if (trim($dateend) == '') {
    $err['msg'] = $l_fill_dateend;
    return false;
  } else if (preg_match($php_regexp_isodatetime, $dateend) == 0) {
    $err['msg'] = "$l_dateend : $l_invalid_date"; 
    return false;
  }
  
  // Repeatition check
  if ($kind != 'none') {
    // End repeat Date check
    if (($repeat_end != '') && (preg_match($php_regexp_isodate, $repeat_end) == 0)) {
      $err['msg'] = "$l_date_repeatend : $l_invalid_date"; 
      return false;
    }
  }
  
  if (!is_numeric($repeatfrequency) ||  $repeatfrequency < 1) {
    $err['msg'] = $l_err_repeatfrequency;
    return false;
  }

  if ($dateend < $datebegin || ( $all_day !=  1 && $dateend == $datebegin)) {
    $err['msg'] = $l_err_begin_end;
    return false;
  } 
  
  if (trim($repeat_end) != '' && $dateend>$repeat_end && $kind != 'none') {
    $err['msg'] =  $l_err_end_repeat;
    return false;
  }
  
  // If repeat kind is weekly, repeat days must be set
  if ($kind == 'weekly' && $repeat_days == '0000000') {
    $err['msg'] = $l_err_days_repeat;
    return false;
  }

  // If repeat days are set, repeat kind must be weekly
  if ($kind != 'weekly' && $repeat_days != '0000000') {
    $err['msg'] = $l_err_days_repeat_not_weekly;
    return false;
  }

  if ($kind == 'weekly' && strtotime('+ 1 week',strtotime($dateend)) > strtotime($repeat_end)) {
    $err['msg'] = $l_err_weekly_repeat;
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

 $year = date('Y', $timestamp);
 $months = date('n', $timestamp)-1;
 $days = date('j', $timestamp);
 $dayofweek = date('w', $timestamp);
	
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
//   - $day       : day of week ('sun', 'mon',...) at least 3 letters
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
//   - $sel_entity_id   : ['user'] ['resource'] ids
//   - $nb_max          : nb max entities to display
//   - $entity_readable : if given, entities eligibles
///////////////////////////////////////////////////////////////////////////////
function slice_calendar_entities($sel_entity_id, $nb_max, $entity_readable='') {
  global $obm;

  $sel_user_id = $sel_entity_id['user'];
  $sel_group_id = $sel_entity_id['group'];
  $sel_resource_id = $sel_entity_id['resource'];

  // users : slice only if users are selected 
  if (is_array($sel_user_id)) {
    $res_user_id = array();
    if ($action != 'perform_meeting') {
      foreach ($sel_user_id as $u_id) {
        if (is_array($entity_readable)) {
	    if (in_array($u_id, $entity_readable['user']['ids'])) {
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
    if ($action != 'perform_meeting') {
      foreach ($sel_resource_id as $r_id) {
	if (is_array($entity_readable)) {
	    if (in_array($r_id, $entity_readable['resource']['ids'])) {
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
    $res_user_id = array($obm['uid']);
  }

  return array(
    'user' => $res_user_id,
    'group' => $sel_group_id,
    'resource' => $res_resource_id
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
  $delta_thursday = date('w',strtotime("-$num days",strtotime('thursday')))-date('w',strtotime("-$num days",$date));
  $week_num = date('W',strtotime("$delta_thursday days",$date));
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
  $calendarevent_date = sql_date_format($db_type,'calendarevent_date');
  $calendarevent_date_l = sql_date_format($db_type,'calendarevent_date','calendarevent_date');

  // get all users (from groups too)
  $users = run_query_calendar_get_allusers($sel_entity_id['user'], $sel_entity_id['group']);

  if ((is_array($users)) && (count($users) > 0)) {
    $has_user = true;
  }
  if (is_array($sel_entity_id['resource'])
      && (count($sel_entity_id['resource']) > 0) ) {
    $has_resource = true;
  }

  if ($has_user) {
    $where_user .= "( evententity_entity_id IN (";
    foreach($users as $id) {
      $where_user .= "$coma'$id'";
      $coma = ',';
    }
    $where_user.= ')';
    $where_user .= " AND evententity_entity = 'user' )"; 
    $select_user = "userobm_lastname,
      userobm_firstname,";
    $join_user = "LEFT JOIN UserObm ON evententity_entity_id = userobm_id";
  }   	     	    

  if ($has_resource) {
    $coma = '';
    $where_res .= "( evententity_entity_id IN (";
    foreach($sel_entity_id['resource'] as $id) {
      $where_res .= "$coma'$id'";
      $coma = ',';
    }
    $where_res.= ')';
    $where_res .= " AND evententity_entity = 'resource' )"; 
    $select_resource = 'resource_name,';
    $join_resource = "LEFT JOIN Resource ON evententity_entity_id = resource_id";
  }   	     	    

  if ($has_user && $has_resource) {
    $where_ent = "AND ( $where_user OR $where_res)";
  } elseif ($has_user || $has_resource) {
    $where_ent = "AND $where_user $where_res";
  }

  if ($event_id != '') {
    $where_event .= " AND calendarevent_id != '$event_id' ";
  }
  $multidomain = sql_multidomain('calendarevent');

  $day_start = strtotime(date('Ymd 00:00:00',$start));
  $day_end = strtotime(date('Ymd 23:59:59',$end));
  $query = "SELECT
      calendarevent_id,
      calendarevent_title,
      calendarevent_privacy,
      calendarevent_description, 
      calendarevent_properties, 
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
      AND calendarevent_repeatkind = 'none' AND
      (($calendarevent_date > $start - calendarevent_duration AND $calendarevent_date < $end) 
        OR (calendarevent_allday = 1 AND $calendarevent_date >= $day_start - calendarevent_duration AND $calendarevent_date <= $day_end))
      $where_ent
      $where_event
      $multidomain
    ORDER BY calendarevent_date";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_conflict_events(()');
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
function run_query_calendar_no_repeat_events($start,$end, $calendar_entity,$category_filter = '') {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $calendarevent_date = sql_date_format($db_type,'calendarevent_date');
  $calendarevent_date_l = sql_date_format($db_type,'calendarevent_date','calendarevent_date');
  

  $multidomain = sql_multidomain('calendarevent');
  if($category_filter != '') {
    $filter = " AND calendarcategory1_id = '$category_filter' ";
  }  
  if (is_array($calendar_entity['user'])
      && (count($calendar_entity['user']) > 0) ) {
    $has_user = true;
  }
  if (is_array($calendar_entity['resource'])
      && (count($calendar_entity['resource']) > 0) ) {
    $has_resource = true;
  }
  if (is_array($calendar_entity['resourcegroup'])
      && (count($calendar_entity['resourcegroup']) > 0) ) {
    $has_resourcegroup = true;
  }
  
  if ($has_user) {
    $where_user .= "( evententity_entity_id IN (";
    foreach($calendar_entity['user'] as $id => $prop) {
      $where_user .= "$coma'$id'";
      $coma = ',';
    }
    $where_user.= ')';
    $where_user .= " AND evententity_entity = 'user' )"; 
  }   	     	    

  if ($has_resource) {
    $coma = '';
    $where_res .= "( evententity_entity_id IN (";
    foreach($calendar_entity['resource'] as $id => $prop) {
      $where_res .= "$coma'$id'";
      $coma = ',';
    }
    $where_res.= ')';
    $where_res .= " AND evententity_entity = 'resource' )"; 
  }   	     	    

  if ($has_user && $has_resource) {
    $where_ent = "AND ( $where_user OR $where_res)";
  } elseif ($has_user || $has_resource) {
    $where_ent = "AND $where_user $where_res";
  }
  
  if ($has_resourcegroup) {
    $coma = '';
    $where_grp .= "( evententity_entity_id IN (";
    foreach($calendar_entity['resourcegroup'] as $id => $prop) {
      $where_grp .= "$coma'$id'";
      $coma = ',';
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
      calendarevent_properties,
      calendarevent_location,
      calendarevent_repeatfrequence,
      calendarevent_owner,
      evententity_entity_id,
      'resourcegroup' as evententity_entity,
      calendarcategory1_label,
      $calendarevent_date_l,
      calendarevent_duration,
      calendarevent_color,
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
      calendarevent_properties,
      calendarevent_location,
      calendarevent_repeatfrequence,
      calendarevent_owner,
      evententity_entity_id,
      evententity_entity,
      calendarcategory1_label,
      $calendarevent_date_l,
      calendarevent_duration,
      calendarevent_color,
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
      ORDER BY calendarevent_date, evententity_entity, evententity_entity_id
";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_no_repeat_events()');
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a week of users or/and groups
// Parameters:
//   - $calendar          : calendar params
//   - $calendar_entity : array of entities elements to display
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_repeat_events($start, $end, $calendar_entity, $category_filter='') {
  global $cdg_sql;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $calendarevent_endrepeat = sql_date_format($db_type,'calendarevent_endrepeat');
  $calendarevent_date = sql_date_format($db_type,'calendarevent_date');
  $calendarevent_date_l = sql_date_format($db_type,'calendarevent_date','calendarevent_date');
  $calendarevent_endrepeat_l = sql_date_format($db_type,'calendarevent_endrepeat','calendarevent_endrepeat');
  $multidomain = sql_multidomain('calendarevent');
  if($category_filter != '') {
    $filter = " AND calendarcategory1_id = '$category_filter' ";
  }
  if (is_array($calendar_entity['user'])
      && (count($calendar_entity['user']) > 0) ) {
    $has_user = true;
  }
  if (is_array($calendar_entity['resource'])
      && (count($calendar_entity['resource']) > 0) ) {
    $has_resource = true;
  }

  if ($has_user) {
    $where_user .= "( evententity_entity_id IN (";
    foreach($calendar_entity['user'] as $id => $prop) {
      $where_user .= "$coma'$id'";
      $coma = ",";
    }
    $where_user.= ")";
    $where_user .= " AND evententity_entity = 'user' )"; 
  }   	     	    

  if ($has_resource) {
    $coma = '';
    $where_res .= "( evententity_entity_id IN (";
    foreach($calendar_entity['resource'] as $id => $prop) {
      $where_res .= "$coma'$id'";
      $coma = ',';
    }
    $where_res.= ')';
    $where_res .= " AND evententity_entity = 'resource' )"; 
  }   

  if (is_array($calendar_entity['resourcegroup'])
      && (count($calendar_entity['resourcegroup']) > 0) ) {
    $has_resourcegroup = true;
  }

  if ($has_user && $has_resource) {
    $where_ent = "AND ( $where_user OR $where_res)";
  } elseif ($has_user || $has_resource) {
    $where_ent = "AND $where_user $where_res";
  }
  
  if ($has_resourcegroup) {
    $coma = '';
    $where_grp .= "( evententity_entity_id IN (";
    foreach($calendar_entity['resourcegroup'] as $id => $prop) {
      $where_grp .= "$coma'$id'";
      $coma = ',';
    }
    $where_grp.= ')';
    $where_grp .= " AND evententity_entity = 'resource' )"; 
    
    $union = "
      UNION
      SELECT
      calendarevent_id,
      calendarevent_title,
      calendarevent_privacy,
      calendarevent_description, 
      calendarevent_properties, 
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
      calendarevent_color,
      calendarevent_allday
    FROM CalendarEvent
      LEFT JOIN CalendarCategory1
        ON calendarevent_category1_id = calendarcategory1_id
      LEFT JOIN EventEntity ON calendarevent_id = evententity_event_id
   WHERE calendarevent_repeatkind != 'none'
      $filter
      AND $calendarevent_date <= $end 
      AND ($calendarevent_endrepeat >= $start - calendarevent_duration
      OR $calendarevent_endrepeat = '0' OR calendarevent_endrepeat IS NULL)
      AND $where_grp
      $multidomain
    ";
  }

  $query = "SELECT
      calendarevent_id,
      calendarevent_title,
      calendarevent_privacy,
      calendarevent_description, 
      calendarevent_properties, 
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
      calendarevent_color,
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
      OR $calendarevent_endrepeat = '0' OR calendarevent_endrepeat IS NULL)
      $where_ent
      $multidomain
      $union

    ORDER BY calendarevent_date, evententity_entity, evententity_entity_id"; 

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_repeat_events()');
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

  $event_list = implode(',',$events) ;
  if ($event_list == '') {
    $event_list = '0';
  }
  $obm_q = new DB_OBM;

  $multidomain = sql_multidomain('calendarevent');
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

  display_debug_msg($query, $cdg_sql, 'run_query_get_events_attendee()');
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
  $event_list = implode(',',$events) ;
  $obm_q = new DB_OBM;
  $calendarexception_date = sql_date_format($obm_q->type,'calendarexception_date');
  $calendarexception_date_l = sql_date_format($obm_q->type,'calendarexception_date','calendarexception_date');  

  $query = "SELECT
    calendarexception_event_id,
    $calendarexception_date_l
   FROM CalendarException
   WHERE calendarexception_event_id IN ($event_list) 
   AND $calendarexception_date > $start_time 
   AND $calendarexception_date < $end_time";
  display_debug_msg($query, $cdg_sql, 'run_query_get_events_exception()');
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
  $calendarexception_date = sql_date_format($obm_q->type,'calendarexception_date');
  $calendarexception_date_l = sql_date_format($obm_q->type,'calendarexception_date','calendarexception_date');  

  $query = "SELECT
    calendarexception_event_id,
    $calendarexception_date_l
   FROM CalendarException
   WHERE calendarexception_event_id='$calendar_id'"; 

  display_debug_msg($query, $cdg_sql, 'run_query_get_one_event_exception()');
  $obm_q->query($query);

  return $obm_q;
}


/**
 * Get all alerts for an (event, user)
 * 
 * @param $event_id
 * @param $user_id
 * @access public
 * @return $alerts array ('second' => 'label')
 */
function get_one_event_user_alert($event_id, $user_id = null) {
  global $cdg_sql;

  $alerts = array();
  $obm_q = new DB_OBM;
  
  $query = "SELECT
    calendaralert_duration,
    calendaralert_user_id
   FROM CalendarAlert
   WHERE calendaralert_event_id='$event_id' "; 

  if($user_id) {
    $query .= "AND calendaralert_user_id='$user_id'";
  }

  display_debug_msg($query, $cdg_sql, 'get_one_event_user_alert()');
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $dur = $obm_q->f('calendaralert_duration');
    $id = $obm_q->f('calendaralert_user_id');
    $dur_min = $dur / 60;
    $alerts[$id] = $dur;
  }

  return $alerts;
}


///////////////////////////////////////////////////////////////////////////////
// Return a table of Events between two dates
// Parameters: 
//   - $start_time      : timestamp of start date
//   - $end_time        : timestamp of end date
//   - $calendar_entity :
///////////////////////////////////////////////////////////////////////////////
function calendar_events_model($start_time, $end_time, $calendar_entity,$category_filter = '', $unique=false) {
  global $day_duration;

  $day_duration = 86400;
  $nr_q = run_query_calendar_no_repeat_events($start_time,$end_time,$calendar_entity,$category_filter);
  $of = &OccurrenceFactory::getInstance();
  $of->setBegin($start_time);
  $of->setEnd($end_time);
  while ($nr_q->next_record()) {
    $id = $nr_q->f('calendarevent_id');
    $title = $nr_q->f('calendarevent_title');
    $privacy = $nr_q->f('calendarevent_privacy');
    $description = $nr_q->f('calendarevent_description'); 
    $properties = $nr_q->f('calendarevent_properties'); ;
    $location = $nr_q->f('calendarevent_location'); 
    $category1 = $nr_q->f('calendarcategory1_label');
    $priority = $nr_q->f('calendarevent_priority');
    $date = $nr_q->f('calendarevent_date');
    $color = $nr_q->f('calendarevent_color');
    $duration = $nr_q->f('calendarevent_duration');
    $all_day = $nr_q->f('calendarevent_allday');
    $entity = $nr_q->f('evententity_entity');
    $entity_id = $nr_q->f('evententity_entity_id');
    $owner = $nr_q->f('calendarevent_owner');
    if (isset($of->events[$id])) {
      $event = &$of->events[$id];
      if (!$unique) {
        $of->addOccurrence($event, $date, $entity, $entity_id);
      }
    } else {
      $event = &new Event($id,$duration,$title,$location,$category1,$privacy,$description,$properties,$all_day,'none',$owner,$color);
      $of->addOccurrence($event, $date, $entity, $entity_id);
    }
  }
  
  $r_q = run_query_calendar_repeat_events($start_time,$end_time,$calendar_entity,$category_filter);
  while ($r_q->next_record()) {
    $id = $r_q->f('calendarevent_id');
    $title = $r_q->f('calendarevent_title');
    $privacy = $r_q->f('calendarevent_privacy');
    $description = $r_q->f('calendarevent_description'); 
    $properties = $r_q->f('calendarevent_properties'); 
    $location = $r_q->f('calendarevent_location'); 
    $category1 = $r_q->f('calendarcategory1_label');
    $date = $r_q->f('calendarevent_date');
    $duration = $r_q->f('calendarevent_duration');
    $repeatkind = $r_q->f('calendarevent_repeatkind');
    $endrepeat = $r_q->f('calendarevent_endrepeat');
    $entity = $r_q->f('evententity_entity');
    $all_day = $r_q->f('calendarevent_allday');     
    $color = $r_q->f('calendarevent_color');
    $repeatfrequence = $r_q->f('calendarevent_repeatfrequence');
    $repeatdays = $r_q->f('calendarevent_repeatdays');
    $entity_id = $r_q->f('evententity_entity_id');    
    $all_day = $r_q->f('calendarevent_allday');       
    $owner = $r_q->f('calendarevent_owner');
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
      $event = &new Event($id,$duration,$title,$location,$category1,$privacy,$description,$properties,$all_day,$repeatkind,$owner,$color);
    }
    $event_start =  $start_time ;
    $delta = date('H',$date) * 3600 + date('i',$date) * 60 + date('s',$date) + $duration;
    $delta = floor($delta/$day_duration);
    $event_start -= $delta * $day_duration;
    $end_date = ($endrepeat < $end_time) ? $endrepeat : $end_time;
    $end_date += $day_duration;
    if ($addOcc) {
      switch ($repeatkind) {
        case 'daily' :
          calendar_daily_repeatition($date,$event_start,$end_date,$repeatfrequence,&$event,$entity_id,$entity);	
          break; 
        case 'weekly' :
          calendar_weekly_repeatition($date,$event_start,$end_date,$repeatdays,$repeatfrequence,&$event,$entity_id,$entity); 
          break;
        case 'monthlybyday' :
          $stored = calendar_monthlybyday_repeatition($date,$event_start,$end_date,$repeatfrequence,&$event,$entity_id,$entity); 
          break;
        case 'monthlybydate' :
          $stored = calendar_monthlybydate_repeatition($date,$event_start,$end_date,$repeatfrequence,&$event,$entity_id,$entity);
          break;
        case 'yearly' :
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
      $id = $attendee_q->f('calendarevent_id');
      $entity = $attendee_q->f('evententity_entity');
      $entity_id = $attendee_q->f('evententity_entity_id');
      switch($entity) {
        case 'user':
          $entity_label = $attendee_q->f('userobm_lastname').' '.$attendee_q->f('userobm_firstname');
          break;
        case 'resource':
          $entity_label = $attendee_q->f('resource_name');
          break;
        case 'task':
          $entity_label = $attendee_q->f('project_name').'-'.$attendee_q->f('projecttask_label');
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
    $temp_date = strtotime("-$temp_date days +$hour_diff hours +$min_diff minutes", $start_time);
  }
  while ($temp_date < $end_date ) {
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
  $temp_date = strtotime('+'.of_date_get_hour($date).' hours +'.date('i',$date).' minutes',$temp_date);
  if ($temp_date < $start_time) {
    $temp_date = ceil(($start_time - $temp_date) / $day_duration);
    $hour_diff = of_date_get_hour($date) - of_date_get_hour($start_time);
    $min_diff = date('i',$date) - date('i',$start_time);
    $temp_date = ($temp_date % ($repeatfrequence * 7));
    if ($temp_date == '') $temp_date = 0;
    $temp_date = strtotime("-$temp_date days +$hour_diff hours +$min_diff minutes", $start_time);
  }
  $num = (date('w', $temp_date) - date('w', strtotime($ccalendar_weekstart)) +7)%7;
  for ($i=0; $i<strlen($repeatdays);$i++) {;
    $k = ($i+$num)%7;
    $repeat_days[$i]=substr($repeatdays, $k, 1);
  }
  while ($temp_date < $end_date) {
    foreach ($repeat_days as $delta_day => $repeatition) {	  
      if ($repeatition == 1) {
        $current_date = strtotime("+$delta_day days",$temp_date);
        if ($current_date >= $start_time && $current_date < $end_date 
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
  $ts_cur = strtotime(date('Y-m-01',$temp_date));
  while ($ts_cur < $end_date) {
    $start_num = (date('w', $ts_cur) - $start_week +7)%7;
    $day_diff = ($num - $start_num +7)%7;
    $day_diff += 7*($monthlybyday-1);
    $current_date = strtotime("+ $day_diff days +$hour_diff hours +$min_diff minutes",$ts_cur);
    $event_end = $current_date + $event->duration;
    if (of_date_get_month($ts_cur) == of_date_get_month($current_date)
       && $event_end >= $start_time
       && $current_date < $end_date) {
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
    $temp_date = strtotime(date('Y-m-01',$start_time));
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
  $ts_date = strtotime(date('Y-m-'.date('d',$date),$temp_date));
  $ts_date = strtotime("+$hour_diff hours +$min_diff minutes",$ts_date);
  while ($ts_date < $end_date) {
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
    $temp_date = strtotime(date('Y-m-01',$start_time));
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
  $ts_date = strtotime(date('Y-m-'.date('d',$date),$temp_date));
  $ts_date = strtotime("+$hour_diff hours +$min_diff minutes",$ts_date);
  while ($ts_date < $end_date) {
    $of = &OccurrenceFactory::getInstance();
    $of->addOccurrence($event, $ts_date, $entity, $entity_id);
    $ts_date = strtotime("+$repeatfrequence years",$ts_date);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Return my views
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_get_my_views() {
  global $cdg_sql, $obm;
  
  $user_id = $obm["uid"];
  $obm_q = new DB_OBM;

  $query = "SELECT * FROM ObmBookmark
    WHERE obmbookmark_user_id='$user_id'
    AND obmbookmark_entity='calendar'";

  display_debug_msg($query, $cdg_sql, "run_query_calendar_get_my_views()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Construct URL parameters
// Parameters:
//   - $id : OBM bookmark id
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_get_bookmark_property($id) {
  global $cdg_sql, $c_all;

  $obm_q = new DB_OBM;
  $url_param = array();

  $query = "SELECT * FROM ObmBookmarkProperty WHERE obmbookmarkproperty_bookmark_id='$id'";
  display_debug_msg($query, $cdg_sql, "run_query_calendar_get_bookmark_property()");
  $obm_q->query($query);

  while($obm_q->next_record()) {
    $property = $obm_q->f("obmbookmarkproperty_property");
    $value = $obm_q->f("obmbookmarkproperty_value");

    switch($property) {
      case "users":
        $users = explode(",", $value);
        if (is_array($users)) {
          foreach($users as $user) {
            array_push($url_param, "sel_ent[]=data-user-$user");
          }
        } else {
          array_push($url_param, "sel_ent[]=data-user-$value"); 
        }
        break;
      case "resources":
        $resources = explode(",", $value);
        if (is_array($resources)) {
          foreach($resources as $resource) {
            array_push($url_param, "sel_ent[]=data-resource-$resource");
          }
        } else {
          array_push($url_param, "sel_ent[]=data-resource-$value"); 
        }
        break;
      case "group":
        if (empty($value)) {
          array_push($url_param, "new_group=1&amp;group_view=$c_all");
        } else {
          array_push($url_param, "group_id=$value&amp;new_group=1");
        }
        break;
      case "category":
        if (empty($value)) {
          $value = $c_all;
        }
        array_push($url_param, "sel_category_filter=$value");
        break;
      default:
        array_push($url_param, "$property=$value");
        break; 
    }
  }

  array_push($url_param, "view_id=$id");

  $return = implode("&amp;", $url_param);
  return $return;
}

///////////////////////////////////////////////////////////////////////////////
// Insert a calendar view
// Parameters:
//   - $calendar : hashed calendar params
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_insert_view($calendar) {
  global $cdg_sql, $obm;

  $calendar_bookmarkproperties = array("users", "group", "resources", "category", "cal_range", "cal_view");

  $obm_q = new DB_OBM;
  $user_id = $obm["uid"];

  $category = $calendar["category_filter"];
  $label = $calendar["view_label"];
  $group = $calendar["entity"]["group"][0];
  $users = is_array($calendar["entity"]["user"]) ? implode(",", $calendar["entity"]["user"]) : "" ;
  $resources = is_array($calendar["entity"]["resource"]) ? implode(",", $calendar["entity"]["resource"]) : "" ;
  $cal_range = $_SESSION['cal_range'];
  $cal_view = $_SESSION['cal_view'];

  // Insert ObmBookmark
  $query = "INSERT INTO ObmBookmark(
    obmbookmark_user_id,
    obmbookmark_label,
    obmbookmark_entity) VALUES (
    '$user_id',
    '$label',
    'calendar')";
  $obm_q->query($query);

  // Select obmbookmark_id
  $query = "SELECT obmbookmark_id FROM ObmBookmark
    WHERE obmbookmark_user_id='$user_id'
    AND obmbookmark_label='$label'
    AND obmbookmark_entity='calendar'";
  $obm_q->query($query);
  $obm_q->next_record();
  $obmbookmark_id = $obm_q->f("obmbookmark_id");

  // Insert ObmBookmark properties 
  if (is_array($calendar_bookmarkproperties)) {
    foreach($calendar_bookmarkproperties as $property) {
      $query = "INSERT INTO ObmBookmarkProperty (
        obmbookmarkproperty_bookmark_id,
        obmbookmarkproperty_property,
        obmbookmarkproperty_value) VALUES (
        '$obmbookmark_id',
        '$property',
        '".${$property}."')";
      $obm_q->query($query);
    }
  }

  $properties = str_replace("&amp;", "&", run_query_calendar_get_bookmark_property($obmbookmark_id));

  $message = "\"obmbookmark_id\": \"$obmbookmark_id\", \"obmbookmark_label\":\"$label\", \"obmbookmarkproperties\":\"$properties\"";
  
  return $message;
}


///////////////////////////////////////////////////////////////////////////////
// Delete a calendar view
// Parameters:
//   - $calendar : hashed calendar params
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_delete_view($calendar) {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  $id = $calendar["view_id"];

  // Delete view
  $query = "DELETE FROM ObmBookmark WHERE obmbookmark_id='$id'";
  $obm_q->query($query);

  // Delete view properties
  $query = "DELETE FROM ObmBookmarkProperty WHERE obmbookmarkproperty_bookmark_id='$id'";
  $obm_q->query($query);

  $message = "\"obmbookmark_id\": \"$id\"";
  
  return $message;
}


/**
 * Describe an Event 
 * 
 */
Class Event {
  
  var $id;
  var $duration;
  var $title;
  var $location;
  var $category1;
  var $privacy;
  var $description;
  var $properties;
  var $attendee;
  var $repeat;
  var $all_day;
  var $owner;
  var $color;

  /**
   * Event 
   * 
   * @param mixed $id 
   * @param mixed $duration 
   * @param mixed $title 
   * @param mixed $location 
   * @param mixed $category1 
   * @param mixed $privacy 
   * @param mixed $description 
   * @param mixed $properties 
   * @param mixed $all_day 
   * @param mixed $repeat 
   * @param mixed $owner 
   * @access public
   * @return void
   */
  function Event($id,$duration,$title,$location,$category1,$privacy,$description,$properties,$all_day,$repeat,$owner,$color) {
    $this->id = $id;
    $this->duration = $duration;
    $this->title = $title;
    $this->location = $location;
    $this->category1 = $category1;
    $this->privacy = $privacy;
    $this->description = $description;
    $this->properties = $properties;
    $this->repeat = $repeat;
    $this->all_day = $all_day;
    $this->attendee = array();
    $this->owner = $owner;
    $this->color = $color;
  }

  /**
   * Return true if the event is a meeting 
   * 
   * @access public
   * @return boolean
   */
  function isMeeting() {
    return (count($this->attendee['user']) > 1);
  }

  /**
   * Return true if the event is periodic 
   * 
   * @access public
   * @return boolean 
   */
  function isPeriodic() {
    return ($this->repeat != 'none');
  }

  /**
   * Add an attendee to the event 
   * 
   * @param mixed $entity 
   * @param mixed $entity_id 
   * @param mixed $entity_label 
   * @access public
   * @return void
   */
  function add_attendee($entity,$entity_id,$entity_label) {
    $this->attendee[$entity][$entity_id] = $entity_label;
  }

}

/**
 * Describe an occurrence of an event 
 */
Class Occurrence {
  var $event;
  var $date;
  var $entity;
  var $id;

  /**
   * Occurrence 
   * 
   * @param mixed $event 
   * @param mixed $date 
   * @param mixed $entity 
   * @param mixed $id 
   * @access public
   * @return void
   */
  function Occurrence(&$event, $date, $entity, $id) {
    $this->event = &$event;
    $this->date = $date;
    $this->entity = $entity;
    $this->id = $id;
  }

}

/**
 * Store all Event/Occurence/Date relations 
 */
Class OccurrenceFactory {

  var $occurrences;
  var $events;
  var $begin;
  var $end;

  function OccurrenceFactory() {
    $this->occurrences = array();
    $this->events = array();
  }
  
  /**
   * set the beginning of the period 
   * 
   * @param timestamp $begin 
   * @access public
   * @return void
   */
  function setBegin($begin) {
    $this->begin = $begin;
  }

  /**
   * set the end of the period 
   * 
   * @param timestamp $end 
   * @access public
   * @return void
   */
  function setEnd($end) {
    $this->end = $end;
  }

  /**
   * add an occurence for a given event 
   * 
   * @param Event $event 
   * @param timestamp $date 
   * @param string $entity 
   * @param id $id 
   * @access public
   * @return void
   */
  function addOccurrence(&$event, $date, $entity, $id) {
    if ($date < $this->end && ($date + $event->duration) > $this->begin) {
      $this->occurrences[$event->id][] = &new Occurrence($event, $date, $entity, $id);
      $this->events[$event->id] = &$event;
    }
  }

  /**
   * remove all occurence of an event 
   * 
   * @param event id $id 
   * @param timestamp $date 
   * @access public
   * @return void
   */
  function removeOccurrences($id, $date) {
    foreach($this->occurrences[$id] as $key => $occurrence) {
      if(date('Ymd',$date) ==  date('Ymd',$occurrence->date)) {
        unset($this->occurrences[$id][$key]); 
      }
    }
  }

  /**
   * Returl all occurence
   * 
   * @access public
   * @return array
   */
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

  /**
   * Return true if an event have a least one occurrence 
   * 
   * @param event id $id 
   * @access public
   * @return boolean
   */
  function eventHaveOccurences($id) {
    return isset($this->events[$id]);
  }

  /**
   * Return true if there is at leat one occurrence of an
   * event in a given time period
   * 
   * @param timestamp $time 
   * @param int $duration 
   * @param HashMap $entityFilter 'entityKind' => [entity_ids,..] if set only
   * count occurrence of the given entityKind and if set the given ids              
   * @access public
   * @return boolean
   */
  function periodHaveOccurrences($time, $duration, $entityFilter=null) {
    foreach($this->occurrences as $evtOccurrences) {
      foreach($evtOccurrences as $occurence) {
        if(!$entityFilter || ( 
           is_array($entityFilter[$occurence->entity]) && (
             count($entityFilter[$occurence->entity]) == 0 || 
             in_array($occurence->id,$entityFilter[$occurence->entity])))) {
          $eventDuration = $occurence->event->duration;
          $eventDate = $occurence->date;
          $eventAllDay = $occurence->event->all_day;
          if(($eventAllDay && date('Ymd',$time) == date('Ymd',$eventDate)) 
              || ($time < ($eventDate + $eventDuration) && ($time + $duration) > $eventDate)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Return occurrences of an event in a given time period
   * 
   * @param timestamp $time 
   * @param int $duration 
   * @param HashMap $entityFilter 'entityKind' => [entity_ids,..] if set only
   * count occurrence of the given entityKind and, if set, the given ids     
   * @param int $eventFilter 0:All events;1:All events but all day events;
   * 2: Only all day events
   * @access public
   * @return boolean
   */
  function getPeriodOccurrences($time, $duration, $entityFilter=null, $eventFilter=0) {
    $occurences = array();
    foreach($this->occurrences as $evtOccurrences) {
      foreach($evtOccurrences as $occurence) {
        if(!$entityFilter || ( 
           is_array($entityFilter[$occurence->entity]) && (
             count($entityFilter[$occurence->entity]) == 0 || 
             in_array($occurence->id,$entityFilter[$occurence->entity])))) {
          $eventDuration = $occurence->event->duration;
          $eventDate = $occurence->date;
          $eventAllDay = $occurence->event->all_day;
          if(($eventFilter == 0 || ($eventFilter == 1 && !$eventAllDay) || ($eventFilter == 2 && $eventAllDay))
            && (($eventAllDay && date('Ymd',$time) == date('Ymd',$eventDate)) 
            || ($time < ($eventDate + $eventDuration) && ($time + $duration) > $eventDate))) {
            if($eventAllDay) {
              array_unshift($occurences,$occurence);
            } else {
              array_push($occurences,$occurence);
            }
          }
        }
      }
    }
    if(count($occurence) > 0) {
      return $occurences;
    } else {
      return NULL;
    }
  }
}

/**
 * Difference between database values and params values for an event 
 * 
 * @param mixed $calendar
 * @access public
 * @return void
 */
function run_query_prepare_event_mail($calendar, $action, $event_q = null) {
  global $cdg_sql;
  global $l_add_event_mail_subject, $l_update_event_mail_subject, $l_delete_event_mail_subject;
  global $l_add_event_mail_body, $l_update_event_mail_body, $l_delete_event_mail_body;
  global $l_add_resource_mail_body, $l_update_resource_mail_body, $l_delete_resource_mail_body;
  global $l_add_exception_mail_body, $l_add_exception_mail_subject;
  global $l_delete_exception_mail_body, $l_delete_exception_mail_subject;
  global $l_update_state_mail_subject, $l_update_state_mail_body;
  global $l_add_resource_mail_body;
  global $cgp_host, $ccalendar_send_ics, $obm;

  $mail_datas = array('reset_state' => false, 'reset_resource_state' => false, 
                'add_attendee' => false, 'remove_attendee' => false,
                'add_exception' => false, 'remove_exception' => false,
                'update_decision' => false, 'add_resource' => false,
                'remove_resource' => false, 'mails' => array());
  
  $accept_link ="$cgp_host/calendar/calendar_index.php?action=update_decision&calendar_id=$calendar[calendar_id]&entity_kind=user&rd_decision_event=A";
  $refuse_link ="$cgp_host/calendar/calendar_index.php?action=update_decision&calendar_id=$calendar[calendar_id]&entity_kind=user&rd_decision_event=R";
  $consult_link = "$cgp_host/calendar/calendar_index.php?action=detailconsult&calendar_id=$calendar[calendar_id]";
  
  $userinfo = get_user_info();
  
  if($action == 'update') {
    if(!$event_q) {
      $event_q = run_query_calendar_detail($calendar['calendar_id']);
    }
    
    $old_title = $event_q->f('calendarevent_title');
    $title = $calendar['title'];
    $old_datebegin = of_datetime_format($event_q->f('calendarevent_date'));
    $datebegin =  of_datetime_format(strtotime($calendar['date_begin']));
    $old_dateend = of_datetime_format($event_q->f('calendarevent_date') + $event_q->f('calendarevent_duration'));
    $dateend = of_datetime_format(strtotime($calendar['date_end']));
    $old_location = $event_q->f('calendarevent_location');
    $location = $calendar['location'];
    $old_all_day = $event_q->f('calendarevent_allday');
    ($calendar['all_day'] != 1)?$all_day= '0':$all_day = '1';
    $old_duration = $event_q->f('calendarevent_duration');
    $duration = $calendar['event_duration'];
    $old_repeatkind = $event_q->f('calendarevent_repeatkind');
    $repeatkind = $calendar['repeat_kind'];
    $old_repeatdays = $event_q->f('calendarevent_repeatdays');
    $repeatdays = $calendar['repeat_days'];
    $old_repeatend = of_datetime_format($event_q->f('calendarevent_endrepeat'));
    $repeatend = of_datetime_format(strtotime($calendar['repeat_end']));
    $old_repeatfrequence = $event_q->f('calendarevent_repeatfrequence');
    $repeatfrequence = $calendar['repeatfrequency'];
    $owner = $event_q->f('owner_lastname').' '.$event_q->f('owner_firstname');    

    $mail_datas['reset_state'] = (($location != $old_location) || $mail_datas['reset_state']);
    $mail_datas['reset_state'] = (($all_day != $old_all_day) || $mail_datas['reset_state']);
    $mail_datas['reset_state'] = (($datebegin != $old_datebegin) || $mail_datas['reset_state']);
    $mail_datas['reset_state'] = (($duration != $old_duration) || $mail_datas['reset_state']);
    $mail_datas['reset_state'] = (($repeatkind != $old_repeatkind) || $mail_datas['reset_state']);
    $mail_datas['reset_state'] = (($repeatdays != $old_repeatdays) || $mail_datas['reset_state']);
    $mail_datas['reset_state'] = (($repeatend != $old_repeatend) || $mail_datas['reset_state']);
    $mail_datas['reset_state'] = (($repeatfrequence != $old_repeatfrequence) || $mail_datas['reset_state']);

    $date_exception = array();
    $exceptions = array(); 
    $p_exceptions = array();
    $new_exceptions = array();
    $old_exceptions = array();
    $exceptions_q = run_query_get_one_event_exception($calendar['calendar_id']);
    $entities = get_calendar_event_entity($calendar['calendar_id']); 
    
    
    $attendees_q = $entities["user"]["ids"];
    if(is_array($entities["resource"]["ids"])) {
      $resources_q = $entities["resource"]["ids"];
    } else {
      $resources_q = array();
    }
    
    $attendees = array_intersect($calendar['sel_user_id'], $attendees_q);
    $attendees = array_diff($attendees, array($obm['uid']));
    $old_attendees = array_diff($attendees_q,$calendar['sel_user_id'], array($obm['uid']));
    $new_attendees = array_diff($calendar['sel_user_id'],$attendees_q, array($obm['uid']));
    if(is_array($calendar['sel_resource_id'])) {
      $resources = array_intersect($calendar['sel_resource_id'], $resources_q);
      $old_resources = array_diff($resources_q,$calendar['sel_resource_id']);
      $new_resources = array_diff($calendar['sel_resource_id'],$resources_q);
    } else {
      $resources = array();
      $old_resources = $resources_q;
      $new_resources = array();
    }
  
    $mail_datas['reset_resource_state'] = $mail_datas['reset_state'] && (count($resources) > 0);
    $mail_datas['reset_state'] = $mail_datas['reset_state'] && (count($attendees) > 0);
    if(count($old_attendees) > 0) {
      $mail_datas['remove_attendee'] = true;
    }
    if(count($new_attendees) > 0) {
      $mail_datas['add_attendee'] = true;
    }
    if(count($old_resources) > 0) {
      $mail_datas['remove_resource'] = true;
    }
    if(count($new_resources) > 0) {
      $mail_datas['add_resource'] = true;
    }
    
    if(!$mail_datas['reset_state']) {
      if(is_array($calendar['date_exception'])) {
        $date_exception = array_unique($calendar['date_exception']);
      }
      while($exceptions_q->next_record()) {
        $exceptions[] = of_date_format($exceptions_q->f('calendarexception_date'));
      }
      foreach($date_exception as $value) {
        if($value != '') {
          $p_exceptions[] = $value; 
        }
      }
      $old_exceptions = array_diff($exceptions,$p_exceptions);
      $new_exceptions = array_diff($p_exceptions,$exceptions);
      if(count($old_exceptions) > 0) {
        $mail_datas['remove_exception'] = true;
      }
      if(count($new_exceptions) > 0) {
        $mail_datas['add_exception'] = true;
      }
    }
  } elseif ($action == 'insert') {
    if (is_array($calendar['sel_user_id'])) {
      $new_attendees = array_diff($calendar['sel_user_id'], array($obm['uid']));
      $mail_datas['add_attendee'] = true;
    }
    if(is_array($calendar['sel_resource_id'])) {
      $new_resources = $calendar['sel_resource_id'];
      $mail_datas['add_resource'] = true;
    }
    $title = $calendar['title'];
    $datebegin =  of_datetime_format(strtotime($calendar['date_begin']));
    $dateend = of_datetime_format(strtotime($calendar['date_end']));
    $location = $calendar['location'];
  } elseif ($action == 'delete') {
    $event_q = run_query_calendar_detail($calendar['calendar_id']);
    $old_title = $event_q->f('calendarevent_title');
    $old_datebegin = of_datetime_format($event_q->f('calendarevent_date'));
    $old_dateend = of_datetime_format($event_q->f('calendarevent_date') + $event_q->f('calendarevent_duration'));
    $old_location = $event_q->f('calendarevent_location');
    $entities = get_calendar_event_entity($calendar['calendar_id']); 
    $old_resources = $entities['resource']['ids'];
    if(is_array($old_resources)) {
      $mail_datas["remove_resource"] = true;
    }
    $old_attendees = array_diff($entities['user']['ids'], array($obm['uid']));
    $mail_datas['remove_attendee'] = true;    
  } elseif ($action == 'quick_delete') {
    if($event_q->f('calendarevent_repeatkind') != 'none') {
      $title = $event_q->f('calendarevent_title');
      $new_exceptions[] = $calendar['old_date_begin'];  
      $entities = get_calendar_event_entity($calendar['calendar_id']); 
      $old_resources = $entities['resource']['ids'];
      $attendees = array_diff($entities['user']['ids'], array($obm['uid']));
      $mail_datas['add_exception'] = true;
    } else {
      $old_title = $event_q->f('calendarevent_title');
      $old_datebegin = of_datetime_format($event_q->f('calendarevent_date'));
      $old_dateend = of_datetime_format($event_q->f('calendarevent_date') + $event_q->f('calendarevent_duration'));
      $old_location = $event_q->f('calendarevent_location');
      $entities = get_calendar_event_entity($calendar['calendar_id']); 
      $old_resources = $entities['resource']['ids'];
      if(is_array($old_resources)) {
        $mail_datas["remove_resource"] = true;
      }
      $old_attendees = array_diff($entities['user']['ids'], array($obm['uid']));
      $mail_datas['remove_attendee'] = true;    
    }
  } elseif ($action == 'quick_update') {
    $old_title = $event_q->f('calendarevent_title');
    $title = $calendar['title'];
    $old_datebegin = of_datetime_format($event_q->f('calendarevent_date'));
    $datebegin =  of_datetime_format(strtotime($calendar['date_begin']));
    $old_duration = $event_q->f('calendarevent_duration');
    $duration = $calendar['duration'];
    $old_dateend = of_datetime_format($event_q->f('calendarevent_date') + $event_q->f('calendarevent_duration'));
    $dateend = of_datetime_format(strtotime($calendar['date_begin']) + $duration);
    $mail_datas['reset_state'] = (($datebegin != $old_datebegin) || $mail_datas['reset_state']);
    $mail_datas['reset_state'] = (($duration != $old_duration) || $mail_datas['reset_state']);
    $entities = get_calendar_event_entity($calendar['calendar_id']); 
    $resources = $entities['resource']['ids'];
    $attendees = array_diff($entities['user']['ids'], array($obm['uid']));
    $mail_datas['reset_resource_state'] = $mail_datas['reset_state'] && (count($resources) > 0);
  } elseif ($action == 'update_decision' || $action == 'decision') {
    if(!$event_q) {
      $event_q = run_query_calendar_detail($calendar['calendar_id']);
    }    
    if($calendar['entity_kind'] != 'user' || $event_q->f('calendarevent_owner') != $obm['uid']) {
      $decision = $calendar['decision_event'];
      $event_q = run_query_calendar_detail($calendar['calendar_id']);
      $title = $event_q->f('calendarevent_title');
      $datebegin = of_datetime_format($event_q->f('calendarevent_date'));
      $entityinfo = get_entity_info($calendar['entity_id'],$calendar['entity_kind']);
      $entity =  $entityinfo['lastname'].' '.$entityinfo['firstname'];
      $attendees = array($event_q->f('calendarevent_owner'));
      $mail_datas['update_decision'] = true;
    }
  }
  if($mail_datas['reset_state']) {
    $mail_datas['mails']['reset_state'] = array (
      'subject' => sprintf($l_update_event_mail_subject,$old_title),
      'message' => sprintf($l_update_event_mail_body, $old_title, $old_datebegin, 
                           $old_dateend, $old_location, $datebegin, $dateend, 
                           $location,$consult_link,$accept_link,$refuse_link,
                           of_date_format(), date("H:i"), $userinfo["firstname"],
                           $userinfo["lastname"],$cgp_host),
      'recipients' => $attendees
    ) ;
  }
  if($mail_datas['reset_resource_state']) {
    $rattendees =  of_right_users_for_entity('resource', $resources, 'write');
    $rattendees = array_diff($rattendees["ids"], array($obm['uid']));
    $mail_datas['mails']['reset_resource_state'] = array (
      'subject' => sprintf($l_update_event_mail_subject,$old_title),
      'message' => sprintf($l_update_resource_mail_body, $old_title, $old_datebegin, 
                           $old_dateend, $old_location, $datebegin, $dateend, 
                           $location,$consult_link,
                           of_date_format(), date("H:i"), $userinfo["firstname"],
                           $userinfo["lastname"],$cgp_host),
      'recipients' => $rattendees
    ) ;
  }   
  if ($mail_datas['add_attendee']) {
    if ($ccalendar_send_ics) {
      $tmp_filename = secure_tmpname('.ics','ics_');
      create_calendar_ics_meeting_file($calendar, $calendar['calendar_id'], $new_attendees, array(), $tmp_filename);
      $h_file = array(
        array (
               'filename' => "$tmp_filename",
               'name' => 'meeting.ics',
               'type' => 'ics'
               )
        );
    }
    $mail_datas['mails']['add_attendee'] = array (
      'subject' => sprintf($l_add_event_mail_subject,$title),
      'message' => sprintf($l_add_event_mail_body, $datebegin, $dateend, 
                           $title, $location, $consult_link,$accept_link,$refuse_link,
                           of_date_format(), date("H:i"), $userinfo["firstname"],
                           $userinfo["lastname"],$cgp_host),
      'recipients' => $new_attendees,
      'h_file' => $hfile
    ) ;
  }
  if ($mail_datas['add_resource']) {
    $rattendees =  of_right_users_for_entity('resource', $new_resources, 'write');
    $rattendees = array_diff($rattendees["ids"], array($obm['uid']));    
    $mail_datas['mails']['add_resource'] = array (
      'subject' => sprintf($l_add_event_mail_subject,$title),
      'message' => sprintf($l_add_resource_mail_body, $datebegin, $dateend, 
                           $title, $location, $consult_link,
                           of_date_format(), date("H:i"), $userinfo["firstname"],
                           $userinfo["lastname"],$cgp_host),
      'recipients' => $rattendees,
      'h_file' => $hfile
    ) ;

  }
  if ($mail_datas['remove_attendee']) {
    $mail_datas['mails']['remove_attendee'] = array (
      'subject' => sprintf($l_delete_event_mail_subject,$old_title),
      'message' => sprintf($l_delete_event_mail_body, $old_title, $old_datebegin, 
                           $old_dateend, $old_location,$userinfo["email"], 
                           of_date_format(), date("H:i"), $userinfo["firstname"],
                           $userinfo["lastname"],$cgp_host),
      'recipients' => $old_attendees
    ) ;
  }
  if ($mail_datas['remove_resource']) {
    $rattendees =  of_right_users_for_entity('resource', $old_resources, 'write');
    $rattendees = array_diff($rattendees["ids"], array($obm['uid']));    
    $mail_datas['mails']['remove_resource'] = array (
      'subject' => sprintf($l_delete_event_mail_subject,$old_title),
      'message' => sprintf($l_delete_resource_mail_body, $old_title, $old_datebegin, 
                           $old_dateend, $old_location,$userinfo["email"], 
                           of_date_format(), date("H:i"), $userinfo["firstname"],
                           $userinfo["lastname"],$cgp_host),
      'recipients' => $rattendees
    ) ;
  }  
  if ($mail_datas['add_exception']) {
    $rattendees =  of_right_users_for_entity('resource', $resources, 'write');
    $rattendees = array_diff($rattendees["ids"], array($obm['uid']));    
    $attendees = array_merge($attendees, $rattendees);    
    $mail_datas['mails']['add_exception'] = array (
      'subject' => sprintf($l_add_exception_mail_subject,$title),
      'message' => sprintf($l_add_exception_mail_body,$title,implode(', ', $new_exceptions),$consult_link, 
                           of_date_format(), date("H:i"), $userinfo["firstname"],
                           $userinfo["lastname"],$cgp_host),
      'recipients' => $attendees
    ) ;
  }
  if ($mail_datas['remove_exception']) {
    $rattendees =  of_right_users_for_entity('resource', $resources, 'write');
    $rattendees = array_diff($rattendees["ids"], array($obm['uid']));    
    $attendees = array_merge($attendees, $rattendees);        
    $mail_datas['mails']['remove_exception'] = array (
      'subject' => sprintf($l_delete_exception_mail_subject,$title),
      'message' => sprintf($l_delete_exception_mail_body,$title,implode(', ', $old_exceptions),$consult_link, 
                           of_date_format(), date("H:i"), $userinfo["firstname"],
                           $userinfo["lastname"],$cgp_host),
      'recipients' => $attendees
    ) ;
  }
  if ($mail_datas['update_decision']) {
    $mail_datas['mails']['update_decision'] = array (
      'subject' => sprintf($l_update_state_mail_subject,$title),
      'message' => sprintf($l_update_state_mail_body,$entity,$title,$decision,$consult_link, 
                           of_date_format(), date("H:i"), $userinfo["firstname"],
                           $userinfo["lastname"],$cgp_host),
      'recipients' => $attendees
    ) ;
  }

  return $mail_datas;
}

function calendar_send_mail($mails) {
  foreach($mails['mails'] as $mail) {
    send_mail($mail['subject'], $mail['message'], $mail['recipients'], array(), $mail, $mail['h_file']);
  }
}
?>
