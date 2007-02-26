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
    $where_domain = "AND userobm_domain_id='$domain_id'";
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
    WHERE userobm_archive='1'
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
    $where_domain = "AND group_domain_id='$domain_id'";
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
    WHERE group_privacy = '1'
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
    $where_domain = "WHERE mailsharedir_domain_id='$domain_id'";
  }

  $query = "SELECT count(*) FROM P_MailShareDir $where_domain";

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

//
// get virus statistics
//
function get_virus_stats() {
  global $cdg_sql;

  $query = "SELECT stats_name, stats_value
  FROM Stats
  WHERE stats_name like 'virus_%'";

  display_debug_msg($query, $cdg_sql, "get_virus_stats()");
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
  global $php_regexp_password;

  $passwd = $control["ht_passwd"];
  if (! preg_match($php_regexp_password, $passwd)) {
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
function get_virus_db_version() {
  global $c_db_version_file,$php_regexp_main_virus_db;
  global $php_regexp_daily_virus_db,$php_regexp_virus_db_version;
  global $php_regexp_virus_db_update;

  if ($handle = fopen($c_db_version_file, "rb")) {
    $contents = '';
    $i = 0;

    # Initialisation des valeurs a 0
    $return["main"] = "--";
    $return["daily"] = "--";
    $return["lastupdate"] = "--";

    while (!feof($handle)) {
      $line = fgets($handle, 8192);

      if(preg_match($php_regexp_main_virus_db, $line)) {
        preg_match($php_regexp_virus_db_version, $line,$version);
        $return["main"] = $version[1];
      } elseif(preg_match($php_regexp_daily_virus_db, $line)) {
        preg_match($php_regexp_virus_db_version, $line,$version);
        $return["daily"] = $version[1];
      } elseif (preg_match($php_regexp_virus_db_update, $line)) {
        $return["lastupdate"] = $line;
      }
    }
    fclose($handle);
  
  } else {
    return array(); 
  }
  
  return $return;
}


///////////////////////////////////////////////////////////////////////////////
function run_update_htpasswd($controle) {
  global $c_file_password,$l_file_read_only,$c_htstat_user;
  global $l_err_file_writing,$l_file_not_found;
  
  if (is_writable($c_file_password)) {
    if ($handle = @fopen($c_file_password, 'w')) {  
      if (!@fwrite($handle, "$c_htstat_user:".crypt($controle["ht_passwd"]))) {
	$classe = $classe_erreur;
	$message = $l_err_file_writing;
	break;
      }    
      fclose($handle);
      return true;
    }
    $message = $l_file_not_found;
  } else {
    $message = $l_file_read_only;
  }
  return $message;
}

</script>
