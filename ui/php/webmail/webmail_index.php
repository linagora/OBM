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
$module = 'webmail';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_global_params('webmail');
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
include("webmail_display.inc");
require_once("$obminclude/of/of_query.inc");
page_close();


$token = "";
if ( $auth_kind != "CAS" ) {
  ///////////////////////////////////////////////////////////////////////////////
  // Create Token to be used by Roundcube
  //////////////////////////////////////////////////////////////////////////////
  $token = generate_token();
}

function generate_token(){
	global $obm, $path;

	$userInfo = get_user_info($obm['uid']);
	$hasHostedMailbox = $userInfo["mail_perms"] == 1;

	if ($hasHostedMailbox) {
		$token = get_trust_token($userInfo);
	} else {
		header('Status: 301 OK');
		header("Location: $path/calendar/calendar_index.php");
		exit();
	}

	return $token;
}

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head('Webmail', false);

$display['header'] = display_menu($module);

$get_params = params_for_iframe($token);

$display['detail'] = '<iframe src="index.php?' . $get_params . '" id="webmail_iframe" frameBorder="0"></iframe>';
display_outframe($display);


function params_for_iframe($token){
  global $obm;

  $data = array('userobm_id' => $obm['uid']);
  if (isset($token)) {
    $data['obm_token'] = $token;
  }

  return http_build_query(array_merge($data, $_GET));
}

?>
