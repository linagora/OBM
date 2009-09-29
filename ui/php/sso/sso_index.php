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
// OBM - File : sso_index.php                                               //
//     - Desc : SSO Index File                                              //
// 2007-11-28 Mehdi Rande
///////////////////////////////////////////////////////////////////////////////
// $Id: user_index.php 2274 2007-11-20 15:32:14Z mehdi $ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$module = "sso";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
require("sso_query.inc");
$params = get_user_params();

if($action == "ticket") {
  page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
  include("$obminclude/global_pref.inc");
  get_sso_action();

  page_close();
}

if ($action == "validate") {
///////////////////////////////////////////////////////////////////////////////
  if($_REQUEST['ticket']) {
    $udata = run_query_validate($_REQUEST["ticket"]);
  }
  if(is_array($udata)) {
    echo "login=".($udata['login'])."&password=".($udata['password']);
  } else {
    echo "invalidOBMTicket";
  }
} elseif ($action == "ticket") {
///////////////////////////////////////////////////////////////////////////////
  $ticket = run_query_create();
  if($params['mode'] == 'interactive') {
    echo "ticket=$ticket";
  } else {
    if(isset($params['service'])) {
	if (strrpos($params[service], "?") > 0) {
          preg_replace("")
          header("location:$params[service]&ticket=$ticket");
          echo "<html><body onload=\"document.location='$params[service]&ticket=$ticket'\"></body></html>";
	} else {
          header("location:$params[service]?ticket=$ticket");
          echo "<html><body onload=\"document.location='$params[service]?ticket=$ticket'\"></body></html>";
	}
    } elseif(isset($params['section']) && isset($cgp_show["section"][$params['section']]['url'])) {
      header("location:".$cgp_show["section"][$params['section']]['url']."?ticket=$ticket");
      echo "<html><body onload=\"document.location='".$cgp_show["section"][$params['section']]['url']."?ticket=$ticket'\"></body></html>";
    } else {
      header("location:".$cgp_show["section"]["webmail"]["url"]."/login.php?ticket=$ticket");
      echo "<html><body onload=\"document.location='".$cgp_show["section"]["webmail"]["url"]."/login.php?ticket=$ticket'\"></body></html>";
    }
  }
} elseif ($action == "logout") {
///////////////////////////////////////////////////////////////////////////////
  header("location:".$cgp_show["section"]["webmail"]["url"]."/login.php?logout_reason=logout");
}



///////////////////////////////////////////////////////////////////////////////
// Stores User parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_user_params() {
  // Get global params
  $params = get_global_params("SSO");
  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// User Action 
///////////////////////////////////////////////////////////////////////////////
function get_sso_action() {
  global $params, $actions, $path;
  global $cright_read;
  
  $actions["sso"]["validate"] = array (
    'Url'      => "$path/sso/sso_index.php?action=validate",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                               );
  $actions["sso"]["ticket"] = array (
    'Url'      => "$path/sso/sso_index.php?action=ticket",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                               );
  $actions["sso"]["logout"] = array (
    'Url'      => "$path/sso/sso_index.php?action=logout",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                               );
}


?>
