<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : contact_query.inc                                           //
//     - Desc  : contact query File                                          //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Contact search query execution                                            //
// Parametes:
//   - $time[]    :    time search criteria
//     keys used       interval, date
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
//   - $p_new_order2 : infos for order clause
///////////////////////////////////////////////////////////////////////////////
function run_query_search($time, $p_new_order, $p_order_dir,$p_new_order2) {
  global $auth, $l_date_format, $l_jour_format, $fraction_jour, $cdg_sql;

  $date = $time["date"];
  //  echo "date $date<br>";
  if ($time['interval'] = "week") {
      $array_date = get_week_point($date);
  }
  else
    die ("interval != 'week' not fonctionnal"); 

  // 0 = task_status renamed as test_status : if =0, check box visible
  //    if <> 0 (i.e. validated), checkbox not visible
  $query="select Task.*,task_id, Task.task_id as Id, DATE_FORMAT(task_date,'$l_jour_format') as task_date, DATE_FORMAT(task_timeupdate,'$l_date_format') as timeupdate, DATE_FORMAT(task_timecreate,'$l_date_format') as timecreate, concat(task_length,concat('/',$fraction_jour)) as task_length, TaskType.*, deal_label as task_deal_label, company_name as task_company_name, 0=task_status as test_status from Task, TaskType, Deal, Company where task_tasktype_id = tasktype_id and task_user_id=".$time["contact_id"]." and task_deal_id = deal_id and deal_company_id = company_id ";

 
  // If a time interval has been specified, get it 
  if (is_array($array_date)) {
    $query .= "and task_date  >='" . $array_date[0] . "' and " .
              "task_date  <'" . $array_date[1] . "'";
  }
  // If a contact indication has been specified, get it 
  if ($lname != "") {
    $query .= " contact_lastname like '" . $lname . "%' and";
  }
  // If a company indication has been specified, get it 
  if ($company != "") {
    $query .= " company_name like '" . $company . "%' and";
  }
  // If a company was sent as parameter, get it
  if ($company_id != "") {
    $query .= " company_id =" . $company_id . " and";
  }

  // order
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "Task.task_date";
  $query .= " order by $order $p_order_dir";
  if (strcmp($p_new_order2,"") != 0) {
    $query .= ", ".$p_new_order2;
  }
   
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  return $obm_db;
}



///////////////////////////////////////////////////////////////////////////////
// Task grouped by day                                                       //
// Parametes:
//   - $time[]    :    time search criteria
//     keys used       week,contact_id, date, date_end
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
//   - $p_new_order2 : infos for order clause
///////////////////////////////////////////////////////////////////////////////
function run_query_by_day($time, $p_new_order, $p_order_dir,$p_new_order2) {
  global $auth, $l_date_format, $l_jour_format, $fraction_jour, $cdg_sql;

  $week = $time["week"];
  $contact_id = $time["contact_id"];
  $week_start = $time["date"];
  $week_end = $time["date_end"];

  // task_valid 0 if checkbox not to be shown
  $query="select concat(sum(task_length),concat('/',$fraction_jour)) as task_totallength,DATE_FORMAT(task_date,'$l_jour_format') as task_date,(1=sum(task_length)/$fraction_jour and task_status<>2) as task_valid,task_status from Task where task_user_id='$contact_id' and task_date>='$week_start' and task_date<='$week_end' group by task_date";

  // order
  $query .= " order by Task.task_date";
  
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  return $obm_db;
}

///////////////////////////////////////////////////////////////////////////////
// Task grouped by day                                                       //
// Parametes:
//   - $time[]       : time search criteria
//     keys used       contac_id, date, date_end
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
//   - $p_new_order2 : infos for order clause
///////////////////////////////////////////////////////////////////////////////
function run_query_by_deal($time, $p_new_order, $p_order_dir,$p_new_order2) {
  global $auth, $l_date_format, $l_jour_format, $fraction_jour, $cdg_sql;

  $week = $time["week"];
  $contact_id = $time["contact_id"];
  $week_start = $time["date"];
  $week_end = $time["date_end"];

  $query="select concat(sum(task_length),concat('/',$fraction_jour)) as task_totallength,DATE_FORMAT(task_date,'$l_jour_format') as task_date, deal_label as task_deal_label, company_name as task_company_name from Task, Deal, TaskType, Company where tasktype_id=task_tasktype_id and task_deal_id = deal_id and task_user_id='$contact_id'  and task_date>='$week_start' and task_date<='$week_end' and deal_company_id = company_id group by task_deal_id ";

  // order
  $query .= " order by Task.task_date";
  
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);

  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Task to validate                                                         //
// Parametes:
//   - $time[]    : contact search criteria 
//     keys used       contac_id, date, date_end
///////////////////////////////////////////////////////////////////////////////
function run_query_admin($time) {

   global $l_jour_format,$fraction_jour, $cdg_sql;

   $contact_id = $time["contact_id"];
   $week_start = $time["date"];
   $week_end = $time["date_end"];
 
   $query = "select concat(sum(task_length),concat('/',$fraction_jour)) as task_totallength,DATE_FORMAT(task_date,'$l_jour_format') as task_date,task_status=2 as task_status from Task where task_user_id='$contact_id' and task_date>='$week_start' and task_date<='$week_end' and task_status!=0 group by task_date";

   //order
   $query .= " order by Task.task_date";

   $obm_db = new DB_OBM;
   $obm_db->query($query);
   return $obm_db;
}
///////////////////////////////////////////////////////////////////////////////
// return array with date where task have been validate
///////////////////////////////////////////////////////////////////////////////
function run_query_valid_search($time) {
global $cdg_sql;

  $uid = $time["contact_id"];
  $i=0; //for loop
  
  $query = "select task_date from Task where task_status != 0 and task_user_id='$uid' group by task_date";
  
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  while($obm_db->next_record()) {
    $valid_array[$i++] = substr($obm_db->f("task_date"),0,8);
  }
   
  return $valid_array; 
}  



///////////////////////////////////////////////////////////////////////////////
// get all for task woth $id id                                         //
// Parametes:
//   - $id : id for task
///////////////////////////////////////////////////////////////////////////////
function run_query_get_task($id) {
  global $cdg_sql;

  $query = "select * from Task where task_id = '$id'";
  
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $obm_db->next_record();
  return $obm_db;
}

///////////////////////////////////////////////////////////////////////////////
// Task Insertion query execution                                         //
// Parameters:
//   - $task[] : Entry's values
//   -$date : date of creation
//     keys used  : tasktype, project, time, label, date
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($task) {
  global $auth;
  global $cdg_sql;

  $valid_date = run_query_valid_search($task);
  $date = $task["sel_date"];
  $ttype = $task["tasktype"];
  $project = $task["project"];
  $time = $task["time"];
  $label = $task["label"];
  $user = $task["contact_id"];
    
  if ( $ttype != 0 ) //for navigators wich dosen't support HTML 4 => <option disabled>
  { 
     if(is_array($date)){
     foreach($date as $key => $value) {
       $query = "insert into Task (task_timeupdate,
       task_timecreate,
       task_userupdate,
       task_usercreate,
       task_user_id,
       task_date,
       task_deal_id,
       task_length,
       task_tasktype_id,
       task_label,
       task_status)
       values(null,
       '" . date("Y-m-d H:i:s") . "',
       null,
       '" . $auth->auth["uid"] . "',
       $user,
       '$key',
       '$project',
       '$time',
       '$ttype',
       '$label',
       0)";

       display_debug_msg($query, $cdg_sql);
       $obm_q = new DB_OBM;
       $obm_q->query($query);  
     }
    }
    else {
       $query = "insert into Task (task_timeupdate,
       task_timecreate,
       task_userupdate,
       task_usercreate,
       task_user_id,
       task_date,
       task_deal_id,
       task_length,
       task_tasktype_id,
       task_label,
       task_status)
       values(null,
       '" . date("Y-m-d H:i:s") . "',
       null,
       '" . $auth->auth["uid"] . "',
       $user,
       '$date',
       '$project',
       '$time',
       '$ttype',
       '$label',
       0)";

       display_debug_msg($query, $cdg_sql);
       $obm_q = new DB_OBM;
       $obm_q->query($query);  
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Task modification query execution                                         //
// Parameters:
//   - $task[] : Entry's values
//   -$date : date of creation
//     keys used  : tasktype, project, time, label
///////////////////////////////////////////////////////////////////////////////
function run_query_modify($task,$date) {
  global $cdg_sql;

  $id = $task["task_id"];
  $ttype = $task["tasktype"];
  $project = $task["project"];
  $time = $task["time"];
  $label = $task["label"];
  
  display_debug_msg($query, $cdg_sql);
  $query = "update Task set task_date='$date',task_tasktype_id='$ttype',task_length='$time',task_label='$label',task_deal_id='$project' where task_id='$id'";
  $obm_db = new DB_OBM;
  $obm_db->query($query);
}

///////////////////////////////////////////////////////////////////////////////
// Delete task pass in $params                                              //
// Parameters:
//     -$parms = correponding to page parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($parms) {

  global $cdg_sql;

  foreach($parms as $key => $val) {
    $id_task = strstr($key,"_"); 
    if( $id_task != "" ) {//if we have a id task
      $id_task = substr($id_task, 1);  
    
      if($val == "on"){ 
        $query = "delete from Task where task_id = $id_task";
        $delete_query = new DB_OBM;
        $delete_query->query($query);
      }    
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Task Validation                                                          //
// Parameters:
//     - $time[]    : time search criteria  
//     -$parms      : correponding to page parameters
//     -$who        : user who make the validation : 1=admin or other=user
///////////////////////////////////////////////////////////////////////////////
function run_query_validate($parms,$time,$who) {

  global $l_jour_format;
  global $uid;
  global $cdg_sql;

  if($who == 1) {
    $status = 2;
    ///////////////////////////////////////////
    $zero = "update Task SET task_status=1 where task_status=2 and task_date>='".$time["date"]."' and task_date<='".$time["date_end"]."' and task_user_id='". $time["contact_id"] ."'";
    $zero_query = new DB_OBM;                  
    $zero_query->query($zero);
    //////////////////////////////////////////
  }
  else { 
    $status = 1;
    ///////////////////////////////////////////
    $zero = "update Task SET task_status=0 where task_status=1 and task_date>='".$time["date"]."' and task_date<='".$time["date_end"]."' and task_user_id='". $time["contact_id"] ."'";
    $zero_query = new DB_OBM;                  
    $zero_query->query($zero);
    //////////////////////////////////////////
  }
   
  foreach($parms as $key => $val) {
    $d_task = strstr($key,"_"); 
    if( $d_task != "" ) {//if we have a id task
      $d_task = substr($d_task, 1);  
      if($val == "on"){ 
        $d_task = str_replace("_"," ",$d_task); 
        $query = "UPDATE Task SET task_status=$status where DATE_FORMAT(task_date,'$l_jour_format') = '$d_task' and task_user_id='". $time["contact_id"]."'";
        $delete_query = new DB_OBM;
        display_debug_msg($query, $cdg_sql);
        $delete_query->query($query);
      }	   
    }
  }
}

///////////////////////////////////////////////////////////////////////////////
// Timemanager: TaskType select query execution                              //
// Returns : DB object result with all type of task
///////////////////////////////////////////////////////////////////////////////
function run_query_tasktype() {
  global $cdg_sql;

  $query = "select * from TaskType order by tasktype_internal";

  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Timemanager: Project select query execution                              //
//    only signed but not still realised...
// Returns : DB object result with project satisfying criteria
///////////////////////////////////////////////////////////////////////////////
function run_query_project() {
  global $cdg_sql;

  $query = "select deal_id, deal_label, company_name from Deal, Company where company_id = deal_company_id and deal_status_id in ( 1 ,7 ,8 ) order by company_name";

  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


//////////////////////////////////////////////////////////////////////////
// Get all the contacts of the companyusing obm
//////////////////////////////////////////////////////////////////////////
function run_query_get_mycontacts() {
  global $cdg_sql;
 
  $obm_db=new DB_OBM;
  $query="select contact_id,contact_lastname,contact_firstname from Contact,Company where contact_company_id=company_id and company_state=1 order by contact_lastname";
  display_debug_msg($query, $cdg_sql);

  $obm_db->query($query);
  return $obm_db;
}


//////////////////////////////////////////////////////////////////////////
// return contact_id 
// Param : $user_id
//////////////////////////////////////////////////////////////////////////
function run_query_get_contact_id($user_id) {
  global $cdg_sql;

  $obm_db = new DB_OBM;
  $query="select userobm_contact_id from UserObm where $user_id  = userobm_id";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  $obm_db->next_record(); 
  return $obm_db->f("userobm_contact_id");
}

//////////////////////////////////////////////////////////////////////////
// return contact_id 
// Param : $contact_id
//////////////////////////////////////////////////////////////////////////
function run_query_get_names($contact) { 
  global $cdg_sql;

  $obm_db = new DB_OBM;
  $query = "select contact_lastname,contact_firstname from Contact where $contact=contact_id";
  display_debug_msg($query, $cdg_sql);

  $obm_db->query($query);
  return $obm_db;
}


//////////////////////////////////////////////////////////////////////////
// Get n day values for a given week, for displaying in the creating task
//  form
// Param: $date : a date starting on the 1st day of the week
//        $num : the number of days choosen (5)
//////////////////////////////////////////////////////////////////////////
function get_this_week($date, $num_days) {
    global $cdg_sql;
  
    for ($i=0 ; $i <$num_days; $i++) {
       // Calc the 5 dates
       //don't change it the first time
       if ($i != 0)
         $date += 86400;
       $date_hum = date("Ymd",$date);
       // spliting it
       $p_year=substr($date_hum,0,4);
       $p_month=substr($date_hum,4,2);
       $p_day=substr($date_hum,6,2);
       // Then we display it
       // display the name for the day, 1 is monday
       if ($i ==6)
         $dis_date = weekday_short_name(0);
       else  
         $dis_date = weekday_short_name($i+1);
       
	   // We add day of month and month
       $dis_date .= " $p_day " . month_short_name($p_month);
       // We create an array 
       $array_date[$i][0]=$dis_date;
       $array_date[$i][1]="$p_year$p_month$p_day";

    }

    return $array_date;
}

//////////////////////////////////////////////////////////////////////////
// Get the starting and ending date of a week
//  params :
//////////////////////////////////////////////////////////////////////////
function get_week_point($sel_date) {
  global $auth, $debug;
  global $cdg_sql;

  // $l_date_format, $l_jour_format, $fraction_jour;

    // week planning
	if (is_null($sel_date))
		$p_date=date("YmdHis");
    else
		$p_date=$sel_date;

    $p_contact_array=array(run_query_contactid_user($auth->auth["uid"]));
    $p_year=substr($p_date,0,4);
    $p_month=substr($p_date,4,2);
    $p_day=substr($p_date,6,2);
     // day of week (0=sunday)
    $dow=date("w",mktime(0,0,0,$p_month,$p_day,$p_year));       
    // Weekstart :
    if ($dow == 0) {
      $weekstart=mktime(0,0,0,$p_month,$p_day - 6,$p_year);
    } elseif ( $dow == 1 ) {
      $weekstart=mktime(0,0,0,$p_month,$p_day,$p_year); 
    } else {
      $weekstart=mktime(0,0,0,$p_month,$p_day - ($dow-1),$p_year);
    }
    $weekend=date("YmdHis",$weekstart + 7 * 86400);
    $weekstart=date("YmdHis",$weekstart);  
    
	// Date Couple in an array
    if ($debug > 0)
		echo "dates debut $weekstart fin $weekend<br>";

	return array($weekstart, $weekend);
	
}

//////////////////////////////////////////////////////////////////////////
// Show a date in human format
//  params : $p_date : date in yyyymmdd format
//////////////////////////////////////////////////////////////////////////
function dis_human_date($p_date) {
  global $cdg_sql;
    
    $p_year=substr($p_date,0,4);
    $p_month=substr($p_date,4,2);
    $p_day=substr($p_date,6,2);

   
    $mois = month_name($p_month);

    return "$p_day $mois $p_year";

}

//////////////////////////////////////////////////////////////////////////
// Show a date in mysql format (for query)
//  params : $p_date : date in yyyymmdd format
//////////////////////////////////////////////////////////////////////////
function dis_mysql_date($p_date) {
  global $cdg_sql;
    
    $p_year=substr($p_date,0,4);
    $p_month=substr($p_date,4,2);
    $p_day=substr($p_date,6,2);

    return "'$p_year-$mois-$p_day'";

}


//////////////////////////////////////////////////////////////////////////
// return the start of the week
//  params : $stamp : current time in seconds
//////////////////////////////////////////////////////////////////////////
function start_of_week($stamp) {
  global $cdg_sql;

  $current_day = date("w",$stamp); 
  if ( !$current_day ) //sunday
    $stamp -=  6*86400;
  else if ( $current_day > 1) //not monday
    $stamp -= ($current_day-1)*86400;

  return($stamp); 
}


/////////////////////////////////////////////////////////////////////////////
// Return the first day of the week in seconds                            //
//Parameters :
// - $time : array with $time["date"] == current date 
///////////////////////////////////////////////////////////////////////////
function first_day_week(&$time) {
  global $cdg_sql;

  if ( isset($time["date"]) ) { 

    $year=substr($time["date"],0,4);
    $month=substr($time["date"],4,2);
    $day=substr($time["date"],6,2);
    $d_start_week=mktime(0,0,0,$month,$day,$year);
    
  }
  else {

    $today = time();
    $d_start_week = start_of_week($today);
    //set $time["date"]
    $time["date"] = date("Ymd",$d_start_week);  
  }
  
  return ($d_start_week); 

}




/////////////////////////////////////////////////////////////////////////////
// Return the number of days in the month                                 //
//Parameters :
// - $time = current time in seconds 
// - $direction = "prev" or "next" month,if others => current month 
///////////////////////////////////////////////////////////////////////////
function nb_days_in_month($sec,$direction) {
    global $cdg_sql;

  if($direction == "prev")
    $move = -1;
  else if($direction == "next")
    $move = 1;
  else
    $move = 0;

  $month = date('m',$sec)+$move; 
  if ($month == 0) $month = 12;
  
  if ($month == "2") //if february
  {
    $year = date('Y',$sec);
    if($year%4 == 0 and $year%100 ==0 and $year%400 !=0) //if bissextile
      return(29);
    else
      return(28);
  }
  else
    return(number_days_month($month));
}


</SCRIPT>
