<script language="php">

///////////////////////////////////////////////////////////////////////////////
// Return all not rejected events in a month for current user in current month
// Parameters: 
///////////////////////////////////////////////////////////////////////////////
function run_query_month_event_list() {
  global $cdg_sql, $set_start_time, $set_stop_time,$set_weekstart_default,$auth;
  $start_month = date("Ym01");
  $start_month_time = strtotime("+$set_start_time hours",strtotime($start_month));
  $end_month_time = strtotime("+1 month",$start_month_time);
  $obm_db = new DB_OBM;
  $db_type = $obm_db->type;
  $calendarsegment_date = sql_date_format($db_type,"calendarsegment_date");
  $calendarsegment_date_l = sql_date_format($db_type,"calendarsegment_date","calendarsegment_date");  
  $query = "SELECT $calendarsegment_date_l,
		   calendarsegment_flag
            FROM CalendarEvent, CalendarSegment
	    WHERE calendarevent_id = calendarsegment_eventid
	      AND calendarsegment_state = 'A'
	      AND ((calendarsegment_flag = 'begin' AND $calendarsegment_date < $end_month_time AND
	            $calendarsegment_date + calendarevent_length > $start_month_time)
	        OR (calendarsegment_flag = 'end' AND $calendarsegment_date > $start_month_time AND	    
		    $calendarsegment_date - calendarevent_length < $end_month_time))
             ";
  $query .= "AND calendarsegment_customerid = '".$auth->auth["uid"]."'";
  $query.=" ORDER BY calendarsegment_date"; 
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  
  return $obm_db;
}

///////////////////////////////////////////////////////////////////////////////
// Return the number of waiting events
// Parameters: 
///////////////////////////////////////////////////////////////////////////////

function run_query_waiting_events() {
  global $cdg_sql,$auth;

  $writable_users = run_query_userobm_writable();
  $obm_db = new DB_OBM;
  
  $query = "SELECT calendarsegment_eventid as num FROM  CalendarSegment WHERE calendarsegment_state = 'W' ";
  if(is_array($writable_users)) {
    $query .= " AND calendarsegment_customerid IN (";
    $num = count($writable_users);
    for($i=0;$i<$num;$i++) {
      $query.= "'".$writable_users[$i]."',";
    }
    $query.="".$auth->auth["uid"].")";
  }
  else {
    $query.="AND calendarsegment_customerid = ".$auth->auth["uid"]."";
  }
  $query .= " GROUP BY calendarsegment_eventid";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db->nf()/1;
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



/////////////////////////////////////////////////////////////////////////////
// Return tables of hashed events and of data event.(hashed by day for year
//                 and month view)
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    - $pbm_q  : DB Object which contain the event of current month
/////////////////////////////////////////////////////////////////////////////
function store_daily_events($obm_q) {
  global $set_cal_interval;
  global $set_start_time;

  $obm_q->next_record();
  $start_month = date("Ym01");
  $p_date_begin = strtotime("+$set_start_time hours",strtotime($start_month));
  $p_date_end = strtotime("+1 month",$p_date_begin);

  for($current_time=$p_date_begin;$current_time<$p_date_end;$current_time = strtotime("+1 day",$current_time)) {
    $current_date = date("Ymd",$current_time);
    $db_date = $obm_q->f("calendarsegment_date");
    $db_id = $obm_q->f("calendarevent_id");
    $db_flag = $obm_q->f("calendarsegment_flag");	    

    while($db_date && $db_date < $current_time) {
      $current_event[$current_date][] = $db_id;
      $obm_q->next_record(); 
      $db_date = $obm_q->f("calendarsegment_date");
      $db_id = $obm_q->f("calendarevent_id");
      $db_flag = $obm_q->f("calendarsegment_flag");	    
    }
    if(is_array($current_event[$past_date])) {
      if(is_array($end_event)){
	if(count($current_event[$current_date] = array_diff($current_event[$past_date],$end_event))==0){
	  $current_event[$current_date]="";
	}
      }
      else {
	$current_event[$current_date]=$current_event[$past_date];
      }
    }
    $end_event ="";
    while($current_date == date("Ymd",$db_date)) {
      if($db_flag == "begin") {
	  $current_event[$current_date][] = $db_id;
	  $obm_q->next_record(); 
      }elseif($db_flag == "end"){
	$end_event[] = $db_id;
	$obm_q->next_record();
      }       
      $db_date = $obm_q->f("calendarsegment_date");
      $db_id = $obm_q->f("calendarevent_id");
      $db_flag = $obm_q->f("calendarsegment_flag");	    
    }
    $past_date = $current_date;
  }   
  return $current_event;
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
// Gets the total of task length for The current user
// Returns : DB object result
///////////////////////////////////////////////////////////////////////////////
function run_query_days_unfilled() {
  global $cdg_sql, $cdg_param, $auth;
  global $c_day_fraction,$working_days;


  // First and last days of month
  $query = "
    select
      sum(timetask_length) as total_length
    from TimeTask
    where timetask_date <= '".date("Ymd")."'
      and timetask_user_id = '".$auth->auth["uid"]."' 
    ";
  
  display_debug_msg($query, $cdg_sql, "run_query_task_one_month");
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $obm_db->next_record();
  $filled_days = $obm_db->f("total_length");
  $filled_days = $filled_days/$c_day_fraction;
  $workingdays = 0;
  $current_day = strtotime(date("Y0101"));
  $today = time();
  while($current_day <= $today) {
    $day = date("w",$current_day);
    if( $working_days[$day] == 1) {
      $workingdays ++;
    }
    $current_day = strtotime("+1 day",$current_day);
  }
  return $workingdays - $filled_days;
}
</script>
