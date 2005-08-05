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
      LEFT JOIN CalendarUser ON calendarevent_id = calendaruser_event_id
      LEFT JOIN CalendarCategory ON calendarevent_category_id = calendarcategory_id
      LEFT JOIN UserObm AS c ON calendarevent_usercreate = c.userobm_id
      LEFT JOIN UserObm AS u ON calendarevent_userupdate = u.userobm_id
    WHERE calendarevent_id = $param_event";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return events users
// Parameters: 
//   - $param_event
///////////////////////////////////////////////////////////////////////////////
function run_query_event_customers($param_event) {
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type;
  $query = "SELECT DISTINCT userobm_lastname,userobm_firstname,userobm_id,calendaruser_state,userobm_id
            FROM UserObm
	    LEFT JOIN CalendarUser ON calendaruser_user_id = userobm_id
            WHERE calendaruser_event_id = '$param_event'";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the name and first name of users
// Parameters: 
//   - $users : users id array, the event is assigned to 
///////////////////////////////////////////////////////////////////////////////
function run_query_get_user_name($users) {
  global $cdg_sql;

  $in = "'".implode ("','", $users)."'";

  $obm_q = new DB_OBM;
  $query = "SELECT userobm_lastname, userobm_firstname, userobm_id
            FROM UserObm
	    WHERE userobm_id IN ($in)";

  $query.= " ORDER BY userobm_id"; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Perform the concatenation of users and groups array in a users array 
// Parameters:
//   - $users_array : List of the users
//   - $groups_array : List of the groups 
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
// XXXXX???? Bad definition : Update a user decision for an user
// Parameters: 
//  -  $agenda : Agenda params
///////////////////////////////////////////////////////////////////////////////
function run_query_update_occurence_state($event_id,$user_id,$state){
  global $cdg_sql;
  $obm_db = new DB_OBM;
  $query = "UPDATE CalendarUser 
            SET calendaruser_state = '$state'
            WHERE calendaruser_user_id = $user_id
              AND calendaruser_event_id = '$event_id'";
  display_debug_msg($query, $cdg_sql);  
  $obm_db->query($query);
  return $obm_db;    
}


///////////////////////////////////////////////////////////////////////////////
// Select All waiting Events
///////////////////////////////////////////////////////////////////////////////
function run_query_waiting_events() {
  global $auth, $cdg_sql;
  
  $writable_user = run_query_userobm_writable();
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
      LEFT JOIN CalendarUser ON calendarevent_id  = calendaruser_event_id
      LEFT JOIN UserObm attendee ON calendaruser_user_id = attendee.userobm_id
      LEFT JOIN UserObm u ON calendarevent_usercreate = u.userobm_id
    WHERE calendaruser_state = 'W'";

  if (is_array($writable_user)) {
    $query .= " AND calendaruser_user_id IN (";
    $num = count($writable_user);
    foreach($writable_user as $user) {
      $query .= "'$user',";
    }
    $query.= "'".$auth->auth["uid"]."')";
  } else {
    $query .= " AND calendaruser_user_id = '".$auth->auth["uid"]."'";
  }
  $query .= " ORDER BY userobm_id"; 
  display_debug_msg($query, $cdg_sql);
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

  $obm_q = new DB_OBM;
  $e_id = $agenda["id"];
  $query = "DELETE FROM CalendarUser WHERE calendaruser_event_id = '$e_id'";
  $obm_q->query($query);
} 


///////////////////////////////////////////////////////////////////////////////
// Insert one event
// Parameters: 
//   - $agenda      : Agenda params
//   - $users_array : List of the users 
///////////////////////////////////////////////////////////////////////////////
function run_query_add_event($agenda,$users_array,&$event_id) {
  global $uid, $l_from, $l_to, $l_date;
  global $l_add_event_mail_head,$l_add_event_mail_subject;

  $writable_user = run_query_userobm_writable(); 
  $mail  = $agenda["mail"];
  $groups = $agenda["group"];
  $mail_title = stripslashes($agenda["title"]);
  $datebegin = $agenda["datebegin"];
  $dateend = $agenda["dateend"];
  
  if (is_array($groups)) {
    foreach($groups as $group_id) {
      $user_group_array = array_merge($user_group_array, get_all_users_from_group($group_id));
    }
    $users_array = array_merge($users_array,$user_group_array);
    $users_array = array_unique($users_array);
  }
  
  $event_id = run_query_insert_event_data($agenda);   
  foreach ($users_array as $user_id) {
    if ($user_id == $uid || in_array($user_id,$writable_user)) {
      run_query_insert_user_event($user_id,$event_id,'A');	
    } else {
      run_query_insert_user_event($user_id,$event_id,'W');	
    }
  }

  $subject = "$l_add_event_mail_subject" . $mail_title;
  $message = "$l_add_event_mail_head $mail_title $l_date : $l_from $datebegin $l_to $dateend";
  $recipient_array = array_diff($users_array, array($uid));
  $tmp_filename = secure_tmpname(".ics","ics_");
  print_r($users_array); 
  print_r($recipient_array); 
  echo "dis_export_meeting($agenda, $event_id, $tmp_filename)";
  dis_export_meeting($agenda, $event_id, $recipient_array, $tmp_filename);
  print_r($users_array); 
  print_r($recipient_array); 
  echo "dis_export_meeting($agenda, $event_id, $tmp_filename)";
  
  $h_file = array(
    array (
      "filename" => "$tmp_filename",
      "name" => "meeting.ics",
      "type" => "ics"
    )
  );
  send_mail($subject, $message, $recipient_array, "",$mail, $h_file);
  //unlink($tmp_filename);
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
  $all_day = $agenda["allday"]; 
  $datebegin = $agenda["datebegin"];
  $dateend = $agenda["dateend"];
  $event_duration = $agenda["event_duration"];
  if ($agenda["privacy"]!=1) $privacy = 0; else $privacy = 1; 
  $repeat_kind = $agenda["kind"];
  $repeat_days = $agenda["repeat_days"]; 
  $repeat_end = $agenda["repeat_end"]; 
  
  $obm_q = new DB_OBM;
  // XXXXXXXXXXXXXXxx
  $query = "SELECT MAX(calendarevent_id) as max_id FROM CalendarEvent";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record(); 
  $max_id = $obm_q->f("max_id")+1;

  $query = "INSERT INTO CalendarEvent (
    calendarevent_id,
    calendarevent_timecreate,                         
    calendarevent_usercreate,
    calendarevent_owner,
    calendarevent_title, 
    calendarevent_date,     
    calendarevent_description,
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
    '$uid',
    '$uid',
    '$title',
    '$datebegin',
    '$description',
    '$category_id',
    '$priority',
    '$privacy',
    '$event_duration',		
    '$repeat_kind',
    '1',
    '$repeat_days',
    '$all_day',
    '$repeat_end')";

  display_debug_msg($query, $cdg_sql);
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

  $obm_db = new DB_OBM;
  $query = "INSERT INTO CalendarUser (
    calendaruser_event_id, 
    calendaruser_user_id,
    calendaruser_required,
    calendaruser_state) 
  VALUES (
    '$event_id',
    '$user_id',
    '0',
    '$state')";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db; 
}


///////////////////////////////////////////////////////////////////////////////
// Check for conflict : Only with the first occurence of each event,
///////////////////////////////////////////////////////////////////////////////
function check_for_conflict($agenda,$sel_user_id) {
  
  $ts_begin = isodate_to_timestamp($agenda["datebegin"]);
  $ts_end = isodate_to_timestamp($agenda["dateend"]);
  $id = $agenda["id"];
  $obm_q = run_query_conflict_events($ts_begin,$ts_end,$sel_user_id,$id);
  if ($obm_q->nf() == 0) {
    return false;
  }
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Check for conflict : Only with the first occurence of each event
///////////////////////////////////////////////////////////////////////////////
function check_for_decision_conflict($agenda) {
  
  $user[] = $agenda["user_id"];
  $event_id = $agenda["id"];
  $event_data = run_query_detail($event_id);
  $ts_begin = $event_data->f("calendarevent_date");
  $ts_end = $event_data->f("calendarevent_date") + $event_data->f("calendarevent_duration");
  $obm_conflict = run_query_conflict_events($ts_begin,$ts_end,$user,$event_id);
  if ($obm_conflict->nf() == 0) {
    return false;
  }
  return $obm_conflict;
}


/////////////////////////////////////////////////////////////////////////
// Get all event categories
/////////////////////////////////////////////////////////////////////////
function run_query_get_eventcategories() {
  global $cdg_sql;

  $query = "SELECT * FROM CalendarCategory";
  display_debug_msg($query, $cdg_sql);
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
  $writable_user = run_query_userobm_writable(); 

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
	display_debug_msg($query, $cdg_sql);
	$retour = $obm_q->query($query);
      }
    }

    // If users have been inserted they must not appear in DeletedCalendarEvent
    if (count($new_users) > 0) {
      foreach($new_users as $u_id) {
	$query = "DELETE FROM DeletedCalendarEvent
      WHERE deletedcalendarevent_event_id = '$event_id'
        AND deletedcalendarevent_user_id = '$u_id'";
	display_debug_msg($query, $cdg_sql);
	$retour = $obm_q->query($query);
      }
    }
  }

  // XXXX bizarre les old_end et old_begin semblent etre affectes par l'update
  // ci-dessous sous mysql 4.0.23-10 (portable fourmi)
  run_query_update_event_data($agenda);  
  run_query_delete_user_event($agenda);
  foreach($users_array as $user_id) {
    if ($user_id == $uid || in_array($user_id,$writable_user)) {
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

  $now = date("Y-m-d H:i");
  $title = $agenda["title"];
  $category_id = $agenda["category"];
  $priority = $agenda["priority"];
  $description = $agenda["description"];
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
  display_debug_msg($query, $cdg_sql);
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
  $obm_q->query($query);
  $obm_q->next_record();
  $title = $obm_q->f("calendarevent_title");

  $query = "DELETE FROM CalendarException WHERE calendarexception_event_id = '$event_id'";
  $obm_q->query($query);
  $query = "DELETE FROM CalendarEvent WHERE calendarevent_id = '$event_id'";
  $obm_q->query($query);
  $query = "DELETE FROM CalendarUser WHERE calendaruser_event_id = '$event_id'";
  $obm_q->query($query);

  // If connectors in use
  if ($c_use_connectors) {
    $now = date("Y-m-d H:i:s");
    
    // We purge the individual user entries
    $query = "DELETE FROM DeletedCalendarEvent
      WHERE deletedcalendarevent_event_id = '$event_id'";
    display_debug_msg($query, $cdg_sql);
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
    display_debug_msg($query, $cdg_sql);
    $retour = $obm_q->query($query);
  }

  $message = $l_delete_event_mail_head.$title;   
  $subject = "$l_delete_event_mail_subject" . $title;
  $recipient_array = array_diff($users_array, array($uid));
  send_mail($subject, $message, $recipient_array, "", $mail);
}


///////////////////////////////////////////////////////////////////////////////
// Get active Users or archived with rights set with their permissions
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_right($uid) {
  global $cdg_sql;

  $query = "
    SELECT userobm_id,
      calendarright_customerid,
      u.userobm_lastname, u.userobm_firstname,
      c.calendarright_write, c.calendarright_read 
    FROM CalendarRight as c
    LEFT JOIN UserObm as u
    ON c.calendarright_ownerid = '$uid'
          AND c.calendarright_customerid = userobm_id 
    WHERE userobm_id != '$uid'
      and userobm_archive != '1'
      and (c.calendarright_write != '0' OR  c.calendarright_read != '0')
    ORDER BY u.userobm_lastname";

  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}  


///////////////////////////////////////////////////////////////////////////////
// Get the list of default agenda users for the current group
///////////////////////////////////////////////////////////////////////////////
function get_default_group_user_ids($p_group) {
  global $cdg_sql, $auth, $agenda, $c_all;

  $uid = $auth->auth["uid"];
  $group = $p_group;
  if ($p_group != $c_all) {
    $query = "SELECT u.*,
      c.calendarright_read
    FROM UGroup g, UserObmGroup ug, UserObm as u
      LEFT OUTER JOIN CalendarRight as c
        ON c.calendarright_customerid = '$uid'
          AND u.userobm_id = c.calendarright_ownerid
    WHERE
      g.group_id = '$group' AND
      ug.userobmgroup_group_id = g.group_id AND
      ug.userobmgroup_userobm_id = u.userobm_id AND
      (c.calendarright_read = 1 OR u.userobm_id='$uid')
    ORDER BY u.userobm_lastname";
  } else {
    $query = "SELECT u.*,
        c.calendarright_read
      FROM UserObm as u
        LEFT OUTER JOIN CalendarRight as c
          ON c.calendarright_customerid = '$uid'
            AND u.userobm_id = c.calendarright_ownerid
      WHERE (c.calendarright_read = 1 OR u.userobm_id='$uid')
      ORDER BY u.userobm_lastname";
  }

  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);

  $ret_array = array();
  while($obm_db->next_record()) {
    $ret_array[] = $obm_db->f("userobm_id");
  }
  if (count($ret_array) == 0) {
    $ret_array = array($uid);
  }

  return $ret_array;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable agenda users for the current user
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_readable() {
  global $cdg_sql, $auth, $agenda, $c_all;

  $uid = $auth->auth["uid"];

  if (isset($agenda["agenda_group"]) && ($agenda["agenda_group"] != $c_all)) {
    $group = $agenda["agenda_group"];
    $query = "SELECT u.*,
        c.calendarright_read
      FROM UGroup g, UserObmGroup ug, UserObm as u
        LEFT OUTER JOIN CalendarRight as c
          ON c.calendarright_customerid = '$uid'
            AND u.userobm_id = c.calendarright_ownerid
      WHERE
        g.group_id = '$group' AND
        ug.userobmgroup_group_id = g.group_id AND
        ug.userobmgroup_userobm_id = u.userobm_id AND
        (c.calendarright_read = 1 OR u.userobm_id='$uid')
      ORDER BY u.userobm_lastname";
  } else {
    $query = "SELECT u.*,
        c.calendarright_read
      FROM UserObm as u
        LEFT OUTER JOIN CalendarRight as c
          ON c.calendarright_customerid = '$uid'
            AND u.userobm_id = c.calendarright_ownerid
      WHERE (c.calendarright_read = 1 OR u.userobm_id='$uid')
      ORDER BY u.userobm_lastname";
  }

  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);

  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of calendar group for the current user
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_group() {
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
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of writable agenda users for the current user
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_writable() {
  global $cdg_sql,$auth;

  $uid = $auth->auth["uid"];
  $query = "SELECT u.userobm_id,
      c.calendarright_write
    FROM UserObm as u
      LEFT OUTER JOIN CalendarRight as c 
        ON u.userobm_id = c.calendarright_ownerid
          AND c.calendarright_customerid = '$uid'
    WHERE (c.calendarright_write = 1 OR u.userobm_id='$uid') 
    ORDER BY u.userobm_lastname";

  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql);	    
  $obm_db->query($query);
  $ret_array = array();
  while($obm_db->next_record()) {
    $ret_array[] = $obm_db->f("userobm_id");
  }
  return $ret_array;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of writable agenda users for the current user
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_label_writable() {
  global $cdg_sql,$auth;

  $uid = $auth->auth["uid"];
  $query = "SELECT u.userobm_id,u.userobm_lastname,u.userobm_firstname,
      c.calendarright_write
    FROM UserObm as u
      LEFT OUTER JOIN CalendarRight as c 
        ON u.userobm_id = c.calendarright_ownerid
          AND c.calendarright_customerid = '$uid'
    WHERE (c.calendarright_write = 1 OR u.userobm_id='$uid') 
    ORDER BY u.userobm_lastname";

  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql);	    
  $obm_db->query($query);
  $ret_array = array();
  while($obm_db->next_record()) {
       $ret_array[] = array("id"=>$obm_db->f("userobm_id"),
       "firstname" => $obm_db->f("userobm_firstname"),
       "lastname" => $obm_db->f("userobm_lastname"));
  }
  return $ret_array;
}


///////////////////////////////////////////////////////////////////////////////
// Get groups infos from the gid array given
// Parameters:
//   - groups : array of groups id
///////////////////////////////////////////////////////////////////////////////
function run_query_group_in($groups) {
  global $cdg_sql;

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
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, "run_query_group_in()");

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
  display_debug_msg($query, $cdg_sql);	    
  $obm_db->query($query);
  return $obm_db;
}


/////////////////////////////////////////////////////////////////////////////
// Update the list of the rights give to users. 
//////////////////////////////////////////////////////////////////////////////
function run_query_update_right($agenda) {
  global $cdg_sql,$auth;  

  $uid = $agenda["user_id"];
  $accept_write = $agenda["accept_w"];
  $accept_read = $agenda["accept_r"];
  
  if($uid != $auth->auth["uid"]){
    $writable_user = run_query_userobm_writable();  
    if(!in_array($uid,$writable_user)) 
      return 0;
  }
  $obm_db = new DB_OBM;
  $query = "DELETE From CalendarRight where  calendarright_ownerid = '$uid'";
  display_debug_msg($query, $cdg_sql);	    
  $obm_db->query($query);
  
  if (is_array($accept_write)) {
    foreach ($accept_write as $key => $id) {
      $query = "INSERT INTO CalendarRight VALUES('$uid','$id',1,0)";
      display_debug_msg($query, $cdg_sql);	    
      $obm_db->query($query);
    }
  }
  if (is_array($accept_read)) {
    foreach($accept_read as $key => $id) {
      $query = "UPDATE CalendarRight SET calendarright_read = 1
        WHERE calendarright_ownerid = '$uid'
          AND calendarright_customerid = '$id'";
      display_debug_msg($query, $cdg_sql);	    
      $obm_db->query($query);
      if ($obm_db->affected_rows() == 0) {
	$query = "INSERT INTO CalendarRight VALUES('$uid','$id',0,1)";
      	display_debug_msg($query, $cdg_sql);	    
       	$obm_db->query($query);
      }
    } 
  }
}


///////////////////////////////////////////////////////////////////////////////
// Category agenda query execution                                           //
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_agendacategory() {
  global $cdg_sql;

  $query = "select * from CalendarCategory order by calendarcategory_label";
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
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

  
/////////////////////////////////////////////////////////////////////////////
// Return if the event is valid or not. It permit to keep only valid event in
// a table.
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    $status_event : status of a event
/////////////////////////////////////////////////////////////////////////////
function valid_event($status_event) {
  return ($status_event != -1);
  
}
 
///////////////////////////////////////////////////////////////////////////////
// Return tables of hashed users.
// Parameters:
//   - $user_q : Userobm DBO
/////////////////////////////////////////////////////////////////////////////
function store_users($user_q) {
  global $ico_calendar_user0,$ico_calendar_user1,$ico_calendar_user2,$ico_calendar_user3,$ico_calendar_user4;
  global $ico_calendar_user5;

  $i = 0;
  while($user_q->next_record()) {
    $user_tab[$user_q->f("userobm_id")] = array(
      "name"=>$user_q->f("userobm_firstname")." ".$user_q->f("userobm_lastname"),
      "class"=>"agendaEventBg$i",
      "image"=>${"ico_calendar_user".$i},
      "id"=>$user_q->f("userobm_id"));
    $i++;
  }
  return $user_tab;
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

  display_debug_msg($query, $cdg_sql);
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

  $query = "SELECT calendaruser_user_id
    FROM CalendarUser
    WHERE calendaruser_event_id = '$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  while ($obm_q->next_record()) {
    $users_array[] = $obm_q->f("calendaruser_user_id");
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
//------------------------------------------------------------------------
// Argument:
// ---------
//    - $format : format of the wished result
//    - $timestamp : time to format
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
// Slice the number of user
//------------------------------------------------------------------------
// Argument:
// ---------
//     - $sel_user
///////////////////////////////////////////////////////////////////////////////
function slice_user($sel_user_id) {
  if($action != "perform_meeting" && count($sel_user_id) > 6) {
    $sel_user_id = array_slice ($sel_user_id, 0, 6);
  }
  return $sel_user_id;
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
//   - $start_time  : timestamp of start date
//   - $end_time    : timestamp of end date
//   - $users_array : users id array, the event is assigned to 
///////////////////////////////////////////////////////////////////////////////
function run_query_no_repeat_events($start,$end,$users_array) {
  global $cdg_sql, $uid;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date");
  $calendarevent_date_l = sql_date_format($db_type,"calendarevent_date","calendarevent_date");

  $query = "SELECT
      calendarevent_id,
      calendarevent_title,
      calendarevent_privacy,
      calendarevent_description, 
      calendaruser_user_id,
      calendarcategory_label,
      $calendarevent_date_l,
      calendarevent_duration,
      calendarevent_allday
    FROM CalendarEvent, CalendarCategory, CalendarUser 
    WHERE calendarevent_category_id = calendarcategory_id
      AND calendarevent_id = calendaruser_event_id
      AND calendaruser_state = 'A'
      AND calendarevent_repeatkind = 'none'
      AND $calendarevent_date >= $start - calendarevent_duration
      AND $calendarevent_date <= $end 
";	

  if (is_array($users_array) && (count($users_array)>0) ) {
    $query .= " AND calendaruser_user_id IN (";
    foreach($users_array as $id => $value) {
      $query.= "$coma'$id'";
      $coma = ",";
    }
    $query.=")";
  }   	     	    
  $query.=" ORDER BY calendarevent_date"; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a week of users or/and groups
// Parameters:
//   - $start        : event start datetime (timestamp)
//   - $end          : event end datetime (timestamp)
//   - $agenda       : agenda params
//   - $users_array  : contact id array, the event is assigned to 
//   - $groups_array : group id array, the event is assigned to 
///////////////////////////////////////////////////////////////////////////////
function run_query_conflict_events($start,$end,$users_array,$event_id) {
  global $cdg_sql, $uid;
  
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type;
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date");
  $calendarevent_date_l = sql_date_format($db_type,"calendarevent_date","calendarevent_date");

  $query = "SELECT
      calendarevent_id,
      calendarevent_title,
      calendarevent_privacy,
      calendarevent_description, 
      calendaruser_user_id,
      userobm_lastname,
      userobm_firstname,
      calendarcategory_label,
      $calendarevent_date_l,
      calendarevent_duration,
      calendarevent_allday
    FROM CalendarEvent
      LEFT JOIN CalendarCategory ON calendarcategory_id = calendarevent_category_id
      LEFT JOIN CalendarUser ON calendaruser_event_id = calendarevent_id
      LEFT JOIN UserObm ON calendaruser_user_id = userobm_id
    WHERE calendarevent_category_id = calendarcategory_id
      AND calendarevent_id = calendaruser_event_id
      AND calendaruser_state = 'A'
      AND $calendarevent_date > $start - calendarevent_duration
      AND $calendarevent_date < $end 
            ";	
  if (is_array($users_array) && (count($users_array)>0) ) {
    $query .= " AND calendaruser_user_id IN (";
    foreach($users_array as $id) {
      $query.= "$coma'$id'";
      $coma = ",";
    }
    $query.=")";
  }   	     
  if($event_id != "") {
    $query .= " AND calendarevent_id != '$event_id' ";
  }
  $query.=" ORDER BY calendarevent_date";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}

///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a week of users or/and groups
// Parameters:
//   - $agenda       : agenda params
//   - $users_array  : contact id array, the event is assigned to 
//   - $groups_array : group id array, the event is assigned to 
///////////////////////////////////////////////////////////////////////////////
function run_query_repeat_events($start,$end,$users_array) {
  global $cdg_sql, $uid;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $calendarevent_endrepeat = sql_date_format($db_type,"calendarevent_endrepeat");
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date");
  $calendarevent_date_l = sql_date_format($db_type,"calendarevent_date","calendarevent_date");
  $calendarevent_endrepeat_l = sql_date_format($db_type,"calendarevent_endrepeat","calendarevent_endrepeat");

  $query = "SELECT
      calendarevent_id,
      calendarevent_title,
      calendarevent_privacy,
      calendarevent_description, 
      calendarcategory_label,
      $calendarevent_date_l,
      calendarevent_duration,
      calendarevent_repeatkind,
      $calendarevent_endrepeat_l,
      calendarevent_repeatfrequence,
      calendaruser_user_id,
      calendarevent_repeatdays,
      calendarevent_allday
    FROM CalendarEvent, CalendarCategory, CalendarUser 
    WHERE calendarevent_category_id = calendarcategory_id
      AND calendarevent_id = calendaruser_event_id
      AND calendaruser_state = 'A'
      AND calendarevent_repeatkind != 'none'
      AND $calendarevent_date <= $end 
      AND ($calendarevent_endrepeat >= $start - calendarevent_duration
           OR $calendarevent_endrepeat = '0')
";		  

  if (is_array($users_array) && (count($users_array)>0) ) {
    $query .= " AND calendaruser_user_id IN (";
    foreach($users_array as $id => $value) {
      $query.= "$coma'$id'";
      $coma = ",";
    }
    $query .= ")";
  }    	    
  $query.=" ORDER BY calendarevent_date";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return a table of Events between two dates
// Parameters: 
//   - $start_time    : timestamp of start date
//   - $end_time      : timestamp of end date
//   - $calendar_user :
///////////////////////////////////////////////////////////////////////////////
function events_model($start_time, $end_time, $calendar_user) {
  global $day_duration;

  $day_duration = 86400;
  $obm_no_repeat = run_query_no_repeat_events($start_time,$end_time,$calendar_user);
  while ($obm_no_repeat->next_record()) {
    $id = $obm_no_repeat->f("calendarevent_id");
    $title = $obm_no_repeat->f("calendarevent_title");
    $privacy = $obm_no_repeat->f("calendarevent_privacy");
    $description = $obm_no_repeat->f("calendarevent_description"); 
    $category = $obm_no_repeat->f("calendarcategory_label");
    $date = $obm_no_repeat->f("calendarevent_date");
    $duration = $obm_no_repeat->f("calendarevent_duration");
    $user_id = $obm_no_repeat->f("calendaruser_user_id");
    $all_day = $obm_no_repeat->f("calendarevent_allday");    
    $event = &new Event($id,$duration,$title,$category,$privacy,$description);
    store_event($date, $event, $day_events, $user_id, $all_day, $end_time);
  }
  
  $obm_repeat = run_query_repeat_events($start_time,$end_time,$calendar_user);
   
  while ($obm_repeat->next_record()) {
    $id = $obm_repeat->f("calendarevent_id");
    $title = $obm_repeat->f("calendarevent_title");
    $privacy = $obm_repeat->f("calendarevent_privacy");
    $description = $obm_repeat->f("calendarevent_description"); 
    $category = $obm_repeat->f("calendarcategory_label");
    $date = $obm_repeat->f("calendarevent_date");
    $duration = $obm_repeat->f("calendarevent_duration");
    $repeatkind = $obm_repeat->f("calendarevent_repeatkind");
    $endrepeat = $obm_repeat->f("calendarevent_endrepeat");
    $all_day = $obm_no_repeat->f("calendaruser_allday");     
    if (!$endrepeat) {
      $endrepeat = $end_time;
    }
    $repeatfrequence = $obm_repeat->f("calendarevent_repeatfrequence");
    $repeatdays = $obm_repeat->f("calendarevent_repeatdays");
    $user_id = $obm_repeat->f("calendaruser_user_id");    
    $all_day = $obm_repeat->f("calendarevent_allday");       
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
