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
  $query = "select calendarevent_id,
                   calendarevent_usercreate,
		   calendarevent_title,
		   calendarevent_priority,
		   calendarevent_privacy,
		   calendarevent_datebegin,
		   calendarevent_dateend,
		   eventcategory_label,
		   eventuser_user_id,
		   calendarsegment_date,
		   calendarsegment_flag
            from CalendarEvent,EventCategory,EventUser, CalendarSegment
	    where calendarevent_category_id=eventcategory_id
	      and calendarevent_id = calendarsegment_id
              and eventuser_state <> 'R'
	      and eventuser_event_id=calendarevent_id	      
	      and calendarevent_datebegin < $end_day  
	      and calendarevent_dateend > $start_day
             ";



  if(is_array($contacts_array) and (count($contacts_array)>0) ) {
    $query .= "and eventuser_user_id in ('".$contacts_array[0]."'";
    for ($i=1;$i<count($contacts_array);$i++) {
      $query.= ",'".$contacts_array[$i]."'";
    }
    $query.=")";
  }    
        
  $query.=" order by calendarsegment_date"; 
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
  $query = "select calendarevent_id,
                   calendarevent_usercreate,
		   calendarevent_title,
		   calendarevent_priority,
		   calendarevent_privacy,
		   calendarevent_datebegin,
		   calendarevent_dateend,
		   eventcategory_label,
		   eventuser_user_id,
		   calendarsegment_date,
		   calendarsegment_flag
            from CalendarEvent,EventCategory,EventUser, CalendarSegment
	    where calendarevent_category_id=eventcategory_id
	      and calendarevent_id = calendarsegment_id
              and eventuser_state <> 'R'
	      and eventuser_event_id=calendarevent_id	      
	      and calendarevent_datebegin < $end_week_time  
	      and calendarevent_dateend > $start_week_time
             ";



  if(is_array($contacts_array) and (count($contacts_array)>0) ) {
    $query .= "and eventuser_user_id in ('".$contacts_array[0]."'";
    for ($i=1;$i<count($contacts_array);$i++) {
      $query.= ",'".$contacts_array[$i]."'";
    }
    $query.=")";
  }    
        
  $query.=" order by calendarsegment_date"; 
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
  $query = "select userobm_lastname,userobm_firstname,userobm_id
            from UserObm
	    where userobm_id in (".$contacts_array[0];
  for ($i=1;$i<count($contacts_array);$i++) {
    $query.= ",'".$contacts_array[$i]."'";
  }
  $query.=")";        
  $query.=" order by userobm_id"; 
  display_debug_msg($query, $cdg_sql);
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
// Return tables of hashed events and of data event.
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
	$curent_event[$current_time][$obm_q->f("eventuser_user_id")] = $obm_q->f("calendarevent_id");
	$event_data[$obm_q->f("calendarevent_id")] = 
  	      array("title"=>$obm_q->f("calendarevent_title"),"type"=>$obm_q->f("eventcategory_label"),
	      "begin"=>date("H:i",$obm_q->f("calendarevent_datebegin")),"end"=>date("H:i",$obm_q->f("calendarevent_dateend")), 
	      "status"=>0);
	$obm_q->next_record(); 
      }elseif($obm_q->f("calendarsegment_flag") == "end"){
	$curent_event[$current_time][$obm_q->f("eventuser_user_id")] = -1;
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
