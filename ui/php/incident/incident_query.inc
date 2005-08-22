<script language="php">
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
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($incident, $p_new_order, $p_order_dir) {
  global $c_all, $cdg_sql, $ctu_sql_limit;

  $status = $incident["status"];
  $lincident = sql_search_text_parse($incident["lincident"]);
  $lcontract = sql_search_text_parse($incident["lcontract"]);
  $company = sql_search_text_parse($incident["company"]);
  $priority = $incident["priority"];
  $contract_id =  $incident["contract_id"];
  $owner = $incident["owner"];
  $dateafter = $incident["date_after"];
  $datebefore = $incident["date_before"];
  $archive = $incident["archive"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $date = sql_date_format($db_type, "incident_date", "date");
  $timeupdate = sql_date_format($db_type, "incident_timeupdate", "timeupdate");

  $where = "1=1";
  if ($lincident != "") {
     $where .= " AND Incident.incident_label $like '$lincident%'";
  }
  if (($priority != $c_all) && ($priority != "")) { 
     $where .= " AND Incident.incident_priority_id = '$priority'";
  }
  if ($owner != $c_all && ($owner != "")) {
     $where .=" AND (Incident.incident_owner = '$owner') ";
  }
  if ($dateafter !="") {
     $where .=" AND (Incident.incident_date >= '$dateafter') ";
  }
  if ($datebefore !="") {
     $where .=" AND (Incident.incident_date <= '$datebefore') ";
  }
  if ($contract_id != "") {
     $where .= " AND incident_contract_id = '$contract_id'";
  }
  if ($lcontract != "") {
     $where .= " AND contract_label $like '$lcontract%'";
     $join_contr = "LEFT JOIN Contract ON incident_contract_id=contract_id";
  }
  if ($company != "") {
     $where .= " AND company_name $like '$company%'";
     $join_contr = "LEFT JOIN Contract ON incident_contract_id=contract_id";
     $join_comp = "LEFT JOIN Company ON contract_company_id=company_id";
  }
  if (($status != $c_all) && ($status != "")) { 
     $where .= " AND Incident.incident_status_id = '$status'";
  }
  if ($archive == "0") {
     $where .= " AND Incident.incident_archive != '1'";
  }
  $whereq = "WHERE $where";

  // ORDER construction
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "incidentstatus_order";
  $orderq .= " ORDER BY $order $p_order_dir, incidentpriority_order";

  $req = "SELECT
      Incident.incident_id as id,
      incident_id,
      incident_timeupdate,
      incident_contract_id,
      incident_label,
      incident_date,
      incident_priority_id,
      incidentpriority_label as incident_priority,
      incidentpriority_color,
      incident_status_id,
      incidentstatus_label as incident_status,
      incident_logger,
      incident_owner,
      incident_archive,
      incident_duration,
      $date,
      $timeupdate,
      u1.userobm_lastname as incident_logger_lastname,
      u2.userobm_lastname as incident_owner_lastname,
      Company.company_name as incident_company_name
    FROM Incident
         LEFT JOIN Contract ON incident_contract_id=contract_id
         LEFT JOIN Company ON contract_company_id=company_id
         LEFT JOIN UserObm as u1 ON incident_logger=u1.userobm_id
         LEFT JOIN UserObm as u2 ON incident_owner=u2.userobm_id
         LEFT JOIN IncidentPriority ON incident_priority_id=incidentpriority_id
         LEFT JOIN IncidentStatus ON incident_status_id=incidentstatus_id
    $whereq
    $orderq
    $limit
";

  if ($ctu_sql_limit) {
    $cq = "SELECT count(*) FROM Incident $join_contr $join_comp $whereq";
    $count = get_query_count($cq);
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg($req, $cdg_sql);
    $obm_q->query($req);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Incident detail query execution
// Parameters:
//   - $id : incident id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $date = sql_date_format($db_type, "incident_date", "date");
  $timeupdate = sql_date_format($db_type, "incident_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "incident_timecreate", "timecreate");
  $datebegin = sql_date_format($db_type, "contract_datebegin", "datebegin");
  $dateexp = sql_date_format($db_type, "contract_dateexp", "dateexp");

  $query = "SELECT *,
        u1.userobm_lastname as lname1, u1.userobm_firstname as fname1,
        u2.userobm_lastname as lname2, u2.userobm_firstname as fname2,
        incidentpriority_label,
        incidentstatus_label,
        incidentcategory1_label,
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
        c1.contact_phone as phone_c1, c2.contact_phone as phone_c2,
        c2.contact_lastname as lname_c2, c2.contact_firstname as fname_c2
      FROM Incident
        LEFT JOIN Contract on incident_contract_id=contract_id
        LEFT JOIN ContractStatus on contract_status_id=contractstatus_id
        LEFT JOIN Company on contract_company_id=company_id
        LEFT JOIN UserObm u1 on incident_logger=u1.userobm_id
        LEFT JOIN UserObm u2 on incident_owner=u2.userobm_id
        LEFT JOIN IncidentPriority on incident_priority_id=incidentpriority_id
        LEFT JOIN IncidentStatus on incident_status_id=incidentstatus_id
        LEFT JOIN IncidentCategory1 on incident_cat1_id=incidentcategory1_id
        LEFT JOIN UserObm as c on incident_usercreate=c.userobm_id
        LEFT JOIN UserObm as u on incident_userupdate=u.userobm_id
        LEFT JOIN Contact c1 on contract_contact1_id=c1.contact_id
        LEFT JOIN Contact c2 on contract_contact2_id=c2.contact_id
      WHERE incident_id='$id'";
 
  display_debug_msg($query, $cdg_sql);
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
function run_query_insert($incident) {
  global $auth, $cdg_sql, $cgp_host;
  global $cmail_incident, $l_mail_incident_new_subject, $l_status, $l_contract;

  $uid = $auth->auth["uid"];
  $now = date("Y-m-d H:i:s");

  $contract_id = $incident["cont_new_id"];
  if ($contract_id < 1) {
    $contract_id = $incident["contract_id"];
  }

  $lincident = $incident["lincident"];
  $owner = ($incident["owner"] ? "'".$incident["owner"]."'" : "null");
  $logger = ($incident["logger"] ? "'".$incident["logger"]."'" : "null");
  $archive = ($incident["archive"] ? $incident["archive"] : "0");
  $solu = $incident["solution"];
  $priority = $incident["priority"];
  $status = $incident["status"];
  $cat1 = $incident["cat1"];
  $date = $incident["date"];
  $hour = $incident["hour"];
  $add_duration = $incident["add_duration"];
  $archive = ($incident["archive"] ? $incident["archive"] : "0");
  $call_date = "$date $hour:0:0";
  $add_comment = $incident["add_comment"];
  if ($add_comment != "") {
    $datecomment = $incident["datecomment"];
    $usercomment = $incident["usercomment"];
    $comment = "$datecomment:$usercomment:$add_comment";
  }

  $query = "INSERT INTO Incident (incident_timeupdate,
    incident_timecreate,
    incident_userupdate,
    incident_usercreate,
    incident_label,
    incident_contract_id,
    incident_date,
    incident_priority_id,
    incident_status_id,
    incident_cat1_id,
    incident_comment,
    incident_logger,
    incident_owner,
    incident_duration,
    incident_archive,
    incident_resolution)
  VALUES (null,
    '$now',
    null,
    '$uid',
    '$lincident',
    '$contract_id',
    '$call_date',
    '$priority',
    '$status',
    '$cat1',
    '$comment',
    $logger,
    $owner,
    '$add_duration',
    '$archive',
    '$solu')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  $i_id = run_query_incident_id($incident);

  if ($cmail_incident != "") {
    $inc = get_incident_info($i_id);
    $company_name = $inc["company"];
    $status = $inc["status"];
    $contract = $inc["contract"];
    $label_mail = stripslashes($lincident);
    $subject = "$l_mail_incident_new_subject : $company_name : $label_mail";
    $message = "$cgp_host/incident/incident_index.php?action=detailconsult&param_incident=$i_id

$l_status: $status
$l_contract: $contract
$comment";
    send_mail($subject, $message, array($logger, $owner), array("$cmail_incident"), 1);
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

  $contract_id = $incident["cont_new_id"];
  if ($contract_id < 1) {
    $contract_id = $incident["contract_id"];
  }
  $lincident = $incident["lincident"];
  $owner = ($incident["owner"] ? "='".$incident["owner"]."'" : " is null");
  $logger = ($incident["logger"] ? "='".$incident["logger"]."'" : " is null");
  $date = ($incident["date"] ? "='".$incident["date"]."'" : "is null");
  $pri = ($incident["priority"] ? "='".$incident["priority"]."'" : " is null");
  $status = ($incident["status"] ? "='".$incident["status"]."'" : " is null");
  $hour = $incident["hour"];
  $date = $incident["date"];
  $call_date = "$date $hour:0:0";
  $archive = ($incident["archive"] ? $incident["archive"] : "0");

  $query = "SELECT incident_id
    FROM Incident
    WHERE incident_contract_id='$contract_id'
      AND incident_label = '$lincident'
      AND incident_owner $owner
      AND incident_logger $logger
      AND incident_date='$call_date'
      AND incident_priority_id $pri
      AND incident_status_id $status
      AND incident_archive='$archive'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    $obm_q->next_record();
    $id = $obm_q->f("incident_id");
  } else {
    $id = false;
  }

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Incident update query execution
// Parameters:
//   - $incident[] : Entry's values
//     keys used   : lincident, logger, owner,  solution, priority
//                 : status, date, hour, duration, archive, contract_id
///////////////////////////////////////////////////////////////////////////////
function run_query_update($incident) {
  global $auth, $cdg_sql;

  $contract_id = $incident["cont_new_id"];
  if ($contract_id < 1) {
    $contract_id = $incident["contract_id"];
  }
  $param_incident = $incident["id"];
  $lincident = $incident["lincident"];
  $owner = ($incident["owner"] ? "='".$incident["owner"]."'" : "is null");
  $logger = ($incident["logger"] ? "='".$incident["logger"]."'" : "is null");
  $solu = $incident["solution"];
  $priority = $incident["priority"];
  $status = $incident["status"];
  $cat1 = $incident["cat1"];
  $date = $incident["date"];
  $hour = $incident["hour"];
  $duration = $incident["duration"];
  $archive = $incident["archive"];
  $call_date = "$date $hour:0:0";
  $comment = $incident["com"];
  $add_comment = $incident["add_comment"];
  if ($add_comment != "") {
    $datecomment = $incident["datecomment"];
    $usercomment = $incident["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
    $add_duration = $incident["add_duration"];
    $duration = $duration + $add_duration;
  }
  $query = "UPDATE Incident SET
      incident_timeupdate='". date("Y-m-d H:i:s")."',
      incident_userupdate='".$auth->auth["uid"]."',
      incident_logger $logger,
      incident_owner $owner,
      incident_label='$lincident',
      incident_date='$call_date', 
      incident_priority_id='$priority',
      incident_status_id='$status',
      incident_cat1_id='$cat1',
      incident_resolution = '$solu',
      incident_duration = '$duration',
      incident_comment='$comment',
      incident_archive='$archive',
      incident_contract_id = '$contract_id'
    WHERE incident_id='$param_incident'";

  $inc_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $ret = $inc_q->query($query);  

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Delete query construction
// Parameters:
//   - $i_id : incident id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($i_id) {
  global $auth, $cdg_sql;
 
  $query = "DELETE FROM Incident WHERE incident_id='$i_id'";

  $i_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $i_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Incident Priority list
///////////////////////////////////////////////////////////////////////////////
function run_query_priority() {
  global $cdg_sql;

  $query = "SELECT * FROM IncidentPriority ORDER BY incidentpriority_order";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
 
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Incident Status list
///////////////////////////////////////////////////////////////////////////////
function run_query_status() {
  global $cdg_sql;

  $query = "SELECT * FROM IncidentStatus ORDER BY incidentstatus_order";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Incident Status list
///////////////////////////////////////////////////////////////////////////////
function run_query_cat1() {
  global $cdg_sql;

  $query = "SELECT * FROM IncidentCategory1 ORDER BY incidentcategory1_order";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Priority insert
// Parameters:
//   - $incdident[] : incident hash info : keys used : pri_label, pri_order
///////////////////////////////////////////////////////////////////////////////
function run_query_priority_insert($incident) {
  global $auth, $cdg_sql;
	
  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $order = $incident["pri_order"];
  $color = $incident["pri_color"];
  $label = $incident["pri_label"];

  $query = "INSERT INTO IncidentPriority (
    incidentpriority_timecreate,
    incidentpriority_usercreate,
    incidentpriority_order,
    incidentpriority_color,
    incidentpriority_label)
  VALUES (
    '$timecreate',
    '$usercreate',
    '$order',
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
//   - $incident[] : incident hash info :
//     keys used : priority, pri_label, pri_order
///////////////////////////////////////////////////////////////////////////////
function run_query_priority_update($incident) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $priority = $incident["priority"];
  $order = $incident["pri_order"];
  $color = $incident["pri_color"];
  $label = $incident["pri_label"];

  $query = "UPDATE IncidentPriority SET
    incidentpriority_label='$label',
    incidentpriority_timeupdate='$timeupdate',
    incidentpriority_userupdate='$userupdate',
    incidentpriority_order='$order',
    incidentpriority_color='$color'
  WHERE incidentpriority_id='$priority'";

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
function run_query_priority_delete($id) {
  global $cdg_sql;

  $query = "DELETE FROM IncidentPriority WHERE incidentpriority_id='$id'";

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
function run_query_priority_links($p_id) {
  global $cdg_sql;

  $query = "SELECT incident_id, incident_label
    FROM Incident
    WHERE incident_priority_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $pri_q = new DB_OBM;
  $pri_q->query($query);

  return $pri_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Status insert
// Parameters:
//   - $incdident[] : incident hash info : keys used : sta_label, sta_order
///////////////////////////////////////////////////////////////////////////////
function run_query_status_insert($incident) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $label = $incident["sta_label"];
  $order = $incident["sta_order"];

  $query = "INSERT INTO IncidentStatus (
    incidentstatus_timecreate,
    incidentstatus_usercreate,
    incidentstatus_label,
    incidentstatus_order)
  VALUES (
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
// Query execution : Status update
// Parameters:
//   - $incident[] : incident hash info :
//     keys used : status, sta_label, sta_order
///////////////////////////////////////////////////////////////////////////////
function run_query_status_update($incident) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $status = $incident["status"];
  $label = $incident["sta_label"];
  $order = $incident["sta_order"];

  $query = "UPDATE IncidentStatus SET
    incidentstatus_label='$label',
    incidentstatus_timeupdate='$timeupdate',
    incidentstatus_userupdate='$userupdate',
    incidentstatus_order='$order'
  WHERE incidentstatus_id='$status'";

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
function run_query_status_delete($id) {
  global $cdg_sql;

  $query = "DELETE FROM IncidentStatus WHERE incidentstatus_id='$id'";

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
function run_query_status_links($s_id) {
  global $cdg_sql;

  $query = "SELECT incident_id, incident_label
    FROM Incident
    WHERE incident_status_id='$s_id'";

  display_debug_msg($query, $cdg_sql);
  $sta_q = new DB_OBM;
  $sta_q->query($query);

  return $sta_q;
}


//////////////////////////////////////////////////////////////////////////////
// Query execution : Category1 insert
// Parameters:
//   - $incident[] : incident hash info : keys used : cat1_label, cat1_order
///////////////////////////////////////////////////////////////////////////////
function run_query_cat1_insert($incident) {
  global $auth, $cdg_sql;

  $timecreate = date("Y-m-d H:i:s");
  $usercreate = $auth->auth["uid"];
  $label = $incident["cat1_label"];
  $order = $incident["cat1_order"];

  $query = "INSERT INTO IncidentCategory1 (
    incidentcategory1_timecreate,
    incidentcategory1_usercreate,
    incidentcategory1_label,
    incidentcategory1_order)
  VALUES (
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
// Query execution : Category1 update
// Parameters:
//   - $incident[] : incident hash info :
//     keys used : cat1, cat1_label, cat1_order
///////////////////////////////////////////////////////////////////////////////
function run_query_cat1_update($incident) {
  global $auth, $cdg_sql;

  $timeupdate = date("Y-m-d H:i:s");
  $userupdate = $auth->auth["uid"];
  $cat1 = $incident["cat1"];
  $label = $incident["cat1_label"];
  $order = $incident["cat1_order"];

  $query = "UPDATE IncidentCategory1 SET
    incidentcategory1_label='$label',
    incidentcategory1_timeupdate='$timeupdate',
    incidentcategory1_userupdate='$userupdate',
    incidentcategory1_order='$order'
  WHERE incidentcategory1_id='$cat1'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Category1 deletion
// Parameters:
//   - $id : Category1 id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_cat1_delete($id) {
  global $cdg_sql;

  $query = "DELETE FROM IncidentCategory1 WHERE incidentcategory1_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $sta_q = new DB_OBM;
  $retour = $sta_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Links to the Category1 given
// Parameters:
//   - $c_id : cat1 id
///////////////////////////////////////////////////////////////////////////////
function run_query_cat1_links($c_id) {
  global $cdg_sql;

  $query = "SELECT incident_id, incident_label
    FROM Incident
    WHERE incident_cat1_id='$c_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get Incident infos
// Parameters:
//   - $id : incident id
///////////////////////////////////////////////////////////////////////////////
function get_incident_info($id) {
  global $cdg_sql;

  $query = "SELECT
      incident_id,
      incident_label,
      incidentstatus_label,
      contract_label,
      company_name
    FROM Incident
      LEFT JOIN IncidentStatus on incident_status_id=incidentstatus_id
      LEFT JOIN Contract on incident_contract_id=contract_id
      LEFT JOIN Company on contract_company_id=company_id
    WHERE incident_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $res["id"] = $obm_q->f("incident_id");
  $res["label"] = $obm_q->f("incident_label");
  $res["status"] = $obm_q->f("incidentstatus_label");
  $res["contract"] = $obm_q->f("contract_label");
  $res["company"] = $obm_q->f("company_name");

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Get a priority label
// Parameters:
//   - $id : priority id
///////////////////////////////////////////////////////////////////////////////
function get_priority_label($id) {
  global $cdg_sql;

  $query = "SELECT incidentpriority_label
    FROM IncidentPriority
    WHERE incidentpriority_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("incidentpriority_label");

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get a status label
// Parameters:
//   - $id : status id
///////////////////////////////////////////////////////////////////////////////
function get_status_label($id) {
  global $cdg_sql;

  $query = "SELECT incidentstatus_label
    FROM IncidentStatus
    WHERE incidentstatus_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("incidentstatus_label");

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get a Category1 label
// Parameters:
//   - $id : cat1 id
///////////////////////////////////////////////////////////////////////////////
function get_cat1_label($id) {
  global $cdg_sql;

  $query = "SELECT incidentcategory1_label
    FROM IncidentCategory1
    WHERE incidentcategory1_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f("incidentcategory1_label");

  return $retour;
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
  global $err_msg,$l_length_label,$l_invalid_date;

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
  if (($date == "") || (preg_match($php_regexp_isodate, $date) == 0)) {
    $err_msg = "$l_incident_date : $l_invalid_date"; 
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
  global $cdg_sql, $err_msg, $ok_msg;

  $delete_ok = true;

  return $delete_ok;
}
