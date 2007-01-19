<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : global_mailldap.inc                                          //
//     - Desc : Temp file for aliamin migration                              //
// 2007-01-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


//
// Get Mail informations
//
function get_mail_info() {
  global $cdg_sql;

  $query = "select * from Mail";

  display_debug_msg($query, $cdg_sql, "get_mail_info()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $cpt_domain = 0 ;
  $cpt_server = 0 ;

  while ($obm_q->next_record()) {
    $name = $obm_q->f("mail_name");
    $value = $obm_q->f("mail_value");
    
    if ($name == "domain") {
      $name .= "_$cpt_domain";
      $cpt_domain++;
    } elseif ($name == "mailserver") {
      $name .= "_$cpt_server";
      $cpt_server++;
    }
    
    $mail["$name"] = $value;
  }

  if (!isset($mail["relayhost"]) || $mail["relayhost"] == "") {
    $mail["only_relaying"] = 0;
  }else {
    $mail["only_relaying"] = 1;
  }

  return $mail;
}

?>
