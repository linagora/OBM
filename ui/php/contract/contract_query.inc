<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : contract_query.inc                                              //
//     - Desc  : Contract Support query File                                             //
///////////////////////////////////////////////////////////////////////////////
//   $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Contract Search query 
///////////////////////////////////////////////////////////////////////////////
// Parameters :
// ------------
//    $p_new_order : if it's set, it replaces the default "order by" value
//    $p_new_order2 : the "order by" uses this value too if it's set.
//    $p_label : keyword for libelle
//    $p_type : contract'type
//    $p_company_name : society name
//    $p_dateafter :
//    $p_datebefore :
//    $p_numero :
//    $p_tech : technique responsable name
//    $p_com : commercial responsable name
//////////////////////////////////////////////////////////////////////////////

function run_query_search($contract, $p_order_dir,$p_new_order="",$p_new_order2="") {

  global $auth;
  global $set_debug;
  global $cdg_sql;

 $p_label = $contract["label"];
 $p_type = $contract["type"];
 $p_dateafter = $contract["dateafter"];
 $p_datebefore = $contract["datebefore"];
 $p_companyname = $contract["companyname"];
 $p_numero = $contract["numero"];
 $p_tech = $contract["technical"];
 $p_com = $contract["comercial"];


  $order=(strcmp($p_new_order,"") != 0) ? $p_new_order : "contract_expiration";
  
  $connect_db=new DB_OBM;
  
  $query="select contract_label,
                 contract_id,
		 contract_id as Id,
                 contract_numero,
                 contract_company_id,
                 company_name as contract_company_name,
                 contracttype_label,
                 contract_expiration
          from Contract, Company, ContractType ";

  $query .="where contract_company_id=company_id and contracttype_id=contract_type_id ";

  if (($p_type != "_TOUT_") && ($p_type != "")) $query .=" and contract_type_id = ".$p_type." ";

  if ($p_numero != "" )  $query .= " and (contract_numero like '%".$p_numero."%') ";
  
  if ($p_companyname !="") $query .=" and (company_name like '%".$p_companyname."%') ";
 
  if ($p_dateafter !="") $query .=" and (contract_expiration >= '".$p_dateafter."') ";

  if ($p_datebefore !="") $query .=" and (contract_expiration <= '".$p_datebefore."') ";
 
  if ($p_label !="") $query .=" and (contract_label like '%".$p_label."%') ";
  
  if (($p_tech !="_TOUT_") && ($p_tech != "")) $query .=" and (contract_responsable_tech_id = ".$p_tech.") ";

  if (($p_com !="_TOUT_") && ($p_com != ""))  $query .=" and (contract_responsable_com_id = ".$p_com.") ";
  
  //  $query .="where (contract_usercreate=".$auth->auth["uid"].") ";
  
  $query .= " order by  $order $p_order_dir";

  if (strcmp($p_new_order2,"") != 0) {
    $query .= ", $p_new_order2";
  }
  
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
  return($connect_db);

}


///////////////////////////////////////////////////////////////////////////////
// ContractContract: Select query construction                                           //
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_contract_id) {
  global $cdg_sql;
  global $l_date_format; 
  global $db_type_mysql,$db_type_pgsql;  

  $connect_db = new DB_OBM;
  if ($connect_db->type == $db_type_mysql) {
    $query = "select *,DATE_FORMAT(contract_timeupdate,'$l_date_format') as datemodif,DATE_FORMAT(contract_timeupdate,'$l_date_format') as timeupdate,DATE_FORMAT(contract_timecreate,'$l_date_format') as timecreate from Contract where contract_id=".$p_contract_id;
  }
  else if ($connect_db->type == $db_type_pgsql) {
    $query = "select *,contract_timeupdate as datemodif,contract_timeupdate as timeupdate,contract_timecreate as timecreate from Contract where contract_id=".$p_contract_id;
  };
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
  return $connect_db;
}



///////////////////////////////////////////////////////////////////////////////
// Insertion query construction                                              //
///////////////////////////////////////////////////////////////////////////////
//   run_query_insert($tf_label_aff,1,$tf_number_contract,$ta_clause_aff,$tf_datedebut_aff,$tf_datefin_aff,$sel_type_aff,$ta_com_aff,56,$sel_resptech_aff,87,$sel_typedeal_aff,$hd_soc_aff);

function run_query_insert($contract) {
  global $cdg_sql;

  global $auth;




  $p_label_aff = $contract["label"];
  $hd_soc_aff = $contract["company_id"];
  $p_number_contract = $contract["numero"];
  $ta_clause_aff = $contract["clause"];
  $p_datedebut_aff = $contract["datedebut"];
  $p_datefin_aff = $contract["datefin"];
  $sel_type_aff = $contract["type"];
  $ta_com_aff = $contract["commentaire"];
  $sel_respclient_aff =  $contract["clientel1"];
  $sel_respclient2_aff =  $contract["clientel2"];
  $sel_resptech_aff  = $contract["technical"];
  $sel_resp_comm_aff = $contract["comercial"];
  $sel_typedeal_aff = $contract["typedeal"];

  $connect_db=new DB_OBM;

  $query = "insert into Contract (contract_timeupdate,contract_timecreate,contract_userupdate,contract_usercreate,contract_label,contract_company_id,contract_numero,contract_clause,contract_debut,contract_expiration,contract_type_id,contract_comment,contract_responsable_client_id,contract_responsable_client2_id,contract_responsable_tech_id,contract_responsable_com_id,contract_typedeal) values ('". date("Y-m-d H:i:s") ."','". date("Y-m-d H:i:s") ."',".$auth->auth["uid"].",".$auth->auth["uid"].",'" .$p_label_aff. "'," .$hd_soc_aff. ",'" .$p_number_contract. "','".$ta_clause_aff."','".$p_datedebut_aff."','".$p_datefin_aff."'," .$sel_type_aff. ",'" .$ta_com_aff. "'," .$sel_respclient_aff. "," .$sel_respclient2_aff. "," .$sel_resptech_aff.",".$sel_resp_comm_aff.",".$sel_typedeal_aff.")";

  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
}



///////////////////////////////////////////////////////////////////////////////
// Update query construction                                                 //
///////////////////////////////////////////////////////////////////////////////
function run_query_update($contract) {

   global $cdg_sql;

  $p_number = $contract["numero"];
  $p_label = $contract["label"];
  $p_datebegin = $contract["datedebut"];
  $p_datefin = $contract["datefin"];
  $p_type = $contract["type"];
  $p_company_id = $contract["company_id"];
  $p_con1_id =  $contract["clientel1"];
  $p_con2_id =  $contract["clientel2"];
  $p_marketingmanager_id = $contract["comercial"];
  $p_technicalmanager_id  = $contract["technical"];
  $p_comment = $contract["commentaire"];
  $p_clause = $contract["clause"];
  $param_contract = $contract["id"];
 
  global $auth;
  $connect_db=new DB_OBM;
  
  if ($p_con1_id == "")
    $p_con1_id = "null";
  if ($p_con2_id == "")
    $p_con2_id = "null";
  if ($p_marketingmanager_id == "")
    $p_marketingmanager_id = "null";
  if ($p_technicalmanager_id == "")
    $p_technicalmanager_id = "null";
  

 $query = "update Contract set contract_timeupdate='". date("Y-m-d H:i:s")."',contract_userupdate=".$auth->auth["uid"].",contract_numero='".$p_number."', contract_label='" .$p_label. "', contract_debut='" .$p_datebegin. "', contract_expiration='" .$p_datefin. "', contract_type_id=" .$p_type. ", contract_company_id=" .$p_company_id. ", contract_responsable_client_id=". $p_con1_id. ",  contract_responsable_client2_id=". $p_con2_id. ",contract_responsable_com_id=" .$p_marketingmanager_id. ", contract_responsable_tech_id=" .$p_technicalmanager_id. ", contract_comment='" .$p_comment. "', contract_clause='" .$p_clause. "'  where contract_id=$param_contract";

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
// Requete : Liste des TYPES de deals : Table DealType                  //
// Query   : Leads kinds Deals          : Table DealType                   //
///////////////////////////////////////////////////////////////////////////////
function run_query_contract_dealtype() {

  global $cdg_sql;
  $connect_db=new DB_OBM;
  $query = "select dealtype_label,dealtype_id from DealType";
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
// Query   : company contract       :  Table Company                           //
///////////////////////////////////////////////////////////////////////////////
function run_query_company_contract($p_company_id) {
  
  global $cdg_sql;
  $connect_db=new DB_OBM;
  $query = "select company_name, company_address1,company_zipcode,company_town from Company where company_id=".$p_company_id;
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
  return $connect_db;
}

///////////////////////////////////////////////////////////////////////////////
// Query   : company contacts contract  :  Table Contact                        //
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_contract($p_company_id) {
  
  global $cdg_sql;
  $connect_db=new DB_OBM;
  $query = "select contact_id,contact_lastname,contact_firstname,contact_phone from Contact where contact_company_id=".$p_company_id;
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
// Query   : company contacts deal  :  Table Contact (company OBM)           //
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_company_obm() {
  
  global $cdg_sql;
  $connect_db=new DB_OBM;
  $query = "select contact_id,contact_lastname,contact_firstname from Contact,Company where contact_company_id=company_id and company_state=1 order by contact_lastname"; 
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
  global  $tf_maitre_aff,$tf_origine_aff,$cb_prop_aff,$lcb_prop_aff,$cb_arc_aff,$lcb_arc_aff,$tf_montant_aff,$tf_datealarme_aff,$tf_datedebut_aff,$tf_dateprop_aff;
  global $err_msg;
  global $cdg_sql;


  // Date alarme check
  if ($tf_datedebut_aff == "") {
    $tf_datedebut_aff = "null";
  } else {
    $tf_datedebut_aff = "'".$tf_datedebut_aff."'";
  };

  // Checkbox proposition
  if ($cb_prop_aff != "1") {
    $lcb_prop_aff = 0;
  } else {
    $lcb_prop_aff = 1;
  }

  if ($cb_arc_aff == "archives") {
    $lcb_arc_aff = 1;
  } else {
    $lcb_arc_aff = 0;
  }

  // Proposal date
  if ($tf_dateprop_aff == "") {
    $tf_dateprop_aff = "null";
  } else {
    $tf_dateprop_aff = "'" . $tf_dateprop_aff . "'";
  }
  // Amount
  if ($tf_montant_aff == "") {
    $tf_montant_aff = "null";
  } else {
    $tf_montant_aff = "'" . $tf_montant_aff . "'";
  }

  // Date alarme check
  if ($tf_datealarme_aff == "") {
    $tf_datealarme_aff = "null";
  } else {
    $tf_datealarme_aff = "'".$tf_datealarme_aff."'";
  };

  return true;
}


</SCRIPT>
