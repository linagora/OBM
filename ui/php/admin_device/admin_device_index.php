<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
*/

$path = '..';
$module = 'admin_device';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';

include("$obminclude/global.inc");

$params = get_device_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
$params = get_device_params();
include("$obminclude/global_pref.inc");

require('admin_device_display.inc');
require('admin_device_query.inc');
require('../user/user_display.inc');
require('../user/user_query.inc');

get_device_action();
$perm->check_permissions($module, $action);

page_close();

$extra_js_include[] = 'user.js';

if ($action == 'index') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = html_admin_device_search_form($params);
} elseif ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = html_admin_device_search_form($params);
  $display['result'] = dis_admin_device_search_list($params);
}

// Display page
$display['head'] = display_head($GLOBALS['l_module_admin_device']);
$display['header'] = display_menu($module);
$display['end'] = display_end();
display_page($display);


/**
 * Get params
 */
function get_device_params() {
  // Get global params
  $params = get_global_params();

  return $params;
}


/**
 * Get actions
 */
function get_device_action() {
  global $path, $actions, $cright_read;

  $actions['admin_device']['index'] = array (
    'Name'     => $GLOBALS['l_header_find'],
    'Url'      => "$path/admin_device/admin_device_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all'));

  $actions['admin_device']['search'] = array (
    'Url'      => "$path/admin_device/admin_device_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None'));
}
?>
