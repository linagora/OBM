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
// OBM - File : admin_index.php                                              //
//     - Desc : Administration (Language, themes,...) management index file  //
// 1999-03-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Session Management                                                        //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$module = "admin";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";

$acts = array ('help', 'index', 'data_show', 'data_update', 'clear_sess');

include("$obminclude/global.inc");
require("admin_display.inc");
require("admin_query.inc");

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
$params = get_admin_params();

switch ($params["mode"]) {
 case "txt":
   $retour = parse_admin_arg($argv);
   include("$obminclude/global_pref.inc");
   if (! $retour) { end; }
   break;
 case "html":
   page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
   include("$obminclude/global_pref.inc");
   if ($action == "") $action = "index";
   get_admin_action();
   $perm->check_permissions($module, $action);
   $display["head"] = display_head($module);
   $display["header"] = display_menu($module);
   break;
}


switch ($action) {
  case "help":
    dis_admin_help($params["mode"]);
    break;
  case "index":
    $display['detail'] = dis_admin_index($params["mode"], $cs_lifetime);
    break;
  case "clear_sess":
    $display['detail'] = dis_admin_clear_sess($params["mode"], $cs_lifetime);
    break;
  default:
    echo "No action specified !";
    break;
}


// Program End
switch ($params["mode"]) {
 case "txt":
   echo "bye...\n";
   break;
 case "html":
   page_close();
   $display["end"] = display_end();
   $display["header"] = display_menu($module);
   display_page($display);
   break;
}


///////////////////////////////////////////////////////////////////////////////
// Stores Admin parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_admin_params() {

  $params = get_global_params("admin");

  if (($params["mode"] == "") || ($params["mode"] != "html")) {
    $params["mode"] = "txt";
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Display command use                                                       //
///////////////////////////////////////////////////////////////////////////////
function dis_admin_command_use($msg="") {
  global $acts, $target_modules, $langs, $themes;

  while (list($nb, $val) = each ($acts)) {
    if ($nb == 0) $lactions .= "$val";
    else $lactions .= ", $val";
  }

  echo "$msg
Usage: $argv[0] [Options]
where Options:
-h, --help help screen
-a action  ($lactions)

Ex: php4 admin_index.php -a clear_sess
";
}


///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function parse_admin_arg($argv) {
  global $debug, $acts, $target_modules;
  global $params, $action, $module;

  // We skip the program name [0]
  next($argv);
  while (list ($nb, $val) = each ($argv)) {
    switch($val) {
    case '-h':
    case '--help':
      $params["action"] = "help";
      dis_admin_help($params["mode"]);
      return true;
      break;
    case '-a':
      list($nb2, $val2) = each ($argv);
      if (in_array($val2, $acts)) {
        $params["action"] = $val2;
        if ($debug > 0) { echo "-a -> \$action=$val2\n"; }
      }
      else {
	dis_admin_command_use("Invalid action ($val2)");
	return false;
      }
      break;
    }
  }

  if (! $params["action"]) $params["action"] = "index";
  $action = $params["action"];
}


//////////////////////////////////////////////////////////////////////////////
// ADMIN actions
//////////////////////////////////////////////////////////////////////////////
function get_admin_action() {
  global $actions, $path;
  global $l_header_clear_sess,$l_header_index,$l_header_help;
  global $cright_read_admin, $cright_write_admin;

  // Index 
  $actions["admin"]["index"] = array (
    'Name'     => $l_header_index,   
    'Url'      => "$path/admin/admin_index.php?action=index&amp;mode=html",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                     );
  // data_show 
  $actions["admin"]["data_show"] = array (
    'Url'      => "$path/admin/admin_index.php?action=data_show&amp;mode=html",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('None') 
                                     );
  // Data Update 
  $actions["admin"]["data_update"] = array (
    'Url'      => "$path/admin/admin_index.php?action=data_update&amp;mode=html",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     );
  // Help
  $actions["admin"]["help"] = array (
     'Name'     => $l_header_help,
     'Url'      => "$path/admin/admin_index.php?action=help&amp;mode=html",
     'Right'    => $cright_read_admin,
     'Condition'=> array ('all') 
                                    );
  // Clear Session
  $actions["admin"]["clear_sess"] = array (
     'Name'     => $l_header_clear_sess,
     'Url'      => "$path/admin/admin_index.php?action=clear_sess&amp;mode=html",
     'Right'    => $cright_write_admin,
     'Condition'=> array ('None') 
                                    );
}

?>
