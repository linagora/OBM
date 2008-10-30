<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailforward_index.php                                        //
//     - Desc : Mail Forwarding Index File                                   //
// 2002-09-01 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index        --         -- show the Mail forwarding detail
// - detailupdate -- $system -- show the Mail forwarding detail form
// - update       -- $system -- update the Mail forwarding options
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'mailforward';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_forward_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
require('mailforward_display.inc');
require('mailforward_query.inc');
require('mailforward_js.inc');

if ($action == '') $action = 'index';
get_forward_action();
$perm->check_permissions($module, $action);


if (($action == 'index') || ($action == '')) {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_mailforward_detail($obm['uid']);
  if ($obm_q->num_rows() == 1) {
    $display['detailInfo'] = display_record_info($obm_q); 
    $display['detail'] = html_forward_consult($obm_q);
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }

} elseif ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_mailforward_detail($obm['uid']);
  if ($obm_q->num_rows() == 1) {
    $display['detail'] = html_forward_form($obm_q, $params);
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }

} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_mailforward_detail($obm['uid']);
  if (check_user_defined_rules() && check_mailforward_data_form($obm_q, $params)) {
    $retour = run_query_mailforward_update($params, $obm['uid']);
    if ($retour) {
      exec_change_alias($obm_q, $params);
      $display['msg'] .= display_ok_msg("$l_mailforward : $l_update_ok");
      $obm_q = run_query_mailforward_detail($obm['uid']);
      $display['detail'] = html_forward_consult($obm_q);
    } else {
      $display['msg'] .= display_err_msg("$l_mailforward : $l_update_error");
      $display['detail'] = html_forward_form($obm_q, $params);
    }
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['detail'] = html_forward_form($obm_q, $params, $err['field']);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display page
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_mailforward);
if (! $params['popup']) {
  $display['header'] = display_menu($module);
}
$display['end'] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Mail forwarding parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_forward_params() {

  // Get global params
  $params = get_global_params('mailforward');

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Forward Action 
///////////////////////////////////////////////////////////////////////////////
function get_forward_action() {
  global $params, $actions, $path;
  global $l_header_consult, $l_header_update;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions['mailforward']['index'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/mailforward/mailforward_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

// Detail Update
  $actions['mailforward']['detailupdate'] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/mailforward/mailforward_index.php?action=detailupdate",
     'Right'    => $cright_read,
     'Condition'=> array ('index', 'detailconsult', 'update') 
                                     	   );

// Update
  $actions['mailforward']['update'] = array (
    'Url'      => "$path/mailforward/mailforward_index.php?action=update",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     );

}


</script>
