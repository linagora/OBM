<?
///////////////////////////////////////////////////////////////////////////////
// OBM - File : agenda_query.inc                                             //
//     - Desc : Agenda query File                                            //
// Created    : 2001-06-27 by Mehdi Rande                                //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a day of users or/and groups
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    $agenda : agenda params
//    $contacts_array : array containing the id of the contact(s) the event is assigned to 
//    $groups_array   : array containing the id of the group(s) the event is assigned to 
/////////////////////////////////////////////////////////////////////////////

function run_query_day_event_list($agenda,$contacts_array) {
  global $cdg_sql, $set_start_time, $set_stop_time;
  
  $getdate = $agenda["date"];
  $start_day = strtotime("+$set_start_time hours",strtotime($getdate));
  $start_day = date("YmdHi",$start_day);
  $end_day = strtotime("+$set_stop_time hours",strtotime($getdate));
  $end_day = date("YmdHi",$end_day);
  
  $obm_db = new DB_OBM;
  $query = "SELECT calendarevent_id,
		   calendarevent_title,
		   calendarevent_priority,
		   calendarevent_privacy,
		   calendarcategory_label,
		   cs1.calendarsegment_customerid,
		   cs1.calendarsegment_date,
		   cs1.calendarsegment_flag
		   FROM CalendarEvent,CalendarCategory, CalendarSegment cs1,CalendarSegment cs2
	    WHERE calendarevent_category_id = calendarcategory_id
	      AND calendarevent_id = cs1.calendarsegment_eventid
	      AND cs1.calendarsegment_eventid = cs2.calendarsegment_eventid
	      AND cs1.calendarsegment_customerid = cs2.calendarsegment_customerid
	      AND cs1.calendarsegment_state = 'A'
	      AND ((cs1.calendarsegment_flag = 'begin' AND cs1.calendarsegment_date < $end_day
	            AND cs2.calendarsegment_flag = 'end' AND cs2.calendarsegment_date > $start_day)
		OR (cs1.calendarsegment_flag = 'end' AND cs1.calendarsegment_date > $start_day
	    	    AND cs2.calendarsegment_flag = 'begin' AND cs2.calendarsegment_date < $end_day))
	      ";



  if(is_array($contacts_array) and (count($contacts_array)>0) ) {
    $query .= "AND cs1.calendarsegment_customerid IN ('".$contacts_array[0]."'";
    for ($i=1;$i<count($contacts_array);$i++) {
      $query.= ",'".$contacts_array[$i]."'";
    }
    $query.=")";
  }    
        
  $query.=" ORDER BY cs1.calendarsegment_date"; 
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}

/////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a week of users or/and groups
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    $agenda : agenda params
//    $contacts_array : array containing the id of the contact(s) the event is assigned to 
//    $groups_array   : array containing the id of the group(s) the event is assigned to 
/////////////////////////////////////////////////////////////////////////////

function run_query_week_event_list($agenda,$contacts_array) {
  global $cdg_sql, $set_start_time, $set_stop_time,$set_weekstart_default;
  
  $getdate = $agenda["date"];
  $start_week_time = strtotime(dateOfWeek($getdate, $set_weekstart_default));
  $end_week_time = $start_week_time + ((7 * 24 + $set_stop_time) * 60 * 60);
  $start_week_time = date("YmdHi",strtotime("+ $set_start_time hours",$start_week_time));
  $end_week_time = date("YmdHi",$end_week_time);
  
  $obm_db = new DB_OBM;
  $query = "SELECT DISTINCT calendarevent_id,
		   calendarevent_title,
		   calendarevent_priority,
		   calendarevent_privacy,
		   calendarcategory_label,
		   cs1.calendarsegment_customerid,
		   cs1.calendarsegment_date,
		   cs1.calendarsegment_flag 
	      FROM CalendarEvent,CalendarCategory, CalendarSegment cs1,CalendarSegment cs2,CalendarSegment cs3
	      WHERE calendarevent_category_id = calendarcategory_id
	      AND calendarevent_id = cs1.calendarsegment_eventid 
	      AND cs1.calendarsegment_eventid = cs2.calendarsegment_eventid
	      AND cs1.calendarsegment_customerid = cs2.calendarsegment_customerid
	      AND cs1.calendarsegment_eventid = cs3.calendarsegment_eventid
	      AND cs1.calendarsegment_customerid = cs3.calendarsegment_customerid	      
	      AND cs1.calendarsegment_state = 'A'
	      AND ((cs1.calendarsegment_flag = 'begin' AND cs1.calendarsegment_date < $end_week_time
	            AND cs2.calendarsegment_flag = 'end' AND cs2.calendarsegment_date < $start_week_time
		    AND cs2.calendarsegment_date < cs1.calendarsegment_date))";
//    	        OR (cs1.calendarsegment_flag = 'end' AND cs1.calendarsegment_date > $start_week_time
//		    AND cs3.calendarsegment_flag = 'end' AND cs3.calendarsegment_date < cs1.calendarsegment_date
//		    AND cs2.calendarsegment_date > cs3.calendarsegment_date 
//		    AND cs2.calendarsegment_flag = 'begin' AND cs2.calendarsegment_date < $end_week_time))		      
//	      ";
	      
  if(is_array($contacts_array) and (count($contacts_array)>0) ) {
    $query .= "AND  cs1.calendarsegment_customerid IN ('".$contacts_array[0]."'";
    for ($i=1;$i<count($contacts_array);$i++) {
      $query.= ",'".$contacts_array[$i]."'";
    }
    $query.=")";
  }    
  $query.=" ORDER BY cs1.calendarsegment_date"; 
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  while($obm_db->next_record()){echo $obm_db->f("calendarevent_title")."-F".$obm_db->f("calendarsegment_flag")."-D".$obm_db->f("calendarsegment_date")."-I".$obm_db->f("calendarevent_id")."-P".$obm_db->f("calendarevent_priority")."-C".$obm_db->f("calendarsegment_customerid")."-L".$obm_db->f("calendarcategory_label")."<br>";}
  return $obm_db;
}
/////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a month of users or/and groups
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    $agenda : agenda params
//    $contacts_array : array containing the id of the contact(s) the event is assigned to 
//    $groups_array   : array containing the id of the group(s) the event is assigned to 
/////////////////////////////////////////////////////////////////////////////

function run_query_month_event_list($agenda,$contacts_array) {
  global $cdg_sql, $set_start_time, $set_stop_time,$set_weekstart_default;
  
  $getdate = $agenda["date"];
  $month = substr($getdate,0,6);
   
  
  $obm_db = new DB_OBM;
  $query = "SELECT calendarevent_id,
                   calendarevent_usercreate,
		   calendarevent_title,
		   calendarevent_priority,
		   calendarevent_privacy,
		   calendarcategory_label,
		   c1.calendarsegment_customerid,
		   c1.calendarsegment_date,
		   c1.calendarsegment_flag
            FROM CalendarEvent,CalendarCategory, CalendarSegment c1,
	    CalendarSegment c2
	    WHERE calendarevent_category_id = calendarcategory_id
	      AND calendarevent_id = c1.calendarsegment_eventid
	      AND c1.calendarsegment_state = 'A'
	      AND c1.calendarsegment_eventid = c2.calendarsegment_eventid
	      AND c2.calendarsegment_flag = 'begin'
	      AND c2.calendarsegment_date LIKE '$month%'  
             ";

  if(is_array($contacts_array) and (count($contacts_array)>0) ) {
    $query .= "AND c1.calendarsegment_customerid IN ('".$contacts_array[0]."'";
    for ($i=1;$i<count($contacts_array);$i++) {
      $query.= ",'".$contacts_array[$i]."'";
    }
    $query.=")";
  }    
        
  $query.=" ORDER BY c1.calendarsegment_date"; 
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}
/////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a month of users or/and groups
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    $agenda : agenda params
//    $contacts_array : array containing the id of the contact(s) the event is assigned to 
//    $groups_array   : array containing the id of the group(s) the event is assigned to 
/////////////////////////////////////////////////////////////////////////////

function run_query_year_event_list($agenda,$contacts_array) {
  global $cdg_sql, $set_start_time, $set_stop_time,$set_weekstart_default;
  
  $getdate = $agenda["date"];
  $year = substr($getdate,0,4);
   
  
  $obm_db = new DB_OBM;
  $query = "SELECT calendarevent_id,
                   calendarevent_usercreate,
		   calendarevent_title,
		   calendarevent_priority,
		   calendarevent_privacy,
		   calendarcategory_label,
		   c1.calendarsegment_customerid,
		   c1.calendarsegment_date,
		   c1.calendarsegment_flag
            FROM CalendarEvent,CalendarCategory, CalendarSegment c1,
	    CalendarSegment c2
	    WHERE calendarevent_category_id = calendarcategory_id
	      AND calendarevent_id = c1.calendarsegment_eventid
	      AND c1.calendarsegment_state = 'A'
	      AND c1.calendarsegment_eventid = c2.calendarsegment_eventid
	      AND c2.calendarsegment_flag = 'begin'
	      AND c2.calendarsegment_date LIKE '$year%'  
             ";



  if(is_array($contacts_array) and (count($contacts_array)>0) ) {
    $query .= "AND c1.calendarsegment_customerid IN ('".$contacts_array[0]."'";
    for ($i=1;$i<count($contacts_array);$i++) {
      $query.= ",'".$contacts_array[$i]."'";
    }
    $query.=")";
  }    
        
  $query.=" ORDER BY c1.calendarsegment_date"; 
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}

/////////////////////////////////////////////////////////////////////////////
// Return all the name and first name of users
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    $contacts_array : array containing the id of the contact(s) the event is assigned to 
/////////////////////////////////////////////////////////////////////////////
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
/////////////////////////////////////////////////////////////////////////////
// Perform the addiction of a event
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    $agenda : Agenda params
//    $contacts_array : List of the users 
/////////////////////////////////////////////////////////////////////////////

function run_query_add_event($agenda,$contacts_array) {
  global $auth;
  $return_info = array();
  $event_repeat_dates = get_event_repetition_dates($agenda);
  var_dump($event_repeat_dates);
  foreach($event_repeat_dates as $dates) {
    $conflicts = run_query_get_conflicts($dates["date_begin"],$dates["date_end"],$contacts_array);
    $return_info = array_merge($return_info, $conflicts);
  }
  if($force == 1 || count($return_info) == 0){    
    $event_id = run_query_insert_event_data($agenda);   
    foreach($event_repeat_dates as $dates) {
      foreach($contacts_array as $user_id) {
	if($user_id == $auth->auth["uid"]) {
	  $conflicts = run_query_get_conflicts_user($dates["date_begin"],$dates["date_end"],$user_id);
	  run_query_delete_occurence($conflicts,$user_id);
  	  run_query_insert_occurence($dates["date_begin"],$dates["date_end"],$user_id,$event_id,'A');	
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
// Insert a occurence of a event
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    $date_begin : 
//    $date_end : 
//    $user_id :
//    $event_id :
//    $state :
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
// Delete occurence of a events for a user 
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    $occur : object which contains events id, date of the occurence
//    $user_id :
/////////////////////////////////////////////////////////////////////////////

function run_query_delete_occurence($occur,$user_id) {
  global $cdg_sql;
  while($occur->next_record()) {
    $obm_db = new DB_OBM;
    $query = "DELETE CalendarSegment WHERE calendarsegment_customerid = $user_id 
            AND calendarsegment_eventid = ".$occur->f("calendarsegment_eventid")." AND 
	    ( calendarsegment.date = '".$occur->f("date_begin")."' OR calendarsegment.date = '".$occur->f("date_end")."')";
    
    display_debug_msg($query, $cdg_sql);
    $obm_db->query($query);
  }
}

/////////////////////////////////////////////////////////////////////////////
// search all conflict for a user in a laps of  time 
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    $date_begin
//    $date_end
//    $user_id :
/////////////////////////////////////////////////////////////////////////////
function run_query_get_conflicts_user($date_begin,$date_end,$user_id) {
  global $cdg_sql;
  $obm_db = new DB_OBM;
  $query = "SELECT cs1.calendarsegment_eventid ,cs1.calendarsegment_date as date_begin
            ,cs2.calendarsegment_date as date_end FROM CalendarSegment cs1, CalendarSegment cs2 
            WHERE cs1.calendarsegment_eventid = cs2.calendarsegment_eventid and cs1.calendarsegment_date < '$date_end'
	    AND cs1.calendarsegment_flag = 'begin' AND cs2.calendarsegment_date > 'date_begin' AND
	    cs2.calendarsegment_flag = 'end' AND cs1.calendarsegment_customerid = $user_id AND cs1.calendarsegment_type = 'user'";
  
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}
/////////////////////////////////////////////////////////////////////////////
// search all conflict for a user in a laps of  time 
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    $date_begin
//    $date_end
//    $user_id :
////////////////////////////////////////////////////////////////////////////

function run_query_get_conflicts($date_begin,$date_end,$contacts_array) {
  global $cdg_sql;
  $obm_db = new DB_OBM;
  $query = "SELECT cs1.calendarsegment_eventid ,cs1.calendarsegment_customerid,cs1.calendarsegment_date as date_begin
            ,cs2.calendarsegment_date as date_end FROM CalendarSegment cs1, CalendarSegment cs2 
            WHERE cs1.calendarsegment_eventid = cs2.calendarsegment_eventid and cs1.calendarsegment_date < '$date_end'
	    AND cs1.calendarsegment_flag = 'begin' AND cs2.calendarsegment_date > 'date_begin' AND
	    cs2.calendarsegment_flag = 'end' AND cs1.calendarsegment_type = 'user'";
  $query .= " AND cs1.calendarsegment_customerid IN ('".$contacts_array[0]."'";
  for ($i=1;$i<count($contacts_array);$i++) {
    $query.= ",'".$contacts_array[$i]."'";
  }
  $query.=")";

  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  while ($obm_db->next_record()) {
    $conflict_array[] = array("event_id" => $obm_db->f("calendarsegment_eventid"), "user_id" => $obm_db->f("calendarsegment_customerid"));
  }
  return $conflict_array;
}
	    
/////////////////////////////////////////////////////////////////////////////
// Insert data of a event  Return the id of this event
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    $agenda : hashed agenda params
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
  if($agenda["privacy"]!=1) $privacy = 0;else $privacy = 1; 
  $repeat_kind = $agenda["kind"];
  $repeat_days =$agenda["repeat_days"]; 
  $repeat_end = $agenda["repeat_end"];

  $obm_db = new DB_OBM;
  $obm_db->lock("CalendarEvent");
  $query = "SELECT MAX(calendarevent_id) as max_id FROM CalendarEvent";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  $obm_db->next_record(); 
  $max_id = $obm_db->f("max_id")+1;
  $query = "INSERT INTO CalendarEvent (calendarevent_id,calendarevent_timeupdate, calendarevent_timecreate, calendarevent_userupdate, 
                         calendarevent_usercreate, calendarevent_title, calendarevent_description,
			 calendarevent_category_id,calendarevent_priority,calendarevent_privacy,
			 calendarevent_repeatkind,calendarevent_repeatdays,calendarevent_endrepeat)
	         VALUES ($max_id,null,'".date("YmdHis")."',null,".$auth->auth["uid"].",'$title',
		         '$description',$category_id,$priority,$privacy,
			 '$repeat_kind','$repeatdays','$endrepeat')";

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
//    $agenda : agenda params
//    $contacts_array : array containing the id of the contact(s) the event is assigned to 
//    $groups_array   : array containing the id of the group(s) the event is assigned to 
/////////////////////////////////////////////////////////////////////////////

function store_events($obm_q,&$curent_event,&$event_data,$p_date_begin,$p_date_end) {
  global  $set_start_time, $set_stop_time,$set_weekstart_default,$set_time_unit;
  $obm_q->next_record();
  $time_unit = 60 / $set_time_unit;
  $s =0;
  $unix_time = mktime(substr($p_date_begin,8,2), substr($p_date_begin,10,2), "00", 
                      substr($p_date_begin,4,2), substr($p_date_begin,6,2), substr($p_date_begin,0,4));
  for($current_time=$p_date_begin;$current_time<$p_date_end;
      $current_time=date("YmdHi",$unix_time)) {
    if(date("G",$unix_time) < $set_stop_time){
      $unix_time = strtotime("+$time_unit minutes",$unix_time);	
    }
    else {
      $tonextday = $set_start_time - $set_stop_time;
      $unix_time = strtotime("+1 day $tonextday hours",$unix_time);
    }
    if(is_array($curent_event[$past_time])) {
      $temp_array = array_filter($curent_event[$past_time],"valid_event");
      if(count($temp_array)>0){
	$curent_event[$current_time] = $temp_array;
      }
    }
    while($obm_q->f("calendarsegment_date") == $current_time) {
      if($obm_q->f("calendarsegment_flag") == "begin") {
	$curent_event[$current_time][$obm_q->f("calendarsegment_customerid")] = $obm_q->f("calendarevent_id");
	$event_data[$obm_q->f("calendarevent_id")] = 
  	array("title"=>$obm_q->f("calendarevent_title"),"type"=>$obm_q->f("calendarcategory_label"),
	$obm_q->f("calendarsegment_flag")=>substr($obm_q->f("calendarsegment_date"),8,2).":".
	substr($obm_q->f("calendarsegment_date"),10,2),"status"=>0);
	$obm_q->next_record(); 
      }elseif($obm_q->f("calendarsegment_flag") == "end"){
	$curent_event[$current_time][$obm_q->f("calendarsegment_customerid")] = -1;
	$obm_q->next_record(); 
      }       
    }
    $past_time = $current_time;
  }    
}
/////////////////////////////////////////////////////////////////////////////
// Return tables of hashed events and of data event.(hashed by day for year
//                 and month view)
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    $agenda : agenda params
//    $contacts_array : array containing the id of the contact(s) the event is assigned to 
//    $groups_array   : array containing the id of the group(s) the event is assigned to 
/////////////////////////////////////////////////////////////////////////////

function store_daily_events($obm_q,&$curent_event,&$event_data,$p_date_begin,$p_date_end) {
  global $set_time_unit;
  $obm_q->next_record();
  $unix_time = strtotime($p_date_begin);
  //                 substr($p_date_begin,4,2), substr($p_date_begin,6,2), substr($p_date_begin,0,4));
  for($current_time=$p_date_begin;$current_time<$p_date_end;$current_time=date("Ymd",$unix_time)) {
    $unix_time = strtotime("+1 day",$unix_time);	
    if(is_array($curent_event[$past_time])) {
      $temp_array = array_filter($curent_event[$past_time],"valid_event");
      if(count($temp_array)>0){
	$curent_event[$current_time] = $temp_array;
      }
    }
    while(substr($obm_q->f("calendarsegment_date"),0,8) == $current_time) {
      if($obm_q->f("calendarsegment_flag") == "begin") {
	$curent_event[$current_time][$obm_q->f("calendarevent_id")] = 1;
	$event_data[$obm_q->f("calendarevent_id")] = 
	array("title"=>$obm_q->f("calendarevent_title"),"type"=>$obm_q->f("calendarcategory_label"),
	$obm_q->f("calendarsegment_flag")=>substr($obm_q->f("calendarsegment_date"),8,2).":".
	substr($obm_q->f("calendarsegment_date"),10,2),"status"=>0);
	$event_data[$obm_q->f("calendarevent_id")]["user_id"][] = $obm_q->f("calendarsegment_customerid");
	$obm_q->next_record(); 
      }elseif($obm_q->f("calendarsegment_flag") == "end"){
	$curent_event[$current_time][$obm_q->f("calendarevent_id")] = -1;
	$obm_q->next_record(); 
      }       
    }
    $past_time = $current_time;
  }    
}

/////////////////////////////////////////////////////////////////////////////
// Return tables of hashed users.
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    $contacts_array : array containing the id of the contact(s) the event is assigned to 
/////////////////////////////////////////////////////////////////////////////

function store_users($user_q) {
  while($user_q->next_record()) {
    $user_tab[$user_q->f("userobm_id")] = $user_q->f("userobm_firstname")." ".$user_q->f("userobm_lastname");
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
  global $l_fill_title, $l_fill_dateend, $l_fill_datebegin, $l_incorect_datebegin, $l_incorect_dateend,$err_msg ;

  
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
    $err_msg = $l_incorect_datebegin;
    return false;
  }  
  
  if (trim($dateend) == "") {
    $err_msg = $l_fill_dateend;
    return false;
  }
  elseif(!checkdate($this_month_e,$this_day_e,$this_year_e)) {    
    $err_msg = $l_incorect_dateend;
    return false;
  }  
  
  if (trim($repeat_end) != "") {
    if(!checkdate($this_month_r,$this_day_r,$this_year_r)) {    
      $err_msg = $l_incorect_repeat;
      return false;
    }  
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
  

  if($repeat_end) {
    $time_end_repeat = strtotime("+1 day",strtotime($repeat_end));
  }
  else {
    $time_end_repeat = strtotime("+1 year +1 day", strtotime(date("Ymd")));
  }
  $time_b=strtotime("+$this_hour_b hours +$this_min_b minutes",strtotime($this_year_b.$this_month_b.$this_day_b));
  $time_e=strtotime("+$this_hour_e hours +$this_min_e minutes",strtotime($this_year_e.$this_month_e.$this_day_e));
  
  if ($repeat_kind=="none") {
    $event_dates[] = array("date_begin" => date("YmdHi",$time_b),"date_end" => date("YmdHi",$time_e));
  }    
  elseif ($p_repeatkind=="daily") {
    for($var_time_b=$time_b, $var_time_e=$time_e;
    $var_time_b <= $time_end_repeat;
    $var_time_b = strtotime("+1 day",$var_time_b), $var_time_e = strtotime("+1 day",$var_time_e)){
      $event_dates[]= array("date_begin" => date("YmdHi",$var_time_b),"date_end" => date("YmdHi",$var_time_e));
    }
  }
  elseif ($repeat_kind=="weekly") {
    $start_week_day = strtotime($set_weekstart_default);
    $first_day = (date("w",$time_b) - date("w",$start_week_day))%7;
    for($var_time_b = strtotime("-$first_day days",$time_b), 
        $var_time_e = strtotime("-$first_day days",$time_e);
    $var_time_b <= $time_end_repeat;    
    $var_time_b = strtotime("+1 week",$var_time_b), $var_time_e = strtotime("+1 week",$var_time_e)) {      
      for ($i=0;($i<7) && (strtotime("+$i days",$var_time_b) <= $time_end_repeat ); $i++) { 
	$day_char=substr($repeat_days,$i,1);
	if (strcmp($day_char,"1")==0) {
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
    $day_beg = (date("w",$time_b) - $start_week_day)%7;
    $day_end = (date("w",$time_e) - $start_week_day)%7;
    $tab_beg = array((($day_beg - 3)%7) => '+3',(($day_beg - 2)%7) => '+2',(($day_beg - 1)%7) => '+1',(($day_beg - 0)%7) => '+0',
                (($day_beg - 1)%7) => '-1',(($day_beg - 2)%7) => '-2',(($day_beg - 3)%7) => '-3');
    $tab_end = array((($day_end - 3)%7) => '+3',(($day_end - 2)%7) => '+2',(($day_end - 1)%7) => '+1',(($day_end - 0)%7) => '+0',
                (($day_end - 1)%7) => '-1',(($day_end - 2)%7) => '-2',(($day_end - 3)%7) => '-3');
    while($var_time_b <= $time_end_repeat){            
      $event_dates[]= array("date_begin" => date("YmdHi",$var_time_b),"date_end" => date("YmdHi",$var_time_e));     
      $var_time_b = strtotime("+1 month",$var_time_b);
      $var_time_e = strtotime("+1 month",$var_time_e);
      $beg_day = date("w",$var_time_b);
      $end_day = date("w",$var_time_e);
      $var_time_b = strtotime("$beg_day days",$var_time_b);
      if(date("d",$var_time_b) < 1) {
	$var_time_b = strtotime("+7 days",$var_time_b);
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
// $format : format of the wished result
// $timestamp : time to format
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
// $Ymd 
// $$day
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

