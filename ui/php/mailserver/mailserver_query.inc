<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailserver_query.inc                                           //
//     - Desc : Calendar query File                                          //
// 2007-02-08 - Mehdi Rande                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Return the mailserver amount
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function run_query_mailserver_count() {
  global $cdg_sql;

  $multidomain = sql_multidomain("host");
  $query = "SELECT mailserver_id FROM MailServer 
    JOIN Host ON mailserver_host_id = host_id
    WHERE 1=1 $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_mailserver_count())");
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;  
}
///////////////////////////////////////////////////////////////////////////////
// Return All mailservers
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function run_query_mailserver_getall() {
  global $cdg_sql;

  $multidomain = sql_multidomain("host");
  $query = "SELECT
    mailserver_id,
    rh.host_name as relayhost_name,
    rh.host_ip as relayhost_ip,
    ms.host_name as mailserver_name,
    ms.host_ip as mailserver_ip    
    FROM MailServer 
    JOIN Host ms ON mailserver_host_id = ms.host_id
    LEFT JOIN Host rh ON mailserver_relayhost_id = rh.host_id    
    WHERE 1=1 $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_mailserver_count())");
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  return $obm_q;  

}
///////////////////////////////////////////////////////////////////////////////
// Return all suitable host for mail server role
///////////////////////////////////////////////////////////////////////////////
function run_query_mailserver_server_host($id=false) {
  global $cdg_sql;

  $multidomain = sql_multidomain("host");
  if($id) {
    $where = "OR mailserver_id = '$id'";
  }
  $query = "
    SELECT host_id, host_name FROM Host
    LEFT JOIN MailServer ON host_id = mailserver_host_id
    WHERE (mailserver_id IS NULL $where)
          $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_mailserver_server_host()");
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Return all suitable host for mail server role
///////////////////////////////////////////////////////////////////////////////
function run_query_mailserver_check_server($host_id,$id=false) {
  global $cdg_sql;
  if($id) {
    $where = "AND mailserver_id != '$id'";
  }

  $multidomain = sql_multidomain("host");
  $query = "
    SELECT mailserver_id FROM MailServer
    WHERE mailserver_host_id = '$host_id' $where
          $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_mailserver_check_server()");
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  return !($obm_q->next_record());
  
}

///////////////////////////////////////////////////////////////////////////////
// Insert Mail server datas
// ///////////////////////////////////////////////////////////////////////////////
function run_query_mailserver_insert($mailserver) {
  global $cdg_sql;
  $host_id = $mailserver["host_id"];
  $relayhost_id = $mailserver["relayhost_id"];
  $mailserver_networks = $mailserver["mailserver_networks"];
  if($relayhost_id != "") {
    $relayhost_networks = $mailserver["relayhost_networks"];
  }
  $query = "
    INSERT INTO MailServer (
      mailserver_host_id,
      mailserver_relayhost_id
    )
    VALUES (
      '$host_id',
      '$relayhost_id'
    )";
  $obm_q= new DB_OBM;
  display_debug_msg($query, $cdg_sql, "run_query_mailserver_insert()");
  $obm_q->query($query);
  $query = "SELECT mailserver_id FROM MailServer where mailserver_host_id = '$host_id'";
  display_debug_msg($query, $cdg_sql, "run_query_mailserver_add_networks()");
  $obm_q->query($query);
  $obm_q->next_record();
  $id = $obm_q->f("mailserver_id");
  run_query_mailserver_add_networks($relayhost_networks,$relayhost_id);

  run_query_mailserver_add_networks($mailserver_networks,$host_id);
  return $id;
}

///////////////////////////////////////////////////////////////////////////////
// Insert Mail server datas
// ///////////////////////////////////////////////////////////////////////////////
function run_query_mailserver_update($mailserver) {
  $host_id = $mailserver["host_id"];
  $relayhost_id = $mailserver["relayhost_id"];
  $mailserver_networks = $mailserver["mailserver_networks"];
  $mailserver_networks = $mailserver["mailserver_networks"];
  if($relayhost_id != "") {
    $relayhost_networks = $mailserver["relayhost_networks"];
  }  
  $id = $mailserver["id"];

  $query = "
    UPDATE MailServer SET
      mailserver_host_id = '$host_id',
      mailserver_relayhost_id = '$relayhost_id'
    WHERE mailserver_id = '$id'
    ";
  $obm_q= new DB_OBM;
  display_debug_msg($query, $cdg_sql, "run_query_mailserver_insert()");
  $obm_q->query($query);

  run_query_mailserver_add_networks($relayhost_networks,$relayhost_id);

  run_query_mailserver_add_networks($mailserver_networks,$host_id);
}

///////////////////////////////////////////////////////////////////////////////
// Return the mailserver details
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function run_query_mailserver_details($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("host");
  $query = "
    SELECT
    mailserver_id,
    mailserver_host_id, 
    mailserver_relayhost_id,
    rh.host_name as relayhost_name,
    rh.host_ip as relayhost_ip,
    ms.host_name as mailserver_name,
    ms.host_ip as mailserver_ip
    FROM MailServer 
    JOIN Host ms ON mailserver_host_id = ms.host_id
    LEFT JOIN Host rh ON mailserver_relayhost_id = rh.host_id
    WHERE mailserver_id = $id $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_mailserver_count())");
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;  
}
///////////////////////////////////////////////////////////////////////////////
// Return the mailserver networks
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function run_query_mailserver_networks($id) {
  $multidomain = sql_multidomain("host");
  
  $query = "
    SELECT
    mailservernetwork_host_id, 
    mailservernetwork_ip
    FROM MailServerNetwork
    JOIN MailServer ON mailservernetwork_host_id = mailserver_host_id
                    OR  mailservernetwork_host_id = mailserver_relayhost_id
    WHERE mailserver_id = $id $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_mailserver_count())");
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;  
}

///////////////////////////////////////////////////////////////////////////////
// Insert Mail networks datas
// ///////////////////////////////////////////////////////////////////////////////
function run_query_mailserver_add_networks($networks,$id) {
  global $cdg_sql;

  $query = "
    DELETE FROM MailServerNetwork WHERE mailservernetwork_host_id = '$id'
  ";
  $obm_q= new DB_OBM;
  display_debug_msg($query, $cdg_sql, "run_query_mailserver_add_networks()");
  $obm_q->query($query);

  $query = "
    INSERT INTO MailServerNetwork (
      mailservernetwork_host_id,
      mailservernetwork_ip
    )
    VALUES (
      '$id',
      '%s'
    )";

  if(is_array($networks))
  foreach($networks as $network) {
    if($network != "") {
      $tquery = sprintf($query,$network);
      $obm_q->query($tquery);
    }
  }
}
///////////////////////////////////////////////////////////////////////////////
// Check if the mailserver can be deleted
///////////////////////////////////////////////////////////////////////////////
function check_can_change_mailserver($id) {
  global $cdg_sql;
  $multidomain = sql_multidomain("userobm");
  
  $query = "
    SELECT
    userobm_id
    FROM UserObm
    JOIN MailServer ON userobm_mail_server_id = mailserver_id
    WHERE mailserver_id = $id $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_mailserver_count())");
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return !($obm_q->next_record());  

}
///////////////////////////////////////////////////////////////////////////////
// Delete th mailserver
///////////////////////////////////////////////////////////////////////////////
function run_query_mailserver_delete($id) {
  global $cdg_sql;
  $ms_q = run_query_mailserver_details($id);
  if($ms_q->f('mailserver_relayhost_id') != "")  {
    $where = "OR mailservernetwork_host_id = '".$ms_q->f('mailserver_relayhost_id')."'";
  }
  $obm_q= new DB_OBM;
  $query = "
    DELETE 
    FROM MailServerNetwork
    WHERE mailservernetwork_host_id = '".$ms_q->f('mailserver_host_id')."'
    $where
  ";
  $retour = $obm_q->query($query); 
  if($retour) {
    $query = "
      DELETE 
      FROM MailServer
      WHERE mailserver_id = '$id'
    ";
    $retour = $obm_q->query($query); 
  } 
  return $retour;
}
///////////////////////////////////////////////////////////////////////////////
// Check creation form
///////////////////////////////////////////////////////////////////////////////
function check_mailserver_data_form($mailserver) {
  global $php_regexp_ip,$err_msg;
  global $l_invalid_mailserver,$l_invalid_relayhost,$l_invalid_ip;


  $host_id = $mailserver["host_id"];
  $id = $mailserver["id"];
  $relayhost_id = $mailserver["relayhost_id"];
  $mailserver_networks = $mailserver["mailserver_networks"];

  if(!is_numeric($host_id)) {
    $err_msg = $l_invalid_mailserver;
    return false;
  }

  $check = run_query_mailserver_check_server($host_id,$id);
  if(!$check) {
    $err_msg = $l_invalid_mailserver;
    return false;
  }
  if(is_numeric($relayhost_id)) {
    if($relayhost_id == $host_id) {
      $err_msg = $l_invalid_relayhost;
      return false;    
    }
    $check = run_query_mailserver_check_server($relayhost_id,$id);
    if(!$check) {
      $err_msg = $l_invalid_relayhost;
      return false;    
    } 
  }

  if(is_array($mailserver_networks)) {
    foreach($mailserver_networks as $net) {
      if($net != "" && preg_match($php_regexp_ip, $net) == 0) {
        $err_msg = "$l_invalid_ip : $net";
        return false;    
      }
    }
  }

  return true;
}
