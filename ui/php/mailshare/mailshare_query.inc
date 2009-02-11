<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailshare_query.inc                                          //
//     - Desc : mailshare query File                                         //
// 2007-02-02 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Mailshare Search query execution 
// Parameters :
//   - $mailshare[] : mailshare search criteria
//     keys used    : name
///////////////////////////////////////////////////////////////////////////////
function run_query_mailshare_search($mailshare) {
  global $cdg_sql, $ctu_sql_limit;

  $sql_order_dir = $user['sql_order_dir'];
  $sql_order_field = $user['sql_order_field'];

  $name = sql_search_text_parse($mailshare['name']);
  $delegation = sql_search_text_parse($mailshare['delegation']);
  $email = sql_search_text_parse($mailshare['email']);
  $quota = sql_search_text_parse($mailshare['quota']);
  $desc = sql_search_text_parse($mailshare['desc']);

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $timeupdate = sql_date_format($db_type,'mailshare_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type,'mailshare_timecreate', 'timecreate');
  $multidomain = sql_multidomain('mailshare');

  $where = '(1=1)';
  if ($name != '') {
    $where .= " AND mailshare_name $like '$name%'";
  }
  // If a delegation indication has been specified, get it
  if (trim($delegation) != '') {
    $where .= " AND mailshare_delegation $like '$delegation%'";
  }
  if ($email != '') {
    $where .= " AND mailshare_email $like '%$email%'";
  }
  if ($quota > 0) {
    $where .= " AND mailshare_quota ='$quota'";
  }
  if ($desc != '') {
    $where .= " AND mailshare_description $like '%$desc%'";
  }
  $whereq = "WHERE $where";

  // ORDER construction
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'mailshare_name';
  $orderq .= " ORDER BY $order $sql_order_dir";

  $query = "SELECT DISTINCT MailShare.*,
      mailshare_id as id,
      $timecreate,
      $timeupdate,
      A.userobm_login as usercreate,
      B.userobm_login as userupdate
    FROM MailShare
      LEFT JOIN UserObm as A ON mailshare_usercreate=A.userobm_id
      LEFT JOIN UserObm as B ON mailshare_userupdate=B.userobm_id
    $whereq $multidomain
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM MailShare $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'run_query_mailshare_search()');
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// MailShare detail query execution
// Parameters:
//   - $id : mailshare id
///////////////////////////////////////////////////////////////////////////////
function run_query_mailshare_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type,'mailshare_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type,'mailshare_timecreate', 'timecreate');
  $multidomain = sql_multidomain('mailshare');
  $id = sql_parse_id($id, true);
  $query = "SELECT *,
      host_name as mailserver_hostname,
      $timecreate,
      $timeupdate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM MailShare
         LEFT JOIN Host ON mailshare_mail_server_id = host_id
         LEFT JOIN UserObm as c ON mailshare_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON mailshare_userupdate=u.userobm_id
    WHERE mailshare_id $id 
      $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_mailshare_detail()');
  $obm_q->query($query) ;
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : mailshare insertion
// Parameters:
//   - $mailshare[] : mailshare hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_mailshare_insert($mailshare) {
  global $cdg_sql, $obm;

  $name = strtolower($mailshare['name']);
  $quota = $mailshare['quota'];
  $email = $mailshare['email'];
  $desc = $mailshare['desc'];
  $mail_server_id = sql_parse_id($mailshare['mail_server_id']);
  $now = date('Y-m-d H:i:s');
  $domain_id = sql_parse_id($obm['domain_id']);
  $uid = sql_parse_id($obm['uid'], true);
  // Delegation handling
  $q_delegation = of_delegation_query_insert_clauses('mailshare_delegation', stripslashes($mailshare['delegation']));

  $query = "INSERT INTO MailShare (
    mailshare_domain_id,
    mailshare_timeupdate,
    mailshare_timecreate,
    mailshare_userupdate,
    mailshare_usercreate,
    mailshare_name
    $q_delegation[field],
    mailshare_mail_server_id,
    mailshare_quota,
    mailshare_description,
    mailshare_email)
  VALUES (
    $domain_id,
    null,
    '$now',
    null,
    $obm[uid],
    '$name'
    $q_delegation[value],
    $mail_server_id,
    '$quota',
    '$desc',
    '$email')";

  display_debug_msg($query, $cdg_sql, 'run_query_mailshare_insert(1)');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    $id = $obm_q->lastid();
    $entity_id = of_entity_insert($GLOBALS['module'], $id);    
    OBM_Acl::setDefaultPublicRights('mailshare', $id);
    $default_rights = OBM_Acl_Utils::parseRightsParams($mailshare);
    foreach ($default_rights['user'] as $user_id => $rights) {
      OBM_Acl::setRights($user_id, 'mailshare', $id, $rights);
    }
    return $id;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Mailshare Update query execution
// Parameters:
//   - $mailshare[] : mailshare hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_mailshare_update($mailshare) {
  global $obm, $cdg_sql;

  $id = sql_parse_id($mailshare['mailshare_id'], true);
  $name = strtolower($mailshare['name']);
  $quota = $mailshare['quota'];
  $email = $mailshare['email'];
  $desc = $mailshare['desc'];
  $mail_server_id = sql_parse_id($mailshare['mail_server_id']);
  $now = date('Y-m-d H:i:s');
  $multidomain = sql_multidomain('mailshare');

  // Delegation handling
  $q_delegation = of_delegation_query_update_clause('mailshare_delegation', stripslashes($mailshare['delegation']));
  
  $query = "UPDATE MailShare set
    mailshare_timeupdate = '$now',
    mailshare_userupdate = $obm[uid],
    mailshare_name = '$name',
    mailshare_quota = '$quota',
    mailshare_mail_server_id = $mail_server_id,
    mailshare_description = '$desc',
    mailshare_email = '$email'
    $q_delegation
  WHERE mailshare_id $id 
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_mailshare_update()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : mailshare id
///////////////////////////////////////////////////////////////////////////////
function run_query_mailshare_delete($p_id) {
  global $cdg_sql, $obm;

  of_entity_delete($GLOBALS['module'], $p_id);
  $multidomain = sql_multidomain('mailshare');
  $id = sql_parse_id($p_id, true);
  $obm_q = new DB_OBM;

  // Delete the Mailshare
  $query = "DELETE FROM MailShare
    WHERE mailshare_id $id 
      $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_mailshare_delete(2)');
  $retour = $obm_q->query($query);

  // If this mailshare exists in Prod table, Insert it in Deleted table
  $query = "SELECT mailshare_id, mailshare_delegation FROM P_MailShare WHERE mailshare_id $id"; 
  display_debug_msg($query, $cdg_sql, 'run_query_mailshare_delete(auto)');
  $obm_q->query($query);
  $obm_q->next_record();
  $delegation = $obm_q->f('mailshare_delegation');

  if ($obm_q->num_rows() > 0) {
    $uid = sql_parse_id($obm['uid']);
    $domain_id = sql_parse_id($obm['domain_id']);
    $now = date('Y-m-d H:i:s');
    $p_id = sql_parse_id($p_id); 
    $query = "INSERT INTO Deleted (
      deleted_domain_id,
      deleted_user_id,
      deleted_delegation,
      deleted_table,
      deleted_entity_id,
      deleted_timestamp)
    VALUES (
      $domain_id,
      $uid,
      '$delegation',
      'MailShare',
      $p_id,
      '$now')";
    display_debug_msg($query, $cdg_sql, 'run_query_mailshare_delete(auto)');
    $retour = $obm_q->query($query);
  }

  return $retour;
}


/**
 * get_mailshare_infos
 * Get one mailshare infos as an array
 *
 * @param mixed $id mailshare id
 * @return mixed infos array
 */
function get_mailshare_info($id='') {
  global $cdg_sql;

  $multidomain = sql_multidomain('mailshare');
  $obm_q = new DB_OBM ;
  $db_type = $obm_q->type;
  $id = sql_parse_id($id, true);
  $query = "SELECT *
    FROM MailShare
    WHERE mailshare_id $id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_mailshare_infos()');
  $obm_q->query($query);
  $obm_q->next_record();
  if (is_array($obm_q->Record)) {
    $m['name'] = $obm_q->f('mailshare_name');
    $m['delegation'] = $obm_q->f('mailshare_delegation');
    $m['quota'] = $obm_q->f('mailshare_quota');
    $m['email'] = $obm_q->f('mailshare_email');
  }  

  return $m;
}


///////////////////////////////////////////////////////////////////////////////
// Check if Mailshare (except with id given) with the name given already exists
// Parameters:
//   - $name : name to search for
//   - $id   : mailshare id to exclude
// Returns:
//   - true (if a mailshare exists) or false
///////////////////////////////////////////////////////////////////////////////
function get_mailshare_name_exists($name, $id='') {
  global $cdg_sql;

  $multidomain = sql_multidomain('mailshare');

  if ($id != '') {
    $id = sql_parse_id($id, true, true);
    $where_id = "AND mailshare_id $id ";
  }

  $query = "SELECT mailshare_id, mailshare_name
    FROM MailShare
    WHERE
      mailshare_name = '$name'
      $where_id
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_mailshare_name_exists()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Return the mailshares which matches the name or the desc
// except the one given (update mode)
// Parameters:
//   - $id   : mailshare id
//   - $name : mailshare name
//   - $desc : mailshare desc
///////////////////////////////////////////////////////////////////////////////
function run_query_check_mailshare($id, $name, $desc) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $multidomain = sql_multidomain('mailshare');

  if ($id != '') {
    $id = sql_parse_id($id, true, true);
    $where_id = "AND mailshare_id $id ";
  }

  // If name is short, we test equality, else similarity
  if (strlen($name) > 2) {
    $wname = "mailshare_name $like '%$name%'";
  } else {
    $wname = "mailshare_name = '$name'";
  }

  // If desc is short, we test equality, else similarity
  if (trim($desc) != '') {
    if (strlen($desc) > 2) {
      $wdesc = "mailshare_description $like '%$desc%'";
    } else {
      $wdesc = "mailshare_description = '$desc'";
    }
  }
  if ($wdesc != '') $wdesc = "OR $wdesc";

  $query = "SELECT DISTINCT mailshare_id,
      mailshare_name,
      mailshare_description
    FROM MailShare
    WHERE
      ($wname $wdesc)
      $where_id
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_check_mailshare()');
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Mailshare context checking (same mailshare exists ?)
// Parameters:
//   - $id          : mailshare id
//   - $mailshare[] : mailshare values
// Returns:
//   - Mailshare Database object with mailshare of similar mailshares
///////////////////////////////////////////////////////////////////////////////
function check_mailshare_context($id, $mailshare) {
  global $cdg_sql;

  $name = strtolower($mailshare['name']);
  $desc = $mailshare['desc'];

  // return the mailshares with same name or desc
  $m_q = run_query_check_mailshare($id, $name, $desc);

  return $m_q;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the mailshare can be deleted
// Parameters:
//   - $p_id : mailshare id
// Returns:
//   true if the mailshare can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_can_delete_mailshare($p_id) {
  global $ok_msg;

  $delete_ok = true;
  // Check that user has delegation rights for this user
  if (! check_mailshare_update_rights(array('mailshare_id' => $p_id))) {
    $delete_ok = false;
  }
  return $delete_ok;
}


/**
 * check_mailshare_update_rights
 * Mailshare specific rights checking (eg: delegation)
 *
 * @param array $params paramaters hash
 * @return bool true or false
 */
function check_mailshare_update_rights($params) {
  global $err, $cgp_use, $l_error_delegation;

  $id = $params['mailshare_id'];

  if ($id > 0) {

    // Check if delegation is used
    if ($cgp_use['property']['delegation']) {
      $m = get_mailshare_info($id);
      // Check delegation update right
      if (! of_delegation_check_update_rights($params, $m['delegation'])) {
	$err['msg'] .= $l_error_delegation;
	return false;
      }
    }
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Mailshare Form Data checking and formatting
// Parameters:
//   - $mailshare[] : values checked
///////////////////////////////////////////////////////////////////////////////
function check_mailshare_data_form($mailshare) {
  global $err, $l_err_mailshare_name_empty, $l_err_mailshare_name_exist;
  global $php_regexp_login, $l_name_error, $l_name_in_use;
  global $php_regexp_email_name, $php_regexp_quota, $l_err_mailshare_email;
  global $l_err_mailshare_email_empty, $l_err_mailshare_email_exist,$cgp_use;
  global $l_err_mailshare_uemail_exist, $l_err_mailshare_quota,$php_regexp_email;

  $id = $mailshare['mailshare_id'];
  $name = strtolower($mailshare['name']);
  $quota = $mailshare['quota'];
  $email = stripslashes($mailshare['email']);
  $desc = $mailshare['desc'];

  // MANDATORY: Mailshare name not empty
  if (trim($name) == '') {
    $err['msg'] = $l_err_mailshare_name_empty;
    $err['field'] = 'name';
    return false;
  } else {
    if (! preg_match($php_regexp_login, $name)) {
      $err['msg'] .= "$name : $l_name_error";
      $err['field'] = 'name';
      return false;
    }
    // Name not already in use
    if (name_in_use($name, $id)) {
      $err['msg'] = "$name : $l_name_in_use";
      $err['field'] = 'name';
      return false;
    }
  }

  // MANDATORY: Host name unique
  if (get_mailshare_name_exists($name, $id)) {
    $err['msg'] = "$l_err_mailshare_name_exist ($name)";
    $err['field'] = 'name';
    return false;
  }

  // Les contraintes sur la delegation ne sont pas valables pour le domaine global
  if ($cgp_use['property']['delegation']) {
    if(!of_delegation_check_data($mailshare['delegation'])) {
      $err['field'] = 'delegation';
      return false;
    }
  } 

  // If quota set, must be numerical
  if ($quota != '') {
    if (! preg_match($php_regexp_quota, $quota)) {
      $err['msg'] = " $quota : $l_err_mailshare_quota";
      $err['field'] = 'quota';
      return false;
    }
  }

  // MANDATORY: Email filled
  if (trim($email) == '') {
    $err['msg'] = "$l_err_mailshare_email_empty";
    $err['field'] = 'email';
    return false;
  }

  // User email addresses (only the name before @)
  if ($email != '') {
    $um = array();

    // All email addresses given are valid and not already used
    $em = strtok($email, "\r\n");
    while ($em) {
      if (! preg_match($php_regexp_email_name, $em) && !preg_match($php_regexp_email, $em)) {
        $err['msg'] .= "$em : $l_err_mailshare_email";
	$err['field'] = 'email';
        return false;
      }
      // Email address not already used by this user
      if (in_array($em, $um)) {
	$err['msg'] = "$l_err_mailshare_uemail_exist : $em";
	$err['field'] = 'email';
        return false;
      }
      $emails[] = $em;
      array_push($um, $em);
      $em = strtok("\r\n");
    }
    $mails = get_email_used($emails, '', '', $id);
    if(!empty($mails)) {
      $err['msg'] = "$l_err_mailshare_email_exist : $mails ";
      $err['field'] = 'email';
      return false;
    }    
  }
 
  return true;
}

///////////////////////////////////////////////////////////////////////////////
// Check if a login is present in the user table
// Parameters:
//   - $login : Login to test
//   - $u_id  : user Id to ignore (not to test current user in update)
// Returns:
//   - true if found, else false
///////////////////////////////////////////////////////////////////////////////
function name_in_use($name, $u_id='') {
  global $cdg_sql;

  $multidomain = sql_multidomain('mailshare');
  $u_id = sql_parse_id($u_id, true, true);
  $query = "SELECT
    mailshare_id,
    mailshare_name
  FROM MailShare
  WHERE mailshare_name = '$name'
    AND mailshare_id $u_id 
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'name_in_use()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}

function update_mailshare_acl($name, $domain_id) {
    global $cmd_update_bal_acl;
    global $params;

    $obm_q = run_query_mailshare_detail($params['mailshare_id']);

    $cmd = $cmd_update_bal_acl." --type mailshare --name '".$obm_q->f('mailshare_name')."' --domain ".$domain_id;

    display_debug_msg($cmd, $cdg_exe, "update_mailshare_acl()");
    exec($cmd);

    return true;
}

</script>
