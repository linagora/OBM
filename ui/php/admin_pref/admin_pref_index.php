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
// - user_pref_update
// - global_pref_update
///////////////////////////////////////////////////////////////////////////////
// Ce script s'utilise avec PHP en mode commande (php4 sous debian)          //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "ADMIN";
$menu = "ADMIN_PREF";

$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("$obminclude/phplib/obmlib.inc");
include("$obminclude/global.inc"); 
require("admin_pref_display.inc");
require("admin_pref_query.inc");

$debug=0;
$actions = array ('help', 'index', 'user_pref_update', 'global_pref_update');

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if ($mode == "") $mode = "txt";

switch ($mode) {
 case "txt":
   include("$obminclude/global_pref.inc"); 
   $retour = parse_arg($argv);
   if (! $retour) { end; }
   $pref = get_param_pref();
   break;
 case "html":
   $pref = get_param_pref();
   $debug = $set_debug;
   page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
   include("$obminclude/global_pref.inc"); 
   display_head("Admin_Pref");
   if($action == "") $action = "index";
   generate_menu($menu, $section);
   break;
 default:
   echo "No mode specified !";
}


switch ($action) {
  case "help":
    dis_help($mode);
    break;
  case "index":
    dis_pref_index($mode);
    break;
  case "user_pref_update":
    dis_user_pref_update($mode);
    break;
  case "global_pref_update":
    if (check_data_form($pref)) {
      $retour = update_global_pref($pref);
      if ($retour)
        echo "$l_update_ok<p>\n";
      else
        echo "$l_update_error<p>\n";
      dis_pref_index($mode);
    } else {
      echo "$l_invalid_data : ($err_msg)<p>\n";
      dis_pref_index($mode);
    }
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

  $query = "select userobm_id, userobm_login
          from UserObm order by userobm_login";

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
-l lifetime, --lifetime lifetime : session lifetime (seconds)
-s session_cookie, --session-cookie session_cookie : session_cookie (1=true |0)

Ex: php4 admin_pref_index.php -a user_pref_update
Ex: php4 admin_pref_index.php -a global_pref_update -l 3600 -s 1
";
}


///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function parse_arg($argv) {
  global $debug, $actions;
  global $action, $tf_lifetime, $cb_session_cookie;

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
    case '-l' || '--lifetime':
      list($nb2, $val2) = each ($argv);
      $tf_lifetime = $val2;
      if ($debug > 0) { echo "-a -> \$tf_lifetime=$val2\n"; }
      break;
    case '-s' || '--session-cookie':
      list($nb2, $val2) = each ($argv);
      $cb_session_cookie = $val2;
      if ($debug > 0) { echo "-a -> \$cb_session_cookie=$val2\n"; }
      break;
    }
  }

  if (! $action) $action = "user_pref_update";
}


///////////////////////////////////////////////////////////////////////////////
// Stores Admin preferences parameters transmited in $pref hash
// returns : $pref hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_pref() {
  global $tf_lifetime, $cb_session_cookie;
  global $cdg_param;

  if (isset ($tf_lifetime)) $pref["lifetime"] = $tf_lifetime;
  if (isset ($cb_session_cookie)) $pref["session_cookie"] = 1;

  if (debug_level_isset($cdg_param)) {
    if ( $pref ) {
      while ( list( $key, $val ) = each( $pref ) ) {
        echo "<br>pref[$key]=$val";
      }
    }
  }

  return $pref;
}

</SCRIPT>

