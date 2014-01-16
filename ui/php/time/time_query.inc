<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



///////////////////////////////////////////////////////////////////////////////
// OBM - File : time_query.inc                                               //
//     - Desc : time manager query File                                      //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Time search query execution
// Parameters:
//   - $time[]    :    time search criteria
//     keys used       interval, date, contact_id
///////////////////////////////////////////////////////////////////////////////
function run_query_time_task_list($time) {
  global $c_day_fraction, $cdg_sql;

  $date = $time['date'];
  $user_id = $time['user_id'];

  //if ($time['interval'] == 'week') {
    $array_date = get_time_week_point($date);
  //}

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $datetask = sql_date_format($db_type, 'timetask_date', 'date_task');
  $timeupdate = sql_date_format($db_type, 'timetask_timeupdate', 'date_timeupdate');
  $timecreate = sql_date_format($db_type, 'timetask_timecreate', 'date_timecreate');
  $ctt[0]['type'] = 'field';
  $ctt[0]['value'] = 'timetask_length';
  $ctt[1]['type'] = 'string';
  $ctt[1]['value'] = '/';
  $ctt[2]['type'] = 'string';
  $ctt[2]['value'] = "$c_day_fraction";
  $concat = sql_string_concat($db_type, $ctt);
  $if_status = sql_if($db_type, '(timetask_status != 2)', 1, 0);

  $query = "
    SELECT timetask_id,
      timetask_user_id,
      project_id,
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
      $if_status as test_status
    FROM TimeTask
      LEFT JOIN ProjectTask ON timetask_projecttask_id = projecttask_id
      LEFT JOIN Project ON projecttask_project_id = project_id
      LEFT JOIN Company ON project_company_id = company_id, TaskType 
    WHERE timetask_tasktype_id = tasktype_id
      AND timetask_date >='". $array_date[0] ."' 
      AND timetask_date <'". $array_date[1] ."'
      AND timetask_user_id = '$user_id'
    ORDER BY timetask_date
";

  display_debug_msg($query, $cdg_sql,'run_query_search');
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  return $obm_db;
}


/**
 * Determine dates range according to interval and date given
 * and store it in parameters $params ['date_begin'], ['date_end']
 *
 * @param mixed &$params : parameter hash
 **/
function get_time_params_date_range(&$params) {
  global $cdg_param, $c_dst_err;

  $action = $params['action'];
  $interval = $params['interval'];

  $iso_date = of_isodate_convert($params['date']);
  $ts_date = strtotime($iso_date);
  $date_begin = $iso_date;

  if ($interval == 'week') {
    $d_start_week = of_date_get_first_day_week($iso_date);
    $d_end_week = $d_start_week+6*86400;
    $date_begin = of_isodate_format($d_start_week);
    $date_end = of_isodate_format($d_end_week);
  } else if ($interval == 'month') {
    $month = of_date_get_month($ts_date);
    $year = of_date_get_year($ts_date);
    $ts_last_day_month = mktime(0,0,0,$month+1,0,$year);
    $date_end = of_isodate_format($ts_last_day_month);
  } else {
    if ($params['date_end'] != '') {
      // If date end set, dates have been set
      $date_end = of_isodate_convert($params['date_end'], true);
    } else {
      // No date set, default to the current month
      $month = of_date_get_month($ts_date);
      $year = of_date_get_year($ts_date);
      $ts_last_day_month = mktime(0,0,0,$month+1,0,$year);
      $date_end = of_isodate_format($ts_last_day_month);
      $date_begin = of_isodate_format(mktime(0,0,0,$month,1,$year));
    }
  }

  $params['date_begin'] = $date_begin;
  $params['date_end'] = $date_end;
}


/**
 * Task grouped by month and summed by project
 *
 * @param mixed $time[] : time search criteria
 * @return mixed info array
 */
function get_time_stat_project($time) {
  global $c_day_fraction, $cdg_sql;

  $uid_tab = array();
  $uids_str = '';
  while ($key = each($time['user_ids'])) {
    $uid_tab[] = $key['value'];
  }
  if (count($uid_tab)) {
    $uids_str = 'AND timetask_user_id IN ('.implode(', ', $uid_tab).')';
  }

  $date_begin = $time['date_begin'];
  $date_end = $time['date_end'];
  $obm_q = new DB_OBM;
  $iftd_begin = sql_if($obm_q->type, "timetask_date < '$date_begin'", 'timetask_length', 0);
  $iftd_end  = sql_if($obm_q->type, "timetask_date >= '$date_begin' and timetask_date <= '$date_end' ", 'timetask_length', 0);

  $query = "
    SELECT
      project_id,
      project_name,
      tasktype_label,
      company_name,
      (sum($iftd_begin) / $c_day_fraction) as total_before,
      (sum($iftd_end) / $c_day_fraction) as total_length,
      (sum(timetask_length) / $c_day_fraction) as total_after
    FROM TimeTask
      INNER JOIN ProjectTask ON timetask_projecttask_id = projecttask_id
      INNER JOIN Project ON projecttask_project_id = project_id
      LEFT JOIN Company ON project_company_id = company_id
      LEFT JOIN TaskType ON project_tasktype_id = tasktype_id
    WHERE timetask_date <= '$date_end'
      $uids_str
    GROUP BY project_id,project_name,tasktype_label,company_name
    HAVING (sum($iftd_end)) != 0
";

  display_debug_msg($query, $cdg_sql, 'get_time_stat_project(1)');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  $res = array();
  while ($obm_q->next_record()) {
    $p_id = $obm_q->f('project_id');
    $p_name = $obm_q->f('project_name');
    $tt_label = $obm_q->f('tasktype_label');
    $company = $obm_q->f('company_name');
    $time_before = round($obm_q->f('total_before'),3);
    $time = round($obm_q->f('total_length'),3);
    $time_after = round($obm_q->f('total_after'),3);

    $res[$p_id]['project'] = $p_name;
    $res[$p_id]['tasktype'] = $tt_label;
    $res[$p_id]['company'] = $company;
    $res[$p_id]['time'] = $time;
    $res[$p_id]['time_before'] = $time_before;
    $res[$p_id]['time_after'] = $time_after;

    $res[0]['time_total'] += $time;
    $res[0]['time_before_total'] += $time_before;
    $res[0]['time_after_total'] += $time_after;
  }

  $keys = array_keys($res);
  if (count($keys) > 0) {
    $projects = implode(',', $keys);
    $where_pid = "WHERE project_id IN($projects)";

    $query = "
    SELECT
      project_id,
      project_name,
      (sum(projectuser_missingtime)) as total_missing
    FROM ProjectUser
      INNER JOIN Project ON projectuser_project_id = project_id
    $where_pid
    GROUP BY project_id, project_name
";

    display_debug_msg($query, $cdg_sql, 'get_time_stat_project(2)');
    $obm_q->query($query);
    while ($obm_q->next_record()) {
      $p_id = $obm_q->f('project_id');
      $missing = $obm_q->f('total_missing');
      $res[$p_id]['missing'] = $missing;
    }

    $query = "
    SELECT
      projecttask_project_id,
      (sum($iftd_begin) / $c_day_fraction) as total_before,
      (sum($iftd_end) / $c_day_fraction) as total_length,
      (sum(timetask_length) / $c_day_fraction) as total_after
    FROM ProjectTask
      INNER JOIN TimeTask on projecttask_id=timetask_projecttask_id
    WHERE timetask_date <= '$date_end'
      AND timetask_projecttask_id = projecttask_id
      AND projecttask_project_id IN ($projects)
    GROUP BY projecttask_project_id
    having (sum($iftd_end)) != 0
";

    display_debug_msg($query, $cdg_sql, 'get_time_stat_project(project)');
    $obm_q->query($query);
    $p = array();
    while ($obm_q->next_record()) {
      $p_id = $obm_q->f('projecttask_project_id');
      $time_before = round($obm_q->f('total_before'),3);
      $time = round($obm_q->f('total_length'),3);
      $time_after = round($obm_q->f('total_after'),3);

      $res[$p_id]['all_time'] = $time;
      $res[$p_id]['all_time_before'] = $time_before;
      $res[$p_id]['all_time_after'] = $time_after;

      $res[0]['all_time_total'] += $time;
      $res[0]['all_time_before_total'] += $time_before;
      $res[0]['all_time_after_total'] += $time_after;
    }
  }

  return $res;
}


/**
 * Task grouped by month and summed by tasktype
 *
 * @param mixed $time[] : time search criteria
 * @return mixed info array
 */
function get_time_stat_tasktype($time) {
  global $c_day_fraction, $cdg_sql;

  $tail = 0;
  $uid_tab = '(';
  while ($key = each($time['user_ids'])) {
    $uid = $key['value'];
    $uid_tab .= ($tail) ? ", $uid" : "$uid";
    $tail = 1;
  }
  $uid_tab .= ')';

  $date_begin = $time['date_begin'];
  $date_end = $time['date_end'];

  $obm_q = new DB_OBM;
  $iftd_begin = sql_if($obm_q->type, "timetask_date < '$date_begin'", 'timetask_length', 0);
  $iftd_end  = sql_if($obm_q->type, "timetask_date >= '$date_begin' and timetask_date <= '$date_end' ", 'timetask_length', 0);
  $query = "
    SELECT
      timett.tasktype_id as time_tasktype_id,
      timett.tasktype_label as time_tasktype_label,
      projtt.tasktype_id as proj_tasktype_id,
      projtt.tasktype_label as proj_tasktype_label,
      (sum($iftd_begin) / $c_day_fraction) as total_before,
      (sum($iftd_end) / $c_day_fraction) as total_length,
      (sum(timetask_length) / $c_day_fraction) as total_after
    FROM TimeTask JOIN TaskType as timett on timetask_tasktype_id=timett.tasktype_id
      LEFT JOIN ProjectTask on timetask_projecttask_id=projecttask_id 
      LEFT JOIN Project on projecttask_project_id=project_id 
      LEFT JOIN TaskType as projtt on project_tasktype_id=projtt.tasktype_id 
    WHERE timetask_date <= '$date_end'
      AND timetask_user_id in $uid_tab
    GROUP BY projtt.tasktype_id, timett.tasktype_id, timett.tasktype_label,projtt.tasktype_id,projtt.tasktype_label
    having (sum($iftd_end)) != 0
    ORDER BY projtt.tasktype_id";

  display_debug_msg($query, $cdg_sql, 'get_time_stat_tasktype');

  $obm_q->query($query);

  $res = array();
  while ($obm_q->next_record()) {
    $tt_id = $obm_q->f('time_tasktype_id');
    $tt_label = $obm_q->f('time_tasktype_label');
    $proj_tt_id = $obm_q->f('proj_tasktype_id');
    $proj_tt_label = $obm_q->f('proj_tasktype_label');
    $time_before = round($obm_q->f('total_before'),3);
    $time = round($obm_q->f('total_length'),3);
    $time_after = round($obm_q->f('total_after'),3);
    // Tasktype_id : use project tasktype id is exists, else timetask one
    if ($proj_tt_id != '') {
      $tt_id = $proj_tt_id;
      $res[$tt_id]['label'] = $proj_tt_label;
      $res[$tt_id]['type'] = 'project';
    } else {
      $res[$tt_id]['label'] = $tt_label;
      $res[$tt_id]['type'] = 'tt_others';
    }
    $res[$tt_id]['time'] += $time;
    $res[$tt_id]['time_before'] += $time_before;
    $res[$tt_id]['time_after'] += $time_after;

    $res[0]['time_total'] += $time;
    $res[0]['time_before_total'] += $time_before;
    $res[0]['time_after_total'] += $time_after;
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// return array with filled days and status in the week containing time[date]
// Parameters:
//  - $time : array with defined variables
// Returns:
//  array of days corresponding to validated date, and task_status : 
//   ('20010911' => 2, '20010912' => 1, '20010913' => 1)
///////////////////////////////////////////////////////////////////////////////
function run_query_time_valid_search($time) {
  global $cdg_sql;

  $week_span = get_time_week_point($time['date']);

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $datetask = sql_date_format($db_type, 'timetask_date', 'date_task');

  $query = "SELECT 
      $datetask,
      timetask_status
    FROM TimeTask 
    WHERE timetask_status != 0 
      AND timetask_date >= '". $week_span[0] ."' 
      AND timetask_date <= '". $week_span[1]. "'";

  // user_id : array -> size 1 or more 
  if (sizeof($time['user_ids']) == 1) {
    $query .= " AND timetask_user_id = '". $time['user_id'] . "'";
  } else {
    $users_list = time_array_to_list($time['user_ids']);
    $query .= " AND timetask_user_id in $users_list";
  }

  // Group
  $query .= " GROUP BY timetask_date, timetask_status ";
  
  display_debug_msg($query, $cdg_sql, 'run_query_time_valid_search');
  $obm_q->query($query);
  
  while ($obm_q->next_record()) {
    $date_Ymd = date('Ymd',$obm_q->f('date_task'));
    $valid_array[$date_Ymd] = $obm_q->f('timetask_status');
  }
   
  return $valid_array; 
}


///////////////////////////////////////////////////////////////////////////////
// get all for task with $id id
// Parametes:
//   - $id : id for task
///////////////////////////////////////////////////////////////////////////////
function run_query_time_get_task($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $datetask = sql_date_format($db_type, 'timetask_date', 'date_task');

  $query = "
    SELECT
      TimeTask.*,
      $datetask,
      projecttask_project_id as timetask_project_id  
    FROM TimeTask
      LEFT JOIN ProjectTask ON timetask_projecttask_id = projecttask_id
    WHERE timetask_id = '$id'";
  
  display_debug_msg($query, $cdg_sql, 'run_query_time_get_task');
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Task Insertion query execution
// Parameters:
//   - $task[] : Entry's values
//     keys used  : tasktype, project, time, label, date
///////////////////////////////////////////////////////////////////////////////
function run_query_time_insert($task) {
  global $obm, $cdg_sql;

  $obm_q = new DB_OBM;
  $valid_date = run_query_time_valid_search($task);
  $day = $task['day'];
  $ttype = $task['tasktype'];
  $projecttask = ($task['projecttask'] == 0) ? 'null' : $obm_q->escape($task['projecttask']);
  $time = $task['time'];
  $label = $task['label'];
  $user_id = $task['user_id'];

  if ( $ttype != 0 ) {
    // for navigators wich doesn't support HTML 4 => <option disabled>
    if (is_array($day)) {
      foreach ($day as $key => $value) {
    $obm_q = new DB_OBM;
	$query = "
       INSERT INTO TimeTask (
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
       VALUES (
         null,
         '" . date('Y-m-d H:i:s') . "',
         null,
         '$obm[uid]',
         '$user_id',
         '".$obm_q->escape($key)."',
         $projecttask,
         '$time',
         '".$obm_q->escape($ttype)."',
         '".$obm_q->escape($label)."',
         0)";

	display_debug_msg($query, $cdg_sql, 'run_query_time_insert');
	$obm_q->query($query);  
      }
    } else {
      $obm_q = new DB_OBM;
      $query = "
       INSERT INTO TimeTask (
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
       VALUES (
         null,
         '" . date('Y-m-d H:i:s') . "',
         null,
         '$obm[uid]',
         '$user_id',
         '$date',
         $projecttask,
         '$time',
         '".$obm_q->escape($ttype)."',
         '".$obm_q->escape($label)."',
         0)";

       display_debug_msg($query, $cdg_sql, 'run_query_time_insert');
       $obm_q->query($query);  
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Task modification query execution
// Parameters:
//   - $task[] : Entry's values
//     keys used  : tasktype, project, time, label
///////////////////////////////////////////////////////////////////////////////
function run_query_time_update($task) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $id = $task['task_id'];
  $ttype = $task['tasktype'];
  $projecttask = sql_parse_id($obm_q->escape($task['projecttask']));
  $time = $task['time'];
  $label = $task['label'];
  $day = $task['day'];
  
  $query = "
  UPDATE TimeTask SET
    timetask_date='$day',
    timetask_tasktype_id='".$obm_q->escape($ttype)."',
    timetask_length='$time',
    timetask_label='".$obm_q->escape($label)."',
    timetask_projecttask_id = $projecttask
  WHERE timetask_id='$id'";

  display_debug_msg($query, $cdg_sql, 'run_query_time_update');
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Delete task given in $params
// Parameters:
//   - $parms : parameters hash
///////////////////////////////////////////////////////////////////////////////
function run_query_time_delete($parms) {
  global $cdg_sql;

  foreach($parms as $key => $val) {
    $id_task = strstr($key,'_'); 
    if ( $id_task != '' ) {  // if we have an id task
      $id_task = substr($id_task, 1);  
    
      if ($val == 'on') { 
        $query = "DELETE FROM TimeTask WHERE timetask_id = '$id_task'";
	display_debug_msg($query, $cdg_sql, 'run_query_time_delete');
        $obm_q = new DB_OBM;
        $obm_q->query($query);
      }    
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Task Validation
// Parameters:
//   - $time[]    :    time search criteria
//     keys used
///////////////////////////////////////////////////////////////////////////////
function run_query_time_validate($who) {
  global $cdg_sql, $c_day_fraction;

  // to see if a day is full
  $cdf = $c_day_fraction;

  // look for days that can be validated
  // an array is made with the result
  // should use subselect (mysql 4.1 and further)
  $query = "select timetask_date
            from TimeTask
            where timetask_user_id = '$who'
            and timetask_status = 0
            group by timetask_date
            having sum(timetask_length) = '$cdf'";
  
  display_debug_msg($query, $cdg_sql, 'run_query_time_validate(1)');
  $search_valid_query = new DB_OBM;
  $search_valid_query->query($query);

  if ($search_valid_query->nf() != 0) {
    
    $search_valid_query->next_record();
    $date = $search_valid_query->f('timetask_date');
    $date_list = "('$date'";

    while($search_valid_query->next_record()) {
      $date = $search_valid_query->f('timetask_date');
      $date_list .= ", '$date'";
    }

    $date_list .= ')';

    // validate dates that are fully filled
    $query = "update TimeTask
              set timetask_status = 1
              where timetask_user_id=$who
              and timetask_date in $date_list";
    
    display_debug_msg($query, $cdg_sql, 'run_query_time_validate(2)');
    $valid_query = new DB_OBM;
    $valid_query->query($query);
  }

  // look for days that are not supposed to be validated
  // ie. if a task has been updated...
  $query = "select timetask_date
            from TimeTask
            where timetask_user_id = '$who'
            and timetask_status != 2
            group by timetask_date
            having sum(timetask_length) != '$cdf'";
  
  display_debug_msg($query, $cdg_sql, 'run_query_time_validate(3)');
  $search_unvalid_query = new DB_OBM;
  $search_unvalid_query->query($query);

  if ($search_unvalid_query->nf() != 0) {
    
    $search_unvalid_query->next_record();
    $date = $search_unvalid_query->f('timetask_date');
    $date_list = "('$date'";

    while($search_unvalid_query->next_record()) {
      $date = $search_unvalid_query->f('timetask_date');
      $date_list .= ", '$date'";
    }

    $date_list .= ')';

    // validate dates that are fully filled
    $query = "update TimeTask
              set timetask_status = 0
              where timetask_user_id = '$who'
              and timetask_date in $date_list";
    
    display_debug_msg($query, $cdg_sql, 'run_query_time_validate(4)');
    $unvalid_query = new DB_OBM;
    $unvalid_query->query($query);
  }
 
}


///////////////////////////////////////////////////////////////////////////////
// Task Validation by the Admin (once each month)
// Parameters:
//   - $time[]    :    time search criteria
//     keys used       date, user_id
///////////////////////////////////////////////////////////////////////////////
function run_query_time_adminvalidate($time) {
  global $cdg_sql, $cdg_param;

  $iso_date = $time['date'];
  $ts_date = strtotime($iso_date);
  $month = of_date_get_month($ts_date);
  $year = of_date_get_year($ts_date);
  $day = 1;

  // First and last days of month
  $month_begin = of_isodate_format(mktime(0,0,0,$month, 1, $year));
  $month_end = of_isodate_format(mktime(23,0,0,$month+1, $day-1, $year));

  $user_id = $time['user_id'];

  $query = "
    update TimeTask
    set timetask_status = 2
    where timetask_user_id = '$user_id'
      and timetask_date >= '$month_begin' 
      and timetask_date <= '$month_end'";

  display_debug_msg($query, $cdg_sql, 'run_query_time_adminvalidate');
  $obm_db = new DB_OBM;
  $obm_db->query($query);

  return $obm_db; 
}


///////////////////////////////////////////////////////////////////////////////
// Task Validation Cancelling for the admin
// Parameters:
//   - $time[]    :    time search criteria
//     keys used       date, user_id
///////////////////////////////////////////////////////////////////////////////
function run_query_time_adminunvalidate($time) {
  global $cdg_sql, $cdg_param;

  $iso_date = $time['date'];
  $ts_date = strtotime($iso_date);
  $month = of_date_get_month($ts_date);
  $year = of_date_get_year($ts_date);
  $day = 1;

  // First and last days of month
  $month_begin = of_isodate_format(mktime(0,0,0,$month, 1, $year));
  $month_end = of_isodate_format(mktime(23,0,0,$month+1, $day-1, $year));

  $user_id = $time['user_id'];

  $query = "
    update TimeTask
    set timetask_status = 1
    where timetask_user_id = '$user_id'
      and timetask_date >= '$month_begin' 
      and timetask_date <= '$month_end'";

  display_debug_msg($query, $cdg_sql, 'run_query_time_adminvalidate');
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
function run_query_time_short_month($time) {
  global $cdg_sql, $cdg_param;

  $iso_date = $time['date'];
  $ts_date = strtotime($iso_date);
  $month = of_date_get_month($ts_date);
  $year = of_date_get_year($ts_date);
  $day = 1;

  // First and last days of month
  $month_begin = of_isodate_format(mktime(0,0,0,$month, 1, $year));
  $month_end = of_isodate_format(mktime(23,0,0,$month+1, $day-1, $year));

  if ($time['order'] == 'activity') {
    $order = 'total_length';
  } else {
    $order = 'userobm_lastname, userobm_firstname';
  }

  // Get users that have tasks filled in the period
  $query = "
    SELECT DISTINCT
      timetask_user_id as user_id
    FROM TimeTask
    WHERE timetask_date >= '$month_begin' 
      AND timetask_date <= '$month_end'
";
  display_debug_msg($query, $cdg_sql, 'run_query_time_short_month(1)');
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $coma = '';
  $not_in = '0';
  while ($obm_db->next_record()) {
    $id = $obm_db->f('user_id');
    $not_in .= ",$id";
    $coma = ',';
  }

  // Get tasks filled with associated users
  // and active users that have no tasks filled in the period
  $query = "
    SELECT
      userobm_lastname,
      userobm_firstname,
      sum(timetask_length) as total_length,
      timetask_user_id as userid,
      min(timetask_status) as status
    FROM TimeTask, UserObm
    WHERE timetask_date >= '$month_begin' 
      AND timetask_date <= '$month_end'
      AND timetask_user_id = userobm_id
    GROUP BY userobm_lastname,
             userobm_firstname,
             timetask_user_id
  UNION
    SELECT
      userobm_lastname,
      userobm_firstname,
      0 as total_length,
      userobm_id as userid,
      0 as status
    FROM UserObm
    WHERE userobm_archive='0'
      AND userobm_id NOT IN ($not_in)
      AND userobm_datebegin <= '$month_end'
    ORDER BY $order
";
  
  display_debug_msg($query, $cdg_sql, 'run_query_time_short_month(2)');
  $obm_db = new DB_OBM;
  $obm_db->query($query);

  return $obm_db; 
}


///////////////////////////////////////////////////////////////////////////////
// Timemanager: qet tasks for given users and month                 
// Parameters:
//   - $time[] : time search criteria
// Returns : DB object result
///////////////////////////////////////////////////////////////////////////////
function run_query_time_user_month_planning($time) {
  global $cdg_sql, $cdg_param;

  $iso_date = $time['date'];
  $ts_date = strtotime($iso_date);
  $month = of_date_get_month($ts_date);
  $year = of_date_get_year($ts_date);
  $day = 1;

  // First and last days of month
  $month_begin = of_isodate_format(mktime(0,0,0,$month, 1, $year));
  $month_end = of_isodate_format(mktime(23,0,0,$month+1, $day-1, $year));

  if ((is_array($time['user_ids'])) && (count($time['user_ids']) > 0)) {
    $uid_tab = '('.implode(',',$time['user_ids']).')';
  } else {
    $uid_tab = '(0)';
  }

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $date = sql_date_format($db_type, 'timetask_date', 'date');

  $query = "
    SELECT
      project_name as label,
      project_shortname as shortname,
      project_id as id,
      sum(timetask_length) as timetask_length,
      $date,
      userobm_id,
      userobm_lastname,
      userobm_firstname,
      timetask_status,
      'project' as type
    FROM TimeTask JOIN UserObm ON timetask_user_id = userobm_id
    JOIN ProjectTask ON projecttask_id = timetask_projecttask_id
    JOIN Project ON projecttask_project_id = project_id
    WHERE timetask_date >= '$month_begin' 
      AND timetask_date <= '$month_end'
      AND timetask_user_id IN $uid_tab
    GROUP BY date, timetask_status, id, label,userobm_id,userobm_lastname,userobm_firstname,type, project_shortname, project_name, project_id
    UNION
    SELECT
      tasktype_label as label,
      tasktype_label as shortname,
      timetask_tasktype_id as id,
      sum(timetask_length) as timetask_length,
      $date,
      userobm_id,
      userobm_lastname,
      userobm_firstname,
      timetask_status,
      'task' as type
    FROM TimeTask JOIN UserObm ON  timetask_user_id = userobm_id
    LEFT JOIN ProjectTask ON projecttask_id = timetask_projecttask_id 
    JOIN TaskType ON tasktype_id = timetask_tasktype_id
    WHERE projecttask_id IS NULL
      AND timetask_date >= '$month_begin' 
      AND timetask_date <= '$month_end'
      AND timetask_user_id IN $uid_tab
    GROUP BY  date, timetask_status, id, label,userobm_id,userobm_lastname,userobm_firstname,type
    ORDER BY userobm_lastname, type, label
";
  
  display_debug_msg($query, $cdg_sql, 'run_query_time_user_month_planning');
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
function run_query_time_task_one_week($time) {
  global $cdg_sql, $cdg_param;

  $week = get_time_week_point($time['date']);

  $user_id = $time['user_id'];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $date = sql_date_format($db_type, 'timetask_date', 'date');

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

  display_debug_msg($query, $cdg_sql, 'run_query_time_task_one_week');
  $obm_q->query($query);

  return $obm_q;
}


/**
 * Time date form check
 * @param mixed $params
 * @return true or false
 **/
function check_time_date_form($params) {
  global $php_regexp_isodate, $l_invalid_date, $l_begin, $l_end;
  global $err;

  $date = of_isodate_convert($params['date'], true);
  // Date check
  if (($date != '') && (preg_match($php_regexp_isodate, $date) == 0)) {
    $err['msg'] = "$l_date : $l_invalid_date";
    return false;
  }

  $date_end = of_isodate_convert($params['date_end'],true);
  // End Date check
  if (($date_end != '') && (preg_match($php_regexp_isodate, $date_end) == 0)) {
    $err['msg'] = "$l_date $l_end : $l_invalid_date";
    return false;
  }

  if (($date != '') && ($date_end != '')) {
    if ($date >= $date_end) {
      $err['msg'] = "$l_invalid_date : $l_begin > $l_end";
      return $false;
    }
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Timemanager: Projects for pdf generation
// Returns : DB object result with project satisfying criteria
///////////////////////////////////////////////////////////////////////////////
function run_query_time_ra_projects($time) {
  global $cdg_sql;

  $uid = $time['user_id'];
  $projs = implode(',', $time['projects']);

  $query = "
    SELECT distinct
      project_id,
      project_company_id,
      project_name,
      project_shortname,
      company_name,
      project_tasktype_id
    FROM Project
      LEFT JOIN Company ON company_id = project_company_id,
      ProjectTask, ProjectUser
    WHERE project_archive='0'
      AND projectuser_projecttask_id = projecttask_id
      AND projecttask_project_id = project_id
      AND projectuser_user_id = '$uid'
      AND projectuser_missingtime != '0'
      AND project_id IN ($projs)
      ORDER BY project_id, project_tasktype_id, company_name, project_name";

  display_debug_msg($query, $cdg_sql, 'run_query_time_project');
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Timemanager: TaskType select query execution
// Returns : DB object result with all type of task
///////////////////////////////////////////////////////////////////////////////
function run_query_time_used_tasktype($uid = 0) {
  global $cdg_sql, $ctt_sales, $ctt_research, $ctt_others;

  $multidomain = sql_multidomain('tasktype');

  if ($uid) {
    $query = "
      SELECT DISTINCT TaskType.*
      FROM TaskType, Project, ProjectUser
      WHERE tasktype_id = project_tasktype_id
        AND project_id = projectuser_project_id
        AND project_archive = '0'
        AND projectuser_user_id = '$uid'
        AND projectuser_projecttask_id is not null
      ORDER BY tasktype_internal, tasktype_label";

    display_debug_msg($query, $cdg_sql, 'run_query_time_used_tasktype');
    $obm1_q= new DB_OBM;
    $obm1_q->query($query);

    $query = "
      SELECT TaskType.*
      FROM TaskType
      WHERE tasktype_internal='$ctt_others' $multidomain
      ORDER BY tasktype_internal, tasktype_label";

    display_debug_msg($query, $cdg_sql, 'run_query_time_used_tasktype');
    $obm2_q= new DB_OBM;
    $obm2_q->query($query);

    $obm_q = Array($obm1_q, $obm2_q);

  } else {
    $query = "
      SELECT *
      FROM TaskType
      WHERE 1=1 $multidomain
      ORDER BY tasktype_internal, tasktype_label";

    display_debug_msg($query, $cdg_sql, 'run_query_time_used_tasktype');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Timemanager: Project select query execution
// Returns : DB object result with project satisfying criteria
///////////////////////////////////////////////////////////////////////////////
function run_query_time_project($time) {
  global $cdg_sql;

  $uid = $time['user_id'];

  $query = "
    SELECT distinct
      project_id,
      project_company_id,
      project_name,
      project_shortname,
      company_name,
      project_tasktype_id
    FROM Project
      LEFT JOIN Company ON company_id = project_company_id,
      ProjectTask, ProjectUser
    WHERE project_archive='0'
      AND projectuser_projecttask_id = projecttask_id
      AND projecttask_project_id = project_id
      AND projectuser_user_id = '$uid'
      AND projectuser_missingtime != '0'
      ORDER BY project_tasktype_id, project_shortname, company_name, project_name";

  display_debug_msg($query, $cdg_sql, 'run_query_time_project');
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Timemanager: ProjectTask select query execution                           //
// Returns : DB object result with project satisfying criteria               //
// This function does return the task_label, and, in case a parenttask_label //
//   exists, it returns 'parenttask_label --- task_label'.                   //
//  Used for update and insert task form                                     //
///////////////////////////////////////////////////////////////////////////////
function run_query_time_projecttask($time) {
  global $cdg_sql;

  $uid = $time['user_id'];

  $obm_q= new DB_OBM;
  $db_type = $obm_q->type;
  // array of the sql concataneted strings
  $a_concat[]=array('type'=>'field', 'value'=>'b.projecttask_label');
  $a_concat[]=array('type'=>'string', 'value'=>' --- ');
  $a_concat[]=array('type'=>'field', 'value'=>'a.projecttask_label');
  $concat = sql_string_concat($db_type, $a_concat);
  display_debug_msg($concat, $cdg_sql, "run_query_time_projecttask : $db_type concat : ");
  $sql_if = sql_if($db_type, 'a.projecttask_parenttask_id IS  NULL', 'a.projecttask_label', $concat);

  $query = "
    SELECT
      a.projecttask_id,
      $sql_if as projecttask_label,
      a.projecttask_project_id
    FROM ProjectTask a left join ProjectTask b 
       ON a.projecttask_parenttask_id = b.projecttask_id,
       ProjectUser
    WHERE projectuser_projecttask_id = a.projecttask_id
      AND projectuser_user_id = $uid
      AND projectuser_missingtime != 0
      ORDER BY a.projecttask_project_id, a.projecttask_parenttask_id, a.projecttask_label";

  display_debug_msg($query, $cdg_sql, 'run_query_time_projecttask');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get an array of working days date for the week begining at the date given
// Paramaters:
//   - $date : a date (timestamp) starting on the 1st day of the week
// Returns:
//  array with for each days of the week : 0->'Mon 23 Apr' 1->20040413
///////////////////////////////////////////////////////////////////////////////
function get_time_week_working_days_dates($date) {
  global $c_week_first_day, $l_daysofweekshort, $l_monthsofyearshort;
  
  $cpt_end = $c_week_first_day + 7;
  for ($i=$c_week_first_day; $i < $cpt_end; $i++) {
    if (time_is_working_day($date)) {
      $date_link = date('Ymd', $date);
      $month = date('m', $date);
      $day = date('d', $date);
      $dis_date = $l_daysofweekshort[$i%7] ." $day " . $l_monthsofyearshort[$month - 1];
      
      $array_date[$i][0] = $dis_date;
      $array_date[$i][1] = $date_link;
    }

    $date += 86400;
  }
  return $array_date;
}


/**
 * Get the starting and ending date of a week
 *
 * @param mixed $date date in iso format
 * @return array ($weekstart, $weekend) in iso format
 */
function get_time_week_point($date) {

  if (is_null($date)) {
    $p_date = of_isodate_format();
  } else {
    $p_date = $date;
  }

  $week_first_day = of_date_get_first_day_week($p_date);
  // beginning and end of week (Ymd format)
  $weekstart = of_isodate_format($week_first_day);
  $weekend = of_isodate_format($week_first_day + 6 * 86400);

  return array($weekstart, $weekend);
}


/////////////////////////////////////////////////////////////////////////////
// Return a string list of the elements of the array
//Parameters :
// - $array = any array 
///////////////////////////////////////////////////////////////////////////
function time_array_to_list($array) {
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
  $list .= ')';

  return $list;
}


///////////////////////////////////////////////////////////////////////////////
// Test if the given day is an 'Open days' (jours ouvrables)
// Parameters :
// - $date = a date in timestamp format 
// Return :
//  true or false
///////////////////////////////////////////////////////////////////////////////
function time_is_working_day($date) {
  global $c_working_days;

  $wday = date('w', $date);

  if ($c_working_days[$wday] == 1) 
    return true;
  else
    return false;
}


/**
 * Return number of 'working days' (jours ouvrables) in a month
 *
 * @param mixed $iso_date date in iso format in the month
 * @return integer $workingdays : number of working days in the month
 */
function get_time_nb_working_days($iso_date) {

  $ts_date = strtotime($iso_date);
  $month = of_date_get_month($ts_date);
  $year = of_date_get_year($ts_date);
  $day = 1;

  // First and last days of month
  $ts_month_begin = mktime(12,0,0,$month, 1, $year);
  $ts_next_month = mktime(0,0,0,$month+1, 1, $year);

  $date = $ts_month_begin;
  $workingdays = 0;
  while ($date < $ts_next_month) {
    if (time_is_working_day($date)) {
      $workingdays++;
    }
    $date += 86400;
  }

  return $workingdays;
}


/**
 * Active Userobm + users that have reported activity in the time range
 *
 * @param mixed $params parameters hash
 * @return user array
 */
function get_time_active_user($params) {
  global $cdg_sql;

  $multidomain = sql_multidomain('userobm');

  $iso_date = of_isodate_convert($params['date']);
  $ts_date = strtotime($iso_date);

  $month = of_date_get_month($ts_date);
  $year = of_date_get_year($ts_date);
  $ts_first_day_month = mktime(0,0,0,$month,1,$year);
  $date_begin = of_isodate_format($ts_first_day_month);
  $ts_last_day_month = mktime(0,0,0,$month+1,0,$year);
  $date_end = of_isodate_format($ts_last_day_month);

  $query = "SELECT distinct userobm_id,
      userobm_lastname,
      userobm_firstname
    FROM TimeTask
      LEFT JOIN UserObm on timetask_user_id=userobm_id
    WHERE timetask_date <= '$date_end' AND timetask_date >= '$date_begin'
    $multidomain
    ORDER BY userobm_lastname"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'get_time_active_user()');

  return $obm_q;
}


?>
