<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : group_query.inc                                              //
//     - Desc : group query File                                             //
// 2003-08-22 AliaSource                                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Group Search query execution 
// Parameters :
//   - $group[]      : list search criteria
//     keys used     : name, user
///////////////////////////////////////////////////////////////////////////////
function run_query_group_search($group) {
  global $cdg_sql, $ctu_sql_limit;
  global $c_public, $c_private;
  
  $name = sql_search_text_parse($group["name"]);
  $delegation = sql_search_text_parse($group["delegation"]);
  $email = sql_search_text_parse($group["email"]);
  $privacy = $group["privacy"];
  $user = sql_search_text_parse($group["user"]);
  
  $sql_order_field = $group["sql_order_field"];
  $sql_order_dir = $group["sql_order_dir"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $timeupdate = sql_date_format($db_type,"UGroup.group_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type,"UGroup.group_timecreate", "timecreate");
  $multidomain = sql_multidomain("group");
	
  $where = "group_name $like '$name%'";
  if ($user != '') {
    $join_user = "
      LEFT JOIN of_usergroup as D ON D.of_usergroup_group_id=group_id
      LEFT JOIN UserObm as C ON D.of_usergroup_user_id = C.userobm_id";
    $where .= " AND C.userobm_lastname $like '$user%'";
  }
  // If a delegation indication has been specified, get it
  if (trim($delegation) != "") {
    $where .= " AND group_delegation $like '$delegation%'";
  }
  if ($email != '') {
    $where .= " AND group_email $like '%$email%'";
  }
  if ($privacy == $c_public) {
    $where .= " AND group_privacy = 0";
  } elseif ($privacy == $c_private) { 
    $where .= " AND group_privacy = 1";
  }
  // only the one which are allowed (ie. publics )
  $where .=  " AND " .sql_obm_entity_privacy("group");
    
  $whereq = "WHERE $where $multidomain";

  // ORDER construction
  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "group_name";
  $orderq .= " ORDER BY $order $sql_order_dir";

  $query = "SELECT  
      group_id as id,
      group_id,
      group_domain_id,
      group_system,
      group_privacy,
      group_local,
      group_ext_id,
      group_samba,
      group_gid,
      group_mailing,
      group_delegation,
      group_manager_id,
      group_name,
      group_desc,
      group_email,
      group_contacts,
      $timecreate,
      $timeupdate,
      A.userobm_login as usercreate,
      B.userobm_login as userupdate,
      count(usergroup.of_usergroup_user_id) as group_nb_user
    FROM UGroup
      LEFT JOIN of_usergroup usergroup ON of_usergroup_group_id=group_id
      LEFT JOIN UserObm as A ON UGroup.group_usercreate=A.userobm_id
      LEFT JOIN UserObm as B ON UGroup.group_userupdate=B.userobm_id
      $join_user
    $whereq
    GROUP BY group_id,
      group_domain_id,
      group_system,
      group_privacy,
      group_local,
      group_ext_id,
      group_samba,
      group_gid,
      group_mailing,
      group_delegation,
      group_manager_id,
      group_name,
      group_desc,
      group_email,
      group_contacts,
      timecreate,
      timeupdate,
      usercreate,
      userupdate
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM UGroup $join_user $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, "run_query_group_search()");
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Possible children Group Search query execution 
// Parameters :
//   - $group[]      : list search criteria
//     keys used     : name, user
///////////////////////////////////////////////////////////////////////////////
function run_query_group_search_possible_children($group) {
  global $cdg_sql;
  
  $id = $group["ext_id"];
  $name = $group["name"];
  $delegation = sql_search_text_parse($group["delegation"]);
  $email = $group["email"];
  $user = $group["user"];
  $sql_order_field = $group["sql_order_field"];
  $sql_order_dir = $group["sql_order_dir"];

  $parents = get_group_parents($id);

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  $timeupdate = sql_date_format($db_type,"UGroup.group_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type,"UGroup.group_timecreate", "timecreate");
  $multidomain = sql_multidomain("group");
	
  // WHERE Clause

  // only the one which are allowed (ie. publics )
  $where .=  " AND " .sql_obm_entity_privacy("group");
  if ($user != '') {
    $where .= " AND C.userobm_lastname $like '$user%'";
  }
  // If a delegation indication has been specified, get it
  if (trim($delegation) != "") {
    $where .= " AND group_delegation $like '$delegation%'";
  }
  if ($email != '') {
    $where .= " AND group_email $like '%$email%'";
  }
  while (list ($key, $g_id) = each($parents)) {
    $where .= " AND group_id != '$g_id'";
  }
    
  $query = "SELECT DISTINCT UGroup.*, group_id as id,
      $timecreate,
      $timeupdate,
      A.userobm_login as usercreate,
      B.userobm_login as userupdate
    FROM UGroup
      LEFT JOIN UserObmGroup ON group_id=UserObmGroup_group_id
      LEFT JOIN UserObm as C ON UserObmGroup.userobmgroup_userobm_id=C.userobm_id
      LEFT JOIN UserObm as A ON UGroup.group_usercreate=A.userobm_id
      LEFT JOIN UserObm as B ON UGroup.group_userupdate=B.userobm_id
    WHERE group_name $like '$name%'
      AND group_id != '$id'
      $where
      $multidomain";

  // ORDER construction

  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "group_name";
  $query .= " ORDER BY $order $sql_order_dir";

  display_debug_msg($query, $cdg_sql, "run_query_group_search_possible_children()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Group detail query execution
// Parameters:
//   - $id : group id
///////////////////////////////////////////////////////////////////////////////
function run_query_group_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type,"UGroup.group_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type,"UGroup.group_timecreate", "timecreate");
  $multidomain = sql_multidomain("group");
	
  $query = "SELECT *,
      group_privacy as privacy,
      group_mailing as mailing,
      $timecreate,
      $timeupdate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate,
      m.userobm_lastname,
      m.userobm_firstname
    FROM UGroup
         LEFT JOIN UserObm as c ON group_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON group_userupdate=u.userobm_id
         LEFT JOIN UserObm as m ON group_manager_id=m.userobm_id
    WHERE group_id = '$id'
      $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_group_detail()");
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : group insertion
// Parameters:
//   - $group[] : group hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_group_insert($group) {
  global $cdg_sql, $obm;

  $id = $group["group_id"];
  $system = ($group["system"] == 1) ? 1 : 0;
  $samba = ($group["samba"] == 1) ? 1 : 0;
  $name = $group["name"];
  $desc = $group["desc"];
  $email = $group["email"];
  $priv = (isset($group["privacy"]) ? $group["privacy"] : '0');
  $mailing = (isset($group["mailing"]) ? $group["mailing"] : '0');
  $now = date("Y-m-d H:i:s");
  $gid = get_first_group_free_gid();
  $domain_id = $obm["domain_id"];	
  if ($priv == 1) {
    $mailing = 0;
    $samba = 0;
    $email = '';
  }

  // Delegation handling
  $q_delegation = of_delegation_query_insert_clauses('group_delegation', stripslashes($group["delegation"]));

  $query = "INSERT INTO UGroup (
    group_timeupdate,
    group_timecreate,
    group_userupdate,
    group_usercreate,
    group_domain_id,
    group_system,
    group_samba,
    group_privacy,
    group_gid,
    group_name
    $q_delegation[field],
    group_desc,
    group_email,
    group_mailing
  ) VALUES (
    null,
    '$now',
    null,
    '$obm[uid]',
    '$domain_id',
    '$system',
    '$samba',
    '$priv',
    '$gid',
    '$name'
    $q_delegation[value],
    '$desc',
    '$email',
    '$mailing'
  )";

  display_debug_msg($query, $cdg_sql, "run_query_group_insert()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  $g_id = run_query_group_id($group);

  if ($g_id > 0) {
    $ret = of_userdata_query_update("group", $g_id, $group);
  }

  return $g_id;
}


///////////////////////////////////////////////////////////////////////////////
// Group ID : Get the id of the group inserted
// Parameters:
//   - $group[] : group hash info : keys used : all
// Returns:
//   group Id if found else false
///////////////////////////////////////////////////////////////////////////////
function run_query_group_id($group) {
  global $cdg_sql;

  $system = ($group["system"] == 1) ? 1 : 0;
  $name = $group["name"];
  $desc = $group["desc"];
  $email = $group["email"];
  $priv = (isset($group["privacy"]) ? $group["privacy"] : '0');
  $multidomain = sql_multidomain("group");
	
  $query = "SELECT group_id
    FROM UGroup
    WHERE group_system='$system'
      AND group_name='$name'
      AND group_privacy='$priv'
      AND group_desc='$desc'
      AND group_email='$email'
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_group_id()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    $obm_q->next_record();
    $id = $obm_q->f("group_id");
  } else {
    $id = false;
  }

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Group Update query execution
// Parameters:
//   - $group[] : group hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_group_update($group) {
  global $obm, $cdg_sql;

  $id = $group["group_id"];
  $name = $group["name"];
  $samba = ($group["samba"] == 1) ? 1 : 0;
  $desc = $group["desc"];
  $email = $group["email"];
  $manager = $group["manager"];
  $priv = (isset($group["privacy"]) ? $group["privacy"] : '0');
  $mailing = (isset($group["mailing"]) ? $group["mailing"] : '0');
  $now = date("Y-m-d H:i:s");
  $multidomain = sql_multidomain("group");
  if ($priv == 1) {
    $mailing = 0;
    $samba = 0;
    $email = "";
  }

  // Delegation handling
  $q_delegation = of_delegation_query_update_clause('group_delegation', stripslashes($group["delegation"]));

  $query = "UPDATE UGroup SET
    group_timeupdate='$now',
    group_userupdate='$obm[uid]',
    group_samba='$samba',
    group_privacy='$priv',
    group_name='$name',
    group_desc='$desc',
    group_email='$email',
    group_mailing='$mailing',
    group_manager_id='$manager'
    $q_delegation
  WHERE group_id='$id'
    $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_group_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($id > 0) {
    $ret = of_userdata_query_update("group", $id, $group);
  }

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : group id
///////////////////////////////////////////////////////////////////////////////
function run_query_group_delete($p_id) {
  global $cdg_sql, $obm;

  $ret = of_userdata_query_delete("group", $p_id);

  // Delete all users registrations to this group
  $query = "DELETE FROM UserObmGroup WHERE userobmgroup_group_id='$p_id'";
  display_debug_msg($query, $cdg_sql, "run_query_group_delete(users links)");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Delete all groups registration referencing this group
  $query = "DELETE FROM GroupGroup
    WHERE groupgroup_child_id='$p_id'
      OR groupgroup_parent_id='$p_id'";
  display_debug_msg($query, $cdg_sql, "run_query_group_delete(groups links)");
  $retour = $obm_q->query($query);

  // Delete Internal users registrations to this group
  $query = "DELETE FROM of_usergroup WHERE of_usergroup_group_id='$p_id'";
  display_debug_msg($query, $cdg_sql, "run_query_group_delete(internal links)");
  $retour = $obm_q->query($query);

  // Delete the Group
  $multidomain = sql_multidomain("group");
  $query = "DELETE FROM UGroup WHERE group_id='$p_id' $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_group_delete(3)");
  $retour = $obm_q->query($query);

  // If the Group exists in Prod table AND is public, Insert it in Deleted table
  $query = "SELECT group_id, group_delegation FROM P_UGroup
    WHERE group_id='$p_id' AND group_privacy=0";
  display_debug_msg($query, $cdg_sql, "run_query_group_delete(auto)");
  $obm_q->query($query);
  $obm_q->next_record();
  $delegation = $obm_q->f('group_delegation');

  if ($obm_q->num_rows() > 0) {
    $uid = $obm["uid"];
    $domain_id = $obm["domain_id"];
    $now = date("Y-m-d H:i:s");

    $query = "INSERT INTO Deleted (
      deleted_domain_id,
      deleted_user_id,
      deleted_delegation,
      deleted_table,
      deleted_entity_id,
      deleted_timestamp)
    VALUES (
      '$domain_id',
      '$uid',
      '$delegation',
      'UGroup',
      '$p_id',
      '$now')";
    display_debug_msg($query, $cdg_sql, "run_query_group_delete(auto)");
    $retour = $obm_q->query($query);
  }

  return $retour;
}


/**
 * Query execution : UserObmGroup conditionnal insertion
 *
 * @param mixed $group[] : group hash info : keys used : id, user_nb, userX
 * @result number of users inserted
**/
function run_query_group_usergroup_insert($group) {
  global $obm, $cdg_sql;

  $id = $group['group_id'];
  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $group["user_nb"]) {
    $cpt++;
    $u_id = $group["user$cpt"];

    $query = "SELECT * FROM UserObmGroup
      WHERE userobmgroup_group_id='$id'
        AND userobmgroup_userobm_id='$u_id'";
    display_debug_msg($query, $cdg_sql, "run_query_group_usergroup_insert(1)");
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $query = "INSERT INTO UserObmGroup (
        userobmgroup_group_id,
        userobmgroup_userobm_id)
      VALUES ($id, $u_id)";

      display_debug_msg($query, $cdg_sql, 'run_query_group_usergroup_insert(2)');
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      $cpt_ins++;
    }
  }

  // Update internal group membership direct mapping
  if ($cpt_ins > 0) {
    of_usergroup_update_group_hierarchy($id);
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : UserObmGroup deletion
// Parameters:
//   - $group[] : group hash info : keys used : id, user_nb, userX
// Return: number of users deleted
///////////////////////////////////////////////////////////////////////////////
function run_query_group_usergroup_delete($group) {
  global $cdg_sql;

  $id = $group["group_id"];
  $cpt = 0;
  $cpt_del = 0;
  while ($cpt < $group["user_nb"]) {
    $cpt++;
    $u_id = $group["user$cpt"];

    $query = "DELETE FROM UserObmGroup
      WHERE userobmgroup_group_id='$id'
        AND userobmgroup_userobm_id='$u_id'";
    display_debug_msg($query, $cdg_sql, "run_query_group_usergroup_delete()");
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
    if ($retour) {
      $cpt_del++;
    }
  }

  // Update internal group membership direct mapping
  if ($cpt_del > 0) {
    of_usergroup_update_group_hierarchy($id);
  }

  return $cpt_del;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : GroupGroup conditionnal insertion
// Parameters:
//   - $group[] : group hash info : keys used : id, group_nb, group_X
// Return: number of groups inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_group_group_insert($group) {
  global $cdg_sql;

  $id = $group["group_id"];
  $cpt = 0;
  $cpt_ins = 0;

  $parents = get_group_parents($id);
  $g = get_group_info($id);
  $privacy = $g["privacy"];

  while ($cpt < $group["group_nb"]) {
    $cpt++;
    $g_id = $group["group_$cpt"];

    $query = "SELECT group_privacy, groupgroup_child_id FROM 
      UGroup
      LEFT JOIN GroupGroup ON groupgroup_parent_id='$id' AND groupgroup_child_id = group_id
      WHERE group_id='$g_id'";
    display_debug_msg($query, $cdg_sql, "run_query_group_group_insert(1)");
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    $test_q->next_record(); 
    // If the entry doesn't already exist, doesn't create a loop,
    // and child group is same privacy as parent, insert it
    if ( (!$test_q->f("groupgroup_child_id"))
         && (! in_array($g_id, $parents))
         && ($id != $g_id)
         && ($privacy == 1 || $test_q->f("group_privacy") != 1)
       ) {
        $query = "INSERT INTO GroupGroup (
          groupgroup_parent_id,
          groupgroup_child_id)
        VALUES ($id, $g_id)";

      display_debug_msg($query, $cdg_sql, "run_query_group_group_insert(2)");
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      $cpt_ins++;
    }
  }

  // Update internal group membership direct mapping
  if ($cpt_ins > 0) {
    of_usergroup_update_group_hierarchy($id);
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : GroupGroup deletion
// Parameters:
//   - $group[] : group hash info : keys used : id, group_nb, group_X
// Return: number of groups deleted
///////////////////////////////////////////////////////////////////////////////
function run_query_group_group_delete($group) {
  global $cdg_sql;

  $id = $group["group_id"];
  $cpt = 0;
  $cpt_del = 0;
  while ($cpt < $group["group_nb"]) {
    $cpt++;
    $g_id = $group["group_$cpt"];

    $query = "DELETE FROM GroupGroup
      WHERE groupgroup_parent_id='$id'
        AND groupgroup_child_id='$g_id'";

    display_debug_msg($query, $cdg_sql, "run_query_group_group_delete()");
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
    if ($retour) {
      $cpt_del++;
    }
  }

  // Update internal group membership direct mapping
  if ($cpt_del > 0) {
    of_usergroup_update_group_hierarchy($id);
  }

  return $cpt_del;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get the users of the given group
// Parameters:
//   - $group[] : Group parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_group_user_group($group) {
  global $cdg_sql;

  $obm_q = new DB_OBM; 
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type);

  $id = $group["group_id"];
  $entity = $group["entity"];
  $sql_order_field = $group["sql_order_field"];
  $sql_order_dir = $group["sql_order_dir"];

  if (($entity == "group_user") && (trim($sql_order_field) != "")) {
    $order = "ORDER BY $sql_order_field $sql_order_dir";
  } else {
    $order = "ORDER BY userobm_lastname";
  }
	
  $query = "SELECT
    userobmgroup_userobm_id as group_user_id,
    userobmgroup_userobm_id as id,
    userobm_timeupdate as group_user_timeupdate,
    userobm_timecreate as group_user_timecreate,
    userobm_userupdate as group_user_userupdate,
    userobm_usercreate as group_user_usercreate,
    userobm_login as group_user_login,
    userobm_lastname as group_user_lastname,
    userobm_firstname as group_user_firstname,
    userobm_phone as group_user_phone,
    userobm_email as group_user_email,
    userobm_hidden,
    domain_name
  FROM UserObmGroup LEFT JOIN UserObm ON userobmgroup_userobm_id=userobm_id
  LEFT JOIN Domain ON domain_id = userobm_domain_id
  WHERE userobmgroup_group_id='$id'
  $order
  $limit";

  display_debug_msg($query, $cdg_sql, "run_query_group_user_group()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get the groups members of the given group
// Parameters:
//   - $group[] : Group parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_group_group($group) {
  global $cdg_sql;

  $obm_q = new DB_OBM; 
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type);

  $id = $group["group_id"];
  $entity = $group["entity"];
  $sql_order_field = $group["sql_order_field"];
  $sql_order_dir = $group["sql_order_dir"];

  if (($entity == "group_group") && (trim($sql_order_field) != "")) {
    $order = "ORDER BY $sql_order_field $sql_order_dir";
  } else {
    $order = "ORDER BY group_name";
  }

  $query = "SELECT
    groupgroup_child_id as child_id,
    groupgroup_child_id as id,
    group_timeupdate,
    group_timecreate,
    group_userupdate,
    group_usercreate,
    group_id,
    group_name,
    group_desc,
    group_email
  FROM GroupGroup LEFT JOIN UGroup ON groupgroup_child_id=group_id
  WHERE groupgroup_parent_id='$id'
  $order
  $limit";

  $obm_q = new DB_OBM; 
  display_debug_msg($query, $cdg_sql, "run_query_group_group()");
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return the number of registered user in the group specified
// Parameters:
//   - $id : group id
///////////////////////////////////////////////////////////////////////////////
function get_group_nb_user($id) {
  global $cdg_sql;

  $query = "SELECT count(*) FROM UserObmGroup
    WHERE userobmgroup_group_id='$id'";

  display_debug_msg($query, $cdg_sql, "get_group_nb_user()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f(0);

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Check if a group (except with id given) with the name given already exists
// Parameters:
//   - $name : name to search for
//   - $id   : group id to exclude
// Returns:
//   - true (if a group exists) or false
///////////////////////////////////////////////////////////////////////////////
function get_group_name_exists($name, $id='') {
  global $cdg_sql, $obm;

  if ($id != '') {
    $where_id = "and group_id!='$id'";
  }
	
  $multidomain = sql_multidomain("group");
	
  $query = "SELECT group_id, group_name
    FROM UGroup
    WHERE group_name='$name'
      AND (group_privacy=0 OR group_usercreate='$obm[uid]')
      $where_id
      $multidomain";

  display_debug_msg($query, $cdg_sql, "get_group_name_exists()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Return the groups which matches the name or the desc
// except the one given (update mode)
// Parameters:
//   - $id   : group id
//   - $name : group name
//   - $desc : group desc
///////////////////////////////////////////////////////////////////////////////
function run_query_group_check($id, $name, $desc) {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $multidomain = sql_multidomain("group");

  if ($id != "") {
    $where_id = "AND group_id!='$id'";
  }

  // If name is short, we test equality, else similarity
  if (strlen($name) > 2) {
    $wname = "group_name $like '%$name%'";
  } else {
    $wname = "group_name = '$name'";
  }

  // If desc is short, we test equality, else similarity
  if (trim($desc) != "") {
    if (strlen($desc) > 2) {
      $wdesc = "group_desc $like '%$desc%'";
    } else {
      $wdesc = "group_desc = '$desc'";
    }
  }
  if ($wdesc != "") $wdesc = "or $wdesc";

  $query = "SELECT distinct group_id, group_name, group_desc
     FROM UGroup
     WHERE
       (group_privacy=0 OR group_usercreate='$obm[uid]')
       AND ($wname $wdesc)
       $where_id
       $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_group_check()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Group context checking (same groups exists ?)
// Parameters:
//   - $id       : group id
//   - $group[]  : group values
//     keys used : name, desc, email
// Returns:
//   - Group Database object with group of similar groups
///////////////////////////////////////////////////////////////////////////////
function check_group_context($id, $group) {
  global $cdg_sql;

  $name = $group["name"];
  $desc = $group["desc"];

  // return the groups with same name or desc
  $g_q = run_query_group_check($id, $name, $desc);

  return $g_q;
}


/*
 * Group specific rights checking (eg : public group needs write_admin)
 * Parameters:
 * @param $params : parameters values
 * @param $g[]    : current group values
 * @return true if OK, else false
 */
function check_group_update_rights($params, $g='') {
  global $actions, $perm, $err, $cright_write_admin;
  global $cg_gid_smb_user, $cg_gid_smb_admin, $l_cant_update_smb_group;
  global $l_err_public_rights, $l_error_permission, $l_error_delegation;

  $id = $params['group_id'];
  if ($id > 0) {
    // Get current group info if not given
    if (! is_array($g)) {
      $g = get_group_info($id);
    }

    // Check delegation update right
    if (! of_delegation_check_update_rights($params, $g['delegation'])) {
      $err['msg'] .= $l_error_delegation;
      return false;
    }

    // Check update right is set
    if (! $perm->check_right('group', $actions['group']['update']['Right'])) {
      $err['msg'] .= $l_error_permission;
      return false;
    }

    // Allow public group handling only if write_admin right
    if (($g['privacy'] != 1)
	&& (! $perm->check_right('group', $cright_write_admin))) {
      $err['msg'] .= $l_err_public_rights;
      return false;
    }
  
    // We forbid to update Window system groups
    $gid = $g['gid'];
    if (($gid == $cg_gid_smb_user) || ($gid == $cg_gid_smb_admin)) {
      $err['msg'] .= $l_cant_update_smb_group;
      return false;
    }
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Group Form Data checking and formatting
// Parameters:
//   - $group[]  : values checked
//     keys used : name, desc, email
///////////////////////////////////////////////////////////////////////////////
function check_group_data_form(&$group) {
  global $php_regexp_email_name, $php_regexp_email, $l_fill_name;
  global $l_group_exists, $l_email_exist;
  global $obm, $perm, $err, $action, $cright_write_admin;
  global $l_j_check_email, $l_system_no_priv, $l_err_system_samba;
  global $l_only_owner_priv, $l_err_public_rights, $l_err_private_email;

  $id = $group['group_id'];
  $name = $group['name'];
  $samba = $group['samba'];
  $desc = $group['desc'];
  $email = $group['email'];
  $priv = $group['privacy'];
  $g = get_group_info($id);
  $usercreate = $g['usercreate'];
  $system = $g['system'];
  $gsamba = $g['samba'];


  // MANDATORY: Group name not empty
  if (trim($name) == '') {
    $err['msg'] = $l_fill_name;
    $err['field'] = 'name';
    return false;
  }

  // MANDATORY: Group name unique
  if (get_group_name_exists($name, $id)) {
    $err['msg'] = "$l_group_exists ($name)";
    $err['field'] = 'name';
    return false;
  }

  // Group email
  if (($email != '') && (! preg_match($php_regexp_email_name, $email))
      && (! preg_match($php_regexp_email, $email))) {
    $err['msg'] = " $email : $l_j_check_email";
    $err['field'] = 'email';
    return false;
  }

  // If email not empty, must not already exists
  if ($email != '') {
    $mails = get_global_email_used('', $id);

    // Email address not already used
    if (array_key_exists($email, $mails)) {
      $err['msg'] = "$l_email_exist : $email (" . $mails["$email"] . ')';
      $err['field'] = 'email';
      return false;
    }
  }

  // Allow public group handling only if write_admin right
  if (($priv != 1) && (! $perm->check_right('group', $cright_write_admin))) {
    $err['msg'] = "$l_err_public_rights";
    $err['field'] = 'privacy';
    return false;
  }

  // A public group can only be private if user = owner
  if ($priv && ($usercreate > 0) && ($usercreate != $obm["uid"])) {
    $err['msg'] = "$l_only_owner_priv";
    $err['field'] = 'privacy';
    return false;
  }

  // A private group can't have an email address
  if ($priv == 1 && ($email != '')) {
    $err['msg'] = "$l_err_private_email";
    $err['field'] = 'email';
    return false;
  }

  // A system group can't be private
  if ($system && $priv) {
    $err['msg'] = "$l_system_no_priv";
    $err['field'] = 'system';
    return false;
  }

  if ($action == 'update') {
    // MANDATORY: we do not allow to delete samba perms for a system user
    if (($system) && ($gsamba == '1') && ($samba != '1')) {
      $err['msg'] = "$l_err_system_samba";
      $err['field'] = 'samba';
      return false;
    }
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the group can be deleted
// Parameters:
//   - $p_id : group id
// Returns:
//   true if the group can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_group_can_delete($p_id) {
  global $obm, $perm, $err, $cright_write_admin, $ok_msg;
  global $l_warn_delete, $l_cant_delete_system, $l_err_public_rights;

  $delete_ok = true;

  $lgroup = get_group_info($p_id);
  if ($lgroup["system"] != "0") {
    $err["msg"] .= "$l_cant_delete_system";
    $delete_ok = false;
  } else if (($lgroup["privacy"] == "0") &&
	     (! $perm->check_right('group', $cright_write_admin))) {
    $err["msg"] .= $l_err_public_rights;
    $err['field'] = 'privacy';
    $delete_ok = false;
  } else {
    $nb_u = get_group_nb_user($p_id);
    $ok_msg .= "$nb_u $l_warn_delete";
  }

  return $delete_ok;
}


///////////////////////////////////////////////////////////////////////////////
// Get a group parent list
// Parameters:
//   - $id : group id
// Returns:
//   - array of parent groups ids
///////////////////////////////////////////////////////////////////////////////
function get_group_parents($id) {
  global $cdg_sql;

  $query = "SELECT groupgroup_parent_id
    FROM GroupGroup
    WHERE groupgroup_child_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
 
  $parents = array();
  while ($obm_q->next_record()) {
    $g_id = $obm_q->f("groupgroup_parent_id");
    array_push($parents, $g_id);
    $child_parents = get_group_parents($g_id);
    $parents = array_merge($parents, $child_parents);
  }

  return array_unique($parents);
}


/**
 * Search group from a single field 
 * 
 * @param mixed $group
 * @access public
 * @return array of 'length' => DB query results nb, and 'datas' => DB Search results
 */
function run_query_group_ext_search($group) {
  global $obm, $c_all, $cdg_sql, $ctu_sql_limit;

  $pattern = $group["pattern"];
  $restriction = $group["restriction"];

  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain("Group");
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = $group['limit'];
  if(isset($group["first_row"])) $first_row = $group["first_row"] - 1;
  else $first_row = 0;

  $where_privacy = "AND (group_privacy = 0 OR (group_privacy = 1 AND group_usercreate = '$obm[uid]'))";

  if ($restriction == "user") {
    $uid = $obm["uid"];
    $rs = of_right_entity_for_user("Group", $uid, "read");
    if ((is_array($rs["ids"])) && (count($rs["ids"]) > 0)) {
      $where_restriction = " AND (group_id IN (";
      foreach($rs["ids"] as $r_id) {
	$where_restriction .= "$coma'$r_id'";
	$coma = ",";
      }
      $where_restriction .= "))";
    }
  } else if ($restriction == "calendar") {
    $where_privacy = " AND group_privacy=0";
  }

  $query = "SELECT 
      count(*) as resultscount
    FROM UGroup
    WHERE 
      group_name $like '$pattern%' 
      $where_privacy
      $multidomain
      $where_restriction
    ORDER BY group_name  ";

  display_debug_msg($query, $cdg_sql, "run_query_group_ext_search()");
  $obm_q->query($query);
  $obm_q->next_record();
  $resultsCount = $obm_q->f("resultscount");
  $query = "SELECT 
      group_id,
      group_name,
      group_desc
    FROM UGroup
    WHERE 
      group_name $like '$pattern%' 
      $where_privacy
      $multidomain
      $where_restriction
    ORDER BY group_name
    LIMIT $limit OFFSET $first_row";

  display_debug_msg($query, $cdg_sql, "run_query_group_ext_search()");
  $obm_q->query($query);

  return array('length' => $resultsCount, 'datas' => $obm_q);
}

?>
