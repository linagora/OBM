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
// OBM - File : resource_index.php                                           //
//     - Desc :  Resource Index File                                         //
// 2005-08-13 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields      -- show the resource search form
// - search          -- search fields      -- show the result set of search
// - new             --                    -- show the new resource form
// - detailconsult   -- $param_resource    -- show the resource detail
// - detailupdate    -- $param_resource    -- show the resource detail form
// - insert          -- form fields        -- insert the resource 
// - update          -- form fields        -- update the resource 
// - check_delete    -- $param_resource    -- check links before delete
// - delete          -- $param_resource    -- delete the resource 
// - rights_admin    -- access rights screen
// - rights_update   -- Update resource calendar access rights
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position
// External API ---------------------------------------------------------------
// - ext_get_ids     --                -- select multiple resources (ret id) 
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'resource';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include_once("$obminclude/global.inc");
$params = get_resource_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include_once("$obminclude/global_pref.inc");
require_once('resource_display.inc');
require_once('resource_query.inc');
require_once('resource_js.inc');
require_once("$obminclude/of/of_right.inc");
include_once("$obminclude/of/of_category.inc");

get_resource_action();

// If user has individual admin right on the selected resource,	give access
// if user does not have admin right on module, check for the resource right
if (($params['resource_id'] > 0)
  && (! $perm->check_right('resource', $cright_write_admin))) {
  if (OBM_Acl::canAdmin($obm['uid'], 'resource', $params['resource_id'])) {
    $actions['resource']['rights_admin']['Right'] = $cright_read;
    $actions['resource']['rights_update']['Right'] = $cright_read;
  }
}

$perm->check_permissions($module, $action);
if (! check_privacy($module, 'Resource', $action, $params['resource_id'], $obm['uid'])) {
  $display['msg'] = display_err_msg($l_error_visibility);
  $action = 'index';
} else {
  update_last_visit('resource', $params['resource_id'], $action);
}
page_close();


///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == 'ext_get_ids') {
  $display['search'] = html_resource_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_resource_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} elseif ($action == 'ext_ritem') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= dis_ritem_popup($params);

} elseif ($action == 'index' || $action == '') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = html_resource_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_resource_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} elseif ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = html_resource_search_form($params);
  $display['result'] = dis_resource_search_list($params);

} elseif ($action == 'ext_search') {
///////////////////////////////////////////////////////////////////////////////
  $res_q = run_query_resource_ext_search($params);
  json_search_resources($params, $res_q);
  echo '('.$display['json'].')';
  exit();

} elseif ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = html_resource_form('',$params);

} elseif ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_resource_consult($params);

} elseif ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  if (check_resource_update_rights($params)) {
    $obm_q = run_query_resource_detail($params['resource_id']);
    if ($obm_q->num_rows() == 1) {
      $display['detailInfo'] = display_record_info($obm_q);
      $display['detail'] = html_resource_form($obm_q, $params);
    } else {
      $display['msg'] .= display_err_msg($l_err_reference);
    }
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
  }

} elseif ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_resource_data_form('', $params)) {
    // If the context (same resource) was confirmed ok, we proceed
    if (is_array($params['accept_admin'])) {
      $params['accept_write'] = $params['accept_admin'];
      $params['accept_read'] = $params['accept_admin'];
    }
    $rid = run_query_resource_insert($params);
    if ($rid > 0) {
      $params['resource_id'] = $rid;
      $display['msg'] .= display_ok_msg("$l_resource : $l_insert_ok");
      $display['detail'] = dis_resource_consult($params);
    } else {
      $display['msg'] .= display_err_msg("$l_resource : $l_insert_error");
      $display['search'] = html_resource_search_form($params);
    }
  // Form data are not valid
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['detail'] = html_resource_form('', $params);
  }

} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_resource_data_form($params['resource_id'], $params)) {
    $retour = run_query_resource_update($params['resource_id'], $params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_resource : $l_update_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_resource : $l_update_error");
    }
    $display['detail'] = dis_resource_consult($params);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['detail'] = html_resource_form('', $params);
  }

} elseif ($action == 'check_delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_resource_update_rights($params)) {
    if (check_resource_can_delete($params)) {
      $display['msg'] .= display_info_msg($ok_msg, false);
      $display['detail'] = dis_resource_can_delete($params['resource_id']);
    } else {
      $display['msg'] .= display_warn_msg($err['msg'], false);
      $display['msg'] .= display_warn_msg($l_cant_delete, false);
      $display['detail'] = dis_resource_consult($params);
    }
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
  }

} elseif ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_resource_can_delete($params)) {
    $retour = run_query_resource_delete($params['resource_id']);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_resource : $l_delete_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_resource : $l_delete_error");
    }
    $display['search'] = html_resource_search_form($params);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_resource_consult($params);
  }

} elseif ($action == 'rights_admin') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_resource_right_dis_admin($params['entity_id']);

} elseif ($action == 'rights_update') {
///////////////////////////////////////////////////////////////////////////////
  if (OBM_Acl_Utils::updateRights('resource', $params['entity_id'], $obm['uid'], $params)) {
    $display['msg'] .= display_ok_msg($l_right_update_ok);
  } else {
    $display['msg'] .= display_warn_msg($l_of_right_err_auth);
  }
  $display['detail'] = dis_resource_right_dis_admin($params['entity_id']);

}  elseif ($action == 'admin') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_resource_admin_index($params);

} elseif ($action == 'rtype_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_resource_rtype_insert($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_rtype : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_rtype : $l_insert_error");
  }
  $display['detail'] .= dis_resource_admin_index($params);

} elseif ($action == 'rtype_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_resource_rtype_update($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_rtype : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_rtype : $l_update_error");
  }
  $display['detail'] .= dis_resource_admin_index($params);

} elseif ($action == 'rtype_checklink') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= dis_resource_links($params, 'rtype');

} elseif ($action == 'rtype_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_resource_rtype_delete($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_rtype : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_rtype : $l_delete_error");
  }
  $display['detail'] .= dis_resource_admin_index($params);

} elseif ($action == 'ritem_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_resource_ritem_insert($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_ritem : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_ritem : $l_insert_error");
  }
  $display['detail'] .= dis_resource_admin_index($params);

} elseif ($action == 'ritem_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_resource_ritem_update($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_ritem : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_ritem : $l_update_error");
  }
  $display['detail'] .= dis_resource_admin_index($params);

}  elseif ($action == 'ritem_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_resource_ritem_delete($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_ritem : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_ritem : $l_delete_error");
  }
  $display['detail'] .= dis_resource_admin_index($params);

} elseif ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'], 'resource', 1);
  $display['detail'] = dis_resource_display_pref($prefs);

} else if ($action == 'dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'resource', 1);
  $display['detail'] = dis_resource_display_pref($prefs);

} else if ($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'resource', 1);
  $display['detail'] = dis_resource_display_pref($prefs);
}

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_resource);
if (! $params['popup']) {
  update_resource_action();
  $display['header'] = display_menu($module);
}
$display['end'] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Resource parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_resource_params() {
  
  // Get global params
  $params = get_global_params('Resource');

  //Get resource specific params
  if (isset ($params['ext_id'])) $params['resource_id'] = $params['ext_id'];
  if ((isset ($params['entity_id'])) && (! isset($params['resource_id']))) {
    $params['resource_id'] = $params['entity_id'];
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// User Action 
///////////////////////////////////////////////////////////////////////////////
function get_resource_action() {
  global $params, $actions, $path;
  global $l_header_find,$l_header_new_f,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display,$l_header_admin,$l_header_reset,$l_header_right;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin, $l_header_admin;
  global $cright_none;

// Index
  $actions['resource']['index'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/resource/resource_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

// Get Ids
  $actions['resource']['ext_get_ids'] = array (
    'Url'      => "$path/resource/resource_index.php?action=ext_get_ids",
    'Right'    => $cright_none,
    'Condition'=> array ('none'),
    'popup' => 1
                                    );

// New
  $actions['resource']['new'] = array (
    'Name'     => $l_header_new_f,
    'Url'      => "$path/resource/resource_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
                                  );

// Search
  $actions['resource']['search'] = array (
    'Url'      => "$path/resource/resource_index.php?action=search",
    'Right'    => $cright_none,
    'Condition'=> array ('None') 
  );

  $actions['resource']['ext_search'] = array (
    'Url'      => "$path/resource/resource_index.php?action=ext_search",
    'Right'    => $cright_none,
    'Condition'=> array ('None')

  );  

  // Get resource id from external window (js)
  $actions['resource']['getsearch'] = array (
    'Url'      => "$path/resource/resource_index.php?action=search",
    'Right'    => $cright_none,
    'Condition'=> array ('None') 
                                  );
// Detail Consult
  $actions['resource']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/resource/resource_index.php?action=detailconsult&amp;resource_id=".$params['resource_id'],
    'Right'    => $cright_read,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'rights_admin', 'rights_update') 
                                  );
// Detail Update
  $actions['resource']['detailupdate'] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/resource/resource_index.php?action=detailupdate&amp;resource_id=".$params['resource_id'],
     'Right'    => $cright_write,
     'Condition'=> array ('detailconsult', 'insert', 'update')
                                     	   );

// Insert
  $actions['resource']['insert'] = array (
    'Url'      => "$path/resource/resource_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Update
  $actions['resource']['update'] = array (
    'Url'      => "$path/resource/resource_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Check Delete
  $actions['resource']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/resource/resource_index.php?action=check_delete&amp;resource_id=".$params['resource_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'insert')
                                     	      );

// Delete
  $actions['resource']['delete'] = array (
    'Url'      => "$path/resource/resource_index.php?action=delete&amp;resource_id=".$params['resource_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Rights Admin.
  $actions['resource']['rights_admin'] = array (
    'Name'     => $l_header_right,
    'Url'      => "$path/resource/resource_index.php?action=rights_admin&amp;entity_id=".$params['resource_id'],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult','rights_update','rights_admin','detailupdate','update')
                                     );

// Rights Update
  $actions['resource']['rights_update'] = array (
    'Url'      => "$path/resource/resource_index.php?action=rights_update&amp;entity_id=".$params['resource_id'],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                     );

// Display
  $actions['resource']['display'] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/resource/resource_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                         );
// Display
  $actions['resource']['dispref_display'] = array (
    'Url'      => "$path/resource/resource_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                         );
// Display
  $actions['resource']['dispref_level'] = array (
    'Url'      => "$path/resource/resource_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                         );
// Admin
  $actions['resource']['admin'] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/resource/resource_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                       );

// Resource Type Insert
  $actions['resource']['rtype_insert'] = array (
    'Url'      => "$path/resource/resource_index.php?action=rtype_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Resource Type Update
  $actions['resource']['rtype_update'] = array (
    'Url'      => "$path/resource/resource_index.php?action=rtype_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Resource Type Check Link
  $actions['resource']['rtype_checklink'] = array (
    'Url'      => "$path/resource/resource_index.php?action=rtype_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Resource Type Delete
  $actions['resource']['rtype_delete'] = array (
    'Url'      => "$path/resource/resource_index.php?action=rtype_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Resource Item Insert
  $actions['resource']['ritem_insert'] = array (
    'Url'      => "$path/resource/resource_index.php?action=ritem_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Resource Item Update
  $actions['resource']['ritem_update'] = array (
    'Url'      => "$path/resource/resource_index.php?action=ritem_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Resource Item Delete
  $actions['resource']['ritem_delete'] = array (
    'Url'      => "$path/resource/resource_index.php?action=ritem_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );
// Add Resource Item
  $actions['resource']['ext_ritem'] = array (
    'Url'      => "$path/resource/resource_index.php?action=ext_ritem",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     	       );

}


///////////////////////////////////////////////////////////////////////////////
// Resource Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_resource_action() {
  global $params, $actions, $path;

  $id = $params["resource_id"];
  if ($id > 0) {
    $actions["resource"]["rights_admin"]['Condition'][] = 'insert';
  }
  // Detail Consult
  $actions['resource']['detailconsult']['Url'] = "$path/resource/resource_index.php?action=detailconsult&amp;resource_id=".$params['resource_id'];

  if (check_resource_update_rights($params)) {
    // Detail Update
    $actions['resource']['detailupdate']['Url'] = "$path/resource/resource_index.php?action=detailupdate&amp;resource_id=".$params['resource_id'];

    // Check Delete
    $actions['resource']['check_delete']['Url'] = "$path/resource/resource_index.php?action=check_delete&amp;resource_id=".$params['resource_id'];

    // Right admin
    $actions['resource']['rights_admin']['Url'] = "$path/resource/resource_index.php?action=rights_admin&amp;entity_id=".$params['resource_id'];

  } else {
    $actions['resource']['detailupdate']['Condition'] = array('None');
    $actions['resource']['check_delete']['Condition'] = array('None');
    $actions['resource']['rights_admin']['Condition'] = array('None');
  }
}

?>
