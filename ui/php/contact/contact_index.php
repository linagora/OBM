<?php
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
// - new                -- $company_id -- show the new contact form
// - detailconsult      -- $contact_id -- show the contact detail
// - detailupdate       -- $contact_id -- show the contact detail form
// - insert             -- form fields    -- insert the contact
// - update             -- form fields    -- update the contact
// - check_delete       -- $contact_id -- check links before delete
// - delete             -- $contact_id -- delete the contact
// - admin              --                -- admin index (kind)
// - statistics         --                -- statistics index 
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
$params = get_contact_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("contact_display.inc");
require("contact_query.inc");
require_once("contact_js.inc");
require_once("$obminclude/of/of_category.inc");

$uid = $auth->auth["uid"];

get_contact_action();
$perm->check_permissions($module, $action);
if (! check_privacy($module, "Contact", $action, $params["contact_id"], $uid)) {
  $display["msg"] = display_err_msg($l_error_visibility);
  $action = "index";
} else {
  update_last_visit("contact", $params["contact_id"], $action);
}
page_close();


///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_ids") {
  $display["search"] = dis_contact_search_form($params);
  if ($set_display == "yes") {
    $display["result"] = dis_contact_search_list($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "vcard") {
///////////////////////////////////////////////////////////////////////////////
  dis_contact_vcard_export($params);
  exit();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

} else if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_contact_search_form($params);
  if ($set_display == "yes") {
    $display["result"] = dis_contact_search_list($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }
  
} elseif ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_contact_search_form($params);
  $display["result"] = dis_contact_search_list($params);

} elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  if (isset($params["company_id"])) {
    $comp_q = run_query_contact_company($params["company_id"]);
  }
  $display["detail"] = dis_contact_form($action, $comp_q, $params);

} elseif ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  if ($params["contact_id"] > 0) {
    $display["detail"] = dis_contact_consult($params);
  }
  
} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  if ($params["contact_id"] > 0) {
    $con_q = run_query_contact_detail($params["contact_id"]);
    if ($con_q->num_rows() == 1) {
      $display["detailInfo"] = display_record_info($con_q);
      $display["detail"] = dis_contact_form($action, $con_q, $params);
    } else {
      $display["msg"] .= display_err_msg($l_err_reference);
    }
  }

} elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_contact_data_form("", $params)) {

    // If the context (same contacts) was confirmed ok, we proceed
    if ($params["confirm"] == $c_yes) {
      $id = run_query_contact_insert($params);
      if ($id > 0) {
        $params["contact_id"] = $id;
        $display["msg"] .= display_ok_msg("$l_contact : $l_insert_ok");
      } else {
        $display["msg"] .= display_err_msg("$l_contact : $l_insert_error");
      }
      $display["detail"] = dis_contact_consult($params);

    // If it is the first try, we warn the user if some contacts seem similar
    } else {
      $obm_q = check_contact_context("", $params);
      if ((is_object($obm_q)) && ($obm_q->num_rows() > 0)) {
	$display["title"] = display_title("$l_contact : $l_insert");
        $display["detail"] = dis_contact_warn_insert("", $obm_q, $params);
      } else {
        $id = run_query_contact_insert($params);
        if ($id > 0) {
          $params["contact_id"] = $id;
          $display["msg"] .= display_ok_msg("$l_contact : $l_insert_ok");
          $display["detail"] = dis_contact_consult($params);
        } else {
          $display["msg"] .= display_err_msg("$l_contact : $l_insert_error");
          $display["detail"] = dis_contact_form($action, "", $params);
        }
      }
    }

  // Form data are not valid
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = dis_contact_form($action, "", $params);
  }
  
} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_contact_data_form("", $params)) {
    $retour = run_query_contact_update($params);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_contact : $l_update_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_contact : $l_update_error");
    }
    if ($params["contact_id"] > 0) {
      $display["detail"] = dis_contact_consult($params);
    }    
  } else {
    $display["msg"] .= display_err_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = dis_contact_form($action, "", $params);
  }
  
} elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_contact($params["contact_id"])) {
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_can_delete_contact($params["contact_id"]);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_contact_consult($params);
  }

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_contact($params["contact_id"])) {
    $retour = run_query_contact_delete($params["contact_id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_contact : $l_delete_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_contact : $l_delete_error");
    }
    $display["search"] = dis_contact_search_form($params);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_contact_consult($params);
  }

} elseif ($action == "statistics") {
///////////////////////////////////////////////////////////////////////////////
  require_once("$obminclude/lang/$set_lang/statistic.inc");
  // Specific conf statistics lang file
  if ($conf_lang) {
    $lang_file = "$obminclude/conf/lang/$set_lang/statistic.inc";
    if (file_exists("$path/../".$lang_file)) {
      include("$lang_file");
    }
  }
  $display["title"] = display_title($l_stats);
  $display["detail"] = dis_category_contact_stats($params);

} elseif ($action == "admin") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_contact_admin_index();

} elseif ($action == "function_insert") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("contact", "function", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_function : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_function : $l_insert_error");
  }
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "function_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("contact", "function", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_function : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_function : $l_update_error");
  }
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "function_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("contact", "function", $params, "mono");

} elseif ($action == "function_delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("contact", "function", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_function : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_function : $l_delete_error");
  }
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "kind_insert") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_contact_kind_insert($params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_kind : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_kind : $l_insert_error");
  }
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "kind_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_contact_kind_update($params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_kind : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_kind : $l_update_error");
  }
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "kind_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= dis_contact_kind_links($params);

} elseif ($action == "kind_delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_contact_kind_delete($params["kind"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_kind : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_kind : $l_delete_error");
  }
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
  $params["contact_id"] = $params["ext_id"];
  if ($params["doc_nb"] > 0) {
    $nb = run_query_global_insert_documents($params, "contact");
    $display["msg"] .= display_ok_msg("$nb $l_document_added");
  } else {
    $display["msg"] .= display_err_msg($l_no_document_added);
  }
  if ($params["contact_id"] > 0) {
    $display["detail"] = dis_contact_consult($params);
  }
}

of_category_user_action_switch($module, $action, $params);

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_contact);
$display["end"] = display_end();
if (! $params["popup"]) {
  update_contact_action();
  $display["header"] = display_menu($module);
}

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Contact parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_contact_params() {
  
  // Get global params
  $params = get_global_params("Contact");
  
  // Get contact specific params
  if (isset ($params["town"])) $params["town"] = get_format_town($params["town"]);

  get_global_params_document($params);

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
//  Contact Action 
///////////////////////////////////////////////////////////////////////////////
function get_contact_action() {
  global $params, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete,$l_header_stats;
  global $l_header_consult,$l_header_vcard, $l_header_display, $l_header_admin;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

  of_category_user_module_action("contact");

// ext_get_ids
  $actions["contact"]["ext_get_ids"] = array (
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
    'Url'      => "$path/contact/contact_index.php?action=detailconsult&amp;contact_id=".$params["contact_id"]."",
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult','detailupdate','check_delete') 
                                    		 );

// Vcard Export
  $actions["contact"]["vcard"] = array (
    'Name'     => $l_header_vcard,
    'Url'      => "$path/contact/contact_index.php?action=vcard&amp;popup=1&amp;contact_id=".$params["contact_id"]."",
    'Right'    => $cright_read,
    'Privacy'  => true,    
    'Condition'=> array ('detailconsult','detailupdate','update','check_delete') 
                                       );

// Detail Update
  $actions["contact"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/contact/contact_index.php?action=detailupdate&amp;contact_id=".$params["contact_id"]."",
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
    'Url'      => "$path/contact/contact_index.php?action=check_delete&amp;contact_id=".$params["contact_id"]."",
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
  global $params, $actions, $path;

  $id = $params["contact_id"];
  if ($id > 0) {
    // Detail Consult
    $actions["contact"]["detailconsult"]["Url"] = "$path/contact/contact_index.php?action=detailconsult&amp;contact_id=$id";
    
    // Detail Update
    $actions["contact"]["detailupdate"]['Url'] = "$path/contact/contact_index.php?action=detailupdate&amp;contact_id=$id";
    $actions["contact"]["detailupdate"]['Condition'][] = 'insert';
    
    // Check Delete
    $actions["contact"]["check_delete"]['Url'] = "$path/contact/contact_index.php?action=check_delete&amp;contact_id=$id";
    $actions["contact"]["check_delete"]['Condition'][] = 'insert';
  }
}


?>
