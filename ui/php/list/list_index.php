<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : list_index.php                                               //
//     - Desc : List Index File                                              //
// 1999-03-19 Vincent MARGUERIT                                              //
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
$www ="   <p class=\"messageInfo\">
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
require("$obminclude/phplib/obmlib.inc");
require("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
require("$obminclude/global_pref.inc");

include("list_display.inc");
include("list_query.inc");

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
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
display_head($l_list);  // Head & Body
if (! $popup) {
  generate_menu($menu,$section);   // Menu
  display_bookmarks();    // links to the last visited companie, contact, deal
}

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  html_list_search_form($list);
  if ($set_display == "yes") {
    dis_list_search_list("", $popup);
  } else {
    display_ok_msg($l_no_display);
  }
}

else if ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  html_list_search_form($list);
  dis_list_search_list($list, $popup);
}

else if ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {
    html_list_form($action, "", $list);
  } else {
    echo $l_error_authentification;
  }
}

else if ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $list_q = run_query_detail($param_list);
  $pref_con_q = run_query_display_pref($uid, "list_contact");
  $con_q = run_query_contacts_list($param_list, $new_order, $order_dir);
  html_list_consult($list_q, $pref_con_q, $con_q);
}

else if ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_detail($param_list);
  html_list_form($action, $obm_q, $list);
}

else if ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form("", $list)) {

    // If the context (same list) was confirmed ok, we proceed
    if ($hd_confirm == $c_yes) {
      $retour = run_query_insert($list);
      if ($retour) {
        display_ok_msg($l_insert_ok);
      } else {
        display_err_msg($l_insert_error);
      }
      html_list_search_form($list);

    // If it is the first try, we warn the user if some lists seem similar
    } else {
      $obm_q = check_list_context("", $list);
      if ($obm_q->num_rows() > 0) {
        dis_list_warn_insert("", $obm_q, $list);
      } else {
        $retour = run_query_insert($list);
        if ($retour) {
          display_ok_msg($l_insert_ok);
        } else {
          display_err_msg($l_insert_error);
        }
        html_list_search_form($list);
      }
    }

  // Form data are not valid
  } else {
    display_warn_msg($err_msg);
    html_list_form($action, "", $list);
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form($param_list, $list)) {
    $retour = run_query_update($list);
    if ($retour) {
      display_ok_msg($l_update_ok);
    } else {
      display_err_msg($l_update_error);
    }
    $list_q = run_query_detail($param_list);
    $pref_con_q = run_query_display_pref($uid, "list_contact");
    $con_q = run_query_contacts_list($param_list);
    html_list_consult($list_q, $pref_con_q, $con_q);
  } else {
    display_warn_msg($err_msg);
    $list_q = run_query_detail($param_list);
    html_list_form($action, $list_q, $list);
  }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  dis_warn_delete($hd_list_id);

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {
    $retour = run_query_delete($hd_list_id);
    if ($retour) {
      display_ok_msg($l_delete_ok);
    } else {
      display_err_msg($l_delete_error);
    }
    html_list_search_form("");
  } else {
   display_err_msg($l_error_authentification);
  }

} elseif ($action == "contact_add")  {
///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {
    if ($list["con_nb"] > 0) {
      $nb = run_query_contactlist_insert($list);
      display_ok_msg("$nb $l_contact_added");
    } else {
      display_err_msg("no contact to add");
    }
    $list_q = run_query_detail($param_list);
    $pref_con_q = run_query_display_pref($uid, "list_contact");
    $con_q = run_query_contacts_list($param_list);
    html_list_consult($list_q, $pref_con_q, $con_q);
  } else {
    display_err_msg($l_error_authentification);
  }

} elseif ($action == "contact_del")  {
///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {
    if ($list["con_nb"] > 0) {
      $nb = run_query_contactlist_delete($list);
      display_ok_msg("$nb $l_contact_removed");
    } else {
      display_err_msg("no contact to delete");
    }
    $list_q = run_query_detail($param_list);
    $pref_con_q = run_query_display_pref($uid, "list_contact");
    $con_q = run_query_contacts_list($param_list);
    html_list_consult($list_q, $pref_con_q, $con_q);
  } else {
    display_err_msg($l_error_authentification);
  }

} else if ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $pref_q = run_query_display_pref($uid, "list", 1);
  $pref_con_q = run_query_display_pref($uid, "list_contact", 1);
  dis_list_display_pref($pref_q, $pref_con_q);
}

else if($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update($entity, $fieldname, $display);
  $pref_q = run_query_display_pref($uid, "list", 1);
  $pref_con_q = run_query_display_pref($uid, "list_contact", 1);
  dis_list_display_pref($pref_q, $pref_con_q);
}

else if($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update($entity, $new_level, $fieldorder);
  $pref_q = run_query_display_pref($uid, "list", 1);
  $pref_con_q = run_query_display_pref($uid, "list_contact", 1);
  dis_list_display_pref($pref_q, $pref_con_q);
}

else if($action == "export_add") {
///////////////////////////////////////////////////////////////////////////////
  dis_export_form($list);
}

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_ids") {
  html_list_search_form($list);
  if ($set_display == "yes") {
    dis_list_search_list($list, $popup);
  } else {
    display_ok_msg($l_no_display);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display end of page                                                       //
///////////////////////////////////////////////////////////////////////////////
display_end();


///////////////////////////////////////////////////////////////////////////////
// Stores in $list hash, List parameters transmited
// returns : $list hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_list() {
  global $tf_name, $tf_subject, $tf_contact, $tf_datebegin, $tf_email, $cb_vis;
  global $param_list, $hd_usercreate, $hd_timeupdate, $cdg_param;
  global $action, $ext_action, $ext_url, $ext_id, $title;
  global $HTTP_POST_VARS, $HTTP_GET_VARS, $ses_list;

  // List fields
  if (isset ($param_list)) $list["id"] = $param_list;
  if (isset ($tf_name)) $list["name"] = trim($tf_name);
  if (isset ($tf_subject)) $list["subject"] = trim($tf_subject);
  if (isset ($tf_email)) $list["email"] = $tf_email;
  if (isset ($cb_vis)) $list["vis"] = $cb_vis;
  if (isset ($tf_contact)) $list["contact"] = trim($tf_contact);

  if (isset ($tf_datebegin)) $list["datebegin"] = $tf_datebegin;
  if (isset ($param_parent)) $list["parent"] = $param_parent;
  if (isset ($sel_kind)) $list["kind"] = $sel_kind;
  if (isset ($sel_cat)) $list["cat"] = $sel_cat;
  if (isset ($param_company)) $list["company"] = $param_company;
  if (isset ($sel_contact1)) $list["contact1"] = $sel_contact1;
  if (isset ($sel_contact2)) $list["contact2"] = $sel_contact2;
  if (isset ($sel_market)) $list["market"] = $sel_market;
  if (isset ($sel_tech)) $list["tech"] = $sel_tech;
  if (isset ($tf_dateprop)) $list["dateprop"] = $tf_dateprop;
  if (isset ($tf_amount)) $list["amount"] = $tf_amount;
  if (isset ($sel_state)) $list["state"] = $sel_state;
  if (isset ($tf_datealarm)) $list["datealarm"] = $tf_datealarm;
  if (isset ($ta_com)) $list["com"] = $ta_com;
  if (isset ($cb_archive)) $list["archive"] = $cb_archive;

  if (isset ($hd_usercreate)) $list["usercreate"] = $hd_usercreate;
  if (isset ($hd_timeupdate)) $list["timeupdate"] = $hd_timeupdate;

  // External param
  if (isset ($ext_action)) $list["ext_action"] = $ext_action;
  if (isset ($ext_url)) $list["ext_url"] = $ext_url;
  if (isset ($ext_id)) $list["ext_id"] = $ext_id;
  if (isset ($title)) $list["title"] = stripslashes($title);

  if ((is_array ($HTTP_POST_VARS)) && (count($HTTP_POST_VARS) > 0)) {
    $http_obm_vars = $HTTP_POST_VARS;
  } elseif ((is_array ($HTTP_GET_VARS)) && (count($HTTP_GET_VARS) > 0)) {
    $http_obm_vars = $HTTP_GET_VARS;
  }

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
        echo "<BR>list[$key]=$val";
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
  global $l_header_find,$l_header_new,$l_header_modify,$l_header_delete;
  global $l_header_display,$l_header_export, $l_header_global_export;
  global $l_header_admin, $l_header_add_contact, $l_select_list;
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
// Detail Consult
  $actions["LIST"]["detailconsult"] = array (
    'Url'      => "$path/list/list_index.php?action=detailconsult",
    'Right'    => $list_read,
    'Condition'=> array ('None') 
                                      );

// Detail Update
  $actions["LIST"]["detailupdate"] = array (
     'Name'     => $l_header_modify,
     'Url'      => "$path/list/list_index.php?action=detailupdate&amp;param_list=".$list["id"]."",
     'Right'    => $list_write,
     'Condition'=> array ('detailconsult','contact_add','contact_del') 
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

// Ext get Ids : Lists selection
  $actions["LIST"]["ext_get_ids"] = array (
    'Name'     => $l_header_add_contact,
    'Url'      => "$path/contact/contact_index.php?action=ext_get_ids&amp;popup=1&amp;title=".urlencode($l_add_contact)."&amp;ext_action=contact_add&amp;ext_url=".urlencode($path."/list/list_index.php")."&amp;ext_id=".$list["id"]."",
    'Right'    => $list_write,
    'Popup'    => 1,
    'Condition'=> array ('detailconsult','contact_add','contact_del') 
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
    'Url'      => "$path/list/list_index.php?action=ext_get_ids&amp;popup=1&amp;title=".urlencode($l_select_list)."&amp;ext_action=export_add&amp;ext_url=".urlencode("$path/list/list_index.php"),
    'Right'    => $list_write,
    'Popup'    => 1,
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

</SCRIPT>
