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
// OBM - File : deal_index.php                                               //
//     - Desc : Deal Index File                                              //
// 1999-04-10 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the deal search form
// - search          -- search fields  -- show the result set of search
// - new             -- $company_id -- show the new deal form
// - detailconsult   -- $deal_id    -- show the deal detail
// - detailupdate    -- $deal_id    -- show the deal detail form
// - quick_detail    -- $deal_id    -- show the deal quick detail form
// - insert          -- form fields    -- insert the deal
// - update          -- form fields    -- update the deal
// - quick_update    -- form fields    -- update (quick) the deal
// - check_delete    -- $deal_id    -- check links before delete
// - delete          -- $deal_id    -- delete the deal
// - affect          -- $deal_id    -- show the new parent deal form
// - affect_update   -- $deal_id    -- affect the deal to the parentdeal
// - dashboard       -- form fields    -- Display the dashboard screen
// - document_add    -- form fields    -- Add a doucment
// - admin           --                -- admin index (type)
// - type_insert     -- form fields    -- insert the type
// - type_update     -- form fields    -- update the type
// - type_checklink  -- $sel_type      -- check if type is used
// - type_delete     -- $sel_type      -- delete the type
// - status_insert   -- form fields    -- insert the status
// - status_update   -- form fields    -- update the status
// - status_checklink-- $sel_status    -- check if status is used
// - status_delete   -- $sel_status    -- delete the status
// - category1_insert    -- form fields    -- insert the category
// - category1_update    -- form fields    -- update the category
// - category1_checklink -- $sel_cat       -- check if category is used
// - category1_delete    -- $sel_cat       -- delete the category
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
// - parent_search        -- search fields  -- show the result set of search
// - parent_new           -- search fields  -- show the result set of search
// - parent_detailconsult -- $parentdeal_id  -- show the parent detail
// - parent_detailupdate  -- $parentdeal_id  -- show the parent detail form
// - parent_insert        -- form fields    -- insert the deal
// - parent_update        -- form fields    -- update the parent
// - parent_delete        -- $parentdeal_id  -- delete the parent
// External API ---------------------------------------------------------------
// - ext_get_id      -- $ext_params    -- select a deal (return id) 
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'deal';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_deal_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
require('deal_query.inc');
require('deal_display.inc');
require('deal_js.inc');
require_once("$obminclude/of/of_select.inc");
require_once("$obminclude/of/of_category.inc");

get_deal_action();
$perm->check_permissions($module, $action);
if (! check_privacy($module, 'Deal', $action, $params['deal_id'], $obm['uid'])) {
  $display['msg'] = display_err_msg($l_error_visibility);
  $action = 'index';
} else {
  update_last_visit('deal', $params['deal_id'], $action);
  update_last_visit('parentdeal', $params['parentdeal_id'], $action);
}
page_close();


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

// when searching deals belonging to a parent, we display the parent
if (($action == 'search') && ($params['parentdeal_id'])) {
///////////////////////////////////////////////////////////////////////////////
  $action = 'parent_detailconsult';
}


///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == 'ext_get_id') {
  $display['search'] = dis_deal_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_deal_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} elseif ($action == 'ext_get_category1_ids') {
  $extra_css = 'category.css';
  $display['detail'] = of_category_dis_tree('deal', 'category1', $params, $action);

///////////////////////////////////////////////////////////////////////////////
// Normal calls
///////////////////////////////////////////////////////////////////////////////
} elseif (($action == 'index') || ($action == '')) {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_deal_index();
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_deal_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }
 
} elseif ($action == 'search') { // tester si hd_parent mis ??? pour form :oui
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_deal_search_form($params);
  $display['result'] = dis_deal_search_list($params);
  
} elseif ($action == 'new')  {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_deal_form($params);

} elseif ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_deal_consult($params);

} elseif ($action == 'quick_detail') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_deal_quick_form($params);
  
} elseif ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_deal_form($params);

} elseif ($action == 'duplicate') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_deal_form($params);

} elseif ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_deal_form('', $params)) {
    $params['deal_id'] = run_query_deal_insert($params);
    if ($params['deal_id']) {
      $display['msg'] .= display_ok_msg("$l_deal : $l_insert_ok");
      $display['detail'] = dis_deal_consult($params);
    } else {
      $display['msg'] .= display_err_msg("$l_deal : $l_insert_error : $err[msg]");
      $display['search'] = dis_deal_index($params);
    }
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['detail'] = dis_deal_form($params);
  }

} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_deal_form('', $params)) {
    $retour = run_query_deal_update($params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_deal : $l_update_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_deal : $l_update_error");
    }
    $display['detail'] = dis_deal_consult($params);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['detail'] = dis_deal_form($params);
  }

} elseif ($action == 'quick_update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_deal_quick_form($params)) {
    $retour = run_query_deal_quick_update($params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_deal : $l_update_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_deal : $l_update_error");
    }
    $display['detail'] = dis_deal_consult($params);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['detail'] = dis_deal_quick_form($params);
  }

} elseif ($action == 'check_delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_deal($params['deal_id'])) {
    $display['msg'] .= display_info_msg($ok_msg, false);
    $display['detail'] = dis_deal_can_delete_deal($params['deal_id']);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_deal_consult($params);
  }

} elseif ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_deal($params['deal_id'])) {
    $retour = run_query_deal_delete($params['deal_id']);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_deal : $l_delete_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_deal : $l_delete_error");
    }
    $display['search'] = dis_deal_index();
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_deal_consult($params);
  }

} elseif ($action == 'dashboard') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_deal_dashboard_index($params);
  $display['detail'] .= dis_deal_dashboard_view($params);

} elseif ($action == 'dashboard_list') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_deal_dashboard_index($params);
  $display['detail'] .= dis_deal_dashboard_list($params);

} elseif ($action == 'document_add') {
///////////////////////////////////////////////////////////////////////////////
  $params['deal_id'] = $params['ext_id'];
  if ($params['doc_nb'] > 0) {
    $nb = run_query_global_insert_documents_links($params, 'deal');
    $display['msg'] .= display_ok_msg("$nb $l_document_added");
  } else {
    $display['msg'] .= display_err_msg($l_no_document_added);
  }
  $display['detail'] = dis_deal_consult($params);

} elseif ($action == 'admin') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_deal_admin_index();
  
} elseif ($action == 'category1_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert('deal', 'category1', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_category1 : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_category1 : $l_insert_error");
  }
  $display['detail'] = dis_deal_admin_index();

} elseif ($action == 'category1_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update('deal', 'category1', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_category1 : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_category1 : $l_update_error");
  }
  $display['detail'] = dis_deal_admin_index();

} elseif ($action == 'category1_checklink') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= of_category_dis_links('deal', 'category1', $params);

} elseif ($action == 'category1_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete('deal', 'category1', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_category1 : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_category1 : $l_delete_error");
  }
  $display['detail'] = dis_deal_admin_index();

} elseif ($action == 'type_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_deal_type_insert($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_type : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_type : $l_insert_error");
  }
  $display['detail'] = dis_deal_admin_index();
  
} elseif ($action == 'type_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_deal_type_update($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_type : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_type : $l_update_error");
  }
  $display['detail'] = dis_deal_admin_index();
  
} elseif ($action == 'type_checklink') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_deal_type_links($params['type']);
  
} elseif ($action == 'type_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_deal_type_delete($params['type_id']);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_type : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_type : $l_delete_error");
  }
  $display['detail'] = dis_deal_admin_index();

} elseif ($action == 'status_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_deal_status_insert($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_status : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_status : $l_insert_error");
  }
  $display['detail'] = dis_deal_admin_index();

} elseif ($action == 'status_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_deal_status_update($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_status : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_status : $l_update_error");
  }
  $display['detail'] = dis_deal_admin_index();

} elseif ($action == 'status_checklink') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_deal_status_links($params['status']);

} elseif ($action == 'status_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_deal_status_delete($params['status_id']);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_status : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_status : $l_delete_error");
  }
  $display['detail'] = dis_deal_admin_index();

} elseif ($action == 'role_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert('DealCompany', 'role', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_role : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_role : $l_insert_error");
  }
  $display['detail'] = dis_deal_admin_index();
  
} elseif ($action == 'role_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update('DealCompany', 'role', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_role : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_role : $l_update_error");
  }
  $display['detail'] = dis_deal_admin_index();
  
} elseif ($action == 'role_checklink') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= of_category_dis_links('DealCompany', 'role', $params, 'mono');
  
} elseif ($action == 'role_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete('DealCompany', 'role', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_role : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_role : $l_delete_error");
  }
  $display['detail'] = dis_deal_admin_index();


///////////////////////////////////////////////////////////////////////////////
// -- Actions about ParentDeal -- 
///////////////////////////////////////////////////////////////////////////////


} elseif ($action == 'parent_search') {
///////////////////////////////////////////////////////////////////////////////
  $usr_q = run_query_deal_manager();
  $display['search'] = html_deal_parentdeal_search_form($params, $usr_q);
  $display['result'] = dis_deal_parentdeal_search_list($params);

} elseif ($action == 'parent_new') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_deal_parentdeal_form($action, $params);

} elseif ($action == 'parent_detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_deal_parentdeal_consult($params);

} elseif ($action == 'parent_detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_deal_parentdeal_form($action, $params);
  
} elseif ($action == 'parent_insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_deal_parent_form('', $params)) {
    $retour = run_query_deal_insert_parentdeal($params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_parentdeal : $l_insert_ok"); 
    } else {
      $display['msg'] .= display_err_msg("$l_parentdeal : $l_insert_error : $err[msg]");
    }
    $display['search'] = html_deal_parentdeal_search_form($params, run_query_deal_manager(1));
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
    $display['search'] = html_deal_parentdeal_search_form($params, run_query_deal_manager(1));
  }
  
} elseif ($action == 'parent_delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_deal_can_delete_parentdeal($params['parentdeal_id'])) {
    run_query_deal_parentdeal_delete($params['parentdeal_id']); 
    $display['msg'] .= display_ok_msg("$l_parentdeal : $l_delete_ok"); 
    $display['search'] = html_deal_parentdeal_search_form($params, run_query_deal_manager(1));
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete_parent, false);
    $display['search'] = dis_deal_parentdeal_consult($params);
  }
  
} elseif  ($action == 'parent_update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_deal_parent_form('', $params)) {
    $retour = run_query_deal_parentdeal_update($params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_parentdeal : $l_update_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_parentdeal : $l_update_error");
    }
    $display['detail'] = dis_deal_parentdeal_consult($params);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['detail'] = dis_deal_parentdeal_form($action, $params);
  }

} elseif ($action == 'affect') {
///////////////////////////////////////////////////////////////////////////////
  $parent_q = run_query_deal_search_parentdeal('');
  $display['detail'] = html_deal_affect($parent_q, $params['deal_id']);

} elseif ($action == 'affect_update') {
///////////////////////////////////////////////////////////////////////////////
  if ($params['deal_id'] > 0) {
    run_query_deal_affect_deal_parentdeal($params['deal_id'], $params);
    $display['msg'] .= display_ok_msg($l_updateaffect_ok);
    $display['detail'] = dis_deal_consult($params);
  }

} elseif ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'],'deal',1);
  $prefs_parent = get_display_pref($obm['uid'],'parentdeal',1);
  $display['detail'] = dis_deal_display_pref($prefs, $prefs_parent);
  
} else if ($action == 'dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'],'deal',1);
  $prefs_parent = get_display_pref($obm['uid'],'parentdeal',1);
  $display['detail'] = dis_deal_display_pref($prefs, $prefs_parent);

} else if ($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'],'deal',1);
  $prefs_parent = get_display_pref($obm['uid'],'parentdeal',1);
  $display['detail'] = dis_deal_display_pref($prefs, $prefs_parent);
}

of_category_user_action_switch($module, $action, $params);


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
// Update actions url in case some values have been updated (id after insert) 
update_deal_action();
if (! $popup) {
  $display['header'] = display_menu($module);
}
$display['head'] = display_head($l_deal);
$display['end'] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $params hash, Deal parameters transmited
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_deal_params() {
  
  // Get global params
  $params = get_global_params('Deal');
  
  // Get deal specific params
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

  // Handle DealCompany infos
  $cpt = 0;
  while (isset($params["data_dc_$cpt"])) {
    $dccid = $params["data_dccid_$cpt"];
    $dccname = $params["data_dccname_$cpt"];
    $dccnewid = $params["data_dccnewid_$cpt"];
    $dccnewname = $params["data_dccnewname_$cpt"];
    $dcroleid = $params["role$cpt"];

    if ($dccnewid > 0) {
      $params['dc'][$cpt]['company_id'] = $dccnewid;
      $params['dc'][$cpt]['company_name'] = $dccnewname;
      $params['dc'][$cpt]['role_id'] = $dcroleid;
    } else if ($dccid > 0) {
      $params['dc'][$cpt]['company_id'] = $dccid;
      $params['dc'][$cpt]['company_name'] = $dccname;
      $params['dc'][$cpt]['role_id'] = $dcroleid;
    }
    $cpt++;
  }

  $params['order'] = (isset($params['order']) ? $params['order'] : '0');
  
  get_global_params_document($params);
  
  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Deal Actions 
///////////////////////////////////////////////////////////////////////////////
function get_deal_action() {
  global $params, $actions, $path;
  global $l_header_find,$l_header_new_f,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_duplicate,$l_header_display;
  global $l_header_new_child, $l_header_new_parent, $l_header_quickupdate;
  global $l_header_dashboard,$l_header_admin, $l_deal_select_company;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

  of_category_user_module_action('deal');
  $id = $params['deal_id'];
  $pid = $params['parentdeal_id'];

  // Index
  $actions['deal']['index'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/deal/deal_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

  // Search
  $actions['deal']['search'] = array (
    'Url'      => "$path/deal/deal_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     );

  // Parent Search
  $actions['deal']['parent_search'] = array (
    'Url'      => "$path/deal/deal_index.php?action=parent_search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     );

  // New
  $actions['deal']['new'] = array (
    'Name'     => $l_header_new_f,
    'Url'      => "$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_deal_select_company)."&amp;ext_url=".urlencode("$path/deal/deal_index.php?action=new&amp;company_id="),
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Condition'=> array ('all') 
                                  );

  // New Child
  $ret_url = urlencode("$path/deal/deal_index.php?action=new&amp;parentdeal_id=$pid&amp;sel_market=" . $params['pmarket'] . "&amp;sel_tech=" . $params['ptech'] . "&amp;company_id=");
  $actions['deal']['new_child'] = array (
    'Name'     => $l_header_new_child,
    'Url'      => "$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_deal_select_company)."&amp;ext_url=$ret_url",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Condition'=> array ('parent_detailconsult') 
                                  );

  // Parent New
  $actions['deal']['parent_new'] = array (
    'Name'     => $l_header_new_parent,
    'Url'      => "$path/deal/deal_index.php?action=parent_new",
    'Right'    => $cright_write,
    'Condition'=> array ('','search','parent_search','index','detailconsult',
                         'parent_detailconsult','parent_insert',
                         'admin','display') 
                                         );

  // Detail Consult
  $actions['deal']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/deal/deal_index.php?action=detailconsult&amp;deal_id=$id",
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('detailupdate', 'update', 'quick_detail', 'quick_update') 
                                    	    );

  // Quick Detail
  $actions['deal']['quick_detail'] = array (
    'Name'     => $l_header_quickupdate,
    'Url'      => "$path/deal/deal_index.php?action=quick_detail&amp;deal_id=$id",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'quick_update')
                                    	    );

  // Detail Update
  $actions['deal']['detailupdate'] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/deal/deal_index.php?action=detailupdate&amp;deal_id=$id",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'update', 'quick_detail', 'quick_update')
                                     	    );
					    
  // Duplicate
  $actions['deal']['duplicate'] = array (
    'Name'     => $l_header_duplicate,
    'Url'      => "$path/deal/deal_index.php?action=duplicate&amp;deal_id=$id",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
  );

  // Convert from Lead
  $actions['deal']['lead_convert'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	    );
					    
  //  Update
  $actions['deal']['update'] = array (
    'Url'      => "$path/deal/deal_index.php?action=update&amp;deal_id=$id",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	    );

  //  Quick Update
  $actions['deal']['quick_update'] = array (
    'Url'      => "$path/deal/deal_index.php?action=quick_update&amp;deal_id=$id",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	    );
					    
  // Parent Detail Consult
  $actions['deal']['parent_detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/deal/deal_index.php?action=parent_detailconsult&amp;parentdeal_id=$pid",
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('parent_detailupdate', 'parent_update') 
                                    	    );

  // Parent Detail Update
  $actions['deal']['parent_detailupdate'] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/deal/deal_index.php?action=parent_detailupdate&amp;parentdeal_id=$pid",
    'Right'    => $cright_write,
    'Condition'=> array ('parent_detailconsult', 'parent_update') 
                                     		  );
                                                                                                                                                             
  // Parent Update
  $actions['deal']['parent_update'] = array (
    'Url'      => "$path/deal/deal_index.php?action=parent_update&amp;parentdeal_id=$pid",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                                  );

  // Insert
  $actions['deal']['insert'] = array (
    'Url'      => "$path/deal/deal_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

  // Parent insert
  $actions['deal']['parent_insert'] = array (
    'Url'      => "$path/deal/deal_index.php?action=parent_insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                            );

  // Check Delete
  $actions['deal']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/deal/deal_index.php?action=check_delete&amp;deal_id=$id",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'update') 
                                     );

  // Delete
  $actions['deal']['delete'] = array (
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     );

  // Dashboard
  $actions['deal']['dashboard'] = array (
    'Name'     => $l_header_dashboard,
    'Url'      => "$path/deal/deal_index.php?action=dashboard",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                     );

  // Dashboard List
  $actions['deal']['dashboard_list'] = array (
    'Url'      => "$path/deal/deal_index.php?action=dashboard_list",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('None') 
                                     );

  // Document add
  $actions['deal']['document_add'] = array (
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
  );
  
  // Parent Delete
  $actions['deal']['parent_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/deal/deal_index.php?action=parent_delete&amp;parentdeal_id=$pid",
    'Right'    => $cright_write,
    'Condition'=> array ('parent_detailconsult') 
                                     	     );

  // Affect
  $actions['deal']['affect'] = array (
    'Url'      => "$path/deal/deal_index.php?action=affect&amp;parentdeal_id=$pid&amp;deal_id=$id",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	     );

  // Affect Update
  $actions['deal']['affect_update'] = array (
    'Url'      => "$path/deal/deal_index.php?action=affect_update&amp;sel_parent=$pid&amp;deal_id=$id",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	     );

  // Admin  
  $actions['deal']['admin'] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/deal/deal_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                    );

  // Type Insert
  $actions['deal']['type_insert'] = array (
    'Url'      => "$path/deal/deal_index.php?action=type_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                           );

  // Type Update
  $actions['deal']['type_update'] = array (
    'Url'      => "$path/deal/deal_index.php?action=type_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                           );

  // Type checklink
  $actions['deal']['type_checklink'] = array (
    'Url'      => "$path/deal/deal_index.php?action=type_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                             );

  // Type delete
  $actions['deal']['type_delete'] = array (
    'Url'      => "$path/deal/deal_index.php?action=type_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                          );

  // Status Insert 
  $actions['deal']['status_insert'] = array (
    'Url'      => "$path/deal/deal_index.php?action=status_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                          );

  // Status Update 
  $actions['deal']['status_update'] = array (
    'Url'      => "$path/deal/deal_index.php?action=status_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                          );

  // Status Checklink 
  $actions['deal']['status_checklink'] = array (
    'Url'      => "$path/deal/deal_index.php?action=status_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                          );

  // Status Delete 
  $actions['deal']['status_delete'] = array (
    'Url'      => "$path/deal/deal_index.php?action=status_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                          );

  // Role Insert
  $actions['deal']['role_insert'] = array (
    'Url'      => "$path/deal/deal_index.php?action=role_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                           );

  // Role Update
  $actions['deal']['role_update'] = array (
    'Url'      => "$path/deal/deal_index.php?action=role_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                           );

  // Role checklink
  $actions['deal']['role_checklink'] = array (
    'Url'      => "$path/deal/deal_index.php?action=role_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                             );

  // Role delete
  $actions['deal']['role_delete'] = array (
    'Url'      => "$path/deal/deal_index.php?action=role_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                          );

  // Display
  $actions['deal']['display'] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/deal/deal_index.php?action=display",
    'Right'    => $cright_read, 
    'Condition'=> array ('all') 
                                      );

  // Display Preference
  $actions['deal']['dispref_display'] = array (
    'Url'      => "$path/deal/deal_index.php?action=dispref_display",
    'Right'    => $cright_read, 
    'Condition'=> array ('None') 
                                      );

  // Display Level
  $actions['deal']['dispref_level'] = array (
    'Url'      => "$path/deal/deal_index.php?action=dispref_level",
    'Right'    => $cright_read, 
    'Condition'=> array ('None') 
                                      );

  // Category Select 
  $actions['deal']['ext_get_category1_ids']  = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

  // Category Check Link
  $actions['deal']['category1_checklink'] = array (
    'Url'      => "$path/contact/deal_index.php?action=category1_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                    );                

// Category Update
  $actions['deal']['category1_update'] = array (
    'Url'      => "$path/contact/deal_index.php?action=category1_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Category Insert
  $actions['deal']['category1_insert'] = array (
    'Url'      => "$path/contact/deal_index.php?action=category1_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Category Delete
  $actions['deal']['category1_delete'] = array (
    'Url'      => "$path/contact/deal_index.php?action=category1_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

  // External call : select one deal
  $actions['deal']['ext_get_id'] = array (
    'Url'      => "$path/deal/deal_index.php?action=ext_get_id",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     );
				     
}


///////////////////////////////////////////////////////////////////////////////
// Deal Actions updates (after processing, before displaying menu)  
///////////////////////////////////////////////////////////////////////////////
function update_deal_action() {
  global $params, $actions, $path;

  $id = $params['deal_id'];
  if ($id > 0) {
    // Detail Consult
    $actions['deal']['detailconsult']['Url'] = "$path/deal/deal_index.php?action=detailconsult&amp;deal_id=$id";
    $actions['deal']['detailonsult']['Condition'][] = 'insert';

    // Detail Update
    $actions['deal']['detailupdate']['Url'] = "$path/deal/deal_index.php?action=detailupdate&amp;deal_id=$id";
    $actions['deal']['detailupdate']['Condition'][] = 'insert';
    
    // Quick Detail
    $actions['deal']['quick_detail']['Url'] = "$path/deal/deal_index.php?action=quick_detail&amp;deal_id=$id";
    $actions['deal']['quick_detail']['Condition'][] = 'insert';

    // Check Delete
    $actions['deal']['check_delete']['Url'] = "$path/deal/deal_index.php?action=check_delete&amp;deal_id=$id";
    $actions['deal']['check_delete']['Condition'][] = 'insert';
  }

  $pid = $params['parentdeal_id'];
  if ($pid > 0) {
    // Parent Detail Update
    $actions['deal']['parent_detailupdate']['Url'] = "$path/deal/deal_index.php?action=parent_detailupdate&amp;parentdeal_id=$pid";

    // Parent Check Delete
    $actions['deal']['parent_delete']['Url'] = "$path/deal/deal_index.php?action=parent_delete&amp;parentdeal_id=$pid";
  }

}

?>
