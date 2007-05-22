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


///////////////////////////////////////////////////////////////////////////////
// Get the Update state (1 update to be done, 0 no changes)
// Returns:
//   - $state : 1 : update to be done, 0 no changes
///////////////////////////////////////////////////////////////////////////////
function get_update_state() {
  global $cdg_sql;

  $query = "SELECT obminfo_name, obminfo_value
    FROM ObmInfo
    WHERE obminfo_name = 'update_state'";

  display_debug_msg($query, $cdg_sql, "get_update_state()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $state = $obm_q->f("obminfo_value");

  if ($state == "1") return "1";
  else return "0";
}


///////////////////////////////////////////////////////////////////////////////
// Check differences between Production (aliamin) and update (aliaminupd)
// databases, and set the paramater 'update_state' accordingly.
///////////////////////////////////////////////////////////////////////////////
function update_update_state() {
  global $cdg_sql;

  $scope = update_scope();

  $state = 0;
  if ( (! is_array($scope)) || (count($scope) == 0)) {
    // si pas de modifications, state à 0
    $state = 0;
  } else {
    // si modifications (au moins un service a "1"), state a 1
    while ( list( $service, $val ) = each( $scope ) ) {
      if ($val == "1") {
        $state = 1;
        break;
      }
    }
  }

  $query = "UPDATE ObmInfo
    SET obminfo_value = '$state'
    WHERE obminfo_name = 'update_state'";

  display_debug_msg($query, $cdg_sql, "update_update_state()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Final Updates detection between Production (aliamin) and update (aliaminupd)
// Parameters:
// Returns:
//   - $scope[] hash with scopes keys $scope[$key] = 1 if $key in scope
//     keys : all, ldap, cyrus, postfix, alias
///////////////////////////////////////////////////////////////////////////////
function update_scope() {
  global $cdg_sql;

  $scope = update_scope_all();
  $scope = reduce_scope_from_used_services($scope);

  return $scope;
}


///////////////////////////////////////////////////////////////////////////////
// Updates detection between Production (aliamin) and update (aliaminupd) dbs
// Parameters:
// Returns:
//   - $scope[] hash with scopes keys $scope[$key] = 1 if $key in scope
//     keys : all, ldap, cyrus, postfix, alias
///////////////////////////////////////////////////////////////////////////////
function update_scope_all() {
  global $cdg_sql;

  $scope = update_scope_UserObm();

  // Si portee maximale, on retourne directement
  if ((is_array($scope)) && (scope_is_max($scope)))
    return $scope;

  // Scope : Group
  if ( ($scope["alias"] != "1") || ($scope["ldap"] != "1") ) {
    $scope_group = update_scope_UGroup();
    if (is_array($scope_group))
      $scope = array_merge ($scope, $scope_group);
    if ((is_array($scope)) && (scope_is_max($scope)))
      return $scope;
  }

  // Scope : UserGroup
  if ( ($scope["alias"] != "1") || ($scope["ldap"] != "1") ) {
    $scope_usergroup = update_scope_UserObmGroup();
    if (is_array($scope_usergroup))
      $scope = array_merge ($scope, $scope_usergroup);
    if ((is_array($scope)) && (scope_is_max($scope)))
      return $scope;
  }

  // Scope : GroupGroup
  if ( ($scope["alias"] != "1") || ($scope["ldap"] != "1") ) {
    $scope_groupgroup = update_scope_GroupGroup();
    if (is_array($scope_groupgroup))
      $scope = array_merge ($scope, $scope_groupgroup);
    if ((is_array($scope)) && (scope_is_max($scope)))
      return $scope;
  }

  // Scope : Host
  if ( $scope["ldap"] != "1" ) {
    $scope_host = update_scope_Host();
    if (is_array($scope_host))
      $scope = array_merge ($scope, $scope_host);
    if ((is_array($scope)) && (scope_is_max($scope)))
      return $scope;
  }

  // Scope : Mail
  if ( $scope["ldap"] != "1" ) {
    $scope_mail = update_scope_Mail();
    if (is_array($scope_mail))
      $scope = array_merge ($scope, $scope_mail);
    if ((is_array($scope)) && (scope_is_max($scope)))
      return $scope;
  }

  // Scope : Samba : nothing to update from samba table

  // Scope : MailShare
  if ( ($scope["cyrus"] != "1") || ($scope["ldap"] != "1") ) {
    $scope_mailshare = update_scope_Mailshare();
    if (is_array($scope_mailshare))
      $scope = array_merge ($scope, $scope_mailshare);
    if ((is_array($scope)) && (scope_is_max($scope)))
      return $scope;
  }

  return $scope;
}


///////////////////////////////////////////////////////////////////////////////
// Tell if the update scope is maximal
// Parameters:
//   - $scope[] hash with scope keys $scope[$key] = 1 if $key in scope
//     keys : all, ldap, cyrus, postfix, alias
// Returns:
//   - true if scope is full, else false
///////////////////////////////////////////////////////////////////////////////
function scope_is_max($scope) {
  global $cgp_use, $cgp_securinet;

  if ($cgp_securinet) {
    // Cas securinet : scope postfix et alias ne sont plus utilises
    if ( ($scope["all"] == "1") ||
	 ( ((! $cgp_use["service"]["ldap"]) || ($scope["ldap"] == "1"))
	   && ((! $cgp_use["service"]["mail"]) || ($scope["cyrus"] == "1"))
	   && ((! $cgp_use["service"]["mail"]) || ($scope["amavis"] == "1"))
	   ) ) {
      return true;
    } else {
      return false;
    }
  } else {
    // Cas aliamin
    if ( ($scope["all"] == "1") ||
	 ( ((! $cgp_use["service"]["ldap"]) || ($scope["ldap"] == "1"))
	   && ((! $cgp_use["service"]["mail"]) || ($scope["cyrus"] == "1"))
	   && ((! $cgp_use["service"]["mail"]) || ($scope["postfix"] == "1"))
	   && ((! $cgp_use["service"]["mail_nomad"]) || ($scope["alias"] == "1"))
	   ) ) {
      return true;
    } else {
      return false;
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Correct the scope according to services configured to Use
// Parameters:
//   - $pscope[] hash with scope keys $scope[$key] = 1 if $key in scope
//     keys : all, ldap, cyrus, postfix, alias
// Returns:
//   - scope corrected
///////////////////////////////////////////////////////////////////////////////
function reduce_scope_from_used_services(&$scope) {
  global $cgp_use, $cgp_securinet, $cgp_postfixConf, $cgp_aliasLdap;

  while ( list( $key, $val ) = each( $cgp_use["service"] ) ) {
    if ($val == false) {
      $scope["all"] = false;
      break;
    }
  }

  if (($cgp_postfixConf == "ldap") && $scope["postfix"]) {
    $scope["postfix"] = false;
    $scope["ldap"] = true;
  } else if ($cgp_postfixConf == false) {
    $scope["postfix"] = false;
  }

  # Si les alias ne sont pas dans LDAP, et que le service LDAP est actif, il
  # faut mettre a jour les alias dans le fichier et dans LDAP (carnet d'adresse)
  if (!$cgp_aliasLdap && ($cgp_use["service"]["ldap"] == true) && $scope["alias"]) {
    $scope["alias"] = true;
    $scope["ldap"] = true;
  }

  # Si les alias sont dans LDAP, on supprime le scope alias pour mettre le scope
  # LDAP
  if ($cgp_aliasLdap && $scope["alias"]) {
    $scope["alias"] = false;
    $scope["ldap"] = true;
  }

  if ($cgp_use["service"]["mail"] == false) {
    $scope["postfix"] = false;
    $scope["cyrus"] = false;
    $scope["amavis"] = false;
    $scope["alias"] = false;
  }
  if ($cgp_use["service"]["dns"] == false) {
    $scope["dns"] = false;
  }
  if ($cgp_use["service"]["firewall"] == false) {
    $scope["firewall"] = false;
  }
  if ($cgp_use["service"]["network"] == false) {
    $scope["network"] = false;
    $scope["vpn"] = false;
  }

  // Si le service LDAP est desactive, on supprime le scope LDAP
  if ($cgp_use["service"]["ldap"] == false) {
    $scope["ldap"] = false;
  }

  // Cas securinet : on supprime les scope "postfix" et "alias"
  if ($cgp_securinet) {
    $scope["postfix"] = false;
    $scope["alias"] = false;
  }    

  return $scope;
}


///////////////////////////////////////////////////////////////////////////////
// Updates detection between Production (aliamin) and update (aliaminUpd)
// database for table UserObm
// Parameters:
// Returns:
//   - $scope[] hash with scopes keys $scope[$key] = 1 if $key in scope
//     keys : all, ldap, cyrus, postfix, alias
///////////////////////////////////////////////////////////////////////////////
function update_scope_UserObm() {
  global $cdg_sql;

  $query = "SELECT * FROM P_UserObm ORDER BY userobm_id";
  $query_upd = "SELECT * FROM UserObm ORDER BY userobm_id";
  $sys_q = new DB_OBM;
  $sys_upd_q = new DB_OBM;
  $scope = array();

  display_debug_msg($query, $cdg_sql, "update_scope_UserObm(1)");
  $sys_q->query($query);
  display_debug_msg($query_upd, $cdg_sql, "update_scope_UserObm(2)");
  $sys_upd_q->query($query_upd);

  // S'il n'y a pas le meme nombre d'enregistrement (ajout ou suppresion)
  if ($sys_q->num_rows() != $sys_upd_q->num_rows()) {
    $scope["ldap"] = 1;
    $scope["cyrus"] = 1;
    $scope["postfix"] = 1;
    $scope["alias"] = 1;

    return $scope;
  }

  // Pour tous les utilisateurs

  while ($sys_q->next_record()) {
    $sys_upd_q->next_record();

    // Si un utilisateur a change de login
    if ($sys_q->f("userobm_login") != $sys_upd_q->f("userobm_login")) {
      $scope["ldap"] = 1;
      $scope["cyrus"] = 1;
      $scope["alias"] = 1;
    }

    // Passwd in Changed on live

    if ($sys_q->f("userobm_uid") != $sys_upd_q->f("userobm_uid")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_gid") != $sys_upd_q->f("userobm_gid")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_lastname") != $sys_upd_q->f("userobm_lastname")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_firstname") != $sys_upd_q->f("userobm_firstname")) {
      $scope["ldap"] = 1;
    }
    
    if ($sys_q->f("userobm_description") != $sys_upd_q->f("userobm_description")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_address1") != $sys_upd_q->f("userobm_address1")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_address2") != $sys_upd_q->f("userobm_address2")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_address3") != $sys_upd_q->f("userobm_address3")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_zipcode") != $sys_upd_q->f("userobm_zipcode")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_town") != $sys_upd_q->f("userobm_town")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_phone") != $sys_upd_q->f("userobm_phone")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_phone2") != $sys_upd_q->f("userobm_phone2")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_phone3") != $sys_upd_q->f("userobm_phone3")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_fax") != $sys_upd_q->f("userobm_fax")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_fax2") != $sys_upd_q->f("userobm_fax2")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_direction") != $sys_upd_q->f("userobm_direction")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_title") != $sys_upd_q->f("userobm_title")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_service") != $sys_upd_q->f("userobm_service")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_function") != $sys_upd_q->f("userobm_function")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_web_perms") != $sys_upd_q->f("userobm_web_perms")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_mail_perms") != $sys_upd_q->f("userobm_mail_perms")) {
      $scope["ldap"] = 1;
      $scope["postfix"] = 1;
      $scope["cyrus"] = 1;
      $scope["alias"] = 1;
    }

    if ($sys_q->f("userobm_mail_ext_perms") != $sys_upd_q->f("userobm_mail_ext_perms")) {
      $scope["ldap"] = 1;
      $scope["postfix"] = 1;
    }

    if ($sys_q->f("userobm_email") != $sys_upd_q->f("userobm_email")) {
      $scope["ldap"] = 1;
      $scope["alias"] = 1;
    }

    if ($sys_q->f("userobm_mail_quota") != $sys_upd_q->f("userobm_mail_quota")) {
      $scope["cyrus"] = 1;
    }

    if ($sys_q->f("userobm_nomade_perms") != $sys_upd_q->f("userobm_nomade_perms")) {
      $scope["cyrus"] = 1;
    }

    if ($sys_q->f("userobm_nomade_enable") != $sys_upd_q->f("userobm_nomade_enable")) {
      $scope["cyrus"] = 1;
    }

    if ($sys_q->f("userobm_nomade_local_copy") != $sys_upd_q->f("userobm_nomade_local_copy")) {
      $scope["cyrus"] = 1;
    }

    if ($sys_q->f("userobm_email_nomade") != $sys_upd_q->f("userobm_email_nomade")) {
      $scope["cyrus"] = 1;
    }

    if ($sys_q->f("userobm_vacation_enable") != $sys_upd_q->f("userobm_vacation_enable")) {
      $scope["cyrus"] = 1;
    }

    if ($sys_q->f("userobm_vacation_message") != $sys_upd_q->f("userobm_vacation_message")) {
      $scope["cyrus"] = 1;
    }

    if ($sys_q->f("userobm_samba_perms") != $sys_upd_q->f("userobm_samba_perms")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_samba_home") != $sys_upd_q->f("userobm_samba_home")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_samba_home_drive") != $sys_upd_q->f("userobm_samba_home_drive")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobm_samba_logon_script") != $sys_upd_q->f("userobm_samba_logon_script")) {
      $scope["ldap"] = 1;
    }

    // Si portée deja maximale pour les users, on retourne directement
    if ( ($scope["ldap"] == 1)
	 && ($scope["cyrus"] == 1)
	 && ($scope["postfix"] == 1)
	 && ($scope["alias"] == 1) ) {
      return $scope;
    }
  }

  return $scope;
}


///////////////////////////////////////////////////////////////////////////////
// Updates detection between Production (aliamin) and update (aliaminUpd)
// database for table UGroup
// Returns:
//   - $scope[] hash with scopes keys $scope[$key] = 1 if $key in scope
//     keys : all, ldap, cyrus, postfix, alias
///////////////////////////////////////////////////////////////////////////////
function update_scope_UGroup() {
  global $cdg_sql;

  $query = "SELECT * FROM P_UGroup ORDER BY group_id";
  $query_upd = "SELECT * FROM UGroup ORDER BY group_id";
  $sys_q = new DB_OBM;
  $sys_upd_q = new DB_OBM;

  display_debug_msg($query, $cdg_sql, "update_scope_UGroup(1)");
  $sys_q->query($query);
  display_debug_msg($query_upd, $cdg_sql, "update_scope_UGroup(2)");
  $sys_upd_q->query($query_upd);

  // S'il n'y a pas le meme nombre d'enregistrement (ajout ou suppresion)
  if ($sys_q->num_rows() != $sys_upd_q->num_rows()) {
    $scope["alias"] = 1;
    $scope["ldap"] = 1;
    return $scope;
  }

  // Pour tous les groupes

  while ($sys_q->next_record()) {
    $sys_upd_q->next_record();

    // Si un groupe a change d'adresse e-mail
    if ($sys_q->f("group_email") != $sys_upd_q->f("group_email")) {
      $scope["alias"] = 1;
      $scope["ldap"] = 1;
    }

    // Si un groupe a change de nom
    if ($sys_q->f("group_name") != $sys_upd_q->f("group_name")) {
      $scope["ldap"] = 1;
    }

    // Si un groupe a change son appartenance windows
    if ($sys_q->f("group_samba") != $sys_upd_q->f("group_samba")) {
      $scope["ldap"] = 1;
    }

    // Si un groupe a change son gid
    if ($sys_q->f("group_gid") != $sys_upd_q->f("group_gid")) {
      $scope["ldap"] = 1;
    }

    // Si un groupe a change de description
    if ($sys_q->f("group_desc") != $sys_upd_q->f("group_desc")) {
      $scope["ldap"] = 1;
    }

    // Si un groupe a change d'adresses de contacts externes
    if ($sys_q->f("group_contacts") != $sys_upd_q->f("group_contacts")) {
      $scope["alias"] = 1;
      $scope["ldap"] = 1;
    }

    // Si portée deja maximale pour les groupes, on retourne directement
    if (($scope["alias"] == 1) && ($scope["ldap"] == 1)) {
      return $scope;
    }
  }

  return $scope;
}


///////////////////////////////////////////////////////////////////////////////
// Updates detection between Production (aliamin) and update (aliaminUpd)
// database for table UserObmGroup
// Returns:
//   - $scope[] hash with scopes keys $scope[$key] = 1 if $key in scope
//     keys : all, ldap, cyrus, postfix, alias
///////////////////////////////////////////////////////////////////////////////
function update_scope_UserObmGroup() {
  global $cdg_sql;

  $query = "SELECT * FROM P_UserObmGroup
    ORDER BY userobmgroup_group_id, userobmgroup_userobm_id";
  $query_upd = "SELECT * FROM UserObmGroup
    ORDER BY userobmgroup_group_id, userobmgroup_userobm_id";
  $sys_q = new DB_OBM;
  $sys_upd_q = new DB_OBM;

  display_debug_msg($query, $cdg_sql, "update_scope_UserObmGroup(1)");
  $sys_q->query($query);
  display_debug_msg($query_upd, $cdg_sql, "update_scope_UserObmGroup(2)");
  $sys_upd_q->query($query_upd);

  // S'il n'y a pas le meme nombre d'enregistrement (ajout ou suppresion)
  if ($sys_q->num_rows() != $sys_upd_q->num_rows()) {
    $scope["alias"] = 1;
    $scope["ldap"] = 1;
    return $scope;
  }

  // Pour tous les membres de groupe groupes

  while ($sys_q->next_record()) {
    $sys_upd_q->next_record();

    // Si une ligne est differente
    if ($sys_q->f("userobmgroup_group_id") != $sys_upd_q->f("userobmgroup_group_id")) {
      $scope["alias"] = 1;
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("userobmgroup_userobm_id") != $sys_upd_q->f("userobmgroup_userobm_id")) {
      $scope["alias"] = 1;
      $scope["ldap"] = 1;
    }

    // Si portée deja maximale pour les groupes, on retourne directement
    if (($scope["alias"] == 1) && ($scope["ldap"] == 1)) {
      return $scope;
    }
  }

  return $scope;
}


///////////////////////////////////////////////////////////////////////////////
// Updates detection between Production (aliamin) and update (aliaminUpd)
// database for table GroupGroup
// Returns:
//   - $scope[] hash with scopes keys $scope[$key] = 1 if $key in scope
//     keys : all, ldap, cyrus, postfix, alias
///////////////////////////////////////////////////////////////////////////////
function update_scope_GroupGroup() {
  global $cdg_sql;

  $query = "SELECT * FROM P_GroupGroup
    ORDER BY groupgroup_parent_id, groupgroup_child_id";
  $query_upd = "SELECT * FROM GroupGroup
    ORDER BY groupgroup_parent_id, groupgroup_child_id";
  $sys_q = new DB_OBM;
  $sys_upd_q = new DB_OBM;

  display_debug_msg($query, $cdg_sql, "update_scope_GroupGroup(1)");
  $sys_q->query($query);
  display_debug_msg($query_upd, $cdg_sql, "update_scope_GroupGroup(2)");
  $sys_upd_q->query($query_upd);

  // S'il n'y a pas le meme nombre d'enregistrement (ajout ou suppresion)
  if ($sys_q->num_rows() != $sys_upd_q->num_rows()) {
    $scope["alias"] = 1;
    $scope["ldap"] = 1;
    return $scope;
  }

  // Pour tous les groupes membres de groupe

  while ($sys_q->next_record()) {
    $sys_upd_q->next_record();

    // Si une ligne est differente
    if ($sys_q->f("groupgroup_parent_id") != $sys_upd_q->f("groupgroup_parent_id")) {
      $scope["alias"] = 1;
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("groupgroup_child_id") != $sys_upd_q->f("groupgroup_child_id")) {
      $scope["alias"] = 1;
      $scope["ldap"] = 1;
    }

    // Si portée deja maximale pour les groupes, on retourne directement
    if (($scope["alias"] == 1) && ($scope["ldap"] == 1)) {
      return $scope;
    }
  }

  return $scope;
}


///////////////////////////////////////////////////////////////////////////////
// Updates detection between Production (aliamin) and update (aliaminUpd)
// database for table Host
// Returns:
//   - $scope[] hash with scopes keys $scope[$key] = 1 if $key in scope
//     keys : all, ldap, cyrus, postfix, alias
///////////////////////////////////////////////////////////////////////////////
function update_scope_Host() {
  global $cdg_sql;

  $query = "SELECT * FROM P_Host ORDER BY host_uid";
  $query_upd = "SELECT * FROM Host ORDER BY host_uid";
  $sys_q = new DB_OBM;
  $sys_upd_q = new DB_OBM;

  display_debug_msg($query, $cdg_sql, "update_scope_Host(1)");
  $sys_q->query($query);
  display_debug_msg($query_upd, $cdg_sql, "update_scope_Host(2)");
  $sys_upd_q->query($query_upd);

  // S'il n'y a pas le meme nombre d'enregistrement (ajout ou suppresion)
  if ($sys_q->num_rows() != $sys_upd_q->num_rows()) {
    $scope["ldap"] = 1;
    return $scope;
  }

  // Pour tous hotes

  while ($sys_q->next_record()) {
    $sys_upd_q->next_record();

    // Si une ligne est differente
    if ($sys_q->f("host_uid") != $sys_upd_q->f("host_uid")) {
      $scope["ldap"] = 1;
    }
    
    if ($sys_q->f("host_gid") != $sys_upd_q->f("host_gid")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("host_samba") != $sys_upd_q->f("host_samba")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("host_name") != $sys_upd_q->f("host_name")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("host_ip") != $sys_upd_q->f("host_ip")) {
      $scope["ldap"] = 1;
    }

    if ($sys_q->f("host_description") != $sys_upd_q->f("host_description")) {
      $scope["ldap"] = 1;
    }

    // Si portée deja maximale pour les hotes, on retourne directement
    if ( $scope["ldap"] == 1 ) {
      return $scope;
    }
  }

  return $scope;
}


///////////////////////////////////////////////////////////////////////////////
// Updates detection between Production (aliamin) and update (aliaminUpd)
// database for table Mail
// Returns:
//   - $scope[] hash with scopes keys $scope[$key] = 1 if $key in scope
//     keys : all, ldap, cyrus, postfix, alias
///////////////////////////////////////////////////////////////////////////////
function update_scope_Mail() {
  global $cdg_sql;

  $query = "SELECT * FROM P_MailServer ORDER BY mailserver_id";
  $query_upd = "SELECT * FROM MailServer ORDER BY mailserver_id";
  $sys_q = new DB_OBM;
  $sys_upd_q = new DB_OBM;

  display_debug_msg($query, $cdg_sql, "update_scope_Mail(1)");
  $sys_q->query($query);
  display_debug_msg($query_upd, $cdg_sql, "update_scope_Mail(2)");
  $sys_upd_q->query($query_upd);

  // S'il n'y a pas le meme nombre d'enregistrement (ajout ou suppresion)
  if ($sys_q->num_rows() != $sys_upd_q->num_rows()) {
    $scope["ldap"] = 1;
    $scope["postfix"] = 1;
    return $scope;
  }

  // Pour toutes les lignes Mail

  while ($sys_q->next_record()) {
    $sys_upd_q->next_record();

    $m_id = $sys_q->f("mailserver_id");
    $m_host_id = $sys_q->f("mailserver_host_id");
    $m_relayhost_id = $sys_upd_q->f("mailserver_relayhost_id");
    $mu_id = $sys_upd_q->f("mailserver_id");
    $mu_host_id = $sys_upd_q->f("mailserver_host_id");
    $mu_relayhost_id = $sys_upd_q->f("mailserver_relayhost_id");

    if (($m_id != $mu_id) || ($m_host_id != $mu_host_id) || ($m_relayhost_id != $mu_relayhost_id)) {
      $scope["ldap"] = 1;
      $scope["postfix"] = 1;
      return $scope;
    }
  }

  return $scope;
}


///////////////////////////////////////////////////////////////////////////////
// Not used for now
// Updates detection between Production (aliamin) and update (aliaminUpd)
// database for table Samba
// Returns:
//   - $scope[] hash with scopes keys $scope[$key] = 1 if $key in scope
//     keys : all, ldap, cyrus, postfix, alias
///////////////////////////////////////////////////////////////////////////////
function update_scope_Samba() {
  global $cdg_sql;

  $query = "SELECT * FROM P_Samba ORDER BY samba_name, samba_value";
  $query_upd = "SELECT * FROM Samba ORDER BY samba_name, samba_value";
  $sys_q = new DB_OBM;
  $sys_upd_q = new DB_OBM;

  display_debug_msg($query, $cdg_sql, "update_scope_Samba(1)");
  $sys_q->query($query);
  display_debug_msg($query_upd, $cdg_sql, "update_scope_Samba(2)");
  $sys_upd_q->query($query_upd);

  // S'il n'y a pas le meme nombre d'enregistrement (ajout ou suppresion)
  if ($sys_q->num_rows() != $sys_upd_q->num_rows()) {
    $scope["ldap"] = 1;
    $scope["postfix"] = 1;
    return $scope;
  }

  // Pour toutes les lignes Mail

  while ($sys_q->next_record()) {
    $sys_upd_q->next_record();

    $samba_name = $sys_q->f("samba_name");
    $samba_value = $sys_q->f("samba_value");
    $samba_name_upd = $sys_upd_q->f("samba_name");
    $samba_value_upd = $sys_upd_q->f("samba_value");

    if (($samba_name != $samba_name_upd) || ($samba_value != $samba_value_upd)) {
      $scope["ldap"] = 1;
      $scope["postfix"] = 1;
      return $scope;
    }
  }

  return $scope;
}


///////////////////////////////////////////////////////////////////////////////
// Updates detection between Production (aliamin) and update (aliaminUpd)
// database for table Mailshare
// Returns:
//   - $scope[] hash with scopes keys $scope[$key] = 1 if $key in scope
//     keys : all, ldap, cyrus, postfix, alias
///////////////////////////////////////////////////////////////////////////////
function update_scope_Mailshare() {
  global $cdg_sql;

  // Mailshare entries
  $query = "SELECT * FROM P_MailShare ORDER BY mailshare_id";
  $query_upd = "SELECT * FROM MailShare ORDER BY mailshare_id";
  $sys_q = new DB_OBM;
  $sys_upd_q = new DB_OBM;

  display_debug_msg($query, $cdg_sql, "update_scope_Mailshare(1)");
  $sys_q->query($query);
  display_debug_msg($query_upd, $cdg_sql, "update_scope_Mailshare(2)");
  $sys_upd_q->query($query_upd);

  // S'il n'y a pas le meme nombre d'enregistrement (ajout ou suppresion)
  if ($sys_q->num_rows() != $sys_upd_q->num_rows()) {
    $scope["cyrus"] = 1;
    $scope["ldap"] = 1;
    $scope["alias"] = 1;

    return $scope;
  }

  // Pour toutes les lignes Ldap

  while ($sys_q->next_record()) {
    $sys_upd_q->next_record();

    $id = $sys_q->f("mailshare_id");
    $name = $sys_q->f("mailshare_name");
    $quota = $sys_q->f("mailshare_quota");
    $email = $sys_q->f("mailshare_email");
    $desc = $sys_q->f("mailshare_description");

    $id_upd = $sys_upd_q->f("mailshare_id");
    $name_upd = $sys_upd_q->f("mailshare_name");
    $quota_upd = $sys_upd_q->f("mailshare_quota");
    $email_upd = $sys_upd_q->f("mailshare_email");
    $desc_upd = $sys_upd_q->f("mailshare_description");

    // Si modif du quota, alors cyrus
    if ($quota != $quota_upd) {
      $scope["cyrus"] = 1;
    }
    // Si desc scope ldap
    if ($desc != $desc_upd) {
      $scope["ldap"] = 1;
    }
    // Si email, scope alias
    if ($email != $email_upd) {
      $scope["alias"] = 1;
    }
    // Si id, nom ou e-mail different, il y a eu ajout et suppression, ldap + cyrus
    if (($id != $id_upd) || ($name != $name_upd)) {
      $scope["alias"] = 1;
      $scope["ldap"] = 1;
      $scope["cyrus"] = 1;
    }
    // Si portée deja maximale pour le mailshare, on retourne directement
    if ( ($scope["ldap"] == 1)
	 && ($scope["cyrus"] == 1)
     && ($scope["alias"] == 1) ) {
      return $scope;
    }

  }

  // Si portée deja maximale pour le mailshare entity, on retourne directement
  if ( ($scope["cyrus"] == 1) ) {
    return $scope;
  }

  // Mailshare entities link
  $query = "SELECT *
    FROM P_EntityRight
    WHERE entityright_entity = 'mailshare'
    ORDER BY entityright_entity_id, entityright_consumer, entityright_consumer_id";
  $query_upd = "SELECT *
    FROM EntityRight
    WHERE entityright_entity = 'mailshare'
    ORDER BY entityright_entity_id, entityright_consumer, entityright_consumer_id";
  $sys_q = new DB_OBM;
  $sys_upd_q = new DB_OBM;

  display_debug_msg($query, $cdg_sql, "update_scope_Mailshare(3)");
  $sys_q->query($query);
  display_debug_msg($query_upd, $cdg_sql, "update_scope_Mailshare(4)");
  $sys_upd_q->query($query_upd);

  // S'il n'y a pas le meme nombre d'enregistrement (ajout ou suppresion)
  if ($sys_q->num_rows() != $sys_upd_q->num_rows()) {
    $scope["cyrus"] = 1;
    return $scope;
  }

  // Pour toutes les entrees

  while ($sys_q->next_record()) {
    $sys_upd_q->next_record();

    $id = $sys_q->f("entityright_entity_id");
    $con = $sys_q->f("entityright_consumer");
    $con_id = $sys_q->f("entityright_consumer_id");
    $read = $sys_q->f("entityright_read");
    $write = $sys_q->f("entityright_write");

    $id_upd = $sys_upd_q->f("entityright_entity_id");
    $con_upd = $sys_upd_q->f("entityright_consumer");
    $con_id_upd = $sys_upd_q->f("entityright_consumer_id");
    $read_upd = $sys_upd_q->f("entityright_read");
    $write_upd = $sys_upd_q->f("entityright_write");

    // Si difference, scope cyrus
    if (($id != $id_upd) || ($con != $con_upd) || ($con_id != $con_id_upd)
	|| ($read != $read_upd) || ($write != $write_upd)) {
      $scope["cyrus"] = 1;
    }

    // Si portée deja maximale pour le mailshare, on retourne directement
    if ( ($scope["cyrus"] == 1) ) {
      return $scope;
    }

  }

  return $scope;
}


?>
