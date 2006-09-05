<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_code_query.inc                                         //
//     - Desc : Code admin Query File                                        //
// 2003-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$regexp_def_func = '/^\s*function ([a-z_]*)\s{0,3}\(.*/Us';
$regexp_used_func = '/^.*([a-z_]{1,96})\s{0,3}\(.*$/Us';
$regexp_used_func_not_def = '/^\s*(function|\/\/).*$/Us';
$regexp_used_func_not_debug = '/display_debug_msg.*([a-z_]{1,96})\s{0,3}\(.*$/Us';

$modules = get_modules_array();
array_unshift($modules, $c_all);
$acts = array ('help', 'index', 'show_amp', 'func_unused', 'function_uses');
$words = array ('amp;', 'nbsp;', ' ', '&', '\(', '\)', '\\n', '\$', '\'', '\|', 'eacute;', 'egrave;', 'agrave;', 'middot;');
$exclude = array('.', '..', 'CVS', 'doc', 'scripts', 'contrib', 'www');


///////////////////////////////////////////////////////////////////////////////
// Parse and process one directory (recursive) for defined function search
// Parameters:
//   - $dir : directory to process
///////////////////////////////////////////////////////////////////////////////
function get_admin_code_def_func_directory($dir) {
  global $exclude;

  $table = array();
  $table2 = array();

  $obm_dir = dir($dir);
  while ($entry=$obm_dir->read()) {

    $long_entry = $obm_dir->path."/".$entry;

    if (! in_array ($entry, $exclude)) {
      if (is_dir($long_entry)) {
	$table2 = get_admin_code_def_func_directory($long_entry);
      } else {
	// We look only in .php and .inc files
	$temp = explode (".", $entry);
	$ext = $temp[1];
	if (($ext == "php") || ($ext == "inc")) {
	  $table2 = get_admin_code_def_functions($long_entry);
	}
      }
    }

    $table = array_merge($table, $table2);
  }
  $obm_dir->close();

  return $table;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of defined functions in the file given
// Parameters:
//   - $file   : file to process
// Returns:
//   - $resultat : array of defined functions
///////////////////////////////////////////////////////////////////////////////
function get_admin_code_def_functions($file) {
  global $debug, $path, $regexp_def_func;

  if ($debug > 0) {
    echo "<br />Files read : $pfile";
  }

  $tableau = array();
  $contents = array();
  if (file_exists($file)) {
    $contents = file($file);
    if ($debug > 0) { echo "file=$file\n"; }
  }

  // For each line of the file
  while (list($line_num, $line) = each($contents)) {

    // If a function is defined, we note it
    if (preg_match($regexp_def_func, $line, $matches)) {
      $func_name = $matches[1];
      $tableau[$func_name]["file"] = $file;
      $tableau[$func_name]["line_num"] = $line_num;
      $tableau[$func_name]["cpt"] += 1;

    }
  }

  return $tableau;
}


///////////////////////////////////////////////////////////////////////////////
// Parse and process recursively the directory for used function search
// Parameters:
//   - $dir : root directory to process
///////////////////////////////////////////////////////////////////////////////
function get_admin_code_used_functions($dir) {
  global $exclude, $used_table;

  $used_table = array();

  $obm_dir = dir($dir);
  while ($entry=$obm_dir->read()) {

    $long_entry = $obm_dir->path."/".$entry;

    if (! in_array ($entry, $exclude)) {
      if (is_dir($long_entry)) {
	get_admin_code_used_func_directory($long_entry);
      } else {
	// We look only in .php and .inc files
	$temp = explode (".", $entry);
	$ext = $temp[1];
	if (($ext == "php") || ($ext == "inc")) {
	  get_admin_code_used_func_file($long_entry);
	}
      }
    }
  }
  $obm_dir->close();

  return $used_table;
}


///////////////////////////////////////////////////////////////////////////////
// Parse and process one directory (recursive) for used function search
// Parameters:
//   - $dir : directory to process
///////////////////////////////////////////////////////////////////////////////
function get_admin_code_used_func_directory($dir) {
  global $exclude;

  $obm_dir = dir($dir);
  while ($entry=$obm_dir->read()) {

    $long_entry = $obm_dir->path."/".$entry;

    if (! in_array ($entry, $exclude)) {
      if (is_dir($long_entry)) {
	get_admin_code_used_func_directory($long_entry);
      } else {
	// We look only in .php and .inc files
	$temp = explode (".", $entry);
	$ext = $temp[1];
	if (($ext == "php") || ($ext == "inc")) {
	  get_admin_code_used_func_file($long_entry);
	}
      }
    }
  }
  $obm_dir->close();

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of used functions in the file given
// Parameters:
//   - $file   : file to process
// Returns:
//   - $resultat : array of functions (defined or used)
///////////////////////////////////////////////////////////////////////////////
function get_admin_code_used_func_file($file) {
  global $debug, $path, $regexp_used_func, $regexp_used_func_not_def;
  global $regexp_used_func_not_debug, $used_table;

  $contents = array();
  if (file_exists($file)) {
    $contents = file($file);
    if ($debug > 0) { echo "file=$file\n"; }
  }

  // For each line of the file
  while (list($line_num, $line) = each($contents)) {

    // If the line is not a function definition and not a debug msg
    if ((! preg_match($regexp_used_func_not_def, $line, $tmp))
	&& (! preg_match($regexp_used_func_not_debug, $line, $tmp))) {

      // If a function is used, we note it
      while (preg_match($regexp_used_func, $line, $matches)) {
	$func_name =  $matches[1];
	$used_table[$func_name]["file"] = $file;
	$used_table[$func_name]["line_num"] = $line_num;
	$used_table[$func_name]["cpt"] += 1;

	$pattern = preg_quote($matches[1]);
	$line = preg_replace("/$pattern/","",$line,1);
      }
    }
  }
}

?>
