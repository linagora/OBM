<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : resourcegroup_index.php                                      //
//     - Desc : Resource Group Index File                                    //
// 2005-03-15 Florent Goalabr�                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the group search form
// - search          -- search fields  -- show the result set of search
// - new             --                -- show the new group form
// - detailconsult   -- $param_resourcegroup  -- show the group detail
// - detailupdate    -- $param_resourcegroup  -- show the group detail form
// - insert          -- form fields    -- insert the group
// - update          -- form fields    -- update the group
// - check_delete    -- $param_resourcegroup  -- check delete the group
// - delete          -- $param_resourcegroup  -- delete the group
// - resourcegroup_add --                -- add groups
// - resourcegroup_del --                -- delete some resource of the group
// - resource_add    --                -- add resources
// - resource_del    --                -- delete some resource of the group
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
// External API ---------------------------------------------------------------
// - ext_get_ids     --                -- select multiple groups (url or sel) 
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "resourcegroup";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_resourcegroup_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("resourcegroup_display.inc");
require("resourcegroup_query.inc");
require("resourcegroup_js.inc");

get_resourcegroup_action();
$perm->check_permissions($module, $action);

$uid = $auth->auth["uid"];

if (! check_privacy($module, "RGroup", $action, $params["resourcegroup_id"], 0)) {
  $display["msg"] = display_err_msg($l_error_visibility);
  $action = "index";
} else {
  update_last_visit("resourcegroup", $params["resourcegroup_id"], $action);
}

page_close();


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_resourcegroup_search_form($params);
  if ($set_display == "yes") {
    $display["msg"] .= dis_resourcegroup_search_resourcegroup("");
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} else if ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_resourcegroup_search_form($params);
  $display["result"] = dis_resourcegroup_search_resourcegroup($params);

} else if ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_resourcegroup_form($action, "", $params);

} else if ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_resourcegroup_consult($params, $uid);

} else if ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_resourcegroup_detail($params["resourcegroup_id"]);
  $display["detail"] = html_resourcegroup_form($action, $obm_q, $params);

} else if ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_resourcegroup_data_form($params)) {

    // If the context (same group) was confirmed ok, we proceed
    if ($hd_confirm == $c_yes) {
      $params["resourcegroup_id"] = run_query_resourcegroup_insert($params);
      if ($params["resourcegroup_id"]) {
	$display["msg"] .= display_ok_msg("$l_resourcegroup : $l_insert_ok");
	$display["detail"] = dis_resourcegroup_consult($params, $uid);
      } else {
	$display["msg"] .= display_err_msg("$l_resourcegroup : $l_insert_error");
	$display["detail"] = html_resourcegroup_form($action, "", $params);
      }
      
      // If it is the first try, we warn the user if some groups seem similar
    } else {
      $obm_q = check_resourcegroup_context("", $params);
      if ($obm_q->num_rows() > 0) {
	$display["detail"] = dis_resourcegroup_warn_insert("", $obm_q, $params);
      } else {
	$params["resourcegroup_id"] = run_query_resourcegroup_insert($params);
	if ($params["resourcegroup_id"] > 0) {
	  $display["msg"] .= display_ok_msg("$l_resourcegroup : $l_insert_ok");
	  $display["detail"] = dis_resourcegroup_consult($params, $uid);
	} else {
	  $display["msg"] .= display_err_msg("$l_resourcegroup : $l_insert_error");
	  $display["detail"] = html_resourcegroup_form($action, "", $params);
	}
      }
    }
    
    // Form data are not valid
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    $display["detail"] = html_resourcegroup_form($action, "", $params);
  }

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_resourcegroup_data_form($params)) {
    $retour = run_query_resourcegroup_update($params);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_resourcegroup : $l_update_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_resourcegroup : $l_update_error");
    }
    $display["detail"] = dis_resourcegroup_consult($params, $uid);
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    $params_q = run_query_resourcegroup_detail($params["resourcegroup_id"]);
    $display["detail"] = html_resourcegroup_form($action, $params_q, $params);
  }

} elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_resourcegroup_warn_delete($params["resourcegroup_id"]);

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_resourcegroup_delete($hd_resourcegroup_id);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_resourcegroup : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_resourcegroup : $l_delete_error");
  }
  $display["search"] = html_resourcegroup_search_form("");

} elseif ($action == "resource_add") {
///////////////////////////////////////////////////////////////////////////////
  if ($params["resource_nb"] > 0) {
    $nb = run_query_resourcegroup_resourcegroup_insert($params);
    $display["msg"] .= display_ok_msg("$nb $l_resource_added");
  } else {
    $display["msg"] .= display_err_msg($l_no_resource_added);
  }
  $display["detail"] = dis_resourcegroup_consult($params, $uid);

} elseif ($action == "resource_del") {
///////////////////////////////////////////////////////////////////////////////
  if ($params["resource_nb"] > 0) {
    $nb = run_query_resourcegroup_resourcegroup_delete($params);
    $display["msg"] .= display_ok_msg("$nb $l_resource_removed");
  } else {
    $display["msg"] .= display_err_msg($l_no_resource_deleted);
  }
  $display["detail"] = dis_resourcegroup_consult($params, $uid);

} elseif ($action == "resourcegroup_add") {
///////////////////////////////////////////////////////////////////////////////
  if ($params["resourcegroup_nb"] > 0) {
    $nb = run_query_resourcegroupresourcegroup_insert($params);
    $display["msg"] .= display_ok_msg("$nb $l_resourcegroup_added");
  } else {
    $display["msg"] .= display_err_msg($l_no_resourcegroup_added);
  }
  $display["detail"] = dis_resourcegroup_consult($params, $uid);

} elseif ($action == "resourcegroup_del") {
///////////////////////////////////////////////////////////////////////////////
  if ($params["resourcegroup_nb"] > 0) {
    $nb = run_query_resourcegroupresourcegroup_delete($params);
    $display["msg"] .= display_ok_msg("$nb $l_resourcegroup_removed");
  } else {
    $display["msg"] .= display_err_msg($l_no_resourcegroup_deleted);
  }
  $display["detail"] = dis_resourcegroup_consult($params, $uid);

} else if ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($uid, "resourcegroup", 1);
  $prefs_r = get_display_pref($uid, "resourcegroup_resource", 1);
  $display["detail"] = dis_resourcegroup_display_pref($prefs, $prefs_r);

} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($uid, "resourcegroup", 1);
  $prefs_r = get_display_pref($uid, "resourcegroup_resource", 1);
  $display["detail"] = dis_resourcegroup_display_pref($prefs, $prefs_r);

} else if($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($uid, "resourcegroup", 1);
  $prefs_r = get_display_pref($uid, "resourcegroup_resource", 1);
  $display["detail"] = dis_resourcegroup_display_pref($prefs, $prefs_r);

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
} else if ($action == "ext_get_ids") {
  $display["search"] = html_resourcegroup_search_form($params);
  if ($set_display == "yes") {
    $display["result"] = dis_resourcegroup_search_resourcegroup($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
// Update actions url in case some values have been updated (id after insert) 
update_resourcegroup_action();
$display["head"] = display_head($l_resourcegroup);
if (! $params["popup"]) {
  $display["header"] = display_menu($module);
}
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $group hash, Group parameters transmited
// returns : $group hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_resourcegroup_params() {

  // Get global params
  $params = get_global_params("ResourceGroup");

  //Get resourcegroup specific params
  if (isset ($params["ext_id"])) $params["resourcegroup_id"] = $params["ext_id"];

  $nb_u = 0;
  $nb_resourcegroup = 0;
  foreach($params as $key => $value ) {
    if (strcmp(substr($key, 0, 7),"data-r-") == 0) {
$nb_u++;
      $u_num = substr($key, 7);
      $params["resource$nb_u"] = $u_num;
    } elseif (strcmp(substr($key, 0, 7),"data-g-") == 0) {
$nb_resourcegroup++;
      $params_num = substr($key, 7);
      $params["resourcegroup_$nb_resourcegroup"] = $params_num;
    }
  }
  $params["resource_nb"] = $nb_u;
  $params["resourcegroup_nb"] = $nb_resourcegroup;

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Group Action 
///////////////////////////////////////////////////////////////////////////////
function get_resourcegroup_action() {
  global $params, $actions, $path, $l_resourcegroup;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display,$l_header_admin;
  global $l_header_add_resource, $l_add_resource, $l_header_add_resourcegroup, $l_add_resourcegroup;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["resourcegroup"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    );

// Search
  $actions["resourcegroup"]["search"] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                  );

// New
  $actions["resourcegroup"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('all')
                                  );

// Detail Consult
  $actions["resourcegroup"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=detailconsult&amp;resourcegroup_id=".$params["resourcegroup_id"]."",
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailupdate')
                                  );

// Detail Update
  $actions["resourcegroup"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=detailupdate&amp;resourcegroup_id=".$params["resourcegroup_id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'resource_add', 'resource_del', 'resourcegroup_add', 'resourcegroup_del', 'update')
                                     	   );

// Insert
  $actions["resourcegroup"]["insert"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=insert",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     );

// Update
  $actions["resourcegroup"]["update"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=update",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     );

// Check Delete
  $actions["resourcegroup"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=check_delete&amp;resourcegroup_id=".$params["resourcegroup_id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailupdate', 'resource_add', 'resource_del', 'resourcegroup_add', 'resourcegroup_del', 'update')
                                     	   );

// Delete
  $actions["resourcegroup"]["delete"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=delete",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     );

// Ext get Ids : external Group selection
  $actions["resourcegroup"]["ext_get_ids"] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                    	  );

// sel resourcegroup add : Groups selection
  $actions["resourcegroup"]["sel_resourcegroup_add"] = array (
    'Name'     => $l_header_add_resourcegroup,
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_resourcegroup)."&amp;ext_action=resourcegroup_add&amp;ext_url=".urlencode($path."/resourcegroup/resourcegroup_index.php")."&amp;ext_id=".$params["resourcegroup_id"]."&amp;ext_target=$l_resourcegroup&amp;child_res=1",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Target'   => $l_resourcegroup,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                    	  );

// Sel resource add : Resources selection
  $actions["resourcegroup"]["sel_resource_add"] = array (
    'Name'     => $l_header_add_resource,
    'Url'      => "$path/resource/resource_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_resource)."&amp;ext_action=resource_add&amp;ext_url=".urlencode($path."/resourcegroup/resourcegroup_index.php")."&amp;ext_id=".$params["resourcegroup_id"]."&amp;ext_target=$l_resourcegroup",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Target'   => $l_resourcegroup,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult','resource_add','resource_del', 'resourcegroup_add','resourcegroup_del', 'update')
                                    	  );

// Resource add
  $actions["resourcegroup"]["resource_add"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=resource_add",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     );

// Resource del
  $actions["resourcegroup"]["resource_del"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=resource_del",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     );

// Group add
  $actions["resourcegroup"]["resourcegroup_add"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=resourcegroup_add",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     );

// Group del
  $actions["resourcegroup"]["resourcegroup_del"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=resourcegroup_del",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     );

// Admin
  $actions["resourcegroup"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all')
                                    );

// Display
  $actions["resourcegroup"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    );
// Display
  $actions["resourcegroup"]["dispref_display"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                         );
// Display
  $actions["resourcegroup"]["dispref_level"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                         );

}


///////////////////////////////////////////////////////////////////////////////
// ResourceGroup Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_resourcegroup_action() {
  global $params, $actions, $path, $l_add_resource, $l_add_resourcegroup, $l_resourcegroup;

  $id = $params["resourcegroup_id"];
  if ($id > 0) {
    // Detail Consult
    $actions["resourcegroup"]["detailconsult"]['Url'] = "$path/resourcegroup/resourcegroup_index.php?action=detailconsult&amp;resourcegroup_id=$id";
    $actions["resourcegroup"]["detailconsult"]['Condition'][] = 'insert';

    // Detail Update
    $actions["resourcegroup"]["detailupdate"]['Url'] = "$path/resourcegroup/resourcegroup_index.php?action=detailupdate&amp;resourcegroup_id=$id";
    $actions["resourcegroup"]["detailupdate"]['Condition'][] = 'insert';

    // Check Delete
    $actions["resourcegroup"]["check_delete"]['Url'] = "$path/resourcegroup/resourcegroup_index.php?action=check_delete&amp;resourcegroup_id=$id";
    $actions["resourcegroup"]["check_delete"]['Condition'][] = 'insert';

    // Sel Resource add
    $actions["resourcegroup"]["sel_resource_add"]['Url'] = "$path/resource/resource_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_resource)."&amp;ext_action=resource_add&amp;ext_url=".urlencode($path."/resourcegroup/resourcegroup_index.php")."&amp;ext_id=$id&amp;ext_target=$l_resourcegroup";
    $actions["resourcegroup"]["sel_resource_add"]['Condition'][] = 'insert';

    // Sel group add : Groups selection
    $actions["resourcegroup"]["sel_resourcegroup_add"]['Url'] = "$path/resourcegroup/resourcegroup_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_resourcegroup)."&amp;ext_action=resourcegroup_add&amp;ext_url=".urlencode($path."/resourcegroup/resourcegroup_index.php")."&amp;ext_id=$id&amp;ext_target=$l_resourcegroup&amp;child_res=1";
    $actions["resourcegroup"]["sel_resourcegoup_add"]['Condition'][] = 'insert';
  }
}

?>
