<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : admin_lang_index.php                                        //
//     - Desc  : lang admin index File                                       //
// 2001-12-17 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";

$debug = 1;
require("admin_query.inc");
require("admin_lang_query.inc");
require("admin_lang_display.inc");


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
   $debug = $set_debug;
   $menu = "ADMIN";
   require("$obminclude/phplib/obmlib.inc");
   page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
   include("$obminclude/global.inc");
   display_head("Admin_Lang");
   generate_menu($menu);
   break;
}


switch ($action) {
  case "help":
    dis_help($mode);
    break;
  case "index":
    dis_lang_index($mode, $actions, $modules, $langs, $themes);
    break;
  case "show_src":
    dis_src_vars($mode, $module);
    break;
  case "show_lang":
    dis_lang_vars($mode, $module, $lang);
    break;
  case "comp_lang":
    dis_comp_lang_vars($mode, $module, $lang, $lang2);
    break;
  case "comp_header_lang":
    dis_comp_header_lang_vars($mode, $theme, $lang, $lang2);
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
  global $actions, $modules, $langs, $themes;

  while (list($nb, $val) = each ($actions)) {
    if ($nb == 0) $lactions .= "$val";
    else $lactions .= ", $val";
  }
  while (list($nb, $val) = each ($modules)) {
    if ($nb == 0) $lmodules .= "$val";
    else $lmodules .= ", $val";
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
function parse_arg($argv) {
  global $debug, $actions, $modules, $langs, $themes;
  global $action, $module, $lang, $theme;

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
    case '-l':
      list($nb2, $val2) = each ($argv);
      if (in_array($val2, $langs)) {
        $lang = $val2;
        if ($debug > 0) { echo "-l -> \$lang=$val2\n"; }
      }
      else {
	dis_command_use("Invalid language ($val2)");
	return false;
      }
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
    case '-t':
      list($nb2, $val2) = each ($argv);
      if (in_array($val2, $themes)) {
        $theme = $val2;
        if ($debug > 0) { echo "-t -> \$theme=$val2\n"; }
      }
      else {
	dis_command_use("Invalid theme ($val2)");
	return false;
      }
      break;
    }
  }

  if (! $module) $module = "contact";
  if (! $lang) $lang = "fr";
  if (! $action) $action = "show_src";
  if (! $theme) $theme = "standard";
}
