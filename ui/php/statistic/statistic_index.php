<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



///////////////////////////////////////////////////////////////////////////////
// OBM - File : statistic_index.php                                          //
//     - Desc : Statistic Index File                                         //
// 2004-04-19 Rande Mehdi                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default)              -- search fields -- the statistic search form
// - contact_date_evolution_graph --               -- contact date evolution 
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'statistic';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_statistic_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
require('statistic_display.inc');
require('statistic_query.inc');
require('statistic_js.inc');
include("$obminclude/of/of_category.inc");
 
get_statistic_action();
$perm->check_permissions($module, $action);

page_close();

if (! $params['popup']) {
  $display['header'] = display_menu($module);
}

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if ($action == 'index' || $action == '') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_statistic_index(); 

} elseif ($action == 'manager') {
///////////////////////////////////////////////////////////////////////////////
  $cont_q = run_query_statistic_contact_manager();
  $comp_q = run_query_statistic_company_manager();
  $display['title'] = display_title($l_stats_manager);
  $display['detail'] = dis_statistic_resp_stats($cont_q,$comp_q);

} elseif ($action == 'stats_category') {
///////////////////////////////////////////////////////////////////////////////
  require("$path/list/list_query.inc");
  $category = $params['category'];
  $entity = $params['entity'];
  if ($params['list_id'] == $c_all) {
    $cat_q = run_query_statistic_entity_per_country_per_cat($entity, $category);
    $nb_ent = get_entity_nb($entity);
  } else {
    $obm_q = run_query_statistic_get_list($params['list_id']);
    $ext_list_function = "ext_list_get_${entity}_ids";
    $ent_q = $ext_list_function($params['list_id']);
    $cat_q = run_query_statistic_selected_entity_per_country_per_cat($ent_q, $entity, $category);
    $nb_ent = $ent_q->nf();
    $title = ' : ' . $obm_q->f('list_name');
  }
  $display['detail'] = dis_statistic_cat_stats($cat_q, $nb_ent);
  $l_entity = ${"l_$entity"};
  $l_category = ${"l_$category"};
  $display['title'] = display_title("$l_stats : $l_entity / $l_category$title");

} elseif ($action == 'stats_category_export') {
///////////////////////////////////////////////////////////////////////////////
  require("$path/list/list_query.inc");
  $category = $params['category'];
  $entity = $params['entity'];
  if ($params['list_id'] == $c_all) {
    $cat_q = run_query_statistic_entity_per_country_per_cat($entity, $category);
    $nb_ent = get_entity_nb($entity);
  } else {
    $obm_q = run_query_statistic_get_list($params['list_id']);
    $ext_list_function = "ext_list_get_${entity}_ids";
    $ent_q = @$ext_list_function($params['list_id']);
    $cat_q = run_query_statistic_selected_entity_per_country_per_cat($ent_q, $entity, $category);
    $nb_ent = $ent_q->nf();
  }
  export_statistic_cat_stats($cat_q, $nb_ent);

} elseif ($action == 'contact_date_evolution_graph') {
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
$display['head'] = display_head($l_statistic);
$display['end'] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Statistic parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_statistic_params() {
  
  // Get global params
  $params = get_global_params();
  return $params;
}


///////////////////////////////////////////////////////////////////////////////
//  Statistic Action 
///////////////////////////////////////////////////////////////////////////////
function get_statistic_action() {
  global $cright_read, $cright_write,$cright_admin_read,$cright_admin_write;
  global $path,$actions,$params;
  global $l_header_comp_stats,$l_header_index,$l_header_export;
  global $l_header_contact_date_evolution_stats;

// Index
  $actions['statistic']['index'] = array (
    'Name'     => $l_header_index,
    'Url'      => "$path/statistic/statistic_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                        );
// Index
  $actions['statistic']['manager'] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('content') 
                                        );
					
// Stats entity by category 
  $actions['statistic']['stats_category'] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                        );
					
  $actions['statistic']['stats_category_export'] = array (
    'Name'     => $l_header_export,
    'Url'      => "$path/statistic/statistic_index.php?action=stats_category_export&amp;category=$params[category]&amp;popup=1&amp;list_id=$params[list_id]&amp;entity=$params[entity]",
    'Right'    => $cright_read,
    'Popup'    => 1,
    'Target'   => $l_statistic,
    'Condition'=> array ('stats_category')
                                        );					
					
  $actions['statistic']['contact_date_evolution'] = array (
    'Name'     => $l_header_contact_date_evolution_stats,
    'Url'      => "$path/statistic/statistic_index.php?action=contact_date_evolution",
    'Right'    => $cright_read,
    'Condition'=> array ('content')
                                        );

  $actions['statistic']['contact_date_evolution_graph'] = array (
    'Url'      => "$path/statistic/statistic_index.php?action=contact_date_evolution_graph",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                        );


}

?>
