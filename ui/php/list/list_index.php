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
// OBM - File : list_index.php                                               //
//     - Desc : List Index File                                              //
// 1999-03-19 - Aliacom                                                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the list search form
// - search          -- search fields  -- show the result set of search
// - new             --                -- show the new list form
// - detailconsult   -- $param_list    -- show the list detail
// - detailupdate    -- $param_list    -- show the list detail form
// - detailduplicate -- $param_list    -- show the list detail form
// - insert          -- form fields    -- insert the list
// - update          -- form fields    -- update the list
// - delete          -- $param_list    -- delete the list
// - contact_add     -- 
// - contact_del     -- 
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
// External API ---------------------------------------------------------------
// - ext_get_ids     --                -- select multiple lists (return id) 
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'list';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
require("$obminclude/global.inc");
$params = get_list_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
require("$obminclude/global_pref.inc");

require_once("$obminclude/of/of_select.inc");
include('list_display.inc');
include('list_query.inc');
include("$obminclude/of/of_category.inc");

get_list_action();
$perm->check_permissions($module, $action);
if (! check_privacy($module, 'List', $action, $params['list_id'], $obm['uid'])) {
  $display['msg'] = display_err_msg($l_error_visibility);
  $action = 'index';
} else {
  update_last_visit('list', $params['list_id'], $action);
}

page_close();

require('list_js.inc');

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == 'ext_get_id') {
  $display['search'] = dis_list_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['detail'] = dis_list_search_list($params, $popup);
  } else {
    $display['msg'] .= display_ok_msg($l_no_display);
  }

}

else if ($action == 'new_criterion') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_list_add_criterion_form($params);

} elseif (($action == 'index') || ($action == '')) {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_list_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_list_search_list('', $popup);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} else if ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_list_search_form($params);
  $display['result'] = dis_list_search_list($params, $popup);

} else if ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_list_form($action, '', $params);

} else if ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_list_consult($params);

} else if ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  $params_q = run_query_list_detail($params['list_id']);
  $display['detail'] = dis_list_form($action, $params_q, $params);

} else if ($action == 'detailduplicate') {
///////////////////////////////////////////////////////////////////////////////
  $params_q = run_query_list_detail($params['list_id']);
  $params['id_duplicated'] = $params['list_id'];
  $params['list_id'] = '';
  $params['name'] = $params_q->f('list_name') . " - $l_aduplicate";
  $display['detail'] = dis_list_form($action, $params_q, $params);

} else if ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  if ($params['criteria'] != '') {
    $dynlist = make_list_query_from_criteria($params);
    $params['query'] = $dynlist['query'];
  } else {
    // To change : we do not know if expert mode (query should be stripslashed)
    // or no more graphical criteria (query should be set to empty)
    $params['query'] = stripslashes($params['query']);
  }
  if (check_user_defined_rules() && check_list_data('', $params)) {
    // If the context (same list) was confirmed ok, we proceed
    if ($params['confirm'] == $c_yes) {
      $params['list_id'] = run_query_list_insert($params);
      if ($params['list_id'] > 0) {
        $display['msg'] .= display_ok_msg("$l_list : $l_insert_ok");
      } else {
        $display['msg'] .= display_err_msg("$l_list : $l_insert_error");
      }
      $display['detail'] = dis_list_consult($params);

    // If it is the first try, we warn the user if some lists seem similar
    } else {
      $obm_q = check_list_context('', $params);
      if ($obm_q->num_rows() > 0) {
        $display['detail'] = dis_list_warn_insert($obm_q, $params);
      } else {
	$params['list_id'] = run_query_list_insert($params);
        if ($params['list_id'] > 0) {
          $display['msg'] .= display_ok_msg("$l_list : $l_insert_ok");
        } else {
          $display['msg'] .= display_err_msg("$l_list : $l_insert_error");
        }
	$display['detail'] = dis_list_consult($params);
      }
    }

  // Form data are not valid
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
    $display['detail'] = dis_list_form($action, '', $params);
  }

} elseif ($action == 'update')  {
///////////////////////////////////////////////////////////////////////////////
  if ($params['criteria'] != '') {
    $dynlist = make_list_query_from_criteria($params);
    $params['query'] = $dynlist['query'];
  } else {
    // To change : we do not know if expert mode (query should be stripslashed)
    // or no more graphical criteria (query should be set to empty)
    $params['query'] = stripslashes($params['query']);
  }
  if (check_user_defined_rules() && check_list_data($params['list_id'], $params)) {
    $retour = run_query_list_update($params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_list : $l_update_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_list : $l_update_error");
    }
    $display['detail'] = dis_list_consult($params);
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
    $params_q = run_query_list_detail($params['list_id']);
    $display['detail'] = dis_list_form($action, $params_q, $params);
  }

} elseif ($action == 'check_delete')  {
///////////////////////////////////////////////////////////////////////////////
  if (check_list_can_delete($params['list_id'])) {
    $display['msg'] .= display_info_msg($ok_msg, false);
    $display['detail'] = dis_list_can_delete($params['list_id']);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_list_consult($params);
  }

} elseif ($action == 'delete')  {
///////////////////////////////////////////////////////////////////////////////
  if (check_list_can_delete($params['list_id'])) {
    $retour = run_query_list_delete($params['list_id']);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_list : $l_delete_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_list : $l_delete_error");
    }
    $display['search'] = dis_list_search_form('');
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_list_consult($params);
  }

} elseif ($action == 'contact_add')  {
///////////////////////////////////////////////////////////////////////////////
  if ($params['con_nb'] > 0) {
    $nb = run_query_list_contactlist_insert($params);
    run_query_list_update_sql($params['list_id']);
    run_query_list_update_static_nb($params['list_id']);
    $display['msg'] .= display_ok_msg("$nb $l_contact_added");
  } else {
    $display['msg'] .= display_err_msg('no contact to add');
  }
  $display['detail'] = dis_list_consult($params);

} elseif ($action == 'contact_del')  {
///////////////////////////////////////////////////////////////////////////////
  if ($params['con_nb'] > 0) {
    $nb = run_query_list_contactlist_delete($params);
    run_query_list_update_sql($params['list_id']);
    run_query_list_update_static_nb($params['list_id']);
    $display['msg'] .= display_ok_msg("$nb $l_contact_removed");
  } else {
    $display['msg'] .= display_err_msg('no contact to delete');
  }
  $display['detail'] = dis_list_consult($params);

} else if ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'], 'list', 1);
  $prefs_con = get_display_pref($obm['uid'], 'list_contact', 1);
  $display['detail'] = dis_list_display_pref($prefs, $prefs_con);

} else if ($action == 'dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'list', 1);
  $prefs_con = get_display_pref($obm['uid'], 'list_contact', 1);
  $display['detail'] = dis_list_display_pref($prefs, $prefs_con);

} else if($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'list', 1);
  $prefs_con = get_display_pref($obm['uid'], 'list_contact', 1);
  $display['detail'] = dis_list_display_pref($prefs, $prefs_con);

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
} else if ($action == 'ext_get_ids') {
  $display['search'] = dis_list_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['detail'] = dis_list_search_list($params, $popup);
  } else {
    $display['msg'] .= display_ok_msg($l_no_display);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
// Update actions url in case some values have been updated (id after insert) 
update_list_action();
$display['head'] = display_head($l_list);
$display['end'] = display_end();
if (! $params['popup']) {
  $display['header'] = display_menu($module);
}

display_page($display);
exit(0);


///////////////////////////////////////////////////////////////////////////////
// Stores in $params hash, List parameters transmited
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_list_params() {
  global $cgp_user;
  
  // Get global params
  $params = get_global_params('List');
   
  // Get List specific params
  if (isset ($params['ext_id'])) $params['list_id'] = $params['ext_id'];
  
  // Criteria params :
  // Company
  if (isset ($params['company_name'])) $params['criteria']['modules']['company']['company_name'] = $params['company_name'];
  if (isset ($params['company_country_iso3166'])) $params['criteria']['modules']['company']['company_country_iso3166'] = $params['company_country_iso3166'];
  if (isset ($params['company_timeafter'])) $params['criteria']['modules']['company']['company_timeafter'] = $params['company_timeafter']; 
  if (isset ($params['company_timebefore'])) $params['criteria']['modules']['company']['company_timebefore'] = $params['company_timebefore'];
  if (isset ($params['company_zipcode'])) $params['criteria']['modules']['company']['company_zipcode'] = $params['company_zipcode'];
  if (isset ($params['company_marketingmanager_id'])) $params['criteria']['modules']['company']['company_marketingmanager_id'] = $params['company_marketingmanager_id'];
  if (isset ($params['company_town'])) $params['criteria']['modules']['company']['company_town'] = $params['company_town'];
  if (isset ($params['company_datasource_id'])) $params['criteria']['modules']['company']['company_datasource_id'] = $params['company_datasource_id'];

  // User data categories handling
  if (is_array($cgp_user['company']['category'])) {
    foreach($cgp_user['company']['category'] as $cat_name => $one_cat) {
      $cat_id = "${cat_name}_id";
      $cat_tree = "${cat_name}_tree";
      if (isset ($params[$cat_id])) $params['criteria']['modules']['company'][$cat_id] = $params[$cat_id];
      if (isset ($params[$cat_name])) $params['criteria']['modules']['company'][$cat_name] = $params[$cat_name];
      if (isset ($params[$cat_tree])) $params['criteria']['modules']['company'][$cat_tree] = $params[$cat_tree];
    }
  }

  // Contact
  if (isset ($params['contact_lastname'])) $params['criteria']['modules']['contact']['contact_lastname'] = $params['contact_lastname'];
  if (isset ($params['contact_firstname'])) $params['criteria']['modules']['contact']['contact_firstname'] = $params['contact_firstname'];
  if (isset ($params['contact_country_iso3166'])) $params['criteria']['modules']['contact']['contact_country_iso3166'] = $params['contact_country_iso3166'];
  if (isset ($params['contact_timeafter'])) $params['criteria']['modules']['contact']['contact_timeafter'] = $params['contact_timeafter'];
  if (isset ($params['contact_timebefore'])) $params['criteria']['modules']['contact']['contact_timebefore'] = $params['contact_timebefore'];
  if (isset ($params['contact_marketingmanager_id'])) $params['criteria']['modules']['contact']['contact_marketingmanager_id'] = $params['contact_marketingmanager_id'];
  if (isset ($params['contact_datasource_id'])) $params['criteria']['modules']['contact']['contact_datasource_id'] = $params['contact_datasource_id'];
  if (isset ($params['contact_town'])) $params['criteria']['modules']['contact']['contact_town'] = $params['contact_town'];
  if (isset ($params['contact_zipcode'])) $params['criteria']['modules']['contact']['contact_zipcode'] = $params['contact_zipcode'];
  if (isset ($params['contact_function_id'])) $params['criteria']['modules']['contact']['contact_function_id'] = $params['contact_function_id'];
  if (isset ($params['kind_lang'])) $params['criteria']['modules']['contact']['kind_lang'] = $params['kind_lang'];

  // User data categories handling
  if (is_array($cgp_user['contact']['category'])) {
    foreach($cgp_user['contact']['category'] as $cat_name => $one_cat) {
      $cat_id = "${cat_name}_id";
      $cat_tree = "${cat_name}_tree";
      if (isset ($params[$cat_id])) $params['criteria']['modules']['contact'][$cat_id] = $params[$cat_id];
      if (isset ($params[$cat_name])) $params['criteria']['modules']['contact'][$cat_name] = $params[$cat_name];
      if (isset ($params[$cat_tree])) $params['criteria']['modules']['contact'][$cat_tree] = $params[$cat_tree];
    }
  }

  // Publication
  if (isset ($params['subscription_publication_id'])) $params['criteria']['modules']['publication']['subscription_publication_id'] = $params['subscription_publication_id'];
  if (isset ($params['publication_lang'])) $params['criteria']['modules']['publication']['publication_lang'] = $params['publication_lang'];
  if (isset ($params['publication_year'])) $params['criteria']['modules']['publication']['publication_year'] = $params['publication_year'];
  if (isset ($params['subscription_reception_id'])) $params['criteria']['modules']['publication']['subscription_reception_id'] = $params['subscription_reception_id'];
  if (isset ($params['subscription_renewal'])) $params['criteria']['modules']['publication']['subscription_renewal'] = $params['subscription_renewal'];
  if (isset ($params['subscription_timeafter'])) $params['criteria']['modules']['publication']['subscription_timeafter'] = $params['subscription_timeafter'];
  if (isset ($params['subscription_timebefore'])) $params['criteria']['modules']['publication']['subscription_timebefore'] = $params['subscription_timebefore'];

  if (isset ($params['log_not'])) $params['criteria']['logical']['NOT'] = $params['log_not'];
  if (isset ($params['log_and'])) $params['criteria']['logical']['AND'] = $params['log_and'];
  
  if (isset ($params['se_criteria'])) {
    $params['criteria'] = unserialize(urldecode($params['se_criteria']));
  }

  $nb_con = 0;
  $nb_list = 0;
  $params['static_con'] = array();
  foreach($_REQUEST as $key => $value ) {
    if (strcmp(substr($key, 0, 6),'cb_con') == 0) {
      $con_num = intval(substr($key, 6));
      if ($con_num > 0) {
        $nb_con++;
        $params['static_con'][] = $con_num;
      }
    }
  }
  $params['con_nb'] = $nb_con;
  $params['list_nb'] = $nb_list;

  return $params;
}


//////////////////////////////////////////////////////////////////////////////
// list actions
//////////////////////////////////////////////////////////////////////////////
function get_list_action() {
  global $params, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_list,$l_header_display, $l_header_duplicate;
  global $l_header_consult, $l_header_add_contact;
  global $l_select_list, $l_add_contact,$l_list_wizard;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions['list']['index'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/list/list_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

// Search
  $actions['list']['search'] = array (
    'Url'      => "$path/list/list_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      );

// New
  $actions['list']['new'] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/list/list_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
                                  );

// New
  $actions['list']['new_criterion'] = array (
    'Url'      => "$path/list/list_index.php?action=new_criterion",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                  );				  

  // Detail Consult
  $actions['list']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/list/list_index.php?action=detailconsult&amp;list_id=".$params['list_id'],
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('detailduplicate', 'detailupdate') 
                                      );

  // Detail Duplicate
  $actions['list']['detailduplicate'] = array (
    'Name'     => $l_header_duplicate,
    'Url'      => "$path/list/list_index.php?action=detailduplicate&amp;list_id=".$params['list_id'],
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailupdate', 'contact_add','contact_del', 'update')
                                      );

// Detail Update
  $actions['list']['detailupdate'] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/list/list_index.php?action=detailupdate&amp;list_id=".$params['list_id'],
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailduplicate', 'contact_add','contact_del', 'update')
                                           );

// Insert
  $actions['list']['insert'] = array (
    'Url'      => "$path/list/list_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                      );

// Update
  $actions['list']['update'] = array (
    'Url'      => "$path/list/list_index.php?action=update",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                      );

// Check Delete
  $actions['list']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/list/list_index.php?action=check_delete&amp;list_id=".$params['list_id'],
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult','detailupdate','contact_add','contact_del') 
                                           );

// Delete
  $actions['list']['delete'] = array (
    'Url'      => "$path/list/list_index.php?action=delete",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                      );

// Sel list contacts : Contacts selection
  $actions['list']['sel_list_contact'] = array (
    'Name'     => $l_header_add_contact,
    'Url'      => "$path/contact/contact_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_contact)."&amp;ext_action=contact_add&amp;ext_url=".urlencode($path.'/list/list_index.php')."&amp;ext_id=".$params['list_id']."&amp;ext_target=$l_list",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Target'   => $l_list,
    'Condition'=> array ('detailconsult','update','contact_add','contact_del')
                                          );

// Contact ADD
  $actions['list']['contact_add'] = array (
    'Url'      => "$path/list/list_index.php?action=contact_add",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                          );
// Contact Del
  $actions['list']['contact_del'] = array (
    'Url'      => "$path/list/list_index.php?action=contact_del",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                          );

// Display
  $actions['list']['display'] = array (
   'Name'     => $l_header_display,
   'Url'      => "$path/list/list_index.php?action=display",
   'Right'    => $cright_read,
   'Condition'=> array ('all') 
                                      );

// Display Preference
  $actions['list']['dispref_display'] = array (
   'Url'      => "$path/list/list_index.php?action=dispref_display",
   'Right'    => $cright_read,
   'Condition'=> array ('None') 
                                               );

// Display level
  $actions['list']['dispref_level'] = array (
   'Url'      => "$path/list/list_index.php?action=dispref_level",
   'Right'    => $cright_read,
   'Condition'=> array ('None') 
                                            );

// External List Select 
  $actions['list']['ext_get_id']  = array (
    'Url'      => "$path/list/list_index.php?action=ext_get_id",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );
// External List Select 
  $actions['list']['ext_get_ids']  = array (
    'Url'      => "$path/list/list_index.php?action=ext_get_ids",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );
					    
}


///////////////////////////////////////////////////////////////////////////////
// List Actions updates (after processing, before displaying menu)  
///////////////////////////////////////////////////////////////////////////////
function update_list_action() {
  global $params, $actions, $path, $l_add_contact, $l_list;

  $id = $params['list_id'];
  if ($id > 0) {
    // Detail Consult
    $actions['list']['detailconsult']['Url'] = "$path/list/list_index.php?action=detailconsult&amp;list_id=$id";
    $actions['list']['detailconsult']['Condition'][] = 'insert';

    // Detail Update
    $actions['list']['detailupdate']['Url'] = "$path/list/list_index.php?action=detailupdate&amp;list_id=$id";
    $actions['list']['detailupdate']['Condition'][] = 'insert';

    // Check Delete
    $actions['list']['check_delete']['Url'] = "$path/list/list_index.php?action=check_delete&amp;list_id=$id";
    $actions['list']['check_delete']['Condition'][] = 'insert';

    // Contact selection
    $actions['list']['sel_list_contact']['Url'] = "$path/contact/contact_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_contact)."&amp;ext_action=contact_add&amp;ext_url=".urlencode($path.'/list/list_index.php')."&amp;ext_id=$id&amp;ext_target=$l_list";
    $actions['list']['sel_list_contact']['Condition'][] = 'insert';
  }

}

?>
