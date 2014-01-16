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
// OBM - File : import_index.php                                             //
//     - Desc : Import Index File                                            //
// 2004-01-16 - Aliacom - Pierre Baudracco                                   //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the list search form
// - search          -- search fields  -- show the result set of search
// - new             --                -- show the new list form
// - detailconsult   -- $param_list    -- show the list detail
// - detailupdate    -- $param_list    -- show the list detail form
// - insert          -- form fields    -- insert the list
// - update          -- form fields    -- update the list
// - delete          -- $param_list    -- delete the list
// - file_sample     -- 
// - file_test       -- 
// - file_import     -- 
// - file_conflict   -- 
// - contact_del     -- 
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
// - export_add      --                --
// External API ---------------------------------------------------------------
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'import';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
require("$obminclude/global.inc");
include("$obminclude/of/of_category.inc");
require_once("$obminclude/of/of_search.php");
require_once ('../contact/addressbook.php');
$params = get_import_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));

///////////////////////////////////////////////////////////////////////////////
// Company and Contact lang files inclusions
///////////////////////////////////////////////////////////////////////////////
$lang_file = "$obminclude/lang/".$_SESSION['set_lang']."/company.inc";
if (file_exists("$path/../".$lang_file)) {
  include("$lang_file");
}

// Specific conf company lang file
if ($conf_lang) {
  $lang_file = "$obminclude/conf/lang/".$_SESSION['set_lang']."/company.inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  }
}

$lang_file = "$obminclude/lang/".$_SESSION['set_lang']."/contact.inc";
if (file_exists("$path/../".$lang_file)) {
  include("$lang_file");
}

// Specific conf contact lang file
if ($conf_lang) {
  $lang_file = "$obminclude/conf/lang/".$_SESSION['set_lang']."/contact.inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  }
}

///////////////////////////////////////////////////////////////////////////////

require("$obminclude/global_pref.inc");
include('import_display.inc');
include('import_query.inc');
require('import_js.inc');

get_import_action();
$perm->check_permissions($module, $action);

page_close();

$field_size = get_import_field_size();


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if (! $popup) {
  $display['header'] = display_menu($module);
}


if (($action == 'index') || ($action == '')) {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_import_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_import_search_list('', $popup);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} else if ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_import_search_form($params);
  $display['result'] = dis_import_search_list($params, $popup);

} else if ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $dsrc_q = run_query_global_datasource();
  $users = of_usergroup_get_group_users($obm['group_com'], true);
  $display['detail'] = html_import_form($action, $params, '', $dsrc_q, $users);

} else if ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_import_consult($params);

} else if ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_import_detail($params['import_id']);
  $dsrc_q = run_query_global_datasource();
  $users = of_usergroup_get_group_users($obm['group_com'], true);
  $display['detail'] = html_import_form($action, $params, $obm_q, $dsrc_q, $users);

} else if ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_import_data_form('', $params)) {

    // If the context (same import) was confirmed ok, we proceed
    if ($hd_confirm == $c_yes) {
      $retour = run_query_import_insert($params);
      if ($retour) {
        $display['msg'] .= display_ok_msg("$l_import : $l_insert_ok");
      } else {
        $display['msg'] .= display_err_msg("$l_import : $l_insert_error");
      }
      $display['search'] = dis_import_search_form($import_q);

    // If it is the first try, we warn the user if some imports seem similar
    } else {
      $obm_q = check_import_context('', $params);
      if ($obm_q->num_rows() > 0) {
        $display['detail'] = dis_import_warn_insert($obm_q, $params);
      } else {
        $retour = run_query_import_insert($params);
        if ($retour) {
          $display['msg'] .= display_ok_msg("$l_import : $l_insert_ok");
        } else {
          $display['msg'] .= display_err_msg("$l_import : $l_insert_error");
        }
        $display['search'] = dis_import_search_form($params);
      }
    }

  // Form data are not valid
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
    $dsrc_q = run_query_global_datasource();
    $users = of_usergroup_get_group_users($obm['group_com'], true);
    $display['detail'] = html_import_form($action, $params, '', $dsrc_q, $users);
  }

} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_import_data_form($params['import_id'], $params)) {
    $retour = run_query_import_update($params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_import : $l_update_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_import : $l_update_error");
    }
    $import_q = run_query_import_detail($params['import_id']);
    $display['detail'] = html_import_consult($import_q);
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
    $import_q = run_query_import_detail($params['import_id']);
    $dsrc_q = run_query_global_datasource();
    $users = of_usergroup_get_group_users($obm['group_com'], true);
    $display['detail'] = html_import_form($action, $params, $import_q, $dsrc_q, $users);
  }

} elseif ($action == 'check_delete') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_import_warn_delete($params['import_id']);

} elseif ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_import_delete($params['import_id']);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_import : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_import : $l_delete_error");
  }
  $display['search'] = dis_import_search_form($params);

} elseif ($action == 'file_sample') {
///////////////////////////////////////////////////////////////////////////////
  $import_q = run_query_import_detail($params['import_id']);
  $display['detail'] = html_import_consult_file($import_q);
  $display['detail'] .= html_import_file_sample($import_q, $params, 5);

} elseif ($action == 'file_test') {
///////////////////////////////////////////////////////////////////////////////
  $import_q = run_query_import_detail($params['import_id']);
  $display['detail'] = html_import_consult_file($import_q);
  $display['detail'] .= html_import_file_import($import_q, $params);

} elseif ($action == 'file_import') {
///////////////////////////////////////////////////////////////////////////////
  $import_q = run_query_import_detail($params['import_id']);
  $display['detail'] = html_import_consult_file($import_q);
  $display['detail'] .= html_import_file_import($import_q, $params);

}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_import);
$display["header"] = display_menu($module);
$display['end'] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Construct the Import description field from import parameters hash
// returns:
//   $impd : string with each import parameters concatenated 
///////////////////////////////////////////////////////////////////////////////
function get_import_desc($import) {
  global $cgp_user;

  $desc = '\$comp_name="'.$import['comp_name'] . '";';
  $desc .= '\$comp_name_d="'.$import['comp_name_d'] . '";';
  $desc .= '\$comp_num="'.$import['comp_num'] . '";';
  $desc .= '\$comp_num_d="'.$import['comp_num_d'] . '";';
  $desc .= '\$comp_ad1="'.$import['comp_ad1'] . '";';
  $desc .= '\$comp_ad1_d="'.$import['comp_ad1_d'] . '";';
  $desc .= '\$comp_ad2="'.$import['comp_ad2'] . '";';
  $desc .= '\$comp_ad2_d="'.$import['comp_ad2_d'] . '";';
  $desc .= '\$comp_ad3="'.$import['comp_ad3'] . '";';
  $desc .= '\$comp_ad3_d="'.$import['comp_ad3_d'] . '";';
  $desc .= '\$comp_zip="'.$import['comp_zip'] . '";';
  $desc .= '\$comp_zip_d="'.$import['comp_zip_d'] . '";';
  $desc .= '\$comp_town="'.$import['comp_town'] . '";';
  $desc .= '\$comp_town_d="'.$import['comp_town_d'] . '";';
  $desc .= '\$comp_cdx="'.$import['comp_cdx'] . '";';
  $desc .= '\$comp_cdx_d="'.$import['comp_cdx_d'] . '";';
  $desc .= '\$comp_ctry="'.$import['comp_ctry'] . '";';
  $desc .= '\$comp_ctry_d="'.$import['comp_ctry_d'] . '";';
  $desc .= '\$comp_pho="'.$import['comp_pho'] . '";';
  $desc .= '\$comp_pho_d="'.$import['comp_pho_d'] . '";';
  $desc .= '\$comp_fax="'.$import['comp_fax'] . '";';
  $desc .= '\$comp_fax_d="'.$import['comp_fax_d'] . '";';
  $desc .= '\$comp_web="'.$import['comp_web'] . '";';
  $desc .= '\$comp_web_d="'.$import['comp_web_d'] . '";';
  $desc .= '\$comp_mail="'.$import['comp_mail'] . '";';
  $desc .= '\$comp_mail_d="'.$import['comp_mail_d'] . '";';
  $desc .= '\$comp_com="'.$import['comp_com'] . '";';
  $desc .= '\$comp_com_d="'.$import['comp_com_d'] . '";';
  $desc .= '\$con_ln="'.$import['con_ln'] . '";';
  $desc .= '\$con_ln_d="'.$import['con_ln_d'] . '";';
  $desc .= '\$con_fn="'.$import['con_fn'] . '";';
  $desc .= '\$con_fn_d="'.$import['con_fn_d'] . '";';
  $desc .= '\$con_lang="'.$import['con_lang'] . '";';
  $desc .= '\$con_lang_d="'.$import['con_lang_d'] . '";';
  $desc .= '\$con_func="'.$import['con_func'] . '";';
  $desc .= '\$con_func_d="'.$import['con_func_d'] . '";';
  $desc .= '\$con_tit="'.$import['con_tit'] . '";';
  $desc .= '\$con_tit_d="'.$import['con_tit_d'] . '";';
  $desc .= '\$con_ad1="'.$import['con_ad1'] . '";';
  $desc .= '\$con_ad1_d="'.$import['con_ad1_d'] . '";';
  $desc .= '\$con_ad2="'.$import['con_ad2'] . '";';
  $desc .= '\$con_ad2_d="'.$import['con_ad2_d'] . '";';
  $desc .= '\$con_ad3="'.$import['con_ad3'] . '";';
  $desc .= '\$con_ad3_d="'.$import['con_ad3_d'] . '";';
  $desc .= '\$con_zip="'.$import['con_zip'] . '";';
  $desc .= '\$con_zip_d="'.$import['con_zip_d'] . '";';
  $desc .= '\$con_town="'.$import['con_town'] . '";';
  $desc .= '\$con_town_d="'.$import['con_town_d'] . '";';
  $desc .= '\$con_cdx="'.$import['con_cdx'] . '";';
  $desc .= '\$con_cdx_d="'.$import['con_cdx_d'] . '";';
  $desc .= '\$con_ctry="'.$import['con_ctry'] . '";';
  $desc .= '\$con_ctry_d="'.$import['con_ctry_d'] . '";';
  $desc .= '\$con_pho="'.$import['con_pho'] . '";';
  $desc .= '\$con_pho_d="'.$import['con_pho_d'] . '";';
  $desc .= '\$con_hpho="'.$import['con_hpho'] . '";';
  $desc .= '\$con_hpho_d="'.$import['con_hpho_d'] . '";';
  $desc .= '\$con_mpho="'.$import['con_mpho'] . '";';
  $desc .= '\$con_mpho_d="'.$import['con_mpho_d'] . '";';
  $desc .= '\$con_fax="'.$import['con_fax'] . '";';
  $desc .= '\$con_fax_d="'.$import['con_fax_d'] . '";';
  $desc .= '\$con_mail="'.$import['con_mail'] . '";';
  $desc .= '\$con_mail_d="'.$import['con_mail_d'] . '";';
  $desc .= '\$con_mailok="'.$import['con_mailok'] . '";';
  $desc .= '\$con_mailok_d="'.$import['con_mailok_d'] . '";';
  $desc .= '\$con_com="'.$import['con_com'] . '";';
  $desc .= '\$con_com_d="'.$import['con_com_d'] . '";';

  $desc .= '\$comp["comp_name"]["value"] ="'.$import['comp_name'] . '";';
  $desc .= '\$comp["comp_name"]["label"] ="l_company";';
  $desc .= '\$comp["comp_name"]["default"]="'.$import['comp_name_d'] . '";';
  $desc .= '\$comp["comp_num"]["value"] ="'.$import['comp_num'] . '";';
  $desc .= '\$comp["comp_num"]["label"] ="l_number";';
  $desc .= '\$comp["comp_num"]["default"]="'.$import['comp_num_d'] . '";';
  $desc .= '\$comp["comp_ad1"]["value"] ="'.$import['comp_ad1'] . '";';
  $desc .= '\$comp["comp_ad1"]["label"] ="l_address";';
  $desc .= '\$comp["comp_ad1"]["default"]="'.$import['comp_ad1_d'] . '";';
  $desc .= '\$comp["comp_ad2"]["value"] ="'.$import['comp_ad2'] . '";';
  $desc .= '\$comp["comp_ad2"]["label"] ="l_address";';
  $desc .= '\$comp["comp_ad2"]["default"]="'.$import['comp_ad2_d'] . '";';
  $desc .= '\$comp["comp_ad3"]["value"] ="'.$import['comp_ad3'] . '";';
  $desc .= '\$comp["comp_ad3"]["label"] ="l_address";';
  $desc .= '\$comp["comp_ad3"]["default"]="'.$import['comp_ad3_d'] . '";';
  $desc .= '\$comp["comp_zip"]["value"] ="'.$import['comp_zip'] . '";';
  $desc .= '\$comp["comp_zip"]["label"] ="l_postcode";';
  $desc .= '\$comp["comp_zip"]["default"]="'.$import['comp_zip_d'] . '";';
  $desc .= '\$comp["comp_town"]["value"] ="'.$import['comp_town'] . '";';
  $desc .= '\$comp["comp_town"]["label"] ="l_town";';
  $desc .= '\$comp["comp_town"]["default"]="'.$import['comp_town_d'] . '";';
  $desc .= '\$comp["comp_cdx"]["value"] ="'.$import['comp_cdx'] . '";';
  $desc .= '\$comp["comp_cdx"]["label"] ="l_expresspostal";';
  $desc .= '\$comp["comp_cdx"]["default"]="'.$import['comp_cdx_d'] . '";';
  $desc .= '\$comp["comp_ctry"]["value"] ="'.$import['comp_ctry'] . '";';
  $desc .= '\$comp["comp_ctry"]["label"] ="l_country";';
  $desc .= '\$comp["comp_ctry"]["default"]="'.$import['comp_ctry_d'] . '";';
  $desc .= '\$comp["comp_pho"]["value"] ="'.$import['comp_pho'] . '";';
  $desc .= '\$comp["comp_pho"]["label"] ="l_phone";';
  $desc .= '\$comp["comp_pho"]["default"]="'.$import['comp_pho_d'] . '";';
  $desc .= '\$comp["comp_fax"]["value"] ="'.$import['comp_fax'] . '";';
  $desc .= '\$comp["comp_fax"]["label"] ="l_fax";';
  $desc .= '\$comp["comp_fax"]["default"]="'.$import['comp_fax_d'] . '";';
  $desc .= '\$comp["comp_web"]["value"] ="'.$import['comp_web'] . '";';
  $desc .= '\$comp["comp_web"]["label"] ="l_web";';
  $desc .= '\$comp["comp_web"]["default"]="'.$import['comp_web_d'] . '";';
  $desc .= '\$comp["comp_mail"]["value"] ="'.$import['comp_mail'] . '";';
  $desc .= '\$comp["comp_mail"]["label"] ="l_email";';
  $desc .= '\$comp["comp_mail"]["default"]="'.$import['comp_mail_d'] . '";';
  $desc .= '\$comp["comp_com"]["value"] ="'.$import['comp_com'] . '";';
  $desc .= '\$comp["comp_com"]["label"] ="l_comment";';
  $desc .= '\$comp["comp_com"]["default"]="'.$import['comp_com_d'] . '";';

  $desc .= '\$con["con_ln"]["value"] ="'.$import['con_ln'] . '";';
  $desc .= '\$con["con_ln"]["label"] ="l_lastname";';
  $desc .= '\$con["con_ln"]["default"]="'.$import['con_ln_d'] . '";';
  $desc .= '\$con["con_fn"]["value"] ="'.$import['con_fn'] . '";';
  $desc .= '\$con["con_fn"]["label"] ="l_firstname";';
  $desc .= '\$con["con_fn"]["default"]="'.$import['con_fn_d'] . '";';
  $desc .= '\$con["con_lang"]["value"] ="'.$import['con_lang'] . '";';
  $desc .= '\$con["con_lang"]["label"] ="l_lang";';
  $desc .= '\$con["con_lang"]["default"]="'.$import['con_lang_d'] . '";';
  $desc .= '\$con["con_func"]["value"] ="'.$import['con_func'] . '";';
  $desc .= '\$con["con_func"]["label"] ="l_function";';
  $desc .= '\$con["con_func"]["default"]="'.$import['con_func_d'] . '";';
  $desc .= '\$con["con_tit"]["value"] ="'.$import['con_tit'] . '";';
  $desc .= '\$con["con_tit"]["label"] ="l_title";';
  $desc .= '\$con["con_tit"]["default"]="'.$import['con_tit_d'] . '";';
  $desc .= '\$con["con_ad1"]["value"] ="'.$import['con_ad1'] . '";';
  $desc .= '\$con["con_ad1"]["label"] ="l_address";';
  $desc .= '\$con["con_ad1"]["default"]="'.$import['con_ad1_d'] . '";';
  $desc .= '\$con["con_ad2"]["value"] ="'.$import['con_ad2'] . '";';
  $desc .= '\$con["con_ad2"]["label"] ="l_address";';
  $desc .= '\$con["con_ad2"]["default"]="'.$import['con_ad2_d'] . '";';
  $desc .= '\$con["con_ad3"]["value"] ="'.$import['con_ad3'] . '";';
  $desc .= '\$con["con_ad3"]["label"] ="l_address";';
  $desc .= '\$con["con_ad3"]["default"]="'.$import['con_ad3_d'] . '";';
  $desc .= '\$con["con_zip"]["value"] ="'.$import['con_zip'] . '";';
  $desc .= '\$con["con_zip"]["label"] ="l_postcode";';
  $desc .= '\$con["con_zip"]["default"]="'.$import['con_zip_d'] . '";';
  $desc .= '\$con["con_town"]["value"] ="'.$import['con_town'] . '";';
  $desc .= '\$con["con_town"]["label"] ="l_town";';
  $desc .= '\$con["con_town"]["default"]="'.$import['con_town_d'] . '";';
  $desc .= '\$con["con_cdx"]["value"] ="'.$import['con_cdx'] . '";';
  $desc .= '\$con["con_cdx"]["label"] ="l_expresspostal";';
  $desc .= '\$con["con_cdx"]["default"]="'.$import['con_cdx_d'] . '";';
  $desc .= '\$con["con_ctry"]["value"] ="'.$import['con_ctry'] . '";';
  $desc .= '\$con["con_ctry"]["label"] ="l_country";';
  $desc .= '\$con["con_ctry"]["default"]="'.$import['con_ctry_d'] . '";';
  $desc .= '\$con["con_pho"]["value"] ="'.$import['con_pho'] . '";';
  $desc .= '\$con["con_pho"]["label"] ="l_phone";';
  $desc .= '\$con["con_pho"]["default"]="'.$import['con_pho_d'] . '";';
  $desc .= '\$con["con_hpho"]["value"] ="'.$import['con_hpho'] . '";';
  $desc .= '\$con["con_hpho"]["label"] ="l_hphone";';
  $desc .= '\$con["con_hpho"]["default"]="'.$import['con_hpho_d'] . '";';
  $desc .= '\$con["con_mpho"]["value"] ="'.$import['con_mpho'] . '";';
  $desc .= '\$con["con_mpho"]["label"] ="l_mphone";';
  $desc .= '\$con["con_mpho"]["default"]="'.$import['con_mpho_d'] . '";';
  $desc .= '\$con["con_fax"]["value"] ="'.$import['con_fax'] . '";';
  $desc .= '\$con["con_fax"]["label"] ="l_fax";';
  $desc .= '\$con["con_fax"]["default"]="'.$import['con_fax_d'] . '";';
  $desc .= '\$con["con_mail"]["value"] ="'.$import['con_mail'] . '";';
  $desc .= '\$con["con_mail"]["label"] ="l_email";';
  $desc .= '\$con["con_mail"]["default"]="'.$import['con_mail_d'] . '";';
  $desc .= '\$con["con_mailok"]["value"] ="'.$import['con_mailok'] . '";';
  $desc .= '\$con["con_mailok"]["label"] ="l_mailing_ok";';
  $desc .= '\$con["con_mailok"]["default"]="'.$import['con_mailok_d'] . '";';
  $desc .= '\$con["con_com"]["value"] ="'.$import['con_com'] . '";';
  $desc .= '\$con["con_com"]["label"] ="l_comment";';
  $desc .= '\$con["con_com"]["default"]="'.$import['con_com_d'] . '";';

  // User data categories handling
  if (is_array($cgp_user['company']['category'])) {
    foreach($cgp_user['company']['category'] as $cat_name => $one_cat) {
      $field = "comp_${cat_name}";
      $field_d = "comp_${cat_name}_d";

      $desc .= '\$'.$field.'="'.$import["$field"] . '";';
      $desc .= '\$'.$field_d.'="'.$import["$field_d"] . '";';

      $desc .= '\$comp["'.$field.'"]["value"] ="'.$import["$field"] . '";';
      $desc .= '\$comp["'.$field.'"]["label"] = "l_'.$cat_name.'";';
      $desc .= '\$comp["'.$field.'"]["default"]="'.$import["$field_d"] . '";';
    }
  }
  if (is_array($cgp_user['contact']['category'])) {
    foreach($cgp_user['contact']['category'] as $cat_name => $one_cat) {
      $field = "con_${cat_name}";
      $field_d = "con_${cat_name}_d";

      $desc .= '\$'.$field.'="'.$import["$field"] . '";';
      $desc .= '\$'.$field_d.'="'.$import["$field_d"] . '";';

      $desc .= '\$con["'.$field.'"]["value"] ="'.$import["$field"] . '";';
      $desc .= '\$con["'.$field.'"]["label"] = "l_'.$cat_name.'";';
      $desc .= '\$con["'.$field.'"]["default"]="'.$import["$field_d"] . '";';
    }
  }
  
  return $desc;
}


///////////////////////////////////////////////////////////////////////////////
// Get fields max size (hard coded, must be similar to database field length)
// returns : $field_size hash : $field_size['comp_name'] = 96
///////////////////////////////////////////////////////////////////////////////
function get_import_field_size() {

  $fsize = '';
  $fsize['comp_name'] = 96;
  $fsize['comp_num'] = 32;
  $fsize['comp_ad1'] = 64;
  $fsize['comp_ad2'] = 64;
  $fsize['comp_ad3'] = 64;
  $fsize['comp_zip'] = 14;
  $fsize['comp_town'] = 64;
  $fsize['comp_cdx'] = 16;
  $fsize['comp_ctry'] = 2;
  $fsize['comp_pho'] = 32;
  $fsize['comp_fax'] = 32;
  $fsize['comp_web'] = 64;
  $fsize['comp_mail'] = 64;
  $fsize['comp_com'] = 256;

  $fsize['con_ln'] = 64;
  $fsize['con_fn'] = 64;
  $fsize['con_func'] = 64;
  $fsize['con_tit'] = 64;
  $fsize['con_ad1'] = 64;
  $fsize['con_ad2'] = 64;
  $fsize['con_ad3'] = 64;
  $fsize['con_zip'] = 14;
  $fsize['con_town'] = 64;
  $fsize['con_cdx'] = 16;
  $fsize['con_ctry'] = 2;
  $fsize['con_pho'] = 32;
  $fsize['con_hpho'] = 32;
  $fsize['con_mpho'] = 32;
  $fsize['con_fax'] = 32;
  $fsize['con_mail'] = 128;
  $fsize['con_com'] = 256;

  return $fsize;
}


///////////////////////////////////////////////////////////////////////////////
// Stores in $list hash, List parameters transmited
// returns : $list hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_import_params() {
  global $tmp_path, $action;

  // Get global params
  $params = get_global_params('Import');

  if (($action == 'file_import')  && ($params['file'] != '')
      && ($params['file_saved'] == '')) {
    $file_saved = $tmp_path . '/' . $params['file_name'];
    copy ($params['file'], $file_saved);
    $params['file_saved'] = $file_saved;
  }

  // Run mode
  if ($action == 'file_test') {
    $params['run_mode'] = 'test';
  } else if ($action == 'file_import') {
    if (isset ($params['auto_mode'])) {
      $params['run_mode'] = 'auto';
    } else {
      $params['run_mode'] = 'run';
    }
  }

  return $params;
}


//////////////////////////////////////////////////////////////////////////////
// Import actions
//////////////////////////////////////////////////////////////////////////////
function get_import_action() {
  global $params, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_import,$l_header_display;
  global $l_header_consult, $l_header_add_contact;
  global $l_select_list, $l_add_contact;
  global $cright_read_admin, $cright_write_admin;

// Index
  $actions['import']['index'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/import/import_index.php?action=index",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                    );

// Search
  $actions['import']['search'] = array (
    'Url'      => "$path/import/import_index.php?action=search",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('None') 
                                      );

// New
  $actions['import']['new'] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/import/import_index.php?action=new",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('','search','index','detailconsult','admin','display') 
                                  );
// Detail Consult
  $actions['import']['detailconsult'] = array (
     'Name'     => $l_header_consult,
     'Url'      => "$path/import/import_index.php?action=detailconsult&amp;import_id=".$params['import_id'],
    'Right'    => $cright_read_admin,
    'Condition'=> array ('detailupdate', 'file_sample', 'file_test', 'file_import') 
                                      );

// Detail Update
  $actions['import']['detailupdate'] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/import/import_index.php?action=detailupdate&amp;import_id=".$params['import_id'],
     'Right'    => $cright_write_admin,
     'Condition'=> array ('detailconsult', 'update', 'file_sample', 'file_test') 
                                           );

// Insert
  $actions['import']['insert'] = array (
    'Url'      => "$path/import/import_index.php?action=insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                      );

// Update
  $actions['import']['update'] = array (
    'Url'      => "$path/import/import_index.php?action=update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                      );

// Check Delete
  $actions['import']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/import/import_index.php?action=check_delete&amp;import_id=".$params['import_id'],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'update') 
                                           );

// Delete
  $actions['import']['delete'] = array (
    'Url'      => "$path/import/import_index.php?action=delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                      );

// Sample File
  $actions['import']['file_sample'] = array (
    'Url'      => "$path/import/import_index.php?action=file_sample&amp;import_id=".$params['import_id'],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                      );

// Test File
  $actions['import']['file_test'] = array (
    'Url'      => "$path/import/import_index.php?action=file_test&amp;import_id=".$params['import_id'],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                      );

// Import File
  $actions['import']['file_import'] = array (
    'Url'      => "$path/import/import_index.php?action=file_import&amp;import_id=".$params['import_id'],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                      );

}

?>
