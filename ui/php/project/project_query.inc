<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : project_query.inc                                            //
//     - Desc : project query File                                           //
// 2003-07-08 Bastien Continsouzas                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Project search query execution                                            //
// Parametes:
//   - $project[]    : project search criteria
//     keys used       name, company_name, tt, manager, member
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($project, $p_new_order="", $p_order_dir) {
  global $auth, $c_all, $cdg_sql;

  $name = $project["name"];
  $tt = $project["tt"];
  $company_name = $project["company_name"];
  $company = $project["company"];
  $deal = $project["deal"];
  $manager = $project["manager"];
  $member = $project["member"];
  $archive = $project["archive"];
  $date = $project["datebegin"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  $query = "select distinct
      project_id,
      project_name,
      project_soldtime,
      project_estimatedtime,
      company_name as project_company,
      tasktype_label as project_tasktype,
      project_datebegin,
      project_dateend,
      project_archive
    from Project
         left join Company on project_company_id=company_id
         left join Deal on project_deal_id=deal_id,
      TaskType
 ";

  if (($member != "") && ($member != $c_all)) {
    $query .= ", ProjectUser Member";
  }
  
  if (($manager != "") && ($manager != $c_all)) {
    $query .= ", ProjectUser Manager";
  }

  $query .= " where tasktype_id=project_tasktype_id";

  if ($company) {
    $query .= " and project_company_id = '$company'";
  }

  if ($deal) {
    $query .= " and project_deal_id = '$deal'";
  }

  if (!($archive)) {
    $query .= " and project_archive = '0'";
  }

  if ($name != "") {
    $query .= " and project_name $like '$name%'";
  }

  if ($company_name != "") {
    $query .= " and company_name $like '$company_name%'";
  }

  if ($tt != 0) {
    $query .= " and project_tasktype_id = '$tt'";
  }

  if (($manager != "") && ($manager != $c_all)) {
    $query .= " and project_id = Manager.projectuser_project_id
                and Manager.projectuser_user_id = '$manager'
                and Manager.projectuser_manager = 1";
  }

  if (($member != "") && ($member != $c_all)) {
    $query .= " and project_id = Member.projectuser_project_id
                and Member.projectuser_user_id = '$member'";
  }

  if ($date != "") {
    $query .= " and project_datebegin < '$date'
                  and (project_dateend < '$date' or project_dateend is null)";
  }

  // Order by clause
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "project_name";
  $query .= " order by $order $p_order_dir";

  display_debug_msg($query, $cdg_sql, "run_query_search");
  $obm_q->query($query);
  
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Detail query execution (retrieve the main infos about the project)        //
// Parameters :
//   - $p_id  : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $datebegin = sql_date_format($db_type, "project_datebegin", "datebegin");
  $dateend = sql_date_format($db_type, "project_dateend", "dateend");
  $timeupdate = sql_date_format($db_type, "project_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "project_timecreate", "timecreate");

  $query = "select
      project_id,
      project_name,
      project_tasktype_id,
      project_company_id,
      project_deal_id,
      tasktype_label as project_tasktype_label,
      company_name,
      deal_label,
      project_soldtime,
      project_estimatedtime,
      project_archive,
      $timeupdate,
      $timecreate,
      $datebegin,
      $dateend,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    from Project
         left join Company on project_company_id=company_id
         left join Deal on project_deal_id=deal_id
         left join TaskType on project_tasktype_id=tasktype_id
         LEFT JOIN UserObm as c on project_usercreate=c.userobm_id
         LEFT JOIN UserObm as u on project_userupdate=u.userobm_id
    where project_id='$p_id'
    ";

  display_debug_msg($query, $cdg_sql, "run_query_detail");
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Project tasks user allocation and time spend (in time management)
// Different from run_query_allocation as a project member who has already
// spent some time on the project (filled in the time management) but is not
// anymore assigned to some tasks, must appear in the detailed progress table 
// Parameters:
//   - $p_id : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_allocation_and_time($p_id) {
  global $cdg_sql, $c_day_fraction;

  $frac = $c_day_fraction;

  // We first get Times from Allocated Tasks
  $query = "
    select
      userobm_id,
      projecttask_id,
      sum((timetask_length)/$frac) as used_time,
      projectuser_projectedtime as projected_time,
      projectuser_missingtime as missing_time
    from ProjectUser
      left join ProjectTask
        on projectuser_projecttask_id = projecttask_id
      left join UserObm
        on projectuser_user_id = userobm_id
      left join TimeTask
        on timetask_projecttask_id = projecttask_id
        and timetask_user_id = userobm_id
    where projectuser_project_id = '$p_id'
      and projectuser_projectedtime != '0'
      and projectuser_projectedtime is not null
    group by userobm_id, projecttask_id, projected_time, missing_time
    ";

  display_debug_msg($query, $cdg_sql, "run_query_allocation_and_time");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  $times = array();
  $cpt = 0;
  while ($obm_q->next_record()) {
    $times[$cpt]["uid"] = $obm_q->f("userobm_id");
    $times[$cpt]["task_id"] = $obm_q->f("projecttask_id");
    $times[$cpt]["allo"] = $obm_q->f("projected_time");
    $times[$cpt]["miss"] = $obm_q->f("missing_time");
    $times[$cpt]["used"] = $obm_q->f("used_time");
    $cpt++;
  }

  // We query TimeTask data filled but where no allocation is present anymore
  // We start by TimeTask and join to ProjectUser but ProjectUser must be empty
  $query = "
    select
      userobm_id,
      projecttask_id,
      projecttask_project_id, 
      sum((timetask_length)/$frac) as used_time
    from TimeTask
      left join ProjectTask
        on timetask_projecttask_id = projecttask_id
      left join UserObm
        on timetask_user_id = userobm_id
      left join ProjectUser
        on projectuser_projecttask_id = projecttask_id
        and projectuser_user_id = userobm_id
    where projecttask_project_id = '$p_id'
      and projectuser_id is null
    group by userobm_id, projecttask_id, projecttask_project_id
    ";

  display_debug_msg($query, $cdg_sql, "run_query_allocation_and_time (2)");
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $times[$cpt]["uid"] = $obm_q->f("userobm_id");
    $times[$cpt]["task_id"] = $obm_q->f("projecttask_id");
    $times[$cpt]["allo"] = "0";
    $times[$cpt]["miss"] = "0";
    $times[$cpt]["used"] = $obm_q->f("used_time");
    $cpt++;
  }

  return $times;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query execution                                                 //
// Parameters:
//   - $project[] : Entry's values
//     keys used  : name, soldtime, tt
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($project) {
  global $auth, $cdg_sql;

  $uid = $auth->auth["uid"];
  $date_now = date("Y-m-d H:i:s");

  $name = $project["name"];
  $soldtime = ($project["soldtime"] ? "'".$project["soldtime"]."'" : "null");
  $estimated = ($project["estimated"] ? "'".$project["estimated"]."'" : "null");
  $tt = $project["tt"];
  $comp = ($project["company"] ? "'".$project["company"]."'" : "null");
  $deal = ($project["deal"] ? "'".$project["deal"]."'" : "null");
  $com = $project["comment"];
  $task = $project["tasklabel"];
  $member = $project["member"];
  $datebegin = ($project["datebegin"] ? "'".$project["datebegin"]."'" : "null");
  $dateend = ($project["dateend"] ? "'".$project["dateend"]."'" : "null");
  

  $query = "insert into Project
   (project_timeupdate,
    project_timecreate,
    project_userupdate,
    project_usercreate,
    project_name,
    project_tasktype_id,
    project_company_id,
    project_deal_id,
    project_soldtime,
    project_estimatedtime,
    project_datebegin,
    project_dateend,
    project_archive,
    project_comment)
  values (null,
    '$date_now',
    null,
    '$uid',
    '$name',
    '$tt',
    $comp,
    $deal,
    $soldtime,
    $estimated,
    $datebegin,
    $dateend,
    '0',
    '$com')";

  display_debug_msg($query, $cdg_sql, "run_query_insert");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  // get the id of the inserted project. Date is to insure correct project
  // is selected (and not similar from the same deal)
  $query = "select project_id
            from Project
            where project_name = '$name'
              and project_tasktype_id = '$tt'
              and project_timecreate = '$date_now'";

  display_debug_msg($query, $cdg_sql, "run_query_insert (get Id)");
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    $obm_q->next_record();
    $pid = $obm_q->f("project_id");
  } else {
    $pid = 0;
    return $pid;
  }

  // If a task has been given, We create a task for this project
  if ($task != "") {
    $query = "
    insert into ProjectTask (
      projecttask_project_id,
      projecttask_timecreate,
      projecttask_usercreate,
      projecttask_label,
      projecttask_parenttask_id,
      projecttask_rank)
    values (
      '$pid',
      '$date_now',
      '$uid',
      '$task',
      '0',
      '1')
      ";

    display_debug_msg($query, $cdg_sql, "run_query_insert (create task)");
    $retour = $obm_q->query($query);
  }

  // If a member has been given, We insert it
  if ($member > 0) {

    // We affect the member to this project (projecttask null)
    $query = "
      insert into ProjectUser 
        (projectuser_project_id,
         projectuser_user_id,
         projectuser_projecttask_id,
         projectuser_timecreate,
         projectuser_usercreate,
         projectuser_manager
        )
      values
        ('$pid',
        '$member',
        null,
        '$date_now',
        '$uid',
        '1')";

    display_debug_msg($query, $cdg_sql, "run_query_insert (add member)");
    $retour = $obm_q->query($query);

    // We get the projecttask_id if a project task has been filled
    if ($task != "") {
      $query = "
        select projecttask_id
        from ProjectTask
        where projecttask_project_id = '$pid'";

      display_debug_msg($query, $cdg_sql, "run_query_short_create(get task)");
      $obm_q->query($query);
      $obm_q->next_record();
      $pt_id = "'".$obm_q->f("projecttask_id")."'";

      // We affect the member to this projecttask
      $query = "
      insert into ProjectUser 
        (projectuser_project_id,
         projectuser_user_id,
         projectuser_projecttask_id,
         projectuser_timecreate,
         projectuser_usercreate,
         projectuser_projectedtime,
         projectuser_missingtime,
         projectuser_validity
        )
      values
        ('$pid',
        '$member',
        $pt_id,
        '$date_now',
        '$uid',
        $estimated,
        $estimated,
        '$date_now')";

      display_debug_msg($query, $cdg_sql, "run_query_insert (add member)");
      $retour = $obm_q->query($query);
    }
  }

  return $pid;
}


///////////////////////////////////////////////////////////////////////////////
// Update query execution                                                    //
// Parameters:
//   - $pid       : project id
//   - $project[] : Entry's values
//     keys used  : name, soldtime, tt
///////////////////////////////////////////////////////////////////////////////
function run_query_update($pid, $project) {
  global $auth, $cdg_sql;

  $uid = $auth->auth["uid"];
  $name = $project["name"];
  $soldtime = ($project["soldtime"] ? "'".$project["soldtime"]."'" : "null");
  $estimated = ($project["estimated"] ? "'".$project["estimated"]."'" : "null");
  $tt = $project["tt"];
  $datebegin = ($project["datebegin"] ? $project["datebegin"] : "null");
  $dateend = ($project["dateend"] ? $project["dateend"] : "null");
  $archive = $project["archive"];
  if ($archive) {
    $arch = "1";
  } else {
    $arch = "0";
  }

  $query = "update Project set
      project_timeupdate='". date("Y-m-d H:i:s") ."',
      project_userupdate='$uid',
      project_name='$name',
      project_tasktype_id='$tt',
      project_soldtime=$soldtime,
      project_estimatedtime=$estimated,
      project_datebegin='$datebegin',
      project_dateend='$dateend',
      project_archive='$arch'
    where project_id='$pid'";

  display_debug_msg($query, $cdg_sql, "run_query_update");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the timetask entries assigned to the project
// Parameters:
//   - $p_id : project Id
///////////////////////////////////////////////////////////////////////////////
function run_query_project_timetask_links($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $query = "select distinct timetask_id,
      projecttask_label,
      userobm_id,
      userobm_lastname,
      userobm_firstname
    from TimeTask
         left join UserObm on timetask_user_id=userobm_id,
      ProjectTask
    where projecttask_project_id = '$p_id'
      and timetask_projecttask_id = projecttask_id
  ";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution                                                  //
// Parameters:
//   - $p_id : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_id) {
  global $cdg_sql;

  // Check that no Links from Time management
  $obm_q = run_query_project_timetask_links($p_id);

  if ($obm_q->num_rows() > 0) {
    $retour = false;
  } else {
    $obm_q = new DB_OBM;

    // Delete all users associations to the project
    $query = "delete from ProjectUser where projectuser_project_id='$p_id'";
    display_debug_msg($query, $cdg_sql);
    $retour = $obm_q->query($query);

    // Delete all task assigned to the project
    $query = "delete from ProjectTask where projecttask_project_id='$p_id'";
    display_debug_msg($query, $cdg_sql);
    $retour = $obm_q->query($query);

    // Delete the project
    $query = "delete from Project where project_id='$p_id'";
    display_debug_msg($query, $cdg_sql);
    $retour = $obm_q->query($query);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : UserList conditionnal insertion                         //
// Parameters:
//   - $project[] : Entry's values
//     keys used  : id
///////////////////////////////////////////////////////////////////////////////
function run_query_projectname($pid) {
  global $cdg_sql;

  $query = "
    select project_name
    from Project
    where project_id = '$pid'";

  display_debug_msg($query, $cdg_sql, "run_query_projectname");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->nf() != 0) {
    $obm_q->next_record();
    $p_name = $obm_q->f("project_name");
  } else {
    $p_name = "project name error";
  }

  return $p_name;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Task conditionnal insertion                         //
// Parameters:
//   - $project[] : Entry's values
//     keys used  : id, ptask, tasklabel
///////////////////////////////////////////////////////////////////////////////
function run_query_task_insert($project) {
  global $auth, $cdg_sql;

  $pid = $project["id"];
  $ptask = $project["ptask"];
  $tasklabel = $project["tasklabel"];

  $search_q = new DB_OBM;

  // If a parent task has been given, check that there are no links from
  // User to the parent task (a parent task can't be assigned to users)
  if (!(isset($ptask))) {
    $ptask = 0;
  } else {
    $query = "
      select projectuser_user_id
      from ProjectUser
      where projectuser_projecttask_id = '$ptask'
      ";

    display_debug_msg($query, $cdg_sql, "run_query_task_insert(1)");
    $search_q->query($query);
  }

  if (($ptask == 0) or ($search_q->nf() == 0))  {
    $query = "
      insert into ProjectTask (
        projecttask_project_id,
        projecttask_timecreate,
        projecttask_usercreate,
        projecttask_label,
        projecttask_parenttask_id,
        projecttask_rank)
      values (
        '$pid',
        '". date("Y-m-d H:i:s") ."',
        '". $auth->auth["uid"] ."',
        '$tasklabel',
        '$ptask',
        '1')
        ";

    display_debug_msg($query, $cdg_sql, "run_query_task_insert(2)");
    $obm_q = new DB_OBM;
    $obm_q->query($query);
  
    return $obm_q;
  } else {
    return 0;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Task Update
// Parameters:
//   - $project[] : Entry's values
//     keys used  : id, ptask, tasklabel
///////////////////////////////////////////////////////////////////////////////
function run_query_task_update($project) {
  global $auth, $cdg_sql;

  $task = $project["task"];
  $ptask = $project["ptask"];
  $tasklabel = $project["tasklabel"];

  $query = "
      update ProjectTask set
        projecttask_timeupdate='". date("Y-m-d H:i:s") ."',
        projecttask_userupdate='". $auth->auth["uid"] ."',
        projecttask_label='$tasklabel',
        projecttask_parenttask_id='$ptask',
        projecttask_rank='1'
      where
        projecttask_id='$task'
        ";

  display_debug_msg($query, $cdg_sql, "run_query_task_update(2)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Tasklist deletion                                       //
// Parameters:
//   - $project[] : hash info : keys used : id, task_nb, tskX
///////////////////////////////////////////////////////////////////////////////
function run_query_task_delete($project) {
  global $auth, $cdg_sql;

  $id = $project["id"];
  $cpt = 0;
  $cpt_del = 0;
  $delete_ok = true;
  
  while ($cpt < $project["tsk_nb"]) {
    $cpt++;
    $tsk_id = $project["tsk$cpt"];

    // Check that neither the task to be deleted or its children tasks are
    // assigned to users
    $query = "
      select projectuser_user_id
      from ProjectUser, ProjectTask
      where projectuser_projecttask_id = projecttask_id
        and (projecttask_id='$tsk_id' or projecttask_parenttask_id='$tsk_id')
      ";
    
    display_debug_msg($query, $cdg_sql, "run_query_task_delete(1)");
    $search_q = new DB_OBM;
    $search_q->query($query);

    if ($search_q->nf() > 0) {
      $delete_ok = false;
    }

    if ($delete_ok == true) {
      // Check that neither the task to be deleted or its childre tasks are
      // used in time management
      $query = "
        select timetask_id
        from TimeTask, ProjectTask
        where timetask_projecttask_id = projecttask_id
          and (projecttask_id='$tsk_id' or projecttask_parenttask_id='$tsk_id')
        ";
    
      display_debug_msg($query, $cdg_sql, "run_query_task_delete(2)");
      $search_q = new DB_OBM;
      $search_q->query($query);

      if ($search_q->nf() > 0) {
	$delete_ok = false;
      }
    }

    // If the task can be deleted
    if ($delete_ok) {
      $query = "
        delete
        from ProjectTask
        where projecttask_id = '$tsk_id'
          or  projecttask_parenttask_id = '$tsk_id'
        ";

      display_debug_msg($query, $cdg_sql, "run_query_task_delete(3)");
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
//   - $project[] : hash info : keys used : id, mem_nb, memX
// Return: number of users inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_memberlist_insert($project) {
  global $auth, $cdg_sql;

  $id = $project["id"];
  $cpt = 0;
  $cpt_ins = 0;
  
  while ($cpt < $project["mem_nb"]) {
    $cpt++;
    $mem_id = $project["mem$cpt"];

    $query = "
      select *
      from ProjectUser
      where projectuser_project_id = '$id'
        and projectuser_user_id = '$mem_id'";

    display_debug_msg($query, $cdg_sql, "run_query_memberlist_insert(2)");
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $query = "
        insert into ProjectUser 
          (projectuser_user_id,
           projectuser_project_id,
           projectuser_projecttask_id,
           projectuser_timecreate,
           projectuser_usercreate,
           projectuser_projectedtime,
           projectuser_manager
          )
        values
          ('$mem_id',
          '$id',
          null,
          '".date("Y-m-d H:i:s")."',
          ". $auth->auth["uid"] .",
          0,
          0)";

      display_debug_msg($query, $cdg_sql, "run_query_memberlist_insert(3)");
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
//   - $project[] : hash info : keys used : id, mem_nb, memX
///////////////////////////////////////////////////////////////////////////////
function run_query_memberlist_delete($project) {
  global $auth, $cdg_sql;

  $id = $project["id"];
  $cpt = 0;
  $cpt_del = 0;

  $obm_q = new DB_OBM;
  $search_q = new DB_OBM;

  while ($cpt < $project["mem_nb"]) {
    $cpt++;
    $mem_id = $project["mem$cpt"];

    // Check if the user has filled time management for this project
    $query = "
        select timetask_id
        from TimeTask, ProjectTask
        where timetask_user_id = '$mem_id'
          and timetask_projecttask_id = projecttask_id
          and projecttask_project_id = '$id'
        ";
    
    display_debug_msg($query, $cdg_sql, "run_query_member_delete(2b)");
    $search_q->query($query);
    
    if ($search_q->nf() == 0) {
      $query = "
        delete
        from ProjectUser
        where projectuser_project_id = '$id'
          and projectuser_user_id = '$mem_id'
        ";
      
      display_debug_msg($query, $cdg_sql, "run_query_memberlist_delete(3b)");
      $retour = $obm_q->query($query);
      
      if ($retour)
	$cpt_del++;
    }
  }

  return $cpt_del;
}


///////////////////////////////////////////////////////////////////////////////
// Project Update query execution                                            //
// Parameters:
//   - $project[] : hash info : user_id, user_status
///////////////////////////////////////////////////////////////////////////////
function run_query_memberstatus_change($project) {
  global $cdg_sql;

  $p_id = $project["id"];
  $m_id = $project["user_id"];
  $m_val = $project["user_status"];

  $query = "
    update ProjectUser
    set projectuser_manager = '$m_val'
    where projectuser_user_id = '$m_id'
      and projectuser_project_id = '$p_id'
      and projectuser_projecttask_id is null";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "run_query_memberstatus_change");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Project tasks user allocation
// Parameters:
//   - $p_id : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_allocation($p_id) {
  global $cdg_sql, $c_day_fraction;

  $frac = $c_day_fraction;

  $query = "
    select
      userobm_id,
      projectuser_projecttask_id,
      projectuser_projectedtime as projected_time,
      projectuser_missingtime as missing_time
    from ProjectUser
      left join ProjectTask
        on projectuser_projecttask_id = projecttask_id
      left join UserObm
        on projectuser_user_id = userobm_id
    where projectuser_project_id = '$p_id'
      and projectuser_projectedtime is not null
    group by userobm_id, projectuser_projecttask_id, projected_time, missing_time
    ";

  display_debug_msg($query, $cdg_sql, "run_query_allocation");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Project Allocation Update query execution                                 //
// Parameters:
//   - $project[] : hash info : keys used : projected
///////////////////////////////////////////////////////////////////////////////
function run_query_allocate_update($project) {
  global $auth, $cdg_sql;

  $p_id = $project["id"];
  $new = $project["projected"];
  $uid = $auth->auth["uid"];  
  $date_now = date("Y-m-d H:i:s");

  $query = "
    select
      projectuser_user_id as mt_user,
      projectuser_projecttask_id as mt_task,
      projectuser_projectedtime as mt_proj,
      projectuser_missingtime as mt_miss
    from ProjectUser
    where projectuser_project_id = '$p_id'
    order by mt_user, mt_task";

  $old_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "run_query_allocate_update(old)");
  $old_q->query($query);

  while ($old_q->next_record()) {
    $old_user = $old_q->f("mt_user");
    $old_task = $old_q->f("mt_task");
    $old_proj = $old_q->f("mt_proj");
    $old_miss = $old_q->f("mt_miss");
    
    $old[$old_user][$old_task] = $old_proj;
    $miss_tab[$old_user][$old_task] = $old_miss;
  }
  
  $obm_q = new DB_OBM;
  
  while ( list($m_id, $ttime) = each($new) ) {
    while ( list($t_id, $proj) = each($ttime) ) {

      $tnew = ($new[$m_id][$t_id] != "")  ? 1 : 0;
      $told = (isset ($old[$m_id][$t_id])) ? 1 : 0;
    
      // if an allocation existed, but has been removed
      if (!($tnew) and ($told)) {
	$query = "
        delete from ProjectUser
        where projectuser_user_id = '$m_id'
          and projectuser_projecttask_id = '$t_id'";

 	display_debug_msg($query, $cdg_sql, "run_query_allocate_update(2)");
	$obm_q->query($query);

      // if an allocation existed, and a new has been given
      } else if (($tnew) and ($told)) {
	// if missing time was not set or never updated (= old projected)
	// we set missing time to new projected
	if ( (! isset($miss_tab[$m_id][$t_id]))
             || ($miss_tab[$m_id][$t_id] == $old[$m_id][$t_id]) ) {
	  $set_miss_time = ", projectuser_missingtime = '$proj'";
	} else {
	  $set_miss_time = "";
	}

	$query = "
         update ProjectUser
         set projectuser_timeupdate = '$date_now',
             projectuser_userupdate = '$uid',
             projectuser_projectedtime = '$proj'
             $set_miss_time
         where projectuser_user_id = '$m_id'
           and projectuser_projecttask_id = '$t_id'";

	display_debug_msg($query, $cdg_sql, "run_query_allocate_update(3)");
	$obm_q->query($query);

      // if a new allocation has been given
      } else if (($tnew) and !($told)){

	$query = "	
        insert into ProjectUser 
          (projectuser_user_id,
           projectuser_project_id,
           projectuser_projecttask_id,
           projectuser_timecreate,
           projectuser_usercreate,
           projectuser_projectedtime,
           projectuser_missingtime
          )
        values
          ('$m_id',
           '$p_id',
           '$t_id',
           '$date_now',
           '$uid',
           '$proj',
           '$proj'
          )";

 	display_debug_msg($query, $cdg_sql, "run_query_allocate_update(4)");
	$obm_q->query($query);
      }
    }
  }
 
  return $ins_err;
}


///////////////////////////////////////////////////////////////////////////////
// Advance Update query execution                                            //
// Parameters:
//   - $project[] : hash info : keys used : missing
///////////////////////////////////////////////////////////////////////////////
function run_query_progress($project) {
  global $auth, $cdg_sql;

  $date_now = date("Y-m-d H:i:s");
  $obm_q = new DB_OBM;
  
  // Update project estimated missing times
  while ( list($t_id, $misstab) = each($project["missing"]) ) {
    while ( list($m_id, $misstime) = each($misstab) ) {
      if ($misstime != "") {
	
	$query = "
          update ProjectUser set
            projectuser_timeupdate = '$date_now',
            projectuser_userupdate = '". $auth->auth["uid"] ."',
            projectuser_validity = '$date_now',
            projectuser_missingtime = '$misstime'
          where projectuser_projecttask_id = '$t_id'
            and projectuser_user_id = '$m_id'
          ";
	
	display_debug_msg($query, $cdg_sql, "run_query_progress");
	$retour = $obm_q->query($query);
	
	if (!($retour))
	  $ins_err = 1;
      }
    }
  }
  
  return $ins_err;
}


///////////////////////////////////////////////////////////////////////////////
// Stat Log query execution (creates an entry in the project progress log)   //
// Parameters:
//   - $p_id      : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_statlog($p_id) {
  global $auth, $cdg_sql, $c_day_fraction;

  $uid = $auth->auth["uid"];
  $frac = $c_day_fraction;

  $query = "
    select (sum(timetask_length)/8) as time_used
      from TimeTask, ProjectTask
    where timetask_projecttask_id = projecttask_id
      and projecttask_project_id = '$p_id'
    group by projecttask_project_id";

  display_debug_msg($query, $cdg_sql, "run_query_statlog(1)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $time_used = $obm_q->f("time_used");
  if ($time_used == "")  $time_used = 0;

  $query = "select sum(projectuser_missingtime) as time_left
            from ProjectUser
            where projectuser_project_id = '$p_id'
            group by projectuser_project_id";

  display_debug_msg($query, $cdg_sql, "run_query_statlog(2)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $time_left = $obm_q->f("time_left");

  $query = "
    insert into ProjectStat (
      projectstat_project_id,
      projectstat_usercreate,
      projectstat_date,
      projectstat_useddays,
      projectstat_remainingdays)
    values (
      '$p_id',
      '$uid',
      '". date("Y-m-d H:i:s") ."',
      '$time_used',
      '$time_left'
    )";

   display_debug_msg($query, $cdg_sql, "run_query_statlog(4)");
   $obm_q = new DB_OBM;
   $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Project tasks query
// Parameters :
//   - $pid : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_tasks($pid) {
  global $cdg_sql;

  $query = "
    select
      projecttask_id,
      projecttask_label,
      projecttask_parenttask_id as parent_id,
      CASE when projecttask_parenttask_id=0 THEN projecttask_id
           else projecttask_parenttask_id
      END as parent_group
    from ProjectTask
    where projecttask_project_id = '$pid'
    order by parent_group, parent_id, projecttask_rank
    ";

  display_debug_msg($query, $cdg_sql, "run_query_tasks");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Project members query
// If no project id given, return the list of user referenced in projects
// Parameters :
//   - $p_id : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_members($p_id = 0) {
  global $cdg_sql;
  
  if ($p_id) {
    $query = "
      select
        userobm_id as member_id,
        userobm_firstname as member_firstname,
        userobm_lastname as member_lastname,
        projectuser_manager as member_manager
      from ProjectUser
           left join UserObm on projectuser_user_id = userobm_id
      where projectuser_project_id = '$p_id'
        and projectuser_projecttask_id is null
        and userobm_archive = 0
      group by member_id, member_firstname, member_lastname, member_manager
      order by member_lastname
      ";
  } else { 
    $query = "
      select
        userobm_id as member_id,
        userobm_firstname as member_firstname,
        userobm_lastname as member_lastname
      from ProjectUser
           left join UserObm on projectuser_user_id = userobm_id
      where userobm_archive = 0
        and projectuser_projecttask_id is null
      group by member_id, member_firstname, member_lastname
      order by member_lastname
      ";
  }  
  display_debug_msg($query, $cdg_sql, "run_query_members");
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
  
  $query = "
    select
      projectuser_user_id
    from ProjectUser
      where projectuser_project_id = '$p_id'
      "; 
  display_debug_msg($query, $cdg_sql, "run_query_participants(1)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  $users_arr = array();
  while ($obm_q->next_record()) {
    $u_id = $obm_q->f("projectuser_user_id");
    array_push($users_arr, $u_id);
  }

 $query = "
    select
      timetask_user_id
    from TimeTask, ProjectTask
      where timetask_projecttask_id = projecttask_id
        and projecttask_project_id = '$p_id'
      "; 
  display_debug_msg($query, $cdg_sql, "run_query_participants(2)");
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $u_id = $obm_q->f("timetask_user_id");
    array_push($users_arr, $u_id);
  }

  $users = array_unique ($users_arr);

  return $users;
}


///////////////////////////////////////////////////////////////////////////////
// Project managers query
// Parameters :
//   - $p_id      : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_managers($p_id = 0) {
  global $cdg_sql;

  $query = "select userobm_id as manager_id,
      userobm_firstname as manager_firstname,
      userobm_lastname as manager_lastname
    from ProjectUser, UserObm
    where projectuser_user_id = userobm_id
      and projectuser_manager = 1 ";
  
  if ($p_id)
    $query .= "and projectuser_project_id = '$p_id' ";

  $query .= "group by manager_id, manager_firstname, manager_lastname
             order by manager_lastname";

  display_debug_msg($query, $cdg_sql, "run_query_managers");
  $obm_q = new DB_OBM;
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


///////////////////////////////////////////////////////////////////////////////
// New Project Form Data checking and formatting                             //
// Parameters:
//   - $pid   : project id
//   - $project[] : values checked
//     keys used  : name, tt, soldtime
// Returns:
//   - (true | false) : true if user have write rights for this project 
///////////////////////////////////////////////////////////////////////////////
function manager_rights($uid, $project, $project_q) {
  global $cdg_sql, $perms;

  if ($perms == "admin") {
    return true;
  }

  $pid = $project["id"];

  $query = "
    select projectuser_id
    from ProjectUser
    where projectuser_project_id = '$pid'
      and projectuser_manager != '0'";

  display_debug_msg($query, $cdg_sql, "manager_rights(managers)");
  $managers_q = new DB_OBM;
  $managers_q->query($query);

  $manager_rights = 
    ( ( (is_object($project_q)) && ( ($project_q->f("usercreate") == $uid) ) )
    or ($project_q->f("userupdate") == $uid) 
    or ($managers_q->nf() != 0) ) ? true : false;

  return $manager_rights;
}


///////////////////////////////////////////////////////////////////////////////
// New Project Form Data checking and formatting                             //
// Parameters:
//   - $pid       : project id (empty on insertion)
//   - $project[] : values checked
//     keys used  : name, tt, soldtime
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_project_form($pid, $project) {
  global $l_fill_name, $l_fill_tt, $l_fill_estimated, $l_fill_soldtime;
  global $err_msg, $php_regexp_number;

  $name = $project["name"];
  $company = $project["company"];
  $tt = $project["tt"];
  $soldtime = $project["soldtime"];
  $estimated = $project["estimated"];
  $member = $project["member"];

  // MANDATORY: Project name
  if (trim($name) == "") {
    $err_msg = $l_fill_name;
    return false;
  }

  // MANDATORY: TaskType selection
  if ($tt == 0) {
    $err_msg = $l_fill_tt;
    return false;
  }

  // MANDATORY: Soldtime
  if (($company > 0) && (trim($soldtime) == "")) {
    $err_msg = $l_fill_soldtime;
    return false;
  }

  // MANDATORY: Estimated
  if (trim($estimated) == "") {
    $err_msg = $l_fill_estimated;
    return false;
  }

  // MANDATORY: Soldtime
//   if (ereg($php_regexp_number, $soldtime) == false) {
//     $err_msg = $l_fill_soldnumber ."".$soldtime."".$php_regexp_number;
//     return false;
//   }

  return true; 
}


///////////////////////////////////////////////////////////////////////////////
// Project Task Form Data checking and formatting                            //
// Parameters:
//   - $pid   : project id
//   - $project[] : values checked
//     keys used  : tasklabel
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_task_form($pid, $project) {
  global $err_msg, $l_ptask_same_task, $l_fill_tasklabel, $l_ptask_has_user;
  global $l_ptask_has_parent, $l_ptask_and_parent;

  $pid = $project["id"];
  $task = $project["task"];
  $ptask = $project["ptask"];
  $tasklabel = $project["tasklabel"];

  // MANDATORY: task label
  if (trim($tasklabel) == "") {
    $err_msg = $l_fill_tasklabel;
    return false;
  }

  // If parent task same has task
  if ( (isset($ptask)) && ($ptask == $task)) {
    $err_msg = $l_ptask_same_task;
    return false;
  }

  // If parent task is given, it should not have parents
  // Can happen if update from another window simultaneously
  if (task_has_parent($ptask)) {
    $err_msg = $l_ptask_has_parent;
    return false;
  }

  // If parent task and task is already parent
  if (is_task_parent($task)) {
    $task_is_parent = true;
  } else {
    $task_is_parent = false;
  }

  if ( ($task > 0) && $task_is_parent && ($ptask > 0)) {
    $err_msg = $l_ptask_and_parent;
    return false;
  }

  // If a parent task has been given, check that there are no links from
  // User to the parent task (a parent task can't be assigned to users)
  if (task_has_user($ptask)) {
    $pt_has_user = true;
  } else {
    $pt_has_user = false;
  }

  if ( ($ptask > 0) && $pt_has_user) {
    $err_msg = $l_ptask_has_user;
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
function is_task_parent($tid) {
  global $cdg_sql;

  $query = "select projecttask_id
    from ProjectTask
    where projecttask_parenttask_id = '$tid'
    ";

  display_debug_msg($query, $cdg_sql, "is_task_parent");
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
function task_has_parent($tid) {
  global $cdg_sql;

  $query = "select projecttask_parenttask_id
    from ProjectTask
    where projecttask_id = '$tid'
    ";

  display_debug_msg($query, $cdg_sql, "task_has_parent");
  $t_q = new DB_OBM;
  $t_q->query($query);
  $t_q->next_record();
  
  if ($t_q->f("projecttask_parenttask_id") > 0) {
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
function task_has_user($tid) {
  global $cdg_sql;

  $query = "select projectuser_id
    from ProjectUser
    where projectuser_projecttask_id = '$tid'
    ";

  display_debug_msg($query, $cdg_sql, "task_has_user");
  $t_q = new DB_OBM;
  $t_q->query($query);

  if ($t_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Advance Form Data checking and formatting                                 //
// Parameters:
//   - $pid   : project id
//   - $project[] : values checked
//     keys used  : missing
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_advance_form($project) {

  $error = 0;

  while ( list($t_id, $misstab) = each($project["missing"]) ) {
    while ( list($m_id, $misstime) = each($misstab) ) {
      if (trim($misstime == "")) {
	$error = 1;
      }
    }
  }

  if ($error == 1)
    return false;
  else
    return true;
}

</script>
