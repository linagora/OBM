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
// OBM - File : admin_pref_index.php                                         //
//     - Desc : Update User Preferences (Display,...)                        //
// 2002-07-02 - Pierre Baudracco                                             //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions
// - index
// - user_pref_reset
// - user_pref_update_one -- update one preference for all users
///////////////////////////////////////////////////////////////////////////////
// Ce script s'utilise avec PHP en mode commande (php4 sous debian)          //
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "admin_pref";

$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
require("admin_pref_display.inc");
require("admin_pref_query.inc");

//$set_debug=1;
$actions = array ('help', 'index', 'user_pref_reset', 'user_pref_update_one');

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
$params = get_admin_pref_params();

switch ($params["mode"]) {
 case "txt":
   // Check that this is not a fake txt attempt from a browser
   if (isset($_SERVER["SERVER_PROTOCOL"]) && ($_SERVER["SERVER_PROTOCOL"] != "")) {
     echo "TXT mode can only be used from CLI !!";
     exit;
   }
   include("$obminclude/global_pref.inc");
   $retour = parse_admin_pref_arg($argv);
   if (! $retour) { end; }
   break;
 case "html":
   page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
   include("$obminclude/global_pref.inc"); 
   if ($action == "") $action = "index";
   get_admin_pref_admin_pref_action();
   $perm->check_permissions($module, $action);
   $display["head"] = display_head("Admin_Pref");
   $display["header"] = display_menu($module);
   echo $display["head"] . $display["header"] . "<p>&nbsp;</p>". $display["action"];
   break;
 default:
   echo "No mode specified !";
}


switch ($action) {
  case "help":
    dis_admin_pref_help($params["mode"]);
    break;
  case "index":
    dis_admin_pref_index($params["mode"]);
    break;
  case "user_pref_reset":
    dis_admin_pref_user_pref_reset($params["mode"]);
    dis_admin_pref_index($params["mode"]);
    break;
  case "user_pref_update_one":
    dis_admin_pref_user_pref_update_one($params["mode"], $params["userpref"], $params["pref_value"]);
    dis_admin_pref_index($params["mode"]);
    break;
  default:
    $msg = "$action : Action not implemented !";
    $display["msg"] .= display_err_msg("$msg");
    echo (($params["mode"] == "txt") ? $msg : $display["msg"]);
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
function get_admin_pref_params() {

  $params = get_global_params("admin_pref");

  if (($params["mode"] == "") || ($params["mode"] != "html")) {
    $params["mode"] = "txt";
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function dis_admin_pref_command_use($msg="") {
  global $actions;

  while (list($nb, $val) = each ($actions)) {
    if ($nb == 0) $lactions .= "$val";
    else $lactions .= ", $val";
  }
  
  echo "$msg
Usage: $argv[0] [Options]
where Options:
-h, --help help screen
-a action  ($lactions)
-o option, --option option
-v value, --value value

Ex: php admin_pref_index.php -a user_pref_reset
Ex: php admin_pref_index.php -a user_pref_update_one -o last_account -v 0
";
}


///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function parse_admin_pref_arg($argv) {
  global $cdg_param, $actions, $action;
  global $params, $sel_userpref, $tf_pref_value;

  // We skip the program name [0]
  next($argv);
  while (list ($nb, $val) = each ($argv)) {
    switch($val) {
    case '-h':
    case '--help':
      $params["action"] = "help";
      dis_admin_pref_help($params["mode"]);
      return true;
      break;
    case '-a':
      list($nb2, $val2) = each ($argv);
      if (in_array($val2, $actions)) {
        $params["action"] = $val2;
	if (debug_level_isset($cdg_param)) {
	  echo "-a -> \$action=$val2\n";
	}
      }
      else {
	dis_admin_pref_command_use("Invalid action ($val2)");
	return false;
      }
      break;
    case '-o':
    case '--option':
      list($nb2, $val2) = each ($argv);
      $params["userpref"] = $val2;
      if (debug_level_isset($cdg_param)) {
	echo "-o -> \$sel_userpref=$val2\n";
      }
      break;
    case '-v':
    case '--value':
      list($nb2, $val2) = each ($argv);
      $params["pref_value"] = $val2;
      if (debug_level_isset($cdg_param)) {
	echo "-v -> \$pref_value=$val2\n";
      }
      break;
    }
  }

  if (! $params["action"]) $params["action"] = "user_pref_reset";
  $action = $params["action"];
}


//////////////////////////////////////////////////////////////////////////////
// ADMIN PREF actions
//////////////////////////////////////////////////////////////////////////////
function get_admin_pref_admin_pref_action() {
  global $actions, $path;
  global $l_header_index,$l_header_pref_update,$l_header_help;
  global $cright_read_admin, $cright_write_admin;

  // index : lauch forms
  $actions["admin_pref"]["index"] = array (
     'Name'     => $l_header_index,
     'Url'      => "$path/admin_pref/admin_pref_index.php?action=index&amp;mode=html",
     'Right'    => $cright_read_admin,
     'Condition'=> array ('all') 
                                    	 );
  // help
  $actions["admin_pref"]["help"] = array (
     'Name'     => $l_header_help,
     'Url'      => "$path/admin_pref/admin_pref_index.php?action=help&amp;mode=html",
     'Right' 	=> $cright_read_admin,
     'Condition'=> array ('all') 
                                    	);
  // user_pref_reset : reset (drop) all users prefs
  $actions["admin_pref"]["user_pref_reset"] = array (
     'Name'     => $l_header_pref_update,
     'Url'      => "$path/admin_pref/admin_pref_index.php?action=user_pref_reset&amp;mode=html",
     'Right' 	=> $cright_write_admin,
     'Condition'=> array ('index') 
                                    	);
  // user_pref_update_one : update (set to default) one pref for all users
  $actions["admin_pref"]["user_pref_update_one"] = array (
     'Name'     => $l_header_pref_update,
     'Url'      => "$path/admin_pref/admin_pref_index.php?action=user_pref_update_one&amp;mode=html",
     'Right' 	=> $cright_write_admin,
     'Condition'=> array ('None') 
                                    	);

}

?>
