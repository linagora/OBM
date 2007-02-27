<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailserver_index.php                                         //
//     - Desc : Calendar Index File                                          //
// 2007-02-08 - AliaSource - Mehdi Rande                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- this week for this user.
// - new
// - insert
// - detailconsult
// - detailupdate
// - update
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "mailserver";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_mailserver_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("mailserver_query.inc");
require("mailserver_display.inc");
require_once("$obminclude/of/of_category.inc");

get_mailserver_action();
if ($action == "index") {
  $mode = run_query_mailserver_count();
  if ($mode->nf() == 0) {
    $action = "new";
  } elseif($mode->nf() == 1) {
    $params["id"] = $mode->f("mailserver_id");
    $action = "detailconsult";
  }
} 
$perm->check_permissions($module, $action);
page_close();

if ($action == "index") {
///////////////////////////////////////////////////////////////////////////////
  $ms_q = run_query_mailserver_getall();
  $display["result"] = dis_mailserver_list($ms_q);

} elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_mailserver_form($action, $params);

} elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_mailserver_data_form($params)) {
    $params["id"] =  run_query_mailserver_insert($params);
    $ms_q = run_query_mailserver_details($params["id"]);
    $net_q = run_query_mailserver_networks($params["id"]);
    $display["detail"] = dis_mailserver_consult($ms_q, $net_q);
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err["msg"]);
    $display["detail"] = dis_mailserver_form($action, $params, null);
  }

} elseif ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $ms_q = run_query_mailserver_details($params["id"]);
  $net_q = run_query_mailserver_networks($params["id"]);
  $display["detail"] = dis_mailserver_consult($ms_q,$net_q);    

} elseif ($action == "checkdelete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_change_mailserver($params["id"])) {
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_can_delete_mailserver($params["id"]);
  } else {
    $display["msg"] .= display_warn_msg($err["msg"], false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $ms_q = run_query_mailserver_details($params["id"]);
    $net_q = run_query_mailserver_networks($params["id"]);
    $display["detail"] = dis_mailserver_consult($ms_q,$net_q);      
  }

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_change_mailserver($params["id"])) {
    $retour = run_query_mailserver_delete($params["id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_mailserver : $l_delete_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_mailserver : $l_delete_error");
  }
    $ms_q = run_query_mailserver_getall();
    $display["result"] = dis_mailserver_list($ms_q);    
  } else {
    $display["msg"] .= display_warn_msg($err["msg"], false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $ms_q = run_query_mailserver_details($params["id"]);
    $net_q = run_query_mailserver_networks($params["id"]);
    $display["detail"] = dis_mailserver_consult($ms_q,$net_q);  
  }

} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $ms_q = run_query_mailserver_details($params["id"]);
  $net_q = run_query_mailserver_networks($params["id"]);
  $display["detail"] = dis_mailserver_form($action, $params, $ms_q, $net_q);

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_mailserver_data_form($params)) {
    run_query_mailserver_update($params);
    $ms_q = run_query_mailserver_details($params["id"]);
    $net_q = run_query_mailserver_networks($params["id"]);
    $display["detail"] = dis_mailserver_consult($ms_q,$net_q);
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err["msg"]);
    $display["detail"] = dis_mailserver_form($action, $params, null);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_mailserver);
$display["end"] = display_end();
update_mailserver_action();
$display["header"] = display_menu($module);

display_page($display);

///////////////////////////////////////////////////////////////////////////////
// Stores in $params hash, Mailserver parameters transmited
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_mailserver_params() {

  // Get global params
  $params = get_global_params("mailserver");

  return $params;
}

///////////////////////////////////////////////////////////////////////////////
// MailServer Action 
///////////////////////////////////////////////////////////////////////////////
function get_mailserver_action() {
  global $mailserver, $actions, $path;
  global $l_mailserver, $l_header_update,$l_header_consult;
  global $l_header_index, $l_header_delete, $l_header_new;
  global $cright_read_admin,$params;

  $actions["mailserver"]["index"] = array (
    'Name'     => $l_header_index,
    'Url'      => "$path/mailserver/mailserver_index.php?action=index",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
  ); 

  $actions["mailserver"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/mailserver/mailserver_index.php?action=new",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
  );

  $actions["mailserver"]["insert"] = array (
    'Right'    => $cright_read_admin,
    'Condition'=> array ('none') 
  );


  $actions["mailserver"]["checkdelete"] = array (
    'Name'     => $l_header_delete,    
    'Url'      => "$path/mailserver/mailserver_index.php?action=checkdelete&amp;id=".$params["id"],
    'Right'    => $cright_read_admin,
    'Condition'=> array ('detailconsult','insert','update') 
  );

  $actions["mailserver"]["delete"] = array (
    'Right'    => $cright_read_admin,
    'Condition'=> array ('none') 
  );

  $actions["mailserver"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/mailserver/mailserver_index.php?action=detailupdate&amp;id=".$params["id"],
    'Right'    => $cright_read_admin,
    'Condition'=> array ('detailconsult','insert','update') 
  );

  $actions["mailserver"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/mailserver/mailserver_index.php?action=detailconsult&amp;id=".$params["id"],
    'Right'    => $cright_read_admin,
    'Condition'=> array ('detailupdate','update') 
  );

  $actions["mailserver"]["update"] = array (
    'Right'    => $cright_read_admin,
    'Condition'=> array ('none') 
  ); 
}

///////////////////////////////////////////////////////////////////////////////
// Contact Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_mailserver_action() {
  global $params, $actions, $path;

  $id = $params["id"];
  if ($id > 0) {
    // Detail Consult
    $actions["mailserver"]["detailconsult"]["Url"] = "$path/mailserver/mailserver_index.php?action=detailconsult&amp;id=$id";
    
    // Detail Update
    $actions["mailserver"]["detailupdate"]['Url'] = "$path/mailserver/mailserver_index.php?action=detailupdate&amp;id=$id";
    $actions["mailserver"]["detailupdate"]['Condition'][] = 'insert';
    
    // Check Delete
    $actions["mailserver"]["checkdelete"]['Url'] = "$path/mailserver/mailserver_index.php?action=checkdelete&amp;id=$id";
    $actions["mailserver"]["checkdelete"]['Condition'][] = 'insert';
  }
}
