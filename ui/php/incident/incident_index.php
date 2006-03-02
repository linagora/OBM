<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : incident_index.php                                           //
//     - Desc : Incident Index File                                          //
// 2002-03-14 : Mehdi Rande                                                  //
///////////////////////////////////////////////////////////////////////////////
//  $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default)     -- search fields  -- show the Incident search form
// - search              -- search fields  -- show the result set of search
// - new                 -- $param_contract-- show the new Incident form
// - detailconsult       -- $param_incident-- show the Incident detail
// - detailupdate        -- $param_incident-- show the Incident detail form
// - insert              -- form fields    -- insert the Incident 
// - update              -- form fields    -- update the Incident
// - delete              -- $param_incident-- delete the Incident
// - priority_insert     -- form fields    -- insert the category
// - priority_update     -- form fields    -- update the category
// - priority_checklink  --                -- check if category is used
// - priority_delete     -- $sel_cat1      -- delete the category
// - status_insert       -- form fields    -- insert the category
// - status_update       -- form fields    -- update the category
// - status_checklink    --                -- check if category is used
// - status_delete       -- $sel_cat1      -- delete the category
// - category1_insert    -- form fields    -- insert the category
// - category1_update    -- form fields    -- update the category
// - category1_checklink --                -- check if category is used
// - category1_delete    -- $sel_cat1      -- delete the category
// - display             --                -- display, set display parameters
// - dispref_display     --                -- update one field display value
// - dispref_level       --                -- update one field display position
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "incident";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("incident_query.inc");
require("incident_display.inc");
require_once("$obminclude/of/of_category.inc");

$uid = $auth->auth["uid"];

update_last_visit("incident", $param_incident, $action);

if ($action == "") $action = "index";
$incident = get_param_incident();
get_incident_action();
$perm->check_permissions($module, $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main Program
///////////////////////////////////////////////////////////////////////////////

if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  require("incident_js.inc");
  $display["search"] = dis_incident_search_form($incident);
  if ($set_display == "yes") {
    $display["result"] = dis_incident_search_list($incident);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "search")  {
///////////////////////////////////////////////////////////////////////////////
  require("incident_js.inc");
  $display["search"] = dis_incident_search_form($incident);
  $display["result"] = dis_incident_search_list($incident);

} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  require("incident_js.inc");
  $display["detail"] = dis_incident_form($action,$incident);

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  if ($incident["id"] > 0) {
    $display["detail"] = dis_incident_consult($incident);
  } else {
    $display["msg"] .= display_err_msg($l_error_visibility);
  }

} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  if ($incident["id"] > 0) {
    require("incident_js.inc");
    $display["detailInfo"] = display_record_info($inc_q);
    $display["detail"] = dis_incident_form($action,$incident);
  } else {
    $display["msg"] = display_err_msg($l_query_error . " - " . $con_q->query . " !");
    $display["search"] = dis_incident_search_form($incident);
  }
  
} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_incident_form($incident)) {
    $incident["id"] = run_query_incident_insert($incident);
    if ($incident["id"] > 0) {
      $display["msg"] = display_ok_msg($l_insert_ok);
      $display["detail"] = dis_incident_consult($incident);
    } else {
      $display["msg"] = display_err_msg($l_insert_error);
      require("incident_js.inc");
      $display["detail"] = dis_incident_form($action,$incident);
    }
  } else {
    require("incident_js.inc");
    $display["msg"] = display_warn_msg($err_msg);
    $display["detail"] = dis_incident_form($action,$incident);
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_incident_form($incident)) {
    $ret = run_query_incident_update($incident);
    if ($ret) {
      $display["msg"] = display_ok_msg($l_update_ok);
      $display["detail"] = dis_incident_consult($incident);
    } else {
      $display["msg"] = display_error_msg($l_update_error);
      $display["detail"] = dis_incident_form($action,$incident);
    }
  } else {
    require("incident_js.inc");
    $display["msg"] = display_warn_msg($err_msg);
    $display["detail"] = dis_incident_form($action,$incident);
  }
 
} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_incident($incident["id"])) {
    require("incident_js.inc");
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_can_delete_incident($incident["id"]);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_incident_consult($incident);
  }

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_incident($incident["id"])) {
    $retour = run_query_incident_delete($incident["id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_delete_ok);
    } else {
      $display["msg"] .= display_err_msg($l_delete_error);
    }
    require("incident_js.inc");
    $display["search"] = dis_incident_search_form($incident);
    if ($set_display == "yes") {
      $display["result"] = dis_incident_search_list($incident);
    } else {
      $display["msg"] .= display_info_msg($l_no_display);
    }
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_incident_consult($incident);
  }

} elseif ($action == "admin")  {
///////////////////////////////////////////////////////////////////////////////
    require("incident_js.inc");
    $display["detail"] = dis_admin_index();

} elseif ($action == "priority_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("incident", "priority", $incident);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_priority)." : $l_c_insert_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_priority)." : $l_c_insert_error");
  }
  require("incident_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "priority_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("incident", "priority", $incident);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_priority)." : $l_c_update_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_priority)." : $l_c_update_error");
  }
  require("incident_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "priority_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("incident", "priority", $incident, "mono");

} elseif ($action == "priority_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("incident", "priority", $incident);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_priority)." : $l_c_delete_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_priority)." : $l_c_delete_error");   
  }
  require("incident_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "status_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("incident", "status", $incident);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_status)." : $l_c_insert_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_status)." : $l_c_insert_error");
  }
  require("incident_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "status_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("incident", "status", $incident);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_status)." : $l_c_update_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_status)." : $l_c_update_error");
  }
  require("incident_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "status_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("incident", "status", $incident, "mono");

} elseif ($action == "status_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("incident", "status", $incident);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_status)." : $l_c_delete_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_status)." : $l_c_delete_error");
  }
  require("incident_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "category1_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("incident", "category1", $incident);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category1)." : $l_c_insert_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category1)." : $l_c_insert_error");
  }
  require("incident_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "category1_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("incident", "category1", $incident);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category1)." : $l_c_update_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category1)." : $l_c_update_error");
  }
  require("incident_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "category1_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("incident", "category1", $incident, "mono");

} elseif ($action == "category1_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("incident", "category1", $incident);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category1)." : $l_c_delete_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category1)." : $l_c_delete_error");
  }
  require("incident_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($uid, "incident", 1);
  $display["detail"] = dis_incident_display_pref($prefs); 

} else if($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($uid, "incident", 1);
  $display["detail"] = dis_incident_display_pref($prefs);

} else if($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($uid, "incident", 1);
  $display["detail"] = dis_incident_display_pref($prefs);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
update_incident_action();
$display["head"] = display_head($l_incident);
$display["header"] = display_menu($module);
$display["end"] = display_end();
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Contact parameters transmited in $incident hash
// returns : $incident hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_incident() {
  global $tf_lcontract, $tf_lincident, $tf_company, $tf_id;
  global $sel_hour, $sel_dur, $sel_logger, $sel_owner, $cb_archive;
  global $tf_date, $ta_solu, $param_contract, $param_incident;
  global $tf_dateafter,$tf_datebefore, $contract_new_id;
  global $tf_duration, $tf_pri, $tf_order, $tf_color;
  global $set_debug, $cdg_param,$res_duration;
  global $ta_com, $tf_datecomment, $sel_usercomment, $ta_add_comment;
  global $tf_priority_label, $tf_priority_code, $sel_priority;
  global $tf_status_label, $tf_status_code, $sel_status;
  global $tf_category1_label, $tf_category1_code, $sel_category1;


  if (isset ($tf_dateafter)) $incident["date_after"] = $tf_dateafter;
  if (isset ($tf_datebefore)) $incident["date_before"] = $tf_datebefore;
  if (isset ($tf_id)) $incident["id"] = $tf_id;
  if (isset ($param_incident)) $incident["id"] = $param_incident;
  if (isset ($param_contract)) $incident["contract_id"] = $param_contract;
  if (isset ($tf_lcontract)) $incident["lcontract"] = $tf_lcontract;
  if (isset ($tf_lincident)) $incident["lincident"] = $tf_lincident;
  if (isset ($sel_owner)) $incident["owner"] = $sel_owner;
  if (isset ($sel_logger)) $incident["logger"] = $sel_logger;
  if (isset ($tf_date)) $incident["date"] = $tf_date;
  if (isset ($tf_duration)) $incident["duration"] = $tf_duration;
  if (isset ($sel_hour)) $incident["hour"] = $sel_hour;
  if (isset ($ta_solu)) $incident["solution"] = $ta_solu;
  if (isset ($contract_new_id)) $incident["cont_new_id"] = $contract_new_id;
  if (isset ($tf_company)) $incident["company"] = $tf_company;
  if (isset ($ta_com)) $incident["com"] = $ta_com;
  if (isset ($tf_datecomment)) $incident["datecomment"] = $tf_datecomment;
  if (isset ($sel_usercomment)) $incident["usercomment"] = $sel_usercomment;
  if (isset ($ta_add_comment)) $incident["add_comment"] = trim($ta_add_comment);
  if (isset ($sel_dur)) $incident["add_duration"] = $sel_dur;
  if (isset ($tf_priority_label)) $incident["priority_label"] = $tf_priority_label;
  if (isset ($tf_priority_code)) $incident["priority_code"] = $tf_priority_code;
  if (isset ($sel_priority)) $incident["priority"] = $sel_priority;
  if (isset ($tf_status_label)) $incident["status_label"] = $tf_status_label;
  if (isset ($tf_status_code)) $incident["status_code"] = $tf_status_code;
  if (isset ($sel_status)) $incident["status"] = $sel_status;
  if (isset ($tf_category1_label)) $incident["category1_label"] = $tf_category1_label;
  if (isset ($tf_category1_code)) $incident["category1_code"] = $tf_category1_code;
  if (isset ($sel_category1)) $incident["category1"] = $sel_category1;

  $incident["archive"] = ( ($cb_archive == '1') ? '1' : '0');

  // Admin - Priority fields
  $incident["pri_color"] = (isset($tf_color) ? $tf_color : "");

  display_debug_param($incident);

  return $incident;
}



///////////////////////////////////////////////////////////////////////////////
// Incident actions
///////////////////////////////////////////////////////////////////////////////
function get_incident_action() {
  global $incident, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult, $l_header_admin, $l_header_display;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

//  Index
  $actions["incident"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/incident/incident_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                	);

//  Search
  $actions["incident"]["search"] = array (
    'Url'      => "$path/incident/incident_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                	);

//  New
  $actions["incident"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/incident/incident_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('','search','index','detailconsult', 'insert', 'update','display') 
                    		       );

//  Detail Consult
  $actions["incident"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/incident/incident_index.php?action=detailconsult&amp;param_incident=".$incident["id"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('detailconsult', 'detailupdate')
                                	       );

//  Detail Update
  $actions["incident"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/incident/incident_index.php?action=detailupdate&amp;param_incident=".$incident["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'update')
                                     	        );

//  Insert
  $actions["incident"]["insert"] = array (
    'Url'      => "$path/incident/incident_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	 );

//  Update
  $actions["incident"]["update"] = array (
    'Url'      => "$path/incident/incident_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	 );

  // Check Delete
  $actions["incident"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/incident/incident_index.php?action=check_delete&amp;param_incident=".$incident["id"],
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update')
                                     );

//  Delete
  $actions["incident"]["delete"] = array (
    'Url'      => "$path/incident/incident_index.php?action=delete&amp;param_incident=".$incident["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	 );
//  Admin
  $actions["incident"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/incident/incident_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                       );

//  Priority insert
  $actions["incident"]["priority_insert"] = array (
    'Url'      => "$path/incident/incident_index.php?action=priority_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Priority update
  $actions["incident"]["priority_update"] = array (
    'Url'      => "$path/incident/incident_index.php?action=priority_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Priority Check Link
  $actions["incident"]["priority_checklink"] = array (
    'Url'      => "$path/incident/incident_index.php?action=priority_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Priority delete
  $actions["incident"]["priority_delete"] = array (
    'Url'      => "$path/incident/incident_index.php?action=priority_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Status insert
  $actions["incident"]["status_insert"] = array (
    'Url'      => "$path/incident/incident_index.php?action=status_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Status update
  $actions["incident"]["status_update"] = array (
    'Url'      => "$path/incident/incident_index.php?action=status_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Status Check Link
  $actions["incident"]["status_checklink"] = array (
    'Url'      => "$path/incident/incident_index.php?action=status_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Status delete
  $actions["incident"]["status_delete"] = array (
    'Url'      => "$path/incident/incident_index.php?action=status_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Category1 insert
  $actions["incident"]["category1_insert"] = array (
    'Url'      => "$path/incident/incident_index.php?action=category1_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Category1 update
  $actions["incident"]["category1_update"] = array (
    'Url'      => "$path/incident/incident_index.php?action=category1_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Category1 Check Link
  $actions["incident"]["category1_checklink"] = array (
    'Url'      => "$path/incident/incident_index.php?action=category1_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Category1 delete
  $actions["incident"]["category1_delete"] = array (
    'Url'      => "$path/incident/incident_index.php?action=category1_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Display
  $actions["incident"]["display"] = array (
     'Name'     => $l_header_display,
     'Url'      => "$path/incident/incident_index.php?action=display",
     'Right'    => $cright_read,
     'Condition'=> array ('all') 
                                      	   );

//  Display Préférence
  $actions["incident"]["dispref_display"] = array (
     'Url'      => "$path/incident/incident_index.php?action=dispref_display",
     'Right'    => $cright_read,
     'Condition'=> array ('None') 
                                      	   );

//  Display level
  $actions["incident"]["dispref_level"] = array (
     'Url'      => "$path/incident/incident_index.php?action=dispref_level",
     'Right'    => $cright_read,
     'Condition'=> array ('None') 
                                      	   );

}


///////////////////////////////////////////////////////////////////////////////
// Incident Actions URL updates (after processing, before displaying menu)  
///////////////////////////////////////////////////////////////////////////////
function update_incident_action() {
  global $incident, $actions, $path;
  
  $id = $incident["id"];
  if ($id > 0) {
    // Detail Consult
    $actions["incident"]["detailconsult"]['Url'] = "$path/incident/incident_index.php?action=detailconsult&amp;param_incident=$id";
    $actions["incident"]["detailconsult"]['Condition'][] = 'insert';
    
    // Detail Update
    $actions["incident"]["detailupdate"]['Url'] = "$path/incident/incident_index.php?action=detailupdate&amp;param_incident=$id";
    $actions["incident"]["detailupdate"]['Condition'][] = 'insert';
    
    // Check Delete
    $actions["incident"]["check_delete"]['Url'] = "$path/incident/incident_index.php?action=check_delete&amp;param_incident=$id";
    $actions["incident"]["check_delete"]['Condition'][] = 'insert';
  }

}

</script>
