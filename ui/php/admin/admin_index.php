<script language="php">
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
if ($mode == "") $mode = "txt";

switch ($mode) {
 case "txt":
   $retour = parse_arg($argv);
   include("$obminclude/global_pref.inc");
   if (! $retour) { end; }
   break;
 case "html":
   page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
   include("$obminclude/global_pref.inc");
   if ($action == "") $action = "index";
   get_admin_action();
   $perm->check_permissions($module, $action);
   $display["head"] = display_head("$module");
   $display["header"] = generate_menu($module, $section);
   echo $display["head"] . $display["header"];
   break;
}


switch ($action) {
  case "help":
    dis_help($mode);
    break;
  case "index":
    dis_index($mode, $cs_lifetime);
    break;
  case "clear_sess":
    dis_clear_sess($mode, $cs_lifetime);
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
// Display command use                                                       //
///////////////////////////////////////////////////////////////////////////////
function dis_command_use($msg="") {
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
function parse_arg($argv) {
  global $debug, $acts, $target_modules;
  global $action, $module;

  // We skip the program name [0]
  next($argv);
  while (list ($nb, $val) = each ($argv)) {
    switch($val) {
    case '-h':
    case '--help':
      $action = "help";
      return true;
      break;
    case '-a':
      list($nb2, $val2) = each ($argv);
      if (in_array($val2, $acts)) {
        $action = $val2;
        if ($debug > 0) { echo "-a -> \$action=$val2\n"; }
      }
      else {
	dis_command_use("Invalid action ($val2)");
	return false;
      }
      break;
    }
  }

  if (! $action) $action = "clear_sess";
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
     'Condition'=> array ('index') 
                                    );
}

</script>
