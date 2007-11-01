<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailbox_index.php                                            //
//     - Desc : Mailbox Index File                                           //
// 2007-03-28 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - rights_admin    --                  -- see mailbox rights
// - rights_update   --                  -- update mailbox rights
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "mailbox";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_mailbox_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("mailbox_display.inc");
require("mailbox_query.inc");
require("$obminclude/of/of_right.inc");

if (($action == "") || ($action == "index")) {
  $action = "rights_admin";
}

get_mailbox_action();
$perm->check_permissions($module, $action);

page_close();


if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_mailshare_search_form($params);
  if ($_SESSION['set_display'] == "yes") {
    $display["result"] = dis_mailshare_search_list("");
  } else {
    $display["msg"] .= display_ok_msg($l_no_display);
  }

} elseif ($action == "rights_admin") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_mailbox_right_dis_admin($params["entity_id"]);

} elseif ($action == "rights_update") {
///////////////////////////////////////////////////////////////////////////////
  if (of_right_update_right($params, "mailbox", "userobm")) {
    set_update_state();
    $display["msg"] .= display_ok_msg("$l_rights : $l_update_ok");
  } else {
    $display["msg"] .= display_warn_msg($err["msg"]);
  }
  $display["detail"] = dis_mailbox_right_dis_admin($params["entity_id"]);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_mailbox);
if (! $params["popup"]) {
  $display["header"] = display_menu($module);
}
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $params hash, parameters transmited
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_mailbox_params() {
  global $action, $cdg_param, $popup;
  global $cb_read_public, $cb_write_public,$sel_accept_write,$sel_accept_read;

  // Get global params
  $params = get_global_params("Mailbox");

  if ((isset ($params["entity_id"])) && (! isset($params["mailbox_id"]))) {
    $params["mailbox_id"] = $params["entity_id"];
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Mailbox Action 
///////////////////////////////////////////////////////////////////////////////
function get_mailbox_action() {
  global $params, $actions, $path;
  global $l_header_right;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Rights Admin.
  $actions["mailbox"]["rights_admin"] = array (
    'Name'     => $l_header_right,
    'Url'      => "$path/mailbox/mailbox_index.php?action=rights_admin&amp;entity_id=".$params["mailbox_id"],
    'Right'    => $cright_write,
    'Condition'=> array ('all')
                                     );

// Rights Update
  $actions["mailbox"]["rights_update"] = array (
    'Url'      => "$path/mailbox/mailbox_index.php?action=rights_update&amp;entity_id=".$params["mailbox_id"],
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     );
}


</script>
