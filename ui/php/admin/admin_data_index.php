<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_data_index.php                                         //
//     - Desc : Update static database data (company contact number,...)     //
// 2002-06-28 - Pierre Baudracco                                             //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions
// - index
// - data_show
// - data_update
///////////////////////////////////////////////////////////////////////////////
// Ce script s'utilise avec PHP en mode commande (php4 sous debian)          //
///////////////////////////////////////////////////////////////////////////////

$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";

require("admin_query.inc");
require("admin_data_display.inc");

$debug=1;

$modules = array ('company');
//$modules = get_modules_array();
$actions = array ('help', 'index', 'data_show', 'data_update','clear_sess');

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if ($mode == "") $mode = "txt";

switch ($mode) {
 case "txt":
   require("$obminclude/phplib/obmlib.inc");
   include("$obminclude/global.inc"); 
   include("$obminclude/global_pref.inc"); 
   require("$obminclude/phplib/obmlib.inc");
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
   if($action!="clear_sess") {
    display_head("Admin_Data");
    generate_menu($menu);
   }
   break;
}


switch ($action) {
  case "help":
    dis_help($mode);
    break;
  case "index":
    dis_data_index($mode, $actions, $modules, $langs, $themes);
    break;
  case "data_show":
    dis_data($action, $mode, $module);
    break;
  case "data_update":
    dis_data($action, $mode, $module);
    break;
  case "clear_sess":
    dis_clear_sess($mode);
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
// Query execution - company list                                            //
///////////////////////////////////////////////////////////////////////////////
function get_company_list() {
  global $cdg_sql;

  $query = "select company_id, company_contact_number, company_deal_number
          from Company";

  display_debug_msg($query, $cdg_sql);

  $c_q = new DB_OBM;
  $c_q->query($query);
  return $c_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution - Company Update
// Parametres:
//   - $id       : company id
//   - $con_num  : contact number
//   - $deal_num : deal number
///////////////////////////////////////////////////////////////////////////////
function update_one_company($id, $con_num, $deal_num) {
  global $cdg_sql;

  $query = "update Company set company_contact_number='$con_num',
                 company_deal_number='$deal_num'
            where company_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $u_q = new DB_OBM;
  $retour = $u_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function dis_command_use($msg="") {
  global $actions, $modules;

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
-m module  ($lmodules)

Ex: php4 admin_data_index.php -a data_show -m company
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

  if (! $module) $module = "company";
  if (! $action) $action = "data_show";
}

</SCRIPT>

