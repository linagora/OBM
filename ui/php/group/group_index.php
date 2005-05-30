<script language="php">
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
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("group_display.inc");
require("group_query.inc");
require("group_js.inc");

if ($action == "") $action = "index";
$group = get_param_group();
get_group_action();
$perm->check_permissions($module, $action);

$uid = $auth->auth["uid"];

update_last_visit("group", $param_group, $action);

page_close();


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_group_search_form($group);
  if ($set_display == "yes") {
    $display["result"] .= dis_group_search_group("");
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} else if ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_group_search_form($group);
  $display["result"] = dis_group_search_group($group);

} else if ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_group_form($action, "", $group);

} else if ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_group_consult($group, $uid);

} else if ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_detail($group["id"]);
  $display["detail"] = html_group_form($action, $obm_q, $group);

} else if ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form($group)) {

    // If the context (same group) was confirmed ok, we proceed
    if ($hd_confirm == $c_yes) {
      $group["id"] = run_query_insert($group);
      if ($group["id"]) {
	$display["msg"] .= display_ok_msg($l_insert_ok);
	$display["detail"] = dis_group_consult($group, $uid);
      } else {
	$display["msg"] .= display_err_msg($l_insert_error);
	$display["search"] = html_group_search_form($group);
      }
      
      // If it is the first try, we warn the user if some groups seem similar
    } else {
      $obm_q = check_group_context("", $group);
      if ($obm_q->num_rows() > 0) {
	$display["detail"] = dis_group_warn_insert("", $obm_q, $group);
      } else {
	$group["id"] = run_query_insert($group);
	if ($group["id"] > 0) {
	  $display["msg"] .= display_ok_msg($l_insert_ok);
	  $display["detail"] = dis_group_consult($group, $uid);
	} else {
	  $display["msg"] .= display_err_msg($l_insert_error);
	  $display["search"] = html_group_search_form($group);
	}
      }
    }
    
    // Form data are not valid
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    $display["detail"] = html_group_form($action, "", $group);
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form($group)) {
    $retour = run_query_update($group);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok);
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
    }
    $display["detail"] = dis_group_consult($group, $uid);
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    $group_q = run_query_detail($group["id"]);
    $display["detail"] = html_group_form($action, $group_q, $group);
  }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_warn_delete($group["id"]);

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_delete($hd_group_id);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_delete_error);
  }
  $display["search"] = html_group_search_form("");

} elseif ($action == "user_add")  {
///////////////////////////////////////////////////////////////////////////////
  if ($group["user_nb"] > 0) {
    $nb = run_query_usergroup_insert($group);
    $display["msg"] .= display_ok_msg("$nb $l_user_added");
  } else {
    $display["msg"] .= display_err_msg($l_no_user_added);
  }
  $display["detail"] = dis_group_consult($group, $uid);

} elseif ($action == "user_del")  {
///////////////////////////////////////////////////////////////////////////////
  if ($group["user_nb"] > 0) {
    $nb = run_query_usergroup_delete($group);
    $display["msg"] .= display_ok_msg("$nb $l_user_removed");
  } else {
    $display["msg"] .= display_err_msg($l_no_user_deleted);
  }
  $display["detail"] = dis_group_consult($group, $uid);

} elseif ($action == "group_add")  {
///////////////////////////////////////////////////////////////////////////////
  if ($group["group_nb"] > 0) {
    $nb = run_query_groupgroup_insert($group);
    $display["msg"] .= display_ok_msg("$nb $l_group_added");
  } else {
    $display["msg"] .= display_err_msg($l_no_group_added);
  }
  $display["detail"] = dis_group_consult($group, $uid);

} elseif ($action == "group_del")  {
///////////////////////////////////////////////////////////////////////////////
  if ($group["group_nb"] > 0) {
    $nb = run_query_groupgroup_delete($group);
    $display["msg"] .= display_ok_msg("$nb $l_group_removed");
  } else {
    $display["msg"] .= display_err_msg($l_no_group_deleted);
  }
  $display["detail"] = dis_group_consult($group, $uid);

} else if ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $pref_q = run_query_display_pref($uid, "group", 1);
  $pref_u_q = run_query_display_pref($uid, "group_user", 1);
  $display["detail"] = dis_group_display_pref($pref_q, $pref_u_q);

} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update($entity, $fieldname, $disstatus);
  $pref_q = run_query_display_pref($uid, "group", 1);
  $pref_u_q = run_query_display_pref($uid, "group_user", 1);
  $display["detail"] = dis_group_display_pref($pref_q, $pref_u_q);

} else if($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update($entity, $new_level, $fieldorder);
  $pref_q = run_query_display_pref($uid, "group", 1);
  $pref_u_q = run_query_display_pref($uid, "group_user", 1);
  $display["detail"] = dis_group_display_pref($pref_q, $pref_u_q);

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
} else if ($action == "ext_get_ids") {
  $display["search"] = html_group_search_form($group);
  if ($set_display == "yes") {
    $display["result"] = dis_group_search_group($group);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
// Update actions url in case some values have been updated (id after insert) 
update_group_action_url();
$display["head"] = display_head($l_group);
if (! $group["popup"]) {
  $display["header"] = generate_menu($module,$section);
}
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $group hash, Group parameters transmited
// returns : $group hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_group() {
  global $param_group, $popup, $child_res;
  global $new_order, $order_dir, $entity;
  global $tf_name, $tf_desc, $tf_user, $tf_email, $cb_vis, $cb_priv, $sel_privacy;
  global $ext_action, $ext_url, $ext_id, $ext_target, $ext_title, $ext_widget;
  global $ext_element;
  global $HTTP_POST_VARS, $HTTP_GET_VARS;

  // Group fields
  if (isset ($param_group)) $group["id"] = $param_group;
  if (isset ($tf_name)) $group["name"] = trim($tf_name);
  if (isset ($tf_desc)) $group["desc"] = trim($tf_desc);
  if (isset ($tf_email)) $group["email"] = $tf_email;
  if (isset ($tf_user)) $group["user"] = trim($tf_user);
  if (isset ($cb_priv)) $group["priv"] = ($cb_priv == 1 ? 1 : 0);
  if (isset ($sel_privacy)) $group["privacy"] = $sel_privacy;

  if (isset ($child_res)) $group["children_restriction"] = $child_res;

  if (isset ($new_order)) $group["new_order"] = $new_order;
  if (isset ($order_dir)) $group["order_dir"] = $order_dir;
  if (isset ($entity)) $group["entity"] = $entity;

  // External param
  if (isset ($popup)) $group["popup"] = $popup;
  if (isset ($ext_action)) $group["ext_action"] = $ext_action;
  if (isset ($ext_title)) $group["ext_title"] = $ext_title;
  if (isset ($ext_target)) $group["ext_target"] = $ext_target;
  if (isset ($ext_url)) $group["ext_url"] = $ext_url;
  if (isset ($ext_id)) $group["ext_id"] = $ext_id;
  if (isset ($ext_id)) $group["id"] = $ext_id;
  if (isset ($ext_element)) $group["ext_element"] = $ext_element;  
  if (isset ($ext_widget)) $group["ext_widget"] = $ext_widget;

  if ((is_array ($HTTP_POST_VARS)) && (count($HTTP_POST_VARS) > 0)) {
    $http_obm_vars = $HTTP_POST_VARS;
  } elseif ((is_array ($HTTP_GET_VARS)) && (count($HTTP_GET_VARS) > 0)) {
    $http_obm_vars = $HTTP_GET_VARS;
  }

  if (isset ($http_obm_vars)) {
    $nb_u = 0;
    $nb_group = 0;
    while ( list( $key ) = each( $http_obm_vars ) ) {
      if (strcmp(substr($key, 0, 4),"cb_u") == 0) {
	$nb_u++;
        $u_num = substr($key, 4);
        $group["user$nb_u"] = $u_num;
      } elseif (strcmp(substr($key, 0, 4),"cb_g") == 0) {
	$nb_group++;
        $group_num = substr($key, 4);
        $group["group_$nb_group"] = $group_num;
      }
    }
    $group["user_nb"] = $nb_u;
    $group["group_nb"] = $nb_group;
  }

  display_debug_param($group);

  return $group;
}


///////////////////////////////////////////////////////////////////////////////
// Group Action 
///////////////////////////////////////////////////////////////////////////////
function get_group_action() {
  global $group, $actions, $path, $l_group;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display,$l_header_admin;
  global $l_header_add_user, $l_add_user, $l_header_add_group, $l_add_group;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

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

// New
  $actions["group"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/group/group_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('search','index','admin','detailconsult','display', 'user_add', 'user_del', 'group_add', 'group_del', 'insert', 'update', 'delete')
                                  );

// Detail Consult
  $actions["group"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/group/group_index.php?action=detailconsult&amp;param_group=".$group["id"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate', 'update')
                                  );

// Detail Update
  $actions["group"]["detailupdate"] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/group/group_index.php?action=detailupdate&amp;param_group=".$group["id"]."",
     'Right'    => $cright_write,
     'Condition'=> array ('detailconsult', 'user_add', 'user_del', 'group_add', 'group_del', 'insert', 'update') 
                                     	   );

// Insert
  $actions["group"]["insert"] = array (
    'Url'      => "$path/group/group_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Update
  $actions["group"]["update"] = array (
    'Url'      => "$path/group/group_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Check Delete
  $actions["group"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/group/group_index.php?action=check_delete&amp;param_group=".$group["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'user_add', 'user_del', 'group_add', 'group_del', 'insert', 'update')
                                     	   );

// Delete
  $actions["group"]["delete"] = array (
    'Url'      => "$path/group/group_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Ext get Ids : external Group selection
  $actions["group"]["ext_get_ids"] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                    	  );

// sel group add : Groups selection
  $actions["group"]["sel_group_add"] = array (
    'Name'     => $l_header_add_group,
    'Url'      => "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_group)."&amp;ext_action=group_add&amp;ext_url=".urlencode($path."/group/group_index.php")."&amp;ext_id=".$group["id"]."&amp;ext_target=$l_group&amp;child_res=1",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Target'   => $l_group,
    'Condition'=> array ('detailconsult','user_add','user_del', 'group_add','group_del', 'insert', 'update') 
                                    	  );

// Sel user add : Users selection
  $actions["group"]["sel_user_add"] = array (
    'Name'     => $l_header_add_user,
    'Url'      => "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_user)."&amp;ext_action=user_add&amp;ext_url=".urlencode($path."/group/group_index.php")."&amp;ext_id=".$group["id"]."&amp;ext_target=$l_group",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Target'   => $l_group,
    'Condition'=> array ('detailconsult','user_add','user_del', 'group_add','group_del', 'insert', 'update') 
                                    	  );

// User add
  $actions["group"]["user_add"] = array (
    'Url'      => "$path/group/group_index.php?action=user_add",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     );

// User del
  $actions["group"]["user_del"] = array (
    'Url'      => "$path/group/group_index.php?action=user_del",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Group add
  $actions["group"]["group_add"] = array (
    'Url'      => "$path/group/group_index.php?action=group_add",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Group del
  $actions["group"]["group_del"] = array (
    'Url'      => "$path/group/group_index.php?action=group_del",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Admin
  $actions["group"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/group/group_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                    );

// Dispay
  $actions["group"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/group/group_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );

}


///////////////////////////////////////////////////////////////////////////////
// Group Actions URL updates (after processing, before displaying menu)  
///////////////////////////////////////////////////////////////////////////////
function update_group_action_url() {
  global $group, $actions, $path, $l_add_user, $l_add_group, $l_group;

  if ($group["id"] > 0) {
    // Detail Update
    $actions["group"]["detailupdate"]['Url'] = "$path/group/group_index.php?action=detailupdate&amp;param_group=".$group["id"];
    // Check Delete
    $actions["group"]["check_delete"]['Url'] = "$path/group/group_index.php?action=check_delete&amp;param_group=".$group["id"];
    // Sel User add
    $actions["group"]["sel_user_add"]['Url'] = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_user)."&amp;ext_action=user_add&amp;ext_url=".urlencode($path."/group/group_index.php")."&amp;ext_id=".$group["id"]."&amp;ext_target=$l_group";
    // Sel group add : Groups selection
  $actions["group"]["sel_group_add"]['Url'] = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_group)."&amp;ext_action=group_add&amp;ext_url=".urlencode($path."/group/group_index.php")."&amp;ext_id=".$group["id"]."&amp;ext_target=$l_group&amp;child_res=1";
  } else {
    $actions["group"]["detailupdate"]['Condition'] = array('None');
  }


}

</script>
