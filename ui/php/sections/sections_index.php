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
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
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
