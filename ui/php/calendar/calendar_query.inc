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
//   - $calendar_id (can be Event table dependant if is_numeric(), or world-wide
//                   unique if begins with "OBM-")
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_detail($calendar_id) {
  global $cdg_sql;

  if(preg_match('/^OBM-/', $calendar_id)) {
    $get_id = "event_ext_id = '".addslashes($calendar_id)."'";
  } else {
    $calendar_id = sql_parse_id($calendar_id, true);
    $get_id = "event_id $calendar_id";
  }

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type; 
  $timeupdate = sql_date_format($db_type, 'event_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'event_timecreate', 'timecreate');
  $multidomain = sql_multidomain('event');
  $eventexception_sql_if = sql_if($db_type, 'ee.eventexception_parent_id IS  NULL', '0', '1');
  $query = "SELECT 
    event_id,
    event_ext_id,
    e.evententity_entity_id AS event_entity_id,
    c.userobm_login AS usercreate,
    u.userobm_login AS userupdate,
    $timeupdate,
    $timecreate,
    event_owner,
    o.userobm_lastname AS owner_lastname,
    o.userobm_firstname AS owner_firstname,
    event_opacity,
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
    event_tag_id,
    eventtag_color,
    eventtag_label,
    event_endrepeat,
    event_allow_documents,
    event_date,
    $eventexception_sql_if AS event_is_exception,
    ee.eventexception_parent_id AS event_eventexception_parent_id,
    ee.eventexception_date AS event_eventexception_date
    FROM
    Event
    INNER JOIN EventEntity AS e ON event_id = evententity_event_id
    LEFT JOIN EventCategory1 ON event_category1_id = eventcategory1_id
    INNER JOIN UserObm AS c ON event_usercreate = c.userobm_id
    LEFT JOIN UserObm AS u ON event_userupdate = u.userobm_id
    INNER JOIN UserObm AS o ON event_owner = o.userobm_id
    LEFT JOIN EventTag AS t ON event_tag_id = t.eventtag_id
    LEFT JOIN EventException AS ee ON event_id = ee.eventexception_child_id
    WHERE event_type='VEVENT'
    AND $get_id
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
function get_calendar_user_name($users='') {
  global $cdg_sql;

  $multidomain = sql_multidomain('userobm');

  $in = implode (",", $users);

  $obm_q = new DB_OBM;
  $query = "SELECT userobm_lastname, userobm_firstname, userobm_id
    FROM UserObm
    WHERE userobm_id IN ($in) $multidomain";
  display_debug_msg($query, $cdg_sql, 'get_calendar_user_name()');
  $obm_q->query($query);

  $u = array();
  while ($obm_q->next_record()) {
    $id = $obm_q->f('userobm_id');
    $firstname = $obm_q->f('userobm_firstname');
    $lastname = $obm_q->f('userobm_lastname');
    $u[$id] = "$firstname $lastname";
  }

  // Preserve the users order
  $names = array();
  foreach ($users as $id) {
    $names[$id] = $u[$id];
  } 

  return $names;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the label of resources
// Parameters:
//   - $resources : resource id array, the event is assigned to
///////////////////////////////////////////////////////////////////////////////
function get_calendar_resource_label($resources) {
  global $cdg_sql;

  if (count($resources) > 0) {
    $in = implode (",", $resources);
    $where = " resource_id IN ($in)";
    $and = true;
  }
  $multidomain = sql_multidomain('resource',$and);
  $and = false; 
  $obm_q = new DB_OBM;
  $query = "SELECT resource_name, resource_id
    FROM Resource 
    WHERE $where $multidomain";
  display_debug_msg($query, $cdg_sql, 'get_calendar_resource_label()');
  $obm_q->query($query);

  $r = array();
  while ($obm_q->next_record()) {
    $id = $obm_q->f('resource_id');
    $name = $obm_q->f('resource_name');
    $r[$id] = "$name";
  }

  $names = array();
  // Preserve the users order
  foreach ($resources as $id) {
    $names[$id] = $r[$id];
  } 

  return $names;
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
function get_calendar_entity_label($entities) {

  if ((is_array($entities['user']))
    && (count($entities['user']) > 0)) {
      $ret['user'] = get_calendar_user_name($entities['user']);
    }
  if ((is_array($entities['resource']))
    && (count($entities['resource']) > 0)) {
      $ret['resource'] = get_calendar_resource_label($entities['resource']);
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
  $owner_notification = $calendar['owner_notification'];

  run_query_calendar_update_occurrence_state($event_id,$entity,$entity_id,$state, $owner_notification);
}


///////////////////////////////////////////////////////////////////////////////
// Update a user decision for its participation to an event
// Parameters: 
//   - $event_id : event Id
//   - $user_id  : User id
//   - $state    : New state to set (ACCEPTED, NEEDS-ACTION or DECLINED)
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_update_occurrence_state($event_id, $entity, $entity_id,$state, $owner_notification = true, $force = false) {
  global $cdg_sql;
  global $obm, $err, $l_of_right_err_auth;

  $oldEvent = OBM_EventFactory::getInstance()->getById($event_id);

  if (!$force && ($obm['uid'] != $entity_id || $entity != 'user')) {
    if ($entity == 'user') {
      $ok = OBM_Acl::canWrite($obm['uid'], 'calendar', $entity_id);
    } elseif ($entity == 'resource') {
      $ok = OBM_Acl::canWrite($obm['uid'], 'resource', $entity_id);
    } elseif ($entity == 'contact') {
      $ok = true;
    }

    if (!$ok) {
      $err['msg'] = $l_of_right_err_auth;  
      return false;
    } 
  }

  $id = sql_parse_id($event_id, true);
  $query = "UPDATE EventLink 
    SET eventlink_state = '$state'
    WHERE eventlink_entity_id = ".of_entity_subquery($entity, $entity_id)." 
    AND eventlink_event_id $id";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_occurrence_state()');  
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    $query = "UPDATE Event
      SET event_timeupdate = NOW()
      WHERE event_id $id";

    display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_occurrence_state()');
    $retour = $obm_q->query($query);

    // Send mail
    if ($owner_notification == 'true') {
      OBM_EventFactory::getInstance()->store(OBM_EventFactory::getInstance()->getById($event_id), $oldEvent);
    }

  }

  return $retour;    
}


/**
 * Check if the user participation to en event differs from the one given
 *
 * @param
 * return true if new participation differs from current 
 **/
function check_calendar_event_participation($params) {
  global $obm;

  $event_id = $params['calendar_id'];
  $entity = $params['entity_kind'];
  $entity_id = $params['entity_id'];
  $state = $params['decision_event'];

  if (empty($entity_id) || $state == get_calendar_event_participation_state($event_id, $entity, $entity_id)) {
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

  $query = "SELECT eventlink_state
    FROM EventLink 
    WHERE eventlink_entity_id = ".of_entity_subquery($entity, $entity_id)."
    AND eventlink_event_id = '$event_id'";

  display_debug_msg($query, $cdg_sql, 'get_calendar_event_participation_state()');  
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  if ($obm_q->num_rows() == 1) {
    $obm_q->next_record();
    $state = $obm_q->f('eventlink_state');
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
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $concat = array (
    array (
      'type' => 'field',
      'value' => 'attendee.userobm_lastname'
    ),
    array (
      'type' => 'string',
      'value' => ' '
    ),
    array (
      'type' => 'field',
      'value' => 'attendee.userobm_firstname'
    ) 
  );
  $entity_label = sql_string_concat($db_type,$concat);

  $query = "SELECT
    u.userobm_lastname as usercreate_lastname,
    u.userobm_firstname as usercreate_firstname,
    $entity_label AS entity_label,
    attendee.userobm_id AS entity_id,
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
    INNER JOIN EventLink ON event_id  = eventlink_event_id
    INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id
    INNER JOIN UserObm attendee ON userentity_user_id = attendee.userobm_id
    INNER JOIN UserObm u ON event_owner = u.userobm_id
    WHERE
    event_type='VEVENT'
    AND eventlink_state = 'NEEDS-ACTION'
    $multidomain
    AND ( 1=0
    ";
  $writable_cal = array_keys(OBM_Acl::getAllowedEntities($uid, 'calendar', 'write'));
  $writable_res = array_keys(OBM_Acl::getAllowedEntities($uid, 'resource', 'write'));

  if (count($writable_cal) > 0) {
    $query .= "OR userentity_user_id IN (".implode(",", $writable_cal).")";    
  }
  $query .= " OR userentity_user_id = '$uid')";

  if (count($writable_res) > 0) {
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
      INNER JOIN EventLink ON event_id  = eventlink_event_id
      INNER JOIN ResourceEntity ON resourceentity_entity_id = eventlink_entity_id
      INNER JOIN Resource ON resourceentity_resource_id = resource_id
      INNER JOIN UserObm u ON event_owner = u.userobm_id
      WHERE
      event_type='VEVENT'
      AND eventlink_state = 'NEEDS-ACTION'
      $multidomain
      AND (resourceentity_resource_id IN (".implode(",", $writable_res)."))";
  }
  $query .= " ORDER BY entity_entity, entity_label, event_date"; 
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_waiting_events()');
  $obm_q->query($query);

  return $obm_q;
}

function get_json_waiting_events($id) {
  global $obm, $cdg_sql, $display;
  global $l_waiting_events;

  $multidomain = sql_multidomain('event');
  $obm_q = new DB_OBM;

  $query = "
    SELECT count(event_id) as nb_event
    FROM Event
    INNER JOIN EventLink ON event_id  = eventlink_event_id
    INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id
    INNER JOIN UserObm attendee ON userentity_user_id = attendee.userobm_id
    INNER JOIN UserObm u ON event_owner = u.userobm_id
    WHERE
    event_type='VEVENT'
    AND eventlink_state = 'NEEDS-ACTION'
    AND userentity_user_id = $id
    ";

  $obm_q->query($query);
  if ($obm_q->num_rows() == 1) {
    $obm_q->next_record();
    $nb_event = $obm_q->f('nb_event');
    if($nb_event != 0) {
      $display['json'] = "{msg : '".phpStringToJsString($l_waiting_events.' : '.$nb_event)."'}";
    } else {
      $display['json'] = "{msg : '".phpStringToJsString($l_waiting_events.' : 0')."'}";
    }

  }
}

///////////////////////////////////////////////////////////////////////////////
// Delete all participation to an event
// Parameters:
//   - $calendar[] : hash values for the event
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_delete_event_entity($calendar, $users = null) {
  global $cdg_sql;

  $e_id = sql_parse_id( $calendar['calendar_id'], true);
  $query = "DELETE FROM EventLink WHERE eventlink_event_id  $e_id"; 
  if (is_array($users)) {
    if (count($users) != 0) {
      $query .= " AND eventlink_entity_id NOT IN ".of_entity_subquery('user', $users, true);
    }
  } else {
    $query .= " AND eventlink_entity_id NOT IN ".of_entity_subquery('user')."";
  }

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete_event_entity()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
} 

/**
 * run_query_expand_groups 
 * 
 * @param mixed $groups 
 * @access public
 * @return void
 */
function run_query_calendar_merge_groups($groups, $users) {
  $users = (is_array($users)?$users:array());
  if (is_array($groups)) {
    foreach($groups as $group_id) {
      $users = array_merge($users, of_usergroup_get_group_users($group_id));
    }
    $users = array_unique($users);
  }
  return $users;
}


///////////////////////////////////////////////////////////////////////////////
// Insert one event
// Parameters: 
//   - $calendar      : Calendar params
//   - $sel_entity_id : List entitis id ['user'] ['resource']
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_add_event($calendar, $sel_entity_id) {
  global $obm, $ccalendar_resource_admin, $ccalendar_send_ics;

  $groups = $sel_entity_id['group'];
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
    $users_array = array($obm['uid']);
  }

  // Get contacts list
  $contacts_array = $sel_entity_id['contact'];
  
  $documents_array = $sel_entity_id['document'];

  // Insert the Event
  list($event_id, $event_entity_id) = run_query_calendar_insert_event_data($calendar);

  // Insert Event users
  foreach ($users_array as $user_id) {
    if ($user_id == $obm['uid'] || OBM_Acl::canWrite($obm['uid'], 'calendar', $user_id)) {
      run_query_calendar_insert_entity_event('user', $user_id, $event_id, 'ACCEPTED');
    } else {
      run_query_calendar_insert_entity_event('user', $user_id, $event_id, 'NEEDS-ACTION');
    }
  }

  // Insert Event contacts
  if (is_array($contacts_array)) {
    $contacts_array = array_unique($contacts_array);
    foreach ($contacts_array as $contact_id) {
      run_query_calendar_insert_entity_event('contact', $contact_id, $event_id, 'NEEDS-ACTION');
    }
  }

  // Insert Event resources
  $resources_writable = array(); // Writable Resources
  $resources_address = array(); // Resource Admin notification
  // Get the final resource list and insert resource links
  if ( (is_array($resources_array)) && (count($resources_array) > 0) ) {
    $resources_array = array_unique($resources_array);
    foreach ($resources_array as $res_id) {
      if (OBM_Acl::canWrite($obm['uid'], 'resource', $res_id)) {
        run_query_calendar_insert_entity_event('resource', $res_id, $event_id, 'ACCEPTED');
      } else {
        run_query_calendar_insert_entity_event('resource', $res_id, $event_id, 'NEEDS-ACTION');
      }
      array_push($resources_writable, $res_id);
    }
  }
  
  // Insert Event documents
  if (is_array($documents_array)) {
    $documents_array = array_unique($documents_array);
    foreach ($documents_array as $document_id) {
      run_query_calendar_attach_document($document_id, $event_entity_id);
    }
  }

  // Insert My alert if set
  if (($calendar['alert']) > 0) {
    run_query_calendar_event_alert_insert($event_id, $obm['uid'], $calendar['alert']);
  }

  $calendar['calendar_id'] = $event_id;
  if ($calendar['attendees_notification']) {
    $data = array ( 
      'id' => $event_id,
      'title' => "$calendar[title]" ,
      'owner' => "$calendar[owner]" ,
      'location' => "$calendar[location]" ,
      'category1' => "$calendar[category1]" ,
      'privacy' => $calendar['privacy'],
      'date_begin' => $calendar['date_begin'],
      'date_end' => $calendar['date_end'],
      'priority' => "$calendar[priority]" ,
      'color' =>  "$calendar[color]" ,
      'repeat_kind' => "$calendar[repeat_kind]" ,
      'repeatfrequency' => "$calendar[repeatfrequency]" ,
      'repeat_end' => $calendar['repeat_end'],
      'date_exception' => $calendar['date_exception'] ,
      'description' => "$calendar[description]" ,
      'duration' => $calendar['duration'],
      'repeat_days' => "$calendar[repeat_days]", 
      'user' => $users_array,
      'resource' => $resources_array,
      'contact' => $contacts_array
    );
    OBM_EventFactory::getInstance()->create($data);
  }
  return $event_id;
}


///////////////////////////////////////////////////////////////////////////////
// Insert an event data
// Parameters:
//   - $calendar : hashed calendar params
// Returns: array(event id inserted, event entity id inserted)
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_insert_event_data($calendar) {
  global $cdg_sql, $obm;

  if (array_key_exists('ext_id', $calendar)) {
    $ext_id = $calendar['ext_id'];
  } else {
    $ext_id = genUniqueExtEventId();
  }
  if (!is_numeric($calendar['owner']) || $calendar['owner'] <= 0) {
    $owner = $obm['uid'];
  } else {
    $owner = $calendar['owner'];
  }
  $title = sql_parse_text($calendar['title']);
  $category1_id = sql_parse_id( $calendar['category1']);
  $priority = sql_parse_int( $calendar['priority']);
  $description = sql_parse_text($calendar['description']);
  $location = sql_parse_text($calendar['location']);
  ($calendar['all_day'] != 1)? $all_day = 'FALSE' : $all_day = 'TRUE';
  $datebegin = $calendar['date_begin'];
  $event_duration = $calendar['event_duration'];
  if ($calendar['privacy']!=1) $privacy = 0; else $privacy = 1;
  $repeat_kind = $calendar['repeat_kind'];
  $repeat_days = $calendar['repeat_days'];
  if (isset($calendar['repeat_end'])) {
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

  $event_tag_id = run_query_calendar_create_or_update_tag($calendar);
  
  if ($calendar['allow_documents'] != 1) $allow_documents = 0; else $allow_documents = 1; 
  $opacity = $calendar['opacity'];
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
  if($all_day == 'TRUE') {
    $dateend = clone $datebegin;
    $dateend->addSecond($event_duration - 1);
    $dateend->setHour(0)->setMinute(0)->setSecond(0)->addDay(1);
    $datebegin->setHour(0)->setMinute(0)->setSecond(0);
    $event_duration = $dateend->diffTimestamp($datebegin);
  }
  if($repeat_kind == 'weekly') {
    $days = str_split($repeat_days);
    $event_day = $datebegin->getWeekday();
    if($days[$event_day] != 1) {
      for($i = ($event_day +1)%7; $i != $event_day; $i = ($i+1)%7) {
        if($days[$i] == 1) {
          $datebegin->setWeekday($i);
          break;
        } 
      }      
    } 
  } else {
    $repeat_days = '0000000';
  }

  $query = "INSERT INTO Event (
    event_ext_id,
    event_timeupdate,                         
    event_timecreate,                         
    event_usercreate,
    event_type,
    event_origin,
    event_owner,
    event_timezone,
    event_opacity,
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
    event_tag_id, 
    event_allow_documents,
    event_endrepeat,
    event_domain_id)
    VALUES (
    '$ext_id',
      NOW(),
    NOW(),
    '$obm[uid]',
    'VEVENT',
    '$GLOBALS[c_origin_web]',
    '$owner',
    '".Of_Date::getOption('timezone')."',
    '$opacity', 
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
    $event_tag_id,
    '$allow_documents',
    $repeat_end,
    $obm[domain_id])";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_event_data()');
  $obm_q = new DB_OBM();
  $obm_q->query($query);

  $id = $obm_q->lastid();
  $calendar['calendar_id'] = $id;
  $eid = of_entity_insert('event',$id);
  if($repeat_kind != 'none' && is_array($date_exception)) {
    foreach($date_exception as $key => $value) {
      $calendar['old_date_begin'] = $value->setHour($datebegin)->setMinute($datebegin);        
      run_query_calendar_event_exception_insert($calendar);
    }
  }

  return array($id, $eid);
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
  $entity_id = sql_parse_id($entity_id);
  $uid = sql_parse_id($obm['uid']);
  $query = "INSERT INTO EventLink (
    eventlink_timecreate,
    eventlink_usercreate,
    eventlink_event_id,
    eventlink_entity_id,
    eventlink_required,
    eventlink_state)
    VALUES (
      NOW(),
    $uid,
    $event_id,
    ".of_entity_subquery($entity, $entity_id).",
    'REQ',
    '$state')";

  display_debug_msg($query, $cdg_sql, 'run_query_entity_user_event()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q; 
}

///////////////////////////////////////////////////////////////////////////////
// Get document infos from the document ids array given
// Parameters:
//   - $documents : array of document ids
// Returns:
// array [ids], [entity] (label=>)
///////////////////////////////////////////////////////////////////////////////
function get_calendar_document_from_ids($documents) {
  global $cdg_sql;

  $multidomain = sql_multidomain('document');
  $where = '('.implode(',', $documents).')';
  $query = "SELECT *
    FROM Document 
    WHERE document_id IN $where $multidomain
    ORDER by document_name";
  display_debug_msg($query, $cdg_sql, 'get_calendar_document_from_ids()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  while ($obm_q->next_record()) {
    $id = $obm_q->f('document_id');
    $name = $obm_q->f('document_name');
    $docs['ids'][] = $id;
    $docs['entity'][$id] = array('label' => $name);
  }

  return $docs;
}

///////////////////////////////////////////////////////////////////////////////
// Attach a document to an event
// Parameters: 
//   - $document_id :
//   - $event_id  :
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_attach_document($document_id, $event_id) {
  global $cdg_sql, $obm;

  $event_id = $event_id;
  $document_id = sql_parse_id($document_id);
  $uid = sql_parse_id($obm['uid']);
  $query = "INSERT INTO DocumentLink (
    documentlink_document_id,
    documentlink_entity_id,
    documentlink_usercreate)
    VALUES (
    $document_id,
    $event_id,
    $uid)";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_attach_document()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q; 
}

///////////////////////////////////////////////////////////////////////////////
// Detach a document from an event
// Parameters: 
//   - $document_id :
//   - $event_id  :
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_detach_document($document_id, $event_id) {
  global $cdg_sql, $obm;

  $event_id = sql_parse_id($event_id);
  $document_id = sql_parse_id($document_id);
  $query = "DELETE FROM DocumentLink 
    WHERE documentlink_document_id = $document_id
    AND documentlink_entity_id = $event_id";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_detach_document()');
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
  global $obm;
  global $all_emails;

  // --- get the unfolding ics
  $ics = $params['ics_tmp'];
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


///////////////////////////////////////////////////////////////////////////////
// Check for conflict : Only with the first occurrence of each event,
// Parameters:
//   - $calendar     : calendar parameters
//   - sel_entity_id : selected entities id
///////////////////////////////////////////////////////////////////////////////
function check_calendar_conflict($calendar, $sel_entity_id) {

  $begin = clone $calendar['date_begin'];
  $end = clone $calendar['date_end'];
  $begin->setHour(0)->setMinute(0)->setSecond(0); 
  $end->addDay(1)->setHour(0)->setMinute(0)->setSecond(0);

  $cal_entities = store_calendar_entities(get_calendar_entity_label($sel_entity_id));
  $occurrences = calendar_events_model($begin,$end,$cal_entities);
  $of = &OccurrenceFactory::getInstance(); 
  if(!$calendar['all_day']) {
    $occurrences = $of->getPeriodOccurrences($calendar['date_begin'], $calendar['date_end']);
  }

  if( !is_null($occurrences)) {
    if(isset($calendar['calendar_id'])) {
      $event_id = $calendar["calendar_id"];
    } else {
      $event_id = NULL;
    }
    foreach($occurrences as $occurrence) {
      if($occurrence->event->id != $event_id && $occurrence->event->isOpaque()) {
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
// Check for conflict : Only with the first occurrence of each event,
// Parameters:
//   - $calendar       : calendar parameters
///////////////////////////////////////////////////////////////////////////////
function quick_check_calendar_conflict($calendar, $entities) {

  $begin = clone $calendar['date_begin'];
  $end = clone $calendar['date_end'];
  $begin->setHour(0)->setMinute(0)->setSecond(0); 
  $end->addDay(1)->setHour(0)->setMinute(0)->setSecond(0);

  // Event entities
  $sel_entity_id['user'] = $entities['user']['ids'];
  $sel_entity_id['resource'] = $entities['resource']['ids'];

  $cal_entities = store_calendar_entities(get_calendar_entity_label($sel_entity_id));
  $occurrences = calendar_events_model($begin,$end,$cal_entities);
  $of = &OccurrenceFactory::getInstance(); 
  if(!$calendar['all_day']) {
    $occurrences = $of->getPeriodOccurrences($calendar['date_begin'], $calendar['date_end']);
  }

  if(!is_null($occurrences)) {
    if(isset($calendar['calendar_id'])) {
      $event_id = $calendar["calendar_id"];
    } else {
      $event_id = NULL;
    }
    foreach($occurrences as $occurrence) {
      if($occurrence->event->id != $event_id && $occurrence->event->isOpaque()) {
        return true;
      }
    }
  }
  return false;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the user is authorized to access calendars and resources
///////////////////////////////////////////////////////////////////////////////
function check_access_entity($calendar_ids, $resource_ids) {
  global $obm, $err;
  global $l_of_right_err_user, $l_of_right_err_resource;
  $ok = true;
  if(isset($calendar_ids) && !empty($calendar_ids)) {
    $err['msg'] = $l_of_right_err_user;
    $ok = OBM_Acl::areAllowed($obm['uid'], 'calendar', $calendar_ids, 'access');
  }
  if(isset($resource_ids) &&  !empty($calendar_ids)) {
    $err['msg'] = $l_of_right_err_resource;
    $ok = OBM_Acl::areAllowed($obm['uid'], 'resource', $resource_ids, 'access');
  }

  return $ok;
}


/**
 * Check for participation decision
 * Essentially a hook container to allow specific rules
 * @param $calendar parameters
 * @return  
 **/
function check_calendar_participation_decision($calendar) {

  if (function_exists('hook_pre_check_calendar_participation_decision')) {
    if (! hook_pre_check_calendar_participation_decision($calendar)) {
      return false;
    }
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check for conflict : Only with the first occurrence of each event
///////////////////////////////////////////////////////////////////////////////
function check_calendar_decision_conflict($calendar) {

  if ($calendar['decision_event'] != 'ACCEPTED') {
    return false;
  }
  $event_id = $calendar['calendar_id'];
  $sel_entity_id[$calendar['entity_kind']][] = $calendar['entity_id'];
  $e_q = run_query_calendar_detail($event_id);
  $begin = new Of_Date($e_q->f('event_date'),'GMT');
  $end = clone $begin;
  $end->addSecond($e_q->f('event_duration'));
  if ($e_q->f('all_day')) {
    $begin->setHour(0)->setMinute(0)->setSecond(0);
    $end->addDay(1)->setHour(0)->setMinute(0)->setSecond(0);
  }

  $cal_entities = store_calendar_entities(get_calendar_entity_label($sel_entity_id));
  $occurrences = calendar_events_model($begin,$end,$cal_entities);
  if (!is_null($occurrences)) {
    if(isset($calendar['calendar_id'])) {
      $event_id = $calendar["calendar_id"];
    } else {
      $event_id = NULL;
    }
    $event_id = $calendar['calendar_id'];
    foreach($occurrences as $occurrence) {
      if ($occurrence->event->id != $event_id && $occurrence->event->isOpaque()) {
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
// Event Update Execution
// Parameters: 
//   - $calendar      : Calendar params
//   - $sel_entity_id : List entitis id ['user'] ['resource']
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_event_update(&$calendar, $sel_entity_id, $event_id, $reset_state) {
  global $c_use_connectors, $obm, $ccalendar_resource_admin;
  global $obm ;

  $datebegin = $calendar['date_begin'];
  $dateend = $calendar['date_end'];
  $event_id =  $calendar['calendar_id'];
  $event_entity_id = of_entity_get('event', $event_id);
  $groups = $sel_entity_id['group'];
  $mail = $calendar['mail'];
  $mail_title = stripslashes($calendar['title']);

  $oldEvent = OBM_EventFactory::getInstance()->getById($calendar['calendar_id']);

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
  } else {
    $resources_array = array();
  }
  
  // Get the final document list
  $documents_array = $sel_entity_id['document'];
  if ((is_array($documents_array)) && (count($documents_array) > 0) ) {
    $documents_array = array_unique($documents_array);
  } else {
    $documents_array = array();
  }

  // Get contacts list
  $contacts_array = $sel_entity_id['contact'];

  if (!is_array($resources_array) && !is_array($users_array)) {
    $users_array = array($obm['uid']);
  }

  // Handle Connectors data
  if ($c_use_connectors) {
    $obm_q = new DB_OBM;
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
          deletedevent_origin,
          deletedevent_type,
          deletedevent_timestamp)
          VALUES (
            $event_id,
            $u_id,
            '$GLOBALS[c_origin_web]',
            'VEVENT',
            NOW())";
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


  if (!OBM_EventMailObserver::hasEventChanged($oldEvent, OBM_EventFactory::getInstance()->getById($calendar['calendar_id']))) {
    $entities = get_calendar_event_entity($event_id); 
    if (count($users_array) > 0) {
      $qusers = "AND eventlink_entity_id NOT IN ".of_entity_subquery('user',$users_array);
    }
    if (count($resources_array) > 0) {
      $rresources = "AND eventlink_entity_id NOT IN ".of_entity_subquery('resource',$resources_array);
    }
    if (count($documents_array) > 0) {
      $qdocs = "AND documentlink_document_id NOT IN (".implode(',', $documents_array).")";
    }
    if (is_array($entities['user']['ids'])) {
      $users_array = array_diff($users_array,$entities['user']['ids']);
    }
    if (is_array($entities['resource']['ids'])) {
      $resources_array = array_diff($resources_array, $entities['resource']['ids']);
    }
    if (is_array($entities['document']['ids'])) {
      $documents_array = array_diff($documents_array, $entities['document']['ids']);
    }
  }

  $query = "DELETE FROM EventLink WHERE 1=1 $qusers $rresources AND eventlink_event_id = $event_id";
  $db = new DB_OBM;
  $db->query($query);
  
  $query = "DELETE FROM DocumentLink WHERE 1=1 $qdocs AND documentlink_entity_id = $event_entity_id";
  $db = new DB_OBM;
  $db->query($query);

  // User insertion
  if (is_array($users_array)) {
    foreach($users_array as $user_id) {
      if ($user_id == $obm['uid'] || OBM_Acl::canWrite($obm['uid'], 'calendar', $user_id)) {
        run_query_calendar_insert_entity_event('user', $user_id, $event_id, 'ACCEPTED');
      } else {
        run_query_calendar_insert_entity_event('user', $user_id, $event_id, 'NEEDS-ACTION');
      }
    }
  }

  // Insert Event contacts
  if (is_array($contacts_array)) {
    $contacts_array = array_unique($contacts_array);
    foreach ($contacts_array as $contact_id) {
      run_query_calendar_insert_entity_event('contact', $contact_id, $event_id, 'NEEDS-ACTION');
    }
  }

  if (is_array($resources_array)) {
    foreach ($resources_array as $res_id) {
      if (OBM_Acl::canWrite($obm['uid'], 'resource', $res_id)) {
        run_query_calendar_insert_entity_event('resource', $res_id, $event_id, 'ACCEPTED');
      } else {
        run_query_calendar_insert_entity_event('resource', $res_id, $event_id, 'NEEDS-ACTION');
      }
    }
  }
  
  // Insert Event documents
  if (is_array($documents_array)) {
    foreach ($documents_array as $document_id) {
      run_query_calendar_attach_document($document_id, $event_entity_id);
    }
  }

  // Insert My alert if set
  if (($calendar['alert']) > 0) {
    run_query_calendar_event_alert_insert($event_id, $obm['uid'], $calendar['alert']);
  }

  // Send mail
  if ($calendar['attendees_notification']) {
    OBM_EventFactory::getInstance()->store(
      OBM_EventFactory::getInstance()->getById($calendar['calendar_id']),
      $oldEvent);
  }

}


///////////////////////////////////////////////////////////////////////////////
// Event Data Update Execution
// Parameters: 
//   - $calendar : Calendar params
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_update_event_data($calendar) {
  global $cdg_sql, $obm, $c_none;
  $title = sql_parse_text($calendar['title']);
  if (is_numeric($calendar['owner']) && $calendar['owner'] >= 0) {
    $owner = "event_owner = $calendar[owner],";
  }
  $category1_id = sql_parse_id( $calendar['category1']);
  $priority = $calendar['priority'];
  $description = sql_parse_text($calendar['description']);
  $location = sql_parse_text($calendar['location']);
  $datebegin = $calendar['date_begin'];
  $event_duration = $calendar['event_duration'];
  ($calendar['all_day'] != 1) ? $all_day = 'FALSE': $all_day = 'TRUE';
  $event_id = sql_parse_id( $calendar['calendar_id'], true); 
  if ($calendar['privacy'] != 1) $privacy = 0; else $privacy = 1; 
  $repeat_kind = $calendar['repeat_kind'];
  $repeat_days = $calendar['repeat_days'];
  if (isset($calendar['repeat_end'])) {
    $repeat_end = "'$calendar[repeat_end]'";
  } else {
    $repeat_end = 'NULL';
  }    
  $frequency = $calendar['repeatfrequency']; 
  $color = $calendar['color'];
  if ($calendar['allow_documents'] != 1) $allow_documents = 0; else $allow_documents = 1; 
  $date_exception = $calendar['date_exception'];
  $item = $calendar['list-item'];
  $item_id = $calendar['list-item-id'];
  $res_name = $calendar['res-name'];
  $res_id = $calendar['res-id'];
  $property = $calendar['property'];
  $property_name = $calendar['property-name'];
  $opacity = $calendar['opacity'];
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
  
  $event_tag_id = run_query_calendar_create_or_update_tag($calendar);

  if ($all_day == 'TRUE') {
    $dateend = clone $datebegin;
    $dateend->addSecond($event_duration - 1);
    $dateend->setHour(0)->setMinute(0)->setSecond(0)->addDay(1);
    $datebegin->setHour(0)->setMinute(0)->setSecond(0);
    $event_duration = $dateend->diffTimestamp($datebegin);
  }
  if ($repeat_kind == 'weekly') {
    $days = str_split($repeat_days);
    $event_day = $datebegin->getWeekday();
    if ($days[$event_day] != 1) {
      for($i = ($event_day +1)%7; $i != $event_day; $i = ($i+1)%7) {
        if ($days[$i] == 1) {
          $datebegin->setWeekday($i);
          break;
        }
      }
    }
  } else {
    $repeat_days = '0000000';
  }

  $obm_q = new DB_OBM;
  $query = "UPDATE Event SET
    event_timeupdate = NOW(),
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
      event_opacity = '$opacity', 
      event_duration = '$event_duration',
      event_allday = $all_day, 
      event_tag_id = $event_tag_id,
      event_allow_documents = '$allow_documents',
      event_origin = '$GLOBALS[c_origin_web]',
      event_repeatkind = '$repeat_kind',
      event_repeatfrequence = '$frequency',
      event_repeatdays = '$repeat_days',
      event_endrepeat = $repeat_end
      WHERE
      event_id $event_id"; 


  display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_event_data()');
  $obm_q->query($query);

  $query = "DELETE FROM EventException WHERE eventexception_parent_id $event_id"; 
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_event_data(2)');
  $obm_q->query($query);

  if ($repeat_kind != 'none' && is_array($date_exception)) {
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
  $all_day = ($calendar['all_day'])? 'TRUE' : 'FALSE';
  $title = sql_parse_text($calendar['title']);
  $datebegin = $calendar['date_begin'];
  $event_duration = $calendar['duration'];
  $event_id = sql_parse_id($calendar['calendar_id'], true);
  if ($all_day == 'TRUE') {
    $dateend = clone $datebegin;
    $dateend->addSecond($event_duration - 1);
    $dateend->setHour(0)->setMinute(0)->setSecond(0)->addDay(1);
    $datebegin->setHour(0)->setMinute(0)->setSecond(0);
    $event_duration = $dateend->diffTimestamp($datebegin);
  }
  $oldEvent = OBM_EventFactory::getInstance()->getById($calendar['calendar_id']);
  $obm_q = new DB_OBM;
  $query = "UPDATE Event SET
    event_timeupdate = NOW(),
      event_userupdate = $obm[uid],
      event_title = '$title',
      event_date = '$datebegin',
      event_origin = '$GLOBALS[c_origin_ajax]',
      event_duration = '$event_duration'
      WHERE
      event_id $event_id $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_event_data()');
  $obm_q->query($query);

  // Send mail
  if ($calendar['send_mail']) {
    $newEvent = OBM_EventFactory::getInstance()->getById($calendar['calendar_id']);
    OBM_EventFactory::getInstance()->store($newEvent,$oldEvent);
  }
}


/**
 * Event Data  Execution for ajax client 
 * 
 * @param mixed $calendar 
 * @access public
 * @return void
 */
function run_query_calendar_quick_event_insert($calendar,$state) {
  global $cdg_sql, $obm;

  $title = sql_parse_text($calendar['title']);
  $datebegin = $calendar['date_begin'];
  $event_duration = $calendar['duration'];
  $all_day = ($calendar['all_day'])? 'TRUE' : 'FALSE';
  if ($all_day == 'TRUE') {
    $dateend = clone $datebegin;
    $dateend->addSecond($event_duration - 1);
    $dateend->setHour(0)->setMinute(0)->setSecond(0)->addDay(1);
    $datebegin->setHour(0)->setMinute(0)->setSecond(0);
    $event_duration = $dateend->diffTimestamp($datebegin);
  }
  $ext_id = genUniqueExtEventId();

  $obm_q = new DB_OBM;
  $query = "INSERT INTO Event
    (event_timecreate,
    event_timeupdate,
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
    event_domain_id,
    event_ext_id
  )
  VALUES (
    NOW(),
    NOW(),
    $obm[uid],
    'VEVENT',
    '$GLOBALS[c_origin_ajax]',
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
    $obm[domain_id],
    '$ext_id'
  )";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_quick_insert_event_data()');
  $obm_q->query($query);
  $event_id = $obm_q->lastid();
  of_entity_insert('event', $event_id);
  run_query_calendar_insert_entity_event('user', $calendar['entity_id'], $event_id, $state);
  if ($calendar['send_mail']) {
    // Send mail
    $data = array (
      'id' => $event_id
    );
    OBM_EventFactory::getInstance()->create($data);
  }
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

  $title = sql_parse_text($calendar['title']);
  $id = $calendar['calendar_id'];
  $olddatebegin = $calendar['old_date_begin'];
  $event_duration = $calendar['duration'];
  $oldEvent = OBM_EventFactory::getInstance()->getById($id);

  $obm_q = new DB_OBM;

  if (is_object($event_q)) {
    if ($event_q->f('event_privacy')!=1) $privacy = 0; else $privacy = 1; 
    $repeat_kind = $event_q->f('event_repeatkind');
    $repeat_days = $event_q->f('event_repeatdays');
    if ($event_q->f('event_endrepeat')) {
      $repeat_end = new Of_Date($event_q->f('event_endrepeat'), 'GMT');
      $repeat_end = "'$repeat_end'";
    } else {
      $repeat_end = 'NULL';
    }
    $repeatfrequency = $event_q->f('event_repeatfrequence');
    $category1_id = sql_parse_id( $event_q->f('event_category1_id'));
    $priority = sql_parse_int( $event_q->f('event_priority'));
    $event_tag_id = $event_q->f('tag_id') ;
    if ($event_tag_id == $GLOBALS['c_none']) $event_tag_id = 'NULL' ;
    if (is_null($event_tag_id)) $event_tag_id = 'NULL' ;
    $description = $event_q->f('event_description');
    $properties = $event_q->f('event_properties');
    $location = $event_q->f('event_location');
    ($calendar['all_day'] != 1)? $all_day = 'FALSE' : $all_day = 'TRUE';
    $datebegin = $calendar['date_begin'];
    $ext_id = genUniqueExtEventId();

    $query = "INSERT INTO Event (
      event_ext_id,
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
      event_tag_id,
      event_endrepeat,
      event_domain_id
    )
    VALUES (
      '$ext_id',
      NOW(),
      NOW(),
      $obm[uid],
      'VEVENT',
      '$GLOBALS[c_origin_web]',
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
      $event_tag_id,
      $repeat_end,
      '$obm[domain_id]'
    )";

    display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_event_data(0)');
    $obm_q->query($query);
    $new_id = $obm_q->lastid();
    of_entity_insert('event', $new_id);
    $entities = get_calendar_event_entity($id);
    foreach ($entities as $entity => $data) {
      foreach ($data['entity'] as $entity_id => $entity_data) {
        run_query_calendar_insert_entity_event($entity, $entity_id, $new_id, $entity_data['state']);
      }
    }
  }

  $sql_id = sql_parse_id($id);
  $sql_new_id = ( $new_id ? sql_parse_id($new_id) : 'NULL' );
  $query = "UPDATE Event SET event_timeupdate = NOW() WHERE event_id = $sql_id";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_exception_insert(1)');
  $obm_q->query($query);
  $query = "INSERT INTO EventException (
    eventexception_usercreate,
    eventexception_timecreate,
    eventexception_userupdate,
    eventexception_timeupdate,
    eventexception_parent_id,
    eventexception_child_id,
    eventexception_date)
    VALUES (
      $obm[uid],
      NOW(),
        $obm[uid],
        NOW(),
        $sql_id,
        $sql_new_id,
        '$olddatebegin'
      )";

  $sql_id = sql_parse_id($id, true);
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_exception_insert(2)');
  $obm_q->query($query);
  $query = "Update Event SET
    event_userupdate = $obm[uid],
    event_timeupdate = NOW()
    WHERE event_id $sql_id";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_exception_insert(3)');
  $obm_q->query($query);
  // Send mail
  if ($calendar['send_mail']) {
    $newEvent = OBM_EventFactory::getInstance()->getById($calendar['calendar_id']);
    OBM_EventFactory::getInstance()->store($newEvent,$oldEvent);
  }

  if ($new_id) {
    if ($calendar['send_mail']) {
      OBM_EventFactory::getInstance()->create(array('id' => $new_id));
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

  $event_id = $eve_q->f('event_id');
  $entities = get_calendar_event_entity($event_id);
  run_query_calendar_delete_event_entity($params,array());
  foreach ($entities as $entity => $data) {
    foreach ($data['entity'] as $entity_id => $entity_data) {
      $entity_type = ($entity == 'user') ? 'calendar' : 'resource';
      if (OBM_Acl::canWrite($obm['uid'], $entity_type, $entity_id)) {
        run_query_calendar_insert_entity_event($entity, $entity_id, $event_id, 'ACCEPTED');
      } else {
        run_query_calendar_insert_entity_event($entity, $entity_id, $event_id, 'NEEDS-ACTION');
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
          NOW(),
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

/**
 * Insert an event template
 * 
 * @param $calendar array of calendar params
 * @access public
 * @return integer event template id inserted
 */
function run_query_calendar_create_or_update_event_template($calendar) {
  global $cdg_sql, $obm;

  // Insert "others attendees" as private contacts
  if ($calendar['others_attendees'] != "") {
    $others_attendees = run_query_insert_others_attendees($calendar);
    $calendar['sel_contact_id'] = is_array($calendar['sel_contact_id']) 
                              ? array_merge($calendar['sel_contact_id'], $others_attendees)
                              : $others_attendees;
  }
  // Insert "other files" as private documents
  if (is_array($calendar['other_files'])) {
    $other_files = run_query_insert_other_files($calendar);
    $calendar['sel_document_id'] = is_array($calendar['sel_document_id'])
                               ? array_merge($calendar['sel_document_id'], $other_files)
                               : $other_files;
  }
  
  if (!is_numeric($calendar['owner']) || $calendar['owner'] <= 0) {
    $owner = $obm['uid'];
  } else {
    $owner = $calendar['owner'];
  }
  $name = sql_parse_text($calendar['template_name']);
  $title = sql_parse_text($calendar['title']);
  $category1_id = sql_parse_id($calendar['category1']);
  $priority = sql_parse_int($calendar['priority']);
  $description = sql_parse_text($calendar['description']);
  $location = sql_parse_text($calendar['location']);
  $alert = sql_parse_int($calendar['alert']);
  $all_day = ($calendar['all_day'] != 1) ? 'FALSE' : 'TRUE';
  $datebegin = $calendar['date_begin'];
  $event_duration = $calendar['event_duration'];
  $privacy = ($calendar['privacy'] != 1) ? 0 : 1;
  $repeat_kind = $calendar['repeat_kind'];
  $repeat_days = $calendar['repeat_days'];
  $repeat_end = isset($calendar['repeat_end']) ? "'$calendar[repeat_end]'" : 'NULL';
  $repeatfrequency = $calendar['repeatfrequency'];
  $date_exception = $calendar['date_exception'];
  $allow_documents = ($calendar['allow_documents'] != 1) ? 0 : 1;
  
  $user_ids = is_array($calendar['sel_user_id']) ? "'".implode(',', $calendar['sel_user_id'])."'" : 'NULL';
  $contact_ids = is_array($calendar['sel_contact_id']) ? "'".implode(',', $calendar['sel_contact_id'])."'" : 'NULL';
  $resource_ids = is_array($calendar['sel_resource_id']) ? "'".implode(',', $calendar['sel_resource_id'])."'" : 'NULL';
  $group_ids = is_array($calendar['sel_group_id']) ? "'".implode(',', $calendar['sel_group_id'])."'" : 'NULL';
  $document_ids = is_array($calendar['sel_document_id']) ? "'".implode(',', $calendar['sel_document_id'])."'" : 'NULL';

  $event_tag_id = run_query_calendar_create_or_update_tag($calendar);
  
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
  if($all_day == 'TRUE') {
    $dateend = clone $datebegin;
    $dateend->addSecond($event_duration - 1);
    $dateend->setHour(0)->setMinute(0)->setSecond(0)->addDay(1);
    $datebegin->setHour(0)->setMinute(0)->setSecond(0);
    $event_duration = $dateend->diffTimestamp($datebegin);
  }
  if($repeat_kind == 'weekly') {
    $days = str_split($repeat_days);
    $event_day = $datebegin->getWeekday();
    if($days[$event_day] != 1) {
      for($i = ($event_day +1)%7; $i != $event_day; $i = ($i+1)%7) {
        if($days[$i] == 1) {
          $datebegin->setWeekday($i);
          break;
        } 
      }      
    } 
  } else {
    $repeat_days = '0000000';
  }
  
  if (isset($calendar['template_id'])) {
    $template_id = (int) $calendar['template_id'];
    $query = "UPDATE EventTemplate SET
      eventtemplate_timeupdate = NOW(),
      eventtemplate_owner = '$owner',
      eventtemplate_title = '$title', 
      eventtemplate_date = '$datebegin',     
      eventtemplate_description = '$description',
      eventtemplate_properties = '$properties',
      eventtemplate_location = '$location',
      eventtemplate_category1_id = $category1_id,
      eventtemplate_priority = $priority,
      eventtemplate_privacy = $privacy,
      eventtemplate_duration = '$event_duration',
      eventtemplate_repeatkind = '$repeat_kind',
      eventtemplate_repeatfrequence = '$repeatfrequency',
      eventtemplate_repeatdays = '$repeat_days',
      eventtemplate_allday = $all_day,
      eventtemplate_tag_id = $event_tag_id,
      eventtemplate_allow_documents = '$allow_documents',
      eventtemplate_alert = $alert,
      eventtemplate_endrepeat = $repeat_end,
      eventtemplate_user_ids = $user_ids,
      eventtemplate_contact_ids = $contact_ids,
      eventtemplate_resource_ids = $resource_ids,
      eventtemplate_group_ids = $group_ids,
      eventtemplate_document_ids = $document_ids 
      WHERE eventtemplate_id = $template_id";
  } else {
    $query = "INSERT INTO EventTemplate (
      eventtemplate_name,
      eventtemplate_timeupdate,                         
      eventtemplate_timecreate,                         
      eventtemplate_usercreate,
      eventtemplate_owner,
      eventtemplate_title, 
      eventtemplate_date,     
      eventtemplate_description,
      eventtemplate_properties,
      eventtemplate_location, 
      eventtemplate_category1_id,
      eventtemplate_priority,
      eventtemplate_privacy, 
      eventtemplate_duration,
      eventtemplate_repeatkind,
      eventtemplate_repeatfrequence,
      eventtemplate_repeatdays,
      eventtemplate_allday,
      eventtemplate_tag_id, 
      eventtemplate_allow_documents,
      eventtemplate_alert,
      eventtemplate_endrepeat,
      eventtemplate_domain_id,
      eventtemplate_user_ids,
      eventtemplate_contact_ids,
      eventtemplate_resource_ids,
      eventtemplate_group_ids,
      eventtemplate_document_ids)
      VALUES (
      '$name',
      NOW(),
      NOW(),
      '$obm[uid]',
      '$owner',
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
      $event_tag_id,
      '$allow_documents',
      $alert,
      $repeat_end,
      $obm[domain_id],
      $user_ids,
      $contact_ids,
      $resource_ids,
      $group_ids,
      $document_ids)";
  }

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_create_or_update_event_template()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  $id = $obm_q->lastid();
  
  return $id;
}

/**
 * Get all user-defined event templates
 * 
 * @param $user_id current user id
 * @access public
 * @return DBO
 */
function run_query_calendar_get_alltemplates($user_id) {
  global $cdg_sql;
  
  $query = "SELECT eventtemplate_id, eventtemplate_name FROM EventTemplate 
    WHERE eventtemplate_usercreate = $user_id 
    ORDER BY eventtemplate_name";
  
  $obm_q = new DB_OBM ;
  $obm_q->query($query) ;
  display_debug_msg($query, $cdg_sql,"run_query_calendar_get_alltemplates()");
  
  return $obm_q ;
}

/**
 * Get an event template
 * 
 * @param $template_id
 * @access public
 * @return array
 */
function run_query_calendar_get_template($template_id) {
  global $cdg_sql;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $template_id = sql_parse_id($template_id, true);
  $get_id = "eventtemplate_id $template_id";
  $multidomain = sql_multidomain('eventtemplate');
  $query = "SELECT 
    eventtemplate_id,
    eventtemplate_name,
    eventtemplate_owner AS event_owner,
    o.userobm_lastname AS owner_lastname,
    o.userobm_firstname AS owner_firstname,
    eventtemplate_title AS event_title,
    eventtemplate_description AS event_description,
    eventtemplate_properties AS event_properties,
    eventtemplate_repeatfrequence AS event_repeatfrequence,
    eventtemplate_location AS event_location,
    eventtemplate_category1_id AS event_category1_id,
    eventcategory1_label,
    eventtemplate_privacy AS event_privacy,
    eventtemplate_priority AS event_priority,
    eventtemplate_repeatkind AS event_repeatkind,
    eventtemplate_duration AS event_duration,
    eventtemplate_repeatdays AS event_repeatdays,
    eventtemplate_allday AS event_allday,
    eventtemplate_tag_id AS event_tag_id,
    eventtag_color,
    eventtag_label,
    eventtemplate_endrepeat AS event_endrepeat,
    eventtemplate_allow_documents AS event_allow_documents,
    eventtemplate_alert AS event_alert,
    eventtemplate_date AS event_date,
    eventtemplate_user_ids AS event_user_ids,
    eventtemplate_contact_ids AS event_contact_ids,
    eventtemplate_resource_ids AS event_resource_ids,
    eventtemplate_group_ids AS event_group_ids,
    eventtemplate_document_ids AS event_document_ids
    FROM
    EventTemplate 
    LEFT JOIN EventCategory1 ON eventtemplate_category1_id = eventcategory1_id 
    INNER JOIN UserObm AS o ON eventtemplate_owner = o.userobm_id 
    LEFT JOIN EventTag AS t ON eventtemplate_tag_id = t.eventtag_id 
    WHERE $get_id
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_get_template()');
  $obm_q->query($query);
  $obm_q->next_record();
  
  $entities = array();
  foreach (array('user', 'contact', 'resource', 'group', 'document') as $type) {
    $value = $obm_q->f("event_{$type}_ids");
    if (!empty($value)) {
      $entities[$type] = explode(',', $value);
    } else {
      $entities[$type] = array();
    }
  }
  
  return array($obm_q, $entities);
}

/**
 * Get one (or many) event template(s) details
 * 
 * @param $columns array of columns to be retrieved
 * @param $template_id optional
 * @access public
 * @return DBO
 */
function run_query_calendar_get_templates_details($columns, $template_id = null) {
  global $obm, $cdg_sql;
  
  if (count($columns) == 0) {
    $columns = '*';
  } else {
    foreach ($columns as $k => $c) {
      $columns[$k] = 'eventtemplate_'.$c;
    }
    $columns = implode(',', $columns);
  }
  
  $query = "SELECT $columns FROM EventTemplate ";
  if (is_null($template_id)) {
    $query.= "WHERE eventtemplate_usercreate = $obm[uid]";
  } else {
    $template_id = (int) $template_id;
    $query.= "WHERE eventtemplate_id = $template_id";
  }
  
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_get_templates_details()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}

/**
 * Duplicate an event template
 * 
 * @param $template_id
 * @param $duplicate_name
 * @access public
 * @return integer duplicate ID
 */
function run_query_calendar_duplicate_template($template_id, $duplicate_name) {
  global $cdg_sql;
  
  $query = "INSERT INTO EventTemplate (
    eventtemplate_name,
    eventtemplate_timeupdate,                         
    eventtemplate_timecreate,                         
    eventtemplate_usercreate,
    eventtemplate_owner,
    eventtemplate_title, 
    eventtemplate_date,     
    eventtemplate_description,
    eventtemplate_properties,
    eventtemplate_location, 
    eventtemplate_category1_id,
    eventtemplate_priority,
    eventtemplate_privacy, 
    eventtemplate_duration,
    eventtemplate_repeatkind,
    eventtemplate_repeatfrequence,
    eventtemplate_repeatdays,
    eventtemplate_allday,
    eventtemplate_tag_id, 
    eventtemplate_allow_documents,
    eventtemplate_alert,
    eventtemplate_endrepeat,
    eventtemplate_domain_id,
    eventtemplate_user_ids,
    eventtemplate_contact_ids,
    eventtemplate_resource_ids,
    eventtemplate_group_ids,
    eventtemplate_document_ids) 
    SELECT 
    '$duplicate_name',
    NOW(),
    NOW(),
    eventtemplate_usercreate,
    eventtemplate_owner,
    eventtemplate_title, 
    eventtemplate_date,     
    eventtemplate_description,
    eventtemplate_properties,
    eventtemplate_location, 
    eventtemplate_category1_id,
    eventtemplate_priority,
    eventtemplate_privacy, 
    eventtemplate_duration,
    eventtemplate_repeatkind,
    eventtemplate_repeatfrequence,
    eventtemplate_repeatdays,
    eventtemplate_allday,
    eventtemplate_tag_id, 
    eventtemplate_allow_documents,
    eventtemplate_alert,
    eventtemplate_endrepeat,
    eventtemplate_domain_id,
    eventtemplate_user_ids,
    eventtemplate_contact_ids,
    eventtemplate_resource_ids,
    eventtemplate_group_ids,
    eventtemplate_document_ids
    FROM EventTemplate 
    WHERE eventtemplate_id = $template_id";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_duplicate_template()');
  $obm_q = new DB_OBM;
  return $obm_q->query($query);
}

/**
 * Update the name of an event template
 * 
 * @param $template_id, $new_name
 * @access public
 * @return void
 */
function run_query_calendar_set_template_name($template_id, $new_name) {
  global $obm;
  
  $template_id = (int) $template_id;
  $query = "UPDATE EventTemplate SET eventtemplate_name = '$new_name' 
    WHERE eventtemplate_id = $template_id AND eventtemplate_usercreate = $obm[uid]";
  $obm_q = new DB_OBM;
  return $obm_q->query($query);
}

/**
 * Delete an event template
 * 
 * @param $template_id
 * @access public
 * @return void
 */
function run_query_calendar_delete_template($template_id) {
  global $cdg_sql;
  
  $template_id = (int) $template_id;
  $query = "DELETE FROM EventTemplate WHERE eventtemplate_id = $template_id";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete_template()');
  $obm_q = new DB_OBM;
  return $obm_q->query($query);
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

  if (!$id) {
    $err['msg'] = $l_err_reference;
    return false;
  }
  $e = get_calendar_event_info($id);
  if ($e['owner'] == $obm['uid']) {
    return true;
  }
  if ($right == 'write') {
    $access = OBM_Acl::canWrite($obm['uid'], 'calendar', $e['owner']);
  } else {
    if (in_array($obm['uid'], $e['attendees']['user'])) {
      return true;
    }
    $access = OBM_Acl::areSomeAllowed($obm['uid'], 'calendar', $e['attendees']['user'], $right);
    if (!$access && is_array($e['attendees']['resource'])) {
      $access = OBM_Acl::areSomeAllowed($obm['uid'], 'resource', $e['attendees']['resource'], $right);
    }
  }
  if (!$access) { 
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
function run_query_calendar_quick_delete($calendar) {
  global $cdg_sql, $c_use_connectors, $ccalendar_resource_admin;

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
    $query = "SELECT userentity_user_id FROM EventLink INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id WHERE eventlink_event_id = $event_id";
    $obm_q->query($query);
    $obm_qm = new DB_OBM;
    $users = array();
    while ($obm_q->next_record()) {
      // We note the event deletion
      $query = "INSERT INTO DeletedEvent (deletedevent_event_id, deletedevent_user_id, deletedevent_origin, deletedevent_type, deletedevent_timestamp)
        VALUES ($event_id, ".$obm_q->f('userentity_user_id').",'$GLOBALS[c_origin_web]', 'VEVENT', NOW())";
      display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete(2)');
      $retour = $obm_qm->query($query);
    }
  }

  // Send mail
  $event = OBM_EventFactory::getInstance()->getById($event_id);
  of_entity_delete('event', $event_id);
  $query = "DELETE FROM Event WHERE event_id = $event_id";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete(5)');
  if($calendar['send_mail']) {
    OBM_EventFactory::getInstance()->delete($event);
  }
  $obm_q->query($query);
}

///////////////////////////////////////////////////////////////////////////////
// Delete all events of a evenements, and the event
// Parameters: 
//   - $calendar : calendar infos hash
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_delete($calendar) {
  global $cdg_sql, $c_use_connectors, $ccalendar_resource_admin;

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
    $obm_qm = new DB_OBM;
    $query = "SELECT userentity_user_id FROM EventLink INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id WHERE eventlink_event_id = $event_id";
    $obm_q->query($query);
    $users = array();
    while ($obm_q->next_record()) {
      // We note the event deletion
      $query = "INSERT INTO DeletedEvent (deletedevent_event_id, deletedevent_user_id, deletedevent_origin, deletedevent_type, deletedevent_timestamp)
        VALUES ($event_id, ".$obm_q->f('userentity_user_id').",'$GLOBALS[c_origin_web]', 'VEVENT', NOW())";
      display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete(2)');
      $retour = $obm_qm->query($query);
    }
  }

  // Send mail
  $event = OBM_EventFactory::getInstance()->getById($event_id);
  of_entity_delete('event', $event_id);
  $query = "DELETE FROM Event WHERE event_id = $event_id";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete(5)');

  OBM_EventFactory::getInstance()->delete($event);
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

  $readable_calendars = OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'read');

  return OBM_Acl_Utils::expandEntitiesArray($readable_calendars);
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

  $readable_resources = OBM_Acl::getAllowedEntities($obm['uid'], 'resource', 'read');

  return OBM_Acl_Utils::expandEntitiesArray($readable_resources);
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
  $users = $entities_q['user'];
  if (is_array($users)) {
    foreach ($users as $id => $name) {
      if ($obm['uid'] == $id) {
        $entity_tab['user'][$id] = array(
          'name'  => $name,
          'class' => 'eventOwner',
          'type'  => 'user',
          'id'    => $id);
      } else {
        $entity_tab['user'][$id] = array(
          'name'  => $name,
          'class' => "eventOwner$i",
          'type'  => 'user',
          'id'    => $id);
        $i++;
        $i = $i%20;
      }
    }
  }
  $resources = $entities_q['resource'];
  if (is_array($resources)) {
    foreach ($resources as $id => $name) {
      $entity_tab['resource'][$id] = array(
        'name'  => $name,
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
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $event_date_l = sql_date_format($db_type,'event_date','event_date');
  $multidomain = sql_multidomain('event');
  $query = "SELECT *, $event_date_l FROM Event 
    $a_sql
    WHERE event_id = $id 
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_calendar_event_info()');
  $obm_q->query($query);
  $e = array('attendees' => array('user' => array()));
  $obm_q->next_record();
  $e['usercreate'] = $obm_q->f('event_usercreate');
  $e['owner'] = $obm_q->f('event_owner');
  $e['date'] = new  Of_Date($obm_q->f('event_date'), 'GMT');
  $e['duration'] = $obm_q->f('event_duration');
  $e['end'] = clone $e['date'];
  $e['end']->addSecond($obm_q->f('event_duration'));
  if ($attendees) {
    $obm_q= run_query_get_events_attendee(array($id));
    while($obm_q->next_record()) {
      $e['attendees'][$obm_q->f('eventlink_entity')][] = $obm_q->f('eventlink_entity_id');
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

  $query = "SELECT userentity_user_id as eventlink_entity_id
    FROM EventLink
    INNER JOIN UserEntity ON eventlink_entity_id = userentity_entity_id
    WHERE eventlink_event_id $id";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  while ($obm_q->next_record()) {
    $users_array[] = $obm_q->f('eventlink_entity_id');
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

  $calendar_id = sql_parse_id( $calendar_id, true);

  // XXXX Optimisation possible : 1 seule requete pour toutes les entites (sauf les documents...)
  $multidomain = sql_multidomain('userobm');
  $query = "SELECT DISTINCT
    userobm_id,
    userobm_lastname,
    userobm_firstname,
    'user' as eventlink_entity,
    userentity_user_id as eventlink_entity_id,
    eventlink_state as state
    FROM EventLink
    INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id
    INNER JOIN UserObm ON userentity_user_id = userobm_id
    WHERE 
    eventlink_event_id $calendar_id 
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_calendar_event_entity(user)');
  $u_q = new DB_OBM;
  $u_q->query($query);
  while ($u_q->next_record()) {
    $entity = $u_q->f('eventlink_entity');
    $entity_id = $u_q->f('eventlink_entity_id');
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
    'resource' as eventlink_entity,
    resourceentity_resource_id as eventlink_entity_id,
    eventlink_state as state
    FROM EventLink
    INNER JOIN ResourceEntity ON resourceentity_entity_id = eventlink_entity_id
    INNER JOIN Resource ON resourceentity_resource_id = resource_id
    WHERE 
    eventlink_event_id $calendar_id 
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_calendar_event_entity(resource)');
  $r_q = new DB_OBM;
  $r_q->query($query);
  while ($r_q->next_record()) {
    $entity = $r_q->f('eventlink_entity');
    $entity_id = $r_q->f('eventlink_entity_id');
    $state = $r_q->f('state');
    $label = $r_q->f('resource_name');
    $entities[$entity]['ids'][] = $entity_id;
    $entities[$entity]['entity'][$entity_id] = array('label'=> $label,
      'state' => $state);
  }

  $multidomain = sql_multidomain('contact');
  $query = "SELECT DISTINCT
    contact_id,
    contact_lastname,
    contact_firstname,
    'contact' as eventlink_entity,
    contactentity_contact_id as eventlink_entity_id,
    eventlink_state as state
    FROM EventLink
    INNER JOIN ContactEntity ON contactentity_entity_id = eventlink_entity_id
    INNER JOIN Contact ON contactentity_contact_id = contact_id
    WHERE 
    eventlink_event_id $calendar_id 
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_calendar_event_entity(contact)');
  $u_q = new DB_OBM;
  $u_q->query($query);
  while ($u_q->next_record()) {
    $entity = $u_q->f('eventlink_entity');
    $entity_id = $u_q->f('eventlink_entity_id');
    $state = $u_q->f('state');
    $label = $u_q->f('contact_lastname'). ' ' .$u_q->f('contact_firstname');
    $entities[$entity]['ids'][] = $entity_id;
    $entities[$entity]['entity'][$entity_id] = array('label'=> $label,
      'state' => $state);
  }
  
  $multidomain = sql_multidomain('document');
  $query = "SELECT 
    document_id,
    document_title,
    document_name,
    document_privacy,
    documentlink_usercreate
    FROM Document
    INNER JOIN DocumentLink ON document_id = documentlink_document_id
    INNER JOIN EventEntity ON documentlink_entity_id = evententity_entity_id
    WHERE 
    evententity_event_id $calendar_id 
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_calendar_event_entity(document)');
  $u_q = new DB_OBM;
  $u_q->query($query);
  while ($u_q->next_record()) {
    $entity_id = $u_q->f('document_id');
    $entities['document']['ids'][] = $entity_id;
    $entities['document']['entity'][$entity_id] = array(
      'title'=> $u_q->f('document_title'),
      'name' => $u_q->f('document_name'),
      'label' => $u_q->f('document_name'),
      'private' => ($u_q->f('document_privacy') == 1),
      'user' => $u_q->f('documentlink_usercreate')
    );
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
  $others_attendees = $calendar['others_attendees'];
  
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

  // Others attendees email
  if (is_array($calendar['others_attendees'])) {
    foreach($others_attendees as $email) {
      $email = trim($email);
      if ($email != '') {
        if(!preg_match($GLOBALS['php_regexp_email'], $email)) {
          $err['msg'] = "$GLOBALS[l_others_attendees] : $GLOBALS[l_invalid_email] ($email)";
          return false;
        }
      }
    }
  }

  // Repeatition check
  if ($kind != 'none') {
    // End repeat Date check
    if (!is_null($repeat_end)) {
      if ($repeat_end->error() != Of_Date::WARN_EMPTY_DATE && $repeat_end->error() == Of_Date::ERR_INVALID_DATE) {
        $err['msg'] = "$l_date_repeatend : $l_invalid_date"; 
        return false;
      }
    }

    if (!is_numeric($repeatfrequency) || $repeatfrequency < 1) {
      $err['msg'] = $l_err_repeatfrequency;
      return false;
    }
  }

  if ($end->compare($begin) < 0 || $event_duration < 0 || ( $all_day && $end->compare($begin) == 0)) {
    $err['msg'] = $l_err_begin_end;
    return false;
  } 

  if (!is_null($repeat_end)) {
    if ($repeat_end->error() != Of_Date::WARN_EMPTY_DATE && $end->compare($repeat_end) > 0 && $kind != 'none') {
      $err['msg'] =  $l_err_end_repeat;
      return false;
    }
  }

  // If repeat kind is weekly, repeat days must be set
  if ($kind == 'weekly' && $repeat_days == '0000000') {
    $err['msg'] = $l_err_days_repeat;
    return false;
  }

  $clone = clone $end;
  // Weekly repetition end >= date end + 1 week
  if ($kind == 'weekly' && !is_null($repeat_end) && $clone->addWeek(1)->compare($repeat_end) > 0) {
    $err['msg'] = $l_err_weekly_repeat;
    return false;
  }

  // Monthly repetition end >= date end + 1 month
  if ((($kind == 'monthlybydate') || ($kind == 'monthlybyday')) && !is_null($repeat_end) && $clone->addWeek(1)->compare($repeat_end) > 0) {
    $err['msg'] = $l_err_monthly_dateend_repeat;
    return false;
  }

  // Yearly repetition end >= date end + 1 year
  if ($kind == 'yearly' && !is_null($repeat_end) && $clone->addYear(1)->compare($repeat_end) > 0) {
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


/**
 * Search fiter
 * Construct WHERE clause 
 */
function run_query_calendar_build_search_filters($pattern, $type, $like) {
  $return = "";
  if ($type == 'basic') {
    $words = explode(' ', $pattern);
    foreach($words as $w) {
      $return .= "AND (
      event_title $like '%$w%' OR 
      event_location $like '%$w%' OR 
      event_description $like '%$w%' OR
      eventcategory1_label $like '%$w%'OR
      eventtag_label $like '%$w%' OR
      userobm_lastname $like '%$w%' OR
      userobm_firstname $like '%$w%' ";
    }
  } else {
    $event_pattern = array();
    foreach($pattern as $field => $pattern) {
      if ($pattern) {
        $words = explode(' ', $pattern); 
        $filter = array();

        if ($field == 'owner') {
          foreach($words as $w) {
            array_push($filter, "userobm_lastname $like '%$w%' OR userobm_firstname $like '%$w%'");
          }
        } elseif ($field == 'attendee') {
          // foreach($words as $w) {
          //   array_push($filter, "a.userobm_lastname $like '%$w%' OR a.userobm_firstname $like '%$w%'");
          // }
        } elseif($field =='event_tag_id') {
            if ($words[0] != $GLOBALS['c_none']) {
              array_push($filter, "event_tag_id='$words[0]'");
            }
        } else {
          foreach($words as $w) {
            array_push($filter, "$field $like '%$w%'");
          }
        }
        if (sizeof($filter) > 0)
          array_push($event_pattern, "(".implode(' OR ', $filter).")");
      }
    }
    if (sizeof($event_pattern) > 0) {
      $return = "AND (".implode(" AND ", $event_pattern)."";
    }
  }

  return $return;
}

function run_query_calendar_build_attendee_search_filters($pattern, $type, $like, $kind) {
  $return = "";
  return;
  if ($type=='basic') {
    if ($kind == 'user') {
      $words = explode(' ', $pattern);
      foreach($words as $w) {
        $return .= " OR a.userobm_lastname $like '%$w%' OR a.userobm_firstname $like '%$w%'";
      }
    } else {
      $words = explode(' ', $pattern);
      foreach($words as $w) {
        $return .= " OR resource_name $like '%$w%'";
      }
    }
  } else {
    $event_pattern = array();
    foreach($pattern as $field => $pattern) {
      if ($pattern) {
        $words = explode(' ', $pattern); 
        $filter = array();
        if ($field == 'attendee') {
          if ($kind == 'user') {
            foreach($words as $w) {
              array_push($filter, "a.userobm_lastname $like '%$w%' OR a.userobm_firstname $like '%$w%'");
            }
          } else {
            foreach($words as $w) {
              array_push($filter, "resource_name $like '%$w%'");
            }
          }
        }
      }
    }
  }

  return $return;
}


//FIXME HERE I AM
///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a week of users or/and groups
// Parameters:
//   - $start           : timestamp of start date
//   - $end             : timestamp of end date
//   - $calendar_entity : array of entities elements to display
//   - $status          : array of event status to get ('ACCEPTED', 'R', 'W')
//   - $category_filter : category
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_no_repeat_events($start,$end, $calendar_entity, $status, $category_filter='',$pattern='', $type='') {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $like = sql_casei_like($db_type);
  $timeupdate = sql_date_format($db_type, 'event_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'event_timecreate', 'timecreate');

  $multidomain = sql_multidomain('event');
  if ($category_filter != '') {
    $filter = " AND eventcategory1_id = '$category_filter' ";
  }

  if ($start) $event_date_start = "AND event_date >= #SUBSECONDS('$start',event_duration)";
  if ($end) $event_date_end = "AND event_date <= '$end'";

  if ($pattern) {
    $event_search_pattern = run_query_calendar_build_search_filters($pattern, $type, $like);
  }

  $first = true;
  if (is_array($status)
    && (count($status) > 0) ) {
      $where_status = "AND eventlink_state IN ('".implode("','",$status)."')"    ;
    }

  $select_q = "
    event_id,
    $timeupdate,
    $timecreate,
    event_ext_id,
    event_title,
    event_privacy,
    event_priority,
    event_description,
    event_properties,
    event_location,
    event_repeatfrequence,
    event_owner,
    userobm_lastname,
    userobm_firstname,
    event_timezone,
    event_opacity,
    eventlink_state,
    eventcategory1_label,
    event_date,
    event_duration,
    eventtag_color AS event_color,
    eventtag_label as tag,
    eventtag_id,
    event_allday,";

  $left_q = "
    LEFT JOIN EventCategory1 ON event_category1_id = eventcategory1_id
    INNER JOIN UserObm ON event_owner = userobm_id
    LEFT JOIN EventTag ON event_tag_id = eventtag_id 
    INNER JOIN EventLink ON event_id = eventlink_event_id";

  if (is_array($calendar_entity['user'])
    && (count($calendar_entity['user']) > 0) ) {
      if ($pattern) {
        $search_pattern = $event_search_pattern;
        $search_pattern .= run_query_calendar_build_attendee_search_filters($pattern, $type, $like,'user');
        if ($search_pattern) $search_pattern .= ")";
      }
      $select[] =  "SELECT
        $select_q
        userentity_user_id as eventlink_entity_id,
        'user' as eventlink_entity
        FROM Event
        $left_q 
        INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id
        WHERE event_type='VEVENT'
        $where_status
        $filter
        AND event_repeatkind = 'none'
        $event_date_start
        $event_date_end
        $search_pattern
        AND userentity_user_id IN (".implode(',', array_keys($calendar_entity['user'])).")
        $multidomain";       
    }               

  if (is_array($calendar_entity['resource'])
    && (count($calendar_entity['resource']) > 0) ) {
      if ($pattern) {
        $search_pattern = $event_search_pattern;
        $search_pattern .= run_query_calendar_build_attendee_search_filters($pattern, $type, $like,'resource');
        if ($search_pattern) $search_pattern .= ")";
      }
      $select[] =  "SELECT
        $select_q
        resourceentity_resource_id as eventlink_entity_id,
        'resource' as eventlink_entity
        FROM Event
        $left_q 
        INNER JOIN ResourceEntity ON resourceentity_entity_id = eventlink_entity_id
        WHERE event_type='VEVENT'
        $where_status
        $filter
        AND event_repeatkind = 'none'
        $event_date_start
        $event_date_end
        $search_pattern
        AND resourceentity_resource_id IN (".implode(',', array_keys($calendar_entity['resource'])).")
        $multidomain";   
    }               

  if (is_array($calendar_entity['resourcegroup'])
    && (count($calendar_entity['resourcegroup']) > 0) ) {
      if ($pattern) {
        $search_pattern = $event_search_pattern;
        $search_pattern .= run_query_calendar_build_attendee_search_filters($pattern, $type, $like,'resource');
        if ($search_pattern) $search_pattern .= ")";
      }
      $select[] = "SELECT
        $select_q
        resourceentity_resource_id as eventlink_entity_id,
        'resourcegroup' as eventlink_entity
        FROM Event
        $left_q 
        INNER JOIN ResourceEntity ON resourceentity_entity_id = eventlink_entity_id
        WHERE event_type='VEVENT'
        $where_status
        $filter
        AND event_repeatkind = 'none'
        $event_date_start
        $event_date_end
        $search_pattern
        AND resourceentity_resource_id IN (".implode(',', array_keys($calendar_entity['resourcegroup'])).")
        $multidomain";
    }
  $query = implode(' UNION ', $select)." ORDER BY event_date, eventlink_entity, eventlink_entity_id"; 
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
function run_query_calendar_repeat_events($start, $end, $calendar_entity, $status, $category_filter='', $pattern='', $type='') {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $like = sql_casei_like($db_type);

  $timeupdate = sql_date_format($db_type, 'event_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'event_timecreate', 'timecreate');

  $multidomain = sql_multidomain('event');
  if ($category_filter != '') {
    $filter = " AND eventcategory1_id = '$category_filter' ";
  }

  $first = true;
  if (is_array($status)
    && (count($status) > 0) ) {
      $where_status = "AND eventlink_state IN ('".implode("','",$status)."')";
    }

  if ($start) $event_date_start = "AND (event_endrepeat >= #SUBSECONDS('$start',event_duration)
        OR event_endrepeat IS NULL)";
  if ($end) $event_date_end = "AND event_date <= '$end'";

  if ($pattern) {
    $event_search_pattern = run_query_calendar_build_search_filters($pattern, $type, $like);
  }

  $select_q = "
    event_id,
    $timeupdate,
    $timecreate,
    event_ext_id,
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
    userobm_lastname,
    userobm_firstname,
    event_timezone,
    event_opacity,
    eventlink_state,
    event_repeatdays,
    eventtag_color AS event_color,
    eventtag_label as tag,
    eventtag_id,
    event_allday,";

  $left_q = "
    LEFT JOIN EventCategory1 ON event_category1_id = eventcategory1_id
    LEFT JOIN EventTag ON event_tag_id = eventtag_id 
    INNER JOIN UserObm ON event_owner = userobm_id
    INNER JOIN EventLink ON event_id = eventlink_event_id";

  if (is_array($calendar_entity['user'])
    && (count($calendar_entity['user']) > 0) ) {
      if ($pattern) {
        $search_pattern = $event_search_pattern;
        $search_pattern .= run_query_calendar_build_attendee_search_filters($pattern, $type, $like,'user');
        if ($search_pattern) $search_pattern .= ")";
      }
      $select[] =  "SELECT
        $select_q
        'user' as eventlink_entity,
        userentity_user_id as eventlink_entity_id
        FROM Event
        $left_q
        INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id
        WHERE event_type='VEVENT'
        AND event_repeatkind != 'none'
        $where_status
        $filter
        $event_date_start
        $event_date_end
        $search_pattern
        AND userentity_user_id IN (".implode(',', array_keys($calendar_entity['user'])).")
        $multidomain";
    }               

  if (is_array($calendar_entity['resource'])
    && (count($calendar_entity['resource']) > 0) ) {
      if ($pattern) {
        $search_pattern = $event_search_pattern;
        $search_pattern .= run_query_calendar_build_attendee_search_filters($pattern, $type, $like,'resource');
        if ($search_pattern) $search_pattern .= ")";
      }
      $select[] = "SELECT
        $select_q
        'resource' as eventlink_entity,
        resourceentity_resource_id as eventlink_entity_id
        FROM Event
        $left_q
        INNER JOIN ResourceEntity ON resourceentity_entity_id = eventlink_entity_id
        WHERE event_type='VEVENT'
        AND event_repeatkind != 'none'
        $where_status
        $filter
        $event_date_start
        $event_date_end
        $search_pattern
        AND resourceentity_resource_id IN (".implode(',', array_keys($calendar_entity['resource'])).")
        $multidomain";
    }               

  if (is_array($calendar_entity['resourcegroup'])
    && (count($calendar_entity['resourcegroup']) > 0) ) {
      if ($pattern) {
        $search_pattern = $event_search_pattern;
        $search_pattern .= run_query_calendar_build_attendee_search_filters($pattern, $type, $like,'resource');
        if ($search_pattern) $search_pattern .= ")";
      }
      $select[] = "SELECT
        $select_q
        'resourcegroup' as eventlink_entity,
        resourceentity_resource_id as eventlink_entity_id
        FROM Event
        $left_q
        INNER JOIN ResourceEntity ON resourceentity_entity_id = eventlink_entity_id
        WHERE event_type='VEVENT' 
        AND event_repeatkind != 'none'
        $where_status
        $filter
        $event_date_start
        $event_date_end
        $search_pattern
        AND resourceentity_resource_id IN (".implode(',', array_keys($calendar_entity['resourcegroup'])).")
        $multidomain";
    }

  $query = implode(' UNION ', $select)." ORDER BY event_date, eventlink_entity, eventlink_entity_id"; 
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
  $concat = array (
    array ('type' => 'field','value' => 'UserObm.userobm_lastname'),
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
  $multidomain = sql_multidomain('event');
  $query = "SELECT
    event_id,
    eventlink_state,
    userentity_user_id as eventlink_entity_id,
    'user' as eventlink_entity,
    #CONCAT(userobm_lastname, ' ', userobm_firstname) as eventlink_label
    FROM Event
    INNER JOIN EventLink ON eventlink_event_id = event_id
    INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id
    INNER JOIN UserObm ON userentity_user_id = userobm_id
    WHERE event_id IN ($event_list) $multidomain   
    UNION
    SELECT
    event_id,
    eventlink_state,
    resourceentity_resource_id as eventlink_entity_id,
    'resource' as eventlink_entity,
    resource_name as eventlink_label
    FROM Event
    INNER JOIN EventLink ON eventlink_event_id = event_id
    INNER JOIN ResourceEntity ON resourceentity_entity_id = eventlink_entity_id
    INNER JOIN Resource ON resourceentity_resource_id = resource_id
    WHERE event_id IN ($event_list) $multidomain
    UNION
    SELECT
    event_id,
    eventlink_state,
    contactentity_contact_id as eventlink_entity_id,
    'contact' as eventlink_entity,
    #CONCAT(contact_lastname, ' ', contact_firstname) as eventlink_label
    FROM Event
    INNER JOIN EventLink ON eventlink_event_id = event_id
    INNER JOIN ContactEntity ON contactentity_entity_id = eventlink_entity_id
    INNER JOIN Contact ON contactentity_contact_id = contact_id
    WHERE event_id IN ($event_list) $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_get_events_attendee()');
  $obm_q->xquery($query);
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
    $start = "AND eventexception_date >= '$start_time'";
  }
  if(!is_null($end_time)) {
    $end = "AND eventexception_date < '$end_time'";
  }
  $query = "SELECT
    event_duration,
    event_timezone,
    eventexception_parent_id,
    eventexception_date
    FROM EventException
    JOIN Event ON eventexception_parent_id = event_id
    WHERE eventexception_parent_id IN ($event_list) 
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
    eventexception_parent_id,
    eventexception_child_id,
    eventexception_date,
    event_date AS eventexception_event_date
    FROM EventException
    LEFT JOIN Event ON eventexception_child_id = event_id
    WHERE eventexception_parent_id $calendar_id"; 

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


///////////////////////////////////////////////////////////////////////////////
// Return a table of Events between two dates
// Parameters: 
//   - $start_time      : timestamp of start date
//   - $end_time        : timestamp of end date
//   - $calendar_entity :
///////////////////////////////////////////////////////////////////////////////
function calendar_events_model($start, $end, $calendar_entity, $status=array('ACCEPTED'), $category_filter='', $unique=false, $pattern='',$searchType='') {

  // Get all non repeating events and construct occurences & events model
  $nr_q = run_query_calendar_no_repeat_events($start,$end,$calendar_entity, $status, $category_filter,$pattern, $searchType);
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
    $entity = $nr_q->f('eventlink_entity');
    $entity_id = $nr_q->f('eventlink_entity_id');
    $entity_state = $nr_q->f('eventlink_state');
    $owner = $nr_q->f('event_owner');
    $owner_name = $nr_q->f('userobm_lastname').' '.$nr_q->f('userobm_firstname');
    $timezone = $nr_q->f('event_timezone');
    $opacity = $nr_q->f('event_opacity');
    $tag = $nr_q->f('eventtag_id');
    if (isset($of->events[$id])) {
      $event = $of->events[$id];
      if (!$unique) {
        $of->addOccurrence($event, $date, $entity, $entity_id, $entity_state);
      }
    } else {
      $event = new Event($id,$duration,$title,$location,$category1,$privacy,$description,$properties,$all_day,'none',$owner,$owner_name,$color,$opacity);
      $of->addOccurrence($event, $date, $entity, $entity_id, $entity_state);
    }
  }

  // Get all repeating events and construct occurences & events model
  $r_q = run_query_calendar_repeat_events($start,$end,$calendar_entity, $status, $category_filter,$pattern, $searchType);
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
    $entity = $r_q->f('eventlink_entity');
    $all_day = $r_q->f('event_allday');
    $color = $r_q->f('event_color');
    $repeatfrequence = $r_q->f('event_repeatfrequence');
    $repeatdays = $r_q->f('event_repeatdays');
    $entity_id = $r_q->f('eventlink_entity_id');
    $entity_state = $r_q->f('eventlink_state');
    $all_day = $r_q->f('event_allday');    
    $owner = $r_q->f('event_owner');
    $owner_name = $r_q->f('userobm_lastname').' '.$r_q->f('userobm_firstname');
    $timezone = $r_q->f('event_timezone');
    $opacity = $r_q->f('event_opacity');
    $tag = $r_q->f('eventtag_id');
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
      $event = new Event($id,$duration,$title,$location,$category1,$privacy,$description,$properties,$all_day,$repeatkind,$owner,$owner_name,$color,$opacity);
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
      $of->removeOccurrences($exception_q->f('eventexception_parent_id'), new Of_Date($exception_q->f('eventexception_date'), 'GMT'));
    }

    // Add attendees to events
    $attendee_q = run_query_get_events_attendee(array_keys($of->events));
    while($attendee_q->next_record()) {
      $id = $attendee_q->f('event_id');
      $entity = $attendee_q->f('eventlink_entity');
      $entity_id = $attendee_q->f('eventlink_entity_id');
      $entity_state = $attendee_q->f('eventlink_state');
      $entity_label = $attendee_q->f('eventlink_label');
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
  global $ccalendar_weekstart;

  $current = clone $date;
  $current->setWeekday($ccalendar_weekstart);
  if ($current->compare($start) < 0) {
    $diff = - ceil($current->diffTimestamp($start) / Of_Date::DAYDURATION); 
    if ($diff > 0) {
      $diff = ($diff % (($repeatfrequence) * 7));
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
    $current->setWeekday($ccalendar_weekstart);
    $current->addWeek($repeatfrequence);
  } 
}


///////////////////////////////////////////////////////////////////////////////
// Perform the monthly by day repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function calendar_monthlybyday_repeatition($date,$start,$end,$repeatfrequence,$event,$entity_id,$entity, $entity_state) {
  global $ccalendar_weekstart;

  $current = clone $date;
  $current->setDay(1);
  if ($current->compare($start) < 0) {
    $diff = ($start->getYear() - $current->getYear()) * 12 + ($start->getMonth() - $current->getMonth());
    if ($diff > 0) {
      $diff = $diff  % $repeatfrequence;
    }
    $current->setYear($start)->setMonth($start)->subMonth($diff);
  }
  $position = ceil($date->getDay() / 7);
  $pattern = ($position == 5)? "-1 ".$date->get(Of_Date::WEEKDAY): "+$position ".$date->get(Of_Date::WEEKDAY);
  while ($current->compareDateIso($end) < 0) {
    $current->setDay(1);
    if($position == 5) $current->addMonth(1); else {
      $current->subDay(1);
      // Needed because of 'next week' behavior modification (bug ?) in, at
      // least, PHP 5.2.[8|9|10]
      if ($current->get(Of_Date::WEEKDAY) == $date->get(Of_Date::WEEKDAY)) {
        $current->addDay(1);
      }
    }
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

  $current = clone $date;
  if ($current->compare($start) < 0) {
    $diff = ($start->getYear() - $current->getYear()) * 12 + ($start->getMonth() - $current->getMonth());
    if ($diff > 0) {
      $diff = $diff  % $repeatfrequence;
    }
    $current->setYear($start)->setMonth($start)->subMonth($diff);    
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
// Insert default views
// Parameters:
//      - $id : current view
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_insert_default_view($id){
  global $cdg_sql, $obm;

  $id_view = $id;
  $user_id = sql_parse_id($obm['uid']);
  $obm_q = new DB_OBM;

  $query = "INSERT INTO UserObmPref (
    userobmpref_user_id, 
    userobmpref_option, 
    userobmpref_value 
  ) VALUES (
    $user_id,
    'set_cal_default_view',
    $id_view
  )";

  $obm_q->query($query);
}

///////////////////////////////////////////////////////////////////////////////
// Update default views
// Parameters:
//      - $id : current view
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_update_default_view($id){
  global $cdg_sql, $obm;

  $id_view = $id;
  $user_id = sql_parse_id($obm['uid']);
  $obm_q = new DB_OBM;

  $query = "UPDATE UserObmPref 
    SET userobmpref_value=$id_view
    WHERE userobmpref_user_id=$user_id
    AND userobmpref_option='set_cal_default_view' 
    ";

  $obm_q->query($query);
}

///////////////////////////////////////////////////////////////////////////////
// Delete default views
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_delete_default_view(){
  global $cdg_sql, $obm;

  $user_id = sql_parse_id($obm['uid']);
  $obm_q = new DB_OBM;

  $query = "DELETE FROM UserObmPref
    WHERE userobmpref_user_id=$user_id
    AND userobmpref_option='set_cal_default_view' 
    ";

  $obm_q->query($query);
}

///////////////////////////////////////////////////////////////////////////////
// Return Tab Bookmark parameters
// Parameters:
//      - $id : OBM bookmark_id
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_get_BookmarkProperty_view($id){
  global $cdg_sql, $obm;

  $id_view = $id;
  $default_view_property=array();
  $obm_q = new DB_OBM;

  $query = "SELECT * FROM ObmBookmarkProperty
    WHERE obmbookmarkproperty_bookmark_id='$id_view'";

  display_debug_msg($query, $cdg_sql, "run_query_calendar_get_BookmarkProperty_view())");
  $obm_q->query($query);

  while($obm_q->next_record()) {
    $property = $obm_q->f("obmbookmarkproperty_property");
    $value = $obm_q->f("obmbookmarkproperty_value");
    switch($property) {
    case "users":
    case "resources":
      $return[$property] = explode(',', $value);
      break;
    default:
      $return[$property]=$value;
      break; 
    }
  }

  $return['view_id']=$id;

  return $return;
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
  $cal_range = 'week';//$_SESSION['cal_range'];

  // View
  $cal_view = 'agenda';//$_SESSION['cal_view'];

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

  $obmbookmark_id = $obm_q->lastid();
  of_entity_insert('obmbookmark', $obmbookmark_id);
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
  global $cdg_sql, $obm;

  $user_id = sql_parse_id($obm['uid']);
  $obm_q = new DB_OBM;

  of_entity_delete('obmbookmark', $calendar['view_id']);
  $id = sql_parse_id( $calendar['view_id'], true);

  // Delete view
  $query = "DELETE FROM ObmBookmark WHERE obmbookmark_id $id"; 
  $obm_q->query($query);

  // Delete default view (if the default view is the deleted one)
  $query = "DELETE FROM UserObmPref
    WHERE userobmpref_user_id=$user_id
    AND userobmpref_option='set_cal_default_view'
    AND userobmpref_value $id
    ";
  $obm_q->query($query);

  $message = "\"obmbookmark_id\": \"$calendar[view_id]\"";

  return $message;
}


///////////////////////////////////////////////////////////////////////////////
// Insert others attendees as private contacts
// Parameters:
//   - $attendees : hashed attendees email
///////////////////////////////////////////////////////////////////////////////
function run_query_insert_others_attendees($calendar) {
  global $obm, $cdg_sql;

  $uid = sql_parse_id($obm['uid']);
  $domain_id = sql_parse_id($obm['domain_id']);

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $attendees = $calendar['others_attendees'];
  $ret = array();

  foreach($attendees as $email) {
    //// Check if exists
    // XXXXXX Migrate the access right to the Addressbook
    $query = "SELECT contact_id FROM Contact
      INNER JOIN ContactEntity ON contactentity_contact_id = contact_id
        INNER JOIN Email ON email_entity_id = contactentity_entity_id 
      WHERE /*(contact_privacy = '0' OR contact_usercreate='$uid') AND*/ contact_archive='0' and email_address = '$email'";
    display_debug_msg($query, $cdg_sql,"'run_query_insert_others_attendees($email)");
    $obm_q->query($query);    

    if ($obm_q->next_record()) {
      if(!in_array($obm_q->f('contact_id'), $ret)) {
        array_push($ret, $obm_q->f('contact_id'));
      }
    } else {
      list( $first_name, $last_name ) = guess_name_from_email($email);
      // Insert contact
      $query = "INSERT INTO Contact (contact_timeupdate,
        contact_timecreate,
        contact_userupdate,
        contact_usercreate,
        contact_domain_id,
        contact_lastname,
        contact_firstname,
        contact_marketingmanager_id,
        contact_archive,
        contact_origin) VALUES (
          NOW(),
        NOW(),
        $uid,
        $uid,
        $domain_id,
        '$last_name',
        '$first_name',
        '$uid',
        '0',
        '$GLOBALS[c_origin_web]')";

      display_debug_msg($query, $cdg_sql,"'run_query_insert_others_attendees($email)");
      $obm_q->query($query);

      // Get contact_id & entity_id
      $id = $obm_q->lastid();
      $entityid = of_entity_insert('contact',$id);

      $query = "INSERT INTO Email (email_entity_id, email_address, email_label) 
        VALUES ($entityid, '$email', 'INTERNET;X-OBM-Ref1')";
      display_debug_msg($query, $cdg_sql,"'run_query_insert_others_attendees($email)");
      $obm_q->query($query);

      of_userdata_query_update('contact', $id, $calendar);

      array_push($ret, $id);
    }
  }
  return $ret;

}

///////////////////////////////////////////////////////////////////////////////
// Create or update tag
// Parameters:
//   - $calendar : event params
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_create_or_update_tag(&$calendar) {
  global $obm;
  
  $tagcolor = $calendar['tag_color'] ;
  if ($tagcolor == '') {
    $tagcolor = "#eff0f2" ; // default background ;
  }
  $taglabel = $calendar['tag_label'] ;
  $obm_q = new DB_OBM;
  
  if ($taglabel != '') {
    $query = "SELECT eventtag_id FROM EventTag 
              WHERE eventtag_label = '$taglabel' and eventtag_user_id = ".$obm['uid']." ;" ;
    $obm_q->query($query);
    if ($obm_q->num_rows() > 0) {
      $query = "UPDATE EventTag SET 
                eventtag_color = '$tagcolor '
                WHERE eventtag_label = '$taglabel'
                AND eventtag_user_id = ".$obm['uid']." ;" ;
      $obm_q->query($query) ; 
    } else {
      $query = "INSERT INTO EventTag 
                (eventtag_label, eventtag_color, eventtag_user_id) 
                VALUES ('$taglabel', '$tagcolor', ".$obm['uid'].") ;" ;
      $obm_q->query($query) ;
      // get back the last inserted eventtag_id
      $query = "SELECT eventtag_id FROM EventTag 
                WHERE eventtag_label = '$taglabel' and eventtag_user_id = ".$obm['uid']." ;" ;
      $obm_q->query($query) ;
      $obm_q->next_record() ;
      $tagid = $obm_q->f('eventtag_id') ;
      
      // tweak
      $calendar['tag_id'] = $tagid ; 
    }
  }
  $event_tag_id = $calendar['tag_id'] ;
  if ($event_tag_id == $GLOBALS['c_none']) $event_tag_id = 'NULL' ;
  
  return $event_tag_id;
}

///////////////////////////////////////////////////////////////////////////////
// Get all user defined tags for event
// Parameters:
//   - $obmUserId : int obmuser identifier
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_get_alltags($obmUserId,$tag_id=null) {
  global $cdg_sql;

  $obm_q = new DB_OBM ;
  
  if ($tag_id) $where = "AND eventtag_id=$tag_id";
  $query = "SELECT * FROM EventTag WHERE eventtag_user_id = $obmUserId $where 
    ORDER BY eventtag_label";
  $obm_q->query($query) ;
  display_debug_msg($query, $cdg_sql,"run_query_calendar_get_alltags()");
  
  return $obm_q ;
}

///////////////////////////////////////////////////////////////////////////////
// Update tag (label and color)
// Parameters:
//   - $uid : int obmuser identifier
//   - $params : hashed tag params
///////////////////////////////////////////////////////////////////////////////
function run_query_tag_update($uid, $params) {
  $obm_q = new DB_OBM ;
  
  $tagid = intval($params['tag_id']) ;
  $taglabel = $params['tag_label'] ;
  $tagcolor = $params['tag_color'] ;
  
  $query = "UPDATE EventTag 
      SET eventtag_label = '$taglabel', eventtag_color = '$tagcolor' 
      WHERE eventtag_id = $tagid AND eventtag_user_id = $uid ;" ;

  return $obm_q->query($query) ;
}

///////////////////////////////////////////////////////////////////////////////
// Insert tag (label and color)
// Parameters:
//   - $params : hashed tag params
//   - $uid : int obmuser id
///////////////////////////////////////////////////////////////////////////////
function run_query_tag_insert($uid, $params) {
  $obm_q = new DB_OBM ;
  
  if (!isset($uid)) {
    return ;
  }
  
  $taglabel = $params['tag_label'] ;
  $tagcolor = $params['tag_color'] ;
  
  // verifier si le tag n'existe pas deja
  $query = "SELECT eventtag_id 
      FROM EventTag 
      WHERE eventtag_label = '$taglabel' 
      AND eventtag_user_id = $uid" ;
  $obm_q->query($query) ;
  
  if ($obm_q->num_rows() > 0) {
    // update the tag
    $obm_q->next_record() ;
    $tagid = $obm_q->f('eventtag_id') ;
    $query = "UPDATE EventTag 
      SET eventtag_label = '$taglabel', eventtag_color = '$tagcolor' 
      WHERE eventtag_id = $tagid AND eventtag_user_id = $uid ;" ;
  }
  else {
    // create the tag
    $query = "INSERT INTO EventTag 
              (eventtag_label, eventtag_color, eventtag_user_id)
              VALUES ('$taglabel', '$tagcolor', $uid) ;" ;
  }
  
  return $obm_q->query($query) ;
}

///////////////////////////////////////////////////////////////////////////////
// Delete tag (label and color)
// Parameters:
//   - $uid : int omuser id
//   - - $params : hashed tag params
///////////////////////////////////////////////////////////////////////////////
function run_query_tag_delete($uid, $params) {
  $obm_q = new DB_OBM ;
  
  $tagid = intval($params['tag_id']) ;
  
  $query = "DELETE FROM EventTag 
            WHERE eventtag_id = $tagid AND eventtag_user_id = $uid" ;
  
  return $obm_q->query($query) ;
}

///////////////////////////////////////////////////////////////////////////////
// Search tag (label and color)
// Parameters:
//   - $uid : int omuser id
//   - $params : hashed tag params
///////////////////////////////////////////////////////////////////////////////
function run_query_tag_search($uid, $params) {
  $obm_q = new DB_OBM ;
  
  $search_pattern = $params['label'] ;
  $query = "SELECT eventtag_id, eventtag_label, eventtag_color 
            FROM EventTag 
            WHERE eventtag_label #LIKE '%$search_pattern%' 
            AND eventtag_user_id = $uid ;" ;
  $obm_q->xquery($query) ; // obm sql
  
  return $obm_q ;
}

function check_tag_form($params) {
  global $err, $l_tag_fill_label, $l_tag_fill_color ;
  if (trim($params['tag_label']) === '') {
    $err['msg'] = $l_tag_fill_label ;
    return false ;
  }
  if (trim($params['tag_color']) === '') {
    $err['msg'] = $l_tag_fill_color ;
    return false ;
  }
  return true ;
}

///////////////////////////////////////////////////////////////////////////////
// Insert other files as private files
// Parameters:
//   - $params : POSTed params
///////////////////////////////////////////////////////////////////////////////
function run_query_insert_other_files($params) {
  global $obm, $default_path;
  
  require(dirname(__FILE__).'/../document/document_query.inc');
  
  $doc_ids = array();
  foreach ($params['other_files'] as $document) {
    $document['title'] = $document['name'];
    $document['author'] = $obm['login'];
    $document['mime'] = get_document_auto_mime_type($document);
    $document['path'] = $default_path.'/';
    $document['kind'] = 1;
    $document['privacy'] = 1;
    
    $doc_ids[] = run_query_document_insert($document);
  }
  
  return $doc_ids;
}

function guess_name_from_email($email) {
  $first_name = '';
  $last_name  = '';

  if(preg_match('/(.+?)\.(.+?)@/', $email, $match)) {
    $first_name = ucfirst(strtolower($match[1]));
    $last_name  = strtoupper($match[2]);
  } elseif(preg_match('/([^.]+)@/', $email, $match)) {
    $last_name = ucfirst(strtolower($match[1]));
  }

  return array( $first_name, $last_name );
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
  var $opacity;
  var $all_day;
  var $owner;
  var $owner_name;
  var $color;
  var $tag ;
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
   * @param mixed $owner_name
   * @param mixed $color
   * @access public
   * @return void
   */
  function Event($id,$duration,$title,$location,$category1,$privacy,$description,$properties,$all_day,$repeat,$owner,$owner_name,$color,$opacity) {
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
    $this->owner_name = $owner_name;
    $this->color = $color;
    $this->opacity = $opacity;
    $this->tag = $tag;
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
    return (count($this->attendee['user']) + count($this->attendee['contact']) > 1);
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

  function isAllDay() {
    return ($this->all_day == 1);
  }
  
  /**
   *isOpaque
   *
   * @access public
   * @return bool
   */
  public function isOpaque() {
    $retour = false;
    if($this->opacity == 'OPAQUE'){
      $retour = true;
    }
    return $retour;
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
    $this->attendee[$entity][$entity_id] = $entity_label.'('.$GLOBALS["l_".strtolower($entity_state)].')';
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
    if($event->all_day == 1) {
      $this->end = clone $date;
      $this->date = $date->setHour(0)->setMinute(0)->setSecond(0);
      $this->end->addSecond($event->duration - 1);
      $this->end->addDay(1)->setHour(0)->setMinute(0)->setSecond(0);
    } else {
      $this->date = $date;
      $this->end = clone $this->date;
      $this->end->addSecond($event->duration);
    }

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
      $this->occurrences[$event->id][] = new Occurrence($event, $date, $entity, $id, $state);
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
    usort($return, 'occurrenceDateSort');
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
              $eventBegin = clone $occurrence->date;
              $eventEnd = clone $occurrence->end;          
              $eventAllDay = $occurrence->event->all_day;
              if($eventAllDay && $eventFilter != 1 && $begin->compare($eventEnd) < 0 && $end->compare($eventBegin) > 0) {
                return true;
              } 
              if(!$eventAllDay && $eventEnd->compare($begin) > 0 && $eventBegin->compare($end) < 0) {
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
              $eventBegin = clone $occurrence->date;
              $eventEnd = clone $occurrence->end;
              $eventAllDay = $occurrence->event->all_day;
              if($eventAllDay && $eventFilter != 1 && $begin->compare($eventEnd) < 0 && $end->compare($eventBegin) > 0) {
                array_unshift($occurrences,$occurrence);
              } 
              if(!$eventAllDay && $eventFilter != 2 && $begin->compare($eventEnd) < 0 && $end->compare($eventBegin) > 0) {
                array_push($occurrences,$occurrence);
              }
            }
      }
    }
    if (count($occurrences) > 0) {
      // SORT repeat And non repeat event
      usort($occurrences, 'occurrenceDateSort');
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
 * Sort function to allow occurences sorting by date
 * 
 * @param mixed $occA first comparison element
 * @param mixed $occB second comparison element
 * @access public
 * @return void
 */
function occurrenceDateSort($occA, $occB) {
  if ($occA->date < $occB->date) {
    return -1;
  } else {
    return 1;
  }
}


/**
 * Reset the user calendar. 
 * 
 * @access public
 * @return void
 */
function run_query_calendar_reset($id,$params) {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;
  $calendar['mail'] = false;
  $writable = array_keys(OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'write'));

  $meeting = $params['delete_meeting'];
  $export = $params['export'];
  $date = $params['event_before_date'];

  if(is_null($date)){
    $date = new Of_date();
  }

  if($export != 'date'){
    $date->addYear(100);
  }

  # Delete all events where the user is the owner and an attendee and all events where the owner
  # is writable and the user is the only attendee
  $query = "SELECT event_id FROM Event
    INNER JOIN EventLink ON event_id = eventlink_event_id
    INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id
    WHERE (event_type='VEVENT' AND event_owner = $id AND userentity_user_id = $id AND event_date < '$date')
    GROUP BY event_id HAVING COUNT(*) = 1
    UNION
    SELECT event_id FROM Event
    INNER JOIN EventLink ON event_id = eventlink_event_id
    INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id
    WHERE event_type='VEVENT' AND event_owner IN  (".implode(',',$writable).") AND event_date < '$date'
    GROUP BY event_id HAVING COUNT(*) = 1";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_reset()');
  $obm_q->query($query);

  while($obm_q->next_record()) {
    $calendar['calendar_id'] = $obm_q->f('event_id');
    $event[] = $calendar['calendar_id'];
    run_query_calendar_delete($calendar);
  }

  if(!is_null($meeting)){
    # Refuse all other events
    $query = "SELECT eventlink_event_id FROM EventLink INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id INNER JOIN Event ON event_id = eventlink_event_id WHERE userentity_user_id = $id 
      AND eventlink_state != 'DECLINED' AND event_date < '$date'";
    $obm_q->query($query);
    while($obm_q->next_record()) {
      $event[] = $obm_q->f('eventlink_event_id');
      run_query_calendar_update_occurrence_state($obm_q->f('eventlink_event_id'), 'user', $id, 'DECLINED');
    }
  }

  # Delete all alerts
  if(!is_null($event)) {
    $query = "DELETE FROM EventAlert WHERE eventalert_user_id = $id AND eventalert_event_id IN (".implode(',',$event).")"; 
    display_debug_msg($query, $cdg_sql, 'run_query_calendar_reset()');
    $obm_q->query($query);
  }
}

/**
 * Returns the OBM product id or create a new one
 * @access public
 * @return unique id (21 hex chars sized)
 */
function get_or_create_prod_id(){
  global $cdg_sql;

  $prod_id = get_obm_info('prod_id');

  if(!is_null($prod_id)) {
    return $prod_id;
  }

  $prod_id = uniqid(dechex(rand()));
  $obm_q = new DB_OBM;
  $query = "INSERT INTO ObmInfo
            (obminfo_name, obminfo_value) VALUES
            ('prod_id', '$prod_id')";

  display_debug_msg($query, $cdg_sql, 'get_or_create_prod_id()');
  $obm_q->query($query);

  return $prod_id;
}

/**
 * Generates a world-wide unique event id. Format is :
 * OBM-<obm version>-<obm prod id>-<hash>@domain
 * @access public
 * @return unique id
 */
function genUniqueExtEventId() {
  global $obm_version;

  $domain = $GLOBALS['obm']['domain_name'];
  $version = 'OBM-'.substr($obm_version, 0, 15).'-'.get_or_create_prod_id();

  /* 255 (max event_ext_id field) - 128 (max domain len) - 21 (prod id)
     - 15 (max version length guess) - 7 (strlen('OBM---@'))
     = 84
     = (the Answer to the Ultimate Question of Life, the Universe, and Everything) * 2
  */
  $max_len = 84;
  $event_ext_id = '';
  while(strlen($event_ext_id) < $max_len) {
    $event_ext_id .= dechex(rand()%16);
  }
  return $version.'-'.$event_ext_id.'@'.$domain;
}

/**
 * Get events for an entity
 */
function get_json_entity_events($calendar, $entity_store) {
  global $obm, $display;

  $json = array();
  if (is_array($calendar['resource_id']) || is_array($calendar['user_id'])) {
    $date = $calendar['date'];
    $date->setWeekday($GLOBALS['ccalendar_weekstart'])->setHour(0)->setMinute(0)->setSecond(0);
    $end = clone $date;
    $end->addDay(7);  

    $canRead = 0;
    $status = array('ACCEPTED');
    $of = &OccurrenceFactory::getInstance();
    calendar_events_model($date,$end,$entity_store, $status);
    $json_attendees = array();
    $user = array();
    if (is_array($calendar['user_id'])) {
      foreach($calendar['user_id'] as $u_id) {
        if(OBM_Acl::canRead($obm['uid'], 'calendar', $u_id)) {
          array_push($user, $u_id);
          $json_attendees['user'][$u_id] = array();
        } else {
          $entity_info = get_entity_info($u_id, 'user');
          array_push($json, "{entity:'user', entity_id:'$u_id', entity_label:'".phpStringToJsString($entity_info['label'])."', data:{canRead:0}}");
        }
      }
    }

    $resource = array();
    if (is_array($calendar['resource_id'])) {
      foreach($calendar['resource_id'] as $r_id) {
        if(OBM_Acl::canRead($obm['uid'], 'resource', $r_id)) {
          array_push($resource, $r_id);
          $json_attendees['resource'][$r_id] = array();
        } else {
          $entity_info = get_entity_info($r_id, 'resource');
          array_push($json, "{entity:'resource', entity_id:'$r_id', entity_label:'".phpStringToJsString($entity_info['label'])."', data:{canRead:0}}");
        }
      }
    }

    $ocs = $of->getPeriodOccurrences($date, $end, array('user' => $user, 'resource' => $resource));
    $json_evts = build_json_event($ocs, $json_attendees);
    foreach($json_evts as $entity => $entity_ids) {
      foreach($entity_ids as $entity_id => $events) {
        $events = implode(",", $events);
        $entity_info = get_entity_info($entity_id, $entity);
        array_push($json, "{entity:'$entity', entity_id:'$entity_id',entity_label:'".phpStringToJsString($entity_info['label'])."', data:{canRead:1,events:[$events]}}");
      }
    }
  }

  if (is_array($calendar['contact_id'])) {
    foreach($calendar['contact_id'] as $c_id) {
      $entity_info = get_entity_info($c_id, 'contact');
      array_push($json, "{entity:'contact', entity_id:'$c_id',entity_label:'".phpStringToJsString($entity_info['label'])."', data:{canRead:0}}");
    }
  }

  $display['json'] = "listEvents:[".implode(',', $json)."]";
}

function build_json_event($ocs, $json_evts) {
  global $obm;

  if (is_array($ocs)) {
    foreach($ocs as $oc) {
      if($oc->event->isOpaque()) {
        $date = $oc->date;
        $begin = clone $date;
        $end = clone $date;
        $end->addTimestamp($oc->event->duration); 
        $evt = "{event:{";
        $title = $oc->event->title;
        $entity = $oc->entity;
        $entity_id = $oc->id;
        if (!is_array($json_evts[$entity][$entity_id])) $json_evts[$entity][$entity_id] = array();
        if($oc->event->isPrivate() && !(array_key_exists($obm['uid'],$oc->event->attendee['user']))) {
          $title = $GLOBALS['l_private'];
        }
        $evt .= "begin:'".$begin->getTimestamp()."',";
        $evt .= "end:'".$end->getTimestamp()."',";
        $evt .= "title:'".phpStringToJsString($title)."',";
        $evt .= "meeting:".($oc->event->isMeeting()?'true':'false').",";
        $evt .= "private:".($oc->event->isPrivate()?'true':'false').",";
        $evt .= "allday:".($oc->event->isAllDay()?'true':'false').",";
        $evt .= "periodic:".($oc->event->isPeriodic()?'true':'false')."";
        $evt .= "}}";
        array_push($json_evts[$entity][$entity_id], $evt);
      }
    }
  }
  return $json_evts;
}

/**
 * Check if there is a conflict about a resource, and return false
 * if we can't force its insertion.
 */
function can_force_resource_conflict($conflicts = array()) {
  global $ccalendar_resource_overbooking;

  if($ccalendar_resource_overbooking) {
    return true;
  } else {
    foreach($conflicts as $conflict) {
      if($conflict->entity == 'resource') {
        return false;
      }
    }
  }
  return true;
}



/**
 * Describe a calendar View
 * 
 */
Class CalendarView {
  protected $id;
  protected $entity_id;
  protected $label;
  protected $modified;

  protected $cal_range;
  protected $cal_view;
  protected $category;
  protected $group;
  protected $users;
  protected $resources;
  protected $classes;

  /**
   * CalendarView constructor
   * 
   * @param view data $data
   * @access public
   */
  public function __construct($data = array()) {
    $this->modified = true;
    $this->set_cal_range($data['cal_range']);
    $this->set_cal_view($data['cal_view']);
    $this->set_category($data['category']);
    $this->remove_all();
    $this->set_group($data['group']);
    $this->set_users($data['users']);
    if (is_array($data['user_classes'])) {
      foreach($data['user_classes'] as $user => $class) {
        $this->set_user_class($user,$class);
      }
    }
    $this->set_resources($data['resources']);
    if (is_array($data['resource_classes'])) {
      foreach($data['resource_classes'] as $resource => $class) {
        $this->set_resource_class($resource,$class);
      }
    }
  }

  public function __clone() {
    $this->id = null;
  }

  /**
   * retrieve a CalendarView from database
   * 
   * @param bookmark id
   * @access public
   * @return CalendarView
   */
  public static function get_from_id($bookmark_id) {
    global $cdg_sql;

    $obm_q = new DB_OBM;
    $id = sql_parse_id($bookmark_id);

    $query = "SELECT * FROM ObmBookmarkProperty WHERE obmbookmarkproperty_bookmark_id='$id'";
    display_debug_msg($query, $cdg_sql, "CalendarView::get_from_id()");
    $obm_q->query($query);

    $data = array();
    while($obm_q->next_record()) {
      $property = $obm_q->f("obmbookmarkproperty_property");
      $value = $obm_q->f("obmbookmarkproperty_value");

      if (in_array($property,array('users','resources'))) {
        $data[$property] = explode(',',$value);
      } elseif (in_array($property,array('user_classes','resource_classes'))) {
        $data[$property] = unserialize($value);
      } else {
        $data[$property] = $value;
      }
    }
    $view = new CalendarView($data);
    $view->id = $bookmark_id;
    $view->modified = false;

    return $view;
  }


  /**
   * standard id getter,
   * the id is null if the CalendarView has no record in the database
   * 
   * @access public
   * @return integer
   */
  public function get_id() {
    return $this->id;
  }

  /**
   * standard entity_id getter,
   * the entity_id is null if the CalendarView has no record in the database
   * 
   * @access public
   * @return integer
   */
  public function get_entity_id() {
    if (!isset($this->entity_id) && ($this->id))
      $this->entity_id = of_entity_get('obmbookmark',$this->id);
    return $this->entity_id;
  }

  /**
   * standard label getter,
   * 
   * @access public
   * @return string
   */
  public function get_label() {
    global $cdg_sql;

    if (!isset($this->label) && ($this->id)) {
      $obm_q = new DB_OBM;
      $id = sql_parse_id($this->id);
      $query = "SELECT obmbookmark_label FROM ObmBookmark WHERE obmbookmark_id='$id' AND obmbookmark_entity='calendar'";
      display_debug_msg($query, $cdg_sql, "CalendarView::get_label()");
      $obm_q->query($query);
      if ($obm_q->next_record())
        $this->label = $obm_q->f('obmbookmark_label');
    }
    return $this->label;
  }

  /**
   * true if unsaved changes has been done to the view
   * 
   * @access public
   * @return boolean
   */
  public function is_modified() {
    return $this->modified;
  }

  /**
   * true if current view is the default one
   * default view id is read from $_SESSION['set_cal_default_view']
   * 
   * @access public
   * @return boolean
   */
  public function is_default() {
    return (($this->id) && ($this->id==$_SESSION['set_cal_default_view']));
  }

  /**
   * get the selected cal_range
   * 
   * @access public
   * @return string
   */
  public function get_cal_range() {
    return $this->cal_range;
  }

  /**
   * get the selected cal_view
   * 
   * @access public
   * @return string
   */
  public function get_cal_view() {
    return $this->cal_view;
  }

  /**
   * get the selected category
   * 
   * @access public
   * @return string
   */
  public function get_category() {
    return $this->category;
  }

  /**
   * get the selected group id
   * 
   * @access public
   * @return integer
   */
  public function get_group() {
    return $this->group;
  }

  public function get_users() {
    return $this->users;
  }

  public function get_resources() {
    return $this->resources;
  }

  public function get_entities($nb_max=0, $entity_readable=array()) {
    $entities = array();
    if ($nb_max <= 0) $nb_max = count($this->users) + count($this->resources);

    if (count($this->users)>0) {
      $user_readable = (is_array($entity_readable['user']['ids']) ? $entity_readable['user']['ids'] : $this->users);
      $entities['user'] = $this->get_users_data($nb_max, $user_readable);
      $nb_max = $nb_max - count($entities['user']);
    }

    if (count($this->resources)>0) {
      $resource_readable = (is_array($entity_readable['resource']['ids']) ? $entity_readable['resource']['ids'] : $this->resources);
      $entities['resource'] = $this->get_resources_data($nb_max, $entity_readable['resource']['ids']);
      $nb_max = $nb_max - count($entities['resource']);
    }

/*    if ($this->group!=$GLOBALS['c_all']) {
      $entities['group_view'] = $this->group;
}*/

    return $entities;
  }

  public function set_label($label) {
    $this->label = $label;
    $this->modified = true;
  }

  public function set_cal_range($cal_range = null) {
    $this->cal_range = (empty($cal_range) ? 'week' : $cal_range);
    $this->modified = true;
  }

  public function set_cal_view($cal_view = null) {
    $this->cal_view = (empty($cal_view) ? 'agenda' : $cal_view);
    $this->modified = true;
  }

  public function set_category($category = null) {
    $this->category = (empty($category) ? $GLOBALS['c_all'] : $category);
    $this->modified = true;
  }

  public function set_group($group_id) {
    $this->group = (empty($group_id) ? $GLOBALS['c_all'] : $group_id);
    if ($this->group!=$GLOBALS['c_all']) {
      foreach ($this->resources as $resource_id) {
        $this->remove_resource($resource_id);
      }
      $this->set_users(of_usergroup_get_group_users($this->group));
    }
    $this->modified = true;
  }

  public function set_users($user_ids) {
    if (empty($user_ids))
      $user_ids = array();
    if (!is_array($user_ids))
      $user_ids = array($user_ids);
    $to_add = array_diff($user_ids,$this->users);
    $to_remove = array_diff($this->users,$user_ids);
    foreach ($to_remove as $user_id) {
      $this->remove_user($user_id);
    }
    foreach ($to_add as $user_id) {
      $this->add_user($user_id);
    }
  }

  public function set_resources($resource_ids) {
    if (empty($resource_ids))
      $resource_ids = array();
    if (!is_array($resource_ids))
      $resource_ids = array($resource_ids);
    $to_add = array_diff($resource_ids,$this->resources);
    $to_remove = array_diff($this->resources,$resource_ids);
    foreach ($to_remove as $resource_id) {
      $this->remove_resource($resource_id);
    }
    foreach ($to_add as $resource_id) {
      $this->add_resource($resource_id);
    }
  }

  public function add_user($user_id) {
    global $obm;
    if (!in_array($user_id,$this->users)) {
      $this->users[] = $user_id;
      $this->set_class('user',$user_id,($user_id==$obm['uid'] ? 'eventOwner' : $this->generate_class()));
      $this->modified = true;
    }
  }

  public function add_resource($resource_id) {
    if (!in_array($resource_id,$this->resources)) {
      $this->resources[] = $resource_id;
      $this->set_class('resource',$resource_id,$this->generate_class());
      $this->modified = true;
    }
  }

  public function set_user_class($user_id, $class = null) {
    global $obm;
    if (!in_array($user_id,$this->users))
      return;
    if (($user_id==$obm['uid']) && (empty($class))) {
      $class='eventOwner';
    } elseif (empty($class)) {
      $class=$this->generate_class();
    }
    $this->set_class('user',$user_id,$class);
  }

  public function set_resource_class($resource_id, $class = null) {
    global $obm;
    if (!in_array($resource_id,$this->resources))
      return;
    if (empty($class)) {
      $class=$this->generate_class();
    }
    $this->set_class('resource',$resource_id,$class);
  }

  public function set_entity_class($entity, $entity_id, $class = null) {
    call_user_func(array($this,"set_{$entity}_class"),$entity_id, $class);
  }

  public function remove_user($user_id) {
    $old_count = count($this->users);
    $this->users = array_diff($this->users,array($user_id));
    if (count($this->users)<$old_count)
      $this->remove_class('user',$user_id);
  }

  public function remove_resource($resource_id) {
    $old_count = count($this->resources);
    $this->resources = array_diff($this->resources,array($resource_id));
    if (count($this->resources)<$old_count)
      $this->remove_class('resource',$resource_id);
  }

  public function remove_all() {
    $this->group = null;
    $this->users = array();
    $this->resources = array();
    $this->init_classes();
  }

  public function init_classes() {
      $this->classes = array(
      'by_entity' => array(
        'user' => array(),
        'resource' => array()
      ),
      'by_class' => array(
        'eventOwner' => 0
      )
    );
    for ($i=0;$i<20;$i++) {
      $this->classes['by_class']["eventOwner{$i}"] = 0;
    }
  }

  public function save() {
    if ($this->id)
      $return = $this->update();
    else
      $return = $this->insert();
    $this->modified = false;
    return $return;
  }

  public function delete() {
    global $cdg_sql, $obm;

    $user_id = sql_parse_id($obm['uid']);
    $obm_q = new DB_OBM;

    of_entity_delete('obmbookmark', $this->id);
    $id = sql_parse_id( $this->id, true);

    // Delete view
    $query = "DELETE FROM ObmBookmark WHERE obmbookmark_id $id"; 
    $obm_q->query($query);

    // Delete default view (if the default view is the deleted one)
    $query = "DELETE FROM UserObmPref
      WHERE userobmpref_user_id=$user_id
      AND userobmpref_option='set_cal_default_view'
      AND userobmpref_value $id
      ";
    display_debug_msg($query, $cdg_sql, 'CalendarView::delete()');
    $obm_q->query($query);

    $message = "\"obmbookmark_id\": \"{$this->id}\"";

    return $message;
  }

  protected function insert() {
    global $cdg_sql, $obm;

    $obm_q = new DB_OBM;

    $calendar_bookmarkproperties = array(
      'category'  => $this->category,
      'cal_range' => $this->cal_range,
      'cal_view'  => $this->cal_view
    );

    if ($this->group!=$GLOBALS['c_all']) {
      $calendar_bookmarkproperties['group'] = $this->group;
    } elseif (!empty($this->users)) {
      $calendar_bookmarkproperties['users'] = implode(',', $this->users);
      $calendar_bookmarkproperties['user_classes'] = serialize($this->classes['by_entity']['user']);
    }
    if (!empty($this->resources)) {
      $calendar_bookmarkproperties['resources'] = implode(',', $this->resources);
      $calendar_bookmarkproperties['resource_classes'] = serialize($this->classes['by_entity']['resource']);
    }

    $user_id = sql_parse_id($obm['uid']);
    // Insert ObmBookmark
    $query = "INSERT INTO ObmBookmark(
      obmbookmark_user_id,
      obmbookmark_label,
      obmbookmark_entity) VALUES (
        $user_id,
        '{$this->label}',
        'calendar')";
    display_debug_msg($query, $cdg_sql, 'CalendarView::insert()');
    $obm_q->query($query);

    $this->id = $obm_q->lastid();
    of_entity_insert('obmbookmark', $this->id);
    // Insert ObmBookmark properties 
    foreach($calendar_bookmarkproperties as $property => $value) {
      $query = "INSERT INTO ObmBookmarkProperty (
        obmbookmarkproperty_bookmark_id,
        obmbookmarkproperty_property,
        obmbookmarkproperty_value) VALUES (
          {$this->id},
          '$property',
          '$value')";
      display_debug_msg($query, $cdg_sql, 'CalendarView::insert()');
      $obm_q->query($query);
    }
  }

  protected function update() {
    //FIXME: NOT implemented yet
  }

  protected function get_users_data($nb_max, $user_readable) {
    global $cdg_sql;

    $multidomain = sql_multidomain('userobm');

    $obm_q = new DB_OBM;
    $query = "SELECT userobm_lastname, userobm_firstname, userobm_id
      FROM UserObm
      WHERE userobm_id IN (".implode(",", $this->users).") $multidomain";
    display_debug_msg($query, $cdg_sql, 'CalendarView::get_users_data()');
    $obm_q->query($query);

    $u = array();
    while ($obm_q->next_record()) {
      $id = $obm_q->f('userobm_id');
      $firstname = $obm_q->f('userobm_firstname');
      $lastname = $obm_q->f('userobm_lastname');
      $u[$id] = "$firstname $lastname";
    }

    // Preserve the users order
    $data = array();
    foreach ($this->users as $id) {
      if (in_array($id,$user_readable) && $nb_max>0) {
        $data[$id] = array(
          'name'  => $u[$id],
          'class' => $this->get_class($id,'user'),
          'type'  => "user",
          'id'    => $id
        );
        $nb_max--;
      }
    }

    return $data;
  }

  protected function get_resources_data($nb_max, $resource_readable) {
    global $cdg_sql;

    $multidomain = sql_multidomain('resource',$and);

    $obm_q = new DB_OBM;
    $query = "SELECT resource_name, resource_id
      FROM Resource 
      WHERE resource_id IN (".implode(",", $this->resources).") AND $multidomain";
    display_debug_msg($query, $cdg_sql, 'CalendarView::get_resources_data()');
    $obm_q->query($query);

    $r = array();
    while ($obm_q->next_record()) {
      $id = $obm_q->f('resource_id');
      $name = $obm_q->f('resource_name');
      $r[$id] = "$name";
    }

    // Preserve the resources order
    $data = array();
    foreach ($this->resources as $id) {
      if (in_array($id,$resource_readable) && $nb_max>0) {
        $data[$id] = array(
          'name'  => $r[$id],
          'class' => $this->get_class($id,'resource'),
          'type'  => "resource",
          'id'    => $id
        );
        $nb_max--;
      }
    } 

    return $data;
  }

  protected function get_class($id,$entity) {
    $c = $this->classes['by_entity'][$entity][$id];
    if (empty($c))
      $c = $this->generate_class();
    return $c;
  }

  protected function set_class($entity,$id,$class) {
    $old_class = $this->classes['by_entity'][$entity][$id];
    if (!empty($old_class))
      $this->classes['by_class'][$old_class] = max(0,intval($this->classes['by_class'][$old_class])-1);
    $this->classes['by_entity'][$entity][$id] = $class;
    if (!empty($class))
      $this->classes['by_class'][$class] = intval($this->classes['by_class'][$class])+1;
  }

  protected function remove_class($entity,$id) {
    $old_class = $this->classes['by_entity'][$entity][$id];
    if (!is_null($old_class)) {
      $this->classes['by_entity'][$entity][$id] = null;
      $this->classes['by_class'][$old_class] = max(0,intval($this->classes['by_class'][$old_class])-1);
    }
  }

  protected function generate_class() {
    $min = 9000;
    $index = 0;
    for ($i=0;($i<20);$i++) {
      $class = "eventOwner{$i}";
      if ($this->classes['by_class'][$class] < $min) {
        $min = $this->classes['by_class'][$class];
        $index = $class;
      }
    }
    return $index;
  }
}

?>
