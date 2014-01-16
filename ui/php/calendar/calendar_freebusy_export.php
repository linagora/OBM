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
$module = 'calendar';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_global_params('Entity');
$cgp_cookie_name = 'OBM_Public_Session';
require('calendar_query.inc');
require_once("$obminclude/of/of_contact.php");
require('calendar_display.inc');
require('../user/user_query.inc');
global $obm , $php_regexp_email;

if ( !isset($action) OR $action != 'freebusy_export' ) {
	// invalid action
	header('HTTP/1.1 403 Forbidden');
	exit;
}

if( !isset($_GET["email"]) OR !preg_match($php_regexp_email, stripslashes($_GET["email"]))) {
	// $mymail not defined or not well formed
	header('HTTP/1.1 404 Not Found');
	exit;
}

$mymail = stripslashes($_GET["email"]);
$myid = get_userid_from_mail($mymail);

if(!$myid){
	// User doesn't exist
	header('HTTP/1.1 404 Not Found');
	exit;
}

$user_pref = get_one_user_pref($myid, 'set_public_fb');

if($user_pref[$myid]['value'] != 'yes') {
	// freebusy charing not allowed by the user
	header('HTTP/1.1 403 Forbidden');
	exit;
}

$obmSyncServer = of_domain_get_domain_syncserver($obm['domain_id']);

if(!count($obmSyncServer)) {
	// No OBmSync server configured
	header('HTTP/1.1 501 Not Implemented');
	exit;
}

$iterator = new ArrayIterator($obmSyncServer);
$obmSyncServer = $iterator->current();
$obmSyncRootPath = "http://".$obmSyncServer[0]["ip"].":8080";
$freebusyPath = "/obm-sync/freebusy/";
$freebusyUrl = $obmSyncRootPath . $freebusyPath . $_GET[email];
$ret = (@file_get_contents($freebusyUrl));

if (!$ret) {
	header('HTTP/1.1 500 Internal Server Error');
	exit;
}

header('Content-Type: text/calendar');
header('Content-Disposition: inline; filename=ObmFreebusy.ics');
header('Cache-Control: maxage=3600');
header('Pragma: public');
echo $ret;


?>
