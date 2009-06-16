<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : control_query.inc                                            //
//     - Desc : Control panel query & db File                                //
// 2002-09-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Get number of user defined (Production database)
// Parameters:
//   - $domain_id : domain
///////////////////////////////////////////////////////////////////////////////
function get_control_user_infos($domain_id="") {
  global $cdg_sql;

  if ($domain_id > 0) {
    $domain_id = sql_parse_id($domain_id, true);
    $where_domain = "AND userobm_domain_id $domain_id"; 
  }

  // get # users
  $query = "SELECT count(*) FROM P_UserObm WHERE 1=1 $where_domain";

  display_debug_msg($query, $cdg_sql, "get_control_user_infos(1)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $r['nb'] = $obm_q->f("count(*)");

  // get # archive users
  $query = "SELECT count(*) FROM P_UserObm
    WHERE userobm_archive=1
      $where_domain";

  display_debug_msg($query, $cdg_sql, "get_control_user_infos(2)");
  $obm_q->query($query);
  $obm_q->next_record();
  $r['nb_archive'] = $obm_q->f("count(*)");

  return $r;
}


///////////////////////////////////////////////////////////////////////////////
// Get number of group defined (Production database)
// Parameters:
//   - $domain_id : domain
///////////////////////////////////////////////////////////////////////////////
function get_control_group_infos($domain_id="") {
  global $cdg_sql;

  if ($domain_id > 0) {
    $domain_id = sql_parse_id($domain_id, true);
    $where_domain = "AND group_domain_id $domain_id"; 
  }

  // get # groups

  $query = "SELECT count(*) FROM P_UGroup WHERE 1=1 $where_domain";
  display_debug_msg($query, $cdg_sql, "get_control_group_infos(1)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $r["nb"] = $obm_q->f("count(*)");

  // get # private groups

  $query = "SELECT count(*) FROM P_UGroup
    WHERE group_privacy = 1
      $where_domain";
  display_debug_msg($query, $cdg_sql, "get_control_group_infos(2)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $r["nb_private"] = $obm_q->f("count(*)");

  return $r;
}


///////////////////////////////////////////////////////////////////////////////
// Get number of mailshare defined (Production database)
// Parameters:
//   - $domain_id : domain
///////////////////////////////////////////////////////////////////////////////
function get_control_mailshare_infos($domain_id="") {
  global $cdg_sql;

  if ($domain_id > 0) {
    $domain_id = sql_parse_id($domain_id, true);
    $where_domain = "WHERE mailshare_domain_id $domain_id"; 
  }

  $query = "SELECT count(*) FROM P_MailShare $where_domain";

  display_debug_msg($query, $cdg_sql, "get_control_mailshare_infos()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f("count(*)");

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Get disk free space
///////////////////////////////////////////////////////////////////////////////
function get_sys_disk_use() {
  global $var_device;

  $ret = exec("df -m | grep '$var_device' | cut -b52-55");

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Get mail statistics
// Returns:
//   - $m[] with values : mail_*
///////////////////////////////////////////////////////////////////////////////
function get_mail_stats() {
  global $cdg_sql;

  $query = "SELECT stats_name, stats_value
  FROM Stats
  WHERE stats_name like 'mail_%'";

  display_debug_msg($query, $cdg_sql, "get_mail_stats()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  while ($obm_q->next_record()) {
    $name = $obm_q->f("stats_name");
    $value = $obm_q->f("stats_value");
    $m["$name"] = $value;
  }

  return $m;
}


///////////////////////////////////////////////////////////////////////////////
function check_data($control) {

  return true;
}


</script>
