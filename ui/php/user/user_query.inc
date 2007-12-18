<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : user_query.inc                                               //
//     - Desc : User query & db File                                         //
// 2000-01-13 Aliacom - Florent Goalabre                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// User query search query execution
// Parameters:
//   - $user[] : user search criteria
//     keys used  : name, perms
//   - $sql_order_field : infos for order clause
//   - $sql_order_dir : direction for order clause (asc, desc)
//   - $restriction : if and which restriction to set (eg calendar readable)
///////////////////////////////////////////////////////////////////////////////
function run_query_user_search($user) {
  global $obm, $c_all, $cdg_sql, $ctu_sql_limit;
  global $cgp_archive_only;

  $sql_order_dir = $user['sql_order_dir'];
  $sql_order_field = $user['sql_order_field'];

  $restriction = $user['restriction_calendar'];
  $login = sql_search_text_parse($user['login']);
  $profile = $user['profile'];
  $domain_id = $user['domain'];
  $lname = sql_search_text_parse($user['lastname']);
  $delegation = sql_search_text_parse($user['delegation']);
  $email = sql_search_text_parse($user['email']);
  $desc = sql_search_text_parse($user['desc']);
  $group = sql_search_text_parse($user['group']);
  $archive = $user['archive'];
  $phone = $user['phone'];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $datebegin = sql_date_format($db_type, 'userobm_datebegin', 'datebegin');
  $lastaccess = sql_date_format($db_type, 'userobm_timelastaccess', 'timelastaccess');
  $multidomain = sql_multidomain('userobm');

  if ($restriction == 'calendar') {
    $users = of_right_entity_for_consumer('calendar', 'user', $obm['uid'], 'read','','userobm');
    if ((is_array($users['ids'])) && (count($users['ids']) > 0)) {
      $where_restriction = " AND (userobm_id IN ($obm[uid], ";
      foreach($users['ids'] as $u_id) {
	$where_restriction .= "$coma'$u_id'";
	$coma = ',';
      }
      $where_restriction .= '))';
    } else {
      // If no users have given $uid with read right, return empty set
      $where_restriction = ' AND userobm_id < 0';
    }
  }

  $where = '';

  // If a login indication has been specified, get it
  if (trim($login) != '') {
     $where .= " userobm_login $like '$login%'";
  }

  // If a user profile indication has been specified, get it
  if (($profile != $c_all) && ($profile != '')) {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_perms='$profile'";
  }

  // If a user domain indication has been specified, get it
  if (($domain_id != $c_all) && ($domain_id != '')) {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_domain_id='$domain_id'";
  }

  // If a lastname indication has been specified, get it
  if (trim($lname) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_lastname $like '$lname%'";
  }

  // If a delegation indication has been specified, get it
  if (trim($delegation) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_delegation $like '$delegation%'";
  }

  // If an email indication has been specified, get it
  if (trim($email) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_email like '%$email%'";
  }

  // If a phone number indication has been specified, get it
  if (trim($phone) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " (userobm_phone like '%$phone%'
       OR userobm_phone2 like '%$phone%'
       OR userobm_mobile like '%$phone%')";
  }

  // If a description indication has been specified, get it
  if (trim($desc) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_description like '%$desc%'";
  }

  // If a group indication has been specified, get it
  if (trim($group) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " group_name like '$group%'";
     $join_grp = 'LEFT JOIN of_usergroup ON userobm_id=of_usergroup_user_id
                  LEFT JOIN UGroup ON of_usergroup_group_id=group_id';
  }

  if ($archive != '1') {
    if (trim($where) != '') $where .= ' AND';
    $where .= " userobm_archive = '0'";
  } elseif($cgp_archive_only) {
    if (trim($where) != '') $where .= ' AND';
    $where .= " userobm_archive = '1'";
  }

  if ((trim($where) != '') || (trim($where_restriction) != '')) {
    $whereq = " WHERE $where $where_restriction $multidomain";
  } else {
    $whereq = " WHERE 1=1 $multidomain";
  }

  $select = "SELECT distinct UserObm.*,
      userobm_id as id,
      domain_label,
      domain_name,
      $datebegin,
      $lastaccess
    FROM UserObm 
    LEFT JOIN Domain ON userobm_domain_id = domain_id
    $join_grp";


  // ORDER construction
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'userobm_login';
  if ($order == 'userobm_mail_quota') {
    $order = 'userobm_mail_quota_use / userobm_mail_quota';
  }
  $orderq .= " ORDER BY $order $sql_order_dir";

  $query = "$select
    $whereq
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(distinct userobm_id) FROM UserObm $join_grp $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'run_query_user_search()');
    $obm_q->query($query);
  }

  return $obm_q;
}


/**
 * Search users from a single field 
 * 
 * @param mixed $user 
 * @access public
 * @return array of 'length' => DB user results nb, and 'datas' => DB User search query results
 */
function run_query_user_ext_search($user) {
  global $obm, $c_all, $cdg_sql, $ctu_sql_limit;

  $pattern = $user['pattern'];
  $restriction = $user['restriction'];
  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain('UserObm.userobm');

  $limit = $user['limit'];
  if(isset($user['first_row'])) $first_row = $user['first_row'] - 1;
  else $first_row = 0;

  if ($restriction == 'calendar') {
    $users = of_right_entity_for_consumer('calendar', 'user', $obm['uid'], 'read','','userobm');
    if ((is_array($users['ids'])) && (count($users['ids']) > 0)) {
      $where_restriction = " AND (userobm_id IN ($obm[uid], ";
      foreach($users['ids'] as $u_id) {
	$where_restriction .= "$coma'$u_id'";
	$coma = ',';
      }
      $where_restriction .= '))';
    } else {
      // If no users have given $uid with read right, return empty set
      $where_restriction = ' AND userobm_id < 0';
    }
  }

  $query = "SELECT 
      count(*) as resultsCount
    FROM UserObm
    WHERE (
      userobm_lastname LIKE '$pattern%'
      OR userobm_login LIKE '$pattern%' 
      OR userobm_firstname LIKE '$pattern%' 
      OR userobm_email LIKE '$pattern%' )
      AND userobm_archive = '0'
      $multidomain
      $where_restriction";

  display_debug_msg($query, $cdg_sql, 'run_query_user_ext_search()');
  $obm_q->query($query);
  $obm_q->next_record();
  $resultsCount = $obm_q->f('resultsCount');

  $query = "SELECT 
      userobm_id,
      userobm_lastname,
      userobm_firstname,
      userobm_email
    FROM UserObm
    WHERE (
      userobm_lastname LIKE '$pattern%'
      OR userobm_login LIKE '$pattern%' 
      OR userobm_firstname LIKE '$pattern%' 
      OR userobm_email LIKE '$pattern%' )
      AND userobm_archive = '0'
      $multidomain
      $where_restriction
    ORDER BY  userobm_lastname, userobm_firstname
    LIMIT $first_row, $limit";

  display_debug_msg($query, $cdg_sql, 'run_query_user_ext_search()');
  $obm_q->query($query);

  return array('length' => $resultsCount, 'datas' => $obm_q);
}


///////////////////////////////////////////////////////////////////////////////
// User detail query execution
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_user_detail($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, 'UserObm.userobm_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'UserObm.userobm_timecreate', 'timecreate');
  $datebegin = sql_date_format($db_type, 'UserObm.userobm_datebegin', 'datebegin');
  $lastaccess = sql_date_format($db_type, 'UserObm.userobm_timelastaccess', 'userobm_timelastaccess');
  $multidomain = sql_multidomain('UserObm.userobm');
	
  $query = "SELECT UserObm.*,
      $timeupdate,
      $timecreate,
      $datebegin,
      $lastaccess,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate,
      domain_label,
      domain_name,
      ms.host_name as mailserver_hostname,
      mailserver_id,
      ms.host_id as mailserver_host_id,
      Host.host_name,
      Host.host_web_perms
    FROM UserObm
         LEFT JOIN UserObm as c ON UserObm.userobm_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON UserObm.userobm_userupdate=u.userobm_id
         LEFT JOIN Domain on UserObm.userobm_domain_id=domain_id
         LEFT JOIN Host on UserObm.userobm_host_id=Host.host_id
         LEFT JOIN MailServer on UserObm.userobm_mail_server_id=mailserver_id
         LEFT JOIN Host ms on mailserver_host_id=ms.host_id
    WHERE UserObm.userobm_id='$p_id'
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_user_detail()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// User insert query execution
// Parameters:
//   - $user[]   : entry values
// Returns:
//   $id : new user id if ok, else false
///////////////////////////////////////////////////////////////////////////////
function run_query_user_insert($user) {
  global $obm, $profiles, $cdg_sql, $cgp_use, $password_encryption, $perms_admin;
  global $cg_gid_smb_admin, $cg_gid_smb_user, $c_first_gid_admin, $c_first_gid_user;

  $now = date('Y-m-d H:i:s');
  $uid = $obm['uid'];
  $login = $user['login'];
  $passwd = $user['passwd'];
  if ($passwd != '') {
    // If a password type is given, get it else get the system parameter
    $passwd_type = $user['password_type'];
    if (($passwd_type == 'PLAIN')
	|| ($passwd_type == 'CRYPT')
	|| ($passwd_type == 'PLAIN')) {
      $passwd_type = $passwd_type;
    } else {
      $passwd_type = $password_encryption;
    }

    if ($passwd_type == 'CRYPT') {
      $passwd_crypt = crypt($passwd);
    } else if ($passwd_type == 'MD5SUM') {
      $passwd_crypt = md5($passwd);
    } else {
      $passwd_crypt = $passwd;
    }
  }

  // infos
  $profile = $user['profile'];
  $archive = ($user['archive'] == '1' ? '1' : '0');
  $hidden = ($user['hidden'] == '1' ? '1' : '0');
  $kind = $user['kind'];
  $datebegin = ($user['datebegin'] ? "'".of_isodate_convert($user['datebegin'])."'" : 'null');
  $lname = $user['lastname'];
  $fname = $user['firstname'];
  $title = $user['title'];
  $email = $user['email'];
  $desc = $user['desc'];

  // Target Delegation handling (if updated user as delegation admin realm)
  $admin_realm = $profiles[$profile]['properties']['admin_realm'];
  if (is_array($admin_realm)) {
    if (in_array('delegation', $admin_realm)) {
      $delegation_target = $user['delegation_target'];
      $q_insert_delegation_target = ',
    userobm_delegation_target';
      $q_value_delegation_target = ",
    '$delegation_target'";
    }
  }

  // Delegation handling
  $q_delegation = of_delegation_query_insert_clauses('userobm_delegation', $user['delegation']);

  // Coord
  $phone = $user['phone'];
  $phone2 = $user['phone2'];
  $mobile = $user['mobile'];
  $fax = $user['fax'];
  $fax2 = $user['fax2'];
  $company = $user['company'];
  $direction = $user['direction'];
  $service = $user['service'];
  $ad1 = $user['ad1'];
  $ad2 = $user['ad2'];
  $ad3 = $user['ad3'];
  $zip = $user['zip'];
  $town = $user['town'];
  $cdx = $user['cdx'];
  $ctry = $user['country'];

  // Section Web
  if ($cgp_use['service']['web']) {
    // Web access
    $host_name = $user['host_name'];
    $host_id = $user['host_id'];
    $web_perms = ($user['web_perms'] == '' ? '0' : $user['web_perms']);
    $web_all = ($user['web_all'] == '' ? '0' : $user['web_all']);
    $web_list = $user['web_list'];
    $query_insert_web = ',
    userobm_web_perms,
    userobm_web_all,
    userobm_web_list';
    $query_values_web = ",
    '$web_perms',
    '$web_all',
    '$web_list'";
  }

  // Section Mail
  if ($cgp_use['service']['mail']) {
    $mail_perms = ($user['mail_perms'] == '' ? '0' : $user['mail_perms']);
    $mail_ext_perms = ($user['mail_ext_perms'] == '' ? '0' : $user['mail_ext_perms']);
    $mail_server_id = ($user['mail_server_id'] == '' ? '0' : $user['mail_server_id']);
    $mail_quota = $user['mail_quota'];
    if ($mail_quota == '') {
      $mail_quota = '0';
    }
    $query_insert_mail = ',
    userobm_mail_perms,
    userobm_mail_ext_perms,
    userobm_mail_server_id,
    userobm_mail_quota';
    $query_values_mail = ",
    '$mail_perms',
    '$mail_ext_perms',
    '$mail_server_id',
    '$mail_quota'";
  }

  // Section Mail Nomad
  if ($cgp_use['service']['mail_nomad']) {
    $nomade_perms = ($user['nomade_perms'] == '' ? '0' : $user['nomade_perms']);
    $nomade_enable = ($user['nomade_enable'] == '' ? '0' : $user['nomade_enable']);
    $nomade_local_copy = ($user['nomade_local_copy'] == '' ? '0' : $user['nomade_local_copy']);
    $nomad_email = $user['email_nomade'];
    $query_insert_mail_nomad = ',
    userobm_email_nomade,
    userobm_nomade_perms,
    userobm_nomade_enable,
    userobm_nomade_local_copy';
    $query_values_mail_nomad = ",
    '$nomad_email',
    '$nomade_perms',
    '$nomade_enable',
    '$nomade_local_copy'";
  }

  // Section Mail Vacation
  if ($cgp_use['service']['mail_vacation']) {
    $vacation_enable = ($user['vacation_enable'] == '' ? '0' : $user['vacation_enable']);
    $vacation_message = $user['vacation_message'];
    $query_insert_mail_vacation = ',
    userobm_vacation_enable,
    userobm_vacation_message';
    $query_values_mail_vacation = ",
    '$vacation_enable',
    '$vacation_message'";
  }

  // Section Samba
  if ($cgp_use['service']['samba']) {
    $smb_perms = ($user['smb_perms'] == '' ? '0' : $user['smb_perms']);
    $smb_home = $user['smb_home'];
    $smb_home_drive = $user['smb_home_drive'];
    $smb_logon = $user['smb_logon'];
    $query_insert_samba = ',
    userobm_samba_perms,
    userobm_samba_home,
    userobm_samba_home_drive,
    userobm_samba_logon_script';
    $query_values_samba = ",
    '$smb_perms',
    '$smb_home',
    '$smb_home_drive',
    '$smb_logon'";
  }

  // Uid, gid
  $user_uid = get_first_user_free_uid();
  if ($profile == "$perms_admin") {
    if ($cgp_use['service']['samba']) {
      $gid = $cg_gid_smb_admin;
    } else {
      $gid = $c_first_gid_admin;
    }
  } else {
    if ($cgp_use['service']['samba']) {
      $gid = $cg_gid_smb_user;
    } else {
      $gid = $c_first_gid_user;
    }
  }

  if (isset($user['domain']) && $obm['domain'] == 0) {
    $domain_id = $user['domain'];
  } else {
    $domain_id = $obm['domain_id'];
  }
  if ($domain_id == 0) {
    $profile == 'admin';
  }
  $query = "INSERT INTO UserObm (
    userobm_domain_id,
    userobm_timeupdate,
    userobm_timecreate,
    userobm_userupdate,
    userobm_usercreate,
    userobm_login,
    userobm_password,
    userobm_password_type,
    userobm_perms,
    userobm_uid,
    userobm_gid,
    userobm_archive,
    userobm_datebegin,
    userobm_hidden,
    userobm_kind,
    userobm_lastname,
    userobm_firstname,
    userobm_title,
    userobm_company,
    userobm_direction,
    userobm_service,
    userobm_address1,
    userobm_address2,
    userobm_address3,
    userobm_zipcode,
    userobm_town,
    userobm_expresspostal,
    userobm_country_iso3166,
    userobm_phone,
    userobm_phone2,
    userobm_mobile,
    userobm_fax,
    userobm_fax2,
    userobm_email
    $q_insert_delegation_target
    $q_delegation[field],
    userobm_description
    $query_insert_web
    $query_insert_mail
    $query_insert_mail_nomad
    $query_insert_mail_vacation
    $query_insert_samba
  ) VALUES (
    '$domain_id',
    '$now',
    '$now',
    '$uid',
    '$uid',
    '$login',
    '$passwd_crypt',
    '$passwd_type',
    '$profile',
    '$user_uid',
    '$gid',
    '$archive',
    $datebegin,
    '$hidden',
    '$kind',
    '$lname',
    '$fname',
    '$title',
    '$company',
    '$direction',
    '$service',
    '$ad1',
    '$ad2',
    '$ad3',
    '$zip',
    '$town',
    '$cdx',
    '$country',
    '$phone',
    '$phone2',
    '$mobile',
    '$fax',
    '$fax2',
    '$email'
    $q_value_delegation_target
    $q_delegation[value],
    '$desc'
    $query_values_web
    $query_values_mail
    $query_values_mail_nomad
    $query_values_mail_vacation
    $query_values_samba
    )";

  display_debug_msg($query, $cdg_sql, 'run_query_user_insert()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    // Get the user id
    $query = "SELECT userobm_id as id
    FROM UserObm
    WHERE userobm_login = '$login'
      AND userobm_lastname='$lname'
      AND userobm_firstname='$fname'
      AND userobm_domain_id='$domain_id'";
  
    display_debug_msg($query, $cdg_sql, 'run_query_user_insert(2)');
    $obm_q->query($query);
    $obm_q->next_record();
    $id = $obm_q->f('id');
  } else {
    $id = false;
  }

  if ($id > 0) {
    $ret = of_userdata_query_update('user', $id, $user);
    // Insert user's photo
    if(isset($user['file_tmp']) && $user['file_tmp'] != '') {
      $user['title'] = $login;
      $doc_id = run_query_global_insert_document($user, $id, 'user');
      if ($doc_id > 0) {
        // Update userobm_photo_id
        $query = "UPDATE UserObm SET userobm_photo_id='$doc_id' WHERE userobm_id='$id'"; 
        display_debug_msg($query, $cdg_sql, 'run_query_user_insert(3)');
        $obm_q->query($query);
      }
    }
  }

  // Subscribe the user to his default group
  run_query_user_init_group($id, $gid, NULL);

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Init user member of group
// Parameters:
//   - $u_id : user id
//   - $gid  : group GID (not id)
// Return: number of usergroup inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_user_init_group($u_id, $gid, $oldGid) {
  global $cdg_sql;

  if ($oldGid != NULL) {
    $group_id = get_group_id_from_gid($oldGid);

    $query = "DELETE FROM UserObmGroup
      WHERE userobmgroup_userobm_id = '$u_id'
      AND userobmgroup_group_id = '$group_id'";

    display_debug_msg($query, $cdg_sql, 'run_query_user_init_group()');
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
  }

  // we get the group gid from its id
  $group_id = get_group_id_from_gid($gid);

  if ($group_id > 0) {
    $query = "DELETE FROM UserObmGroup
    WHERE userobmgroup_userobm_id = '$u_id'
      and userobmgroup_group_id = '$group_id'";

    display_debug_msg($query, $cdg_sql, 'run_query_user_init_group()');
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);

    $query = "INSERT INTO UserObmGroup (userobmgroup_group_id,
      userobmgroup_userobm_id)
    VALUES ('$group_id', '$u_id')";

    display_debug_msg($query, $cdg_sql, 'run_query_user_init_group()');
    $retour = $obm_q->query($query);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// User update query execution
// Parameters:
//   - $p_id     : user id
//   - $user[]   : entry values
//     keys used : login, passwd, perms, archive, *name, email
///////////////////////////////////////////////////////////////////////////////
function run_query_user_update($p_id, $user) {
  global $obm, $profiles, $cdg_sql, $cgp_use, $password_encryption, $perms_admin;
  global $cg_gid_smb_admin, $cg_gid_smb_user, $c_first_gid_admin, $c_first_gid_user;
  global $cmd_ldap_passwd;

  // Connect to the BD
  $obm_q = new DB_OBM;

  $passwd = $user['passwd'];
  if ($passwd != '') {
    // If a password type is given, get it else get the system parameter
    $passwd_type = $user['password_type'];
    if (($passwd_type == 'PLAIN')
	|| ($passwd_type == 'CRYPT')
	|| ($passwd_type == 'PLAIN')) {
      $passwd_type = $passwd_type;
    } else {
      $passwd_type = $password_encryption;
    }
    if ($passwd_type == 'CRYPT') {
      $passwd_crypt = crypt($passwd);
    } else if ($passwd_type == 'MD5SUM') {
      $passwd_crypt = md5($passwd);
    } else {
      $passwd_crypt = $passwd;
    }

    $passwd_update = "userobm_password='$passwd_crypt', userobm_password_type='$password_encryption'";

    if ($cgp_use['service']['ldap']) {
      $query = "UPDATE P_UserObm SET
        $passwd_update
        WHERE userobm_id='$p_id'";

      display_debug_msg($query, $cdg_sql, 'run_query_user_update()');
      $retour = $obm_q->query($query);

      if ($retour) {
	// Get the user domain if admin in domain 0 or domain not set
        if ((!isset($obm['domain_id'])) || ($obm['domain_id'] == '') || ($obm['domain_id'] == '0')) {
          $query = "SELECT userobm_domain_id FROM UserObm WHERE userobm_id='$p_id'";
          display_debug_msg($query, $cdg_sql, 'run_query_user_update()');
          $retour = $obm_q->query($query);
          $obm_q->next_record();
          $user_domain = $obm_q->f('userobm_domain_id');
        } else {
          $user_domain = $obm['domain_id'];
        }
        $cmd = $cmd_ldap_passwd." --login ".$user['login']." --domain ".$user_domain." --type ".$password_encryption." --no-old --passwd '".stripslashes($passwd)."'";
        display_debug_msg($cmd, $cdg_exe, 'run_query_user_update()');
        exec($cmd);
      }
    }

    $passwd_update .= ',';
  }

  // infos
  $profile = $user['profile'];
  $archive = ($user['archive'] == '1' ? '1' : '0');
  $datebegin = ($user['datebegin'] ? "'".of_isodate_convert($user['datebegin'])."'" : 'null');
  $hidden = ($user['hidden'] == '1' ? '1' : '0');
  $kind = $user['kind'];
  $lname = $user['lastname'];
  $fname = $user['firstname'];
  $title = $user['title'];
  $email = $user['email'];
  $desc = $user['desc'];
  $multidomain = sql_multidomain('userobm');
  $photo_id = $user['document_id'];

  // Target Delegation handling (if updated user as delegation admin realm)
  $admin_realm = $profiles[$profile]['properties']['admin_realm'];
  if (is_array($admin_realm)) {
    if (in_array('delegation', $admin_realm)) {
      $delegation_target = $user['delegation_target'];
      $q_delegation_target = ",
    userobm_delegation_target='$delegation_target'";
    }
  }

  // Delegation handling
  $q_delegation = of_delegation_query_update_clause('userobm_delegation', $user['delegation']);

  // Coord
  $phone = $user['phone'];
  $phone2 = $user['phone2'];
  $mobile = $user['mobile'];
  $fax = $user['fax'];
  $fax2 = $user['fax2'];
  $company = $user['company'];
  $direction = $user['direction'];
  $service = $user['service'];
  $ad1 = $user['ad1'];
  $ad2 = $user['ad2'];
  $ad3 = $user['ad3'];
  $zip = $user['zip'];
  $town = $user['town'];
  $cdx = $user['cdx'];
  $ctry = $user['country'];

  // Section Web
  if ($cgp_use['service']['web']) {
    // Web access
    $host_name = $user['host_name'];
    $host_id = $user['host_id'];
    $web_perms = ($user['web_perms'] == '' ? '0' : $user['web_perms']);
    $web_all = ($user['web_all'] == '' ? '0' : $user['web_all']);
    $web_list = $user['web_list'];
    $query_web = ",
    userobm_web_perms='$web_perms',
    userobm_web_all='$web_all',
    userobm_web_list='$web_list'";
  }

  // Section Mail
  if ($cgp_use['service']['mail']) {
    $mail_perms = ($user['mail_perms'] == '' ? '0' : $user['mail_perms']);
    $mail_ext_perms = ($user['mail_ext_perms'] == '' ? '0' : $user['mail_ext_perms']);
    $mail_server_id = ($user['mail_server_id'] == '' ? '0' : $user['mail_server_id']);
    $mail_quota = $user['mail_quota'];
    $query_mail = ",
    userobm_mail_perms='$mail_perms',
    userobm_mail_ext_perms='$mail_ext_perms',
    userobm_mail_server_id='$mail_server_id',
    userobm_mail_quota='$mail_quota'";
  }

  // Section Mail Nomad
  if ($cgp_use['service']['mail_nomad']) {
    $nomade_perms = ($user['nomade_perms'] == '' ? '0' : $user['nomade_perms']);
    $nomade_enable = ($user['nomade_enable'] == '' ? '0' : $user['nomade_enable']);
    $nomade_local_copy = ($user['nomade_local_copy'] == '' ? '0' : $user['nomade_local_copy']);
    $nomad_email = $user['email_nomade'];
    $query_mail_nomad = ",
    userobm_email_nomade='$nomad_email',
    userobm_nomade_perms='$nomade_perms',
    userobm_nomade_enable='$nomade_enable',
    userobm_nomade_local_copy='$nomade_local_copy'";
  }

  // Section Mail Vacation
  if ($cgp_use['service']['mail_vacation']) {
    $vacation_enable = ($user['vacation_enable'] == '' ? '0' : $user['vacation_enable']);
    $vacation_message = $user['vacation_message'];
    $query_mail_vacation = ",
    userobm_vacation_enable='$vacation_enable',
    userobm_vacation_message='$vacation_message'";
  }

  // Section Samba
  if ($cgp_use['service']['samba']) {
    $smb_perms = ($user['smb_perms'] == '' ? '0' : $user['smb_perms']);
    $smb_home = $user['smb_home'];
    $smb_home_drive = $user['smb_home_drive'];
    $smb_logon = $user['smb_logon'];
    $query_samba = ",
    userobm_samba_perms='$smb_perms',
    userobm_samba_home='$smb_home',
    userobm_samba_home_drive='$smb_home_drive',
    userobm_samba_logon_script='$smb_logon'";
  }

  // Gid info
  $userDesc = get_user_info($id);
  $oldGid = $userDesc['gid'];
  if ($perms == "$perms_admin") {
    if ($cgp_use['service']['samba']) {
      $gid = $cg_gid_smb_admin;
    } else {
      $gid = $c_first_gid_admin;
    }
  } else {
    if ($cgp_use['service']['samba']) {
      $gid = $cg_gid_smb_user;
    } else {
      $gid = $c_first_gid_user;
    }
  }
  if ($domain_id == 0) {
    $profile == 'admin';
  }

  $query = "UPDATE UserObm SET
    userobm_timeupdate='". date('Y-m-d H:i:s')."',
    userobm_userupdate='". $obm['uid'] ."',
    $passwd_update
    userobm_perms='$profile',
    userobm_gid='$gid',
    userobm_archive='$archive',
    userobm_datebegin=$datebegin,
    userobm_hidden='$hidden',
    userobm_kind='$kind',
    userobm_lastname='$lname',
    userobm_firstname='$fname',
    userobm_title='$title',
    userobm_phone='$phone',
    userobm_phone2='$phone2',
    userobm_mobile='$mobile',
    userobm_fax='$fax',
    userobm_fax2='$fax2',
    userobm_company = '$company',
    userobm_direction = '$direction',
    userobm_service = '$service',
    userobm_address1 = '$ad1',
    userobm_address2 = '$ad2',
    userobm_address3 = '$ad3',
    userobm_zipcode = '$zip',
    userobm_town = '$town',
    userobm_expresspostal = '$cdx',
    userobm_email='$email',
    userobm_description='$desc'
    $q_delegation_target
    $q_delegation
    $query_web
    $query_mail
    $query_mail_nomad
    $query_mail_vacation
    $query_samba
  WHERE userobm_id='$p_id' $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_user_update()');
  $retour = $obm_q->query($query);

  // If the user is archived, we unsubscribe it from all Groups
  if ($archive == '1') {
    $retour = run_query_user_unsubscribe_all_groups($p_id);    
  }

  // Update photo
  if(isset($user['file_tmp']) && $user['file_tmp'] != '') {
    // Select user photo
    $query = "SELECT userobm_photo_id FROM UserObm WHERE userobm_id='$p_id'";
    $obm_q->query($query);
    $obm_q->next_record();
    if($obm_q->f('userobm_photo_id') == '') {
      // Insert
      $doc_id = run_query_global_insert_document($user, $p_id, 'user');
      if($doc_id > 0) {
        // Update userobm_photo_id
        $query = "UPDATE UserObm SET userobm_photo_id='$doc_id' WHERE userobm_id='$p_id'"; 
        display_debug_msg($query, $cdg_sql, 'run_query_user_update(2)');
        $obm_q->query($query);
      }
    } else {
      // Update a photo
      run_query_global_update_document($user, $photo_id);
    }
  }

  if ($retour) {
    $ret = of_userdata_query_update('user', $p_id, $user);
  }
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// User delete query execution
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_user_delete($p_id) {
  global $cdg_sql, $obm, $c_use_connectors;

  $multidomain = sql_multidomain('userobm');
  $ret = of_userdata_query_delete('user', $p_id);
  $obm_q = new DB_OBM;

  // Remove user's photo (document & documententity)
  $query = "SELECT userobm_photo_id FROM UserObm WHERE userobm_id='$p_id' $multidomain";
  $obm_q->query($query);
  $obm_q->next_record();
  $photo_id = $obm_q->f('userobm_photo_id');
  if($photo_id > 0) {
    run_query_global_delete_document($photo_id);
  }

  // Remove user
  $query = "DELETE FROM UserObm
    WHERE userobm_id='$p_id'
      $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete()');
  $retour = $obm_q->query($query);

  // If this user exists in Prod table, Insert it in Deleted table
  $query = "SELECT userobm_id FROM P_UserObm WHERE userobm_id='$p_id'";
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete(auto)');
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    $uid = $obm['uid'];
    $domain_id = $obm['domain_id'];
    $now = date('Y-m-d H:i:s');

    $query = "INSERT INTO Deleted (
      deleted_domain_id,
      deleted_table,
      deleted_entity_id,
      deleted_user_id,
      deleted_timestamp)
    VALUES (
      '$domain_id',
      'UserObm',
      '$p_id',
      '$uid',
      '$now')";
    display_debug_msg($query, $cdg_sql, 'run_query_user_delete(auto)');
    $retour = $obm_q->query($query);
  }

  // If connectors in use
  if ($c_use_connectors) {
    $now = date('Y-m-d H:i:s');
    $query = "INSERT INTO
      DeletedUser (deleteduser_user_id, deleteduser_timestamp)
      VALUES ('$p_id', '$now')";
    display_debug_msg($query, $cdg_sql, 'run_query_user_delete(connector)');
    $retour = $obm_q->query($query);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// User Groups query execution
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_user_user_group($p_id) {
  global $cdg_sql, $obm;

  $query = "SELECT *
    FROM UGroup
      LEFT JOIN UserObmGroup ON group_id = userobmgroup_group_id
    WHERE userobmgroup_userobm_id='$p_id'
      AND (group_privacy=0 OR group_usercreate='$obm[uid]')";

  display_debug_msg($query, $cdg_sql, 'run_query_user_user_group()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Unsubscribe the user from all groups
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_user_unsubscribe_all_groups($p_id) {
  global $cdg_sql;

  $query = "DELETE
  FROM UserObmGroup
  WHERE userobmgroup_userobm_id = '$p_id'";

  display_debug_msg($query, $cdg_sql, 'run_query_user_unsubscribe_all_groups()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Update internal usergroup mapping
  $query = "DELETE
  FROM of_usergroup
  WHERE of_usergroup_user_id = '$p_id'";
  display_debug_msg($query, $cdg_sql, 'run_query_user_unsubscribe_all_groups()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Groups query execution
///////////////////////////////////////////////////////////////////////////////
function run_query_user_public_owner_group($uid) {
  global $obm, $cdg_sql;

  $query = "SELECT *
    FROM UGroup
    JOIN UserObm ON userobm_id = '$uid' AND group_domain_id = userobm_domain_id
    WHERE
      (group_privacy=0 OR group_usercreate='$obm[uid]')
    ORDER by group_privacy, group_name";

  display_debug_msg($query, $cdg_sql, 'run_query_user_public_owner_group()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : UserObmGroup global update
// Parameters:
//   - $user[] : user hash info : keys used : id, group_nb, group_X
// Return: number of usergroup inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_user_update_user_group($user) {
  global $obm, $cdg_sql, $perms_admin, $cg_gid_smb_admin, $cg_gid_smb_user;

  $u_id = $user['user_id'];

  // $groups tracks the user membership, to optimize internal mapping update
  $groups = array();

  // Get the displayed group list (public and owner)
  $g_q = run_query_user_public_owner_group($u_id);

  // Construct the group list for deletion clause (public or owner groups)
  if ($g_q->num_rows() > 0) {
    $w_g = 'AND (';
    $or = '';
    while ($g_q->next_record()) {
      $g_id = $g_q->f('group_id');
      $groups[$g_id] = 0;
      $w_g .= " $or userobmgroup_group_id='$g_id'";
      $or = 'OR';
    }
    $w_g .= ')';
  }

  // Get current membership and track it in groups
  $m_q = run_query_user_user_group($u_id);
  while ($m_q->next_record()) {
    $g_id = $m_q->f('group_id');
    $groups[$g_id] = 1;
  }

  // Delete the current user membership (from public and owner groups)
  $query = "DELETE FROM UserObmGroup
    WHERE userobmgroup_userobm_id = '$u_id'
      $w_g";
  display_debug_msg($query, $cdg_sql, 'run_query_user_update_user_group()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Renew the selected user membership
  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $user['group_nb']) {
    $cpt++;
    $g_id = $user["group_$cpt"];

    $query = "INSERT INTO UserObmGroup (
        userobmgroup_group_id,
        userobmgroup_userobm_id)
      VALUES (
        $g_id,
        $u_id)";

    display_debug_msg($query, $cdg_sql, 'run_query_user_update_user_group()');
    $retour = $obm_q->query($query);
    if (! $retour) {
      return -1;
    }
    $cpt_ins++;

    // If this group membership is new (just set), update internal mapping
    if ($groups[$g_id] != 1) {
      of_usergroup_update_group_hierarchy($g_id);
    }
    unset ($groups[$g_id]);
  }

  // Finish the internal usergroup mapping update
  // for groups membership that are not renewed (=remaining groups = 1)
  foreach ($groups as $g_id => $value) {
    if ($value != 0) {
      of_usergroup_update_group_hierarchy($g_id);
    }
  }

  // Si l'utilisateur est windows on l'inscrit dans un groupe Win par defaut
  // Si c'est un simple user => Domain users 
  // Si c'est un amdin aliamin => Admins aliamin = Domain admins
  $u = get_user_info($u_id);
  if ($u['samba_perms'] == '1') {
    if ($u['perms'] == $perms_admin) {
      $gid = $cg_gid_smb_admin;
    } else {
      $gid = $cg_gid_smb_user;
    }
    run_query_user_init_group($u_id, $gid, NULL);
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// Return the number of admin user
// Returns:
//   - number of admin user
///////////////////////////////////////////////////////////////////////////////
function get_nb_user_admin() {
  global $cdg_sql, $perms_admin;

  $query = "SELECT count(*) as nb
    FROM UserObm
    WHERE userobm_perms='$perms_admin'";

  display_debug_msg($query, $cdg_sql, 'get_nb_user_admin()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $resultat = $obm_q->f('nb');

  return $resultat;
}


///////////////////////////////////////////////////////////////////////////////
// Check if a login is present in the user table
// Parameters:
//   - $login : Login to test
//   - $u_id  : user Id to ignore (not to test current user in update)
// Returns:
//   - true if found, else false
///////////////////////////////////////////////////////////////////////////////
function check_user_login_in_use($login, $u_id='') {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  if ($u_id != '') {
    $where_uid = "AND userobm_id != '$u_id'";
  }

  $query = "SELECT
      userobm_id,
      userobm_login
    FROM UserObm
    WHERE userobm_login $like '$login'
      $where_uid";

  display_debug_msg($query, $cdg_sql, 'check_user_login_in_use()');
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// User environment checking (same user exists ?)
// Parameters:
//   - $p_id   : user id
//   - $user[] : user's values
// Returns:
//   - User Database object with list of similar users
///////////////////////////////////////////////////////////////////////////////
function check_user_context($p_id, $user) {

  $login = $user['login'];
  $passwd = $user['passwd'];
  $profile = $user['profile'];
  $lname = $user['lastname'];
  $fname = $user['firstname'];
  $phone = $user['phone'];
  $email = $user['email'];
  $domain_id = $user['domain'];

  // if a user with same name exists, return false
  $co_q = run_query_user_check_user($p_id, $user);

  return $co_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return the user with similar names
// Parameters:
//   - $p_id  : user id
//   - $login : login
///////////////////////////////////////////////////////////////////////////////
function run_query_user_check_user($p_id, $user) {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $multidomain = sql_multidomain('userobm');
  $login = $user['login'];
  $domain_id = $user['domain'];
  if ($domain_id == '') $domain_id = $obm['domain_id'];
  $where_multidomain = " AND userobm_domain_id='$domain_id'";

  if ($p_id) {
    $where_user = "AND userobm_id != '$p_id'";
  }

  $query = "SELECT distinct userobm_id, userobm_login
    FROM UserObm
    WHERE userobm_login $like '$login%'
    $where_user $where_multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_user_check_user()');
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// User specific rights checking (eg : delegation)
// Parameters:
//   - $params[] : user values
// Returns:
//   - true if OK, else false
///////////////////////////////////////////////////////////////////////////////
function check_user_update_rights($params) {
  global $err, $cgp_use, $l_error_delegation;

  $id = $params['user_id'];

  if ($id > 0) {

    // Check if delegation is used
    if ($cgp_use['property']['delegation']) {
      $u = get_user_info($id);
      // Check delegation update right
      if (! of_delegation_check_update_rights($params, $u['delegation'])) {
	$err['msg'] .= $l_error_delegation;
	return false;
      }
    }
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Data checking and formatting
// Parameters:
//   - $p_id     : user id
//   - $user[]   : values checked
//     keys used : name, passwd, email
///////////////////////////////////////////////////////////////////////////////
function check_user_data_form($p_id, $user) {
  global $l_exist_error,$l_login_error, $l_login_in_use;
  global $l_password_error, $l_password_empty, $l_profile_privilege_error;
  global $l_lname_error, $l_last_admin_error, $l_image_type_error;
  global $l_image_size_error, $l_email_error, $l_mail_server_error;
  global $l_email_exist, $l_uemail_exist;
  global $l_quota_error, $l_nomade_perms_error, $l_nomade_without_mail_perms;
  global $l_nomade_local_copy_error, $l_err_system_samba,$l_err_web_all;
  global $l_email_empty, $l_mail_perms_error, $l_email_nomad_error;
  global $l_vacation_without_mail_perms, $l_empty_vacation_message;
  global $php_regexp_login;
  global $php_regexp_email, $php_regexp_email_name, $php_regexp_quota;
  global $php_regexp_home, $php_regexp_drive, $l_err_home, $l_err_drive;
  global $php_regexp_password;
  global $err, $action, $cdg_sql, $gp_max_user, $perms_admin;
  global $obm, $profiles, $cgp_use, $c_imagetype;
  global $c_max_image_size;
  global $l_err_delegation_target_outside_domain, $l_err_delegation_target_outside_target;
  global $l_err_delegation_outside_domain, $l_err_delegation_outside_target;
  global $l_err_delegation_target_profile;

  $action = $user['action'];
  $id = $user['user_id'];
  $login = $user['login'];
  $passwd = $user['passwd'];
  $profile = $user['profile'];
  $delegation = stripslashes($user['delegation']);
  $delegation_target = stripslashes($user['delegation_target']);
  $lname = $user['lastname'];
  $fname = $user['firstname'];
  $title = $user['title'];
  $email = $user['email'];
  $phone = $user['phone'];
  $phone2 = $user['phone2'];
  $mobile = $user['mobile'];
  $fax = $user['fax'];
  $fax2 = $user['fax2'];
  $host_id = $user['host_id'];
  $web_perms = $user['web_perms'];
  $web_list = $user['web_list'];
  $web_all = ($user['web_all'] == '1' ? '1' : '0');
  $mail_perms = $user['mail_perms'];
  $mail_ext_perms = $user['mail_ext_perms'];
  $mail_server_id = $user['mail_server_id'];
  $mail_quota = $user['mail_quota'];
  $nomade_perms = $user['nomade_perms'];
  $nomade_enable = $user['nomade_enable'];
  $nomade_local_copy = $user['nomade_local_copy'];
  $vacation_enable = $user['vacation_enable'];
  $vacation_message = stripslashes($user['vacation_message']);
  $email_nomade = $user['email_nomade'];
  $smb_perms = $user['smb_perms'];
  $smb_home = stripslashes($user['smb_home']);
  $smb_home_drive = $user['smb_home_drive'];
  $smb_logon = $user['smb_logon'];
  $file = $user['file_tmp'];
  
  // We can't handle a user with a more privileged profile
  // Or same privilege with 'impair' profile
  $level = $profiles[$obm['profile']]['level'];
  $managepeers = $profiles[$obm['profile']]['level_managepeers'];
  if (($profiles[$profile]['level'] < $level)
      || (($profiles[$profile]['level'] == $level) && (! $managepeers))) {
    $err['msg'] = "$l_profile_privilege_error";
    $err['field'] = 'profile';
    return false;
  }
  
  if ($action == 'update') {
    $u = get_user_info($id);
    $usr_profile = $u['perms'];
    if (($profiles[$usr_profile]['level'] < $level)
        || (($profiles[$usr_profile]['level'] == $level) && (! $managepeers))) {
      $err['msg'] = "$l_profile_privilege_error";
      $err['field'] = 'profile';
      return false;
    }    
  }


  if ($action == 'update') {

    // MANDATORY: we do not allow to update last admin'perms
    if (($usr_profile == $perms_admin) && ($profile != $perms_admin)) {
      $nb_admin = get_nb_user_admin();
      if ($nb_admin < 2) {
	$err['msg'] = "$l_last_admin_error";
	$err['field'] = 'profile';
	return false;
      }
    }
    // MANDATORY: we do not allow to delete samba perms for a system user
    $system = $u['system'];
    $usmb_perms = $u['samba_perms'];
    if (($system) && ($usmb_perms == '1') && ($smb_perms != '1')) {
      $err['msg'] = "$l_err_system_samba";
      $err['field'] = 'smb_perms';
      return false;
    }
  }

  // login (can't be updated, so only on insert)
  if (($action == 'new') || ($action == 'insert')) {
    // MANDATORY: User login, not empty and unique
    if (trim($login) == '') {
      $err['msg'] = "$login : $l_login_error";
      $err['field'] = 'login';
      return false;
    } else {
      if (! preg_match($php_regexp_login, $login)) {
	$err['msg'] .= "$login : $l_login_error";
	$err['field'] = 'login';
	return false;
      }
      // Login not already in use
      if (check_user_login_in_use($login, $id)) {
	$err['msg'] = "$login : $l_login_in_use";
	$err['field'] = 'login';
	return false;
      }
    }
  }

  // MANDATORY: User password on insert
  if (($action == 'insert') && (trim($passwd) == '')) {
    $err['msg'] = $l_password_empty;
    $err['field'] = 'password';
    return false;
  }

  // If password is set, it must be valid
  if ((trim($passwd) != '') && (!preg_match($php_regexp_password, $passwd))) {
    $err['msg'] = $l_password_error;
    $err['field'] = 'password';
    return false;
  }

  // MANDATORY: Lastname
  if (trim($lname) == '') {
    $err['msg'] = $l_lname_error.' : '. $lname;
    $err['field'] = 'lastname';
    return false;
  }

  // If external mail access is granted, internal access should be too
  if ($mail_ext_perms && !($mail_perms)) {
    $err['msg'] = "$l_mail_perms_error";
    $err['field'] = 'mail_ext_perms';
    return false;
  }

  // If no mail server is set
  if( ($mail_perms) && ($mail_server_id == '') ) {
    $err['msg'] = "$l_mail_server_error";
    $err['field'] = 'mail_server';
    //    return false;
  }

  // If mail access is granted, there must be at least one email address
  if ($mail_perms && (trim($email) == '')) {
    $err['msg'] = "$l_email_empty";
    $err['field'] = 'email';
    return false;
  }

  if ($cgp_use['service']['mail']) {
    // User email addresses (only the name before @)
    if ($email != '') {
      $mails = get_email_used($id);
      $um = array();

      // All email addresses given are valid and not already used
      $em = strtok($email, "\r\n");
      while ($em) {
	if (! preg_match($php_regexp_email_name, $em)) {
	  $err['msg'] .= "$em : $l_email_error";
	  $err['field'] = 'email';
	  return false;
	}
	// Email address not already used
	if (array_key_exists($em, $mails)) {
	  $err['msg'] = "$l_email_exist : $em (" . $mails["$em"] . ')';
	  $err['field'] = 'email';
	  return false;
	}
	// Email address not already used by this user
	if (in_array($em, $um)) {
	  $err['msg'] = "$l_uemail_exist : $em";
	  $err['field'] = 'email';
	  return false;
	}
	array_push($um, $em);
	$em = strtok("\r\n");
      }
    }
  } else {
    // OBM-Mail disabled, email must be full (user@domain)
    if (($email != '') && (preg_match($php_regexp_email, $email) == 0)) {
      $err['msg'] = $l_email_error.' : '. $email;
      $err['field'] = 'email';
      return false;
    }
  }

  // Email quota
  if ( (trim($email)) && ($mail_quota != '')
       && (! preg_match($php_regexp_quota, $mail_quota))) {
    $err['msg'] = "$mail_quota : $l_quota_error";
    $err['field'] = 'mail_quota';
    return false;
  }
  
  // Email Nomad perms
  if (($nomade_perms) && (!$mail_perms)) {
    $err['msg'] = $l_nomade_without_mail_perms;
    $err['field'] = 'nomade_perms';
    return false;
  }

  // Email Nomad perms
  if (($nomade_enable) && (!$nomade_perms)) {
    $err['msg'] = $l_nomade_perms_error;
    $err['field'] = 'nomade_perms';
    return false;
  }

  // User nomad email not null if active
  if ( ($email_nomade == '')
       && ($nomade_enable) ) {
    $err['msg'] = "$l_email_nomad_error";
    $err['field'] = 'email_nomade';
    return false;
  }

  // User nomad email
  if ( ($email_nomade != '')
       && (! preg_match($php_regexp_email, $email_nomade))) {
    $err['msg'] = "$email_nomade : $l_email_error";
    $err['field'] = 'email_nomade';
    return false;
  }

  // User nomade local copy only if autorized and activated
  if ( ($nomade_local_copy) && ((! $nomade_perms) || (! $nomade_enable)) )
  {
    $err['msg'] = $l_nomade_local_copy_error;
    $err['field'] = 'nomade_local_copy';
    return false;
  }

  if (($vacation_enable) && (!$mail_perms)) {
    $err['msg'] = $l_vacation_without_mail_perms;
    $err['field'] = 'vacation_enable';
    return false;
  }

  // If vacation is enable, the message must be set
  if ($vacation_enable && !$vacation_message) {
    $err['msg'] = $l_empty_vacation_message;
    $err['field'] = 'vacation_message';
    return false;
  }

  // Samba checks
  if ($cgp_use['service']['samba']) {
    // Samba home correct
    if (($smb_home != '') && (! preg_match($php_regexp_home, $smb_home))) {
      $err['msg'] = " $smb_home : $l_err_home";
      $err['field'] = 'smb_home';
      return false;
    }

    // Samba home drive correct
    if (($smb_home_drive != '') && (! preg_match($php_regexp_drive, $smb_home_drive))) {
      $err['msg'] = " $smb_home_drive : $l_err_drive";
      $err['field'] = 'smb_home_drive';
      return false;
    }
  }

  if ($host_id != '' && $host_id != 0) {
    $host_user = host_cannot_be_used_by_user($host_id,$id);
    // La machine existe, on verifie qu'elle n'est pas utilisee par quelqu'un d'autre
    if ($host_user > 0) {
      global $l_err_host_already_used_by;
      $login = get_user_using_host($host_id);
      $err['msg'] = " $l_err_host_already_used_by $login";
      $err['field'] = 'host_name';
      return false;
    }
  }

  // Gestion d'un poste par utilisateur : Squid local only
  if ($cgp_use['service']['web_local']) {
   // web_all must be 0 or 1
    if (($web_all != '0') && ($web_all != '1')) {
      $err['msg'] = $l_err_web_all;
      return false;
    }
  }

  // Photo
  if ($file != '') {
    // Test image mime-type
    $image_type = exif_imagetype($file);
    if(!$image_type || !array_key_exists($image_type, $c_imagetype)) {
      $err['msg'] = $l_image_type_error;
      $err['field'] = 'file';
      return false;
    }

    // Test image size
    $max_width = $c_max_image_size['width'];
    $max_height = $c_max_image_size['height'];
    $max_size = $c_max_image_size['size'];
    $image_info = getimagesize($file); 
    $width = $image_info['0'];
    $height = $image_info['1'];
    $size  = $user['img_size'];
    if ($width > $max_width || $height > $max_height || $size > $max_size) {
      $err['msg'] = $l_image_size_error;
      $err['field'] = 'file';
      return false;
    } 
  }

  /////////////////////////////////////////////////////////////////////////////
  // if a user with the same name already exists  return false
  /////////////////////////////////////////////////////////////////////////////
  if (($action == 'insert') && (isset($user['domain']))) {
    $domain_id = $user['domain'];
  } else if (($action == 'update') && (isset($u['domain_id']))) {
    $domain_id = $u['domain_id'];
  } else {
    $domain_id = $obm['domain_id'];
  }
  $where_multidomain = "AND (userobm_domain_id = '$domain_id' OR userobm_domain_id = 0)";
  $query = "SELECT * FROM UserObm WHERE userobm_login='$login' $where_multidomain ";
  if ($p_id > 0) {
    $query .= " AND userobm_id!='$p_id'";
  }
  display_debug_msg($query, $cdg_sql, 'check_user_data_form()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  if ($obm_q->num_rows() > 0) {
    $err['msg'] = "$login : $l_exist_error";
    $err['field'] = 'login';
    return false;
  }

  // XXXXXXX verifier ici + fichiers de langue
  $d = of_domain_get_domain_infos($domain_id, true);

  // Les contraintes sur la delegation ne sont pas valables pour le domaine 0
  if ((int)($domain_id) > 0) {

    // Verifier que pour un profil ayant delegation
    if (is_array($profiles[$profile]['properties']['admin_realm'])
        && (in_array('delegation', $profiles[$profile]['properties']['admin_realm']))) {
      // la cible de delegation est bien dans l'arbo. de l'entite
      if (isset($d['delegation']) && ($d['delegation'] != '')) {
	if (strpos($delegation_target, $d['delegation']) !== 0) {
	  $err['msg'] = "$l_err_delegation_target_outside_domain ($d[delegation])";
	  $err['field'] = 'delegation_target';
	  return false;
	}
      }
      // la cible de delegation est dans la cible de delegation de l'admin
      if (isset($obm['delegation']) && ($obm['delegation'] != '')) {
	if (strpos($delegation_target, $obm['delegation']) !== 0) {
	  $err['msg'] = "$l_err_delegation_target_outside_target ($obm[delegation])";
	  $err['field'] = 'delegation_target';
	  return false;
	}
      }
    } else {
      // Sinon la cible de delegation ne doit pas etre remplie !
      if ($delegation_target != '') {
        $err['msg'] = "$l_err_delegation_target_profile ($delegation_target)";
        $err['field'] = 'delegation_target';
        return false;
      }
    }

    // Verifier delegation est dans la delegation du domaine
    if (isset($d['delegation']) && ($d['delegation'] != '')) {
      if (strpos($delegation, $d['delegation']) !== 0) {
	$err['msg'] = "$l_err_delegation_outside_domain ($d[delegation])";
	$err['field'] = 'delegation';
	return false;
      }
    }

    // Verifier delegation est dans la cible de delegation de l'admin
    if (isset($obm['delegation'])
        && ($obm['delegation'] != '')
        && (strpos($delegation, $obm['delegation']) !== 0)) {
      $err['msg'] = "$l_err_delegation_outside_target ($obm[delegation])";
      $err['field'] = 'delegation';
      return false;
    }
  }         

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Return the number of timetask entries referencing to the user (and where
// project is set)
// Parameters:
//   - $p_id : user Id
///////////////////////////////////////////////////////////////////////////////
function get_linked_user_timetask_nb($p_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('userobm');

  $obm_q = new DB_OBM;
  $query = "SELECT count(timetask_id) as nb
    FROM TimeTask
    JOIN UserObm ON timetask_user_id = userobm_id
    WHERE timetask_user_id='$p_id'
      AND timetask_projecttask_id > 0
      $multidomain";

  display_debug_msg($query, $cdg_sql, "get_linked_user_timetask_nb($p_id)");
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f('nb');

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the user can be deleted
// Parameters:
//   - $params[] : user values
// Returns:
//   true if the user can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_user_can_delete($params) {
  global $obm, $profiles, $err, $ok_msg;
  global $l_profile_privilege_error, $l_link_timetask, $l_link_timetask_no;

  $delete_ok = true;

  // Links from Time management (referencing projects)
  $nb = get_linked_user_timetask_nb($params['user_id']);
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= "$l_link_timetask";
  } else {
    $ok_msg .= "$l_link_timetask_no";
  }

  // Check that user has delegation rights for this user
  if (! check_user_update_rights($params)) {
    $delete_ok = false;
  }

  $u = get_user_info($params['user_id']);
  $usr_profile = $u['perms'];

  // We can't delete a user with a more privileged profile
  // Or same privilege with 'impair' profile
  $level = $profiles[$obm['profile']]['level'];
  $managepeers = $profiles[$obm['profile']]['level_managepeers'];
  if (($profiles[$usr_profile]['level'] < $level)
      || (($profiles[$usr_profile]['level'] == $level) && (! $managepeers))) {
    $err['msg'] = "$l_profile_privilege_error";
    $delete_ok = false;
  }

  return $delete_ok;
}


///////////////////////////////////////////////////////////////////////////////
// Query Executions - Purge a deleted user
// Delete all references to the user (UserObmPref, DisplayPref, acl)
// Move all user private contact to public 
// Parameters:
//   - $user_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_user_delete_profile($user_id)  {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  // Unsubscribe from all groups
  $retour = run_query_user_unsubscribe_all_groups($user_id);    

  // Delete all user prefs
  $query = "DELETE FROM UserObmPref WHERE userobmpref_user_id='$user_id'"; 
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete_profile(UserObmPref)');
  $obm_q->query($query);

  $query = "DELETE FROM DisplayPref WHERE display_user_id='$user_id'"; 
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete_profile(DisplayPref)');
  $obm_q->query($query);

  // Move all the user contact from private to public
  $query = "UPDATE Contact SET
      contact_privacy='0'
    WHERE contact_usercreate='$user_id'
      AND contact_privacy != '0'"; 
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete_profile(Contact)');
  $obm_q->query($query);

  // Move all the user deal from private to public
  $query = "UPDATE Deal SET
      deal_privacy='0'
    WHERE deal_usercreate='$user_id'
      AND deal_privacy != '0'"; 
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete_profile(Deal)');
  $obm_q->query($query);

  // Move all the user list from private to public
  $query = "UPDATE List SET
      list_privacy='0'
    WHERE list_usercreate='$user_id'
      AND list_privacy != '0'"; 
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete_profile(List)');
  $obm_q->query($query);

  // Move all the user events from private to public
  // Not activated ! case of private event with multiple users XXXXX to handle
  $query = "UPDATE CalendarEvent SET
      calendarevent_privacy='0'
    WHERE calendarevent_usercreate='$user_id'
      AND calendarevent_privacy != '0'"; 
  //display_debug_msg($query, $cdg_sql);
  //$obm_q->query($query);

  // Remove all User event participation
  $query = "DELETE FROM EventEntity
    WHERE evententity_entity = 'user'
      AND evententity_entity_id = '$user_id'"; 
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete_profile(EventEntity)');
  $obm_q->query($query);

  // Remove all acl to the user and user's one
  $query = "DELETE FROM EntityRight
    WHERE entityright_entity = 'calendar'
      AND entityright_entity_id = '$user_id'"; 
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete_profile(EntityRight)');
  $obm_q->query($query);
  $query = "DELETE FROM EntityRight
    WHERE entityright_consumer = 'user'
      AND entityright_consumer_id = '$user_id'"; 
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete_profile(EntityRight)');
  $obm_q->query($query);

  // Remove all Project participation
  $query = "DELETE FROM ProjectUser
    WHERE projectuser_user_id = '$user_id'"; 
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete_profile(ProjectUser)');
  $obm_q->query($query);

  // Remove all User Time management data
  // This is dangerous, but forbidden if user has timetask data (check_can..)
  $query = "DELETE FROM TimeTask
    WHERE timetask_user_id = '$user_id'"; 
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete_profile(TimeTask)');
  $obm_q->query($query);

}


///////////////////////////////////////////////////////////////////////////////
// Query Executions - Insert UserObm's photo
// Insert user's photo in document module
// Write photo on disk
// Insert links between user and document
// Parameters:
//   - $user[] : user's value
//   - $user_id : user's id
///////////////////////////////////////////////////////////////////////////////
function run_query_user_insert_photo($user, $user_id) {
  global $cdg_sql, $obm;

  $uid = $obm['uid'];
  $now = date('Y-m-d H:i:s');
  $domain_id = $obm['domain_id'];

  $title = '';
  $author = get_last_user_text($uid);
  $mime = '';
  $name = $user['name'];
  $path = '';
  $size = $user['size'];
  $kind = '';
  $url = '';
  $privacy = '0';
  $entity = 'user';
  $entity_id = $user_id;
  
  $query = "INSERT INTO Document (
    document_timeupdate,
    document_timecreate,
    document_userupdate,
    document_usercreate,
    document_domain_id,
    document_title,
    document_author,
    document_name,
    document_path,
    document_kind,
    document_size,
    document_mimetype_id,
    document_privacy
  ) VALUES (
    null,
    '$now',
    null,
    '$uid',
    '$domain_id',
    '$title',
    '$author',
    '$name',
    '$path',
    '$kind',
    '$size',
    '$mime',
    '$privacy')";

  display_debug_msg($query, $cdg_sql, 'run_query_user_insert_photo(1)');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  $doc_id_q = "SELECT document_id
    FROM Document
    WHERE 
      document_title='$title'
      AND document_author='$author'
      AND document_name='$name'
      AND document_size='$size'";  

  display_debug_msg($query, $cdg_sql, 'run_query_user_insert_photo(2)');
  $obm_q = new DB_OBM;
  $obm_q->query($doc_id_q);
  
  // If the document is correctly inserted in database
  if ($obm_q->num_rows() > 0) {
    $obm_q->next_record();
    $doc_id = $obm_q->f('document_id');

    // Create document on disk
    if (! run_query_document_create_file($document)) {
      $err_msg = $l_create_file_error;
      // If error, we delete The document infos to keep bd and rep on sync
      run_query_document_delete($doc_id);
      return false;
    }
  }   

}

?>
