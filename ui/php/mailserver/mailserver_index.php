<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailserver_index.php                                         //
//     - Desc : Calendar Index File                                          //
// 2007-02-08 - AliaSource - Mehdi Rande                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- this week for this user.
// - new
// - insert
// - detailconsult
// - detailupdate
// - update
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'mailserver';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_mailserver_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");

require('mailserver_query.inc');
require('mailserver_display.inc');
require_once("$obminclude/of/of_category.inc");

get_mailserver_action();
$perm->check_permissions($module, $action);
page_close();


///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == 'ext_get_id') {
  $display['search'] = html_mailserver_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_mailserver_search_list($params);
  } else {
    $display['msg'] = display_info_msg($l_no_display);
  }
  
} else if (($action == 'index') || ($action == '')) {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = html_mailserver_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_mailserver_search_list('');
  } else {
    $display['msg'] .= display_ok_msg($l_no_display);
  }

} else if ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = html_mailserver_search_form($params);
  $display['result'] = dis_mailserver_search_list($params);

} else if ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_mailserver_form($action, $params);

} else if ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_mailserver_consult($params);

} else if ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'].= dis_mailserver_form($action, $params);

} else if ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_mailserver_data_form($params)) {
    $retour = run_query_mailserver_insert($params);
    if ($retour) {
      $params['mailserver_id'] = $retour;
      $display['msg'] .= display_ok_msg($l_insert_ok);
      $display['detail'] = dis_mailserver_consult($params);
    } else {
      $display['msg'] .= display_err_msg($l_insert_error);
    }
    // Form data are not valid
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['detail'].= dis_mailserver_form($action, $params, $err['field']);
  }

} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_mailserver_data_form($params)) {
    $retour = run_query_mailserver_update($params);
    if ($retour) {
      $display['msg'] .= display_ok_msg($l_update_ok);
    } else {
      $display['msg'] .= display_err_msg($l_update_error);
    }
    $display['detail'] = dis_mailserver_consult($params);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['detail'].= dis_mailserver_form($action, $params, $err['field']);
  }

} elseif ($action == 'check_delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_mailserver($params['mailserver_id'])) {
    $display['msg'] .= display_info_msg($ok_msg, false);
    $display['detail'] = dis_can_delete_mailserver($params['mailserver_id']);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_mailserver_consult($params);
  }

} elseif ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_mailserver($params['mailserver_id'])) {
    $retour = run_query_mailserver_delete($params['mailserver_id']);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_mailserver : $l_delete_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_mailserver : $l_delete_error");
    }
    $display['search'] = html_mailserver_search_form($params);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_mailserver_consult($params);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_mailserver);
$display['end'] = display_end();
update_mailserver_action();
$display['header'] = display_menu($module);

display_page($display);

///////////////////////////////////////////////////////////////////////////////
// Stores in $params hash, Mailserver parameters transmited
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_mailserver_params() {

  // Get global params
  $params = get_global_params('mailserver');

  return $params;
}

///////////////////////////////////////////////////////////////////////////////
// MailServer Action 
///////////////////////////////////////////////////////////////////////////////
function get_mailserver_action() {
  global $params, $actions, $path, $l_mailserver;
  global $l_header_find, $l_header_update,$l_header_consult;
  global $l_header_index, $l_header_delete, $l_header_new;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

  $id = $params['mailserver_id'];

  $actions['mailserver']['index'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/mailserver/mailserver_index.php?action=index",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
  );

// Search
  $actions['mailserver']['search'] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                  );

  $actions['mailserver']['new'] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/mailserver/mailserver_index.php?action=new",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('all') 
  );

  $actions['mailserver']['insert'] = array (
    'Right'    => $cright_read_admin,
    'Condition'=> array ('none') 
  );

  $actions['mailserver']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/mailserver/mailserver_index.php?action=detailconsult&amp;mailserver_id=$id",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('detailupdate','update') 
  );

  $actions['mailserver']['detailupdate'] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/mailserver/mailserver_index.php?action=detailupdate&amp;mailserver_id=$id",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('detailconsult','insert','update') 
    //'Condition'=> array ('none') 
  );

  $actions['mailserver']['update'] = array (
    'Right'    => $cright_read_admin,
    'Condition'=> array ('none') 
  ); 

  $actions['mailserver']['check_delete'] = array (
    'Name'     => $l_header_delete,    
    'Url'      => "$path/mailserver/mailserver_index.php?action=check_delete&amp;mailserver_id=$id",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult','insert','update') 
  );

  $actions['mailserver']['delete'] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('none') 
  );
}


///////////////////////////////////////////////////////////////////////////////
// Contact Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_mailserver_action() {
  global $params, $actions, $path;

  $id = $params['mailserver_id'];
  if ($id > 0) {
    // Detail Consult
    $actions['mailserver']['detailconsult']['Url'] = "$path/mailserver/mailserver_index.php?action=detailconsult&amp;mailserver_id=$id";
    
    // Detail Update
    $actions['mailserver']['detailupdate']['Url'] = "$path/mailserver/mailserver_index.php?action=detailupdate&amp;mailserver_id=$id";
    $actions['mailserver']['detailupdate']['Condition'][] = 'insert';
    
    // Check Delete
    $actions['mailserver']['check_delete']['Url'] = "$path/mailserver/mailserver_index.php?action=check_delete&amp;mailserver_id=$id";
    $actions['mailserver']['check_delete']['Condition'][] = 'insert';
  }
}
