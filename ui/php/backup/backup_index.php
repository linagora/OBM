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


$path = "..";
$module = "backup";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("backup_query.inc");
require("backup_display.inc");
require_once("$obminclude/javascript/calendar_js.inc");

if ($action == "") $action = "index";
$backup = get_param_backup();
get_backup_action();
$perm->check_permissions($module, $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if ($action == "index") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_backup_search_form($backup);
  if ($set_display == "yes") {
    $display["result"] = dis_backup_search_list($backup);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_backup_search_form($backup);
  $display["result"] = dis_backup_search_list($backup);
  
} elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_backup_form($backup);
  
} elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  $ret = run_query_backup_create();
  if ($ret) {
    $display["msg"] .= display_ok_msg("$l_backup : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_backup : $l_insert_error $err_msg");
  }
  dis_backup_index();

} elseif ($action == "restore") {
///////////////////////////////////////////////////////////////////////////////
  $ret = run_query_backup_restore($backup["filename"]);
  if ($ret) {
    $display["msg"] .= display_ok_msg("$l_backup : $l_restore_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_backup : $l_restore_error $err_msg");
  }
  dis_backup_index();

} elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_backup_can_delete($backup["filename"])) {
    $display["msg"] .= display_info_msg($err_msg);
    $display["detail"] = dis_can_delete_backup($backup["filename"]);
  } else {
    $display["msg"] .= display_warn_msg($err_msg);
    $display["detail"] = dis_backup_index($backup);
  }

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_backup_can_delete($backup["filename"])) {
    $retour = run_query_backup_delete($backup["filename"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_backup : $l_delete_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_backup : $err_msg : $l_delete_error");
    }
    dis_backup_index($backup);
  } else {
    $display["msg"] .= display_warn_msg("$err_msg $l_cant_delete");
    dis_backup_index($backup);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_backup);
if (! $backup["popup"]) {
  $display["header"] = display_menu($module);
}
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Backup parameters transmited in $backup hash
// returns : $backup hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_backup() {
  global $tf_filename, $tf_version, $tf_date, $param_backup, $popup; 

  if (isset ($param_backup)) $backup["filename"] = $param_backup;
  if (isset ($tf_filename)) $backup["filename"] = $tf_filename;
  if (isset ($tf_version)) $backup["version"] = $tf_version;
  if (isset ($tf_date)) $backup["date"] = $tf_date;

  display_debug_param($backup);

  return $backup;
}


///////////////////////////////////////////////////////////////////////////////
//  Backup Action 
///////////////////////////////////////////////////////////////////////////////
function get_backup_action() {
  global $dbackup, $actions, $path;
  global $l_header_list, $l_header_find, $l_header_new, $l_header_delete;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index  
  $actions["backup"]["index"] = array (
    'Name'     => $l_header_list,
    'Url'      => "$path/backup/backup_index.php?action=index",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                    	 );

// Search
  $actions["backup"]["search"] = array (
    'Url'      => "$path/backup/backup_index.php?action=search",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('None') 
                                    	 );

// New
  $actions["backup"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/backup/backup_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('search','index','insert','check_delete', 'delete') 
                                     );

// CheckDelete
  $actions["backup"]["check_delete"] = array (
    'Url'      => "$path/backup/backup_index.php?action=check_delete&amp;backup_id=".$backup["filename"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	      );

// Delete
  $actions["backup"]["delete"] = array (
    'Url'      => "$path/backup/backup_index.php?action=delete",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	      );

// Insert
  $actions["backup"]["insert"] = array (
    'Url'      => "$path/backup/backup_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );  

// Restore
  $actions["backup"]["restore"] = array (
    'Url'      => "$path/backup/backup_index.php?action=restore",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );  

}

?>