<SCRIPT language="php">
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

$actions = array ('help', 'index', 'data_show', 'data_update', 'clear_sess');

require("$obminclude/phplib/obmlib.inc");
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
   display_head("Admin_Code");
   generate_menu($menu, $section);
   if($action == "") $action = "index";
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
   display_end();
   break;
}


///////////////////////////////////////////////////////////////////////////////
// Display command use                                                       //
///////////////////////////////////////////////////////////////////////////////
function dis_command_use($msg="") {
  global $actions, $modules, $langs, $themes;

  while (list($nb, $val) = each ($actions)) {
    if ($nb == 0) $lactions .= "$val";
    else $lactions .= ", $val";
  }

  echo "$msg
Usage: $argv[0] [Options]
where Options:
-h, --help help screen
-a action  ($lactions)

Ex: php4 admin_clear_sess.php -a clear_sess
";
}

///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function parse_arg($argv) {
  global $debug, $actions, $modules;
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
      if (in_array($val2, $actions)) {
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


</SCRIPT>
