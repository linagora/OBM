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
$module = 'device';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';

include("$obminclude/global.inc");

//$params = get_global_params('Entity');
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
$params = get_device_params();
include("$obminclude/global_pref.inc");

require('device_display.inc');
require('../user/user_display.inc');
require('../user/user_query.inc');

get_device_action();
//$perm->check_permissions($module, $action);

page_close();

$extra_js_include[] = 'user.js';

if ($action == 'index') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_device_form();
}

// Display page
$display['head'] = display_head($GLOBALS['l_module_device']);
$display['header'] = display_menu($module);
$display['end'] = display_end();
display_page($display);


/**
 * Get params
 */
function get_device_params() {
  // Get global params
  $params = get_global_params('Device');
}


/**
 * Get actions
 */
function get_device_action() {
  global $path, $actions, $cright_read;

  $actions['device']['index'] = array (
    'Name'     => $GLOBALS['l_header_consult'],
    'Url'      => "$path/device/device_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all'));

}
?>
