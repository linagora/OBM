<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : incident_query.inc                                           //
//     - Desc : Incident query File                                          //
// 2000-01-20 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Incident search query execution                                           //
// Parametes:
//   - $incident[]   : incident search criteria
//     keys used       status, label incident, priority, label contract
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($incident, $p_new_order, $p_order_dir) {
  global $c_all, $cdg_sql;  

  $status = $incident["status"];
  $lincident = $incident["lincident"];
  $lcontract = $incident["lcontract"];
  $priority = $incident["priority"];
  $param_contract =  $incident["contract_id"];
  $owner = $incident["owner"];
  $dateafter = $incident["date_after"];
  $datebefore = $incident["date_before"];
  $archive = ($incident["archive"] ? "1" : "0");

  $req = "select
      Incident.incident_id as Id,
      incident_id,
      incident_timeupdate,
      incident_contract_id,
      incident_label,
      incident_date,
      incident_priority_id,
      incidentpriority_label as incident_priority,
      incident_status_id,
      incidentstatus_label as incident_status,
      incident_logger,
      incident_owner,
      incident_archive,
      UNIX_TIMESTAMP(incident_date) as date,
      UNIX_TIMESTAMP(incident_timeupdate) as timeupdate,
      u1.userobm_lastname as incident_logger_lastname,
      u2.userobm_lastname as incident_owner_lastname,
      Company.company_name as incident_company_name
    from Incident, IncidentPriority, IncidentStatus, Contract,
      UserObm u1, UserObm u2, Company
    where u1.userobm_id = Incident.incident_logger
      AND u2.userobm_id = Incident.incident_owner
      AND Incident.incident_contract_id = Contract.contract_id
      AND Contract.contract_company_id = Company.company_id
      AND Incident.incident_priority_id = IncidentPriority.incidentpriority_id
      AND Incident.incident_status_id = IncidentStatus.incidentstatus_id";

  if ($lincident != "") {
     $req .= " and Incident.incident_label like '$lincident%'";
  }
  if (($priority != $c_all) && ($priority != "")) { 
     $req .= " and Incident.incident_priority_id = '$priority'";
  }
  if($param_contract != "") {
     $req .= " and Contract.contract_id = '$param_contract'";
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
     $req .= " and Incident.incident_status_id = '$status'";
  }
  if ($archive == "0") {
     $req .= " and Incident.incident_archive != '1'";
  }
  // ORDER construction

  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "incidentstatus_order";
  $req .= " order by $order $p_order_dir, incidentpriority_order";

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
  global $db_type_mysql,$db_type_pgsql;  
  global $cdg_sql;

  $connect_db = new DB_OBM;
  if ($connect_db->type == $db_type_mysql) {
    $query = "select *,
        u1.userobm_lastname as lname1, u1.userobm_firstname as fname1,
        u2.userobm_lastname as lname2, u2.userobm_firstname as fname2,
        p.incidentpriority_label,
        s.incidentstatus_label,
        UNIX_TIMESTAMP(incident_date) as date,
        UNIX_TIMESTAMP(incident_timeupdate) as timeupdate,
        UNIX_TIMESTAMP(incident_timecreate) as timecreate
      from (((Incident
              left join UserObm u1 on incident_logger=u1.userobm_id)
              left join UserObm u2 on incident_owner=u2.userobm_id)
              left join IncidentPriority p on incident_priority_id=p.incidentpriority_id)
              left join IncidentStatus s on incident_status_id=s.incidentstatus_id
      where incident_id='$id'";
  }
  else if ($connect_db->type == $db_type_pgsql) {
    $query = "select *,
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


///////////////////////////////////////////////////////////////////////////////
// Query execution: Contract info query                                      //
// Parameters:
//   - $cid : contract Id
// Returns:
//   -  DB object result : contract info
//////////////////////////////////////////////////////////////////////////////
function run_query_incident_contract($cid) {
  global $cdg_sql;

  $query = "select contract_id,
      contract_label,
      contract_company_id,
      contract_number,
      contract_datebegin,
      contract_dateexp,
      contract_type_id,
      u1.userobm_lastname as lname1, u1.userobm_firstname as fname1,
      u2.userobm_lastname as lname2, u2.userobm_firstname as fname2,
      c1.contact_lastname as lname_c1, c1.contact_firstname as fname_c1,
      c2.contact_lastname as lname_c2, c2.contact_firstname as fname_c2,
      company_name,
      company_address1,
      company_zipcode,
      company_town
    from ((((Contract
              left join UserObm u1 on contract_marketmanager_id=u1.userobm_id)
              left join UserObm u2 on contract_techmanager_id=u2.userobm_id)
              left join Contact c1 on contract_contact1_id=c1.contact_id)
              left join Contact c2 on contract_contact2_id=c2.contact_id)
              left join Company on contract_company_id=company_id
    where contract_id='$cid'
    order by contract_label";

  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Incident Insertion query execution                                        //
// Parameters:
//   - $incident[] : Entry's values
//     keys used   : lincident, logger, owner, description, solution, priority
//                 : status, date, hour, duration, archive, contract_id
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($incident) {
  global $auth, $cdg_sql;

  $lincident = $incident["lincident"];
  $owner =  $incident["owner"];
  $logger =  $incident["logger"];
  $desc =  $incident["description"];
  $solu =  $incident["solution"];
  $priority =  $incident["priority"];
  $status =  $incident["status"];
  $date =  $incident["date"];
  $hour =  $incident["hour"];
  $duration =  $incident["duration"];
  $archive = ($incident["archive"] ? "1" : "0");
  $contract_id = $incident["contract_id"];
  $call_date = "$date $hour:0:0";

  $connect_db=new DB_OBM;

  $query = "insert into Incident (incident_timeupdate,
    incident_timecreate,
    incident_userupdate,
    incident_usercreate,
    incident_label,
    incident_contract_id,
    incident_date,
    incident_priority_id,
    incident_status_id,
    incident_logger,
    incident_owner,
    incident_duration,
    incident_archive,
    incident_description,
    incident_resolution)
  values ('". date("Y-m-d H:i:s") ."',
    '". date("Y-m-d H:i:s") ."',
    '".$auth->auth["uid"]."',
    '".$auth->auth["uid"]."',
    '$lincident',
    '$contract_id',
    '$call_date',
    '$priority',
    '$status',
    '$logger',
    '$owner',
    '$duration',
    '$archive',
    '$desc',
    '$solu')";

  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Incident update query execution                                           //
// Parameters:
//   - $incident[] : Entry's values
//     keys used   : lincident, logger, owner, description, solution, priority
//                 : status, date, hour, duration, archive, contract_id
///////////////////////////////////////////////////////////////////////////////
function run_query_update($incident) {
  global $auth, $cdg_sql;

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
  $hour =  $incident["hour"];
  $duration =  $incident["duration"];
  $archive = ($incident["archive"] ? "1" : "0");
  $call_date = "$date $hour:0:0";

  $query = "update Incident set
    incident_timeupdate='". date("Y-m-d H:i:s")."',
    incident_userupdate='".$auth->auth["uid"]."',
    incident_logger='$logger',
    incident_owner='$owner',
    incident_label='$lincident',
    incident_date='$call_date', 
    incident_priority_id='$priority',
    incident_status_id='$status',
    incident_resolution = '$solu',
    incident_description = '$desc',
    incident_duration = '$duration',
    incident_archive='$archive',
    incident_contract_id = '$param_contract'
   where incident_id='$param_incident'";

  $con_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $con_q->query($query);  
}


///////////////////////////////////////////////////////////////////////////////
// Delete query construction/                                                //
// Parameters:
//   - $i_id : incident id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($i_id) {
  global $cdg_sql;

  $query = "delete from Incident where incident_id='$i_id'";

  $i_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $i_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Incident Priority list                                  //
///////////////////////////////////////////////////////////////////////////////
function run_query_priority() {
  global $cdg_sql;

  $query = "select * from IncidentPriority order by incidentpriority_order";

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

  $query = "select * from IncidentStatus order by incidentstatus_order";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Incident Form Data checking and formatting
// Parameters:
//   - $incident[] : values to check
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





