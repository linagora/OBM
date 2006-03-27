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
// - detailconsult   -- $param_user    -- show the user detail
// - detailupdate    -- $param_user    -- show the user detail form
// - insert          -- form fields    -- insert the user
// - reset           -- $param_user    -- reset user preferences
// - update          -- form fields    -- update the user
// - check_delete    -- $param_user    -- check links before delete
// - delete          -- $param_user    -- delete the user
// - group_consult   -- $param_user    -- show the user groups form
// - group_update    -- $param_user    -- update the user groups
// External API ---------------------------------------------------------------
// - ext_get_ids     --                -- select multiple users (return id) 
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "user";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("user_display.inc");
require("user_query.inc");
require("user_js.inc");
require("$obminclude/lib/right.inc"); // needed by call from calendar
require_once("$obminclude/javascript/calendar_js.inc");

//There is no page_close(). yes, at the end
if ($action == "") $action = "index";
$obm_user = get_param_user();  // $user is used by phplib
get_user_action();
$perm->check_permissions($module, $action);
$uid = $auth->auth["uid"];

update_last_visit("user", $param_user, $action);

page_close();


///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_ids") {
  $display["search"] = html_user_search_form($obm_user);
  if ($set_display == "yes") {
    $display["result"] = dis_user_search_list($obm_user);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_user_search_form($obm_user);
  if ($set_display == "yes") {
    $display["result"] = dis_user_search_list($obm_user);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "search")  {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_user_search_form($obm_user);
  $display["result"] = dis_user_search_list($obm_user);

} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_user_form("",$obm_user);

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_user_consult($obm_user);

} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_user_detail($obm_user["id"]);
  if ($obm_q->num_rows() == 1) {
    $display["detailInfo"] = display_record_info($obm_q);
    $display["detail"] = html_user_form($obm_q, $obm_user);
  } else {
    $display["msg"] .= display_err_msg($l_query_error . " - " . $query . " !");
  }

} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_data_form("", $obm_user)) {

    // If the context (same user) was confirmed ok, we proceed
    if ($hd_confirm == $c_yes) {
      $cid = run_query_user_insert($obm_user);
      if ($cid > 0) {
	$obm_user["id"] = $cid;
        $display["msg"] .= display_ok_msg($l_insert_ok);
	$display["detail"] = dis_user_consult($obm_user);
      } else {
	$display["msg"] .= display_err_msg($l_insert_error);
	$display["detail"] = html_user_form("",$obm_user);
      }

    // If it is the first try, we warn the user if some user seem similar
    } else {
      $obm_q = check_user_context("", $obm_user);
      if ($obm_q->num_rows() > 0) {
        $display["detail"] = dis_user_warn_insert("", $obm_q, $obm_user);
      } else {
        $cid = run_query_user_insert($obm_user);
        if ($cid > 0) {
	  $obm_user["id"] = $cid;
          $display["msg"] .= display_ok_msg($l_insert_ok);
	  $display["detail"] = dis_user_consult($obm_user);
        } else {
          $display["msg"] .= display_err_msg($l_insert_error);
	  $display["detail"] = html_user_form("",$obm_user);
        }
      }
    }

  // Form data are not valid
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = html_user_form("", $obm_user);
  }

} elseif ($action == "reset")  {
///////////////////////////////////////////////////////////////////////////////
  reset_preferences_to_default($obm_user["id"]);
  session_load_user_prefs();
  $display["msg"] .= display_ok_msg($l_reset_ok);
  $display["detail"] = dis_user_consult($obm_user);

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_data_form($obm_user["id"], $obm_user)) {
    $retour = run_query_user_update($obm_user["id"], $obm_user);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok);
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
    }
    $display["detail"] = dis_user_consult($obm_user);
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    $display["detail"] = html_user_form("", $obm_user);
  }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_can_delete($obm_user["id"])) {
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_user_can_delete($obm_user["id"]);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_user_consult($obm_user);
  }

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_can_delete($obm_user["id"])) {
    $retour = run_query_user_delete($obm_user["id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_delete_ok);
    } else {
      $display["msg"] .= display_err_msg($l_delete_error);
    }
    run_query_user_delete_profile($obm_user["id"]);
    $display["search"] = html_user_search_form($obm_user);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_user_consult($obm_user);
  }

} elseif ($action == "group_consult")  {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_user_detail($obm_user["id"]);
  if ($obm_q->num_rows() == 1) {
    $display["detail"] = html_user_group_consult($obm_q);
  } else {
    $display["msg"] .= display_err_msg($l_query_error . " - " . $query . " !");
  }

} elseif ($action == "group_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_user_update_user_group($obm_user);
  if ($retour >= 0) {
    $display["msg"] .= display_ok_msg($l_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_update_error);
  }
  $display["detail"] = dis_user_consult($obm_user);

}  elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($auth->auth["uid"], "user", 1);
  $display["detail"] = dis_user_display_pref($prefs);

} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($auth->auth["uid"], "user", 1);
  $display["detail"] = dis_user_display_pref($prefs);

} else if ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($auth->auth["uid"], "user", 1);
  $display["detail"] = dis_user_display_pref($prefs);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_user);
if (! $obm_user["popup"]) {
  update_user_action();
  $display["header"] = display_menu($module);
}
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores User parameters transmited in $obm_user hash
// returns : $obm_user hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_user() {
  global $param_user, $tf_login, $tf_passwd, $sel_perms, $tf_email, $tf_group;
  global $tf_datebegin, $tf_lastname, $tf_firstname, $cb_archive;
  global $tf_desc, $tf_phone, $tf_phone2, $tf_fax, $tf_fax2;
  global $param_ext, $ext_action, $ext_url, $ext_id, $ext_title, $ext_target;
  global $ext_widget,$ext_element, $restriction_calendar;
  global $popup, $HTTP_POST_VARS, $HTTP_GET_VARS;

  if (isset ($param_ext)) $obm_user["id"] = $param_ext;
  if (isset ($param_user)) $obm_user["id"] = $param_user;
  if (isset ($tf_login)) $obm_user["login"] = $tf_login;
  if (isset ($tf_passwd)) $obm_user["passwd"] = $tf_passwd;
  if (isset ($sel_perms)) $obm_user["perms"] = $sel_perms;
  if (isset ($tf_email)) $obm_user["email"] = $tf_email;
  if (isset ($tf_group)) $obm_user["group"] = $tf_group;
  if (isset ($tf_datebegin)) $obm_user["datebegin"] = $tf_datebegin;
  if (isset ($tf_lastname)) $obm_user["lastname"] = $tf_lastname;
  if (isset ($tf_firstname)) $obm_user["firstname"] = $tf_firstname;
  if (isset ($tf_desc)) $obm_user["description"] = $tf_desc;
  if (isset ($tf_phone)) $obm_user["phone"] = $tf_phone;
  if (isset ($tf_phone2)) $obm_user["phone2"] = $tf_phone2;
  if (isset ($tf_fax)) $obm_user["fax"] = $tf_fax;
  if (isset ($tf_fax2)) $obm_user["fax2"] = $tf_fax2;
  if (isset ($cb_archive)) $obm_user["archive"] = $cb_archive;

  if (isset ($restriction_calendar)) $obm_user["restriction_calendar"] = $restriction_calendar;

  // External param
  if (isset ($popup)) $obm_user["popup"] = $popup;
  if (isset ($ext_title)) $obm_user["ext_title"] = $ext_title;
  if (isset ($ext_action)) $obm_user["ext_action"] = $ext_action;
  if (isset ($ext_url)) $obm_user["ext_url"] = $ext_url;
  if (isset ($ext_id)) $obm_user["ext_id"] = $ext_id;
  if (isset ($ext_element)) $obm_user["ext_element"] = $ext_element;
  if (isset ($ext_target)) $obm_user["ext_target"] = $ext_target;
  if (isset ($ext_widget)) $obm_user["ext_widget"] = $ext_widget;
  if ((is_array ($HTTP_POST_VARS)) && (count($HTTP_POST_VARS) > 0)) {
    $http_obm_vars = $HTTP_POST_VARS;
  } elseif ((is_array ($HTTP_GET_VARS)) && (count($HTTP_GET_VARS) > 0)) {
    $http_obm_vars = $HTTP_GET_VARS;
  }

  if (isset ($http_obm_vars)) {
    $nb_group = 0;
    while ( list( $key ) = each( $http_obm_vars ) ) {
      if (strcmp(substr($key, 0, 4),"cb_g") == 0) {
	$nb_group++;
        $group_num = substr($key, 4);
        $obm_user["group_$nb_group"] = $group_num;
      }
    }
    $obm_user["group_nb"] = $nb_group;
  }

  display_debug_param($obm_user);

  return $obm_user;
}


///////////////////////////////////////////////////////////////////////////////
// User Action 
///////////////////////////////////////////////////////////////////////////////
function get_user_action() {
  global $obm_user, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display,$l_header_admin,$l_header_reset;
  global $l_header_upd_group;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["user"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/user/user_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    );

// Get Ids
  $actions["user"]["ext_get_ids"] = array (
    'Url'      => "$path/user/user_index.php?action=ext_get_ids",
    'Right'    => $cright_read,
    'Condition'=> array ('none'),
    'popup' => 1
                                    );

// New
  $actions["user"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/user/user_index.php?action=new",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('search','index','insert','update','admin','detailconsult','reset','display')
                                  );

// Search
  $actions["user"]["search"] = array (
    'Url'      => "$path/user/user_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                  );
  
// Get user id from external window (js)
  $actions["user"]["getsearch"] = array (
    'Url'      => "$path/user/user_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                  );
// Detail Consult
  $actions["user"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/user/user_index.php?action=detailconsult&amp;param_user=".$obm_user["id"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'group_consult', 'group_update')
                                  );

// Detail Update
  $actions["user"]["detailupdate"] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/user/user_index.php?action=detailupdate&amp;param_user=".$obm_user["id"]."",
     'Right'    => $cright_write_admin,
     'Condition'=> array ('detailconsult', 'reset', 'update', 'group_consult', 'group_update')
                                     	   );

// Insert
  $actions["user"]["insert"] = array (
    'Url'      => "$path/user/user_index.php?action=insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     );

// Update
  $actions["user"]["update"] = array (
    'Url'      => "$path/user/user_index.php?action=update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     );

// Group Consult
  $actions["user"]["group_consult"] = array (
    'Name'     => $l_header_upd_group,
    'Url'      => "$path/user/user_index.php?action=group_consult&amp;param_user=".$obm_user["id"],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'reset', 'detailupdate', 'update', 'group_update')
                                     );

// Group Update
  $actions["user"]["group_update"] = array (
    'Url'      => "$path/user/user_index.php?action=group_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     );

// Reset
  $actions["user"]["reset"] = array (
    'Name'     => $l_header_reset,
    'Url'      => "$path/user/user_index.php?action=reset&amp;param_user=".$obm_user["id"]."",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'group_consult', 'group_update') 
                                    );

// Check Delete
  $actions["user"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/user/user_index.php?action=check_delete&amp;param_user=".$obm_user["id"]."",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'reset', 'group_consult', 'group_update') 
                                     	   );

// Delete
  $actions["user"]["delete"] = array (
    'Url'      => "$path/user/user_index.php?action=delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     );

// Admin
  $actions["user"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/user/user_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                    );

// Display
  $actions["user"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/user/user_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );

// Display
  $actions["user"]["dispref_display"] = array (
    'Url'      => "$path/user/user_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	 );
// Display
  $actions["user"]["dispref_level"] = array (
    'Url'      => "$path/user/user_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	 );

}


///////////////////////////////////////////////////////////////////////////////
// User Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_user_action() {
  global $obm_user, $actions, $path;

  $id = $obm_user["id"];
  if ($id > 0) {
    // Detail Consult
    $actions["user"]["detailconsult"]["Url"] = "$path/user/user_index.php?action=detailconsult&amp;param_user=$id";
    $actions["user"]["detailconsult"]['Condition'][] = 'insert';

    // Detail Update
    $actions["user"]["detailupdate"]['Url'] = "$path/user/user_index.php?action=detailupdate&amp;param_user=$id";
    $actions["user"]["detailupdate"]['Condition'][] = 'insert';

    // Check Delete
    $actions["user"]["check_delete"]['Url'] = "$path/user/user_index.php?action=check_delete&amp;param_user=$id";
    $actions["user"]["check_delete"]['Condition'][] = 'insert';

    // Group Consult
    $actions["user"]["group_consult"]["Url"] = "$path/user/user_index.php?action=group_consult&amp;param_user=$id";
    $actions["user"]["group_consult"]['Condition'][] = 'insert';
  }
}


?>