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
// OBM - File : global_mailldap.inc                                          //
//     - Desc : Temp file for aliamin migration                              //
// 2007-01-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Return an array of all used emails (users, groups, mailshare) conflicting
// with the given email set
// Parameters:
//   - $emails : email set
//   - $id  : User id to exclude from the result
//   - $gid : Group id to exclude from the result
//   - $mid : Mailshare id to exclude from the result
// Returns:
//   - $mails : array of used emails
///////////////////////////////////////////////////////////////////////////////
function get_email_used($emails, $id='', $gid='', $mid='') {
  global $cdg_sql;

  foreach($emails as $email) {
    list($mail,$domain) = explode('@', $email);
    $mail = addslashes($mail);
    $domain = addslashes($domain);
    $user_query[] = "userobm_email #LIKE '%$mail%'";
    $mailshare_query[] = "mailshare_email #LIKE '%$mail%'";
    $group_query[] = "group_email #LIKE '$mail%'";
    $regexp[] = str_replace('/','\/',preg_quote($mail));
    if(empty($domain)) {
      $regexp[] = str_replace('/','\/',preg_quote($mail))."@.*";
    } else {
      $regexp[] = str_replace('/','\/',preg_quote($email));
    }
  }
  if ($id != '') {
    $id = sql_parse_id($id, true, true);
    $where_user = " AND userobm_id $id ";
  }
  $where_user .= sql_multidomain('userobm');
  if ($gid != '') {
    $gid = sql_parse_id($gid, true, true);
    $where_group = " AND group_id $gid ";
  }
  $where_group .= sql_multidomain('group');
  if ($mid != '') {
    $mid = sql_parse_id($mid, true, true);
    $where_mailshare = " AND mailshare_id $mid ";
  }
  $where_mailshare .= sql_multidomain('mailshare');

  $obm_q = new DB_OBM;
  $ctt[0]["type"] = "field";
  $ctt[0]["value"] = "userobm_lastname";
  $ctt[1]["type"] = "string";
  $ctt[1]["value"] = " ";
  $ctt[2]["type"] = "field";
  $ctt[2]["value"] = "userobm_firstname";
  $user_label = sql_string_concat($obm_q->type, $ctt);

  $query = "SELECT userobm_email as mail, $user_label as label FROM UserObm WHERE (".implode(' OR ', $user_query).") $where_user
            UNION
            SELECT mailshare_email as mail, mailshare_name as label FROM MailShare WHERE (".implode(' OR ', $mailshare_query).") $where_mailshare
            UNION
            SELECT group_email as mail, group_name as label FROM UGroup WHERE (".implode(' OR ', $group_query).") $where_group"; 

  display_debug_msg($query, $cdg_sql, 'get_email_used(1)');
  $obm_q->xquery($query);
  $regexp = implode('|',$regexp);
  $return = array();
  while($obm_q->next_record()) {
    preg_match_all('/^('.$regexp.')\r?$/mi',$obm_q->f('mail'),$results);
    $return = array_merge($return, $results[1]);
  }
  return implode(', ',$return);
 
} 
///////////////////////////////////////////////////////////////////////////////
// Get the host name from the host Id
// Parameters:
//   - $id : host id 
///////////////////////////////////////////////////////////////////////////////
function get_last_host_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT host_name FROM Host WHERE host_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_host_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('host_name');
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the host name from the host Id (better function name)
// Parameters:
//   - $id : host id
///////////////////////////////////////////////////////////////////////////////
function get_hostname_by_id($id) {

  return get_last_host_text($id);
}


///////////////////////////////////////////////////////////////////////////////
// Get Samba informations
///////////////////////////////////////////////////////////////////////////////
function get_samba_info() {
  global $cdg_sql, $obm;

  $query = "
    SELECT * FROM ServiceProperty 
    INNER JOIN DomainEntity ON domainentity_entity_id = serviceproperty_entity_id
    WHERE serviceproperty_service = 'samba'
    AND domainentity_domain_id = $obm[domain_id]";

  display_debug_msg($query, $cdg_sql, 'get_samba_info()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $cpt = 0;
  while ($obm_q->next_record()) {
    $name = $obm_q->f('serviceproperty_property');
    $value = $obm_q->f('serviceproperty_value');
    $smb["$name"] = $value;
  }
  return $smb;
}


///////////////////////////////////////////////////////////////////////////////
// Return the first uid not in use (starting at $c_first_uid)
// Returns:
//   - $uid : first free uid
///////////////////////////////////////////////////////////////////////////////
function get_first_user_free_uid($first_id=false) {
  global $cdg_sql;

  $query = "
    SELECT obminfo_value as uid_max_used
    FROM ObmInfo
    WHERE obminfo_name = 'uid_max_used'
    ORDER BY obminfo_value";
  display_debug_msg($query, $cdg_sql, 'get_first_user_free_uid()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $uid_max_used = $obm_q->f('uid_max_used');
  }

  if (is_null($uid_max_used)) {
    $uid_max_used = get_first_user_free_uid_from_bd($first_id);
    $query = "
      INSERT INTO ObmInfo
      (obminfo_name, obminfo_value)
      VALUES ('uid_max_used', '".$uid_max_used."');";
    display_debug_msg($query, $cdg_sql, 'get_first_user_free_uid()');
    $obm_q->query($query);
  }

  $new_uid = $uid_max_used + 1;

  $query = "
    UPDATE ObmInfo
    SET obminfo_value='".$new_uid."'
    WHERE obminfo_name='uid_max_used'";
  display_debug_msg($query, $cdg_sql, 'get_first_user_free_uid()');
  $obm_q->query($query);

  return $new_uid;
}


function get_first_user_free_uid_from_bd($first_id=false) {
  global $c_first_uid;

  $uids = array();

  if ($first_id === false) {
    $first_id = $c_first_uid;
  }
  
  $query = "
    SELECT host_uid as uid
    FROM Host
    WHERE host_uid >= '$c_first_uid'
    ORDER BY uid";
  display_debug_msg($query, $cdg_sql, 'get_first_user_free_uid()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $uids[] = $obm_q->f('uid');
  }

  $query = "
    SELECT userobm_uid as uid
    FROM UserObm
    WHERE userobm_uid >= '$c_first_uid'
    ORDER BY uid";
  display_debug_msg($query, $cdg_sql, 'get_first_user_free_uid()');
  $obm_q->query($query);
  
  while ($obm_q->next_record()) {
    $uids[] = $obm_q->f('uid');
  }

  // Avec Samba, les UID et GID doivent être unique car le SID en est déduit
  // directement en concaténant l'UID ou le GID au SID du domaine.
  // Or il faut éviter les doublons de SID, y compris entre
  // utilisateurs/hôtes/groupes.
  // Le mappage direct est intéressant dans le cadre de migration NT->Samba
  $query = "
    SELECT group_gid as uid
    FROM UGroup
    WHERE group_gid >= '$first_id'
    ORDER BY group_gid";
  display_debug_msg($query, $cdg_sql, 'get_first_user_free_uid()');
  $obm_q->query($query);

  while( $obm_q->next_record() ) {
    $uids[] = $obm_q->f('uid');
  }
  
  // On ne tient pas compte des doublons - même si ce cas ne peut pas se
  // produire via l'interface, il peut se produire en cas de peuplement
  // direct de celle-ci par un script. Ceci est un PROBLEME.
  // On supprime donc les doublons afin d'eviter que le probleme n'empire via
  // l'interface...
  $uids = array_unique($uids);
  sort($uids);
  if ($uibBdMax = end($uids)) {
    if ($uibBdMax > $first_id) {
      $first_id = $uibBdMax;
    }
  }

  return $first_id;
}


///////////////////////////////////////////////////////////////////////////////
// Return group id from its gid
// Parameters:
//   - $gid : group gid
// Returns:
//   - group id
///////////////////////////////////////////////////////////////////////////////
function get_group_id_from_gid($domain_id, $gid) {
  global $cdg_sql;

  $query = "SELECT group_id
    FROM UGroup
    WHERE group_gid='$gid' AND group_domain_id=$domain_id";

  display_debug_msg($query, $cdg_sql, 'get_group_id_from_gid()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $id = $obm_q->f('group_id');

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Return the first gid not in use (starting at $c_gid_user)
// Returns:
//   - $gid : first free gid
///////////////////////////////////////////////////////////////////////////////
function get_first_group_free_gid() {
  global $cg_gid_user;

  return get_first_user_free_uid($cg_gid_user);
}


///////////////////////////////////////////////////////////////////////////////
// Return an array of all used IPs (hosts only for now)
// Parameters:
//   - $id  : Host id to exclude from the result
// Returns:
//   - $mails : array of used emails
///////////////////////////////////////////////////////////////////////////////
function get_ip_used($id='') {
  global $cdg_sql, $php_regexp_email_name, $l_host;

  $ips = array();

  if ($id != '') {
    $id = sql_parse_id($id, true, true);
    $where_id = "WHERE host_id $id ";
  }
  $query = "SELECT host_name, host_ip
    FROM Host
    $where_id";

  display_debug_msg($query, $cdg_sql, 'get_ip_used()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $ip = $obm_q->f('host_ip');
    $hname = $obm_q->f('host_name');
    $name = "$l_host : $hname";
    $ips["$ip"] = "$name";
  }

  return $ips;
}


/**
 * Get the Update state (1 update to be done, 0 no changes)
 * return $state : 1 : update to be done, 0 no changes
 */
function get_update_state($domain_id='no') {
  global $obm, $cdg_sql;
  if ($domain_id == 'no') {
    $domain_id = $obm['domain_id'];
  }
  $domain_id = sql_parse_id($domain_id, true);
  $query = "SELECT domainpropertyvalue_value as value
    FROM DomainPropertyValue
    WHERE domainpropertyvalue_domain_id $domain_id 
     AND domainpropertyvalue_property_key = 'update_state'";
  display_debug_msg($query, $cdg_sql, 'get_update_state()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $state = $obm_q->f('value');

  if ($state == '1') return '1';
  else return '0';
}


/**
 * Update : set the update flag to true to allow display update info
 *
 * @param $state : 1 set to true (there are update, else 0)
 */
function set_update_state($domain_id='no', $state=1) {
  global $obm, $cdg_sql;

  if ($domain_id == 'no') {
    $domain_id = $obm['domain_id'];
  }
  $domain_id = sql_parse_id($domain_id, true);

  $query = "UPDATE DomainPropertyValue
    SET domainpropertyvalue_value = '$state'
    WHERE domainpropertyvalue_domain_id $domain_id 
      AND domainpropertyvalue_property_key = 'update_state'";

  display_debug_msg($query, $cdg_sql, 'set_update_state($state)');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


/**
 * Get the Update lock
 * return $lock : 1 locked, 0 no lock
 */
function get_update_lock() {
  global $obm, $cdg_sql;

  $query = "SELECT obminfo_value as value
    FROM ObmInfo
    WHERE obminfo_name='update_lock'";

  display_debug_msg($query, $cdg_sql, 'get_update_lock()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $lock = $obm_q->f('value');

  if ($lock == '1') return '1';
  else return '0';
}


/**
 * Update : set the update lock
 */
function set_update_lock() {
  global $cdg_sql;

  $query = "UPDATE ObmInfo
    SET obminfo_value = '1'
    WHERE obminfo_name = 'update_lock'";

  display_debug_msg($query, $cdg_sql, 'set_update_lock()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


/**
 * Update : remove the update lock
 */
function remove_update_lock() {
  global $cdg_sql;

  $query = "UPDATE ObmInfo
    SET obminfo_value = '0'
    WHERE obminfo_name = 'update_lock'";

  display_debug_msg($query, $cdg_sql, 'remove_update_lock()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


?>
