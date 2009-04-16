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
?>
<?php

$path = '..';
$module = 'webmail';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_global_params('webmail');
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
include("webmail_display.inc");
get_webmail_action();


//$perm->check_permissions($module, $action);
$action = 'index';
page_close();

if ($action == 'index' || $action == '') {
///////////////////////////////////////////////////////////////////////////////
    $display['detail'] = dis_webmail_content();
}

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head('Webmail', false);
$display['header'] = display_menu($module);
$display['end'] = display_end();
display_outframe($display);



///////////////////////////////////////////////////////////////////////////////
// User Action 
///////////////////////////////////////////////////////////////////////////////
function get_webmail_action() {

// Index
  $GLOBALS['actions']['webmail']['index'] = array (
    'Url'      => "$GLOBALS[path]/resource/resource_index.php?action=index",
    'Right'    => $GLOBALS['cright_read'],
    'Condition'=> array ('None') 
                                    );
}

?>
