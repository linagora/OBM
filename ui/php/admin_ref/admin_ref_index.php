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
// OBM - File : admin_ref_index.php                                          //
//     - Desc : Referential data index file                                  //
// 2003-12-05 - Pierre Baudracco                                             //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions
// - index
// - datasource           --                -- Data Source index
// - datasource_insert    -- form fields    -- insert the Data Source
// - datasource_update    -- form fields    -- update the Data Source
// - datasource_checklink --                -- check if Data Source is used
// - datasource_delete    -- $sel_kind      -- delete the Data Source
// - country              --                -- Country index
// - country_insert       -- form fields    -- insert the Country
// - country_update       -- form fields    -- update the Country
// - country_checklink    --                -- check if Country is used
// - country_delete       --                -- delete the Country
// - tasktype             --                -- TaskType index
// - tasktype_insert      -- form fields    -- insert the Tasktype
// - tasktype_update      -- form fields    -- update the Tasktype
// - tasktype_checklink   --                -- check if Tasktype is used
// - tasktype_delete      --                -- delete the Tasktype
// - region               --                -- Region index
// - region_insert        -- form fields    -- insert the Region
// - region_update        -- form fields    -- update the Region
// - region_checklink     --                -- check if Region is used
// - region_delete        --                -- delete the Region
// External API ---------------------------------------------------------------
// - tt_ext_get_ids       --                -- select multiple tt (return id) 
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'admin_ref';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc"); 
$params = get_admin_ref_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");

require('admin_ref_display.inc');
require('admin_ref_query.inc');
require_once('admin_ref_js.inc');
require_once("$obminclude/of/of_category.inc");

if ($action == 'index') $action = 'country';
get_admin_ref_action();
$perm->check_permissions($module, $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)
///////////////////////////////////////////////////////////////////////////////
if ($action == 'tt_ext_get_ids') {
  $display['detail'] = html_admin_ref_tasktype_select_list($params);
  
} elseif ($action == 'index') {
///////////////////////////////////////////////////////////////////////////////
  //$display['detail'] = dis_ref_index();

} elseif ($action == 'country') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_admin_ref_country_index();

} elseif ($action == 'country_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_admin_ref_country_insert($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg($l_country_insert_ok);
  } else {
    $display['msg'] .= display_err_msg($l_country_insert_error);
  }
  $display['detail'] .= dis_admin_ref_country_index();

} elseif ($action == 'country_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_admin_ref_country_update($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg($l_country_update_ok);
  } else {
    $display['msg'] .= display_err_msg($l_country_update_error);
  }
  $display['detail'] .= dis_admin_ref_country_index();

} elseif ($action == 'country_checklink') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= dis_admin_ref_country_links($params);
  $display['detail'] .= dis_admin_ref_country_index();

} elseif ($action == 'country_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_admin_ref_country_delete($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg($l_country_delete_ok);
  } else {
    $display['msg'] .= display_err_msg($l_country_delete_error);
  }
  $display['detail'] .= dis_admin_ref_country_index();

} elseif ($action == 'datasource') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_admin_ref_datasource_index();

} elseif ($action == 'datasource_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_admin_ref_datasource_insert($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg($l_dsrc_insert_ok);
  } else {
    $display['msg'] .= display_err_msg($l_dsrc_insert_error);
  }
  $display['detail'] .= dis_admin_ref_datasource_index();

} elseif ($action == 'datasource_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_admin_ref_datasource_update($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg($l_dsrc_update_ok);
  } else {
    $display['msg'] .= display_err_msg($l_dsrc_update_error);
  }
  $display['detail'] .= dis_admin_ref_datasource_index();

} elseif ($action == 'datasource_checklink') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= dis_admin_ref_datasource_links($params);

} elseif ($action == 'datasource_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_admin_ref_datasource_delete($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg($l_dsrc_delete_ok);
  } else {
    $display['msg'] .= display_err_msg($l_dsrc_delete_error);
  }
  $display['detail'] .= dis_admin_ref_datasource_index();

} elseif ($action == 'tasktype') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_admin_ref_tasktype_index();

} elseif ($action == 'tasktype_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_admin_ref_tasktype_insert($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg($l_tt_insert_ok);
  } else {
    $display['msg'] .= display_err_msg($l_tt_insert_error);
  }
  $display['detail'] .= dis_admin_ref_tasktype_index();

} elseif ($action == 'tasktype_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_admin_ref_tasktype_update($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg($l_tt_update_ok);
  } else {
    $display['msg'] .= display_err_msg($l_tt_update_error);
  }
  $display['detail'] .= dis_admin_ref_tasktype_index();

} elseif ($action == 'tasktype_checklink') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= dis_admin_ref_tasktype_links($params);

} elseif ($action == 'tasktype_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_admin_ref_tasktype_delete($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg($l_tt_delete_ok);
  } else {
    $display['msg'] .= display_err_msg($l_tt_delete_error);
  }
  $display['detail'] .= dis_admin_ref_tasktype_index();

} elseif ($action == 'region') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_admin_ref_region_index();

} elseif ($action == 'region_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert('', 'region', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_region : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_region : $l_insert_error");
  }
  $display['detail'] = dis_admin_ref_region_index();
  
} elseif ($action == 'region_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update('', 'region', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_region : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_region : $l_update_error");
  }
  $display['detail'] = dis_admin_ref_region_index();
  
} elseif ($action == 'region_checklink') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= of_category_dis_links(array('deal'), 'region', $params, 'mono');
  
} elseif ($action == 'region_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete('', 'region', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_region : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_region : $l_delete_error");
  }
  $display['detail'] = dis_admin_ref_region_index();
}

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_header_admin_ref);
if (! $params['popup']) {
  $display['header'] = display_menu($module);
 }
$display['end'] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Admin Ref parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_admin_ref_params() {

  $params = get_global_params('admin_ref');

  // Admin - Country fields
  if (isset ($params['country'])) {
    $sel_country = $params['country'];
    $pos = strpos($sel_country, '-');
    $params['iso'] = substr($sel_country, 0, $pos);
    $params['lang'] = substr($sel_country, $pos+1);
  }

  if (isset ($$params['ext_id'])) { $params['id'] = $params['ext_id']; }

  return $params;
}


//////////////////////////////////////////////////////////////////////////////
// ADMIN REF actions
//////////////////////////////////////////////////////////////////////////////
function get_admin_ref_action() {
  global $actions, $path, $cgp_show;
  global $l_header_datasource, $l_header_country, $l_header_tasktype;
  global $l_header_region;
  global $cright_read, $cright_read_admin, $cright_write_admin;

  // Country index
  $actions['admin_ref']['country'] = array (
     'Name'     => $l_header_country,
     'Url'      => "$path/admin_ref/admin_ref_index.php?action=country&amp;mode=html",
     'Right'    => $cright_read_admin,
     'Condition'=> array ('all')
                                    	  );

// Country Insert
  $actions['admin_ref']['country_insert'] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=country_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Country Update
  $actions['admin_ref']['country_update'] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=country_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Country Check Link
  $actions['admin_ref']['country_checklink'] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=country_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Country Delete
  $actions['admin_ref']['country_delete'] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=country_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

  // DataSource index
  $actions['admin_ref']['datasource'] = array (
     'Name'     => $l_header_datasource,
     'Url'      => "$path/admin_ref/admin_ref_index.php?action=datasource&amp;mode=html",
     'Right'    => $cright_read_admin,
     'Condition'=> array ('all')
                                    	  );

// DataSource Insert
  $actions['admin_ref']['datasource_insert'] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=datasource_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// DataSource Update
  $actions['admin_ref']['datasource_update'] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=datasource_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// DataSource Check Link
  $actions['admin_ref']['datasource_checklink'] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=datasource_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// DataSource Delete
  $actions['admin_ref']['datasource_delete'] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=datasource_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

  // Tasktype management only displayed if deal or project or time module
  if (($cgp_show['module']['deal'])
      || ($cgp_show['module']['project'])
      || ($cgp_show['module']['time'])) {

    // External : Get Ids
    $actions['admin_ref']['tt_ext_get_ids'] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=tt_ext_get_ids",
    'Right'    => $cright_read,
    'Condition'=> array ('none'),
    'popup' => 1
                                    );

    // Tasktype index
    $actions['admin_ref']['tasktype'] = array (
     'Name'     => $l_header_tasktype,
     'Url'      => "$path/admin_ref/admin_ref_index.php?action=tasktype&amp;mode=html",
     'Right'    => $cright_read_admin,
     'Condition'=> array ('all')
                                    	  );

    // Tasktype Insert
    $actions['admin_ref']['tasktype_insert'] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=tasktype_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

    // Tasktype Update
    $actions['admin_ref']['tasktype_update'] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=tasktype_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

    // Tasktype Check Link
    $actions['admin_ref']['tasktype_checklink'] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=tasktype_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

    // Tasktype Delete
    $actions['admin_ref']['tasktype_delete'] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=tasktype_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );
  }

  // Region management only displayed if deal module
  if (($cgp_show['module']['deal'])
      || ($cgp_show['module']['deal'])) {

    // Region index
    $actions['admin_ref']['region'] = array (
     'Name'     => $l_header_region,
     'Url'      => "$path/admin_ref/admin_ref_index.php?action=region&amp;mode=html",
     'Right'    => $cright_read_admin,
     'Condition'=> array ('all')
                                    	  );

    // Region Insert
    $actions['admin_ref']['region_insert'] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=region_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

    // Region Update
    $actions['admin_ref']['region_update'] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=region_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

    // Region Check Link
    $actions['admin_ref']['region_checklink'] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=region_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

    // Region Delete
    $actions['admin_ref']['region_delete'] = array (
    'Url'      => "$path/admin_ref/admin_ref_index.php?action=region_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );
  }


}

?>
