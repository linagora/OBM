<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : host_query.inc                                               //
//     - Desc : host query File                                              //
// 2004-09-09 Aliacom                                                        //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Host Search query execution 
// Parameters :
//   - $host[]   : host search criteria
//     keys used : name, user
///////////////////////////////////////////////////////////////////////////////
function run_query_host_search($host) {
  global $cdg_sql, $ctu_sql_limit;
  
  $sql_order_dir = $host["sql_order_dir"];
  $sql_order_field = $host["sql_order_field"];
  $multidomain = sql_multidomain("host");

  $name = sql_search_text_parse($host["name"]);
  $ip = sql_search_text_parse($host["ip"]);
  $samba = $host["samba"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $timeupdate = sql_date_format($db_type,"host_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type,"host_timecreate", "timecreate");

  $where = "(1=1)";
  if ($name != '') {
    $where .= " AND host_name $like '$name%'";
  }
  if ($ip != '') {
    $where .= " AND host_ip $like '%$ip%'";
  }
  if ($samba != '') {
    $where .= " AND host_samba = 1";
  }
  $whereq = "WHERE $where $multidomain";

  // ORDER construction
  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "host_name";
  $orderq .= " ORDER BY $order $sql_order_dir";

  $query = "SELECT DISTINCT Host.*,
      host_id as id,
      $timecreate,
      $timeupdate,
      A.userobm_login as usercreate,
      B.userobm_login as userupdate
    FROM Host
      LEFT JOIN UserObm as A ON host_usercreate=A.userobm_id
      LEFT JOIN UserObm as B ON host_userupdate=B.userobm_id
    $whereq 
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM Host $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, "run_query_host_search()");
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Host detail query execution
// Parameters:
//   - $id : host id
///////////////////////////////////////////////////////////////////////////////
function run_query_host_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type,"host_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type,"host_timecreate", "timecreate");
  $multidomain = sql_multidomain("host");

  $query = "SELECT *,
      $timecreate,
      $timeupdate,
      uo.userobm_id as userobm_id,
      uo.userobm_login as userobm_login,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate,
      mail_name
    FROM Host
         LEFT JOIN UserObm as c ON host_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON host_userupdate=u.userobm_id
         LEFT JOIN UserObm as uo ON host_id=uo.userobm_host_id
         LEFT JOIN Mail ON host_id=mail_value AND mail_name LIKE '%mailserver'
    WHERE host_id = '$id'
      $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_host_detail()");
  $obm_q->query($query) ;
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : host insertion
// Parameters:
//   - $host[] : host hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_host_insert($host) {
  global $obm, $cdg_sql, $cg_gid_smb_host;

  $domain_id = $obm["domain_id"];
  $name = $host["name"];
  $ip = $host["ip"];
  $samba = ($host["samba"] == 1) ? 1 : 0;
  $web_perms = $host["web_perms"];
  $ftp_perms = $host["ftp_perms"];
  $firewall_perms = $host["firewall_perms"];
  $web_all = $user["web_all"];  
  $list = $user["web_list"];
  $desc = $host["desc"];
  $now = date("Y-m-d H:i:s");
  $host_uid = get_first_user_free_uid();
  $host_gid = $cg_gid_smb_host;

  $query = "INSERT INTO Host (host_timeupdate,
    host_timecreate,
    host_userupdate,
    host_usercreate,
    host_uid,
    host_gid,
    host_samba,
    host_name,
    host_ip,
    host_description,
    host_web_perms,
    host_web_list,
    host_web_all,    
    host_ftp_perms,
    host_firewall_perms)
  VALUES (null,
    '$now',
    null,
    '$obm[uid]',
    '$host_uid',
    '$host_gid',
    '$samba',
    '$name',
    '$ip',
    '$desc',
    '$web_perms',
    '$web_list',
    '$web_all',    
    '$ftp_perms',
    '$firewall_perms')";

  display_debug_msg($query, $cdg_sql, "run_query_host_insert(1)");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Mise a jour du lien user s'il a ete précisé
  $user_id = $host["user_id"];
  // Cas ou il n'y a pas de lien
  if ($user_id == '') 
    $user_id = 0;
    
  if ($retour && $user_id != 0) {

    // On recupere l'ID de l'hôte qu'on vient d'ajouter
    $query = "SELECT host_id
    FROM Host
    WHERE
      host_timecreate='$now' and
      host_userupdate is null and
      host_usercreate='$obm[uid]' and
      host_uid='$host_uid' and
      host_gid='$host_gid' and
      host_samba='$samba' and
      host_name='$name' and
      host_ip='$ip' and
      host_description='$desc' and
      host_web_perms='$web_perms' and
      host_ftp_perms='$ftp_perms' and
      host_firewall_perms='$firewall_perms'";

    display_debug_msg($query, $cdg_sql, "run_query_host_insert(2)");
    $retour = $obm_q->query($query);

    // Mise a jour du lien
    if ($retour) {
      $obm_q->next_record();
      $host_id = $obm_q->f("host_id");

      $query = "UPDATE UserObm 
      SET userobm_timeupdate='$now',
          userobm_userupdate='$obm[uid]',
          userobm_host_id='$host_id'
      WHERE
          userobm_id='$user_id'";

      display_debug_msg($query, $cdg_sql, "run_query_host_insert(3)");
      $retour = $obm_q->query($query);
    }
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Host Update query execution
// Parameters:
//   - $host[] : host hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_host_update($host) {
  global $obm, $cdg_sql;

  $id = $host["host_id"];
  $name = $host["name"];
  $ip = $host["ip"];
  $samba = ($host["samba"] == 1) ? 1 : 0;
  $web_perms = $host["web_perms"];
  $ftp_perms = $host["ftp_perms"];
  $web_all = $host["web_all"];  
  $web_list = $host["web_list"];
  $firewall_perms = $host["firewall_perms"];
  $desc = $host["desc"];
  $now = date("Y-m-d H:i:s");
  
  $query = "UPDATE Host SET
    host_timeupdate='$now',
    host_userupdate='$obm[uid]',
    host_samba='$samba',
    host_name='$name',
    host_ip='$ip',
    host_description='$desc',
    host_web_perms='$web_perms',
    host_web_all='$web_all',
    host_web_list='$web_list',    
    host_ftp_perms='$ftp_perms',
    host_firewall_perms='$firewall_perms'
  WHERE host_id='$id'";

  display_debug_msg($query, $cdg_sql, "run_query_host_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Mise a jour du lien user s'il a ete précisé
  $user_id = $host["user_id"];
  if ($retour) {

    // Suppression de l'ancien lien user=>host
    $query = "UPDATE UserObm 
    SET userobm_timeupdate='$now',
        userobm_userupdate='$obm[uid]',
        userobm_host_id='0'
    WHERE
        userobm_host_id='$id'";
    $retour = $obm_q->query($query);

    // Cas ou il n'y a plus de lien
    if ($user_id == '') 
      $user_id = 0;
    if ($retour && $user_id != 0) {
      // Creation du nouveau lien user=>host
      $query = "UPDATE UserObm 
      SET userobm_timeupdate='$now',
          userobm_userupdate='$obm[uid]',
          userobm_host_id='$id'
      WHERE
          userobm_id='$user_id'";

      display_debug_msg($query, $cdg_sql, "run_query_host_update()");
      $retour = $obm_q->query($query);
    }
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : host id
///////////////////////////////////////////////////////////////////////////////
function run_query_host_delete($p_id) {
  global $obm, $cdg_sql;

  // Delete the Host
  $query = "DELETE FROM Host WHERE host_id='$p_id'";
  display_debug_msg($query, $cdg_sql, "run_query_host_delete()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Mise a jour du lien vers user
  if ($retour) {
    $obm_q->next_record();
    $host_id = $obm_q->f("host_id");
    $user_id = $host["user_id"];

    $query = "UPDATE UserObm
    SET userobm_timeupdate='$now',
        userobm_userupdate='$obm[uid]',
        userobm_host_id='0'
    WHERE
        userobm_host_id='$p_id'";

    display_debug_msg($query, $cdg_sql, "run_query_host_delete()");
    $retour = $obm_q->query($query);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Return the host infos
// Parameters:
//   - $id : host id
///////////////////////////////////////////////////////////////////////////////
function get_host_info($id) {
  global $cdg_sql;

  if ($id == "") {
    return false;
  }

  $query = "SELECT *
    FROM Host
      LEFT JOIN Mail ON mail_value=$id AND mail_name LIKE '%mailserver'
    WHERE host_id='$id'";

  display_debug_msg($query, $cdg_sql, "get_host_info()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $h["name"] = $obm_q->f("host_name");
  $h["ip"] = $obm_q->f("host_ip");
  $h["samba"] = $obm_q->f("host_samba");
  $h["desc"] = $obm_q->f("host_description");
  $h["mailserver"] = $obm_q->f("mail_name");

  return $h;
}


///////////////////////////////////////////////////////////////////////////////
// Check if a host (except with id given) with the name given already exists
// Parameters:
//   - $name : name to search for
//   - $id   : host id to exclude
// Returns:
//   - true (if a host exists) or false
///////////////////////////////////////////////////////////////////////////////
function get_host_name_exists($name, $id="") {
  global $cdg_sql;

  if ($id != "") {
    $where_id = "host_id!='$id' and";
  }

  $query = "SELECT host_id, host_name
    FROM Host
    WHERE $where_id host_name='$name'";

  display_debug_msg($query, $cdg_sql, "get_host_name_exists()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Return the hosts which matches the name or the desc
// except the one given (update mode)
// Parameters:
//   - $id   : host id
//   - $name : host name
//   - $desc : host desc
///////////////////////////////////////////////////////////////////////////////
function run_query_check_host($id, $name, $desc) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  if ($id != "") {
    $where_id = "host_id!='$id' and";
  }

  // If name is short, we test equality, else similarity
  if (strlen($name) > 2) {
    $wname = "host_name like '%$name%'";
  } else {
    $wname = "host_name = '$name'";
  }

  // If desc is short, we test equality, else similarity
  if (trim($desc) != "") {
    if (strlen($desc) > 2) {
      $wdesc = "host_description like '%$desc%'";
    } else {
      $wdesc = "host_description = '$desc'";
    }
  }
  if ($wdesc != "") $wdesc = "or $wdesc";

  $query = "select distinct host_id, host_name, host_description
     from Host
     where $where_id
       ($wname $wdesc)";

  display_debug_msg($query, $cdg_sql, "run_query_check_host()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Host context checking (same hosts exists ?)
// Parameters:
//   - $id     : host id
//   - $host[] : host values
//     keys used : name, desc, email
// Returns:
//   - Host Database object with host of similar hosts
///////////////////////////////////////////////////////////////////////////////
function check_host_context($id, $host) {
  global $cdg_sql;

  $name = $host["name"];
  $desc = $host["desc"];

  // return the hosts with same name or desc
  $g_q = run_query_check_host($id, $name, $desc);

  return $g_q;
}


///////////////////////////////////////////////////////////////////////////////
// Host Form Data checking and formatting
// Parameters:
//   - $host[]  : values checked
//     keys used : name, desc, email
///////////////////////////////////////////////////////////////////////////////
function check_host_data_form($host) {
  global $php_regexp_hostname, $l_err_host_name_empty, $l_err_host_name_exist;
  global $cgp_use, $err, $l_err_host_name;
  global $php_regexp_ip, $l_err_host_ip, $l_err_host_ip_exist;
  global $l_err_host_ip_not_in_lan, $l_err_firewall_perms, $l_err_web_all;

  $id = $host["host_id"];
  $name = $host["name"];
  $ip = $host["ip"];
  $web_list = $host["web_list"];
  $web_all = $host["web_all"];     

  // MANDATORY: Host name not empty
  if (trim($name) == "") {
    $err["msg"] = $l_err_host_name_empty;
    $err["field"] = "name";
    return false;
  }

  // MANDATORY: Host name unique
  if (get_host_name_exists($name, $id)) {
    $err["msg"] = "$l_err_host_name_exist ($name)";
    $err["field"] = "name";
    return false;
  }

  // Host name correct
  if (($name != "") && (! preg_match($php_regexp_hostname, $name))) {
    $err["msg"] = "$name : $l_err_host_name";
    $err["field"] = "name";
    return false;
  }

  // If IP not empty, must not already exists
  if ($ip != "") {

    // Host IP correct
    if (! preg_match($php_regexp_ip, $ip)) {
      $err["msg"] = " $ip : $l_err_host_ip";
      $err["field"] = "ip";
      return false;
    }

    $ips = get_ip_used($id);
    // IP address not already used
    if (array_key_exists($ip, $ips)) {
      $err["msg"] = "$l_err_host_ip_exist : $ip (" . $ips["$ip"] . ")";
      $err["field"] = "ip";
      return false;
    }
    // IP is in a network :
    if ($cgp_use["service"]["network"]) {
      if (!is_ip_in_an_internal_network($ip)) {
        $err["msg"] = "$l_err_host_ip_not_in_lan : $ip ";
        $err["field"] = "ip";
        return false;
      }
    }
  }
 
  if ($cgp_use["service"]["web_local"]) {
    // web_all must be 0 or 1
    if (($web_all != "0") && ($web_all != "1")) {
      $err["msg"] = $l_err_web_all;
      return false;
    }
  }
 
  if ($cgp_use["service"]["firewall"]) {
    $firewall_perms = $host["firewall_perms"];
    
    if (!is_valid_firewall_perms($firewall_perms)) {
      $err["msg"] = " $l_err_firewall_perms";
      $err["field"] = "firewall_perms";
      return false;
    }
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if string from textarea is a good candidate for firewall perms.
// Valid if : 
//  + port list port[-port]
//  + IP
//  + IP:port[-port]
// Parameters:
//   - $str String to be cleaned up
// Returns:
//   - The string cleaned up  
///////////////////////////////////////////////////////////////////////////////
function is_valid_firewall_perms($str) {
  $result = true;
  if ($str != '') {
    $lines = explode("\r\n",$str);
    $i = 0;
    while ($i < count($lines) && $result) {
      $currentline = $lines[$i];

      if (!is_valid_port_list($currentline) && 
          !is_valid_network($currentline) &&
          !is_valid_network_port_list($currentline)) {
          $result = false;
      }
      $i++;
    }
  }
  return $result;
}

function is_valid_network($str) {
  global $php_regexp_network;
  $result = preg_match($php_regexp_network, $str);
  return $result;
}

function is_valid_port_list($str) {
  global $php_regexp_port_list;
  $result = preg_match($php_regexp_port_list, $str);
  return $result;
}

function is_valid_network_port_list($str) {
  global $php_regexp_network_port_list;
  $result = preg_match($php_regexp_network_port_list, $str);
  return $result;
}

</script>
