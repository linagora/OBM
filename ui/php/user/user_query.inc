<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : user_query.inc                                               //
//     - Desc : User query & db File                                         //
// 2000-01-13 Florent Goalabre : Last Update 2001-07-27 Francois Bloque      //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// User query search query execution                                         //
// Parameters:
//   - $user[] : user search criteria
//     keys used  : name, perms
//   - $p_new_order : infos for order clause
//   - $p_order_dir : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($user, $p_new_order, $p_order_dir) {
  global $c_all, $cdg_sql;

  $login = $user["login"];
  $lname = $user["lastname"];
  $perms = $user["perms"];
  $email = $user["email"];
  $archive = $user["archive"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  $lastaccess = sql_date_format($db_type, "userobm_timelastaccess", "userobm_timelastaccess");

  $select = "select UserObm.*,
      userobm_id as Id,
      $lastaccess
    from UserObm";
  $where = "";

  // If a login indication has been specified, get it
  if (trim($login) != "") {
     $where .= " userobm_login $like '$login%'";
  }

  // If a user perms indication has been specified, get it
  if (($perms != $c_all) && ($perms != "")) {
     if (trim($where) != "") $where .= " and";
     $where .= " userobm_perms='$perms'";
  }

  // If a lastname indication has been specified, get it
  if (trim($lname) != "") {
     if (trim($where) != "") $where .= " and";
     $where .= " userobm_lastname $like '$lname%'";
  }

  // If an email indication has been specified, get it
  if (trim($email) != "") {
     if (trim($where) != "") $where .= " and";
     $where .= " userobm_email like '%$email%'";
  }

  if ($archive != "1") {
    if (trim($where) != "") $where .= " and";
    $where .= " userobm_archive = '0'";
  }

  if (trim($where) != "") $where = " where" . $where;
  $query = $select . $where;

  // ORDER construction
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "userobm_login";
  $query .= " order by $order $p_order_dir";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// User query search query execution                                         //
// Parameters:
//   - $user[] : user search criteria
//     keys used  : name, perms
//   - $p_new_order : infos for order clause
//   - $p_order_dir : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_get_search($user, $p_new_order, $p_order_dir) {
  global $c_all, $cdg_sql,$auth;

  $login = $user["login"];
  $lname = $user["lastname"];
  $perms = $user["perms"];
  $archive = $user["archive"];
  $uid = $auth->auth["uid"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  $lastaccess = sql_date_format($db_type, "userobm_timelastaccess", "userobm_timelastaccess");

  $select = "select u.*,
      userobm_id as Id,
      $lastaccess
    from UserObm  as u
      LEFT OUTER JOIN CalendarRight as c 
        ON u.userobm_id = c.calendarright_ownerid
          AND c.calendarright_customerid = '$uid'
    WHERE (c.calendarright_write = 1 OR u.userobm_id='$uid') AND";
  $where = "";

  // If a login indication has been specified, get it
  if (trim($login) != "") {
    $where .= " userobm_login $like '$login%'";
  }

  // If a user perms indication has been specified, get it
  if (($perms != $c_all) && ($perms != "")) {
    if (trim($where) != "") $where .= " and";
    $where .= " userobm_perms='$perms'";
  }

  // If a name indication has been specified, get it
  if (trim($lname) != "") {
    if (trim($where) != "") $where .= " and";
    $where .= " userobm_lastname $like '$lname%'";
  }

  if ($archive != "1") {
    if (trim($where) != "") $where .= " and";
    $where .= " userobm_archive = '0'";
  }

  $query = $select . $where;

  // ORDER construction
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "userobm_login";
  $query .= " order by $order $p_order_dir";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// User detail query execution                                               //
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, "UserObm.userobm_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "UserObm.userobm_timecreate", "timecreate");
  $lastaccess = sql_date_format($db_type, "UserObm.userobm_timelastaccess", "userobm_timelastaccess");

  $query = "select UserObm.*,
      $timeupdate,
      $timecreate,
      $lastaccess,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    from UserObm
         left join UserObm as c on UserObm.userobm_usercreate=c.userobm_id
         left join UserObm as u on UserObm.userobm_userupdate=u.userobm_id
    where UserObm.userobm_id='$p_id'";

  $obm_q->query($query);
  $obm_q->next_record();
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// User insert query execution                                               //
// Parameters:
//   - $user[]   : entry values
//     keys used : login, passwd, perms, archive, *name, email
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($user) {
  global $auth, $cdg_sql;

  $login = $user["login"];
  $passwd = $user["passwd"];
  if ($passwd != "") {
    $passwd_crypt = $user["passwd_crypt"];
  }
  $perms = $user["perms"];
  $archive = ($user["archive"] ? "1" : "0");
  $lname = $user["lastname"];
  $fname = $user["firstname"];
  $phone = $user["phone"];
  $email = $user["email"];

  $query = "insert into UserObm (userobm_timeupdate,
    userobm_timecreate,
    userobm_userupdate,
    userobm_usercreate,
    userobm_login,
    userobm_password,
    userobm_perms,
    userobm_archive,
    userobm_lastname,
    userobm_firstname,
    userobm_phone,
    userobm_email,
    userobm_timelastaccess)
  values (
    null,
    '" . date("Y-m-d H:i:s") . "',
    null,
    ". $auth->auth["uid"] . ",
    '$login',
    '$passwd_crypt',
    '$perms',
    '$archive',
    '$lname',
    '$fname',
    '$phone',
    '$email',
    null)";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// User update query execution                                               //
// Parameters:
//   - $p_id     : user id
//   - $user[]   : entry values
//     keys used : login, passwd, perms, archive, *name, email
///////////////////////////////////////////////////////////////////////////////
function run_query_update($p_id, $user) {
  global $auth, $cdg_sql;

  $login = $user["login"];
  $passwd = $user["passwd"];
  if ($passwd != "") {
    $passwd_crypt = $user["passwd_crypt"];
    $passwd_update = "userobm_password='$passwd_crypt',";
  }
  $perms = $user["perms"];
  $archive = ($user["archive"] ? "1" : "0");
  $lname = $user["lastname"];
  $fname = $user["firstname"];
  $phone = $user["phone"];
  $email = $user["email"];

  $query = "update UserObm set
    userobm_timeupdate='". date("Y-m-d H:i:s")."',
    userobm_userupdate=". $auth->auth["uid"] .",
    userobm_login='$login',
    $passwd_update
    userobm_perms='$perms',
    userobm_archive='$archive',
    userobm_lastname='$lname',
    userobm_firstname='$fname',
    userobm_phone='$phone',
    userobm_email='$email'
  where userobm_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// User delete query execution                                               //
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_id) {
  global $cdg_sql;

  $query = "delete from UserObm where userobm_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// User Groups query execution                                               //
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_user_group($p_id) {
  global $cdg_sql;

  $query = "select * from UGroup
    left join UserObmGroup on group_id = userobmgroup_groupid
    where userobmgroup_userobmid='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Groups query execution                                                    //
///////////////////////////////////////////////////////////////////////////////
function run_query_group() {
  global $cdg_sql;

  $query = "select * from UGroup order by group_name";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : UserObmGroup global update                              //
// Parameters:
//   - $user[] : user hash info : keys used : id, group_nb, group_X
// Return: number of usergroup inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_update_user_group($user) {
  global $cdg_sql;

  $uid = $user["id"];
  $query = "delete from UserObmGroup where userobmgroup_userobmid = '$uid'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $user["group_nb"]) {
    $cpt++;
    $g_id = $user["group_$cpt"];

    $query = "insert into UserObmGroup (userobmgroup_groupid,
        userobmgroup_userobmid) values ($g_id, $uid)";

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
// User environment checking (same user exists ?)                            //
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
    $where_user = "and userobm_id != '$p_id'";
  }

  $query = "select distinct userobm_id, userobm_login
            from UserObm
            where userobm_login $like '$login%'
              $where_user";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Data checking and formatting                                              //
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
  if (($email != "") && (eregi($php_regexp_email, $email) == false)) {
    $err_msg = $l_email_error." : ". $email;
    return false;
  }

  /////////////////////////////////////////////////////////////////////////////
  // if a user with the same name already exists  return false               //
  /////////////////////////////////////////////////////////////////////////////
  $query = "select * from UserObm where userobm_login='$login'";
  if ($p_id > 0) {
    $query .= " and userobm_id!='$p_id'";
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
// Query Executions - Purge a deleted user
// Delete all references to the user (UserObmPref, DisplayPref)
// Move all user private contact to public 
// Parameters:
//   - $param_user : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete_profil($param_user)  {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  $query = "Delete from UserObmPref where userobmpref_user_id='$param_user'"; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  $query = "Delete from DisplayPref where display_user_id='$param_user'"; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  // Move all the user contact from private to public
  $query = "Update Contact set
      contact_visibility='0'
    where contact_usercreate='$param_user'
      and contact_visibility != '0'"; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  // Move all the user deal from private to public
  $query = "Update Deal set
      deal_visibility='0'
    where deal_usercreate='$param_user'
      and deal_visibility != '0'"; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  // Move all the user list from private to public
  $query = "Update List set
      list_visibility='0'
    where list_usercreate='$param_user'
      and list_visibility != '0'"; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
}


</script>
