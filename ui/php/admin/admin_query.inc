<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_query.inc                                              //
//     - Desc : Admin Query File                                             //
// 2001-12-27 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Query execution : Return the number of entries in ActiveSessions Table    //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Get the passive session number                                            //
// Parameters:
//   - $delay : session older than this delay (in sec) are considered passive
// Returns:
//   - number of passive session
///////////////////////////////////////////////////////////////////////////////
function run_query_count_passive_sess($delay) {
  global $cdg_sql;

  $time_pass = date("YmdHis",time() - $delay);
  $query = "select count(*) as Num from ActiveUserObm Where ActiveUserObm.activeuserobm_timeupdate <= '$time_pass'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q->f("Num");
}


///////////////////////////////////////////////////////////////////////////////
// Get the number of User session (rows in ActiveUserObm)                    //
// Returns:
//   - number of user session
///////////////////////////////////////////////////////////////////////////////
function get_usersession_number() {
  global $cdg_sql;

  $query = "select count(*) from ActiveUserObm";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f("count(*)");
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - List of Active User session (not older than delay)      //
// Parameters:
//   - $delay : session older than this delay (in sec) are considered passive
// Returns:
//   - DBO : list of active user session
///////////////////////////////////////////////////////////////////////////////
function run_query_active_user($delay) {
  global $cdg_sql;

  // if lifetime = 0 (or bad:< 0) we consider all session actives.
  if ($delay > 0)
    $date_limit = date("YmdHis",time() - $delay);
  else
    $date_limit = 0;

  $query = "select UserObm.userobm_login,
       UserObm.userobm_timelastaccess,
       ActiveUserObm.*,
       UNIX_TIMESTAMP(activeuserobm_timecreate) as timecreate,
       UNIX_TIMESTAMP(activeuserobm_timeupdate) as timeupdate
    from UserObm, ActiveUserObm
    where ActiveUserObm.activeuserobm_userobm_id = UserObm.userobm_id
      and ActiveUserObm.activeuserobm_timeupdate >= '$date_limit'";
  display_debug_msg($query,$cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - List of Passive User session (older than delay)         //
// Parameters:
//   - $delay : session older than this delay (in sec) are considered passive
// Returns:
//   - DBO : list of passive user session
///////////////////////////////////////////////////////////////////////////////
function run_query_passive_user($delay) {
  global $cdg_sql;

  $date_limit = date("YmdHis",time() - $delay);
  $query = "select UserObm.userobm_login,
                   ActiveUserObm.*
            from UserObm, ActiveUserObm
            where ActiveUserObm.activeuserobm_userobm_id = UserObm.userobm_id
              and ActiveUserObm.activeuserobm_timeupdate < '$date_limit'";
  display_debug_msg($query,$cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Delete an ActiveUserObm entry                           //
// Parameters:
//   - $sid : session id
// Returns:
//   - DBO : list of passive user session
///////////////////////////////////////////////////////////////////////////////
function run_query_activeuserobm_delete($sid) {
  global $cdg_sql;

  $query = "delete from ActiveUserObm where activeuserobm_sid = '$sid'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

}


///////////////////////////////////////////////////////////////////////////////
// Get the lifetime global pref value                                        //
// Returns:
//   - $lifetime value
///////////////////////////////////////////////////////////////////////////////
function get_global_pref_lifetime() {
  global $cdg_sql;

  $query = "select globalpref_value
            from GlobalPref
            where globalpref_option = 'lifetime'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("globalpref_value");
  return $retour;
}
