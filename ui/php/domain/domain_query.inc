<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : domain_query.inc                                             //
//     - Desc : domain query & db File                                       //
// 2000-05-17 Aliacom - David Phan                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// domain query search query execution
// Parameters:
//   - $domain[] : domain search criteria
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_search($domain, $restriction='') {
  global $c_all, $cdg_sql, $ctu_sql_limit;

  $sql_order_dir = $domain['sql_order_dir'];
  $sql_order_field = $domain['sql_order_field'];
  $text = sql_search_text_parse($incident['text']);

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);

  $where = '';

  // If a login indication has been specified, get it
  if (trim($text) != '') {
    $where .= " domain_label $like '$text%'
      OR domain_description like '$text%'";
  }

  if ((trim($where) != '') || (trim($where_restriction) != '')) {
    $whereq = "AND $where $where_restriction";
  }

  $select = "SELECT distinct Domain.*,
    domain_id as id
    FROM Domain WHERE domain_global = false $whereq";


  // ORDER construction
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'domain_label';
  $orderq .= " ORDER BY $order $sql_order_dir";

  $query = "$select
    $whereq
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(distinct domain_id) FROM Domain $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'run_query_domain_search()');
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// domain detail query execution
// Parameters:
//   - $p_id : domain id
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_detail($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $timeupdate = sql_date_format($db_type, 'domain_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'domain_timecreate', 'timecreate');
  $p_id = sql_parse_id($p_id, true);
  $query = "SELECT Domain.*,
    $timeupdate,
    $timecreate,
    host_name as mailserver_hostname,
    c.userobm_login as usercreate,
    u.userobm_login as userupdate
    FROM Domain
    LEFT JOIN MailServer ON mailserver_id = domain_mail_server_id
    LEFT JOIN Host ON host_id = mailserver_host_id
    LEFT JOIN UserObm as c ON domain_usercreate=c.userobm_id
    LEFT JOIN UserObm as u ON domain_userupdate=u.userobm_id
    WHERE domain_id $p_id ";

  display_debug_msg($query, $cdg_sql, 'run_query_domain_detail()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// domain insert query execution
// Parameters:
//   - $domain[]   : entry values
// Returns:
//   $id : new domain id if ok, else false
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_insert($domain) {
  global $obm, $cdg_sql;

  $uid = sql_parse_id($obm['uid']);
  $now = date('Y-m-d H:i:s');
  $label = $domain['label'];
  $desc = $domain['desc'];
  $domain_name = $domain['domain_name'];
  $alias = $domain['alias'];
  $mail_server_id = sql_parse_id($domain['mail_server_id']);
  $query = "INSERT INTO Domain (
    domain_timeupdate,
    domain_timecreate,
    domain_userupdate,
    domain_usercreate,
    domain_label,
    domain_description,
    domain_name,
    domain_mail_server_id,
    domain_alias)
    VALUES (
      null,
      '$now',
      null,
      $uid,
      '$label',
      '$desc',
      '$domain_name',
      $mail_server_id,
      '$alias')";
  display_debug_msg($query, $cdg_sql, 'run_query_domain_insert(1)');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    // Get the domain id
    $query = "SELECT domain_id as id
      FROM Domain
      WHERE domain_label = '$label'";

    display_debug_msg($query, $cdg_sql, 'run_query_domain_detail(2)');

    $obm_q->query($query);
    $obm_q->next_record();
    $id = $obm_q->f('id');
    run_query_domain_replace_properties($id,$domain);
  } else {
    $id = false;
  }

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// domain insert query execution
// Parameters:
//   - $domain[]   : entry values
// Returns:
//   $id : new domain id if ok, else false
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_replace_properties($domain_id,$domain) {
  global $cdg_sql;
  $domain_id = sql_parse_id($domain_id, true);
  $query = "DELETE FROM DomainPropertyValue WHERE domainpropertyvalue_domain_id $domain_id ";
  display_debug_msg($query, $cdg_sql, 'run_query_domain_replace_properties()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  $prop_q = run_query_domain_properties();
  while($prop_q->next_record()) {
    $key = $prop_q->f('domainproperty_key');
    $value = $domain[$key];

    if ($value == '') {
      $value = $prop_q->f('domainproperty_default');
    }

    if ($value != '') {
      $query = "INSERT INTO DomainPropertyValue (
        domainpropertyvalue_domain_id,
        domainpropertyvalue_property_key,
        domainpropertyvalue_value)
        VALUES (
          $domain_id,
          '$key',
          '$value'
        )";
      $obm_q->query($query);
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// domain update query execution
// Parameters:
//   - $p_id     : domain id
//   - $domain[]   : entry values
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_update($d_id, $domain) {
  global $obm, $cdg_sql;

  $uid = sql_parse_id($obm['uid']);
  $now = date('Y-m-d H:i:s');
  $label = $domain['label'];
  $desc = $domain['desc'];
  $alias = $domain['alias'];
  $mail_server_id = sql_parse_id($domain['mail_server_id']);
  $d_id = sql_parse_id($d_id, true);
  $query = "UPDATE Domain SET
    domain_timeupdate='$now',
    domain_userupdate=$uid,
    domain_label='$label',
    domain_description='$desc',
    domain_mail_server_id=$mail_server_id,
    domain_alias='$alias'
    WHERE domain_id $d_id ";

  display_debug_msg($query, $cdg_sql, 'run_query_domain_update()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  run_query_domain_replace_properties($d_id,$domain);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// domain delete query execution
// Parameters:
//   - $p_id : domain id
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_delete($p_id) {
  global $cdg_sql, $c_use_connectors;

  $obm_q = new DB_OBM;
  $p_id = sql_parse_id($p_id, true);
  $query = "DELETE FROM Domain WHERE domain_id $p_id ";
  display_debug_msg($query, $cdg_sql, 'run_query_domain_delete()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data checking and formatting
// Parameters:
//   - $d_id     : domain id
//   - $user[]   : values checked
//     keys used : name, passwd, email
///////////////////////////////////////////////////////////////////////////////
function check_domain_data_form($d_id, $domain) {
  global $err, $l_label_error,$l_alias_user_used,$l_name_in_use;

  $label = $domain['label'];
  $alias = trim($domain['alias']);
  $name = trim($domain['domain_name']);
  // MANDATORY: Label
  if (trim($label) == '') {
    $err['msg'] = $l_label_error.' : '. $label;
    return false;
  }

  $used_name = run_query_domain_get_used_name($alias, $name, $d_id);
  if(count($used_name) > 0) {
    $err['msg'] = "$l_name_in_use : ".implode(', ',$used_name);
    return false;
  }
  if ($d_id && !empty($alias)) {
    $users_aliases = run_query_domain_get_users_aliases($alias, $d_id);
    if(count($users_aliases) > 0) {
      $err['msg'] = "$l_alias_user_used : ".implode(', ',$users_aliases);
      return false;
    }
  } 
  return true;
}
/**
 * Check if aliases or domain name  are already in use
 * 
 * @param mixed $aliases 
 * @param mixed $id 
 * @access public
 * @return void
 */
function run_query_domain_get_used_name($alias, $name=null, $id=null) {
  global $cdg_sql;
  $return = array();
  $aliases = array();
  if(!empty($alias)) {
    $aliases = explode("\r\n",$alias);
  }
  if(!empty($id)) {
    $d = of_domain_get_domain_infos($id);
    array_push($aliases,$d['name']);
    $id = sql_parse_id($id, true, true);
    $dquery = " AND domain_id $id "; 
  } elseif(!empty($name)) {
    array_push($aliases,$name);    
  }
  if(count($aliases) > 0) {
    foreach($aliases as $alias) {
      $alias_query[] = "domain_alias LIKE '%$alias%'";
      $domain_query [] = "domain_name  = '$alias'";
    }
    $query = "SELECT domain_alias as name FROM Domain WHERE (".implode(' OR ', $alias_query).") $dquery
      UNION
      SELECT domain_name as name FROM Domain WHERE (".implode(' OR ', $domain_query).") $dquery";
    $obm_q = new DB_OBM;
    display_debug_msg($query, $cdg_sql, 'get_email_used(1)');
    $obm_q->query($query);
    while($obm_q->next_record()) {
      preg_match_all('/^('.implode('|',$aliases).')\r?$/m',$obm_q->f('name'),$results);
      $return = array_merge($return, $results[1]);      
    }
  }
  return $return;
}

/**
 * Check aliases integrity 
 * 
 * @param mixed $aliases 
 * @param mixed $id 
 * @access public
 * @return void
 */
function run_query_domain_get_users_aliases($aliases, $id) {
  global $cdg_sql;
  $d = of_domain_get_domain_infos($id);
  $return = array();
  if(!empty($d['alias'])) {
    $old_aliases = explode("\r\n",$d['alias']);
  } else {
    return array();
  }
  if(!empty($aliases)) {
    $aliases = explode("\r\n",$aliases);
    $aliases = array_diff($old_aliases,$aliases);
  } else {
    $aliases = $old_aliases;
  }
  if(count($aliases) > 0) {
    foreach($aliases as $alias) {
      $user_query[] = "userobm_email LIKE '%@$alias%'";
      $mailshare_query[] = "mailshare_email LIKE '%@$alias%'";
      $group_query[] = "group_email LIKE '%@$alias'";
      $regexp[] = "($alias)";
    }
    $id = sql_parse_id($id, true);
    $where_user .= " AND userobm_domain_id $id"; 
    $where_group .= " AND group_domain_id $id"; 
    $where_mailshare .= " AND mailshare_domain_id $id"; 

    $query = "SELECT userobm_email as mail FROM UserObm WHERE (".implode(' OR ', $user_query).") $where_user
      UNION
      SELECT mailshare_email as mail FROM MailShare WHERE (".implode(' OR ', $mailshare_query).") $where_mailshare
      UNION
      SELECT group_email as mail FROM UGroup WHERE (".implode(' OR ', $group_query).") $where_group";
    $obm_q = new DB_OBM;
    display_debug_msg($query, $cdg_sql, 'get_email_used(1)');
    $obm_q->query($query);
    while($obm_q->next_record()) {
      preg_match_all('/^[^@\n]*@('.implode('|',$aliases).').*\r?$/m',$obm_q->f('mail'),$results);
      $return = array_merge($return, $results[0]);
    }
  }
  return $return;
}

/**
 * Check if the domain can be deleted
 *
 * @param mixed $d_id : domain_id
 * @return true if the domain can be deleted, else false
 **/
function check_domain_can_delete($p_id) {
  global $err, $ok_msg;
  global $l_link_user, $l_link_user_no, $l_link_host, $l_link_host_no;

  $delete_ok = true;

  // Links from Users
  $nb = get_linked_user_domain($p_id);
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= $l_link_user;
  } else {
    $ok_msg .= $l_link_user_no;
  }

  // Links from Hosts
  $nb = get_linked_host_domain($p_id);
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= $l_link_host;
  } else {
    $ok_msg .= $l_link_host_no;
  }

  return $delete_ok;
}


/**
 * Check if a domain can be added
 *
 * @return true if a domain can be added
 **/
function check_domain_can_add_domain() {
  global $c_singleNameSpace, $err, $l_cant_add_domain_single;

  $add_ok = true;

  if ($c_singleNameSpace) {
    // check # domain
    $cpt = 0;
    $test_q = new DB_OBM;
    $query = "SELECT count(*) as nb FROM Domain";
    display_debug_msg($query, $cdg_sql, 'check_domain_can_add_domain()');
    $test_q->query($query);
    $test_q->next_record();
    $nb = $test_q->f('nb');
    if ($nb >= 1) {
      $err['msg'] .= $l_cant_add_domain_single;
      return false;
    }
  }

  return $add_ok;
}


/**
 * Count number of users in a domain
 *
 * @param mixed $domain_id : domain_id
 * @return Number of users in a domain
 **/
function get_linked_user_domain($domain_id) {
  $domain_id = sql_parse_id($domain_id, true);
  $nb = get_query_count("SELECT count(userobm_id) as nb
    FROM UserObm
    WHERE userobm_domain_id $domain_id ");

  return $nb;
}


/**
 * Count number of hosts in a domain
 *
 * @param mixed $domain_id : domain_id
 * @return Number of hosts in a domain
 **/
function get_linked_host_domain($domain_id) {
  $domain_id = sql_parse_id($domain_id, true);
  $nb = get_query_count("SELECT count(host_id) as nb
    FROM Host
    WHERE host_domain_id $domain_id ");

  return $nb;
}


/**
 * Check if a mailserver can be added in a domain
 *
 * @param mixed $params : parameters : domain_id, mailserver_id
 * @return true if the mailserver can be removed from the domain
 **/
function check_domain_can_add_mailserver($params) {
  global $err;
  global $l_mailserver_invalid_role;

  $add_ok = true;
  $role = $params['role']; 

  // check mail servers have valid role
  $cpt = 0;
  $test_q = new DB_OBM;
  while ($cpt < $params['mailserver_nb']) {
    $cpt++;
    $m_id = sql_parse_id($params["mailserver$cpt"], true);
    $query = "SELECT count(*) as nb FROM MailServer
      WHERE mailserver_id $m_id 
      AND mailserver_$role=1";
    display_debug_msg($query, $cdg_sql, 'check_domain_can_add_mailserver()');
    $test_q->query($query);
    $test_q->next_record();
    $nb = $test_q->f('nb');
    if ($nb == 0) {
      $add_ok = false;
      $err['msg'] .= $l_mailserver_invalid_role;
      return $add_ok;
    }
  }

  return $add_ok;
}


/**
 * Check if a mailserver can be removed from a domain
 *
 * @param mixed $params : parameters : domain_id, mailserver_id
 * @return true if the mailserver can be removed from the domain
 **/
function check_domain_can_delete_mailserver($params) {
  global $err, $ok_msg;
  global $l_link_user_mailserver, $l_link_user_mailserver_no;

  $delete_ok = true;
  $domain_id = $params['domain_id'];
  $mail_server_id = $params['mail_server_id'];

  // Links from Users
  $nb = get_domain_linked_user_mailserver($domain_id, $mail_server_id);
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= $l_link_user_mailserver;
  } else {
    $ok_msg .= $l_link_user_mailserver_no;
  }

  return $delete_ok;
}


/**
 * Count number of users connected to a mailserver in a domain
 *
 * @param mixed $domain_id : domain_id
 * @param mixed $mailserver_id : mailserver_id
 * @return Number of users in a domain
 **/
function get_domain_linked_user_mailserver($domain_id, $mailserver_id) {
  $domain_id = sql_parse_id($domain_id, true);
  $mailserver_id = sql_parse_id($mailserver_id, true);
  $nb = get_query_count("SELECT count(userobm_id) as nb
    FROM UserObm
    WHERE userobm_domain_id $domain_id 
    AND userobm_mail_server_id $mailserver_id ");

  return $nb;
}


/**
 * Get the mailserver list candidate for a domain
 * where mailserver host_domain_id matches or from domain 0)
 * @param $role : restrict list to mail server role
 * @param $domain_id : restrict list to mail server in this domain
 *   if doma
 * @return array with mail servers
 **/
function get_domain_mailservers($role, $domain_id='') {
  global $cdg_sql;

  if ($domain_id > 0) {
    $domain_id = sql_parse_id($domain_id, true);
    // For one domain specified (domain update), global or domain mailserver
    $where_domain = "AND (domain_id $domain_id OR domain_global = true)";
  } else {
    // New domain (no id specified), host from domain 0
    $where_domain = "AND (domain_global = true)";
  }

  $obm_q = new DB_OBM;
  $query = "SELECT *
    FROM MailServer
    LEFT JOIN Host on mailserver_host_id = host_id
    LEFT JOIN Domain ON domain_id = host_domain_id
    WHERE mailserver_$role = 1
    $where_domain";
  display_debug_msg($query, $cdg_sql, 'get_domain_mailserver_list()');
  $obm_q->query($query);

  $m = array();
  while($obm_q->next_record()) {
    $id = $obm_q->f('mailserver_id');
    $m[$id]['id'] = $id;
    $m[$id]['name'] = $obm_q->f('host_name');
    $m[$id]['imap'] = $obm_q->f('mailserver_imap');
    $m[$id]['smtp_in'] = $obm_q->f('mailserver_smtp_in');
    $m[$id]['smtp_out'] = $obm_q->f('mailserver_smtp_out');
  }

  return $m;
}


///////////////////////////////////////////////////////////////////////////////
// Get a domain properties
// Parameters:
//   - $domain_id : domain_id
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_properties($domain_id = NULL) {
  global $cdg_sql;

  if (!is_null($domain_id)) {
    $val = 'domainpropertyvalue_value,';
    $join = " LEFT JOIN DomainPropertyValue
      ON domainproperty_key = domainpropertyvalue_property_key
      AND domainpropertyvalue_domain_id = '$domain_id'";
  }
  $query = "SELECT 
    $val
    domainproperty_key,
    domainproperty_type,
    domainproperty_default,
    domainproperty_readonly
    FROM DomainProperty $join";

  display_debug_msg($query, $cdg_sql, 'run_query_domain_properties()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;

}


/**
 * Query execution : DomainMailServer conditionnal insertion
 *
 * @param mixed $params[] : parameters
 * @result number of mailserver inserted
 **/
function run_query_domain_mailserver_insert($params) {
  global $obm, $cdg_sql;

  $sel_id = sql_parse_id($params['domain_id'], true);
  $ins_id = sql_parse_id($params['domain_id']);
  $role = $params['role'];
  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $params['mailserver_nb']) {
    $cpt++;
    $m_id = sql_parse_id($params["mailserver$cpt"], true);

    $query = "SELECT * FROM DomainMailServer
      WHERE domainmailserver_domain_id $id 
      AND domainmailserver_mailserver_id $m_id 
      AND domainmailserver_role='$role'";

    display_debug_msg($query, $cdg_sql, 'run_query_domain_mailserver_insert(1)');
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);

    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $query = "INSERT INTO DomainMailServer (
        domainmailserver_domain_id,
        domainmailserver_mailserver_id,
        domainmailserver_role)
        VALUES ($id, $m_id, '$role')";

      display_debug_msg($query, $cdg_sql, 'run_query_domain_mailserver_insert(2)');
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      $cpt_ins++;
    }
  }

  return $cpt_ins;
}


/**
 * Query execution : DomainMailServer deletion
 *
 * @param mixed $params[] : parameters 'domain_id', 'mailserver_id', 'role'
 * @result number of mailserver inserted
 **/
function run_query_domain_mailserver_delete($params) {
  global $obm, $cdg_sql;

  $id = sql_parse_id($params['domain_id'], true);
  $mailserver_id = sql_parse_id($params['mail_server_id'], true);
  $role = $params['role'];
  $query = "DELETE FROM DomainMailServer
    WHERE domainmailserver_domain_id $id 
    AND domainmailserver_mailserver_id $mailserver_id 
    AND domainmailserver_role='$role'";

  display_debug_msg($query, $cdg_sql, 'run_query_domain_mailserver_delete()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

?>
