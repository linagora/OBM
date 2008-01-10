<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : backup_query.inc                                             //
//     - Desc : Backup query File                                            //
// 2005-08-22 Aliacom - Pierre Baudracco                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Get backup list from Filesystem.
// Parameters:
//   - $backup : hash values
// Returns:
// Array( [$fullname] => array([0]=>$path [1]=>$name), ..)
///////////////////////////////////////////////////////////////////////////////
function get_backup_list($backup) {
  global $cbackup_root;

  $version = $backup['version'];
  $date = of_isodate_convert($backup['date']);

  if ( substr($cbackup_root, strlen($cbackup_root)-1 ) != '/' ) {
    $backup_path = $cbackup_root . '/';
  } else {
    $backup_path = $cbackup_root;
  }

  $root_handler = opendir($backup_path);
  while (($file = readdir($root_handler)) !== false) {
    if (($file != '..') && ($file != '.')) {
      $fullname = $backup_path . $file;
      if (! is_dir($fullname)) {
        preg_match('/[0-9]{1,2}\.[0-9]{1,2}(\.[0-9]{1,2})?/', $file, $matches);
	$b_version = $matches[0];
	if (preg_match("/^$version/", $b_version)) {
	  $b_date = filemtime($fullname);
	  if (preg_match("/^$date/", of_datetime_format($b_date))) {
	    $files[$file]['version'] = $b_version;
	    $files[$file]['date'] = $b_date;
	    $files[$file]['size'] = filesize($fullname);
	  }
	}
      }
    }
  }
  closedir($root_handler);

  return $files;
}


///////////////////////////////////////////////////////////////////////////////
// Backup Creation
///////////////////////////////////////////////////////////////////////////////
function run_query_backup_create() {
  global $err, $l_create_backup_error, $l_err_unknown_db;
  global $obmdb_dbtype, $obmdb_db, $obmdb_user, $obmdb_password;
  global $db_type_mysql, $db_type_pgsql;

  $date = date('Ymd:His');
  $version = get_obm_info('db_version');
  $filename = "obmdb-$date-$version.dump";
  $disk_path = get_backup_disk_path();
  $disk_fullname = $disk_path . $filename; 

  $output = array();
  $ret = array();

  if ($obmdb_dbtype == $db_type_mysql) {
    $cmd = "mysqldump -u $obmdb_user -p$obmdb_password $obmdb_db > $disk_fullname";
    exec($cmd, $output, $ret);
  } else if ($obmdb_dbtype == $db_type_pgsql) {
    $cmd = "pg_dump -U $obmdb_user $obmdb_db -f $disk_fullname";
    exec($cmd, $output, $ret);
  } else {
    $err['msg'] = "$l_err_unknown_db ($obmdb_dbtype)";
    return false;
  }

  if ($ret != 0) {
    $err['msg'] = $l_create_backup_error;
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Backup Restore
///////////////////////////////////////////////////////////////////////////////
function run_query_backup_restore($filename) {
  global $err, $l_err_backup_file, $l_err_unknown_db;
  global $obmdb_dbtype, $obmdb_db, $obmdb_user, $obmdb_password;
  global $db_type_mysql, $db_type_pgsql;

  $disk_path = get_backup_disk_path();
  $disk_fullname = $disk_path . $filename; 
  
  if ( (! is_file($disk_fullname)) || (! is_readable($disk_fullname) ) ) {
    $err['msg'] = "$l_err_backup_file ($disk_fullname)";
    return;
  }

  $output = array();
  $ret = array();

  if ($obmdb_dbtype == $db_type_mysql) {
    $cmd_drop = "DROP DATABASE IF EXISTS $obmdb_db";
    $cmd = "mysql -u $obmdb_user -p$obmdb_password -e '$cmd_drop'";
    exec($cmd, $output, $ret);

    $cmd_create = "CREATE DATABASE $obmdb_db";
    $cmd = "mysql -u $obmdb_user -p$obmdb_password -e '$cmd_create'";
    exec($cmd, $output, $ret);

    $cmd = "mysql -u $obmdb_user -p$obmdb_password $obmdb_db < $disk_fullname";
    exec($cmd, $output, $ret);

  } else if ($obmdb_dbtype == $db_type_pgsql) {

    // Because can't Drop database we are connected on, on Postgres
    // And we can't close all connections
    // Delete all postgres Data
    $obm_q = new DB_OBM;
    $tables = $obm_q->table_names();
    $i = 0;
    while ($tables[$i]['table_name']) {
      $table = $tables[$i]['table_name'];

      // We exclude POSTGRES meta table (sql_xxxx)
      if (! (substr($table, 0, 4) == 'sql_')) {
	$query = "DELETE FROM $table";
	$obm_q->query($query);
      }
      $i++;
    }

    //    $cmd_drop = "DROP DATABASE $obmdb_db";
    //    $cmd = "psql -U $obmdb_user template1 -c '$cmd_drop'";
    //    exec($cmd, $output, $ret);

    //    $cmd_create = "CREATE DATABASE $obmdb_db with owner=$obmdb_user";
    //    $cmd = "psql -U $obmdb_user template1 -c '$cmd_create'";
    //    exec($cmd, $output, $ret);

    //    $cmd = "pg_restore $disk_fullname | psql -U $obmdb_user $obmdb_db";
    $cmd = "psql -U $obmdb_user $obmdb_db < $disk_fullname";
    exec($cmd, $output, $ret);

  } else {
    $err['msg'] = "$l_err_unknown_db ($obmdb_dbtype)";
    return false;
  }

  if ($ret != 0) {
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $filename : backup filename
///////////////////////////////////////////////////////////////////////////////
function run_query_backup_delete($filename) {
  global $err;

  // Delete the backup file
  $retour = run_query_backup_delete_backup_file($filename);

  if (! $retour) {
    $err['msg'] = $l_delete_file_error;
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Delete Backup File from Disk
// Parameters:
//   - $id : id of file to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_backup_delete_backup_file($filename) {

  $disk_path = get_backup_disk_path();
  $disk_fullname = $disk_path . $filename; 

  $ret = unlink($disk_fullname);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Calculate the real disk backup path
// Returns:
//   real disk backup path
///////////////////////////////////////////////////////////////////////////////
function get_backup_disk_path() {
  global $cbackup_root;

  $disk_path = format_path($cbackup_root);
  
  return $disk_path; 
}


///////////////////////////////////////////////////////////////////////////////
// Check the backup can be deleted
// Parameters:
//   - $filename : backup filename
// Returns:
//   true if backup can be deleted (for now always), else false
///////////////////////////////////////////////////////////////////////////////
function check_backup_can_delete($filename) {
  global $err, $php_regexp_backup, $l_err_backup_name;

  $delete_ok = true;

  // Filename check (to avoid filename corruptions)
  if (preg_match($php_regexp_backup, $filename) == 0) {
    $err['msg'] = "$l_err_backup_name : $filename"; 
    $delete_ok = false;
  }

  return $delete_ok;
}

?>