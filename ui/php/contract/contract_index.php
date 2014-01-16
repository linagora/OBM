<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



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
// - new             -- $company_id, -- show the new Contract form
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
$params = get_contract_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
include("$obminclude/of/of_category.inc");
require("contract_query.inc");
require("contract_display.inc");
require("contract_js.inc");

get_contract_action();
$perm->check_permissions($module, $action);

if (! check_privacy($module, "Contract", $action, $params["contract_id"], $obm["uid"])) {
  $display["msg"] = display_err_msg($l_error_visibility);
  $action = "index";
} else {
  update_last_visit("contract", $params["contract_id"], $action);
}

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if (! $popup) {
  $display["header"] = display_menu($module);
}

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_id") {
  $display["search"] = dis_contract_search_form($params);
  if ($_SESSION['set_display'] == "yes") {
    $display["result"] = dis_contract_search_list($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

///////////////////////////////////////////////////////////////////////////////
// Normal calls
///////////////////////////////////////////////////////////////////////////////
} elseif ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_contract_search_form($params);
  if ($_SESSION['set_display'] == "yes") {
    $display["result"] = dis_contract_search_list($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }
  
} elseif ($action == "search")  {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_contract_search_form($params);
  $display["result"] = dis_contract_search_list($params);
  
} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_contract_form($action,$params,$param_company);

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_contract_consult($params);

} elseif ($action == "export") {
///////////////////////////////////////////////////////////////////////////////
  dis_contract_export($params["contract_id"]);
  exit();

} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_contract_form($action, $params,"");
  
} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_contract_form("", $params)) {
    // If the context (same contracts) was confirmed ok, we proceed
    if ($params['confirm'] == $c_yes) {
      $params["contract_id"] = run_query_contract_insert($params);
      $display["detail"] = dis_contract_consult($params);
      // If first try, we warn the user if some contracts seem similar
    } else {
      $obm_q = check_contract_context("", $params);
      if ($obm_q->num_rows() > 0) {
	     $display["detail"] = dis_contract_warn_insert("", $obm_q, $params);
      } else {
      	$params["contract_id"] = run_query_contract_insert($params);
      	if ($params["contract_id"]) {
      	  $display["msg"] .= display_ok_msg("$l_contract : $l_insert_ok");
      	  $display["detail"] = dis_contract_consult($params);
      	} else {
      	  $display["msg"] .= display_err_msg("$l_contract : $l_insert_error");
      	}
      }
    }
  } else {
    $display["msg"] .= display_err_msg($err["msg"]);
    $display["detail"] = dis_contract_form($action, $params,"");
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_contract_form("", $params)) {  
    $ret = run_query_contract_update($params);         
    if ($ret) {
      $display["msg"] .= display_ok_msg("$l_contract : $l_update_ok");
    } else {
        $display["msg"] .= display_err_msg("$l_contract : $l_update_error");
      }
    $display["search"] = dis_contract_consult($params);      
  } else {
      $display["msg"] .= display_err_msg($l_invalid_data. " : " . $err["msg"]);
      $display["detail"] = dis_contract_form($action, $params,"");
    }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_contract($params["contract_id"])) {
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_can_delete_contract($params["contract_id"]);
  } else {
    $display["msg"] .= display_warn_msg($err["msg"], false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_contract_consult($params);
  }

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_contract($params["contract_id"])) {
    $ret = run_query_contract_delete($params["contract_id"]);
    if ($ret) {
      $display["msg"] .= display_ok_msg("$l_contract : $l_delete_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_contract : $l_delete_error");
    }
    $display["search"] = dis_contract_search_form($params);
    if ($_SESSION['set_display'] == "yes") {
      $display["result"] = dis_contract_search_list($params);
    } else {
      $display["msg"] .= display_info_msg($l_no_display);
    }
  } else {
    $display["msg"] .= display_warn_msg($err["msg"], false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_contract_consult($params);
  }

} elseif ($action == "priority_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_contract_priority_insert($params);
  if ($retour) {
    $display["msg"] = display_ok_msg("$l_priority : $l_insert_ok");
  } else {
    $display["msg"] = display_err_msg("$l_priority : $l_insert_error");
  }
  $display["detail"] = dis_contract_admin_index();

} elseif ($action == "priority_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_contract_priority_links($params["priority"]);

} elseif ($action == "priority_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_contract_priority_update($params);
  if ($retour) {
    $display["msg"] = display_ok_msg("$l_priority : $l_update_ok");
  } else {
    $display["msg"] = display_err_msg("$l_priority : $l_update_error");
  }
  $display["detail"] = dis_contract_admin_index();

} elseif ($action == "priority_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_contract_priority_delete($params["priority"]);
  if ($retour) {
    $display["msg"] = display_ok_msg("$l_priority : $l_delete_ok");
  } else {
    $display["msg"] = display_err_msg("$l_priority : $l_delete_error");
  }
  $display["detail"] = dis_contract_admin_index();

} elseif ($action == "status_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_contract_status_insert($params);
  if ($retour) {
    $display["msg"] = display_ok_msg("$l_status : $l_insert_ok");
  } else {
    $display["msg"] = display_err_msg("$l_status : $l_insert_error");
  }
  $display["detail"] = dis_contract_admin_index();

} elseif ($action == "status_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_contract_status_update($params);
  if ($retour) {
    $display["msg"] = display_ok_msg("$l_status : $l_update_ok");
  } else {
    $display["msg"] = display_err_msg("$l_status : $l_update_error");
  }
  $display["detail"] = dis_contract_admin_index();

} elseif ($action == "status_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_contract_status_links($params["status"]);

} elseif ($action == "status_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_contract_status_delete($params["status"]);
  if ($retour) {
    $display["msg"] = display_ok_msg("$l_status : $l_delete_ok");
  } else {
    $display["msg"] = display_err_msg("$l_status : $l_delete_error");
  }
  $display["detail"] = dis_contract_admin_index();

} elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm["uid"], "contract", 1);
  $display["detail"] = dis_contract_display_pref($prefs);
  
} elseif ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm["uid"], "contract", 1);
  $display["detail"] = dis_contract_display_pref($prefs);
  
} elseif ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm["uid"], "contract", 1);
  $display["detail"] = dis_contract_display_pref($prefs);
  
} elseif ($action == "admin")  {
//////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_contract_admin_index();  
 
} elseif ($action == "type_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_contract_type_insert($params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_type : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_type : $l_insert_error");
  }
  $display["detail"] = dis_contract_admin_index();  
    
} elseif ($action == "type_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_contract_type_update($params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_type : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_type : $l_update_error");
  }
  $display["detail"] = dis_contract_admin_index();  

} elseif ($action == "type_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_contract_type_links($params["type"]);
  
} elseif ($action == "type_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_contract_type_delete($params["type"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_type : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_type : $l_delete_error");
  }
  $display["detail"] = dis_contract_admin_index();

} elseif ($action == "document_add")  {
///////////////////////////////////////////////////////////////////////////////
  $params["contract_id"] = $params["ext_id"];
  if ($params["doc_nb"] > 0) {
    $nb = run_query_global_insert_documents_links($params, "contract");
    $display["msg"] .= display_ok_msg("$nb $l_document_added");
  } else {
    $display["msg"] .= display_err_msg($l_no_document_added);
  }
  if ($params["contract_id"] > 0) {
    $display["detail"] = dis_contract_consult($params);
  }
}
///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
update_contract_action();
if (! $popup) {
  $display["header"] = display_menu($module);
}
$display["head"] = display_head($l_contract);
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Contract parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_contract_params() {
  
  // Get global params
  $params = get_global_params("Contract");
  
  // Get contract specific params
  $params["code"] = (isset($params["code"]) ? $params["code"] : "0");

  get_global_params_document($params);

  return $params;
}



//////////////////////////////////////////////////////////////////////////////
// Contract actions
//////////////////////////////////////////////////////////////////////////////
function get_contract_action() {
  global $params, $actions, $path, $l_select_company;
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
    'Url'      => "$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_select_company)."&amp;ext_url=".urlencode("$path/contract/contract_index.php?action=new&amp;company_id=")."",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Condition'=> array ('','search','index','detailconsult','admin','type_insert','type_update','type_delete','display','delete')
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
    'Url'      => "$path/contract/contract_index.php?action=detailconsult&amp;contract_id=".$params["contract_id"]."",
    'Right'    => $cright_read, 
    'Privacy'  => true,
    'Condition'=> array ('detailupdate')
                                    	);

// Detail Update
  $actions["contract"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/contract/contract_index.php?action=detailupdate&amp;contract_id=".$params["contract_id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'update') 
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
    'Url'      => "$path/contract/contract_index.php?action=check_delete&amp;contract_id=".$params["contract_id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,  
    'Condition'=> array ('detailconsult', 'detailupdate', 'update') 
                                     	 );

// Delete
  $actions["contract"]["delete"] = array (
    'Url'      => "$path/contract/contract_index.php?action=delete&amp;contract_id=".$params["contract_id"]."",
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
    'Url'      => "$path/contract/contract_index.php?action=export&amp;popup=1&amp;contract_id=".$params["contract_id"]."",
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

// Display Preference
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
// Contract Actions updates (after processing, before displaying menu)  
///////////////////////////////////////////////////////////////////////////////
function update_contract_action() {
  global $params, $actions, $path;

  $id = $params["contract_id"];
  if ($id > 0) {
    // Detail Consult
    $actions["contract"]["detailconsult"]['Url'] = "$path/contract/contract_index.php?action=detailconsult&amp;contract_id=$id";
    $actions["contract"]["detailconsult"]['Condition'][] = 'insert';
    
    // Detail Update
    $actions["contract"]["detailupdate"]['Url'] = "$path/contract/contract_index.php?action=detailupdate&amp;contract_id=$id";
    $actions["contract"]["detailupdate"]['Condition'][] = 'insert';

    // Check Delete
    $actions["contract"]["check_delete"]['Url'] = "$path/contract/contract_index.php?action=check_delete&amp;contract_id=$id";
    $actions["contract"]["check_delete"]['Condition'][] = 'insert';
  }
}

?>
