<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : admin_lang_display.inc                                      //
//     - Desc  : lang admin display File                                     //
// 2001-12-18 Pierre Baudracco                                               //
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
  include ("admin_lang_help.inc");
  if ($mode == "html") echo "\n</pre></td></tr></table>";

}


///////////////////////////////////////////////////////////////////////////////
// Display the search form for lang module
// Parameters:
//   - $mode   : "txt" ou "html"
///////////////////////////////////////////////////////////////////////////////
function dis_lang_index($mode, $actions, $modules, $langs, $themes) {

  switch ($mode) {
  case "txt":
    txt_lang_index($actions, $modules, $langs, $themes);
    break;
  case "html":
    html_lang_index($actions, $modules, $langs, $themes);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }

}


///////////////////////////////////////////////////////////////////////////////
// Display the HTML search form for lang module
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_lang_index($actions, $modules, $langs, $themes) {
  global $sess;
  global $l_execute;
  global $col_textem, $col_a_table, $col_a_text, $col_a_tableh;

  // modules SELECT
  $sel_module = "<SELECT name=module>";
  while (list ($key, $val) = each ($modules)) {
    $sel_module .= "<OPTION value=\"$val\">$val";
  }
  $sel_module .= "</SELECT>";

  // lang1 SELECT
  $sel_lang1 = "<SELECT name=lang>";
  while (list ($key, $val) = each ($langs)) {
    $sel_lang1 .= "<OPTION value=\"$val\">$val";
  }
  $sel_lang1 .= "</SELECT>";

  // lang2 SELECT
  reset ($langs);
  $sel_lang2 = "<SELECT name=lang2>";
  while (list ($key, $val) = each ($langs)) {
    $sel_lang2 .= "<OPTION value=\"$val\">$val";
  }
  $sel_lang2 .= "</SELECT>";

  // theme SELECT
  reset ($themes);
  $sel_theme = "<SELECT name=theme>";
  while (list ($key, $val) = each ($themes)) {
    $sel_theme .= "<OPTION value=\"$val\">$val";
  }
  $sel_theme .= "</SELECT>";

  echo "<FORM method=get action=\"" . $sess->url("admin_lang_index.php"). "\">
    <table border=1>
    <tr>
      <td>action : show_src<br>
        <font color=\"#$col_textem\">Show vars from source</font></td>
      <td>module :<br>$sel_module</td>
      <td><input type=hidden name=mode value=\"html\">
          <input type=hidden name=action value=show_src>
          <input type=submit value=\"$l_execute\"></td>
    </tr>
    </table>
    </FORM>

    <HR>

    <FORM method=get action=\"" . $sess->url("admin_lang_index.php"). "\">
    <table border=1>
    <tr>
      <td>action : show_lang<br>
        <font color=\"#$col_textem\">Show Lang definition</font></td>
      <td>module :<br>$sel_module</td>
      <td>lang 1 :<br>$sel_lang1</td>
      <td><input type=hidden name=mode value=\"html\">
          <input type=hidden name=action value=show_lang>
          <input type=submit value=\"$l_execute\"></td>
    </tr>
    </table>
    </FORM>

    <HR>

    <FORM method=get action=\"" . $sess->url("admin_lang_index.php"). "\">
    <table border=1>
    <tr>
      <td>action : comp_lang<br>
        <font color=\"#$col_textem\">Compare Lang definition</font></td>
      <td>module :<br>$sel_module</td>
      <td>lang 1 :<br>$sel_lang1</td>
      <td>lang 2 :<br>$sel_lang2</td>
      <td><input type=hidden name=mode value=\"html\">
          <input type=hidden name=action value=comp_lang>
          <input type=submit value=\"$l_execute\"></td>
    </tr>
    </table>
    </FORM>

    <HR>

    <FORM method=get action=\"" . $sess->url("admin_lang_index.php"). "\">
    <table border=1>
    <tr>
      <td>action : comp_header_lang<br>
        <font color=\"#$col_textem\">Compare Header Lang definition</font></td>
      <td>theme :<br>$sel_theme</td>
      <td>lang 1 :<br>$sel_lang1</td>
      <td>lang 2 :<br>$sel_lang2</td>
      <td><input type=hidden name=mode value=\"html\">
          <input type=hidden name=action value=comp_header_lang>
          <input type=submit value=\"$l_execute\"></td>
    </tr>
    </table>
    </FORM>";

}


///////////////////////////////////////////////////////////////////////////////
// Display variables referenced in a module source code with indication about//
// real use. (A variable referenced only in a global statement is not used)  //
// Parameters:
//   - $mode   : "txt" or "html"
//   - $module : module name
///////////////////////////////////////////////////////////////////////////////
function dis_src_vars($mode, $module) {

  $ref_vars = get_src_vars($module);
  $used_vars = get_src_used_vars($module);
  $unused_vars = array_diff ($ref_vars, $used_vars);

  switch ($mode) {
  case "txt":
    txt_src_vars($module, $ref_vars, $used_vars, $unused_vars);
    break;
  case "html":
    html_src_vars($module, $ref_vars, $used_vars, $unused_vars);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }
}


///////////////////////////////////////////////////////////////////////////////
// TXT Display vars referenced from a module source files
// Parametres:
//   - $module      : module name
//   - $ref_vars    : existing vars in source code
//   - $used_vars   : used vars
//   - $unused_vars : unused vars (defined in global but not used)
///////////////////////////////////////////////////////////////////////////////
function txt_src_vars($module, $ref_vars, $used_vars, $unused_vars) {

  $nb_used_vars = count($used_vars);
  $nb_unused_vars = count($unused_vars);

  echo "
------------------------------------------------------------------------------
*** $module : Lang vars used : $nb_used_vars vars
------------------------------------------------------------------------------
";
  while (list ($nb, $myvar) = each ($used_vars)) {
    echo "$myvar\n";
  }
  echo "
------------------------------------------------------------------------------
*** $module : Lang vars referenced but not used : $nb_unused_vars vars
------------------------------------------------------------------------------
";
  while (list ($nb, $myvar) = each ($unused_vars)) {
    echo "$myvar\n";
  }
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display vars referenced from a module source files
// Parameters:
//   - $module      : module name
//   - $ref_vars    : existing vars in source code
//   - $used_vars   : used vars
//   - $unused_vars : unused vars (defined in global but not used)
///////////////////////////////////////////////////////////////////////////////
function html_src_vars($module, $ref_vars, $used_vars, $unused_vars) {
  global $col_textem, $col_a_table, $col_a_text, $col_a_tableh;

  $nb_used_vars = count($used_vars);
  $nb_unused_vars = count($unused_vars);

  echo "<table>
    <tr>
      <td colspan=2 align=center bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">
      $module : Variables referenced from module source</font>
      </td>
    </tr><tr>
      <td valign=top><table border=0 cellspacing=0>
    <tr><td bgcolor=\"#$col_a_tableh\"><font color=\"#$col_textem\">used vars : $nb_used_vars</font></td></tr>";
  while (list ($nb, $myvar) = each ($used_vars)) {
    echo "<tr><td bgcolor=\"#$col_a_table\"><font color=\"$col_a_text\">$myvar</font></td></tr>\n";
  }
  echo "</table></td>
    <td valign=top><table border=0 cellspacing=0>
  <tr><td bgcolor=\"#$col_a_tableh\"><font color=\"#$col_textem\">UNused vars : $nb_unused_vars</font></td></tr>";
  while (list ($nb, $myvar) = each ($unused_vars)) {
    echo "<tr><td bgcolor=\"#$col_a_table\"><font color=\"$col_a_text\">$myvar</font></td></tr>\n";
  }
  echo "</table></td></tr></table>";
}


///////////////////////////////////////////////////////////////////////////////
// Display vars from the lang and module given with information about use
// Parametres:
//   - $mode   : "txt" ou "html"
//   - $module : module name
//   - $lang   : language to display
///////////////////////////////////////////////////////////////////////////////
function dis_lang_vars($mode, $module, $lang) {

  $lang_v = get_lang_vars($module, $lang);
  if (! $lang_v) {
    echo "File $lang/$module.inc is missing ! Create it";
    return false;
  }
  $ref_v = get_src_vars($module);
  $used_v = get_src_used_vars($module);
  $unused_v = array_diff ($ref_v, $used_v);

  switch ($mode) {
  case "txt":
    txt_lang_vars($module, $lang, $lang_v, $ref_v, $used_v, $unused_v);
    break;
  case "html":
    html_lang_vars($module, $lang, $lang_v, $ref_v, $used_v, $unused_v);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }
}


///////////////////////////////////////////////////////////////////////////////
// TXT Display vars from the lang and module given
// Parameters:
//   - $module   : module name
//   - $lang     : language to display
//   - $lang_v   : lang vars defined
//   - $ref_v    : lang vars referenced in source files
//   - $used_v   : lang vars used in source files
//   - $unused_v : lang vars referenced in source but not used
///////////////////////////////////////////////////////////////////////////////
function txt_lang_vars($module, $lang, $lang_v, $ref_v, $used_v, $unused_v) {

  $text = "";
  $nb_ref = count($ref_v);
  $nb_used = count($used_v);
  $nb_unused = count($unused_v);
  $nb_lang = count($lang_v);
  $nb_used_def = 0;
  $nb_used_undef = 0;
  $nb_unused_def = 0;
  $nb_unused_undef = 0;
  $nb_def_unref = 0;

  while (list ($nb, $lval) = each ($used_v)) {
    $value = $lang_v[$lval];
    if ($value) {
      $text .= "$lval = $value\n";
      $nb_used_def++;
    }
  }
  echo "------------------------------------------------------------------------------
*** $module / $lang
------------------------------------------------------------------------------
From Source    : $nb_ref var referenced : $nb_used used - $nb_unused unused 
From lang file : $nb_lang var defined
------------------------------------------------------------------------------
*** $module / $lang : vars used and defined : $nb_used_def vars
------------------------------------------------------------------------------
$text
";

  $text = "";
  reset($used_v);
  while (list ($nb, $lval) = each ($used_v)) {
    $value = $lang_v[$lval];
    if (! $value) {
      $text .= "$lval = $value\n";
      $nb_used_undef++;
    }
  }
  echo "------------------------------------------------------------------------------
*** $module / $lang : vars used but not defined : $nb_used_undef vars
------------------------------------------------------------------------------
$text
";

  $text = "";
  reset($unused_v);
  while (list ($nb, $lval) = each ($unused_v)) {
    $value = $lang_v[$lval];
    if ($value) {
      $text .= "$lval = $value\n";
      $nb_unused_def++;
    }
  }
  echo "------------------------------------------------------------------------------
*** $module / $lang : vars Unused but defined : $nb_unused_def vars
------------------------------------------------------------------------------
$text
";

  $text = "";
  reset($unused_v);
  while (list ($nb, $lval) = each ($unused_v)) {
    $value = $lang_v[$lval];
    if (! $value) {
      $text .= "$lval = $value\n";
      $nb_unused_undef++;
    }
  }
  echo "------------------------------------------------------------------------------
*** $module / $lang : vars Unused and not defined : $nb_unused_undef vars
------------------------------------------------------------------------------
$text
";

  // Vars defined in Lang file but not referenced in source
  $text = "";
  reset($lang_v);
  while (list ($lvar, $lval) = each ($lang_v)) {
    if ((! in_array($lvar, $used_v)) && (! in_array($lvar, $unused_v))) {
      $text .= "$lvar = $lval\n";
      $nb_def_unref++;
    }
  }
  echo "
------------------------------------------------------------------------------
*** $module / $lang : vars defined in lang but not referenced : $nb_def_unref vars
------------------------------------------------------------------------------
$text
";

}


///////////////////////////////////////////////////////////////////////////////
// HTML Display vars from the lang and module given
// Parameters:
//   - $module   : module name
//   - $lang     : language to display
//   - $lang_v   : lang vars defined
//   - $ref_v    : lang vars referenced in source files
//   - $used_v   : lang vars used in source files
//   - $unused_v : lang vars referenced in source but not used
///////////////////////////////////////////////////////////////////////////////
function html_lang_vars($module, $lang, $lang_v, $ref_v, $used_v, $unused_v) {
  global $col_textem, $col_a_table, $col_a_text, $col_a_tableh;

  $nb_ref = count($ref_v);
  $nb_used = count($used_v);
  $nb_unused = count($unused_v);
  $nb_lang = count($lang_v);
  $nb_used_def = 0;
  $nb_used_undef = 0;
  $nb_unused_def = 0;
  $nb_unused_undef = 0;
  $nb_def_unref = 0;

  while (list ($nb, $lval) = each ($used_v)) {
    $value = $lang_v[$lval];
    if ($value) {
      $text_used_def .= "<tr><td bgcolor=\"#$col_a_table\"><font color=\"#$col_a_text\">$lval</font></td><td bgcolor=\"#$col_a_table\"><font color=\"#$col_a_text\">$value</font></td></tr>\n";
      $nb_used_def++;
    }
  }

  reset($used_v);
  while (list ($nb, $lval) = each ($used_v)) {
    $value = $lang_v[$lval];
    if (! $value) {
      $text_used_undef .= "<tr><td bgcolor=\"#$col_a_table\"><font color=\"#$col_a_text\">$lval</font></td><td bgcolor=\"#$col_a_table\"><font color=\"#$col_a_text\">$value</font></td></tr>\n";
      $nb_used_undef++;
    }
  }

  reset($unused_v);
  while (list ($nb, $lval) = each ($unused_v)) {
    $value = $lang_v[$lval];
    if ($value) {
      $text_unused_def .= "<tr><td bgcolor=\"#$col_a_table\"><font color=\"#$col_a_text\">$lval</font></td><td bgcolor=\"#$col_a_table\"><font color=\"#$col_a_text\">$value</font></td></tr>\n";
      $nb_unused_def++;
    }
  }

  reset($unused_v);
  while (list ($nb, $lval) = each ($unused_v)) {
    $value = $lang_v[$lval];
    if (! $value) {
      $text_unused_undef .= "<tr><td bgcolor=\"#$col_a_table\"><font color=\"#$col_a_text\">$lval</font></td><td bgcolor=\"#$col_a_table\"><font color=\"#$col_a_text\">$value</font></td></tr>\n";
      $nb_unused_undef++;
    }
  }

  reset($lang_v);
  while (list ($lvar, $lval) = each ($lang_v)) {
    if ((! in_array($lvar, $used_v)) && (! in_array($lvar, $unused_v))) {
      $text_def_unref .= "<tr><td bgcolor=\"#$col_a_table\"><font color=\"#$col_a_text\">$lvar</font></td><td bgcolor=\"#$col_a_table\"><font color=\"#$col_a_text\">$lval</font></td></tr>\n";
      $nb_def_unref++;
    }
  }

  echo "<table cellspacing=0 border=0>
    <tr>
      <td colspan=2 bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">$module / $lang<br>
      From Source : $nb_ref var referenced : $nb_used used - $nb_unused unused 
      <br>From lang file : $nb_lang var defined
      </font>
      </td>
    </tr><tr>
      <td colspan=2 bgcolor=\"#$col_a_table\">&nbsp;</td>
    </tr><tr>
      <td colspan=2 bgcolor=\"#$col_a_tableh\">
        <font color=\"#$col_textem\">Vars Used and defined : $nb_used_def</font>
      </td>
    </tr>
      $text_used_def
    <tr>
      <td colspan=2 bgcolor=\"#$col_a_tableh\">
        <font color=\"#$col_textem\">Vars used but not defined : $nb_used_undef</font>
      </td>
    </tr>
      $text_used_undef
    <tr>
      <td colspan=2 bgcolor=\"#$col_a_tableh\">
        <font color=\"#$col_textem\">Vars UNused but defined : $nb_unused_def</font>
      </td>
    </tr>
      $text_unused_def
    <tr>
      <td colspan=2 bgcolor=\"#$col_a_tableh\">
        <font color=\"#$col_textem\">Vars UNused and not defined : $nb_unused_undef</font>
      </td>
    </tr>
      $text_unused_undef
    <tr>
      <td colspan=2 bgcolor=\"#$col_a_tableh\">
        <font color=\"#$col_textem\">Vars defined but not referenced : $nb_def_unref</font>
      </td>
    </tr>
      $text_def_unref
    </table>";

}


///////////////////////////////////////////////////////////////////////////////
// Display the comparison of lang vars for the langs and the module given    //
// Give information about the usage of vars from both langs
// Parametres:
//   - $mode   : "txt" ou "html"
//   - $module : module name
//   - $lang   : language src to display
//   - $lang2  : language dest to compare
///////////////////////////////////////////////////////////////////////////////
function dis_comp_lang_vars($mode, $module, $lang, $lang2) {

  $lang_v = get_lang_vars($module, $lang);
  if (! $lang_v) {
    echo "File $lang/$module.inc is missing ! Create it";
    return false;
  }
  $lang2_v = get_lang_vars($module, $lang2);
  if (! $lang2_v) {
    echo "File $lang2/$module.inc is missing ! Create it";
    return false;
  }

  $ref_v = get_src_vars($module);
  $used_v = get_src_used_vars($module);
  $unused_v = array_diff ($ref_v, $used_v);

  switch ($mode) {
  case "txt":
    txt_comp_lang_vars($module, $lang, $lang2, $lang_v, $lang2_v, $ref_v, $used_v, $unused_v);
    break;
  case "html":
    html_comp_lang_vars($module, $lang, $lang2, $lang_v, $lang2_v, $ref_v, $used_v, $unused_v);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }
}


///////////////////////////////////////////////////////////////////////////////
// TXT Display vars comparison from 2 langs and module given
// Parameters:
//   - $module   : module name
//   - $lang     : language src to display
//   - $lang2    : language dest to display
//   - $lang_v   : lang vars defined
//   - $lang2_v  : lang vars defined
//   - $ref_v    : lang vars referenced in source files
//   - $used_v   : lang vars used in source files
//   - $unused_v : lang vars referenced in source but not used
///////////////////////////////////////////////////////////////////////////////
function txt_comp_lang_vars($module, $lang, $lang2, $lang_v, $lang2_v, $ref_v, $used_v, $unused_v) {
  echo "txt mode Not implemented: Run this action in HTML mode.\n";
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display vars comparison from 2 langs and module given
// Parameters:
//   - $module   : module name
//   - $lang     : language src to display
//   - $lang2    : language dest to display
//   - $lang_v   : lang vars defined
//   - $lang2_v  : lang vars defined
//   - $ref_v    : lang vars referenced in source files
//   - $used_v   : lang vars used in source files
//   - $unused_v : lang vars referenced in source but not used
///////////////////////////////////////////////////////////////////////////////
function html_comp_lang_vars($module, $lang, $lang2, $lang_v, $lang2_v, $ref_v, $used_v, $unused_v) {
  global $col_textem, $col_a_table, $col_a_text, $col_a_tableh;

  $nb_ref = count($ref_v);
  $nb_used = count($used_v);
  $nb_unused = count($unused_v);
  $nb_lang = count($lang_v);
  $nb_lang2 = count($lang2_v);
  $nb_used_def = $nb_used_def_2 = 0;
  $nb_used_undef = $nb_used_undef_2 = 0;
  $nb_unused_def = $nb_unused_def_2 = 0;
  $nb_unused_undef = $nb_unused_undef_2 = 0;
  $nb_def_unref = $nb_def_unref_2 = 0;

  $fonttext = "<font color=\"#$col_a_text\">";

  while (list ($nb, $lval) = each ($used_v)) {
    $value = $lang_v[$lval];
    $value2 = $lang2_v[$lval];
    if ($value || $value2) {
      $text_used_def .= "<tr>
        <td bgcolor=\"#$col_a_table\">$fonttext$lval</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$value</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$value2</font></td>
        </tr>\n";
      if ($value) $nb_used_def++;
      if ($value2) $nb_used_def_2++;
    }
  }

  reset($used_v);
  while (list ($nb, $lval) = each ($used_v)) {
    $value = $lang_v[$lval];
    $value2 = $lang2_v[$lval];
    if ((! $value) || (! $value2)) {
      $text_used_undef .= "<tr>
        <td bgcolor=\"#$col_a_table\">$fonttext$lval</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$value</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$value2</font></td>
        </tr>\n";
      if (! $value) $nb_used_undef++;
      if (! $value2) $nb_used_undef_2++;
    }
  }

  reset($unused_v);
  while (list ($nb, $lval) = each ($unused_v)) {
    $value = $lang_v[$lval];
    $value2 = $lang2_v[$lval];
    if ($value || $value2) {
      $text_unused_def .= "<tr>
        <td bgcolor=\"#$col_a_table\">$fonttext$lval</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$value</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$value2</font></td>
        </tr>\n";
      if ($value) $nb_unused_def++;
      if ($value2) $nb_unused_def_2++;
    }
  }

  reset($unused_v);
  while (list ($nb, $lval) = each ($unused_v)) {
    $value = $lang_v[$lval];
    $value2 = $lang2_v[$lval];
    if ((! $value) || (! $value2)) {
      $text_unused_undef .= "<tr>
        <td bgcolor=\"#$col_a_table\">$fonttext$lval</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$value</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$value2</font></td>
        </tr>\n";
      if (! $value) $nb_unused_undef++;
      if (! $value2) $nb_unused_undef_2++;
    }
  }

  reset($lang_v);
  while (list ($lvar, $lval) = each ($lang_v)) {
    if ((! in_array($lvar, $used_v)) && (! in_array($lvar, $unused_v))) {
      $lval2 = $lang2_v[$lvar];
      $text_def_unref .= "<tr>
        <td bgcolor=\"#$col_a_table\">$fonttext$lvar</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$lval</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$lval2</font></td>
        </tr>\n";
      $nb_def_unref++;
      if (array_key_exists($lvar, $lang2_v)) $nb_def_unref_2++;
    }
  }
  reset($lang2_v);
  while (list ($lvar2, $lval2) = each ($lang2_v)) {
    // If not already processed by precedent step (lang_v)
    if ( (! array_key_exists($lvar2, $lang_v)) &&
         (! in_array($lvar2, $used_v)) && (! in_array($lvar2, $unused_v))) {
      $lval = $lang_v[$lvar2];
      $text_def_unref .= "<tr>
        <td bgcolor=\"#$col_a_table\">$fonttext$lvar2</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$lval</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$lval2</font></td>
        </tr>\n";
      $nb_def_unref_2++;
      if (array_key_exists($lvar2, $lang_v)) $nb_def_unref++;
    }
  }

  echo "<table cellspacing=0 border=0>
    <tr>
      <td colspan=3 bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">$module :
      From Source : $nb_ref var referenced : $nb_used used - $nb_unused unused
      </td>
    </tr><tr>
      <td bgcolor=\"#$col_a_tableh\">
        <font color=\"#$col_textem\">&nbsp;</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$module / $lang</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$module / $lang2</font></td>
    </tr><tr>
      <td bgcolor=\"#$col_a_tableh\">
        <font color=\"#$col_textem\">From lang file :</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_lang vars defined</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_lang2 vars defined</font></td>
    </tr><tr>
      <td colspan=3 bgcolor=\"#$col_a_table\">&nbsp;</td>
    </tr><tr>
      <td bgcolor=\"#$col_a_tableh\">
        <font color=\"#$col_textem\">Vars Used and defined :</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_used_def</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_used_def_2</font></td>
    </tr>
      $text_used_def
    <tr>
      <td bgcolor=\"#$col_a_tableh\">
        <font color=\"#$col_textem\">Vars used but not defined</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_used_undef</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_used_undef_2</font></td>
    </tr>
      $text_used_undef
    <tr>
      <td bgcolor=\"#$col_a_tableh\">
        <font color=\"#$col_textem\">Vars UNused but defined</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_unused_def</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_unused_def_2</font></td>
    </tr>
      $text_unused_def
    <tr>
      <td bgcolor=\"#$col_a_tableh\">
        <font color=\"#$col_textem\">Vars UNused and not defined</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_unused_undef</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_unused_undef_2</font></td>
    </tr>
      $text_unused_undef
    <tr>
      <td bgcolor=\"#$col_a_tableh\">
        <font color=\"#$col_textem\">Vars defined but not referenced</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_def_unref</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_def_unref_2</font></td>
    </tr>
      $text_def_unref
    </table>";

}


///////////////////////////////////////////////////////////////////////////////
// Display the comparison of lang vars for the header of the theme and the   //
// langs given
// Parametres:
//   - $mode  : "txt" ou "html"
//   - $theme : theme
//   - $lang  : language src to display
//   - $lang2 : language dest to compare
///////////////////////////////////////////////////////////////////////////////
function dis_comp_header_lang_vars($mode, $theme, $lang, $lang2) {

  $module = "header";
  $lang_v = get_lang_vars($module, $lang);
  if (! $lang_v) {
    echo "File $lang/$module.inc is missing ! Create it";
    return false;
  }
  $lang2_v = get_lang_vars($module, $lang2);
  if (! $lang2_v) {
    echo "File $lang2/$module.inc is missing ! Create it";
    return false;
  }

  $ref_v = get_src_header_vars($theme);

  switch ($mode) {
  case "txt":
    txt_comp_header_lang_vars($theme,$lang, $lang2, $lang_v, $lang2_v, $ref_v);
    break;
  case "html":
    html_comp_header_lang_vars($theme,$lang,$lang2, $lang_v, $lang2_v, $ref_v);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }
}


///////////////////////////////////////////////////////////////////////////////
// TXT Display vars comparison from 2 langs for header of the theme given    //
// Parameters:
//   - $theme   : theme name
//   - $lang    : language src to display
//   - $lang2   : language dest to display
//   - $lang_v  : lang vars defined
//   - $lang2_v : lang vars defined
//   - $ref_v   : lang vars referenced in source files
///////////////////////////////////////////////////////////////////////////////
function txt_comp_header_lang_vars($theme, $lang, $lang2, $lang_v, $lang2_v, $ref_v) {
  echo "txt mode Not implemented: Run this action in HTML mode.\n";
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display vars comparison from 2 langs for header of the theme given   //
// Parameters:
//   - $theme   : theme name
//   - $lang    : language src to display
//   - $lang2   : language dest to display
//   - $lang_v  : lang vars defined
//   - $lang2_v : lang vars defined
//   - $ref_v   : lang vars referenced in source files
///////////////////////////////////////////////////////////////////////////////
function html_comp_header_lang_vars($theme, $lang, $lang2, $lang_v, $lang2_v, $ref_v) {
  global $col_textem, $col_a_table, $col_a_text, $col_a_tableh;

  $nb_ref = count($ref_v);
  $nb_lang = count($lang_v);
  $nb_lang2 = count($lang2_v);
  $nb_ref_def = $nb_ref_def_2 = 0;
  $nb_ref_undef = $nb_ref_undef_2 = 0;
  $nb_def_unref = $nb_def_unref_2 = 0;

  $module = "header";
  $fonttext = "<font color=\"#$col_a_text\">";

  while (list ($nb, $lval) = each ($ref_v)) {
    $value = $lang_v[$lval];
    $value2 = $lang2_v[$lval];
    if ($value || $value2) {
      $text_ref_def .= "<tr>
        <td bgcolor=\"#$col_a_table\">$fonttext$lval</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$value</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$value2</font></td>
        </tr>\n";
      if ($value) $nb_ref_def++;
      if ($value2) $nb_ref_def_2++;
    }
  }

  reset($ref_v);
  while (list ($nb, $lval) = each ($ref_v)) {
    $value = $lang_v[$lval];
    $value2 = $lang2_v[$lval];
    if ((! $value) || (! $value2)) {
      $text_ref_undef .= "<tr>
        <td bgcolor=\"#$col_a_table\">$fonttext$lval</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$value</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$value2</font></td>
        </tr>\n";
      if (! $value) $nb_ref_undef++;
      if (! $value2) $nb_ref_undef_2++;
    }
  }

  reset($lang_v);
  while (list ($lvar, $lval) = each ($lang_v)) {
    if (! in_array($lvar, $ref_v)) {
      $lval2 = $lang2_v[$lvar];
      $text_def_unref .= "<tr>
        <td bgcolor=\"#$col_a_table\">$fonttext$lvar</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$lval</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$lval2</font></td>
        </tr>\n";
      $nb_def_unref++;
      if (array_key_exists($lvar, $lang2_v)) $nb_def_unref_2++;
    }
  }
  reset($lang2_v);
  while (list ($lvar2, $lval2) = each ($lang2_v)) {
    // If not already processed by precedent step (lang_v)
    if ( (! array_key_exists($lvar2, $lang_v))
           && (! in_array($lvar2, $ref_v)) ) {
      $lval = $lang_v[$lvar2];
      $text_def_unref .= "<tr>
        <td bgcolor=\"#$col_a_table\">$fonttext$lvar2</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$lval</font></td>
        <td bgcolor=\"#$col_a_table\">$fonttext$lval2</font></td>
        </tr>\n";
      $nb_def_unref_2++;
      if (array_key_exists($lvar2, $lang_v)) $nb_def_unref++;
    }
  }

  echo "<table cellspacing=0 border=0>
    <tr>
      <td colspan=3 bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">$theme / $module :
      From Source : $nb_ref var referenced
      </td>
    </tr><tr>
      <td bgcolor=\"#$col_a_tableh\">
        <font color=\"#$col_textem\">&nbsp;</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$theme / $module / $lang</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$theme / $module / $lang2</font></td>
    </tr><tr>
      <td bgcolor=\"#$col_a_tableh\">
        <font color=\"#$col_textem\">From lang file :</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_lang vars defined</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_lang2 vars defined</font></td>
    </tr><tr>
      <td colspan=3 bgcolor=\"#$col_a_table\">&nbsp;</td>
    </tr><tr>
      <td bgcolor=\"#$col_a_tableh\">
        <font color=\"#$col_textem\">Vars referenced and defined</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_ref_def</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_ref_def_2</font></td>
    </tr>
      $text_ref_def
    <tr>
      <td bgcolor=\"#$col_a_tableh\">
        <font color=\"#$col_textem\">Vars referenced but not defined</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_ref_undef</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_ref_undef_2</font></td>
    </tr>
      $text_ref_undef
    <tr>
      <td bgcolor=\"#$col_a_tableh\">
        <font color=\"#$col_textem\">Vars defined but not referenced</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_def_unref</font></td>
      <td bgcolor=\"#$col_a_tableh\" align=center>
        <font color=\"#$col_textem\">$nb_def_unref_2</font></td>
    </tr>
      $text_def_unref
    </table>";
}
