<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : statistic_index.php                                          //
//     - Desc : Statistic Index File                                         //
// 2004-04-19 Rande Mehdi                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default)                -- search fields  -- show the statistic search form
// - contact_date_evolution_graph   --                -- show contact date evolution stats 
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "statistic";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("statistic_display.inc");
require("statistic_query.inc");
 
$uid = $auth->auth["uid"];

if ($action == "") $action = "index";
$params = get_statistic_params();
get_statistic_action();
$perm->check_permissions($module, $action);

page_close();

if (! $params["popup"]) {
  $display["header"] = display_menu($module);
}

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $display["features"] = dis_statistic_menu_stats(); 

} elseif ($action == "responsible") {
///////////////////////////////////////////////////////////////////////////////
  $cont_q = run_query_statistic_contact_per_resp();
  $comp_q = run_query_statistic_company_per_resp();
  $display["title"] = display_title($l_header_resp_stats);
  $display["detail"] = dis_statistic_resp_stats($cont_q,$comp_q);
  $display["features"] = dis_statistic_menu_stats(); 

} elseif ($action == "company") {
///////////////////////////////////////////////////////////////////////////////
  $list_q = run_query_statistic_get_lists();
  $display["detail"] = dis_statistic_list_select($list_q);
  $display["features"] = dis_statistic_menu_stats();
  $display["title"] = display_title($l_header_comp_stats);

} elseif ($action == "company_statistic") {
///////////////////////////////////////////////////////////////////////////////
  require("statistic_js.inc");
  require("$path/list/list_query.inc");
  if ($params["list_id"] == $c_all) {
    $cat_q = run_query_statistic_company_per_country_per_cat();
    $nb_comp = run_query_statistic_nb_company();
    $display["title"] = display_title($l_header_comp_stats);
  } else {
    $obm_q = run_query_statistic_get_list($params["list_id"]);
    $query = stripslashes($obm_q->f("list_query"));
    $com_q = ext_list_get_company_ids($params["list_id"]);
    $cat_q = run_query_statistic_selected_company_per_country_per_cat($com_q);
    $nb_comp = $com_q->nf();
    $display["title"] = display_title("$l_header_comp_stats : ".$obm_q->f("list_name"));
  }
  $display["detail"] = dis_statistic_cat_stats($cat_q, $nb_comp);
  $display["features"] = dis_statistic_menu_stats(); 

} elseif ($action == "company_statistic_export") {
///////////////////////////////////////////////////////////////////////////////
  require("$path/list/list_query.inc");
  if ($params["list_id"] == $c_all) {
    $cat_q = run_query_statistic_company_per_country_per_cat();
    $nb_comp = run_query_statistic_nb_company();
  } else {
    $obm_q = run_query_statistic_get_list($params["list_id"]);
    $query = $obm_q->f("list_query");
    $com_q = ext_list_get_company_ids($params["list_id"]);
    $cat_q = run_query_statistic_selected_company_per_country_per_cat($com_q);
    $nb_comp = $com_q->nf();
  }
  export_statistic_cat_stats($cat_q, $nb_comp);

} elseif ($action == "contact_date_evolution_graph") {
///////////////////////////////////////////////////////////////////////////////
  $stats = get_statistic_contact_date_total();
//  $tot = get_stats_contact_date_categories($stats);
//  dis_statistic_contact_date_evolution_graph($tot);
  dis_statistic_contact_date_evolution_graph($stats);
  flush();
  exit();
}

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_statistic);
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Statistic parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_statistic_params() {
  
  // Get global params
  $params = get_global_params();
  
  display_debug_param($params);
  
  return $params;
}


///////////////////////////////////////////////////////////////////////////////
//  Statistic Action 
///////////////////////////////////////////////////////////////////////////////
function get_statistic_action() {
  global $cright_read, $cright_write,$cright_admin_read,$cright_admin_write;
  global $path,$actions,$params;
  global $ico_contact,$ico_company;
  global $l_header_resp_stats,$l_header_comp_stats,$l_header_index,$l_header_export;
  global $l_header_contact_date_evolution_stats;

// Index
  $actions["statistic"]["index"] = array (
    'Name'     => $l_header_index,
    'Url'      => "$path/statistic/statistic_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                        );
// Index
  $actions["statistic"]["responsible"] = array (
    'Name'     => $l_header_resp_stats,
    'Url'      => "$path/statistic/statistic_index.php?action=responsible",
    'Ico'      => $ico_contact,
    'Right'    => $cright_read,
    'Condition'=> array ('content') 
                                        );
					
// 
  $actions["statistic"]["company"] = array (
    'Name'     => $l_header_comp_stats,
    'Url'      => "$path/statistic/statistic_index.php?action=company",
    'Ico'      => $ico_company,
    'Right'    => $cright_read,
    'Condition'=> array ('content') 
                                        );
// 
  $actions["statistic"]["company_statistic"] = array (
    'Url'      => "$path/statistic/statistic_index.php?action=company_statistic",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                        );
					
  $actions["statistic"]["company_statistic_export"] = array (
    'Name'     => $l_header_export,    
    'Url'      => "$path/statistic/statistic_index.php?action=company_statistic_export&amp;popup=1&amp;sel_list=".$params["list_id"]."",
    'Right'    => $cright_read,
    'Popup'    => 1,   
    'Target'   => $l_statistic,
    'Condition'=> array ('company_statistic') 
                                        );					
					
  $actions["statistic"]["contact_date_evolution"] = array (
    'Name'     => $l_header_contact_date_evolution_stats,
    'Url'      => "$path/statistic/statistic_index.php?action=contact_date_evolution",
    'Ico'      => $ico_contact,
    'Right'    => $cright_read,
    'Condition'=> array ('content')
                                        );

  $actions["statistic"]["contact_date_evolution_graph"] = array (
    'Url'      => "$path/statistic/statistic_index.php?action=contact_date_evolution_graph",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                        );


}

?>