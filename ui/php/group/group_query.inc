<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : group_query.inc                                              //
//     - Desc : group query File                                             //
// 2003-08-22 Aliacom                                                        //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Group Search query execution 
// Parameters :
//   - $group[]      : list search criteria
//     keys used     : name, user
///////////////////////////////////////////////////////////////////////////////
function run_query_search($group) {
  global $cdg_sql, $ctu_sql_limit;
  
  $name = sql_search_text_parse($group["name"]);
  $email = sql_search_text_parse($group["email"]);
  $user = sql_search_text_parse($group["user"]);
  $new_order = $group["new_order"];
  $order_dir = $group["order_dir"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $timeupdate = sql_date_format($db_type,"UGroup.group_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type,"UGroup.group_timecreate", "timecreate");

  $where = "group_name $like '$name%'";
  if ($user != '') {
    $join_user = "
      LEFT JOIN UserObmGroup ON group_id=UserObmGroup_group_id
      LEFT JOIN UserObm as C ON UserObmGroup.userobmgroup_userobm_id=C.userobm_id";
    $where .= " and C.userobm_lastname $like '$user%'";
  }
  if ($email != '') {
    $where .= " and group_email $like '%$email%'";
  }
  // only the one which are allowed (ie. publics )
  $where .=  " and " .sql_obm_entity_privacy("group");
    
  $whereq = "where $where";

  // ORDER construction
  $order = (strcmp($new_order,"") != 0) ? $new_order : "group_name";
  $orderq .= " order by $order $order_dir";

  $query = "select distinct UGroup.*, group_id as id,
      $timecreate,
      $timeupdate,
      A.userobm_login as usercreate,
      B.userobm_login as userupdate
    from UGroup
      $join_user
      LEFT JOIN UserObm as A ON UGroup.group_usercreate=A.userobm_id
      LEFT JOIN UserObm as B ON UGroup.group_userupdate=B.userobm_id
    $whereq 
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("select count(*) from UGroup $join_user $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql);
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
function run_query_search_possible_children($group) {
  global $cdg_sql;
  
  $id = $group["ext_id"];
  $name = $group["name"];
  $email = $group["email"];
  $user = $group["user"];
  $new_order = $group["new_order"];
  $order_dir = $group["order_dir"];

  $parents = get_group_parents($id);

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  $timeupdate = sql_date_format($db_type,"UGroup.group_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type,"UGroup.group_timecreate", "timecreate");

  // WHERE Clause

  // only the one which are allowed (ie. publics )
  $where .=  " and " .sql_obm_entity_privacy("group");
  if ($user != '') {
    $where .= " and C.userobm_lastname $like '$user%'";
  }
  if ($email != '') {
    $where .= " and group_email $like '%$email%'";
  }
  while (list ($key, $g_id) = each($parents)) {
    $where .= " and group_id != '$g_id'";
  }
    
  $query = "select distinct UGroup.*, group_id as id,
      $timecreate,
      $timeupdate,
      A.userobm_login as usercreate,
      B.userobm_login as userupdate
    from 
      (UGroup LEFT JOIN UserObmGroup ON group_id=UserObmGroup_group_id)
      LEFT JOIN UserObm as C ON UserObmGroup.userobmgroup_userobm_id=C.userobm_id
      LEFT JOIN UserObm as A ON UGroup.group_usercreate=A.userobm_id
      LEFT JOIN UserObm as B ON UGroup.group_userupdate=B.userobm_id
    where group_name $like '$name%'
      and group_id != '$id'
      $where";

  // ORDER construction

  $order = (strcmp($new_order,"") != 0) ? $new_order : "group_name";
  $query .= " order by $order $order_dir";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Group detail query execution                                              //
// Parameters:
//   - $id : group id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type,"UGroup.group_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type,"UGroup.group_timecreate", "timecreate");

  $query = "select *,
      $timecreate,
      $timeupdate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    from UGroup
         left join UserObm as c on group_usercreate=c.userobm_id
         left join UserObm as u on group_userupdate=u.userobm_id
    where group_id = '$id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : group insertion
// Parameters:
//   - $group[] : group hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($group) {
  global $uid, $cdg_sql;

  $id = $group["id"];
  $system = ($group["system"] == 1) ? 1 : 0;
  $name = $group["name"];
  $desc = $group["desc"];
  $email = $group["email"];
  $priv = (isset($group["priv"]) ? $group["priv"] : '0');
  $now = date("Y-m-d H:i:s");

  $query = "insert into UGroup (group_timeupdate,
    group_timecreate,
    group_userupdate,
    group_usercreate,
    group_system,
    group_privacy,
    group_name,
    group_desc,
    group_email)
  values (null,
    '$now',
    null,
    '$uid',
    '$system',
    '$priv',
    '$name',
    '$desc',
    '$email')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Group Update query execution                                              //
// Parameters:
//   - $group[] : group hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_update($group) {
  global $uid, $cdg_sql;

  $id = $group["id"];
  $name = $group["name"];
  $desc = $group["desc"];
  $email = $group["email"];
  $system = ($group["system"] == 1) ? 1 : 0;
  $priv = (isset($group["priv"]) ? $group["priv"] : '0');
  $now = date("Y-m-d H:i:s");
  
  $query = "update UGroup set
    group_timeupdate='$now',
    group_userupdate='$uid',
    group_system='$system',
    group_privacy='$priv',
    group_name='$name',
    group_desc='$desc',
    group_email='$email'
  where group_id='$id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution                                                  //
// Parameters:
//   - $p_id : group id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_id) {
  global $cdg_sql;

  // Delete all users registrations to this group
  $query = "delete from UserObmGroup where userobmgroup_group_id='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Delete all groups registration referencing this group
  $query = "delete from GroupGroup
    where groupgroup_child_id='$p_id'
      or groupgroup_parent_id='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Delete the Group
  $query = "delete from UGroup where group_id='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : UserObmGroup conditionnal insertion                     //
// Parameters:
//   - $group[] : group hash info : keys used : id, user_nb, userX
// Return: number of users inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_usergroup_insert($group) {
  global $auth, $cdg_sql;

  $id = $group["id"];
  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $group["user_nb"]) {
    $cpt++;
    $u_id = $group["user$cpt"];

    $query = "select * from UserObmGroup
      where userobmgroup_group_id='$id'
        and userobmgroup_userobm_id='$u_id'";
    display_debug_msg($query, $cdg_sql);
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $query = "insert into UserObmGroup (userobmgroup_group_id,
        userobmgroup_userobm_id) values ($id, $u_id)";

      display_debug_msg($query, $cdg_sql);
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      $cpt_ins++;
    }
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : UserObmGroup deletion                                   //
// Parameters:
//   - $group[] : group hash info : keys used : id, user_nb, userX
// Return: number of users deleted
///////////////////////////////////////////////////////////////////////////////
function run_query_usergroup_delete($group) {
  global $auth, $cdg_sql;

  $id = $group["id"];
  $cpt = 0;
  $cpt_del = 0;
  while ($cpt < $group["user_nb"]) {
    $cpt++;
    $u_id = $group["user$cpt"];

    $query = "delete from UserObmGroup
      where userobmgroup_group_id='$id' and userobmgroup_userobm_id='$u_id'";
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
// Query execution : GroupGroup conditionnal insertion                       //
// Parameters:
//   - $group[] : group hash info : keys used : id, group_nb, group_X
// Return: number of groups inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_groupgroup_insert($group) {
  global $auth, $cdg_sql;

  $id = $group["id"];
  $cpt = 0;
  $cpt_ins = 0;

  $parents = get_group_parents($id);

  while ($cpt < $group["group_nb"]) {
    $cpt++;
    $g_id = $group["group_$cpt"];

    $query = "select * from GroupGroup
      where groupgroup_parent_id='$id'
        and groupgroup_child_id='$g_id'";
    display_debug_msg($query, $cdg_sql);
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist and doesn't create a loop, insert it
    if ( ($test_q->num_rows() == 0)
         && (! in_array($g_id, $parents))
         && ($id != $g_id)
       ) {
        $query = "insert into GroupGroup (groupgroup_parent_id,
        groupgroup_child_id) values ($id, $g_id)";

      display_debug_msg($query, $cdg_sql);
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      $cpt_ins++;
    }
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : GroupGroup deletion                                     //
// Parameters:
//   - $group[] : group hash info : keys used : id, group_nb, group_X
// Return: number of groups deleted
///////////////////////////////////////////////////////////////////////////////
function run_query_groupgroup_delete($group) {
  global $auth, $cdg_sql;

  $id = $group["id"];
  $cpt = 0;
  $cpt_del = 0;
  while ($cpt < $group["group_nb"]) {
    $cpt++;
    $g_id = $group["group_$cpt"];

    $query = "delete from GroupGroup
      where groupgroup_parent_id='$id' and groupgroup_child_id='$g_id'";

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
// Query Execution : Get the users of the given group                        //
// Parameters:
//   - $group[] : Group parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_user_group($group) {
  global $cdg_sql;

  $obm_q = new DB_OBM; 
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type);

  $id = $group["id"];
  $entity = $group["entity"];
  $new_order = $group["new_order"];
  $order_dir = $group["order_dir"];

  if (($entity == "group_user") && (trim($new_order) != "")) {
    $order = "order by $new_order $order_dir";
  } else {
    $order = "order by userobm_lastname";
  }

  $query = "SELECT userobmgroup_userobm_id as group_user_id,
         userobmgroup_userobm_id as id,
         userobm_timeupdate as group_user_timeupdate,
         userobm_timecreate as group_user_timecreate,
         userobm_userupdate as group_user_userupdate,
         userobm_usercreate as group_user_usercreate,
         userobm_lastname as group_user_lastname,
         userobm_firstname as group_user_firstname,
         userobm_phone as group_user_phone,
         userobm_email as group_user_email
      FROM UserObmGroup LEFT JOIN UserObm ON userobmgroup_userobm_id=userobm_id
      WHERE userobmgroup_group_id='$id'
         $order
         $limit";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get the groups members of the given group               //
// Parameters:
//   - $group[] : Group parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_group_group($group) {
  global $cdg_sql;

  $obm_q = new DB_OBM; 
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type);

  $id = $group["id"];
  $entity = $group["entity"];
  $new_order = $group["new_order"];
  $order_dir = $group["order_dir"];

  if (($entity == "group_group") && (trim($new_order) != "")) {
    $order = "order by $new_order $order_dir";
  } else {
    $order = "order by group_name";
  }

  $query = "SELECT groupgroup_child_id as child_id,
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
  display_debug_msg($query, $cdg_sql);
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

  $query = "select count(*) from UserObmGroup
    where userobmgroup_group_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f(0);
  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Return the group infos
// Parameters:
//   - $id : group id
///////////////////////////////////////////////////////////////////////////////
function get_group_info($id) {
  global $cdg_sql;

  if ($id == "") {
    return false;
  }

  $query = "select * from UGroup where group_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $g["name"] = $obm_q->f("group_name");
  $g["system"] = $obm_q->f("group_system");

  return $g;
}


///////////////////////////////////////////////////////////////////////////////
// Check if a group (except with id given) with the name given already exists
// Parameters:
//   - $name : name to search for
//   - $id   : group id to exclude
// Returns:
//   - true (if a group exists) or false
///////////////////////////////////////////////////////////////////////////////
function get_group_name_exists($name, $id="") {
  global $cdg_sql;

  if ($id != "") {
    $where_id = "group_id!='$id' and";
  }

  $query = "select group_id, group_name
    from UGroup
    where $where_id group_name='$name'";

  display_debug_msg($query, $cdg_sql);
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
function run_query_check_group($id, $name, $desc) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  if ($id != "") {
    $where_id = "group_id!='$id' and";
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

  $query = "select distinct group_id, group_name, group_desc
     from UGroup
     where $where_id
       ($wname $wdesc)";

  display_debug_msg($query, $cdg_sql);
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
  $g_q = run_query_check_group($id, $name, $desc);

  return $g_q;
}


///////////////////////////////////////////////////////////////////////////////
// Group Form Data checking and formatting
// Parameters:
//   - $group[]  : values checked
//     keys used : name, desc, email
///////////////////////////////////////////////////////////////////////////////
function check_data_form($group) {
  global $php_regexp_email_name, $l_fill_name, $l_group_exists, $l_email_exist;
  global $err_msg, $l_j_check_email;

  $id = $group["id"];
  $name = $group["name"];
  $desc = $group["desc"];
  $email = $group["email"];

  // MANDATORY: Group name not empty
  if (trim($name) == "") {
    $err_msg = $l_fill_name;
    return false;
  }

  // MANDATORY: Group name unique
  if (get_group_name_exists($name, $id)) {
    $err_msg = "$l_group_exists ($name)";
    return false;
  }

  // Group email
  if (($email != "") && (! preg_match($php_regexp_email_name, $email))) {
    $err_msg = " $email : $l_j_check_email";
    return false;
  }

  // If email not empty, must not already exists
  if ($email != "") {
    $mails = get_email_used("", $id);

    // Email address not already used
    if (array_key_exists($email, $mails)) {
      $err_msg = "$l_email_exist : $email (" . $mails["$email"] . ")";
      $err_field = "email";
      return false;
    }
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Get a group parent list                                                   //
// Parameters:
//   - $id : group id
// Returns:
//   - array of parent groups ids
///////////////////////////////////////////////////////////////////////////////
function get_group_parents($id) {
  global $cdg_sql;

  $query = "select groupgroup_parent_id
    from GroupGroup where groupgroup_child_id='$id'";

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


</script>
