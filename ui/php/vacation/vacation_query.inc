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
// OBM - File : vacation_query.inc                                           //
//     - Desc : vacation query File                                          //
// 2007-01-22 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Vacation detail query execution
// Parameters:
//   - $u_id : User id
///////////////////////////////////////////////////////////////////////////////
function run_query_vacation_detail($u_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;    
  $db_type = $obm_q->type;
  $u_id = sql_parse_id($u_id, true);
  $query = "SELECT 
    userobm_vacation_datebegin,
    userobm_vacation_dateend, 
    userobm_id,
    userobm_domain_id,
    userobm_login,
    userobm_mail_perms,
    userobm_vacation_enable,
    userobm_vacation_message
  FROM UserObm
  WHERE userobm_id $u_id ";
    
  display_debug_msg($query, $cdg_sql, "run_query_vacation_detail()");
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


//
// updates the database fields according to vacation 
// updates also sieve/cyrus to activate vacation if needed
// returns true if no problem is encountered
// else returns false
//
function update_vacation($params, $vac_q) {
  $retVal = run_query_vacation_update($params, $vac_q->f("userobm_id"));

  if ($retVal) {
    $retVal = update_sieve($params, $vac_q->f("userobm_login"), $vac_q->f("userobm_domain_id"));
  }

  return $retVal;
}

//
// updates the database fields according to $vacation
// return true if ok
// else returns false
//
function run_query_vacation_update($params, $u_id) {
  global $cdg_sql;
  $obm_q = new DB_OBM;

  if (!$params['auto_enable']) {
    $params['vacation_datebegin'] = '0';
    $params['vacation_dateend'] = '0';
  }    

  
  $vacation_datebegin = ($params['vacation_datebegin'] ? "'$params[vacation_datebegin]'" : 'null');
  $vacation_dateend = ($params['vacation_dateend'] ? "'$params[vacation_dateend]'" : 'null');  

  $enabled = sql_parse_int($params["enabled"]);
  $message_update = ", userobm_vacation_message='".$obm_q->escape($params["message"])."'";
  $time_begin = $params["time_begin"].":".$params["min_begin"].":00";
  $time_end = $params["time_end"].":".$params["min_end"].":00";
  $message_update .= ", userobm_vacation_datebegin= $vacation_datebegin";
  $message_update .= ", userobm_vacation_dateend= $vacation_dateend";
  $u_id = sql_parse_id($u_id, true);

  // Update done in real-time, so updating UserObm AND P_UserObm
  $query = "UPDATE UserObm
    SET userobm_vacation_enable = $enabled
    $message_update
    WHERE userobm_id $u_id ";
    
  display_debug_msg($query, $cdg_sql, "run_query_vacation_update()");
  $ret = $obm_q->query($query);

  $query = "UPDATE P_UserObm
    SET userobm_vacation_enable = $enabled
    $message_update
    WHERE userobm_id $u_id ";

  display_debug_msg($query, $cdg_sql, "run_query_vacation_update()");
  $ret = $obm_q->query($query);
 
  return $ret;
}


//
// updates sieve status according to $vacation,
// ie. upload and activate the new script in sieve
// returns true if ok,
// else returns false and sets $err["msg"];
//
function update_sieve($vacation, $u_uid, $u_domain_id) {  
  global $err, $l_sieve_login_failed, $cmd_vacation;

  $cmd = $cmd_vacation.' --login '.$u_uid.' --domain-id '.$u_domain_id;
  
  display_debug_msg($cmd, $cdg_exe, "update_sieve()");
  exec($cmd);
  
  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Vacation Data checking and formatting
// Parameters:
//   - $params : values checked
///////////////////////////////////////////////////////////////////////////////
function check_vacation_data_form($params) {
  global $err, $l_empty_message;

  // le message doit être non-vide si le renvoi est activé
  
  if ($params["enabled"] == 0 && $params['auto_enable'] == 0) {
    return true;
  }

  if (trim($params["message"]) == "") {
    $err["msg"] = "$l_empty_message";
    $err["field"] = "message";
    return false;
  }

  return true;
}


?>
