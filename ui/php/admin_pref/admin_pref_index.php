<script language="php">
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
// - user_pref_update_one -- update one preference for all users
// - global_pref_update
///////////////////////////////////////////////////////////////////////////////
// Ce script s'utilise avec PHP en mode commande (php4 sous debian)          //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "ADMINS";
$menu = "ADMIN_PREF";

$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc"); 
require("admin_pref_display.inc");
require("admin_pref_query.inc");

$debug=1;
$actions = array ('help', 'index', 'user_pref_update', 'user_pref_update_one', 'global_pref_update');

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
   if ($action == "") $action = "index";
   get_admin_pref_action();
   $perm->check();
   $display["head"] = display_head("Admin_Pref");
   $display["header"] = generate_menu($menu, $section);
   echo $display["head"] . $display["header"];
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
  case "user_pref_update_one":
    $option = $pref["up_option"];
    $value = $pref["up_value"];
    if (! $value) $value = get_userpref_value($option); 
    dis_user_pref_update_one($mode, $option, $value);
    break;
  case "global_pref_update":
    if (check_data_form($pref)) {
      $retour = update_global_pref($pref);
      if ($retour) {
        $msg = $l_update_ok;
        $display["msg"] .= display_ok_msg($msg);
      } else {
        $msg = $l_update_error;
        $display["msg"] .= display_err_msg($msg);
      }
      echo (($mode == "txt") ? $msg : $display["msg"]);
      dis_pref_index($mode);
    } else {
      $msg = "$l_invalid_data : ($err_msg)";
      $display["msg"] .= display_err_msg("$msg");
      echo (($mode == "txt") ? $msg : $display["msg"]);
      dis_pref_index($mode);
    }
    break;
  default:
    $msg = "$action : Action not implemented !";
    $display["msg"] .= display_err_msg("$msg");
    echo (($mode == "txt") ? $msg : $display["msg"]);
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
-o option, --option option
-v value, --value value

Ex: php4 admin_pref_index.php -a user_pref_update
Ex: php4 admin_pref_index.php -a global_pref_update -l 3600 -s 1
Ex: php4 admin_pref_index.php -a user_pref_update_one -o last_account -v 0
";
}


///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function parse_arg($argv) {
  global $debug, $actions;
  global $action, $tf_lifetime, $cb_session_cookie;
  global $sel_userpref, $tf_pref_value;

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
    case '-l':
    case '--lifetime':
      list($nb2, $val2) = each ($argv);
      $tf_lifetime = $val2;
      if ($debug > 0) { echo "-l -> \$tf_lifetime=$val2\n"; }
      break;
    case '-s':
    case '--session-cookie':
      list($nb2, $val2) = each ($argv);
      $cb_session_cookie = $val2;
      if ($debug > 0) { echo "-s -> \$cb_session_cookie=$val2\n"; }
      break;
    case '-o':
    case '--option':
      list($nb2, $val2) = each ($argv);
      $sel_userpref = $val2;
      if ($debug > 0) { echo "-o -> \$sel_userpref=$val2\n"; }
      break;
    case '-v':
    case '--value':
      list($nb2, $val2) = each ($argv);
      $tf_pref_value = $val2;
      if ($debug > 0) { echo "-v -> \$tf_pref_value=$val2\n"; }
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
  global $tf_lifetime, $cb_session_cookie, $sel_userpref, $tf_pref_value;
  global $cdg_param;

  if (isset ($tf_lifetime)) $pref["lifetime"] = $tf_lifetime;
  if (isset ($cb_session_cookie)) $pref["session_cookie"] = 1;
  if (isset ($sel_userpref)) $pref["up_option"] = $sel_userpref;
  if (isset ($tf_pref_value)) $pref["up_value"] = $tf_pref_value;

  if (debug_level_isset($cdg_param)) {
    if ( $pref ) {
      while ( list( $key, $val ) = each( $pref ) ) {
        echo "<br>pref[$key]=$val";
      }
    }
  }

  return $pref;
}


//////////////////////////////////////////////////////////////////////////////
// ADMIN PREF actions
//////////////////////////////////////////////////////////////////////////////
function get_admin_pref_action() {
  global $actions, $path;
  global $l_header_index,$l_header_pref_update,$l_header_help;
  global $admin_pref_read, $admin_pref_write;

  // index : lauch forms
  $actions["ADMIN_PREF"]["index"] = array (
     'Name'     => $l_header_index,
     'Url'      => "$path/admin_pref/admin_pref_index.php?action=index&amp;mode=html",
     'Right'    => $admin_pref_read,
     'Condition'=> array ('all') 
                                    	 );
  // help
  $actions["ADMIN_PREF"]["help"] = array (
     'Name'     => $l_header_help,
     'Url'      => "$path/admin_pref/admin_pref_index.php?action=help&amp;mode=html",
     'Right' 	=> $admin_pref_read,
     'Condition'=> array ('all') 
                                    	);
  // user_pref_update : update (set to default) all users prefs
  $actions["ADMIN_PREF"]["user_pref_update"] = array (
     'Name'     => $l_header_pref_update,
     'Url'      => "$path/admin_pref/admin_pref_index.php?action=user_pref_update&amp;mode=html",
     'Right' 	=> $admin_pref_write,
     'Condition'=> array ('index') 
                                    	);
  // user_pref_update_one : update (set to default) one pref for all users
  $actions["ADMIN_PREF"]["user_pref_update_one"] = array (
     'Name'     => $l_header_pref_update,
     'Url'      => "$path/admin_pref/admin_pref_index.php?action=user_pref_update_one&amp;mode=html",
     'Right' 	=> $admin_pref_write,
     'Condition'=> array ('None') 
                                    	);
  // global_pref_update : global preferences update
  $actions["ADMIN_PREF"]["global_pref_update"] = array (
     'Url'     => "$path/admin_pref/admin_pref_index.php?action=global_pref_update&amp;mode=html",
     'Right' 	=> $admin_pref_write,
     'Condition'=> array ('None') 
                                    	);

}

</script>

