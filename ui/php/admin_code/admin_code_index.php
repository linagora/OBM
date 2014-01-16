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
// OBM - File : admin_code_index.php                                         //
//     - Desc : code admin index File                                        //
// 2001-12-17 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "admin_code";
$obm_root = "../..";

// $obminclude not used in txt mode
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
require("admin_code_query.inc");
require("admin_code_display.inc");

list($key, $val) = each ($words);
$regexp = "&(?!($val";
while (list($key, $val) = each ($words)) {
  $regexp .= "|$val";
}
$regexp .= '))';


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
$params = get_admin_code_params();

switch ($params["mode"]) {
 case "txt":
   // Check that this is not a fake txt attempt from a browser
   if (isset($_SERVER["SERVER_PROTOCOL"]) && ($_SERVER["SERVER_PROTOCOL"] != "")) {
     echo "TXT mode can only be used from CLI !!";
     exit;
   }
   $retour = parse_admin_code_arg($argv);
   if (! $retour) { end; }
   break;
 case "html":
   page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
   include("$obminclude/global_pref.inc");
   //   $debug = $set_debug;
   if ($action == "") $action = "index";
   get_admin_code_action();
   $perm->check_permissions($module, $action);
   $display["head"] = display_head("Admin_Code");
   $display["header"] = display_menu($module);
   echo $display["head"] . $display["header"] . "<p>&nbsp;</p>". $display["action"];
   break;
}


switch ($action) {
  case "help":
    dis_admin_code_help($params["mode"]);
    break;
  case "index":
    dis_admin_code_code_index($params["mode"], $acts, $words);
    break;
  case "show_amp":
    dis_admin_code_amp($params["mode"], $words);
    break;
  case "func_unused":
    dis_admin_code_unused_functions($params["mode"], $params["param_module"]);
    break;
  case "function_uses":
    dis_admin_code_function_uses($params["mode"], $params["function"]);
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
   echo $display["end"];
   break;
}


///////////////////////////////////////////////////////////////////////////////
// Stores Admin parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_admin_code_params() {

  $params = get_global_params("admin_code");

  if (($params["mode"] == "") || ($params["mode"] != "html")) {
    $params["mode"] = "txt";
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function dis_admin_code_command_use($msg="") {
  global $acts, $target_modules, $langs, $themes;

  while (list($nb, $val) = each ($acts)) {
    if ($nb == 0) $lactions .= "$val";
    else $lactions .= ", $val";
  }
  while (list($nb, $val) = each ($target_modules)) {
    if ($nb == 0) $lmodules .= "$val";
    else $lmodules .= ", $val";
  }

  echo "$msg
Usage: $argv[0] [Options]
where Options:
-h, --help help screen
-a action  ($lactions)

Ex: php4 admin_code_index.php -a show_amp
";
}


///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function parse_admin_code_arg($argv) {
  global $debug, $acts, $target_modules;
  global $params, $action;

  // We skip the program name [0]
  next($argv);
  while (list ($nb, $val) = each ($argv)) {
    switch($val) {
    case '-h':
    case '--help':
      $params["action"] = "help";
      dis_admin_code_help($params["mode"]);
      break;
    case '-m':
      list($nb2, $val2) = each ($argv);
      if (in_array($val2, $target_modules)) {
        $params["param_module"] = $val2;
        if ($debug > 0) { echo "-m -> \$param_module=$val2\n"; }
      }
      else {
        dis_admin_code_command_use("Invalid module ($val2)");
	return false;
      }
      break;
    case '-a':
      list($nb2, $val2) = each ($argv);
      if (in_array($val2, $acts)) {
        $params["action"] = $val2;
        if ($debug > 0) { echo "-a -> \$action=$val2\n"; }
      }
      else {
	dis_admin_code_command_use("Invalid action ($val2)");
	return false;
      }
      break;
    case '-f':
      list($nb2, $val2) = each ($argv);
      if (in_array($val2, $acts)) {
        $params["action"] = $val2;
        if ($debug > 0) { echo "-f -> \$function=$val2\n"; }
      }
      else {
	dis_admin_code_command_use("Invalid action ($val2)");
	return false;
      }
      break;
    }
  }

  if (! $params["param_module"]) $params["param_module"] = "contact";
  if (! $params["action"]) $params["action"] = "show_amp";
  if (! $params["function"]) $params["function"] = "run_query_detail";
  $action = $params["action"];
}


///////////////////////////////////////////////////////////////////////////////
//  Admin Code Action 
///////////////////////////////////////////////////////////////////////////////
function get_admin_code_action() {
  global $actions, $path;
  global $l_header_index,$l_header_help, $l_header_amp, $l_header_func_unused;
  global $cright_read_admin, $cright_write_admin;

  // index : launch form
  $actions["admin_code"]["index"] = array (
     'Name'     => $l_header_index,
     'Url'      => "$path/admin_code/admin_code_index.php?action=index&amp;mode=html",
     'Right'    => $cright_read_admin,
     'Condition'=> array ('all') 
                                    	 );
  // help
  $actions["admin_code"]["help"]	= array (
     'Name'     => $l_header_help,
     'Url'      => "$path/admin_code/admin_code_index.php?action=help&amp;mode=html",
     'Right'    => $cright_read_admin,
     'Condition'=> array ('all') 
                                        );
  // show_amp : show & (&amp; use). & alone shouldn't be used in url
  $actions["admin_code"]["show_amp"]	= array (
     'Name'     => $l_header_amp,
     'Url'      => "$path/admin_code/admin_code_index.php?action=show_amp&amp;mode=html",
     'Right'    => $cright_write_admin,
     'Condition'=> array ('index') 
                                        );
  // func_unused : show unused functions
  $actions["admin_code"]["func_unused"]	= array (
     'Name'     => $l_header_func_unused,
     'Url'      => "$path/admin_code/admin_code_index.php?action=func_unused&amp;mode=html",
     'Right'    => $cright_write_admin,
     'Condition'=> array ('index') 
                                        );

  // function_uses : show function uses
  $actions["admin_code"]["function_uses"]	= array (
     'Name'     => $l_header,
     'Url'      => "$path/admin_code/admin_code_index.php?action=function_uses&amp;mode=html",
     'Right'    => $cright_write_admin,
     'Condition'=> array ('index') 
                                        );

}

?>
