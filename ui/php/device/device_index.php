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



$path = '..';
$module = 'device';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';

include("$obminclude/global.inc");

$params = get_global_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
$params = get_device_params();
include("$obminclude/global_pref.inc");

require('device_display.inc');
require('../user/user_display.inc');
require('../user/user_query.inc');

get_device_action();
$perm->check_permissions($module, $action);

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
  $params = get_global_params();

  return $params;
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
