<SCRIPT language="php">
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
function dis_help($mode) {

  if ($mode == "html") echo "<table border=1><tr><td><pre>\n";
  include ("admin_code_help.inc");
  if ($mode == "html") echo "\n</pre></td></tr></table>";

}


///////////////////////////////////////////////////////////////////////////////
// Display the Code admin Index
// Parameters:
//   - $mode  : "txt" ou "html"
//   - $words : list of excluded words
///////////////////////////////////////////////////////////////////////////////
function dis_code_index($mode, $actions, $words) {

  switch ($mode) {
  case "txt":
    echo "try --help to get info on available actions. ";
    break;
  case "html":
    html_code_index($actions, $words);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display the HTML search form for code module
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_code_index($actions, $words) {
  global $sess;
  global $l_execute, $l_help;
  global $col_textem, $col_a_table, $col_a_text, $col_a_tableh;

  // exlude words list
  list ($key, $val) = each ($words);
  $word_list = $val;
  while (list ($key, $val) = each ($words)) {
    $word_list .= ", $val";
  }

  echo "
    <a href=\"" . $sess->url("admin_code_index.php?action=help&amp;mode=html"). "\">$l_help</a>
    <p>
    <FORM method=get action=\"" . $sess->url("admin_code_index.php"). "\">
    <table border=1>
    <tr>
      <td>action : show_amp<br>
        <font color=\"#$col_textem\">Show misuse of & in url (& amp; required) from source</font></td>
      <td>Excluded words :<br><tt>$word_list</tt></td>
      <td><input type=hidden name=mode value=\"html\">
          <input type=hidden name=action value=show_amp>
          <input type=submit value=\"$l_execute\"></td>
    </tr>
    </table>
    </FORM>";
}


///////////////////////////////////////////////////////////////////////////////
// Display files containing '&' but not in exluded words from $words         //
// Parameters:
//   - $mode  : "txt" or "html"
//   - $words : word list to exclude
///////////////////////////////////////////////////////////////////////////////
function dis_amp($mode, $words) {
  global $obm_root, $cpt_line;

  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
";
  } else {
    echo "<table>";
  }
  dis_amp_directory($mode, $words, $obm_root);
  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
$cpt_line lines parsed
------------------------------------------------------------------------------
";
  } else {
    echo "</table>
      <p>
      <b>$cpt_line</b> lines parsed";
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Parse and proceed one directory (recursive)
// Parameters:
//   - $mode  : "txt" or "html"
//   - $words : word list to exclude
//   - $dir   : directory to process
///////////////////////////////////////////////////////////////////////////////
function dis_amp_directory($mode, $words, $dir) {
  global $exclude;

  $obm_dir = dir($dir);
  while($entry=$obm_dir->read()) {

    $long_entry = $obm_dir->path."/".$entry;

    if (! in_array ($entry, $exclude)) {
      if (is_dir($long_entry)) {
	dis_amp_directory($mode, $words, $long_entry);
      } else {
	// We look only in .php and .inc files
	$temp = explode (".", $entry);
	$ext = $temp[1];
	if (($ext == "php") || ($ext == "inc")) {
	  dis_amp_file($mode, $long_entry);
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
function dis_amp_file($mode, $file) {
  global $col_textem, $col_a_table, $col_a_text, $col_a_tableh;
  global $regexp, $debug, $cpt_line;

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
	  echo "<tr><td colspan=2 align=center>
          <font color=\"#$col_textem\">$file</font>
      </td>
    </tr><tr>
      <td bgcolor=\"#$col_a_tableh\"><font color=\"#$col_textem\">#</font></td>
      <td bgcolor=\"#$col_a_tableh\"><font color=\"#$col_textem\">Line</font></td>
    </tr>";

	}
      }

      if ($mode == "txt") {
	echo "$line_num : $line";
      } else {
	echo "<tr>
          <td>$line_num</td>
          <td><pre>$line</pre></td>
        </tr>";
      }

      $line = "";
    }
  }

  return true;

}


