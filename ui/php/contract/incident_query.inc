<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : incident_query.inc                                           //
//     - Desc : Incident query File                                          //
// 2000-01-20 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////////
// Incident search query execution                                          //
// Parametes:
//   - $incident[]   : incident search criteria
//     keys used       status, label incident, priority, label contract
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
//////////////////////////////////////////////////////////////////////////////
function run_query_search($incident, $p_new_order, $p_order_dir) {
  global $c_all,$l_date_format, $cdg_sql;  

  $status = $incident["status"];
  $lincident = $incident["lincident"];
  $lcontract = $incident["lcontract"];
  $priority = $incident["priority"];
  $param_contract =  $incident["contract_id"];
  $owner = $incident["owner"];
  $dateafter = $incident["date_after"];
  $datebefore = $incident["date_before"];
  $archive = ($incident["archive"] ? "1" : "0");

  $req = "select Incident.*,
      Incident.incident_id as Id,
      DATE_FORMAT(Incident.incident_timeupdate,'$l_date_format') as timeupdate,
      u1.userobm_lastname as incident_logger_lastname,
      u2.userobm_lastname as incident_owner_lastname,
      Company.company_name as incident_company_name
    from Incident, Contract,
      UserObm u1, UserObm u2, Company
    where u1.userobm_id = Incident.incident_logger
      AND u2.userobm_id = Incident.incident_owner
      AND Incident.incident_contract_id = Contract.contract_id
      AND Contract.contract_company_id = Company.company_id";

  if ($lincident != "") {
     $req .= " and Incident.incident_label like '$lincident%'";
  }
  if (($priority != $c_all) && ($priority != "")) { 
     $req .= " and Incident.incident_priority ='$priority'";
  }
  if($param_contract != "") {
     $req .= " and Contract.contract_id = $param_contract";
  }
  if ($owner != $c_all && ($owner != "")) {
     $req .=" and (Incident.incident_owner = '$owner') ";
  }
  if ($dateafter !="") {
     $req .=" and (Incident.incident_date >= '$dateafter') ";
  }
  if ($datebefore !="") {
     $req .=" and (Incident.incident_date <= '$datebefore') ";
  }
  if ($lcontract != "") {
     $req .= " and Contract.contract_label like '$lcontract%'";
  }
  if (($status != $c_all) && ($status != "")) { 
     $req .= " and Incident.incident_etat ='$status'";
  }
  if ($archive == "0") {
     $req .= " and Incident.incident_archive != '1'";
  }
  // ORDER construction

  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "Incident.incident_etat";
  $req .= " order by $order $p_order_dir, Incident.incident_priority";

  $obm_db=new DB_OBM;
  $obm_db->query($req);
  display_debug_msg($req, $cdg_sql);
  
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Incident detail query execution                                           //
// Parameters:
//   - $id : incident id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($id) {
  global $l_date_format; 
  global $db_type_mysql,$db_type_pgsql;  
  global $cdg_sql;

  $connect_db = new DB_OBM;
  if ($connect_db->type == $db_type_mysql) {
    $query = "select *,
        u1.userobm_lastname as lname1, u1.userobm_firstname as fname1,
        u2.userobm_lastname as lname2, u2.userobm_firstname as fname2,
        DATE_FORMAT(incident_timeupdate,'$l_date_format') as datemodif,
        DATE_FORMAT(incident_timeupdate,'$l_date_format') as timeupdate,
        DATE_FORMAT(incident_timecreate,'$l_date_format') as timecreate
      from (Incident
              left join UserObm u1 on incident_logger=u1.userobm_id)
              left join UserObm u2 on incident_owner=u2.userobm_id
      where incident_id='$id'";
  }
  else if ($connect_db->type == $db_type_pgsql) {
    $query = "select *,
        incident_timeupdate as datemodif,
        incident_timeupdate as timeupdate,
        incident_timecreate as timecreate
      from Incident
      where incident_id='$id'";
  }

  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
  $connect_db->next_record();
  return $connect_db;
}


//////////////////////////////////////////////////////////////////////////////
// Incident: Contract select query execution                                 //
// Returns : DB object result with all contract
//////////////////////////////////////////////////////////////////////////////
function run_query_contract() {
  global $cdg_sql;
  $query = "select contract_id,contract_label from Contract order by contract_label";

  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query   : company contacts deal  :  Table Contact (company OBM)           //
///////////////////////////////////////////////////////////////////////////////
function run_query_internal_contact() {
 global $cdg_sql;

  $connect_db=new DB_OBM;
  $query = "select contact_id,contact_lastname,contact_firstname from Contact,Company where contact_company_id=company_id and company_state=1 order by contact_lastname"; 	
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
  return $connect_db;
}


//////////////////////////////////////////////////////////////////////////////
// ContractContract: Select query construction                               //
//////////////////////////////////////////////////////////////////////////////
function run_query_incident_contract($p_contract_id) {
  global $cdg_sql;

  $connect_db = new DB_OBM;
  $query = "select * from Contract where contract_id='$p_contract_id'";
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
  $connect_db->next_record();

  return $connect_db;
}


//////////////////////////////////////////////////////////////////////////////
// Insertion query construction                                             //
//////////////////////////////////////////////////////////////////////////////
function run_query_insert($incident) {

  global $auth;
 global $cdg_sql;

  $lincident = $incident["lincident"];
  $owner =  $incident["owner"];
  $logger =  $incident["logger"];
  $desc =  $incident["description"];
  $solu =  $incident["solution"];
  $priority =  $incident["priority"];
  $status =  $incident["status"];
  $date =  $incident["date"];
  $archive = $incident["archive"];
  $contract_id = $incident["contract_id"];

  $connect_db=new DB_OBM;

  $query = "insert into Incident (incident_timeupdate,incident_timecreate,incident_userupdate,incident_usercreate,
				  incident_label,incident_contract_id,incident_date,incident_description,
				  incident_resolution,incident_priority,incident_etat,incident_logger,incident_owner,incident_archive) 
			values 
				 ('". date("Y-m-d H:i:s") ."','". date("Y-m-d H:i:s") ."',".$auth->auth["uid"].",".$auth->auth["uid"].",
				  '" .$lincident. "'," .$contract_id. ",'" .$date. "','".$desc."','".$solu."','".$priority."',
				  '" .$status. "'," .$logger. "," .$owner. ",'".$archive."')";

  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
}


//////////////////////////////////////////////////////////////////////////////
// Update query construction                                                //
//////////////////////////////////////////////////////////////////////////////
function run_query_update($incident) {

  global $cdg_sql;
  $param_incident = $incident["id"];
  $param_contract = $incident["contract_id"];
  $lincident = $incident["lincident"];
  $owner =  $incident["owner"];
  $logger =  $incident["logger"];
  $desc =  $incident["description"];
  $solu =  $incident["solution"];
  $priority =  $incident["priority"];
  $status =  $incident["status"];
  $date =  $incident["date"];
  $archive = $incident["archive"];
 
  global $auth;
  $connect_db=new DB_OBM;
  

 $query = "update Incident set incident_timeupdate='". date("Y-m-d H:i:s")."',incident_userupdate=".$auth->auth["uid"].",
           incident_logger=".$logger.",incident_owner=" .$owner. ", incident_label='" .$lincident. "', incident_date='" .$date. "', 
           incident_priority='" .$priority. "', incident_etat='". $status. "',incident_resolution = '$solu',
	   incident_description = '$desc', incident_archive='". $archive. "', incident_contract_id = $param_contract
           where incident_id=$param_incident";

  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);  
}


//////////////////////////////////////////////////////////////////////////////
// Delete query construction                                                //
//////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_incident_id) {
   global $cdg_sql;

  $connect_db=new DB_OBM;
  $query = "delete from Incident where incident_id=".$p_incident_id;
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
}


//////////////////////////////////////////////////////////////////////////////
// Requete : Type du contract  : Table ContractType                  	    //
// Query   :  kind of The Contract          : Table ContractType             //
//////////////////////////////////////////////////////////////////////////////
function run_query_contracttype($type_id) {

  global $cdg_sql;
  $connect_db=new DB_OBM;
  $query = "select contracttype_label,contracttype_id from ContractType where contracttype_id=".$type_id."";
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
  return $connect_db;
}


//////////////////////////////////////////////////////////////////////////////
// Requete :  TYPES de deal du contract : Table DealType                     //
// Query   :  kind of Deal of the Contract        : Table DealType          //
//////////////////////////////////////////////////////////////////////////////
function run_query_contract_dealtype($deal_id) {
  global $cdg_sql;

  $connect_db=new DB_OBM;
  $query = "select dealtype_label,dealtype_id from DealType where dealtype_id=".$deal_id."";
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
  return $connect_db;
}


//////////////////////////////////////////////////////////////////////////////
// Query   : company contract       :  Table Company                         //
//////////////////////////////////////////////////////////////////////////////
function run_query_company_contract($p_company_id) {
  global $cdg_sql;
  
  $connect_db=new DB_OBM;
  $query = "select company_name, company_address1,company_zipcode,company_town from Company where company_id=".$p_company_id;
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
  return $connect_db;
}


//////////////////////////////////////////////////////////////////////////////
// Query   : company contacts contract  :  Table Contact                     //
//////////////////////////////////////////////////////////////////////////////
function run_query_contact_contract($p_company_id) {
  global $cdg_sql;
  
  $connect_db=new DB_OBM;
  $query = "select contact_id,contact_lastname,contact_firstname,contact_phone from Contact where contact_company_id=".$p_company_id;
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
  return $connect_db;
}

///////////////////////////////////////////////////////////////////////////////
// Deal Form Data checking and formatting
// Parameters:
//   - $did : deal id : (empty on insertion)
//   - $deal[] : values to check
// Return : true if check ok, else false
///////////////////////////////////////////////////////////////////////////////
function check_incident_form($incident) {
  global $cdg_sql;
  global  $php_regexp_isodate, $l_fill_label, $l_fill_contract, $l_incident_date;
  global $err_msg,$l_length_label,$l_invalid_date ;

  $c_id = $incident["contract_id"];
  $c_new_id = $incident["cont_new_id"];

  // Check the label is filled
  if (trim($incident["lincident"]) == "") {
    $err_msg = $l_fill_label;
    return false;
  }

  // MANDATORY: a contract must be assigned
  if (($c_id < 1) && ($c_new_id < 1)) {
    $err_msg = $l_fill_contract;
    return false;
  }

  if (strlen($incident["lincident"]) > 100) {
    $err_msg = $l_length_label;
    return false;
  }

  $date = $incident["date"];

  // Begin Date check
  if (($date == "") || (ereg($php_regexp_isodate, $date) == false)) {
    $err_msg = "$l_incident_date : $l_invalid_date"; 
    return false;
  }

  return true;
}





