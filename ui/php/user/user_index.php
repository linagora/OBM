<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : user_index.php                                               //
//     - Desc : User Index File                                              //
// 2000-01-13 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the user search form
// - search          -- search fields  -- show the result set of search
// - new             --                -- show the new user form
// - detailconsult   -- $user_id       -- show the user detail
// - detailupdate    -- $user_id       -- show the user detail form
// - insert          -- form fields    -- insert the user
// - reset           -- $user_id       -- reset user preferences
// - update          -- form fields    -- update the user
// - check_delete    -- $user_id       -- check links before delete
// - delete          -- $user_id    -- delete the user
// - group_consult   -- $user_id    -- show the user groups form
// - group_update    -- $user_id    -- update the user groups
// External API ---------------------------------------------------------------
// - ext_get_ids     --                -- select multiple users (return id) 
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "user";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_user_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require_once("$obminclude/of/of_category.inc");
require("user_display.inc");
require("user_query.inc");
require("user_js.inc");
require("$obminclude/of/of_right.inc"); // needed by call from calendar

// detailconsult can be accessed without user_id (-> display current user)
if (($action == "detailconsult") && (! $params["user_id"])) $params["user_id"] = $obm["uid"];

get_user_action();
$perm->check_permissions($module, $action);

update_last_visit("user", $params["user_id"], $action);

page_close();


///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_ids") {
  $display["search"] = html_user_search_form($params);
  if ($_SESSION['set_display'] == "yes") {
    $display["result"] = dis_user_search_list($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "ext_get_id") {
  $display["search"] = html_user_search_form($params);
  if ($_SESSION['set_display'] == "yes") {
    $display["result"] = dis_user_search_list($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_user_search_form($params);
  if ($_SESSION['set_display'] == "yes") {
    $display["result"] = dis_user_search_list($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_user_search_form($params);
  $display["result"] = dis_user_search_list($params);

} elseif ($action == "ext_search") {
///////////////////////////////////////////////////////////////////////////////
  $user_q = run_query_user_ext_search($params);
  json_search_users($params, $user_q);
  echo "(".$display['json'].")";
  exit();
} elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_user_form("",$params);

} elseif ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_user_consult($params);

} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_update_rights($params)) {
    $obm_q = run_query_user_detail($params["user_id"]);
    if ($obm_q->num_rows() == 1) {
      $display["detailInfo"] = display_record_info($obm_q);
      $display["detail"] = html_user_form($obm_q, $params);
    } else {
      $display["msg"] .= display_err_msg($l_err_reference);
    }
  } else {
    $display["msg"] .= display_warn_msg($err['msg']);
  }

} elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_user_data_form("", $params)) {

    // If the context (same user) was confirmed ok, we proceed
    if ($params["confirm"] == $c_yes) {
      $cid = run_query_user_insert($params);
      if ($cid > 0) {
	$params["user_id"] = $cid;
	set_update_state();
        $display["msg"] .= display_ok_msg("$l_user : $l_insert_ok");
	$display["detail"] = dis_user_consult($params);
      } else {
	$display["msg"] .= display_err_msg("$l_user : $l_insert_error");
	$display["detail"] = html_user_form("", $params);
      }

    // If it is the first try, we warn the user if some user seem similar
    } else {
      $obm_q = check_user_context("", $params);
      if ($obm_q->num_rows() > 0) {
        $display["detail"] = dis_user_warn_insert("", $obm_q, $params);
      } else {
        $cid = run_query_user_insert($params);
        if ($cid > 0) {
	  set_update_state();
	  $params["user_id"] = $cid;
          $display["msg"] .= display_ok_msg("$l_user : $l_insert_ok");
	  $display["detail"] = dis_user_consult($params);
        } else {
          $display["msg"] .= display_err_msg("$l_user : $l_insert_error");
	  $display["detail"] = html_user_form("",$params);
        }
      }
    }

  // Form data are not valid
  } else {
    $display["msg"] .= display_err_msg($l_invalid_data . " : " . $err["msg"]);
    $display["detail"] = html_user_form("", $params, $err["field"]);
  }

} elseif ($action == "reset") {
///////////////////////////////////////////////////////////////////////////////
  reset_preferences_to_default($params["user_id"]);
  session_load_user_prefs();
  $display["msg"] .= display_ok_msg($l_reset_ok);
  $display["detail"] = dis_user_consult($params);

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_user_data_form($params["user_id"], $params)) {
    $retour = run_query_user_update($params["user_id"], $params);
    if ($retour) {
      set_update_state();
      $display["msg"] .= display_ok_msg("$l_user : $l_update_ok");
      $display["detail"] = dis_user_consult($params);
    } else {
      $display["msg"] .= display_err_msg("$l_user : $l_update_error");
      $display["detail"] = html_user_form("", $params, $err["field"]);
    }
  } else {
    $display["msg"] .= display_err_msg($err["msg"]);
    $display["detail"] = html_user_form("", $params, $err["field"]);
  }

} elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_can_delete($params)) {
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_user_can_delete($params["user_id"]);
  } else {
    $display["msg"] .= display_warn_msg($err["msg"], false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_user_consult($params);
  }

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_can_delete($params)) {
    run_query_user_delete_profile($params["user_id"]);
    $retour = run_query_user_delete($params["user_id"]);
    if ($retour) {
      set_update_state();
      $display["msg"] .= display_ok_msg("$l_user : $l_delete_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_user : $l_delete_error");
    }
    $display["search"] = html_user_search_form($params);
  } else {
    $display["msg"] .= display_warn_msg($err['msg'], false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_user_consult($params);
  }

} elseif ($action == "group_consult") {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_user_detail($params["user_id"]);
  if ($obm_q->num_rows() == 1) {
    $display["detail"] = html_user_group_consult($obm_q);
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }

} elseif ($action == "group_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_user_update_user_group($params);
  if ($retour >= 0) {
    set_update_state();
    $display["msg"] .= display_ok_msg("$l_user : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_user : $l_update_error");
  }
  $display["detail"] = dis_user_consult($params);

} elseif ($action == 'import') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_user_import_index();

} elseif ($action == 'import_file') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_user_import_file_run($params);

} elseif ($action == 'admin') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_user_admin_index();

} elseif ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'], 'user', 1);
  $display['detail'] = dis_user_display_pref($prefs);

} else if ($action == 'dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'user', 1);
  $display['detail'] = dis_user_display_pref($prefs);

} else if ($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'user', 1);
  $display['detail'] = dis_user_display_pref($prefs);
}

of_category_user_action_switch($module, $action, $params);

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_user);
if (! $params['popup']) {
  update_user_action();
  $display['header'] = display_menu($module);
}
$display['end'] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores User parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_user_params() {
  
  // Get global params
  $params = get_global_params('UserObm');

  if (isset($params)) {
    $nb_group = 0;
    while ( list( $key ) = each($params) ) {
      if (strcmp(substr($key, 0, 7),'data-g-') == 0) {
        $nb_group++;
        $group_num = substr($key, 7);
        $params["group_$nb_group"] = $group_num;
      }
    }
    $params['group_nb'] = $nb_group;
  }
  
  if (isset ($_FILES['fi_file'])) {
    $params['file_tmp'] = $_FILES['fi_file']['tmp_name'];
    $params['file_name'] = $_FILES['fi_file']['name'];
    $params['size'] = $_FILES['fi_file']['size'];
    $params['type'] = $_FILES['fi_file']['type'];
  }

  if(is_array($params['email'])) {
    $email_aliases = array();
    while(!empty($params['email'])) {
      $email = trim(array_shift($params['email']));
      $domain = array_shift($params['aliases']);
      if(!empty($email)) {
       if(!empty($domain)) {
          $email_aliases[] = $email.'@'.$domain;
        } else {
          $email_aliases[] = $email;
        }
      }
    }

    $params['email'] = implode("\r\n", $email_aliases);
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// User Action 
///////////////////////////////////////////////////////////////////////////////
function get_user_action() {
  global $params, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display,$l_header_admin,$l_header_import;
  global $l_header_upd_group,$l_header_admin, $l_header_reset;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;
  
  of_category_user_module_action('user');

// Index
  $actions['user']['index'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/user/user_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    );

// Get Ids
  $actions['user']['ext_get_ids'] = array (
    'Url'      => "$path/user/user_index.php?action=ext_get_ids",
    'Right'    => $cright_read,
    'Condition'=> array ('none'),
    'popup' => 1
                                    );
                                    
// Get Ids
  $actions['user']['ext_get_id'] = array (
    'Url'      => "$path/user/user_index.php?action=ext_get_id",
    'Right'    => $cright_read,
    'Condition'=> array ('none'),
    'popup' => 1
                                    );

// New
  $actions['user']['new'] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/user/user_index.php?action=new",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('search','index','insert','update','admin','detailconsult','reset','display','dispref_display','dispref_level', 'delete')
                                  );

// Search
  $actions['user']['search'] = array (
    'Url'      => "$path/user/user_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                  );
// Search
  $actions['user']['ext_search'] = array (
    'Url'      => "$path/user/user_index.php?action=ext_search",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
  );  

// Get user id from external window (js)
  $actions['user']['getsearch'] = array (
    'Url'      => "$path/user/user_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                  );
// Detail Consult
  $actions['user']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/user/user_index.php?action=detailconsult&amp;user_id=".$params['user_id'],
    'Right'    => $cright_read_admin,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'group_consult', 'group_update')
                                  );

// Detail Update
  $actions['user']['detailupdate'] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/user/user_index.php?action=detailupdate&amp;user_id=".$params['user_id'],
     'Right'    => $cright_write_admin,
     'Condition'=> array ('detailconsult', 'reset', 'update', 'group_consult', 'group_update')
                                     	   );

// Insert
  $actions['user']['insert'] = array (
    'Url'      => "$path/user/user_index.php?action=insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     );

// Update
  $actions['user']['update'] = array (
    'Url'      => "$path/user/user_index.php?action=update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     );

// Group Consult
  $actions['user']['group_consult'] = array (
    'Name'     => $l_header_upd_group,
    'Url'      => "$path/user/user_index.php?action=group_consult&amp;user_id=".$params['user_id'],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'reset', 'detailupdate', 'update', 'group_update')
                                     );

// Group Update
  $actions['user']['group_update'] = array (
    'Url'      => "$path/user/user_index.php?action=group_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                     );

// Reset
  $actions['user']['reset'] = array (
    'Name'     => $l_header_reset,
    'Url'      => "$path/user/user_index.php?action=reset&amp;user_id=".$params['user_id'],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'group_consult', 'group_update') 
                                    );

// Check Delete
  $actions['user']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/user/user_index.php?action=check_delete&amp;user_id=".$params['user_id'],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'reset', 'group_consult', 'group_update') 
                                     	   );

// Delete
  $actions['user']['delete'] = array (
    'Url'      => "$path/user/user_index.php?action=delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                     );

// Display
  $actions['user']['display'] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/user/user_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                      	 );

// Display
  $actions['user']['dispref_display'] = array (
    'Url'      => "$path/user/user_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                      	 );
// Display
  $actions['user']['dispref_level'] = array (
    'Url'      => "$path/user/user_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                      	 );

// Import
  $actions['user']['import'] = array (
    'Name'     => $l_header_import,
    'Url'      => "$path/user/user_index.php?action=import",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('all')
                                                 );

// Import file
  $actions['user']['import_file'] = array (
    'Url'      => "$path/user/user_index.php?action=import_file",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                                 );

// Admin
  $actions['user']['admin'] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/user/user_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all')
                                                 );

}


///////////////////////////////////////////////////////////////////////////////
// User Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_user_action() {
  global $params, $actions, $path;

  $id = $params['user_id'];
  if ($id > 0) {
    // Detail Consult
    $actions['user']['detailconsult']['Url'] = "$path/user/user_index.php?action=detailconsult&amp;user_id=$id";
    $actions['user']['detailconsult']['Condition'][] = 'insert';

    // Detail Update
    $actions['user']['detailupdate']['Url'] = "$path/user/user_index.php?action=detailupdate&amp;user_id=$id";
    $actions['user']['detailupdate']['Condition'][] = 'insert';

    // Check Delete
    $actions['user']['check_delete']['Url'] = "$path/user/user_index.php?action=check_delete&amp;user_id=$id";
    $actions['user']['check_delete']['Condition'][] = 'insert';

    // Group Consult
    $actions['user']['group_consult']['Url'] = "$path/user/user_index.php?action=group_consult&amp;user_id=$id";
    $actions['user']['group_consult']['Condition'][] = 'insert';
  }
}


?>
