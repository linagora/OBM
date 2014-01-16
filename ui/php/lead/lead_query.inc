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
// OBM - File : lead_query.inc 
//     - Desc : lead query File
// 2006-05-19 Aliacom PB
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Lead search query execution
// Parametes:
//   - $lead[]       : lead search criteria
///////////////////////////////////////////////////////////////////////////////
function run_query_lead_search($params) {
  global $cdg_sql, $c_all, $c_null, $c_undef, $ctu_sql_limit, $cgp_archive_only;
  $obm_q = new DB_OBM;

  $sql_order_dir = $params['sql_order_dir'];
  $sql_order_field = $params['sql_order_field'];

  $text = sql_search_text_parse($obm_q->escape($params['text']));
  $company_id = $params['company_id'];
  $company = sql_search_text_parse($obm_q->escape($params['company']));
  $date_after = of_isodate_convert($params['date_after'],true);
  $date_before = of_isodate_convert($params['date_before'],true);
  $date_field = $params['date_field'] ? $params['date_field'] : 'date';
  $priority = $params['priority'];
  $source = $params['source'];
  $status = $params['status'];
  $manager = $params['manager'];
  $archive = $params['archive'];
  $delegation = sql_search_text_parse($obm_q->escape($params['delegation']));

  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $multidomain = sql_multidomain('lead');
	
  $date = sql_date_format($db_type, 'lead_date', 'date');
  $datealarm = sql_date_format($db_type, 'lead_datealarm', 'datealarm');

  $manager_name[] = array('type' => 'field', 'value' => 'userobm_lastname');
  $manager_name[] = array('type' => 'string', 'value' => ' ');
  $manager_name[] = array('type' => 'field', 'value' => 'userobm_firstname');
  $concat_manager = sql_string_concat($db_type, $manager_name);

  $contact_name[] = array('type' => 'field', 'value' => 'contact_lastname');
  $contact_name[] = array('type' => 'string', 'value' => ' ');
  $contact_name[] = array('type' => 'field', 'value' => 'contact_firstname');
  $concat_contact = sql_string_concat($db_type, $contact_name);

  $where .= sql_obm_entity_privacy('lead');

  $and = 'AND';

  // search criterias :
  if ($text != '') {
    $where .= "$and (lead_name $like '%$text%'
                     OR lead_comment $like '%$text%')";
  }
   if (($company_id != '') && ($company_id > 0)) {
    $where .= " $and lead_company_id = '$company_id'";
    $and = 'AND';
  }
 if ($company != '') {
    $where .= sql_global_company_name_advanced_search($company, $like);
    $join_comp = "LEFT JOIN Company ON lead_company_id=company_id";
    $and = 'AND';
  }
  if (($date_after == "$c_null") || ($date_before == "$c_null")) {
    $where .= " $and lead_$date_field is NULL";
  } else {
    if ($date_after) {
      $date_after = new Of_Date($date_after);
      $where .= " $and lead_$date_field >= '".$date_after->getDateIso()."'";
      $and = 'AND';
    }
    if ($date_before) {
      $date_before = new Of_Date($date_before);
      $where .= " $and lead_$date_field <= '".$date_before->getDateIso()."'";
      $and = 'AND';
    }
  }
  if (($priority != '') && ($priority != "$c_undef")) {
    $priority = sql_parse_int($priority, true);
    $where .= " $and lead_priority = $priority ";
    $and = 'AND';
  }
  if (($source != '') && ($source != "$c_all")) {
    $source = sql_parse_id($source, true);
    $where .= " $and lead_source_id $source ";
    $and = 'AND';
  }
  if (($status != '') && ($status != "$c_all")) {
    $status = sql_parse_id($status, true);
    $where .= " $and lead_status_id $status ";
    $and = 'AND';
  }
  if (($manager != '') && ($manager != "$c_all")) {
    $manager = sql_parse_id($manager, true);
    $where .= " $and lead_manager_id $manager ";
    $and = 'AND';
  }
  // If a delegation indication has been specified, get it
  if (trim($delegation) != '') {
    $where .= " $and userobm_delegation $like '$delegation%'";
    $and = 'AND';
  }
  if ($archive != '1') {
    $where .= " $and lead_archive=0";
  } elseif($cgp_archive_only) {
    $where .= " $and lead_archive=1";
    $and = 'AND';
  }

  if ($where != '') {
    $whereq = "WHERE $where $multidomain";
  }

  // Order by clause
  $field = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'lead_datealarm';
  $orderq = " ORDER BY $field $sql_order_dir";

  $join_user = "LEFT JOIN UserObm ON lead_manager_id=userobm_id";

  $query = "SELECT
    lead_id,
    lead_source_id,
    lead_manager_id,
    lead_company_id,
    lead_name,
    lead_priority,
    lead_archive,
    $date,
    $datealarm,
    lead_todo,
    company_name,
    address_zipcode as company_zipcode,
    phone_number as company_phone,
    leadsource_label,
    leadstatus_label,
    contact_id,
    $concat_contact as lead_contact,
    $concat_manager as manager
  FROM Lead
    LEFT JOIN Company ON lead_company_id=company_id
    LEFT JOIN CompanyEntity ON company_id = companyentity_company_id
    LEFT JOIN Address ON address_entity_id = companyentity_entity_id
    LEFT JOIN Phone as cpp ON cpp.phone_entity_id = companyentity_entity_id AND cpp.phone_label like 'WORK;VOICE%'
    LEFT JOIN Contact ON lead_contact_id=contact_id
    LEFT JOIN LeadSource ON lead_source_id=leadsource_id
    LEFT JOIN LeadStatus ON lead_status_id=leadstatus_id
    $join_user
  $whereq
  AND (address_id IN (SELECT MIN(address_id) FROM Address WHERE address_entity_id = companyentity_entity_id) OR address_id IS NULL)
  AND (phone_id IN (SELECT MIN(phone_id) FROM Phone WHERE phone_entity_id = companyentity_entity_id) OR phone_id IS NULL)
  $orderq
  $limit
";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM Lead $join_comp $join_user $whereq");
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'lead_search()');
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Detail query execution
// Parameters :
//   - $p_id  : lead id
///////////////////////////////////////////////////////////////////////////////
function run_query_lead_detail($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, 'lead_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'lead_timecreate', 'timecreate');
  $date = sql_date_format($db_type, 'lead_date', 'date');
  $datealarm = sql_date_format($db_type, 'lead_datealarm', 'datealarm');
  $manager_name[] = array('type' => 'field', 'value' => 'm.userobm_lastname');
  $manager_name[] = array('type' => 'string', 'value' => ' ');
  $manager_name[] = array('type' => 'field', 'value' => 'm.userobm_firstname');
  $concat_manager = sql_string_concat($db_type, $manager_name);
  $multidomain = sql_multidomain('lead');
  $p_id = sql_parse_id($p_id, true);	
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type,1,0);	  
  $query = "SELECT Lead.*,
      $timeupdate,
      $timecreate,
      $date,
      $datealarm,
      leadsource_label,
      leadstatus_label,
      company_name, 
      cpp.phone_number as company_phone,
      address_street as company_address1,
      address_zipcode as company_zipcode,
      address_town as company_town,
      contact_lastname, 
      contact_firstname, 
      c1p.phone_number as contact_phone,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate,
      $concat_manager as manager
    FROM Lead
         LEFT JOIN LeadSource ON lead_source_id=leadsource_id
         LEFT JOIN LeadStatus ON lead_status_id=leadstatus_id
         LEFT JOIN Company ON lead_company_id=company_id
         LEFT JOIN CompanyEntity ON company_id = companyentity_company_id
         LEFT JOIN Address ON address_entity_id = companyentity_entity_id
         LEFT JOIN Phone as cpp ON cpp.phone_entity_id = companyentity_entity_id AND cpp.phone_label like 'WORK;VOICE%'         
         LEFT JOIN Contact ON lead_contact_id=contact_id
         LEFT JOIN ContactEntity  ON contact_id = contactentity_contact_id         
         LEFT JOIN Phone as c1p ON c1p.phone_entity_id = contactentity_entity_id  AND c1p.phone_label like 'WORK;VOICE%'
         LEFT JOIN UserObm as c ON lead_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON lead_userupdate=u.userobm_id
         LEFT JOIN UserObm as m ON lead_manager_id=m.userobm_id
    WHERE lead_id $p_id 
    $multidomain
    $limit";

  display_debug_msg($query, $cdg_sql, 'run_query_lead_detail()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Lead Insertion query execution
// Parameters:
//   - $params[]: Entry's values
///////////////////////////////////////////////////////////////////////////////
function run_query_lead_insert($params) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM;

  $uid = sql_parse_id($obm['uid']);
	
  $company_id = $params['company_new_id'];
  if ($company_id < 1) {
    $company_id = $params['company_id'];
  }
  $company_id = sql_parse_id($company_id);
  $contact_id = $params['contact_new_id'];
  if ($contact_id < 1) {
    $contact_id = $params['contact_id'];
  }
  $contact_id = sql_parse_id($contact_id);
  $source_id = sql_parse_id($params['source']);
  $status_id = sql_parse_id($params['status']);
  $manager_id = sql_parse_id($params['manager']);
  $name = $params['name'];
  $date = ($params['date'] ? "'".of_isodate_convert($params['date'])."'" : 'null');
  $datealarm = ($params['datealarm'] ? "'".of_isodate_convert($params['datealarm'])."'" : 'null');
  $todo = $params['todo'];
  $archive = sql_parse_int($params['archive']);
  $privacy = sql_parse_int($params['privacy']);
  $priority = sql_parse_int($params['priority']);

  $add_comment = $params['add_comment'];
  if ($add_comment != '') {
    $datecomment = of_isodate_convert($params['datecomment'],true);
    $today = date('Y-m-d');
    if ($today == $datecomment) {
      $datecomment = date('Y-m-d H:i:s');
    }
    $usercomment = $params['usercomment'];
    $comment = "$datecomment:$usercomment:$add_comment";
  }

  // Get domain id from Company. If no company, from user
  $domain_id = of_domain_get_entity_domain_id($company_id, 'Company');
  if (! ($domain_id > 0)) {
    $domain_id = $obm['domain_id'];
  }
  $domain_id = sql_parse_id($domain_id);
  $query = "INSERT INTO Lead (
    lead_domain_id,
    lead_timeupdate,
    lead_timecreate,
    lead_userupdate,
    lead_usercreate,
    lead_source_id,
    lead_status_id,
    lead_manager_id,
    lead_company_id,
    lead_contact_id,
    lead_priority,
    lead_privacy,
    lead_name,
    lead_date,
    lead_datealarm,
    lead_archive,
    lead_todo,
    lead_comment)
   VALUES (
    $domain_id,
    null,
    NOW(),
    null,
    $uid,
    $source_id,
    $status_id,
    $manager_id,
    $company_id,
    $contact_id,
    '".$obm_q->escape($privacy)."',
    '".$obm_q->escape($priority)."',
    '".$obm_q->escape($name)."',
    $date,
    $datealarm,
    '".$obm_q->escape($archive)."',
    '".$obm_q->escape($todo)."',
    '".$obm_q->escape($comment)."')";
  
  display_debug_msg($query, $cdg_sql, 'run_query_lead_insert()');
  $retour = $obm_q->query($query);

  $id = $obm_q->lastid();
  $entity_id = of_entity_insert($GLOBALS['module'], $id);  

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Lead Update query execution
// Parameters:
//   - $pid       : lead id
//   - $params[]  : Entry's values
///////////////////////////////////////////////////////////////////////////////
function run_query_lead_update($pid, $params) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM;

  $uid = sql_parse_id($obm['uid']);
  $pid = sql_parse_id($pid, true);
  $company_id = $params['company_new_id'];
  if ($company_id < 1) {
    $company_id = $params['company_id'];
  }
  $company_id = sql_parse_id($company_id);
  $contact_id = $params['contact_new_id'];
  if ($contact_id < 1) {
    $contact_id = $params['contact_id'];
  }
  $contact_id = sql_parse_id($contact_id);
  $source_id = sql_parse_id($params['source']);
  $status_id = sql_parse_id($params['status']);
  $manager_id = sql_parse_id($params['manager']);
  $name = $params['name'];
  $date = ($params['date'] ? "'".of_isodate_convert($params['date'])."'" : 'null');
  $datealarm = ($params['datealarm'] ? "'".of_isodate_convert($params['datealarm'])."'" : 'null');
  $todo = $params['todo'];
  $priority = sql_parse_int($params['priority']);
  $archive = ($params['archive'] == '1' ? '1' : '0');
  $privacy = ($params['privacy'] == '1' ? '1' : '0');
  $comment = $params['comment'];

  $add_comment = $params['add_comment'];
  if ($add_comment != '') {
    $datecomment = of_isodate_convert($params['datecomment'],true);
    $today = date('Y-m-d');
    if ($today == $datecomment) {
      $datecomment = date('Y-m-d H:i:s');
    }
    $usercomment = $params['usercomment'];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }

  $multidomain = sql_multidomain('lead');
	
  $query = "UPDATE Lead SET
    lead_timeupdate=NOW(),
    lead_userupdate=$uid,
    lead_source_id=$source_id,
    lead_status_id=$status_id,
    lead_manager_id=$manager_id,
    lead_company_id=$company_id,
    lead_contact_id=$contact_id,
    lead_priority='".$obm_q->escape($priority)."',
    lead_name='".$obm_q->escape($name)."',
    lead_date=$date,
    lead_datealarm=$datealarm,
    lead_todo='".$obm_q->escape($todo)."',
    lead_archive='".$obm_q->escape($archive)."',
    lead_privacy='".$obm_q->escape($privacy)."',
    lead_comment='".$obm_q->escape($comment)."'
  WHERE lead_id $pid 
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_lead_update()');
  $retour = $obm_q->query($query);

  return $retour;
}


/**
 * Get Lead manager list (from lead database content)
 * @param $archive : if true, get managers of archive leads too
 * return : array : userobm list
 **/
function get_lead_managers($archive = false) {
  global $cdg_sql;

  if (! $archive) {
    $where_arch .= " lead_archive=0";
  }

  if ($where_arch != '') {
    $where = "WHERE $where_arch $multidomain";
  } else {
    $where = "WHERE 1=1 $multidomain";
  }
  
  $multidomain = sql_multidomain('userobm');
	
  $query = "SELECT DISTINCT
      userobm_id,
      userobm_lastname,
      userobm_firstname
    FROM Lead
      JOIN UserObm ON lead_manager_id=userobm_id
    $where
    ORDER BY userobm_lastname";

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'get_lead_managers()');

  while ($obm_q->next_record()) {
    $id = $obm_q->f('userobm_id');
    $name = $obm_q->f('userobm_lastname').' '.$obm_q->f('userobm_firstname');
    $ret[$id] = array('id' => $id, 'name' => $name);
  }

  return $ret;
}


/**
 * Get Lead status list
 * @param $archive : if true, get managers of archive leads too
 * return : array : lead status list
 **/
function get_lead_status() {
  global $cdg_sql;

  $multidomain = sql_multidomain('leadstatus');
	
  $query = "SELECT * FROM LeadStatus ORDER BY leadstatus_code, leadstatus_label";

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'get_lead_status()');

  while ($obm_q->next_record()) {
    $id = $obm_q->f('leadstatus_id');
    $code = $obm_q->f('leadstatus_code');
    $label = $obm_q->f('leadstatus_label');
    $ret[$id] = array('id' => $id, 'code' => $code, 'label' => $label);
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Lead Form Data checking and formatting
// Parameters:
//   - $id       : lead id (empty on insertion)
//   - $params[] : values to check
///////////////////////////////////////////////////////////////////////////////
function check_lead_data_form($id, $params) {
  global $php_regexp_isodate;
  global $l_fill_name, $l_fill_company;
  global $l_fill_date, $l_date, $l_alarm, $l_invalid_date;
  global $err, $l_exist_error;

  $name = $params['name'];
  $date = of_isodate_convert($params['date'],true);
  $datealarm = of_isodate_convert($params['datealarm'],true);
  $c_id = $params['company_id'];
  $c_new_id = $params['company_new_id'];
  $contact_id = $params['contact_id'];
  $contact_new_id = $params['contact_new_id'];
  $kind = $params['kind'];
  $company = $params['company_new_name'];

  // MANDATORY: Name must be set
  if (trim($name) == '') {
    $err['msg'] = $l_fill_name;
    return false;
  }

  // Date check
  if (preg_match($php_regexp_isodate, $date) == 0) {
    $err['msg'] = "$l_date : $date : $l_invalid_date"; 
    return false;
  }

  // Date check
  if (($datealarm != '') && (preg_match($php_regexp_isodate, $datealarm) == 0)) {
    $err['msg'] = "$l_alarm : $datealarm : $l_invalid_date"; 
    return false;
  }
  
  // Company check
  if ((! ($c_id > 0)) && (! ($c_new_id > 0))) {
    $err['msg'] = $l_fill_company;
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the lead can be deleted
// Parameters:
//   - $id : lead id
// Returns:
//   true if the lead can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_can_delete_lead($id) {
  global $ok_msg;

  $delete_ok = true;

  return $delete_ok;
}


///////////////////////////////////////////////////////////////////////////////
// Lead deletion query execution
// Parameters:
//   - $p_id : lead id
///////////////////////////////////////////////////////////////////////////////
function run_query_lead_delete($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain('lead');
  of_entity_delete($GLOBALS['module'], $p_id);
  $p_id = sql_parse_id($p_id, true);	
  $query = "DELETE FROM Lead WHERE lead_id $p_id $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_lead_delete()');
  $retour = $obm_q->query($query);

  return $retour;
}


?>
