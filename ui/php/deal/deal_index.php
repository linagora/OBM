<SCRIPT language="php">
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
// - insert          -- form fields    -- insert the deal
// - update          -- form fields    -- update the deal
// - check_delete    -- $param_deal    -- check links before delete
// - delete          -- $param_deal    -- delete the deal
// - affect          -- $param_deal    -- show the new parent deal form
// - affect_update   -- $param_deal    -- affect the deal to the parentdeal
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
// - cat_checklink   -- $sel_cat       -- check if category is used
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
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Session,Auth,Perms Management                                             //
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$section = "COM";
$menu = "DEAL";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("$obminclude/phplib/obmlib.inc");
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("deal_query.inc");
require("deal_display.inc");

$uid = $auth->auth["uid"];

// Updating the "last deal" bookmark 
if ( ($param_deal == $last_deal) && (strcmp($action,"delete")==0) ) {
  $last_deal = $last_deal_default;
} elseif ( ($param_deal > 0) && ($last_deal != $param_deal) ) {
  $last_deal = $param_deal;
  run_query_set_user_pref($uid,"last_deal",$param_deal);
  $last_deal_name = run_query_global_deal_label($last_deal);
}

// Updating the "last parentdeal" bookmark 
if ( ($param_parent == $last_parentdeal) && (strcmp($action,"parent_delete")==0) ) {
  $last_parentdeal = $last_parentdeal_default;
} elseif ( ($param_parent > 0) && ($last_parentdeal != $param_parent) ) {
  $last_parentdeal = $param_parent;
  run_query_set_user_pref($uid, "last_parentdeal", $param_parent);
  $last_parentdeal_name = run_query_global_parentdeal_label($last_parentdeal);
}

page_close();
if ($action == "") $action = "index";
$deal = get_param_deal();
get_deal_action();
$perm->check();


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if (! $popup) {
  $display["header"] = generate_menu($menu, $section);
}

// when searching deals belonging to a parent, we display the parent
if (($action == "search") && ($deal["parent"])) {
///////////////////////////////////////////////////////////////////////////////
  $action = "parent_detailconsult";
}

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_id") {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $deal_q = run_query_deal($comp_id);
  $display["detail"] = html_select_deal($deal_q, stripslashes($title));

} elseif ($action == "ext_get_id_url") {
///////////////////////////////////////////////////////////////////////////////
  require("contract_js.inc");
  $deal_q = run_query_deal($comp_id);
  $display["detail"] = html_select_deal($deal_q, stripslashes($title), $url);
}


///////////////////////////////////////////////////////////////////////////////
// Normal calls
///////////////////////////////////////////////////////////////////////////////
elseif (($action == "index") || ($action == "")) {
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
  $usr_q = run_query_userobm();
  $display["search"] = html_deal_search_form($deal, run_query_dealtype(), run_query_deal_tasktype(), run_query_dealstatus(), $usr_q);
  $display["result"] = dis_deal_search_list($deal);
  
} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $usr_q = run_query_userobm_active();
  $display["detail"] = html_deal_form($action, "", run_query_dealtype(), run_query_deal_tasktype(), $usr_q, run_query_company_info($param_company), run_query_contact_deal($param_company), run_query_dealstatus(), $param_company, $deal);

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  if ($deal["id"] > 0) {
    $deal_q = run_query_detail($deal["id"]);
    if ( ($deal_q->f("deal_visibility")==0) ||
         ($deal_q->f("deal_usercreate")==$uid) ) {
      $display["detailInfo"] = display_record_info($deal_q->f("deal_usercreate"),$deal_q->f("deal_userupdate"),$deal_q->f("timecreate"),$deal_q->f("timeupdate"));
      $cid = $deal_q->f("deal_company_id");
      // we retrieve invoices data :
      $inv_q = run_query_search_connected_invoices ($deal["id"], $incl_arch);
      $pref_q = run_query_display_pref ($uid, "invoice");
      $display["detail"] = html_deal_consult($deal_q, run_query_contact_deal($cid), $cid, $inv_q, $pref_q);
    } else {
      // this deal's page has "private" access
      $display["msg"] .= display_err_msg($l_error_visibility);
    } 	
  }
  
} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  if ($deal["id"] > 0) {
    require("deal_js.inc");
    $deal_q = run_query_detail($deal["id"]);
    $display["detailInfo"] = display_record_info($deal_q->f("deal_usercreate"),$deal_q->f("deal_userupdate"),$deal_q->f("timecreate"),$deal_q->f("timeupdate"));
    $param_company = $deal_q->f("deal_company_id");
    $users = array($deal_q->f("deal_marketingmanager_id"), $deal_q->f("deal_technicalmanager_id"));
    $usr_q = run_query_userobm_active($users);
    $display["detail"] = html_deal_form($action, $deal_q, run_query_dealtype(), run_query_deal_tasktype(), $usr_q, "", run_query_contact_deal($param_company), run_query_dealstatus(), $param_company, $deal);
  }
  
} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_deal_form("", $deal)) {
    $retour = run_query_insert($deal);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_insert_ok);
      $param_deal = run_query_deal_id($deal);
      $deal_q = run_query_detail($param_deal);
      $display["detailInfo"] = display_record_info($deal_q->f("deal_usercreate"),$deal_q->f("deal_userupdate"),$deal_q->f("timecreate"),$deal_q->f("timeupdate"));
      $cid = $deal_q->f("deal_company_id");
      $inv_q = run_query_search_connected_invoices ($param_deal, $incl_arch);
      $display["detail"] = html_deal_consult($deal_q, run_query_contact_deal($cid), $cid, $inv_q, "");
    } else {
      $display["msg"] .= display_err_msg("$l_insert_error : $err_msg");
      $display["search"] = dis_deal_index($deal);
    }
  } else {
    require("deal_js.inc");
    $display["msg"] .= display_err_msg($err_msg);
    $users = array($deal["market"], $deal["tech"]);
    $usr_q = run_query_userobm_active($users);
    $display["detail"] = html_deal_form($action, "", run_query_dealtype(), run_query_deal_tasktype(), $usr_q, "", run_query_contact_deal($param_company), run_query_dealstatus(), $param_company, $deal);
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
    $deal_q = run_query_detail($deal["id"]);
    $display["detailInfo"] = display_record_info($deal_q->f("deal_usercreate"),$deal_q->f("deal_userupdate"),$deal_q->f("timecreate"),$deal_q->f("timeupdate"));
    $cid = $deal_q->f("deal_company_id");
    $q_invoices = run_query_search_connected_invoices ($deal["id"], $incl_arch);
    $q_invoices->next_record();
    $invoices_options = run_query_display_pref ($uid, "invoice");
    $display["detail"] = html_deal_consult($deal_q, run_query_contact_deal($cid), $cid, $q_invoices, $invoices_options);
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    $param_company = $deal["company"];
    $users = array($deal["market"], $deal["tech"]);
    $usr_q = run_query_userobm_active($users);
    $display["detail"] = html_deal_form($action, "", run_query_dealtype(), run_query_deal_tasktype(), $usr_q, "", run_query_contact_deal($param_company), run_query_dealstatus(), $param_company, $deal);

    // If deal archived, we look about archiving the parentdeal ?????
    if ($cb_arc_aff == "archives") {
      $obm_q = run_query_detail($deal["id"]);
      $obm_q->next_record();
      $param_parent = $obm_q->f("deal_parentdeal_id");
      run_query_update_archive($deal["id"], $param_parent);
    }
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
  
} elseif ($action == "document_add")  {
///////////////////////////////////////////////////////////////////////////////
  if ($deal["doc_nb"] > 0) {
    $nb = run_query_insert_documents($deal,"Deal");
    $display["msg"] .= display_ok_msg("$nb $l_document_added");
  } else {
    $display["msg"] .= display_err_msg($l_no_document_added);
  }
    $deal_q = run_query_detail($deal["id"]);
    $display["detailInfo"] = display_record_info($deal_q->f("deal_usercreate"),$deal_q->f("deal_userupdate"),$deal_q->f("timecreate"),$deal_q->f("timeupdate"));
    $cid = $deal_q->f("deal_company_id");
    $q_invoices = run_query_search_connected_invoices ($deal["id"], $incl_arch);
    $q_invoices->next_record();
    $invoices_options = run_query_display_pref ($uid, "invoice");
    $display["detail"] = html_deal_consult($deal_q, run_query_contact_deal($cid), $cid, $q_invoices, $invoices_options);
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
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype("all"),run_query_dealstatus());
  
} elseif ($action == "kind_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_kind_insert($deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_kind_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_kind_insert_error);
  }
  require("deal_js.inc");
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype("all"),run_query_dealstatus());
  
} elseif ($action == "kind_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_kind_update($deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_kind_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_kind_update_error);
  }
  require("deal_js.inc");
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype("all"),run_query_dealstatus());
  
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
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype("all"),run_query_dealstatus());

} elseif ($action == "status_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_status_insert($deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_status_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_status_insert_error);
  }
  require("deal_js.inc");
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype("all"),run_query_dealstatus());

} elseif ($action == "status_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_status_update($deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_status_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_status_update_error);
  }
  require("deal_js.inc");
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype("all"),run_query_dealstatus());

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
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus());

} elseif ($action == "cat_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_cat_insert($deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_cat_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_cat_insert_error);
  }
  require("deal_js.inc");
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype("all"),run_query_dealstatus());

} elseif ($action == "cat_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_cat_update($deal);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_cat_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_cat_update_error);
  }
  require("deal_js.inc");
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype("all"),run_query_dealstatus());

} elseif ($action == "cat_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_cat_links($deal["cat"]);
  require("deal_js.inc");

} elseif ($action == "cat_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_cat_delete($deal["cat"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_cat_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_cat_delete_error);
  }
  require("deal_js.inc");
  $display["detail"] = html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype("all"),run_query_dealstatus());


///////////////////////////////////////////////////////////////////////////////
// -- Actions about ParentDeal -- 
///////////////////////////////////////////////////////////////////////////////


} elseif ($action == "parent_search")  {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $usr_q = run_query_userobm();
  $display["search"] = html_parentdeal_search_form($deal, $usr_q);
  $display["result"] = dis_parentdeal_search_list($deal);

} elseif ($action == "parent_new") {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $obm_q = new DB_OBM;
  $usr_q = run_query_userobm_active();
  $display["detail"] = html_parentdeal_form($action,$obm_q, $usr_q,'');

} elseif ($action == "parent_detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_detail_parentdeal($deal["parent"]);
  $pref_q = run_query_display_pref ($uid, "deal");
  $deal_q = run_query_search($deal, 0, $new_order, $order_dir);
  $num_rows = $deal_q->num_rows();

  $display["detail"] = html_parentdeal_consult($obm_q,$deal_q,$deal,$pref_q,$num_rows,run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus(),run_query_userobm());

} elseif ($action == "parent_detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  if ($deal["parent"] > 0) {
    require("deal_js.inc");
    $obm_q = run_query_detail_parentdeal($deal["parent"]);
    $users = array($obm_q->f("parentdeal_marketingmanager_id"), $obm_q->f("parentdeal_technicalmanager_id"));
    $usr_q = run_query_userobm_active($users);
    $display["detailInfo"] = display_record_info($obm_q->f("parentdeal_usercreate"),$obm_q->f("parentdeal_userupdate"),$obm_q->f("timecreate"),$obm_q->f("timeupdate"));
    $display["detail"] = html_parentdeal_form($action,$obm_q, $usr_q, $deal);
  } 
  
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
    $obm_q = run_query_detail_parentdeal($deal["parent"]);
    $users = array($obm_q->f("parentdeal_marketingmanager_id"), $obm_q->f("parentdeal_technicalmanager_id"));
    $usr_q = run_query_userobm_active($users);
    $display["detailInfo"] = display_record_info($obm_q->f("parentdeal_usercreate"),$obm_q->f("parentdeal_userupdate"),$obm_q->f("parentdeal_timecreate"),$obm_q->f("timeupdate"));
    $display["search"] = html_parentdeal_form($action,$obm_q, $usr_q, $deal);
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
    $obm_q = run_query_detail_parentdeal($deal["parent"]);
    $pref_q = run_query_display_pref ($uid,"deal");
    $deal_q = run_query_search($deal, 0, $new_order, $order_dir);
    $num_rows = $deal_q->num_rows();
    $display["detail"] = html_parentdeal_consult($obm_q,$deal_q,$deal,$pref_q,$num_rows,run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus(),run_query_userobm());
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    require("deal_js.inc");
    $obm_q = run_query_detail_parentdeal($deal["parent"]);
    $users = array($obm_q->f("parentdeal_marketingmanager_id"), $obm_q->f("parentdeal_technicalmanager_id"));
    $usr_q = run_query_userobm_active($users);
    $display["detailInfo"] = display_record_info($obm_q->f("parentdeal_usercreate"),$obm_q->f("parentdeal_userupdate"),$obm_q->f("parentdeal_timecreate"),$obm_q->f("timeupdate"));
    $display["detail"] = html_parentdeal_form($action, $obm_q, $usr_q, $deal);
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

    // then we display the deal
    $deal_q = run_query_detail($deal["id"]);
    if ( ($deal_q->f("deal_visibility")==0) ||
         ($deal_q->f("deal_usercreate")==$uid) ) {
      $display["detailInfo"] = display_record_info($deal_q->f("deal_usercreate"),$deal_q->f("deal_userupdate"),$deal_q->f("timecreate"),$deal_q->f("timeupdate"));
      $cid = $deal_q->f("deal_company_id");
      $q_invoices = run_query_search_connected_invoices ($deal["id"], $incl_arch);
      $q_invoices->next_record();
      $invoices_options = run_query_display_pref ($uid,"invoice");
      $display["detail"] = html_deal_consult($deal_q, run_query_contact_deal($cid), $cid, $q_invoices, $invoices_options);
    }
  } else {
    $display["msg"] .= display_err_msg($l_query_error);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_deal);
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $deal hash, Deal parameters transmited
// returns : $deal hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_deal() {
  global $tf_num, $tf_label, $tf_datebegin, $param_parent, $sel_kind, $sel_cat;
  global $param_company, $sel_contact1, $sel_contact2, $sel_market, $sel_tech;
  global $tf_dateprop, $tf_amount, $sel_state, $tf_datealarm, $ta_com;
  global $tf_datecomment, $sel_usercomment, $ta_add_comment;
  global $tf_plabel, $sel_pmanager, $cb_parchive, $cb_archive,$tf_todo,$cb_vis;
  global $hd_company_ad1, $hd_company_zip, $hd_company_town;
  global $tf_company_name, $tf_zip,$sel_manager, $tf_dateafter, $tf_datebefore;
  global $sel_pmarket, $sel_ptech, $ta_pcom, $sel_parent;
  global $param_deal, $hd_usercreate, $hd_timeupdate, $set_debug;
  global $tf_kind, $rd_kind_inout, $tf_status, $tf_order, $tf_cat, $tf_hitrate;
  global $rd_cat_internal,$ext_action, $ext_url, $ext_id, $ext_title, $ext_target;
  global $HTTP_POST_VARS,$HTTP_GET_VARS;

  if (isset ($ext_action)) $deal["ext_action"] = $ext_action;
  if (isset ($ext_url)) $deal["ext_url"] = $ext_url;
  if (isset ($ext_id)) $deal["ext_id"] = $ext_id;
  if (isset ($ext_id)) $deal["id"] = $ext_id;
  if (isset ($ext_title)) $deal["ext_title"] = $ext_title;
  if (isset ($ext_target)) $deal["ext_target"] = $ext_target;

  if ((is_array ($HTTP_POST_VARS)) && (count($HTTP_POST_VARS) > 0)) {
    $http_obm_vars = $HTTP_POST_VARS;
  } elseif ((is_array ($HTTP_GET_VARS)) && (count($HTTP_GET_VARS) > 0)) {
    $http_obm_vars = $HTTP_GET_VARS;
  }

  if (isset ($http_obm_vars)) {
    $nb_d = 0;
    $nb_deal = 0;
    while ( list( $key ) = each( $http_obm_vars ) ) {
      if (strcmp(substr($key, 0, 4),"cb_d") == 0) {
	$nb_d++;
	$d_num = substr($key, 4);
	$deal["doc$nb_d"] = $d_num;
      }
    }
    $deal["doc_nb"] = $nb_d;
  }
  
  // Deal fields
  if (isset ($param_deal)) $deal["id"] = $param_deal;
  if (isset ($tf_num)) $deal["num"] = $tf_num;
  if (isset ($tf_label)) $deal["label"] = $tf_label;
  if (isset ($tf_datebegin)) $deal["datebegin"] = $tf_datebegin;
  if (isset ($param_parent)) $deal["parent"] = $param_parent;
  if (isset ($sel_kind)) $deal["kind"] = $sel_kind;
  if (isset ($sel_cat)) $deal["cat"] = $sel_cat;
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
  if (isset ($ta_com)) $deal["com"] = $ta_com;
  if (isset ($cb_archive)) {
    $deal["archive"] = $cb_archive;
  }
  if (isset ($tf_todo)) $deal["todo"] = $tf_todo;
  $deal["vis"] = ($cb_vis == 1 ? 1 : 0);
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

  // Admin - Category fields
  // $sel_cat -> "cat" is already set
  if (isset ($tf_cat)) $deal["cat_label"] = $tf_cat;
  if (isset ($rd_cat_internal)) $deal["cat_internal"] = $rd_cat_internal;

  dis_debug_param($deal);
  return $deal;
}


///////////////////////////////////////////////////////////////////////////////
// Comodity function: display the parameters store in the hash given
// Parameters:
//   - $deal[] : hash which values to display
///////////////////////////////////////////////////////////////////////////////
function dis_debug_param($deal) {
  global $cdg_param, $action;

  if (debug_level_isset($cdg_param)) {
    echo "<br />action=$action";
    if ( $deal ) {
      while ( list( $key, $val ) = each( $deal ) ) {
        echo "<br />deal[$key]=$val";
      }
    }
  }

}


///////////////////////////////////////////////////////////////////////////////
// Deal Actions 
///////////////////////////////////////////////////////////////////////////////
function get_deal_action() {
  global $deal, $actions, $path;
  global $l_header_find,$l_header_new_f,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display,$l_header_admin;
  global $l_header_new_child, $l_header_new_parent;
  global $l_deal_select_company;
  global $deal_read, $deal_write, $deal_admin_read, $deal_admin_write;

  // Index
  $actions["DEAL"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/deal/deal_index.php?action=index",
    'Right'    => $deal_read,
    'Condition'=> array ('all') 
                                    );

  // Search
  $actions["DEAL"]["search"] = array (
    'Url'      => "$path/deal/deal_index.php?action=search",
    'Right'    => $deal_read,
    'Condition'=> array ('None') 
                                     );

  // Parent Search
  $actions["DEAL"]["parent_search"] = array (
    'Url'      => "$path/deal/deal_index.php?action=parent_search",
    'Right'    => $deal_read,
    'Condition'=> array ('None') 
                                     );

  // New
  $actions["DEAL"]["new"] = array (
    'Name'     => $l_header_new_f,
    'Url'      => "$path/company/company_index.php?action=ext_get_id_url&amp;popup=1&amp;title=".urlencode($l_deal_select_company)."&amp;url=".urlencode("$path/deal/deal_index.php?action=new&amp;param_company=")."",
    'Right'    => $deal_write,
    'Popup'    => 1,
    'Condition'=> array ('all') 
                                  );

  // New Child
  $ret_url = urlencode("$path/deal/deal_index.php?action=new&amp;param_parent=". $deal["parent"] . "&amp;sel_market=" . $deal["pmarket"] . "&amp;sel_tech=" . $deal["ptech"] . "&amp;param_company=");
  $actions["DEAL"]["new_child"] = array (
    'Name'     => $l_header_new_child,
    'Url'      => "$path/company/company_index.php?action=ext_get_id_url&amp;popup=1&amp;title=".urlencode($l_deal_select_company)."&amp;url=$ret_url",
    'Right'    => $deal_write,
    'Popup'    => 1,
    'Condition'=> array ('parent_detailconsult') 
                                  );

  // Parent New
  $actions["DEAL"]["parent_new"] = array (
    'Name'     => $l_header_new_parent,
    'Url'      => "$path/deal/deal_index.php?action=parent_new",
    'Right'    => $deal_write,
    'Condition'=> array ('','search','parent_search','index',
                         'detailconsult','parent_detailconsult',
                         'admin','display') 
                                         );

  // Detail Consult
  $actions["DEAL"]["detailconsult"] = array (
    'Url'      => "$path/deal/deal_index.php?action=detailconsult",
    'Right'    => $deal_read,
    'Condition'=> array ('None') 
                                    	    );

  // Parent Detail Consult
  $actions["DEAL"]["parent_detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/deal/deal_index.php?action=detailconsult&amp;param_deal=".$deal["id"]."",
    'Right'    => $deal_read,
    'Condition'=> array ('detailupdate')
                                    	    );

  // Detail Update
  $actions["DEAL"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/deal/deal_index.php?action=detailupdate&amp;param_deal=".$deal["id"]."",
    'Right'    => $deal_write,
    'Condition'=> array ('detailconsult', 'update') 
                                     	    );

  // Parent Detail Update
  $actions["DEAL"]["parent_detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/deal/deal_index.php?action=parent_detailupdate&amp;param_parent=".$deal["parent"]."",
    'Right'    => $deal_write,
    'Condition'=> array ('parent_detailconsult') 
                                     		  );

  // Insert
  $actions["DEAL"]["insert"] = array (
    'Url'      => "$path/deal/deal_index.php?action=insert",
    'Right'    => $deal_write,
    'Condition'=> array ('None') 
                                     );

  // Parent insert
  $actions["DEAL"]["parent_insert"] = array (
    'Url'      => "$path/deal/deal_index.php?action=parent_insert",
    'Right'    => $deal_write,
    'Condition'=> array ('None') 
                                            );

  // Delete
  $actions["DEAL"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/deal/deal_index.php?action=check_delete&amp;param_deal=".$deal["id"]."",
    'Right'    => $deal_write,
    'Condition'=> array ('detailconsult', 'update') 
                                     );

// Document add
  $actions["DEAL"]["document_add"] = array (
    'Url'      => "$path/deal/deal_index.php?action=document_add",
    'Right'    => $deal_write,
    'Condition'=> array ('None')
  );
  
  // Parent Delete
  $actions["DEAL"]["parent_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/deal/deal_index.php?action=parent_delete&amp;param_parent=".$deal["parent"]."",
    'Right'    => $deal_write,
    'Condition'=> array ('parent_detailconsult') 
                                     	     );

  // Admin  
  $actions["DEAL"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/deal/deal_index.php?action=admin",
    'Right'    => $deal_admin_read,
    'Condition'=> array ('all') 
                                    );

  // Kind Insert
  $actions["DEAL"]["kind_insert"] = array (
    'Url'      => "$path/deal/deal_index.php?action=kind_insert",
    'Right'    => $deal_admin_write,
    'Condition'=> array ('None') 
                                           );

  // Kind Update
  $actions["DEAL"]["kind_update"] = array (
    'Url'      => "$path/deal/deal_index.php?action=kind_update",
    'Right'    => $deal_admin_write,
    'Condition'=> array ('None') 
                                           );

  // Kind checklink
  $actions["DEAL"]["kind_checklink"] = array (
    'Url'      => "$path/deal/deal_index.php?action=kind_checklink",
    'Right'    => $deal_admin_write,
    'Condition'=> array ('None') 
                                             );

  // Kind delete
  $actions["DEAL"]["kind_delete"] = array (
    'Url'      => "$path/deal/deal_index.php?action=kind_delete",
    'Right'    => $deal_admin_write,
    'Condition'=> array ('None') 
                                          );

  // Status Insert 
  $actions["DEAL"]["status_status"] = array (
    'Url'      => "$path/deal/deal_index.php?action=status_insert",
    'Right'    => $deal_admin_write,
    'Condition'=> array ('None') 
                                          );

  // Status Update 
  $actions["DEAL"]["status_update"] = array (
    'Url'      => "$path/deal/deal_index.php?action=status_update",
    'Right'    => $deal_admin_write,
    'Condition'=> array ('None') 
                                          );

  // Status Checklink 
  $actions["DEAL"]["status_checklink"] = array (
    'Url'      => "$path/deal/deal_index.php?action=status_checklink",
    'Right'    => $deal_admin_write,
    'Condition'=> array ('None') 
                                          );

  // Status Delete 
  $actions["DEAL"]["status_delete"] = array (
    'Url'      => "$path/deal/deal_index.php?action=status_delete",
    'Right'    => $deal_admin_write,
    'Condition'=> array ('None') 
                                          );

  // Cat Insert
  $actions["DEAL"]["cat_insert"] = array (
    'Url'      => "$path/deal/deal_index.php?action=cat_insert",
    'Right'    => $deal_admin_write,
    'Condition'=> array ('None') 
                                          );

  // Cat Update
  $actions["DEAL"]["cat_insert"] = array (
    'Url'      => "$path/deal/deal_index.php?action=cat_insert",
    'Right'    => $deal_admin_write,
    'Condition'=> array ('None') 
                                          );

  // Cat Checklink
  $actions["DEAL"]["cat_checklink"] = array (
    'Url'      => "$path/deal/deal_index.php?action=cat_checklink",
    'Right'    => $deal_admin_write,
    'Condition'=> array ('None') 
                                          );
  // Cat Delete
  $actions["DEAL"]["cat_delete"] = array (
    'Url'      => "$path/deal/deal_index.php?action=cat_checkdelete",
    'Right'    => $deal_admin_write,
    'Condition'=> array ('None') 
                                          );

  // Display
  $actions["DEAL"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/deal/deal_index.php?action=display",
    'Right'    => $deal_read, 
    'Condition'=> array ('all') 
                                      );

  // Display Preference
  $actions["DEAL"]["dispref_display"] = array (
    'Url'      => "$path/deal/deal_index.php?action=dispref_display",
    'Right'    => $deal_read, 
    'Condition'=> array ('None') 
                                      );

  // Display Level
  $actions["DEAL"]["dispref_level"] = array (
    'Url'      => "$path/deal/deal_index.php?action=dispref_level",
    'Right'    => $deal_read, 
    'Condition'=> array ('None') 
                                      );

}

</SCRIPT>
