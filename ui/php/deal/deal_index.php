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
  $last_deal=$last_deal_default;
} elseif  ( ($param_deal > 0) && ($last_deal != $param_deal) ) {
  $last_deal=$param_deal;
  run_query_set_user_pref($uid,"last_deal",$param_deal);
  $last_deal_name = run_query_global_deal_label($last_deal);
  //$sess->register("last_deal");
}

page_close();
if($action == "") $action = "index";
$deal = get_param_deal();
get_deal_action();
$perm->check();
///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
display_head($l_deal);     // Head & Body

if ($popup) {
///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
  if ($action == "ext_get_id") {
    require("deal_js.inc");
    $deal_q = run_query_deal($comp_id);
    html_select_deal($deal_q, stripslashes($title));
  } elseif ($action == "ext_get_id_url") {
    require("contract_js.inc");
    $deak_q = run_query_deal($comp_id);
    html_select_deal($deal_q, stripslashes($title), $url);
  } else {
    display_err_msg($l_error_permission);
  }

  display_end();
  exit();
}



generate_menu($menu,$section);      // Menu
display_bookmarks();


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
// when searching deals belonging to a parent, we display the parent
if (($action == "search") && ($param_parent)) {
  $action = "parent_detailconsult";
}


if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  dis_deal_index();
  if ($set_display == "yes") {
    dis_deal_search_list($deal);
  } else {
    display_info_msg($l_no_display);
  }
 
} elseif ($action == "search")  { // tester si hd_parent mis ??? pour form :oui
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $usr_q = run_query_userobm();
  html_deal_search_form($deal, run_query_dealtype(), run_query_deal_tasktype(), run_query_dealstatus(), $usr_q);
  dis_deal_search_list($deal);
  

} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $usr_q = run_query_userobm();
  html_deal_form($action, "", run_query_dealtype(), run_query_deal_tasktype(), $usr_q, run_query_company_info($param_company), run_query_contact_deal($param_company), run_query_dealstatus(), $param_company, run_query_linked_contract($param_deal), $deal);
  
} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_deal > 0) {
    $deal_q = run_query_detail($param_deal);
    if ( ($deal_q->f("deal_visibility")==0) ||
         ($deal_q->f("deal_usercreate")==$uid) ) {
      display_record_info($deal_q->f("deal_usercreate"),$deal_q->f("deal_userupdate"),$deal_q->f("timecreate"),$deal_q->f("timeupdate"));
      $cid = $deal_q->f("deal_company_id");
      // we retrieve invoices data :
      $inv_q = run_query_search_connected_invoices ($param_deal, $incl_arch);
      $pref_q = run_query_display_pref ($uid, "invoice");
      html_deal_consult($deal_q, run_query_contact_deal($cid), $cid, $inv_q, $pref_q);
    } else {
      // this deal's page has "private" access
      display_err_msg($l_error_visibility);
    } 	
  }
  
} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_deal > 0) {
    require("deal_js.inc");
    $deal_q = run_query_detail($param_deal);
    display_record_info($deal_q->f("deal_usercreate"),$deal_q->f("deal_userupdate"),$deal_q->f("timecreate"),$deal_q->f("timeupdate"));
    $param_company = $deal_q->f("deal_company_id");
    $usr_q = run_query_userobm();
    html_deal_form($action, $deal_q, run_query_dealtype(), run_query_deal_tasktype(), $usr_q, "", run_query_contact_deal($param_company), run_query_dealstatus(), $param_company,run_query_linked_contract($param_deal), $deal);
  }
  
} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_deal_form("", $deal)) {
    $retour = run_query_insert($deal);
    if ($retour) {
      display_ok_msg($l_insert_ok);
      if($deal["add_contract"]) {
        $param_deal = run_query_deal_id($deal);
	echo "
        <script type=\"text/javascript\">
         window.location.href = '$path/contract/contract_index.php?action=new&param_company=".$deal["company"]."&param_deal=".$param_deal."&hd_deal_label=".$deal["label"]."&sel_con1=".$deal["contact1"]."&sel_con2=".$deal["contact2"]."&sel_tech=".$deal["tech"]."&sel_market=".$deal["market"]."&tf_label=".$deal["label"]."&ok_message=".addslashes($l_insert_ok)."'
        </script>
        ";
      }
    } else {
      display_err_msg("$l_insert_error : $err_msg");
    }
    dis_deal_index($deal);
  } else {
    require("deal_js.inc");
    display_err_msg($err_msg);
    $usr_q = run_query_userobm();
    html_deal_form($action, "", run_query_dealtype(), run_query_deal_tasktype(), $usr_q, "", run_query_contact_deal($param_company), run_query_dealstatus(), $param_company,run_query_linked_contract($param_deal), $deal);
  }
  
} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_deal_form("", $deal)) {
    $retour = run_query_update($deal);
    if ($retour) {
      display_ok_msg($l_update_ok);
      if($deal["add_contract"]) {
        $param_deal = run_query_deal_id($deal);
	echo "
        <script type=\"text/javascript\">
         window.location.href = '$path/contract/contract_index.php?action=new&param_company=".$deal["company"]."&param_deal=".$param_deal."&hd_deal_label=".$deal["label"]."&sel_con1=".$deal["contact1"]."&sel_con2=".$deal["contact2"]."&sel_tech=".$deal["tech"]."&sel_market=".$deal["market"]."&tf_label=".$deal["label"]."&ok_message=".addslashes($l_update_ok)."'
        </script>
        ";
      }
    } else {
      display_err_msg($l_update_error);
    }
    $deal_q = run_query_detail($param_deal);
    display_record_info($deal_q->f("deal_usercreate"),$deal_q->f("deal_userupdate"),$deal_q->f("timecreate"),$deal_q->f("timeupdate"));
    $cid = $deal_q->f("deal_company_id");
    $q_invoices = run_query_search_connected_invoices ($param_deal, $incl_arch);
    $q_invoices->next_record();
    $invoices_options = run_query_display_pref ($uid, "invoice");
    html_deal_consult($deal_q, run_query_contact_deal($cid), $cid, $q_invoices, $invoices_options);
  } else {
    display_err_msg($err_msg);
    $param_company = $deal["company"];
    $usr_q = run_query_userobm();
    html_deal_form($action, "", run_query_dealtype(), run_query_deal_tasktype(), $usr_q, "", run_query_contact_deal($param_company), run_query_dealstatus(), $param_company,run_query_linked_contract($param_deal), $deal);

    // If deal archived, we look about archiving the parentdeal ?????
    if ($cb_arc_aff=="archives") {
      echo "update<br>";
      $obm_q=run_query_detail($param_deal);
      $obm_q->next_record();
      $param_parent=$obm_q->f("deal_parentdeal_id");
      run_query_update_archive($param_deal,$param_parent);
    }
  }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  dis_check_links($param_deal);

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_delete($param_deal);
  if ($retour) {
    display_ok_msg($l_delete_ok);
  } else {
    display_err_msg($l_delete_error);
  }
  dis_deal_index();
  

} elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $pref_q = run_query_display_pref($uid,"deal",1);
  $pref_parent_q = run_query_display_pref($uid,"parentdeal",1);
  $pref_invoice = run_query_display_pref ($uid,"invoice",1);
  dis_deal_display_pref($pref_q, $pref_parent_q, $pref_invoice);
  
} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update($entity, $fieldname, $display);
  $pref_q = run_query_display_pref($uid,"deal",1);
  $pref_parent_q = run_query_display_pref($uid,"parentdeal",1);
  $pref_invoice = run_query_display_pref ($uid,"invoice",1);
  dis_deal_display_pref($pref_q, $pref_parent_q, $pref_invoice);

} else if ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update($entity, $new_level, $fieldorder);
  $pref_q = run_query_display_pref($uid,"deal",1);
  $pref_parent_q = run_query_display_pref($uid,"parentdeal",1);
  $pref_invoice = run_query_display_pref ($uid,"invoice",1);
  dis_deal_display_pref($pref_q, $pref_parent_q, $pref_invoice);
  

} elseif ($action == "admin")  {
///////////////////////////////////////////////////////////////////////////////
    require("deal_js.inc");
    html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus());
  
} elseif ($action == "kind_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_kind_insert($deal);
  if ($retour) {
    display_ok_msg($l_kind_insert_ok);
  } else {
    display_err_msg($l_kind_insert_error);
  }
  require("deal_js.inc");
  html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus());
  
} elseif ($action == "kind_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_kind_update($deal);
  if ($retour) {
    display_ok_msg($l_kind_update_ok);
  } else {
    display_err_msg($l_kind_update_error);
  }
  require("deal_js.inc");
  html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus());
  
} elseif ($action == "kind_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  dis_kind_links($deal["kind"]);
  require("deal_js.inc");
  html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus());
  
} elseif ($action == "kind_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_kind_delete($deal["kind"]);
  if ($retour) {
    display_ok_msg($l_kind_delete_ok);
  } else {
    display_err_msg($l_kind_delete_error);
  }
  require("deal_js.inc");
  html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus());
  
} elseif ($action == "status_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_status_insert($deal);
  if ($retour) {
    display_ok_msg($l_status_insert_ok);
  } else {
    display_err_msg($l_status_insert_error);
  }
  require("deal_js.inc");
  html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus());
  
} elseif ($action == "status_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_status_update($deal);
  if ($retour) {
    display_ok_msg($l_status_update_ok);
  } else {
    display_err_msg($l_status_update_error);
  }
  require("deal_js.inc");
  html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus());
  
} elseif ($action == "status_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  dis_status_links($deal["state"]);
  require("deal_js.inc");
  html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus());
  
} elseif ($action == "status_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_status_delete($deal["state"]);
  if ($retour) {
    display_ok_msg($l_status_delete_ok);
  } else {
    display_err_msg($l_status_delete_error);
  }
  require("deal_js.inc");
  html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus());
  
} elseif ($action == "cat_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_cat_insert($deal);
  if ($retour) {
    display_ok_msg($l_cat_insert_ok);
  } else {
    display_err_msg($l_cat_insert_error);
  }
  require("deal_js.inc");
  html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus());
  
} elseif ($action == "cat_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_cat_update($deal);
  if ($retour) {
    display_ok_msg($l_cat_update_ok);
  } else {
    display_err_msg($l_cat_update_error);
  }
  require("deal_js.inc");
  html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus());

} elseif ($action == "cat_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  dis_cat_links($deal["cat"]);
  require("deal_js.inc");
  html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus());
  
} elseif ($action == "cat_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_cat_delete($deal["cat"]);
  if ($retour) {
    display_ok_msg($l_cat_delete_ok);
  } else {
    display_err_msg($l_cat_delete_error);
  }
  require("deal_js.inc");
  html_deal_admin_form(run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus());
  

///////////////////////////////////////////////////////////////////////////////
// -- Actions about ParentDeal -- 
///////////////////////////////////////////////////////////////////////////////


} elseif ($action == "parent_search")  {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $usr_q = run_query_userobm();
  html_parentdeal_search_form($deal, $usr_q);
  dis_parentdeal_search_list($deal);
  

} elseif ($action == "parent_new") {
///////////////////////////////////////////////////////////////////////////////
    require("deal_js.inc");
    $obm_q = new DB_OBM;
    $usr_q = run_query_userobm();
    html_parentdeal_form($action,$obm_q, $usr_q,'');

} elseif ($action == "parent_detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_detail_parentdeal($param_parent);
  $obm_q_options = run_query_display_pref ($uid, "deal");
  $deal_q = run_query_search($deal, 0, $new_order, $order_dir);
  $num_rows = $deal_q->num_rows();

  html_parentdeal_consult($obm_q,$deal_q,$deal,$obm_q_options,$num_rows,run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus(),run_query_userobm());
  

} elseif ($action == "parent_detailupdate") {
///////////////////////////////////////////////////////////////////////////////
    if ($param_parent > 0) {
      require("deal_js.inc");
      $obm_q = run_query_detail_parentdeal($param_parent);
      $usr_q = run_query_userobm();
      display_record_info($obm_q->f("parentdeal_usercreate"),$obm_q->f("parentdeal_userupdate"),$obm_q->f("parentdeal_timecreate"),$obm_q->f("timeupdate"));
      html_parentdeal_form($action,$obm_q, $usr_q, $deal);
    } 
  
} elseif ($action == "parent_insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_parent_form("", $deal)) {
    $retour = run_query_insert_parentdeal($deal);
    if ($retour) {
      display_ok_msg($l_insert_ok); 
    } else {
      display_err_msg($err_msg);
    }
    html_parentdeal_search_form($deal, run_query_userobm());
  } else {
    require("deal_js.inc");
    display_warn_msg($err_msg);
    html_parentdeal_search_form($deal, run_query_userobm());
  }
  
} elseif ($action == "parent_delete") {
/////////////////////////////////////////////////////////////////////
  if (check_parent_has_deal($param_parent)) {
    display_err_msg($l_err_parent_has_deal);
    $obm_q = run_query_detail_parentdeal($param_parent);
    $usr_q = run_query_userobm();
    display_record_info($obm_q->f("parentdeal_usercreate"),$obm_q->f("parentdeal_userupdate"),$obm_q->f("parentdeal_timecreate"),$obm_q->f("timeupdate"));
    html_parentdeal_form($action,$obm_q, $usr_q, $deal);
  } else {
    run_query_delete_parentdeal($param_parent); 
    display_ok_msg($l_delete_ok); 
    require("deal_js.inc");
    html_parentdeal_search_form($deal, run_query_userobm());
  }    
  

} elseif  ($action == "parent_update") {
/////////////////////////////////////////////////////////////////////
  if (check_parent_form("", $deal)) {
    $retour = run_query_update_parentdeal($deal);
    if ($retour) {
      display_ok_msg($l_update_ok); 
    } else {
      display_err_msg($l_update_error);
    }
    $obm_q = run_query_detail_parentdeal($param_parent);
    $obm_q_options = run_query_display_pref ($uid,"deal");
    $deal_q = run_query_search($deal, 0, $new_order, $order_dir);
    $num_rows = $deal_q->num_rows();
    html_parentdeal_consult($obm_q,$deal_q,$deal,$obm_q_options,$num_rows,run_query_dealtype(),run_query_deal_tasktype(),run_query_dealstatus(),run_query_userobm());
  } else {
    display_err_msg($err_msg);
    require("deal_js.inc");
    $obm_q = run_query_detail_parentdeal($param_parent);
    $usr_q = run_query_userobm();
    display_record_info($obm_q->f("parentdeal_usercreate"),$obm_q->f("parentdeal_userupdate"),$obm_q->f("parentdeal_timecreate"),$obm_q->f("timeupdate"));
    html_parentdeal_form($action,$obm_q, $usr_q, $deal);
  }
  

} elseif ($action=="affect") {
///////////////////////////////////////////////////////////////////////////////
  require("deal_js.inc");
  $list_parent_q = run_query_search_parentdeal('');
  html_deal_affect($list_parent_q, $param_deal);
  

} elseif ($action == "affect_update") {
///////////////////////////////////////////////////////////////////////////////
  if ($param_deal>0) {
    run_query_affect_deal_parentdeal($param_deal, $deal);
    display_ok_msg($l_updateaffect_ok); 

    // then we display the deal
    $deal_q = run_query_detail($param_deal);
    if ( ($deal_q->f("deal_visibility")==0) ||
         ($deal_q->f("deal_usercreate")==$uid) ) {
      display_record_info($deal_q->f("deal_usercreate"),$deal_q->f("deal_userupdate"),$deal_q->f("timecreate"),$deal_q->f("timeupdate"));
      $cid = $deal_q->f("deal_company_id");
      $q_invoices = run_query_search_connected_invoices ($param_deal, $incl_arch);
      $q_invoices->next_record();
      $invoices_options = run_query_display_pref ($uid,"invoice");
      html_deal_consult($deal_q, run_query_contact_deal($cid), $cid, $q_invoices, $invoices_options);
    }
  } else {
    display_err_msg($l_query_error);
  }
  

}


///////////////////////////////////////////////////////////////////////////////
// Display end of page                                                       //
///////////////////////////////////////////////////////////////////////////////
display_end();



///////////////////////////////////////////////////////////////////////////////
// Stores in $deal hash, Deal parameters transmited
// returns : $deal hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_deal() {
  global $tf_num, $tf_label, $tf_datebegin, $param_parent, $sel_kind, $sel_cat;
  global $param_company, $sel_contact1, $sel_contact2, $sel_market, $sel_tech;
  global $tf_dateprop, $tf_amount, $sel_state, $tf_datealarm, $ta_com;
  global $cb_archive, $tf_todo, $cb_vis;
  global $tf_plabel, $sel_pmanager, $cb_parchive;
  global $hd_company_ad1, $hd_company_zip, $hd_company_town;
  global $tf_company_name, $tf_zip,$sel_manager, $tf_dateafter, $tf_datebefore;
  global $sel_pmarket, $sel_ptech, $ta_pcom, $sel_parent, $ch_contrat;
  global $param_deal, $hd_usercreate, $hd_timeupdate, $set_debug;
  global $tf_kind, $rd_kind_inout, $tf_status, $tf_order, $tf_cat, $tf_hitrate;

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
  } else {
    $deal["archive"] = "0";
  }
  if (isset ($tf_todo)) $deal["todo"] = $tf_todo;
  if (isset ($cb_vis)) $deal["vis"] = $cb_vis;

  if (isset ($hd_usercreate)) $deal["usercreate"] = $hd_usercreate;
  if (isset ($hd_timeupdate)) $deal["timeupdate"] = $hd_timeupdate;
  if (isset ($ch_contrat)) $deal["add_contract"] = $ch_contrat;
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
  if (isset ($tf_order)) $deal["status_order"] = $tf_order;

  // Admin - Category fields
  // $sel_cat -> "cat" is already set
  if (isset ($tf_cat)) $deal["cat_label"] = $tf_cat;

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
  global $l_header_display,$l_header_admin,$l_header_new_parent;
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
    'Url'      => "$path/deal/deal_index.php?action=parent_detailconsult",
    'Right'    => $deal_read,
    'Condition'=> array ('None') 
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

  // Parent Delete
  $actions["DEAL"]["parent_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/deal/deal_index.php?action=delete&amp;param_parent=".$deal["parent"]."",
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
