<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : list_index.php                                               //
//     - Desc : List Index File                                              //
// 1999-03-19 - Aliacom - Vincent MARGUERIT                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the list search form
// - search          -- search fields  -- show the result set of search
// - new             --                -- show the new list form
// - detailconsult   -- $param_list    -- show the list detail
// - detailupdate    -- $param_list    -- show the list detail form
// - insert          -- form fields    -- insert the list
// - update          -- form fields    -- update the list
// - delete          -- $param_list    -- delete the list
// - contact_add     -- 
// - contact_del     -- 
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
// - export_add      --                --
// External API ---------------------------------------------------------------
// - ext_get_ids     --                -- select multiple lists (return id) 
///////////////////////////////////////////////////////////////////////////////
$www = "<p class=\"messageInfo\">
    	<a href=\"http://validator.w3.org/check/referer\"><img
        src=\"http://www.w3.org/Icons/valid-xhtml10\"
        alt=\"Valid XHTML 1.0!\" height=\"31\" width=\"88\" /></a>
	<a href=\"http://jigsaw.w3.org/css-validator/\">
 	 <img style=\"border:0;width:88px;height:31px\"
       src=\"http://jigsaw.w3.org/css-validator/images/vcss\" 
       alt=\"Valid CSS!\" />
	 </a>
  	</p>";


///////////////////////////////////////////////////////////////////////////////
// Session,Auth,Perms Management                                             //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "COM";
$menu = "LIST";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
require("$obminclude/global_pref.inc");

include("list_display.inc");
include("list_query.inc");

// updating the list bookmark : 
if ( ($param_list == $last_list) && (strcmp($action,"delete")==0) ) {
  $last_list = $last_list_default;
} else if ( ($param_list > 0 ) && ($last_list != $param_list) ) {
  $last_list = $param_list;
  run_query_set_user_pref($auth->auth["uid"],"last_list", $param_list);
  $last_list_name = run_query_global_list_name($last_list);
}

if($action == "") $action = "index";
$uid = $auth->auth["uid"];
$list = get_param_list();
get_list_action();
$perm->check();

// ses_list is the session array of lists id to export
if (sizeof($ses_list) >= 1) {
  $sess->register("ses_list");
}
if ($action != "export_add") {
  $ses_list = "";
  $sess->unregister("ses_list");
}
page_close();

require("list_js.inc");

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if (! $popup) {
  $display["header"] = generate_menu($menu, $section); // Menu
}
///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "new_criterion") {
  $display["detail"] = dis_add_criterion_form($list);
}
elseif (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_list_search_form($list);
  if ($set_display == "yes") {
    $display["result"] = dis_list_search_list("", $popup);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }
}

else if ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_list_search_form($list);
  $display["result"] = dis_list_search_list($list, $popup);
}

else if ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_list_form($action, "", $list);
}
else if ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $list_q = run_query_detail($list["id"]);
  $pref_con_q = run_query_display_pref($uid, "list_contact");
  $con_q = run_query_contacts_list($list, $entity);
  $display["detail"] = html_list_consult($list_q, $pref_con_q, $con_q);
}

else if ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_detail($list["id"]);
  $display["detail"] = dis_list_form($action, $obm_q, $list);
}

else if ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if(isset($list["criteria"])) {
    $list["query"] = make_query($list);
  }
  if (check_data_form("", $list)) {
    // If the context (same list) was confirmed ok, we proceed
    if ($hd_confirm == $c_yes) {
      $retour = run_query_insert($list);
      if ($retour) {
        $display["msg"] .= display_ok_msg($l_insert_ok);
      } else {
        $display["msg"] .= display_err_msg($l_insert_error);
      }
      $display["search"] = html_list_search_form($list);

    // If it is the first try, we warn the user if some lists seem similar
    } else {
      $obm_q = check_list_context("", $list);
      if ($obm_q->num_rows() > 0) {
        $display["detail"] = dis_list_warn_insert($obm_q, $list);
      } else {
        $retour = run_query_insert($list);
        if ($retour) {
          $display["msg"] .= display_ok_msg($l_insert_ok);
        } else {
          $display["msg"] .= display_err_msg($l_insert_error);
        }
        $display["search"] = html_list_search_form($list);
      }
    }

  // Form data are not valid
  } else {
    $display["msg"] .= display_warn_msg($err_msg);
    $display["detail"] = dis_list_form($action, "", $list);
  }


} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if(isset($list["criteria"])) {
    $list["query"] = make_query($list);
  }
  if (check_data_form($list["id"], $list)) {
    $retour = run_query_update($list);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok);
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
    }
    $list_q = run_query_detail($list["id"]);
    $pref_con_q = run_query_display_pref($uid, "list_contact");
    $con_q = run_query_contacts_list($list);
    $display["detail"] = html_list_consult($list_q, $pref_con_q, $con_q);
  } else {
    $display["msg"] .= display_warn_msg($err_msg);
    $list_q = run_query_detail($list["id"]);
    $display["detail"] = dis_list_form($action, $list_q, $list);
  }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_warn_delete($hd_list_id);

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {
    $retour = run_query_delete($hd_list_id);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_delete_ok);
    } else {
      $display["msg"] .= display_err_msg($l_delete_error);
    }
    $display["search"] = html_list_search_form("");
  } else {
   $display["msg"] .= display_err_msg($l_error_permission);
  }

} elseif ($action == "contact_add")  {
///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {
    if ($list["con_nb"] > 0) {
      $nb = run_query_contactlist_insert($list);
      $display["msg"] .= display_ok_msg("$nb $l_contact_added");
    } else {
      $display["msg"] .= display_err_msg("no contact to add");
    }
    $list_q = run_query_detail($list["id"]);
    $pref_con_q = run_query_display_pref($uid, "list_contact");
    $con_q = run_query_contacts_list($list);
    $display["detail"] = html_list_consult($list_q, $pref_con_q, $con_q);
  } else {
    $display["msg"] .= display_err_msg($l_error_permission);
  }

} elseif ($action == "contact_del")  {
///////////////////////////////////////////////////////////////////////////////
if ($perm->have_perm("editor")) {
  if ($list["con_nb"] > 0) {
    $nb = run_query_contactlist_delete($list);
      $display["msg"] .= display_ok_msg("$nb $l_contact_removed");
    } else {
      $display["msg"] .= display_err_msg("no contact to delete");
    }
    $list_q = run_query_detail($list["id"]);
    $pref_con_q = run_query_display_pref($uid, "list_contact");
    $con_q = run_query_contacts_list($list);
    $display["detail"] = html_list_consult($list_q, $pref_con_q, $con_q);
  } else {
    $display["msg"] .= display_err_msg($l_error_permission);
  }

} else if ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $pref_q = run_query_display_pref($uid, "list", 1);
  $pref_con_q = run_query_display_pref($uid, "list_contact", 1);
  $display["detail"] = dis_list_display_pref($pref_q, $pref_con_q);
}

else if($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update($entity, $fieldname, $disstatus);
  $pref_q = run_query_display_pref($uid, "list", 1);
  $pref_con_q = run_query_display_pref($uid, "list_contact", 1);
  $display["detail"] = dis_list_display_pref($pref_q, $pref_con_q);
}

else if($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update($entity, $new_level, $fieldorder);
  $pref_q = run_query_display_pref($uid, "list", 1);
  $pref_con_q = run_query_display_pref($uid, "list_contact", 1);
  $display["detail"] = dis_list_display_pref($pref_q, $pref_con_q);
}

else if($action == "export_add") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_export_form($list);
}

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_ids") {
  $display["search"] = html_list_search_form($list);
  if ($set_display == "yes") {
    $display["detail"] = dis_list_search_list($list, $popup);
  } else {
    $display["msg"] .= display_ok_msg($l_no_display);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_list);
$display["end"] = display_end();

display_page($display);
exit(0);

///////////////////////////////////////////////////////////////////////////////
// Stores in $list hash, List parameters transmited
// returns : $list hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_list() {
  global $tf_name, $tf_subject, $tf_email, $ta_query, $tf_contact;
  global $param_list, $param_ext, $hd_usercreate, $hd_timeupdate, $cdg_param;
  global $action, $ext_action, $ext_url, $ext_id, $ext_target, $title;
  global $new_order, $order_dir,$popup,$row_index;
  global $tf_company_name,$tf_company_country,$tf_company_zipcode,$tf_company_town;
  global $tf_contact_firstname,$tf_contact_lastname,$tf_contact_country;
  global $tf_contact_zipcode,$tf_contact_town,$sel_log_and,$sel_log_not;
  global $tf_publication_title,$tf_publication_lang,$tf_publication_year;
  global $se_criteria;
  global $HTTP_POST_VARS, $HTTP_GET_VARS, $ses_list;

  // List fields
  if (isset ($param_ext)) $list["id"] = $param_ext;
  if (isset ($param_list)) $list["id"] = $param_list;
  if (isset ($tf_name)) $list["name"] = trim($tf_name);
  if (isset ($tf_subject)) $list["subject"] = trim($tf_subject);
  if (isset ($tf_email)) $list["email"] = $tf_email;
  if (isset ($ta_query)) $list["query"] = trim($ta_query);
  if (isset ($tf_contact)) $list["contact"] = trim($tf_contact);
  if (isset ($row_index)) $list["row_index"] = $row_index;

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

  //Criteria params :
  //Company
  if (isset ($tf_company_name)) $list["criteria"]["modules"]["company"]["company_name"] = $tf_company_name;
  if (isset ($tf_company_country)) $list["criteria"]["modules"]["company"]["c1.country_name"] = $tf_company_country;
  if (isset ($tf_company_zipcode)) $list["criteria"]["modules"]["company"]["company_zipcode"] = $tf_company_zipcode;
  if (isset ($tf_company_town)) $list["criteria"]["modules"]["company"]["company_town"] = $tf_company_town;
  //Contact
  if (isset ($tf_contact_firstname)) $list["criteria"]["modules"]["contact"]["contact_firstname"] = $tf_contact_firstname;
  if (isset ($tf_contact_lastname)) $list["criteria"]["modules"]["contact"]["contact_lastname"] = $tf_contact_lastname;
  if (isset ($tf_contact_country)) $list["criteria"]["modules"]["contact"]["c2.country_name"] = $tf_contact_country;
  if (isset ($tf_contact_zipcode)) $list["criteria"]["modules"]["contact"]["contact_zipcode"] = $tf_contact_zipcode;
  if (isset ($tf_contact_town)) $list["criteria"]["modules"]["contact"]["contact_town"] = $tf_contact_town;
  //Publication
  if (isset ($tf_publication_title)) $list["criteria"]["modules"]["publication"]["publication_title"] = $tf_publication_title;
  if (isset ($tf_publication_lang)) $list["criteria"]["modules"]["publication"]["publication_lang"] = $tf_publication_lang;
  if (isset ($tf_publication_year)) $list["criteria"]["modules"]["publication"]["publication_year"] = $tf_publication_year;

  if (isset ($sel_log_not)) $list["criteria"]["logical"]["NOT"] = $sel_log_not;
  if (isset ($sel_log_and)) $list["criteria"]["logical"]["AND"] = $sel_log_and;

  if (isset ($se_criteria)){$list["criteria"] = unserialize( urldecode($se_criteria)); }
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

  if (debug_level_isset($cdg_param)) {
    echo "action=$action";
    if ( $list ) {
      while ( list( $key, $val ) = each( $list ) ) {
        echo "<br />list[$key]=$val";
      }
    }
  }

  return $list;
}

//////////////////////////////////////////////////////////////////////////////
// LIST actions
//////////////////////////////////////////////////////////////////////////////
function get_list_action() {
  global $list, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_list,$l_header_display,$l_header_export, $l_header_global_export;
  global $l_header_consult, $l_header_add_contact;
  global $l_select_list, $l_add_contact,$l_list_wizard;
  global $list_read, $list_write, $list_admin_read, $list_admin_write;

// Index
  $actions["LIST"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/list/list_index.php?action=index",
    'Right'    => $list_read,
    'Condition'=> array ('all') 
                                    );

// Search
  $actions["LIST"]["search"] = array (
    'Url'      => "$path/list/list_index.php?action=search",
    'Right'    => $list_read,
    'Condition'=> array ('None') 
                                      );

// New
  $actions["LIST"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/list/list_index.php?action=new",
    'Right'    => $list_write,
    'Condition'=> array ('','search','index','detailconsult','admin','display') 
                                  );

// New
  $actions["LIST"]["new_criterion"] = array (
    'Url'      => "$path/list/list_index.php?action=new_criterion",
    'Right'    => $list_write,
    'Condition'=> array ('None') 
                                  );				  
// Detail Consult
  $actions["LIST"]["detailconsult"] = array (
     'Name'     => $l_header_consult,
     'Url'      => "$path/list/list_index.php?action=detailconsult&amp;param_list=".$list["id"]."",
    'Right'    => $list_read,
    'Condition'=> array ('detailupdate') 
                                      );

// Detail Update
  $actions["LIST"]["detailupdate"] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/list/list_index.php?action=detailupdate&amp;param_list=".$list["id"]."",
     'Right'    => $list_write,
     'Condition'=> array ('detailconsult','contact_add','contact_del', 'update') 
                                           );

// Insert
  $actions["LIST"]["insert"] = array (
    'Url'      => "$path/list/list_index.php?action=insert",
    'Right'    => $list_write,
    'Condition'=> array ('None') 
                                      );

// Update
  $actions["LIST"]["update"] = array (
    'Url'      => "$path/list/list_index.php?action=update",
    'Right'    => $list_write,
    'Condition'=> array ('None') 
                                      );

// Check Delete
  $actions["LIST"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/list/list_index.php?action=check_delete&amp;hd_list_id=".$list["id"]."",
    'Right'    => $list_write,
    'Condition'=> array ('detailconsult','contact_add','contact_del') 
                                           );

// Delete
  $actions["LIST"]["delete"] = array (
    'Url'      => "$path/list/list_index.php?action=delete",
    'Right'    => $list_write,
    'Condition'=> array ('None') 
                                      );

// Sel list contacts : Contacts selection
  $actions["LIST"]["sel_list_contact"] = array (
    'Name'     => $l_header_add_contact,
    'Url'      => "$path/contact/contact_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_contact)."&amp;ext_action=contact_add&amp;ext_url=".urlencode($path."/list/list_index.php")."&amp;ext_id=".$list["id"]."&amp;ext_target=$l_list",
    'Right'    => $list_write,
    'Popup'    => 1,
    'Target'   => $l_list,
    'Condition'=> array ('detailconsult','update','contact_add','contact_del') 
                                    	  );

// Contact ADD
  $actions["LIST"]["contact_add"] = array (
    'Url'      => "$path/list/list_index.php?action=contact_add",
    'Right'    => $list_write,
    'Condition'=> array ('None') 
                                          );
// Contact Del
  $actions["LIST"]["contact_del"] = array (
    'Url'      => "$path/list/list_index.php?action=contact_del",
    'Right'    => $list_write,
    'Condition'=> array ('None') 
                                          );

// Export ADD
  $actions["LIST"]["export_add"] = array (
    'Name'     => $l_header_export,
    'Url'      => "$path/list/list_index.php?action=export_add&amp;cb_list".$list["id"]."=".$list["id"]."",
    'Right'    => $list_write,
    'Condition'=> array ('detailconsult','contact_add','contact_del') 
                                     	 );

// Export
  $actions["LIST"]["export"] = array (
    'Name'     => $l_header_global_export,
    'Url'      => "$path/list/list_index.php?action=ext_get_ids&amp;popup=1&amp;title=".urlencode($l_select_list)."&amp;ext_action=export_add&amp;ext_target=$l_list&amp;ext_url=".urlencode("$path/list/list_index.php"),
    'Right'    => $list_write,
    'Popup'    => 1,
    'Target'   => $l_list,
    'Condition'=> array ('all') 
                                     	 );

// Display
  $actions["LIST"]["display"] = array (
   'Name'     => $l_header_display,
   'Url'      => "$path/list/list_index.php?action=display",
   'Right'    => $list_read,
   'Condition'=> array ('all') 
                                      );

// Display Préférence
  $actions["LIST"]["dispref_display"] = array (
   'Url'      => "$path/list/list_index.php?action=dispref_display",
   'Right'    => $list_write,
   'Condition'=> array ('None') 
                                               );

// Display level
  $actions["LIST"]["dispref_level"] = array (
   'Url'      => "$path/list/list_index.php?action=dispref_level",
   'Right'    => $list_write,
   'Condition'=> array ('None') 
                                            );

}

</script>
