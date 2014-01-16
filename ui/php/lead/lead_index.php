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
// OBM - File : lead_index.php 
//     - Desc : lead Index File
// 2006-05-19 Aliacom - PB
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'lead';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
require("$obminclude/global.inc");
$params = get_lead_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
require("$obminclude/global_pref.inc");
require('lead_display.inc');
require('lead_query.inc');
require_once('lead_js.inc');
require_once("$obminclude/of/of_category.inc");

get_lead_action();
$perm->check_permissions($module, $action);

update_last_visit('lead', $params['lead_id'], $action);
page_close();


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if ($action == 'index' || $action == '') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_lead_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_lead_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} elseif ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_lead_search_form($params);
  $display['result'] = dis_lead_search_list($params);
  
} elseif ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_lead_form($action, $params);

} elseif ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_lead_data_form('', $params)) {
    $id = run_query_lead_insert($params);
    if ($id > 0) {
      $params['lead_id'] = $id;
      $display['msg'] = display_ok_msg ("$l_lead : $l_insert_ok");
      $display['detail'] = dis_lead_consult($params);
    } else {
      $display['msg'] .= display_err_msg("$l_lead : $l_insert_error : $err[msg]");
      $display['detail'] = dis_lead_form($action, $params);
    }

  // Form data are not valid
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['detail'] = dis_lead_form($action, $params);
  }

} elseif ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_lead_consult($params);

} elseif ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_lead_form($action, $params);

} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_lead_data_form($params['lead_id'], $params)) {
    $retour = run_query_lead_update($params['lead_id'], $params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_lead : $l_update_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_lead : $l_update_error");
    }
    $display['detail'] = dis_lead_consult($params);
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['detail'] = dis_lead_form($action, $params);
  }

} elseif ($action == 'check_delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_lead($params['lead_id'])) {
    $display['msg'] .= display_info_msg($ok_msg, false);
    $display['detail'] = dis_can_delete_lead($params['lead_id']);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_lead_consult($params);
  }

} elseif ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_lead($params['lead_id'])) {
    $retour = run_query_lead_delete($params['lead_id']);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_lead : $l_delete_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_lead : $l_delete_error");
    }
    $display['search'] = dis_lead_search_form($params);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_lead_consult($params);
  }
  
} elseif ($action == 'admin') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_lead_admin_index();

} elseif ($action == 'source_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert('lead', 'source', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_source : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_source : $l_insert_error");
  }
  $display['detail'] .= dis_lead_admin_index();

} elseif ($action == 'source_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update('lead', 'source', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_source : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_source : $l_update_error");
  }
  $display['detail'] .= dis_lead_admin_index();

} elseif ($action == 'source_checklink') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= of_category_dis_links('lead', 'source', $params, 'mono');

} elseif ($action == 'source_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete('lead', 'source', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_source : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_source : $l_delete_error");
  }
  $display['detail'] .= dis_lead_admin_index();

} elseif ($action == 'status_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert('lead', 'status', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_status : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_status : $l_insert_error");
  }
  $display['detail'] .= dis_lead_admin_index();

} elseif ($action == 'status_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update('lead', 'status', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_status : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_status : $l_update_error");
  }
  $display['detail'] .= dis_lead_admin_index();

} elseif ($action == 'status_checklink') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= of_category_dis_links('lead', 'status', $params, 'mono');

} elseif ($action == 'status_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete('lead', 'status', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_status : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_status : $l_delete_error");
  }
  $display['detail'] .= dis_lead_admin_index();

} elseif ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'], 'lead', 1);
  $display['detail'] = dis_lead_display_pref($prefs);

} elseif ($action == 'dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'lead', 1);
  $display['detail'] = dis_lead_display_pref($prefs);

} elseif ($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'lead', 1);
  $display['detail'] = dis_lead_display_pref($prefs);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head("$l_lead");
update_lead_action();
$display['header'] = display_menu($module);
$display['end'] = display_end();
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Lead parameters transmitted in $lead hash
// returns : $lead hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_lead_params() {
  
  $params = get_global_params('Lead');

  return $params;
}


//////////////////////////////////////////////////////////////////////////////
// Lead actions
//////////////////////////////////////////////////////////////////////////////
function get_lead_action() {
  global $params, $actions, $path;
  global $l_header_find, $l_header_new, $l_header_update, $l_header_delete;
  global $l_header_consult, $l_header_display, $l_header_admin;
  global $l_header_convert_deal;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

  $id = $params['lead_id'];

//Index
  $actions['lead']['index'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/lead/lead_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                        );

// Search
  $actions['lead']['search'] = array (
    'Url'      => "$path/lead/lead_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                        );

// New
  $actions['lead']['new'] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/lead/lead_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
                                     );

// Detail Consult
  $actions['lead']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/lead/lead_index.php?action=detailconsult&amp;lead_id=$id",
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate', 'update')
                                        );

// Convert to Deal
  $actions['lead']['convert_deal'] = array (
    'Name'     => $l_header_convert_deal,
    'Url'      => "$path/deal/deal_index.php?action=new&amp;lead_id=$id",
    'Right'    => $cright_read,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update')
                                        );

// Detail Update
  $actions['lead']['detailupdate'] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/lead/lead_index.php?action=detailupdate&amp;lead_id=$id",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'update') 
                                     	      );

// Insert
  $actions['lead']['insert'] = array (
    'Url'      => "$path/lead/lead_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                                );

// Update
  $actions['lead']['update'] = array (
    'Url'      => "$path/lead/lead_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                                );

// Check Delete
  $actions['lead']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/lead/lead_index.php?action=check_delete&amp;lead_id=$id",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update')
                                     	 );

// Delete
  $actions['lead']['delete'] = array (
    'Url'      => "$path/lead/lead_index.php?action=delete&amp;lead_id=$id",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	 );

// Admin
  $actions['lead']['admin'] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/lead/lead_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                       );

// Source Insert
  $actions['lead']['source_insert'] = array (
    'Url'      => "$path/lead/lead_index.php?action=source_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Source Update
  $actions['lead']['source_update'] = array (
    'Url'      => "$path/lead/lead_index.php?action=source_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Source Check Link
  $actions['lead']['source_checklink'] = array (
    'Url'      => "$path/lead/lead_index.php?action=source_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Source Delete
  $actions['lead']['source_delete'] = array (
    'Url'      => "$path/lead/lead_index.php?action=source_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Status Insert
  $actions['lead']['status_insert'] = array (
    'Url'      => "$path/lead/lead_index.php?action=status_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Status Update
  $actions['lead']['status_update'] = array (
    'Url'      => "$path/lead/lead_index.php?action=status_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Status Check Link
  $actions['lead']['status_checklink'] = array (
    'Url'      => "$path/lead/lead_index.php?action=status_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Status Delete
  $actions['lead']['status_delete'] = array (
    'Url'      => "$path/lead/lead_index.php?action=status_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Display
  $actions['lead']['display'] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/lead/lead_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );

// Display
  $actions['lead']['dispref_display'] = array (
    'Url'      => "$path/lead/lead_index.php?action=display_dispref",
    'Right'    => $cright_read, 
    'Condition'=> array ('None') 
                                      	 );

// Display Level
  $actions['lead']['dispref_level'] = array (
    'Url'      => "$path/lead/lead_index.php?action=display_level",
    'Right'    => $cright_read, 
    'Condition'=> array ('None') 
                                      	        );

}


///////////////////////////////////////////////////////////////////////////////
// Lead Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_lead_action() {
  global $params, $actions, $path, $l_lead;

  $id = $params['lead_id'];
  if ($id > 0) {
    // Detail Consult
    $actions['lead']['detailconsult']['Url'] = "$path/lead/lead_index.php?action=detailconsult&amp;lead_id=$id";
    $actions['lead']['detailconsult']['Condition'][] = 'insert';

    // Detail Update
    $actions['lead']['detailupdate']['Url'] = "$path/lead/lead_index.php?action=detailupdate&amp;lead_id=$id";
    $actions['lead']['detailupdate']['Condition'][] = 'insert';

    // Check Delete
    $actions['lead']['check_delete']['Url'] = "$path/lead/lead_index.php?action=check_delete&amp;lead_id=$id";
    $actions['lead']['check_delete']['Condition'][] = 'insert';
  }
}

?>
