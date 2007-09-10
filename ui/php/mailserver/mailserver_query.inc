<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailserver_query.inc                                         //
//     - Desc : Calendar query File                                          //
// 2007-02-08 - Mehdi Rande                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


/**
 * Mailserver Search query execution 
 *
 * @param mixed $params : mailserver search criteria
 * @return database object with search result
 **/
function run_query_mailserver_search($params) {
  global $cdg_sql, $ctu_sql_limit;
  
  $sql_order_dir = $params['sql_order_dir'];
  $sql_order_field = $params['sql_order_field'];
  $multidomain = sql_multidomain('host');

  $host_name = sql_search_text_parse($params['host_name']);
  $host_ip = sql_search_text_parse($params['host_ip']);
  $imap = $params['imap'];
  $smtp_in = $params['smtp_in'];
  $smtp_out = $params['smtp_out'];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $timeupdate = sql_date_format($db_type,'mailserver_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type,'mailserver_timecreate', 'timecreate');

  $where = '(1=1)';
  if ($host_name != '') {
    $where .= " AND host_name $like '$name%'";
  }
  if ($host_ip != '') {
    $where .= " AND host_ip $like '%$ip%'";
  }
  if ($imap != '') {
    $where .= ' AND mailserver_imap = 1';
  }
  if ($smtp_in != '') {
    $where .= ' AND mailserver_smtp_in = 1';
  }
  if ($smtp_out != '') {
    $where .= ' AND mailserver_smtp_out = 1';
  }
  $whereq = "WHERE $where $multidomain";

  // ORDER construction
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'host_name';
  $orderq .= " ORDER BY $order $sql_order_dir";

  $multidomain = sql_multidomain('host');
  $query = "SELECT DISTINCT MailServer.*,
      mailserver_id as id,
      $timecreate,
      $timeupdate,
      A.userobm_login as usercreate,
      B.userobm_login as userupdate,
      host_id,
      host_name,
      host_ip
    FROM MailServer
      LEFT JOIN Host on mailserver_host_id=host_id
      LEFT JOIN UserObm as A ON host_usercreate=A.userobm_id
      LEFT JOIN UserObm as B ON host_userupdate=B.userobm_id
    $whereq $multidomain 
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM MailServer LEFT JOIN Host on mailserver_host_id=host_id $whereq $multidomain");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'run_query_mailserver_search()');
    $obm_q->query($query);
  }

  return $obm_q;
}


/**
 * MailServer detail query execution
 *
 * @param mixed $id : mailserver id
 * @return database object with mailserver info
 **/
function run_query_mailserver_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type,'mailserver_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type,'mailserver_timecreate', 'timecreate');
  $multidomain = sql_multidomain('host');

  $query = "SELECT *,
      $timecreate,
      $timeupdate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate,
      host_id,
      host_name,
      host_ip
    FROM MailServer
         LEFT JOIN UserObm as c ON mailserver_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON mailserver_userupdate=u.userobm_id
         LEFT JOIN Host ON mailserver_host_id=host_id
    WHERE mailserver_id = '$id'
      $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_mailserver_detail()');
  $obm_q->query($query) ;
  $obm_q->next_record();

  return $obm_q;
}


/**
 * Query execution : mail server insertion
 *
 * @param mixed $params : mail server hash info
 * @return true if insert ok
 **/
function run_query_mailserver_insert($params) {
  global $obm, $cdg_sql;

  $now = date('Y-m-d H:i:s');
  $host_id = $params['host_id'];
  $imap = ($params['imap'] == 1) ? 1 : 0;
  $smtp_in = ($params['smtp_in'] == 1) ? 1 : 0;
  $smtp_out = ($params['smtp_out'] == 1) ? 1 : 0;

  $query = "INSERT INTO MailServer (mailserver_timeupdate,
    mailserver_timecreate,
    mailserver_userupdate,
    mailserver_usercreate,
    mailserver_host_id,
    mailserver_imap,
    mailserver_smtp_in,
    mailserver_smtp_out)
  VALUES (null,
    '$now',
    null,
    '$obm[uid]',
    '$host_id',
    '$imap',
    '$smtp_in',
    '$smtp_out')";

  display_debug_msg($query, $cdg_sql, 'run_query_mailserver_insert()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    $multidomain = sql_multidomain('host');
    // Get the inserted mailserver id
    $query = "SELECT mailserver_id
    FROM MailServer
      LEFT JOIN Host on mailserver_host_id=host_id
    WHERE
      mailserver_timecreate='$now'
      AND mailserver_host_id='$host_id'
      $multidomain";

    display_debug_msg($query, $cdg_sql, 'run_query_mailserver_insert()');
    $obm_q->query($query);
    $obm_q->next_record();
    $retour = $obm_q->f('mailserver_id');
  }

  return $retour;
}


/**
 * Maiserver Update query execution
 *
 * @param mixed $params : mailserver hash info
 * @return true is update OK
 **/
function run_query_mailserver_update($params) {
  global $obm, $cdg_sql;

  $id = $params['mailserver_id'];
  $imap = ($params['imap'] == 1) ? 1 : 0;
  $smtp_in = ($params['smtp_in'] == 1) ? 1 : 0;
  $smtp_out = ($params['smtp_out'] == 1) ? 1 : 0;
  $now = date('Y-m-d H:i:s');

  $query = "UPDATE MailServer SET
    mailserver_timeupdate='$now',
    mailserver_userupdate='$obm[uid]',
    mailserver_imap='$imap',
    mailserver_smtp_in='$smtp_in',
    mailserver_smtp_out='$smtp_out'
  WHERE mailserver_id='$id'";

  display_debug_msg($query, $cdg_sql, 'run_query_mailserver_update()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


/**
 * Deletion query execution
 *
 * @param mixed $m_id : mailserver id
 * @return true is query is ok
 **/
function run_query_mailserver_delete($m_id) {
  global $cdg_sql;

  $query = "DELETE FROM MailServer
    WHERE mailserver_id='$m_id'";
  display_debug_msg($query, $cdg_sql, 'run_query_mailserver_delete()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


/**
 * Check a host given is not already a mail server
 *
 * @param mixed $host_id Host id to check
 * @param mixed $id mailserver id to exclude from the check
 * @return true if host is a mailserver
 **/
function check_mailserver_host_is_mailserver($host_id, $id=false) {
  global $cdg_sql;

  if ($id) {
    $where = "AND mailserver_id != '$id'";
  }

  $multidomain = sql_multidomain('host');
  $query = "SELECT mailserver_id
    FROM MailServer
      JOIN Host ON mailserver_host_id = host_id
    WHERE mailserver_host_id = '$host_id'
          $where
          $multidomain";
  display_debug_msg($query, $cdg_sql, 'check_mailserver_host_is_mailserver()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}


/**
 * Check mail server form data
 *
 * @param mixed $params parameters
 * @return true if data are correct else false
 **/
function check_mailserver_data_form($params) {
  global $err;
  global $l_err_host_invalid, $l_err_host_is_mailserver;

  $action = $params['action'];

  $id = $params['id'];
  $host_id = $params['host_id'];

  // Check a correct host has been selected on insert
  if ($action == 'insert') {
    if (!is_numeric($host_id)) {
      $err['msg'] = $l_err_host_invalid;
      return false;
    }
  }

  // Check the host is not already referenced as a mail server
  if ($action == 'insert') {
    if (check_mailserver_host_is_mailserver($host_id, $id)) {
      $err['msg'] = $l_err_host_is_mailserver;
      return false;
    }
  }

  return true;
}


/**
 * Check if the mailserver can be deleted
 *
 * @param mixed $m_id : mailserver id
 * @return true if the mailserver can be deleted, else false
 **/
function check_can_delete_mailserver($m_id) {
  global $err, $cdg_sql, $ok_msg;
  global $l_link_domain, $l_link_domain_no, $l_link_user, $l_link_user_no;

  $delete_ok = true;

  // Links from Domain
  $query = "SELECT *
    FROM DomainMailServer
    WHERE domainmailserver_mailserver_id='$m_id'";
  display_debug_msg($query, $cdg_sql, 'check_can_delete_mailserver(1)');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $nb = $obm_q->num_rows();
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= $l_link_domain;
  } else {
    $query = "SELECT *
    FROM Domain
    WHERE domain_mail_server_id='$m_id'";
    display_debug_msg($query, $cdg_sql, 'check_can_delete_mailserver(2)');
    $obm_q->query($query);
    $nb = $obm_q->num_rows();
    if ($nb > 0) {
      $delete_ok = false;
      $err['msg'] .= $l_link_domain;
    } else {
      $ok_msg .= $l_link_domain_no;
    }
  }

  // Links from Users
  $query = "SELECT count(*) as nb
    FROM UserObm
    WHERE userobm_mail_server_id='$m_id'";
  display_debug_msg($query, $cdg_sql, 'check_can_delete_mailserver(3)');
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f('nb');
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= "<br />$l_link_user";
  } else {
    $ok_msg .= "<br />$l_link_user_no";
  }

  return $delete_ok;
}
