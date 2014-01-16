<script language="php">
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
// OBM - File : planning_query.inc                                           //
//     - Desc : Planning query File                                          //
// 2008-10-14 Guillaume Lardon                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Get tasks for given users and month                 
// Parameters:
//   - $time[] : time search criteria
// Returns : DB object result
///////////////////////////////////////////////////////////////////////////////
function run_query_planning_month($time) {
  global $cdg_sql, $cdg_param;
  $iso_date = $time['date'];
  $ts_date = strtotime($iso_date);
  $month = of_date_get_month($ts_date);
  $year = of_date_get_year($ts_date);
  $day = 1;

  // First and last days of month
  $month_begin = of_isodate_format(mktime(0,0,0,$month, 1, $year));
  $month_end = of_isodate_format(mktime(23,0,0,$month+1, $day-1, $year));

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
 
  $datebegin = sql_date_format($db_type, 'plannedtask_datebegin', 'datebegin');
  $dateend = sql_date_format($db_type, 'plannedtask_dateend', 'dateend');

  $query = "
    SELECT
      plannedtask_id,
      #CONCAT(project_name,' - ',company_name) as label,
      project_shortname as shortname,
      project_id as id,
      project_tasktype_id as tasktype_id,
      tasktype_tasktypegroup_id,
      plannedtask_user_id,
      plannedtask_overrun,
      $datebegin,
      $dateend,
      plannedtask_period,
      project_type_id,
      'project' as type
    FROM PlannedTask
    JOIN Project ON plannedtask_project_id = project_id
    JOIN TaskType ON tasktype_id = project_tasktype_id
    LEFT JOIN Company on project_company_id = company_id
    WHERE plannedtask_datebegin >= '$month_begin' 
      AND plannedtask_dateend <= '$month_end'
    UNION
    SELECT
      plannedtask_id,
      tasktype_label as label,
      tasktype_code as shortname,
      plannedtask_tasktype_id as id,
      tasktype_id,
      tasktype_tasktypegroup_id,
      plannedtask_user_id,
      plannedtask_overrun,
      $datebegin,
      $dateend,
      plannedtask_period,
      null,
      'task' as type
    FROM PlannedTask
    JOIN TaskType ON tasktype_id = plannedtask_tasktype_id
    WHERE plannedtask_datebegin >= '$month_begin' 
      AND plannedtask_dateend <= '$month_end'";
  
  display_debug_msg($query, $cdg_sql, 'run_query_planning_month');
  $obm_db = new DB_OBM;
  $obm_db->query($query);

  return $obm_db; 
}

/**
 * Projects Search query execution 
 *
 * @param mixed $user : user search criteria
 * @return database object with search result
 */
function run_query_plannedtask_projects($user,$project=null)
{
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain('project');

  $query = "SELECT distinct project_id,project_name,company_name
    FROM Project
    JOIN ProjectUser on project_id=projectuser_project_id
    LEFT JOIN Company on project_company_id=company_id
    WHERE projectuser_projectedtime != '0' $multidomain
    AND projectuser_user_id=$user
    AND project_archive=0";
  if($project)
  $query.=" OR project_id=$project";
  $query.="
    ORDER BY project_name";
  display_debug_msg($query, $cdg_sql, 'run_query_plannedtask_projects()');
  $obm_q->query($query) ;
  return $obm_q;
}

/**
 * Public groups list query execution
 * @return database object with search result
 **/
function run_query_planning_groups() {
  global $cdg_sql,$obm;
  global $cplanning_groups;

  $query = "SELECT group_id,group_name
    FROM UGroup
    WHERE (group_privacy='0' or (group_privacy='1' and group_usercreate=".$obm['uid'].") )";
  if(is_array($cplanning_groups))
  $query.= " AND group_id in (".implode(",",$cplanning_groups).")";
  $query.= " order by group_name";
  display_debug_msg($query, $cdg_sql, 'run_query_planning_groups()');
  $obm_q = new DB_OBM;

  $obm_q->query($query);
  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Query execution : TaskType Group list
///////////////////////////////////////////////////////////////////////////////
function run_query_tasktypegroup_list() {
  global $cdg_sql;

  $multidomain = sql_multidomain('tasktypegroup');

  $query = "SELECT tasktypegroup_id,
    tasktypegroup_label,
    tasktypegroup_code,
    tasktypegroup_bgcolor,
    tasktypegroup_fgcolor
  FROM TaskTypeGroup WHERE 1=1 $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_tasktypegroup_list()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


/**
 * Get task groups
 * @return array of tasktypegroups datas
 **/
function get_tasktypegroups_infos()
{
  $ttg_q = run_query_tasktypegroup_list();
  $ttg = array();
  while($ttg_q->next_record()) {
    $id = $ttg_q->f('tasktypegroup_id');
    $ttg[$id]['label'] = $ttg_q->f('tasktypegroup_label');
    $ttg[$id]['code'] = $ttg_q->f('tasktypegroup_code');
    $ttg[$id]['bgcolor'] = $ttg_q->f('tasktypegroup_bgcolor');
    $ttg[$id]['fgcolor'] = $ttg_q->f('tasktypegroup_fgcolor');
  }
  return $ttg;
}

/**
 * Planning users list query execution 
 *
 * @param mixed $users : array of user ids
 * @return database object with search result
 */
function run_query_planning_get_usernames($users)
{
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain('planninguser');

  $query = "SELECT userobm_id,userobm_lastname,userobm_firstname
    FROM UserObm
    WHERE userobm_id in (".join($users,",").")
    ORDER BY userobm_lastname";
  display_debug_msg($query, $cdg_sql, 'run_query_get_usernames()');
  $obm_q->query($query) ;
  return $obm_q;
}

/**
 * Task Search query execution 
 *
 * @return database object with search result
 */
function run_query_plannedtask_tasks($internal_cond="1=1")
{
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain('tasktype');
  $query = "SELECT tasktype_id,tasktype_label,tasktype_tasktypegroup_id FROM TaskType WHERE $internal_cond $multidomain order by tasktype_label";
  display_debug_msg($query, $cdg_sql, 'run_query_plannedtask_task()');
  $obm_q->query($query) ;
  return $obm_q;
}

/**
 * Planning detail query execution
 *
 * @param mixed $id : plannedtask id
 * @return database object with plannedtask info
 */
function run_query_plannedtask_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $datebegin = sql_date_format($db_type, 'plannedtask_datebegin', 'datebegin');
  $dateend = sql_date_format($db_type, 'plannedtask_dateend', 'dateend');
  $timeupdate = sql_date_format($db_type, 'plannedtask_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'plannedtask_timecreate', 'timecreate');
  $multidomain = sql_multidomain('plannedtask');

  $query = "SELECT plannedtask_id,
      plannedtask_user_id,
      $timeupdate,
      $timecreate,
      $datebegin,
      $dateend,
      plannedtask_period,
      plannedtask_project_id,
      plannedtask_tasktype_id,
      plannedtask_overrun,
      plannedtask_comment,
      project_name,
      tasktype_label,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM PlannedTask
      LEFT JOIN Project on project_id=plannedtask_project_id
      LEFT JOIN TaskType on tasktype_id=plannedtask_tasktype_id
      LEFT JOIN UserObm as c ON plannedtask_usercreate=c.userobm_id
      LEFT JOIN UserObm as u ON plannedtask_userupdate=u.userobm_id
    WHERE plannedtask_id = '$id'
      $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_planning_detail()');
  $obm_q->query($query) ;
  $obm_q->next_record();

  return $obm_q;
}

function add_week_item(&$parts,$date1,$date2)
{
/*
---
  $ts_date = strtotime($iso_date);

  // first day of the month in second
  $first_day_sec = mktime(0,0,0,$month, 1, $year);
  $last_day_sec = mktime(23,0,0,$month+1, 0, $year);
  $day_in_sec = $first_day_sec;
  $dow = date('w', $day_in_sec);
  $obm_dow = ($dow - $c_week_first_day + 7) % 7;
----
*/
  $month1 = of_date_get_month($date1);
  $month2 = of_date_get_month($date2);

  if($month1 == $month2)
  $parts[] = array(date('Y-m-d',$date1),date('Y-m-d',$date2));
  else
  {
    $year1 = of_date_get_year($date1);
    $year2 = of_date_get_year($date2);
    $date4 = $date2;
    $date2 = mktime(23,0,0,$month2, 0, $year2);
    $date3 = mktime(0,0,0,$month2, 1, $year2);

    $parts[] = array(date('Y-m-d',$date1),date('Y-m-d',$date2));
    $parts[] = array(date('Y-m-d',$date3),date('Y-m-d',$date4));

  }

}

/**
 * Separate interval of 2 dates in different weeks
 *
 * @param $datebegin, $dateend
 * @return array of week date couples
 */
function separate_week_items($datebegin,$dateend)
{
  $result = array();

  $first_day_sec = strtotime($datebegin);
  $last_day_sec = strtotime($dateend);
  $d1 = $first_day_sec;
 
  
  // TODO : potentiel bug lors de jours de changement d'heure
  for($d = $first_day_sec ; $d<$last_day_sec;$d+=86400)
  {
    $dow = date('w', $d);
    if($dow == 5)
    {
      add_week_item($result,$d1,$d);
      $d1 = null;
    }
    else if($dow == 1)
    $d1 = $d;
  }

  if($d1)
  add_week_item($result,$d1,$d);

  return $result;
}

/**
 * Query execution : plannedtask insertion
 *
 * @param mixed $plannedtask : plannedtask hash info
 * @return true if insert ok
 */
function run_query_plannedtask_insert($plannedtask) {
  global $obm, $cdg_sql;

  $obm_q = new DB_OBM;
  $domain_id = $obm['domain_id'];
  $id = $plannedtask['id'];
  $project = $plannedtask['project']!=0?"'".$plannedtask['project']."'":'null';
  $task = $plannedtask['task']!=0?"'".$plannedtask['task']."'":'null';
  $overrun = ($plannedtask['overrun']=='1'?'TRUE':'FALSE');
  $comment = $plannedtask['comment'];
  $user = $plannedtask['user'];
  $datebegin = of_isodate_convert($plannedtask['datebegin']);
  $dateend = of_isodate_convert($plannedtask['dateend']);
  $period = $plannedtask['period'];
  $now = date('Y-m-d H:i:s');
  $multidomain = sql_multidomain('plannedtask');

  // Separe l'interval en plusieurs semaines si nécessaire
  $weeks = separate_week_items($datebegin,$dateend);
 
  foreach ($weeks as $dw)
  {
    $datebegin = $dw[0];
    $dateend = $dw[1];

    $query = "INSERT INTO PlannedTask (plannedtask_timeupdate,
      plannedtask_timecreate,
      plannedtask_userupdate,
      plannedtask_usercreate,
      plannedtask_domain_id,
      plannedtask_project_id,
      plannedtask_tasktype_id,
      plannedtask_overrun,
      plannedtask_comment,
      plannedtask_user_id,
      plannedtask_datebegin,
      plannedtask_dateend,
      plannedtask_period)
  VALUES (null,
    '$now',
    null,
    '$obm[uid]',
    '$domain_id',
    $project,
    $task,
    $overrun,
    '".$obm_q->escape($comment)."',
    '".$obm_q->escape($user)."',
    '$datebegin',
    '$dateend',
    '$period')";

    display_debug_msg($query, $cdg_sql, 'run_query_plannedtask_insert');
    $return = $obm_q->query($query);
    pt_assert($return,__FILE__,__LINE__,"run_query_plannedtask_insert");
  }
  return $return;
}


/**
 * PlannedTask Update query execution
 *
 * @param mixed $plannedtask : plannedtask hash info
 * @return true is update OK
 */
function run_query_plannedtask_update($plannedtask) {
  global $obm, $cdg_sql;

  $obm_q = new DB_OBM;
  $id = $plannedtask['id'];
  $project = $plannedtask['project']!=0?$plannedtask['project']:'null';
  $task = $plannedtask['task']!=0?$plannedtask['task']:'null';
  $overrun = ($plannedtask['overrun']=='1'?'TRUE':'FALSE');
  $comment = $plannedtask['comment'];
  $user = $plannedtask['user'];
  $datebegin = of_isodate_convert($plannedtask['datebegin']);
  $dateend = of_isodate_convert($plannedtask['dateend']);
  $period = $plannedtask['period'];
  $now = date('Y-m-d H:i:s');
  $multidomain = sql_multidomain('plannedtask');
 
  $query = "UPDATE PlannedTask SET
    plannedtask_timeupdate='$now',
    plannedtask_userupdate='$obm[uid]',
    plannedtask_project_id=$project,
    plannedtask_tasktype_id=$task,
    plannedtask_overrun=$overrun,
    plannedtask_comment='".$obm_q->escape($comment)."',
    plannedtask_user_id='".$obm_q->escape($user)."',
    plannedtask_datebegin='$datebegin',
    plannedtask_dateend='$dateend',
    plannedtask_period='$period'
  WHERE plannedtask_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_plannedtask_update()');
  $return = $obm_q->query($query);
  pt_assert($return,__FILE__,__LINE__,"run_query_plannedtask_update");

  return $return;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : plannedtask id
///////////////////////////////////////////////////////////////////////////////
function run_query_plannedtask_delete($p_id) {
  global $obm, $cdg_sql;

  // Delete the planned task
  $multidomain = sql_multidomain('plannedtask');
  $query = "DELETE FROM PlannedTask WHERE plannedtask_id='$p_id' $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_plannedtask_delete()');
  $obm_q = new DB_OBM;
  return $obm_q->query($query);
}

/**
 * check_planning_update_rights
 * Planning specific rights checking (eg: delegation)
 *
 * @param array $params paramaters hash
 * @return bool true or false
 */
function check_planning_update_rights($params) {

  return true;
}

///////////////////////////////////////////////////////////////////////////////
// PlannedTask Form Data checking and formatting
// Parameters:
//   - $plannedtask[]  : values checked
//     keys used : name, desc, email
///////////////////////////////////////////////////////////////////////////////
function check_plannedtask_data_form($plannedtask,&$conflict_id) {
  global $l_err_plannedtask_activity_empty,$l_err_plannedtask_activity_one;
  global $l_err_plannedtask_user_empty;
  global $l_err_plannedtask_datebegin_empty,$l_err_plannedtask_dateend_empty;
  global $l_err_plannedtask_dateinverted,$l_err_plannedtask_detected_conflict;
  global $cgp_use, $err;

  $id = $plannedtask['id'];
  $project = $plannedtask['project'];
  $task = $plannedtask['task'];
  $user = $plannedtask['user'];
  $datebegin = of_isodate_convert($plannedtask['datebegin']);
  $dateend = of_isodate_convert($plannedtask['dateend']);
  $period = $plannedtask['period'];
  
  // MANDATORY: activity selected
  if ($project == '' && $task == '' ) {
    $err['msg'] = $l_err_plannedtask_activity_empty;
    $err['field'] = 'activity'; // TODO : ce champ n'existe pas vraiment
    return false;
  }
  // MANDATORY: only 1 activity selected
  if ($project != '' && $task != '' ) {
    $err['msg'] = $l_err_plannedtask_activity_one;
    $err['field'] = 'activity'; // TODO : ce champ n'existe pas vraiment
    return false;
  }
  // MANDATORY: user not empty
  if ($user == '') {
    $err['msg'] = $l_err_plannedtask_user_empty;
    $err['field'] = 'user';
    return false;
  }
  // MANDATORY: date begin not empty
  if ($datebegin == '') {
    $err['msg'] = $l_err_plannedtask_datebegin_empty;
    $err['field'] = 'datebegin';
    return false;
  }
  // MANDATORY: date end not empty
  if ($datebegin == '') {
    $err['msg'] = $l_err_plannedtask_dateend_empty;
    $err['field'] = 'dateend';
    return false;
  }

  // Check if begin date is not after end
  if (strtotime($datebegin)>strtotime($dateend)) {
    $err['msg'] = $l_err_plannedtask_dateinverted;
    return false;
  }

  // Recherche de conflits
  // A = evenement a inserer ou mettre a jour
  // B = evenement deja existant pouvait faire conflit
  // Conflit si :
  // il existe un evenement dont 
  // - dA<=dB && fA>=dB
  //    <---- A ------->
  //       <---- B ---->
  //  - dA>=dB && dA<=fB
  //        <---- A ---->
  //    <---- B -------->
  $obm_q = new DB_OBM;
  $query = "select plannedtask_id from PlannedTask where ( ('$datebegin'<=plannedtask_datebegin AND '$dateend'>=plannedtask_datebegin) OR ('$datebegin'>=plannedtask_datebegin AND '$datebegin'<=plannedtask_dateend) ) AND plannedtask_user_id=$user";
  
  // Si c'est une Maj, on ne tient pas compte de l'evt qu'on mets a jour
  if($id)
  $query .= " AND plannedtask_id!=$id";

  // Si insertion d'une demi journee, alors on ne cherche
  // que les elements de cette periode ou de la journee
  if($period==1)
  $query .= " AND (plannedtask_period=1 OR plannedtask_period=0)";
  else if($period==2)
  $query .= " AND (plannedtask_period=2 OR plannedtask_period=0)";

  $obm_q->query($query);
  if($obm_q->next_record())
  {
    $conflict_id = $obm_q->f('plannedtask_id');
    $err['msg'] = $l_err_plannedtask_detected_conflict;
    return false;
  }
  
  return true;
}

// Recuperation des noms des groupes d'utilisateurs
function get_planning_groups($group_ids)
{
  // Coutournement du bug sur la perte de groupe sélectionné (valeur =-1)
  if($group_ids==-1) 
  $group_ids = array();

  pt_assert(is_array($group_ids),__FILE__,__LINE__,"get_planning_groups",'quiet',$group_ids);
  $groups = array();
  foreach($group_ids as $g_id)
  {
    $gi = get_group_info($g_id);
    $groups[$g_id]["label"] = $gi["name"];
    $groups[$g_id]["stats"] = 1;
 
    $users = get_all_users_id_from_group($g_id);
    if(count($users)>0)
    { 
      $obm_q = run_query_planning_get_usernames($users);
      while($obm_q->next_record())
      {
        $u_id=$obm_q->f('userobm_id');
        $name = $obm_q->f('userobm_lastname');
        $groups[$g_id]["users"][$u_id] = $name;
      }
    }
  } 
  return $groups;
}

///////////////////////////////////////////////////////////////////////////////
// Query execution : Tasktype Group insert
// Parameters:
//   - $params[] : tasktype group hash info
///////////////////////////////////////////////////////////////////////////////
function run_query_tasktypegroup_insert($params) {
  global $cdg_sql, $obm;
	
  $obm_q = new DB_OBM;
  $label = $params['label'];
  $code = $params['code'];
  $bgcolor = $params['bgcolor'];
  $fgcolor = $params['fgcolor'];
  $domain_id = sql_parse_id($obm['domain_id']);
	
  $query = "INSERT INTO TaskTypeGroup (tasktypegroup_timeupdate,
    tasktypegroup_timecreate,
    tasktypegroup_usercreate,
    tasktypegroup_domain_id,
    tasktypegroup_label,
    tasktypegroup_code,
    tasktypegroup_bgcolor,
    tasktypegroup_fgcolor)
  VALUES (null,
    '".date('Y-m-d H:i:s')."',
    ".$obm['uid'].",
    $domain_id,
    '".$obm_q->escape($label)."',
    '".$obm_q->escape($code)."',
    '".$obm_q->escape($bgcolor)."',
    '".$obm_q->escape($fgcolor)."')";

  display_debug_msg($query, $cdg_sql, 'run_query_tasktypegroup_insert()');
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Query execution : Tasktypegroup update
// Parameters:
//   - $params[] : tasktypegroup hash info
///////////////////////////////////////////////////////////////////////////////
function run_query_tasktypegroup_update($params) {
  global $obm, $cdg_sql;
  
  $obm_q = new DB_OBM;
  $id = sql_parse_id($params['tasktypegroup'], true);

  $label = $params['tasktypegroup_label'];
  $code = $params['tasktypegroup_code'];
  $bgcolor = $params['tasktypegroup_bgcolor'];
  $fgcolor = $params['tasktypegroup_fgcolor'];
  $multidomain = sql_multidomain('tasktypegroup');

  $query = "UPDATE TaskTypeGroup SET
    tasktypegroup_label='".$obm_q->escape($label)."',
    tasktypegroup_timeupdate='".date('Y-m-d H:i:s')."',
    tasktypegroup_userupdate='".$obm['uid']."',
    tasktypegroup_code='".$obm_q->escape($code)."',
    tasktypegroup_bgcolor='".$obm_q->escape($bgcolor)."',
    tasktypegroup_fgcolor='".$obm_q->escape($fgcolor)."'
  WHERE tasktypegroup_id $id
    $multidomain";
  
  display_debug_msg($query, $cdg_sql, 'run_query_tasktypegroup_update()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a tasktype group
// Parameters:
//   - $id : tasktypegroup id
///////////////////////////////////////////////////////////////////////////////
function get_tasktypegroup_label($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('tasktypegroup');
  $id = sql_parse_id($id, true);
  $query = "SELECT tasktypegroup_label FROM TaskTypeGroup 
		WHERE tasktypegroup_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_tasktypegroup_label()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f('tasktypegroup_label');
  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Query execution : Links to the tasktypegroup given
// Parameters:
//   - $s_id : tasktypegroup id
///////////////////////////////////////////////////////////////////////////////
function run_query_planning_tasktypegroup_links($s_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('tasktype');
  $s_id = sql_parse_id($s_id, true);	
  $query = "SELECT tasktype_id, tasktype_label FROM TaskType WHERE tasktype_tasktypegroup_id $s_id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_planning_tasktypegroup_links()');
  $sta_q = new DB_OBM;
  $sta_q->query($query);

  return $sta_q;
}

///////////////////////////////////////////////////////////////////////////////
// Query execution : Tasktypegroup deletion
// Parameters:
//   - $id : tasktypegroup id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_tasktypegroup_delete($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('tasktypegroup');
  $id = sql_parse_id($id, true);
  $query = "DELETE FROM TaskTypeGroup WHERE tasktypegroup_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_tasktypegroup_delete()');
  $sta_q = new DB_OBM;
  $retour = $sta_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Tasktype update
// Parameters:
//   - $params[] : tasktype hash info
///////////////////////////////////////////////////////////////////////////////
function run_query_tasktype_update($params) {
  global $obm, $cdg_sql;

  $id = sql_parse_id($params['tasktype'], true);

  $ttg = $params['tasktype_tasktypegroup_id'];
  $multidomain = sql_multidomain('tasktype');

  $query = "UPDATE TaskType SET
    tasktype_tasktypegroup_id='$ttg',
    tasktype_timeupdate='".date('Y-m-d H:i:s')."',
    tasktype_userupdate='".$obm['uid']."'
  WHERE tasktype_id $id
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_tasktype_update()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

// Fonction d'assert pour debug en cas de probleme
/* exemple d'appel :
  pt_assert(0,__FILE__,__LINE__,"test","quiet",$buggy_var);
  actions possibles : 'die','report','quiet'
*/
function pt_assert($cond,$file,$line,$context="non defini",$action='die',$extravar=null)
{
  if(!$cond)
  {
    global $obm,$obm_version;
    $user = $obm['login'];
    $url = "http(s)://".$_SERVER['SERVER_NAME'].":".$_SERVER['SERVER_PORT'].$_SERVER['REQUEST_URI'];
    $body = "Assertion failed in OBM Planning module

Context string : $context
Source code : $file:$line
URL : $url
User : $user
Version : $obm_version
Planning revision : $Id$

Additionnal infos :
".var_export($extravar,true)."

OBM vars :
".var_export($obm,true)."
Session : 
".var_export($_SESSION,true)."

Server variables :
".var_export($_SERVER,true)."\n";
    
    mail("root","OBM Planning : time to fix this one",$body);

    if($action!='quiet')
    {
      print"<h1>Erreur applicative</h1>";

      print "<p>Une erreur est survenue dans l'execution de ce module planning<br>\n";
      print "Un rapport a ete envoyé a l'administrateur de maniere a pouvoir le corriger rapidement<br>";

      print "Contexte de l'erreur : <i>$context</i></p>";
      if($action=='die')
      die("Fin du script : assertion failed");
    }
  }
}
</script>
