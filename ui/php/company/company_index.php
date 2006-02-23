<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : company_index.php                                            //
//     - Desc : Company Index File                                           //
// 1999-03-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions                 -- Parameter
// - index (default)       -- search fields  -- show the company search form
// - search                -- search fields  -- show the result set of search
// - new                   --                -- show the new company form
// - detailconsult         -- $param_company -- show the company detail
// - detailupdate          -- $param_company -- show the company detail form
// - insert                -- form fields    -- insert the company
// - update                -- form fields    -- update the company
// - check_delete          -- $param_company -- check links before delete
// - delete                -- $param_company -- delete the company
// - admin                 --                -- admin index (kind)
// - type_insert           -- form fields    -- insert the  type
// - type_update           -- form fields    -- update the type 
// - type_checklink        --                -- check if type is used
// - type_delete           -- $sel_type      -- delete the type 
// - activity_insert       -- form fields    -- insert the activity
// - activity_update       -- form fields    -- update the activity
// - activity_checklink    --                -- check if activity is used
// - activity_delete       -- $sel_kind      -- delete the activity
// - nafcode_insert        -- form fields    -- insert the nafcode
// - nafcode_update        -- form fields    -- update the nafcode
// - nafcode_checklink     --                -- check if nafcode is used
// - nafcode_delete        -- $sel_kind      -- delete the nafcode
// - category1_insert      -- form fields    -- insert the category
// - category1_update      -- form fields    -- update the category
// - category1_checklink   --                -- check if category is used
// - category1_delete      -- $sel_kind      -- delete the category
// - display               --                -- display, set display parameters
// - dispref_display       --                -- update one field display value
// - dispref_level         --                -- update 1 field display position
// External API ---------------------------------------------------------------
// - ext_get_id         -- $title         -- select a company (return id) 
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "company";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("company_query.inc");
require("company_display.inc");
include("$obminclude/of/of_category.inc");

$uid = $auth->auth["uid"];

if ($action == "") $action = "index";
$params = get_param_company();
get_company_action();
$perm->check_permissions($module, $action);
update_last_visit("company", $params["id"], $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_id") {
  require("company_js.inc");
  $display["search"] = dis_company_search_form($params);
  if ($set_display == "yes") {
    $display["result"] = dis_company_search_list($params);
  } else {
    $display["msg"] = display_info_msg($l_no_display);
  }

} elseif ($action == "ext_get_category1_ids") {
///////////////////////////////////////////////////////////////////////////////
  $extra_css = "category.css";
  require("company_js.inc");
  $display["detail"] = of_category_dis_tree("company", "category1", $params, $action);

} elseif ($action == "ext_get_category1_code") {
///////////////////////////////////////////////////////////////////////////////
  $extra_css = "category.css";
  require("company_js.inc");
  $display["detail"] = of_category_dis_tree("company", "category1", $params, $action);

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
  require("company_js.inc");
  $display["search"] = dis_company_search_form($params);
  $display["result"] = dis_company_search_list($params);

} elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  require("company_js.inc");
  $display["detail"] = dis_company_form($action, $params);

} elseif ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  require("company_js.inc");
  $display["detail"] = dis_company_consult($params);

} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  require("company_js.inc");
  $display["detail"] = dis_company_form($action, $params);

} elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form("", $params)) {

    // If the context (same companies) was confirmed ok, we proceed
    if ($params["confirm"] == $c_yes) {
      $cid = run_query_company_insert($params);
      if ($cid > 0) {
        $params["id"] = $cid;
	$display["detail"] = dis_company_consult($params);
        $display["msg"] .= display_ok_msg($l_insert_ok);
      } else {
        $display["msg"] .= display_err_msg($l_insert_error);
	require("company_js.inc");
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
          $params["id"] = $cid;
	  $display["detail"] = dis_company_consult($params);
          $display["msg"] .= display_ok_msg($l_insert_ok);
        } else {
          $display["msg"] .= display_err_msg($l_insert_error);
	  require("company_js.inc");
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
  if (check_data_form($params["id"], $params)) {
    $retour = run_query_company_update($params["id"], $params);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok);
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
    }
    require("company_js.inc");
    $display["detail"] = dis_company_consult($params);
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = dis_company_form($action, $params);
  }

} elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_company($params["id"])) {
    require("company_js.inc");
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_can_delete_company($params["id"]);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_company_consult($params);
  }

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_company($params["id"])) {
    $retour = run_query_company_delete($params["id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_delete_ok);
    } else {
      $display["msg"] .= display_err_msg($l_delete_error);
    }
    $display["search"] = dis_company_search_form($params);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_company_consult($params);
  }

} elseif ($action == "admin") {
///////////////////////////////////////////////////////////////////////////////
  require("company_js.inc");
  $display["detail"] = dis_admin_index();

} elseif ($action == "type_insert") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("company", "type", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_type)." : $l_c_insert_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_type)." : $l_c_insert_error");
  }
  require("company_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "type_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("company", "type", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_type)." : $l_c_update_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_type)." : $l_c_update_error");
  }
  require("company_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "type_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("company", "type", $params, "mono");

} elseif ($action == "type_delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("company", "type", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_type)." : $l_c_delete_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_type)." : $l_c_delete_error");
  }
  require("company_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "activity_insert") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("company", "activity", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_activity)." : $l_c_insert_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_activity)." : $l_c_insert_error");
  }
  require("company_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "activity_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("company", "activity", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_activity)." : $l_c_update_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_activity)." : $l_c_update_error");
  }
  require("company_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "activity_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("company", "activity", $params, "mono");

} elseif ($action == "activity_delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("company", "activity", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_activity)." : $l_c_delete_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_activity)." : $l_c_delete_error");
  }
  require("company_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "nafcode_insert") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_nafcode_insert($params);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_naf_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_naf_insert_error);
  }
  require("company_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "nafcode_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_nafcode_update($params);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_naf_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_naf_update_error);
  }
  require("company_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "nafcode_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= dis_nafcode_links($params);

} elseif ($action == "nafcode_delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_nafcode_delete($params["nafcode"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_naf_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_naf_delete_error);
  }
  require("company_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "category1_insert") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("company", "category1", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category1)." : $l_c_insert_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category1)." : $l_c_insert_error");
  }
  require("company_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "category1_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("company", "category1", $params); 
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category1)." : $l_c_update_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category1)." : $l_c_update_error");
  }
  require("company_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "category1_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("company", "category1", $params);

} elseif ($action == "category1_delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("company", "category1", $params); 
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category1)." : $l_c_delete_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category1)." : $l_c_delete_error");
  }
  require("company_js.inc");
  $display["detail"] .= dis_admin_index();

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
  if ($params["doc_nb"] > 0) {
    $nb = run_query_insert_documents($params, "company");
    $display["msg"] .= display_ok_msg("$nb $l_document_added");
  } else {
    $display["msg"] .= display_err_msg($l_no_document_added);
  }
  require("company_js.inc");
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
function get_param_company() {
  global $tf_num, $cb_archive, $tf_name, $tf_aka, $tf_ad1, $tf_ad2, $tf_ad3;
  global $tf_zip, $tf_town, $tf_cdx, $sel_ctry, $tf_phone, $tf_fax, $tf_web;
  global $tf_email, $sel_naf, $sel_market;
  global $ta_com, $tf_datecomment, $sel_usercomment, $ta_add_comment;
  global $tf_dateafter, $tf_datebefore, $cb_category1_tree, $cb_fuzzy;
  global $sel_dsrc, $tf_act, $tf_category1_code, $tf_cat;
  global $tf_naf_code, $tf_naf_label, $cb_naf_title, $tf_vat;
  global $tf_category1_label, $tf_category1_code, $sel_category1;
  global $tf_type_label, $tf_type_code, $sel_type;
  global $tf_activity_label, $tf_activity_code, $sel_activity;
  global $param_company, $hd_confirm;
  global $popup, $ext_action, $ext_url, $ext_id, $ext_title, $ext_target;  
  global $ext_widget, $ext_widget_text;
  global $view;

  if (isset ($popup)) $params["popup"] = $popup;
  if (isset ($ext_action)) $params["ext_action"] = $ext_action;
  if (isset ($ext_url)) $params["ext_url"] = urldecode($ext_url);
  if (isset ($ext_id)) $params["ext_id"] = $ext_id;
  if (isset ($ext_id)) $params["id"] = $ext_id;
  if (isset ($ext_title)) $params["ext_title"] = stripslashes(urldecode($ext_title));
  if (isset ($ext_target)) $params["ext_target"] = $ext_target;
  if (isset ($ext_widget)) $params["ext_widget"] = $ext_widget;
  if (isset ($ext_widget_text)) $params["ext_widget_text"] = $ext_widget_text;

  if (isset ($param_company)) $params["id"] = $param_company;
  if (isset ($view)) $params["view"] = $view;
  if (isset ($hd_confirm)) $params["confirm"] = $hd_confirm;
  if (isset ($tf_num)) $params["num"] = $tf_num;
  if (isset ($tf_vat)) $params["vat"] = $tf_vat;
  if (isset ($cb_archive)) $params["archive"] = ($cb_archive == 1 ? 1 : 0);
  if (isset ($tf_name)) $params["name"] = get_format_company_name($tf_name);
  if (isset ($tf_aka)) $params["aka"] = $tf_aka;
  if (isset ($sel_dsrc)) $params["datasource"] = $sel_dsrc;
  if (isset ($sel_naf)) $params["nafcode"] = $sel_naf;
  if (isset ($sel_market)) $params["marketing_manager"] = $sel_market;
  if (isset ($tf_ad1)) $params["ad1"] = $tf_ad1;
  if (isset ($tf_ad2)) $params["ad2"] = $tf_ad2;
  if (isset ($tf_ad3)) $params["ad3"] = $tf_ad3;
  if (isset ($tf_zip)) $params["zip"] = $tf_zip;
  if (isset ($tf_town)) $params["town"] = get_format_town($tf_town);
  if (isset ($tf_cdx)) $params["cdx"] = $tf_cdx;
  if (isset ($sel_ctry)) $params["country"] = $sel_ctry;
  if (isset ($tf_phone)) $params["phone"] = $tf_phone;
  if (isset ($tf_fax)) $params["fax"] = $tf_fax;
  if (isset ($tf_web)) $params["web"] = $tf_web;
  if (isset ($tf_email)) $params["email"] = $tf_email;
  if (isset ($ta_com)) $params["comment"] = $ta_com;
  if (isset ($tf_datecomment)) $params["datecomment"] = $tf_datecomment;
  if (isset ($sel_usercomment)) $params["usercomment"] = $sel_usercomment;
  if (isset ($ta_add_comment)) $params["add_comment"] = trim($ta_add_comment);
  if (isset ($sel_category1)) $params["category1"] = $sel_category1;
  if (isset ($sel_type)) $params["type"] = $sel_type;
  if (isset ($sel_activity)) $params["activity"] = $sel_activity;

  // Search fields
  if (isset ($tf_dateafter)) $params["dateafter"] = $tf_dateafter;
  if (isset ($tf_datebefore)) $params["datebefore"] = $tf_datebefore;
  if (isset ($cb_fuzzy)) $params["fuzzy"] = ($cb_fuzzy == 1 ? 1 : 0);

  // Admin - Type fields
  // $sel_type -> "type" is already set
  if (isset ($tf_type_code)) $params["type_code"] = $tf_type_code;
  if (isset ($tf_type_label)) $params["type_label"] = $tf_type_label;

  // Admin - Activity fields
  // $sel_activity -> "activity" is already set
  if (isset ($tf_activity_code)) $params["activity_code"] = $tf_activity_code;
  if (isset ($tf_activity_label)) $params["activity_label"] = $tf_activity_label;

  // Admin - Nafcode fields
  // $sel_naf -> "naf" is already set
  if (isset ($tf_naf_code)) $params["naf_code"] = $tf_naf_code;
  if (isset ($tf_naf_label)) $params["naf_label"] = $tf_naf_label;
  if (isset ($cb_naf_title)) $params["naf_title"] = $cb_naf_title;

  // Admin - Cat fields
  // $sel_category1 -> "category1" is already set
  if (isset ($tf_category1_code)) $params["category1_code"] = $tf_category1_code;
  if (isset ($tf_category1_label)) $params["category1_label"] = $tf_category1_label;
  if (isset ($cb_category1_tree)) $params["category1_tree"] = ($cb_category1_tree == 1 ? 1 : 0);

  get_global_param_document($params);

  display_debug_param($params);

  return $params;
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
    'Url'      => "$path/company/company_index.php?action=detailconsult&amp;param_company=".$params["id"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('detailconsult', 'detailupdate')
                                     		 );

// Detail Update
  $actions["company"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/company/company_index.php?action=detailupdate&amp;param_company=".$params["id"]."",
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
    'Url'      => "$path/company/company_index.php?action=check_delete&amp;param_company=".$params["id"]."",
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
  $actions["company"]["category1_insert"] = array (
    'Url'      => "$path/company/company_index.php?action=category1_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Category Update
  $actions["company"]["category1_update"] = array (
    'Url'      => "$path/company/company_index.php?action=category1_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Category Check Link
  $actions["company"]["category1_checklink"] = array (
    'Url'      => "$path/company/company_index.php?action=category1_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Category Delete
  $actions["company"]["category1_delete"] = array (
    'Url'      => "$path/company/company_index.php?action=category1_delete",
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

// Display Préférences
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

  $id = $params["id"];
  if ($id > 0) {
    // Detail Consult
    $actions["company"]["detailconsult"]["Url"] = "$path/company/company_index.php?action=detailconsult&amp;param_company=$id";
    $actions["company"]["detailconsult"]['Condition'][] = 'insert';

    // Detail Update
    $actions["company"]["detailupdate"]['Url'] = "$path/company/company_index.php?action=detailupdate&amp;param_company=$id";
    $actions["company"]["detailupdate"]['Condition'][] = 'insert';

    // Check Delete
    $actions["company"]["check_delete"]['Url'] = "$path/company/company_index.php?action=check_delete&amp;param_company=$id";
    $actions["company"]["check_delete"]['Condition'][] = 'insert';
  }
}

</script>
