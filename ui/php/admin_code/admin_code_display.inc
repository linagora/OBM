<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_code_display.inc                                       //
//     - Desc : code admin display File                                      //
// 2002-07-15 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display the help screen
// Parameters:
//   - $mode   : "txt" ou "html"
///////////////////////////////////////////////////////////////////////////////
function dis_admin_code_help($mode) {

  if ($mode == "html")
    echo "<table class=\"detail\">
      <tr>
        <td><pre>";

  include ("admin_code_help.inc");

  if ($mode == "html")
    echo "</pre>
        </td>
      </tr>
      </table>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the Code admin Index
// Parameters:
//   - $mode  : "txt" ou "html"
//   - $words : list of excluded words
///////////////////////////////////////////////////////////////////////////////
function dis_admin_code_code_index($mode, $actions, $words) {

  switch ($mode) {
  case "txt":
    echo "try --help to get info on available actions. ";
    break;
  case "html":
    html_admin_code_index($actions, $words);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display the HTML search form for code module
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_admin_code_index($actions, $words) {
  global $modules, $l_execute;

  // modules SELECT
  $sel_module = "<select name=\"param_module\">";
  while (list ($key, $val) = each ($modules)) {
    $sel_module .= "<option value=\"$val\">$val</option>";
  }
  $sel_module .= "</select>";

  // exlude words list
  list ($key, $val) = each ($words);
  $word_list = $val;
  while (list ($key, $val) = each ($words)) {
    $word_list .= ", $val";
  }

  echo "
    <br />
    <form method=\"get\" action=\"" . url_prepare("admin_code_index.php"). "\">
    <table class=\"admin\">
    <tr>
      <td class=\"adminHead\">action : show_amp</td>
      <td class=\"adminHead\">Excluded words</td>
      <td class=\"adminLabel\" rowspan=\"2\">
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"show_amp\" />
        <input type=\"submit\" value=\"$l_execute\" /></td>
    </tr><tr>
      <td class=\"adminText\">
        Show misuse of & in url (& amp; required) from source</td>
      <td class=\"adminText\"><tt>$word_list</tt></td>
    </tr>
    </table>
    </form>

    <form method=\"get\" action=\"" . url_prepare("admin_code_index.php"). "\">
    <table class=\"admin\">
    <tr>
      <td class=\"adminHead\">action : func_unused</td>
      <td class=\"adminLabel\" rowspan=\"2\">module :<br />$sel_module</td>
      <td class=\"adminLabel\" rowspan=\"2\">
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"func_unused\" />
        <input type=\"submit\" value=\"$l_execute\" /></td>
    </tr><tr>
      <td class=\"adminText\">
        Show used and unused functions (defined but not used in code)</td>
    </tr>
    </table>
    </form>

    <form method=\"get\" action=\"" . url_prepare("admin_code_index.php"). "\">
    <table class=\"admin\">
    <tr>
      <td class=\"adminHead\">action : function_uses</td>
      <td class=\"adminLabel\" rowspan=\"2\">
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"function_uses\" />
        <input type=\"submit\" value=\"$l_execute\" /></td>
    </tr><tr>
      <td class=\"adminText\">
        Show Uses of function
        <input type=\"text\" name=\"function\" value=\"\" />
      </td>
    </tr>
    </table>
    </form>
";
}


///////////////////////////////////////////////////////////////////////////////
// Display files containing '&' but not in exluded words from $words
// Parameters:
//   - $mode  : "txt" or "html"
///////////////////////////////////////////////////////////////////////////////
function dis_admin_code_amp($mode) {
  global $obm_root, $cpt_line;

  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
";
  } else {
    echo "<table class=\"admin\">";
  }
  dis_admin_code_amp_directory($mode, $obm_root);
  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
$cpt_line lines parsed
------------------------------------------------------------------------------
";
  } else {
    echo "</table>
      <p />
      <b>$cpt_line</b> lines parsed";
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Parse and proceed one directory (recursive)
// Parameters:
//   - $mode  : "txt" or "html"
//   - $dir   : directory to process
///////////////////////////////////////////////////////////////////////////////
function dis_admin_code_amp_directory($mode, $dir) {
  global $exclude;

  $obm_dir = dir($dir);
  while($entry=$obm_dir->read()) {

    $long_entry = $obm_dir->path."/".$entry;

    if (! in_array ($entry, $exclude)) {
      if (is_dir($long_entry)) {
	dis_admin_code_amp_directory($mode, $long_entry);
      } else {
	// We look only in .php and .inc files
	$temp = explode (".", $entry);
	$ext = $temp[1];
	if (($ext == "php") || ($ext == "inc")) {
	  dis_admin_code_amp_file($mode, $long_entry);
	}
      }
    }
  }
  $obm_dir->close();

}


///////////////////////////////////////////////////////////////////////////////
// Display misuse of '&' in a code file (cause in URL we want to force use   //
// of &amp; instead of &
// Parametres:
//   - $mode : "txt" or "html"
//   - $file : file name (complete with path)
///////////////////////////////////////////////////////////////////////////////
function dis_admin_code_amp_file($mode, $file) {
  global $regexp, $cpt_line;

  $first = true;  // first line matching in this file
  $contents = array();
  if (file_exists($file)) {
    $contents = file($file);
  }

  // For each line of the file
  while (list($line_num, $line) = each($contents)) {
    $cpt_line++;
    // for each lang var, we note it as recorded "R_" and put it in the array
    while (preg_match("/$regexp/Us",$line, $matches)) {
      // if first line matching, display the file header
      if ($first) {
	$first = false;
	if ($mode == "txt") {
	  echo "
------------------------------------------------------------------------------
*** $file
------------------------------------------------------------------------------
";
	} else {
	  echo "
    <tr>
      <td class=\"adminHead\">#&nbsp;Line</td>
      <td class=\"adminHead\">$file</td>
    </tr>";
	}
      }

      if ($mode == "txt") {
	echo "$line_num : $line";
      } else {
	echo "<tr>
          <td class=\"adminLabel\">$line_num</td>
          <td class=\"adminText\">$line</td>
        </tr>";
      }

      $line = "";
    }
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Display defined functions that are not used in code
// Parameters:
//   - $mode   : "txt" or "html"
//   - $module : module to process (or All)
///////////////////////////////////////////////////////////////////////////////
function dis_admin_code_unused_functions($mode, $module="") {
  global $path, $c_all, $def_func, $used_func;

  if (($module != $c_all) && ($module != "")) $root = "$path/$module";
  else $root = "$path/..";

  $def_func = get_admin_code_def_func_directory($root);
  $used_func = get_admin_code_used_functions($root);
  
  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
File   --   Line   --   Function   --   Used
";
  } else {
    echo "<table class=\"admin\">
    <tr>
      <td class=\"adminHead\">Function</td>
      <td class=\"adminHead\"> # Line</td>
      <td class=\"adminHead\">Defined in file</td>
      <td class=\"adminHead\"> # used</td>
    </tr>";
  }
  dis_admin_code_unused_func_content($mode, $def_func, $used_func);
  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
";
  } else {
    echo "</table>";
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Display Unused functions
// Parameters:
//   - $mode : "txt" or "html"
//   - $def  : defined functions array
//   - $used : used functions array
///////////////////////////////////////////////////////////////////////////////
function dis_admin_code_unused_func_content($mode, $def, $used) {

  foreach ($def as $func => $fa) {
    $def_file = $fa["file"];
    $def_ln = $fa["line_num"];

    if (array_key_exists($func, $used)) {
      $used_cpt = $used["$func"]["cpt"];
      $classl = "class=\"adminTextLeft\"";
      $classr = "class=\"adminTextRight\"";
      $html_func = "<a href=\"" . url_prepare("admin_code_index.php?action=function_uses&amp;function=$func&amp;mode=html") . "\">$func</a>";
    } else {
      $used_cpt = "No";
      $classl = "class=\"adminLabel\"";
      $classr = "class=\"adminLabel\"";
      $html_func = $func;
    }

    if ($mode == "txt") {
      echo "$def_file -- $def_ln -- $func -- $used_cpt\n";
    } else {
      echo "<tr>
        <td $classl>$html_func</td>
        <td $classr>$def_ln</td>
        <td $classl>$def_file</td>
        <td $classr>$used_cpt</td>
      </tr>";
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display one function uses
// Parameters:
//   - $mode : "txt" or "html"
//   - $func : function to display
///////////////////////////////////////////////////////////////////////////////
function dis_admin_code_function_uses($mode, $func) {
  global $obm_root, $def_func, $used_func;

  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
Line   --   File   --   #Use
------------------------------------------------------------------------------
";
  } else {
    echo "<table class=\"admin\">
    <tr>
      <td class=\"adminHead\" colspan=\"3\">Function $func</td>
    </tr>
    <tr>
      <td class=\"adminHead\"> # Line</td>
      <td class=\"adminHead\"> File</td>
      <td class=\"adminHead\"> # used</td>
    </tr>";
  }
  dis_admin_code_function_uses_content($mode, $func, $obm_root);
  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
";
  } else {
    echo "</table>";
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Parse and process one directory (recursive) for given function uses
// Parameters:
//   - $mode     : "txt" or "html"
//   - $function : function to search
//   - $dir      : directory to process
///////////////////////////////////////////////////////////////////////////////
function dis_admin_code_function_uses_content($mode, $function, $dir) {
  global $exclude;

  $obm_dir = dir($dir);
  while ($entry=$obm_dir->read()) {

    $long_entry = $obm_dir->path."/".$entry;

    if (! in_array ($entry, $exclude)) {
      if (is_dir($long_entry)) {
	dis_admin_code_function_uses_content($mode, $function, $long_entry);
      } else {
	// We look only in .php and .inc files
	$temp = explode (".", $entry);
	$ext = $temp[1];
	if (($ext == "php") || ($ext == "inc")) {
	  dis_admin_code_function_uses_content_file($mode, $function, $long_entry);
	}
      }
    }
  }
  $obm_dir->close();

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Dislpay uses of the function in the file given
// Parameters:
//   - $mode : "txt" or "html"
//   - $function : function to search
//   - $file     : file to process
///////////////////////////////////////////////////////////////////////////////
function dis_admin_code_function_uses_content_file($mode, $function, $file) {
  global $regexp_used_func, $regexp_used_func_not_def, $used_table;
  global $regexp_used_func_not_debug;

  $contents = array();
  if (file_exists($file)) {
    $contents = file($file);
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
	$cpt = $used_table[$func_name]["cpt"];

	// If it is the searched function, we display it
	if ($function == $func_name) {
	  if ($mode == "txt") {
	    echo "\n$line_num - $file - $cpt";
	  } else {
	    echo "
    <tr>
      <td class=\"adminTextRight\"> $line_num</td>
      <td class=\"adminTextLeft\"> $file</td>
      <td class=\"adminTextRight\"> $cpt</td>
    </tr>";
	  }
	}
	$pattern = preg_quote($matches[1]);
	$line = preg_replace("/$pattern/","",$line,1);
      }
    }
  }
}

?>