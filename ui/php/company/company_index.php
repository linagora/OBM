<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : company_index.php                                            //
//     - Desc : Company Index File                                           //
// 1999-03-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions               -- Parameter
// - index (default)     -- search fields  -- show the company search form
// - search              -- search fields  -- show the result set of search
// - new                 --                -- show the new company form
// - detailconsult       -- $company_id -- show the company detail
// - detailupdate        -- $company_id -- show the company detail form
// - insert              -- form fields    -- insert the company
// - update              -- form fields    -- update the company
// - check_delete        -- $company_id -- check links before delete
// - delete              -- $company_id -- delete the company
// - admin               --                -- admin index (kind)
// - type_insert         -- form fields    -- insert the  type
// - type_update         -- form fields    -- update the type 
// - type_checklink      --                -- check if type is used
// - type_delete         -- $sel_type      -- delete the type 
// - activity_insert     -- form fields    -- insert the activity
// - activity_update     -- form fields    -- update the activity
// - activity_checklink  --                -- check if activity is used
// - activity_delete     -- $sel_kind      -- delete the activity
// - nafcode_insert      -- form fields    -- insert the nafcode
// - nafcode_update      -- form fields    -- update the nafcode
// - nafcode_checklink   --                -- check if nafcode is used
// - nafcode_delete      -- $sel_kind      -- delete the nafcode
// - category_insert     -- form fields    -- insert the category
// - category_update     -- form fields    -- update the category
// - category_checklink  --                -- check if category is used
// - category_delete     -- $sel_kind      -- delete the category
// - display             --                -- display, set display parameters
// - dispref_display     --                -- update one field display value
// - dispref_level       --                -- update 1 field display position
// External API ---------------------------------------------------------------
// - ext_get_id          -- $title         -- select a company (return id) 
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "company";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_company_params();

page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("company_display.inc");
require("company_query.inc");
include("$obminclude/of/of_category.inc");
require_once("$obminclude/javascript/calendar_js.inc");
require("company_js.inc");

$uid = $auth->auth["uid"];

get_company_action();
$perm->check_permissions($module, $action);
update_last_visit("company", $params["company_id"], $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_id") {
  $display["search"] = dis_company_search_form($params);
  if ($set_display == "yes") {
    $display["result"] = dis_company_search_list($params);
  } else {
    $display["msg"] = display_info_msg($l_no_display);
  }

} elseif ($action == "ext_get_category1_ids") {
///////////////////////////////////////////////////////////////////////////////
  $extra_css = "category.css";
  $display["detail"] = of_category_dis_tree("company", "category1", $params, $action);

} elseif ($action == "ext_get_category1_code") {
///////////////////////////////////////////////////////////////////////////////
  $extra_css = "category.css";
  $display["detail"] = of_category_dis_tree("company", "category1", $params, $action);

} elseif ($action == "ext_get_category_ids") {
///////////////////////////////////////////////////////////////////////////////
  $extra_css = "category.css";
  $display["detail"] = of_category_user_dis_tree("company", $params["category"], $params, $action);


///////////////////////////////////////////////////////////////////////////////
// Normal calls
///////////////////////////////////////////////////////////////////////////////
} elseif ($action == "index") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_company_search_form($params);
  if ($set_display == "yes") {
    $display["result"] = dis_company_search_list($params);
  } else {
    $display["msg"] = display_info_msg($l_no_display);
  }

} elseif ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_company_search_form($params);
  $display["result"] = dis_company_search_list($params);

} elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_company_form($action, $params);

} elseif ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_company_consult($params);

} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_company_form($action, $params);

} elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_company_data_form("", $params)) {

    // If the context (same companies) was confirmed ok, we proceed
    if ($params["confirm"] == $c_yes) {
      $cid = run_query_company_insert($params);
      if ($cid > 0) {
        $params["company_id"] = $cid;
	$display["detail"] = dis_company_consult($params);
        $display["msg"] .= display_ok_msg("$l_company : $l_insert_ok");
      } else {
        $display["msg"] .= display_err_msg("$l_company : $l_insert_error");
	$display["detail"] = dis_company_form($action, $params);
      }
    // If it is the first try, we warn the user if some companies seem similar
    } else {
      $obm_q = check_company_context("", $params);
      if ($obm_q->num_rows() > 0) {
        $display["detail"] = dis_company_warn_insert("", $obm_q, $params);
      } else {
        $cid = run_query_company_insert($params);
        if ($cid > 0) {
          $params["company_id"] = $cid;
	  $display["detail"] = dis_company_consult($params);
          $display["msg"] .= display_ok_msg("$l_company : $l_insert_ok");
        } else {
          $display["msg"] .= display_err_msg("$l_company : $l_insert_error");
	  $display["detail"] = dis_company_form($action, $params);
        }
      }
    }

  // Form data are not valid
  } else {
    $display["msg"] = display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = dis_company_form($action, $params);
  }

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_company_data_form($params["company_id"], $params)) {
    $retour = run_query_company_update($params["company_id"], $params);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_company : $l_update_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_company : $l_update_error");
    }
    $display["detail"] = dis_company_consult($params);
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = dis_company_form($action, $params);
  }

} elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_company($params["company_id"])) {
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_can_delete_company($params["company_id"]);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_company_consult($params);
  }

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_company($params["company_id"])) {
    $retour = run_query_company_delete($params["company_id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_company : $l_delete_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_company : $l_delete_error");
    }
    $display["search"] = dis_company_search_form($params);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_company_consult($params);
  }

} elseif ($action == "admin") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_company_admin_index();

} elseif ($action == "type_insert") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("company", "type", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_type : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_type : $l_insert_error");
  }
  $display["detail"] .= dis_company_admin_index();

} elseif ($action == "type_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("company", "type", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_type : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_type : $l_update_error");
  }
  $display["detail"] .= dis_company_admin_index();

} elseif ($action == "type_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("company", "type", $params, "mono");

} elseif ($action == "type_delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("company", "type", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_type : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_type : $l_delete_error");
  }
  $display["detail"] .= dis_company_admin_index();

} elseif ($action == "activity_insert") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("company", "activity", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_activity : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_activity : $l_insert_error");
  }
  $display["detail"] .= dis_company_admin_index();

} elseif ($action == "activity_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("company", "activity", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_activity : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_activity : $l_update_error");
  }
  $display["detail"] .= dis_company_admin_index();

} elseif ($action == "activity_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("company", "activity", $params, "mono");

} elseif ($action == "activity_delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("company", "activity", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_activity : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_activity : $l_delete_error");
  }
  $display["detail"] .= dis_company_admin_index();

} elseif ($action == "nafcode_insert") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_company_nafcode_insert($params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_nafcode : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_nafcode : $l_insert_error");
  }
  $display["detail"] .= dis_company_admin_index();

} elseif ($action == "nafcode_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_company_nafcode_update($params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_nafcode : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_nafcode : $l_update_error");
  }
  $display["detail"] .= dis_company_admin_index();

} elseif ($action == "nafcode_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= dis_company_nafcode_links($params);

} elseif ($action == "nafcode_delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_company_nafcode_delete($params["naf"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_nafcode : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_nafcode : $l_delete_error");
  }
  $display["detail"] .= dis_company_admin_index();

} elseif ($action == "category_insert") {
///////////////////////////////////////////////////////////////////////////////
  $category = $params["category"];
  $l_cat = ${"l_$category"};
  $retour = of_category_user_query_insert($params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_cat : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_cat : $l_insert_error");
  }
  $display["detail"] .= dis_company_admin_index();

} elseif ($action == "category_update") {
///////////////////////////////////////////////////////////////////////////////
  $category = $params["category"];
  $l_cat = ${"l_$category"};
  $retour = of_category_user_query_update($params); 
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_cat : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_cat : $l_update_error");
  }
  $display["detail"] .= dis_company_admin_index();

} elseif ($action == "category_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_user_dis_links($params);

} elseif ($action == "category_delete") {
///////////////////////////////////////////////////////////////////////////////
  $category = $params["category"];
  $l_cat = ${"l_$category"};
  $retour = of_category_user_query_delete($params); 
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_cat : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_cat : $l_delete_error");
  }
  $display["detail"] .= dis_company_admin_index();

}  elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($uid, "company", 1);
  $display["detail"] = dis_company_display_pref($prefs);

} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($uid, "company", 1);
  $display["detail"] = dis_company_display_pref($prefs);

} else if ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($uid, "company", 1);
  $display["detail"] = dis_company_display_pref($prefs);

} elseif ($action == "document_add") {
///////////////////////////////////////////////////////////////////////////////
  $params["company_id"] = $params["ext_id"];
  if ($params["doc_nb"] > 0) {
    $nb = run_query_global_insert_documents($params, "company");
    $display["msg"] .= display_ok_msg("$nb $l_document_added");
  } else {
    $display["msg"] .= display_err_msg($l_no_document_added);
  }
  $display["detail"] = dis_company_consult($params);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_company);
$display["end"] = display_end();
// Update actions url in case some values have been updated (id after insert) 
if (! $params["popup"]) {
  update_company_action();
  $display["header"] = display_menu($module);
}

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Company parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_company_params() {

  // Get global params
  $params = get_global_params("Company");
  
  // Get company specific params
  if (isset ($params["name"])) $params["name"] = get_format_company_name($params["name"]);
  if (isset ($params["town"])) $params["town"] = get_format_town($params["town"]);
  
  get_global_params_document($params);
  
  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Auto transform company name (could become a HOOK)
// Here cause used in get_company_params()
// Parameters:
//   - $name : company name
// Returns:
//   $name formatted
///////////////////////////////////////////////////////////////////////////////
function get_format_company_name($name) {
  global $caf_company_name;

  $res = $name;
  if ($caf_company_name) {
    $res = strip_tags(format_name($res, 2, true, false));
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Company Actions 
///////////////////////////////////////////////////////////////////////////////
function get_company_action() {
  global $params, $actions, $path;
  global $l_header_find,$l_header_new_f,$l_header_update,$l_header_delete;
  global $l_header_consult, $l_header_display,$l_header_admin;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["company"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/company/company_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    	 );

// Search
  $actions["company"]["search"] = array (
    'Url'      => "$path/company/company_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    	 );

// New
  $actions["company"]["new"] = array (
    'Name'     => $l_header_new_f,
    'Url'      => "$path/company/company_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('search','index','detailconsult','insert','update','delete','admin','display') 
                                     );

// Detail Consult
  $actions["company"]["detailconsult"]  = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/company/company_index.php?action=detailconsult&amp;company_id=".$params["company_id"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('detailconsult', 'detailupdate')
                                     		 );

// Detail Update
  $actions["company"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/company/company_index.php?action=detailupdate&amp;company_id=".$params["company_id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'update')
                                     	      );

// Insert
  $actions["company"]["insert"] = array (
    'Url'      => "$path/company/company_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	 );

// Update
  $actions["company"]["update"] = array (
    'Url'      => "$path/company/company_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	 );

// Check Delete
  $actions["company"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/company/company_index.php?action=check_delete&amp;company_id=".$params["company_id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update') 
                                     	      );

// Delete
  $actions["company"]["delete"] = array (
    'Url'      => "$path/company/company_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );

// Admin
  $actions["company"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/company/company_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                       );

// Type Insert
  $actions["company"]["type_insert"] = array (
    'Url'      => "$path/company/company_index.php?action=type_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Type Update
  $actions["company"]["type_update"] = array (
    'Url'      => "$path/company/company_index.php?action=type_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Type Check Link
  $actions["company"]["type_checklink"] = array (
    'Url'      => "$path/company/company_index.php?action=type_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Type Delete
  $actions["company"]["type_delete"] = array (
    'Url'      => "$path/company/company_index.php?action=type_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Category Insert
  $actions["company"]["category_insert"] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Category Update
  $actions["company"]["category_update"] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Category Check Link
  $actions["company"]["category_checklink"] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Category Delete
  $actions["company"]["category_delete"] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Activity Insert
  $actions["company"]["activity_insert"] = array (
    'Url'      => "$path/company/company_index.php?action=activity_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Activity Update
  $actions["company"]["activity_update"] = array (
    'Url'      => "$path/company/company_index.php?action=activity_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Activity Check Link
  $actions["company"]["activity_checklink"] = array (
    'Url'      => "$path/company/company_index.php?action=activity_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Activity Delete
  $actions["company"]["activity_delete"] = array (
    'Url'      => "$path/company/company_index.php?action=activity_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Naf Code Insert
  $actions["company"]["nafcode_insert"] = array (
    'Url'      => "$path/company/company_index.php?action=nafcode_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Naf Code Update
  $actions["company"]["nafcode_update"] = array (
    'Url'      => "$path/company/company_index.php?action=nafcode_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Naf Code Check Link
  $actions["company"]["nafcode_checklink"] = array (
    'Url'      => "$path/company/company_index.php?action=nafcode_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Naf Code Delete
  $actions["company"]["nafcode_delete"] = array (
    'Url'      => "$path/company/company_index.php?action=nafcode_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Display
  $actions["company"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/company/company_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );

// Display Preferences
  $actions["company"]["dispref_display"] = array (
    'Url'      => "$path/company/company_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

// Display Level
  $actions["company"]["dispref_level"]  = array (
    'Url'      => "$path/company/company_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

// Category Select 
  $actions["company"]["ext_get_category1_ids"]  = array (
    'Url'      => "$path/company/company_index.php?action=ext_get_category1_ids",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );
// Category Select 
  $actions["company"]["ext_get_category1_code"]  = array (
    'Url'      => "$path/company/company_index.php?action=ext_get_category1_code",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

// Category Select 
  $actions["company"]["ext_get_category_ids"]  = array (
    'Url'      => "$path/company/company_index.php?action=ext_get_category_ids",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                     		 );

// Company Select 
  $actions["company"]["ext_get_id"]  = array (
    'Url'      => "$path/company/company_index.php?action=ext_get_id",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

// Document add
  $actions["company"]["document_add"] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None')
					       );
}


///////////////////////////////////////////////////////////////////////////////
// Company Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_company_action() {
  global $params, $actions, $path;

  $id = $params["company_id"];
  if ($id > 0) {
    // Detail Consult
    $actions["company"]["detailconsult"]["Url"] = "$path/company/company_index.php?action=detailconsult&amp;company_id=$id";
    $actions["company"]["detailconsult"]['Condition'][] = 'insert';

    // Detail Update
    $actions["company"]["detailupdate"]['Url'] = "$path/company/company_index.php?action=detailupdate&amp;company_id=$id";
    $actions["company"]["detailupdate"]['Condition'][] = 'insert';

    // Check Delete
    $actions["company"]["check_delete"]['Url'] = "$path/company/company_index.php?action=check_delete&amp;company_id=$id";
    $actions["company"]["check_delete"]['Condition'][] = 'insert';
  }
}

?>
