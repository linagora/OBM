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
    echo "login=".urlencode($udata['login'])."&password=".urlencode($udata['password']);
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
