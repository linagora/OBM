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
// OBM - File : incident_query.inc                                           //
//     - Desc : Incident query File                                          //
// 2000-01-20 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Incident search query execution
// Parametes:
//   - $incident[]   : incident search criteria
//     keys used       status, label incident, priority, label contract
///////////////////////////////////////////////////////////////////////////////
function run_query_incident_search($incident) {
  global $c_all, $cdg_sql, $ctu_sql_limit;
  global $cgp_archive_only;
  $obm_q = new DB_OBM;

  $sql_order_dir = $incident['sql_order_dir'];
  $sql_order_field = $incident['sql_order_field'];

  $text = sql_search_text_parse($obm_q->escape($incident['text']));
  $lincident = sql_search_text_parse($obm_q->escape($incident['lincident']));
  $lcontract = sql_search_text_parse($obm_q->escape($incident['lcontract']));
  $i_id = sql_search_text_parse($obm_q->escape($incident['incident_id']));
  $company = sql_search_text_parse($obm_q->escape($incident['company']));
  $priority = $incident['priority'];
  $status = $incident['status'];
  $resolutiontype = $incident['resolutiontype'];
  $contract_id =  $incident['contract_id'];
  $owner = $incident['owner'];
  $logger = $incident['logger'];
  $dateafter = of_isodate_convert($incident['date_after'],true);
  $datebefore = of_isodate_convert($incident['date_before'],true);
  $archive = $incident['archive'];
  $reference = sql_search_text_parse($obm_q->escape($incident['reference']));
  $multidomain = sql_multidomain('incident');

  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $date = sql_date_format($db_type, 'incident_date', 'date');
  $timeupdate = sql_date_format($db_type, 'incident_timeupdate', 'timeupdate');

  $where = '1=1';
  if ($text != '') {
    $where .= " AND (Incident.incident_label $like '%$text%'
      OR Incident.incident_reference $like '%$text%'
      OR Incident.incident_comment $like '%$text%'
      OR Incident.incident_resolution $like '%$text%')";
  }
  if ($lincident != '') {
    $where .= " AND Incident.incident_label $like '%$lincident%'";
  }
  if ($reference != '') {
    $where .= " AND Incident.incident_reference $like '$reference'";
  }
  if ($i_id != '') {
    $i_id = sql_parse_id($i_id, true);
    $where .= " AND Incident.incident_id $i_id ";
  }
  if ($owner != $c_all && ($owner != '')) {
    $owner = sql_parse_id($owner, true);
    $where .=" AND (Incident.incident_owner $owner ) ";
  }
  if ($logger != $c_all && ($logger != '')) {
    $logger = sql_parse_id($logger, true);
    $where .=" AND (Incident.incident_logger $logger ) ";
  }
  if ($dateafter !='') {
    $dateafter = new Of_Date($dateafter);
    $where .=" AND (Incident.incident_date >= '".$dateafter->getDateIso()."') ";
  }
  if ($datebefore !='') {
    $datebefore = new Of_Date($datebefore);
    $where .=" AND (Incident.incident_date <= '".$datebefore->getDateIso()."') ";
  }
  if ($contract_id != '') {
    $contract_id = sql_parse_id($contract_id, true);
    $where .= " AND incident_contract_id = '$contract_id'";
  }
  if ($lcontract != '') {
    $where .= " AND contract_label $like '$lcontract%'";
    $join_contr = 'LEFT JOIN Contract ON incident_contract_id=contract_id';
  }
  if ($company != '') {
    $where .= sql_global_company_name_advanced_search($company, $like);
    $join_contr = 'LEFT JOIN Contract ON incident_contract_id=contract_id';
    $join_comp = 'LEFT JOIN Company ON contract_company_id=company_id';
  }
  $join_priority = 'LEFT JOIN IncidentPriority ON incident_priority_id=incidentpriority_id';
  if (($priority != $c_all) && ($priority != '')) { 
    $priority = sql_parse_id($priority, true);
    $where .= " AND Incident.incident_priority_id $priority ";
  }
  if (($status != $c_all) && ($status != '')) { 
    $status = sql_parse_id($status, true);
    $where .= " AND Incident.incident_status_id $status ";
  }
  $join_resolutiontype = 'LEFT JOIN IncidentResolutionType ON incident_resolutiontype_id=incidentresolutiontype_id';
  if (($resolutiontype != $c_all) && ($resolutiontype != '')) {
    $resolutiontype = sql_parse_id($resolutiontype, true);
    $where .= " AND Incident.incident_resolutiontype_id $resolutiontype ";
  }

  if ($archive != '1') {
    $where .= " AND Incident.incident_archive = 0";
  } elseif($cgp_archive_only) {
    $where .= " AND Incident.incident_archive = 1";
  }

  // User defined data
  $userdata = of_userdata_query_search('Incident', $incident);
  if ($userdata['where'] != '') {
    $where .= ' AND '. $userdata['where'];
    $join_userdata = $userdata['join'];
  }


  $whereq = "WHERE $where $multidomain";

  // ORDER construction
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'incident_status_id';
  $orderq .= " ORDER BY $order $sql_order_dir, incidentpriority_code";

  $select = "SELECT
    Incident.incident_id as id,
    incident_id,
    incident_timeupdate,
    incident_contract_id,
    incident_label,
    incident_reference,
    incident_date,
    incident_priority_id,
    incidentpriority_label as incident_priority,
    incidentpriority_color,
    incident_status_id as incident_status,
    incidentresolutiontype_label as resolutiontype_label,
    incident_logger,
    incident_owner,
    incident_archive,
    incident_duration,
    $date,
    $timeupdate,
    contract_label,
    u1.userobm_lastname as incident_logger_lastname,
    u2.userobm_lastname as incident_owner_lastname,
    Company.company_name as incident_company_name
    FROM Incident
    INNER JOIN IncidentEntity ON incidententity_incident_id = incident_id";

  $left_join = "
    LEFT JOIN Contract ON incident_contract_id=contract_id
    LEFT JOIN Company ON contract_company_id=company_id
    LEFT JOIN UserObm as u1 ON incident_logger=u1.userobm_id
    LEFT JOIN UserObm as u2 ON incident_owner=u2.userobm_id
    $join_priority
    $join_status
    $join_resolutiontype
    $join_userdata";

  $query = "$select $left_join $whereq $orderq $limit";

  if ($ctu_sql_limit) {
    $cq = "SELECT count(*) FROM Incident $left_join $whereq";
    $count = get_query_count($cq);
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'run_query_incident_search()');
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Incident detail query execution
// Parameters:
//   - $id : incident id
///////////////////////////////////////////////////////////////////////////////
function run_query_incident_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $date = sql_date_format($db_type, 'incident_date', 'date');
  $timeupdate = sql_date_format($db_type, 'incident_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'incident_timecreate', 'timecreate');
  $datebegin = sql_date_format($db_type, 'contract_datebegin', 'datebegin');
  $dateexp = sql_date_format($db_type, 'contract_dateexp', 'dateexp');
  $multidomain = sql_multidomain('incident');
  $id = sql_parse_id($id, true);
  $query = "SELECT *,
    u1.userobm_lastname as lname1, u1.userobm_firstname as fname1,
    u2.userobm_lastname as lname2, u2.userobm_firstname as fname2,
    incidentpriority_label,
    incident_status_id,
    incidentresolutiontype_label,
    $date,
    $timeupdate,
    $timecreate,
    c.userobm_login as usercreate,
    u.userobm_login as userupdate,
    company_name,
    contract_id,
    contract_label,
    contract_number,
    $datebegin,
    $dateexp,
    contractstatus_label,
    c1.contact_lastname as lname_c1, c1.contact_firstname as fname_c1,
    c1p.phone_number as phone_c1,
    c1e.email_address as email_c1,
    c2.contact_lastname as lname_c2,
    c2.contact_firstname as fname_c2,
    c2p.phone_number as phone_c2,
    c2e.email_address as email_c2
    FROM Incident
    LEFT JOIN Contract on incident_contract_id=contract_id
    LEFT JOIN ContractStatus on contract_status_id=contractstatus_id
    LEFT JOIN Company on contract_company_id=company_id
    LEFT JOIN UserObm u1 on incident_logger=u1.userobm_id
    LEFT JOIN UserObm u2 on incident_owner=u2.userobm_id
    LEFT JOIN IncidentPriority on incident_priority_id=incidentpriority_id
    LEFT JOIN IncidentResolutionType on incident_resolutiontype_id=incidentresolutiontype_id
    LEFT JOIN UserObm as c on incident_usercreate=c.userobm_id
    LEFT JOIN UserObm as u on incident_userupdate=u.userobm_id
    LEFT JOIN Contact c1 on contract_contact1_id=c1.contact_id
    LEFT JOIN ContactEntity ce1 ON c1.contact_id = ce1.contactentity_contact_id
    LEFT JOIN Email as c1e ON c1e.email_entity_id = ce1.contactentity_entity_id
    LEFT JOIN Phone as c1p ON c1p.phone_entity_id = ce1.contactentity_entity_id AND c1p.phone_label like 'WORK;VOICE%'
    LEFT JOIN Contact c2 on contract_contact2_id=c2.contact_id
    LEFT JOIN ContactEntity ce2 ON c2.contact_id = ce2.contactentity_contact_id
    LEFT JOIN Phone as c2p ON c2p.phone_entity_id = ce2.contactentity_entity_id AND c2p.phone_label like 'WORK;VOICE%'    
    LEFT JOIN Email as c2e ON c2e.email_entity_id = ce2.contactentity_entity_id
    WHERE incident_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_incident_detail()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Incident Insertion query execution
// Parameters:
//   - $incident[] : Entry's values
//     keys used   : lincident, logger, owner, description, solution, priority
//                 : status, date, hour, duration, archive, contract_id
///////////////////////////////////////////////////////////////////////////////
function run_query_incident_insert($incident) {
  global $cdg_sql, $cgp_host, $obm;
  global $cmail_incident, $l_mail_incident_new_subject, $l_status, $l_contract;
  $obm_q = new DB_OBM;

  $uid = sql_parse_id($obm['uid']);
  $domain_id = sql_parse_id($obm['domain_id']);

  $contract_id = $incident['contract_new_id'];
  if ($contract_id < 1) {
    $contract_id = $incident['contract_id'];
  }
  $contract_id = sql_parse_id($contract_id);
  $lincident = $incident['lincident'];
  $reference = $incident['reference'];
  $owner = sql_parse_id($incident['owner']);
  $logger = sql_parse_id($incident['logger']);
  $archive = sql_parse_int($incident['archive']);
  $solu = $incident['solution'];
  $priority = sql_parse_id($incident['priority']);
  $status = sql_parse_id($incident['status']);
  $reso = sql_parse_id($incident['resolutiontype']);
  $date = of_isodate_convert($incident['date']);
  $hour = $incident['hour'];
  $add_duration = $incident['add_duration'];
  $call_date = "$date $hour:0:0";
  $add_comment = $incident['add_comment'];
  if ($add_comment != '') {
    $datecomment = of_isodate_convert($incident['datecomment']);
    $usercomment = $incident['usercomment'];
    $comment = "$datecomment:$usercomment:$add_comment";
  }

  $query = "INSERT INTO Incident (
    incident_domain_id,
    incident_timeupdate,
    incident_timecreate,
    incident_userupdate,
    incident_usercreate,
    incident_label,
    incident_reference,
    incident_contract_id,
    incident_date,
    incident_priority_id,
    incident_status_id,
    incident_resolutiontype_id,
    incident_comment,
    incident_logger,
    incident_owner,
    incident_duration,
    incident_archive,
    incident_resolution)
    VALUES (
      $domain_id,
      null,
      NOW(),
      null,
      $uid,
      '".$obm_q->escape($lincident)."',
      '".$obm_q->escape($reference)."',
      $contract_id,
      '".$obm_q->escape($call_date)."',
      $priority,
      $status,
      $reso,
      '".$obm_q->escape($comment)."',
      $logger,
      $owner,
      '".$obm_q->escape($add_duration)."',
      $archive,
      '".$obm_q->escape($solu)."')";

  display_debug_msg($query, $cdg_sql, 'run_query_incident_insert()');
  $obm_q->query($query);

  $i_id = $obm_q->lastid(); 

  if ($i_id > 0) {
    $entity_id = of_entity_insert($GLOBALS['module'], $i_id);  
    $ret = of_userdata_query_update('incident', $i_id, $incident);
  }

  if ($cmail_incident != '') {
    $inc = get_incident_info($i_id);
    $company_name = $inc['company'];
    $status = $inc['status'];
    $contract = $inc['contract'];
    $label_mail = stripslashes($lincident);
    $subject = "$l_mail_incident_new_subject : $company_name : $label_mail";
    $message = "$cgp_host/incident/incident_index.php?action=detailconsult&incident_id=$i_id

      $l_status: $status
      $l_contract: $contract
      $comment";
    send_mail($subject, $message, array($logger, $owner), array("$cmail_incident"), '', 1);
  }

  return $i_id;
}


///////////////////////////////////////////////////////////////////////////////
// Incident ID : Get the id of the incident given as parameter
// Parameters:
//   - $incident[] : incident hash info : keys used : all
// Returns:
//   incident Id if found else false
///////////////////////////////////////////////////////////////////////////////
function run_query_incident_id($incident) {
  global $cdg_sql;
  $obm_q = new DB_OBM;

  $contract_id = $incident['contract_new_id'];
  if ($contract_id < 1) {
    $contract_id = $incident['contract_id'];
  }
  $contract_id = sql_parse_id($contract_id, true);
  $lincident = $incident['lincident'];
  $owner = sql_parse_id($incident['owner'], true);
  $logger = sql_parse_id($incident['logger'], true);
  $date = of_isodate_convert($incident['date']);
  $pri = sql_parse_id($incident['priority'], true);
  $status = sql_parse_id($incident['status'], true);
  $hour = $incident['hour'];
  $call_date = "$date $hour:0:0";
  $archive = sql_parse_int($incident['archive']);
  $multidomain = sql_multidomain('incident');

  $query = "SELECT MAX(incident_id) as incident_id
    FROM Incident
    WHERE incident_contract_id $contract_id 
    AND incident_label = '".$obm_q->escape($lincident)."'
    AND incident_owner $owner
    AND incident_logger $logger
    AND incident_date='".$obm_q->escape($call_date)."'
    AND incident_priority_id $pri
    AND incident_status_id $status
    AND incident_archive=$archive
    $multidomain";

  display_debug_msg($query, $cdg_sql,'run_query_incident_id()');
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    $obm_q->next_record();
    $id = $obm_q->f('incident_id');
  } else {
    $id = false;
  }

  return $id;
}

/**
 * get_incident_status 
 * 
 * @param string $target 
 * @access public
 * @return void
 */
function get_incident_status($target='') {
  global $cdg_sql, $cincident_status;

  $sta = array();

  if ($target != '') {
    $status_target[$target] = $cincident_status[$target];
  } else {
    $status_target = $cincident_status;
  }

  foreach ($status_target as $status => $s_id) {
    $s_label = $GLOBALS["l_incidentstatus_$status"];
    $sta[$s_id] = $s_label;
  }

  return $sta;
}


///////////////////////////////////////////////////////////////////////////////
// Incident update query execution
// Parameters:
//   - $incident[] : Entry's values
//     keys used   : lincident, logger, owner,  solution, priority
//                 : status, date, hour, duration, archive, contract_id
///////////////////////////////////////////////////////////////////////////////
function run_query_incident_update($incident) {
  global $obm, $cdg_sql, $cincident_status,$l_mail_incident_affected_subject;
  global $l_mail_incident_closed_subject, $cmail_incident,$cgp_host;
  global $l_mail_incident_closed_content, $l_mail_incident_affected_content;
  $inc_q = new DB_OBM;

  $contract_id = $incident['contract_new_id'];
  if ($contract_id < 1) {
    $contract_id = $incident['contract_id'];
  }
  $contract_id = sql_parse_id($contract_id);
  $id = sql_parse_id($incident['incident_id'], true);
  $lincident = $incident['lincident'];
  $reference = $incident['reference'];
  $owner = sql_parse_id($incident['owner']);
  $logger = sql_parse_id($incident['logger']);
  $solu = $incident['solution'];
  $priority = sql_parse_id($incident['priority']);
  $status = sql_parse_id($incident['status']);
  $reso = sql_parse_id($incident['resolutiontype']);
  $date = of_isodate_convert($incident['date']);
  $hour = $incident['hour'];
  $duration = $incident['duration'];
  $archive = sql_parse_int($incident['archive']);
  $call_date = "$date $hour:0:0";
  $comment = $incident['comment'];
  $add_comment = $incident['add_comment'];
  if ($add_comment != '') {
    $datecomment = of_isodate_convert($incident['datecomment']);
    $usercomment = $incident['usercomment'];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
    $add_duration = $incident['add_duration'];
    $duration = $duration + $add_duration;
  }
  $inc_info = get_incident_info($incident['incident_id']);
  if($inc_info['owner'] != $incident['owner']) {
    $owner_changed = true;
  }
  if ($inc_info['status_id'] != $cincident_status['closed'] 
    && $status == $cincident_status['closed']) {
      $closed = true;  
    }

  $multidomain = sql_multidomain('incident');

  $query = "UPDATE Incident SET
    incident_timeupdate='". date('Y-m-d H:i:s')."',
      incident_userupdate=$obm[uid],
      incident_logger=$logger,
      incident_owner=$owner,
      incident_label='".$inc_q->escape($lincident)."',
      incident_reference='".$inc_q->escape($reference)."',
      incident_date='".$inc_q->escape($call_date)."', 
      incident_priority_id=$priority,
      incident_status_id=$status,
      incident_resolutiontype_id=$reso,
      incident_resolution = '".$inc_q->escape($solu)."',
      incident_duration = '".$inc_q->escape($duration)."',
      incident_comment='".$inc_q->escape($comment)."',
      incident_archive=$archive,
      incident_contract_id=$contract_id
      WHERE incident_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_incident_update()');
  $ret = $inc_q->query($query);  

  if ($ret) {
    if($owner_changed) {
      $label_mail = stripslashes($lincident);
      $subject = "$l_mail_incident_affected_subject : $label_mail";
      $message = "$l_mail_incident_affected_content : $cgp_host/incident/incident_index.php?action=detailconsult&incident_id=$incident[incident_id]";
      send_mail($subject, $message,  array($incident['owner']), array(), '', 1);
    }
    if($closed) {
      $label_mail = stripslashes($lincident);
      $subject = "$l_mail_incident_closed_subject : $label_mail";
      $message = "$cgp_host/incident/incident_index.php?action=detailconsult&incident_id=$incident[incident_id] $l_mail_incident_closed_content";

      send_mail($subject, $message, array($incident['owner']), array("$cmail_incident"), '', 1);
    }
    $ret = of_userdata_query_update('incident', $incident['incident_id'], $incident);
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Delete query construction
// Parameters:
//   - $i_id : incident id
///////////////////////////////////////////////////////////////////////////////
function run_query_incident_delete($i_id) {
  global $cdg_sql;

  $ret = of_userdata_query_delete('incident', $i_id);
  $multidomain = sql_multidomain('incident');
  $i_id = sql_parse_id($i_id, true);
  $query = "DELETE FROM Incident 
    WHERE incident_id $i_id 
    $multidomain";

  $i_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, 'run_query_incident_delete()');
  $ret = $i_q->query($query);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Get Incident owner list
// Parameters:
//   - $archive : if true, get owner of archive incidents too
// Return:
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_incident_owner($archive = false) {
  global $cdg_sql;

  if (! $archive) {
    $where_arch .= " incident_archive=0";
  }

  $multidomain = sql_multidomain('incident');

  if ($where_arch != '') {
    $where = "WHERE $where_arch $multidomain";
  } else {
    $where = "WHERE 1=1 $multidomain";
  }

  $query = "SELECT DISTINCT
    userobm_id,
    userobm_lastname,
    userobm_firstname
    FROM Incident
    LEFT JOIN UserObm ON incident_owner=userobm_id
    $where
    ORDER BY userobm_lastname";

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'run_query_incident_owner()');

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get Incident logger list
// Parameters:
//   - $archive : if true, get logger of archive incidents too
// Return:
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_incident_logger($archive = false) {
  global $cdg_sql;

  if (! $archive) {
    $where_arch .= " incident_archive=0";
  }

  $multidomain = sql_multidomain('incident');

  if ($where_arch != '') {
    $where = "WHERE $where_arch $multidomain";
  } else {
    $where = "WHERE 1=1 $multidomain";
  }

  $query = "SELECT DISTINCT
    userobm_id,
    userobm_lastname,
    userobm_firstname
    FROM Incident
    LEFT JOIN UserObm ON incident_logger=userobm_id
    $where
    ORDER BY userobm_lastname";

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'run_query_incident_logger()');

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get Incident infos
// Parameters:
//   - $id : incident id
///////////////////////////////////////////////////////////////////////////////
function get_incident_info($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('incident');
  $id = sql_parse_id($id, true);
  $query = "SELECT
    incident_id,
    incident_label,
    incident_status_id,
    incident_owner,
    contract_label,
    company_name
    FROM Incident
    LEFT JOIN Contract on incident_contract_id=contract_id
    LEFT JOIN Company on contract_company_id=company_id
    WHERE incident_id $id $multidomain";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $res['incident_id'] = $obm_q->f('incident_id');
  $res['label'] = $obm_q->f('incident_label');
  $status = get_incident_status();
  $res['status'] = $status[$obm_q->f('incident_status_id')];
  $res['status_id'] = $obm_q->f('incident_status_id');
  $res['contract'] = $obm_q->f('contract_label');
  $res['company'] = $obm_q->f('company_name');
  $res['owner'] = $obm_q->f('incident_owner');

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Incident Form Data checking and formatting
// Parameters:
//   - $incident[] : values to check
// Return : true if check ok, else false
///////////////////////////////////////////////////////////////////////////////
function check_incident_form($incident) {
  global $cdg_sql;
  global $php_regexp_isodate, $l_fill_label, $l_fill_contract,$l_incident_date;
  global $l_fill_status, $l_fill_priority;
  global $err, $l_length_label, $l_invalid_date;

  $c_id = $incident['contract_id'];
  $c_new_id = $incident['contract_new_id'];
  $reference = $incident['reference'];

  // Check the label is filled
  if (trim($incident['lincident']) == '') {
    $err['msg'] = $l_fill_label;
    return false;
  }

  // Check the status is filled
  if (!is_numeric($incident['status'])) {
    $err['msg'] = $l_fill_status;
    return false;
  }

  // Check the priority is filled
  if ($incident['priority'] < 1) {
    $err['msg'] = $l_fill_priority;
    return false;
  }

  // MANDATORY: a contract must be assigned
  if (($c_id < 1) && ($c_new_id < 1)) {
    $err['msg'] = $l_fill_contract;
    return false;
  }

  if (strlen($incident['lincident']) > 100) {
    $err['msg'] = $l_length_label;
    return false;
  }

  if (strlen($reference) > 32) {
    $err['msg'] = $l_length_reference;
    return false;
  }

  $date = of_isodate_convert($incident['date'],true);
  // Begin Date check
  if (($date == '') || (preg_match($php_regexp_isodate, $date) == 0)) {
    $err['msg'] = "$l_incident_date : $l_invalid_date"; 
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the incident can be deleted
// Parameters:
//   - $p_id : incident id
// Returns:
//   true if the incident can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_can_delete_incident($p_id) {
  global $cdg_sql, $ok_msg;

  $delete_ok = true;

  return $delete_ok;
}

?>
