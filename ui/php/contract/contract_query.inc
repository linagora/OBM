<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : contract_query.inc                                           //
//     - Desc : Contract Support query File                                  //
// 2002-02-02 : Cedric Bellegarde
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
  global $cdg_sql, $c_all;

  $p_label = $contract["label"];
  $p_type = $contract["type"];
  $p_dateafter = $contract["dateafter"];
  $p_datebefore = $contract["datebefore"];
  $comp = $contract["company_name"];
  $p_num = $contract["number"];
  $manager = $contract["manager"];
  $p_deal = $contract["deal_id"];

  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "contract_dateexp";
  
  $connect_db = new DB_OBM;
  
  $query = "select contract_label,
      contract_id,
      contract_id as Id,
      contract_deal_id,
      contract_number,
      contract_company_id,
      company_name as contract_company_name,
      contracttype_label,
      contract_dateexp,
      contract_type_id,
      contract_techmanager_id,
      contract_marketmanager_id,
      u1.userobm_lastname as ltech, u1.userobm_firstname as ftech,
      u2.userobm_lastname as lmarket, u2.userobm_firstname as fmarket,
      UNIX_TIMESTAMP(contract_dateexp) as contract_dateexp
    from ((Contract
      left join UserObm u1 on contract_techmanager_id=u1.userobm_id)
      left join UserObm u2 on contract_marketmanager_id=u2.userobm_id),
      Company, ContractType
    where contract_company_id=company_id
      and contracttype_id=contract_type_id";

  if (($p_type != $c_all) && ($p_type != ""))
    $query .= " and contract_type_id = '$p_type'";

  if ($p_num != "")
    $query .= " and (contract_number like '%$p_num%')";
  
  if ($comp != "")
    $query .= " and (company_name like '%$comp%')";

  if ($p_dateafter != "")
    $query .= " and (contract_dateexp >= '$p_dateafter')";

  if ($p_datebefore != "")
    $query .= " and (contract_dateexp <= '$p_datebefore')";

  if ($p_label != "")
    $query .= " and (contract_label like '%$p_label%')";
  
  if (($manager != $c_all) && ($manager != "")) {
    $query .= " and (contract_techmanager_id='$manager'
                    or contract_marketmanager_id='$manager') ";
  } 
  if ($p_deal != "")
    $query .= " and (contract_deal_id = '$p_deal')";

  $query .= " order by $order $p_order_dir";

  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
  return($connect_db);

}


///////////////////////////////////////////////////////////////////////////////
// ContractContract: Select query construction                               //
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_contract_id) {
  global $cdg_sql, $db_type_mysql,$db_type_pgsql;  

  $connect_db = new DB_OBM;
  if ($connect_db->type == $db_type_mysql) {
    $query = "select *,
        u1.userobm_lastname as lname1, u1.userobm_firstname as fname1,
        u2.userobm_lastname as lname2, u2.userobm_firstname as fname2,
        UNIX_TIMESTAMP(contract_datebegin) as datebegin,
        UNIX_TIMESTAMP(contract_dateexp) as dateexp,
        UNIX_TIMESTAMP(contract_timeupdate) as timeupdate,
        UNIX_TIMESTAMP(contract_timecreate) as timecreate
      from (Contract
            left join UserObm u1 on contract_marketmanager_id=u1.userobm_id)
            left join UserObm u2 on contract_techmanager_id=u2.userobm_id
      where contract_id='$p_contract_id'";
  }
  else if ($connect_db->type == $db_type_pgsql) {
    $query = "select *,
        u1.userobm_lastname as lname1, u1.userobm_firstname as fname1,
        u2.userobm_lastname as lname2, u2.userobm_firstname as fname2,
        contract_datebegin as datebegin,
        contract_dateexp as dateexp,
        contract_timeupdate as timeupdate,
        contract_timecreate as timecreate
      from (Contract
            left join UserObm u1 on contract_marketmanager_id=u1.userobm_id)
            left join UserObm u2 on contract_techmanager_id=u2.userobm_id
      where contract_id='$p_contract_id'";
  }
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
  $connect_db->next_record();
  return $connect_db;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query construction                                              //
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($contract) {
  global $cdg_sql;
  global $auth;

  $p_label = $contract["label"];
  $comp_id = $contract["company_id"];
  $p_num = $contract["number"];
  $ta_clause = $contract["clause"];
  $p_datebegin = $contract["datebegin"];
  $p_dateexp = $contract["dateexp"];
  $sel_type = $contract["type"];
  $ta_com = $contract["comment"];
  $sel_con1 = $contract["contact1"];
  $sel_con2 =  $contract["contact2"];
  $sel_tech  = $contract["tech"];
  $sel_market = $contract["market"];
  $deal_id = $contract["deal_id"];
  $connect_db=new DB_OBM;

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
      contract_comment)
    values ('".
      date("Y-m-d H:i:s") ."',
      '". date("Y-m-d H:i:s") ."',
      '".$auth->auth["uid"]."',
      '".$auth->auth["uid"]."',
      '$p_label',
      '$deal_id',
      '$comp_id',
      '$p_num',
      '$p_datebegin',
      '$p_dateexp',
      '$sel_type',
      '$sel_con1',
      '$sel_con2',
      '$sel_tech',
      '$sel_market',
      '$ta_clause',
      '$ta_com')";

  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
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
  $deal_id = $contract["deal_id"];
 
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
      contract_clause = '$p_clause'
    where contract_id = '$id'";

  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);  
}


///////////////////////////////////////////////////////////////////////////////
// Delete query construction                                                 //
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_contract_id) {
  global $cdg_sql;
  
  $connect_db=new DB_OBM;
  $query = "delete from Contract where contract_id=".$p_contract_id;
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Requete : Liste des TYPES de contract : Table ContractType                  //
// Query   : Leads kinds Contract          : Table ContractType                   //
///////////////////////////////////////////////////////////////////////////////
function run_query_contracttype() {

  global $cdg_sql;
  $connect_db=new DB_OBM;
  $query = "select contracttype_label,contracttype_id from ContractType";
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
  return $connect_db;
}


///////////////////////////////////////////////////////////////////////////////
// Query   : company List         : Table Company                            //
///////////////////////////////////////////////////////////////////////////////
function run_query_company() {

  global $cdg_sql;
  $connect_db=new DB_OBM;
  $query = "select company_id,company_name from Company order by company_name";
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
  return $connect_db;
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
  $connect_db=new DB_OBM;
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

  $connect_db=new DB_OBM;
  $query = "select * from Contract";
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
  return $connect_db;
}


///////////////////////////////////////////////////////////////////////////////
// Type insertion query construction                                         //
///////////////////////////////////////////////////////////////////////////////
function query_type_insert() {
  global $cdg_sql;
  global $tf_type_new,$rd_type_inout; 
  global $auth;
	
  $query = "insert into ContractType (contracttype_timeupdate,contracttype_timecreate,contracttype_userupdate,contracttype_usercreate,contracttype_label) values (null,'".date("Y-m-d H:i:s")."',null,".$auth->auth["uid"].",'". $tf_type_new ."')";
  display_debug_msg($query, $cdg_sql);
  return $query;
}


///////////////////////////////////////////////////////////////////////////////
// Type update query construction                                            //
///////////////////////////////////////////////////////////////////////////////
function query_type_update() {
  global $cdg_sql;
   global $tf_type_upd,$sel_type,$rd_type_inout;
   global $auth;  

  $query = "update ContractType set contracttype_label='". $tf_type_upd ."', contracttype_timeupdate='".date("Y-m-d H:i:s")."', contracttype_userupdate=".$auth->auth["uid"]." where contracttype_id=". $sel_type;
  display_debug_msg($query, $cdg_sql);
  return $query;
}


///////////////////////////////////////////////////////////////////////////////
// Type deletion query construction                                          //
///////////////////////////////////////////////////////////////////////////////
function query_type_delete() {
  global $sel_type; 	
  global $cdg_sql;
	
  $query="delete from ContractType where contracttype_id=$sel_type";
  display_debug_msg($query, $cdg_sql);
  return $query;
}



///////////////////////////////////////////////////////////////////////////////
// Type verif query construction                                             //
///////////////////////////////////////////////////////////////////////////////
function query_type_verif() {
  global $sel_type; 
  global $cdg_sql;

  $query = "select * from Contract,ContractType where Contract.contract_type_id=ContractType.contracttype_id and ContractType.contracttype_id=". $sel_type;
  display_debug_msg($query, $cdg_sql);
  return $query;
}



///////////////////////////////////////////////////////////////////////////////
// Data checking and formatting
///////////////////////////////////////////////////////////////////////////////
function check_data_form() {
  global  $cb_arc, $lcb_arc,$tf_datealarme,$tf_datebegin;
  global $err_msg;
  global $cdg_sql;


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

///////////////////////////////////////////////////////////////////////////////
// Select the deal linked to the contract                                    //
///////////////////////////////////////////////////////////////////////////////
function run_query_deal($deal_id) {
  global $sel_type; 
  global $cdg_sql;
  $obm_q= new DB_OBM;
  if($deal_id != 0) { 
    $query = "select * from Deal where deal_id = $deal_id";
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);
    $obm_q->next_record();
  }
  return $obm_q;
}


</SCRIPT>
