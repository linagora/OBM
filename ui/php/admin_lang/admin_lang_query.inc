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
// OBM - File : admin_lang_query.inc                                         //
//     - Desc : lang admin Query File                                        //
// 2001-12-18 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$actions = array ('help', 'index', 'show_src', 'show_lang', 'comp_lang', 'comp_header_lang');
$langs = get_admin_lang_langs_array();
$themes = get_admin_lang_themes_array();


///////////////////////////////////////////////////////////////////////////////
// Get the lang list
// Return : $langs : array of langs
///////////////////////////////////////////////////////////////////////////////
function get_admin_lang_langs_array() {
  global $obminclude, $path;

  // Lang dirs to exclude
  $langs = array();

  $lang_dir = dir("$path/../$obminclude/lang");
  while($entry=$lang_dir->read()) {
    if (!preg_match("/^\..*$/",$entry) && is_dir($lang_dir->path."/".$entry)) {
      array_push($langs, $entry);
    }
  }
  $lang_dir->close();

  return $langs;
}


///////////////////////////////////////////////////////////////////////////////
// Get the theme list
// Return : $themes : array of themes
///////////////////////////////////////////////////////////////////////////////
function get_admin_lang_themes_array() {
  global $path;

  // Themes dirs to exclude
  $exclude = array('images');
  $themes = array();

  $theme_dir = dir("$path/../resources/themes");
  while($entry=$theme_dir->read()) {
    if (! in_array ($entry, $exclude) && !preg_match("/^\..*$/",$entry) && is_dir($theme_dir->path."/".$entry)) {
      array_push($themes, $entry);
    }
  }
  $theme_dir->close();

  return $themes;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of lang vars that appears in the module sources
// Parameters:
//   - $module : module to proces
// Returns:
//   - $resultat : array of var present in the module sources
///////////////////////////////////////////////////////////////////////////////
function get_admin_lang_src_vars($module) {
  global $debug, $path;

  if ($debug > 0) {
    echo "Source vars : Files read : ";
  }

  $tableau = array();
  $contents = array();
  $file = "$path/$module/${module}_index.php";
  if (file_exists($file)) {
    $contents = file($file);
    if ($debug > 0) { echo "file=$file\n"; }
  }
  $file = "$path/$module/${module}_display.inc";
  if (file_exists($file)) {
    $contents = array_merge($contents, file($file));
    if ($debug > 0) { echo "file=$file\n"; }
  }
  $file = "$path/$module/${module}_query.inc";
  if (file_exists($file)) {
    $contents = array_merge($contents, file($file));
    if ($debug > 0) { echo "file=$file\n"; }
  }
  $file = "$path/$module/${module}_js.inc";
  if (file_exists($file)) {
    $contents = array_merge($contents, file($file));
    if ($debug > 0) { echo "file=$file\n"; }
  }

  // For each line of the file
  while (list($line_num, $line) = each($contents)) {

    // for each lang var, we note it as recorded "R_" and put it in the array
    while (preg_match('/^.*(\$l_)([a-z0-9_]*)[^a-z_].*$/Us',$line, $matches)) {
      $lvar = "\$l_" . $matches[2];
      $pattern = preg_quote($matches[1]);
      $line = preg_replace("/$pattern/", "\$R_", $line, 1);

      array_push($tableau, $lvar);      
    }
  }

  $resultat = array_unique($tableau);
  return $resultat;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of lang vars that appears and are really used in the module
// sources (not just defined in a global line)
// Parameters:
//   - $module : module to process
// Returns:
//   - $resultat : array of var present and used in the module sources
///////////////////////////////////////////////////////////////////////////////
function get_admin_lang_src_used_vars($module) {
  global $debug, $path;

  if ($debug > 0) {
    echo "Source used vars : Files read : ";
  }

  $tableau = array();
  $contents = array();

  $file = "$path/$module/${module}_index.php";
  if (file_exists($file)) {
    $contents = file($file);
    if ($debug > 0) { echo "file=$file\n"; }
  }
  $file = "$path/$module/${module}_display.inc";
  if (file_exists($file)) {
    $contents = array_merge($contents, file($file));
    if ($debug > 0) { echo "file=$file\n"; }
  }
  $file = "$path/$module/${module}_query.inc";
  if (file_exists($file)) {
    $contents = array_merge($contents, file($file));
    if ($debug > 0) { echo "file=$file\n"; }
  }
  $file = "$path/$module/${module}_js.inc";
  if (file_exists($file)) {
    $contents = array_merge($contents, file($file));
    if ($debug > 0) { echo "file=$file\n"; }
  }

  // For each line of the file
  while (list($line_num, $line) = each($contents)) {

    // if the global identifier is present (begin of the line), we note it
    if (preg_match('/^[\s\t]*global .*$/Us',$line, $matches)) {
      $global = true;
    } else {
      $global = false;
    }

    // for each lang var, we note it as recorded "R_" and put it in the array
    while (preg_match('/^.*(\$l_)([a-z0-9_]*)[^a-z_].*$/Us',$line, $matches)) {
      $lvar = "\$l_" . $matches[2];
      $pattern = preg_quote($matches[1]);
      if ($debug > 1) {
        echo "line=$line \n";
        echo "matches2=" . $matches[2] . "\n";
        echo "pattern=$pattern\n";
        echo "$global - $lvar \n";
        echo "\n*****************\n\n";
      }
      $line = preg_replace("/$pattern/", "\$R_", $line, 1);

      // We add the var in the result only if it is not a declaration (global)
      // ie the var is really used.
      if (! $global) array_push($tableau, $lvar);      

    }
  }

  $resultat = array_unique($tableau);
  return $resultat;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of lang vars that appears and are really used in the file
// sources (not just defined in a global line)
// Parameters:
//   - $myfile : file to process
// Returns:
//   - $resultat : array of var present and used in the file source
///////////////////////////////////////////////////////////////////////////////
function get_admin_lang_src_used_vars_file($myfile) {
  global $debug, $path;

  if ($debug > 0) {
    echo "Source used vars : File read : $myfile";
  }

  $tableau = array();
  $contents = array();

  $file = "$myfile";
  if (file_exists($file)) {
    $contents = file($file);
    if ($debug > 0) { echo "file=$file\n"; }
  } else {
    echo "File not found ! global_display.inc";
  }

  // For each line of the file
  while (list($line_num, $line) = each($contents)) {

    // if the global identifier is present (begin of the line), we note it
    if (preg_match('/^[\s\t]*global .*$/Us',$line, $matches)) {
      $global = true;
    } else {
      $global = false;
    }

    // for each lang var, we note it as recorded "R_" and put it in the array
    while (preg_match('/^.*(\$l_)([a-z0-9_]*)[^a-z_].*$/Us',$line, $matches)) {
      $lvar = "\$l_" . $matches[2];
      $pattern = preg_quote($matches[1]);
      if ($debug > 1) {
        echo "line=$line \n";
        echo "matches2=" . $matches[2] . "\n";
        echo "pattern=$pattern\n";
        echo "$global - $lvar \n";
        echo "\n*****************\n\n";
      }
      $line = preg_replace("/$pattern/", "\$R_", $line, 1);

      // We add the var in the result only if it is not a declaration (global)
      // ie the var is really used.
      if (! $global) array_push($tableau, $lvar);      

    }
  }

  $resultat = array_unique($tableau);
  return $resultat;
}


///////////////////////////////////////////////////////////////////////////////
// Returns the list of all vars used in OBM modules                          //
///////////////////////////////////////////////////////////////////////////////
function get_admin_lang_all_src_used_vars() {
  global $debug, $obminclude, $path, $modules;

  $all_vars = array();
  $vars = array();

  while (list($key, $mod) = each($modules)) {
    if ($debug > 0) {
      echo "All Source used vars : module : $mod[Name]";
    }

    $vars = get_admin_lang_src_used_vars($key);
    $all_vars = array_merge($all_vars, $vars);
  }

  $vars = get_admin_lang_src_used_vars_file("$path/../$obminclude/global.inc");
  $all_vars = array_merge($all_vars, $vars);
  $vars = get_admin_lang_src_used_vars_file("$path/../$obminclude/global_display.inc");
  $all_vars = array_merge($all_vars, $vars);
  $vars = get_admin_lang_src_used_vars_file("$path/../$obminclude/global_pref.inc");
  $all_vars = array_merge($all_vars, $vars);
  $vars = get_admin_lang_src_used_vars_file("$path/../$obminclude/lang/fr/check_date_js.inc");
  $all_vars = array_merge($all_vars, $vars);
  $vars = get_admin_lang_src_used_vars_file("$path/../$obminclude/phplib/obmlib.inc");
  $all_vars = array_merge($all_vars, $vars);

  $resultat = array_unique($all_vars);

  return $resultat;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of vars defined in the module lang file
// Parameters:
//   - $module : module to process
//   - $lang   : lang to process
// Returns: 
//   - $tableau : array of lang module vars : $tableau[var]=value
///////////////////////////////////////////////////////////////////////////////
function get_admin_lang_vars($module, $lang) {
  global $debug, $obminclude, $path;

  $tableau = array();
  $file = "$path/../$obminclude/lang/$lang/${module}.inc";
  if (file_exists($file)) {
    $contents = file($file);
  } else {
    return false;
  }

  if ($debug > 0) {
    echo "Lang vars : Files read : ";
    echo "file=$file\n";
  }

  // For each line of the file
  while (list($line_num, $line) = each($contents)) {

    // for each lang var, we note it as recorded "R_" and put it in the array
    while (preg_match('/^.*(\$l_)([a-z0-9_]*)\s*=\s*"(.*)"\s*;\s*(\/\/.*$)*$/Us',$line, $matches)) {
      $lvar = "\$l_" . $matches[2];
      $lvalue = $matches[3];
      $pattern = preg_quote($matches[1]);
      $line = preg_replace("/$pattern/", "\$R_", $line, 1);

      $tableau[$lvar] = $lvalue;
    }
  }

  return $tableau;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of lang vars defined in the global lang file (included in
// each module)
// Parameters:
//   - $lang   : lang to process
// Returns: 
//   - $tableau : array of global lang vars : $tableau[var]=value
///////////////////////////////////////////////////////////////////////////////
function get_admin_lang_global_lang_vars($lang) {
  global $debug, $obminclude, $path;

  $tableau = array();
  $file = "$path/../$obminclude/lang/$lang/global.inc";

  if (file_exists($file)) {
    $contents = file($file);
  } else {
    return false;
  }

  if ($debug > 0) {
    echo "Global Lang vars : Files read : ";
    echo "file=$file\n";
  }

  // For each line of the file
  while (list($line_num, $line) = each($contents)) {

    // for each lang var, we note it as recorded "R_" and put it in the array
    while (preg_match('/^.*(\$l_)([a-z0-9_]*)\s*=\s*"(.*)"\s*;\s*(\/\/.*$)*/Us',$line, $matches)) {
      $lvar = "\$l_" . $matches[2];
      $lvalue = $matches[3];
      $pattern = preg_quote($matches[1]);
      $line = preg_replace("/$pattern/", "\$R_", $line, 1);

      $tableau[$lvar] = $lvalue;
    }
  }

  return $tableau;
}
?>
