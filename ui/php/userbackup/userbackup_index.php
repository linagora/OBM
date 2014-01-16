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
// OBM - File : userbackup_index.php                                         //
//     - Desc : User Backup Index File                                       //
// 2012-07-17 Alexis Gavoty                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


$debug = 1;
$path = '..';
$module = 'userbackup';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';

include_once("$obminclude/global.inc");

$params = get_global_params('Entity');
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
$params = get_user_params();
include_once("$obminclude/global_pref.inc");
require_once('../profile/profile_query.inc');
require_once('userbackup_display.inc');
require_once('../user/user_query.inc');
require_once("$path/../app/default/models/UserPattern.php");
require_once("$obminclude/of/of_category.inc");
require_once("$obminclude/of/of_right.inc"); // needed by call from calendar
require_once("$obminclude/of/of_contact.php");

if ($action == 'index') $action = 'backup';

get_user_action();
$perm->check_permissions($module, $action);

update_last_visit('user', $params['user_id'], $action);

page_close();

$params['user_id'] = $obm['uid'];

if ($action == 'backup') {
  // lang file include for backup
  $lang = strtolower(get_lang());
  include_once("obminclude/lang/$lang/backup.inc");
  try {
    $backup = new Backup('user', $params['user_id']);
    $dis_form = true;

    $options = array();
    if (!empty($params['retrieveAll'])) {
      $backup->retrieveBackups($options);
      $display['msg'] .= display_ok_msg($l_retrieve_from_ftp_success);
    } elseif (!empty($params['execute']) && Perm::check_right('userbackup', $cright_write_admin)) {
      $result = $backup->doBackup($options);
      $display['msg'] .= display_ok_msg($l_backup_complete);
      if (!$result['pushFtp']['success']) {
        $display['msg'] .= display_warn_msg($l_push_backup_ftp_failed.' ('.$result['pushFtp']['msg'].')');
      }
    }
  } catch (Exception $e) {
    $display['msg'] .= display_err_msg($e->getMessage());
  }

  if ($dis_form) {
    $display['detail'] = dis_user_backup_form($backup, $params);
  }

} elseif ($action == 'restore') {
///////////////////////////////////////////////////////////////////////////////
  // lang file include for backup
  $lang = strtolower(get_lang());
  include_once("obminclude/lang/$lang/backup.inc");
  include_once("obminclude/lang/$lang/contact.inc");
  try {
    $backup = new Backup('user', $params['user_id']);
    $dis_form = true;

    if (!empty($params['execute'])) {
      $filename = $params['filename'];
      $method = $params['method'];
      $options = array();
      $backup->doRestore($filename, $method, $options);
      $display['msg'] .= display_ok_msg($l_restore_complete);
    }
  } catch (Exception $e) {
    $display['msg'] .= display_err_msg($e->getMessage());
  }

  if ($dis_form) {
    $display['detail'] = dis_user_backup_form($backup, $params);
  }

}

of_category_user_action_switch($module, $action, $params);

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_user);
if (! $params['popup']) {
  update_user_action();
  $display['header'] = display_menu($module);
}
$display['end'] = display_end();

display_page($display);
        
///////////////////////////////////////////////////////////////////////////////
// Stores User parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_user_params() {
  
  // Get global params
  $params = get_global_params('UserObm');

  if (isset($params)) {
    $nb_group = 0;
    while ( list( $key ) = each($params) ) {
      if (strcmp(substr($key, 0, 11),'data-group-') == 0) {
        $nb_group++;
        $group_num = substr($key, 11);
        $params["group_$nb_group"] = $group_num;
      }
    }
    $params['group_nb'] = $nb_group;
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// User Action 
///////////////////////////////////////////////////////////////////////////////
function get_user_action() {
  global $actions, $cright_read, $cright_write;

  
  of_category_user_module_action('userbackup');

// Backup
  $actions['userbackup']['backup'] = array (
    'Name'     => $GLOBALS['l_header_backup_restore'],
    'Url'      => "$path/userbackup/userbackup_index.php?action=backup",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

// Restore
  $actions['userbackup']['restore'] = array (
    'Name'     => $GLOBALS['l_header_backup_restore'],
    'Url'      => "$path/userbackup/userbackup_index.php?action=restore",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
  );
}



///////////////////////////////////////////////////////////////////////////////
// User Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////

function update_user_action() {
  global $params, $actions, $path, $cgp_user, $obm, $profiles;

  $id = $params['user_id'];
  if ($id > 0) {
    $u = get_user_info($id);
    if (Perm::user_can_update_peer($obm['uid'], $profiles[$obm['profile']], $id, $profiles[$u['perms']])
        && check_user_update_rights($params, $u)) {

      // Backup
      $actions['userbackup']['backup']['Url'] = "$path/userbackup/userbackup_index.php";
      $actions['userbackup']['backup']['Condition'][] = 'insert';

      // Restore
      $actions['userbackup']['restore']['Url'] = "$path/userbackup/userbackup_index.php?action=restore";
      $actions['userbackup']['restore']['Condition'][] = 'insert';
    } else {
      $actions['userbackup']['backup']['Condition'] = array('None');
      $actions['userbackup']['restore']['Condition'] = array('None');
    }
  }

  // Display admin menu only if no userdata defined
  if (empty($cgp_user['user']['category'])) {
    $actions['userbackup']['admin']['Condition'] = array('None');
  }

  if (!check_user_wait($params)) {
    $actions['userbackup']['wait']['Condition'] = array('None');
  }
}

?>
