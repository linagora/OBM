<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_pref_index.php                                         //
//     - Desc : Update User Preferences (Display,...)                        //
// 2002-07-02 - Pierre Baudracco                                             //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions
// - index
// - pref_update
///////////////////////////////////////////////////////////////////////////////
// Ce script s'utilise avec PHP en mode commande (php4 sous debian)          //
///////////////////////////////////////////////////////////////////////////////

$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("admin_pref_display.inc");

$debug=0;

$actions = array ('help', 'index', 'pref_update');

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if ($mode == "") $mode = "txt";

switch ($mode) {
 case "txt":
   require("$obminclude/phplib/obmlib.inc");
   include("$obminclude/global.inc"); 
   include("$obminclude/global_pref.inc"); 
   $retour = parse_arg($argv);
   if (! $retour) { end; }
   break;
 case "html":
   $debug = $set_debug;
   $menu = "ADMIN";
   require("$obminclude/phplib/obmlib.inc");
   include("$obminclude/global.inc");
   page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
   include("$obminclude/global_pref.inc"); 
   display_head("Admin_Pref");
   generate_menu($menu);
   break;
}


switch ($action) {
  case "help":
    dis_help($mode);
    break;
  case "index":
    dis_pref_index($mode, $actions);
    break;
  case "pref_update":
    dis_pref_update($mode);
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
// Query execution - User list                                               //
///////////////////////////////////////////////////////////////////////////////
function get_user_list() {
  global $cdg_sql;

  $query = "select userobm_id, userobm_username
          from UserObm order by userobm_username";

  display_debug_msg($query, $cdg_sql);

  $u_q = new DB_OBM;
  $u_q->query($query);
  return $u_q;
}


///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function dis_command_use($msg="") {
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

Ex: php4 admin_pref_index.php -a pref_update
";
}


///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function parse_arg($argv) {
  global $debug, $actions;
  global $action;

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

  if (! $action) $action = "pref_update";
}

</SCRIPT>

