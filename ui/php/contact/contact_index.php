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
// - insert             -- form fields    -- insert the company
// - update             -- form fields    -- update the company
// - check_delete       -- $param_contact -- check links before delete
// - delete             -- $param_contact -- delete the company
// - admin              --                -- admin index (kind)
// - function_insert    -- form fields    -- insert the function
// - function_update    -- form fields    -- update the function
// - function_checklink --                -- check if function is used
// - function_delete    -- $sel_func      -- delete the function
// - kind_insert        -- form fields    -- insert the kind
// - kind_update        -- form fields    -- update the kind
// - kind_checklink     --                -- check if kind is used
// - kind_delete     	-- $sel_kind      -- delete the kind
// - display            --                -- display and set display parameters
// - dispref_display    --                -- update one field display value
// - dispref_level      --                -- update one field display position 
// External API ---------------------------------------------------------------
// - ext_get_ids     --                -- select multiple contacts (return id) 
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Session, Auth, Perms Management                                           //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "COM";
$menu = "CONTACT";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("contact_display.inc");
require("contact_query.inc");

$uid = $auth->auth["uid"];

// updating the contact bookmark : 
if ( ($param_contact == $last_contact) && (strcmp($action,"delete")==0) ) {
  $last_contact = $last_contact_default;
} else if ( ($param_contact > 0) && ($last_contact != $param_contact) ) {
  $last_contact = $param_contact;
  run_query_set_user_pref($auth->auth["uid"],"last_contact",$param_contact);
  $last_contact_name = run_query_global_contact_name($last_contact);
  //$sess->register("last_contact");  
}

page_close();

if ($action == "") $action = "index";
$contact = get_param_contact();
get_contact_action();
$perm->check_permissions($menu, $action);


if (! $contact["popup"]) {
  $display["header"] = generate_menu($menu,$section);
}

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
}

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if ($action == "index" || $action == "") {
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
  if (isset($param_company)) {
    $comp_q = run_query_contact_company($param_company);
  }
  require("contact_js.inc");
  $display["detail"] = dis_contact_form($action, $comp_q, $contact);

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_contact > 0) {
    $display["detail"] = dis_contact_consult($contact);
  }
  
} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_contact > 0) {
    $con_q = run_query_contact_detail($param_contact);
    if ($con_q->num_rows() == 1) {
      require("contact_js.inc");
      $display["detailInfo"] = display_record_info($con_q);
      $display["detail"] = dis_contact_form($action, $con_q, $contact);
    } else {
      $display["msg"] .= display_err_msg($l_query_error . " - " . $con_q->query . " !");
    }
  }
} elseif ($action == "externaldetail")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_contact > 0 && $module !="") {
    include("$path/$module/".$module."_display.inc");
    $con_q = run_query_contact_detail($param_contact);
    if ($con_q->num_rows() != 1) {
      $display["msg"] .= display_err_msg($l_query_error . " - " . $con_q->query . " !");
    }
    if ( ($con_q->f("contact_visibility")==0) || ($con_q->f("contact_usercreate") == $uid) ) {
      $display["detailInfo"] = display_record_info($con_q);
      $display["detail"] = html_contact_header($con_q);
      $display["detail"] .= dis_subscription_external_list();
    } else {
      // this contact's page has "private" access
      $display["msg"] .= display_err_msg($l_error_visibility);
    }      
  }
  
} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_contact_data_form("", $contact)) {

    // If the context (same contacts) was confirmed ok, we proceed
    if ($hd_confirm == $c_yes) {
      $retour = run_query_contact_insert($contact);
      if ($retour) {
        $display["msg"] .= display_ok_msg($l_insert_ok);
      } else {
        $display["msg"] .= display_err_msg($l_insert_error);
      }
      $display["search"] = dis_contact_search_form($contact);

    // If it is the first try, we warn the user if some contacts seem similar
    } else {
      $obm_q = check_contact_context("", $contact);
      if ($obm_q->num_rows() > 0) {
	$display["title"] = display_title("$l_contact : $l_insert");
        $display["detail"] = dis_contact_warn_insert("", $obm_q, $contact);
      } else {
        $retour = run_query_contact_insert($contact);
        if ($retour) {
          $display["msg"] .= display_ok_msg($l_insert_ok);
        } else {
          $display["msg"] .= display_err_msg($l_insert_error);
        }
        $display["search"] = dis_contact_search_form($contact);
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
    $con_q = run_query_contact_detail($param_contact);
    $display["detailInfo"] = display_record_info($con_q);
    $cat1_q = run_query_get_contactcategory1_label($con_q->f("contact_id"));
    $cat2_q = run_query_get_contactcategory2_label($con_q->f("contact_id"));
    $display["detail"] = html_contact_consult($con_q,$cat1_q,$cat2_q);
  } else {
    $display["msg"] .= display_err_msg($l_invalid_data . " : " . $err_msg);
    require("contact_js.inc");
    $display["detail"] = dis_contact_form($action, "", $contact);
  }
  
} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  require("contact_js.inc");
  $display["detail"] = dis_check_links($param_contact);
  
} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_contact_delete($param_contact);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_delete_error);
  }
  $display["search"] = dis_contact_search_form($contact);

} elseif ($action == "admin")  {
///////////////////////////////////////////////////////////////////////////////
  require("contact_js.inc");
  $display["detail"] = dis_contact_admin_index();

} elseif ($action == "cat1_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_cat1_insert($contact);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_cat1_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_cat1_insert_error);
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "cat1_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_cat1_update($contact);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_cat1_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_cat1_update_error);
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "cat1_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= dis_cat1_links($contact);

} elseif ($action == "cat1_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_cat1_delete($contact["category1"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_cat1_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_cat1_delete_error);
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "cat2_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_cat2_insert($contact);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_cat2_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_cat2_insert_error);
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "cat2_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_cat2_update($contact);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_cat2_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_cat2_update_error);
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

} elseif ($action == "cat2_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= dis_cat2_links($contact);

} elseif ($action == "cat2_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_cat2_delete($contact["category2"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_cat2_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_cat2_delete_error);
  }
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

}elseif ($action == "function_insert")  {
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
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

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
  require("contact_js.inc");
  $display["detail"] .= dis_contact_admin_index();

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

}  elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $pref_q = run_query_display_pref($uid, "contact", 1);
  $display["detail"] = dis_contact_display_pref($pref_q); 
  
} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update($entity, $fieldname, $disstatus);
  $pref_q = run_query_display_pref($uid, "contact", 1);
  $display["detail"] = dis_contact_display_pref($pref_q);
  
} else if ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update($entity, $new_level, $fieldorder);
  $pref_q = run_query_display_pref($uid, "contact", 1);
  $display["detail"] = dis_contact_display_pref($pref_q);

} elseif ($action == "document_add")  {
///////////////////////////////////////////////////////////////////////////////
  if ($contact["doc_nb"] > 0) {
    $nb = run_query_insert_documents($contact,"Contact");
    $display["msg"] .= display_ok_msg("$nb $l_document_added");
  } else {
    $display["msg"] .= display_err_msg($l_no_document_added);
  }
  $con_q = run_query_contact_detail($contact["id"]);
  if ($con_q->num_rows() != 1) {
    $display["msg"] .= display_err_msg($l_query_error . " - " . $con_q->query . " !");
  }
  if ( ($con_q->f("contact_visibility")==0) || ($con_q->f("contact_usercreate") == $uid) ) {
    $display["detailInfo"] = display_record_info($con_q);
    $cat1_q = run_query_get_contactcategory1_label($con_q->f("contact_id"));
    $cat2_q = run_query_get_contactcategory2_label($con_q->f("contact_id"));
    $display["detail"] = html_contact_consult($con_q,$cat1_q,$cat2_q);
  } else {
    // this contact's page has "private" access
    $display["msg"] .= display_err_msg($l_error_visibility);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_contact);
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Contact parameters transmited in $contact hash
// returns : $contact hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_contact() {
  global $action;
  global $sel_dsrc, $sel_kind, $tf_lname, $tf_fname, $tf_company;
  global $tf_ad1, $tf_ad2, $tf_ad3, $tf_zip, $tf_town, $tf_cdx, $sel_ctry;
  global $sel_func, $tf_title, $tf_phone, $tf_hphone, $tf_mphone, $tf_fax;
  global $sel_market, $tf_email, $cb_mailok, $ta_com, $cb_vis, $cb_archive;
  global $param_company, $param_contact, $hd_usercreate, $cdg_param;
  global $company_name, $company_new_name, $company_new_id;
  global $tf_func, $tf_label, $tf_lang, $tf_header,$view;
  global $popup, $ext_action, $ext_url, $ext_id, $ext_target, $ext_title;
  global $tf_cat1,$tf_cat2,$sel_cat1, $sel_cat2,$tf_code2,$tf_code1;
  global $HTTP_POST_VARS,$HTTP_GET_VARS;

  if ((is_array ($HTTP_POST_VARS)) && (count($HTTP_POST_VARS) > 0)) {
    $http_obm_vars = $HTTP_POST_VARS;
  } elseif ((is_array ($HTTP_GET_VARS)) && (count($HTTP_GET_VARS) > 0)) {
    $http_obm_vars = $HTTP_GET_VARS;
  }

  if (isset ($http_obm_vars)) {
    $nb_d = 0;
    $nb_contact = 0;
    while ( list( $key ) = each( $http_obm_vars ) ) {
      if (strcmp(substr($key, 0, 4),"cb_d") == 0) {
	$nb_d++;
	$d_num = substr($key, 4);
	$contact["doc$nb_d"] = $d_num;
      }
    }
    $contact["doc_nb"] = $nb_d;
  }
  if (isset ($view)) $contact["view"] = $view;
  if (isset ($sel_cat1)) $contact["category1"] = $sel_cat1;
  if (isset ($sel_cat2)) $contact["category2"] = $sel_cat2;
  if (isset ($tf_cat1)) $contact["cat1_label"] = $tf_cat1;
  if (isset ($tf_cat2)) $contact["cat2_label"] = $tf_cat2;
  if (isset ($tf_cat1)) $contact["cat1_code"] = $tf_code1;
  if (isset ($tf_cat2)) $contact["cat2_code"] = $tf_code2;  
  if (isset ($param_contact)) $contact["id"] = $param_contact;
  if (isset ($hd_usercreate)) $contact["usercreate"] = $hd_usercreate;
  if (isset ($sel_dsrc)) $contact["datasource"] = $sel_dsrc;
  if (isset ($sel_kind)) $contact["kind"] = $sel_kind;
  if (isset ($sel_market)) $contact["marketing_manager"] = $sel_market;
  if (isset ($tf_lname)) $contact["lname"] = trim($tf_lname);
  if (isset ($tf_fname)) $contact["fname"] = trim($tf_fname);
  if (isset ($param_company)) $contact["company_id"] = $param_company;
  if (isset ($tf_company)) $contact["company"] = $tf_company;
  if (isset ($company_name)) $contact["company_name"] = $company_name;
  if (isset ($company_new_name)) $contact["comp_new_name"] = $company_new_name;
  if (isset ($company_new_id)) $contact["comp_new_id"] = $company_new_id;
  if (isset ($tf_ad1)) $contact["ad1"] = $tf_ad1;
  if (isset ($tf_ad2)) $contact["ad2"] = $tf_ad2;
  if (isset ($tf_ad3)) $contact["ad3"] = $tf_ad3;
  if (isset ($tf_zip)) $contact["zip"] = $tf_zip;
  if (isset ($tf_town)) $contact["town"] = $tf_town;
  if (isset ($tf_cdx)) $contact["cdx"] = $tf_cdx;
  if (isset ($sel_ctry)) $contact["country"] = $sel_ctry;
  if (isset ($sel_func)) $contact["function"] = $sel_func;
  if (isset ($tf_title)) $contact["title"] = $tf_title;
  if (isset ($tf_phone)) $contact["phone"] = trim($tf_phone);
  if (isset ($tf_hphone)) $contact["hphone"] = trim($tf_hphone);
  if (isset ($tf_mphone)) $contact["mphone"] = trim($tf_mphone);
  if (isset ($tf_fax)) $contact["fax"] = trim($tf_fax);
  if (isset ($tf_email)) $contact["email"] = trim($tf_email);
  if (isset ($cb_archive)) $contact["archive"] = ($cb_archive == 1 ? 1 : 0);
  if (isset ($cb_vis)) $contact["vis"] = ($cb_vis == 1 ? 1 : 0);
  if (isset ($cb_mailok)) $contact["mailok"] = ($cb_mailok == 1 ? 1 : 0);
  if (isset ($ta_com)) $contact["com"] = $ta_com;

  // Admin - Function fields
  // $sel_func -> "function" is already set
  if (isset ($tf_func)) $contact["func_label"] = $tf_func;

  // Admin - Kind fields
  if (isset ($tf_label)) $contact["kind_label"] = $tf_label;
  if (isset ($tf_lang)) $contact["kind_lang"] = $tf_lang;
  if (isset ($tf_header)) $contact["kind_header"] = $tf_header;

  // External param
  if (isset ($popup)) $contact["popup"] = $popup;
  if (isset ($ext_action)) $contact["ext_action"] = $ext_action;
  if (isset ($ext_url)) $contact["ext_url"] = $ext_url;
  if (isset ($ext_id)) $contact["ext_id"] = $ext_id;
  if (isset ($ext_id)) $contact["id"] = $ext_id;
  if (isset ($ext_target)) $contact["ext_target"] = $ext_target;
  if (isset ($ext_title)) $contact["ext_title"] = $ext_title;

  if (debug_level_isset($cdg_param)) {
    echo "<br />action=$action";
    if ( $contact ) {
      while ( list( $key, $val ) = each( $contact ) ) {
        echo "<br />contact[$key]=$val";
      }
    }
  }

  return $contact;
}


///////////////////////////////////////////////////////////////////////////////
//  Contact Action 
///////////////////////////////////////////////////////////////////////////////
function get_contact_action() {
  global $contact, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult, $l_header_display, $l_header_admin;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["CONTACT"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/contact/contact_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                        );

// Search
  $actions["CONTACT"]["search"] = array (
    'Url'      => "$path/contact/contact_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    	);

// New
  $actions["CONTACT"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/contact/contact_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('','index','search','new','detailconsult','update','admin','display') 
                                     );

// Detail Consult
 $actions["CONTACT"]["detailconsult"]   = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$contact["id"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate') 
                                    		 );

// Detail Update
  $actions["CONTACT"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/contact/contact_index.php?action=detailupdate&amp;param_contact=".$contact["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'update') 
                                     		 );

// Insert
  $actions["CONTACT"]["insert"] = array (
    'Url'      => "$path/contact/contact_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	);

// Update
  $actions["CONTACT"]["update"] = array (
    'Url'      => "$path/contact/contact_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	);

// Check Delete
  $actions["CONTACT"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/contact/contact_index.php?action=check_delete&amp;param_contact=".$contact["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update') 
                                     	      );

// Delete
  $actions["CONTACT"]["delete"] = array (
    'Url'      => "$path/contact/contact_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	);

// Admin
  $actions["CONTACT"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/contact/contact_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                      		 );

// Function Insert
  $actions["CONTACT"]["function_insert"] = array (
    'Url'      => "$path/contact/contact_index.php?action=function_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Function Update
  $actions["CONTACT"]["function_update"] = array (
    'Url'      => "$path/contact/contact_index.php?action=function_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Function Check Link
  $actions["CONTACT"]["function_checklink"] = array (
    'Url'      => "$path/contact/contact_index.php?action=function_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Function Delete
  $actions["CONTACT"]["function_delete"] = array (
    'Url'      => "$path/contact/contact_index.php?action=function_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Kind Insert
  $actions["CONTACT"]["kind_insert"] = array (
    'Url'      => "$path/contact/contact_index.php?action=kind_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Kind Update
  $actions["CONTACT"]["kind_update"] = array (
    'Url'      => "$path/contact/contact_index.php?action=kind_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Kind Check Link
  $actions["CONTACT"]["kind_checklink"] = array (
    'Url'      => "$path/contact/contact_index.php?action=kind_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Kind Delete
  $actions["CONTACT"]["kind_delete"] = array (
    'Url'      => "$path/contact/contact_index.php?action=kind_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Category Insert
  $actions["CONTACT"]["cat1_insert"] = array (
    'Url'      => "$path/contact/contact_index.php?action=cat1_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Category Update
  $actions["CONTACT"]["cat1_update"] = array (
    'Url'      => "$path/contact/contact_index.php?action=cat1_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Category Check Link
  $actions["CONTACT"]["cat1_checklink"] = array (
    'Url'      => "$path/contact/contact_index.php?action=cat1_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Category Delete
  $actions["CONTACT"]["cat1_delete"] = array (
    'Url'      => "$path/contact/contact_index.php?action=cat1_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Category Insert
  $actions["CONTACT"]["cat2_insert"] = array (
    'Url'      => "$path/contact/contact_index.php?action=cat2_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Category Update
  $actions["CONTACT"]["cat2_update"] = array (
    'Url'      => "$path/contact/contact_index.php?action=cat2_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Category Check Link
  $actions["CONTACT"]["cat2_checklink"] = array (
    'Url'      => "$path/contact/contact_index.php?action=cat2_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Category Delete
  $actions["CONTACT"]["cat2_delete"] = array (
    'Url'      => "$path/contact/contact_index.php?action=cat2_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Dispay
  $actions["CONTACT"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/contact/contact_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );

// Dispay Preferences
  $actions["CONTACT"]["displref_level"]	= array (
    'Url'      => "$path/contact/contact_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	        );

// Dispay Level
  $actions["CONTACT"]["displref_level"]= array (
    'Url'      => "$path/contact/contact_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      		 );

}

</script>
