<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : time_query.inc                                               //
//     - Desc : time manager query File                                      //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Time search query execution                                            //
// Parameters:
//   - $time[]    :    time search criteria
//     keys used       interval, date, contact_id
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($time) {
  global $c_day_fraction, $cdg_sql;

  $date = $time["date"];

  if ($time["interval"] == "week") {
    $array_date = get_week_point($date);
  }

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $datetask = sql_date_format($db_type, "timetask_date", "date_task");
  $timeupdate = sql_date_format($db_type, "timetask_timeupdate", "date_timeupdate");
  $timecreate = sql_date_format($db_type, "timetask_timecreate", "date_timecreate");
  $ctt[0]["type"] = "field";
  $ctt[0]["value"] = "timetask_length";
  $ctt[1]["type"] = "string";
  $ctt[1]["value"] = "/";
  $ctt[2]["type"] = "string";
  $ctt[2]["value"] = "$c_day_fraction";
  $concat = sql_string_concat($db_type, $ctt);
  $if_status = sql_if($db_type, "(timetask_status != 2)", 1, 0);

  $query = "
    select timetask_id,
      $datetask,
      $timeupdate,
      $timecreate,
      $concat as timetask_length,
      tasktype_label,
      project_name as timetask_project_name,
      timetask_label,
      projecttask_label,
      company_name as timetask_company_name,
      timetask_status,
      $if_status as test_status,
      SUBSTRING(timetask_date,8,1) as date_parity
    from TimeTask
      left join ProjectTask on timetask_projecttask_id = projecttask_id
      left join Project on projecttask_project_id = project_id
      left join Company on project_company_id = company_id, TaskType 
    where timetask_tasktype_id = tasktype_id
      and timetask_date >='". $array_date[0] ."' 
      and timetask_date <'". $array_date[1] ."'
      and timetask_user_id = ". $time["user_id"] ."
    order by timetask_date
    ";
   
  display_debug_msg($query, $cdg_sql,"run_query_search");
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Task grouped by month and summed by task project                          //
// Parameters:
//   - $time[]    :    time search criteria
//     keys used       date
// Returns:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_stat_project($time) {
  global $c_day_fraction, $cdg_sql;

  $tail = 0;
  $user = $time["user_id"];
  $uid_tab = "(";

  while ($key = each($time["user_id"])) {
    $uid = $key["value"];
    $uid_tab .= ($tail) ? ", $uid" : "$uid";
    $tail = 1;
  }

  $uid_tab .= ")";

  $year=substr($time["date"],0,4);
  $month=substr($time["date"],4,2);
  $day=1;

  // First and last days of month
  $month_begin = date("Ymd", mktime(0,0,0,$month, 1, $year));
  $month_end = date("Ymd", mktime(23,0,0,$month+1, $day-1, $year));

  // task_valid 0 if checkbox not to be shown
  $query = "
    select
      project_name,
      company_name,
      FORMAT(sum(if (timetask_date < '$month_begin', timetask_length, 0)) / $c_day_fraction,3) as total_before,
      FORMAT(sum(if (timetask_date >= '$month_begin' and timetask_date <= '$month_end', timetask_length, 0)) / $c_day_fraction,3) as total_length,
      FORMAT(sum(timetask_length) / $c_day_fraction,3) as total_after
    from TimeTask, ProjectTask, Project
    left join Company on project_company_id = company_id
    where timetask_date <= '$month_end'
      and timetask_projecttask_id = projecttask_id
      and projecttask_project_id = project_id
      and timetask_user_id in $uid_tab
    group by project_id
    having (sum(if (timetask_date >= '$month_begin' and timetask_date <= '$month_end', timetask_length, 0))) != 0
    ";

  display_debug_msg($query, $cdg_sql, "run_query_stat_project_by_month");

  $obm_db = new DB_OBM;
  $obm_db->query($query);
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Task grouped by month and summed by tasktype                              //
// Parameters:
//   - $time[]    :    time search criteria
//     keys used       date
// Returns:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_stat_tasktype($time) {
  global $c_day_fraction, $cdg_sql;

  $tail = 0;
  $user = $time["user_id"];
  $uid_tab = "(";

  while ($key = each($time["user_id"])) {
    $uid = $key["value"];
    $uid_tab .= ($tail) ? ", $uid" : "$uid";
    $tail = 1;
  }

  $uid_tab .= ")";

  $year=substr($time["date"],0,4);
  $month=substr($time["date"],4,2);
  $day=1;

  // First and last days of month
  $month_begin = date("Ymd", mktime(0,0,0,$month, 1, $year));
  $month_end = date("Ymd", mktime(23,0,0,$month+1, $day-1, $year));

  // task_valid 0 if checkbox not to be shown
  $query = "
    select
      tasktype_label,
      FORMAT(sum(if (timetask_date < '$month_begin', timetask_length, 0)) / $c_day_fraction,3) as total_before,
      FORMAT(sum(if (timetask_date >= '$month_begin' and timetask_date <= '$month_end', timetask_length, 0)) / $c_day_fraction,3) as total_length,
      FORMAT(sum(timetask_length) / $c_day_fraction,3) as total_after
    from TimeTask, TaskType
    where timetask_date <= '$month_end'
      and timetask_tasktype_id = tasktype_id
      and timetask_user_id in $uid_tab
    group by tasktype_id
    having (sum(if (timetask_date >= '$month_begin' and timetask_date <= '$month_end', timetask_length, 0))) != 0
    ";

  display_debug_msg($query, $cdg_sql, "run_query_stat_project_by_month");

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

  $week_span = get_week_point($time["date"]);

  $user_id = $time["user_id"];
  //  echo "Run_query_valid_search<br>";
  //  print_r($time);
  $query = "select timetask_date, timetask_status
      from TimeTask 
      where timetask_status != 0 
        and timetask_date >= '". $week_span[0] ."' 
        and timetask_date <= '". $week_span[1]. "'";

  // user_id : array -> size 1 or more 
  if (sizeof($time["user_id"]) == 1)
	  $query .= " and timetask_user_id = ". $time["user_id"];
  else {
	  $users_list = array_to_list($time["user_id"]);
	  $query .= " and timetask_user_id in $users_list";
  }

  // Group
  $query .= " group by timetask_date, timetask_status ";
  
  display_debug_msg($query, $cdg_sql, "run_query_valid_search");
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  

  $i=0; //for loop
  while ($obm_db->next_record()) {
    $valid_array[substr($obm_db->f("timetask_date"),0,8)]= $obm_db->f("timetask_status");
  }
   
  return $valid_array; 
}


///////////////////////////////////////////////////////////////////////////////
// get all for task with $id id
// Parametes:
//   - $id : id for task
///////////////////////////////////////////////////////////////////////////////
function run_query_get_task($id) {
  global $cdg_sql;

  $query = "
    select
       TimeTask.*,
       projecttask_project_id as timetask_project_id  
     from TimeTask
     left join ProjectTask on timetask_projecttask_id = projecttask_id
     where timetask_id = $id";
  
  display_debug_msg($query, $cdg_sql, "run_query_get_task");

  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $obm_db->next_record();
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Task Insertion query execution
// Parameters:
//   - $task[] : Entry's values
//     keys used  : tasktype, project, time, label, date
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($task) {
  global $auth;
  global $cdg_sql;

  $valid_date = run_query_valid_search($task);
  $date = $task["sel_date"];
  $ttype = $task["tasktype"];
  $projecttask = ($task["projecttask"] == 0) ? 'null' : $task["projecttask"];
  $time = $task["time"];
  $label = $task["label"];
  $user_id = $task["user_id"];
    

  if ( $ttype != 0 ) //for navigators wich doesn't support HTML 4 => <option disabled>
  { 
     if(is_array($date)){
     foreach($date as $key => $value) {
       $query = "
       insert into TimeTask (
         timetask_timeupdate,
         timetask_timecreate,
         timetask_userupdate,
         timetask_usercreate,
         timetask_user_id,
         timetask_date,
         timetask_projecttask_id,
         timetask_length,
         timetask_tasktype_id,
         timetask_label,
         timetask_status)
       values(null,
         '" . date("Y-m-d H:i:s") . "',
         null,
         '" . $auth->auth["uid"] . "',
         '$user_id',
         '$key',
         $projecttask,
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
       $query = "
       insert into TimeTask (
         timetask_timeupdate,
         timetask_timecreate,
         timetask_userupdate,
         timetask_usercreate,
         timetask_user_id,
         timetask_date,
         timetask_projecttask_id,
         timetask_length,
         timetask_tasktype_id,
         timetask_label,
         timetask_status)
       values(null,
         '" . date("Y-m-d H:i:s") . "',
         null,
         '" . $auth->auth["uid"] . "',
         '$user_id',
         '$date',
         $projecttask,
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
  $projecttask = $task["projecttask"];
  $time = $task["time"];
  $label = $task["label"];
  $date = $task["sel_date"];
  
  $query = "
  update TimeTask set
    timetask_date='$date',
    timetask_tasktype_id='$ttype',
    timetask_length='$time',
    timetask_label='$label',
    timetask_projecttask_id='$projecttask'
  where timetask_id='$id'";

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
        $query = "delete from TimeTask where timetask_id = $id_task";

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
//   - $time[]    :    time search criteria
//     keys used
///////////////////////////////////////////////////////////////////////////////
function run_query_validate($who) {
  
  global $c_day_fraction;

  // to see if a day is full
  $cdf = $c_day_fraction;

  // look for days that can be validated
  // an array is made with the result
  // should use subselect (mysql 4.1 and further)
  $query = "select timetask_date
            from TimeTask
            where timetask_user_id=$who
            and timetask_status=0
            group by timetask_date
            having sum(timetask_length) = $cdf";
  
  display_debug_msg($query, $cdg_sql, "run_query_validate(1)");
  $search_valid_query = new DB_OBM;
  $search_valid_query->query($query);

  if ($search_valid_query->nf() != 0) {
    
    $search_valid_query->next_record();
    $date = $search_valid_query->f("timetask_date");
    $date_list = "($date";

    while($search_valid_query->next_record()) {
      $date = $search_valid_query->f("timetask_date");
      $date_list .= ", $date";
    }

    $date_list .= ")";

    // validate dates that are fully filled
    $query = "update TimeTask
              set timetask_status = 1
              where timetask_user_id=$who
              and timetask_date in $date_list";
    
    display_debug_msg($query, $cdg_sql, "run_query_validate(2)");
    $valid_query = new DB_OBM;
    $valid_query->query($query);
  }

  // look for days that are not supposed to be validated
  // ie. if a task has been updated...
  $query = "select timetask_date
            from TimeTask
            where timetask_user_id=$who
            and timetask_status != 2
            group by timetask_date
            having sum(timetask_length) != $cdf";
  
  display_debug_msg($query, $cdg_sql, "run_query_validate(3)");
  $search_unvalid_query = new DB_OBM;
  $search_unvalid_query->query($query);

  if ($search_unvalid_query->nf() != 0) {
    
    $search_unvalid_query->next_record();
    $date = $search_unvalid_query->f("timetask_date");
    $date_list = "($date";

    while($search_unvalid_query->next_record()) {
      $date = $search_unvalid_query->f("timetask_date");
      $date_list .= ", $date";
    }

    $date_list .= ")";

    // validate dates that are fully filled
    $query = "update TimeTask
              set timetask_status=0
              where timetask_user_id=$who
              and timetask_date in $date_list";
    
    display_debug_msg($query, $cdg_sql, "run_query_validate(4)");
    $unvalid_query = new DB_OBM;
    $unvalid_query->query($query);
  }
 
}


///////////////////////////////////////////////////////////////////////////////
// Task Validation by the Admin (once each month)                            //
// Parameters:
//   - $time[]    :    time search criteria
//     keys used       date, user_id
///////////////////////////////////////////////////////////////////////////////
function run_query_adminvalidate($time) {
  global $cdg_sql, $cdg_param;

  $year=substr($time["date"],0,4);
  $month=substr($time["date"],4,2);
  $day=1;

  // First and last days of month
  $month_begin = date("Ymd", mktime(0,0,0,$month, 1, $year));
  $month_end = date("Ymd", mktime(23,0,0,$month+1, $day-1, $year));

  $user_id = $time["user_id"];

  $query = "
    update TimeTask
    set timetask_status = 2
    where timetask_user_id = $user_id
      and timetask_date >= '$month_begin' 
      and timetask_date <= '$month_end' 
    ";

  display_debug_msg($query, $cdg_sql, "run_query_adminvalidate");
  $obm_db = new DB_OBM;
  $obm_db->query($query);

  return $obm_db; 
}

///////////////////////////////////////////////////////////////////////////////
// Task Validation Cancelling for the admin                                  //
// Parameters:
//   - $time[]    :    time search criteria
//     keys used       date, user_id
///////////////////////////////////////////////////////////////////////////////
function run_query_adminunvalidate($time) {
  global $cdg_sql, $cdg_param;

  $year=substr($time["date"],0,4);
  $month=substr($time["date"],4,2);
  $day=1;

  // First and last days of month
  $month_begin = date("Ymd", mktime(0,0,0,$month, 1, $year));
  $month_end = date("Ymd", mktime(23,0,0,$month+1, $day-1, $year));

  $user_id = $time["user_id"];

  $query = "
    update TimeTask
    set timetask_status = 1
    where timetask_user_id = $user_id
      and timetask_date >= '$month_begin' 
      and timetask_date <= '$month_end' 
    ";

  display_debug_msg($query, $cdg_sql, "run_query_adminvalidate");
  $obm_db = new DB_OBM;
  $obm_db->query($query);

  return $obm_db; 
}

///////////////////////////////////////////////////////////////////////////////
// Short month query (gets the total of task length for each user this month)
//   - $time[]    :    time search criteria
//     keys used       date, user_id
// Returns : DB object result
///////////////////////////////////////////////////////////////////////////////
function run_query_short_month($time) {
  global $cdg_sql, $cdg_param;

  $year=substr($time["date"],0,4);
  $month=substr($time["date"],4,2);
  $day=1;

  // First and last days of month
  $month_begin = date("Ymd", mktime(0,0,0,$month, 1, $year));
  $month_end = date("Ymd", mktime(23,0,0,$month+1, $day-1, $year));

  $query = "
    select
      userobm_lastname,
      userobm_firstname,
      sum(timetask_length) as total_length,
      timetask_user_id as userid,
      timetask_status
    from TimeTask, UserObm
    where timetask_date >= '$month_begin' 
      and timetask_date <= '$month_end'
      and timetask_user_id = userobm_id
    group by timetask_user_id
    order by userobm_lastname, userobm_firstname
    ";
  
  display_debug_msg($query, $cdg_sql, "run_query_task_one_month");
  $obm_db = new DB_OBM;
  $obm_db->query($query);

  return $obm_db; 
}


///////////////////////////////////////////////////////////////////////////////
// Timemanager: qet tasks for given user and month                 
// Parameters:
//   - $time[] : time search criteria
// Returns : DB object result
///////////////////////////////////////////////////////////////////////////////
function run_query_task_one_month($time) {
  global $cdg_sql, $cdg_param;

  $year=substr($time["date"],0,4);
  $month=substr($time["date"],4,2);
  $day=1;

  // First and last days of month
  $month_begin = date("Ymd", mktime(0,0,0,$month, 1, $year));
  $month_end = date("Ymd", mktime(23,0,0,$month+1, $day-1, $year));

  $user_id = $time["user_id"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $date = sql_date_format($db_type, "timetask_date", "date");

  $query = "
    SELECT
      sum(timetask_length) as total_length,
      $date,
      timetask_status
    FROM TimeTask, UserObm
    WHERE timetask_date >= '$month_begin' 
      and timetask_date <= '$month_end'
      and timetask_user_id = userobm_id
      and timetask_user_id = $user_id
    GROUP by timetask_user_id, date, timetask_status
    ORDER by date
    ";
  
  display_debug_msg($query, $cdg_sql, "run_query_task_one_month");
  $obm_db = new DB_OBM;
  $obm_db->query($query);

  return $obm_db; 
}


///////////////////////////////////////////////////////////////////////////////
// Timemanager: get tasks for given user and week, and returns total by day
// Parameters:
//   - $time[] : time search criteria
// Returns : DB object result
///////////////////////////////////////////////////////////////////////////////
function run_query_task_one_week($time) {
global $cdg_sql, $cdg_param;

  $week = get_week_point($time["date"]);

  $user_id = $time["user_id"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $date = sql_date_format($db_type, "timetask_date", "date");

  $query = "
    SELECT 
      sum(timetask_length) as total_length,
      $date
    FROM TimeTask
    WHERE timetask_date >= '$week[0]' 
      and timetask_date <= '$week[1]'
      and timetask_user_id = $user_id
    GROUP by date
    ORDER by date";

  display_debug_msg($query, $cdg_sql, "run_query_task_one_week");
  $obm_q->query($query);
  
  return $obm_q; 
}




///////////////////////////////////////////////////////////////////////////////
// Timemanager: TaskType select query execution                              //
// Returns : DB object result with all type of task
///////////////////////////////////////////////////////////////////////////////
function run_query_used_tasktype($uid = 0) {
  global $cdg_sql, $ctt_sales, $ctt_research, $ctt_others;

  if ($uid) {
    $query = "
      select distinct TaskType.*
      from TaskType, Project, ProjectUser
      where tasktype_id = project_tasktype_id
        and project_id = projectuser_project_id
        and project_archive = '0'
        and projectuser_user_id = '$uid'
      order by tasktype_internal, tasktype_label";

    display_debug_msg($query, $cdg_sql, "run_query_used_tasktype");
    $obm1_q= new DB_OBM;
    $obm1_q->query($query);

    $query = "
      select TaskType.*
      from TaskType
      where tasktype_internal='$ctt_others'
      order by tasktype_internal, tasktype_label";

    display_debug_msg($query, $cdg_sql, "run_query_used_tasktype");
    $obm2_q= new DB_OBM;
    $obm2_q->query($query);

    $obm_q = Array($obm1_q, $obm2_q);

  } else {
    $query = "
      select *
      from TaskType
      order by tasktype_internal, tasktype_label";

    display_debug_msg($query, $cdg_sql, "run_query_used_tasktype");
    $obm_q= new DB_OBM;
    $obm_q->query($query);
  }

  return $obm_q;
}

//     $query = "
//       select distinct TaskType.*
//       from TaskType
//         left join Deal on (tasktype_id = deal_tasktype_id
//                            and deal_archive = 0
//                            and deal_project_status != 0)
//         left join ProjectUser on (deal_id=projectuser_project_id
//                                   and projectuser_projectedtime != 0)
//       where (tasktype_internal in (0,1) and (projectuser_user_id=$uid))
//         or tasktype_internal='$ctt_others'
//       order by tasktype_internal, tasktype_label";

///////////////////////////////////////////////////////////////////////////////
// Timemanager: Project select query execution                              //
// Returns : DB object result with project satisfying criteria
///////////////////////////////////////////////////////////////////////////////
function run_query_project($time) {
  global $cdg_sql;

  $uid = $time["user_id"];

  $query = "
    select distinct
      project_id,
      project_company_id,
      project_name,
      company_name,
      project_tasktype_id
    from Project
      left join Company on company_id = project_company_id,
      ProjectTask, ProjectUser
    where project_archive='0'
      and projectuser_projecttask_id = projecttask_id
      and projecttask_project_id = project_id
      and projectuser_user_id = '$uid'
      and projectuser_projectedtime != '0'
      order by project_tasktype_id, company_name, project_name";

  display_debug_msg($query, $cdg_sql, "run_query_project");
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Timemanager: ProjectTask select query execution                           //
// Returns : DB object result with project satisfying criteria               //
// This function does return the task_label, and, in case a parenttask_label //
//   exists, it returns "parenttask_label --- task_label".                   //
//  Used for update and insert task form                                     //
///////////////////////////////////////////////////////////////////////////////
function run_query_projecttask($time) {
  global $cdg_sql;

  $uid = $time["user_id"];

  $obm_q= new DB_OBM;
  $db_type = $obm_q->type;
	// array of the sql concataneted strings
	$a_concat[]=array("type"=>"field", "value"=>"b.projecttask_label");
  $a_concat[]=array("type"=>"string", "value"=>" --- ");
  $a_concat[]=array("type"=>"field", "value"=>"a.projecttask_label");
  $concat = sql_string_concat($db_type, $a_concat);
  display_debug_msg($concat, $cdg_sql, "run_query_projecttask : $db_type concat : ");
  $sql_if = sql_if($db_type, "a.projecttask_parenttask_id = 0", "a.projecttask_label", $concat);

  $query = "
    select
      a.projecttask_id,
      $sql_if as projecttask_label,
      a.projecttask_project_id
    from ProjectTask a left join ProjectTask b 
       on a.projecttask_parenttask_id = b.projecttask_id,
       ProjectUser
    where projectuser_projecttask_id = a.projecttask_id
      and projectuser_user_id = $uid
      and projectuser_projectedtime != 0
      order by a.projecttask_project_id, a.projecttask_label";

  display_debug_msg($query, $cdg_sql, "run_query_projecttask");
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


//////////////////////////////////////////////////////////////////////////
// Return th Id of people that will see the globalview
//  -- THIS SHOULD CHANGE TO USE THE User TABLE --
//////////////////////////////////////////////////////////////////////////
function run_query_managers() {
  global $cdg_sql;
 
  // should use union (mysql 4.0.0 and further)

  $manager=new DB_OBM;
  $query="select projectuser_user_id as manager_id from ProjectUser where projectuser_manager = 1";
  display_debug_msg($query, $cdg_sql, "run_query_managers(manager)");
  $manager->query($query);

  $admin=new DB_OBM;
  $query="select userobm_id as manager_id from UserObm where userobm_perms = 'admin'";
  display_debug_msg($query, $cdg_sql, "run_query_managers(admin)");
  $admin->query($query);

  $m_array = array();

  while ($manager->next_record())  {
    $id = $manager->f("manager_id");
    array_push($m_array, $id);
  }

  while ($admin->next_record())  {
    $id = $admin->f("manager_id");
    array_push($m_array, $id);
  }
    
  return $m_array;
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


///////////////////////////////////////////////////////////////////////////////
// Get an array of working days date for the week begining at the date given
// Paramaters:
//   - $date : a date starting on the 1st day of the week
// Returns:
//  array with for each days of the week : 0->"Mon 23 Apr" 1->20040413
///////////////////////////////////////////////////////////////////////////////
function get_week_working_days_dates($date) {
  global $c_week_first_day, $l_daysofweekshort, $l_monthsofyearshort;
  
  $cpt_end = $c_week_first_day + 7;
  for ($i=$c_week_first_day ; $i < $cpt_end; $i++) {
    if (is_working_day($date)) {
      $date_link = date("Ymd", $date);
      $month = date("m", $date);
      $day = date("d", $date);
      $dis_date = $l_daysofweekshort[$i%7] ." $day " . $l_monthsofyearshort[$month - 1];
      
      $array_date[$i][0] = $dis_date;
      $array_date[$i][1] = $date_link;
    }

    $date += 86400;
  }
  
  return $array_date;
}


///////////////////////////////////////////////////////////////////////////////
// Get the starting and ending date of a week in YYYYMMDD format
// Parameters:
//   - $date : date with YYYYMMDD format
///////////////////////////////////////////////////////////////////////////////
function get_week_point($date) {

  if (is_null($date)) {
    $p_date = date("Ymd");
  } else {
    $p_date = $date;
  }

  $week_first_day = first_day_week($p_date);
  // beginning and end of week (Ymd format)
  $weekstart = date("Ymd", $week_first_day);
  $weekend = date("Ymd", $week_first_day + 6 * 86400);

  return array($weekstart, $weekend);
}


///////////////////////////////////////////////////////////////////////////////
// Return the first day of the week (in timestamp format) in where the date is
// Parameters :
// - $date : with YYYYMMDD format
///////////////////////////////////////////////////////////////////////////////
function first_day_week($date) {
  global $c_week_first_day;

  $date_int = strtotime($date);
  $year = date("Y", $date_int);
  $month = date("m", $date_int);
  $day = date("d", $date_int);
  $dow = date("w", $date_int);
  $obm_dow = ($dow - $c_week_first_day + 7) % 7;
  $weekstart = mktime(0, 0, 0, $month, $day - $obm_dow, $year);

  return ($weekstart); 
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


///////////////////////////////////////////////////////////////////////////////
// Test if the given day is an "Open days" (jours ouvrables)
// Parameters :
// - $date = a date in timestamp format 
// Return :
//  true or false
///////////////////////////////////////////////////////////////////////////////
function is_working_day($date) {
  global $c_working_days;

  $wday = date("w", $date);

  if ($c_working_days[$wday] == 1) 
    return true;
  else
    return false;
}


///////////////////////////////////////////////////////////////////////////////
// Return number of "Open days" (jours ouvrables) in a month
// Parameters :
//   - $date = a date in the month in the AAAAMMDD format 
///////////////////////////////////////////////////////////////////////////////
function get_nb_working_days($date) {

  $date_int = strtotime(substr($date,0,6)."01");
  $date_int += 43200;  // We add 12h
  $month = date("m", $date_int);
  $year = date("Y", $date_int);
  $first_next_month = mktime(0,0,0,$month+1, 1, $year);

  $workingdays = 0;
  while ($date_int < $first_next_month) {
    if (is_working_day($date_int)) {
      $workingdays++;
    }
    $date_int += 86400;
  }

  return $workingdays;
}


</script>
