<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : list_index.php                                               //
//     - Desc : List Index File                                              //
// 1999-03-19 - Aliacom                                                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the list search form
// - search          -- search fields  -- show the result set of search
// - new             --                -- show the new list form
// - detailconsult   -- $param_list    -- show the list detail
// - detailupdate    -- $param_list    -- show the list detail form
// - detailduplicate -- $param_list    -- show the list detail form
// - insert          -- form fields    -- insert the list
// - update          -- form fields    -- update the list
// - delete          -- $param_list    -- delete the list
// - contact_add     -- 
// - contact_del     -- 
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
// External API ---------------------------------------------------------------
// - ext_get_ids     --                -- select multiple lists (return id) 
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "list";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
require("$obminclude/global_pref.inc");
require_once("$obminclude/javascript/calendar_js.inc");

include("list_display.inc");
include("list_query.inc");

if ($action == "") $action = "index";
$uid = $auth->auth["uid"];
$list = get_param_list();
get_list_action();
$perm->check_permissions($module, $action);
if (! check_privacy($module, "List", $action, $list["id"], $uid)) {
  $display["msg"] = display_err_msg($l_error_visibility);
  $action = "index";
} else {
  update_last_visit("list", $list["id"], $action);
}

page_close();

require("list_js.inc");

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_id") {
  $display["search"] = dis_list_search_form($list);
  if ($set_display == "yes") {
    $display["detail"] = dis_list_search_list($list, $popup);
  } else {
    $display["msg"] .= display_ok_msg($l_no_display);
  }

}
else if ($action == "new_criterion") {
  $display["detail"] = dis_list_add_criterion_form($list);

} elseif (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_list_search_form($list);
  if ($set_display == "yes") {
    $display["result"] = dis_list_search_list("", $popup);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} else if ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_list_search_form($list);
  $display["result"] = dis_list_search_list($list, $popup);

} else if ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_list_form($action, "", $list);

} else if ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_list_consult($list);

} else if ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $list_q = run_query_list_detail($list["id"]);
  $display["detail"] = dis_list_form($action, $list_q, $list);

} else if ($action == "detailduplicate") {
///////////////////////////////////////////////////////////////////////////////
  $list_q = run_query_list_detail($list["id"]);
  $list["id_duplicated"] = $list["id"];
  $list["id"] = "";
  $list["name"] = $list_q->f("list_name") . " - $l_aduplicate";
  $display["detail"] = dis_list_form($action, $list_q, $list);

} else if ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if ($list["criteria"] != "") {
    $dynlist = make_list_query_from_criteria($list);
    $list["query"] = $dynlist["query"];
  } else {
    // To change : we do not know if expert mode (query should be stripslashed)
    // or no more graphical criteria (query should be set to empty)
    $list["query"] = stripslashes($list["query"]);
  }
  if (check_list_data("", $list)) {
    // If the context (same list) was confirmed ok, we proceed
    if ($hd_confirm == $c_yes) {
      $list["id"] = run_query_list_insert($list);
      if ($list["id"] > 0) {
        $display["msg"] .= display_ok_msg($l_insert_ok);
      } else {
        $display["msg"] .= display_err_msg($l_insert_error);
      }
      $display["detail"] = dis_list_consult($list);

    // If it is the first try, we warn the user if some lists seem similar
    } else {
      $obm_q = check_list_context("", $list);
      if ($obm_q->num_rows() > 0) {
        $display["detail"] = dis_list_warn_insert($obm_q, $list);
      } else {
	$list["id"] = run_query_list_insert($list);
        if ($list["id"] > 0) {
          $display["msg"] .= display_ok_msg($l_insert_ok);
        } else {
          $display["msg"] .= display_err_msg($l_insert_error);
        }
	$display["detail"] = dis_list_consult($list);
      }
    }

  // Form data are not valid
  } else {
    $display["msg"] .= display_warn_msg($err_msg);
    $display["detail"] = dis_list_form($action, "", $list);
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if ($list["criteria"] != "") {
    $dynlist = make_list_query_from_criteria($list);
    $list["query"] = $dynlist["query"];
  } else {
    // To change : we do not know if expert mode (query should be stripslashed)
    // or no more graphical criteria (query should be set to empty)
    $list["query"] = stripslashes($list["query"]);
  }
  if (check_list_data($list["id"], $list)) {
    $retour = run_query_list_update($list);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok);
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
    }
    $display["detail"] = dis_list_consult($list);
  } else {
    $display["msg"] .= display_warn_msg($err_msg);
    $list_q = run_query_list_detail($list["id"]);
    $display["detail"] = dis_list_form($action, $list_q, $list);
  }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_list_can_delete($list["id"])) {
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_list_can_delete($list["id"]);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_list_consult($list);
  }

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_list_can_delete($list["id"])) {
    $retour = run_query_list_delete($list["id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_delete_ok);
    } else {
      $display["msg"] .= display_err_msg($l_delete_error);
    }
    $display["search"] = dis_list_search_form("");
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_list_consult($list);
  }

} elseif ($action == "contact_add")  {
///////////////////////////////////////////////////////////////////////////////
  if ($list["con_nb"] > 0) {
    $nb = run_query_list_contactlist_insert($list);
    run_query_list_update_sql($list["id"]);
    run_query_list_update_static_nb($list["id"]);
    $display["msg"] .= display_ok_msg("$nb $l_contact_added");
  } else {
    $display["msg"] .= display_err_msg("no contact to add");
  }
  $display["detail"] = dis_list_consult($list);

} elseif ($action == "contact_del")  {
///////////////////////////////////////////////////////////////////////////////
  if ($list["con_nb"] > 0) {
    $nb = run_query_list_contactlist_delete($list);
    run_query_list_update_sql($list["id"]);
    run_query_list_update_static_nb($list["id"]);
    $display["msg"] .= display_ok_msg("$nb $l_contact_removed");
  } else {
    $display["msg"] .= display_err_msg("no contact to delete");
  }
  $display["detail"] = dis_list_consult($list);

} else if ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($uid, "list", 1);
  $prefs_con = get_display_pref($uid, "list_contact", 1);
  $display["detail"] = dis_list_display_pref($prefs, $prefs_con);

} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($uid, "list", 1);
  $prefs_con = get_display_pref($uid, "list_contact", 1);
  $display["detail"] = dis_list_display_pref($prefs, $prefs_con);

} else if($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($uid, "list", 1);
  $prefs_con = get_display_pref($uid, "list_contact", 1);
  $display["detail"] = dis_list_display_pref($prefs, $prefs_con);

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
} else if ($action == "ext_get_ids") {
  $display["search"] = dis_list_search_form($list);
  if ($set_display == "yes") {
    $display["detail"] = dis_list_search_list($list, $popup);
  } else {
    $display["msg"] .= display_ok_msg($l_no_display);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
// Update actions url in case some values have been updated (id after insert) 
update_list_action();
$display["head"] = display_head($l_list);
$display["end"] = display_end();
if (! $popup) {
  $display["header"] = display_menu($module);
}

display_page($display);
exit(0);

///////////////////////////////////////////////////////////////////////////////
// Stores in $list hash, List parameters transmited
// returns : $list hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_list() {
  global $tf_name, $tf_subject, $tf_email, $ta_query, $tf_contact, $sel_market;
  global $param_list, $param_ext, $hd_usercreate, $hd_timeupdate, $rd_mode;
  global $action, $cb_priv, $ext_action, $ext_url, $ext_id, $ext_target,$title;
  global $id_duplicated, $new_order, $order_dir, $popup, $row_index;
  global $param_contact, $cb_mailing_ok, $cb_contact_arch, $cb_info_pub;

  global $tf_company_name, $tf_company_zipcode, $tf_company_town;
  global $tf_company_timeafter, $tf_company_timebefore;
  global $sel_company_country_iso3166, $sel_company_marketingmanager_id;
  global $sel_company_datasource_id, $sel_companycategory1_code;

  global $tf_contact_firstname, $tf_contact_lastname;
  global $tf_contact_zipcode, $tf_contact_town, $sel_kind_lang;
  global $tf_contact_timeafter, $tf_contact_timebefore;
  global $sel_contact_country_iso3166, $sel_contact_marketingmanager_id;
  global $sel_contact_datasource_id, $sel_contactcategory1link_category_id;
  global $sel_contactcategory2link_category_id, $sel_contact_function_id;
  
  global $sel_subscription_publication_id,$tf_publication_lang,$tf_publication_year;
  global $sel_subscription_reception_id, $tf_subscription_renewal;
  global $tf_subscription_timeafter, $tf_subscription_timebefore;
  
  global $sel_log_and,$sel_log_not;
  global $se_criteria;
  global $HTTP_POST_VARS, $HTTP_GET_VARS, $ses_list;

  // List fields
  if (isset ($param_ext)) $list["id"] = $param_ext;
  if (isset ($param_list)) $list["id"] = $param_list;
  if (isset ($id_duplicated)) $list["id_duplicated"] = $id_duplicated;
  if (isset ($param_contact)) $list["contact_id"] = $param_contact;
  if (isset ($tf_name)) $list["name"] = trim($tf_name);
  if (isset ($tf_subject)) $list["subject"] = trim($tf_subject);
  if (isset ($tf_email)) $list["email"] = $tf_email;
  if (isset ($rd_mode)) $list["mode"] = $rd_mode;
  if (isset ($ta_query)) $list["query"] = trim($ta_query);
  if (isset ($tf_contact)) $list["contact"] = trim($tf_contact);
  if (isset ($sel_market)) $list["marketing_manager"] = $sel_market;
  if (isset ($row_index)) $list["row_index"] = $row_index;
  if (isset($cb_priv)) $list["priv"] = ($cb_priv == "1") ? 1 : 0;
  if (isset($cb_mailing_ok)) $list["mailing_ok"] = $cb_mailing_ok == 1 ? 1 : 0;
  if (isset($cb_contact_arch)) $list["contact_arch"] = $cb_contact_arch == 1 ? 1 : 0; 
  if (isset($cb_info_pub)) $list["info_pub"] = $cb_info_pub == 1 ? 1 : 0;

  if (isset ($hd_usercreate)) $list["usercreate"] = $hd_usercreate;
  if (isset ($hd_timeupdate)) $list["timeupdate"] = $hd_timeupdate;

  if (isset ($new_order)) $list["new_order"] = $new_order;
  if (isset ($order_dir)) $list["order_dir"] = $order_dir;
  if (isset ($popup)) $list["popup"] = $popup;

  // External param
  if (isset ($ext_action)) $list["ext_action"] = $ext_action;
  if (isset ($ext_url)) $list["ext_url"] = $ext_url;
  if (isset ($ext_id)) $list["ext_id"] = $ext_id;
  if (isset ($ext_target)) $list["ext_target"] = $ext_target;
  if (isset ($title)) $list["title"] = stripslashes($title);
  if ((is_array ($HTTP_POST_VARS)) && (count($HTTP_POST_VARS) > 0)) {
    $http_obm_vars = $HTTP_POST_VARS;
  } elseif ((is_array ($HTTP_GET_VARS)) && (count($HTTP_GET_VARS) > 0)) {
    $http_obm_vars = $HTTP_GET_VARS;
  }
  
  // Criteria params :
  // Company
  if (isset ($tf_company_name)) $list["criteria"]["modules"]["company"]["company_name"] = $tf_company_name;
  if (isset ($sel_company_country_iso3166)) $list["criteria"]["modules"]["company"]["company_country_iso3166"] = $sel_company_country_iso3166;
  if (isset ($tf_company_timeafter)) $list["criteria"]["modules"]["company"]["company_timeafter"] = $tf_company_timeafter; 
  if (isset ($tf_company_zipcode)) $list["criteria"]["modules"]["company"]["company_zipcode"] = $tf_company_zipcode;
  if (isset ($sel_company_marketingmanager_id)) $list["criteria"]["modules"]["company"]["company_marketingmanager_id"] = $sel_company_marketingmanager_id;
  if (isset ($tf_company_timebefore)) $list["criteria"]["modules"]["company"]["company_timebefore"] = $tf_company_timebefore;
  if (isset ($tf_company_town)) $list["criteria"]["modules"]["company"]["company_town"] = $tf_company_town;
  if (isset ($sel_company_datasource_id)) $list["criteria"]["modules"]["company"]["company_datasource_id"] = $sel_company_datasource_id;
  if (isset ($sel_companycategory1_code)) $list["criteria"]["modules"]["company"]["companycategory1_code"] = $sel_companycategory1_code;
  
  // Contact
  if (isset ($tf_contact_firstname)) $list["criteria"]["modules"]["contact"]["contact_firstname"] = $tf_contact_firstname;
  if (isset ($sel_contact_country_iso3166)) $list["criteria"]["modules"]["contact"]["contact_country_iso3166"] = $sel_contact_country_iso3166;
  if (isset ($tf_contact_timeafter)) $list["criteria"]["modules"]["contact"]["contact_timeafter"] = $tf_contact_timeafter;
  if (isset ($tf_contact_lastname)) $list["criteria"]["modules"]["contact"]["contact_lastname"] = $tf_contact_lastname;
  if (isset ($sel_contact_marketingmanager_id)) $list["criteria"]["modules"]["contact"]["contact_marketingmanager_id"] = $sel_contact_marketingmanager_id;
  if (isset ($tf_contact_timebefore)) $list["criteria"]["modules"]["contact"]["contact_timebefore"] = $tf_contact_timebefore;
  if (isset ($sel_contact_datasource_id)) $list["criteria"]["modules"]["contact"]["contact_datasource_id"] = $sel_contact_datasource_id;
  if (isset ($tf_contact_town)) $list["criteria"]["modules"]["contact"]["contact_town"] = $tf_contact_town;
  if (isset ($tf_contact_zipcode)) $list["criteria"]["modules"]["contact"]["contact_zipcode"] = $tf_contact_zipcode;
  if (isset ($sel_contactcategory1link_category_id)) $list["criteria"]["modules"]["contact"]["contactcategory1link_category_id"] = $sel_contactcategory1link_category_id;
  if (isset ($sel_contactcategory2link_category_id)) $list["criteria"]["modules"]["contact"]["contactcategory2link_category_id"] = $sel_contactcategory2link_category_id;
  if (isset ($sel_contact_function_id)) $list["criteria"]["modules"]["contact"]["contact_function_id"] = $sel_contact_function_id;  
  if (isset ($sel_kind_lang)) $list["criteria"]["modules"]["contact"]["kind_lang"] = $sel_kind_lang;  

  // Publication
  if (isset ($sel_subscription_publication_id)) $list["criteria"]["modules"]["publication"]["subscription_publication_id"] = $sel_subscription_publication_id;
  if (isset ($tf_publication_lang)) $list["criteria"]["modules"]["publication"]["publication_lang"] = $tf_publication_lang;
  if (isset ($tf_publication_year)) $list["criteria"]["modules"]["publication"]["publication_year"] = $tf_publication_year;
  if (isset ($sel_subscription_reception_id)) $list["criteria"]["modules"]["publication"]["subscription_reception_id"] = $sel_subscription_reception_id;
  if (isset ($tf_subscription_renewal)) $list["criteria"]["modules"]["publication"]["subscription_renewal"] = $tf_subscription_renewal;
  if (isset ($tf_subscription_timeafter)) $list["criteria"]["modules"]["publication"]["subscription_timeafter"] = $tf_subscription_timeafter;
  if (isset ($tf_subscription_timebefore)) $list["criteria"]["modules"]["publication"]["subscription_timebefore"] = $tf_subscription_timebefore;
  

  if (isset ($sel_log_not)) $list["criteria"]["logical"]["NOT"] = $sel_log_not;
  if (isset ($sel_log_and)) $list["criteria"]["logical"]["AND"] = $sel_log_and;
  if (isset ($se_criteria)) {
    $list["criteria"] = unserialize(urldecode($se_criteria));
  }
  if (isset ($http_obm_vars)) {
    $nb_con = 0;
    $nb_list = 0;
    while ( list( $key ) = each( $http_obm_vars ) ) {
      if (strcmp(substr($key, 0, 6),"cb_con") == 0) {
	$nb_con++;
        $con_num = substr($key, 6);
        $list["con$nb_con"] = $con_num;
      } elseif (strcmp(substr($key, 0, 7),"cb_list") == 0) {
	$nb_list++;
        $list_num = substr($key, 7);
        $list["list_$nb_list"] = $list_num;
	// register the list in the list session array
	$ses_list[$list_num] = $list_num;
      }
    }
    $list["con_nb"] = $nb_con;
    $list["list_nb"] = $nb_list;
  }

  display_debug_param($list);

  return $list;
}


//////////////////////////////////////////////////////////////////////////////
// list actions
//////////////////////////////////////////////////////////////////////////////
function get_list_action() {
  global $list, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_list,$l_header_display, $l_header_duplicate;
  global $l_header_consult, $l_header_add_contact;
  global $l_select_list, $l_add_contact,$l_list_wizard;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["list"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/list/list_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

// Search
  $actions["list"]["search"] = array (
    'Url'      => "$path/list/list_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      );

// New
  $actions["list"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/list/list_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
                                  );

// New
  $actions["list"]["new_criterion"] = array (
    'Url'      => "$path/list/list_index.php?action=new_criterion",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                  );				  

  // Detail Consult
  $actions["list"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/list/list_index.php?action=detailconsult&amp;param_list=".$list["id"]."",
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('detailduplicate', 'detailupdate') 
                                      );

  // Detail Duplicate
  $actions["list"]["detailduplicate"] = array (
    'Name'     => $l_header_duplicate,
    'Url'      => "$path/list/list_index.php?action=detailduplicate&amp;param_list=".$list["id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailupdate', 'contact_add','contact_del', 'update')
                                      );

// Detail Update
  $actions["list"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/list/list_index.php?action=detailupdate&amp;param_list=".$list["id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailduplicate', 'contact_add','contact_del', 'update')
                                           );

// Insert
  $actions["list"]["insert"] = array (
    'Url'      => "$path/list/list_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                      );

// Update
  $actions["list"]["update"] = array (
    'Url'      => "$path/list/list_index.php?action=update",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                      );

// Check Delete
  $actions["list"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/list/list_index.php?action=check_delete&amp;param_list=".$list["id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult','detailupdate','contact_add','contact_del') 
                                           );

// Delete
  $actions["list"]["delete"] = array (
    'Url'      => "$path/list/list_index.php?action=delete",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                      );

// Sel list contacts : Contacts selection
  $actions["list"]["sel_list_contact"] = array (
    'Name'     => $l_header_add_contact,
    'Url'      => "$path/contact/contact_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_contact)."&amp;ext_action=contact_add&amp;ext_url=".urlencode($path."/list/list_index.php")."&amp;ext_id=".$list["id"]."&amp;ext_target=$l_list",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Target'   => $l_list,
    'Condition'=> array ('detailconsult','update','contact_add','contact_del')
                                          );

// Contact ADD
  $actions["list"]["contact_add"] = array (
    'Url'      => "$path/list/list_index.php?action=contact_add",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                          );
// Contact Del
  $actions["list"]["contact_del"] = array (
    'Url'      => "$path/list/list_index.php?action=contact_del",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                          );

// Display
  $actions["list"]["display"] = array (
   'Name'     => $l_header_display,
   'Url'      => "$path/list/list_index.php?action=display",
   'Right'    => $cright_read,
   'Condition'=> array ('all') 
                                      );

// Display Préférence
  $actions["list"]["dispref_display"] = array (
   'Url'      => "$path/list/list_index.php?action=dispref_display",
   'Right'    => $cright_read,
   'Condition'=> array ('None') 
                                               );

// Display level
  $actions["list"]["dispref_level"] = array (
   'Url'      => "$path/list/list_index.php?action=dispref_level",
   'Right'    => $cright_read,
   'Condition'=> array ('None') 
                                            );

// External List Select 
  $actions["list"]["ext_get_id"]  = array (
    'Url'      => "$path/list/list_index.php?action=ext_get_id",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );
					    
}


///////////////////////////////////////////////////////////////////////////////
// List Actions updates (after processing, before displaying menu)  
///////////////////////////////////////////////////////////////////////////////
function update_list_action() {
  global $list, $actions, $path, $l_add_contact, $l_list;

  $id = $list["id"];
  if ($id > 0) {
    // Detail Consult
    $actions["list"]["detailconsult"]['Url'] = "$path/list/list_index.php?action=detailconsult&amp;param_list=$id";
    $actions["list"]["detailconsult"]['Condition'][] = 'insert';

    // Detail Update
    $actions["list"]["detailupdate"]['Url'] = "$path/list/list_index.php?action=detailupdate&amp;param_list=$id";
    $actions["list"]["detailupdate"]['Condition'][] = 'insert';

    // Check Delete
    $actions["list"]["check_delete"]['Url'] = "$path/list/list_index.php?action=check_delete&amp;param_list=$id";
    $actions["list"]["check_delete"]['Condition'][] = 'insert';

    // Contact selection
    $actions["list"]["sel_list_contact"]['Url'] = "$path/contact/contact_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_contact)."&amp;ext_action=contact_add&amp;ext_url=".urlencode($path."/list/list_index.php")."&amp;ext_id=$id&amp;ext_target=$l_list";
    $actions["list"]["sel_list_contact"]['Condition'][] = 'insert';
  }

}

?>