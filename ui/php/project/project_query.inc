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

  $query = "select 
      deal_id as project_id,
      deal_label as project_label,
      deal_soldtime as project_soldtime,
      deal_archive as project_archive,
      deal_state as project_state,
      company_name as project_company_name,
      tasktype_label as project_tasktype
    from Deal, TaskType";

  if ($member != $c_all)
  $query .= ", ProjectUser Member";
  
  if ($manager != $c_all)
  $query .= ", ProjectUser Manager";
  
  $query .= " left join Company on deal_company_id=company_id";
  $query .= " where tasktype_id=deal_tasktype_id
              and ((deal_state = 1) or (deal_state = 2))
              and deal_archive = 0";

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
                and Manager.projectuser_manager = 1
                and Manager.projectuser_userobm_id = $manager";
  }

  if ($member != $c_all) {
    $query .= " and deal_id = Member.projectuser_deal_id
                and Member.projectuser_userobm_id = $member";
  }

  // Order by clause
  $order=(strcmp($p_new_order,"") != 0) ? $p_new_order : "deal_label";
  $query .= " order by $order $p_order_dir";

  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Detail query execution                                                    //
// Parameters :
//   - $p_id  : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_id) {
  global $cdg_sql;

  $query = "select
      deal_id as project_id,
      deal_label as project_label,
      deal_state as project_state,
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
  display_debug_msg($query, $cdg_sql);
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
    deal_state)
  values (null,
    '" . date("Y-m-d H:i:s") ."',
    null,
    '" . $auth->auth["uid"] . "',
    '$name',
    '$tt',
    $soldtime,
    2)";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  // search the id of the inserted deal
  $query = "select deal_id
            from Deal
            where deal_label = '$name'";

  display_debug_msg($query, $cdg_sql);
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
      deal_state = 1
    where deal_id = $p_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Update query execution                                                    //
// Parameters:
//   - $pid       : project id
//   - $project[] : Entry's values
//     keys used  : name, soldtime, tt, state
///////////////////////////////////////////////////////////////////////////////
function run_query_update($pid, $project) {
  global $auth, $cdg_sql;

  $state = $project["state"];
  $soldtime = $project["soldtime"];
  $name = $project["name"];
  $tt = $project["tt"];

  $query = "update Deal set
      deal_timeupdate='". date("Y-m-d H:i:s") ."',
      deal_userupdate='". $auth->auth["uid"] ."',
      deal_soldtime=$soldtime";

  if ($state == 2)
    $query .= ", deal_label='$name'
               , deal_tasktype_id=$tt";
  
  $query .= " where deal_id='$pid'";

  display_debug_msg($query, $cdg_sql);
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
      deal_state=3
    where deal_id='$pid'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : ContactList conditionnal insertion                      //
// Parameters:
//   - $list[] : deal hash info : keys used : id, con_nb, conX
// Return: number of contact inserted
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
        and projectuser_userobm_id = $mem_id";

    display_debug_msg($query, $cdg_sql);
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $query = "
        insert into ProjectUser 
          (projectuser_userobm_id,
           projectuser_deal_id,
           projectuser_timecreate,
           projectuser_usercreate
          )
        values
          ($mem_id,
          $id,
          '".date("Y-m-d H:i:s")."',
          ". $auth->auth["uid"] .")";

      display_debug_msg($query, $cdg_sql);
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
//   - $list[] : deal hash info : keys used : id, con_nb, conX
///////////////////////////////////////////////////////////////////////////////
function run_query_memberlist_delete($project) {
  global $auth, $cdg_sql;

  $id = $project["id"];
  $cpt = 0;
  $cpt_del = 0;
  while ($cpt < $project["mem_nb"]) {
    $cpt++;
    $mem_id = $project["mem$cpt"];

    $query = "delete
      from ProjectUser
      where projectuser_deal_id = $id
        and projectuser_userobm_id = '$mem_id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
    if ($retour) {
      $cpt_del++;
    }
  }

  return $cpt_del;
}


///////////////////////////////////////////////////////////////////////////////
// Advance Update query execution                                            //
// Parameters:
//   - $p_id      : project id
//   - $u_id      : user id
//   - $missing   : new missing time
///////////////////////////////////////////////////////////////////////////////
function run_query_projectupdate($project) {
  global $auth, $cdg_sql;

  $p_id = $project["id"];
  $obm_q = new DB_OBM;

  // Update project estimated missing times
  while ( list($m_id, $projtime) = each($project["projected"]) ) {
    if ($projtime != "") {

      $query = "update ProjectUser set
          projectuser_timeupdate='". date("Y-m-d H:i:s") ."',
          projectuser_userupdate='". $auth->auth["uid"] ."',
          projectuser_missingtime =  $projtime,
          projectuser_projectedtime =  $projtime
        where projectuser_deal_id = $p_id
          and projectuser_userobm_id = $m_id
      ";
      
      display_debug_msg($query, $cdg_sql);
      $retour = $obm_q->query($query);
      
      if (!($retour))
	$ins_err = 1;
    }
  }


  return $ins_err;
}


///////////////////////////////////////////////////////////////////////////////
// Advance Update query execution                                            //
// Parameters:
//   - $p_id      : project id
//   - $u_id      : user id
//   - $missing   : new missing time
///////////////////////////////////////////////////////////////////////////////
function run_query_advanceupdate($project) {
  global $auth, $cdg_sql;

  $p_id = $project["id"];
  $obm_q = new DB_OBM;

  // Update project estimated missing times
  while ( list($m_id, $misstime) = each($project["missing"]) ) {
    if ($misstime != "") {
      
      $query = "update ProjectUser set
          projectuser_timeupdate='". date("Y-m-d H:i:s") ."',
          projectuser_userupdate='". $auth->auth["uid"] ."',
          projectuser_missingtime =  $misstime
        where projectuser_deal_id = $p_id
          and projectuser_userobm_id = $m_id
      ";
      
      display_debug_msg($query, $cdg_sql);
      $retour = $obm_q->query($query);
      
      if (!($retour))
	$ins_err = 1;
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

  $query = "select (sum(task_length)/$frac) as time_used
            from Task
            where task_deal_id=$p_id
            group by task_deal_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $time_used = $obm_q->f("time_used");
  if ($time_used == "")  $time_used = 0;

  $query = "select sum(projectuser_missingtime) as time_left
            from ProjectUser
            where projectuser_deal_id=$p_id
            group by projectuser_deal_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $time_left = $obm_q->f("time_left");

  $query = "
    insert into ProjectStat (
      projectstat_deal_id,
      projectstat_date,
      projectstat_useddays,
      projectstat_remainingdays)
    values (
      $p_id,
      '". date("Y-m-d H:i:s") ."',
      $time_used,
      $time_left
    )";

   display_debug_msg($query, $cdg_sql);
   $obm_q = new DB_OBM;
   $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution                                                  //
// Parameters:
//   - $p_id : company id
///////////////////////////////////////////////////////////////////////////////
// function run_query_delete($p_id) {
//   global $cdg_sql;

//   $query = "delete from Company where company_id='$p_id'";

//   display_debug_msg($query, $cdg_sql);
//   $obm_q = new DB_OBM;
//   $retour = $obm_q->query($query);

//   return $retour;
// }


///////////////////////////////////////////////////////////////////////////////
// Return all the project that are still in commercial state
// Parameters :
///////////////////////////////////////////////////////////////////////////////
function run_query_newprojects() {
  global $cdg_sql;

  $query = "select deal_id as project_id,
              deal_state as project_state,
              deal_label as project_initlabel,
              company_name as project_company_name,
              tasktype_label as project_tasktype
            from Deal, Company, TaskType
            where deal_company_id = company_id
              and deal_tasktype_id = tasktype_id
              and deal_state = 0
              and deal_hitrate = '100'
              and deal_archive = 0
            order by project_initlabel";

  display_debug_msg($query, $cdg_sql);
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

  $query = "select tasktype_id, tasktype_label, tasktype_internal
            from TaskType ";

  if ($type == 'all')
    $query .= "where (tasktype_internal = 0 or tasktype_internal = 1)";
  elseif ($type == 'billable')
    $query .= "where tasktype_internal = 0";
  else
    $query .= "where tasktype_internal = 1";

  $query .= " order by tasktype_internal";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Project managers query
// Parameters :
///////////////////////////////////////////////////////////////////////////////
function run_query_manager($p_id = 0) {
  global $cdg_sql;

  $query = "select userobm_id as manager_id,
                   userobm_firstname as manager_firstname,
                   userobm_lastname as manager_lastname
            from ProjectUser, UserObm
            where projectuser_userobm_id = userobm_id
            and   projectuser_manager = 1 ";
  
  if ($p_id)
    $query .= "and projectuser_deal_id = $p_id ";

  $query .= "group by manager_id
             order by manager_firstname";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Project members query
// Parameters :
///////////////////////////////////////////////////////////////////////////////
function run_query_member($p_id = 0) {
  global $cdg_sql;

  $query = "select userobm_id as member_id,
                   userobm_firstname as member_firstname,
                   userobm_lastname as member_lastname
            from ProjectUser, UserObm
            where projectuser_userobm_id = userobm_id ";
  
  if ($p_id)
    $query .= "and projectuser_deal_id = $p_id ";

  $query .= "group by member_id
             order by member_firstname";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Members time informations query
// Parameters :
//   - $pid      : project id
///////////////////////////////////////////////////////////////////////////////
function run_query_memberstime($p_id) {
  global $cdg_sql, $c_day_fraction;

  $frac = $c_day_fraction;

  $query = "select
      userobm_id,
      userobm_lastname,
      userobm_firstname,
      sum((task_length)/$frac) as used_time,
      projectuser_projectedtime as projected_time,
      projectuser_missingtime as missing_time,
      projectuser_manager
    from UserObm, ProjectUser
    left join Task 
      on  task_deal_id = $p_id
      and task_user_id = userobm_id
    where projectuser_deal_id = $p_id
      and projectuser_userobm_id = userobm_id
    group by userobm_id, task_user_id
    order by projectuser_manager desc, userobm_lastname, userobm_firstname
    ";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


</SCRIPT>
