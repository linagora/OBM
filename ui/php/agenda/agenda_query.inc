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
function run_query_detail($param_event,$getdate) {
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type; 
  $date_time_d = strtotime($getdate);
  $date_time_f = strtotime("+1 day",$date_time_d);
  $timeupdate = sql_date_format($db_type, "calendarevent_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "calendarevent_timecreate", "timecreate");
  $calendarsegment_date_l_1 = sql_date_format($db_type,"cs1.calendarsegment_date","datebegin");
  $calendarsegment_date_l_2 = sql_date_format($db_type,"cs2.calendarsegment_date","dateend");
  $calendarsegment_date_1 = sql_date_format($db_type,"cs1.calendarsegment_date");
  $calendarsegment_date_2 = sql_date_format($db_type,"cs2.calendarsegment_date");
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
      calendarevent_repeatdays,
      $calendarevent_endrepeat,
      $calendarsegment_date_l_1,
      $calendarsegment_date_l_2
      FROM  CalendarCategory, CalendarSegment cs1, CalendarSegment cs2, CalendarEvent
      left join UserObm as c on calendarevent_usercreate=c.userobm_id
      left join UserObm as u on calendarevent_userupdate=u.userobm_id
    WHERE calendarevent_category_id = calendarcategory_id
      AND calendarevent_id = $param_event
      AND calendarevent_id = cs1.calendarsegment_eventid
      AND calendarevent_id = cs2.calendarsegment_eventid		  
      AND cs1.calendarsegment_flag = 'begin' AND $calendarsegment_date_1 < $date_time_f
      AND $calendarsegment_date_1 + calendarevent_length > $date_time_d 
      AND cs2.calendarsegment_flag = 'end' AND $calendarsegment_date_2 > $date_time_d
      AND $calendarsegment_date_2 - calendarevent_length < $date_time_f";
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
function run_query_event_customers($param_event,$getdate) {
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type;
  $getdate = format_to_iso($db_type,$getdate);
  $query = "SELECT DISTINCT userobm_lastname,userobm_firstname,userobm_id,calendarsegment_state,userobm_id
            FROM UserObm, CalendarSegment
            WHERE  calendarsegment_eventid = '$param_event'
	    AND calendarsegment_date LIKE '$getdate%'
	    AND calendarsegment_customerid = userobm_id
	    AND calendarsegment_type = 'user'";
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
function run_query_event_customers_array($param_event,$getdate) {
  $obm_db = new DB_OBM;
  $db_typ = $obm_db->type;
  $getdate = format_to_iso($db_type,$getdate);
  $query = "SELECT DISTINCT userobm_id
            FROM UserObm, CalendarSegment
            WHERE  calendarsegment_eventid = '$param_event'
            AND calendarsegment_date LIKE '$getdate%'
	    AND calendarsegment_customerid = userobm_id
	    AND calendarsegment_type = 'user'";
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
// Return all not rejected events in a day of users or/and groups
// Parameters: 
//   - $agenda         : agenda params
//   - $contacts_array : contact id array, the event is assigned to 
//   - $groups_array   : group id array, the event is assigned to 
///////////////////////////////////////////////////////////////////////////////
function run_query_day_event_list($agenda,$contacts_array) {
  global $cdg_sql, $set_start_time, $set_stop_time;
  
  $getdate = $agenda["date"];
  $start_day_time = strtotime("+$set_start_time hours",strtotime($getdate));
  $end_day_time = strtotime("+$set_stop_time hours",strtotime($getdate));
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type;
  $calendarsegment_date = sql_date_format($db_type,"calendarsegment_date");
  $calendarsegment_date_l = sql_date_format($db_type,"calendarsegment_date","calendarsegment_date");
  $query = "SELECT calendarevent_id,
      calendarevent_title,
      calendarevent_priority,
      calendarevent_privacy,
      calendarevent_description, 
      calendarcategory_label,
      calendarsegment_customerid,
      $calendarsegment_date_l,
      calendarsegment_flag
    FROM CalendarEvent,CalendarCategory, CalendarSegment
    WHERE calendarevent_category_id = calendarcategory_id
      AND calendarevent_id = calendarsegment_eventid
      AND  calendarsegment_state = 'A'
      AND (calendarsegment_flag = 'begin'
      AND $calendarsegment_date < $end_day_time
             AND $calendarsegment_date + calendarevent_length > $start_day_time
	  OR (calendarsegment_flag = 'end'
              AND $calendarsegment_date > $start_day_time
              AND $calendarsegment_date - calendarevent_length < $end_day_time)
          )";

  if(is_array($contacts_array) and (count($contacts_array)>0) ) {
    $query .= " AND calendarsegment_customerid IN ('".$contacts_array[0]."'";
    for ($i=1;$i<count($contacts_array);$i++) {
      $query.= ",'".$contacts_array[$i]."'";
    }
    $query.=")";
  }    
       
  $query.=" ORDER BY calendarsegment_date"; 
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
function run_query_week_event_list($agenda,$contacts_array) {
  global $cdg_sql, $set_start_time, $set_stop_time,$set_weekstart_default;
  
  $getdate = $agenda["date"];
  $start_week_time = strtotime(dateOfWeek($getdate, $set_weekstart_default));
  $end_week_time = $start_week_time + ((6 * 24 + $set_stop_time) * 60 * 60);
  
  $start_week_time = strtotime("+ $set_start_time hours",$start_week_time);
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type;
  $calendarsegment_date = sql_date_format($db_type,"calendarsegment_date");
  $calendarsegment_date_l = sql_date_format($db_type,"calendarsegment_date","calendarsegment_date");

  $query = "SELECT  calendarevent_id,
		   calendarevent_title,
		   calendarevent_priority,
		   calendarevent_privacy,
		   calendarevent_description, 
		   calendarcategory_label,
		   calendarsegment_customerid,
		   $calendarsegment_date_l,
		   calendarsegment_flag 
	      FROM CalendarEvent,CalendarCategory, CalendarSegment 
	      WHERE calendarevent_category_id = calendarcategory_id
	      AND calendarevent_id = calendarsegment_eventid       
	      AND calendarsegment_state = 'A'
	      AND ((calendarsegment_flag = 'begin' AND $calendarsegment_date < $end_week_time AND
	            $calendarsegment_date + calendarevent_length > $start_week_time)
	        OR (calendarsegment_flag = 'end' AND $calendarsegment_date > $start_week_time AND	    
		    $calendarsegment_date - calendarevent_length < $end_week_time))
          ";		   
  if(is_array($contacts_array) and (count($contacts_array)>0) ) {
    $query .= "AND  calendarsegment_customerid IN ('".$contacts_array[0]."'";
    for ($i=1;$i<count($contacts_array);$i++) {
      $query.= ",'".$contacts_array[$i]."'";
    }
    $query.=")";
  }    
  $query.=" ORDER BY calendarsegment_date"; 
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a month of users or/and groups
// Parameters: 
//   - $agenda : agenda params
//   - $contacts_array : contact id array, the event is assigned to 
//   - $groups_array   : group id array, the event is assigned to 
///////////////////////////////////////////////////////////////////////////////
function run_query_month_event_list($agenda,$contacts_array) {
  global $cdg_sql, $set_start_time, $set_stop_time,$set_weekstart_default;
  $getdate = $agenda["date"];
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})",$getdate , $day_array);

  $start_month = $day_array[1].$day_array[2]."01";
  $start_month_time = strtotime("+$set_start_time hours",strtotime($start_month));
  $end_month_time = strtotime("+1 month",$start_month_time);
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type;
  $calendarsegment_date = sql_date_format($db_type,"calendarsegment_date");
  $calendarsegment_date_l = sql_date_format($db_type,"calendarsegment_date","calendarsegment_date");  
  $query = "SELECT calendarevent_id,
                   calendarevent_usercreate,
		   calendarevent_title,
		   calendarevent_priority,
		   calendarevent_description, 
		   calendarevent_privacy,
		   calendarcategory_label,
		   calendarsegment_customerid,
		   $calendarsegment_date_l,
		   calendarsegment_flag
            FROM CalendarEvent,CalendarCategory, CalendarSegment
	    WHERE calendarevent_category_id = calendarcategory_id
	      AND calendarevent_id = calendarsegment_eventid
	      AND calendarsegment_state = 'A'
	      AND ((calendarsegment_flag = 'begin' AND $calendarsegment_date < $end_month_time AND
	            $calendarsegment_date + calendarevent_length > $start_month_time)
	        OR (calendarsegment_flag = 'end' AND $calendarsegment_date > $start_month_time AND	    
		    $calendarsegment_date - calendarevent_length < $end_month_time))
             ";

  if(is_array($contacts_array) and (count($contacts_array)>0) ) {
    $query .= "AND calendarsegment_customerid IN ('".$contacts_array[0]."'";
    for ($i=1;$i<count($contacts_array);$i++) {
      $query.= ",'".$contacts_array[$i]."'";
    }
    $query.=")";
  }    
  $query.=" ORDER BY calendarsegment_date"; 
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a month of users or/and groups
// Parameters: 
//   - $agenda : agenda params
//   - $contacts_array : contact id array, the event is assigned to 
//   - $groups_array   : group id array, the event is assigned to 
///////////////////////////////////////////////////////////////////////////////
function run_query_year_event_list($agenda,$contacts_array) {
  global $cdg_sql, $set_start_time, $set_stop_time,$set_weekstart_default;

  $getdate = $agenda["date"];
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})",$getdate , $day_array);
  $start_year = $day_array[1]."0101";
  $start_year_time  = strtotime("+$set_start_time hours",strtotime($start_year));strtotime($start_year);
  $end_year_time = strtotime("+1 year",$start_year_time);
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type;
  $calendarsegment_date_l = sql_date_format($db_type,"calendarsegment_date","calendarsegment_date");  
  $calendarsegment_date = sql_date_format($db_type,"calendarsegment_date");

  $query = "SELECT calendarevent_id,
                   calendarevent_usercreate,
		   calendarevent_title,
		   calendarevent_priority,
		   calendarevent_privacy,
		   calendarcategory_label,
		   calendarsegment_customerid,
		   $calendarsegment_date_l,
		   calendarsegment_flag
	      FROM CalendarEvent,CalendarCategory, CalendarSegment 
	      WHERE calendarevent_category_id = calendarcategory_id
	      AND calendarevent_id = calendarsegment_eventid
	      AND calendarsegment_state = 'A'
	      AND ((calendarsegment_flag = 'begin' AND $calendarsegment_date < $end_year_time AND
	            $calendarsegment_date + calendarevent_length > $start_year_time)
	        OR (calendarsegment_flag = 'end' AND $calendarsegment_date > $start_year_time AND	    
		    $calendarsegment_date - calendarevent_length < $end_year_time))
             ";

  if(is_array($contacts_array) and (count($contacts_array)>0) ) {
    $query .= "AND calendarsegment_customerid IN ('".$contacts_array[0]."'";
    for ($i=1;$i<count($contacts_array);$i++) {
      $query.= ",'".$contacts_array[$i]."'";
    }
    $query.=")";
  }    
  $query.=" ORDER BY calendarsegment_date"; 
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
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
  $query.=")";        
  $query.=" ORDER BY userobm_id"; 
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
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})",$agenda["date_begin"] , $day_array);  
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})",$agenda["date_end"] , $day_array2);    
  $force = $agenda["force"];
  $mail  = $agenda["mail"];
  $groups = $agenda["group"];
  if(is_array($groups)) {
    foreach($groups as $group_id) {
      $user_group_array = array_merge($user_group_array,run_query_get_groupsusers($group_id));
    }
    $contacts_array = array_merge($contacts_array,$user_group_array);
    $contacts_array = array_unique($contacts_array);
  }
  $return_info = array();
  $event_repeat_dates = get_event_repetition_dates($agenda);
  foreach ($event_repeat_dates as $dates) {
    $conflicts = run_query_get_conflicts($dates["date_begin"],$dates["date_end"],$contacts_array);
    $return_info = array_merge($return_info, $conflicts);
  }
  if ($force == 1 || count($return_info) == 0) {  
    $event_id = run_query_insert_event_data($agenda);   
    foreach ($event_repeat_dates as $dates) {
      if (is_array($groups)) {
	foreach($groups as $group_id) {
	  run_query_insert_occurence($dates["date_begin"],$dates["date_end"],$group_id,'group',$event_id,'R');
	}
      }
      foreach ($contacts_array as $user_id) {
	if ($user_id == $auth->auth["uid"] || in_array($user_id,$writable_user)) {

    	  run_query_insert_occurence($dates["date_begin"],$dates["date_end"],$user_id,'user',$event_id,'A');	
	} else {
	  run_query_insert_occurence($dates["date_begin"],$dates["date_end"],$user_id,'user',$event_id,'W');	
	}
      }
    }
    $message = $l_add_event_mail_head.$agenda["title"]."\n".$l_event_mail_body.$l_from." "
	       .$day_array[1]."-".$day_array[2]."-".$day_array[3]." @ ".$day_array[4].":".$day_array[5]." "
	       .$l_to." ".$day_array2[1]."-".$day_array2[2]."-".$day_array2[3]." @ ".$day_array2[4].":".$day_array2[5];      
    $subject = "$l_add_event_mail_subject" . $agenda["title"];
    send_mail($subject, $message, $contacts_array,$mail);
  }

  return $return_info;
}


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
  if ($state == 'A') {
    $conflicts = run_query_get_latent_conflicts($event_id, $user);
  }
  else{
    $conflicts = array();
  }
  if ($force == 1 || count($conflicts) == 0){  
    run_query_insert_occurence_state($event_id,$user,$state,$conflicts);
  }
  return $conflicts;
}


///////////////////////////////////////////////////////////////////////////////
// Get conflict if the waiting events are set to accept
// Parameters:
//   - $event_id 
//   - $user_id
///////////////////////////////////////////////////////////////////////////////
function run_query_get_latent_conflicts($event_id, $user_id) {
  global $cdg_sql;
  $return_info = array();
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type; 
  
  $calendarsegment_date_l_1 = sql_date_format($db_type,"cs1.calendarsegment_date","datebegin");
  $calendarsegment_date_l_2 = sql_date_format($db_type,"cs2.calendarsegment_date","dateend");
  $calendarsegment_date_1 = sql_date_format($db_type,"cs1.calendarsegment_date");
  $calendarsegment_date_2 = sql_date_format($db_type,"cs2.calendarsegment_date");   
  
  $query = "SELECT DISTINCT
      $calendarsegment_date_l_1,
      $calendarsegment_date_l_2 
      FROM  CalendarSegment cs1, CalendarSegment cs2, CalendarEvent
    WHERE cs1.calendarsegment_customerid = $user_id
      AND cs1.calendarsegment_state = 'W'
      AND cs2.calendarsegment_customerid = $user_id
      AND cs2.calendarsegment_state = 'W'
      AND cs2.calendarsegment_eventid = cs1.calendarsegment_eventid
      AND cs1.calendarsegment_eventid = '$event_id'	
      AND cs1.calendarsegment_flag = 'begin'
      AND cs2.calendarsegment_flag = 'end'
      AND calendarevent_id = cs1.calendarsegment_eventid
      AND $calendarsegment_date_2 - calendarevent_length = $calendarsegment_date_1
    ORDER BY datebegin,dateend";

  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  while( $obm_db->next_record() ) {
    $conflicts = run_query_get_conflicts($obm_db->f("datebegin"),$obm_db->f("dateend"),array($user_id));
    $return_info = array_merge($return_info, $conflicts);
  }
  return $return_info;  
}


///////////////////////////////////////////////////////////////////////////////
// XXXXX???? Bad definition : Update a user decision for an user
// Parameters: 
//  -  $agenda : Agenda params
///////////////////////////////////////////////////////////////////////////////
function run_query_insert_occurence_state($event_id,$user_id,$state, $conflicts){
  global $cdg_sql;
  $db_type = $obm_db->type;
  $calendarsegment_date = sql_date_format($db_type,"calendarsegment_date");
  $obm_db = new DB_OBM;
  $query = "UPDATE CalendarSegment 
            SET calendarsegment_state = '$state',
	    calendarsegment_date = calendarsegment_date
            WHERE calendarsegment_customerid = $user_id
		  AND calendarsegment_state = 'W'
		  AND calendarsegment_eventid = '$event_id'";
  if(count($conflicts) != 0) {
    $query .= " AND $calendarsegment_date NOT IN (".$conflicts[0]["date_begin"].",".$conflicts[0]["date_end"]."";
    for ($i=1;$i<count($contacts_array);$i++) {
      $query.= ",".$conflicts[$i]["date_begin"].",".$conflicts[$i]["date_end"]."";
    }
    $query.=")";
  }   
  display_debug_msg($query, $cdg_sql);  
  $obm_db->query($query);
  return $obm_db;    
}


///////////////////////////////////////////////////////////////////////////////
// Update a user decision for an user
// Parameters: 
//   - $agenda : Agenda params
///////////////////////////////////////////////////////////////////////////////
function run_query_change_decision($agenda) {
  global $auth;
  if($agenda["decision_event"] == 'A') {
    $conflicts = run_query_get_conflicts($agenda["date_begin"],$agenda["date_end"],array($auth->auth["uid"]));   
  }
  else{
    $conflicts = array();
  }
  if(count($conflicts) == 0) {
    run_query_update_occurence_state($agenda["id"],$agenda["date_begin"],$agenda["date_end"],$auth->auth["uid"],$agenda["decision_event"]);	
  }
  return $conflicts;
}


///////////////////////////////////////////////////////////////////////////////
// Perform the conflict management
// Parameters: 
//   - $agenda : Agenda params
///////////////////////////////////////////////////////////////////////////////
function run_query_manage_conflict($agenda) {
  global $auth;

  $conflict_array = $agenda["conflict_event"];
  $conflict_end = $agenda["conflict_end"];
  $param_event = $agenda["id"];
  foreach($conflict_array as $date => $event_array) {
    foreach($event_array as $event_id => $decision) {
      if($decision == "force") {
	run_query_update_occurence_state($param_event,$date,$conflict_end[$date][$event_id],$auth->auth["uid"],'A');
      }elseif($decision == "replace") {
	run_query_update_occurence_state($param_event,$date,$conflict_end[$date][$event_id],$auth->auth["uid"],'A');
	run_query_update_occurence_state($event_id,$date,$conflict_end[$date][$event_id],$auth->auth["uid"],'R');
      }elseif($decision == "cancel") {	
	run_query_update_occurence_state($param_event,$date,$conflict_end[$date][$event_id],$auth->auth["uid"],'R');
      }  
    }
  }
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
  $calendarsegment_date_1 = sql_date_format($db_type,"cs1.calendarsegment_date");    
  $calendarsegment_date_2 = sql_date_format($db_type,"cs2.calendarsegment_date"); 
  $query = "SELECT DISTINCT
                   userobm_id,
		   userobm_lastname,
		   userobm_firstname,
                   calendarevent_id,
                   calendarevent_title, 
		   calendarcategory_label,
		   calendarevent_privacy,
		   calendarevent_priority,
		   calendarevent_repeatkind,
		   calendarevent_repeatdays,
		   $calendarevent_endrepeat,
		   MIN($calendarsegment_date_1) as datebegin,
		   MIN($calendarsegment_date_2)	as dateend   
            FROM CalendarEvent, CalendarCategory, CalendarSegment cs1, CalendarSegment cs2,
	    UserObm 
	    WHERE calendarevent_category_id = calendarcategory_id
	          AND userobm_id = cs1.calendarsegment_customerid  
	      	  AND cs1.calendarsegment_customerid = cs2.calendarsegment_customerid
		  AND cs1.calendarsegment_state = 'W'
		  AND cs2.calendarsegment_state = 'W'
	          AND calendarevent_id = cs1.calendarsegment_eventid
		  AND calendarevent_id = cs2.calendarsegment_eventid	
		  AND cs1.calendarsegment_flag = 'begin'
		  AND cs2.calendarsegment_flag = 'end' ";

  if(is_array($writable_user)) {
    $query .= " AND cs1.calendarsegment_customerid IN (";
    $num = count($writable_user);
    for($i=0;$i<$num;$i++) {
      $query.= "'".$writable_user[$i]."',";
    }
    $query.="".$auth->auth["uid"].")";
  }
  else {
    $query.="AND cs1.calendarsegment_customerid = ".$auth->auth["uid"]."";
  }
  $query .= "GROUP BY calendarevent_id,
		 calendarevent_title,
		 calendarcategory_label,
		 calendarevent_privacy,
		 calendarevent_priority,
		 calendarevent_repeatkind,
		 calendarevent_repeatdays,
		 calendarevent_endrepeat,
		 userobm_id,
		 userobm_lastname,
	      	 userobm_firstname
		 ORDER BY userobm_id
		 "; 
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}


/////////////////////////////////////////////////////////////////////////////
// Update the state of a couple of occurence
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    - $param_event
//    - $date_begin
//    - $date_end
//    - $user_id
//    - $state

/////////////////////////////////////////////////////////////////////////////
function run_query_update_occurence_state($event_id, $date_begin,$date_end,$user_id,$state) {
  global $cdg_sql;
  $obm_db = new DB_OBM;
   $db_type = $obm_db->type;
  $calendarsegment_date_l = sql_date_format($db_type,"calendarsegment_date","calendarsegment_date");    
  $calendarsegment_date = sql_date_format($db_type,"calendarsegment_date");     
  $date_begin_time = mktime(substr($date_begin,8,2), substr($date_begin,10,2), "00", substr($date_begin,4,2), substr($date_begin,6,2), substr($date_begin,0,4));
  $date_end_time = mktime(substr($date_end,8,2), substr($date_end,10,2), "00", substr($date_end,4,2), substr($date_end,6,2), substr($date_end,0,4));
  
  $query = "SELECT DISTINCT $calendarsegment_date_l FROM CalendarSegment,CalendarEvent WHERE
            calendarsegment_customerid = $user_id AND calendarsegment_eventid = '$event_id'
	    AND calendarsegment_eventid = calendarevent_id
	    AND ((calendarsegment_flag = 'begin' AND $calendarsegment_date < $date_end_time AND
	          $calendarsegment_date + calendarevent_length > $date_begin_time)
	      OR (calendarsegment_flag = 'end' AND $calendarsegment_date > $date_begin_time AND	    
		  $calendarsegment_date - calendarevent_length < $date_end_time))
             ";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  $obm_db->next_record();
  
  $query = "UPDATE CalendarSegment SET calendarsegment_state = '$state', calendarsegment_date = calendarsegment_date WHERE
            calendarsegment_customerid = $user_id AND calendarsegment_eventid = '$event_id'
	    AND  $calendarsegment_date IN (".$obm_db->f("calendarsegment_date")."";
  while ( $obm_db->next_record()) {
    $query.= ",".$obm_db->f("calendarsegment_date")."";
  }
  $query.=")";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);

  if($state == 'R') {
    $query = "SELECT COUNT(*) AS isDeprectated FROM CalendarSegment WHERE calendarsegment_eventid = '$event_id' AND calendarsegment_state != 'R'";
    $obm_db->query($query);
    $obm_db->next_record();    
    if($obm_db->f("isDeprectated") == 0) {
      $query = "DELETE FROM CalendarSegment WHERE calendarsegment_eventid = '$event_id'";
      $obm_db->query($query);
      $query = "DELETE FROM CalendarEvent WHERE calendarevent_id = $event_id";
      $obm_db->query($query);
    }
  }

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
function run_query_insert_occurence($date_begin,$date_end,$user_id,$customer_type,$event_id,$state) {
  global $cdg_sql;

  $obm_db = new DB_OBM;
  $query = "INSERT INTO CalendarSegment (calendarsegment_eventid, calendarsegment_customerid, calendarsegment_date, calendarsegment_flag,
                                         calendarsegment_type, calendarsegment_state) 
                                  VALUES ($event_id,$user_id,'".date("Y-m-d H:i:s",$date_begin)."','begin','$customer_type','$state')";
  
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  $query = "INSERT INTO CalendarSegment (calendarsegment_eventid, calendarsegment_customerid, calendarsegment_date, calendarsegment_flag,
                                         calendarsegment_type, calendarsegment_state) 
                                  VALUES ($event_id,$user_id,'".date("Y-m-d H:i:s",$date_end)."','end','$customer_type','$state')";
  
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db; 
}



/////////////////////////////////////////////////////////////////////////////
// search all conflict for a user in a laps of  time 
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    - $date_begin
//    - $date_end
//    - $user_id :
/////////////////////////////////////////////////////////////////////////////
function run_query_get_conflicts_user($date_begin,$date_end,$user_id) {
  global $cdg_sql;
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type;    
  $calendarsegment_date = sql_date_format($db_type,"calendarsegment_date");

  $query = "SELECT DISTINCT calendarsegment_eventid ,calendarsegment_date
             FROM  CalendarEvent, CalendarSegment
	    WHERE calendarsegment_eventid = calendarevent_id AND calendarsegment_state = 'A' AND
	          ((calendarsegment_flag = 'begin' AND $calendarsegment_date < $date_end_time AND
	            $calendarsegment_date + calendarevent_length > $date_begin_time)
	        OR (calendarsegment_flag = 'end' AND $calendarsegment_date > $date_begin_time AND	    
		    $calendarsegment_date calendarevent_length < $date_end_time))
            AND calendarsegment_customerid = $user_id ORDER BY calendarsegment_date";
	    
  
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}
/////////////////////////////////////////////////////////////////////////////
// search all conflict for in a laps of  time 
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    - $date_begin
//    - $date_end
//    - $user_id :
////////////////////////////////////////////////////////////////////////////
function run_query_get_conflicts($date_begin,$date_end,$contacts_array) {
  global $cdg_sql;
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type; 
  $calendarsegment_date_l_1 = sql_date_format($db_type,"cs1.calendarsegment_date","date_begin");
  $calendarsegment_date_l_2 = sql_date_format($db_type,"cs2.calendarsegment_date","date_end");
  $calendarsegment_date_1 = sql_date_format($db_type,"cs1.calendarsegment_date");
  $calendarsegment_date_2 = sql_date_format($db_type,"cs2.calendarsegment_date");    
  
  $query = "SELECT DISTINCT cs1.calendarsegment_eventid ,cs1.calendarsegment_customerid,$calendarsegment_date_l_1,
                   $calendarsegment_date_l_2, calendarevent_title,userobm_lastname,userobm_firstname
            FROM   CalendarEvent, CalendarSegment cs1,CalendarSegment cs2, UserObm 
	    WHERE cs1.calendarsegment_eventid = calendarevent_id AND userobm_id = cs1.calendarsegment_customerid 
	          AND cs1.calendarsegment_state = 'A' AND cs2.calendarsegment_state = 'A' 
		  AND cs2.calendarsegment_eventid = calendarevent_id AND userobm_id = cs2.calendarsegment_customerid
	          AND (cs1.calendarsegment_flag = 'begin' AND $calendarsegment_date_1 < $date_end AND
		  $calendarsegment_date_1 + calendarevent_length > $date_begin)
		  AND (cs2.calendarsegment_flag = 'end' AND $calendarsegment_date_1 
		  + calendarevent_length = $calendarsegment_date_2)
	    ";  		  
  $query .= " AND cs1.calendarsegment_customerid IN ('".$contacts_array[0]."'";
  for ($i=1;$i<count($contacts_array);$i++) {
    $query.= ",'".$contacts_array[$i]."'";
  }
  $query.=")";
  $query .= " ORDER BY date_begin, cs1.calendarsegment_customerid";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  while ($obm_db->next_record()) {
    $conflict_array[] = array("event_id" => $obm_db->f("calendarsegment_eventid"),"event_title" => $obm_db->f("calendarevent_title"),
    "user_id" => $obm_db->f("calendarsegment_customerid"),"user_name" => $obm_db->f("userobm_lastname")." 
    ".$obm_db->f("userobm_firstname"),"date_begin" => $obm_db->f("date_begin"),"date_end" => $obm_db->f("date_end"));
    $day_array="";
  }
  return $conflict_array;
}
/////////////////////////////////////////////////////////////////////////////
// search all conflict for in a laps of  time  except a event
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    - $date_begin
//    - $date_end
//    - $user_id :
////////////////////////////////////////////////////////////////////////////

function run_query_get_conflicts_update($date_begin,$date_end,$contacts_array,$event_id) {
  global $cdg_sql;
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type; 
  
  $calendarsegment_date_l_1 = sql_date_format($db_type,"cs1.calendarsegment_date","date_begin");
  $calendarsegment_date_l_2 = sql_date_format($db_type,"cs2.calendarsegment_date","date_end");
  $calendarsegment_date_1 = sql_date_format($db_type,"cs1.calendarsegment_date");
  $calendarsegment_date_2 = sql_date_format($db_type,"cs2.calendarsegment_date");    

  $query = "SELECT DISTINCT cs1.calendarsegment_eventid ,cs1.calendarsegment_customerid,$calendarsegment_date_l_1,
                   $calendarsegment_date_l_2, calendarevent_title,userobm_lastname,userobm_firstname
            FROM   CalendarEvent, CalendarSegment cs1,CalendarSegment cs2, UserObm 
	    WHERE cs1.calendarsegment_eventid = calendarevent_id AND userobm_id = cs1.calendarsegment_customerid 
	          AND cs1.calendarsegment_state = 'A' AND cs2.calendarsegment_state = 'A' 
		  AND calendarevent_id != '$event_id'
		  AND cs2.calendarsegment_eventid = calendarevent_id AND userobm_id = cs2.calendarsegment_customerid
	          AND (cs1.calendarsegment_flag = 'begin' AND $calendarsegment_date_1 < $date_end AND
		  $calendarsegment_date_1 + calendarevent_length > $date_begin)
		  AND (cs2.calendarsegment_flag = 'end' AND $calendarsegment_date_1
		  + calendarevent_length = $calendarsegment_date_2)
	    ";  		  
  $query .= " AND cs1.calendarsegment_customerid IN ('".$contacts_array[0]."'";
  for ($i=1;$i<count($contacts_array);$i++) {
    $query.= ",'".$contacts_array[$i]."'";
  }
  $query.=")";
  $query .= " ORDER BY date_begin, cs1.calendarsegment_customerid";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  while ($obm_db->next_record()) {
    $conflict_array[] = array("event_id" => $obm_db->f("calendarsegment_eventid"),"event_title" => $obm_db->f("calendarevent_title"),
    "user_id" => $obm_db->f("calendarsegment_customerid"),"user_name" => $obm_db->f("userobm_lastname")." 
    ".$obm_db->f("userobm_firstname"),"date_begin" => $obm_db->f("date_begin"),"date_end" => $obm_db->f("date_end"));
    $day_array="";
  }
  return $conflict_array;
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
  $stamp_beg = mktime(substr($agenda["date_begin"],8,2), substr($agenda["date_begin"],10,2), "00", substr($agenda["date_begin"],4,2), substr($agenda["date_begin"],6,2), substr($agenda["date_begin"],0,4));
  $stamp_end = mktime(substr($agenda["date_end"],8,2), substr($agenda["date_end"],10,2), "00", substr($agenda["date_end"],4,2), substr($agenda["date_end"],6,2), substr($agenda["date_end"],0,4));
  $length = $stamp_end - $stamp_beg;
  if($agenda["privacy"]!=1) $privacy = 0;else $privacy = 1; 
  $repeat_kind = $agenda["kind"];
  $repeat_days = $agenda["repeat_days"]; 
  $repeat_end = mktime(substr($agenda["repeat_end"],8,2), substr($agenda["repeat_end"],10,2), "00", substr($agenda["repeat_end"],4,2), substr($agenda["repeat_end"],6,2), substr($agenda["repeat_end"],0,4));
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
  $query = "INSERT INTO CalendarEvent (calendarevent_id,calendarevent_timecreate,  
                         calendarevent_usercreate, calendarevent_title, calendarevent_description,
			 calendarevent_category_id,calendarevent_priority,calendarevent_privacy, calendarevent_length,
			 calendarevent_repeatkind,calendarevent_repeatdays,calendarevent_endrepeat)
	         VALUES ($max_id,'".date("Y-m-d H:i")."',".$auth->auth["uid"].",'$title',
		         '$description',$category_id,$priority,$privacy,$length,
			 '$repeat_kind','$repeat_days','".date("Y-m-d H:i",$repeat_end)."')";

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
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})",$agenda["date_begin"] , $day_array);  
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})",$agenda["date_end"] , $day_array2);   
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})",$agenda["old_begin"] , $day_array3);  
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})",$agenda["old_end"] , $day_array4);    
  $force = $agenda["force"];
  $repeat_update = $agenda["repeat_update"];
  $old_event_id = $agenda["id"];
  $groups = $agenda["group"];
  $mail = $agenda["mail"];
  if($repeat_update == 0 ) {
    $agenda["kind"] ="none";
    $agenda["repeat_days"] ="0000000";
    $agenda["repeat_end"] = "";
  }
  if(is_array($groups)) {
    foreach($groups as $group_id) {
      $user_group_array = array_merge($user_group_array,run_query_get_groupsusers($group_id));
    }
    $contacts_array = array_merge($contacts_array,$user_group_array);
    $contacts_array = array_unique($contacts_array);
  }
  
  $return_info = array();
  $event_repeat_dates = get_event_repetition_dates($agenda);
  foreach($event_repeat_dates as $dates) {
    $conflicts = run_query_get_conflicts_update($dates["date_begin"],$dates["date_end"],$contacts_array,$old_event_id);
    $return_info = array_merge($return_info, $conflicts);
  }

  if($force == 1 || count($return_info) == 0){  
    run_query_update_deprecated_events($agenda);
    $event_id = run_query_insert_event_data($agenda);   
    foreach($event_repeat_dates as $dates) {
      if(is_array($groups)) {
	foreach($groups as $group_id) {
	  run_query_insert_occurence($dates["date_begin"],$dates["date_end"],$group_id,'group',$event_id,'R');
	}
      }
      foreach($contacts_array as $user_id) {
	if($user_id == $auth->auth["uid"] || in_array($user_id,$writable_user)) {
      	  run_query_insert_occurence($dates["date_begin"],$dates["date_end"],$user_id,'user',$event_id,'A');	
	}
	else{
	  run_query_insert_occurence($dates["date_begin"],$dates["date_end"],$user_id,'user',$event_id,'W');	
	}
      }
    }
    if(($agenda["old_begin"] != $agenda["date_begin"] || $agenda["old_end"] != $agenda["date_end"])) {
      $message = $l_update_event_mail_head.$agenda["title"]."\n".$l_event_mail_body.$l_from." "
                 .$day_array[1]."-".$day_array[2]."-".$day_array[3]." @ ".$day_array[4].":".$day_array[5]." "
		 .$l_to." ".$day_array2[1]."-".$day_array2[2]."-".$day_array2[3]." @ ".$day_array2[4].":".$day_array2[5]." ".
		 $l_update_event_mail_body.$l_from." "
                 .$day_array3[1]."-".$day_array3[2]."-".$day_array3[3]." @ ".$day_array3[4].":".$day_array3[5]." "
		 .$l_to." ".$day_array4[1]."-".$day_array4[2]."-".$day_array4[3]." @ ".$day_array4[4].":".$day_array4[5];         
      $subject = "$l_update_event_mail_subject" . $agenda["title"];
      send_mail($subject, $message, $contacts_array, $mail);
    }
  }
  return $return_info;
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

  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})",$agenda["date_begin"] , $day_array);  
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})",$agenda["date_end"] , $day_array2);      
  $obm_db = new DB_OBM;
  $event_id = $agenda["id"];
  $mail = $agenda["mail"]; 
  $query = "SELECT calendarsegment_customerid FROM CalendarSegment WHERE calendarsegment_eventid = '$event_id'";
  $obm_db->query($query);
  while($obm_db->next_record()) {
    $contacts[] = $obm_db->f("calendarsegment_customerid");
  }
  $query = "SELECT calendarevent_title FROM CalendarEvent WHERE calendarevent_id = $event_id";
  $obm_db->query($query); 
  $obm_db->next_record();
  $title = $obm_db->f("calendarevent_title");
  $query = "DELETE FROM CalendarSegment WHERE calendarsegment_eventid = '$event_id'";
  $obm_db->query($query);
  $query = "DELETE FROM CalendarEvent WHERE calendarevent_id = $event_id";
  $obm_db->query($query);
  $message = $l_delete_event_mail_head.$title;   
  $subject = "$l_delete_event_mail_subject" . $title;
  send_mail($subject, $message, $contacts, $mail);
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

///////////////////////////////////////////////////////////////////////////////
// Get a group user list                                                   //
// Parameters:
//   - $id : group id
// Returns:
//   - array of users groups ids
///////////////////////////////////////////////////////////////////////////////
function run_query_get_groupsusers($id) {

   $groups[] = $id;
   $groups = array_merge($groups,get_group_children($id));
   $groups = array_unique($groups);
   return run_query_get_group_users($groups);
}




///////////////////////////////////////////////////////////////////////////////
// Get a group child list                                                   //
// Parameters:
//   - $id : group id
// Returns:
//   - array of parent groups ids
///////////////////////////////////////////////////////////////////////////////
function get_group_children($id) {
  global $cdg_sql;

  $query = "select groupgroup_childid
    from GroupGroup where groupgroup_parentid='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
 
  $children = array();
  while ($obm_q->next_record()) {
    $g_id = $obm_q->f("groupgroup_childid");
    array_push($children, $g_id);
    $child_parents = get_group_children($g_id);
    $children = array_merge($children, $child_parents);

  }

  return array_unique($children);
}



///////////////////////////////////////////////////////////////////////////////
// Get a group first level user list                                         //
// Parameters:
//   - $id : array ofgroup id
// Returns:
//   - array of users groups ids
///////////////////////////////////////////////////////////////////////////////
function run_query_get_group_users($groups) {
  global $cdg_sql;

  $query = "select userobmgroup_userobmid
    from UserObmGroup where userobmgroup_groupid IN(";
  $coma = "";
  foreach($groups as $group_id) {
	$query .= $coma.$group_id;
	$coma = ",";
  }
  $query .= ")";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
 
  $users = array();
  while($obm_q->next_record()) {
    $users[] = $obm_q->f("userobmgroup_userobmid");
  }
  return array_unique($users);
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
// Return tables of hashed events and of data event (hashed by the time units
//define in global.inc)
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    - $agenda : agenda params
//    - $contacts_array : array containing the id of the contact(s) the event is assigned to 
//    - $groups_array   : array containing the id of the group(s) the event is assigned to 
/////////////////////////////////////////////////////////////////////////////
function store_events($obm_q,&$current_event,&$event_data,$p_date_begin,$p_date_end) {
  global $set_start_time, $set_stop_time,$set_weekstart_default,$set_cal_interval;

  $obm_q->next_record();
  $db_date = $obm_q->f("calendarsegment_date");
  $db_customer_id = $obm_q->f("calendarsegment_customerid");
  $db_id = $obm_q->f("calendarevent_id");
  $db_title = $obm_q->f("calendarevent_title");
  $db_cat_label = $obm_q->f("calendarcategory_label");
  $db_desc = $obm_q->f("calendarevent_description");
  $db_flag = $obm_q->f("calendarsegment_flag");
  $db_privacy = $obm_q->f("calendarevent_privacy");
  $time_unit = 60 / $set_cal_interval;
  $s =0;
  while($db_date != "" && $db_date < $p_date_begin) {
    $current_event[$current_time][$db_customer_id][] = $db_id;
    $status = $event_data[$db_id]["status"] + 1;
    $event_data[$db_id] = 
    array("title"=>$db_title,"type"=>$db_cat_label,"begin"=>date("H:i",$db_date),"status" => $status,"privacy" => $db_privacy);
    $obm_q->next_record(); 
    $db_date = $obm_q->f("calendarsegment_date");
    $db_customer_id = $obm_q->f("calendarsegment_customerid");
    $db_id = $obm_q->f("calendarevent_id");
    $db_title = $obm_q->f("calendarevent_title");
    $db_cat_label = $obm_q->f("calendarcategory_label");
    $db_desc = $obm_q->f("calendarevent_description");
    $db_flag = $obm_q->f("calendarsegment_flag");
    $db_privacy = $obm_q->f("calendarevent_privacy");
  }  
  for($current_time=$p_date_begin;$current_time<=$p_date_end;$current_time += 60*$time_unit) {
    if(date("G",$current_time) >= $set_stop_time){
      $tonextday = $set_start_time - $set_stop_time;
      $current_time = strtotime("+1 day $tonextday hours",$current_time);
    }
    if(is_array($current_event[$past_time])) {
      $temp_array = array_filter($current_event[$past_time],"valid_event");
      if(count($temp_array)>0){
	$current_event[$current_time] = $temp_array;
      }
    }   
    while($db_date != "" && $db_date >$past_time && $db_date < $current_time) {
      if($db_flag == "end") {
	if(is_array($current_event[$current_time][$db_customer_id])) {
	  $current_event[$current_time][$db_customer_id] = array_diff($current_event[$current_time][$db_customer_id],array($db_id));
	}
       	else {
  	  $curent_event[$current_time][$db_customer_id] = array();
	}
	if(count($current_event[$current_time][$db_customer_id]) == 0) {
	  $current_event[$current_time][$db_customer_id] = -1;
	}
	$event_data[$db_id]["end"] = date("H:i",$db_date);    
      }
      else{
	if(!is_array($current_event[$current_time][$db_customer_id])) {
	  $current_event[$current_time][$db_customer_id] = array();
	}
	if(!is_array($current_event[$past_time][$db_customer_id])) {
	  $current_event[$past_time][$db_customer_id] = array();
	}	  
	$current_event[$past_time][$db_customer_id][] = $db_id;
	$current_event[$current_time][$db_customer_id][] = $db_id;
	$status = $event_data[$db_id]["status"] + 1;
	$event_data[$db_id] = 
	array("title"=>$db_title,"type"=>$db_cat_label,"description"=>$db_desc,"begin"=>date("H:i",$db_date),"status" => $status,"privacy" => $db_privacy);
      }	
      $obm_q->next_record();

      $db_date = $obm_q->f("calendarsegment_date");
      $db_customer_id = $obm_q->f("calendarsegment_customerid");
      $db_id = $obm_q->f("calendarevent_id");
      $db_title = $obm_q->f("calendarevent_title");
      $db_cat_label = $obm_q->f("calendarcategory_label");
      $db_desc = $obm_q->f("calendarevent_description");
      $db_flag = $obm_q->f("calendarsegment_flag");
      $db_privacy = $obm_q->f("calendarevent_privacy");
    }
 
    while($db_date == $current_time) {
      if($db_flag == "begin") {
	if(!is_array($current_event[$current_time][$db_customer_id])) {
  	  $current_event[$current_time][$db_customer_id] = array();
	}
	$current_event[$current_time][$db_customer_id][] = $db_id;
	$status = $event_data[$db_id]["status"] + 1;
	$event_data[$db_id] = 
	array("title"=>$db_title,"type"=>$db_cat_label,"description"=>$db_desc,"begin"=>date("H:i",$db_date),"status" => $status,"privacy" => $db_privacy);
	$obm_q->next_record(); 

      	$db_date = $obm_q->f("calendarsegment_date");
	$db_customer_id = $obm_q->f("calendarsegment_customerid");
	$db_id = $obm_q->f("calendarevent_id");
	$db_title = $obm_q->f("calendarevent_title");
	$db_cat_label = $obm_q->f("calendarcategory_label");
	$db_desc = $obm_q->f("calendarevent_description");
	$db_flag = $obm_q->f("calendarsegment_flag");
	$db_privacy = $obm_q->f("calendarevent_privacy");
      }elseif($db_flag == "end"){
	if(is_array($current_event[$current_time][$db_customer_id])) {
	  $current_event[$current_time][$db_customer_id] = array_diff($current_event[$current_time][$db_customer_id],array($db_id));
	}
       	else {
  	  $curent_event[$current_time][$db_customer_id] = array();
	}
	if(count($current_event[$current_time][$db_customer_id]) == 0) {
	  $current_event[$current_time][$db_customer_id] = -1;
	}	
	$event_data[$db_id]["end"] = date("H:i",$db_date);
        $obm_q->next_record();

	$db_date = $obm_q->f("calendarsegment_date");
	$db_customer_id = $obm_q->f("calendarsegment_customerid");
	$db_id = $obm_q->f("calendarevent_id");
	$db_title = $obm_q->f("calendarevent_title");
	$db_cat_label = $obm_q->f("calendarcategory_label");
	$db_desc = $obm_q->f("calendarevent_description");
	$db_flag = $obm_q->f("calendarsegment_flag");							   
	$db_privacy = $obm_q->f("calendarevent_privacy");
      }       
    }
    $past_time = $current_time;
  }    
  do {
    $db_id = $obm_q->f("calendarevent_id");
    $db_date = $obm_q->f("calendarsegment_date");
    $event_data[$db_id]["end"] = date("H:i",$db_date);
    $obm_q->next_record();
    $db_date = $obm_q->f("calendarsegment_date");
    $db_id = $obm_q->f("calendarevent_id");
    $current_event[$current_time][$db_customer_id] = -1;
    $event_data[$db_id]["end"] = date("H:i",$db_date);
  } while($obm_q->next_record());
  ksort($current_event);
}


/////////////////////////////////////////////////////////////////////////////
// Return tables of hashed events and of data event.(hashed by day for year
//                 and month view)
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    - $agenda : agenda params
//    - $contacts_array : array containing the id of the contact(s) the event is assigned to 
//    - $groups_array   : array containing the id of the group(s) the event is assigned to 
/////////////////////////////////////////////////////////////////////////////
function store_daily_events($obm_q,&$current_event,&$event_data,$p_date_begin,$p_date_end) {
  global $set_cal_interval;

  $obm_q->next_record();

  for($current_time=$p_date_begin;$current_time<$p_date_end;$current_time = strtotime("+1 day",$current_time)) {
    $current_date = date("Ymd",$current_time);
    $db_date = $obm_q->f("calendarsegment_date");
    $db_customer_id = $obm_q->f("calendarsegment_customerid");
    $db_id = $obm_q->f("calendarevent_id");
    $db_title = $obm_q->f("calendarevent_title");
    $db_cat_label = $obm_q->f("calendarcategory_label");
    $db_desc = $obm_q->f("calendarevent_description");
    $db_flag = $obm_q->f("calendarsegment_flag");	    
    $db_privacy = $obm_q->f("calendarevent_privacy");

    while($db_date && $db_date < $current_time) {
      $current_event[$current_date][$db_customer_id][] = $db_id;
      $status = $event_data[$db_id]["status"] + 1;
      $event_data[$db_id] = array("title"=>$db_title,"type"=>$db_cat_label,
      "description"=>$db_desc,"begin"=>date("H:i",$db_date),"status"=>$status,"privacy"=>$db_privacy);
      $obm_q->next_record(); 
      $db_date = $obm_q->f("calendarsegment_date");
      $db_customer_id = $obm_q->f("calendarsegment_customerid");
      $db_id = $obm_q->f("calendarevent_id");
      $db_title = $obm_q->f("calendarevent_title");
      $db_cat_label = $obm_q->f("calendarcategory_label");
      $db_desc = $obm_q->f("calendarevent_description");
      $db_flag = $obm_q->f("calendarsegment_flag");	    
      $db_privacy = $obm_q->f("calendarevent_privacy");      
    }
    if(is_array($current_event[$past_date])){
      foreach($current_event[$past_date] as $customer => $past_events) {
	if(is_array($past_events)) {
	  if(is_array($end_event)){
	    if(count($current_event[$current_date][$customer]=array_diff($past_events,$end_event))==0){
	      $current_event[$current_date][$customer]="";
	    }
	  }
	  else {
	    $current_event[$current_date][$customer]=$past_events;
	  }
	}
      }
    }
    $end_event ="";
    while($current_date == date("Ymd",$db_date)) {
      if($db_flag == "begin") {
	  $current_event[$current_date][$db_customer_id][] = $db_id;
	  $status = $event_data[$db_id]["status"] + 1;
  	  $event_data[$db_id] = 
  	    array("title"=>$db_title,"type"=>$db_cat_label,
  	    "description"=>$db_desc,"begin"=>date("H:i",$db_date)
	    ,"status"=>$status,"privacy"=>$db_privacy);
	  $obm_q->next_record(); 
      }elseif($db_flag == "end"){
	$end_event[] = $db_id;
        $event_data[$db_id]["end"] = date("H:i",$db_date);
	$obm_q->next_record();
      }       
      $db_date = $obm_q->f("calendarsegment_date");
      $db_customer_id = $obm_q->f("calendarsegment_customerid");
      $db_id = $obm_q->f("calendarevent_id");
      $db_title = $obm_q->f("calendarevent_title");
      $db_cat_label = $obm_q->f("calendarcategory_label");
      $db_desc = $obm_q->f("calendarevent_description");
      $db_flag = $obm_q->f("calendarsegment_flag");	    
      $db_privacy = $obm_q->f("calendarevent_privacy");   
    }
    $past_date = $current_date;
  }    
  do {
    $event_data[$db_id]["end"] =  date("H:i",$db_date);
    $db_date = $obm_q->f("calendarsegment_date");
    $db_id = $obm_q->f("calendarevent_id");
  }while($obm_q->next_record());

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
	                                      "class"=>"agendaEventBg$i","image"=>${"ico_calendar_user".$i});
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
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})",$datebegin , $day_array);
  $this_day_b = $day_array[3]; 
  $this_month_b = $day_array[2];
  $this_year_b = $day_array[1];
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})",$dateend , $day_array2);
  $this_day_e = $day_array2[3]; 
  $this_month_e = $day_array2[2];
  $this_year_e = $day_array2[1];
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})",$repeat_end , $day_array3);
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
  
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})",$agenda["date_begin"] , $day_array);
  $this_min_b = $day_array[5]; 
  $this_hour_b = $day_array[4]; 
  $this_day_b = $day_array[3]; 
  $this_month_b = $day_array[2];
  $this_year_b = $day_array[1];
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})",$agenda["date_end"] , $day_array2);
  $this_min_e = $day_array2[5]; 
  $this_hour_e = $day_array2[4];   
  $this_day_e = $day_array2[3]; 
  $this_month_e = $day_array2[2];
  $this_year_e = $day_array2[1];
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})",$repeat_end , $day_array3);
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
// Vcalendar Exportation : SQL Query for non repeated event
//------------------------------------------------------------------------
// Argument:
// ---------
//     - $agenda
///////////////////////////////////////////////////////////////////////////////
function run_query_get_vevent_norepeat($agenda) { 
  global $auth;

  $uid = $auth->auth["uid"];
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type;    
  $calendarsegment_date = sql_date_format($db_type, "calendarsegment_date","calendarsegment_date"); 
  
  $query = "SELECT calendarevent_id,
                   calendarevent_timeupdate,
                   calendarevent_title,
                   calendarevent_priority,
                   calendarevent_privacy,
		   calendarevent_description, 
		   calendarevent_length,
		   calendarcategory_label,
		   calendarsegment_customerid,
		   $calendarsegment_date,
		   calendarsegment_flag
	    FROM CalendarEvent,CalendarCategory, CalendarSegment
	    WHERE calendarsegment_customerid = '$uid'
	    AND calendarevent_category_id = calendarcategory_id
	    AND calendarevent_id = calendarsegment_eventid
	    AND calendarevent_repeatkind = 'none'
	    AND calendarsegment_state = 'A'
";

  $query.=" ORDER BY calendarevent_id,calendarsegment_date";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Vcalendar Exportation : SQL Query for repeated event
//------------------------------------------------------------------------
// Argument:
// ---------
//     - $agenda
///////////////////////////////////////////////////////////////////////////////
function run_query_get_vevent_repeat($agenda) { 
  global $auth;

  $obm_db = new DB_OBM;
  $db_type = $obm_db->type;  
  $calendarsegment_date = sql_date_format($db_type, "calendarsegment_date"); 
  $calendarsegment_endrepeat = sql_date_format($db_type, "calendarevent_endrepeat","calendarevent_endrepeat"); 
  $uid = $auth->auth["uid"];
  $query = "SELECT calendarevent_id,
                   calendarevent_timeupdate,
                   calendarevent_title,
                   calendarevent_priority,
                   calendarevent_privacy,
		   calendarevent_description,
		   calendarevent_repeatkind,
		   $calendarsegment_endrepeat,
		   calendarevent_repeatdays,
		   calendarevent_length,
		   calendarcategory_label,
		   calendarsegment_customerid,
		   MIN($calendarsegment_date) as calendarsegment_date,
		   calendarsegment_flag
	    FROM CalendarEvent,CalendarCategory, CalendarSegment
	    WHERE calendarsegment_customerid = '$uid'
	    AND calendarevent_category_id = calendarcategory_id
	    AND calendarevent_id = calendarsegment_eventid
	    AND calendarevent_repeatkind != 'none'
	    AND calendarsegment_state = 'A'";

  $query.="GROUP BY
  calendarevent_id,
  calendarsegment_flag,
  calendarevent_timeupdate,
  calendarevent_title,
  calendarevent_priority,
  calendarevent_privacy,
  calendarevent_description,
  calendarevent_repeatkind,
  calendarevent_endrepeat,
  calendarevent_repeatdays,
  calendarevent_length,
  calendarcategory_label,
  calendarsegment_customerid
  ORDER BY calendarevent_id,calendarsegment_date";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
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
    ereg ("([0-9]{4})([0-9]{2})([0-9]{2})",$date , $day_array);
    return $day_array[1]."-".$day_array[2]."-".$day_array[3];
  } elseif ($db_type == $db_type_mysql) {
    return $date;
  }
}

 
</script>
