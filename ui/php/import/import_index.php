<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : import_index.php                                             //
//     - Desc : Import Index File                                            //
// 2004-01-16 - Aliacom - Pierre Baudracco                                   //
///////////////////////////////////////////////////////////////////////////////
// $Id$
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


///////////////////////////////////////////////////////////////////////////////
// Session,Auth,Perms Management                                             //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "ADMINS";
$menu = "IMPORT";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
require("$obminclude/global_pref.inc");

include("import_display.inc");
include("import_query.inc");


if ($action == "") $action = "index";
$uid = $auth->auth["uid"];
$import = get_param_import();
get_import_action();
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

require("import_js.inc");

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if (! $popup) {
  $display["header"] = generate_menu($menu, $section); // Menu
}


if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $dsrc_q = run_query_datasource();
  $display["search"] = html_import_search_form($import, $dsrc_q);
  if ($set_display == "yes") {
    $display["result"] = dis_import_search_list("", $popup);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }
}

else if ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $dsrc_q = run_query_datasource();
  $display["search"] = html_import_search_form($import, $dsrc_q);
  $display["result"] = dis_import_search_list($import, $popup);
}

else if ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {
    $dsrc_q = run_query_datasource();
    $display["detail"] = html_import_file_form($action, $import, "", $dsrc_q);
  } else {
    $display["msg"] .= display_err_msg($l_error_permission);
  }
}

else if ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $import_q = run_query_detail($import["id"]);
  $display["detail"] = html_import_consult($import_q);
}

else if ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_detail($import["id"]);
  $dsrc_q = run_query_datasource();
  $display["detail"] = html_import_file_form($action, $import, $obm_q, $dsrc_q);
}

else if ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form("", $import)) {

    // If the context (same import) was confirmed ok, we proceed
    if ($hd_confirm == $c_yes) {
      $retour = run_query_insert($import);
      if ($retour) {
        $display["msg"] .= display_ok_msg($l_insert_ok);
      } else {
        $display["msg"] .= display_err_msg($l_insert_error);
      }
      $dsrc_q = run_query_datasource();
      $display["search"] = html_import_search_form($import, $dsrc_q);

    // If it is the first try, we warn the user if some imports seem similar
    } else {
      $obm_q = check_import_context("", $import);
      if ($obm_q->num_rows() > 0) {
        $display["detail"] = dis_import_warn_insert($obm_q, $import);
      } else {
        $retour = run_query_insert($import);
        if ($retour) {
          $display["msg"] .= display_ok_msg($l_insert_ok);
        } else {
          $display["msg"] .= display_err_msg($l_insert_error);
        }
	$dsrc_q = run_query_datasource();
        $display["search"] = html_import_search_form($import, $dsrc_q);
      }
    }

  // Form data are not valid
  } else {
    $display["msg"] .= display_warn_msg($err_msg);
    $dsrc_q = run_query_datasource();
    $display["detail"] = html_import_file_form($action, $import, "", $dsrc_q);
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form($import["id"], $import)) {
    $retour = run_query_update($import);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok);
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
    }
    $import_q = run_query_detail($import["id"]);
    $display["detail"] = html_import_consult($import_q);
  } else {
    $display["msg"] .= display_warn_msg($err_msg);
    $import_q = run_query_detail($import["id"]);
    $dsrc_q = run_query_datasource();
    $display["detail"] = html_import_file_form($action, $import, $import_q, $dsrc_q);
  }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_warn_delete($import["id"]);

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {
    $retour = run_query_delete($import["id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_delete_ok);
    } else {
      $display["msg"] .= display_err_msg($l_delete_error);
    }
    $dsrc_q = run_query_datasource();
    $display["search"] = html_import_search_form($import, $dsrc_q);
  } else {
   $display["msg"] .= display_err_msg($l_error_permission);
  }

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


///////////////////////////////////////////////////////////////////////////////
// Stores in $list hash, List parameters transmited
// returns : $list hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_import() {
  global $tf_name, $sel_dsrc, $rd_sep;
  global $param_import, $hd_usercreate, $hd_timeupdate, $cdg_param;
  global $action, $new_order, $order_dir;
  global $HTTP_POST_VARS, $HTTP_GET_VARS, $ses_list;

  // List fields
  if (isset ($param_import)) $import["id"] = $param_import;
  if (isset ($tf_name)) $import["name"] = trim($tf_name);
  if (isset ($sel_dsrc)) $import["datasource"] = $sel_dsrc;
  if (isset ($rd_sep)) $import["sep"] = $rd_sep;

  if (isset ($hd_usercreate)) $list["usercreate"] = $hd_usercreate;
  if (isset ($hd_timeupdate)) $list["timeupdate"] = $hd_timeupdate;

  if (isset ($new_order)) $list["new_order"] = $new_order;
  if (isset ($order_dir)) $list["order_dir"] = $order_dir;

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
    if ( $import ) {
      while ( list( $key, $val ) = each( $import ) ) {
        echo "<br />import[$key]=$val";
      }
    }
  }

  return $import;
}


//////////////////////////////////////////////////////////////////////////////
// LIST actions
//////////////////////////////////////////////////////////////////////////////
function get_import_action() {
  global $import, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_import,$l_header_display,$l_header_test_file;
  global $l_header_consult, $l_header_add_contact;
  global $l_select_list, $l_add_contact;
  global $import_read, $import_write, $import_admin_read, $import_admin_write;

// Index
  $actions["IMPORT"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/import/import_index.php?action=index",
    'Right'    => $import_read,
    'Condition'=> array ('all') 
                                    );

// Search
  $actions["IMPORT"]["search"] = array (
    'Url'      => "$path/import/import_index.php?action=search",
    'Right'    => $import_read,
    'Condition'=> array ('None') 
                                      );

// New
  $actions["IMPORT"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/import/import_index.php?action=new",
    'Right'    => $import_write,
    'Condition'=> array ('','search','index','detailconsult','admin','display') 
                                  );
// Detail Consult
  $actions["IMPORT"]["detailconsult"] = array (
     'Name'     => $l_header_consult,
     'Url'      => "$path/import/import_index.php?action=detailconsult&amp;param_import=".$import["id"]."",
    'Right'    => $import_read,
    'Condition'=> array ('detailupdate') 
                                      );

// Detail Update
  $actions["IMPORT"]["detailupdate"] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/import/import_index.php?action=detailupdate&amp;param_import=".$import["id"]."",
     'Right'    => $import_write,
     'Condition'=> array ('detailconsult', 'update') 
                                           );

// Insert
  $actions["IMPORT"]["insert"] = array (
    'Url'      => "$path/import/import_index.php?action=insert",
    'Right'    => $import_write,
    'Condition'=> array ('None') 
                                      );

// Update
  $actions["IMPORT"]["update"] = array (
    'Url'      => "$path/import/import_index.php?action=update",
    'Right'    => $import_write,
    'Condition'=> array ('None') 
                                      );

// Check Delete
  $actions["IMPORT"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/import/import_index.php?action=check_delete&amp;param_import=".$import["id"]."",
    'Right'    => $import_write,
    'Condition'=> array ('detailconsult', 'update') 
                                           );

// Delete
  $actions["IMPORT"]["delete"] = array (
    'Url'      => "$path/import/import_index.php?action=delete",
    'Right'    => $import_write,
    'Condition'=> array ('None') 
                                      );

// Test File
  $actions["IMPORT"]["test_file"] = array (
    'Name'     => $l_header_test_file,
    'Url'      => "$path/import/import_index.php?action=test_file&amp;param_import=".$import["id"]."",
    'Right'    => $import_write,
    'Condition'=> array ('detailconsult') 
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
