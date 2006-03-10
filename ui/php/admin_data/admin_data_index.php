<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_data_index.php                                         //
//     - Desc : Update static database data (company contact number,...)     //
// 2002-06-28 - Pierre Baudracco                                             //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions
// - index
// - data_show
// - data_update
// - sound_aka_update
///////////////////////////////////////////////////////////////////////////////
// Ce script s'utilise avec PHP en mode commande (php4 sous debian)          //
///////////////////////////////////////////////////////////////////////////////

$debug = 1;
$path = "..";
$module = "admin_data";

$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";

include("$obminclude/global.inc");
require("admin_data_display.inc");
require("admin_data_query.inc");
require("$path/document/document_query.inc");

$target_modules = array ('company', 'deal', 'list', 'document');
$target_upd_modules = array ('company', 'deal', 'list');
$acts = array ('help', 'index', 'data_show', 'data_update', 'sound_aka_update');

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if ($mode == "") $mode = "txt";

switch ($mode) {
 case "txt":
   include("$obminclude/global_pref.inc"); 
   $retour = parse_admin_data_arg($argv);
   if (! $retour) { end; }
   break;
 case "html":
   $debug = $set_debug;
   page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
   include("$obminclude/global_pref.inc"); 
   if ($action == "") $action = "index";
   get_admin_data_action();
   $perm->check_permissions($module, $action);
   $display["head"] = display_head("Admin_Data");
   $display["header"] = display_menu($module);
   echo $display["head"] . $display["header"];
   break;
}


switch ($action) {
  case "help":
    dis_admin_data_help($mode);
    break;
  case "index":
    dis_admin_data_index($mode, $acts, $target_modules, $target_upd_modules);
    break;
  case "data_show":
    dis_admin_data($action, $mode, $target_module);
    break;
  case "data_update":
    dis_admin_data($action, $mode, $target_module);
    break;
  case "sound_aka_update":
    dis_admin_data_sound_aka_update($mode);
    break;
  default:
    echo "No action specified !";
    break;
}

// Program End
switch ($mode) {
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
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function dis_admin_data_command_use($msg="") {
  global $acts, $target_modules;

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
-m module  ($lmodules)

Ex: php4 admin_data_index.php -a data_show -m company
";
}


///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function parse_admin_data_arg($argv) {
  global $debug, $acts, $target_modules;
  global $action, $target_module;

  // We skip the program name [0]
  next($argv);
  while (list ($nb, $val) = each ($argv)) {
    switch($val) {
    case '-h':
    case '--help':
      $action = "help";
      return true;
      break;
    case '-m':
      list($nb2, $val2) = each ($argv);
      if (in_array($val2, $target_modules)) {
        $target_module = $val2;
        if ($debug > 0) { echo "-m -> \$target_module=$val2\n"; }
      }
      else {
        dis_admin_data_command_use("Invalid target_module ($val2)");
	return false;
      }
      break;
    case '-a':
      list($nb2, $val2) = each ($argv);
      if (in_array($val2, $acts)) {
        $action = $val2;
        if ($debug > 0) { echo "-a -> \$action=$val2\n"; }
      }
      else {
	dis_admin_data_command_use("Invalid action ($val2)");
	return false;
      }
      break;
    }
  }

  if (! $target_module) $target_module = "company";
  if (! $action) $action = "data_show";
}


///////////////////////////////////////////////////////////////////////////////
// ADMIN DATA actions
///////////////////////////////////////////////////////////////////////////////
function get_admin_data_action() {
  global $actions, $path;
  global $l_header_index,$l_header_help;
  global $cright_read_admin, $cright_write_admin;

  // index
  $actions["admin_data"]["index"] = array (
     'Name'     => $l_header_index,
     'Url'      => "$path/admin_data/admin_data_index.php?action=index&amp;mode=html",
     'Right'    => $cright_read_admin,
     'Condition'=> array ('all')
                                    	  );

 $actions["admin_data"]["help"] = array (
     'Name'     => $l_header_help,
     'Url'      => "$path/admin_data/admin_data_index.php?action=help&amp;mode=html",
     'Right' 	=> $cright_read_admin,
     'Condition'=> array ('all')
                                    	);

 $actions["admin_data"]["data_show"] = array (
     'Url'      => "$path/admin_data/admin_data_index.php?action=data_show&amp;mode=html",
     'Right' 	=> $cright_read_admin,
     'Condition'=> array ('None')
                                    	);

 $actions["admin_data"]["data_update"] = array (
     'Url'      => "$path/admin_data/admin_data_index.php?action=data_update&amp;mode=html",
     'Right' 	=> $cright_write_admin,
     'Condition'=> array ('None')
                                    	);

 $actions["admin_data"]["sound_aka_update"] = array (
     'Url'      => "$path/admin_data/admin_data_index.php?action=sound_search_update&amp;mode=html",
     'Right' 	=> $cright_write_admin,
     'Condition'=> array ('None')
                                    	);

}

?>