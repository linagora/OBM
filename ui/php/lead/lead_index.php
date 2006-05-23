<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : lead_index.php 
//     - Desc : lead Index File
// 2006-05-19 Aliacom - PB
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "lead";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
require("$obminclude/global_pref.inc");
require("lead_display.inc");
require("lead_query.inc");
require("lead_js.inc");
require_once("$obminclude/of/of_category.inc");
require_once("$obminclude/javascript/calendar_js.inc");

if ($action == "") $action = "index";
$params = get_param_lead();
get_lead_action();
$perm->check_permissions($module, $action);

update_last_visit("lead", $params["id"], $action);
page_close();


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_lead_search_form($params);
  if ($set_display == "yes") {
    $display["result"] = dis_lead_search_list($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_lead_search_form($params);
  $display["result"] = dis_lead_search_list($params);
  
} elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  require("lead_js.inc");
  $display["detail"] = dis_lead_form($action, $params);

} elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_lead_data_form("", $params)) {
    $id = run_query_lead_insert($params);
    if ($id > 0) {
      $params["id"] = $id;
      $display["msg"] = display_ok_msg ($l_insert_ok);
      $display["detail"] = dis_lead_consult($params);
    }
  // Form data are not valid
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    require("lead_js.inc");
    $display["detail"] = dis_lead_form($action, $params);
  }

} elseif ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_lead_consult($params);

} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  require("lead_js.inc");
  $display["detail"] = dis_lead_form($action, $params);

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_lead_data_form($params["id"], $params)) {
    $retour = run_query_lead_update($params["id"], $params);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok);
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
    }
    $display["detail"] = dis_lead_consult($params);
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = dis_lead_form($action, $params);
  }

} elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_lead($params["id"])) {
    require("lead_js.inc");
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_can_delete_lead($params["id"]);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_lead_consult($params);
  }

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_lead($params["id"])) {
    $retour = run_query_lead_delete($params["id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_delete_ok);
    } else {
      $display["msg"] .= display_err_msg($l_delete_error);
    }
    $display["search"] = dis_lead_search_form($params);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_lead_consult($params);
  }
  
} elseif ($action == "admin") {
///////////////////////////////////////////////////////////////////////////////
  require("lead_js.inc");
  $display["detail"] = dis_lead_admin_index();

} elseif ($action == "source_insert") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("lead", "source", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_source)." : $l_c_insert_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_source)." : $l_c_insert_error");
  }
  require("lead_js.inc");
  $display["detail"] .= dis_lead_admin_index();

} elseif ($action == "source_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("lead", "source", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_source)." : $l_c_update_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_source)." : $l_c_update_error");
  }
  require("lead_js.inc");
  $display["detail"] .= dis_lead_admin_index();

} elseif ($action == "source_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("lead", "source", $params, "mono");

} elseif ($action == "source_delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("lead", "source", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_source)." : $l_c_delete_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_source)." : $l_c_delete_error");
  }
  require("lead_js.inc");
  $display["detail"] .= dis_lead_admin_index();

} elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($auth->auth["uid"], "lead", 1);
  $display["detail"] = dis_lead_display_pref($prefs);

} elseif ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($auth->auth["uid"], "lead", 1);
  $display["detail"] = dis_lead_display_pref($prefs);

} elseif ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($auth->auth["uid"], "lead", 1);
  $display["detail"] = dis_lead_display_pref($prefs);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head("$l_lead");
update_lead_action();
$display["header"] = display_menu($module);
$display["end"] = display_end();
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Lead parameters transmitted in $lead hash
// returns : $lead hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_lead() {
  global $sel_manager, $tf_name, $tf_date, $tf_datealarm, $ta_comment;
  global $tf_datecomment, $sel_usercomment, $ta_add_comment, $rd_mail_comment;
  global $param_lead, $cb_archive, $tf_todo, $cb_privacy;
  global $tf_source_label, $tf_source_code, $sel_source;

  global $param_company, $company_name, $company_new_name, $company_new_id;

  if (isset ($param_lead)) $lead["id"] = $param_lead;
  if (isset ($sel_source)) $lead["source_id"] = $sel_source;
  if (isset ($sel_manager)) $lead["manager_id"] = $sel_manager;
  if (isset ($tf_name)) $lead["name"] = $tf_name;
  if (isset ($tf_date)) $lead["date"] = $tf_date;
  if (isset ($tf_datealarm)) $lead["datealarm"] = $tf_datealarm;
  if (isset ($tf_todo)) $lead["todo"] = $tf_todo;
  if (isset ($cb_privacy)) { $lead["privacy"] = ($cb_privacy == 1 ? 1 : 0); };
  if (isset ($ta_comment)) $lead["comment"] = $ta_comment;
  if (isset ($tf_datecomment)) $lead["datecomment"] = $tf_datecomment;
  if (isset ($sel_usercomment)) $lead["usercomment"] = $sel_usercomment;
  if (isset ($ta_add_comment)) $lead["add_comment"] = trim($ta_add_comment);
  if (isset ($cb_archive)) $lead["archive"] = ($cb_archive == 1 ? 1 : 0);

  if (isset ($param_company)) $lead["company_id"] = $param_company;
  if (isset ($tf_company)) $lead["company"] = $tf_company;
  if (isset ($company_name)) $lead["company_name"] = $company_name;
  if (isset ($company_new_name)) $lead["comp_new_name"] = $company_new_name;
  if (isset ($company_new_id)) $lead["comp_new_id"] = $company_new_id;

  if (isset ($tf_source_label)) $lead["source_label"] = $tf_source_label;
  if (isset ($tf_source_code)) $lead["source_code"] = $tf_source_code;

  display_debug_param($lead);

  return $lead;
}


//////////////////////////////////////////////////////////////////////////////
// Lead actions
//////////////////////////////////////////////////////////////////////////////
function get_lead_action() {
  global $params, $actions, $path;
  global $l_header_find, $l_header_new, $l_header_update, $l_header_delete;
  global $l_header_consult, $l_header_display, $l_header_admin;
  global $l_header_duplicate;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

  $id = $params["id"];

//Index
  $actions["lead"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/lead/lead_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                        );

// Search
  $actions["lead"]["search"] = array (
    'Url'      => "$path/lead/lead_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                        );

// New
  $actions["lead"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/lead/lead_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
                                     );

// Detail Consult
  $actions["lead"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/lead/lead_index.php?action=detailconsult&amp;param_lead=".$params["id"],
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate')
                                        );

// Detail Update
  $actions["lead"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/lead/lead_index.php?action=detailupdate&amp;param_lead=".$params["id"],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
                                     	      );

// Insert
  $actions["lead"]["insert"] = array (
    'Url'      => "$path/lead/lead_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                                );

// Update
  $actions["lead"]["update"] = array (
    'Url'      => "$path/lead/lead_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                                );

// Check Delete
  $actions["lead"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/lead/lead_index.php?action=check_delete&amp;param_lead=".$params["id"],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate')
                                     	 );

// Delete
  $actions["lead"]["delete"] = array (
    'Url'      => "$path/lead/lead_index.php?action=delete&amp;param_lead=".$params["id"],
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	 );

// Admin
  $actions["lead"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/lead/lead_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                       );

// Source Insert
  $actions["lead"]["source_insert"] = array (
    'Url'      => "$path/lead/lead_index.php?action=source_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Source Update
  $actions["lead"]["source_update"] = array (
    'Url'      => "$path/lead/lead_index.php?action=source_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Source Check Link
  $actions["lead"]["source_checklink"] = array (
    'Url'      => "$path/lead/lead_index.php?action=source_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Source Delete
  $actions["lead"]["source_delete"] = array (
    'Url'      => "$path/lead/lead_index.php?action=source_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );


// Display
  $actions["lead"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/lead/lead_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );

// Display
  $actions["lead"]["dispref_display"] = array (
    'Url'      => "$path/lead/lead_index.php?action=display_dispref",
    'Right'    => $cright_read, 
    'Condition'=> array ('None') 
                                      	 );

// Display Level
  $actions["lead"]["dispref_level"] = array (
    'Url'      => "$path/lead/lead_index.php?action=display_level",
    'Right'    => $cright_read, 
    'Condition'=> array ('None') 
                                      	        );

}


///////////////////////////////////////////////////////////////////////////////
// Lead Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_lead_action() {
  global $params, $actions, $path, $l_lead;

  $id = $params["id"];
  if ($id > 0) {
    // Detail Consult
    $actions["lead"]["detailconsult"]["Url"] = "$path/lead/lead_index.php?action=detailconsult&amp;param_lead=$id";
    $actions["lead"]["detailconsult"]['Condition'][] = 'insert';

    // Detail Update
    $actions["lead"]["detailupdate"]['Url'] = "$path/lead/lead_index.php?action=detailupdate&amp;param_lead=$id";
    $actions["lead"]["detailupdate"]['Condition'][] = 'insert';

    // Check Delete
    $actions["lead"]["check_delete"]['Url'] = "$path/lead/lead_index.php?action=check_delete&amp;param_lead=$id";
    $actions["lead"]["check_delete"]['Condition'][] = 'insert';
  }
}

?>