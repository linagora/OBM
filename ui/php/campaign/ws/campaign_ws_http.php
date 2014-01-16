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
// OBM - File : php/campaign/ws/campaign_ws_http.php
//     - Desc : campaign web service
// 2008-02-11 Christophe Liou Kee On
///////////////////////////////////////////////////////////////////////////////
// $Id:  $ //
///////////////////////////////////////////////////////////////////////////////

$path = '../..';
$module = 'campaign';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == "") $obminclude = 'obminclude';
include("$obminclude/global.inc");
include 'classes.php';

$ws = new CampaignWSHM();

if (false) {
	
} else if ($_REQUEST['action'] == 'listTargets') {
	$ws->listTargets(split(',', $_REQUEST['campaign_ids']));

} else if ($_REQUEST['action'] == 'listTodayCampaignEmails') {
	$ws->listTodayCampaignEmails();

} else if ($_REQUEST['action'] == 'setTodayCampaignRunningStatus') {
	$ws->setCampaignRunningStatus(date("Y-m-d H:i:s"));

} else if ($_REQUEST['action'] == 'setCampaignsStatus') {
	$ws->setCampaignsStatus(split(',', $_REQUEST['campaign_ids']), $_REQUEST['status']);

} else if ($_REQUEST['action'] == 'getMailDocument') {
	$ws->getMailDocument($_REQUEST["document_id"]);

} else if ($_REQUEST['action'] == 'reportMailProgress') {
	$ws->reportMailProgress($_REQUEST['campaign_id'], $_REQUEST['nb_in_queue'],
		$_REQUEST['nb_sent'], $_REQUEST['nb_error']);

} else {
	echo "<pre>"
	. "listTargets(campaign_ids)\n"
	. "listTodayCampaignEmails()\n"
	. "getMailDocument(document_id)\n"
	. "reportMailProgress(campaign_id, nb_in_queue, nb_sent, nb_error)\n"
	. "setCampaignsStatus(campaign_ids, status)\n"
	. "   campaign_ids : x,y,z\n"
	. "   status : running|finished\n"
	. "</pre>";
}


?>