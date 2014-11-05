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

require_once(dirname(__file__)."/../contact/addressbook.php");
require_once(dirname(__file__)."/../share/share_query.inc");
require_once('obm_eventdiff.php');
require_once('calendar_utils.php');

function run_query_calendar_detail($calendar_id, $occurrence_date=null) {
  global $cdg_sql;

  if ($occurrence_date !== null) {
    $exception_q = run_query_find_event_exception($calendar_id, $occurrence_date);
    if ($exception_q !== null) {
      return $exception_q;
    }
  }

  $obm_q = new DB_OBM;

  if(preg_match('/^OBM-/', $calendar_id)) {
    $get_id = "event_ext_id = '".$obm_q->escape($calendar_id)."'";
  } else {
    $parsed_calendar_id = sql_parse_id($calendar_id, true);
    $get_id = "event_id $parsed_calendar_id";
  }

  $db_type = $obm_q->type; 
  $timeupdate = sql_date_format($db_type, 'event_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'event_timecreate', 'timecreate');
  $multidomain = sql_multidomain('event');
  $eventexception_sql_if = sql_if($db_type, 'ee.eventexception_parent_id IS  NULL', '0', '1');
  $query = "SELECT
    event_id,
    event_ext_id,
    event_userupdate,
    event_usercreate,
    e.evententity_entity_id AS event_entity_id,
    c.userobm_login AS usercreate,
    u.userobm_login AS userupdate,
    $timeupdate,
    $timecreate,
    event_owner,
    o.userobm_lastname AS owner_lastname,
    o.userobm_firstname AS owner_firstname,
    d.domain_name AS owner_domain,
    d.domain_uuid AS owner_domain_uuid,
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
    event_domain_id,
    event_origin,
    event_allow_documents,
    event_date,
    $eventexception_sql_if AS event_is_exception,
    ee.eventexception_parent_id AS event_eventexception_parent_id,
    ee.eventexception_date AS event_eventexception_date,
    event_sequence
    FROM
    Event
    INNER JOIN EventEntity AS e ON event_id = evententity_event_id
    LEFT JOIN EventCategory1 ON event_category1_id = eventcategory1_id
    INNER JOIN UserObm AS c ON event_usercreate = c.userobm_id
    LEFT JOIN UserObm AS u ON event_userupdate = u.userobm_id
    INNER JOIN UserObm AS o ON event_owner = o.userobm_id
    INNER JOIN Domain AS d ON d.domain_id = o.userobm_domain_id
    LEFT JOIN EventTag AS t ON event_tag_id = t.eventtag_id
    LEFT JOIN EventException AS ee ON event_id = ee.eventexception_child_id
    WHERE event_type='VEVENT'
    AND $get_id
    $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_detail()');
  $obm_q->query($query);
  if ( !$obm_q->num_rows() ) {
    return run_query_calendar_detail_without_usercreate($calendar_id);
  }
  $obm_q->next_record();

  return $obm_q;
}

function run_query_find_event_exception($parent_event_id, $date_occurrence) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $timeupdate = sql_date_format($db_type, 'event_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'event_timecreate', 'timecreate');
  $multidomain = sql_multidomain('event');

  $query = "SELECT
    event_id,
    event_ext_id,
    event_userupdate,
    event_usercreate,
    e.evententity_entity_id AS event_entity_id,
    c.userobm_login AS usercreate,
    u.userobm_login AS userupdate,
    $timeupdate,
    $timecreate,
    event_owner,
    o.userobm_lastname AS owner_lastname,
    o.userobm_firstname AS owner_firstname,
    d.domain_name AS owner_domain,
    d.domain_uuid AS owner_domain_uuid,
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
    event_domain_id,
    event_origin,
    event_allow_documents,
    event_date,
    ee.eventexception_parent_id AS event_eventexception_parent_id,
    ee.eventexception_date AS event_eventexception_date,
    event_sequence
    FROM
    Event
    INNER JOIN EventEntity AS e ON event_id = evententity_event_id
    LEFT JOIN EventCategory1 ON event_category1_id = eventcategory1_id
    INNER JOIN UserObm AS c ON event_usercreate = c.userobm_id
    LEFT JOIN UserObm AS u ON event_userupdate = u.userobm_id
    INNER JOIN UserObm AS o ON event_owner = o.userobm_id
    INNER JOIN Domain AS d ON d.domain_id = o.userobm_domain_id
    LEFT JOIN EventTag AS t ON event_tag_id = t.eventtag_id
    LEFT JOIN EventException AS ee ON event_id = ee.eventexception_child_id
    WHERE event_type='VEVENT'
    AND ee.eventexception_parent_id ".sql_parse_id($parent_event_id, true)."
    AND ee.eventexception_date='".gmdate('Y-m-d H:i:s', $date_occurrence)."'
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_find_event_exception()');
  $obm_q->query($query);
  if ($obm_q->next_record()) {
    return $obm_q;
  }
  else {
    return null;
  }
}

///////////////////////////////////////////////////////////////////////////////
// Return events details, without the inner jointure on event_usercreate field :
// if the usercreate is deleted other users can't see the event anymore (M3190)
//
// Parameters:
//   - $calendar_id (can be Event table dependant if is_numeric(), or world-wide
//                   unique if begins with "OBM-")
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_detail_without_usercreate($calendar_id) {
  global $cdg_sql;
  $obm_q = new DB_OBM;

  if(preg_match('/^OBM-/', $calendar_id)) {
    $get_id = "event_ext_id = '".$obm_q->escape($calendar_id)."'";
  } else {
    $calendar_id = sql_parse_id($calendar_id, true);
    $get_id = "event_id $calendar_id";
  }

  $db_type = $obm_q->type; 
  $timeupdate = sql_date_format($db_type, 'event_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'event_timecreate', 'timecreate');
  $multidomain = sql_multidomain('event');
  $eventexception_sql_if = sql_if($db_type, 'ee.eventexception_parent_id IS  NULL', '0', '1');
  $query = "SELECT
    event_id,
    event_ext_id,
    event_userupdate,
    event_usercreate,
    e.evententity_entity_id AS event_entity_id,
    '' AS usercreate,
    u.userobm_login AS userupdate,
    $timeupdate,
    $timecreate,
    event_owner,
    o.userobm_lastname AS owner_lastname,
    o.userobm_firstname AS owner_firstname,
    d.domain_name AS owner_domain,
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
    event_domain_id,
    event_origin,
    event_allow_documents,
    event_date,
    $eventexception_sql_if AS event_is_exception,
    ee.eventexception_parent_id AS event_eventexception_parent_id,
    ee.eventexception_date AS event_eventexception_date,
    event_sequence
    FROM
    Event
    INNER JOIN EventEntity AS e ON event_id = evententity_event_id
    LEFT JOIN EventCategory1 ON event_category1_id = eventcategory1_id
    LEFT JOIN UserObm AS u ON event_userupdate = u.userobm_id
    INNER JOIN UserObm AS o ON event_owner = o.userobm_id
    INNER JOIN Domain AS d ON d.domain_id = o.userobm_domain_id
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
// Return all the name and first name of contacts
// Parameters: 
//   - $contacts : users id array, the event is assigned to 
///////////////////////////////////////////////////////////////////////////////
function get_calendar_contact_name($contacts='') {
  $names = array();
  foreach ($contacts as $id) {
	$contact = OBM_Contact::get($id); 
	$c = $contact->display_name(); 
    $names[$id] = $c;
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
  if ((is_array($entities['contact']))
    && (count($entities['contact']) > 0)) {
      $ret['contact'] = get_calendar_contact_name($entities['contact']);
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
// Query Execution : Update event timeupdate                                 //
// Parameters:
//   - $event_id : ...
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_touch_event($event_id) {
  $obm_q = new DB_OBM;
  $id = sql_parse_id($event_id, true);
  $query = "UPDATE Event
    SET event_timeupdate = NOW()
    WHERE event_id $id";
  // SOLR
  run_query_calendar_solr_store($event_id);
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_touch_event()');
  return $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Insert a user decision for an user
// Parameters:
//   - $calendar : Calendar params
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_insert_decision($calendar, $userId) {

  $entity_id = $calendar['entity_id'];
  $entity = $calendar['entity_kind'];
  $event_id = $calendar['calendar_id'];
  $state = $calendar['decision_event'];
  $owner_notification = $calendar['owner_notification'];
  $event =  run_query_calendar_detail($event_id);

  $concernedPeople = get_concerned_people_for_event($event_id);

  if($entity == 'user' && count($concernedPeople) <= 1 && $state == "DECLINED"){
    $query = "DELETE FROM Event WHERE event_id = $event_id";
    $obm_q = new DB_OBM;
    $obm_q->query($query);
  }
  else{
  	if ($event->f('event_repeatkind') != 'none') {
      run_query_calendar_update_occurrence_state($event_id,$entity,$entity_id,$state, true, $owner_notification);
  	} else {
  	  run_query_calendar_update_occurrence_state($event_id,$entity,$entity_id,$state,false, $owner_notification);
  	}
  }
}

function get_concerned_people_for_event($eventId){
  $concernedPeople = array();
 
  $attendees = run_query_get_events_attendee(array($eventId), false, false);
  while($attendees->next_record()){
    $attendeeCommonName = $attendees->f("eventlink_label");
    $attendeeId = $attendees->f("eventlink_entity_id");
    $concernedPeople[$attendeeId] = $attendeeCommonName;
  }
  return $concernedPeople;
}


///////////////////////////////////////////////////////////////////////////////
// Update a user decision for its participation to an event using internal id
// Parameters:
//   - $event_id : event Id
//   - $user_id  : User id
//   - $state    : New state to set (ACCEPTED, NEEDS-ACTION or DECLINED)
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_update_occurrence_state($event_id, $entity, $entity_id, $state, $isRecurrent = false, $owner_notification = true, $force = false) {
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
	
	if ($isRecurrent == true) {
		$retour = run_query_calendar_update_entity_recurrent_event($entity, $entity_id, $oldEvent->uid, $state);
	} else {
		$retour = run_query_calendar_update_entity_event($entity, $entity_id, $event_id, $state);
	}

	if ($retour) {
		//$retour = run_query_calendar_touch_event($event_id);

		if ($owner_notification == 'true') {
			OBM_EventFactory::getInstance()->store(OBM_EventFactory::getInstance()->getById($event_id), $oldEvent);
		}

	}

	return $retour;
}



/**
* increment sequence number for all events having ext_id $event_ext_id
* => this propagates changes in exceptions
*
*/
function run_query_increment_sequence($event_ext_id){
  global $cdg_sql;

  $query = "UPDATE Event
    SET event_sequence = event_sequence + 1
    WHERE event_ext_id = '$event_ext_id'";  
  display_debug_msg($query, $cdg_sql, 'run_query_increment_sequence()');

  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

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
    AND ( (event_repeatkind='none' AND event_date >= NOW() )
          OR (event_repeatkind <> 'none'
             AND (event_endrepeat IS NULL OR event_endrepeat >= NOW())
             ) 
        )
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
      AND ( (event_repeatkind='none' AND event_date >= NOW() )
          OR (event_repeatkind <> 'none'
             AND (event_endrepeat IS NULL OR event_endrepeat >= NOW())
             ) 
          )
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
  global $l_waiting_events, $conf_obm3_topbar;

  $multidomain = sql_multidomain('event');
  $obm_q = new DB_OBM;

  $writable_cal = array_keys(OBM_Acl::getAllowedEntities($id, 'calendar', 'write'));
  $writable_res = array_keys(OBM_Acl::getAllowedEntities($id, 'resource', 'write'));

  $resource_where = '';
  if(count($writable_res) > 0){
    $resource_where = "resourceentity_resource_id IN (".implode(",", $writable_res).") OR";
  }

  $query = "
    SELECT count(event_id) as nb_event
    FROM Event
    INNER JOIN EventLink ON event_id  = eventlink_event_id
    LEFT JOIN UserEntity ON userentity_entity_id = eventlink_entity_id
    LEFT JOIN ResourceEntity ON resourceentity_entity_id = eventlink_entity_id
    WHERE
    event_type='VEVENT'
    AND eventlink_state = 'NEEDS-ACTION'
    AND ( (event_repeatkind='none' AND event_date >= NOW() )
        OR (event_repeatkind <> 'none'
           AND (event_endrepeat IS NULL OR event_endrepeat >= NOW())
           ) 
        )
    AND ( $resource_where userentity_user_id IN (".implode(",", $writable_cal).") )
  ";

  $obm_q->query($query);
  if ($obm_q->num_rows() == 1) {
    $obm_q->next_record();
    $nb_event = $obm_q->f('nb_event');

    if($nb_event == 0) {
      $nb_event = '0';
    }

    $message_event = $l_waiting_events.' : '.$nb_event;

    $display['json'] = "{msg : '".phpStringToJsString($message_event)."', count : '".$nb_event."'}";
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
  return run_query_calendar_merge($groups, $users, of_usergroup_get_group_users);
}

function run_query_calendar_merge_resource_groups($groups, $resources) {
  return run_query_calendar_merge($groups, $resources, of_resourcegroup_get_group_resources);
}

function run_query_calendar_merge($groups, $users, $get_children) {
  $users = (is_array($users)?$users:array());

  if (is_array($groups)) {
    foreach($groups as $group_id) {
      $users = array_merge($users, $get_children($group_id));
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
  $users = $sel_entity_id['user'];
  if (is_array($groups)) {
    $user_group_array = array();
    foreach($groups as $group_id) {
      $user_group_array = array_merge($user_group_array, of_usergroup_get_group_users($group_id));
    }
    $users = array_merge($users, $user_group_array);
    $users = array_unique($users);
  }

  $resources = $sel_entity_id['resource'];
  $contacts = $sel_entity_id['contact'];
  $documents = $sel_entity_id['document'];

  if (!is_numeric($calendar['organizer']) || $calendar['organizer'] <= 0) {
    $organizerId = $obm['uid'];
  } else {
    $organizerId = $calendar['organizer'];
  }

  // Insert the Event
  list($event_id, $event_entity_id) = run_query_calendar_insert_event_data($calendar, $organizerId);
  
  // Insert EventLinks
  run_query_calendar_create_event_links($users, $contacts, $resources, $event_id, $organizerId);
  
  // Insert Event documents
  if (is_array($documents)) {
    $documents = array_unique($documents);
    foreach ($documents as $document_id) {
      run_query_calendar_attach_document($document_id, $event_entity_id);
    }
  }

  // Insert my own alert
  run_query_calendar_event_alert_insert($event_id, $organizerId, $calendar['alert']);
  
  // SOLR CREATE
  run_query_calendar_solr_store($event_id);

  $calendar['calendar_id'] = $event_id;
  $GLOBALS['send_notification_mail'] = $calendar['attendees_notification'];
    $data = array ( 
      'id' => $event_id,
      'title' => "$calendar[title]" ,
      'owner' => "$organizerId" ,
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
      'user' => $users,
      'resource' => $resources,
      'contact' => $contacts
    );
  OBM_EventFactory::getInstance()->create($data);
  return $event_id;
}


///////////////////////////////////////////////////////////////////////////////
// Insert an event data
// Parameters:
//   - $calendar : hashed calendar params
//   - $owner
// Returns: array(event id inserted, event entity id inserted)
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_insert_event_data($calendar, $organizer) {
  global $cdg_sql, $obm;
  $obm_q = new DB_OBM();

  if (array_key_exists('ext_id', $calendar)) {
    $ext_id = $calendar['ext_id'];
  } else {
    $ext_id = generateUniqueKey();
  }
  $title = sql_parse_text($calendar['title']);
  $category1_id = sql_parse_id( $calendar['category1']);
  $priority = sql_parse_int( $calendar['priority']);
  $description = sql_parse_text($calendar['description']);
  $location = sql_parse_text($calendar['location']);
  $all_day = ($calendar['all_day'] == 1) ? 'TRUE' : 'FALSE';
  $datebegin = $calendar['date_begin'];
  if ($all_day == 'TRUE') {
    $datebegin->setHour(0)->setMinute(0)->setSecond(0);
  }
  $event_duration = event_duration($datebegin, $calendar['event_duration'], $all_day == 'TRUE');
  $privacy = $calendar['privacy'];
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
  if (!isset($opacity)) $opacity = "OPAQUE";
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
    '$organizer',
    '".Of_Date::getOption('timezone')."',
    '$opacity', 
    '".$obm_q->escape($title)."',
    '$datebegin',
    '".$obm_q->escape($description)."',
    '".$obm_q->escape($properties)."',
    '".$obm_q->escape($location)."',
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
  run_query_update_description_with_attachments($eid);
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
function run_query_calendar_insert_entity_event($entity, $entity_id, $event_id, $state, $is_organizer = false, $event_is_exception = false) {
  global $cdg_sql, $obm;
  $obm_q = new DB_OBM;

  $entity_id = sql_parse_id($entity_id);
  $uid = sql_parse_id($obm['uid']);
  $is_organizer = ($is_organizer === true) ? 'true' : 'false';
  $query = "INSERT INTO EventLink (
    eventlink_timecreate,
    eventlink_usercreate,
    eventlink_event_id,
    eventlink_entity_id,
    eventlink_required,
    eventlink_state,
    eventlink_is_organizer,
    eventlink_comment)
    VALUES (
      NOW(),
    $uid,
    $event_id,
    ".of_entity_subquery($entity, $entity_id).",
    'REQ',
    '".$obm_q->escape($state)."',
    $is_organizer,
    '')";
  
  // OBMFULL-3226
  // This will propagate the new attendee to all moved exceptions
  if (!$event_is_exception) {
	  $exceptions = run_query_get_events_exception(array($event_id));
	  
	  while ($exceptions->next_record()) {
	  	$child_id = $exceptions->f('eventexception_child_id');
	  	
	  	if ($child_id) {
	  		run_query_calendar_insert_entity_event($entity, $entity_id, $child_id, $state, $is_organizer, true);
	  	}
	  }
  }

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_entity_event()');
  $obm_q->query($query);
  return $obm_q; 
}

///////////////////////////////////////////////////////////////////////////////
// Insert an occurrence of a event
// Parameters: 
//   - $entity : entity type 'user'
//   - $entity_id :
//   - $event_id  :
//   - $state     :
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_insert_entity_event_alert($entity_id, $event_id) {
  global $cdg_sql, $obm;

  $event_id = $event_id;
  $entity_id = sql_parse_id($entity_id);
  $uid = sql_parse_id($obm['uid']);
  $query = "INSERT INTO EventAlert (
    eventalert_timeupdate,
    eventalert_timecreate,
    eventalert_usercreate,
    eventalert_event_id,
    eventalert_user_id,
    eventalert_duration)
    SELECT NOW(),
      NOW(),
      $uid,
      $event_id,
      $entity_id,
      #CAST(userobmpref_value,INTEGER)
      FROM UserObmPref 
      WHERE userobmpref_user_id = $entity_id AND userobmpref_option = 'set_cal_alert';";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_insert_entity_event_alert()');
  $obm_q = new DB_OBM;
  $obm_q->xquery($query);
  return $obm_q; 
}

function run_query_calendar_update_entity_event($entity, $entity_id, $event_id, $state, $reset_comment = false) {
  global $cdg_sql, $obm;

  $sql_comment = '';
  if( $reset_comment ) $sql_comment = " , eventlink_comment = '' ";

  $entity_id = sql_parse_id($entity_id);
  $query = "UPDATE EventLink SET eventlink_state = '$state'
    $sql_comment
    WHERE eventlink_event_id = $event_id 
    AND eventlink_entity_id = ".of_entity_subquery($entity, $entity_id);

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_entity_event()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q; 
}

function run_query_calendar_update_entity_recurrent_event($entity, $entity_id, $ext_id, $state , $reset_comment = false) {
  global $cdg_sql, $obm;

  $sql_comment = '';
  if( $reset_comment ) $sql_comment = " , eventlink_comment = '' ";

  $entity_id = sql_parse_id($entity_id);
  $query = "UPDATE EventLink SET eventlink_state = '$state'
    $sql_comment
    WHERE eventlink_event_id IN (SELECT event_id FROM Event WHERE event_ext_id='$ext_id')
    AND eventlink_entity_id = ".of_entity_subquery($entity, $entity_id);

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_entity_recurrent_event()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}

function run_query_reset_comment_entity_event($entity, $entity_id, $event_id) {
  global $cdg_sql, $obm;
  $entity_id = sql_parse_id($entity_id);
  $query = "UPDATE EventLink SET eventlink_comment = ''
    WHERE eventlink_event_id = $event_id 
    AND eventlink_entity_id = ".of_entity_subquery($entity, $entity_id);

  display_debug_msg($query, $cdg_sql, 'run_query_reset_comment_entity_event()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}

function run_query_reset_comment_recurrent_event($entity, $entity_id, $ext_id) {
  global $cdg_sql, $obm;

  $entity_id = sql_parse_id($entity_id);
  $query = "UPDATE EventLink SET eventlink_comment = ''
    WHERE eventlink_event_id IN (SELECT event_id FROM Event WHERE event_ext_id='$ext_id')
    AND eventlink_entity_id = ".of_entity_subquery($entity, $entity_id);

  display_debug_msg($query, $cdg_sql, 'run_query_reset_comment_recurrent_event()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}

function run_query_calendar_set_organizer($user_id, $event_id) {
  global $cdg_sql, $obm;

  run_query_calendar_unset_organizer($event_id);

  $entity_id = sql_parse_id($entity_id);
  $query = "UPDATE EventLink SET eventlink_is_organizer = true
    WHERE eventlink_event_id = $event_id 
    AND eventlink_entity_id = ".of_entity_subquery('user', $user_id);

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_set_organizer()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q; 
}

function run_query_calendar_unset_organizer($event_id) {
  global $cdg_sql;
  
  $query = "UPDATE EventLink SET eventlink_is_organizer = false WHERE eventlink_event_id = $event_id";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_unset_organizer()');
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
// Return the Event Id for a given Event Entity id, null otherwise 
// Parameters: 
//   - $event_entity_id :
///////////////////////////////////////////////////////////////////////////////
function select_eventid_from_evententity($event_entity_id)
{
  global $cdg_sql, $obm;
  
  $query_fetch_event_id = "SELECT evententity_event_id
                          FROM EventEntity
                          WHERE evententity_entity_id = $event_entity_id";
                          
  $obm_q = new DB_OBM;
  $obm_q->query($query_fetch_event_id);
  if ($obm_q->num_rows() == 1 && $obm_q->next_record()) {
    $event_id = $obm_q->f('evententity_event_id');
  }
  else {
    $event_id = null;
  }
  return $event_id;
}

///////////////////////////////////////////////////////////////////////////////
// Returns an array of external, tokenized url for each obm document attached
// To an event
// Parameters: 
//   - $event_id  :
///////////////////////////////////////////////////////////////////////////////
function get_event_attachments_urls($event_id)
{
  $documents_rs = run_query_get_events_documents_with_name(array($event_id));
  $attachments = array();
  
  while ($documents_rs->next_record()) {
    $document_id = $documents_rs->f('document_id');
    $document_name = $documents_rs->f('document_name');
    $attachments[] = $GLOBALS['cgp_host'].'document/document_download.php?externalToken='.
    get_calendar_entity_share($document_id, 'document', 'private', 'document').
    '&document_id='.$document_id."#".urlencode($document_name);
  }
  return $attachments;
}

///////////////////////////////////////////////////////////////////////////////
// Returns an external, tokenized url for a obm document attached
// To an event
// Parameters:
//   - $document_id  :
///////////////////////////////////////////////////////////////////////////////
function get_document_attachments_url($document_id, $private) {
  $private = $private ? 'private' : 'public';
  return $GLOBALS['cgp_host'].'document/document_download.php?externalToken='.
    get_calendar_entity_share($document_id, 'document', $private , 'document').
    '&document_id='.$document_id;
}

///////////////////////////////////////////////////////////////////////////////
// Returns an event description given its id
// Parameters: 
//   - $event_id  :
///////////////////////////////////////////////////////////////////////////////
function get_event_description($event_id) {
  global $cdg_sql, $obm;
  
  $query_fetch_desc = "SELECT event_description
                      FROM Event
                      WHERE event_id = $event_id";
  
  $obm_q = new DB_OBM;
  $obm_q->query($query_fetch_desc);
  $obm_q->next_record();
  $description =  $obm_q->f('event_description');
  return $description;
}

///////////////////////////////////////////////////////////////////////////////
// For a given description and a given array of url, generate a new description
// with a unique obm reserved area listing all URLS linking to this event's
// attached documents.
// Parameters: 
//   - $attachments_urls :
//   - $description :
///////////////////////////////////////////////////////////////////////////////
function get_event_desc_with_attachements($attachments_urls, $description) {
  global $l_obm_reserved_event_data_attachment_begin;
  global $l_obm_reserved_event_data_attachment_end;

  $header = trim($l_obm_reserved_event_data_attachment_begin);
  $footer = trim($l_obm_reserved_event_data_attachment_end);

  if (!empty($attachments_urls)) {
    $desc_attachment_block = "\n\n" . $header . "\n" . implode("\n", $attachments_urls) . "\n" . $footer . "\n\n";
  }
  else {
    $desc_attachment_block = "";
  }
    
  $description = str_replace("\r\n", "\n", $description);
  $block_regexp = "/\s*" . preg_quote($header, "/") . ".*" . preg_quote($footer, "/") . "\s*/s";
  
  if (($ret = preg_match($block_regexp, $description)) == 1) {
    $new_description = preg_replace($block_regexp, $desc_attachment_block, $description);
  }
  else if ($ret == 0) {
    $new_description = $description . $desc_attachment_block;
  }
  else if ($ret == FALSE) {
    error_log("OBM-UI: Calendar: event_entity_id=[$event_entity_id], regexp error while forging description with attachments.");
  }
  return $new_description;
}

///////////////////////////////////////////////////////////////////////////////
// Attach a document to an event
// Parameters: 
//   - $document_id :
//   - $event_id  :
///////////////////////////////////////////////////////////////////////////////
function run_query_update_description_with_attachments($event_entity_id)
{
  global $cdg_sql, $obm;
  $obm_q = new DB_OBM;

  $event_id = select_eventid_from_evententity($event_entity_id);

  if (!empty($event_id)) {
    $new_description = get_event_desc_with_attachements(get_event_attachments_urls($event_id), get_event_description($event_id));
    $query_update_desc = "UPDATE Event
    SET event_description = '".$obm_q->escape($new_description, true)."'
    WHERE event_id = $event_id";
    $obm_q->query($query_update_desc);
  }
  else {
    error_log("OBM-UI: Calendar: event_entity_id=[$event_entity_id], problem finding event_id from event_entity_id while forging description with attachments.");
  }
}

///////////////////////////////////////////////////////////////////////////////
// Attach a document to an event
// Parameters: 
//   - $document_id :
//   - $event_id  :
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_attach_document($document_id, $event_id) {
  global $cdg_sql, $obm;

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
  run_query_update_description_with_attachments($event_id);
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
  run_query_update_description_with_attachments($event_id);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Detach all documents from an event && delete this documents
// Parameters: 
//   - $event_id  :
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_delete_document($event_id) {
  $event_entity_id = of_entity_get('event', $event_id);

  $obm_q = new DB_OBM;
  // Get documents ids
  $query = "SELECT documentlink_document_id FROM DocumentLink 
    WHERE documentlink_entity_id = $event_entity_id";
  $obm_q->query($query);
  while($obm_q->next_record()) {
  run_query_global_delete_document($obm_q->f("documentlink_document_id"));
  }

  // Delete linked document
  $query = "DELETE FROM DocumentLink 
    WHERE documentlink_entity_id = $event_entity_id";
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return;
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
  $document->destroy();

  $fd_dest = fopen ($filename,'w');

  if ($fd_dest == false) {
    die ($l_file_error.$dest.'dead');
  }
  fputs($fd_dest, $writer->buffer);
  fclose($fd_dest);
}

function run_icalendar_obmsync_insert($ics){
  global $obm;
  $userInfo = get_user_info($obm['uid']);
  
  if(empty($userInfo["email"])) {
  	return array(0,__("Couldn't find your Email adress for this domain"));
  }
  
  $obmSyncServer = of_domain_get_domain_syncserver($obm['domain_id']);
  if(!count($obmSyncServer)){
    return array(0, __("Couldn't find an obm-sync server for this domain"));
  }
  $iterator = new ArrayIterator($obmSyncServer);
  $obmSyncServer = $iterator->current();
  $obmSyncRootPath = "http://".$obmSyncServer[0]["ip"].":8080";
  $loginPath = "/obm-sync/services/login/trustedLogin";
  $username = $userInfo["login"];
  $password = get_trust_token($userInfo);
  $origin= $GLOBALS['c_origin_web'];

  $loginUrl = $obmSyncRootPath.$loginPath."?login=".$username."&password=".$password."&origin=".$origin;
  if ($userInfo["password_type"] == "MD5SUM") {
      $loginUrl .= "&isPasswordHashed=true";
      error_log("Password already hashed, adding 'isPasswordHashed' parameter to the obm-sync call");
  }
  $xmlLogin = new DomDocument();
  $xmlLogin->load($loginUrl);
  $errors = $xmlLogin->getElementsByTagName("error");
  if($errors->length > 0){
    return array(0, __("Couldn't login to obm-sync"));
  }
  $sid = $xmlLogin->getElementsByTagName('sid')
                  ->item(0)
                  ->nodeValue;

  $insertPath = "/obm-sync/services/calendar/importICalendar";
  $insertUrl = $obmSyncRootPath.$insertPath."?sid=".$sid."&calendar=".$username;

  // Sends ics to obm-sync import service
  $ch = curl_init($insertUrl);
  curl_setopt($ch, CURLOPT_POST, true);
  curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query(array("ics"=>$ics)));
  curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
  curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0);
  curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
  curl_setopt($ch,CURLOPT_HTTPHEADER,array (
      "Content-Type: application/x-www-form-urlencoded; charset=utf-8"
  ));
  $ret = curl_exec($ch);
  if (!$ret) {
      return array(0, __("An unknown error occured during the ics import"));
  }
  curl_close($ch);

  $xmlInsert = new DomDocument();
  $xmlInsert->loadXML($ret);
  $errors = $xmlInsert->getElementsByTagName("error");
  $error = null;
  if($errors->length > 0){
    $error = $errors->item(0)->nodeValue ?
             $errors->item(0)->nodeValue :
             __("An unknown error occured during the ics import");
    if(preg_match("/Calendar : duplicate with same extId found/", $error, $matches)){
      preg_match("/\[.*\]/", $error, $matches);
      
      $error = __("An event from the ics already exists")." ".$matches[0];
    }

    return array(0, $error);

  }

  $nbInsertedEvents = $xmlInsert->getElementsByTagName("value")->item(0)->nodeValue;
  return array($nbInsertedEvents, null);
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
  $nb_wrong_event = $writer->event_error;
  $nb_processed_event = count($document->getVevents()) - $nb_wrong_event;
  
  $document->destroy();

  return array($nb_processed_event, $nb_wrong_event);
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
  $obm_q = new DB_OBM();

  $sql = "SELECT userobm_id from UserObm ";
  $sql .= "WHERE ";
  $sql .= "UPPER(userobm_lastname) = '".$obm_q->escape(strtoupper($lastname))."' AND ";
  $sql .= "UPPER(userobm_firstname) = '".$obm_q->escape(strtoupper($firstname))."' AND ";
  $sql .= "userobm_archive = '0';";

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
function check_calendar_conflict($calendar, $sel_entity_id, $timezone) {
  global $ccalendar_conflicts_limit;

  if ($calendar['opacity'] == 'TRANSPARENT') return false;
  if(!$sel_entity_id && $calendar['calendar_id']) {
    $entities = get_calendar_event_entity($calendar['calendar_id']) ;
    foreach($entities as $entity => $data) {
      $sel_entity_id[$entity] = $data['ids'];
    }
  }
  $begin = clone $calendar['date_begin'];
  $begin->setHour(0)->setMinute(0)->setSecond(0);
  $end = get_conflict_end_date($calendar);

  if (!empty($sel_entity_id['user'])) {
    $sel_entity_id['user'] = array_keys(OBM_Acl::getAllowedEntities($GLOBALS[obm][uid], 'calendar', 'access', $sel_entity_id['user']));
  }
  $cal_entities = store_calendar_entities(get_calendar_entity_label($sel_entity_id));
  if (sizeof($cal_entities) == 0) return false; // user doesn't have read permission on attendees

  $occurrences = calendar_events_model($begin,$end,$cal_entities,$timezone, array('ACCEPTED', 'NEEDS-ACTION'));
  $of = &OccurrenceFactory::getInstance(); 
  if(!$calendar['all_day']) {
    $occurrences = $of->getPeriodOccurrences($calendar['date_begin'], $end);
  }

  if( !is_null($occurrences)) {
    $oc = array();
    $event_id = isset($calendar['calendar_id']) ? $calendar["calendar_id"] : NULL;

    if (!isset($calendar["repeat_kind"]) || $calendar["repeat_kind"] == "none") {
      foreach($occurrences as $occurrence) {
        if($occurrence->event->id != $event_id && $occurrence->event->isOpaque()) {
          $oc[] = $occurrence;

          if (count($oc) >= $ccalendar_conflicts_limit) {
            break;
          }
        }
      }
    } else {
      OccurrenceFactory::reset();

      $event = new Event(0, $calendar["event_duration"], $calendar["title"], NULL, NULL, $calendar["privacy"], NULL, NULL, $calendar["all_day"], $calendar["repeat_kind"], $calendar["owner"], NULL, NULL, 'OPAQUE', NULL);

      calendar_add_anonymous_occurrences($calendar["repeat_kind"], $calendar['date_begin'], $begin, $end, $calendar["repeatfrequency"], $event, $calendar["owner"], 'user', 'ACCEPTED', $calendar["repeat_days"]);

      $virtualOccurences = $of->getPeriodOccurrences($calendar['date_begin'], $end);
      $negativeExceptions = array_map(
        function($date_exception) {
          return $date_exception->getDateIso();
        },
        $calendar['date_exception']);

      foreach($virtualOccurences as $virtualOccurrence) {
        foreach($occurrences as $occurrence) {
          if($occurrence->event->id != $event_id
              && $occurrence->event->isOpaque()
              && $virtualOccurrence->date->compare($occurrence->end) < 0
              && $virtualOccurrence->end->compare($occurrence->date) > 0
              && !in_array($occurrence->date->getDateIso(), $negativeExceptions)) {
            $oc[] = $occurrence;

            if (count($oc) >= $ccalendar_conflicts_limit) {
              break 2;
            }
          }
          // The occurrences are sorted, so if we're past the date of our virtual occurrence, we can break
          if ($occurrence->date->compare($virtualOccurrence->end) > 0) {
            break;
          }
        }
      }
    }

    $occurrences = $oc;
  }

  OccurrenceFactory::reset();

  return count($occurrences) != 0 ? $occurrences : false;
}

///////////////////////////////////////////////////////////////////////////////
// Check for conflict : Only with the first occurrence of each event,
// Parameters:
//   - $calendar       : calendar parameters
///////////////////////////////////////////////////////////////////////////////
function quick_check_calendar_conflict($calendar, $entities, $timezone) {
  if ($calendar['opacity'] == 'TRANSPARENT') return false;

  $begin = clone $calendar['date_begin'];
  $end = clone $calendar['date_end'];
  if ($calendar['template_id'] > 0) {
    $t = run_query_calendar_get_template($calendar['template_id']);
    $q = $t[0]; 
    $all_day = ($q->f("event_allday"))?'TRUE':'FALSE';
    $end = clone $calendar['date_begin'];
    $calendar['date_end'] = $end->addTimestamp($q->f('event_duration'));

    if ($q->f("event_allday")) {
      $begin->setHour(0)->setMinute(0)->setSecond(0);
      $end->addDay(1)->setHour(0)->setMinute(0)->setSecond(0);
    }
  }

  // Event entities
  if (!empty($entities['user']['ids'])) {
    $sel_entity_id['user'] = array_keys(OBM_Acl::getAllowedEntities($GLOBALS[obm][uid], 'calendar', 'access', $entities['user']['ids']));
  }
  $sel_entity_id['resource'] = $entities['resource']['ids'];

  $cal_entities = store_calendar_entities(get_calendar_entity_label($sel_entity_id));
	$status = array('ACCEPTED', 'NEEDS-ACTION');
  $occurrences = calendar_events_model($begin,$end,$cal_entities,$timezone,$status);
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


///////////////////////////////////////////////////////////////////////////////
// Calendar specific rights checking (eg : delegation)
// Parameters:
//   - $params[] : user values
// Returns:
//   - true if OK, else false
///////////////////////////////////////////////////////////////////////////////
function check_calendar_update_rights($params) {
  global $err, $cgp_use, $l_error_delegation;

  $entity_id = $params['entity_id'];
  if ($entity_id > 0) {
    // Check if delegation is used
    if ($cgp_use['property']['delegation']) { 
      $obm_q = new DB_OBM;
      $query = "SELECT userobm_delegation FROM UserObm
        INNER JOIN CalendarEntity on calendarentity_calendar_id=userobm_id WHERE calendarentity_calendar_id='$entity_id'";

      display_debug_msg($query, $cdg_sql, "check_calendar_update_rights");
      $obm_q->query($query);
      if($obm_q->num_rows()>0) {
        $obm_q->next_record();
        $calendar_delegation = $obm_q->f("userobm_delegation");
  
        // Check delegation update right
        if (empty($calendar_delegation)) {
            return false;
        }
        if (! of_delegation_check_update_rights($params, $calendar_delegation, $calendar_delegation)) {
	        $err['msg'] .= $l_error_delegation;
	        return false;
        }
        else {
            return true;
        }
      }
    }
  }
  return false;
}




/**
 * Check if the user is an event's attendee
 * @param $event_id
 * @param $user_id
 * @return bool
 **/
function check_user_attendance($event_id, $user_id) {
  $sql = "
    SELECT 1 FROM EventLink 
    INNER JOIN EventEntity ON eventlink_event_id = evententity_event_id 
    INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id 
    WHERE eventlink_event_id = $event_id 
    AND userentity_user_id = $user_id";
    
  $obm_q = new DB_OBM;
  $obm_q->query($sql);
  return $obm_q->num_rows() == 1;
}

/**
 * Check if a token-auth'd person can download an attached document
 * @param $document_id
 * @param $user_id
 * @return bool
 **/
function check_document_access_by_token($document_id, $event_id) {
  $user_id = get_user_id_by_token();
  if (!$user_id || !check_user_attendance($event_id, $user_id)) return false;
  if (!in_array($document_id, get_calendar_event_document_ids($event_id))) return false;
  return true;
}

/**
 * Check for participation decision
 * Essentially a hook container to allow specific rules
 * @param $calendar parameters
 * @return bool
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
function check_calendar_decision_conflict($calendar, $timezone) {

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
  $occurrences = calendar_events_model($begin,$end,$cal_entities, $timezone);
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
  if(!in_array($calendar['organizer'], $users_array)){
    $organizer_array = array(0 => $calendar['organizer']);
    $users_array = array_merge($users_array, $organizer_array);
    $GLOBALS['display']['warm_add_organizer'] = true;
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
        // Avoid duplicates
        $query = "DELETE FROM DeletedEvent ".
          "WHERE deletedevent_event_ext_id='".$obm_q->escape($oldEvent->uid)."' AND ".
          "deletedevent_user_id ".sql_parse_id($u_id, true);
        display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_update(1)');
        $obm_q->query($query);

        $query = "INSERT INTO DeletedEvent (
          deletedevent_event_id,
          deletedevent_event_ext_id,
          deletedevent_user_id,
          deletedevent_origin,
          deletedevent_type,
          deletedevent_timestamp)
          VALUES (
            $event_id,
            '".$oldEvent->uid."',
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

  // always set organizer == owner
  if ( $calendar['organizer'] && is_numeric($calendar['organizer']) ) {
    $calendar['owner'] = $calendar['organizer'];
  }

  // XXXX bizarre les old_end et old_begin semblent etre affectes par l'update
  // ci-dessous sous mysql 4.0.23-10 (portable fourmi)
  run_query_calendar_update_event_data($calendar, $oldEvent); 

  $updatedEvent = OBM_EventFactory::getInstance()->getById($calendar['calendar_id']);
  
  $entities = get_calendar_event_entity($event_id);
  $new_entities = array();
  $entity_types = array('user', 'contact', 'resource', 'document');
  $db = new DB_OBM;
  foreach ($entity_types as $type) {
    $entity_array = "{$type}s_array";
    
    $same_entities[$type] = array();
    $removed_entities = null;
    
    if (is_array($entities[$type]['ids'])) {
      $new_entities[$type] = array_diff($$entity_array, $entities[$type]['ids']);
      $removed_entities = array_diff($entities[$type]['ids'], $$entity_array);
      if ($type == 'user' && count($removed_entities) > 0) {
        foreach ($entities[$type]['entity'] as $id => $u) {
          if ($u['is_organizer'] == true) {
            $k = array_search($id, $removed_entities);
            if($k !== false){
              unset($removed_entities[$k]);
              run_query_calendar_update_entity_event('user', $id, $event_id, 'DECLINED');
            }
          }
        }
      }
      if (count($removed_entities) > 0) {
        if ($type == 'document') {
          foreach ($removed_entities as $document_id) {
      		run_query_calendar_detach_document($document_id, $event_entity_id);
	      }			
        } else {
        	$target_event_ids = array($event_id);
        	$exceptions = run_query_get_events_exception(array($event_id));
        	
        	// OBMFULL-3226
        	// This gathers the event_ids of all moved exceptions to propagate the removal of the attendee
        	while ($exceptions->next_record()) {
        		$child_id = $exceptions->f('eventexception_child_id');
        	
       			if ($child_id) {
       				$target_event_ids[] = $child_id;
       			}
       		}
        	
          $query = "DELETE FROM EventLink WHERE eventlink_entity_id IN ".of_entity_subquery($type, $removed_entities)." AND eventlink_event_id IN (" . implode(',', $target_event_ids) . ")";
          $db->query($query);
        }
      }
      
      $same_entities[$type] = array_diff($entities[$type]['ids'], $new_entities[$type], $removed_entities);
    } else {
      $new_entities[$type] = $$entity_array;
    }
  }
  
  run_query_calendar_add_event_links($new_entities['user'], $new_entities['contact'], $new_entities['resource'], $event_id);
  run_query_calendar_set_organizer($calendar['organizer'], $event_id);
  
  if ($updatedEvent->shouldIncrementSequence($oldEvent)) {
  	if ($updatedEvent->isDifferent($oldEvent)) {
	  	if($oldEvent->repeat_kind == 'none' || $params['all'] == 1) {
	  		run_query_calendar_reset_occurrence_links($same_entities['user'], $same_entities['contact'], $same_entities['resource'], $calendar['calendar_id']);
	  	} else {
	    	run_query_calendar_reset_event_links($same_entities['user'], $same_entities['contact'], $same_entities['resource'], $oldEvent->uid);
	    }
  	}
	run_query_increment_sequence($oldEvent->uid);
  }
  
  foreach ($new_entities['document'] as $document_id) {
    run_query_calendar_attach_document($document_id, $event_entity_id);
  }

  // Insert My alert if set
  if (($calendar['alert']) > 0) {
    run_query_calendar_event_alert_insert($event_id, $calendar['organizer'], $calendar['alert']);
  } else {
    run_query_calendar_event_alert_delete($event_id, $calendar['organizer']);
  }
  
  // SOLR
  run_query_calendar_solr_store($event_id);

  $newEvent = OBM_EventFactory::getInstance()->getById($calendar['calendar_id']);
  $eventDiff = new OBM_EventDiff($oldEvent, $newEvent);

  if ($eventDiff->hasEventFullyChanged($oldEvent, $newEvent, false)
  	|| count($new_users) > 0 
  	|| count($removed_users) > 0
    || $eventDiff->hasResourcesAdded()
    || $eventDiff->hasResourcesRemoved() ) {
    // Send mail session var
    $GLOBALS['send_notification_mail'] = $calendar['attendees_notification'];
    // Notify Observers
    OBM_EventFactory::getInstance()->store(
              $newEvent,
              $oldEvent
             );
  }



}

function event_duration($datebegin, $duration, $allDay) {
  if (!$allDay) {
    return $duration;
  }
  
  $dateend = clone $datebegin;
  $dateend->addSecondsInUTC($duration);
  $durationInDays = $datebegin->daysCount($dateend);
  
  return $durationInDays * 24 * 60 * 60;
}

///////////////////////////////////////////////////////////////////////////////
// Event Data Update Execution
// Parameters: 
//   - $calendar : Calendar params
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_update_event_data($calendar, $oldEvent) {
  global $cdg_sql, $obm, $c_none;
  $title = sql_parse_text($calendar['title']);
  
  $category1_id = sql_parse_id( $calendar['category1']);
  $priority = $calendar['priority'];
  $description = sql_parse_text($calendar['description']);
  $location = sql_parse_text($calendar['location']);
  $datebegin = $calendar['date_begin'];
  ($calendar['all_day'] != 1) ? $all_day = 'FALSE': $all_day = 'TRUE';
  $event_duration = event_duration($datebegin, $calendar['event_duration'], $calendar['all_day'] == 1);
  $event_id = sql_parse_id( $calendar['calendar_id'], true); 
  $privacy = $calendar['privacy'];
  $repeat_kind = $calendar['repeat_kind'] ? $calendar['repeat_kind'] : "none";
  $repeat_days = $calendar['repeat_days'];
  if (isset($calendar['repeat_end'])) {
    $repeat_end = "'$calendar[repeat_end]'";
  } else {
    $repeat_end = 'NULL';
  }    
  $oldEvent_repeat_end = $oldEvent->repeat_end ? "'".$oldEvent->repeat_end."'" : "NULL";

  $update_owner = "";
  if ( $calendar["owner"] ) {
    $update_owner = "event_owner = ".$calendar["owner"].", ";
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

  $obm_q = new DB_OBM;
  $query = "UPDATE Event SET
    event_timeupdate = NOW(),
      event_userupdate = $obm[uid], 
      event_title = '".$obm_q->escape($title)."', 
      event_date = '".$obm_q->escape($datebegin)."',     
      event_description = '".$obm_q->escape($description)."',
      event_properties = '".$obm_q->escape($properties)."', 
      event_location = '".$obm_q->escape($location)."',
      event_category1_id = $category1_id,
      event_priority = '$priority',
      event_privacy = '$privacy', 
      event_opacity = '$opacity', 
      event_duration = '".$obm_q->escape($event_duration)."',
      event_allday = $all_day, 
      event_tag_id = $event_tag_id,
      event_allow_documents = '$allow_documents',
      event_origin = '$GLOBALS[c_origin_web]',
      event_repeatkind = '$repeat_kind',
      event_repeatfrequence = '$frequency',
      event_repeatdays = '$repeat_days',
      $update_owner
      event_endrepeat = $repeat_end
      WHERE
      event_id $event_id"; 


  display_debug_msg($query, $cdg_sql, 'run_query_calendar_update_event_data()');
  $obm_q->query($query);

  $event_id = $calendar['calendar_id'];


  if ($datebegin->getTimestamp() != $oldEvent->date_begin->getTimestamp()
  	|| $repeat_kind != $oldEvent->repeat_kind 
  	|| $frequency != $oldEvent->repeatfrequency 
  	|| $repeat_days != $oldEvent->repeat_days
  	|| $repeat_end != $oldEvent_repeat_end) {

  	run_query_reinitialize_recurrent_event($event_id);

    return;
  }

  $old_exceptions = array();
  $exceptions_q = run_query_get_events_exception(array($event_id));
  while ($exceptions_q->next_record()) {
    $eventexception_date = $exceptions_q->f('eventexception_date');
    $date = new Of_Date($eventexception_date, "GMT");
    $iso_date = $date->format(Of_Date::DATE_ISO);
    $old_exceptions[$iso_date] = array(
      'eventexception_date' => $eventexception_date,
      'child_id' => $exceptions_q->f('eventexception_child_id')
    );
  }

  if (!is_array($date_exception)) $date_exception = array();
  foreach ($date_exception as $value) {
    $iso_date = $value->format(Of_Date::DATE_ISO);
    if (isset($old_exceptions[$iso_date])) {
      $old_exceptions[$iso_date] = null;
      unset($old_exceptions[$iso_date]);
    } else {
      $calendar['old_date_begin'] = $value->setHour($datebegin)->setMinute($datebegin);
      run_query_calendar_event_exception_insert($calendar, '', true);
    }
  }

  foreach($old_exceptions as $data) {
    if (!empty($data))
      run_query_delete_one_event_exception($event_id,$data['eventexception_date'],$data['child_id']);
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
  $title = sql_parse_text($calendar['title']);
  $datebegin = $calendar['date_begin'];
  $event_duration = $calendar['duration'];
  $all_day = ($calendar["all_day"])?'TRUE':'FALSE';
  if ($all_day == 'TRUE' && $event_duration == 3600) {
      $event_duration = 86400;
  }
  $all = $calendar['all'];
  if ($all != 1) {
    $event_date = "event_date = '$datebegin', event_timezone = '" . $datebegin->getTimezone()->getName() . "',";
  }
  $oldEvent = OBM_EventFactory::getInstance()->getById($calendar['calendar_id']);
  $obm_q = new DB_OBM;
  $escape_title = ($oldEvent->privacy == 1) ? $obm_q->escape($oldEvent->title) : $obm_q->escape($title);
  $query = "UPDATE Event SET
      event_timeupdate = NOW(),
      event_userupdate = $obm[uid],
      event_title = '".$escape_title."',
      $event_date
      event_origin = '$GLOBALS[c_origin_ajax]',
      event_duration = '$event_duration',
      event_allday = $all_day
      WHERE
      event_id ".sql_parse_id($calendar['calendar_id'], true)." $multidomain";
	display_debug_msg($query, $cdg_sql, 'run_query_calendar_quick_event_update()');
	$obm_q->query($query);

	// reload event from database
	$newEvent = OBM_EventFactory::getInstance()->getById($calendar['calendar_id']);
	// increment sequence
	if ( $newEvent->shouldIncrementSequence($oldEvent) ) {
		run_query_increment_sequence($newEvent->uid);
		
		// OBMFULL-3499
		// This is done here so that participation status is up-to-date BEFORE the email is sent
		// This allows the attached ICS to contain the correct PARTSTAT for the attendees
		run_query_quick_attendee_update($calendar, $calendar['calendar_id']);
	}

	// reload once again to have the correct sequence number
	$newEvent = OBM_EventFactory::getInstance()->getById($calendar['calendar_id']);
	
  // Send mail session var
  $GLOBALS['send_notification_mail'] = $calendar['send_mail'];
  // Notify Observers
  OBM_EventFactory::getInstance()->store($newEvent,$oldEvent);
}


/**
 * Event Data  Execution for ajax client 
 * 
 * @param mixed $calendar 
 * @access public
 * @return void
 */
function run_query_calendar_quick_event_insert(&$calendar,$state) {
  global $cdg_sql, $obm;
  $title = sql_parse_text($calendar['title']);
  $datebegin = $calendar['date_begin'];
  $event_duration = $calendar['duration'];
  $organizerId = $calendar['entity_id'];
  $users = array($organizerId);
  $all_day = ($calendar['all_day'])? 'TRUE' : 'FALSE';
  $opacity = 'OPAQUE';
  if ($all_day == 'TRUE') {
    if ($event_duration == 3600) {
      $event_duration = "86400";
    }
    $opacity = $_SESSION["set_allday_opacity"] != '' ? $_SESSION["set_allday_opacity"] : 'OPAQUE';
  }

  $event_privacy = 0;
  $event_priority = 2;
  $event_repeatkind = 'none';
  $event_repeatfrequence = 1;
  $event_endrepeat = 'NOW()';
  $event_category1_id = "NULL,";
  $event_tag_id = "NULL";
  if ($calendar['template_id'] > 0) {
    $t = run_query_calendar_get_template($calendar['template_id']);
    $q = $t[0]; 
    $datebegin_tpl = new Of_Date($q->f('event_date'), 'GMT');
    $datebegin = $datebegin_tpl->setDay($datebegin->getDay())->setMonth($datebegin->getMonth())->setYear($datebegin->getYear());
    $attendees = $t[1];
    $event_duration = $q->f('event_duration');
    $calendar['duration'] = $event_duration;
    $all_day = ($q->f("event_allday"))?'TRUE':'FALSE';
    if ($all_day === 'TRUE') {
      $datebegin->setHour(0)->setMinute(0)->setSecond(0);
      $calendar['all_day'] = 1;
    }
    $event_location = $q->f("event_location");
    $calendar['location'] = $event_location; 
    $event_privacy = $q->f("event_privacy");
    $event_description = $q->f("event_description");
    $calendar['description'] = $event_description; 
    $event_priority = $q->f("event_priority");
    $event_repeatkind = $q->f("event_repeatkind");
    $event_repeatdays = $q->f("event_repeatdays");
    $event_tag_id = $q->f("event_tag_id") ? $q->f("event_tag_id") : "NULL";
    $event_repeatfrequence = $q->f("event_repeatfrequence");
    $event_endrepeat = $q->f("event_endrepeat") ? "'".$q->f("event_endrepeat")."'" : "NULL" ;
    $event_category1_id = $q->f("event_category1_id") ? $q->f("event_category1_id")."," : "NULL,";
    $event_properties = $q->f("event_properties");

    $users = $attendees['user'];
    $resources = $attendees['resource'];
    $contacts = $attendees['contact'];
    $groups = $attendees['group'];
    if (is_array($groups)) {
      $user_group_array = array();
      foreach($groups as $group_id) {
        $user_group_array = array_merge($user_group_array, of_usergroup_get_group_users($group_id));
      }
      $users = array_merge($users,$user_group_array);
      $users = array_unique($users);
    }
    $documents = $attendees['document'];

  }

  $ext_id = generateUniqueKey();

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
    event_opacity,
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
    event_ext_id,
    event_tag_id) VALUES (
    NOW(),
    NOW(),
    $obm[uid],
    'VEVENT',
    '$GLOBALS[c_origin_ajax]',
    ".$organizerId.",
    '".Of_Date::getOption('timezone')."', 
    '".$obm_q->escape($title)."', 
    '".$obm_q->escape($datebegin)."',     
    '$event_duration',
    $all_day,
    '$opacity',
    '".$obm_q->escape($event_description)."',
    '".$obm_q->escape($event_properties)."',
    $event_privacy,
    '$event_repeatkind',
    $event_repeatfrequence,
    $event_endrepeat,
    '$event_repeatdays',
    '".$obm_q->escape($event_location)."',
    $event_category1_id
    $event_priority,
    $obm[domain_id],
    '$ext_id',
    $event_tag_id)";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_quick_event_insert()');
  $obm_q->query($query);
  $event_id = $obm_q->lastid();
  $event_entity_id = of_entity_insert('event', $event_id);

  run_query_calendar_create_event_links($users, $contacts, $resources, $event_id, $organizerId);

  // Documents
  if (is_array($documents)) {
    $documents = array_unique($documents);
    foreach ($documents as $document_id) {
      run_query_calendar_attach_document($document_id, $event_entity_id);
    }
  }
  run_query_update_description_with_attachments($event_entity_id);

// Send Mail
  $GLOBALS['send_notification_mail'] = $calendar['send_mail'];
  OBM_EventFactory::getInstance()->create(array('id' => $event_id));
  
  return $event_id;
}

function run_query_calendar_create_event_links($users, $contacts, $resources, $event_id, $organizer) {
  run_query_calendar_add_event_links($users, $contacts, $resources, $event_id);
  if (!in_array($organizer, (array) $users)) {
    run_query_calendar_insert_entity_event('user', $organizer, $event_id, 'ACCEPTED');
    $GLOBALS['display']['warm_add_organizer'] = true;
  }
  run_query_calendar_set_organizer($organizer, $event_id);
}

function run_query_calendar_add_event_links($users, $contacts, $resources, $event_id) {
  global $obm;
  
  if (is_array($users)) {
    $users = array_unique($users);
    foreach ($users as $user_id) {
      if ($user_id == $obm['uid'] || OBM_Acl::canWrite($obm['uid'], 'calendar', $user_id)) {
        run_query_calendar_insert_entity_event('user', $user_id, $event_id, 'ACCEPTED');
      } else {
        run_query_calendar_insert_entity_event('user', $user_id, $event_id, 'NEEDS-ACTION');
      }
    }
  }
  if (is_array($contacts)) {
    $contacts = array_unique($contacts);
    foreach ($contacts as $contact_id) {
      run_query_calendar_insert_entity_event('contact', $contact_id, $event_id, 'NEEDS-ACTION');
    }
  }
  if (is_array($resources)) {
    $resources = array_unique($resources);
    foreach ($resources as $res_id) {
      if (OBM_Acl::canWrite($obm['uid'], 'resource', $res_id)) {
        run_query_calendar_insert_entity_event('resource', $res_id, $event_id, 'ACCEPTED');
      } else {
        run_query_calendar_insert_entity_event('resource', $res_id, $event_id, 'NEEDS-ACTION');
      }
    }
  }
}

function run_query_calendar_reset_event_links($users, $contacts, $resources, $event_ext_id) {
  global $obm;
  
  if (is_array($users)) {
    $users = array_unique($users);
    foreach ($users as $user_id) {
      if ($user_id != $obm['uid'] ) {
      	if ( !OBM_Acl::canWrite($obm['uid'], 'calendar', $user_id)) {
      		run_query_calendar_update_entity_recurrent_event('user', $user_id, $event_ext_id, 'NEEDS-ACTION', true);
      	} else {
      		run_query_calendar_update_entity_recurrent_event('user', $user_id, $event_ext_id, 'ACCEPTED', true);
      	}
      }
    }
  }
  if (is_array($contacts)) {
    $contacts = array_unique($contacts);
    foreach ($contacts as $contact_id) {
      run_query_calendar_update_entity_recurrent_event('contact', $contact_id, $event_ext_id, 'NEEDS-ACTION', true);
    }
  }
  if (is_array($resources)) {
    $resources = array_unique($resources);
    foreach ($resources as $res_id) {
      if (!OBM_Acl::canWrite($obm['uid'], 'resource', $res_id)) {
        run_query_calendar_update_entity_recurrent_event('resource', $res_id, $event_ext_id, 'NEEDS-ACTION', true);
      }
    }
  }
}

function run_query_calendar_reset_occurrence_links($users, $contacts, $resources, $id) {
	global $obm;
	if (is_array($users)) {
		$users = array_unique($users);
		foreach ($users as $user_id) {
			if ($user_id != $obm['uid'] ) {
				if ( !OBM_Acl::canWrite($obm['uid'], 'calendar', $user_id) ) {
					run_query_calendar_update_entity_event('user', $user_id, $id, 'NEEDS-ACTION', true);
				} else {
					run_query_calendar_update_entity_event('user', $user_id, $id, 'ACCEPTED', true);
				}
			}
		}
	}
	if (is_array($contacts)) {
		$contacts = array_unique($contacts);
		foreach ($contacts as $contact_id) {
			run_query_calendar_update_entity_recurrent_event('contact', $contact_id, $id, 'NEEDS-ACTION', true);
		}
	}
	if (is_array($resources)) {
		$resources = array_unique($resources);
		foreach ($resources as $res_id) {
			if (!OBM_Acl::canWrite($obm['uid'], 'resource', $res_id)) {
				run_query_calendar_update_entity_event('resource', $res_id, $id, 'NEEDS-ACTION', true);
			}
		}
	}
}

/**
 * Create an exception for an event 
 * 
 * @param mixed $event_q 
 * @param mixed $calendar 
 * @access public
 * @return mixed exception id if an exception event has been created in the event table, an array('already_set' => true, 'id' => exceptionid) otherwise
 */
function run_query_calendar_event_exception_insert( $calendar, $event_q = '', $run_observers = false, $date_occurrence = null, $sel_entity_id = null) {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;

  if( $date_occurrence != NULL){
    $id = sql_parse_id($calendar['event_id']);
    $check_exception_already_set = "SELECT eventexception_child_id FROM EventException WHERE eventexception_date = '".$date_occurrence."' AND eventexception_parent_id = ".$id.";";
    display_debug_msg($check_exception_already_set, $cdg_sql, 'check_exception_already_set');
    $obm_q->query($check_exception_already_set);
    if ($obm_q->next_record())
      return array('already_set' => true, 'id' => $obm_q->f('eventexception_child_id'));

    $exception_recurrence_id_date = $date_occurrence;
  } else {
    $id = sql_parse_id($calendar['calendar_id']);
    $exception_recurrence_id_date = $calendar['old_date_begin'];
  }
  $exception_date = $obm_q->escape($calendar['date_begin']) ? $obm_q->escape($calendar['date_begin']) : $date_occurrence;
  $event_duration = $calendar['event_duration'] > 0 ? $calendar['event_duration'] : $calendar['duration'];
  $title = $calendar['event_title'] ? sql_parse_text($calendar['event_title']) : sql_parse_text($calendar['title']);

  $oldEvent = OBM_EventFactory::getInstance()->getById($id);

  $isMovedException = is_object($event_q);

  if ($isMovedException) {
    $privacy = $calendar['privacy']? $calendar['privacy'] : $event_q->f('event_privacy');
    $repeat_kind = $event_q->f('event_repeatkind');
    $repeat_days = $event_q->f('event_repeatdays');
    if ($event_q->f('event_endrepeat')) {
      $repeat_end = new Of_Date($event_q->f('event_endrepeat'), 'GMT');
      $repeat_end = "'$repeat_end'";
    } else {
      $repeat_end = 'NULL';
    }
    $repeatfrequency = $event_q->f('event_repeatfrequence');
    $category1_id = $calendar['category1'] ? sql_parse_id($calendar['category1']) : sql_parse_id( $event_q->f('event_category1_id'));
    $priority = $calendar['priority'] ? sql_parse_int($calendar['priority']) : sql_parse_int( $event_q->f('event_priority'));
    $event_tag_id = $event_q->f('tag_id') ;
    if ($event_tag_id == $GLOBALS['c_none']) $event_tag_id = 'NULL' ;
    if (is_null($event_tag_id)) $event_tag_id = 'NULL' ;
    $description = $calendar['description'] ? sql_parse_text($calendar['description']) : $event_q->f('event_description');
    $properties = $event_q->f('event_properties');
    $location = $calendar['location'] ? $calendar['location'] : $event_q->f('event_location');
    ($calendar['all_day'] != 1)? $all_day = 'FALSE' : $all_day = 'TRUE';
    $ext_id = $event_q->f('event_ext_id');
    $oldOwner = intval($oldEvent->get("owner")->__toString());

    $sequence_number = $event_q->f('event_sequence');
    # Increment sequence number, just like we do in OBM-Sync
    $sequence_number++;
    $escape_title = ($oldEvent->privacy == 1) ? $obm_q->escape($oldEvent->title) : $obm_q->escape($title);
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
      event_domain_id,
      event_sequence
    )
    VALUES (
      '$ext_id',
      NOW(),
      NOW(),
      $obm[uid],
      'VEVENT',
      '$GLOBALS[c_origin_web]',
      $oldOwner,
      '".Of_Date::getOption('timezone')."',
      '".$escape_title."',
      '".$obm_q->escape($exception_date)."',
      '".$obm_q->escape($description)."',
      '$properties',
      '".$obm_q->escape($location)."',
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
      '$obm[domain_id]',
      $sequence_number
    )";

    display_debug_msg($query, $cdg_sql, 'insert_new_exception');
    $obm_q->query($query);

    $exception_id = $obm_q->lastid();
    $exception_entity_id = of_entity_insert('event', $exception_id);
    $entities = get_calendar_event_entity($id);
    foreach ($entities as $entity => $data) {
      foreach ($data['entity'] as $entity_id => $entity_data) {
        # Ensure that both the new exception, the recurring event and the old
        # exceptions have the same participation state and organizer.
      	if($entity != 'document') {
          run_query_calendar_insert_entity_event($entity, $entity_id, $exception_id, $entity_data['state'], $entity_data['is_organizer']);
        } else {
          run_query_calendar_attach_document($entity_id, $exception_entity_id);
        }
      }
    }
  }
  else {
      $sequence_number = $oldEvent->sequence;
      $ext_id = $oldEvent->uid;
  }
  $sql_id = sql_parse_id($id);
  $sql_exception_id = ( $exception_id ? sql_parse_id($exception_id) : 'NULL' );
  if ($exception_id)
    run_query_calendar_touch_event($exception_id);

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
        $sql_exception_id,
        '$exception_recurrence_id_date'
      )";
  $sql_id = sql_parse_id($id, true);
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_exception_insert(2)');
  $obm_q->query($query);
  $query = "Update Event SET
    event_userupdate = $obm[uid],
    event_timeupdate = NOW(),
    event_sequence = $sequence_number
    WHERE event_ext_id = '$ext_id'";

  display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_exception_insert(3)');
  $obm_q->query($query);

  if ( $exception_id ) {
    $newEvent = OBM_EventFactory::getInstance()->getById($exception_id);
    $idsToIndex = array($id, $exception_id);
  } else {
    $newEvent = OBM_EventFactory::getInstance()->getById($id);
    $newEvent->negative_recurrence_id = $exception_recurrence_id_date;
    $newEvent->date_begin = $exception_recurrence_id_date;
    $newEvent->date_end = clone $exception_recurrence_id_date;
    $newEvent->date_end->addSecond($newEvent->duration);
    $newEvent->repeat_kind = "none";
    $idsToIndex = array($id);
  }
  
  // OBMFULL-3499
  // This is done here so that participation status is up-to-date BEFORE the email is sent
  // This allows the attached ICS to contain the correct PARTSTAT for the attendees
  $shouldUpdateAttendees = ($obm['uid'] == $oldOwner || OBM_Acl::canWrite($obm['uid'], 'calendar', $oldOwner))
	&& ($exception_date != $exception_recurrence_id_date || $location != $oldEvent->location || $event_duration != $oldEvent->duration);
  if ($shouldUpdateAttendees) {
	  run_query_quick_attendee_update($calendar, $exception_id);
  }

  if($sel_entity_id) {
  	run_query_update_link_event_exception($entities, $sel_entity_id, $exception_id, $exception_entity_id, $oldEvent);
    if (!is_numeric($calendar['organizer']) || $calendar['organizer'] <= 0) {
  		$organizerId = $obm['uid'];
  	} else {
  		$organizerId = $calendar['organizer'];
  	}
  	run_query_calendar_event_alert_insert($exception_id, $organizerId, $calendar['alert']);
  }

  if($run_observers) {
  	// Send mail session var
  	$GLOBALS['send_notification_mail'] = isset($calendar['attendees_notification'])
  		? $calendar['attendees_notification'] : $calendar['send_mail'];
  	// Notify Observers and ensure that email content points to the old event
  	// begin and end date.
    if ($isMovedException) {
        $oldEvent->set("date_begin",$exception_recurrence_id_date);
        $oldEvent->date_end = clone $oldEvent->date_begin;
        $oldEvent->date_end->addSecond($oldEvent->duration);
        OBM_EventFactory::getInstance()->store($newEvent,$oldEvent);
    }
    else {
        OBM_EventFactory::getInstance()->delete($newEvent);
    }
  }
  
  // SOLR
  foreach ( $idsToIndex as $id ) {
    run_query_calendar_solr_store($id);
  }
  if ( $date_occurrence != null){
    return array('already_set' => false, 'id' => $exception_id);
  }

  return $exception_id;
}

function run_query_update_link_event_exception($entities, $sel_entity_id, $exception_id, $exception_entity_id, $oldEvent) {
	global $cdg_sql, $obm;

	$groups = $sel_entity_id['group'];
	$users = $sel_entity_id['user'];
	if (is_array($groups)) {
		$user_group_array = array();
		foreach($groups as $group_id) {
			$user_group_array = array_merge($user_group_array, of_usergroup_get_group_users($group_id));
		}
		$users = array_merge($users, $user_group_array);
		$users = array_unique($users);
	}

	$old_users = get_calendar_event_users_info($exception_id);
	if (is_array($old_users)) {
		$new_users = array_diff($users, $old_users);
		$removed_users = array_diff($old_users, $users);
		if (count($removed_users) > 0) {
			run_query_calendar_deletedEvent_inserts($exception_id, $oldEvent, $removed_users);
			run_query_calendar_delete_event_links('user', $removed_users, $exception_id);
		}
	} else {
		$new_users = $users;
	}

	$resources = $sel_entity_id['resource'] ? array_unique($sel_entity_id['resource']) : array();
	if (is_array($entities['resource']['ids'])) {
		$new_resources = array_diff($resources, $entities['resource']['ids']);
		$removed_resources = array_diff($entities['resource']['ids'], $resources);
		if(count($removed_resources) > 0) {
			run_query_calendar_delete_event_links('resource', $removed_resources, $exception_id);
		}
	} else {
		$new_resources = $resources;
	}

	$contacts = $sel_entity_id['contact'] ? array_unique($sel_entity_id['contact']) : array();
	if (is_array($entities['contact']['ids'])) {
		$new_contacts = array_diff($contacts, $entities['contact']['ids']);
		$removed_contacts = array_diff($entities['contact']['ids'], $contacts);
		if(count($removed_contacts) > 0) {
			run_query_calendar_delete_event_links('contact', $removed_contacts, $exception_id);
		}
	} else {
		$new_contacts = $contacts;
	}

	run_query_calendar_add_event_links($new_users, $new_contacts, $new_resources, $exception_id);

	$documents = $sel_entity_id['document'] ? array_unique($sel_entity_id['document']) : array();
	$removed_documents = array();
	if (is_array($entities['document']['ids'])) {
		$new_documents = array_diff($documents, $entities['document']['ids']);
		$removed_documents = array_diff($entities['document']['ids'], $documents);
	} else {
		$new_documents = $documents;
	}

	foreach ($new_documents as $document_id) {
		run_query_calendar_attach_document($document_id, $exception_entity_id);
	}

	foreach ($removed_documents as $document_id) {
		run_query_calendar_detach_document($document_id, $exception_entity_id);
	}
}

function run_query_calendar_delete_event_links($type, $removed_entities, $event_id) {
	global $cdg_sql;

	$db = new DB_OBM;
	$query = "DELETE FROM EventLink WHERE eventlink_entity_id IN ".of_entity_subquery($type, $removed_entities)." AND eventlink_event_id = $event_id";
	$db->query($query);
}

function run_query_calendar_deletedEvent_inserts($event_id, $oldEvent, $removed_users) {
	global $cdg_sql;

	$db = new DB_OBM;
	foreach($removed_users as $user_id) {
		$query = "INSERT INTO DeletedEvent (
		          deletedevent_event_id,
		          deletedevent_event_ext_id,
		          deletedevent_user_id,
		          deletedevent_origin,
		          deletedevent_type,
		          deletedevent_timestamp)
		          VALUES (
		            $event_id,
		            '".$db->escape($oldEvent->uid)."',
		            $user_id,
		            '$db->escape($GLOBALS[c_origin_web])',
		            'VEVENT',
		            NOW())";
		display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_update(1)');
		$retour = $db->query($query);
	}
}

/**
 * Delete all exceptions for an event 
 * 
 * @param mixed $event_id 
 * @access public
 * @return void
 */
function run_query_delete_event_exceptions($event_id) {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;
  $event_id = sql_parse_id($event_id);
  $query = "SELECT eventexception_child_id
            FROM EventException
            WHERE eventexception_parent_id = $event_id"; 
  display_debug_msg($query, $cdg_sql, 'run_query_delete_event_exceptions(0)');
  $obm_q->query($query);

  if ($obm_q->num_rows()>0) {
    $childs = array();
    while ($obm_q->next_record()) {
      $child_id = $obm_q->f('eventexception_child_id');
      // SOLR
      OBM_IndexingService::delete('event', $child_id);
      if (!empty($child_id)) {
        $childs[] = $child_id;
        of_entity_delete('event', $child_id);
      }
    }

    if (!empty($childs)) {
      $query = "DELETE FROM Event WHERE event_id IN (".implode(',',$childs).")";
      display_debug_msg($query, $cdg_sql, 'run_query_delete_event_exceptions(1)');
      $obm_q->query($query);
    }

    $query = "DELETE FROM EventException WHERE eventexception_parent_id = $event_id";
    display_debug_msg($query, $cdg_sql, 'run_query_delete_event_exceptions(2)');
    $obm_q->query($query);
	return $childs;
  }
}


/**
 * Delete one exception of an event 
 * 
 * @param mixed $event_id
 * @param mixed $eventexception_date
 * @param mixed $child_id
 * @access public
 * @return void
 */
function run_query_delete_one_event_exception($event_id, $eventexception_date, $child_id=null) {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;
  $event_id = sql_parse_id($event_id);
  if ($child_id) {
    $child_id = sql_parse_id($child_id);
    of_entity_delete('event', child_id);
    $query = "DELETE FROM Event WHERE event_id = $child_id";
    // SOLR
    OBM_IndexingService::delete('event', $child_id);
    display_debug_msg($query, $cdg_sql, 'run_query_delete_one_event_exception(1)');
    $obm_q->query($query);
  }
  $query = "DELETE FROM EventException "
          ."WHERE eventexception_parent_id = $event_id "
          ."AND eventexception_date = '$eventexception_date'";
  display_debug_msg($query, $cdg_sql, 'run_query_delete_one_event_exception(2)');
  $obm_q->query($query);
}

function run_query_reinitialize_recurrent_event($event_id) {
	$events_exception_to_delete = run_query_get_one_event_exception($event_id);

	while ($events_exception_to_delete->next_record()) {
		$child_id = $events_exception_to_delete->f('eventexception_child_id');
		$exception_date = $events_exception_to_delete->f('eventexception_date');
		if($child_id) {
			$exception_to_delete= OBM_EventFactory::getInstance()->getById($child_id);
			$exception_to_delete->getIcs($GLOBALS['obm']['uid'],"cancel",false);
			run_query_delete_one_event_exception($event_id, $exception_date, $child_id);
			OBM_EventFactory::getInstance()->delete($exception_to_delete);
		} else {
			run_query_delete_one_event_exception($event_id, $exception_date);
		}
	}
}


/**
 * Update attendee_participation 
 * 
 * @param mixed $params 
 * @param mixed $eve_q 
 * @access public
 * @return void
 */
function run_query_quick_attendee_update($params, $event_id) {
  global $obm, $cgp_host;
  $entities = get_calendar_event_entity($event_id);
  $users = isset($entities['user']) ? $entities['user']['ids'] : array();
  $contacts = isset($entities['contact']) ? $entities['contact']['ids'] : array();
  $resources = isset($entities['resource']) ? $entities['resource']['ids'] : array();
  run_query_calendar_reset_occurrence_links($users, $contacts, $resources, $event_id);
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
 * Create Comment for an event 
 * 
 * @param $event_id 
 * @param $user_id 
 * @param string $comment
 * @access public
 * @return void
 */
function run_query_calendar_event_comment_insert($event_id, $ent_id, $comment, $type, $notify = false ) {
	global $cdg_sql, $obm;
	$event_id = sql_parse_id($event_id);
	$ent_id = sql_parse_id($ent_id);
	if($type == 'user'){
		$entity_id = get_user_entity_id_for_comment_insert($ent_id);
	}else{
		$entity_id = get_resource_entity_id_for_comment_insert($ent_id);
	}
	$oldEvent = OBM_EventFactory::getInstance()->getById($event_id);
	$obm_q = new DB_OBM;
	$query = "
	UPDATE EventLink 
	SET eventlink_comment ='".$obm_q->escape($comment)."'
	WHERE eventlink_event_id = $event_id
	AND eventlink_entity_id = $entity_id
	;";
	if ( $obm_q->query($query) ){
		if($notify == true) send_mail_when_comment($event_id,$oldEvent);
		return true;
	}else{
		return false;
	}
}
function get_user_entity_id_for_comment_insert($user_id){
    global $cdg_sql, $obm;
    $obm_q = new DB_OBM;
    $query = "SELECT userentity_entity_id from UserEntity where userentity_user_id = $user_id;";
    if ( $obm_q->query($query) ){
        $obm_q->next_record();
        return $obm_q->f('userentity_entity_id');
    }else{
        return false;
    }
}
function get_resource_entity_id_for_comment_insert($res_id){
    global $cdg_sql, $obm;
    $obm_q = new DB_OBM;
    $query = "SELECT resourceentity_entity_id  from ResourceEntity where resourceentity_resource_id = $res_id;";
    if ( $obm_q->query($query) ){
        $obm_q->next_record();
        return $obm_q->f('resourceentity_entity_id');
    }else{
        return false;
    }
}
function send_mail_when_comment($event_id, $oldEvent){
  $GLOBALS["send_notification_mail"] = true;
  $newEvent = OBM_EventFactory::getInstance()->getById($event_id);
  //notify attendee
  OBM_EventFactory::getInstance()->store($newEvent, $oldEvent);
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_event_comment_insert(0)');
}
/**
 * Insert an event template
 * 
 * @param $calendar array of calendar params
 * @access public
 * @return integer event template id inserted
 */
function run_query_calendar_create_or_update_event_template(&$calendar) {
  global $cdg_sql, $obm;
  $obm_q = new DB_OBM;

  // Insert "others attendees" as private contacts
  if ($calendar['others_attendees'] != "") {
    $others_attendees = run_query_insert_others_attendees($calendar);
    $calendar['sel_contact_id'] = is_array($calendar['sel_contact_id']) 
                              ? array_merge($calendar['sel_contact_id'], $others_attendees)
                              : $others_attendees;
    $calendar['sel_contact_id'] = array_unique($calendar['sel_contact_id']);
  }
  // Insert "other files" as private documents
  if (is_array($calendar['other_files'])) {
    $other_files = run_query_insert_other_files($calendar);
    $calendar['sel_document_id'] = is_array($calendar['sel_document_id'])
                               ? array_merge($calendar['sel_document_id'], $other_files)
                               : $other_files;
  }

  if (!is_numeric($calendar['organizer']) || $calendar['organizer'] <= 0) {
    $owner = $obm['uid'];
  } else {
    $owner = $calendar['organizer'];
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
  $event_duration = event_duration($datebegin, $calendar['event_duration'], $calendar['all_day'] == 1);
  $privacy = $calendar['privacy'];
  $repeat_kind = $calendar['repeat_kind'];
  $repeat_days = $calendar['repeat_days'];
  $repeat_end = isset($calendar['repeat_end']) ? "'$calendar[repeat_end]'" : 'NULL';
  $repeatfrequency = $calendar['repeatfrequency'];
  $date_exception = $calendar['date_exception'];
  $allow_documents = ($calendar['allow_documents'] != 1) ? 0 : 1;
  $force_insertion = $calendar['force'] ? 'true' : 'false';
  $opacity = $calendar['opacity'];
  $show_user_calendar = $calendar['show_user_calendar'] ? 'true' : 'false';
  $show_resource_calendar = $calendar['show_resource_calendar'] ? 'true' : 'false';
  $attendees_notification = $calendar['attendees_notification'] ? 'true' : 'false';
  $allResources = run_query_calendar_merge_resource_groups($calendar['sel_resource_group_id'], $calendar['sel_resource_id']);
  
  $user_ids = is_array($calendar['sel_user_id']) ? "'".implode(',', $calendar['sel_user_id'])."'" : 'NULL';
  $contact_ids = is_array($calendar['sel_contact_id']) ? "'".implode(',', $calendar['sel_contact_id'])."'" : 'NULL';
  $resource_ids = is_array($allResources) ? "'" . implode(',', $allResources) . "'" : 'NULL';
  $group_ids = is_array($calendar['sel_group_id']) ? "'".implode(',', $calendar['sel_group_id'])."'" : 'NULL';
  $document_ids = is_array($calendar['sel_document_id']) ? "'".implode(',', $calendar['sel_document_id'])."'" : 'NULL';
  $organizer = $calendar['organizer'] ? $calendar['organizer'] : "0";

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
  
  if (isset($calendar['template_id'])) {
    $template_id = (int) $calendar['template_id'];
    $query = "UPDATE EventTemplate SET
      eventtemplate_timeupdate = NOW(),
      eventtemplate_owner = '$owner',
      eventtemplate_title = '".$obm_q->escape($title)."', 
      eventtemplate_date = '".$obm_q->escape($datebegin)."',     
      eventtemplate_description = '".$obm_q->escape($description)."',
      eventtemplate_properties = '".$obm_q->escape($properties)."',
      eventtemplate_location = '".$obm_q->escape($location)."',
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
      eventtemplate_document_ids = $document_ids,
      eventtemplate_force_insertion = '$force_insertion',
      eventtemplate_opacity = '$opacity',
      eventtemplate_show_user_calendar = '$show_user_calendar',
      eventtemplate_show_resource_calendar = '$show_resource_calendar',
      eventtemplate_notify = $attendees_notification
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
      eventtemplate_document_ids,
      eventtemplate_organizer,
      eventtemplate_force_insertion,
      eventtemplate_opacity,
      eventtemplate_show_user_calendar,
      eventtemplate_show_resource_calendar,
      eventtemplate_notify)
      VALUES (
      '".$obm_q->escape($name)."',
      NOW(),
      NOW(),
      '$obm[uid]',
      '$owner',
      '".$obm_q->escape($title)."',
      '".$obm_q->escape($datebegin)."',
      '".$obm_q->escape($description)."',
      '".$obm_q->escape($properties)."',
      '".$obm_q->escape($location)."',
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
      $document_ids,
      $organizer,
      '$force_insertion',
      '$opacity',
      '$show_user_calendar',
      '$show_resource_calendar',
      $attendees_notification)";
  }
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_create_or_update_event_template()');
  $obm_q->query($query);
  if (isset($calendar['template_id'])) {
    $id = $calendar['template_id'];
  } else {
    $id = $obm_q->lastid();
  }
  
  return $id;
}

/**
 * Checks if an event template exists or not
 *
 * @param   <string>  $templateName template name to check for existence
 * @return  <bool>    True if exists, false otherwise
 */
function run_query_check_exists_template_by_name($templateName){
  global $cdg_sql;
  $obm_q = new DB_OBM;

  $query = "SELECT eventtemplate_id 
            FROM EventTemplate
            WHERE eventtemplate_name = '".$obm_q->escape($templateName)."'";
  display_debug_msg($query, $cdg_sql, 'run_query_check_exists_template_by_name()');
  
  $obm_q->query($query);
  return ($obm_q->num_rows() > 0);
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
  
  $query = "SELECT eventtemplate_id, eventtemplate_name, eventtemplate_title FROM EventTemplate 
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
  global $obm, $cdg_sql;
  
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
    eventtemplate_date AS event_date,
    eventtemplate_duration AS event_duration,
    eventtemplate_repeatdays AS event_repeatdays,
    eventtemplate_allday AS event_allday,
    eventtemplate_tag_id AS event_tag_id,
    eventtag_color,
    eventtag_label,
    eventtemplate_endrepeat AS event_endrepeat,
    eventtemplate_allow_documents AS event_allow_documents,
    eventtemplate_alert AS event_alert,
    eventtemplate_user_ids AS event_user_ids,
    eventtemplate_contact_ids AS event_contact_ids,
    eventtemplate_resource_ids AS event_resource_ids,
    eventtemplate_group_ids AS event_group_ids,
    eventtemplate_organizer AS event_organizer,
    eventtemplate_document_ids AS event_document_ids,
    eventtemplate_force_insertion AS event_force_insertion,
    eventtemplate_opacity AS event_opacity,
    eventtemplate_show_user_calendar AS event_show_user_calendar,
    eventtemplate_show_resource_calendar AS event_show_resource_calendar,
    eventtemplate_notify AS event_attendees_notification
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
  
  $entities = check_template_entities_acl($entities);
  
  return array($obm_q, $entities);
}

/**
 * Checks if the current user has the necessary rights on a template's entities
 * 
 * @param $entities
 * @access public
 * @return array
 */
function check_template_entities_acl($entities) {
  global $obm, $cdg_sql;
  
  $acl_ok = true;
  
  if (count($entities['resource']) > 0) {
    $allowed_resources = OBM_Acl::getAllowedEntities($obm['uid'], 'resource', 'access', $entities['resource']);
    if (count($allowed_resources) < count($entities['resource'])) {
      $entities['resource'] = array_keys($allowed_resources);
      $acl_ok = false;
    }
  }
  
  if (count($entities['user']) > 0) {
    $allowed_users = OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'access', $entities['user']);
    if (count($allowed_users) < count($entities['user'])) {
      $entities['user'] = array_keys($allowed_users);
      $acl_ok = false;
    }
  }
  
  if (count($entities['group']) > 0) {
    $allowed_groups = array();
    $group_q = new DB_OBM;
    display_debug_msg($query, $cdg_sql, 'check_template_entities_acl()');
    $group_q->query("SELECT group_id FROM UGroup WHERE group_id IN (".implode(',', $entities['group']).") AND group_privacy = 0");
    while ($group_q->next_record()) {
      $allowed_groups[] = $group_q->f('group_id');
    }
    if (count($allowed_groups) < count($entities['group'])) {
      $entities['group'] = array_keys($allowed_groups);
      $acl_ok = false;
    }
  }
  
  if (count($entities['document']) > 0) {
    $allowed_docs = array();
    $doc_q = new DB_OBM;
    display_debug_msg($query, $cdg_sql, 'check_template_entities_acl()');
    $doc_q->query("SELECT document_id FROM Document WHERE document_id IN (".implode(',', $entities['document']).") AND (document_privacy = 0 OR document_usercreate = $obm[uid])");
    while ($doc_q->next_record()) {
      $allowed_docs[] = $doc_q->f('document_id');
    }
    if (count($allowed_docs) < count($entities['document'])) {
      $entities['document'] = array_keys($allowed_docs);
      $acl_ok = false;
    }
  }
  
  if (count($entities['contact']) > 0) {
    $allowed_contacts = array();
    $books = OBM_AddressBook::search();
    $books_ids = array();
    foreach ($books as $b) $books_ids[] = $b->id;
    $contact_q = new DB_OBM;
    display_debug_msg($query, $cdg_sql, 'check_template_entities_acl()');
    $contact_q->query("SELECT contact_id FROM Contact WHERE contact_id IN (".implode(',', $entities['contact']).") AND contact_addressbook_id IN (".implode(',', $books_ids).")");
    while ($contact_q->next_record()) {
      $allowed_contacts[] = $contact_q->f('contact_id');
    }
    if (count($allowed_contacts) < count($entities['contact'])) {
      $entities['contact'] = array_keys($allowed_contacts);
      $acl_ok = false;
    }
  }
  
  if (!$acl_ok) {
    $GLOBALS['display']['msg'] .= display_warn_msg($GLOBALS['l_template_entities_access_warning']);
  }
  return $entities;
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
    eventtemplate_document_ids,
    eventtemplate_organizer,
    eventtemplate_notify)
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
    eventtemplate_document_ids,
    eventtemplate_organizer,
    eventtemplate_notify
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
  $obm_q = new DB_OBM;

  $template_id = (int) $template_id;
  $query = "UPDATE EventTemplate SET eventtemplate_name = '".$obm_q->escape($new_name)."' 
    WHERE eventtemplate_id = $template_id AND eventtemplate_usercreate = $obm[uid]";
  return $obm_q->query($query);
}

/**
 * Import event templates from a XML file
 * 
 * @param $xml SimpleXMLElement object
 * @access public
 * @return boolean
 */
function run_query_calendar_import_template($xml) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM ;

  if ($xml->getName() == 'event_templates') {
    foreach ($xml->event_template as $template) {
      if (!run_query_calendar_import_template($template)) {
        return false;
      }
    }
    return true;
  }
  
  if (!$xml instanceof SimpleXMLElement || $xml->getName() != 'event_template') {
    return false;
  }
  
  $columns = array('name','title','owner','date','description','location',
    'category1_id','priority','privacy','duration','repeatkind','repeatfrequence',
    'repeatdays','allday','tag_id','allow_documents','alert','endrepeat','user_ids',
    'contact_ids','resource_ids','group_ids','document_ids', 'organizer');
    
  foreach ($columns as $column) {
    $value = (string) $xml->{$column};
    $$column = ($value == '') ? 'NULL' : "'".$obm_q->escape($value)."'";
  }
  
  if (!is_null($xml->properties->extended_desc)) {
    $properties = $xml->properties->extended_desc->asXML();
  }
  
  $entities = array();
  foreach (array('user', 'group', 'resource', 'contact', 'document') as $type) {
    $varname = $type.'_ids';
    $value = (string) $xml->{$varname};
    $entities[$type] = ($value == '') ? array() : explode(',', $value);
  }
  check_template_entities_acl($entities);
  
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
      eventtemplate_document_ids,
      eventtemplate_organizer)
      VALUES (
      $name,
      NOW(),
      NOW(),
      $obm[uid],
      $owner,
      $title,
      $date,
      $description,
      '$properties',
      $location,
      $category1_id,
      $priority,
      $privacy,
      $duration,
      $repeatkind,
      $repeatfrequence,
      $repeatdays,
      $allday,
      $tag_id,
      $allow_documents,
      $alert,
      $endrepeat,
      $obm[domain_id],
      $user_ids,
      $contact_ids,
      $resource_ids,
      $group_ids,
      $document_ids,
      $organizer)";
  
  display_debug_msg($query, $cdg_sql,"run_query_calendar_import_template()");
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
  if(!$e) {
    $err['msg'] = $l_err_reference;    
    return false;
  }
  if ($e['owner'] == $obm['uid']) {
    return true;
  }
  if ($right == 'write') {
    $access = OBM_Acl::canWrite($obm['uid'], 'calendar', $e['owner']);
  } else {
    if (in_array($obm['uid'], $e['attendees']['user'])) {
      return true;
    }
    if ($e['privacy'] != 2) {
      if(is_array($e['attendees']['user']) && count($e['attendees']['user']) > 0) {
        $access = OBM_Acl::areSomeAllowed($obm['uid'], 'calendar', $e['attendees']['user'], $right);
      }
      if (!$access && is_array($e['attendees']['resource'])) {
        $access = OBM_Acl::areSomeAllowed($obm['uid'], 'resource', $e['attendees']['resource'], $right);
      }
    }
  }
  if (!$access) { 
    $err['msg'] = $l_insufficient_permission;
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the event can be accessed using the token
// Parameters:
//   - $id : event id
// Returns:
//   true if the event can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_calendar_access_by_token($id, $right = 'read') {
  global $l_event_cant_delete, $obm, $delete_ok, $err, $l_insufficient_permission, $l_err_reference;

  $uid = get_user_id_by_token();

  if (($right == 'write') || ($right == 'admin')) {
    $err['msg'] = $l_insufficient_permission;
    return false;
  }
  if ((!$id) || (!$uid)) {
    $err['msg'] = $l_err_reference;
    return false;
  }
  $e = get_calendar_event_info($id);
  if(!$e) {
    $err['msg'] = $l_err_reference;    
    return false;
  }
  if ($e['owner'] == $uid) {
    return true;
  }
  if(is_array($e['attendees']['user']) && in_array($uid, $e['attendees']['user'])) {
    return true;
  }
  $err['msg'] = $l_insufficient_permission;
  return false;
}

function get_user_id_by_token() {
  $uid = $GLOBALS['token']['uid'];
  if ((!$uid) && ($GLOBALS['token']['entity']=='user')) {
    $uid = $GLOBALS['token']['entityId'];
  }
  return $uid;
}

///////////////////////////////////////////////////////////////////////////////
// Delete all events of a evenements, and the event
// Parameters: 
//   - $calendar : calendar infos hash
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_delete($calendar,$force_sendmail = true) {
  global $cdg_sql, $c_use_connectors, $ccalendar_resource_admin;

  $obm_q = new DB_OBM;
  $event_id =  $calendar['calendar_id'];

  $users_array = get_calendar_event_users_info($event_id);
  $obm_q = run_query_calendar_detail($event_id);

  $title = $obm_q->f('event_title');
  $is_exception = $obm_q->f('event_is_exception');

  $event = OBM_EventFactory::getInstance()->getById($event_id);

  // First increment the sequence then
  // prepare the ICS file bc we can't create it once the event is deleted from the database...
  run_query_increment_sequence($event->uid);
  $icsFile = $event->getIcs($GLOBALS['obm']['uid'],"cancel",false);

  // If connectors in use
  if ($c_use_connectors) {

    // if deleted event is an exception, we touch parent event timeupdate (for connectors)
    if ($is_exception) {
      $parent_event_id = $obm_q->f('event_eventexception_parent_id');
      run_query_calendar_touch_event($parent_event_id);

    } else {
      $query = "SELECT userentity_user_id FROM EventLink INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id WHERE eventlink_event_id = $event_id";
      $obm_q->query($query);
      $obm_qm = new DB_OBM;
      $users = array();
      while ($obm_q->next_record()) {
        // Avoid duplicates
        $query = "DELETE FROM DeletedEvent ".
          "WHERE deletedevent_event_ext_id='".$obm_q->escape($event->uid)."' AND ".
          "deletedevent_user_id ".sql_parse_id($obm_q->f('userentity_user_id'), true);
        display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete(2)');
        $obm_qm->query($query);

        // We note the event deletion
        $query = "INSERT INTO DeletedEvent (deletedevent_event_id, deletedevent_event_ext_id, deletedevent_user_id, deletedevent_origin, deletedevent_type, deletedevent_timestamp)
          VALUES ($event_id, '".$event->uid."', ".$obm_q->f('userentity_user_id').",'$GLOBALS[c_origin_web]', 'VEVENT', NOW())";
        display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete(3)');
        $retour = $obm_qm->query($query);
      }
    }
  }
  $deleted_evt_ids = array($event_id);
  
  // Delete documents
  run_query_calendar_delete_document($event_id);

  // Send mail
  if (!($is_exception || $event->repeat_kind == 'none')) 
  {
    $deleted_events_exceptions = run_query_delete_event_exceptions($event_id);
    if ( !is_array($deleted_events_exceptions) ) {
      $deleted_events_exceptions = array();
    }
    $deleted_evt_ids = array_merge($deleted_evt_ids, $deleted_events_exceptions);
  }
  of_entity_delete('event', $event_id);
  $query = "DELETE FROM Event WHERE event_id = $event_id";
  $obm_q->query($query);
  
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_delete(5)');
  // Send mail session var
  $GLOBALS['send_notification_mail'] = ($force_sendmail || (isset($calendar['send_mail']) && $calendar['send_mail']));
  // Notify Observers
  OBM_EventFactory::getInstance()->delete($event);
  return $deleted_evt_ids;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable calendar group for the current user
///////////////////////////////////////////////////////////////////////////////
function run_query_get_allowed_groups($ids=null) {
  global $cdg_sql, $obm, $ccalendar_public_groups;

  $where = sql_obm_entity_privacy('group');
  if (! $ccalendar_public_groups) {
    $where .= " AND group_privacy=1";
  }
  if($id !== null) {
    $where .=  "AND group_id IN (".implode(',', $ids).")";
  }
  $multidomain = sql_multidomain('group');
  $query = "
    SELECT group_id, group_privacy, group_name
    FROM UGroup
    WHERE $where  $multidomain
    ORDER BY group_privacy DESC, group_name";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, 'run_query_get_allowed_groups');
  $obm_q->query($query);
  $entities = array('ids' => array(), 'entity' => array());
  while($obm_q->next_record()) {
    $entities['ids'][] = $obm_q->f('group_id');
    $entities['entity'][$obm_q->f('group_id')] = array('id' => $obm_q->f('group_id'),'label' => $obm_q->f('group_name'), 'privacy' => $obm_q->f('group_privacy'));
  }
  return $entities;
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
//  - $entities_array : hashed array of userobm and resource DBO and contact
///////////////////////////////////////////////////////////////////////////////
function store_calendar_entities($entities_q) {
  global $ico_calendar_user0,$ico_calendar_user1,$ico_calendar_user2;
  global $ico_calendar_user3,$ico_calendar_user4,$ico_calendar_user5;
  global $display, $obm, $ccalendar_max_users;

  $i = 0;
  $j = 0;
  $entities = array();
  $users = $entities_q['user'];
  if (is_array($users)) {
    foreach ($users as $id => $data) {
      if($j > $ccalendar_max_users) return $entities;
      if ($obm['uid'] == $id) {
        $entities['user'][$id] = array(
          'name'  => $data,
          'class' => 'eventOwner',
          'type'  => 'user',
          'id'    => $id);
      } else {
        $entities['user'][$id] = array(
          'name'  => $data,
          'class' => "eventOwner$i",
          'type'  => 'user',
          'id'    => $id);
        $i++;
        $j++;
        $i = $i%20;
      }
    }
  }
  $resources = $entities_q['resource'];
  if (is_array($resources)) {
    foreach ($resources as $id => $data) {
      if($j > $ccalendar_max_users) return $entities;
      $entities['resource'][$id] = array(
        'name'  => $data,
        'class' => "eventOwner$i",
        'image' => ${'ico_calendar_user'.$i},
        'type'  => 'resource',
        'id'    => $id
      );
      $i++;
      $j++;
      $i = $i%20;
    }
  }
  $resourcegroup_q = $entities_q['resourcegroup'];
  if (is_object($resourcegroup_q)) {
    while($resourcegroup_q->next_record()) {
      $id = $resourcegroup_q->f('resource_id');
      $entities['resourcegroup'][$id] = array(
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
  $contacts = $entities_q['contact'];
  if (is_array($contacts)) {
    foreach ($contacts as $id => $data) {
      if($j > $ccalendar_max_users) return $entities;
      $entities['contact'][$id] = array(
        'name'  => $data,
        'class' => "eventOwner",
        'image' => ${'ico_calendar_user'.$i},
        'type'  => 'contact',
        'id'    => $id
      );
      $i++;
      $j++;
      $i = $i%20;
    }
  }

  return $entities;
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
    WHERE event_id ".sql_parse_id($id, true)."
    $multidomain";
  display_debug_msg($query, $cdg_sql, 'get_calendar_event_info()');
  $obm_q->query($query);
  $e = array('attendees' => array('user' => array()));
  if(!$obm_q->next_record()) {
    return false;
  }
  $e['privacy'] = $obm_q->f('event_privacy');
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

  $multidomain = sql_multidomain('userobm');
  $query = "SELECT DISTINCT
    userobm_id,
    userobm_lastname,
    userobm_firstname,
    'user' as eventlink_entity,
    userentity_user_id as eventlink_entity_id,
    eventlink_state as state,
    eventlink_comment as comment,
    eventlink_is_organizer as is_organizer
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
    $comment = $u_q->f('comment');
    $state = $u_q->f('state');
    $is_organizer = ($u_q->f('is_organizer') == 1);
    $label = $u_q->f('userobm_lastname'). ' ' .$u_q->f('userobm_firstname');
    $entities[$entity]['ids'][] = $entity_id;
    $entities[$entity]['entity'][$entity_id] = array('label'=> $label,
      'state' => $state, 'is_organizer' => $is_organizer, 'comment' => $comment);
  }

  $multidomain = sql_multidomain('resource');
  $query = "SELECT DISTINCT
    resource_id,
    resource_name,
    'resource' as eventlink_entity,
    resourceentity_resource_id as eventlink_entity_id,
    eventlink_state as state,
    eventlink_comment as comment
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
    $comment = $r_q->f('comment');
    $label = $r_q->f('resource_name');
    $entities[$entity]['ids'][] = $entity_id;
    $entities[$entity]['entity'][$entity_id] = array('label'=> $label,
      'state' => $state, 'comment' => $comment);
  }

  $multidomain = sql_multidomain('contact');
  $query = "SELECT DISTINCT
    contact_id,
    contact_lastname,
    contact_firstname,
    'contact' as eventlink_entity,
    contactentity_contact_id as eventlink_entity_id,
    eventlink_state as state,
    eventlink_comment as comment,
    eventlink_is_organizer as is_organizer
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
    $comment = $u_q->f('comment');
    $is_organizer = ($u_q->f('is_organizer') == 1);
    $label = $u_q->f('contact_lastname'). ' ' .$u_q->f('contact_firstname');
    $entities[$entity]['ids'][] = $entity_id;
    $entities[$entity]['entity'][$entity_id] = array('label'=> $label,
      'state' => $state, 'is_organizer' => $is_organizer,'comment' => $comment);
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
// Return ids of documents attached to an event
// Parameters: 
//   - $calendar_id
///////////////////////////////////////////////////////////////////////////////
function get_calendar_event_document_ids($event_id) {
  global $cdg_sql;

  $event_id = sql_parse_id($event_id, true);
  
  $query = "SELECT 
    documentlink_document_id AS document_id
    FROM DocumentLink
    INNER JOIN EventEntity ON documentlink_entity_id = evententity_entity_id
    WHERE 
    evententity_event_id $event_id";

  display_debug_msg($query, $cdg_sql, 'get_calendar_event_document_ids()');
  $u_q = new DB_OBM;
  $u_q->query($query);
  $ids = array();
  while ($u_q->next_record()) {
    $ids[] = $u_q->f('document_id');
  }
  return $ids;
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

  $datebegin = $calendar['date_begin'];
  $duration = $calendar['duration'];
  $all_day = $calendar['all_day'];

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
  global $l_err_transparent_empty_resources;

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
  $opacity = $calendar['opacity'];
  $resources = $calendar['resource_id'];
  
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

  if($opacity == 'TRANSPARENT' && !empty($resources)){
    $err['msg'] = $l_err_transparent_empty_resources;
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
// Return all not rejected events in a week of users or/and groups
// Parameters:
//   - $start           : timestamp of start date
//   - $end             : timestamp of end date
//   - $calendar_entity : array of entities elements to display
//   - $status          : array of event status to get ('ACCEPTED', 'R', 'W')
//   - $category_filter : category
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_no_repeat_events($start,$end, $calendar_entity, $status, $category_filter='',$events=null, $opac=null) {
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

  if ($events !== null) {
    if (isset($events) && sizeof($events) > 0) {
      $search_pattern = "AND event_id IN(".implode(',', $events).")";
    } else {
      $search_pattern = "AND 0=1";
    }
  }

  if ($opac != null) $where_opacity = " AND event_opacity='$opac' ";

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
    eventlink_comment,
    eventcategory1_label,
    event_date,
    event_duration,
    eventtag_color AS event_color,
    eventtag_label as tag,
    eventtag_id,
    event_allday,
    event_sequence,";

  $left_q = "
    LEFT JOIN EventCategory1 ON event_category1_id = eventcategory1_id
    INNER JOIN UserObm ON event_owner = userobm_id
    LEFT JOIN EventTag ON event_tag_id = eventtag_id 
    INNER JOIN EventLink ON event_id = eventlink_event_id";


  if (is_array($calendar_entity['user'])
    && (count($calendar_entity['user']) > 0) ) {
      $select[] =  "SELECT
        $select_q
        userentity_user_id as eventlink_entity_id,
        'user' as eventlink_entity
        FROM Event
        $left_q 
        INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id
        WHERE event_type='VEVENT'
        $where_opacity
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
      $select[] =  "SELECT
        $select_q
        resourceentity_resource_id as eventlink_entity_id,
        'resource' as eventlink_entity
        FROM Event
        $left_q 
        INNER JOIN ResourceEntity ON resourceentity_entity_id = eventlink_entity_id
        WHERE event_type='VEVENT'
        $where_opacity
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
      $select[] = "SELECT
        $select_q
        resourceentity_resource_id as eventlink_entity_id,
        'resourcegroup' as eventlink_entity
        FROM Event
        $left_q 
        INNER JOIN ResourceEntity ON resourceentity_entity_id = eventlink_entity_id
        WHERE event_type='VEVENT'
        $where_opacity
        $where_status
        $filter
        AND event_repeatkind = 'none'
        $event_date_start
        $event_date_end
        $search_pattern
        AND resourceentity_resource_id IN (".implode(',', array_keys($calendar_entity['resourcegroup'])).")
        $multidomain";
    }
  if($select != null) {
    $query = implode(' UNION ', $select)." ORDER BY event_date, eventlink_entity, eventlink_entity_id";
    display_debug_msg($query, $cdg_sql, 'run_query_calendar_no_repeat_events()');
    $obm_q->xquery($query);
    return $obm_q;
  }
  return null;
}

/*
* select the eventexception row & the parent event row of given exception ids
*
* @param array $ids list of child ( = exceptions events ) ids . Eg: array(234,567)
* @return SQL xquery resultCursor
*/
function run_query_calendar_get_parent_event($ids) {
  $selected_ids = implode(",",$ids);
  $obm_q = new DB_OBM;
  $query = "SELECT
    event_duration,
    event_timezone,
    eventexception_parent_id,
    eventexception_child_id,
    eventexception_date
    FROM EventException
    INNER JOIN Event ON eventexception_parent_id = event_id
    WHERE eventexception_child_id IN ($selected_ids) ";
    display_debug_msg($query, $cdg_sql, 'run_query_calendar_get_parent_event()');
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
function run_query_calendar_repeat_events($start, $end, $calendar_entity, $status, $category_filter='', $events=null, $opac=null, $no_birthday=false) {
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

  if ($opac != null) $where_opacity = " AND event_opacity='$opac' ";

  $first = true;
  if (is_array($status)
    && (count($status) > 0) ) {
      $where_status = "AND eventlink_state IN ('".implode("','",$status)."')";
    }

  if ($start) $event_date_start = "AND (event_endrepeat >= #SUBSECONDS('$start',event_duration)
        OR event_endrepeat IS NULL)";
  if ($end) $event_date_end = "AND event_date <= '$end'";

  if ($events !== null) {
    if (isset($events) && sizeof($events) > 0) {
      $search_pattern = "AND event_id IN(".implode(',', $events).")";
    } else {
      $search_pattern = "AND 0=1";
    }
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
    eventlink_comment,
    event_repeatdays,
    eventtag_color AS event_color,
    eventtag_label as tag,
    eventtag_id,
    event_allday,
    event_sequence,";

  $left_q = "
    LEFT JOIN EventCategory1 ON event_category1_id = eventcategory1_id
    LEFT JOIN EventTag ON event_tag_id = eventtag_id 
    INNER JOIN UserObm ON event_owner = userobm_id
    INNER JOIN EventLink ON event_id = eventlink_event_id";

  if ($no_birthday) {
    $left_q .= "
      LEFT JOIN Contact birthday ON birthday.contact_birthday_id = event_id
      LEFT JOIN Contact anniversary ON anniversary.contact_anniversary_id = event_id";
    $where_no_birthday = " AND birthday.contact_birthday_id IS NULL AND anniversary.contact_anniversary_id IS NULL ";
  }

  if (is_array($calendar_entity['user'])
    && (count($calendar_entity['user']) > 0) ) {
      $select[] =  "SELECT
        $select_q
        'user' as eventlink_entity,
        userentity_user_id as eventlink_entity_id
        FROM Event
        $left_q
        INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id
        WHERE event_type='VEVENT'
        $where_no_birthday
        $where_opacity
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
      $select[] = "SELECT
        $select_q
        'resource' as eventlink_entity,
        resourceentity_resource_id as eventlink_entity_id
        FROM Event
        $left_q
        INNER JOIN ResourceEntity ON resourceentity_entity_id = eventlink_entity_id
        WHERE event_type='VEVENT'
        $where_no_birthday
        $where_opacity
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
      $select[] = "SELECT
        $select_q
        'resourcegroup' as eventlink_entity,
        resourceentity_resource_id as eventlink_entity_id
        FROM Event
        $left_q
        INNER JOIN ResourceEntity ON resourceentity_entity_id = eventlink_entity_id
        WHERE event_type='VEVENT' 
        $where_no_birthday
        $where_opacity
        AND event_repeatkind != 'none'
        $where_status
        $filter
        $event_date_start
        $event_date_end
        $search_pattern
        AND resourceentity_resource_id IN (".implode(',', array_keys($calendar_entity['resourcegroup'])).")
        $multidomain";
    }
  if($select!= null){
    $query = implode(' UNION ', $select)." ORDER BY event_date, eventlink_entity, eventlink_entity_id"; 
    display_debug_msg($query, $cdg_sql, 'run_query_calendar_repeat_events()');
    $obm_q->xquery($query);
    return $obm_q;
  }
  return null;
}


/**
 * Get all attendees of an array of events
 *  
 * @param array $events 
 * @access public
 * @return void
 */
function run_query_get_events_attendee($events, $with_resources = true, $with_contacts = true) {
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
    eventlink_is_organizer,
    eventlink_comment,
    ".DB_OBM::CONCAT(array("userobm_lastname", "' '", "userobm_firstname"))." as eventlink_label
    FROM Event
    INNER JOIN EventLink ON eventlink_event_id = event_id
    INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id
    INNER JOIN UserObm ON userentity_user_id = userobm_id
    WHERE event_id IN ($event_list) $multidomain";
  if($with_resources)
    $query .= "
    UNION
    SELECT
    event_id,
    eventlink_state,
    resourceentity_resource_id as eventlink_entity_id,
    'resource' as eventlink_entity,
    eventlink_is_organizer,
    eventlink_comment,
    resource_name as eventlink_label
    FROM Event
    INNER JOIN EventLink ON eventlink_event_id = event_id
    INNER JOIN ResourceEntity ON resourceentity_entity_id = eventlink_entity_id
    INNER JOIN Resource ON resourceentity_resource_id = resource_id
    WHERE event_id IN ($event_list) $multidomain";
  if($with_contacts)
    $query .= "
    UNION
    SELECT
    event_id,
    eventlink_state,
    contactentity_contact_id as eventlink_entity_id,
    'contact' as eventlink_entity,
    eventlink_is_organizer,
    eventlink_comment,
    ".DB_OBM::CONCAT(array("contact_lastname", "' '", "contact_firstname"))." as eventlink_label
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
 * Get all deleted attendees of an array of events
 *  
 * @param array $events 
 * @access public
 * @return void
 */
function run_query_get_events_deletedAttendees($events) {
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

  $query = "SELECT event_id, 
            'DECLINED' as eventlink_state, 
            userobm_id as eventlink_entity_id, 
            'user' as eventlink_entity, 
            ".DB_OBM::CONCAT(array("userobm_lastname", "' '", "userobm_firstname"))." as eventlink_label 
            FROM DeletedEvent 
            LEFT JOIN UserObm ON userobm_id = deletedevent_user_id 
            LEFT JOIN Event ON deletedevent_event_id = event_id 
            WHERE event_id IN ($event_list) $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_get_events_deletedAttendees()');
  $obm_q->xquery($query);
  return $obm_q;
}

/**
* Get the ID of the userObm user that is organizer of this event
* if the organizer is not a userObm it returns FALSE
*/
function get_event_organizer_userObm_id($event_id) {
  $res=run_query_get_events_attendee(array($event_id), false, false);
  while($res->next_record()) {
    if($res->f('eventlink_is_organizer')
    AND $res->f('eventlink_entity') == "user" ){
      return $res->f('eventlink_entity_id');
    }
  }
  return false;
}

/**
 * Get all exceptions for an array of events
 * 
 * @param array $events 
 * @param boolean $no_child if set to true, only exceptions that does not refer to a child event in Event table are selected
 * @access public
 * @return void
 */
function run_query_get_events_exception($events,$start_time = NULL,$end_time = NULL, $no_child = false) {
  global $cdg_sql;
  $obm_q = new DB_OBM;
  $event_list = implode(',',$events);
  if(!is_null($start_time)) {
    $start = "AND eventexception_date >= #SUBSECONDS('$start_time',event_duration)";
  }
  if(!is_null($end_time)) {
    $end = "AND eventexception_date <= '$end_time'";
  }
  if ( $no_child ) {
    $child_null = "AND eventexception_child_id is null";
  }

  $query = "SELECT
    event_duration,
    event_timezone,
    event_location,
    eventexception_parent_id,
    eventexception_child_id,
    eventexception_date
    FROM EventException
    INNER JOIN Event ON eventexception_parent_id = event_id
    WHERE eventexception_parent_id IN ($event_list) 
    $start $end $child_null
    ORDER BY eventexception_date";
  display_debug_msg($query, $cdg_sql, 'run_query_get_events_exception()');
  $obm_q->xquery($query);
  return $obm_q;
}

/**
 * Get all attached documents of an array of events
 *  
 * @param array $events 
 * @access public
 * @return void
 */
function run_query_get_events_documents($events) {
  global $cdg_sql;

  $event_list = implode(',',$events);
  if ($event_list == '') {
    $event_list = '0';
  }
  $obm_q = new DB_OBM;
  $query = "SELECT 
    evententity_event_id AS event_id,
    documentlink_document_id AS document_id
    FROM DocumentLink
    INNER JOIN EventEntity ON documentlink_entity_id = evententity_entity_id
    WHERE 
    evententity_event_id IN ($event_list)";
  
  display_debug_msg($query, $cdg_sql, 'run_query_get_events_documents()');
  $obm_q->xquery($query);
  return $obm_q;
}

/**
 * Get all attached documents of an array of events
 *  
 * @param array $events 
 * @access public
 * @return void
 */
function run_query_get_events_documents_with_name($events) {
  global $cdg_sql;

  $event_list = implode(',',$events);
  if ($event_list == '') {
    $event_list = '0';
  }
  $obm_q = new DB_OBM;
  $query = "SELECT 
    evententity_event_id AS event_id,
    documentlink_document_id AS document_id,
    d.document_name
    FROM DocumentLink
    LEFT JOIN Document as d on DocumentLink.documentlink_document_id = d.document_id
    INNER JOIN EventEntity ON documentlink_entity_id = evententity_entity_id
    WHERE 
    evententity_event_id IN ($event_list)";
  
  display_debug_msg($query, $cdg_sql, 'run_query_get_events_documents_with_name()');
  $obm_q->xquery($query);
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
    event_duration AS eventexception_event_duration,
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

function allday_start_date($date, $event_timezone_name, $display_timezone_name) {
  $date_with_timezone = clone $date;
  $event_timezone = new DateTimeZone($event_timezone_name);
  $date_with_timezone->setTimezone($event_timezone);
  $display_timezone = new DateTimeZone($display_timezone_name);
  $date_for_display = Of_Date::createFromFormat(
      "Y-m-d h:i:s", $date_with_timezone->format('Y-m-d')." 00:00:00", $display_timezone);
  return $date_for_display;
}

function allday_actual_duration($date, $duration) {
  $end = clone $date;
  $end->addDay(Of_Date::allDayDurationInDays($duration));
  
  return $end->diffTimestamp($date);
}

///////////////////////////////////////////////////////////////////////////////
// Return a table of Events between two dates
// Parameters: 
//   - $start_time      : timestamp of start date
//   - $end_time        : timestamp of end date
//   - $calendar_entity :
///////////////////////////////////////////////////////////////////////////////
function calendar_events_model($start, $end, $calendar_entity, $current_timezone, $status=array('ACCEPTED'), $category_filter='', $unique=false, $events=null, $opac=null, $occurences=true) {
  global $obm;

  // Get all non repeating events and construct occurences & events model
  $nr_q = run_query_calendar_no_repeat_events($start,$end,$calendar_entity, $status, $category_filter,$events, $opac);
  $of = &OccurrenceFactory::getInstance();
  $of->setBegin($start);
  $of->setEnd($end);
  if($nr_q != null) {
  while ($nr_q->next_record()) {
      $id = $nr_q->f('event_id');
      $title = $nr_q->f('event_title');
      $privacy = $nr_q->f('event_privacy');
      $description = $nr_q->f('event_description'); 
      $properties = $nr_q->f('event_properties');
      $location = $nr_q->f('event_location'); 
      $category1 = $nr_q->f('eventcategory1_label');
      $priority = $nr_q->f('event_priority');
      $all_day = $nr_q->f('event_allday');
      $timezone = $nr_q->f('event_timezone');
      $date = new Of_Date($nr_q->f('event_date'),'GMT');
      $duration = $nr_q->f('event_duration');
      if ($all_day == 1) {
        $date = allday_start_date($date, $timezone, $current_timezone);
        $duration = allday_actual_duration($date, $duration);
      }
      
      $color = $nr_q->f('event_color');
      $entity = $nr_q->f('eventlink_entity');
      $entity_id = $nr_q->f('eventlink_entity_id');
      $entity_state = $nr_q->f('eventlink_state');
      $owner = $nr_q->f('event_owner');
      $owner_name = $nr_q->f('userobm_lastname').' '.$nr_q->f('userobm_firstname');
      $opacity = $nr_q->f('event_opacity');
      $tag = $nr_q->f('eventtag_id');
      if (isset($of->events[$id])) {
        $event = $of->events[$id];
        if (!$unique) {
          $of->addOccurrence($event, $date, $entity, $entity_id, $entity_state);
        }
      } else {
        $event = new Event($id,$duration,$title,$location,$category1,$privacy,$description,$properties,$all_day,'none',$owner,$owner_name,$color,$opacity,$tag);
        $of->addOccurrence($event, $date, $entity, $entity_id, $entity_state);
      }
    }
  }
  // Get all repeating events and construct occurences & events model
  $r_q = run_query_calendar_repeat_events($start,$end,$calendar_entity, $status, $category_filter,$events, $opac);
  if($r_q != null) {
    while ($r_q->next_record()) {
      $id = $r_q->f('event_id');
      $title = $r_q->f('event_title');
      $privacy = $r_q->f('event_privacy');
      $description = $r_q->f('event_description');
      $properties = $r_q->f('event_properties');
      $location = $r_q->f('event_location');
      $category1 = $r_q->f('eventcategory1_label');
      $all_day = $r_q->f('event_allday');
      $timezone = $r_q->f('event_timezone');
      $date = new Of_Date($r_q->f('event_date'), 'GMT');
      $duration = $r_q->f('event_duration');
      if ($all_day == 1) {
        $date = allday_start_date($date, $timezone, $current_timezone);
        $duration = allday_actual_duration($date, $duration);
      }
      
      $repeatkind = $r_q->f('event_repeatkind');
      $endrepeat = new Of_Date($r_q->f('event_endrepeat'), 'GMT');
      $entity = $r_q->f('eventlink_entity');
      $color = $r_q->f('event_color');
      $repeatfrequence = $r_q->f('event_repeatfrequence');
      $repeatdays = $r_q->f('event_repeatdays');
      $entity_id = $r_q->f('eventlink_entity_id');
      $entity_state = $r_q->f('eventlink_state');
      $all_day = $r_q->f('event_allday');    
      $owner = $r_q->f('event_owner');
      $owner_name = $r_q->f('userobm_lastname').' '.$r_q->f('userobm_firstname');
      $opacity = $r_q->f('event_opacity');
      $tag = $r_q->f('eventtag_id');

      if ($occurences) {
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
          $event = new Event($id,$duration,$title,$location,$category1,$privacy,$description,$properties,$all_day,$repeatkind,$owner,$owner_name,$color,$opacity,$tag);
          $event->setTimezone($timezone);
        }
        $tz = new DateTimeZone($timezone);
        $date->setTimezone($tz);
        $event_start = clone $start;
        $event_start->setTimezone($tz);
        $event_start->subSecond($duration)->setHour($date)->setMinute($date)->setSecond($date);
        $event_end = ($end->compare($endrepeat) > 0)? clone $endrepeat: clone $end;
        $event_end->setTimezone($tz);
        $event_end->setHour($date)->setMinute($date)->setSecond($date)->addSecond(1);
        if ($addOcc) {
          calendar_add_anonymous_occurrences($repeatkind,$date,$event_start,$event_end,$repeatfrequence,$event,$entity_id, $entity,$entity_state,$repeatdays);
        }
      } else {
        if (isset($of->events[$id])) {
          $event = $of->events[$id];
          if (!$unique) {
            $of->addOccurrence($event, $date, $entity, $entity_id, $entity_state);
          }
        } else {
          $event = new Event($id,$duration,$title,$location,$category1,$privacy,$description,$properties,$all_day,$repeatkind,$owner,$owner_name,$color,$opacity,$tag);
          $of->addOccurrence($event, $date, $entity, $entity_id, $entity_state);
        }
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
      if($attendee_q->f('eventlink_is_organizer')){
        $event->setOrganizerName($entity_label);
      }
      $event->addAttendee($entity,$entity_id,$entity_label,$entity_state);
    }

    // Remove occurences of confidential events that the logged-in user doesn't attend
    foreach ($of->events as $id => $event) {
      if ($event->privacy == 2) {
        foreach ($of->occurrences[$id] as $occurence) {
          if ($occurence->id != $obm['uid'] && !$event->userAttendeeExists($obm['uid'])) {
            $of->removeOccurrences($id, $occurence->date);
          }
        }
      }
    }
  }

  if (is_array($calendar_entity['contact'])) {
    $search = false;
    if ($GLOBALS['action'] == 'search') {
      $calendar = $GLOBALS['params'];
      $type = $calendar['searchType'];
      $summary = trim($calendar['title']);
      $location = trim($calendar['location']);
      $desc = trim($calendar['desc']);
      $owner = trim($calendar['owner']);
      if ($type == 'basic') {
        $pattern = trim(strtolower("$calendar[pattern] $summary $owner $location $desc"));
        $pattern_owner = $pattern;
      } else if ($type == 'advanced') {
        $pattern = array('summary' => $summary, 'location' => $location, 'desc' => $desc);
        $pattern_owner = $owner;
      }
      $search = true;
    }
    foreach($calendar_entity['contact'] as $c) {
      $obmsyncServer = array_key_exists("obmsyncServer",$c) ? $c["obmsyncServer"] : null;
      $contact = OBM_Contact::get($c['id']);
      $emails = array();
      foreach($contact->email as $email) {
        $emails[] = 'mailto:'.trim(strtolower($email['address']));
      }
      $owner_name = $contact->display_name();
      if (is_array($vevents = $contact->getEventsInInterval($start, $end, $obmsyncServer))) {
        $extra_exceptions = array();
        foreach($vevents as $vevent) {
          if (!$search || 
              ($search  && $type == 'advanced' && ((empty($pattern_owner) || !empty($pattern_owner) && (array_intersect(explode(" ", strtolower($owner_name)), explode(" ", $pattern_owner)))) 
                && $vevent->match($pattern, $type))) || 
              ($search  && $type == 'basic' && ((empty($pattern_owner) || !empty($pattern_owner) && (array_intersect(explode(" ", strtolower($owner_name)), explode(" ", $pattern_owner)))) 
                || $vevent->match($pattern, $type)))) {
            $uid = $vevent->getUid();
			$recurrence_id = $vevent->getRecurrenceId() ;
            if ($recurrence_id) {
              $extra_exceptions[] = array('uid' => $vevent->getUid(), 'date' => $recurrence_id);
              $uid = $uid.'-'.count($extra_exceptions);
            }
            $event = new Event($uid,$vevent->getDuration(), $vevent->getSummary(), $vevent->getLocation(),
              $vevent->getCategory(),$vevent->getPrivacy(),$vevent->getDescription(), null,
              $vevent->isAllDay(),'none',$c['id'],$owner_name, null, $vevent->getTransparency(),null,true);
	    if ( get_class($vevent) == "Vpdi_Icalendar_Freebusy" ) {
	      $event->addAttendee('contact',$c['id'],$owner_name, "ACCEPTED");
	    } else {
	      $attendees = $vevent->evt->getAttendees();
	      $partstat = 'ACCEPTED';
	      foreach($attendees as $attendee) {
		if(trim(strtolower($attendee->cn)) == trim(strtolower($c['name']))) {
		  $partstat = $attendee->partstat;
		  break;
		}
		if(in_array(trim(strtolower($attendee->uri)), $emails)) {
		  $partstat = $attendee->partstat;
		  break;
		}
	      }
	      $event->addAttendee('contact',$c['id'],$owner_name, $partstat);
	    }
            $date = clone $vevent->start;
            if ($rrule = $vevent->getRrule()) {
              // Repeat events
              $event_start = clone $date;
              $event_start->setHour(0)->setMinute(0)->setSecond(0);
              $event_end = ($rrule['until'] != null && $end->compare($rrule['until']) > 0)? clone $rrule['until']: clone $end; 
              $event_end->setHour(0)->setMinute(0)->setSecond(0)->addSecond(1);
              switch($rrule['freq']) {
                case 'daily' :
                  calendar_daily_repeatition($date, $event_start,$event_end, $rrule['interval'], $event, $c['id'], "contact", $partstat);
                  break;
                case 'weekly' :
                  calendar_weekly_repeatition($date, $event_start,$event_end, $rrule['repeat_days'], $rrule['interval'], $event, $c['id'], "contact", $partstat);
                  break;
                case 'monthlybyday' :
                  $stored = calendar_monthlybyday_repeatition($date, $event_start,$event_end, $rrule['interval'], $event, $c['id'], "contact", $partstat);
                  break;
                case 'monthlybydate' :
                  $stored = calendar_monthlybydate_repeatition($date, $event_start,$event_end, $rrule['interval'], $event, $c['id'], "contact", $partstat);
                  break;
                case 'yearly' :
                  $stored = calendar_yearly_repeatition($date, $event_start,$event_end, $rrule['interval'], $event, $c['id'], "contact", $partstat);
                  break;
              }
              // Remove exceptions
              if (is_array($exdates = $rrule['exdates'])) {
                foreach($exdates as $exdate) {
                  if ($exdate->compare($end) < 0 && $exdate->compare($start) > 0) { 
                    $of->removeOccurrences($vevent->getUid(), $exdate);
                  }
                }
              } 
            } else {
              $of->addOccurrence($event, $date, "contact", $c['id'], $partstat);
            }
          }
        }
        foreach ($extra_exceptions as $e) {
          $of->removeOccurrences($e['uid'], $e['date']);
        }
      }
    }
  }

  return $of->getOccurrences(); 
}

function calendar_add_anonymous_occurrences($repeatkind, $date, $event_start, $event_end, $repeatfrequence, $event, $entity_id, $entity, $entity_state, $repeatdays = null) {
  // OBMFULL-3223
  // repeatfrequence shouldn't be 0 or the occurrences fetching algorithm will never increment
  if ( $repeatfrequence == 0 ) {
    $repeatfrequence = 1;
  }

  switch ($repeatkind) {
  case 'daily' :
    calendar_daily_repeatition($date,$event_start,$event_end,$repeatfrequence,$event,$entity_id,$entity, $entity_state);
    break;
  case 'weekly' :
    calendar_weekly_repeatition($date,$event_start,$event_end,$repeatdays,$repeatfrequence,$event,$entity_id,$entity, $entity_state);
    break;
  case 'monthlybyday' :
    calendar_monthlybyday_repeatition($date,$event_start,$event_end,$repeatfrequence,$event,$entity_id,$entity, $entity_state);
    break;
  case 'monthlybydate' :
    calendar_monthlybydate_repeatition($date,$event_start,$event_end,$repeatfrequence,$event,$entity_id,$entity, $entity_state);
    break;
  case 'yearly' :
    calendar_yearly_repeatition($date,$event_start,$event_end,$repeatfrequence,$event,$entity_id,$entity, $entity_state);
    break;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Perform the daily repeatition of an event 
// Parameters:
//   - $date : timestamp
///////////////////////////////////////////////////////////////////////////////
function calendar_daily_repeatition($date,$start,$end,$repeatfrequence,$event,$entity_id,$entity, $entity_state) {

  $current = clone $date;
  if ($current->compare($start) < 0) {
    $diff = - round($current->diffTimestamp($start) / Of_Date::DAYDURATION); 
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
    $diff = - round($current->diffTimestamp($start) / Of_Date::DAYDURATION); 
    if ($diff > 0) {
      $diff = ($diff % (($repeatfrequence) * 7));
    }     
    $current->setDateIso($start)->subDay($diff);
  }
  $days = str_split($repeatdays);
  $is_allday = $event->all_day;
  if($start->compare($date) < 0) {
    $start = $date;
    $of = &OccurrenceFactory::getInstance();
    $of->addOccurrence($event, clone $date, $entity, $entity_id, $entity_state);
  }
  while ($current->compare($end) < 0) {
    foreach ($days as $num => $repeat) {  
      if ($repeat == 1) {
        $current->setWeekday($num);
        $is_occ_in_interval = $current->compare($start) >= 0 && $current->compare($end) < 0;
        // When the first occurrence (as given by the recurrence 
        // rule) matches the start date, we do not add this occurence,
        // since we always add an occurrence which matches the current day
        $is_occ_already_added = $date->compare($current) != 0;
        if($is_occ_in_interval && $is_occ_already_added) {
            $occ_date = $is_allday ?
                allday_start_date($current, $event->timezone, Of_date::getOption('timezone')) : clone $current;
          $of = &OccurrenceFactory::getInstance();
          $of->addOccurrence($event, $occ_date, $entity, $entity_id, $entity_state);
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
  while ($current->compareDateIso($end) <= 0) {
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
    $current->setHour($date->getHour())->setMinute($date->getMinute());
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
  while ($current->compareDateIso($end) <= 0) {
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
  while ($current->compareDateIso($end) <= 0) {
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
// Collects a new contact : used for other attendees or shared calendars
// Parameters:
//   - $attendees : hashed attendees email
///////////////////////////////////////////////////////////////////////////////
function run_query_collect_contact($email, $lastname = null, $firstname = null) {
  global $obm, $cdg_sql;

  $uid = sql_parse_id($obm['uid']);
  $domain_id = sql_parse_id($obm['domain_id']);

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  
  // Get collected addressbook
  $addressBooks = OBM_AddressBook::search();
  $ids = array();
  foreach($addressBooks as $ad) {
    array_push($ids, $ad->id);
  }
  $addressbook_id = $addressBooks->getCollectedAddressbook()->id;
  $in = implode(',', $ids);
  
  //// Check if exists
  // XXXXXX Migrate the access right to the Addressbook
  $query = "SELECT contact_id FROM Contact
    INNER JOIN ContactEntity ON contactentity_contact_id = contact_id
    INNER JOIN Email ON email_entity_id = contactentity_entity_id 
    WHERE (contact_addressbook_id IN ($in) OR contact_usercreate='$uid') AND contact_archive='0' and email_address = '$email'";
  display_debug_msg($query, $cdg_sql,"'run_query_collect_contact($email)");
  $obm_q->query($query);    

  if ($obm_q->next_record()) {
    return $obm_q->f('contact_id');
  }
  
  if (is_null($lastname) && is_null($firstname)) {
    list($firstname, $lastname) = guess_name_from_email($email);
  }
  // Insert contact
  $query = "INSERT INTO Contact (contact_timeupdate,
    contact_timecreate,
    contact_userupdate,
    contact_usercreate,
    contact_domain_id,
    contact_lastname,
    contact_firstname,
    contact_addressbook_id,
    contact_marketingmanager_id,
    contact_archive,
    contact_origin) VALUES (
    NOW(),
    NOW(),
    $uid,
    $uid,
    $domain_id,
    '$lastname',
    '$firstname',
    '$addressbook_id',
    '$uid',
    '0',
    '$GLOBALS[c_origin_web]')";

  display_debug_msg($query, $cdg_sql,"'run_query_collect_contact($email)");
  $obm_q->query($query);

  // Get contact_id & entity_id
  $id = $obm_q->lastid();
  $entityid = of_entity_insert('contact', $id);

  $query = "INSERT INTO Email (email_entity_id, email_address, email_label) 
    VALUES ($entityid, '$email', 'INTERNET;X-OBM-Ref1')";
  display_debug_msg($query, $cdg_sql,"'run_query_insert_others_attendees($email)");
  $obm_q->query($query);
  
  // solr
  $c = OBM_Contact::get($id);
  OBM_Contact::solrStore($c);
  OBM_IndexingService::commit('contact');
    
  return $id;
}

///////////////////////////////////////////////////////////////////////////////
// Adds shared calendar with contact info
///////////////////////////////////////////////////////////////////////////////
function run_query_add_contact_shared_calendar($params) {
  global $obm, $cdg_sql;
  
  $email = $params['email'];
  $url = $params['url'];
  $lastname = isset($params['lastname']) ? $params['lastname'] : null;
  $firstname = isset($params['firstname']) ? $params['firstname'] : null;
  
  $id = run_query_collect_contact($email, $lastname, $firstname);
  $entityid = of_entity_insert('contact', $id);
  
  $obm_q = new DB_OBM;
  $query = "DELETE FROM Website WHERE website_entity_id = $entityid";
  display_debug_msg($query, $cdg_sql, 'run_query_add_contact_shared_calendar()');
  $obm_q->query($query);
  $query = "INSERT INTO Website (website_entity_id, website_url, website_label) VALUES ($entityid, '".$obm_q->escape($url)."', 'CALURI;X-OBM-Ref1')";
  display_debug_msg($query, $cdg_sql, 'run_query_add_contact_shared_calendar()');
  $obm_q->query($query);
  
  return $id;
}

///////////////////////////////////////////////////////////////////////////////
// Insert others attendees as private contacts
///////////////////////////////////////////////////////////////////////////////
function run_query_insert_others_attendees($calendar) {
  $attendees = $calendar['others_attendees'];
  $ret = array();

  foreach ($attendees as $email) {
    $id = run_query_collect_contact($email);
    if (!in_array($id, $ret)) {
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
              WHERE eventtag_label = '".$obm_q->escape($taglabel)."' and eventtag_user_id = ".$obm['uid']." ;" ;
    $obm_q->query($query);
    if ($obm_q->num_rows() > 0) {
      $query = "UPDATE EventTag SET 
                eventtag_color = '".$obm_q->escape($tagcolor)." '
                WHERE eventtag_label = '".$obm_q->escape($taglabel)."'
                AND eventtag_user_id = ".$obm['uid']." ;" ;
      $obm_q->query($query) ; 
    } else {
      $query = "INSERT INTO EventTag 
                (eventtag_label, eventtag_color, eventtag_user_id) 
                VALUES ('".$obm_q->escape($taglabel)."', '".$obm_q->escape($tagcolor)."', ".$obm['uid'].") ;" ;
      $obm_q->query($query) ;
      // get back the last inserted eventtag_id
      $query = "SELECT eventtag_id FROM EventTag 
                WHERE eventtag_label = '".$obm_q->escape($taglabel)."' and eventtag_user_id = ".$obm['uid']." ;" ;
      $obm_q->query($query) ;
      $obm_q->next_record() ;
      $tagid = $obm_q->f('eventtag_id') ;
      
      // tweak
      $calendar['tag_id'] = $tagid ; 
    }
  }
  $event_tag_id = $calendar['tag_id'] ;
  if (!isset($event_tag_id) || $event_tag_id == $GLOBALS['c_none']) $event_tag_id = 'NULL' ;
  
  return $event_tag_id;
}

///////////////////////////////////////////////////////////////////////////////
// Get all user defined tags for event
// Parameters:
//   - $obmUserId : int obmuser identifier
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_get_alltags($obmUserId=null,$tag_id=null) {
  global $cdg_sql;

  $obm_q = new DB_OBM ;
  if ($obmUserId) {
    $uid = sql_parse_id($obmUserId);
    $where = " AND eventtag_user_id='$uid'";
  }
  if ($tag_id) $where = " AND eventtag_id=$tag_id";
  $query = "SELECT * FROM EventTag WHERE 1=1 $where 
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
      SET eventtag_label = '".$obm_q->escape($taglabel)."', eventtag_color = '".$obm_q->escape($tagcolor)."' 
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
      WHERE eventtag_label = '".$obm_q->escape($taglabel)."' 
      AND eventtag_user_id = $uid" ;
  $obm_q->query($query) ;
  
  if ($obm_q->num_rows() > 0) {
    // update the tag
    $obm_q->next_record() ;
    $tagid = $obm_q->f('eventtag_id') ;
    $query = "UPDATE EventTag 
      SET eventtag_label = '".$obm_q->escape($taglabel)."', eventtag_color = '".$obm_q->escape($tagcolor)."' 
      WHERE eventtag_id = $tagid AND eventtag_user_id = $uid ;" ;
  }
  else {
    // create the tag
    $query = "INSERT INTO EventTag 
              (eventtag_label, eventtag_color, eventtag_user_id)
              VALUES ('".$obm_q->escape($taglabel)."', '".$obm_q->escape($tagcolor)."', $uid) ;" ;
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
  if (is_array($params['other_files'])) {
    foreach ($params['other_files'] as $document) {
      $document['name'] = strip_tags($document['name']);
      $document['title'] = $document['name'];
      $document['author'] = $obm['login'];
      $document['mime'] = get_document_auto_mime_type($document);
      $document['path'] = $default_path.'/';
      $document['kind'] = 1;
      $document['privacy'] = 1;
      
      $doc_id = run_query_document_insert($document);
      if (!$doc_id) {
        return false;
      } else {
        $doc_ids[] = $doc_id;
      }
    }
  }
  
  return $doc_ids;
}



/*
 * Store event in solr
 * @param event_id
 */
function run_query_calendar_solr_store($event_id) {
  $query = "SELECT 
      event_id,
      event_timeupdate,
      event_timecreate,
      event_usercreate,
      event_userupdate,
      event_domain_id,
      event_title,
      event_location,
      event_date,
      event_duration,
      eventcategory1_id, 
      eventcategory1_label, 
      event_owner,
      event_description,
      eventtag_id,
      eventtag_label,
      event_allday,
      event_repeatkind,
      event_opacity,
      event_privacy,
      event_origin,
      userobm_lastname as owner_lastname,
      userobm_firstname as owner_firstname
    FROM Event
    LEFT JOIN EventCategory1 ON event_category1_id = eventcategory1_id
    LEFT JOIN EventTag ON eventtag_id = event_tag_id
    INNER JOIN UserObm ON userobm_id = event_owner
    WHERE event_id='$event_id'";
  $db = new DB_OBM;
  $db->xquery($query);
  $db->next_record();

  $doc = new Apache_Solr_Document();
  $doc->setField('id', $db->f('event_id'));
  $timecreate = new Of_Date($db->f('event_timecreate'));
  $doc->setField('timecreate', $timecreate->format('Y-m-d\TH:i:s\Z'));
  $timeupdate = new Of_Date($db->f('event_timeupdate'));
  $doc->setField('timeupdate', $timeupdate->format('Y-m-d\TH:i:s\Z'));
  $doc->setField('usercreate', $db->f('event_usercreate'));
  $doc->setField('userupdate', $db->f('event_userupdate'));
  $doc->setField('domain', $db->f('event_domain_id'));
  $doc->setField('title', $db->f('event_title'));
  $doc->setField('location', $db->f('event_location'));
  $doc->setMultiValue('category', $db->f('eventcategory1_id'));
  $doc->setMultiValue('category', $db->f('eventcategory1_label'));
  $edate = new Of_Date($db->f('event_date'));
  $doc->setField('date', $edate->format('Y-m-d\TH:i:s\Z'));
  $doc->setField('duration', $db->f('event_duration'));
  $doc->setMultiValue('ownerId', $db->f('event_owner'));
  $doc->setMultiValue('owner', $db->f('owner_lastname')." ".$db->f('owner_firstname'));
  $doc->setField('description', $db->f('event_description'));
  $doc->setMultiValue('tag', $db->f('eventtag_id'));
  $doc->setMultiValue('tag', $db->f('eventtag_label'));
  if ($db->f('event_allday')) {
    $doc->setMultiValue('is', 'allday');
  }
  if ($db->f('event_repeatkind') != 'none') {
    $doc->setMultiValue('is', 'periodic');
  }
  if ($db->f('event_opacity') == 'OPAQUE') {
    $doc->setMultiValue('is', 'busy');
  } elseif ($db->f('event_opacity') == 'TRANSPARENT') {
    $doc->setMultiValue('is', 'free');
  }
  switch ($db->f('event_privacy')) {
    case 1:
      $doc->setMultiValue('is', 'private');
      break;
    case 2:
      $doc->setMultiValue('is', 'confidential');
  }
  $doc->setField('from', $db->f('event_origin'));
  $attendees_q = run_query_get_events_attendee(array($event_id));
  while ($attendees_q->next_record()) {
    $doc->setMultiValue('with', $attendees_q->f('eventlink_label'));
    $doc->setMultiValue('withId', $attendees_q->f('eventlink_entity_id'));
  }
  OBM_IndexingService::store('event', array($doc));
  OBM_IndexingService::commit('event');
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
  var $organizer_name;
  var $color;
  var $tag ;
  var $timezone;
  var $external;

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
  function Event($id,$duration,$title,$location,$category1,$privacy,$description,$properties,$all_day,$repeat,$owner,$owner_name,$color,$opacity,$tag,$external=false, $organizerName=null) {
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
    $this->attendee_data = array();
    $this->owner = $owner;
    $this->owner_name = $owner_name;
    $this->color = $color;
    $this->opacity = $opacity;
    $this->tag = $tag;
    $this->external = $external;
    $this->organizer_name = $organizerName != null ? $organizerName : $owner_name;
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
  
  function isConfidential() {
    return $this->privacy == 2;
  }
  
  function isNotPublic() {
    return $this->isPrivate() || $this->isConfidential();
  }

  function isExternal() {
    return ($this->external);
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
    $this->attendee[$entity][$entity_id] = $entity_label.'('.$GLOBALS["l_".strtolower(str_replace('-', '_', $entity_state))].')';
    $this->attendee_data[] = array(
      "entity"=>$entity,
      "entity_id"=>$entity_id,
      "entity_label"=>$entity_label,
      "entity_state"=>$entity_state
    );
  }

  function userAttendeeExists($entity_id) {
    foreach ($this->attendee_data as $attendee) {
      if ($attendee["entity"] == "user" && $attendee["entity_id"] == $entity_id) {
        return true;
      }
    }

    return false;
  }

  function setOrganizerName($organizerName){
    $this->organizer_name = $organizerName;
  }

  public function isOrganizerAnObmUser() {
    if ( !$this->organizer_name ) {
      return FALSE;
    }
    foreach ( $this->attendee_data as $attendee) {
      if ( $attendee["entity"] == "user" && $attendee["entity_label"] == $this->organizer_name ) {
        return TRUE;
      }
    }
    return FALSE;
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
      $this->date = $date;
      $this->end = clone $date;
      $this->end->addSecondsInUTC($event->duration);
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
  if ($occA->date->compare($occB->date) < 0 ){
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

  if($export == 'date'){
	$date = $params['event_before_date'];
	$filter =  "AND event_date < '$date' AND (event_repeatkind = 'none' OR event_endrepeat < '$date')";
  }

  # Delete all events where the user is the owner and an attendee and all events where the owner
  # is writable and the user is the only attendee
  $query = "SELECT event_id FROM Event
    INNER JOIN EventLink ON event_id = eventlink_event_id
    INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id
    LEFT JOIN Contact birthday ON birthday.contact_birthday_id = event_id
    LEFT JOIN Contact anniversary ON anniversary.contact_anniversary_id = event_id
    LEFT JOIN EventException AS ee ON event_id = ee.eventexception_child_id
    WHERE (event_type='VEVENT' AND event_owner = $id AND userentity_user_id = $id $filter)
      AND birthday.contact_birthday_id IS NULL AND anniversary.contact_anniversary_id IS NULL
      AND ee.eventexception_parent_id IS NULL
    GROUP BY event_id HAVING COUNT(*) = 1
    UNION
    SELECT event_id FROM Event
    INNER JOIN EventLink ON event_id = eventlink_event_id
    INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id
    LEFT JOIN Contact birthday ON birthday.contact_birthday_id = event_id
    LEFT JOIN Contact anniversary ON anniversary.contact_anniversary_id = event_id
    LEFT JOIN EventException AS ee ON event_id = ee.eventexception_child_id
    WHERE event_type='VEVENT' AND event_owner IN  (".implode(',',$writable).") $filter
      AND birthday.contact_birthday_id IS NULL AND anniversary.contact_anniversary_id IS NULL
      AND ee.eventexception_parent_id IS NULL
    GROUP BY event_id HAVING COUNT(*) = 1 AND MAX(userentity_user_id) = $id";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_reset(1)');
  $obm_q->query($query);

  while($obm_q->next_record()) {
    $calendar['calendar_id'] = $obm_q->f('event_id');
    $event[] = $calendar['calendar_id'];
    run_query_calendar_delete($calendar);
  }

  if(!is_null($meeting)){
    # Refuse all other events
    $query = "SELECT eventlink_event_id FROM EventLink
      INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id
      INNER JOIN Event ON event_id = eventlink_event_id
      LEFT JOIN Contact birthday ON birthday.contact_birthday_id = event_id
      LEFT JOIN Contact anniversary ON anniversary.contact_anniversary_id = event_id
      WHERE userentity_user_id = $id 
      AND birthday.contact_birthday_id IS NULL AND anniversary.contact_anniversary_id IS NULL
      AND eventlink_state != 'DECLINED' $filter";
    display_debug_msg($query, $cdg_sql, 'run_query_calendar_reset(2)');
    $obm_q->query($query);
    while($obm_q->next_record()) {
      $event[] = $obm_q->f('eventlink_event_id');
      run_query_calendar_update_occurrence_state($obm_q->f('eventlink_event_id'), 'user', $id, 'DECLINED', true);
    }
  }

  # Delete all alerts
  if(!is_null($event)) {
    $query = "DELETE FROM EventAlert WHERE eventalert_user_id = $id AND eventalert_event_id IN (".implode(',',$event).")"; 
    display_debug_msg($query, $cdg_sql, 'run_query_calendar_reset(3)');
    $obm_q->query($query);
  }
}

/**
 * Get events for an entity
 */
function get_json_entity_events($calendar, $entity_store, $timezone) {
  global $obm, $display;

  $json = array();
  if (is_array($calendar['resource_id']) || is_array($calendar['user_id'])) {
    $date = $calendar['date'];
    $date->setWeekday($GLOBALS['ccalendar_weekstart'])->setHour(0)->setMinute(0)->setSecond(0);
    $end = clone $date;
    $end->addDay(7);  

    $canRead = 0;
    $status = array('ACCEPTED', 'NEEDS-ACTION');
    $of = &OccurrenceFactory::getInstance();
    calendar_events_model($date,$end,$entity_store, $timezone, $status);
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
  protected $date;

  protected $cal_range;
  protected $cal_view;
  protected $category;
  protected $group;
  protected $users;
  protected $resources;
  protected $contacts;
  protected $classes;
  protected $entity_id_entity;
  protected $ndays;

  protected $writable_calendars = array();

  /**
   * CalendarView constructor
   * 
   * @param view data $data
   * @access public
   */
  public function __construct($data = array()) {
    $this->modify();
    $this->date = new Of_Date();
    $this->set_cal_range($data['cal_range']);
    $this->set_cal_view($data['cal_view']);
    $this->set_category($data['category']);
    $this->remove_all();
    $this->entity_id_entity=$data['entity_id_entity'];
    $this->load_color_entity();
    $this->set_users($data['users']);
    $this->set_group($data['group']);
    $this->set_contacts($data['contacts']);
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
    $this->modify();
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
    $id = ( $id == 'NULL' ) ? 0 : $id;

    $query = "SELECT * FROM ObmBookmarkProperty WHERE obmbookmarkproperty_bookmark_id='$id'";
    display_debug_msg($query, $cdg_sql, "CalendarView::get_from_id()");
    $obm_q->query($query);

    $data = array();
    while($obm_q->next_record()) {
      $property = $obm_q->f("obmbookmarkproperty_property");
      $value = $obm_q->f("obmbookmarkproperty_value");

      if (in_array($property,array('users','resources','contacts'))) {
        if ($value != "")
          $data[$property] = explode(',',$value);
      } elseif (in_array($property,array('user_classes','resource_classes','contact_classes', 'entity_id_entity'))) {
        $data[$property] = unserialize($value);
      } else {
        $data[$property] = $value;
      }
    }
    if($data['group'] == NULL && $data['users'] == NULL && $data['resources'] == NULL) {
      $GLOBALS['display']['msg'] .= display_err_msg($GLOBALS['l_view_invalid']);
    } else {
      $view = new CalendarView($data);
      $view->id = $bookmark_id;
      return $view;
    }
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

    if (empty($this->label) && ($this->id)) {
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
   * standard date getter,
   * 
   * @access public
   * @return Of_Date
   */
  public function get_date() {
    return $this->date;
  }
  
  /**
   *  return date for new event, new meeting
   *
   *  @access public
   *  @return Of_Date
   */
  public function get_day() {
    $day = $this->get_date();
    $today_date = Of_Date::today();
    $date_range = $this->get_date_range();

    if ($this->cal_range == "week" && !$day->diffWeek($today_date)) {
      $day = $today_date; 
    } else if ($this->cal_range == "month" && !$day->diffMonth($today_date)) {
      $day = $today_date; 
    } else if ($this->cal_range == "custom" && $date_range['begin'] < $today_date && $today_date < $date_range['end']) {
      $day = $today_date;
    }

    if(!$day->isToday()){
      $day = $date_range['begin'];
    }

    return $day;
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
   * get the date range
   * 
   * The date range return the first and last day displayed in the calendar view
   * In the week view the first day is the monday and the last day is the sunday
   * In the month view the first day displayed is the monday if the month begins a monday,
   * otherwise it's the last monday of the previous month. Etc...
   *
   * @access public
   * @return array
   */
  public function get_date_range() {
    $begin = clone $this->get_date();

    if ($this->cal_range == "day") {
      $begin->setHour(0)->setMinute(0)->setSecond(0);
      $end = clone $begin;
      $end->addDay(1);
    } else if ($this->cal_range == "week") {
      $begin->setWeekday($GLOBALS['ccalendar_weekstart'])->setHour(0)->setMinute(0)->setSecond(0);
      $end = clone $begin;
      $end->addDay(7);
    } else if ($this->cal_range == "month") {
      $begin->setDay(1)->addDay(1)->subWeekday($GLOBALS['ccalendar_weekstart'])->setHour(0)->setMinute(0)->setSecond(0);
      $end = clone $this->get_date();
      $end->setHour(0)->setMinute(0)->setSecond(0)->setDay(1);// the first day of the displayed month at midnight
      $end->addMonth(1); // the first day of the next month
      $end->subDay(1); // the last day of the displayed month
      $end->addWeekday($GLOBALS['ccalendar_weekstart']);//find the next "first day of week" (eg monday)
      $end->setHour(0)->setMinute(0)->setSecond(0);
    } else if ($this->cal_range == "custom") {
      $end = clone $this->get_date();
      $end->addDay($this->ndays);
    }

    return array('begin' => $begin, 'end' => $end);
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
   * get the selected category
   * 
   * @access public
   * @return string
   */
  public function get_category_filter() {
    return $this->category == $GLOBALS['c_all'] ? '' : $this->category;
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

  public function users_count() {
    return count($this->users);
  }

  public function get_resources() {
    return $this->resources;
  }

  public function resources_count() {
    return count($this->resources);
  }

  public function get_contacts() {
    return $this->contacts;
  }

  public function contacts_count() {
    return count($this->contacts);
  }

  public function get_entities() {
    global $ccalendar_max_users;
    $remaining_count = $ccalendar_max_users;

    $entities = array();
    if (count($this->users)>0) {
      $entities['user'] = $this->get_users_data($remaining_count);
      $remaining_count = $remaining_count - count($entities['user']);
    }
    if (count($this->resources)>0) {
      $entities['resource'] = $this->get_resources_data($remaining_count);
      $remaining_count = $remaining_count - count($entities['resource']);
    }
    if (count($this->contacts)>0) {
      $entities['contact'] = $this->get_contacts_data($remaining_count);
      $remaining_count = $remaining_count - count($entities['contact']);
    }
    return $entities;
  }

  public function entities_count() {
    return count($this->users) + count($this->resources) + $this->contacts_count();
  }

  public function get_writable_calendars($current_user_id) {
    if ( isset($this->writable_calendars[$current_user_id]) ) {
      return $this->writable_calendars[$current_user_id];
    }
    $writable_calendars = null;
    $calendar_entity = $this->get_entities();
    if (isset($calendar_entity['user'])) {
      $calendar_user_objects = $calendar_entity['user'];
      $writable_calendars = run_query_writable_calendars($current_user_id, array_values($calendar_user_objects));
    }
    $this->writable_calendars[$current_user_id] = $writable_calendars;
    return $this->writable_calendars[$current_user_id];
  }

  public function get_default_writable_calendar($current_user_id) {
    $default_writable_calendar = null;
    $writable_calendars = $this->get_writable_calendars($current_user_id);
    if (!is_array($writable_calendars) || count($writable_calendars) == 0 ) {
      return $default_writable_calendar;
    }

    foreach ( $writable_calendars as $cal ) {
      if ( $cal["id"] == $current_user_id ) {
	$default_writable_calendar = $cal;
      }
    }
    if ( !$default_writable_calendar ) {
      $default_writable_calendar = $writable_calendars[0];
    }
    return $default_writable_calendar;
  }

  public function set_label($label) {
    $this->label = $label;
  }

  public function set_date($date) {
    $this->date = $date;
  }

  public function get_ndays() {
    return $this->ndays;
  }

  public function set_cal_range($cal_range = null,$ndays = null) {
    $this->cal_range = (empty($cal_range) ? 'week' : $cal_range);
    $this->ndays = (empty($ndays) ? 7 : $ndays);
  }

  public function set_cal_view($cal_view = null) {
    $this->cal_view = (empty($cal_view) ? 'agenda' : $cal_view);
  }

  public function set_category($category = null) {
    $this->category = (empty($category) ? $GLOBALS['c_all'] : $category);
  }

  public function set_group($group_id) {
    if ( !is_numeric($group_id) || !ctype_digit($group_id) ) {
      $group_id = null;
    }
    $this->group = (empty($group_id) ? $GLOBALS['c_all'] : $group_id);
    if ($this->group!=$GLOBALS['c_all']) {
      foreach ($this->resources as $resource_id) {
        $this->remove_resource($resource_id);
      }
      $this->set_users(of_usergroup_get_group_users($this->group));
    }
  }

  public function set_users($user_ids) {
    $this->set_entities($user_ids, 'user');
  }

  public function set_resources($resource_ids) {
    $this->set_entities($resource_ids, 'resource');
  }

  public function set_contacts($contact_ids) {
    $this->set_entities($contact_ids, 'contact');
  }

  private function set_entities($ids, $entity) {
    switch($entity) {
      case 'user':
        $thisEntities = &$this->users;
        break;
      case 'resource':
        $thisEntities = &$this->resources;
        break;
      case 'contact':
        $thisEntities = &$this->contacts;
        break;
    }
    if (empty($ids)) $ids = array();
    if (!is_array($ids)) $ids = array($ids);
    if (!is_array($thisEntities)) $thisEntities = array();
    $to_add = array_diff($ids,$thisEntities);
    $to_remove = array_diff($thisEntities,$ids);
    foreach ($to_remove as $id) {
      $this->remove_entity($entity, $id);
    }
    foreach ($to_add as $id) {
      $this->add_entity($entity, $id);
    }
  }

  public function add_entity($entity, $id) {
    switch($entity) {
    case 'user' :
      $this->add_user($id);
      break;
    case 'resource' :
      $this->add_resource($id);
      break;
    case 'contact':
      $this->add_contact($id);
      break;
    }
  }

  public function remove_entity($entity, $id) {
    switch($entity) {
    case 'user' :
      $this->remove_user($id);
      break;
    case 'resource' :
      $this->remove_resource($id);
      break;
    case 'contact' :
      $this->remove_contact($id);
      break;
    }
  }

  public function add_user($user_id) {
    if (!in_array($user_id,$this->users)) {
      $this->users[] = $user_id;
      $class = $this->get_entity_class('user',$user_id);
      $this->set_class('user',$user_id,$class);
    }
  }

  public function add_resource($resource_id) {
    if (!in_array($resource_id,$this->resources)) {
      $this->resources[] = $resource_id;
      $class = $this->get_entity_class('resource',$resource_id);
      $this->set_class('resource',$resource_id,$class);
    }
  }

  public function add_contact($contact_id) {
    if (!in_array($contact_id,$this->contacts)) {
      $this->contacts[] = $contact_id;
      $class = $this->get_entity_class('contact',$contact_id);
      $this->set_class('contact',$contact_id,$class);
    }
  }

  public function set_user_class($user_id, $class = null) {
    global $obm;
    if (!in_array($user_id,$this->users))
      return;
    if (($user_id==$obm['uid']) && (empty($class))) {
      $class='eventOwner';
    } elseif (empty($class)) {
      $class=$this->get_entity_class('user',$user_id);
    }
    $this->set_class('user',$user_id,$class);
  }

  public function set_resource_class($resource_id, $class = null) {
    global $obm;
    if (!in_array($resource_id,$this->resources))
      return;
    if (empty($class)) {
      $class = $this->get_entity_class('resource',$resource_id);
    }
    $this->set_class('resource',$resource_id,$class);
  }

  public function set_entity_class($entity, $entity_id, $class = null) {
    call_user_func(array($this,"set_{$entity}_class"),$entity_id, $class);
    $this->save_color_entity($entity,$entity_id,$class);
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

  public function remove_contact($contact_id) {
    $old_count = count($this->contacts);
    $this->contacts = array_diff($this->contacts,array($contact_id));
    if (count($this->contacts)<$old_count)
      $this->remove_class('contact',$contact_id);
  }

  public function remove_all() {
    $this->group = $GLOBALS['c_all'];
    $this->users = array();
    $this->resources = array();
    $this->contacts = array();
    $this->init_classes();
  }

  public function init_classes() {
      $this->classes = array(
      'by_entity' => array(
        'user' => array(),
        'resource' => array(),
        'contact' => array()
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
    return $this->insert();
  }
  
  public function save_color_entity($entity,$entity_id,$class) {
    $entity_id = of_entity_get($entity,$entity_id);
    $id_class = strtok($class,"eventOwner");
    if($id_class == ""){
      $id_class = "NULL";
    }
    $update = $this->update_color_entity($entity_id,$id_class);
    if(!$update){
      $this->insert_color_entity($entity_id,$id_class);
    }
    return $update;
  }

  public function load_color_entity(){
    $this->color_entity = $this->select_color_entity();
  }

  protected function get_entity_class($entity,$id){
    global $obm;
    if(isset($this->entity_id_entity[$entity][$id])){
      $entity_id = $this->entity_id_entity[$entity][$id];
    } else {
      $entity_id = of_entity_get($entity,$id);
      $this->entity_id_entity[$entity][$id] = $entity_id;
    }
    if(isset($this->color_entity[$entity_id])){
      $class ="eventOwner{$this->color_entity[$entity_id]}";
    }
    if(!isset($class)){
      $class = (($entity=='user' && $id==$obm['uid']) ? 'eventOwner' : $this->generate_class());
    }
    return $class;
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
      AND userobmpref_id $id
      ";
    display_debug_msg($query, $cdg_sql, 'CalendarView::delete()');
    $obm_q->query($query);

    $message = "\"obmbookmark_id\": \"{$this->id}\"";

    return $message;
  }

  public function useCalendarTimeForCreationWithTemplate() {
    return $this->cal_range != 'month';
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
    if (!empty($this->entity_id_entity)) {
      $calendar_bookmarkproperties['entity_id_entity'] = serialize($this->entity_id_entity);
    }
    if (!empty($this->contacts)) {
      $calendar_bookmarkproperties['contacts'] = implode(',', $this->contacts);
      $calendar_bookmarkproperties['contact_classes'] = serialize($this->classes['by_entity']['contact']);
    }

    $user_id = sql_parse_id($obm['uid']);
    // Insert ObmBookmark
    $query = "INSERT INTO ObmBookmark(
      obmbookmark_user_id,
      obmbookmark_label,
      obmbookmark_entity) VALUES (
        $user_id,
        '".$obm_q->escape($this->label)."',
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
          '".$obm_q->escape($value)."')";
      display_debug_msg($query, $cdg_sql, 'CalendarView::insert()');
      $obm_q->query($query);
    }
  }

  protected function select_color_entity(){
    global $obm, $cdg_sql;

    $obm_q = new DB_OBM;

    $user_id = sql_parse_id($obm['uid']);

    $query = "SELECT entity_id, eventowner
      FROM calendarcolor
      WHERE user_id = $user_id
      ";
    $obm_q->query($query);
    display_debug_msg($query, $cdg_sql, 'CalendarView::select_color_entity()');
    while($obm_q->next_record()){
      $entity_id = $obm_q->f("entity_id");
      $entity[$entity_id] = $obm_q->f("eventowner");
    }
    return $entity;
  }

  protected function insert_color_entity($entity_id,$id_class){
    global $obm, $cdg_sql;

    $obm_q = new DB_OBM;

    $user_id = sql_parse_id($obm['uid']);
    // Insert color calendar entity
    $query = "INSERT INTO calendarcolor(
      user_id,
      entity_id,
      eventowner
    ) VALUES (
      $user_id,
      $entity_id,
      $id_class
    )";
    display_debug_msg($query, $cdg_sql, 'CalendarView::insert_color_entity()');
    $obm_q->query($query);
  }

  protected function update_color_entity($entity_id,$id_class){
    global $obm, $cdg_sql;

    $obm_q = new DB_OBM;

    $user_id = sql_parse_id($obm['uid']);
    // Update color calendar entity
    $query = "UPDATE calendarcolor
      SET eventowner = $id_class
      WHERE user_id = $user_id
      AND entity_id = $entity_id
    ";
    display_debug_msg($query, $cdg_sql, 'CalendarView::update_color_entity()');
    $obm_q->query($query);
    return $obm_q->affected_rows();
  }

  protected function get_users_data($nb_max) {
    global $cdg_sql, $obm;

    $users = OBM_Acl_Utils::expandEntitiesArray(OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'read', $this->users));
    $u = $users['entity'];

    // Preserve the users order
    $data = array();
    foreach ($this->users as $id) {
      if (isset($u[$id]) && $nb_max>0) {
        $data[$id] = array(
          'name'  => $u[$id]['label'],
          'class' => $this->get_class($id,'user'),
          'type'  => 'user',
          'id'    => $id
        );
        $nb_max--;
      }
    }

    return $data;
  }

  protected function get_resources_data($nb_max) {
    global $cdg_sql, $obm;

    $resources = OBM_Acl_Utils::expandEntitiesArray(OBM_Acl::getAllowedEntities($obm['uid'], 'resource', 'read', $this->resources));
    $r = $resources['entity'];

    // Preserve the resources order
    $data = array();
    foreach ($this->resources as $id) {
      if (isset($r[$id]) && $nb_max>0) {
        $data[$id] = array(
          'name'  => $r[$id]['label'],
          'class' => $this->get_class($id,'resource'),
          'type'  => 'resource',
          'id'    => $id
        );
        $nb_max--;
      }
    } 

    return $data;
  }

  protected function get_contacts_data($nb_max) {
    global $cdg_sql, $obm;

    // Preserve the resources order
    $data = array();
    foreach ($this->contacts as $id) {
      $contact = OBM_Contact::get($id);
      $data[$id] = array(
        'name'  => $contact->display_name(),
        'class' => $this->get_class($id,'contact'),
        'type'  => 'contact',
        'id'    => $id
      );
    } 

    return $data;
  }

  protected function get_group_data() {
    $groups = run_query_get_allowed_groups($sel_entity_id['group']);
    return $groups['entity'];
  }

  public function get_class($id,$entity) {
    $c = $this->classes['by_entity'][$entity][$id];
    if (empty($c))
      $c = $this->get_entity_class($entity, $id);
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

  protected function modify() {
    $this->id = null;
    $this->entity_id = null;
    $this->label = null;
  }
}

function run_query_calendar_no_mail($users) {
  $obm_q = new DB_OBM;
  foreach($users as $u_id) {
    $query = "SELECT userobmpref_value FROM UserObmPref WHERE  userobmpref_option = 'set_mail' AND ( userobmpref_user_id = $u_id OR ( userobmpref_user_id IS NULL AND userobmpref_option  NOT IN ( SELECT userobmpref_option FROM UserObmPref WHERE userobmpref_user_id = $u_id AND userobmpref_option = 'set_mail') ) )";
    $obm_q->query($query);
    $obm_q->next_record();
    if($obm_q->f('userobmpref_value') == 'yes') {
      return false;
    }
  }
  return true;
}

# Given a list of calendars, and a user id, returns the subset of calendars this 
# user can write to
function run_query_writable_calendars($obm_user_id, $selected_calendars) {
  $writable_calendars = array();
  foreach ($selected_calendars as $calendar){
	$calendar_user_id = $calendar['id'];
	# The calendar owner is always able to write on his own calendar
    if (OBM_Acl::canWrite($user_id, 'calendar', array($calendar_user_id)) || ($obm_user_id == $calendar_user_id)) {
	  array_push($writable_calendars, $calendar);
	}
  }
  return $writable_calendars;
}


function run_query_get_recurring_event_id_from_ext_id($ext_id, $obm_user_id) {
  global $cdg_sql;
  $obm_q = new DB_OBM;

  $query = "SELECT e.event_id AS event_id
        FROM Event e
        LEFT JOIN EventException ee
        ON e.event_id = ee.eventexception_child_id
        WHERE e.event_ext_id = '".$obm_q->escape($ext_id)."'
        AND e.event_owner = $obm_user_id
        AND ee.eventexception_parent_id IS NULL";
  display_debug_msg($query, $cdg_sql, 'run_query_calendar_detail()');
  $obm_q->query($query);

  $event_id = null;
  if ($obm_q->next_record()) {
    $event_id = $obm_q->f('event_id');
  }
  return $event_id;
}

# Get the user id from the email adress 
# return the user id or false 
function get_userid_from_mail($mail){
	list($left,$right) = split('@',$mail);
	
	$query = "SELECT userobm_id FROM UserObm INNER JOIN Domain ON userobm_domain_id = domain_id WHERE userobm_email #LIKE '$left' AND ( domain_name #LIKE '$right' OR domain_alias #LIKE '$right' OR domain_label #LIKE '$right' )";
	
	$obm_q = new DB_OBM;
	$obm_q->query($query);
	$obm_q->next_record();
	return ($obm_q->f('userobm_id')) ? $obm_q->f('userobm_id') : false ;
	

}

///////////////////////////////////////////////////////////////////////////////
// Retrieve user quota
///////////////////////////////////////////////////////////////////////////////
function run_query_calendar_user_quota($u_id) {
	global $cdg_sql;
	$obm_q = new DB_OBM;

    // document_privacy = 1 because every document attached to an event is private.
    // ATM quota is only related to private document in OBM (OBMFULL-3795).
	$query = "SELECT SUM(document_size)
			AS sum_size
			FROM Document
			WHERE document_usercreate = '".$obm_q->escape($u_id)."'
			AND document_privacy = 1";
	display_debug_msg($query, $cdg_sql, 'run_query_document_user_quota()');
	$obm_q->query($query);
	$obm_q->next_record();
	return $obm_q->f('sum_size');
}
?>
