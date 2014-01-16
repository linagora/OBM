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
// OBM - File : php/campaign/campaign_index.php
//     - Desc : campaign Index File
// 2008-02-11 Christophe Liou Kee On
///////////////////////////////////////////////////////////////////////////////
// $Id:  $ //
///////////////////////////////////////////////////////////////////////////////
// Actions :

// TODO put me in globals
$c_campaign_status_enum = array (
  'created' => 0,
  'ready' => 1,
  'running' => 2,
  'planified' => 3,
  'finished' => 4,
  'archived' => 5,
  'error_mail_format' => 6,
  'error_target' => 7,
  'error' => 8,
);

$c_campaign_entity_group = 1;
$c_campaign_entity_user = 2;
$c_campaign_entity_list = 3;
$c_campaign_entity_contact = 4;


$path = '..';
$module = 'campaign';

$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == "") $obminclude = 'obminclude';
include("$obminclude/global.inc");
require('campaign_query.inc');
$params = get_campaign_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");

require_once("$obminclude/of/of_category.inc");

require('campaign_display.inc');
require('campaign_js.inc');

get_campaign_action();

if ($action == $_SESSION[previous_action]
  && ($action == 'new_subcampaign' || $action == 'duplicate')) {
    $action = 'search';
  }

$_SESSION['previous_action'] = $action;

$perm->check_permissions($module, $action);
update_last_visit('campaign', $params['campaign_id'], $action);

page_close();


if ($action == 'archive') {
  ///////////////////////////////////////////////////////////////////////////////
  run_query_campaign_archive($params['campaign_id']);
  $action = 'detailconsult';

} else if ($action == 'unarchive') {
  ///////////////////////////////////////////////////////////////////////////////
  run_query_campaign_unarchive($params['campaign_id']);
  $action = 'detailconsult';

} else if ($action == 'insert') {
  ///////////////////////////////////////////////////////////////////////////////
  $insert_cols = array('name','start_date','end_date','manager_id','email','objective','comment');
  if (check_campaign_form("", $params, $insert_cols)) {
    $params['campaign_id'] = run_query_campaign_insert($params, $insert_cols);
    if ($params['campaign_id']) {
      $display['msg'] .= display_ok_msg($l_insert_ok);
      $action = 'detailconsult';
      $force_action_update = true;

    } else {
      $display['msg'] .= display_err_msg($l_insert_error);
      $action = 'new';
    }
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $action = 'new';
  }

} else if ($action == 'update') {
  ///////////////////////////////////////////////////////////////////////////////
  $update_cols =  array('name','start_date','end_date','manager_id','email','objective','comment');
  if (check_campaign_form("", $params, $update_cols)) {
    $ret = run_query_campaign_update($params, $update_cols);
    if ($ret) {
      $display['msg'] .= display_ok_msg($l_update_ok);
    } else {
      $display['msg'] .= display_err_msg($l_update_error);
    }
    $action = 'detailconsult';
  } else {
    $display['msg'] .= display_err_msg($l_invalid_da. " : " . $err['msg']);
    $action = 'detailupdate';
  }

} else if ($action == 'duplicate') {
  ///////////////////////////////////////////////////////////////////////////////
  $params['campaign_id'] = run_query_campaign_duplicate($params['campaign_id']);
  $action = 'search';
  $force_action_update = true;

} else if ($action == 'new_subcampaign') {
  ///////////////////////////////////////////////////////////////////////////////
  $params['campaign_id'] = run_query_campaign_insert(array('parent' => $params['campaign_id']),
    array('parent'));

  $action = 'detailupdate';

} else if ($action == 'add_list_target') {
  ///////////////////////////////////////////////////////////////////////////////
  run_query_campaign_target_insert('List', $params['ext_id'], $params['multiple_selection']);
  run_query_campaign_update(array('campaign_id' => $params['ext_id']), array());

  $action = 'detailconsult';

} else if ($action == 'add_group_target') {
  ///////////////////////////////////////////////////////////////////////////////
  run_query_campaign_target_insert('Group', $params['ext_id'], $params['multiple_selection']);
  run_query_campaign_update(array('campaign_id' => $params['ext_id']), array());

  $action = 'detailconsult';

} else if ($action == 'add_user_target') {
  ///////////////////////////////////////////////////////////////////////////////
  run_query_campaign_target_insert('User', $params['ext_id'], $params['multiple_selection']);
  run_query_campaign_update(array('campaign_id' => $params['ext_id']), array());

  $action = 'detailconsult';

} else if ($action == 'del_target') {
  ///////////////////////////////////////////////////////////////////////////////
  run_query_campaign_target_delete($params['campaign_id'], $params['multiple_selection']);
  run_query_campaign_update(array('campaign_id' => $params['campaign_id']), array());

  $action = 'detailconsult';

} else if ($action == 'delete') {
  ///////////////////////////////////////////////////////////////////////////////
  if (check_campaign_can_delete($params['campaign_id'])) {
    $ret = run_query_campaign_delete($params['campaign_id']);
    if ($ret) {
      $display['msg'] .= display_ok_msg($l_delete_ok);
    } else {
      $display['msg'] .= display_err_msg($l_delete_error);
    }
    $action = 'search';
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $action = 'detailconsult';
  }

} else if ($action == 'check_delete') {
  ///////////////////////////////////////////////////////////////////////////////
  if (check_campaign_can_delete($params['campaign_id'])) {
    $display['msg'] .= display_info_msg($ok_msg, false);
    $display['detail'] = dis_campaign_can_delete($params['campaign_id']);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $action = 'detailconsult';
  }
}

update_campaign_action($force_action_update);




// -------------------------------------------------------------------------
// SECOND PART - MAIN DISPLAYING ACTIONS
//

if (false) {

} else if ($action == 'dispref_display') {
  ///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm["uid"], "campaign", 1);
  //~ $prefs_u = get_display_pref($obm["uid"], "campaign_user", 1);
  $display['detail'] = dis_campaign_display_pref($prefs);

} else if ($action == 'dispref_level') {
  ///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm["uid"], "campaign", 1);
  //~ $prefs_u = get_display_pref($obm["uid"], "campaign_user", 1);
  $display['detail'] = dis_campaign_display_pref($prefs);

} else if ($action == 'ext_get_ids') {
  ///////////////////////////////////////////////////////////////////////////////
  // External calls (main menu not displayed)                                  //
  ///////////////////////////////////////////////////////////////////////////////

  $display['search'] = dis_campaign_search_form($params);
  if ($_SESSION['set_display'] == "yes") {
    $display["result"] = dis_campaign_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} else if ($action == 'ext_get_id') {
  ///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_campaign_search_form($params);
  if ($_SESSION['set_display'] == "yes") {
    $display["result"] = dis_campaign_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} else if ($action == 'search') {
  ///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_campaign_search_form($params);
  $display['result'] .= dis_campaign_search_list($params);

} else if ($action == 'index' || $action == '') {
  ///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_campaign_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] .= dis_campaign_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} else if ($action == 'detailconsult') {
  ///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_campaign_consult($params);

} else if ($action == 'new') {
  ///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_campaign_form('insert', $params);

} else if ($action == 'detailupdate') {
  ///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_campaign_form('update',$params);

} else if ($action == 'admin') {
  ///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_campaign_admin_index();

} else if ($action == 'display') {
  ///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm["uid"], 'campaign', 1);
  $display['detail'] = dis_campaign_display_pref($prefs);

//} else if ($action == 'monitor') {
//  ///////////////////////////////////////////////////////////////////////////////
//  run_query_campaign_import_sent_emails();
//  $display['detail'] = dis_campaign_monitor($params);

} else if ($action == 'test_module_admin') {
  ///////////////////////////////////////////////////////////////////////////////
  require('campaign_test.inc');

  test_campaign_module_admin();
}

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////

$display["head"] = display_head($l_campaign);
if (! $params["popup"]) {
  $display["header"] = display_menu($module);
}
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $params hash, campaign parameters transmited
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_campaign_params() {
  global $actions;

  // Get global params
  $params = get_global_params("campaign");

  $params['multiple_selection'] = array();

  foreach ($params as $k => $v) {
    if (preg_match('/^data-.*-((user|group|list)?[0-9]+)$/', $k, $matches)) {
      $params['multiple_selection'][] = $matches[1];
    }
  }

  if (isset($params['ext_id']))
    $params['campaign_id'] = $params['ext_id'];

  if (isset($params['campaign_id'])) {
    $params['campaign_id'] = $params['campaign_id'] +0;
  }

  if (isset($_FILES['fi_email']) && $_FILES['fi_email']['size'] != 0) {
    $params['fi_email']['file_tmp'] = $_FILES['fi_email']['tmp_name'];
    $params['fi_email']['file_name'] = $_FILES['fi_email']['name'];
    $params['fi_email']['size'] = $_FILES['fi_email']['size'];
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// campaign Action
///////////////////////////////////////////////////////////////////////////////
function get_campaign_action() {
  global $params, $actions, $path, $action;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

  $lang = &$GLOBALS;
  //$campaign_index = "$path/campaign/campaign_index.php";
  $campaign_index = '';

  $actions['campaign']['dispref_display'] = array (
    'Url'      => "${campaign_index}?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('none') 
  );

  $actions['campaign']['dispref_level'] = array (
    'Url'      => "${campaign_index}?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('none') 
  );

  $actions['campaign']['ext_get_ids'] = array (
    'Url'      => "${campaign_index}?action=ext_get_ids",
    'Right'    => $cright_read,
    'Condition'=> array ('none') 
  );

  $actions['campaign']['insert'] = array (
    'Url'      => "${campaign_index}?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('none') 
  );

  $actions['campaign']['ext_get_id'] = array (
    'Url'      => "${campaign_index}?action=ext_get_id",
    'Right'    => $cright_read,
    'Condition'=> array ('none') 
  );

  $actions['campaign']['update'] = array (
    'Url'      => "${campaign_index}?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('none') 
  );

  $actions['campaign']['search'] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('none') 
  );

  $actions['campaign']['index'] = array (
    'Name'     => $lang['l_header_find'], 
    'Url'      => "${campaign_index}?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
  );

  $actions['campaign']['new'] = array (
    'Name'     => $lang['l_header_new'], 
    'Url'      => "${campaign_index}?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
  );

  $actions['campaign']['detailconsult'] = array (
    'Name'     => $lang['l_header_consult'], 
    'Url'      => "${campaign_index}?action=detailconsult&amp;campaign_id=$params[campaign_id]",
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate', 'monitor') 
  );

  $actions['campaign']['delete'] = array (
    'Url'      => "${campaign_index}?action=delete&amp;campaign_id=$params[campaign_id]",
    'Right'    => $cright_write,
    'Condition'=> array ('none') 
  );

  $actions['campaign']['detailupdate'] = array (
    'Name'     => $lang['l_header_update'], 
    'Url'      => "${campaign_index}?action=detailupdate&amp;campaign_id=$params[campaign_id]",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
  );

  $actions['campaign']['check_delete'] = array (
    'Name'     => $lang['l_header_delete'], 
    'Url'      => "${campaign_index}?action=check_delete&amp;campaign_id=$params[campaign_id]",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult','detailupdate') 
  );

  $actions['campaign']['duplicate'] = array (
    'Name'     => $lang['l_header_duplicate'], 
    'Url'      => "${campaign_index}?action=duplicate&amp;campaign_id=$params[campaign_id]",
    'Right'    => $cright_write,
    'Condition'=> array('detailconsult')
  );

  $actions['campaign']['del_target'] = array (
    'Privacy'  => true,
    'Right'    => $cright_write,
    'Condition'=> array ('none') 
  );

  $actions['campaign']['add_list_target'] = array (
    'Name'     => "$lang[l_add] $lang[l_listss]",
    'Url'      => "$path/list/list_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title="
    .urlencode($lang['l_add_lists'])."&amp;ext_action=add_list_target&amp;ext_url="
    .urlencode($path."/campaign/campaign_index.php")."&amp;ext_id="
    .$params['campaign_id']."&amp;ext_target=Lists",
    'Popup'    => 1,
    'Target'   => $lang['l_listes'],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
  );

// TODO: disabled because the sendCampaign.pl script doesn't care about
//  $actions['campaign']['add_user_target'] = array (
//    'Name'     => "$lang[l_add] $lang[l_userss]",
//    'Url'      => "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title="
//    .urlencode($lang['l_add_users'])."&amp;ext_action=add_user_target&amp;ext_url="
//    .urlencode($path."/campaign/campaign_index.php")."&amp;ext_id=".$params['campaign_id']
//    ."&amp;ext_target=Users",
//    'Popup'    => 1,
//    'Target'   => $lang['l_users'],
//    'Right'    => $cright_write,
//    'Condition'=> array ('detailconsult') 
//  );
//
//  $actions['campaign']['add_group_target'] = array (
//    'Name'     => "$lang[l_add] $lang[l_groupss]",
//    'Url'      => "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title="
//    .urlencode($lang['l_add_groups'])."&amp;ext_action=add_group_target&amp;ext_url="
//    .urlencode($path."/campaign/campaign_index.php")."&amp;ext_id=".$params['campaign_id']
//    ."&amp;ext_target=Groups",
//    'Popup'    => 1,
//    'Target'   => $lang['l_groups'],
//    'Right'    => $cright_write,
//    'Condition'=> array ('detailconsult') 
//  );

  $actions['campaign']['new_subcampaign'] = array (
    'Name'     => "$lang[l_header_new] $lang[l_subcampaign]", 
    'Url'      => "${campaign_index}?action=new_subcampaign&amp;campaign_id=$params[campaign_id]",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
  );

  $actions['campaign']['admin'] = array (
    'Name'     => $lang['l_header_admin'], 
    'Url'      => "${campaign_index}?action=admin",
    'Right'    => $cright_write,
    'Condition'=> array ('none') 
  );

  $actions['campaign']['test_module_admin'] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('none') 
  );

  $actions['campaign']['display'] = array (
    'Name'     => $lang['l_header_display'], 
    'Url'      => "${campaign_index}?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('index','search') 
  );

  $actions['campaign']['archive'] = array (
    'Name'     => $lang['l_campaign_archive'], 
    'Url'      => "${campaign_index}?action=archive&amp;campaign_id=$params[campaign_id]",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
  );

  $actions['campaign']['unarchive'] = array (
    'Name'     => $lang['l_campaign_unarchive'], 
    'Url'      => "${campaign_index}?action=unarchive&amp;campaign_id=$params[campaign_id]",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
  );


  // TODO in developement

//  $actions['campaign']['monitor'] = array (
//    'Name'     => 'Monitor', //$lang['l_monitor'], 
//    'Url'      => "${campaign_index}?action=monitor&amp;campaign_id=$params[campaign_id]",
//    'Right'    => $cright_read,
//    //'Condition'=> array ('none') ,
//    'Condition'=> array ('detailconsult'),
//  );


  if (isset($params['campaign_id'])) {
    $params['campaign_q'] = run_query_campaign_detail($params['campaign_id']);

    if (!$params['campaign_q']) {
      $action == 'search';
      $display['msg'] .= display_err_msg($GLOBALS['l_campaign_not_found']);

    } else {
      if (!can_update_campaign($params['campaign_q'])) {
        $actions['campaign']['detailupdate']['Condition'] = array('none');
        $actions['campaign']['add_group_target']['Condition'] = array('none');
        $actions['campaign']['add_user_target']['Condition'] = array('none');
        $actions['campaign']['add_list_target']['Condition'] = array('none');
      }

      if ($params['campaign_q']->f('campaign_status') != $GLOBALS['c_campaign_status_enum']['finished'])
        $actions['campaign']['archive']['Condition'] = array('none');

      if ($params['campaign_q']->f('campaign_status') != $GLOBALS['c_campaign_status_enum']['archived'])
        $actions['campaign']['unarchive']['Condition'] = array('none');
    }
  }

  of_category_user_module_action("campaign");
}


///////////////////////////////////////////////////////////////////////////////
// campaign Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_campaign_action($force_action_update = false) {
  global $params, $actions, $path;
  global $cright_write_admin;

  global $c_campaign_status_enum;

  if ($force_action_update) {
    get_campaign_action();
  }

  /*
  $actions['campaign']['detailconsult']['Url'] = "$path/campaign/campaign_index.php?action=detailconsult&amp;campaign_id=$params[campaign_id]";
  $actions['campaign']['delete']['Url'] = "$path/campaign/campaign_index.php?action=delete&amp;campaign_id=$params[campaign_id]";
  $actions['campaign']['detailupdate']['Url'] = "$path/campaign/campaign_index.php?action=detailupdate&amp;campaign_id=$params[campaign_id]";
  $actions['campaign']['check_delete']['Url'] = "$path/campaign/campaign_index.php?action=check_delete&amp;campaign_id=$params[campaign_id]";
  $actions['campaign']['archive']['Url'] = "$path/campaign/campaign_index.php?action=archive&amp;campaign_id=$params[campaign_id]";
  $actions['campaign']['unarchive']['Url'] = "$path/campaign/campaign_index.php?action=unarchive&amp;campaign_id=$params[campaign_id]";
   */
}

?>
