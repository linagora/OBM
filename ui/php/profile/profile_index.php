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
// OBM - File : profile_index.php                                            //
//     - Desc : Profile Index File                                           //
// 2008-09-10 Christophe LIOU KEE ON                                         //
///////////////////////////////////////////////////////////////////////////////
// $Id: profile_index.php,v 1.78 2007/02/19 14:32:51 mehdi Exp $ //
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'profile';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_profile_params();

page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");

$extra_js_include[] = 'profile.js';

require('profile_display.inc');
require('profile_query.inc');
require('profile_js.inc');
include("$obminclude/of/of_category.inc");
$params = get_profile_params();

get_profile_action();
$perm->check_permissions($module, $action);

//update_last_visit('profile', $params['profile_id'], $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if ($action == 'index' || $action == '') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_profile_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_profile_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} elseif ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_profile_search_form($params);
  $display['result'] = dis_profile_search_list($params);
  
} elseif ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  foreach($c_profile_properties as $property) {
    $profile['property'][$property] = false;
  }
  $display['detail'] = html_profile_form($profile);

} elseif ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  if(check_user_defined_rules() && check_profile_data_form($params)) {
    $params['profile_id'] = run_query_profile_insert($params);
    if($params['profile_id'] > 0) {
      $display['msg'] .= display_ok_msg("$l_profile : $l_insert_ok");
      $profile = run_query_profile_details($params['profile_id']);
      $display['detail'] = html_profile_consult($profile);         
    } else {
      $display['msg'] .= display_err_msg("$l_profile : $l_insert_error");
      $display['detail'] = html_profile_form($params);
    }
  } else {
    $display['msg'] = display_warn_msg($l_invalid_data . " : " . $err['msg']);
    $display['detail'] = html_profile_form($params);
  }
} elseif ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $profile = run_query_profile_details($params['profile_id']);
  $display['detail'] = html_profile_consult($profile);

} elseif ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  $profile = run_query_profile_details($params['profile_id']);
  $display['detail'] = html_profile_form($profile);

} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  if(check_user_defined_rules() && check_profile_data_form($params)) {
    if (run_query_profile_update($params)) {
      $display['msg'] .= display_ok_msg("$l_profile : $l_update_ok");
      $profile = run_query_profile_details($params['profile_id']);
      $display['detail'] = html_profile_consult($profile);      
    } else {
      $display['msg'] .= display_err_msg("$l_profile : $l_update_error");
      $display['detail'] = html_profile_form($params);
    }
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . " : " . $err['msg']);
    $display['detail'] = html_profile_form($params);
  }
  
} elseif ($action == 'check_delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_profile($params['profile_id'])) {
    $retour = run_query_profile_delete($params['profile_id']);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_profile : $l_delete_ok");
      $display['search'] = dis_profile_search_form($params);
    } else {
      $display['msg'] .= display_err_msg("$l_profile : $l_delete_error");
      $display['search'] = dis_profile_search_form($params);
    }    
  } else {
    $display['msg'] .= display_warn_msg($l_profile_delete_warning);
    $profile = run_query_profile_details($params['profile_id']);
    $display['detail'] = html_profile_consult($profile);      
  }

} elseif ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'], 'profile', 1);
  $display['detail'] = dis_profile_display_pref($prefs);

} else if ($action == 'dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($obm['uid'], 'profile', 1);
  $display['detail'] = dis_profile_display_pref($prefs);

} else if ($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($obm['uid'], 'profile', 1);
  $display['detail'] = dis_profile_display_pref($prefs);
}

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_profile);
if (! $params['popup']) {
  update_profile_action();
  $display['header'] = display_menu($module);
}
$display['end'] = display_end();
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores User parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_profile_params() {
  // Get global params
  $params = get_global_params('profile');
  
  $action = $params['action'];
  if(is_array($params['rights'])) {
    foreach($params['rights'] as $module => $rights) {
      $params['module'][$module] = 0;
      foreach($rights as $right) {
        $params['module'][$module] = $params['module'][$module] | ($right);
      }
    } 
  }
  if(is_array($params['enabled'])) {
    foreach($params['enabled'] as $section => $show) {
      $params['section'][$section] = $show;
    } 
  }  
  if(is_array($params['property'])) {
    foreach($params['property'] as $property => $value) {
      if(!is_array($value) && count(explode(',', $value)) > 1) {
        $params['property'][$property] = explode(',', $value);
      }
    }
  }
  if(is_array($property['default_right'])) {

  }
  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// User Action 
///////////////////////////////////////////////////////////////////////////////
function get_profile_action() {
  global $params, $actions, $path, $view;
  global $l_header_find, $l_header_new, $l_header_display, $l_header_consult, $l_header_properties;
  global $l_header_update, $l_header_delete, $l_ldif_export, $l_svg_export, $l_pdf_export;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

  $id = $params['profile_id'];
  $display_id = $params['display_id'];

  // Index
  $actions['profile']['index'] = array (
  	'Name'     => $l_header_find,
  	'Url'      => "$path/profile/profile_index.php?action=index",
  	'Right'    => $cright_read,
  	'Condition'=> array ('all') );
  
// Search
  $actions['profile']['search'] = array (
  	'Url'      => "$path/profile/profile_index.php?action=search",
  	'Right'    => $cright_read,
  	'Condition'=> array ('None') );
  
  // New
  $actions['profile']['new'] = array (
  	'Name'     => $l_header_new,
  	'Url'      => "$path/profile/profile_index.php?action=new",
  	'Right'    => $cright_write_admin,
  	'Condition'=> array ('search', 'index', 'detailconsult', 'insert', 'update', 'delete', 'display', 'export_ldif') );
  
  // Insert
    $actions['profile']['insert'] = array (
      'Url'      => "$path/profile/profile_index.php?action=insert",
      'Right'    => $cright_write_admin,
      'Condition'=> array ('None') );
  
  // Detail Consult
    $actions['profile']['detailconsult']  = array (
      'Name'     => $l_header_consult,
      'Url'      => "$path/profile/profile_index.php?action=detailconsult&amp;profile_id=$id",
      'Right'    => $cright_read,
      'Condition'=> array ('detailupdate'),
    );
    
  // Detail Update
  $actions['profile']['detailupdate'] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/profile/profile_index.php?action=detailupdate&amp;profile_id=$id",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'update') );  

  // Update
  $actions['profile']['update'] = array (
    'Url'      => "$path/profile/profile_index.php?action=update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') );
  
  // Check Delete
  $actions['profile']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/profile/profile_index.php?action=check_delete&amp;profile_id=$id",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'export_ldif') );

  // Delete
  $actions['profile']['delete'] = array (
    'Url'      => "$path/profile/profile_index.php?action=delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') );

//FIXME
//  // Display
//    $actions['profile']['display'] = array (
//      'Name'     => $l_header_display,
//      'Url'      => "$path/profile/profile_index.php?action=display",
//      'Right'    => $cright_read,
//      'Condition'=> array ('all') );
//  
//  // Display Preferences
//    $actions['profile']['dispref_display'] = array (
//      'Url'      => "$path/profile/profile_index.php?action=dispref_display",
//      'Right'    => $cright_read,
//      'Condition'=> array ('None') );
//  
//  // Display Level
//    $actions['profile']['dispref_level']  = array (
//      'Url'      => "$path/profile/profile_index.php?action=dispref_level",
//      'Right'    => $cright_read,
//      'Condition'=> array ('None') );
//FIXME
}


///////////////////////////////////////////////////////////////////////////////
// Organizational Chart Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_profile_action() {
  global $params, $actions, $path;

  $id = $params['profile_id'];
  
  if ($id > 0) {
    // Detail Consult
    $actions['profile']['detailconsult']['Url'] = "$path/profile/profile_index.php?action=detailconsult&amp;profile_id=$id";
    $actions['profile']['detailconsult']['Condition'][] = 'insert';

    // Detail Update
    $actions['profile']['detailupdate']['Url'] = "$path/profile/profile_index.php?action=detailupdate&amp;profile_id=$id";
    $actions['profile']['detailupdate']['Condition'][] = 'insert';

    // Check Delete
    $actions['profile']['check_delete']['Url'] = "$path/profile/profile_index.php?action=check_delete&amp;profile_id=$id";
    $actions['profile']['check_delete']['Condition'][] = 'insert';
  }
}
?>
