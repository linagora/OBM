<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_code_index.php                                         //
//     - Desc : code admin index File                                        //
// 2001-12-17 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$section = "ADMINS";
$menu = "ADMIN_CODE";

// $obminclude not used in txt mode
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
$debug=1;
$obm_root = "../..";
$cpt_line = 0;

require("admin_code_display.inc");

$acts = array ('help', 'index', 'show_amp');
$words = array ('amp;', 'nbsp;', ' ', '&', '\(', '\)', '\\n', '\$', '\'', '\|', 'eacute;', 'egrave;', 'agrave;', 'middot;');
$exclude = array('.', '..', 'CVS', 'doc', 'scripts');

list($key, $val) = each ($words);
$regexp = "&(?!($val";
while (list($key, $val) = each ($words)) {
  $regexp .= "|$val";
}
$regexp .= '))';


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if ($mode == "") $mode = "txt";

switch ($mode) {
 case "txt":
   $retour = parse_arg($argv);
   if (! $retour) { end; }
   break;
 case "html":
   require("$obminclude/phplib/obmlib.inc");
   include("$obminclude/global.inc");
   page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
   include("$obminclude/global_pref.inc");
   //   $debug = $set_debug;
   if($action == "") $action = "index";
   get_admin_code_action();
   $perm->check();
   display_head("Admin_Code");
   generate_menu($menu, $section);
   break;
}


switch ($action) {
  case "help":
    dis_help($mode);
    break;
  case "index":
    dis_code_index($mode, $acts, $words);
    break;
  case "show_amp":
    dis_amp($mode, $words);
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
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function dis_command_use($msg="") {
  global $acts, $modules, $langs, $themes;

  while (list($nb, $val) = each ($acts)) {
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

Ex: php4 admin_code_index.php -a show_amp
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
    case '-m':
      list($nb2, $val2) = each ($argv);
      if (in_array($val2, $modules)) {
        $module = $val2;
        if ($debug > 0) { echo "-m -> \$module=$val2\n"; }
      }
      else {
        dis_command_use("Invalid module ($val2)");
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
	dis_command_use("Invalid action ($val2)");
	return false;
      }
      break;
    }
  }

  if (! $module) $module = "contact";
  if (! $action) $action = "show_amp";
}


///////////////////////////////////////////////////////////////////////////////
//  Admin Code Action 
///////////////////////////////////////////////////////////////////////////////
function get_admin_code_action() {
  global $actions, $path;
  global $l_header_index,$l_header_help, $l_header_amp;
  global $admin_code_read,$admin_code_write;

  // index : launch form
  $actions["ADMIN_CODE"]["index"] = array (
     'Name'     => $l_header_index,
     'Url'      => "$path/admin_code/admin_code_index.php?action=index&amp;mode=html",
     'Right'    => $admin_code_read,
     'Condition'=> array ('all') 
                                    	 );
  // help
  $actions["ADMIN_CODE"]["help"]	= array (
     'Name'     => $l_header_help,
     'Url'      => "$path/admin_code/admin_code_index.php?action=help&amp;mode=html",
     'Right'    => $admin_code_read,
     'Condition'=> array ('all') 
                                        );
  // show_amp : show & (&amp; use). & alone shouldn't be used in url
  $actions["ADMIN_CODE"]["show_amp"]	= array (
     'Name'     => $l_header_amp,
     'Url'      => "$path/admin_code/admin_code_index.php?action=show_amp&amp;mode=html",
     'Right'    => $admin_code_write,
     'Condition'=> array ('index') 
                                        );

}

