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
// - ext_get_ids     --                -- select multiple groups (return id) 
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$section = "USER";
$menu = "GROUP";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("group_display.inc");
require("group_query.inc");
require("group_js.inc");

if ($action == "") $action = "index";
$group = get_param_group();
get_group_action();
$perm->check_permissions($menu, $action);

$uid = $auth->auth["uid"];

update_last_visit("group", $param_group, $action);

page_close();


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if (! $group["popup"]) {
  $display["header"] = generate_menu($menu,$section);
}

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_ids") {
  $display["search"] = html_group_search_form($group);
  if ($set_display == "yes") {
    $display["result"] = dis_group_search_group($group, $popup);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }
}

elseif ($action == "ext_get_group") {
  $display["search"] = html_get_group_search_form($group);
  if ($set_display == "yes") {
    $display["result"] = dis_get_group_search_group($group, $popup);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }
}

elseif (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_group_search_form($group);
  if ($set_display == "yes") {
    $display["msg"] .= dis_group_search_group("", $popup);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }
}

else if ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_group_search_form($group);
  $display["result"] = dis_group_search_group($group, $popup);
}
else if ($action == "getsearch") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_get_group_search_form($group);
  $display["result"] = dis_get_group_search_group($group, $popup);
}
else if ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_group_form($action, "", $group);

} else if ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $group_q = run_query_detail($group["id"]);
  $pref_u_q = run_query_display_pref($uid, "group_user");
  $u_q = run_query_user_group($group, $entity);
  $pref_g_q = run_query_display_pref($uid, "group_group");
  $g_q = run_query_group_group($group, $entity);
  $display["detail"] = html_group_consult($group_q, $pref_u_q, $u_q, $pref_g_q, $g_q);
}

else if ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_detail($group["id"]);
  $display["detail"] = html_group_form($action, $obm_q, $group);

} else if ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form($group)) {

    // If the context (same group) was confirmed ok, we proceed
    if ($hd_confirm == $c_yes) {
      $retour = run_query_insert($group);
      if ($retour) {
	$display["msg"] .= display_ok_msg($l_insert_ok);
      } else {
	$display["msg"] .= display_err_msg($l_insert_error);
      }
      $display["search"] = html_group_search_form($group);
      
      // If it is the first try, we warn the user if some groups seem similar
    } else {
      $obm_q = check_group_context("", $group);
      if ($obm_q->num_rows() > 0) {
	$display["detail"] = dis_group_warn_insert("", $obm_q, $group);
      } else {
	$retour = run_query_insert($group);
	if ($retour) {
	  $display["msg"] .= display_ok_msg($l_insert_ok);
	} else {
	  $display["msg"] .= display_err_msg($l_insert_error);
	}
	$display["search"] = html_group_search_form($group);
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
    $group_q = run_query_detail($group["id"]);
    $pref_u_q = run_query_display_pref($uid, "group_user");
    $u_q = run_query_user_group($group);
    $pref_g_q = run_query_display_pref($uid, "group_group");
    $g_q = run_query_group_group($group);
    $display["detail"] = html_group_consult($group_q, $pref_u_q, $u_q, $pref_g_q, $g_q);
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
  $group_q = run_query_detail($group["id"]);
  $pref_u_q = run_query_display_pref($uid, "group_user");
  $u_q = run_query_user_group($group);
  $pref_g_q = run_query_display_pref($uid, "group_group");
  $g_q = run_query_group_group($group);
  $display["detail"] = html_group_consult($group_q, $pref_u_q, $u_q, $pref_g_q, $g_q);

} elseif ($action == "user_del")  {
///////////////////////////////////////////////////////////////////////////////
  if ($group["user_nb"] > 0) {
    $nb = run_query_usergroup_delete($group);
    $display["msg"] .= display_ok_msg("$nb $l_user_removed");
  } else {
    $display["msg"] .= display_err_msg($l_no_user_deleted);
  }
  $group_q = run_query_detail($group["id"]);
  $pref_u_q = run_query_display_pref($uid, "group_user");
  $u_q = run_query_user_group($group);
  $pref_g_q = run_query_display_pref($uid, "group_group");
  $g_q = run_query_group_group($group);
  $display["detail"] = html_group_consult($group_q, $pref_u_q, $u_q, $pref_g_q, $g_q);

} elseif ($action == "group_add")  {
///////////////////////////////////////////////////////////////////////////////
  if ($group["group_nb"] > 0) {
    $nb = run_query_groupgroup_insert($group);
    $display["msg"] .= display_ok_msg("$nb $l_group_added");
  } else {
    $display["msg"] .= display_err_msg($l_no_group_added);
  }
  $group_q = run_query_detail($group["id"]);
  $pref_u_q = run_query_display_pref($uid, "group_user");
  $u_q = run_query_user_group($group);
  $pref_g_q = run_query_display_pref($uid, "group_group");
  $g_q = run_query_group_group($group);
  $display["detail"] = html_group_consult($group_q, $pref_u_q, $u_q, $pref_g_q, $g_q);

} elseif ($action == "group_del")  {
///////////////////////////////////////////////////////////////////////////////
  if ($group["group_nb"] > 0) {
    $nb = run_query_groupgroup_delete($group);
    $display["msg"] .= display_ok_msg("$nb $l_group_removed");
  } else {
    $display["msg"] .= display_err_msg($l_no_group_deleted);
  }
  $group_q = run_query_detail($group["id"]);
  $pref_u_q = run_query_display_pref($uid, "group_user");
  $u_q = run_query_user_group($group);
  $pref_g_q = run_query_display_pref($uid, "group_group");
  $g_q = run_query_group_group($group);
  $display["detail"] = html_group_consult($group_q, $pref_u_q, $u_q, $pref_g_q, $g_q);

} else if ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $pref_q = run_query_display_pref($uid, "group", 1);
  $pref_u_q = run_query_display_pref($uid, "group_user", 1);
  $display["detail"] = dis_group_display_pref($pref_q, $pref_u_q);
}

else if($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update($entity, $fieldname, $display);
  $pref_q = run_query_display_pref($uid, "group", 1);
  $pref_u_q = run_query_display_pref($uid, "group_user", 1);
  $display["detail"] = dis_group_display_pref($pref_q, $pref_u_q);
}

else if($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update($entity, $new_level, $fieldorder);
  $pref_q = run_query_display_pref($uid, "group", 1);
  $pref_u_q = run_query_display_pref($uid, "group_user", 1);
  $display["detail"] = dis_group_display_pref($pref_q, $pref_u_q);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_group);
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $group hash, Group parameters transmited
// returns : $group hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_group() {
  global $param_group, $cdg_param, $popup, $ext_title;
  global $new_order, $order_dir, $entity;
  global $tf_name, $tf_desc, $tf_user, $tf_email, $cb_vis;
  global $action, $ext_action, $ext_url, $ext_id, $ext_target;
  global $HTTP_POST_VARS, $HTTP_GET_VARS;

  // Group fields
  if (isset ($param_group)) $group["id"] = $param_group;
  if (isset ($tf_name)) $group["name"] = trim($tf_name);
  if (isset ($tf_desc)) $group["desc"] = trim($tf_desc);
  if (isset ($tf_email)) $group["email"] = $tf_email;
  if (isset ($tf_user)) $group["user"] = trim($tf_user);

  if (isset ($new_order)) $group["new_order"] = $new_order;
  if (isset ($order_dir)) $group["order_dir"] = $order_dir;
  if (isset ($entity)) $group["entity"] = $entity;

  // External param
  if (isset ($popup)) $group["popup"] = $popup;
  if (isset ($ext_title)) $group["ext_title"] = $ext_title;
  if (isset ($ext_action)) $group["ext_action"] = $ext_action;
  if (isset ($ext_url)) $group["ext_url"] = $ext_url;
  if (isset ($ext_id)) $group["ext_id"] = $ext_id;
  if (isset ($ext_id)) $group["id"] = $ext_id;
  if (isset ($ext_target)) $group["ext_target"] = $ext_target;

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

  if (debug_level_isset($cdg_param)) {
    echo "action=$action";
    if ( $group ) {
      while ( list( $key, $val ) = each( $group ) ) {
        echo "<br>group[$key]=$val";
      }
    }
  }

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
  $actions["GROUP"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/group/group_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

// Search
  $actions["GROUP"]["search"] = array (
    'Url'      => "$path/group/group_index.php?action=new",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                  );

// New
  $actions["GROUP"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/group/group_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('search','index','admin','detailconsult','display', 'user_add', 'user_del', 'group_add', 'group_del')
                                  );

// Detail Consult
  $actions["GROUP"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/group/group_index.php?action=detailconsult&amp;param_group=".$group["id"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate') 
                                  );

// Detail Update
  $actions["GROUP"]["detailupdate"] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/group/group_index.php?action=detailupdate&amp;param_group=".$group["id"]."",
     'Right'    => $cright_write,
     'Condition'=> array ('detailconsult', 'user_add', 'user_del', 'group_add', 'group_del') 
                                     	   );

// Insert
  $actions["GROUP"]["insert"] = array (
    'Url'      => "$path/group/group_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Update
  $actions["GROUP"]["update"] = array (
    'Url'      => "$path/group/group_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Check Delete
  $actions["GROUP"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/group/group_index.php?action=check_delete&amp;param_group=".$group["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'user_add', 'user_del', 'group_add', 'group_del') 
                                     	   );

// Delete
  $actions["GROUP"]["delete"] = array (
    'Url'      => "$path/group/group_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Ext get Ids : external Group selection
  $actions["GROUP"]["ext_get_ids"] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                    	  );

// sel group add : Groups selection
  $actions["GROUP"]["sel_group_add"] = array (
    'Name'     => $l_header_add_group,
    'Url'      => "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_group)."&amp;ext_action=group_add&amp;ext_url=".urlencode($path."/group/group_index.php")."&amp;ext_id=".$group["id"]."&amp;ext_target=$l_group",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Target'   => $l_group,
    'Condition'=> array ('detailconsult','user_add','user_del', 'group_add','group_del') 
                                    	  );

// Sel user add : Users selection
  $actions["GROUP"]["sel_user_add"] = array (
    'Name'     => $l_header_add_user,
    'Url'      => "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_user)."&amp;ext_action=user_add&amp;ext_url=".urlencode($path."/group/group_index.php")."&amp;ext_id=".$group["id"]."&amp;ext_target=$l_group",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Target'   => $l_group,
    'Condition'=> array ('detailconsult','user_add','user_del', 'group_add','group_del') 
                                    	  );

// User add
  $actions["GROUP"]["user_add"] = array (
    'Url'      => "$path/group/group_index.php?action=user_add",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     );

// User del
  $actions["GROUP"]["user_del"] = array (
    'Url'      => "$path/group/group_index.php?action=user_del",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Group add
  $actions["GROUP"]["group_add"] = array (
    'Url'      => "$path/group/group_index.php?action=group_add",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Group del
  $actions["GROUP"]["group_del"] = array (
    'Url'      => "$path/group/group_index.php?action=group_del",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Admin
  $actions["GROUP"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/group/group_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                    );

// Dispay
  $actions["GROUP"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/group/group_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );

}

</script>
