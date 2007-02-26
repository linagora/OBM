<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : samba_index.php                                              //
//     - Desc : Aliamin Samba Administration Index File                      //
// 2004-09-15 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index        --         -- show the Mail forwarding detail
// - detailupdate -- $system -- show the Mail forwarding detail form
// - update       -- $system -- update the Mail forwarding options
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "samba";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_samba_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("samba_display.inc");
require("samba_query.inc");

get_samba_action();
$perm->check_permissions($module, $action);


if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_samba_consult();

} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_samba_form($params);

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form($params)) {
    $retour = run_query_samba_update($params);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok);
      $display["detail"] = dis_samba_consult();
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
      $display["detail"] = dis_samba_form($params);
    }
  } else {
    $display["msg"] .= display_err_msg($err["msg"]);
    $display["detail"] = dis_samba_form($params, $err["field"]);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_samba);
$display["end"] = display_end();
$display["header"] = display_menu($module);

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Samba parameters transmitted in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_samba_params() {

  // Get global params
  $params = get_global_params("samba");

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Samba Action 
///////////////////////////////////////////////////////////////////////////////
function get_samba_action() {
  global $params, $actions, $path;
  global $l_header_consult, $l_header_update;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["samba"]["index"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/samba/samba_index.php?action=index",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                    );

// Detail Update
  $actions["samba"]["detailupdate"] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/samba/samba_index.php?action=detailupdate",
     'Right'    => $cright_read_admin,
     'Condition'=> array ('index', 'update') 
                                     	   );

// Update
  $actions["samba"]["update"] = array (
    'Url'      => "$path/samba/samba_index.php?action=update",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('None') 
                                     );

}

</script>
