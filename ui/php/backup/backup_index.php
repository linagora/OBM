<?php
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
