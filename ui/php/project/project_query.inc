<SCRIPT language="php">
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
  $company_name = $project["company_name"];
  $tt = $project["tt"];
  $manager = $project["manager"];
  $member = $project["member"];
  $new = $project["newlist"];
  $archive = $project["archive"];

  $query = "select distinct
      deal_id as project_id,
      deal_label as project_label,
      company_name as project_company_name,
      tasktype_label as project_tasktype,
      deal_project_status as project_status,
      deal_archive as project_archive
    from Deal, TaskType";

//   if (($member != $c_all) || ($manager != $c_all))
//   $query .= ", ProjectTask";

  if ($member != $c_all)
  $query .= ", ProjectUser Member";
  
  if ($manager != $c_all)
  $query .= ", ProjectUser Manager";
  
  $query .= " left join Company on deal_company_id=company_id
              where tasktype_id=deal_tasktype_id";

  if (!($new)) {
    $query .= " and deal_project_status != 0";
  }

  if (!($archive)) {
    $query .= " and deal_archive = 0";
  }

  if ($name != "") {
    $query .= " and deal_label like '$name%'";
  }

  if ($company_name != "") {
    $query .= " and company_name like '$company_name%'";
  }

  if ($tt != 0) {
    $query .= " and deal_tasktype_id = $tt";
  }

  if ($manager != $c_all) {
    $query .= " and deal_id = Manager.projectuser_deal_id
                and Manager.projectuser_user_id = $manager";
    //     $query .= " and deal_id = projecttask_deal_id
    //                 and projecttask_id = Manager.projectuser_projecttask_id
    //                 and Manager.projectuser_manager = 1
    //                 and Manager.projectuser_user_id = $manager";
  }

  if ($member != $c_all) {
    $query .= " and deal_id = Member.projectuser_deal_id
                and Member.projectuser_user_id = $member";
    //     $query .= " and deal_id = projecttask_deal_id
    //                 and projecttask_id = Member.projectuser_projecttask_id
    //                 and Member.projectuser_user_id = $member";
  }

  // Order by clause
  $order=(strcmp($p_new_order,"") != 0) ? $p_new_order : "deal_label";
  $query .= " order by $order $p_order_dir";

  display_debug_msg($query, $cdg_sql, "run_query_search");
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Detail query execution (retrieve the main infos about the project)        //
// Parameters :
//   - $p_id  : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_id) {
  global $cdg_sql;

  $query = "select
      deal_id as project_id,
      deal_label as project_label,
      deal_project_status as project_status,
      deal_archive as project_archive,
      deal_visibility as project_visibility,
      company_name as project_company_name,
      tasktype_id as project_tasktype_id,
      tasktype_label as project_tasktype_label,
      deal_soldtime as project_soldtime,
      deal_userupdate as userupdate,
      deal_usercreate as usercreate,
      UNIX_TIMESTAMP(deal_timeupdate) as timeupdate,
      UNIX_TIMESTAMP(deal_timecreate) as timecreate
    from Deal, TaskType
      left join Company on deal_company_id=company_id
    where deal_id=$p_id
      and tasktype_id=deal_tasktype_id";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "run_query_detail");
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query execution                                                 //
// Parameters:
//   - $project[] : Entry's values
//     keys used  : name, soldtime, tt
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($project) {
  global $auth, $cdg_sql;

  $name = $project["name"];
  $soldtime = $project["soldtime"];
  $tt = $project["tt"];

  $query = "insert into Deal
   (deal_timeupdate,
    deal_timecreate,
    deal_userupdate,
    deal_usercreate,
    deal_label,
    deal_tasktype_id,
    deal_soldtime,
    deal_project_status)
  values (null,
    '" . date("Y-m-d H:i:s") ."',
    null,
    '" . $auth->auth["uid"] . "',
    '$name',
    '$tt',
    $soldtime,
    2)";

  display_debug_msg($query, $cdg_sql, "run_query_insert");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  // search the id of the inserted deal
  $query = "select deal_id
            from Deal
            where deal_label = '$name'";

  display_debug_msg($query, $cdg_sql, "run_query_insert (search)");
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    $obm_q->next_record();
    $pid = $obm_q->f("deal_id");
  } else {
    $pid = 0;
  }

  return $pid;
}


///////////////////////////////////////////////////////////////////////////////
// Creat query execution (change state of a deal from 0 to 1                 //
// Parameters:
//   - $project[] : Entry's values
//     keys used  : id, soldtime
///////////////////////////////////////////////////////////////////////////////
function run_query_create($project) {
  global $auth, $cdg_sql;

  $p_id = $project["id"];
  $soldtime = $project["soldtime"];

  $query = "update Deal
    set
      deal_timeupdate='". date("Y-m-d H:i:s") ."',
      deal_userupdate='". $auth->auth["uid"] ."',
      deal_soldtime = $soldtime,
      deal_project_status = 1
    where deal_id = $p_id";

  display_debug_msg($query, $cdg_sql, "run_query_create");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Update query execution                                                    //
// Parameters:
//   - $pid       : project id
//   - $project[] : Entry's values
//     keys used  : name, soldtime, tt, status
///////////////////////////////////////////////////////////////////////////////
function run_query_update($pid, $project) {
  global $auth, $cdg_sql;

  $state = $project["status"];
  $soldtime = $project["soldtime"];
  $name = $project["name"];
  $tt = $project["tt"];
  $archive = $project["archive"];

  $query = "update Deal set
      deal_timeupdate='". date("Y-m-d H:i:s") ."',
      deal_userupdate='". $auth->auth["uid"] ."',
      deal_soldtime=$soldtime";

  if ($archive)
    $query .= ", deal_archive = 1";
  else
    $query .= ", deal_archive = 0";

  if ($state == 2)
    $query .= ", deal_label='$name'
               , deal_tasktype_id=$tt";
  
  $query .= " where deal_id='$pid'";

  display_debug_msg($query, $cdg_sql, "run_query_update");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Close query execution (mark a project as finished                         //
// Parameters:
//   - $pid       : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_close($pid) {
  global $auth, $cdg_sql;

  $query = "update Deal
    set
      deal_timeupdate='". date("Y-m-d H:i:s") ."',
      deal_userupdate='". $auth->auth["uid"] ."',
      deal_project_status=3
    where deal_id='$pid'";

  display_debug_msg($query, $cdg_sql, "run_query_close");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Query execution : UserList conditionnal insertion                         //
// Parameters:
//   - $project[] : Entry's values
//     keys used  : id
///////////////////////////////////////////////////////////////////////////////
function run_query_projectname($pid) {

  $query = "
    select deal_label as project_label
    from Deal
    where deal_id = $pid";

  display_debug_msg($query, $cdg_sql, "run_query_projectname");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->nf() != 0) {
    $obm_q->next_record();
    $p_name = $obm_q->f("project_label");
  } else {
    $p_name = "project name error";
  }

  return $p_name;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : UserList conditionnal insertion                         //
// Parameters:
//   - $project[] : Entry's values
//     keys used  : id, ptask, tasklabel
///////////////////////////////////////////////////////////////////////////////
function run_query_tasklist_insert($project) {
  global $auth, $cdg_sql;

  $pid = $project["id"];
  $ptask = $project["ptask"];
  $tasklabel = $project["tasklabel"];

  $search_q = new DB_OBM;


  //bcontins verifie que la tache n'apparait pas dans projectuser (verifier timetask aussi ?)
  if (!(isset($ptask)))
    $ptask = 0;
  else {
    $query = "
      select projectuser_user_id
      from ProjectUser
      where projectuser_projecttask_id = $ptask
      ";

    display_debug_msg($query, $cdg_sql, "run_query_tasklist_insert(1)");
    $search_q->query($query);
  }

  if (($search_q->nf() == 0) or ($ptask == 0))  {
    $query = "
      insert into ProjectTask (
        projecttask_deal_id,
        projecttask_timecreate,
        projecttask_usercreate,
        projecttask_label,
        projecttask_parenttask_id,
        projecttask_rank)
      values (
        $pid,
        '". date("Y-m-d H:i:s") ."',
        '". $auth->auth["uid"] ."',
        '$tasklabel',
        $ptask,
        1)
        ";

  display_debug_msg($query, $cdg_sql, "run_query_tasklist_insert(2)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  
  return $obm_q;
  } else {
    return 0;
  }
}

///////////////////////////////////////////////////////////////////////////////
// Query execution : Tasklist deletion                                       //
// Parameters:
//   - $project[] : deal hash info : keys used : id, task_nb, tskX
///////////////////////////////////////////////////////////////////////////////
function run_query_tasklist_delete($project) {
  global $auth, $cdg_sql;

  $id = $project["id"];
  $cpt = 0;
  $cpt_del = 0;
  
  while ($cpt < $project["tsk_nb"]) {
    $cpt++;
    $tsk_id = $project["tsk$cpt"];

    $query = "
      select projectuser_user_id
      from ProjectUser, ProjectTask
      where projectuser_projecttask_id = projecttask_id
        and (projecttask_id = $tsk_id  or projecttask_parenttask_id = $tsk_id)
      ";
    
    display_debug_msg($query, $cdg_sql, "run_query_tasklist_delete(1)");
    $search_q = new DB_OBM;
    $search_q->query($query);
    
    if ($search_q->nf() == 0) {
      
      $query = "
        delete
        from ProjectTask
        where projecttask_id = $tsk_id
          or  projecttask_parenttask_id = $tsk_id
        ";
      
      display_debug_msg($query, $cdg_sql, "run_query_tasklist_delete(2)");
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      
      if ($retour)
	$cpt_del++;
    }
  }

  return $cpt_del;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : UserList conditionnal insertion                         //
// Parameters:
//   - $project[] : deal hash info : keys used : id, mem_nb, memX
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
      where projectuser_deal_id = $id
        and projectuser_user_id = $mem_id";

    display_debug_msg($query, $cdg_sql, "run_query_memberlist_insert(2)");
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $query = "
        insert into ProjectUser 
          (projectuser_user_id,
           projectuser_deal_id,
           projectuser_timecreate,
           projectuser_usercreate,
           projectuser_projectedtime,
           projectuser_manager
          )
        values
          ($mem_id,
          $id,
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
// Query execution : ContactList deletion                                    //
// Parameters:
//   - $project[] : deal hash info : keys used : id, mem_nb, memX
///////////////////////////////////////////////////////////////////////////////
function run_query_memberlist_delete($project, $all = 0) {
  global $auth, $cdg_sql;

  $id = $project["id"];
  $cpt = 0;
  $cpt_del = 0;

  $obm_q = new DB_OBM;
  $search_q = new DB_OBM;

  if ($all == 1) {
    $query = "
        delete
        from ProjectUser
        where projectuser_deal_id = $id
        ";

    display_debug_msg($query, $cdg_sql, "run_query_memberlist_delete(2)");
    $retour = $obm_q->query($query);
    
  } else {
    
    while ($cpt < $project["mem_nb"]) {
      $cpt++;
      $mem_id = $project["mem$cpt"];

      $query = "
        select timetask_id
        from TimeTask, ProjectTask
        where timetask_user_id = $mem_id
          and timetask_projecttask_id = projecttask_id
          and projecttask_deal_id = $id
        ";

      display_debug_msg($query, $cdg_sql, "run_query_member_delete(2b)");
      $search_q->query($query);

      if ($search_q->nf() == 0) {
	$query = "
        delete
        from ProjectUser
        where projectuser_deal_id = $id
          and projectuser_user_id = $mem_id
        ";

	display_debug_msg($query, $cdg_sql, "run_query_memberlist_delete(3b)");
	$retour = $obm_q->query($query);

	if ($retour)
	  $cpt_del++;
      }
    }
  }

  return $cpt_del;
}

///////////////////////////////////////////////////////////////////////////////
// Project Update query execution                                            //
// Parameters:
//   - $project[] : deal hash info : user_id, user_status
///////////////////////////////////////////////////////////////////////////////
function run_query_memberstatus_change($project) {
  $p_id = $project["id"];
  $m_id = $project["user_id"];
  $m_val = $project["user_status"];

  $query = "
    update ProjectUser
    set projectuser_manager = $m_val
    where projectuser_user_id = $m_id
      and projectuser_deal_id = $p_id";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "run_query_memberstatus_change");
  $obm_q->query($query);

  return $obm_q;
}
///////////////////////////////////////////////////////////////////////////////
// Project Update query execution                                            //
// Parameters:
//   - $project[] : deal hash info : keys used : projected
///////////////////////////////////////////////////////////////////////////////
function run_query_projectupdate($project) {
  global $auth, $cdg_sql;

  $p_id = $project["id"];
  $new = $project["projected"];
  
  $query = "
    select
      projectuser_user_id as mt_user,
      projectuser_projecttask_id as mt_task,
      projectuser_projectedtime as mt_proj,
      projectuser_missingtime as mt_miss
  from ProjectUser
  where projectuser_deal_id = $p_id
  order by mt_user, mt_task";

  $old_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "run_query_projectupdate(old)");
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
    
      if (!($tnew) and ($told)) {

	$query = "
        delete from ProjectUser
        where projectuser_user_id = $m_id
          and projectuser_projecttask_id = $t_id";

 	display_debug_msg($query, $cdg_sql, "run_query_projectupdate");
	$obm_q->query($query);

      } else if (($tnew) and ($told)) {

	$set_miss_time = (isset($miss_tab[$m_id][$t_id])) ? "" : ", projectuser_missingtime = $proj";

	$query = "
         update ProjectUser
         set projectuser_timeupdate = '".date("Y-m-d H:i:s")."',
             projectuser_userupdate = ". $auth->auth["uid"] .",
             projectuser_projectedtime = $proj
             $set_miss_time
         where projectuser_user_id = $m_id
           and projectuser_projecttask_id = $t_id";

	display_debug_msg($query, $cdg_sql, "run_query_projectupdate");
	$obm_q->query($query);

      } else if (($tnew) and !($told)){

	$query = "	
        insert into ProjectUser 
          (projectuser_user_id,
           projectuser_deal_id,
           projectuser_projecttask_id,
           projectuser_timecreate,
           projectuser_usercreate,
           projectuser_projectedtime,
           projectuser_missingtime
          )
        values
          ($m_id,
           $p_id,
           $t_id,
           '".date("Y-m-d H:i:s")."',
           ". $auth->auth["uid"] .",
           $proj,
           $proj
          )";

 	display_debug_msg($query, $cdg_sql, "run_query_projectupdate");
	$obm_q->query($query);
      }
    }

    $query = "
      delete from ProjectUser
      where projectuser_user_id = $m_id
        and projectuser_deal_id = $p_id
        and projectuser_projecttask_id is null";
  
    display_debug_msg($query, $cdg_sql, "run_query_projectupdate");
    $obm_q->query($query);
  }
 
  return $ins_err;
}


///////////////////////////////////////////////////////////////////////////////
// Advance Update query execution                                            //
// Parameters:
//   - $project[] : deal hash info : keys used : missing
///////////////////////////////////////////////////////////////////////////////
function run_query_advanceupdate($project) {
  global $auth, $cdg_sql;

  $obm_q = new DB_OBM;
  
  // Update project estimated missing times
  while ( list($t_id, $misstab) = each($project["missing"]) ) {
    while ( list($m_id, $misstime) = each($misstab) ) {
      if ($misstime != "") {
	
	$query = "
          update ProjectUser set
            projectuser_timeupdate='". date("Y-m-d H:i:s") ."',
            projectuser_userupdate='". $auth->auth["uid"] ."',
            projectuser_validity='". date("Y-m-d H:i:s") ."',
            projectuser_missingtime =  $misstime
          where projectuser_projecttask_id = $t_id
            and projectuser_user_id = $m_id
          ";
	
	display_debug_msg($query, $cdg_sql, "run_query_advanceupdate");
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

  $frac = $c_day_fraction;

  $query = "
    select (sum(timetask_length)/8) as time_used
      from TimeTask, ProjectTask
    where timetask_projecttask_id = projecttask_id
      and projecttask_deal_id = $p_id
    group by projecttask_deal_id";

  display_debug_msg($query, $cdg_sql, "run_query_statlog(1)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $time_used = $obm_q->f("time_used");
  if ($time_used == "")  $time_used = 0;

  $query = "select sum(projectuser_missingtime) as time_left
            from ProjectUser
            where projectuser_deal_id = $p_id
            group by projectuser_deal_id";

  display_debug_msg($query, $cdg_sql, "run_query_statlog(2)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $time_left = $obm_q->f("time_left");

  $query = "
    insert into ProjectStat (
      projectstat_deal_id,
      projectstat_date,
      projectstat_timecreate,
      projectstat_usercreate,
      projectstat_useddays,
      projectstat_remainingdays)
    values (
      $p_id,
      '". date("Y-m-d H:i:s") ."',
      '". date("Y-m-d H:i:s") ."',
      ". $auth->auth["uid"] .",
      $time_used,
      $time_left
    )";

   display_debug_msg($query, $cdg_sql, "run_query_statlog(4)");
   $obm_q = new DB_OBM;
   $retour = $obm_q->query($query);

  return $retour;
}

// bcontins : pour les projets internes : non géré sur la 0.7?
///////////////////////////////////////////////////////////////////////////////
// Deletion query execution                                                  //
// Parameters:
//   - $p_id : company id
///////////////////////////////////////////////////////////////////////////////
// function run_query_delete($p_id) {
//   global $cdg_sql;

//   $query = "delete from  where ";

//   display_debug_msg($query, $cdg_sql);
//   $obm_q = new DB_OBM;
//   $retour = $obm_q->query($query);

//   return $retour;
// }


///////////////////////////////////////////////////////////////////////////////
// Return all the project that are still in commercial state
// Parameters :
///////////////////////////////////////////////////////////////////////////////
function run_query_newprojects($project) {
  global $cdg_sql;

  $name = $project["name"];
  $tt = $project["tt"];

  $query = "
    select deal_id as project_id,
      deal_project_status as project_status,
      deal_label as project_initlabel,
      company_name as project_company_name,
      tasktype_label as project_tasktype
    from Deal, Company, TaskType
    where deal_company_id = company_id
      and deal_tasktype_id = tasktype_id
      and deal_project_status = 0
      and deal_hitrate = '100'
      and deal_archive = 0
    ";

  if ($name != "") {
    $query .= " and deal_label like '$name%'";
  }

  if ($tt != 0) {
    $query .= " and deal_tasktype_id = $tt";
  }

  $query .= " order by project_initlabel";

  display_debug_msg($query, $cdg_sql, "run_query_newprojects");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
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
      if (projecttask_parenttask_id =0, projecttask_id, projecttask_parenttask_id) as parent_group
    from ProjectTask
    where projecttask_deal_id = $pid
    order by parent_group, parent_id, projecttask_rank
    ";

  display_debug_msg($query, $cdg_sql, "run_query_tasks");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Project members query
// Parameters :
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
      from UserObm, ProjectUser
      where projectuser_deal_id = $p_id
        and projectuser_user_id = userobm_id
        and userobm_archive = 0
      group by member_id
      order by member_lastname
      "; 
  } else { 
    $query = "
      select
        userobm_id as member_id,
        userobm_firstname as member_firstname,
        userobm_lastname as member_lastname
      from UserObm, ProjectUser
      where projectuser_user_id = userobm_id
         and userobm_archive = 0
      group by member_id
      order by member_lastname
      ";
  }  
    display_debug_msg($query, $cdg_sql, "run_query_members");
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Members time informations query
// Parameters :
//   - $p_id      : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_membertime($p_id) {
  global $cdg_sql, $c_day_fraction;

  $frac = $c_day_fraction;

  $query = "
    select
      userobm_id,
      projectuser_projecttask_id,
      sum((timetask_length)/$frac) as used_time,
      projectuser_projectedtime as projected_time,
      projectuser_missingtime as missing_time
    from UserObm, ProjectUser, ProjectTask
    left join TimeTask
      on  timetask_projecttask_id = projecttask_id
      and timetask_user_id = userobm_id
    where projecttask_deal_id = $p_id
      and projecttask_id = projectuser_projecttask_id
      and projectuser_user_id = userobm_id
      and projectuser_projectedtime != 0
    group by userobm_id, projecttask_id
    ";

  display_debug_msg($query, $cdg_sql, "run_query_membertime");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
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
            and   projectuser_manager = 1 ";
  
  if ($p_id)
    $query .= "and projectuser_deal_id = $p_id ";

  $query .= "group by manager_id
             order by manager_firstname";

  display_debug_msg($query, $cdg_sql, "run_query_managers");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Return all the project that are still in commercial state
// Parameters :
//   $type : categories of tasktype
///////////////////////////////////////////////////////////////////////////////
function run_query_projecttype($type = 'all') {
  global $cdg_sql;

  $query = "
    select
      tasktype_id,
      tasktype_label, 
      tasktype_internal
    from TaskType ";

  if ($type == 'all')
    $query .= "where (tasktype_internal = 0 or tasktype_internal = 1)";
  elseif ($type == 'billable')
    $query .= "where tasktype_internal = 0";
  else
    $query .= "where tasktype_internal = 1";

  $query .= " order by tasktype_internal";

  display_debug_msg($query, $cdg_sql, "run_query_projecttype");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
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
function manager_rights($uid, $project) {

  $pid = $project["id"];

  $query = "
    select userobm_id
    from UserObm
    where userobm_perms='admin'
      and userobm_id = $uid";

  display_debug_msg($query, $cdg_sql, "manager_rights(admins)");
  $admins_q = new DB_OBM;
  $admins_q->query($query);

  $query = "
    select projectuser_id
    from ProjectUser
    where projectuser_deal_id = $pid
      and projectuser_manager != 0";

  display_debug_msg($query, $cdg_sql, "manager_rights(managers)");
  $managers_q = new DB_OBM;
  $managers_q->query($query);

  $manager_rights = (($admins_q->nf()!=0) or ($managers_q->nf() !=0)) ? true : false;

  return $manager_rights;

}

///////////////////////////////////////////////////////////////////////////////
// New Project Form Data checking and formatting                             //
// Parameters:
//   - $pid   : project id
//   - $project[] : values checked
//     keys used  : name, tt, soldtime
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_new_form($pid, $project) {
  global $l_fill_name, $l_fill_tt, $l_fill_soldtime, $l_fill_soldnumber;
  global $err_msg;
  global $php_regexp_number;

  $name = $project["name"];
  $tt = $project["tt"];
  $soldtime = $project["soldtime"];

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
  if (trim($soldtime) == "") {
    $err_msg = $l_fill_soldtime;
    return false;
  }

  // MANDATORY: Soldtime
  if (trim($soldtime) == 0) {
    $err_msg = $l_fill_soldtime;
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
// Project Init Form Data checking and formatting                                 //
// Parameters:
//   - $pid   : project id
//   - $project[] : values checked
//     keys used  : tt, soldtime
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_init_form($pid, $project) {
  global $l_fill_soldtime, $l_fill_soldnumber;
  global $err_msg;
  global $php_regexp_number;

  $soldtime = $project["soldtime"];

  // MANDATORY: Soldtime
  if (trim($soldtime) == "") {
    $err_msg = $l_fill_soldtime;
    return false;
  }

  // MANDATORY: Soldtime
  if (trim($soldtime) == 0) {
    $err_msg = $l_fill_soldtime;
    return false;
  }

  // MANDATORY: Soldtime
//   if (ereg($php_regexp_number, $soldtime) == false) {
//     $err_msg = $l_fill_soldnumber;
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
function check_taskadd_form($pid, $project) {
  global $l_fill_tasklabel;
  global $err_msg;

  $tasklabel = $project["tasklabel"];

  // MANDATORY: Soldtime
  if (trim($tasklabel) == "") {
    $err_msg = $l_fill_tasklabel;
    return false;
  }

  return true; 
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

</SCRIPT>
