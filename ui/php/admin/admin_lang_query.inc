<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_lang_query.inc                                         //
//     - Desc : lang admin Query File                                        //
// 2001-12-18 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$actions = array ('help', 'index', 'show_src', 'show_lang', 'comp_lang', 'comp_header_lang');
$modules = get_modules_array();
$langs = get_langs_array();
$themes = get_themes_array();


///////////////////////////////////////////////////////////////////////////////
// Get the lang list                                                       //
// Return : $langs : array of langs
///////////////////////////////////////////////////////////////////////////////
function get_langs_array() {
  global $obminclude;

  // Lang dirs to exclude
  $exclude = array('.', '..', 'CVS');
  $langs = array();

  $lang_dir = dir("../../$obminclude/lang");
  while($entry=$lang_dir->read()) {
    if (! in_array ($entry, $exclude) && is_dir($lang_dir->path."/".$entry)) {
      array_push($langs, $entry);
    }
  }
  $lang_dir->close();

  return $langs;
}


///////////////////////////////////////////////////////////////////////////////
// Get the theme list                                                        //
// Return : $themes : array of themes
///////////////////////////////////////////////////////////////////////////////
function get_themes_array() {
  global $obminclude;

  // Themes dirs to exclude
  $exclude = array('.', '..', 'CVS', 'images');
  $themes = array();

  $theme_dir = dir("../../$obminclude/themes");
  while($entry=$theme_dir->read()) {
    if (! in_array ($entry, $exclude) && is_dir($theme_dir->path."/".$entry)) {
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
function get_src_vars($module) {
  global $debug;

  $path = "..";

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
    while (preg_match('/^.*(\$l_)([a-z_]*)[^a-z_].*$/Us',$line, $matches)) {
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
function get_src_used_vars($module) {
  global $debug;

  $path = "..";

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
    while (preg_match('/^.*(\$l_)([a-z_]*)[^a-z_].*$/Us',$line, $matches)) {
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
// Returns the list of vars used in the header section ($theme/menu.inc)     //
// There is no unused notion here as the lang file is included in function
// and so the vars are not defined as global
// Parameters:
//   - $theme : theme where to get the menu file
///////////////////////////////////////////////////////////////////////////////
function get_src_header_vars($theme) {
  global $debug, $obminclude;

  $path = "..";

  if ($debug > 0) {
    echo "Source header used vars : Files read : ";
  }

  $tableau = array();
  $contents = array();
  $file = "$path/../$obminclude/themes/$theme/menu.inc";

  if (file_exists($file)) {
    $contents = file($file);
    if ($debug > 0) { echo "file=$file\n"; }
  }

  // For each line of the file
  while (list($line_num, $line) = each($contents)) {

    $valid = true;
    // If var is in a global, comment or function line we bypass this variable
    // if the global identifier is present (begin of the line), we note it
    if (preg_match('/^[\s\t]*global .*$/Us',$line, $matches)) {
      $valid = false;
    }
    if (preg_match('/^.*(\/\/|function).*$l_.*$/Us',$line, $matches)) {
      $valid = false;
    }

    // for each lang var, we note it as recorded "R_" and put it in the array
    while (preg_match('/^.*(\$l_)([a-z_]*)[^a-z_].*$/Us',$line, $matches)) {
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

      array_push($tableau, $lvar);      

      // We add the var in the result only if it has to be
      if ($valid) array_push($tableau, $lvar);      
    }
  }

  $resultat = array_unique($tableau);
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
function get_lang_vars($module, $lang) {
  global $debug, $obminclude;

  $path = "..";

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
    while (preg_match('/^.*(\$l_)([a-z_]*)\s*=\s*"(.*)"\s*;\s*(\/\/.*$)*$/Us',$line, $matches)) {
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
function get_global_lang_vars($lang) {
  global $debug, $obminclude;

  $path = "..";

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
    while (preg_match('/^.*(\$l_)([a-z_]*)\s*=\s*"(.*)"\s*;\s*(\/\/.*$)*/Us',$line, $matches)) {
      $lvar = "\$l_" . $matches[2];
      $lvalue = $matches[3];
      $pattern = preg_quote($matches[1]);
      $line = preg_replace("/$pattern/", "\$R_", $line, 1);

      $tableau[$lvar] = $lvalue;

    }
  }

  return $tableau;
}
