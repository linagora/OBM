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

  $calendar_id = sql_parse_id($calendar_id, true);
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type; 
  $timeupdate = sql_date_format($db_type, 'event_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'event_timecreate', 'timecreate');
  $multidomain = sql_multidomain('event');
  $query = "SELECT DISTINCT
      event_id,
      c.userobm_login AS usercreate,
      u.userobm_login AS userupdate,
      $timeupdate,
      $timecreate,
      event_owner,
      o.userobm_lastname AS owner_lastname,
      o.userobm_firstname AS owner_firstname,
      event_title,
      event_description,
      event_properties,
      event_repeatfrequence,
      event_location,
      event_category1_id,
      eventcategory1_label,
      event_privacy,
      event_priority,
      event_repeatkind,
      event_duration,
      event_repeatdays,
      event_allday,
      event_color,
      event_endrepeat,
      event_date
    FROM  
      Event
      LEFT JOIN EventCategory1 ON event_category1_id = eventcategory1_id
      LEFT JOIN UserObm AS c ON event_usercreate = c.userobm_id
      LEFT JOIN UserObm AS u ON event_userupdate = u.userobm_id
      LEFT JOIN UserObm AS o ON event_owner = o.userobm_id
    WHERE event_type='VEVENT'
      AND event_id $calendar_id
      $multidomain";

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

  $in = "'".implode (",", $users)."'";
  $in = implode (",", $users);
  
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
    $in = "'".implode (",", $resources)."'";
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
  $r_id = sql_parse_id( $r_id, true);
  $multidomain = sql_multidomain('resource');
  $obm_q = new DB_OBM;
  $query = "SELECT resourcetype_property 
    FROM ResourceType 
    LEFT JOIN Resource ON resource_rtype_id=resourcetype_id
    WHERE resource_id $r_id $multidomain";

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
// Query Execution : Get the resources of the given group                    //
// Parameters:
//   - $group[] : Group parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_get_group_resource($group) {

  $obm_q = run_query_resource_resourcegroup($group);
  $resources = array();
  if(is_object($obm_q)) {
    while($obm_q->next_record()) {
      $resources[] = $obm_q->f('resource_id');
    }
  }
  return $resources;
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

  run_query_calendar_update_occurrence_state($event_id,$entity,$entity_id,$state);
}


///////////////////////////////////////////////////////////////////////////////
// Update a user decision for its participation to an event
// Parameters: 
//   - $event_id : event Id
//   - $user_id  : User id
//   - $state    : New state to set (ACCEPTED, NEEDS-ACTION or DECLINED)
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_update_occurrence_state($event_id, $entity, $entity_id,$state, $force = false) {
  global $cdg_sql;
  global $obm, $err, $l_of_right_err_auth;

  if (!$force && ($obm['uid'] != $entity_id || $entity != 'user')) {
    if ($entity == 'user') {
      $writable = of_right_entity_for_user('calendar', $obm['uid'], 'write', '', 'userobm');
    } elseif ($entity == 'resource') {
      $writable = of_right_entity_for_user('Resource', $obm['uid'], 'write');
    }
    
    if (!in_array($entity_id, $writable['ids'])) {
      $err['msg'] = $l_of_right_err_auth;  
      return false;
    } 
  }

  $event_id = sql_parse_id($event_id, true);
  $entity_id = sql_parse_id($entity_id, true);
  $query = "UPDATE EventEntity 
            SET evententity_state = '$state'
            WHERE evententity_entity_id  $entity_id 
              AND evententity_entity = '$entity'
              AND evententity_event_id $event_id"; 

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_occurrence_state()');  
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    $now = date('Y-m-d H:i:s');
    $query = "UPDATE Event
      SET event_timeupdate = '$now'
      WHERE event_id $event_id";

    display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_occurrence_state()');
    $retour = $obm_q->query($query);
  }
  
  return $retour;    
}


/**
 * Check if the user participation to en event differs from the one given
 *
 * @param
 **/
function check_calendar_event_participation($params) {
  global $obm;

  $event_id = $params['calendar_id'];
  $entity = 'user';
  $entity_id = $obm['uid'];
  $state = $params['decision_event'];

  $current_state = get_calendar_event_participation_state($event_id, $entity, $entity_id);
  if ($state == $current_state) {
    return false;
  } else {
    return true;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Update a user decision for its participation to an event
// Parameters: 
//   - $event_id : event Id
//   - $user_id  : User id
//   - $state    : New state to set (ACCEPTED, NEEDS-ACTION or DECLINED)
///////////////////////////////////////////////////////////////////////////////
function get_calendar_event_participation_state($event_id, $entity, $entity_id) {
  global $cdg_sql;

  $query = "SELECT evententity_state
    FROM EventEntity 
    WHERE evententity_entity_id = $entity_id
      AND evententity_entity = '$entity'
      AND evententity_event_id = '$event_id'";

  display_debug_msg($query, $cdg_sql, 'get_calendar_event_participation_state()');  
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  if ($obm_q->num_rows() == 1) {
    $obm_q->next_record();
    $state = $obm_q->f('evententity_state');
    return $state;
  } else {
    return false;
  }
}


/**
 * Select All waiting Events
 **/
function run_query_calendar_waiting_events() {
  global $obm, $cdg_sql;

  $uid = $obm['uid'];
  
  $multidomain = sql_multidomain('event');
  $writable_cal = of_right_entity_for_user('calendar', $uid, 'write', '', 'userobm');
  $writable_res = of_right_entity_for_user('Resource', $uid, 'write');
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $concat = array (
    array (
      'type' => 'field',
      'value' => 'UserObm.userobm_lastname'
      ),
    array (
      'type' => 'string',
      'value' => ' '
      ),
    array (
      'type' => 'field',
      'value' => 'UserObm.userobm_firstname'
      ) 
    );
  $entity_label = sql_string_concat($db_type,$concat);

  $query = "SELECT
      u.userobm_lastname as usercreate_lastname,
      u.userobm_firstname as usercreate_firstname,
      $entity_label AS entity_label,
      UserObm.userobm_id AS entity_id,
      'user' AS entity_entity,
      event_id,
      event_title,
      event_location,
      eventcategory1_label,
      event_privacy,
      event_priority,
      event_repeatkind,
      event_repeatdays,
      event_duration,
      event_endrepeat,
      event_date
    FROM Event
      LEFT JOIN EventCategory1 ON event_category1_id = eventcategory1_id
      LEFT JOIN EventEntity ON event_id  = evententity_event_id
      LEFT JOIN UserObm on evententity_entity='user' AND evententity_entity_id=userobm_id
      LEFT JOIN UserObm u ON event_owner = u.userobm_id
    WHERE
      event_type='VEVENT'
      AND evententity_state = 'NEEDS-ACTION'
      AND ( 1=0
      $multidomain
";
  if (is_array($writable_cal) && (count($writable_cal['ids'])>0)) {
    $query .= "OR ( evententity_entity_id IN ('".implode("','",$writable_cal['ids'])."') AND evententity_entity = 'user')";    
  }
  $query .= " OR (evententity_entity_id = '$uid' AND evententity_entity = 'user'))";

  if (is_array($writable_res) && (count($writable_res['ids'])>0)) {
    $query .= "
    UNION
    SELECT
      u.userobm_lastname as usercreate_lastname,
      u.userobm_firstname as usercreate_firstname,
      resource_name AS entity_label,
      resource_id AS entity_id,
      'resource' AS entity_entity,
      event_id,
      event_title,
      event_location,
      eventcategory1_label,
      event_privacy,
      event_priority,
      event_repeatkind,
      event_repeatdays,
      event_duration,
      event_endrepeat,
      event_date
    FROM Event
      LEFT JOIN EventCategory1 ON event_category1_id = eventcategory1_id
      LEFT JOIN EventEntity ON event_id  = evententity_event_id
      LEFT JOIN Resource on evententity_entity='resource' AND evententity_entity_id=resource_id
      LEFT JOIN UserObm u ON event_owner = u.userobm_id
    WHERE
      event_type='VEVENT'
      AND evententity_state = 'NEEDS-ACTION'
      AND ( 1=0
      $multidomain
";
    $query .= "OR ( evententity_entity_id IN ('".implode("','",$writable_res['ids'])."') AND evententity_entity = 'resource'))";
  }
  $query .= " ORDER BY entity_entity, entity_label, event_date"; 
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

  $e_id = sql_parse_id( $calendar['calendar_id'], true);
  $query = "DELETE FROM EventEntity WHERE evententity_event_id  $e_id"; 
  if (is_array($users)) {
    if (count($users) != 0) {
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
function run_query_calendar_add_event($calendar, $sel_entity_id, $event_id) {
  global $obm, $ccalendar_resource_admin, $ccalendar_send_ics;
  global $l_from, $l_to, $l_date, $l_resource_allocated;
  global $l_add_event_mail_head,$l_add_event_mail_subject;

  $writable_cal = of_right_entity_for_user('calendar', $obm['uid'], 'write', '', 'userobm');
  $groups = $sel_entity_id['group'];
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
  
  $resources_array = $sel_entity_id['resource'];
  
  if (!is_array($resources_array) && !is_array($users_array)) {
    $users_array = array($obm["uid"]);
  }

  // Insert the Event
  $event_id = run_query_calendar_insert_event_data($calendar);   

  // Insert Event users
  foreach ($users_array as $user_id) {
    if ($user_id == $obm['uid'] || in_array($user_id, $writable_cal['ids'])) {
      run_query_calendar_insert_entity_event('user', $user_id, $event_id, 'ACCEPTED');
    } else {
      run_query_calendar_insert_entity_event('user', $user_id, $event_id, 'NEEDS-ACTION');
    }
  }

  // Insert The event task
  if ($task_id > 0) {
    run_query_calendar_insert_entity_event('task', $task_id, $event_id, 'ACCEPTED');
  }

  // Insert Event resources
  $resources_writable = array(); // Writable Resources
  $resources_address = array(); // Resource Admin notification
  // Get the final resource list and insert resource links
  if ( (is_array($resources_array)) && (count($resources_array) > 0) ) {
    $resources_array = array_unique($resources_array);
    $writable_res = of_right_entity_for_user('Resource', $obm['uid'], 'write');
    foreach ($resources_array as $res_id) {
      if (in_array($res_id, $writable_res['ids'])) {
	run_query_calendar_insert_entity_event('resource', $res_id, $event_id, 'ACCEPTED');
      } else {
	run_query_calendar_insert_entity_event('resource', $res_id, $event_id, 'NEEDS-ACTION');
      }
      array_push($resources_writable, $res_id);
    }
  }    

  // Insert My alert if set
  if (($calendar['alert']) > 0) {
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

  $now = Of_Date::today();
  if(!is_numeric($calendar['owner']) || $calendar['owner'] <= 0) {
    $owner = $obm['uid'];
  } else {
    $owner = $calendar['owner'];
  }
  $title = $calendar['title'];
  $category1_id = sql_parse_id( $calendar['category1']);
  $priority = sql_parse_int( $calendar['priority']);
  $description = $calendar['description'];
  $location = $calendar['location'];
  ($calendar['all_day'] != 1)? $all_day = 'FALSE' : $all_day = 'TRUE';
  $datebegin = $calendar['date_begin'];
  $event_duration = $calendar['event_duration'];
  if ($calendar['privacy']!=1) $privacy = 0; else $privacy = 1; 
  $repeat_kind = $calendar['repeat_kind'];
  $repeat_days = $calendar['repeat_days'];
  if(isset($calendar['repeat_end'])) {
    $repeat_end = "'$calendar[repeat_end]'";
  } else {
    $repeat_end = 'NULL';
  }  
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
  
  $query = "INSERT INTO Event (
    event_timeupdate,                         
    event_timecreate,                         
    event_usercreate,
    event_type,
    event_origin,
    event_owner,
    event_timezone,
    event_title, 
    event_date,     
    event_description,
    event_properties,
    event_location, 
    event_category1_id,
    event_priority,
    event_privacy, 
    event_duration,
    event_repeatkind,
    event_repeatfrequence,
    event_repeatdays,
    event_allday,
    event_color,
    event_endrepeat,
    event_domain_id)
  VALUES (
    '$now',
    '$now',
    '$obm[uid]',
    'VEVENT',
    'ui',
    '$owner',
    '".Of_Date::getOption('timezone')."', 
    '$title',
    '$datebegin',
    '$description',
    '$properties',
    '$location',
    $category1_id,
    $priority,
    $privacy,
    '$event_duration',		
    '$repeat_kind',
    '$repeatfrequency',
    '$repeat_days',
    $all_day,
    '$color',
    $repeat_end,
    $obm[domain_id])";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_event_data()');
  $obm_q->query($query);
  
  $query = "SELECT MAX(event_id) as event_id
    FROM Event
    WHERE event_date='$datebegin' 
      AND event_title='$title'
      AND event_duration='$event_duration'
      AND event_domain_id=$obm[domain_id]
      ORDER BY event_id";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_event_data()');
  $obm_q->query($query);
  $obm_q->next_record();
  $id = $obm_q->f('event_id');
  $calendar['calendar_id'] = $id;
    
  if($repeat_kind != 'none' && is_array($date_exception)) {
    foreach($date_exception as $key => $value) {
      $calendar['old_date_begin'] = $value->setHour($datebegin)->setMinute($datebegin);        
      run_query_calendar_event_exception_insert($calendar);
    }
  }

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Insert an occurrence of a event
// Parameters: 
//   - $entity : entity type 'user', 'resource'
//   - $entity_id :
//   - $event_id  :
//   - $state     :
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_insert_entity_event($entity, $entity_id,$event_id, $state) {
  global $cdg_sql, $obm;

  $event_id = $event_id;
  $entity_id = sql_parse_id( $entity_id);
  $uid = sql_parse_id( $obm['uid']);
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
    $event_id,
    $entity_id,
    '$entity',
    'REQ',
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

  include_once('obminclude/of/vcalendar/writer/ICS.php');
  include_once('obminclude/of/vcalendar/reader/OBM.php');

  $calendar_user['user'] = array ($obm['uid'] => 'dummy');
  
  $reader = new Vcalendar_Reader_OBM($calendar_user,array($event_id));
  $document = $reader->getDocument();
  $writer = new Vcalendar_Writer_ICS();  
  $writer->writeDocument($document);

  $fd_dest = fopen ($filename,'w');

  if ($fd_dest == false) {
    die ($l_file_error.$dest.'dead');
  }
  fputs($fd_dest, $writer->buffer);
  fclose($fd_dest);
}


///////////////////////////////////////////////////////////////////////////////
// Analyse the imported ics file
///////////////////////////////////////////////////////////////////////////////
function run_query_icalendar_insert($params, $force = false) {
  global $obm ;
  global $all_emails ;

  // --- get the unfolding ics
  $ics = $params['ics_tmp'] ;
  include_once('obminclude/of/vcalendar/Utils.php');
  include_once('obminclude/of/vcalendar/writer/OBM.php');
  $format = VCalendar_Utils::getFileType($ics);
  if($format == 'vcs') {
    include_once('obminclude/of/vcalendar/reader/VCS.php');
    $reader = new Vcalendar_Reader_VCS($ics);
  } elseif($format == 'ics') {
    include_once('obminclude/of/vcalendar/reader/ICS.php');
    $reader = new Vcalendar_Reader_ICS($ics);
  } else {
    $err[''] = '';
    return false;
  }
  $document = $reader->getDocument();
  $writer = new Vcalendar_Writer_OBM($force);  
  $writer->writeDocument($document);
  $nb_processed_event = count($document->getVevents());
  return array($nb_processed_event);

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

  $obm_q = new DB_OBM();
  $result = $obm_q->query($sql);
  if ($obm_q->num_rows() == 1) {
    $obm_q->next_record();
    return $obm_q->f('userobm_id');
  }
  else { // no result OR more than one result
    return $c_none ;
  }
}

//FIXME HERE
///////////////////////////////////////////////////////////////////////////////
// Check for conflict : Only with the first occurrence of each event,
// Parameters:
//   - $calendar       : calendar parameters
//   - sel_entity_id : selected entities id
///////////////////////////////////////////////////////////////////////////////
function check_calendar_conflict($calendar, $sel_entity_id) {

  $begin = clone $calendar['date_begin'];
  $end = clone $calendar['date_end'];
  $begin->setHour(0)->setMinute(0)->setSecond(0); 
  $end->addDay(1)->setHour(0)->setMinute(0)->setSecond(0);

  $cal_entities = store_calendar_entities(run_query_calendar_get_entity_label($sel_entity_id));
  $occurrences = calendar_events_model($begin,$end,$cal_entities);
  $of = &OccurrenceFactory::getInstance(); 
  if(!$calendar['all_day']) {
    $occurrences = $of->getPeriodOccurrences($calendar['date_begin'], $calendar['date_end']);
  }
  if(isset($calendar['calendar_id'])) {
    $event_id = $calendar["calendar_id"];
    foreach($occurrences as $occurrence) {
      if($occurrence->event->id != $event_id) {
        $oc[] = $occurrence;
      }
    }
    $occurrences = $oc;
  }  
  OccurrenceFactory::reset();
  if (count($occurrences) == 0) {
    return false;
  }
  return $occurrences;
}

///////////////////////////////////////////////////////////////////////////////
// Check right user ,
// Parameters:
//   - sel_entity_id : selected entities id
///////////////////////////////////////////////////////////////////////////////
function check_access_entity($consumers) {
  
  $access_entity = of_right_entity_for_user('calendar', $obm['uid'], 'access','', 'userobm');

  $no_access=array_diff($consumers,$access_entity);
  
  return $no_access;
}

///////////////////////////////////////////////////////////////////////////////
// Check for conflict : Only with the first occurrence of each event
///////////////////////////////////////////////////////////////////////////////
function check_calendar_decision_conflict($calendar) {
  
  $event_id = $calendar['calendar_id'];
  $sel_entity_id["user"][] = $calendar["user_id"];
  $e_q = run_query_calendar_detail($event_id);
  $begin = new Of_Date($e_q->f('event_date'),'GMT');
  $end = clone $begin;
  $end->addSecond($e_q->f('event_duration'));
  if($e_q->f('all_day')) {
    $begin->setHour(0)->setMinute(0)->setSecond(0); 
    $end->addDay(1)->setHour(0)->setMinute(0)->setSecond(0);
  }

  $cal_entities = store_calendar_entities(run_query_calendar_get_entity_label($sel_entity_id));
  $occurrences = calendar_events_model($begin,$end,$cal_entities);
  if(isset($calendar['calendar_id'])) {
    $event_id = $calendar["calendar_id"];
    foreach($occurrences as $occurrence) {
      if($occurrence->event->id != $event_id) {
        $oc = $occurrence;
      }
    }
    $occurrences = $oc;
  }
  OccurrenceFactory::reset();
  if (count($occurrences) == 0) {
    return false;
  }
  return $occurrences;
}

///////////////////////////////////////////////////////////////////////////////
// Event Update Execution
// Parameters: 
//   - $calendar      : Calendar params
//   - $sel_entity_id : List entitis id ['user'] ['resource']
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_event_update($calendar, $sel_entity_id, $event_id, $reset_state) {
  global $c_use_connectors, $obm, $l_date;
  global $l_update_event_mail_head,$l_update_event_mail_subject,$l_update_event_mail_body,$l_to,$l_from;
  global $ccalendar_resource_admin, $l_resource_allocated;

  $datebegin = $calendar['date_begin'];
  $dateend = $calendar['date_end'];
  $event_id =  $calendar['calendar_id'];
  $groups = $sel_entity_id['group'];
  $mail = $calendar['mail'];
  $mail_title = stripslashes($calendar['title']);
  $writable_cal = of_right_entity_for_user('calendar', $obm['uid'], 'write', '', 'userobm');

  if($calendar['task_new_id']) $task_id =  $calendar['task_new_id']; 
  else $task_id =  $calendar['task_id'];

  $user_group_array = array();

  // Get the final user list
  $users_array = $sel_entity_id['user'];
  $user_group_array = array();
  if (is_array($groups)) {
    foreach($groups as $group_id) {
      $user_group_array = array_merge($user_group_array, of_usergroup_get_group_users($group_id));
    }
    $users_array = array_merge($users_array,$user_group_array);
    $users_array = array_unique($users_array);
  }

  // Get the final resource list
  $resources_array = $sel_entity_id['resource'];
  if ( (is_array($resources_array)) && (count($resources_array) > 0) ) {
    $resources_array = array_unique($resources_array);
    $writable_res = of_right_entity_for_user('Resource', $obm['uid'], 'write');
  } else {
    $resources_array = array();
  }

  if (!is_array($resources_array) && !is_array($users_array)) {
    $users_array = array($obm["uid"]);
  }

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

    // If users have been removed, we note it in DeletedEvent
    if (count($removed_users) > 0) {
      foreach($removed_users as $u_id) {
	$query = "INSERT INTO DeletedEvent (
        deletedevent_event_id,
        deletedevent_user_id,
        deletedevent_timestamp)
      VALUES (
        $event_id,
        $u_id,
        '$now')";
	display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_update(1)');
	$retour = $obm_q->query($query);
      }
    }

    // If users have been inserted they must not appear in DeletedEvent
    if (count($new_users) > 0) {
      foreach($new_users as $u_id) {
	$query = "DELETE FROM DeletedEvent
      WHERE deletedevent_event_id = $event_id
        AND deletedevent_user_id = $u_id";
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
  
  $query = "DELETE FROM EventEntity WHERE 1=1 $qusers $rresources AND evententity_event_id = $event_id";
  $db = new DB_OBM;
  $db->query($query);

  // User insertion
  if(is_array($users_array)) {
    foreach($users_array as $user_id) {
      if ($user_id == $obm['uid'] || in_array($user_id, $writable_cal['ids'])) {
        run_query_calendar_insert_entity_event('user', $user_id, $event_id, 'ACCEPTED');
      } else {
        run_query_calendar_insert_entity_event('user', $user_id, $event_id, 'NEEDS-ACTION');
      }
    }
  }

  if (is_array($resources_array)) {
    foreach ($resources_array as $res_id) {
      if (in_array($res_id, $writable_res['ids'])) {
	run_query_calendar_insert_entity_event('resource', $res_id, $event_id, 'ACCEPTED');
      } else {
	run_query_calendar_insert_entity_event('resource', $res_id, $event_id, 'NEEDS-ACTION');
      }
    }
  }
  
  if ($task_id > 0) {
    run_query_calendar_insert_entity_event('task', $task_id, $event_id, 'ACCEPTED');
  }

  // Insert My alert if set
  if (($calendar['alert']) > 0) {
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
  if(is_numeric($calendar['owner']) && $calendar['owner'] >= 0) {
    $owner = "event_owner = $calendar[owner],";
  }
  $category1_id = sql_parse_id( $calendar['category1']);
  $priority = $calendar['priority'];
  $description = $calendar['description'];
  $location = $calendar['location'];
  $datebegin = $calendar['date_begin'];
  $event_duration = $calendar['event_duration'];
  ($calendar['all_day'] != 1) ? $all_day = 'FALSE': $all_day = 'TRUE';
  $event_id = sql_parse_id( $calendar['calendar_id'], true); 
  if ($calendar['privacy'] != 1) $privacy = 0; else $privacy = 1; 
  $repeat_kind = $calendar['repeat_kind'];
  $repeat_days = $calendar['repeat_days'];
  if(isset($calendar['repeat_end'])) {
    $repeat_end = "'$calendar[repeat_end]'";
  } else {
    $repeat_end = 'NULL';
  }    
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
  $query = "UPDATE Event SET
    event_timeupdate = '$now',
    $owner
    event_userupdate = $obm[uid], 
    event_title = '$title', 
    event_date = '$datebegin',     
    event_description = '$description',
    event_properties = '$properties', 
    event_location = '$location',
    event_category1_id = $category1_id,
    event_priority = '$priority',
    event_privacy = '$privacy', 
    event_duration = '$event_duration',
    event_allday = $all_day, 
    event_color = '$color',
    event_repeatkind = '$repeat_kind',
    event_repeatfrequence = '$frequency',
    event_repeatdays = '$repeat_days',
    event_endrepeat = $repeat_end
  WHERE
    event_id $event_id"; 

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_event_data()');
  $obm_q->query($query);

  $query = "DELETE FROM EventException WHERE eventexception_event_id $event_id"; 
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_event_data(2)');
  $obm_q->query($query);
  
  if($repeat_kind != 'none' && is_array($date_exception)) {
    foreach($date_exception as $key => $value) {
      $calendar['old_date_begin'] = $value->setHour($datebegin)->setMinute($datebegin);        
      run_query_calendar_event_exception_insert($calendar);
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

  $multidomain = sql_multidomain('event');
  $now = date('Y-m-d H:i:s');
  $title = $calendar['title'];
  $datebegin = $calendar['date_begin'];
  $event_duration = $calendar['duration'];
  $event_id = sql_parse_id($calendar['calendar_id'], true); 
  $obm_q = new DB_OBM;
  $query = "UPDATE Event SET
    event_timeupdate = '$now',
    event_userupdate = $obm[uid], 
    event_title = '$title', 
    event_date = '$datebegin',     
    event_duration = '$event_duration'
  WHERE
  event_id $event_id $multidomain";
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
  $all_day = ($calendar['all_day'])? 'TRUE' : 'FALSE';

  $obm_q = new DB_OBM;
  $query = "INSERT INTO Event
    (event_timecreate,
     event_usercreate,
     event_type,
     event_origin,
     event_owner,
     event_timezone,
     event_title,
     event_date,
     event_duration,
     event_allday,
     event_description,
     event_properties,
     event_privacy,
     event_repeatkind,
     event_repeatfrequence,
     event_endrepeat,
     event_repeatdays,
     event_location, 
     event_category1_id,
     event_priority,
     event_domain_id
   )
  VALUES (
    '$now',
    $obm[uid],
    'VEVENT',
    'ui_ajax',
     $obm[uid],
    '".Of_Date::getOption('timezone')."', 
    '$title', 
    '$datebegin',     
    '$event_duration',
    $all_day,
    '',
    '',
    0,
    'none',
    1,
    'NOW()',
    '',
    '',
    NULL,
    2,
    $obm[domain_id]
  )";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_quick_insert_event_data()');
  $obm_q->query($query);
  $query = "SELECT MAX(event_id) as max_id
    FROM Event
    WHERE event_title='$title'
      AND event_date='$datebegin'";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_event_data()');
  $obm_q->query($query);
  $obm_q->next_record(); 
  $event_id = $obm_q->f('max_id'); 
  run_query_calendar_insert_entity_event('user', $obm['uid'], $event_id, 'ACCEPTED');

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
  $id =  $calendar['calendar_id'];
  $olddatebegin = $calendar['old_date_begin'];
  $event_duration = $calendar['duration'];
  
  $obm_q = new DB_OBM;

  $sql_id = sql_parse_id($id, true);
  $query = "UPDATE Event SET event_timeupdate = NOW() WHERE event_id = $id";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_exception_insert(0)');
  $obm_q->query($query);
  $sql_id = sql_parse_id($id);
  $query = "INSERT INTO EventException (
      eventexception_usercreate,
      eventexception_timecreate ,   
      eventexception_userupdate,
      eventexception_timeupdate,
      eventexception_event_id,
      eventexception_date)
      VALUES (
        $obm[uid],
        '$now',
        $obm[uid],
        '$now',
        $id,
        '$olddatebegin'
      )";
  
  $sql_id = sql_parse_id($id, true);
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_exception_insert(1)');
  $obm_q->query($query);
  $query = "Update Event SET
    event_userupdate = $obm[uid],
    event_timeupdate = NOW()
    WHERE
    event_id $sql_id";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_exception_insert(2)');
  $obm_q->query($query);

  if(is_object($event_q)) {
    if ($event_q->f('event_privacy')!=1) $privacy = 0; else $privacy = 1; 
    $repeat_kind = $event_q->f('event_repeatkind');
    $repeat_days = $event_q->f('event_repeatdays');
    if($event_q->f('event_endrepeat')) {
      $repeat_end = new Of_Date($event_q->f('event_endrepeat'), 'GMT');
      $repeat_end = "'$repeat_end'";
    } else {
      $repeat_end = "NULL";
    }
    $repeatfrequency = $event_q->f('event_repeatfrequence');  
    $category1_id = sql_parse_id( $event_q->f('event_category1_id'));
    $priority = sql_parse_int( $event_q->f('event_priority'));
    $color = $event_q->f('event_color');
    $description = $event_q->f('event_description');
    $properties = $event_q->f('event_properties');
    $location = $event_q->f('event_location');
    ($calendar['all_day'] != 1)? $all_day = 'FALSE' : $all_day = 'TRUE';
    $datebegin = $calendar['date_begin'];

    $query = "INSERT INTO Event (
      event_timeupdate,                         
      event_timecreate,                         
      event_usercreate,
      event_type,
      event_origin,
      event_owner,
      event_timezone,
      event_title, 
      event_date,     
      event_description,
      event_properties,
      event_location, 
      event_category1_id,
      event_priority,
      event_privacy, 
      event_duration,
      event_repeatkind,
      event_repeatfrequence,
      event_repeatdays,
      event_allday,
      event_color,
      event_endrepeat,
      event_domain_id
    )
    VALUES (
      '$now',
      '$now',
      $obm[uid],
      'VEVENT',
      'ui',
      $obm[uid],
      '".Of_Date::getOption('timezone')."', 
      '$title',
      '$datebegin',
      '$description',
      '$properties',
      '$location',
      $category1_id,
      $priority,
      $privacy,
      '$event_duration',		
      'none',
      '$repeatfrequency',
      '$repeat_days',
      $all_day,
      '$color',
      $repeat_end,
      '$obm[domain_id]'
    )";

    display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_event_data(3)');
    $obm_q->query($query);
    
    $query = "SELECT MAX(event_id) as max_id FROM Event";
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

  $event_id = $eve_q->f('event_id');
  $writable['user'] = of_right_entity_for_user('calendar', $obm['uid'], 'write', '', 'userobm');
  array_push($writable['user']['ids'], $obm['uid']);
  $writable['resource'] = of_right_entity_for_user('Resource', $obm['uid'], 'write');
  $entities = get_calendar_event_entity($event_id);
  run_query_calendar_delete_event_entity($params,array());
  foreach ($entities as $entity => $data) {
    foreach ($data['entity'] as $entity_id => $entity_data) { 
      if(is_array( $writable[$entity]['ids'])) {
        if (in_array($entity_id, $writable[$entity]['ids'])) {
          run_query_calendar_insert_entity_event($entity, $entity_id, $event_id, 'ACCEPTED');
        } else {
          run_query_calendar_insert_entity_event($entity, $entity_id, $event_id, 'NEEDS-ACTION');
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
  $event_id = sql_parse_id($event_id);
  $user_id = sql_parse_id($user_id);

  if (is_array($alerts) && count($alerts) > 0) {
    $obm_q = new DB_OBM;
    foreach ($alerts as $dur => $label) {
      $query = "INSERT INTO EventAlert (
        eventalert_timecreate,
        eventalert_usercreate,
        eventalert_event_id,
        eventalert_user_id,
        eventalert_duration)
      VALUES (
        '$now',
        $obm[uid],
        $event_id,
        $user_id,
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
    $where_user = "AND eventalert_user_id = $user_id";
  }

  $query = "DELETE FROM EventAlert
    WHERE eventalert_event_id = '$event_id'
      $where_user";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_alert_delete()');
  $obm_q->query($query);

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the event can be accessed
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
  $event_id =  $calendar['calendar_id'];
  $mail = $calendar['mail']; 
  
  $users_array = get_calendar_event_users_info($event_id);
  
  $multidomain = sql_multidomain('event');
  $query = "SELECT event_title
    FROM Event
    WHERE event_id = $event_id $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete(1)');
  $obm_q->query($query);
  $obm_q->next_record();
  $title = $obm_q->f('event_title');

  // If connectors in use
  if ($c_use_connectors) {
    $now = date('Y-m-d H:i:s');
    $query = "SELECT evententity_entity_id FROM EventEntity WHERE evententity_event_id = $event_id AND evententity_entity = 'user'";
    $obm_q->query($query);
    $users = array();
    while ($obm_q->next_record()) {
      // We note the event deletion
      $query = "INSERT INTO DeletedEvent (deletedevent_event_id, deletedevent_user_id, deletedevent_timestamp)
                VALUES ($event_id, ".$obm_q->f('evententity_entity_id').", '$now')";
      display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete(2)');
      $retour = $obm_q->query($query);
    }
  }
  $query = "DELETE FROM Event WHERE event_id = $event_id";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete(5)');
  $obm_q->query($query);


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
        $i = $i%20;
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
        'image' => ${'ico_calendar_user'.$i},
        'type'  => 'resource',
        'id'    => $id
      );
      $i++;
      $i = $i%20;
    }
  }
  $resourcegroup_q = $entities_q['resourcegroup'];
  if (is_object($resourcegroup_q)) {
    while($resourcegroup_q->next_record()) {
      $id = $resourcegroup_q->f('resource_id');
      $entity_tab['resourcegroup'][$id] = array(
        'name'  => $resourcegroup_q->f('resource_name'),
        'class' => "eventBg$i",
        'image' => ${'ico_calendar_user'.$i},
        'type'  => 'resourcegroup',
        'id'    => $id,
        'group' => $resourcegroup_q->f('resource_group'),
        'groupLabel' => $resourcegroup_q->f('rgroup_name'),
      );
      $i++;
      $i = $i%20;
    }
  }  

  return $entity_tab;
}


///////////////////////////////////////////////////////////////////////////////
// Return the event infos
// Parameters:
//   - $id : event id
///////////////////////////////////////////////////////////////////////////////
function get_calendar_event_info($id,$attendees=true) {
  global $cdg_sql;

  if ($id == '') {
    return false;
  }
  $id = sql_parse_id( $id, true);
  if($attendees) {
    $a_sql = 'LEFT JOIN EventEntity ON evententity_event_id = event_id';
  }
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $event_date_l = sql_date_format($db_type,'event_date','event_date');
  $multidomain = sql_multidomain('event');
  $query = "SELECT *, $event_date_l FROM Event 
            $a_sql
            WHERE event_id $id 
            $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_calendar_event_info()');
  $obm_q->query($query);
  $e = array('attendees' => array('user' => array()));
  while($obm_q->next_record()) {
    $e['usercreate'] = $obm_q->f('event_usercreate');
    $e['owner'] = $obm_q->f('event_owner');
    $e['date'] = new  Of_Date($obm_q->f('event_date'), 'GMT');
    $e['duration'] = $obm_q->f('event_duration');
    $e['end'] = clone $e['date'];
    $e['end']->addSecond($obm_q->f('event_duration'));
    if($obm_q->f('evententity_entity_id') && $attendees) {
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

  $id = sql_parse_id( $id, true);
  if ($id == '') {
    return $users_array;
  }

  $query = "SELECT evententity_entity_id
    FROM EventEntity
    WHERE evententity_event_id $id AND evententity_entity = 'user'";

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
  $calendar_id = sql_parse_id( $calendar_id, true);
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
    evententity_event_id $calendar_id AND 
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
    evententity_event_id $calendar_id 
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
    evententity_event_id $calendar_id 
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
  global $l_err_monthly_dateend_repeat, $l_err_yearly_dateend_repeat;
  
  $title = $calendar['title'];
  $begin = $calendar['date_begin'];
  $end = $calendar['date_end'];
  $repeat_end = $calendar['repeat_end'];
  $kind = $calendar['repeat_kind'];
  $repeat_days = $calendar['repeat_days'];
  $repeatfrequency = $calendar['repeatfrequency'];
  $event_duration = $calendar['event_duration'];
  ($calendar['all_day'] != 1)? $all_day = 'FALSE' : $all_day = 'TRUE';

  if (trim($title) == '') {
    $err['msg'] = $l_fill_title;
    return false;
  }
  
  // Begin Date check
  if ($begin->error() == Of_Date::WARN_EMPTY_DATE) {
    $err['msg'] = $l_fill_datebegin;
    return false;
  } else if ($begin->error() == Of_Date::ERR_INVALID_DATE) {
    $err['msg'] = "$l_datebegin : $l_invalid_date"; 
    return false;
  }
  
  // End Date check
  if ($end->error() == Of_Date::WARN_EMPTY_DATE) {
    $err['msg'] = $l_fill_dateend;
    return false;
  } else if ($end->error() == Of_Date::ERR_INVALID_DATE) {
    $err['msg'] = "$l_dateend : $l_invalid_date"; 
    return false;
  }
  
  // Repeatition check
  if ($kind != 'none') {
    // End repeat Date check
    if ($repeat_end->error() != Of_Date::WARN_EMPTY_DATE && $repeat_end->error() == Of_Date::ERR_INVALID_DATE) {
      $err['msg'] = "$l_date_repeatend : $l_invalid_date"; 
      return false;
    }
  }

  if (!is_numeric($repeatfrequency) || $repeatfrequency < 1) {
    $err['msg'] = $l_err_repeatfrequency;
    return false;
  }

  if ($end->compare($begin) < 0 || $event_duration < 0 || ( $all_day && $end->compare($begin) == 0)) {
    $err['msg'] = $l_err_begin_end;
    return false;
  } 
  
  if ($repeat_end->error() != Of_Date::WARN_EMPTY_DATE && $end->compare($repeat_end) > 0 && $kind != 'none') {
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

  $clone = clone $end;
  // Weekly repetition end >= date end + 1 week
  if ($kind == 'weekly' && $clone->addWeek(1)->compare($repeat_end) > 0) {
    $err['msg'] = $l_err_weekly_repeat;
    return false;
  }

  // Monthly repetition end >= date end + 1 month
  if ((($kind == 'monthlybydate') || ($kind == 'monthlybyday')) && $clone->addWeek(1)->compare($repeat_end) > 0) {
    $err['msg'] = $l_err_monthly_dateend_repeat;
    return false;
  }

  // Yearly repetition end >= date end + 1 year
  if ($kind == 'yearly' && $clone->addYear(1)->compare($repeat_end) > 0) {
    $err['msg'] = $l_err_yearly_dateend_repeat;
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
//   - $date : time to format
///////////////////////////////////////////////////////////////////////////////
function calendar_localizeDate($format, $date) {

 $day = Of_Date::OBM_DATE_DAY;
 $week = Of_Date::OBM_DATE_WEEK;
 $week_list = Of_Date::OBM_DATE_WEEK_LIST;
 $week_jump = Of_Date::OBM_DATE_WEEK_JUMP;
 $month = Of_Date::OBM_DATE_MONTH;
 $month_list = Of_Date::OBM_DATE_MONTH_LIST;
 if(is_object($date)) {

 return $date->localize(${$format});	
 }
}

//FIXME NECESSARY?
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

//FIXME NECESSARY?
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
    $select_user = "
      userobm_id,
      userobm_lastname,
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
    $where_event .= " AND event_id != '$event_id' ";
  }
  $multidomain = sql_multidomain('event');

  $query = "SELECT
      event_id,
      event_owner,
      event_title,
      event_privacy,
      event_description,
      event_properties,
      event_location,
      event_repeatfrequence,
      evententity_entity,
      evententity_entity_id,
      $select_user
      $select_resource
      eventcategory1_label,
      event_date,
      event_duration,
      event_allday
    FROM Event
      LEFT JOIN EventCategory1 ON eventcategory1_id = event_category1_id
      LEFT JOIN EventEntity ON event_id = evententity_event_id
      $join_user
      $join_resource
    WHERE event_type='VEVENT'
      AND evententity_state = 'ACCEPTED'
      AND event_repeatkind = 'none' AND
      ((event_date > #SUBSECONDS('$start', event_duration) AND event_date < '$end') 
        OR (event_allday = TRUE AND event_date >= #SUBSECONDS('$start', event_duration) AND event_date <= '$end'))
      $where_ent
      $where_event
      $multidomain
    ORDER BY event_date";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_conflict_events(()');
  $obm_q->xquery($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a week of users or/and groups
// Parameters:
//   - $start           : timestamp of start date
//   - $end             : timestamp of end date
//   - $calendar_entity : array of entities elements to display
//   - $status          : array of event status to get ('ACCEPTED', 'R', 'W')
//   - $category_filter : category
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_no_repeat_events($start,$end, $calendar_entity, $status, $category_filter='') {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $multidomain = sql_multidomain('event');
  if ($category_filter != '') {
    $filter = " AND eventcategory1_id = '$category_filter' ";
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

  $first = true;
  if (is_array($status)
      && (count($status) > 0) ) {
    foreach ($status as $one_status) {
      if ($first) {
	$where_status = "AND (evententity_state = '$one_status'";
	$first = false;
      } else {
	$where_status .= " OR evententity_state = '$one_status'";
      }
    }
    $where_status .= ')';
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
    $where_grp.= ')';
    $where_grp .= " AND evententity_entity = 'resource' )"; 
    
    $union = "
      UNION
    SELECT
      event_id,
      event_title,
      event_privacy,
      event_priority,
      event_description,
      event_properties,
      event_location,
      event_repeatfrequence,
      event_owner,
      event_timezone,
      evententity_entity_id,
      'resourcegroup' as evententity_entity,
      evententity_state,
      eventcategory1_label,
      event_date,
      event_duration,
      event_color,
      event_allday
    FROM Event
      LEFT JOIN EventCategory1
        ON event_category1_id = eventcategory1_id
      LEFT JOIN EventEntity ON event_id = evententity_event_id
    WHERE event_type='VEVENT'
      $where_status
      $filter
      AND event_repeatkind = 'none'
      AND event_date >= #SUBSECONDS('$start',event_duration)
      AND event_date <='$end'
      AND $where_grp
      $multidomain";
  }
  
  $query = "SELECT
      event_id,
      event_title,
      event_privacy,
      event_priority,
      event_description,
      event_properties,
      event_location,
      event_repeatfrequence,
      event_owner,
      event_timezone,
      evententity_entity_id,
      evententity_entity,
      evententity_state,
      eventcategory1_label,
      event_date,
      event_duration,
      event_color,
      event_allday
    FROM Event
      LEFT JOIN EventCategory1
        ON event_category1_id = eventcategory1_id
      LEFT JOIN EventEntity ON event_id = evententity_event_id
    WHERE event_type='VEVENT'
      $where_status
      $filter
      AND event_repeatkind = 'none'
      AND event_date >= #SUBSECONDS('$start',event_duration)
      AND event_date <= '$end'
      $where_ent
      $multidomain
    $union
      ORDER BY event_date, evententity_entity, evententity_entity_id
";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_no_repeat_events()');
  $obm_q->xquery($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a week of users or/and groups
// Parameters:
//   - $start           : timestamp of start date
//   - $end             : timestamp of end date
//   - $calendar_entity : array of entities elements to display
//   - $status          : array of event status to get ('ACCEPTED', 'R', 'W')
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_repeat_events($start, $end, $calendar_entity, $status, $category_filter='') {
  global $cdg_sql;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $multidomain = sql_multidomain('event');
  if ($category_filter != '') {
    $category_filter = sql_parse_id( $category_filter);
    $filter = " AND eventcategory1_id = $category_filter ";
  }
  if (is_array($calendar_entity['user'])
      && (count($calendar_entity['user']) > 0) ) {
    $has_user = true;
  }
  if (is_array($calendar_entity['resource'])
      && (count($calendar_entity['resource']) > 0) ) {
    $has_resource = true;
  }

  $first = true;
  if (is_array($status)
      && (count($status) > 0) ) {
    foreach ($status as $one_status) {
      if ($first) {
	$where_status = "AND (evententity_state = '$one_status'";
	$first = false;
      } else {
	$where_status .= " OR evententity_state = '$one_status'";
      }
    }
    $where_status .= ')';
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
      event_id,
      event_title,
      event_privacy,
      event_priority,
      event_description, 
      event_properties, 
      event_location, 
      eventcategory1_label,
      event_date,
      event_duration,
      event_repeatkind,
      event_endrepeat,
      event_repeatfrequence,
      event_owner,
      event_timezone,
      'resourcegroup' as evententity_entity,
      evententity_entity_id,
      evententity_state,
      event_repeatdays,
      event_color,
      event_allday
    FROM Event
      LEFT JOIN EventCategory1
        ON event_category1_id = eventcategory1_id
      LEFT JOIN EventEntity ON event_id = evententity_event_id
   WHERE event_type='VEVENT' 
      AND event_repeatkind != 'none'
      $where_status
      $filter
      AND event_date <= '$end' 
      AND (event_endrepeat >= #SUBSECONDS('$start',event_duration)
      OR event_endrepeat IS NULL)
      AND $where_grp
      $multidomain";
  }

  $query = "SELECT
      event_id,
      event_title,
      event_privacy,
      event_priority,
      event_description, 
      event_properties, 
      event_location, 
      eventcategory1_label,
      event_date,
      event_duration,
      event_repeatkind,
      event_endrepeat,
      event_repeatfrequence,
      event_owner,
      event_timezone,
      evententity_entity,
      evententity_entity_id,
      evententity_state,
      event_repeatdays,
      event_color,
      event_allday
    FROM Event
      LEFT JOIN EventCategory1
        ON event_category1_id = eventcategory1_id
      LEFT JOIN EventEntity ON event_id = evententity_event_id
    WHERE event_type='VEVENT'
      AND event_repeatkind != 'none'
      $where_status
      $filter
      AND event_date <= '$end' 
      AND (event_endrepeat >= #SUBSECONDS('$start',event_duration)
      OR event_endrepeat IS NULL)
      $where_ent
      $multidomain
      $union

    ORDER BY event_date, evententity_entity, evententity_entity_id"; 

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_repeat_events()');
  $obm_q->xquery($query);
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

  $event_list = implode(',',$events);
  if ($event_list == '') {
    $event_list = '0';
  }
  $obm_q = new DB_OBM;

  $multidomain = sql_multidomain('event');
  $query = "SELECT
   event_id,
   evententity_state,
   evententity_entity_id,
   evententity_entity,
   userobm_firstname,
   userobm_lastname,
   resource_name,
   projecttask_label,
   project_name
   FROM Event
   JOIN EventEntity ON evententity_event_id = event_id
   LEFT JOIN UserObm ON evententity_entity = 'user' AND evententity_entity_id = userobm_id
   LEFT JOIN Resource ON evententity_entity = 'resource' AND evententity_entity_id = resource_id
   LEFT JOIN ProjectTask ON evententity_entity = 'task' AND  evententity_entity_id = projecttask_id
   LEFT JOIN Project ON project_id = projecttask_project_id
   WHERE event_id IN ($event_list) $multidomain";

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
function run_query_get_events_exception($events,$start_time = NULL,$end_time = NULL) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $event_list = implode(',',$events);
  if(!is_null($start_time)) {
    $start = "AND eventexception_date > '$start_time'";
  }
  if(!is_null($end_time)) {
    $end = "AND eventexception_date < '$end_time'";
  }
  $query = "SELECT
    eventexception_event_id,
    eventexception_date
   FROM EventException
   WHERE eventexception_event_id IN ($event_list) 
   $start $end";
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
  $calendar_id = sql_parse_id( $calendar_id, true);

  $query = "SELECT
    eventexception_event_id,
    eventexception_date
   FROM EventException
   WHERE eventexception_event_id $calendar_id"; 

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
  $event_id = sql_parse_id($event_id, true); 
  $query = "SELECT
    eventalert_duration,
    eventalert_user_id
   FROM EventAlert
   WHERE eventalert_event_id $event_id"; 

  if($user_id) {
    $user_id = sql_parse_id($user_id, true);
    $query .= " AND eventalert_user_id $user_id"; 
  }

  display_debug_msg($query, $cdg_sql, 'get_one_event_user_alert()');
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $dur = $obm_q->f('eventalert_duration');
    $id = $obm_q->f('eventalert_user_id');
    $dur_min = $dur / 60;
    $alerts[$id] = $dur;
  }

  return $alerts;
}

//FIXME
///////////////////////////////////////////////////////////////////////////////
// Return a table of Events between two dates
// Parameters: 
//   - $start_time      : timestamp of start date
//   - $end_time        : timestamp of end date
//   - $calendar_entity :
///////////////////////////////////////////////////////////////////////////////
function calendar_events_model($start, $end, $calendar_entity, $status=array('ACCEPTED'), $category_filter='', $unique=false) {
  global $day_duration;

  $day_duration = 86400;
  // Get all non repeating events and construct occurences & events model
  $nr_q = run_query_calendar_no_repeat_events($start,$end,$calendar_entity, $status, $category_filter);
  $of = &OccurrenceFactory::getInstance();
  $of->setBegin($start);
  $of->setEnd($end);
  while ($nr_q->next_record()) {
    $id = $nr_q->f('event_id');
    $title = $nr_q->f('event_title');
    $privacy = $nr_q->f('event_privacy');
    $description = $nr_q->f('event_description'); 
    $properties = $nr_q->f('event_properties');
    $location = $nr_q->f('event_location'); 
    $category1 = $nr_q->f('eventcategory1_label');
    $priority = $nr_q->f('event_priority');
    $date = new Of_Date($nr_q->f('event_date'),'GMT');
    $color = $nr_q->f('event_color');
    $duration = $nr_q->f('event_duration');
    $all_day = $nr_q->f('event_allday');
    $entity = $nr_q->f('evententity_entity');
    $entity_id = $nr_q->f('evententity_entity_id');
    $entity_state = $nr_q->f('evententity_state');
    $owner = $nr_q->f('event_owner');
    $timezone = $nr_q->f('event_timezone');
    if (isset($of->events[$id])) {
      $event = $of->events[$id];
      if (!$unique) {
	$of->addOccurrence($event, $date, $entity, $entity_id, $entity_state);
      }
    } else {
      $event = &new Event($id,$duration,$title,$location,$category1,$privacy,$description,$properties,$all_day,'none',$owner,$color);
      $of->addOccurrence($event, $date, $entity, $entity_id, $entity_state);
    }
  }
  
  // Get all repeating events and construct occurences & events model
  $r_q = run_query_calendar_repeat_events($start,$end,$calendar_entity, $status, $category_filter);
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
    $endrepeat = new Of_Date($r_q->f('event_endrepeat'), 'GMT');
    $entity = $r_q->f('evententity_entity');
    $all_day = $r_q->f('event_allday');
    $color = $r_q->f('event_color');
    $repeatfrequence = $r_q->f('event_repeatfrequence');
    $repeatdays = $r_q->f('event_repeatdays');
    $entity_id = $r_q->f('evententity_entity_id');
    $entity_state = $r_q->f('evententity_state');
    $all_day = $r_q->f('event_allday');    
    $owner = $r_q->f('event_owner');
    $timezone = $r_q->f('event_timezone');
    if ($endrepeat->error() == Of_Date::WARN_EMPTY_DATE) {
      $endrepeat = $end;
    }
    $addOcc = true;
    if (isset($of->events[$id])) {
      $event = $of->events[$id];
      if ($unique) { 
        $addOcc = false;
      }
    } else {
      $event = &new Event($id,$duration,$title,$location,$category1,$privacy,$description,$properties,$all_day,$repeatkind,$owner,$color);
      $event->setTimezone($timezone);
    }
    $tz = new DateTimeZone($timezone);
    $date->setTimezone($tz);
    $event_start = clone $start;
    $event_start->setTimezone($tz); 
    $event_start->subSecond($duration)->setHour($date)->setMinute($date)->setSecond($date);
    $event_end = ($end->compare($endrepeat) > 0)? clone $endrepeat: clone $end; 
    $event_end->setTimezone($tz);
    $event_end->setHour($date)->setMinute($date)->setSecond($date)->addSecond($duration);
    if ($addOcc) {
      switch ($repeatkind) {
        case 'daily' :
          calendar_daily_repeatition($date,$event_start,$event_end,$repeatfrequence,$event,$entity_id,$entity, $entity_state);
          break;
        case 'weekly' :
          calendar_weekly_repeatition($date,$event_start,$event_end,$repeatdays,$repeatfrequence,$event,$entity_id,$entity, $entity_state);
          break;
        case 'monthlybyday' :
          $stored = calendar_monthlybyday_repeatition($date,$event_start,$event_end,$repeatfrequence,$event,$entity_id,$entity, $entity_state);
          break;
        case 'monthlybydate' :
          $stored = calendar_monthlybydate_repeatition($date,$event_start,$event_end,$repeatfrequence,$event,$entity_id,$entity, $entity_state);
          break;
        case 'yearly' :
          $stored = calendar_yearly_repeatition($date,$event_start,$event_end,$repeatfrequence,$event,$entity_id,$entity, $entity_state);
          break;
      }
    }
  }

  // Process the model : exceptions, attendees
  if (count($of->events) > 0) {

    // Handle exceptions : remove occurences from the model
    $exception_q = run_query_get_events_exception(array_keys($of->events),$start,$end);
    while($exception_q->next_record()) {
      $of->removeOccurrences($exception_q->f('eventexception_event_id'), new Of_Date($exception_q->f('eventexception_date'), 'GMT'));
    }

    // Add attendees to events
    $attendee_q = run_query_get_events_attendee(array_keys($of->events));
    while($attendee_q->next_record()) {
      $id = $attendee_q->f('event_id');
      $entity = $attendee_q->f('evententity_entity');
      $entity_id = $attendee_q->f('evententity_entity_id');
      $entity_state = $attendee_q->f('evententity_state');
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
      $event = $of->events[$id];
      $event->addAttendee($entity,$entity_id,$entity_label,$entity_state);
    }
  }
  return $of->getOccurrences(); 
}


///////////////////////////////////////////////////////////////////////////////
// Perform the daily repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function calendar_daily_repeatition($date,$start,$end,$repeatfrequence,$event,$entity_id,$entity, $entity_state) {
  global $day_duration;

  $current = clone $date;
  if ($current->compare($start) < 0) {
    $diff = - ceil($current->diffTimestamp($start) / Of_Date::DAYDURATION); 
    if ($diff > 0) {
      $diff = ($diff % $repeatfrequence);
    }	    
    $current->setDateIso($start)->subDay($diff);
  }
  while ($current->compare($end) < 0) {
    $of = &OccurrenceFactory::getInstance();
    $of->addOccurrence($event, clone $current, $entity, $entity_id, $entity_state);
    $current->addDay($repeatfrequence);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Perform the weekly repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function calendar_weekly_repeatition($date,$start,$end,$repeatdays,$repeatfrequence,$event,$entity_id,$entity, $entity_state) {
  global $day_duration, $ccalendar_weekstart;

  $current = clone $date;
  $current->setWeekday($ccalendar_weekstart);
  if ($current->compare($start) < 0) {
    $diff = - ceil($start->diffTimestamp($current) / Of_Date::DAYDURATION); 
    if ($diff > 0) {
      $diff = ($diff % ($repeatfrequence * 7));
    }	    
    $current->setDateIso($start)->subDay($diff);
  }
  $days = str_split($repeatdays);
  if($start->compare($date) < 0) $start = $date;
  while ($current->compare($end) < 0) {
    foreach ($days as $num => $repeat) {	
      if ($repeat == 1) {
        $current->setWeekday($num);
        if($current->compare($start) >= 0 && $current->compare($end) < 0) {
          $of = &OccurrenceFactory::getInstance();
          $of->addOccurrence($event, clone $current, $entity, $entity_id, $entity_state);
	}
      }
    } 
    $current->addWeek($repeatfrequence);
  }	
}


///////////////////////////////////////////////////////////////////////////////
// Perform the monthly by day repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function calendar_monthlybyday_repeatition($date,$start,$end,$repeatfrequence,$event,$entity_id,$entity, $entity_state) {
  global $day_duration,$ccalendar_weekstart;

  $current = clone $date;
  $current->setDay(1);
  if ($current->compare($start) < 0) {
    $diff = ($start->getYear() - $current->getYear()) * 12 + ($start->getMonth() - $current->getMonth());
    if ($diff > 0) {
      $diff = $diff  % $repeatfrequence;
    }
    $current->setMonth($start)->subMonth($diff);
  }
  $position = ceil($date->getDay() / 7);
  $pattern = ($position == 5)? "-1 ".$date->format(Of_Date::WEEKDAY): "+$position ".$date->format(Of_Date::WEEKDAY);
  while ($current->compareDateIso($end) < 0) {
    $current->setDay(1);
    if($position == 5) $current->addMonth(1); else $current->subDay(1);
    $current->custom($pattern);
    $of = &OccurrenceFactory::getInstance();
    $of->addOccurrence($event, clone $current, $entity, $entity_id, $entity_state);
    $current->setDay(1)->addMonth($repeatfrequence);
  }	
}


///////////////////////////////////////////////////////////////////////////////
// Perform the monthly by date repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function calendar_monthlybydate_repeatition($date,$start,$end,$repeatfrequence,$event,$entity_id,$entity, $entity_state) {  
  global $day_duration;

  $current = clone $date;
  if ($current->compare($start) < 0) {
    $diff = ($start->getYear() - $current->getYear()) * 12 + ($start->getMonth() - $current->getMonth());
    if ($diff > 0) {
      $diff = $diff  % $repeatfrequence;
    }
    $current->setMonth($start)->subMonth($diff);    
  }	
  while ($current->compareDateIso($end) < 0) {
    $of = &OccurrenceFactory::getInstance();
    $of->addOccurrence($event, clone $current, $entity, $entity_id, $entity_state);
    $current->addMonth($repeatfrequence);
  }  
}


///////////////////////////////////////////////////////////////////////////////
// Perform the yearly repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function calendar_yearly_repeatition($date,$start,$end,$repeatfrequence,$event,$entity_id,$entity, $entity_state) {
  global $day_duration;

  $current = clone $date;
  if ($current->compare($start) < 0) {
    $diff = $start->getYear() - $current->getYear();
    if ($diff > 0) {
      $diff = $diff  % $repeatfrequence;
    }
    $current->setYear($start)->subYear($diff); 
  } 
    
  while ($current->compareDateIso($end) < 0) {
    $of = &OccurrenceFactory::getInstance();
    $of->addOccurrence($event, clone $current, $entity, $entity_id, $entity_state);
    $current->addYear($repeatfrequence);
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
        if (!empty($value)) {
          $users = explode(",", $value);
          if (is_array($users)) {
            foreach($users as $user) {
              array_push($url_param, "sel_ent[]=data-user-$user");
            }
          } else {
            array_push($url_param, "sel_ent[]=data-user-$value"); 
          }
        }
        break;
      case "resources":
        if (!empty($value)) {
          $resources = explode(",", $value);
          if (is_array($resources)) {
            foreach($resources as $resource) {
              array_push($url_param, "sel_ent[]=data-resource-$resource");
            }
          } else {
            array_push($url_param, "sel_ent[]=data-resource-$value"); 
          }
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
  global $c_all;

  $calendar_bookmarkproperties = array('category', 'cal_range', 'cal_view');

  $obm_q = new DB_OBM;

  $label = $calendar['view_label'];
  // Category
  $category = $calendar['category_filter'];
  if (empty($category)) {
    $category = $c_all;
  }
  
  // Entity
  $group = $calendar['entity']['group'][0];
  if (empty($group)) {
    $users = is_array($calendar['entity']['user']) ? implode(',', array_unique($calendar['entity']['user'])) : '';
    array_push($calendar_bookmarkproperties, 'users');
  } else {
    array_push($calendar_bookmarkproperties, 'group');
  }
  $resources = is_array($calendar['entity']['resource']) ? implode(',', array_unique($calendar['entity']['resource'])) : '';
  if (!empty($resources)) {
    array_push($calendar_bookmarkproperties, 'resources');
  }

  // Range
  $cal_range = $_SESSION['cal_range'];

  // View
  $cal_view = $_SESSION['cal_view'];

  $user_id = sql_parse_id($obm['uid']);
  // Insert ObmBookmark
  $query = "INSERT INTO ObmBookmark(
    obmbookmark_user_id,
    obmbookmark_label,
    obmbookmark_entity) VALUES (
    $user_id,
    '$label',
    'calendar')";
  $obm_q->query($query);

  $user_id = sql_parse_id($obm['uid'], true);
  // Select obmbookmark_id
  $query = "SELECT obmbookmark_id FROM ObmBookmark
    WHERE obmbookmark_user_id $user_id 
    AND obmbookmark_label='$label'
    AND obmbookmark_entity='calendar'";
  $obm_q->query($query);
  $obm_q->next_record();
  $obmbookmark_id = sql_parse_id( $obm_q->f('obmbookmark_id'));

  // Insert ObmBookmark properties 
  if (is_array($calendar_bookmarkproperties)) {
    foreach($calendar_bookmarkproperties as $property) {
      $query = "INSERT INTO ObmBookmarkProperty (
        obmbookmarkproperty_bookmark_id,
        obmbookmarkproperty_property,
        obmbookmarkproperty_value) VALUES (
        $obmbookmark_id,
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

  $id = sql_parse_id( $calendar['view_id'], true);

  // Delete view
  $query = "DELETE FROM ObmBookmark WHERE obmbookmark_id $id"; 
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
  var $timezone;

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
   * setTimezone 
   * 
   * @param mixed $timezone 
   * @access public
   * @return void
   */
  function setTimezone($timezone) {
    $this->timezone = $timezone;
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

  function isPrivate() {
    return ($this->privacy == 1);
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
  function addAttendee($entity,$entity_id,$entity_label, $entity_state) {
    $this->attendee[$entity][$entity_id] = $entity_label.'('.$GLOBALS["l_$entity_state"].')';
  }

}

/**
 * Describe an occurrence of an event 
 */
Class Occurrence {
  var $event;
  var $date;
  var $end;
  var $entity;
  var $id;
  var $entity_state;

  /**
   * Occurrence
   *
   * @param mixed $event
   * @param mixed $date
   * @param mixed $entity
   * @param mixed $id
   * @param mixed $state entity state
   * @access public
   * @return void
   */
  function Occurrence($event, $date, $entity, $id, $state) {
    $this->event = $event;
    $this->date = $date;
    $this->end = clone $this->date;
    $this->end->addSecond($event->duration);
    $this->entity = $entity;
    $this->id = $id;
    $this->entity_state = $state;
  }

}

/**
 * Store all Event/Occurrence/Date relations 
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
   * add an occurrence for a given event (add the event if not set)
   * 
   * @param Event $event
   * @param timestamp $date
   * @param string $entity
   * @param string $id entity id
   * @param mixed $state entity state
   * @access public
   * @return void
   */
  function addOccurrence($event, $date, $entity, $id, $state='ACCEPTED') {
    $date->setDefaultTimeZone();
    if ($date->compare($this->end) < 0 && ($date->getTimestamp() + $event->duration) > $this->begin->getTimestamp()) {
      $this->occurrences[$event->id][] = &new Occurrence($event, $date, $entity, $id, $state);
      $this->events[$event->id] = $event;
    }
  }

  /**
   * remove all occurrence of an event 
   * 
   * @param event id $id 
   * @param timestamp $date 
   * @access public
   * @return void
   */
  function removeOccurrences($id, $date) {
    foreach($this->occurrences[$id] as $key => $occurrence) {
      if($occurrence->date->compareDateIso($date) == 0) {
        unset($this->occurrences[$id][$key]); 
      }
    }
  }

  /**
   * Returl all occurrences
   * 
   * @access public
   * @return array
   */
  function getOccurrences() {
    $return = array();
    foreach($this->occurrences as $evtOccurrences) {
      $return = array_merge($return, $evtOccurrences);
    }
    return $return;
  }

  static function getInstance() {
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
  function eventHaveOccurrences($id) {
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
  function periodHaveOccurrences($begin, $end  = null, $entityFilter=null) {
    if(is_null($end)) {
      $end = clone $begin;
      $end->addDay(1);
    }    
    foreach($this->occurrences as $evtOccurrences) {
      foreach($evtOccurrences as $occurrence) {
        if(!$entityFilter || ( 
           is_array($entityFilter[$occurrence->entity]) && (
             count($entityFilter[$occurrence->entity]) == 0 || 
             in_array($occurrence->id,$entityFilter[$occurrence->entity])))) {
          $eventDuration = $occurrence->event->duration;
          $eventDate = $occurrence->date;
          $eventEnd = $occurrence->end;
          $eventAllDay = $occurrence->event->all_day;
          if($eventAllDay && $eventEnd->compareDateIso($begin) > 0 && $eventDate->compareDateIso($end) < 0) {
            return true;
          } 
          if(!$eventAllDay && $eventEnd->compare($begin) > 0 && $eventDate->compare($end) < 0) {
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
  function getPeriodOccurrences($begin, $end = null, $entityFilter=null, $eventFilter=0) {
    $occurrences = array();
    if(is_null($end)) {
      $end = clone $begin;
      $end->addDay(1);
    }
    foreach($this->occurrences as $evtOccurrences) {
      foreach($evtOccurrences as $occurrence) {
        if(!$entityFilter || ( 
           is_array($entityFilter[$occurrence->entity]) && (
             count($entityFilter[$occurrence->entity]) == 0 || 
             in_array($occurrence->id,$entityFilter[$occurrence->entity])))) {
          $eventDuration = $occurrence->event->duration;
          $eventBegin = $occurrence->date;
          $eventEnd = $occurrence->end;
          $eventAllDay = $occurrence->event->all_day;
          if($eventAllDay && $eventFilter != 1 && $begin->compareDateIso($eventEnd) <= 0 && $end->compareDateIso($eventBegin) >= 0) {
            array_unshift($occurrences,$occurrence);
          } 
          if(!$eventAllDay && $eventFilter != 2 && $begin->compare($eventEnd) < 0 && $end->compareDateIso($eventBegin) > 0) {
            array_push($occurrences,$occurrence);
          }
        }
      }
    }
    if (count($occurrence) > 0) {
      return $occurrences;
    } else {
      return NULL;
    }
  }

  function reset() {
    $instance = &OccurrenceFactory::getInstance();
    unset($instance->occurrences);
    unset($instance->events);
    $instance->occurrences = array();
    $instance->events = array();
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
  global $cgp_host, $ccalendar_send_ics, $obm, $cdg_sql;
  global $l_add_event_mail_subject, $l_update_event_mail_subject, $l_delete_event_mail_subject;
  global $l_add_event_mail_body, $l_update_event_mail_body, $l_delete_event_mail_body;
  global $l_add_resource_mail_body, $l_update_resource_mail_body, $l_delete_resource_mail_body;
  global $l_add_exception_mail_body, $l_add_exception_mail_subject;
  global $l_delete_exception_mail_body, $l_delete_exception_mail_subject;
  global $l_update_state_mail_subject, $l_update_state_mail_body;
  global $l_accepted, $l_rejected, $l_waiting;

  $mail_datas = array('reset_state' => false, 'reset_resource_state' => false, 
                'add_attendee' => false, 'remove_attendee' => false,
                'add_exception' => false, 'remove_exception' => false,
                'update_decision' => false, 'add_resource' => false,
                'remove_resource' => false, 'mails' => array());
  
  $accept_link = "$cgp_host/calendar/calendar_index.php?action=update_decision&calendar_id=$calendar[calendar_id]&entity_kind=user&rd_decision_event=ACCEPTED";
  $refuse_link = "$cgp_host/calendar/calendar_index.php?action=update_decision&calendar_id=$calendar[calendar_id]&entity_kind=user&rd_decision_event=DECLINED";
  $consult_link = "$cgp_host/calendar/calendar_index.php?action=detailconsult&calendar_id=$calendar[calendar_id]";
  
  $userinfo = get_user_info();
  $groups = $calendar['sel_group_id'];
  if (is_array($groups)) {
    $user_group_array = array();
    foreach($groups as $group_id) {
      $user_group_array = array_merge($user_group_array, of_usergroup_get_group_users($group_id));
    }
    $calendar['sel_user_id'] = array_merge($calendar['sel_user_id'], $user_group_array);
    $calendar['sel_user_id'] = array_unique($calendar['sel_user_id']);
  }

  if($action == 'update') {
    if(!$event_q) {
      $event_q = run_query_calendar_detail($calendar['calendar_id']);
    }

    


    $oldtitle = $event_q->f('event_title');
    $old_start = new Of_Date($event_q->f('event_date'), 'GMT');
    $old_end = clone $old_start;
    $old_end->addSecond($event_q->f('event_duration'));
    $old_location = $event_q->f('event_location');
    $old_all_day = $event_q->f('event_allday');
    $old_duration = $event_q->f('event_duration');
    $old_repeatkind = $event_q->f('event_repeatkind');
    $old_repeatdays = $event_q->f('event_repeatdays');
    $old_repeatend = new Of_Date($event_q->f('event_endrepeat'),'GMT');
    $old_repeatfrequence = $event_q->f('event_repeatfrequence');
    $title = $calendar['title'];
    $start =  $calendar['date_begin'];
    $end = $calendar['date_end'];
    $location = $calendar['location'];
    ($calendar['all_day'] != 1)? $all_day = FALSE : $all_day = TRUE;
    $duration = $calendar['event_duration'];
    $repeatkind = $calendar['repeat_kind'];
    $repeatdays = $calendar['repeat_days'];
    $repeatend = $calendar['repeat_end'];
    $repeatfrequence = $calendar['repeatfrequency'];
    $owner = $event_q->f('owner_lastname').' '.$event_q->f('owner_firstname');

    $mail_datas['reset_state'] = (($location != $old_location) || $mail_datas['reset_state']);
    $mail_datas['reset_state'] = (($all_day != $old_all_day) || $mail_datas['reset_state']);
    $mail_datas['reset_state'] = (($start->compare($old_start) != 0) || $mail_datas['reset_state']);
    $mail_datas['reset_state'] = (($duration != $old_duration) || $mail_datas['reset_state']);
    $mail_datas['reset_state'] = (($repeatkind != $old_repeatkind) || $mail_datas['reset_state']);
    if($repeatkind != 'none') {
      $mail_datas['reset_state'] = (($repeatdays != $old_repeatdays) || $mail_datas['reset_state']);
      $mail_datas['reset_state'] = (($repeatend->compare($old_repeatend) != 0) || $mail_datas['reset_state']);
      $mail_datas['reset_state'] = (($repeatfrequence != $old_repeatfrequence) || $mail_datas['reset_state']);
    }


    $date_exception = array();
    $exceptions = array(); 
    $p_exceptions = array();
    $new_exceptions = array();
    $old_exceptions = array();
    $exceptions_q = run_query_get_one_event_exception($calendar['calendar_id']);
    $entities = get_calendar_event_entity($calendar['calendar_id']); 
    
    if(is_array($entities['user']['ids'])) {
      $attendees_q = $entities['user']['ids'];
    } else {
      $attendees_q = array();
    }
  
    if(is_array($entities['resource']['ids'])) {
      $resources_q = $entities['resource']['ids'];
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
      $date_exception = $calendar['date_exception'];
      if(!is_array($date_exception)) {
        $date_exception = array();
      }
      while($exceptions_q->next_record()) {
        $exceptions[] = new Of_Date($exceptions_q->f('eventexception_date'), 'GMT');
      }
      $old_exceptions = array_udiff($exceptions,$date_exception, array('Of_Date', 'cmpDate'));
      $new_exceptions = array_udiff($date_exception,$exceptions, array('Of_Date', 'cmpDate'));
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
    $start =  $calendar['date_begin'];
    $end = $calendar['date_end'];
    $location = $calendar['location'];
  } elseif ($action == 'delete') {
    $event_q = run_query_calendar_detail($calendar['calendar_id']);
    $old_title = $event_q->f('event_title');
    $old_start = new Of_Date($event_q->f('event_date'), 'GMT');
    $old_end = clone $start;
    $old_end->addSecond($event_q->f('event_duration'));
    $old_location = $event_q->f('event_location');
    $entities = get_calendar_event_entity($calendar['calendar_id']); 
    $old_resources = $entities['resource']['ids'];
    if(is_array($old_resources)) {
      $mail_datas['remove_resource'] = true;
    }
    if (is_array($entities['user']['ids'])) {
      $old_attendees = array_diff($entities['user']['ids'], array($obm['uid']));
    } else {
      $old_attendees = array();
    }
    $mail_datas['remove_attendee'] = true;    
  } elseif ($action == 'quick_delete') {
    if($event_q->f('event_repeatkind') != 'none') {
      $title = $event_q->f('event_title');
      $new_exceptions[] = $calendar['old_date_begin'];  
      $entities = get_calendar_event_entity($calendar['calendar_id']); 
      $old_resources = $entities['resource']['ids'];
      if (is_array($entities['user']['ids'])) {
	$attendees = array_diff($entities['user']['ids'], array($obm['uid']));
      } else {
	$attendees = array();
      }
      $mail_datas['add_exception'] = true;
    } else {
      $old_title = $event_q->f('event_title');
      $old_start = new Of_Date($event_q->f('event_date'), 'GMT');
      $old_end = clone $old_start;
      $old_end->addSecond($event_q->f('event_duration'));
      $old_location = $event_q->f('event_location');
      $entities = get_calendar_event_entity($calendar['calendar_id']); 
      $old_resources = $entities['resource']['ids'];
      if(is_array($old_resources)) {
        $mail_datas['remove_resource'] = true;
      }
      if (is_array($entities['user']['ids'])) {
	$old_attendees = array_diff($entities['user']['ids'], array($obm['uid']));
      } else {
	$old_attendees = array();
      }
      $mail_datas['remove_attendee'] = true;    
    }
  } elseif ($action == 'quick_update') {
    $old_title = $event_q->f('event_title');
    $old_start = new Of_Date($event_q->f('event_date'), 'GMT');
    $old_duration = $event_q->f('event_duration');
    $old_end = clone $old_start;
    $old_end->addSecond($event_q->f('event_duration'));
    $title = $calendar['title'];
    $start =  $calendar['date_begin'];
    $duration = $calendar['duration'];
    $end = clone $start;
    $end->addSecond($duration);
    $mail_datas['reset_state'] = (($start != $old_start) || $mail_datas['reset_state']);
    $mail_datas['reset_state'] = (($duration != $old_duration) || $mail_datas['reset_state']);
    $entities = get_calendar_event_entity($calendar['calendar_id']); 
    $resources = $entities['resource']['ids'];
    $attendees = array_diff($entities['user']['ids'], array($obm['uid']));
    $mail_datas['reset_resource_state'] = $mail_datas['reset_state'] && (count($resources) > 0);
  } elseif ($action == 'update_decision' || $action == 'decision') {
    if(!$event_q) {
      $event_q = run_query_calendar_detail($calendar['calendar_id']);
    }    
    if($calendar['entity_kind'] != 'user' || $event_q->f('event_owner') != $obm['uid']) {
      $decision = $calendar['decision_event'];
      $event_q = run_query_calendar_detail($calendar['calendar_id']);
      $title = $event_q->f('event_title');
      $start = new Of_Date($event_q->f('event_date'), 'GMT');
      $entityinfo = get_entity_info($calendar['entity_id'],$calendar['entity_kind']);

      $attendees = array($event_q->f('event_owner'));
      $mail_datas['update_decision'] = true;
    }
  }
  $date = new Of_Date();
  if($mail_datas['reset_state']) {
    $mail_datas['mails']['reset_state'] = array (
      'subject' => sprintf($l_update_event_mail_subject,$old_title),
      'message' => sprintf($l_update_event_mail_body, $old_title, $old_start->getOutputDateTime(), 
                           $old_end->getOutputDateTime(), $old_location, $start->getOutputDateTime(), $end->getOutputDateTime(), 
                           $location,$consult_link,$accept_link,$refuse_link,
                           $date->getOutputDate(), $date->get(Of_Date::TIME_SHORT), $userinfo['firstname'],
                           $userinfo['lastname'],$cgp_host),
      'recipients' => $attendees
    );
  }
  if($mail_datas['reset_resource_state']) {
    $rattendees =  of_right_users_for_entity('resource', $resources, 'write');
    if(!in_array($obm['uid'], $rattendees['ids'])) {
      $rattendees = array_diff($rattendees['ids'], array($obm['uid']));
      $mail_datas['mails']['reset_resource_state'] = array (
        'subject' => sprintf($l_update_event_mail_subject,$old_title),
        'message' => sprintf($l_update_resource_mail_body, $old_title, $old_start->getOutputDateTime(), 
                             $old_end->getOutputDateTime(), $old_location, $start->getOutputDateTime(), $end->getOutputDateTime(), 
                             $location,$consult_link,
                             $date->getOutputDate(), $date->get(Of_Date::TIME_SHORT), $userinfo['firstname'],
                             $userinfo['lastname'],$cgp_host),
        'recipients' => $rattendees
      );
    }
  }   
  if ($mail_datas['add_attendee']) {
    if ($ccalendar_send_ics) {
      $tmp_filename = secure_tmpname('.ics','ics_');
      create_calendar_ics_meeting_file($calendar, $calendar['calendar_id'], $calendar['sel_user_id'], array(), $tmp_filename);
      $h_file[] = array(
         'filename' => "$tmp_filename",
         'name' => 'meeting.ics',
         'type' => 'ics'
      );
    }
    $mail_datas['mails']['add_attendee'] = array (
      'subject' => sprintf($l_add_event_mail_subject,$title),
      'message' => sprintf($l_add_event_mail_body, $start->getOutputDateTime(), $end->getOutputDateTime(), 
                           $title, $location, $consult_link,$accept_link,$refuse_link,
                           $date->getOutputDate(), $date->get(Of_Date::TIME_SHORT), $userinfo['firstname'],
                           $userinfo['lastname'],$cgp_host),
      'recipients' => $new_attendees,
      'h_file' => $h_file
    );
  }
  if ($mail_datas['add_resource']) {
    $rattendees =  of_right_users_for_entity('resource', $new_resources, 'write');
    if(!in_array($obm['uid'], $rattendees['ids'])) {
      $rattendees = array_diff($rattendees['ids'], array($obm['uid']));    
      $mail_datas['mails']['add_resource'] = array (
        'subject' => sprintf($l_add_event_mail_subject,$title),
        'message' => sprintf($l_add_resource_mail_body, $start->getOutputDateTime(), $end->getOutputDateTime() ,
                             $title, $location, $consult_link,
                             $date->getOutputDate(), $date->get(Of_Date::TIME_SHORT), $userinfo['firstname'],
                             $userinfo['lastname'],$cgp_host),
        'recipients' => $rattendees
      );
    }
  }
  if ($mail_datas['remove_attendee']) {
    $mail_datas['mails']['remove_attendee'] = array (
      'subject' => sprintf($l_delete_event_mail_subject,$old_title),
      'message' => sprintf($l_delete_event_mail_body, $old_title, $old_start->getOutputDateTime(), 
                           $old_end->getOutputDateTime(), $old_location,$userinfo['email'], 
                           $date->getOutputDate(), $date->get(Of_Date::TIME_SHORT), $userinfo['firstname'],
                           $userinfo['lastname'],$cgp_host),
      'recipients' => $old_attendees
    );
  }
  if ($mail_datas['remove_resource']) {
    $rattendees =  of_right_users_for_entity('resource', $old_resources, 'write');
    if(!in_array($obm['uid'], $rattendees['ids'])) {
      $rattendees = array_diff($rattendees['ids'], array($obm['uid']));    
      $mail_datas['mails']['remove_resource'] = array (
        'subject' => sprintf($l_delete_event_mail_subject,$old_title),
        'message' => sprintf($l_delete_resource_mail_body, $old_title, $old_start->getOutputDateTime(), 
                             $old_end->getOutputDateTime(), $old_location,$userinfo['email'], 
                             $userinfo['lastname'],$cgp_host),
        'recipients' => $rattendees
      );
    }
  }  
  if ($mail_datas['add_exception']) {
    $rattendees =  of_right_users_for_entity('resource', $resources, 'write');
    $rattendees = array_diff($rattendees['ids'], array($obm['uid']));    
    $attendees = array_merge($attendees, $rattendees);    
    $mail_datas['mails']['add_exception'] = array (
      'subject' => sprintf($l_add_exception_mail_subject,$title),
      'message' => sprintf($l_add_exception_mail_body,$title,implode(', ', $new_exceptions),$consult_link, 
                           $date->getOutputDate(), $date->get(Of_Date::TIME_SHORT), $userinfo['firstname'],
                           $userinfo['lastname'],$cgp_host),
      'recipients' => $attendees
    );
  }
  if ($mail_datas['remove_exception']) {
    $rattendees =  of_right_users_for_entity('resource', $resources, 'write');
    $rattendees = array_diff($rattendees['ids'], array($obm['uid']));    
    $attendees = array_merge($attendees, $rattendees);        
    $mail_datas['mails']['remove_exception'] = array (
      'subject' => sprintf($l_delete_exception_mail_subject,$title),
      'message' => sprintf($l_delete_exception_mail_body,$title,implode(', ', $old_exceptions),$consult_link, 
                           $date->getOutputDate(), $date->get(Of_Date::TIME_SHORT), $userinfo['firstname'],
                           $userinfo['lastname'],$cgp_host),
      'recipients' => $attendees
    );
  }
  if ($mail_datas['update_decision']) {
    switch ($decision) {
    case 'ACCEPTED':
      $decision_label = $l_accepted;
      break;
    case 'NEEDS-ACTION':
      $decision_label = $l_waiting;
      break;
    case 'DECLINED':
      $decision_label = $l_rejected;
      break;
    default:
      $decision_label = $decision;
    }

    $mail_datas['mails']['update_decision'] = array (
      'subject' => sprintf($l_update_state_mail_subject,$title),
      'message' => sprintf($l_update_state_mail_body, $entityinfo['label'],
                           $title, $decision_label, $consult_link, 
                           $date->getOutputDate(), $date->get(Of_Date::TIME_SHORT), $userinfo['firstname'],
                           $userinfo['lastname'],$cgp_host),
      'recipients' => $attendees
    );
  }

  return $mail_datas;
}


function calendar_send_mail($mails) {
  foreach($mails['mails'] as $mail) {
    send_mail(stripslashes($mail['subject']), $mail['message'], $mail['recipients'], array(), $mail, $mail['h_file']);
  }
}


/**
 * Reset the user calendar. 
 * 
 * @access public
 * @return void
 */
function run_query_calendar_reset($id) {
  global $cdg_sql, $obm;
  $obm_q = new DB_OBM;
  $calendar['mail'] = false;
  $id =  $id; 
  $writable = of_right_entity_for_user('calendar', $obm['uid'], 'write', '', 'userobm');
  $writable = $writable['ids'];
  # Delete all events where the user is the owner and an attendee and all events where the owner
  # is writable and the user is the only attendee
  $sql_id = sql_parse_id($id, true);
  $query = "SELECT event_id FROM Event LEFT JOIN EventEntity ON event_id = evententity_event_id
            WHERE (event_type='VEVENT' AND event_owner $sql_id AND evententity_entity_id $sql_id AND evententity_entity = 'user')
            UNION
            SELECT event_id FROM Event LEFT JOIN EventEntity ON event_id = evententity_event_id
            WHERE event_type='VEVENT' AND event_owner IN  (".implode(',',$writable).") AND evententity_entity = 'user'
            GROUP BY event_id HAVING MAX(evententity_entity_id) = MIN(evententity_entity_id)";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_reset()');
  $obm_q->query($query);
  
  while($obm_q->next_record()) {
    $calendar['calendar_id'] = $obm_q->f('event_id');
    run_query_calendar_delete($calendar);
  }

  # Refuse all other events
  $query = "SELECT evententity_event_id FROM EventEntity WHERE evententity_entity_id $id and evententity_entity = 'user'
            AND evententity_state != 'DECLINED'";
  $obm_q->query($query);
  while($obm_q->next_record()) {
    run_query_calendar_update_occurrence_state($obm_q->f('evententity_event_id'), 'user', $id, 'DECLINED');
  }
  # Delete all alerts

  $query = "DELETE FROM EventAlert WHERE eventalert_event_id NOT IN (SELECT evententity_event_id FROM EventEntity) 
                  OR eventalert_user_id $id"; 
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_reset()');
  $obm_q->query($query);
}


?>
