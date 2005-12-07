<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : contact_index.php                                            //
//     - Desc : Contact Index File                                           //
// 1999-03-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default)    -- search fields  -- show the contact search form
// - search             -- search fields  -- show the result set of search
// - new                -- $param_company -- show the new contact form
// - detailconsult      -- $param_contact -- show the contact detail
// - detailupdate       -- $param_contact -- show the contact detail form
// - insert             -- form fields    -- insert the contact
// - update             -- form fields    -- update the contact
// - check_delete       -- $param_contact -- check links before delete
// - delete             -- $param_contact -- delete the contact
// - admin              --                -- admin index (kind)
// - function_insert    -- form fields    -- insert the function
// - function_update    -- form fields    -- update the function
// - function_checklink --                -- check if function is used
// - function_delete    -- $sel_func      -- delete the function
// - kind_insert        -- form fields    -- insert the kind
// - kind_update        -- form fields    -- update the kind
// - kind_checklink     --                -- check if kind is used
// - kind_delete     	-- $sel_kind      -- delete the kind
// - statistics     	--                -- display contact statistics
// - display            --                -- display and set display parameters
// - dispref_display    --                -- update one field display value
// - dispref_level      --                -- update one field display position 
// External API ---------------------------------------------------------------
// - ext_get_ids     --                -- select multiple contacts (return id) 
///////////////////////////////////////////////////////////////////////////////

// XXXXXXX dis_contact_form ? pourquoi param co_q et pas fait a l'interieur a la company ??

$path = "..";
$module = "contact";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("contact_display.inc");
require("contact_query.inc");
require_once("$obminclude/of/of_category.inc");

$uid = $auth->auth["uid"];

if ($action == "") $action = "index";
$contact = get_param_contact();
get_contact_action();
$perm->check_permissions($module, $action);
if (! check_privacy($module, "Contact", $action, $contact["id"], $uid)) {
  $display["msg"] = display_err_msg($l_error_visibility);
  $action = "index";
} else {
  update_last_visit("contact", $contact["id"], $action);
}
page_close();


///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_ids") {
  $display["search"] = dis_contact_search_form($contact);
  if ($set_display == "yes") {
    $display["result"] = dis_contact_search_list($contact);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "ext_get_category1_ids") {
///////////////////////////////////////////////////////////////////////////////
  $extra_css = "category.css";
  require("contact_js.inc");
  $display["detail"] = of_category_dis_tree($contact, $action, "contact", "category1");

} elseif ($action == "ext_get_category2_ids") {
///////////////////////////////////////////////////////////////////////////////
  $extra_css = "category.css";
  require("contact_js.inc");
  $display["detail"] = of_category_dis_tree($contact, $action, "contact", "category2");

} elseif ($action == "ext_get_category3_ids") {
///////////////////////////////////////////////////////////////////////////////
  $extra_css = "category.css";
  require("contact_js.inc");
  $display["detail"] = of_category_dis_tree($contact, $action, "contact", "category3");

} elseif ($action == "ext_get_category4_ids") {
///////////////////////////////////////////////////////////////////////////////
  $extra_css = "category.css";
  require("contact_js.inc");
  $display["detail"] = of_category_dis_tree($contact, $action, "contact", "category4");

} elseif ($action == "vcard") {
///////////////////////////////////////////////////////////////////////////////
  dis_vcard_export($contact);
  exit();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

} else if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_contact_search_form($contact);
  if ($set_display == "yes") {
    $display["result"] = dis_contact_search_list($contact);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }
  
} elseif ($action == "search")  {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_contact_search_form($contact);
  $display["result"] = dis_contact_search_list($contact);
  
} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  if (isset($contact["company_id"])) {
    $comp_q = run_query_contact_company($contact["company_id"]);
  }
  require("contact_js.inc");
  $display["detail"] = dis_contact_form($action, $comp_q, $contact);

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  if ($contact["id"] > 0) {
    require("contact_js.inc");
    $display["detail"] = dis_contact_consult($contact);
  }
  
} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  if ($contact["id"] > 0) {
    $con_q = run_query_contact_detail($contact["id"]);
    if ($con_q->num_rows() == 1) {
      require("contact_js.inc");
      $display["detailInfo"] = display_record_info($con_q);
      $display["detail"] = dis_contact_form($action, $con_q, $contact);
    } else {
      $display["msg"] .= display_err_msg($l_query_error . " - " . $con_q->query . " !");
    }
  }

} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_contact_data_form("", $contact)) {

    // If the context (same contacts) was confirmed ok, we proceed
    if ($hd_confirm == $c_yes) {
      $id = run_query_contact_insert($contact);
      if ($id > 0) {
        $contact["id"] = $id;
        $display["msg"] .= display_ok_msg($l_insert_ok);
      } else {
        $display["msg"] .= display_err_msg($l_insert_error);
      }
      require("contact_js.inc");
      $display["detail"] = dis_contact_consult($contact);

    // If it is the first try, we warn the user if some contacts seem similar
    } else {
      $obm_q = check_contact_context("", $contact);
      if ((is_object($obm_q)) && ($obm_q->num_rows() > 0)) {
	$display["title"] = display_title("$l_contact : $l_insert");
        $display["detail"] = dis_contact_warn_insert("", $obm_q, $contact);
      } else {
        $id = run_query_contact_insert($contact);
        if ($id > 0) {
          $contact["id"] = $id;
          $display["msg"] .= display_ok_msg($l_insert_ok);
          require("contact_js.inc");
          $display["detail"] = dis_contact_consult($contact);
        } else {
          $display["msg"] .= display_err_msg($l_insert_error);
          $display["detail"] = dis_contact_form($action, "", $contact);
        }
      }
    }

  // Form data are not valid
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    require("contact_js.inc");
    $display["detail"] = dis_contact_form($action, "", $contact);
  }
  
} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_contact_data_form("", $contact)) {
    $retour = run_query_contact_update($contact);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok);
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
    }
    if ($contact["id"] > 0) {
      require("contact_js.inc");
      $display["detail"] = dis_contact_consult($contact);
    }    
  } else {
    $display["msg"] .= display_err_msg($l_invalid_data . " : " . $err_msg);
    require("contact_js.inc");
    $display["detail"] = dis_contact_form($action, "", $contact);
  }
  
} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_contact($contact["id"])) {
    require("contact_js.inc");
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_can_delete_contact($contact["id"]);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_contact_consult($contact);
  }

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_contact($contact["id"])) {
    $retour = run_query_contact_delete($contact["id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_delete_ok);
    } else {
      $display["msg"] .= display_err_msg($l_delete_error);
    }
    $display["search"] = dis_contact_search_form($contact);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_contact_consult($contact);
  }

} elseif ($action == "statistics")  {
///////////////////////////////////////////////////////////////////////////////
  require_once("$obminclude/lang/$set_lang/statistic.inc");

  $cat1_q = of_run_query_category_per_entity("contact", "category1", "multi");
  $cat2_q = of_run_query_category_per_entity("contact", "category2", "multi");
  $cat3_q = of_run_query_category_per_entity("contact", "category3", "multi");
  $cat4_q = of_run_query_category_per_entity("contact", "category4", "multi");
  $cat5_q = of_run_query_category_per_entity("contact", "category5", "mono");
  $display["title"] = display_title($l_header_contact_stats);
  $display["detail"] = dis_category_contact_stats($cat1_q,$cat2_q,$cat3_q,$cat4_q,$cat5_q);
//  $display["features"] = dis_menu_stats();

} elseif ($action == "admin")  {
///////////////////////////////////////////////////////////////////////////////
  require("contact_js.inc");
  $display["detail"] = dis_contact_admin_index();

} elseif ($action == "category1_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_run_query_category_insert($contact, "contact", "category1");
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category1)." : $l_c_insert_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category1)." : $l_c_insert_error");
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "category1_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_run_query_category_update($contact, "contact", "category1");
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category1)." : $l_c_update_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category1)." : $l_c_update_error");
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "category1_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_dis_category_links($contact, "contact", "category1");

} elseif ($action == "category1_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_run_query_category_delete($contact, "contact", "category1");
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category1)." : $l_c_delete_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category1)." : $l_c_delete_error");
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "category2_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_run_query_category_insert($contact, "contact", "category2");
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category2)." : $l_c_insert_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category2)." : $l_c_insert_error");
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "category2_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_run_query_category_update($contact, "contact", "category2");
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category2)." : $l_c_update_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category2)." : $l_c_update_error");
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "category2_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_dis_category_links($contact, "contact", "category2");

 } elseif ($action == "category2_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_run_query_category_delete($contact, "contact", "category2");
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category2).": $l_c_delete_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category2)." : $l_c_delete_error");
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "category3_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_run_query_category_insert($contact, "contact", "category3");
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category3)." : $l_c_insert_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category3)." : $l_c_insert_error");
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "category3_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_run_query_category_update($contact, "contact", "category3");
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category3)." : $l_c_update_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category3)." : $l_c_update_error");
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "category3_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_dis_category_links($contact, "contact", "category3");

} elseif ($action == "category3_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_run_query_category_delete($contact, "contact", "category3");
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category3)." : $l_c_delete_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category3)." : $l_c_delete_error");
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "category4_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_run_query_category_insert($contact, "contact", "category4");
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category4)." : $l_c_insert_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category4)." : $l_c_insert_error");
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "category4_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_run_query_category_update($contact, "contact", "category4");
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category4)." : $l_c_update_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category4)." : $l_c_update_error");
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "category4_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_dis_category_links($contact, "contact", "category4");

} elseif ($action == "category4_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_run_query_category_delete($contact, "contact", "category4");
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category4)." : $l_c_delete_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category4)." : $l_c_delete_error");
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "category5_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_run_query_category_insert($contact, "contact", "category5");
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category5)." : $l_c_insert_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category5)." : $l_c_insert_error");
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "category5_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_run_query_category_update($contact, "contact", "category5");
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category5)." : $l_c_update_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category5)." : $l_c_update_error");
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "category5_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_dis_category_links($contact, "contact", "category5", "mono");

} elseif ($action == "category5_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_run_query_category_delete($contact, "contact", "category5");
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category5)." : $l_c_delete_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category5)." : $l_c_delete_error");
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "function_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_function_insert($contact);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_func_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_func_insert_error);
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "function_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_function_update($contact);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_func_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_func_update_error);
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "function_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= dis_function_links($contact);

} elseif ($action == "function_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_function_delete($contact["function"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_func_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_func_delete_error);
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "kind_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_kind_insert($contact);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_kind_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_kind_insert_error);
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "kind_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_kind_update($contact);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_kind_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_kind_update_error);
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "kind_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= dis_kind_links($contact);

} elseif ($action == "kind_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_kind_delete($contact["kind"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_kind_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_kind_delete_error);
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($uid, "contact", 1);
  $display["detail"] = dis_contact_display_pref($prefs); 
  
} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($uid, "contact", 1);
  $display["detail"] = dis_contact_display_pref($prefs);
  
} else if ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($uid, "contact", 1);
  $display["detail"] = dis_contact_display_pref($prefs);

} elseif ($action == "document_add")  {
///////////////////////////////////////////////////////////////////////////////
  if ($contact["doc_nb"] > 0) {
    $nb = run_query_insert_documents($contact, "contact");
    $display["msg"] .= display_ok_msg("$nb $l_document_added");
  } else {
    $display["msg"] .= display_err_msg($l_no_document_added);
  }
  if ($contact["id"] > 0) {
    require("contact_js.inc");
    $display["detail"] = dis_contact_consult($contact);
  }    
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_contact);
$display["end"] = display_end();
if (! $contact["popup"]) {
  update_contact_action();
  $display["header"] = display_menu($module);
}

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Contact parameters transmited in $contact hash
// returns : $contact hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_contact() {
  global $action, $view;
  global $sel_dsrc, $sel_kind, $tf_lname, $tf_fname, $tf_company, $tf_service;
  global $tf_ad1, $tf_ad2, $tf_ad3, $tf_zip, $tf_town, $tf_cdx, $sel_ctry;
  global $sel_func, $tf_title, $tf_phone, $tf_hphone, $tf_mphone, $tf_fax;
  global $sel_market, $tf_email, $tf_email2, $cb_mailok, $cb_priv, $ta_com, $ta_com2, $ta_com3, $tf_date;
  global $tf_datecomment , $sel_usercomment , $ta_add_comment ;
  global $tf_datecomment2, $sel_usercomment2, $ta_add_comment2;
  global $tf_datecomment3, $sel_usercomment3, $ta_add_comment3;
  global $cb_archive;
  global $param_company, $param_contact, $hd_usercreate;
  global $company_name, $company_new_name, $company_new_id;
  global $tf_func, $tf_label, $tf_lang, $tf_header, $cb_default;
  global $popup, $ext_action, $ext_url, $ext_id, $ext_target, $ext_title;
  global $tf_category1_label, $tf_category1_code, $sel_category1; 
  global $tf_category2_label, $tf_category2_code, $sel_category2; 
  global $tf_category3_label, $tf_category3_code, $sel_category3; 
  global $tf_category4_label, $tf_category4_code, $sel_category4; 
  global $tf_category5_label, $tf_category5_code, $sel_category5; 

  if (isset ($param_contact)) $contact["id"] = $param_contact;
  if (isset ($view)) $contact["view"] = $view;
  if (isset ($tf_category1_label)) $contact["category1_label"] = $tf_category1_label;
  if (isset ($tf_category1_code)) $contact["category1_code"] = $tf_category1_code;
  if (isset ($sel_category1)) $contact["category1"] = $sel_category1;
  if (isset ($tf_category2_label)) $contact["category2_label"] = $tf_category2_label;
  if (isset ($tf_category2_code)) $contact["category2_code"] = $tf_category2_code;
  if (isset ($sel_category2)) $contact["category2"] = $sel_category2;
  if (isset ($tf_category3_label)) $contact["category3_label"] = $tf_category3_label;
  if (isset ($tf_category3_code)) $contact["category3_code"] = $tf_category3_code;
  if (isset ($sel_category3)) $contact["category3"] = $sel_category3;
  if (isset ($tf_category4_label)) $contact["category4_label"] = $tf_category4_label;
  if (isset ($tf_category4_code)) $contact["category4_code"] = $tf_category4_code;
  if (isset ($sel_category4)) $contact["category4"] = $sel_category4;
  if (isset ($tf_category5_label)) $contact["category5_label"] = $tf_category5_label;
  if (isset ($tf_category5_code)) $contact["category5_code"] = $tf_category5_code;
  if (isset ($sel_category5)) $contact["category5"] = $sel_category5;
  if (isset ($hd_usercreate)) $contact["usercreate"] = $hd_usercreate;
  if (isset ($sel_dsrc)) $contact["datasource"] = $sel_dsrc;
  if (isset ($sel_kind)) $contact["kind"] = $sel_kind;
  if (isset ($sel_market)) $contact["marketing_manager"] = $sel_market;
  if (isset ($tf_date)) $contact["date"] = $tf_date;
  if (isset ($tf_lname)) $contact["lname"] = trim($tf_lname);
  if (isset ($tf_fname)) $contact["fname"] = trim($tf_fname);
  if (isset ($param_company)) $contact["company_id"] = $param_company;
  if (isset ($tf_company)) $contact["company"] = $tf_company;
  if (isset ($company_name)) $contact["company_name"] = $company_name;
  if (isset ($company_new_name)) $contact["comp_new_name"] = $company_new_name;
  if (isset ($company_new_id)) $contact["comp_new_id"] = $company_new_id;
  if (isset ($tf_service)) $contact["service"] = $tf_service;
  if (isset ($tf_ad1)) $contact["ad1"] = $tf_ad1;
  if (isset ($tf_ad2)) $contact["ad2"] = $tf_ad2;
  if (isset ($tf_ad3)) $contact["ad3"] = $tf_ad3;
  if (isset ($tf_zip)) $contact["zip"] = $tf_zip;
  if (isset ($tf_town)) $contact["town"] = get_format_town($tf_town);
  if (isset ($tf_cdx)) $contact["cdx"] = $tf_cdx;
  if (isset ($sel_ctry)) $contact["country"] = $sel_ctry;
  if (isset ($sel_func)) $contact["function"] = $sel_func;
  if (isset ($tf_title)) $contact["title"] = $tf_title;
  if (isset ($tf_phone)) $contact["phone"] = trim($tf_phone);
  if (isset ($tf_hphone)) $contact["hphone"] = trim($tf_hphone);
  if (isset ($tf_mphone)) $contact["mphone"] = trim($tf_mphone);
  if (isset ($tf_fax)) $contact["fax"] = trim($tf_fax);
  if (isset ($tf_email)) $contact["email"] = trim($tf_email);
  if (isset ($tf_email2)) $contact["email2"] = trim($tf_email2);
  if (isset ($cb_archive)) $contact["archive"] = ($cb_archive == 1 ? 1 : 0);
  if (isset ($cb_priv)) $contact["priv"] = ($cb_priv == 1 ? 1 : 0);
  if (isset ($cb_mailok)) $contact["mailok"] = ($cb_mailok == 1 ? 1 : 0);
  if (isset ($ta_com)) $contact["com"] = $ta_com;
  if (isset ($ta_com2)) $contact["com2"] = $ta_com2;
  if (isset ($ta_com3)) $contact["com3"] = $ta_com3;
  if (isset ($tf_datecomment)) $contact["datecomment"] = $tf_datecomment;
  if (isset ($tf_datecomment2)) $contact["datecomment2"] = $tf_datecomment2;
  if (isset ($tf_datecomment3)) $contact["datecomment3"] = $tf_datecomment3;
  if (isset ($sel_usercomment)) $contact["usercomment"] = $sel_usercomment;
  if (isset ($sel_usercomment2)) $contact["usercomment2"] = $sel_usercomment2;
  if (isset ($sel_usercomment3)) $contact["usercomment3"] = $sel_usercomment3;
  if (isset ($ta_add_comment)) $contact["add_comment"] = trim($ta_add_comment);
  if (isset ($ta_add_comment2)) $contact["add_comment2"] = trim($ta_add_comment2);
  if (isset ($ta_add_comment3)) $contact["add_comment3"] = trim($ta_add_comment3);

  // Admin - Function fields
  // $sel_func -> "function" is already set
  if (isset ($tf_func)) $contact["func_label"] = $tf_func;

  // Admin - Kind fields
  if (isset ($tf_label)) $contact["kind_label"] = $tf_label;
  if (isset ($tf_lang)) $contact["kind_lang"] = $tf_lang;
  if (isset ($tf_header)) $contact["kind_header"] = $tf_header;
  if (isset ($cb_default)) $contact["kind_default"] = ($cb_default == 1 ? 1 : 0);

  // External param
  if (isset ($popup)) $contact["popup"] = $popup;
  if (isset ($ext_action)) $contact["ext_action"] = $ext_action;
  if (isset ($ext_url)) $contact["ext_url"] = $ext_url;
  if (isset ($ext_id)) $contact["ext_id"] = $ext_id;
  if (isset ($ext_id)) $contact["id"] = $ext_id;
  if (isset ($ext_target)) $contact["ext_target"] = $ext_target;
  if (isset ($ext_title)) $contact["ext_title"] = $ext_title;

  get_global_param_document($contact);

  display_debug_param($contact);

  return $contact;
}

///////////////////////////////////////////////////////////////////////////////
//  Contact Action 
///////////////////////////////////////////////////////////////////////////////
function get_contact_action() {
  global $contact, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete,$l_header_stats;
  global $l_header_consult,$l_header_vcard, $l_header_display, $l_header_admin;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// ext_get_ids
  $actions["contact"]["ext_get_ids"] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                        );
// Category1 Select 
  $actions["contact"]["ext_get_category1_ids"]  = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );
// Category2 Select 
  $actions["contact"]["ext_get_category2_ids"]  = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );						 
// Category3 Select 
  $actions["contact"]["ext_get_category3_ids"]  = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );						 
// Category4 Select 
  $actions["contact"]["ext_get_category4_ids"]  = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );						 
// Index
  $actions["contact"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/contact/contact_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                        );

// Search
  $actions["contact"]["search"] = array (
    'Url'      => "$path/contact/contact_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    	);

// New
  $actions["contact"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/contact/contact_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('','index','search','new','detailconsult','update','statistics','check_delete','delete','admin','display') 
                                     );

// Detail Consult
 $actions["contact"]["detailconsult"]   = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$contact["id"]."",
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult','detailupdate','check_delete') 
                                    		 );

// Vcard Export
  $actions["contact"]["vcard"] = array (
    'Name'     => $l_header_vcard,
    'Url'      => "$path/contact/contact_index.php?action=vcard&amp;popup=1&amp;param_contact=".$contact["id"]."",
    'Right'    => $cright_read,
    'Privacy'  => true,    
    'Condition'=> array ('detailconsult','detailupdate','update','check_delete') 
                                       );

// Detail Update
  $actions["contact"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/contact/contact_index.php?action=detailupdate&amp;param_contact=".$contact["id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'update','check_delete')
                                     		 );

// Insert
  $actions["contact"]["insert"] = array (
    'Url'      => "$path/contact/contact_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	);

// Update
  $actions["contact"]["update"] = array (
    'Url'      => "$path/contact/contact_index.php?action=update",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	);
// Statistics
  $actions["contact"]["statistics"] = array (
    'Name'     => $l_header_stats,
    'Url'      => "$path/contact/contact_index.php?action=statistics",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                        );
					
// Document Add
  $actions["contact"]["document_add"] = array (
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	);

// Check Delete
  $actions["contact"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/contact/contact_index.php?action=check_delete&amp;param_contact=".$contact["id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update','check_delete') 
                                     	      );

// Delete
  $actions["contact"]["delete"] = array (
    'Url'      => "$path/contact/contact_index.php?action=delete",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	);

// Admin
  $actions["contact"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/contact/contact_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                      		 );

// Function Insert
  $actions["contact"]["function_insert"] = array (
    'Url'      => "$path/contact/contact_index.php?action=function_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Function Update
  $actions["contact"]["function_update"] = array (
    'Url'      => "$path/contact/contact_index.php?action=function_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Function Check Link
  $actions["contact"]["function_checklink"] = array (
    'Url'      => "$path/contact/contact_index.php?action=function_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Function Delete
  $actions["contact"]["function_delete"] = array (
    'Url'      => "$path/contact/contact_index.php?action=function_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Kind Insert
  $actions["contact"]["kind_insert"] = array (
    'Url'      => "$path/contact/contact_index.php?action=kind_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Kind Update
  $actions["contact"]["kind_update"] = array (
    'Url'      => "$path/contact/contact_index.php?action=kind_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Kind Check Link
  $actions["contact"]["kind_checklink"] = array (
    'Url'      => "$path/contact/contact_index.php?action=kind_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Kind Delete
  $actions["contact"]["kind_delete"] = array (
    'Url'      => "$path/contact/contact_index.php?action=kind_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Category Insert
  $actions["contact"]["category1_insert"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category1_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Category Update
  $actions["contact"]["category1_update"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category1_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Category Check Link
  $actions["contact"]["category1_checklink"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category1_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Category Delete
  $actions["contact"]["category1_delete"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category1_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Category Insert
  $actions["contact"]["category2_insert"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category2_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Category Update
  $actions["contact"]["category2_update"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category2_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Category Check Link
  $actions["contact"]["category2_checklink"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category2_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Category Delete
  $actions["contact"]["category2_delete"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category2_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Category Insert
  $actions["contact"]["category3_insert"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category3_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                             );

// Category Update
  $actions["contact"]["category3_update"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category3_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                              );

// Category Check Link
  $actions["contact"]["category3_checklink"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category3_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                                );

// Category Delete
  $actions["contact"]["category3_delete"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category3_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                               );

// Category Insert
  $actions["contact"]["category4_insert"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category4_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                             );

// Category Update
  $actions["contact"]["category4_update"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category4_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                              );

// Category Check Link
  $actions["contact"]["category4_checklink"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category4_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                                );

// Category Delete
  $actions["contact"]["category4_delete"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category4_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                               );
// Category Insert
  $actions["contact"]["category5_insert"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category5_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                             );

// Category Update
  $actions["contact"]["category5_update"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category5_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                              );

// Category Check Link
  $actions["contact"]["category5_checklink"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category5_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                                );

// Category Delete
  $actions["contact"]["category5_delete"] = array (
    'Url'      => "$path/contact/contact_index.php?action=category5_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                               );

// Display
  $actions["contact"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/contact/contact_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );

// Display Preferences
  $actions["contact"]["dispref_display"]	= array (
    'Url'      => "$path/contact/contact_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	        );

// Display Level
  $actions["contact"]["dispref_level"]= array (
    'Url'      => "$path/contact/contact_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      		 );

}


///////////////////////////////////////////////////////////////////////////////
// Contact Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_contact_action() {
  global $contact, $actions, $path;

  $id = $contact["id"];
  if ($id > 0) {
    // Detail Consult
    $actions["contact"]["detailconsult"]["Url"] = "$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$id";
    
    // Detail Update
    $actions["contact"]["detailupdate"]['Url'] = "$path/contact/contact_index.php?action=detailupdate&amp;param_contact=$id";
    $actions["contact"]["detailupdate"]['Condition'][] = 'insert';
    
    // Check Delete
    $actions["contact"]["check_delete"]['Url'] = "$path/contact/contact_index.php?action=check_delete&amp;param_contact=$id";
    $actions["contact"]["check_delete"]['Condition'][] = 'insert';
  }
}

</script>
