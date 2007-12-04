<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : project_query.inc                                            //
//     - Desc : project query File                                           //
// 2003-07-08 Aliacom                                                        //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Project search query execution
// Parametes:
//   - $params[]    : project search criteria
//     keys used       name, company_name, tt, manager, member
///////////////////////////////////////////////////////////////////////////////
function run_query_project_search($params) {
  global $c_all, $cdg_sql, $ctu_sql_limit;
  global $cgp_archive_only;

  $sql_order_dir = $params['sql_order_dir'];
  $sql_order_field = $params['sql_order_field'];

  $name = sql_search_text_parse($params['name']);
  $type_id = $params['type_id'];
  $tt = $params['tt'];
  $company_name = sql_search_text_parse($params['company_name']);
  $company = $params['company_id'];
  $deal = $params['deal_id'];
  $manager = $params['manager'];
  $member = $params['member'];
  $archive = $params['archive'];
  $date = of_isodate_convert($params['datebegin'],true);

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $datebegin = sql_date_format($db_type, 'project_datebegin', 'project_datebegin');
  $dateend = sql_date_format($db_type, 'project_dateend', 'project_dateend');
  $multidomain = sql_multidomain('project');

  $where = "1=1 $multidomain";

  if ($name != '') {
    $where .= " AND project_name $like '$name%'";
  }

  if ($company) {
    $where .= " AND project_company_id = '$company'";
  }

  if ($company_name != '') {
    $where .= sql_global_company_name_advanced_search($company_name, $like);
    $join_comp = 'LEFT JOIN Company on project_company_id=company_id';
  }

  if ($deal) {
    $where .= " AND project_deal_id = '$deal'";
  }

  if ($type_id != 0) {
    $where .= " AND project_type_id = '$type_id'";
  }

  if ($tt != 0) {
    $where .= " AND project_tasktype_id = '$tt'";
  }

  if ($date != '') {
    $where .= " AND project_datebegin <= '$date'
                  AND (project_dateend >= '$date' OR project_dateend is null)";
  }

  if ($archive != '1') {
    $where .= " AND project_archive = '0'";
  } elseif($cgp_archive_only) {
    $where .= " AND project_archive = '1'";
  }

  if (($member != '') && ($member != $c_all)) {
    $join_member = "LEFT JOIN ProjectUser as Member ON project_id=Member.projectuser_project_id";
    $where .= " AND Member.projectuser_user_id = '$member'
                AND Member.projectuser_projecttask_id is null";
  }

  if (($manager != '') && ($manager != $c_all)) {
    $join_member = "LEFT JOIN ProjectUser as Member
                      ON project_id=Member.projectuser_project_id
                        AND Member.projectuser_manager = 1";
    $where .= " AND Member.projectuser_user_id = '$manager'
                AND Member.projectuser_projecttask_id is null";
  }

  $whereq = "WHERE $where";

  // Order by clause
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'project_name';
  $orderq = " ORDER BY $order $sql_order_dir";

  $query = "SELECT DISTINCT
      project_id as id,
      project_id,
      project_name,
      project_shortname,
      project_soldtime,
      project_estimatedtime,
      company_name as project_company,
      dealtype_label as project_type,
      tasktype_label as project_tasktype,
      $datebegin,
      $dateend,
      project_archive
    FROM Project
         LEFT JOIN Company on project_company_id=company_id
         LEFT JOIN Deal on project_deal_id=deal_id
         LEFT JOIN DealType on project_type_id=dealtype_id
         LEFT JOIN TaskType on project_tasktype_id=tasktype_id
         $join_member
    $whereq
    $orderq
    $limit
";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM Project $join_comp $join_member $whereq");
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'run_query_project_search');
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Detail query execution (retrieve the main infos about the project)
// Parameters :
//   - $p_id  : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_project_detail($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $datebegin = sql_date_format($db_type, 'project_datebegin', 'datebegin');
  $dateend = sql_date_format($db_type, 'project_dateend', 'dateend');
  $timeupdate = sql_date_format($db_type, 'project_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'project_timecreate', 'timecreate');
  $multidomain = sql_multidomain('project');
	
  $query = "SELECT
      project_id,
      project_name,
      project_shortname,
      project_tasktype_id,
      project_company_id,
      project_deal_id,
      dealtype_label as type_label,
      tasktype_label as project_tasktype_label,
      company_name,
      deal_label,
      project_soldtime,
      project_estimatedtime,
      project_archive,
      project_reference_date,
      project_reference_duration,
      project_reference_desc,
      project_reference_tech,
      project_comment,
      $timeupdate,
      $timecreate,
      $datebegin,
      $dateend,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM Project
         left join Company on project_company_id=company_id
         left join Deal on project_deal_id=deal_id
         left join DealType on project_type_id=dealtype_id
         left join TaskType on project_tasktype_id=tasktype_id
         LEFT JOIN UserObm as c on project_usercreate=c.userobm_id
         LEFT JOIN UserObm as u on project_userupdate=u.userobm_id
    WHERE project_id='$p_id' $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_project_detail');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Project tasks user allocation and time spend (in time management)
// Different from run_query_project_allocation as a project member who has already
// spent some time on the project (filled in the time management) but is not
// anymore assigned to some tasks, must appear in the detailed progress table 
// Parameters:
//   - $p_id : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_project_allocation_and_time($p_id) {
  global $cdg_sql, $c_day_fraction;

  $frac = $c_day_fraction;
  $multidomain = sql_multidomain('userobm');	
  
  // We first get Times from Allocated Tasks
  $query = "
    SELECT
      userobm_id,
      projecttask_id,
      sum((timetask_length)/$frac) as used_time,
      projectuser_projectedtime as projected_time,
      projectuser_missingtime as missing_time
    FROM ProjectUser
    JOIN ProjectTask
      on projectuser_projecttask_id = projecttask_id
    LEFT JOIN UserObm
      on projectuser_user_id = userobm_id
    LEFT JOIN TimeTask
      on timetask_projecttask_id = projecttask_id
      and timetask_user_id = userobm_id
    WHERE projectuser_project_id = '$p_id' $multidomain
      AND ( (projectuser_projectedtime != '0'
             AND projectuser_projectedtime is not null)
          OR (projectuser_missingtime != '0'
             AND projectuser_missingtime is not null) )
    GROUP BY userobm_id, projecttask_id, projected_time, missing_time
";

  display_debug_msg($query, $cdg_sql, 'run_query_project_allocation_and_time');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  $times = array();
  $cpt = 0;
  while ($obm_q->next_record()) {
    $times[$cpt]['uid'] = $obm_q->f('userobm_id');
    $times[$cpt]['task_id'] = $obm_q->f('projecttask_id');
    $times[$cpt]['allo'] = $obm_q->f('projected_time');
    $times[$cpt]['miss'] = $obm_q->f('missing_time');
    $times[$cpt]['used'] = $obm_q->f('used_time');
    $cpt++;
  }

  // We query TimeTask data filled but where no allocation is present anymore
  // We start by TimeTask and join to ProjectUser but ProjectUser must be empty
  $query = "
    SELECT
      userobm_id,
      projecttask_id,
      projecttask_project_id, 
      sum((timetask_length)/$frac) as used_time
    FROM TimeTask
      LEFT JOIN ProjectTask
        ON timetask_projecttask_id = projecttask_id
      LEFT JOIN UserObm
        ON timetask_user_id = userobm_id
      LEFT JOIN ProjectUser
        ON projectuser_projecttask_id = projecttask_id
        AND projectuser_user_id = userobm_id
    WHERE projecttask_project_id = '$p_id'
      AND projectuser_projectedtime is null
      AND projectuser_missingtime is null
    GROUP BY userobm_id, projecttask_id, projecttask_project_id";

  display_debug_msg($query, $cdg_sql, 'run_query_project_allocation_and_time (2)');
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $times[$cpt]['uid'] = $obm_q->f('userobm_id');
    $times[$cpt]['task_id'] = $obm_q->f('projecttask_id');
    $times[$cpt]['allo'] = '0';
    $times[$cpt]['miss'] = '0';
    $times[$cpt]['used'] = $obm_q->f('used_time');
    $cpt++;
  }

  return $times;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query execution
// Parameters:
//   - $params[] : Entry's values
//     keys used  : name, soldtime, tt
///////////////////////////////////////////////////////////////////////////////
function run_query_project_insert($params) {
  global $obm, $cdg_sql;

  $uid = $obm['uid'];
  $date_now = date('Y-m-d H:i:s');
  $domain_id = $obm['domain_id'];
  $name = $params['name'];
  $soldtime = ($params['soldtime'] ? "'".$params['soldtime']."'" : 'null');
  $estimated = ($params['estimated'] ? "'".$params['estimated']."'" : 'null');
  $type_id = $params['type_id'];
  $tt = $params['tt'];
  $comp = ($params['company_id'] ? "'".$params['company_id']."'" : 'null');
  $deal = ($params['deal_id'] ? "'".$params['deal_id']."'" : 'null');
  $task = trim($params['tasklabel']);
  $member = $params['member'];
  $datebegin = ($params['datebegin'] ? "'".of_isodate_convert($params['datebegin'])."'" : 'null');
  $dateend = ($params['dateend'] ? "'".of_isodate_convert($params['dateend'])."'" : 'null');
  $project_reference_date = $params['reference_date'];
  $project_reference_duration = $params['reference_duration'];
  $project_reference_desc = $params['reference_desc'];
  $project_reference_tech = $params['reference_tech'];
  $add_comment = $params['add_comment'];
  $ref_task = $params['reftask_id'];
  $shortname = $params['shortname'];
  $archive = ($params['archive'] == '1' ? '1' : '0');

  if ($add_comment != '') {
    $datecomment = of_isodate_convert($params['datecomment'],true);
    $usercomment = $params['usercomment'];
    $comment = "$datecomment:$usercomment:$add_comment";
  }

  $query = "INSERT INTO Project (
    project_timeupdate,
    project_timecreate,
    project_userupdate,
    project_usercreate,
    project_domain_id,
    project_name,
    project_shortname,
    project_type_id,
    project_tasktype_id,
    project_company_id,
    project_deal_id,
    project_soldtime,
    project_estimatedtime,
    project_datebegin,
    project_dateend,
    project_archive,
    project_reference_date,
    project_reference_duration,
    project_reference_desc,
    project_reference_tech,
    project_comment)
  VALUES (null,
    '$date_now',
    null,
    '$uid',
    '$domain_id',
    '$name',
    '$shortname',
    '$type_id',
    '$tt',
    $comp,
    $deal,
    $soldtime,
    $estimated,
    $datebegin,
    $dateend,
    '$archive',
    '$project_reference_date',
    '$project_reference_duration',
    '$project_reference_desc',
    '$project_reference_tech',
    '$comment')";

  display_debug_msg($query, $cdg_sql, 'run_query_project_insert');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  // get the id of the inserted project. Date is to insure correct project
  // is selected (and not similar from the same deal)
  $query = "SELECT project_id
            FROM Project
            WHERE project_name = '$name'
              AND project_tasktype_id = '$tt'
              AND project_timecreate = '$date_now'";

  display_debug_msg($query, $cdg_sql, 'run_query_project_insert (get Id)');
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    $obm_q->next_record();
    $pid = $obm_q->f('project_id');
  } else {
    $pid = 0;
    return $pid;
  }

  // If a task has been given, We create a task for this project
  if ($task != '') {
    $query = "
    INSERT INTO ProjectTask (
      projecttask_project_id,
      projecttask_timecreate,
      projecttask_usercreate,
      projecttask_label,
      projecttask_parenttask_id,
      projecttask_rank)
    VALUES (
      '$pid',
      '$date_now',
      '$uid',
      '$task',
      '0',
      '1')";

    display_debug_msg($query, $cdg_sql, 'run_query_project_insert (create task)');
    $retour = $obm_q->query($query);
  }

  if (is_array($ref_task)) {
    $query = "
    INSERT INTO ProjectTask (
      projecttask_project_id,
      projecttask_timecreate,
      projecttask_usercreate,
      projecttask_label,
      projecttask_parenttask_id,
      projecttask_rank)
    SELECT 
      '$pid',
      '$date_now',
      '$uid',
      projectreftask_label,
      '0',
      '1'
    FROM ProjectRefTask
    WHERE
      projectreftask_id IN (".implode(',',$ref_task).')';
      display_debug_msg($query, $cdg_sql, 'run_query_project_insert (create ref task)');
      $retour = $obm_q->query($query);
  }

  // If a member has been given, We insert it
  if ($member > 0) {

    // We affect the member to this project (projecttask null)
    $query = "
      INSERT INTO ProjectUser 
        (projectuser_project_id,
         projectuser_user_id,
         projectuser_projecttask_id,
         projectuser_timecreate,
         projectuser_usercreate,
         projectuser_manager
        )
      VALUES
        ('$pid',
        '$member',
        null,
        '$date_now',
        '$uid',
        '1')";

    display_debug_msg($query, $cdg_sql, 'run_query_project_insert (add member)');
    $retour = $obm_q->query($query);
    
    // Alert the user he has been affected to the project
    project_send_mail($params, $pid);

    // We get the projecttask_id if a project task has been filled
    if ($task != '') {
      $query = "
        SELECT projecttask_id
        FROM ProjectTask
        WHERE projecttask_project_id = '$pid'";

      display_debug_msg($query, $cdg_sql, 'run_query_short_create(get task)');
      $obm_q->query($query);
      $obm_q->next_record();
      $pt_id = "'".$obm_q->f('projecttask_id')."'";

      // We affect the member to this projecttask
      $query = "
      INSERT INTO ProjectUser 
        (projectuser_project_id,
         projectuser_user_id,
         projectuser_projecttask_id,
         projectuser_timecreate,
         projectuser_usercreate,
         projectuser_projectedtime,
         projectuser_missingtime,
         projectuser_validity
        )
      VALUES
        ('$pid',
        '$member',
        $pt_id,
        '$date_now',
        '$uid',
        $estimated,
        $estimated,
        '$date_now')";

      display_debug_msg($query, $cdg_sql, 'run_query_project_insert (affect member)');
      $retour = $obm_q->query($query);
    }
  }

  return $pid;
}


///////////////////////////////////////////////////////////////////////////////
// Update query execution
// Parameters:
//   - $pid       : project id
//   - $params[] : Entry's va)lues
//     keys used  : name, soldtime, tt
///////////////////////////////////////////////////////////////////////////////
function run_query_project_update($pid, $params) {
  global $obm, $cdg_sql;

  $uid = $obm['uid'];
  $name = $params['name'];
  $soldtime = ($params['soldtime'] ? "'".$params['soldtime']."'" : 'null');
  $estimated = ($params['estimated'] ? "'".$params['estimated']."'" : 'null');
  $type_id = $params['type_id'];
  $tt = $params['tt'];
  $datebegin = ($params['datebegin'] ? "'".of_isodate_convert($params['datebegin'])."'" : 'null');
  $dateend = ($params['dateend'] ? "'".of_isodate_convert($params['dateend'])."'" : 'null');
  $reference_date = $params['reference_date'];
  $reference_duration = $params['reference_duration'];
  $reference_desc = $params['reference_desc'];
  $reference_tech = $params['reference_tech'];
  $archive = ($params['archive'] == '1' ? '1' : '0');
  $shortname = $params['shortname'];

  if ($archive) {
    $arch = '1';
  } else {
    $arch = '0';
  }
  $comment = $params['comment'];
  $add_comment = $params['add_comment'];
  if ($add_comment != '') {
    $datecomment = of_isodate_convert($params['datecomment'],true);
    $usercomment = $params['usercomment'];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $multidomain = sql_multidomain('project');
  $query = "UPDATE Project SET
      project_timeupdate='". date('Y-m-d H:i:s') ."',
      project_userupdate='$uid',
      project_name='$name',
      project_shortname='$shortname',
      project_type_id='$type_id',
      project_tasktype_id='$tt',
      project_soldtime=$soldtime,
      project_estimatedtime=$estimated,
      project_datebegin=$datebegin,
      project_dateend=$dateend,
      project_reference_date='$reference_date',
      project_reference_duration='$reference_duration',
      project_reference_desc='$reference_desc',
      project_reference_tech='$reference_tech',
      project_archive='$arch',
      project_comment='$comment'
    WHERE project_id='$pid' $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_project_update');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return the number of timetask entries assigned to the project
// Parameters:
//   - $p_id : project Id
///////////////////////////////////////////////////////////////////////////////
function get_project_linked_project_timetask_nb($p_id) {
  global $cdg_sql;
  $multidomain = sql_multidomain('project');

  $obm_q = new DB_OBM;
  $query = "SELECT count(timetask_id) as nb
    FROM TimeTask,
    ProjectTask,
    Project
    WHERE projecttask_project_id = '$p_id'
      AND projecttask_project_id = project_id
      AND timetask_projecttask_id = projecttask_id $multidomain";

  display_debug_msg($query, $cdg_sql, "get_project_linked_project_timetask_nb($p_id)");
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f('nb');

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_project_delete($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  // Delete all links to documents
  run_query_global_delete_document_links($p_id, 'project');

  // Delete all users associations to the project
  $query = "DELETE FROM ProjectUser 
		WHERE projectuser_project_id='$p_id'";
  display_debug_msg($query, $cdg_sql, 'run_query_project_delete(1)');
  $retour = $obm_q->query($query);

  // Delete all task assigned to the project
  $query = "DELETE FROM ProjectTask WHERE projecttask_project_id='$p_id'";
  display_debug_msg($query, $cdg_sql, 'run_query_project_delete(2)');
  $retour = $obm_q->query($query);

  // Delete the project
  $multidomain_project = sql_multidomain('project');
  $query = "DELETE FROM Project WHERE project_id='$p_id' $multidomain_project";
  display_debug_msg($query, $cdg_sql, 'run_query_project_delete(3)');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : UserList conditionnal insertion
// Parameters:
//   - $params[] : Entry's values
//     keys used  : id
///////////////////////////////////////////////////////////////////////////////
function run_query_project_name($pid) {
  global $cdg_sql;

  $multidomain = sql_multidomain('project');

  $query = "
    SELECT project_name
    FROM Project
    WHERE project_id = '$pid' $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_project_name');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->nf() != 0) {
    $obm_q->next_record();
    $p_name = $obm_q->f('project_name');
  } else {
    $p_name = 'project name error';
  }

  return $p_name;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Task conditionnal insertion
// Parameters:
//   - $params[] : Entry's values
//     keys used  : id, ptask, tasklabel
///////////////////////////////////////////////////////////////////////////////
function run_query_project_task_insert($params) {
  global $cdg_sql, $obm;

  $pid = $params['project_id'];
  $ptask = $params['ptask'];
  $tasklabel = trim($params['tasklabel']);
  $ref_task = $params['reftask_id'];
  $search_q = new DB_OBM;

  // If a parent task has been given, check that there are no links from
  // User to the parent task (a parent task can't be assigned to users)
  if (!(isset($ptask))) {
    $ptask = 0;
  } else {
    $multidomain = sql_multidomain('userobm');
    $query = "
      SELECT projectuser_user_id
      FROM ProjectUser
      JOIN UserObm ON projectuser_user_id = userobm_id
      WHERE projectuser_projecttask_id = '$ptask' $multidomain";

    display_debug_msg($query, $cdg_sql, 'run_query_project_task_insert(1)');
    $search_q->query($query);
  }

  if (($ptask == 0) or ($search_q->nf() == 0))  {
    $obm_q = new DB_OBM;
    if ($tasklabel != '') {
      $query = "
        INSERT INTO ProjectTask (
          projecttask_project_id,
          projecttask_timecreate,
          projecttask_usercreate,
          projecttask_label,
          projecttask_parenttask_id,
          projecttask_rank)
        VALUES (
          '$pid',
          '". date('Y-m-d H:i:s') ."',
          '". $obm['uid'] ."',
          '$tasklabel',
          '$ptask',
          '1')";

      display_debug_msg($query, $cdg_sql, 'run_query_project_task_insert(2)');
      $obm_q->query($query);
    }
    if(is_array($ref_task)) {
      $query = "
        INSERT INTO ProjectTask (
        projecttask_project_id,
        projecttask_timecreate,
        projecttask_usercreate,
        projecttask_label,
        projecttask_parenttask_id,
        projecttask_rank)
        SELECT 
        '$pid',
        '". date('Y-m-d H:i:s') ."',
        '$uid',
        projectreftask_label,
        '$ptask',
        '1' FROM ProjectRefTask WHERE
        projectreftask_id IN (".implode(',',$ref_task).')';
      display_debug_msg($query, $cdg_sql, 'run_query_project_insert (create ref task)');
      $obm_q->query($query);
    }    
    return $obm_q;
  } else {
    return 0;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Task Update
// Parameters:
//   - $params[] : Entry's values
//     keys used  : id, ptask, tasklabel
///////////////////////////////////////////////////////////////////////////////
function run_query_project_task_update($params) {
  global $obm, $cdg_sql;

  $task = $params['task'];
  $ptask = $params['ptask'];
  $tasklabel = $params['tasklabel'];

  $query = "
      UPDATE ProjectTask SET
        projecttask_timeupdate='". date('Y-m-d H:i:s') ."',
        projecttask_userupdate='". $obm['uid'] ."',
        projecttask_label='$tasklabel',
        projecttask_parenttask_id='$ptask',
        projecttask_rank='1'
      WHERE
        projecttask_id='$task'";

  display_debug_msg($query, $cdg_sql, 'run_query_project_task_update(2)');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Tasklist deletion
// Parameters:
//   - $params[] : hash info : keys used : id, task_nb, tskX
///////////////////////////////////////////////////////////////////////////////
function run_query_project_task_delete($params) {
  global $cdg_sql;

  $id = $params['project_id'];
  $cpt = 0;
  $cpt_del = 0;
  $delete_ok = true;
  
  $multidomain = sql_multidomain('project');
  while ($cpt < $params['tsk_nb']) {
    $cpt++;
    $tsk_id = $params["tsk$cpt"];
    
    // Check that neither the task to be deleted or its children tasks are
    // assigned to users
    $query = "
      SELECT projectuser_user_id
      FROM ProjectUser, ProjectTask, Project
      WHERE projectuser_projecttask_id = projecttask_id
        AND projecttask_project_id = project_id
        AND (projecttask_id='$tsk_id' OR projecttask_parenttask_id='$tsk_id')
        $multidomain ";
    
    display_debug_msg($query, $cdg_sql, 'run_query_project_task_delete(1)');
    $search_q = new DB_OBM;
    $search_q->query($query);

    if ($search_q->nf() > 0) {
      $delete_ok = false;
    }

    if ($delete_ok == true) {
      // Check that neither the task to be deleted or its childre tasks are
      // used in time management
      $query = "
        SELECT timetask_id
        FROM TimeTask, ProjectTask
        WHERE timetask_projecttask_id = projecttask_id
          AND (projecttask_id='$tsk_id' or projecttask_parenttask_id='$tsk_id')";
    
      display_debug_msg($query, $cdg_sql, 'run_query_project_task_delete(2)');
      $search_q = new DB_OBM;
      $search_q->query($query);

      if ($search_q->nf() > 0) {
	$delete_ok = false;
      }
    }

    // If the task can be deleted
    if ($delete_ok) {
      $query = "
        DELETE
        FROM ProjectTask
        WHERE projecttask_id = '$tsk_id'
          OR  projecttask_parenttask_id = '$tsk_id'";

      display_debug_msg($query, $cdg_sql, 'run_query_project_task_delete(3)');
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      if ($retour)
	$cpt_del++;
    }
  }

  return $cpt_del;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Member conditionnal insertion
// Parameters:
//   - $params[] : hash info : keys used : id, mem_nb, memX
// Return: number of users inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_project_memberlist_insert($params) {
  global $cdg_sql, $obm;

  $id = $params['project_id'];
  $cpt = 0;
  $cpt_ins = 0;
  $multidomain = sql_multidomain('userobm');
	  
  while ($cpt < $params['mem_nb']) {
    $cpt++;
    $mem_id = $params["mem$cpt"];

    $query = "
      SELECT *
      FROM ProjectUser
      JOIN UserObm ON projectuser_user_id = userobm_id
      WHERE projectuser_project_id = '$id'
        AND projectuser_user_id = '$mem_id' $multidomain";

    display_debug_msg($query, $cdg_sql, 'run_query_project_memberlist_insert(2)');
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $query = "
        INSERT INTO ProjectUser 
          (projectuser_user_id,
           projectuser_project_id,
           projectuser_projecttask_id,
           projectuser_timecreate,
           projectuser_usercreate,
           projectuser_projectedtime,
           projectuser_manager
          )
        VALUES
          ('$mem_id',
          '$id',
          null,
          '".date('Y-m-d H:i:s')."',
          ". $obm['uid'] .",
          0,
          0)";

      display_debug_msg($query, $cdg_sql, 'run_query_project_memberlist_insert(3)');
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      $cpt_ins++;
    }
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Member deletion
// Parameters:
//   - $params[] : hash info : keys used : id, mem_nb, memX
///////////////////////////////////////////////////////////////////////////////
function run_query_project_memberlist_delete($params) {
  global $cdg_sql;

  $id = $params['project_id'];
  $cpt = 0;
  $cpt_del = 0;
  $multidomain = sql_multidomain('userobm');

  $obm_q = new DB_OBM;
  $search_q = new DB_OBM;

  while ($cpt < $params['mem_nb']) {
    $cpt++;
    $mem_id = $params["mem$cpt"];

    // Check if the user has filled time management for this project
    $query = "
        SELECT timetask_id
        FROM TimeTask, ProjectTask, UserObm
        WHERE timetask_user_id = '$mem_id'
          AND userobm_id = timetask_user_id
          AND timetask_projecttask_id = projecttask_id
          AND projecttask_project_id = '$id' $multidomain";
    
    display_debug_msg($query, $cdg_sql, 'run_query_project_member_delete(2b)');
    $search_q->query($query);
    
    if ($search_q->nf() == 0) {
      $query = "
        DELETE
        FROM ProjectUser
        WHERE projectuser_project_id = '$id'
          AND projectuser_user_id = '$mem_id'";
      
      display_debug_msg($query, $cdg_sql, 'run_query_project_memberlist_delete(3b)');
      $retour = $obm_q->query($query);
      
      if ($retour)
	$cpt_del++;
    }
  }

  return $cpt_del;
}


///////////////////////////////////////////////////////////////////////////////
// Member status Update query execution
// Parameters:
//   - $params[] : hash info : user_id, user_status
///////////////////////////////////////////////////////////////////////////////
function run_query_project_memberstatus_change($params) {
  global $cdg_sql;

  $p_id = $params['project_id'];
  $m_id = $params['user_id'];
  $m_val = $params['user_status'];
		
  $query = "
    UPDATE ProjectUser
    SET projectuser_manager = '$m_val'
    WHERE projectuser_user_id = '$m_id'
      AND projectuser_project_id = '$p_id'
      AND projectuser_projecttask_id is null";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, 'run_query_project_memberstatus_change');
  $obm_q->query($query);
  if ($m_val == 1) {
    project_send_mail($params,$pid);
  }
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Project tasks user allocation
// Parameters:
//   - $p_id : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_project_allocation($p_id) {
  global $cdg_sql, $c_day_fraction;

  $frac = $c_day_fraction;
  $multidomain = sql_multidomain('userobm');
	
  $query = "
    SELECT
      userobm_id,
      projectuser_projecttask_id,
      projectuser_projectedtime as projected_time,
      projectuser_missingtime as missing_time
    FROM ProjectUser
      LEFT JOIN ProjectTask
        ON projectuser_projecttask_id = projecttask_id
      LEFT JOIN UserObm
        ON projectuser_user_id = userobm_id
    WHERE projectuser_project_id = '$p_id'
      AND projectuser_projectedtime is not null $multidomain
    GROUP BY userobm_id, projectuser_projecttask_id, projected_time, missing_time
    ";

  display_debug_msg($query, $cdg_sql, 'run_query_project_allocation');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Project Allocation Update query execution
// Parameters:
//   - $params[] : hash info : keys used : projected
///////////////////////////////////////////////////////////////////////////////
function run_query_project_allocate_update($params) {
  global $cdg_sql, $obm;

  $p_id = $params['project_id'];
  $new = $params['projected'];
  $uid = $obm['uid'];  
  $date_now = date('Y-m-d H:i:s');
  $multidomain = sql_multidomain('project');
	
  $query = "
    SELECT
      projectuser_user_id as mt_user,
      projectuser_projecttask_id as mt_task,
      projectuser_projectedtime as mt_proj,
      projectuser_missingtime as mt_miss
    FROM ProjectUser
    JOIN Project ON project_id = projectuser_project_id
    WHERE projectuser_project_id = '$p_id'
      $multidomain
    ORDER by mt_user, mt_task";

  $old_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, 'run_query_project_allocate_update(old)');
  $old_q->query($query);

  while ($old_q->next_record()) {
    $old_user = $old_q->f('mt_user');
    $old_task = $old_q->f('mt_task');
    $old_proj = $old_q->f('mt_proj');
    $old_miss = $old_q->f('mt_miss');
    
    $old[$old_user][$old_task] = $old_proj;
    $miss_tab[$old_user][$old_task] = $old_miss;
  }
  
  $obm_q = new DB_OBM;
  
  while ( list($m_id, $ttime) = each($new) ) {
    while ( list($t_id, $proj) = each($ttime) ) {

      $tnew = ($new[$m_id][$t_id] != '')  ? 1 : 0;
      $told = (isset ($old[$m_id][$t_id])) ? 1 : 0;
    
      // if an allocation existed, but has been removed
      if (!($tnew) and ($told)) {
	$query = "
        DELETE from ProjectUser
        WHERE projectuser_user_id = '$m_id'
          AND projectuser_projecttask_id = '$t_id'";

 	display_debug_msg($query, $cdg_sql, 'run_query_project_allocate_update(2)');
	$obm_q->query($query);

      // if an allocation existed, and a new has been given
      } else if (($tnew) and ($told)) {
	// if missing time was not set or never updated (= old projected)
	// we set missing time to new projected
	if ( (! isset($miss_tab[$m_id][$t_id]))
             || ($miss_tab[$m_id][$t_id] == $old[$m_id][$t_id]) ) {
	  $set_miss_time = ", projectuser_missingtime = '$proj'";
	} else {
	  $set_miss_time = '';
	}

	$query = "
         UPDATE ProjectUser
         SET projectuser_timeupdate = '$date_now',
             projectuser_userupdate = '$uid',
             projectuser_projectedtime = '$proj'
             $set_miss_time
         WHERE projectuser_user_id = '$m_id'
           AND projectuser_projecttask_id = '$t_id'";

	display_debug_msg($query, $cdg_sql, 'run_query_project_allocate_update(3)');
	$obm_q->query($query);

      // if a new allocation has been given
      } else if (($tnew) and !($told)){

	$query = "	
        INSERT INTO ProjectUser 
          (projectuser_user_id,
           projectuser_project_id,
           projectuser_projecttask_id,
           projectuser_timecreate,
           projectuser_usercreate,
           projectuser_projectedtime,
           projectuser_missingtime
          )
        VALUES
          ('$m_id',
           '$p_id',
           '$t_id',
           '$date_now',
           '$uid',
           '$proj',
           '$proj'
          )";

 	display_debug_msg($query, $cdg_sql, 'run_query_project_allocate_update(4)');
	$obm_q->query($query);
      }
    }
  }
 
  return $ins_err;
}


///////////////////////////////////////////////////////////////////////////////
// Project Advance Update query execution
// Parameters:
//   - $params[] : hash info : keys used : projected
///////////////////////////////////////////////////////////////////////////////
function run_query_project_advance_update($params) {
  global $cdg_sql, $obm;

  $p_id = $params['project_id'];
  $new = $params['projected'];
  $uid = $obm['uid'];  
  $date_now = date('Y-m-d H:i:s');
  $miss_tab = $params['missing'];
  $multidomain = sql_multidomain('project');
	
  $query = "
    SELECT
      projectuser_user_id as mt_user,
      projectuser_projecttask_id as mt_task,
      projectuser_projectedtime as mt_proj,
      projectuser_missingtime as mt_miss
    FROM ProjectUser
    JOIN Project on projectuser_project_id = project_id
    WHERE projectuser_project_id = '$p_id'
      $multidomain
    ORDER by mt_user, mt_task";

  $old_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, 'run_query_project_allocate_update(old)');
  $old_q->query($query);

  while ($old_q->next_record()) {
    $old_user = $old_q->f('mt_user');
    $old_task = $old_q->f('mt_task');
    $old_proj = $old_q->f('mt_proj');
    $old_miss = $old_q->f('mt_miss');
    
    $old[$old_user][$old_task] = $old_proj;
    $old_miss_tab[$old_user][$old_task] = $old_miss;
  }
  
  $obm_q = new DB_OBM;
  
  while ( list($m_id, $ttime) = each($new) ) {
    while ( list($t_id, $proj) = each($ttime) ) {

      $proj_new = ($new[$m_id][$t_id] > 0) ? $new[$m_id][$t_id] : 0;
      $proj_old = (isset ($old[$m_id][$t_id])) ? $old[$m_id][$t_id] : 0;
      $miss_new = ($miss_tab[$m_id][$t_id] > 0) ? $miss_tab[$m_id][$t_id] : 0;
      $miss_old = ($old_miss_tab[$m_id][$t_id] > 0) ? $old_miss_tab[$m_id][$t_id] : 0;

      // if an entry existed, but has been removed, we delete it
      if (!($proj_new) && !($miss_new) && ($proj_old || $miss_old)) {
	$query = "
        DELETE FROM ProjectUser
        WHERE projectuser_user_id = '$m_id'
          AND projectuser_projecttask_id = '$t_id'";

 	display_debug_msg($query, $cdg_sql, 'run_query_project_allocate_update(2)');
	$obm_q->query($query);

      // if an entry existed, and a new has been given, we update
      } else if (($proj_new || $miss_new) && ($proj_old || $miss_old)) {
	// if missing time not and never set or never updated (=old projected)
	// we set missing time to new projected
	if ( (! $proj_old) && (! $miss_old) && (! $miss_new) ) {
	  $set_miss_time = $proj_new;
	} else {
	  $set_miss_time = $miss_new;
	}

	$query = "
         UPDATE ProjectUser
         SET projectuser_timeupdate = '$date_now',
             projectuser_userupdate = '$uid',
             projectuser_projectedtime = '$proj_new',
             projectuser_missingtime = '$set_miss_time'
         WHERE projectuser_user_id = '$m_id'
           AND projectuser_projecttask_id = '$t_id'";

	display_debug_msg($query, $cdg_sql, 'run_query_project_allocate_update(3)');
	$obm_q->query($query);

      // if a new entry has been given, we insert it
      } else if (($proj_new || $miss_new) && (!($proj_old) && !($miss_old))) {
	// if missing time not set, we set missing time to new projected
	if (! $miss_new) {
	  $set_miss_time = $proj_new;
	} else {
	  $set_miss_time = $miss_new;
	}

	$query = "	
        INSERT into ProjectUser 
          (projectuser_user_id,
           projectuser_project_id,
           projectuser_projecttask_id,
           projectuser_timecreate,
           projectuser_usercreate,
           projectuser_projectedtime,
           projectuser_missingtime
          )
        VALUES
          ('$m_id',
           '$p_id',
           '$t_id',
           '$date_now',
           '$uid',
           '$proj_new',
           '$set_miss_time'
          )";

 	display_debug_msg($query, $cdg_sql, 'run_query_project_allocate_update(4)');
	$obm_q->query($query);
      }
    }
  }
 
  return $ins_err;
}


///////////////////////////////////////////////////////////////////////////////
// Stat Log query execution (creates an entry in the project progress log)
// Parameters:
//   - $p_id      : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_project_statlog($p_id) {
  global $cdg_sql, $c_day_fraction, $obm;

  $uid = $obm['uid'];
  $frac = $c_day_fraction;
  $multidomain = sql_multidomain('project');
	
  $query = "
    SELECT (sum(timetask_length)/8) as time_used
      FROM TimeTask, ProjectTask, Project
    WHERE timetask_projecttask_id = projecttask_id
    AND project_id = projecttask_project_id
    AND project_id = '$p_id' $multidomain
    GROUP BY project_id";

  display_debug_msg($query, $cdg_sql, 'run_query_project_statlog(1)');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $time_used = $obm_q->f('time_used');
  if ($time_used == '')  $time_used = 0;
  $multidomain = sql_multidomain('userobm');
  $query = "SELECT sum(projectuser_missingtime) as time_left
            FROM ProjectUser JOIN UserObm ON projectuser_user_id = userobm_id
            WHERE projectuser_project_id = '$p_id' $multidomain
            GROUP BY projectuser_project_id";

  display_debug_msg($query, $cdg_sql, 'run_query_project_statlog(2)');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $time_left = $obm_q->f('time_left');
	
  /*  $query = "
    INSERT INTO ProjectStat (
      projectstat_project_id,
      projectstat_usercreate,
      projectstat_date,
      projectstat_useddays,
      projectstat_remainingdays)
    VALUES (
      '$p_id',
      '$uid',
      '". date("Y-m-d H:i:s") ."',
      '$time_used',
      '$time_left'
    )";

   display_debug_msg($query, $cdg_sql, "run_query_project_statlog(4)");
   $obm_q = new DB_OBM;
   $retour = $obm_q->query($query);
  */

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Project tasks query
// Parameters :
//   - $pid : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_project_tasks($pid) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $if_parentgroup = sql_if($db_type, "(projecttask_parenttask_id != 0)", 'projecttask_parenttask_id', 'projecttask_id');
  $multidomain = sql_multidomain('project');

  $query = "
    SELECT
      projecttask_id,
      projecttask_label,
      projecttask_parenttask_id as parent_id,
      $if_parentgroup as parent_group
    FROM ProjectTask
    JOIN Project ON project_id = projecttask_project_id
    WHERE project_id = '$pid' $multidomain
    ORDER BY parent_group, parent_id, projecttask_rank";

  display_debug_msg($query, $cdg_sql, 'run_query_project_tasks');
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Project members query
// If no project id given, return the list of user referenced in projects
// Parameters :
//   - $p_id : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_project_members($p_id = 0) {
  global $cdg_sql;
  
  $multidomain = sql_multidomain('userobm');
  
  if ($p_id) {
    $query = "
      SELECT
        userobm_id as member_id,
        userobm_firstname as member_firstname,
        userobm_lastname as member_lastname,
        projectuser_manager as member_manager
      FROM ProjectUser
     LEFT JOIN UserObm ON projectuser_user_id = userobm_id
     WHERE projectuser_project_id = '$p_id'
        AND projectuser_projecttask_id is null
	$multidomain
     GROUP BY member_id, member_firstname, member_lastname, member_manager
     ORDER BY member_lastname";

  } else { 
    $query = "
      SELECT
        userobm_id as member_id,
        userobm_firstname as member_firstname,
        userobm_lastname as member_lastname
      FROM ProjectUser
      LEFT JOIN UserObm ON projectuser_user_id = userobm_id
      WHERE projectuser_projecttask_id is null $multidomain
      GROUP BY member_id, member_firstname, member_lastname
      ORDER BY member_lastname";
  }

  display_debug_msg($query, $cdg_sql, 'run_query_project_members()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Project Participants query (current members + time management filling) 
// Parameters :
//   - $p_id : project id
// Returns:
//   array of project particpants id
///////////////////////////////////////////////////////////////////////////////
function get_project_participants($p_id = 0) {
  global $cdg_sql;
  
  $multidomain = sql_multidomain('project');
  
  $query = "
    SELECT
      projectuser_user_id
    FROM ProjectUser JOIN Project on projectuser_project_id = project_id
      WHERE project_id = '$p_id' $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_participants(1)');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  $users_arr = array();
  while ($obm_q->next_record()) {
    $u_id = $obm_q->f('projectuser_user_id');
    array_push($users_arr, $u_id);
  }


  $query = "
    SELECT
      timetask_user_id
    FROM TimeTask, ProjectTask, Project
    WHERE timetask_projecttask_id = projecttask_id
    AND project_id = projecttask_project_id
    AND projecttask_project_id = '$p_id' $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_participants(2)');
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $u_id = $obm_q->f('timetask_user_id');
    array_push($users_arr, $u_id);
  }

  $users = array_unique ($users_arr);

  return $users;
}


///////////////////////////////////////////////////////////////////////////////
// Get Project infos
// Parameters:
//   - $id : project id
///////////////////////////////////////////////////////////////////////////////
function get_project_info($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $datebegin = sql_date_format($db_type, 'project_datebegin', 'project_datebegin');
  $dateend = sql_date_format($db_type, 'project_dateend', 'project_dateend');
  $multidomain = sql_multidomain('project');

  $query = "SELECT
      project_id,
      project_name,
      project_shortname,
      $datebegin,
      $dateend,
      project_soldtime,
      project_estimatedtime,
      project_comment,
      dealtype_label,
      tasktype_label,
      company_name,
      deal_label
    FROM Project
      LEFT JOIN DealType on project_type_id=dealtype_id
      LEFT JOIN TaskType on project_tasktype_id=tasktype_id
      LEFT JOIN Deal on project_deal_id=deal_id
      LEFT JOIN Company on project_company_id=company_id
    WHERE project_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_project_info()');
  $obm_q->query($query);
  $obm_q->next_record();
  
  $res['project_id'] = $obm_q->f('project_id');
  $res['name'] = $obm_q->f('project_name');
  $res['dealtype'] = $obm_q->f('dealtype_label');
  $res['tasktype'] = $obm_q->f('tasktype_label');
  $res['deal'] = $obm_q->f('deal_label');
  $res['company'] = $obm_q->f('company_name');
  $res['datebegin'] = $obm_q->f('project_datebegin');
  $res['dateend'] = $obm_q->f('project_dateend');
  $res['shortname'] = $obm_q->f('project_shortname');
  $res['soldtime'] = $obm_q->f('project_soldtime');
  $res['estimatedtime'] = $obm_q->f('project_estimatedtime');
  $res['comment'] = $obm_q->f('project_comment');

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// New Project Form Data checking and formatting
// Parameters:
//   - $pid   : project id
//   - $params[] : values checked
//     keys used  : name, tt, soldtime
// Returns:
//   - (true | false) : true if user have write rights for this project 
///////////////////////////////////////////////////////////////////////////////
function project_manager_rights($uid, $params, $project_q) {
  global $cdg_sql, $obm;

  if ($obm['profile'] == 'admin') {
    return true;
  }

  $pid = $params['project_id'];
  $multidomain = sql_multidomain('project');
	
  $query = "
    SELECT projectuser_id
    FROM ProjectUser
      JOIN Project ON project_id = projectuser_project_id
    WHERE project_id = '$pid'
      AND projectuser_manager != '0'
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'project_manager_rights(managers)');
  $managers_q = new DB_OBM;
  $managers_q->query($query);

  $project_manager_rights = 
    ( ( (is_object($project_q)) && ( ($project_q->f('usercreate') == $uid) ) )
    or ($project_q->f('userupdate') == $uid) 
    or ($managers_q->nf() != 0) ) ? true : false;

  return $project_manager_rights;
}


///////////////////////////////////////////////////////////////////////////////
// New Project Form Data checking and formatting
// Parameters:
//   - $pid       : project id (empty on insertion)
//   - $params[] : values checked
//     keys used  : name, tt, soldtime
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_project_form($pid, $params) {
  global $l_fill_name, $l_fill_tt, $l_fill_estimated, $l_fill_soldtime;
  global $err, $php_regexp_number,$l_tolong_shortname;

  $name = $params['name'];
  $company = $params['company_id'];
  $tt = $params['tt'];
  $soldtime = $params['soldtime'];
  $estimated = $params['estimated'];
  $member = $params['member'];

  $shortname = $params['shortname'];
  // MANDATORY: Project name
  if (trim($name) == '') {
    $err['msg'] = $l_fill_name;
    return false;
  }

  if (trim($shortname) != '' && strlen(trim($shortname)) > 10) {
    $err['msg'] = $l_tolong_shortname;
    return false;
  }


  // MANDATORY: TaskType selection
  if ($tt == 0) {
    $err['msg'] = $l_fill_tt;
    return false;
  }

  // MANDATORY: Soldtime
  if (($company > 0) && (trim($soldtime) == '')) {
    $err['msg'] = $l_fill_soldtime;
    return false;
  }

  // MANDATORY: Estimated
  if (trim($estimated) == '') {
    $err['msg'] = $l_fill_estimated;
    return false;
  }

  // MANDATORY: Soldtime
  //  if (preg_match($php_regexp_number, $soldtime) == 0)) {
  //     $err['msg'] = $l_fill_soldnumber .''.$soldtime.''.$php_regexp_number;
  //     return false;
  //   }

  return true; 
}


///////////////////////////////////////////////////////////////////////////////
// Check if the project can be deleted
// Parameters:
//   - $p_id : project id
// Returns:
//   true if the project can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_project_can_delete($p_id) {
  global $err, $ok_msg;
  global $l_link_timetask, $l_link_timetask_no;

  $delete_ok = true;

  // Links from Time management
  $nb = get_project_linked_project_timetask_nb($p_id);
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= "$l_link_timetask";
  } else {
    $ok_msg .= "$l_link_timetask_no";
  }

  return $delete_ok;
}


///////////////////////////////////////////////////////////////////////////////
// Project Task Form Data checking and formatting
// Parameters:
//   - $pid   : project id
//   - $params[] : values checked
//     keys used  : tasklabel
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_project_task_form($pid, $params) {
  global $err, $l_ptask_same_task, $l_fill_tasklabel, $l_ptask_has_user;
  global $l_ptask_has_parent, $l_ptask_and_parent;

  $pid = $params['project_id'];
  $task = $params['task'];
  $ptask = $params['ptask'];
  $tasklabel = $params['tasklabel'];
  $ref_task = $params['reftask_id'];
  // MANDATORY: task label
  if (trim($tasklabel) == '' && !is_array($ref_task)) {
    $err['msg'] = $l_fill_tasklabel;
    return false;
  }

  // If parent task same has task
  if ( (isset($ptask)) && ($ptask == $task)) {
    $err['msg'] = $l_ptask_same_task;
    return false;
  }

  // If parent task is given, it should not have parents
  // Can happen if update from another window simultaneously
  if (project_task_has_parent($ptask)) {
    $err['msg'] = $l_ptask_has_parent;
    return false;
  }

  // If parent task and task is already parent
  if (project_is_task_parent($task)) {
    $task_is_parent = true;
  } else {
    $task_is_parent = false;
  }

  if ( ($task > 0) && $task_is_parent && ($ptask > 0)) {
    $err['msg'] = $l_ptask_and_parent;
    return false;
  }

  // If a parent task has been given, check that there are no links from
  // User to the parent task (a parent task can't be assigned to users)
  if (project_task_has_user($ptask)) {
    $pt_has_user = true;
  } else {
    $pt_has_user = false;
  }

  if ( ($ptask > 0) && $pt_has_user) {
    $err['msg'] = $l_ptask_has_user;
    return false;
  }

  return true; 
}


///////////////////////////////////////////////////////////////////////////////
// Check if a task is a parent task
// Parameters:
//   - $tid : task id
// Returns:
//   - (true | false) : true if task is parent, else false 
///////////////////////////////////////////////////////////////////////////////
function project_is_task_parent($tid) {
  global $cdg_sql;

  if ($tid == '') {
    return false;
  }
  $multidomain = sql_multidomain('project');
	
  $query = "SELECT projecttask_id
    FROM ProjectTask
    JOIN Project on project_id = projecttask_project_id
    WHERE projecttask_parenttask_id = '$tid' $multidomain";

  display_debug_msg($query, $cdg_sql, 'project_is_task_parent()');
  $t_q = new DB_OBM;
  $t_q->query($query);
  
  if ($t_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if a task has a parent task
// Parameters:
//   - $tid : task id
// Returns:
//   - (true | false) : true if task has a parent, else false 
///////////////////////////////////////////////////////////////////////////////
function project_task_has_parent($tid) {
  global $cdg_sql;

  $multidomain = sql_multidomain('project');

  $query = "SELECT projecttask_parenttask_id
    FROM ProjectTask JOIN Project on project_id = projecttask_project_id
    WHERE projecttask_id = '$tid' $multidomain";

  display_debug_msg($query, $cdg_sql, 'project_task_has_parent()');
  $t_q = new DB_OBM;
  $t_q->query($query);
  $t_q->next_record();
  
  if ($t_q->f('projecttask_parenttask_id') > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if a task as some user allocation
// Parameters:
//   - $tid : task id
// Returns:
//   - (true | false) : true if task is assigned to user, else false 
///////////////////////////////////////////////////////////////////////////////
function project_task_has_user($tid) {
  global $cdg_sql;

  $multidomain = sql_multidomain('userobm');

  $query = "SELECT projectuser_id
    FROM ProjectUser JOIN UserObm ON userobm_id = projectuser_user_id
    WHERE projectuser_projecttask_id = '$tid' $multidomain";

  display_debug_msg($query, $cdg_sql, 'project_task_has_user()');
  $t_q = new DB_OBM;
  $t_q->query($query);

  if ($t_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Progress Form Data checking and formatting
// Parameters:
//   - $pid   : project id
//   - $params[] : values checked
//     keys used  : missing
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_project_progress_form($params) {

  $error = 0;

  while ( list($t_id, $misstab) = each($params['missing']) ) {
    while ( list($m_id, $misstime) = each($misstab) ) {
      if (trim($misstime == '')) {
	$error = 1;
      }
    }
  }

  if ($error == 1)
    return false;
  else
    return true;
}


///////////////////////////////////////////////////////////////////////////////
// Timemanager: qet tasks for given projects and month                 
// Parameters:
//   - $time[] : time search criteria
// Returns : DB object result
///////////////////////////////////////////////////////////////////////////////
function run_query_project_month_planning($params) {
  global $cdg_sql, $cdg_param;

  $year = substr($params['date'],0,4);
  $month = substr($params['date'],4,2);
  $day = 1;
  $id = $params['project_id'];
  // First and last days of month
  $month_begin = date('Ymd', mktime(0,0,0,$month, 1, $year));
  $month_end = date('Ymd', mktime(23,0,0,$month+1, $day-1, $year));

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $date = sql_date_format($db_type, 'timetask_date', 'date');
  $multidomain = sql_multidomain('project');

  $query = "
    SELECT
      projecttask_id as id,
      projecttask_label as label,
      sum(timetask_length) timetask_length,
      $date,
      userobm_id,
      userobm_lastname,
      userobm_firstname,      
      timetask_status
    FROM TimeTask JOIN UserObm ON timetask_user_id = userobm_id
    JOIN ProjectTask ON projecttask_id = timetask_projecttask_id
    JOIN Project ON projecttask_project_id = project_id
    WHERE timetask_date >= '$month_begin' 
      AND timetask_date <= '$month_end'
      AND project_id = $id
			$multidomain
      GROUP BY  project_id,project_name,date, timetask_status,id,label,
      userobm_id,userobm_lastname,userobm_firstname
";
  display_debug_msg($query, $cdg_sql, 'run_query_project_month_planning()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q; 
}


///////////////////////////////////////////////////////////////////////////////
// Get Tasks for a given users list                 
// Parameters:
//   - $params[] : time search criteria
// Returns : DB object result
///////////////////////////////////////////////////////////////////////////////
function run_query_project_tasks_by_users($params) {
  global $cdg_sql, $cdg_param;

  $uids = implode(',',$params['user_ids']);
  $multidomain = sql_multidomain('project');
	
  $query = "
  SELECT 
    project_tasktype_id,
    project_id,
    project_name,
    company_name,
    projecttask_id,
    projecttask_label
  FROM  Project
  LEFT JOIN Company ON company_id = project_company_id
  JOIN ProjectTask ON projecttask_project_id = project_id
  JOIN ProjectUser ON projectuser_projecttask_id = projecttask_id
  WHERE project_archive='0'
    AND projectuser_user_id IN ($uids)
    AND projectuser_projectedtime != '0'
    $multidomain
  GROUP BY project_tasktype_id, project_id, project_name, company_name, 
    projecttask_id, projecttask_label
  ORDER BY project_tasktype_id, company_name, project_name";

  display_debug_msg($query, $cdg_sql, 'run_query_project_tasks_by_users()');
  $obm_db = new DB_OBM;
  $obm_db->query($query);

  return $obm_db; 
}


///////////////////////////////////////////////////////////////////////////////
// Get Taskstypes                 
// Parameters:
// Returns : DB object result
///////////////////////////////////////////////////////////////////////////////
function run_query_project_tasktype(){
  global $cdg_sql, $cdg_param;
  
  $multidomain = sql_multidomain('tasktype',false);
	
  $query = "
    SELECT tasktype_id,tasktype_label,tasktype_internal
    FROM TaskType
    WHERE  $multidomain
    ORDER BY tasktype_internal, tasktype_label";

  display_debug_msg($query, $cdg_sql, 'run_query_time_used_tasktype()');
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get Reference Tasks                 
// Parameters:
//   - $params[] : time search criteria
// Returns : DB object result
///////////////////////////////////////////////////////////////////////////////
function run_query_project_reftasks(){
  global $cdg_sql, $cdg_param;

  $multidomain = sql_multidomain('tasktype',false);

  $query = "
  SELECT 
    projectreftask_id,
    projectreftask_label,
    tasktype_id,
    tasktype_label,
    tasktype_internal
  FROM ProjectRefTask
  JOIN TaskType ON projectreftask_tasktype_id = tasktype_id
  WHERE $multidomain 
  GROUP BY projectreftask_id,projectreftask_label,tasktype_id,
    tasktype_label,tasktype_internal
  ORDER BY projectreftask_tasktype_id";

  display_debug_msg($query, $cdg_sql, 'run_query_time_used_tasktype()');
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Create a new ref task                
// Parameters:
//   - $params[] : time search criteria
// Returns : DB object result
///////////////////////////////////////////////////////////////////////////////
function run_query_project_reftask_insert($params) {
  global $cdg_sql, $cdg_param, $obm;

  $uid = $obm['uid'];
  $tasktype = $params['tasktype'];
  $label = $params['reftask_label'];
	
  // TODO faire une fonction check_param_reftask_update
  if (($tasktype == '') or ($label == '')) {
  	return false ;
  }

  $query = "INSERT INTO ProjectRefTask (
    projectreftask_timeupdate,
    projectreftask_timecreate,
    projectreftask_usercreate,
    projectreftask_tasktype_id,
    projectreftask_label)
    VALUE (
    '0',
    NOW(),
    '$uid',
    '$tasktype',
    '$label'
    )";

  display_debug_msg($query, $cdg_sql, 'run_query_project_reftask_insert()');
  $obm_q= new DB_OBM;
  return $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Update a ref task                
// Parameters:
//   - $params[] : time search criteria
// Returns : DB object result
///////////////////////////////////////////////////////////////////////////////
function run_query_project_reftask_update($params) {
  global $cdg_sql, $cdg_param, $obm;

  $uid = $obm['uid'];
  $tasktype = $params['tasktype'];
  $label = $params['reftask_label'];
  $id = $params['reftask'];
  
  // TODO faire une fonction check_param_reftask_update
  if (($tasktype == '') or ($label == '')) {
  	return false ;
  }

  $query = "UPDATE ProjectRefTask 
  SET projectreftask_label = '$label',
    projectreftask_tasktype_id = '$tasktype',
    projectreftask_userupdate = '$uid',
    projectreftask_timeupdate = NOW()
    WHERE projectreftask_id = '$id'";
    
  display_debug_msg($query, $cdg_sql, 'run_query_project_reftask_update()');
  $obm_q = new DB_OBM;
  return $obm_q->query($query);  
}


///////////////////////////////////////////////////////////////////////////////
// Update a ref task                
// Parameters:
//   - $params[] : time search criteria
// Returns : DB object result
///////////////////////////////////////////////////////////////////////////////
function run_query_project_reftask_delete($params) {
  global $cdg_sql, $cdg_param;

  $id = $params['reftask'];

  $query = "DELETE
    FROM ProjectRefTask
    WHERE projectreftask_id = '$id'";

  display_debug_msg($query, $cdg_sql, 'run_query_project_reftask_delete()');
  $obm_q= new DB_OBM;
  return $obm_q->query($query);  
}


///////////////////////////////////////////////////////////////////////////////
// Project Send Mail
// Parameters:
//   - $project[] : project hash info
///////////////////////////////////////////////////////////////////////////////
function project_send_mail($params, $p_id) {
  global $cgp_host, $l_mail_project_subject, $l_mail_project_member, $l_company;
  global $l_name, $l_sold, $l_estimated, $l_datebegin,$l_dateend, $l_tasktype;
  global $l_deal, $l_type;

  if (!$p_id) {
    return 0;
  }
  if (isset($params['member'])) {
    $member = $params['member'];
  } elseif (isset($params['user_id'])) {
    $member = $params['user_id'];
  } else {
    return 0;
  }

  $p = get_project_info($p_id);
  $name = $p['name'];
  $shortname = $p['shortname'];
  $company = $p['company'];
  $deal = $p['deal'];
  $type = $p['dealtype'];
  $tasktype = $p['tasktype'];
  $datebegin = of_isodate_convert($p['datebegin'], true);
  $dateend = of_isodate_convert($p['dateend'], true);
  $sold = $p['soldtime'];
  $estimated = $p['estimatedtime'];
  $comment = $p['comment'];
  $subject = "$l_mail_project_subject : $company : $name";
  $message = "$l_mail_project_member
$cgp_host/project/project_index.php?action=detailconsult&project_id=$p_id

  $l_company : $company
  $l_deal : $deal
  $l_type : $type
  $l_tasktype : $tasktype
  $l_name : $name ($shortname)
  $l_estimated : $estimated
  $l_sold : $sold
  $l_datebegin : $datebegin
  $l_dateend : $dateend

  $comment";
  send_mail($subject, $message, array($member), array(), 1);
}
?>
