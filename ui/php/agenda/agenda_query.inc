<?
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
  $date_time_d = strtotime($getdate);
  $date_time_f = strtotime("+1 day",$date_time_d);
  $getdate_d = date("YmdHi",$date_time_d); 
  $getdate_f = date("YmdHi",$date_time_f); 
  $query = "SELECT DISTINCT
      calendarevent_id,
      calendarevent_usercreate,
      calendarevent_userupdate,
      calendarevent_timecreate,
      calendarevent_timeupdate,
      calendarevent_title, 
      calendarevent_description,
      calendarevent_category_id,
      calendarcategory_label,
      calendarevent_privacy,
      calendarevent_priority,
      calendarevent_repeatkind,
      calendarevent_repeatdays,
      calendarevent_endrepeat,
      cs1.calendarsegment_date as datebegin,
      cs2.calendarsegment_date as dateend
    FROM CalendarEvent, CalendarCategory, CalendarSegment cs1, CalendarSegment cs2
    WHERE calendarevent_category_id = calendarcategory_id
      AND calendarevent_id = $param_event
      AND calendarevent_id = cs1.calendarsegment_eventid
      AND calendarevent_id = cs2.calendarsegment_eventid		  
      AND cs1.calendarsegment_flag = 'begin' AND cs1.calendarsegment_date < $getdate_f
      AND UNIX_TIMESTAMP(CONCAT(cs1.calendarsegment_date,'00')) + calendarevent_length > $date_time_d 
      AND cs2.calendarsegment_flag = 'end' AND cs2.calendarsegment_date > $getdate_d
      AND UNIX_TIMESTAMP(CONCAT(cs2.calendarsegment_date,'00')) - calendarevent_length < $date_time_f";
    
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
  $query = "SELECT DISTINCT userobm_lastname,userobm_firstname,userobm_id,calendarsegment_state,userobm_id
            FROM UserObm, CalendarSegment
            WHERE  calendarsegment_eventid = $param_event
	    AND calendarsegment_date LIKE '$getdate%'
	    AND calendarsegment_customerid = userobm_id";
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
  $query = "SELECT DISTINCT userobm_id
            FROM UserObm, CalendarSegment
            WHERE  calendarsegment_eventid = $param_event
            AND calendarsegment_date LIKE '$getdate%'
	    AND calendarsegment_customerid = userobm_id";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  while($obm_db->next_record()) {
    $p_arrayuserobm_id[] = $obm_db->f("userobm_id");
  }
  return $p_arrayuserobm_id;
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
  $start_day = date("YmdHi",$start_day_time);
  $end_day_time = strtotime("+$set_stop_time hours",strtotime($getdate));
  $end_day = date("YmdHi",$end_day_time);
  
  $obm_db = new DB_OBM;
  $query = "SELECT calendarevent_id,
      calendarevent_title,
      calendarevent_priority,
      calendarevent_privacy,
      calendarevent_description, 
      calendarcategory_label,
      calendarsegment_customerid,
      calendarsegment_date,
      calendarsegment_flag
    FROM CalendarEvent,CalendarCategory, CalendarSegment
    WHERE calendarevent_category_id = calendarcategory_id
      AND calendarevent_id = calendarsegment_eventid
      AND  calendarsegment_state = 'A'
      AND ( (calendarsegment_flag = 'begin'
             AND calendarsegment_date < $end_day
             AND UNIX_TIMESTAMP(CONCAT(calendarsegment_date,'00')) + calendarevent_length > $start_day_time)
	  OR (calendarsegment_flag = 'end'
              AND calendarsegment_date > $start_day
              AND UNIX_TIMESTAMP(CONCAT(calendarsegment_date,'00')) - calendarevent_length < $end_day_time)
          )";

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
  $start_week = date("YmdHi",$start_week_time);
  $end_week = date("YmdHi",$end_week_time);
  $obm_db = new DB_OBM;
  
  $query = "SELECT  calendarevent_id,
		   calendarevent_title,
		   calendarevent_priority,
		   calendarevent_privacy,
		   calendarevent_description, 
		   calendarcategory_label,
		   calendarsegment_customerid,
		   calendarsegment_date,
		   calendarsegment_flag 
	      FROM CalendarEvent,CalendarCategory, CalendarSegment 
	      WHERE calendarevent_category_id = calendarcategory_id
	      AND calendarevent_id = calendarsegment_eventid       
	      AND calendarsegment_state = 'A'
	      AND ((calendarsegment_flag = 'begin' AND calendarsegment_date < $end_week AND
	            UNIX_TIMESTAMP(CONCAT(calendarsegment_date,'00')) + calendarevent_length > $start_week_time)
	        OR (calendarsegment_flag = 'end' AND calendarsegment_date > $start_week AND	    
		    UNIX_TIMESTAMP(CONCAT(calendarsegment_date,'00')) - calendarevent_length < $end_week_time))
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
  $start_month = date("YmdHi",$start_month_time);
  $end_month_time = strtotime("+1 month",$start_month_time);
  $end_month = date("YmdHi",$end_month_time); 
  
  $obm_db = new DB_OBM;
  $query = "SELECT calendarevent_id,
                   calendarevent_usercreate,
		   calendarevent_title,
		   calendarevent_priority,
		   calendarevent_description, 
		   calendarevent_privacy,
		   calendarcategory_label,
		   calendarsegment_customerid,
		   calendarsegment_date,
		   calendarsegment_flag
            FROM CalendarEvent,CalendarCategory, CalendarSegment
	    WHERE calendarevent_category_id = calendarcategory_id
	      AND calendarevent_id = calendarsegment_eventid
	      AND calendarsegment_state = 'A'
	      AND ((calendarsegment_flag = 'begin' AND calendarsegment_date < $end_month AND
	            UNIX_TIMESTAMP(CONCAT(calendarsegment_date,'00')) + calendarevent_length > $start_month_time)
	        OR (calendarsegment_flag = 'end' AND calendarsegment_date > $start_month AND	    
		    UNIX_TIMESTAMP(CONCAT(calendarsegment_date,'00')) - calendarevent_length < $end_month_time))
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
  $start_year = date("YmdHi",$start_year_time);
  $end_year_time = strtotime("+1 year",$start_year_time);
  $end_year = date("YmdHi",$end_year_time);
 
  $obm_db = new DB_OBM;
  $query = "SELECT calendarevent_id,
                   calendarevent_usercreate,
		   calendarevent_title,
		   calendarevent_priority,
		   calendarevent_privacy,
		   calendarcategory_label,
		   calendarsegment_customerid,
		   calendarsegment_date,
		   calendarsegment_flag
	      FROM CalendarEvent,CalendarCategory, CalendarSegment 
	      WHERE calendarevent_category_id = calendarcategory_id
	      AND calendarevent_id = calendarsegment_eventid
	      AND calendarsegment_state = 'A'
	      AND ((calendarsegment_flag = 'begin' AND calendarsegment_date < $end_year AND
	            UNIX_TIMESTAMP(CONCAT(calendarsegment_date,'00')) + calendarevent_length > $start_year_time)
	        OR (calendarsegment_flag = 'end' AND calendarsegment_date > $start_year AND	    
		    UNIX_TIMESTAMP(CONCAT(calendarsegment_date,'00')) - calendarevent_length < $end_year_time))
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
  global $auth;

  $force = $agenda["force"];
  $return_info = array();
  $event_repeat_dates = get_event_repetition_dates($agenda);
  foreach($event_repeat_dates as $dates) {
    $conflicts = run_query_get_conflicts($dates["date_begin"],$dates["date_end"],$contacts_array);
    $return_info = array_merge($return_info, $conflicts);
  }
  if($force == 1 || count($return_info) == 0){    
    $event_id = run_query_insert_event_data($agenda);   
    foreach($event_repeat_dates as $dates) {
      foreach($contacts_array as $user_id) {
	if($user_id == $auth->auth["uid"]) {
	  if(count($return_info) != 0) {
	    $conflicts = run_query_get_conflicts_user($dates["date_begin"],$dates["date_end"],$user_id);
	    if($conflicts->nf() != 0) {
	      run_query_insert_occurence($dates["date_begin"],$dates["date_end"],$user_id,$event_id,'W');	
	    }
	    else{
	      run_query_insert_occurence($dates["date_begin"],$dates["date_end"],$user_id,$event_id,'A');	
	    }	    
	  }
	  else {
	    run_query_insert_occurence($dates["date_begin"],$dates["date_end"],$user_id,$event_id,'A');	
	  }
	}
	else{
	  run_query_insert_occurence($dates["date_begin"],$dates["date_end"],$user_id,$event_id,'W');	
	}
      }
    }
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
  $event_id = $agenda["id"];
  $state = $agenda["decision_event"];
  if ($state == 'A') {
    $conflicts = run_query_get_latent_conflicts($event_id, $auth->auth["uid"]);
  }
  else{
    $conflicts = array();
  }
  run_query_insert_occurence_state($event_id,$auth->auth["uid"],$state,$conflicts);
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
  $query = "SELECT DISTINCT
      cs1.calendarsegment_date as datebegin,
      cs2.calendarsegment_date as dateend
    FROM  CalendarSegment cs1, CalendarSegment cs2, CalendarEvent
    WHERE cs1.calendarsegment_customerid = $user_id
      AND cs1.calendarsegment_state = 'W'
      AND cs2.calendarsegment_customerid = $user_id
      AND cs2.calendarsegment_state = 'W'
      AND cs2.calendarsegment_eventid = cs1.calendarsegment_eventid
      AND cs1.calendarsegment_eventid = $event_id	
      AND cs1.calendarsegment_flag = 'begin'
      AND cs2.calendarsegment_flag = 'end'
      AND calendarevent_id = cs1.calendarsegment_eventid
      AND UNIX_TIMESTAMP(CONCAT(cs2.calendarsegment_date,'00')) - calendarevent_length = UNIX_TIMESTAMP(CONCAT(cs1.calendarsegment_date,'00'))
    ORDER BY cs1.calendarsegment_date";

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

  $obm_db = new DB_OBM;
  $query = "UPDATE CalendarSegment 
            SET calendarsegment_state = '$state'
            WHERE calendarsegment_customerid = $user_id
		  AND calendarsegment_state = 'W'
		  AND calendarsegment_eventid = $event_id";
  if(count($conflicts) != 0) {
    $query .= " AND calendarsegment_date NOT IN ('".$conflicts[0]["date_begin"]."','".$conflicts[0]["date_end"]."'";
    for ($i=1;$i<count($contacts_array);$i++) {
      $query.= ",'".$conflicts[$i]["date_begin"]."','".$conflicts[$i]["date_end"]."'";
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
  $obm_db = new DB_OBM;
  $query = "SELECT DISTINCT
                   calendarevent_id,
                   calendarevent_usercreate,
                   calendarevent_title, 
		   calendarcategory_label,
		   calendarevent_privacy,
		   calendarevent_priority,
		   calendarevent_repeatkind,
		   calendarevent_repeatdays,
		   calendarevent_endrepeat,
		   MIN(cs1.calendarsegment_date) as datebegin,
		   MIN(cs2.calendarsegment_date) as dateend
            FROM CalendarEvent, CalendarCategory, CalendarSegment cs1, CalendarSegment cs2
	    WHERE calendarevent_category_id = calendarcategory_id
	      	  AND cs1.calendarsegment_customerid = ".$auth->auth["uid"]."
		  AND cs1.calendarsegment_state = 'W'
  		  AND cs2.calendarsegment_customerid = ".$auth->auth["uid"]."
		  AND cs2.calendarsegment_state = 'W'
	          AND calendarevent_id = cs1.calendarsegment_eventid
		  AND calendarevent_id = cs2.calendarsegment_eventid	
		  AND cs1.calendarsegment_flag = 'begin'
		  AND cs2.calendarsegment_flag = 'end'
		 GROUP BY calendarevent_id"; 
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
  
  $date_begin_time = mktime(substr($date_begin,8,2), substr($date_begin,10,2), "00", substr($date_begin,4,2), substr($date_begin,6,2), substr($date_begin,0,4));
  $date_end_time = mktime(substr($date_end,8,2), substr($date_end,10,2), "00", substr($date_end,4,2), substr($date_end,6,2), substr($date_end,0,4));
  
  $query = "SELECT DISTINCT calendarsegment_date FROM CalendarSegment,CalendarEvent WHERE
            calendarsegment_customerid = $user_id AND calendarsegment_eventid = $event_id
	    AND calendarsegment_eventid = calendarevent_id
	    AND ((calendarsegment_flag = 'begin' AND calendarsegment_date < $date_end AND
	          UNIX_TIMESTAMP(CONCAT(calendarsegment_date,'00')) + calendarevent_length > $date_begin_time)
	      OR (calendarsegment_flag = 'end' AND calendarsegment_date > $date_begin AND	    
		  UNIX_TIMESTAMP(CONCAT(calendarsegment_date,'00')) - calendarevent_length < $date_end_time))
             ";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  $obm_db->next_record();
  
  $query = "UPDATE CalendarSegment SET calendarsegment_state = '$state' WHERE
            calendarsegment_customerid = $user_id AND calendarsegment_eventid = $event_id
	    AND  calendarsegment_date IN ('".$obm_db->f("calendarsegment_date")."'";
  while ( $obm_db->next_record()) {
    $query.= ",'".$obm_db->f("calendarsegment_date")."'";
  }
  $query.=")";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);

  if($state == 'R') {
    $query = "SELECT COUNT(*) AS isDeprectated FROM CalendarSegment WHERE calendarsegment_eventid = $event_id AND calendarsegment_state != 'R'";
    $obm_db->query($query);
    $obm_db->next_record();    
    if($obm_db->f("isDeprectated") == 0) {
      $query = "DELETE FROM CalendarSegment WHERE calendarsegment_eventid = $event_id";
      $obm_db->query($query);
      $query = "DELETE FROM CalendarEvent WHERE calendarevent_id = $event_id";
      $obm_db->query($query);
    }
  }

}
  
/////////////////////////////////////////////////////////////////////////////
// Insert a occurence of a event
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    - $date_begin : 
//    - $date_end : 
//    - $user_id :
//    - $event_id :
//    - $state :
/////////////////////////////////////////////////////////////////////////////

function run_query_insert_occurence($date_begin,$date_end,$user_id,$event_id,$state) {
  global $cdg_sql;
  $obm_db = new DB_OBM;
  $query = "INSERT INTO CalendarSegment (calendarsegment_eventid, calendarsegment_customerid, calendarsegment_date, calendarsegment_flag,
                                         calendarsegment_type, calendarsegment_state) 
                                  VALUES ($event_id,$user_id,'$date_begin','begin','user','$state')";
  
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  $query = "INSERT INTO CalendarSegment (calendarsegment_eventid, calendarsegment_customerid, calendarsegment_date, calendarsegment_flag,
                                         calendarsegment_type, calendarsegment_state) 
                                  VALUES ($event_id,$user_id,'$date_end','end','user','$state')";
  
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

  $date_begin_time = mktime(substr($date_begin,8,2), substr($date_begin,10,2), "00", substr($date_begin,4,2), substr($date_begin,6,2), substr($date_begin,0,4));

  $date_end_time = mktime(substr($date_end,8,2), substr($date_end,10,2), "00", substr($date_end,4,2), substr($date_end,6,2), substr($date_end,0,4));

  $query = "SELECT DISTINCT calendarsegment_eventid ,calendarsegment_date
            ,calendarsegment_date FROM  CalendarEvent, CalendarSegment
	    WHERE calendarsegment_eventid = calendarevent_id AND calendarsegment_state = 'A' AND
	          ((calendarsegment_flag = 'begin' AND calendarsegment_date < $date_end AND
	            UNIX_TIMESTAMP(CONCAT(calendarsegment_date,'00')) + calendarevent_length > $date_begin_time)
	        OR (calendarsegment_flag = 'end' AND calendarsegment_date > $date_begin AND	    
		    UNIX_TIMESTAMP(CONCAT(calendarsegment_date,'00')) - calendarevent_length < $date_end_time))
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
 
  $date_begin_time = mktime(substr($date_begin,8,2), substr($date_begin,10,2), "00", substr($date_begin,4,2), substr($date_begin,6,2), substr($date_begin,0,4));

  $date_end_time = mktime(substr($date_end,8,2), substr($date_end,10,2), "00", substr($date_end,4,2), substr($date_end,6,2), substr($date_end,0,4));

  $query = "SELECT DISTINCT cs1.calendarsegment_eventid ,cs1.calendarsegment_customerid,cs1.calendarsegment_date as date_begin,
                   cs2.calendarsegment_date as date_end, calendarevent_title,userobm_lastname,userobm_firstname
            FROM   CalendarEvent, CalendarSegment cs1,CalendarSegment cs2, UserObm 
	    WHERE cs1.calendarsegment_eventid = calendarevent_id AND userobm_id = cs1.calendarsegment_customerid 
	          AND cs1.calendarsegment_state = 'A' AND cs2.calendarsegment_state = 'A' 
		  AND cs2.calendarsegment_eventid = calendarevent_id AND userobm_id = cs2.calendarsegment_customerid
	          AND (cs1.calendarsegment_flag = 'begin' AND cs1.calendarsegment_date < $date_end AND
		  UNIX_TIMESTAMP(CONCAT(cs1.calendarsegment_date,'00')) + calendarevent_length > $date_begin_time)
		  AND (cs2.calendarsegment_flag = 'end' AND UNIX_TIMESTAMP(CONCAT(cs1.calendarsegment_date,'00')) 
		  + calendarevent_length = UNIX_TIMESTAMP(CONCAT(cs2.calendarsegment_date,'00')))
	    ";  		  
  $query .= " AND cs1.calendarsegment_customerid IN ('".$contacts_array[0]."'";
  for ($i=1;$i<count($contacts_array);$i++) {
    $query.= ",'".$contacts_array[$i]."'";
  }
  $query.=")";
  $query .= " ORDER BY cs1.calendarsegment_date, cs1.calendarsegment_customerid";
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
 
  $date_begin_time = mktime(substr($date_begin,8,2), substr($date_begin,10,2), "00", substr($date_begin,4,2), substr($date_begin,6,2), substr($date_begin,0,4));

  $date_end_time = mktime(substr($date_end,8,2), substr($date_end,10,2), "00", substr($date_end,4,2), substr($date_end,6,2), substr($date_end,0,4));

  $query = "SELECT DISTINCT cs1.calendarsegment_eventid ,cs1.calendarsegment_customerid,cs1.calendarsegment_date as date_begin,
                   cs2.calendarsegment_date as date_end, calendarevent_title,userobm_lastname,userobm_firstname
            FROM   CalendarEvent, CalendarSegment cs1,CalendarSegment cs2, UserObm 
	    WHERE cs1.calendarsegment_eventid = calendarevent_id AND userobm_id = cs1.calendarsegment_customerid 
	          AND cs1.calendarsegment_state = 'A' AND cs2.calendarsegment_state != 'A' 
		  AND calendarevent_id != $event_id
		  AND cs2.calendarsegment_eventid = calendarevent_id AND userobm_id = cs2.calendarsegment_customerid
	          AND (cs1.calendarsegment_flag = 'begin' AND cs1.calendarsegment_date < $date_end AND
		  UNIX_TIMESTAMP(CONCAT(cs1.calendarsegment_date,'00')) + calendarevent_length > $date_begin_time)
		  AND (cs2.calendarsegment_flag = 'end' AND UNIX_TIMESTAMP(CONCAT(cs1.calendarsegment_date,'00')) 
		  + calendarevent_length = UNIX_TIMESTAMP(CONCAT(cs2.calendarsegment_date,'00')))
	    ";  		  
  $query .= " AND cs1.calendarsegment_customerid IN ('".$contacts_array[0]."'";
  for ($i=1;$i<count($contacts_array);$i++) {
    $query.= ",'".$contacts_array[$i]."'";
  }
  $query.=")";
  $query .= " ORDER BY cs1.calendarsegment_date, cs1.calendarsegment_customerid";
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
  $datebegin = substr($agenda["date_begin"],0,8);
  $timebegin = substr($agenda["date_begin"],8,4);
  $dateend = substr($agenda["date_end"],0,8);
  $timeend = substr($agenda["date_begin"],8,6);
  $stamp_beg = mktime(substr($agenda["date_begin"],8,2), substr($agenda["date_begin"],10,2), "00", substr($agenda["date_begin"],4,2), substr($agenda["date_begin"],6,2), substr($agenda["date_begin"],0,4));
  $stamp_end = mktime(substr($agenda["date_end"],8,2), substr($agenda["date_end"],10,2), "00", substr($agenda["date_end"],4,2), substr($agenda["date_end"],6,2), substr($agenda["date_end"],0,4));
  $length = $stamp_end - $stamp_beg;
 
  if($agenda["privacy"]!=1) $privacy = 0;else $privacy = 1; 
  $repeat_kind = $agenda["kind"];
  $repeat_days = $agenda["repeat_days"]; 
  $repeat_end = $agenda["repeat_end"];
  if($repeat_end) {
    $repeat_end = date("YmdHi",strtotime("+1 day",strtotime($repeat_end)));
  }
  else {
    $repeat_end = date("YmdHi",strtotime("+1 year +1 day", strtotime(date("Ymd"))));
  } 
  
  $obm_db = new DB_OBM;
  $obm_db->lock("CalendarEvent");
  $query = "SELECT MAX(calendarevent_id) as max_id FROM CalendarEvent";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  $obm_db->next_record(); 
  $max_id = $obm_db->f("max_id")+1;
  $query = "INSERT INTO CalendarEvent (calendarevent_id,calendarevent_timeupdate, calendarevent_timecreate, calendarevent_userupdate, 
                         calendarevent_usercreate, calendarevent_title, calendarevent_description,
			 calendarevent_category_id,calendarevent_priority,calendarevent_privacy, calendarevent_length,
			 calendarevent_repeatkind,calendarevent_repeatdays,calendarevent_endrepeat)
	         VALUES ($max_id,null,'".date("YmdHis")."',null,".$auth->auth["uid"].",'$title',
		         '$description',$category_id,$priority,$privacy,'$length',
			 '$repeat_kind','$repeat_days','$repeat_end')";

  display_debug_msg($query, $cdg_sql);
  $obm_db->unlock();  
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
  global $auth;
  $force = $agenda["force"];
  $repeat_update = $agenda["repeat_update"];
  $old_event_id = $agenda["id"];
  if($repeat_update != 1) {
    $agenda["kind"] ="none";
    $agenda["repeat_days"] ="0000000";
    $agenda["repeat_end"] = "";
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
      foreach($contacts_array as $user_id) {
	if($user_id == $auth->auth["uid"]) {
	  if(count($return_info) != 0) {
	    $conflicts = run_query_get_conflicts_user($dates["date_begin"],$dates["date_end"],$user_id);
	    if(count($conflicts) != 0) {
	      run_query_insert_occurence($dates["date_begin"],$dates["date_end"],$user_id,$event_id,'W');	
	    }
	    else{
	      run_query_insert_occurence($dates["date_begin"],$dates["date_end"],$user_id,$event_id,'A');	
	    }	    
	  }
	  else {
	    run_query_insert_occurence($dates["date_begin"],$dates["date_end"],$user_id,$event_id,'A');	
	  }
	}
	else{
	  run_query_insert_occurence($dates["date_begin"],$dates["date_end"],$user_id,$event_id,'W');	
	}
      }
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
  global $cdg_sql;  
  $repeat_update = $agenda["repeat_update"];
  $old_event_id = $agenda["id"];
  $date_begin = $agenda["date_begin"];
  $old_date_begin = $agenda["old_begin"];
  $old_date_end = $agenda["old_end"];
  $obm_db = new DB_OBM;
  $query = "DELETE FROM CalendarSegment WHERE
                  calendarsegment_eventid = $old_event_id AND (calendarsegment_date
		  ";
  if($repeat_update == 1) {
    $query .= ">= '$old_date_begin')";
    $query2 = "UPDATE CalendarEvent Set calendarevent_endrepeat = $date_begin WHERE calendarevent_id = $old_event_id";
  }else{
    $query .= "= '$old_date_begin' OR calendarsegment_date = '$old_date_begin')";
  }
 
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
 
  display_debug_msg($query2, $cdg_sql);
  $obm_db->query($query2);
  
}  
/////////////////////////////////////////////////////////////////////////////
// Delete all events of a evenements, and the event
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    - $agenda
/////////////////////////////////////////////////////////////////////////////

function run_query_delete($agenda) {
  global $cdg_sql;
  $obm_db = new DB_OBM;
  $event_id = $agenda["id"];
  
  $query = "DELETE FROM CalendarSegment WHERE calendarsegment_eventid = $event_id";
  $obm_db->query($query);
  $query = "DELETE FROM CalendarEvent WHERE calendarevent_id = $event_id";
  $obm_db->query($query);
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
  global  $set_start_time, $set_stop_time,$set_weekstart_default,$set_time_unit;
  $obm_q->next_record();
  $time_unit = 60 / $set_time_unit;
  $s =0;
  $unix_time = mktime(substr($p_date_begin,8,2), substr($p_date_begin,10,2), "00", 
                      substr($p_date_begin,4,2), substr($p_date_begin,6,2), substr($p_date_begin,0,4));
  for($current_time=$p_date_begin;$current_time<=$p_date_end;
      $current_time=date("YmdHi",$unix_time)) {
    if(date("G",$unix_time) < $set_stop_time){
      $unix_time = strtotime("+$time_unit minutes",$unix_time);	
    }
    else {
      $tonextday = $set_start_time - $set_stop_time;
      $unix_time = strtotime("+1 day $tonextday hours",$unix_time);
    }
    if(is_array($current_event[$past_time])) {
      $temp_array = array_filter($current_event[$past_time],"valid_event");
      if(count($temp_array)>0){
	$current_event[$current_time] = $temp_array;
      }
    }
    while($obm_q->f("calendarsegment_date") != "" && $obm_q->f("calendarsegment_date") < $current_time) {
      $current_event[$current_time][$obm_q->f("calendarsegment_customerid")][] = $obm_q->f("calendarevent_id");
      $status = $event_data[$obm_q->f("calendarevent_id")]["status"] + 1;
      $event_data[$obm_q->f("calendarevent_id")] = 
        array("title"=>$obm_q->f("calendarevent_title"),"type"=>$obm_q->f("calendarcategory_label"),
        "begin"=>substr($obm_q->f("calendarsegment_date"),8,2).":".
        substr($obm_q->f("calendarsegment_date"),10,2),"status" => $status);
      $obm_q->next_record(); 
    }
    while($obm_q->f("calendarsegment_date") == $current_time) {
      if($obm_q->f("calendarsegment_flag") == "begin") {
	$current_event[$current_time][$obm_q->f("calendarsegment_customerid")][] = $obm_q->f("calendarevent_id");
	$status = $event_data[$obm_q->f("calendarevent_id")]["status"] + 1;
	$event_data[$obm_q->f("calendarevent_id")] = 
  	array("title"=>$obm_q->f("calendarevent_title"),"type"=>$obm_q->f("calendarcategory_label"),
	"description"=>$obm_q->f("calendarevent_description"),"begin"=>substr($obm_q->f("calendarsegment_date"),8,2).":".
	substr($obm_q->f("calendarsegment_date"),10,2),"status" => $status);
	$obm_q->next_record(); 
      }elseif($obm_q->f("calendarsegment_flag") == "end"){
	if(is_array($current_event[$current_time][$obm_q->f("calendarsegment_customerid")])) {
	  $current_event[$current_time][$obm_q->f("calendarsegment_customerid")] = array_diff($current_event[$current_time][$obm_q->f("calendarsegment_customerid")],array($obm_q->f("calendarevent_id")));
	}
       	else {
  	  $curent_event[$current_time][$obm_q->f("calendarsegment_customerid")] = array();
	}
	if(count($current_event[$current_time][$obm_q->f("calendarsegment_customerid")]) == 0) {
	  $current_event[$current_time][$obm_q->f("calendarsegment_customerid")] = -1;
	}
	
	$event_data[$obm_q->f("calendarevent_id")]["end"] = substr($obm_q->f("calendarsegment_date"),8,2).":".
							   substr($obm_q->f("calendarsegment_date"),10,2);
	$obm_q->next_record(); 
      }       
    }
    $past_time = $current_time;
  }    
  do {
    $current_event[$current_time][$obm_q->f("calendarsegment_customerid")] = -1;
    $event_data[$obm_q->f("calendarevent_id")]["end"] = substr($obm_q->f("calendarsegment_date"),8,2).":".
                                                        substr($obm_q->f("calendarsegment_date"),10,2);
  }while($obm_q->next_record());

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
  global $set_time_unit;
  $obm_q->next_record();
  $unix_time = strtotime($p_date_begin);
  for($current_time=$p_date_begin;$current_time<$p_date_end;$current_time=date("Ymd",$unix_time)) {
    $unix_time = strtotime("+1 day",$unix_time);	
    while($obm_q->f("calendarsegment_date") != "" && substr($obm_q->f("calendarsegment_date"),0,8) < $current_time) {
      $current_event[$current_time][$obm_q->f("calendarsegment_customerid")][] = $obm_q->f("calendarevent_id");
      $status = $event_data[$obm_q->f("calendarevent_id")]["status"] + 1;
      $event_data[$obm_q->f("calendarevent_id")] = 
        array("title"=>$obm_q->f("calendarevent_title"),"type"=>$obm_q->f("calendarcategory_label"),
        "description"=>$obm_q->f("calendarevent_description"),"begin"=>substr($obm_q->f("calendarsegment_date"),8,2).":".
        substr($obm_q->f("calendarsegment_date"),10,2),"status"=>$status);
      $obm_q->next_record(); 
    }
    if(is_array($current_event[$past_time])){
      foreach($current_event[$past_time] as $customer => $past_events) {
	if(is_array($past_events)) {
	  if(is_array($end_event)){
	    if(count($current_event[$current_time][$customer]=array_diff($past_events,$end_event))==0){
	      $current_event[$current_time][$customer]="";
	    }
	  }
	  else {
	    $current_event[$current_time][$customer]=$past_events;
	  }
	}
      }
    }
    $end_event ="";
    while(substr($obm_q->f("calendarsegment_date"),0,8) == $current_time) {
      if($obm_q->f("calendarsegment_flag") == "begin") {
	  $current_event[$current_time][$obm_q->f("calendarsegment_customerid")][] = $obm_q->f("calendarevent_id");
	  $status = $event_data[$obm_q->f("calendarevent_id")]["status"] + 1;
  	  $event_data[$obm_q->f("calendarevent_id")] = 
  	    array("title"=>$obm_q->f("calendarevent_title"),"type"=>$obm_q->f("calendarcategory_label"),
  	    "description"=>$obm_q->f("calendarevent_description"),"begin"=>substr($obm_q->f("calendarsegment_date"),8,2).":".
  	    substr($obm_q->f("calendarsegment_date"),10,2),"status"=>$status);
	  $obm_q->next_record(); 
      }elseif($obm_q->f("calendarsegment_flag") == "end"){
	$end_event[] = $obm_q->f("calendarevent_id");
        $event_data[$obm_q->f("calendarevent_id")]["end"] = substr($obm_q->f("calendarsegment_date"),8,2).":".
  	                                                    substr($obm_q->f("calendarsegment_date"),10,2);
	$obm_q->next_record();
      }       
    }
    $past_time = $current_time;
  }    
  do {
    $event_data[$obm_q->f("calendarevent_id")]["end"] = substr($obm_q->f("calendarsegment_date"),8,2).":".
                                                        substr($obm_q->f("calendarsegment_date"),10,2);
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
// Agenda Form Data checking and formatting                                 //
// Parameters:
//   - $agenda[] : values checked
//     keys used  : num, name, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_data_form($agenda) {
  global $l_fill_title, $l_fill_dateend, $l_fill_datebegin, $l_err_datebegin, $l_err_dateend,$err_msg ;
  global $l_err_begin_end ,$l_err_end_repeat, $l_err_repeat,$l_err_end_repeat2 ;
  
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


  if (trim($title) == "") {
    $err_msg = $l_fill_title;
    return false;
  }
  
  if (trim($datebegin) == "") {
    $err_msg = $l_fill_datebegin;
    return false;
  }
  elseif(!checkdate($this_month_b,$this_day_b,$this_year_b)) {    
    $err_msg = $l_err_datebegin;
    return false;
  }  
  
  if (trim($dateend) == "") {
    $err_msg = $l_fill_dateend;
    return false;
  }
  elseif(!checkdate($this_month_e,$this_day_e,$this_year_e)) {    
    $err_msg = $l_err_dateend;
    return false;
  }  
  
  if (trim($repeat_end) != "") {
    if(!checkdate($this_month_r,$this_day_r,$this_year_r)) {    
      $err_msg = $l_err_repeat;
      return false;
    }  
  }

  if($dateend<$datebegin) {
    $err_msg =  $l_err_begin_end;
    return false;
  } 
  
  if(trim($repeat_end) != "" && $dateend>$repeat_end) {
    $err_msg =  $l_err_end_repeat;
    return false;
  }  
  if(trim($repeat_end) != "" && ($dateend+10000) < $repeat_end) {
    $err_msg =  $l_err_end_repeat2;
    return false;
  }  
  
  return true; 
}



///////////////////////////////////////////////////////////////////////////////
// Agenda Form Data checking and formatting                                 //
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
    $event_dates[] = array("date_begin" => date("YmdHi",$time_b),"date_end" => date("YmdHi",$time_e));
  }    
  elseif ($repeat_kind=="daily") {
    for($var_time_b=$time_b, $var_time_e=$time_e;
    $var_time_b <= $time_end_repeat;
    $var_time_b = strtotime("+1 day",$var_time_b), $var_time_e = strtotime("+1 day",$var_time_e)){
      $event_dates[]= array("date_begin" => date("YmdHi",$var_time_b),"date_end" => date("YmdHi",$var_time_e));
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
	  $event_dates[]=array("date_begin" => date("YmdHi",strtotime("+$i days",$var_time_b)),
	                       "date_end" => date("YmdHi",strtotime("+$i days",$var_time_e)));
	}
      }
    }  
  }  
  else if ($repeat_kind=="monthlybydate") {
    for($var_time_b=$time_b, $var_time_e=$time_e;
    $var_time_b <= $time_end_repeat;
    $var_time_b = strtotime("+1 month",$var_time_b), $var_time_e = strtotime("+1 month",$var_time_e)){
      $event_dates[]= array("date_begin" => date("YmdHi",$var_time_b),"date_end" => date("YmdHi",$var_time_e));
    }
  }
  else if ($repeat_kind=="monthlybyday") {
    $var_time_b = $time_b;
    $var_time_e = $time_e;
    $start_week_day = date("w",strtotime($set_weekstart_default)); 
    $day_beg = (date("w",$var_time_b) - $start_week_day)%7;
    $day_end = (date("w",$var_time_e) - $start_week_day)%7;   
    $tab_beg = array((($day_beg + 4)%7) => '+3',(($day_beg + 5)%7) => '+2',(($day_beg + 6)%7) => '+1',$day_beg => '+0',
               (($day_beg + 8)%7) => '-1',(($day_beg + 9)%7) => '-2',(($day_beg + 10)%7) => '-3');
    $tab_end = array((($day_end + 4)%7) => '+3',(($day_end + 5)%7) => '+2',(($day_end + 6)%7) => '+1',$day_end => '+0',
               (($day_end + 8)%7) => '-1',(($day_end + 9)%7) => '-2',(($day_end + 10)%7) => '-3'); 
    while($var_time_b <= $time_end_repeat){
      $event_dates[]= array("date_begin" => date("YmdHi",$time_b),"date_end" => date("YmdHi",$time_e));     
      $var_time_b = strtotime("+1 month",$var_time_b);
      $var_time_e = strtotime("+1 month",$var_time_e);
      $day_beg = ((date("w",$var_time_b) - $start_week_day)+7)%7;
      $day_end = ((date("w",$var_time_e) - $start_week_day)+7)%7;
      $time_b = strtotime($tab_beg[$day_beg]." days",$var_time_b);
      $time_e = strtotime($tab_end[$day_end]." days",$var_time_e);


      if(date("d",$time_b) < 1) {
	$time_b = strtotime("+7 days",$time_b);
      }
      if(date("d",$time_e) < 1) {
	$time_e = strtotime("+7 days",$time_e);
      } 
    }
  } else if ($repeat_kind=="yearly") {  
    for($var_time_b=$time_b, $var_time_e=$time_e;
    $var_time_b <= $time_end_repeat;
    $var_time_b = strtotime("+1 year",$var_time_b), $var_time_e = strtotime("+1 year",$var_time_e)){  
      $event_dates[]= array("date_begin" => date("YmdHi",$var_time_b),"date_end" => date("YmdHi",$var_time_e));
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
