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
// OBM - File : admin_lang_index.php                                         //
//     - Desc : lang admin index File                                        //
// 2001-12-17 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$module = "admin_lang";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");

$debug = 1;
require("admin_lang_query.inc");
require("admin_lang_display.inc");


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
$params = get_admin_lang_params();

switch ($params["mode"]) {
 case "txt":
   // Check that this is not a fake txt attempt from a browser
   if (isset($_SERVER["SERVER_PROTOCOL"]) && ($_SERVER["SERVER_PROTOCOL"] != "")) {
     echo "TXT mode can only be used from CLI !!";
     exit;
   }
   $retour = parse_admin_lang_arg($argv);
   if (! $retour) { end; }
   break;
 case "html":
   $debug = $_SESSION['set_debug'];
   page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
   include("$obminclude/global_pref.inc");
   if ($action == "") $action = "index";
   get_admin_lang_action();
   $perm->check_permissions($module, $action);
   $display["head"] = display_head("$module");
   $display["header"] = display_menu($module);
   echo $display["head"] . $display["header"]. "<p>&nbsp;</p>" . $display["action"];
   break;
}


switch ($action) {
  case "help":
    dis_admin_lang_help($params["mode"]);
    break;
  case "index":
    dis_admin_lang_index($params["mode"], $actions, $modules, $langs, $themes);
    break;
  case "show_src":
    dis_admin_lang_src_vars($params["mode"], $params["target_module"]);
    break;
  case "show_lang":
    dis_admin_lang_vars($params["mode"], $params["target_module"], $params["lang"]);
    break;
  case "comp_lang":
    dis_admin_lang_comp_lang_vars($params["mode"], $params["target_module"], $params["lang"], $params["lang2"]);
    break;
  case "comp_global_lang":
    dis_admin_lang_comp_global_lang_vars($params["mode"], $params["lang"], $params["lang2"]);
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
function get_admin_lang_params() {

  $params = get_global_params("admin_lang");

  if (($params["mode"] == "") || ($params["mode"] != "html")) {
    $params["mode"] = "txt";
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function dis_admin_lang_command_use($msg="") {
  global $argv, $actions, $modules,$langs, $themes;

  while (list($nb, $val) = each ($actions)) {
    if ($nb == 0) $lactions .= "$val";
    else $lactions .= ", $val";
  }
  while (list($nb, $val) = each ($modules)) {
    if ($nb == 0) $lmodules .= "$key";
    else $lmodules .= ", $key";
  }
  while (list($nb, $val) = each ($langs)) {
    if ($nb == 0) $llangs .= "$val";
    else $llangs .= ", $val";
  }
  while (list($nb, $val) = each ($themes)) {
    if ($nb == 0) $lthemes .= "$val";
    else $lthemes .= ", $val";
  }

  echo "$msg
Usage: $argv[0] [Options]
where Options:
-h, --help help screen
-a action  ($lactions)
-m module  ($lmodules)
-l lang    ($llangs)
-t theme   ($lthemes)

Ex: php4 admin_lang.php -a show_lang -m deal -l fr
";
}


///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function parse_admin_lang_arg($argv) {
  global $debug, $actions, $modules, $langs, $themes;
  global $params, $action, $lang, $theme;

  // We skip the program name [0]
  next($argv);
  while (list ($nb, $val) = each ($argv)) {
    switch($val) {
    case '-h':
    case '--help':
      $params["action"] = "help";
      dis_admin_lang_help($params["mode"]);
      break;
    case '-m':
      list($nb2, $val2) = each ($argv);
      if (in_array($val2, $modules)) {
        $params["target_module"] = $val2;
        if ($debug > 0) { echo "-m -> \$target_module=$val2\n"; }
      }
      else {
        dis_admin_lang_command_use("Invalid module ($val2)");
	return false;
      }
      break;
    case '-l':
      list($nb2, $val2) = each ($argv);
      if (in_array($val2, $langs)) {
        $params["lang"] = $val2;
        if ($debug > 0) { echo "-l -> \$lang=$val2\n"; }
      }
      else {
	dis_admin_lang_command_use("Invalid language ($val2)");
	return false;
      }
      break;
    case '-a':
      list($nb2, $val2) = each ($argv);
      if (in_array($val2, $actions)) {
        $params["action"] = $val2;
        if ($debug > 0) { echo "-a -> \$action=$val2\n"; }
      }
      else {
	dis_admin_lang_command_use("Invalid action ($val2)");
	return false;
      }
      break;
    case '-t':
      list($nb2, $val2) = each ($argv);
      if (in_array($val2, $themes)) {
        $params["theme"] = $val2;
        if ($debug > 0) { echo "-t -> \$theme=$val2\n"; }
      }
      else {
	dis_admin_lang_command_use("Invalid theme ($val2)");
	return false;
      }
      break;
    }
  }

  if (! $params["target_module"]) $params["target_module"] = "contact";
  if (! $params["lang"]) $params["lang"] = "fr";
  if (! $params["action"]) $params["action"] = "show_src";
  if (! $params["theme"]) $params["theme"] = "default";
  $action = $params["action"];
}


//////////////////////////////////////////////////////////////////////////////
// ADMIN LANG actions
//////////////////////////////////////////////////////////////////////////////
function get_admin_lang_action() {
  global $actions, $path;
  global $l_header_clear_sess,$l_header_index,$l_header_help;
  global $cright_read_admin, $cright_write_admin;

  // index : launch forms
  $actions["admin_lang"]["index"] = array (
     'Name'     => $l_header_index,
     'Url'      => "$path/admin_lang/admin_lang_index.php?action=index&amp;mode=html",
     'Right'    => $cright_read_admin,
     'Condition'=> array ('all') 
                                    	 ); 
  // help
  $actions["admin_lang"]["help"] = array (
     'Name'     => $l_header_help,
     'Url'      => "$path/admin_lang/admin_lang_index.php?action=help&amp;mode=html",
     'Right'    => $cright_read_admin,
     'Condition'=> array ('all')
                                    	);
  // show_src : show variables referenced in module sources
  $actions["admin_lang"]["show_src"] = array (
     'Url'      => "$path/admin_lang/admin_lang_index.php?action=show_src&amp;mode=html",
     'Right'    => $cright_write_admin,
     'Condition'=> array ('None')
                                    	);
  // show_lang : compare vars referenced in source and defined in lang files
  $actions["admin_lang"]["show_lang"] = array (
     'Url'      => "$path/admin_lang/admin_lang_index.php?action=show_lang&amp;mode=html",
     'Right'    => $cright_write_admin,
     'Condition'=> array ('None') 
                                    	);
  // comp_lang : compare vars from 2 langs
  $actions["admin_lang"]["comp_lang"] = array (
     'Url'      => "$path/admin_lang/admin_lang_index.php?action=comp_lang&amp;mode=html",
     'Right'    => $cright_write_admin,
     'Condition'=> array ('None')
                                    	);
  // comp_global_lang : 
  $actions["admin_lang"]["comp_global_lang"] = array (
     'Url'      => "$path/admin_lang/admin_lang_index.php?action=comp_global_lang&amp;mode=html",
     'Right'    => $cright_write_admin,
     'Condition'=> array ('None')
                                    	);

}

?>
