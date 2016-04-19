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


?>
<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailshare_index.php                                          //
//     - Desc : MailShare Index File                                         //
// 2005-10-07 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields    -- show the mailshare search form
// - search          -- search fields    -- show the result set of search
// - new             --                  -- show the new mailshare form
// - detailconsult   -- $mailshare_id    -- show the mailshare detail
// - detailupdate    -- $mailshare_id    -- show the mailshare detail form
// - insert          -- form fields      -- insert the mailshare
// - update          -- form fields      -- update the mailshare
// - check_delete    -- $mailshare_id    -- check the mailshare
// - delete          -- $mailshare_id    -- delete the mailshare
// - rights_admin    --                  -- see mailshare rights
// - rights_update   --                  -- update mailshare rights
// - display         --                  -- display and set display parameters
// - dispref_display --                  -- update one field display value
// - dispref_level   --                  -- update one field display position 
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'mailshare';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_mailshare_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
require('../domain/domain_query.inc');
require('mailshare_display.inc');
require('mailshare_query.inc');
require('mailshare_js.inc');
require("$obminclude/of/of_right.inc");

if ($action == '') $action = 'index';
get_mailshare_action();
$perm->check_permissions($module, $action);
update_last_visit('mailshare', $params['mailshare_id'], $action);

page_close();


///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
// XXXXXXX ????
if ($action == 'ext_get_id') {
  $display['search'] = html_host_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_host_search_list($params);
  } else {
    $display['msg'] = display_info_msg($l_no_display);
  }

} else if (($action == 'index') || ($action == '')) {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = html_mailshare_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_mailshare_search_list('');
  } else {
    $display['msg'] .= display_ok_msg($l_no_display);
  }

} else if ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = html_mailshare_search_form($params);
  $display['result'] = dis_mailshare_search_list($params);

} else if ($action == 'ext_search') {
///////////////////////////////////////////////////////////////////////////////
  // Makes it possible to process other requests in parallel, do not remove
  session_write_close();
  $mailshare_q = run_query_mailshare_ext_search($params);
  json_search_mailshares($mailshare_q);
  echo '('.$display['json'].')';
  exit();

} else if ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = html_mailshare_form($action, '', $params);

} else if ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_mailshare_consult($params);

} else if ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  if (check_mailshare_update_rights($params)) {
    $obm_q = run_query_mailshare_detail($params['mailshare_id']);
    $display['detail'] = html_mailshare_form($action, $obm_q, $params);
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
  }

} else if ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_mailshare_data_form($params)) {
    
    // If the context (same mailshare ?) was confirmed ok, we proceed
    if ($params['confirm'] == $c_yes) {
      $id = run_query_mailshare_insert($params);
      if ($id > 0) {
        $params['mailshare_id'] = $id;
        set_update_state();
        $display['msg'] .= display_ok_msg("$l_mailshare : $l_insert_ok");
      } else {
        $display['msg'] .= display_err_msg("$l_mailshare : $l_insert_error");
      }
      $display['detail'] = dis_mailshare_consult($params);
      
    // If first try, we warn the user if some mailshare seem similar
    } else {
      $obm_q = check_mailshare_context('', $params);
      if ($obm_q->num_rows() > 0) {
        $display['detail'] = dis_mailshare_warn_insert('', $obm_q, $params);
      } else {
        $id = run_query_mailshare_insert($params);

	if ($id > 0) {
	  $params['mailshare_id'] = $id;
          set_update_state();
          $display['msg'] .= display_ok_msg("$l_mailshare : $l_insert_ok");
	} else {

          $display['msg'] .= display_err_msg("$l_mailshare : $l_insert_error");
        }
    
        $display['detail'] = dis_mailshare_consult($params);
      }
    }
    
  // Form data are not valid
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['detail'].= html_mailshare_form($action, '', $params, $err['field']);
  }

} elseif ($action == 'update')  {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_mailshare_data_form($params)) {
    $retour = run_query_mailshare_update($params);
    if ($retour) {
      set_update_state();
      $display['msg'] .= display_ok_msg("$l_mailshare : $l_update_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_mailshare : $l_update_error");
    }
    $display['detail'] = dis_mailshare_consult($params);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $mailshare_q = run_query_mailshare_detail($params['mailshare_id']);
    $display['detail'] = html_mailshare_form($action, $mailshare_q, $params, $err['field']);
  }

} elseif ($action == 'check_delete')  {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_mailshare($params['mailshare_id'])) {
    $display['msg'] .= display_info_msg($ok_msg, false);
    $display['detail'] = dis_can_delete_mailshare($params['mailshare_id']);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_mailshare_consult($params);
  }

} elseif ($action == 'delete')  {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_mailshare($params['mailshare_id'])) {
    $retour = run_query_mailshare_delete($params['mailshare_id']);
    if ($retour) {
      set_update_state();
      $display['msg'] .= display_ok_msg("$l_mailshare : $l_delete_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_mailshare : $l_delete_error");
    }
    $display['search'] = html_mailshare_search_form('');
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_mailshare_consult($params);
  }

} elseif ($action == 'rights_admin') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_mailshare_right_dis_admin($params['entity_id']);

} elseif ($action == 'rights_update') {
///////////////////////////////////////////////////////////////////////////////
  if (OBM_Acl_Utils::updateRights('mailshare', $params['entity_id'], $obm['uid'], $params)) {
    update_mailshare_acl( $obm['uid'], $obm['domain_id'] );
    $display['msg'] .= display_ok_msg("$l_rights : $l_update_ok");
  } else {
    $display['msg'] .= display_warn_msg($l_of_right_err_auth);
  }
  $display['detail'] = dis_mailshare_right_dis_admin($params['entity_id']);

} elseif ($action == 'backup') {
///////////////////////////////////////////////////////////////////////////////
  try {
    $backup = new Backup('mailshare', $params['mailshare_id']);
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
    $display['detail'] = dis_mailshare_backup_form($backup, $params);
  }

} elseif ($action == 'restore') {
///////////////////////////////////////////////////////////////////////////////
  try {
    $backup = new Backup('mailshare', $params['mailshare_id']);
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
    $display['detail'] = dis_mailshare_backup_form($backup, $params);
  }

} else if ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'], 'mailshare', 1);
  $display['detail'] = dis_mailshare_display_pref($prefs);

} else if ($action == 'dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'mailshare', 1);
  $display['detail'] = dis_mailshare_display_pref($prefs);

} else if($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'mailshare', 1);
  $display['detail'] = dis_mailshare_display_pref($prefs);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_mailshare);
if (! $params['popup']) {
  update_mailshare_action();
  $display['header'] = display_menu($module);
}
$display['end'] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $params hash, parameters transmited
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_mailshare_params() {
  global $action, $cdg_param, $popup;
  global $cb_read_public, $cb_write_public,$sel_accept_write,$sel_accept_read;

  // Get global params
  $params = get_global_params('MailShare');

  if ((isset ($params['entity_id'])) && (! isset($params['mailshare_id']))) {
    $params['mailshare_id'] = $params['entity_id'];
  }
  
  if(is_array($params['email'])) {
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
    $params['email'] = implode("\r\n",$email_aliases);
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// MailShare Action 
///////////////////////////////////////////////////////////////////////////////
function get_mailshare_action() {
  global $params, $actions, $path, $l_mailshare;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display,$l_header_admin, $l_header_right;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions['mailshare']['index'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/mailshare/mailshare_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
  );

// Search
  $actions['mailshare']['search'] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None')
  );

// Search
  $actions['mailshare']['ext_search'] = array (
    'Url'      => "$path/mailshare/mailshare_index.php?action=ext_search",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
  );

// New
  $actions['mailshare']['new'] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/mailshare/mailshare_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('search','index','admin', 'showlist','detailconsult','delete','display','backup','restore')
  );

// Detail Consult
  $actions['mailshare']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/mailshare/mailshare_index.php?action=detailconsult&amp;mailshare_id=".$params['mailshare_id'],
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate', 'showlist', 'update', 'rights_admin', 'rights_update','backup','restore') 
  );

// Detail Update
  $actions['mailshare']['detailupdate'] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/mailshare/mailshare_index.php?action=detailupdate&amp;mailshare_id=".$params['mailshare_id'],
     'Right'    => $cright_write,
     'Condition'=> array ('detailconsult', 'showlist', 'update','backup','restore') 
  );
					   
// Insert
  $actions['mailshare']['insert'] = array (
    'Url'      => "$path/mailshare/mailshare_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

// Update
  $actions['mailshare']['update'] = array (
    'Url'      => "$path/mailshare/mailshare_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

// Check Delete
  $actions['mailshare']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/mailshare/mailshare_index.php?action=check_delete&amp;mailshare_id=".$params['mailshare_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'showlist', 'showlist', 'update', 'detailupdate','backup','restore') 
  );

// Delete
  $actions['mailshare']['delete'] = array (
    'Url'      => "$path/mailshare/mailshare_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

// Rights Admin.
  $actions['mailshare']['rights_admin'] = array (
    'Name'     => $l_header_right,
    'Url'      => "$path/mailshare/mailshare_index.php?action=rights_admin&amp;entity_id=".$params['mailshare_id'],
    'Right'    => $cright_read,
    'Condition'=> array ('detailconsult','update','rights_update','rights_admin','backup','restore')
  );

// Rights Update
  $actions['mailshare']['rights_update'] = array (
    'Url'      => "$path/mailshare/mailshare_index.php?action=rights_update&amp;entity_id=".$params['mailshare_id'],
    'Right'    => $cright_read,
    'Condition'=> array ('None')
  );

// Backup
  $actions['mailshare']['backup'] = array (
    'Name'     => $GLOBALS['l_header_backup_restore'],
    'Url'      => "$path/mailshare/mailshare_index.php?action=backup&amp;mailshare_id=".$params['mailshare_id'],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'update', 'detailupdate','rights_update','rights_admin')
  );

// Restore
  $actions['mailshare']['restore'] = array (
    'Name'     => $GLOBALS['l_header_backup_restore'],
    'Url'      => "$path/mailshare/mailshare_index.php?action=restore&amp;mailshare_id=".$params['mailshare_id'],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
  );

// Display
  $actions['mailshare']['display'] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/mailshare/mailshare_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
  );

// Dispay Prefs
  $actions['mailshare']['dispref_display'] = array (
    'Url'      => "$path/mailshare/mailshare_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

// Display
  $actions['mailshare']['dispref_level'] = array (
    'Url'      => "$path/mailshare/mailshare_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

// External call
  $actions['mailshare']['ext_get_id'] = array (
    'Url'      => "$path/mailshare/mailshare_index.php?action=ext_get_id",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );
}


///////////////////////////////////////////////////////////////////////////////
// MailShare Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_mailshare_action() {
  global $params, $actions, $path;

  $id = $params['mailshare_id'];
  if ($id > 0) {
    $m = get_mailshare_info($id);
    // Detail Consult
    $actions['mailshare']['detailconsult']['Url'] = "$path/mailshare/mailshare_index.php?action=detailconsult&amp;mailshare_id=$id";
    if (check_mailshare_update_rights($params, $m)) { 
      // Detail Update
      $actions['mailshare']['detailupdate']['Url'] = "$path/mailshare/mailshare_index.php?action=detailupdate&amp;mailshare_id=$id";
      $actions['mailshare']['detailupdate']['Condition'][] = 'insert';
      
      // Check Delete
      $actions['mailshare']['check_delete']['Url'] = "$path/mailshare/mailshare_index.php?action=check_delete&amp;mailshare_id=$id";
      $actions['mailshare']['check_delete']['Condition'][] = 'insert';

      // Rights admin
      $actions['mailshare']['rights_admin']['Url'] = "$path/mailshare/mailshare_index.php?action=rights_admin&amp;entity_id=$id";
      $actions['mailshare']['rights_admin']['Condition'][] = 'insert';

      // Backup
      $actions['mailshare']['backup']['Url'] = "$path/mailshare/mailshare_index.php?action=backup&amp;mailshare_id=$id";
      $actions['mailshare']['backup']['Condition'][] = 'insert';

    } else {
      // Detail Update
      $actions['mailshare']['detailupdate']['Condition'] = array('None');
      // Check Delete
      $actions['mailshare']['check_delete']['Condition'] = array('None');
      // Rights admin
      $actions['mailshare']['rights_admin']['Condition'] = array('None');
      // Backup
      $actions['mailshare']['backup']['Condition'] = array('None');
    }
  }
}


</script>
