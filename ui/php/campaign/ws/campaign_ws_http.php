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