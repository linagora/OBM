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
// OBM - File : user_index.php                                               //
//     - Desc : User Index File                                              //
// 2000-01-13 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the user search form
// - search          -- search fields  -- show the result set of search
// - new             --                -- show the new user form
// - detailconsult   -- $user_id       -- show the user detail
// - detailupdate    -- $user_id       -- show the user detail form
// - insert          -- form fields    -- insert the user
// - reset           -- $user_id       -- reset user preferences
// - update          -- form fields    -- update the user
// - check_delete    -- $user_id       -- check links before delete
// - delete          -- $user_id    -- delete the user
// - group_consult   -- $user_id    -- show the user groups form
// - group_update    -- $user_id    -- update the user groups
// - batch_process   --             -- Batch processing for users
// External API ---------------------------------------------------------------
// - ext_get_ids     --                -- select multiple users (return id) 
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
require_once('user_display.inc');
require_once('user_query.inc');
require_once('user_js.inc');
require_once("$path/../app/default/models/UserPattern.php");
require_once("$obminclude/of/of_category.inc");
require_once("$obminclude/of/of_right.inc"); // needed by call from calendar
require_once("$obminclude/of/of_contact.php");


// detailconsult can be accessed without user_id (-> display current user)
if (($action == 'detailconsult') && (! $params['user_id'])) $params['user_id'] = $obm['uid'];

get_user_action();
$perm->check_permissions($module, $action);

update_last_visit('user', $params['user_id'], $action);

page_close();

// get Profile list (name and id)
$params['profiles'] = get_all_profiles(false);

$user_results_limit = get_user_results_limit($obm['profile']);
$params = get_user_params_mail_server_id($params);
///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == 'ext_get_ids') {
  $display['search'] = html_user_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_user_search_list($params, $user_results_limit);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} elseif ($action == 'ext_get_id') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = html_user_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_user_search_list($params, $user_results_limit);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} elseif ($action == 'index' || $action == '') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = html_user_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_user_search_list($params, $user_results_limit);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} elseif ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = html_user_search_form($params);
  $display['result'] = dis_user_search_list($params, $user_results_limit);

} elseif ($action == 'ext_search') {
///////////////////////////////////////////////////////////////////////////////
  // Makes it possible to process other requests in parallel, do not remove
  session_write_close();
  $user_q = run_query_user_ext_search($params);
  json_search_users($params, $user_q);
  echo '('.$display['json'].')';
  exit();

} elseif ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_user_form($action, $params);

} elseif ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_user_consult($params);

} elseif ($action == 'wait') {
///////////////////////////////////////////////////////////////////////////////
  $display['result'] = dis_user_wait_list($params);

} elseif ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_update_rights($params)) {
    $display['detail'] = dis_user_form($action, $params);
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
    $display['detail'] = dis_user_consult($params);
  }

} elseif ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////

  if (check_user_defined_rules() && check_user_data_form('', $params)) {

    // If the context (same user) was confirmed ok, we proceed
    if ($params['confirm'] == $c_yes) {
      $cid = run_query_user_insert($params);
      if ($cid > 0) {
        $params['user_id'] = $cid;
        set_update_state();
        $display['msg'] .= display_ok_msg("$l_user : $l_insert_ok");
        $display['msg'] .= display_ok_msg("<input type='button' onclick=\"window.location='$path/user/user_index.php?action=pdf&user_id=$params[user_id]'\" value=\"$l_download_user_card\" />", false);
        $display['detail'] = dis_user_consult($params);
      } else {
        $display['msg'] .= display_err_msg("$l_user : $l_insert_error");
        $display['detail'] = dis_user_form($action, $params);
      }

    // If it is the first try, we warn the user if some user seem similar
    } else {
      $obm_q = check_user_context('', $params);
      if ($obm_q->num_rows() > 0) {
        $display['detail'] = dis_user_warn_insert('', $obm_q, $params);
      } else {
        $cid = run_query_user_insert($params);
        if ($cid > 0) {
          set_update_state();
          $params['user_id'] = $cid;
          $display['msg'] .= display_ok_msg("$l_user : $l_insert_ok");
          $display['msg'] .= display_ok_msg("<input type='button' onclick=\"window.location='$path/user/user_index.php?action=pdf&user_id=$params[user_id]'\" value=\"$l_download_user_card\" />", false);
          $display['detail'] = dis_user_consult($params);
        } else {
          $display['msg'] .= display_err_msg("$l_user : $l_insert_error");
          $display['detail'] = dis_user_form($action, $params);
        }
      }
    }

  // Form data are not valid
  } else {
    $display['msg'] .= display_err_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['detail'] = dis_user_form($action, $params, $err['field']);
  }

} elseif ($action == 'reset') {
///////////////////////////////////////////////////////////////////////////////
  reset_preferences_to_default($params['user_id']);
  session_load_user_prefs();
  $display['msg'] .= display_ok_msg($l_reset_ok);
  $display['detail'] = dis_user_consult($params);

} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_user_data_form($params['user_id'], $params)) {
    $retour = run_query_user_update($params['user_id'], $params);
    if ($retour) {
      set_update_state();
      $display['msg'] .= display_ok_msg("$l_user : $l_update_ok");
      $display['detail'] = dis_user_consult($params);
    } else {
      $display['msg'] .= display_err_msg("$l_user : $l_update_error");
      $display['detail'] = dis_user_form($action, $params, $err['field']);
    }
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['detail'] = dis_user_form($action, $params, $err['field']);
  }

} elseif ($action == 'valid') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_user_data_form($params['user_id'], $params)) {
    $retour = run_query_user_update($params['user_id'], $params);
    if ($retour) {
      $retour = run_query_user_valid($params['user_id']);
      if ($retour) {
        set_update_state();
        require_once('user_mailer.php');
        $user = new UserMailer();
        $user->sendValidateConfirmation($params['user_id']);
        $display['msg'] .= display_ok_msg("$l_user : $l_valid_ok");
        $display['msg'] .= display_ok_msg("<input type='button' onclick=\"window.location='$path/user/user_index.php?action=pdf&user_id=$params[user_id]'\" value=\"$l_download_user_card\" />", false);
        $display['detail'] = dis_user_consult($params);
      } else {
        $display['msg'] .= display_err_msg("$l_user : $l_valid_error");
        $display['detail'] = dis_user_form($action, $params, $err['field']);
      }
    } else {
      $display['msg'] .= display_err_msg("$l_user : $l_update_error");
      $display['detail'] = dis_user_form($action, $params, $err['field']);
    }
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['detail'] = dis_user_form($action, $params, $err['field']);
  }

} elseif ($action == 'pdf') {
///////////////////////////////////////////////////////////////////////////////
  require_once("$obminclude/of/of_pdf.php");
  dis_user_export_pdf($params['user_id']);
  exit(1);

} elseif ($action == 'check_delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_can_delete($params)) {
    $display['msg'] .= display_info_msg($ok_msg, false);
    $display['detail'] = dis_user_can_delete($params['user_id']);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_user_consult($params);
  }

} elseif ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_can_delete($params)) {
    run_query_user_delete_profile($params['user_id']);
    $retour = run_query_user_delete($params['user_id']);
    if ($retour) {
      set_update_state();
      $display['msg'] .= display_ok_msg("$l_user : $l_delete_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_user : $l_delete_error");
    }
    $display['search'] = html_user_search_form($params);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_user_consult($params);
  }

} elseif ($action == 'backup') {
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

} elseif ($action == 'group_consult') {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_user_detail($params['user_id']);
  if ($obm_q->num_rows() == 1) {
    $display['detail'] = html_user_group_consult($obm_q);
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }

} elseif ($action == 'group_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_user_update_user_group($params);
  if ($retour >= 0) {
    set_update_state();
    $display['msg'] .= display_ok_msg("$l_user : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_user : $l_update_error");
  }
  $display['detail'] = dis_user_consult($params);

} elseif ($action == 'import') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_user_import_index();

} elseif ($action == 'import_file') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_user_import_file_run($params);

} elseif ($action == 'admin') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_user_admin_index();

} elseif ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'], 'user', 1);
  $display['detail'] = dis_user_display_pref($prefs);

} else if ($action == 'dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'user', 1);
  $display['detail'] = dis_user_display_pref($prefs);

} else if ($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'user', 1);
  $display['detail'] = dis_user_display_pref($prefs);
  
} else if ($action == 'search_batch_user') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = html_batch_user_search_form($params);
  $display['msg'] .= display_info_msg($l_no_display);
  
} else if ($action == 'sel_batch_users') {
///////////////////////////////////////////////////////////////////////////////
  if($cgp_use['property']['delegation'] && !of_delegation_check_data($params['delegation'], $obm['delegation_target'])) {   
    $params['delegation'] = $obm['delegation_target'];
  }
  $display['search'] = html_batch_user_search_form($params);
  $setrows = $_SESSION['set_rows'];
  $_SESSION['set_rows'] = 250;   

  list($strongest_profile_level, $user_can_manage_peers) = get_user_profile_level($obm);
  $display['result'] = dis_user_search_list($params, $user_results_limit, $strongest_profile_level, $user_can_manage_peers);
  $_SESSION['set_rows'] = $setrows;  
} else if ($action == 'edit_batch_values') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = html_user_batch_form($params);
  
} else if ($action == 'batch_processing') {
///////////////////////////////////////////////////////////////////////////////
  list($user_level, $user_can_manage_peers) = get_user_profile_level($obm);
  if (check_batch_processing_data($params, $obm['uid'], $user_level, $user_can_manage_peers)) {
    $users_id_error = run_query_user_data_batch($params);
    $users_id = array_diff($params['data-user-id'], $users_id_error);    
    $retour = run_query_batch_processing_update($params, $users_id);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_header_batch : $l_update_ok (".sizeof($users_id).") ");
      if(sizeof($users_id) > 0) {
        set_update_state();
      }
      if (sizeof($users_id_error) > 0) {
        check_users_error_data($params, $users_id_error);
        $display['msg'] .= display_warn_msg($err['msg'], false);
      }
      $display['search'] = html_user_search_form($params);
    } else {
      $display['msg'] .= display_err_msg("$l_header_batch : $l_update_error");
      $display['detail'] = html_user_batch_form($params);
    }
  } else {
    $display['msg'] .= display_err_msg($err['msg'], false);
    $display['detail'] = html_user_batch_form($params);
  }
} else if ($action == 'add_partnership') {
///////////////////////////////////////////////////////////////////////////////
  $add_q = run_query_userobm_mobile_add_partnership($params);
  echo "({".$display['json']."})";
  exit();

} else if ($action == 'unlink_mobile') {
///////////////////////////////////////////////////////////////////////////////
  $remove_q = run_query_userobm_mobile_unlink($params);
  echo "({".$display['json']."})";
  exit();

} else if ($action == 'remove_partnership') {
///////////////////////////////////////////////////////////////////////////////
  $remove_q = run_query_userobm_mobile_remove_partnership($params);
  echo "({".$display['json']."})";
  exit();
} else if ($action == 'profile_quota') {
///////////////////////////////////////////////////////////////////////////////
  $profile_name = $params['profile_name'];
  if ($params['user_pattern']) {
	$pattern = UserPattern::get($params['user_pattern']);
	if ($pattern->__get('profile') == $profile_name) {
		$user_pattern_quota = $pattern->__get('mail_quota');
		$profile_quota = (!empty($user_pattern_quota)) ? $user_pattern_quota : $params['profiles'][$profile_name]['properties']['mail_quota_default'];
	} else {
		$profile_quota = $params['profiles'][$profile_name]['properties']['mail_quota_default'];
	}
  } else {	
	$profile_quota = $params['profiles'][$profile_name]['properties']['mail_quota_default'];
  }
  echo "({quota:".$profile_quota."})";
  exit();
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
  
  of_category_user_module_action('user');

// Index
  $actions['user']['index'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/user/user_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
  );

// Get Ids
  $actions['user']['ext_get_ids'] = array (
    'Url'      => "$path/user/user_index.php?action=ext_get_ids",
    'Right'    => $cright_none,
    'Condition'=> array ('none'),
    'popup' => 1
  );
                                    
// Get Ids
  $actions['user']['ext_get_id'] = array (
    'Url'      => "$path/user/user_index.php?action=ext_get_id",
    'Right'    => $cright_none,
    'Condition'=> array ('none'),
    'popup' => 1
  );

// New
  $actions['user']['new'] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/user/user_index.php?action=new",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('search','index','insert','update','admin','detailconsult','reset','display','dispref_display','dispref_level', 'delete', 'backup', 'restore')
  );

// Wait
  $actions['user']['wait'] = array (
   'Name'     => $l_header_wait,
   'Url'      => "$path/user/user_index.php?action=wait",
   'Right'    => $cright_read_admin,
   'Condition'=> array ('all')
  );

// Search
  $actions['user']['search'] = array (
    'Url'      => "$path/user/user_index.php?action=search",
    'Right'    => $cright_none,
    'Condition'=> array ('None')
  );

// Search
  $actions['user']['ext_search'] = array (
    'Url'      => "$path/user/user_index.php?action=ext_search",
    'Right'    => $cright_none,
    'Condition'=> array ('None')
  );  

// Get user id from external window (js)
  $actions['user']['getsearch'] = array (
    'Url'      => "$path/user/user_index.php?action=search",
    'Right'    => $cright_none,
    'Condition'=> array ('None')
  );

// Detail Consult
  $actions['user']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/user/user_index.php?action=detailconsult&amp;user_id=".$params['user_id'],
    'Right'    => $cright_read_admin,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'group_consult', 'group_update', 'backup', 'restore')
  );

// Detail Update
  $actions['user']['detailupdate'] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/user/user_index.php?action=detailupdate&amp;user_id=".$params['user_id'],
     'Right'    => $cright_write_admin,
     'Condition'=> array ('detailconsult', 'reset', 'update', 'group_consult', 'group_update', 'backup', 'restore')
  );

// Insert
  $actions['user']['insert'] = array (
    'Url'      => "$path/user/user_index.php?action=insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
  );

// Update
  $actions['user']['update'] = array (
    'Url'      => "$path/user/user_index.php?action=update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
  );

// Update
  $actions['user']['pdf'] = array (
    'Url'      => "$path/user/user_index.php?action=pdf",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
  );

// Valid
  $actions['user']['valid'] = array (
    'Url'      => "$path/user/user_index.php?action=valid",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
  );

// Group Consult
  $actions['user']['group_consult'] = array (
    'Name'     => $l_header_upd_group,
    'Url'      => "$path/user/user_index.php?action=group_consult&amp;user_id=".$params['user_id'],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'reset', 'detailupdate', 'update', 'group_update', 'backup', 'restore')
  );

// Group Update
  $actions['user']['group_update'] = array (
    'Url'      => "$path/user/user_index.php?action=group_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
  );

// Reset
  $actions['user']['reset'] = array (
    'Name'     => $l_header_reset,
    'Url'      => "$path/user/user_index.php?action=reset&amp;user_id=".$params['user_id'],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'group_consult', 'group_update', 'backup', 'restore') 
  );

// Check Delete
  $actions['user']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/user/user_index.php?action=check_delete&amp;user_id=".$params['user_id'],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'reset', 'group_consult', 'group_update', 'backup', 'restore') 
  );

// Delete
  $actions['user']['delete'] = array (
    'Url'      => "$path/user/user_index.php?action=delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
  );

// Backup
  $actions['user']['backup'] = array (
    'Name'     => $GLOBALS['l_header_backup_restore'],
    'Url'      => "$path/user/user_index.php?action=backup&amp;user_id=".$params['user_id'],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'reset', 'group_consult', 'group_update','check_delete', 'backup', 'restore') 
  );

// Backup
  $actions['user']['restore'] = array (
    'Name'     => $GLOBALS['l_header_backup_restore'],
    'Url'      => "$path/user/user_index.php?action=restore&amp;user_id=".$params['user_id'],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
  );

// Display
  $actions['user']['display'] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/user/user_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
  );

// Display
  $actions['user']['dispref_display'] = array (
    'Url'      => "$path/user/user_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
  );

// Display
  $actions['user']['dispref_level'] = array (
    'Url'      => "$path/user/user_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
  );

// Import
  $actions['user']['import'] = array (
    'Name'     => $l_header_import,
    'Url'      => "$path/user/user_index.php?action=import",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('all')
  );

// Import file
  $actions['user']['import_file'] = array (
    'Url'      => "$path/user/user_index.php?action=import_file",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
  );

// Admin
  $actions['user']['admin'] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/user/user_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all')
  );

// Search Batch user : Users selection
  $actions['user']['search_batch_user'] = array (
    'Name'     => $l_header_batch,
    'Url'      => "$path/user/user_index.php?action=search_batch_user&amp;next_action=sel_batch_users",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('all')
  );

// Choose batch values
  $actions['user']['sel_batch_users'] = array (
    'Url'      => "$path/user/user_index.php?action=sel_batch_users",
    'Right'    => $cright_write_admin,
    'Condition'=> array('None')
  );

// Edit batch values
  $actions['user']['edit_batch_values'] = array (
    'Url'      => "$path/user/user_index.php?action=edit_batch_values",
    'Right'    => $cright_write_admin,
    'Condition'=> array('None')
  );

// Batch processing
  $actions['user']['batch_processing'] = array (
    'Url'	   => "$path/user/user_index.php?action=batch_processing",
    'Right'	   => $cright_write_admin,
    'Condition'=> array('None')
  );

  // Add partnership
  $actions['user']['add_partnership'] = array (
    'Url'	   => "$path/user/user_index.php?action=add_partnership",
    'Right'	   => $cright_write_admin,
    'Condition'=> array('None')
  );

  // Remove partnership
  $actions['user']['remove_partnership'] = array (
    'Url'	   => "$path/user/user_index.php?action=remove_partnership",
    'Right'	   => $cright_write_admin,
    'Condition'=> array('None')
  );

  // Get profile quota
  $actions['user']['profile_quota'] = array (
    'Url'	   => "$path/user/user_index.php?action=profile_quota",
    'Right'	   => $cright_write_admin,
    'Condition'=> array('None')
  );

  // Unlink mobile 
  $actions['user']['unlink_mobile'] = array (
    'Url'	   => "$path/user/user_index.php?action=unlink_mobile",
    'Right'	   => $cright_write_admin,
    'Condition'=> array('None')
  );

}


///////////////////////////////////////////////////////////////////////////////
// User Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////

function get_user_results_limit($profile) {
    global $profiles;
    $limit = $profiles[$profile]['properties']['user_results_limit'];
    return $limit ? $limit : PHP_INT_MAX;
}

function update_user_action() {
  global $params, $actions, $path, $cgp_user, $obm, $profiles;

  $id = $params['user_id'];
  if ($id > 0) {
    $u = get_user_info($id);
    if (Perm::user_can_update_peer($obm['uid'], $profiles[$obm['profile']], $id, $profiles[$u['perms']])
        || check_user_update_rights($params, $u)) {
      // Detail Consult
      $actions['user']['detailconsult']['Url'] = "$path/user/user_index.php?action=detailconsult&amp;user_id=$id";
      $actions['user']['detailconsult']['Condition'][] = 'insert';

      // Detail Update
      $actions['user']['detailupdate']['Url'] = "$path/user/user_index.php?action=detailupdate&amp;user_id=$id";
      $actions['user']['detailupdate']['Condition'][] = 'insert';

      // Check Delete
      $actions['user']['check_delete']['Url'] = "$path/user/user_index.php?action=check_delete&amp;user_id=$id";
      $actions['user']['check_delete']['Condition'][] = 'insert';

      // Group Consult
      $actions['user']['group_consult']['Url'] = "$path/user/user_index.php?action=group_consult&amp;user_id=$id";
      $actions['user']['group_consult']['Condition'][] = 'insert';

      // Backup
      $actions['user']['backup']['Url'] = "$path/user/user_index.php?action=backup&amp;user_id=$id";
      $actions['user']['backup']['Condition'][] = 'insert';

      // Backup
      $actions['user']['restore']['Url'] = "$path/user/user_index.php?action=restore&amp;user_id=$id";
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

function get_user_profile_level($obm) {
  $profiles = get_all_profiles(false);
  $user_profile = $profiles[$obm['profile']];
  $user_profile_level = array_key_exists('level', $user_profile) ? $user_profile['level'] : $user_profile['properties']['level'];
  $user_can_manage_peers = array_key_exists('level_managepeers', $user_profile) ? $user_profile['level_managepeers'] : $user_profile['properties']['level_managepeers'];
  return array($user_profile_level, $user_can_manage_peers);
}
?>
