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
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type; 
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
      calendarevent_title, 
      calendarevent_description,
      calendarevent_category_id,
      calendarcategory_label,
      calendarevent_privacy,
      calendarevent_priority,
      calendarevent_repeatkind,
      calendarevent_duration,
      calendarevent_repeatdays,
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
  $obm_db->query($query);
  $obm_db->next_record();
  return $obm_db;
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
            WHERE  calendaruser_event_id = '$param_event'";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}

///////////////////////////////////////////////////////////////////////////////
// Return events users
// Parameters: 
//   - $param_event
///////////////////////////////////////////////////////////////////////////////
function run_query_event_groups($param_event,$getdate) {
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type;
  $getdate = format_to_iso($db_type,$getdate);
  $query = "SELECT DISTINCT group_id, group_name,calendarsegment_state
            FROM UGroup, CalendarSegment
            WHERE  calendarsegment_eventid = '$param_event'
	    AND calendarsegment_date LIKE '$getdate%'
	    AND calendarsegment_customerid = group_id
	    AND calendarsegment_type = 'group'";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Return events users array
// Parameters: 
//   - $param_event
///////////////////////////////////////////////////////////////////////////////
function run_query_event_customers_array($param_event) {
  $obm_db = new DB_OBM;
  $db_typ = $obm_db->type;
  $query = "SELECT DISTINCT userobm_id
            FROM UserObm, CalendarUser
            WHERE  calendaruser_event_id = '$param_event'
	    AND calendaruser_user_id = userobm_id";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  while($obm_db->next_record()) {
    $p_arrayuserobm_id[] = $obm_db->f("userobm_id");
  }
  return $p_arrayuserobm_id;
}

///////////////////////////////////////////////////////////////////////////////
// Return events groups array
// Parameters: 
//   - $param_event
///////////////////////////////////////////////////////////////////////////////
function run_query_event_groups_array($param_event,$getdate) {
  $obm_db = new DB_OBM;
  $getdate = format_to_iso($db_type,$getdate);
  $query = "SELECT DISTINCT group_id
            FROM UGroup, CalendarSegment
            WHERE  calendarsegment_eventid = '$param_event'
            AND calendarsegment_date LIKE '$getdate%'
	    AND calendarsegment_customerid = group_id
	    AND calendarsegment_type = 'group'";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  while($obm_db->next_record()) {
    $p_arraygroup_id[] = $obm_db->f("group_id");
  }
  return $p_arraygroup_id;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the name and first name of users
// Parameters: 
//   - $contacts_array : contact id array, the event is assigned to 
///////////////////////////////////////////////////////////////////////////////
function run_query_get_user_name($contacts_array) {
  global $cdg_sql;
  
  $obm_db = new DB_OBM;
  $query = "SELECT userobm_lastname,userobm_firstname,userobm_id
            FROM UserObm
	    WHERE userobm_id IN (".$contacts_array[0];
  for ($i=1;$i<count($contacts_array);$i++) {
    $query.= ",'".$contacts_array[$i]."'";
  }
  $query.= ")";        
  $query.= " ORDER BY userobm_id"; 
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Perform the addiction of a event
// Parameters: 
//   - $agenda : Agenda params
//   - $contacts_array : List of the users 
///////////////////////////////////////////////////////////////////////////////
function run_query_add_event($agenda,$contacts_array,&$event_id) {
  global $auth, $l_add_event_mail_head,$l_add_event_mail_subject,$l_event_mail_body,$l_from,$l_to;

  $writable_user = run_query_userobm_writable(); 
  preg_match ("/^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})/",$agenda["date_begin"] , $day_array);  
  preg_match ("/^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})/",$agenda["date_end"] , $day_array2);    
  $mail  = $agenda["mail"];
  $groups = $agenda["group"];
  $mail_title = stripslashes($agenda["title"]);

  if (is_array($groups)) {
    foreach($groups as $group_id) {
      $user_group_array = array_merge($user_group_array, get_all_users_from_group($group_id));
    }
    $contacts_array = array_merge($contacts_array,$user_group_array);
    $contacts_array = array_unique($contacts_array);
  }
  
  $event_id = run_query_insert_event_data($agenda);   
  foreach ($contacts_array as $user_id) {
    if ($user_id == $auth->auth["uid"] || in_array($user_id,$writable_user)) {
      run_query_insert_user_event($user_id,$event_id,'A');	
    } else {
      run_query_insert_user_event($user_id,$event_id,'W');	
    }
  }
  $subject = "$l_add_event_mail_subject" . $mail_title;
  $message = $l_add_event_mail_head . $mail_title . "\n"
    .$l_event_mail_body.$l_from." ".$day_array[1]."-".$day_array[2]."-".$day_array[3]." @ ".$day_array[4].":".$day_array[5]." "
    .$l_to." ".$day_array2[1]."-".$day_array2[2]."-".$day_array2[3]." @ ".$day_array2[4].":".$day_array2[5];      
  send_mail($subject, $message, $contacts_array,"",$mail);
}


///////////////////////////////////////////////////////////////////////////////
// TODO : Implemet The conflict detection
///////////////////////////////////////////////////////////////////////////////
// Insert a user decision for an user
// Parameters:
//   - $agenda : Agenda params
///////////////////////////////////////////////////////////////////////////////
function run_query_insert_decision($agenda) {
  global $auth;

  $return_info = array();
  $user = $agenda["user_id"];
  $event_id = $agenda["id"];
  $force = $agenda["force"];
  $state = $agenda["decision_event"];
  run_query_update_occurence_state($event_id,$user,$state,$conflicts);
}
///////////////////////////////////////////////////////////////////////////////
// Get users from the array passes
// Parameters:
//   - users : array of users id
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_in($users) {
  global $cdg_sql;
  $where = "(0";
  foreach($users as $user) {
    $where .= ", $user";
  }
  $where .= ")";
  $query = "select *
    from UserObm
    where userobm_archive = 0
    AND userobm_id IN $where 
    order by userobm_lastname"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, "run_query_userobm()");

  return $obm_q;

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
  $query = "SELECT userobm_id,
		   userobm_lastname,
		   userobm_firstname,
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
            FROM CalendarEvent LEFT JOIN CalendarCategory ON calendarevent_category_id = calendarcategory_id
	    LEFT JOIN CalendarUser ON calendarevent_id  = calendaruser_event_id
	    LEFT JOIN UserObm ON calendaruser_user_id = userobm_id
	    WHERE calendaruser_state = 'W'";

  if(is_array($writable_user)) {
    $query .= " AND calendaruser_user_id IN (";
    $num = count($writable_user);
    foreach($writable_user as $user) {
      $query.= "'$user',";
    }
    $query.="'".$auth->auth["uid"]."')";
  }
  else {
    $query.="AND calendaruser_user_id = '".$auth->auth["uid"]."'";
  }
  $query .= "ORDER BY userobm_id"; 
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Insert a occurence of a event
// Parameters: 
//   - $date_begin : 
//   - $date_end : 
//   - $user_id :
//   - $event_id :
//   - $state :
////////////////////////////////////////////////////////////////////////////
function run_query_insert_user_event($user_id,$event_id,$state){
  global $cdg_sql;

  $obm_db = new DB_OBM;
  $query = "INSERT INTO CalendarUser (
    calendaruser_event_id, 
    calendaruser_user_id,
    calendaruser_state) 
            VALUES (
    $event_id,
    $user_id,
    '$state')";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db; 
}

/////////////////////////////////////////////////////////////////////////////
// Delete all events of a evenements, and the event
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    - $agenda
/////////////////////////////////////////////////////////////////////////////
function run_query_delete_user_event($agenda) {
  global $cdg_sql,$l_delete_event_mail_head,$l_delete_event_mail_subject,$l_delete_event_mail_body,$l_to,$l_from;

  $obm_db = new DB_OBM;
  $event_id = $agenda["id"];
  $query = "DELETE FROM CalendarUser WHERE calendaruser_event_id = $event_id";
  $obm_db->query($query);
} 

/////////////////////////////////////////////////////////////////////////////
// Insert data of a event  Return the id of this event
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    - $agenda : hashed agenda params
/////////////////////////////////////////////////////////////////////////////
function run_query_insert_event_data($agenda) {
  global $cdg_sql,$auth;

  $title = $agenda["title"];
  $category_id = $agenda["category"];
  $priority = $agenda["priority"];
  $description = $agenda["description"];
  preg_match ("/^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})/",$agenda["date_begin"] , $begin_array);      
  $stamp_beg = mktime($begin_array[4],$begin_array[5],00,$begin_array[2],$begin_array[3],$begin_array[1] );
  preg_match ("/^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})/",$agenda["date_end"] , $end_array);        
  $stamp_end = mktime($end_array[4],$end_array[5], 00,$end_array[2],$end_array[3],$end_array[1] );
  
  $duration = $stamp_end - $stamp_beg;
  
  if($agenda["privacy"]!=1) $privacy = 0;else $privacy = 1; 
  
  $repeat_kind = $agenda["kind"];
  $repeat_days = $agenda["repeat_days"]; 
  preg_match ("/^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})/",$agenda["date_begin"] , $repeat_array);        
  $stamp_repeat = mktime($repeat_array[4],$repeat_array[5],00,$repeat_array[2],$repeat_array[3],$repeat_array[1] );
  
  if($repeat_end && $repeat_kind != "none") {
    $repeat_end = $repeat_end;
  }
  elseif($repeat_kind != "none") {
    $repeat_end = strtotime("+1 year");
  } 
  else{
    $repeat_end = "";
  }
  
  $obm_db = new DB_OBM;
  $query = "SELECT MAX(calendarevent_id) as max_id FROM CalendarEvent";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  $obm_db->next_record(); 
  $max_id = $obm_db->f("max_id")+1;
  $query = "INSERT INTO CalendarEvent (
    calendarevent_id,calendarevent_timecreate,                         
    calendarevent_usercreate, 
    calendarevent_title, 
    calendarevent_date,     
    calendarevent_description,
    calendarevent_category_id,
    calendarevent_priority,
    calendarevent_privacy, 
    calendarevent_duration,
    calendarevent_repeatkind,
    calendarevent_repeatdays,
    calendarevent_endrepeat)
	         VALUES (
    $max_id,
    '".date("Y-m-d H:i")."',
    ".$auth->auth["uid"].",
    '$title',
    '".date("Y-m-d H:i",$stamp_beg)."',
    '$description',
    $category_id,
    $priority,
    $privacy,
    $duration,		
    '$repeat_kind',
    '$repeat_days',
    '".date("Y-m-d H:i",$repeat_end)."')";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);

  return $max_id;
}

/////////////////////////////////////////////////////////////////////////
// Get all event categories
/////////////////////////////////////////////////////////////////////////
function run_query_get_eventcategories() {
  global $cdg_sql;

  $query = "SELECT * FROM CalendarCategory";
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  return $obm_db;
}
/////////////////////////////////////////////////////////////////////////////
// Perform the modification of a event
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    - $agenda : Agenda params
//    - $contacts_array : List of the users 
/////////////////////////////////////////////////////////////////////////////
function run_query_modify_event($agenda,$contacts_array,&$event_id) {
  global $auth,$l_update_event_mail_head,$l_update_event_mail_subject,$l_event_mail_body,$l_update_event_mail_body,$l_to,$l_from;

  $writable_user = run_query_userobm_writable(); 
  preg_match ("/^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})/",$agenda["date_begin"] , $day_array);  
  preg_match ("/^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})/",$agenda["date_end"] , $day_array2);   
  
  $repeat_update = $agenda["repeat_update"];
  $event_id = $agenda["id"];
  $groups = $agenda["group"];
  $mail = $agenda["mail"];
  
  $mail_title = stripslashes($agenda["title"]);

  if(is_array($groups)) {
    foreach($groups as $group_id) {
      $user_group_array = array_merge($user_group_array, get_all_users_from_group($group_id));
    }
    $contacts_array = array_merge($contacts_array,$user_group_array);
    $contacts_array = array_unique($contacts_array);
  }
  
  $event_data = run_query_detail($event_id);
  run_query_update_event_data($agenda);  
  run_query_delete_user_event($agenda);
  foreach($contacts_array as $user_id) {
    if ($user_id == $auth->auth["uid"] || in_array($user_id,$writable_user)) {
      run_query_insert_user_event($user_id,$event_id,'A');	
    } else {
      run_query_insert_user_event($user_id,$event_id,'W');	
    }
  }
  $old_begin = date("YmdHi",$event_data->f("date"));
  $old_end = date("YmdHi",$event_data->f("calendarevent_date") + $event_data->f("calendarevent_duration"));
  if (($old_begin != $agenda["date_begin"] || $old_end != $agenda["date_end"]) ) {
    $subject = "$l_update_event_mail_subject" . $mail_title;
    $message = $l_update_event_mail_head. $mail_title . "\n".$l_event_mail_body.$l_from." "
	       .$day_array[1]."-".$day_array[2]."-".$day_array[3]." @ ".$day_array[4].":".$day_array[5]." "
	       .$l_to." ".$day_array2[1]."-".$day_array2[2]."-".$day_array2[3]." @ ".$day_array2[4].":".$day_array2[5]." ".
	       $l_update_event_mail_body.$l_from." "
	       .$day_array3[1]."-".$day_array3[2]."-".$day_array3[3]." @ ".$day_array3[4].":".$day_array3[5]." "
	       .$l_to." ".$day_array4[1]."-".$day_array4[2]."-".$day_array4[3]." @ ".$day_array4[4].":".$day_array4[5];         
    send_mail($subject, $message, $contacts_array, "",$mail);
  }
}

/////////////////////////////////////////////////////////////////////////////
// Perform the modification of a event
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    - $agenda : Agenda params
//    - $contacts_array : List of the users 
/////////////////////////////////////////////////////////////////////////////
function run_query_update_event_data($agenda) {
  global $cdg_sql,$auth;

  $title = $agenda["title"];
  $category_id = $agenda["category"];
  $priority = $agenda["priority"];
  $description = $agenda["description"];
  preg_match ("/^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})/",$agenda["date_begin"] , $begin_array);      
  $stamp_beg = mktime($begin_array[4],$begin_array[5],00,$begin_array[2],$begin_array[3],$begin_array[1] );
  preg_match ("/^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})/",$agenda["date_end"] , $end_array);        
  $stamp_end = mktime($end_array[4],$end_array[5], 00,$end_array[2],$end_array[3],$end_array[1] );
  $event_id = $agenda["id"]; 
  $duration = $stamp_end - $stamp_beg;
  
  if($agenda["privacy"]!=1) $privacy = 0;else $privacy = 1; 
  
  $repeat_kind = $agenda["kind"];
  $repeat_days = $agenda["repeat_days"]; 
  preg_match ("/^(\d{4})(\d{2})(\d{2})/",$agenda["repeat_end"] , $repeat_array);        
  $stamp_repeat = mktime(00,00,00,$repeat_array[2],$repeat_array[3],$repeat_array[1] );
  
  if($stamp_repeat && $repeat_kind != "none") {
    $stamp_repeat = $stamp_repeat;
  }
  elseif($repeat_kind != "none") {
    $stamp_repeat = strtotime("+1 year",date("Ymd"));
    
  } 
  else{
    $stamp_repeat = "";
  }
  
  $obm_db = new DB_OBM;
  $query = "
  UPDATE CalendarEvent SET
    calendarevent_timeupdate = '".date("Y-m-d H:i")."',                         
    calendarevent_usercreate = ".$auth->auth["uid"].", 
    calendarevent_title = '$title', 
    calendarevent_date = '".date("Y-m-d H:i",$stamp_beg)."',     
    calendarevent_description = '$description',
    calendarevent_category_id = '$category_id',
    calendarevent_priority = '$priority',
    calendarevent_privacy = '$privacy', 
    calendarevent_duration = '$duration',
    calendarevent_repeatkind = '$repeat_kind',
    calendarevent_repeatdays = '$repeat_days',
    calendarevent_endrepeat = '".date("Y-m-d H:i",$stamp_repeat)."'
  WHERE
    calendarevent_id = '$event_id'
  ";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);

}

/////////////////////////////////////////////////////////////////////////////
// Delete all event after the new begin date and update old end_repeat date
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    - $agenda
/////////////////////////////////////////////////////////////////////////////
function  run_query_update_deprecated_events($agenda) {
  global $cdg_sql,$auth;  

  $repeat_update = $agenda["repeat_update"];
  $old_event_id = $agenda["id"];
  $date_begin = mktime(substr($agenda["date_begin"],8,2), substr($agenda["date_begin"],10,2), "00", substr($agenda["date_begin"],4,2), substr($agenda["date_begin"],6,2), substr($agenda["date_begin"],0,4));
  $old_date_begin = mktime(substr($agenda["old_begin"],8,2), substr($agenda["old_begin"],10,2), "00", substr($agenda["old_begin"],4,2), substr($agenda["old_begin"],6,2), substr($agenda["old_begin"],0,4));  
  $old_date_end = mktime(substr($agenda["old_end"],8,2), substr($agenda["old_end"],10,2), "00", substr($agenda["old_end"],4,2), substr($agenda["old_end"],6,2), substr($agenda["old_end"],0,4));    
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type;
  $calendarsegment_date = sql_date_format($db_type,"calendarsegment_date");
  $query = "DELETE FROM CalendarSegment WHERE
                  calendarsegment_eventid = '$old_event_id' AND ($calendarsegment_date
		  ";
  if($repeat_update == 1) {
    $query .= ">= '$old_date_begin')";
    $query2 = "UPDATE CalendarEvent Set calendarevent_endrepeat = '".date("Y-m-d H:i",$date_begin)."', calendarevent_userupdate = ".$auth->auth["uid"].", calendarevent_timeupdate = '".date("Y-m-d H:i")."' WHERE calendarevent_id = $old_event_id";
  }else{
    $query .= "= '$old_date_begin' OR calendarsegment_date = '$old_date_end')";
  }
 
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
 
  display_debug_msg($query2, $cdg_sql);
  $obm_db->query($query2);

  $query = "SELECT COUNT(*) AS isDeprectated FROM CalendarSegment WHERE calendarsegment_eventid = '$old_event_id' 
            AND calendarsegment_state != 'R'";
  $obm_db->query($query);
  $obm_db->next_record();    
  if($obm_db->f("isDeprectated") == 0) {
    $query = "DELETE FROM CalendarSegment WHERE calendarsegment_eventid = '$old_event_id'";
    $obm_db->query($query);
    $query = "DELETE FROM CalendarEvent WHERE calendarevent_id = $old_event_id";
    $obm_db->query($query);
  }
}  


/////////////////////////////////////////////////////////////////////////////
// Delete all events of a evenements, and the event
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    - $agenda
/////////////////////////////////////////////////////////////////////////////
function run_query_delete($agenda) {
  global $cdg_sql,$l_delete_event_mail_head,$l_delete_event_mail_subject,$l_delete_event_mail_body,$l_to,$l_from;

  $obm_db = new DB_OBM;
  $event_id = $agenda["id"];
  $mail = $agenda["mail"]; 
  
  $query = "select calendaruser_user_id
    from CalendarUser
    where calendaruser_event_id = '$event_id'";
  $obm_db->query($query);
  while($obm_db->next_record()) {
    $contacts[] = $obm_db->f("calendaruser_user_id");
  }
  $query = "select calendarevent_title
    from CalendarEvent
    where calendarevent_id = '$event_id'";
  $obm_db->query($query); 
  $obm_db->next_record();
  $title = $obm_db->f("calendarevent_title");

  $query = "DELETE FROM CalendarException WHERE calendarexception_event_id = '$event_id'";
  $obm_db->query($query);
  $query = "DELETE FROM CalendarEvent WHERE calendarevent_id = $event_id";
  $obm_db->query($query);
  $query = "DELETE FROM CalendarUser WHERE calendaruser_event_id = $event_id";
  $obm_db->query($query);

  $message = $l_delete_event_mail_head.$title;   
  $subject = "$l_delete_event_mail_subject" . $title;
  send_mail($subject, $message, $contacts, "", $mail);
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
    FROM UserObm as u
      LEFT OUTER JOIN CalendarRight as c
        ON c.calendarright_ownerid = '$uid'
          AND c.calendarright_customerid = userobm_id 
    WHERE userobm_id != '$uid'
      and (userobm_archive != '1' or c.calendarright_write is not null)
    ORDER BY u.userobm_lastname";

  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}  


///////////////////////////////////////////////////////////////////////////////
// Get the list of readable agenda users for the current user
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_readable() {
  global $cdg_sql, $auth;

  $uid = $auth->auth["uid"];
  $query = "SELECT u.*,
      c.calendarright_read
    FROM UserObm as u
      LEFT OUTER JOIN CalendarRight as c 
        ON c.calendarright_customerid = '$uid'
          AND u.userobm_id = c.calendarright_ownerid 
    WHERE (c.calendarright_read = 1 OR u.userobm_id='$uid')
    ORDER BY u.userobm_lastname";

  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql);	    
  $obm_db->query($query);
  return $obm_db;
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
  $deny_write = $agenda["deny_w"];
  $deny_read = $agenda["deny_r"];
  $accept_read = $agenda["accept_r"];
  if($uid != $auth->auth["uid"]){
    $writable_user = run_query_userobm_writable();  
    if(!in_array($uid,$writable_user)) 
      return 0;
  }
  $obm_db = new DB_OBM;

  if (is_array($accept_write)) {
    foreach ($accept_write as $key => $id) {
      $query = "UPDATE CalendarRight SET calendarright_write = 1
        WHERE calendarright_ownerid = '$uid'
          AND calendarright_customerid = '$id'";
      display_debug_msg($query, $cdg_sql);	    
      $obm_db->query($query);
      if ($obm_db->affected_rows() == 0) {
	$query = "INSERT INTO CalendarRight VALUES('$uid','$id',1,0)";
      	display_debug_msg($query, $cdg_sql);	    
       	$obm_db->query($query);
      }
    }
  }

  if (is_array($deny_write)) {
    foreach ($deny_write as $key => $id) {
      $query = "UPDATE CalendarRight SET calendarright_write = 0
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

  if (is_array($deny_read)) {
    foreach ($deny_read as $key => $id) {
      $query = "UPDATE CalendarRight SET calendarright_read = 0
        WHERE calendarright_ownerid = '$uid'
          AND calendarright_customerid = '$id'";
      display_debug_msg($query, $cdg_sql); 
      $obm_db->query($query);
      if ($obm_db->affected_rows() == 0) {
	$query = "INSERT INTO CalendarRight VALUES('$uid','$id',1,0)";
      	display_debug_msg($query, $cdg_sql);	    
       	$obm_db->query($query);
      }
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

  // We delete users rows which have no rights
  $query = "DELETE FROM CalendarRight
    WHERE calendarright_read = 0 AND calendarright_write = 0";
  display_debug_msg($query, $cdg_sql);	    
  $obm_db->query($query);  
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
// category insertion query construction and execution                           //
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
// category update query execution                                               //
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
// category deletion query execution                                             //
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
// category deletion query execution                                             //
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
// category - Company links query execution                                      //
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
 
/////////////////////////////////////////////////////////////////////////////
// Return tables of hashed users.
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    - $contacts_array : array containing the id of the contact(s) the event is assigned to 
/////////////////////////////////////////////////////////////////////////////
function store_users($user_q) {
  global $ico_calendar_user0,$ico_calendar_user1,$ico_calendar_user2,$ico_calendar_user3,$ico_calendar_user4;
  global $ico_calendar_user5;

  $i = 0;
  while($user_q->next_record()) {
    $user_tab[$user_q->f("userobm_id")] = array("name"=>$user_q->f("userobm_firstname")." ".$user_q->f("userobm_lastname"),
	                                        "class"=>"agendaEventBg$i",
					        "image"=>${"ico_calendar_user".$i},
					        "id"=>$user_q->f("userobm_id"));
    $i++;
  }
  return $user_tab;
}


///////////////////////////////////////////////////////////////////////////////
// Hash a event table a perform where it's free for all, and when the 
// meeting could take place.
// Parameters:
//    - $current_event : Week event table 
//    - $duration : duration of the event
///////////////////////////////////////////////////////////////////////////////
function store_meeting_cell($current_events,$duration,$calendar_user,$getdate) {
  global $set_weekstart_default, $set_start_time, $set_stop_time,$set_cal_interval;

  $time_unit = 60 / $set_cal_interval;
  $start_week_time = strtotime(dateOfWeek($getdate, $set_weekstart_default));
  $end_week_time = $start_week_time + ((6 * 24) * 60 * 60);
  $start_week_time = strtotime("+$set_start_time hours",$start_week_time);
  $end_week_time = strtotime("+$set_stop_time hours",$end_week_time);
  for($current_time=$start_week_time;$current_time<=$end_week_time;) {
    $row_free = 0;
    $row_meeting = 0;
    if (is_array($current_events[$current_time])) {
      $isvalid = FALSE;
      foreach ($calendar_user as $id => $user_data ) {
	if (is_array($current_events[$current_time][$id])){
	  do {
	    $isvalid = TRUE;
	    $meeting_event[$current_time] = 2;
	    $temp_array = $current_events[$current_time][$id];
	    $current_time += $time_unit*60;
	    $hour_time = date("H",$current_time);
	  } while (is_array($current_events[$current_time][$id]) 
	  && count(array_intersect($temp_array,$current_events[$current_time][$id])) != 0  && $hour_time < $set_stop_time);
	}
      }
      if (!$isvalid)
      {
	$is_free = TRUE;
	$hour_time = date("H",$current_time);
	$temp_current_time = $current_time;
	while ($is_free == TRUE &&  $hour_time < $set_stop_time){
	  $temp_current_time += $time_unit*60;
	  if ($row_meeting == $duration) {
	    $row_free ++;
	  } else {
	    $row_meeting ++;
	  }
	$hour_time = date("H",$temp_current_time);	  
	  foreach ($calendar_user as $id => $user_data ) {
	    $is_free = FALSE;
	    if (is_array($current_events[$temp_current_time][$id]))
	      break;
	    $is_free = TRUE;
	  }	     
	}
	for ($count = 0;$count < $row_free; $count++) {
	  $meeting_event[$current_time] = 0;
	  $current_time += $time_unit*60;
	}	    
	for ($count = 0;$count < $row_meeting; $count++) {	    
	  $meeting_event[$current_time] = 1;
	  $current_time += $time_unit*60;	  
	}
      }
    } else {
      $is_free = TRUE;
      $hour_time = date("H",$current_time);
      $temp_current_time = $current_time;
      while ($is_free == TRUE &&  $hour_time < $set_stop_time){
	$temp_current_time += $time_unit*60;
	if ($row_meeting == $duration) {
	  $row_free ++;
	} else {
	  $row_meeting ++;
	}
	$hour_time = date("H",$temp_current_time);	
	foreach ($calendar_user as $id => $user_data ) {
	  $is_free = FALSE;
	  if (is_array($current_events[$temp_current_time][$id]))
	    break;
	  $is_free = TRUE;
	}	     
      }
      for ($count = 0;$count < $row_free; $count++) {
	$meeting_event[$current_time] = 0;
	$current_time += $time_unit*60;  
      }	   
      for ($count = 0;$count < $row_meeting; $count++) {
	$meeting_event[$current_time] = 1;
	$current_time += $time_unit*60;		
      }
    }
    if (date("G",$current_time) >= $set_stop_time){
      $tonextday = $set_start_time - $set_stop_time;
      $current_time = strtotime("+1 day $tonextday hours",$current_time);
    }     
  }

  return $meeting_event;
}


///////////////////////////////////////////////////////////////////////////////
// Agenda Form Data checking and formatting                                 //
// Parameters:
//   - $agenda[] : values checked
//     keys used  : num, name, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_data_form($agenda) {
  global $l_fill_title, $l_fill_dateend, $l_fill_datebegin, $l_err_datebegin, $l_err_dateend,$err_msg,$l_err_weekly_repeat ;
  global $l_err_begin_end, $l_err_end_repeat, $l_err_repeat,$l_err_end_repeat2;
  global $l_err_end_repeat3, $l_err_days_repeat, $l_err_days_repeat_not_weekly;
  
  $title = $agenda["title"];
  $datebegin = substr($agenda["date_begin"],0,8);
  $timebegin = substr($agenda["date_begin"],8,4);
  $dateend = substr($agenda["date_end"],0,8);
  $timeend = substr($agenda["date_end"],8,6);
  $repeat_end = $agenda["repeat_end"];
  preg_match ("/^(\d{4})(\d{2})(\d{2})/",$datebegin , $day_array);
  $this_day_b = $day_array[3]; 
  $this_month_b = $day_array[2];
  $this_year_b = $day_array[1];
  preg_match ("/^(\d{4})(\d{2})(\d{2})/",$dateend , $day_array2);
  $this_day_e = $day_array2[3]; 
  $this_month_e = $day_array2[2];
  $this_year_e = $day_array2[1];
  preg_match ("/^(\d{4})(\d{2})(\d{2})/",$repeat_end , $day_array3);
  $this_day_r = $day_array3[3]; 
  $this_month_r = $day_array3[2];
  $this_year_r = $day_array3[1];
  $kind = $agenda["kind"];
  $repeat_days = $agenda["repeat_days"];

  if (trim($title) == "") {
    $err_msg = $l_fill_title;
    return false;
  }
  
  if (trim($datebegin) == "") {
    $err_msg = $l_fill_datebegin;
    return false;
  }
  elseif (!checkdate($this_month_b,$this_day_b,$this_year_b)) {    
    $err_msg = $l_err_datebegin;
    return false;
  }  
  
  if (trim($dateend) == "") {
    $err_msg = $l_fill_dateend;
    return false;
  }
  elseif (!checkdate($this_month_e,$this_day_e,$this_year_e)) {    
    $err_msg = $l_err_dateend;
    return false;
  }  
  
  if (trim($repeat_end) != "" && $kind != "none") {
    if (!checkdate($this_month_r,$this_day_r,$this_year_r)) {    
      $err_msg = $l_err_repeat;
      return false;
    }  
  }

  if ($dateend<$datebegin || ($dateend==$datebegin && $timeend<=$timebegin)) {
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
  
  if (trim($repeat_end) != "" && (($dateend+100000) < $repeat_end) && $kind == "yearly") {
    $err_msg = $l_err_end_repeat3;
    return false;
  }  
  
  if (trim($repeat_end) != "" && (($dateend+10000) < $repeat_end) && $kind != "none" &&  $kind != "yearly") {
    $err_msg = $l_err_end_repeat2;
    return false;
  }  
  
  if ($kind != "none" && $dateend > $datebegin) {
    $err_msg = $l_err_repeat;
    return false;
  }  
  
  return true; 
}

//DEPRECATED
///////////////////////////////////////////////////////////////////////////////
// Agenda Form Data checking and formatting
// Parameters:
//   - $agenda[] : values checked
//     keys used  : num, name, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function get_event_repetition_dates($agenda) {
  global $set_weekstart_default;

  $repeat_kind = $agenda["kind"];
  $repeat_days =$agenda["repeat_days"]; 
  $repeat_end = $agenda["repeat_end"];
  
  preg_match ("/^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})/",$agenda["date_begin"] , $day_array);
  $this_min_b = $day_array[5]; 
  $this_hour_b = $day_array[4]; 
  $this_day_b = $day_array[3]; 
  $this_month_b = $day_array[2];
  $this_year_b = $day_array[1];
  preg_match ("/^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})/",$agenda["date_end"] , $day_array2);
  $this_min_e = $day_array2[5]; 
  $this_hour_e = $day_array2[4];   
  $this_day_e = $day_array2[3]; 
  $this_month_e = $day_array2[2];
  $this_year_e = $day_array2[1];
  preg_match ("([0-9]{4})([0-9]{2})([0-9]{2})",$repeat_end , $day_array3);
  $this_day_r = $day_array3[3]; 
  $this_month_r = $day_array3[2];
  $this_year_r = $day_array3[1];
  

  if($repeat_end!="") {
    $time_end_repeat = strtotime("+1 day",strtotime($repeat_end));
  }
  else {
    $time_end_repeat = strtotime("+1 year +1 day", strtotime($this_year_b.$this_month_b.$this_day_b));
  }
  $time_b=strtotime("+$this_hour_b hours +$this_min_b minutes",strtotime($this_year_b.$this_month_b.$this_day_b));
  $time_e=strtotime("+$this_hour_e hours +$this_min_e minutes",strtotime($this_year_e.$this_month_e.$this_day_e));
  if ($repeat_kind=="none") {
    $event_dates[] = array("date_begin" => $time_b,"date_end" => $time_e);
  }    
  elseif ($repeat_kind=="daily") {
    for($var_time_b=$time_b, $var_time_e=$time_e;
    $var_time_b <= $time_end_repeat;
    $var_time_b = strtotime("+1 day",$var_time_b), $var_time_e = strtotime("+1 day",$var_time_e)){
      $event_dates[]= array("date_begin" => $var_time_b,"date_end" => $var_time_e);
    }
  }
  elseif ($repeat_kind=="weekly") {
    $start_week_day = strtotime($set_weekstart_default);
    $first_day = (date("w",$time_b) - date("w",$start_week_day)+7)%7;
    for($var_time_b = strtotime("-$first_day days",$time_b), 
        $var_time_e = strtotime("-$first_day days",$time_e);
    $var_time_b <= $time_end_repeat;    
    $var_time_b = strtotime("+1 week",$var_time_b), $var_time_e = strtotime("+1 week",$var_time_e)) {      
      for ($i=0;($i<7) && (strtotime("+$i days",$var_time_b) <= $time_end_repeat ); $i++) { 
	$day_char=substr($repeat_days,$i,1);
	if (strcmp($day_char,"1")==0 && date("YmdHi",strtotime("+$i days",$var_time_b)) >= $agenda["date_begin"]) {
	  $event_dates[]=array("date_begin" => strtotime("+$i days",$var_time_b),
	                       "date_end" => strtotime("+$i days",$var_time_e));
	}
      }
    }  
  }  
  else if ($repeat_kind=="monthlybydate") {
    $var_time_e = $time_e;
    $i = 0;
    while(($var_time_t = strtotime("+$i month",$time_b)) <= $time_end_repeat) {
      if(date("d",$var_time_t) == $this_day_b) {
	$event_dates[]= array("date_begin" => $var_time_t,"date_end" => strtotime("+$i month",$var_time_e));
      }
      $i++;
    }
  }
  else if ($repeat_kind=="monthlybyday") {
    
    $start_of_month = strtotime($this_year_b.$this_month_b."01");
    
    $time_rb = $time_b;
    $time_re = $time_e;
    $start_week_day = date("w",strtotime($set_weekstart_default));
    
    $day_beg = (date("w",$time_rb) - $start_week_day)%7;
    $day_end = (date("w",$time_re) - $start_week_day)%7;  
    
    $tab_beg = array((($day_beg + 4)%7) => '+3',(($day_beg + 5)%7) => '+2',(($day_beg + 6)%7) => '+1',$day_beg => '+0',
               (($day_beg + 8)%7) => '-1',(($day_beg + 9)%7) => '-2',(($day_beg + 10)%7) => '-3');
    $tab_end = array((($day_end + 4)%7) => '+3',(($day_end + 5)%7) => '+2',(($day_end + 6)%7) => '+1',$day_end => '+0',
               (($day_end + 8)%7) => '-1',(($day_end + 9)%7) => '-2',(($day_end + 10)%7) => '-3'); 
	       
    $i = 1;
    $var_time_b = $time_b;
    while($var_time_b <= $time_end_repeat){
      $event_dates[]= array("date_begin" => $time_rb,"date_end" => $time_re);     
      $var_time_e = strtotime("+$i month",$time_e);
      $var_time_b = strtotime("+$i month",$time_b);
      $day_beg = (date("w",$var_time_b) - $start_week_day +7)%7;
      $day_end = (date("w",$var_time_e) - $start_week_day +7)%7;
      $time_rb = strtotime($tab_beg[$day_beg]." days",$var_time_b);
      $time_re = strtotime($tab_end[$day_end]." days",$var_time_e);
      if(date("m",$time_rb) != date("m",strtotime("+$i month",$start_of_month))) {
       	if(date("d",$time_rb) < 7) {
  	  $time_rb = strtotime("-7 days",$time_rb);
	  $time_re = strtotime("-7 days",$time_re);
      	}
	elseif(date("d",$time_rb) > 21) {
  	  $time_rb = strtotime("+7 days",$time_rb);
	  $time_re = strtotime("+7 days",$time_re);
	} 
      }
      $i ++;
    }
  } else if ($repeat_kind=="yearly") {  
    for($var_time_b=$time_b, $var_time_e=$time_e;
    $var_time_b <= $time_end_repeat;
    $var_time_b = strtotime("+1 year",$var_time_b), $var_time_e = strtotime("+1 year",$var_time_e)){  
      $event_dates[]= array("date_begin" => $var_time_b,"date_end" => $var_time_e);
    }
  }
  return $event_dates;
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
 global $l_daysofweek, $l_daysofweekshort, $l_daysofweekreallyshort;
 global $l_monthsofyear,  $l_monthsofyearshort;

 $day = '%A %e %B';
 $week = '%e %B';
 $week_list = '%a %e %b';
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
// dateOfWeek() takes a date in Ymd and a day of week in 3 letters or more
// and returns the date of that day. (ie: "sun" or "sunday" would be 
// acceptable values of $day but not "su")
//------------------------------------------------------------------------
// Argument:
// ---------
//     - $Ymd 
//     - $day
///////////////////////////////////////////////////////////////////////////////
function dateOfWeek($Ymd, $day) {
  global $set_weekstart_default;

  if (!isset($set_weekstart_default)) $set_weekstart_default = 'Sunday';
  $timestamp = strtotime($Ymd);
  $num = date('w', strtotime($set_weekstart_default));
  $start_day_time = strtotime((date('w',$timestamp)==$num ? "$set_weekstart_default" : "last $set_weekstart_default"), $timestamp);
  $ret_unixtime = strtotime($day,$start_day_time);
  $ret_unixtime = strtotime('+12 hours', $ret_unixtime);
  $ret = date('Ymd',$ret_unixtime);
  return $ret;
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
// Take a date YYYYMMDD and format it as YYYY-MM-DD
//------------------------------------------------------------------------
// Argument:
// ---------
//     - $date
///////////////////////////////////////////////////////////////////////////////
function format_to_iso($db_type,$date) {
  global $db_type_mysql, $db_type_pgsql;

  if ($db_type == $db_type_pgsql) {
    preg_match ("/^(\d{4})(\d{2})(\d{2})/",$date , $day_array);
    return $day_array[1]."-".$day_array[2]."-".$day_array[3];
  } elseif ($db_type == $db_type_mysql) {
    return $date;
  }
}

///////////////////////////////////////////////////////////////////////////////
// return the number of the week of the argument.
//------------------------------------------------------------------------
// Argument:
// ---------
//     - $date
///////////////////////////////////////////////////////////////////////////////
function get_week_num($date) {
  global $set_weekstart_default;
  $num = date('w', strtotime($set_weekstart_default));
  $delta_thursday = date("w",strtotime("-$num days",strtotime("thursday")))-date("w",strtotime("-$num days",$data));
  $week_num = date("W",strtotime("$delta_thursday days",$date));
  return $week_num;
}

///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a week of users or/and groups
// Parameters:
//   - $agenda : agenda params
//   - $contacts_array : contact id array, the event is assigned to 
//   - $groups_array   : group id array, the event is assigned to 
///////////////////////////////////////////////////////////////////////////////
function run_query_no_repeat_events($start,$end,$contacts_array) {
  global $cdg_sql, $auth;
  
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type;
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date");
  $calendarevent_date_l = sql_date_format($db_type,"calendarevent_date","calendarevent_date");
  $uid = $auth->auth["uid"];
  $query = "SELECT calendarevent_id,
		   calendarevent_title,
		   calendarevent_privacy,
		   calendarevent_description, 
		   calendaruser_user_id,
		   calendarcategory_label,
		   $calendarevent_date_l,
		   calendarevent_duration
	      FROM CalendarEvent, CalendarCategory, CalendarUser 
	      WHERE calendarevent_category_id = calendarcategory_id
	      AND calendarevent_id = calendaruser_event_id
	      AND calendaruser_state = 'A'
	      AND calendarevent_repeatkind = 'none'
	      AND $calendarevent_date >= $start - calendarevent_duration
	      AND $calendarevent_date <= $end 
            ";	
  if(is_array($contacts_array) && (count($contacts_array)>0) ) {
    $query .= " AND calendaruser_user_id IN (";
    foreach($contacts_array as $id => $value) {
      $query.= "$coma'$id'";
      $coma = ",";
    }
    $query.=")";
  }   	     	    
  $query.=" ORDER BY calendarevent_date"; 
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}

///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a week of users or/and groups
// Parameters:
//   - $agenda : agenda params
//   - $contacts_array : contact id array, the event is assigned to 
//   - $groups_array   : group id array, the event is assigned to 
///////////////////////////////////////////////////////////////////////////////
function run_query_repeat_events($start,$end,$contacts_array) {
  global $cdg_sql, $set_start_time, $set_stop_time,$set_weekstart_default,$auth;
  
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type;
  $calendarevent_endrepeat = sql_date_format($db_type,"calendarevent_endrepeat");
  $calendarevent_date = sql_date_format($db_type,"calendarevent_date");
  $calendarevent_date_l = sql_date_format($db_type,"calendarevent_date","calendarevent_date");
  $calendarevent_endrepeat_l = sql_date_format($db_type,"calendarevent_endrepeat","calendarevent_endrepeat");
  $uid = $auth->auth["uid"];

  $query = "SELECT calendarevent_id,
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
		   calendarevent_repeatdays
	      FROM CalendarEvent, CalendarCategory, CalendarUser 
	      WHERE calendarevent_category_id = calendarcategory_id
	      AND calendarevent_id = calendaruser_event_id
	      AND calendaruser_state = 'A'
	      AND calendarevent_repeatkind != 'none'
	      AND $calendarevent_date <= $end  - calendarevent_duration
	      AND $calendarevent_endrepeat >= $start
            ";		  
  if(is_array($contacts_array) && (count($contacts_array)>0) ) {
    $query .= " AND calendaruser_user_id IN (";
    foreach($contacts_array as $id => $value) {
      $query.= "$coma'$id'";
      $coma = ",";
    }
    $query.=")";
  }    	    
  $query.=" ORDER BY calendarevent_date";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}

/////////////////////////////////////////////////////////////////////////////
// Return a table of Events.
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
/////////////////////////////////////////////////////////////////////////////
function events_model($start_time, $end_time,$calendar_user) {
  global $set_start_time, $set_stop_time,$set_weekstart_default;

  $day_duration = 86400;
  $obm_no_repeat = run_query_no_repeat_events($start_time,$end_time,$calendar_user);
  while($obm_no_repeat->next_record()) {
    $multiday = false; 
    $id = $obm_no_repeat->f("calendarevent_id");
    $title = $obm_no_repeat->f("calendarevent_title");
    $privacy = $obm_no_repeat->f("calendarevent_privacy");
    $description = $obm_no_repeat->f("calendarevent_description"); 
    $category = $obm_no_repeat->f("calendarcategory_label");
    $date = $obm_no_repeat->f("calendarevent_date");
    $duration = $obm_no_repeat->f("calendarevent_duration");
    $user_id = $obm_no_repeat->f("calendaruser_user_id");
    
    $end_event = $date + $duration;
    $event = &new Event($id,$duration,$title,$category,$privacy,$description);
    do {
      $day = &$day_events[date("Ymd",$date)];
      if(!is_object($day) || !$day->is_same_day($date)) {
	$day = &new Day($date);
	$day_events[date("Ymd",$date)] = &$day;
      }
      $day->add_event(&$event,$date,$user_id);
      if(!$multiday) {
        $date = mktime ($set_start_time ,0,0,date("m",$date),date("d",$date),date("Y",$date));
	$multiday = true;
      } 
      $date = strtotime("+1 day",$date);
    }while($end_event > $date && $date < $end_time);
  }
  
  $obm_repeat = run_query_repeat_events($start_time,$end_time,$calendar_user);

  while($obm_repeat->next_record()) {
    $id = $obm_repeat->f("calendarevent_id");
    $title = $obm_repeat->f("calendarevent_title");
    $privacy = $obm_repeat->f("calendarevent_privacy");
    $description = $obm_repeat->f("calendarevent_description"); 
    $category = $obm_repeat->f("calendarcategory_label");
    $date = $obm_repeat->f("calendarevent_date");
    $duration = $obm_repeat->f("calendarevent_duration");
    $repeatkind = $obm_repeat->f("calendarevent_repeatkind");
    $endrepeat = $obm_repeat->f("calendarevent_endrepeat");
    $repeatfrequence = $obm_repeat->f("calendarevent_repeatfrequence");
    $repeatdays = $obm_repeat->f("calendarevent_repeatdays");
    $user_id = $obm_repeat->f("calendaruser_user_id");    

    $event = &new Event($id,$duration,$title,$category,$privacy,$description);
    $temp_date = $date;
    $end_date = ($endrepeat < $end_time)?$endrepeat:$end_time;
    $end_date += $day_duration;
    switch($repeatkind) {
      case "daily" :
	if($temp_date < $start_time) {
	  $temp_date = ceil(($start_time - $date) / $day_duration);
	  $hour_diff = date("H",$date) - date("H",$start_time);
	  $min_diff = date("i",$date) - date("i",$start_time);
	  if($temp_date > 0) {	  
	    $temp_date = $repeatfrequence -($temp_date % $repeatfrequence);
	  }	    
	  $temp_date = strtotime("+$temp_date days +$hour_diff hours +$min_diff minutes", $start_time);
	}
	while($temp_date <= $end_date) {
	  $day = &$day_events[date("Ymd",$temp_date)];
	  if(!is_object($day)) {
	    $day = &new Day($temp_date);
	    $day_events[date("Ymd",$temp_date)] = &$day;
	  }
	  $day->add_event(&$event,$temp_date,$user_id);
	  $temp_date = strtotime("+$repeatfrequence days",$temp_date);
	}
        break; 
      case "weekly" :
	$temp_date = strtotime(dateOfWeek(date("YmdHis",$date), $set_weekstart_default));
	if($temp_date < $start_time) {
	  $temp_date = ceil(($start_time - $temp_date) / $day_duration);
	  $hour_diff = date("H",$date) - date("H",$start_time);
	  $min_diff = date("i",$date) - date("i",$start_time);
	  $temp_date = ($temp_date % ($repeatfrequence * 7));
	  if($temp_date == "") $temp_date = 0;
	  $temp_date = strtotime("-$temp_date days +$hour_diff hours +$min_diff minutes", $start_time);
	}
        for($i=0; $i<strlen($repeatdays);$i++) {
          $repeat_days[$i]=substr($repeatdays, $i, 1);
	}	
	while($temp_date <= $end_date) {
	  foreach($repeat_days as $delta_day => $repeatition) {	   
	    if($repeatition == 1) {
	      $current_date = strtotime("+$delta_day days",$temp_date);
	      $day = &$day_events[date("Ymd",$current_date)];
	      if(!is_object($day)) {
		$day = &new Day($current_date);
		$day_events[date("Ymd",$current_date)] = &$day;
	      }
	      $day->add_event(&$event,$current_date,$user_id);
	    }	  
	  }	  
	  $temp_date = strtotime("+$repeatfrequence weeks",$temp_date);
	}		
	break;
      case "monthlybyday" :
        break;
      case "monthlybydate" :
	while($temp_date < $start_time) {
	  $temp_date = strtotime("$repeatfrequence months",$temp_date);
	}
	while($temp_date <= $end_date) {
	  $day = &$day_events[date("Ymd",$temp_date)];
	  if(!is_object($day)) {
	    $day = &new Day($temp_date);
	    $day_events[date("Ymd",$temp_date)] = &$day;
	  }
	  $day->add_event(&$event,$temp_date,$user_id);
	  $temp_date = strtotime("+$repeatfrequence months",$temp_date);
	}
	break;
    }
  }
  
  return $day_events; 
}

/////////////////////////////////////////////////////////////////////////////
// Class Event :
// Describe an event.
//////////////////////////////////////////////////////////////////////////////
class Event {
  
  var $id;
  var $start;
  var $end;
  var $duration;
  var $title;
  var $category;
  var $privacy;
  var $description;
  var $day;

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
  var $events;
  var $index;

  function Day($day) {
    $this->day = $day;
  }

  function is_same_day($date) {
    if(date("Ymd",$this->day) == date("Ymd",$date)) {
      return true;
    }
    return false;
  }

  function add_event(&$event,$begin_date,$uid) {
    $date = date("His",$begin_date);
    if(!isset($this->events[$date.".".$event->id])) {
      $this->events[$date.".".$event->id] = array("event"=>&$event,"date"=>$begin_date,"users"=>array($uid));
      ksort($this->events);
    }
    else {
      $this->events[$date.".".$event->id]["users"][] = $uid;
    }
  }

  function get_events($uid) {
    foreach($this->events as $event) {
      if(!in_array($uid,$event["users"])) {
	continue;
      }  
      else {
	$return[] = $event;
      }
    }
    return $return; 
  }
  
  function have_events_between($start,$end) {
    foreach($this->events as $event) {
      $ev_start = $event["date"];
      $ev_end = $event["event"]->duration + $ev_start;
      if($ev_start < $end && $ev_end > $start) {
	return true;
      }
    }
    return false;
  }

  
  function get_events_between($start, $end, $uid) {
    foreach($this->events as $event) {
      $ev_start = $event["date"];
      $ev_end = $event["event"]->duration + $ev_start;  
      if($ev_end < $start) {
	continue;
      }
      if($ev_start > $end) {
	break;
      }
      if(!in_array($uid,$event["users"])) {
	continue;
      }      
      if($ev_end > $start && $ev_start < $start) {
	return -1;
      }
      if($ev_start < $end && $ev_end > $start) {
	$return[] = $event;
	if($ev_end > $end) {
	  $end = $ev_end;
	}
      }
      
    }
    if($return != null) {
      $return = array("duration"=>$end - $start,"events"=>$return);
    }
    return $return; 
  }
}
</script>
