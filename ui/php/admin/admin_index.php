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
$section = "ADMIN";
$menu = "ADMIN";
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
   if($action == "") $action = "index";
   get_admin_action();
   $perm->check();
   $display["head"] = display_head("Admin");
   $display["header"] = generate_menu($menu, $section);
   echo $display["head"] . $display["header"];
   break;
}


if ($action != "help") {
  // We get lifetime from the database and not from the session variable
  // in case it has been updated since (session value last during the session)
  $lifetime = get_global_pref_lifetime();
}


switch ($action) {
  case "help":
    dis_help($mode);
    break;
  case "index":
    dis_index($mode, $lifetime);
    break;
  case "clear_sess":
    dis_clear_sess($mode, $lifetime);
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
  global $acts, $modules, $langs, $themes;

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
  global $debug, $acts, $modules;
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
  global $admin_read, $admin_write;

  // Index 
  $actions["ADMIN"]["index"] = array (
    'Name'     => $l_header_index,   
    'Url'      => "$path/admin/admin_index.php?action=index&amp;mode=html",
    'Right'    => $admin_read,
    'Condition'=> array ('all') 
                                     );
  // data_show 
  $actions["ADMIN"]["data_show"] = array (
    'Url'      => "$path/admin/admin_index.php?action=data_show&amp;mode=html",
    'Right'    => $admin_read,
    'Condition'=> array ('None') 
                                     );
  // Data Update 
  $actions["ADMIN"]["data_update"] = array (
    'Url'      => "$path/admin/admin_index.php?action=data_update&amp;mode=html",
    'Right'    => $admin_write,
    'Condition'=> array ('None') 
                                     );
  // Help
  $actions["ADMIN"]["help"] = array (
     'Name'     => $l_header_help,
     'Url'      => "$path/admin/admin_index.php?action=help&amp;mode=html",
     'Right'    => $admin_read,
     'Condition'=> array ('all') 
                                    );
  // Clear Session
  $actions["ADMIN"]["clear_sess"] = array (
     'Name'     => $l_header_clear_sess,
     'Url'      => "$path/admin/admin_index.php?action=clear_sess&amp;mode=html",
     'Right'    => $admin_write,
     'Condition'=> array ('index') 
                                    );
}

</SCRIPT>
