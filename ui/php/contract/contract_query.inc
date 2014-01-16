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
// OBM - File : contract_query.inc                                           //
//     - Desc : Contract Support query File                                  //
// 2002-02-02 : Aliacom                                                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Contract Search query 
// Parameters :
//   - $contract[]  : contract search criteria
//     keys used       status, label incident, priority, label contract
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_search($contract) {
  global $cdg_sql, $c_all, $ctu_sql_limit;
  global $cgp_archive_only;
  $obm_q = new DB_OBM;

  $sql_order_dir = $contract['sql_order_dir'];
  $sql_order_field = $contract['sql_order_field'];

  $label = sql_search_text_parse($obm_q->escape($contract['label']));
  $type = $contract['type'];
  $dateafter = of_isodate_convert($contract['date_after'],true);
  $datebefore = of_isodate_convert($contract['date_before'],true);
  $comp = sql_search_text_parse($obm_q->escape($contract['company']));
  $num = sql_search_text_parse($obm_q->escape($contract['number']));
  $manager = $contract['manager'];
  $deal = $contract['deal_id'];
  $priority = $contract['priority'];
  $status = $contract['status'];
  $company_id = $contract['company_id'];
  $contact_id = $contract['contact_id'];
  $archive = $contract['archive'];
  $contract_kind = $contract['kind'];
  $delegation = sql_search_text_parse($obm_q->escape($contract['delegation']));
	
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $datebegin = sql_date_format($db_type, 'contract_datebegin', 'contract_datebegin');
  $dateexp = sql_date_format($db_type, 'contract_dateexp', 'contract_dateexp');
  $multidomain = sql_multidomain('contract');
  
  $where .= sql_obm_entity_privacy('contract');

  if (($type != $c_all) && ($type != '')) { 
    $type = sql_parse_id( $type, true);
    $where .= " AND contract_type_id $type"; 
  }
  if ($num != "") {
    $where .= " AND (contract_number $like '%$num%')";
  }

  // If a delegation indication has been specified, get it
  if (trim($delegation) != '') {
    $where .= " AND manager.userobm_delegation $like '$delegation%'";
    $join_delegation = 'LEFT JOIN UserObm as manager ON contract_marketmanager_id=manager.userobm_id';
  }

  if (($status != $c_all) && ($status != "")) {
    $status = sql_parse_id( $status, true);
    $where .= " AND (contract_status_id $status)"; 
  }

  if (($contract_kind != $c_all) && ($contract_kind != "")) {
    $contract_kind = sql_parse_id( $contract_kind, true);
    $where .= " AND (contract_kind $contract_kind)"; 
  }

  if (($priority != $c_all) && ($priority != "")) {
    $priority = sql_parse_id(  $priority, true);
    $where .= " AND (contract_priority_id $priority)"; 
  }

  if ($comp != "") {
    $where .= sql_global_company_name_advanced_search($comp, $like);
    $join_comp = "LEFT JOIN Company ON contract_company_id=company_id";
  }
  if ($company_id != "") {
    $company_id = sql_parse_id( $company_id, true);
    $where .= " and contract_company_id $company_id"; 
  }
  if ($contact_id != "") {
    $contact_id = sql_parse_id( $contact_id, true);
    $where .= " AND (contract_contact1_id $contact_id 
                     OR contract_contact2_id $contact_id)"; 
  }
  if ($dateafter != "") {
    $dateafter = new Of_Date($dateafter);
    $where .= " AND (contract_dateexp >= '".$dateafter->getDateIso()."')";
  }

  if ($datebefore != "") {
    $datebefore = new Of_Date($datebefore);
    $where .= " AND (contract_dateexp <= '".$datebefore->getDateIso()."')";
  }

  if ($label != "")
    $where .= " AND (contract_label $like '%$label%')";
  
  if (($manager != $c_all) && ($manager != "")) {
    $manager = sql_parse_id( $manager, true);
    $where .= " AND (contract_techmanager_id $manager 
                    OR contract_marketmanager_id $manager) ";
  }
  if ($deal != "") {
    $deal = sql_parse_id( $deal, true);
    $where .= " AND (contract_deal_id $deal)"; 
  }
  if ($archive != "1") {
    $where .= " AND contract_archive = 0";
  } elseif ($cgp_archive_only) {
    $where .= " AND contract_archive = 1";
  }

  $whereq = "WHERE $where $multidomain";

  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "contract_dateexp";
  // Order exceptions
  if (strcmp($sql_order_field,"contract_marketmanager")==0) {
    $order = "lmarket";
  }
  if (strcmp($sql_order_field,"contract_techmanager")==0) {
    $order = "ltech";
  }
  $orderq .= " ORDER BY $order $sql_order_dir";

  $query = "SELECT contract_label,
      contract_id,
      contract_id as id,
      contract_deal_id,
      contract_privacy,
      contract_usercreate,
      contract_number,
      contract_company_id,
      company_name as contract_company_name,
      contracttype_label,
      contract_type_id,
      contract_priority_id,
      contract_status_id,
      contractstatus_label as contract_status,
      contractpriority_label as contract_priority,
      contractpriority_color,
      contract_kind,
      contract_format,
      contract_archive,
      contract_techmanager_id,
      contract_marketmanager_id,
      u1.userobm_lastname as ltech, u1.userobm_firstname as ftech,
      u2.userobm_lastname as lmarket, u2.userobm_firstname as fmarket,
      $datebegin,
      $dateexp
    FROM Contract
         LEFT JOIN Company on contract_company_id=company_id
         LEFT JOIN UserObm u1 on contract_techmanager_id=u1.userobm_id
         LEFT JOIN UserObm u2 on contract_marketmanager_id=u2.userobm_id
         LEFT JOIN ContractType on contract_type_id=contracttype_id
         LEFT JOIN ContractPriority on contract_priority_id=contractpriority_id
         LEFT JOIN ContractStatus on contract_status_id = contractstatus_id
         $join_delegation
    $whereq
    $orderq
    $limit
";

  if ($ctu_sql_limit) {
    $cq = "SELECT count(*) FROM Contract $join_comp $join_delegation $whereq";
    $count = get_query_count($cq);
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, "run_query_contract_search()");
    $obm_q->query($query);
  }

  return($obm_q);
}


///////////////////////////////////////////////////////////////////////////////
// Contract detail query execution
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_detail($id) {
  global $cdg_sql;  
  $id = sql_parse_id( $id, true);
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $datebegin = sql_date_format($db_type, "contract_datebegin", "datebegin");
  $dateexp = sql_date_format($db_type, "contract_dateexp", "dateexp");
  $daterenew = sql_date_format($db_type, "contract_daterenew", "daterenew");
  $datecancel = sql_date_format($db_type, "contract_datecancel", "datecancel");
  $datesignature = sql_date_format($db_type, "contract_datesignature", "datesignature");
  $timeupdate = sql_date_format($db_type, "contract_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "contract_timecreate", "timecreate");
  $multidomain = sql_multidomain("contract");
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type,1,0);	

  $query = "SELECT Contract.*,
      contract_privacy as privacy,
      deal_id,
      deal_label,
      contracttype_label,
      company_name,
      company_id,
      cpp.phone_number as company_phone,
      cpf.phone_number as company_fax,
      address_street as company_address1,
      address_zipcode as company_zipcode,
      address_town as company_town,
      address_expresspostal as company_expresspostal,
      u1.userobm_lastname as lname1, u1.userobm_firstname as fname1,
      u2.userobm_lastname as lname2, u2.userobm_firstname as fname2,
      c1.contact_lastname as clname1, c1.contact_firstname as cfname1,
      c2.contact_lastname as clname2, c2.contact_firstname as cfname2,
      c1p.phone_number as cphone1, 
      c2p.phone_number as cphone2,
      p.contractpriority_label,
      s.contractstatus_label,
      $datesignature, 
      $datebegin,
      $dateexp,
      $daterenew,
      $datecancel,
      $timeupdate,
      $timecreate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM Contract
         LEFT JOIN UserObm u1 on contract_marketmanager_id=u1.userobm_id
         LEFT JOIN UserObm u2 on contract_techmanager_id=u2.userobm_id
         LEFT JOIN Contact c1 on contract_contact1_id=c1.contact_id
         LEFT JOIN ContactEntity ce1 ON c1.contact_id = ce1.contactentity_contact_id
         LEFT JOIN Contact c2 on contract_contact2_id=c2.contact_id
         LEFT JOIN ContactEntity ce2 ON c2.contact_id = ce2.contactentity_contact_id
         LEFT JOIN Company on contract_company_id=company_id
         LEFT JOIN CompanyEntity ON company_id = companyentity_company_id
         LEFT JOIN Address ON address_entity_id = companyentity_entity_id
         LEFT JOIN Country ON country_iso3166 = address_country AND country_lang='FR' 
         LEFT JOIN Phone as cpp ON cpp.phone_entity_id = companyentity_entity_id AND cpp.phone_label like 'WORK;VOICE%'
         LEFT JOIN Phone as cpf ON cpf.phone_entity_id = companyentity_entity_id AND cpf.phone_label like 'WORK;FAX%'
         LEFT JOIN Phone as c1p ON c1p.phone_entity_id = ce1.contactentity_entity_id AND c1p.phone_label like 'WORK;VOICE%'
         LEFT JOIN Phone as c2p ON c2p.phone_entity_id = ce2.contactentity_entity_id AND c2p.phone_label like 'WORK;VOICE%'
         LEFT JOIN ContractPriority p on contract_priority_id=p.contractpriority_id
         LEFT JOIN ContractStatus s on contract_status_id=s.contractstatus_id
         LEFT JOIN ContractType on contract_type_id=contracttype_id
         LEFT JOIN Deal on contract_deal_id=deal_id
         LEFT JOIN UserObm as c on contract_usercreate=c.userobm_id
         LEFT JOIN UserObm as u on contract_userupdate=u.userobm_id
    WHERE contract_id $id 
    $multidomain
    $limit";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query construction
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_insert($contract) {
  global $display, $cdg_sql, $cgp_show, $obm;
  $obm_q = new DB_OBM;

  $vis = ($contract["privacy"]==1)?1:0;
  $uid = sql_parse_id( $obm["uid"]);
  $domain_id = sql_parse_id( $obm["domain_id"]);
  $label = $contract["label"];
  $comp_id = sql_parse_id( $contract["company_id"]);
  $deal_id = $contract["deal_new_id"];
  if ($deal_id < 1) {
    $deal_id = $contract["deal_id"];
  }
  $deal_id = sql_parse_id( $deal_id);
  $num = $contract["number"];
  $clause = $contract["clause"];
  $datesignature = ($contract["datesignature"] ? "'".$contract["datesignature"]."'" : "null");
  $datebegin = ($contract["datebegin"] ? "'".of_isodate_convert($contract["datebegin"])."'" : "null");
  $dateexp = ($contract["dateexp"] ? "'".of_isodate_convert($contract["dateexp"])."'" : "null");
  $daterenew = ($contract["daterenew"] ? "'".of_isodate_convert($contract["daterenew"])."'" : "null");
  $datecancel = ($contract["datecancel"] ? "'".of_isodate_convert($contract["datecancel"])."'" : "null");
  $priority = sql_parse_id( $contract["priority"]);
  $status = sql_parse_id( $contract["status"]);
  $contract_kind = sql_parse_id( $contract["kind"]);
  $format = $contract["format"];
  $ticket_nb = sql_parse_int( $contract["ticket_nb"]);
  $duration = sql_parse_int( $contract["duration"]);
  $autorenew = sql_parse_int( $contract["autorenew"]);
  $sel_type = sql_parse_id( $contract["type"]);
  $con1 = sql_parse_id( $contract["con1"]);
  $con2 = sql_parse_id( $contract["con2"]);
  $tech = sql_parse_id( $contract["tech"]);
  $market = sql_parse_id( $contract["market"]);
  $add_comment = $contract["add_comment"];
  if ($add_comment != "") {
    $datecomment = of_isodate_convert($contract["datecomment"]);
    $usercomment = $contract["usercomment"];
    $comment = "$datecomment:$usercomment:$add_comment";
  }
  $archive = ($contract["archive"] == "1" ? "1" : "0");

  $query = "INSERT INTO Contract
     (contract_timeupdate,
      contract_timecreate,
      contract_userupdate,
      contract_usercreate,
      contract_domain_id,
      contract_privacy,
      contract_label,
      contract_deal_id, 
      contract_company_id,
      contract_number,
      contract_priority_id,
      contract_status_id,
      contract_datesignature,
      contract_datebegin,
      contract_dateexp,  
      contract_daterenew,
      contract_datecancel,
      contract_type_id,
      contract_autorenewal,
      contract_kind,
      contract_format,
      contract_ticketnumber,
      contract_duration,
      contract_contact1_id,
      contract_contact2_id,
      contract_techmanager_id,
      contract_marketmanager_id,
      contract_clause,
      contract_archive,
      contract_comment)
    VALUES (
      null,
      '". date("Y-m-d H:i:s") ."', 
      $uid,
      $uid,
      $domain_id,
      $vis,  
      '".$obm_q->escape($label)."',
      $deal_id,
      $comp_id,
      '".$obm_q->escape($num)."',
      $priority,      
      $status,
      $datesignature,
      $datebegin,
      $dateexp,
      $daterenew,
      $datecancel,
      $sel_type,
      $autorenew,
      $contract_kind,    
      '".$obm_q->escape($format)."',  
      '".$obm_q->escape($ticket_nb)."',  
      $duration,  
      $con1,
      $con2,
      $tech,
      $market,
      '".$obm_q->escape($clause)."',
      $archive,    
      '".$obm_q->escape($comment)."')";

  display_debug_msg($query, $cdg_sql);
  $retour = $obm_q->query($query);

  $c_id = $obm_q->lastid();
  $entity_id = of_entity_insert($GLOBALS['module'], $c_id);  

  return $c_id;
}


///////////////////////////////////////////////////////////////////////////////
// Update query construction
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_update($contract) {
  global $obm, $cdg_sql, $ccf_ticket, $ccf_duration;
  $obm_q = new DB_OBM;

  $vis = ($contract["privacy"]==1)?1:0;
  $id = sql_parse_id( $contract["contract_id"], true);
  $num = $contract["number"];
  $label = $contract["label"];
  $datesignature = ($contract["datesignature"] ? "'".of_isodate_convert($contract["datesignature"])."'" : "null");
  $datebegin = ($contract["datebegin"] ? "'".of_isodate_convert($contract["datebegin"])."'" : "null");
  $dateexp = ($contract["dateexp"] ? "'".of_isodate_convert($contract["dateexp"])."'" : "null");
  $daterenew = ($contract["daterenew"] ? "'".of_isodate_convert($contract["daterenew"])."'" : "null");
  $datecancel = ($contract["datecancel"] ? "'".of_isodate_convert($contract["datecancel"])."'" : "null");
  $priority = sql_parse_id( $contract["priority"]);
  $status = $contract["status"];
  $contract_kind = sql_parse_id( $contract["kind"]);
  $type = sql_parse_id( $contract["type"]);
  $autorenew = sql_parse_int( $contract["autorenew"]);
  $format = $contract["format"];
  if ($format == $ccf_ticket) {
    $ticket_nb = sql_parse_int( $contract["ticket_nb"]);
  } else {
    $ticket_nb = 0;
  }
  if ($format == $ccf_duration) {
    $duration = sql_parse_int( $contract["duration"]);
  } else {
    $duration = 0;
  }
  $company_id = sql_parse_id( $contract["company_id"]);
  $con1 = sql_parse_id( $contract["con1"]);
  $con2 = sql_parse_id( $contract["con2"]);
  $tech = sql_parse_id( $contract["tech"]);
  $market = sql_parse_id( $contract["market"]);
  $comment = $contract["comment"];
  $add_comment = $contract["add_comment"];
  if ($add_comment != "") {
    $datecomment = of_isodate_convert($contract["datecomment"]);
    $usercomment = $contract["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $clause = $contract["clause"];
  $deal_id = $contract["deal_new_id"];
  if ($deal_id < 1) {
    $deal_id = $contract["deal_id"];
  }
  // In case deal module not used, to avoid postgres error
  $deal_id = sql_parse_id( $deal_id);
  $archive = ($contract["archive"] == "1" ? "1" : "0");
  $multidomain = sql_multidomain("contract");
	  
  $query = "UPDATE Contract SET
      contract_timeupdate = '". date("Y-m-d H:i:s")."',
      contract_userupdate = ".$obm["uid"].",
      contract_privacy = $vis,
      contract_number = '".$obm_q->escape($num)."',
      contract_label = '".$obm_q->escape($label)."',
      contract_deal_id = $deal_id,
      contract_priority_id = $priority,
      contract_status_id = $status,
      contract_kind = $contract_kind,
      contract_format = '".$obm_q->escape($format)."',
      contract_autorenewal = $autorenew,
      contract_ticketnumber = $ticket_nb,
      contract_duration = $duration,
      contract_datesignature = $datesignature,
      contract_datebegin = $datebegin,
      contract_dateexp = $dateexp,
      contract_daterenew = $daterenew,
      contract_datecancel = $datecancel,
      contract_type_id = $type,
      contract_company_id = $company_id,
      contract_contact1_id = $con1,
      contract_contact2_id = $con2,
      contract_marketmanager_id = $market,
      contract_techmanager_id = $tech,
      contract_comment = '".$obm_q->escape($comment)."',
      contract_clause = '".$obm_q->escape($clause)."',
      contract_archive = $archive
    WHERE contract_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contract_update()");
  $ret = $obm_q->query($query);  

  return $ret;
}

///////////////////////////////////////////////////////////////////////////////
// Delete query execution
// Parameters:
//   - $p_id : contract id
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_delete($p_id) {
  global $cdg_sql;
  of_entity_delete($GLOBALS['module'], $p_id);
  $p_id = sql_parse_id($p_id, true); 
  $multidomain = sql_multidomain("contract");
  $query = "DELETE FROM Contract WHERE contract_id $p_id $multidomain";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $ret = $obm_q->query($query);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Get Contract manager list (include marketing + tech)
// Parameters:
//   - $archive : if true, get managers of archive contracts too
// Return:
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_manager($archive = false) {
  global $cdg_sql;

  if (! $archive) {
    $where_arch .= " contract_archive=0";
  }
  $multidomain = sql_multidomain("userobm");
  if ($where_arch != "") {
    $where = "WHERE $where_arch $multidomain";
  } else {
  	$where = "WHERE 1=1 $multidomain";
  }

		
  $query = "SELECT DISTINCT
      userobm_id,
      userobm_lastname,
      userobm_firstname
    FROM Contract
      JOIN UserObm ON contract_marketmanager_id=userobm_id
    $where
  UNION DISTINCT
    SELECT DISTINCT
      userobm_id,
      userobm_lastname,
      userobm_firstname
    FROM Contract
      JOIN UserObm ON contract_techmanager_id=userobm_id
    $where
  ORDER BY userobm_lastname";

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, "run_query_contract_manager()");

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the contract incident list
// Parameters:
//   - $id : contract id
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_incident($id) {
  global $cdg_sql;
  
  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain("incident");
  $id = sql_parse_id($id, true); 
  $query = "SELECT
      incident_id,
      incident_duration
    FROM Incident
    WHERE incident_contract_id $id $multidomain
    ORDER BY incident_timecreate";
 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Contract Priority list
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_priority() {
  global $cdg_sql;

	$multidomain = sql_multidomain("contractpriority");

  $query = "SELECT * 
    FROM ContractPriority 
		WHERE 1=1 $multidomain
    ORDER BY contractpriority_code";

  display_debug_msg($query, $cdg_sql, "run_query_contract_priority()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Incident Status list
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_status() {
  global $cdg_sql;

	$multidomain = sql_multidomain("contractstatus");

  $query = "SELECT * 
    FROM ContractStatus 
		WHERE 1=1 $multidomain
    ORDER BY contractstatus_code";

  display_debug_msg($query, $cdg_sql, "run_query_contract_status()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Priority insert
// Parameters:
//   - $contract[] : contract hash info : keys used : pri_label, pri_code
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_priority_insert($contract) {
  global $cdg_sql, $obm;
	$obm_q = new DB_OBM;

  $code = $contract["priority_code"];
  $color = $contract["priority_color"];
  $label = $contract["priority_label"];
  $domain_id = $obm["domaini_id"];
	
  $query = "INSERT INTO ContractPriority (
    contractpriority_timeupdate,
    contractpriority_timecreate,
    contractpriority_usercreate,
    contractpriority_domain_id,
    contractpriority_code,
    contractpriority_color,
    contractpriority_label)
    values (
    null,
    '".date("Y-m-d H:i:s")."',
    ".$obm["uid"].",
    $obm[domain_id],
    '".$obm_q->escape($code)."',
    '".$obm_q->escape($color)."',
    '".$obm_q->escape($label)."')";

  display_debug_msg($query, $cdg_sql, "run_query_contract_priority_insert()");
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Priority update
// Parameters:
//   - $contract[] : contract hash info :
//     keys used : priority, pri_label, pri_code
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_priority_update($contract) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM;

  $priority = $contract["priority"];
  $code = $contract["priority_code"];
  $color = $contract["priority_color"];
  $label = $contract["priority_label"];
  $multidomain = sql_multidomain("contractpriority");
	
  $query = "UPDATE ContractPriority SET
    contractpriority_label='".$obm_q->escape($label)."',
    contractpriority_timeupdate='".date("Y-m-d H:i:s")."',
    contractpriority_userupdate='".$obm["uid"]."',
    contractpriority_code='".$obm_q->escape($code)."',
    contractpriority_color='".$obm_q->escape($color)."'
  WHERE contractpriority_id='".$obm_q->escape($priority)."' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contract_priority_update()");
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Priority deletion
// Parameters:
//   - $id : Priority id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_priority_delete($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("contractpriority");
  $id = sql_parse_id($id, true);  
  $query = "DELETE FROM ContractPriority 
		WHERE contractpriority_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contract_priority_delete()");
  $sta_q = new DB_OBM;
  $retour = $sta_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Links to the Priority given
// Parameters:
//   - $s_id : priority id
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_priority_links($p_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("contract");
  $p_id = sql_parse_id($p_id, true);
  $query = "SELECT
      contract_id, 
      contract_label
    FROM Contract 
    WHERE contract_priority_id $p_id $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contract_priority_links()");
  $pri_q = new DB_OBM;
  $pri_q->query($query);

  return $pri_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Status insert
// Parameters:
//   - $contract[] : contract hash info : keys used : sta_label, sta_code
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_status_insert($contract) {
  global $cdg_sql, $obm;
  $obm_q = new DB_OBM;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = sql_parse_id($obm["uid"]);
  $label = $contract["status_label"];
  $code = $contract["status_code"];
  $domain_id = sql_parse_id($obm["domain_id"]);
	
  $query = "INSERT INTO ContractStatus (
    contractstatus_timecreate,
    contractstatus_usercreate,
    contractstatus_domain_id,
    contractstatus_label,
    contractstatus_code)
  values (
    '".$obm_q->escape($timecreate)."',
    $usercreate,
    $domain_id,
    '".$obm_q->escape($label)."',
    '".$obm_q->escape($code)."')";

  display_debug_msg($query, $cdg_sql, "run_query_contract_status_insert()");
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Status update
// Parameters:
//   - $contract[] : contract hash info :
//     keys used : status, sta_label, sta_code
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_status_update($contract) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = sql_parse_id($obm["uid"]);
  $status = sql_parse_id($contract["status"], true);
  $label = $contract["status_label"];
  $code = $contract["status_code"];
  $multidomain = sql_multidomain("contractstatus");
	
  $query = "UPDATE ContractStatus SET
    contractstatus_label='".$obm_q->escape($label)."',
    contractstatus_timeupdate='".$obm_q->escape($timeupdate)."',
    contractstatus_userupdate=$userupdate,
    contractstatus_code='".$obm_q->escape($code)."'
  WHERE contractstatus_id '".$obm_q->escape($status)."' 
    $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contract_status_update()");
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Status deletion
// Parameters:
//   - $id : Status id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_status_delete($id) {
  global $cdg_sql;
  $id = sql_parse_id($id, true);
  $multidomain = sql_multidomain("contractstatus");
  $query = "DELETE FROM ContractStatus 
		WHERE contractstatus_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contract_status_delete()");
  $sta_q = new DB_OBM;
  $retour = $sta_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Links to the Status given
// Parameters:
//   - $s_id : status id
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_status_links($s_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("contract");
  $s_id = sql_parse_id($s_id, true);
  $query = "SELECT
      contract_id, 
      contract_label
    FROM Contract 
    WHERE contract_status_id $s_id $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contract_status_links()");
  $sta_q = new DB_OBM;
  $sta_q->query($query);

  return $sta_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get a priority label
// Parameters:
//   - $id : priority id
///////////////////////////////////////////////////////////////////////////////
function get_contract_priority_label($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("contractpriority");
  $id = sql_parse_id($id, true);
  $query = "SELECT
      contractpriority_label
    FROM ContractPriority
    WHERE contractpriority_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, "get_contract_priority_label()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("contractpriority_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get a status label
// Parameters:
//   - $id : status id
///////////////////////////////////////////////////////////////////////////////
function get_contract_status_label($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("contractstatus");
  $id = sql_parse_id($id, true);
  $query = "SELECT
      contractstatus_label
    FROM ContractStatus
    WHERE contractstatus_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, "get_contract_status_label()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("contractstatus_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query : get contracttype entries                                          //
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_type() {
  global $cdg_sql;

  $multidomain = sql_multidomain("contracttype");
	
  $obm_q = new DB_OBM;
  $query = "SELECT contracttype_label, contracttype_id 
		FROM ContractType WHERE 1=1 $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_contract_type()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Company info
// Parameters:
//   - $id : company Id
///////////////////////////////////////////////////////////////////////////////
function run_query_company_info($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("company");
  $id = sql_parse_id($id, true);
  $query = "SELECT
      company_name, 
      company_marketingmanager_id,
      address_street as company_address1,
      address_zipcode as company_zipcode,
      address_town as company_town
    FROM Company
    INNER JOIN CompanyEntity ON companyentity_company_id = company_id
    LEFT JOIN Address ON address_entity_id = companyentity_entity_id       
    WHERE company_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_company_info()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}
 

///////////////////////////////////////////////////////////////////////////////
// Query   : company contacts contract  :  Table Contact                     //
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_contract($p_company_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("contact");
  $p_company_id = sql_parse_id($p_company_id, true);
  $query = "SELECT
      contact_id,
      contact_lastname,
      contact_firstname,
      phone_number as contact_phone 
    FROM Contact 
    INNER JOIN ContactEntity ON contactentity_contact_id = contact_id
    LEFT JOIN Phone ON phone_entity_id = contactentity_entity_id
    WHERE contact_company_id $p_company_id $multidomain AND (phone_id IN (SELECT MIN(phone_id) FROM Phone WHERE phone_entity_id = contactentity_entity_id) OR phone_id IS NULL) ";
  
  display_debug_msg($query, $cdg_sql, "run_query_contact_contract()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Contract list
// Parameters:
//   - $archive : if set get also archived contracts
///////////////////////////////////////////////////////////////////////////////
function run_query_contract($archive=false) {
  global $cdg_sql;

  $multidomain = sql_multidomain("contract");

  if (! $archive) {
    $whereq = "WHERE contract_archive = 0 $multidomain";
  } else {
    $whereq = "WHERE 1=1 $multidomain";
  }

  $query = "SELECT *,
      company_name
    FROM Contract Left Join Company on contract_company_id=company_id
    $whereq
    ORDER BY company_name";

  display_debug_msg($query, $cdg_sql, "run_query_contract()");
  $con_q = new DB_OBM;
  $con_q->query($query);

  return $con_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Type insert
// Parameters:
//   - $contract[] : contract hash info : keys used : type_label
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_type_insert($contract) {
  global $cdg_sql, $obm;
	$obm_q = new DB_OBM;

  $label = $contract["type_label"];
  $domain_id = sql_parse_id($obm["domain_id"]);
	
  $query = "INSERT INTO ContractType (
    contracttype_timeupdate,
    contracttype_timecreate,
    contracttype_usercreate,
    contracttype_domain_id,
    contracttype_label)
  VALUES (null,
    '".date("Y-m-d H:i:s")."',
    ".$obm["uid"].",
    $domain_id,
    '".$obm_q->escape($label)."')";

  display_debug_msg($query, $cdg_sql, "run_query_contract_type_insert()");
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Type update
// Parameters:
//   - $contract[] : contract hash info : keys used : type, type_label
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_type_update($contract) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM;

  $type = sql_parse_id($contract["type"], true);
  $label = $contract["type_label"];
  $multidomain = sql_multidomain("contracttype");

  $query = "UPDATE ContractType SET 
      contracttype_label='".$obm_q->escape($label)."',
      contracttype_timeupdate='".date("Y-m-d H:i:s")."',
      contracttype_userupdate='".$obm["uid"]."'
    WHERE contracttype_id $type 
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contract_type_update()");
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Type deletion
// Parameters:
//   - $id : Type id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_type_delete($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("contracttype");
  $id = sql_parse_id($id, true);  
  $query = "DELETE FROM ContractType WHERE contracttype_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contract_type_delete()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Links to the Type given
// Parameters:
//   - $t_id : type id
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_type_links($t_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("contract");
  $t_id = sql_parse_id($t_id, true);
  $query = "SELECT
      contract_id, 
      contract_label
    FROM Contract
    WHERE contract_type_id $t_id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contract_type_links()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a type
// Parameters:
//   - $id : type id
///////////////////////////////////////////////////////////////////////////////
function get_contract_type_label($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("contracttype");
  $id = sql_parse_id($id, true);
  $query = "SELECT
      contracttype_label
    FROM ContractType
    WHERE contracttype_id $id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, "get_contract_type_label()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("contracttype_label");
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get Contract infos from its Id
// Parameters:
//   - $id : contract id
///////////////////////////////////////////////////////////////////////////////
function get_contract_info($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("contract");
  $id = sql_parse_id($id, true);
  $query = "SELECT
      contract_id,
      contract_name,
    FROM Contract
    WHERE contract_id $id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, "get_contract_info()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $res["id"] = $obm_q->f("contract_id");
  $res["name"] = $obm_q->f("contract_name");

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Contract ID : Get the id of the contract inserted
// Parameters:
//   - $contract[] : contract hash info : keys used : all
// Returns:
//   contract Id if found else false
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_id($contract) {
  global $cdg_sql;
  $obm_q = new DB_OBM;

  $label = $contract["label"];
  $comp_id = sql_parse_id($contract["company_id"], true);
  $num = $contract["number"];
  $clause = $contract["clause"];
  $datebegin = of_isodate_convert($contract["datebegin"],true);
  $dateexp = of_isodate_convert($contract["dateexp"],true);
  $priority = sql_parse_id($contract["priority"], true);
  $status = sql_parse_id($contract["status"], true);
  $contract_kind = sql_parse_id($contract["kind"], true);
  $format = $contract["format"];
  $ticket_nb = $contract["ticket_nb"];
  $duration = sql_parse_int($contract["duration"]);
  $sel_type = sql_parse_id($contract["type"], true);
  $multidomain = sql_multidomain("contract");
	
  // comment not searched because processed before stored in db
  $query = "SELECT
      contract_id
    FROM Contract
    WHERE contract_number = '".$obm_q->escape($num)."'
      and contract_label = '".$obm_q->escape($label)."'
      and contract_datebegin ='$datebegin'
      and contract_dateexp = '$dateexp'
      and contract_clause = '".$obm_q->escape($clause)."'
      and contract_company_id $comp_id 
      and contract_priority_id $priority 
      and contract_status_id $status $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_contract_id()");
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
   $obm_q->next_record();
    $id = $obm_q->f("contract_id");
  } else {
    $id = false;
  }

  return $id;
}



///////////////////////////////////////////////////////////////////////////////
// Return the contracts with this name 
// Parameters:
//   - $c_id : contract Id not to return
//   - $name : name
///////////////////////////////////////////////////////////////////////////////
function run_query_check_contract($c_id, $name) {
  global $cdg_sql;

  $where_id = "";
  if ($c_id != "") {
    $c_id = sql_parse_id($c_id, true, true);
    $where_id = "AND contract_id $c_id"; 
  }
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  $query = "SELECT DISTINCT 
      contract_id, 
      contract_label 
    FROM Contract
    WHERE contract_label $like '".$obm_q->escape($name)."%'
      $where_id";

  display_debug_msg($query, $cdg_sql, "run_query_check_contract()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contract environment checking (same contracts exists ?)
// Parameters: :/ Tous les champs ici
//   - $c_id       : contract id
//   - $contract[] : contract's values
// Returns:
//   - Contract Database object with list of similar contracts
///////////////////////////////////////////////////////////////////////////////
function check_contract_context($c_id, $contract) {

  $num = $contract["number"];
  $name = $contract["label"];
 
  // if a company with same name and zip exists, return false
  $co_q = run_query_check_contract($c_id, $name);
  return $co_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contract Form Data checking and formatting
// Parameters:
//   - $cid        : contract id : (empty on insertion)
//   - $contract[] : values to check
// Return : true if check ok, else false
///////////////////////////////////////////////////////////////////////////////
function check_contract_form($cid, $contract) {
  global $err, $ccf_ticket, $ccf_duration;
  global $php_regexp_isodate, $l_invalid_date, $l_err_fill;
  global $l_err_label_empty, $l_err_ticket_empty, $l_err_duration_empty;
  global $l_err_date, $l_err_daterenew, $l_err_datecancel;
  global $l_date, $l_signature,$l_begin,$l_exp,$l_renew,$l_date_cancel;

  $datebegin = of_isodate_convert($contract["datebegin"],true);
  $dateexp = of_isodate_convert($contract["dateexp"],true);
  $daterenew = of_isodate_convert($contract["daterenew"],true);
  $datecancel = of_isodate_convert($contract["datecancel"],true);
  $format = $contract["format"];
  $ticket_nb = $contract["ticket_nb"];
  $duration = $contract["duration"];

  // Check if the label is filled
  if (trim($contract["label"]) == "") {
    $err["msg"] = $l_err_label_empty;
    return false;
  }

  // Check if the Tickets number is filled
  if ($format == $ccf_ticket)
    if (($ticket_nb == "") || ($ticket_nb < 1)) {
      $err["msg"] = $l_err_ticket_empty;
      return false;
    }

  // Check if the Support duration is filled
  if ($format == $ccf_duration)
    if (($duration == "") || ($duration < 1)) {
      $err["msg"] = $l_err_duration_empty;
      return false;
    }

  // Check if the dates fields are filled properly
  // Begin Date check
  if ($datebegin != "") {
    if (preg_match($php_regexp_isodate, $datebegin) == 0) {
      $err["msg"] = "$l_begin : $l_invalid_date"; 
      return false;
    }
  } else {
    $err["msg"] = "$l_begin : $l_err_fill"; 
    return false;
  }

  // Expiration Date check
  if ($dateexp != "") {
    if (preg_match($php_regexp_isodate, $dateexp) == 0) {
      $err["msg"] = "$l_exp : $l_invalid_date"; 
      return false;
    }
  } else {
    $err["msg"] = "$l_exp : $l_err_fill"; 
    return false;
  }

  // Signature Date check
  if ( ($datesignature != "")
       && (preg_match($php_regexp_isodate, $datesignature) == 0) ) {
    $err["msg"] = "$l_signature : $l_invalid_date"; 
    return false;
  }

  // Renew Date check
  if ( ($daterenew != "")
       && (preg_match($php_regexp_isodate, $daterenew) == 0) ) {
    $err["msg"] = "$l_renew : $l_invalid_date"; 
    return false;
  }

  // Cancel Date check
  if ( ($datecancel != "")
       && (preg_match($php_regexp_isodate, $datecancel) == 0) ) {
    $err["msg"] = "$l_date_cancel : $l_invalid_date"; 
    return false;
  }


  if ($dateexp < $datebegin){
    $err["msg"] = $l_err_date;
    return false;
  }
  if (!($daterenew == "")) {
    if (($daterenew < $datebegin) || ($daterenew > $dateexp)){
      $err["msg"] = $l_err_daterenew;
      return false;
    }
  }
  if (!($datecancel == "")) {
    if (($datecancel < $datebegin) || ($datecancel > $dateexp)){
      $err["msg"] = $l_err_datecancel;
      return false;
    }
  }  

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the contract can be deleted
// Parameters:
//   - $p_id : contract id
// Returns:
//   true if the contract can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_can_delete_contract($p_id) {
  global $err, $ok_msg;
  global $l_link_incident, $l_link_incident_no;

  $delete_ok = true;

  // Links from Incident
  $nb = get_global_linked_incident_nb($p_id, "contract", true);
  if ($nb > 0) {
    $delete_ok = false;
    $err["msg"] .= "$l_link_incident";
  } else {
    $ok_msg .= "$l_link_incident_no";
  }

  return $delete_ok;
}


?>
