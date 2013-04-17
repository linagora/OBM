<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

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

$obminclude = getenv('OBM_INCLUDE_VAR');
if (!$obminclude) $obminclude = 'obminclude';
require_once($obminclude . '/of/of_captcha.php');
require_once($obminclude . '/of/of_session.inc');


///////////////////////////////////////////////////////////////////////////////
// OBM - File : user_query.inc                                               //
//     - Desc : User query & db File                                         //
// 2000-01-13 Aliacom - Florent Goalabre                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// User query search query execution
// Parameters:
//   - $user[] : user search criteria
//     keys used  : name, perms
//   - $sql_order_field : infos for order clause
//   - $sql_order_dir : direction for order clause (asc, desc)
//   - $filter_entity : if and which filter_entity to set (eg calendar readable)
///////////////////////////////////////////////////////////////////////////////
function run_query_user_search($user, $strongest_profile_level=null, $user_can_manage_peers=null) {
  global $obm, $c_all, $cdg_sql, $ctu_sql_limit;
  global $cgp_archive_only;
  
  $obm_q = new DB_OBM;

  $sql_order_dir = $user['sql_order_dir'];
  $sql_order_field = $user['sql_order_field'];

  $filter_entity = $user['filter_entity'];
  $filter_pattern = $user['filter_pattern'];
  $profile = $user['profile'];
  $domain_id = $user['domain_id'];
  
  $login = sql_search_text_parse($obm_q->escape($user['login']));
  $cname = sql_search_text_parse($obm_q->escape($user['commonname']));
  $lname = sql_search_text_parse($obm_q->escape($user['lastname']));
  $fname = sql_search_text_parse($obm_q->escape($user['firstname']));
  $delegation = sql_search_text_parse($user['delegation']);
  $email = sql_search_text_parse($obm_q->escape($user['email']));
  $desc = sql_search_text_parse($obm_q->escape($user['desc']));
  $group = sql_search_text_parse($obm_q->escape($user['group']));
  $archive = $user['archive'];
  $nomade = $obm_q->escape($user['nomade']);
  $phone = $obm_q->escape($user['phone']);
  $fax = $obm_q->escape($user['fax']);
  $exp = $user['exp'];
  $exp_op = $user['exp_op'];
  $delegation_target = $user['delegation_target'];
  $quota = $obm_q->escape($user['quota']);
  $quota_op = $obm_q->escape($user['quota_op']);
  $mail_server_host_name = $obm_q->escape($user['host_name']);
  $date_begin = ($user['datebegin'] ? "'".of_isodate_convert($user['datebegin'])."'" : '');
  $title = $obm_q->escape($user['title']);
  $direction = $obm_q->escape($user['direction']);
  $service = $obm_q->escape($user['service']);

  $db_type = $obm_q->type;
  $limit = sql_limit($db_type);
  $datebegin = sql_date_format($db_type, 'userobm_datebegin', 'datebegin');
  $dateexp = sql_date_format($db_type, 'userobm_account_dateexp', 'dateexp');
  $diff = "#DAYDIFF(now(),userobm_account_dateexp)";
  $lastaccess = sql_date_format($db_type, 'userobm_timelastaccess', 'timelastaccess');
  $multidomain = sql_multidomain('userobm');

  if(isset($user['status'])){
    $status = $user['status'];
  } else {
    $status = 'VALID';
  }

  if ($filter_entity == 'calendar') {
    $where_filter_entity = ' AND userobm_id IN ('.OBM_Acl::getAclSubselect('calendarentity_calendar_id', 'calendar', null, $obm['uid'], $filter_pattern).')';
  }

  $where = '1=1';

  // If a login indication has been specified, get it
  if (trim($login) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_login #LIKE '$login%'";
  }

  // If a user profile indication has been specified, get it
  if (($profile != $c_all) && ($profile != '')) {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_perms='$profile'";
  }

  // If a user domain indication has been specified, get it
  if (($domain_id != $c_all) && ($domain_id != '')) {
     if (trim($where) != '') $where .= ' AND';
     $domain_id = sql_parse_id($domain_id, true);
     $where .= " userobm_domain_id $domain_id ";
  }

  // If a commonname indication has been specified, get it
  if (trim($cname) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_commonname #LIKE '$cname%'";
  }

  // If a lastname indication has been specified, get it
  if (trim($lname) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_lastname #LIKE '$lname%'";
  }

  // If a lastname indication has been specified, get it
  if (trim($fname) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_firstname #LIKE '$fname%'";
  }

  // If a delegation indication has been specified, get it
  if (trim($delegation) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_delegation #LIKE '$delegation%'";
  }
  
  // If a status indication has been specified, get it
  if ($status != '') {
    if (trim($where) != '') $where .= ' AND';
    $where .= " userobm_status = '$status'";
  }

  // If an email indication has been specified, get it
  if (trim($email) != '') {
    if (trim($where) != '') $where .= ' AND';
    list($left,$right) = split('@',$email);
    if ($right === null){
      $left = $left.'%';
      $right = $left;
      $condition = 'OR';
    } else {
      $condition = 'AND';
    }
    if (!$left) {
      $left = '%';
    }
    $right = $right.'%';
    $where .= "
      userobm_email != '' AND (
          ( userobm_email #LIKE '$left@$right' OR userobm_email #LIKE '$left@$right\r\n%' OR userobm_email #LIKE '%\r\n$left@$right\r\n%' OR userobm_email #LIKE '%\r\n$left@$right' )
          OR (
          ( userobm_email #LIKE '$left' OR userobm_email #LIKE '$left\r\n%' OR userobm_email #LIKE '%\r\n$left\r\n%' OR userobm_email #LIKE '%\r\n$left' )
          $condition
          ( domain_name #LIKE '$right' OR domain_alias #LIKE '$right' OR domain_alias #LIKE '$right\r\n%' OR domain_alias #LIKE '%\r\n$right\r\n%' OR domain_alias #LIKE '%\r\n$right' ))
        ) ";
    $join_domain =  "INNER JOIN Domain ON userobm_domain_id = domain_id";

  }

  // If a phone number indication has been specified, get it
  if (trim($phone) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " (userobm_phone #LIKE '%$phone%'
       OR userobm_phone2 #LIKE '%$phone%'
       OR userobm_mobile #LIKE '%$phone%')";
  }

  // If a fax number indication has been specified, get it
  if (trim($fax) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " (userobm_fax #LIKE '%$fax%'
       OR userobm_fax2 #LIKE '%$fax%')";
  }

	// if a time of expiration has been specified, get it  
  if (trim($exp) != '') {
    $exp = sql_parse_int($exp);
    if (trim($where) != '') $where .= ' AND';
    switch($exp_op) {
      case '>':
      case '<':
      case '=':
        $where .= " $diff $exp_op $exp";
        break;
      default :
        $where .= " $diff < $exp";
        break;
    }    
  }

  // If a description indication has been specified, get it
  if (trim($desc) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_description #LIKE '%$desc%'";
  }

  // If a group indication has been specified, get it
  if (trim($group) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " group_name #LIKE '$group%'";
     $join_grp = 'INNER JOIN of_usergroup ON userobm_id=of_usergroup_user_id
                  INNER JOIN UGroup ON of_usergroup_group_id=group_id';
  }

  if (trim($date_begin) != '') {
    $date_begin = new Of_Date($date_begin);
    if (trim($where) != '') $where .= ' AND';
    $where .= " userobm_datebegin = '".$date_begin->getDateIso()."'";
  }

  // If a quota indication has been specified, get it
  if (trim($quota) != '') {
    $quota = sql_parse_int($quota);
    if (trim($where) != '') $where .= ' AND';
    switch($quota_op) {
      case '>':
      case '<':
        $where .= " userobm_mail_quota $quota_op '$quota'";
        break;
      default :
        $where .= " userobm_mail_quota = '$quota'";
        break;

    }
  }

  // If a delegation target indication has been specified, get it
  if (trim($delegation_target) != '') {
    if (trim($where) != '') $where .= ' AND';
    $where .= " userobm_delegation_target #LIKE '%$delegation_target%'";
  }

  // If a delegation target indication has been specified, get it
  if (trim($mail_server_host_name) != '') {
    if (trim($where) != '') $where .= ' AND';
    $where .= " host_name #LIKE '%$mail_server_host_name%'";
    $join_host = 'INNER JOIN Host ON host_id = userobm_mail_server_id';
  }

  // If an archive indication has been specified, get it
  if ($archive != '1') {
    if (trim($where) != '') $where .= ' AND';
    $where .= " userobm_archive = 0";
  } elseif($cgp_archive_only) {
    if (trim($where) != '') $where .= ' AND';
    $where .= " userobm_archive = 1";
  }

  // If a nomade indication has been specified, get it
  if (trim($nomade) != '') {
    if ($nomade != '1') {
      if (trim($where) != '') $where .= ' AND';
      $where .= " userobm_nomade_enable = 0";
    } else {
      if (trim($where) != '') $where .= ' AND';
      $where .= " userobm_nomade_enable = 1";
    }
  }

  // If a title indication has been specified, get it
  if (trim($title) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_title #LIKE '$title%'";
  }

  // If a direction indication has been specified, get it
  if (trim($direction) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_direction #LIKE '$direction%'";
  }

  
  // If a service indication has been specified, get it
  if (trim($service) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_service #LIKE '$service%'";
  }

  // User defined data
  $userdata = of_userdata_query_search('User', $user, 'UserObm');
  if ($userdata['where'] != '') {
    if (trim($where) != '') $where .= ' AND ';
    $where .= $userdata['where'];
    $join_userdata = $userdata['join'];
  }

  $join_profile = null;
  if ($strongest_profile_level != null) {
    $join_profile = ' JOIN Profile p ON userobm_perms = p.profile_name '.
      'JOIN ProfileProperty pp_level ON p.profile_id = pp_level.profileproperty_profile_id '.
        "AND pp_level.profileproperty_name='level' ";

    # Only list the users with a stronger profile level (the smaller values are
    # stronger than the bigger values, hence the > or >=)
    if ($user_can_manage_peers) {
      $where .= " AND (#CAST(pp_level.profileproperty_value, INTEGER) >= $strongest_profile_level) ";
    }
    else {
      $where .= " AND (#CAST(pp_level.profileproperty_value, INTEGER) > $strongest_profile_level) ";
    }
  }
  else {
    $join_profile = '';
  }

  // User custom fields
  $customFields = new OBM_Form_Field('user');
  $custom = $customFields->buildQuery($user, $obm_q);
  if ($custom != '') {
    $where .= $custom['where'];
    $join_custom = $custom['join'];
  }

  if ((trim($where) != '') || (trim($where_filter_entity) != '')) {
    $whereq = " WHERE $where $where_filter_entity $multidomain";
  } else {
    $whereq = " WHERE 1=1 $multidomain";
  }

  // Magic quota : the quota usage formula is userobm_mail_quota_use / userobm_mail_quota
  // But userobm_mail_quota can is an integer >= 0 and userobm_mail_quota_use / 0 > *
  // So the sort by formula can be (userobm_mail_quota_use / (userobm_mail_quota * 2 - 1))
  // -0.5 to prevent divized by 0 and to prevent quota = 1 (so quota - 1 => 0) and to force
  // float
  $select = "SELECT distinct UserObm.*,
      userobm_id as id,
      domain_label,
      domain_name,
     (userobm_mail_quota_use / (userobm_mail_quota  - 0.5)) as mockup_quota_used,
      $datebegin,
      $dateexp,
      $lastaccess
    FROM UserObm 
    INNER JOIN UserEntity ON userentity_user_id = userobm_id
    INNER JOIN Domain ON userobm_domain_id = domain_id
    $join_grp
    $join_host
    $join_userdata
    $join_custom
    $join_profile";


  // ORDER construction
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'userobm_login';
  if ($order == 'userobm_mail_quota') {
    $order = 'mockup_quota_used';
  } else if ($order == 'userobm_nomade') {
    $order = "userobm_nomade_perms $sql_order_dir, userobm_nomade_enable $sql_order_dir, userobm_nomade_local_copy";
  }

  $orderq .= " ORDER BY $order $sql_order_dir";

  $query = "$select
    $whereq
    $orderq
    $limit";
  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(distinct userobm_id) FROM UserObm INNER JOIN UserEntity ON userentity_user_id = userobm_id $join_grp $join_host $join_userdata $join_custom $join_domain $join_profile $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'run_query_user_search()');
    $obm_q->xquery($query);
  }

  return $obm_q;
}


/**
 * Search users from a single field 
 * 
 * @param mixed $user 
 * @access public
 * @return array of 'length' => DB user results nb, and 'datas' => DB User search query results
 */
function run_query_user_ext_search($user) {
  global $obm, $c_all, $cdg_sql, $ctu_sql_limit;

  $pattern = $user['pattern'];
  $filter_entity = $user['filter_entity'];
  $filter_pattern = $user['filter_pattern'];
  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain('UserObm.userobm');

  $userLimit = $user['limit'];
  $limit = $userLimit * 6; // This accounts for firstname, lastname, login and 3 emails per user
  if(isset($user['first_row'])) $first_row = $user['first_row'] - 1;
  else $first_row = 0;

  $patterns = splitwords($pattern);
  foreach ($patterns as $index => $pattern) {
    $pattern = strtolower($pattern);
    $innerpattern .= " INNER JOIN _userpattern  p$index ON p$index.id  = userobm_id ";
    $wherepattern .= " AND p$index.pattern #LIKE '$pattern%'";
  }

  if ($filter_entity == 'calendar') {
    $userEntities = OBM_Acl::getUserEntities($obm['uid']);
    $filterJoin = '
      INNER JOIN CalendarEntity ON calendarentity_calendar_id = userobm_id
      INNER JOIN EntityRight ON calendarentity_entity_id = entityright_entity_id
      ';
    $filterWhere = 'AND ( entityright_'.$filter_pattern.' = 1 AND (entityright_consumer_id IN ('.implode(',',$userEntities).') OR entityright_consumer_id IS NULL) OR userobm_id = '.$obm['uid'].')';    
  }

  $query = "SELECT DISTINCT * FROM (SELECT 
      userobm_id,
      userobm_commonname,
      userobm_lastname,
      userobm_firstname,
      userobm_hidden,
      userobm_email
    FROM UserObm
    $innerpattern
    $filterJoin
    WHERE
      1 = 1  
      $wherepattern
      $filterWhere
      AND userobm_archive = 0
      $multidomain
    LIMIT $limit OFFSET $first_row) AS unsorted ORDER BY userobm_id LIMIT $userLimit ";
  display_debug_msg($query, $cdg_sql, 'run_query_user_ext_search()');
  $obm_q->xquery($query);
  $rows =  array();
  while($obm_q->next_record()) {
    $rows[$obm_q->f("userobm_lastname")." ".$obm_q->f("userobm_firstname"). " ".$obm_q->f('userobm_id')] = $obm_q->Record;
  }
  $resultsCount = count($rows);
  ksort($rows);
  return array('length' => $resultsCount, 'datas' => $rows);
}


///////////////////////////////////////////////////////////////////////////////
// User detail query execution
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_user_detail($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, 'UserObm.userobm_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'UserObm.userobm_timecreate', 'timecreate');
  $datebegin = sql_date_format($db_type, 'UserObm.userobm_datebegin', 'datebegin');
  $dateexp = sql_date_format($db_type, 'UserObm.userobm_account_dateexp', 'dateexp');
  $lastaccess = sql_date_format($db_type, 'UserObm.userobm_timelastaccess', 'userobm_timelastaccess');
  $multidomain = sql_multidomain('UserObm.userobm');
  $p_id = sql_parse_id($p_id, true);	

  $query = "SELECT UserObm.*,
      $timeupdate,
      $timecreate,
      $datebegin,
      $dateexp,
      $lastaccess,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate,
      group_name,
      domain_label,
      domain_name,
      ms.host_name as mailserver_hostname,
      ms.host_id as mailserver_id,
      ms.host_ip as mailserver_ip,
      Host.host_name
    FROM UserObm
         LEFT JOIN UserObm as c ON UserObm.userobm_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON UserObm.userobm_userupdate=u.userobm_id
         LEFT JOIN UGroup on UserObm.userobm_gid=group_gid
                   AND UserObm.userobm_domain_id=group_domain_id
         LEFT JOIN Domain on UserObm.userobm_domain_id=domain_id
         LEFT JOIN Host on UserObm.userobm_host_id=Host.host_id
         LEFT JOIN Host ms on UserObm.userobm_mail_server_id=ms.host_id
    WHERE UserObm.userobm_id $p_id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_user_detail()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// User insert query execution
// Parameters:
//   - $user[]   : entry values
// Returns:
//   $id : new user id if ok, else false
///////////////////////////////////////////////////////////////////////////////
function run_query_user_insert($user) {
  global $obm, $cdg_sql, $cgp_use, $password_encryption, $perms_admin;
  global $cg_gid_smb_user, $cg_gid_user;
  $obm_q = new DB_OBM;

  $uid = sql_parse_id($obm['uid']);
  $login = strtolower($user['login']);
  $passwd = stripslashes($user['passwd']);
  if ($passwd != '') {
    // If a password type is given, get it else get the system parameter
    $passwd_type = $user['password_type'];
    if (($passwd_type == 'MD5SUM')
	|| ($passwd_type == 'CRYPT')
	|| ($passwd_type == 'PLAIN')) {
      $passwd_type = $passwd_type;
    } else {
      $passwd_type = $password_encryption;
    }

    if ($passwd_type == 'CRYPT') {
      $passwd_crypt = crypt($passwd);
    } else if ($passwd_type == 'MD5SUM') {
      $passwd_crypt = md5($passwd);
    } else {
      $passwd_crypt = $passwd;
    }
    $passwd_crypt = $obm_q->escape($passwd_crypt);
  }

  // infos
  $profile = $user['profile'];
  $archive = sql_parse_int($user['archive']);
  $hidden = sql_parse_int($user['hidden']);
  $kind = $user['kind'];
  $datebegin = of_isodate_convert($user['datebegin'], true, true);
  $dateexp = of_isodate_convert($user['dateexp'], true, true);
  $datebegin = ($datebegin ? "'".($datebegin)."'" : 'null');
  $dateexp = ($dateexp ? "'".($dateexp)."'" : 'null');
  $cname = $user['commonname'];
  $lname = $user['lastname'];
  $fname = $user['firstname'];
  $title = $user['title'];
  $email = $user['email'];
  $desc = $user['desc'];

  // Target Delegation handling (if updated user as delegation admin realm)
  $profiles = $user['profiles'];
  $admin_realm = $profiles[$profile]['properties']['admin_realm'];
  if ($GLOBALS['cgp_use']['property']['delegation']) {
    $delegation_target = $user['delegation_target'];
    $q_insert_delegation_target = ',
      userobm_delegation_target';
    $q_value_delegation_target = ",
      '$delegation_target'";
  }

  // Delegation handling
  $q_delegation = of_delegation_query_insert_clauses('userobm_delegation', $user['delegation']);

  // Coord
  $phone = $user['phone'];
  $phone2 = $user['phone2'];
  $mobile = $user['mobile'];
  $fax = $user['fax'];
  $fax2 = $user['fax2'];
  $company = $user['company'];
  $direction = $user['direction'];
  $service = $user['service'];
  $ad1 = $user['ad1'];
  $ad2 = $user['ad2'];
  $ad3 = $user['ad3'];
  $zip = $user['zip'];
  $town = $user['town'];
  $cdx = $user['cdx'];
  $ctry = $user['country'];

  // Section Web
  if ($cgp_use['service']['web']) {
    // Web access
    $host_name = $user['host_name'];
    $host_id = sql_parse_id($user['host_id']);
    $web_perms = sql_parse_int($user['web_perms']);
    $web_all = sql_parse_int($user['web_all']);
    $web_list = $user['web_list'];
    $query_insert_web = ',
    userobm_web_perms,
    userobm_web_all,
    userobm_web_list';
    $query_values_web = ",
    $web_perms,
    $web_all,
    '$web_list'";
  }

  // Section Mail
  if ($cgp_use['service']['mail']) {
    $mail_perms = sql_parse_int($user['mail_perms']);
    $mail_ext_perms = sql_parse_int($user['mail_ext_perms']);
    $mail_server_id = sql_parse_id($user['mail_server_id']);
    $mail_quota = $user['mail_quota'];
    if ($mail_quota == '') {
      $mail_quota = '0';
    }
    $query_insert_mail = ',
    userobm_mail_perms,
    userobm_mail_ext_perms,
    userobm_mail_server_id,
    userobm_mail_quota';
    $query_values_mail = ",
    $mail_perms,
    $mail_ext_perms,
    $mail_server_id,
    $mail_quota";
  }

  // Section Mail Nomad
  if ($cgp_use['service']['mail_nomad']) {
    $nomade_perms = sql_parse_id($user['nomade_perms']);
    $nomade_enable = sql_parse_id($user['nomade_enable']);
    $nomade_local_copy = sql_parse_id($user['nomade_local_copy']);
    $nomad_email = $user['email_nomade'];
    $query_insert_mail_nomad = ',
    userobm_email_nomade,
    userobm_nomade_perms,
    userobm_nomade_enable,
    userobm_nomade_local_copy';
    $query_values_mail_nomad = ",
    '$nomad_email',
    $nomade_perms,
    $nomade_enable,
    $nomade_local_copy";
  }
  
  // Section Mail Vacation
  if ($cgp_use['service']['mail_vacation']) {
    $vacation_enable = sql_parse_int($user['vacation_enable']);
    $vacation_message = $user['vacation_message'];
    $vacation_datebegin = ($user['vacation_datebegin'] ? "'$user[vacation_datebegin]'" : 'null');
    $vacation_dateend = ($user['vacation_dateend'] ? "'$user[vacation_dateend]'" : 'null');
    $vacation_message = $user['vacation_message'];
    $query_insert_mail_vacation = ",
    userobm_vacation_enable,
    userobm_vacation_datebegin,
    userobm_vacation_dateend,
    userobm_vacation_message";
    $query_values_mail_vacation = ",
    $vacation_enable,
    $vacation_datebegin,
    $vacation_dateend,
    '".$obm_q->escape($vacation_message)."'";
  }

  // Section Samba
  if ($cgp_use['service']['samba']) {
    $smb_perms = sql_parse_int($user['smb_perms']);
    $smb_home = ($smb_perms == 1)?$user['smb_home']:'';
    $smb_home_drive = ($smb_perms == 1)?$user['smb_home_drive'] : '';
    $smb_logon = ($smb_perms == 1)?$user['smb_logon']: '';
    $query_insert_samba = ',
    userobm_samba_perms,
    userobm_samba_home,
    userobm_samba_home_drive,
    userobm_samba_logon_script';
    $query_values_samba = ",
    $smb_perms,
    '$smb_home',
    '$smb_home_drive',
    '$smb_logon'";
  }

  // User UID/GID
  $user_uid = get_first_user_free_uid();
  if ($cgp_use['service']['samba'] && $smb_perms) {
    // Windows user
    $gid = $cg_gid_smb_user;
  } else {
    // Non windows user
    $gid = $cg_gid_user;
  }

  if (isset($user['domain_id']) && $obm['domain_global']) {
    $domain_id = sql_parse_id($user['domain_id']);
  } else {
    $domain_id = sql_parse_id($obm['domain_id']);
  }
  $query = "INSERT INTO UserObm (
    userobm_domain_id,
    userobm_timeupdate,
    userobm_timecreate,
    userobm_userupdate,
    userobm_usercreate,
    userobm_login,
    userobm_password,
    userobm_password_type,
    userobm_perms,
    userobm_uid,
    userobm_gid,
    userobm_archive,
    userobm_hidden,
    userobm_datebegin,
    userobm_account_dateexp,
    userobm_kind,
    userobm_commonname,
    userobm_lastname,
    userobm_firstname,
    userobm_title,
    userobm_company,
    userobm_direction,
    userobm_service,
    userobm_address1,
    userobm_address2,
    userobm_address3,
    userobm_zipcode,
    userobm_town,
    userobm_expresspostal,
    userobm_country_iso3166,
    userobm_phone,
    userobm_phone2,
    userobm_mobile,
    userobm_fax,
    userobm_fax2,
    userobm_email
    $q_insert_delegation_target
    $q_delegation[field],
    userobm_description
    $query_insert_web
    $query_insert_mail
    $query_insert_mail_nomad
    $query_insert_mail_vacation
    $query_insert_samba
  ) VALUES (
    $domain_id,
    NOW(),
    NOW(),
    $uid,
    $uid,
    '$login',
    '$passwd_crypt',
    '$passwd_type',
    '$profile',
    '$user_uid',
    '$gid',
    $archive,
    $hidden,
    $datebegin,
    $dateexp,
    '".$obm_q->escape($kind)."',
    '".$obm_q->escape($cname)."',
    '".$obm_q->escape($lname)."',
    '".$obm_q->escape($fname)."',
    '".$obm_q->escape($title)."',
    '".$obm_q->escape($company)."',
    '".$obm_q->escape($direction)."',
    '".$obm_q->escape($service)."',
    '".$obm_q->escape($ad1)."',
    '".$obm_q->escape($ad2)."',
    '".$obm_q->escape($ad3)."',
    '".$obm_q->escape($zip)."',
    '".$obm_q->escape($town)."',
    '".$obm_q->escape($cdx)."',
    '".$obm_q->escape($country)."',
    '".$obm_q->escape($phone)."',
    '".$obm_q->escape($phone2)."',
    '".$obm_q->escape($mobile)."',
    '".$obm_q->escape($fax)."',
    '".$obm_q->escape($fax2)."',
    '".$obm_q->escape($email)."'
    $q_value_delegation_target
    $q_delegation[value],
    '".$obm_q->escape($desc)."'
    $query_values_web
    $query_values_mail
    $query_values_mail_nomad
    $query_values_mail_vacation
    $query_values_samba
    )";

  display_debug_msg($query, $cdg_sql, 'run_query_user_insert()');
  $retour = $obm_q->query($query);

  if ($retour) {
    $id = $obm_q->lastid();
  } else {
    $id = false;
  }

  if ($id > 0) {
    // Insert Address book
    run_query_user_addressbook_insert($id, $domain_id);
    of_index_update('user', $id);
    $user['entity_id'] = $id;
    $entity_id = of_entity_insert($GLOBALS['module'], $id);
    $customFields = new OBM_Form_Field('user');
    $customFields->insert($id, $user);
    $entity_id = of_entity_insert('calendar', $id);    
    $entity_id = of_entity_insert('mailbox', $id);    
    run_query_user_set_default_rights($id, $profile, 'mailbox');
    run_query_user_set_default_rights($id, $profile, 'calendar');
    $ret = of_userdata_query_update('user', $id, $user);
    // Insert user's photo
    if (isset($user['file_tmp']) && $user['file_tmp'] != '') {
      $user['title'] = $login;
      $user['user_id'] = $id;
      $doc_id = run_query_global_insert_document($user, $id, 'user');
      if ($doc_id > 0) {
        // Update userobm_photo_id
        $doc_id = sql_parse_id($doc_id);
        $uid = sql_parse_id($id, true);
        $query = "UPDATE UserObm SET userobm_photo_id=$doc_id WHERE userobm_id $uid"; 
        display_debug_msg($query, $cdg_sql, 'run_query_user_insert(3)');
        $obm_q->query($query);
      }
    }
  }

  // Subscribe the user to his default group
  run_query_user_init_group($domain_id, $id, $gid);

  return $id;
}
/**
 * @see OBM_Acl::setDefaultPublicRights
 * Use the newly created user profile instead of the currently connected user profile.
 *
 *
 */
function run_query_user_set_default_rights($id, $profile, $module) {

    $confRights = $GLOBALS['profiles'][$profile]['properties']['default_right'][$module]['public'];
    $rights = array();
    if ($confRights['write'] == -2)
		$rights['write'] = 1;
	else
		$rights['write']  = ($confRights['write']  == -1) ? 0 : $confRights['write'];

    if ($confRights['read'] == -2)
		$rights['read'] = 1;
	else
		$rights['read']   = ($confRights['read']   == -1) ? 0 : $confRights['read'];
	
	if ($confRights['access'] == -2)
		$rights['access'] = 1;
	else
		$rights['access'] = ($confRights['access'] == -1) ? 0 : $confRights['access'];
    OBM_Acl::setConsumerRights('user', null, $module, $id, $rights);
}
/**
 * Insert default address book
 * address_book type:
 *   +contacts
 *   +collected_contacts
 *   +public_contacts
 */
function run_query_user_addressbook_insert($user_id, $domain_id) {
  global $cdg_sql, $obm;

  $uid = $obm['uid'];
  $obm_q = new DB_OBM;

  foreach($GLOBALS['c_addressbook_type'] as $addressbook_type => $syncable) {
    $query = "INSERT INTO AddressBook (
      domain_id,
      timeupdate,
      timecreate,
      userupdate,
      usercreate,
      origin,
      owner,
      name,
      is_default,
      syncable) VALUES (
      $domain_id,
      NOW(),
      NOW(),
      $uid,
      $uid,
      '$GLOBALS[c_origin_web]',
      $user_id,
      '".$obm_q->escape($addressbook_type)."',
      true,
      $syncable)";
    display_debug_msg($query, $cdg_sql, "run_query_user_addressbook_insert($addressbook_type)");
    $obm_q->query($query);
    $id = $obm_q->lastid();
    $query = "INSERT INTO SyncedAddressbook VALUES ($user_id, $id, NOW())"; 
    $obm_q->query($query);
    $entity_id = of_entity_insert('addressbook', $id);    

  }

}


///////////////////////////////////////////////////////////////////////////////
// Init user member of group
// Parameters:
//   - $u_id : user id
//   - $gid  : group GID (not id)
// Return: number of usergroup inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_user_init_group($domain_id, $u_id, $gid) {
  global $cdg_sql;

  $sel_user = sql_parse_id($u_id, true);
  $ins_user = sql_parse_id($u_id);

  // we get the group gid from its id
  $group_id = get_group_id_from_gid($domain_id, $gid);

  if ($group_id > 0) {
    $g_id = sql_parse_id($group_id, true);
    $query = "DELETE FROM UserObmGroup
    WHERE userobmgroup_userobm_id $sel_user 
      and userobmgroup_group_id $g_id";
    display_debug_msg($query, $cdg_sql, 'run_query_user_init_group()');
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);

    $g_id = sql_parse_id($group_id);
    $query = "INSERT INTO UserObmGroup (userobmgroup_group_id,
      userobmgroup_userobm_id)
    VALUES ($g_id, $ins_user)";
    display_debug_msg($query, $cdg_sql, 'run_query_user_init_group()');
    $retour = $obm_q->query($query);

    // Update internal group membership direct mapping
    of_usergroup_update_group_hierarchy($group_id);
  } else {
    // What to do if default User group does not exists ??
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// User update query execution
// Parameters:
//   - $u_id     : user id
//   - $user[]   : entry values
//     keys used : login, passwd, perms, archive, *name, email
///////////////////////////////////////////////////////////////////////////////
function run_query_user_update($u_id, $user) {
  global $obm, $cdg_sql, $cgp_use, $password_encryption, $perms_admin;
  global $cg_gid_smb_admin, $cg_gid_smb_user, $cg_gid_user;
  global $cmd_ldap_passwd;

  // Connect to the BD
  $obm_q = new DB_OBM;

  // infos
  $gid = $user['gid'];
  $profile = $user['profile'];
  $archive = sql_parse_int($user['archive']);
  $datebegin = of_isodate_convert($user['datebegin'], true, true);
  $dateexp = of_isodate_convert($user['dateexp'], true, true);
  $datebegin = ($datebegin ? "'".($datebegin)."'" : 'null');
  $dateexp = ($dateexp ? "'".($dateexp)."'" : 'null');  
  $hidden = sql_parse_int($user['hidden']);
  $kind = $user['kind'];
  $cname = $user['commonname'];
  $lname = $user['lastname'];
  $fname = $user['firstname'];
  $title = $user['title'];
  $email = $user['email'];
  $desc = $user['desc'];
  $user_update_q = "userobm_login='".strtolower($user['login'])."',";
  $multidomain = sql_multidomain('userobm');
  $photo_id = sql_parse_id($user['photo_id']);
  
  $u = get_user_info($u_id);
  $domain_id = $u['domain_id'];

  // Target Delegation handling (if updated user as delegation admin realm)
  $profiles = $user['profiles'];
  $admin_realm = $profiles[$profile]['properties']['admin_realm'];
  if ($GLOBALS['cgp_use']['property']['delegation']) {
    $delegation_target = $user['delegation_target'];
    $q_delegation_target = ",
  userobm_delegation_target='$delegation_target'";
  }

  // Delegation handling
  $q_delegation = of_delegation_query_update_clause('userobm_delegation', $user['delegation']);

  // Update group create by user Delegation
  update_private_groups_delegation($u_id, $user['delegation']);

  // Coord
  $phone = $user['phone'];
  $phone2 = $user['phone2'];
  $mobile = $user['mobile'];
  $fax = $user['fax'];
  $fax2 = $user['fax2'];
  $company = $user['company'];
  $direction = $user['direction'];
  $service = $user['service'];
  $ad1 = $user['ad1'];
  $ad2 = $user['ad2'];
  $ad3 = $user['ad3'];
  $zip = $user['zip'];
  $town = $user['town'];
  $cdx = $user['cdx'];
  $ctry = $user['country'];

  // Section Web
  if ($cgp_use['service']['web']) {
    // Web access
    $host_name = $user['host_name'];
    $host_id = sql_parse_id($user['host_id']);
    $web_perms = sql_parse_int($user['web_perms']);
    $web_all = sql_parse_int($user['web_all']);
    $web_list = $user['web_list'];
    $query_web = ",
    userobm_web_perms=$web_perms,
    userobm_web_all=$web_all,
    userobm_web_list='$web_list'";
  }

  // Section Mail
  if ($cgp_use['service']['mail']) {
    $mail_perms = sql_parse_int($user['mail_perms']);
    $mail_ext_perms = sql_parse_int($user['mail_ext_perms']);
    // Once mail server is assigned, it can't be modified for now
    if( !$u['mail_server_id'] ) {
      $mail_server_id = sql_parse_id($user['mail_server_id']);
    }else {
      $mail_server_id = $u['mail_server_id'];
    }
    $mail_quota = (isset($user['mail_quota']) ? $user['mail_quota'] : 0);
    $query_mail = ",
    userobm_mail_perms=$mail_perms,
    userobm_mail_ext_perms=$mail_ext_perms,
    userobm_mail_server_id=$mail_server_id,
    userobm_mail_quota=$mail_quota";
  }

  // Section Mail Nomad
  if ($cgp_use['service']['mail_nomad']) {
    $nomade_perms = sql_parse_int($user['nomade_perms']);
    $nomade_enable = sql_parse_int($user['nomade_enable']);
    $nomade_local_copy = sql_parse_int($user['nomade_local_copy']);
    $nomad_email = $user['email_nomade'];
    $query_mail_nomad = ",
    userobm_email_nomade='$nomad_email',
    userobm_nomade_perms=$nomade_perms,
    userobm_nomade_enable=$nomade_enable,
    userobm_nomade_local_copy=$nomade_local_copy";
  }

  // Section Mail Vacation
  if ($cgp_use['service']['mail_vacation']) {
    $vacation_enable = sql_parse_int($user['vacation_enable']);
    $vacation_message = $user['vacation_message'];
    $vacation_datebegin = ($user['vacation_datebegin'] ? "'$user[vacation_datebegin]'" : 'null');
    $vacation_dateend = ($user['vacation_dateend'] ? "'$user[vacation_dateend]'" : 'null');    
    $vacation_message = $user['vacation_message'];
    $query_mail_vacation = ",
    userobm_vacation_enable=$vacation_enable,
    userobm_vacation_datebegin= $vacation_datebegin,
    userobm_vacation_dateend= $vacation_dateend,
    userobm_vacation_message='".addslashes($vacation_message)."'";
  }

  // Section Samba
  if ($cgp_use['service']['samba']) {
    $smb_perms = sql_parse_int($user['smb_perms']);
    $smb_home = ($smb_perms == 1)?$user['smb_home']:'';
    $smb_home_drive = ($smb_perms == 1)?$user['smb_home_drive']:'';
    $smb_logon = ($smb_perms == 1)?$user['smb_logon']:'';
    $query_samba = ",
    userobm_samba_perms=$smb_perms,
    userobm_samba_home='$smb_home',
    userobm_samba_home_drive='$smb_home_drive',
    userobm_samba_logon_script='$smb_logon'";
  }


  // User UID/GID
  $userDesc = get_user_info($u_id);

  $old_gid = $userDesc['gid'];
  if ($cgp_use['service']['samba']) {
    if ($userDesc['samba_perms'] && !$smb_perms) {
      // Windows attribute just removed
      $gid = $cg_gid_user;
    } elseif (!$userDesc['samba_perms'] && $smb_perms) {
      // Windows attribute just added
      $gid = $cg_gid_smb_user;
    }
  }

  // If gid not set, give it an integer value
  if ($gid == '') {
    $gid = $cg_gid_user;
  }

  $passwd = stripslashes($user['passwd']);
  if ($passwd != '') {
    // If a password type is given, get it else get the system parameter
    $passwd_type = $user['password_type'];
    if (($passwd_type == 'PLAIN')
	|| ($passwd_type == 'CRYPT')
	|| ($passwd_type == 'PLAIN')) {
      $passwd_type = $passwd_type;
    } else {
      $passwd_type = $password_encryption;
    }
    if ($passwd_type == 'CRYPT') {
      $passwd_crypt = crypt($passwd);
    } else if ($passwd_type == 'MD5SUM') {
      $passwd_crypt = md5($passwd);
    } else {
      $passwd_crypt = $passwd;
    }
    $passwd_crypt = addslashes($passwd_crypt);

    $passwd_update = "userobm_password='$passwd_crypt', userobm_password_type='$password_encryption'";
    $passwd_update_q = $passwd_update.',';
  }
  $id = sql_parse_id($u_id, true);
  $query = "UPDATE UserObm SET
    userobm_timeupdate = NOW(),
    userobm_userupdate = $obm[uid],
    $passwd_update_q
    $user_update_q
    userobm_perms = '".$obm_q->escape($profile)."',
    userobm_gid = '$gid',
    userobm_archive = $archive,
    userobm_hidden = $hidden,
    userobm_datebegin = $datebegin,
    userobm_account_dateexp = $dateexp,
    userobm_kind = '".$obm_q->escape($kind)."',
    userobm_commonname = '".$obm_q->escape($cname)."',
    userobm_lastname = '".$obm_q->escape($lname)."',
    userobm_firstname = '".$obm_q->escape($fname)."',
    userobm_title = '".$obm_q->escape($title)."',
    userobm_phone = '".$obm_q->escape($phone)."',
    userobm_phone2 = '".$obm_q->escape($phone2)."',
    userobm_mobile = '".$obm_q->escape($mobile)."',
    userobm_fax = '".$obm_q->escape($fax)."',
    userobm_fax2 = '".$obm_q->escape($fax2)."',
    userobm_company = '".$obm_q->escape($company)."',
    userobm_direction = '".$obm_q->escape($direction)."',
    userobm_service = '".$obm_q->escape($service)."',
    userobm_address1 = '".$obm_q->escape($ad1)."',
    userobm_address2 = '".$obm_q->escape($ad2)."',
    userobm_address3 = '".$obm_q->escape($ad3)."',
    userobm_zipcode = '".$obm_q->escape($zip)."',
    userobm_town = '".$obm_q->escape($town)."',
    userobm_expresspostal = '".$obm_q->escape($cdx)."',
    userobm_email = '".$obm_q->escape($email)."',
    userobm_description = '".$obm_q->escape($desc)."'
    $q_delegation_target
    $q_delegation
    $query_web
    $query_mail
    $query_mail_nomad
    $query_mail_vacation
    $query_samba
  WHERE userobm_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_user_update()');
  $retour = $obm_q->query($query);



  if ($passwd != '') {
    if ($cgp_use['service']['ldap']) {
      $query = "UPDATE P_UserObm SET
        $passwd_update
        WHERE userobm_id $id ";

      display_debug_msg($query, $cdg_sql, 'run_query_user_update()');
      $retour = $obm_q->query($query);

      if ($retour) {
        // Get the user domain if admin in domain 0 or domain not set
        if ((!isset($obm['domain_id'])) || ($obm['domain_id'] == '') || ($obm['domain_global'])) {
          $query = "SELECT userobm_domain_id FROM UserObm WHERE userobm_id $id ";
          display_debug_msg($query, $cdg_sql, 'run_query_user_update()');
          $retour = $obm_q->query($query);
          $obm_q->next_record();
          $user_domain = $obm_q->f('userobm_domain_id');
        } else {
          $user_domain = $obm['domain_id'];
        }

        if ($retour) {
          $parameters = " --unix";

          if ($user['smb_perms'] && ($cgp_use['service']['samba'] || $password_encryption != 'PLAIN')) {
            $parameters .= " --samba";
          }

          if ($parameters) {
            $cmd = $cmd_ldap_passwd.$parameters." --login ".$user['login']." --domain-id ".$user_domain." --no-old --passwd '".$passwd."'";

            display_debug_msg($cmd, $cdg_exe, 'run_query_password_update()');
            exec($cmd);
          }
        }
      }
    }
  }

  if ($old_gid != $gid) {
    // Subscribe the user to his default group
    run_query_user_init_group($domain_id, $u_id, $gid);
  }

  // Update photo
  if (isset($user['file_tmp']) && $user['file_tmp'] != '') {
    $user['title'] = $user['login'];

    // Select user photo
    $query = "SELECT userobm_photo_id FROM UserObm WHERE userobm_id $id ";
    display_debug_msg($query, $cdg_sql, 'run_query_user_update(2)');
    $obm_q->query($query);
    $obm_q->next_record();

    // Update user's photo
    run_query_global_delete_document($obm_q->f('userobm_photo_id'));
    $doc_id = run_query_global_insert_document($user, $u_id, 'user');

    if ($doc_id > 0) {
      // Update userobm_photo_id
      $query = "UPDATE UserObm SET userobm_photo_id='$doc_id' WHERE userobm_id='$u_id'"; 
      display_debug_msg($query, $cdg_sql, 'run_query_user_update(3)');
      $obm_q->query($query);
     }

  } else {
    $doc_id = $user['photo_id'];
    if (empty($doc_id)) {
      // Select user photo
      $query = "SELECT userobm_photo_id FROM UserObm WHERE userobm_id $id ";
      $obm_q->query($query);
      $obm_q->next_record();
      $photo_id = $obm_q->f('userobm_photo_id');
      run_query_global_delete_document($photo_id);

      // Update userobm_photo_id
      $query = "UPDATE UserObm SET userobm_photo_id=NULL WHERE userobm_id $id "; 
      display_debug_msg($query, $cdg_sql, 'run_query_user_update(remove photo)');
      $obm_q->query($query);
    } 
  }

  if ($retour) {
    $customFields = new OBM_Form_Field('user');
    $customFields->insert($u_id, $user);
    $ret = of_index_update('user', $u_id);
    $ret = of_userdata_query_update('user', $u_id, $user);
  }

  if ($p_id == $obm['uid']) {
    $obm['login'] = $login;
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// User delete query execution
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_user_delete($p_id) {
  global $cdg_sql, $obm, $c_use_connectors;

  $multidomain = sql_multidomain('userobm');
  $customFields = new OBM_Form_Field('user');
  $customFields->delete($p_id);
  of_entity_delete($GLOBALS['module'], $p_id);
  of_entity_delete('mailbox', $p_id);
  of_entity_delete('calendar', $p_id);
  $ret = of_userdata_query_delete('user', $p_id);
  $obm_q = new DB_OBM;
  $id = sql_parse_id($p_id, true);
  $p_id = sql_parse_id($p_id);
  // Get some user infos
  $query = "SELECT userobm_photo_id,
      userobm_delegation,
      userobm_domain_id
    FROM UserObm
    WHERE userobm_id $id $multidomain";
  $obm_q->query($query);
  $obm_q->next_record();
  $photo_id = $obm_q->f('userobm_photo_id');
  $delegation = $obm_q->f('userobm_delegation');
  $domain_id = $obm_q->f('userobm_domain_id');

  // Remove user's photo (document & documententity)
  if ($photo_id > 0) {
    run_query_global_delete_document($photo_id);
  }

  // Remove user
  $query = "DELETE FROM UserObm
    WHERE userobm_id $id 
      $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete()');
  $retour = $obm_q->query($query);

  // If this user exists in Prod table, Insert it in Deleted table
  $query = "SELECT userobm_id FROM P_UserObm WHERE userobm_id $id ";
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete(auto)');
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    $uid = sql_parse_id($obm['uid']);
    $domain_id = sql_parse_id($domain_id);

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
      'UserObm',
      $p_id,
      NOW())";
    display_debug_msg($query, $cdg_sql, 'run_query_user_delete(auto)');
    $retour = $obm_q->query($query);
  }

  // If connectors in use
  if ($c_use_connectors) {
    $query = "INSERT INTO
      DeletedUser (deleteduser_user_id, deleteduser_timestamp)
      VALUES ($p_id, NOW())";
    display_debug_msg($query, $cdg_sql, 'run_query_user_delete(connector)');
    $retour = $obm_q->query($query);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// User Groups query execution
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_user_user_group($p_id) {
  global $cdg_sql, $obm;

  $p_id = sql_parse_id($p_id, true);
  $query = "SELECT *
    FROM UGroup
      LEFT JOIN UserObmGroup ON group_id = userobmgroup_group_id
    WHERE userobmgroup_userobm_id $p_id 
      AND (group_privacy=0 OR group_usercreate=$obm[uid])";

  display_debug_msg($query, $cdg_sql, 'run_query_user_user_group()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Unsubscribe the user from all groups
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_user_unsubscribe_all_groups($p_id) {
  global $cdg_sql;

  $p_id = sql_parse_id($p_id, true);
  $query = "DELETE
  FROM UserObmGroup
  WHERE userobmgroup_userobm_id $p_id";

  display_debug_msg($query, $cdg_sql, 'run_query_user_unsubscribe_all_groups()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Update internal usergroup mapping
  $query = "DELETE
  FROM of_usergroup
  WHERE of_usergroup_user_id $p_id ";
  display_debug_msg($query, $cdg_sql, 'run_query_user_unsubscribe_all_groups()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Groups query execution
///////////////////////////////////////////////////////////////////////////////
function run_query_user_public_owner_group($uid) {
  global $obm, $cdg_sql;

  $uid = sql_parse_id($uid, true);
  $query = "SELECT *
    FROM UGroup
    JOIN UserObm ON userobm_id $uid AND group_domain_id = userobm_domain_id
    WHERE
      (group_privacy=0 OR group_usercreate=$obm[uid])
    ORDER by group_privacy, group_name";

  display_debug_msg($query, $cdg_sql, 'run_query_user_public_owner_group()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Groups of User in DB query execution
///////////////////////////////////////////////////////////////////////////////
function run_query_group_of_user_from_database($uid) {
  global $obm, $cdg_sql;

  $uid = sql_parse_id($uid, true);
  $query = "SELECT *
    FROM UGroup
    JOIN UserObm ON userobm_id $uid AND group_domain_id = userobm_domain_id
    INNER JOIN UserObmGroup ON userobmgroup_group_id = group_id AND userobmgroup_userobm_id = userobm_id
    WHERE
      (group_privacy=0 OR group_usercreate=$obm[uid])
    ORDER by group_privacy, group_name";

  display_debug_msg($query, $cdg_sql, 'run_query_user_public_owner_group()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : UserObmGroup global update
// Parameters:
//   - $user[] : user hash info : keys used : user_id, group_nb, group_X
// Return: number of usergroup inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_user_update_user_group($user) {
  global $obm, $cdg_sql, $cg_gid_user, $cg_gid_smb_admin, $cg_gid_smb_user;

  // Hook : Pre
  if (function_exists('hook_pre_run_query_user_update_user_group')) {
    if (! hook_pre_run_query_user_update_user_group($user)) {
      return false;
    }
  }

  $u_id = $user['user_id'];

  $user_groups = array();
  $groups_in_db = array();
  $groups_to_add = array();
  $groups_to_remove = array();

  $q_groups_in_db = run_query_group_of_user_from_database($u_id);

  if ($q_groups_in_db->num_rows() > 0) {
    while ($q_groups_in_db->next_record()) {
      $group_id = $q_groups_in_db->f('group_id');
      $groups_in_db[] = $group_id;
    }
  }

  $cpt = 0;
  while ($cpt < $user['group_nb']) {
    $cpt++;
    $group_id = $user["group_$cpt"];
    $user_groups[] = $group_id;
  }

  $prepared_array = array_intersect($groups_in_db, $user_groups);
  $groups_to_add = array_diff($user_groups, $prepared_array);

  $groups_to_remove = array_diff($groups_in_db, $user_groups);

  $operations_done=0;

  if( count($groups_to_remove) > 0 ){
    $sql_groups_to_remove = '';
    foreach ($groups_to_remove as $g_id) {
      $operations_done++;
      $parsed_g_id = sql_parse_id($g_id);
      $sql_groups_to_remove .= ( $sql_groups_to_remove == '' ) ? $parsed_g_id : ', '.$parsed_g_id;
    }

    $query = "DELETE FROM UserObmGroup
              WHERE userobmgroup_userobm_id ".sql_parse_id($u_id, true)."
              AND userobmgroup_group_id IN ( $sql_groups_to_remove )";
    display_debug_msg($query, $cdg_sql, 'run_query_user_update_user_group()');
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
    if (!$retour) {
      return -1;
    }
  }

  if( count($groups_to_add) > 0){
    $sql_groups_to_add = '';
    $parsed_u_id = sql_parse_id($u_id);
    foreach ($groups_to_add as $g_id) {
      $operations_done++;
      $parsed_g_id = sql_parse_id($g_id);
      $sql_groups_to_add .= ( $sql_groups_to_add == '' ) ? "( $parsed_g_id, $parsed_u_id )" : ", ( $parsed_g_id, $parsed_u_id )" ;
    }
    $query ="INSERT INTO UserObmGroup ( userobmgroup_group_id, userobmgroup_userobm_id )
       VALUES $sql_groups_to_add ";
    display_debug_msg($query, $cdg_sql, 'run_query_user_update_user_group()');
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
    if (!$retour) {
      return -1;
    }
  }

  $need_update_hierarchy = array_merge($groups_to_add, $groups_to_remove);
  foreach ($need_update_hierarchy as $g_id) {
    of_usergroup_update_group_hierarchy($g_id);
  }

  // Register the user to its group GID
  $u = get_user_info($u_id);
  $gid = $u['gid'];
  $domain_id = $u['domain_id'];
  run_query_user_init_group($domain_id, $u_id, $gid);
  return $operations_done;
}


///////////////////////////////////////////////////////////////////////////////
// Return the number of admin user
// Returns:
//   - number of admin user
///////////////////////////////////////////////////////////////////////////////
function get_nb_user_admin() {
  global $cdg_sql, $perms_admin;

  $query = "SELECT count(*) as nb
    FROM UserObm
    WHERE userobm_perms='$perms_admin'";

  display_debug_msg($query, $cdg_sql, 'get_nb_user_admin()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $resultat = $obm_q->f('nb');

  return $resultat;
}


/**
 * Return the public group hash [name] => id
 **/
function get_user_group_public_hash() {
  global $cdg_sql;

  $groups = array();
  $query = "SELECT group_id, group_name
    FROM UGroup
    WHERE group_privacy=0";

  display_debug_msg($query, $cdg_sql, 'get_user_group_public_hash()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $id = $obm_q->f('group_id');
    $name = trim($obm_q->f('group_name'));
    $groups[$name] = $id;
  }

  return $groups;
}


///////////////////////////////////////////////////////////////////////////////
// Check if a login is present in the user table
// Parameters:
//   - $login : Login to test
//   - $u_id  : user Id to ignore (not to test current user in update)
// Returns:
//   - true if found, else false
///////////////////////////////////////////////////////////////////////////////
function check_user_login_in_use($domain_id, $login, $u_id='') {
  global $cdg_sql, $obm, $c_singleNameSpace;

  if (! ($domain_id > 0)) {
    $domain_id = $obm['domain_id'];
  }

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  if ($u_id != '') {
    $u_id = sql_parse_id($u_id, true, true);
    $where_uid = "AND userobm_id $u_id";
  }
  if (!$c_singleNameSpace) {
    $join_multidomain .= ' LEFT JOIN Domain on domain_id = userobm_domain_id ';
    $where_multidomain = "AND (domain_id = '$domain_id' OR domain_global = true)";
  }
  $query = "SELECT
      userobm_id,
      userobm_login
    FROM UserObm
    $join_multidomain
    WHERE userobm_login='$login' $where_multidomain 
      $where_uid";

  display_debug_msg($query, $cdg_sql, 'check_user_login_in_use()');
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// User environment checking (same user exists ?)
// Parameters:
//   - $p_id   : user id
//   - $user[] : user's values
// Returns:
//   - User Database object with list of similar users
///////////////////////////////////////////////////////////////////////////////
function check_user_context($p_id, $user) {

  $login = $user['login'];
  $passwd = $user['passwd'];
  $profile = $user['profile'];
  $lname = $user['lastname'];
  $fname = $user['firstname'];
  $phone = $user['phone'];
  $email = $user['email'];
  $domain_id = $user['domain_id'];

  // if a user with same name exists, return false
  $co_q = run_query_user_check_user($p_id, $user);

  return $co_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return the user with similar names
// Parameters:
//   - $p_id  : user id
//   - $login : login
///////////////////////////////////////////////////////////////////////////////
function run_query_user_check_user($p_id, $user) {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $multidomain = sql_multidomain('userobm');
  $login = strtolower($user['login']);
  $domain_id = $user['domain_id'];
  if ($domain_id == '') $domain_id = $obm['domain_id'];
  $domain_id = sql_parse_id($domain_id, true);
  $where_multidomain = " AND userobm_domain_id $domain_id ";

  if ($p_id) {
    $p_id = sql_parse_id($p_id, true, true);
    $where_user = "AND userobm_id $p_id ";
  }

  $query = "SELECT distinct userobm_id, userobm_login
    FROM UserObm
    WHERE userobm_login #LIKE '$login%'
    $where_user $where_multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_user_check_user()');
  $obm_q->xquery($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// User specific rights checking (eg : delegation)
// Parameters:
//   - $params[] : user values
// Returns:
//   - true if OK, else false
///////////////////////////////////////////////////////////////////////////////
function check_user_update_rights($params) {
  global $err, $cgp_use, $l_error_delegation;

  $id = $params['user_id'];

  if ($id > 0) {

    // Check if delegation is used
    if ($cgp_use['property']['delegation']) {
      $u = get_user_info($id);
      // Check delegation update right
      if (! of_delegation_check_update_rights($params, $u['delegation'])) {
	$err['msg'] .= $l_error_delegation;
	return false;
      }
    }
  }

  return true;
}

function profile_privilege_error() {
  global $err;
  global $l_profile_privilege_error;
  $err['msg'] = $l_profile_privilege_error;
  $err['field'] = 'profile';
}

///////////////////////////////////////////////////////////////////////////////
// Data checking and formatting
// Parameters:
//   - $p_id     : user id
//   - $user[]   : values checked
//     keys used : name, passwd, email
///////////////////////////////////////////////////////////////////////////////
function check_user_data_form($p_id, &$user) {
  global $l_exist_error,$l_login_error, $l_login_in_use;
  global $l_password_error, $l_password_empty;
  global $l_lname_error, $l_last_admin_error, $l_image_type_error;
  global $l_image_size_error, $l_email_error, $l_mail_server_error;
  global $l_email_exist, $l_uemail_exist;
  global $l_quota_error, $l_nomade_perms_error, $l_nomade_without_mail_perms;
  global $l_nomade_local_copy_error, $l_err_system_samba,$l_err_web_all;
  global $l_email_empty, $l_mail_perms_error;
  global $l_email_nomad_error, $l_email_nomad_error_loop;
  global $l_vacation_without_mail_perms, $l_empty_vacation_message;
  global $l_user_quota_error, $l_max_quota;
  global $php_regexp_login;
  global $php_regexp_email, $php_regexp_email_name, $php_regexp_quota;
  global $php_regexp_home, $php_regexp_drive, $l_err_home, $l_err_drive;
  global $php_regexp_password;
  global $err, $action, $cdg_sql, $gp_max_user, $perms_admin;
  global $obm, $cgp_use, $c_imagetype;
  global $c_max_image_size;
  global $l_err_delegation_outside_domain, $l_err_delegation_outside_target;
  
  // Hook : Pre
  if (function_exists('hook_pre_check_user_data_form')) {
    if (! hook_pre_check_user_data_form($p_id, $user)) {
      return false;
    }
  }

  $action = $user['action'];
  $id = $user['user_id'];
  $domain_id = $user['domain_id'];
  $login = strtolower($user['login']);
  $passwd = $user['passwd'];
  $profile = $user['profile'];
  $delegation = stripslashes($user['delegation']);
  $delegation_target = stripslashes($user['delegation_target']);
  $cname = $user['commonname'];
  $lname = $user['lastname'];
  $fname = $user['firstname'];
  $title = $user['title'];
  $email = stripslashes($user['email']);
  $phone = $user['phone'];
  $phone2 = $user['phone2'];
  $mobile = $user['mobile'];
  $fax = $user['fax'];
  $fax2 = $user['fax2'];
  $host_id = $user['host_id'];
  $web_perms = $user['web_perms'];
  $web_list = $user['web_list'];
  $web_all = ($user['web_all'] == '1' ? '1' : '0');
  $mail_perms = $user['mail_perms'];
  $mail_ext_perms = $user['mail_ext_perms'];
  $mail_server_id = $user['mail_server_id'];
  $mail_quota = $user['mail_quota'];
  $nomade_perms = $user['nomade_perms'];
  $nomade_enable = $user['nomade_enable'];
  $nomade_local_copy = $user['nomade_local_copy'];
  $vacation_enable = $user['vacation_enable'];
  $vacation_message = stripslashes($user['vacation_message']);
  $email_nomade = stripslashes($user['email_nomade']);
  $smb_perms = $user['smb_perms'];
  $smb_home = trim(stripslashes($user['smb_home']));
  $smb_home_drive = $user['smb_home_drive'];
  $smb_logon = trim($user['smb_logon']);
  $file = $user['file_tmp'];
  
  $profiles = $user['profiles'];
  if(!isset($profiles[$profile])) {
    $err['msg'] = $GLOBALS['l_err_profile']." : $profile";
    $err['field'] = 'profile';
    return false;
  }

  if (!Perm::user_profile_can_edit_peer_profile($profiles[$obm['profile']], $profiles[$profile])) {
    profile_privilege_error();
    return false;
  }

  if ($action == 'update') {
    $u = get_user_info($id);
    $usr_profile = $u['perms'];
    if (!Perm::user_profile_can_edit_peer_profile($profiles[$obm['profile']], $profiles[$usr_profile])) {
      profile_privilege_error();
      return false;
    }

    $domain_id = $user_info['domain_id'];

    // MANDATORY: we do not allow to update last admin'perms
    if (($usr_profile == $perms_admin) && ($profile != $perms_admin)) {
      $nb_admin = get_nb_user_admin();
      if ($nb_admin < 2) {
	$err['msg'] = "$l_last_admin_error";
	$err['field'] = 'profile';
	return false;
      }
    }
    // MANDATORY: we do not allow to delete samba perms for a system user
    $system = $u['system'];
    $usmb_perms = $u['samba_perms'];
    if (($system) && ($usmb_perms == '1') && ($smb_perms != '1')) {
      $err['msg'] = "$l_err_system_samba";
      $err['field'] = 'smb_perms';
      return false;
    }
  }

  // Get domain infos for further use
  $d = of_domain_get_domain_infos($domain_id, true);
  $domain_aliases = array($d['name']);
  $one_dom = strtok($d['alias'], "\r\n");
  while ($one_dom) {
    $domain_aliases[] = $one_dom;
    $one_dom = strtok("\r\n");
  }

  // MANDATORY: User login, not empty and unique
  if (trim($login) == '') {
    $err['msg'] = "$login : $l_login_error";
    $err['field'] = 'login';
    return false;
  } else {
    if (! preg_match($php_regexp_login, $login)) {
      $err['msg'] .= "$login : $l_login_error";
      $err['field'] = 'login';
      return false;
    }
    // Login not already in use
    if (check_user_login_in_use($domain_id, $login, $id)) {
      $err['msg'] = "$login : $l_login_in_use";
      $err['field'] = 'login';
      return false;
    }
  }

  // MANDATORY: User password on insert
  if (($action == 'insert' || $action == 'valid') && (trim($passwd) == '')) {
    $err['msg'] = $l_password_empty;
    $err['field'] = 'password';
    return false;
  }

  // If password is set, it must be valid
  if ((trim($passwd) != '') && (!preg_match($php_regexp_password, $passwd))) {
    $err['msg'] = $l_password_error;
    $err['field'] = 'password';
    return false;
  }

  // MANDATORY: Lastname
  if (trim($lname) == '') {
    $err['msg'] = $l_lname_error.' : '. $lname;
    $err['field'] = 'lastname';
    return false;
  }

  // Email Nomad perms
  if (($nomade_perms) && (!$mail_perms)) {
    $err['msg'] = $l_nomade_without_mail_perms;
    $err['field'] = 'nomade_perms';
    return false;
  }

  // Email Nomad perms
  if (($nomade_enable) && (!$nomade_perms)) {
    $err['msg'] = $l_nomade_perms_error;
    $err['field'] = 'nomade_perms';
    return false;
  }

  // User nomad email not null if active
  if ( ($email_nomade == '')
       && ($nomade_enable) ) {
    $err['msg'] = "$l_email_nomad_error";
    $err['field'] = 'email_nomade';
    return false;
  }

  // User nomad email
  if ($email_nomade != '') {
    $emails_nomade = array();
    $em = strtok(strtolower($email_nomade),"\r\n");
    while ($em) {

      if (!preg_match($php_regexp_email, $em)) {
        $err['msg'] = "$em : $l_email_error";
        $err['field'] = 'email_nomade';
        return false;
      }

      if (in_array($em, $emails_nomade)) {
        $err['msg'] = "$l_email_nomade : $l_uemail_exist : $em";
        $err['field'] = 'email_nomade';
        return false;
      }
      $emails_nomade[] = $em;

      $em = strtok("\r\n");
    }
  }

  // User nomade local copy only if autorized and activated
  if ( ($nomade_local_copy) && ((! $nomade_perms) || (! $nomade_enable)) )
  {
    $err['msg'] = $l_nomade_local_copy_error;
    $err['field'] = 'nomade_local_copy';
    return false;
  }

  // If external mail access is granted, internal access should be too
  if ($mail_ext_perms && !($mail_perms)) {
    $err['msg'] = "$l_mail_perms_error";
    $err['field'] = 'mail_ext_perms';
    return false;
  }

  // If no mail server is set
  if ( ($mail_perms) && ($mail_server_id == '') ) {
    $err['msg'] = "$l_mail_server_error";
    $err['field'] = 'mail_server';
    return false;
  }

  // If mail access is granted, there must be at least one email address
  if ($mail_perms && (trim($email) == '')) {
    $err['msg'] = "$l_email_empty";
    $err['field'] = 'email';
    return false;
  }

  // If Mail service activated
  if ($cgp_use['service']['mail']  && $mail_perms == 1) {
    // User email addresses (only the name before @)
    if ($email != '') {
      $um = array();

      // All email addresses given are valid and not already used
      $em = strtok(strtolower($email), "\r\n");
      while ($em) {

        if (! preg_match($php_regexp_email_name, $em) && !preg_match($php_regexp_email, $em)) {
          $err['msg'] .= "$em : $l_email_error";
          $err['field'] = 'email';
          return false;
        }

        // Email address not already used by this user
        if (in_array($em, $um)) {
          $err['msg'] = "$l_uemail_exist : $em";
          $err['field'] = 'email';
          return false;
        }
        $emails[] = $em;
        array_push($um, $em);
        $em = strtok("\r\n");
      }
      $mails = get_email_used($emails, $id);

      if (!empty($mails)) {
        $err['msg'] = "$l_email_exist : $mails ";
        $err['field'] = 'email';
        return false;
      }
    }

    // If nomad email, check if it doesn't loop to one of the user email
    // $um is the array of the user emails
    if ( ($email_nomade != '')
	 && ($nomade_enable) ) {

      foreach ($um as $one_email) {
	if (strpos($one_email, '@')) {
	  if (in_array($one_email, $emails_nomade)) {
	    $err['msg'] = "$l_email_nomad_error_loop";
	    $err['field'] = 'email_nomade';
	    return false;
	  }
	} else {
	  // email name (@ left side), check with all domain aliases
	  foreach ($domain_aliases as $one_dom) {
	    if (in_array($one_email.'@'.$one_dom, $emails_nomade)) {
	      $err['msg'] = "$l_email_nomad_error_loop";
	      $err['field'] = 'email_nomade';
	      return false;
	    }
	  }	    
	}
      }
    }
   // Get default quota on domain
   $max_quota = get_domain_property($obm['domain_id'], 'mail_quota');
   // Email quota check with profile max quota
   $max_profile_quota = $profiles[$profile]['properties']['mail_quota_max'];
   if($max_profile_quota != 0 && ($max_quota == 0 || $max_profile_quota < $max_quota)) $max_quota = $max_profile_quota;

   if(($mail_quota > $max_quota && $max_quota != 0)
     || ($mail_quota < $max_quota && $mail_quota == 0)) {
     $err['msg'] = "$l_user_quota_error ($l_max_quota : $max_quota)";
     $err['field'] = 'mail_quota';
     return false;
   }

  } else {
    // OBM-Mail disabled, email must be full (user@domain)
    if (($email != '') && (preg_match($php_regexp_email, $email) == 0)) {
      $err['msg'] = $l_email_error.' : '. $email;
      $err['field'] = 'email';
      return false;
    }
  }

  // Email quota
  if ( (trim($email)) && ($mail_quota != '')
       && (! preg_match($php_regexp_quota, $mail_quota))) {
    $err['msg'] = "$mail_quota : $l_quota_error";
    $err['field'] = 'mail_quota';
    return false;
  }

  if (($vacation_enable) && (!$mail_perms)) {
    $err['msg'] = $l_vacation_without_mail_perms;
    $err['field'] = 'vacation_enable';
    return false;
  }

  // If vacation is enable, the message must be set
  if ($vacation_enable && !$vacation_message) {
    $err['msg'] = $l_empty_vacation_message;
    $err['field'] = 'vacation_message';
    return false;
  }

  // Samba checks
  if ($cgp_use['service']['samba']) {
    // Samba home correct
    if ($smb_perms == 1 && ($smb_home != '' && !preg_match($php_regexp_home, $smb_home))) {
      $err['msg'] = " $smb_home : $l_err_home";
      $err['field'] = 'smb_home';
      return false;
    }

    // Samba home drive correct
    if ($smb_perms == 1 && ($smb_home_drive != '' && !preg_match($php_regexp_drive, $smb_home_drive))) {
      $err['msg'] = " $smb_home_drive : $l_err_drive";
      $err['field'] = 'smb_home_drive';
      return false;
    }
  }

  if ($host_id != '' && $host_id != 0) {
    $host_user = host_cannot_be_used_by_user($host_id,$id);
    // La machine existe, on verifie qu'elle n'est pas utilisee par quelqu'un d'autre
    if ($host_user > 0) {
      global $l_err_host_already_used_by;
      $login = get_user_using_host($host_id);
      $err['msg'] = " $l_err_host_already_used_by $login";
      $err['field'] = 'host_name';
      return false;
    }
  }

  // Gestion d'un poste par utilisateur : Squid local only
  if ($cgp_use['service']['web_local']) {
   // web_all must be 0 or 1
    if (($web_all != '0') && ($web_all != '1')) {
      $err['msg'] = $l_err_web_all;
      return false;
    }
  }

  // Photo
  if ($file != '') {
    // Test image mime-type
    $image_type = exif_imagetype($file);
    if(!$image_type || !array_key_exists($image_type, $c_imagetype)) {
      $err['msg'] = $l_image_type_error;
      $err['field'] = 'file';
      return false;
    }

    // Test image size
    $max_width = $c_max_image_size['width'];
    $max_height = $c_max_image_size['height'];
    $max_size = $c_max_image_size['size'];
    $image_info = getimagesize($file); 
    $width = $image_info['0'];
    $height = $image_info['1'];
    $size  = $user['img_size'];
    if ($width > $max_width || $height > $max_height || $size > $max_size) {
      $err['msg'] = $l_image_size_error;
      $err['field'] = 'file';
      return false;
    }
  }

  // XXXXXXX verifier ici + fichiers de langue
  $d = of_domain_get_domain_infos($domain_id, true);

  // Les contraintes sur la delegation ne sont pas valables pour le domaine global
  if (!$d['global'] && $cgp_use['property']['delegation']) {
    if (!of_delegation_check_data($delegation, $d['delegation'])) { 
      $err['field'] = 'delegation';
      return false;
    }
    if (!of_delegation_check_data($delegation_target, $d['delegation'])) {
      $err['field'] = 'delegation_target';
      return false;
    }
  }

  // Check the phones and faxes number are not the same
  if (!check_user_phone_fax($user)) {
    return false;
  }

  return true;
}

/**
 * Check the phones and faxes number are not the same
 *
 * @param array $user
 * @return boolean
 */
function check_user_phone_fax($user) {
  global $l_coord_same_phone_error, $l_coord_same_fax_error;
  global $err;
  
  $phone1 = (isset($user['phone']) ? trim($user['phone']) : '');
  $phone2 = (isset($user['phone2']) ? trim($user['phone2']) : '');
  $fax1 = (isset($user['fax']) ? trim($user['fax']) : '');
  $fax2 = (isset($user['fax2']) ? trim($user['fax2']) : '');
  
  if ($phone1 != '' && $phone2 != '' && $phone1 == $phone2) {
    $err['msg'] = $l_coord_same_phone_error;
    $err['field'] = 'phone2';
    return false; 
  }
  
  if ($fax1 != '' && $fax2 != '' && $fax1 == $fax2) {
    $err['msg'] = $l_coord_same_fax_error;
    $err['field'] = 'fax2';
    return false; 
  }
  
  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Return the number of timetask entries referencing to the user (and where
// project is set)
// Parameters:
//   - $p_id : user Id
///////////////////////////////////////////////////////////////////////////////
function get_linked_user_timetask_nb($p_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('userobm');

  $p_id = sql_parse_id($p_id, true);
  $obm_q = new DB_OBM;
  $query = "SELECT count(timetask_id) as nb
    FROM TimeTask
    JOIN UserObm ON timetask_user_id = userobm_id
    WHERE timetask_user_id $p_id 
      AND timetask_projecttask_id > 0
      $multidomain";

  display_debug_msg($query, $cdg_sql, "get_linked_user_timetask_nb($p_id)");
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f('nb');

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the user can be deleted
// Parameters:
//   - $params[] : user values
// Returns:
//   true if the user can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_user_can_delete($params) {
  //global $obm, $profiles, $err, $ok_msg;
  global $obm, $err, $ok_msg;
  global $l_profile_privilege_error, $l_link_timetask, $l_link_timetask_no;
  global $l_user_delete_himself_error;

  $profiles = $params['profiles'];
  
  $delete_ok = true;

  // Hook : Pre
  if (function_exists('hook_pre_check_user_can_delete')) {
    if (! hook_pre_check_user_can_delete($params)) {
      return false;
    }
  }

  // Check if this user is'nt to try to delete himself
  $current_uid = $obm['uid'];
  $uid = $params['user_id'];
  if ($uid == $current_uid) {
    $delete_ok = false;
    $err['msg'] .= $l_user_delete_himself_error;
  }

  // Links from Time management (referencing projects)
  $nb = get_linked_user_timetask_nb($params['user_id']);
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= "$l_link_timetask";
  } else {
    $ok_msg .= "$l_link_timetask_no";
  }

  // Check that user has delegation rights for this user
  if (! check_user_update_rights($params)) {
    $delete_ok = false;
  }

  $u = get_user_info($params['user_id']);
  $usr_profile = $u['perms'];

  // We can't delete a user with a more privileged profile
  // Or same privilege with 'impair' profile
  $level = $profiles[$obm['profile']]['properties']['level'];
  $managepeers = $profiles[$obm['profile']]['properties']['level_managepeers'];
  if (isset($profiles[$usr_profile]['properties']['level'])) {
    if (($profiles[$usr_profile]['properties']['level'] < $level)
	|| (($profiles[$usr_profile]['properties']['level'] == $level) && (! $managepeers))) {
      $err['msg'] = "$l_profile_privilege_error";
      $delete_ok = false;
    }
  }

  return $delete_ok;
}


///////////////////////////////////////////////////////////////////////////////
// Query Executions - Purge a deleted user
// Delete all references to the user (UserObmPref, DisplayPref, acl)
// Delete all user private contact
// Parameters:
//   - $user_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_user_delete_profile($user_id)  {
  global $cdg_sql, $l_attendee, $obm;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $u = get_user_info($user_id);
  $name = addslashes($u['firstname'] . ' ' . $u['lastname']);
  // Unsubscribe from all groups
  $retour = run_query_user_unsubscribe_all_groups($user_id);    
  $user_id = sql_parse_id($user_id, true);


#  $query = "DELETE FROM Entity 
#    WHERE entity_id IN (
#      SELECT contactentity_entity_id FROM ContactEntity INNER JOIN  Contact ON contactentity_contact_id = contact_id
#      WHERE contact_usercreate $user_id AND contact_privacy != 0)";
#  display_debug_msg($query, $cdg_sql, 'run_query_user_delete_profile(Contact)');
#  $obm_q->query($query);
#  // Delete all the user private contact
#  $query = "DELETE FROM Contact
#    WHERE contact_usercreate $user_id 
#      AND contact_privacy != 0"; 
#  display_debug_msg($query, $cdg_sql, 'run_query_user_delete_profile(Contact)');
#  $obm_q->query($query);

  // Move all the user deal from private to public
  $query = "UPDATE Deal SET
      deal_privacy=0
    WHERE deal_usercreate $user_id 
      AND deal_privacy != 0"; 
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete_profile(Deal)');
  $obm_q->query($query);

  // Move all the user list from private to public
  $query = "UPDATE List SET
      list_privacy=0
    WHERE list_usercreate $user_id 
      AND list_privacy != 0"; 
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete_profile(List)');
  $obm_q->query($query);

  // Update all events where user has participation (add user in comment)
  $cdesc[0]['type'] = 'string';
  $cdesc[0]['value'] = "$l_attendee : $name\r\n";
  $cdesc[1]['type'] = 'field';
  $cdesc[1]['value'] = 'event_description';
  $concat = sql_string_concat($db_type, $cdesc);
  $query = "UPDATE Event
    SET event_description = $concat,
      event_timeupdate = NOW() 
    WHERE event_owner $user_id 
      OR event_id IN (
        SELECT eventlink_event_id as event_id
        FROM EventLink
        INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id
        WHERE userentity_user_id $user_id )";
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete_profile(Event update)');
  $obm_q->query($query);

  $query = "DELETE FROM Entity 
    WHERE entity_id IN (
      SELECT evententity_entity_id FROM EventEntity 
      JOIN Event ON event_id = evententity_event_id
      LEFT JOIN EventLink ON event_id = eventlink_event_id
      WHERE event_owner $user_id AND evententity_event_id IS NULL)";
  display_debug_msg($query, $cdg_sql, "run_query_user_delete_profile(Orphan events)");
  $obm_q->query($query);

  // Remove all events without attendee from the user's calendar
  $query = "SELECT event_id FROM Event LEFT JOIN EventLink ON event_id = eventlink_event_id WHERE eventlink_event_id IS NULL AND event_owner $user_id";
  display_debug_msg($query, $cdg_sql, "run_query_user_delete_profile(Orphan events)");
  $obm_q->query($query);

  $obm_q2 = new DB_OBM;

  while($obm_q->next_record()) {
    $id = $obm_q->f('event_id');
    $query2 = "DELETE FROM Event WHERE event_id = $id"; 
    display_debug_msg($query2, $cdg_sql, "run_query_user_delete_profile(Orphan events)");
    $obm_q2->query($query2);
  }

  $query = "UPDATE Event SET event_owner = $obm[uid] WHERE event_owner $user_id";
  display_debug_msg($query, $cdg_sql, 'run_query_user_delete_profile(Event update)');
  $obm_q->query($query);
}


/**
 * Check if batch processing data form are not conflicting
 *
 * @param array $params
 * @return boolean
 */
function check_batch_processing_data($params, $user_id, $user_level, $user_can_manage_peers) {
  global $err;
  global $l_batch_no_user_error, $l_nomade_without_mail_perms, $l_nomade_perms_error, $l_nomade_local_copy_error, $l_quota_error;
  global $l_user,$l_mail_server_error, $l_users_not_updatable_error;
  global $php_regexp_quota, $php_regexp_phone, $php_regexp_fax, $php_regexp_zip,$php_regexp_number, $php_regexp_profile_name;
  global $l_phone_error, $l_fax_error, $l_zip_error, $l_expresspostal_error, $l_lname_error;
  
  $u_id = $params['data-user-id'];
  
  // Check if there are selected users
  if (!isset($u_id)) {
    $err['msg'] = $l_batch_no_user_error; 
    return false;    
  }
  
  
  // Check if data forms are not conflicting
  $mail_perms = $params['mail_perms'];
  $mail_quota = $params['mail_quota'];
  $nomade_perms = $params['nomade_perms'];
  $nomade_enable = $params['nomade_enable'];
  $nomade_local_copy = $params['nomade_local_copy'];
  
  if ($nomade_perms === '1' && $mail_perms === '0') {
    $err['msg'] = $l_nomade_without_mail_perms;
    return false;
  }
  
  if ($nomade_enable === '1' && $nomade_perms === '0') {
    $err['msg'] = $l_nomade_perms_error;
    return false;
  }
  
  if ($nomade_local_copy === '1' && ($nomade_perms === '0' || $nomade_enable === '0')) {
    $err['msg'] = $l_nomade_local_copy_error;
    return false;
  }
  
  if ( $mail_quota != '' && !preg_match($php_regexp_quota, $mail_quota)) {
    $err['msg'] = "$mail_quota : $l_quota_error";
    return false;
  }
  
  // check data

  // Profile
  if ($params['profile_name'] != '' && !preg_match($php_regexp_profile_name, $params['profile_name'])) {
    $err['msg'] = "$params[profile_name] : $l_lname_error";
    return false;
  }
  // Phone
  if ($params['phone'] != '' && !preg_match($php_regexp_phone, $params['phone'])) {
    $err['msg'] = "$params[phone] : $l_phone_error";
    return false;
  }
  
  // Phone2
  if ($params['phone2'] != '' && !preg_match($php_regexp_phone, $params['phone2'])) {
    $err['msg'] = "$params[phone2] : $l_phone_error";
    return false;
  }
  
  // Mobile
  if ($params['mobile'] != '' && !preg_match($php_regexp_phone, $params['mobile'])) {
    $err['msg'] = "$params[mobile] : $l_phone_error";
    return false;
  }
  
  // fax
  if ($params['fax'] != '' && !preg_match($php_regexp_fax, $params['fax'])) {
    $err['msg'] = "$params[fax] : $l_fax_error";
    return false;
  }
  
  // fax2
  if ($params['fax2'] != '' && !preg_match($php_regexp_fax, $params['fax2'])) {
    $err['msg'] = "$params[fax2] : $l_fax_error";
    return false;
  }
  
  // zipcode
  if ($params['zipcode'] != '' && !preg_match($php_regexp_zip, $params['zipcode'])) {
    $err['msg'] = "$params[zipcode] : $l_zip_error";
    return false;
  }
  
  // Express postal
  if ($params['expresspostal'] != '' && !preg_match($php_regexp_number, $params['expresspostal'])) {
    $err['msg'] = "$params[expresspostal] : $l_expresspostal_error";
    return false;
  }
  
  // Express postal
  if ($params['mail_perms'] == 1 && $params['mail_server_id'] == '') {
    $err['msg'] = "$l_mail_server_error";
    return false;
  }

  // Check the phones and faxes number are not the same
  if (!check_user_phone_fax($params)) {
    return false;
  }
  // XXXXXXX verifier ici + fichiers de langue
  
  $d = of_domain_get_domain_infos($GLOBALS['obm']['domain_id'], true);
  
  // Les contraintes sur la delegation ne sont pas valables pour le domaine global
  if (!$d['global'] && $GLOBALS['cgp_use']['property']['delegation']) {
    if ($params['to_update_delegation'] == 'true' && !of_delegation_check_data($params['delegation'], $d['delegation'])) { 
      $err['field'] = 'delegation';
      return false;
    }
    if ($params['to_update_delegation_target'] == 'true' && !of_delegation_check_data($params['delegation_target'], $d['delegation'])) {
      $err['field'] = 'delegation_target';
      return false;
    }
  }
  $updatable_users = Perm::updatable_users($u_id, $user_id, $user_level, $user_can_manage_peers);
  if (count($updatable_users) < count($u_id)) {
    $err['msg'] = $l_users_not_updatable_error;
    return false;
  }
  return true;
}

function find_not_updatable_users($user_ids, $user_id, $user_can_manage_peers) {
  $not_updatable_users = array();
  foreach ($user_ids as $user_id) {
  }
  return $users_not_updatable;
}

/**
 * Query execution - Return users that cannot be updated by batch processing
 *
 * @param array $params
 * @return $users_id_error
 */
function run_query_user_data_batch($params) {

  $mail_perms = $params['mail_perms'];
  $mail_quota = $params['mail_quota'];
  $nomade_perms = $params['nomade_perms'];
  $nomade_enable = $params['nomade_enable'];
  $nomade_local_copy = $params['nomade_local_copy'];
  $u_id = $params['data-user-id'];
  
  $check = array();
  
  if ($mail_perms == '1') {
    $check[] = "userobm_email = '' OR userobm_email IS NULL"; 
  }
  
  if ($nomade_perms == '1' && !isset($mail_perms)) {
    $check[] = "userobm_mail_perms = '' OR userobm_mail_perms IS NULL";
  }
  
  if ($nomade_enable == '1' && $nomade_perms == '1') {
    $check[] = "userobm_email_nomade = '' OR userobm_email_nomade IS NULL";
  }
  
  if ($nomade_enable == '1' && !isset($nomade_perms)) {
    $check[] = "userobm_nomade_perms = '' OR userobm_nomade_perms IS NULL OR userobm_email_nomade = '' OR userobm_email_nomade IS NULL";
  }
  
  if ($nomade_local_copy == '1' && (!isset($nomade_perms) || (!isset($nomade_enable)))) {
    $check[] = "userobm_nomade_perms = '' OR userobm_nomade_perms IS NULL OR userobm_nomade_enable = '' OR userobm_nomade_enable IS NULL";
  }
  
  $query = "";
  $users_id_error = array();
  if (sizeof($check) > 0) {
    $query = "SELECT userobm_id FROM UserObm WHERE (".implode(' OR ', $check).") AND userobm_id IN (".implode(', ', $u_id).")";
    $obm_q = new DB_OBM;
    
    display_debug_msg($query, $cdg_sql, 'run_query_user_data_batch()');
    $obm_q->query($query);
    while ($obm_q->next_record()) {
      $users_id_error[] = $obm_q->f('userobm_id');
    }
  }
  return $users_id_error;
}

/**
 * Check the type of error for each users that cannot be updated by batch processing
 * and return the error message to be displayed.
 *
 * @param unknown_type $params
 * @param unknown_type $users_id_error
 */
function check_users_error_data($params, $users_id_error) {
  global $err;
  global $l_user;
  global $l_email_empty, $l_nomade_without_mail_perms, $l_nomade_perms_error, $l_email_nomad_error, $l_nomade_local_copy_error;
  
  $mail_perms = $params['mail_perms'];
  $nomade_perms = $params['nomade_perms'];
  $nomade_enable = $params['nomade_enable'];
  $nomade_local_copy = $params['nomade_local_copy'];
  $check = array();

  // Users who haven't email address if mail perms are activated
  if ($mail_perms == '1') {
    $check['email'] = "userobm_email = '' OR userobm_email IS NULL";
  }
  
  // Users who haven't mail perms if nomade perms are activated
  if ($nomade_perms == '1' && !isset($mail_perms)) {
    $check['mail_perms'] = "userobm_mail_perms = '' OR userobm_mail_perms IS NULL";
  }
  
  // Users who haven't email nomade if nomade is enable or nomade perms are activated
  if ($nomade_enable == '1' && $nomade_perms == '1') {
    $check['email_nomade'] = "userobm_email_nomade = '' OR userobm_email_nomade IS NULL";
  }
  if ($nomade_enable == '1' && !isset($nomade_perms)) {
    $check['nomade_perms'] = "userobm_nomade_perms = '' OR userobm_nomade_perms IS NULL OR userobm_email_nomade = '' OR userobm_email_nomade IS NULL";
  }
  
  // Users who havent't nomade perms or nomade enable if nomade local copy is activated
  if ($nomade_local_copy == '1' && (!isset($nomade_perms) || (!isset($nomade_enable)))) {
    $check['nomade_local_copy'] = "userobm_nomade_perms = '' OR userobm_nomade_perms IS NULL OR userobm_nomade_enable = '' OR userobm_nomade_enable IS NULL";
  }
  
  $obm_q = new DB_OBM;
  $msg_error = "";
  foreach ($check as $err_type => $where) {
    $query = "SELECT userobm_login, userobm_nomade_perms FROM UserObm WHERE ($where) AND userobm_id IN(".implode(', ', $users_id_error).")";
    display_debug_msg($query, $cdg_sql, 'check_users_error_data()');
    $obm_q->query($query);
    while ($obm_q->next_record()) {
      if ($err_type == "email") { $err['msg'] = $l_email_empty; }
      if ($err_type == "mail_perms") {$err['msg'] = $l_nomade_without_mail_perms; }
      if ($err_type == "nomade_perms") {
        if ($obm_q->f('userobm_nomade_perms') == 1) {
          $err['msg'] = $l_email_nomad_error;
        } else {
          $err['msg'] = $l_nomade_perms_error;
        }
      }
      if ($err_type == "email_nomade") {$err['msg'] = $l_email_nomad_error; }
      if ($err_type == "nomade_local_copy") {$err['msg'] = $l_nomade_local_copy_error; }
      $msg_error .= $err['msg']."&nbsp;$l_user&nbsp;".$obm_q->f('userobm_login')."<br />";
    }
  }
  $err['msg'] = $msg_error;
}


/**
 * Query execution - Update data for each selected and valid users
 *
 * @param array $params
 * @param array $users_id
 * @return unknown
 */
function run_query_batch_processing_update($params, $users_id) {
  $obm_q = new DB_OBM;

  // user
  $archive = $params['archive'];
  $profile = $params["profile_name"];
  
  // Mail
  $mail_perms = $params['mail_perms'];
  $mail_quota = $params['mail_quota'];
  
  // Nomade
  $nomade_perms = $params['nomade_perms'];
  $nomade_enable = $params['nomade_enable'];
  $nomade_local_copy = $params['nomade_local_copy'];
  
  // Coords
  $is_phone_up = (strtolower($params['to_update_phone']) == 'true');
  $is_phone2_up = (strtolower($params['to_update_phone2']) == 'true');
  $is_mobile_up = (strtolower($params['to_update_mobile']) == 'true');
  $is_fax_up = (strtolower($params['to_update_fax']) == 'true');
  $is_fax2_up = (strtolower($params['to_update_fax2']) == 'true');
  $is_company_up = (strtolower($params['to_update_company']) == 'true');
  $is_direction_up = (strtolower($params['to_update_direction']) == 'true');
  $is_service_up = (strtolower($params['to_update_service']) == 'true');
  $is_address1_up = (strtolower($params['to_update_address1']) == 'true');
  $is_address2_up = (strtolower($params['to_update_address2']) == 'true');
  $is_address3_up = (strtolower($params['to_update_address3']) == 'true');
  $is_zipcode_up = (strtolower($params['to_update_zipcode']) == 'true');
  $is_town_up = (strtolower($params['to_update_town']) == 'true');
  $is_delelagation = (strtolower($params['to_update_delegation']) == 'true');
  $is_delelagation_target= (strtolower($params['to_update_delegation_target']) == 'true');
  $is_expresspostal_up = (strtolower($params['to_update_expresspostal']) == 'true');
  $phone = "".$params['phone'];
  $phone2 = "".$params['phone2'];
  $mobile = "".$params['mobile'];
  $fax = "".$params['fax'];
  $fax2 = "".$params['fax2'];
  $company = "".$params['company'];
  $direction = "".$params['direction'];
  $service = "".$params['service'];
  $address1 = "".$params['address1'];
  $address2 = "".$params['address2'];
  $address3 = "".$params['address3'];
  $zipcode = "".$params['zipcode'];
  $town = "".$params['town'];
  $expresspostal = $params['expresspostal'];
  $delegation_target = $params[delegation_target] ;
  $delegation = $params[delegation];
  $set = array();
  
  if(sizeof($users_id) == 0) return true;
  // User
  if (isset($archive)) { $set[] = "userobm_archive=$archive"; }
  if (isset($profile)) { $set[] = "userobm_perms='$profile'"; }
  
  // Mail
  if (isset($mail_perms)) {
    $set[] = "userobm_mail_perms=$mail_perms";
    if ($mail_perms == '0') {
      $set[] = "userobm_vacation_enable=0";
      $set[] = "userobm_nomade_perms=0";
      $set[] = "userobm_nomade_enable=0";
      $set[] = "userobm_nomade_local_copy=0";
      $set[] = "userobm_mail_server_id=NULL";
    } else {
      $queries[] = "UPDATE UserObm SET userobm_mail_server_id = $params[mail_server_id] WHERE userobm_mail_perms = 1 AND userobm_mail_server_id IS NULL";
    }
  } 
  if (isset($mail_quota)) { $set[] = "userobm_mail_quota=$mail_quota"; }
  
  // Nomade
  if (isset($nomade_perms)) { $set[] = "userobm_nomade_perms=$nomade_perms"; }
  if (isset($nomade_enable)) { $set[] = "userobm_nomade_enable=$nomade_enable"; }
  if (isset($nomade_local_copy)) { $set[] = "userobm_nomade_local_copy=$nomade_local_copy"; }
  
  // Coords
  if ($is_phone_up) { $set[] = "userobm_phone='".$obm_q->escape($phone)."'"; }
  if ($is_phone2_up) { $set[] = "userobm_phone2='".$obm_q->escape($phone2)."'"; }
  if ($is_mobile_up) { $set[] = "userobm_mobile='".$obm_q->escape($mobile)."'"; }
  if ($is_fax_up) { $set[] = "userobm_fax='".$obm_q->escape($fax)."'"; }
  if ($is_fax2_up) { $set[] = "userobm_fax2='".$obm_q->escape($fax2)."'"; }
  if ($is_company_up) { $set[] = "userobm_company='".$obm_q->escape($company)."'"; }
  if ($is_direction_up) { $set[] = "userobm_direction='".$obm_q->escape($direction)."'"; }
  if ($is_service_up) { $set[] = "userobm_service='".$obm_q->escape($service)."'"; }
  if ($is_address1_up) { $set[] = "userobm_address1='".$obm_q->escape($address1)."'"; }
  if ($is_address2_up) { $set[] = "userobm_address2='".$obm_q->escape($address2)."'"; }
  if ($is_address3_up) { $set[] = "userobm_address3='".$obm_q->escape($address3)."'"; }
  if ($is_zipcode_up) { $set[] = "userobm_zipcode='".$obm_q->escape($zipcode)."'"; }
  if ($is_town_up) { $set[] = "userobm_town='".$obm_q->escape($town)."'"; }
  if ($is_expresspostal_up) { $set[] = "userobm_expresspostal='".$obm_q->escape($expresspostal)."'"; }
  if ($is_delelagation_target ) { $set[] = "userobm_delegation_target = '$delegation_target'";}
  if ($is_delelagation ) { $set[] = "userobm_delegation = '".$obm_q->escape($delegation)."'";}
  
  
  // Get users who have actual value of there archive state different of archive state data form 
  if (isset($archive)) {
    $users = array();
    $query = "SELECT userobm_id FROM UserObm WHERE userobm_id IN (".implode(', ', $users_id).") AND userobm_archive != $archive";
    display_debug_msg($query, $cdg_sql, 'run_query_batch_processing_update()');
    $obm_q->query($query);
    while($obm_q->next_record()) {
      $users[] = $obm_q->f('userobm_id');
    }
  }
  
  // Update users data
  if (sizeof($set) > 0) {
    $query = "UPDATE UserObm SET ".implode(', ', $set)." WHERE userobm_id IN (".implode(', ', $users_id).")";
    display_debug_msg($query, $cdg_sql, 'run_query_batch_processing_update()');
    $retour = $obm_q->query($query);
    if(is_array($queries)) {
      foreach($queries as $query) {
        display_debug_msg($query, $cdg_sql, 'run_query_batch_processing_update()');
        $retour = $obm_q->query($query." AND  userobm_id IN (".implode(', ', $users_id).")");
      }
    }
  } else {
    return true;
  }
  
  // Update users/group links in of_usergroup table
  if ($retour) {
    if (isset($users) && count($users) > 0) {
      if ($archive == '1') {
        of_usergroup_delete_user_hierarchy($users, true);
      } else if ($archive == '0') {
        of_usergroup_update_user_hierarchy($users, true);
      }
    }
  }
  
  return $retour;
}


// FUNCTION - automatic mail server chooser hooks
function has_mail_server_auto () {
  global $obm;
  
  {
    $obm_q = new DB_OBM();
    $query = "SELECT serviceproperty_value as domain_mail_server_auto FROM ServiceProperty 
      INNER JOIN DomainEntity ON serviceproperty_entity_id = domainentity_entity_id 
      WHERE domainentity_domain_id = $obm[domain_id] AND serviceproperty_property = 'imap_autoselect'";
    display_debug_msg($query, $cdg_sql, 'has_mail_server_auto()');
    $obm_q->query($query);
    
    if ($obm_q->num_rows() == 1) {
      $obm_q->next_record();
      if ($obm_q->f('domain_mail_server_auto')) return true;
    }
  }
  
  return false;
}


// FUNCTION - automatic mail server chooser hooks
function get_user_params_mail_server_id ($params) {
  global $cdg_sql, $obm, $path;
  global $c_mailchooser_global_hooks;

  // Only handles mail server 'auto'
  if ($params['mail_server_id'] != 'auto') return $params;
  
  $hooks = $c_mailchooser_global_hooks;
  
  if (file_exists("$path/../conf/hooks/user/mailchooser.inc")) {
    require_once("$path/../conf/hooks/user/mailchooser.inc");
    $hooks = array_merge($hooks, $c_mailchooser_hooks);
  }
  
  unset($params['mail_server_id']);
  
  if (isset($params['domain']) && $params['domain_global']) {
    $domain_id = sql_parse_id($params['domain']);
  } else {
    $domain_id = sql_parse_id($obm['domain_id']);
  }
  
  $obm_q = new DB_OBM();
  $query = "SELECT serviceproperty_value as domain_mail_server_auto
  FROM ServiceProperty
    INNER JOIN DomainEntity ON serviceproperty_entity_id=domainentity_entity_id
  WHERE domainentity_domain_id = $domain_id
    AND serviceproperty_property = 'imap_autoselect'";
  display_debug_msg($query, $cdg_sql, 'get_user_params_mail_server_id()');
  $obm_q->query($query);
  
  if ($obm_q->num_rows() == 1) {
    $obm_q->next_record();
    
    if ($obm_q->f('domain_mail_server_auto')) {
      foreach ($hooks as $hook) {
        if ($hook['id'] == $obm_q->f('domain_mail_server_auto')) {
          $hook_function = 'hook_user_mailchooser_'. $hook['hook_function'];
          $hook_result = $hook_function($params);
        }
      }
    }
  }

  if ($hook_result) {
    if (isset($hook_result['host_id'])) {
      $params['mail_server_id'] = $hook_result['host_id'];
    } else {
      $query = "SELECT host_id FROM Host 
        WHERE host_name = '$hook_result[hostname]'";
      display_debug_msg($query, $cdg_sql, 'get_user_params_mail_server_id(search)');
      $obm_q->query($query);
      
      if ($obm_q->num_rows() == 1) {
        $obm_q->next_record();
        $params['mail_server_id'] = $obm_q->f('host_id');
      }
    }
  }

  return $params;
}


// FUNCTION - automatic mail server chooser static hook
function hook_user_mailchooser_least_boxes () {
  global $cdg_sql, $obm;
  
  $sel_id = sql_parse_id($obm['domain_id'], true);
  
  $query = "
    SELECT
      host_id,
      COUNT(*) AS mailbox_count
    FROM ServiceProperty
    INNER JOIN DomainEntity ON domainentity_entity_id = serviceproperty_entity_id
    INNER JOIN Host ON host_id = #CAST(serviceproperty_value, INTEGER)
    LEFT JOIN UserObm ON userobm_mail_server_id = host_id
    WHERE serviceproperty_property = 'imap' and serviceproperty_service = 'mail'
    AND domainentity_domain_id $sel_id 
    GROUP BY host_id
    ORDER BY mailbox_count ASC";

  display_debug_msg($query, $cdg_sql, 'hook_user_mailchooser_least_boxes()');
  $obm_q = new DB_OBM;
  $obm_q->xquery($query);
  $obm_q->next_record();

  return array('host_id' => $obm_q->f('host_id'));
}


// FUNCTION - automatic mail server chooser static hook
function hook_user_mailchooser_least_used_memory () {
  global $cdg_sql, $obm;
  
  $sel_id = sql_parse_id($obm['domain_id'], true);
  
  $query = "SELECT
      host_id,
      SUM(userobm_mail_quota_use) AS used_memory
    FROM ServiceProperty
    INNER JOIN DomainEntity ON domainentity_entity_id = serviceproperty_entity_id
    INNER JOIN Host ON host_id = #CAST(serviceproperty_value, INTEGER)
    LEFT JOIN UserObm ON userobm_mail_server_id = host_id
    WHERE serviceproperty_property = 'imap' and serviceproperty_service = 'mail'
      AND domainentity_domain_id $sel_id
    GROUP BY host_id
    ORDER BY used_memory ASC
    ";
  
  display_debug_msg($query, $cdg_sql, 'hook_user_mailchooser_least_used_memory()');
  $obm_q = new DB_OBM;
  $obm_q->xquery($query);
  $obm_q->next_record();
  
  return array('host_id' => $obm_q->f('host_id'));
}


/**
 * Get domain samba properties 
 * 
 * @param mixed $id 
 * @access public
 * @return void
 */
function run_query_user_samba_properties() {
  global $obm, $cdg_sql;
  if($obm['domain_global'] == true) {
    return array();
  }
  $db = new DB_OBM;
  $query = "SELECT * FROM ServiceProperty 
    INNER JOIN DomainEntity ON domainentity_entity_id = serviceproperty_entity_id
    WHERE serviceproperty_service = 'samba' and domainentity_domain_id = '$obm[domain_id]'";
  display_debug_msg($query, $cdg_sql, 'run_query_domain_samba_properties()');
  $db->query($query);
  $samba = array();
  while($db->next_record()) {
    $samba[$db->f('serviceproperty_property')] = $db->f('serviceproperty_value');
  }
  return $samba;
}


///////////////////////////////////////////////////////////////////////////////
// Check user who wait to be valid
// Parameters:
//   - $user[] : user search criteria
// Returns : true if some user wait or false
///////////////////////////////////////////////////////////////////////////////
function check_user_wait($user){
  global $obm;

  $rep = false;
  
  $user['archive'] = 1;
  $user['status'] = 'INIT';
  $user['delegation'] = $obm['delegation_target'];

  $obm_q = run_query_user_search($user);  
  
  if ($obm_q->num_rows() > 0){
    $rep = true;
  }

  return $rep;
}


///////////////////////////////////////////////////////////////////////////////
// Query Executions - Valid a INIT user
// Unarchive the user an update the status to VALID
// Parameters:
//   - $user_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_user_valid($user_id){

  $status = 'VALID';
  $archive = 0;

  $query = "
    UPDATE UserObm SET
    userobm_status = '$status',
    userobm_archive = $archive
    WHERE userobm_id = $user_id";

  $obm_q = new DB_OBM;

  display_debug_msg($query, $cdg_sql, 'run_query_user_valid()');
  $retour = $obm_q->query($query);

  return $retour;
}


/**
 * Get mobile devices
 * 
 * @param mixed $id 
 * @access public
 * @return void
 */
function run_query_userobm_get_devices($user_id) {
  global $cdg_sql;

  $uid = $obm['uid'];
  $obm_q = new DB_OBM;

  $query = "SELECT 
    opush_device.id,
    opush_device.owner,
    opush_device.identifier,
    opush_device.type, 
    MIN(f.last_sync) as first_sync,
    MAX(f.last_sync) as last_sync,
    opush_sync_perms.device_id, 
    opush_sync_perms.policy 
    FROM opush_device 
    LEFT JOIN opush_sync_state f ON f.device_id=opush_device.id
    LEFT JOIN opush_sync_perms ON opush_sync_perms.device_id = opush_device.id
    WHERE opush_device.owner='$user_id'
    GROUP BY opush_device.id, opush_device.owner, opush_device.identifier,opush_device.type,opush_sync_perms.device_id, opush_sync_perms.policy
    ORDER BY identifier";

  display_debug_msg($query, $cdg_sql, 'run_query_userobm_get_devices()');
  $obm_q->query($query);

  return $obm_q;
}


/**
 * Add mobile partnership
 * 
 * @param mixed $id 
 * @access public
 * @return void
 */
function run_query_userobm_mobile_add_partnership($user) {
  $obm_q = new DB_OBM;

  $owner = $user['owner'];
  $device_id = $user['device_id'];

  $query = "INSERT INTO opush_sync_perms(
    owner,
    device_id,
    policy,
    pending_accept) VALUES(
    '".$obm_q->escape($owner)."',
    '$device_id',
    NULL,
    false)";
  $obm_q->query($query);
}


/**
 * Remove mobile partnership
 * 
 * @param mixed $id 
 * @access public
 * @return void
 */
function run_query_userobm_mobile_remove_partnership($user) {

  $owner = $user['owner'];
  $device_id = $user['device_id'];

  $query = "DELETE FROM opush_sync_perms WHERE owner='$owner' AND device_id='$device_id';";
  $obm_q = new DB_OBM;
  $obm_q->query($query);

}


/**
 * unlink mobile 
 * 
 * @param mixed $id 
 * @access public
 * @return void
 */
function run_query_userobm_mobile_unlink($user) {
  $owner = $user['owner'];
  $device_id = $user['device_id'];

  $query = "DELETE FROM opush_device WHERE owner='$owner' AND id='$device_id'";
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


/**
 * Check that user hasn't pending changes since last automate update
 */
function check_user_can_backup($user_id) {

  $id = sql_parse_id($user_id, true);
  $query = "SELECT u.userobm_id
    FROM P_UserObm p
    INNER JOIN UserObm u ON p.userobm_id=u.userobm_id AND p.userobm_email=u.userobm_email
    WHERE u.userobm_id $id";
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return ($obm_q->num_rows()==1);
}

function update_private_groups_delegation($userid, $delegation){
  // Delegation handling
  $q_delegation = of_delegation_query_update_clause('group_delegation', stripslashes($delegation));

  $query = "UPDATE UGroup SET group_timeupdate=NOW() $q_delegation WHERE group_usercreate='$userid' AND group_privacy='1'";

  display_debug_msg($query, $cdg_sql, 'update_private_groups_delegation()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}

?>
