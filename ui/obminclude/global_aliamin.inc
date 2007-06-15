<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : global_mailldap.inc                                          //
//     - Desc : Temp file for aliamin migration                              //
// 2007-01-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Return an array of all used emails (users, groups, mailshare)
// Parameters:
//   - $id  : User id to exclude from the result
//   - $gid : Group id to exclude from the result
//   - $mid : Mailshare id to exclude from the result
// Returns:
//   - $mails : array of used emails
///////////////////////////////////////////////////////////////////////////////
function get_email_used($id="", $gid="", $mid="") {
  global $cdg_sql, $php_regexp_email_name, $l_user, $l_group, $l_mailshare;

  $mails = array();

  if ($id != "") {
    $where_user = "WHERE userobm_id != '$id'";
  }
  // Users
  $query = "SELECT userobm_lastname, userobm_firstname, userobm_email
    FROM UserObm
    $where_user";

  display_debug_msg($query, $cdg_sql, "get_email_used(1)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $email = $obm_q->f("userobm_email");
    $name = "$l_user : " . $obm_q->f("userobm_lastname") ." ". $obm_q->f("userobm_firstname");
    $em = strtok($email, "\r\n");
    while ($em) {
      $mails["$em"] = "$name";
      $em = strtok("\r\n");
    }
  }

  // Groups
  if ($gid != "") {
    $where_group = "WHERE group_id != '$gid'";
  }
  $query = "SELECT group_name, group_email
    FROM UGroup
    $where_group";

  display_debug_msg($query, $cdg_sql, "get_email_used(2)");
  $g_q = new DB_OBM;
  $g_q->query($query);

  while ($g_q->next_record()) {
    $email = $g_q->f("group_email");
    $name = "$l_group : " . $g_q->f("group_name");
    $mails["$email"] = "$name";
  }

  // Mailshares
  if ($mid != "") {
    $where_mailshare = "WHERE mailshare_id != '$mid'";
  }
  $query = "SELECT mailshare_name, mailshare_email
    FROM MailShare
    $where_mailshare";

  display_debug_msg($query, $cdg_sql, "get_email_used(3)");
  $m_q = new DB_OBM;
  $m_q->query($query);

  while ($m_q->next_record()) {
    $email = $m_q->f("mailshare_email");
    $name = "$l_mailshare : " . $m_q->f("mailshare_name");
    $mails["$email"] = "$name";
  }

  return $mails;
}


///////////////////////////////////////////////////////////////////////////////
// Get the host name from the host Id
// Parameters:
//   - $id : host id 
///////////////////////////////////////////////////////////////////////////////
function get_last_host_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "SELECT host_name FROM Host WHERE host_id='$id'";
    display_debug_msg($query, $cdg_sql, "get_last_host_text()");
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("host_name");
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the host name from the host Id (better function name)
// Parameters:
//   - $id : host id
///////////////////////////////////////////////////////////////////////////////
function get_hostname_by_id($id) {

  return get_last_host_text($id);
}


///////////////////////////////////////////////////////////////////////////////
// Get Samba informations
///////////////////////////////////////////////////////////////////////////////
function get_samba_info() {
  global $cdg_sql;

  $query = "select * from Samba";

  display_debug_msg($query, $cdg_sql, "get_samba_info()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $cpt = 0;
  while ($obm_q->next_record()) {
    $name = $obm_q->f("samba_name");
    $value = $obm_q->f("samba_value");
    $smb["$name"] = $value;
  }

  return $smb;
}


///////////////////////////////////////////////////////////////////////////////
// Return the first uid not in use (starting at $c_first_uid)
// Returns:
//   - $uid : first free uid
///////////////////////////////////////////////////////////////////////////////
function get_first_user_free_uid($first_id=false) {
  global $cdg_sql, $c_first_uid;
  $uids = array();

  if( $first_id===false ) {
    $first_id = $c_first_uid;
  }
  
  $query = "
    SELECT host_uid as uid
    FROM Host
    WHERE host_uid >= '$c_first_uid'
    ORDER BY uid";
  display_debug_msg($query, $cdg_sql, "get_first_user_free_uid()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $uids[] = $obm_q->f("uid");
  }

  $query = "
    SELECT userobm_uid as uid
    FROM UserObm
    WHERE userobm_uid >= '$c_first_uid'
    ORDER BY uid";
  display_debug_msg($query, $cdg_sql, "get_first_user_free_uid()");
  $obm_q->query($query);
  
  while ($obm_q->next_record()) {
    $uids[] = $obm_q->f("uid");
  }

  // Avec Samba, les UID et GID doivent être unique car le SID en est déduit
  // directement en concaténant l'UID ou le GID au SID du domaine.
  // Or il faut éviter les doublons de SID, y compris entre
  // utilisateurs/hôtes/groupes.
  // Le mappage direct est intéressant dans le cadre de migration NT->Samba
  $query = "
    SELECT group_gid as uid
    FROM UGroup
    WHERE group_gid >= '$first_id'
    ORDER BY group_gid";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while( $obm_q->next_record() ) {
    $uids[] = $obm_q->f("uid");
  }
  
  // On ne tient pas compte des doublons - même si ce cas ne peut pas se
  // produire via l'interface, il peut se produire en cas de peuplement
  // direct de celle-ci par un script. Ceci est un PROBLEME.
  // On supprime donc les doublons afin d'eviter que le probleme n'empire via
  // l'interface...
  $uids = array_unique($uids);
  sort($uids);

  $cpt = $c_first_uid;

  // On se place sur le premier element
  reset($uids);
  while ( ($uid = current($uids)) && ($uid == $cpt) ) {
    $cpt++;
    next($uids);
  }

  return $cpt;
}


///////////////////////////////////////////////////////////////////////////////
// Return group id from its gid
// Parameters:
//   - $gid : group gid
// Returns:
//   - group id
///////////////////////////////////////////////////////////////////////////////
function get_group_id_from_gid($gid) {
  global $cdg_sql;

  $query = "SELECT group_id
    FROM UGroup
    WHERE group_gid='$gid'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $id = $obm_q->f("group_id");

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Return the first gid not in use (starting at $c_first_gid_user)
// Returns:
//   - $gid : first free gid
///////////////////////////////////////////////////////////////////////////////
function get_first_group_free_gid() {
  global $c_first_gid_user;

  return get_first_user_free_uid($c_first_gid_user);
}


///////////////////////////////////////////////////////////////////////////////
// Return an array of all used IPs (hosts only for now)
// Parameters:
//   - $id  : Host id to exclude from the result
// Returns:
//   - $mails : array of used emails
///////////////////////////////////////////////////////////////////////////////
function get_ip_used($id="") {
  global $cdg_sql, $php_regexp_email_name, $l_host;

  $ips = array();
  $query = "SELECT host_name, host_ip
    FROM Host
    WHERE host_id != '$id'";

  display_debug_msg($query, $cdg_sql, "get_ip_used()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $ip = $obm_q->f("host_ip");
    $hname = $obm_q->f("host_name");
    $name = "$l_host : $hname";
    $ips["$ip"] = "$name";
  }

  return $ips;
}


/**
 * Get the Update state (1 update to be done, 0 no changes)
 * return $state : 1 : update to be done, 0 no changes
 */
function get_update_state() {
  global $obm, $cdg_sql;

  $query = "SELECT domainpropertyvalue_value as value
    FROM DomainPropertyValue
    WHERE domainpropertyvalue_domain_id='$obm[domain_id]'
     AND domainpropertyvalue_property_key = 'update_state'";

  display_debug_msg($query, $cdg_sql, "get_update_state()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $state = $obm_q->f("value");

  if ($state == "1") return "1";
  else return "0";
}


/**
 * Update : set the update flag to true to allow display update info
 *
 * @param $state : 1 set to true (there are update, else 0)
 */
function set_update_state($state=1) {
  global $obm, $cdg_sql;

  $query = "UPDATE DomainPropertyValue
    SET domainpropertyvalue_value = '$state'
    WHERE domainpropertyvalue_domain_id='$obm[domain_id]'
      AND domainpropertyvalue_property_key = 'update_state'";

  display_debug_msg($query, $cdg_sql, "set_update_state($state)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


/**
 * Get the Update lock
 * return $lock : 1 locked, 0 no lock
 */
function get_update_lock() {
  global $obm, $cdg_sql;

  $query = "SELECT obminfo_value as value
    FROM ObmInfo
    WHERE obminfo_name='update_lock'";

  display_debug_msg($query, $cdg_sql, 'get_update_lock()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $lock = $obm_q->f('value');

  if ($lock == '1') return '1';
  else return '0';
}


/**
 * Update : set the update lock
 */
function set_update_lock() {
  global $cdg_sql;

  $query = "UPDATE ObmInfo
    SET obminfo_value = '1'
    WHERE obminfo_name = 'update_lock'";

  display_debug_msg($query, $cdg_sql, 'set_update_lock()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


/**
 * Update : remove the update lock
 */
function remove_update_lock() {
  global $cdg_sql;

  $query = "UPDATE ObmInfo
    SET obminfo_value = '0'
    WHERE obminfo_name = 'update_lock'";

  display_debug_msg($query, $cdg_sql, 'remove_update_lock()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


?>
