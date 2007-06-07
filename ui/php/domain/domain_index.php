<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : domain_index.php                                             //
//     - Desc : Domain Index File                                            //
// 2006-17-05 Phan David                                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the domain search form
// - search          -- search fields  -- show the result set of search
// - new             --                -- show the new domain form
// - detailconsult   -- $domain_id    -- show the domain detail
// - detailupdate    -- $domain_id    -- show the domain detail form
// - insert          -- form fields    -- insert the domain
// - update          -- form fields    -- update the domain
// - check_delete    -- $domain_id    -- check links before delete
// - delete          -- $domain_id    -- delete the user
// External API ---------------------------------------------------------------
//
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "domain";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("domain_display.inc");
require("domain_query.inc");

if ($action == "") $action = "index";
$params = get_domain_params();
get_domain_action();
$perm->check_permissions($module, $action);

page_close();


///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_domain_search_form($params);
  if ($_SESSION['set_display'] == "yes") {
    $display["result"] = dis_domain_search_list($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }
  
} elseif ($action == "search")  {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_domain_search_form($params);
  $display["result"] = dis_domain_search_list($params);

} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  require("domain_js.inc");
  $prop_q = run_query_domain_properties();
  $display["detail"] = html_domain_form("",$prop_q,$params);

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_domain_consult($params);

} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_domain_detail($params["domain_id"]);
  $prop_q = run_query_domain_properties($params["domain_id"]);
  if ($obm_q->num_rows() == 1) {
    $display["detailInfo"] = display_record_info($obm_q);
    $display["detail"] = html_domain_form($obm_q, $prop_q, $params);
  } else {
    $display["msg"] .= display_err_msg($l_query_error . " - " . $query . " !");
  }

} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  require("domain_js.inc");
  if (check_domain_data_form("", $params)) {
    $cid = run_query_domain_insert($params);
    if ($cid > 0) {
      set_update_state();
      $params["domain_id"] = $cid;
      $display["msg"] .= display_ok_msg($l_insert_ok);
      $display["detail"] = dis_domain_consult($params);
    } else {
      $display["msg"] .= display_err_msg($l_insert_error);
      $prop_q = run_query_domain_properties();
      $display["detail"] = html_domain_form("",$prop_q,$params);
    }
  // Form data are not valid
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err["msg"]);
    $prop_q = run_query_domain_properties();
    $display["detail"] = html_domain_form("",$prop_q,$params);
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  require("domain_js.inc");
  if (check_domain_data_form($params["domain_id"], $params)) {
    $retour = run_query_domain_update($params["domain_id"], $params);
    if ($retour) {
      set_update_state();
      $display["msg"] .= display_ok_msg($l_update_ok);
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
    }
    $display["detail"] = dis_domain_consult($params);
  } else {
    $display["msg"] .= display_err_msg($err["msg"]);
    $prop_q = run_query_domain_properties($params["domain_id"]);
    $display["detail"] = html_domain_form("", $prop_q, $params);
  }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_domain_can_delete($params["domain_id"])) {
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_domain_can_delete($params["domain_id"]);
  } else {
    $display["msg"] .= display_warn_msg($err["msg"], false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_domain_consult($params);
  }

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_domain_can_delete($params["domain_id"])) {
    $retour = run_query_domain_delete($params["domain_id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_delete_ok);
    } else {
      $display["msg"] .= display_err_msg($l_delete_error);
    }
    $display["search"] = html_domain_search_form($params);
  } else {
    $display["msg"] .= display_warn_msg($err["msg"], false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_domain_consult($params);
  }

}

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_domain, $module, $params);
if (! $params["popup"]) {
  update_domain_action();
  $display["header"] = display_menu($module);
}
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores domain parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_domain_params() {
  
  // Get global params
  $params = get_global_params();

  display_debug_param($params);

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// domain Action 
///////////////////////////////////////////////////////////////////////////////
function get_domain_action() {
  global $params, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display, $l_header_reset;
  global $l_header_upd_group;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

  $id = $params["domain_id"];

// Index
  $actions["domain"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/domain/domain_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    );

// New
  $actions["domain"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/domain/domain_index.php?action=new",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('search','index','insert','update','admin','detailconsult','reset','display')
                                  );

// Search
  $actions["domain"]["search"] = array (
    'Url'      => "$path/domain/domain_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                  );
  
// Detail Consult
  $actions["domain"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/domain/domain_index.php?action=detailconsult&amp;domain_id=$id",
    'Right'    => $cright_read,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'group_consult', 'group_update')
                                  );

// Detail Update
  $actions["domain"]["detailupdate"] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/domain/domain_index.php?action=detailupdate&amp;domain_id=$id",
     'Right'    => $cright_write_admin,
     'Condition'=> array ('detailconsult', 'reset', 'update', 'group_consult', 'group_update')
                                     	   );

// Insert
  $actions["domain"]["insert"] = array (
    'Url'      => "$path/domain/domain_index.php?action=insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     );

// Update
  $actions["domain"]["update"] = array (
    'Url'      => "$path/domain/domain_index.php?action=update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     );

// Check Delete
  $actions["domain"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/domain/domain_index.php?action=check_delete&amp;domain_id=$id",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'reset', 'group_consult', 'group_update') 
                                     	   );

// Delete
  $actions["domain"]["delete"] = array (
    'Url'      => "$path/domain/domain_index.php?action=delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     );
}


///////////////////////////////////////////////////////////////////////////////
// domain Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_domain_action() {
  global $params, $actions, $path;

  $id = $params["domain_id"];
  if ($id > 0) {
    // Detail Consult
    $actions["domain"]["detailconsult"]["Url"] = "$path/domain/domain_index.php?action=detailconsult&amp;domain_id=$id";
    $actions["domain"]["detailconsult"]['Condition'][] = 'insert';

    // Detail Update
    $actions["domain"]["detailupdate"]['Url'] = "$path/domain/domain_index.php?action=detailupdate&amp;domain_id=$id";
    $actions["domain"]["detailupdate"]['Condition'][] = 'insert';

    // Check Delete
    $actions["domain"]["check_delete"]['Url'] = "$path/domain/domain_index.php?action=check_delete&amp;domain_id=$id";
    $actions["domain"]["check_delete"]['Condition'][] = 'insert';

    // Group Consult
    $actions["domain"]["group_consult"]["Url"] = "$path/domain/domain_index.php?action=group_consult&amp;domain_id=$id";
    $actions["domain"]["group_consult"]['Condition'][] = 'insert';
  }
}


?>