<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

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
$module = 'user';
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


get_user_action();
$perm->check_permissions($module, $action);

update_last_visit('user', $params['user_id'], $action);

page_close();

// get Profile list (name and id)
// $params['profiles'] = get_all_profiles(false);

if ($action == 'backup') {
///////////////////////////////////////////////////////////////////////////////
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
    } elseif (!empty($params['execute'])) {
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
$extra_js_include[] = 'user.js';
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
  if(isset($params['exp_op']))  $params['exp_op'] = urldecode($params['exp_op']);
  if(isset($params['quota_op']))  $params['quota_op'] = urldecode($params['quota_op']);
  if (isset ($_FILES['fi_file'])) {
    $params['file_tmp'] = $_FILES['fi_file']['tmp_name'];
    $params['file_name'] = $_FILES['fi_file']['name'];
    $params['size'] = $_FILES['fi_file']['size'];
    $params['type'] = $_FILES['fi_file']['type'];
  }

  if(isset ($params['vacation_datebegin'])) {
    $params['vacation_datebegin'] = of_isodate_convert($params['vacation_datebegin']);
    $params['vacation_datebegin'] = new Of_Date($params['vacation_datebegin']);
    $params['vacation_datebegin']->setHour($params["time_begin"])->setMinute($params["min_begin"])->setSecond(0);
  }

  if(isset ($params['vacation_dateend'])) {
    $params['vacation_dateend'] = of_isodate_convert($params['vacation_dateend']);
    $params['vacation_dateend'] = new Of_Date($params['vacation_dateend']);
    $params['vacation_dateend']->setHour($params["time_end"])->setMinute($params["min_end"])->setSecond(0);
  } 

  if (is_array($params['email_nomade'])) {
    $email_aliases = array();
    while(!empty($params['email_nomade'])) {
      $email = trim(array_shift($params['email_nomade']));
      if(!empty($email)) {
        $email_aliases[] = $email;
      }
    }

    $params['email_nomade'] = implode("\r\n", $email_aliases);
  }

  if (is_array($params['email'])) {
    $email_aliases = array();
    while(!empty($params['email'])) {
      $email = trim(array_shift($params['email']));
      $domain = array_shift($params['aliases']);
      if(!empty($email)) {
       if(!empty($domain)) {
          $email_aliases[] = $email.'@'.$domain;
        } else {
          $email_aliases[] = $email;
        }
      }
    }

    $params['email'] = implode("\r\n", $email_aliases);
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// User Action 
///////////////////////////////////////////////////////////////////////////////
function get_user_action() {
  global $params, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display,$l_header_admin,$l_header_import;
  global $l_header_upd_group,$l_header_admin, $l_header_reset, $l_header_batch;
  global $l_header_wait;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;
  global $cright_none;
  
  of_category_user_module_action('userbackup');

// Backup
  $actions['user']['backup'] = array (
    'Name'     => $GLOBALS['l_header_backup_restore'],
    'Url'      => "$path/userbackup/userbackup_index.php?action=backup&amp;user_id=".$params['user_id'],
    // 'Right'    => $cright_write_admin,
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'reset', 'group_consult', 'group_update','check_delete', 'backup', 'restore') 
  );

// Restore
  $actions['user']['restore'] = array (
    'Name'     => $GLOBALS['l_header_backup_restore'],
    'Url'      => "$path/userbackup/userbackup_index.php?action=restore&amp;user_id=".$params['user_id'],
    // 'Right'    => $cright_write_admin,
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
      $actions['user']['backup']['Url'] = "$path/userbackup/userbackup_index.php?action=backup&amp;user_id=$id";
      $actions['user']['backup']['Condition'][] = 'insert';

      // Restore
      $actions['user']['restore']['Url'] = "$path/userbackup/userbackup_index.php?action=restore&amp;user_id=$id";
      $actions['user']['restore']['Condition'][] = 'insert';
    } else {
      $actions['user']['group_consult']['Condition'] = array('None');
      $actions['user']['check_delete']['Condition'] = array('None');
      $actions['user']['detailupdate']['Condition'] = array('None');
      $actions['user']['detailconsult']['Condition'] = array('None');
      $actions['user']['backup']['Condition'] = array('None');
      $actions['user']['restore']['Condition'] = array('None');
    }
  }

  // Display admin menu only if no userdata defined
  if (empty($cgp_user['user']['category'])) {
    $actions['user']['admin']['Condition'] = array('None');
  }

  if (!check_user_wait($params)) {
    $actions['user']['wait']['Condition'] = array('None');
  }
}

?>
