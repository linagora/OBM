<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : contract_query.inc                                           //
//     - Desc : Contract Support query File                                  //
// 2002-02-02 : Aliacom - Cedric Bellegarde
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
function run_query_search($contract, $p_new_order, $p_order_dir) {
  global $cdg_sql, $c_all, $ctu_sql_limit;

  $p_label = sql_search_text_parse($contract["label"]);
  $p_type = $contract["type"];
  $p_dateafter = $contract["dateafter"];
  $p_datebefore = $contract["datebefore"];
  $comp = sql_search_text_parse($contract["company_name"]);
  $p_num = sql_search_text_parse($contract["number"]);
  $manager = $contract["manager"];
  $p_deal = $contract["deal_id"];
  $priority = $contract["priority"];
  $status = $contract["status"];
  $company_id = $contract["company_id"];
  $archive = $contract["archive"];
  $p_contract_kind = $contract["kind"];
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $datebegin = sql_date_format($db_type, "contract_datebegin", "contract_datebegin");
  $dateexp = sql_date_format($db_type, "contract_dateexp", "contract_dateexp");
  $where .= sql_obm_entity_privacy("contract");

  if (($p_type != $c_all) && ($p_type != ""))
    $where .= " and contract_type_id = '$p_type'";

  if ($p_num != "")
    $where .= " and (contract_number $like '%$p_num%')";

  if (($status != $c_all) && ($status != ""))
    $where .= " and (contract_status_id = '$status')";

  if (($p_contract_kind != $c_all) && ($p_contract_kind != ""))
    $where .= " and (contract_kind = '$p_contract_kind')";

  if (($priority != $c_all) && ($priority != ""))
    $where .= " and (contract_priority_id = '$priority')";

  if ($comp != "") {
    $where .= " and (company_name $like '$comp%')";
    $join_comp = "left join Company on contract_company_id=company_id";
  }
  if ($company_id != "")
    $where .= " and contract_company_id = '$company_id'";

  if ($p_dateafter != "")
    $where .= " and (contract_dateexp >= '$p_dateafter')";

  if ($p_datebefore != "")
    $where .= " and (contract_dateexp <= '$p_datebefore')";

  if ($p_label != "")
    $where .= " and (contract_label $like '%$p_label%')";
  
  if (($manager != $c_all) && ($manager != "")) {
    $where .= " and (contract_techmanager_id='$manager'
                    or contract_marketmanager_id='$manager') ";
  }
  if ($p_deal != "") {
    $where .= " and (contract_deal_id = '$p_deal')";
  }
  if ($archive != "1") {
    $where .= " and contract_archive != '1'";
  }

  $whereq = "where $where";

  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "contract_dateexp";
  // Order exceptions
  if (strcmp($p_new_order,"contract_marketmanager")==0) {
    $order = "lmarket";
  }
  if (strcmp($p_new_order,"contract_techmanager")==0) {
    $order = "ltech";
  }
  $orderq .= " order by $order $p_order_dir";

  $query = "select contract_label,
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
      contract_archive,
      $datebegin,
      $dateexp
    from Contract
         left join Company on contract_company_id=company_id
         left join UserObm u1 on contract_techmanager_id=u1.userobm_id
         left join UserObm u2 on contract_marketmanager_id=u2.userobm_id
         left join ContractType on contract_type_id=contracttype_id
         left join ContractPriority on contract_priority_id = contractpriority_id
         left join ContractStatus on contract_status_id = contractstatus_id
    $whereq
    $orderq
    $limit
";

  if ($ctu_sql_limit) {
    $cq = "select count(*) from Contract $join_comp $whereq";
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
// Contract deatil query execution                                           //
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_contract_id) {
  global $cdg_sql, $db_type_mysql,$db_type_pgsql;  

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $datebegin = sql_date_format($db_type, "contract_datebegin", "datebegin");
  $dateexp = sql_date_format($db_type, "contract_dateexp", "dateexp");
  $daterenew = sql_date_format($db_type, "contract_daterenew", "daterenew");
  $datecancel = sql_date_format($db_type, "contract_datecancel", "datecancel");
  $datesignature = sql_date_format($db_type, "contract_datesignature", "datesignature");
  $timeupdate = sql_date_format($db_type, "contract_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "contract_timecreate", "timecreate");

  $query = "select Contract.*,
      deal_id,
      deal_label,
      contracttype_label,
      company_name,
      company_id,
      company_address1,
      company_zipcode,
      company_town,
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
    from Contract
         left join UserObm u1 on contract_marketmanager_id=u1.userobm_id
         left join UserObm u2 on contract_techmanager_id=u2.userobm_id
         left join Contact c1 on contract_contact1_id=c1.contact_id
         left join Contact c2 on contract_contact2_id=c2.contact_id
         left join Company on contract_company_id=company_id
         left join ContractPriority p on contract_priority_id=p.contractpriority_id
         left join ContractStatus s on contract_status_id=s.contractstatus_id
         left join ContractType on contract_type_id=contracttype_id
         left join Deal on contract_deal_id=deal_id
         left join UserObm as c on contract_usercreate=c.userobm_id
         left join UserObm as u on contract_userupdate=u.userobm_id
    where contract_id='$p_contract_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}




///////////////////////////////////////////////////////////////////////////////
// Contract deatil query execution                                           //
///////////////////////////////////////////////////////////////////////////////
function run_query_detail_company($company_id) {
  global $cdg_sql, $db_type_mysql,$db_type_pgsql;  

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
 
  $query = "select *
            from Company
            where company_id='$company_id'";

  display_debug_msg($query, $cdg_sql);

 $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query construction                                              //
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($contract) {
  global $cdg_sql;
  global $auth;

  $vis = ($contract["privacy"]==1)?1:0;
  $uid = $auth->auth["uid"];
  $p_label = $contract["label"];
  $comp_id = $contract["company_id"];
  $p_num = $contract["number"];
  $ta_clause = $contract["clause"];
  $p_datebegin = $contract["datebegin"];
  $p_dateexp = $contract["dateexp"];
  $priority = $contract["priority"];
  $status = $contract["status"];
  $p_contract_kind = $contract["kind"];
  $format = $contract["format"];
  $p_tf_no_ticket =$contract["ticketnumber"];
  $p_tf_supportduration = $contract["duration"];
  $p_autorenewal =$contract["autorenewal"];
  $p_daterenew = $contract["daterenew"];
  $p_datecancel = $contract["datecancel"];
  $p_datesignature = $contract["datesignature"];
  $sel_type = $contract["type"];
  $ta_com = $contract["comment"];
  $con1 = ($contract["contact1"] ? $contract["contact1"] : "0");
  $con2 = ($contract["contact2"] ? $contract["contact2"] : "0");
  $sel_tech  = $contract["tech"];
  $sel_market = $contract["market"];
  $deal_id = ($contract["deal_new_id"] ? $contract["deal_new_id"] : "0");
  $archive = ($contract["archive"] ? $contract["archive"] : "0");
  $connect_db = new DB_OBM;

  $query = "insert into Contract
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
    values (
      null,
      '". date("Y-m-d H:i:s") ."', 
      '$uid',
      '$uid',
      '$vis',  
      '$p_label',
      '$deal_id',
      '$comp_id',
      '$p_num',
      '$priority',      
      '$status',
      '$p_datesignature',
      '$p_datebegin',
      '$p_dateexp',
      '$p_daterenew',
      '$p_datecancel',
      '$sel_type',
      '$p_autorenewal',
      '$p_contract_kind',    
      '$format',  
      '$p_tf_no_ticket',  
      '$p_tf_supportduration',  
      '$con1',
      '$con2',
      '$sel_tech',
      '$sel_market',
      '$ta_clause',
      '$archive',    
      '$ta_com')";


  display_debug_msg($query, $cdg_sql);
  $retour = $connect_db->query($query);

  if ($retour) {
    $display["msg"] .= display_ok_msg($l_insert_ok);
  } else {
      $display["msg"] .= display_err_msg($l_insert_error);
    }

  $c_id = run_query_contract_id($contract);

  return $c_id;
}



///////////////////////////////////////////////////////////////////////////////
// Update query construction                                                 //
///////////////////////////////////////////////////////////////////////////////
function run_query_update($contract) {
  global $auth, $cdg_sql, $ccf_ticket, $ccf_duration;

  $vis = ($contract["privacy"]==1)?1:0;
  $id = $contract["id"];
  $p_num = $contract["number"];
  $p_label = $contract["label"];
  $p_datebegin = $contract["datebegin"];
  $p_dateexp = $contract["dateexp"];
  $priority = $contract["priority"];
  $status = $contract["status"];
  $p_contract_kind = $contract["kind"];
  $p_daterenew = $contract["daterenew"];
  $p_datecancel = $contract["datecancel"];
  $p_datesignature = $contract["datesignature"];
  $p_type = $contract["type"];
  $p_autorenewal = $contract["autorenewal"];
  $format =$contract["format"];
 
  if ($format == $ccf_ticket) {
    $p_tf_no_ticket = $contract["ticketnumber"];
  } else {
    $p_tf_no_ticket =0;
  }
  if ($format == $ccf_duration) {
    $p_tf_supportduration = $contract["duration"];
  } else {
    $p_tf_supportduration = 0;
  }

  $p_company_id = $contract["company_id"];
  $sel_con1 =  $contract["contact1"];
  $sel_con2 =  $contract["contact2"];
  $sel_tech  = $contract["tech"];
  $sel_market = $contract["market"];
  $p_comment = $contract["comment"];
  $p_clause = $contract["clause"];
  $deal_id = ($contract["deal_new_id"] ? $contract["deal_new_id"] : $contract["deal_id"]);
  $archive = ($contract["archive"] ? $contract["archive"] : "0");
  $connect_db=new DB_OBM;
  
  if ($sel_con1 == "")
    $sel_con1 = "null";
  if ($sel_con2 == "")
    $sel_con2 = "null";
  if ($sel_market == "")
    $sel_market = "null";
  if ($sel_tech == "")
    $sel_tech = "null";

  $query = "update Contract set
      contract_timeupdate = '". date("Y-m-d H:i:s")."',
      contract_userupdate = '".$auth->auth["uid"]."',
      contract_privacy='$vis',
      contract_number = '$p_num',
      contract_label = '$p_label',
      contract_deal_id = '$deal_id',
      contract_priority_id='$priority',
      contract_status_id='$status',
      contract_kind = '$p_contract_kind',
      contract_format = '$format',
      contract_autorenewal = '$p_autorenewal',
      contract_ticketnumber = '$p_tf_no_ticket',
      contract_duration = '$p_tf_supportduration',
      contract_datesignature = '$p_datesignature',
      contract_datebegin = '$p_datebegin',
      contract_dateexp = '$p_dateexp',
      contract_daterenew = '$p_daterenew',
      contract_datecancel = '$p_datecancel',
      contract_type_id = '$p_type',
      contract_company_id = '$p_company_id',
      contract_contact1_id = '$sel_con1',
      contract_contact2_id = '$sel_con2',
      contract_marketmanager_id = '$sel_market',
      contract_techmanager_id = '$sel_tech',
      contract_comment = '$p_comment',
      contract_clause = '$p_clause',
      contract_archive = '$archive'
     
    where contract_id = '$id'";

  display_debug_msg($query, $cdg_sql);
  $ret = $connect_db->query($query);  

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Delete query execution                                                    //
// Parameters:
//   - $p_id : contract id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_id) {
  global $cdg_sql;
  
  $obm_q = new DB_OBM;
  $query = "delete from Contract where contract_id='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $ret = $obm_q->query($query);

  return $ret;
}

///////////////////////////////////////////////////////////////////////////////
// Resolution duration incident query                                        //
// Parameters:
//   - $id : contract id
////////////////////////////////////////////////////////////////////////////

function run_query_coupon_final($id){
  global $cdg_sql;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type; 
  
  $query = "select 
              incident_id, 
              incident_duration 
            from Incident
              where incident_contract_id= '$id'";
 
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Resolution duration incident query    (for the last incident)             //
// Parameters:
//   - $max :  last contract id
//////////////////////////////////////////////////////////////////////////// 

function run_query_coupon_final_duration($max){
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type; 

  $query = "select 
              incident_duration 
            from Incident
              where incident_id = '$max'";
 

display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q;
}



///////////////////////////////////////////////////////////////////////////////
// Query execution : Contract Priority list                                  //
///////////////////////////////////////////////////////////////////////////////
function run_query_priority() {
  global $cdg_sql;

  $query = "select * 
            from ContractPriority 
            order by contractpriority_order";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}



///////////////////////////////////////////////////////////////////////////////
// Query execution : Incident Status list                                    //
///////////////////////////////////////////////////////////////////////////////
function run_query_status() {
  global $cdg_sql;

  $query = "select * 
            from ContractStatus 
            order by contractstatus_order";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}



///////////////////////////////////////////////////////////////////////////////
// Query execution : Priority insert                                         //
// Parameters:
//   - $contract[] : contract hash info : keys used : pri_label, pri_order
///////////////////////////////////////////////////////////////////////////////
function run_query_priority_insert($contract) {
  global $auth, $cdg_sql;
	
  $order = $contract["pri_order"];
  $color = $contract["pri_color"];
  $label = $contract["pri_label"];

  $query = "insert into ContractPriority (
    contractpriority_timeupdate,
    contractpriority_timecreate,
    contractpriority_usercreate,
    contractpriority_order,
    contractpriority_color,
    contractpriority_label)
  values ('null',
    '".date("Y-m-d H:i:s")."',
    ".$auth->auth["uid"].",
    '$order',
    '$color',
    '$label')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}



///////////////////////////////////////////////////////////////////////////////
// Query execution : Priority update                                         //
// Parameters:
//   - $contract[] : contract hash info :
//     keys used : priority, pri_label, pri_order
///////////////////////////////////////////////////////////////////////////////
function run_query_priority_update($contract) {
  global $auth, $cdg_sql;

  $priority = $contract["priority"];
  $order = $contract["pri_order"];
  $color = $contract["pri_color"];
  $label = $contract["pri_label"];

  $query = "update ContractPriority set 
    contractpriority_label='$label',
    contractpriority_timeupdate='".date("Y-m-d H:i:s")."',
    contractpriority_userupdate='".$auth->auth["uid"]."',
    contractpriority_order='$order',
    contractpriority_color='$color'
  where contractpriority_id='$priority' ";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}



///////////////////////////////////////////////////////////////////////////////
// Query execution : Priority deletion                                       //
// Parameters:
//   - $id : Priority id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_priority_delete($id) {
  global $cdg_sql;

  $query = "delete from ContractPriority where contractpriority_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $sta_q = new DB_OBM;
  $retour = $sta_q->query($query);

  return $retour;
}



///////////////////////////////////////////////////////////////////////////////
// Query execution : Links to the Priority given                             //
// Parameters:
//   - $s_id : priority id
///////////////////////////////////////////////////////////////////////////////
function run_query_priority_links($p_id) {
  global $cdg_sql;

  $query = "select 
                 contract_id, 
                 contract_label
            from Contract 
            where contract_priority_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $pri_q = new DB_OBM;
  $pri_q->query($query);

  return $pri_q;
}



///////////////////////////////////////////////////////////////////////////////
// Query execution : Status insert                                           //
// Parameters:
//   - $contract[] : contract hash info : keys used : sta_label, sta_order
///////////////////////////////////////////////////////////////////////////////
function run_query_status_insert($contract) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $label = $contract["sta_label"];
  $order = $contract["sta_order"];

  $query = "insert into ContractStatus (
    contractstatus_timecreate,
    contractstatus_usercreate,
    contractstatus_label,
    contractstatus_order)
  values (
    '$timecreate',
    '$usercreate',
    '$label',
    '$order')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}



///////////////////////////////////////////////////////////////////////////////
// Query execution : Status update                                           //
// Parameters:
//   - $contract[] : contract hash info :
//     keys used : status, sta_label, sta_order
///////////////////////////////////////////////////////////////////////////////
function run_query_status_update($contract) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $status = $contract["status"];
  $label = $contract["sta_label"];
  $order = $contract["sta_order"];

  $query = "update ContractStatus set contractstatus_label='$label',
    contractstatus_timeupdate='$timeupdate',
    contractstatus_userupdate='$userupdate',
    contractstatus_order='$order'
  where contractstatus_id='$status'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}



///////////////////////////////////////////////////////////////////////////////
// Query execution : Status deletion                                         //
// Parameters:
//   - $id : Status id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_status_delete($id) {
  global $cdg_sql;

  $query = "delete from ContractStatus where contractstatus_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $sta_q = new DB_OBM;
  $retour = $sta_q->query($query);

  return $retour;
}



///////////////////////////////////////////////////////////////////////////////
// Query execution : Links to the Status given                               //
// Parameters:
//   - $s_id : status id
///////////////////////////////////////////////////////////////////////////////
function run_query_status_links($s_id) {
  global $cdg_sql;

  $query = "select 
                 contract_id, 
                 contract_label
            from Contract 
            where contract_status_id='$s_id'";

  display_debug_msg($query, $cdg_sql);
  $sta_q = new DB_OBM;
  $sta_q->query($query);

  return $sta_q;
}



///////////////////////////////////////////////////////////////////////////////
// Get a priority label                                                      //
// Parameters:
//   - $id : priority id
///////////////////////////////////////////////////////////////////////////////
function get_priority_label($id) {
  global $cdg_sql;

  $query = "select 
                 contractpriority_label
            from ContractPriority
            where contractpriority_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("contractpriority_label");
  return $retour;
}



///////////////////////////////////////////////////////////////////////////////
// Get a status label                                                        //
// Parameters:
//   - $id : status id
///////////////////////////////////////////////////////////////////////////////
function get_status_label($id) {
  global $cdg_sql;

  $query = "select 
              contractstatus_label
            from ContractStatus
            where contractstatus_id='$id'";

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
function run_query_contracttype() {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $query = "select contracttype_label,contracttype_id from ContractType";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the incidents attached to the contract
// Parameters:
//   - $p_id : contract Id
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_incident_links($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $query = "select distinct 
                 incident_id, 
                 incident_label 
            from Incident
            where incident_contract_id='$p_id'";

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

  $query = "select 
                 company_name, 
                 company_marketingmanager_id, 
                 company_address1,
                 company_zipcode,
                 company_town 
           from Company 
           where company_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $connect_db=new DB_OBM;
  $connect_db->query($query);
  $connect_db->next_record();

  return $connect_db;
}
 


///////////////////////////////////////////////////////////////////////////////
// Query   : company contacts contract  :  Table Contact                     //
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_contract($p_company_id) {
  global $cdg_sql;

  $connect_db = new DB_OBM;
  $query = "select 
                 contact_id,
                 contact_lastname,
                 contact_firstname,
                 contact_phone 
            from Contact 
            where contact_company_id='$p_company_id'";
  
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);

  return $connect_db;
}



///////////////////////////////////////////////////////////////////////////////
// Query   :  contract  :  Table Contract                        //
///////////////////////////////////////////////////////////////////////////////
function run_query_contract() {
  global $cdg_sql;

  $query = "select *, 
                 company_name
            from Contract Left Join Company on contract_company_id=company_id
            order by company_name";

  display_debug_msg($query, $cdg_sql);
  $con_q = new DB_OBM;
  $con_q->query($query);

  return $con_q;
}



///////////////////////////////////////////////////////////////////////////////
// Query execution : Type insert                                             //
// Parameters:
//   - $contract[] : contract hash info : keys used : type_label
///////////////////////////////////////////////////////////////////////////////
function run_query_type_insert($contract) {
  global $auth, $cdg_sql;
	
  $label = $contract["type_label"];

  $query = "insert into ContractType (
    contracttype_timeupdate,
    contracttype_timecreate,
    contracttype_usercreate,
    contracttype_label)
  values (null,
    '".date("Y-m-d H:i:s")."',
    ".$auth->auth["uid"].",
    '$label')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}



///////////////////////////////////////////////////////////////////////////////
// Query execution : Type update                                             //
// Parameters:
//   - $contract[] : contract hash info : keys used : type, type_label
///////////////////////////////////////////////////////////////////////////////
function run_query_type_update($contract) {
  global $auth, $cdg_sql;

  $type = $contract["type"];
  $label = $contract["type_label"];

  $query = "update ContractType 
            set 
      contracttype_label='$label',
      contracttype_timeupdate='".date("Y-m-d H:i:s")."',
      contracttype_userupdate='".$auth->auth["uid"]."'
            where contracttype_id='$type'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}



///////////////////////////////////////////////////////////////////////////////
// Query execution : Type deletion                                           //
// Parameters:
//   - $id : Type id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_type_delete($id) {
  global $cdg_sql;

  $query = "delete from ContractType where contracttype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}



///////////////////////////////////////////////////////////////////////////////
// Query execution : Links to the Type given                                 //
// Parameters:
//   - $t_id : type id
///////////////////////////////////////////////////////////////////////////////
function run_query_type_links($t_id) {
  global $cdg_sql;

  $query = "select 
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
// Get the label of a type                                                   //
// Parameters:
//   - $id : type id
///////////////////////////////////////////////////////////////////////////////
function get_type_label($id) {
  global $cdg_sql;

  $query = "select 
                 contracttype_label
            from ContractType
            where contracttype_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("contracttype_label");
  return $retour;
}



///////////////////////////////////////////////////////////////////////////////
// Get the infos of a List from its Id                                       //
// Parameters:
//   - $id : list id
///////////////////////////////////////////////////////////////////////////////
function get_contract_info($id) {
  global $cdg_sql;

  $query = "select 
                 contract_id,
                 contract_name,
            from Contract
            where contract_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $res["id"] = $obm_q->f("contract_id");
  $res["name"] = $obm_q->f("contract_name");
 

  return $res;
}



///////////////////////////////////////////////////////////////////////////////
// Contract ID : Get the id of the contract inserted.                                //
// Parameters:
//   - $contract[] : contract hash info : keys used : all
// Returns:
//   contract Id if found else false
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_id($contract) {
  global $cdg_sql,$c_id;

  $p_label = $contract["label"];
  $comp_id = $contract["company_id"];
  $p_num = $contract["number"];
  $ta_clause = $contract["clause"];
  $p_datebegin = $contract["datebegin"];
  $p_dateexp = $contract["dateexp"];
  $priority = $contract["priority"];
  $status = $contract["status"];
  $p_contract_kind = $contract["kind"];
  $format =$contract["format"];
  $p_tf_no_ticket =$contract["ticketnumber"];
  $p_tf_supportduration = $contract["duration"];
  $p_autorenewal =$contract["autorenewal"];
  $p_daterenew = $contract["daterenew"];
  $p_datecancel = $contract["datecancel"];
  $p_datesignature = $contract["datesignature"];
  $sel_type = $contract["type"];
  $ta_com = $contract["comment"];
  $sel_tech  = $contract["tech"];
  $sel_market = $contract["market"];
  $connect_db = new DB_OBM;

  // comment not searched because processed before stored in db
  $query = "select contract_id from Contract Where 
    contract_number='$p_num' and
    contract_label='$p_label' and
    contract_datebegin ='$p_datebegin' and
    contract_dateexp = '$p_dateexp' and
    contract_comment = '$ta_com' and
    contract_clause = '$ta_clause' and
    contract_datecancel = '$p_datecancel'and
    contract_datesignature = '$p_datesignature' and
    contract_autorenewal = '$p_autorenewal' and
    contract_company_id = '$comp_id' and
    contract_priority_id = '$priority' and
    contract_status_id = '$status'";
         
   
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
/////////////////////////////////////////////////////////////////////////////////
function run_query_check_contract($c_id, $name) {
  global $cdg_sql;

  $where_id = "";
  if ($c_id != "") {
    $where_id = "and contract_id!='$c_id'";
  }
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  $query = "select distinct 
                 contract_id, 
                 contract_label 
            from Contract
            where contract_label $like '$name%'
              $where_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}



///////////////////////////////////////////////////////////////////////////////
// Contract environment checking (same contracts exists ?)                    //
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
  global $cb_arc, $lcb_arc,$tf_datealarme,$tf_datebegin,$tf_daterenew,$tf_datecancel,$tf_datesignature;
  global $err_msg, $l_err_label_empty,$tf_no_ticket,$tf_supportduration,$tf_no_ticket;
  global $l_err_ticket_empty, $l_err_duration_empty,$tf_dateexp;
  global $ccf_ticket, $ccf_duration,$l_err_date,$l_err_daterenew,$l_err_datecancel;

  $format = $contract["format"];

  // Check if the label is filled
  if (trim($contract["label"]) == "") {
    $err_msg = $l_err_label_empty;
    return false;
  }

  // Check if the Tickets number is filled

  if ($format == $ccf_ticket)
    if ($tf_no_ticket == "" || $tf_no_ticket < 1){
      $err_msg = $l_err_ticket_empty;
      return false;
    }

  // Check if the Support duration is filled

  if ($format == $ccf_duration)
    if ($tf_supportduration == "" || $tf_supportduration < 1){
      $err_msg = $l_err_duration_empty;
      return false;
    }

  // Check if the dates fields are filled properly

  if ($tf_dateexp < $tf_datebegin){
    $err_msg = $l_err_date;
    return false;
  }
  if (!($tf_daterenew == "")){
    if (($tf_daterenew < $tf_datebegin) || ($tf_daterenew > $tf_dateexp)){
      $err_msg = $l_err_daterenew;
      return false;
    }
  }
  if (!($tf_datecancel == "")){
    if (($tf_datecancel < $tf_datebegin) || ($tf_datecancel > $tf_dateexp)){
      $err_msg = $l_err_datecancel;
      return false;
    }
  }  

  // Date alarme check

  if ($tf_datebegin == "") {
    $tf_datebegin = "null";
  } else {
      $tf_datebegin = "'$tf_datebegin'";
    }

  // Date signature check

  if ($tf_datesignature == "") {
    $tf_datesignature = "null";
  } else {
      $tf_datesignature = "'$tf_datesignature'";
    }

  // Date renew check

  if ($tf_daterenew == "") {
    $tf_daterenew = "null";
  } else {
      $tf_daterenew = "'$tf_daterenew'";
    }

  // Date cancel check
  if ($tf_datecancel == "") {
    $tf_datecancel = "null";
  } else {
      $tf_datecancel = "'$tf_datecancel'";
    }


  if ($cb_arc == "archives") {
    $lcb_arc = 1;
  } else {
      $lcb_arc = 0;
   }

  // Date alarme check

  if ($tf_datealarme == "") {
    $tf_datealarme = "null";
  } else {
      $tf_datealarme = "'$tf_datealarme'";
    }

  return true;
}

</script>
