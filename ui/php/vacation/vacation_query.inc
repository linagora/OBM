<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : vacation_query.inc                                           //
//     - Desc : vacation query File                                          //
// 2007-01-22 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Vacation detail query execution
// Parameters:
//   - $u_id : User id
///////////////////////////////////////////////////////////////////////////////
function run_query_vacation_detail($u_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;    
  $db_type = $obm_q->type;

  $vacation_datebegin = sql_date_format($db_type, 'UserObm.userobm_vacation_datebegin', 'vacation_datebegin');
  $vacation_dateend = sql_date_format($db_type, 'UserObm.userobm_vacation_dateend', 'vacation_dateend');
  $u_id = sql_parse_id($u_id, true);
  $query = "SELECT 
    $vacation_datebegin,
    $vacation_dateend , 
    userobm_id,
    userobm_domain_id,
    userobm_login,
    userobm_mail_perms,
    userobm_vacation_enable,
    userobm_vacation_message
  FROM UserObm
  WHERE userobm_id $u_id ";
    
  display_debug_msg($query, $cdg_sql, "run_query_vacation_detail()");
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


//
// updates the database fields according to vacation 
// updates also sieve/cyrus to activate vacation if needed
// returns true if no problem is encountered
// else returns false
//
function update_vacation($params, $vac_q) {
  $retVal = run_query_vacation_update($params, $vac_q->f("userobm_id"));

  if ($retVal == 1) {
    $retVal = update_sieve($params, $vac_q->f("userobm_login"), $vac_q->f("userobm_domain_id"));
  }

  return $retVal;
}

//
// updates the database fields according to $vacation
// return true if ok
// else returns false
//
function run_query_vacation_update($params, $u_id) {
  global $cdg_sql;

  if (!$params['auto_enable']) {
    $params['vacation_datebegin'] = '0';
    $params['vacation_dateend'] = '0';
  }    
  $enabled = sql_parse_int($params["enabled"]);
  $message_update = ", userobm_vacation_message='".addslashes($params["message"])."'";
  $time_begin = $params["time_begin"].":".$params["min_begin"].":00";
  $time_end = $params["time_end"].":".$params["min_end"].":00";
  $vacation_datebegin = ($params['vacation_datebegin'] ? "'".of_isodate_convert($params['vacation_datebegin'])." $time_begin'" : '0');
  $vacation_dateend = ($params['vacation_dateend'] ? "'".of_isodate_convert($params['vacation_dateend'])." $time_end'" : '0');
  $message_update .= ", userobm_vacation_datebegin=".$vacation_datebegin;
  $message_update .= ", userobm_vacation_dateend=".$vacation_dateend;
  $u_id = sql_parse_id($u_id, true);

  // Update done in real-time, so updating UserObm AND P_UserObm
  $query = "UPDATE UserObm
    SET userobm_vacation_enable = $enabled
    $message_update
    WHERE userobm_id $u_id ";
    
  display_debug_msg($query, $cdg_sql, "run_query_vacation_update()");
  $obm_q = new DB_OBM;
  $ret = $obm_q->query($query);

  $query = "UPDATE P_UserObm
    SET userobm_vacation_enable = $enabled
    $message_update
    WHERE userobm_id $u_id ";

  display_debug_msg($query, $cdg_sql, "run_query_vacation_update()");
  $obm_q = new DB_OBM;
  $ret = $obm_q->query($query);
 
  return $ret;
}


//
// updates sieve status according to $vacation,
// ie. upload and activate the new script in sieve
// returns true if ok,
// else returns false and sets $err["msg"];
//
function update_sieve($vacation, $u_uid, $u_domain_id) {  
  global $err, $l_sieve_login_failed, $cmd_vacation;

  $cmd = "$cmd_vacation --login ".$u_uid." --domain ".$u_domain_id;
  
  display_debug_msg($cmd, $cdg_exe, "update_sieve()");
  exec($cmd);
  
  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Vacation Data checking and formatting
// Parameters:
//   - $params : values checked
///////////////////////////////////////////////////////////////////////////////
function check_vacation_data_form($params) {
  global $err, $l_empty_message;

  // le message doit être non-vide si le renvoi est activé
  
  if ($params["enabled"] == 0) {
    return true;
  }

  if (trim($params["message"]) == "") {
    $err["msg"] = "$l_empty_message";
    $err["field"] = "message";
    return false;
  }

  return true;
}


</script>
