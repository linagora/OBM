<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : time_query.inc                                               //
//     - Desc : time manager query File                                      //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Time search query execution                                            //
// Parameters:
//   - $time[]    :    time search criteria
//     keys used       interval, date, contact_id
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($time, $p_new_order, $p_order_dir) {
  global $auth, $l_datetime_format, $l_date_text_format, $c_day_fraction, $cdg_sql;

  $date = $time["date"];
  //debug_array($time);
  //reset($time);
  //  echo "date $date<br>";
  //  echo "interval $interval<br>";

  if ($time["interval"] == "week") {
      $array_date = get_week_point($date);
  }
  //  else {
  //    echo "week ". $time["interval"] ." !!<br>";
  //    die ("interval != 'week' not fonctionnal"); 
  //}

  // Query to get any time event for the given User
  //  Optional parameters :
  //   - $array_date with begin and end date
  //   - $company : a company name (not used at this time)
  //   - $company_id : a company id (not used at this time)

  // 0 = task_status renamed as test_status : if =0, check box visible
  //    if <> 0 (i.e. validated), checkbox not visible
  $query = "select Task.*,
      task_id,
      Task.task_id as Id,
      DATE_FORMAT(task_date,'$l_date_text_format') as task_date,
      DATE_FORMAT(task_timeupdate,'$l_datetime_format') as timeupdate,
      DATE_FORMAT(task_timecreate,'$l_datetime_format') as timecreate,
      concat(task_length,concat('/',$c_day_fraction)) as task_length,
      TaskType.*,
      deal_label as task_deal_label,
      company_name as task_company_name,
      0=task_status as test_status
    from (Task left join Deal on task_deal_id = deal_id) 
      left join Company on deal_company_id = company_id,
      TaskType
    where task_tasktype_id = tasktype_id ";

 
  // If a time interval has been specified, get it 
  if (is_array($array_date)) {
    $query .= "and task_date  >='" . $array_date[0] . "' and " .
              "task_date  <'" . $array_date[1] . "'";
  }

  // If a company indication has been specified, get it 
  if ($company != "") {
    $query .= " and company_name like '" . $company . "%'";
  }

  // If a company was sent as parameter, get it
  if ($company_id != "") {
    $query .= " and company_id =" . $company_id ;
  }

  // user_id : array -> size 1 or more 
  if (sizeof($time["user_id"]) == 1)
	  $query .= " and task_user_id = ". $time["user_id"]["0"];
  else {
	$users_list = array_to_list($time["user_id"]);
	$query .= " and task_user_id in $users_list";
  }

  // order
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "Task.task_date";
  $query .= " order by $order $p_order_dir";
   
  display_debug_msg($query, $cdg_sql,"run_query_search");
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  return $obm_db;
}



///////////////////////////////////////////////////////////////////////////////
// Task grouped by day and summed by task length                             //
// Parameters:
//   - $time[]    :    time search criteria
//     keys used       week, user_id, date, date_end
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
// Returns:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_by_day($time, $p_new_order, $p_order_dir) {
  global $auth, $l_date_text_format, $c_day_fraction, $cdg_sql;

  $week = $time["week"];
  $user_id = $time["user_id"];
  $week_start = $time["date"];
  $week_end = $time["date_end"];

  // task_valid 0 if checkbox not to be shown
  $query = "select
      concat(sum(task_length),concat('/',$c_day_fraction)) as task_totallength,
      DATE_FORMAT(task_date,'$l_date_text_format') as task_date,
      (1=sum(task_length)/$c_day_fraction and task_status<>2) as task_valid,
      task_status
    from Task
    where task_date>='$week_start'
      and task_date<='$week_end' ";

  // user_id : array -> size 1 or more 
  if (sizeof($time["user_id"]) == 1)
	  $query .= " and task_user_id = ". $time["user_id"]["0"];
  else {
	$users_list = array_to_list($time["user_id"]);
	$query .= " and task_user_id in $users_list";
  }

  // group and order
  $query .=   "  group by task_date order by Task.task_date";
  
  display_debug_msg($query, $cdg_sql, "run_query_by_day");
//  echo "$query <br>";
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  return $obm_db;
}

///////////////////////////////////////////////////////////////////////////////
// Task grouped by deal                                                      //
// Parameters:
//   - $time[]       : time search criteria
//     keys used       contac_id, date, date_end
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
/* OLD
function run_query_by_deal($time, $p_new_order, $p_order_dir) {
  global $auth, $l_date_text_format, $c_day_fraction, $cdg_sql;

  $week = $time["week"];
  $week_start = $time["date"];
  $week_end = $time["date_end"];
  //  $time["all_users"] = true;

  $query="select 
      FORMAT(sum(task_length)/ $c_day_fraction,3) as task_totallength,
      deal_label as task_deal_label, 
      tasktype_internal, tasktype_label,
      if(tasktype_internal=1, 'interne', company_name) as task_company_name 
    from Task, Deal, TaskType, Company 
    where tasktype_id=task_tasktype_id 
      and task_deal_id = deal_id 
      and task_date>='$week_start' 
      and task_date<='$week_end' 
      and deal_company_id = company_id ";

  //if (tasktype_internal=0, NULL,deal_label) as task_deal_label
  if (isset($time) && $time["all_users"])
	$where_users = "";
  else if (sizeof($time["user_id"]) == 1)
	  $where_users = " and task_user_id = ". $time["user_id"]["0"];
  else {
	$users_list = array_to_list($time["user_id"]);
	$where_users = " and task_user_id in $users_list";
  }
  // order
  $query .= $where_users. " group by task_deal_id order by tasktype_internal, tasktype_label";
  
  display_debug_msg($query, $cdg_sql, "run_query_by_deal");

  $obm_db = new DB_OBM;
  $obm_db->query($query);

  return $obm_db;
}

*/

function run_query_by_deal($time, $p_new_order, $p_order_dir, $alltask=0) {
  global $auth, $l_date_text_format, $c_day_fraction, $cdg_sql;

  $week = $time["week"];
  $week_start = $time["date"];
  $week_end = $time["date_end"];
  //  $time["all_users"] = true;

  //     if(tasktype_internal=1, 'interne', company_name) as task_company_name, 

  $query="select 
     company_name as task_company_name, 
      deal_label as task_deal_label, 
      tasktype_internal, tasktype_label,
      FORMAT(sum(if (task_date < '$week_start', task_length, 0)) / $c_day_fraction,3) as task_totalbefore,
      FORMAT(sum(if (task_date >= '$week_start' and task_date <= '$week_end', task_length, 0)) / $c_day_fraction,3) as task_totalperiod ,
      FORMAT(sum(task_length) / $c_day_fraction,3) as task_totallength
    from (Task left join Deal on task_deal_id = deal_id) left join Company on deal_company_id=company_id, TaskType 
    where tasktype_id=task_tasktype_id 
      and task_date<='$week_end'";

  //if (tasktype_internal=0, NULL,deal_label) as task_deal_label
  if (isset($time) && $time["all_users"])
	$where_users = "";
  else if (sizeof($time["user_id"]) == 1)
	  $where_users = " and task_user_id = ". $time["user_id"]["0"];
  else {
	$users_list = array_to_list($time["user_id"]);
	$where_users = " and task_user_id in $users_list";
  }

  // Do we display all tasks or those not empty for this period

  if (isset($time) && $time["alltask"]) 
	$having_task = ""; 
  else 
 	$having_task = " having task_totalperiod <> 0";

  // order
  $query .= $where_users. $where_task ." group by task_deal_id, tasktype_id $having_task 
      order by tasktype_internal, tasktype_id, task_totalperiod desc";
  
  display_debug_msg($query, $cdg_sql, "run_query_by_deal 2");

  $obm_db = new DB_OBM;
  $obm_db->query($query);

  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Task to validate                                                         //
// Parametes:
//   - $time[]   : task search criteria 
//     keys used : user_id, date, date_end
///////////////////////////////////////////////////////////////////////////////
function run_query_admin($time) {
  global $l_date_text_format,$c_day_fraction, $cdg_sql;

  $user_id = $time["user_id"][0];
  $week_start = $time["date"];
  $week_end = $time["date_end"];
 
  $query = "select
      FORMAT(concat(sum(task_length),concat('/',$c_day_fraction)),3) as task_totallength,
      DATE_FORMAT(task_date,'$l_date_text_format') as task_date,
      task_status=2 as task_status
    from Task
    where task_user_id='$user_id'
      and task_date>='$week_start'
      and task_date<='$week_end'
      and task_status!=0
    group by task_date";

   //order
   $query .= " order by Task.task_date";

   display_debug_msg($query, $cdg_sql, "run_query_admin");

   $obm_db = new DB_OBM;
   $obm_db->query($query);
   return $obm_db;
}
///////////////////////////////////////////////////////////////////////////////
// return array with date where task have been validate 
//   in the given period of time
// Parameters:
//  - $time : array with defined variables
//    keys used : - user_id
//                - date
//                - date_end
// Returns:
//  array of days corresponding to validated date, and task_status : 
//   ("20010911" => 2, "20010912" => 1, "20010913" => 1)
///////////////////////////////////////////////////////////////////////////////
function run_query_valid_search($time) {
global $cdg_sql;

  $user_id = $time["user_id"];
  //  echo "Run_query_valid_search<br>";
  //  print_r($time);
  $query = "select task_date, task_status 
      from Task 
      where task_status != 0 
        and task_date >= '". $time["date"] ."' 
        and task_date <= '". $time["date_end"] ."' ";

  // user_id : array -> size 1 or more 
  if (sizeof($time["user_id"]) == 1)
	  $query .= " and task_user_id = ". $time["user_id"]["0"];
  else {
	  $users_list = array_to_list($time["user_id"]);
	  $query .= " and task_user_id in $users_list";
  }

  // Group
  $query .= " group by task_date ";
  
  display_debug_msg($query, $cdg_sql, "run_query_valid_search");
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  

  $i=0; //for loop
  while($obm_db->next_record()) {
    $valid_array[substr($obm_db->f("task_date"),0,8)]= $obm_db->f("task_status");
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
  
  display_debug_msg($query, $cdg_sql, "run_query_get_task");

  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $obm_db->next_record();
  return $obm_db;
}

///////////////////////////////////////////////////////////////////////////////
// Task Insertion query execution                                         //
// Parameters:
//   - $task[] : Entry's values
//     keys used  : tasktype, project, time, label, date
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($task) {
  global $auth;
  global $cdg_sql;

  
  //  if (debug_level_isset($cdg_param))
  //	debug_array($task);

  $valid_date = run_query_valid_search($task);
  $date = $task["sel_date"];
  $ttype = $task["tasktype"];
  $project = ($task["project"] == 0) ? 'null' : $task["project"];
  $time = $task["time"];
  $label = $task["label"];
  $user_id = $task["user_id"][0];
    

  if ( $ttype != 0 ) //for navigators wich doesn't support HTML 4 => <option disabled>
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
       '$user_id',
       '$key',
       $project,
       '$time',
       '$ttype',
       '$label',
       0)";

       display_debug_msg($query, $cdg_sql, "run_query_insert");
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
       '$user_id',
       '$date',
       $project,
       '$time',
       '$ttype',
       '$label',
       0)";

       display_debug_msg($query, $cdg_sql, "run_query_insert");
       $obm_q = new DB_OBM;
       $obm_q->query($query);  
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Task modification query execution                                         //
// Parameters:
//   - $task[] : Entry's values
//     keys used  : tasktype, project, time, label
///////////////////////////////////////////////////////////////////////////////
function run_query_update($task) {
  global $cdg_sql;

  $id = $task["task_id"];
  $ttype = $task["tasktype"];
  $project = $task["project"];
  $time = $task["time"];
  $label = $task["label"];
  $date = $task["sel_date"];
  
  $query = "update Task set task_date='$date',task_tasktype_id='$ttype',task_length='$time',task_label='$label',task_deal_id='$project' where task_id='$id'";
  display_debug_msg($query, $cdg_sql, "run_query_update");
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

		display_debug_msg($query, $cdg_sql, "run_query_delete");
        $delete_query = new DB_OBM;
        $delete_query->query($query);
      }    
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Task Validation                                                          //
// Parameters:
//   - $time[] : time search criteria  
//   - $parms  : correponding to page parameters
//   - $who    : user who make the validation : 1=admin or other=user
///////////////////////////////////////////////////////////////////////////////
function run_query_validate($parms,$time,$who) {

  global $l_date_text_format;
  global $uid;
  global $cdg_sql, $cdg_param;

  if (debug_level_isset($cdg_param))
	debug_array($time);
  

  if($who == 1) {
    $status = 2;
    ///////////////////////////////////////////
    $zero = "update Task SET task_status=1 where task_status=2 and task_date>='".$time["date"]."' and task_date<='".$time["date_end"]."' ";

	// user_id : array -> size 1  
	$zero .= " and task_user_id = ". $time["user_id"]["0"];

	display_debug_msg($zero, $cdg_sql, "run_query_validate");
    $zero_query = new DB_OBM;                  
    $zero_query->query($zero);
    //////////////////////////////////////////
  }
  else { 
    $status = 1;
    ///////////////////////////////////////////
    $zero = "update Task SET task_status=0 where task_status=1 and task_date>='".$time["date"]."' and task_date<='".$time["date_end"]."' and task_user_id='". $time["user_id"]["0"] ."'";
	display_debug_msg($query, $cdg_sql, "run_query_validate");

	display_debug_msg($zero, $cdg_sql, "run_query_validate");
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
        $query = "UPDATE Task SET task_status=$status where DATE_FORMAT(task_date,'$l_date_text_format') = '$d_task' and task_user_id='". $time["user_id"]["0"]."'";
		display_debug_msg($query, $cdg_sql, "run_query_validate");

        $delete_query = new DB_OBM;
        $delete_query->query($query);
      }	   
    }
  }
}

///////////////////////////////////////////////////////////////////////////////
// Timemanager: qet tasks for given user and month                 
//   - $option : 'all' allow a request for all users
// Returns : DB object result
///////////////////////////////////////////////////////////////////////////////
function run_query_task_one_month($time, $option='') {
global $cdg_sql, $cdg_param;

  $user_id = $time["user_id"];
  //  echo "Run_query_valid_search<br>";
  //  print_r($time);
  $query = "select sum(task_length) as total_length, substring(task_date,7,2) as day_in_month, 
        task_user_id as userid from Task where  
         task_date >= '". $time["date"] ."' 
        and task_date <= '". $time["date_end"] ."' ";

  // user_id : array -> size 1 or more 
  if (empty($option) && (sizeof($time["user_id"]) == 1))
	  $query .= " and task_user_id = ". $time["user_id"]["0"];
  elseif ($option == 'all')
	  display_debug_msg("\$option='all', select for all users", $cdg_param,
						"run_query_task_one_month");
  else {
      die("run_query_task_one_month : This function works only for one user");
  }

  // Group
  $query .= " group by task_user_id, day_in_month order by task_user_id, day_in_month ";
  
  display_debug_msg($query, $cdg_sql, "run_query_task_one_month");
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  
  //  echo "nombre ligne : " . $obm_db->num_rows() . "<br>";
  return $obm_db; 

}

///////////////////////////////////////////////////////////////////////////////
// Timemanager: TaskType select query execution                              //
// Returns : DB object result with all type of task
///////////////////////////////////////////////////////////////////////////////
function run_query_tasktype() {
  global $cdg_sql;

  $query = "select * from TaskType order by tasktype_internal";

  display_debug_msg($query, $cdg_sql, "run_query_tasktype");
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Timemanager: Project select query execution                              //
//    only signed but not still realised...
//    or payed (garantee, hosting...)
// Returns : DB object result with project satisfying criteria
///////////////////////////////////////////////////////////////////////////////
function run_query_project() {
  global $cdg_sql;
  global $project_dealstatus;

  $query = "select deal_id, deal_label, company_name from Deal, Company where company_id = deal_company_id and deal_status_id in $project_dealstatus order by company_name";

  display_debug_msg($query, $cdg_sql, "run_query_project");
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


//////////////////////////////////////////////////////////////////////////
// Get all the contacts of the companyusing obm
//  -- THIS SHOULD CHANGE TO USE THE User TABLE --
//////////////////////////////////////////////////////////////////////////
function run_query_get_obmusers() {
  global $cdg_sql;
 
  $obm_db=new DB_OBM;
  $query="select userobm_id,userobm_lastname,userobm_firstname from UserObm 
          where userobm_archive!=1 order by userobm_lastname";
  display_debug_msg($query, $cdg_sql, "run_query_get_obmusers");

  $obm_db->query($query);
  return $obm_db;
}


//////////////////////////////////////////////////////////////////////////
// return contact_id 
// Param : $user_id
//////////////////////////////////////////////////////////////////////////
// function run_query_get_contact_id($user_id) {
//   global $cdg_sql;

//   $obm_db = new DB_OBM;
//   $query="select userobm_contact_id from UserObm where $user_id  = userobm_id";
//   display_debug_msg($query, $cdg_sql);
//   $obm_db->query($query);
//   $obm_db->next_record(); 
//   return $obm_db->f("userobm_contact_id");
// }

//////////////////////////////////////////////////////////////////////////
// return contact_id 
// Param : $contact_id
//////////////////////////////////////////////////////////////////////////
// function run_query_get_names($contact) { 
//   global $cdg_sql;

//   $obm_db = new DB_OBM;
//   $query = "select contact_lastname,contact_firstname from Contact where $contact=contact_id";
//   display_debug_msg($query, $cdg_sql);

//   $obm_db->query($query);
//   return $obm_db;
// }


//////////////////////////////////////////////////////////////////////////
// Get n day values for a given week, for displaying in the creating task
//  form
// Paramaters: $date : a date starting on the 1st day of the week
//        $num_days : the number of days choosen (5)
// Returns:
//  array with days of the week in "Mon 23 Apr" format
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


    // week planning
  if (is_null($sel_date))
    $p_date=date("YmdHis");
  else
    $p_date=$sel_date;

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
  $weekend=date("YmdHis",$weekstart + 6 * 86400);
  $weekstart=date("YmdHis",$weekstart);  
    
  // Date Couple in an array
  //  if ($debug > 0)
  //    echo "dates debut $weekstart fin $weekend<br>";
  
  return array($weekstart, $weekend);
	
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
// Return the first day of the month in 20020825 format                    //
//Parameters :
// - $time : array with $time["date"] == current date 
///////////////////////////////////////////////////////////////////////////
function first_day_month(&$time, $change=true) {
  global $cdg_sql;

  if ( isset($time["date"]) ) { 

    $year=substr($time["date"],0,4);
    $month=substr($time["date"],4,2);
    $d_start_month=mktime(0,0,0,$month,1,$year);
    // According to doc, 0th day of a month is the last of the previous one
    $d_end_month=mktime(0,0,0,$month+1,0, $year);
    $date_start_month=date("Ymd",$d_start_month);
  }
  else {
    $today = date("Ymd");
	//    echo "first_day_month : today $today <br>";
    $year=substr($today,0,4);
    $month=substr($today,4,2);
    $d_start_month=mktime(0,0,0,$month,1,$year);
    // According to doc, 0th day of a month is the last of the previous one
    $d_end_month=mktime(0,0,0,$month+1,0, $year);
    //set $time["date"]
    $date_start_month=date("Ymd",$d_start_month);
    $time["date"] = $date_start_month;  
  }

  if ($change) {
    // Set last day of the month
    $time["date_end"] = date("Ymd", $d_end_month);
  }
  return ($date_start_month); 

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


/////////////////////////////////////////////////////////////////////////////
// Return a string list of the elements of the array                //
//Parameters :
// - $array = any array 
///////////////////////////////////////////////////////////////////////////
function array_to_list($array) {
  global $cdg_param;

  $i = 0;
  while (sizeof($array) > 0) {
	$elem = array_shift($array);
	if ($i==0)
	  $list = "($elem";
	else
	  $list .= ", $elem"; 
	$i++;
  }
  $list .= ")";

  if (debug_level_isset($cdg_param))
	echo "liste : $list <br>";

  return $list;
}

/////////////////////////////////////////////////////////////////////////////
// Test if the given day is an "Open days" (jours ouvrables)               //
//   we mean by "open day" $c_days_in_a_week, starting on Monday
//    $c_days_in_a_week = 5 : Mon to Fri
//    $c_days_in_a_week = 6 : Mon to Sat
//    $c_days_in_a_week = 7 : Mon to Sun !
//Parameters :
// - $date = a date in 20020917 format 
//Return :
//  true or false
///////////////////////////////////////////////////////////////////////////
function in_workingdays($date) {
   global $working_days, $cdg_param;

  $year=substr($date,0,4);
  $month=substr($date,4,2);
  $day=substr($date,6,2);

  // day of the month in second
  $day_sec = mktime(0,0,0,$month, $day, $year);
  // Weekday
  $wday =date("w", $day_sec);

  if ($working_days[$wday] == 1) 
	return true;
  else
	return false;
}

/////////////////////////////////////////////////////////////////////////////
// Return number of "Open days" (jours ouvrables) in a month     //
//   for a given date in this month
// THIS COPE WITH 5 or 6 (or 7) working days
//  IN A MONTH (if we work on saturday or sunday) 
//  (based on a global array, $working_days)
//Parameters :
// - $date = a date in 20020917 format 
///////////////////////////////////////////////////////////////////////////
function get_nb_working_days($date) {
    global $cdg_param;


  $year=substr($date,0,4);
  $month=substr($date,4,2);
  $this_day=substr($date,6,2);
  $day =1;
  $workingdays = 0;

  /*
  if (debug_level_isset($cdg_param))
	echo "jour $day mois $month an $year <br>";
  */
  // set this to 12h
  $day_sec = mktime(12,0,0,$month, $day, $year);
  // set this to 00h
  $first_next_month = mktime(0,0,0,$month+1, 1, $year);
  while ($day_sec < $first_next_month) {
    if (in_workingdays($year.$month.$day))
      $workingdays++;
  $day++;	  
  $day_sec = mktime(12,0,0,$month, $day, $year);
  }

  /*  if (debug_level_isset($cdg_param))
  	echo "<br> date $date, working days $workingdays <br>";
  */

  return $workingdays;

}



function get_nb_working_days2($date) {
    global $c_days_in_a_week, $cdg_param;


  $year=substr($date,0,4);
  $month=substr($date,4,2);
  $day=substr($date,6,2);


  /*
  if (debug_level_isset($cdg_param))
	echo "jour $day mois $month an $year <br>";
  */

  // first day of the month in second
  $first_day_sec = mktime(0,0,0,$month, $day, $year);
  // Fist Weekday
  $first_wday =date("w", $first_day_sec);

  // Last day
  $last_day_sec = mktime(23,0,0,$month+1, $day-1, $year);
  $last_wday =date("w", $last_day_sec);

  /*
  if (debug_level_isset($cdg_param)) {
	echo "dernier jour". date ("F j, Y H:m:s ", $last_day_sec).
       "<br> 1er : $first_wday, dernier ".date("w",$last_day_sec) ."  <br>";
  }
  */

  // Testing on where is Sunday in the month to calculate
  // First sunday of the month is :
  if ($first_wday ==0) {
	$first_sunday = $first_day_sec; 
    // Count of working day to first sunday
    $workingdays = 0;
  }
  else {
	// nb of days to sunday
	$nb_to_sun = 7-$first_wday;
	$first_sunday = mktime(0,0,0,$month, $day+$nb_to_sun, $year); 
    // Count of working day to first sunday
    $workingdays = $nb_to_sun -1;
  }

  /*
  if (debug_level_isset($cdg_param))
  	echo "dernier jour". date ("F j, Y -- w", $first_sunday).
         "<br> working days $workingdays ; week in a day $c_days_in_a_week<br>";
  */

  // We calculate the number of completes weeks
  // (sunday to sunday)
  $week =0;
  do {
	  $week++;
	  /*
	    if (debug_level_isset($cdg_param))
		  echo "<br>semaines incrémentées $week : ". 
			  date ("F j Y -- w", mktime(0,0,0,$month, $day+$nb_to_sun+7*$week, $year));
	  */
  } while ($last_day_sec > mktime(0,0,0,$month, $day+$nb_to_sun+7*$week, $year));

  $workingdays += $c_days_in_a_week *($week-1);

  /*
  if (debug_level_isset($cdg_param))
  	echo "<br> working days $workingdays <br>";
  */

  // we now add the number of days to end of month
  // What is the last in the month ?
  // If last day is sunday (wday = 0), nothing to add
  if ($last_wday !=0) {
	// If wday > 5 (mean Saturday, one day to sub)
    // Count of working day to last sunday
    $workingdays += ($last_wday > 5 ? 5 : $last_wday);
  }

  return $workingdays;

}



</SCRIPT>
