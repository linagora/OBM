<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : incident_index.php                                           //
//     - Desc : Incident Index File                                          //
// 2002-03-14 : Mehdi Rande                                                  //
///////////////////////////////////////////////////////////////////////////////
//  $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the Incident search form
// - search          -- search fields  -- show the result set of search
// - new             -- $param_contract -- show the new Incidentform
// - detailconsult   -- $param_incident-- show the Incident detail
// - detailupdate    -- $param_incident-- show the Incident detail form
// - insert          -- form fields    -- insert the Incident 
// - update          -- form fields    -- update the Incident
// - delete          -- $param_incident-- delete the Incident
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Session,Auth,Perms Management                                             //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "PROD";
$menu="INCIDENT";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
require("$obminclude/phplib/obmlib.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
$perm->check("user");

include("$obminclude/global_pref.inc");

require("incident_query.inc");
require("incident_display.inc");

$uid = $auth->auth["uid"];

// Updating the "last incident" bookmark 
if ( ($param_incident == $last_incident) && (strcmp($action,"delete")==0) ) {
  $last_incident=$last_incident_default;
} elseif  ( ($param_incident > 0) && ($last_incident != $param_incident) ) {
    $last_incident=$param_incident;
    run_query_set_user_pref($auth->auth["uid"],"last_incident",$param_incident);
    $last_incident_name = run_query_global_incident_label($last_incident);
    //$sess->register("last_incident");
}

page_close();

if($action == "") $action = "index";
$incident=get_param_incident();
get_incident_action();
$perm->check();
///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
display_head($l_incident);     // Head & Body
generate_menu($menu,$section);      // Menu
display_bookmarks();


///////////////////////////////////////////////////////////////////////////////
// Programe principal                                                        //
///////////////////////////////////////////////////////////////////////////////

if ($action == "index" || $action == "") {
//////////////////////h////////////////////////////////////////////////////////
  require("incident_js.inc");
  html_incident_search_form(run_query_userobm(),$incident);
  if ($set_display == "yes") {
    dis_incident_search_list($incident);
  } else {
    display_info_msg($l_no_display);
  }

} elseif ($action == "search")  {
///////////////////////////////////////////////////////////////////////////////
  require("incident_js.inc");
  html_incident_search_form(run_query_userobm(),$incident);
  dis_incident_search_list($incident);

} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  require("incident_js.inc");
  html_incident_form($action, "", "", run_query_userobm(), $incident);

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_incident > 0) {
    $inc_q = run_query_detail($param_incident);
    if ($inc_q->num_rows() == 1) {
      $con_q = run_query_incident_contract($inc_q->f("incident_contract_id"));
    } else {
      display_err_msg($l_query_error . " - " . $inc_q->query . " !");
    }
    display_record_info($inc_q->f("incident_usercreate"),$inc_q->f("incident_userupdate"),$inc_q->f("timecreate"),$inc_q->f("timeupdate"));
    $comp_id = $con_q->f("contract_company_id");
    html_incident_consult($inc_q, $con_q, run_query_company_contract($comp_id), run_query_contact_contract($comp_id));
  }

} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_incident > 0) {
    $inc_q = run_query_detail($param_incident);
    if ($inc_q->num_rows() == 1) {
      $contr_q = run_query_incident_contract($inc_q->f("incident_contract_id"));
      require("incident_js.inc");
      display_record_info($inc_q->f("incident_usercreate"),$inc_q->f("incident_userupdate"),$inc_q->f("timecreate"),$inc_q->f("timeupdate")); 
      html_incident_form($action, $inc_q, $contr_q, run_query_userobm(), $incident);
    } else {
      display_err_msg($l_query_error . " - " . $con_q->query . " !");
      html_incident_search_form(run_query_userobm(),$incident);
    }
  }

} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_incident_form($incident)) {
    run_query_insert($incident);
    display_ok_msg($l_insert_ok);
    require("incident_js.inc");
    html_incident_search_form(run_query_userobm(),$incident);
  } else {
    require("incident_js.inc");
    display_warn_msg($err_msg);
    html_incident_form($action, "", run_query_userobm(), $incident);
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
 if (check_incident_form($incident)) {
    run_query_update($incident);         
    display_ok_msg($l_update_ok);
    require("incident_js.inc");
    html_incident_search_form(run_query_userobm(),$incident);
 } else {
    require("incident_js.inc");
    display_warn_msg($err_msg);
    html_incident_search_form(run_query_userobm(),$incident);
 }
 
} elseif ($action == "delete")  {
///OK//////////////////////////////////////////////////////////////////////////
 
  run_query_delete($param_incident);
  display_ok_msg($l_delete_ok);
  require("incident_js.inc");
  html_incident_search_form(run_query_userobm(),$incident);
  
}  elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $pref_q = run_query_display_pref($uid, "incident", 1);
  dis_incident_display_pref($pref_q); 

} else if($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update($entity, $fieldname, $display);
  $pref_q = run_query_display_pref($uid, "incident", 1);
  dis_incident_display_pref($pref_q);

} else if($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update($entity, $new_level, $fieldorder);
  $pref_q = run_query_display_pref($uid, "incident", 1);
  dis_incident_display_pref($pref_q);
}


///////////////////////////////////////////////////////////////////////////////
// Display end of page                                                       //
///////////////////////////////////////////////////////////////////////////////
display_end();


///////////////////////////////////////////////////////////////////////////////
// Stores Contact parameters transmited in $incident hash
// returns : $incident hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_incident() {
   global $tf_lcontract, $tf_lincident, $sel_status, $sel_priority, $sel_logger, $sel_owner, $cb_archive;
   global $tf_date, $ta_desc, $ta_solu,$param_contract,$param_incident,$tf_dateafter,$tf_datebefore, $contract_new_id;
   global $set_debug, $cdg_param;

  if (isset ($tf_dateafter)) $incident["date_after"] = $tf_dateafter;
  if (isset ($tf_datebefore)) $incident["date_before"] = $tf_datebefore;
  if (isset ($param_incident)) $incident["id"] = $param_incident;
  if (isset ($tf_lcontract)) $incident["lcontract"] = $tf_lcontract;
  if (isset ($tf_lincident)) $incident["lincident"] = $tf_lincident;
  if (isset ($sel_priority)) $incident["priority"] = $sel_priority;
  if (isset ($sel_status)) $incident["status"] = $sel_status;
  if (isset ($sel_owner)) $incident["owner"] = $sel_owner;
  if (isset ($sel_logger)) $incident["logger"] = $sel_logger;
  if (isset ($tf_date)) $incident["date"] = $tf_date;
  if (isset ($ta_desc)) $incident["description"] = $ta_desc;
  if (isset ($ta_solu)) $incident["solution"] = $ta_solu;
  $incident["archive"] = ( ($cb_archive == '1') ? '1' : '0');
  if (isset ($param_contract)) $incident["contract_id"] = $param_contract;
  if (isset ($contract_new_id)) $incident["cont_new_id"] = $contract_new_id;

  if (($set_debug > 0) && (($set_debug & $cdg_param) == $cdg_param)) {
    if ( $incident ) {
      while ( list( $key, $val ) = each( $incident ) ) {
        echo "<BR>incident[$key]=$val";
      }
    }
  }

  return $incident;
}


///////////////////////////////////////////////////////////////////////////////
// Incident actions
///////////////////////////////////////////////////////////////////////////////
function get_incident_action() {
  global $incident, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_modify,$l_header_delete;
  global $l_header_display;
  global $incident_read, $incident_write, $incident_admin_write, $incident_admin_read;

//  Index
  $actions["INCIDENT"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/contract/incident_index.php?action=index",
    'Right'    => $incident_read,
    'Condition'=> array ('all') 
                                	);

//  Search
  $actions["INCIDENT"]["search"] = array (
    'Url'      => "$path/contract/incident_index.php?action=search",
    'Right'    => $incident_read,
    'Condition'=> array ('None') 
                                	);

// New
  $actions["INCIDENT"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/contract/incident_index.php?action=new",
    'Right'    => $incident_write,
    'Condition'=> array ('','search','index','detailconsult','display') 
                    		       );

// Detail Consult
  $actions["INCIDENT"]["detailconsult"] = array (
    'Url'      => "$path/contract/incident_index.php?action=consult",
    'Right'    => $incident_read,
    'Condition'=> array ('None') 
                                	       );

// Detail Update
  $actions["INCIDENT"]["detailupdate"] = array (
    'Name'     => $l_header_modify,
    'Url'      => "$path/contract/incident_index.php?action=detailupdate&amp;param_incident=".$incident["id"]."",
    'Right'    => $incident_write,
    'Condition'=> array ('detailconsult') 
                                     	        );

// Insert
  $actions["INCIDENT"]["insert"] = array (
    'Url'      => "$path/contract/incident_index.php?action=insert",
    'Right'    => $incident_write,
    'Condition'=> array ('None') 
                                     	 );

// Update
  $actions["INCIDENT"]["update"] = array (
    'Url'      => "$path/contract/incident_index.php?action=update",
    'Right'    => $incident_write,
    'Condition'=> array ('None') 
                                     	 );

// Delete
  $actions["INCIDENT"]["delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/contract/incident_index.php?action=delete&amp;param_incident=".$incident["id"]."",
    'Right'    => $incident_write,
    'Condition'=> array ('detailconsult') 
                                     	 );
// Display
  $actions["INCIDENT"]["display"] = array (
     'Name'     => $l_header_display,
     'Url'      => "$path/contract/incident_index.php?action=display",
     'Right'    => $incident_read,
     'Condition'=> array ('all') 
                                      	   );

// Display Préférence
  $actions["INCIDENT"]["dispref_display"] = array (
     'Url'      => "$path/contract/incident_index.php?action=dispref_display",
     'Right'    => $incident_read,
     'Condition'=> array ('None') 
                                      	   );

// Display level
  $actions["INCIDENT"]["dispref_level"] = array (
     'Url'      => "$path/contract/incident_index.php?action=dispref_level",
     'Right'    => $incident_read,
     'Condition'=> array ('None') 
                                      	   );

}

</SCRIPT>
