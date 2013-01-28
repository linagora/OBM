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

///////////////////////////////////////////////////////////////////////////////
// Create Token to be used by Roundcube
//////////////////////////////////////////////////////////////////////////////
$token = generate_token();

function generate_token(){
	global $obm, $path;

	$userInfo = get_user_info($obm['uid']);

	if( isset($userInfo['email']) ){
		$token = get_trust_token($userInfo);
	} else {
		$url_redirect = $path."/calendar/calendar_index.php";
		header('Status: 301 OK');
		header("Location: $url_redirect");
		exit();
	}
	return $token;
}

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head('Webmail', false);
/*
$spreadlove= "<h2 class=\"spreadLove\"><div>"
  .__("You are using the open-source and free version of <a href=\"http://www.obm.org/\">OBM</a> developped and supported by <a href=\"http://www.linagora.com/\">Linagora</a>.")
  ."</div><div class=\"supportUs\">"
  .__("Contribute to the product R&amp;D by subscribing to an <a href=\"http://pro.obm.org/\">enterprise offer</a>.")
  ."</div></h2>";
*/
$display['header'] = display_menu($module).$spreadlove;

$get_params = params_for_iframe($token);

$display['detail'] = '<iframe src="index.php'.$get_params.'" style="border:none;width:100%;height:94%;padding-top:40px;" id="webmail_iframe"></iframe>';
display_outframe($display);


function params_for_iframe($token){
	$get_params = ( isset($token) ) ? '?obm_token='.$token : '';
	if (!empty($_GET)) {
		foreach ($_GET as $key => $value) {
			if ($get_params == '') {
				$get_params .= '?'.$key.'='.$value;
			} else {
				$get_params .= '&'.$key.'='.$value;
			}
		}
	}
	return $get_params;
}

?>
