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
            FROM CalendarEvent,EventCategory,EventUser, CalendarSegment
	    WHERE calendarevent_category_id=eventcategory_id
	      AND calendarevent_id = calendarsegment_id
              AND eventuser_state <> 'R'
	      AND eventuser_event_id=calendarevent_id	      
	      AND calendarevent_datebegin < $end_day  
	      AND calendarevent_dateend > $start_day
             ";



  if(is_array($contacts_array) and (count($contacts_array)>0) ) {
    $query .= "AND eventuser_user_id IN ('".$contacts_array[0]."'";
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
  $query = "SELECT calendarevent_id,
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
            FROM CalendarEvent,EventCategory,EventUser, CalendarSegment
	    WHERE calendarevent_category_id=eventcategory_id
	      AND calendarevent_id = calendarsegment_id
              AND eventuser_state <> 'R'
	      AND eventuser_event_id=calendarevent_id	      
	      AND calendarevent_datebegin < $end_week_time  
	      AND calendarevent_dateend > $start_week_time
             ";



  if(is_array($contacts_array) and (count($contacts_array)>0) ) {
    $query .= "AND eventuser_user_id IN ('".$contacts_array[0]."'";
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
		   calendarevent_datebegin,
		   calendarevent_dateend,
		   eventcategory_label,
		   eventuser_user_id,
		   calendarsegment_date,
		   calendarsegment_flag
            FROM CalendarEvent,EventCategory,EventUser, CalendarSegment
	    WHERE calendarevent_category_id=eventcategory_id
	      AND calendarevent_id = calendarsegment_id
              AND eventuser_state <> 'R'
	      AND eventuser_event_id=calendarevent_id	      
	      AND calendarevent_datebegin LIKE '$month%'  
             ";



  if(is_array($contacts_array) and (count($contacts_array)>0) ) {
    $query .= "AND eventuser_user_id IN ('".$contacts_array[0]."'";
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
		   calendarevent_datebegin,
		   calendarevent_dateend,
		   eventcategory_label,
		   eventuser_user_id,
		   calendarsegment_date,
		   calendarsegment_flag
            FROM CalendarEvent,EventCategory,EventUser, CalendarSegment
	    WHERE calendarevent_category_id=eventcategory_id
	      AND calendarevent_id = calendarsegment_id
              AND eventuser_state <> 'R'
	      AND eventuser_event_id=calendarevent_id	      
	      AND calendarevent_datebegin LIKE '$year%'  
             ";



  if(is_array($contacts_array) and (count($contacts_array)>0) ) {
    $query .= "AND eventuser_user_id IN ('".$contacts_array[0]."'";
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
// Insert a event
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    $contacts_array : array containing the id of the contact(s) the event is assigned to 
/////////////////////////////////////////////////////////////////////////////
function run_query_insert_event($agenda) {
  global $cdg_sql,$auth;

  $obm_db = new DB_OBM;
  $title = $agenda["title"];
  $category_id = $agenda["category"];
  $priority = $agenda["priority"];
  $description = $agenda["description"];
  $datebegin = $agenda["date_begin"];
  $dateend = $agenda["date_end"];
  $privacy = $agenda["privacy"]; 
  $occupied_day = $agenda["occupied"];
  $repeat_kind = $agenda["kind"];
  $repeat_interval = $agenda["interval"];
  $repeat_days =$agenda["repeat_days"]; 
  $repeat_end = $agenda["repeat_end"];
  $force = $agenda["force"];
  
  if ($interval =='') {
    $interval=1;
  }

  if($repeat_end == '') {
    $repeat_end = substr($agenda["date_end"],0,8);
  }
  
  switch($repeat_kind) {
    case 'daily'   : $inter = '+1 day'; break;
    case 'weekly'  : $inter = '+1 week'; break;
    case 'monthly' : $inter = '+1 month'; break; 
    case 'yearly'  : $inter = '+1 year'; break; 
    default        : $inter = '+$interval days'; break;  
    }
 
  if ($privacy != 1) {
    $privacy=0;
  }

  
  for($i=substr($agenda["date_end"],0,8); $i< $repeat_end; $i = date("Ymd",strtotime("$inter",$i))) {
/////////////////////////////////////// 
// Verification de l'absence de conflit
///////////////////////////////////////
    $query = "SELECT calendarevent_id,
              FROM CalendarEvent,EventUser
	      WHERE  eventuser_state <> 'R' AND eventuser_event_id=calendarevent_id AND calendarevent_datebegin < $date_end  
	      AND calendarevent_dateend > $date_begin";

	      
    if(is_array($contacts_array) and (count($contacts_array)>0) ) {
      $query .= "AND eventuser_user_id in ('".$contacts_array[0]."'";
      for ($i=1;$i<count($contacts_array);$i++) {
      	$query.= ",'".$contacts_array[$i]."'";
      }
      $query.=")";
    }      
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);
//////////////////////////////////////////////////
// Si pas de conflit ou que l'on force l'insertion
//////////////////////////////////////////////////
    if($obm_q->nf() == 0 || $force == 1) {
      if($force == 1 && $obm_q->nf()>0) {
/////////////////////////////////////////////////////////////////////////////	
// Si Conflit et que l'insertion est forcée on supprime l'ancien rendez vous.
/////////////////////////////////////////////////////////////////////////////
	run_query_delete_event($p_user_array, $obm_q->f("calendarevent_id"));
      }
/////////////////////////////////////////      
// Insertion des données de l'évenement.
/////////////////////////////////////////
      $query = "INSERT INTO CalendarEvent (calendarevent_timeupdate, calendarevent_timecreate, calendarevent_userupdate, 
                                         calendarevent_usercreate, calendarevent_origin_id, calendarevent_title, calendarevent_description,
					 calendarevent_category_id,calendarevent_priority,calendarevent_privacy,calendarevent_datebegin,
					 calendarevent_dateend,calendarevent_occupied_day,calendarevent_repeatkind,
					 calendarevent_repeat_interval,calendarevent_repeatdays,calendarevent_endrepeat)
			         VALUES (null,'".date("YmdHis")."',null,".$auth->auth["uid"].",$p_origin,'$title',
				         '$description',$category_id,$priority,$privacy,'$datebegin','$dateend',
					 $repeat_interval,'$repeatdays','$endrepeat')";

      display_debug_msg($query, $cdg_sql);
      $obm_q->query($query);
//////////////////////////////////////////////////////////////      
// On rechecher l'id de l'evenement que l'ont vient d'inserer
//////////////////////////////////////////////////////////////
      $query = "SELECT calendarevent_id FROM CalendarEvent 
                                      WHERE calendarevent_title='$p_title' AND calendarevent_usercreate='".$auth->auth["uid"]."'
	  			      AND calendarevent_datebegin='$p_datebegin' AND calendarevent_dateend='$p_dateend'";
      display_debug_msg($query, $cdg_sql);
      $obm_db->query($query);
      $obm_db->next_record();
      $event_id = $obm_db->f("calendarevent_id");
////////////////////////////////////////////////////////////////////      
// Calcul des dates de début et de fin dans Calendar Segment.
// On insere les données de Calendar Segment, permettant l'affichage
////////////////////////////////////////////////////////////////////
      $query = "INSERT INTO CalendarSegment (calendarsegment_id,calendarsegment_date,calendarsegment_flag)
                            VALUES ($event_id, '$date_b', 'begin')";
      $obm_q->query($query);   
      $query = "INSERT INTO CalendarSegment (calendarsegment_id,calendarsegment_date,calendarsegment_flag)
                          VALUES ($event_id, '$date_e', 'end')";
      display_debug_msg($query, $cdg_sql);
      $obm_q->query($query); 		     
////////////////////////////////////////////////////////////      
// On insère les liens entre les utilisateurs et l'evenement
////////////////////////////////////////////////////////////
      foreach($p_user_array as $user_id) {
	if($user_id == $auth->auth["uid"]) {$state = 'A';} else {	$state = 'W'; }		    
	$query = "INSERT INTO CalendarSegment (eventuser_event_id, eventuser_user_id, eventuser_state)
                            VALUES($event_id,$user_id,$state)";
        display_debug_msg($query, $cdg_sql);
        $obm_q->query($query);		  
      }	
      if($p_origin=='null')
        $p_origin=$event_id;
    }
  }
  display_debug_msg($query, $cdg_sql);
  $obm_q =new DB_OBM;
  $obm_q->query($query); 
}



/////////////////////////////////////////////////////////////////////////
// Get all event categories
/////////////////////////////////////////////////////////////////////////
function run_query_get_eventcategories() {
  global $cdg_sql;

  $query = "SELECT * FROM EventCategory";
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
// Return tables of hashed events and of data event.(hashed by day)
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
	array("title"=>$obm_q->f("calendarevent_title"),"type"=>$obm_q->f("eventcategory_label"),
	"begin"=>date("H:i",$obm_q->f("calendarevent_datebegin")),"end"=>date("H:i",$obm_q->f("calendarevent_dateend")), 
	"status"=>0);
	$event_data[$obm_q->f("calendarevent_id")]["user_id"][] = $obm_q->f("eventuser_user_id");
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
  global $l_fill_title, $l_fill_dateend, $l_fill_datebegin, $l_incorect_datebegin, $l_incorect_dateend ;

  
  $title = $agenda["title"];
  $datebegin = substr($agenda["date_begin"],0,8);
  $timebegin = substr($agenda["date_begin"],8,4);
  $dateend = substr($agenda["date_end"],0,8);
  $timeend = substr($agenda["date_end"],8,6);
  $repeat_end = $agenda["repeat_end"];
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})",$datebegin , $day_array2);
  $this_day_b = $day_array2[3]; 
  $this_month_b = $day_array2[2];
  $this_year_b = $day_array2[1];
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})",$dateend , $day_array2);
  $this_day_e = $day_array2[3]; 
  $this_month_e = $day_array2[2];
  $this_year_e = $day_array2[1];
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})",$repeat_end , $day_array2);
  $this_day_r = $day_array2[3]; 
  $this_month_r = $day_array2[2];
  $this_year_r = $day_array2[1];


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

