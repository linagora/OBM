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
  global $c_all, $cdg_sql, $ctu_sql_limit;

  $login = sql_search_text_parse($user["login"]);
  $perms = $user["perms"];
  $lname = sql_search_text_parse($user["lastname"]);
  $email = sql_search_text_parse($user["email"]);
  $archive = $user["archive"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $datebegin = sql_date_format($db_type, "userobm_datebegin", "datebegin");
  $lastaccess = sql_date_format($db_type, "userobm_timelastaccess", "timelastaccess");

  $select = "select UserObm.*,
      userobm_id as id,
      $datebegin,
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

  if (trim($where) != "") {
    $whereq = " where $where";
  }

  // ORDER construction
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "userobm_login";
  $orderq .= " order by $order $p_order_dir";

  $query = "$select
    $whereq
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("select count(*) from UserObm $whereq");
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

  $query = "select UserObm.*,
      $timeupdate,
      $timecreate,
      $datebegin,
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

  $query = "insert into UserObm (userobm_timeupdate,
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
  values (
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
// User update query execution                                               //
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

  $query = "update UserObm set
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
  where userobm_id='$p_id'";

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
  global $cdg_sql, $uid;

  $query = "SELECT *
    FROM UGroup
    LEFT join UserObmGroup on group_id = userobmgroup_group_id
    WHERE userobmgroup_userobm_id='$p_id'
      and (group_privacy=0 OR group_usercreate='$uid')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Unsubscribe the user from all groups                                      //
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
// Groups query execution                                                    //
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
// Query execution : UserObmGroup global update                              //
// Parameters:
//   - $user[] : user hash info : keys used : id, group_nb, group_X
// Return: number of usergroup inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_update_user_group($user) {
  global $cdg_sql;

  $uid = $user["id"];
  $query = "delete from UserObmGroup where userobmgroup_userobm_id = '$uid'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $user["group_nb"]) {
    $cpt++;
    $g_id = $user["group_$cpt"];

    $query = "insert into UserObmGroup (userobmgroup_group_id,
        userobmgroup_userobm_id) values ($g_id, $uid)";

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
  if (($email != "") && (preg_match($php_regexp_email, $email) == 0)) {
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
function run_query_delete_profile($param_user)  {
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
      contact_privacy='0'
    where contact_usercreate='$param_user'
      and contact_privacy != '0'"; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  // Move all the user deal from private to public
  $query = "Update Deal set
      deal_privacy='0'
    where deal_usercreate='$param_user'
      and deal_privacy != '0'"; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  // Move all the user list from private to public
  $query = "Update List set
      list_privacy='0'
    where list_usercreate='$param_user'
      and list_privacy != '0'"; 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  // Move all the user events from private to public
  // Not activated ! case of private event with multiple users..... to handle
  $query = "Update CalendarEvent set
      calendarevent_privacy='0'
    where calendarevent_usercreate='$param_user'
      and calendarevent_privacy != '0'"; 
  //display_debug_msg($query, $cdg_sql);
  //$obm_q->query($query);
}


</script>
