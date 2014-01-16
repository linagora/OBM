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


?>
<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : payment_index.php 
//     - Desc : payment Index File
// 2001-08-21 Aliacom, AliaSource
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'payment';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
require("$obminclude/global.inc");
$params = get_payment_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
require("$obminclude/global_pref.inc");
require('payment_display.inc');
require('payment_query.inc');
require('payment_js.inc');
require_once("$obminclude/of/of_category.inc");

get_payment_action();
$perm->check_permissions($module, $action);

update_last_visit('payment', $params['payment_id'], $action);
page_close();


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if ($action == 'index') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_payment_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_payment_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} elseif ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_payment_search_form($params);
  $display['result'] = dis_payment_search_list($params);
  
} elseif ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  require('payment_js.inc');
  $display['detail'] = dis_payment_form($action, $params);

} elseif ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_payment_data_form('', $params)) {
    $id = run_query_payment_insert($params);
    if ($id > 0) {
      $params['payment_id'] = $id;
      $display['msg'] = display_ok_msg ("$l_payment : $l_insert_ok");
      $display['detail'] = dis_payment_consult($params);
    } else {
      $display['msg'] = display_err_msg ("$l_payment : $l_insert_error");
      $display['detail'] = dis_payment_form($action, $params);
    }
  // Form data are not valid
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    require('payment_js.inc');
    $display['detail'] = dis_payment_form($action, $params);
  }

} elseif ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_payment_consult($params);

} elseif ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  require('payment_js.inc');
  $display['detail'] = dis_payment_form($action, $params);

} elseif ($action == 'detail_invoice') {
///////////////////////////////////////////////////////////////////////////////
  require('payment_js.inc');
  $display['detail'] = dis_payment_invoice($params);

} else if ($action == 'detailduplicate') {
///////////////////////////////////////////////////////////////////////////////
  $params['id_duplicated'] = $params['payment_id'];
  $params['payment_id'] = '';
  $display['detail'] = dis_payment_form($action, $params);

} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_payment_data_form($params['payment_id'], $params)) {
    $retour = run_query_payment_update($params['payment_id'], $params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_payment : $l_update_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_payment : $l_update_error");
    }
    $display['detail'] = dis_payment_consult($params);
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['detail'] = dis_payment_form($action, $params);
  }

} elseif ($action == 'invoice_update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_payment_invoice_data_form($params)) {
    $retour = run_query_payment_invoice_update($params['payment_id'], $params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_payment : $l_update_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_payment : $l_update_error");
    }
    $display['detail'] = dis_payment_consult($params);
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['detail'] = dis_payment_invoice($params);
  }

} elseif ($action == 'check_delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_payment($params['payment_id'])) {
    require('payment_js.inc');
    $display['msg'] .= display_info_msg($ok_msg, false);
    $display['detail'] = dis_can_delete_payment($params['payment_id']);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_payment_consult($params);
  }

} elseif ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_payment($params['payment_id'])) {
    $retour = run_query_payment_delete($params['payment_id']);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_payment : $l_delete_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_payment : $l_delete_error");
    }
    $display['search'] = dis_payment_search_form($params);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_payment_consult($params);
  }
  
} elseif ($action == 'invoice_add') {
///////////////////////////////////////////////////////////////////////////////
  if (($params['inv_nb'] > 0) && ($params['payment_id'] > 0)) {
    $nb = run_query_payment_invoice_insert($params);
    $display['msg'] .= display_ok_msg("$nb : $l_invoice_added");
  } else {
    $display['msg'] .= display_err_msg("$l_no_invoice_added");
  }
  $display['detail'] = dis_payment_invoice($params);

} elseif ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'],'payment',1);
  $display['detail'] = dis_payment_display_pref ($prefs);

} elseif ($action == 'dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'payment', 1);
  $display['detail'] = dis_payment_display_pref($prefs);

} elseif ($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'payment', 1);
  $display['detail'] = dis_payment_display_pref($prefs);
  
} elseif ($action == 'admin') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_payment_admin_index();
  
} elseif ($action == 'kind_update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_payment_kind($params)) {
    $retour = run_query_payment_kind_update($params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_kind : $l_update_ok");
      $display['detail'] = dis_payment_admin_index();
    } else {
      $display['msg'] .= display_err_msg("$l_err_kind");
      $display['detail'] = dis_payment_admin_index();
    }
  } else {
    $display['msg'] .= display_warn_msg("$l_kind : $err[msg]");
    $display['detail'] = dis_payment_admin_index();
  }
  
} elseif ($action == 'kind_insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_payment_kind($params)) {
    $retour = run_query_payment_kind_insert($params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_kind : $l_insert_ok");
      $display['detail'] = dis_payment_admin_index();
    } else {
      $display['msg'] .= display_err_msg("$l_err_kind");
      $display['detail'] = dis_payment_admin_index();
    }
  } else {
    $display['msg'] .= display_warn_msg("$l_kind : $err[msg]");
    $display['detail'] = dis_payment_admin_index();
  }
  
} elseif ($action == 'kind_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_payment_kind_delete($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_kind : $l_delete_ok");
    $display['detail'] = dis_payment_admin_index();
  } else {
    $display['msg'] .= display_err_msg("$l_err_del_kind");
    $display['detail'] = dis_payment_admin_index();
  }
}

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head("$l_payment");
update_payment_action();
$display['header'] = display_menu($module);
$display['end'] = display_end();
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Payment parameters transmitted in $payment hash
// returns : $payment hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_payment_params() {

  // Get global params
  $params = get_global_params('Payment');

  // Handle payment-invoice associations
  if (isset($params)) {
    $nb_inv = 0;
    while ( list( $key, $value ) = each($params) ) {
      if (strcmp(substr($key, 0, 9),'data-inv-') == 0) {
        $nb_inv++;
        $inv_num = substr($key, 9);
        $params['invoices'][$inv_num] = $value;
      }
    }
    $params['invoices_nb'] = $nb_inv;
  }

  // Add Invoices
  $nb_inv = 0;
  foreach($_REQUEST as $key => $value ) {
    if (strcmp(substr($key, 0, 7),'cb_inv-') == 0) {
      $nb_inv++;
      $inv_num = substr($key, 7);
      $params["invo$nb_inv"] = $inv_num;
    }
  }
  $params['inv_nb'] = $nb_inv;

  return $params;
}


//////////////////////////////////////////////////////////////////////////////
// Payment actions
//////////////////////////////////////////////////////////////////////////////
function get_payment_action() {
  global $params, $actions, $path, $l_payment;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult, $l_header_display, $l_header_admin;
  global $l_header_duplicate, $l_module_invoice, $l_header_link_invoice;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

  $id = $params['payment_id'];

//Index
  $actions['payment']['index'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/payment/payment_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                        );

// Search
  $actions['payment']['search'] = array (
    'Url'      => "$path/payment/payment_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                        );

// New
  $actions['payment']['new'] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/payment/payment_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
                                     );

// Detail Consult
  $actions['payment']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/payment/payment_index.php?action=detailconsult&amp;payment_id=".$params['payment_id'],
    'Right'    => $cright_read,
    'Condition'=> array ('detail_invoice', 'detailupdate', 'invoice_add', 'invoice_update', 'update')
                                        );

// Detail Consult Invoice
  $actions['payment']['detail_invoice'] = array (
    'Name'     => $l_module_invoice,
    'Url'      => "$path/payment/payment_index.php?action=detail_invoice&amp;payment_id=".$params['payment_id'],
    'Right'    => $cright_read,
    'Condition'=> array ('detailconsult', 'detailupdate', 'invoice_add', 'invoice_update', 'update') 
                                        );

// Sel invoice : Invoice selection (menu)
  $actions['payment']['sel_invoice'] = array (
    'Name'     => $l_header_link_invoice,
    'Url'      => "$path/invoice/invoice_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=invoice_add&amp;ext_url=".urlencode($path."/payment/payment_index.php?action=invoice_add&amp;payment_id=$id&amp;sel_invoice_id=")."&amp;ext_id=".$params['payment_id']."&amp;ext_target=$l_payment",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Target'   => $l_payment,
    'Condition'=> array ('detailconsult','detail_invoice','update','invoice_add','invoice_del', 'invoice_update')
                                          );

// Invoice ADD
  $actions['payment']['invoice_add'] = array (
    'Url'      => "$path/payment/payment_index.php?action=invoice_add",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                          );

// Invoice Del
  $actions['payment']['invoice_del'] = array (
    'Url'      => "$path/payment/payment_index.php?action=invoice_del",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                          );

// Detail Duplicate
  $actions['payment']['detailduplicate'] = array (
     'Name'     => $l_header_duplicate,
     'Url'      => "$path/payment/payment_index.php?action=detailduplicate&amp;payment_id=".$params['payment_id'],
     'Right'    => $cright_write,
     'Condition'=> array ('detailconsult', 'detailupdate', 'update') 
                                           );

// Detail Update
  $actions['payment']['detailupdate'] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/payment/payment_index.php?action=detailupdate&amp;payment_id=".$params['payment_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
                                     	      );

// Insert
  $actions['payment']['insert'] = array (
    'Url'      => "$path/payment/payment_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                                );

// Update
  $actions['payment']['update'] = array (
    'Url'      => "$path/payment/payment_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                                );

// Invoice Update
  $actions['payment']['invoice_update'] = array (
    'Url'      => "$path/payment/payment_index.php?action=invoice_update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                                );

// Check Delete
  $actions['payment']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/payment/payment_index.php?action=check_delete&amp;payment_id=".$params['payment_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate')
                                     	 );

// Delete
  $actions['payment']['delete'] = array (
    'Url'      => "$path/payment/payment_index.php?action=delete&amp;payment_id=".$params['payment_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	 );

// Admin
  $actions['payment']['admin'] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/payment/payment_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                       );

// Display
  $actions['payment']['display'] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/payment/payment_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );

// Display
  $actions['payment']['dispref_display'] = array (
    'Url'      => "$path/payment/payment_index.php?action=display_dispref",
    'Right'    => $cright_read, 
    'Condition'=> array ('None') 
                                      	 );

// Display Level
  $actions['payment']['dispref_level'] = array (
    'Url'      => "$path/payment/payment_index.php?action=display_level",
    'Right'    => $cright_read, 
    'Condition'=> array ('None') 
                                      	        );

// Kind update
  $actions['payment']['kind_update'] = array (
    'Url'    => "$path/payment/payment_index.php?action=kind_update",
    'Right'  => $cright_write_admin,
    'Condition'  => array('None')
  );
  
  // Kind Insert
  $actions['payment']['kind_insert'] = array (
    'Url'    => "$path/payment/payment_index.php?action=kind_insert",
    'Right'  => $cright_write_admin,
    'Condition'  => array('None')
  );
  
  // Kind Delete
  $actions['payment']['kind_delete'] = array (
    'Url'    => "$path/payment/payment_index.php?action=kind_delete",
    'Right'  => $cright_write_admin,
    'Condition'  => array('None')
  );

}


///////////////////////////////////////////////////////////////////////////////
// Payment Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_payment_action() {
  global $params, $actions, $path, $l_payment;

  $id = $params['payment_id'];
  if ($id > 0) {

    $p = get_payment_info($id);
    // Detail Consult
    $actions['payment']['detailconsult']['Url'] = "$path/payment/payment_index.php?action=detailconsult&amp;payment_id=$id";
    $actions['payment']['detailconsult']['Condition'][] = 'insert';

    // Sel invoice : Invoice selection (menu)
    $actions['payment']['sel_invoice']['Url'] = "$path/invoice/invoice_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=invoice_add&amp;ext_url=".urlencode($path."/payment/payment_index.php?action=invoice_add&amp;payment_id=$id&amp;sel_invoice_id=")."&amp;ext_id=$id&amp;ext_target=$l_payment&amp;company=".urlencode($p['company']);
    $actions['payment']['sel_invoice']['Condition'][] = 'insert';

    // Invoice
    $actions['payment']['detail_invoice']['Url'] = "$path/payment/payment_index.php?action=detail_invoice&amp;payment_id=$id";
    $actions['payment']['detail_invoice']['Condition'][] = 'insert';

    // Detail Update
    $actions['payment']['detailupdate']['Url'] = "$path/payment/payment_index.php?action=detailupdate&amp;payment_id=$id";
    $actions['payment']['detailupdate']['Condition'][] = 'insert';

    // Check Delete
    $actions['payment']['check_delete']['Url'] = "$path/payment/payment_index.php?action=check_delete&amp;payment_id=$id";
    $actions['payment']['check_delete']['Condition'][] = 'insert';
  }
}

?>
