<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : obm_query.inc                                                //
//     - Desc : OBM entry page query File                                    //
// 2004-01-20 Aliacom - Pierre Baudracco                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

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
  return $obm_db->nf();
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



///////////////////////////////////////////////////////////////////////////////////////
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
// Gets the total of task length for The current user
// Returns : DB object result
///////////////////////////////////////////////////////////////////////////////
function run_query_days_unfilled() {
  global $cdg_sql, $cdg_param, $auth;
  global $c_day_fraction, $c_working_days;

  $uid = $auth->auth["uid"];

  $begindate = get_user_entrydate($uid);
  $unix_ts = $begindate["unix_timestamp"];
  $iso = $begindate["iso"];
  $jan_first = mktime (0, 0, 0, 1, 1, date("Y"));
  if ($unix_ts > $jan_first) {
    $date = $iso;
  } else {
    $date = date("Y0101");
  }

  // First and last days of month
  $query = "
    select
      sum(timetask_length) as total_length
    from TimeTask
    where timetask_date <= '".date("Ymd")."'
    and timetask_date >= '$date'
      and timetask_user_id = '$uid'
    ";
  
  display_debug_msg($query, $cdg_sql, "run_query_days_unfilled");
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $obm_db->next_record();
  $filled_days = $obm_db->f("total_length");
  $filled_days = $filled_days/$c_day_fraction;
  $workingdays = 0;
  $current_day = strtotime($date);
  $today = time();
  while($current_day <= $today) {
    $day = date("w",$current_day);
    if ($c_working_days[$day] == 1) {
      $workingdays ++;
    }
    $current_day = strtotime("+1 day",$current_day);
  }
  return $workingdays - $filled_days;
}



</script>
