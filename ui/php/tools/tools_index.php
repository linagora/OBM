<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : tools_index.php                                              //
//     - Desc : OBM Tools Index File                                         //
// 2002-10-30 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - update_index  --         -- show the Update screen
// - update_update --         -- run the config update
// - update_cancel --         -- cancel the config update
// - update_detail --         -- display the updates
// - halt_index    --         -- show the shutdown tool
// - halt_halt     --         -- Halt the system
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "tools";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_tools_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("tools_display.inc");
require("tools_query.inc");

if ($action == "index") $action = "update_index";
get_tools_action();
$perm->check_permissions($module, $action);


if (($action == "update_index") || ($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_tools_update_index();

} elseif ($action == "update_detail_admin") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_tools_update_detail();

} elseif ($action == "update_update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_tools_update_context()) {
    $res = run_query_tools_update_update($params);
    if ($res == "0") {
      $display["msg"] .= display_ok_msg($l_upd_ok);
    } else {
      $display["msg"] .= display_err_msg("$l_upd_error ($res)");
    }
  } else {
    // Si le contexte ne permet pas une modification de configuration
    $display["detail"] = html_tools_update_index();
  }

} elseif ($action == "update_update2")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_tools_update_context()) {
    $res = run_query_tools_update_update($params);
    if ($res == "0") {
      $display["msg"] .= display_ok_msg($l_upd_ok);
    } else {
      $display["msg"] .= display_err_msg("$l_upd_error ($res)");
    }
  } else {
    // Si le contexte ne permet pas une modification de configuration
    $display["detail"] = html_tools_update_index();
  }

} elseif ($action == "update_cancel")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_tools_update_context()) {
    $res = run_query_tools_update_cancel();
    if ($res == "0") {
      $display["msg"] .= display_ok_msg($l_upd_cancel_ok);
    } else {
      $display["msg"] .= display_err_msg("$l_upd_cancel_error ($res)");
    }
    $display["detail"] = html_tools_update_index();
  } else {
    // Si le contexte ne permet pas une modification de configuration
    $display["detail"] = html_tools_update_index();
  }

} elseif ($action == "halt_index") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_tools_halt_index();

} elseif ($action == "halt_halt")  {
///////////////////////////////////////////////////////////////////////////////
  $display["msg"] .= display_debug_msg($cmd_halt, $cdg_exe);
  $ret = exec($cmd_halt);

} elseif ($action == "remote_index") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_tools_remote_index();

} elseif ($action == "remote_update")  {
///////////////////////////////////////////////////////////////////////////////
  $res = run_query_tools_remote_update($params);

  if ($res) {
    $remote_access = $params["remote_access"];
    if ($remote_access == "1") {
      $display["msg"] .= display_debug_msg($cmd_enable_remote, $cdg_exe);
      $ret = exec($cmd_enable_remote);
    } else {
      $display["msg"] .= display_debug_msg($cmd_disable_remote, $cdg_exe);
      $ret = exec($cmd_disable_remote);
    }
    $display["detail"] = html_tools_remote_index();
  } else {
    $display["msg"] .= display_err_msg("$l_update_error - $l_remote_access !");
    $display["detail"] = html_tools_remote_index();
  }

}

///////////////////////////////////////////////////////////////////////////////
// Display page
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_tools);
$display["header"] = display_menu($module);
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Tools parameters transmited in $tools hash
// returns : $tools hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_tools_params() {

  // Get global params
  $params = get_global_params("UserObm");

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Tools Action 
///////////////////////////////////////////////////////////////////////////////
function get_tools_action() {
  global $params, $actions, $path;
  global $l_header_tools_upd, $l_header_tools_halt,$l_header_tools_remote;
  global $l_header_tools_upd_admin, $l_header_tools_upd_domain;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;
  global $cgp_securinet;

// Tool Update
  $actions["tools"]["update_index"] = array (
    'Name'     => $l_header_tools_upd,
    'Url'      => "$path/tools/tools_index.php?action=update_index",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('all') 
                                    );

// Display Admin Update
  $actions["tools"]["update_detail_admin"] = array (
    'Name'     => $l_header_tools_upd_admin,
    'Right'    => $cright_read_admin,
    'Url'      => "$path/tools/tools_index.php?action=update_detail_admin",
    'Condition'=> array ('all') 
                                    );

// Display Domain Update
  $actions["tools"]["update_detail_domain"] = array (
    'Name'     => $l_header_tools_upd_domain,
    'Right'    => $cright_read_admin,
    'Url'      => "$path/tools/tools_index.php?action=update_detail_domain",
    'Condition'=> array ('all') 
                                    );

// Cancel Update
  $actions["tools"]["update_cancel"] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('none') 
                                    );

// Confirm Update
  $actions["tools"]["update_update"] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('none') 
                                    );

// Tool Remote
  if( $cgp_securinet ) {
    $actions["tools"]["remote_index"] = array (
      'Name'     => $l_header_tools_remote,
      'Url'      => "$path/tools/tools_index.php?action=remote_index",
      'Right'    => $cright_write_admin,
      'Condition'=> array ('all') 
                                    );
  }

// Tool Remote
  $actions["tools"]["remote_update"] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('none') 
                                    );
// Tool Halt
  $actions["tools"]["halt_index"] = array (
    'Name'     => $l_header_tools_halt,
    'Url'      => "$path/tools/tools_index.php?action=halt_index",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('all') 
                                    );

// Tool Halt
  $actions["tools"]["halt_halt"] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('none') 
                                    );


}

</script>
