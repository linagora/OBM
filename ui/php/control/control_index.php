<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : control_index.php                                            //
//     - Desc : Control panel Index File                                     //
// 2004-07-28 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index        --         -- show the system control board
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "control";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_control_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("control_display.inc");
require("control_query.inc");

get_control_action();
$perm->check_permissions($module, $action);

if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_control_consult();

} elseif ($action == "update_htpasswd") {
///////////////////////////////////////////////////////////////////////////////
  if (check_data($params)) {
    $display["msg"] .= display_debug_msg($cmd_disable_remote, $cdg_exe);
    $return = run_update_htpasswd($params);
    if ($return === true) {
      $display["msg"] .= display_ok_msg($l_upd_passwd_ok);
    } else {
      $display["msg"] .= display_err_msg($return);
    }
  } else {
    $display["msg"] .= display_err_msg("$l_err_invalid_passwd");
  }
  $display["detail"] = html_control_consult();
} elseif ($action == "download_log") {
  dis_download_log($params);
}

///////////////////////////////////////////////////////////////////////////////
// Display page
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_control);
$display["end"] = display_end();
$display["header"] = display_menu($module);

display_page($display);

///////////////////////////////////////////////////////////////////////////////
// Stores control parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_control_params() {

  // Get global params
  $params = get_global_params("control");
  
  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Control Action 
///////////////////////////////////////////////////////////////////////////////
function get_control_action() {
  global $params, $actions, $path;
  global $l_header_consult;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["control"]["index"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/control/control_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );
  $actions["control"]["viewproxystats"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/control/control_index.php?action=viewproxystats",
    'Right'    => $cright_read,
    'Condition'=> array ('none') 
                                    );
  $actions["control"]["update_htpasswd"] = array (
    'Url'      => "$path/control/control_index.php?action=update_htpasswd",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('none') 
                                    );
  $actions["control"]["download_log"] = array (
    'Url'      => "$path/control/control_index.php?action=download_log",
    'Right'    => $cright_read_admin,
    'Popup'    => 1,
    'Condition'=> array ('none') 
                                    );
				    

}
</script>
