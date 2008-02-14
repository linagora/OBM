<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : vacation_index.php
//     - Desc : vacation Index File
// 2007-01-22 Pierre Baudracco
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "vacation";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_vacation_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("vacation_display.inc");
require("vacation_query.inc");
require("vacation_js.inc");

if ($action == "") $action = "index";
get_vacation_action();
$perm->check_permissions($module, $action);


if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_vacation_detail($obm["uid"]);
  if ($obm_q->num_rows() == 1) {
    $display["detailInfo"] = display_record_info($obm_q);
    $display["detail"] = html_vacation_consult($obm_q) ;
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }
  
} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_vacation_detail($obm["uid"]);
  if ($obm_q->num_rows() == 1) {
    $display["detailInfo"] = display_record_info($obm_q);
    $display["detail"] = html_vacation_form($obm_q, $params);
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_vacation_data_form($params)) {
    $obm_q = run_query_vacation_detail($obm["uid"]);
    if (update_vacation($params, $obm_q)) {
      $display["msg"] .= display_ok_msg("$l_vacation : $l_update_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_vacation : $l_update_error : " . $err["msg"]);
    }
  
    $obm_q = run_query_vacation_detail($obm["uid"]);
    $display["detailInfo"] = display_record_info($obm_q);
    $display["detail"] = html_vacation_consult($obm_q) ;
    
  } else {
    $display["msg"] = display_err_msg($err["field"] . " : " . $err["msg"]);
    $display["detailInfo"] = display_record_info($obm_q);
    $display["detail"] .= html_vacation_form(null, $params, $err);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display page
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_vacation);
if (! $params["popup"]) {
  $display["header"] = display_menu($module);
}
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Vacation parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_vacation_params() {

  // Get global params
  $params = get_global_params("vacation");


  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Forward Action 
///////////////////////////////////////////////////////////////////////////////
function get_vacation_action() {
  global $actions, $path;
  global $l_header_consult, $l_header_update;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["vacation"]["index"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/vacation/vacation_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

// Detail Update
  $actions["vacation"]["detailupdate"] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/vacation/vacation_index.php?action=detailupdate",
     'Right'    => $cright_read,
     'Condition'=> array ('index', 'detailconsult', 'update') 
                                     	   );

// Update
  $actions["vacation"]["update"] = array (
    'Url'      => "$path/vacation/vacation_index.php?action=update",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     );
}


</script>
