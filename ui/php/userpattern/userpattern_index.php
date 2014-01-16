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
// OBM - File : userpattern_index.php                                        //
//     - Desc : User Pattern Index File                                      //
// 2010-01-21 Vincent ALQUIER                                                //
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'userpattern';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_userpattern_params();

page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
$user_lang_file = "$obminclude/lang/".strtolower(get_lang()).'/user.inc';
if (file_exists("$path/../".$user_lang_file)) include_once("$user_lang_file");
include("$obminclude/global_pref.inc");

require('userpattern_display.inc');
require('userpattern_query.inc');
require('userpattern_js.inc');
include_once("$path/../app/default/models/UserPattern.php");
include("$obminclude/of/of_category.inc");
$params = get_userpattern_params();

get_userpattern_action();
$perm->check_permissions($module, $action);

//update_last_visit('userpattern', $params['userpattern_id'], $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if ($action == 'index' || $action == '') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_userpattern_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_userpattern_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} elseif ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_userpattern_search_form($params);
  $display['result'] = dis_userpattern_search_list($params);

} elseif ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_userpattern_form($action, $params);

} elseif ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  $pattern = null;
  if (check_userpattern_data_form($params, $pattern)) {
    $insert_ok = $pattern->save();
  }
  if ($insert_ok) {
    $params['userpattern_id'] = $pattern->id;
    $display["detail"] = dis_userpattern_consult($pattern);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display["detail"] = dis_userpattern_form($action, $params);
  }

} elseif ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $userpattern_id = $params['userpattern_id'];
  if ($pattern = UserPattern::get($userpattern_id)) {
    $display["detail"] = dis_userpattern_consult($pattern);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['search'] = dis_userpattern_search_form($params);
  }

} elseif ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  $userpattern_id = $params['userpattern_id'];
  if ($pattern = UserPattern::get($userpattern_id)) {
    $display["detail"] = dis_userpattern_form($action, $params, $pattern);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['search'] = dis_userpattern_search_form($params);
  }

} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  $userpattern_id = $params['userpattern_id'];
  if ($pattern = UserPattern::get($userpattern_id)) {
    if (check_userpattern_data_form($params, $pattern)) {
      $update_ok = $pattern->save();
    }
    if ($update_ok) {
      $display["detail"] = dis_userpattern_consult($pattern);
    } else {
      $display['msg'] .= display_err_msg($err['msg']);
      $display["detail"] = dis_userpattern_form($action, $params, $pattern);
    }
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['search'] = dis_userpattern_search_form($params);
  }

} elseif ($action == 'detaildelete') {
///////////////////////////////////////////////////////////////////////////////
  $userpattern_id = $params['userpattern_id'];
  if ($pattern = UserPattern::get($userpattern_id)) {
    $display["detail"] = dis_userpattern_can_delete($pattern);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['search'] = dis_userpattern_search_form($params);
  }

} elseif ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  $userpattern_id = $params['userpattern_id'];
  if ($pattern = UserPattern::get($userpattern_id)) {
    $pattern->delete();
    $params['userpattern_id'] = 0;
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
  }
  $display['search'] = dis_userpattern_search_form($params);

} elseif ($action == 'apply') {
///////////////////////////////////////////////////////////////////////////////
  $userpattern_id = $params['userpattern_id'];
  if ($pattern = UserPattern::get($userpattern_id)) {
    $attributes = is_array($params['attributes']) ? $params['attributes'] : array();
    $pattern->applyTo($attributes);
    $return = array('attributes' => $attributes);
  }
  if (!empty($err['msg']))
    $return = array('err' => $err['msg']);
  echo json_encode($return);
  exit();
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_userpattern);
update_userpattern_action();
$display['header'] = display_menu($module);
$display['end'] = display_end();
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores User parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_userpattern_params() {
  // Get global params
  $params = get_global_params('userpattern');

  if (isset($params['mail_server_id']))
    $params['attributes']['mail_server_id'] = $params['mail_server_id'];

  if (is_array($GLOBALS['cgp_user']['user']['field'])) {
    foreach ($GLOBALS['cgp_user']['user']['field'] as $fieldname => $properties) {
      if (isset($params[$fieldname])) $params['attributes'][$fieldname] = $params[$fieldname];
    }
  }
  if (is_array($GLOBALS['cgp_user']['user']['category'])) {
    foreach ($GLOBALS['cgp_user']['user']['category'] as $fieldname => $properties) {
      if (isset($params[$fieldname])) $params['attributes'][$fieldname] = $params[$fieldname];
    }
  }

  // !!! WARNING: cheat anti magic_quotes !!! 
  if (get_magic_quotes_gpc()) {
    if (is_array($params['userpattern']))
      array_walk_recursive($params['userpattern'],create_function('&$value,$key','$value=stripslashes($value);'));
    if (is_array($params['attributes']))
      array_walk_recursive($params['attributes'],create_function('&$value,$key','$value=stripslashes($value);'));
  }

  if (is_array($params['attributes']['aliases'])) {
    foreach ($params['attributes']['aliases'] as $i => $alias) {
      if (!empty($alias)) {
        $params['attributes']['email'][$i] .= "@$alias";
      }
    }
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// User Action 
///////////////////////////////////////////////////////////////////////////////
function get_userpattern_action() {
  global $params, $actions, $path;

  $id = $params['userpattern_id'];
  $display_id = $params['display_id'];

  $actions['userpattern']['index'] = array (
    'Name'     => $GLOBALS['l_header_find'],
    'Url'      => "$path/userpattern/userpattern_index.php?action=index",
    'Right'    => $GLOBALS['cright_read'],
    'Condition'=> array ('all')
  );

  $actions['userpattern']['search'] = array (
    'Name'     => $GLOBALS['l_header_find'],
    'Url'      => "$path/userpattern/userpattern_index.php?action=search",
    'Right'    => $GLOBALS['cright_read'],
    'Condition'=> array ('None')
  );

  $actions['userpattern']['new'] = array (
    'Name'     => $GLOBALS['l_header_new'],
    'Url'      => "$path/userpattern/userpattern_index.php?action=new",
    'Right'    => $GLOBALS['cright_write'],
    'Condition'=> array ('all')
  );

  $actions['userpattern']['insert'] = array (
    'Url'      => "$path/userpattern/userpattern_index.php?action=insert",
    'Right'    => $GLOBALS['cright_write'],
    'Condition'=> array ('None')
  );

  $actions['userpattern']['detailconsult'] = array (
    'Name'     => $GLOBALS['l_header_consult'],
    'Url'      => "$path/userpattern/userpattern_index.php?action=detailconsult",
    'Right'    => $GLOBALS['cright_write'],
    'Condition'=> array ('detailupdate')
  );

  $actions['userpattern']['detailupdate'] = array (
    'Name'     => $GLOBALS['l_header_update'],
    'Url'      => "$path/userpattern/userpattern_index.php?action=detailupdate",
    'Right'    => $GLOBALS['cright_write'],
    'Condition'=> array ('detailconsult')
  );

  $actions['userpattern']['update'] = array (
    'Url'      => "$path/userpattern/userpattern_index.php?action=update",
    'Right'    => $GLOBALS['cright_write'],
    'Condition'=> array ('None')
  );

  $actions['userpattern']['detaildelete'] = array (
    'Name'     => $GLOBALS['l_header_delete'],
    'Url'      => "$path/userpattern/userpattern_index.php?action=detaildelete",
    'Right'    => $GLOBALS['cright_write'],
    'Condition'=> array ('detailconsult')
  );

  $actions['userpattern']['delete'] = array (
    'Url'      => "$path/userpattern/userpattern_index.php?action=delete",
    'Right'    => $GLOBALS['cright_write'],
    'Condition'=> array ('None')
  );

  $actions['userpattern']['apply'] = array (
    'Url'      => "$path/userpattern/userpattern_index.php?action=apply",
    'Right'    => $GLOBALS['cright_read'],
    'Condition'=> array ('None')
  );

}

///////////////////////////////////////////////////////////////////////////////
// User pattern Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_userpattern_action() {
  global $params, $actions, $path;

  $id = $params['userpattern_id'];
  if ($id > 0) {
    // Detail Consult
    $actions['userpattern']['detailconsult']['Url'] = "$path/userpattern/userpattern_index.php?action=detailconsult&amp;userpattern_id=$id";
    $actions['userpattern']['detailconsult']['Condition'][] = 'insert';
    $actions['userpattern']['detailconsult']['Condition'][] = 'update';
    $actions['userpattern']['detailconsult']['Condition'][] = 'delete';

    // Detail Update
    $actions['userpattern']['detailupdate']['Url'] = "$path/userpattern/userpattern_index.php?action=detailupdate&amp;userpattern_id=$id";
    $actions['userpattern']['detailupdate']['Condition'][] = 'insert';
    $actions['userpattern']['detailupdate']['Condition'][] = 'update';
    $actions['userpattern']['detailupdate']['Condition'][] = 'delete';

    // Detail Delete
    $actions['userpattern']['detaildelete']['Url'] = "$path/userpattern/userpattern_index.php?action=detaildelete&amp;userpattern_id=$id";
    $actions['userpattern']['detaildelete']['Condition'][] = 'insert';
    $actions['userpattern']['detaildelete']['Condition'][] = 'update';
    $actions['userpattern']['detaildelete']['Condition'][] = 'delete';
  }

}

?>
