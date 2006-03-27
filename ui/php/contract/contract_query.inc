<?php
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
//   - $p_new_order : if it's set, replaces the default "order by" value
//   - $p_order_dir : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_search($contract, $p_new_order, $p_order_dir) {
  global $cdg_sql, $c_all, $ctu_sql_limit;

  $label = sql_search_text_parse($contract["label"]);
  $type = $contract["type"];
  $dateafter = $contract["dateafter"];
  $datebefore = $contract["datebefore"];
  $comp = sql_search_text_parse($contract["company_name"]);
  $num = sql_search_text_parse($contract["number"]);
  $manager = $contract["manager"];
  $deal = $contract["deal_id"];
  $priority = $contract["priority"];
  $status = $contract["status"];
  $company_id = $contract["company_id"];
  $contact_id = $contract["contact_id"];
  $archive = $contract["archive"];
  $contract_kind = $contract["kind"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $datebegin = sql_date_format($db_type, "contract_datebegin", "contract_datebegin");
  $dateexp = sql_date_format($db_type, "contract_dateexp", "contract_dateexp");
  $where .= sql_obm_entity_privacy("contract");

  if (($type != $c_all) && ($type != ""))
    $where .= " AND contract_type_id = '$type'";

  if ($num != "")
    $where .= " AND (contract_number $like '%$num%')";

  if (($status != $c_all) && ($status != ""))
    $where .= " AND (contract_status_id = '$status')";

  if (($contract_kind != $c_all) && ($contract_kind != ""))
    $where .= " AND (contract_kind = '$contract_kind')";

  if (($priority != $c_all) && ($priority != ""))
    $where .= " AND (contract_priority_id = '$priority')";

  if ($comp != "") {
    $where .= sql_global_company_name_advanced_search($comp, $like);
    $join_comp = "LEFT JOIN Company ON contract_company_id=company_id";
  }
  if ($company_id != "") {
    $where .= " and contract_company_id = '$company_id'";
  }
  if ($contact_id != "") {
    $where .= " AND (contract_contact1_id = '$contact_id'
                     OR contract_contact2_id = '$contact_id')";
  }
  if ($dateafter != "")
    $where .= " AND (contract_dateexp >= '$dateafter')";

  if ($datebefore != "")
    $where .= " AND (contract_dateexp <= '$datebefore')";

  if ($label != "")
    $where .= " AND (contract_label $like '%$label%')";
  
  if (($manager != $c_all) && ($manager != "")) {
    $where .= " AND (contract_techmanager_id='$manager'
                    OR contract_marketmanager_id='$manager') ";
  }
  if ($deal != "") {
    $where .= " AND (contract_deal_id = '$deal')";
  }
  if ($archive != "1") {
    $where .= " AND contract_archive != '1'";
  }

  $whereq = "WHERE $where";

  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "contract_dateexp";
  // Order exceptions
  if (strcmp($p_new_order,"contract_marketmanager")==0) {
    $order = "lmarket";
  }
  if (strcmp($p_new_order,"contract_techmanager")==0) {
    $order = "ltech";
  }
  $orderq .= " ORDER BY $order $p_order_dir";

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
    $whereq
    $orderq
    $limit
";

  if ($ctu_sql_limit) {
    $cq = "SELECT count(*) FROM Contract $join_comp $whereq";
    $count = get_query_count($cq);
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);
  }

  return($obm_q);
}


///////////////////////////////////////////////////////////////////////////////
// Contract detail query execution
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_detail($id) {
  global $cdg_sql;  

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $datebegin = sql_date_format($db_type, "contract_datebegin", "datebegin");
  $dateexp = sql_date_format($db_type, "contract_dateexp", "dateexp");
  $daterenew = sql_date_format($db_type, "contract_daterenew", "daterenew");
  $datecancel = sql_date_format($db_type, "contract_datecancel", "datecancel");
  $datesignature = sql_date_format($db_type, "contract_datesignature", "datesignature");
  $timeupdate = sql_date_format($db_type, "contract_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "contract_timecreate", "timecreate");

  $query = "SELECT Contract.*,
      deal_id,
      deal_label,
      contracttype_label,
      company_name,
      company_id,
      company_phone,
      company_fax,
      company_address1,
      company_zipcode,
      company_town,
      company_expresspostal,
      u1.userobm_lastname as lname1, u1.userobm_firstname as fname1,
      u2.userobm_lastname as lname2, u2.userobm_firstname as fname2,
      c1.contact_lastname as clname1, c1.contact_firstname as cfname1,
      c2.contact_lastname as clname2, c2.contact_firstname as cfname2,
      c1.contact_phone as cphone1, c2.contact_phone as cphone2,
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
         LEFT JOIN Contact c2 on contract_contact2_id=c2.contact_id
         LEFT JOIN Company on contract_company_id=company_id
         LEFT JOIN ContractPriority p on contract_priority_id=p.contractpriority_id
         LEFT JOIN ContractStatus s on contract_status_id=s.contractstatus_id
         LEFT JOIN ContractType on contract_type_id=contracttype_id
         LEFT JOIN Deal on contract_deal_id=deal_id
         LEFT JOIN UserObm as c on contract_usercreate=c.userobm_id
         LEFT JOIN UserObm as u on contract_userupdate=u.userobm_id
    WHERE contract_id = '$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query construction
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_insert($contract) {
  global $display, $cdg_sql, $auth, $cgp_show;

  $vis = ($contract["privacy"]==1)?1:0;
  $uid = $auth->auth["uid"];
  $label = $contract["label"];
  $comp_id = $contract["company_id"];
  $deal_id = $contract["deal_new_id"];
  if ($deal_id < 1) {
    $deal_id = $contract["deal_id"];
  }
  // In case deal module not used, to avoid postgres error
  if ($deal_id == "") {
    $deal_id = "0";
  }
  $num = $contract["number"];
  $clause = $contract["clause"];
  $datesignature = ($contract["datesignature"] ? "'".$contract["datesignature"]."'" : "null");
  $datebegin = ($contract["datebegin"] ? "'".$contract["datebegin"]."'" : "null");
  $dateexp = ($contract["dateexp"] ? "'".$contract["dateexp"]."'" : "null");
  $daterenew = ($contract["daterenew"] ? "'".$contract["daterenew"]."'" : "null");
  $datecancel = ($contract["datecancel"] ? "'".$contract["datecancel"]."'" : "null");
  $priority = ($contract["priority"] ? $contract["priority"] : "0");
  $status = $contract["status"];
  $contract_kind = $contract["kind"];
  $format = $contract["format"];
  $ticket_nb = ($contract["ticketnumber"] ? $contract["ticketnumber"] : "0");
  $duration = $contract["duration"];
  $autorenewal = ($contract["autorenewal"] ? $contract["autorenewal"] : "0");
  $sel_type = $contract["type"];
  $con1 = ($contract["contact1"] ? $contract["contact1"] : "0");
  $con2 = ($contract["contact2"] ? $contract["contact2"] : "0");
  $tech = ($contract["tech"] ? $contract["tech"] : "0");
  $market = ($contract["market"] ? $contract["market"] : "0");
  $add_comment = $contract["add_comment"];
  if ($add_comment != "") {
    $datecomment = $contract["datecomment"];
    $usercomment = $contract["usercomment"];
    $comment = "$datecomment:$usercomment:$add_comment";
  }
  $archive = ($contract["archive"] ? $contract["archive"] : "0");

  $query = "INSERT INTO Contract
     (contract_timeupdate,
      contract_timecreate,
      contract_userupdate,
      contract_usercreate,
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
      '$uid',
      '$uid',
      '$vis',  
      '$label',
      '$deal_id',
      '$comp_id',
      '$num',
      '$priority',      
      '$status',
      $datesignature,
      $datebegin,
      $dateexp,
      $daterenew,
      $datecancel,
      '$sel_type',
      '$autorenewal',
      '$contract_kind',    
      '$format',  
      '$ticket_nb',  
      '$duration',  
      '$con1',
      '$con2',
      '$tech',
      '$market',
      '$clause',
      '$archive',    
      '$comment')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  $c_id = run_query_contract_id($contract);

  return $c_id;
}


///////////////////////////////////////////////////////////////////////////////
// Update query construction
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_update($contract) {
  global $auth, $cdg_sql, $ccf_ticket, $ccf_duration;

  $vis = ($contract["privacy"]==1)?1:0;
  $id = $contract["id"];
  $num = $contract["number"];
  $label = $contract["label"];
  $datesignature = ($contract["datesignature"] ? "'".$contract["datesignature"]."'" : "null");
  $datebegin = ($contract["datebegin"] ? "'".$contract["datebegin"]."'" : "null");
  $dateexp = ($contract["dateexp"] ? "'".$contract["dateexp"]."'" : "null");
  $daterenew = ($contract["daterenew"] ? "'".$contract["daterenew"]."'" : "null");
  $datecancel = ($contract["datecancel"] ? "'".$contract["datecancel"]."'" : "null");
  $priority = ($contract["priority"] ? $contract["priority"] : "0");
  $status = $contract["status"];
  $contract_kind = $contract["kind"];
  $type = $contract["type"];
  $autorenewal = ($contract["autorenewal"] ? $contract["autorenewal"] : "0");
  $format = $contract["format"];
  if ($format == $ccf_ticket) {
    $ticket_nb = ($contract["ticketnumber"] ? $contract["ticketnumber"] : "0");
  } else {
    $ticket_nb = 0;
  }
  if ($format == $ccf_duration) {
    $duration = $contract["duration"];
  } else {
    $duration = 0;
  }
  $company_id = $contract["company_id"];
  $con1 = ($contract["contact1"] ? $contract["contact1"] : "0");
  $con2 = ($contract["contact2"] ? $contract["contact2"] : "0");
  $tech = ($contract["tech"] ? $contract["tech"] : "0");
  $market = ($contract["market"] ? $contract["market"] : "0");
  $comment = $contract["comment"];
  $add_comment = $contract["add_comment"];
  if ($add_comment != "") {
    $datecomment = $contract["datecomment"];
    $usercomment = $contract["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $clause = $contract["clause"];
  $deal_id = $contract["deal_new_id"];
  if ($deal_id < 1) {
    $deal_id = $contract["deal_id"];
  }
  // In case deal module not used, to avoid postgres error
  if ($deal_id == "") {
    $deal_id = "0";
  }
  $archive = ($contract["archive"] ? $contract["archive"] : "0");
  
  $query = "UPDATE Contract SET
      contract_timeupdate = '". date("Y-m-d H:i:s")."',
      contract_userupdate = '".$auth->auth["uid"]."',
      contract_privacy = '$vis',
      contract_number = '$num',
      contract_label = '$label',
      contract_deal_id = '$deal_id',
      contract_priority_id = '$priority',
      contract_status_id = '$status',
      contract_kind = '$contract_kind',
      contract_format = '$format',
      contract_autorenewal = '$autorenewal',
      contract_ticketnumber = '$ticket_nb',
      contract_duration = '$duration',
      contract_datesignature = $datesignature,
      contract_datebegin = $datebegin,
      contract_dateexp = $dateexp,
      contract_daterenew = $daterenew,
      contract_datecancel = $datecancel,
      contract_type_id = '$type',
      contract_company_id = '$company_id',
      contract_contact1_id = '$con1',
      contract_contact2_id = '$con2',
      contract_marketmanager_id = '$market',
      contract_techmanager_id = '$tech',
      contract_comment = '$comment',
      contract_clause = '$clause',
      contract_archive = '$archive'
    WHERE contract_id = '$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
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
  
  $query = "DELETE FROM Contract WHERE contract_id='$p_id'";

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

  if ($where_arch != "") {
    $where = "WHERE $where_arch";
  }

  $query = "SELECT DISTINCT
      userobm_id,
      userobm_lastname,
      userobm_firstname
    FROM Contract
      LEFT JOIN UserObm ON contract_marketmanager_id=userobm_id
    $where
  UNION DISTINCT
    SELECT DISTINCT
      userobm_id,
      userobm_lastname,
      userobm_firstname
    FROM Contract
      LEFT JOIN UserObm ON contract_techmanager_id=userobm_id
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
  
  $query = "SELECT
      incident_id,
      incident_duration
    FROM Incident
    WHERE incident_contract_id = '$id'
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

  $query = "SELECT * 
    FROM ContractPriority 
    ORDER BY contractpriority_code";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Incident Status list
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_status() {
  global $cdg_sql;

  $query = "SELECT * 
    FROM ContractStatus 
    ORDER BY contractstatus_code";

  display_debug_msg($query, $cdg_sql);
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
  global $auth, $cdg_sql;
	
  $code = $contract["pri_code"];
  $color = $contract["pri_color"];
  $label = $contract["pri_label"];

  $query = "INSERT INTO ContractPriority (
    contractpriority_timeupdate,
    contractpriority_timecreate,
    contractpriority_usercreate,
    contractpriority_code,
    contractpriority_color,
    contractpriority_label)
	values ('null',
    '".date("Y-m-d H:i:s")."',
    ".$auth->auth["uid"].",
    '$code',
    '$color',
    '$label')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
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
  global $auth, $cdg_sql;

  $priority = $contract["priority"];
  $code = $contract["pri_code"];
  $color = $contract["pri_color"];
  $label = $contract["pri_label"];

  $query = "UPDATE ContractPriority SET
    contractpriority_label='$label',
    contractpriority_timeupdate='".date("Y-m-d H:i:s")."',
    contractpriority_userupdate='".$auth->auth["uid"]."',
    contractpriority_code='$code',
    contractpriority_color='$color'
  WHERE contractpriority_id='$priority' ";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
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

  $query = "DELETE FROM ContractPriority WHERE contractpriority_id='$id'";

  display_debug_msg($query, $cdg_sql);
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

  $query = "SELECT
      contract_id, 
      contract_label
    FROM Contract 
    WHERE contract_priority_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
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
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $label = $contract["sta_label"];
  $code = $contract["sta_code"];

  $query = "INSERT INTO ContractStatus (
    contractstatus_timecreate,
    contractstatus_usercreate,
    contractstatus_label,
    contractstatus_code)
	values (
    '$timecreate',
    '$usercreate',
    '$label',
    '$code')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
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
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $status = $contract["status"];
  $label = $contract["sta_label"];
  $code = $contract["sta_code"];

  $query = "UPDATE ContractStatus SET
    contractstatus_label='$label',
    contractstatus_timeupdate='$timeupdate',
    contractstatus_userupdate='$userupdate',
    contractstatus_code='$code'
  WHERE contractstatus_id='$status'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
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

  $query = "DELETE FROM ContractStatus WHERE contractstatus_id='$id'";

  display_debug_msg($query, $cdg_sql);
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

  $query = "SELECT
      contract_id, 
      contract_label
    FROM Contract 
    WHERE contract_status_id='$s_id'";

  display_debug_msg($query, $cdg_sql);
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

  $query = "SELECT
      contractpriority_label
    FROM ContractPriority
    WHERE contractpriority_id='$id'";

  display_debug_msg($query, $cdg_sql);
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

  $query = "SELECT
      contractstatus_label
    FROM ContractStatus
    WHERE contractstatus_id='$id'";

  display_debug_msg($query, $cdg_sql);
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

  $obm_q = new DB_OBM;
  $query = "SELECT contracttype_label, contracttype_id FROM ContractType";
  display_debug_msg($query, $cdg_sql);
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

  $query = "SELECT
      company_name, 
      company_marketingmanager_id, 
      company_address1,
      company_zipcode,
      company_town 
    FROM Company 
    WHERE company_id='$id'";

  display_debug_msg($query, $cdg_sql);
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

  $query = "SELECT
      contact_id,
      contact_lastname,
      contact_firstname,
      contact_phone 
    FROM Contact 
    WHERE contact_company_id='$p_company_id'";
  
  display_debug_msg($query, $cdg_sql);
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

  if (! $archive) {
    $whereq = "WHERE contract_archive = 0";
  }

  $query = "SELECT *,
      company_name
    FROM Contract Left Join Company on contract_company_id=company_id
    $whereq
    ORDER BY company_name";

  display_debug_msg($query, $cdg_sql);
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
  global $auth, $cdg_sql;
	
  $label = $contract["type_label"];

  $query = "INSERT INTO ContractType (
    contracttype_timeupdate,
    contracttype_timecreate,
    contracttype_usercreate,
    contracttype_label)
  VALUES (null,
    '".date("Y-m-d H:i:s")."',
    ".$auth->auth["uid"].",
    '$label')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Type update
// Parameters:
//   - $contract[] : contract hash info : keys used : type, type_label
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_type_update($contract) {
  global $auth, $cdg_sql;

  $type = $contract["type"];
  $label = $contract["type_label"];

  $query = "UPDATE ContractType SET 
      contracttype_label='$label',
      contracttype_timeupdate='".date("Y-m-d H:i:s")."',
      contracttype_userupdate='".$auth->auth["uid"]."'
    WHERE contracttype_id='$type'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
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

  $query = "DELETE FROM ContractType WHERE contracttype_id='$id'";

  display_debug_msg($query, $cdg_sql);
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

  $query = "SELECT
      contract_id, 
      contract_label
    from Contract
    where contract_type_id='$t_id'";

  display_debug_msg($query, $cdg_sql);
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

  $query = "SELECT
      contracttype_label
    FROM ContractType
    WHERE contracttype_id='$id'";

  display_debug_msg($query, $cdg_sql);
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

  $query = "SELECT
      contract_id,
      contract_name,
    FROM Contract
    WHERE contract_id='$id'";

  display_debug_msg($query, $cdg_sql);
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

  $label = $contract["label"];
  $comp_id = $contract["company_id"];
  $num = $contract["number"];
  $clause = $contract["clause"];
  $datebegin = $contract["datebegin"];
  $dateexp = $contract["dateexp"];
  $priority = $contract["priority"];
  $status = $contract["status"];
  $contract_kind = $contract["kind"];
  $format = $contract["format"];
  $ticket_nb = $contract["ticketnumber"];
  $duration = $contract["duration"];
  $autorenewal =$contract["autorenewal"];
  $sel_type = $contract["type"];
  $com = $contract["comment"];

  // comment not searched because processed before stored in db
  $query = "SELECT
      contract_id
    FROM Contract
    WHERE contract_number = '$num'
      and contract_label = '$label'
      and contract_datebegin ='$datebegin'
      and contract_dateexp = '$dateexp'
      and contract_comment = '$com'
      and contract_clause = '$clause'
      and contract_company_id = '$comp_id'
      and contract_priority_id = '$priority'
      and contract_status_id = '$status'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
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
    $where_id = "AND contract_id!='$c_id'";
  }
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  $query = "SELECT DISTINCT 
      contract_id, 
      contract_label 
    FROM Contract
    WHERE contract_label $like '$name%'
      $where_id";

  display_debug_msg($query, $cdg_sql);
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

  $num = $contract["num"];
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
  global $err_msg, $ccf_ticket, $ccf_duration;
  global $php_regexp_isodate, $l_invalid_date, $l_err_fill;
  global $l_err_label_empty, $l_err_ticket_empty, $l_err_duration_empty;
  global $l_err_date, $l_err_daterenew, $l_err_datecancel;
  global $l_date, $l_signature,$l_begin,$l_exp,$l_renew,$l_date_cancel;

  $datebegin = $contract["datebegin"];
  $dateexp = $contract["dateexp"];
  $daterenew = $contract["daterenew"];
  $datecancel = $contract["datecancel"];
  $format = $contract["format"];
  $ticket_nb = $contract["ticketnumber"];
  $duration = $contract["duration"];

  // Check if the label is filled
  if (trim($contract["label"]) == "") {
    $err_msg = $l_err_label_empty;
    return false;
  }

  // Check if the Tickets number is filled
  if ($format == $ccf_ticket)
    if (($ticket_nb == "") || ($ticket_nb < 1)) {
      $err_msg = $l_err_ticket_empty;
      return false;
    }

  // Check if the Support duration is filled
  if ($format == $ccf_duration)
    if (($duration == "") || ($duration < 1)) {
      $err_msg = $l_err_duration_empty;
      return false;
    }

  // Check if the dates fields are filled properly
  // Begin Date check
  if ($datebegin != "") {
    if (preg_match($php_regexp_isodate, $datebegin) == 0) {
      $err_msg = "$l_begin : $l_invalid_date"; 
      return false;
    }
  } else {
    $err_msg = "$l_begin : $l_err_fill"; 
    return false;
  }

  // Expiration Date check
  if ($dateexp != "") {
    if (preg_match($php_regexp_isodate, $dateexp) == 0) {
      $err_msg = "$l_exp : $l_invalid_date"; 
      return false;
    }
  } else {
    $err_msg = "$l_exp : $l_err_fill"; 
    return false;
  }

  // Signature Date check
  if ( ($datesignature != "")
       && (preg_match($php_regexp_isodate, $datesignature) == 0) ) {
    $err_msg = "$l_signature : $l_invalid_date"; 
    return false;
  }

  // Renew Date check
  if ( ($daterenew != "")
       && (preg_match($php_regexp_isodate, $daterenew) == 0) ) {
    $err_msg = "$l_renew : $l_invalid_date"; 
    return false;
  }

  // Cancel Date check
  if ( ($datecancel != "")
       && (preg_match($php_regexp_isodate, $datecancel) == 0) ) {
    $err_msg = "$l_date_cancel : $l_invalid_date"; 
    return false;
  }


  if ($dateexp < $datebegin){
    $err_msg = $l_err_date;
    return false;
  }
  if (!($daterenew == "")) {
    if (($daterenew < $datebegin) || ($daterenew > $dateexp)){
      $err_msg = $l_err_daterenew;
      return false;
    }
  }
  if (!($datecancel == "")) {
    if (($datecancel < $datebegin) || ($datecancel > $dateexp)){
      $err_msg = $l_err_datecancel;
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
  global $err_msg, $ok_msg;
  global $l_link_incident, $l_link_incident_no;

  $delete_ok = true;

  // Links from Incident
  $nb = get_global_linked_incident_nb($p_id, "contract", true);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "$l_link_incident";
  } else {
    $ok_msg .= "$l_link_incident_no";
  }

  return $delete_ok;
}


?>
