<?php

///////////////////////////////////////////////////////////////////////////////
// OBM - File : php/campaign/campaign_webservice.php
//     - Desc : campaign web service
// 2008-02-11 Christophe Liou Kee On
///////////////////////////////////////////////////////////////////////////////
// $Id:$
///////////////////////////////////////////////////////////////////////////////
// Actions :

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