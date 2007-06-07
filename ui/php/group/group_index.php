<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : group_index.php                                              //
//     - Desc : Group Index File                                             //
// 2003-08-22 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the group search form
// - search          -- search fields  -- show the result set of search
// - new             --                -- show the new group form
// - detailconsult   -- $param_group   -- show the group detail
// - detailupdate    -- $param_group   -- show the group detail form
// - insert          -- form fields    -- insert the group
// - update          -- form fields    -- update the group
// - check_delete    -- $param_group   -- delete the group
// - delete          -- $param_group   -- delete the group
// - group_add       --                -- add groups
// - group_del       --                -- delete some user of the group
// - user_add        --                -- add users
// - user_del        --                -- delete some user of the group
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
// External API ---------------------------------------------------------------
// - ext_get_ids     --                -- select multiple groups (url or sel) 
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "group";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_group_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("group_display.inc");
require("group_query.inc");
require("group_js.inc");
require_once("$obminclude/of/of_category.inc");

get_group_action();
$perm->check_permissions($module, $action);

if (! check_privacy($module, "UGroup", $action, $params["group_id"])) {
  $display["msg"] = display_err_msg($l_error_visibility);
  $action = "index";
} else {
  update_last_visit("group", $params["group_id"], $action);
}

page_close();


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_group_search_form($params);
  if ($_SESSION['set_display'] == "yes") {
    $display["result"] .= dis_group_search_group("");
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} else if ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_group_search_form($params);
  $display["result"] = dis_group_search_group($params);

} elseif ($action == "ext_search") {
///////////////////////////////////////////////////////////////////////////////
  $grp_q = run_query_group_ext_search($params);
  json_search_group($params, $grp_q);
  echo "(".$display['json'].")";
  exit();

} else if ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_group_form($action, "", $params);

} else if ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_group_consult($params, $obm["uid"]);

} else if ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  if (check_group_update_rights($params)) {
    $obm_q = run_query_group_detail($params["group_id"]);
    $display["detail"] = html_group_form($action, $obm_q, $params);
  } else {
    $display["msg"] .= display_warn_msg($err['msg']);
  }

} else if ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_group_data_form($params)) {

    // If the context (same group) was confirmed ok, we proceed
    if ($params["confirm"] == $c_yes) {
      $params["group_id"] = run_query_group_insert($params);
      if ($params["group_id"]) {
	$display["msg"] .= display_ok_msg("$l_group : $l_insert_ok");
	$display["detail"] = dis_group_consult($params, $obm["uid"]);
      } else {
	$display["msg"] .= display_err_msg("$l_group : $l_insert_error");
	$display["search"] = html_group_search_form($params);
      }
      
      // If it is the first try, we warn the user if some groups seem similar
    } else {
      $obm_q = check_group_context("", $params);
      if ($obm_q->num_rows() > 0) {
	$display["detail"] = dis_group_warn_insert("", $obm_q, $params);
      } else {
	$params["group_id"] = run_query_group_insert($params);
	if ($params["group_id"] > 0) {
	  set_update_state();
	  $display["msg"] .= display_ok_msg("$l_group : $l_insert_ok");
	  $display["detail"] = dis_group_consult($params, $obm["uid"]);
	} else {
	  $display["msg"] .= display_err_msg("$l_group : $l_insert_error");
	  $display["search"] = html_group_search_form($params);
	}
      }
    }
    
    // Form data are not valid
  } else {
    $display["msg"] .= display_err_msg($err["msg"]);
    $display["detail"] = html_group_form($action, "", $params, $err["field"]);
  }

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_group_data_form($params)) {
    $retour = run_query_group_update($params);
    if ($retour) {
      set_update_state();
      $display["msg"] .= display_ok_msg("$l_group : $l_update_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_group : $l_update_error");
    }
    $display["detail"] = dis_group_consult($params, $obm["uid"]);
  } else {
    $display["msg"] .= display_err_msg($err["msg"]);
    $params_q = run_query_group_detail($params["group_id"]);
    $display["detail"] = html_group_form($action, $params_q, $params, $err["field"]);
  }

} elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_group_can_delete($params["group_id"])) {
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_group_can_delete($params["group_id"]);
  } else {
    $display["msg"] .= display_warn_msg($err["msg"], false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_group_consult($params, $obm["uid"]);
  }

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_group_can_delete($params["group_id"])) {
    $retour = run_query_group_delete($params["group_id"]);
    if ($retour) {
      set_update_state();
      $display["msg"] .= display_ok_msg("$l_group : $l_delete_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_group : $l_delete_error");
    }
    $display["search"] = html_group_search_form("");
  } else {
    $display["msg"] .= display_warn_msg($err["msg"], false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_group_consult($params, $obm["uid"]);
  }

} elseif ($action == "user_add") {
///////////////////////////////////////////////////////////////////////////////
  if (check_group_update_rights($params)) {
    if ($params["user_nb"] > 0) {
      $nb = run_query_group_usergroup_insert($params);
      set_update_state();
      $display["msg"] .= display_ok_msg("$nb $l_user_added");
    } else {
      $display["msg"] .= display_err_msg($l_no_user_added);
    }
  } else {
    $display["msg"] .= display_warn_msg($err['msg']);
  }
  $display["detail"] = dis_group_consult($params, $obm["uid"]);

} elseif ($action == "user_del") {
///////////////////////////////////////////////////////////////////////////////
  if (check_group_update_rights($params)) {
    if ($params["user_nb"] > 0) {
      $nb = run_query_group_usergroup_delete($params);
      set_update_state();
      $display["msg"] .= display_ok_msg("$nb $l_user_removed");
    } else {
      $display["msg"] .= display_err_msg($l_no_user_deleted);
    }
  } else {
    $display["msg"] .= display_warn_msg($err['msg']);
  }
  $display["detail"] = dis_group_consult($params, $obm["uid"]);

} elseif ($action == "group_add") {
///////////////////////////////////////////////////////////////////////////////
  if (check_group_update_rights($params)) {
    if ($params["group_nb"] > 0) {
      $nb = run_query_group_group_insert($params, $g["privacy"]);
      set_update_state();
      $display["msg"] .= display_ok_msg("$nb $l_group_added");
    } else {
      $display["msg"] .= display_err_msg($l_no_group_added);
    }
  } else {
    $display["msg"] .= display_warn_msg($err['msg']);
  }
  $display["detail"] = dis_group_consult($params, $obm["uid"]);

} elseif ($action == "group_del") {
///////////////////////////////////////////////////////////////////////////////
  if (check_group_update_rights($params)) {
    if ($params["group_nb"] > 0) {
      $nb = run_query_group_group_delete($params);
      set_update_state();
      $display["msg"] .= display_ok_msg("$nb $l_group_removed");
    } else {
      $display["msg"] .= display_err_msg($l_no_group_deleted);
    }
  } else {
    $display["msg"] .= display_warn_msg($err['msg']);
  }
  $display["detail"] = dis_group_consult($params, $obm["uid"]);

} else if ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm["uid"], "group", 1);
  $prefs_u = get_display_pref($obm["uid"], "group_user", 1);
  $display["detail"] = dis_group_display_pref($prefs, $prefs_u);

} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm["uid"], "group", 1);
  $prefs_u = get_display_pref($obm["uid"], "group_user", 1);
  $display["detail"] = dis_group_display_pref($prefs, $prefs_u);

} else if ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm["uid"], "group", 1);
  $prefs_u = get_display_pref($obm["uid"], "group_user", 1);
  $display["detail"] = dis_group_display_pref($prefs, $prefs_u);

} elseif ($action == "admin") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_group_admin_index();

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
} else if ($action == "ext_get_ids") {
  $display["search"] = html_group_search_form($params);
  if ($_SESSION['set_display'] == "yes") {
    $display["result"] = dis_group_search_group($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }
}

of_category_user_action_switch($module, $action, $params);

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
// Update actions url in case some values have been updated (id after insert) 
update_group_action();
$display["head"] = display_head($l_group);
if (! $params["popup"]) {
  $display["header"] = display_menu($module);
}
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $params hash, Group parameters transmited
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_group_params() {
  
  // Get global params
  $params = get_global_params("Group");

  // Get group specific params
  // Group fields
  if (isset ($params["ext_id"])) $params["group_id"] = trim($params["ext_id"]);
    
  $nb_u = 0;
  $nb_group = 0;
  foreach ($params as $key => $value) {
    if (strcmp(substr($key, 0, 7),"data-u-") == 0) {
      $nb_u++;
      $u_num = substr($key, 7);
      $params["user$nb_u"] = $u_num;
    } elseif (strcmp(substr($key, 0, 7),"data-g-") == 0) {
      $nb_group++;
      $params_num = substr($key, 7);
      $params["group_$nb_group"] = $params_num;
    }
  }
  $params["user_nb"] = $nb_u;
  $params["group_nb"] = $nb_group;
  
  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Group Action 
///////////////////////////////////////////////////////////////////////////////
function get_group_action() {
  global $params, $actions, $path, $l_group;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display,$l_header_admin;
  global $l_header_add_user, $l_add_user, $l_header_add_group, $l_add_group;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

  of_category_user_module_action("group");

// Index
  $actions["group"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/group/group_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

// Search
  $actions["group"]["search"] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                  );

  $actions["group"]["ext_search"] = array (
    'Url'      => "$path/group/group_index.php?action=ext_search",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
  );  

// New
  $actions["group"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/group/group_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('all')
                                  );

// Detail Consult
  $actions["group"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/group/group_index.php?action=detailconsult&amp;group_id=".$params["group_id"]."",
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update')
                                  );

// Detail Update
  $actions["group"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/group/group_index.php?action=detailupdate&amp;group_id=".$params["group_id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'user_add', 'user_del', 'group_add', 'group_del', 'update')
                                     	   );

// Insert
  $actions["group"]["insert"] = array (
    'Url'      => "$path/group/group_index.php?action=insert",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     );

// Update
  $actions["group"]["update"] = array (
    'Url'      => "$path/group/group_index.php?action=update",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     );

// Check Delete
  $actions["group"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/group/group_index.php?action=check_delete&amp;group_id=".$params["group_id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailupdate', 'user_add', 'user_del', 'group_add', 'group_del', 'update')
                                     	   );

// Delete
  $actions["group"]["delete"] = array (
    'Url'      => "$path/group/group_index.php?action=delete",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     );

// Ext get Ids : external Group selection
  $actions["group"]["ext_get_ids"] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    	  );

// sel group add : Groups selection
  $actions["group"]["sel_group_add"] = array (
    'Name'     => $l_header_add_group,
    'Url'      => "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_group)."&amp;ext_action=group_add&amp;ext_url=".urlencode($path."/group/group_index.php")."&amp;ext_id=".$params["group_id"]."&amp;ext_target=$l_group&amp;child_res=1",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Target'   => $l_group,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult','user_add','user_del', 'group_add','group_del', 'update')
                                    	  );

// Sel user add : Users selection
  $actions["group"]["sel_user_add"] = array (
    'Name'     => $l_header_add_user,
    'Url'      => "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_user)."&amp;ext_action=user_add&amp;ext_url=".urlencode($path."/group/group_index.php")."&amp;ext_id=".$params["group_id"]."&amp;ext_target=$l_group",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Target'   => $l_group,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult','user_add','user_del', 'group_add','group_del', 'update')
                                    	  );

// User add
  $actions["group"]["user_add"] = array (
    'Url'      => "$path/group/group_index.php?action=user_add",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     );

// User del
  $actions["group"]["user_del"] = array (
    'Url'      => "$path/group/group_index.php?action=user_del",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     );

// Group add
  $actions["group"]["group_add"] = array (
    'Url'      => "$path/group/group_index.php?action=group_add",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     );

// Group del
  $actions["group"]["group_del"] = array (
    'Url'      => "$path/group/group_index.php?action=group_del",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     );
// Display
  $actions["group"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/group/group_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                      	 );

// Display
  $actions["group"]["dispref_display"] = array (
    'Url'      => "$path/group/group_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                      	 );
// Display
  $actions["group"]["dispref_level"] = array (
    'Url'      => "$path/group/group_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                      	 );
// Admin
  $actions["group"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/group/group_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all')
                                                 );

}


///////////////////////////////////////////////////////////////////////////////
// Group Actions updates (after processing, before displaying menu)  
///////////////////////////////////////////////////////////////////////////////
function update_group_action() {
  global $params, $actions, $path, $l_add_user, $l_add_group, $l_group;
  global $cright_write_admin;

  $id = $params["group_id"];

  if ($id > 0) {
    $g = get_group_info($id);

    // Allow public group handling only if write_admin right
    if ($g['privacy'] != 1) {
      $actions['group']['detailupdate']['Right'] = $cright_write_admin;
      $actions['group']['update']['Right'] = $cright_write_admin;
      $actions['group']['insert']['Right'] = $cright_write_admin;
      $actions['group']['check_delete']['Right'] = $cright_write_admin;
      $actions['group']['delete']['Right'] = $cright_write_admin;
      $actions['group']['sel_user_add']['Right'] = $cright_write_admin;
      $actions['group']['user_add']['Right'] = $cright_write_admin;
      $actions['group']['user_del']['Right'] = $cright_write_admin;
      $actions['group']['sel_group_add']['Right'] = $cright_write_admin;
      $actions['group']['group_add']['Right'] = $cright_write_admin;
      $actions['group']['group_del']['Right'] = $cright_write_admin;
    }

    // Detail Consult
    $actions["group"]["detailconsult"]['Url'] = "$path/group/group_index.php?action=detailconsult&amp;group_id=$id";
    $actions["group"]["detailconsult"]['Condition'][] = 'insert';

    // If user has update right on the group
    if (check_group_update_rights($params, $g)) {

      // Detail Update
      $actions["group"]["detailupdate"]['Url'] = "$path/group/group_index.php?action=detailupdate&amp;group_id=$id";
      $actions["group"]["detailupdate"]['Condition'][] = 'insert';

      // Check Delete
      $actions["group"]["check_delete"]['Url'] = "$path/group/group_index.php?action=check_delete&amp;group_id=$id";
      $actions["group"]["check_delete"]['Condition'][] = 'insert';

      // Sel User add
      $actions["group"]["sel_user_add"]['Url'] = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_user)."&amp;ext_action=user_add&amp;ext_url=".urlencode($path."/group/group_index.php")."&amp;ext_id=$id&amp;ext_target=$l_group";
      $actions["group"]["sel_user_add"]['Condition'][] = 'insert';

      // Sel group add : Groups selection
      $actions["group"]["sel_group_add"]['Url'] = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_group)."&amp;ext_action=group_add&amp;ext_url=".urlencode($path."/group/group_index.php")."&amp;ext_id=$id&amp;ext_target=$l_group&amp;child_res=1";
      $actions["group"]["sel_group_add"]['Condition'][] = 'insert';

    } else {
      // User does not have update rights
      $actions["group"]["detailupdate"]['Condition'] = array('None');
      $actions["group"]["check_delete"]['Condition'] = array('None');
      $actions["group"]["sel_user_add"]['Condition'] = array('None');
      $actions["group"]["sel_group_add"]['Condition'] = array('None');
    }

  }
}


?>
