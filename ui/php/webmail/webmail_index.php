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
page_close();

///////////////////////////////////////////////////////////////////////////////
// Write session in Roundcube DB
//////////////////////////////////////////////////////////////////////////////
prepare_session_roundcube();

function prepare_session_roundcube(){
	global $obm, $full_locale, $obmdb_db;
	$saved_db = $obmdb_db;
	$obmdb_db = 'roundcubemail';

	if (!isset($saved_session)){
		$saved_session = session_id();
		$query = 'SELECT sess_id FROM session WHERE sess_id=\''.$saved_session.'\'';

		$obm_q = new DB_OBM;
		$obm_q->query($query);

		if(!$obm_q->next_record()){		
			$vars = 'language|s:'.strlen($full_locale).':"'.$full_locale.'";obm_user_id|s:'.strlen((string) $obm['uid']).':"'.$obm['uid'].'";';
			$encoded_vars = base64_encode($vars);
			$obm_q2 = new DB_OBM;
			$insert_session_query = 'INSERT INTO session (sess_id, created, changed, ip, vars)
									 VALUES (\''.$saved_session.'\', NOW(), NOW(), \''.$_SERVER['REMOTE_ADDR'].'\', \''.$encoded_vars.'\')';
			$obm_q2->query($insert_session_query);
		}
		$obmdb_db = $saved_db;
	}
}

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head('Webmail', false);

$spreadlove= "<h2 class=\"spreadLove\"><div>"
  .__("You are using the open-source and free version of <a href=\"http://www.obm.org/\">OBM</a> developped and supported by <a href=\"http://www.linagora.com/\">Linagora</a>.")
  ."</div><div class=\"supportUs\">"
  .__("Contribute to the product R&amp;D by subscribing to an <a href=\"http://pro.obm.org/\">enterprise offer</a>.")
  ."</div></h2>";

$display['header'] = display_menu($module).$spreadlove;

$get_params = params_for_iframe();

$display['detail'] = '<iframe src="index.php'.$get_params.'" style="border:none;width:100%;height:94%;" id="webmail_iframe"></iframe>';
display_outframe($display);


function params_for_iframe(){
	$get_params = '';
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
