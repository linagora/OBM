<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : update-0.5.2-0.5.3.php                                       //
//     - Desc : Update Database data from v. 0.5.2 to 0.5.3                  //
//       Internal Contact functionnality has been moved to userobm           //
//       * Fill userobm lastname and firstname from his associated contact   //
//       * technical and marketing manager from deals are not taken anymore  //
//         from Contact (with flag internal) but from userobm                //
//       So this script handle this update                                   //
// 2002-08-13 - Pierre Baudracco                                             //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// To use this script : $ php update-0.5.2-0.5.3.php                         //
//                   or $ php4 update-0.5.2-0.5.3.php (on debian)            //
///////////////////////////////////////////////////////////////////////////////

$mode = "txt";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc"); 
require("$obminclude/phplib/obmlib.inc");
include("$obminclude/global_pref.inc"); 

$set_debug=15;

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
// Hash : userobm[contact_id] = userobm_id : Construction
$usr_q = get_user_list();
while($usr_q->next_record()) {
  $uid = $usr_q->f("userobm_id");
  $cid = $usr_q->f("userobm_contact_id");
  // if an associated contact has been defined
  if ($cid) {
    $users["$cid"] = $uid;
    $lastname = $usr_q->f("contact_lastname");
    $firstname = $usr_q->f("contact_firstname");
    $lastnames["$uid"] = $lastname;
    $firstnames["$uid"] = $firstname;
  }
}
// When no contact defined
$users["0"] = 0;

// Displays userobm - contact association
while (list ($cid, $uid) = each ($users)) {
  echo "userobm[$cid] = $uid, ".$lastnames["$uid"]." ".$firstnames["$uid"]."\n";
}

// fill userobm names
update_userobm_names();
// Update deal managers
update_deal_managers();
// Update Parentdeal managers
update_parentdeal_managers();
// Update contract managers
update_contract_managers();
// Update contract managers
update_incident_managers();
// Update Time management contcts
update_task_contact();
// Update Eventuser
update_eventuser_contact();
// No need to update CalendarLayer : empty

///////////////////////////////////////////////////////////////////////////////
// Query execution - User list                                               //
///////////////////////////////////////////////////////////////////////////////
function get_user_list() {
  global $cdg_sql;

  $query = "select userobm_id, userobm_login, userobm_contact_id,
                   contact_lastname, contact_firstname
          from UserObm, Contact
          where userobm_contact_id = contact_id";

  display_debug_msg($query, $cdg_sql);

  $u_q = new DB_OBM;
  $u_q->query($query);
  return $u_q;
}


///////////////////////////////////////////////////////////////////////////////
// Update userobm lastnames and firstnames                                   //
///////////////////////////////////////////////////////////////////////////////
function update_userobm_names() {
  global $cdg_sql, $users, $lastnames, $firstnames;

  reset($users);
  while (list ($cid, $uid) = each ($users)) {
    $lname = $lastnames["$uid"];
    $fname = $firstnames["$uid"];

    // If correct userobm id, update names
    if ($uid > 0) {
      $query = "update UserObm
		set userobm_lastname='$lname',
		    userobm_firstname='$fname'
		where userobm_id = '$uid'";

      display_debug_msg($query, $cdg_sql);
  
      $u_q = new DB_OBM;
      $u_q->query($query);
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Deal list                                               //
///////////////////////////////////////////////////////////////////////////////
function get_deal_list() {
  global $cdg_sql;

  $query = "select deal_id, deal_marketingmanager_id, deal_technicalmanager_id
            from Deal";

  display_debug_msg($query, $cdg_sql);

  $d_q = new DB_OBM;
  $d_q->query($query);
  return $d_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - ParentDeal list                                         //
///////////////////////////////////////////////////////////////////////////////
function get_parentdeal_list() {
  global $cdg_sql;

  $query = "select parentdeal_id,
                   parentdeal_marketingmanager_id,
                   parentdeal_technicalmanager_id
            from ParentDeal";

  display_debug_msg($query, $cdg_sql);

  $d_q = new DB_OBM;
  $d_q->query($query);
  return $d_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Contract list                                           //
///////////////////////////////////////////////////////////////////////////////
function get_contract_list() {
  global $cdg_sql;

  $query = "select contract_id,
                   contract_marketmanager_id,
                   contract_techmanager_id
            from Contract";

  display_debug_msg($query, $cdg_sql);

  $d_q = new DB_OBM;
  $d_q->query($query);
  return $d_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Incident list                                           //
///////////////////////////////////////////////////////////////////////////////
function get_incident_list() {
  global $cdg_sql;

  $query = "select incident_id,
                   incident_owner,
                   incident_logger
            from Incident";

  display_debug_msg($query, $cdg_sql);

  $d_q = new DB_OBM;
  $d_q->query($query);
  return $d_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - EventUser list                                          //
///////////////////////////////////////////////////////////////////////////////
function get_eventuser_list() {
  global $cdg_sql;

  $query = "select * from EventUser";

  display_debug_msg($query, $cdg_sql);

  $d_q = new DB_OBM;
  $d_q->query($query);
  return $d_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Task list                                               //
///////////////////////////////////////////////////////////////////////////////
function get_task_list() {
  global $cdg_sql;

  $query = "select * from Task";

  display_debug_msg($query, $cdg_sql);

  $d_q = new DB_OBM;
  $d_q->query($query);
  return $d_q;
}


///////////////////////////////////////////////////////////////////////////////
// Update Deal marketing and technical managers                              //
///////////////////////////////////////////////////////////////////////////////
function update_deal_managers() {
  global $cdg_sql, $users;

  $d_q = get_deal_list();
  $nb = $d_q->num_rows();
  echo "Migrating Deal marketing and technical managers : #Deal : $nb\n";

  while($d_q->next_record()) {
    $id = $d_q->f("deal_id");
    $mm = $d_q->f("deal_marketingmanager_id");
    $tm = $d_q->f("deal_technicalmanager_id");
    $new_mm = $users["$mm"]; 
    $new_tm = $users["$tm"]; 

    $query = "update Deal
		set deal_marketingmanager_id='$new_mm',
		    deal_technicalmanager_id='$new_tm'
		where deal_id = '$id'";

    display_debug_msg($query, $cdg_sql);
  
    $d2_q = new DB_OBM;
    $d2_q->query($query);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Update ParentDeal marketing and technical managers                        //
///////////////////////////////////////////////////////////////////////////////
function update_parentdeal_managers() {
  global $cdg_sql, $users;

  $d_q = get_parentdeal_list();
  $nb = $d_q->num_rows();
  echo "Migrating ParentDeal marketing and technical managers : #ParentDeal : $nb\n";

  while($d_q->next_record()) {
    $id = $d_q->f("parentdeal_id");
    $mm = $d_q->f("parentdeal_marketingmanager_id");
    $tm = $d_q->f("parentdeal_technicalmanager_id");
    $new_mm = $users["$mm"]; 
    $new_tm = $users["$tm"]; 

    $query = "update ParentDeal
		set parentdeal_marketingmanager_id='$new_mm',
		    parentdeal_technicalmanager_id='$new_tm'
		where parentdeal_id = '$id'";

    display_debug_msg($query, $cdg_sql);
  
    $d2_q = new DB_OBM;
    $d2_q->query($query);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Update Contract marketing and technical managers                          //
///////////////////////////////////////////////////////////////////////////////
function update_contract_managers() {
  global $cdg_sql, $users;

  $d_q = get_contract_list();
  $nb = $d_q->num_rows();
  echo "Migrating Contract marketing and technical managers : #Contract : $nb\n";

  while($d_q->next_record()) {
    $id = $d_q->f("contract_id");
    $mm = $d_q->f("contract_marketmanager_id");
    $tm = $d_q->f("contract_techmanager_id");
    $new_mm = $users["$mm"]; 
    $new_tm = $users["$tm"]; 

    $query = "update Contract
		set contract_marketmanager_id='$new_mm',
		    contract_techmanager_id='$new_tm'
		where contract_id = '$id'";

    display_debug_msg($query, $cdg_sql);
  
    $d2_q = new DB_OBM;
    $d2_q->query($query);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Update Incident logger and owner                                          //
///////////////////////////////////////////////////////////////////////////////
function update_incident_managers() {
  global $cdg_sql, $users;

  $d_q = get_incident_list();
  $nb = $d_q->num_rows();
  echo "Migrating Incident logger and owner : #Incident : $nb\n";

  while($d_q->next_record()) {
    $id = $d_q->f("incident_id");
    $owner = $d_q->f("incident_owner");
    $logger = $d_q->f("incident_logger");
    $new_owner = $users["$owner"]; 
    $new_logger = $users["$logger"]; 

    $query = "update Incident
		set incident_owner='$new_owner',
		    incident_logger='$new_logger'
		where incident_id = '$id'";

    display_debug_msg($query, $cdg_sql);
  
    $d2_q = new DB_OBM;
    $d2_q->query($query);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Update EventUser Contact                                                  //
///////////////////////////////////////////////////////////////////////////////
function update_eventuser_contact() {
  global $cdg_sql, $users;

  $d_q = get_eventuser_list();
  $nb = $d_q->num_rows();
  echo "Migrating EventUser Contact : #EventUser : $nb\n";

  while($d_q->next_record()) {
    $id = $d_q->f("eventuser_event_id");
    $con = $d_q->f("eventuser_user_id");
    $new_user = $users["$con"]; 

    $query = "update EventUser
		set eventuser_user_id='$new_user'
		where eventuser_event_id = '$id'
                  and eventuser_user_id = '$con'";

    display_debug_msg($query, $cdg_sql);
  
    $d2_q = new DB_OBM;
    $d2_q->query($query);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Update task Contact                                                       //
///////////////////////////////////////////////////////////////////////////////
function update_task_contact() {
  global $cdg_sql, $users;

  $d_q = get_task_list();
  $nb = $d_q->num_rows();
  echo "Migrating Task Contact : #Task : $nb\n";

  while($d_q->next_record()) {
    $id = $d_q->f("task_id");
    $user = $d_q->f("task_user_id");
    $new_user = $users["$user"]; 

    $query = "update Task
		set task_user_id='$new_user'
		where task_id = '$id'";

    display_debug_msg($query, $cdg_sql);
  
    $d2_q = new DB_OBM;
    $d2_q->query($query);
  }
}


</SCRIPT>
