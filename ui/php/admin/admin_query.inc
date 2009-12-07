<?php
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
function run_query_admin_count_passive_sess($delay) {
  global $cdg_sql;

  $multidomain = sql_multidomain('UserObm');
  $time_pass = date("Y-m-d H:i:s",time() - $delay);
  $query = "select count(*) as num
    from ActiveUserObm
    INNER JOIN UserObm ON activeuserobm_userobm_id = userobm_id
    where ActiveUserObm.activeuserobm_timeupdate <= '$time_pass' $multidomain";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q->f("num");
}


///////////////////////////////////////////////////////////////////////////////
// Get the number of User session (rows in ActiveUserObm)                    //
// Returns:
//   - number of user session
///////////////////////////////////////////////////////////////////////////////
function get_admin_usersession_number() {
  global $cdg_sql;

  $multidomain = sql_multidomain('UserObm');
  $query = "select count(*) as num from ActiveUserObm INNER JOIN UserObm ON activeuserobm_userobm_id = userobm_id WHERE 1=1 $multidomain";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f("num");
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - List of Active User session (not older than delay)      //
// Parameters:
//   - $delay : session older than this delay (in sec) are considered passive
// Returns:
//   - DBO : list of active user session
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_active_user($delay) {
  global $cdg_sql;

  // if lifetime = 0 (or bad:< 0) we consider all session actives.
  if ($delay > 0) {
    $date_limit = date("Y-m-d H:i:s",time() - $delay);
    $where_date = "and ActiveUserObm.activeuserobm_timeupdate >= '$date_limit'";
  }

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $multidomain = sql_multidomain('userobm');

  $timeupdate = sql_date_format($db_type, "activeuserobm_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "activeuserobm_timecreate", "timecreate");

  $query = "select UserObm.userobm_login,
      UserObm.userobm_timelastaccess,
      ActiveUserObm.*,
      $timecreate,
      $timeupdate
    from UserObm, ActiveUserObm
    where ActiveUserObm.activeuserobm_userobm_id = UserObm.userobm_id
      $where_date $multidomain";
  display_debug_msg($query,$cdg_sql);
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
function run_query_admin_passive_user($delay) {
  global $cdg_sql;

  $multidomain = sql_multidomain('UserObm');
  $date_limit = date("Y-m-d H:i:s",time() - $delay);
  $query = "select UserObm.userobm_login,
                   ActiveUserObm.*
            from UserObm, ActiveUserObm
            where ActiveUserObm.activeuserobm_userobm_id = UserObm.userobm_id
              and ActiveUserObm.activeuserobm_timeupdate < '$date_limit' $multidomain";
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
function run_query_admin_activeuserobm_delete($sid) {
  global $cdg_sql;

  $query = "delete from ActiveUserObm where activeuserobm_sid = '$sid'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

}

?>
