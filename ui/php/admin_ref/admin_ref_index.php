<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_ref_index.php                                          //
//     - Desc : Referential data index file                                  //
// 2003-12-05 - Pierre Baudracco                                             //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions
// - index
// - datasource           --                -- Data Source index
// - datasource_insert    -- form fields    -- insert the Data Source
// - datasource_update    -- form fields    -- update the Data Source
// - datasource_checklink --                -- check if Data Source is used
// - datasource_delete    -- $sel_kind      -- delete the Data Source
// - country              --                -- Country index
// - country_insert       -- form fields    -- insert the Country
// - country_update       -- form fields    -- update the Country
// - country_checklink    --                -- check if Country is used
// - country_delete       --                -- delete the Country
// - tasktype             --                -- TaskType index
// - tasktype_insert      -- form fields    -- insert the Tasktype
// - tasktype_update      -- form fields    -- update the Tasktype
// - tasktype_checklink   --                -- check if Tasktype is used
// - tasktype_delete      --                -- delete the Tasktype
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "admin_ref";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc"); 
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("admin_ref_display.inc");
require("admin_ref_query.inc");

if ( ($action == "") || ($action == "index")) $action = "country";
$ref = get_param_ref();
get_admin_ref_action();
$perm->check_permissions($module, $action);

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if ($action == "index")  {
///////////////////////////////////////////////////////////////////////////////
  require("admin_ref_js.inc");
  $display["detail"] = dis_ref_index();

} elseif ($action == "country")  {
///////////////////////////////////////////////////////////////////////////////
  require("admin_ref_js.inc");
  $display["detail"] = dis_country_index();

} elseif ($action == "country_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_country_insert($ref);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_country_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_country_insert_error);
  }
  require("admin_ref_js.inc");
  $display["detail"] .= dis_country_index();

} elseif ($action == "country_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_country_update($ref);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_country_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_country_update_error);
  }
  require("admin_ref_js.inc");
  $display["detail"] .= dis_country_index();

} elseif ($action == "country_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= dis_country_links($ref);
  require("admin_ref_js.inc");
  $display["detail"] .= dis_country_index();

} elseif ($action == "country_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_country_delete($ref);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_country_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_country_delete_error);
  }
  require("admin_ref_js.inc");
  $display["detail"] .= dis_country_index();

} elseif ($action == "datasource")  {
///////////////////////////////////////////////////////////////////////////////
  require("admin_ref_js.inc");
  $display["detail"] = dis_datasource_index();

} elseif ($action == "datasource_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_datasource_insert($ref);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_dsrc_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_dsrc_insert_error);
  }
  require("admin_ref_js.inc");
  $display["detail"] .= dis_datasource_index();

} elseif ($action == "datasource_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_datasource_update($ref);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_dsrc_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_dsrc_update_error);
  }
  require("admin_ref_js.inc");
  $display["detail"] .= dis_datasource_index();

} elseif ($action == "datasource_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  require("admin_ref_js.inc");
  $display["detail"] .= dis_datasource_links($ref);

} elseif ($action == "datasource_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_datasource_delete($ref);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_dsrc_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_dsrc_delete_error);
  }
  require("admin_ref_js.inc");
  $display["detail"] .= dis_datasource_index();

} elseif ($action == "tasktype")  {
///////////////////////////////////////////////////////////////////////////////
  require("admin_ref_js.inc");
  $display["detail"] = dis_tasktype_index();

} elseif ($action == "tasktype_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_tasktype_insert($ref);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_tt_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_tt_insert_error);
  }
  require("admin_ref_js.inc");
  $display["detail"] .= dis_tasktype_index();

} elseif ($action == "tasktype_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_tasktype_update($ref);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_tt_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_tt_update_error);
  }
  require("admin_ref_js.inc");
  $display["detail"] .= dis_tasktype_index();

} elseif ($action == "tasktype_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  require("admin_ref_js.inc");
  $display["detail"] .= dis_tasktype_links($ref);

} elseif ($action == "tasktype_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_tasktype_delete($ref);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_tt_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_tt_delete_error);
  }
  require("admin_ref_js.inc");
  $display["detail"] .= dis_tasktype_index();
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_header_admin_ref);
$display["header"] = display_menu($module);
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Admin Ref parameters transmited in $ref hash
// returns : $ref hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_ref() {
  global $tf_name, $sel_dsrc, $sel_ctry, $tf_iso, $tf_lang, $tf_phone;
  global $sel_tt, $tf_label, $rd_tt_internal, $hd_old_iso, $hd_old_lang;
  global $cdg_param;
  global $HTTP_POST_VARS,$HTTP_GET_VARS;

  // Admin - generic fields
  if (isset ($tf_name)) $ref["name"] = $tf_name;

  // Admin - Data Source fields
  if (isset ($sel_dsrc)) $ref["datasource"] = $sel_dsrc;

  // Admin - Country fields
  if (isset ($sel_ctry)) {
    $pos = strpos($sel_ctry, "-");
    $ref["iso"] = substr($sel_ctry, 0, $pos);
    $ref["lang"] = substr($sel_ctry, $pos+1);
  }
  if (isset ($tf_iso)) $ref["iso"] = $tf_iso;
  if (isset ($tf_lang)) $ref["lang"] = $tf_lang;
  if (isset ($tf_phone)) $ref["phone"] = $tf_phone;
  if (isset ($hd_old_iso)) $ref["old_iso"] = $hd_old_iso;
  if (isset ($hd_old_lang)) $ref["old_lang"] = $hd_old_lang;

  // Admin - Task Type fields
  if (isset ($sel_tt)) $ref["tasktype"] = $sel_tt;
  if (isset ($tf_label)) $ref["label"] = $tf_label;
  if (isset ($rd_tt_internal)) $ref["internal"] = $rd_tt_internal;

  if (debug_level_isset($cdg_param)) {
    if ( $ref ) {
      while ( list( $key, $val ) = each( $ref ) ) {
        echo "<br />ref[$key]=$val";
      }
    }
  }

  return $ref;
}


//////////////////////////////////////////////////////////////////////////////
// ADMIN REF actions
//////////////////////////////////////////////////////////////////////////////
function get_admin_ref_action() {
  global $actions, $path;
  global $l_header_datasource, $l_header_country, $l_header_tasktype;
  global $cright_read_admin, $cright_write_admin;


  // Country index
  $actions["admin_ref"]["country"] = array (
     'Name'     => $l_header_country,
     'Url'      => "$path/admin_ref/admin_ref_index.php?action=country&amp;mode=html",
     'Right'    => $cright_read_admin,
     'Condition'=> array ('all')
                                    	  );

// Country Insert
  $actions["admin_ref"]["country_insert"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=country_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Country Update
  $actions["admin_ref"]["country_update"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=country_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Country Check Link
  $actions["admin_ref"]["country_checklink"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=country_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Country Delete
  $actions["admin_ref"]["country_delete"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=country_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

  // DataSource index
  $actions["admin_ref"]["datasource"] = array (
     'Name'     => $l_header_datasource,
     'Url'      => "$path/admin_ref/admin_ref_index.php?action=datasource&amp;mode=html",
     'Right'    => $cright_read_admin,
     'Condition'=> array ('all')
                                    	  );

// DataSource Insert
  $actions["admin_ref"]["datasource_insert"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=datasource_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// DataSource Update
  $actions["admin_ref"]["datasource_update"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=datasource_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// DataSource Check Link
  $actions["admin_ref"]["datasource_checklink"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=datasource_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// DataSource Delete
  $actions["admin_ref"]["datasource_delete"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=datasource_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

  // Tasktype index
  $actions["admin_ref"]["tasktype"] = array (
     'Name'     => $l_header_tasktype,
     'Url'      => "$path/admin_ref/admin_ref_index.php?action=tasktype&amp;mode=html",
     'Right'    => $cright_read_admin,
     'Condition'=> array ('all')
                                    	  );

// Tasktype Insert
  $actions["admin_ref"]["tasktype_insert"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=tasktype_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Tasktype Update
  $actions["admin_ref"]["tasktype_update"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=tasktype_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Tasktype Check Link
  $actions["admin_ref"]["tasktype_checklink"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=tasktype_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Tasktype Delete
  $actions["admin_ref"]["tasktype_delete"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=tasktype_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

}

</script>
