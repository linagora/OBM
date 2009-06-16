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
<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : control_index.php                                            //
//     - Desc : Control panel Index File                                     //
// 2004-07-28 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index        --         -- show the system control board
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'control';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_control_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
require('control_display.inc');
require('control_query.inc');

get_control_action();
$perm->check_permissions($module, $action);

if (($action == 'index') || ($action == '')) {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = html_control_consult();

}

///////////////////////////////////////////////////////////////////////////////
// Display page
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_control);
$display['end'] = display_end();
$display['header'] = display_menu($module);

display_page($display);

///////////////////////////////////////////////////////////////////////////////
// Stores control parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_control_params() {

  // Get global params
  $params = get_global_params('control');
  
  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Control Action 
///////////////////////////////////////////////////////////////////////////////
function get_control_action() {
  global $params, $actions, $path;
  global $l_header_consult;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions['control']['index'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/control/control_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

}
</script>
