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
function run_query_forward($u_id) {
  global $cdg_sql;

  $query = "select userobm_id,
      userobm_login,
      userobm_email,
      userobm_nomade_perms,
      userobm_nomade_enable,
      userobm_email_nomade,
      userobm_nomade_local_copy
    from UserObm
    where userobm_id='$u_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// User (mail forwarding info) update query execution                        //
// As this action is "hot plug", Production and Update databases are updated
// Parameters:
//   - $forward[] : entry values
//     keys used  : email_nomade, nomade_enable
//   - $uid       : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_update($forward, $uid) {
  global $auth, $cdg_sql;

  $email_nomade = $forward["email_nomade"];
  $nomade_enable = $forward["nomade_enable"];
  $nomade_local_copy = $forward["nomade_local_copy"];  

  $query = "update UserObm set
    userobm_email_nomade    = '$email_nomade',
    userobm_nomade_enable   = '$nomade_enable',
    userobm_nomade_local_copy='$nomade_local_copy'    
  where userobm_id = '$uid'";

  display_debug_msg("DB_OBM_Prod + DB_OBM : $query", $cdg_sql);
  // Update database update
  $usr_q = new DB_OBM;
  $retour = $usr_q->query($query);

  // Production database update
  $usr_q = new DB_OBM_Prod;
  $retour = $usr_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Tell if the nomade email is allowed                                       //
// Parameters:
//   - $u_id : User id
///////////////////////////////////////////////////////////////////////////////
function nomade_is_allowed($u_id) {
  global $cdg_sql;

  $query = "select userobm_nomade_perms
    from UserObm
    where userobm_id = '$u_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("userobm_nomade_perms");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Execute the Change Alias command according paratemers given               //
// Parameters:
//   - $usr_q     : current user values (used are email, login, nomade_perms)
//   - $forward[] : values to set
//     keys used  : email_nomade, nomade_enable
///////////////////////////////////////////////////////////////////////////////
function exec_change_alias($usr_q, $forward) {
  global $cmd_mailforward, $cdg_exe;

  $alias = $usr_q->f("userobm_login");

  // Si le forward est autorisé et activé
  if ( ($usr_q->f("userobm_nomade_perms")) && ($forward["nomade_enable"]) ) {
    $dst = $forward["email_nomade"];
  } else {
    // pas autorise ou inactif, destination est le compte local
    $dst = $usr_q->f("userobm_login");
  }    

  if( $forward["nomade_local_copy"] ) {
    $cmd_ext = " --local $alias ";
  }
  $cmd = "$cmd_mailforward --alias $alias --dst $dst $cmd_ext";
  display_debug_msg($cmd, $cdg_exe);
  exec($cmd);

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Data checking and formatting                                              //
// Parameters:
//   - $usr_q     : User mail forwarding database result
//   - $forward[] : values checked
//     keys used  : all
///////////////////////////////////////////////////////////////////////////////
function check_data_form($usr_q, $forward) {
  global $l_nomade_not_allowed, $l_email_error;
  global $php_regexp_email,$l_nomade_local_copy_error;
  global $err_msg, $err_field, $action, $cdg_sql;

  $nomade_perms = $usr_q->f("userobm_nomade_perms");
  $email_nomade = $forward["email_nomade"];
  $nomade_enable = $forward["nomade_enable"];
  $nomade_local_copy = $forward["nomade_local_copy"];
  
  // Should not happen here !
  if (! $nomade_perms) {
    $err_msg = $l_nomade_not_allowed;
    return false;
  }

  // Email nomade : correct
  if ( (trim($email_nomade) == "")
       || (preg_match($php_regexp_email, $email_nomade) == false) ) {
    $err_msg = "$email_nomade : $l_email_error";
    $err_field = "email_nomade";
    return false;
  }
  // User nomade local copy only if autorized and activated
  if ( ($nomade_local_copy) && ((! $nomade_perms) || (! $nomade_enable)) )
  {
    $err_msg = $l_nomade_local_copy_error;
    $err_field = "nlocalcopy";
    return false;
  }
  
  return true;
}


</script>
