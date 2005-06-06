<script language="php">
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
// - stats           -- form fields    -- Display the stats screen
// - document_add    -- form fields    -- Add a doucment
// - admin           --                -- admin index (kind)
// - kind_insert     -- form fields    -- insert the kind
// - kind_update     -- form fields    -- update the kind
// - kind_checklink  -- $sel_kind      -- check if kind is used
// - kind_delete     -- $sel_kind      -- delete the kind
// - status_insert   -- form fields    -- insert the status
// - status_update   -- form fields    -- update the status
// - status_checklink-- $sel_state     -- check if status is used
// - status_delete   -- $sel_state     -- delete the status
// - cat_insert      -- form fields    -- insert the category
// - cat_update      -- form fields    -- update the category
// - cat_checklin k  -- $sel_cat       -- check if category is used
// - cat_delete      -- $sel_cat       -- delete the category
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

///////////////////////////////////////////////////////////////////////////////
// Session,Auth,Perms Management                                             //
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
  require("deal_js.inc");
  $display["search"] = dis_deal_search_form($deal);
  if ($set_display == "yes") {
    $display["result"] = dis_deal_search_list($deal);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "ext_get_cat_ids") {
  $extra_css = "category.css";
  require("deal_js.inc");
  $display["detail"] =  html_category_list($contact);


///////////////////////////////////////////////////////////////////////////////
// Normal calls
///////////////////////////////////////////////////////////////////////////////
} elseif (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $display["search"] = dis_deal_index();
  if ($set_display == "yes") {
    $display["detail"] = dis_deal_search_list($deal);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }
 
} elseif ($action == "search")  { // tester si hd_parent mis ??? pour form :oui
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $display["search"] = dis_deal_search_form($deal);
  $display["result"] = dis_deal_search_list($deal);
  
} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $display["detail"] = dis_deal_form($deal);

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_deal_consult($deal);

} elseif ($action == "quick_detail")  {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $display["detail"] = dis_deal_quick_form($deal);
  
} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $display["detail"] = dis_deal_form($deal);

} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_deal_form("", $deal)) {
    $deal["id"] = run_query_insert($deal);
    if ($deal["id"]) {
      $display["msg"] .= display_ok_msg($l_insert_ok);
      $display["detail"] = dis_deal_consult($deal);
    } else {
      $display["msg"] .= display_err_msg("$l_insert_error : $err_msg");
      $display["search"] = dis_deal_index($deal);
    }
  } else {
    require("deal_js.inc");
    $display["msg"] .= display_err_msg($err_msg);
    $display["detail"] = dis_deal_form($deal);
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_deal_form("", $deal)) {
    $retour = run_query_update($deal);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok);
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
    }
    $display["detail"] = dis_deal_consult($deal);
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    $display["detail"] = dis_deal_form($deal);

    // If deal archived, we look about archiving the parentdeal ?????
    if ($cb_arc_aff == "archives") {
      $obm_q = run_query_detail($deal["id"]);
      $obm_q->next_record();
      $deal["parent"] = $obm_q->f("deal_parentdeal_id");
      run_query_update_archive($deal["id"], $deal["parent"]);
    }
  }

} elseif ($action == "quick_update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_deal_quick_form($deal)) {
    $retour = run_query_quick_update($deal);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok);
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
    }
    $display["detail"] = dis_deal_consult($deal);
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    $display["detail"] = dis_deal_quick_form($deal);
  }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $display["detail"] = dis_check_links($deal["id"]);

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_delete($deal["id"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_delete_error);
  }
  $display["search"] = dis_deal_index();
  
} elseif ($action == "stats")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_deal_potential($deal);
  $display["detail"] .= dis_deal_stats($deal);

} elseif ($action == "document_add")  {
///////////////////////////////////////////////////////////////////////////////
  if ($deal["doc_nb"] > 0) {
    $nb = run_query_insert_documents($deal, "deal");
    $display["msg"] .= display_ok_msg("$nb $l_document_added");
  } else {
    $display["msg"] .= display_err_msg($l_no_document_added);
  }
  $display["detail"] = dis_deal_consult($deal);

}  elseif ($action == "cat_insert")  {
///////////////////////////////////////////////////////////////////////////////
     $retour = run_query_cat_insert($deal);
    if ($retour) {
    $display["msg"] .= display_ok_msg($l_cat_insert_ok);
    } else {
    $display["msg"] .= display_err_msg($l_cat_insert_error);
    }
    require("deal_js.inc");
    $display["detail"] .= html_deal_admin_form(run_query_dealtype(),run_query_dealstatus());


}  elseif ($action == "cat_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_cat_update($deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_cat_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_cat_update_error);
  }
  require("deal_js.inc");
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_dealstatus());

} elseif ($action == "cat_checklink")  {
///////////////////////////////////////////////////////////////////////////////  
$display["detail"] .= dis_cat_links($deal);
 
} elseif ($action == "cat_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_cat_delete($deal["category"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_cat_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_cat_delete_error);
  }
  require("deal_js.inc");
  $display["detail"] .= html_deal_admin_form(run_query_dealtype(),run_query_dealstatus());

} elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $pref_q = run_query_display_pref($uid,"deal",1);
  $pref_parent_q = run_query_display_pref($uid,"parentdeal",1);
  $pref_invoice = run_query_display_pref ($uid,"invoice",1);
  $display["detail"] = dis_deal_display_pref($pref_q, $pref_parent_q, $pref_invoice);
  
} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update($entity, $fieldname, $disstatus);
  $pref_q = run_query_display_pref($uid,"deal",1);
  $pref_parent_q = run_query_display_pref($uid,"parentdeal",1);
  $pref_invoice = run_query_display_pref ($uid,"invoice",1);
  $display["detail"] = dis_deal_display_pref($pref_q, $pref_parent_q, $pref_invoice);

} else if ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update($entity, $new_level, $fieldorder);
  $pref_q = run_query_display_pref($uid,"deal",1);
  $pref_parent_q = run_query_display_pref($uid,"parentdeal",1);
  $pref_invoice = run_query_display_pref ($uid,"invoice",1);
  $display["detail"] = dis_deal_display_pref($pref_q, $pref_parent_q, $pref_invoice);
  
} elseif ($action == "admin")  {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_dealstatus());
  
} elseif ($action == "kind_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_kind_insert($deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_kind_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_kind_insert_error);
  }
  require("deal_js.inc");
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_dealstatus());
  
} elseif ($action == "kind_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_kind_update($deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_kind_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_kind_update_error);
  }
  require("deal_js.inc");
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_dealstatus());
  
} elseif ($action == "kind_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_kind_links($deal["kind"]);
  require("deal_js.inc");
  
} elseif ($action == "kind_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_kind_delete($deal["kind"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_kind_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_kind_delete_error);
  }
  require("deal_js.inc");
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_dealstatus());

} elseif ($action == "status_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_status_insert($deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_status_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_status_insert_error);
  }
  require("deal_js.inc");
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_dealstatus());

} elseif ($action == "status_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_status_update($deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_status_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_status_update_error);
  }
  require("deal_js.inc");
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_dealstatus());

} elseif ($action == "status_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_status_links($deal["state"]);
  require("deal_js.inc");

} elseif ($action == "status_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_status_delete($deal["state"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_status_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_status_delete_error);
  }
  require("deal_js.inc");
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_dealstatus());


///////////////////////////////////////////////////////////////////////////////
// -- Actions about ParentDeal -- 
///////////////////////////////////////////////////////////////////////////////


} elseif ($action == "parent_search")  {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $usr_q = run_query_userobm_active();
  $display["search"] = html_parentdeal_search_form($deal, $usr_q);
  $display["result"] = dis_parentdeal_search_list($deal);

} elseif ($action == "parent_new") {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $display["detail"] = dis_parentdeal_form($action, $deal);

} elseif ($action == "parent_detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_parentdeal_consult($deal);

} elseif ($action == "parent_detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $display["detail"] = dis_parentdeal_form($action, $deal);
  
} elseif ($action == "parent_insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_parent_form("", $deal)) {
    $retour = run_query_insert_parentdeal($deal);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_insert_ok); 
    } else {
      $display["msg"] .= display_err_msg($err_msg);
    }
    $display["search"] = html_parentdeal_search_form($deal, run_query_userobm());
  } else {
    require("deal_js.inc");
    $display["msg"] .= display_warn_msg($err_msg);
    $display["search"] = html_parentdeal_search_form($deal, run_query_userobm());
  }
  
} elseif ($action == "parent_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_parent_has_deal($deal["parent"])) {
    $display["msg"] .= display_err_msg($l_err_parent_has_deal);
    $display["search"] = dis_parentdeal_consult($deal);
  } else {
    run_query_delete_parentdeal($deal["parent"]); 
    $display["msg"] .= display_ok_msg($l_delete_ok); 
    require("deal_js.inc");
    $display["search"] = html_parentdeal_search_form($deal, run_query_userobm());
  }    
  
} elseif  ($action == "parent_update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_parent_form("", $deal)) {
    $retour = run_query_update_parentdeal($deal);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok); 
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
    }
    $display["detail"] = dis_parentdeal_consult($deal);
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    require("deal_js.inc");
    $display["detail"] = dis_parentdeal_form($action, $deal);
  }

} elseif ($action == "affect") {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $parent_q = run_query_search_parentdeal('');
  $display["detail"] = html_deal_affect($parent_q, $deal["id"]);

} elseif ($action == "affect_update") {
///////////////////////////////////////////////////////////////////////////////
  if ($deal["id"] > 0) {
    run_query_affect_deal_parentdeal($deal["id"], $deal);
    $display["msg"] .= display_ok_msg($l_updateaffect_ok); 

    $display["detail"] = dis_deal_consult($deal);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
// Update actions url in case some values have been updated (id after insert) 
update_deal_action_url();
if (! $popup) {
  $display["header"] = generate_menu($module, $section);
}
$display["head"] = display_head($l_deal);
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $deal hash, Deal parameters transmited
// returns : $deal hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_deal() {
  global $tf_num, $tf_label, $tf_datebegin, $param_parent, $sel_kind, $sel_tt;
  global $param_company, $sel_contact1, $sel_contact2, $sel_market, $sel_tech;
  global $tf_dateprop, $tf_amount, $sel_state, $tf_datealarm, $ta_com;
  global $tf_datecomment, $sel_usercomment, $ta_add_comment;
  global $tf_plabel, $sel_pmanager, $cb_parchive,$cb_archive,$tf_todo,$cb_priv;
  global $hd_company_ad1, $hd_company_zip, $hd_company_town;
  global $tf_company_name, $tf_zip,$sel_manager, $tf_dateafter, $tf_datebefore;
  global $sel_pmarket, $sel_ptech, $ta_pcom, $sel_parent;
  global $sel_cat, $tf_code, $tf_cat;
  global $param_deal, $hd_usercreate, $hd_timeupdate, $set_debug;
  global $tf_kind, $rd_kind_inout, $tf_status, $tf_order, $tf_hitrate;
  global $popup, $ext_action, $ext_url, $ext_id, $ext_title, $ext_target;  
  global $ext_widget, $ext_widget_text, $new_order, $order_dir;
 
  if (isset ($popup)) $deal["popup"] = $popup;
  if (isset ($ext_action)) $deal["ext_action"] = $ext_action;
  if (isset ($ext_url)) $deal["ext_url"] = $ext_url;
  if (isset ($ext_id)) $deal["ext_id"] = $ext_id;
  if (isset ($ext_title)) $deal["ext_title"] = stripslashes(urldecode($ext_title));
  if (isset ($ext_target)) $deal["ext_target"] = $ext_target;
  if (isset ($ext_widget)) $deal["ext_widget"] = $ext_widget;
  if (isset ($ext_widget_text)) $deal["ext_widget_text"] = $ext_widget_text;

  if (isset ($new_order)) $deal["new_order"] = $new_order;
  if (isset ($order_dir)) $deal["order_dir"] = $order_dir;

  // Deal fields
  if (isset ($param_deal)) $deal["id"] = $param_deal;
  if (isset ($tf_num)) $deal["num"] = $tf_num;
  if (isset ($tf_label)) $deal["label"] = $tf_label;
  if (isset ($tf_datebegin)) $deal["datebegin"] = $tf_datebegin;
  if (isset ($param_parent)) $deal["parent"] = $param_parent;
  if (isset ($sel_kind)) $deal["kind"] = $sel_kind;
  if (isset ($sel_tt)) $deal["tasktype"] = $sel_tt;
  if (isset ($param_company)) $deal["company"] = $param_company;
  if (isset ($sel_contact1)) $deal["contact1"] = $sel_contact1;
  if (isset ($sel_contact2)) $deal["contact2"] = $sel_contact2;
  if (isset ($sel_market)) $deal["market"] = $sel_market;
  if (isset ($sel_tech)) $deal["tech"] = $sel_tech;
  if (isset ($tf_dateprop)) $deal["dateprop"] = $tf_dateprop;
  if (isset ($tf_amount)) $deal["amount"] = $tf_amount;
  if (isset ($tf_hitrate)) $deal["hitrate"] = $tf_hitrate;
  if (isset ($sel_state)) $deal["state"] = $sel_state;
  if (isset ($tf_datealarm)) $deal["datealarm"] = $tf_datealarm;
  if (isset ($tf_cat)) $deal["cat_label"] = $tf_cat;
  if (isset ($tf_code)) $deal["cat_code"] = $tf_code;
  if (isset ($sel_cat)) $deal["category"] = $sel_cat;
  if (isset ($cb_archive)) {
    $deal["archive"] = $cb_archive;
  }
  if (isset ($tf_todo)) $deal["todo"] = $tf_todo;
  $deal["priv"] = ($cb_priv == 1 ? 1 : 0);
  if (isset ($ta_com)) $deal["com"] = $ta_com;
  if (isset ($tf_datecomment)) $deal["datecomment"] = $tf_datecomment;
  if (isset ($sel_usercomment)) $deal["usercomment"] = $sel_usercomment;
  if (isset ($ta_add_comment)) $deal["add_comment"] = trim($ta_add_comment);
  if (isset ($hd_usercreate)) $deal["usercreate"] = $hd_usercreate;
  if (isset ($hd_timeupdate)) $deal["timeupdate"] = $hd_timeupdate;

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

  // Search fields
  if (isset ($tf_company_name)) $deal["company_name"] = $tf_company_name;
  if (isset ($tf_zip)) $deal["company_zip"] = $tf_zip;
  if (isset ($sel_manager)) $deal["manager"] = $sel_manager;
  if (isset ($tf_dateafter)) $deal["dateafter"] = $tf_dateafter;
  if (isset ($tf_datebefore)) $deal["datebefore"] = $tf_datebefore;

  // Company infos (with company_name)
  if (isset ($hd_company_ad1)) $deal["company_ad1"] = $hd_company_ad1;
  if (isset ($hd_company_zip)) $deal["company_zip"] = $hd_company_zip;
  if (isset ($hd_company_town)) $deal["company_town"] = $hd_company_town;

  // Admin - Kind fields
  // $sel_kind -> "kind" is already set
  if (isset ($tf_kind)) $deal["kind_label"] = $tf_kind;
  if (isset ($rd_kind_inout)) $deal["kind_inout"] = $rd_kind_inout;

  // Admin - Status fields
  // $sel_state -> "state" is already set
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
  global $l_header_stats, $l_deal_select_company;
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
    'Condition'=> array ('detailupdate', 'update', 'insert', 'quick_detail', 'quick_update') 
                                    	    );

  // Quick Detail
  $actions["deal"]["quick_detail"] = array (
    'Name'     => $l_header_quickupdate,
    'Url'      => "$path/deal/deal_index.php?action=quick_detail&amp;param_deal=".$deal["id"],
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailupdate', 'insert', 'update', 'quick_update')
                                    	    );

  // Parent Detail Consult
  $actions["deal"]["parent_detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/deal/deal_index.php?action=detailconsult&amp;param_deal=".$deal["id"],
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate')
                                    	    );

  // Detail Update
  $actions["deal"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/deal/deal_index.php?action=detailupdate&amp;param_deal=".$deal["id"],
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'update','insert', 'quick_detail', 'quick_update')
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
    'Condition'=> array ('detailconsult', 'update','insert') 
                                     );

  // Delete
  $actions["deal"]["delete"] = array (
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     );

  // Stats
  $actions["deal"]["stats"] = array (
    'Name'     => $l_header_stats,
    'Url'      => "$path/deal/deal_index.php?action=stats",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
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
  $actions["deal"]["ext_get_cat_ids"]  = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

  // Category Check Link
  $actions["deal"]["cat_checklink"] = array (
    'Url'      => "$path/contact/deal_index.php?action=cat_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                    );                

// Category Update
  $actions["deal"]["cat_update"] = array (
    'Url'      => "$path/contact/deal_index.php?action=cat_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Category Insert
  $actions["deal"]["cat_insert"] = array (
    'Url'      => "$path/contact/deal_index.php?action=cat_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Category Delete
  $actions["deal"]["cat_delete"] = array (
    'Url'      => "$path/contact/deal_index.php?action=cat_delete",
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
// Deal Actions URL updates (after processing, before displaying menu)  
///////////////////////////////////////////////////////////////////////////////
function update_deal_action_url() {
  global $deal, $actions, $path;

  // Detail Update
  $actions["deal"]["detailupdate"]['Url'] = "$path/deal/deal_index.php?action=detailupdate&amp;param_deal=".$deal["id"];

  // Parent Detail Update
  $actions["deal"]["parent_detailupdate"]['Url'] = "$path/deal/deal_index.php?action=parent_detailupdate&amp;param_parent=".$deal["parent"];

  // Check Delete
  $actions["deal"]["check_delete"]['Url'] = "$path/deal/deal_index.php?action=check_delete&amp;param_deal=".$deal["id"];

  // Parent Check Delete
  $actions["deal"]["parent_delete"]['Url'] = "$path/deal/deal_index.php?action=parent_delete&amp;param_parent=".$deal["parent"];

}

</script>
