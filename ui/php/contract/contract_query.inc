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

  $p_label = $contract["label"];
  $p_type = $contract["type"];
  $p_dateafter = $contract["dateafter"];
  $p_datebefore = $contract["datebefore"];
  $comp = $contract["company_name"];
  $p_num = $contract["number"];
  $manager = $contract["manager"];
  $p_deal = $contract["deal_id"];
  $company_id = $contract["company_id"];
  $archive = $contract["archive"];
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $dateexp = sql_date_format($db_type, "contract_dateexp", "contract_dateexp");

  $where = "1=1";
  if (($p_type != $c_all) && ($p_type != ""))
    $where .= " and contract_type_id = '$p_type'";

  if ($p_num != "")
    $where .= " and (contract_number $like '%$p_num%')";
  
  if ($comp != "")
    $where .= " and (company_name $like '$comp%')";

  if ($company_id != "")
    $where .= " and company_id = '$company_id'";

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
      contract_number,
      contract_company_id,
      company_name as contract_company_name,
      contracttype_label,
      contract_type_id,
      contract_archive,
      contract_techmanager_id,
      contract_marketmanager_id,
      u1.userobm_lastname as ltech, u1.userobm_firstname as ftech,
      u2.userobm_lastname as lmarket, u2.userobm_firstname as fmarket,
      contract_archive,
      $dateexp
    from Contract
         left join Company on contract_company_id=company_id
         left join UserObm u1 on contract_techmanager_id=u1.userobm_id
         left join UserObm u2 on contract_marketmanager_id=u2.userobm_id
         left join ContractType on contract_type_id=contracttype_id
    $whereq
    $orderq
    $limit
";

  if ($ctu_sql_limit) {
    $cq = "select count(*)
      from Contract
           left join Company on contract_company_id=company_id
      $whereq";
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
  $timeupdate = sql_date_format($db_type, "contract_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "contract_timecreate", "timecreate");

  $query = "select Contract.*,
      deal_id,
      deal_label,
      contracttype_label,
      company_name,
      company_address1,
      company_zipcode,
      company_town,
      u1.userobm_lastname as lname1, u1.userobm_firstname as fname1,
      u2.userobm_lastname as lname2, u2.userobm_firstname as fname2,
      c1.contact_lastname as clname1, c1.contact_firstname as cfname1,
      c2.contact_lastname as clname2, c2.contact_firstname as cfname2,
      c1.contact_phone as cphone1, c2.contact_phone as cphone2,
      $datebegin,
      $dateexp,
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
// Insertion query construction                                              //
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($contract) {
  global $cdg_sql;
  global $auth;

  $uid = $auth->auth["uid"];

  $p_label = $contract["label"];
  $comp_id = $contract["company_id"];
  $p_num = $contract["number"];
  $ta_clause = $contract["clause"];
  $p_datebegin = $contract["datebegin"];
  $p_dateexp = $contract["dateexp"];
  $sel_type = $contract["type"];
  $ta_com = $contract["comment"];
  $con1 = ($contract["contact1"] ? $contract["contact1"] : "0");
  $con2 = ($contract["contact2"] ? $contract["contact2"] : "0");
  $sel_tech  = $contract["tech"];
  $sel_market = $contract["market"];
  $deal_id = ($contract["deal_id"] ? $contract["deal_id"] : "0");
  $archive = ($contract["archive"] ? $contract["archive"] : "0");
  $connect_db = new DB_OBM;

  $query = "insert into Contract
     (contract_timeupdate,
      contract_timecreate,
      contract_userupdate,
      contract_usercreate,
      contract_label,
      contract_deal_id, 
      contract_company_id,
      contract_number,
      contract_datebegin,
      contract_dateexp,
      contract_type_id,
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
      '$p_label',
      '$deal_id',
      '$comp_id',
      '$p_num',
      '$p_datebegin',
      '$p_dateexp',
      '$sel_type',
      '$con1',
      '$con2',
      '$sel_tech',
      '$sel_market',
      '$ta_clause',
      '$archive',
      '$ta_com')";

  display_debug_msg($query, $cdg_sql);
  $retour = $connect_db->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Update query construction                                                 //
///////////////////////////////////////////////////////////////////////////////
function run_query_update($contract) {
  global $auth, $cdg_sql;

  $id = $contract["id"];
  $p_num = $contract["number"];
  $p_label = $contract["label"];
  $p_datebegin = $contract["datebegin"];
  $p_dateexp = $contract["dateexp"];
  $p_type = $contract["type"];
  $p_company_id = $contract["company_id"];
  $sel_con1 =  $contract["contact1"];
  $sel_con2 =  $contract["contact2"];
  $sel_tech  = $contract["tech"];
  $sel_market = $contract["market"];
  $p_comment = $contract["comment"];
  $p_clause = $contract["clause"];
  $deal_id = ($contract["deal_id"] ? $contract["deal_id"] : "0");
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
      contract_number = '$p_num',
      contract_label = '$p_label',
      contract_deal_id = '$deal_id',
      contract_datebegin = '$p_datebegin',
      contract_dateexp = '$p_dateexp',
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
  $query = "select distinct incident_id, incident_label 
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

  $query = "select company_name, company_marketingmanager_id, company_address1,company_zipcode,company_town from Company where company_id='$id'";

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
  $query = "select contact_id,contact_lastname,contact_firstname,contact_phone from Contact where contact_company_id='$p_company_id'";
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
  return $connect_db;
}


///////////////////////////////////////////////////////////////////////////////
// Query   :  contract  :  Table Contract                        //
///////////////////////////////////////////////////////////////////////////////
function run_query_contract() {
  global $cdg_sql;

  $query = "select *, company_name
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

  $query = "insert into ContractType (contracttype_timeupdate,
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

  $query = "update ContractType set contracttype_label='$label',
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

  $query = "select contract_id, contract_label
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

  $query = "select contracttype_label
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
// Contract Form Data checking and formatting
// Parameters:
//   - $cid        : contract id : (empty on insertion)
//   - $contract[] : values to check
// Return : true if check ok, else false
///////////////////////////////////////////////////////////////////////////////
function check_contract_form($cid, $contract) {
  global $cb_arc, $lcb_arc,$tf_datealarme,$tf_datebegin;
  global $err_msg, $l_err_label_empty;

  // Check if the label is filled
  if (trim($contract["label"]) == "") {
    $err_msg = $l_err_label_empty;
    return false;
  }

  // Date alarme check
  if ($tf_datebegin == "") {
    $tf_datebegin = "null";
  } else {
    $tf_datebegin = "'$tf_datebegin'";
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
