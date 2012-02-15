<?php

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
require_once('calendar_js.inc');
require("$obminclude/of/of_right.inc");
require_once("$obminclude/of/of_category.inc");
require('calendar_mailer.php');
require('event_observer.php');
require('../contact/addressbook.php');
require('../user/user_query.inc');
global $obm , $php_regexp_email;
if ( $action == 'freebusy_export') {
	$mymail = stripslashes($_GET["email"]);
    if( isset($mymail) && preg_match($php_regexp_email, $mymail)) {
        
        $myid = get_userid_from_mail($mymail);
        if($myid){
        	
        	$user_pref = get_one_user_pref($myid, 'set_public_fb');
        	if($user_pref[$myid]['value'] == 'yes') {
        		
        		$obmSyncServer = of_domain_get_domain_syncserver($obm['domain_id']);
        		
        		if(!count($obmSyncServer)) {
        			header('HTTP/1.1 501 Not Implemented');
        			exit;
        		}else {
        			$iterator = new ArrayIterator($obmSyncServer);
        			$obmSyncServer = $iterator->current();
        			$obmSyncRootPath = "http://".$obmSyncServer[0]["ip"].":8080";
        			$freebusyPath = "/obm-sync/freebusy/";
        			$freebusyUrl = $obmSyncRootPath . $freebusyPath . $_GET[email];
        			$ret = (@file_get_contents($freebusyUrl));
        			if (!$ret) {
        				header('HTTP/1.1 500 Internal Server Error');
        				exit;
        			}else {
        	
        				header('Content-Type: text/calendar');
        				header('Content-Disposition: inline; filename=ObmFreebusy.ics');
        				header('Cache-Control: maxage=3600');
        				header('Pragma: public');
        				echo $ret;
        			}
        		}
        	}else {
        		// TODO freebusy charing not allowed by the user
        		header('HTTP/1.1 403 Forbidden');
        		exit;
        	}
        }else{
        	//TODO email adress doesn't exist
        	header('HTTP/1.1 404 Not Found');
        	exit;
        }

    }else {
        //TODO $mymail not defined or not well formed
    	header('HTTP/1.1 404 Not Found');
    	exit;
    }
}else {
    // TODO invalid action
	header('HTTP/1.1 403 Forbidden');
	exit;
}


?>
