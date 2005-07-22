<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : contract_index.php                                           //
//     - Desc : Contract Support Index File                                  //
// 2001-07-17 : Aliacom                                                      //
///////////////////////////////////////////////////////////////////////////////
//  $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields   -- show the Contract search form
// - search          -- search fields   -- show the result set of search
// - new             -- $param_company, -- show the new Contract form
// - detailconsult   -- $param_contract -- show the Contract detail
// - detailupdate    -- $param_contract -- show the Contract detail form
// - insert          -- form fields     -- insert the Contract 
// - update          -- form fields     -- update the Contract
// - check_delete    -- $param_contract -- check links before delete
// - delete          -- $param_contract -- delete the Contract
// - admin	     --		        -- admin index (kind)
// - type_insert     -- form fields     -- insert the type
// - type_update     -- form fields     -- update the type
// - type_checklink  -- $sel_kind       -- check if kind is used
// - type_delete     -- $sel_kind       -- delete the type
// - display         --                 -- display and set display parameters
// - dispref_display --                 -- update one field display value
// - dispref_level   --                 -- update one field display position 
// External API ---------------------------------------------------------------
// - ext_get_id      -- $title          -- select a contract (return id) 
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "contract";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("contract_query.inc");
require("contract_display.inc");

$uid = $auth->auth["uid"];

if ($action == "") $action = "index";
$contract = get_param_contract();
get_contract_action();
$perm->check_permissions($module, $action);

if (! check_privacy($module, "Contract", $action, $contract["id"], $uid)) {
  $display["msg"] = display_err_msg($l_error_visibility);
  $action = "index";
} else {
  update_last_visit("contract", $contract["id"], $action);
}

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if (! $popup) {
  $display["header"] = generate_menu($module, $section);
}

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_id") {
  require("contract_js.inc");
  $display["search"] = dis_contract_search_form($contract);
  if ($set_display == "yes") {
    $display["result"] = dis_contract_search_list($contract);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

///////////////////////////////////////////////////////////////////////////////
// Normal calls
///////////////////////////////////////////////////////////////////////////////
} elseif ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  require("contract_js.inc");
  $display["search"] = dis_contract_search_form($contract);
  if ($set_display == "yes") {
    $display["result"] = dis_contract_search_list($contract);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }
  
} elseif ($action == "search")  {
///////////////////////////////////////////////////////////////////////////////
  require("contract_js.inc");
  $display["search"] = dis_contract_search_form($contract);
  $display["result"] = dis_contract_search_list($contract);
  
} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  require("contract_js.inc");  
  $display["detail"] = dis_contract_form($action,$contract,$param_company);

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_contract_consult($contract);

} elseif ($action == "export") {
///////////////////////////////////////////////////////////////////////////////
  dis_contract_export($contract["id"]);
  exit();

} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  require("contract_js.inc"); 
  $display["detail"] = dis_contract_form($action, $contract,"");
  
} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  require("contract_js.inc");
  if (check_contract_form("", $contract)) {
    // If the context (same contracts) was confirmed ok, we proceed
    if ($hd_confirm == $c_yes) {
      $contract["id"] = run_query_insert($contract);
      $display["detail"] = dis_contract_consult($contract);
      // If first try, we warn the user if some contracts seem similar
    } else {
      $obm_q = check_contract_context("", $contract);
      if ($obm_q->num_rows() > 0) {
	$display["detail"] = dis_contract_warn_insert("", $obm_q, $contract);
      } else {
	$contract["id"] = run_query_insert($contract);
	if ($contract["id"]) {
	  $display["msg"] .= display_ok_msg($l_insert_ok);
	  $display["detail"] = dis_contract_consult($contract);
	} else {
	  $display["msg"] .= display_err_msg($l_insert_error);
	}
      }
    }
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    $display["detail"] = dis_contract_form($action, $contract,"");
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_contract_form("", $contract)) {  
    $ret = run_query_update($contract);         
    if ($ret) {
      $display["msg"] .= display_ok_msg($l_update_ok);
    } else {
        $display["msg"] .= display_err_msg($l_update_error);
      }
    $display["search"] = dis_contract_consult($contract);      
  } else {
      require("contract_js.inc");
      $display["msg"] .= display_err_msg($l_invalid_da. " : " . $err_msg);
      $display["detail"] = dis_contract_form($action, $contract,"");
    }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  require("contract_js.inc");
  $display["detail"] = dis_check_links($contract["id"]);

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  $ret = run_query_delete($contract["id"]);
  if ($ret) {
    $display["msg"] .= display_ok_msg($l_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_delete_error);
  }
  $display["search"] = dis_contract_search_form($contract);

} elseif ($action == "priority_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_priority_insert($contract);
  if ($retour) {
    $display["msg"] = display_ok_msg($l_pri_insert_ok);
  } else {
    $display["msg"] = display_err_msg($l_pri_insert_error);
  }
  require("contract_js.inc");
  $display["detail"] = dis_admin_index();

} elseif ($action == "priority_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_priority_links($contract["priority"]);

} elseif ($action == "priority_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_priority_update($contract);
  if ($retour) {
    $display["msg"] = display_ok_msg($l_pri_update_ok);
  } else {
    $display["msg"] = display_err_msg($l_pri_update_error);
  }
  require("contract_js.inc");
  $display["detail"] = dis_admin_index();

} elseif ($action == "priority_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_priority_delete($contract["priority"]);
  if ($retour) {
    $display["msg"] = display_ok_msg($l_pri_delete_ok);
  } else {
    $display["msg"] = display_err_msg($l_pri_delete_error);
  }
  require("contract_js.inc");
  $display["detail"] = dis_admin_index();

} elseif ($action == "status_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_status_insert($contract);
  if ($retour) {
    display_ok_msg($l_sta_insert_ok);
  } else {
    display_err_msg($l_sta_insert_error);
  }
  require("contract_js.inc");
  $display["detail"] = dis_admin_index();

} elseif ($action == "status_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_status_update($contract);
  if ($retour) {
    $display["msg"] = display_ok_msg($l_sta_update_ok);
  } else {
    $display["msg"] = display_err_msg($l_sta_update_error);
  }
  require("contract_js.inc");
  $display["detail"] = dis_admin_index();

} elseif ($action == "status_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_status_links($contract["status"]);

} elseif ($action == "status_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_status_delete($contract["status"]);
  if ($retour) {
    $display["msg"] = display_ok_msg($l_sta_delete_ok);
  } else {
    $display["msg"] = display_err_msg($l_sta_delete_error);
  }
  require("contract_js.inc");
  $display["detail"] = dis_admin_index();

} elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($auth->auth["uid"], "contract", 1);
  $display["detail"] = dis_contract_display_pref($prefs);
  
} elseif ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($auth->auth["uid"], "contract", 1);
  $display["detail"] = dis_contract_display_pref($prefs);
  
} elseif ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($auth->auth["uid"], "contract", 1);
  $display["detail"] = dis_contract_display_pref($prefs);
  
} elseif ($action == "admin")  {
//////////////////////////////////////////////////////////////////////////////
  require("contract_js.inc");
  $display["detail"] = dis_admin_index();  
 
} elseif ($action == "type_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_type_insert($contract);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_type_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_type_insert_error);
  }
  require("contract_js.inc");
  $display["detail"] = dis_admin_index();  
    
} elseif ($action == "type_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_type_update($contract);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_type_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_type_update_error);
  }
  require("contract_js.inc");
  $display["detail"] = dis_admin_index();  

} elseif ($action == "type_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_type_links($contract["type"]);
  require("contract_js.inc");
  
} elseif ($action == "type_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_type_delete($contract["type"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_type_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_type_delete_error);
  }
  require("contract_js.inc");
  $display["detail"] = dis_admin_index();  
}

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
update_contract_action_url();
if (! $popup) {
  $display["header"] = generate_menu($module, $section);
}

$display["head"] = display_head($l_contract);
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Contract parameters transmited in $contract hash
// returns : $contract hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_contract() {
  global $tf_label,$tf_company,$sel_type, $tf_type,$rd_kind,$cb_autorenew;
  global $tf_dateafter,$tf_datebefore,$sel_manager,$cb_arc,$param_company;
  global $param_contract,$tf_num,$sel_market, $sel_tech, $sel_con1, $sel_con2;
  global $ta_clause;
  global $ta_com, $tf_datecomment, $sel_usercomment, $ta_add_comment;
  global $tf_datebegin,$tf_dateexp,$tf_daterenew,$tf_datecancel,$tf_datesignature;
  global $hd_usercreate,$cb_archive,$hd_timeupdate,$param_deal,$deal_label,$deal_new_id;
  global $hd_company_ad1, $hd_company_zip, $hd_company_town,$l_header_export;
  global $popup, $ext_title, $ext_target, $ext_widget, $ext_widget_text;
  global $cdg_param, $action,$sel_priority,$sel_status,$param_contract;
  global $tf_pri,$tf_order, $tf_status, $tf_color, $tf_ticket_nb;
  global $tf_duration, $rd_format, $cb_vis;

  if (isset ($popup)) $contract["popup"] = $popup;
  if (isset ($ext_title)) $contract["ext_title"] = stripslashes(urldecode($ext_title));
  if (isset ($ext_target)) $contract["ext_target"] = $ext_target;
  if (isset ($ext_widget)) $contract["ext_widget"] = $ext_widget;
  if (isset ($ext_widget_text)) $contract["ext_widget_text"] = $ext_widget_text;

  if (isset ($param_contract)) $contract["id"] = $param_contract;
  if (isset ($param_company)) $contract["company_id"] = $param_company;
  if (isset ($sel_priority)) $contract["priority"] = $sel_priority;
  if (isset ($sel_status)) $contract["status"] = $sel_status;
  if (isset( $cb_vis)) $contract["privacy"] = $cb_vis; 
  if (isset ($rd_kind)) $contract["kind"] = $rd_kind;
  if (isset ($tf_ticket_nb)) $contract["ticketnumber"] = $tf_ticket_nb;
  if (isset ($tf_duration)) $contract["duration"] = $tf_duration;
  if (isset ($rd_format)) $contract["format"] = $rd_format;
  if (isset ($tf_label)) $contract["label"] = $tf_label;
  if (isset ($tf_datebegin)) $contract["datebegin"] = $tf_datebegin;
  if (isset ($tf_dateexp)) $contract["dateexp"] = $tf_dateexp;
  if (isset ($tf_datesignature)) $contract["datesignature"] = $tf_datesignature;
  if (isset ($tf_daterenew)) $contract["daterenew"] = $tf_daterenew;
  if (isset ($tf_datecancel)) $contract["datecancel"] = $tf_datecancel;
  if (isset ($tf_num)) $contract["number"] = $tf_num;
  if (isset ($cb_archive)) $contract["archive"] = $cb_archive;
  if (isset ($sel_market)) $contract["market"] = $sel_market;
  if (isset ($sel_tech)) $contract["tech"] = $sel_tech;
  if (isset ($sel_con1)) $contract["contact1"] = $sel_con1;
  if (isset ($sel_con2)) $contract["contact2"] = $sel_con2;
  if (isset ($sel_type)) $contract["type"] = $sel_type;
  if (isset ($cb_arc)) $contract["arc"] = $cb_arc;
  if (isset ($ta_clause)) $contract["clause"] = $ta_clause;
  if (isset ($ta_com)) $contract["comment"] = $ta_com;
  if (isset ($tf_datecomment)) $contract["datecomment"] = $tf_datecomment;
  if (isset ($sel_usercomment)) $contract["usercomment"] = $sel_usercomment;
  if (isset ($ta_add_comment)) $contract["add_comment"] = trim($ta_add_comment);
  if (isset ($hd_usercreate)) $contract["usercreate"] = $hd_usercreate;
  if (isset ($hd_timeupdate)) $contract["timeupdate"] = $hd_timeupdate;
  if (isset ($cb_autorenew)) $contract["autorenewal"] = $cb_autorenew;

  // Admin - Priority fields
  // $sel_priority -> "priority" is already set
  if (isset ($tf_pri)) $contract["pri_label"] = $tf_pri;
  $contract["pri_order"] = (isset($tf_order) ? $tf_order : "0");
  $contract["pri_color"] = (isset($tf_color) ? $tf_color : "");

  // Admin - Status fields
  // $sel_status -> "status" is already set
  if (isset ($tf_status)) $contract["sta_label"] = $tf_status;
  $contract["sta_order"] = (isset($tf_order) ? $tf_order : "0");

  // Search fields
  if (isset ($tf_dateafter)) $contract["dateafter"] = $tf_dateafter;
  if (isset ($tf_datebefore)) $contract["datebefore"] = $tf_datebefore;
  if (isset ($sel_manager)) $contract["manager"] = $sel_manager;
  if (isset ($tf_company)) $contract["company_name"] = $tf_company;

  // Company infos (with company_name)
  if (isset ($hd_company_ad1)) $contract["company_ad1"] = $hd_company_ad1;
  if (isset ($hd_company_zip)) $contract["company_zip"] = $hd_company_zip;
  if (isset ($hd_company_town)) $contract["company_town"] = $hd_company_town;

  // Deal infos
  if (isset ($param_deal)) $contract["deal_id"] = $param_deal;
  if (isset ($deal_new_id)) $contract["deal_new_id"] = $deal_new_id;
  if (isset ($deal_label)) $contract["deal_label"] = $deal_label;
  if (isset ($tf_type)) $contract["type_label"] = $tf_type;

  display_debug_param($contract);

  return $contract;
}



//////////////////////////////////////////////////////////////////////////////
// Contract actions
//////////////////////////////////////////////////////////////////////////////
function get_contract_action() {
  global $contract, $actions, $path, $l_select_company;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult, $l_header_display, $l_header_admin;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin,$l_header_export;


// Ext Get Id
  $actions["contract"]["ext_get_id"] = array (
    'Url'      => "$path/contract/contract_index.php?action=ext_get_id",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    	);

// Index
  $actions["contract"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/contract/contract_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    	);

// Search
  $actions["contract"]["search"] = array (
    'Url'      => "$path/contract/contract_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    	);

// New
  $actions["contract"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_select_company)."&amp;ext_url=".urlencode("$path/contract/contract_index.php?action=new&amp;param_company=")."",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Condition'=> array ('','search','index','detailconsult','admin','type_insert','type_update','type_delete','display')
                                      );

// Insert
  $actions["contract"]["insert"] = array (
    'Url'      => "$path/contract/contract_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                    	);

// Detail Consult
  $actions["contract"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/contract/contract_index.php?action=detailconsult&amp;param_contract=".$contract["id"]."",
    'Right'    => $cright_read, 
    'Privacy'  => true,
    'Condition'=> array ('detailupdate') 
                                    	);

// Detail Update
  $actions["contract"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/contract/contract_index.php?action=detailupdate&amp;param_contract=".$contract["id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'insert', 'update') 
                                     	 );

// Update
  $actions["contract"]["update"] = array (
    'Url'      => "$path/contract/contract_index.php?action=update",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                    	);

// Check Delete
  $actions["contract"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/contract/contract_index.php?action=check_delete&amp;param_contract=".$contract["id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,  
    'Condition'=> array ('detailconsult') 
                                     	 );

// Delete
  $actions["contract"]["delete"] = array (
    'Url'      => "$path/contract/contract_index.php?action=delete&amp;param_contract=".$contract["id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	 );

// Document add
  $actions["contract"]["document_add"] = array (
    'Url'      => "$path/contract/contract_index.php?action=document_add",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
  					);
  
// Contract export
  $actions["contract"]["export"] = array (
    'Name'     => $l_header_export,
    'Url'      => "$path/contract/contract_index.php?action=export&amp;popup=1&amp;param_contract=".$contract["id"]."",
    'Right'    => $cright_read,
    'Privacy'  => true,    
    'Condition'=> array ('detailconsult','detailupdate','update') 
                                       );

// Admin
  $actions["contract"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/contract/contract_index.php?action=admin",
    'Right'    => $cright_read_admin, 
    'Condition'=> array ('all') 
                                      	);

// Admin Priority Insert
  $actions["contract"]["priority_insert"] = array (
    'Url'      => "$path/contract/contract_index.php?action=type_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                    	);

// Admin Priority Update
  $actions["contract"]["priority_update"] = array (
    'Url'      => "$path/contract/contract_index.php?action=type_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                    	);

// Admin Type Priority link
  $actions["contract"]["priority_checklink"] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                    	);

// Admin Priority Delete
  $actions["contract"]["priority_delete"] = array (
    'Url'      => "$path/contract/contract_index.php?action=type_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                    	);

// Status insert
  $actions["contract"]["status_insert"] = array (
    'Url'      => "$path/incident/contract_index.php?action=status_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Status update
  $actions["contract"]["status_update"] = array (
    'Url'      => "$path/incident/contract_index.php?action=status_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Status Check Link
  $actions["contract"]["status_checklink"] = array (
    'Url'      => "$path/incident/contract_index.php?action=status_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Status delete
  $actions["contract"]["status_delete"] = array (
    'Url'      => "$path/incident/contract_index.php?action=status_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Admin Type Insert
  $actions["contract"]["type_insert"] = array (
    'Url'      => "$path/contract/contract_index.php?action=type_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                    	);

// Admin Type Update
  $actions["contract"]["type_update"] = array (
    'Url'      => "$path/contract/contract_index.php?action=type_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                    	);

// Admin Type check link
  $actions["contract"]["type_checklink"] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                    	);

// Admin Type Delete
  $actions["contract"]["type_delete"] = array (
    'Url'      => "$path/contract/contract_index.php?action=type_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                    	);

// Display
  $actions["contract"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/contract/contract_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	  );

// Display Préférence
  $actions["contract"]["dispref_display"] = array (
    'Url'      => "$path/contract/contract_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                        	  );

// Display Level
  $actions["contract"]["dispref_level"] = array (
    'Url'      => "$path/contract/contract_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    	       );

}


///////////////////////////////////////////////////////////////////////////////
// Contract Actions URL updates (after processing, before displaying menu)  
///////////////////////////////////////////////////////////////////////////////
function update_contract_action_url() {
  global $contract, $actions, $path;

  // Detail Update
               
  $actions["contract"]["detailupdate"]['Url'] = "$path/contract/contract_index.php?action=detailupdate&amp;param_contract=".$contract["id"]."";

  // Check Delete
  $actions["contract"]["check_delete"]['Url'] = "$path/contract/contract_index.php?action=check_delete&amp;param_contract=".$contract["id"]."";

}



///////////////////////////////////////////////////////////////////////////////
// Display end of page                                                       //
///////////////////////////////////////////////////////////////////////////////
display_end();


</script>
