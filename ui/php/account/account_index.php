<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



///////////////////////////////////////////////////////////////////////////////
// OBM - File : account_query.inc                                            //
//     - Desc : account query File                                           //
// 2001-07-30 Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'account';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
require('account_display.inc');
require('account_query.inc');
$params = get_account_params();

update_last_visit('account', $param_account, $action);

get_account_action();
$perm->check_permissions($module, $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// Programme principal                                                       //
///////////////////////////////////////////////////////////////////////////////

if ($action == 'index') {
///////////////////////////////////////////////////////////////////////////////
  require('account_js.inc');
  $display['search'] = html_account_search_form ($action, $params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_account_search_list($params);
  } else {
    $display['msg'] = display_ok_msg($l_no_display);
  }

} elseif ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  require('account_js.inc');
  $display['search'] = html_account_search_form($action, $params);
  $display['result'] = dis_account_search_list($params);
  
} elseif ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  if ($obm['profile'] != $perms_user) {
    require('account_js.inc');
    $display['detail'] = html_account_form($obm_q_accounts, $action);
  } else {
    $display['msg'] = display_err_msg($l_error_permission);
  }

} elseif ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  if(check_user_defined_rules()) {
    run_query_account_insert($params);
    $display['msg'] = display_ok_msg("$l_account : $l_insert_ok");
    require('account_js.inc');
    $display['search'] = html_account_search_form($action, $params);
  } else {
    $display['detail'] = html_account_form($obm_q_accounts, $action);
    $display['msg'] = display_err_msg($err);
  }

} elseif ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  require('account_js.inc');
  if ($params['account_id'] > 0) {
    $ac_q = run_query_account_detail($params['account_id']);
    $display['detailInfo'] = display_record_info($ac_q);
    $display['detail'] = html_account_consult($ac_q, $action);
  }

} elseif ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  if ($params['account_id'] > 0) {
    $ac_q = run_query_account_detail($params['account_id']);
    require('account_js.inc');
    $display['detailInfo'] = display_record_info($ac_q);
    $display['detail'] = html_account_form($ac_q, $action);
  }

} elseif ($action == 'update')  {
///////////////////////////////////////////////////////////////////////////////
  require('account_js.inc');
  run_query_account_update($params);

  if ($params['account_id'] > 0) {
    $ac_q = run_query_account_detail($params['account_id']);
    $display['detailInfo'] = display_record_info($ac_q);
    $display['detail'] = html_account_consult($ac_q, $action);
  }
//   $display['msg'] = display_ok_msg($l_update_ok);
//   require('account_js.inc');
//   $display['search'] = html_account_search_form($action, $params);
  
} elseif ($action == 'delete')  {
///////////////////////////////////////////////////////////////////////////////
  // checking that no payment is linked to this account
  $q_related_payments = run_query_account_search_payments ($params['account_id']);
  if ($q_related_payments->nf() != 0){
    $display['detail'] = html_account_impossible_deletion ($params['account_id'], $q_related_payments);
    // maybe a confirmation from the user would be enough...
  } else {
    run_query_account_delete($params['account_id']);
    $display['msg'] = display_ok_msg("$l_account  : $l_delete_ok");
    require('account_js.inc');
    $display['search'] = html_account_search_form($action,'');
  }

} elseif ($action == 'compute_balance') {
///////////////////////////////////////////////////////////////////////////////
  /*  if (true){
    display_ok_msg ('FIXME PERMISSIONS');
    require ('account_js.inc');
    $q_account = run_query_account_detail ($params['account_id']);
    // used to compute today balance :
    $q_payments = run_query_account_search_payments($params['account_id'], date ('Y-m-d'));
    // used to compute balance on $tf_balance_date :
    $q_expected_payments = run_query_account_search_expected_payments ($params['account_id'], $tf_balance_date);
    $payments_options = run_query_display_options ($obm['uid'], 'payment');
    $expected_payments_options = run_query_display_options ($obm['uid'], 'payment');
    html_account_compute_balance ($q_account, $q_payments, $q_expected_payments, $payments_options, $expected_payments_options, $tf_balance_date);
  } else{
    display_err_msg($l_error_permission);
  }
  */
  if (true) {
    // account_js.inc needed to check date input by user...
    require ('account_js.inc');
    $display['msg'] = display_ok_msg ('FIXME PERMISSIONS');
    //$q_account = run_query_account_detail ($params['account_id']);
    //    $payments_options = run_query_display_options ($obm['uid'],'payment');
    $payments_prefs = get_display_pref ($obm['uid'], 'payment');

    $display['detail'] = html_account_compute_balance ($params['account_id'], $payments_prefs, $params['balance_date']);

  } else {
    $display['msg'] = display_err_msg($l_error_permission);
  } 
} elseif ($action == 'admin')  {
///////////////////////////////////////////////////////////////////////////////
  if ($obm['profile'] != $perms_user) {  
    $display['msg'] = "<center>Nothing here for now</center><br />";
  } else {
    $display['msg'] = display_err_msg($l_error_permission);
  }	
    
} elseif ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'], 'account',1);
  $prefs_p = get_display_pref ($obm['uid'], 'payment',1);
  $display['detail'] = dis_account_display_pref ($prefs, $prefs_p); 

} else if($action =='dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'account',1);
  $prefs_p = get_display_pref ($obm['uid'], 'payment',1);
  $display['detail'] = dis_account_display_pref ($prefs, $prefs_p); 

} else if($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'account',1);
  $prefs_p = get_display_pref ($obm['uid'], 'payment',1);
  $display['detail'] = dis_account_display_pref ($prefs, $prefs_p); 
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
update_account_action();
$display['header'] = display_menu($module);
$display['head'] = display_head("$l_account");
$display['end'] = display_end();
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Account parameters transmitted in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_account_params() {
  
  // Get global params
  $params = get_global_params('Account');
  $params['balance_date'] = of_isodate_convert($params['balance_date'], true);
  
  return $params;
}



///////////////////////////////////////////////////////////////////////////////
// Account actions
///////////////////////////////////////////////////////////////////////////////
function get_account_action() {
  global $params, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_display,$l_header_admin,$l_header_compute_balance;
  global $l_header_consult;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions['account']['index'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/account/account_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                       );

// Search
  $actions['account']['search'] = array (
    'Url'      => "$path/account/account_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                       );

// New
  $actions['account']['new'] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/account/account_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('','search','index','detailconsult','display') 
     		                     );

// Insert
  $actions['account']['insert'] = array (
    'Url'      => "$path/account/account_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                       );

// Detail Consult
  $actions['account']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/account/account_index.php?action=detailconsult&amp;account_id=".$params['account_id'],
    'Right'    => $cright_read,
    'Condition'=> array ('detailconsult', 'compute_balance', 'detailupdate') 
                                       );

// Compute Balance
  $actions['account']['compute_balance'] = array (
    'Name'     => $l_header_compute_balance,
    'Url'      => "$path/account/account_index.php?action=compute_balance&amp;account_id=".$params['account_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
                                     		 );

// Detail Update
  $actions['account']['detailupdate'] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/account/account_index.php?action=detailupdate&amp;account_id=".$params['account_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'update')
                                     	      );

// Update
  $actions['account']['update'] = array (
    'Url'      => "$path/account/account_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                       );

// Delete
  $actions['account']['delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/account/account_index.php?action=delete&amp;account_id=".$params['account_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update') 
                                     	);

// Display
  $actions['account']['display'] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/account/account_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );

// Display Preferences
  $actions['account']['dispref_display'] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/account/account_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	 );

// Display Preferences
  $actions['account']['level_display'] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/account/account_index.php?action=level_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	 );

}


///////////////////////////////////////////////////////////////////////////////
// Account Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_account_action() {
  global $params, $actions, $path;

  $id = $params['account_id'];
  if ($id > 0) {
    // Detail Consult
    $actions['account']['detailconsult']['Url'] = "$path/account/account_index.php?action=detailconsult&amp;account_id=$id";
    $actions['account']['detailconsult']['Condition'][] = 'insert';

    // Detail Update
    $actions['account']['detailupdate']['Url'] = "$path/account/account_index.php?action=detailupdate&amp;account_id=$id";
    $actions['account']['detailupdate']['Condition'][] = 'insert';

    // Check Delete
    $actions['account']['check_delete']['Url'] = "$path/account/account_index.php?action=check_delete&amp;account_id=$id";
    $actions['account']['check_delete']['Condition'][] = 'insert';
  }
}

?>
