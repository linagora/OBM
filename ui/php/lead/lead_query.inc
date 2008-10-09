<?php
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
  global $cdg_sql, $c_all, $c_null, $ctu_sql_limit, $cgp_archive_only;

  $sql_order_dir = $params['sql_order_dir'];
  $sql_order_field = $params['sql_order_field'];

  $text = sql_search_text_parse($params['text']);
  $company_id = $params['company_id'];
  $company = sql_search_text_parse($params['company']);
  $date_after = of_isodate_convert($params['date_after'],true);
  $date_before = of_isodate_convert($params['date_before'],true);
  $date_field = $params['date_field'] ? $params['date_field'] : 'date';
  $source = $params['source'];
  $status = $params['status'];
  $manager = $params['manager'];
  $archive = $params['archive'];

  $obm_q = new DB_OBM;
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
      $where .= " $and lead_$date_field >= '$date_after'";
      $and = 'AND';
    }
    if ($date_before) {
      $where .= " $and lead_$date_field <= '$date_before'";
      $and = 'AND';
    }
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

  $query = "SELECT
    lead_id,
    lead_source_id,
    lead_manager_id,
    lead_company_id,
    lead_name,
    lead_archive,
    $date,
    $datealarm,
    lead_todo,
    company_name,
    company_zipcode,
    company_phone,
    leadsource_label,
    leadstatus_label,
    contact_id,
    $concat_contact as lead_contact,
    $concat_manager as manager
  FROM Lead
    LEFT JOIN Company ON lead_company_id=company_id
    LEFT JOIN Contact ON lead_contact_id=contact_id
    LEFT JOIN LeadSource ON lead_source_id=leadsource_id
    LEFT JOIN LeadStatus ON lead_status_id=leadstatus_id
    LEFT JOIN UserObm ON lead_manager_id=userobm_id
  $whereq
  $orderq
  $limit
";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM Lead $join_comp $whereq");
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
  $query = "SELECT Lead.*,
      $timeupdate,
      $timecreate,
      $date,
      $datealarm,
      leadsource_label,
      leadstatus_label,
      company_name, company_address1, company_zipcode, company_town,
      company_phone,
      contact_lastname, contact_firstname, contact_phone,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate,
      $concat_manager as manager
    FROM Lead
         LEFT JOIN LeadSource ON lead_source_id=leadsource_id
         LEFT JOIN LeadStatus ON lead_status_id=leadstatus_id
         LEFT JOIN Company ON lead_company_id=company_id
         LEFT JOIN Contact ON lead_contact_id=contact_id
         LEFT JOIN UserObm as c ON lead_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON lead_userupdate=u.userobm_id
         LEFT JOIN UserObm as m ON lead_manager_id=m.userobm_id
    WHERE lead_id $p_id 
      $multidomain";

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

  $now = date('Y-m-d H:i:s');
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

  $add_comment = $params['add_comment'];
  if ($add_comment != '') {
    $datecomment = of_isodate_convert($params['datecomment'],true);
    $today = date('Y-m-d');
    if ($today == $datecomment) {
      $datecomment = $now;
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
    '$now',
    null,
    $uid,
    $source_id,
    $status_id,
    $manager_id,
    $company_id,
    $contact_id,
    '$privacy',
    '$name',
    $date,
    $datealarm,
    '$archive',
    '$todo',
    '$comment')";
  
  display_debug_msg($query, $cdg_sql, 'run_query_lead_insert()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  $datep = ($params['date'] ? "='".of_isodate_convert($params['date'])."'" : 'is null');
  $uid = sql_parse_id($uid, true);
  $domain_id = sql_parse_id($domain_id, true); 
  // Get the lead id inserted
  $query = "SELECT MAX(lead_id) as lead_id
    FROM Lead
    WHERE lead_usercreate $uid 
      AND lead_domain_id $domain_id ";

  display_debug_msg($query, $cdg_sql, 'run_query_lead_insert(2)');
  $obm_q->query($query);
  $obm_q->next_record();
  $id = $obm_q->f('lead_id');

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

  $now = date('Y-m-d H:i:s');
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
  $archive = ($params['archive'] == '1' ? '1' : '0');
  $privacy = ($params['privacy'] == '1' ? '1' : '0');
  $comment = $params['comment'];

  $add_comment = $params['add_comment'];
  if ($add_comment != '') {
    $datecomment = of_isodate_convert($params['datecomment'],true);
    $today = date('Y-m-d');
    if ($today == $datecomment) {
      $datecomment = $now;
    }
    $usercomment = $params['usercomment'];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }

  $multidomain = sql_multidomain('lead');
	
  $query = "UPDATE Lead SET
    lead_timeupdate='$now',
    lead_userupdate=$uid,
    lead_source_id=$source_id,
    lead_status_id=$status_id,
    lead_company_id=$company_id,
    lead_contact_id=$contact_id,
    lead_manager_id=$manager_id,
    lead_name='$name',
    lead_date=$date,
    lead_datealarm=$datealarm,
    lead_todo='$todo',
    lead_archive='$archive',
    lead_privacy='$privacy',
    lead_comment='$comment'
  WHERE lead_id $pid 
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_lead_update()');
  $obm_q = new DB_OBM;
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
  global $l_fill_name;
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
  $p_id = sql_parse_id($p_id, true);	
  $query = "DELETE FROM Lead WHERE lead_id $p_id $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_lead_delete()');
  $retour = $obm_q->query($query);

  return $retour;
}


?>
