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
    echo "<table>
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
  global $modules, $l_execute,$l_all,$c_all;

  // modules SELECT
  $sel_module = "<select name=\"param_module\">";
    $sel_module .= "<option value=\"$c_all\">$l_all</option>";
  while (list ($key, $val) = each ($modules)) {
    $sel_module .= "<option value=\"$key\">$val[Name]</option>";
  }
  $sel_module .= "</select>";

  // exlude words list
  list ($key, $val) = each ($words);
  $word_list = $val;
  while (list ($key, $val) = each ($words)) {
    $word_list .= ", $val";
  }

  echo "
    <form method=\"get\" action=\"" . url_prepare("admin_code_index.php"). "\">
    <div class=\"detail infos\">
    <h1>Show misuse</h1>
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th>action : show_amp</th>
      <th>Excluded words</th>      
      </tr>
    </thead>
    <tbody>
    <tr>
      <td>
        Show misuse of & in url (& amp; required) from source</td>
      <td>$word_list</td>
    </tr>
    </tbody>
    </table>
    <div class=\"buttons\">
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"show_amp\" />
        <input type=\"submit\" value=\"$l_execute\" />
   </div>
   </div>
   </form>

    <form method=\"get\" action=\"" . url_prepare("admin_code_index.php"). "\">
    <div class=\"detail infos\">
    <h1>Show used and unused functions</h1>
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th>Action : func_unused</th>
      <th>Module</th>
    </tr>
    </thead>
    <tbody>
    <tr>
      <td>
        Show used and unused functions (defined but not used in code)
      </td>
      <td>
      $sel_module
      </td>
    </tr>
    </table>    
    <div class=\"buttons\">  
	<input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"func_unused\" />
        <input type=\"submit\" value=\"$l_execute\" />
   </div>
   </div>    
   </form>

    <form method=\"get\" action=\"" . url_prepare("admin_code_index.php"). "\">
    <div class=\"detail infos\">
    <h1>Show Uses</h1>
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th colspan=\"2\">Action : function_uses</th>
    </tr>
    </thead>
    <tbody>
      <tr>
      <td>
        Show Uses of function
      </td>
      <td>
        <input type=\"text\" name=\"function\" value=\"\" />
      </td>
    </tr>
    </tbody>
    </table>
    <div class=\"buttons\">
    <input type=\"hidden\" name=\"mode\" value=\"html\" />
    <input type=\"hidden\" name=\"action\" value=\"function_uses\" />
    <input type=\"submit\" value=\"$l_execute\" />
    </div>
    </div>

    
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
    echo "<div class=\"O\" style=\"width=100%;\"><table class=\"spreadSheet\">";
  }
  dis_admin_code_amp_directory($mode, $obm_root);
  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
$cpt_line lines parsed
------------------------------------------------------------------------------
";
  } else {
    echo "<tr><th colspan=\"3\"><b>$cpt_line</b> lines parsed</th></tr></table></div>
      ";
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
      if (is_dir($long_entry) && $entry != '.' && $entry != '..') {
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
    <thead>
    <tr>
      <th># &nbsp; Line</th>
      <th>$file</th>
    </tr>
    </thead>";
	}
      }

      if ($mode == "txt") {
	echo "$line_num : $line";
      } else {
	echo "
	<tbody>
	<tr>
          <td>$line_num</td>
          <td>$line</td>
        </tr>
	</tbody>";
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
    echo "
    <div class=\"detail extra\">
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th>Function</th>
      <th> # Line</th>
      <th>Defined in file</th>
      <th> # used</th>
    </tr>
    </thead>";
  }
  dis_admin_code_unused_func_content($mode, $def_func, $used_func);
  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
";
  } else {
    echo "</table></div>";
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
      $html_func = "<a href=\"" . url_prepare("admin_code_index.php?action=function_uses&amp;function=$func&amp;mode=html") . "\">$func</a>";
    } else {
      $used_cpt = "No";
      $html_func = $func;
    }

    if ($mode == "txt") {
      echo "$def_file -- $def_ln -- $func -- $used_cpt\n";
    } else {
      echo "<tbody><tr>
        <td>$html_func</td>
        <td>$def_ln</td>
        <td>$def_file</td>
        <td>$used_cpt</td>
      </tr></tbody>";
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
    echo "
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th colspan=\"3\">Function $func</th>
    </tr>
    <tr>
      <th> # Line</th>
      <th> File</th>
      <th> # used</th>
    </tr>
    </thead>";
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
      if (is_dir($long_entry) && $entry != '.' && $entry != '..') {
	dis_admin_code_function_uses_content($mode, $function, $long_entry);
      } elseif ($entry != '.' && $entry != '..') {
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
    <tbody>
    <tr>
      <td>$line_num</td>
      <td>$file</td>
      <td>$cpt</td>
    </tr>
    </tbody>";
	  }
	}
	$pattern = preg_quote($matches[1]);
	$line = preg_replace("/$pattern/","",$line,1);
      }
    }
  }
}

?>
