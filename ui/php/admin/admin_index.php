<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : admin_index.php                                             //
//     - Desc  : Administration (Language, themes,...) management index file //
// 1999-03-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Session Management                                                        //
///////////////////////////////////////////////////////////////////////////////
$menu="ADMIN";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";

$actions = array ('help', 'index', 'data_show', 'data_update','clear_sess');

require("$obminclude/phplib/obmlib.inc");
require("admin_display.inc");
require("admin_query.inc");

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if ($mode == "") $mode = "txt";

switch ($mode) {
 case "txt":
   $retour = parse_arg($argv);
   include("$obminclude/global.inc");
   include("$obminclude/global_pref.inc");
   if (! $retour) { end; }
   break;
 case "html":
   $menu = "ADMIN";
   include("$obminclude/global.inc");
   page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
   include("$obminclude/global_pref.inc");
   //   $debug = $set_debug;
//   if($action!="clear_sess") {
     display_head("Admin_Code");
     generate_menu($menu);
//   }
   break;
}


switch ($action) {
  case "help":
    dis_help($mode);
    break;
  case "index":
    dis_index($mode);
    break;
  case "clear_sess":
    dis_clear_sess($mode);
    break;
  default:
    echo "No action specified !";
    break;
}
///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////

function dis_command_use($msg="") {
  global $actions, $modules, $langs, $themes;

  while (list($nb, $val) = each ($actions)) {
    if ($nb == 0) $lactions .= "$val";
    else $lactions .= ", $val";
  }
  while (list($nb, $val) = each ($modules)) {
    if ($nb == 0) $lmodules .= "$val";
    else $lmodules .= ", $val";
  }

  echo "$msg
Usage: $argv[0] [Options]
where Options:
-h, --help help screen
-a action  ($lactions)

Ex: php4 admin_clear_sess.php -a clear_sess
";
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
