<script language="php"></script>
<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailchooser.inc.sample                                       //
//     - Desc : OBM user mail server automatic chooser                       //
//              configuration Sample file                                    //
// 2008-10-22 Christophe Liou Kee On                                         //
///////////////////////////////////////////////////////////////////////////////
// $Id:$ //
///////////////////////////////////////////////////////////////////////////////


// Mail chooser hooks list

$c_mailchooser_hooks = array(

  array(
    'id'            => 1,
    'hook_function' => 'lest_boxes',
    'name'          => $l_mailboxchoice_lest_boxes,
  ),
  
  array(
    'id'            => 2,
    'hook_function' => 'lest_used_memory',
    'name'          => $l_mailboxchoice_lest_used_memory,
  ),

);

/*

$form

Array
(
    [lastname] => tata
    [login] => tata
    [profile] => admin
    [email] => 
    [aliases] => Array
        (
        )
    [mail_quota] => 0
    [vacation_datebegin] => 10/22/2008
    [time_begin] => 08
    [min_begin] => 00
    [vacation_dateend] => 10/22/2008
    [time_end] => 08
    [min_end] => 00
    [user_id] => 5
    [group_nb] => 0
)



*/
function hook_user_mailchooser_lest_boxes ($form) {
  global $cdg_sql, $obm;
  
  $sel_id = sql_parse_id($obm['domain_id'], true);
  
  $query = "SELECT
      domainmailserver_mailserver_id as mailserver_id,
      COUNT(*) AS mailbox_count
    FROM DomainMailServer
    JOIN UserObm u ON domainmailserver_mailserver_id = userobm_mail_server_id
    WHERE domainmailserver_domain_id $sel_id
    GROUP BY domainmailserver_mailserver_id
    ORDER BY mailbox_count ASC
    ";

  display_debug_msg($query, $cdg_sql, 'hook_user_mailchooser_lest_boxes()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  return array('mailserver_id' => $obm_q->f('mailserver_id'));
}

function hook_user_mailchooser_lest_used_memory ($form) {
  global $cdg_sql, $obm;
  
  $sel_id = sql_parse_id($obm['domain_id'], true);
  
  $query = "SELECT
      domainmailserver_mailserver_id as mailserver_id,
      SUM(userobm_mail_quota_use) AS used_memory
    FROM DomainMailServer
    JOIN UserObm u ON domainmailserver_mailserver_id = userobm_mail_server_id
    WHERE domainmailserver_domain_id $sel_id
    GROUP BY domainmailserver_mailserver_id
    ORDER BY used_memory ASC
    ";

  display_debug_msg($query, $cdg_sql, 'hook_user_mailchooser_lest_used_memory()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  return array('mailserver_id' => $obm_q->f('mailserver_id'));
}

