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
// - country              --                -- Data Source index
// - country_insert       -- form fields    -- insert the Country
// - country_update       -- form fields    -- update the Country
// - country_checklink    --                -- check if Country is used
// - country_delete       --                -- delete the Country
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$section = "ADMINS";
$menu = "ADMIN_REF";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("$obminclude/phplib/obmlib.inc");
include("$obminclude/global.inc"); 
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("admin_ref_display.inc");
require("admin_ref_query.inc");

if ($action == "") $action = "index";
$ref = get_param_ref();
get_admin_ref_action();
$perm->check();

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
  $display["detail"] .= dis_admin_index();

} elseif ($action == "country_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_country_update($ref);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_country_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_country_update_error);
  }
  require("admin_ref_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "country_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= dis_country_links($ref);
  require("admin_ref_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "country_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_country_delete($ref);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_country_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_country_delete_error);
  }
  require("admin_ref_js.inc");
  $display["detail"] .= dis_admin_index();

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
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_header_admin_ref);
$display["header"] = generate_menu($menu, $section); // Menu
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Admin Ref parameters transmited in $ref hash
// returns : $ref hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_ref() {
  global $tf_name, $sel_dsrc;
  global $cdg_param;
  global $HTTP_POST_VARS,$HTTP_GET_VARS;

  if (isset ($tf_name)) $ref["name"] = $tf_name;

  // Admin - Data Source fields
  if (isset ($sel_dsrc)) $ref["datasource"] = $sel_dsrc;


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
  global $l_header_datasource, $l_header_country;
  global $admin_ref_read, $admin_ref_write;

  // index
  $actions["ADMIN_REF"]["datasource"] = array (
     'Name'     => $l_header_datasource,
     'Url'      => "$path/admin_ref/admin_ref_index.php?action=datasource&amp;mode=html",
     'Right'    => $admin_ref_read,
     'Condition'=> array ('all')
                                    	  );

// DataSource Insert
  $actions["ADMIN_REF"]["datasource_insert"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=datasource_insert",
    'Right'    => $admin_ref_write,
    'Condition'=> array ('None') 
                                     	     );

// DataSource Update
  $actions["ADMIN_REF"]["datasource_update"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=datasource_update",
    'Right'    => $admin_ref_write,
    'Condition'=> array ('None') 
                                     	      );

// DataSource Check Link
  $actions["ADMIN_REF"]["datasource_checklink"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=datasource_checklink",
    'Right'    => $admin_ref_write,
    'Condition'=> array ('None') 
                                     		);

// DataSource Delete
  $actions["ADMIN_REF"]["datasource_delete"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=datasource_delete",
    'Right'    => $admin_ref_write,
    'Condition'=> array ('None') 
                                     	       );

  // index
  $actions["ADMIN_REF"]["country"] = array (
     'Name'     => $l_header_country,
     'Url'      => "$path/admin_ref/admin_ref_index.php?action=country&amp;mode=html",
     'Right'    => $admin_ref_read,
     'Condition'=> array ('all')
                                    	  );

// Country Insert
  $actions["ADMIN_REF"]["country_insert"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=country_insert",
    'Right'    => $admin_ref_write,
    'Condition'=> array ('None') 
                                     	     );

// Country Update
  $actions["ADMIN_REF"]["country_update"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=country_update",
    'Right'    => $admin_ref_write,
    'Condition'=> array ('None') 
                                     	      );

// Country Check Link
  $actions["ADMIN_REF"]["country_checklink"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=country_checklink",
    'Right'    => $admin_ref_write,
    'Condition'=> array ('None') 
                                     		);

// Country Delete
  $actions["ADMIN_REF"]["country_delete"] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=country_delete",
    'Right'    => $admin_ref_write,
    'Condition'=> array ('None') 
                                     	       );

}

</script>
