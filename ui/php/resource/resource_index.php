<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : resource_index.php                                           //
//     - Desc :  Resource Index File                                         //
// 2005-08-13 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields      -- show the resource search form
// - search          -- search fields      -- show the result set of search
// - new             --                    -- show the new resource form
// - detailconsult   -- $param_resource    -- show the resource detail
// - detailupdate    -- $param_resource    -- show the resource detail form
// - insert          -- form fields        -- insert the resource 
// - reset           -- $param_resource    -- reset resource preferences
// - update          -- form fields        -- update the resource 
// - check_delete    -- $param_resource    -- check links before delete
// - delete          -- $param_resource    -- delete the resource 
// - rights_admin    -- access rights screen
// - rights_update   -- Update resource agenda access rights
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position
// External API ---------------------------------------------------------------
// - ext_get_ids     --                -- select multiple resources (ret id) 
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$extra_css = "resource.css";
$module = "resource";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("resource_display.inc");
require("resource_query.inc");
require("resource_js.inc");
require("$obminclude/lib/right.inc");

$uid = $auth->auth["uid"];

if ($action == "") $action = "index";
$resource = get_param_resource();
get_resource_action();
$perm->check_permissions($module, $action);
if (! check_privacy($module, "Resource", $action, $resource["id"], $uid)) {
  $display["msg"] = display_err_msg($l_error_visibility);
  $action = "index";
} else {
  update_last_visit("resource", $resource["id"], $action);
}
page_close();


///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_ids") {
  $display["search"] = html_resource_search_form($resource);
  if ($set_display == "yes") {
    $display["result"] = dis_resource_search_list($resource);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_resource_search_form($resource);
  if ($set_display == "yes") {
    $display["result"] = dis_resource_search_list($resource);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_resource_search_form($resource);
  $display["result"] = dis_resource_search_list($resource);

} elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_resource_form("",$resource);

} elseif ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_resource_consult($resource);

} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_detail($resource["id"]);
  if ($obm_q->num_rows() == 1) {
    $display["detailInfo"] = display_record_info($obm_q);
    $display["detail"] = html_resource_form($obm_q, $resource);
  } else {
    $display["msg"] .= display_err_msg($l_query_error . " - " . $query . " !");
  }

} elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form("", $resource)) {
    // If the context (same resource) was confirmed ok, we proceed
    $rid = run_query_insert($resource);
    if ($rid > 0) {
      $resource["id"] = $rid;
      $display["msg"] .= display_ok_msg($l_insert_ok);
      $display["detail"] = dis_resource_consult($resource);
    } else {
      $display["msg"] .= display_err_msg($l_insert_error);
      $display["search"] = html_resource_search_form($resource);
    }
  // Form data are not valid
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = html_resource_form("", $resource);
  }

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form($resource["id"], $resource)) {
    $retour = run_query_update($resource["id"], $resource);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok);
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
    }
    $display["detail"] = dis_resource_consult($resource);
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    $display["detail"] = html_resource_form("", $resource);
  }

} elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_resource($resource["id"])) {
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_can_delete_resource($resource["id"]);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_resource_consult($resource);
  }

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_resource($resource["id"])) {
    $retour = run_query_delete($resource["id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_delete_ok);
    } else {
      $display["msg"] .= display_err_msg($l_delete_error);
    }
    $display["search"] = html_resource_search_form($resource);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_resource_consult($resource);
  }

} elseif ($action == "rights_admin") {
///////////////////////////////////////////////////////////////////////////////
  $display["title"] = "<div class=\"title\">$l_resource</div>";
  $display["detail"] = of_right_dis_admin("resource", $resource["entity_id"]);

} elseif ($action == "rights_update") {
///////////////////////////////////////////////////////////////////////////////
  $display["title"] = "<div class=\"title\">$l_resource</div>";
  of_right_update_right($resource, "resource");
  $display["msg"] .= display_ok_msg($l_right_update_ok);
  $display["msg"] .= display_warn_msg($err_msg);
  $display["detail"] = of_right_dis_admin("resource", $resource["entity_id"]);

}  elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($auth->auth["uid"], "resource", 1);
  $display["detail"] = dis_resource_display_pref($prefs);

} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($auth->auth["uid"], "resource", 1);
  $display["detail"] = dis_resource_display_pref($prefs);

} else if ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($auth->auth["uid"], "resource", 1);
  $display["detail"] = dis_resource_display_pref($prefs);
}

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_resource);
if (! $resource["popup"]) {
  update_resource_action();
  $display["header"] = generate_menu($module,$section);
}
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Resource parameters transmited in $resource hash
// returns : $resource hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_resource() {
  global $param_resource, $param_entity;
  global $tf_name, $tf_desc, $ta_desc, $tf_qty, $tf_qtyinf, $tf_qtysup;
  global $tf_datebegin, $tf_lastname, $tf_firstname, $cb_archive, $restriction;
  global $param_ext, $ext_action, $ext_url, $ext_id, $ext_title, $ext_target;
  global $ext_widget,$ext_element;
  global $popup, $HTTP_POST_VARS, $HTTP_GET_VARS;
  global $cb_read_public, $cb_write_public,$sel_accept_write,$sel_accept_read,$param_entity;

  if (isset ($param_entity)) $resource["id"] = $param_entity;
  if (isset ($param_ext)) $resource["id"] = $param_ext;
  if (isset ($param_resource)) $resource["id"] = $param_resource;
  if (isset ($tf_name)) $resource["name"] = $tf_name;
  if (isset ($tf_desc)) $resource["desc"] = $tf_desc;
  if (isset ($ta_desc)) $resource["desc"] = $ta_desc;
  if (isset ($tf_qty)) $resource["qty"] = $tf_qty;
  if (isset ($tf_qtyinf)) $resource["qtyinf"] = $tf_qtyinf;
  if (isset ($tf_qtysup)) $resource["qtysup"] = $tf_qtysup;

  if (isset ($restriction)) $resource["restriction"] = $restriction;

  // External param
  if (isset ($popup)) $resource["popup"] = $popup;
  if (isset ($ext_title)) $resource["ext_title"] = $ext_title;
  if (isset ($ext_action)) $resource["ext_action"] = $ext_action;
  if (isset ($ext_url)) $resource["ext_url"] = $ext_url;
  if (isset ($ext_id)) $resource["ext_id"] = $ext_id;
  if (isset ($ext_element)) $resource["ext_element"] = $ext_element;
  if (isset ($ext_target)) $resource["ext_target"] = $ext_target;
  if (isset ($ext_widget)) $resource["ext_widget"] = $ext_widget;
  if ((is_array ($HTTP_POST_VARS)) && (count($HTTP_POST_VARS) > 0)) {
    $http_obm_vars = $HTTP_POST_VARS;
  } elseif ((is_array ($HTTP_GET_VARS)) && (count($HTTP_GET_VARS) > 0)) {
    $http_obm_vars = $HTTP_GET_VARS;
  }

  // Rights parameters
  if (isset($param_entity)) $resource["entity_id"] = $param_entity;
  if (is_array($sel_accept_write)) $resource["accept_w"] = $sel_accept_write;
  if (is_array($sel_accept_read)) $resource["accept_r"] = $sel_accept_read;
  if (isset($cb_write_public)) $resource["public_w"] = $cb_write_public;
  if (isset($cb_read_public)) $resource["public_r"] = $cb_read_public;

  display_debug_param($resource);

  return $resource;
}


///////////////////////////////////////////////////////////////////////////////
// User Action 
///////////////////////////////////////////////////////////////////////////////
function get_resource_action() {
  global $resource, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display,$l_header_admin,$l_header_reset,$l_header_right;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["resource"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/resource/resource_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

// Get Ids
  $actions["resource"]["ext_get_ids"] = array (
    'Url'      => "$path/resource/resource_index.php?action=ext_get_ids",
    'Right'    => $cright_read,
    'Condition'=> array ('none'),
    'popup' => 1
                                    );

// New
  $actions["resource"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/resource/resource_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('search','index','insert','update','admin','detailconsult','reset','display','rights_admin','rights_update') 
                                  );

// Search
  $actions["resource"]["search"] = array (
    'Url'      => "$path/resource/resource_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                  );
  
  // Get resource id from external window (js)
  $actions["resource"]["getsearch"] = array (
    'Url'      => "$path/resource/resource_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                  );
// Detail Consult
  $actions["resource"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/resource/resource_index.php?action=detailconsult&amp;param_resource=".$resource["id"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'rights_admin', 'rights_update') 
                                  );
// Detail Update
  $actions["resource"]["detailupdate"] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/resource/resource_index.php?action=detailupdate&amp;param_resource=".$resource["id"]."",
     'Right'    => $cright_write,
     'Condition'=> array ('detailconsult', 'insert', 'update')
                                     	   );

// Insert
  $actions["resource"]["insert"] = array (
    'Url'      => "$path/resource/resource_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Update
  $actions["resource"]["update"] = array (
    'Url'      => "$path/resource/resource_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Check Delete
  $actions["resource"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/resource/resource_index.php?action=check_delete&amp;param_resource=".$resource["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'insert')
                                     	      );

// Delete
  $actions["resource"]["delete"] = array (
    'Url'      => "$path/resource/resource_index.php?action=delete&amp;param_resource=".$resource["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Rights Admin.
  $actions["resource"]["rights_admin"] = array (
    'Name'     => $l_header_right,
    'Url'      => "$path/resource/resource_index.php?action=rights_admin&amp;param_entity=".$resource["id"]."",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult')
                                     );

// Rights Update
  $actions["resource"]["rights_update"] = array (
    'Url'      => "$path/resource/resource_index.php?action=rights_update&amp;param_entity=".$resource["id"]."",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                     );

// Admin
  $actions["resource"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/resource/resource_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                     );

// Display
  $actions["resource"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/resource/resource_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                         );
// Display
  $actions["resource"]["dispref_display"] = array (
    'Url'      => "$path/resource/resource_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                         );
// Display
  $actions["resource"]["dispref_level"] = array (
    'Url'      => "$path/resource/resource_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                         );

}


///////////////////////////////////////////////////////////////////////////////
// Resource Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_resource_action() {
  global $resource, $actions, $path;

  // Detail Consult
  $actions["resource"]["detailconsult"]["Url"] = "$path/resource/resource_index.php?action=detailconsult&amp;param_resource=".$resource["id"];

  // Detail Update
  $actions["resource"]["detailupdate"]["Url"] = "$path/resource/resource_index.php?action=detailupdate&amp;param_resource=".$resource["id"];

  // Check Delete
  $actions["resource"]["check_delete"]["Url"] = "$path/resource/resource_index.php?action=check_delete&amp;param_resource=".$resource["id"];

}

</script>
