<SCRIPT language="php">
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
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($group, $p_new_order, $p_order_dir) {
  global $cdg_sql;
  
  $name = $group["name"];
  $email = $group["email"];
  $user = $group["user"];

  $obm_q = new DB_OBM;

  if ($user != '') {
    $where .= " and C.userobm_lastname like '$user%'";
  }
  if ($email != '') {
    $where .= " and group_email like '%$email%'";
  }
  
  $query = "select distinct UGroup.*, group_id as Id,
      UNIX_TIMESTAMP(UGroup.group_timecreate) as timecreate,
      UNIX_TIMESTAMP(UGroup.group_timeupdate) as timeupdate,
      A.userobm_login as usercreate,
      B.userobm_login as userupdate
    from 
      (UGroup LEFT JOIN UserObmGroup ON group_id=UserObmGroup_groupid)
      LEFT JOIN UserObm as C ON UserObmGroup.userobmgroup_userobmid=C.userobm_id
      LEFT JOIN UserObm as A ON UGroup.group_usercreate=A.userobm_id
      LEFT JOIN UserObm as B ON UGroup.group_userupdate=B.userobm_id
    where group_name like '$name%'
      $where";


  // ORDER construction

  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "group_name";
  $query .= " order by $order $p_order_dir";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Possible children Group Search query execution 
// Parameters :
//   - $group[]      : list search criteria
//     keys used     : name, user
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search_possible_children($group, $p_new_order, $p_order_dir) {
  global $cdg_sql;
  
  $id = $group["ext_id"];
  $name = $group["name"];
  $email = $group["email"];
  $user = $group["user"];

  $parents = get_group_parents($id);

  $obm_q = new DB_OBM;

  // WHERE Clause

  if ($user != '') {
    $where .= " and C.userobm_lastname like '$user%'";
  }
  if ($email != '') {
    $where .= " and group_email like '%$email%'";
  }
  while (list ($key, $g_id) = each($parents)) {
    $where .= " and group_id != '$g_id'";
  }
    
  $query = "select distinct UGroup.*, group_id as Id,
      UNIX_TIMESTAMP(UGroup.group_timecreate) as timecreate,
      UNIX_TIMESTAMP(UGroup.group_timeupdate) as timeupdate,
      A.userobm_login as usercreate,
      B.userobm_login as userupdate
    from 
      (UGroup LEFT JOIN UserObmGroup ON group_id=UserObmGroup_groupid)
      LEFT JOIN UserObm as C ON UserObmGroup.userobmgroup_userobmid=C.userobm_id
      LEFT JOIN UserObm as A ON UGroup.group_usercreate=A.userobm_id
      LEFT JOIN UserObm as B ON UGroup.group_userupdate=B.userobm_id
    where group_name like '$name%'
      and group_id != '$id'
      $where";

  // ORDER construction

  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "group_name";
  $query .= " order by $order $p_order_dir";

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
  $query = "select *,
      UNIX_TIMESTAMP(UGroup.group_timecreate) as timecreate,
      UNIX_TIMESTAMP(UGroup.group_timeupdate) as timeupdate
    from UGroup where group_id = '$id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query) ;
  $obm_q->next_record();
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : group insertion                                         //
// Parameters:
//   - $group[] : group hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($group) {
  global $auth, $cdg_sql;

  $id = $group["id"];
  $name = $group["name"];
  $desc = $group["desc"];
  $email = $group["email"];

  $query = "insert into UGroup (group_timeupdate,
    group_timecreate,
    group_userupdate,
    group_usercreate,
    group_name,
    group_desc,
    group_email)
  values (null,
    '" . date("Y-m-d H:i:s") ."',
    null,
    '" . $auth->auth["uid"] . "',
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
  global $auth, $cdg_sql;

  $id = $group["id"];
  $name = $group["name"];
  $desc = $group["desc"];
  $email = $group["email"];
  
  $query = "update UGroup set group_timeupdate='". date("Y-m-d H:i:s")."',
    group_userupdate='".$auth->auth["uid"]."',
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

  // Delete all contacts registration to this group
  $query = "delete from UserObmGroup where userobmgroup_groupid='$p_id'";
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
      where userobmgroup_groupid='$id'
        and userobmgroup_userobmid='$u_id'";
    display_debug_msg($query, $cdg_sql);
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $query = "insert into UserObmGroup (userobmgroup_groupid,
        userobmgroup_userobmid) values ($id, $u_id)";

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
      where userobmgroup_groupid='$id' and userobmgroup_userobmid='$u_id'";
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
      where groupgroup_parentid='$id'
        and groupgroup_childid='$g_id'";
    display_debug_msg($query, $cdg_sql);
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist and doesn't create a loop, insert it
    if ( ($test_q->num_rows() == 0)
         && (! in_array($g_id, $parents))
         && ($id != $g_id)
       ) {
      $query = "insert into GroupGroup (groupgroup_parentid,
        groupgroup_childid) values ($id, $g_id)";

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
      where groupgroup_parentid='$id' and groupgroup_childid='$g_id'";

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
//   - $group_id : Group id
// -- Optionnals
//   - $entity   : dataset entity to order (to handle multiple Display in 1 p)
///////////////////////////////////////////////////////////////////////////////
function run_query_user_group($group, $entity="") {
  global $cdg_sql;

  $id = $group["id"];
  $new_order = $group["new_order"];
  $order_dir = $group["order_dir"];

  if (($entity == "group_user") && (trim($new_order) != "")) {
    $order = "order by $new_order $order_dir";
  } else {
    $order = "order by userobm_lastname";
  }

  $query = "select userobmgroup_userobmid as group_user_id,
         userobmgroup_userobmid as Id,
         userobm_timeupdate as group_user_timeupdate,
         userobm_timecreate as group_user_timecreate,
         userobm_userupdate as group_user_userupdate,
         userobm_usercreate as group_user_usercreate,
         userobm_lastname as group_user_lastname,
         userobm_firstname as group_user_firstname,
         userobm_phone as group_user_phone,
         userobm_email as group_user_email
      from UserObmGroup LEFT JOIN UserObm ON userobmgroup_userobmid=userobm_id
      where userobmgroup_groupid='$id'
         $order";

  $obm_q = new DB_OBM; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get the groups members of the given group               //
// Parameters:
//   - $group_id  : Group id
// -- Optionnals
//   - $new_order : field for the older clause
//   - $order_dir : direction for the older clause
//   - $entity    : dataset entity to order (to handle multiple Display in 1 p)
///////////////////////////////////////////////////////////////////////////////
function run_query_group_group($group_id, $new_order="", $order_dir="", $entity="") {
  global $cdg_sql;

  if (($entity == "group_group") && (trim($new_order) != "")) {
    $order = "order by $new_order $order_dir";
  } else {
    $order = "order by group_name";
  }

  $query = "select groupgroup_childid as child_id,
         groupgroup_childid as Id,
         group_timeupdate,
         group_timecreate,
         group_userupdate,
         group_usercreate,
         group_id,
         group_name,
         group_desc,
         group_email
      from GroupGroup LEFT JOIN UGroup ON groupgroup_childid=group_id
      where groupgroup_parentid='$group_id'
         $order";

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
    where userobmgroup_groupid='$id'";

  display_debug_msg($query, $cdg_sql);
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
function get_group_name_exists($name, $id="") {
  global $cdg_sql;

  $query = "select group_id, group_name
    from UGroup
    where group_id!='$id' and group_name='$name'";

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

  // If name is short, we test equality, else similarity
  if (strlen($name) > 2) {
    $wname = "group_name like '%$name%'";
  } else {
    $wname = "group_name = '$name'";
  }

  // If desc is short, we test equality, else similarity
  if (trim($desc) != "") {
    if (strlen($desc) > 2) {
      $wdesc = "group_desc like '%$desc%'";
    } else {
      $wdesc = "group_desc = '$desc'";
    }
  }
  if ($wdesc != "") $wdesc = "or $wdesc";

  $query = "select distinct group_id, group_name, group_desc
     from UGroup
     where group_id!='$id'
       and ($wname $wdesc)";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
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
  $q_q = run_query_check_group($id, $name, $desc);

  return $q_q;
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

  // MANDATORY: List name unique
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
    //    $mails = get_email_used("", $id);

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
// Get the Name of a Group from its Id                                       //
// Parameters:
//   - $id : group id
// Returns:
//   - Group Name
///////////////////////////////////////////////////////////////////////////////
function get_group_name($id) {
  global $cdg_sql;

  $query = "select group_name from UGroup where group_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("group_name");
  return $retour;
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

  $query = "select groupgroup_parentid
    from GroupGroup where groupgroup_childid='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
 
  $parents = array();
  while ($obm_q->next_record()) {
    $g_id = $obm_q->f("groupgroup_parentid");
    array_push($parents, $g_id);
    $child_parents = get_group_parents($g_id);
    $parents = array_merge($parents, $child_parents);
  }

  return array_unique($parents);
}


</script>
