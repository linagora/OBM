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
// OBM - File : invoice_index.inc
//     - Desc : Invoice Main file
// 2001-07-30 Aliacom - Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default)    -- search fields  -- show the invoice search form
// - search             -- search fields  -- show the result set of search
// - new                -- $params        -- show the new invoice form
// - detailconsult      -- $param_invoice -- show the invoice detail
// - detailupdate       -- $param_invoice -- show the invoice detail form
// - duplicate          -- $params       -- new invoice form from existing one
// - insert             -- form fields    -- insert the invoice
// - update             -- form fields    -- update the invoice
// - check_delete       -- $param_invoice -- check links before delete
// - delete             -- $param_invoice -- delete the invoice
// - display            --                -- display and set display parameters
// - dispref_display    --                -- update one field display value
// - dispref_level      --                -- update one field display position 
// - document_add       -- $params sess  -- link documents to an invoice
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'invoice';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_invoice_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
require('invoice_display.inc');
require('invoice_query.inc');
require_once('invoice_js.inc');
require_once("$obminclude/of/of_select.inc");
require_once("$obminclude/of/of_category.inc");
require_once("$obminclude/lib/ODF/odf.inc");

get_invoice_action();
$perm->check_permissions($module, $action);

update_last_visit('invoice', $params['invoice_id'], $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if (($action == 'ext_get_ids') || ($action == 'ext_get_id')) {
  if ($action == 'ext_get_ids') {
    $params['ext_type'] = 'multi';
  } else {
    $params['ext_type'] = 'mono';
  }
  $display['search'] = dis_invoice_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_invoice_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }
  
} elseif ($action == 'index' || $action == '') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_invoice_search_form($params); 
  if ($_SESSION['set_display'] == 'yes') { 
    $display['result'] = dis_invoice_search_list($params);
  } else { 
    $display['msg'] .= display_info_msg($l_no_display); 
  } 

} elseif ($action == 'search')  { 
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_invoice_search_form($params); 
  $display['result'] = dis_invoice_search_list($params);
  
} elseif ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_invoice_form($action, $params);

} elseif ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  if (isset($params['export_odf']) && isset($params['invoice_id']))
  {
	if ($params['account'] == 0) {
	  $display['msg'] .= display_err_msg($l_odf_choose_bank);
	} elseif ($params['odf_model'] === "0") {
	  $display['msg'] .= display_err_msg($l_odf_choose_model);
	} else {
	  $inv_q = get_invoice_export_infos($params['invoice_id'], $params['account']);
	  $odf = new ODF($params['odf_model']);
	  $res = $odf->export($inv_q);
	  if ($res !== true)
		$display['msg'] .= display_err_msg($res);
	}
  }
  $display['detail'] = dis_invoice_consult($params);
	
}elseif ($action =='reminder'){
	
	if (isset($params['relance_odf']) && isset($params['invoice_id']))
        {	
	  $inv_q = get_invoice_export_reminder_infos($params['invoice_id']);
		
	  $odf = new ODF('relance_1.odt');
	  $res = $odf->export_reminder($inv_q);
	  if ($res !== true){
		$display['msg'] .= display_err_msg($res);
	}

       }else{
	 $inv_q = get_invoice_export_reminder_infos($params['invoice_id']);
	  $odf = new ODF('relance_2.odt');
	  $res = $odf->export_reminder($inv_q);
	  if ($res !== true){
		$display['msg'] .= display_err_msg($res);
	}
       }
	$display['detail'] = dis_invoice_consult($params);

} elseif ($action == 'detailupdate') { 
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_invoice_form($action, $params);

} elseif ($action == 'duplicate') {
///////////////////////////////////////////////////////////////////////////////
  // we give the user the traditionnal form to modify this invoice :
  $display['detail'] = dis_invoice_form($action, $params);
  
} elseif ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_invoice_data_form('', $params)) {
    $retour = run_query_invoice_insert($params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_invoice : $l_insert_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_invoice : $l_insert_error");
    }
    $display['search'] = dis_invoice_search_form($params);
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['detail'] = dis_invoice_form($action, $params, $err['field']);
  }
  
} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_invoice_data_form($params['invoice_id'], $params)) {
    $retour = run_query_invoice_update($params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_invoice : $l_update_ok");
    } else {
      $display['msg'] .= display_ok_msg("$l_invoice : $l_update_error");
    }
    $display['detail'] = dis_invoice_consult($params);
  } else {
    $display['msg'] .= display_err_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['search'] = dis_invoice_form($action, $params, $err['field']);
  }

} elseif ($action == 'check_delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_invoice($params['invoice_id'])) {
    $display['msg'] .= display_info_msg($ok_msg, false);
    $display['detail'] = dis_can_delete_invoice($params['invoice_id']);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_invoice_consult($params);
  }
  //  $display['detail'] = dis_check_invoice_links($params['invoice_id']);

} elseif ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_invoice($params['invoice_id'])) {
    $retour = run_query_invoice_delete($params['invoice_id']); 
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_invoice : $l_delete_ok");
      $display['search'] = dis_invoice_search_form($params);
    } else {
      $display['msg'] .= display_err_msg("$l_invoice : $l_delete_error");
      $display['detail'] = dis_invoice_consult($params);
    }
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_invoice_consult($params);
  }

} elseif ($action == 'dashboard') {
///////////////////////////////////////////////////////////////////////////////
  //include_once("$obminclude/Artichow/BarPlot.class.php");
  $display['detail'] = dis_invoice_dashboard_index($params);

} elseif ($action == 'document_add') {
///////////////////////////////////////////////////////////////////////////////
  $params['invoice_id'] = $params['ext_id'];
  if ($params['doc_nb'] > 0) {
    $nb = run_query_global_insert_documents_links($params, 'invoice');
    $display['msg'] .= display_ok_msg("$nb $l_document_added");
  } else {
    $display['msg'] .= display_err_msg($l_no_document_added);
  }
  $display['detail'] = dis_invoice_consult($params);

} elseif ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'], 'invoice', 1);
  $display['detail'] = dis_invoice_display_pref($prefs);
  
}elseif($action == 'all_reminder'){
  $req = all_reminder();

  $display['detail'] = dis_invoice_all_reminder($req);

}else if($action == 'reminder_company'){

 $inv_q = reminder_by_company($params['company_id']);
 $display['detail'] = dis_invoice_reminder_company($inv_q);

} else if ($action == 'dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'invoice', 1);
  $display['detail'] = dis_invoice_display_pref($prefs);
  
} else if($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'invoice', 1);
  $display['detail'] = dis_invoice_display_pref($prefs);
}
  

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head("$l_invoice");
$display['end'] = display_end();
if (! $params['popup']) {
  update_invoice_action();
  $display['header'] = display_menu($module);
}
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Invoice parameters transmitted in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_invoice_params() {

  // Get global params
  $params = get_global_params('Invoice');

  // Get Invoice specific params
  // sel_tt
  if (is_array($params['tt'])) {
    while ( list( $key, $value ) = each( $params['tt'] ) ) {
      // sel_tt contains select infos (data-tt-$id)
      if (strcmp(substr($value, 0, 8),'data-tt-') == 0) {
        $data = explode('-', $value);
        $id = $data[2];
        $params['tasktype'][] = $id;
      } else {
        // sel_tt contains ids
        $params['tasktype'][] = $value;
      } 
    }
  }
  
  get_global_params_document($params);
  
  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Invoice actions
///////////////////////////////////////////////////////////////////////////////
function get_invoice_action() {
  global $params, $actions, $path;
  global $l_header_find,$l_header_new_f,$l_header_update,$l_header_delete;
  global $l_header_consult, $l_header_display,$l_header_duplicate,$l_header_admin;
  global $l_header_add_deal, $l_header_dashboard, $params_admin_write;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index 
  $actions['invoice']['index'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/invoice/invoice_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                       );

// Search
  $actions['invoice']['search'] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                   );

// New
  $actions['invoice']['new'] = array (
    'Name'     => $l_header_new_f,
    'Url'      => "$path/invoice/invoice_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('','search','index','detailconsult','insert', 'update','delete','display')
                                   );

//Insert
  $actions['invoice']['insert'] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                   );

// Detail Consult
  $actions['invoice']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/invoice/invoice_index.php?action=detailconsult&amp;invoice_id=".$params['invoice_id'],
    'Right'    => $cright_read,
    'Condition'=> array ('detailconsult', 'detailupdate', 'duplicate', 'update')
                                   );

	$actions['invoice']['reminder'] = array (
	'Name'     => $GLOBALS['l_reminder'],
	'URL'      => "$path/invoice/invoice_index.php?action=reminder&amp;invoice_id=".$params['invoice_id'],
	'Right'	   => $cright_read,
	'Condition'=> array ('detailconsult', 'detailupdate', 'duplicate', 'update'));

// Duplicate
  $actions['invoice']['duplicate'] = array (
    'Name'     => $l_header_duplicate,
    'Url'      => "$path/invoice/invoice_index.php?action=duplicate&amp;invoice_id=".$params['invoice_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'update')
                                     	   );

// Detail Update
  $actions['invoice']['detailupdate'] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/invoice/invoice_index.php?action=detailupdate&amp;invoice_id=".$params['invoice_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'update')
                                     	       );

// Update
  $actions['invoice']['update'] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                        );

// Check Delete
  $actions['invoice']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/invoice/invoice_index.php?action=check_delete&amp;invoice_id=".$params['invoice_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update')
                                     	      );

// Delete
  $actions['invoice']['delete'] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=delete&amp;invoice_id=".$params['invoice_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	);

// Dashboard
  $actions['invoice']['dashboard'] = array (
    'Name'     => $l_header_dashboard,
    'Url'      => "$path/invoice/invoice_index.php?action=dashboard",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all')
                                        );

// Display
  $actions['invoice']['display'] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/invoice/invoice_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                        );
// all_reminder
  $actions['invoice']['all_reminder'] = array (
    'Name'     => 'Relances',
    'Url'      => "$path/invoice/invoice_index.php?action=all_reminder",
    'Right'    => $cright_read,
    'Condition'=> array ('all'));

// reminder_company
 $actions['invoice']['reminder_company'] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=reminder_company&amp;company_id=".$params['company_id'],
    'Right'    => $cright_read,
    'Condition'=> array ('None'));

// Display Preferences
  $actions['invoice']['dispref_display'] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                        );

// Display Preferences
  $actions['invoice']['dispref_level'] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                        );

// Document add
  $actions['invoice']['document_add'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None')
  );

// External Invoice Select 
  $actions['invoice']['ext_get_ids']  = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

}


///////////////////////////////////////////////////////////////////////////////
// Invoice Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_invoice_action() {
  global $params, $actions, $path;

  $id = $params['invoice_id'];
  $idc = $params['company_id'];
  if ($id > 0) {
    // Detail Consult
    $actions['invoice']['detailconsult']['Url'] = "$path/invoice/invoice_index.php?action=detailconsult&amp;invoice_id=$id";
    $actions['invoice']['detailconsult']['Condition'][] = 'insert';

    // Detail Update
    $actions['invoice']['detailupdate']['Url'] = "$path/invoice/invoice_index.php?action=detailupdate&amp;invoice_id=$id";
    $actions['invoice']['detailupdate']['Condition'][] = 'insert';

    // Duplicate
    $actions['invoice']['duplicate']['Url'] = "$path/invoice/invoice_index.php?action=duplicate&amp;invoice_id=$id";
    $actions['invoice']['duplicate']['Condition'][] = 'insert';

    // Check Delete
    $actions['invoice']['check_delete']['Url'] = "$path/invoice/invoice_index.php?action=check_delete&amp;invoice_id=$id";
    $actions['invoice']['check_delete']['Condition'][] = 'insert';
	
     //reminder_company
    $actions['invoice']['reminder_company']['Url'] = "$path/invoice/invoice_index.php?action=reminder_company&amp;company_id=$idc";
    $actions['invoice']['reminder_company']['Condition'][] = 'insert';

  }
}

?>
