<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : user_query.inc                                               //
//     - Desc : User query & db File                                         //
// 2000-01-13 Aliacom - Florent Goalabre                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// User query search query execution
// Parameters:
//   - $user[] : user search criteria
//     keys used  : name, perms
//   - $p_new_order : infos for order clause
//   - $p_order_dir : direction for order clause (asc, desc)
//   - $restriction : if and which restriction to set (eg calendar readable)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($user, $p_new_order, $p_order_dir, $restriction="") {
  global $auth, $c_all, $cdg_sql, $ctu_sql_limit;

  $login = sql_search_text_parse($user["login"]);
  $perms = $user["perms"];
  $lname = sql_search_text_parse($user["lastname"]);
  $email = sql_search_text_parse($user["email"]);
  $desc = sql_search_text_parse($user["description"]);
  $group = sql_search_text_parse($user["group"]);
  $archive = $user["archive"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $datebegin = sql_date_format($db_type, "userobm_datebegin", "datebegin");
  $lastaccess = sql_date_format($db_type, "userobm_timelastaccess", "timelastaccess");

  if ($restriction == "calendar") {
    $uid = $auth->auth["uid"];
    $users = of_right_entity_for_consumer("calendar", "user", $uid, "read");
    if ((is_array($users["ids"])) && (count($users["ids"]) > 0)) {
      $where_restriction = " AND (userobm_id IN (";
      foreach($users["ids"] as $u_id) {
	$where_restriction .= "$coma'$u_id'";
	$coma = ",";
      }
      $where_restriction .= "))";
    }
  }

  $where = "";

  // If a login indication has been specified, get it
  if (trim($login) != "") {
     $where .= " userobm_login $like '$login%'";
  }

  // If a user perms indication has been specified, get it
  if (($perms != $c_all) && ($perms != "")) {
     if (trim($where) != "") $where .= " AND";
     $where .= " userobm_perms='$perms'";
  }

  // If a lastname indication has been specified, get it
  if (trim($lname) != "") {
     if (trim($where) != "") $where .= " AND";
     $where .= " userobm_lastname $like '$lname%'";
  }

  // If an email indication has been specified, get it
  if (trim($email) != "") {
     if (trim($where) != "") $where .= " AND";
     $where .= " userobm_email like '%$email%'";
  }

  // If a description indication has been specified, get it
  if (trim($desc) != "") {
     if (trim($where) != "") $where .= " AND";
     $where .= " userobm_description like '%$desc%'";
  }

  // If a group indication has been specified, get it
  if (trim($group) != "") {
     if (trim($where) != "") $where .= " AND";
     $where .= " group_name like '%$group%'";
     $join_grp = "LEFT JOIN UserObmGroup ON userobm_id=userobmgroup_userobm_id
                  LEFT JOIN UGroup ON userobmgroup_group_id=group_id";
  }

  if ($archive != "1") {
    if (trim($where) != "") $where .= " AND";
    $where .= " userobm_archive = '0'";
  }

  if ((trim($where) != "") || (trim($where_restriction) != "")) {
    $whereq = " WHERE $where $where_restriction";
  }

  $select = "SELECT distinct UserObm.*,
      userobm_id as id,
      $datebegin,
      $lastaccess
    FROM UserObm $join_grp
    ";


  // ORDER construction
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "userobm_login";
  $orderq .= " ORDER BY $order $p_order_dir";

  $query = "$select
    $whereq
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(distinct userobm_id) FROM UserObm $join_grp $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// User detail query execution
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, "UserObm.userobm_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "UserObm.userobm_timecreate", "timecreate");
  $datebegin = sql_date_format($db_type, "UserObm.userobm_datebegin", "datebegin");
  $lastaccess = sql_date_format($db_type, "UserObm.userobm_timelastaccess", "userobm_timelastaccess");

  $query = "SELECT UserObm.*,
      $timeupdate,
      $timecreate,
      $datebegin,
      $lastaccess,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM UserObm
         LEFT JOIN UserObm as c ON UserObm.userobm_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON UserObm.userobm_userupdate=u.userobm_id
    WHERE UserObm.userobm_id='$p_id'";

  $obm_q->query($query);
  $obm_q->next_record();
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// User insert query execution
// Parameters:
//   - $user[]   : entry values
//     keys used : login, passwd, perms, archive, *name, email
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($user) {
  global $auth, $cdg_sql, $password_encryption;

  $login = $user["login"];
  $passwd = $user["passwd"];
  if ($passwd != ""){
    if ($password_encryption == "crypt") {
      $passwd_crypt = crypt($passwd);
    } else if ($password_encryption == "md5") {
      $passwd_crypt = md5($passwd);
    } else {
      $passwd_crypt = $passwd;
    }
  }
  $perms = $user["perms"];
  $archive = ($user["archive"] ? "1" : "0");
  $lname = $user["lastname"];
  $fname = $user["firstname"];
  $desc = $user["description"];
  $phone = $user["phone"];
  $phone2 = $user["phone2"];
  $fax = $user["fax"];
  $fax2 = $user["fax2"];
  $email = $user["email"];
  $datebegin = ($user["datebegin"] ? "'".$user["datebegin"]."'" : "null");

  $query = "INSERT INTO UserObm (userobm_timeupdate,
    userobm_timecreate,
    userobm_userupdate,
    userobm_usercreate,
    userobm_login,
    userobm_password,
    userobm_perms,
    userobm_archive,
    userobm_datebegin,
    userobm_lastname,
    userobm_firstname,
    userobm_phone,
    userobm_phone2,
    userobm_fax,
    userobm_fax2,
    userobm_email,
    userobm_description,
    userobm_timelastaccess)
  VALUES (
    null,
    '" . date("Y-m-d H:i:s") . "',
    null,
    ". $auth->auth["uid"] . ",
    '$login',
    '$passwd_crypt',
    '$perms',
    '$archive',
    $datebegin,
    '$lname',
    '$fname',
    '$phone',
    '$phone2',
    '$fax',
    '$fax2',
    '$email',
    '$desc',
    null)";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// User update query execution
// Parameters:
//   - $p_id     : user id
//   - $user[]   : entry values
//     keys used : login, passwd, perms, archive, *name, email
///////////////////////////////////////////////////////////////////////////////
function run_query_update($p_id, $user) {
  global $auth, $cdg_sql, $password_encryption;

  $login = $user["login"];
  $passwd = $user["passwd"];
  if ($passwd != ""){
    if ($password_encryption == "crypt") {
      $passwd_crypt = crypt($passwd);
    } else if ($password_encryption == "md5") {
      $passwd_crypt = md5($passwd);
    } else {
      $passwd_crypt = $passwd;
    }
    $passwd_update = "userobm_password='$passwd_crypt',";
  }
  $perms = $user["perms"];
  $archive = ($user["archive"] ? "1" : "0");
  $datebegin = ($user["datebegin"] ? "'".$user["datebegin"]."'" : "null");
  $lname = $user["lastname"];
  $fname = $user["firstname"];
  $phone = $user["phone"];
  $phone2 = $user["phone2"];
  $fax = $user["fax"];
  $fax2 = $user["fax2"];
  $email = $user["email"];
  $desc = $user["description"];

  $query = "UPDATE UserObm SET
    userobm_timeupdate='". date("Y-m-d H:i:s")."',
    userobm_userupdate=". $auth->auth["uid"] .",
    userobm_login='$login',
    $passwd_update
    userobm_perms='$perms',
    userobm_archive='$archive',
    userobm_datebegin=$datebegin,
    userobm_lastname='$lname',
    userobm_firstname='$fname',
    userobm_phone='$phone',
    userobm_phone2='$phone2',
    userobm_fax='$fax',
    userobm_fax2='$fax2',
    userobm_email='$email',
    userobm_description='$desc'
  WHERE userobm_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // If the user is archived, we unsubscribe it from all Groups
  if ($archive == "1") {
    $retour = run_query_unsubscribe_all_groups($p_id);    
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// User delete query execution
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_id) {
  global $cdg_sql, $c_use_connectors;

  $query = "DELETE FROM UserObm WHERE userobm_id='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // If connectors in use
  if ($c_use_connectors) {
    $now = date("Y-m-d H:i:s");
    $query = "INSERT INTO
      DeletedUser (deleteduser_user_id, deleteduser_timestamp)
      VALUES ('$p_id', '$now')";
    display_debug_msg($query, $cdg_sql);
    $retour = $obm_q->query($query);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// User Groups query execution
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_user_group($p_id) {
  global $cdg_sql, $uid;

  $query = "SELECT *
    FROM UGroup
      LEFT JOIN UserObmGroup ON group_id = userobmgroup_group_id
    WHERE userobmgroup_userobm_id='$p_id'
      AND (group_privacy=0 OR group_usercreate='$uid')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Unsubscribe the user from all groups
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_unsubscribe_all_groups($p_id) {
  global $cdg_sql;

  $query = "DELETE
  FROM UserObmGroup
  WHERE userobmgroup_userobm_id = '$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Groups query execution
///////////////////////////////////////////////////////////////////////////////
function run_query_group() {
  global $cdg_sql, $uid;

  $query = "SELECT *
    FROM UGroup
    ORDER by group_name";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : UserObmGroup global update
// Parameters:
//   - $user[] : user hash info : keys used : id, group_nb, group_X
// Return: number of usergroup inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_update_user_group($user) {
  global $cdg_sql;

  $uid = $user["id"];
  $query = "DELETE FROM UserObmGroup WHERE userobmgroup_userobm_id = '$uid'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $user["group_nb"]) {
    $cpt++;
    $g_id = $user["group_$cpt"];

    $query = "INSERT INTO UserObmGroup (
        userobmgroup_group_id,
        userobmgroup_userobm_id)
      VALUES (
        $g_id,
        $uid)";

    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
    if (! $retour) {
      return -1;
    }
    $cpt_ins++;
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// User environment checking (same user exists ?)
// Parameters:
//   - $p_id   : user id
//   - $user[] : user's values
// Returns:
//   - User Database object with list of similar users
///////////////////////////////////////////////////////////////////////////////
function check_user_context($p_id, $user) {

  $login = $user["login"];
  $passwd = $user["passwd"];
  $perms = $user["perms"];
  $lname = $user["lastname"];
  $fname = $user["firstname"];
  $phone = $user["phone"];
  $email = $user["email"];

  // if a user with same name exists, return false
  $co_q = run_query_check_user($p_id, $login);
  return $co_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return the user with similar names
// Parameters:
//   - $p_id  : user id
//   - $login : login
///////////////////////////////////////////////////////////////////////////////
function run_query_check_user($p_id, $login) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  if ($p_id) {
    $where_user = "AND userobm_id != '$p_id'";
  }

  $query = "SELECT distinct userobm_id, userobm_login
    FROM UserObm
    WHERE userobm_login $like '$login%'
          $where_user";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Data checking and formatting
// Parameters:
//   - $p_id     : user id
//   - $user[]   : values checked
//     keys used : name, passwd, email
///////////////////////////////////////////////////////////////////////////////
function check_data_form($p_id, $user) {
  global $l_exist_error, $l_login_error, $l_password_error, $l_email_error;
  global $l_lname_error, $php_regexp_email;
  global $err_msg, $action, $cdg_sql;

  $id = $user["id"];
  $login = $user["login"];
  $passwd = $user["passwd"];
  $perms = $user["perms"];
  $lname = $user["lastname"];
  $fname = $user["firstname"];
  $phone = $user["phone"];
  $email = $user["email"];

  // MANDATORY: Login
  if (trim($login) == "") {
    $err_msg = $l_login_error." : ". $login;
    return false;
  }
  // MANDATORY: User password on insert
  if (($action == "insert") && (trim($passwd) == "")) {
    $err_msg = $l_password_error." : ". $passwd;
    return false;
  }
  // MANDATORY: Lastname
  if (trim($lname) == "") {
    $err_msg = $l_lname_error." : ". $lname;
    return false;
  }
  // User email
  if (($email != "") && (preg_match($php_regexp_email, $email) == 0)) {
    $err_msg = $l_email_error." : ". $email;
    return false;
  }

  /////////////////////////////////////////////////////////////////////////////
  // if a user with the same name already exists  return false               //
  /////////////////////////////////////////////////////////////////////////////
  $query = "SELECT * FROM UserObm WHERE userobm_login='$login'";
  if ($p_id > 0) {
    $query .= " AND userobm_id!='$p_id'";
  }
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  if ($obm_q->num_rows() > 0) {
    $err_msg = "$login : $l_exist_error";
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Return the number of timetask entries referencing to the user (and where
// project is set)
// Parameters:
//   - $p_id : user Id
///////////////////////////////////////////////////////////////////////////////
function get_linked_user_timetask_nb($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $query = "SELECT count(timetask_id) as nb
    FROM TimeTask
    WHERE timetask_user_id='$p_id'
      AND timetask_projecttask_id > 0
  ";

  display_debug_msg($query, $cdg_sql, "get_linked_user_timetask_nb($p_id)");
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f("nb");

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the user can be deleted
// Parameters:
//   - $p_id : user id
// Returns:
//   true if the user can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_can_delete_user($p_id) {
  global $err_msg, $ok_msg;
  global $l_link_timetask, $l_link_timetask_no;

  $delete_ok = true;

  // Links from Time management (referencing projects)
  $nb = get_linked_user_timetask_nb($p_id);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "$l_link_timetask";
  } else {
    $ok_msg .= "$l_link_timetask_no";
  }

  return $delete_ok;
}


///////////////////////////////////////////////////////////////////////////////
// Query Executions - Purge a deleted user
// Delete all references to the user (UserObmPref, DisplayPref, acl)
// Move all user private contact to public 
// Parameters:
//   - $param_user : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete_profile($param_user)  {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  $query = "DELETE FROM UserObmPref WHERE userobmpref_user_id='$param_user'"; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  $query = "DELETE FROM DisplayPref WHERE display_user_id='$param_user'"; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  // Move all the user contact from private to public
  $query = "UPDATE Contact SET
      contact_privacy='0'
    WHERE contact_usercreate='$param_user'
      AND contact_privacy != '0'"; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  // Move all the user deal from private to public
  $query = "UPDATE Deal SET
      deal_privacy='0'
    WHERE deal_usercreate='$param_user'
      AND deal_privacy != '0'"; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  // Move all the user list from private to public
  $query = "UPDATE List SET
      list_privacy='0'
    WHERE list_usercreate='$param_user'
      AND list_privacy != '0'"; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  // Move all the user events from private to public
  // Not activated ! case of private event with multiple users XXXXX to handle
  $query = "UPDATE CalendarEvent SET
      calendarevent_privacy='0'
    WHERE calendarevent_usercreate='$param_user'
      AND calendarevent_privacy != '0'"; 
  //display_debug_msg($query, $cdg_sql);
  //$obm_q->query($query);

  // Remove all acl to the user and user's one
  $query = "DELETE FROM EntityRight
    WHERE entityright_entity = 'calendar'
      AND entityright_entity_id = '$param_user'"; 
  display_debug_msg($query, $cdg_sql, "run_query_delete_profile()");
  $obm_q->query($query);
  $query = "DELETE FROM EntityRight
    WHERE entityright_consumer = 'user'
      AND entityright_consumer_id = '$param_user'"; 
  display_debug_msg($query, $cdg_sql, "run_query_delete_profile()");
  $obm_q->query($query);

  // XXXXXX what to do with : project allocation entries and timetask entries
}


</script>
