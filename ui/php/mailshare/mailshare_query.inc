<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



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

  $sql_order_dir = $mailshare['sql_order_dir'];
  $sql_order_field = $mailshare['sql_order_field'];

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
    $where .= " AND mailshare_name $like '".$obm_q->escape($name)."%'";
  }
  // If a delegation indication has been specified, get it
  if (trim($delegation) != '') {
    $where .= " AND mailshare_delegation $like '".$obm_q->escape($delegation)."%'";
  }
  if ($email != '') {
    $where .= " AND mailshare_email $like '%".$obm_q->escape($email)."%'";
  }
  if ($quota > 0) {
    $where .= " AND mailshare_quota ='".$obm_q->escape($quota)."'";
  }
  if ($desc != '') {
    $where .= " AND mailshare_description $like '%".$obm_q->escape($desc)."%'";
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


function run_query_mailshare_ext_search($mailshare) {

  $pattern = $mailshare['pattern'];
  $mailshare = array();
  $mailshare['name'] = $pattern;
  $obm_q = run_query_mailshare_search($mailshare);

  $rows =  array();
  while($obm_q->next_record()) {
    $rows[$obm_q->f("mailshare_name")." ".$obm_q->f("mailshare_description")." ".$obm_q->f('mailshare_id')] = $obm_q->Record;
  }
  $resultsCount = count($rows);
  ksort($rows);
  return array('length' => $resultsCount, 'datas' => $rows);
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
      host_ip as mailserver_ip,
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

  $obm_q = new DB_OBM;
  $name = strtolower($mailshare['name']);
  $quota = $mailshare['quota'];
  $email = $mailshare['email'];
  $desc = $mailshare['desc'];
  $mail_server_id = sql_parse_id($mailshare['mail_server_id']);
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
    NOW(),
    null,
    $obm[uid],
    '".$obm_q->escape($name)."'
    $q_delegation[value],
    $mail_server_id,
    '".$obm_q->escape($quota)."',
    '".$obm_q->escape($desc)."',
    '".$obm_q->escape($email)."')";

  display_debug_msg($query, $cdg_sql, 'run_query_mailshare_insert(1)');
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

  $obm_q = new DB_OBM;
  $id = sql_parse_id($mailshare['mailshare_id'], true);
  $name = strtolower($mailshare['name']);
  $quota = $mailshare['quota'];
  $email = $mailshare['email'];
  $desc = $mailshare['desc'];
  $mail_server_id = sql_parse_id($mailshare['mail_server_id']);
  $multidomain = sql_multidomain('mailshare');

  // Delegation handling
  $q_delegation = of_delegation_query_update_clause('mailshare_delegation', stripslashes($mailshare['delegation']));
  
  $query = "UPDATE MailShare set
    mailshare_timeupdate = NOW(),
    mailshare_userupdate = $obm[uid],
    mailshare_name = '".$obm_q->escape($name)."',
    mailshare_quota = '".$obm_q->escape($quota)."',
    mailshare_mail_server_id = $mail_server_id,
    mailshare_description = '".$obm_q->escape($desc)."',
    mailshare_email = '".$obm_q->escape($email)."'
    $q_delegation
  WHERE mailshare_id $id 
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_mailshare_update()');
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
  $query = "SELECT mailshare_id, mailshare_delegation,mailshare_domain_id FROM P_MailShare WHERE mailshare_id $id"; 
  display_debug_msg($query, $cdg_sql, 'run_query_mailshare_delete(auto)');
  $obm_q->query($query);
  $obm_q->next_record();
  $delegation = $obm_q->f('mailshare_delegation');
  $domain_id = $obm_q->f('mailshare_domain_id');

  if ($obm_q->num_rows() > 0) {
    $uid = sql_parse_id($obm['uid']);
    $domain_id = sql_parse_id($domain_id);
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
      '".$obm_q->escape($delegation)."',
      'MailShare',
      $p_id,
      NOW())";
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
    $wname = "mailshare_name $like '%".$obm_q->escape($name)."%'";
  } else {
    $wname = "mailshare_name = '".$obm_q->escape($name)."'";
  }

  // If desc is short, we test equality, else similarity
  if (trim($desc) != '') {
    if (strlen($desc) > 2) {
      $wdesc = "mailshare_description $like '%".$obm_q->escape($desc)."%'";
    } else {
      $wdesc = "mailshare_description = '".$obm_q->escape($desc)."'";
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
  global $l_err_mailshare_quota_max, $l_max_quota, $obm;

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

  // Get default quota on domain
  $prop_q = run_query_domain_properties($obm['domain_id']);
  while($prop_q->next_record()){
    $name = $prop_q->f('domainproperty_key');
    $domain_value = $prop_q->f('domainpropertyvalue_value');
    $domain[$name] = $domain_value;
  }

  $max_quota = $domain['mailshares_quota_max'];
  if (($max_quota < $quota && $max_quota != 0) 
    || ($quota < $max_quota && $quota == 0)) {
    $err['msg'] = "$l_err_mailshare_quota_max ($l_max_quota : $max_quota)";
    $err['field'] = 'quota';
    return false;
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


/**
 * Check that mailshare hasn't pending changes since last automate update
 */
function check_mailshare_can_backup($mailshare_id) {

  $id = sql_parse_id($mailshare_id, true);
  $query = "SELECT m.mailshare_id
    FROM P_MailShare p
    INNER JOIN MailShare m ON p.mailshare_id=m.mailshare_id AND p.mailshare_email=m.mailshare_email
    WHERE m.mailshare_id $id";
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return ($obm_q->num_rows()==1);
}

