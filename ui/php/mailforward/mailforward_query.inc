<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailforward_query.inc                                        //
//     - Desc : Mail Forwarding query & db File                              //
// 2002-09-01 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Mail Forwardig query execution
// Parameters:
//   - $u_id : User id
///////////////////////////////////////////////////////////////////////////////
function run_query_mailforward_detail($u_id) {
  global $cdg_sql;

  $u_id = sql_parse_id($u_id, true);
  $query = "SELECT userobm_id,
      userobm_domain_id,
      userobm_login,
      userobm_email,
      userobm_nomade_perms,
      userobm_nomade_enable,
      userobm_email_nomade,
      userobm_nomade_local_copy,
      domain_name
    FROM UserObm
    LEFT JOIN Domain ON domain_id = userobm_domain_id
    WHERE userobm_id $u_id ";

  display_debug_msg($query, $cdg_sql, "run_query_mailforward_detail()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// User (mail forwarding info) update query execution
// As this action is "hot plug", Production and Update databases are updated
// Parameters:
//   - $forward[] : entry values
//     keys used  : email_nomade, nomade_enable
//   - $u_id       : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_mailforward_update($forward, $u_id) {
  global $cdg_sql;

  $email_nomade = $forward["email_nomade"];
  $nomade_enable = sql_parse_int($forward["nomade_enable"]);
  $nomade_local_copy = sql_parse_int($forward["nomade_local_copy"]);  
  $u_id = sql_parse_id($u_id, true);
  // Update done in real-time, so updating UserObm AND P_UserObm
  $query = "UPDATE UserObm set
    userobm_email_nomade    = '$email_nomade',
    userobm_nomade_enable   = $nomade_enable,
    userobm_nomade_local_copy=$nomade_local_copy    
  WHERE userobm_id $u_id ";

  display_debug_msg($query, $cdg_sql, "run_query_mailforward_update()");
  $usr_q = new DB_OBM;
  $retour = $usr_q->query($query);

  $query = "UPDATE P_UserObm set
    userobm_email_nomade    = '$email_nomade',
    userobm_nomade_enable   = $nomade_enable,
    userobm_nomade_local_copy=$nomade_local_copy    
  WHERE userobm_id $u_id ";

  display_debug_msg($query, $cdg_sql, "run_query_mailforward_update()");
  $usr_q = new DB_OBM;
  $retour = $usr_q->query($query);



  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Tell if the nomade email is allowed
// Parameters:
//   - $u_id : User id
///////////////////////////////////////////////////////////////////////////////
function nomade_is_allowed($u_id) {
  global $cdg_sql;
  $u_id = sql_parse_id($u_id, true);
  $query = "SELECT userobm_nomade_perms
    FROM UserObm
    WHERE userobm_id $u_id ";

  display_debug_msg($query, $cdg_sql, "nomade_is_allowed()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("userobm_nomade_perms");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Execute the Change Alias command according paratemers given
// Parameters:
//   - $usr_q     : current user values (used are email, login, nomade_perms)
//   - $forward[] : values to set
///////////////////////////////////////////////////////////////////////////////
function exec_change_alias($usr_q, $forward) {
  global $cmd_mailforward, $cdg_exe;

  $login = $usr_q->f("userobm_login");
  $domain = $usr_q->f("userobm_domain_id");

  // Si le forward est autorisé et activé
  if ( ($usr_q->f("userobm_nomade_perms")) && ($forward["nomade_enable"]) ) {
    $dst = $forward["email_nomade"];
  } else {
    // pas autorise ou inactif, destination est le compte local
    $dst = $usr_q->f("userobm_login");
  }

  $cmd = "$cmd_mailforward --login $login --domain $domain";
  display_debug_msg($cmd, $cdg_exe);
  exec($cmd);

  return;
}


///////////////////////////////////////////////////////////////////////////////
// MailForward Data checking and formatting
// Parameters:
//   - $usr_q     : User mail forwarding database result
//   - $forward[] : values checked
///////////////////////////////////////////////////////////////////////////////
function check_mailforward_data_form($usr_q, $forward) {
  global $l_nomade_not_allowed, $l_err_email;
  global $php_regexp_email, $l_err_nomade_local_copy;
  global $err, $action;

  $nomade_perms = $usr_q->f("userobm_nomade_perms");
  $email_nomade = $forward["email_nomade"];
  $nomade_enable = $forward["nomade_enable"];
  $nomade_local_copy = $forward["nomade_local_copy"];
  
  // Should not happen here !
  if (! $nomade_perms) {
    $err["msg"] = $l_nomade_not_allowed;
    return false;
  }

  // Email nomade must be filled if nomade activated
  if ( ($nomade_enable) && (trim($email_nomade) == "")) {
    $err["msg"] = "$email_nomade : $l_err_email";
    $err["field"] = "email_nomade";
    return false;
  }

  // Email nomade : correct if filled
  if ( (trim($email_nomade) != "")
       && (preg_match($php_regexp_email, $email_nomade) == false) ) {
    $err["msg"] = "$email_nomade : $l_err_email";
    $err["field"] = "email_nomade";
    return false;
  }

  // User nomade local copy only if autorized and activated
  if ( ($nomade_local_copy) && ((! $nomade_perms) || (! $nomade_enable)) ) {
    $err["msg"] = $l_err_nomade_local_copy;
    $err["field"] = "nomade_local_copy";
    return false;
  }
  
  return true;
}


</script>
