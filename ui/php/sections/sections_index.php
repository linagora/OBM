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

///////////////////////////////////////////////////////////////////////////////
// OBM - File : calendar_index.php                                           //
//     - Desc : Calendar Index File                                          //
// 2002-11-26 - Mehdi Rande                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id: calendar_index.php 6282 2010-04-14 09:19:47Z david $ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// gw_links
// sections_html
///////////////////////////////////////////////////////////////////////////////
$path = '..';
$module = 'sections';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
require("$obminclude/global.inc");
$params = get_global_params('Entity');
page_open(array('sess' => 'OBM_No_Session', 'auth' => 'OBM_No_Auth', 'perm' => 'OBM_Perm'));
require("$obminclude/global_pref.inc");

$module = ($_GET['module'])?$_GET['module']:'webmail';
$section = $cgp_show['module'][$module];
echo "<style type='text/css' src='$GLOBALS[css_obm]'></style>";
if ($_GET["action"] == 'gw_links') {
  echo display_modules($section, $module);
} else if ($_GET["action"] == 'sections_html') {
  echo display_sections($section);
}

?>
