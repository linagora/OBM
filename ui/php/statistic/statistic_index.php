<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : statistic_index.php                                            //
//     - Desc : Statistic Index File                                           //
// 2004-04-19 Rande Mehdi                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default)    -- search fields  -- show the statistic search form
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Session, Auth, Perms Management                                           //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "COM";
$menu = "STATISTIC";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("statistic_display.inc");
require("statistic_query.inc");
 

$uid = $auth->auth["uid"];

page_close();

if ($action == "") $action = "index";
$statistic = get_param_statistic();
get_statistic_action();
$perm->check_permissions($menu, $action);


if (! $statistic["popup"]) {
  $display["header"] = generate_menu($menu,$section);
}

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $display["features"] = dis_menu_stats(); 
}
elseif ($action == "responsible") {
///////////////////////////////////////////////////////////////////////////////
  $cont_q = run_query_contact_per_resp();
  $comp_q = run_query_company_per_resp();
  $display["detail"] = dis_resp_stats($cont_q,$comp_q);
  $display["features"] = dis_menu_stats(); 

}
elseif ($action == "company") {
///////////////////////////////////////////////////////////////////////////////
  $cat_q = run_query_company_per_country_per_cat();
  $nb_comp = run_query_nb_company();
  $display["detail"] = dis_cat_stats($cat_q,$nb_comp);
  $display["features"] = dis_menu_stats(); 

}

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_statistic);
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Statistic parameters transmited in $statistic hash
// returns : $statistic hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_statistic() {
  global $action;
  global $HTTP_POST_VARS,$HTTP_GET_VARS;

  return $statistic;
}


///////////////////////////////////////////////////////////////////////////////
//  Statistic Action 
///////////////////////////////////////////////////////////////////////////////
function get_statistic_action() {
  global $cright_read, $cright_write,$cright_admin_read,$cright_admin_write;
  global $path,$actions;
  global $ico_contact,$ico_company;
  global $l_header_resp_stats,$l_header_comp_stats,$l_header_index;
// Index
  $actions["STATISTIC"]["index"] = array (
    'Name'     => $l_header_index,
    'Url'      => "$path/statistic/statistic_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                        );
// Index
  $actions["STATISTIC"]["responsible"] = array (
    'Name'     => $l_header_resp_stats,
    'Url'      => "$path/statistic/statistic_index.php?action=responsible",
    'Ico'      => $ico_contact,
    'Right'    => $cright_read,
    'Condition'=> array ('content') 
                                        );
					
// 
  $actions["STATISTIC"]["company"] = array (
    'Name'     => $l_header_comp_stats,
    'Url'      => "$path/statistic/statistic_index.php?action=company",
    'Ico'      => $ico_company,
    'Right'    => $cright_read,
    'Condition'=> array ('content') 
                                        );
}

</script>
