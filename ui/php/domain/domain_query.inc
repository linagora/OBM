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
    $whereq = " WHERE $where $where_restriction";
  }

  $select = "SELECT distinct Domain.*,
      domain_id as id
    FROM Domain";


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

  $query = "SELECT Domain.*,
    host_name as mailserver_hostname
    FROM Domain
    LEFT JOIN MailServer ON mailserver_id = domain_mail_server_id
    LEFT JOIN Host ON host_id = mailserver_host_id
    WHERE domain_id='$p_id'";

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
  
  $uid = $obm['uid'];
  $now = date('Y-m-d H:i:s');
  $label = $domain['label'];
  $desc = $domain['desc'];
  $domain_name = $domain['domain_name'];
  $alias = $domain['alias'];
  $mail_server = $domain['mail_server'];

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
    '$uid',
    '$label',
    '$desc',
    '$domain_name',
    '$mail_server',
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

  $query = "DELETE FROM DomainPropertyValue WHERE domainpropertyvalue_domain_id = '$domain_id'";
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
          '$domain_id',
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

  $uid = $obm['uid'];
  $now = date('Y-m-d H:i:s');
  $label = $domain['label'];
  $desc = $domain['desc'];
  $alias = $domain['alias'];
  $mail_server = $domain['mail_server'];

  $query = "UPDATE Domain SET
    domain_timeupdate='$now',
    domain_userupdate='$uid',
    domain_label='$label',
    domain_description='$desc',
    domain_mail_server_id='$mail_server',
    domain_alias='$alias'
  WHERE domain_id='$d_id'";

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

  $query = "DELETE FROM DomainPropertyValue WHERE domainpropertyvalue_domain_id='$p_id'";
  display_debug_msg($query, $cdg_sql, 'run_query_domain_delete()');
  $retour = $obm_q->query($query);

  $query = "DELETE FROM Domain WHERE domain_id='$p_id'";
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
  global $err, $l_label_error;

  $label = $domain['label'];

  // MANDATORY: Label
  if (trim($label) == '') {
    $err['msg'] = $l_label_error.' : '. $label;
    return false;
  }
    
  return true;
}


/**
 * Check if the domain can be deleted
 *
 * @param mixed $d_id : domain_id
 * @return true if the domain can be deleted, else false
 **/
function check_domain_can_delete($p_id) {
  global $err, $ok_msg;
  global $l_link_user, $l_link_user_no;

  $delete_ok = true;

  // Links from Users
  $nb = get_linked_user_domain($p_id);
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= $l_link_user;
  } else {
    $ok_msg .= $l_link_user_no;
  }

  return $delete_ok;
}


/**
 * Count number of users in a domain
 *
 * @param mixed $domain_id : domain_id
 * @return Number of users in a domain
 **/
function get_linked_user_domain($domain_id) {

  $nb = get_query_count("SELECT count(userobm_id) as nb
    FROM UserObm
    WHERE userobm_domain_id='$domain_id'");

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
    $m_id = $params["mailserver$cpt"];
    $query = "SELECT count(*) as nb FROM MailServer
      WHERE mailserver_id='$m_id'
        AND mailserver_$role='1'";
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
  $mailserver_id = $params['mailserver_id'];

  // Links from Users
  $nb = get_domain_linked_user_mailserver($domain_id, $mailserver_id);
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

  $nb = get_query_count("SELECT count(userobm_id) as nb
    FROM UserObm
    WHERE userobm_domain_id='$domain_id'
      AND userobm_mail_server_id='$mailserver_id'");

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Get a domain properties
// Parameters:
//   - $domain_id : domain_id
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_properties($domain_id=0) {
  global $cdg_sql;

  if ($domain_id != 0) {
    $val = 'domainpropertyvalue_value,';
    $join = " LEFT JOIN DomainPropertyValue
                ON domainproperty_key = domainpropertyvalue_property_key
                  AND domainpropertyvalue_domain_id = '$domain_id'";
  }
  $query = "SELECT 
      $val
      domainproperty_key,
      domainproperty_type,
      domainproperty_default
    FROM DomainProperty $join
    WHERE domainproperty_readonly = 0";
  
  display_debug_msg($query, $cdg_sql, 'run_query_domain_properties()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;

}


/**
 * Get one domain mailservers
 *
 * @param mixed $domain_id : domain_id
 * @return mailserver array
 **/
function get_domain_mailserver($domain_id=0, $role='') {
  global $cdg_sql;

  if ($domain_id != 0) {
    $where_domain = "domainmailserver_domain_id='$domain_id'";
    $and = 'AND';
  }
  if ($role != '') {
    $where_role = "$and domainmailserver_role='$role'";
    $and = 'AND';
  }

  if (($where_domain != '') || ($where_role != '')) {
    $where = "WHERE $where_domain $where_role";
  }

  $query = "SELECT distinct
      domainmailserver_domain_id,
      domainmailserver_mailserver_id,
      domainmailserver_role,
      mailserver_host_id,
      host_name,
      host_ip
    FROM DomainMailServer
      LEFT JOIN MailServer ON domainmailserver_mailserver_id=mailserver_id
      LEFT JOIN Host ON mailserver_host_id=host_id
    $where";
  
  display_debug_msg($query, $cdg_sql, 'get_domain_mailserver()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  $m = array();
  while ($obm_q->next_record()) {
    $d_id = $obm_q->f('domainmailserver_domain_id');
    $m_id = $obm_q->f('domainmailserver_mailserver_id');
    $role = $obm_q->f('domainmailserver_role');
    $name = $obm_q->f('host_name');
    $ip = $obm_q->f('host_ip');
    $m[$d_id][$m_id][] = array ('name' => $name, 'ip' => $ip, 'role' => $role);
  }

  return $m;
}


/**
 * Query execution : DomainMailServer conditionnal insertion
 *
 * @param mixed $params[] : parameters
 * @result number of mailserver inserted
**/
function run_query_domain_mailserver_insert($params) {
  global $obm, $cdg_sql;

  $id = $params['domain_id'];
  $role = $params['role'];
  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $params['mailserver_nb']) {
    $cpt++;
    $m_id = $params["mailserver$cpt"];

    $query = "SELECT * FROM DomainMailServer
      WHERE domainmailserver_domain_id='$id'
        AND domainmailserver_mailserver_id='$m_id'
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
      VALUES ('$id', '$m_id', '$role')";

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

  $id = $params['domain_id'];
  $mailserver_id = $params['mailserver_id'];
  $role = $params['role'];
  $query = "DELETE FROM DomainMailServer
      WHERE domainmailserver_domain_id='$id'
        AND domainmailserver_mailserver_id='$mailserver_id'
        AND domainmailserver_role='$role'";

  display_debug_msg($query, $cdg_sql, 'run_query_domain_mailserver_delete()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

?>
