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
// OBM - File : backup_index.php                                             //
//     - Desc : Backup Index File                                            //
// 2005-08-22 Aliacom - Pierre Baudracco                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions              -- Parameter
// - index (default)    -- search fields  -- show the backup list
// - search             -- search fields  -- show the backup list
// - new                --                -- New backup form
// - insert             --                -- create a new backup
// - check_delete       -- $backup_id  -- Check if a backup can be deleted
// - delete             -- $backup_id  -- Delete a backup (if ok)
// - restore            -- $backup_id  -- Restore a backup
///////////////////////////////////////////////////////////////////////////////


$path = '..';
$module = 'backup';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_param_backup();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
require('backup_query.inc');
require('backup_display.inc');

get_backup_action();
$perm->check_permissions($module, $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
// Check that the Backup directory is ok
if (substr($cbackup_root, strlen($cbackup_root)-1 ) != '/') {
  $backup_path = $cbackup_root . '/';
} else {
  $backup_path = $cbackup_root;
}
if (! file_exists($backup_path)) {
  $display['msg'] = display_err_msg($l_err_backup_dir_not_exist);
} else if (! is_writable($backup_path)) {
  $display['msg'] = display_err_msg($l_err_backup_dir_not_writable);

} else if ($action == 'index') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_backup_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_backup_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} elseif ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_backup_search_form($params);
  $display['result'] = dis_backup_search_list($params);
  
} elseif ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_backup_form($params);
  
} elseif ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  $ret = run_query_backup_create();
  if ($ret) {
    $display['msg'] .= display_ok_msg("$l_backup : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_backup : $l_insert_error $err[msg]");
  }
  dis_backup_index();

} elseif ($action == 'restore') {
///////////////////////////////////////////////////////////////////////////////
  $ret = run_query_backup_restore($params['filename']);
  if ($ret) {
    $display['msg'] .= display_ok_msg("$l_backup : $l_restore_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_backup : $l_restore_error $err[msg]");
  }
  dis_backup_index();

} elseif ($action == 'check_delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_backup_can_delete($params['filename'])) {
    $display['msg'] .= display_info_msg($err['msg']);
    $display['detail'] = dis_can_delete_backup($params['filename']);
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
    $display['detail'] = dis_backup_index($params);
  }

} elseif ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_backup_can_delete($params['filename'])) {
    $retour = run_query_backup_delete($params['filename']);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_backup : $l_delete_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_backup : $err[msg] : $l_delete_error");
    }
    dis_backup_index($params);
  } else {
    $display['msg'] .= display_warn_msg("$err[msg] $l_cant_delete");
    dis_backup_index($params);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_backup);
if (! $params['popup']) {
  $display['header'] = display_menu($module);
}
$display['end'] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Backup parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_backup() {

  // Get global params
  $params = get_global_params('Backup');

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
//  Backup Action 
///////////////////////////////////////////////////////////////////////////////
function get_backup_action() {
  global $$params, $actions, $path;
  global $l_header_list, $l_header_find, $l_header_new, $l_header_delete;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index  
  $actions['backup']['index'] = array (
    'Name'     => $l_header_list,
    'Url'      => "$path/backup/backup_index.php?action=index",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                    	 );

// Search
  $actions['backup']['search'] = array (
    'Url'      => "$path/backup/backup_index.php?action=search",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('None') 
                                    	 );

// New
  $actions['backup']['new'] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/backup/backup_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('search','index','insert','check_delete', 'delete') 
                                     );

// CheckDelete
  $actions['backup']['check_delete'] = array (
    'Url'      => "$path/backup/backup_index.php?action=check_delete&amp;backup_id=".$backup['filename'],
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	      );

// Delete
  $actions['backup']['delete'] = array (
    'Url'      => "$path/backup/backup_index.php?action=delete",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	      );

// Insert
  $actions['backup']['insert'] = array (
    'Url'      => "$path/backup/backup_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );  

// Restore
  $actions['backup']['restore'] = array (
    'Url'      => "$path/backup/backup_index.php?action=restore",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );  

}

?>
