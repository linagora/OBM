<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : deal_index.php                                               //
//     - Desc : Deal Index File                                              //
// 1999-04-10 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the deal search form
// - search          -- search fields  -- show the result set of search
// - new             -- $param_company -- show the new deal form
// - detailconsult   -- $param_deal    -- show the deal detail
// - detailupdate    -- $param_deal    -- show the deal detail form
// - quick_detail    -- $param_deal    -- show the deal quick detail form
// - insert          -- form fields    -- insert the deal
// - update          -- form fields    -- update the deal
// - quick_update    -- form fields    -- update (quick) the deal
// - check_delete    -- $param_deal    -- check links before delete
// - delete          -- $param_deal    -- delete the deal
// - affect          -- $param_deal    -- show the new parent deal form
// - affect_update   -- $param_deal    -- affect the deal to the parentdeal
// - dashboard       -- form fields    -- Display the dashboard screen
// - document_add    -- form fields    -- Add a doucment
// - admin           --                -- admin index (kind)
// - kind_insert     -- form fields    -- insert the kind
// - kind_update     -- form fields    -- update the kind
// - kind_checklink  -- $sel_kind      -- check if kind is used
// - kind_delete     -- $sel_kind      -- delete the kind
// - status_insert   -- form fields    -- insert the status
// - status_update   -- form fields    -- update the status
// - status_checklink-- $sel_status    -- check if status is used
// - status_delete   -- $sel_status    -- delete the status
// - category1_insert    -- form fields    -- insert the category
// - category1_update    -- form fields    -- update the category
// - category1_checklink -- $sel_cat       -- check if category is used
// - category1_delete    -- $sel_cat       -- delete the category
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
// - parent_search        -- search fields  -- show the result set of search
// - parent_new           -- search fields  -- show the result set of search
// - parent_detailconsult -- $param_parent  -- show the parent detail
// - parent_detailupdate  -- $param_parent  -- show the parent detail form
// - parent_insert        -- form fields    -- insert the deal
// - parent_update        -- form fields    -- update the parent
// - parent_delete        -- $param_parent  -- delete the parent
// External API ---------------------------------------------------------------
// - ext_get_id      -- $ext_params    -- select a deal (return id) 
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "deal";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");

page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("deal_query.inc");
require("deal_display.inc");
require("deal_js.inc");
require_once("$obminclude/of/of_extmod.inc");
require_once("$obminclude/of/of_category.inc");
require_once("$obminclude/javascript/calendar_js.inc");
$uid = $auth->auth["uid"];

if ($action == "") $action = "index";
$deal = get_param_deal();
get_deal_action();
$perm->check_permissions($module, $action);
if (! check_privacy($module, "Deal", $action, $deal["id"], $uid)) {
  $display["msg"] = display_err_msg($l_error_visibility);
  $action = "index";
} else {
  update_last_visit("deal", $param_deal, $action);
  update_last_visit("parentdeal", $param_parent, $action);
}
page_close();


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

// when searching deals belonging to a parent, we display the parent
if (($action == "search") && ($deal["parent"])) {
///////////////////////////////////////////////////////////////////////////////
  $action = "parent_detailconsult";
}


///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_id") {
  $display["search"] = dis_deal_search_form($deal);
  if ($set_display == "yes") {
    $display["result"] = dis_deal_search_list($deal);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "ext_get_category1_ids") {
  $extra_css = "category.css";
  $display["detail"] = of_category_dis_tree("deal", "category1", $deal, $action);

///////////////////////////////////////////////////////////////////////////////
// Normal calls
///////////////////////////////////////////////////////////////////////////////
} elseif (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_deal_index();
  if ($set_display == "yes") {
    $display["detail"] = dis_deal_search_list($deal);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }
 
} elseif ($action == "search") { // tester si hd_parent mis ??? pour form :oui
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_deal_search_form($deal);
  $display["result"] = dis_deal_search_list($deal);
  
} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_deal_form($deal);

} elseif ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_deal_consult($deal);

} elseif ($action == "quick_detail") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_deal_quick_form($deal);
  
} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_deal_form($deal);

} elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_deal_form("", $deal)) {
    $deal["id"] = run_query_deal_insert($deal);
    if ($deal["id"]) {
      $display["msg"] .= display_ok_msg("$l_deal : $l_insert_ok");
      $display["detail"] = dis_deal_consult($deal);
    } else {
      $display["msg"] .= display_err_msg("$l_deal : $l_insert_error : $err_msg");
      $display["search"] = dis_deal_index($deal);
    }
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    $display["detail"] = dis_deal_form($deal);
  }

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_deal_form("", $deal)) {
    $retour = run_query_deal_update($deal);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_deal : $l_update_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_deal : $l_update_error");
    }
    $display["detail"] = dis_deal_consult($deal);
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    $display["detail"] = dis_deal_form($deal);

    // If deal archived, we look about archiving the parentdeal ?????
    if ($cb_arc_aff == "archives") {
      $obm_q = run_query_deal_detail($deal["id"]);
      $obm_q->next_record();
      $deal["parent"] = $obm_q->f("deal_parentdeal_id");
      run_query_update_archive($deal["id"], $deal["parent"]);
    }
  }

} elseif ($action == "quick_update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_deal_quick_form($deal)) {
    $retour = run_query_deal_quick_update($deal);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_deal : $l_update_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_deal : $l_update_error");
    }
    $display["detail"] = dis_deal_consult($deal);
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    $display["detail"] = dis_deal_quick_form($deal);
  }

} elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_deal($deal["id"])) {
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_deal_can_delete_deal($deal["id"]);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_deal_consult($deal);
  }

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_deal($deal["id"])) {
    $retour = run_query_deal_delete($deal["id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_deal : $l_delete_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_deal : $l_delete_error");
    }
    $display["search"] = dis_deal_index();
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_deal_consult($deal);
  }

} elseif ($action == "dashboard") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_deal_dashboard_index($deal);
  $display["detail"] .= dis_deal_dashboard_view($deal);

} elseif ($action == "dashboard_list") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_deal_dashboard_index($deal);
  $display["detail"] .= dis_deal_dashboard_list($deal);

} elseif ($action == "document_add") {
///////////////////////////////////////////////////////////////////////////////
  if ($deal["doc_nb"] > 0) {
    $nb = run_query_global_insert_documents($deal, "deal");
    $display["msg"] .= display_ok_msg("$nb $l_document_added");
  } else {
    $display["msg"] .= display_err_msg($l_no_document_added);
  }
  $display["detail"] = dis_deal_consult($deal);

} elseif ($action == "admin") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_deal_admin_index();
  
} elseif ($action == "category1_insert") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("deal", "category1", $deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category1)." : $l_c_insert_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category1)." : $l_c_insert_error");
  }
  $display["detail"] = dis_deal_admin_index();

} elseif ($action == "category1_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("deal", "category1", $deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category1)." : $l_c_update_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category1)." : $l_c_update_error");
  }
  $display["detail"] = dis_deal_admin_index();

} elseif ($action == "category1_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("deal", "category1", $deal);

} elseif ($action == "category1_delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("deal", "category1", $deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category1)." : $l_c_delete_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category1)." : $l_c_delete_error");
  }
  $display["detail"] = dis_deal_admin_index();

} elseif ($action == "kind_insert") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_deal_kind_insert($deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_kind : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_kind : $l_insert_error");
  }
  $display["detail"] = dis_deal_admin_index();
  
} elseif ($action == "kind_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_deal_kind_update($deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_kind : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_kind : $l_update_error");
  }
  $display["detail"] = dis_deal_admin_index();
  
} elseif ($action == "kind_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_deal_kind_links($deal["kind"]);
  
} elseif ($action == "kind_delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_deal_kind_delete($deal["kind"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_kind : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_kind : $l_delete_error");
  }
  $display["detail"] = dis_deal_admin_index();

} elseif ($action == "status_insert") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_deal_status_insert($deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_status : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_status : $l_insert_error");
  }
  $display["detail"] = dis_deal_admin_index();

} elseif ($action == "status_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_deal_status_update($deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_status : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_status : $l_update_error");
  }
  $display["detail"] = dis_deal_admin_index();

} elseif ($action == "status_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_deal_status_links($deal["status"]);

} elseif ($action == "status_delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_deal_status_delete($deal["status"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_status : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_status : $l_delete_error");
  }
  $display["detail"] = dis_deal_admin_index();

} elseif ($action == "role_insert") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("DealCompany", "role", $deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_role : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_role : $l_insert_error");
  }
  $display["detail"] = dis_deal_admin_index();
  
} elseif ($action == "role_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("DealCompany", "role", $deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_role : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_role : $l_update_error");
  }
  $display["detail"] = dis_deal_admin_index();
  
} elseif ($action == "role_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("DealCompany", "role", $deal, "mono");
  
} elseif ($action == "role_delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("DealCompany", "role", $deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_role : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_role : $l_delete_error");
  }
  $display["detail"] = dis_deal_admin_index();


///////////////////////////////////////////////////////////////////////////////
// -- Actions about ParentDeal -- 
///////////////////////////////////////////////////////////////////////////////


} elseif ($action == "parent_search") {
///////////////////////////////////////////////////////////////////////////////
  $usr_q = run_query_deal_manager();
  $display["search"] = html_deal_parentdeal_search_form($deal, $usr_q);
  $display["result"] = dis_deal_parentdeal_search_list($deal);

} elseif ($action == "parent_new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_deal_parentdeal_form($action, $deal);

} elseif ($action == "parent_detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_deal_parentdeal_consult($deal);

} elseif ($action == "parent_detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_deal_parentdeal_form($action, $deal);
  
} elseif ($action == "parent_insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_deal_parent_form("", $deal)) {
    $retour = run_query_deal_insert_parentdeal($deal);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_parentdeal : $l_insert_ok"); 
    } else {
      $display["msg"] .= display_err_msg($err_msg);
    }
    $display["search"] = html_deal_parentdeal_search_form($deal, run_query_deal_manager(1));
  } else {
    $display["msg"] .= display_warn_msg($err_msg);
    $display["search"] = html_deal_parentdeal_search_form($deal, run_query_deal_manager(1));
  }
  
} elseif ($action == "parent_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_deal_can_delete_parentdeal($deal["parent"])) {
    run_query_deal_parentdeal_delete($deal["parent"]); 
    $display["msg"] .= display_ok_msg("$l_parentdeal : $l_delete_ok"); 
    $display["search"] = html_deal_parentdeal_search_form($deal, run_query_deal_manager(1));
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete_parent, false);
    $display["search"] = dis_deal_parentdeal_consult($deal);
  }
  
} elseif  ($action == "parent_update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_deal_parent_form("", $deal)) {
    $retour = run_query_deal_parentdeal_update($deal);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_parentdeal : $l_update_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_parentdeal : $l_update_error");
    }
    $display["detail"] = dis_deal_parentdeal_consult($deal);
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    $display["detail"] = dis_deal_parentdeal_form($action, $deal);
  }

} elseif ($action == "affect") {
///////////////////////////////////////////////////////////////////////////////
  $parent_q = run_query_deal_search_parentdeal('');
  $display["detail"] = html_deal_affect($parent_q, $deal["id"]);

} elseif ($action == "affect_update") {
///////////////////////////////////////////////////////////////////////////////
  if ($deal["id"] > 0) {
    run_query_deal_affect_deal_parentdeal($deal["id"], $deal);
    $display["msg"] .= display_ok_msg($l_updateaffect_ok); 
    $display["detail"] = dis_deal_consult($deal);
  }

} elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($uid,"deal",1);
  $prefs_parent = get_display_pref($uid,"parentdeal",1);
  $display["detail"] = dis_deal_display_pref($prefs, $prefs_parent);
  
} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($uid,"deal",1);
  $prefs_parent = get_display_pref($uid,"parentdeal",1);
  $display["detail"] = dis_deal_display_pref($prefs, $prefs_parent);

} else if ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($uid,"deal",1);
  $prefs_parent = get_display_pref($uid,"parentdeal",1);
  $display["detail"] = dis_deal_display_pref($prefs, $prefs_parent);
  
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
// Update actions url in case some values have been updated (id after insert) 
update_deal_action();
if (! $popup) {
  $display["header"] = display_menu($module);
}
$display["head"] = display_head($l_deal);
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $deal hash, Deal parameters transmited
// returns : $deal hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_deal() {
  global $year, $tf_num, $tf_label, $param_parent, $sel_kind, $sel_tt;
  global $param_company, $sel_contact1, $sel_contact2, $sel_market, $sel_tech;
  global $tf_amount, $sel_status, $tf_datealarm, $ta_com, $tf_commission;
  global $tf_datebegin, $tf_dateend, $tf_dateprop, $tf_dateexpected;
  global $tf_datecomment, $sel_usercomment, $ta_add_comment, $rd_mail_comment;
  global $tf_plabel, $sel_pmanager, $cb_archive, $tf_todo, $cb_privacy;
  global $hd_company_ad1, $hd_company_zip, $hd_company_town;
  global $tf_company_name, $tf_zip,$sel_manager, $tf_date_after, $tf_date_before;
  global $sel_pmarket, $sel_ptech, $ta_pcom, $sel_parent;
  global $tf_category1_label, $tf_category1_code, $sel_category1;
  global $tf_role_label, $tf_role_code, $sel_role;
  global $param_deal, $param_contact, $param_lead, $sel_source, $tf_source;
  global $tf_kind, $rd_kind_inout, $tf_status, $tf_order, $tf_hitrate;
  global $popup, $ext_action, $ext_url, $ext_id, $ext_title, $ext_target;  
  global $ext_widget, $ext_widget_text, $new_order, $order_dir, $dash_view;
 
  if (isset ($popup)) $deal["popup"] = $popup;
  if (isset ($ext_action)) $deal["ext_action"] = $ext_action;
  if (isset ($ext_url)) $deal["ext_url"] = $ext_url;
  if (isset ($ext_id)) $deal["ext_id"] = $ext_id;
  if (isset ($ext_title)) $deal["ext_title"] = stripslashes(urldecode($ext_title));
  if (isset ($ext_target)) $deal["ext_target"] = $ext_target;
  if (isset ($ext_widget)) $deal["ext_widget"] = $ext_widget;
  if (isset ($ext_widget_text)) $deal["ext_widget_text"] = $ext_widget_text;

  if (isset ($dash_view)) $deal["dash_view"] = $dash_view;

  if (isset ($new_order)) $deal["new_order"] = $new_order;
  if (isset ($order_dir)) $deal["order_dir"] = $order_dir;

  // Deal fields
  if (isset ($param_deal)) $deal["id"] = $param_deal;
  if (isset ($tf_num)) $deal["num"] = $tf_num;
  if (isset ($tf_label)) $deal["label"] = $tf_label;
  if (isset ($tf_datebegin)) $deal["datebegin"] = $tf_datebegin;
  if (isset ($tf_dateend)) $deal["dateend"] = $tf_dateend;
  if (isset ($tf_dateprop)) $deal["dateproposal"] = $tf_dateprop;
  if (isset ($tf_dateexpected)) $deal["dateexpected"] = $tf_dateexpected;
  if (isset ($tf_datealarm)) $deal["datealarm"] = $tf_datealarm;
  if (isset ($param_parent)) $deal["parent"] = $param_parent;
  if (isset ($sel_source)) $deal["source_id"] = $sel_source;
  if (isset ($tf_source)) $deal["source"] = $tf_source;
  if (isset ($sel_kind)) $deal["kind"] = $sel_kind;
  if (isset ($param_company)) $deal["company"] = $param_company;
  if (isset ($param_contact)) $deal["contact_id"] = $param_contact;
  if (isset ($sel_contact1)) $deal["contact1"] = $sel_contact1;
  if (isset ($sel_contact2)) $deal["contact2"] = $sel_contact2;
  if (isset ($sel_market)) $deal["market"] = $sel_market;
  if (isset ($sel_tech)) $deal["tech"] = $sel_tech;
  if (isset ($tf_commission)) $deal["commission"] = $tf_commission;
  if (isset ($tf_amount)) $deal["amount"] = $tf_amount;
  if (isset ($tf_hitrate)) $deal["hitrate"] = $tf_hitrate;
  if (isset ($sel_status)) $deal["status"] = $sel_status;
  if (isset ($tf_category1_label)) $deal["category1_label"] = $tf_category1_label;
  if (isset ($tf_category1_code)) $deal["category1_code"] = $tf_category1_code;
  if (isset ($sel_category1)) $deal["category1"] = $sel_category1;
  if (isset ($tf_role_label)) $deal["role_label"] = $tf_role_label;
  if (isset ($tf_role_code)) $deal["role_code"] = $tf_role_code;
  if (isset ($sel_role)) $deal["role"] = $sel_role;
  if (isset ($cb_archive)) {
    $deal["archive"] = $cb_archive;
  }
  if (isset ($tf_todo)) $deal["todo"] = $tf_todo;
  if (isset ($cb_privacy)) { $deal["privacy"] = ($cb_privacy == 1 ? 1 : 0); };
  if (isset ($ta_com)) $deal["com"] = $ta_com;
  if (isset ($tf_datecomment)) $deal["datecomment"] = $tf_datecomment;
  if (isset ($sel_usercomment)) $deal["usercomment"] = $sel_usercomment;
  if (isset ($ta_add_comment)) $deal["add_comment"] = trim($ta_add_comment);
  if (isset ($rd_mail_comment)) $deal["mail_comment"] = trim($rd_mail_comment);

  if (isset ($year)) $deal["year"] = $year;

  // sel_tt
  if (is_array($sel_tt)) {
    while ( list( $key, $value ) = each( $sel_tt ) ) {
      // sel_tt contains select infos (data-tt-$id)
      if (strcmp(substr($value, 0, 8),"data-tt-") == 0) {
	$data = explode("-", $value);
	$id = $data[2];
	$deal["sel_tt"][] = $id;
      } else {
	// sel_tt contains ids
	$deal["sel_tt"][] = $value;
      }	
    }
  } else {
    if (isset ($sel_tt)) $deal["tasktype"] = $sel_tt;
  }

  // Parent Deal fields
  if (isset ($tf_plabel)) $deal["plabel"] = $tf_plabel;
  if (isset ($sel_pmarket)) $deal["pmarket"] = $sel_pmarket;
  if (isset ($sel_ptech)) $deal["ptech"] = $sel_ptech;
  if (isset ($sel_pmanager)) $deal["pmanager"] = $sel_pmanager; // in search
  if (isset ($cb_parchive)) {
    $deal["parchive"] = $cb_parchive;
  } else {
    $deal["parchive"] = "0";
  }
  if (isset ($ta_pcom)) $deal["pcom"] = $ta_pcom;
  if (isset ($sel_parent)) $deal["sel_parent"] = $sel_parent;

  if (isset ($param_lead)) $deal["lead_id"] = $param_lead;

  // Search fields
  if (isset ($tf_company_name)) $deal["company_name"] = $tf_company_name;
  if (isset ($tf_zip)) $deal["company_zip"] = $tf_zip;
  if (isset ($sel_manager)) $deal["manager"] = $sel_manager;
  if (isset ($tf_date_after)) $deal["date_after"] = $tf_date_after;
  if (isset ($tf_date_before)) $deal["date_before"] = $tf_date_before;

  // Company infos (with company_name)
  if (isset ($hd_company_ad1)) $deal["company_ad1"] = $hd_company_ad1;
  if (isset ($hd_company_zip)) $deal["company_zip"] = $hd_company_zip;
  if (isset ($hd_company_town)) $deal["company_town"] = $hd_company_town;

  // Admin - Kind fields
  // $sel_kind -> "kind" is already set
  if (isset ($tf_kind)) $deal["kind_label"] = $tf_kind;
  if (isset ($rd_kind_inout)) $deal["kind_inout"] = $rd_kind_inout;

  // Admin - Status fields
  // $sel_status -> "status" is already set
  if (isset ($tf_status)) $deal["status_label"] = $tf_status;
  $deal["status_order"] = (isset($tf_order) ? $tf_order : "0");

  get_global_param_document($deal);
  
  display_debug_param($deal);

  return $deal;
}


///////////////////////////////////////////////////////////////////////////////
// Deal Actions 
///////////////////////////////////////////////////////////////////////////////
function get_deal_action() {
  global $deal, $actions, $path;
  global $l_header_find,$l_header_new_f,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display,$l_header_admin;
  global $l_header_new_child, $l_header_new_parent, $l_header_quickupdate;
  global $l_header_dashboard, $l_deal_select_company;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

  // Index
  $actions["deal"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/deal/deal_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

  // Search
  $actions["deal"]["search"] = array (
    'Url'      => "$path/deal/deal_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     );

  // Parent Search
  $actions["deal"]["parent_search"] = array (
    'Url'      => "$path/deal/deal_index.php?action=parent_search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     );

  // New
  $actions["deal"]["new"] = array (
    'Name'     => $l_header_new_f,
    'Url'      => "$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_deal_select_company)."&amp;ext_url=".urlencode("$path/deal/deal_index.php?action=new&amp;param_company="),
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Condition'=> array ('all') 
                                  );

  // New Child
  $ret_url = urlencode("$path/deal/deal_index.php?action=new&amp;param_parent=". $deal["parent"] . "&amp;sel_market=" . $deal["pmarket"] . "&amp;sel_tech=" . $deal["ptech"] . "&amp;param_company=");
  $actions["deal"]["new_child"] = array (
    'Name'     => $l_header_new_child,
    'Url'      => "$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_deal_select_company)."&amp;ext_url=$ret_url",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Condition'=> array ('parent_detailconsult') 
                                  );

  // Parent New
  $actions["deal"]["parent_new"] = array (
    'Name'     => $l_header_new_parent,
    'Url'      => "$path/deal/deal_index.php?action=parent_new",
    'Right'    => $cright_write,
    'Condition'=> array ('','search','parent_search','index','detailconsult',
                         'parent_detailconsult','parent_insert',
                         'admin','display') 
                                         );

  // Detail Consult
  $actions["deal"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/deal/deal_index.php?action=detailconsult&amp;param_deal=".$deal["id"],
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('detailupdate', 'update', 'quick_detail', 'quick_update') 
                                    	    );

  // Quick Detail
  $actions["deal"]["quick_detail"] = array (
    'Name'     => $l_header_quickupdate,
    'Url'      => "$path/deal/deal_index.php?action=quick_detail&amp;param_deal=".$deal["id"],
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'quick_update')
                                    	    );

  // Detail Update
  $actions["deal"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/deal/deal_index.php?action=detailupdate&amp;param_deal=".$deal["id"],
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'update', 'quick_detail', 'quick_update')
                                     	    );
					    
  // Convert from Lead
  $actions["deal"]["lead_convert"] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	    );
					    
  //  Update
  $actions["deal"]["update"] = array (
    'Url'      => "$path/deal/deal_index.php?action=update&amp;param_deal=".$deal["id"],
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	    );

  //  Quick Update
  $actions["deal"]["quick_update"] = array (
    'Url'      => "$path/deal/deal_index.php?action=quick_update&amp;param_deal=".$deal["id"],
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	    );
					    
  // Parent Detail Consult
  $actions["deal"]["parent_detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/deal/deal_index.php?action=parent_detailconsult&amp;param_parent=".$deal["parent"],
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('parent_detailupdate', 'parent_update') 
                                    	    );

  // Parent Detail Update
  $actions["deal"]["parent_detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/deal/deal_index.php?action=parent_detailupdate&amp;param_parent=".$deal["parent"],
    'Right'    => $cright_write,
    'Condition'=> array ('parent_detailconsult', 'parent_update') 
                                     		  );
                                                                                                                                                             
  // Parent Update
  $actions["deal"]["parent_update"] = array (
    'Url'      => "$path/deal/deal_index.php?action=parent_update&amp;param_parent=".$deal["parent"],
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                                  );

  // Insert
  $actions["deal"]["insert"] = array (
    'Url'      => "$path/deal/deal_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

  // Parent insert
  $actions["deal"]["parent_insert"] = array (
    'Url'      => "$path/deal/deal_index.php?action=parent_insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                            );

  // Check Delete
  $actions["deal"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/deal/deal_index.php?action=check_delete&amp;param_deal=".$deal["id"],
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'update') 
                                     );

  // Delete
  $actions["deal"]["delete"] = array (
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     );

  // Dashboard
  $actions["deal"]["dashboard"] = array (
    'Name'     => $l_header_dashboard,
    'Url'      => "$path/deal/deal_index.php?action=dashboard",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                     );

  // Dashboard List
  $actions["deal"]["dashboard_list"] = array (
    'Url'      => "$path/deal/deal_index.php?action=dashboard_list",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('None') 
                                     );

  // Document add
  $actions["deal"]["document_add"] = array (
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
  );
  
  // Parent Delete
  $actions["deal"]["parent_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/deal/deal_index.php?action=parent_delete&amp;param_parent=".$deal["parent"],
    'Right'    => $cright_write,
    'Condition'=> array ('parent_detailconsult') 
                                     	     );

  // Affect
  $actions["deal"]["affect"] = array (
    'Url'      => "$path/deal/deal_index.php?action=affect&amp;param_parent=".$deal["parent"]."&amp;param_deal=".$deal["id"],
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	     );

  // Affect Update
  $actions["deal"]["affect_update"] = array (
    'Url'      => "$path/deal/deal_index.php?action=affect_update&amp;sel_parent=".$deal["parent"]."&amp;param_deal=".$deal["id"],
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	     );

  // Admin  
  $actions["deal"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/deal/deal_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                    );

  // Kind Insert
  $actions["deal"]["kind_insert"] = array (
    'Url'      => "$path/deal/deal_index.php?action=kind_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                           );

  // Kind Update
  $actions["deal"]["kind_update"] = array (
    'Url'      => "$path/deal/deal_index.php?action=kind_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                           );

  // Kind checklink
  $actions["deal"]["kind_checklink"] = array (
    'Url'      => "$path/deal/deal_index.php?action=kind_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                             );

  // Kind delete
  $actions["deal"]["kind_delete"] = array (
    'Url'      => "$path/deal/deal_index.php?action=kind_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                          );

  // Status Insert 
  $actions["deal"]["status_status"] = array (
    'Url'      => "$path/deal/deal_index.php?action=status_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                          );

  // Status Update 
  $actions["deal"]["status_update"] = array (
    'Url'      => "$path/deal/deal_index.php?action=status_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                          );

  // Status Checklink 
  $actions["deal"]["status_checklink"] = array (
    'Url'      => "$path/deal/deal_index.php?action=status_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                          );

  // Status Delete 
  $actions["deal"]["status_delete"] = array (
    'Url'      => "$path/deal/deal_index.php?action=status_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                          );

  // Role Insert
  $actions["deal"]["role_insert"] = array (
    'Url'      => "$path/deal/deal_index.php?action=role_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                           );

  // Role Update
  $actions["deal"]["role_update"] = array (
    'Url'      => "$path/deal/deal_index.php?action=role_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                           );

  // Role checklink
  $actions["deal"]["role_checklink"] = array (
    'Url'      => "$path/deal/deal_index.php?action=role_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                             );

  // Role delete
  $actions["deal"]["role_delete"] = array (
    'Url'      => "$path/deal/deal_index.php?action=role_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                          );

  // Display
  $actions["deal"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/deal/deal_index.php?action=display",
    'Right'    => $cright_read, 
    'Condition'=> array ('all') 
                                      );

  // Display Preference
  $actions["deal"]["dispref_display"] = array (
    'Url'      => "$path/deal/deal_index.php?action=dispref_display",
    'Right'    => $cright_read, 
    'Condition'=> array ('None') 
                                      );

  // Display Level
  $actions["deal"]["dispref_level"] = array (
    'Url'      => "$path/deal/deal_index.php?action=dispref_level",
    'Right'    => $cright_read, 
    'Condition'=> array ('None') 
                                      );

  // Category Select 
  $actions["deal"]["ext_get_category1_ids"]  = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

  // Category Check Link
  $actions["deal"]["category1_checklink"] = array (
    'Url'      => "$path/contact/deal_index.php?action=category1_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                    );                

// Category Update
  $actions["deal"]["category1_update"] = array (
    'Url'      => "$path/contact/deal_index.php?action=category1_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Category Insert
  $actions["deal"]["category1_insert"] = array (
    'Url'      => "$path/contact/deal_index.php?action=category1_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Category Delete
  $actions["deal"]["category1_delete"] = array (
    'Url'      => "$path/contact/deal_index.php?action=category1_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

  // External call : select one deal
  $actions["deal"]["ext_get_id"] = array (
    'Url'      => "$path/deal/deal_index.php?action=ext_get_id",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     );
				     
}


///////////////////////////////////////////////////////////////////////////////
// Deal Actions updates (after processing, before displaying menu)  
///////////////////////////////////////////////////////////////////////////////
function update_deal_action() {
  global $deal, $actions, $path;

  $id = $deal["id"];
  if ($id > 0) {
    // Detail Consult
    $actions["deal"]["detailconsult"]['Url'] = "$path/deal/deal_index.php?action=detailconsult&amp;param_deal=$id";
    $actions["deal"]["detailonsult"]['Condition'][] = 'insert';

    // Detail Update
    $actions["deal"]["detailupdate"]['Url'] = "$path/deal/deal_index.php?action=detailupdate&amp;param_deal=$id";
    $actions["deal"]["detailupdate"]['Condition'][] = 'insert';
    
    // Quick Detail
    $actions["deal"]["quick_detail"]['Url'] = "$path/deal/deal_index.php?action=quick_detail&amp;param_deal=$id";
    $actions["deal"]["quick_detail"]['Condition'][] = 'insert';

    // Check Delete
    $actions["deal"]["check_delete"]['Url'] = "$path/deal/deal_index.php?action=check_delete&amp;param_deal=$id";
    $actions["deal"]["check_delete"]['Condition'][] = 'insert';
  }

  $pid = $deal["parent"];
  if ($pid > 0) {
    // Parent Detail Update
    $actions["deal"]["parent_detailupdate"]['Url'] = "$path/deal/deal_index.php?action=parent_detailupdate&amp;param_parent=$pid";

    // Parent Check Delete
    $actions["deal"]["parent_delete"]['Url'] = "$path/deal/deal_index.php?action=parent_delete&amp;param_parent=$pid";
  }


}

?>