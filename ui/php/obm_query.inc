<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : obm_query.inc                                                //
//     - Desc : OBM entry page query File                                    //
// 2004-01-20 Aliacom - Pierre Baudracco                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

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
  $query = "SELECT
      count(userobm_id) as num
    FROM CalendarEvent LEFT JOIN CalendarCategory ON calendarevent_category_id = calendarcategory_id
      LEFT JOIN CalendarUser ON calendarevent_id  = calendaruser_event_id
      LEFT JOIN UserObm ON calendaruser_user_id = userobm_id
    WHERE calendaruser_state = 'W'";

  if (is_array($writable_user)) {
    $query .= " AND calendaruser_user_id IN (";
    $num = count($writable_user);
    foreach($writable_user as $user) {
      $query.= "'$user',";
    }
    $query.="'".$auth->auth["uid"]."')";
  } else {
    $query.="AND calendaruser_user_id = '".$auth->auth["uid"]."'";
  }
  $query .= " GROUP BY userobm_id ORDER BY userobm_id"; 
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  $obm_db->next_record();
  return $obm_db->f("num");
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
  $ret_ts = strtotime('+12 hours', $ret_ts);

  return $ret_ts;
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
    store_event($date, &$event, &$day_events,$user_id,$all_day,$end_time);
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
    store_event($temp_date, &$event, &$day_events,$user_id,$all_day,$end_date);
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
	  store_event($current_date, &$event, &$day_events,$user_id,$all_day,$end_date);
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
      store_event($current_date, &$event, &$day_events,$user_id,$all_day,$end_date);
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
    store_event($ts_date, &$event, &$day_events,$user_id,$all_day,$end_date);
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
    store_event($ts_date, &$event, &$day_events,$user_id,$all_day,$end_date);
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
    if(date("Ymd",$this->day) == date("Ymd",$date)) {
      return true;
    }
    return false;
  }
  
  function store_event(&$event,$begin_date,$uid,$day_event) {
    if($day_event == 1) {
      $this->add_day_event(&$event,$uid);
    } else {
      $this->add_event(&$event,$begin_date,$uid);
    }
  }
  
  function add_event(&$event,$begin_date,$uid) {
    $date = date("YmdHis",$begin_date);
    if(!isset($this->events[$date.".".$event->id])) {
      $this->events[$date.".".$event->id] = array("event"=>&$event,"date"=>$begin_date,"users"=>array($uid));
      ksort($this->events);
    }
    else {
      $this->events[$date.".".$event->id]["users"][] = $uid;
    }
  }
  
  function add_day_event(&$event,$uid) {
    $this->day_events[$uid][] = &$event;
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
  
  function get_events_before($end, $uid,$start="") {
    foreach($this->events as $event) {
      
      $ev_start = $event["date"];
      $ev_end = $event["event"]->duration + $ev_start;  
          
      if($ev_start > $end) {
	break;
      }
      
      if(!in_array($uid,$event["users"])) {
	continue;
      }      
      if($ev_start < $end) {
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


///////////////////////////////////////////////////////////////////////////////
// Deal : To complete
///////////////////////////////////////////////////////////////////////////////
function run_query_deals() {
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
